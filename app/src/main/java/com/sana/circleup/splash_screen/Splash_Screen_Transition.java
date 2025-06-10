package com.sana.circleup.splash_screen; // <-- Replace with your actual package name

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.sana.circleup.Login; // Import your Login activity
import com.sana.circleup.MainActivity; // Import your MainActivity
import com.sana.circleup.R;
import com.sana.circleup.Welcome_Screen; // Import your Welcome_Screen activity (if used)
import com.sana.circleup.encryptionfiles.YourKeyManager;
// Import YourKeyManager


public class Splash_Screen_Transition extends AppCompatActivity {

    private static final String TAG = "Splash_Transition";
    // Duration for *this* transition splash screen
    private static final int TRANSITION_SPLASH_DURATION = 1600; // Adjust time as needed (e.g., 1.5 seconds)

    private static final String PREFS_NAME = "CircleUpPrefs";
    private static final String PREF_REMEMBER_ME = "RememberMe";

    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the new transition splash layout
        setContentView(R.layout.activity_splash_screen_transition);
        Log.d(TAG, "🟢 Splash_Screen_Transition onCreate.");

        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // --- Add animation here if you want the WHITE logo to animate ---
        // Example: (Assuming you want a pulse or different effect here)
        /*
        ImageView whiteLogoImageView = findViewById(R.id.splash_image_white);
        if (whiteLogoImageView != null) {
            ObjectAnimator pulseAnimator = ObjectAnimator.ofFloat(whiteLogoImageView, "scaleX", 1.0f, 1.1f, 1.0f);
            pulseAnimator.setDuration(1000);
            pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            // pulseAnimator.setRepeatMode(ObjectAnimator.REVERSE); // Not needed for simple scale pulse
            pulseAnimator.start();
            // Store this animator in a member variable if needed for cancellation in onDestroy
        }
        */
        // --- End optional animation ---


        // --- Your original navigation logic goes HERE ---
        // This handler starts the timer for THIS splash screen's duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Splash_Screen_Transition: postDelayed finished. Checking navigation.");

            // Clear keys from KeyManager just before navigating to ensure clean state for next Activity's load logic
            // This is important because the previous splash screen might have left keys if it was interrupted or logic was changed.
//            YourKeyManager.getInstance().clearKeys();
            Log.d(TAG, "Splash_Screen_Transition: Cleared KeyManager before final navigation.");


            FirebaseUser currentUser = auth.getCurrentUser();
            boolean isRemembered = sharedPreferences.getBoolean(PREF_REMEMBER_ME, false);

            Log.d(TAG, "Splash_Screen_Transition: currentUser: " + (currentUser != null ? currentUser.getUid() : "null"));
            Log.d(TAG, "Splash_Screen_Transition: isRemembered: " + isRemembered);


            if (currentUser != null) {
                // Firebase user is authenticated.
                if (isRemembered) {
                    // User is authenticated AND RememberMe was checked last time.
                    // Go to MainActivity. MainActivity will attempt local key load.
                    Log.d(TAG, "Splash_Screen_Transition: User authenticated and remembered. Navigating to MainActivity.");
                    Intent mainIntent = new Intent(Splash_Screen_Transition.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Use flags to clear previous activities
                    startActivity(mainIntent);
                } else {
                    // User is authenticated but RememberMe was NOT checked last time.
                    // Navigate to Login. Login activity will check Firebase auth state.
                    Log.d(TAG, "Splash_Screen_Transition: User authenticated but NOT remembered. Navigating to Login.");
                    startActivity(new Intent(Splash_Screen_Transition.this, Login.class)); // Go to Login
                }
            } else {
                // No Firebase user is authenticated. Go to Welcome Screen (or Login).
                // Based on your previous code, navigating to Welcome_Screen seems appropriate.
                Log.d(TAG, "Splash_Screen_Transition: No user authenticated. Navigating to Welcome Screen.");
                startActivity(new Intent(Splash_Screen_Transition.this, Welcome_Screen.class)); // Ensure Welcome_Screen exists
            }

            // Finish *this* splash screen so user cannot go back to it or the first splash screen
            finish();
            Log.d(TAG, "Splash_Screen_Transition Activity finished.");

        }, TRANSITION_SPLASH_DURATION); // Use the duration for THIS splash screen

        Log.d(TAG, "✅ onCreate finished in Splash_Screen_Transition.");
    }

    // --- Add onDestroy if you added any animator here ---
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If you added an animator in this class, cancel it here
        /*
        if (pulseAnimator != null) {
             pulseAnimator.cancel();
             pulseAnimator = null;
        }
        */
        Log.d(TAG, "🔴 Splash_Screen_Transition onDestroy called.");
    }
    // --- End onDestroy ---
}