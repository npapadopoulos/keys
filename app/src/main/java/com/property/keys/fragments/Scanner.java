package com.property.keys.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.databinding.FragmentScannerBinding;
import com.property.keys.entities.Key;
import com.property.keys.entities.Property;
import com.property.keys.utils.Utils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Scanner extends Fragment {
    private static final String TAG = Scanner.class.getSimpleName();

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private CodeScanner codeScanner;

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;

    public Scanner(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentScannerBinding binding = FragmentScannerBinding.inflate(getLayoutInflater(), container, false);
        bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_scanner, true);
        navigation.getCheckedItem().setChecked(false);
        this.toolbar.setTitle("Scanner");

        CodeScannerView scannerView = binding.scannerView;
        codeScanner = new CodeScanner(requireActivity(), scannerView);
        codeScanner.setDecodeCallback(result -> firebaseDatabase.getReference("keys")
                .child(result.getText())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot keySnapshot) {
                        if (keySnapshot.exists()) {

                            Key key = keySnapshot.getValue(Key.class);

                            firebaseDatabase.getReference("properties").child(key.getPropertyId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                Property property = snapshot.getValue(Property.class);
                                                if (property.isDeleted()) {
                                                    Snackbar snackbar = Snackbar.make(((Container) getActivity()).getPlaceSnackBar(), "The current property has been deleted.", Snackbar.LENGTH_LONG);
                                                    snackbar.addCallback(new Snackbar.Callback() {
                                                        @Override
                                                        public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event) {
                                                            codeScanner.startPreview();
                                                        }
                                                    });
                                                    snackbar.show();
                                                } else {
                                                    codeScanner.releaseResources();
                                                    CheckInOut checkInOut = CheckInOut.newInstance(property, key, codeScanner);
                                                    checkInOut.show(requireActivity().getSupportFragmentManager(), "checkInOut");
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        } else {
                            AlertDialog ok = new MaterialAlertDialogBuilder(requireContext())
                                    .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                                    .setMessage("No key found.")
                                    .setNeutralButton("Ok", Utils::onClick).create();
                            ok.setOnDismissListener(dialog -> codeScanner.startPreview());
                            ok.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }));
        scannerView.setOnClickListener(view -> codeScanner.startPreview());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
//        Utils.checkForPermissions(getActivity());
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    public void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }
}