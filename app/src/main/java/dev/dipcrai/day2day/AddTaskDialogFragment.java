package dev.dipcrai.day2day;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.Random;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AddTaskDialogFragment extends DialogFragment {

    public interface OnTaskCreatedListener {
        void onTaskCreated(Task task);
    }

    private static final int[] COLORS = {
            0xFF3B82F6, 0xFF8B5CF6, 0xFF22C55E,
            0xFFF59E0B, 0xFFEC4899, 0xFF06B6D4,
            0xFFEF4444, 0xFFF97316, 0xFF84CC16
    };

    private OnTaskCreatedListener listener;
    private String selectedDate;
    private int selectedHour = 9;
    private int selectedMinute = 0;
    private int durationMinutes = 60;

    public void setOnTaskCreatedListener(OnTaskCreatedListener listener) {
        this.listener = listener;
    }

    public void setSelectedDate(String date) {
        this.selectedDate = date;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        TextInputEditText etTitle = view.findViewById(R.id.etTaskTitle);
        TextInputEditText etDescription = view.findViewById(R.id.etTaskDescription);
        Button btnStartTime = view.findViewById(R.id.btnStartTime);
        Button btnDuration = view.findViewById(R.id.btnDuration);
        Slider sliderComplexity = view.findViewById(R.id.sliderComplexity);
        TextView tvComplexityValue = view.findViewById(R.id.tvComplexityValue);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        btnStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));

        btnStartTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                    (view1, hourOfDay, minute) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        btnStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    }, selectedHour, selectedMinute, true);
            timePicker.show();
        });

        btnDuration.setText(formatDuration(durationMinutes));

        btnDuration.setOnClickListener(v -> {
            String[] items = {"30 мин", "1 час", "1.5 часа", "2 часа", "3 часа", "4 часа"};
            int[] values = {30, 60, 90, 120, 180, 240};
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Длительность")
                    .setItems(items, (dialog, which) -> {
                        durationMinutes = values[which];
                        btnDuration.setText(formatDuration(durationMinutes));
                    })
                    .show();
        });

        sliderComplexity.addOnChangeListener((slider, value, fromUser) ->
                tvComplexityValue.setText(String.valueOf((int) value)));

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etTitle.setError("Введите название");
                return;
            }
            String description = etDescription.getText().toString().trim();
            int complexity = (int) sliderComplexity.getValue();

            int endHour = selectedHour + durationMinutes / 60;
            int endMinute = selectedMinute + durationMinutes % 60;

            String startTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
            String endTime = String.format(Locale.getDefault(), "%02d:%02d", endHour % 24, endMinute % 60);

            int color = COLORS[new Random().nextInt(COLORS.length)];
            String id = String.valueOf(System.currentTimeMillis());
            String date = selectedDate != null ? selectedDate : new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());

            Task task = new Task(id, title, description, date, startTime, endTime, color, complexity);

            if (listener != null) {
                listener.onTaskCreated(task);
            }
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    private String formatDuration(int minutes) {
        if (minutes < 60) return minutes + " мин";
        int hours = minutes / 60;
        int remain = minutes % 60;
        if (remain == 0) return hours + " ч";
        return hours + " ч " + remain + " мин";
    }
}
