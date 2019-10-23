package com.example.taskmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grab the add a task button
        Button addTaskButton = findViewById(R.id.addTaskButton);
        // Add an event listener
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                Intent goToAddATaskActivityIntent = new Intent(MainActivity.this, AddATask.class);
                MainActivity.this.startActivity(goToAddATaskActivityIntent);
            }
        });

        // Grab the all tasks button
        Button allTasksButton = findViewById(R.id.allTasksButton);
        // Add an event listener
        allTasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                Intent goToAllTasksActivityIntent = new Intent(MainActivity.this, AllTasks.class);
                MainActivity.this.startActivity(goToAllTasksActivityIntent);
            }
        });

        // For hardcoded task buttons
        final Button task1Button = findViewById(R.id.taskTitle1Button);
        // Add an event listener
        task1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                Intent redirectToTaskDetailIntent = new Intent(MainActivity.this, TaskDetails.class);
                String task1Title = task1Button.getText().toString();
                redirectToTaskDetailIntent.putExtra("taskTitle", task1Title);
                MainActivity.this.startActivity(redirectToTaskDetailIntent);
            }
        });

        // For hardcoded task buttons
        final Button task2Button = findViewById(R.id.taskTitle2Button);
        // Add an event listener
        task2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                Intent redirectToTaskDetailIntent = new Intent(MainActivity.this, TaskDetails.class);
                String task2Title = task2Button.getText().toString();
                redirectToTaskDetailIntent.putExtra("taskTitle", task2Title);
                MainActivity.this.startActivity(redirectToTaskDetailIntent);
            }
        });

        // For hardcoded task buttons
        final Button task3Button = findViewById(R.id.taskTitle3Button);
        // Add an event listener
        task3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                Intent redirectToTaskDetailIntent = new Intent(MainActivity.this, TaskDetails.class);
                String task3Title = task3Button.getText().toString();
                redirectToTaskDetailIntent.putExtra("taskTitle", task3Title);
                MainActivity.this.startActivity(redirectToTaskDetailIntent);
            }
        });
    }

//    public static final String taskTitle = "taskTitle";

}
