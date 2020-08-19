package com.property.keys.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.function.Consumer;

@RequiresApi(api = Build.VERSION_CODES.R)
public class StorageUtils {
    private static final String TAG = "StorageUtils";

    private static StorageReference reference = FirebaseStorage.getInstance().getReference();

    private StorageUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void uploadImage(String id, String name, Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        uploadImage(id, name, baos.toByteArray());
    }

    public static void uploadImage(String id, String name, byte[] data) {
        reference.child(id + "/images/" + name + ".jpg").putBytes(data).addOnFailureListener(exception -> {
            //TODO Handle unsuccessful uploads
            Log.e(TAG, exception.getMessage(), exception);
        }).addOnSuccessListener(taskSnapshot -> {
            Log.i(TAG, "Profile image successfully has been synced with remote storage");
            //TODO taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
        });
    }

    public static void downloadAndSaveImage(Context context, String id, String name, ImageView[] imageViews) {
        downloadImage(context, id, name, image -> ImageUtils.loadImages(context, image, imageViews));
    }

    public static void downloadImage(Context context, String id, String name, Consumer<File> loader) {
        reference.child(id + "/images/" + name + ".jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(data -> {
            // Use the bytes to display the image
            File image = ImageUtils.saveImage(context, BitmapFactory.decodeByteArray(data, 0, data.length), id);
            if (loader != null) {
                loader.accept(image);
            }
        }).addOnFailureListener(exception -> {
            StorageException storageException = (StorageException) exception;
            if (storageException.getHttpResultCode() == 404) {
                Log.i(TAG, "Image was not found in path " + id + " in remote storage");
            }
        });
    }
}
