package com.parsons.bakery.ui.cart;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.OutlineDrawable;
import com.parsons.bakery.R;
import com.parsons.bakery.ui.order.CategoryRecyclerAdapter;
import com.parsons.bakery.ui.order.MenuCustomization;

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

public class CartRecyclerAdapter extends RecyclerView.Adapter<CartRecyclerAdapter.CustomViewHolder> {
    private final List<CartItem> itemList;
    private final Context mContext;

    public void clear() {
        itemList.clear();
        notifyDataSetChanged();
    }

    public CartRecyclerAdapter(Context context, List<CartItem> feedItemList) {
        this.itemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_item_cart, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        CartItem feedItem = itemList.get(i);
        OutlineDrawable outlineDrawable = new OutlineDrawable(5, Color.BLACK);
        customViewHolder.add.setBackground(outlineDrawable);
        customViewHolder.sub.setBackground(outlineDrawable);
        customViewHolder.name.setText(feedItem.getName());
        customViewHolder.count.setText(feedItem.getCount());
        customViewHolder.count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!customViewHolder.count.getText().toString().equals("") && Integer.parseInt(customViewHolder.count.getText().toString()) > 1) {
                    customViewHolder.dbHandler.executeOne("UPDATE cart SET count=" + (Integer.parseInt(customViewHolder.count.getText().toString())) + " WHERE id=" + feedItem.getId());
                } else if (customViewHolder.count.getText().toString().equals("")) {
                    //TODO prompt invalid
                } else if (Integer.parseInt(customViewHolder.count.getText().toString()) == 0) {
                    //TODO prompt remove
                }
            }
        });

        customViewHolder.customizations.setText(feedItem.getCustomizations());

        customViewHolder.add.setOnClickListener(view -> {
                customViewHolder.dbHandler.executeOne("UPDATE cart SET count=" + (Integer.parseInt(customViewHolder.count.getText().toString()) + 1) + " WHERE id=" + feedItem.getId());
                customViewHolder.count.setText(String.valueOf(Integer.parseInt(customViewHolder.count.getText().toString()) + 1));
        });
        customViewHolder.sub.setOnClickListener(view -> {
            if (Integer.parseInt(customViewHolder.count.getText().toString()) > 1) {
                customViewHolder.dbHandler.executeOne("UPDATE cart SET count=" + (Integer.parseInt(customViewHolder.count.getText().toString()) - 1) + " WHERE id=" + feedItem.getId());
                customViewHolder.count.setText(String.valueOf(Integer.parseInt(customViewHolder.count.getText().toString()) - 1));
            }
        });


        if (!feedItem.getUrl().equals("null")) {
            System.out.println("Trying to set image - " + feedItem.getUrl());
            Runnable r = new runner(feedItem.getUrl(), customViewHolder.image, mContext);
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.start();
        } else {
            System.out.println("Removing the image View");
            ContextCompat.getMainExecutor(mContext).execute(() -> {
                customViewHolder.parentView.removeView(customViewHolder.image);

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) customViewHolder.name.getLayoutParams();
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                customViewHolder.name.setLayoutParams(layoutParams);

                RelativeLayout.LayoutParams layoutParamsRemove = (RelativeLayout.LayoutParams) customViewHolder.removeLayout.getLayoutParams();
                layoutParamsRemove.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                customViewHolder.removeLayout.setLayoutParams(layoutParamsRemove);
            });
        }

    }


    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected ImageView image;
        protected TextView customizations;
        protected EditText count;
        protected TextView add;
        protected TextView sub;
        protected ImageButton remove;
        protected LinearLayout removeLayout;
        protected DBHandler dbHandler;
        protected RelativeLayout parentView;

        public CustomViewHolder(View view) {
            super(view);
            customizations = view.findViewById(R.id.customizations);
            count = view.findViewById(R.id.count);
            name = view.findViewById(R.id.name);
            image = view.findViewById(R.id.referenceImage);
            add = view.findViewById(R.id.add);
            sub = view.findViewById(R.id.sub);
            remove = view.findViewById(R.id.removeFromCart);
            dbHandler = new DBHandler(mContext);
            parentView = view.findViewById(R.id.parentRelative);
            removeLayout = view.findViewById(R.id.linearLayoutRemove);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO prompt remove-
                    int position = getAdapterPosition();
                    dbHandler.executeOne("DELETE FROM cart WHERE id=" + itemList.get(position).getId());
                    removeAt(position);
                }
            });
        }
        public void removeAt(int position) {
            // Remove the item from the data set
            itemList.remove(position);
            // Notify the adapter that the item has been removed
            notifyItemRemoved(position);
            // Notify the adapter that the data set has changed
            notifyItemRangeChanged(position, itemList.size());
        }
    }

    static class runner implements Runnable {
        String url;
        ImageView imageView;
        Context context;

        public runner(String url, ImageView imageView, Context context) {
            this.url = url;
            this.imageView = imageView;
            this.context = context;
        }

        @Override
        public void run() {
            System.out.println(new ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE).getPath() + "/" + url.split("/")[url.split("/").length - 1]);
            Bitmap b = loadImageFromStorage(new ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]);
            System.out.println("Loading from Device: " + b);
            ContextCompat.getMainExecutor(context).execute(() -> imageView.setImageBitmap(b));
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
