interface WeekDaysProps {
  selectedDate: Date;
  onDateSelect: (date: Date) => void;
}

export function WeekDays({ selectedDate, onDateSelect }: WeekDaysProps) {
  const getWeekDays = () => {
    const today = new Date();
    const currentDay = today.getDay();
    const monday = new Date(today);
    monday.setDate(today.getDate() - (currentDay === 0 ? 6 : currentDay - 1));

    const days = [];
    for (let i = 0; i < 7; i++) {
      const day = new Date(monday);
      day.setDate(monday.getDate() + i);
      days.push(day);
    }
    return days;
  };

  const weekDays = getWeekDays();
  const dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

  const isToday = (date: Date) => {
    const today = new Date();
    return (
      date.getDate() === today.getDate() &&
      date.getMonth() === today.getMonth() &&
      date.getFullYear() === today.getFullYear()
    );
  };

  const isSameDay = (date1: Date, date2: Date) => {
    return (
      date1.getDate() === date2.getDate() &&
      date1.getMonth() === date2.getMonth() &&
      date1.getFullYear() === date2.getFullYear()
    );
  };

  return (
    <div className="flex gap-1 sm:gap-2 overflow-x-auto pb-1">
      {weekDays.map((day, index) => {
        const today = isToday(day);
        const selected = isSameDay(day, selectedDate);

        return (
          <button
            key={day.toISOString()}
            onClick={() => onDateSelect(day)}
            className={`flex flex-col items-center justify-center min-w-[48px] w-12 sm:w-14 h-14 sm:h-16 rounded-lg transition-colors ${
              today
                ? 'bg-primary text-primary-foreground'
                : selected
                ? 'bg-accent'
                : 'hover:bg-muted'
            }`}
          >
            <span className="text-xs opacity-70">{dayNames[index]}</span>
            <span className="text-base sm:text-lg">{day.getDate()}</span>
          </button>
        );
      })}
    </div>
  );
}
