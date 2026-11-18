import { useState } from "react";
import type { Category, Direction, Template } from "../types";
import { CreateGoalRequest } from "../../types";
import AreaStep from "./steps/AreaStep";
import FocusStep from "./steps/FocusStep";
import TemplateStep from "./steps/TemplateStep";
import CustomizeStep from "./steps/CustomizeStep";

type WizardState =
  | { step: "area" }
  | { step: "focus"; area: Category }
  | { step: "template"; area: Category; direction: Direction }
  | {
      step: "customize";
      area: Category;
      direction: Direction | null;
      template: Template | null;
    };

type Props = {
  onCreate: (req: CreateGoalRequest) => Promise<void>;
  onDone?: () => void;
};

export default function GoalWizard({ onCreate, onDone }: Props) {
  const [state, setState] = useState<WizardState>({ step: "area" });

  if (state.step === "area") {
    return <AreaStep onPick={(area) => setState({ step: "focus", area })} />;
  }

  if (state.step === "focus") {
    const { area } = state;
    return (
      <FocusStep
        area={area}
        onBack={() => setState({ step: "area" })}
        onPick={(direction) => setState({ step: "template", area, direction })}
        onCustom={() =>
          setState({ step: "customize", area, direction: null, template: null })
        }
      />
    );
  }

  if (state.step === "template") {
    const { area, direction } = state;
    return (
      <TemplateStep
        area={area}
        direction={direction}
        onBack={() => setState({ step: "focus", area })}
        onPick={(template) =>
          setState({ step: "customize", area, direction, template })
        }
        onCustom={() =>
          setState({ step: "customize", area, direction, template: null })
        }
      />
    );
  }

  const { area, direction, template } = state;
  return (
    <CustomizeStep
      area={area}
      template={template}
      onBack={() =>
        setState(
          direction
            ? { step: "template", area, direction }
            : { step: "focus", area },
        )
      }
      onCreate={onCreate}
      onDone={onDone}
    />
  );
}
