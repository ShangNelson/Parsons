package com.parsons.bakery.ui.acct;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;
import com.parsons.bakery.ui.bakersOrders.OrderItemRecyclerAdapter;
import com.parsons.bakery.ui.order.CategoryRecyclerAdapter;

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
import java.util.Map;

public class PreviousOrdersRecyclerAdapter extends RecyclerView.Adapter<PreviousOrdersRecyclerAdapter.CustomViewHolder> {

    private final List<List<HashMap<String, String>>> orderList;
    private final Context mContext;

    public PreviousOrdersRecyclerAdapter(Context context, List<List<HashMap<String,String>>> orderList) {
        this.orderList = orderList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.past_order_recycler_item, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        List<HashMap<String, String>> order = orderList.get(i);
        /* ORDER
            * Most item
            * Order Date
            * Image Directory
            * Price (TODO)
            * Other Item Count
            * 2nd item (if exists)
            * 3rd item (if exists)
            * Order ID (For reorder)
            * STATEMENT:
                * SELECT oi.item,o.time_pickup,m.img,oi.order_id,oi.count,o.price,o.number_of_items
                * FROM order_items oi
                * JOIN menu m ON m.name=oi.item
                * JOIN orders o ON o.order_id=oi.order_id
                * ORDER BY o.time_pickup DESC,o.order_id,oi.count DESC
         */

        customViewHolder.itemName.setText(order.get(0).get(DBHandler.COLUMN_ORDER_ITEMS_COUNT) + " " + order.get(0).get(DBHandler.COLUMN_ORDER_ITEMS_ITEM));
        customViewHolder.orderDate.setText(order.get(0).get(DBHandler.COLUMN_ORDERS_TIME_PICKUP));
        customViewHolder.itemPrice.setText(order.get(0).get(DBHandler.COLUMN_ORDERS_PRICE));
        if (Integer.parseInt(order.get(0).get(DBHandler.COLUMN_ORDERS_NUMBER_OF_ITEMS)) == 1) {
            customViewHolder.otherItems.setVisibility(View.GONE);
            customViewHolder.otherItem1.setVisibility(View.GONE);
            customViewHolder.otherItem2.setVisibility(View.GONE);
        } else if (Integer.parseInt(order.get(0).get(DBHandler.COLUMN_ORDERS_NUMBER_OF_ITEMS)) == 2) {
            customViewHolder.otherItem1.setText(order.get(1).get(DBHandler.COLUMN_ORDER_ITEMS_COUNT) + " " + order.get(1).get(DBHandler.COLUMN_ORDER_ITEMS_ITEM));
            customViewHolder.otherItem2.setVisibility(View.GONE);
        } else {
            customViewHolder.otherItem1.setText(order.get(1).get(DBHandler.COLUMN_ORDER_ITEMS_COUNT) + " " + order.get(1).get(DBHandler.COLUMN_ORDER_ITEMS_ITEM));
            customViewHolder.otherItem2.setText(order.get(2).get(DBHandler.COLUMN_ORDER_ITEMS_COUNT) + " " + order.get(2).get(DBHandler.COLUMN_ORDER_ITEMS_ITEM));
        }
        String imageDir = "";
        for (HashMap<String, String> item : order) {
            if (!item.get(DBHandler.COLUMN_MENU_IMAGE).equals("null")) {
                imageDir = item.get(DBHandler.COLUMN_MENU_IMAGE);
                break;
            }
        }
        if (!imageDir.equals("")) {
            runner r = new runner(order.get(0).get(DBHandler.COLUMN_MENU_IMAGE), customViewHolder.itemThumbnail, mContext);
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.start();
        }
    }


    @Override
    public int getItemCount() {
        return (null != orderList ? orderList.size() : 0);
    }


    static class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView itemThumbnail;
        TextView itemName;
        TextView orderDate;
        TextView itemPrice;
        LinearLayout otherItems;
        TextView otherItem1;
        TextView otherItem2;
        Button reorderButton;

