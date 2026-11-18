import { useState, type ReactNode, type SubmitEvent } from "react";
import { ApiError, FieldErrors } from "../../../../../shared/api/client";
import {
  CreateGoalRequest,
  GoalPeriod,
  PERIOD_LABEL,
  TrackingType,
} from "../../../types";
import { addDays, todayIso } from "../../dateUtils";
import type { Category, Template } from "../../types";
import WizardProgress from "../WizardProgress";

const CHECK_PERIODS: GoalPeriod[] = ["DAILY", "WEEKLY", "MONTHLY"];
const AMOUNT_PERIODS: GoalPeriod[] = ["WEEKLY", "MONTHLY"];

const TRACKING_LABEL: Record<TrackingType, string> = {
  CHECK: "Done / not done",
  AMOUNT: "Add up an amount (e.g. 1,000 km)",
};

type AmountMode = "PERIOD" | "DATE";

type Props = {
  area: Category;
  template: Template | null;
  onBack: () => void;
  onCreate: (req: CreateGoalRequest) => Promise<void>;
  onDone?: () => void;
};

type FormState = {
  title: string;
  trackingType: TrackingType;
  period: GoalPeriod;
  timesPerPeriod: number;
  amount: number | null;
  unit: string;
  amountMode: AmountMode;
  amountPeriod: GoalPeriod;
  startDate: string;
  endDate: string;
  targetTime: string;
  motivation: string;
  outdoor: boolean;
};

