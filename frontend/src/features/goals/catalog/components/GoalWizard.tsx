import { useState } from "react";
import DirectionStep from "../../catalog/components/DirectionStep";
import TemplateStep from "../../catalog/components/TemplateStep";
import type { Direction, Template } from "../types";
import { CreateGoalRequest } from "../../types";
import CustomizeStep from "./CustomizeStep";

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
    <CustomizeStep
      template={state.template}
      onBack={() =>
        setState(
          state.direction
            ? { step: "template", direction: state.direction }
            : { step: "direction" },
        )
      }
      onCreate={onCreate}
      onDone={onDone}
    />
  );
}
