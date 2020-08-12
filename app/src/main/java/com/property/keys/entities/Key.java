package com.property.keys.entities;

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
public class Key {
    private String id;
    private String name;

    //TODO https://firebase.google.com/docs/database/android/read-and-write#save_data_as_transactions
    //when checkin/out the key
}
