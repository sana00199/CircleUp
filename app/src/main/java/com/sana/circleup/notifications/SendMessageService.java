package com.sana.circleup.notifications;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class SendMessageService extends JobIntentService {
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String senderName = intent.getStringExtra("senderName");
        String message = intent.getStringExtra("message");
        String senderId = intent.getStringExtra("senderId");

        // Show the notification
        LocalNotificationHelper notificationHelper = new LocalNotificationHelper();
        notificationHelper.showNotification(getApplicationContext(), senderName, message, senderId);
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SendMessageService.class, 1000, work);
    }
}
