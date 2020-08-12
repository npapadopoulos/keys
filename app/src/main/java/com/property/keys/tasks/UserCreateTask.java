package com.property.keys.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.SignIn;
import com.property.keys.entities.User;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class UserCreateTask extends AbstractAsyncTask {
    private static final String TAG = UserCreateTask.class.getSimpleName();

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Context context;
    private final User user;
    private final Consumer<Intent> startActivity;
    private final Consumer<Task<AuthResult>> onCreationFailed;

    @Override
    public void runInBackground() {
        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        try {
                            DatabaseReference users = firebaseDatabase.getReference("users");

                            user.setId(firebaseAuth.getCurrentUser().getUid());
                            users.child(user.getId()).setValue(user);

                            Intent next = new Intent(context, SignIn.class);
                            next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity.accept(next);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to start activity.", e);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        onCreationFailed.accept(task);
                    }
                });
    }
}
