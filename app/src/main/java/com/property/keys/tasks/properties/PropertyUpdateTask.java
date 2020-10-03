package com.property.keys.tasks.properties;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.Action;
import com.property.keys.entities.Property;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.NotificationUtils;

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
    private final String keyId;
    private final Consumer<Task<Void>> onUpdateFailed;
    private final Action action;

    /**
     * Updates property details.
     * <p>
     * Note, processes liked/unlinked actions as well.
     */
    @Override
    public void runInBackground() {
        final Map<String, Object> updates = new HashMap<>();
        if (action == Action.DELETED_KEY && keyId != null) {
            property.getKeys().remove(keyId);
            updates.put("properties/" + property.getId() + "/keys/" + keyId, null);
            updates.put("keys/" + keyId, null);
        }
        if (!updates.isEmpty()) {
            firebaseDatabase.getReference("/").updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            NotificationUtils.create(activity, property, action);
                            if (!updates.isEmpty()) {
                                firebaseDatabase.getReference("/").updateChildren(updates);
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
}
