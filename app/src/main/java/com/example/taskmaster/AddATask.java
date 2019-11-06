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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.exception.ApolloException;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import type.CreateTaskInput;
import type.CreateTeamInput;
import type.TaskState;
import static android.widget.Toast.*;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.taskmaster.R.string.submit_confirmation;

public class AddATask extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "fisher.AddATaskActivity";

    private EditText inputTaskTitle;
    private EditText inputTaskDescription;
    static AWSAppSyncClient awsAppSyncClient;
    List<Team> teams;
    Team selectedTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_task);

        // Connect with AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        this.teams = new LinkedList<>();

        queryAllTeams();

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

    public void runAddTaskMutation(View view) {

        EditText inputTaskTitle = findViewById(R.id.taskTitleInput);
        EditText inputTaskDescription = findViewById(R.id.taskDescriptionInput);

        CreateTaskInput createTaskInput = CreateTaskInput.builder()
                .name(inputTaskTitle.getText().toString())
                .description(inputTaskDescription.getText().toString())
                .taskTeamId(selectedTeam.getId())
                .build();

        awsAppSyncClient.mutate(CreateTaskMutation.builder().input(createTaskInput).build())
                .enqueue(addTaskCallBack);

    }

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