package com.property.keys.tasks.users;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.SignIn;
import com.property.keys.entities.User;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.Utils;

import java.util.function.Consumer;

import lombok.Builder;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.R)
@Builder
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
    public Void doInBackground(Void... voids) {
        String hashedPassword = Utils.hash(user.getPassword());
        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), hashedPassword)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        try {
                            DatabaseReference users = firebaseDatabase.getReference("users");
                            user.setId(firebaseAuth.getCurrentUser().getUid());
                            user.setPassword(hashedPassword);
                            users.child(user.getId()).setValue(user);

                            Intent next = new Intent(context, SignIn.class);
                            next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity.accept(next);
                        } catch (Exception e) {
                            Timber.tag(TAG).e(e, "Failed to start activity.");
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        onCreationFailed.accept(task);
                    }
                });
        return null;
    }
}
