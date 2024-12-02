package com.parsons.bakery.client;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parsons.bakery.AndroidDatabaseManager;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.Loading;
import com.parsons.bakery.R;
import com.parsons.bakery.baker.Baker;
import com.parsons.bakery.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.parsons.bakery.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_order, R.id.navigation_acct)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("justSent")) {
                Snackbar.make(findViewById(R.id.container), bundle.getString("results"), Snackbar.LENGTH_LONG).show();
            } else if (bundle.getBoolean("signedUp")) {
                Snackbar.make(findViewById(R.id.container), bundle.getString("results"), Snackbar.LENGTH_LONG).show();
            }
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        MenuItem switchesTo = menu.findItem(R.id.switchesT0);
        switchesTo.setVisible(false);
        if (new DBHandler(getApplicationContext()).executeOne("SELECT * FROM acct").isEmpty()) {
            MenuItem logout = menu.findItem(R.id.Logout);
            logout.setVisible(false);
            invalidateOptionsMenu();
        } else {
            if (Integer.parseInt(new DBHandler(getApplicationContext()).executeOne("SELECT * FROM acct").get(0).get("is_baker")) == 0) {
                MenuItem switches = menu.findItem(R.id.switchesBack);
                switches.setVisible(false);
                invalidateOptionsMenu();
            }
        }
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.db:
                startActivity(new Intent(getApplicationContext(), AndroidDatabaseManager.class));
                return true;
            case R.id.cart:
                startActivity(new Intent(getApplicationContext(), Cart.class));
                return true;
            case R.id.Logout:
                if (!isFinishing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setMessage("Are you sure you want to logout?");

                    builder.setTitle("Logout?");
                    builder.setIcon(R.drawable.ic_baseline_warning_24);

                    builder.setPositiveButton("Yes", (dialog, id) -> {
                        FirebaseMessaging messaging = FirebaseMessaging.getInstance();
                        DBHandler dbHandler = new DBHandler(getApplicationContext());
                        messaging.unsubscribeFromTopic(dbHandler.executeOne("SELECT unique_id FROM acct").get(0).get("unique_id"));
                        dbHandler.executeOne("DELETE FROM acct");
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        Intent intent = new Intent(getApplicationContext(), Loading.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getApplicationContext().startActivity(intent);
                        android.os.Process.killProcess(android.os.Process.myPid());
                    });

                    builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            case R.id.switchesBack:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}