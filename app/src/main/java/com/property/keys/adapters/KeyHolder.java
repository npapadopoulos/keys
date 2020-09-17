package com.property.keys.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.property.keys.R;
import com.property.keys.entities.Key;
import com.property.keys.utils.ImageUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class KeyHolder extends RecyclerView.ViewHolder {

    private ImageView qrCodeImage;
    private TextView checkedInDetails;

    public KeyHolder(@NonNull View itemView) {
        super(itemView);

        qrCodeImage = itemView.findViewById(R.id.qrCodeImage);
        checkedInDetails = itemView.findViewById(R.id.checkedInDetails);
    }

    public void bind(@NonNull Context context, @NonNull Key key) {
        if (key.getCheckedInDate() != null) {
            checkedInDetails.setText("Key was checked in on " + key.getCheckedInDate());
        } else {
            checkedInDetails.setText("Key is available.");
        }
        ImageUtils.syncAndloadImagesKey(context, key.getId(), qrCodeImage, (image) -> {
            ImageView qrCodeImage = itemView.findViewById(R.id.qrCodeImage);
            qrCodeImage.setOnClickListener(v -> {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", image);
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setType("image/jpeg");
                context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.print_qrcode)));
            });
        });
    }
}
