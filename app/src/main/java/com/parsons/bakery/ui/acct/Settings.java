package com.parsons.bakery.ui.acct;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.parsons.bakery.R;

public class Settings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        EditTextPreference number = findPreference("number");
        number.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_PHONE));
        EditTextPreference name = findPreference("name");
        name.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT));
    }
}