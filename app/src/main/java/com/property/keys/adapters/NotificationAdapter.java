package com.property.keys.adapters;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.PropertyDetails;
import com.property.keys.R;
import com.property.keys.entities.Notification;
import com.property.keys.entities.Property;
import com.property.keys.filters.FirebaseRecyclerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NotificationAdapter extends FirebaseRecyclerAdapter<Notification, NotificationHolder> {

    private static final DatabaseReference properties = FirebaseDatabase.getInstance().getReference("properties");

    @NonNull
    private Activity activity;
    private FloatingActionButton deleteNotifications;
    private LinearLayout emptyNotifications;
    private String userId;

    public NotificationAdapter(@NonNull FirebaseRecyclerOptions<Notification> options, @NotNull Activity activity, String userId) {
        super(options, false);
        this.activity = activity;
        deleteNotifications = activity.findViewById(R.id.deleteNotifications);
        emptyNotifications = activity.findViewById(R.id.empty_notifications);
        this.userId = userId;

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
    public void onChildChanged(@NonNull ChangeEventType type, @NonNull DataSnapshot snapshot, int newIndex, int oldIndex) {
        super.onChildChanged(type, snapshot, newIndex, oldIndex);

        Notification notification = snapshot.getValue(Notification.class);

        if (notification != null && notification.getPropertyId() != null) {
            properties.child(notification.getPropertyId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Property property = snapshot.getValue(Property.class);
                            if (property != null) {
                                Intent propertyDetails = new Intent(activity, PropertyDetails.class);
                                propertyDetails.putExtra("property", property);
                                propertyDetails.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, propertyDetails, 0);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, userId)
                                        .setSmallIcon(R.drawable.keys)
                                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getDescription()))
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                        // Set the intent that will fire when the user taps the notification
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);

                                // notificationId is a unique int for each notification that you must define
                                notificationManager.notify(new AtomicInteger().incrementAndGet(), builder.build());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
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
    }

    @NonNull
    @Override
    public String getId(Notification notification) {
        return notification.getId();
    }
}
