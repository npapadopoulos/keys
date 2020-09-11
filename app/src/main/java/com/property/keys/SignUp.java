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
import com.google.firebase.auth.AuthResult;
import com.property.keys.databinding.ActivitySignUpBinding;
import com.property.keys.entities.User;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.util.UUID;
import java.util.function.Consumer;

@RequiresApi(api = Build.VERSION_CODES.R)
public class SignUp extends AppCompatActivity {

    private static final String TAG = SignUp.class.getSimpleName();

    private ActivitySignUpBinding binding;

    @Override
    protected void onStart() {
        super.onStart();
        Utils.reset(binding.email, binding.password, binding.firstName, binding.lastName, binding.phoneNumber);
        binding.progressBar.setVisibility(View.GONE);
        binding.signUp.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signUp.setOnClickListener(view -> {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(view.getWindowToken(), 0);

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.signUp.setEnabled(false);

            registerUser(view);
        });

        binding.signIn.setOnClickListener(view -> {
            Intent signIn = new Intent(getApplicationContext(), SignIn.class);
            Utils.reset(binding.firstName, binding.lastName, binding.email, binding.phoneNumber, binding.password, binding.confirmPassword);
            startActivity(signIn);
            finish();
        });
    }

    public void registerUser(View view) {
        if (!Utils.validateText(binding.firstName) | !Utils.validateText(binding.lastName) | !Utils.validateEmail(binding.email) | !Utils.validatePhoneNumber(binding.phoneNumber) | !Utils.validatePassword(binding.password, binding.confirmPassword)) {
            binding.progressBar.setVisibility(View.GONE);
            binding.signUp.setEnabled(true);
            return;
        }

        String emailValue = binding.email.getEditText().getText().toString();
        String passwordValue = binding.password.getEditText().getText().toString();

        String firstNameValue = binding.firstName.getEditText().getText().toString();
        String lastNameValue = binding.lastName.getEditText().getText().toString();
        String phoneNumberValue = binding.phoneNumber.getEditText().getText().toString();

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .firstName(firstNameValue)
                .lastName(lastNameValue)
                .email(emailValue.toLowerCase().trim())
                .phoneNumber(Utils.addCountryCodeIfMissing(phoneNumberValue))
                .password(passwordValue)
                .remember(false)
                .build();

        Consumer<Intent> startActivity = intent -> {
            startActivity(intent);
            finish();
        };

        Consumer<Task<AuthResult>> onCreationFailed = (Task<AuthResult> task) -> {
            Log.i(TAG, "Account creation for " + emailValue + " failed.", task.getException());
            Snackbar.make(binding.main, "Account creation for " + emailValue + " failed.", Snackbar.LENGTH_SHORT).show();

            binding.progressBar.setVisibility(View.GONE);
            binding.signUp.setEnabled(true);
        };

        UserUtils.create(this, getApplicationContext(), user, startActivity, onCreationFailed);
    }
}