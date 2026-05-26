package dev.dipcrai.day2day;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import androidx.annotation.NonNull;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "start_time")
    private String startTime;

    @ColumnInfo(name = "end_time")
    private String endTime;

    @ColumnInfo(name = "color")
    private int color;

    @ColumnInfo(name = "complexity")
    private int complexity;

    @ColumnInfo(name = "recurrence_type")
    private String recurrenceType;

    @ColumnInfo(name = "recurrence_days")
    private String recurrenceDays;

    @ColumnInfo(name = "recurrence_end_date")
    private String recurrenceEndDate;

    @ColumnInfo(name = "excluded_dates")
    private String excludedDates;

    public Task() {}

    @Ignore
    public Task(String id, String title, String description, String date,
                String startTime, String endTime, int color, int complexity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
        this.complexity = complexity;
        this.recurrenceType = "once";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public int getComplexity() { return complexity; }
    public void setComplexity(int complexity) { this.complexity = complexity; }

    public String getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }

    public String getRecurrenceDays() { return recurrenceDays; }
    public void setRecurrenceDays(String recurrenceDays) { this.recurrenceDays = recurrenceDays; }

    public String getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(String recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }

    public String getExcludedDates() { return excludedDates; }
    public void setExcludedDates(String excludedDates) { this.excludedDates = excludedDates; }
}
