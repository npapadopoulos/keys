package com.property.keys;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.databinding.ActivityContainerBinding;
import com.property.keys.entities.Property;
import com.property.keys.entities.UnreadNotification;
import com.property.keys.entities.User;
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
public class Container extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
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
        updateStatusBarOptions();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        binding = ActivityContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navigation.setNavigationItemSelectedListener(this);
        View view = binding.navigation.getHeaderView(0);
        ImageView navigationProfileImage = view.findViewById(R.id.navigationProfileImage);
        TextView firstNameLabel = view.findViewById(R.id.firstName);
        TextView lastNameLabel = view.findViewById(R.id.lastName);

        User user = UserUtils.getLocalUser(getApplicationContext());
        firstNameLabel.setText(user.getFirstName());
        lastNameLabel.setText(user.getLastName());

        binding.toolbar.setTitle("Dashboard");
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

        updateStatusBarOptions();
        bottomMenu();
        setOnUnreadNotificationListener();

        String selectedMenu = getIntent().getStringExtra("selected");
        if (selectedMenu != null && selectedMenu.equalsIgnoreCase("Properties")) {
            binding.bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_properties, true);
        } else {
            binding.bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_dashboard, true);
        }
    }

    private void updateStatusBarOptions() {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorGrey));
    }

    private void bottomMenu() {
        binding.drawerLayout.closeDrawers();
        binding.bottomNavigationMenu.setOnItemSelectedListener(i -> {
            binding.toolbar.setEnabled(true);
            binding.toolbar.setVisibility(View.VISIBLE);
            switch (i) {
                case R.id.bottom_navigation_dashboard:
                    binding.navigation.setCheckedItem(R.id.navigationDashboard);
                    binding.navigation.getCheckedItem().setChecked(true);
                    binding.toolbar.setTitle("Dashboard");
                    fragment = new Dashboard();
                    break;
                case R.id.bottom_navigation_properties:
                    binding.navigation.getCheckedItem().setChecked(false);
                    binding.toolbar.setEnabled(false);
                    binding.toolbar.setVisibility(View.GONE);
                    fragment = new Properties();
                    break;
                case R.id.bottom_navigation_scanner:
                    binding.navigation.getCheckedItem().setChecked(false);
                    binding.toolbar.setTitle("Scanner");
                    fragment = new Scanner();
                    break;
                case R.id.bottom_navigation_notification:
                    binding.navigation.getCheckedItem().setChecked(false);
                    resetBadge();
                    binding.toolbar.setTitle("Notifications");
                    fragment = new Notifications();
                    break;
                case R.id.bottom_navigation_profile:
                    binding.navigation.getCheckedItem().setChecked(false);
                    binding.toolbar.setTitle("Profile");
                    fragment = new Profile();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigationDashboard:
                binding.bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_dashboard, true);
                binding.toolbar.setTitle("Dashboard");
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
    }

    public RelativeLayout getContentLayout() {
        return binding.content;
    }

    public DrawerLayout getDrawerLayout() {
        return binding.drawerLayout;
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