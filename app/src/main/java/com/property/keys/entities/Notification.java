package com.property.keys.entities;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.IgnoreExtraProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@IgnoreExtraProperties
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiresApi(api = Build.VERSION_CODES.R)
public class Notification implements Parcelable {

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    private String id;
    private String userId;
    private String propertyId;
    private String firstName;
    private String lastName;
    private String description;
    private String date;
    private Action action;
    private Boolean unread = Boolean.TRUE;

    protected Notification(Parcel in) {
        id = in.readString();
        userId = in.readString();
        propertyId = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        description = in.readString();
        date = in.readString();
        action = Action.valueOf(in.readString());
        unread = in.readBoolean();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(userId);
        parcel.writeString(propertyId);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(description);
        parcel.writeString(date);
        parcel.writeString(action.name());
        parcel.writeBoolean(unread);
    }
}
