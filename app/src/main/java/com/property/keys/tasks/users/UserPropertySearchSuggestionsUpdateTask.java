package com.property.keys.tasks.users;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.tasks.AbstractAsyncTask;
import com.property.keys.utils.UserUtils;

import java.util.List;

import lombok.Builder;

import static java.util.Collections.singletonMap;

@RequiresApi(api = Build.VERSION_CODES.R)
@Builder
public class UserPropertySearchSuggestionsUpdateTask extends AbstractAsyncTask {
    private static final String TAG = UserPropertySearchSuggestionsUpdateTask.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private Context context;
    private String userId;
    private List<String> propertySearchSuggestions;

    @Override
    public void runInBackground() {
        UserUtils.updateSuggestions(propertySearchSuggestions, context);
        firebaseDatabase.getReference("users").updateChildren(singletonMap("/" + userId + "/propertySearchSuggestions/", propertySearchSuggestions));
    }
}
