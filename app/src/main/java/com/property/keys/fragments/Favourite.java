package com.property.keys.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.property.keys.R;
import com.property.keys.databinding.FragmentFavouriteBinding;
import com.property.keys.utils.NavigationUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Favourite extends Fragment {
    private static final String TAG = Favourite.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentFavouriteBinding binding = FragmentFavouriteBinding.inflate(getLayoutInflater(), container, false);
        NavigationUtils.onMenuClick(getActivity().findViewById(R.id.drawer_layout), binding.headerMenuIcon);
        return binding.getRoot();
    }
}