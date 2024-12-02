package com.parsons.bakery;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parsons.bakery.ui.cart.Cart;
import com.parsons.bakery.databinding.ActivityMainBinding;

import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    public static final String TARGET_FRAGMENT = "target_fragment";

    public NavController navController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.parsons.bakery.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_order, R.id.navigation_acct)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update the activity's intent
        handleNavigationIntent(intent); // Handle the navigation
    }

    public void handleNavigationIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("justSent")) {
                Snackbar.make(findViewById(R.id.container), bundle.getString("results"), Snackbar.LENGTH_LONG).show();
            } else if (bundle.getBoolean("signedUp")) {
                Snackbar.make(findViewById(R.id.container), bundle.getString("results"), Snackbar.LENGTH_LONG).show();
            }
            if (intent.hasExtra(TARGET_FRAGMENT)) {
                navController.navigate(bundle.getInt(TARGET_FRAGMENT));
            }
        }

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        int acctType = Integer.parseInt(new DBHandler(getApplicationContext()).executeOne("SELECT * FROM acct").get(0).get(DBHandler.COLUMN_ACCT_ACCOUNT_TYPE));
        System.out.println("Account type in menu loader: " + acctType);
        if (acctType == -1) {
            MenuItem logout = menu.findItem(R.id.Logout);
            logout.setVisible(false);
            invalidateOptionsMenu();
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
                AtomicReference<DBHandler> dbHandler = new AtomicReference<>(new DBHandler(getApplicationContext()));
                if ((!isFinishing() && Integer.parseInt(dbHandler.get().executeOne("SELECT " + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + " FROM " + DBHandler.TABLE_ACCT).get(0).get(DBHandler.COLUMN_ACCT_ACCOUNT_TYPE)) != -1)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setMessage("Are you sure you want to logout?");

                    builder.setTitle("Logout?");
                    builder.setIcon(R.drawable.ic_baseline_warning_24);

                    builder.setPositiveButton("Yes", (dialog, id) -> {
                        FirebaseMessaging messaging = FirebaseMessaging.getInstance();
                        dbHandler.set(new DBHandler(getApplicationContext()));
                        messaging.unsubscribeFromTopic(dbHandler.get().executeOne("SELECT unique_id FROM acct").get(0).get("unique_id"));
                        dbHandler.get().executeOne("UPDATE " + DBHandler.TABLE_ACCT + " SET "
                                + DBHandler.COLUMN_ACCT_EMAIL + "='',"
                                + DBHandler.COLUMN_ACCT_LASTNAME + "='',"
                                + DBHandler.COLUMN_ACCT_FIRSTNAME + "='',"
                                + DBHandler.COLUMN_ACCT_PHONE + "=NULL,"
                                + DBHandler.COLUMN_ACCT_USERNAME + "='',"
                                + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + "=-1,"
                                + DBHandler.COLUMN_ACCT_UNIQUE_ID + "='' WHERE id=1");
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("name");
                        editor.remove("number");
                        editor.apply();
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        Intent intent = new Intent(getApplicationContext(), Loading.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getApplicationContext().startActivity(intent);
                    });

                    builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                dbHandler.get().close();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}