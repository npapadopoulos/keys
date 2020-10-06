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
public class HistoryDetails implements Parcelable {
    public static final Creator<HistoryDetails> CREATOR = new Creator<HistoryDetails>() {
        @Override
        public HistoryDetails createFromParcel(Parcel in) {
            return new HistoryDetails(in);
        }

        @Override
        public HistoryDetails[] newArray(int size) {
            return new HistoryDetails[size];
        }
    };
    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String propertyId;
    private String propertyName;
    private String keyId;
    private String checkedInDate;
    private String checkedOutDate;
    private String reason;

    protected HistoryDetails(Parcel in) {
        id = in.readString();
        userId = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        propertyId = in.readString();
        propertyName = in.readString();
        keyId = in.readString();
        checkedInDate = in.readString();
        checkedOutDate = in.readString();
        reason = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(userId);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(propertyId);
        parcel.writeString(propertyName);
        parcel.writeString(keyId);
        parcel.writeString(checkedInDate);
        parcel.writeString(checkedOutDate);
        parcel.writeString(reason);
    }
}
