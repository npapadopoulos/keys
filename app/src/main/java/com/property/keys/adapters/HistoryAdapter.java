package com.property.keys.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.property.keys.R;
import com.property.keys.entities.HistoryDetails;
import com.property.keys.filters.FirebaseRecyclerAdapter;
import com.property.keys.filters.FirebaseRecyclerOptions;

import org.jetbrains.annotations.NotNull;

@RequiresApi(api = Build.VERSION_CODES.R)
public class HistoryAdapter extends FirebaseRecyclerAdapter<HistoryDetails, HistoryHolder> {

    @NonNull
    private Activity activity;
    private LinearLayout emptyHistory;

    public HistoryAdapter(@NonNull FirebaseRecyclerOptions<HistoryDetails> options, @NotNull Activity activity) {
        super(options, false, HistoryDetails.class, activity.findViewById(R.id.progressBar));
        this.activity = activity;
        emptyHistory = activity.findViewById(R.id.empty_history);
    }

    @Override
    protected void onBindViewHolder(@NonNull HistoryHolder holder, int position, @NonNull HistoryDetails model) {
        holder.bind(activity, model);
    }

    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_details, parent, false));
    }

    @NonNull
    @Override
    public String getId(HistoryDetails historyDetails) {
        return historyDetails.getId();
    }

    @Override
    public void onDataChanged() {
        if (getItemCount() == 0) {
            emptyHistory.setVisibility(View.VISIBLE);
        } else {
            emptyHistory.setVisibility(View.INVISIBLE);
        }
        super.onDataChanged();
    }
}
