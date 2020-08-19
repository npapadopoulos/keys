package com.property.keys.utils;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.zxing.WriterException;
import com.property.keys.entities.Key;

import java.io.ByteArrayOutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

@RequiresApi(api = Build.VERSION_CODES.R)
public class QRCodeUtils {
    private static final String TAG = QRCodeUtils.class.getSimpleName();

    public static void generateCode(Key key) {
        try {
            QRGEncoder qrgEncoder = new QRGEncoder(key.getId(), null, QRGContents.Type.TEXT, 350);
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //validate
            StorageUtils.uploadImage(key.getId(), "qrcode", baos.toByteArray());
            Log.i(TAG, "Generated QR code for the  key " + key.getId() + " and uploaded to remote storage.");
        } catch (WriterException e) {
            Log.e(TAG, "Could not generate QR Code for key " + key.getId() + ": " + e.getMessage());
        }
    }
}
