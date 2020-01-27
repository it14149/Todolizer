package gr.edu.todolizer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Calendar;

public class Task implements Parcelable{

    private int taskID = -1;
    private String title;
    private String description;
    private long dtStart;
    private long reminder;
    private String organizer = "Local";
    private int checkboxChecked;

    private static int DEFAULT_TASK_ID = -1;
    private static long DEFAULT_REMINDER = 0;
    private static String DEFAULT_ORGANIZER = "Local";
    private static int DEFAULT_CHECKBOX = 0;

    private int dayOfWeek;
    private int dayOfMonth;
    private int month;
    private int year;

    private static final String[] months = {
            "Jan", "Feb", "Mar",
            "Apr", "May", "Jun",
            "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec"
    };

    private static final String[] weekdays = {
            "Sun", "Mon", "Tue",
            "Wed", "Thu", "Fri",
            "Sat"
    };

    //Task with the least variables possible
    public Task(String title, long dtStart){
        this(title,"",dtStart);
    }

    //Another possibility
    public Task(String title, String description, long dtStart){
        this(title,description,dtStart,DEFAULT_REMINDER);
    }


    //This is the task with the most variables possible when creating a new one
    public Task(String title, String description, long dtStart, long reminder){
        this(DEFAULT_TASK_ID, title,description,dtStart,reminder,DEFAULT_ORGANIZER,DEFAULT_CHECKBOX);
    }


    //This is called when loading events from the calendar with ContentContract
    public Task(String title, String description, long dtStart,String organizer){
        this(DEFAULT_TASK_ID,title,description,dtStart,DEFAULT_REMINDER,organizer,DEFAULT_CHECKBOX);
    }


    //This is called when loading tasks from the database
    public Task(int taskID, String title, String description, long dtStart, long reminder, String organizer, int checkboxChecked){
        this.taskID = taskID;
        this.title = title;
        this.description = description;
        this.dtStart = dtStart;
        this.reminder = reminder;
        this.organizer = organizer;
        this.checkboxChecked = checkboxChecked;



        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dtStart);
        Log.d("REMINDER", "Task constructor reminder: " + this.reminder);

        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        month = calendar.get(Calendar.MONTH) ;
        year = calendar.get(Calendar.YEAR);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    }


    public String getDescription() {
        return description;
    }

    public int getCheckboxChecked() {
        return checkboxChecked;
    }



    public boolean isCheckboxChecked() {
        return checkboxChecked != 0;

    }

    public String getDate(){
        return String.format("%s %d, %s %d",weekdays[dayOfWeek],dayOfMonth,months[month],year);
    }


    public String getMonthAndDay(){
        if(dayOfMonth != 0){
            return months[month] + ", " + dayOfMonth;
        }
        else{
            return null;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    @Override
    public String toString() {
        return "taskID: " + taskID + " ,description: " + description + "Time: " + getDate();
    }



    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public int getTaskID(){
        if (taskID == -1) {
            Log.d("TASK","task does not correspond to a database entry/n" +
                    "This shouldn't happen");
            return -1;
        }
        return taskID;
    }

    public boolean findOriginalID(ArrayList<Task> taskArrayList){
        //String dtStart = this.dtStart;
        for(Task task:taskArrayList){

            if(dtStart == task.dtStart
            && title.equals(task.getTitle())
            && description.equals(task.getDescription())){
                return true;
            }
        }
        return false;
    }

    public void setCheckboxChecked(boolean checked){
        //checkboxChecked = checked ? 1:0 ;

        if(checked){
            checkboxChecked=1;
        }else checkboxChecked= 0;
    }

    //Parcel stuff

    public long getReminder() {
        Log.d("REMINDER", "Get Reminder in Task: " + reminder);
        return reminder;
    }

    protected Task(Parcel in){
        taskID = in.readInt();
        title = in.readString();
        description = in.readString();
        dayOfMonth = in.readInt();
        month = in.readInt();
        year = in.readInt();
        checkboxChecked = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(taskID);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(dayOfMonth);
        dest.writeInt(month);
        dest.writeInt(year);
        dest.writeInt(checkboxChecked);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public int getYear() {
        return year;
    }

    public String getOrganizer() {
        return organizer;
    }

    public long getDtStart(){
        return dtStart;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void removeReminder() {
        this.reminder = 0;
    }

}
