package com.property.keys;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.property.keys.databinding.ActivitySignUpBinding;
import com.property.keys.entities.User;
import com.property.keys.utils.LoginUtils;
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(v.getWindowToken(), 0);

                binding.progressBar.setVisibility(View.VISIBLE);
                binding.signUp.setEnabled(false);

                registerUser(v);
            }
        });

        binding.signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signIn = new Intent(getApplicationContext(), SignIn.class);
                Utils.reset(binding.firstName, binding.lastName, binding.email, binding.phoneNumber, binding.password, binding.confirmPassword);
                startActivity(signIn);
                finish();
            }
        });
    }

    public void registerUser(View view) {
        if (!Utils.validateFirstName(binding.firstName) | !Utils.validateLastName(binding.lastName) | !Utils.validateEmail(binding.email) | !Utils.validatePhoneNumber(binding.phoneNumber) | !Utils.validatePassword(binding.password, binding.confirmPassword)) {
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
                .id(UUID.nameUUIDFromBytes(emailValue.getBytes()).toString())
                .firstName(firstNameValue)
                .lastName(lastNameValue)
                .email(emailValue)
                .phoneNumber(Utils.addCountryCodeIfMissing(phoneNumberValue))
                .password(Utils.hash(passwordValue))
                .build();

        Consumer<Intent> startDashboardActivity = intent -> {
            startActivity(intent);
            finish();
        };

        Consumer<Task<AuthResult>> onCreationFailed = (Task<AuthResult> task) -> {
            // If sign in fails, display a message to the user.
            Log.i(TAG, "Account creation for " + emailValue + " failed.", task.getException());
            Toast.makeText(SignUp.this, "Account creation for " + emailValue + " failed.", Toast.LENGTH_LONG).show();

            binding.progressBar.setVisibility(View.GONE);
            binding.signUp.setEnabled(true);
        };

        LoginUtils.createUser(this, getApplicationContext(), user, startDashboardActivity, onCreationFailed);
    }
}