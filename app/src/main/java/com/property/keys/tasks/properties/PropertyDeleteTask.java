package com.property.keys.tasks.properties;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.Action;
import com.property.keys.entities.Property;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class PropertyDeleteTask extends AbstractAsyncTask {
    private static final String TAG = PropertyDeleteTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Property property;
    private final boolean temporary;
    private final boolean restore;

    /**
     * Deletes property from root properties and user's properties paths.
     */
    @Override
    public void runInBackground() {
        if (restore) {
            property.setDeleted(false);
            update(property, false);
        } else if (temporary) {
            property.setDeleted(true);
            update(property, false);
        } else {
            update(property, true);
        }
    }

    private void update(Property property, boolean delete) {
        final Map<String, Object> updates = new HashMap<>();
        firebaseDatabase.getReference("properties").child(property.getId()).setValue(delete ? null : property);
        if (delete) {
            NotificationUtils.create(activity, property, Action.DELETED_PROPERTY);
        }
        if (property.getFavouredBy() != null) {
            property.getFavouredBy().keySet().forEach(userId -> updates.put("/" + userId + "/properties/" + property.getId(), delete ? null : property));
            if (!updates.isEmpty()) {
                firebaseDatabase.getReference("users").updateChildren(updates);
            }
        }
    }
}
