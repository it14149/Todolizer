package gr.edu.todolizer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;



import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCalendarFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String TAG = "CalendarFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private CalendarView calendarView;

    private OnCalendarFragmentInteractionListener mListener;
    private Bundle bundleDate;


    public void setOnCalendarSelectedListener(OnCalendarFragmentInteractionListener callback){
        this.mListener = callback;
    }


    public CalendarFragment() {
        // Required empty public constructor
    }




    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);

        //Initialize Bundle data day/month/year
        Calendar c = Calendar.getInstance();
        c.get(Calendar.DAY_OF_MONTH);
        int day =  c.get(Calendar.DAY_OF_MONTH);
        int month =  c.get(Calendar.MONTH)+1;
        int year =  c.get(Calendar.YEAR);



        bundleDate = new Bundle();
        //Set bundle info
        setCalendarDate(day,month,year);

        mListener.onDateChangeListener(bundleDate);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int t_month, int dayOfMonth) {
                int month = t_month + 1;
                String date = dayOfMonth + "/" + (month) + "/" + year;
                Log.d(TAG, date);

                //Set bundle info
                setCalendarDate(dayOfMonth,month,year);

                mListener.onDateChangeListener(bundleDate);

                Toast.makeText(getContext(),date,Toast.LENGTH_SHORT).show();


                /*
                Intent intent = new Intent(CalendarActivity.this, TaskActivity.class);

                intent.putExtra("dayOfMonth",dayOfMonth);
                intent.putExtra("month",month+1);
                Log.d("Year",String.valueOf(year));
                intent.putExtra("year",year);

                setResult(Activity.RESULT_OK,intent);
                finish();

                 */
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {

            //mListener.onDateChangeListener(Bundle );
        }
    }

    private void setCalendarDate(int day, int month, int year){
        bundleDate.putInt("dayOfMonth",day);
        bundleDate.putInt("month",month-1);
        bundleDate.putInt("year",year);
    }


    public Bundle getCalendarDate(){
        return bundleDate;
    }




    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCalendarFragmentInteractionListener) {
            mListener = (OnCalendarFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCalendarFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnCalendarFragmentInteractionListener {
        // TODO: Update argument type and name
        void onDateChangeListener(Bundle bundle);
    }
}
