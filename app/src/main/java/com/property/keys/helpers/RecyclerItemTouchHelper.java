package com.property.keys.helpers;

import android.graphics.Canvas;
import android.os.Build;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.property.keys.adapters.Holder;

@RequiresApi(api = Build.VERSION_CODES.R)
public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private RecyclerItemTouchHelperListener listener;
    private boolean enableRestore;

    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener, boolean enableRestore) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
        this.enableRestore = enableRestore;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View foregroundView = ((Holder) viewHolder).getForeground();
            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        Holder holder = (Holder) viewHolder;
        RelativeLayout background = holder.getBackground();
        if (enableRestore) {
            RelativeLayout restoreBackground = holder.getRestoreBackground();
            if (dX > 0) {
                background.setVisibility(View.GONE);
                restoreBackground.setVisibility(View.VISIBLE);
                getDefaultUIUtil().onDrawOver(c, recyclerView, restoreBackground, dX, dY, actionState, isCurrentlyActive);
            } else {
                restoreBackground.setVisibility(View.GONE);
                background.setVisibility(View.VISIBLE);
                getDefaultUIUtil().onDrawOver(c, recyclerView, background, dX, dY, actionState, isCurrentlyActive);
            }
        } else {
            background.setVisibility(View.VISIBLE);
            getDefaultUIUtil().onDrawOver(c, recyclerView, background, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((Holder) viewHolder).getForeground();
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((Holder) viewHolder).getForeground();
        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}