package com.property.keys.tasks.users;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.entities.Notification;
import com.property.keys.tasks.AbstractAsyncTask;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;

@RequiresApi(api = Build.VERSION_CODES.R)
@Builder
public class UserReadNotificationsTask extends AbstractAsyncTask {
    private static final String TAG = UserReadNotificationsTask.class.getSimpleName();

    private static DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users");

    private String userId;
    private String notificationId;

    @Override
    public Void doInBackground(Void... voids) {
        userReference.child(userId).child("notifications").orderByChild("unread").equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot notifications) {
                        Map<String, Object> updates = new HashMap<>();

                        notifications.getChildren().forEach(snapshot -> {
                            Notification notification = snapshot.getValue(Notification.class);
                            if (notification != null) {
                                if (notificationId == null || notification.getId().equals(notificationId)) {
                                    notification.setUnread(false);
                                    updates.put("/" + userId + "/notifications/" + notification.getId(), notification);
                                }
                            }
                        });
                        if (!updates.isEmpty()) {
                            userReference.updateChildren(updates);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        return null;
    }
}
