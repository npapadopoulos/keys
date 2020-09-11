package com.property.keys.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.R;
import com.property.keys.databinding.FragmentScannerBinding;
import com.property.keys.entities.Key;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Scanner extends Fragment {
    private static final String TAG = Scanner.class.getSimpleName();

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private CodeScanner mCodeScanner;

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;

    public Scanner(NavigationView navigation, MaterialToolbar toolbar) {
        this(null, navigation, toolbar);
    }

    public Scanner(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentScannerBinding binding = FragmentScannerBinding.inflate(getLayoutInflater(), container, false);
        FragmentActivity activity = getActivity();

        bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_scanner, true);
        navigation.getCheckedItem().setChecked(false);
        toolbar.setTitle("Scanner");

        CodeScannerView scannerView = binding.scannerView;
        mCodeScanner = new CodeScanner(activity, scannerView);
        mCodeScanner.setDecodeCallback(result -> {
            firebaseDatabase.getReference("properties/keys/").child(result.getText())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Key key = snapshot.getValue(Key.class);
                            activity.runOnUiThread(() -> Snackbar.make(binding.scannerView, "Found key for with id '" + key.getId() + "'.", Snackbar.LENGTH_LONG).show());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        });
        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}