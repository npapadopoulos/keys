package com.property.keys.tasks.users;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.entities.User;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.UserUtils;

import lombok.Builder;

import static java.util.Collections.singletonMap;

@RequiresApi(api = Build.VERSION_CODES.R)
@Builder
public class UserPropertySearchSuggestionsUpdateTask extends AbstractAsyncTask {
    private static final String TAG = UserPropertySearchSuggestionsUpdateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private Context context;
    private User user;

    @Override
    public void runInBackground() {
        UserUtils.saveUser(user, context);
        firebaseDatabase.getReference("users").updateChildren(singletonMap("/" + user.getId() + "/propertySearchSuggestions/", user.getPropertySearchSuggestions()));
    }
}
