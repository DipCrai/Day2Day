package dev.dipcrai.day2day.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dev.dipcrai.day2day.Task;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY start_time ASC")
    List<Task> getAllTasks();

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY start_time ASC")
    List<Task> getTasksByDate(String date);

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getTaskById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM tasks WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM tasks")
    void deleteAll();
}
