package com.property.keys.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.property.keys.PropertyDetails;
import com.property.keys.R;
import com.property.keys.entities.Property;
import com.property.keys.utils.ImageUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyHolder extends RecyclerView.ViewHolder implements Holder {

    private TextView name, address, availableSum, busySum;
    private ImageView propertyImage, availableSumImage, busySumImage;
    private final boolean inTrash;
    private Property property;
    private RelativeLayout propertyBackground, propertyRestoreBackground, propertyForeground;
    private Chip type;

    public PropertyHolder(@NonNull Activity activity, @NonNull View itemView, boolean inTrash) {
        super(itemView);

        name = itemView.findViewById(R.id.name);
        address = itemView.findViewById(R.id.address);
        propertyImage = itemView.findViewById(R.id.propertyImage);
        availableSumImage = itemView.findViewById(R.id.availableSumImage);
        availableSum = itemView.findViewById(R.id.availableSum);
        busySum = itemView.findViewById(R.id.busySum);
        busySumImage = itemView.findViewById(R.id.busySumImage);
        type = itemView.findViewById(R.id.type);
        propertyBackground = itemView.findViewById(R.id.propertyBackground);
        propertyRestoreBackground = itemView.findViewById(R.id.propertyRestoreBackground);
        propertyForeground = itemView.findViewById(R.id.propertyForeground);
        this.inTrash = inTrash;

        addOnPropertyClickListener(activity, itemView);
    }

    private void addOnPropertyClickListener(@NonNull Activity activity, @NonNull View itemView) {
        if (!inTrash) {
            itemView.setOnClickListener(view -> {
                Pair[] pairs = new Pair[5];
                pairs[0] = new Pair<View, String>(propertyImage, "propertyImage");
                pairs[1] = new Pair<View, String>(name, "propertyName");
                pairs[2] = new Pair<View, String>(address, "propertyAddress");
                pairs[3] = new Pair<View, String>(availableSumImage, "propertyKeyAvailableSumImage");
                pairs[4] = new Pair<View, String>(busySumImage, "propertyKeyBusySumImage");

                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(activity, pairs).toBundle();
                Intent propertyDetails = new Intent(itemView.getContext(), PropertyDetails.class);
                propertyDetails.putExtra("property", property);
                view.getContext().startActivity(propertyDetails, bundle);
            });
        }
    }

    public void bind(@NonNull Activity activity, @NonNull Property property) {
        name.setText(property.getName());
        address.setText(property.getAddress());
        availableSum.setText(String.valueOf(property.getKeys().values().stream().filter(k -> k.getCheckedInDate() == null).count()));
        busySum.setText(String.valueOf(property.getKeys().values().stream().filter(k -> k.getCheckedInDate() != null).count()));
        type.setText(property.getType());
        this.property = property;
        ImageUtils.syncAndloadImagesProperty(activity, property.getId(), propertyImage, false);
    }

    @Override
    public RelativeLayout getBackground() {
        return propertyBackground;
    }

    @Override
    public RelativeLayout getRestoreBackground() {
        return propertyRestoreBackground;
    }

    @Override
    public RelativeLayout getForeground() {
        return propertyForeground;
    }
}
