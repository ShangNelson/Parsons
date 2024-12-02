package com.parsons.bakery.client;

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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;
import com.parsons.bakery.ui.order.OrderItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class RecyclerAdapterCategory extends RecyclerView.Adapter<RecyclerAdapterCategory.CustomViewHolder> {
    private final List<OrderItem> itemList;
    private final Context mContext;

    public RecyclerAdapterCategory(Context context, List<OrderItem> feedItemList) {
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
            if (!feedItem.getDescription().equals("null")) {
                customViewHolder.description.setText(feedItem.getDescription());
            } else {
                customViewHolder.parentView.removeView(customViewHolder.description);
                customViewHolder.name.setGravity(RelativeLayout.CENTER_VERTICAL);
            }
            if (feedItem.getUrl().equals("") || feedItem.getUrl() == null || feedItem.getUrl().equals("null")) {
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
                Runnable r = new runner(feedItem.getUrl(), viewsToClick, feedItem.getName(), customViewHolder.image);
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


    class runner implements Runnable {
        String url;
        View[] view;
        ImageView imageView;
        String name;

        public runner(String url, View[] view, String name, ImageView imageView) {
            this.url = url;
            this.view = view;
            this.name = name;
            this.imageView = imageView;
        }

        @Override
        public void run() {
            System.out.println(new ContextWrapper(mContext).getDir("imageDir", Context.MODE_PRIVATE).getPath() + "/" + url.split("/")[url.split("/").length - 1]);
            if (loadImageFromStorage(new ContextWrapper(mContext).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]) == null) {
                final Drawable d = LoadImageFromWebOperations(url);
                System.out.println("Loading from Web");
                ContextCompat.getMainExecutor(mContext).execute(() -> imageView.setImageDrawable(d));
            } else {
                Bitmap b = loadImageFromStorage(new ContextWrapper(mContext).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]);
                System.out.println("Loading from Device: " + b);
                ContextCompat.getMainExecutor(mContext).execute(() -> imageView.setImageBitmap(b));
            }
            for (View thisView : view) {
                ContextCompat.getMainExecutor(mContext).execute(() -> thisView.setOnClickListener(view -> {
                    Intent intent = new Intent(mContext, MenuCustomization.class);
                    intent.putExtra("name", name);
                    intent.putExtra("url", url);
                    mContext.startActivity(intent);
                }));
            }
        }
        private Bitmap loadImageFromStorage(String path, String file)
        {
            try {
                File f=new File(path, file);
                DBHandler db = new DBHandler(mContext);
                String name = path + "/" + file;
                long seconds = System.currentTimeMillis() / 1000;
                boolean has = false;
                List<HashMap<String, String>> returnedRecents = db.executeOne("SELECT * FROM downloaded");
                for (HashMap<String, String> hashy : returnedRecents) {
                    if (hashy.get("url").equals(name)) {
                        has = true;
                    }
                }
                if (has) {
                    db.executeOne("UPDATE downloaded SET lastAccessed='" + seconds + "' WHERE url='" + name + "'");
                } else {
                    db.executeOne("INSERT INTO downloaded (url,lastAccessed) VALUES ('" + name + "','" + seconds + "')");
                }
                return BitmapFactory.decodeStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        private void saveToInternalStorage(Bitmap bitmapImage){
            ContextWrapper cw = new ContextWrapper(mContext);
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath=new File(directory,url.split("/")[url.split("/").length - 1]);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            DBHandler db = new DBHandler(mContext);
            String name = directory + "/" + url.split("/")[url.split("/").length - 1];
            long seconds = System.currentTimeMillis() / 1000;
            boolean has = false;
            List<HashMap<String, String>> returnedRecents = db.executeOne("SELECT * FROM downloaded");
            for (HashMap<String, String> hashy : returnedRecents) {
                if (hashy.get("url").equals(name)) {
                    has = true;
                }
            }
            if (has) {
                db.executeOne("UPDATE downloaded SET lastAccessed='" + seconds + "' WHERE url='" + name + "'");
            } else {
                db.executeOne("INSERT INTO downloaded (url,lastAccessed) VALUES ('" + name + "','" + seconds + "')");
            }
            directory.getAbsolutePath();
        }
        public Drawable LoadImageFromWebOperations(String url) {
            try {
                InputStream is = (InputStream) new URL(url).getContent();
                Drawable d = Drawable.createFromStream(is, "src name");
                saveToInternalStorage(drawableToBitmap(d));
                return d;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        public Bitmap drawableToBitmap (Drawable drawable) {
            Bitmap bitmap = null;

            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if(bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap();
                }
            }

            if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }


}
