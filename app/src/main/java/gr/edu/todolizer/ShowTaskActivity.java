package gr.edu.todolizer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.otto.Bus;

import java.util.Calendar;

public class ShowTaskActivity extends AppCompatActivity {


    private int taskID;
    private Task task;

    private String date;
    private long reminder;


    private int position;
    private Bus bus;

    public ShowTaskActivity(){
       /*
        this.taskID = task.getTaskID();
        this.description = task.getDescription();
        this.isChecked = task.isCheckboxChecked();
        this.date = task.getDate();
        this.reminder = task.getReminder();

        */
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_task);
        //Parcel parcelTask = getIntent().getParcelableExtra("Task");
        task = getIntent().getParcelableExtra("Task");

        this.taskID = task.getTaskID();
        final String description = task.getDescription();
        final boolean isChecked = task.isCheckboxChecked();
        final String title = task.getTitle();
        this.date = task.getDate();
        this.position = getIntent().getIntExtra("position",0);
        Log.d("REMINDER", "TaskID: " + taskID);
        this.reminder = task.getReminder();

        bus = GlobalBus.getInstance();

        Button btnCancelChanges = findViewById(R.id.showTaskCancel);
        Button btnSaveChanges = findViewById(R.id.showTaskSaveChanges);
        Button btnSaveLocal = findViewById(R.id.saveToLocalCalendar);

        final EditText txvTaskTitle = findViewById(R.id.showTask_Title);
        final EditText txvDescription = findViewById(R.id.showTask_description);
        TextView txvDate = findViewById(R.id.showTaskDate);
        TextView txvReminder = findViewById(R.id.showTaskReminder);

        Button deleteTask = findViewById(R.id.showTask_deleteTask);
        final CheckBox checkBox = findViewById(R.id.showTask_checkbox);


        btnCancelChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnSaveChanges.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                DatabaseHelper db = DatabaseHelper.getInstance(ShowTaskActivity.this);

                //Check if the checkbox state has been changed
                boolean checkBoxChecked = checkBox.isChecked();
                if(isChecked != checkBoxChecked){
                    bus.post(new Events.CheckboxStatus(position,checkBoxChecked));
                    db.updateCheckbox(taskID,checkBoxChecked);
                }

                //Check if the description text has been changed
                String changedDescription = txvDescription.getText().toString();
                if(!changedDescription.equals(description)){
                    bus.post(new Events.DescriptionChanged(position,changedDescription));
                    db.updateDescription(taskID, changedDescription);
                }

                String changedTitle = txvTaskTitle.getText().toString();
                if(!changedTitle.equals(title)){
                    bus.post(new Events.TitleChanged(position, changedTitle));
                    db.updateTitle(taskID, changedTitle);
                }
                finish();
            }
        });

        deleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ShowTaskActivity.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Do you want to delete this task");
                builder.setIcon(R.drawable.ic_launcher_foreground);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Log.d("ShowTask","Delete Task");
                        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ShowTaskActivity.this);
                        dbHelper.deleteTask(taskID);


                        messageHasBeenDeleted();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Log.d("ShowTask","NOT Delete Task");
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        btnSaveLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pushToCalendar(task);
            }
        });

        txvDescription.setText(description);
        txvDate.setText(date);
        txvTaskTitle.setText(title);
        Log.d("REMINDER", "Reminder " + reminder);
        if(reminder != 0){
            txvReminder.setText("Reminder: " + reminder);
        }

        checkBox.setChecked(isChecked);

        /*
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bus.post(new Events.CheckboxStatus(position,isChecked));
            }
        });

         */

        txvDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("ShowTask","beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("ShowTask","onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("ShowTask","afterTextChanged");

            }
        });

    }

    private void messageHasBeenDeleted(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Task has been deleted")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        bus.post(new Events.DeletedTaskID(position));
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void pushToCalendar(Task task){

        long dtStart = task.getDtStart();
        Intent intent = new Intent(Intent.ACTION_INSERT)

                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE,task.getTitle())
                .putExtra(CalendarContract.Events.DESCRIPTION,task.getDescription())
                .putExtra(CalendarContract.Events.ORGANIZER,task.getOrganizer())
                .putExtra(CalendarContract.Events.ALL_DAY,true)
                .putExtra(CalendarContract.Events.DTSTART,dtStart);

        startActivity(intent);
        Log.d("ShowTaskActivity", "Pushing Event");
    }

}
