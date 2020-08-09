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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.property.keys.R;
import com.property.keys.databinding.FragmentNotificationsBinding;
import com.property.keys.entities.Notification;
import com.property.keys.holders.NotificationHolder;
import com.property.keys.utils.NavigationUtils;

import java.time.format.DateTimeFormatter;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Notifications extends Fragment implements FirebaseAuth.AuthStateListener {
    /**
     * Get the last 50 chat messages.
     */
    @NonNull
    protected static final Query notificationsQuery = FirebaseDatabase.getInstance().getReference().child("notifications").limitToLast(10);
    private static final String TAG = Notifications.class.getSimpleName();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private FragmentNotificationsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(getLayoutInflater(), container, false);
        NavigationUtils.onMenuClick(getActivity().findViewById(R.id.drawer_layout), binding.headerMenuIcon);

        binding.notificationsList.setHasFixedSize(true);
        binding.notificationsList.setLayoutManager(new LinearLayoutManager(getActivity()));


        //TODO need this when checkin/out the keys
//        for (int i = 0; i < 50; i++) {
//            Notification.NotificationBuilder notification = Notification.builder()
//                    .id(UUID.randomUUID().toString())
//                    .date(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
//            if (i % 2 == 0) { //even
//                notification.event("Checked In the key #" + i);
//            } else {
//                notification.event("Checked Out the key #" + i);
//            }
//            notification.userId(UUID.randomUUID().toString());
//
//            notificationsQuery.getRef().push().setValue(notification.build(), (error, reference) -> {
//                if (error != null) {
//                    Log.e(TAG, "Failed to add notification", error.toException());
//                }
//            });
//        }

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
                        .setQuery(notificationsQuery, Notification.class)
                        .setLifecycleOwner(this)
                        .build();

        return new FirebaseRecyclerAdapter<Notification, NotificationHolder>(options) {
            @Override
            public NotificationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new NotificationHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.notification, parent, false));
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