package com.parsons.bakery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class Loading extends AppCompatActivity {
    TextView currentActions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        DBHandler handler = new DBHandler(this);
        boolean isLoggedIn = !handler.executeOne("SELECT * FROM acct").isEmpty();
        currentActions = findViewById(R.id.actions);
        if (isLoggedIn) {
            if (Integer.parseInt(handler.executeOne("SELECT " + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + " FROM acct").get(0).get("is_baker")) == 1) {
                new Thread(new update(this, this, true)).start();
            } else {
                new Thread(new update(this, this, false)).start();
            }
        } else {
            new Thread(new update(this, this, false)).start();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }

    public void updateText(String newText) {
        currentActions.setText(newText);
    }

    @Override
    public void onBackPressed() {

    }
}