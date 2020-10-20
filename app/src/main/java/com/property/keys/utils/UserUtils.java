package com.property.keys.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.property.keys.entities.Role;
import com.property.keys.entities.User;
import com.property.keys.tasks.TaskExecutor;
import com.property.keys.tasks.users.UserAuthenticateTask;
import com.property.keys.tasks.users.UserCreateTask;
import com.property.keys.tasks.users.UserDeleteNotificationsTask;
import com.property.keys.tasks.users.UserPropertySearchSuggestionsUpdateTask;
import com.property.keys.tasks.users.UserReadNotificationsTask;
import com.property.keys.tasks.users.UserResetPasswordTask;
import com.property.keys.tasks.users.UserUpdateRoleTask;
import com.property.keys.tasks.users.UserUpdateTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Collections.emptySet;

@RequiresApi(api = Build.VERSION_CODES.R)
public class UserUtils {

    private static final String TAG = UserUtils.class.getSimpleName();

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private UserUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void authenticate(Activity activity, Context context, String email, String password, boolean remember,
                                    Consumer<Intent> startActivity,
                                    Consumer<Task<AuthResult>> onAuthenticationFailed,
                                    Consumer<Exception> onFailed) {
        new TaskExecutor().executeAsync(
                UserAuthenticateTask.builder()
                        .activity(activity)
                        .context(context)
                        .email(email)
                        .password(password)
                        .remember(remember)
                        .startActivity(startActivity)
                        .onAuthenticationFailed(onAuthenticationFailed)
                        .onFailed(onFailed)
                        .build()
        );
    }

    public static void create(Activity activity, Context context, User user,
                              Consumer<Intent> startActivity,
                              Consumer<Task<AuthResult>> onCreationFailed) {
        new TaskExecutor().executeAsync(
                UserCreateTask.builder()
                        .activity(activity)
                        .context(context)
                        .user(user)
                        .startActivity(startActivity)
                        .onCreationFailed(onCreationFailed)
                        .build()
        );
    }

    public static void updateBasics(User user, Consumer<Exception> onUpdateFailed, Consumer<Task<Void>> onUpdateSucceeded) {
        new TaskExecutor().executeAsync(
                UserUpdateTask.builder()
                        .user(user)
                        .onUpdateFailed(onUpdateFailed)
                        .onUpdateSucceeded(onUpdateSucceeded)
                        .build()
        );
    }

    public static void updateRole(String userId, boolean isAdmin) {
        new TaskExecutor().executeAsync(
                UserUpdateRoleTask.builder()
                        .userId(userId)
                        .isAdmin(isAdmin)
                        .build()
        );
    }

    public static void setReadNotifications(String userId) {
        new TaskExecutor().executeAsync(
                UserReadNotificationsTask.builder()
                        .userId(userId)
                        .build()
        );
    }

    public static void setReadNotifications(String userId, String notificationId) {
        new TaskExecutor().executeAsync(
                UserReadNotificationsTask.builder()
                        .userId(userId)
                        .notificationId(notificationId)
                        .build()
        );
    }

    public static void deleteNotifications(String userId) {
        deleteNotification(userId, null, true);
    }

    public static void deleteNotification(String userId, String notificationId) {
        deleteNotification(userId, notificationId, false);
    }

    public static void deleteNotification(String userId, String notificationId, boolean all) {
        new TaskExecutor().executeAsync(
                UserDeleteNotificationsTask.builder()
                        .userId(userId)
                        .notificationId(notificationId)
                        .all(all)
                        .build()
        );
    }

    public static void updateSearchSuggestions(Context context, String userId, List<String> propertySearchSuggestions) {
        new TaskExecutor().executeAsync(
                UserPropertySearchSuggestionsUpdateTask.builder()
                        .context(context)
                        .userId(userId)
                        .propertySearchSuggestions(propertySearchSuggestions)
                        .build()
        );
    }

    public static void resetPassword(Context context, String phoneNumber, String password, PhoneAuthCredential credentialByPhone,
                                     Consumer<Intent> startActivity,
                                     Consumer<Task<AuthResult>> onResetFailed) {
        new TaskExecutor().executeAsync(
                UserResetPasswordTask.builder()
                        .context(context)
                        .phoneNumber(phoneNumber)
                        .password(password)
                        .credentialByPhone(credentialByPhone)
                        .startActivity(startActivity)
                        .onResetFailed(onResetFailed)
                        .build()
        );
    }

    public static void signOut() {
        firebaseAuth.signOut();
    }

    public static User getLocalUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        return User.builder()
                .id(sharedPreferences.getString("id", firebaseAuth.getCurrentUser() == null ? "" : firebaseAuth.getCurrentUser().getUid()))
                .firstName(sharedPreferences.getString("firstName", ""))
                .lastName(sharedPreferences.getString("lastName", ""))
                .email(Objects.requireNonNull(sharedPreferences.getString("email", "")).toLowerCase().trim())
                .phoneNumber(sharedPreferences.getString("phoneNumber", ""))
                .password(sharedPreferences.getString("password", ""))
                .remember(sharedPreferences.getBoolean("remember", false))
                .role(Role.valueOf(sharedPreferences.getString("role", Role.BASIC.toString())))
                .propertySearchSuggestions(new ArrayList<>(sharedPreferences.getStringSet("propertySearchSuggestions", emptySet())))
                .build();
    }

    public static boolean rememberCredentials(Context context) {
        return context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE).getBoolean("remember", false);
    }

    public static Optional<String> getGoogleEmail(Context context) {
        return Optional.ofNullable(context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE).getString("googleEmail", null));
    }

    public static void setGoogleEmail(Context context, String googleEmail) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("googleEmail", googleEmail);
        editor.apply();
    }

    //used by Profile Fragment, do not update remember credentials
    public static void saveUser(User user, Context context) {
        saveUser(user, null, context);
    }

    //used by Sign In Fragment, update remember credentials
    public static void saveUser(User user, String password, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("remember", user.isRemember());
        editor.putString("id", user.getId());
        editor.putString("firstName", user.getFirstName());
        editor.putString("lastName", user.getLastName());
        editor.putString("email", user.getEmail());
        editor.putString("phoneNumber", user.getPhoneNumber());
        editor.putString("role", user.getRole().toString());
        editor.putStringSet("propertySearchSuggestions", new HashSet<>(user.getPropertySearchSuggestions()));
        if (user.isRemember() && password != null) {
            editor.putString("password", password);
        }
        editor.apply();
    }

    public static void updateSuggestions(List<String> propertySearchSuggestions, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("propertySearchSuggestions", new HashSet<>(propertySearchSuggestions));
        editor.apply();
    }
}
