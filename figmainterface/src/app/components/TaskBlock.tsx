export interface Task {
  id: string;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  color: string;
  complexity: number; // от 1 до 10
}

interface TaskBlockProps {
  task: Task;
  height?: number;
}

export function TaskBlock({ task, height }: TaskBlockProps) {
  // Определяем компактный режим для задач короче 60 минут (менее 60px)
  const isCompact = height && height < 65;
  const isVeryCompact = height && height < 50;

  return (
    <div
      className={`rounded border-l-4 shadow-sm h-full flex flex-col overflow-hidden ${
        isVeryCompact ? 'p-1.5' : isCompact ? 'p-2' : 'p-2 sm:p-3'
      }`}
      style={{
        backgroundColor: `${task.color}15`,
        borderLeftColor: task.color,
      }}
    >
      {isVeryCompact ? (
        // Очень компактный режим: только название и время
        <div className="flex flex-col justify-center h-full overflow-hidden">
          <div className="flex items-center justify-between gap-1">
            <h4 className="text-xs sm:text-sm font-medium truncate leading-tight flex-1">{task.title}</h4>
            <span className="text-[10px] sm:text-xs opacity-50 font-medium whitespace-nowrap">{task.complexity}/10</span>
          </div>
          <span className="text-[10px] sm:text-xs opacity-70 truncate leading-tight">
            {task.startTime}
          </span>
        </div>
      ) : isCompact ? (
        // Компактный режим: время и название
        <div className="flex flex-col justify-center h-full overflow-hidden">
          <div className="flex items-center justify-between gap-1 mb-0.5">
            <span className="text-[10px] sm:text-xs opacity-70 truncate">
              {task.startTime} - {task.endTime}
            </span>
            <span className="text-[10px] sm:text-xs opacity-50 font-medium whitespace-nowrap">{task.complexity}/10</span>
          </div>
          <h4 className="text-xs sm:text-sm truncate leading-tight">{task.title}</h4>
        </div>
      ) : (
        // Полный режим: все поля
        <>
          <div className="flex items-center justify-between gap-1 sm:gap-2 mb-1">
            <span className="text-[10px] sm:text-xs opacity-70">
              {task.startTime} - {task.endTime}
            </span>
            <span className="text-xs sm:text-sm opacity-60 font-medium">Сложность: {task.complexity}/10</span>
          </div>
          <h4 className="text-xs sm:text-sm md:text-base mb-1">{task.title}</h4>
          <p className="text-[10px] sm:text-xs md:text-sm opacity-70 line-clamp-2">{task.description}</p>
        </>
      )}
    </div>
  );
}
