package com.property.keys.utils;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.property.keys.entities.Action;
import com.property.keys.entities.Key;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.tasks.TaskExecutor;
import com.property.keys.tasks.notifications.NotificationCreateTask;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();

    private NotificationUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void create(Activity activity, Property property, Action action) {
        create(activity, property, null, action);
    }

    public static void create(Activity activity, Property property, Key key, Action action) {
        User user = UserUtils.getLocalUser(activity.getApplicationContext());
        String description;
        switch (action) {
            case ADDED_PROPERTY: {
                description = "added new property '" + property.getName() + "'.";
                break;
            }
            case DELETED_PROPERTY: {
                description = "deleted property '" + property.getName() + "'.";
                break;
            }
            case MOVED_TO_TRASH_PROPERTY: {
                description = "moved property '" + property.getName() + "' to trash.";
                break;
            }
            case RESTORED_FROM_TRASH_PROPERTY: {
                description = "restored property '" + property.getName() + "' from trash.";
                break;
            }
            case UPDATED_PROPERTY: {
                description = "updated property '" + property.getName() + "'.";
                break;
            }
            case ADDED_KEY: {
                description = "added new key for property '" + property.getName() + "'.";
                break;
            }
            case DELETED_KEY: {
                description = "deleted key from property '" + property.getName() + "'.";
                break;
            }
            case CHECKED_IN: {
                description = "checked in " + key.getPurpose() + " key of property '" + property.getName() + "' for " + key.getCheckInReason() + (!TextUtils.isEmpty(key.getEstimatedCheckOutDate()) ? " and will return back to " + key.getLocation() + " on " + key.getEstimatedCheckOutDate() : "") + ".";
                break;
            }
            case CHECKED_OUT: {
                description = "checked out " + key.getPurpose() + " key of property '" + property.getName() + "'.";
                break;
            }
            default:
                //shall never happen
                throw new IllegalStateException("Unexpected value: " + action);
        }
        new TaskExecutor().executeAsync(
                NotificationCreateTask.builder()
                        .activity(activity)
                        .description(description)
                        .user(user)
                        .property(property)
                        .key(key)
                        .action(action)
                        .build());
    }
}