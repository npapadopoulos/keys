package com.property.keys.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.property.keys.R;
import com.property.keys.entities.Key;
import com.property.keys.entities.User;
import com.property.keys.utils.ImageUtils;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.R)
public class KeyHolder extends RecyclerView.ViewHolder implements Holder {

    private ImageView qrCodeImage;
    private TextView checkedInDetails, checkedInDate, location, purpose;
    private RelativeLayout keyBackground, keyForeground, keyDetails;
    private User user;

    public KeyHolder(@NonNull View itemView, User user) {
        super(itemView);

        qrCodeImage = itemView.findViewById(R.id.qrCodeImage);
        location = itemView.findViewById(R.id.location);
        purpose = itemView.findViewById(R.id.purpose);
        checkedInDate = itemView.findViewById(R.id.checkedInDate);
        checkedInDetails = itemView.findViewById(R.id.checkedInDetails);
        keyForeground = itemView.findViewById(R.id.keyForeground);
        keyBackground = itemView.findViewById(R.id.keyBackground);
        keyDetails = itemView.findViewById(R.id.keyDetails);
        this.user = user;
    }

    private static void addTitle(Context context, Bitmap bitmap, String propertyName) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, R.color.red_600));
        paint.setTextSize(25);

        float textWidth = paint.measureText(propertyName);
        int xPos = bitmap.getWidth() / 2 - (int) (textWidth / 2);
        int yPos = (int) (bitmap.getHeight() - 20 - ((paint.descent() + paint.ascent()) / 2));
        canvas.drawText(propertyName, xPos, yPos, paint);
    }

    public void bind(@NonNull Context context, @NonNull Key key, String propertyName) {
        location.setText(key.getLocation());
        purpose.setText(key.getPurpose());
        if (key.getCheckedInDate() != null) {

            String userFullName = key.getLastCheckedInUser();
            if (userFullName.equals(user.getFirstName() + " " + user.getLastName())) {
                userFullName = "You took";
            } else {
                userFullName = userFullName + " took";
            }

            checkedInDate.setText(key.getCheckedInDate());
            checkedInDetails.setText(userFullName + " the key for " + key.getCheckinReason() + ".");
            keyDetails.setBackground(ContextCompat.getDrawable(context, R.drawable.key_busy_background));
        } else {
            checkedInDate.setText("");
            checkedInDetails.setText("Key is available.");
            keyDetails.setBackground(ContextCompat.getDrawable(context, R.drawable.key_available_background));
        }
        ImageUtils.syncAndloadImagesKey(context, key.getId(), qrCodeImage, (image) -> itemView.setOnClickListener(v -> {
            Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
            Bitmap copiedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            addTitle(context, copiedBitmap, propertyName); //TODO configurable -> Settings Menu

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", Objects.requireNonNull(ImageUtils.saveImage(context, copiedBitmap, "code-title")));

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/jpeg");

            context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.print_qrcode)));
        }));
    }

    @Override
    public RelativeLayout getBackground() {
        return keyBackground;
    }

    @Override
    public RelativeLayout getRestoreBackground() {
        return null;
    }

    @Override
    public RelativeLayout getForeground() {
        return keyForeground;
    }
}
