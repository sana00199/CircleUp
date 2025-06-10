package com.sana.circleup.notifications;

import android.content.Context;
import android.content.SharedPreferences;

public class NotifiedMessageStore {
    private static final String PREF_NAME = "notified_messages";

    public static boolean isAlreadyNotified(Context context, String messageId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(messageId, false);
    }

    public static void markAsNotified(Context context, String messageId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(messageId, true).apply();
    }
}
