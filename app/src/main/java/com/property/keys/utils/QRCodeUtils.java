package com.property.keys.utils;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.zxing.WriterException;

import java.io.ByteArrayOutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.R)
public class QRCodeUtils {
    private static final String TAG = QRCodeUtils.class.getSimpleName();

    public static Bitmap generateCode(String id) {
        try {
            QRGEncoder qrgEncoder = new QRGEncoder(id, null, QRGContents.Type.TEXT, 350);
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //validate
            StorageUtils.uploadImage(id, "key", baos.toByteArray());
            Timber.tag(TAG).i("Generated QR code for the  key " + id + " and uploaded to remote storage.");
            return bitmap;
        } catch (WriterException e) {
            Timber.tag(TAG).e("Could not generate QR Code for key " + id + ": " + e.getMessage());
        }
        return null;
    }
}
