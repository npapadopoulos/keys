package com.property.keys.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.Container;
import com.property.keys.entities.Action;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.utils.UserUtils;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class PropertyCreateTask extends AbstractAsyncTask {
    private static final String TAG = PropertyCreateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Context context;
    private final Property property;
    private final Consumer<Intent> startActivity;
    private final Consumer<Task<Void>> onCreationFailed;

    @Override
    public void runInBackground() {
        User user = UserUtils.getLocalUser(activity.getApplicationContext());
        DatabaseReference properties = firebaseDatabase.getReference("properties");
        properties.child(property.getId()).setValue(property)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        try {
                            Intent intent = new Intent();
                            intent.putExtra("userId", user.getId());
                            intent.putExtra("description", user.getFirstName() + " added new property '" + property.getName() + "'.");
                            intent.putExtra("action", Action.ADDED_PROPERTY.name());
                            intent.putExtra("property", property);
                            intent.setAction("com.property.keys.ACTION_PERFORMED");
                            activity.sendBroadcast(intent);

                            Intent next = new Intent(context, Container.class);
                            next.putExtra("selected", "Properties");
                            next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity.accept(next);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to start activity.", e);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        onCreationFailed.accept(task);
                    }
                });
    }
}
