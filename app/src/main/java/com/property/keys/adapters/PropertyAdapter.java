package com.property.keys.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.property.keys.R;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.filters.FirebaseRecyclerAdapter;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyAdapter extends FirebaseRecyclerAdapter<Property, PropertyHolder> {
    @NonNull
    private Activity activity;

    @NonNull
    private User user;

    public PropertyAdapter(@NonNull FirebaseRecyclerOptions<Property> options, Activity activity, User user) {
        super(options, true);
        this.activity = activity;
        this.user = user;
    }

    @Override
    protected void onBindViewHolder(@NonNull PropertyHolder holder, int position, @NonNull Property model) {
        holder.bind(activity, model);
    }

    @Override
    protected boolean filterCondition(Property property, String pattern, boolean showOnlyFavourites) {
        boolean filtered = property.getName().toLowerCase().contains(pattern) || property.getAddress().toLowerCase().contains(pattern);
        if (showOnlyFavourites) {
            return filtered && property.getFavouredBy().get(user.getId()) != null;
        }

        return filtered;
    }

    @NonNull
    @Override
    public PropertyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PropertyHolder(activity, user, LayoutInflater.from(parent.getContext())
                .inflate(R.layout.property, parent, false));
    }

    @NonNull
    @Override
    public String getId(Property property) {
        return property.getId();
    }
}
