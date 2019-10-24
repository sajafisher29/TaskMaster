package com.example.taskmaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.TaskViewHolder> {

    public List<Task> tasks;
    private OnTaskInteractionListener listener;

    public ViewAdapter(List<Task> tasks, OnTaskInteractionListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        Task task;
        TextView taskTitleView;
        TextView taskBodyView;
        TextView taskStateView;

        public TaskViewHolder(@NonNull View taskView) {
            super(taskView);
            this.taskTitleView = taskView.findViewById(R.id.title);
            this.taskBodyView = taskView.findViewById(R.id.body);
        }
    }

    // RecyclerView needs a new row for holding data
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_task, parent, false);
        final TaskViewHolder holder = new TaskViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.taskSelected(holder.task);
            }
        });
        return holder;
    }

    // Recycler view has a row that needs to be updated for a particular location/index
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task taskAtPosition = this.tasks.get(position);
        holder.task = taskAtPosition;
        holder.taskTitleView.setText(taskAtPosition.getTitle());
        holder.taskBodyView.setText(taskAtPosition.getBody());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public int getTaskCount() {
        return this.tasks.size();
    }

    // Make sure the adapter can community with any Activity it is a part of that implements this interface
    public static interface OnTaskInteractionListener {
        public void taskSelected(Task task);
    }
}
