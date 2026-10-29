import { FormEvent, useState } from "react";
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
  CHECK: "Gjort / ikke gjort",
  COUNT: "Antall ganger",
  AMOUNT: "Mengde",
  DURATION: "Tid",
  LIMIT: "Maksgrense",
  TIME_TARGET: "Klokkeslett",
  EVENT: "Mål med dato",
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

function Field(props: {
  label: string;
  error?: string;
  children: React.ReactNode;
}) {
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
    if (!form.title.trim()) e.title = "Tittel må fylles ut";
    if (fields.includes("timesPerPeriod") && form.timesPerPeriod < 1)
      e.timesPerPeriod = "Minst 1";
    if (amountRequired && (form.amount === null || form.amount <= 0))
      e.amountPerSession = "Mengde må være større enn 0";
    if (amountRequired && !form.unit.trim()) e.unit = "Enhet må fylles ut";
    if (fields.includes("targetTime") && !form.targetTime)
      e.targetTime = "Velg et klokkeslett";
    if (fields.includes("targetDate") && form.targetDate <= today)
      e.targetDate = "Velg en dato frem i tid";
    return e;
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
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
        setError("Kunne ikke opprette mål");
      }
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <button type="button" className="btn-outline" onClick={onBack}>
        ← Tilbake
      </button>
      <h3>{template ? template.title : "Lag ditt eget mål"}</h3>

      {!template && (
        <Field label="Hvordan vil du måle det?">
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

      <Field label="Tittel" error={fieldErrors.title}>
        <input
          value={form.title}
          maxLength={255}
          onChange={(e) => set({ title: e.target.value })}
        />
      </Field>

      {fields.includes("period") && (
        <Field label="Periode" error={fieldErrors.period}>
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
        <Field label="Ganger per periode" error={fieldErrors.timesPerPeriod}>
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
            label={amountRequired ? "Mengde" : "Mengde per økt (valgfritt)"}
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
          <Field label="Enhet (f.eks. km, L, min)" error={fieldErrors.unit}>
            <input
              value={form.unit}
              maxLength={50}
              onChange={(e) => set({ unit: e.target.value })}
            />
          </Field>
        </>
      )}

      {fields.includes("targetTime") && (
        <Field label="Klokkeslett" error={fieldErrors.targetTime}>
          <input
            type="time"
            value={form.targetTime}
            onChange={(e) => set({ targetTime: e.target.value })}
          />
        </Field>
      )}

      {fields.includes("targetDate") && (
        <Field label="Dato" error={fieldErrors.targetDate}>
          <input
            type="date"
            value={form.targetDate}
            onChange={(e) => set({ targetDate: e.target.value })}
          />
        </Field>
      )}

      <Field
        label="Hvorfor vil du dette? (valgfritt)"
        error={fieldErrors.motivation}
      >
        <input
          value={form.motivation}
          maxLength={500}
          onChange={(e) => set({ motivation: e.target.value })}
        />
      </Field>

      <Field label="Beskrivelse (valgfritt)" error={fieldErrors.description}>
        <input
          value={form.description}
          maxLength={1000}
          onChange={(e) => set({ description: e.target.value })}
        />
      </Field>

      <Field label="Farge" error={fieldErrors.color}>
        <input
          type="color"
          value={form.color}
          onChange={(e) => set({ color: e.target.value })}
        />
      </Field>

      <button type="submit">Opprett</button>
      {error && <p className="error">{error}</p>}
    </form>
  );
}
