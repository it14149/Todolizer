package gr.edu.todolizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


public class CalendarActivity extends AppCompatActivity implements
        CalendarFragment.OnCalendarFragmentInteractionListener,
        TimeFragment.OnTimeFragmentInteractionListener{

    private static final String TAG = "CalendarActivity";


    private Button calendarBtn, timeBtn;
    private Button cancelSetDateAndTimeBtn, saveDateAndTime;
    private CalendarFragment calendarFragment;
    private TimeFragment timeFragment;
    private FragmentTransaction fragmentTransaction;

    private Bundle dateBundle, timeBundle;


    @Override
    protected void onResume(){
        super.onResume();

    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_layout);

        final String id = getIntent().getStringExtra("id");


        if(id.equals("due_date")){
            findViewById(R.id.linearLayout4).setVisibility(View.GONE);
            //findViewById(R.id.constraintLayout1).setVisibility(View.GONE);

            //Initialize CalendarFragment
            calendarFragment = CalendarFragment.newInstance();

            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.calendar_frame_layout, calendarFragment);
            fragmentTransaction.commit();


        }
        else{ //id.equals("reminder");
            initializeButtonsAndListeners();
            initializeReminderFragments();

        }

        //calendarView = findViewById(R.id.calen);




        cancelSetDateAndTimeBtn = findViewById(R.id.cancelCalendarBtn);
        cancelSetDateAndTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        saveDateAndTime = findViewById(R.id.saveCalendarDateBtn);
        saveDateAndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent(CalendarActivity.this,TaskActivity.class);

                if(id.equals("reminder")){
                    returnIntent.putExtra("date",dateBundle);
                    returnIntent.putExtra("time",timeBundle);
                }
                else{//id.equals("due_date")
                    returnIntent.putExtra("date", dateBundle);
                }
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });






    }
    private void initializeButtonsAndListeners(){
        //Initialize buttons and OnClickListeners
        calendarBtn = findViewById(R.id.calendarBtn);
        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment();
            }
        });

        timeBtn = findViewById(R.id.timeBtn);
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment();
            }
        });
    }


    private void initializeReminderFragments(){
        //Initialize Fragments
        calendarFragment = CalendarFragment.newInstance();
        timeFragment = TimeFragment.newInstance();

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.calendar_frame_layout, timeFragment);
        fragmentTransaction.hide(timeFragment);
        fragmentTransaction.add(R.id.calendar_frame_layout, calendarFragment);
        fragmentTransaction.commit();
    }
    private void loadFragment() {
        //fragmentManager = getSupportFragmentManager();
        //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         fragmentTransaction = getSupportFragmentManager().beginTransaction();

         if(calendarFragment.isVisible()){
             fragmentTransaction.hide(calendarFragment);
             fragmentTransaction.show(timeFragment);
         }
         else{
             fragmentTransaction.hide(timeFragment);
             fragmentTransaction.show(calendarFragment);
         }
        fragmentTransaction.commit();

    }

    //CalendarFragment interface method
    @Override
    public void onDateChangeListener(Bundle bundle) {
        dateBundle = bundle;
    }

    @Override
    public void onTimeChangeListener(Bundle bundle){
        timeBundle = bundle;
        int hour = timeBundle.getInt("hour");
        int minute = timeBundle.getInt("minute");

        String time = hour + ":" + minute;
        Toast.makeText(this,"Time: " + time,Toast.LENGTH_LONG).show();
        Log.d("CalendarActivity", "Time: " + time);
    }



    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        if (fragment instanceof CalendarFragment) {
            CalendarFragment calendarFragment = (CalendarFragment) fragment;
            calendarFragment.setOnCalendarSelectedListener(this);
        } else {//fragment instanceof TimeFragment
            TimeFragment timeFragment = (TimeFragment) fragment;
            timeFragment.setOnTimeSelectedListener(this);

        }
    }
}
