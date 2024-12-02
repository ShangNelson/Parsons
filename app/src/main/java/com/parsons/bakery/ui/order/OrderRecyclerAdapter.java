package com.parsons.bakery.ui.order;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class OrderRecyclerAdapter extends RecyclerView.Adapter<OrderRecyclerAdapter.CustomViewHolder> {
    private final List<OrderItem> itemList;
    private final Context mContext;

    public OrderRecyclerAdapter(Context context, List<OrderItem> feedItemList) {
        this.itemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_item, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        OrderItem feedItem = itemList.get(i);
        customViewHolder.name.setText(feedItem.getName());
        Runnable r = new runner(feedItem.getUrl(), customViewHolder.image, mContext, feedItem.getName(), feedItem.getHas());
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.start();
    }


    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected ImageView image;

        public CustomViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.categoryName);
            image = view.findViewById(R.id.categoryImage);
        }
    }
    static class runner implements Runnable {
        String url;
        ImageView view;
        Context context;
        String name;
        int has;

        public runner(String url, ImageView view, Context context, String name, int has) {
            this.url = url;
            this.view = view;
            this.context = context;
            this.name = name;
            this.has = has;
        }

        @Override
        public void run() {
            //System.out.println(new ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE).getPath() + "/" + url.split("/")[url.split("/").length - 1]);
            Bitmap b = loadImageFromStorage(new ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]);
            System.out.println("Loading from Device: " + b);
            ContextCompat.getMainExecutor(context).execute(() -> view.setImageBitmap(b));

            ContextCompat.getMainExecutor(context).execute(() -> {
                view.setOnClickListener(view -> {
                    Intent intent = new Intent(context, CategorySection.class);
                    intent.putExtra("url", url);
                    intent.putExtra("name", name);
                    intent.putExtra("has_inner_categories", has);
                    context.startActivity(intent);
                });
            });

        }

        private Bitmap loadImageFromStorage(String path, String file) {
            try {
                String[] nameSplit = file.split("/");
                String usedName = nameSplit[nameSplit.length-1];
                File f = new File(path, usedName);
                DBHandler db = new DBHandler(context);
                long seconds = System.currentTimeMillis() / 1000;
                boolean has = false;
                List<HashMap<String, String>> returnedRecents = db.executeOne("SELECT * FROM downloaded");
                for (HashMap<String, String> hashy : returnedRecents) {
                    if (hashy.get("url").equals(usedName)) {
                        has = true;
                    }
                }
                if (has) {
                    db.executeOne("UPDATE downloaded SET lastAccessed='" + seconds + "' WHERE url='" + usedName + "'");
                } else {
                    db.executeOne("INSERT INTO downloaded (url,lastAccessed) VALUES ('" + usedName + "','" + seconds + "')");
                }
                return BitmapFactory.decodeStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
