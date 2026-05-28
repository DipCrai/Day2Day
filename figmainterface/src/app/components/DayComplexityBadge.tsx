interface DayComplexityBadgeProps {
  totalComplexity: number;
}

export function DayComplexityBadge({ totalComplexity }: DayComplexityBadgeProps) {
  const getComplexityColor = (complexity: number) => {
    if (complexity <= 20) return { bg: 'bg-green-500/20', text: 'text-green-700', border: 'border-green-500' };
    if (complexity <= 40) return { bg: 'bg-blue-500/20', text: 'text-blue-700', border: 'border-blue-500' };
    if (complexity <= 60) return { bg: 'bg-yellow-500/20', text: 'text-yellow-700', border: 'border-yellow-500' };
    if (complexity <= 80) return { bg: 'bg-orange-500/20', text: 'text-orange-700', border: 'border-orange-500' };
    return { bg: 'bg-red-500/20', text: 'text-red-700', border: 'border-red-500' };
  };

  const getComplexityLabel = (complexity: number) => {
    if (complexity <= 20) return 'Легкий день';
    if (complexity <= 40) return 'Умеренный';
    if (complexity <= 60) return 'Напряженный';
    if (complexity <= 80) return 'Сложный день';
    return 'Очень сложный';
  };

  const colors = getComplexityColor(totalComplexity);
  const label = getComplexityLabel(totalComplexity);

  return (
    <div className={`inline-flex items-center gap-1 sm:gap-2 px-2 py-1 sm:px-3 sm:py-1.5 rounded-lg border ${colors.bg} ${colors.border}`}>
      <span className={`text-xs sm:text-sm font-medium ${colors.text}`}>
        {label}
      </span>
      <span className={`text-xs ${colors.text} opacity-80`}>
        {totalComplexity}
      </span>
    </div>
  );
}
