package gr.edu.todolizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {


  private static final String TAG = "DatabaseHelper";
  private static DatabaseHelper sInstance;
  private static Bus bus;

  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "task.db";

  private static final String TABLE_TASKS = "Task";

  private static final String TASK_KEY_ID = "taskID";
  private static final String TASK_TITLE = "title";
  private static final String TASK_DESCRIPTION = "description";
  private static final String TASK_DUE_DATE = "due_date";
  private static final String TASK_REMIDNER = "reminder";
  private static final String TASK_CHECKBOX_IS_CHECKED = "checkboxChecked";
  private static final String TASK_ORGANIZER = "organizer";



  private static final int COLUMN_KEY_ID = 0;
  private static final int COLUMN_TITLE = 1;
  private static final int COLUMN_DESCRIPTION = 2;
  private static final int COLUMN_DUE_DATE = 3;
  private static final int COLUMN_REMINDER = 4;
  private static final int COLUMN_CHECKBOX_IS_CHECKED = 5;
  private static final int COLUMN_ORGANIZER = 6;


  //Singleton
  public static synchronized DatabaseHelper getInstance(Context context) {
     
    // Use the application context, which will ensure that you
    // don't accidentally leak an Activity's context
    if (sInstance == null) {
      sInstance = new DatabaseHelper(context.getApplicationContext());
      bus = GlobalBus.getInstance();

    }
    return sInstance;
  }
    
  /**
   * Constructor should be private to prevent direct instantiation.
   * make call to static method "getInstance()" instead.
   */
  private DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  // Called when the database connection is being configured.
  // Configure database settings for things like foreign key support, write-ahead logging, etc.
  /*
  @Override
  public void onConfigure(SQLiteDatabase db) {
    super.onConfigure(db);
    db.setForeignKeyConstraintsEnabled(true);
  }
  */

  @Override
  public void onCreate(SQLiteDatabase db) {
    String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS +
            "(" +
            TASK_KEY_ID + " INTEGER PRIMARY KEY," + // Define a primary key
            //KEY_POST_USER_ID_FK + " INTEGER REFERENCES " + TABLE_USERS + "," + // Define a foreign key
            TASK_TITLE + " TEXT NOT NULL," +
            TASK_DESCRIPTION + " TEXT NOT NULL DEFAULT ''," +
            TASK_DUE_DATE + " INTEGER NOT NULL," +
            TASK_REMIDNER + " INTEGER NOT NULL DEFAULT 0," +
            TASK_CHECKBOX_IS_CHECKED + " INTEGER NOT NULL DEFAULT 0 CHECK(checkboxChecked == 0 OR checkboxChecked ==1), " +
            TASK_ORGANIZER + " TEXT NOT NULL DEFAULT 'Local' "+
            ")";



    db.execSQL(CREATE_TASKS_TABLE);

  }

  // Called when the database needs to be upgraded.
  // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
  // but the DATABASE_VERSION is different than the version of the database that exists on disk.
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion != newVersion) {
      // Simplest implementation is to drop all old tables and recreate them
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
      onCreate(db);
    }
  }

  // Insert a task into the database
  public int addTask(Task task) {
    // Create and/or open the database for writing
    SQLiteDatabase db = getWritableDatabase();
    int taskID = -1;
    // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
    // consistency of the database.
    db.beginTransaction();
    try {
      // The user might already exist in the database (i.e. the same user created multiple posts).
      //long userId = addOrUpdateUser(post.user);

      ContentValues values = new ContentValues();
      values.put(TASK_TITLE, task.getTitle());
      values.put(TASK_DESCRIPTION, task.getDescription());
      values.put(TASK_DUE_DATE, task.getDtStart());
      values.put(TASK_REMIDNER, task.getReminder());
      values.put(TASK_CHECKBOX_IS_CHECKED, task.getCheckboxChecked());
      values.put(TASK_ORGANIZER,task.getOrganizer());


      // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
      long rowID = db.insertOrThrow(TABLE_TASKS, null, values);

      //Log.d("rowID: ", String.valueOf(rowID));


      String readTaskID = String.format("SELECT * from Task WHERE rowID == %d", rowID);
      Cursor cursor = db.rawQuery(readTaskID,null);

      //Log.d(TAG, task.getDescription());
      cursor.moveToNext();
      //Log.d("row cursor pos" , String.valueOf(cursor.getPosition()));
      //Log.d("row cursor " , String.valueOf(cursor.getInt(0)));
      //Log.d("THIS IS THE ID" , cursor.getString(1));
      taskID = cursor.getInt(0);

      cursor.close();




      db.setTransactionSuccessful();
    } catch (Exception e) {
      Log.d(TAG, "Error while trying to add post to database");
      Log.d(TAG,e.toString());
    } finally {
      db.endTransaction();

    }
    return taskID;
  }

  public void populateDB(){

      SQLiteDatabase db = getWritableDatabase();
      db.beginTransaction();
      try {



          for (int i = 1; i < 50; i++) {


              ContentValues values = new ContentValues();
              String title = String.format("Title %d", i);
              values.put(TASK_TITLE, title);
              String description = String.format("Description %d", i);
              values.put(TASK_DESCRIPTION, description);
              values.put(TASK_DUE_DATE, System.currentTimeMillis());
              values.put(TASK_REMIDNER, 0);
              values.put(TASK_CHECKBOX_IS_CHECKED, 0);
              values.put(TASK_ORGANIZER, "Local");



              // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
              db.insertOrThrow(TABLE_TASKS, null, values);
          }
          db.setTransactionSuccessful();
      }
      catch(Exception e){
        Log.d(TAG,e.toString());
      }
      finally {
          db.endTransaction();
      }
    }

  public ArrayList<Task> readTasks(){
    ArrayList<Task> taskArrayList = new ArrayList<>();

    String TASKS_SELECT_QUERY = String.format("SELECT * FROM %s", TABLE_TASKS);

    SQLiteDatabase db = getReadableDatabase();

    Cursor cursor = db.rawQuery(TASKS_SELECT_QUERY, null);

    Log.d("NUMBERS", "Reading tasks on DatabaseHelper");
    //Log.d(TAG, "Let's read");
    while(cursor.moveToNext()){
      int taskID = cursor.getInt(COLUMN_KEY_ID);
      String title = cursor.getString(COLUMN_TITLE);
      String description = cursor.getString(COLUMN_DESCRIPTION);
      long due_date = cursor.getLong(COLUMN_DUE_DATE);

      long reminder = cursor.getLong(COLUMN_REMINDER);

      int checkboxChecked = cursor.getInt(COLUMN_CHECKBOX_IS_CHECKED);
      String organizer = cursor.getString(COLUMN_ORGANIZER);

      Task task = new Task(taskID,title,description,due_date,reminder,organizer,checkboxChecked);
      Log.d("NUMBERS", "dbhelper.readtasks(), taskID: " + taskID + ", due_date: " + due_date + ", reminder: " + reminder);
      taskArrayList.add(task);
      task.setTaskID(taskID);
    }
    cursor.close();
    db.close();

    //Log.d(TAG, String.valueOf(taskArrayList.isEmpty()));

    return taskArrayList;
  }

  /*
  public void setTaskCheckboxIsChecked(Task task){

  }

   */

  public void updateCheckbox(int taskID, boolean isChecked) {
    String updateString = String.format("UPDATE TASK SET checkboxChecked =%d WHERE taskID==%d",isChecked ? 1: 0,taskID);
    SQLiteDatabase db = getWritableDatabase();

    db.beginTransaction();
    try{

      db.execSQL(updateString);
      db.setTransactionSuccessful();
    } catch (Exception e) {
      Log.d(TAG, "Error while trying to update checkbox in database");
      Log.d(TAG,e.toString());
    } finally {
      db.endTransaction();

    }
    String s = String.format("Checkbox %d update successful, new state is: %s",taskID,String.valueOf(isChecked ? 1:0));

    Log.d("checkboxTAG",s);
      Log.d("checkboxTAG"," ");

  }

  public void deleteTask(int taskID) {
    String deleteString = String.format("DELETE FROM %s where %s == %d",TABLE_TASKS,TASK_KEY_ID,taskID);
    SQLiteDatabase db = sInstance.getWritableDatabase();
    db.beginTransaction();
    try{
      db.execSQL(deleteString);
      db.setTransactionSuccessful();

    }catch (Exception e){
      Log.d(TAG,"Error while trying to delete task with ID: "+taskID);
    }
    finally {
      db.endTransaction();
    }

  }

  public boolean lookForTaskID(int taskID) {
    SQLiteDatabase db = sInstance.getReadableDatabase();
    String lookForTaskID_QUERRY = String.format("SELECT EXISTS(SELECT %s FROM %s WHERE %s==%d)",
            TASK_KEY_ID,TABLE_TASKS,TASK_KEY_ID,taskID);

    int result = 1;
    try {
      Cursor cursor = db.rawQuery(lookForTaskID_QUERRY,null);
      if(cursor.moveToNext()){
        result = cursor.getInt(0);
      }

      db.setTransactionSuccessful();
        cursor.close();
    } catch (Exception e) {
      Log.d(TAG, "Error while trying to search for taskID: " + taskID);
    } finally {
      db.endTransaction();

    }

      return result == 1;
  }

  public Bus getBus(){
    return bus;
  }

  public void updateDescription(int taskID, String description) {
    updateString(taskID,description,TASK_DESCRIPTION);
  }

  public void updateTitle(int taskID, String changedTitle) {
    updateString(taskID, changedTitle, TASK_TITLE);
  }

  private void updateString(int taskID, String changedString, String columnName){
    String updateString = String.format("UPDATE %s SET %s ='%s' WHERE taskID==%d",TABLE_TASKS,columnName,changedString,taskID);
    SQLiteDatabase db = getWritableDatabase();
    Log.d("Transaction",updateString);
    db.beginTransaction();
    try{

      db.execSQL(updateString);
      db.setTransactionSuccessful();
      Log.d("Transaction","Success");
      Log.d("Transaction",updateString);
    } catch (Exception e) {
      Log.d(TAG, "Error while trying to update " + columnName + " in database");
      Log.d(TAG,e.toString());
    } finally {
      db.endTransaction();

    }
    String s = String.format(columnName + "  with TaskID: %d, update successful",taskID);
  }
}
