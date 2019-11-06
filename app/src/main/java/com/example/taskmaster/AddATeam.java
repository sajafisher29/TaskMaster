package com.example.taskmaster;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

public class AddATeam extends AppCompatActivity {

    private static final String TAG = "fisher.AddATeam";
    private EditText teamNameInput;
    AWSAppSyncClient awsAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout. ); //Add a team layout

        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        teamNameInput = findViewById(R.id. ); // Team Name input
    }

    public void createdNewTeam(View view) {
        Toast toast = Toast.makeText(this, R.string.submit_confirmation, Toast.LENGTH_SHORT);
        toast.show();

        runAddATeamMutation(teamNameInput.getText().toString());
    }

    // Add new team mutation
    public void runAddATeamMutation(String teamName) {
        CreateTeamInput createTeamInput = CreateTeamInput.builder()
                .name(teamName)
                .build();

        awsAppSyncClient.mutate(CreateTeamMutation.builder().input(createTeamInput).build())
                .enqueue(addATeamCallBack);
    }

    public GraphQLCall.Callback<CreateTeamMutation.Data> addTeamCallBack = new GraphQLCall.Callback<CreateTeamMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateTeamMutation.Data> response) {
            Log.i(TAG, "Successfully added a team");
            finish();
        }

        @Override
        public void onFailure(@Nonnull ApolloException error) {
            Log.e(TAG, error.getMessage());
        }
    };

}
