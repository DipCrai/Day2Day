import { useEffect, useRef } from 'react';
import { Task, TaskBlock } from './TaskBlock';

interface DayScheduleProps {
  tasks: Task[];
}

const HOUR_HEIGHT = 80;
const MIN_TASK_HEIGHT = 45;

interface TaskWithPosition extends Task {
  top: number;
  height: number;
  column: number;
  totalColumns: number;
}

interface FreeSlot {
  id: string;
  startTime: string;
  endTime: string;
  top: number;
  height: number;
}

export function DaySchedule({ tasks }: DayScheduleProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (scrollRef.current) {
      const now = new Date();
      const currentHour = now.getHours();
      const scrollPosition = (currentHour - 2) * HOUR_HEIGHT;
      scrollRef.current.scrollTop = Math.max(0, scrollPosition);
    }
  }, []);

  const hours = Array.from({ length: 24 }, (_, i) => i);

  const getTaskTimeInMinutes = (time: string) => {
    const [hour, minute] = time.split(':').map(Number);
    return hour * 60 + minute;
  };

  const tasksOverlap = (task1: Task, task2: Task) => {
    const start1 = getTaskTimeInMinutes(task1.startTime);
    const end1 = getTaskTimeInMinutes(task1.endTime);
    const start2 = getTaskTimeInMinutes(task2.startTime);
    const end2 = getTaskTimeInMinutes(task2.endTime);

    return start1 < end2 && start2 < end1;
  };

  const calculateTaskPositions = (): TaskWithPosition[] => {
    const sortedTasks = [...tasks].sort((a, b) => {
      const timeA = getTaskTimeInMinutes(a.startTime);
      const timeB = getTaskTimeInMinutes(b.startTime);
      return timeA - timeB;
    });

    const positioned: TaskWithPosition[] = [];

    sortedTasks.forEach((task) => {
      const startInMinutes = getTaskTimeInMinutes(task.startTime);
      const endInMinutes = getTaskTimeInMinutes(task.endTime);
      const durationInMinutes = endInMinutes - startInMinutes;

      const top = (startInMinutes / 60) * HOUR_HEIGHT;
      const calculatedHeight = (durationInMinutes / 60) * HOUR_HEIGHT;
      const height = Math.max(calculatedHeight, MIN_TASK_HEIGHT);

      // Находим пересекающиеся задачи
      const overlapping = positioned.filter((p) => tasksOverlap(task, p));

      let column = 0;
      if (overlapping.length > 0) {
        const usedColumns = overlapping.map((t) => t.column);
        while (usedColumns.includes(column)) {
          column++;
        }

        // Обновляем totalColumns для пересекающихся задач
        const maxColumn = Math.max(...overlapping.map((t) => t.column), column);
        overlapping.forEach((t) => {
          t.totalColumns = maxColumn + 1;
        });
      }

      positioned.push({
        ...task,
        top,
        height,
        column,
        totalColumns: overlapping.length > 0 ? Math.max(...overlapping.map((t) => t.totalColumns)) : 1,
      });
    });

    return positioned;
  };

  const calculateFreeSlots = (): FreeSlot[] => {
    const sortedTasks = [...tasks].sort((a, b) => {
      const timeA = getTaskTimeInMinutes(a.startTime);
      const timeB = getTaskTimeInMinutes(b.startTime);
      return timeA - timeB;
    });

    const freeSlots: FreeSlot[] = [];
    const workDayStart = 8 * 60; // 08:00
    const workDayEnd = 20 * 60; // 20:00

    if (sortedTasks.length === 0) {
      // Весь день свободен
      const top = (workDayStart / 60) * HOUR_HEIGHT;
      const height = ((workDayEnd - workDayStart) / 60) * HOUR_HEIGHT;
      freeSlots.push({
        id: 'free-all',
        startTime: '08:00',
        endTime: '20:00',
        top,
        height,
      });
      return freeSlots;
    }

    // Проверяем промежуток до первой задачи
    const firstTaskStart = getTaskTimeInMinutes(sortedTasks[0].startTime);
    if (firstTaskStart > workDayStart) {
      const durationInMinutes = firstTaskStart - workDayStart;
      if (durationInMinutes >= 30) { // Минимум 30 минут для отображения
        const top = (workDayStart / 60) * HOUR_HEIGHT;
        const height = (durationInMinutes / 60) * HOUR_HEIGHT;
        const startHour = Math.floor(workDayStart / 60);
        const startMin = workDayStart % 60;
        const endHour = Math.floor(firstTaskStart / 60);
        const endMin = firstTaskStart % 60;
        freeSlots.push({
          id: 'free-0',
          startTime: `${startHour.toString().padStart(2, '0')}:${startMin.toString().padStart(2, '0')}`,
          endTime: `${endHour.toString().padStart(2, '0')}:${endMin.toString().padStart(2, '0')}`,
          top,
          height,
        });
      }
    }

    // Проверяем промежутки между задачами
    for (let i = 0; i < sortedTasks.length - 1; i++) {
      const currentTaskEnd = getTaskTimeInMinutes(sortedTasks[i].endTime);
      const nextTaskStart = getTaskTimeInMinutes(sortedTasks[i + 1].startTime);

      if (nextTaskStart > currentTaskEnd) {
        const durationInMinutes = nextTaskStart - currentTaskEnd;
        if (durationInMinutes >= 30) { // Минимум 30 минут для отображения
          const top = (currentTaskEnd / 60) * HOUR_HEIGHT;
          const height = (durationInMinutes / 60) * HOUR_HEIGHT;
          const startHour = Math.floor(currentTaskEnd / 60);
          const startMin = currentTaskEnd % 60;
          const endHour = Math.floor(nextTaskStart / 60);
          const endMin = nextTaskStart % 60;
          freeSlots.push({
            id: `free-${i + 1}`,
            startTime: `${startHour.toString().padStart(2, '0')}:${startMin.toString().padStart(2, '0')}`,
            endTime: `${endHour.toString().padStart(2, '0')}:${endMin.toString().padStart(2, '0')}`,
            top,
            height,
          });
        }
      }
    }

    // Проверяем промежуток после последней задачи
    const lastTaskEnd = getTaskTimeInMinutes(sortedTasks[sortedTasks.length - 1].endTime);
    if (lastTaskEnd < workDayEnd) {
      const durationInMinutes = workDayEnd - lastTaskEnd;
      if (durationInMinutes >= 30) { // Минимум 30 минут для отображения
        const top = (lastTaskEnd / 60) * HOUR_HEIGHT;
        const height = (durationInMinutes / 60) * HOUR_HEIGHT;
        const startHour = Math.floor(lastTaskEnd / 60);
        const startMin = lastTaskEnd % 60;
        const endHour = Math.floor(workDayEnd / 60);
        const endMin = workDayEnd % 60;
        freeSlots.push({
          id: `free-${sortedTasks.length}`,
          startTime: `${startHour.toString().padStart(2, '0')}:${startMin.toString().padStart(2, '0')}`,
          endTime: `${endHour.toString().padStart(2, '0')}:${endMin.toString().padStart(2, '0')}`,
          top,
          height,
        });
      }
    }

    return freeSlots;
  };

  const positionedTasks = calculateTaskPositions();
  const freeSlots = calculateFreeSlots();

  return (
    <div
      ref={scrollRef}
      className="flex-1 overflow-y-auto bg-background"
    >
      <div className="flex relative min-h-full">
        {/* Временная шкала */}
        <div className="w-14 sm:w-16 md:w-20 flex-shrink-0 bg-muted/30 relative pt-[10px]">
          {hours.map((hour, index) => {
            const formattedHour = hour.toString().padStart(2, '0') + ':00';
            return (
              <div
                key={hour}
                className="absolute px-1 sm:px-2 md:px-3 -translate-y-1/2 z-10"
                style={{ top: `${index * HOUR_HEIGHT}px` }}
              >
                <span className="text-xs sm:text-sm opacity-60 bg-muted/30 px-0.5 sm:px-1">{formattedHour}</span>
              </div>
            );
          })}
          {/* Горизонтальные линии на временной шкале */}
          {hours.map((hour, index) => (
            <div
              key={hour}
              className="absolute left-0 w-2 sm:w-3 border-b border-border"
              style={{ top: `${index * HOUR_HEIGHT}px` }}
            />
          ))}
        </div>

        {/* Область с задачами */}
        <div className="flex-1 relative border-l-2 border-border" style={{ minHeight: `${24 * HOUR_HEIGHT}px` }}>
          {/* Горизонтальные линии */}
          {hours.map((hour, index) => (
            <div
              key={hour}
              className="absolute left-0 right-0 border-b border-border"
              style={{ top: `${index * HOUR_HEIGHT}px` }}
            />
          ))}
          {/* Половинные линии для 30 минут */}
          {hours.map((hour, index) => (
            <div
              key={`half-${hour}`}
              className="absolute left-0 right-0 border-b border-dashed border-border/30"
              style={{ top: `${index * HOUR_HEIGHT + HOUR_HEIGHT / 2}px` }}
            />
          ))}

          {/* Индикатор текущего времени */}
          {(() => {
            const now = new Date();
            const currentHour = now.getHours();
            const currentMinute = now.getMinutes();
            const currentTimeInMinutes = currentHour * 60 + currentMinute;
            const currentPosition = (currentTimeInMinutes / 60) * HOUR_HEIGHT;

            return (
              <div
                className="absolute left-0 right-0 h-[2px] bg-destructive z-20 shadow-sm"
                style={{ top: `${currentPosition}px` }}
              >
                <div className="absolute left-[-4px] top-1/2 -translate-y-1/2 w-3 h-3 rounded-full bg-destructive border-2 border-background"></div>
              </div>
            );
          })()}

          {/* Свободное время */}
          {freeSlots.map((slot) => (
            <div
              key={slot.id}
              className="absolute left-0 right-0 px-1"
              style={{
                top: `${slot.top}px`,
                height: `${slot.height}px`,
              }}
            >
              <div className="h-full pb-1">
                <div className="h-full rounded-lg bg-muted/40 border border-dashed border-border flex items-center justify-center overflow-hidden">
                  <span className="text-xs sm:text-sm opacity-50 px-2 text-center">Свободное время</span>
                </div>
              </div>
            </div>
          ))}

          {/* Задачи */}
          {positionedTasks.map((task) => {
            const width = `${100 / task.totalColumns}%`;
            const left = `${(task.column * 100) / task.totalColumns}%`;

            return (
              <div
                key={task.id}
                className="absolute px-1 z-10"
                style={{
                  top: `${task.top}px`,
                  height: `${task.height}px`,
                  left,
                  width,
                }}
              >
                <div className="h-full pb-1 bg-background">
                  <TaskBlock task={task} height={task.height} />
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
