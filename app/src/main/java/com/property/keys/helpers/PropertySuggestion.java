package com.property.keys.helpers;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.Objects;

import lombok.Getter;

@Getter
public class PropertySuggestion implements SearchSuggestion {

    public static final Creator<PropertySuggestion> CREATOR = new Creator<PropertySuggestion>() {
        @Override
        public PropertySuggestion createFromParcel(Parcel in) {
            return new PropertySuggestion(in);
        }

        @Override
        public PropertySuggestion[] newArray(int size) {
            return new PropertySuggestion[size];
        }
    };
    private String propertyName;
    private boolean isHistory = false;

    public PropertySuggestion(String suggestion, boolean isHistory) {
        this.propertyName = suggestion.toLowerCase();
        this.isHistory = isHistory;
    }

    public PropertySuggestion(Parcel source) {
        this.propertyName = source.readString();
        this.isHistory = source.readInt() != 0;
    }

    @Override
    public String getBody() {
        return this.propertyName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(propertyName);
        parcel.writeInt(isHistory ? 1 : 0);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertySuggestion that = (PropertySuggestion) o;
        return isHistory == that.isHistory &&
                Objects.equals(propertyName, that.propertyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyName, isHistory);
    }
}
