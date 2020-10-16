package com.property.keys.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.budiyev.android.codescanner.CodeScanner;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.PropertyDetails;
import com.property.keys.R;
import com.property.keys.databinding.FragmentCheckinOutDialogBinding;
import com.property.keys.entities.Action;
import com.property.keys.entities.Key;
import com.property.keys.entities.Property;
import com.property.keys.entities.Role;
import com.property.keys.entities.User;
import com.property.keys.utils.FileUtils;
import com.property.keys.utils.NotificationUtils;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import static com.property.keys.utils.Utils.DATE_TIME_FORMATTER;
import static com.property.keys.utils.Utils.showDatePicker;

@RequiresApi(api = Build.VERSION_CODES.R)
@RequiredArgsConstructor
public class CheckInOut extends DialogFragment implements DialogInterface.OnDismissListener {

    private static final String TAG = CheckInOut.class.getSimpleName();

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final Property property;
    private final Key key;
    private final CodeScanner codeScanner;
    private FragmentCheckinOutDialogBinding binding;
    private User user;

    private MenuItem submit;

    public static CheckInOut newInstance(Property property, Key key, CodeScanner codeScanner) {
        return new CheckInOut(property, key, codeScanner);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckinOutDialogBinding.inflate(getLayoutInflater());
        submit = binding.checkInOutToolbar.getMenu().getItem(0);
        binding.checkInOutToolbar.setTitle(getResources().getString(key.getCheckedInDate() == null ? R.string.check_in : R.string.check_out));

        user = UserUtils.getLocalUser(requireContext());

        LinearLayout propertyDialog = binding.propertyDialog;
        FrameLayout propertyLayout = propertyDialog.findViewById(R.id.propertyContainer);

        ImageView propertyImage = propertyLayout.findViewById(R.id.propertyImage);
        FileUtils.loadImage(requireContext(), property.getId(), propertyImage);

        TextView propertyName = propertyLayout.findViewById(R.id.name);
        propertyName.setText(property.getName());

        TextView propertyAddress = propertyLayout.findViewById(R.id.address);
        propertyAddress.setText(property.getAddress());

        TextView availableSum = propertyLayout.findViewById(R.id.availableSum);
        availableSum.setText(String.valueOf(property.getKeys().values().stream().filter(k -> k.getCheckedInDate() == null).count()));

        TextView busySum = propertyLayout.findViewById(R.id.busySum);
        busySum.setText(String.valueOf(property.getKeys().values().stream().filter(k -> k.getCheckedInDate() != null).count()));

        prepareContent();

        addOnButtonsClickListeners();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireDialog().getWindow().setWindowAnimations(R.style.ToolbarDialogAnimation);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        codeScanner.startPreview();
        super.onDismiss(dialog);
    }

