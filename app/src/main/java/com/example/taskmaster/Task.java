package com.example.taskmaster;

public class Task {

    private String title;
    private String body;
    private TaskState state;


    public Task(String title, String body) {
        this.title = title;
        this.body = body;
        this.state = TaskState.NEW;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("%s is %s: %s", this.title, this.state, this.body);
    }
}

