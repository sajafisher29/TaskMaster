package com.example.taskmaster;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    private String title;
    private String body;
//    private TaskState state; // Specified with the enum


    public Task(String title, String body) {
        this.title = title;
        this.body = body;
        this.taskState = TaskState.NEW;
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

    @Override
    public String toString() {
        return String.format("%s is %s: %s", this.title, this.taskState, this.body);
    }
}

