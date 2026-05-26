package dev.dipcrai.day2day;

public class Task {
    private final String id;
    private final String title;
    private final String description;
    private final String startTime;
    private final String endTime;
    private final int color;
    private final int complexity;

    public Task(String id, String title, String description, String startTime, String endTime, int color, int complexity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
        this.complexity = complexity;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public int getColor() { return color; }
    public int getComplexity() { return complexity; }
}
