package com.property.keys.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

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
    private String checkedInDate;
    private String checkedOutDate;
    private String lastCheckOutDate;

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
    private Map<String, Object> favouredBy;

    protected Key(Parcel in) {
        id = in.readString();
        checkedInDate = in.readString();
        checkedOutDate = in.readString();
        lastCheckOutDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(checkedInDate);
        parcel.writeString(checkedOutDate);
        parcel.writeString(lastCheckOutDate);
    }
}
