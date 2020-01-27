package gr.edu.todolizer;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.net.Uri;

import android.provider.CalendarContract;
import android.util.Log;

import android.widget.Button;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_READ_CALENDAR = 1;
    private static boolean READ_CALENDAR_GRANTED = false;

    private static String CALENDAR_ACCOUNT_NAME;
    private static int CALENDAR_POS = -1;
    private static long CALENDAR_ID = -1;

    private static final int ADD_NEW_TASK = 0;
    private static final String TAG = "MainActivity";
    private static final String TAG_CALENDAR = "CALENDAR";

    private ArrayList<Task> taskArrayList;
    private TaskListAdapter adapter;


    private DatabaseHelper dbHelper;
    private Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Purge database
        //getBaseContext().deleteDatabase("task.db");

        dbHelper = DatabaseHelper.getInstance(this);
        //dbHelper.populateDB();

        RecyclerView recyclerView = findViewById(R.id.taskListView);
        taskArrayList = new ArrayList<>();



        taskArrayList = dbHelper.readTasks();

        adapter = new TaskListAdapter(this,taskArrayList);
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bus = GlobalBus.getInstance();

        final Button readFromCalendar = findViewById(R.id.checkboxbtn);
        //readEventsFromCalendar.setText("Load from Calendar");
        readFromCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Step 1: Check read calendar permission. If it does not exist, request it
                    Step 2: If it exists or if you get them in onRequestPermissionResult, readDeviceCalendars
                    to read all the calendars in the device
                    Step 3: chooseCalendar() to show the corresponding AlertDialogs for the user to choose which Calendar to load from
                    Step 4: If the user confirms his choice, readEventsFromCalendar to load the events using ContentProvider */

                loadFromCalendar();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                Log.d(TAG, "starting TaskActivity with intent");
                startActivityForResult(intent, ADD_NEW_TASK);
            }
        });
    }

    private void addTaskToAdapter(Task task){
        int taskID = dbHelper.addTask(task);

        if(taskID != -1){

            task.setTaskID(taskID);
            taskArrayList.add(0,task);
            adapter.notifyDataSetChanged();
        }
        else{
            Log.d(TAG,"Adding Task to Database failed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == ADD_NEW_TASK){
            if(resultCode == Activity.RESULT_OK){
                boolean hasDueDate = data.getBooleanExtra("due_date",false);
                boolean hasReminder = data.getBooleanExtra("reminder",false);

                String title = data.getStringExtra("title");
                String descr = data.getStringExtra("descr");

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                //Check if user added a due date. If no due date is, the current date will be added
                if(hasDueDate){
                    Bundle dateBundle = data.getBundleExtra("due_date_dateBundle");
                    int date_dayOfMonth = dateBundle.getInt("dayOfMonth");
                    int date_month = dateBundle.getInt("month");
                    int date_year = dateBundle.getInt("year");
                    calendar.set(date_year,date_month,date_dayOfMonth);
                }
                long dueDateInMillis = calendar.getTimeInMillis();


                //Reset the calendar date to today
                //Check if user added a Reminder date. If not, make it 0
                calendar.setTimeInMillis(System.currentTimeMillis());
                long reminderInMillis = 0;
                if(hasReminder){
                    Bundle dateBundle = data.getBundleExtra("reminder_dateBundle");
                    int time_dayOfMonth = dateBundle.getInt("dayOfMonth");
                    int time_month = dateBundle.getInt("month");
                    int time_year = dateBundle.getInt("year");
                    String date = time_dayOfMonth + "/" + (time_month) + "/" + time_year;

                    Bundle timeBundle = data.getBundleExtra(("reminder_timeBundle"));
                    int hour = timeBundle.getInt("hour");
                    int minute = timeBundle.getInt("minute");
                    calendar.set(time_year,time_month,time_dayOfMonth,hour,minute);
                    reminderInMillis = calendar.getTimeInMillis();
                    Log.d("REMINDER", "MainActivity Reminder " + reminderInMillis);
                }

                Task task = new Task(title,descr,dueDateInMillis,reminderInMillis);
                dbHelper.addTask(task);

                addTaskToAdapter(task);

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dbHelper.close();
        if(adapter != null) {
            bus.unregister(adapter);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /*
    private void printCheckboxStatus(){
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String s = String.format("SELECT taskID, checkboxChecked FROM TASK");
        Cursor cursor = db.rawQuery(s, null);

        while(cursor.moveToNext()){
            int taskID = cursor.getInt(0);
            int checkboxChecked = cursor.getInt(1);

            if(checkboxChecked == 1) {
                String ss = String.format("taskID: %d, checkboxChecked: %d", taskID, checkboxChecked);
            }
        }
    }

     */

    @SuppressLint("MissingPermission")
    private void readEventsFromCalendar() {

        Log.d("CALENDAR", "Checking permission");

        if(READ_CALENDAR_GRANTED){
            Log.d("CALENDAR", "Checking Calendar ID");
            if (CALENDAR_ID != -1) {

                Cursor cur;
                ContentResolver cr = getContentResolver();

                String[] mProjection =
                        {
                                "_id",
                                CalendarContract.Events.TITLE,
                                CalendarContract.Events.DESCRIPTION,
                                CalendarContract.Events.ORGANIZER,
                                CalendarContract.Events.DTSTART,
                        };

                String selection = CalendarContract.Events.ORGANIZER + " = ? ";
                String selectionArgs[] = new String[]{CALENDAR_ACCOUNT_NAME};

                Uri uri = CalendarContract.Events.CONTENT_URI;
                cur = cr.query(uri, mProjection, selection, selectionArgs, null);


                while(cur.moveToNext()){
                    String descr = cur.getString(cur.getColumnIndex(CalendarContract.Events.DESCRIPTION));
                    String title = cur.getString(cur.getColumnIndex(CalendarContract.Events.TITLE));
                    String organizer = cur.getString(cur.getColumnIndex(CalendarContract.Events.ORGANIZER));
                    long dtStart = cur.getLong((cur.getColumnIndex(CalendarContract.Events.DTSTART)));
                    Log.d("CALENDAR","Title: " + title);
                    Log.d("CALENDAR","Description: " + descr);
                    Log.d("CALENDAR","Organizer: " + organizer);
                    Log.d("CALENDAR","DTSTART: " + dtStart);
                    Task task = new Task(title,descr,dtStart,organizer);
                    //Avoid adding tasks that you have added before
                    if(!task.findOriginalID(taskArrayList)
                            && !task.getOrganizer().equals("Local")
                            && task.getDtStart() != dtStart)
                    {

                        addTaskToAdapter(task);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MainActivity.this, "No Calendar chosen", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void loadFromCalendar(){

        int hasReadCalendarPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);
        Log.d(TAG_CALENDAR, "HAS Read Contact Permissions "+hasReadCalendarPermission);

        if(hasReadCalendarPermission == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG_CALENDAR, "Permission already Granted");
            READ_CALENDAR_GRANTED = true;
            readDeviceCalendars();
        }
        else{
            Log.d(TAG_CALENDAR, "Requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, REQUEST_CODE_READ_CALENDAR);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],int[] grantResults){
        String TAG_CALENDAR = "CALENDAR";
        switch(requestCode) {
            case REQUEST_CODE_READ_CALENDAR: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    READ_CALENDAR_GRANTED = true;
                    readDeviceCalendars();
                }
                else{
                    Log.d(TAG_CALENDAR, "READ CALENDAR PERMISSION DENIED");
                }
            }
        }

    }


    private void chooseCalendar(final String[] displayNames, final long[] calendarIDs){


        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose a Calendar");

        // add a radio button list

        int checkedItem = 1; // cow
        builder.setSingleChoiceItems(displayNames, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("CALENDAR","Chose: "+which);
                // user checked an item
                CALENDAR_POS = which;
            }
        });

        // add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
                Log.d("CALENDAR","WHICH: "+ CALENDAR_POS);
                Log.d("CALENDAR","Chosen item " + displayNames[CALENDAR_POS]);
                CALENDAR_ACCOUNT_NAME = displayNames[CALENDAR_POS];
                CALENDAR_ID = calendarIDs[CALENDAR_POS];
                Log.d("CALENDAR","Chosen ID " +CALENDAR_ID);
                readEventsFromCalendar();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CALENDAR_POS = -1;
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void readDeviceCalendars(){
        String[] projection =
                new String[]{
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.ACCOUNT_NAME,
                        CalendarContract.Calendars.ACCOUNT_TYPE,
                        CalendarContract.Calendars.OWNER_ACCOUNT,
                        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
        @SuppressLint("MissingPermission") Cursor calCursor =
                getContentResolver().
                        query(CalendarContract.Calendars.CONTENT_URI,
                                projection,
                                CalendarContract.Calendars.VISIBLE + " = 1",
                                null,
                                CalendarContract.Calendars._ID + " ASC");
        if (calCursor.moveToFirst()) {
            ArrayList<String> stringArrayList = new ArrayList<>();
            ArrayList<Long> longArrayList = new ArrayList<>();
            do {
                long id = calCursor.getLong(0);
                String accountName = calCursor.getString(1);
                long accountType = calCursor.getLong(2);
                long ownerAccount = calCursor.getLong(3);
                String displayName = calCursor.getString(4);

                Log.d(TAG_CALENDAR, "ID: " + id);
                Log.d(TAG_CALENDAR, "Account Name: " + accountName);
                Log.d(TAG_CALENDAR, "Account Type: " + accountType);
                Log.d(TAG_CALENDAR, "Owner Type: " + ownerAccount);

                stringArrayList.add(displayName);
                longArrayList.add(id);
            } while (calCursor.moveToNext());

            Object[] src = stringArrayList.toArray();
            String[] stringList = new String[src.length];

            for (int i = 0; i < src.length; i++) {
                stringList[i] = src[i].toString();
            }
            Object[] srcID = longArrayList.toArray();
            long[] longList = new long[srcID.length];

            for (int i = 0; i < srcID.length; i++) {
                longList[i] = Long.parseLong(srcID[i].toString());
            }

            chooseCalendar(stringList, longList);
        }
    }




}
