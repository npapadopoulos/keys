package com.property.keys.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.property.keys.R;
import com.property.keys.entities.Key;
import com.property.keys.entities.User;
import com.property.keys.fragments.KeyDetails;
import com.property.keys.utils.FileUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class KeyHolder extends RecyclerView.ViewHolder implements Holder {
    private final int DIALOG_REQUEST_CODE = 201;
    private final Activity activity;
    private final FragmentManager supportFragmentManager;

    private ImageView qrCodeImage;
    private TextView checkedInDetails, checkedInDate, location, purpose;
    private RelativeLayout keyBackground, keyForeground, keyDetails;
    private User user;

    public KeyHolder(@NonNull Activity activity, FragmentManager supportFragmentManager, @NonNull View itemView, User user) {
        super(itemView);

        qrCodeImage = itemView.findViewById(R.id.qrCodeImage);
        location = itemView.findViewById(R.id.location);
        purpose = itemView.findViewById(R.id.purpose);
        checkedInDate = itemView.findViewById(R.id.checkedInDate);
        checkedInDetails = itemView.findViewById(R.id.checkedInDetails);
        keyForeground = itemView.findViewById(R.id.keyForeground);
        keyBackground = itemView.findViewById(R.id.keyBackground);
        keyDetails = itemView.findViewById(R.id.keyDetails);

        this.supportFragmentManager = supportFragmentManager;
        this.activity = activity;
        this.user = user;
    }

    public void bind(@NonNull Context context, @NonNull Key key, String propertyName) {
        location.setText(key.getLocation());
        purpose.setText(key.getPurpose());
        if (key.getCheckedInDate() != null) {

            String userFullName = key.getLastCheckedInUser();
            if (userFullName.equals(user.getFirstName() + " " + user.getLastName())) {
                userFullName = "You took";
            } else {
                userFullName = userFullName + " took";
            }

            checkedInDate.setText(key.getCheckedInDate());
            checkedInDetails.setText(userFullName + " the key for " + key.getCheckInReason() + ".");
            keyDetails.setBackground(ContextCompat.getDrawable(context, R.drawable.key_busy_background));
        } else {
            checkedInDate.setText("");
            checkedInDetails.setText("Key is available.");
            keyDetails.setBackground(ContextCompat.getDrawable(context, R.drawable.key_available_background));
        }
        FileUtils.syncAndloadImagesKey(context, key.getId(), qrCodeImage, (image) -> itemView.setOnLongClickListener(v -> {
            KeyDetails keyDetails = KeyDetails.newInstance(context, key, image.getPath(), propertyName);
            keyDetails.show(supportFragmentManager, "keyDetails");
            return true;
        }));
    }

    @Override
    public RelativeLayout getBackground() {
        return keyBackground;
    }

    @Override
    public RelativeLayout getRestoreBackground() {
        return null;
    }

    @Override
    public RelativeLayout getForeground() {
        return keyForeground;
    }
}
