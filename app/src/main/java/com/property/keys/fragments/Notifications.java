package com.property.keys.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.adapters.NotificationAdapter;
import com.property.keys.adapters.NotificationHolder;
import com.property.keys.databinding.FragmentNotificationsBinding;
import com.property.keys.entities.Notification;
import com.property.keys.entities.Role;
import com.property.keys.entities.User;
import com.property.keys.filters.FirebaseRecyclerOptions;
import com.property.keys.helpers.RecyclerItemTouchHelper;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Notifications extends Fragment implements FirebaseAuth.AuthStateListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String TAG = Notifications.class.getSimpleName();

    private final DatabaseReference usersDatabaseReference = FirebaseDatabase.getInstance().getReference("users");

    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;
    private Container container;

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;
    private String userId;

    public Notifications(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(getLayoutInflater(), container, false);
        binding.notificationsList.setHasFixedSize(false);
        bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_notification, true);
        navigation.setCheckedItem(R.id.navigationNotifications);
        navigation.getCheckedItem().setChecked(true);
        this.toolbar.setTitle("Notifications");
        this.container = (Container) getActivity();

        User user = UserUtils.getLocalUser(requireContext());
        userId = user.getId();

        initLayoutManager();
        Utils.initSwipeProperty(binding.notificationsList, this, user.getRole() == Role.ADMIN);
        setReadNotifications();
        addOnClearListener();
        addOnScrollListener();

        return binding.getRoot();
    }

    private void initLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.container);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        binding.notificationsList.setLayoutManager(linearLayoutManager);
    }

    private void addOnScrollListener() {
        binding.notificationsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    binding.deleteNotifications.hide();
                } else if (adapter.getItemCount() > 0) {
                    binding.deleteNotifications.show();
                }
            }
        });
    }

    private void addOnClearListener() {
        binding.deleteNotifications.setOnClickListener(view -> new MaterialAlertDialogBuilder(requireActivity())
                .setMessage("Are you sure you want to remove all notifications?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    UserUtils.deleteNotifications(userId);
                    binding.deleteNotifications.hide();
                    Snackbar.make(this.container.getPlaceSnackBar(), "All notifications deleted", Snackbar.LENGTH_SHORT).show();
                })
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                .setNegativeButton("No", Utils::onClick)
                .setCancelable(false)
                .setOnKeyListener((d, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        d.dismiss();
                        return true;
                    }
                    return false;
                })
                .create().show());
    }

    private void setReadNotifications() {
        UserUtils.setReadNotifications(userId);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof NotificationHolder) {
            new MaterialAlertDialogBuilder(viewHolder.itemView.getContext())
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        UserUtils.deleteNotification(userId, adapter.getItem(viewHolder.getAdapterPosition()).getId());
                        Snackbar.make(container.getPlaceSnackBar(), "Notification deleted.", Snackbar.LENGTH_LONG).show();
                    })
                    .setOnKeyListener((d, keyCode, event) -> {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            d.dismiss();
                            return true;
                        }
                        return false;
                    })
                    .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                    .setNegativeButton("No", (dialogInterface, i) -> adapter.notifyItemChanged(viewHolder.getAdapterPosition()))
                    .setCancelable(false)
                    .create().show();
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
        if (adapter == null) {
            attachRecyclerViewAdapter();
        }
    }

    private void attachRecyclerViewAdapter() {
        final String userId = UserUtils.getLocalUser(requireContext()).getId();

        Query query = usersDatabaseReference.child(userId).child("notifications").orderByChild("date");
        FirebaseRecyclerOptions<Notification> options =
                new FirebaseRecyclerOptions.Builder<Notification>()
                        .setQuery(query, Notification.class)
                        .setLifecycleOwner(this)
                        .build();

        getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        adapter = new NotificationAdapter(options, this.requireActivity());

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