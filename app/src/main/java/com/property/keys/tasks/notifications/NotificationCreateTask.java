package com.property.keys.tasks.notifications;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.entities.Action;
import com.property.keys.entities.HistoryDetails;
import com.property.keys.entities.Key;
import com.property.keys.entities.Notification;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.tasks.AbstractAsyncTask;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

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

    private final User user;
    private final Property property;
    private final Key key;
    private final String description;
    private final Action action;

    /**
     * Adds notification to all users who liked current property except of the user who acted on the property.
     */
    @Override
    public void runInBackground() {
        record(user);

        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .date(DATE_TIME_FORMATTER.format(LocalDateTime.now()))
                .description(description)
                .userId(user.getId())
                .propertyId(property.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .action(action)
                .build();

        update("notifications", notification.getId(), notification, task -> {
            if (task.isSuccessful()) {
                firebaseDatabase.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Object> updates = new HashMap<>();
                        snapshot.getChildren().forEach(userSnapshot -> {
                            User user = userSnapshot.getValue(User.class);
                            Timber.tag(TAG).i("Created notification '" + notification.getDescription() + "'.");
                            notification.setUnread(true);
                            if (!notification.getUserId().equals(user.getId())) {
                                updates.put("/" + user.getId() + "/notifications/" + notification.getId(), notification);
                            }
                        });
                        if (!updates.isEmpty()) {
                            firebaseDatabase.getReference("users").updateChildren(updates);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                Timber.tag(TAG).i("Failed to create notification '" + notification.getDescription() + "'.");
            }
        });
    }

    private void record(User user) {
        if (key != null && key.getCheckinReason() != null && action == Action.CHECKED_IN || action == Action.CHECKED_OUT) {
            HistoryDetails historyDetails = HistoryDetails.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .propertyId(property.getId())
                    .propertyName(property.getName())
                    .keyId(key.getId())
                    .reason(key.getCheckinReason())
                    .build();

            update("history", historyDetails.getId(), historyDetails, task -> {
            });

        }
    }

    private void update(String reference, String id, Object data, Consumer<Task<Void>> postUpdate) {
        DatabaseReference databaseReference = firebaseDatabase.getReference(reference);
        databaseReference.child(id).setValue(data).addOnCompleteListener(activity, postUpdate::accept);
    }

}
