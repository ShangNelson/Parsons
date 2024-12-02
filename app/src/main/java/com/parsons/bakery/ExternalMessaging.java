package com.parsons.bakery;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.StrictMode;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExternalMessaging extends FirebaseMessagingService {
    public static final String TAG = "TOKEN";

    public ExternalMessaging() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody() + ", Sending Notification now.");
            Intent intent = new Intent(this, Loading.class);
            showNotification(this, "New Message!", remoteMessage.getNotification().getBody(), intent, 0);

            DBHandler dbHandler = new DBHandler(this);
            List<HashMap<String, String>> results= dbHandler.executeOne("SELECT * FROM people WHERE unique_id='" + remoteMessage.getNotification().getTitle() + "'");
            String me = dbHandler.executeOne("SELECT unique_id FROM acct").get(0).get("unique_id");
            String myConvo = "No Messages";
            if (results.isEmpty()) {
                dbHandler.executeOne("INSERT INTO people (name,past_messages,unique_id) VALUES ('" + remoteMessage.getData().get("name") + "','No Messages','" + remoteMessage.getData().get("sender") + "')");
            } else {
                myConvo = results.get(0).get("past_messages");
            }
            if (!myConvo.equals("No Messages")) {
                System.out.println("Previous Messages");
                myConvo = "{" + remoteMessage.getNotification().getBody() + "~" + me + "}|" + myConvo;
            } else {
                System.out.println(" No Previous Messages");
                myConvo = "{" + remoteMessage.getNotification().getBody() + "~" + me + "}";
            }

            dbHandler.executeOne("UPDATE people SET past_messages='" + myConvo + "' WHERE unique_id='" + remoteMessage.getData().get("sender") + "'");
            System.out.println(remoteMessage.getNotification().getTitle() + ", " + remoteMessage.getNotification().getBody() + ", " + myConvo + ", " + me + ";");
            //ContextCompat.getMainExecutor(this).execute(() -> new Thread(new sendChat(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle(), me)).start());
        }
    }

    public void showNotification(Context context, String title, String message, Intent intent, int reqCode) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_IMMUTABLE);
        String CHANNEL_ID = "messages";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.send)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence name = "Messages";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        notificationManager.createNotificationChannel(mChannel);
        notificationManager.notify(reqCode, notificationBuilder.build()); // 0 is the request code, it should be unique id

        Log.d("showNotification", "showNotification: " + reqCode);
    }

    static class sendChat implements Runnable {
        String message;
        String topic;
        String sender;
        public sendChat(String message, String topic, String sender) {
            this.message = message;
            this.topic = topic;
            this.sender = sender;
        }

        @Override
        public void run() {
            System.out.println("Sending new message");
            send(message, topic, sender, 5);
        }

        public List<HashMap<String, String>> send(String message, String topic, String sender, int tries) {
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
            ProxyChat proxy = retrofit.create(ProxyChat.class);
            Call<List<Post>> call = proxy.getPosts(
                    BCrypt.hashpw(String.valueOf(getEpoch()), BCrypt.gensalt()),
                    message,
                    topic,
                    "true",
                    sender);
            List<HashMap<String, String>> returnable = new ArrayList<>();
            try {
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
                return send(message, topic, sender, tries-1);
            }
            return returnable;
        }

        public int getEpoch() {
            long seconds = System.currentTimeMillis() / 1000;
            return (int) seconds;
        }
    }
}