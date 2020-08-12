package com.property.keys;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.property.keys.databinding.ActivityContainerBinding;
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

import java.util.HashMap;
import java.util.Map;

import lombok.NoArgsConstructor;
import lombok.Setter;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Container extends AppCompatActivity {
    private static final String TAG = Container.class.getSimpleName();

    private final int REQUEST_CODE_SIGN_IN = 100;

    public static final Map<String, Boolean> UNREAD = new HashMap<>();
    private final Query notificationsQuery = FirebaseDatabase.getInstance().getReference().child("notifications").limitToLast(10).orderByKey();

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        binding = ActivityContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View view = binding.navigation.getHeaderView(0);
        CircularImageView navigationProfileImage = view.findViewById(R.id.navigationProfileImage);
        TextView firstNameLabel = view.findViewById(R.id.firstName);
        TextView lastNameLabel = view.findViewById(R.id.lastName);

        User user = UserUtils.getUser(getApplicationContext());
        firstNameLabel.setText(user.getFirstName());
        lastNameLabel.setText(user.getLastName());

        //TODO when back is pressed. eg. from Properties Details
        String selectedMenu = getIntent().getStringExtra("selected");

        if (selectedMenu != null && selectedMenu.equalsIgnoreCase("Properties")) {
            binding.toolbar.setTitle("Properties");
            fragment = new Properties();
        } else {
            binding.toolbar.setTitle("Dashboard");
            fragment = new Dashboard();
        }

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

        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
        binding.bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_dashboard, true);

        bottomMenu();
        leftMenu();
        initNotificationQuery(true);
    }

    private void bottomMenu() {
        binding.bottomNavigationMenu.setOnItemSelectedListener(i -> {
            switch (i) {
                case R.id.bottom_navigation_dashboard:
                    binding.navigation.setCheckedItem(R.id.navigationDashboard);
                    binding.toolbar.setTitle("Dashboard");
                    fragment = new Dashboard();
                    break;
                case R.id.bottom_navigation_properties:
                    binding.navigation.getCheckedItem().setChecked(false);
                    binding.toolbar.setTitle("Properties");
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

    @NoArgsConstructor
    @Setter
    public final static class OnImageChangedBroadcastReceiver extends BroadcastReceiver {

        private String userId;
        private CircularImageView imageView;

        @Override
        public void onReceive(Context context, Intent intent) {
            ImageUtils.syncAndloadImages(context, userId, imageView);
        }
    }

    private void resetBadge() {
        PropertyUtils.dismissBadge(this);
        UNREAD.entrySet().forEach(notification -> notification.setValue(false));
    }

    public Query getNotificationQuery() {
        return initNotificationQuery(false);
    }

    private Query initNotificationQuery(boolean initialize) {
        if (initialize) {

            resetBadge();
            PropertyUtils.getNotificationCount(getApplicationContext()).forEach(id -> {
                UNREAD.put(id, true);
            });

            Activity activity = this;
            notificationsQuery.getRef().addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot notification, @Nullable String previousChildName) {
                    Boolean unread = UNREAD.get(notification.getKey());
                    if (unread == null) {
                        UNREAD.put(notification.getKey(), true);
                    } else if (!unread) {
                        UNREAD.put(notification.getKey(), false);
                    }
                    PropertyUtils.showBadge(activity, UNREAD.entrySet().stream().filter(Map.Entry::getValue).count());
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return notificationsQuery;
    }

    @NoArgsConstructor
    @Setter
    public final static class OnActionBroadcastReceiver extends BroadcastReceiver {

        private Activity activity;

        @Override
        public void onReceive(Context context, Intent intent) {
            String userId = intent.getStringExtra("userId");
            String description = intent.getStringExtra("description");
            PropertyUtils.notify(activity, description, userId);
        }
    }
}