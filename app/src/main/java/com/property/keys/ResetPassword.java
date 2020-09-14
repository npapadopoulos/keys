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

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.property.keys.databinding.ActivityResetPasswordBinding;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.util.function.Consumer;

import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.R)
public class ResetPassword extends AppCompatActivity {
    private static final String TAG = ResetPassword.class.getSimpleName();

    private ActivityResetPasswordBinding binding;

    @Override
    protected void onStart() {
        super.onStart();
        binding.progressBar.setVisibility(View.GONE);
        binding.submit.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.submit.setOnClickListener(v -> {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(v.getWindowToken(), 0);

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.submit.setEnabled(false);

            if (!Utils.validatePassword(binding.password, binding.confirmPassword)) {
                binding.progressBar.setVisibility(View.GONE);
                binding.submit.setEnabled(true);
                return;
            }

            String phoneNumber = getIntent().getStringExtra("phoneNumber");
            String password = binding.password.getEditText().getText().toString();
            PhoneAuthCredential credentialByPhone = getIntent().getParcelableExtra("credentialByPhone");

            Consumer<Intent> startDashboardActivity = intent -> {
                startActivity(intent);
                finish();
            };

            Consumer<Task<AuthResult>> onResetFailed = (Task<AuthResult> task) -> {
                // If sign in fails, display a message to the user.
                Timber.tag(TAG).i(task.getException(), "Password reset for " + phoneNumber + " failed.");
                Snackbar.make(binding.main, "Password reset for " + phoneNumber + " failed.", Snackbar.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                binding.submit.setEnabled(true);
            };

            UserUtils.resetPassword(getApplicationContext(), phoneNumber, password, credentialByPhone, startDashboardActivity, onResetFailed);
        });
    }
}