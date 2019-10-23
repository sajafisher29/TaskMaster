package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Grab the save username button
        Button saveUsernameButton = findViewById(R.id.usernameSaveButton);
        // Add an event listener
        saveUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
//                Intent goToAllTasksActivityIntent = new Intent(MainActivity.this, AllTasks.class);
//                MainActivity.this.startActivity(goToAllTasksActivityIntent);
            }
        });
    }

}
