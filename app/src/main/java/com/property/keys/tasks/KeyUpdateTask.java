package com.property.keys.tasks;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.Action;
import com.property.keys.entities.Key;
import com.property.keys.entities.User;
import com.property.keys.utils.UserUtils;

import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class KeyUpdateTask extends AbstractAsyncTask {
    private static final String TAG = KeyUpdateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Key key;
    private final boolean favorite;

    @Override
    public void runInBackground() {
        //update real time database
        User user = UserUtils.getLocalUser(activity.getApplicationContext());

        Intent intent = new Intent();
        if (favorite) {
            intent.putExtra("action", Action.ADDED_FAVOURED.name());
            key.getFavouredBy().put(user.getId(), Boolean.TRUE);
            intent.putExtra("description", user.getFirstName() + " is now following '" + key.getId() + "'.");
        } else {
            intent.putExtra("action", Action.REMOVED_FAVOURED.name());
            key.getFavouredBy().remove(user.getId());
            intent.putExtra("description", user.getFirstName() + " stopped following '" + key.getId() + "'.");
        }
        intent.putExtra("key", key);
        intent.setAction("com.property.keys.ACTION_PERFORMED");
        activity.sendBroadcast(intent);

        firebaseDatabase.getReference("keys").child(key.getId()).setValue(key);
    }
}
