package com.parsons.bakery.ui.acct;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parsons.bakery.Baker;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.Loading;
import com.parsons.bakery.MainActivity;
import com.parsons.bakery.R;

import java.util.HashMap;

public class Login extends AppCompatActivity{

    private EditText emailTextView, passwordTextView;
    private Button Btn;
    private FloatingActionButton backButton;
    private ProgressBar progressbar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        emailTextView = findViewById(R.id.email);
        passwordTextView = findViewById(R.id.password);
        Btn = findViewById(R.id.login);
        progressbar = findViewById(R.id.progressBar);
        Btn.setOnClickListener(v -> loginUserAccount(this));
        backButton = findViewById(R.id.backFloatingButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void loginUserAccount(Context context) {
        progressbar.setVisibility(View.VISIBLE);
        String email, password;
        email = emailTextView.getText().toString();
        password = passwordTextView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Snackbar.make(findViewById(R.id.parentIdLinearLayout), "Please enter username.", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Snackbar.make(findViewById(R.id.parentIdLinearLayout), "Please enter password.", Snackbar.LENGTH_LONG).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String UUID = task.getResult().getUser().getUid();

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                DocumentReference ref = firestore.collection("users").document(UUID);
                ref.get().addOnCompleteListener(getDoc -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot reference = getDoc.getResult();
                        DBHandler dbHandler = new DBHandler(context);
                        dbHandler.executeOne("UPDATE " + DBHandler.TABLE_ACCT
                                + " SET "
                                + DBHandler.COLUMN_ACCT_FIRSTNAME + "='" + reference.getString("firstName") + "',"
                                + DBHandler.COLUMN_ACCT_LASTNAME + "='" + reference.getString("lastName") + "',"
                                + DBHandler.COLUMN_ACCT_USERNAME + "='" + reference.getString("username") + "',"
                                + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + "=" + reference.get("accountType") + ","
                                + DBHandler.COLUMN_ACCT_PHONE + "=" + reference.get("phone") + ","
                                + DBHandler.COLUMN_ACCT_EMAIL + "='" + reference.getString("email") + "',"
                                + DBHandler.COLUMN_ACCT_UNIQUE_ID + "='" + UUID + "'" +
                                " WHERE id=1");

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("name", reference.get("firstName") + " " + reference.get("lastName"));
                        editor.putString("number", String.valueOf(reference.getLong("phone")));
                        editor.apply();
                        FirebaseMessaging.getInstance().subscribeToTopic(UUID);
                        System.out.println("Successful");
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show();
                        context.startActivity(new Intent(context, Loading.class));
                        ContextCompat.getMainExecutor(context).execute(() -> progressbar.setVisibility(View.GONE));
                        if ((long) reference.get("accountType") == 0) {
                            startActivity(
                                    new Intent(this, MainActivity.class)
                            );
                        } else {
                            startActivity(
                                    new Intent(this, Baker.class)
                            );
                        }
                    } else {
                        Log.d("FIREBASE", "Failed to pull userInfo");
                    }
                });


            } else {
                Snackbar.make(findViewById(R.id.parentIdLinearLayout), getString(R.string.invalid_login), Snackbar.LENGTH_LONG).show();
                progressbar.setVisibility(View.GONE);
            }
        });
    }
}