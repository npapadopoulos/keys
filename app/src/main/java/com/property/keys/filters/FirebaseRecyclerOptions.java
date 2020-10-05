package com.property.keys.filters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.Query;

/**
 * Options to configure a {@link FirebaseRecyclerAdapter}.
 *
 * @see FirebaseRecyclerOptions.Builder
 */
public final class FirebaseRecyclerOptions<T> {

    private final ObservableSnapshotArray<T> mSnapshots;
    private final LifecycleOwner mOwner;

    private FirebaseRecyclerOptions(ObservableSnapshotArray<T> snapshots,
                                    @Nullable LifecycleOwner owner) {
        mSnapshots = snapshots;
        mOwner = owner;
    }

    /**
     * Get the {@link ObservableSnapshotArray} to listen to.
     */
    @NonNull
    public ObservableSnapshotArray<T> getSnapshots() {
        return mSnapshots;
    }

    /**
     * Get the (optional) LifecycleOwner. Listening will start/stop after the appropriate lifecycle
     * events.
     */
    @Nullable
    public LifecycleOwner getOwner() {
        return mOwner;
    }

    /**
     * Builder for a {@link FirebaseRecyclerOptions}.
     *
     * @param <T> the model class for the {@link FirebaseRecyclerAdapter}.
     */
    public static final class Builder<T> {

        private ObservableSnapshotArray<T> mSnapshots;
        private LifecycleOwner mOwner;
        private String id;

        /**
         * Set the Firebase query to listen to, along with a {@link SnapshotParser} to parse
         * snapshots into model objects.
         * <p>
         */
        @NonNull
        public FirebaseRecyclerOptions.Builder<T> setQuery(@NonNull Query query,
                                                           @NonNull SnapshotParser<T> snapshotParser,
                                                           String... idsToExclude) {
            mSnapshots = new FirebaseArray<>(query, snapshotParser, idsToExclude);
            return this;
        }

        /**
         * Set the Firebase query to listen to, along with a {@link Class} to which snapshots should
         * be parsed.
         * <p>
         */
        @NonNull
        public FirebaseRecyclerOptions.Builder<T> setQuery(@NonNull Query query, @NonNull Class<T> modelClass,
                                                           String... idsToExclude) {
            return setQuery(query, new ClassSnapshotParser<>(modelClass), idsToExclude);
        }

        /**
         * Set the (optional) {@link LifecycleOwner}. Listens will start and stop after the
         * appropriate lifecycle events.
         */
        @NonNull
        public FirebaseRecyclerOptions.Builder<T> setLifecycleOwner(@Nullable LifecycleOwner owner) {
            mOwner = owner;
            return this;
        }

        /**
         * Build a {@link FirebaseRecyclerOptions} from the provided arguments.
         */
        @NonNull
        public FirebaseRecyclerOptions<T> build() {
            return new FirebaseRecyclerOptions<>(mSnapshots, mOwner);
        }
    }

}
