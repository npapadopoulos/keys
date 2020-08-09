package com.property.keys.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.material.textfield.TextInputLayout;
import com.property.keys.entities.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    private final static String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private final static String PASSWORD_PATTERN = "^" +
            "(?=.*[a-zA-Z])" +
            "(?=.*[@#$%^&+=])" +
            ".{4,}" +
            "$";

    private Utils() {
        throw new AssertionError("No instance for you!");
    }

    public static Boolean validateFirstName(TextInputLayout firstName) {
        String value = firstName.getEditText().getText().toString();
        if (value.isEmpty()) {
            Log.v(TAG, "First Name cannot be empty: '" + value + "'");
            firstName.setError("cannot be empty");
            return false;
        } else {
            reset(firstName);
            return true;
        }
    }

    public static Boolean validateLastName(TextInputLayout lastName) {
        String value = lastName.getEditText().getText().toString();
        if (value.isEmpty()) {
            Log.v(TAG, "Last Name cannot be empty: '" + value + "'");
            lastName.setError("cannot be empty");
            return false;
        } else {
            reset(lastName);
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
        } else if (value.length() != 8) {
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

    public static User getUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        return User.builder()
                .id(sharedPreferences.getString("id", ""))
                .firstName(sharedPreferences.getString("firstName", ""))
                .lastName(sharedPreferences.getString("lastName", ""))
                .email(sharedPreferences.getString("email", ""))
                .phoneNumber(sharedPreferences.getString("phoneNumber", ""))
                .build();
    }

    public static void saveUser(User user, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", user.getId());
        editor.putString("firstName", user.getFirstName());
        editor.putString("lastName", user.getLastName());
        editor.putString("email", user.getEmail());
        editor.putString("phoneNumber", user.getPhoneNumber());
        editor.apply();
    }
}
