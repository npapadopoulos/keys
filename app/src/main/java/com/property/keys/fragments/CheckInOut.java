package com.property.keys.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import androidx.fragment.app.DialogFragment;

import com.budiyev.android.codescanner.CodeScanner;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.PropertyDetails;
import com.property.keys.R;
import com.property.keys.databinding.FragmentCheckinOutDialogBinding;
import com.property.keys.entities.Action;
import com.property.keys.entities.Key;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.NotificationUtils;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import static com.property.keys.utils.Utils.DATE_TIME_FORMATTER;

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
        ImageUtils.loadImage(requireContext(), property.getId(), propertyImage);

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
            binding.estimatedCheckInDate.setVisibility(View.VISIBLE);

            binding.checkedInDate.setVisibility(View.GONE);
            binding.checkedInByUser.setVisibility(View.GONE);
        } else if (!key.getLastCheckedInUser().equalsIgnoreCase(user.getFirstName() + " " + user.getLastName())) {
            binding.reasonLabel.setVisibility(View.GONE);
            binding.reason.setVisibility(View.GONE);
            binding.estimatedCheckInDate.setVisibility(View.GONE);

            binding.checkedInDate.setText("Checked in on " + key.getCheckedInDate() + ".");
            binding.checkedInByUser.setText("Checked by " + key.getLastCheckedInUser() + " for " + key.getCheckInReason() + ".");
        } else {
            binding.reasonLabel.setVisibility(View.GONE);
            binding.reason.setVisibility(View.GONE);
            binding.estimatedCheckInDate.setVisibility(View.GONE);

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
                    key.setEstimatedCheckInDate(binding.estimatedCheckInDate.getText().toString());
                    key.setCheckedInDate(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
                    key.setCheckInReason(reasonValue);
                    key.setCheckedOutDate(null);
                    key.setLastCheckedInUser(user.getFirstName() + " " + user.getLastName());
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
            } else if (key.getLastCheckedInUser().equalsIgnoreCase(user.getFirstName() + " " + user.getLastName())) {
                key.setCheckInReason("Check out");
                key.setCheckedInDate(null);
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
            }

            submit.setEnabled(false);
            return true;
        });

        binding.checkInOutToolbar.setNavigationOnClickListener(v -> dismiss());

        binding.estimatedCheckInDate.setOnClickListener(v -> {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            DatePickerDialog picker = new DatePickerDialog(requireContext(),
                    (DatePickerDialog.OnDateSetListener) (view, year1, monthOfYear, dayOfMonth) -> binding.estimatedCheckInDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1), year, month, day);
            picker.getDatePicker().setMinDate(System.currentTimeMillis());
            picker.show();
        });

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