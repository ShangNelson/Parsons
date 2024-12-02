package com.parsons.bakery.ui.order;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
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

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.CustomViewHolder> {
    private final List<OrderItem> itemList;
    private final Context mContext;

    public CategoryRecyclerAdapter(Context context, List<OrderItem> feedItemList) {
        this.itemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        // Return a different view type based on the position of the item
        if (!itemList.get(position).getInner_category().equals("label")) {
            return 1;
        } else {
            return 2;
        }
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_item_category, null);
            System.out.println("inflating normal");
            return new CustomViewHolder(view, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_item_category_label, null);
            return new CustomViewHolder(view, true);
        }
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        OrderItem feedItem = itemList.get(i);
        if (!customViewHolder.isLabel) {
            customViewHolder.name.setText(feedItem.getName());
            View[] viewsToClick = new View[3];
            viewsToClick[0] = customViewHolder.card;
            viewsToClick[1] = customViewHolder.description;
            viewsToClick[2] = customViewHolder.name;
            if (feedItem.getDescription() != null) {
                customViewHolder.description.setText(feedItem.getDescription());
            } else {
                customViewHolder.parentView.removeView(customViewHolder.description);
                customViewHolder.name.setGravity(RelativeLayout.CENTER_VERTICAL);
            }
            if (feedItem.getUrl() == null || feedItem.getUrl().equals("") || feedItem.getUrl().equals("null")) {
                customViewHolder.description.setTextColor(Color.BLACK);
                for (View thisView : viewsToClick) {
                    ContextCompat.getMainExecutor(mContext).execute(() -> thisView.setOnClickListener(view -> {
                        Intent intent = new Intent(mContext, MenuCustomization.class);
                        intent.putExtra("name", feedItem.getName());
                        intent.putExtra("url", feedItem.getUrl());
                        intent.putExtra("inner_category", feedItem.getInner_category());
                        mContext.startActivity(intent);
                    }));
                }
            } else {
                customViewHolder.description.setTextColor(Color.WHITE);
                customViewHolder.image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int width = customViewHolder.image.getWidth();
                        customViewHolder.image.getLayoutParams().height = width; // Set height equal to width
                        customViewHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP); // Ensure cropping
                        customViewHolder.image.requestLayout();

                        // Remove the listener to avoid multiple calls
                        customViewHolder.image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
                Runnable r = new runner(feedItem.getUrl(), viewsToClick, feedItem.getName(), customViewHolder.image, mContext);
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.start();
            }
        } else {
            customViewHolder.label.setText(feedItem.getLabel());
        }
    }


    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }


    static class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected ImageView image;
        protected TextView description;
        protected CardView card;
        protected RelativeLayout parentView;
        protected TextView label;
        protected boolean isLabel;

        public CustomViewHolder(View view, boolean isLabel) {
            super(view);
            this.isLabel = isLabel;
            if (!isLabel) {
                description = view.findViewById(R.id.description);
                name = view.findViewById(R.id.categoryName);
                image = view.findViewById(R.id.categoryImage);
                card = view.findViewById(R.id.card);
                parentView = view.findViewById(R.id.relative);
            } else {
                label = view.findViewById(R.id.label);
            }
        }
    }


    static class runner implements Runnable {
        String url;
        View[] view;
        ImageView imageView;
        String name;
        Context context;

        public runner(String url, View[] view, String name, ImageView imageView, Context context) {
            this.url = url;
            this.view = view;
            this.name = name;
            this.imageView = imageView;
            this.context = context;
        }

        @Override
        public void run() {
            System.out.println(new ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE).getPath() + "/" + url.split("/")[url.split("/").length - 1]);
            Bitmap b = loadImageFromStorage(new ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]);
            System.out.println("Loading from Device: " + b);
            ContextCompat.getMainExecutor(context).execute(() -> imageView.setImageBitmap(b));

            for (View thisView : view) {
                ContextCompat.getMainExecutor(context).execute(() -> thisView.setOnClickListener(view -> {
                    Intent intent = new Intent(context, MenuCustomization.class);
                    intent.putExtra("name", name);
                    intent.putExtra("url", url);
                    context.startActivity(intent);
                }));
            }
        }
        private Bitmap loadImageFromStorage(String path, String file)
        {
            try {
                String[] nameSplit = file.split("/");
                String usedName = nameSplit[nameSplit.length-1];
                File f=new File(path, usedName);
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

    public interface Callback<T> {
        void onComplete(T result);     // Called when the operation completes successfully
        void onFailure(Exception e);   // Called when there's an error
    }


}
