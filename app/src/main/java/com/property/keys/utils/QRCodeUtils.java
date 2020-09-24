package com.property.keys.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.property.keys.R;

import java.io.ByteArrayOutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.R)
public class QRCodeUtils {
    private static final String TAG = QRCodeUtils.class.getSimpleName();

    public static Bitmap generateCode(Context context, String id) {
        QRGEncoder encoder = new QRGEncoder(id, null, QRGContents.Type.TEXT, 350);
        encoder.setColorBlack(ContextCompat.getColor(context, R.color.primaryColor));
        Bitmap bitmap = encoder.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        //validate
        StorageUtils.uploadImage(id, "key", baos.toByteArray());
        Timber.tag(TAG).i("Generated QR code for the  key " + id + " and uploaded to remote storage.");
        return bitmap;
    }
}
