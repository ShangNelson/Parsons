package com.parsons.bakery.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.Post;
import com.parsons.bakery.ProxyStmtSend;
import com.parsons.bakery.R;

import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class sendingOrder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_order);
        Bundle bundle = getIntent().getExtras();
        Thread newThread = new Thread(new sendOrder(bundle.getString("order"), this, bundle.getString("time"), bundle.getBoolean("verification")));
        newThread.start();
    }

    static class sendOrder implements Runnable {
        private final String order;
        private final Context context;
        private final String time;
        private final boolean verification;

        public sendOrder(String order, Context context, String time, boolean verification) {
            this.order = order;
            this.context = context;
            this.time = time;
            this.verification = verification;
        }

        @Override
        public void run() {
            List<HashMap<String, String>> returns = sendOrder(order, 5, verification);
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("justSent", true);
            if (returns == null) {
                intent.putExtra("results", "An error occurred in trying to send your order.");
            } else {
                if (returns.get(0).get("Response").equals("false")) {
                    intent.putExtra("results", "An error occurred in trying to send your order.");
                } else {
                    intent.putExtra("results", "Your order was successfully sent.");
                    DBHandler dbHandler = new DBHandler(context);
                    dbHandler.executeOne("DELETE FROM cart");
                    dbHandler.executeOne("INSERT INTO previous_orders (theOrder,time) VALUES ('" + order + "','" + time + "')");
                }
            }
            context.startActivity(intent);
        }

        public List<HashMap<String, String>> sendOrder(String order, int tries, boolean verified) {
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
            Instant instant = Instant.ofEpochSecond(epoch);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
            String dateTimeString = formatter.format(instant);
            int verification = 0;
            if (verified) {
                verification = 1;
            }
            Call<List<Post>> call = proxy.getPosts(
                    BCrypt.hashpw(String.valueOf(epoch), BCrypt.gensalt()),
                    "INSERT INTO parsons.orders (order_placed,time_placed,needs_verification) VALUES ('" + order + "','" + dateTimeString + "'," + verification + ")",
                    "parsons.orders");
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
                return sendOrder(order, tries-1, verified);
            }
            return returnable;
        }

        public int getEpoch() {
            long seconds = System.currentTimeMillis() / 1000;
            return (int) seconds;
        }
    }

}