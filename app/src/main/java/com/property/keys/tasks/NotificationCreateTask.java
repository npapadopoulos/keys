package com.property.keys.tasks;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private String userId;

    @Override
    public void runInBackground() {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .date(DATE_TIME_FORMATTER.format(LocalDateTime.now()))
                .description(description)
                .userId(userId)
                .build();

        DatabaseReference properties = firebaseDatabase.getReference("notifications");
        properties.child(notification.getId()).setValue(notification)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Created notification " + notification.getDescription() + ".");
                    } else {
                        Log.i(TAG, "Failed to create notification " + notification.getDescription() + ".");
                    }
                });

    }
}
