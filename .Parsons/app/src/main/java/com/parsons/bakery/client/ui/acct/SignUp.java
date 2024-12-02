package com.parsons.bakery.ui.acct;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.Firebase;
import com.parsons.bakery.client.MainActivity;
import com.parsons.bakery.Post;
import com.parsons.bakery.ProxyStmt;
import com.parsons.bakery.ProxyStmtSend;
import com.parsons.bakery.R;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUp extends AppCompatActivity {

    public static List<HashMap<String, String>> usedUsername = new ArrayList<>();
    public static boolean stillRunning = true;
    EditText FirstName;
    EditText LastName;
    EditText Username;
    EditText Password;
    Button Submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        new Thread(new getUsedUsernames()).start();

        FirstName = findViewById(R.id.FirstName);
        LastName = findViewById(R.id.LastName);
        Username = findViewById(R.id.UserName);
        Password = findViewById(R.id.Password);
        Submit = findViewById(R.id.enter);


        Submit.setOnClickListener(v -> {
            if (!stillRunning) {
                String firstName = FirstName.getText().toString();
                String password = Password.getText().toString();
                String lastName = LastName.getText().toString();
                String username = Username.getText().toString();
                HashMap<String, String> contentToCheckFor = new HashMap<>();
                contentToCheckFor.put("username", username);
                if (!firstName.equals("") && !password.equals("") && !lastName.equals("") && !username.equals("")) {
                    if (!usedUsername.contains(contentToCheckFor)) {
                        Thread thread = new Thread(new doSignUp(username.trim(), password, firstName.trim(), lastName.trim(), this));
                        thread.start();
                    } else {
                        Snackbar.make(findViewById(R.id.signUpContainer), "Username already in use.", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.signUpContainer), "Please fill out all fields.", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.signUpContainer), "Please try again in a moment.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    static class doSignUp implements Runnable {

        String pass;
        String username;
        String FirstName;
        String LastName;
        Context context;

        public doSignUp(String username, String pass, String first, String last, Context context) {
            this.username = username;
            this.pass = pass;
            FirstName = first;
            LastName = last;
            this.context = context;
        }

        @Override
        public void run() {
            List<HashMap<String, String>> returns = sendNewAccount((FirstName + " " + LastName), username, BCrypt.hashpw(pass, BCrypt.gensalt()), 5);
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("signedUp", true);
            if (!returns.isEmpty()) {
                if (returns.get(0).get("Response").equals("true")) {
                    intent.putExtra("results", "Sign up Successful.");
                    DBHandler dbHandler = new DBHandler(context);
                    dbHandler.executeOne("DELETE FROM acct");
                    dbHandler.executeOne("INSERT INTO acct (name,username) VALUES ('" + (FirstName + " " + LastName) + "','" + username + "')");

                } else {
                    intent.putExtra("results", "Sign up Failed.");
                }
            }
            ContextCompat.getMainExecutor(context).execute(() -> context.startActivity(intent));
        }

        public List<HashMap<String, String>> sendNewAccount(String name, String username, String hashed_pass, int tries) {
            if (tries <= 0) {
                return null;
            }
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.kidsavings.org/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            ProxyStmtSend proxy = retrofit.create(ProxyStmtSend.class);
            int epoch = getEpoch();
            int epochRand = epoch + new Random().nextInt();

            Call<List<Post>> call = proxy.getPosts(
                    BCrypt.hashpw(String.valueOf(epoch), BCrypt.gensalt()),
                    "INSERT INTO parsons.accts (name, username, hashed_pass, unique_id) VALUES ('" + name + "','" + username + "','" + hashed_pass + "'," + epochRand + ")",
                    "parsons.accts");
            List<HashMap<String, String>> returnable = new ArrayList<>();
            try {
                System.out.println("executed");
                Response<List<Post>> response = call.execute();
                List<Post> posts = response.body();
                HashMap<String, String> content = new HashMap<>();
                if (posts == null) {
                    content.put("Response", "false");
                } else {
                    content.put("Response", posts.get(0).getResponse());
                }
                returnable.add(content);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed with " + tries + " left");
                return sendNewAccount(name, username, hashed_pass, tries-1);
            }
            return returnable;
        }

        public int getEpoch() {
            long seconds = System.currentTimeMillis() / 1000;
            return (int) seconds;
        }
    }

    static class getUsedUsernames implements Runnable {

        @Override
        public void run() {
            usedUsername = openUrlAccounts("SELECT username FROM parsons.accts");
            stillRunning = false;
        }

        public List<HashMap<String, String>> openUrlAccounts(String stmt) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.kidsavings.org/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            ProxyStmt proxy = retrofit.create(ProxyStmt.class);
            Call<List<Post>> call = proxy.getPosts(
                    BCrypt.hashpw(String.valueOf(getEpoch()), BCrypt.gensalt()),
                    stmt);
            List<HashMap<String, String>> returnable = new ArrayList<>();
            try {
                Response<List<Post>> response = call.execute();
                List<Post> posts = response.body();
                if (posts == null) {
                    HashMap<String, String> content = new HashMap<>();
                    content.put("Response", "Failed");
                    returnable.add(content);
                } else {
                    for (Post post : posts) {
                        HashMap<String, String> content = new HashMap<>();
                        content.put("username", post.getUsername());
                        returnable.add(content);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return openUrlAccounts(stmt);
            }
            return returnable;
        }


        public int getEpoch() {
            long seconds = System.currentTimeMillis() / 1000;
            return (int) seconds;
        }
    }
}