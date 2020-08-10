package com.property.keys;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.property.keys.databinding.ActivityPropertyDetailsBinding;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyDetails extends AppCompatActivity {
    private static final String TAG = Container.class.getSimpleName();

    private ActivityPropertyDetailsBinding binding;
    private String propertyId;

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
        propertyId = getIntent().getParcelableExtra("propertyId");

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
}