package com.property.keys.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.property.keys.R;
import com.property.keys.entities.Notification;
import com.property.keys.utils.ImageUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NotificationHolder extends RecyclerView.ViewHolder {

    private TextView description;
    private TextView date;
    private CircularImageView userImage;

    private Activity activity;

    public NotificationHolder(@NonNull View itemView, Activity activity) {
        super(itemView);

        description = itemView.findViewById(R.id.description);
        date = itemView.findViewById(R.id.date);
        userImage = itemView.findViewById(R.id.userImage);

        this.activity = activity;
    }

    public void bind(@NonNull Context context, @NonNull Notification notification) {
        description.setText(notification.getDescription());
        date.setText(notification.getDate());
        ImageUtils.syncAndloadImages(context, notification.getUserId(), userImage);
    }
}
