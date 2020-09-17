package com.property.keys.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.property.keys.entities.ImageGenerationType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.function.Consumer;

import timber.log.Timber;

import static com.property.keys.utils.ImageUtils.generateDefaultProfileImage;
import static com.property.keys.utils.ImageUtils.getImage;

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
            Timber.tag(TAG).e(exception);
        }).addOnSuccessListener(taskSnapshot -> {
            Timber.tag(TAG).i("Profile image successfully has been synced with remote storage");
            //TODO taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
        });
    }

    public static void downloadAndSaveImage(Context context, String id, String name, ImageView imageView, ImageGenerationType type,
                                            Consumer<File> onComplete) {
        downloadImage(context, id, name, image -> ImageUtils.loadImage(context, image, imageView), imageView, type, onComplete);
    }

    public static void downloadImage(Context context, String id, String name, Consumer<Object> loader, ImageView imageView, ImageGenerationType type,
                                     Consumer<File> onComplete) {
        reference.child(id + "/images/" + name + ".jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(data -> {
            // Use the bytes to display the image
            saveAndLoadImage(context, id, loader, onComplete, BitmapFactory.decodeByteArray(data, 0, data.length));
        }).addOnFailureListener(exception -> {
            StorageException storageException = (StorageException) exception;
            if (storageException.getHttpResultCode() == 404) {
                Timber.tag(TAG).i("Image was not found in path " + id + " in remote storage");
                Bitmap generated;
                switch (type) {
                    case PROFILE: {
                        File existing = getImage(context, "default");
                        if (existing == null) {
                            generated = generateDefaultProfileImage(context, imageView);
                            saveAndLoadImage(context, "default", loader, null, generated);
                        }
                        break;
                    }
                    case KEY: {
                        generated = QRCodeUtils.generateCode(id);
                        saveAndLoadImage(context, "default", loader, onComplete, generated);
                        break;
                    }
                }
            }
        });
    }

    private static void saveAndLoadImage(Context context, String id, Consumer<Object> loader, Consumer<File> onComplete, Bitmap bitmap) {
        File image = ImageUtils.saveImage(context, bitmap, id);
        if (loader != null && image != null) {
            loader.accept(image);
            if (onComplete != null) {
                onComplete.accept(image);
            }
        }
    }
}
