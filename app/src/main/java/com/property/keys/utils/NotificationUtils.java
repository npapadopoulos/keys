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

    public static void create(Activity activity, Property property, Action action) {
        create(activity, property.getId(), property.getName(), property.getFavouredBy().keySet(), action);
    }

    public static void create(Activity activity, String propertyId, String propertyName, Set<String> usersToNotify, Action action) {
        User currentUser = UserUtils.getLocalUser(activity.getApplicationContext());
        String description;
        switch (action) {
            case ADDED_PROPERTY: {
                description = currentUser.getFirstName() + " added new property '" + propertyName + "'.";
                break;
            }
            case DELETED_PROPERTY: {
                description = currentUser.getFirstName() + " deleted property '" + propertyName + "'.";
                break;
            }
            case MOVED_TO_TRASH_PROPERTY: {
                description = currentUser.getFirstName() + " moved property '" + propertyName + "' to trash.";
                break;
            }
            case RESTORED_FROM_TRASH_PROPERTY: {
                description = currentUser.getFirstName() + " restored property '" + propertyName + "' from trash.";
                break;
            }
            case UPDATED_PROPERTY: {
                description = currentUser.getFirstName() + " updated property '" + propertyName + "'.";
                break;
            }
            case LIKED_PROPERTY: {
                description = currentUser.getFirstName() + " is now following property '" + propertyName + "'.";
                break;
            }
            case DISLIKED_PROPERTY: {
                description = currentUser.getFirstName() + " stopped following property '" + propertyName + "'.";
                break;
            }
            case ADDED_KEY: {
                description = currentUser.getFirstName() + " added new key for property '" + propertyName + "'.";
                break;
            }
            case DELETED_KEY: {
                description = currentUser.getFirstName() + " deleted key from property '" + propertyName + "'.";
                break;
            }
            case CHECKED_IN: {
                description = currentUser.getFirstName() + " checked in key for property '" + propertyName + "'.";
                break;
            }
            case CHECKED_OUT: {
                description = currentUser.getFirstName() + " checked out key for property '" + propertyName + "'.";
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
                        .propertyId(propertyId)
                        .usersToNotify(usersToNotify)
                        .action(action)
                        .build());
    }
}