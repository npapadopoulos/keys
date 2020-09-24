package com.property.keys.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.property.keys.R;
import com.property.keys.entities.Key;
import com.property.keys.filters.FirebaseRecyclerAdapter;

@RequiresApi(api = Build.VERSION_CODES.R)
public class KeyAdapter extends FirebaseRecyclerAdapter<Key, KeyHolder> {
    @NonNull
    private Activity activity;
    private String propertyName;

    public KeyAdapter(@NonNull FirebaseRecyclerOptions<Key> options, Activity activity, String propertyName) {
        super(options, false);
        this.activity = activity;
        this.propertyName = propertyName;
    }

    @Override
    protected void onBindViewHolder(@NonNull KeyHolder holder, int position, @NonNull Key key) {
        holder.bind(activity, key, propertyName);
    }

    @NonNull
    @Override
    public KeyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new KeyHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.key, parent, false));
    }

    @NonNull
    @Override
    public String getId(Key key) {
        return key.getId();
    }
}
