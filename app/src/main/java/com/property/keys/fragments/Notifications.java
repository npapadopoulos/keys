package com.property.keys.fragments;

import android.app.AlertDialog;
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

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.adapters.NotificationAdapter;
import com.property.keys.adapters.NotificationHolder;
import com.property.keys.databinding.FragmentNotificationsBinding;
import com.property.keys.entities.Notification;
import com.property.keys.entities.UnreadNotification;
import com.property.keys.helpers.RecyclerItemTouchHelper;
import com.property.keys.utils.PropertyUtils;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Notifications extends Fragment implements FirebaseAuth.AuthStateListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String TAG = Notifications.class.getSimpleName();

    private final Query notificationsQuery = FirebaseDatabase.getInstance().getReference().child("notifications")
            .orderByChild("date").limitToLast(10);

    private final DatabaseReference unreadNotifications = FirebaseDatabase.getInstance().getReference("unread_notifications");

    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;
    private Container container;

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;

    public Notifications(NavigationView navigation, MaterialToolbar toolbar) {
        this(null, navigation, toolbar);
    }

    public Notifications(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(getLayoutInflater(), container, false);
        binding.notificationsList.setHasFixedSize(true);

        this.container = (Container) getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.container);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        binding.notificationsList.setLayoutManager(linearLayoutManager);

        bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_notification, true);
        resetBadge();
        toolbar.setTitle("Notifications");

        final String userId = UserUtils.getLocalUser(getContext()).getId();
        unreadNotifications.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UnreadNotification unreadNotification = snapshot.getValue(UnreadNotification.class);
                if (unreadNotification != null && unreadNotification.getNotificationIds() != null) {
                    Map<String, Boolean> readAllNotifications = unreadNotification.getNotificationIds().entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> FALSE));
                    unreadNotification.setNotificationIds(readAllNotifications);
                    unreadNotifications.child(userId).setValue(unreadNotification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Utils.initSwipeProperty(binding.notificationsList, this);

        return binding.getRoot();
    }

    private void resetBadge() {
        PropertyUtils.dismissBadge(getActivity());
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof NotificationHolder) {
            new AlertDialog.Builder(viewHolder.itemView.getContext())
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        // remove the item from recycler view
                        adapter.removeItem(viewHolder.getAdapterPosition());
                        Notification notification = adapter.getItem(viewHolder.getAdapterPosition());

                        // showing snack bar with Undo option
                        Snackbar.make(container.getContentLayout(), "Notification removed!", Snackbar.LENGTH_LONG).show();
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> adapter.notifyItemChanged(viewHolder.getAdapterPosition())).create().show();
        }
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
        FirebaseRecyclerOptions<Notification> options =
                new FirebaseRecyclerOptions.Builder<Notification>()
                        .setQuery(notificationsQuery, Notification.class)
                        .setLifecycleOwner(this)
                        .build();

        adapter = new NotificationAdapter(options, this.getActivity());

        // Scroll to bottom on new messages
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                binding.notificationsList.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        binding.notificationsList.setAdapter(adapter);
    }
}