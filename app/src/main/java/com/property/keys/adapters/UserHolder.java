package com.property.keys.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.entities.Role;
import com.property.keys.entities.User;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.UserUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class UserHolder extends RecyclerView.ViewHolder implements Holder {

    private static final DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");

    private CircularImageView userImage;
    private TextView firstName, lastName;
    private MaterialCheckBox adminCheck;
    private RelativeLayout userForeground, userBackground;
    private String userId;

    public UserHolder(@NonNull Activity activity, @NonNull View itemView) {
        super(itemView);

        userImage = itemView.findViewById(R.id.userImage);
        firstName = itemView.findViewById(R.id.firstName);
        lastName = itemView.findViewById(R.id.lastName);
        userForeground = itemView.findViewById(R.id.userForeground);
        userBackground = itemView.findViewById(R.id.userBackground);

        addOnUserAdminCheckClickListener(activity, itemView);
        //TODO add UserDetails and listen to on click. This is needed for Admin to check user's history
//        addOnUserClickListener(activity, itemView);
    }

    private void addOnUserAdminCheckClickListener(@NonNull Activity activity, @NonNull View itemView) {
        adminCheck = itemView.findViewById(R.id.adminCheck);
        adminCheck.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(itemView.getContext())
                        .setMessage("Are you sure you want to" + (adminCheck.isChecked() ? (" add Admin privileges to ") : " remove Admin privileges from ") + firstName.getText() + " " + lastName.getText() + " ?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            UserUtils.updateRole(userId, adminCheck.isChecked());
                            Snackbar.make(((Container) activity).getPlaceSnackBar(), "User role updated.", Snackbar.LENGTH_LONG).show();
                        })
                        .setOnKeyListener((d, keyCode, event) -> {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                adminCheck.toggle();
                                d.dismiss();
                                return true;
                            }
                            return false;
                        })
                        .setBackground(ContextCompat.getDrawable(activity, R.drawable.white_card_background))
                        .setNegativeButton("No", (dialog, which) -> {
                            adminCheck.toggle();
                            dialog.dismiss();
                        })
                        .setCancelable(false)
                        .create().show());
    }

    private void addOnUserClickListener(@NonNull Activity activity, @NonNull View itemView) {
//        itemView.setOnClickListener(view -> {
//            if (userId == null) {
//                Snackbar.make(((Container) activity).getPlaceSnackBar(), "The current property has been deleted.", Snackbar.LENGTH_LONG).show();
//            }
//            users.child(userId)
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            Property property = snapshot.getValue(Property.class);
//                            if (property == null) {
//                                Snackbar.make(((Container) activity).getPlaceSnackBar(), "The current property has been deleted.", Snackbar.LENGTH_LONG).show();
//                            } else if (property.isDeleted()) {
//                                Snackbar.make(((Container) activity).getPlaceSnackBar(), "The current property has moved to trash.", Snackbar.LENGTH_LONG).show();
//                            } else {
//                                Intent propertyDetails = new Intent(itemView.getContext(), UserDetails.class);
//                                propertyDetails.putExtra("property", property);
//                                view.getContext().startActivity(propertyDetails);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//        });
    }


    public void bind(@NonNull Context context, @NonNull User user) {
        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        adminCheck.setChecked(user.getRole() == Role.ADMIN);
        this.userId = user.getId();
        ImageUtils.syncAndLoadImagesProfile(context, user.getId(), user.getFirstName(), user.getLastName(), userImage);
    }

    @Override
    public RelativeLayout getBackground() {
        return userBackground;
    }

    @Override
    public RelativeLayout getRestoreBackground() {
        return null;
    }

    @Override
    public RelativeLayout getForeground() {
        return userForeground;
    }
}
