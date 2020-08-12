package com.property.keys.utils;

import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener;

import com.google.android.material.navigation.NavigationView;
import com.property.keys.R;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NavigationUtils {
    private static final String TAG = "NavigationUtils";

    private static final float END_SCALE = 0.7f;

    private NavigationUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void initNavigation(NavigationView view,
                                      DrawerLayout drawerLayout,
                                      RelativeLayout content) {

        view.bringToFront();
        view.setCheckedItem(R.id.navigationDashboard);

//        drawerLayout.setScrimColor(view.getResources().getColor(R.color.colorPrimaryLight));
//        createOnDrawSlidListener(drawerLayout, content);
    }

    private static void createOnDrawSlidListener(DrawerLayout drawerLayout, RelativeLayout content) {
        drawerLayout.addDrawerListener(new SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                final float diffScaledOfset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOfset;
                content.setScaleX(offsetScale);
                content.setScaleY(offsetScale);

                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = content.getWidth() * diffScaledOfset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                content.setTranslationX(xTranslation);
            }
        });
    }

    @Deprecated
    public static void onMenuClick(DrawerLayout drawerLayout, ImageView headerMenuIcon) {
        headerMenuIcon.setOnClickListener(view1 -> {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }
}
