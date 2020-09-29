package com.property.keys.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.property.keys.PropertyDetails;
import com.property.keys.R;
import com.property.keys.databinding.FragmentScannerBinding;
import com.property.keys.entities.Action;
import com.property.keys.entities.Key;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.NotificationUtils;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.property.keys.utils.Utils.DATE_TIME_FORMATTER;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Scanner extends Fragment {
    private static final String TAG = Scanner.class.getSimpleName();


    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private CodeScanner mCodeScanner;

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
        toolbar.setTitle("Scanner");

        User user = UserUtils.getLocalUser(requireContext());
        CodeScannerView scannerView = binding.scannerView;
        mCodeScanner = new CodeScanner(requireActivity(), scannerView);
        mCodeScanner.setDecodeCallback(result -> firebaseDatabase.getReference("keys")
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
                                                requireActivity().runOnUiThread(() -> {
                                                    Map<String, Object> updates = new HashMap<>();
                                                    Property property = snapshot.getValue(Property.class);
                                                    View dialog = getLayoutInflater().inflate(R.layout.property_dialog, null);

                                                    ImageView propertyImage = dialog.findViewById(R.id.propertyImage);
                                                    ImageUtils.loadImage(requireContext(), property.getId(), propertyImage);

                                                    TextView propertyName = dialog.findViewById(R.id.propertyName);
                                                    propertyName.setText(property.getName());

                                                    TextView propertyAddress = dialog.findViewById(R.id.propertyAddress);
                                                    propertyAddress.setText(property.getAddress());

                                                    TextView checkedInDate = dialog.findViewById(R.id.checkedInDate);
                                                    TextView checkedInByUser = dialog.findViewById(R.id.checkedInByUser);

                                                    TextView message = dialog.findViewById(R.id.message);
                                                    if (key.getCheckedInDate() == null) {
                                                        checkedInDate.setVisibility(View.GONE);
                                                        checkedInByUser.setVisibility(View.GONE);
                                                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                                                                .setPositiveButton("Yes", (dialogInterface, i) -> {
                                                                    key.setCheckedInDate(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
                                                                    key.setCheckedOutDate(null);
                                                                    key.setLastCheckedInUser(user.getFirstName() + " " + user.getLastName());
                                                                    updates.put("users/" + user.getId() + "/keys/" + key.getId(), key);
                                                                    updates.put("keys/" + key.getId(), key);
                                                                    updates.put("properties/" + key.getPropertyId() + "/keys/" + key.getId(), key);
                                                                    firebaseDatabase.getReference("/").updateChildren(updates);
                                                                    Snackbar.make(binding.scannerView, "Key checked in successfully.", Snackbar.LENGTH_LONG).show();
                                                                    NotificationUtils.create(requireActivity(), property, Action.CHECKED_IN);
                                                                    Intent propertyDetails = new Intent(requireContext(), PropertyDetails.class);
                                                                    propertyDetails.putExtra("property", property);
                                                                    requireContext().startActivity(propertyDetails);

                                                                })
                                                                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                                                                .setNegativeButton("No", Utils::onClick)
                                                                .setCancelable(false);

                                                        materialAlertDialogBuilder.setView(dialog);
                                                        materialAlertDialogBuilder.create().show();
                                                    } else if (!key.getLastCheckedInUser().equalsIgnoreCase(user.getFirstName() + " " + user.getLastName())) {
                                                        Snackbar.make(binding.scannerView, "Key was checked in on " + key.getCheckedInDate() + " by another user. Contact " + key.getLastCheckedInUser() + " to check out the key.", Snackbar.LENGTH_LONG).show();
                                                    } else {
                                                        checkedInDate.setText("Checked in on " + key.getCheckedInDate() + ".");
                                                        checkedInByUser.setText("Checked by " + key.getLastCheckedInUser() + ".");
                                                        message.setText("Are you sure you want to check out?");
                                                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                                                                .setPositiveButton("Yes", (dialogInterface, i) -> {
                                                                    key.setCheckedInDate(null);
                                                                    key.setCheckedOutDate(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
                                                                    key.setLastCheckOutDate(key.getCheckedOutDate());
                                                                    updates.put("users/" + user.getId() + "/keys/" + key.getId(), key);
                                                                    updates.put("keys/" + key.getId(), key);
                                                                    updates.put("properties/" + key.getPropertyId() + "/keys/" + key.getId(), key);
                                                                    firebaseDatabase.getReference("/").updateChildren(updates);
                                                                    Snackbar.make(binding.scannerView, "Key checked out successfully.", Snackbar.LENGTH_LONG).show();
                                                                    NotificationUtils.create(requireActivity(), property, Action.CHECKED_OUT);
                                                                    Intent propertyDetails = new Intent(requireContext(), PropertyDetails.class);
                                                                    propertyDetails.putExtra("property", property);
                                                                    requireContext().startActivity(propertyDetails);
                                                                })
                                                                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                                                                .setNegativeButton("No", Utils::onClick)
                                                                .setCancelable(false);
                                                        materialAlertDialogBuilder.setView(dialog);
                                                        materialAlertDialogBuilder.create().show();
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        } else {
                            Snackbar.make(binding.scannerView, "No key found.", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }));
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