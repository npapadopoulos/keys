package com.property.keys.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.property.keys.R;
import com.property.keys.databinding.FragmentDashboardBinding;
import com.property.keys.utils.NavigationUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Dashboard extends Fragment {
    private static final String TAG = Dashboard.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDashboardBinding binding = FragmentDashboardBinding.inflate(getLayoutInflater(), container, false);
        NavigationUtils.onMenuClick(getActivity().findViewById(R.id.drawer_layout), binding.headerMenuIcon);
        return binding.getRoot();
    }
}