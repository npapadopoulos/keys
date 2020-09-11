package com.property.keys.tasks.notifications;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.Notification;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.UserUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
@Builder
public class NotificationCreateTask extends AbstractAsyncTask {
    private static final String TAG = NotificationCreateTask.class.getSimpleName();

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;

    private final String description;
    private final Set<String> usersToNotify;

    /**
     * Adds notification to all users who liked current property except of the user who acted on the property.
     */
    @Override
    public void runInBackground() {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .date(DATE_TIME_FORMATTER.format(LocalDateTime.now()))
                .description(description)
                .userId(UserUtils.getLocalUser(activity).getId())
                .build();

        DatabaseReference notifications = firebaseDatabase.getReference("notifications");
        notifications.child(notification.getId()).setValue(notification)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Created notification '" + notification.getDescription() + "'.");
                        if (usersToNotify != null) {
                            Map<String, Object> updates = new HashMap<>();
                            notification.setUnread(true);
                            usersToNotify.stream()
                                    .filter(id -> !notification.getUserId().equals(id))
                                    .forEach(userId -> updates.put("/" + userId + "/notifications/" + notification.getId(), notification));

                            if (!updates.isEmpty()) {
                                firebaseDatabase.getReference("users").updateChildren(updates);
                            }
                        }
                    } else {
                        Log.i(TAG, "Failed to create notification '" + notification.getDescription() + "'.");
                    }
                });
    }
}
