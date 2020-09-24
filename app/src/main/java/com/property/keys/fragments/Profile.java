package com.property.keys.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.databinding.FragmentProfileBinding;
import com.property.keys.entities.User;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.StorageUtils;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.io.IOException;
import java.util.function.Consumer;

import timber.log.Timber;

import static com.property.keys.utils.ImageUtils.REQUEST_IMAGE;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Profile extends Fragment {
    private static final String TAG = Profile.class.getSimpleName();
    private User user;

    private FragmentProfileBinding binding;

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;
    private Container container;

    public Profile(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_profile, true);
        navigation.getCheckedItem().setChecked(false);
        toolbar.setTitle("Profile");

        this.container = (Container) getActivity();

        user = UserUtils.getLocalUser(getActivity().getApplicationContext());

        TextInputEditText firstNameEditText = (TextInputEditText) binding.firstName.getEditText();
        TextInputEditText lastNameEditText = (TextInputEditText) binding.lastName.getEditText();
        TextInputEditText emailEditText = (TextInputEditText) binding.email.getEditText();
        TextInputEditText phoneNumberEditText = (TextInputEditText) binding.phoneNumber.getEditText();

        firstNameEditText.setText(user.getFirstName());
        lastNameEditText.setText(user.getLastName());
        emailEditText.setText(user.getEmail());
        phoneNumberEditText.setText(user.getPhoneNumber());


        binding.update.setOnClickListener(view -> {
//            Utils.showProgressBar(getActivity());

            binding.update.setEnabled(false);
            if (!Utils.validateText(binding.firstName) | !Utils.validateText(binding.lastName) | !Utils.validateEmail(binding.email) | !Utils.validatePhoneNumber(binding.phoneNumber)) {
                binding.update.setEnabled(true);
//                Utils.hideProgressBar(getActivity());
                return;
            }

            if (user.getFirstName().equals(firstNameEditText.getText().toString())
                    && user.getLastName().equals(lastNameEditText.getText().toString())
                    && user.getPhoneNumber().equals(phoneNumberEditText.getText().toString())
                    && user.getEmail().equalsIgnoreCase(emailEditText.getText().toString())) {
                binding.update.setEnabled(true);
//                Utils.hideProgressBar(getActivity());
                Snackbar.make(this.container.getPlaceSnackBar(), "Nothing to update.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            user.setFirstName(firstNameEditText.getText().toString());
            user.setLastName(lastNameEditText.getText().toString());
            user.setEmail(emailEditText.getText().toString());
            user.setPhoneNumber(phoneNumberEditText.getText().toString());

            Consumer<Exception> onUpdateFailed = (Exception exception) -> {
                // If sign in fails, display a message to the user.
                Timber.tag(TAG).i(exception, "Account update for " + user.getId() + " failed.");
                Snackbar.make(this.container.getPlaceSnackBar(), "Account update for " + user.getId() + " failed. Try again later.", Snackbar.LENGTH_SHORT).show();
                binding.update.setEnabled(true);
//                Utils.hideProgressBar(getActivity());
            };

            Consumer<Task<Void>> onUpdateSucceeded = (Task<Void> task) -> {
                Timber.tag(TAG).i("Account update for " + user.getId() + " succeeded.");
                Snackbar.make(this.container.getPlaceSnackBar(), "Account details updated.", Snackbar.LENGTH_SHORT).show();
                binding.update.setEnabled(true);
//                Utils.hideProgressBar(getActivity());
                UserUtils.saveUser(user, getActivity().getApplicationContext());
            };
            //updates only basic fields, firstName, lastName, email and phoneNumber
            UserUtils.updateBasics(user, onUpdateFailed, onUpdateSucceeded);
        });

        ImageUtils.syncAndLoadImagesProfile(getActivity(), user, binding.profileImage);
        binding.addImage.setOnClickListener(this::updateImage);
        binding.profileImage.setOnClickListener(this::updateImage);

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getParcelableExtra("path"));
                    ImageUtils.clearCache(getContext());
                    ImageUtils.saveImage(getContext(), image, user.getId());
                    StorageUtils.uploadImage(user.getId(), "profile", image);
                    ImageUtils.loadImage(getActivity(), user.getId(), binding.profileImage);

                    Intent intent = new Intent();
                    intent.setAction("com.property.keys.PROFILE_IMAGE_UPDATED");
                    getActivity().sendBroadcast(intent);
                } catch (IOException e) {
                    Timber.tag(TAG).e(e, e.getMessage());
                }
            }
        }
    }

    private void updateImage(View v) {
        ImageUtils.updateImage(this.getActivity(), this, user.getId());
    }
}