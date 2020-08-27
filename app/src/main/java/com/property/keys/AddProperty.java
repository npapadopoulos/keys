package com.property.keys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import static com.property.keys.utils.ImageUtils.REQUEST_IMAGE;

@RequiresApi(api = Build.VERSION_CODES.R)
public class AddProperty extends AppCompatActivity {

    private static final String TAG = AddProperty.class.getSimpleName();

    private ActivityAddPropertyBinding binding;
    private String pregenaratedPropertyId = UUID.randomUUID().toString();

    @Override
    protected void onStart() {
        super.onStart();
        Utils.reset(binding.name, binding.address);
        binding.progressBar.setVisibility(View.GONE);
        binding.submit.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        binding = ActivityAddPropertyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateStatusBarOptions();

        MaterialToolbar propertyToolbar = binding.addPropertyToolbar;
        setSupportActionBar(propertyToolbar);
        getSupportActionBar().setTitle("Back");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        propertyToolbar.setNavigationOnClickListener(view -> finish());

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

    private void updateStatusBarOptions() {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorGrey));
    }

    private void createProperty() {
        if (!Utils.validateText(binding.name) | !Utils.validateText(binding.address)) {
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
            return;
        }

        File file = ImageUtils.loadImage(this, pregenaratedPropertyId, binding.propertyImage);
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

        HashMap<String, Object> favouredBy = new HashMap<>();
        favouredBy.put(UserUtils.getLocalUser(this).getId(), true);
        Property property = Property.builder()
                .id(pregenaratedPropertyId)
                .name(nameValue)
                .address(addressValue)
                .favouredBy(favouredBy)
                .build();

        Consumer<Intent> startActivity = intent -> {
            try {
                byte[] data = Files.readAllBytes(Paths.get(file.getPath()).toAbsolutePath());
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                StorageUtils.uploadImage(pregenaratedPropertyId, "property", image);
            } catch (IOException e) {
                Log.e(TAG, "Couldn't upload property image for " + pregenaratedPropertyId + " to remote storage.", e);
            }
            startActivity(intent);
            finish();
        };

        Consumer<Task<Void>> onCreationFailed = (Task<Void> task) -> {
            Log.i(TAG, "Property creation for " + nameValue + " failed.", task.getException());
            Snackbar.make(binding.main, "Property creation for " + nameValue + " failed.", Snackbar.LENGTH_SHORT).show();

            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
        };

        PropertyUtils.create(this, getApplicationContext(), property, startActivity, onCreationFailed);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getParcelableExtra("path"));
                    ImageUtils.clearCache(getApplicationContext());
                    ImageUtils.saveImage(getApplicationContext(), image, pregenaratedPropertyId);
                    ImageUtils.loadImage(this, pregenaratedPropertyId, binding.propertyImage);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateImage(View v) {
        ImageUtils.updateImage(this, null, pregenaratedPropertyId);
    }
}