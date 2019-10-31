package com.example.taskmaster;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("DELETE FROM task")
    public void nukeTable();

    @Query("SELECT * FROM task ORDER BY id DESC")
    List<Task> getAll();

    @Query("SELECT * FROM task WHERE id=:id")
    Task getTasksById(long id);

    @Query("SELECT * FROM task WHERE title=:title AND body=:body")
    Task getTasksByTitleAndBody(String title, String body);

    @Insert
    void addTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

}
