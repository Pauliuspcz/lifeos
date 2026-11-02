import { useState, type ReactNode, type SubmitEvent } from "react";
import { ApiError, FieldErrors } from "../../../../shared/api/client";
import { FIELDS_BY_TRACKING } from "../../GoalDisplay";
import {
  CreateGoalRequest,
  GoalPeriod,
  GoalType,
  PERIOD_LABEL,
  PERIODS,
  TRACKING_TYPES,
  TrackingType,
} from "../../types";
import { Template } from "../types";

const TRACKING_LABEL: Record<TrackingType, string> = {
  CHECK: "Done / not done",
  COUNT: "Number of times",
  AMOUNT: "Amount",
  DURATION: "Duration",
  LIMIT: "Max limit",
  TIME_TARGET: "Time of day",
  EVENT: "Goal with a date",
};

type Props = {
  template: Template | null;
  onBack: () => void;
  onCreate: (req: CreateGoalRequest) => Promise<void>;
  onDone?: () => void;
};

type FormState = {
  title: string;
  description: string;
  goalType: GoalType;
  trackingType: TrackingType;
  period: GoalPeriod;
  timesPerPeriod: number;
  amount: number | null;
  unit: string;
  targetTime: string;
  targetDate: string;
  motivation: string;
  color: string;
};

function initialState(t: Template | null): FormState {
  return {
    title: t?.title ?? "",
    description: t?.description ?? "",
    goalType: t?.goalType ?? "HABIT",
    trackingType: t?.trackingType ?? "COUNT",
    period: t?.defaultPeriod ?? "WEEKLY",
    timesPerPeriod: t?.defaultTimesPerPeriod ?? 1,
    amount: t?.defaultTargetAmount ?? null,
    unit: t?.defaultUnit ?? "",
    targetTime: t?.defaultTargetTime?.slice(0, 5) ?? "",
    targetDate: "",
    motivation: "",
    color: t?.color ?? "#4caf50",
  };
}

function Field(props: { label: string; error?: string; children: ReactNode }) {
  return (
    <div>
      <label>
        {props.label}
        {props.children}
      </label>
      {props.error && <p className="error">{props.error}</p>}
    </div>
  );
}

