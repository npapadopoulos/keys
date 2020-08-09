package com.property.keys;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.property.keys.databinding.ActivitySigninBinding;
import com.property.keys.utils.LoginUtils;
import com.property.keys.utils.Utils;

import java.util.function.Consumer;


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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.submit.setOnClickListener(v -> {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(v.getWindowToken(), 0);

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.submit.setEnabled(false);

            loginUser(v);
        });

        binding.signUp.setOnClickListener(view -> {
            Utils.reset(true, binding.email, binding.password);

            Pair[] pairs = new Pair[4];
            pairs[0] = new Pair<View, String>(binding.email, "email");
            pairs[1] = new Pair<View, String>(binding.password, "password");
            pairs[2] = new Pair<View, String>(binding.submit, "submit");
            pairs[3] = new Pair<View, String>(binding.signUp, "signUp");

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SignIn.this, pairs);
            startActivity(new Intent(SignIn.this, SignUp.class), options.toBundle());
        });

        binding.forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(SignIn.this, ForgotPassword.class));
        });
    }


    public void loginUser(View view) {
        if (!Utils.validateEmail(binding.email) | !Utils.validatePassword(binding.password)) {
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
        } else {
            isUser();
        }
    }

    private void isUser() {
        String email = binding.email.getEditText().getText().toString().trim();
        String password = Utils.hash(binding.password.getEditText().getText().toString());

        Consumer<Intent> startDashboardActivity = intent -> {
            startActivity(intent);
            finish();
        };

        Consumer<Exception> onFailed = (Exception e) -> {
            Log.e(TAG, "Failed to start Dashboard activity.", e);
            Toast.makeText(SignIn.this, "Authentication failed. Try again later.", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
        };

        Consumer<Task<AuthResult>> onAuthenticationFailed = (Task<AuthResult> task) -> {
            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "No such User exists: ", task.getException());
                binding.email.setError("No such User exists");
                binding.email.requestFocus();
            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                Log.w(TAG, "Wrong Password: ", task.getException());
                binding.password.setError("Wrong Password");
                binding.password.requestFocus();
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "Authentication failed: ", task.getException());
                Toast.makeText(SignIn.this, "Authentication failed. Try again later", Toast.LENGTH_SHORT).show();
            }
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
        };
        LoginUtils.authenticate(this, getApplicationContext(), email, password, startDashboardActivity, onAuthenticationFailed, onFailed);
    }
}