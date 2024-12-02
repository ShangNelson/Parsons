package com.parsons.bakery;

import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parsons.bakery.baker.Baker;
import com.parsons.bakery.client.MainActivity;

import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class update implements Runnable {
    Context context;
    Loading parentClass;
    boolean isBaker;
    public update(Context context, Loading parentClass, boolean isBaker) {
        this.context = context;
        this.parentClass = parentClass;
        this.isBaker = isBaker;
    }
    @Override
    public void run() {
        DBHandler db = new DBHandler(context);
        //GETTING CATEGORIES
        ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("Getting your bakery categories."));
        List<HashMap<String, String>> returnedCategories = openURLCategories("SELECT * FROM parsons.categories");
        db.executeOne("DELETE FROM categories");
        //UPDATING LOCAL CATEGORIES
        ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("Updating your bakery categories."));
        for (HashMap<String, String> map : returnedCategories) {
            db.executeOne("INSERT INTO categories (id,name,img,level,has_levels) VALUES (" + map.get("id") + ",'" + map.get("name") + "','" + map.get("img") + "'," + map.get("level") + "," + map.get("has_levels") +")");
        }
        //GETTING MENU
        ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("Retrieving your menu"));
        List<HashMap<String, String>> returnedMenu = openURLMenu("SELECT * FROM parsons.menu");
        db.executeOne("DELETE FROM menu");
        //UPDATING LOCAL MENU
        ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("Updating your menu"));
        for (HashMap<String, String> map : returnedMenu) {
            String name = map.get("name");
            if (map.get("name").contains("'")) {
                char[] characters = name.toCharArray();
                int place = 0;
                for (char c : characters) {
                    if (c == '\'') {
                        name = addChar(name, '\'', place);
                    }
                    place++;
                }
            }
            String description = map.get("description");
            if (description != null) {
                if (map.get("description").contains("'")) {
                    char[] characters = description.toCharArray();
                    int place = 0;
                    for (char c : characters) {
                        if (c == '\'') {
                            description = addChar(description, '\'', place);
                        }
                        place++;
                    }
                }
            }
            db.executeOne("INSERT INTO menu (id,name,img,category,description,req,inner_category,use_inner,order_of_options) VALUES (" + map.get("id") + ",'" + name + "','" + map.get("img") + "','" + map.get("category") + "','" + description  + "','" + map.get("req") + "','" + map.get("inner_category") + "'," + map.get("use_inner") + "," + map.get("order_of_options") + ")");
        }
        //GETTING ALL CUSTOM OPTIONS
        ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("Retrieving your available customizations"));
        List<HashMap<String, String>> returnedCustomizations = openURLCustomization( "SELECT * FROM parsons.customizations");
        db.executeOne("DELETE FROM customizations");
        //UPDATING CUSTOM OPTIONS
        ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("Updating your available customizations."));
        for (HashMap<String, String> map : returnedCustomizations) {
            db.executeOne("INSERT INTO customizations (id,type,options,item,title,order_of_options,is_required) VALUES (" + map.get("id") + ",'" + map.get("type") + "','" + map.get("options") + "','" + map.get("item") + "','" + map.get("title") + "'," + map.get("order_of_options") + "," + map.get("is_required") + ")");
        }


        //REMOVING OBSOLETE IMAGES
        ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("Getting rid of useless images."));
        List<HashMap<String, String>> downloadedImages = db.executeOne("SELECT * FROM downloaded");
        for (HashMap<String, String> image : downloadedImages) {
            if (((System.currentTimeMillis()/1000)-Integer.parseInt(image.get("lastAccessed"))) > 2592000) {
                File currentImage = new File(image.get("url"));
                try {
                    if (currentImage.delete()) {
                        System.out.println("Deleted Image Successfully");
                    } else {
                        System.out.println("An issue occurred");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                db.executeOne("DELETE FROM downloaded WHERE url='" + image.get("url") + "'");
            }
        }
        List<HashMap<String, String>> returns = db.executeOne("SELECT * FROM acct");
        //IF LOGGED IN
        if (!returns.isEmpty()) {
            //GETTING ALL CONVERSATIONS
            String myId = returns.get(0).get("unique_id");
            String name = returns.get(0).get("name");
            List<HashMap<String, String>> convos = openUrlPersons(
                    "Select\n" +
                    "    accts.name,\n" +
                    "    chats.recipient,\n" +
                    "    chats.sender,\n" +
                    "    chats.history,\n" +
                    "    accts1.name As name1\n" +
                    "From\n" +
                    "    parsons.chats Inner Join\n" +
                    "    parsons.accts On (chats.recipient = accts.unique_id\n" +
                    "                Or chats.sender = accts.unique_id)\n" +
                    "            And chats.sender = accts.unique_id Inner Join\n" +
                    "    parsons.accts accts1 On chats.recipient = accts1.unique_id");
            db.executeOne("DELETE FROM people");
            if (!convos.isEmpty()) {
                for (HashMap<String, String> chat : convos) {
                    if (chat.get("recipient").equals(myId)) {
                        db.executeOne("INSERT INTO people (name,past_messages,unique_id) VALUES ('" + chat.get("name") + "','" + chat.get("history") + "','" + chat.get("sender") + "')");
                    } else {
                        db.executeOne("INSERT INTO people (name,past_messages,unique_id) VALUES ('" + chat.get("name1") + "','" + chat.get("history") + "','" + chat.get("recipient") + "')");
                    }
                }
            }


            if (Integer.parseInt(db.executeOne("SELECT * FROM acct").get(0).get("is_baker")) == 1) {
                //GETTING ORDERS FOR BAKERS
                ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("Retrieving all orders, baker."));
                List<HashMap<String, String>> returnedOrders = openURLOrders("SELECT * FROM parsons.orders");
                db.executeOne("DELETE FROM orders");
                //UPDATING ORDERS
                ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("Updating your orders, baker."));
                for (HashMap<String, String> order : returnedOrders) {
                    db.executeOne("INSERT INTO orders (id,order_placed,time_placed,needs_verification) VALUES (" + order.get("id") + ",'" + order.get("order_placed") + "','" + order.get("time_placed") + "'," + order.get("needs_verification") + ")");
                }
            }
        }



        db.close();
        //FINISHED
        ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("All Set!"));
        if (!isBaker) {
            context.startActivity(new Intent(context, MainActivity.class));
        } else {
            context.startActivity(new Intent(context, Baker.class));
        }
    }

    public String addChar(String str, char ch, int position) {
        return str.substring(0, position) + ch + str.substring(position);
    }

    public List<HashMap<String, String>> openURLCategories(String stmt) {
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
                    content.put("img", post.getImg());
                    content.put("level", post.getLevel());
                    content.put("has_levels", post.getHas_levels());
                    returnable.add(content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return openURLCategories(stmt);
        }
        return returnable;
    }

    public List<HashMap<String, String>> openURLOrders(String stmt) {
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
                    content.put("order_placed", post.getOrder_placed());
                    content.put("time_placed", post.getTime_placed());
                    content.put("needs_verification", post.getNeeds_verification());
                    returnable.add(content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return openURLOrders(stmt);
        }
        return returnable;
    }


    public List<HashMap<String, String>> openURLMenu(String stmt) {
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
                    content.put("category", post.getCategory());
                    content.put("img", post.getImg());
                    content.put("description", post.getDescription());
                    content.put("req", post.getReq());
                    content.put("inner_category", post.getInner_category());
                    content.put("use_inner", post.getUse_inner());
                    content.put("order_of_options", post.getOrder_of_options());
                    returnable.add(content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return openURLMenu(stmt);
        }
        return returnable;
    }

    public List<HashMap<String, String>> openUrlPersons(String stmt) {
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
                    content.put("recipient", post.getRecipient());
                    content.put("sender", post.getSender());
                    content.put("history", post.getHistory());
                    content.put("name", post.getName());
                    content.put("name1", post.getName1());
                    returnable.add(content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return openUrlPersons(stmt);
        }
        return returnable;
    }

    public List<HashMap<String, String>> openURLCustomization(String stmt) {
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
                    content.put("type", post.getType());
                    content.put("options", post.getOptions());
                    content.put("item", post.getItem());
                    content.put("title", post.getTitle());
                    content.put("order_of_options", post.getOrder_of_options());
                    content.put("is_required", post.getIs_required());
                    returnable.add(content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return openURLCustomization(stmt);
        }
        return returnable;
    }

    public int getEpoch() {
        long seconds = System.currentTimeMillis() / 1000;
        return (int) seconds;
    }

}