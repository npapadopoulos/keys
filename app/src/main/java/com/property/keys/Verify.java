package com.property.keys;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.property.keys.databinding.ActivityVerifyBinding;
import com.property.keys.utils.Utils;

import java.util.concurrent.TimeUnit;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Verify extends AppCompatActivity {
    private static final String TAG = Verify.class.getSimpleName();

    private String verificationCodeBySystem;
    private String phoneNumber;

    private ActivityVerifyBinding binding;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {

                binding.otp.setAnimationEnable(true);
                binding.otp.setText(code);

                //TODO how to wait so we can see the animation

                binding.progressBar.setVisibility(View.VISIBLE);
                binding.submit.setEnabled(false);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Snackbar.make(binding.main, e.getMessage(), Snackbar.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.submit.setEnabled(true);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        binding.progressBar.setVisibility(View.GONE);
        binding.submit.setEnabled(true);

        binding.otp.setText("");
        binding.otp.setError(null);//TODO are you sure it works this way for PinView, please do validate
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = ActivityVerifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.otp.setAnimationEnable(true);

        phoneNumber = Utils.addCountryCodeIfMissing(getIntent().getStringExtra("phoneNumber"));
        sendVerificationCodeToUser();

        binding.submit.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.submit.setEnabled(false);

            String code = binding.otp.getContext().toString();
            if (code.isEmpty() || code.length() < 6) {
                binding.otp.setError("Wrong OTP...");
                binding.otp.requestFocus();
                binding.progressBar.setVisibility(View.GONE);
                return;
            }
            verifyCode(code);
        });
    }

    private void sendVerificationCodeToUser() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                TaskExecutors.MAIN_THREAD,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credentialByPhone = PhoneAuthProvider.getCredential(verificationCodeBySystem, code);
        Intent resetPassword = new Intent(getApplicationContext(), ResetPassword.class);
        resetPassword.putExtra("credentialByPhone", credentialByPhone);
        resetPassword.putExtra("phoneNumber", phoneNumber);

        startActivity(resetPassword);
        finish();
    }
}