package com.property.keys.tasks;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.entities.Action;
import com.property.keys.entities.Notification;
import com.property.keys.entities.Property;
import com.property.keys.entities.UnreadNotification;
import com.property.keys.entities.User;
import com.property.keys.utils.UserUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;

import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class NotificationCreateTask extends AbstractAsyncTask {
    private static final String TAG = NotificationCreateTask.class.getSimpleName();

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;

    private String description;
    private Property property;
    private String action;

    @Override
    public void runInBackground() {
        String userId = UserUtils.getLocalUser(activity.getApplicationContext()).getId();
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .date(DATE_TIME_FORMATTER.format(LocalDateTime.now()))
                .description(description)
                .userId(userId)
                .build();

        DatabaseReference notifications = firebaseDatabase.getReference("notifications");
        notifications.child(notification.getId()).setValue(notification)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Created notification " + notification.getDescription() + ".");
                    } else {
                        Log.i(TAG, "Failed to create notification " + notification.getDescription() + ".");
                    }
                });

        DatabaseReference unreadNotifications = firebaseDatabase.getReference("unread_notifications");
        if (Action.ADDED_PROPERTY.name().equals(action) || Action.DELETED_PROPERTY.name().equals(action)) {
            firebaseDatabase.getReference("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        unreadNotifications.child(user.getId()).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UnreadNotification unreadNotification = snapshot.getValue(UnreadNotification.class);
                                if (unreadNotification == null) {
                                    unreadNotification = UnreadNotification.builder()
                                            .userId(user.getId())
                                            .notificationIds(new HashMap<>())
                                            .build();
                                }

                                unreadNotification.getNotificationIds().put(notification.getId(), Boolean.TRUE);
                                unreadNotifications.child(user.getId()).setValue(unreadNotification);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                //do nothing for the moment
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        if (property.getFavouredBy() != null) {
            property.getFavouredBy().keySet().forEach(id -> {
                if (!id.equals(notification.getUserId())) {
                    unreadNotifications.child(id).getRef().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UnreadNotification unreadNotification = snapshot.getValue(UnreadNotification.class);
                            if (unreadNotification == null) {
                                unreadNotification = UnreadNotification.builder()
                                        .userId(id)
                                        .notificationIds(new HashMap<>())
                                        .build();
                            }

                            unreadNotification.getNotificationIds().put(notification.getId(), Boolean.TRUE);
                            unreadNotifications.child(id).setValue(unreadNotification);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //do nothing for the moment
                        }
                    });
                }
            });
        }
    }
}
