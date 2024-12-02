package com.parsons.bakery.baker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.parsons.bakery.AndroidDatabaseManager;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.Loading;
import com.parsons.bakery.R;
import com.parsons.bakery.baker.ui.chat.AddDialog;
import com.parsons.bakery.baker.ui.chat.ChatFragment;
import com.parsons.bakery.client.Cart;
import com.parsons.bakery.client.MainActivity;
import com.parsons.bakery.databinding.ActivityBakerBinding;

import java.util.Random;

public class Baker extends AppCompatActivity implements AddDialog.OnDialogDismissListener {

    private ActivityBakerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBakerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_baker_home, R.id.navigation_orders, R.id.navigation_chat)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_baker);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        //System.out.println("Sending Notification to self");
        //sendMessage("rJtwz6xmmUhA5vb0xQLyJ7Mr9wv1", "Test", "rJtwz6xmmUhA5vb0xQLyJ7Mr9wv1");
    }

    public void sendMessage(String recipient, String message, String sender) {

        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder("1091222427757@gcm.googleapis.com")
                .setMessageId(getRandomMessageId())
                .addData("action", "MESSAGE")
                .addData("recipient", recipient)
                .addData("message", message)
                .addData("sender", sender)
                .addData("title", "New Message!")
                .addData("name", "Tester123")
                .build());
    }

    public String getRandomMessageId() {
        return "m-" + Long.toString(new Random().nextLong());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        MenuItem switchesBack = menu.findItem(R.id.switchesBack);
        switchesBack.setVisible(false);
        if (new DBHandler(getApplicationContext()).executeOne("SELECT * FROM acct").isEmpty()) {
            MenuItem logout = menu.findItem(R.id.Logout);
            logout.setVisible(false);
            invalidateOptionsMenu();
        } else {
            if (Integer.parseInt(new DBHandler(getApplicationContext()).executeOne("SELECT * FROM acct").get(0).get("is_baker")) == 0) {
                MenuItem switchesTo = menu.findItem(R.id.switchesT0);
                switchesTo.setVisible(false);
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
                        new DBHandler(getApplicationContext()).executeOne("DELETE FROM acct");
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
            case R.id.switchesT0:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogDismissListener() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.nav_host_fragment_activity_main, new ChatFragment());
        ft.commit();
    }
}