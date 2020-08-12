package com.property.keys.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.property.keys.Container;
import com.property.keys.entities.User;
import com.property.keys.tasks.ResetPasswordTask;
import com.property.keys.tasks.TaskExecutor;
import com.property.keys.tasks.UserAuthenticateTask;
import com.property.keys.tasks.UserCreateTask;
import com.property.keys.tasks.UserUpdateTask;

import java.util.Map;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toSet;

@RequiresApi(api = Build.VERSION_CODES.R)
public class UserUtils {

    private static final String TAG = UserUtils.class.getSimpleName();

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private UserUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void authenticate(Activity activity, Context context, String email, String password,
                                    Consumer<Intent> startActivity,
                                    Consumer<Task<AuthResult>> onAuthenticationFailed,
                                    Consumer<Exception> onFailed) {
        new TaskExecutor().executeAsync(new UserAuthenticateTask(activity, context, email, password, startActivity, onAuthenticationFailed, onFailed));
    }

    public static void create(Activity activity, Context context, User user,
                              Consumer<Intent> startActivity,
                              Consumer<Task<AuthResult>> onCreationFailed) {
        new TaskExecutor().executeAsync(new UserCreateTask(activity, context, user, startActivity, onCreationFailed));
    }

    public static void update(Fragment fragment, User currentUser, User newUser, Consumer<Task<Void>> onUpdateFailed, Consumer<Task<Void>> onUpdateSucceeded) {
        new TaskExecutor().executeAsync(new UserUpdateTask(fragment, currentUser, newUser, onUpdateFailed, onUpdateSucceeded));
    }


    public static void resetPassword(Context context, String phoneNumber, String password, PhoneAuthCredential credentialByPhone,
                                     Consumer<Intent> startActivity,
                                     Consumer<Task<AuthResult>> onResetFailed) {
        new TaskExecutor().executeAsync(new ResetPasswordTask(context, phoneNumber, password, credentialByPhone, startActivity, onResetFailed));
    }

    public static void signOut(Context context) {
        PropertyUtils.setNotificationCount(context, Container.UNREAD.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(toSet()));
        firebaseAuth.signOut();
    }

    public static User getUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        return User.builder()
                .id(sharedPreferences.getString("id", firebaseAuth.getCurrentUser() == null ? "" : firebaseAuth.getCurrentUser().getUid()))
                .firstName(sharedPreferences.getString("firstName", ""))
                .lastName(sharedPreferences.getString("lastName", ""))
                .email(sharedPreferences.getString("email", "").toLowerCase().trim())
                .phoneNumber(sharedPreferences.getString("phoneNumber", ""))
                .build();
    }

    public static void saveUser(User user, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", user.getId());
        editor.putString("firstName", user.getFirstName());
        editor.putString("lastName", user.getLastName());
        editor.putString("email", user.getEmail());
        editor.putString("phoneNumber", user.getPhoneNumber());
        editor.apply();
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
