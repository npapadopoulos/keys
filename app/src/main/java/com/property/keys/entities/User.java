package com.property.keys.entities;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiresApi(api = Build.VERSION_CODES.R)
public class User implements Parcelable {

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private Role role = Role.BASIC;
    private boolean remember;
    private Map<String, Notification> notifications = new HashMap<>();
    private List<String> propertySearchSuggestions = new ArrayList<>();

    protected User(Parcel in) {
        id = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        email = in.readString();
        phoneNumber = in.readString();
        password = in.readString();
        role = Role.valueOf(in.readString());
        remember = in.readBoolean();
        notifications = in.readHashMap(String.class.getClassLoader());
        propertySearchSuggestions = in.readArrayList(String.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(email);
        parcel.writeString(phoneNumber);
        parcel.writeString(password);
        parcel.writeString(role.name());
        parcel.writeBoolean(remember);
        parcel.writeMap(notifications == null ? new HashMap<>() : notifications);
        parcel.writeList(propertySearchSuggestions == null ? new ArrayList<>() : propertySearchSuggestions);
    }
}
