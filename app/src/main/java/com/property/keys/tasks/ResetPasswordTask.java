package com.property.keys.tasks;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class ResetPasswordTask extends AbstractAsyncTask {
    private static final String TAG = ResetPasswordTask.class.getSimpleName();

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Context context;
    private final String phoneNumber;
    private final String password;
    private final PhoneAuthCredential credentialByPhone;
    private final Consumer<Intent> startDashboardActivity;
    private final Consumer<Task<AuthResult>> onResetFailed;

    @Override
    public void runInBackground() {
        firebaseAuth.signInWithCredential(credentialByPhone)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> updates = new HashMap<String, Object>();
                        updates.put("password", password);

                        DatabaseReference users = firebaseDatabase.getReference("users");
                        Query query = users.orderByChild("phoneNumber").equalTo(phoneNumber);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot user) {
                                if (user.exists()) {
                                    try {
                                        firebaseAuth.signOut(); //by phone
                                        Intent nextIntent = new Intent(context, SignIn.class);
                                        startDashboardActivity.accept(nextIntent);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Failed to start Sign activity.", e);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        startDashboardActivity.accept(new Intent(context, SignIn.class));
                    } else {
                        onResetFailed.accept(task);
                    }
                });
    }
}
