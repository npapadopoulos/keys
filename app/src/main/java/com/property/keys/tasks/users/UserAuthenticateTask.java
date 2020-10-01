package com.property.keys.tasks.users;

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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.Container;
import com.property.keys.entities.User;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.util.Objects;
import java.util.function.Consumer;

import lombok.Builder;

@RequiresApi(api = Build.VERSION_CODES.R)
@Builder
public class UserAuthenticateTask extends AbstractAsyncTask {
    private static final String TAG = UserAuthenticateTask.class.getSimpleName();

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Context context;
    private final String email;
    private final String password;
    private final Consumer<Intent> startActivity;
    private boolean remember;
    private final Consumer<Task<AuthResult>> onAuthenticationFailed;
    private final Consumer<Exception> onFailed;

    @Override
    public void runInBackground() {
        firebaseAuth.signInWithEmailAndPassword(email, Utils.hash(password))
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        firebaseDatabase.getReference("users")
                                .child(firebaseAuth.getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            try {
                                                User user = snapshot.getValue(User.class);
                                                Objects.requireNonNull(user).setRemember(remember);
                                                Intent nextIntent = new Intent(context, Container.class);
                                                nextIntent.putExtra("fragment", "dashboard");
                                                nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                UserUtils.saveUser(user, password, context);
                                                startActivity.accept(nextIntent);
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
