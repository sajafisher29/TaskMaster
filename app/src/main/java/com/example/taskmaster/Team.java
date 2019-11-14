package com.example.taskmaster;

import androidx.annotation.NonNull;

import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;

public class Team {

    private String id;
    private String name;

    public Team (String name) {
        this.name = name;
    }

    public Team(ListTeamsQuery.Item teamItem) {
        this.id = teamItem.id();
        this.name = teamItem.name();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}
