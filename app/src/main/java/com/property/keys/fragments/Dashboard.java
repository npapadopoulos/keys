package com.property.keys.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.R;
import com.property.keys.databinding.FragmentDashboardBinding;
import com.property.keys.entities.Action;
import com.property.keys.entities.Notification;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.property.keys.utils.Utils.DATE_TIME_FORMATTER;
import static java.util.stream.Collectors.groupingBy;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Dashboard extends Fragment implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = Dashboard.class.getSimpleName();

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;

    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static final Query notificationsQuery = firebaseDatabase.getReference("notifications");
    private final Map<String, List<Notification>> notificationPerUserId = new HashMap<>();
    private PieChart chart;
    private SeekBar seekBarX;

    public Dashboard(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDashboardBinding binding = FragmentDashboardBinding.inflate(getLayoutInflater(), container, false);

        bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_dashboard, true);
        navigation.setCheckedItem(R.id.navigationDashboard);
        navigation.getCheckedItem().setChecked(true);
        toolbar.setTitle("Dashboard");


        notificationsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Notification> temp = new ArrayList<>();
                dataSnapshot.getChildren().forEach(child -> {
                    Notification notification = child.getValue(Notification.class);
                    if (notification != null && (notification.getAction() == Action.CHECKED_IN || notification.getAction() == Action.CHECKED_OUT)) {
                        temp.add(notification);
                    }
                });

                notificationPerUserId.clear();
                notificationPerUserId.putAll(temp.stream().collect(groupingBy(notification -> DayOfWeek.from(DATE_TIME_FORMATTER.parse(notification.getDate())).toString())));
                setData(notificationPerUserId, 4);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        seekBarX = binding.seekBar1;
        chart = binding.chart;

        seekBarX.setOnSeekBarChangeListener(this);

        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(false);
        chart.setRotationAngle(0);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        // add a selection listener
        chart.setOnChartValueSelectedListener(this);

        seekBarX.setProgress(4);

        chart.animateY(1400, Easing.EaseInOutQuad);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        // entry label styling
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(12f);

        return binding.getRoot();
    }

    private void setData(Map<String, List<Notification>> notificationPerUserId, int count) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        int i = 0;
        notificationPerUserId.forEach((dayOfWeek, notifications) -> {
            if (i < count) {
                entries.add(new PieEntry(notifications.size(), dayOfWeek));
            }
        });

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        setData(notificationPerUserId, seekBarX.getProgress());
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}