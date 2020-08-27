package com.property.keys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.property.keys.databinding.ActivityPropertyDetailsBinding;
import com.property.keys.entities.Property;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.PropertyUtils;
import com.property.keys.utils.StorageUtils;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.io.IOException;

import static com.property.keys.utils.ImageUtils.REQUEST_IMAGE;
import static com.property.keys.utils.Utils.updateFavourite;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyDetails extends AppCompatActivity {
    private static final String TAG = Container.class.getSimpleName();

    private ActivityPropertyDetailsBinding binding;
    private Property property;

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = ActivityPropertyDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MaterialToolbar propertyDetailsToolbar = binding.propertyDetailsToolbar;
        setSupportActionBar(propertyDetailsToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        propertyDetailsToolbar.setNavigationOnClickListener(view -> finish());

        property = getIntent().getParcelableExtra("property");
        binding.name.getEditText().setText(property.getName());
        binding.address.getEditText().setText(property.getAddress());

        binding.progressBar.setVisibility(View.GONE);
//        binding.setFavourite.setFavorite(property.getFavouredBy().containsKey(UserUtils.getLocalUser(getApplicationContext()).getId()));

        ImageUtils.syncAndloadImages(this, property.getId(), binding.propertyImage);
        binding.propertyImage.setOnClickListener(this::updateImage);
//        binding.addKey.setOnClickListener(this::addKey);

        Context context = this;
        binding.setFavourite.setOnClickListener(view -> {
            boolean isFavourite = property.getFavouredBy().containsKey(UserUtils.getLocalUser(getApplicationContext()).getId());
            PropertyUtils.update(this, property, !isFavourite);
            updateFavourite(context, binding.setFavourite, !isFavourite);
        });

        updateFavourite(context, binding.setFavourite, property.getFavouredBy().containsKey(UserUtils.getLocalUser(getApplicationContext()).getId()));

        binding.update.setOnClickListener(view -> {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(view.getWindowToken(), 0);

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.update.setEnabled(false);

            updateProperty();
        });

    }

    private void updateProperty() {
        if (!Utils.validateText(binding.name) | !Utils.validateText(binding.address)) {
            binding.progressBar.setVisibility(View.GONE);
            binding.update.setEnabled(true);
            return;
        }

        String nameValue = binding.name.getEditText().getText().toString();
        String addressValue = binding.address.getEditText().getText().toString();

        property.setAddress(addressValue);
        property.setName(nameValue);

//        Consumer<Intent> startActivity = intent -> {
//            try {
//
//                File file = ImageUtils.loadImage(this, property.getId(), binding.propertyImage);
//                byte[] data = Files.readAllBytes(Paths.get(file.getPath()).toAbsolutePath());
//                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
//                StorageUtils.uploadImage(property.getId(), "property", image);
//            } catch (IOException e) {
//                Log.e(TAG, "Couldn't upload property image for " + property.getId() + " to remote storage.", e);
//            }
//            startActivity(intent);
//            finish();
//        };
//
//        Consumer<Task<Void>> onCreationFailed = (Task<Void> task) -> {
//            Log.i(TAG, "Property update for " + nameValue + " failed.", task.getException());
//            Snackbar.make(binding.main, "Property update for " + nameValue + " failed.", Snackbar.LENGTH_SHORT).show();
//
//            binding.progressBar.setVisibility(View.GONE);
//            binding.update.setEnabled(true);
//        };

        PropertyUtils.update(this, property, property.getFavouredBy().containsKey(UserUtils.getLocalUser(getApplicationContext()).getId()));

        binding.progressBar.setVisibility(View.GONE);
        Snackbar.make(binding.main, "Property updated successfully.", Snackbar.LENGTH_SHORT).show();
        binding.update.setEnabled(true);
    }

    private void updateImage(View v) {
        ImageUtils.updateImage(this, property.getId());
    }

    private void addKey(View v) {
        PropertyUtils.generateKey(this, property);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getParcelableExtra("path"));
                    ImageUtils.clearCache(getApplicationContext());
                    ImageUtils.saveImage(getApplicationContext(), image, property.getId());
                    StorageUtils.uploadImage(property.getId(), "property", image);
                    ImageUtils.loadImage(this, property.getId(), binding.propertyImage);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}