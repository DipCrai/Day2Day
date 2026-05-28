type ViewType = 'day' | 'week' | 'month';

interface ViewToggleProps {
  view: ViewType;
  onViewChange: (view: ViewType) => void;
}

export function ViewToggle({ view, onViewChange }: ViewToggleProps) {
  const views: { value: ViewType; label: string }[] = [
    { value: 'day', label: 'День' },
    { value: 'week', label: 'Неделя' },
    { value: 'month', label: 'Месяц' },
  ];

  return (
    <div className="flex gap-0.5 md:gap-1 p-0.5 md:p-1 bg-muted rounded-lg">
      {views.map((v) => (
        <button
          key={v.value}
          onClick={() => onViewChange(v.value)}
          className={`px-2 py-1.5 md:px-4 md:py-2 rounded-md transition-colors text-sm md:text-base ${
            view === v.value
              ? 'bg-background shadow-sm'
              : 'hover:bg-background/50'
          }`}
        >
          {v.label}
        </button>
      ))}
    </div>
  );
}
