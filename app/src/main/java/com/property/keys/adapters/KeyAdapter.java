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

    public KeyAdapter(@NonNull FirebaseRecyclerOptions<Key> options, Activity activity) {
        super(options);
        this.activity = activity;
    }

    @Override
    protected void onBindViewHolder(@NonNull KeyHolder holder, int position, @NonNull Key model) {
        holder.bind(activity, model);
    }

    @NonNull
    @Override
    public KeyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new KeyHolder(activity, LayoutInflater.from(parent.getContext())
                .inflate(R.layout.key, parent, false));
    }
}
