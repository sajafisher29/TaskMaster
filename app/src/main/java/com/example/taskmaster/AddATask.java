package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

public class AddATask extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_task);
    }

//    // Hide keyboard once the Add Button is clicked
//    InputMethodManager inputManager = (InputMethodManager)
//            getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//}

    // Create a Toast to display the submitted message
    public void showMessageConfirmingSubmit(View view) {
        Toast toast = Toast.makeText(this, R.string.submit_confirmation, Toast.LENGTH_LONG);
        toast.show();
    }
}
