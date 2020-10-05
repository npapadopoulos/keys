package com.property.keys.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
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
import com.property.keys.entities.Key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Dashboard extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = Dashboard.class.getSimpleName();

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;

    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static final Query keysQuery = firebaseDatabase.getReference("keys");
    private final Map<Boolean, List<Key>> keysPerStatus = new HashMap<>();
    private PieChart pieChart;

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
        toolbar.setEnabled(true);
        toolbar.setVisibility(View.VISIBLE);

        keysQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                List<Key> temp = new ArrayList<>();
                dataSnapshot.getChildren().forEach(child -> temp.add(child.getValue(Key.class)));

                keysPerStatus.clear();
                keysPerStatus.putAll(temp.stream().collect(groupingBy(key -> key.getCheckedInDate() != null)));

                setPieChartData(keysPerStatus);
                getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        initPieChart(binding.pieChart);

        return binding.getRoot();
    }

    private void initPieChart(PieChart pieChart) {
        this.pieChart = pieChart;

        this.pieChart.setUsePercentValues(false);
        this.pieChart.getDescription().setEnabled(false);
        this.pieChart.setExtraOffsets(5, 10, 5, 5);
        this.pieChart.setDragDecelerationFrictionCoef(0.95f);
        this.pieChart.setTransparentCircleColor(Color.WHITE);
        this.pieChart.setTransparentCircleAlpha(110);
        this.pieChart.setTransparentCircleRadius(61f);
        this.pieChart.setDrawCenterText(false);
        this.pieChart.setRotationAngle(0);
        // enable rotation of the barChart by touch
        this.pieChart.setRotationEnabled(false);
        this.pieChart.setHighlightPerTapEnabled(false);

        this.pieChart.animateY(1400, Easing.EaseInOutQuad);

        Legend legend = this.pieChart.getLegend();
        legend.setEnabled(false);

        // entry label styling
        this.pieChart.setEntryLabelColor(Color.BLACK);
        this.pieChart.setEntryLabelTextSize(15f);
    }

    private void setPieChartData(Map<Boolean, List<Key>> data) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        data.forEach((key, values) -> entries.add(new PieEntry(values.size(), key ? "Busy" : "Available")));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

//        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//        dataSet.setValueLinePart1OffsetPercentage(10.f);
//        dataSet.setValueLinePart1Length(0.43f);
//        dataSet.setValueLinePart2Length(.1f);
//        dataSet.setValueTextColor(Color.BLACK);
//        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//        pieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.primaryColor));

        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(requireContext(), R.color.red_600));
        colors.add(ContextCompat.getColor(requireContext(), R.color.green));

        dataSet.setColors(colors);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(15f);
        pieData.setValueTextColor(Color.BLACK);
        pieChart.setData(pieData);

        // undo all highlights
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }
}