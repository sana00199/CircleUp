package com.sana.circleup.room_db_implement;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MessageEntity.class}, version = 1, exportSchema = false)
public abstract class MessageDatabase extends RoomDatabase {

    private static volatile MessageDatabase INSTANCE;

    public abstract MessageDao messageDao();

    public static MessageDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MessageDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    MessageDatabase.class, "circleup_messages_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
