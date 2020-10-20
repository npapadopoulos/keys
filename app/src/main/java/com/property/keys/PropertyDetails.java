package com.property.keys;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.NestedScrollView;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.adapters.KeyAdapter;
import com.property.keys.adapters.KeyHolder;
import com.property.keys.databinding.ActivityPropertyDetailsBinding;
import com.property.keys.entities.Key;
import com.property.keys.entities.Notification;
import com.property.keys.entities.Property;
import com.property.keys.entities.Role;
import com.property.keys.entities.User;
import com.property.keys.filters.FirebaseRecyclerOptions;
import com.property.keys.helpers.RecyclerItemTouchHelper;
import com.property.keys.utils.FileUtils;
import com.property.keys.utils.PropertyUtils;
import com.property.keys.utils.StorageUtils;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.io.IOException;

import lombok.SneakyThrows;
import timber.log.Timber;

import static com.property.keys.utils.FileUtils.REQUEST_IMAGE;

@RequiresApi(api = Build.VERSION_CODES.R)
public class PropertyDetails extends AppCompatActivity implements FirebaseAuth.AuthStateListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private static final String TAG = Container.class.getSimpleName();

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private ActivityPropertyDetailsBinding binding;
    private KeyAdapter adapter;
    private Property property;
    private User user;
    private int titleTextColor;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            attachRecyclerViewAdapter();
        }
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }


    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = ActivityPropertyDetailsBinding.inflate(getLayoutInflater());

        property = getIntent().getParcelableExtra("property");
        initToolbar();
        setContentView(binding.getRoot());

        user = UserUtils.getLocalUser(this);

        Notification setReadNotification = getIntent().getParcelableExtra("setReadNotification");
        if (setReadNotification != null) {
            UserUtils.setReadNotifications(user.getId(), setReadNotification.getId());
        }
        property = getIntent().getParcelableExtra("property");
        binding.keyList.setHasFixedSize(false);
        firebaseDatabase.getReference("properties").child(property.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        property = snapshot.getValue(Property.class);
                        if (property != null) {
                            binding.availableSum.setText(String.valueOf(property.getKeys().values().stream().filter(k -> k.getCheckedInDate() == null).count()));
                            binding.busySum.setText(String.valueOf(property.getKeys().values().stream().filter(k -> k.getCheckedInDate() != null).count()));
                        } else {
                            binding.addNewKey.setClickable(false);
                            binding.propertyImage.setClickable(false);
                            ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                            if (cn.getClassName().equalsIgnoreCase("com.property.keys.PropertyDetails")) {
                                Snackbar snackbar = Snackbar.make(binding.placeSnackBar, "The current property has been deleted.", Snackbar.LENGTH_LONG);
                                snackbar.addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event) {
                                        finish();
                                    }
                                });
                                snackbar.show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.name.setText(property.getName());
        binding.address.setText(property.getAddress());
        binding.type.setText(property.getType());

        FileUtils.syncAndLoadImagesProperty(this, property.getId(), binding.propertyImage, true);

