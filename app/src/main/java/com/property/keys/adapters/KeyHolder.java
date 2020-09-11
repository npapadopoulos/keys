package com.property.keys.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.property.keys.PropertyDetails;
import com.property.keys.R;
import com.property.keys.entities.Key;
import com.property.keys.utils.ImageUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class KeyHolder extends RecyclerView.ViewHolder {

    private TextView keyId;
    private ImageView qrCodeImage;
    private FloatingActionButton setFavourite;
    private Key key;

    public KeyHolder(@NonNull Activity activity, @NonNull View itemView) {
        super(itemView);

        keyId = itemView.findViewById(R.id.keyId);
        qrCodeImage = itemView.findViewById(R.id.qrCodeImage);

        itemView.setOnClickListener(view -> {
            Pair[] pairs = new Pair[6];
            pairs[0] = new Pair<View, String>(qrCodeImage, "qrCodeImage");
            pairs[1] = new Pair<View, String>(keyId, "keyId");
            pairs[5] = new Pair<View, String>(setFavourite, "favourite");

            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(activity, pairs).toBundle();
            Intent propertyDetails = new Intent(itemView.getContext(), PropertyDetails.class);
            propertyDetails.putExtra("key", key);

            view.getContext().startActivity(propertyDetails, bundle);
            //FIXME probably back button on details has to do with the above. ACTIVITY STARTS FROM PROPERTY_HOLDER INSTEAD OF THE CONTAINER
        });

    }

    public void bind(@NonNull Context context, @NonNull Key key) {
        keyId.setText(key.getId());
        this.key = key;
        ImageUtils.syncAndloadImages(context, key.getId(), qrCodeImage);
    }
}
