package com.parsons.bakery;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class update implements Runnable {
    Context context;
    Loading parentClass;
    TextView updateInformation;
    FirebaseFirestore fireDB = FirebaseFirestore.getInstance();
    DBHandler db;
    int accountType;
    public update(Context context, Loading parentClass, int accountType, DBHandler handler, TextView updateInformation) {
        this.context = context;
        this.parentClass = parentClass;
        this.accountType = accountType;
        this.db = handler;
        this.updateInformation = updateInformation;
    }
    @Override
    public void run() {
        System.out.println("Starting update sequence");
        if (!isInternetAvailable()) {
            System.out.println("No Internet Available, skipping update sequence");
            ContextCompat.getMainExecutor(context).execute(() -> parentClass.updateText("Internet not available!"));
            if (accountType != 1)
                context.startActivity(new Intent(context, MainActivity.class));
            else
                context.startActivity(new Intent(context, Baker.class));
        }
        Task<List<Map<String, Object>>> pullMenuTask = UpdateHelper.ProcessTaskWrapper.wrapPullMenuProcess(fireDB);
        Task<List<Map<String, Object>>> pullCategoryTask = UpdateHelper.ProcessTaskWrapper.wrapPullCategories(fireDB);
        Task<List<Map<String, Object>>> pullCustomizations = UpdateHelper.ProcessTaskWrapper.wrapPullCustomizationsProcess(fireDB);
        Task<List<Map<String, Object>>> pullOrders = UpdateHelper.ProcessTaskWrapper.wrapPullOrdersProcess(fireDB);
        ContextCompat.getMainExecutor(context).execute(() -> updateInformation.setText("Pulling menu information"));


        List<Task<List<Map<String, Object>>>> tasks = new ArrayList<>();

        tasks.add(pullCategoryTask);
        tasks.add(pullMenuTask);
        tasks.add(pullCustomizations);
        if (Integer.parseInt(db.executeOne("SELECT " + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + " FROM " + DBHandler.TABLE_ACCT).get(0).get(DBHandler.COLUMN_ACCT_ACCOUNT_TYPE)) == 1) {
            tasks.add(pullOrders);
        }
        Tasks.whenAllComplete(tasks).addOnCompleteListener(allTask -> {
            if (allTask.isSuccessful()) {
                List<HashMap<String, String>> imagesDownloadedRAW = db.executeOne("SELECT * FROM " + DBHandler.TABLE_DOWNLOADED);
                List<String> imagesDownloaded = new ArrayList<>();
                for (HashMap<String, String> image : imagesDownloadedRAW) {
                    imagesDownloaded.add(image.get(DBHandler.COLUMN_DOWNLOADED_URL));
                }
                ContextCompat.getMainExecutor(context).execute(() -> updateInformation.setText("Updating menu information"));
                if (!db.database.isOpen()) {
                    db = new DBHandler(context);
                }
                List<Map<String, Object>> menuResults = pullMenuTask.getResult();
                List<Map<String, Object>> categoryResults = pullCategoryTask.getResult();
                List<Map<String, Object>> customizationResults = pullCustomizations.getResult();
                System.out.println("All ref pulls complete; \nPull Menu Size: " + menuResults.size() + "\nPull Category Size: " + categoryResults.size()+ "\nPull Customizations Size: " + customizationResults.size());
                if (!categoryResults.isEmpty()) {
                    List<Task<Boolean>> downloads = new ArrayList<>();
                    db.executeOne("DELETE FROM " + DBHandler.TABLE_CATEGORIES);
                    for (Map<String, Object> information : categoryResults) {
                        db.executeOne(
                                "INSERT INTO " + DBHandler.TABLE_CATEGORIES +
                                        " ("
                                        + DBHandler.COLUMN_CATEGORY_NAME + ","
                                        + DBHandler.COLUMN_CATEGORY_LEVEL + ","
                                        + DBHandler.COLUMN_CATEGORY_IMAGE + ","
                                        + DBHandler.COLUMN_CATEGORY_ORDERING + ","
                                        + DBHandler.COLUMN_CATEGORY_HAS_LEVELS +
                                        ") VALUES ('"
                                        + fixQuotes((String) information.get("entry")) + "',"
                                        + information.get("level") + ",'"
                                        + fixQuotes((String) information.get("img")) + "',"
                                        + information.get("order") + ","
                                        + information.get("hasLevels") +
                                        ")");
                        if(information.get("img") != null && information.get("img") != "null") {
                            String[] splitName = ((String) information.get("img")).split("/");
                            String usedName = splitName[splitName.length-1];
                            if (!imagesDownloaded.contains(usedName)) {
                                LoadImageFromWebOperations((String) information.get("img"), new Callback<Boolean>() {

                                    @Override
                                    public void onComplete(Boolean result) {

                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        System.out.println("Failed to pull image");
                                    }
                                });
                            } else {
                                System.out.println("Already had image: " + usedName);
                            }
                        }
                        System.out.println("Entered categories successfully");
                    }
                } else {
                    System.out.println("Results for category was empty");
                }

                if (!menuResults.isEmpty()) {
                    db.executeOne("DELETE FROM " + DBHandler.TABLE_MENU);

                    for (Map<String, Object> map : menuResults) {
                        String name = (String) map.get("name");
                        name = fixQuotes(name);
                        String description = (String) map.get("description");
                        if (description != null) {
                            description = fixQuotes(description);
                        }
                        db.executeOne("INSERT INTO " + DBHandler.TABLE_MENU
                                + " ("
                                + DBHandler.COLUMN_MENU_NAME + ","
                                + DBHandler.COLUMN_MENU_CATEGORY + ","
                                + DBHandler.COLUMN_MENU_DESCRIPTION + ","
                                + DBHandler.COLUMN_MENU_INNER_CATEGORY + ","
                                + DBHandler.COLUMN_MENU_USE_INNER + ","
                                + DBHandler.COLUMN_ORDER_BY_OPTIONS + ","
                                + DBHandler.COLUMN_MENU_REQ + ","
                                + DBHandler.COLUMN_MENU_IMAGE + ","
                                + DBHandler.COLUMN_MENU_PRICE + ","
                                + DBHandler.COLUMN_MENU_DZN_PRICE + ","
                                + ") VALUES ('"
                                + name + "','"
                                + fixQuotes((String) map.get("category")) + "','"
                                + description  + "','"
                                + fixQuotes((String) map.get("innerCategory")) + "',"
                                + map.get("useInner") + ","
                                + map.get("order_of_options") + ",'"
                                + fixQuotes((String) map.get("req")) + "','"
                                + map.get("image") + "',"
                                + map.get("price") + ","
                                + map.get("dozenPrice")
                                + ")");

                        if (map.get("image") != null && map.get("image") != "null") {
                            String[] splitName = ((String) map.get("image")).split("/");
                            String usedName = splitName[splitName.length-1];
                            if (!imagesDownloaded.contains(usedName)) {
                                LoadImageFromWebOperations((String) map.get("image"), new Callback<Boolean>() {
                                    @Override
                                    public void onComplete(Boolean result) {

                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        System.out.println("Error download image");
                                    }
                                });
                            } else {
                                System.out.println("Already had image: " + usedName);
                            }
                        }
                    }
                    System.out.println("Received " + menuResults.size() + " menu items.");
                } else {
                    System.out.println("No data received or there was an error.");
                }

                if (!customizationResults.isEmpty()) {
                    db.executeOne("DELETE FROM " + DBHandler.TABLE_CUSTOMIZATIONS);
                    for (Map<String, Object> map : customizationResults) {
                        db.executeOne("INSERT INTO " + DBHandler.TABLE_CUSTOMIZATIONS
                                + " ("
                                + DBHandler.COLUMN_CUSTOMIZATIONS_IS_REQUIRED + ","
                                + DBHandler.COLUMN_CUSTOMIZATIONS_ITEM + ","
                                + DBHandler.COLUMN_CUSTOMIZATIONS_OPTIONS + ","
                                + DBHandler.COLUMN_CUSTOMIZATIONS_TITLE + ","
                                + DBHandler.COLUMN_CUSTOMIZATIONS_TYPE + ","
                                + DBHandler.COLUMN_ORDER_BY_OPTIONS
                                + ") VALUES ("
                                + map.get("is_required") + ",'"
                                + fixQuotes((String) map.get("item")) + "','"
                                + fixQuotes((String) map.get("options")) + "','"
                                + fixQuotes((String) map.get("title")) + "','"
                                + fixQuotes((String) map.get("type")) + "',"
                                + map.get("order_of_options")
                                + ")"
                        );
                    }
                } else {
                    System.out.println("Empty Customizations");
                }
                if (Integer.parseInt(db.executeOne("SELECT " + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + " FROM " + DBHandler.TABLE_ACCT).get(0).get(DBHandler.COLUMN_ACCT_ACCOUNT_TYPE)) != -1) {
                    List<Map<String, Object>> orderResults = pullOrders.getResult();
                    if (!orderResults.isEmpty()) {
                        db.executeOne("DELETE FROM " + DBHandler.TABLE_ORDERS);
                        db.executeOne("DELETE FROM " + DBHandler.TABLE_ORDER_ITEMS);

                        for (Map<String, Object> order : orderResults) {
                            if (Integer.parseInt(db.executeOne("SELECT " + DBHandler.COLUMN_ACCT_ACCOUNT_TYPE + " FROM " + DBHandler.TABLE_ACCT).get(0).get(DBHandler.COLUMN_ACCT_ACCOUNT_TYPE)) == 0 && !db.executeOne("SELECT " + DBHandler.COLUMN_ACCT_UNIQUE_ID + " FROM " + DBHandler.TABLE_ACCT).get(0).get(DBHandler.COLUMN_ACCT_UNIQUE_ID).equals((String) order.get("userId"))) {
                                continue;
                            }
                            String identifier = (String) order.get("documentId");
                            String name = (String) order.get("name");
                            String number = (String) order.get("number");
                            String time = (String) order.get("time");
                            String timePlaced = (String) order.get("timePlaced");
                            Long numberOfItems = (Long) order.get("numberOfItems");
                            Long isCurrent = (Long) order.get("current");
                            Double price = (Double) order.get("price");
                            String userID = (String) order.get("userId");


                            List<Map<String, Object>> orderSpecifications = (List<Map<String, Object>>) order.get("orderSpecifications");
                            db.executeOne("INSERT INTO " + DBHandler.TABLE_ORDERS
                                    + " ("
                                    + DBHandler.COLUMN_ORDERS_ORDER_ID + ","
                                    + DBHandler.COLUMN_ORDERS_NAME + ","
                                    + DBHandler.COLUMN_ORDERS_NUMBER + ","
                                    + DBHandler.COLUMN_ORDERS_TIME_PLACED + ","
                                    + DBHandler.COLUMN_ORDERS_TIME_PICKUP + ","
                                    + DBHandler.COLUMN_ORDERS_NUMBER_OF_ITEMS + ","
                                    + DBHandler.COLUMN_ORDERS_NEEDS_VERIFICATION + ","
                                    + DBHandler.COLUMN_ORDERS_IS_CURRENT + ","
                                    + DBHandler.COLUMN_ORDERS_USERID + ","
                                    + DBHandler.COLUMN_ORDERS_PRICE
                                    + ") VALUES ('"
                                    + identifier + "','"
                                    + name + "','"
                                    + number + "','"
                                    + timePlaced + "','"
                                    + time + "',"
                                    + numberOfItems + ","
                                    + "1,"
                                    + isCurrent + ",'"
                                    + userID + "',"
                                    + price
                                    + ")"
                            );

                            for (Map<String, Object> specification : orderSpecifications) {
                                Long count = (Long) specification.get("count");
                                String customizations = (String) specification.get("specifications");
                                String item = (String) specification.get("item");
                                String type = (String) specification.get("type");


                                db.executeOne("INSERT INTO " + DBHandler.TABLE_ORDER_ITEMS
                                        + " ("
                                        + DBHandler.COLUMN_ORDER_ITEMS_ORDER_ID + ","
                                        + DBHandler.COLUMN_ORDER_ITEMS_ITEM + ","
                                        + DBHandler.COLUMN_ORDER_ITEMS_COUNT + ","
                                        + DBHandler.COLUMN_ORDER_ITEMS_TYPE + ","
                                        + DBHandler.COLUMN_ORDER_ITEMS_CUSTOMIZATIONS
                                        + ") VALUES ('"
                                        + identifier + "','"
                                        + item + "',"
                                        + count + ",'"
                                        + type + "','"
                                        + customizations + "')"
                                );
                            }

                        }
                    }
                }
            }  else {
                System.out.println("ISSUESSSSSSS");
            }
            ContextCompat.getMainExecutor(context).execute(() -> updateInformation.setText("Pulling images"));
            DocumentReference ref = fireDB.collection("categories").document("homeImages");
            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Map<String, Object> images = task.getResult().getData();

                    DBHandler dbHandler = new DBHandler(context);
                    List<HashMap<String, String>> imagesExisting = dbHandler.executeOne("SELECT * FROM " + DBHandler.TABLE_HOME_IMAGES);
                    List<String> imagesHad = new ArrayList<>();
                    for (HashMap<String, String> image : imagesExisting) {
                        imagesHad.add(image.get(DBHandler.COLUMN_HOME_IMAGES_PATH));
                    }

                    List<String> imagePathsToAdd = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : images.entrySet()) {
                        if (!imagesHad.contains(((String) entry.getValue()).substring(((String) entry.getValue()).lastIndexOf("/") + 1))) {
                            imagePathsToAdd.add((String) entry.getValue());
                        }
                    }

                    ContextCompat.getMainExecutor(context).execute(() -> updateInformation.setText("Downloading images"));
                    ImageDownloadManager.downloadImages(context, db, imagePathsToAdd, () -> {
                        //REMOVE OLD IMAGES
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
                        db.close();
                        if (accountType != 1)
                            context.startActivity(new Intent(context, MainActivity.class));
                        else
                            context.startActivity(new Intent(context, Baker.class));
                    });
                }
            });
        });
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
        String name = directory + "/" + usedName;
        long seconds = System.currentTimeMillis() / 1000;
        boolean has = false;
        List<HashMap<String, String>> returnedRecents = db.executeOne("SELECT * FROM downloaded");
        for (HashMap<String, String> hashy : returnedRecents) {
            if (hashy.get("url").equals(name)) {
                has = true;
            }
        }
        if (has) {
            db.executeOne("UPDATE downloaded SET lastAccessed='" + seconds + "' WHERE url='" + usedName + "'");
        } else {
            db.executeOne("INSERT INTO downloaded (url,lastAccessed) VALUES ('" + usedName + "','" + seconds + "')");
        }
        return directory.getAbsolutePath();
    }

    public static void downloadImageFromFirebaseAndSave(String imagePath, Callback<String> callback) {
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
    public void LoadImageFromWebOperations(String imagePath, Callback<Boolean> callback) {
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
                            callback.onComplete(true);

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

        downloadImageFromFirebaseAndSave(imagePath, new Callback<String>() {
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

    public static String addChar(String str, char ch, int position) {
        return str.substring(0, position) + ch + str.substring(position);
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    public static String fixQuotes(String toFix) {
        if ( toFix == null ) {
            return null;
        }
        String tempFix = toFix;
        if (tempFix.contains("'")) {
            char[] characters = tempFix.toCharArray();
            int place = 0;
            for (char c : characters) {
                if (c == '\'') {
                    tempFix = addChar(tempFix, '\'', place);
                }
                place++;
            }
        }
        return tempFix;
    }

    public interface Callback<T> {
        void onComplete(T result);     // Called when the operation completes successfully
        void onFailure(Exception e);   // Called when there's an error
    }


}