    private void prepareContent() {
        if (key.getCheckedInDate() == null) {
            binding.reasonLabel.setVisibility(View.VISIBLE);
            binding.reason.setVisibility(View.VISIBLE);
            binding.estimatedCheckOutDate.setVisibility(View.VISIBLE);

            binding.checkedInDate.setVisibility(View.GONE);
            binding.checkedInByUser.setVisibility(View.GONE);
            binding.estimatedCheckOutDateValue.setVisibility(View.GONE);
        } else if (!key.getLastCheckedInUser().equalsIgnoreCase(user.getFirstName() + " " + user.getLastName())) {
            binding.reasonLabel.setVisibility(View.GONE);
            binding.reason.setVisibility(View.GONE);
            binding.estimatedCheckOutDate.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(key.getEstimatedCheckOutDate())) {
                binding.estimatedCheckOutDateValue.setVisibility(View.VISIBLE);
                binding.estimatedCheckOutDateValue.setText("Estimated Check out date: " + key.getEstimatedCheckOutDate() + ".");
            } else {
                binding.estimatedCheckOutDateValue.setVisibility(View.GONE);
            }
            binding.checkedInDate.setText("Checked in on " + key.getCheckedInDate() + ".");
            binding.checkedInByUser.setText("Checked by " + key.getLastCheckedInUser() + " for " + key.getCheckInReason() + ".");
        } else {
            binding.reasonLabel.setVisibility(View.GONE);
            binding.reason.setVisibility(View.GONE);
            binding.estimatedCheckOutDate.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(key.getEstimatedCheckOutDate())) {
                binding.estimatedCheckOutDateValue.setVisibility(View.VISIBLE);
                binding.estimatedCheckOutDateValue.setText("Estimated Check out date: " + key.getEstimatedCheckOutDate() + ".");
            } else {
                binding.estimatedCheckOutDateValue.setVisibility(View.GONE);
            }
            binding.checkedInDate.setText("Checked in on " + key.getCheckedInDate() + ".");
            binding.checkedInByUser.setText("Checked by you for " + key.getCheckInReason() + ".");
        }

    }

    private void addOnButtonsClickListeners() {
        binding.checkInOutToolbar.getMenu().getItem(0).setOnMenuItemClickListener(view -> {

            InputMethodManager in = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);

            Map<String, Object> updates = new HashMap<>();

            if (key.getCheckedInDate() == null) {
                boolean reasonValid = true;
                String reasonValue = ((RadioButton) binding.reason.findViewById(binding.reason.getCheckedRadioButtonId())).getText().toString();
                if (binding.reason.getCheckedRadioButtonId() == R.id.other) {
                    reasonValue = binding.customReason.getEditText().getText().toString();
                    reasonValid = Utils.validateText(binding.customReason, 20, 50);
                }
                if (reasonValid) {
                    key.setEstimatedCheckOutDate(binding.estimatedCheckOutDate.getText().toString());
                    key.setCheckedInDate(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
                    key.setCheckInReason(reasonValue);
                    key.setCheckedOutDate(null);
                    key.setLastCheckedInUser(user.getFirstName() + " " + user.getLastName());
                    key.setLastCheckedInUserId(user.getId());
                    updates.put("users/" + user.getId() + "/keys/" + key.getId(), key);
                    updates.put("keys/" + key.getId(), key);
                    updates.put("properties/" + key.getPropertyId() + "/keys/" + key.getId(), key);
                    firebaseDatabase.getReference("/").updateChildren(updates);
                    Snackbar.make(binding.getRoot(), "Key checked in successfully.", Snackbar.LENGTH_LONG).show();

                    NotificationUtils.create(requireActivity(), property, key, Action.CHECKED_IN); // TODO add binding.reason.
                    Intent propertyDetails = new Intent(requireContext(), PropertyDetails.class);
                    propertyDetails.putExtra("property", property);
                    requireContext().startActivity(propertyDetails);
                    dismiss();
                }
            } else if (user.getRole() == Role.ADMIN || key.getLastCheckedInUser().equalsIgnoreCase(user.getFirstName() + " " + user.getLastName())) {
                key.setCheckInReason("Check out");
                key.setCheckedInDate(null);
                key.setEstimatedCheckOutDate(null);
                key.setCheckedOutDate(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
                key.setLastCheckOutDate(key.getCheckedOutDate());
                updates.put("users/" + user.getId() + "/keys/" + key.getId(), key);
                updates.put("keys/" + key.getId(), key);
                updates.put("properties/" + key.getPropertyId() + "/keys/" + key.getId(), key);
                firebaseDatabase.getReference("/").updateChildren(updates);

                NotificationUtils.create(requireActivity(), property, key, Action.CHECKED_OUT); // TODO add binding.reason.
                Intent propertyDetails = new Intent(requireContext(), PropertyDetails.class);
                propertyDetails.putExtra("property", property);
                requireContext().startActivity(propertyDetails);
                dismiss();
            } else {
                AlertDialog ok = new MaterialAlertDialogBuilder(requireContext())
                        .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                        .setMessage("Unable to Check Out. Key was checked in on " + key.getCheckedInDate() + " by " + key.getLastCheckedInUser() + ".")
                        .setNeutralButton("Ok", Utils::onClick).create();
                ok.setOnDismissListener(dialog -> dismiss());
                ok.show();
            }

            submit.setEnabled(false);
            return true;
        });

        binding.checkInOutToolbar.setNavigationOnClickListener(v -> dismiss());

        binding.estimatedCheckOutDate.setOnClickListener(v -> showDatePicker(requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    String updatedMonth = String.valueOf(monthOfYear);
                    String updatedDay = String.valueOf(dayOfMonth);
                    if (monthOfYear < 10) {
                        updatedMonth = "0" + monthOfYear;
                    }
                    if (dayOfMonth < 10) {
                        updatedDay = "0" + updatedDay;
                    }
                    binding.estimatedCheckOutDate.setText(updatedDay + "-" + updatedMonth + "-" + year);
                },
                null));

        binding.reason.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.other) {
                binding.customReason.setVisibility(View.VISIBLE);
                binding.customReason.getEditText().setText("");
                binding.customReason.setError("");
                binding.customReason.setErrorEnabled(false);
            } else {
                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                binding.customReason.setVisibility(View.GONE);
            }
        });
    }
}