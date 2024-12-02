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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parsons.bakery.databinding.ActivityBakerBinding;
import com.parsons.bakery.ui.cart.Cart;

import java.util.concurrent.atomic.AtomicReference;

public class Baker extends AppCompatActivity {

    public static final String TARGET_FRAGMENT = "target_fragment";

    public NavController navController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.parsons.bakery.databinding.ActivityBakerBinding binding = ActivityBakerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_order, R.id.navigation_orders, R.id.navigation_acct)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_baker);
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
        if (intent.hasExtra(TARGET_FRAGMENT)) {
            System.out.println("Had extra to go to fragment");
            navController.navigate(intent.getExtras().getInt(TARGET_FRAGMENT));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        AtomicReference<DBHandler> dbHandler = new AtomicReference<>(new DBHandler(getApplicationContext()));
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
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        Intent intent = new Intent(getApplicationContext(), Loading.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getApplicationContext().startActivity(intent);
                        android.os.Process.killProcess(android.os.Process.myPid());
                    });

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("name");
                    editor.remove("number");
                    editor.apply();

                    builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
