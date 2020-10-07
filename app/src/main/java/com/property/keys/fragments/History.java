package com.property.keys.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.adapters.HistoryAdapter;
import com.property.keys.databinding.FragmentHistoryBinding;
import com.property.keys.entities.HistoryDetails;
import com.property.keys.entities.User;
import com.property.keys.filters.FirebaseRecyclerOptions;
import com.property.keys.utils.UserUtils;

import lombok.Getter;

@RequiresApi(api = Build.VERSION_CODES.R)
public class History extends Fragment implements FirebaseAuth.AuthStateListener {
    private static final String TAG = History.class.getSimpleName();

    @NonNull
    protected final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private User user;

    private FragmentHistoryBinding binding;
    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;

    @Getter
    private HistoryAdapter adapter;
    private Container container;

    public History(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);
        binding.historyDetailsList.setHasFixedSize(false);
        navigation.setCheckedItem(R.id.navigationHistory);
        navigation.getCheckedItem().setChecked(true);
        this.toolbar.setTitle("History");
        this.container = (Container) getActivity();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        bottomNavigationMenu.setItemSelected(bottomNavigationMenu.getSelectedItemId(), false);
        this.toolbar.setEnabled(true);
        this.toolbar.setVisibility(View.VISIBLE);

        binding.historyDetailsList.setLayoutManager(linearLayoutManager);
        binding.historyDetailsList.setItemAnimator(new DefaultItemAnimator());
        binding.historyDetailsList.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));


        this.user = UserUtils.getLocalUser(requireContext());

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null && adapter == null) {
            attachRecyclerViewAdapter();
        }
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (adapter == null) {
            attachRecyclerViewAdapter();
        }
    }

    private void attachRecyclerViewAdapter() {
        Query query = firebaseDatabase.getReference("users").child(user.getId()).child("history").orderByChild("id");
        FirebaseRecyclerOptions<HistoryDetails> options =
                new FirebaseRecyclerOptions.Builder<HistoryDetails>()
                        .setQuery(query, HistoryDetails.class)
                        .setLifecycleOwner(this)
                        .build();

        getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        if (adapter == null) {
            adapter = new HistoryAdapter(options, requireActivity());
            // Scroll to bottom on new properties
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    binding.historyDetailsList.smoothScrollToPosition(adapter.getItemCount());
                }
            });
            binding.historyDetailsList.setAdapter(adapter);
        }
    }
}