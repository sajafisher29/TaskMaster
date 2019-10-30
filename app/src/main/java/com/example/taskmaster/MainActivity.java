package com.example.taskmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskInteractionListener{

    private static final String TAG = "fisher.MainActivity";

    public String enteredTaskName = null;
    public String enteredTaskPreference = null;
    private List<Task> tasks;
    public TaskMasterDatabase database;
    private RecyclerView.Adapter taskAdapter;

    public void renderData(String data) {

        TextView headerTextView = findViewById(R.id.dataTextView);
        headerTextView.setText(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Grab username from SharedPreferences and use it to update the user's name displayed
        SharedPreferences usernameSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = usernameSharedPreferences.getString("username", "user");
        TextView nameTextView = findViewById(R.id.greetingTextView);
        if (username.equals("user") || username.equals("")) {
            myTaskTitle.setText("My Tasks");
        } else {
            myTaskTitle.setText(username + "'s Tasks");
        }

        // Get data from the internet
        // Reference: https://square.github.io/okhttp/
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://taskmaster-api.herokuapp.com/tasks")
                .build();

        // Callback: a function to specify what should happen after the request is done/the response is here
        client.newCall(request).enqueue(new LogDataWhenItComesBackCallback(this));
    }


        if (username.equals("My") || username.equals("")) {
            myTaskTitle.setText("My Tasks");
        } else {
            myTaskTitle.setText("" + username + "'s Tasks");
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://taskmaster-api.herokuapp.com/tasks")
                .build();

        client.newCall(request).enqueue(new LogDataWhenItComesBackCallback(this));
    }

    // This gets called automatically when MainActivity is created/shown for the first time
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get data from the internet
        // Reference: https://square.github.io/okhttp/
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://taskmaster-api.herokuapp.com/tasks")
                .build();

        // Callback: a function to specify what should happen after the request is done/the response is here
        client.newCall(request).enqueue(new LogDataWhenItComesBackCallback(this));

        // Database
        database = Room.databaseBuilder(getApplicationContext(), TaskMasterDatabase.class, "task")
                    .allowMainThreadQueries().build();

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

        // Grab the settings button
        Button settingsButton = findViewById(R.id.settingsPageButton);
        // Add an event listener
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                Intent goToSettingsActivityIntent = new Intent(MainActivity.this, Settings.class);
                MainActivity.this.startActivity(goToSettingsActivityIntent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://taskmaster-api.herokuapp.com/tasks")
                .build();

        client.newCall(request).enqueue(new LogDataWhenItComesBackCallback(this));
    }

    public void taskSelected(Task task) {
        Intent goToTaskDetailsPageActivityIntent = new Intent(this, TaskDetails.class);

        // Add info about what task is being checked
        goToTaskDetailsPageActivityIntent.putExtra("taskTitle", task.getTitle());
        goToTaskDetailsPageActivityIntent.putExtra("taskBody", task.getBody());
        Log.i(TAG, "inside taskSelected trying to move to Task Title " + task.getTitle());
        MainActivity.this.startActivity(goToTaskDetailsPageActivityIntent);
    }

    public static final String taskTitle = "taskTitle";

}

class LogDataWhenItComesBackCallback implements Callback {

    MainActivity actualMainActivityInstance;

    public LogDataWhenItComesBackCallback(MainActivity actualMainActivityInstance) {
        this.actualMainActivityInstance = actualMainActivityInstance;
    }

    private static final String TAG = "fisher.Callback";

    // OkHttp will call this if the request fails

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException error) {
        Log.e(TAG, "Internet error");
        Log.e(TAG, error.getMessage());
    }

    // OkHttp will call this if the request succeeds

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        String responseBody = response.body().string();
        Log.i(TAG, responseBody);

        // Turning JSON into InternetTasks
        Gson gson = new Gson();
        InternetTask[] incomingAPITaskArray = gson.fromJson(data, InternetTask[].class);

        // Database
        database = Room.databaseBuilder(getApplicationContext(), TaskMasterDatabase.class, "task")
                .allowMainThreadQueries().build();

        this.tasks = new LinkedList<>();
        this.tasks.addAll(database.taskDao().getAll());

        // Defining a class that extends Handler with the curly braces
        Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                Task[] listOfInternetTasks = (Task[])inputMessage.obj;
                for (Task task: listOfInternetTasks) {
                    if (database.taskDao().getTasksByTitleAndBody(task.getTitle(), task.getBody()) == null) {
                        database.taskDao().addTask(task);
                    }
                }
                // Grab the data out of the Message object and pass to actualMainActivityInstance
                actualMainActivityInstance.renderData((String) inputMessage.obj);
            }
        };

        Message completeMessage =
                handlerForMainThread.obtainMessage(0, responseBody);
        completeMessage.sendToTarget();
    }
}