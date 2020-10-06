package com.property.keys.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.property.keys.R;
import com.property.keys.entities.Property;
import com.property.keys.filters.FirebaseRecyclerAdapter;
import com.property.keys.filters.FirebaseRecyclerOptions;

import java.util.List;


@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyAdapter extends FirebaseRecyclerAdapter<Property, PropertyHolder> {
    @NonNull
    private Activity activity;

    @NonNull
    private final boolean inTrash;
    private final boolean isAdmin;
    private LinearLayout background;
    private FloatingActionButton deleteProperties;

    public PropertyAdapter(@NonNull FirebaseRecyclerOptions<Property> options, Activity activity,
                           LinearLayout background,
                           boolean inTrash, boolean isAdmin) {
        super(options, true, Property.class, activity.findViewById(R.id.progressBar));
        this.activity = activity;
        this.background = background;
        this.inTrash = inTrash;
        deleteProperties = activity.findViewById(R.id.deleteProperties);
        this.isAdmin = isAdmin;
    }

    @Override
    protected void onBindViewHolder(@NonNull PropertyHolder holder, int position, @NonNull Property model) {
        holder.bind(activity, model);
    }

    @Override
    protected boolean filterCondition(Property property, String pattern) {
        return property.getName().toLowerCase().contains(pattern)
                || property.getAddress().toLowerCase().contains(pattern);
    }

    @Override
    protected boolean filterCondition(Property property, List<String> applyExtraFilters) {
        return applyExtraFilters.isEmpty() || applyExtraFilters.contains(property.getType());
    }

    @Override
    public void onDataChanged() {
        if (getItemCount() == 0) {
            background.setVisibility(View.VISIBLE);
            if (deleteProperties != null) {
                deleteProperties.setVisibility(View.GONE);
            }
        } else {
            background.setVisibility(View.INVISIBLE);
            if (deleteProperties != null) {
                if (isAdmin) {
                    deleteProperties.setVisibility(View.VISIBLE);
                }
            }
        }
        super.onDataChanged();
    }

    @NonNull
    @Override
    public PropertyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PropertyHolder(activity, LayoutInflater.from(parent.getContext())
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
