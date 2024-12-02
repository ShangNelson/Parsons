package com.parsons.bakery;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadImageTask implements Runnable {
    private final Context context;
    private final String imagePath; // Path in Firebase Storage (e.g., "/homeImages/image.png")
    private final TaskCompletionSource<Void> taskCompletionSource; // Signals when the task completes
    private static final ExecutorService executor = Executors.newCachedThreadPool(); // Shared thread pool for downloads

    public DownloadImageTask(Context context, String imagePath) {
        this.context = context;
        this.imagePath = imagePath;
        this.taskCompletionSource = new TaskCompletionSource<>();
    }

    public Task<Void> getTask() {
        return taskCompletionSource.getTask();
    }

    @Override
    public void run() {
        try {
            // Extract the file name from the path
            String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            System.out.println("Saving as " + fileName);
            // Firebase Storage reference
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child(imagePath);

            // Local file where the image will be saved
            File localFile = new File(context.getFilesDir(), fileName);

            // Download the image asynchronously
            storageRef.getStream().addOnSuccessListener(taskSnapshot -> {
                executor.execute(() -> {
                    try (InputStream inputStream = taskSnapshot.getStream();
                         FileOutputStream outputStream = new FileOutputStream(localFile)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        Log.d("DownloadImageTask", "Image downloaded and saved as: " + localFile.getAbsolutePath());
                        taskCompletionSource.setResult(null); // Mark the task as completed
                    } catch (Exception e) {
                        Log.e("DownloadImageTask", "Error saving the image", e);
                        taskCompletionSource.setException(e); // Mark the task as failed
                    }
                });
            }).addOnFailureListener(e -> {
                Log.e("DownloadImageTask", "Failed to download the image", e);
                taskCompletionSource.setException(e); // Mark the task as failed
            });
        } catch (Exception e) {
            Log.e("DownloadImageTask", "Unexpected error", e);
            taskCompletionSource.setException(e); // Mark the task as failed
        }
    }
}
