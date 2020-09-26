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
public class Notification {

    private String id;
    private String userId;
    private String propertyId;
    private String firstName;
    private String lastName;
    private String description;
    private String date;
    private Action action;
    private Boolean unread = Boolean.TRUE;
}
