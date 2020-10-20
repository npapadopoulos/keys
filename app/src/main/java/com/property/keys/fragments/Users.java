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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.adapters.UserAdapter;
import com.property.keys.databinding.FragmentUsersBinding;
import com.property.keys.entities.User;
import com.property.keys.filters.FirebaseRecyclerOptions;
import com.property.keys.utils.UserUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Users extends Fragment implements FirebaseAuth.AuthStateListener {

    private final DatabaseReference usersDatabaseReference = FirebaseDatabase.getInstance().getReference("users");

    private FragmentUsersBinding binding;
    private UserAdapter adapter;
    private Container container;

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;
    private String userId;

    public Users(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(getLayoutInflater(), container, false);

        navigation.setCheckedItem(R.id.navigationUsers);
        navigation.getCheckedItem().setChecked(true);
        bottomNavigationMenu.setItemSelected(bottomNavigationMenu.getSelectedItemId(), false);

        this.toolbar.setTitle("Users");
        this.toolbar.setEnabled(true);
        this.toolbar.setVisibility(View.VISIBLE);
        this.container = (Container) getActivity();

        userId = UserUtils.getLocalUser(requireContext()).getId();

        initLayoutManager();

        return binding.getRoot();
    }

    private void initLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.container);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        binding.usersList.setLayoutManager(linearLayoutManager);
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
        Query query = usersDatabaseReference.orderByChild("firstName");
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class, userId)
                        .setLifecycleOwner(this)
                        .build();

        getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        adapter = new UserAdapter(options, this.requireActivity(), userId);

        // Scroll to bottom on new messages
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                binding.usersList.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        binding.usersList.setAdapter(adapter);
    }
}