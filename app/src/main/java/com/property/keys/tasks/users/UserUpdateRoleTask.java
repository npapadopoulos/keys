package com.property.keys.tasks.users;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.tasks.AbstractAsyncTask;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;

import static com.property.keys.entities.Role.ADMIN;
import static com.property.keys.entities.Role.BASIC;

@RequiresApi(api = Build.VERSION_CODES.R)
@Builder
public class UserUpdateRoleTask extends AbstractAsyncTask {
    private static final String TAG = UserUpdateRoleTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private String userId;
    private boolean isAdmin;

    @Override
    public void runInBackground() {
        final Map<String, Object> updates = new HashMap<>();
        updates.put("/" + userId + "/role/", isAdmin ? ADMIN : BASIC);
        firebaseDatabase.getReference("users").updateChildren(updates);
    }
}
