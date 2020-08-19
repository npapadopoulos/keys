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
import com.property.keys.filters.FirebaseRecyclerAdapter;
import com.property.keys.utils.UserUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyAdapter extends FirebaseRecyclerAdapter<Property, PropertyHolder> {
    @NonNull
    private Activity activity;
    private boolean onlyFavourites = false;

    public PropertyAdapter(@NonNull FirebaseRecyclerOptions<Property> options, Activity activity) {
        super(options);
        this.activity = activity;
    }

    @Override
    protected void onBindViewHolder(@NonNull PropertyHolder holder, int position, @NonNull Property model) {
        holder.bind(activity, model);
    }

    @Override
    protected boolean filterCondition(Property model, String pattern) {
        return model.getName().toLowerCase().contains(pattern) || model.getAddress().toLowerCase().contains(pattern);
    }

    @Override
    public boolean filterFavourites(Property model) {
        String currentUserId = UserUtils.getLocalUser(activity.getApplicationContext()).getId();
        return model.getFavouredBy().keySet().stream().anyMatch(currentUserId::equalsIgnoreCase);
    }

    @NonNull
    @Override
    public PropertyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PropertyHolder(activity, LayoutInflater.from(parent.getContext())
                .inflate(R.layout.property, parent, false));
    }
}