export default function CustomizeStep({
  template,
  onBack,
  onCreate,
  onDone,
}: Props) {
  const [form, setForm] = useState<FormState>(() => initialState(template));
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [error, setError] = useState("");

  const fields = FIELDS_BY_TRACKING[form.trackingType];
  const showAmount = fields.includes("amount") || form.trackingType === "COUNT";
  const amountRequired = fields.includes("amount");
  const set = (patch: Partial<FormState>) =>
    setForm((f) => ({ ...f, ...patch }));

  function validate(): FieldErrors {
    const e: FieldErrors = {};
    const today = new Date().toISOString().slice(0, 10);
    if (!form.title.trim()) e.title = "Title is required";
    if (fields.includes("timesPerPeriod") && form.timesPerPeriod < 1)
      e.timesPerPeriod = "At least 1";
    if (amountRequired && (form.amount === null || form.amount <= 0))
      e.amountPerSession = "Amount must be greater than 0";
    if (amountRequired && !form.unit.trim()) e.unit = "Unit is required";
    if (fields.includes("targetTime") && !form.targetTime)
      e.targetTime = "Pick a time";
    if (fields.includes("targetDate") && form.targetDate <= today)
      e.targetDate = "Pick a future date";
    return e;
  }

  async function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");
    const local = validate();
    setFieldErrors(local);
    if (Object.keys(local).length > 0) return;

    const req: CreateGoalRequest = {
      title: form.title.trim(),
      description: form.description.trim() || null,
      goalType: form.trackingType === "EVENT" ? "EVENT" : form.goalType,
      trackingType: form.trackingType,
      period: fields.includes("period") ? form.period : null,
      timesPerPeriod: fields.includes("timesPerPeriod")
        ? form.timesPerPeriod
        : null,
      amountPerSession: showAmount ? form.amount : null,
      unit: showAmount ? form.unit.trim() || null : null,
      targetTime: fields.includes("targetTime") ? form.targetTime : null,
      targetDate: fields.includes("targetDate") ? form.targetDate : null,
      motivation: form.motivation.trim() || null,
      color: form.color,
      iconKey: template?.iconKey ?? null,
      categoryId: template?.categoryId ?? null,
      templateId: template?.id ?? null,
    };

    try {
      await onCreate(req);
      onDone?.();
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        setFieldErrors(err.fieldErrors);
      } else if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Could not create goal");
      }
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <button type="button" className="btn-outline" onClick={onBack}>
        ← Back
      </button>
      <h3>{template ? template.title : "Create your own goal"}</h3>

      {!template && (
        <Field label="How do you want to track it?">
          <select
            value={form.trackingType}
            onChange={(e) =>
              set({ trackingType: e.target.value as TrackingType })
            }
          >
            {TRACKING_TYPES.map((t) => (
              <option key={t} value={t}>
                {TRACKING_LABEL[t]}
              </option>
            ))}
          </select>
        </Field>
      )}

      <Field label="Title" error={fieldErrors.title}>
        <input
          value={form.title}
          maxLength={255}
          onChange={(e) => set({ title: e.target.value })}
        />
      </Field>

      {fields.includes("period") && (
        <Field label="Period" error={fieldErrors.period}>
          <select
            value={form.period}
            onChange={(e) => set({ period: e.target.value as GoalPeriod })}
          >
            {PERIODS.map((p) => (
              <option key={p} value={p}>
                per {PERIOD_LABEL[p]}
              </option>
            ))}
          </select>
        </Field>
      )}

      {fields.includes("timesPerPeriod") && (
        <Field label="Times per period" error={fieldErrors.timesPerPeriod}>
          <input
            type="number"
            min={1}
            value={form.timesPerPeriod}
            onChange={(e) => set({ timesPerPeriod: Number(e.target.value) })}
          />
        </Field>
      )}

      {showAmount && (
        <>
          <Field
            label={amountRequired ? "Amount" : "Amount per session (optional)"}
            error={fieldErrors.amountPerSession}
          >
            <input
              type="number"
              min={0}
              step="0.01"
              value={form.amount ?? ""}
              onChange={(e) =>
                set({
                  amount: e.target.value === "" ? null : Number(e.target.value),
                })
              }
            />
          </Field>
          <Field label="Unit (e.g. km, L, min)" error={fieldErrors.unit}>
            <input
              value={form.unit}
              maxLength={50}
              onChange={(e) => set({ unit: e.target.value })}
            />
          </Field>
        </>
      )}

      {fields.includes("targetTime") && (
        <Field label="Time of day" error={fieldErrors.targetTime}>
          <input
            type="time"
            value={form.targetTime}
            onChange={(e) => set({ targetTime: e.target.value })}
          />
        </Field>
      )}

      {fields.includes("targetDate") && (
        <Field label="Date" error={fieldErrors.targetDate}>
          <input
            type="date"
            value={form.targetDate}
            onChange={(e) => set({ targetDate: e.target.value })}
          />
        </Field>
      )}

      <Field
        label="Why do you want this? (optional)"
        error={fieldErrors.motivation}
      >
        <input
          value={form.motivation}
          maxLength={500}
          onChange={(e) => set({ motivation: e.target.value })}
        />
      </Field>

      <Field label="Description (optional)" error={fieldErrors.description}>
        <input
          value={form.description}
          maxLength={1000}
          onChange={(e) => set({ description: e.target.value })}
        />
      </Field>

      <Field label="Color" error={fieldErrors.color}>
        <input
          type="color"
          value={form.color}
          onChange={(e) => set({ color: e.target.value })}
        />
      </Field>

      <button type="submit">Create</button>
      {error && <p className="error">{error}</p>}
    </form>
  );
}
