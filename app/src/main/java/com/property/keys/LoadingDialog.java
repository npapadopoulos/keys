package com.property.keys;

import android.app.AlertDialog;
import android.view.LayoutInflater;

import androidx.fragment.app.Fragment;

public class LoadingDialog {
    private Fragment fragment;
    private AlertDialog dialog;

    public LoadingDialog(Fragment fragment) {
        this.fragment = fragment;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        LayoutInflater inflater = fragment.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progress_bar_dialog, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
        dialog.setOnDismissListener(dialogInterface -> System.out.println());
        dialog.setOnCancelListener(dialogInterface -> System.out.println());
    }

    public void hide() {
        dialog.dismiss();
    }
}
