package com.example.taskmaster;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.GetTeamQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import okhttp3.Callback;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskInteractionListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "fisher.MainActivity";

    private static final int READ_REQUEST_CODE = 42;
    public List<Task> tasks;
    RecyclerView recyclerView;
    AWSAppSyncClient awsAppSyncClient;
    private RecyclerView.Adapter taskAdapter;
    List<ListTeamsQuery.Item> teamItems;  //originally teams
    ListTeamsQuery.Item selectedTeamItem; //originally selectedTeam

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

        this.tasks = new LinkedList<>();
        this.teamItems = new LinkedList<>();

        String[] permissions = {READ_EXTERNAL_STORAGE};

        queryAllTeams();

        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));
        ActivityCompat.requestPermissions(this, permissions, 1);

        setContentView(R.layout.activity_main);

//        Button signInButton = findViewById(R.id.signInButton);
//        signInButton.setOnClickListener((event) -> {
//            // Add the sign in
//            // 'this' refers the the current active activity, probably replace with MainActivity.this
//            AWSMobileClient.getInstance().showSignIn(MainActivity.this, SignInUIOptions.builder().build(), new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
//                @Override
//                public void onResult(UserStateDetails result) {
//                    Log.d(TAG, "onResult: " + result.getUserState());
//                    if (result.getUserState().equals(UserState.SIGNED_IN)) {
//                        String username = AWSMobileClient.getInstance().getUsername();
//                        TextView hiView = findViewById(R.id.greetingTextView);
//                        hiView.setText("Hello " + username + "!");
//                    }
//                }
//
//                @Override
//                public void onError(Exception error) {
//                    Log.e(TAG, "onError: ", error);
//                }
//            });
//        });

//        Button signOutButton = findViewById(R.id.signOutButton);
//        signOutButton.setOnClickListener((event) -> {
////            @Override
////            public void onClick (View event){
////                String username = AWSMobileClient.getInstance().getUsername();
////
////                AWSMobileClient.getInstance().signOut();
////
////                TextView hiView = findViewById(R.id.greetingTextView);
////                hiView.setText("Bye " + username + "!");
////                AWSMobileClient.getInstance().signOut();
////            }
//        });

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

    }

    @Override
    public void onResume() {
        super.onResume();
        if (AWSMobileClient.getInstance().isSignedIn()) {
            String username = AWSMobileClient.getInstance().getUsername();
            TextView nameTextView = findViewById(R.id.greetingTextView);
            nameTextView.setText("Hello " + username + "!");
            Log.i("fisher.signin", username);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                // actually get path from URI
                Uri selectedImage = uri;
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                TransferUtility transferUtility =
                        TransferUtility.builder()
                                .context(getApplicationContext())
                                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                                .build();
                TransferObserver uploadObserver =
                        transferUtility.upload(
                                // filename in the cloud
                                "public/picolas",
                                new File(picturePath));

                // Attach a listener to the observer to get state update and progress notifications
                uploadObserver.setTransferListener(new TransferListener() {

                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (TransferState.COMPLETED == state) {
                            // Handle a completed upload.
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                        int percentDone = (int) percentDonef;

                        Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                                + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        // Handle errors
                    }

                });
            }
        }
    }

    public void pickFile(View v) {
        //https://developer.android.com/guide/topics/providers/document-provider
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, READ_REQUEST_CODE);
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

}