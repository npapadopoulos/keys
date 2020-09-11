package com.property.keys.tasks.properties;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.Action;
import com.property.keys.entities.Key;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.QRCodeUtils;
import com.property.keys.utils.UserUtils;

import java.util.UUID;

import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class KeyGenerateTask extends AbstractAsyncTask {
    private static final String TAG = KeyGenerateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Property property;

    @Override
    public void runInBackground() {
        User user = UserUtils.getLocalUser(activity.getApplicationContext());

        Key key = Key.builder()
                .id(UUID.randomUUID().toString())
                .build();

        DatabaseReference keys = firebaseDatabase.getReference("keys");
        keys.child(key.getId()).setValue(key)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Generated new key " + key.getId() + " for property '" + property.getName() + "'.");
                        QRCodeUtils.generateCode(key);
                        //TODO add information when finish to pop message
                        //Snackbar.make(binding.main, "Account update for " + user.getId() + " failed. Try again later.", Snackbar.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("userId", user.getId());
                        intent.putExtra("description", user.getFirstName() + " added new key for property '" + property.getName() + "'.");
                        intent.putExtra("action", Action.ADDED_KEY.name());
                        intent.putExtra("property", property);
                        intent.setAction("com.property.keys.ACTION_PERFORMED");
                        activity.sendBroadcast(intent);
                    } else {
                        Log.i(TAG, "Failed to generated new key " + key.getId() + " for property '" + property.getName() + "'.", task.getException());
                    }
                });
    }
}
