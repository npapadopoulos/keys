package com.property.keys.tasks.properties;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.entities.Action;
import com.property.keys.entities.Property;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private final boolean all;

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
        } else if (all) {
            deleteAll();
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
        if (property.getKeys() != null && delete) {
            property.getKeys().keySet().forEach(keyId -> updates.put("/" + keyId + "/", null));
            if (!updates.isEmpty()) {
                firebaseDatabase.getReference("keys").updateChildren(updates);
            }
        }
    }

    private void deleteAll() {
        firebaseDatabase.getReference("properties").orderByChild("deleted").equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        snapshot.getChildren().forEach(child -> update(Objects.requireNonNull(child.getValue(Property.class)), true));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
