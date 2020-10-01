package com.property.keys.tasks.notifications;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.Action;
import com.property.keys.entities.Notification;
import com.property.keys.entities.User;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.UserUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import timber.log.Timber;

import static com.property.keys.utils.Utils.DATE_TIME_FORMATTER;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
@Builder
public class NotificationCreateTask extends AbstractAsyncTask {
    private static final String TAG = NotificationCreateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;

    private final String propertyId;
    private final String description;
    private final Set<String> usersToNotify;
    private final Action action;

    /**
     * Adds notification to all users who liked current property except of the user who acted on the property.
     */
    @Override
    public void runInBackground() {
        User localUser = UserUtils.getLocalUser(activity);
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .date(DATE_TIME_FORMATTER.format(LocalDateTime.now()))
                .description(description)
                .userId(localUser.getId())
                .propertyId(propertyId)
                .firstName(localUser.getFirstName())
                .lastName(localUser.getLastName())
                .action(action)
                .build();

        DatabaseReference notifications = firebaseDatabase.getReference("notifications");
        notifications.child(notification.getId()).setValue(notification)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Timber.tag(TAG).i("Created notification '" + notification.getDescription() + "'.");
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
                        Timber.tag(TAG).i("Failed to create notification '" + notification.getDescription() + "'.");
                    }
                });
    }
//
//    private void createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getString(R.string.channel_name);
//            String description = getString(R.string.channel_description);
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }
}
