package com.property.keys.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.property.keys.Container;
import com.property.keys.PropertyDetails;
import com.property.keys.R;
import com.property.keys.entities.Notification;
import com.property.keys.entities.Property;
import com.property.keys.utils.ImageUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NotificationHolder extends RecyclerView.ViewHolder implements Holder {

    private static final DatabaseReference properties = FirebaseDatabase.getInstance().getReference("properties");

    private TextView description;
    private TextView date;
    private CircularImageView userImage;
    private RelativeLayout notificationBackground, notificationForeground;

    private Property property;

    public NotificationHolder(@NonNull Activity activity, @NonNull View itemView) {
        super(itemView);

        description = itemView.findViewById(R.id.description);
        date = itemView.findViewById(R.id.date);
        userImage = itemView.findViewById(R.id.userImage);
        notificationBackground = itemView.findViewById(R.id.notificationBackground);
        notificationForeground = itemView.findViewById(R.id.notificationForeground);

        addOnPropertyClickListener(activity, itemView);
    }

    private void addOnPropertyClickListener(@NonNull Activity activity, @NonNull View itemView) {
        itemView.setOnClickListener(view -> {
            if (property == null) {
                Snackbar.make(((Container) activity).getPlaceSnackBar(), "The current property has been deleted.", Snackbar.LENGTH_LONG).show();
            } else {
                Intent propertyDetails = new Intent(itemView.getContext(), PropertyDetails.class);
                propertyDetails.putExtra("property", property);
                view.getContext().startActivity(propertyDetails);
            }
        });
    }


    public void bind(@NonNull Context context, @NonNull Notification notification) {
        description.setText(notification.getDescription());
        date.setText(notification.getDate());

        properties.child(notification.getPropertyId()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                property = snapshot.getValue(Property.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ImageUtils.syncAndLoadImagesProfile(context, notification.getUserId(), notification.getFirstName(), notification.getLastName(), userImage);
    }

    @Override
    public RelativeLayout getBackground() {
        return notificationBackground;
    }

    @Override
    public RelativeLayout getForeground() {
        return notificationForeground;
    }
}
