//package com.sana.circleup.room_db_implement;
//
//import android.content.Context;
//import android.content.Context;
//import androidx.room.Database;
//import androidx.room.Room;
//import androidx.room.RoomDatabase;
//
//@Database(entities = {ChatEntity.class}, version = 5)
//public abstract class ChatDatabase extends RoomDatabase {
//    public static ChatDatabase getDatabase(Context context) {
//        return Room.databaseBuilder(context.getApplicationContext(),
//                        ChatDatabase.class, "chat_database")
//                .fallbackToDestructiveMigration()
//                .build();
//    }
//
//    public abstract ChatDao chatDao();
//
//    private static ChatDatabase instance;
//
//    public static synchronized ChatDatabase getInstance(Context context) {
//        if (instance == null) {
//            instance = Room.databaseBuilder(context.getApplicationContext(),
//                            ChatDatabase.class, "chat_database")
//                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)  // Optional
//                    .fallbackToDestructiveMigration()
//                    .build();
//        }
//        return instance;
//    }
//}

package com.sana.circleup.room_db_implement;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Update the entities list and increment the version
@Database(entities = {ChatEntity.class,WallpaperEntity.class, UserEntity.class,ContactEntity.class, MessageEntity.class, ConversationKeyEntity.class, GroupEntity.class, TemporaryRoomEntity.class, GroupMessageEntity.class}, version = 24, exportSchema = false) // <<< Increment version and add GroupMessageEntity
@TypeConverters({ChatDatabase.Converters.class}) // Reference the nested Converters class
public abstract class ChatDatabase extends RoomDatabase {

    // Existing DAOs
    public abstract ChatDao chatDao();
    public abstract WallpaperDao wallpaperDao(); // *** NEW: Abstract method for WallpaperDao ***
    // Add abstract method for the new DAO
    public abstract GroupMessageDao groupMessageDao(); // <<< ADD THIS LINE


    // Add abstract method for the new DAO
    public abstract UserDao userDao(); // <<< ADD THIS LINE


    // Add abstract methods for the new DAO
    public abstract ContactDao contactDao(); // <<< ADD THIS LINE
    public abstract MessageDao messageDao();
    public abstract GroupListDao groupListDao();



    // --- Add the new ConversationKeyDao ---
    public abstract ConversationKeyDao conversationKeyDao(); // <<< Add this line

    private static volatile ChatDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static ChatDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ChatDatabase.class, "chat_database")
                            // Use destructive migration during development
                            .fallbackToDestructiveMigration() // <<< You will need to increment version every time you change schema
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // --- Type Converters for Room ---
    // Add this nested static class
    public static class Converters {

        // Converter for Map<String, Boolean> (used by readBy field in GroupMessageEntity)
        @TypeConverter
        public static Map<String, Boolean> fromStringToMap(String value) {
            if (value == null) {
                return Collections.emptyMap(); // Return empty map for null
            }
            // Use Gson to deserialize the JSON string back to a Map
            Type listType = new TypeToken<Map<String, Boolean>>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromMapToString(Map<String, Boolean> map) {
            if (map == null) {
                return null; // Store null if the map is null
            }
            // Use Gson to serialize the Map to a JSON string
            return new Gson().toJson(map);
        }

        // Add other TypeConverters here if you have custom types (e.g., List of Strings)
        // Example: for List<String>
        /*
        @TypeConverter
        public static List<String> fromStringToListString(String value) {
             if (value == null) {
                return Collections.emptyList();
            }
            Type listType = new TypeToken<List<String>>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromListStringToString(List<String> list) {
             if (list == null) {
                return null;
            }
            return new Gson().toJson(list);
        }
        */
    }
}