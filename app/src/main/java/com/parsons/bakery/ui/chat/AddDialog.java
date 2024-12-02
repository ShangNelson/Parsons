package com.parsons.bakery.ui.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;
import com.parsons.bakery.ui.acct.AcctSpinnerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class AddDialog extends DialogFragment {
    Context context;
    public DBHandler dbHandler;
    Thread newThread = new Thread();
    OnDialogDismissListener mCallback;
    public static List<HashMap<String, String>> people = new ArrayList<>();

    public AddDialog(Context context) {
        this.context = context;
        dbHandler = new DBHandler(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnDialogDismissListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity
                    + " must implement OnDialogDismissListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_dialog, null);

        Spinner spinner = view.findViewById(R.id.spinner);
        final object[] currentSelection = {new object("Nothing", "NULL")};

        newThread = new Thread(new getPeople(context, spinner));
        newThread.setDaemon(false);
        newThread.start();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSelection[0] = (object) parent.getItemAtPosition(position);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setView(view)
                .setPositiveButton("Ok", (dialog, id) -> {
                    if (!currentSelection[0].getHiddenValue().equals("NULL")) {
                        dbHandler.executeOne("INSERT INTO people (name,past_messages,unique_id) VALUES ('" + currentSelection[0].getDisplayText() + "','No Messages','" + currentSelection[0].getHiddenValue() + "')");
                        Intent intent = new Intent(context, Chat.class);
                        intent.putExtra("name", currentSelection[0].getDisplayText());
                        intent.putExtra("id", currentSelection[0].getHiddenValue());
                        ContextCompat.getMainExecutor(context).execute(() -> context.startActivity(intent));
                    } else {
                        Toast.makeText(context, "Select user to add.", Toast.LENGTH_SHORT).show();
                    }
                    //mCallback.onDialogDismissListener();

                })
                .setNegativeButton("Cancel", (dialog, id) -> getDialog().dismiss());

        return builder.create();
    }

    public interface OnDialogDismissListener {
        void onDialogDismissListener();
    }

    class getPeople implements Runnable {
        private final Context context;
        private final Spinner spinner;

        public getPeople(Context context, Spinner spinner) {
            this.context = context;
            this.spinner = spinner;
        }

        @Override
        public void run() {
            //TODO implement accounts back in;
            //people = URLHelper.openUrlAccounts("SELECT name,unique_id FROM parsons.accts");
            //if (people.equals(new ArrayList<HashMap<String, String>>()))
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            List<HashMap<String, String>> alreadyHave = dbHandler.executeOne("SELECT name,unique_id FROM people");
            HashMap<String, String> me = dbHandler.executeOne("SELECT unique_id FROM acct").get(0);

            List<object> list = new ArrayList<>();
            list.add(new object("Select", "NULL"));
            for (HashMap<String, String> person: people) {
                if (!me.containsValue(person.get("unique_id"))) {
                    if (!compare(alreadyHave, person)) {
                        list.add(new object(person.get("name"), person.get("unique_id")));
                    }
                }
            }

            AcctSpinnerAdapter adapter = new AcctSpinnerAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, list, android.R.layout.simple_spinner_dropdown_item);
            ContextCompat.getMainExecutor(context).execute(() -> spinner.setAdapter(adapter));
        }

        public boolean compare(List<HashMap<String, String>> list1,HashMap<String, String> list2) {
            for (HashMap<String, String> alreadyHasValue : list1) {
               if (Objects.equals(alreadyHasValue.get("unique_id"), list2.get("unique_id"))) {
                    return true;
                }
            }
            return false;
        }
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        newThread.interrupt();
        super.onDismiss(dialog);
    }
}
