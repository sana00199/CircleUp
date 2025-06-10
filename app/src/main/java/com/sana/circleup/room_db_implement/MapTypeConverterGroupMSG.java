package com.sana.circleup.room_db_implement;


import android.util.Log;

import androidx.room.TypeConverter;
import com.google.gson.Gson; // Requires Gson library dependency
import com.google.gson.reflect.TypeToken; // Requires Gson library dependency
import java.lang.reflect.Type; // Import Type
import java.util.Collections; // Import Collections
import java.util.Map; // Import Map
import java.util.HashMap; // Import HashMap

// Add Gson dependency to your app's build.gradle (app module) file:
// implementation 'com.google.code.gson:gson:2.10.1' // Use a recent version

public class MapTypeConverterGroupMSG { // Use the name you provided

    private static final Gson gson = new Gson();

    // Converts a JSON string from the database to a Map<String, Boolean> object
    @TypeConverter
    public static Map<String, Boolean> stringToMap(String value) {
        if (value == null) {
            // Return empty map if the database value is null or empty string
            return new HashMap<>(); // Return empty map instead of null for safety
        }
        // Define the type token for Map<String, Boolean>
        Type mapType = new TypeToken<Map<String, Boolean>>() {}.getType();
        try {
            return gson.fromJson(value, mapType); // Parse JSON string into map
        } catch (Exception e) {
            // Handle parsing errors - maybe log a warning or return default
            Log.e("MapTypeConverter", "Error converting string to Map<String, Boolean>", e);
            return new HashMap<>(); // Return empty map on parsing error
        }
    }

    // Converts a Map<String, Boolean> object to a JSON string for database storage
    @TypeConverter
    public static String mapToString(Map<String, Boolean> map) {
        if (map == null) {
            return null; // Store null in DB if the map is null
            // Or return "{}" if you prefer an empty JSON object string in DB for consistency
            // return gson.toJson(new HashMap<>());
        }
        return gson.toJson(map); // Convert map to JSON string
    }
}