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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.property.keys.PropertyDetails;
import com.property.keys.R;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.utils.ImageUtils;

import static com.property.keys.utils.Utils.updateFavourite;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyHolder extends RecyclerView.ViewHolder implements Holder {

    private TextView name;
    private TextView address;
    private ImageView propertyImage;
    private ImageView availableSumImage, busySumImage;
    private FloatingActionButton setFavourite;
    private RelativeLayout propertyBackground, propertyForeground;
    private Property property;
    private User user;

    public PropertyHolder(@NonNull Activity activity, User user, @NonNull View itemView) {
        super(itemView);

        this.user = user;
        name = itemView.findViewById(R.id.name);
        address = itemView.findViewById(R.id.address);
        propertyImage = itemView.findViewById(R.id.propertyImage);
        availableSumImage = itemView.findViewById(R.id.availableSumImage);
        busySumImage = itemView.findViewById(R.id.busySumImage);
        propertyBackground = itemView.findViewById(R.id.propertyBackground);
        propertyForeground = itemView.findViewById(R.id.propertyForeground);

        addOnSetFavouriteClickListener(activity, itemView);
        addOnPropertyClickListener(activity, itemView);
    }

    private void addOnPropertyClickListener(@NonNull Activity activity, @NonNull View itemView) {
        itemView.setOnClickListener(view -> {
            Pair[] pairs = new Pair[6];
            pairs[0] = new Pair<View, String>(propertyImage, "propertyImage");
            pairs[1] = new Pair<View, String>(name, "propertyName");
            pairs[2] = new Pair<View, String>(address, "propertyAddress");
            pairs[3] = new Pair<View, String>(availableSumImage, "propertyKeyAvailableSumImage");
            pairs[4] = new Pair<View, String>(busySumImage, "propertyKeyBusySumImage");
            pairs[5] = new Pair<View, String>(setFavourite, "favourite");

            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(activity, pairs).toBundle();
            Intent propertyDetails = new Intent(itemView.getContext(), PropertyDetails.class);
            propertyDetails.putExtra("property", property);

            view.getContext().startActivity(propertyDetails, bundle);
        });
    }

    private void addOnSetFavouriteClickListener(@NonNull Activity activity, @NonNull View itemView) {
        setFavourite = itemView.findViewById(R.id.setFavourite);
        setFavourite.setOnClickListener(view -> updateFavourite(activity, setFavourite, property, user));
    }

    public void bind(@NonNull Activity activity, @NonNull Property property) {
        name.setText(property.getName());
        address.setText(property.getAddress());
        this.property = property;
        ImageUtils.syncAndloadImages(activity, property.getId(), propertyImage);
        updateFavourite(activity, setFavourite, property.getFavouredBy().containsKey(user.getId()));
    }

    @Override
    public RelativeLayout getBackground() {
        return propertyBackground;
    }

    @Override
    public RelativeLayout getForeground() {
        return propertyForeground;
    }
}
