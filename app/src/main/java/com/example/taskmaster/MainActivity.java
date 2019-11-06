package com.example.taskmaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.amplify.generated.graphql.OnCreateTaskSubscription;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import okhttp3.Callback;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskInteractionListener{

    private static final String TAG = "fisher.MainActivity";

    public List<Task> tasks;
    RecyclerView recyclerView;
    AWSAppSyncClient awsAppSyncClient;
    private RecyclerView.Adapter taskAdapter;
    List<ListTeamsQuery.Item> teams;
    ListTeamsQuery.Item selectedTeam;

    @Override
    protected void onResume() {
        super.onResume();
        String username = AWSMobileClient.getInstance().getUsername();
        TextView nameTextView = findViewById(R.id.greetingTextView);
        nameTextView.setText("Hello " + username + "!"); // Strings are coded to replace this. Needs to be refactored so it is translatable.

        runQueryForAllTeams();

        // Subscribe for future updates
        OnCreateTaskSubscription subscription = OnCreateTaskSubscription.builder().build();
        awsAppSyncClient.subscribe(subscription).execute(new AppSyncSubscriptionCall.Callback<OnCreateTaskSubscription.Data>() {
            @Override
            public void onResponse(@Nonnull com.apollographql.apollo.api.Response<OnCreateTaskSubscription.Data> response) {
                // AWS calls this method when a new Task is created
                // Task newTask = new Task(response.data().onCreateTask().title(), response.data().onCreateTask().body(), response.data().onCreateTask().state());
                // taskAdapter.addTask(newTask);
            }

            @Override
            public void onFailure(@Nonnull ApolloException error) {
                Log.e(TAG, error.getMessage());
            }

            @Override
            public void onCompleted() {
                Log.i(TAG, "Subscribed to task");
            }
        });
    }

    public void redirectToAddATeamActivity(View view) {
        Intent addTeamIntent = new Intent(this, AddATeam.class);
        startActivity(addTeamIntent);
    }

    @Override
    public void redirectToTaskDetailPage(Task task) {
        Intent taskDetailIntent = new Intent(this, TaskDetail.class);
        taskDetailIntent.putExtra("title", "" + task.getTitle());
        taskDetailIntent.putExtra("description", "" + task.getBody());
        taskDetailIntent.putExtra("state", "" + task.getState());
        startActivity(taskDetailIntent);
    }

    public void runQueryForAllTeams(){
        awsAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(getAllTasksCallback);
    }

    // This gets called automatically when MainActivity is created/shown for the first time
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize AWS' Mobile Client to check on log in/out status
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.i("INIT", "onResult: " + result.getUserState().toString());
                if (result.getUserState().toString().equals("SIGN_OUT")) {
                    AWSMobileClient.getInstance().showSignIn(MainActivity.this,
                        SignInUIOptions.builder().logo(R.drawable.leaftaskmasterapp).build(),
                        new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
                            @Override
                            public void onResult(UserStateDetails result) {
                                signInUser();
                            }

                            @Override
                            public void onError(Exception error) {
                                Log.e("error_signing_in", error.getMessage());
                            }
                        });
                }
            }

            @Override
            public void onError(Exception error) {
                Log.e("INIT", "Initialization error.", error);
            }
        });

        setContentView(R.layout.activity_main);

        this.tasks = new LinkedList<>();
        this.teams = new LinkedList<>();


        // Connect to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        // Set up RecyclerView
        recyclerView = findViewById(R.id.mainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(tasks, this);
        recyclerView.setAdapter(taskAdapter);

        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener((event) -> {
            // Add the sign in
            // 'this' refers the the current active activity, probably replace with MainActivity.this
            AWSMobileClient.getInstance().showSignIn(MainActivity.this, SignInUIOptions.builder().build(), new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails result) {
                    Log.d(TAG, "onResult: " + result.getUserState());
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "onError: ", error);
                }
            });
        });

        Button signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener((event) -> {
            AWSMobileClient.getInstance().signOut();
        });

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
            public void onClick (View event){
            Intent goToAllTasksActivityIntent = new Intent(MainActivity.this, AllTasks.class);
            MainActivity.this.startActivity(goToAllTasksActivityIntent);
    }
        });

    // Grab the settings button
    Button settingsButton = findViewById(R.id.settingsPageButton);
    // Add an event listener
        settingsButton.setOnClickListener(new View.OnClickListener()

    {
        @Override
        public void onClick (View event){
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

    }

abstract class LogDataWhenItComesBackCallback implements Callback {

    MainActivity mainActivityInstance;

    public LogDataWhenItComesBackCallback(MainActivity mainActivityInstance) {
        this.mainActivityInstance = mainActivityInstance;
    }

    private static final String TAG = "fisher.Callback";

}