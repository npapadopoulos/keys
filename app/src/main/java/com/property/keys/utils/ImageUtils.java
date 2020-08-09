package com.property.keys.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.R)
public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

    private ImageUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static final File getImage(Context context, String userId) {
        File image = getFile(context.getExternalCacheDir(), userId);
        if (image == null) {
            Log.e(TAG, "Couldn't find the image in local storage for user with id " + userId);
            return null;
        }

        Log.i(TAG, "Got Image from : " + image.getPath());
        return image;
    }

    public static final File saveImage(Context context, Bitmap image, String name) {
        try {
            clearCache(context);
            File file = newFile(context.getExternalCacheDir(), name);

            FileOutputStream fos = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            Log.i(TAG, "Saved Image to : " + file.getPath());
            return file;
        } catch (IOException e) {
            Log.e(TAG, "Couldn't save the image to local storage.", e);
        }
        return null;
    }

    public static void loadImages(Context context, String name, CircularImageView... imageViews) {
        Arrays.stream(imageViews).forEach(imageView -> {
            loadImage(context, name, imageView);
        });
    }

    public static void loadImages(Context context, File image, CircularImageView... imageViews) {
        Arrays.stream(imageViews).forEach(imageView -> {
            loadImage(context, image, imageView);
        });
    }

    public static void loadImage(Context context, String name, CircularImageView imageView) {
        File image = getImage(context, name);
        if (image == null) {
            Log.i(TAG, "No Image found for: " + name);
            return;
        }

        loadImage(context, image, imageView);
    }

    public static void loadImage(Context context, File image, CircularImageView imageView) {
        Glide.with(context)
                .load(image)
                .dontTransform()
                .into(imageView);
        imageView.setColorFilter(ContextCompat.getColor(context, android.R.color.transparent));
        Log.i(TAG, "Image " + image.getPath() + " has been loaded");
    }

    public static void clearCache(Context context) {
        clearCache(context, null, "");
    }

    public static void clearCache(Context context, File directory, String name) {
        File path = directory;
        if (path == null)
            path = new File(context.getExternalCacheDir(), "images");
        if (path.exists() && path.isDirectory()) {
            for (File child : path.listFiles()) {
                if (child.isDirectory())
                    clearCache(context, child, name);
                if (!child.getName().equals(name)) {
                    boolean deleted = child.delete();
                    if (deleted)
                        Log.i(TAG, "File " + child.getAbsolutePath() + " has been deleted");
                }
            }
        }
    }

    public static void syncAndloadImages(Context context, String name, CircularImageView... imageViews) {
        File image = getImage(context, name);
        if (image != null) {
            loadImages(context, image, imageViews);
        } else {
            StorageUtils.downloadAndSaveImage(context, name, imageViews);
        }
    }

    public static File newFile(File cacheDir, String name) {
        File userDirectory = new File(cacheDir, "images");
        if (!userDirectory.exists()) userDirectory.mkdirs();
        userDirectory = new File(userDirectory, name);
        if (!userDirectory.exists()) userDirectory.mkdirs();

        return new File(userDirectory, Instant.now().toEpochMilli() + ".jpg");
    }

    public static File getFile(File cacheDir, String name) {
        File userDirectory = new File(cacheDir, "images");
        userDirectory = new File(userDirectory, name);
        if (userDirectory.exists() && userDirectory.listFiles() != null && userDirectory.listFiles().length > 0) {
            return userDirectory.listFiles()[0];
        }
        return null;
    }
}
