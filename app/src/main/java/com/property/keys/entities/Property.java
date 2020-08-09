package com.property.keys.entities;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    private String id;
    private String name;
    private String address;

    //TODO
    // Best practices for data structure
    // Avoid nesting data
    /**
     * Flatten data structures
     * If the data is instead split into separate paths, also called denormalization,
     * it can be efficiently downloaded in separate calls, as it is needed. Consider this flattened structure:
     */
    // https://firebase.google.com/docs/database/android/structure-data
    private List<Key> keys;
}
