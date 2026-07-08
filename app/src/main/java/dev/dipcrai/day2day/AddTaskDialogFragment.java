package dev.dipcrai.day2day;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import dev.dipcrai.day2day.util.TaskDateUtils;

public class AddTaskDialogFragment extends DialogFragment {

    public interface OnTaskCreatedListener {
        boolean onTaskCreated(Task task);
    }

    private static final String[] RECURRENCE_VALUES = {"once", "daily", "weekly", "weekdays", "custom_days"};
    private static final int[] DAY_CHIP_IDS = {
            R.id.chipMon, R.id.chipTue, R.id.chipWed, R.id.chipThu,
            R.id.chipFri, R.id.chipSat, R.id.chipSun
    };
    private static final int[] DURATION_PILL_IDS = {
            R.id.pillDur30, R.id.pillDur1h, R.id.pillDur1_5h,
            R.id.pillDur2h, R.id.pillDur3h
    };
    private static final int[] DURATION_VALUES = {30, 60, 90, 120, 180};
    private static final int[] RECURRENCE_PILL_IDS = {
            R.id.pillRecOnce, R.id.pillRecDaily, R.id.pillRecWeekly,
            R.id.pillRecWeekdays, R.id.pillRecCustom
    };

    private OnTaskCreatedListener listener;
    private String selectedDate;
    private List<Task> existingTasks;
    private List<Task> allTasks;
    private final java.text.SimpleDateFormat dateFormat =
            new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String[] recurrenceLabels;
    private String[] dayLabels;
    private int[] colors;
    private int startHour = 9;
    private int startMinute = 0;
    private int endHour = 10;
    private int endMinute = 0;
    private int durationMinutes = 60;
    private String recurrenceType = "once";
    private boolean[] selectedDays = new boolean[7];
    private int sleepStart = 0;
    private int sleepEnd = 480;

    public void setSleepStart(int minutes) { this.sleepStart = minutes; }
    public void setSleepEnd(int minutes) { this.sleepEnd = minutes; }

    public void setOnTaskCreatedListener(OnTaskCreatedListener listener) {
        this.listener = listener;
    }

    public void setSelectedDate(String date) {
        this.selectedDate = date;
    }

    public void setExistingTasks(List<Task> tasks) {
        this.existingTasks = tasks;
    }

    public void setAllTasks(List<Task> tasks) {
        this.allTasks = tasks;
    }

