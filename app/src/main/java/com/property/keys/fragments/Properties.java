package com.property.keys.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.AddProperty;
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.adapters.PropertyAdapter;
import com.property.keys.adapters.PropertyHolder;
import com.property.keys.databinding.FragmentPropertiesBinding;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.filters.FirebaseRecyclerAdapter;
import com.property.keys.helpers.PropertySuggestion;
import com.property.keys.helpers.RecyclerItemTouchHelper;
import com.property.keys.utils.PropertyUtils;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

import static java.util.stream.Collectors.toList;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Properties extends Fragment implements FirebaseAuth.AuthStateListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = Properties.class.getSimpleName();

    @NonNull
    protected static final Query propertiesQuery = FirebaseDatabase.getInstance().getReference().child("properties").orderByChild("name").limitToLast(20);
    private FragmentPropertiesBinding binding;

    @Getter
    private PropertyAdapter adapter;
    private Container container;
    private String lastQuery;
    private Deque<SearchSuggestion> suggestions = new LinkedList<>();

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;
    private User user;

    public Properties(NavigationView navigation, MaterialToolbar toolbar) {
        this(null, navigation, toolbar);
    }

    public Properties(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPropertiesBinding.inflate(getLayoutInflater(), container, false);
        this.container = (Container) getActivity();
        binding.propertyList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_properties, true);
        navigation.getCheckedItem().setChecked(false);
        toolbar.setEnabled(false);
        toolbar.setVisibility(View.GONE);

        user = UserUtils.getLocalUser(requireContext());
        suggestions = user.getPropertySearchSuggestions().stream()
                .map(suggestion -> new PropertySuggestion(suggestion, true))
                .distinct()
                .collect(Collectors.toCollection(LinkedList::new));

        binding.propertyList.setLayoutManager(linearLayoutManager);
        binding.propertyList.setItemAnimator(new DefaultItemAnimator());
        binding.propertyList.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        binding.addNewProperty.setOnClickListener(view -> startActivity(new Intent(getContext(), AddProperty.class)));

        binding.propertyList.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    binding.addNewProperty.hide();
                } else {
                    binding.addNewProperty.show();
                }
            }
        });

        binding.searchBar.addOnOffsetChangedListener(this);
        binding.floatingSearchView.attachNavigationDrawerToMenuButton(this.container.getDrawerLayout());

        Utils.initSwipeProperty(binding.propertyList, this);
        setupSearchBar();

        binding.filters.setOnCheckedChangeListener((group, checkedId) -> {
            FirebaseRecyclerAdapter.SearchFilter searchFilter = (FirebaseRecyclerAdapter.SearchFilter) adapter.getFilter();
            switch (checkedId) {
                case R.id.favouriteFilter: {
                    searchFilter.setShowOnlyFavourites(true);
                    break;
                }
                case R.id.allFilter:
                default: {
                    searchFilter.setShowOnlyFavourites(false);
                    break;
                }
            }
            searchFilter.filter(lastQuery);
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

    private void setupSearchBar() {
        binding.floatingSearchView.setShowMoveUpSuggestion(true);

        binding.floatingSearchView.setOnQueryChangeListener((oldQuery, newQuery) -> {
            binding.floatingSearchView.showProgress();
            if (!oldQuery.equals("") && newQuery.equals("")) {
                binding.floatingSearchView.clearSuggestions();
            } else {
                lastQuery = newQuery;
                FirebaseRecyclerAdapter.SearchFilter searchFilter = (FirebaseRecyclerAdapter.SearchFilter) adapter.getFilter();
                searchFilter.setShowOnlyFavourites(binding.favouriteFilter.isChecked());
                searchFilter.filter(newQuery);
            }
            binding.floatingSearchView.hideProgress();
        });

        binding.floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {
                binding.floatingSearchView.showProgress();
                lastQuery = searchSuggestion.getBody();
                FirebaseRecyclerAdapter.SearchFilter searchFilter = (FirebaseRecyclerAdapter.SearchFilter) adapter.getFilter();
                searchFilter.setShowOnlyFavourites(binding.favouriteFilter.isChecked());
                searchFilter.filter(searchSuggestion.getBody());
                binding.floatingSearchView.setSearchFocused(false);
                binding.floatingSearchView.hideProgress();
            }

            @Override
            public void onSearchAction(String query) {
                binding.floatingSearchView.showProgress();
                lastQuery = query;
                updateSuggestions();
                FirebaseRecyclerAdapter.SearchFilter searchFilter = (FirebaseRecyclerAdapter.SearchFilter) adapter.getFilter();
                searchFilter.setShowOnlyFavourites(binding.favouriteFilter.isChecked());
                searchFilter.filter(query);
                binding.floatingSearchView.hideProgress();
            }
        });

        binding.floatingSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                //show suggestions when search bar gains focus (typically history suggestions)
                binding.floatingSearchView.swapSuggestions(new ArrayList<>(suggestions));
            }

            @Override
            public void onFocusCleared() {
                updateSuggestions();

                String title = "";
                if (lastQuery != null && !lastQuery.isEmpty()) {
                    title = lastQuery;
                }

                //set the title of the bar so that when focus is returned a new query begins
                binding.floatingSearchView.setSearchBarTitle(title);

                //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
                //binding.floatingSearchView.setSearchText(searchSuggestion.getBody());
                binding.floatingSearchView.hideProgress();
            }
        });

        /*
         * Here you have access to the left icon and the text of a given suggestion
         * item after as it is bound to the suggestion list. You can utilize this
         * callback to change some properties of the left icon and the text. For example, you
         * can load the left icon images using your favourite image loading library, or change text color.
         *
         *
         * Important:
         * Keep in mind that the suggestion list is a RecyclerView, so views are reused for different
         * items in the list.
         */
        binding.floatingSearchView.setOnBindSuggestionCallback((suggestionView, leftIcon, textView, item, itemPosition) -> {
            PropertySuggestion propertySuggestion = (PropertySuggestion) item;

            String textColor = "#000000";
            String textLight = "#787878";

            if (propertySuggestion.isHistory()) {
                leftIcon.setImageDrawable(ResourcesCompat.getDrawable(Properties.this.getResources(), R.drawable.ic_history_black_24dp, null));

                Util.setIconColor(leftIcon, Color.parseColor(textColor));
                leftIcon.setAlpha(.36f);
            } else {
                leftIcon.setAlpha(0.0f);
                leftIcon.setImageDrawable(null);
            }

            textView.setTextColor(Color.parseColor(textColor));
            String text = propertySuggestion.getBody()
                    .replaceFirst(binding.floatingSearchView.getQuery(), "<font color=\"" + textLight + "\">" + binding.floatingSearchView.getQuery() + "</font>");
            textView.setText(Html.fromHtml(text));
        });
    }

    private void updateSuggestions() {
        boolean updated = false;
        if (lastQuery != null && !lastQuery.isEmpty()) {
            if (suggestions.size() > 3) {
                updated = true;
                suggestions.removeLast();
            }
            PropertySuggestion newSuggestion = new PropertySuggestion(lastQuery, true);
            if (!suggestions.contains(newSuggestion)) {
                updated = true;
                suggestions.addFirst(newSuggestion);
            }
        }
        if (updated) {
            User user = UserUtils.getLocalUser(requireContext());
            user.setPropertySearchSuggestions(Stream.of(user.getPropertySearchSuggestions(), suggestions.stream().map(SearchSuggestion::getBody).collect(toList()))
                    .flatMap(List::stream)
                    .distinct()
                    .collect(toList()));
            UserUtils.updateSearchSuggestions(requireContext(), user);
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
        FirebaseRecyclerOptions<Property> options =
                new FirebaseRecyclerOptions.Builder<Property>()
                        .setQuery(propertiesQuery, Property.class)
                        .setLifecycleOwner(this)
                        .build();


        adapter = new PropertyAdapter(options, this.getActivity(), user);
        // Scroll to bottom on new properties
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                binding.propertyList.smoothScrollToPosition(adapter.getItemCount());
            }
        });
        binding.propertyList.setAdapter(adapter);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof PropertyHolder) {
            new AlertDialog.Builder(viewHolder.itemView.getContext())
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        PropertyUtils.delete(this.getActivity(), adapter.getItem(viewHolder.getAdapterPosition()));
                        Snackbar.make(container.getPlaceSnackBar(), "Property removed.", Snackbar.LENGTH_LONG).show();
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> adapter.notifyItemChanged(viewHolder.getAdapterPosition()))
                    .setCancelable(false)
                    .create().show();
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        binding.floatingSearchView.setTranslationY(verticalOffset);
    }
}