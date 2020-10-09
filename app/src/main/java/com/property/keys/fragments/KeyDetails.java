package com.property.keys.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.FirebaseDatabase;
import com.property.keys.R;
import com.property.keys.databinding.FragmentKeyDetailsBinding;
import com.property.keys.entities.Key;
import com.property.keys.utils.ImageUtils;
import com.property.keys.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.property.keys.utils.Utils.showDatePicker;

@RequiresApi(api = Build.VERSION_CODES.R)
public class KeyDetails extends BottomSheetDialogFragment {

    private static final String TAG = KeyDetails.class.getSimpleName();

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private final Key key;
    private final String imagePath;
    private final String propertyName;

    private FragmentKeyDetailsBinding binding;
    private Context context;

    public KeyDetails(Context context, Key key, String imagePath, String propertyName) {
        this.key = key;
        this.context = context;
        this.imagePath = imagePath;
        this.propertyName = propertyName;
    }

    public static KeyDetails newInstance(Context context, Key key, String imagePath, String propertyName) {
        return new KeyDetails(context, key, imagePath, propertyName);
    }

    private static void addTitle(Context context, Bitmap bitmap, String propertyName) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, R.color.red_600));
        paint.setTextSize(25);

        float textWidth = paint.measureText(propertyName);
        int xPos = bitmap.getWidth() / 2 - (int) (textWidth / 2);
        int yPos = (int) (bitmap.getHeight() - 20 - ((paint.descent() + paint.ascent()) / 2));
        canvas.drawText(propertyName, xPos, yPos, paint);
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
        binding = FragmentKeyDetailsBinding.inflate(getLayoutInflater());

        binding.shareKey.setOnClickListener(view -> {
            //call share
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Bitmap copiedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            addTitle(context, copiedBitmap, propertyName); //TODO configurable -> Settings Menu

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", Objects.requireNonNull(ImageUtils.saveImage(context, copiedBitmap, "code-title")));

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/jpeg");

            context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.print_qrcode)));
            dismiss();
        });

        binding.editEstimatedCheckOutDate.setOnClickListener(view -> {

            if (TextUtils.isEmpty(key.getEstimatedCheckOutDate())) {
                AlertDialog ok = new MaterialAlertDialogBuilder(requireContext())
                        .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
                        .setMessage("Key is available. Check In first so youc can update the estimated check out date.")
                        .setNeutralButton("Ok", Utils::onClick).create();
                ok.setOnDismissListener(dialog -> dismiss());
                ok.show();
            } else {
                //pop up edit estimated checkout date
                DatePickerDialog.OnDateSetListener onDateSetListener = (v, year, monthOfYear, dayOfMonth) -> {
                    String updatedMonth = String.valueOf(monthOfYear);
                    String updatedDay = String.valueOf(dayOfMonth);
                    if (monthOfYear < 10) {
                        updatedMonth = "0" + monthOfYear;
                    }
                    if (dayOfMonth < 10) {
                        updatedDay = "0" + updatedDay;
                    }
                    key.setEstimatedCheckOutDate(updatedDay + "-" + updatedMonth + "-" + year);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("users/" + key.getLastCheckedInUserId() + "/keys/" + key.getId(), key);
                    updates.put("keys/" + key.getId(), key);
                    updates.put("properties/" + key.getPropertyId() + "/keys/" + key.getId(), key);

                    firebaseDatabase.getReference("/").updateChildren(updates);
                };

                showDatePicker(requireContext(), onDateSetListener, TextUtils.isEmpty(key.getEstimatedCheckOutDate()) ? null : key.getEstimatedCheckOutDate());
            }
            dismiss();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireDialog().getWindow().setWindowAnimations(R.style.ToolbarDialogAnimation);
    }
}