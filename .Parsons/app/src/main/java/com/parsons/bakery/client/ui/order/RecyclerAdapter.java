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

import com.parsons.bakery.client.Category;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.CustomViewHolder> {
    private final List<OrderItem> itemList;
    private final Context mContext;

    public RecyclerAdapter(Context context, List<OrderItem> feedItemList) {
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
    class runner implements Runnable {
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
            System.out.println(new ContextWrapper(mContext).getDir("imageDir", Context.MODE_PRIVATE).getPath() + "/" + url.split("/")[url.split("/").length - 1]);
            if (loadImageFromStorage(new ContextWrapper(mContext).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]) == null) {
                final Drawable d = LoadImageFromWebOperations(url);

                System.out.println("Loading from Web");
                ContextCompat.getMainExecutor(mContext).execute(() -> view.setImageDrawable(d));
            } else {
                Bitmap b = loadImageFromStorage(new ContextWrapper(mContext).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]);
                System.out.println("Loading from Device: " + b);
                ContextCompat.getMainExecutor(mContext).execute(() -> view.setImageBitmap(b));
            }
            ContextCompat.getMainExecutor(mContext).execute(() -> {
                view.setOnClickListener(view -> {
                    Intent intent = new Intent(mContext, Category.class);
                    intent.putExtra("url", url);
                    intent.putExtra("name", name);
                    intent.putExtra("has", has);
                    mContext.startActivity(intent);
                });
            });

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
        private String saveToInternalStorage(Bitmap bitmapImage){
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
            return directory.getAbsolutePath();
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
