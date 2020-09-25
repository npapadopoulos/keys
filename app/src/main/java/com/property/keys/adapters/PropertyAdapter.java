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
    private LinearLayout emptyPropertySearchResults;

    public PropertyAdapter(@NonNull FirebaseRecyclerOptions<Property> options, Activity activity, User user) {
        super(options, true);
        this.activity = activity;
        this.user = user;
        emptyPropertySearchResults = activity.findViewById(R.id.empty_property_search_results);
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
            emptyPropertySearchResults.setVisibility(View.VISIBLE);
        } else {
            emptyPropertySearchResults.setVisibility(View.INVISIBLE);
        }
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

    @Override
    public void postFilterUpdate(int count) {
        if (count == 0) {
            emptyPropertySearchResults.setVisibility(View.VISIBLE);
        } else {
            emptyPropertySearchResults.setVisibility(View.INVISIBLE);
        }
    }
}
