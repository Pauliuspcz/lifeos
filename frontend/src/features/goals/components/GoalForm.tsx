import { ApiError, type FieldErrors } from "../../../shared/api/client";
import { CreateGoalRequest, GoalPeriod, PERIODS, PERIOD_LABEL } from "../types";
import React, { useState, type SubmitEvent } from "react";

type Props = {
  onCreate: (req: CreateGoalRequest) => Promise<void>;
  onDone?: () => void;
};

function errorText(err: unknown): string {
  if (err instanceof ApiError) return `Kunne ikke opprette mål (${err.status})`;
  return "Kunne ikke opprette mål";
}

type FormState = CreateGoalRequest & {
  period: GoalPeriod;
  timesPerPeriod: number;
};

export default function GoalForm({ onCreate, onDone }: Props) {
  const [form, setForm] = useState<FormState>({
    title: "",
    description: null,
    period: "WEEKLY",
    timesPerPeriod: 1,
    amountPerSession: null,
    unit: null,
  });
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [error, setError] = useState("");

  async function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");

    const local: FieldErrors = {};
    if (!form.title.trim()) local.title = "Tittel må fylles ut";
    if (form.timesPerPeriod < 1)
      local.timesPerPeriod = "Antall ganger må være minst 1";
    if (form.amountPerSession != null && form.amountPerSession <= 0)
      local.amountPerSession = "Mengde må være større enn 0";
    if (Object.keys(local).length > 0) {
      setFieldErrors(local);
      return;
    }

    try {
      await onCreate(form);
      setFieldErrors({});
      setForm({ ...form, title: "", description: null });
      onDone?.();
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        setFieldErrors(err.fieldErrors);
      } else {
        setFieldErrors({});
        setError(errorText(err));
      }
    }
  }
  return (
    <>
      <form onSubmit={handleSubmit}>
        <div>
          <input
            placeholder="Tittel"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />
          {fieldErrors.title && <p className="error">{fieldErrors.title}</p>}
        </div>
        <div>
          <select
            value={form.period}
            onChange={(e) =>
              setForm({ ...form, period: e.target.value as GoalPeriod })
            }
          >
            {PERIODS.map((p) => (
              <option key={p} value={p}>
                per {PERIOD_LABEL[p]}
              </option>
            ))}
          </select>
          {fieldErrors.period && <p className="error">{fieldErrors.period}</p>}
        </div>
        <div>
          <input
            type="number"
            placeholder="Ganger per periode"
            min={1}
            value={form.timesPerPeriod}
            onChange={(e) =>
              setForm({ ...form, timesPerPeriod: Number(e.target.value) })
            }
          />
          {fieldErrors.timesPerPeriod && (
            <p className="error">{fieldErrors.timesPerPeriod}</p>
          )}
        </div>
        <div>
          <input
            placeholder="Beskrivelse (valgfritt)"
            maxLength={1000}
            value={form.description ?? ""}
            onChange={(e) =>
              setForm({ ...form, description: e.target.value || null })
            }
          />
          {fieldErrors.description && (
            <p className="error">{fieldErrors.description}</p>
          )}
        </div>
        <div>
          <input
            type="number"
            placeholder="Mengde per økt"
            min={0}
            step="0.01"
            value={form.amountPerSession ?? ""}
            onChange={(e) =>
              setForm({
                ...form,
                amountPerSession:
                  e.target.value === "" ? null : Number(e.target.value),
              })
            }
          />
          {fieldErrors.amountPerSession && (
            <p className="error">{fieldErrors.amountPerSession}</p>
          )}
        </div>
        <div>
          <input
            placeholder="Enhet (f.eks. km)"
            maxLength={50}
            value={form.unit ?? ""}
            onChange={(e) => setForm({ ...form, unit: e.target.value || null })}
          />
          {fieldErrors.unit && <p className="error">{fieldErrors.unit}</p>}
        </div>
        <button type="submit">Opprett</button>
      </form>
      {error && <p className="error">{error}</p>}
    </>
  );
}
