package com.property.keys.adapters;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.property.keys.R;
import com.property.keys.entities.Notification;
import com.property.keys.filters.FirebaseRecyclerAdapter;

import org.jetbrains.annotations.NotNull;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NotificationAdapter extends FirebaseRecyclerAdapter<Notification, NotificationHolder> {
    @NonNull
    private Activity activity;
    private FloatingActionButton deleteNotifications;

    public NotificationAdapter(@NonNull FirebaseRecyclerOptions<Notification> options, @NotNull Activity activity) {
        super(options);
        this.activity = activity;
        deleteNotifications = activity.findViewById(R.id.deleteNotifications);
    }

    @Override
    protected void onBindViewHolder(@NonNull NotificationHolder holder, int position, @NonNull Notification model) {
        holder.bind(activity, model);
    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification, parent, false));
    }

    @Override
    public void onDataChanged() {
        if (getItemCount() == 0) {
            deleteNotifications.setEnabled(false);
            deleteNotifications.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.dark_gray)));

        } else {
            deleteNotifications.setEnabled(true);
            deleteNotifications.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.primaryColor)));
        }
    }
}