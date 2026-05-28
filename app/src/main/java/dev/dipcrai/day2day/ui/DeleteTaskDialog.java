package dev.dipcrai.day2day.ui;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

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
                    .setTitle("Удалить задачу")
                    .setMessage("Удалить \"" + task.getTitle() + "\"?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        taskRepository.deleteById(task.getId(), result -> {});
                        allTasks.remove(task);
                        callback.onDeleted();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle("Удалить повторяющуюся задачу")
                .setMessage("Удалить \"" + task.getTitle() + "\"?")
                .setPositiveButton("Только это", (dialog, which) -> {
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
                .setNeutralButton("Все", (dialog, which) -> {
                    taskRepository.deleteById(task.getId(), result -> {});
                    allTasks.remove(task);
                    callback.onDeleted();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
