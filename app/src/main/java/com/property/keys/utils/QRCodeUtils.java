package com.property.keys.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.property.keys.entities.Key;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.R)
public class QRCodeUtils {

    public static void generate(Key key, int width, int height, String filePath)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix bitMatrix = qrCodeWriter.encode(Utils.hash(key.getId()), BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "jpg", baos);
        //validate
        StorageUtils.uploadImage(key.getId(), baos.toByteArray());
    }
}