    private void syncEndFromStartDuration(TextView btnEndTime) {
        int total = startHour * 60 + startMinute + durationMinutes;
        int clamped = Math.min(total, 1439);
        endHour = clamped / 60;
        endMinute = clamped % 60;
        btnEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        recurrenceLabels = new String[] {
                getString(R.string.recurrence_once), getString(R.string.recurrence_daily),
                getString(R.string.recurrence_weekly), getString(R.string.recurrence_weekdays),
                getString(R.string.recurrence_custom_days)
        };
        dayLabels = new String[] {
                getString(R.string.day_mon), getString(R.string.day_tue),
                getString(R.string.day_wed), getString(R.string.day_thu),
                getString(R.string.day_fri), getString(R.string.day_sat),
                getString(R.string.day_sun)
        };
        colors = new int[] {
                ContextCompat.getColor(requireContext(), R.color.task_blue),
                ContextCompat.getColor(requireContext(), R.color.task_purple),
                ContextCompat.getColor(requireContext(), R.color.task_green),
                ContextCompat.getColor(requireContext(), R.color.task_amber),
                ContextCompat.getColor(requireContext(), R.color.task_pink),
                ContextCompat.getColor(requireContext(), R.color.task_cyan),
                ContextCompat.getColor(requireContext(), R.color.complexity_extreme),
                ContextCompat.getColor(requireContext(), R.color.complexity_hard),
                ContextCompat.getColor(requireContext(), R.color.task_lime)
        };

        EditText etTitle = view.findViewById(R.id.etTaskTitle);
        EditText etDescription = view.findViewById(R.id.etTaskDescription);
        TextView btnStartTime = view.findViewById(R.id.btnStartTime);
        TextView btnEndTime = view.findViewById(R.id.btnEndTime);
        LinearLayout layoutRecurrenceDays = view.findViewById(R.id.layoutRecurrenceDays);
        SeekBar sliderComplexity = view.findViewById(R.id.sliderComplexity);
        TextView tvComplexityValue = view.findViewById(R.id.tvComplexityValue);
        TextView tvTimeError = view.findViewById(R.id.tvTimeError);
        TextView tvFreeSlot = view.findViewById(R.id.tvFreeSlot);
        TextView btnFindTime = view.findViewById(R.id.btnFindTime);
        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnSave = view.findViewById(R.id.btnSave);

        btnStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute));
        btnEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));

        for (int i = 0; i < 5; i++) {
            final int index = i;
            TextView pill = view.findViewById(DURATION_PILL_IDS[i]);
            pill.setOnClickListener(cv -> {
                durationMinutes = DURATION_VALUES[index];
                updateDurationPills(view);
                clearTimeError(btnStartTime, tvTimeError);
                syncEndFromStartDuration(btnEndTime);
            });
        }
        updateDurationPills(view);

        view.findViewById(R.id.pillDurCustom).setOnClickListener(cv -> {
            android.view.View durView = getLayoutInflater().inflate(R.layout.dialog_duration_input, null);
            android.widget.EditText etInput = durView.findViewById(R.id.etDurationInput);
            etInput.setText(String.valueOf(durationMinutes));
            android.app.AlertDialog.Builder durBuilder = new android.app.AlertDialog.Builder(requireContext());
            durBuilder.setView(durView);
            android.app.Dialog durDialog = durBuilder.create();
            if (durDialog.getWindow() != null)
                durDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            durView.findViewById(R.id.btnCancel).setOnClickListener(v -> durDialog.dismiss());
            durView.findViewById(R.id.btnSave).setOnClickListener(v -> {
                try {
                    int val = Integer.parseInt(etInput.getText().toString());
                    if (val > 0) {
                        durationMinutes = val;
                        updateDurationPills(view);
                        clearTimeError(btnStartTime, tvTimeError);
                        syncEndFromStartDuration(btnEndTime);
                        durDialog.dismiss();
                    }
                } catch (NumberFormatException ignored) {}
            });
            durDialog.show();
        });

        for (int i = 0; i < 5; i++) {
            final int index = i;
            TextView pill = view.findViewById(RECURRENCE_PILL_IDS[i]);
            pill.setOnClickListener(cv -> {
                recurrenceType = RECURRENCE_VALUES[index];
                updateRecurrencePills(view);
                layoutRecurrenceDays.setVisibility("custom_days".equals(recurrenceType)
                        ? android.view.View.VISIBLE : android.view.View.GONE);
            });
        }

        for (int i = 0; i < 7; i++) {
            final int index = i;
            TextView chip = view.findViewById(DAY_CHIP_IDS[i]);
            chip.setText(dayLabels[i]);
            chip.setOnClickListener(cv -> {
                selectedDays[index] = !selectedDays[index];
                updateChipStyle(chip, selectedDays[index]);
            });
        }

        sliderComplexity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvComplexityValue.setText(String.valueOf(progress + 1));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnFindTime.setOnClickListener(v -> findFreeSlot(btnStartTime, btnEndTime, tvFreeSlot));

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etTitle.setError(getString(R.string.error_title_required));
                return;
            }
            if ("custom_days".equals(recurrenceType)) {
                boolean anySelected = false;
                for (boolean d : selectedDays) { if (d) { anySelected = true; break; } }
                if (!anySelected) {
                    Toast.makeText(requireContext(), getString(R.string.error_select_day), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            String description = etDescription.getText().toString().trim();
            int complexity = sliderComplexity.getProgress() + 1;

            int[] startParsed = parseTimeInput(btnStartTime.getText().toString());
            if (startParsed == null) {
                showTimeError(btnStartTime, tvTimeError, getString(R.string.error_time_format));
                return;
            }
            startHour = startParsed[0]; startMinute = startParsed[1];

            int[] endParsed = parseTimeInput(btnEndTime.getText().toString());
            if (endParsed == null) {
                showTimeError(btnStartTime, tvTimeError, getString(R.string.error_time_format));
                return;
            }
            endHour = endParsed[0]; endMinute = endParsed[1];

            if (durationMinutes < 1) {
                Toast.makeText(requireContext(), getString(R.string.error_duration_format), Toast.LENGTH_SHORT).show();
                return;
            }

            int startMinutes = startHour * 60 + startMinute;
            int endMinutes = endHour * 60 + endMinute;

            if (endMinutes <= startMinutes) {
                showTimeError(btnStartTime, tvTimeError, getString(R.string.error_end_before_start));
                return;
            }
            if (endMinutes > 1439) {
                showTimeError(btnStartTime, tvTimeError, getString(R.string.error_past_midnight));
                return;
            }

            String startTime = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute);
            String endTime = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute);

            if (hasTimeOverlap(startMinutes, endMinutes)) {
                showTimeError(btnStartTime, tvTimeError, getString(R.string.error_time_overlap));
                return;
            }

            int color = colors[new Random().nextInt(colors.length)];
            String id = String.valueOf(System.currentTimeMillis());
            String date = selectedDate != null ? selectedDate : dateFormat.format(new java.util.Date());

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
        Dialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        return dialog;
    }

    private void findFreeSlot(TextView btnStartTime, TextView btnEndTime,
                              TextView tvFreeSlot) {
        if (existingTasks == null) return;
        if (findSlotOnDay(existingTasks, btnStartTime, btnEndTime, tvFreeSlot)) return;
        findWeekFreeSlot(btnStartTime, btnEndTime, tvFreeSlot);
    }

    private boolean findSlotOnDay(List<Task> dayTasks, TextView btnStartTime,
                                   TextView btnEndTime, TextView tvFreeSlot) {
        if (sleepStart > sleepEnd) {
            return findSlotInWindow(dayTasks, sleepEnd, sleepStart, btnStartTime, btnEndTime, tvFreeSlot);
        } else if (sleepStart < sleepEnd) {
            if (findSlotInWindow(dayTasks, 0, sleepStart, btnStartTime, btnEndTime, tvFreeSlot))
                return true;
            return findSlotInWindow(dayTasks, sleepEnd, 1440, btnStartTime, btnEndTime, tvFreeSlot);
        } else {
            return findSlotInWindow(dayTasks, 0, 1440, btnStartTime, btnEndTime, tvFreeSlot);
        }
    }

    private boolean findSlotInWindow(List<Task> tasks, int winStart, int winEnd,
                                      TextView btnStartTime, TextView btnEndTime,
                                      TextView tvFreeSlot) {
        int cursor = winStart;
        for (Task t : tasks) {
            int tStart = timeToMinutes(t.getStartTime());
            int tEnd = timeToMinutes(t.getEndTime());
            if (tEnd <= winStart || tStart >= winEnd) continue;
            tStart = Math.max(tStart, winStart);
            tEnd = Math.min(tEnd, winEnd);
            if (tStart - cursor >= durationMinutes) {
                applySlot(cursor, winEnd, btnStartTime, btnEndTime, tvFreeSlot);
                return true;
            }
            cursor = Math.max(cursor, tEnd);
        }
        if (winEnd - cursor >= durationMinutes) {
            applySlot(cursor, winEnd, btnStartTime, btnEndTime, tvFreeSlot);
            return true;
        }
        return false;
    }

    private void applySlot(int cursor, int windowEnd, TextView btnStartTime, TextView btnEndTime,
                           TextView tvFreeSlot) {
        startHour = cursor / 60; startMinute = cursor % 60;
        int total = Math.min(cursor + durationMinutes, windowEnd);
        endHour = total / 60; endMinute = total % 60;
        applyFreeSlot(btnStartTime, btnEndTime, tvFreeSlot);
    }

    private void findWeekFreeSlot(TextView btnStartTime, TextView btnEndTime,
                                  TextView tvFreeSlot) {
        if (allTasks == null || selectedDate == null) {
            Toast.makeText(requireContext(), getString(R.string.error_no_free_slot), Toast.LENGTH_SHORT).show();
            return;
        }
        String[] parts = selectedDate.split("-");
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        Calendar monday = TaskDateUtils.getMonday(cal);
        String[] dayNames = getResources().getStringArray(R.array.day_names_short);

        List<int[]> windows = new java.util.ArrayList<>();
        if (sleepStart > sleepEnd) {
            windows.add(new int[]{sleepEnd, sleepStart});
        } else if (sleepStart < sleepEnd) {
            windows.add(new int[]{0, sleepStart});
            windows.add(new int[]{sleepEnd, 1440});
        } else {
            windows.add(new int[]{0, 1440});
        }

        List<Object[]> suggestions = new java.util.ArrayList<>();
        int gap = durationMinutes;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        for (int offset = 0; offset < 7; offset++) {
            Calendar dayCal = (Calendar) monday.clone();
            dayCal.add(Calendar.DAY_OF_MONTH, offset);

            if (dayCal.before(today)) continue;

            List<Task> dayTasks = TaskDateUtils.getTasksForDay(allTasks, dayCal, dateFormat);
            dayTasks.sort((a, b) -> Integer.compare(timeToMinutes(a.getStartTime()), timeToMinutes(b.getStartTime())));

            for (int[] w : windows) {
                int winStart = w[0], winEnd = w[1];
                int cursor = winStart;
                for (Task t : dayTasks) {
                    int tStart = timeToMinutes(t.getStartTime());
                    int tEnd = timeToMinutes(t.getEndTime());
                    if (tEnd <= winStart || tStart >= winEnd) continue;
                    tStart = Math.max(tStart, winStart);
                    tEnd = Math.min(tEnd, winEnd);
                    if (tStart - cursor >= gap) {
                        int total = cursor + gap;
                        suggestions.add(new Object[]{
                                dayCal.get(Calendar.DAY_OF_WEEK), dayCal,
                                cursor / 60, cursor % 60,
                                total / 60, total % 60,
                                dayTasks.size()
                        });
                        cursor = tStart;
                    }
                    cursor = Math.max(cursor, tEnd);
                }
                if (winEnd - cursor >= gap) {
                    int total = cursor + gap;
                    suggestions.add(new Object[]{
                            dayCal.get(Calendar.DAY_OF_WEEK), dayCal,
                            cursor / 60, cursor % 60,
                            total / 60, total % 60,
                            dayTasks.size()
                    });
                }
            }
        }

        if (suggestions.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error_no_free_slot), Toast.LENGTH_SHORT).show();
            return;
        }

        suggestions.sort((a, b) -> Integer.compare((int) a[6], (int) b[6]));

        String[] items = new String[Math.min(suggestions.size(), 10)];
        String[] dayFullNames = getResources().getStringArray(R.array.day_names);
        for (int i = 0; i < items.length; i++) {
            Object[] s = suggestions.get(i);
            int dayIdx = ((int) s[0] + 5) % 7;
            String dayName = dayFullNames[dayIdx];
            int taskCount = (int) s[6];
            items[i] = dayName + " " + String.format(Locale.getDefault(), "%02d:%02d", s[2], s[3])
                    + "\u2013" + String.format(Locale.getDefault(), "%02d:%02d", s[4], s[5])
                    + " (" + taskCount + " задач)";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.free_slots_title))
                .setItems(items, (dialog, which) -> {
                    Object[] sel = suggestions.get(which);
                    Calendar dayCal = (Calendar) sel[1];
                    selectedDate = dateFormat.format(dayCal.getTime());
                    existingTasks = TaskDateUtils.getTasksForDay(allTasks, dayCal, dateFormat);
                    startHour = (int) sel[2];
                    startMinute = (int) sel[3];
                    endHour = (int) sel[4];
                    endMinute = (int) sel[5];
                    applyFreeSlot(btnStartTime, btnEndTime, tvFreeSlot);
                    int dayIdx = ((int) sel[0] + 5) % 7;
                    tvFreeSlot.setText(getString(R.string.free_slot_week,
                            dayNames[dayIdx],
                            String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute),
                            String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute)));
                })
                .show();
    }

    private void applyFreeSlot(TextView btnStartTime, TextView btnEndTime,
                               TextView tvFreeSlot) {
        btnStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute));
        btnEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));
        tvFreeSlot.setText(getString(R.string.free_slot, String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute),
                String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute)));
        tvFreeSlot.setVisibility(android.view.View.VISIBLE);
    }

    private void updateDurationPills(android.view.View root) {
        boolean matched = false;
        for (int i = 0; i < 5; i++) {
            boolean selected = DURATION_VALUES[i] == durationMinutes;
            if (selected) matched = true;
            TextView pill = root.findViewById(DURATION_PILL_IDS[i]);
            pill.setBackgroundResource(selected ? R.drawable.chip_selected : R.drawable.chip_unselected);
            pill.setTextColor(ContextCompat.getColor(requireContext(),
                    selected ? R.color.primary_foreground : R.color.muted_foreground));
        }
        boolean custom = !matched;
        TextView customPill = root.findViewById(R.id.pillDurCustom);
        customPill.setBackgroundResource(custom ? R.drawable.chip_selected : R.drawable.chip_unselected);
        customPill.setTextColor(ContextCompat.getColor(requireContext(),
                custom ? R.color.primary_foreground : R.color.muted_foreground));
        if (custom) {
            customPill.setText(String.valueOf(durationMinutes));
            customPill.setTextSize(12);
        } else {
            customPill.setText("+");
            customPill.setTextSize(18);
        }
    }

    private void updateRecurrencePills(android.view.View root) {
        for (int i = 0; i < 5; i++) {
            TextView pill = root.findViewById(RECURRENCE_PILL_IDS[i]);
            boolean selected = RECURRENCE_VALUES[i].equals(recurrenceType);
            pill.setBackgroundResource(selected ? R.drawable.chip_selected : R.drawable.chip_unselected);
            pill.setTextColor(ContextCompat.getColor(requireContext(),
                    selected ? R.color.primary_foreground : R.color.muted_foreground));
        }
    }

    private void updateChipStyle(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.chip_selected : R.drawable.chip_unselected);
        chip.setTextColor(selected ?
                ContextCompat.getColor(chip.getContext(), R.color.primary_foreground) :
                ContextCompat.getColor(chip.getContext(), R.color.muted_foreground));
    }

    private boolean hasTimeOverlap(int startMinutes, int endMinutes) {
        if (existingTasks == null) return false;
        for (Task existing : existingTasks) {
            if (selectedDate != null && !selectedDate.equals(existing.getDate())) continue;
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

    private void showTimeError(TextView btnStartTime, TextView tvTimeError, String message) {
        tvTimeError.setText(message);
        tvTimeError.setVisibility(android.view.View.VISIBLE);
        btnStartTime.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(btnStartTime.getContext(), R.color.complexity_extreme)));
    }

    private void clearTimeError(TextView btnStartTime, TextView tvTimeError) {
        tvTimeError.setVisibility(android.view.View.GONE);
        btnStartTime.setBackgroundTintList(null);
    }

    private int[] parseTimeInput(String input) {
        input = input.trim();
        if (input.isEmpty()) return null;
        try {
            if (input.contains(":")) {
                String[] parts = input.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (h < 0 || h > 23 || m < 0 || m > 59) return null;
                return new int[]{h, m};
            } else {
                int h = Integer.parseInt(input);
                if (h < 0 || h > 23) return null;
                return new int[]{h, 0};
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatDuration(int minutes) {
        if (minutes < 60) return getString(R.string.duration_min, minutes);
        int hours = minutes / 60;
        int remain = minutes % 60;
        if (remain == 0) return getString(R.string.duration_hour, hours);
        return getString(R.string.duration_hour_min, hours, remain);
    }
}
