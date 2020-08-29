package com.property.keys.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.databinding.FragmentHistoryBinding;

public class History extends Fragment {

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;

    public History(NavigationView navigation, MaterialToolbar toolbar) {
        this(null, navigation, toolbar);
    }

    public History(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentHistoryBinding binding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);

        bottomNavigationMenu.setItemSelected(bottomNavigationMenu.getSelectedItemId(), false);
        toolbar.setTitle("History");

        return binding.getRoot();
    }
}