package com.property.keys.tasks.users;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.User;
import com.property.keys.tasks.AbstractAsyncTask;

import java.util.function.Consumer;

import lombok.Builder;

@RequiresApi(api = Build.VERSION_CODES.R)
@Builder
public class UserUpdateTask extends AbstractAsyncTask {
    private static final String TAG = UserUpdateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final Consumer<Task<Void>> onUpdateFailed;
    private final Consumer<Task<Void>> onUpdateSucceeded;
    private User user;

    @Override
    public void runInBackground() {
        firebaseDatabase.getReference("users").child(user.getId()).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Updated user's details with id " + user.getId() + ".");
                        if (onUpdateSucceeded != null)
                            onUpdateSucceeded.accept(task);
                    } else {
                        if (onUpdateFailed != null)
                            onUpdateFailed.accept(task);
                    }
                });
    }
}
