package com.property.keys.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.property.keys.LoadingDialog;
import com.property.keys.databinding.FragmentProfileBinding;
import com.property.keys.entities.User;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.StorageUtils;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.io.IOException;
import java.util.function.Consumer;

import static com.property.keys.utils.ImageUtils.REQUEST_IMAGE;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Profile extends Fragment {
    private static final String TAG = Profile.class.getSimpleName();
    private User user;

    private FragmentProfileBinding binding;

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

        user = UserUtils.getUser(getActivity().getApplicationContext());

        TextInputEditText firstNameEditText = (TextInputEditText) binding.firstName.getEditText();
        TextInputEditText lastNameEditText = (TextInputEditText) binding.lastName.getEditText();
        TextInputEditText emailEditText = (TextInputEditText) binding.email.getEditText();
        TextInputEditText phoneNumberEditText = (TextInputEditText) binding.phoneNumber.getEditText();

        firstNameEditText.setText(user.getFirstName());
        lastNameEditText.setText(user.getLastName());
        emailEditText.setText(user.getEmail());
        phoneNumberEditText.setText(user.getPhoneNumber());


        binding.update.setOnClickListener(view -> {
            LoadingDialog loadingDialog = new LoadingDialog(this);
            loadingDialog.show();

            binding.update.setEnabled(false);
            if (!Utils.validateText(binding.firstName) | !Utils.validateText(binding.lastName) | !Utils.validateEmail(binding.email) | !Utils.validatePhoneNumber(binding.phoneNumber)) {
                binding.update.setEnabled(true);
                loadingDialog.hide();
                return;
            }

            if (user.getFirstName().equals(firstNameEditText.getText().toString())
                    && user.getLastName().equals(lastNameEditText.getText().toString())
                    && user.getPhoneNumber().equals(phoneNumberEditText.getText().toString())
                    && user.getEmail().equalsIgnoreCase(emailEditText.getText().toString())) {
                binding.update.setEnabled(true);
                loadingDialog.hide();
                return;
            }

            User newUser = new User();
            newUser.setId(user.getId());
            newUser.setFirstName(firstNameEditText.getText().toString());
            newUser.setLastName(lastNameEditText.getText().toString());
            newUser.setEmail(emailEditText.getText().toString());
            newUser.setPhoneNumber(phoneNumberEditText.getText().toString());

            Consumer<Task<Void>> onUpdateFailed = (Task<Void> task) -> {
                // If sign in fails, display a message to the user.
                Log.i(TAG, "Account update for " + user.getId() + " failed.", task.getException());
                Snackbar.make(binding.main, "Account update for " + user.getId() + " failed. Try again later.", Snackbar.LENGTH_SHORT).show();
                binding.update.setEnabled(true);
                loadingDialog.hide();
            };

            Consumer<Task<Void>> onUpdateSucceeded = (Task<Void> task) -> {
                Log.i(TAG, "Account update for " + user.getId() + " succeeded.");
                binding.update.setEnabled(true);
                loadingDialog.hide();
                UserUtils.saveUser(newUser, getActivity().getApplicationContext());
            };
            UserUtils.update(this, user, newUser, onUpdateFailed, onUpdateSucceeded);
        });

        ImageUtils.syncAndloadImages(getActivity(), user.getId(), binding.profileImage);
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
                    StorageUtils.uploadImage(user.getId(), image);
                    ImageUtils.loadImages(getActivity(), user.getId(), binding.profileImage);

                    Intent intent = new Intent();
                    intent.setAction("com.property.keys.PROFILE_IMAGE_UPDATED");
                    getActivity().sendBroadcast(intent);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    private void updateImage(View v) {
        ImageUtils.updateImage(this.getActivity(), this, user.getId());
    }
}