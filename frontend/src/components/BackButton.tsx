export default function BackButton({ onBack }: { onBack: () => void }) {
  return (
    <button onClick={onBack} className="back-button">
      Return
    </button>
  );
}
