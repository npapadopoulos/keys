package com.property.keys.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.property.keys.R;
import com.property.keys.entities.Notification;
import com.property.keys.filters.FirebaseRecyclerAdapter;
import com.property.keys.filters.FirebaseRecyclerOptions;

import org.jetbrains.annotations.NotNull;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NotificationAdapter extends FirebaseRecyclerAdapter<Notification, NotificationHolder> {

    @NonNull
    private Activity activity;
    private FloatingActionButton deleteNotifications;
    private LinearLayout emptyNotifications;

    public NotificationAdapter(@NonNull FirebaseRecyclerOptions<Notification> options, @NotNull Activity activity) {
        super(options, false, Notification.class, activity.findViewById(R.id.progressBar));
        this.activity = activity;
        deleteNotifications = activity.findViewById(R.id.deleteNotifications);
        emptyNotifications = activity.findViewById(R.id.empty_notifications);
    }

    @Override
    protected void onBindViewHolder(@NonNull NotificationHolder holder, int position, @NonNull Notification model) {
        holder.bind(activity, model);
    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationHolder(activity, LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification, parent, false));
    }

    @Override
    public void onDataChanged() {
        if (getItemCount() == 0) {
            emptyNotifications.setVisibility(View.VISIBLE);
            deleteNotifications.setVisibility(View.INVISIBLE);
        } else {
            emptyNotifications.setVisibility(View.INVISIBLE);
            deleteNotifications.setVisibility(View.VISIBLE);
        }
        super.onDataChanged();
    }

    @NonNull
    @Override
    public String getId(Notification notification) {
        return notification.getId();
    }
}
