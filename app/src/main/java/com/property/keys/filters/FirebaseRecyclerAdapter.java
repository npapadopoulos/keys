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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Setter;
import timber.log.Timber;

import static com.google.android.gms.common.util.CollectionUtils.isEmpty;
import static java.util.stream.Collectors.toList;

@RequiresApi(api = Build.VERSION_CODES.R)
public abstract class FirebaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements FirebaseAdapter<T>, Filterable {
    private static final String TAG = "FirebaseRecyclerAdapter";

    private ObservableSnapshotArray<T> mSnapshots;
    protected List<T> filteredSnapshots;
    private Filter filter;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     */
    public FirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions<T> options) {
        mSnapshots = options.getSnapshots();

        filteredSnapshots = isEmpty(options.getSnapshots()) ? new ArrayList<>() : new ArrayList<>(options.getSnapshots());

        if (options.getOwner() != null) {
            options.getOwner().getLifecycle().addObserver(this);
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

        switch (type) {
            case ADDED:
                filteredSnapshots.add(newIndex, mSnapshots.get(newIndex));
                notifyItemInserted(newIndex);
                break;
            case CHANGED:
                //FIXME when unliked a property in filtered state, an REMOVED event type should be fired after of CHANGED event type
                filteredSnapshots.remove(newIndex);
                filteredSnapshots.add(newIndex, mSnapshots.get(newIndex));
                notifyItemChanged(newIndex);
                break;
            case REMOVED:
                filteredSnapshots.remove(newIndex);
                notifyItemRemoved(newIndex);
                break;
            case MOVED:
                filteredSnapshots.remove(oldIndex);
                filteredSnapshots.add(newIndex, mSnapshots.get(newIndex));
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
        return filteredSnapshots.get(position);
    }

    @NonNull
    @Override
    public DatabaseReference getRef(int position) {
        return mSnapshots.getSnapshot(position).getRef();
    }

    @Override
    public int getItemCount() {
        return mSnapshots.isListening(this) ? filteredSnapshots.size() : 0;
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

    public void removeItem(int position) {
        getRef(position).removeValue();
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
            filteredSnapshots.clear();
            filteredSnapshots.addAll(getSnapshots());

            List<T> filtered = new ArrayList<>();
            if (charSequence == null || charSequence.toString().isEmpty()) {
                filtered.addAll(filteredSnapshots);
            } else {
                filtered.addAll(filteredSnapshots.stream()
                        .filter(model -> filterCondition(model, charSequence.toString()))
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
            filteredSnapshots.clear();
            filteredSnapshots.addAll((Collection<? extends T>) filterResults.values);
            notifyDataSetChanged();
        }
    }
}

