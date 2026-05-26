package dev.dipcrai.day2day.data.repository;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.dipcrai.day2day.Task;
import dev.dipcrai.day2day.data.local.dao.TaskDao;

public class TaskRepository {

    private final TaskDao taskDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public TaskRepository(TaskDao taskDao) {
        this.taskDao = taskDao;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getAllTasks(Callback<List<Task>> callback) {
        executor.execute(() -> {
            List<Task> tasks = taskDao.getAllTasks();
            mainHandler.post(() -> callback.onResult(tasks));
        });
    }

    public void getTasksByDate(String date, Callback<List<Task>> callback) {
        executor.execute(() -> {
            List<Task> tasks = taskDao.getTasksByDate(date);
            mainHandler.post(() -> callback.onResult(tasks));
        });
    }

    public void insert(Task task, Callback<Void> callback) {
        executor.execute(() -> {
            taskDao.insert(task);
            mainHandler.post(() -> callback.onResult(null));
        });
    }

    public void delete(Task task, Callback<Void> callback) {
        executor.execute(() -> {
            taskDao.delete(task);
            mainHandler.post(() -> callback.onResult(null));
        });
    }

    public void deleteById(String id, Callback<Void> callback) {
        executor.execute(() -> {
            taskDao.deleteById(id);
            mainHandler.post(() -> callback.onResult(null));
        });
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
