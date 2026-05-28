package dev.dipcrai.day2day.util;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dev.dipcrai.day2day.Task;

public class TaskDateUtils {

    public static int lightenColor(int colorHex) {
        int alpha = 10;
        int red = Color.red(colorHex);
        int green = Color.green(colorHex);
        int blue = Color.blue(colorHex);
        return Color.argb(alpha, red, green, blue);
    }

    public static int timeToMinutes(String time) {
        if (time == null) return 0;
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public static String dateToString(Calendar cal, SimpleDateFormat fmt) {
        return fmt.format(cal.getTime());
    }

    public static Calendar parseDate(String dateStr, SimpleDateFormat fmt) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fmt.parse(dateStr));
            return cal;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static Calendar getMonday(Calendar date) {
        Calendar cal = (Calendar) date.clone();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diff = dayOfWeek == Calendar.SUNDAY ? -6 : Calendar.MONDAY - dayOfWeek;
        cal.add(Calendar.DAY_OF_MONTH, diff);
        return cal;
    }

    public static boolean isTaskOnDate(Task task, Calendar date, SimpleDateFormat fmt) {
        String taskDate = task.getDate();
        if (taskDate == null) return false;

        String type = task.getRecurrenceType();
        if (type == null) type = "once";

        String dateStr = dateToString(date, fmt);

        if ("once".equals(type)) {
            return taskDate.equals(dateStr);
        }

        String excluded = task.getExcludedDates();
        if (excluded != null && !excluded.isEmpty()) {
            for (String d : excluded.split(",")) {
                if (d.trim().equals(dateStr)) return false;
            }
        }

        Calendar startDate = parseDate(taskDate, fmt);
        if (startDate == null) return false;

        if (date.before(startDate) && !dateStr.equals(taskDate)) return false;

        String endDateStr = task.getRecurrenceEndDate();
        if (endDateStr != null) {
            Calendar endDate = parseDate(endDateStr, fmt);
            if (endDate != null && date.after(endDate)) return false;
        }

        switch (type) {
            case "daily":
                return true;
            case "weekly":
                return date.get(Calendar.DAY_OF_WEEK) == startDate.get(Calendar.DAY_OF_WEEK);
            case "weekdays":
                int dow = date.get(Calendar.DAY_OF_WEEK);
                return dow >= Calendar.MONDAY && dow <= Calendar.FRIDAY;
            case "custom_days":
                String daysStr = task.getRecurrenceDays();
                if (daysStr == null || daysStr.isEmpty()) return false;
                int targetDow = date.get(Calendar.DAY_OF_WEEK);
                for (String d : daysStr.split(",")) {
                    if (Integer.parseInt(d.trim()) == targetDow) return true;
                }
                return false;
        }
        return false;
    }

    public static List<Task> getTasksForDate(List<Task> allTasks, Calendar date, SimpleDateFormat fmt) {
        List<Task> result = new ArrayList<>();
        for (Task t : allTasks) {
            if (isTaskOnDate(t, date, fmt)) {
                result.add(t);
            }
        }
        return result;
    }

    public static List<Task> getTasksForDay(List<Task> allTasks, Calendar cal, SimpleDateFormat fmt) {
        List<Task> result = new ArrayList<>();
        for (Task task : allTasks) {
            if (isTaskOnDate(task, cal, fmt)) {
                result.add(task);
            }
        }
        result.sort((a, b) -> Integer.compare(timeToMinutes(a.getStartTime()), timeToMinutes(b.getStartTime())));
        return result;
    }

    public static int calculateDayComplexity(List<Task> allTasks, Calendar day, SimpleDateFormat fmt) {
        int sum = 0;
        for (Task task : getTasksForDay(allTasks, day, fmt)) {
            sum += task.getComplexity();
        }
        return sum;
    }

    public static int getAverageDayComplexity(List<Task> allTasks, Calendar day, SimpleDateFormat fmt) {
        List<Task> tasks = getTasksForDay(allTasks, day, fmt);
        if (tasks.isEmpty()) return 0;
        int sum = 0;
        for (Task t : tasks) sum += t.getComplexity();
        return sum / tasks.size();
    }
}
