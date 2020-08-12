package com.property.keys.tasks;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.utils.UserUtils;

import lombok.AllArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.R)
@AllArgsConstructor
public class PropertyUpdateTask extends AbstractAsyncTask {
    private static final String TAG = PropertyUpdateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Activity activity;
    private final Property property;
    private final boolean favorite;

    @Override
    public void runInBackground() {
        //update real time database
        User user = UserUtils.getUser(activity.getApplicationContext());

        Intent intent = new Intent();
        intent.putExtra("userId", user.getId());
        if (favorite) {
            property.getFavouredBy().put(user.getId(), Boolean.TRUE);
            intent.putExtra("description", user.getFirstName() + " is now following '" + property.getName() + "'.");
        } else {
            property.getFavouredBy().remove(user.getId());
            intent.putExtra("description", user.getFirstName() + " stopped following '" + property.getName() + "'.");
        }
        intent.setAction("com.property.keys.ACTION_PERFORMED");
        activity.sendBroadcast(intent);

        firebaseDatabase.getReference("properties").child(property.getId()).setValue(property);
    }
}
