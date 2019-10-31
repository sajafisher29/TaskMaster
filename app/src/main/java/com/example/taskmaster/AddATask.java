package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class AddATask extends AppCompatActivity {

    private static final String TAG = "fisher.AddATaskActivity";

    public TaskMasterDatabase database;
    private EditText inputTaskTitle;
    private EditText inputTaskDescription;

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
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String username = preferences.getString("username", "user");
            newTask.setAssignedUser(username);

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

    public void showSubmittedMessage(View view) {

        OkHttpClient client = new OkHttpClient();
        String taskTitle = findViewById(R.id.taskTitleInput).toString();
        String taskDescription = findViewById(R.id.taskDescriptionInput).toString();

        RequestBody requestBody = new FormBody.Builder()
                .add("title", taskTitle)
                .add("body", taskDescription)
                .build();
        Request request = new Request.Builder()
                .url("http://taskmaster-api.herokuapp.com/tasks")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new LogDataWhenItComesBackCallback(this));

        Task newTask = new Task(taskTitle, taskDescription);
        database.taskDao().addTask(newTask);

        Intent addTaskToMainPageIntent = new Intent(this, MainActivity.class);
        startActivity(addTaskToMainPageIntent);
        finish();
    }
}

class PostTasksToBackendServer implements Callback {

    AddATask addTaskActivity;

    public PostTasksToBackendServer(AddATask addTaskActivity) {
        this.addTaskActivity = addTaskActivity;
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        Log.e(TAG, "something went wrong with connecting to backend server");
        Log.e(TAG, e.getMessage());
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                addTaskActivity.finish();
            }
        };

        Message completeMessage = handlerForMainThread.obtainMessage(0);
        completeMessage.sendToTarget();
    }
}
