import { useState } from "react";
import DirectionStep from "../../catalog/components/DirectionStep";
import TemplateStep from "../../catalog/components/TemplateStep";
import type { Direction, Template } from "../types";
import { CreateGoalRequest } from "../../types";
import { GoalForm } from "../../index";

type WizardState =
  | { step: "direction" }
  | { step: "template"; direction: Direction }
  | {
      step: "customize";
      direction: Direction | null;
      template: Template | null;
    };

type Props = {
  onCreate: (req: CreateGoalRequest) => Promise<void>;
  onDone?: () => void;
};

export default function GoalWizard({ onCreate, onDone }: Props) {
  const [state, setState] = useState<WizardState>({ step: "direction" });

  if (state.step === "direction") {
    return (
      <DirectionStep
        onPick={(direction) => setState({ step: "template", direction })}
        onCustom={() =>
          setState({ step: "customize", direction: null, template: null })
        }
      />
    );
  }

  if (state.step === "template") {
    return (
      <TemplateStep
        direction={state.direction}
        onBack={() => setState({ step: "direction" })}
        onPick={(template) =>
          setState({ step: "customize", direction: state.direction, template })
        }
      />
    );
  }

  return (
    <div>
      <button
        type="button"
        className="btn-outline"
        onClick={() =>
          setState(
            state.direction
              ? { step: "template", direction: state.direction }
              : { step: "direction" },
          )
        }
      >
        ← Tilbake
      </button>
      {state.template && <p className="muted">Valgt: {state.template.title}</p>}
      {/* Midlertidig: erstattes av CustomizeStep */}
      <GoalForm onCreate={onCreate} onDone={onDone} />
    </div>
  );
}
