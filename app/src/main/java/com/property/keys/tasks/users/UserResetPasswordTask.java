package com.property.keys.tasks.users;


import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.SignIn;
import com.property.keys.entities.User;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import lombok.Builder;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.R)
@Builder
public class UserResetPasswordTask extends AbstractAsyncTask {
    private static final String TAG = UserResetPasswordTask.class.getSimpleName();

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Context context;
    private final String phoneNumber;
    private final String password;
    private final PhoneAuthCredential credentialByPhone;
    private final Consumer<Intent> startActivity;
    private final Consumer<Task<AuthResult>> onResetFailed;

    @Override
    public Void doInBackground(Void... voids) {
        firebaseAuth.signInWithCredential(credentialByPhone)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        DatabaseReference users = firebaseDatabase.getReference("users");

                        Query query = users.orderByChild("phoneNumber").equalTo(phoneNumber);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot user) {
                                if (user.exists()) {
                                    try {
                                        firebaseAuth.signOut(); //by phone

                                        Map<String, Object> updates = new HashMap<String, Object>();
                                        updates.put("password", Utils.hash(password));
                                        User localUser = user.getValue(User.class);
                                        users.child(localUser.getId()).updateChildren(updates);

                                        Intent nextIntent = new Intent(context, SignIn.class);
                                        startActivity.accept(nextIntent);
                                    } catch (Exception e) {
                                        Timber.tag(TAG).e(e, "Failed to start Sign activity.");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        startActivity.accept(new Intent(context, SignIn.class));
                    } else {
                        onResetFailed.accept(task);
                    }
                });
        return null;
    }
}
