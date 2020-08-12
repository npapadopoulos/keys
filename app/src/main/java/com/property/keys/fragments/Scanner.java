package com.property.keys.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.property.keys.databinding.FragmentScannerBinding;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Scanner extends Fragment {
    private static final String TAG = Scanner.class.getSimpleName();

    private CodeScanner mCodeScanner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentScannerBinding binding = FragmentScannerBinding.inflate(getLayoutInflater(), container, false);
        FragmentActivity activity = getActivity();

        CodeScannerView scannerView = binding.scannerView;
        mCodeScanner = new CodeScanner(activity, scannerView);
        mCodeScanner.setDecodeCallback(result -> activity.runOnUiThread(() -> System.out.println("QR Code: " + result.getText())));
        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}