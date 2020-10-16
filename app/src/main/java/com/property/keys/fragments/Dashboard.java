package com.property.keys.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.property.keys.Container;
import com.property.keys.R;
import com.property.keys.databinding.FragmentDashboardBinding;
import com.property.keys.entities.HistoryDetails;
import com.property.keys.entities.User;
import com.property.keys.tasks.TaskExecutor;
import com.property.keys.tasks.google.ReportCreationTask;
import com.property.keys.utils.UserUtils;
import com.property.keys.utils.Utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import lombok.SneakyThrows;

import static com.property.keys.utils.Utils.DATE_TIME_FORMATTER;
import static com.property.keys.utils.Utils.showDatePicker;
import static java.util.stream.Collectors.groupingBy;

@RequiresApi(api = Build.VERSION_CODES.R)
public class Dashboard extends Fragment {
    private static final String TAG = Dashboard.class.getSimpleName();

    private ChipNavigationBar bottomNavigationMenu;
    private NavigationView navigation;
    private MaterialToolbar toolbar;
    private static final Query historyQuery = firebaseDatabase.getReference("history").orderByKey();

    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private User user;

    public Dashboard(ChipNavigationBar bottomNavigationMenu, NavigationView navigation, MaterialToolbar toolbar) {
        this.bottomNavigationMenu = bottomNavigationMenu;
        this.navigation = navigation;
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDashboardBinding binding = FragmentDashboardBinding.inflate(getLayoutInflater(), container, false);

        user = UserUtils.getLocalUser(requireContext());
        bottomNavigationMenu.setItemSelected(R.id.bottom_navigation_dashboard, true);
        navigation.setCheckedItem(R.id.navigationDashboard);
        navigation.getCheckedItem().setChecked(true);
        this.toolbar.setTitle("Dashboard");
        toolbar.setEnabled(true);
        toolbar.setVisibility(View.VISIBLE);
        binding.stats.setVisibility(View.GONE);

        final ProgressBar progressBar = getActivity().findViewById(R.id.progressBar);

        final List<String> reasons = Arrays.asList("Cleaning", "External Partner", "Maintenance", "Viewing", "Inspection");
        historyQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                List<HistoryDetails> temp = new ArrayList<>();
                dataSnapshot.getChildren().forEach(child -> temp.add(child.getValue(HistoryDetails.class)));

                Map<Boolean, List<HistoryDetails>> historyDetailsPerKeyAvailabilityStatus = temp.stream().collect(groupingBy(historyDetails -> historyDetails.getKey().getCheckedInDate() != null));
                Map<String, List<HistoryDetails>> historyDetailsPerKeyCheckOutReason = temp.stream().collect(groupingBy(historyDetails -> {
                    String checkInReason = historyDetails.getKey().getCheckInReason();
                    if (reasons.contains(checkInReason)) {
                        return checkInReason;
                    } else {
                        return "Other";
                    }
                }));

                long checkedInKeysTodayValue = temp.stream().filter(historyDetails -> historyDetails.getKey().getCheckedInDate() != null && LocalDate.from(Utils.DATE_TIME_FORMATTER.parse(historyDetails.getKey().getCheckedInDate())).isEqual(LocalDate.now())).count();
                long checkedOutKeysTodayValue = temp.stream().filter(historyDetails -> historyDetails.getKey().getCheckedOutDate() != null && LocalDate.from(Utils.DATE_TIME_FORMATTER.parse(historyDetails.getKey().getCheckedOutDate())).isEqual(LocalDate.now())).count();

//                Map<String, List<HistoryDetails>> historyDetailsPerKeyId = temp.stream().collect(groupingBy(historyDetails -> historyDetails.getKey().getId()));
//                binding.totalKeysValue.setText(historyDetailsPerKeyId.size());
//                binding.checkedOutAverageDurationValue.setText(); //TODO

                String checkedInKeysTotalValue = String.valueOf(historyDetailsPerKeyAvailabilityStatus.get(Boolean.FALSE) == null ? 0 : historyDetailsPerKeyAvailabilityStatus.get(Boolean.FALSE).size());
                String checkedOutKeysTotalValue = String.valueOf(historyDetailsPerKeyAvailabilityStatus.get(Boolean.TRUE) == null ? 0 : historyDetailsPerKeyAvailabilityStatus.get(Boolean.TRUE).size());
                binding.checkedInKeysTotalValue.setText(checkedInKeysTotalValue);
                binding.checkedOutKeysTotalValue.setText(checkedOutKeysTotalValue);
                binding.checkedInKeysTodayValue.setText(String.valueOf(checkedInKeysTodayValue));
                binding.checkedOutKeysTodayValue.setText(String.valueOf(checkedOutKeysTodayValue));
                binding.cleaningValue.setText(String.valueOf(historyDetailsPerKeyCheckOutReason.get("Cleaning") != null ? historyDetailsPerKeyCheckOutReason.get("Cleaning").size() : 0));
                binding.externalPartnerValue.setText(String.valueOf(historyDetailsPerKeyCheckOutReason.get("External Partner") != null ? historyDetailsPerKeyCheckOutReason.get("External Partner").size() : 0));
                binding.maintenanceValue.setText(String.valueOf(historyDetailsPerKeyCheckOutReason.get("Maintenance") != null ? historyDetailsPerKeyCheckOutReason.get("Maintenance").size() : 0));
                binding.viewingValue.setText(String.valueOf(historyDetailsPerKeyCheckOutReason.get("Viewing") != null ? historyDetailsPerKeyCheckOutReason.get("Viewing").size() : 0));
                binding.inspectionValue.setText(String.valueOf(historyDetailsPerKeyCheckOutReason.get("Inspection") != null ? historyDetailsPerKeyCheckOutReason.get("Inspection").size() : 0));
                binding.otherValue.setText(String.valueOf(historyDetailsPerKeyCheckOutReason.get("Other") != null ? historyDetailsPerKeyCheckOutReason.get("Other").size() : 0));

                binding.stats.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.generateReport.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            historyQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @SneakyThrows
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    generateReport(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

        return binding.getRoot();
    }


    private void generateReport(@NonNull DataSnapshot dataSnapshot) {
        String fileName = "History.Report." + DATE_TIME_FORMATTER.format(LocalDateTime.now()).replaceAll(" ", ".").replaceAll(":", ".").replaceAll("-", ".");
        View generateReportDialog = getLayoutInflater().inflate(R.layout.generate_report_dialog, null);

        Instant now = Instant.now();
        long toEpochMilli = now.toEpochMilli();
        AtomicLong from = new AtomicLong(toEpochMilli);
        AtomicLong to = new AtomicLong(toEpochMilli);

        AtomicBoolean customPeriodChecked = new AtomicBoolean(false);
        RadioGroup period = generateReportDialog.findViewById(R.id.period);
        period.setOnCheckedChangeListener((group, checkedId) -> {
            if (!customPeriodChecked.get() && checkedId == R.id.customPeriod) {
                MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>> onPositiveButtonClickListener = (Pair<Long, Long> range) -> {
                    from.set(range.first);
                    to.set(range.second);
                };
                showDatePicker(requireFragmentManager(), onPositiveButtonClickListener, d -> {
                    customPeriodChecked.set(true);
                    period.check(R.id.today);
                }, v -> {
                    customPeriodChecked.set(true);
                    period.check(R.id.today);
                });
                customPeriodChecked.set(true);
            } else {
                customPeriodChecked.set(false);
            }
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setView(generateReportDialog)
                .setPositiveButton("Generate Report", (dialogInterface, i) -> {
                    if (period.getCheckedRadioButtonId() == R.id.week) {
                        from.set(now.minus(7, ChronoUnit.DAYS).toEpochMilli());
                        to.set(toEpochMilli);
                    } else if (period.getCheckedRadioButtonId() == R.id.month) {
                        from.set(now.minus(30, ChronoUnit.DAYS).toEpochMilli());
                        to.set(toEpochMilli);
                    }
                    new TaskExecutor().executeAsync(new ReportCreationTask(fileName, requireActivity(), dataSnapshot, from.get(), to.get(), instantInstantPair -> {
                        String description = "No history found for period from " + DATE_TIME_FORMATTER.format(instantInstantPair.first) + " to " + DATE_TIME_FORMATTER.format(instantInstantPair.second) + ".";
                        Snackbar.make(((Container) requireActivity()).getPlaceSnackBar(), description, Snackbar.LENGTH_SHORT).show();
                    }));
                    Snackbar.make(((Container) requireActivity()).getPlaceSnackBar(), "You will notified by email once the report is ready.", Snackbar.LENGTH_SHORT).show();
                })
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.white_card_background))
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
}