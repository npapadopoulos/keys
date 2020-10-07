package com.property.keys.adapters;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.property.keys.R;
import com.property.keys.entities.HistoryDetails;
import com.property.keys.utils.ImageUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class HistoryHolder extends RecyclerView.ViewHolder implements Holder {

    private ImageView propertyImage;
    private TextView checkInDate, description;
    private RelativeLayout historyForeground;

    public HistoryHolder(@NonNull View itemView) {
        super(itemView);

        historyForeground = itemView.findViewById(R.id.historyForeground);
        propertyImage = itemView.findViewById(R.id.propertyImage);
        checkInDate = itemView.findViewById(R.id.checkInDate);
        description = itemView.findViewById(R.id.description);
    }

    public void bind(@NonNull Context context, @NonNull HistoryDetails historyDetails) {
        if (historyDetails.getKey().getCheckedInDate() == null) {
            checkInDate.setText(historyDetails.getKey().getCheckedOutDate());
        } else {
            checkInDate.setText(historyDetails.getKey().getCheckedInDate());
        }
        description.setText(historyDetails.getDescription());
        ImageUtils.syncAndloadImagesProperty(context, historyDetails.getKey().getPropertyId(), propertyImage, false);
    }

    @Override
    public RelativeLayout getBackground() {
        return null;
    }

    @Override
    public RelativeLayout getRestoreBackground() {
        return null;
    }

    @Override
    public RelativeLayout getForeground() {
        return historyForeground;
    }
}
