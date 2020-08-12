package com.property.keys.utils;

import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    private final static AtomicInteger CHANGE_COUNTER = new AtomicInteger(0);

    private final static String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private final static String PASSWORD_PATTERN = "^" +
            "(?=.*[a-zA-Z])" +
            "(?=.*[@#$%^&+=])" +
            ".{4,}" +
            "$";

    private Utils() {
        throw new AssertionError("No instance for you!");
    }

    public static Boolean validateText(TextInputLayout textInputLayout) {
        String value = textInputLayout.getEditText().getText().toString();
        if (value.isEmpty()) {
            Log.v(TAG, "Cannot be empty: '" + value + "'");
            textInputLayout.setError("cannot be empty");
            return false;
        } else {
            reset(textInputLayout);
            return true;
        }
    }

    public static Boolean validateEmail(TextInputLayout email) {
        String value = email.getEditText().getText().toString();
        if (value.isEmpty()) {
            Log.v(TAG, "Email cannot be empty: '" + value + "'");
            email.setError("cannot be empty");
            return false;
        } else if (!value.matches(EMAIL_PATTERN)) {
            Log.v(TAG, "Email is not valid: '" + value + "'");
            email.setError("is not valid");
            return false;
        } else {
            reset(email);
            return true;
        }
    }

    public static Boolean validatePhoneNumber(TextInputLayout phoneNumber) {
        String value = phoneNumber.getEditText().getText().toString();
        if (value.isEmpty()) {
            Log.v(TAG, "Phone Number cannot be empty: '" + value + "'");
            phoneNumber.setError("cannot be empty");
            return false;
        } else if (value.length() != 8 && !(value.length() == 12 && value.startsWith("+357")) && !(value.length() == 13 && value.startsWith("00357"))) {
            Log.v(TAG, "Phone Number is not valid: '" + value + "'");
            phoneNumber.setError("is not valid");
            return false;
        } else {
            reset(phoneNumber);
            return true;
        }
    }


    public static Boolean validatePassword(TextInputLayout... passwords) {
        TextInputLayout password = passwords[0];
        TextInputLayout confirmPassword = null;
        if (passwords.length == 2) {
            confirmPassword = passwords[1];
        }
        String value = password.getEditText().getText().toString();
        if (value.isEmpty()) {
            password.setError("cannot be empty");
            return false;
        } else if (!value.matches(PASSWORD_PATTERN)) {
            password.setError("is too weak");
            return false;
        }

        if (confirmPassword != null && !value.equals(confirmPassword.getEditText().getText().toString())) {
            password.setError("does not match");
            confirmPassword.setError("does not match");
            return false;
        }

        reset(password, confirmPassword);
        return true;
    }

    public static void reset(TextInputLayout... views) {
        reset(false, views);
    }

    public static void reset(boolean clearText, TextInputLayout... views) {
        if (views != null) {
            Arrays.stream(views).forEach(view -> {
                if (view != null) {
                    view.setError(null);
                    view.setErrorEnabled(false);

                    if (clearText) {
                        view.getEditText().setText("");
                    }
                }
            });
        }
    }

    public static String addCountryCodeIfMissing(String phoneNumber) {
        //Cyprus case
        if ((phoneNumber.startsWith("+357") && phoneNumber.length() == 12)
                || (phoneNumber.startsWith("00357") && phoneNumber.length() == 13)) {
            return phoneNumber;
        } else if (phoneNumber.length() == 8) {
            return "+357" + phoneNumber;
        } else {
            return phoneNumber;
        }
    }

    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            return hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Couldn't hash the password. Plain text will be used instead.", e);
        }

        return password;
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static void addOnTextChangeListener(MaterialButton button, TextInputEditText textInputEditText, String original) {
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals(original) && !original.equals("")) {
                    button.setEnabled(false);
                    CHANGE_COUNTER.decrementAndGet();
                } else {
                    button.setEnabled(true);
                    CHANGE_COUNTER.incrementAndGet();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}
