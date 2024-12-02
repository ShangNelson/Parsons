package com.parsons.bakery.baker.ui.chat;

import static com.google.firebase.messaging.Constants.MessagePayloadKeys.SENDER_ID;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.Post;
import com.parsons.bakery.ProxyChat;
import com.parsons.bakery.ProxyStmt;
import com.parsons.bakery.ProxyStmtSend;
import com.parsons.bakery.R;
import com.parsons.bakery.databinding.ActivityChatBinding;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Chat extends AppCompatActivity {

    private ActivityChatBinding binding;
    DBHandler dbHandler;
    public String name;
    public String id;
    public String stringedMessages;
    String me;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbHandler = new DBHandler(this);
        Bundle values = getIntent().getExtras();
        name = values.getString("name");
        id = values.getString("id");
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setTitle(name);
        me = dbHandler.executeOne("SELECT unique_id FROM acct").get(0).get("unique_id");

        RecyclerView view = findViewById(R.id.recycler);

        LinearLayoutManager ln = new LinearLayoutManager(this);
        view.setLayoutManager(ln);
        MessageAdapter adapter = new MessageAdapter(this, getData());
        view.setAdapter(adapter);
        EditText editText = findViewById(R.id.sendText);
        ImageButton sendText = findViewById(R.id.send);

        sendText.setOnClickListener(view1 -> {
            System.out.println(editText.getText().toString() + ", " + stringedMessages);
            if (!editText.getText().toString().equals("") && !contains(editText.getText().toString(), '~')) {
                System.out.println("Not Blank");
                if (!stringedMessages.equals("No Messages")) {
                    System.out.println("Previous Messages");
                    stringedMessages = "{" + editText.getText().toString() + "~" + me + "}|" + stringedMessages;
                } else {
                    System.out.println(" No Previous Messages");
                    stringedMessages = "{" + editText.getText().toString() + "~" + me + "}";
                }
                dbHandler.executeOne("UPDATE people SET past_messages='" + stringedMessages + "' WHERE unique_id='" + id + "'");
                new sendChat(editText.getText().toString(), id, me).execute();
                sendMessageToTopic(id, editText.getText().toString(), me);
                adapter.updateInfo(getData());
                ln.scrollToPosition(adapter.getItemCount()-1);
                editText.setText("");
            } else if (contains(editText.getText().toString(), '~')) {
                Snackbar.make(binding.getRoot(), "Cannot include special character '~'", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
    // In your activity or service class
    private void sendMessageToTopic(String topic, String message, String sender) {
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder("1091222427757@fcm.googleapis.com")
                .setMessageId(sender)
                .addData("message", message)
                .addData("title", "New Message!")
                .addData("sender", sender)
                .build());
    }

    // Example usage
    public boolean contains(String string, char regex) {
        char[] characters = string.toCharArray();
        for (char check : characters) {
            if (check == regex) {
                return true;
            }
        }
        return false;
    }

    public List<Message> getData() {
        List<HashMap<String, String>> messages = dbHandler.executeOne("SELECT past_messages FROM people WHERE unique_id='" + id + "'");
        List<Message> individualMessageList = new ArrayList<>();
        stringedMessages = messages.get(0).get("past_messages");
        String[] individuals = stringedMessages.split("\\|");
        for (String message : individuals) {
            System.out.println(message);
            if (!message.equals("No Messages")) {
                message = message.substring(1);
                String[] twoValues = message.split("~");
                individualMessageList.add(new Message(twoValues[0], twoValues[1]));
            } else {
                individualMessageList.add(new Message("No Messages", "No Messages"));
            }
        }
        Collections.reverse(individualMessageList);
        return individualMessageList;
    }

    static class sendChat extends AsyncTask {
        String message;
        String topic;
        String sender;
        public sendChat(String message, String topic, String sender) {
            this.message = message;
            this.topic = topic;
            this.sender = sender;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            System.out.println("Sending new message");
            send(message, topic, sender, 5);
            return null;
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
                System.out.println("Failed with " + (tries-1) + " left");
                return send(message, topic, sender,tries-1);
            }
            return returnable;
        }

        public int getEpoch() {
            long seconds = System.currentTimeMillis() / 1000;
            return (int) seconds;
        }
    }
}