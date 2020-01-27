package gr.edu.todolizer;

import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

public class TimeFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private OnTimeFragmentInteractionListener mListener;
    private Bundle bundleTime;


    public void setOnTimeSelectedListener(OnTimeFragmentInteractionListener callback){
        this.mListener = callback;
    }

    public TimeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        setTime(hourOfDay,minute);
    }

    public static TimeFragment newInstance() {
        return new TimeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_time, container, false);

        TimePicker timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(9);
        timePicker.setCurrentMinute(0);

        bundleTime = new Bundle();
        setTime(9,0);
        mListener.onTimeChangeListener(bundleTime);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                setTime(hourOfDay,minute);
                mListener.onTimeChangeListener(bundleTime);
            }
        });

        return view;
    }

    private void setTime(int hour, int minute) {
        bundleTime.putInt("hour",hour);
        bundleTime.putInt("minute",minute);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnTimeFragmentInteractionListener) {
            mListener = (OnTimeFragmentInteractionListener) context;
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
    public interface OnTimeFragmentInteractionListener {
        // TODO: Update argument type and name
        void onTimeChangeListener(Bundle bundle);
    }
}
