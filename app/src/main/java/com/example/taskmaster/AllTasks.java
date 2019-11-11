package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.List;

import javax.annotation.Nonnull;

public class AllTasks extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tasks);
    }

//    private final GraphQLCall.Callback<ListTasksQuery.Data> getAllTasksCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
//        @Override
//        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
//            Log.i("Results", response.data().listTasks().items().toString());
//            Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
//                @Override
//                public void handleMessage(Message inputMessageToMain) {
//
//                    List<ListTasksQuery.Item> items = response.data().listTasks().items();
//                    tasks.clear();
//                    for (ListTasksQuery.Item item : items) {
//                        tasks.add(new Task(item.name(), item.description()));
//                    }
//                    recyclerView.getAdapter().notifyDataSetChanged();
//                }
//            };
//            Message completeMessage = handlerForMainThread.obtainMessage(0, response);
//            completeMessage.sendToTarget();
//        }
//
//        @Override
//        public void onFailure(@Nonnull ApolloException error) {
//            Log.e("ERROR", error.toString());
//        }
//    };
}
