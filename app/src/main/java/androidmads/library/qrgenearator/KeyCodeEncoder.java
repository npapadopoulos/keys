package androidmads.library.qrgenearator;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

public class KeyCodeEncoder {

    private int WHITE = 0xFFFFFFFF;
    private int BLACK = 0xFF000000;
    private int dimension;
    private String contents = null;
    private BarcodeFormat format;
    private boolean encoded;

    public KeyCodeEncoder(String data, BarcodeFormat format, int dimension) {
        this.dimension = dimension;
        this.format = format;
        encoded = encodeContents(data);
    }

    private boolean encodeContents(String data) {
        // Default to QR_CODE if no format given.
        if (format == null) {
            this.format = BarcodeFormat.QR_CODE;
            encodeCodeContents(data);
        } else if (data != null && data.length() > 0) {
            contents = data;
        }
        return contents != null && contents.length() > 0;
    }

    private void encodeCodeContents(String data) {
        if (data != null && data.length() > 0) {
            contents = data;
        }
    }

    public Bitmap getBitmap() {
        if (!encoded) return null;
        try {
            Map<EncodeHintType, Object> hints = null;
            String encoding = guessAppropriateEncoding(contents);
            if (encoding != null) {
                hints = new EnumMap<>(EncodeHintType.class);
                hints.put(EncodeHintType.CHARACTER_SET, encoding);
            }
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix result = writer.encode(contents, format, dimension, dimension, hints);
            int width = result.getWidth();
            int height = result.getHeight();
            int[] pixels = new int[width * height];
            // All are 0, or black, by default
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception ex) {
            return null;
        }
    }

    private String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }
}
