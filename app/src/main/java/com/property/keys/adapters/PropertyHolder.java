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

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.property.keys.PropertyDetails;
import com.property.keys.R;
import com.property.keys.entities.Property;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.PropertyUtils;
import com.property.keys.utils.UserUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyHolder extends RecyclerView.ViewHolder {

    private TextView name;
    private TextView address;
    private ImageView propertyImage;
    private ImageView availableSumImage, busySumImage;
    private MaterialFavoriteButton setFavourite;
    private Property property;

    public PropertyHolder(@NonNull Activity activity, @NonNull View itemView) {
        super(itemView);

        name = itemView.findViewById(R.id.name);
        address = itemView.findViewById(R.id.address);
        propertyImage = itemView.findViewById(R.id.propertyImage);
        availableSumImage = itemView.findViewById(R.id.availableSumImage);
        busySumImage = itemView.findViewById(R.id.busySumImage);

        setFavourite = itemView.findViewById(R.id.setFavourite);
        setFavourite.setOnFavoriteChangeListener((buttonView, favorite) -> PropertyUtils.update(activity, property, favorite));

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
            //FIXME probably back button on details has to do with the above. ACTIVITY STARTS FROM PROPERTY_HOLDER INSTEAD OF THE CONTAINER
        });

    }

    public void bind(@NonNull Context context, @NonNull Property property) {
        name.setText(property.getName());
        address.setText(property.getAddress());
        this.property = property;
        ImageUtils.syncAndloadImages(context, property.getId(), propertyImage);
        setFavourite.setFavoriteSuppressListener(property.getFavouredBy().containsKey(UserUtils.getLocalUser(context).getId()));
    }
}
