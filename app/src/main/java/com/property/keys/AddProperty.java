package com.property.keys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.property.keys.databinding.ActivityAddPropertyBinding;
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
public class AddProperty extends AppCompatActivity {

    private static final String TAG = AddProperty.class.getSimpleName();

    private ActivityAddPropertyBinding binding;
    private String pregeneratedPropertyId = UUID.randomUUID().toString();

    @Override
    protected void onStart() {
        super.onStart();
        Utils.reset(binding.name, binding.address);
        binding.progressBar.setVisibility(View.GONE);
        binding.submit.setEnabled(true);
    }

    @Override
    protected void onResume() {
//        Utils.checkForPermissions(this);
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        binding = ActivityAddPropertyBinding.inflate(getLayoutInflater());

        initToolbar();
        setContentView(binding.getRoot());

        updateStatusBarOptions();
        addOnButtonsClickListeners();
    }

    private void addOnButtonsClickListeners() {
        binding.submit.setOnClickListener(view -> {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(view.getWindowToken(), 0);

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.submit.setEnabled(false);

            createProperty();
        });

        binding.addImage.setOnClickListener(this::updateImage);
        binding.propertyImage.setOnClickListener(this::updateImage);
    }

    private void initToolbar() {
        MaterialToolbar propertyToolbar = binding.addPropertyToolbar;
        setSupportActionBar(propertyToolbar);

        propertyToolbar.setNavigationOnClickListener(view -> finish());

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void updateStatusBarOptions() {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorGrey));
    }

    private void createProperty() {
        if (!Utils.validateText(binding.name, 20) | !Utils.validateText(binding.address)) {
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
            return;
        }

        File file = (File) ImageUtils.loadImage(this, pregeneratedPropertyId, binding.propertyImage);
        if (file == null || !file.exists()) {
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);

            Snackbar snackbar = Snackbar.make(binding.main, "Property Image is not added.", Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.red_600));
            snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.black_900));
            snackbar.show();
            return;
        }

        String nameValue = binding.name.getEditText().getText().toString();
        String addressValue = binding.address.getEditText().getText().toString();

        Property property = Property.builder()
                .id(pregeneratedPropertyId)
                .name(nameValue)
                .address(addressValue)
                .build();

        Consumer<Intent> startActivity = intent -> {
            try {
                byte[] data = Files.readAllBytes(Paths.get(file.getPath()).toAbsolutePath());
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                StorageUtils.uploadImage(pregeneratedPropertyId, "property", image);
            } catch (IOException e) {
                Timber.tag(TAG).e(e, "Couldn't upload property image for " + pregeneratedPropertyId + " to remote storage.");
            }
            startActivity(intent);
            finish();
        };

        Consumer<Task<Void>> onCreationFailed = (Task<Void> task) -> {
            Timber.tag(TAG).i(task.getException(), "Property creation for " + nameValue + " failed.");
            Snackbar.make(binding.main, "Property creation for " + nameValue + " failed.", Snackbar.LENGTH_SHORT).show();

            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
        };

        PropertyUtils.create(this, property, startActivity, onCreationFailed);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getParcelableExtra("path"));
                    ImageUtils.clearCache(getApplicationContext());
                    ImageUtils.saveImage(getApplicationContext(), image, pregeneratedPropertyId);
                    ImageUtils.loadImage(this, pregeneratedPropertyId, binding.propertyImage);
                } catch (IOException e) {
                    Timber.tag(TAG).e(e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateImage(View v) {
        ImageUtils.updateImage(this, null, pregeneratedPropertyId, true);
    }
}