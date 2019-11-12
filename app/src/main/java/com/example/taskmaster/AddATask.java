package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
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
import com.apollographql.apollo.exception.ApolloException;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import type.CreateTaskInput;
import type.TaskState;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class AddATask extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "fisher.AddATaskActivity";
    private static final int READ_REQUEST_CODE = 42;
    private EditText inputTaskTitle;
    private EditText inputTaskDescription;
    static AWSAppSyncClient awsAppSyncClient;
    List<Team> teams;
    Team selectedTeam;
    private RadioGroup radioTaskStateGroup;
    private RadioButton radioTaskStateButton;
    private String filePath;
    TransferUtility transferUtility;
    Uri fileUri;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_task);

        // Connect with AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        String[] permissions = {READ_EXTERNAL_STORAGE};
        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));
        ActivityCompat.requestPermissions(this, permissions, 1);

        this.teams = new LinkedList<>();

        queryAllTeams();

        // Get the intent that started this activity
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Uri data = intent.getData();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
//            } else if (type.startsWith("image/")) {
//                handleSendImage(intent); // Handle single image being sent
//            }
//        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
//            if (type.startsWith("image/")) {
//                handleSendMultipleImages(intent); // Handle multiple images being sent
//            }
//        } else {
//            // Handle other intents, such as being started from the home screen
            }

        }
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            String textData = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            EditText sharedTaskDescription = findViewById(R.id.taskDescriptionInput);
            sharedTaskDescription.setText(textData);
        }
    }

    // Callback for inserting a new task
    public GraphQLCall.Callback<CreateTaskMutation.Data> addTaskCallBack = new GraphQLCall.Callback<CreateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull com.apollographql.apollo.api.Response<CreateTaskMutation.Data> response) {
            Log.i(TAG, "Saved task to team");
            finish();
        }

        @Override
        public void onFailure(@Nonnull ApolloException error) {
            Log.e(TAG, error.getMessage());
        }
    };

    // Query all teams in DynamoDB
    public void queryAllTeams() {
        awsAppSyncClient.query(ListTeamsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(getAllTeamsCallback);
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

                key = "public/" + UUID.randomUUID().toString();
                TransferObserver uploadObserver =
                        transferUtility.upload(
                                // filename in the cloud
                                key,
                                new File(this.filePath));

                // Attach a listener to the observer to get state update and progress notifications
                uploadObserver.setTransferListener(new TransferListener() {

                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (TransferState.COMPLETED == state) {
                            Log.i(TAG, "Transfer completed");
                            //Add toast when uploaded
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
                        Log.e(TAG, ex.getMessage());
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

    public void runAddTaskMutation(View view) {

        EditText inputTaskTitle = findViewById(R.id.taskTitleInput);
        EditText inputTaskDescription = findViewById(R.id.taskDescriptionInput);
//        EditText inputTaskState = findViewById(R.id.rad);

        CreateTaskInput createTaskInput = CreateTaskInput.builder()
                .name(inputTaskTitle.getText().toString())
                .description(inputTaskDescription.getText().toString())
                .taskTeamId(selectedTeam.getId())
                .taskState(TaskState.NEW)
                .build();

        awsAppSyncClient.mutate(CreateTaskMutation.builder().input(createTaskInput).build())
                .enqueue(addTaskCallBack);

    }

//    public void addListenerOnButton() {
//
//        radioTaskStateGroup = (RadioGroup) findViewById(R.id.radioTaskState);
//        btnDisplay = (Button) findViewById(R.id.btnDisplay);
//
//        btnDisplay.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                // get selected radio button from radioGroup
//                int selectedId = radioTaskStateGroup.getCheckedRadioButtonId();
//
//                // find the radiobutton by returned id
//                radioTaskStateButton = findViewById(selectedId);
//
//            }
//        });
//    }

    public GraphQLCall.Callback<ListTeamsQuery.Data> getAllTeamsCallback = new GraphQLCall.Callback<ListTeamsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final com.apollographql.apollo.api.Response<ListTeamsQuery.Data> response) {

            Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessage) {
                    teams.clear();
                    List<ListTeamsQuery.Item> teamItems = new LinkedList<>();
                    teamItems.addAll(response.data().listTeams().items());

                    for(ListTeamsQuery.Item item: teamItems) {
                        teams.add(new Team(item));
                    }

                    Spinner spinner =  findViewById(R.id.spinnerAddTaskToTeam);
                    ArrayAdapter<Team> adapter = new ArrayAdapter<>(AddATask.this, android.R.layout.simple_spinner_item, teams);
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(AddATask.this);
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
        selectedTeam = teams.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}