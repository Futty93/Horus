interface AssignFlightPlansBarProps {
  onAssign: () => void;
  progress: string | null;
  errors: string[];
}

export function AssignFlightPlansBar({
  onAssign,
  progress,
  errors,
}: AssignFlightPlansBarProps) {
  return (
    <div className="space-y-2">
      <button
        type="button"
        onClick={onAssign}
        disabled={!!progress}
        className="px-4 py-2 text-sm font-bold bg-atc-accent text-white rounded
                   hover:opacity-90 disabled:opacity-50"
      >
        Assign Flight Plans to All
      </button>
      {progress && <p className="text-sm text-atc-accent">{progress}</p>}
      {errors.length > 0 && (
        <div className="text-sm text-atc-danger space-y-1">
          {errors.map((e, i) => (
            <p key={i}>{e}</p>
          ))}
        </div>
      )}
    </div>
  );
}
