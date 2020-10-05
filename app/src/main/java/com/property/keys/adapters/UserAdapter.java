package com.property.keys.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.property.keys.R;
import com.property.keys.entities.User;
import com.property.keys.filters.FirebaseRecyclerAdapter;
import com.property.keys.filters.FirebaseRecyclerOptions;

import org.jetbrains.annotations.NotNull;

@RequiresApi(api = Build.VERSION_CODES.R)
public class UserAdapter extends FirebaseRecyclerAdapter<User, UserHolder> {

    @NonNull
    private Activity activity;

    public UserAdapter(@NonNull FirebaseRecyclerOptions<User> options, @NotNull Activity activity, String userId) {
        super(options, false, User.class, activity.findViewById(R.id.progressBar), userId);
        this.activity = activity;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserHolder holder, int position, @NonNull User model) {
        holder.bind(activity, model);
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserHolder(activity, LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user, parent, false));
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
    }

    @NonNull
    @Override
    public String getId(User user) {
        return user.getId();
    }
}
