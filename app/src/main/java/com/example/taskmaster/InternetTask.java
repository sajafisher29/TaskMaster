package com.example.taskmaster;

import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

class InternetTask {

    @PrimaryKey(autoGenerate = true)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

//    @TypeConverters(TaskStatusConverter.class)
//    public InternetTask.TaskState taskState;
//    public enum TaskState {
//        NEW(0),
//        ASSIGNED(1),
//        IN_PROGRESS(2),
//        COMPLETE(3);
//        private int code;
//        TaskState(int code){
//            this.code = code;
//        }
//        public int getCode() {
//            return code;
//        }
//    }

    private String title;
    private String body;
    private String state;

    public InternetTask(String title, String body) {
        this.title = title;
        this.body = body;
//        this.taskState = state;
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

//    public InternetTask.TaskState getState() {
//        return taskState;
//    }
//
//    public void setState(InternetTask.TaskState state) {
//        this.taskState = state;
//    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.title, this.body);
    }
}