//        PropertyUtils.createMap(this, savedInstanceState, binding.mapquestMapView, property);
        initLayoutManager();

        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (isAdmin) {
            binding.addNewKey.setVisibility(View.VISIBLE);
            binding.propertyImage.setOnClickListener(this::updateImage);
            addOnScrollListener();
            binding.addNewKey.setOnClickListener(this::addNewKey);
        }

        Utils.initSwipeProperty(binding.keyList, this, isAdmin);
    }

    private void initLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        binding.keyList.setLayoutManager(linearLayoutManager);
    }

    private void addOnScrollListener() {
        binding.content.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if ((scrollY - oldScrollY) > 0) {
                binding.addNewKey.hide();
            } else {
                binding.addNewKey.show();
            }
        });
    }

    @SneakyThrows
    private void initToolbar() {
        //title moved below property image image
//        generateTitleTextColor(ImageUtils.getBitmapImage(this, property.getId()));
        MaterialToolbar propertyDetailsToolbar = binding.propertyDetailsToolbar;
        setSupportActionBar(propertyDetailsToolbar);
        // Set the toolbar background and text colors
        propertyDetailsToolbar.setNavigationOnClickListener(view -> finish());

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void generateTitleTextColor(Bitmap bitmap) throws IOException {
        Palette.from(bitmap)
                .generate(palette -> {
                    Palette.Swatch swatch = palette.getVibrantSwatch();
                    if (swatch == null && palette.getSwatches().size() > 0) {
                        swatch = palette.getSwatches().get(0);
                    }

                    titleTextColor = Color.WHITE;

                    if (swatch != null) {
                        titleTextColor = swatch.getTitleTextColor();
                        titleTextColor = ColorUtils.setAlphaComponent(titleTextColor, 255);
                    }
                    binding.name.setTextColor(titleTextColor);

                    binding.propertyDetailsToolbarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                        if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                            //  Collapsed
                            binding.name.setTextColor(ContextCompat.getColor(this, R.color.black_900));
                        } else {
                            //  Expanded
                            binding.name.setTextColor(titleTextColor);

                        }
                    });
                });
    }

    private void updateImage(View v) {
        FileUtils.updateImage(this, property.getId(), true);
    }

    private void addNewKey(View v) {
        View dialog = getLayoutInflater().inflate(R.layout.create_key_dialog, null);

        RadioGroup location = dialog.findViewById(R.id.location);
        RadioGroup purpose = dialog.findViewById(R.id.purpose);

        new MaterialAlertDialogBuilder(this)
                .setView(dialog)
                .setPositiveButton("Add new key", (dialogInterface, i) -> {
                    CharSequence locationValue = ((RadioButton) dialog.findViewById(location.getCheckedRadioButtonId())).getText();
                    CharSequence purposeValue = ((RadioButton) dialog.findViewById(purpose.getCheckedRadioButtonId())).getText();
                    PropertyUtils.generateKey(this, property, locationValue.toString(), purposeValue.toString());
                })
                .setBackground(ContextCompat.getDrawable(this, R.drawable.white_card_background))
                .setNegativeButton("Cancel", Utils::onClick)
                .setCancelable(false)
                .setOnKeyListener((d, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        d.dismiss();
                        return true;
                    }
                    return false;
                })
                .create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getParcelableExtra("path"));
                    FileUtils.saveImage(getApplicationContext(), image, property.getId());
                    StorageUtils.uploadImage(property.getId(), "property", image);
                    FileUtils.loadImage(this, property.getId(), binding.propertyImage);
                    generateTitleTextColor(image);
                } catch (IOException e) {
                    Timber.tag(TAG).e(e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onResume() {
//        Utils.checkForPermissions(this);
        super.onResume();
//        binding.mapquestMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        binding.mapquestMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        binding.mapquestMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        binding.mapquestMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (adapter == null) {
            attachRecyclerViewAdapter();
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof KeyHolder) {
            new MaterialAlertDialogBuilder(viewHolder.itemView.getContext())
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        PropertyUtils.deleteKey(PropertyDetails.this, property, adapter.getItem(viewHolder.getAdapterPosition()).getId());
                        Snackbar.make(binding.main, "Key deleted.", Snackbar.LENGTH_LONG).show();
                    })
                    .setOnKeyListener((d, keyCode, event) -> {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            d.dismiss();
                            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                            return true;
                        }
                        return false;
                    })
                    .setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.white_card_background))
                    .setNegativeButton("No", (dialogInterface, i) -> adapter.notifyItemChanged(viewHolder.getAdapterPosition()))
                    .setCancelable(false)
                    .create().show();
        }
    }

    private void attachRecyclerViewAdapter() {
        Query query = firebaseDatabase.getReference("properties").child(property.getId()).child("keys").orderByChild("checkedInDate");
        FirebaseRecyclerOptions<Key> options =
                new FirebaseRecyclerOptions.Builder<Key>()
                        .setQuery(query, Key.class)
                        .setLifecycleOwner(this)
                        .build();

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        adapter = new KeyAdapter(options, this, this.getSupportFragmentManager(), property.getName(), user);

        // Scroll to bottom on new messages
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                binding.keyList.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        binding.keyList.setAdapter(adapter);
    }
}