package com.parsons.bakery.client;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.parsons.bakery.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeDialog extends DialogFragment {

    TextView text;
    Context context;
    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    int minute = 0;

    public TimeDialog(TextView text, Context context) {
        this.text = text;
        this.context = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.time_dialog, null);
        TimePicker timePicker = view.findViewById(R.id.timePicker);
        timePicker.setHour(hour);
        timePicker.setMinute(0);
        timePicker.setOnTimeChangedListener((view1, hourOfDay, minute) -> {
            int minuteRounded = (minute + 7) / 15 * 15;
            view1.setMinute(minuteRounded);
            if ((hourOfDay + (minuteRounded/60f)) > 17.5f && hourOfDay != 23) {
                view1.setHour(17);
                view1.setMinute(30);
            } else if (hourOfDay == 23) {
                view1.setHour(11);
            } else if (hourOfDay < 7 && hourOfDay != 0) {
                view1.setHour(7);
            } else if (hourOfDay == 0) {
                view1.setHour(12);
            }
        });
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Ok", (dialog, id) -> {
                    hour = timePicker.getHour();
                    minute = timePicker.getMinute();
                    if (minute > 9) {
                        ContextCompat.getMainExecutor(context).execute(() -> text.setText(hour + ":" + minute));
                    } else {
                        ContextCompat.getMainExecutor(context).execute(() -> text.setText(hour + ":0" + minute));

                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> getDialog().dismiss());

        return builder.create();
    }
}