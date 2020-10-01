package com.property.keys.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.databinding.FragmentHistoryBinding;

public class History extends Fragment {

    private ChipNavigationBar bottomNavigationMenu;
    private MaterialToolbar toolbar;

    public History(ChipNavigationBar bottomNavigationMenu, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentHistoryBinding binding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);

        toolbar.setTitle("History");
        bottomNavigationMenu.setItemSelected(bottomNavigationMenu.getSelectedItemId(), false);

        return binding.getRoot();
    }
}