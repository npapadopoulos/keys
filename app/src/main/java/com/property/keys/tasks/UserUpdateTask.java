package com.property.keys.tasks;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.User;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class UserUpdateTask extends AbstractAsyncTask {
    private static final String TAG = UserUpdateTask.class.getSimpleName();

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Fragment fragment;
    private final User currentUser;
    private final User newUser;
    private final Consumer<Task<Void>> onUpdateFailed;
    private final Consumer<Task<Void>> onUpdateSucceeded;

    @Override
    public void runInBackground() {
        //update auth
        if (!currentUser.getEmail().equalsIgnoreCase(newUser.getEmail())) {
            firebaseAuth.getCurrentUser().updateEmail(newUser.getEmail());
            Log.i(TAG, "Updated auth user's email with id " + newUser.getId() + ".");
        }

        //update real time database
        firebaseDatabase.getReference("users").child(newUser.getId()).setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Updated user's details with id " + newUser.getId() + ".");
                        onUpdateSucceeded.accept(task);
                    } else {
                        onUpdateFailed.accept(task);
                    }
                });
    }
}
