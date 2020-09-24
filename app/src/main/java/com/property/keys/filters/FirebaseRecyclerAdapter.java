package com.property.keys.filters;

import android.os.Build;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import timber.log.Timber;

import static java.util.function.Function.identity;

@RequiresApi(api = Build.VERSION_CODES.R)
public abstract class FirebaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements FirebaseAdapter<T>, Filterable {
    private static final String TAG = "FirebaseRecyclerAdapter";

    private final boolean isFilterable;
    private ObservableSnapshotArray<T> mSnapshots;
    private FirebaseRecyclerOptions<T> mOptions;
    private Filter filter;
    private Map<String, T> list, backupList;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     */
    public FirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions<T> options, boolean isFilterable) {
        mOptions = options;
        mSnapshots = options.getSnapshots();
        list = mSnapshots.stream().collect(Collectors.toMap(this::getId, identity()));
        backupList = new HashMap<>(list);

        this.isFilterable = isFilterable;

        if (mOptions.getOwner() != null) {
            mOptions.getOwner().getLifecycle().addObserver(this);
        }
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void startListening() {
        if (!mSnapshots.isListening(this)) {
            mSnapshots.addChangeEventListener(this);
        }
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void stopListening() {
        mSnapshots.removeChangeEventListener(this);
        notifyDataSetChanged();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void cleanup(LifecycleOwner source) {
        source.getLifecycle().removeObserver(this);
    }

    @Override
    public void onChildChanged(@NonNull ChangeEventType type,
                               @NonNull DataSnapshot snapshot,
                               int newIndex,
                               int oldIndex) {
        T model = null;
        if (type != ChangeEventType.REMOVED && mSnapshots != null && !mSnapshots.isEmpty()) {
            model = mSnapshots.get(newIndex);
        }
        onChildUpdate(model, type, newIndex, oldIndex, snapshot.getKey());
    }

    protected void onChildUpdate(T model, ChangeEventType type,
                                 int newIndex,
                                 int oldIndex,
                                 String key) {
        switch (type) {
            case ADDED:
                update(key, model);
                notifyItemInserted(newIndex);
                break;
            case CHANGED:
                update(key, model);
                notifyItemChanged(newIndex);
                break;
            case REMOVED:
                remove(key);
                notifyItemRemoved(newIndex);
                break;
            case MOVED:
                update(key, model);
                notifyItemMoved(oldIndex, newIndex);
                break;
            default:
                throw new IllegalStateException("Incomplete case statement");
        }
    }

    private void update(String key, T t) {
        list.put(key, t);
        if (isFilterable) {
            backupList.put(key, t);
        }
    }

    private void remove(String key) {
        list.remove(key);
        if (isFilterable)
            backupList.remove(key);
    }

    @Override
    public void onDataChanged() {
    }

    @Override
    public void onError(@NonNull DatabaseError error) {
        Timber.tag(TAG).w(error.toException());
    }

    @NonNull
    @Override
    public ObservableSnapshotArray<T> getSnapshots() {
        return mSnapshots;
    }

    @NonNull
    @Override
    public T getItem(int position) {
        String key = mSnapshots.getSnapshot(position).getKey();
        if (!list.containsKey(key)) {
            return getItem(++position);
        }
        return Objects.requireNonNull(list.get(key));
    }

    @NonNull
    @Override
    public DatabaseReference getRef(int position) {
        return mSnapshots.getSnapshot(position).getRef();
    }

    @Override
    public int getItemCount() {
        return mSnapshots.isListening(this) ? list.size() : 0;
    }

    /**
     * Re-initialize the Adapter with a new set of options. Can be used to change the query
     * without re-constructing the entire adapter.
     */
    public void updateOptions(@NonNull FirebaseRecyclerOptions<T> options) {
        // Tear down old options
        boolean wasListening = mSnapshots.isListening(this);
        if (mOptions.getOwner() != null) {
            mOptions.getOwner().getLifecycle().removeObserver(this);
        }
        mSnapshots.clear();
        list.clear();
        backupList.clear();
        stopListening();

        // Set up new options
        mOptions = options;
        mSnapshots = options.getSnapshots();
        list = mSnapshots.stream().collect(Collectors.toMap(this::getId, identity()));
        backupList = new HashMap<>(list);

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
     * @param pattern filter pattern with Lower Case
     */
    protected boolean filterCondition(T model, String pattern, boolean showOnlyFavourite) {
        return true;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new SearchFilter();
        }
        return filter;
    }

    /**
     * Simple search on arrived data from firebase.
     * Note that the search query doesn't apply on firebase directly.
     */
    public class SearchFilter extends Filter {

        private boolean showOnlyFavourites;

        public Filter showOnlyFavourites(boolean showOnlyFavourites) {
            this.showOnlyFavourites = showOnlyFavourites;
            return this;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = backupList;
                results.count = backupList.size();
            } else {
                Map<String, T> filteredList = new HashMap<>();
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (T t : backupList.values()) {
                    if (filterCondition(t, filterPattern, showOnlyFavourites)) {
                        filteredList.put(getId(t), t);
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
            list.putAll((Map<? extends String, ? extends T>) results.values);
            notifyDataSetChanged();
        }
    }
}

