package com.property.keys.utils;

import android.os.Build;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.property.keys.R;
import com.property.keys.entities.Role;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NavigationUtils {
    private static final String TAG = "NavigationUtils";

    private NavigationUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void initNavigation(NavigationView view, Role role) {
        view.bringToFront();
        view.setCheckedItem(R.id.navigationDashboard);

        view.getMenu().findItem(R.id.navigationUsers).setVisible(role == Role.ADMIN);
    }

    @Deprecated
    public static void onMenuClick(DrawerLayout drawerLayout, ImageView headerMenuIcon) {
        headerMenuIcon.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START) || drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }
}
