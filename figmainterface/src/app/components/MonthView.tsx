import { Task } from './TaskBlock';

interface MonthViewProps {
  selectedDate: Date;
  onDateSelect: (date: Date) => void;
  tasks: Task[];
}

interface DayCell {
  date: Date;
  day: number;
  isCurrentMonth: boolean;
}

export function MonthView({ selectedDate, onDateSelect, tasks }: MonthViewProps) {
  const year = selectedDate.getFullYear();
  const month = selectedDate.getMonth();

  const monthNames = [
    'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
    'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'
  ];

  const dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

  // Получаем первый день месяца
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);

  // Получаем день недели первого дня (0 = воскресенье, 1 = понедельник, ...)
  let firstDayOfWeek = firstDay.getDay();
  // Конвертируем в формат, где понедельник = 0
  firstDayOfWeek = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1;

  // Создаем массив дней для отображения
  const daysInMonth = lastDay.getDate();
  const days: DayCell[] = [];

  // Добавляем дни предыдущего месяца
  const prevMonth = new Date(year, month, 0);
  const daysInPrevMonth = prevMonth.getDate();
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const dayNum = daysInPrevMonth - i;
    days.push({
      date: new Date(year, month - 1, dayNum),
      day: dayNum,
      isCurrentMonth: false,
    });
  }

  // Добавляем дни текущего месяца
  for (let i = 1; i <= daysInMonth; i++) {
    days.push({
      date: new Date(year, month, i),
      day: i,
      isCurrentMonth: true,
    });
  }

  // Добавляем дни следующего месяца для заполнения сетки
  const remainingDays = 42 - days.length; // 6 недель по 7 дней
  for (let i = 1; i <= remainingDays; i++) {
    days.push({
      date: new Date(year, month + 1, i),
      day: i,
      isCurrentMonth: false,
    });
  }

  const isToday = (date: Date) => {
    const today = new Date();
    return (
      date.getDate() === today.getDate() &&
      date.getMonth() === today.getMonth() &&
      date.getFullYear() === today.getFullYear()
    );
  };

  const getTasksForDay = (date: Date) => {
    // В реальном приложении здесь была бы логика по дате
    // Для демо просто вернем разные задачи для разных дней
    const dayOfMonth = date.getDate();
    const dayTasks = tasks.filter((_, index) => (dayOfMonth + index) % 3 === 0);

    // Сортируем по времени начала
    return dayTasks.sort((a, b) => {
      const timeA = a.startTime.split(':').map(Number);
      const timeB = b.startTime.split(':').map(Number);
      return timeA[0] * 60 + timeA[1] - (timeB[0] * 60 + timeB[1]);
    });
  };

  const calculateDayComplexity = (dayTasks: Task[]) => {
    return dayTasks.reduce((sum, task) => sum + task.complexity, 0);
  };

  const getComplexityColor = (complexity: number) => {
    if (complexity === 0) return '#d1d5db';
    if (complexity <= 20) return '#22c55e';
    if (complexity <= 40) return '#3b82f6';
    if (complexity <= 60) return '#eab308';
    if (complexity <= 80) return '#f97316';
    return '#ef4444';
  };

  const handleDayClick = (dayCell: DayCell) => {
    onDateSelect(dayCell.date);
  };

  return (
    <div className="flex-1 flex flex-col overflow-auto px-2 py-3 md:p-4">
      <div className="mb-4 md:mb-6">
        <h2 className="text-center text-xl md:text-2xl">{monthNames[month]} {year}</h2>
      </div>

      <div className="max-w-6xl mx-auto w-full">
        {/* Заголовки дней недели */}
        <div className="grid grid-cols-7 gap-1 md:gap-2 mb-1 md:mb-2">
          {dayNames.map((day) => (
            <div key={day} className="text-center py-1 md:py-2 text-xs md:text-sm opacity-60">
              {day}
            </div>
          ))}
        </div>

        {/* Дни месяца */}
        <div className="grid grid-cols-7 gap-1 md:gap-2">
          {days.map((dayCell, index) => {
            const dayTasks = getTasksForDay(dayCell.date).slice(0, 4);
            const dayComplexity = calculateDayComplexity(getTasksForDay(dayCell.date));
            const complexityColor = getComplexityColor(dayComplexity);

            return (
              <button
                key={index}
                onClick={() => handleDayClick(dayCell)}
                className={`h-20 sm:h-24 md:h-32 rounded border-2 transition-colors flex flex-col p-1 md:p-2 overflow-hidden relative ${
                  isToday(dayCell.date)
                    ? 'bg-primary text-primary-foreground border-primary'
                    : dayCell.isCurrentMonth
                    ? 'bg-card hover:bg-accent hover:border-accent-foreground/20'
                    : 'bg-muted/30 border-border/50 opacity-50 hover:opacity-70'
                }`}
                style={{
                  borderColor: isToday(dayCell.date) ? undefined : complexityColor,
                }}
              >
                <div className="flex items-start justify-between mb-1">
                  <span className={`text-xs md:text-sm ${dayCell.isCurrentMonth ? '' : 'opacity-60'}`}>
                    {dayCell.day}
                  </span>
                  {dayComplexity > 0 && !isToday(dayCell.date) && (
                    <span
                      className="text-[10px] md:text-xs font-medium px-1 rounded"
                      style={{
                        backgroundColor: `${complexityColor}20`,
                        color: complexityColor
                      }}
                    >
                      {dayComplexity}
                    </span>
                  )}
                </div>

                {/* Мини-блоки задач */}
                <div className="flex flex-col gap-0.5 w-full">
                  {dayTasks.map((task, idx) => (
                    <div
                      key={idx}
                      className="h-1.5 md:h-2 rounded-sm w-full"
                      style={{ backgroundColor: task.color }}
                      title={task.title}
                    />
                  ))}
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
