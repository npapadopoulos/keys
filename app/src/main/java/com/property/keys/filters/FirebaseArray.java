package com.property.keys.filters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This class implements a collection on top of a Firebase location.
 */
public class FirebaseArray<T> extends ObservableSnapshotArray<T>
        implements ChildEventListener, ValueEventListener {
    private final List<DataSnapshot> mSnapshots = new ArrayList<>();
    private final List<String> idsToExclude;
    private Query mQuery;

    /**
     * Create a new FirebaseArray with a custom {@link SnapshotParser}.
     *
     * @param query        The Firebase location to watch for data changes. Can also be a slice of a
     *                     location, using some combination of {@code limit()}, {@code startAt()}, and
     *                     {@code endAt()}.
     * @param idsToExclude
     * @see ObservableSnapshotArray#ObservableSnapshotArray(SnapshotParser)
     */
    public FirebaseArray(@NonNull Query query, @NonNull SnapshotParser<T> parser, String... idsToExclude) {
        super(parser);
        mQuery = query;
        this.idsToExclude = Arrays.asList(idsToExclude);
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mQuery.addChildEventListener(this);
        mQuery.addValueEventListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQuery.removeEventListener((ValueEventListener) this);
        mQuery.removeEventListener((ChildEventListener) this);
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildKey) {
        if (idsToExclude == null || idsToExclude.isEmpty() || !idsToExclude.contains(snapshot.getKey())) {
            int index = 0;
            if (previousChildKey != null) {
                index = getIndexForKey(previousChildKey) + 1;
            }

            mSnapshots.add(index, snapshot);
            notifyOnChildChanged(ChangeEventType.ADDED, snapshot, index, -1);
        }
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildKey) {
        int index = getIndexForKey(snapshot.getKey());

        mSnapshots.set(index, snapshot);
        notifyOnChildChanged(ChangeEventType.CHANGED, snapshot, index, -1);
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
        int index = getIndexForKey(snapshot.getKey());

        mSnapshots.remove(index);
        notifyOnChildChanged(ChangeEventType.REMOVED, snapshot, index, -1);
    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildKey) {
        int oldIndex = getIndexForKey(snapshot.getKey());
        mSnapshots.remove(oldIndex);

        int newIndex = previousChildKey == null ? 0 : getIndexForKey(previousChildKey) + 1;
        mSnapshots.add(newIndex, snapshot);

        notifyOnChildChanged(ChangeEventType.MOVED, snapshot, newIndex, oldIndex);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        notifyOnDataChanged();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        notifyOnError(error);
    }

    private int getIndexForKey(@NonNull String key) {
        int index = 0;
        for (DataSnapshot snapshot : mSnapshots) {
            if (snapshot.getKey().equals(key)) {
                return index;
            } else {
                index++;
            }
        }
        return index - 1;
    }

    @NonNull
    @Override
    protected List<DataSnapshot> getSnapshots() {
        return mSnapshots;
    }
}