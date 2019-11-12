package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.squareup.picasso.Picasso;

public class TaskDetails extends AppCompatActivity {

    private AWSAppSyncClient awsAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        String taskName = getIntent().getStringExtra("taskTitle");
        TextView tasktitle = findViewById(R.id.taskDetailsHeading);
        tasktitle.setText(taskName);

        String taskDescription = getIntent().getStringExtra("taskBody");
        TextView taskDesc = findViewById(R.id.taskDetailsDescription);
        taskDesc.setText(taskDescription);

        // Build a connection to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        String filePath = getIntent().getStringExtra("filePath");

        if (filePath != null && filePath.length() > 2) {
            ImageView taskImage = findViewById(R.id.mediaPlaceholder);
            Picasso.get().load(filePath).into(taskImage);

        }
    }
}
