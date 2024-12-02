package com.parsons.bakery.ui.acct;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.encoders.ObjectEncoder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.Loading;
import com.parsons.bakery.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registration extends AppCompatActivity {

    public EditText email, password, firstName, lastName, phone, passConf, username;
    public Button Btn;
    public ProgressBar progressbar;
    public FirebaseAuth mAuth;
    private FloatingActionButton backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        // initialising all views through id defined above
        email = findViewById(R.id.email);
        password = findViewById(R.id.passwd);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        phone = findViewById(R.id.phoneNumber);
        username = findViewById(R.id.username);
        passConf = findViewById(R.id.passwdConf);
        Btn = findViewById(R.id.btnregister);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        passConf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals(password.getText().toString())) {
                    passConf.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.invalidEntry));
                } else {
                    passConf.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.black));
                }
            }
        });

        progressbar = findViewById(R.id.progressbar);

        // Set on Click Listener on Registration button
        Btn.setOnClickListener(v -> registerNewUser(this));
    }

    private void registerNewUser(Context context) {
        progressbar.setVisibility(View.VISIBLE);
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();
        if (TextUtils.isEmpty(emailText)) {
            Toast.makeText(getApplicationContext(), "Please enter email", Toast.LENGTH_LONG).show();
            progressbar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(passwordText)) {
            Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_LONG).show();
            progressbar.setVisibility(View.GONE);
            return;
        }
        mAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(context, Loading.class);
                intent.putExtra("signedUp", true);
                Map<String, Object> userData = new HashMap<>();
                userData.put("firstName", firstName.getText().toString());
                userData.put("lastName", lastName.getText().toString());
                userData.put("email", emailText);
                userData.put("phone", Integer.parseInt(phone.getText().toString()));
                userData.put("username", username.getText().toString());
                userData.put("accountType", 0);
                FirebaseFirestore.getInstance().collection("users").document(mAuth.getCurrentUser().getUid())
                        .set(userData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FIREBASE", "DocumentSnapshot successfully written!");
                            intent.putExtra("results", "Sign up Successful.");
                            DBHandler dbHandler = new DBHandler(context);
                            dbHandler.executeOne("UPDATE " + DBHandler.TABLE_ACCT + " SET "
                                    + DBHandler.COLUMN_ACCT_EMAIL + "='" + emailText + "',"
                                    + DBHandler.COLUMN_ACCT_LASTNAME + "='" + lastName.getText().toString() + "',"
                                    + DBHandler.COLUMN_ACCT_FIRSTNAME + "='" + firstName.getText().toString() + "',"
                                    + DBHandler.COLUMN_ACCT_PHONE + "=" + phone.getText().toString() + ","
                                    + DBHandler.COLUMN_ACCT_USERNAME + "='" + username.getText().toString() + "',"
                                    + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + "=0,"
                                    + DBHandler.COLUMN_ACCT_UNIQUE_ID + "='" + mAuth.getCurrentUser().getUid()
                                    + "' WHERE id=1");

                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name", firstName.getText().toString() + " " + lastName.getText().toString());
                            editor.putString("number", phone.getText().toString());
                            editor.apply();
                            FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid());

                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Log.w("FIREBASE", "Error writing document", e);
                            intent.putExtra("results", "Sign up Failed.");
                            startActivity(intent);
                        });
            } else {
                Toast.makeText(getApplicationContext(), "Registration failed, Please try again later", Toast.LENGTH_LONG).show();
                progressbar.setVisibility(View.GONE);
            }
        });
    }


    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }
}