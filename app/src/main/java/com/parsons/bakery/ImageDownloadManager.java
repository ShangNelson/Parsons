package com.parsons.bakery;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.parsons.bakery.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class ImageDownloadManager {
    public static void downloadImages(Context context, DBHandler dbHandler, List<String> imagePaths, Runnable onComplete) {
        List<Task<Void>> tasks = new ArrayList<>();

        for (String imagePath : imagePaths) {
            // Create a download task
            DownloadImageTask downloadTask = new DownloadImageTask(context, imagePath);

            // Start the task in a separate thread
            new Thread(downloadTask).start();

            // Add the task to the list
            tasks.add(downloadTask.getTask());
        }

        // Wait for all tasks to complete
        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            for (String image : imagePaths) {

                String fileName = image.substring(image.lastIndexOf("/") + 1);
                dbHandler.executeOne("INSERT INTO " + DBHandler.TABLE_HOME_IMAGES + " (" + DBHandler.COLUMN_HOME_IMAGES_PATH + ") VALUES ('" + fileName + "')");
            }
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}

