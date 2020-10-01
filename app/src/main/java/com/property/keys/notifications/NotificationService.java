package com.property.keys.notifications;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.property.keys.MainActivity;
import com.property.keys.R;
import com.property.keys.entities.Notification;
import com.property.keys.entities.Property;
import com.property.keys.entities.User;
import com.property.keys.utils.UserUtils;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NotificationService extends Service {

    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        User localUser = UserUtils.getLocalUser(getBaseContext());
        if (localUser != null) {
            firebaseDatabase.getReference("users").child(localUser.getId()).child("notifications").orderByChild("unread").equalTo(true)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            snapshot.getChildren().forEach(child -> {
                                Notification notification = child.getValue(Notification.class);
                                if (notification != null && notification.getPropertyId() != null) {
                                    firebaseDatabase.getReference("properties").child(notification.getPropertyId())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    Property property = snapshot.getValue(Property.class);

                                                    if (property != null) {
                                                        Intent propertyDetails = new Intent(getBaseContext(), MainActivity.class);
                                                        propertyDetails.putExtra("selected", "Properties");
                                                        propertyDetails.putExtra("setReadNotification", notification);
                                                        propertyDetails.putExtra("property", property);
                                                        propertyDetails.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, propertyDetails, 0);

                                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), localUser.getId())
                                                                .setSmallIcon(R.drawable.keys)
                                                                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getDescription()))
                                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                                // Set the intent that will fire when the user taps the notification
                                                                .setContentIntent(pendingIntent)
                                                                .setAutoCancel(true);

                                                        @SuppressLint("ServiceCast")
                                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                                        // notificationId is a unique int for each notification that you must define
                                                        notificationManager.notify(1, builder.build());
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}
