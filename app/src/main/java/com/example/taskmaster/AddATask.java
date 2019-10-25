package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AddATask extends AppCompatActivity {

    public TaskMasterDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_task);

        Button addTask = findViewById(R.id.addTaskButton);
        addTask.setOnClickListener((event) -> {

            TextView titleTextView = findViewById(R.id.taskTitleInput);
            String title = titleTextView.getText().toString();
            TextView bodyTextView = findViewById(R.id.taskDescriptionInput);
            String body = bodyTextView.getText().toString();
            Task newTask = new Task(title, body);

            database = Room.databaseBuilder(getApplicationContext(), TaskMasterDatabase.class, "task").allowMainThreadQueries().build();
            database.taskDao().addTask(newTask);



            // Hide keyboard once the Add Button is clicked
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


            Toast toast = Toast.makeText(this, R.string.submit_confirmation, Toast.LENGTH_LONG);
            toast.show();
        });
    }
}
