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
    private String checkedInDate;
    private String checkinReason;
    private String checkedOutDate;
    private String estimatedCheckinDate;
    private String lastCheckOutDate;
    private String lastCheckedInUser;
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
        checkedInDate = in.readString();
        checkinReason = in.readString();
        checkedOutDate = in.readString();
        estimatedCheckinDate = in.readString();
        lastCheckOutDate = in.readString();
        lastCheckedInUser = in.readString();
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
        parcel.writeString(checkedInDate);
        parcel.writeString(checkinReason);
        parcel.writeString(checkedOutDate);
        parcel.writeString(estimatedCheckinDate);
        parcel.writeString(lastCheckOutDate);
        parcel.writeString(lastCheckedInUser);
        parcel.writeString(location);
        parcel.writeString(purpose);
    }
}
