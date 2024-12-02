package com.parsons.bakery;


import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Loading extends AppCompatActivity {
    TextView currentActions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        DBHandler handler = new DBHandler(this);
        int accountType = -1;
        try {
            accountType = Integer.parseInt(handler.executeOne("SELECT " + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + " FROM " + DBHandler.TABLE_ACCT).get(0).get("account_type"));
        } catch (Exception ignored) {
        }
        currentActions = findViewById(R.id.actions);
        new Thread(new update(this, this, accountType,handler,currentActions)).start();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }

    public void updateText(String newText) {
        currentActions.setText(newText);
    }
}