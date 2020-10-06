package com.property.keys.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.Container;
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
        toolbar.setTitle("Scanner");

        User user = UserUtils.getLocalUser(requireContext());
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
                                                    requireActivity().runOnUiThread(() -> {
                                                        Map<String, Object> updates = new HashMap<>();
                                                        View dialog = getLayoutInflater().inflate(R.layout.checkin_out_dialog, null);

                                                        FrameLayout propertyLayout = dialog.findViewById(R.id.propertyContainer);

                                                        ImageView propertyImage = propertyLayout.findViewById(R.id.propertyImage);
                                                        ImageUtils.loadImage(requireContext(), property.getId(), propertyImage);

                                                        TextView propertyName = propertyLayout.findViewById(R.id.name);
                                                        propertyName.setText(property.getName());

                                                        TextView propertyAddress = propertyLayout.findViewById(R.id.address);
                                                        propertyAddress.setText(property.getAddress());

                                                        TextView availableSum = propertyLayout.findViewById(R.id.availableSum);
                                                        availableSum.setText(String.valueOf(property.getKeys().values().stream().filter(k -> k.getCheckedInDate() == null).count()));

                                                        TextView busySum = propertyLayout.findViewById(R.id.busySum);
                                                        busySum.setText(String.valueOf(property.getKeys().values().stream().filter(k -> k.getCheckedInDate() != null).count()));

                                                        TextView checkedInByUser = dialog.findViewById(R.id.checkedInByUser);
                                                        TextView checkedInDate = dialog.findViewById(R.id.checkedInDate);

                                                        TextView reasonLabel = dialog.findViewById(R.id.reasonLabel);
                                                        RadioGroup reason = dialog.findViewById(R.id.reason);
                                                        TextInputLayout customReason = dialog.findViewById(R.id.customReason);

                                                        reason.setOnCheckedChangeListener((group, checkedId) -> {
                                                            if (checkedId == R.id.other) {
                                                                customReason.setVisibility(View.VISIBLE);
                                                                customReason.getEditText().setText("");
                                                                customReason.setError("");
                                                                customReason.setErrorEnabled(false);
                                                            } else {
                                                                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                in.hideSoftInputFromWindow(dialog.getWindowToken(), 0);

                                                                customReason.setVisibility(View.GONE);
                                                            }
                                                        });

                                                        final AlertDialog alertDialog;
                                                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireContext());
                                                        if (key.getCheckedInDate() == null) {
                                                            reasonLabel.setVisibility(View.VISIBLE);
                                                            reason.setVisibility(View.VISIBLE);

                                                            checkedInDate.setVisibility(View.GONE);
                                                            checkedInByUser.setVisibility(View.GONE);

                                                            alertDialog = materialAlertDialogBuilder.setPositiveButton("Check In", null)
                                                                    .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                                                                    .setNegativeButton("Cancel", Utils::onClick)
                                                                    .setCancelable(false)
                                                                    .setOnKeyListener((d, keyCode, event) -> {
                                                                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                                            d.dismiss();
                                                                            return true;
                                                                        }
                                                                        return false;
                                                                    })
                                                                    .setOnDismissListener(dialog1 -> codeScanner.startPreview())
                                                                    .setView(dialog)
                                                                    .create();

                                                            alertDialog.setOnShowListener(d ->
                                                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                                                                        boolean reasonValid = true;
                                                                        String reasonValue = ((RadioButton) reason.findViewById(reason.getCheckedRadioButtonId())).getText().toString();
                                                                        if (reason.getCheckedRadioButtonId() == R.id.other) {
                                                                            reasonValue = customReason.getEditText().getText().toString();
                                                                            reasonValid = Utils.validateText(customReason, 20, 50);
                                                                        }


                                                                        if (reasonValid) {
                                                                            key.setCheckedInDate(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
                                                                            key.setCheckinReason(reasonValue);
                                                                            key.setCheckedOutDate(null);
                                                                            key.setLastCheckedInUser(user.getFirstName() + " " + user.getLastName());
                                                                            updates.put("users/" + user.getId() + "/keys/" + key.getId(), key);
                                                                            updates.put("keys/" + key.getId(), key);
                                                                            updates.put("properties/" + key.getPropertyId() + "/keys/" + key.getId(), key);
                                                                            firebaseDatabase.getReference("/").updateChildren(updates);
                                                                            Snackbar.make(binding.scannerView, "Key checked in successfully.", Snackbar.LENGTH_LONG).show();

                                                                            NotificationUtils.create(requireActivity(), property, key, Action.CHECKED_IN); // TODO add reason
                                                                            Intent propertyDetails = new Intent(requireContext(), PropertyDetails.class);
                                                                            propertyDetails.putExtra("property", property);
                                                                            requireContext().startActivity(propertyDetails);
                                                                            alertDialog.dismiss();
                                                                        }
                                                                    }));

                                                        } else if (!key.getLastCheckedInUser().equalsIgnoreCase(user.getFirstName() + " " + user.getLastName())) {
                                                            reasonLabel.setVisibility(View.GONE);
                                                            reason.setVisibility(View.GONE);

                                                            checkedInDate.setText("Checked in on " + key.getCheckedInDate() + ".");
                                                            checkedInByUser.setText("Checked by " + key.getLastCheckedInUser() + " for " + key.getCheckinReason() + ".");
                                                            alertDialog = materialAlertDialogBuilder.setNeutralButton("Ok", Utils::onClick)
                                                                    .setCancelable(false)
                                                                    .setOnKeyListener((d, keyCode, event) -> {
                                                                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                                            d.dismiss();
                                                                            return true;
                                                                        }
                                                                        return false;
                                                                    })
                                                                    .setOnDismissListener(dialog1 -> codeScanner.startPreview())
                                                                    .setView(dialog).create();
                                                        } else {
                                                            reasonLabel.setVisibility(View.GONE);
                                                            reason.setVisibility(View.GONE);

                                                            checkedInDate.setText("Checked in on " + key.getCheckedInDate() + ".");
                                                            checkedInByUser.setText("Checked by you for " + key.getCheckinReason() + ".");
                                                            alertDialog = materialAlertDialogBuilder.setPositiveButton("Check Out", (dialogInterface, i) -> {
                                                                key.setCheckinReason("Check out");
                                                                key.setCheckedInDate(null);
                                                                key.setCheckedOutDate(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
                                                                key.setLastCheckOutDate(key.getCheckedOutDate());
                                                                updates.put("users/" + user.getId() + "/keys/" + key.getId(), key);
                                                                updates.put("keys/" + key.getId(), key);
                                                                updates.put("properties/" + key.getPropertyId() + "/keys/" + key.getId(), key);
                                                                firebaseDatabase.getReference("/").updateChildren(updates);
                                                                Snackbar.make(binding.scannerView, "Key checked out successfully.", Snackbar.LENGTH_LONG).show();
                                                                NotificationUtils.create(requireActivity(), property, key, Action.CHECKED_OUT); // TODO add reason
                                                                Intent propertyDetails = new Intent(requireContext(), PropertyDetails.class);
                                                                propertyDetails.putExtra("property", property);
                                                                requireContext().startActivity(propertyDetails);
                                                            })
                                                                    .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                                                                    .setNegativeButton("Cancel", Utils::onClick)
                                                                    .setCancelable(false)
                                                                    .setOnKeyListener((d, keyCode, event) -> {
                                                                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                                            d.dismiss();
                                                                            return true;
                                                                        }
                                                                        return false;
                                                                    })
                                                                    .setOnDismissListener(dialog1 -> codeScanner.startPreview())
                                                                    .setView(dialog).create();
                                                        }

                                                        alertDialog.show();
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        } else {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                                    .setMessage("No key found.")
                                    .setNeutralButton("Ok", Utils::onClick).create().show();
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