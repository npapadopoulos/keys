package com.property.keys.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.property.keys.Container;
import com.property.keys.SignIn;
import com.property.keys.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@RequiresApi(api = Build.VERSION_CODES.R)
public class LoginUtils {

    private static final String TAG = LoginUtils.class.getSimpleName();

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private LoginUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void authenticate(Activity activity, Context context, String email, String password,
                                    Consumer<Intent> startDashboardActivity,
                                    Consumer<Task<AuthResult>> onAuthenticationFailed,
                                    Consumer<Exception> onFailed) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        DatabaseReference users = firebaseDatabase.getReference("users");
                        Query query = users.child(UUID.nameUUIDFromBytes(email.getBytes()).toString());
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot user) {
                                if (user.exists()) {
                                    try {
                                        Intent nextIntent = new Intent(context, Container.class);
                                        Utils.saveUser(user.getValue(User.class), context);
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

    public static void createUser(Activity activity, Context context, User user,
                                  Consumer<Intent> startDashboardActivity,
                                  Consumer<Task<AuthResult>> onCreationFailed) {
        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        try {
                            DatabaseReference users = firebaseDatabase.getReference("users");
                            users.child(user.getId()).setValue(user);

                            Intent dashboard = new Intent(context, Container.class);
                            dashboard.putExtra("user", user);

                            dashboard.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startDashboardActivity.accept(dashboard);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to start Dashboard activity.", e);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        onCreationFailed.accept(task);
                    }
                });
    }


    public static void resetPassword(Context context, String phoneNumber, String password, PhoneAuthCredential credential,
                                     Consumer<Intent> startDashboardActivity,
                                     Consumer<Task<AuthResult>> onResetFailed) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> updates = new HashMap<String, Object>();
                        updates.put("password", password);

                        DatabaseReference users = firebaseDatabase.getReference("users");
                        Query query = users.orderByChild("phoneNumber").equalTo(phoneNumber);
                        query.addValueEventListener(new ValueEventListener() {
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

    public static void signOut() {
        firebaseAuth.signOut();
    }

    public boolean icConnectedToInternet(Context context) {
        /**
         * TODO Need to add listener once connectivity is lost popup a dialog with that info and add a constrait to every ACTION which requires connection to Internet
         */
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
