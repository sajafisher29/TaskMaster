package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void onSaveButtonClicked(View view) {
        // Save the username in SharedPreferences
        EditText nameEditText = findViewById(R.id.usernameInput);
        String name = nameEditText.getText().toString();
        // Grab the SharedPreference in which to save the username data
        SharedPreferences usernameSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Save the data to SharedPreferences
        SharedPreferences.Editor editor = usernameSharedPreferences.edit();
        editor.putString("username", name);
        editor.apply();
        Intent goToHomepage = new Intent(Settings.this, MainActivity.class);
        Settings.this.startActivity(goToHomepage);
    }
}
