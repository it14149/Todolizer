package gr.edu.todolizer;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    private static long CALENDAR_ACCOUNT_TYPE = 0;
    private static int CALENDAR_POS = -1;
    private static long CALENDAR_ID = -1;

    private static final int ADD_NEW_TASK = 0;
    private static final String TAG = "MainActivity";
    private static final String TAG_CALENDAR = "CALENDAR";

    private Button addTaskBtn;
    private RecyclerView recyclerView;
    private ArrayList<Task> taskArrayList;
    private TaskListAdapter adapter;

    //private SQLiteDatabase db;

    private DatabaseHelper dbHelper;
    private Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //AccountManager am = AccountManager.get(this);
        //Account[] myAcc = am.getAccountsByType(null);
        //Log.d("START", myAcc.toString());

        //Purge database
        //getBaseContext().deleteDatabase("task.db");

        //This gets the Calendar ID that we want to read
        //loadFromCalendar();
        //readFromCalendar();

        dbHelper = DatabaseHelper.getInstance(this);



        //dbHelper.populateDB();


        //dbHelper.stuff();

        recyclerView = findViewById(R.id.taskListView);
        taskArrayList = new ArrayList<>();

        /*
        TODO: Use this code to print the checkbox status
        Button getCheckboxStatusBtn = findViewById(R.id.checkboxbtn);

        getCheckboxStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printCheckboxStatus();
            }
        });

         */

        final Button readFromCalendar = findViewById(R.id.checkboxbtn);
        //readFromCalendar.setText("Load from Calendar");
        readFromCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFromCalendar();
                //  readFromCalendar();
            }
        });


        //readDB();

        taskArrayList = dbHelper.readTasks();



        //adapter = new TaskListAdapter(this, R.layout.adapter_task_view_layout, taskArrayList);


        adapter = new TaskListAdapter(this,taskArrayList);
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //taskListView.setAdapter(adapter);

        //adapter.notifyDataSetChanged();

        bus = GlobalBus.getInstance();
//        bus.register(adapter);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    private void readFromCalendar() {
        Log.d("CALENDAR", "Checking permission");
        //if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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

                Log.d("CALENDAR", "Reading Events");
                while(cur.moveToNext()){
                    String descr = cur.getString(cur.getColumnIndex(CalendarContract.Events.DESCRIPTION));
                    String title = cur.getString(cur.getColumnIndex(CalendarContract.Events.TITLE));
                    String organizer = cur.getString(cur.getColumnIndex(CalendarContract.Events.ORGANIZER));
                    long dsStart = cur.getLong((cur.getColumnIndex(CalendarContract.Events.DTSTART)));
                    Log.d("CALENDAR","Title: " + title);
                    Log.d("CALENDAR","Description: " + descr);
                    Log.d("CALENDAR","Organizer: " + organizer);
                    Log.d("CALENDAR","DTSTART: " + String.valueOf(dsStart));
                    Task task = new Task(title,descr,dsStart,organizer);
                    //taskArrayList.add(0,task);
                    //Avoid adding tasks that you have added before
                    //TODO: Should check against all conditions, original_ID is not enough by itself
                    if(!task.findOriginalID(taskArrayList)){
                        if(!task.getOrganizer().equals("Local")) {



                            addTaskToAdapter(task);

                            //adapter.notifyItemChanged();
                        }
                    }


                }
                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(MainActivity.this, "No Calendar chosen", Toast.LENGTH_LONG).show();
            }
        }

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


                int date_dayOfMonth;
                int date_month;
                int date_year;


                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                if(hasDueDate){
                    Bundle dateBundle = data.getBundleExtra("due_date_dateBundle");
                    date_dayOfMonth = dateBundle.getInt("dayOfMonth");
                    date_month = dateBundle.getInt("month");
                    date_year = dateBundle.getInt("year");
                    calendar.set(date_year,date_month,date_dayOfMonth);
                }
                else{

                    date_dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    date_month = calendar.get(Calendar.MONTH) ;
                    date_year = calendar.get(Calendar.YEAR);
                }

                long dueDateInMillis = calendar.getTimeInMillis();

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


                //taskArrayList.add(new Task(title,descr,0,0,0));
                //Task task = new Task( title,descr,date_dayOfMonth,date_month,date_year, 0);
                Task task = new Task(title,descr,dueDateInMillis,reminderInMillis);
                int taskID = dbHelper.addTask(task);

                if(taskID != -1){
                    task.setTaskID(taskID);
                    taskArrayList.add(task);
                    adapter.notifyDataSetChanged();
                }
                else{
                    Log.d(TAG,"Adding Task to Database failed");
                }
            }
        }
    }

    /*
    private void readDB(DatabaseHelper dbHelper){
        SQLiteDatabase db = SQLiteDatabase.
        Cursor cursor = db.rawQuery("select * from Task",
                null);
        dbHelper.
        //StringBuilder sb = new StringBuilder();

        while(cursor.moveToNext()){
            String title = cursor.getString(0);
            String description = cursor.getString(1);
            int dayOfMonth = Integer.parseInt(cursor.getString(2));
            int month = Integer.parseInt(cursor.getString(3));
            int year = Integer.parseInt(cursor.getString(4));
            int checkboxChecked = Integer.parseInt(cursor.getString(5));
            taskArrayList.add(new Task(title,description,dayOfMonth,month,year,checkboxChecked));

        }
        cursor.close();

        //EditText txtDbOut = findViewById(R.id.taskListView);

        //txtDbOut.setText(sb.toString());
    } */

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
                Log.d("checkboxTAG", ss);
            }
        }
    }

    private void loadFromCalendar(){

        int hasReadCalendarPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);
        Log.d(TAG_CALENDAR, "HAS Read Contact Permissions "+hasReadCalendarPermission);

        if(hasReadCalendarPermission == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG_CALENDAR, "Permission already Granted");
            READ_CALENDAR_GRANTED = true;
            readEventsFromCalendar();
        }
        else{
            Log.d(TAG_CALENDAR, "Requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, REQUEST_CODE_READ_CALENDAR);
        }
    }

    private void chooseCalendar(final String[] displayNames,final long[] calendarIDs){


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
                readFromCalendar();
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

    private void stuff(){
        AccountManager am = AccountManager.get(MainActivity.this);
        Bundle options = new Bundle();

        /*
        String myAccount_ = am.getAccountsByType();

        am.getAuthToken(
                myAccount_,
                "Post Task to Google Calendar",
                options,
                MainActivity.this,
                new OnTokenRequired(),
                new Handler(new OnError()));
        )

         */
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],int[] grantResults){
        String TAG_CALENDAR = "CALENDAR";
        switch(requestCode) {
            case REQUEST_CODE_READ_CALENDAR: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    READ_CALENDAR_GRANTED = true;
                    readEventsFromCalendar();
                }
                else{
                    Log.d(TAG_CALENDAR, "READ CALENDAR PERMISSION DENIED");
                }
            }
        }

    }
    private void readEventsFromCalendar(){
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
                // ...
                Log.d(TAG_CALENDAR, "ID: " + String.valueOf(id));
                Log.d(TAG_CALENDAR, "Account Name: " + accountName);
                Log.d(TAG_CALENDAR, "Account Type: " + accountType);
                Log.d(TAG_CALENDAR, "Owner Type: " + String.valueOf(ownerAccount));

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
