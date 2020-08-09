package com.property.keys.holders;

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

import com.mikhaellopez.circularimageview.CircularImageView;
import com.property.keys.PropertyDetails;
import com.property.keys.R;
import com.property.keys.entities.Property;
import com.property.keys.utils.ImageUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyHolder extends RecyclerView.ViewHolder {

    private TextView name;
    private TextView address;
    private CircularImageView propertyImage;
    private ImageView availableSumImage, busySumImage;

    public PropertyHolder(RecyclerView.Adapter adapter, @NonNull Activity activity, @NonNull View itemView) {
        super(itemView);

        name = itemView.findViewById(R.id.name);
        address = itemView.findViewById(R.id.address);
        propertyImage = itemView.findViewById(R.id.propertyImage);
        availableSumImage = itemView.findViewById(R.id.availableSumImage);
        busySumImage = itemView.findViewById(R.id.busySumImage);

        itemView.setOnClickListener(view -> {
            Pair[] pairs = new Pair[5];
            pairs[0] = new Pair<View, String>(propertyImage, "propertyImage");
            pairs[1] = new Pair<View, String>(name, "propertyName");
            pairs[2] = new Pair<View, String>(address, "propertyAddress");
            pairs[3] = new Pair<View, String>(availableSumImage, "propertyKeyAvailableSumImage");
            pairs[4] = new Pair<View, String>(busySumImage, "propertyKeyBusySumImage");

            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(activity, pairs).toBundle();

            int position = getAdapterPosition();

//            if (position != RecyclerView.NO_POSITION && listener != null) {
//                listener.onItemClick(getSnapshots().getSnapshot(position), position);
//            }

            Intent propertyDetails = new Intent(itemView.getContext(), PropertyDetails.class);
//            propertyDetails.putExtra("property", )

            view.getContext().startActivity(propertyDetails, bundle);
        });

    }

    public void bind(@NonNull Context context, @NonNull Property property) {
        name.setText(property.getName());
        address.setText(property.getAddress());
        ImageUtils.syncAndloadImages(context, property.getId(), propertyImage);
    }
}
