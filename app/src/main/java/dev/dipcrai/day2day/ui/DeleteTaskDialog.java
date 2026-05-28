package dev.dipcrai.day2day.ui;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import dev.dipcrai.day2day.R;
import dev.dipcrai.day2day.Task;
import dev.dipcrai.day2day.data.repository.TaskRepository;
import dev.dipcrai.day2day.util.TaskDateUtils;

public class DeleteTaskDialog {

    public interface OnDeleteCallback {
        void onDeleted();
    }

    public static void show(Context context, Task task, Calendar selectedDate,
                            List<Task> allTasks, TaskRepository taskRepository,
                            SimpleDateFormat dateFormat, OnDeleteCallback callback) {
        String type = task.getRecurrenceType();
        boolean isRecurring = type != null && !"once".equals(type);

        if (!isRecurring) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle(context.getString(R.string.delete_task_title))
                    .setMessage(context.getString(R.string.delete_task_message, task.getTitle()))
                    .setPositiveButton(context.getString(R.string.delete_positive), (dialog, which) -> {
                        taskRepository.deleteById(task.getId(), result -> {});
                        allTasks.remove(task);
                        callback.onDeleted();
                    })
                    .setNegativeButton(context.getString(R.string.delete_cancel), null)
                    .show();
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.delete_recurring_title))
                .setMessage(context.getString(R.string.delete_task_message, task.getTitle()))
                .setPositiveButton(context.getString(R.string.delete_this), (dialog, which) -> {
                    String today = TaskDateUtils.dateToString(selectedDate, dateFormat);
                    String excluded = task.getExcludedDates();
                    if (excluded == null || excluded.isEmpty()) {
                        task.setExcludedDates(today);
                    } else {
                        task.setExcludedDates(excluded + "," + today);
                    }
                    taskRepository.update(task, result -> {});
                    callback.onDeleted();
                })
                .setNeutralButton(context.getString(R.string.delete_all), (dialog, which) -> {
                    taskRepository.deleteById(task.getId(), result -> {});
                    allTasks.remove(task);
                    callback.onDeleted();
                })
                .setNegativeButton(context.getString(R.string.delete_cancel), null)
                .show();
    }
}
