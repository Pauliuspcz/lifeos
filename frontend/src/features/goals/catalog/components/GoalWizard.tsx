import { useState } from "react";
import type { Category, Direction, Template } from "../types";
import { CreateGoalRequest } from "../../types";
import AreaStep from "./steps/AreaStep";
import FocusStep from "./steps/FocusStep";
import TemplateStep from "./steps/TemplateStep";
import CustomizeStep from "./steps/CustomizeStep";
import WizardHeader from "./WizardHeader";

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
  title: string;
  variant: "page" | "dialog";
  onClose?: () => void;
  onCreate: (req: CreateGoalRequest) => Promise<void>;
  onDone?: () => void;
};

export default function GoalWizard({
  title,
  variant,
  onClose,
  onCreate,
  onDone,
}: Props) {
  const [state, setState] = useState<WizardState>({ step: "area" });

  function goBack() {
    switch (state.step) {
      case "area":
        return;
      case "focus":
        setState({ step: "area" });
        return;
      case "template":
        setState({ step: "focus", area: state.area });
        return;
      case "customize":
        setState(
          state.direction
            ? { step: "template", area: state.area, direction: state.direction }
            : { step: "focus", area: state.area },
        );
        return;
    }
  }

  function renderStep() {
    if (state.step === "area") {
      return <AreaStep onPick={(area) => setState({ step: "focus", area })} />;
    }

    if (state.step === "focus") {
      const { area } = state;
      return (
        <FocusStep
          area={area}
          onPick={(direction) =>
            setState({ step: "template", area, direction })
          }
          onCustom={() =>
            setState({
              step: "customize",
              area,
              direction: null,
              template: null,
            })
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
          onPick={(template) =>
            setState({ step: "customize", area, direction, template })
          }
          onCustom={() =>
            setState({ step: "customize", area, direction, template: null })
          }
        />
      );
    }

    return (
      <CustomizeStep
        area={state.area}
        template={state.template}
        onCreate={onCreate}
        onDone={onDone}
      />
    );
  }

  return (
    <div>
      <WizardHeader
        title={title}
        variant={variant}
        onBack={state.step === "area" ? undefined : goBack}
        onClose={onClose}
      />
      {renderStep()}
    </div>
  );
}
