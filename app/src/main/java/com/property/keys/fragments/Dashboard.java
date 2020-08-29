package com.property.keys.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.R;
import com.property.keys.databinding.FragmentDashboardBinding;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Dashboard extends Fragment {
    private static final String TAG = Dashboard.class.getSimpleName();

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;

    public Dashboard(NavigationView navigation, MaterialToolbar toolbar) {
        this(null, navigation, toolbar);
    }

    public Dashboard(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDashboardBinding binding = FragmentDashboardBinding.inflate(getLayoutInflater(), container, false);

        bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_dashboard, true);
        navigation.setCheckedItem(R.id.navigationDashboard);
        navigation.getCheckedItem().setChecked(true);
        toolbar.setTitle("Dashboard");

        return binding.getRoot();
    }
}