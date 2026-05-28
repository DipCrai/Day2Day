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
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        findFreeTimeSlot();
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

    private void findFreeTimeSlot() {
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

        if (dayTasks.isEmpty()) {
            startHour = 9; startMinute = 0;
            endHour = 10; endMinute = 0;
            durationMinutes = defaultDuration;
            return;
        }

        int cursor = 0;
        for (Task t : dayTasks) {
            int tStart = timeToMinutes(t.getStartTime());
            if (tStart - cursor >= defaultDuration) {
                startHour = cursor / 60; startMinute = cursor % 60;
                endHour = (cursor + defaultDuration) / 60; endMinute = (cursor + defaultDuration) % 60;
                durationMinutes = defaultDuration;
                return;
            }
            cursor = Math.max(cursor, timeToMinutes(t.getEndTime()));
        }

        if (dayEnd - cursor >= defaultDuration) {
            startHour = cursor / 60; startMinute = cursor % 60;
            endHour = (cursor + defaultDuration) / 60; endMinute = (cursor + defaultDuration) % 60;
            durationMinutes = defaultDuration;
        } else {
            startHour = Math.max(cursor - defaultDuration, 0) / 60;
            startMinute = 0;
            endHour = startHour + 1;
            endMinute = 0;
            durationMinutes = defaultDuration;
        }
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
