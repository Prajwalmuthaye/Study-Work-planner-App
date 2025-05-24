package com.example.studyplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private TaskDatabaseHelper db;
    private TaskAdapter adapter;
    private List<Task> taskList;

    private ProgressBar progressBar;
    private TextView progressText;
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new TaskDatabaseHelper(this);
        taskList = db.getAllTasks();

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        filterSpinner = findViewById(R.id.filterSpinner);

        adapter = new TaskAdapter(this, taskList, db, this::updateProgress);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupFilterSpinner();
        updateProgress();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddTaskDialog());
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(categoryAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                filterTasks(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void filterTasks(String category) {
        List<Task> filtered;

        if (category.equalsIgnoreCase("All")) {
            filtered = db.getAllTasks();
        } else {
            filtered = new ArrayList<>();
            for (Task t : db.getAllTasks()) {
                if (t.getCategory().equalsIgnoreCase(category)) {
                    filtered.add(t);
                }
            }
        }

        adapter.updateTasks(filtered);
        updateProgress();
    }

    private void updateProgress() {
        List<Task> tasks = db.getAllTasks();
        int total = tasks.size();
        int completed = 0;
        for (Task t : tasks) {
            if (t.isDone()) completed++;
        }

        int progress = total == 0 ? 0 : (completed * 100 / total);
        progressBar.setProgress(progress);
        progressText.setText("Progress: " + progress + "%");
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Task");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        EditText editTitle = dialogView.findViewById(R.id.editTaskTitle);
        Spinner spinner = dialogView.findViewById(R.id.spinnerCategory);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(categoryAdapter);

        builder.setView(dialogView);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = editTitle.getText().toString().trim();
            String category = spinner.getSelectedItem().toString();

            if (!title.isEmpty()) {
                Task newTask = new Task(0, title, false, category);
                db.insertTask(newTask);
                taskList.add(newTask);
                adapter.notifyItemInserted(taskList.size() - 1);
                updateProgress();

                // Set reminder 1 min later as example
                long futureTimeMillis = System.currentTimeMillis() + 60000;
                setAlarm(this, futureTimeMillis, title);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void setAlarm(Context context, long triggerAtMillis, String taskTitle) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("taskTitle", taskTitle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }
}
