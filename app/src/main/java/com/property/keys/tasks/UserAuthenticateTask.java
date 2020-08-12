package com.property.keys.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.Container;
import com.property.keys.entities.User;
import com.property.keys.utils.UserUtils;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;


@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class UserAuthenticateTask extends AbstractAsyncTask {
    private static final String TAG = UserAuthenticateTask.class.getSimpleName();

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Context context;
    private final String email;
    private final String password;
    private final Consumer<Intent> startDashboardActivity;
    private final Consumer<Task<AuthResult>> onAuthenticationFailed;
    private final Consumer<Exception> onFailed;

    @Override
    public void runInBackground() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        DatabaseReference users = firebaseDatabase.getReference("users");
                        Query query = users.child(firebaseAuth.getCurrentUser().getUid());
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot user) {
                                if (user.exists()) {
                                    try {
                                        Intent nextIntent = new Intent(context, Container.class);
                                        UserUtils.saveUser(user.getValue(User.class), context);
                                        nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startDashboardActivity.accept(nextIntent);
                                    } catch (Exception e) {
                                        onFailed.accept(e);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        // If sign in fails, display a message to the user.
                        onAuthenticationFailed.accept(task);
                    }
                });

    }
}
