package com.property.keys.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;

import com.property.keys.R;
import com.property.keys.entities.Key;
import com.property.keys.entities.User;
import com.property.keys.filters.FirebaseRecyclerAdapter;
import com.property.keys.filters.FirebaseRecyclerOptions;

@RequiresApi(api = Build.VERSION_CODES.R)
public class KeyAdapter extends FirebaseRecyclerAdapter<Key, KeyHolder> {

    @NonNull
    private final FragmentManager supportFragmentManager;
    @NonNull
    private Activity activity;

    private String propertyName;
    private User user;

    public KeyAdapter(@NonNull FirebaseRecyclerOptions<Key> options, Activity activity, FragmentManager supportFragmentManager, String propertyName, User user) {
        super(options, false, Key.class, activity.findViewById(R.id.progressBar));
        this.activity = activity;
        this.supportFragmentManager = supportFragmentManager;
        this.propertyName = propertyName;
        this.user = user;
    }

    @Override
    protected void onBindViewHolder(@NonNull KeyHolder holder, int position, @NonNull Key key) {
        holder.bind(activity, key, propertyName);
    }

    @NonNull
    @Override
    public KeyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new KeyHolder(activity, supportFragmentManager, LayoutInflater.from(parent.getContext()).inflate(R.layout.key, parent, false), user);
    }

    @NonNull
    @Override
    public String getId(Key key) {
        return key.getId();
    }
}
