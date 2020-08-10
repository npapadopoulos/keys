package com.property.keys;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.property.keys.databinding.ActivityForgotPasswordBinding;
import com.property.keys.utils.Utils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class ForgotPassword extends AppCompatActivity {
    private static final String TAG = ForgotPassword.class.getSimpleName();

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onStart() {
        super.onStart();
        binding.progressBar.setVisibility(View.GONE);
        binding.submit.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.submit.setOnClickListener(v -> {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(v.getWindowToken(), 0);

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.submit.setEnabled(false);

            if (!Utils.validatePhoneNumber(binding.phoneNumber)) {
                binding.progressBar.setVisibility(View.GONE);
                binding.submit.setEnabled(true);
                return;
            }
            Intent verify = new Intent(getApplicationContext(), Verify.class);
            verify.putExtra("phoneNumber", binding.phoneNumber.getEditText().getText().toString());
            startActivity(verify);
        });
    }
}