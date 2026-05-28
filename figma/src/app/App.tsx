import { useState, useEffect } from 'react';
import { ViewToggle } from './components/ViewToggle';
import { WeekDays } from './components/WeekDays';
import { DaySchedule } from './components/DaySchedule';
import { WeekSchedule } from './components/WeekSchedule';
import { MonthView } from './components/MonthView';
import { DayComplexityBadge } from './components/DayComplexityBadge';
import { Task } from './components/TaskBlock';

type ViewType = 'day' | 'week' | 'month';

const mockTasks: Task[] = [
  {
    id: '1',
    title: 'Утренняя планерка',
    description: 'Обсуждение задач на день с командой',
    startTime: '09:00',
    endTime: '09:30',
    color: '#3b82f6',
    complexity: 2,
  },
  {
    id: '2',
    title: 'Разработка функционала',
    description: 'Работа над новым интерфейсом планировщика',
    startTime: '10:00',
    endTime: '12:00',
    color: '#8b5cf6',
    complexity: 8,
  },
  {
    id: '3',
    title: 'Обед',
    description: 'Перерыв на обед',
    startTime: '13:00',
    endTime: '14:00',
    color: '#22c55e',
    complexity: 1,
  },
  {
    id: '4',
    title: 'Код ревью',
    description: 'Проверка pull requests от коллег',
    startTime: '14:00',
    endTime: '15:00',
    color: '#f59e0b',
    complexity: 5,
  },
  {
    id: '5',
    title: 'Встреча с заказчиком',
    description: 'Презентация прототипа нового функционала',
    startTime: '15:30',
    endTime: '16:30',
    color: '#ec4899',
    complexity: 7,
  },
  {
    id: '6',
    title: 'Документация',
    description: 'Обновление технической документации проекта',
    startTime: '17:00',
    endTime: '18:00',
    color: '#06b6d4',
    complexity: 4,
  },
];

export default function App() {
  const [view, setView] = useState<ViewType>('day');
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [previousView, setPreviousView] = useState<ViewType | null>(null);

  // Функция расчета общей сложности дня
  const calculateDayComplexity = (tasks: Task[]) => {
    return tasks.reduce((sum, task) => sum + task.complexity, 0);
  };

  const dayComplexity = calculateDayComplexity(mockTasks);

  const handleDateSelect = (date: Date) => {
    setSelectedDate(date);
    if (view === 'month') {
      setPreviousView('month');
      setView('day');
    }
  };

  const handleViewChange = (newView: ViewType) => {
    if (newView === 'month' && previousView === 'month') {
      setPreviousView(null);
    }
    setView(newView);
  };

  // Обработка навигации назад
  useEffect(() => {
    const handlePopState = () => {
      if (previousView === 'month' && view === 'day') {
        setView('month');
        setPreviousView(null);
      }
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, [previousView, view]);

  // Добавляем запись в историю при переходе с месяца на день
  useEffect(() => {
    if (view === 'day' && previousView === 'month') {
      window.history.pushState({ view: 'day' }, '');
    }
  }, [view, previousView]);

  return (
    <div className="size-full flex flex-col bg-background p-2 sm:p-4 md:p-6 gap-3 md:gap-6">
      <div className="flex items-center justify-between flex-wrap gap-2">
        <h1 className="text-xl md:text-2xl">Планировщик</h1>
        <ViewToggle view={view} onViewChange={handleViewChange} />
      </div>

      {view !== 'month' && (
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
          <WeekDays selectedDate={selectedDate} onDateSelect={handleDateSelect} />
          {view === 'day' && <DayComplexityBadge totalComplexity={dayComplexity} />}
        </div>
      )}

      {view === 'day' && <DaySchedule tasks={mockTasks} />}
      {view === 'week' && <WeekSchedule tasks={mockTasks} />}
      {view === 'month' && (
        <MonthView selectedDate={selectedDate} onDateSelect={handleDateSelect} tasks={mockTasks} />
      )}
    </div>
  );
}