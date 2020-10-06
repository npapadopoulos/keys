package com.property.keys.filters;

import android.os.Build;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import timber.log.Timber;

import static java.util.stream.Collectors.toList;

@RequiresApi(api = Build.VERSION_CODES.R)
public abstract class FirebaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements FirebaseAdapter<T>, Filterable {
    private static final String TAG = "FirebaseRecyclerAdapter";

    private final boolean isFilterable;
    private ObservableSnapshotArray<T> snapshots;
    private FirebaseRecyclerOptions<T> mOptions;
    private Filter filter;
    private List<T> list, backupList;
    private Class<T> clazz;
    private ProgressBar progressBar;
    private boolean filtered = false;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     */
    public FirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions<T> options, boolean isFilterable, Class<T> clazz, ProgressBar progressBar) {
        this(options, isFilterable, clazz, progressBar, null);
    }

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     */
    public FirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions<T> options, boolean isFilterable, Class<T> clazz, ProgressBar progressBar,
                                   String currentUserId) {
        mOptions = options;
        snapshots = options.getSnapshots();
        if (currentUserId != null) {
            list = snapshots.stream().filter(item -> !getId(item).equals(currentUserId)).collect(toList());
        } else {
            list = new ArrayList<>(options.getSnapshots());
        }
        backupList = new ArrayList<>(list);

        this.progressBar = progressBar;
        this.isFilterable = isFilterable;
        this.clazz = clazz;

        if (mOptions.getOwner() != null) {
            mOptions.getOwner().getLifecycle().addObserver(this);
        }
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void startListening() {
        if (list.size() > 0) list.clear();
        if (backupList.size() > 0) backupList.clear();
        if (!snapshots.isListening(this)) {
            snapshots.addChangeEventListener(this);
        }
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void stopListening() {
        snapshots.removeChangeEventListener(this);
        list.clear();
        backupList.clear();
        notifyDataSetChanged();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void cleanup(LifecycleOwner source) {
        source.getLifecycle().removeObserver(this);
    }


    @Override
    public void onChildChanged(ChangeEventType type,
                               DataSnapshot snapshot,
                               int newIndex,
                               int oldIndex) {
        onChildUpdate(snapshot.getValue(clazz), type, newIndex, oldIndex);
    }

    protected void onChildUpdate(T model, ChangeEventType type,
                                 int newIndex,
                                 int oldIndex) {
        switch (type) {
            case ADDED:
                addItem(model);
                notifyItemInserted(newIndex);
                break;
            case CHANGED:
                addItem(model, newIndex);
                notifyItemChanged(newIndex);
                break;
            case REMOVED:
                removeItem(newIndex);
                notifyItemRemoved(newIndex);
                break;
            case MOVED:
                moveItem(model, newIndex, oldIndex);
                notifyItemMoved(oldIndex, newIndex);
                break;
            default:
                throw new IllegalStateException("Incomplete case statement");
        }
    }

    private void moveItem(T t, int newIndex, int oldIndex) {
        list.remove(oldIndex);
        list.add(newIndex, t);
        if (isFilterable) {
            backupList.remove(oldIndex);
            backupList.add(newIndex, t);
        }
    }

    private void removeItem(int newIndex) {
        if (list.size() != backupList.size() && !backupList.isEmpty()) {
            list.remove(backupList.get(newIndex));
        } else {
            list.remove(newIndex);
        }
        if (isFilterable)
            backupList.remove(newIndex);
    }

    private void addItem(T t, int newIndex) {
        list.remove(newIndex);
        list.add(newIndex, t);
        if (isFilterable) {
            backupList.remove(newIndex);
            backupList.add(newIndex, t);
        }
    }

    private void addItem(T t) {
        list.add(t);
        if (isFilterable)
            backupList.add(t);
    }


    @Override
    public void onDataChanged() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void postFilterUpdate(int count) {
    }

    @Override
    public void onError(@NonNull DatabaseError error) {
        Timber.tag(TAG).w(error.toException());
    }

    @NonNull
    @Override
    public ObservableSnapshotArray<T> getSnapshots() {
        return snapshots;
    }

    @NonNull
    @Override
    public T getItem(int position) {
        return filtered ? list.get(position) : snapshots.get(position);
    }

    @NonNull
    @Override
    public DatabaseReference getRef(int position) {
        return snapshots.getSnapshot(position).getRef();
    }

    @Override
    public int getItemCount() {
        return filtered ? list.size() : snapshots.size();
    }

    /**
     * Re-initialize the Adapter with a new set of options. Can be used to change the query
     * without re-constructing the entire adapter.
     */
    public void updateOptions(@NonNull FirebaseRecyclerOptions<T> options) {
        // Tear down old options
        boolean wasListening = snapshots.isListening(this);
        if (mOptions.getOwner() != null) {
            mOptions.getOwner().getLifecycle().removeObserver(this);
        }
        snapshots.clear();
        list.clear();
        backupList.clear();
        stopListening();

        // Set up new options
        mOptions = options;
        snapshots = options.getSnapshots();
        list = new ArrayList<>(options.getSnapshots());
        backupList = new ArrayList<>(list);

        if (options.getOwner() != null) {
            options.getOwner().getLifecycle().addObserver(this);
        }
        if (wasListening) {
            startListening();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        onBindViewHolder(holder, position, getItem(position));
    }

    /**
     * @param model the model object containing the data that should be used to populate the view.
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    protected abstract void onBindViewHolder(@NonNull VH holder, int position, @NonNull T model);

    /**
     * filter condition for Filter
     *
     * @param model   model T
     * @param pattern
     */
    protected boolean filterCondition(T model, String pattern) {
        return true;
    }

    /**
     * filter condition for Filter
     *
     * @param model             model T
     * @param applyExtraFilters
     */
    protected boolean filterCondition(T model, List<String> applyExtraFilters) {
        return true;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new SearchFilter();
        }
        filtered = true;
        return filter;
    }

    /**
     * Simple search on arrived data from firebase.
     * Note that the search query doesn't apply on firebase directly.
     */
    public class SearchFilter extends Filter {
        private List<String> applyExtraFilters;

        public Filter applyExtraFilters(List<String> applyExtraFilters) {
            this.applyExtraFilters = applyExtraFilters;
            return this;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                if (applyExtraFilters.isEmpty()) {
                    results.values = backupList;
                    results.count = backupList.size();
                } else {
                    List<T> filteredList = new ArrayList<>();
                    for (T t : backupList) {
                        if (filterCondition(t, applyExtraFilters)) {
                            filteredList.add(t);
                        }
                    }
                    results.values = filteredList;
                    results.count = filteredList.size();
                }
            } else {
                List<T> filteredList = new ArrayList<>();
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (T t : backupList) {
                    if (filterCondition(t, filterPattern) && filterCondition(t, applyExtraFilters)) {
                        filteredList.add(t);
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            list.clear();
            list.addAll((Collection<? extends T>) results.values);

            postFilterUpdate(list.size());
            notifyDataSetChanged();
        }
    }
}

