package com.property.keys;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.property.keys.databinding.ActivityMainBinding;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SPLASH_SCREEN = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(binding.getRoot());

        binding.logo.setAnimation(AnimationUtils.loadAnimation(this, R.anim.simple));

        new Handler().postDelayed(() -> {
            Intent signIn = new Intent(MainActivity.this, SignIn.class);

            Pair[] pairs = new Pair[1];
            pairs[0] = new Pair<View, String>(binding.logo, "logo");

            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, pairs).toBundle();
            startActivity(signIn, bundle);
        }, SPLASH_SCREEN);
    }
}