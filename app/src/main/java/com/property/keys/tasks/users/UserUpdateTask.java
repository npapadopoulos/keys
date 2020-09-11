package com.property.keys.tasks.users;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.User;
import com.property.keys.tasks.AbstractAsyncTask;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import lombok.Builder;

@RequiresApi(api = Build.VERSION_CODES.R)
@Builder
public class UserUpdateTask extends AbstractAsyncTask {
    private static final String TAG = UserUpdateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final Consumer<Exception> onUpdateFailed;
    private final Consumer<Task<Void>> onUpdateSucceeded;
    private User user;

    @Override
    public void runInBackground() {
        final Map<String, Object> updates = new HashMap<>();
        updates.put("/" + user.getId() + "/firstName/", user.getFirstName());
        updates.put("/" + user.getId() + "/lastName/", user.getLastName());
        updates.put("/" + user.getId() + "/email/", user.getEmail());
        updates.put("/" + user.getId() + "/phoneNumber/", user.getPhoneNumber());
        firebaseDatabase.getReference("users").updateChildren(updates)
                .addOnCompleteListener(onUpdateSucceeded::accept)
                .addOnFailureListener(onUpdateFailed::accept);
    }
}
