import { Task, TaskBlock } from './TaskBlock';
import { DayComplexityBadge } from './DayComplexityBadge';

interface WeekScheduleProps {
  tasks: Task[];
}

const HOUR_HEIGHT = 80;

interface TaskWithPosition extends Task {
  top: number;
  height: number;
  dayIndex: number;
}

export function WeekSchedule({ tasks }: WeekScheduleProps) {
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
  const hours = Array.from({ length: 24 }, (_, i) => i);

  const getTaskTimeInMinutes = (time: string) => {
    const [hour, minute] = time.split(':').map(Number);
    return hour * 60 + minute;
  };

  const calculateTaskPositions = (): TaskWithPosition[] => {
    return tasks.map((task) => {
      const startInMinutes = getTaskTimeInMinutes(task.startTime);
      const endInMinutes = getTaskTimeInMinutes(task.endTime);
      const durationInMinutes = endInMinutes - startInMinutes;

      const top = (startInMinutes / 60) * HOUR_HEIGHT;
      const height = Math.max((durationInMinutes / 60) * HOUR_HEIGHT, 50);

      // Для демо распределим задачи по дням циклически
      const dayIndex = parseInt(task.id) % 7;

      return {
        ...task,
        top,
        height,
        dayIndex,
      };
    });
  };

  const positionedTasks = calculateTaskPositions();

  const getTasksForDay = (dayIndex: number) => {
    return positionedTasks.filter((task) => task.dayIndex === dayIndex);
  };

  const calculateDayComplexity = (dayTasks: Task[]) => {
    return dayTasks.reduce((sum, task) => sum + task.complexity, 0);
  };

  return (
    <div className="flex-1 overflow-auto bg-background">
      {/* Унифицированный вид - список дней */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3 md:gap-4">
        {weekDays.map((day, dayIndex) => {
          const dayTasks = getTasksForDay(dayIndex).sort((a, b) => {
            const timeA = getTaskTimeInMinutes(a.startTime);
            const timeB = getTaskTimeInMinutes(b.startTime);
            return timeA - timeB;
          });

          const isToday =
            day.getDate() === new Date().getDate() &&
            day.getMonth() === new Date().getMonth() &&
            day.getFullYear() === new Date().getFullYear();

          const dayComplexity = calculateDayComplexity(dayTasks);

          return (
            <div key={day.toISOString()} className={`border rounded-lg overflow-hidden ${isToday ? 'border-primary bg-primary/5' : 'border-border'}`}>
              <div className="p-3 border-b border-border bg-muted/30 space-y-2">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-sm opacity-70">{dayNames[dayIndex]}</div>
                    <div className="text-lg font-medium">{day.getDate()} {day.toLocaleDateString('ru', { month: 'long' })}</div>
                  </div>
                  {isToday && (
                    <div className="text-xs bg-primary text-primary-foreground px-2 py-1 rounded">
                      Сегодня
                    </div>
                  )}
                </div>
                {dayTasks.length > 0 && (
                  <div>
                    <DayComplexityBadge totalComplexity={dayComplexity} />
                  </div>
                )}
              </div>
              <div className="p-2 space-y-2 max-h-[400px] overflow-y-auto">
                {dayTasks.length > 0 ? (
                  dayTasks.map((task) => (
                    <div
                      key={task.id}
                      className="rounded border-l-4 p-2 shadow-sm overflow-hidden"
                      style={{
                        backgroundColor: `${task.color}15`,
                        borderLeftColor: task.color,
                      }}
                    >
                      <div className="flex items-center justify-between gap-2 mb-1">
                        <span className="text-xs opacity-70 truncate">
                          {task.startTime} - {task.endTime}
                        </span>
                        <span className="text-xs opacity-60 font-medium whitespace-nowrap">
                          {task.complexity}/10
                        </span>
                      </div>
                      <h4 className="text-sm mb-1 font-medium truncate">{task.title}</h4>
                      <p className="text-xs opacity-70 line-clamp-2">{task.description}</p>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8 text-sm opacity-50">
                    Нет задач
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
