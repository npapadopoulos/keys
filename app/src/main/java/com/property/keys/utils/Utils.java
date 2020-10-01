package com.property.keys.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.property.keys.R;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.helpers.RecyclerItemTouchHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import lombok.Getter;
import timber.log.Timber;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_BOOT_COMPLETED;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    @Getter
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        return validateText(textInputLayout, -1);
    }

    public static Boolean validateText(TextInputLayout textInputLayout, int max) {
        String value = textInputLayout.getEditText().getText().toString().trim();
        if (value.isEmpty()) {
            Timber.tag(TAG).v("Cannot be empty: '" + value + "'");
            textInputLayout.setError("cannot be empty");
            return false;
        } else if (value.length() > max && max != -1) {
            Timber.tag(TAG).v("Cannot exceed " + max + " characters length: '" + value + "'.");
            textInputLayout.setError("Cannot exceed " + max + " characters length.");
            return false;
        } else {
            reset(textInputLayout);
            return true;
        }
    }

    public static Boolean validateEmail(TextInputLayout email) {
        String value = email.getEditText().getText().toString().trim();
        if (value.isEmpty()) {
            Timber.tag(TAG).v("Email cannot be empty: '" + value + "'");
            email.setError("cannot be empty");
            return false;
        } else if (!value.matches(EMAIL_PATTERN)) {
            Timber.tag(TAG).v("Email is not valid: '" + value + "'");
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
            Timber.tag(TAG).v("Phone Number cannot be empty: '" + value + "'");
            phoneNumber.setError("cannot be empty");
            return false;
        } else if (value.length() != 8 && !(value.length() == 12 && value.startsWith("+357")) && !(value.length() == 13 && value.startsWith("00357"))) {
            Timber.tag(TAG).v("Phone Number is not valid: '" + value + "'");
            phoneNumber.setError("is not valid");
            return false;
        } else {
            reset(phoneNumber);
            return true;
        }
    }


    public static Boolean validatePassword(TextInputLayout... passwords) {
        return validatePassword(false, passwords);
    }

    public static Boolean validatePassword(boolean signin, TextInputLayout... passwords) {
        TextInputLayout password = passwords[0];
        TextInputLayout confirmPassword = null;
        if (passwords.length == 2) {
            confirmPassword = passwords[1];
        }
        String value = password.getEditText().getText().toString().trim();
        if (value.isEmpty()) {
            password.setError("cannot be empty");
            return false;
        } else if (!signin && !value.matches(PASSWORD_PATTERN)) {
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
            Timber.tag(TAG).e(e, "Couldn't hash the password. Plain text will be used instead.");
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
            view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.pink)));
        } else {
            view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.primaryColor)));
        }
    }

    public static void initSwipeProperty(RecyclerView view, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener listener) {
        initSwipeProperty(view, listener, false);
    }

    public static void initSwipeProperty(RecyclerView view, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener listener, boolean enableRestore) {
        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        int swipeDirs = ItemTouchHelper.LEFT;
        if (enableRestore) {
            swipeDirs = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        }

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, swipeDirs, listener, enableRestore);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(view);
    }

    public static void onClick(DialogInterface dialogInterface, int i) {
        //does nothing, used in some alert dialogs when user presses "No"
    }


    public static void checkForPermissions(Activity activity) {
        String[] requiredPermissions = new String[]{CAMERA, INTERNET,
                READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE,
                ACCESS_NETWORK_STATE, READ_PHONE_STATE,
                RECEIVE_BOOT_COMPLETED};
        List<String> missingPermissions = new ArrayList<>();
        Stream.of(requiredPermissions).forEach(permission -> {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    missingPermissions.add(permission);
                }
            }
        });
        if (!CollectionUtils.isEmpty(missingPermissions)) {
            activity.requestPermissions(missingPermissions.toArray(new String[0]), PackageManager.PERMISSION_GRANTED);
        }
    }
}
