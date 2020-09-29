package com.property.keys.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
    private final boolean inTrash;
    private LinearLayout background;

    public PropertyAdapter(@NonNull FirebaseRecyclerOptions<Property> options, Activity activity, User user,
                           LinearLayout background,
                           boolean inTrash) {
        super(options, true);
        this.activity = activity;
        this.user = user;
        this.background = background;
        this.inTrash = inTrash;
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

    @Override
    public void onDataChanged() {
        if (getItemCount() == 0) {
            background.setVisibility(View.VISIBLE);
        } else {
            background.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    @Override
    public PropertyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PropertyHolder(activity, user, LayoutInflater.from(parent.getContext())
                .inflate(R.layout.property, parent, false), inTrash);
    }

    @NonNull
    @Override
    public String getId(Property property) {
        return property.getId();
    }

    @Override
    public void postFilterUpdate(int count) {
        if (count == 0) {
            background.setVisibility(View.VISIBLE);
        } else {
            background.setVisibility(View.INVISIBLE);
        }
    }
}
