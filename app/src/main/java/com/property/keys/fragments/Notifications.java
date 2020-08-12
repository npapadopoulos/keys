package com.property.keys.fragments;

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
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.databinding.FragmentNotificationsBinding;
import com.property.keys.entities.Notification;
import com.property.keys.holders.NotificationHolder;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Notifications extends Fragment implements FirebaseAuth.AuthStateListener {

    private static final String TAG = Notifications.class.getSimpleName();

    private FragmentNotificationsBinding binding;
    private Container containerActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(getLayoutInflater(), container, false);
        binding.notificationsList.setHasFixedSize(true);

        containerActivity = (Container) getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(containerActivity);
        linearLayoutManager.setStackFromEnd(false);
        binding.notificationsList.setLayoutManager(linearLayoutManager);

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
                binding.notificationsList.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        binding.notificationsList.setAdapter(adapter);
    }

    @NonNull
    protected RecyclerView.Adapter newAdapter() {
        FirebaseRecyclerOptions<Notification> options =
                new FirebaseRecyclerOptions.Builder<Notification>()
                        .setQuery(containerActivity.getNotificationQuery(), Notification.class)
                        .setLifecycleOwner(this)
                        .build();

        return new FirebaseRecyclerAdapter<Notification, NotificationHolder>(options) {
            @Override
            public NotificationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new NotificationHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.notification, parent, false), getActivity());
            }

            @Override
            protected void onBindViewHolder(@NonNull NotificationHolder holder, int position, @NonNull Notification model) {
                holder.bind(getActivity(), model);
            }

            @Override
            public void onDataChanged() {
                // If there are no more notifications do nothing.
            }
        };
    }
}