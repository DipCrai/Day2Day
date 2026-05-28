package dev.dipcrai.day2day;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AddTaskDialogFragment extends DialogFragment {

    public interface OnTaskCreatedListener {
        boolean onTaskCreated(Task task);
    }

    private static final int[] COLORS = {
            0xFF3B82F6, 0xFF8B5CF6, 0xFF22C55E,
            0xFFF59E0B, 0xFFEC4899, 0xFF06B6D4,
            0xFFEF4444, 0xFFF97316, 0xFF84CC16
    };

    private static final String[] RECURRENCE_LABELS = {"Один раз", "Каждый день", "Каждую неделю", "По будням", "По дням"};
    private static final String[] RECURRENCE_VALUES = {"once", "daily", "weekly", "weekdays", "custom_days"};
    private static final String[] DAY_LABELS = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private static final int[] DAY_CHIP_IDS = {
            R.id.chipMon, R.id.chipTue, R.id.chipWed, R.id.chipThu,
            R.id.chipFri, R.id.chipSat, R.id.chipSun
    };

    private OnTaskCreatedListener listener;
    private String selectedDate;
    private List<Task> existingTasks;
    private int startHour = 9;
    private int startMinute = 0;
    private int endHour = 10;
    private int endMinute = 0;
    private int durationMinutes = 60;
    private String recurrenceType = "once";
    private boolean[] selectedDays = new boolean[7];

    public void setOnTaskCreatedListener(OnTaskCreatedListener listener) {
        this.listener = listener;
    }

    public void setSelectedDate(String date) {
        this.selectedDate = date;
    }

    public void setExistingTasks(List<Task> tasks) {
        this.existingTasks = tasks;
    }

    private void syncEndFromStartDuration(Button btnEndTime) {
        int total = startHour * 60 + startMinute + durationMinutes;
        int clamped = Math.min(total, 1439);
        endHour = clamped / 60;
        endMinute = clamped % 60;
        btnEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));
    }

    private void syncDurationFromEndStart(Button btnDuration) {
        int diff = (endHour * 60 + endMinute) - (startHour * 60 + startMinute);
        durationMinutes = Math.max(diff, 30);
        btnDuration.setText(formatDuration(durationMinutes));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        TextInputEditText etTitle = view.findViewById(R.id.etTaskTitle);
        TextInputEditText etDescription = view.findViewById(R.id.etTaskDescription);
        Button btnStartTime = view.findViewById(R.id.btnStartTime);
        Button btnEndTime = view.findViewById(R.id.btnEndTime);
        Button btnDuration = view.findViewById(R.id.btnDuration);
        Button btnRecurrence = view.findViewById(R.id.btnRecurrence);
        LinearLayout layoutRecurrenceDays = view.findViewById(R.id.layoutRecurrenceDays);
        Slider sliderComplexity = view.findViewById(R.id.sliderComplexity);
        TextView tvComplexityValue = view.findViewById(R.id.tvComplexityValue);
        TextView tvTimeError = view.findViewById(R.id.tvTimeError);
        TextView tvFreeSlot = view.findViewById(R.id.tvFreeSlot);
        Button btnFindTime = view.findViewById(R.id.btnFindTime);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        findFreeTimeSlot(tvFreeSlot);
        btnStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute));
        btnEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));
        btnDuration.setText(formatDuration(durationMinutes));

        btnStartTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                    (view1, hourOfDay, minute) -> {
                        startHour = hourOfDay;
                        startMinute = minute;
                        btnStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        clearTimeError(btnStartTime, tvTimeError);
                        syncEndFromStartDuration(btnEndTime);
                    }, startHour, startMinute, true);
            timePicker.show();
        });

        btnEndTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                    (view1, hourOfDay, minute) -> {
                        endHour = hourOfDay;
                        endMinute = minute;
                        btnEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        clearTimeError(btnStartTime, tvTimeError);
                        syncDurationFromEndStart(btnDuration);
                    }, endHour, endMinute, true);
            timePicker.show();
        });

        btnDuration.setOnClickListener(v -> {
            String[] items = {"30 мин", "1 час", "1.5 часа", "2 часа", "3 часа", "4 часа"};
            int[] values = {30, 60, 90, 120, 180, 240};
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Длительность")
                    .setItems(items, (dialog, which) -> {
                        durationMinutes = values[which];
                        btnDuration.setText(formatDuration(durationMinutes));
                        clearTimeError(btnStartTime, tvTimeError);
                        syncEndFromStartDuration(btnEndTime);
                    })
                    .show();
        });

        btnRecurrence.setText(RECURRENCE_LABELS[0]);
        btnRecurrence.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Повторение")
                    .setItems(RECURRENCE_LABELS, (dialog, which) -> {
                        recurrenceType = RECURRENCE_VALUES[which];
                        btnRecurrence.setText(RECURRENCE_LABELS[which]);
                        layoutRecurrenceDays.setVisibility("custom_days".equals(recurrenceType)
                                ? android.view.View.VISIBLE : android.view.View.GONE);
                    })
                    .show();
        });

        for (int i = 0; i < 7; i++) {
            final int index = i;
            TextView chip = view.findViewById(DAY_CHIP_IDS[i]);
            chip.setText(DAY_LABELS[i]);
            chip.setOnClickListener(cv -> {
                selectedDays[index] = !selectedDays[index];
                updateChipStyle(chip, selectedDays[index]);
            });
        }

        sliderComplexity.addOnChangeListener((slider, value, fromUser) ->
                tvComplexityValue.setText(String.valueOf((int) value)));

        btnFindTime.setOnClickListener(v -> {
            int taskComplexity = (int) sliderComplexity.getValue();
            findWeekFreeTime(taskComplexity, durationMinutes, tvFreeSlot, btnStartTime, btnEndTime, btnDuration);
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etTitle.setError("Введите название");
                return;
            }
            if ("custom_days".equals(recurrenceType)) {
                boolean anySelected = false;
                for (boolean d : selectedDays) { if (d) { anySelected = true; break; } }
                if (!anySelected) {
                    Toast.makeText(requireContext(), "Выберите хотя бы один день", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            String description = etDescription.getText().toString().trim();
            int complexity = (int) sliderComplexity.getValue();

            int startMinutes = startHour * 60 + startMinute;
            int endMinutes = endHour * 60 + endMinute;

            if (endMinutes <= startMinutes) {
                showTimeError(btnStartTime, tvTimeError, "Время конца должно быть позже начала");
                return;
            }
            if (endMinutes > 1439) {
                showTimeError(btnStartTime, tvTimeError, "Задача не может заканчиваться после 23:59");
                return;
            }

            String startTime = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute);
            String endTime = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute);

            if ("once".equals(recurrenceType) && hasTimeOverlap(startMinutes, endMinutes, selectedDate)) {
                showTimeError(btnStartTime, tvTimeError, "На это время уже есть задача");
                return;
            }

            int color = COLORS[new Random().nextInt(COLORS.length)];
            String id = String.valueOf(System.currentTimeMillis());
            String date = selectedDate != null ? selectedDate : new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());

            Task task = new Task(id, title, description, date, startTime, endTime, color, complexity);
            task.setRecurrenceType(recurrenceType);
            if ("custom_days".equals(recurrenceType)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 7; i++) {
                    if (selectedDays[i]) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(i + 2);
                    }
                }
                task.setRecurrenceDays(sb.toString());
            }

            if (listener == null || listener.onTaskCreated(task)) {
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }

    private void findFreeTimeSlot(TextView tvFreeSlot) {
        if (existingTasks == null || selectedDate == null) return;
        List<Task> dayTasks = new java.util.ArrayList<>();
        for (Task t : existingTasks) {
            if (selectedDate.equals(t.getDate()) && "once".equals(t.getRecurrenceType())) {
                dayTasks.add(t);
            }
        }
        dayTasks.sort((a, b) -> Integer.compare(timeToMinutes(a.getStartTime()), timeToMinutes(b.getStartTime())));

        int defaultDuration = 60;
        int dayEnd = 1440;
        boolean found = false;

        if (dayTasks.isEmpty()) {
            startHour = 9; startMinute = 0;
            endHour = 10; endMinute = 0;
            durationMinutes = defaultDuration;
            found = true;
        }

        if (!found) {
            int cursor = 0;
            for (Task t : dayTasks) {
                int tStart = timeToMinutes(t.getStartTime());
                if (tStart - cursor >= defaultDuration) {
                    startHour = cursor / 60; startMinute = cursor % 60;
                    int total = cursor + defaultDuration;
                    endHour = Math.min(total / 60, 23);
                    endMinute = total > 1439 ? 59 : total % 60;
                    durationMinutes = defaultDuration;
                    found = true;
                    break;
                }
                cursor = Math.max(cursor, timeToMinutes(t.getEndTime()));
            }
            if (!found && dayEnd - cursor >= defaultDuration) {
                startHour = cursor / 60; startMinute = cursor % 60;
                int total = cursor + defaultDuration;
                endHour = Math.min(total / 60, 23);
                endMinute = total > 1439 ? 59 : total % 60;
                durationMinutes = defaultDuration;
                found = true;
            }
            if (!found) {
                int total = Math.max(cursor - defaultDuration, 0);
                startHour = Math.min(total / 60, 22);
                startMinute = 0;
                endHour = startHour + 1;
                endMinute = 0;
                durationMinutes = defaultDuration;
            }
        }

        tvFreeSlot.setText("Свободно: " +
                String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute) + " - " +
                String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));
        tvFreeSlot.setVisibility(android.view.View.VISIBLE);
    }

    private void findWeekFreeTime(int taskComplexity, int taskDuration, TextView tvFreeSlot,
                                  Button btnStartTime, Button btnEndTime, Button btnDuration) {
        if (existingTasks == null || selectedDate == null) {
            Toast.makeText(requireContext(), "Данные не загружены", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] dateParts = selectedDate.split("-");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[2]));

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("EEEE", Locale.forLanguageTag("ru"));

        List<Object[]> suggestions = new java.util.ArrayList<>();
        int dayEnd = 1440;

        for (int offset = 0; offset < 7; offset++) {
            java.util.Calendar dayCal = (java.util.Calendar) cal.clone();
            dayCal.add(java.util.Calendar.DAY_OF_MONTH, offset);
            String dayStr = sdf.format(dayCal.getTime());

            List<Task> dayTasks = getDayTasks(dayCal, dayStr);
            dayTasks.sort((a, b) -> Integer.compare(timeToMinutes(a.getStartTime()), timeToMinutes(b.getStartTime())));

            double dayMedian = medianComplexity(dayTasks);
            int cursor = 0;
            for (Task t : dayTasks) {
                cursor = Math.max(cursor, timeToMinutes(t.getEndTime()));
            }

            cursor = 0;
            for (Task t : dayTasks) {
                int tStart = timeToMinutes(t.getStartTime());
                if (tStart - cursor >= taskDuration) {
                    int total = cursor + taskDuration;
                    if (total <= dayEnd) {
                        suggestions.add(new Object[]{dayStr, dayCal.getTime(), cursor / 60, cursor % 60,
                                Math.min(total / 60, 23), total > 1439 ? 59 : total % 60, dayMedian, taskDuration});
                    }
                    cursor = tStart;
                }
                cursor = Math.max(cursor, timeToMinutes(t.getEndTime()));
            }
            if (dayEnd - cursor >= taskDuration) {
                int total = cursor + taskDuration;
                suggestions.add(new Object[]{dayStr, dayCal.getTime(), cursor / 60, cursor % 60,
                        Math.min(total / 60, 23), total > 1439 ? 59 : total % 60, dayMedian, taskDuration});
            }
        }

        if (suggestions.isEmpty()) {
            Toast.makeText(requireContext(), "Нет свободных окон на неделе", Toast.LENGTH_SHORT).show();
            return;
        }

        suggestions.sort((a, b) -> {
            double diffA = Math.abs((Double) a[6] - taskComplexity);
            double diffB = Math.abs((Double) b[6] - taskComplexity);
            int cmp = Double.compare(diffA, diffB);
            if (cmp != 0) return cmp;
            int tasksA = getDayTasksForDate((String) a[0]).size();
            int tasksB = getDayTasksForDate((String) b[0]).size();
            return Integer.compare(tasksA, tasksB);
        });

        String[] items = new String[Math.min(suggestions.size(), 10)];
        for (int i = 0; i < items.length; i++) {
            Object[] s = suggestions.get(i);
            String dayName = dayFormat.format((java.util.Date) s[1]);
            String start = String.format(Locale.getDefault(), "%02d:%02d", s[2], s[3]);
            String end = String.format(Locale.getDefault(), "%02d:%02d", s[4], s[5]);
            int dc = (int) Math.round((Double) s[6]);
            items[i] = dayName + " " + start + "-" + end + " (сложн. " + dc + ")";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Свободное время")
                .setItems(items, (dialog, which) -> {
                    Object[] sel = suggestions.get(which);
                    selectedDate = (String) sel[0];
                    startHour = (int) sel[2];
                    startMinute = (int) sel[3];
                    endHour = (int) sel[4];
                    endMinute = (int) sel[5];
                    durationMinutes = (int) sel[7];
                    btnStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute));
                    btnEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));
                    btnDuration.setText(formatDuration(durationMinutes));
                    tvFreeSlot.setText("Свободно: " + (String) sel[0] + " " +
                            String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute) + "-" +
                            String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));
                    tvFreeSlot.setVisibility(android.view.View.VISIBLE);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private List<Task> getDayTasks(java.util.Calendar dayCal, String dayStr) {
        List<Task> result = new java.util.ArrayList<>();
        for (Task t : existingTasks) {
            if (isTaskOnDate(t, dayCal, dayStr)) {
                result.add(t);
            }
        }
        return result;
    }

    private List<Task> getDayTasksForDate(String dateStr) {
        String[] parts = dateStr.split("-");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        return getDayTasks(cal, dateStr);
    }

    private boolean isTaskOnDate(Task task, java.util.Calendar dateCal, String dateStr) {
        String taskDate = task.getDate();
        if (taskDate == null) return false;
        String type = task.getRecurrenceType();
        if (type == null) type = "once";

        if ("once".equals(type)) {
            return taskDate.equals(dateStr);
        }
        String excluded = task.getExcludedDates();
        if (excluded != null && !excluded.isEmpty()) {
            for (String d : excluded.split(",")) {
                if (d.trim().equals(dateStr)) return false;
            }
        }
        String[] tp = taskDate.split("-");
        java.util.Calendar startCal = java.util.Calendar.getInstance();
        startCal.set(Integer.parseInt(tp[0]), Integer.parseInt(tp[1]) - 1, Integer.parseInt(tp[2]));
        if (dateCal.before(startCal) && !dateStr.equals(taskDate)) return false;

        String endDateStr = task.getRecurrenceEndDate();
        if (endDateStr != null) {
            String[] ep = endDateStr.split("-");
            java.util.Calendar endCal = java.util.Calendar.getInstance();
            endCal.set(Integer.parseInt(ep[0]), Integer.parseInt(ep[1]) - 1, Integer.parseInt(ep[2]));
            if (dateCal.after(endCal)) return false;
        }

        switch (type) {
            case "daily": return true;
            case "weekly": return dateCal.get(java.util.Calendar.DAY_OF_WEEK) == startCal.get(java.util.Calendar.DAY_OF_WEEK);
            case "weekdays": return dateCal.get(java.util.Calendar.DAY_OF_WEEK) >= java.util.Calendar.MONDAY
                    && dateCal.get(java.util.Calendar.DAY_OF_WEEK) <= java.util.Calendar.FRIDAY;
            case "custom_days":
                String daysStr = task.getRecurrenceDays();
                if (daysStr == null || daysStr.isEmpty()) return false;
                int targetDow = dateCal.get(java.util.Calendar.DAY_OF_WEEK);
                for (String d : daysStr.split(",")) {
                    if (Integer.parseInt(d.trim()) == targetDow) return true;
                }
                return false;
        }
        return false;
    }

    private double medianComplexity(List<Task> tasks) {
        if (tasks.isEmpty()) return 5.0;
        List<Integer> comps = new java.util.ArrayList<>();
        for (Task t : tasks) comps.add(t.getComplexity());
        java.util.Collections.sort(comps);
        int mid = comps.size() / 2;
        if (comps.size() % 2 == 1) return comps.get(mid);
        return (comps.get(mid - 1) + comps.get(mid)) / 2.0;
    }

    private void updateChipStyle(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.chip_selected : R.drawable.chip_unselected);
        chip.setTextColor(selected ? 0xFFFFFFFF : 0xFF717182);
    }

    private boolean hasTimeOverlap(int startMinutes, int endMinutes, String date) {
        if (existingTasks == null || date == null) return false;
        for (Task existing : existingTasks) {
            if (!date.equals(existing.getDate())) continue;
            int exStart = timeToMinutes(existing.getStartTime());
            int exEnd = timeToMinutes(existing.getEndTime());
            if (startMinutes < exEnd && exStart < endMinutes) return true;
        }
        return false;
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private void showTimeError(Button btnStartTime, TextView tvTimeError, String message) {
        tvTimeError.setText(message);
        tvTimeError.setVisibility(android.view.View.VISIBLE);
        btnStartTime.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFEF4444));
    }

    private void clearTimeError(Button btnStartTime, TextView tvTimeError) {
        tvTimeError.setVisibility(android.view.View.GONE);
        btnStartTime.setBackgroundTintList(null);
    }

    private String formatDuration(int minutes) {
        if (minutes < 60) return minutes + " мин";
        int hours = minutes / 60;
        int remain = minutes % 60;
        if (remain == 0) return hours + " ч";
        return hours + " ч " + remain + " мин";
    }
}
