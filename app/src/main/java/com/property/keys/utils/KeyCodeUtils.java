package com.property.keys.utils;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.zxing.BarcodeFormat;

import java.io.ByteArrayOutputStream;

import androidmads.library.qrgenearator.KeyCodeEncoder;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.R)
public class KeyCodeUtils {
    private static final String TAG = KeyCodeUtils.class.getSimpleName();

    public static Bitmap generateCode(String id, boolean userBarCode) {
        KeyCodeEncoder encoder = new KeyCodeEncoder(id, userBarCode ? BarcodeFormat.CODE_39 : BarcodeFormat.QR_CODE, 350);
        Bitmap bitmap = encoder.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        //validate
        StorageUtils.uploadImage(id, "key", baos.toByteArray());
        Timber.tag(TAG).i("Generated code for the  key " + id + " and uploaded to remote storage.");
        return bitmap;
    }
}
