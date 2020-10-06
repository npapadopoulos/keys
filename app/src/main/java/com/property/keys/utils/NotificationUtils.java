package com.property.keys.utils;

import android.app.Activity;
import android.os.Build;

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
                description = user.getFirstName() + " added new property '" + property.getName() + "'.";
                break;
            }
            case DELETED_PROPERTY: {
                description = user.getFirstName() + " deleted property '" + property.getName() + "'.";
                break;
            }
            case MOVED_TO_TRASH_PROPERTY: {
                description = user.getFirstName() + " moved property '" + property.getName() + "' to trash.";
                break;
            }
            case RESTORED_FROM_TRASH_PROPERTY: {
                description = user.getFirstName() + " restored property '" + property.getName() + "' from trash.";
                break;
            }
            case UPDATED_PROPERTY: {
                description = user.getFirstName() + " updated property '" + property.getName() + "'.";
                break;
            }
            case ADDED_KEY: {
                description = user.getFirstName() + " added new key for property '" + property.getName() + "'.";
                break;
            }
            case DELETED_KEY: {
                description = user.getFirstName() + " deleted key from property '" + property.getName() + "'.";
                break;
            }
            case CHECKED_IN: {
                description = user.getFirstName() + " checked in key for property '" + property.getName() + "'.";
                break;
            }
            case CHECKED_OUT: {
                description = user.getFirstName() + " checked out key for property '" + property.getName() + "'.";
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