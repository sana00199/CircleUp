package com.sana.circleup.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sana.circleup.ChatPageActivity;
import com.sana.circleup.R;

//public class LocalNotificationHelper {
//    private static final String CHANNEL_ID = "chat_notifications";
//
//    public static void showNotification(Context context, String senderName, String message) {
//        try {
//            createNotificationChannel(context);
//
//            Intent intent = new Intent(context, ChatPageActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                    .setSmallIcon(R.drawable.logo_front_one)
//                    .setContentTitle(senderName)
//                    .setContentText(message)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setAutoCancel(true)
//                    .setContentIntent(pendingIntent);
//
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//
//            // ✅ Runtime Permission Check (Android 13+)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                    Log.e("LocalNotificationHelper", "Notification permission not granted");
//                    return;
//                }
//            }
//
//            notificationManager.notify(1, builder.build());
//            Log.d("LocalNotificationHelper", "Notification sent successfully!");
//
//        } catch (Exception e) {
//            Log.e("LocalNotificationHelper", "Error sending notification: " + e.getMessage());
//        }
//    }
//
//    private static void createNotificationChannel(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            try {
//                NotificationChannel channel = new NotificationChannel(
//                        CHANNEL_ID, "Chat Messages", NotificationManager.IMPORTANCE_HIGH);
//                NotificationManager manager = context.getSystemService(NotificationManager.class);
//                if (manager != null) {
//                    manager.createNotificationChannel(channel);
//                }
//            } catch (Exception e) {
//                Log.e("LocalNotificationHelper", "Error creating notification channel: " + e.getMessage());
//            }
//        }
//    }
//}





public class LocalNotificationHelper {

    public void showNotification(Context context, String senderName, String message, String senderId) {
        String channelId = "chat_message_channel";

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Chat Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for chat messages");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // Intent to open ChatActivity on notification click
        Intent intent = new Intent(context, ChatPageActivity.class);
        intent.putExtra("userId", senderId); // Sending the sender's ID to open the chat with that user
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo_front_one)  // Set your icon here
                .setContentTitle("Message from " + senderName)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());


    }
}

