package com.parsons.bakery.client;

import android.content.Context;
import android.content.ContextWrapper;
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


import com.parsons.bakery.DBHandler;
import com.parsons.bakery.OutlineDrawable;
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
            Runnable r = new runner(feedItem.getUrl(), feedItem.getName(), customViewHolder.image);
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

    class runner implements Runnable {
        String url;
        ImageView imageView;
        String name;

        public runner(String url, String name, ImageView imageView) {
            this.url = url;
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
