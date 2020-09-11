package com.property.keys.tasks.properties;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.Action;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.NotificationUtils;
import com.property.keys.utils.UserUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class PropertyUpdateTask extends AbstractAsyncTask {
    private static final String TAG = PropertyUpdateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Property property;
    private final Consumer<Task<Void>> onUpdateFailed;
    private final Action action;

    /**
     * Updates property details.
     * <p>
     * Note, processes liked/unlinked actions as well.
     */
    @Override
    public void runInBackground() {
        DatabaseReference reference = firebaseDatabase.getReference("properties");

        final Map<String, Object> updates = new HashMap<>();
        User localUser = UserUtils.getLocalUser(activity);
        if (action == Action.UNLIKED_PROPERTY) {
            property.getFavouredBy().remove(localUser.getId());
            updates.put("/" + localUser.getId() + "/properties/" + property.getId(), null);
        } else {
            property.getFavouredBy().put(localUser.getId(), true);
        }
        reference.child(property.getId()).setValue(property)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        NotificationUtils.create(activity, property, property.getFavouredBy().keySet(), action);
                        if (property.getFavouredBy() != null) {
                            property.getFavouredBy().keySet().forEach(userId -> updates.put("/" + userId + "/properties/" + property.getId(), property));
                            if (!updates.isEmpty()) {
                                firebaseDatabase.getReference("users").updateChildren(updates);
                            }
                        }
                    } else {
                        if (onUpdateFailed != null) {
                            // If property update fails, display a message to the user.
                            onUpdateFailed.accept(task);
                        }
                    }
                });
    }
}
