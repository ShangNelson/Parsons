package com.parsons.bakery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalImageLoader {

    /**
     * Loads images from the local app-specific storage directory using file names.
     *
     * @param context   Application context for accessing internal storage.
     * @param fileNames List of image file names (e.g., "image1.png").
     * @return List of Bitmap objects representing the loaded images.
     */
    public static List<Bitmap> loadImages(Context context, List<String> fileNames) {
        List<Bitmap> bitmaps = new ArrayList<>();

        for (String fileName : fileNames) {
            try {
                // Construct the full file path in the app's internal storage directory
                File file = new File(context.getFilesDir(), fileName);
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap != null) {
                        bitmaps.add(bitmap);
                    } else {
                        Log.e("LocalImageLoader", "Failed to decode image: " + fileName);
                    }
                } else {
                    Log.e("LocalImageLoader", "File does not exist: " + fileName);
                }
            } catch (Exception e) {
                Log.e("LocalImageLoader", "Error loading image: " + fileName, e);
            }
        }

        return bitmaps;
    }
}
