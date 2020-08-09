package com.property.keys.holders;

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

    private TextView event;
    private TextView date;
    private CircularImageView userImage;

    public NotificationHolder(@NonNull View itemView) {
        super(itemView);

        event = itemView.findViewById(R.id.event);
        date = itemView.findViewById(R.id.date);
        userImage = itemView.findViewById(R.id.userImage);

    }

    public void bind(@NonNull Context context, @NonNull Notification notification) {
        event.setText(notification.getEvent());
        date.setText(notification.getDate());
        ImageUtils.syncAndloadImages(context, notification.getUserId(), userImage);
    }
}
