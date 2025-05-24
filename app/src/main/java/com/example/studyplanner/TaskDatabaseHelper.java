package com.example.studyplanner;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.util.*;

public class TaskDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "tasks.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_NAME = "tasks";

    public TaskDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS = "CREATE TABLE tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "isDone INTEGER, " +      // 0 or 1 to represent completion
                "category TEXT)";
        db.execSQL(CREATE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
    }



    public void insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", task.getTitle());
        values.put("isDone", task.isDone() ? 1 : 0);
        values.put("category", task.getCategory());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<Task> getAllTasks() {

        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {

                Task task = new Task(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2) == 1,
                        cursor.getString(3)

                );
                list.add(task);


            } while (cursor.moveToNext());
        }



        cursor.close();
        db.close();
        return list;
    }

    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isDone", task.isDone() ? 1 : 0);
        values.put("category", task.getCategory());
        db.update(TABLE_NAME, values, "id=?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tasks", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }


}