function initialState(t: Template | null): FormState {
  const trackingType = t?.trackingType ?? "CHECK";
  return {
    title: t?.title ?? "",
    trackingType,
    period: trackingType === "CHECK" ? (t?.defaultPeriod ?? "DAILY") : "DAILY",
    timesPerPeriod: t?.defaultTimesPerPeriod ?? 1,
    amount: t?.defaultTargetAmount ?? null,
    unit: t?.defaultUnit ?? "",
    amountMode:
      t && trackingType === "AMOUNT" && !t.defaultPeriod ? "DATE" : "PERIOD",
    amountPeriod: t?.defaultPeriod ?? "MONTHLY",
    startDate: todayIso(),
    endDate: "",
    targetTime: t?.defaultTargetTime?.slice(0, 5) ?? "",
    motivation: "",
    outdoor: t?.outdoor ?? false,
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
  area,
  template,
  onBack,
  onCreate,
  onDone,
}: Props) {
  const [form, setForm] = useState<FormState>(() => initialState(template));
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [error, setError] = useState("");
  const set = (patch: Partial<FormState>) =>
    setForm((f) => ({ ...f, ...patch }));

  const today = todayIso();
  const isCheck = form.trackingType === "CHECK";
  const needsDate = !isCheck && form.amountMode === "DATE";
  const durationDays = template?.defaultDurationDays ?? null;
  const challengeEnd = durationDays
    ? addDays(form.startDate || today, durationDays)
    : null;
  const showTime = isCheck && !!template?.defaultTargetTime;
  const canPickOutdoor = area.code === "BODY";

  function summary(): string {
    let text: string;
    if (isCheck) {
      text =
        form.period === "DAILY"
          ? "Every day"
          : `${form.timesPerPeriod}× per ${PERIOD_LABEL[form.period]}`;
      if (durationDays) text += ` for ${durationDays} days`;
    } else {
      const amount = `${form.amount ?? "…"} ${form.unit}`.trim();
      text = needsDate
        ? `${amount} by ${form.endDate || "…"}`
        : `${amount} per ${PERIOD_LABEL[form.amountPeriod]}`;
    }
    return `${text}, starting ${form.startDate || "…"}`;
  }

  function validate(): FieldErrors {
    const e: FieldErrors = {};
    if (!form.title.trim()) e.title = "Title is required";
    if (!form.startDate) e.startDate = "Pick a start date";
    if (isCheck && form.period !== "DAILY" && form.timesPerPeriod < 1)
      e.timesPerPeriod = "At least 1";
    if (!isCheck && (form.amount === null || form.amount <= 0))
      e.targetAmount = "Amount must be greater than 0";
    if (!isCheck && !form.unit.trim()) e.unit = "Unit is required";
    if (needsDate && (!form.endDate || form.endDate <= today))
      e.targetDate = "Pick a future date";
    if (isCheck && !challengeEnd && form.endDate && form.endDate <= today)
      e.targetDate = "Pick a future date";
    if (form.endDate && form.startDate && form.endDate <= form.startDate)
      e.targetDate = "End date must be after the start date";
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
      description: template?.description ?? null,
      trackingType: form.trackingType,
      period: isCheck ? form.period : needsDate ? null : form.amountPeriod,
      timesPerPeriod: isCheck
        ? form.period === "DAILY"
          ? 1
          : form.timesPerPeriod
        : null,
      targetAmount: isCheck ? null : form.amount,
      unit: isCheck ? null : form.unit.trim(),
      targetTime: showTime && form.targetTime ? form.targetTime : null,
      startDate: form.startDate,
      targetDate: isCheck
        ? (challengeEnd ?? (form.endDate || null))
        : needsDate
          ? form.endDate
          : null,
      categoryId: area.id,
      templateId: template?.id ?? null,
      motivation: form.motivation.trim() || null,
      iconKey: template?.iconKey ?? area.iconKey ?? null,
      color: area.color ?? null,
      outdoor: canPickOutdoor ? form.outdoor : (template?.outdoor ?? false),
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
    <form className="wizard-form" onSubmit={handleSubmit}>
      <WizardProgress step={3} color={area.color} />
      <button type="button" className="btn-outline" onClick={onBack}>
        ← Back
      </button>
      <h3>{template ? template.title : "Create your own goal"}</h3>
      <span
        className="area-chip"
        style={{
          borderLeft: `4px solid ${area.color ?? "#ccc"}`,
          paddingLeft: 8,
        }}
      >
        {area.name}
      </span>
      <p className="goal-summary">{summary()}</p>
      {durationDays && (
        <p className="muted">
          Challenge · {durationDays} days, ends {challengeEnd}
        </p>
      )}

      {!template && (
        <Field label="How do you want to track it?">
          <select
            value={form.trackingType}
            onChange={(e) =>
              set({ trackingType: e.target.value as TrackingType })
            }
          >
            {(Object.keys(TRACKING_LABEL) as TrackingType[]).map((t) => (
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

      {isCheck && (
        <>
          <Field label="How often?" error={fieldErrors.period}>
            <select
              value={form.period}
              onChange={(e) => set({ period: e.target.value as GoalPeriod })}
            >
              {CHECK_PERIODS.map((p) => (
                <option key={p} value={p}>
                  {p === "DAILY" ? "Every day" : `Per ${PERIOD_LABEL[p]}`}
                </option>
              ))}
            </select>
          </Field>
          {form.period !== "DAILY" && (
            <Field
              label={`Times per ${PERIOD_LABEL[form.period]}`}
              error={fieldErrors.timesPerPeriod}
            >
              <input
                type="number"
                min={1}
                value={form.timesPerPeriod}
                onChange={(e) =>
                  set({ timesPerPeriod: Number(e.target.value) })
                }
              />
            </Field>
          )}
        </>
      )}

      {!isCheck && (
        <>
          <Field label="Amount" error={fieldErrors.targetAmount}>
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
          <Field label="Unit (e.g. km, books, reps)" error={fieldErrors.unit}>
            <input
              value={form.unit}
              maxLength={50}
              onChange={(e) => set({ unit: e.target.value })}
            />
          </Field>
          <Field label="When should you reach it?">
            <select
              value={form.amountMode}
              onChange={(e) =>
                set({ amountMode: e.target.value as AmountMode })
              }
            >
              <option value="PERIOD">Every week or month</option>
              <option value="DATE">By a date</option>
            </select>
          </Field>
          {form.amountMode === "PERIOD" && (
            <Field label="Per" error={fieldErrors.period}>
              <select
                value={form.amountPeriod}
                onChange={(e) =>
                  set({ amountPeriod: e.target.value as GoalPeriod })
                }
              >
                {AMOUNT_PERIODS.map((p) => (
                  <option key={p} value={p}>
                    {PERIOD_LABEL[p]}
                  </option>
                ))}
              </select>
            </Field>
          )}
        </>
      )}

      <Field label="Start date" error={fieldErrors.startDate}>
        <input
          type="date"
          min={today}
          value={form.startDate}
          onChange={(e) => set({ startDate: e.target.value })}
        />
      </Field>

      {(needsDate || (isCheck && !durationDays)) && (
        <Field
          label={needsDate ? "Target date" : "End date (optional)"}
          error={fieldErrors.targetDate}
        >
          <input
            type="date"
            value={form.endDate}
            onChange={(e) => set({ endDate: e.target.value })}
          />
        </Field>
      )}

      {showTime && (
        <Field label="Time of day (optional)" error={fieldErrors.targetTime}>
          <input
            type="time"
            value={form.targetTime}
            onChange={(e) => set({ targetTime: e.target.value })}
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

      {canPickOutdoor && (
        <div>
          <label className="checkbox-row">
            <input
              type="checkbox"
              checked={form.outdoor}
              onChange={(e) => set({ outdoor: e.target.checked })}
            />
            Outdoor activity
          </label>
          <p className="muted">
            Get weather tips for this goal on your dashboard.
          </p>
        </div>
      )}

      <button type="submit">Create</button>
      {error && <p className="error">{error}</p>}
    </form>
  );
}
