package com.property.keys.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.property.keys.R;
import com.property.keys.entities.Notification;
import com.property.keys.filters.FirebaseRecyclerAdapter;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NotificationAdapter extends FirebaseRecyclerAdapter<Notification, NotificationHolder> {
    @NonNull
    private Activity activity;

    public NotificationAdapter(@NonNull FirebaseRecyclerOptions<Notification> options, Activity activity) {
        super(options);
        this.activity = activity;
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
}
