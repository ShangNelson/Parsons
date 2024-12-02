package com.parsons.bakery.client;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.parsons.bakery.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarDialog extends DialogFragment {

    TextView text;
    Context context;
    int year = Calendar.getInstance().get(Calendar.YEAR);
    int month = Calendar.getInstance().get(Calendar.MONTH);
    int day = Calendar.getInstance().get(Calendar.DATE);

    public CalendarDialog(TextView text, Context context) {
        this.text = text;
        this.context = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.calendar_dialog, null);
        DatePicker datePicker = view.findViewById(R.id.picker);
        datePicker.updateDate(year, month, day);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Ok", (dialog, id) -> {
                    year = datePicker.getYear();
                    month = datePicker.getMonth();
                    day = datePicker.getDayOfMonth();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, day);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    String dateText = simpleDateFormat.format(calendar.getTime());

                    ContextCompat.getMainExecutor(context).execute(() -> text.setText(dateText));
                })
                .setNegativeButton("Cancel", (dialog, id) -> getDialog().dismiss());

        return builder.create();
    }
}