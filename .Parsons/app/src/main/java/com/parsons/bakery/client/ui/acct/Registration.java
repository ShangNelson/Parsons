package com.parsons.bakery.ui.acct;

import androidx.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.Loading;
import com.parsons.bakery.Post;
import com.parsons.bakery.ProxyStmtSend;
import com.parsons.bakery.R;
import com.parsons.bakery.client.MainActivity;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Registration extends AppCompatActivity {

    public EditText emailTextView, passwordTextView, name;
    public Button Btn;
    public ProgressBar progressbar;
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        // initialising all views through id defined above
        emailTextView = findViewById(R.id.email);
        passwordTextView = findViewById(R.id.passwd);
        Btn = findViewById(R.id.btnregister);
        progressbar = findViewById(R.id.progressbar);
        name = findViewById(R.id.name);

        // Set on Click Listener on Registration button
        Btn.setOnClickListener(v -> registerNewUser(this));
    }

    private void registerNewUser(Context context) {
        progressbar.setVisibility(View.VISIBLE);
        String email, password;
        email = emailTextView.getText().toString();
        password = passwordTextView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email!!", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!!", Toast.LENGTH_LONG).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                Runnable newRunnable = new doSignUp(email, name.getText().toString(), context, mAuth.getCurrentUser().getUid());
                Thread thread = new Thread(newRunnable);
                thread.start();
            } else {
                Toast.makeText(getApplicationContext(), "Registration failed!!" + " Please try again later", Toast.LENGTH_LONG).show();
                progressbar.setVisibility(View.GONE);
            }
        });
    }

    class doSignUp implements Runnable {

        String username;
        String name;
        Context context;
        String UUID;

        public doSignUp(String username, String name, Context context, String UUID) {
            this.username = username;
            this.name = name;
            this.context = context;
            this.UUID = UUID;
        }

        @Override
        public void run() {
            List<HashMap<String, String>> returns = sendNewAccount(name, username, UUID,5);
            Intent intent = new Intent(context, Loading.class);
            intent.putExtra("signedUp", true);
            if (!returns.isEmpty()) {
                if (returns.get(0).get("Response").equals("true")) {
                    intent.putExtra("results", "Sign up Successful.");
                    DBHandler dbHandler = new DBHandler(context);
                    dbHandler.executeOne("DELETE FROM acct");
                    dbHandler.executeOne("INSERT INTO acct (name,username,unique_id) VALUES ('" + name + "','" + username + "','" + UUID + "')");
                    FirebaseMessaging.getInstance().subscribeToTopic(UUID);
                } else {
                    intent.putExtra("results", "Sign up Failed.");
                }
            }
            ContextCompat.getMainExecutor(context).execute(() -> {
                progressbar.setVisibility(View.GONE);
                context.startActivity(intent);
            });
        }

        public List<HashMap<String, String>> sendNewAccount(String name, String username, String UUID, int tries) {
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

            Call<List<Post>> call = proxy.getPosts(
                    BCrypt.hashpw(String.valueOf(getEpoch()), BCrypt.gensalt()),
                    "INSERT INTO parsons.accts (name, username, unique_id) VALUES ('" + name + "','" + username + "','" + UUID + "')",
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
                return sendNewAccount(name, username, UUID, tries-1);
            }
            return returnable;
        }

        public int getEpoch() {
            long seconds = System.currentTimeMillis() / 1000;
            return (int) seconds;
        }
    }
}