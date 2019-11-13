package com.example.taskmaster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amazonaws.amplify.generated.graphql.GetTeamQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.amplify.generated.graphql.OnCreateTaskSubscription;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

import okhttp3.Call;
import okhttp3.Callback;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskInteractionListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "fisher.MainActivity";

    public List<Task> tasks;
    RecyclerView recyclerView;
    AWSAppSyncClient awsAppSyncClient;
    private RecyclerView.Adapter taskAdapter;
    List<ListTeamsQuery.Item> teamItems;  //originally teams
    ListTeamsQuery.Item selectedTeamItem; //originally selectedTeam
    private FusedLocationProviderClient fusedLocationClient;
    private static PinpointManager pinpointManager;

    // This gets called automatically when MainActivity is created/shown for the first time
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = {READ_EXTERNAL_STORAGE, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, 1);


        // Initialize AWS' Mobile Client to check on log in/out status
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.i("INIT", "onResult: " + result.getUserState().toString());
                if (result.getUserState().toString().equals("SIGNED_OUT")) {
                    AWSMobileClient.getInstance().showSignIn(MainActivity.this,
                            SignInUIOptions.builder().logo(R.drawable.leaftaskmasterapp).build(),
                            new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
                                @Override
                                public void onResult(UserStateDetails result) {

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

        // Connect to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        // Initialize PinpointManager
        getPinpointManager(getApplicationContext());

        this.tasks = new LinkedList<>();
        this.teamItems = new LinkedList<>();

        queryAllTeams();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setContentView(R.layout.activity_main);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.mainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(tasks, this);
        recyclerView.setAdapter(taskAdapter);

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

        // Get Location button
        Button locationButton = findViewById(R.id.locationButton);
        // Add event listener
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                fusedLocationClient.getLastLocation()
                        .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        })
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    Log.i(TAG, location.toString());
                                }
                            }
                        });
            }
        });

        // Subscribe to future updates
        Log.i(TAG, "Building subscription");
        OnCreateTaskSubscription subscription = OnCreateTaskSubscription.builder().build();
        awsAppSyncClient.subscribe(subscription).execute(new AppSyncSubscriptionCall.Callback<OnCreateTaskSubscription.Data>() {
            @Override
            public void onResponse(@Nonnull com.apollographql.apollo.api.Response<OnCreateTaskSubscription.Data> response) {
                // AWS calls this method when a new Task is created
                Log.i(TAG, "New task added");
                final Task newItem = new Task(response.data().onCreateTask().name(), response.data().onCreateTask().description());
                Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message inputMessage) {
                        tasks.add(newItem);
                        taskAdapter.notifyDataSetChanged();
                    }
                };

                handler.obtainMessage().sendToTarget();

            }

            @Override
            public void onFailure(@Nonnull ApolloException error) {
                Log.i(TAG, Arrays.toString(error.getStackTrace()));
                Log.i(TAG, error.getCause().getMessage());
            }

            @Override
            public void onCompleted() {
                // Subscribed
                Log.i(TAG, "Subscribed to task updates");
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (AWSMobileClient.getInstance().isSignedIn()) {
            String username = AWSMobileClient.getInstance().getUsername();
            TextView nameTextView = findViewById(R.id.greetingTextView);
            nameTextView.setText("Hello " + username + "!");
            Log.i(TAG, "fisher.signin" + username);


            subscribe();
        }
    }

    // Query all teams in DynamoDB to fill spinner
    public void queryAllTeams() {
        awsAppSyncClient.query(ListTeamsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(getAllTeamsCallback);
    }

    // Query team tasks in DynamoDB to fill RecyclerView
    public void queryForAllTeams() {
        awsAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(getAllTasksCallback);
    }

    public void querySelectedTeamTasks() {
        GetTeamQuery getTeamTasks = GetTeamQuery.builder().id(selectedTeamItem.id()).build();
        awsAppSyncClient.query(getTeamTasks)
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(getAllTeamTasksCallback);
    }

    public void redirectToTaskDetailPage(Task task) {
        Intent taskDetailIntent = new Intent(this, TaskDetails.class);
        taskDetailIntent.putExtra("title", "" + task.getTitle());
        taskDetailIntent.putExtra("description", "" + task.getBody());
        taskDetailIntent.putExtra("state", "" + task.getState());
        startActivity(taskDetailIntent);
    }

    private final GraphQLCall.Callback<ListTasksQuery.Data> getAllTasksCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
            Log.i("Results", response.data().listTasks().items().toString());
            Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessageToMain) {

                    List<ListTasksQuery.Item> items = response.data().listTasks().items();
                    tasks.clear();
                    for (ListTasksQuery.Item item : items) {
                        tasks.add(new Task(item.name(), item.description()));
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();
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

    private final GraphQLCall.Callback<GetTeamQuery.Data> getAllTeamTasksCallback = new GraphQLCall.Callback<GetTeamQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetTeamQuery.Data> response) {
            Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage (Message inputMessageToMain) {
                    List<GetTeamQuery.Item> selectedTeamTasksList = response.data().getTeam().tasks().items();
                    tasks.clear();
                    for (GetTeamQuery.Item item : selectedTeamTasksList) {
                        tasks.add(new Task(item.name(), item.description()));
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            };
            handlerForMainThread.obtainMessage().sendToTarget();
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {

        }
    };

    public void taskSelected(Task task) {
        Intent goToTaskDetailsPageActivityIntent = new Intent(this, TaskDetails.class);

        // Add info about what task is being checked
        goToTaskDetailsPageActivityIntent.putExtra("taskTitle", task.getTitle());
        goToTaskDetailsPageActivityIntent.putExtra("taskBody", task.getBody());
        Log.i(TAG, "inside taskSelected trying to move to Task Title " + task.getTitle());
        MainActivity.this.startActivity(goToTaskDetailsPageActivityIntent);
    }

    public void onSignOutButtonClick(View view) {
        AWSMobileClient.getInstance().signOut();
        String username = AWSMobileClient.getInstance().getUsername();
        TextView hiView = findViewById(R.id.greetingTextView);
        hiView.setText("Bye!");
        Log.i(TAG, "Logging out" + AWSMobileClient.getInstance().currentUserState());
    }

    abstract class LogDataWhenItComesBackCallback implements Callback {

        MainActivity mainActivityInstance;

        public LogDataWhenItComesBackCallback(MainActivity mainActivityInstance) {
            this.mainActivityInstance = mainActivityInstance;
        }

        private static final String TAG = "fisher.Callback";

    }

    public GraphQLCall.Callback<ListTeamsQuery.Data> getAllTeamsCallback = new GraphQLCall.Callback<ListTeamsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final com.apollographql.apollo.api.Response<ListTeamsQuery.Data> response) {

            Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessage) {
                    teamItems.clear();
                    teamItems.addAll(response.data().listTeams().items());

                    List<String> teamNames = new LinkedList<>();
                    for (ListTeamsQuery.Item item : teamItems) {
                        teamNames.add(item.name());
                    }

                    Spinner spinner = findViewById(R.id.teamSpinner);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, teamNames);
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(MainActivity.this);
                }
            };

            handlerForMainThread.obtainMessage().sendToTarget();
        }

        @Override
        public void onFailure(@Nonnull ApolloException error) {
            Log.e(TAG, error.getMessage());
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedTeamItem = teamItems.get(position);
        //Once we have the selected team we need to render that teams tasks
        querySelectedTeamTasks();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static PinpointManager getPinpointManager(final Context applicationContext) {
        if (pinpointManager == null) {
            final AWSConfiguration awsConfig = new AWSConfiguration(applicationContext);
            AWSMobileClient.getInstance().initialize(applicationContext, awsConfig, new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails result) {

                }

                @Override
                public void onError(Exception e) {

                }
            });

            PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                    applicationContext,
                    AWSMobileClient.getInstance(),
                    awsConfig);

            pinpointManager = new PinpointManager(pinpointConfig);

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }
                            final String token = task.getResult().getToken();
                            Log.d(TAG, "Registering push notifications token: " + token);
                            pinpointManager.getNotificationClient().registerDeviceToken(token);
                        }

                    });
        }
        return pinpointManager;
    }

    private AppSyncSubscriptionCall subscriptionWatcher;

    private void subscribe(){
        OnCreateTaskSubscription subscription = OnCreateTaskSubscription.builder().build();
        subscriptionWatcher = awsAppSyncClient.subscribe(subscription);
        subscriptionWatcher.execute(subCallback);
    }

    private AppSyncSubscriptionCall.Callback subCallback = new AppSyncSubscriptionCall.Callback() {
        @Override
        public void onResponse(@Nonnull Response response) {
            Log.i("Response", "Received subscription notification: " + response.data().toString());

            // Update UI with the newly added item
            OnCreateTaskSubscription.OnCreateTask data = ((OnCreateTaskSubscription.Data)response.data()).onCreateTask();

            // team needs to be a new team
//            final ListTasksQuery.Item addedItem = new ListTasksQuery.Item(data.__typename(), data.id(), data.name(), data.description(), data.taskState(), );

            // NOTES: you are accessing variables that do not exist here
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mTasks.add(addedItem);
//                    mAdapter.notifyItemInserted(mPets.size() - 1);
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }

        @Override
        public void onCompleted() {
            Log.i("Completed", "Subscription completed");
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
//        subscriptionWatcher.cancel();
    }

}