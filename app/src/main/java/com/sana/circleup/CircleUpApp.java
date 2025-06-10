package com.sana.circleup;

// ### Basic Android Imports ###

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;


import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.onesignal.user.subscriptions.PushSubscriptionChangedState;

import java.util.HashMap;
import java.util.Map;
// ### End OneSignal Imports ###


// Yeh class aapki application context provide karti hai aur
// app ke start hone par code run karne ki jagah hai (jaise OneSignal initialization).
public class CircleUpApp extends Application { // ### Class Ka Naam CircleUpApp Hai ###

    // TAG for logging purposes
    private static final String TAG = "CircleUpApp";
    private DatabaseReference rootRef;

    // ### YAHAN APNI ASLI OneSignal App ID DAALEIN ###
    // Yeh App ID aapko OneSignal website par 'Install and Test' step par nazar aai thi.
    // Isay double quotes ("...") ke andar rakhein.
    // Jo aapne screenshot mein dikhai thi, wahi ID yahan use ho rahi hai.
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <-- Apni Asli App ID Replace Karein!


    // Jab app process start hota hai, yeh method sabse pehle call hoti hai.
    @Override
    public void onCreate() {
        super.onCreate(); // ### YEH LINE HAMESHA SABSE PEHLE AATI HAI ###
        Log.d(TAG, "Application onCreate: Started.");

        // Initialize Firebase reference here
        rootRef = FirebaseDatabase.getInstance().getReference(); // Initialize rootRef


// ---

        // ### OneSignal Initialization Code Yahan Add Karein ###
        Log.d(TAG, "Application onCreate: Starting OneSignal initialization.");

        // Optional: Enable Logging (Debugging ke liye)
        // v5.x+ mein logging getDebug() ke through hoti hai.
        try {
            OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE); // VERBOSE sabse detailed log level hai
            Log.d(TAG, "OneSignal logging level set to VERBOSE.");
        } catch (Exception e) {
            // Agar LogLevel import ya setLogLevel mein koi masla aaye to yahan catch hoga.
            Log.w(TAG, "Failed to set OneSignal log level", e);
        }


        // ### OneSignal SDK Initialize Karein - YEH LINE BOOHAT AHAM HAI AUR LOGGING KE BAAD AATI HAI ###
        // Official docs ke mutabiq, App ID seedha initWithContext() mein pass hoti hai.
        // 'this' refers to the Application context
        try {
            // Use your actual ONESIGNAL_APP_ID here
            OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
            Log.d(TAG, "OneSignal.initWithContext called with App ID: " + ONESIGNAL_APP_ID);
            // OneSignal ab initialize ho gaya hai. Yeh ab device ka push token (registration ID)
            // aur OneSignal Player ID automatic handle karega aur OneSignal server par bhejega.
            // Ab aap iske baad dusre OneSignal configurations jaise Handlers set kar sakte hain.
            OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE); // Optional: Keep logging for debugging


        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignal SDK", e);
            // Yeh ek critical error hai. Agar OneSignal initialize na ho to notifications kaam nahi karengi.
            // Aap yahan koi specific error handling add kar sakte hain agar zaroori ho.
        }

        // ### Optional: Push Notification Permission Prompt Karein (Android 13+) ###
        // Agar aapki targetSdkVersion 33 (Android 13) ya uss se zyada hai, aapko user se explicit permission leni hogi.
        // Documentation wala standard tareeqa jo ab aapke liye kaam kar raha hai:
        try {
            Log.d(TAG, "Attempting to call OneSignal.getNotifications().requestPermission.");
            // requestPermission(false, ...) suggests NOT to prompt user automatically on app open.
            // You might want to use 'true' to prompt on first app open OR tie it to a button click later.
            // false ka matlab hai ke yeh line khud prompt nahi karegi, yeh sirf OneSignal ko prepare karegi.
            // Aapko notification permission khud Android API se handle karni paregi ya 'true' use karna parega prompt ke liye.
            // Agar aap chahte hain ke OneSignal khud handle kare (Android 13+ prompt), toh 'true' use karein.
            // For now, let's keep it as 'false' as per the documentation snippet you shared,
            // assuming you might handle the actual prompt later or OneSignal does it implicitly elsewhere.

            // ** Using the documentation's approach which seems to be resolving now **
            OneSignal.getNotifications().requestPermission(true, Continue.none());
            Log.d(TAG, "OneSignal.getNotifications().requestPermission called.");


        } catch (Exception e) { // Catch general Exception in case of unexpected issues
            Log.e(TAG, "Failed to call OneSignal.getNotifications().requestPermission", e);
        }
        // ### OneSignal Initialization Code Yahan Khatam Hota Hai ###


        // *** NEW CODE START: Add Push Subscription Observer ***
        // This observer will fire whenever the push subscription state changes,
        // including when the Player ID becomes available.


    }

}