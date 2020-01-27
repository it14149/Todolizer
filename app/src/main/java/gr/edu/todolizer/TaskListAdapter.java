package gr.edu.todolizer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {


    private final String TAG = "TaskListAdapter";
    //private final LayoutInflater inflater;
   // private final int layoutResource;
    private List<Task> tasks;
    private Context context;

    private boolean checkboxIsChecked;

    private final Bus bus;



    public TaskListAdapter(@NonNull Context context, ArrayList<Task> objects) {
        this.context = context;
        //inflater = LayoutInflater.from(context);
        tasks = objects;

        bus = DatabaseHelper.getInstance(this.context).getBus();
        bus.register(this);


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.adapter_task_view_layout, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Task task = tasks.get(position);



        viewHolder.taskTitle.setText(task.getTitle() + "");

        viewHolder.taskTitle.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent taskIntent = new Intent();
                taskIntent.setClass(context,ShowTaskActivity.class);

                taskIntent.putExtra("Task",task);
                taskIntent.putExtra("position",position);

                context.startActivity(taskIntent);
            }
        }));


        //viewHolder.taskDescription.setBackground(null);


        String monthAndDay = task.getMonthAndDay();
        viewHolder.taskMonthAndDay.setText(monthAndDay);
        viewHolder.taskYear.setText(String.valueOf(task.getYear()));
        /*
        String monthAndDay = task.getMonthAndDay();
        if(monthAndDay != null){
            viewHolder.taskMonthAndDay.setText(monthAndDay);
            viewHolder.taskYear.setText(String.valueOf(task.getYear()));
        }
        else {
            viewHolder.taskMonthAndDay.setText("");
            viewHolder.taskYear.setText("");
        }

         */





        viewHolder.checkBox.setOnCheckedChangeListener(null);

        boolean checked = task.isCheckboxChecked();
        if(checked){
            viewHolder.checkBox.setChecked(task.isCheckboxChecked());

            viewHolder.taskTitle.setPaintFlags(viewHolder.taskTitle.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
        }

        //viewHolder.checkBox.setChecked(tasks.get(position).isCheckboxChecked());
        //viewHolder.checkBox.setTag(tasks.get(position));

        //viewHolder.checkBox.setOnCheckedChangeListener(new CustomOnCheckedListener());
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Task task = tasks.get(position);
                Task task = tasks.get(viewHolder.getAdapterPosition());


                if(isChecked != task.isCheckboxChecked()){
                    DatabaseHelper db = DatabaseHelper.getInstance(context);
                    db.updateCheckbox(task.getTaskID(),isChecked);

                    task.setCheckboxChecked(isChecked);

                    viewHolder.taskTitle.setPaintFlags(viewHolder.taskTitle.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
                }
                else{//This should never happen.
                    //To avoid this we set the OnClickListener to null on BindViewHolder
                    //Then we get its correct value, set it and set a new OnClickListener
                    Log.d(TAG,"The checkbox OnClickListener fired when it shouldn't");
                }
                //notifyItemChanged(position);
            }
        });

    }




    @Override
    public int getItemCount() {
        return  this.tasks.size();
    }

@Subscribe
public void getTaskDeleted(Events.DeletedTaskID event){
        int position = event.getPosition();
        tasks.remove(tasks.get(position));
        notifyDataSetChanged();
        Log.d("Subscriber Facts", "Task has been deleted");
}


@Subscribe
public void getCheckboxChanged(Events.CheckboxStatus event){
        int position = event.getPosition();
        boolean isChecked = event.getChecked();
        Task task = tasks.get(position);
        task.setCheckboxChecked(isChecked);
        notifyItemChanged(position);
        Log.d("Subscriber Facts", "Checkbox status has been changed");

}

@Subscribe
public void getDescriptionChanged(Events.DescriptionChanged event){
        int position = event.getPosition();
        String description = event.getDescription();
        Task task = tasks.get(position);
        task.setDescription(description);
        notifyItemChanged(position);
        Log.d("Subscriber Facts", "Description has been altered");
}

    @Subscribe
    public void getTitleChanged(Events.TitleChanged event){
        int position = event.getPosition();
        String title = event.getTitle();
        Task task = tasks.get(position);
        task.setTitle(title);
        notifyItemChanged(position);
        Log.d("Subscriber Facts", "Title has been altered");
    }




    public class ViewHolder extends RecyclerView.ViewHolder{
        final Button taskTitle;
        final TextView taskMonthAndDay;
        final TextView taskYear;
        final CheckBox checkBox;

        ViewHolder(View view){
            super(view);
            taskTitle = view.findViewById(R.id.taskTitle);
            taskTitle.setBackground(null);
            taskMonthAndDay = view.findViewById(R.id.taskMonthAndDay);
            taskYear = view.findViewById(R.id.taskYear);
            checkBox = view.findViewById(R.id.checkBox);

        }

    }



}
