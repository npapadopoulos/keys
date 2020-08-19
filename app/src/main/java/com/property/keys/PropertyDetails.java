package com.property.keys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.property.keys.databinding.ActivityPropertyDetailsBinding;
import com.property.keys.entities.Property;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.PropertyUtils;
import com.property.keys.utils.StorageUtils;
import com.property.keys.utils.UserUtils;

import java.io.IOException;

import static com.property.keys.utils.ImageUtils.REQUEST_IMAGE;

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
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
//        binding.setFavourite.setOnFavoriteChangeListener((buttonView, favorite) -> PropertyUtils.update(this, property, favorite));

    }

    private void updateFavourite(Context context, FloatingActionButton view, boolean isFavourite) {
        if (isFavourite) {
            view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPink)));
        } else {
            view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }
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
                    StorageUtils.uploadImage(property.getId(), "profile", image);
                    ImageUtils.loadImages(this, property.getId(), binding.propertyImage);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}