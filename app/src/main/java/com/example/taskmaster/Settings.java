package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.amazonaws.amplify.generated.graphql.CreateTeamMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import javax.annotation.Nonnull;
import type.CreateTeamInput;

public class Settings extends AppCompatActivity {

    private static final String TAG = "fisher.SettingsAndAddTeam";
    AWSAppSyncClient awsAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();
    }

    public void onSaveUsernameButtonClicked(View view) {
        // Save the username in SharedPreferences
        EditText nameEditText = findViewById(R.id.usernameInput);
        String name = nameEditText.getText().toString();
        // Grab the SharedPreference in which to save the username data
        SharedPreferences usernameSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Save the data to SharedPreferences
        SharedPreferences.Editor editor = usernameSharedPreferences.edit();
        editor.putString("username", name);
        editor.apply();
        finish();
    }

    public void createdNewTeam(View view) {
        Toast toast = Toast.makeText(this, R.string.submit_confirmation, Toast.LENGTH_SHORT);
        toast.show();

        EditText teamNameEditText = findViewById(R.id.teamNameInput);
        runAddATeamMutation(teamNameEditText.getText().toString());
    }

    // Add new team mutation
    public void runAddATeamMutation(String teamName) {
        CreateTeamInput createTeamInput = CreateTeamInput.builder()
                .name(teamName)
                .build();

        awsAppSyncClient.mutate(CreateTeamMutation.builder().input(createTeamInput).build())
                .enqueue(addATeamCallBack);
    }

    public GraphQLCall.Callback<CreateTeamMutation.Data> addATeamCallBack = new GraphQLCall.Callback<CreateTeamMutation.Data>() {
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