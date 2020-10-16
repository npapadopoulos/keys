package com.property.keys.tasks.properties;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.Container;
import com.property.keys.entities.Action;
import com.property.keys.entities.Property;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.NotificationUtils;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class PropertyCreateTask extends AbstractAsyncTask {
    private static final String TAG = PropertyCreateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Property property;
    private final Consumer<Intent> startActivity;
    private final Consumer<Task<Void>> onCreationFailed;

    @Override
    public Void doInBackground(Void... voids) {
        DatabaseReference properties = firebaseDatabase.getReference("properties");
        properties.child(property.getId()).setValue(property)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        try {
                            NotificationUtils.create(activity, property, Action.ADDED_PROPERTY);
                            Intent next = new Intent(activity, Container.class);
                            next.putExtra("selected", "Properties");
                            startActivity.accept(next);
                        } catch (Exception e) {
                            Timber.tag(TAG).e(e, "Failed to start activity.");
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        onCreationFailed.accept(task);
                    }
                });
        return null;
    }
}
