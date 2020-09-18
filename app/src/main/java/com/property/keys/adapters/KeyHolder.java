package com.property.keys.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.property.keys.utils.ImageUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class KeyHolder extends RecyclerView.ViewHolder implements Holder {

    private ImageView qrCodeImage;
    private TextView checkedInDetails, checkedInDate;
    private RelativeLayout keyBackground, keyForeground, keyDetails;

    public KeyHolder(@NonNull View itemView) {
        super(itemView);

        qrCodeImage = itemView.findViewById(R.id.qrCodeImage);
        checkedInDate = itemView.findViewById(R.id.checkedInDate);
        checkedInDetails = itemView.findViewById(R.id.checkedInDetails);
        keyForeground = itemView.findViewById(R.id.keyForeground);
        keyBackground = itemView.findViewById(R.id.keyBackground);
        keyForeground = itemView.findViewById(R.id.keyForeground);
        keyDetails = itemView.findViewById(R.id.keyDetails);
    }

    public void bind(@NonNull Context context, @NonNull Key key) {
        if (key.getCheckedInDate() != null) {
            checkedInDate.setText(key.getCheckedInDate());
            checkedInDetails.setText(key.getLastCheckedInUser() + " has the key.");
            keyDetails.setBackground(ContextCompat.getDrawable(context, R.drawable.key_busy_background));
        } else {
            checkedInDate.setText("");
            checkedInDetails.setText("Key is available.");
            keyDetails.setBackground(ContextCompat.getDrawable(context, R.drawable.key_available_background));
        }
        ImageUtils.syncAndloadImagesKey(context, key.getId(), qrCodeImage, (image) -> itemView.setOnClickListener(v -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", image);
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
    public RelativeLayout getForeground() {
        return keyForeground;
    }
}
