package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AddATask extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_task);
    }

    // Create a Toast to display the submitted message
    public void showMessageConfirmingSubmit(View view) {
        Toast toast = Toast.makeText(this, R.string.submit_confirmation, Toast.LENGTH_LONG);
        toast.show();
    }
}
