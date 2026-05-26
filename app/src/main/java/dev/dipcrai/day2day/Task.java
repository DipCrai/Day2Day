package dev.dipcrai.day2day;

public class Task {
    private String id;
    private String title;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private int color;
    private int complexity;

    public Task() {}

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
}
