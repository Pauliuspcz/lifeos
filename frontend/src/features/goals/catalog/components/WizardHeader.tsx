type Props = {
  title: string;
  variant: "page" | "dialog";
  onBack?: () => void;
  onClose?: () => void;
};

export default function WizardHeader({
  title,
  variant,
  onBack,
  onClose,
}: Props) {
  const back = onBack && (
    <button type="button" className="btn-outline" onClick={onBack}>
      ← Back
    </button>
  );
  const close = onClose && (
    <button type="button" className="btn-outline" onClick={onClose}>
      Close
    </button>
  );

  if (variant === "dialog") {
    return (
      <div className="wizard-header is-dialog">
        <div className="wizard-header-slot">{back}</div>
        <h2>{title}</h2>
        <div className="wizard-header-slot end">{close}</div>
      </div>
    );
  }
  return (
    <div className="wizard-header is-page">
      <h2>{title}</h2>
      <div className="wizard-header-slot end">{back}</div>
    </div>
  );
}
