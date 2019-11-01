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

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.exception.ApolloException;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.taskmaster.PostTasksToBackendServerCallback.awsAppSyncClient;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskInteractionListener{

    private static final String TAG = "fisher.MainActivity";

    public String enteredTaskName = null;
    public String enteredTaskPreference = null;
    public List<Task> tasks;
    public TaskMasterDatabase database;
    RecyclerView recyclerView;
    private RecyclerView.Adapter taskAdapter;
    AWSAppSyncClient awsAppSyncClient;

    @Override
    protected void onResume() {
        super.onResume();
        // Grab username from SharedPreferences and use it to update the user's name displayed
        SharedPreferences usernameSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = usernameSharedPreferences.getString("username", "user");
        TextView nameTextView = findViewById(R.id.greetingTextView);
        nameTextView.setText("Hello " + username + "!"); // Strings are coded to replace this. Needs to be refactored.

    }

    // This gets called automatically when MainActivity is created/shown for the first time
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

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

    public void taskSelected(Task task) {
        Intent goToTaskDetailsPageActivityIntent = new Intent(this, TaskDetails.class);

        // Add info about what task is being checked
        goToTaskDetailsPageActivityIntent.putExtra("taskTitle", task.getTitle());
        goToTaskDetailsPageActivityIntent.putExtra("taskBody", task.getBody());
        Log.i(TAG, "inside taskSelected trying to move to Task Title " + task.getTitle());
        MainActivity.this.startActivity(goToTaskDetailsPageActivityIntent);
    }

    public static final String taskTitle = "taskTitle";

    public void putDataOnPage(String data){

        // Turn JSON into InternetTasks
        Gson gson = new Gson();
        InternetTask[] incomingArray = gson.fromJson(data, InternetTask[].class);

        //Getting a set of existing titles
        HashSet<String> titles = new HashSet<>();

        for(Task task : this.tasks){
            titles.add(task.getTitle());
        }

        for(InternetTask internetTask: incomingArray){
            //if the title is a new title then add it
            if(!titles.contains(internetTask.getTitle())){
                titles.add(internetTask.getTitle());
                Task newTask = new Task(internetTask);
                tasks.add(newTask);
                database.taskDao().addTask(newTask);
            }
        }
        
        taskAdapter.notifyDataSetChanged();

    }

}

abstract class LogDataWhenItComesBackCallback implements Callback {

    MainActivity mainActivityInstance;

    public LogDataWhenItComesBackCallback(MainActivity mainActivityInstance) {
        this.mainActivityInstance = mainActivityInstance;
    }

    private static final String TAG = "fisher.Callback";

    public void runQuery(){
        awsAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(tasksCallback);
    }

    private GraphQLCall.Callback<ListTasksQuery.Data> tasksCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull com.apollographql.apollo.api.Response<ListTasksQuery.Data> response) {
            Log.i("Results", response.data().listTasks().items().toString());
            Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessageToMain){
                    mainActivityInstance.putDataOnPage((String)inputMessageToMain.obj);
                }
            };
            Message completeMessage = handlerForMainThread.obtainMessage(0, response);
            completeMessage.sendToTarget();
        }

        @Override
        public void onFailure(@Nonnull ApolloException error) {
            Log.e("ERROR", error.toString());
        }
    };

}