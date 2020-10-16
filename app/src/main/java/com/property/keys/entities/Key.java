package com.property.keys.entities;

import android.os.Parcel;
import android.os.Parcelable;

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
public class Key implements Parcelable {
    private String id;
    private String propertyId;
    private String propertyName;
    private String checkedInDate;
    private String checkInReason;
    private String checkedOutDate;
    private String estimatedCheckOutDate;
    private String lastCheckOutDate;
    private String lastCheckedInUser;
    private String lastCheckedInUserId;
    private String location;
    private String purpose;

    public static final Parcelable.Creator<Key> CREATOR = new Parcelable.Creator<Key>() {
        @Override
        public Key createFromParcel(Parcel in) {
            return new Key(in);
        }

        @Override
        public Key[] newArray(int size) {
            return new Key[size];
        }
    };

    protected Key(Parcel in) {
        id = in.readString();
        propertyId = in.readString();
        propertyName = in.readString();
        checkedInDate = in.readString();
        checkInReason = in.readString();
        checkedOutDate = in.readString();
        estimatedCheckOutDate = in.readString();
        lastCheckOutDate = in.readString();
        lastCheckedInUser = in.readString();
        lastCheckedInUserId = in.readString();
        location = in.readString();
        purpose = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(propertyId);
        parcel.writeString(propertyName);
        parcel.writeString(checkedInDate);
        parcel.writeString(checkInReason);
        parcel.writeString(checkedOutDate);
        parcel.writeString(estimatedCheckOutDate);
        parcel.writeString(lastCheckOutDate);
        parcel.writeString(lastCheckedInUser);
        parcel.writeString(lastCheckedInUserId);
        parcel.writeString(location);
        parcel.writeString(purpose);
    }
}
