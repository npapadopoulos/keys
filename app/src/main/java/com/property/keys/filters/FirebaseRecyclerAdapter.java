package com.property.keys.filters;

import android.os.Build;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Setter;

import static java.util.stream.Collectors.toList;

@RequiresApi(api = Build.VERSION_CODES.R)
public abstract class FirebaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements FirebaseAdapter<T>, Filterable {
    private static final String TAG = "FirebaseRecyclerAdapter";

    private FirebaseRecyclerOptions<T> mOptions;
    private ObservableSnapshotArray<T> mSnapshots;
    private List<T> properties;
    private Filter filter;
    private boolean showOnlyFavourites;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     */
    public FirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions<T> options) {
        mOptions = options;
        mSnapshots = options.getSnapshots();

        properties = new ArrayList<>(options.getSnapshots());

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
        T model = mSnapshots.get(newIndex);
        switch (type) {
            case ADDED:
                properties.add(newIndex, model);
                notifyItemInserted(newIndex);
                break;
            case CHANGED:
                properties.remove(newIndex);
                properties.add(newIndex, model);
                notifyItemChanged(newIndex);
                break;
            case REMOVED:
                properties.remove(newIndex);
                notifyItemRemoved(newIndex);
                break;
            case MOVED:
                properties.remove(oldIndex);
                properties.add(newIndex, model);
                notifyItemMoved(oldIndex, newIndex);
                break;
            default:
                throw new IllegalStateException("Incomplete case statement");
        }
    }

    @Override
    public void onDataChanged() {
    }

    @Override
    public void onError(@NonNull DatabaseError error) {
        Log.w(TAG, error.toException());
    }

    @NonNull
    @Override
    public ObservableSnapshotArray<T> getSnapshots() {
        return mSnapshots;
    }

    @NonNull
    @Override
    public T getItem(int position) {
        return properties.get(position);
    }

    @NonNull
    @Override
    public DatabaseReference getRef(int position) {
        return mSnapshots.getSnapshot(position).getRef();
    }

    @Override
    public int getItemCount() {
        return mSnapshots.isListening(this) ? properties.size() : 0;
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
        stopListening();

        // Set up new options
        mOptions = options;
        mSnapshots = options.getSnapshots();
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
    protected boolean filterCondition(T model, String pattern) {
        return true;
    }

    /**
     * show only favourites condition for Filter
     *
     * @param model model T
     */
    protected boolean filterFavourites(T model) {
        return false;
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

        @Setter
        private boolean showOnlyFavourites;

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            String query = charSequence.toString();
            List<T> filtered = new ArrayList<>();
            if (query.isEmpty()) {
                properties.clear();
                properties.addAll(getSnapshots());
                filtered.addAll(properties);
            } else {
                filtered.addAll(properties.stream()
                        .filter(model -> filterCondition(model, query))
                        .collect(toList()));
            }

            if (showOnlyFavourites) {
                filtered = filtered.stream().filter(FirebaseRecyclerAdapter.this::filterFavourites).collect(toList());
            }

            FilterResults results = new FilterResults();
            results.values = filtered;
            results.count = filtered.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            properties.clear();
            properties.addAll((Collection<? extends T>) filterResults.values);
            notifyDataSetChanged();
        }
    }
}

