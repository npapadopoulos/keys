package com.property.keys;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.property.keys.databinding.ActivityAddPropertyBinding;
import com.property.keys.entities.Property;
import com.property.keys.utils.PropertyUtils;
import com.property.keys.utils.Utils;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

@RequiresApi(api = Build.VERSION_CODES.R)
public class AddProperty extends AppCompatActivity {

    private static final String TAG = AddProperty.class.getSimpleName();

    private ActivityAddPropertyBinding binding;

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = ActivityAddPropertyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.submit.setOnClickListener(view -> {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(view.getWindowToken(), 0);

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.submit.setEnabled(false);

            createProperty(view);
        });
    }

    private void createProperty(View view) {
        if (!Utils.validateText(binding.name) | !Utils.validateText(binding.address)) {
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
            return;
        }

        String nameValue = binding.name.getEditText().getText().toString();
        String addressValue = binding.address.getEditText().getText().toString();

        Property property = Property.builder()
                .id(UUID.randomUUID().toString())
                .name(nameValue)
                .address(addressValue)
                .favouredBy(new HashMap<>())
                .build();

        Consumer<Intent> startActivity = intent -> {
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
}