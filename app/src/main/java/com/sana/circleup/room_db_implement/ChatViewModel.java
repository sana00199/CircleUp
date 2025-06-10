package com.sana.circleup.room_db_implement;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private ChatDao chatDao;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        ChatDatabase db = ChatDatabase.getInstance(application);
        chatDao = db.chatDao();
    }

//    public LiveData<List<ChatEntity>> getMessagesForUser(String userId) {
//        return chatDao.getMessagesForUser(userId);
//    }
}
