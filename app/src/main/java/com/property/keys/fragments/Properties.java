package com.property.keys.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.property.keys.AddProperty;
import com.property.keys.R;
import com.property.keys.databinding.FragmentPropertiesBinding;
import com.property.keys.entities.Property;
import com.property.keys.holders.PropertyHolder;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Properties extends Fragment implements FirebaseAuth.AuthStateListener {

    private static final String TAG = Properties.class.getSimpleName();

    /**
     * Get the last 50 chat messages.
     */
    @NonNull
    protected static final Query propertiesQuery = FirebaseDatabase.getInstance().getReference().child("properties").limitToLast(10);
    private FragmentPropertiesBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPropertiesBinding.inflate(getLayoutInflater(), container, false);
        binding.propertyList.setHasFixedSize(true);
        binding.propertyList.setLayoutManager(new LinearLayoutManager(getActivity()));

        binding.addNewProperty.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), AddProperty.class));
        });

        //TODO
        // Enabling Offline Capabilities on Android
        /**
         * Firebase applications work even if your app temporarily loses its network connection.
         * In addition, Firebase provides tools for persisting data locally, managing presence, and handling latency.
         */
        // https://firebase.google.com/docs/database/android/offline-capabilities

        //TODO
        // Work with Lists of Data on Android
        // Filtering, Sorting, Ordering
        // https://firebase.google.com/docs/database/android/lists-of-data


        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            attachRecyclerViewAdapter();
        }
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        attachRecyclerViewAdapter();
    }

    private void attachRecyclerViewAdapter() {
        final RecyclerView.Adapter adapter = newAdapter();

        // Scroll to bottom on new messages
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                binding.propertyList.smoothScrollToPosition(adapter.getItemCount());
            }
        });
        binding.propertyList.setAdapter(adapter);
    }

    @NonNull
    protected RecyclerView.Adapter newAdapter() {
        FirebaseRecyclerOptions<Property> options =
                new FirebaseRecyclerOptions.Builder<Property>()
                        .setQuery(propertiesQuery, Property.class)
                        .setLifecycleOwner(this)
                        .build();

        return new FirebaseRecyclerAdapter<Property, PropertyHolder>(options) {
            @Override
            public PropertyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new PropertyHolder(getActivity(), LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.property, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull PropertyHolder holder, int position, @NonNull Property model) {
                holder.bind(getActivity(), model);
            }

            @Override
            public void onDataChanged() {
                // If there are no more notifications do nothing.
            }
        };
    }
}