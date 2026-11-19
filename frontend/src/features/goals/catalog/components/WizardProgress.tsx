/*The progress in creating a goal using a template bar*/
const LABELS = ["Area", "Focus", "Goal"];

type Props = { step: 1 | 2 | 3; color?: string | null };

export default function WizardProgress({ step, color }: Props) {
  return (
    <div className="wizard-progress" aria-label={`Step ${step} of 3`}>
      {LABELS.map((label, i) => (
        <span
          key={label}
          style={{
            display: "inline-block",
            width: 28,
            height: 6,
            marginRight: 6,
            borderRadius: 3,
            background: i + 1 <= step ? (color ?? "#555") : "#ddd",
          }}
        />
      ))}
      <span className="muted">
        Step {step} of 3 · {LABELS[step - 1]}
      </span>
    </div>
  );
}
