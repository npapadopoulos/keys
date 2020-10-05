package com.property.keys.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.property.keys.R;
import com.property.keys.databinding.FragmentFilterPropertiesBinding;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RequiresApi(api = Build.VERSION_CODES.R)
public class FilterProperties extends BottomSheetDialogFragment {

    private static final String TAG = FilterProperties.class.getSimpleName();

    private FragmentFilterPropertiesBinding binding;

    private List<String> types;

    private FilterProperties(List<String> types) {
        this.types = types;
    }

    public static FilterProperties newInstance(List<String> types) {
        return new FilterProperties(types);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFilterPropertiesBinding.inflate(getLayoutInflater());

        binding.apply.setOnClickListener(view -> {
            binding.apply.setEnabled(false);
            prepareFilters();
            dismiss();
        });

        binding.close.setOnClickListener(view -> {
            prepareFilters();
            dismiss();
        });

        if (types != null && !types.isEmpty()) {
            for (int i = 0; i < binding.types.getChildCount(); i++) {
                Chip chip = (Chip) binding.types.getChildAt(i);
                if (types.contains(chip.getText().toString())) {
                    chip.setChecked(true);
                } else {
                    chip.setChecked(false);
                }
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireDialog().getWindow().setWindowAnimations(R.style.ToolbarDialogAnimation);
    }

    private void prepareFilters() {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("filters", getFilters());

        Intent intent = new Intent().putExtras(bundle);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }

    public ArrayList<String> getFilters() {
        return (ArrayList<String>) binding.types.getCheckedChipIds().stream()
                .map(id -> ((Chip) binding.types.findViewById(id)).getText().toString()).collect(toList());
    }
}