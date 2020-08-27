package com.property.keys.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.R;
import com.property.keys.entities.Key;
import com.property.keys.entities.Property;
import com.property.keys.tasks.KeyGenerateTask;
import com.property.keys.tasks.KeyUpdateTask;
import com.property.keys.tasks.NotificationCreateTask;
import com.property.keys.tasks.PropertyCreateTask;
import com.property.keys.tasks.PropertyUpdateTask;
import com.property.keys.tasks.TaskExecutor;

import java.util.function.Consumer;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyUtils {

    private static final String TAG = PropertyUtils.class.getSimpleName();

    private PropertyUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void create(Activity activity, Context context, Property property,
                              Consumer<Intent> startActivity,
                              Consumer<Task<Void>> onCreationFailed) {
        new TaskExecutor().executeAsync(new PropertyCreateTask(activity, context, property, startActivity, onCreationFailed));
    }

    public static void update(Activity activity, Property property, boolean favorite) {
        new TaskExecutor().executeAsync(new PropertyUpdateTask(activity, property, favorite));
    }

    public static void update(Activity activity, Key key, boolean favorite) {
        new TaskExecutor().executeAsync(new KeyUpdateTask(activity, key, favorite));
    }

    public static void notify(Activity activity, String description, Property property, String action) {
        new TaskExecutor().executeAsync(new NotificationCreateTask(activity, description, property, action));
    }

    public static void generateKey(Activity activity, Property property) {
        new TaskExecutor().executeAsync(new KeyGenerateTask(activity, property));
    }

    public static void dismissBadge(Activity activity) {
        showBadge(activity, false, 0);
    }

    public static void showBadge(Activity activity, long itemCount) {
        showBadge(activity, true, itemCount);
    }

    private static void showBadge(Activity activity, boolean show, long itemCount) {
        ChipNavigationBar bottomNavigationMenu = activity.findViewById(R.id.bottom_navigation_menu);
        //TODO add badge to left menu as well
        //NavigationView leftNavigationMenu = activity.findViewById(R.id.navigation);
        if (show) {
            bottomNavigationMenu.showBadge(R.id.bottom_navigation_notification, Long.valueOf(itemCount).intValue());
        } else {
            bottomNavigationMenu.dismissBadge(R.id.bottom_navigation_notification);
        }
    }
}
