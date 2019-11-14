package com.example.taskmaster;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.stream.Stream;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String body;
    @Ignore
    private Team team;
    private String fileKey;

    @TypeConverters(TaskStatusConverter.class)
    public TaskState taskState;

    public enum TaskState {
        NEW(0),
        ASSIGNED(1),
        IN_PROGRESS(2),
        COMPLETE(3);
        private int code;
        TaskState(int code){
            this.code = code;
        }
        public int getCode() {
            return code;
        }
    }

    @Ignore
    public Task(String title, String body, Team team) {
        this.title = title;
        this.body = body;
        this.team = team;
    }

    @Ignore
    public Task(String title, String body, int taskState) {
        this.title = title;
        this.body = body;
        this.taskState = getState();
    }

    public Task(String title, String body, TaskState state, String fileKey) {
        this.title = title;
        this.body = body;
        this.taskState = state;
        this.fileKey = fileKey;
    }

    @Ignore
    public Task(String title, String body, TaskState state) {
        this.title = title;
        this.body = body;
        this.taskState = state;
        this.fileKey = null;
    }

    public Task() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
        return taskState;
    }

    public void setState(TaskState state) {
        this.taskState = state;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    @Override
    public String toString() {
        return String.format("%s is %s: %s", this.title, this.taskState, this.body);
    }

    public void setCloudId(long id) {
    }
}

