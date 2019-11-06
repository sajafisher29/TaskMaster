package com.example.taskmaster;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Team {

    @PrimaryKey(autoGenerate = true)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private String name;
    private List<Task> teamTasks;

    public Team(String name, List<Task> teamTasks) {
        this.name = name;
        this.teamTasks = teamTasks;
    }

    
}
