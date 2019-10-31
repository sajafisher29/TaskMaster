package com.example.taskmaster;

import androidx.room.TypeConverter;
import static com.example.taskmaster.Task.TaskState.*;

public class TaskStatusConverter {

    @TypeConverter
    public static Task.TaskState toStatus(int status) {
        if (status == NEW.getCode()) {
            return NEW;
        } else if (status == ASSIGNED.getCode()) {
            return ASSIGNED;
        } else if (status == IN_PROGRESS.getCode()) {
            return IN_PROGRESS;
        } else if (status == COMPLETE.getCode()) {
            return COMPLETE;
        } else {
            throw new IllegalArgumentException("Error finding task status");
        }
    }
    @TypeConverter
    public static int toInt(Task.TaskState status) {
        return status.getCode();
    }

}
