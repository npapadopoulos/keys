package com.property.keys.tasks.users;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.tasks.AbstractAsyncTask;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;

@RequiresApi(api = Build.VERSION_CODES.R)
@Builder
public class UserDeleteNotificationsTask extends AbstractAsyncTask {
    private static final String TAG = UserDeleteNotificationsTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final String notificationId;
    private final boolean all;
    private String userId;

    @Override
    public void runInBackground() {
        Map<String, Object> updates = new HashMap<>();
        if (all && notificationId == null) {
            updates.put("/" + userId + "/notifications", null);
        } else {
            updates.put("/" + userId + "/notifications/" + notificationId, null);
        }

        if (!updates.isEmpty()) {
            firebaseDatabase.getReference("users").updateChildren(updates);
        }
    }
}
