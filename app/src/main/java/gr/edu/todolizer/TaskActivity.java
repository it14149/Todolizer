package gr.edu.todolizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;



public class TaskActivity extends AppCompatActivity {

    private static final String TAG = "TaskActivity";
    private static final String STATE_PENDING_OPERATION = "PendingOperation";

    private static final String BTNCALENDARTEXT = "Set due date";
    private static final String BTNREMINDERTEXT =  "Set Reminder";
    private static final int PICK_DUE_DATE = 0;
    private static final int PICK_REMINDER = 1;

    private static final String DUE_DATE_DAY = "dayOfMonth";
    private static final String DUE_DATE_MONTH = "month";
    private static final String DUE_DATE_YEAR = "year";

    private static final String REMINDER_HOUR = "hour";
    private static final String REMINDER_MINUTE = "minute";

    private Button btnCalendar,btnDeleteDueDate;
    private Button btnReminder,btnDeleteReminder;
    private Intent intentReturnTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstance){
        super.onCreate(savedInstance);


        //ConstraintLayout layout = (ConstraintLayout)getLayoutInflater().inflate(R.layout.task_activity, null);
        setContentView(R.layout.task_activity);

        Button btnCreateTask = findViewById(R.id.task_activity_btn);
        btnDeleteDueDate =  findViewById(R.id.btnRemoveTaskDueDate);
        btnCalendar = findViewById(R.id.btnCalendar);

        intentReturnTask = new Intent(TaskActivity.this, MainActivity.class);
        intentReturnTask.putExtra("due_date",false);
        intentReturnTask.putExtra("reminder",false);

        btnDeleteDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCalendar.setText(BTNCALENDARTEXT);
                //btnDeleteDueDate.clearAnimation();
                btnDeleteDueDate.setVisibility(View.GONE);
                intentReturnTask.putExtra("due_date",false);

            }
        });

        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent calendarIntent = new Intent(TaskActivity.this, CalendarActivity.class);
                calendarIntent.putExtra("id","due_date");
                startActivityForResult(calendarIntent,PICK_DUE_DATE);
            }
        });

        btnDeleteReminder = findViewById(R.id.btnRemoveTaskReminder);
        btnDeleteReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnReminder.setText(BTNREMINDERTEXT);
                btnDeleteReminder.setVisibility(View.GONE);
                intentReturnTask.putExtra("reminder",false);
            }
        });

        btnReminder = findViewById(R.id.btnReminder);
        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(TaskActivity.this, CalendarActivity.class);
                calendarIntent.putExtra("id","reminder");
                startActivityForResult(calendarIntent,PICK_REMINDER);
            }
        });



        btnCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText temptitle =  findViewById(R.id.task_activity_title);
                EditText tempdescr =  findViewById(R.id.task_activity_descr);
                String title = temptitle.getText().toString();
                String description = tempdescr.getText().toString();


                intentReturnTask.putExtra("title", title);
                intentReturnTask.putExtra("descr",description);

                setResult(Activity.RESULT_OK, intentReturnTask);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == PICK_DUE_DATE){
            if(resultCode == Activity.RESULT_OK){
                Bundle dateBundle = data.getBundleExtra("date");
                intentReturnTask.putExtra("due_date",true);

                int dayOfMonth = dateBundle.getInt("dayOfMonth");
                int month = dateBundle.getInt("month");
                int year = dateBundle.getInt("year");
                String date = dayOfMonth + "/" + (month+1) + "/" + year;


                //It's probably better to not pass the same bundle back to MainActivity,
                //but this is easier and I am lazy
                /*
                intentReturnTask.putExtra("dayOfMonth",dayOfMonth);
                intentReturnTask.putExtra("month",month);
                intentReturnTask.putExtra("year",year);
                 */

                intentReturnTask.putExtra("due_date_dateBundle",dateBundle);
                btnCalendar.setText(date);

                btnDeleteDueDate.setVisibility(View.VISIBLE);
            }
        }

        //If requestCode == PICK_REMINDER then have to get data from both date and time fragments
        if(requestCode == PICK_REMINDER){
            if(resultCode == Activity.RESULT_OK){
                intentReturnTask.putExtra("reminder",true);
                Bundle dateBundle = data.getBundleExtra("date");

                //first the date

                int dayOfMonth = dateBundle.getInt("dayOfMonth");
                int month = dateBundle.getInt("month");
                int year = dateBundle.getInt("year");
                String date = dayOfMonth + "/" + (month+1) + "/" + year;


                //then the time
                Bundle timeBundle = data.getBundleExtra("time");
                int hour = timeBundle.getInt("hour");
                int minute = timeBundle.getInt("minute");

                String time = hour + ":" + minute;

                /*
                intentReturnTask.putExtra("hour",hour);
                intentReturnTask.putExtra("minute",minute);
                */
                intentReturnTask.putExtra("reminder_dateBundle",dateBundle);
                intentReturnTask.putExtra("reminder_timeBundle",timeBundle);

                btnReminder.setText(date + ", " +  time);
                btnDeleteReminder.setVisibility(View.VISIBLE);

            }
        }
    }

    private void onDateActivityResult(@Nullable Intent data) {
        int dayOfMonth = data.getIntExtra("dayOfMonth",0);
        int month = data.getIntExtra("month",0);
        int year = data.getIntExtra("year",0);
        String date = dayOfMonth + "/" + (month) + "/" + year;


        intentReturnTask.putExtra("dayOfMonth",dayOfMonth);
        intentReturnTask.putExtra("month",month);
        intentReturnTask.putExtra("year",year);
    }


}
