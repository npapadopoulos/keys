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
    private String description;
    private String created;
    private Key key;

    protected HistoryDetails(Parcel in) {
        id = in.readString();
        userId = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        description = in.readString();
        created = in.readString();
        key = in.readParcelable(Key.class.getClassLoader());
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
        parcel.writeString(description);
        parcel.writeString(created);
        parcel.writeParcelable(key, 0);
    }
}
