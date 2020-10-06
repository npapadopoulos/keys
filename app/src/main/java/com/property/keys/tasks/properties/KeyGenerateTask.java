package com.property.keys.tasks.properties;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.Action;
import com.property.keys.entities.Key;
import com.property.keys.entities.Property;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.NotificationUtils;

import java.util.UUID;

import lombok.AllArgsConstructor;
import timber.log.Timber;

import static java.util.Collections.singletonMap;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class KeyGenerateTask extends AbstractAsyncTask {
    private static final String TAG = KeyGenerateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Property property;
    private final String location;
    private final String purpose;

    @Override
    public void runInBackground() {
        //TODO populate all fields
        Key key = Key.builder()
                .id(UUID.randomUUID().toString())
                .propertyId(property.getId())
                .location(location)
                .purpose(purpose)
                .build();

        firebaseDatabase.getReference("keys").child(key.getId()).setValue(key)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        firebaseDatabase.getReference("properties").updateChildren(singletonMap("/" + property.getId() + "/keys/" + key.getId(), key));
                        NotificationUtils.create(activity, property, Action.ADDED_KEY);
                        Timber.tag(TAG).i("Generated new key " + key.getId() + " for property '" + property.getName() + "'.");
                    } else {
                        Timber.tag(TAG).i(task.getException(), "Failed to generated new key " + key.getId() + " for property '" + property.getName() + "'.");
                    }
                });
    }
}
