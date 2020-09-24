package com.property.keys.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.R;
import com.property.keys.entities.Action;
import com.property.keys.entities.Property;
import com.property.keys.tasks.TaskExecutor;
import com.property.keys.tasks.properties.KeyGenerateTask;
import com.property.keys.tasks.properties.PropertyCreateTask;
import com.property.keys.tasks.properties.PropertyDeleteTask;
import com.property.keys.tasks.properties.PropertyUpdateTask;

import java.util.function.Consumer;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyUtils {

    private static final String TAG = PropertyUtils.class.getSimpleName();

    private PropertyUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void create(Activity activity, Property property,
                              Consumer<Intent> startActivity,
                              Consumer<Task<Void>> onCreationFailed) {
        new TaskExecutor().executeAsync(new PropertyCreateTask(activity, property, startActivity, onCreationFailed));
    }

    public static void delete(Activity activity, Property property) {
        new TaskExecutor().executeAsync(new PropertyDeleteTask(activity, property, false, false));
    }

    public static void remove(Activity activity, Property property) {
        new TaskExecutor().executeAsync(new PropertyDeleteTask(activity, property, true, false));
    }

    public static void restore(Activity activity, Property property) {
        new TaskExecutor().executeAsync(new PropertyDeleteTask(activity, property, false, true));
    }

    public static void like(Activity activity, Property property, boolean liked) {
        new TaskExecutor().executeAsync(new PropertyUpdateTask(activity, property, null, liked ? Action.LIKED_PROPERTY : Action.DISLIKED_PROPERTY));
    }

    public static void generateKey(Activity activity, Property property) {
        new TaskExecutor().executeAsync(new KeyGenerateTask(activity, property));
    }

//    public static void createMap(Context context, Bundle savedInstanceState, MapView view, Property property) {
//        new TaskExecutor().executeAsync(new PropertyMapCreateTask(context, savedInstanceState, view, property));
//    }

    public static void updateBadge(Activity activity, long itemCount) {
        ChipNavigationBar bottomNavigationMenu = activity.findViewById(R.id.bottom_navigation_menu);
        //TODO add badge to left menu as well
        //NavigationView leftNavigationMenu = activity.findViewById(R.id.navigation);
        if (itemCount > 0) {
            bottomNavigationMenu.showBadge(R.id.bottom_navigation_notification, Long.valueOf(itemCount).intValue());
        } else {
            bottomNavigationMenu.dismissBadge(R.id.bottom_navigation_notification);
        }
    }
}
