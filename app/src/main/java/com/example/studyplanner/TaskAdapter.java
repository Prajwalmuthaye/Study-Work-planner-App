package com.example.studyplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public void updateTasks(List<Task> filtered) {
        tasks.clear();
        tasks.addAll(filtered);
        notifyDataSetChanged();
    }

    public interface OnTaskUpdatedListener {
        void onTaskUpdated();
    }

    private List<Task> tasks;
    private TaskDatabaseHelper db;
    private OnTaskUpdatedListener listener;
    private Context context;

    public TaskAdapter(Context context, List<Task> tasks, TaskDatabaseHelper db, OnTaskUpdatedListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.db = db;
        this.listener = listener;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public TextView title, category;
        public CheckBox done;
        public ImageButton edit, delete;

        public TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            category = itemView.findViewById(R.id.taskCategory);
            done = itemView.findViewById(R.id.taskDone);
            edit = itemView.findViewById(R.id.editTask);
            delete = itemView.findViewById(R.id.deleteTask);
        }
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.title.setText(task.getTitle());
        holder.category.setText(task.getCategory());
        holder.done.setChecked(task.isDone());


        holder.done.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setDone(isChecked);
            db.updateTask(task);
            listener.onTaskUpdated();
        });

        holder.delete.setOnClickListener(v -> {
            if (position != RecyclerView.NO_POSITION) {
                db.deleteTask(task.getId());
                tasks.remove(position);
                notifyItemRemoved(position);
                listener.onTaskUpdated();
            }
        });

        holder.edit.setOnClickListener(v -> showEditDialog(holder, task));
    }

    private void showEditDialog(TaskViewHolder holder, Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Task");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null);
        EditText inputTitle = dialogView.findViewById(R.id.editTaskTitle);
        Spinner spinner = dialogView.findViewById(R.id.spinnerCategory);

        inputTitle.setText(task.getTitle());

        // Set up Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.task_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set current category selection
        int categoryPosition = adapter.getPosition(task.getCategory());
        if (categoryPosition >= 0) {
            spinner.setSelection(categoryPosition);
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newTitle = inputTitle.getText().toString().trim();
            String newCategory = spinner.getSelectedItem().toString();

            if (!newTitle.isEmpty()) {
                task.setTitle(newTitle);
                task.setCategory(newCategory);
                db.updateTask(task);
                notifyItemChanged(holder.getAdapterPosition());
                listener.onTaskUpdated();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}
