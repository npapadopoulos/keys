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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.property.keys.databinding.ActivityContainerBinding;
import com.property.keys.entities.User;
import com.property.keys.fragments.Dashboard;
import com.property.keys.fragments.Favourite;
import com.property.keys.fragments.History;
import com.property.keys.fragments.Notifications;
import com.property.keys.fragments.Profile;
import com.property.keys.fragments.Properties;
import com.property.keys.fragments.Scanner;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.LoginUtils;
import com.property.keys.utils.NavigationUtils;
import com.property.keys.utils.Utils;

import org.jetbrains.annotations.NotNull;

import lombok.NoArgsConstructor;
import lombok.Setter;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Container extends AppCompatActivity {
    private static final String TAG = Container.class.getSimpleName();

    private final int REQUEST_CODE_SIGN_IN = 100;

    private ActivityContainerBinding binding;
    private CircularImageView navigationProfileImage;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private Fragment fragment;

    private OnImageChangedBroadcastReceiver onImageChangedBroadcastReceiver;
    private IntentFilter intentFilter;

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        LoginUtils.signOut();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(getAuthStateListener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(getAuthStateListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(onImageChangedBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onImageChangedBroadcastReceiver);
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View view = binding.navigation.getHeaderView(0);
        navigationProfileImage = view.findViewById(R.id.navigationProfileImage);
        TextView firstNameLabel = view.findViewById(R.id.firstName);
        TextView lastNameLabel = view.findViewById(R.id.lastName);

        User user = Utils.getUser(getApplicationContext());

        String selectedMenu = getIntent().getStringExtra("selected");

        if (selectedMenu != null && selectedMenu.equalsIgnoreCase("Properties")) {
            fragment = new Properties();
        } else {
            fragment = new Dashboard();
        }

        NavigationUtils.initNavigation(binding.navigation, binding.bottomNavigationMenu, binding.drawerLayout, binding.content);
        ImageUtils.syncAndloadImages(this, user.getId(), navigationProfileImage);

        onImageChangedBroadcastReceiver = new OnImageChangedBroadcastReceiver();
        onImageChangedBroadcastReceiver.setUserId(user.getId());
        onImageChangedBroadcastReceiver.setImageView(navigationProfileImage);

        intentFilter = new IntentFilter();
        intentFilter.addAction(getPackageName() + ".PROFILE_IMAGE_UPDATED");

        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
        binding.bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_dashboard, true);

        bottomMenu();
        leftMenu();
    }

    private void bottomMenu() {
        binding.bottomNavigationMenu.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i) {
                    case R.id.bottom_navigation_dashboard:
                        binding.navigation.setCheckedItem(R.id.navigationDashboard);
                        fragment = new Dashboard();
                        break;
                    case R.id.bottom_navigation_properties:
                        binding.navigation.getCheckedItem().setChecked(false);
                        fragment = new Properties();
                        break;
                    case R.id.bottom_navigation_scanner:
                        binding.navigation.getCheckedItem().setChecked(false);
                        fragment = new Scanner();
                        break;
                    case R.id.bottom_navigation_favourite:
                        binding.navigation.getCheckedItem().setChecked(false);
                        fragment = new Favourite();
                        break;
                    case R.id.bottom_navigation_profile:
                        binding.navigation.getCheckedItem().setChecked(false);
                        fragment = new Profile();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
            }
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
                    fragment = new History();
                    break;
                case R.id.navigationNotifications:
                    binding.bottomNavigationMenu.setItemSelected(binding.bottomNavigationMenu.getSelectedItemId(), false);
                    fragment = new Notifications();
                    break;
                case R.id.navigationLogout:
                    fragment = null;
                    LoginUtils.signOut();
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
}