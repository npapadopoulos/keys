package com.property.keys.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.property.keys.R;
import com.property.keys.databinding.FragmentAddPropertyBinding;
import com.property.keys.entities.Property;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.PropertyUtils;
import com.property.keys.utils.StorageUtils;
import com.property.keys.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Consumer;

import timber.log.Timber;

import static com.property.keys.utils.ImageUtils.REQUEST_IMAGE;

@RequiresApi(api = Build.VERSION_CODES.R)
public class AddProperty extends DialogFragment {

    private static final String TAG = AddProperty.class.getSimpleName();

    private FragmentAddPropertyBinding binding;
    private String generatedPropertyId = UUID.randomUUID().toString();

    private MenuItem submit;

    public static AddProperty newInstance() {
        return new AddProperty();
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.reset(binding.name, binding.address);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddPropertyBinding.inflate(getLayoutInflater());
        submit = binding.addPropertyToolbar.getMenu().getItem(0);

        addOnButtonsClickListeners();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireDialog().getWindow().setWindowAnimations(R.style.ToolbarDialogAnimation);
    }

    private void addOnButtonsClickListeners() {
        binding.addPropertyToolbar.getMenu().getItem(0).setOnMenuItemClickListener(view -> {

            InputMethodManager in = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);

            submit.setEnabled(false);
            createProperty();
            return true;
        });

        binding.addPropertyToolbar.setNavigationOnClickListener(v -> dismiss());
        binding.addImage.setOnClickListener(this::updateImage);
        binding.propertyImage.setOnClickListener(this::updateImage);
    }

    private void createProperty() {
        if (!Utils.validateText(binding.name, 20) | !Utils.validateText(binding.address)) {
            submit.setEnabled(true);
            return;
        }

        File file = (File) ImageUtils.loadImage(requireContext(), generatedPropertyId, binding.propertyImage);
        if (file == null || !file.exists()) {
            Snackbar snackbar = Snackbar.make(binding.addPropertyDialog, "Property Image is not added.", Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red_600));
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_900));
            snackbar.show();

            submit.setEnabled(true);
            return;
        }

        String nameValue = binding.name.getEditText().getText().toString();
        String addressValue = binding.address.getEditText().getText().toString();

        Chip chip = binding.types.findViewById(binding.types.getCheckedChipId());

        Property property = Property.builder()
                .id(generatedPropertyId)
                .type(chip.getText().toString())
                .name(nameValue)
                .address(addressValue)
                .build();

        Consumer<Intent> startActivity = intent -> {
            try {
                byte[] data = Files.readAllBytes(Paths.get(file.getPath()).toAbsolutePath());
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                StorageUtils.uploadImage(generatedPropertyId, "property", image);
            } catch (IOException e) {
                Timber.tag(TAG).e(e, "Couldn't upload property image for " + generatedPropertyId + " to remote storage.");
            }
            startActivity(intent);
            dismiss();
        };

        Consumer<Task<Void>> onCreationFailed = (Task<Void> task) -> {
            Timber.tag(TAG).i(task.getException(), "Property creation for " + nameValue + " failed.");
            Snackbar.make(binding.addPropertyDialog, "Property creation for " + nameValue + " failed.", Snackbar.LENGTH_SHORT).show();

            submit.setEnabled(true);
        };

        PropertyUtils.create(getActivity(), property, startActivity, onCreationFailed);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), data.getParcelableExtra("path"));
                    ImageUtils.clearCache(requireContext());
                    ImageUtils.saveImage(requireContext(), image, generatedPropertyId);
                    ImageUtils.loadImage(requireContext(), generatedPropertyId, binding.propertyImage);
                } catch (IOException e) {
                    Timber.tag(TAG).e(e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateImage(View v) {
        ImageUtils.updateImage(getActivity(), this, generatedPropertyId, true);
    }
}