package com.property.keys;

import android.app.Activity;
import android.content.Intent;
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
        getSupportActionBar().setTitle("Back");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        binding.setFavourite.setOnFavoriteChangeListener((buttonView, favorite) -> PropertyUtils.update(this, property, favorite));

        property = getIntent().getParcelableExtra("property");
        binding.name.setText(property.getName());
        binding.address.setText(property.getAddress());
        binding.setFavourite.setFavorite(property.getFavouredBy().containsKey(UserUtils.getUser(getApplicationContext()).getId()));

        ImageUtils.syncAndloadImages(this, property.getId(), binding.propertyImage);
        binding.addImage.setOnClickListener(this::updateImage);
        binding.propertyImage.setOnClickListener(this::updateImage);

//        if (property != null && !CollectionUtils.isEmpty(property.getKeys())) {
//            try {
//                Key key = property.getKeys().get(0);
//                QRCodeUtils.generate(key, 350, 350, key.getId());
//            } catch (WriterException e) {
//                System.out.println("Could not generate QR Code, WriterException :: " + e.getMessage());
//            } catch (IOException e) {
//                System.out.println("Could not generate QR Code, IOException :: " + e.getMessage());
//            }
//        }
    }
    //TODO add image selection logic

    private void updateImage(View v) {
        ImageUtils.updateImage(this, property.getId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getParcelableExtra("path"));
                    ImageUtils.clearCache(getApplicationContext());
                    ImageUtils.saveImage(getApplicationContext(), image, property.getId());
                    StorageUtils.uploadImage(property.getId(), image);
                    ImageUtils.loadImages(this, property.getId(), binding.propertyImage);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}