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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.adapters.PropertyAdapter;
import com.property.keys.adapters.PropertyHolder;
import com.property.keys.databinding.FragmentTrashBinding;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.helpers.RecyclerItemTouchHelper;
import com.property.keys.utils.PropertyUtils;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import lombok.Getter;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Trash extends Fragment implements FirebaseAuth.AuthStateListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, AppBarLayout.OnOffsetChangedListener {

    @NonNull
    protected static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    protected static final Query propertiesQuery = firebaseDatabase.getReference("properties").orderByChild("deleted").equalTo(true);
    private static final String TAG = Trash.class.getSimpleName();
    private FragmentTrashBinding binding;

    @Getter
    private PropertyAdapter adapter;
    private Container container;

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;
    private User user;

    public Trash(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTrashBinding.inflate(getLayoutInflater(), container, false);
        binding.deletedPropertyList.setHasFixedSize(false);
        navigation.setCheckedItem(R.id.trash);
        navigation.getCheckedItem().setChecked(true);
        this.toolbar.setTitle("Trash");
        this.container = (Container) getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        bottomNavigationMenu.setItemSelected(bottomNavigationMenu.getSelectedItemId(), false);
        navigation.getCheckedItem().setChecked(false);
        toolbar.setEnabled(true);
        toolbar.setVisibility(View.VISIBLE);

        user = UserUtils.getLocalUser(requireContext());

        binding.deletedPropertyList.setLayoutManager(linearLayoutManager);
        binding.deletedPropertyList.setItemAnimator(new DefaultItemAnimator());
        binding.deletedPropertyList.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        Utils.initSwipeProperty(binding.deletedPropertyList, this, true);

        addOnClearListener();
        addOnScrollListener();

        return binding.getRoot();
    }

    private void addOnClearListener() {
        binding.deleteProperties.setOnClickListener(view -> new MaterialAlertDialogBuilder(requireActivity())
                .setMessage("Are you sure you want to delete all properties?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    PropertyUtils.deleteAll(getActivity());
                    binding.deleteProperties.hide();
                    Snackbar.make(this.container.getPlaceSnackBar(), "All properties deleted", Snackbar.LENGTH_SHORT).show();
                })
                .setOnKeyListener((d, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        d.dismiss();
                        return true;
                    }
                    return false;
                })
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                .setNegativeButton("No", Utils::onClick)
                .setCancelable(false)
                .create().show());
    }

    private void addOnScrollListener() {
        binding.deletedPropertyList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    binding.deleteProperties.hide();
                } else if (adapter.getItemCount() > 0) {
                    binding.deleteProperties.show();
                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            attachRecyclerViewAdapter(propertiesQuery, true);
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
            attachRecyclerViewAdapter(propertiesQuery, true);
        }
    }

    private void attachRecyclerViewAdapter(Query query, boolean filter) {
        FirebaseRecyclerOptions<Property> options =
                new FirebaseRecyclerOptions.Builder<Property>()
                        .setQuery(query, Property.class)
                        .setLifecycleOwner(this)
                        .build();

        if (adapter == null) {
            adapter = new PropertyAdapter(options, this.requireActivity(), binding.emptyTrash, true);
            // Scroll to bottom on new properties
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    binding.deletedPropertyList.smoothScrollToPosition(adapter.getItemCount());
                }
            });
            binding.deletedPropertyList.setAdapter(adapter);
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof PropertyHolder) {
            if (direction == ItemTouchHelper.LEFT) {
                new MaterialAlertDialogBuilder(viewHolder.itemView.getContext())
                        .setMessage("Property will be deleted permanently. Are you sure?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            final Property property = adapter.getItem(viewHolder.getAdapterPosition());
                            PropertyUtils.delete(getActivity(), property);
                            Snackbar.make(container.getPlaceSnackBar(), "Property deleted.", Snackbar.LENGTH_LONG).show();
                        })
                        .setOnKeyListener((d, keyCode, event) -> {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                d.dismiss();
                                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                                return true;
                            }
                            return false;
                        })
                        .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                        .setNegativeButton("No", (dialogInterface, i) -> adapter.notifyItemChanged(viewHolder.getAdapterPosition()))
                        .setCancelable(false)
                        .create().show();
            } else if (direction == ItemTouchHelper.RIGHT) {
                new MaterialAlertDialogBuilder(viewHolder.itemView.getContext())
                        .setMessage("Property will be restored. Are you sure?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            final Property property = adapter.getItem(viewHolder.getAdapterPosition());
                            PropertyUtils.restore(getActivity(), property);
                            Snackbar.make(container.getPlaceSnackBar(), "Property restored.", Snackbar.LENGTH_LONG).show();
                        })
                        .setOnKeyListener((d, keyCode, event) -> {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                d.dismiss();
                                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
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
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

    }
}