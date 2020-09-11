package com.property.keys.utils;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.property.keys.entities.Action;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.tasks.TaskExecutor;
import com.property.keys.tasks.notifications.NotificationCreateTask;

import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();

    private NotificationUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void create(Activity activity, Property property, Set<String> usersToNotify, Action action) {
        User currentUser = UserUtils.getLocalUser(activity.getApplicationContext());
        String description;
        switch (action) {
            case ADDED_PROPERTY: {
                description = currentUser.getFirstName() + " added new property '" + property.getName() + "'.";
                break;
            }
            case DELETED_PROPERTY: {
                description = currentUser.getFirstName() + " deleted property '" + property.getName() + "'.";
                break;
            }
            case UPDATED_PROPERTY: {
                description = currentUser.getFirstName() + " updated property '" + property.getName() + "'.";
                break;
            }
            case LIKED_PROPERTY: {
                description = currentUser.getFirstName() + " is now following property '" + property.getName() + "'.";
                break;
            }
            case UNLIKED_PROPERTY: {
                description = currentUser.getFirstName() + " stopped following property '" + property.getName() + "'.";
                break;
            }
            case ADDED_KEY: {
                description = currentUser.getFirstName() + " added new key for property '" + property.getName() + "'.";
                break;
            }
            case DELETED_KEY: {
                description = currentUser.getFirstName() + " deleted key from property '" + property.getName() + "'.";
                break;
            }
            case CHECKED_IN: {
                description = currentUser.getFirstName() + " checked in key for property '" + property.getName() + "'.";
                break;
            }
            case CHECKED_OUT: {
                description = currentUser.getFirstName() + " checked out key for property '" + property.getName() + "'.";
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
                        .usersToNotify(usersToNotify)
                        .build());
    }
}