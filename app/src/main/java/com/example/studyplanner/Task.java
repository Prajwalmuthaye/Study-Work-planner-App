package com.example.studyplanner;

public class Task {
    private int id;
    private String title;
    private boolean isDone;

    private String category;

    public Task(int id, String title, boolean isDone,String category) {
        this.id = id;
        this.title = title;
        this.isDone = isDone;
        this.category = category != null ? category : "No Category";
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public boolean isDone() { return isDone; }

    public void setDone(boolean done) { isDone = done; }

    public void setTitle(String newTitle) {
        this.title = newTitle;
    }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category;}
}
