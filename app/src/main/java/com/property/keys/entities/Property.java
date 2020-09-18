package com.property.keys.entities;


import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
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
@RequiresApi(api = Build.VERSION_CODES.Q)
public class Property implements Parcelable {

    public static final Creator<Property> CREATOR = new Creator<Property>() {
        @Override
        public Property createFromParcel(Parcel in) {
            return new Property(in);
        }

        @Override
        public Property[] newArray(int size) {
            return new Property[size];
        }
    };

    private String id;
    private String name;
    private String address;
    private boolean deleted;
    private Map<String, Key> keys = new HashMap<>();
    private Map<String, Object> favouredBy = new HashMap<>();

    protected Property(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        deleted = in.readBoolean();
        keys = in.readHashMap(Key.class.getClassLoader());
        favouredBy = in.readHashMap(String.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeBoolean(deleted);
        parcel.writeMap(keys == null ? new HashMap<>() : keys);
        parcel.writeMap(favouredBy == null ? new HashMap<>() : favouredBy);
    }
}
