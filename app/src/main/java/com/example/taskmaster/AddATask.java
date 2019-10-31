package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
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
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.exception.ApolloException;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import javax.annotation.Nonnull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import type.CreateTaskInput;
import type.TaskState;

import static android.widget.Toast.*;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.taskmaster.R.string.submit_confirmation;

public class AddATask extends AppCompatActivity {

    private static final String TAG = "fisher.AddATaskActivity";

    public TaskMasterDatabase database;
    private EditText inputTaskTitle;
    private EditText inputTaskDescription;
    AWSAppSyncClient awsAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_task);

        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        Button addTask = findViewById(R.id.addTaskButton);
        addTask.setOnClickListener((event) -> {

            TextView titleTextView = findViewById(R.id.taskTitleInput);
            String title = titleTextView.getText().toString();
            TextView bodyTextView = findViewById(R.id.taskDescriptionInput);
            String body = bodyTextView.getText().toString();
            Task newTask = new Task(title, body);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String username = preferences.getString("username", "user");
//            newTask.setAssignedUser(username);

//            database = Room.databaseBuilder(getApplicationContext(), TaskMasterDatabase.class, "task").allowMainThreadQueries().build();
//            database.taskDao().addTask(newTask);

            // Hide keyboard once the Add Button is clicked
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            // Saving the new task
            PostTasksToBackendServerCallback.runAddATaskMutation(title, body, "NEW");

            Toast toast = makeText(this, submit_confirmation, LENGTH_LONG);
            toast.show();
        });
    }

    public void showSubmittedMessage(View view) {

        String taskTitle = findViewById(R.id.taskTitleInput).toString();
        String taskDescription = findViewById(R.id.taskDescriptionInput).toString();

        PostTasksToBackendServerCallback.runAddATaskMutation(inputTaskTitle.getText().toString(), inputTaskDescription.getText().toString(), "NEW");

//        OkHttpClient client = new OkHttpClient();
//        RequestBody requestBody = new FormBody.Builder()
//                .add("title", taskTitle)
//                .add("body", taskDescription)
//                .build();
//        Request request = new Request.Builder()
//                .url("http://taskmaster-api.herokuapp.com/tasks")
//                .post(requestBody)
//                .build();
//
//        client.newCall(request).enqueue(new PostTasksToBackendServerCallback(this));

        Task newTask = new Task(taskTitle, taskDescription);
        database.taskDao().addTask(newTask);

        Intent addTaskToMainPageIntent = new Intent(this, MainActivity.class);
        startActivity(addTaskToMainPageIntent);
        finish();
    }
}

class PostTasksToBackendServerCallback implements Callback {

    AddATask addTaskActivity;
    static AWSAppSyncClient awsAppSyncClient;

    public PostTasksToBackendServerCallback(AddATask addTaskActivity) {
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

    // Insert a new task
    public static void runAddATaskMutation(String title, String description, String state) {
        CreateTaskInput createTaskInput = CreateTaskInput.builder()
                .name(title)
                .description(description)
                .taskState(TaskState.valueOf(state))
                .build();
        awsAppSyncClient.mutate(CreateTaskMutation.builder().input(createTaskInput).build())
                .enqueue(addTaskCallBack);
    }

    // Callback for inserting a new task
    public static GraphQLCall.Callback<CreateTaskMutation.Data> addTaskCallBack = new GraphQLCall.Callback<CreateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull com.apollographql.apollo.api.Response<CreateTaskMutation.Data> response) {

            Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessage) {
                    Toast toast = makeText(R.string.submit_confirmation, Toast.LENGTH_LONG);
                    toast.show();
                }
            };

            Message completeMessage = handlerForMainThread.obtainMessage(0);
            completeMessage.sendToTarget();
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.getMessage());
        }
    };
}
