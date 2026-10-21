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

export default function GoalForm({ onCreate, onDone }: Props) {
  const [form, setForm] = useState<CreateGoalRequest>({
    title: "",
    description: null,
    period: "WEEKLY",
    timesPerPeriod: 1,
    amountPerSession: null,
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
    if (Object.keys(local).length > 0) {
      setFieldErrors(local);
      return;
    }

    try {
      await onCreate(form);
      setFieldErrors({});
      setForm({ ...form, title: "" });
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
  // return (...) som før, med feilmeldinger under feltene, se under
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
        <button type="submit">Opprett</button>
      </form>
      {error && <p className="error">{error}</p>}
    </>
  );
}
