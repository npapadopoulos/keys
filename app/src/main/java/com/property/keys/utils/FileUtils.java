package com.property.keys.utils;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.property.keys.R;
import com.property.keys.camera.ImagePicker;
import com.property.keys.entities.ImageGenerationType;
import com.property.keys.entities.User;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import timber.log.Timber;

import static com.property.keys.utils.StorageUtils.downloadAndSaveImage;

@RequiresApi(api = Build.VERSION_CODES.R)
public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();
    public static final int REQUEST_IMAGE = 100;

    private FileUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static File getImage(Context context, String id) {
        File image = getFile(context.getExternalCacheDir(), id);
        if (image == null) {
            Timber.tag(TAG).e("Couldn't find the image in local storage for id %s", id);
            return null;
        }

        Timber.tag(TAG).i("Got Image from : %s", image.getPath());
        return image;
    }

    public static File saveImage(Context context, Bitmap image, String name) {
        try {
            clearCache(context);
            File file = newFile(context.getExternalCacheDir(), name);

            FileOutputStream fos = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            Timber.tag(TAG).i("Saved Image to : %s", file.getPath());
            return file;
        } catch (IOException e) {
            Timber.tag(TAG).e(e, "Couldn't save the image to local storage.");
        }
        return null;
    }

    public static Object loadImage(Context context, String name, ImageView imageView) {
        File image = getImage(context, name);
        if (image == null) {
            Timber.tag(TAG).e("No Image found for: %s", name);
            return null;
        }

        return loadImage(context, image, imageView);
    }

    public static Object loadImage(Context context, Object image, ImageView imageView) {
        return loadImage(context, image, imageView, false, null);
    }

    public static Object loadImage(Context context, Object image, ImageView imageView, boolean useBackground,
                                   Consumer<File> onComplete) {
        RequestBuilder<Drawable> glideBuilder = Glide.with(context)
                .load(image)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontTransform();

        if (useBackground) {
            glideBuilder.into(new CustomViewTarget<ImageView, Drawable>(imageView) {
                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {

                }

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    imageView.setBackground(resource);
                }

                @Override
                protected void onResourceCleared(@Nullable Drawable placeholder) {

                }
            });
        } else {
            glideBuilder.into(imageView);
        }

        if (onComplete != null) {
            onComplete.accept((File) image);
        }
        return image;
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
                if (child.isDirectory() && child.getName().equals(name))
                    clearCache(context, child, name);
                if (!child.getName().equals(name)) {
                    boolean deleted = child.delete();
                    if (deleted)
                        Timber.tag(TAG).i("File " + child.getAbsolutePath() + " has been deleted");
                }
            }
        }
    }

    public static void syncAndLoadImagesProfile(Context context, User user, ImageView imageView) {
        syncAndloadImages(context, "profile", user.getId(), user.getFirstName(), user.getLastName(), imageView, false, ImageGenerationType.PROFILE, null);
    }

    public static void syncAndLoadImagesProfile(Context context, @NonNull String name, String firstName, String lastName, ImageView imageView) {
        syncAndloadImages(context, "profile", name, firstName, lastName, imageView, false, ImageGenerationType.PROFILE, null);
    }

    public static void syncAndloadImagesProperty(Context context, @NonNull String name, ImageView imageView, boolean useBackground) {
        syncAndloadImages(context, "property", name, null, null, imageView, useBackground, ImageGenerationType.NONE, null);
    }

    public static void syncAndloadImagesKey(Context context, @NonNull String name, ImageView imageView, Consumer<File> onComplete) {
        syncAndloadImages(context, "key", name, null, null, imageView, false, ImageGenerationType.KEY, onComplete);
    }

    private static void syncAndloadImages(Context context, String directory, @NonNull String name, String firstName, String lastName, ImageView imageView, boolean useBackground,
                                          ImageGenerationType type,
                                          Consumer<File> onComplete) {
        File image = getImage(context, name);
        if (image != null) {
            loadImage(context, image, imageView, useBackground, onComplete);
        } else {
            downloadAndSaveImage(context, name, directory, firstName, lastName, imageView, type, onComplete);
        }
    }

    public static File newFile(File cacheDir, String name) {
        return newFile(cacheDir, name, "images", Instant.now().toEpochMilli() + ".jpg");
    }

    public static File newFile(File cacheDir, String name, String directoryName, String fileName) {
        File directory = new File(cacheDir, directoryName);
        if (!directory.exists()) directory.mkdirs();
        if (!TextUtils.isEmpty(name)) {
            directory = new File(directory, name);
            if (!directory.exists()) directory.mkdirs();
        }

        return new File(directory, fileName);
    }

    public static File getFile(File cacheDir, String name) {
        return getFile(cacheDir, name, "images");
    }

    public static File getFile(File cacheDir, String name, String directoryName) {
        File directory = new File(cacheDir, directoryName);
        directory = new File(directory, name);
        if (directory.exists() && directory.listFiles() != null && Objects.requireNonNull(directory.listFiles()).length > 0) {
            return Objects.requireNonNull(directory.listFiles())[0];
        }
        return null;
    }

    public static void updateImage(Activity parent, String id, boolean forProperty) {
        updateImage(parent, null, id, forProperty);
    }

    public static void updateImage(Activity parent, Fragment fragment, String id, boolean forProperty) {
        Dexter.withContext(fragment != null ? fragment.getActivity() : parent)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        Activity activity = fragment == null ? parent : fragment.getActivity();
                        if (report.areAllPermissionsGranted()) {
                            showImagePickerOptions(activity, fragment, id, forProperty);
                        } else {
                            showSettingsDialog(activity);
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog(activity);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private static void showImagePickerOptions(Activity activity, Fragment fragment, String id, boolean forProperty) {
        ImagePicker.showImagePickerOptions(activity, new ImagePicker.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent(activity, fragment, id);
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent(activity, fragment, id);
            }
        }, forProperty);
    }

    private static void launchCameraIntent(Activity activity, Fragment fragment, String id) {
        Intent intent = new Intent(activity, ImagePicker.class);
        intent.putExtra(ImagePicker.INTENT_IMAGE_PICKER_OPTION, ImagePicker.REQUEST_IMAGE_CAPTURE);
        intent.putExtra("id", id);

        // setting aspect ratio
        intent.putExtra(ImagePicker.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePicker.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePicker.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePicker.INTENT_BITMAP_MAX_HEIGHT, 1000);

        if (fragment != null) {
            fragment.startActivityForResult(intent, REQUEST_IMAGE);
        } else {
            activity.startActivityForResult(intent, REQUEST_IMAGE);
        }
    }

    private static void launchGalleryIntent(Activity activity, Fragment fragment, String id) {
        Intent intent = new Intent(activity, ImagePicker.class);
        intent.putExtra(ImagePicker.INTENT_IMAGE_PICKER_OPTION, ImagePicker.REQUEST_GALLERY_IMAGE);
        intent.putExtra("id", id);

        // setting aspect ratio
        intent.putExtra(ImagePicker.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_Y, 1);
        if (fragment != null) {
            fragment.startActivityForResult(intent, REQUEST_IMAGE);
        } else {
            activity.startActivityForResult(intent, REQUEST_IMAGE);
        }
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     *
     * @param activity
     */
    private static void showSettingsDialog(Activity activity) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setBackground(ContextCompat.getDrawable(activity, R.drawable.white_card_background));
        builder.setTitle("Grant Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings(activity);
        });
        builder.setNegativeButton(activity.getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // navigating user to app settings
    private static void openSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, 101);
    }


    @NotNull
    public static Bitmap generateDefaultProfileImage(Context context, ImageView imageView, String initials) {
        Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        Random rnd = new Random();
        paint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        Rect r = new Rect();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(70);
        paint.getTextBounds(initials, 0, initials.length(), r);
        int x = canvas.getWidth() / 2;
        int y = canvas.getHeight() / 2;
        y += (Math.abs(r.height())) / 2;
        canvas.drawText(initials, x, y, paint);
        return bitmap;
    }
}
