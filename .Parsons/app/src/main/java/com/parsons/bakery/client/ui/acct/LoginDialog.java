package com.parsons.bakery.ui.acct;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.Loading;
import com.parsons.bakery.Post;
import com.parsons.bakery.ProxyStmt;
import com.parsons.bakery.R;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginDialog extends DialogFragment {

    Context context;
    public static boolean successful = false;
    AcctFragment frag;
    RelativeLayout not;
    ProgressBar waiter;

    public LoginDialog(Context context, AcctFragment frag, RelativeLayout not) {
        this.context = context;
        this.frag = frag;
        this.not = not;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.login_dialog, null);
        EditText username = view.findViewById(R.id.username);
        EditText password = view.findViewById(R.id.pass);
        waiter = AcctFragment.rootBase.findViewById(R.id.progressBarLogin);
        builder.setView(view)
                .setPositiveButton("Ok", (dialog, id) -> {
                    String newPass = BCrypt.hashpw(password.getText().toString(), BCrypt.gensalt());
                    doSignIn runns = new doSignIn(username.getText().toString().trim(), newPass, password.getText().toString().trim(), context, this);
                    Thread thread = new Thread(runns);
                    thread.start();
                    waiter.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("Cancel", (dialog, id) -> getDialog().dismiss());

        return builder.create();
    }

    class doSignIn implements Runnable {

        String hashedPw;
        String username;
        String pass;
        Context context;
        DialogFragment dialogFragment;

        public doSignIn(String username, String hashedPw, String pass, Context context, DialogFragment dialogFragment) {
            this.username = username;
            this.hashedPw = hashedPw;
            this.pass = pass;
            this.context = context;
            this.dialogFragment = dialogFragment;
        }

        @Override
        public void run() {
            Looper.prepare();
            List<HashMap<String, String>> returns = openUrlAccounts("SELECT * FROM parsons.accts WHERE username = '" + username + "'");
            if (returns.size() == 1) {
                if (BCrypt.checkpw(pass, returns.get(0).get("hashed_pass"))) {
                    DBHandler dbHandler = new DBHandler(context);
                    dbHandler.executeOne("INSERT INTO acct (name,username,is_baker,unique_id) VALUES ('" + returns.get(0).get("name") + "','" + returns.get(0).get("username") + "'," + returns.get(0).get("is_baker") + "," + returns.get(0).get("unique_id") + ")");
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", returns.get(0).get("name"));
                    editor.putString("number", returns.get(0).get("number"));
                    editor.apply();
                    System.out.println("Successful");
                    successful = true;
                }
            }
            if (successful) {
                Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show();


                context.startActivity(new Intent(context, Loading.class));
            } else {
                Toast.makeText(context, "Login Failed", Toast.LENGTH_LONG).show();
            }
            ContextCompat.getMainExecutor(context).execute(() -> waiter.setVisibility(View.GONE));

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