        public CustomViewHolder(View view) {
            super(view);
            itemThumbnail = view.findViewById(R.id.item_thumbnail);
            itemName = view.findViewById(R.id.item_name);
            orderDate = view.findViewById(R.id.order_date);
            itemPrice = view.findViewById(R.id.item_price);
            otherItems = view.findViewById(R.id.other_items);
            otherItem1 = view.findViewById(R.id.other_item_1);
            otherItem2 = view.findViewById(R.id.other_item_2);
            reorderButton = view.findViewById(R.id.reorder_button);
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
            if (loadImageFromStorage(new ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]) == null) {
                System.out.println("Loading from Web");
                LoadImageFromWebOperations(url, new CategoryRecyclerAdapter.Callback<Drawable>() {
                    @Override
                    public void onComplete(Drawable result) {
                        System.out.println("Completed and uploading");
                        ContextCompat.getMainExecutor(context).execute(() -> imageView.setImageDrawable(result));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                Bitmap b = loadImageFromStorage(new ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE).getPath(), url.split("/")[url.split("/").length - 1]);
                System.out.println("Loading from Device: " + b);
                ContextCompat.getMainExecutor(context).execute(() -> imageView.setImageBitmap(b));
            }
        }
        private Bitmap loadImageFromStorage(String path, String file)
        {
            try {
                File f=new File(path, file);
                DBHandler db = new DBHandler(context);
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
        private String saveToInternalStorage(String passedName, Bitmap bitmapImage){
            ContextWrapper cw = new ContextWrapper(context);
            System.out.println("Passed Name in: " + passedName);
            String[] nameSplit = passedName.split("/");
            String usedName = nameSplit[nameSplit.length-1];
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory,usedName);

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
            DBHandler db = new DBHandler(context);
            String name = directory + usedName;
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

        public static void downloadImageFromFirebaseAndSave(String imagePath, CategoryRecyclerAdapter.Callback<String> callback) {
            // Create a reference to Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            // Path to the image you want to download
            StorageReference imageRef = storageRef.child(imagePath);
            // Get the download URL
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Use your method to load and save the image
                callback.onComplete(uri.toString());
            }).addOnFailureListener(exception -> {
                // Handle any errors
                System.out.println("Failed to get image URL: " + exception.getMessage());
                callback.onFailure(exception);
            });
        }
        public void LoadImageFromWebOperations(String imagePath, CategoryRecyclerAdapter.Callback<Drawable> callback) {
            Task<String> downloadTask = wrapDownload(imagePath);

            downloadTask.addOnCompleteListener(completion -> {
                if (completion.isSuccessful()) {
                    // Start a background thread for the download process
                    new Thread(() -> {
                        try {
                            String resultingPath = downloadTask.getResult();
                            System.out.println("Pulling image itself: " + resultingPath);

                            // Use HttpURLConnection to download the image
                            URL url = new URL(resultingPath);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.connect();

                            // Check if the response is OK
                            int responseCode = connection.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                InputStream is = connection.getInputStream();

                                // Create Drawable from InputStream
                                System.out.println("Creating Drawable, original type");
                                Drawable d = Drawable.createFromStream(is, "src name");

                                // Save to internal storage
                                saveToInternalStorage(imagePath, drawableToBitmap(d));

                                // Callback success
                                System.out.println("Downloaded and saved image");
                                callback.onComplete(d);

                                // Close the stream
                                is.close();
                            } else {
                                // Handle error response
                                System.out.println("Failed to download image. HTTP Response: " + responseCode);
                                callback.onFailure(new Exception("Failed to download image. Response code: " + responseCode));
                            }

                            // Disconnect
                            connection.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.onFailure(e);
                        }
                    }).start();  // Start the download in a background thread
                } else {
                    // If the task was not successful
                    System.out.println("Unsuccessful Pull");
                    callback.onFailure(new Exception("Unsuccessful Pull"));
                }
            });
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
        public static Task<String> wrapDownload(final String imagePath) {
            TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

            downloadImageFromFirebaseAndSave(imagePath, new CategoryRecyclerAdapter.Callback<String>() {
                @Override
                public void onComplete(String result) {
                    taskCompletionSource.setResult(result);  // Complete task with the result
                }

                @Override
                public void onFailure(Exception e) {
                    taskCompletionSource.setException(e);  // Complete task with an error if something fails
                }
            });

            return taskCompletionSource.getTask();

        }
    }
}
