package com.property.keys;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.property.keys.databinding.ActivitySigninBinding;
import com.property.keys.entities.User;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


@RequiresApi(api = Build.VERSION_CODES.R)
public class SignIn extends AppCompatActivity {
    private static final String TAG = SignIn.class.getSimpleName();

    private ActivitySigninBinding binding;

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.reset(true, binding.email, binding.password);
        binding.progressBar.setVisibility(View.GONE);
        binding.submit.setEnabled(true);
        initRememberCredentials();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkForPermissions(CAMERA, INTERNET, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, ACCESS_NETWORK_STATE);

        binding.password.setErrorIconOnClickListener(view -> {
            if (binding.password.isErrorEnabled()) {
                binding.password.setErrorEnabled(false);
                binding.password.requestFocus();
            }
        });

        addOnSubmitClickListener();
        addOnSignUpClickListener();
        addOnForgotPasswordClickListener();
    }

    private void checkForPermissions(String... permissions) {
        List<String> missingPermissions = new ArrayList<>();
        Stream.of(permissions).forEach(permission -> {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    missingPermissions.add(permission);
                }
            }
        });
        if (!CollectionUtils.isEmpty(missingPermissions)) {
            requestPermissions(missingPermissions.toArray(new String[0]), PackageManager.PERMISSION_GRANTED);
        }

    }

    private void addOnForgotPasswordClickListener() {
        binding.forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(SignIn.this, ForgotPassword.class));
            finish();
        });
    }

    private void addOnSignUpClickListener() {
        binding.signUp.setOnClickListener(view -> {
            Utils.reset(true, binding.email, binding.password);

            Pair[] pairs = new Pair[4];
            pairs[0] = new Pair<View, String>(binding.email, "email");
            pairs[1] = new Pair<View, String>(binding.password, "password");
            pairs[2] = new Pair<View, String>(binding.submit, "submit");
            pairs[3] = new Pair<View, String>(binding.signUp, "signUp");

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SignIn.this, pairs);
            startActivity(new Intent(SignIn.this, SignUp.class), options.toBundle());
            finish();
        });
    }

    private void addOnSubmitClickListener() {
        binding.submit.setOnClickListener(v -> {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(v.getWindowToken(), 0);

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.submit.setEnabled(false);

            loginUser(v);
        });
    }

    private void initRememberCredentials() {
        if (UserUtils.rememberCredentials(this)) {
            User localUser = UserUtils.getLocalUser(this);
            if (localUser != null) {
                String email = localUser.getEmail();
                String password = localUser.getPassword();
                Objects.requireNonNull(binding.email.getEditText()).setText(email);
                Objects.requireNonNull(binding.password.getEditText()).setText(password);
                binding.remember.setChecked(true);
            }
        }
    }


    public void loginUser(View view) {
        if (!Utils.validateEmail(binding.email) | !Utils.validatePassword(true, binding.password)) {
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
        } else {
            isUser();
        }
    }

    private void isUser() {
        String password = binding.password.getEditText().getText().toString();
        String email = binding.email.getEditText().getText().toString().trim();

        Consumer<Intent> startDashboardActivity = intent -> {
            startActivity(intent);
            finish();
        };

        Consumer<Exception> onFailed = (Exception e) -> {
            Log.e(TAG, "Failed to start Dashboard activity.", e);
            Snackbar.make(binding.main, "Authentication failed. Try again later.", Snackbar.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
        };

        Consumer<Task<AuthResult>> onAuthenticationFailed = (Task<AuthResult> task) -> {
            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "No such User exists: ", task.getException());
                binding.email.setError("No such User exists");
            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                Log.w(TAG, "Wrong Password: ", task.getException());
                binding.password.setError("Wrong Password");
            } else if (task.getException() instanceof FirebaseTooManyRequestsException) {
                Log.w(TAG, "Try again later: ", task.getException());
                Snackbar.make(binding.main, "Authentication failed. Max retries limit reached. Try again later.", Snackbar.LENGTH_SHORT).show();
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "Authentication failed: ", task.getException());
                Snackbar.make(binding.main, "Authentication failed. Try again later", Snackbar.LENGTH_SHORT).show();
            }
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
        };
        UserUtils.authenticate(this, getApplicationContext(), email, password, binding.remember.isChecked(),
                startDashboardActivity, onAuthenticationFailed, onFailed);
    }
}