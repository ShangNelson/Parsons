package com.parsons.bakery.ui.cart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.MainActivity;
import com.parsons.bakery.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class sendingOrder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_order);
        Bundle bundle = getIntent().getExtras();

        //Thread newThread = new Thread(new sendOrder((Bundle) bundle.get("order"), this, bundle.getString("time")));
        //newThread.start();

        Task<Boolean> sendOrder = ProcessTaskWrapper.wrapAddOrder((Bundle)bundle.get("order"), FirebaseFirestore.getInstance(), getEpoch(), this);
        sendOrder.addOnCompleteListener(success -> {
            if (sendOrder.isSuccessful()) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("justSent", true);
                intent.putExtra("results", "Your order was successfully sent.");
                DBHandler dbHandler = new DBHandler(this);
                dbHandler.executeOne("DELETE FROM cart");
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("results", "An error occurred in trying to send your order.");
                startActivity(intent);
            }
        });

    }
    //public sendOrder(Bundle order, Context context, String time, boolean verification) {
    public static void sendOrder(Bundle order, FirebaseFirestore db, int epoch, Context context, Callback<Boolean> callback) {
        Instant instant = Instant.ofEpochSecond(epoch);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
        String dateTimeString = formatter.format(instant);
        Map<String, Object> data = new HashMap<>();
        //String SQL = "INSERT INTO parsons.orders (order_placed,time_placed,needs_verification) VALUES ('" + order + "','" + dateTimeString + "'," + verification + ")", ;
        if (Integer.parseInt(new DBHandler(context).executeOne("SELECT " + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + " FROM " + DBHandler.TABLE_ACCT).get(0).get(DBHandler.COLUMN_ACCT_ACCOUNT_TYPE)) != -1) {
            data.put("userId", new DBHandler(context).executeOne("SELECT " + DBHandler.COLUMN_ACCT_UNIQUE_ID + " FROM " + DBHandler.TABLE_ACCT).get(0).get(DBHandler.COLUMN_ACCT_UNIQUE_ID));
        } else {
            data.put("userID", "null");
        }
        data.put("current", 1);
        data.put("price", order.getDouble("price"));
        data.put("name", order.getString("name"));
        data.put("number", order.getString("number"));
        data.put("time", order.getString("time"));
        data.put("timePlaced", order.getString("timePlaced"));
        data.put("numberOfItems", order.getInt("numberOfItems"));

        db.collection("orders")
                .add(data)
                .addOnSuccessListener(orderDocumentReference -> {
                    System.out.println("Successfully added order");

                    // List to track all the order item tasks
                    List<Task<DocumentReference>> orderItemTasks = new ArrayList<>();

                    // Loop through order items and add them to "orderSpecifications"
                    for (int i = 0; i < order.getInt("numberOfItems"); i++) {
                        System.out.println("Cycling and adding Items to collection");
                        Map<String, Object> orderItem = new HashMap<>();
                        orderItem.put("count", order.getBundle("orderItem" + i).getInt("count"));
                        orderItem.put("type", order.getBundle("orderItem" + i).getString("type"));
                        orderItem.put("item", order.getBundle("orderItem" + i).getString("item"));
                        orderItem.put("customizations", order.getBundle("orderItem" + i).getString("customizations"));

                        // Add the order item and add its task to the list
                        Task<DocumentReference> task = orderDocumentReference.collection("orderSpecifications").add(orderItem)
                                .addOnSuccessListener(v -> System.out.println("Successfully added item" + orderItem.get("item")))
                                .addOnFailureListener(callback::onFailure);
                        orderItemTasks.add(task);
                    }

                    // Wait for all tasks to complete
                    Tasks.whenAllComplete(orderItemTasks)
                            .addOnSuccessListener(tasks -> {
                                System.out.println("All order items successfully added.");
                                callback.onComplete(true);  // Call success callback
                            })
                            .addOnFailureListener(callback::onFailure);  // Handle failure if any of the tasks fail
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static class ProcessTaskWrapper {

        public static Task<Boolean> wrapAddOrder(Bundle order, final FirebaseFirestore db, int epoch, Context context) {
            TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

            // Start the PullMenu process and ensure the task is only completed when all async work is done
            sendOrder(order, db, epoch, context, new Callback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    taskCompletionSource.setResult(result);  // Complete task with the result
                }

                @Override
                public void onFailure(Exception e) {
                    taskCompletionSource.setException(e);        // Complete task with an error if something fails
                }
            });

            // Return the Task object so it can be managed like a Firestore task
            return taskCompletionSource.getTask();
        }
    }

    public interface Callback<T> {
        void onComplete(T result);     // Called when the operation completes successfully
        void onFailure(Exception e);   // Called when there's an error
    }
    public int getEpoch() {
        long seconds = System.currentTimeMillis() / 1000;
        return (int) seconds;
    }
}