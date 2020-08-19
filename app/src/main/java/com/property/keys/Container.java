package com.property.keys;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.adapters.PropertyAdapter;
import com.property.keys.databinding.ActivityContainerBinding;
import com.property.keys.entities.Property;
import com.property.keys.entities.UnreadNotification;
import com.property.keys.entities.User;
import com.property.keys.filters.FirebaseRecyclerAdapter;
import com.property.keys.fragments.Dashboard;
import com.property.keys.fragments.History;
import com.property.keys.fragments.Notifications;
import com.property.keys.fragments.Profile;
import com.property.keys.fragments.Properties;
import com.property.keys.fragments.Scanner;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.NavigationUtils;
import com.property.keys.utils.PropertyUtils;
import com.property.keys.utils.UserUtils;

import org.jetbrains.annotations.NotNull;

import lombok.NoArgsConstructor;
import lombok.Setter;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Container extends AppCompatActivity {
    private static final String TAG = Container.class.getSimpleName();

    private final int REQUEST_CODE_SIGN_IN = 100;

    private final static DatabaseReference unreadNotificationsQuery = FirebaseDatabase.getInstance()
            .getReference();

    private ActivityContainerBinding binding;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private Fragment fragment;

    private OnImageChangedBroadcastReceiver onImageChangedBroadcastReceiver;
    private OnActionBroadcastReceiver onActionBroadcastReceiver;
    private IntentFilter actionFilter;
    private IntentFilter imageChangedFilter;
    private Menu menu;

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START) || binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            UserUtils.signOut(getApplicationContext());
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(getAuthStateListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(onImageChangedBroadcastReceiver, imageChangedFilter);
        registerReceiver(onActionBroadcastReceiver, actionFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onImageChangedBroadcastReceiver);
        unregisterReceiver(onActionBroadcastReceiver);
        firebaseAuth.removeAuthStateListener(getAuthStateListener());
    }

    @NotNull
    private FirebaseAuth.AuthStateListener getAuthStateListener() {
        return firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() == null) {
                startActivityForResult(new Intent(getApplicationContext(), SignIn.class), REQUEST_CODE_SIGN_IN);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        binding = ActivityContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View view = binding.navigation.getHeaderView(0);
        ImageView navigationProfileImage = view.findViewById(R.id.navigationProfileImage);
        TextView firstNameLabel = view.findViewById(R.id.firstName);
        TextView lastNameLabel = view.findViewById(R.id.lastName);

        User user = UserUtils.getLocalUser(getApplicationContext());
        firstNameLabel.setText(user.getFirstName());
        lastNameLabel.setText(user.getLastName());

        setSupportActionBar(binding.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationUtils.initNavigation(binding.navigation, binding.drawerLayout, binding.content);
        ImageUtils.syncAndloadImages(this, user.getId(), navigationProfileImage);

        onImageChangedBroadcastReceiver = new OnImageChangedBroadcastReceiver();
        onImageChangedBroadcastReceiver.setUserId(user.getId());
        onImageChangedBroadcastReceiver.setImageView(navigationProfileImage);

        onActionBroadcastReceiver = new OnActionBroadcastReceiver();
        onActionBroadcastReceiver.setActivity(this);

        imageChangedFilter = new IntentFilter();
        imageChangedFilter.addAction(getPackageName() + ".PROFILE_IMAGE_UPDATED");

        actionFilter = new IntentFilter();
        actionFilter.addAction(getPackageName() + ".ACTION_PERFORMED");

        //TODO when back is pressed. eg. from Properties Details

        bottomMenu();
        leftMenu();
        setOnUnreadNotificationListener();

        String selectedMenu = getIntent().getStringExtra("selected");
        if (selectedMenu != null && selectedMenu.equalsIgnoreCase("Properties")) {
            binding.bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_properties, true);
        } else {
            binding.bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_dashboard, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        this.menu = menu;

        getMenuInflater().inflate(R.menu.top_bar_menu, menu);
        MenuItem searchProperties = menu.findItem(R.id.searchProperties);
        MenuItem filterFavourites = menu.findItem(R.id.filterFavourites);
        SwitchMaterial filterView = (SwitchMaterial) filterFavourites.getActionView();
        SearchView searchView = (SearchView) searchProperties.getActionView();
        searchView.setIconified(false);
        searchView.onActionViewExpanded();

        EditText txtSearch = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        txtSearch.setHintTextColor(getResources().getColor(R.color.colorBlack));
        txtSearch.setTextColor(getResources().getColor(R.color.colorBlack));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                PropertyAdapter adapter = ((Properties) fragment).getAdapter();
                FirebaseRecyclerAdapter.SearchFilter searchFilter = (FirebaseRecyclerAdapter.SearchFilter) adapter.getFilter();
                searchFilter.setShowOnlyFavourites(filterView.isChecked());
                searchFilter.filter(query);
                return false;
            }
        });

        filterView.setOnCheckedChangeListener((button, checked) -> {
            PropertyAdapter adapter = ((Properties) fragment).getAdapter();
            FirebaseRecyclerAdapter.SearchFilter searchFilter = (FirebaseRecyclerAdapter.SearchFilter) adapter.getFilter();
            searchFilter.setShowOnlyFavourites(checked);
            searchFilter.filter(searchView.getQuery());
        });

        searchable(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void searchable(boolean showMenu) {
        if (menu == null)
            return;
        menu.setGroupVisible(R.id.searchPropertiesMenuItem, showMenu);

        if (!showMenu) {
            MenuItem searchProperties = menu.findItem(R.id.searchProperties);
            searchProperties.collapseActionView();
            MenuItem filterFavourites = menu.findItem(R.id.filterFavourites);
            SwitchMaterial filterFavouritesActionView = (SwitchMaterial) filterFavourites.getActionView();
            if (filterFavouritesActionView.isChecked()) {
                filterFavouritesActionView.setChecked(false);
            }
        }
    }

    private void bottomMenu() {
        binding.bottomNavigationMenu.setOnItemSelectedListener(i -> {
            switch (i) {
                case R.id.bottom_navigation_dashboard:
                    binding.navigation.setCheckedItem(R.id.navigationDashboard);
                    binding.toolbar.setTitle("Dashboard");
                    searchable(false);
                    fragment = new Dashboard();
                    break;
                case R.id.bottom_navigation_properties:
                    binding.navigation.getCheckedItem().setChecked(false);
                    binding.toolbar.setTitle("Properties");
                    searchable(true);
                    fragment = new Properties();
                    break;
                case R.id.bottom_navigation_scanner:
                    binding.navigation.getCheckedItem().setChecked(false);
                    binding.toolbar.setTitle("Scanner");
                    searchable(false);
                    fragment = new Scanner();
                    break;
                case R.id.bottom_navigation_notification:
                    binding.navigation.getCheckedItem().setChecked(false);
                    resetBadge();
                    binding.toolbar.setTitle("Notifications");
                    searchable(false);
                    fragment = new Notifications();
                    break;
                case R.id.bottom_navigation_profile:
                    binding.navigation.getCheckedItem().setChecked(false);
                    binding.toolbar.setTitle("Profile");
                    searchable(false);
                    fragment = new Profile();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
        });
    }

    private void leftMenu() {
        binding.navigation.setNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.navigationDashboard:
                    binding.bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_dashboard, true);
                    break;
                case R.id.navigationHistory:
                    binding.bottomNavigationMenu.setItemSelected(binding.bottomNavigationMenu.getSelectedItemId(), false);
                    binding.toolbar.setTitle("History");
                    fragment = new History();
                    break;
                case R.id.navigationNotifications:
                    binding.bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_notification, true);
                    resetBadge();
                    binding.toolbar.setTitle("Notifications");
                    fragment = new Notifications();
                    break;
                case R.id.navigationLogout:
                    fragment = null;
                    UserUtils.signOut(getApplicationContext());
                    finish();
                    break;

            }

            if (fragment != null) {
                item.setChecked(true);
                binding.drawerLayout.closeDrawers();

                getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
                return true;
            }

            return false;
        });
    }

    private void resetBadge() {
        PropertyUtils.dismissBadge(this);
    }

    private void setOnUnreadNotificationListener() {
        Activity activity = this;

        User user = UserUtils.getLocalUser(getApplicationContext());
        unreadNotificationsQuery.child("unread_notifications")
                .orderByChild("userId")
                .equalTo(user.getId())
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        snapshot.getChildren().forEach(s -> {
                            UnreadNotification unreadNotification = s.getValue(UnreadNotification.class);
                            long count = unreadNotification.getNotificationIds().values().stream().filter(Boolean::booleanValue).count();
                            updateBadge(activity, count);
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateBadge(Activity activity, long count) {
        if (count > 0)
            PropertyUtils.showBadge(activity, count);
        else
            PropertyUtils.dismissBadge(activity);
    }

    @NoArgsConstructor
    @Setter
    public final static class OnImageChangedBroadcastReceiver extends BroadcastReceiver {

        private String userId;
        private ImageView imageView;

        @Override
        public void onReceive(Context context, Intent intent) {
            ImageUtils.syncAndloadImages(context, userId, imageView);
        }
    }

    @NoArgsConstructor
    @Setter
    public final static class OnActionBroadcastReceiver extends BroadcastReceiver {

        private Activity activity;

        @Override
        public void onReceive(Context context, Intent intent) {
            String description = intent.getStringExtra("description");
            Property property = intent.getParcelableExtra("property");
            String action = intent.getStringExtra("action");
            PropertyUtils.notify(activity, description, property, action);
        }
    }
}