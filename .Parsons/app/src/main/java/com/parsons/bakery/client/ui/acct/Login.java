package com.parsons.bakery.ui.acct;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.Loading;
import com.parsons.bakery.Post;
import com.parsons.bakery.ProxyStmt;
import com.parsons.bakery.R;
import com.parsons.bakery.client.MainActivity;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    private EditText emailTextView, passwordTextView;
    private Button Btn;
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
    }

    private void loginUserAccount(Context context) {
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
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Runnable newRunnable = new doSignIn(email, context);
                Thread thisThread = new Thread(newRunnable);
                thisThread.start();
            } else {
                Toast.makeText(getApplicationContext(), "Login failed!!", Toast.LENGTH_LONG).show();
                progressbar.setVisibility(View.GONE);
            }
        });
    }

    class doSignIn implements Runnable {

        String username;
        Context context;

        public doSignIn(String username, Context context) {
            this.username = username;
            this.context = context;
        }

        @Override
        public void run() {
            Looper.prepare();
            List<HashMap<String, String>> returns = openUrlAccounts("SELECT * FROM parsons.accts WHERE username = '" + username + "'");
            DBHandler dbHandler = new DBHandler(context);
            dbHandler.executeOne("INSERT INTO acct (name,username,is_baker,unique_id) VALUES ('" + returns.get(0).get("name") + "','" + returns.get(0).get("username") + "'," + returns.get(0).get("is_baker") + ",'" + returns.get(0).get("unique_id") + "')");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", returns.get(0).get("name"));
            editor.putString("number", returns.get(0).get("number"));
            editor.apply();
            FirebaseMessaging.getInstance().subscribeToTopic(returns.get(0).get("unique_id"));
            System.out.println("Successful");
            Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(context, Loading.class));
            ContextCompat.getMainExecutor(context).execute(() -> progressbar.setVisibility(View.GONE));
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
                        content.put("id", post.getId());
                        content.put("name", post.getName());
                        content.put("username", post.getUsername());
                        content.put("hashed_pass", post.getHashed_pass());
                        content.put("number", post.getNumber());
                        content.put("is_baker", post.getIs_baker());
                        content.put("unique_id", post.getUnique_id());
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