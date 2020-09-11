package com.property.keys.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.property.keys.R;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.helpers.RecyclerItemTouchHelper;

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

    public static Boolean validateText(TextInputLayout textInputLayout) {
        String value = textInputLayout.getEditText().getText().toString().trim();
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
        String value = email.getEditText().getText().toString().trim();
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
        String value = phoneNumber.getEditText().getText().toString().trim();
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
        String value = password.getEditText().getText().toString().trim();
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

    public static void updateFavourite(Activity activity, FloatingActionButton view, Property property, User user) {
        boolean alreadyLiked = property.getFavouredBy().get(user.getId()) != null;
        PropertyUtils.like(activity, property, !alreadyLiked);
        updateFavourite(activity, view, !alreadyLiked);
    }

    public static void updateFavourite(Activity activity, FloatingActionButton view, boolean liked) {
        if (liked) {
            view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.accentColor)));
        } else {
            view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.primaryColor)));
        }
    }

    public static void initSwipeProperty(RecyclerView view, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener listener) {
        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, listener);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(view);
    }

    public static void onClick(DialogInterface dialogInterface, int i) {
        //does nothing, used in some alert dialogs when user presses "No"
    }

}
