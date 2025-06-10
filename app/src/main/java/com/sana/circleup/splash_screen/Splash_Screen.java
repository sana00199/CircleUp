package com.sana.circleup.splash_screen;

import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sana.circleup.MainActivity;
import com.sana.circleup.R;
import com.sana.circleup.Welcome_Screen;
import com.sana.circleup.Login; // Import Login


import android.animation.ObjectAnimator; // Import ObjectAnimator
import android.animation.Animator; // Import Animator interface
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView; // Import ImageView

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//
//public class Splash_Screen extends AppCompatActivity {
//
//    private static final String TAG = "Splash_Screen";
//    private static final int SPLASH_DURATION = 4000; // milliseconds (Minimum display time for splash)
//
//    private static final String PREFS_NAME = "CircleUpPrefs";
//    private static final String PREF_REMEMBER_ME = "RememberMe";
//
//    private FirebaseAuth auth;
//    private SharedPreferences sharedPreferences;
//
//    // *** NEW: Member variable for the animator ***
//    private ObjectAnimator dotBlinkAnimator;
//    // *** END NEW ***
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash_screen); // Ensure this is your splash screen layout
//        Log.d(TAG, "🟢 Splash_Screen onCreate.");
//
//        auth = FirebaseAuth.getInstance();
//        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//
//        // *** NEW: Find the ImageView and start animation ***
//        ImageView splashImageView = findViewById(R.id.splash_image); // *** Use the correct ID from your layout ***
//
//        if (splashImageView != null) {
//            Log.d(TAG, "Starting dot blinking animation on splash_image.");
//            // Create an ObjectAnimator for the 'alpha' property
//            // Animates alpha from 1.0f (fully visible) to 0.2f (partially visible)
//            // The animation will reverse, fading back from 0.2f to 1.0f
//            dotBlinkAnimator = ObjectAnimator.ofFloat(splashImageView, "alpha", 1.0f, 0.2f);
//            dotBlinkAnimator.setDuration(800); // Duration of one fade cycle (adjust as needed)
//            dotBlinkAnimator.setRepeatCount(ObjectAnimator.INFINITE); // Repeat indefinitely
//            dotBlinkAnimator.setRepeatMode(ObjectAnimator.REVERSE); // Reverse the animation direction on repeat
//
//            // Start the animation
//            dotBlinkAnimator.start();
//
//            // Optional: Add a listener for debugging or specific actions
//             /*
//             dotBlinkAnimator.addListener(new Animator.AnimatorListener() {
//                 @Override public void onAnimationStart(Animator animation) { Log.d(TAG, "Blink animation started."); }
//                 @Override public void onAnimationEnd(Animator animation) {} // Not called with infinite repeat
//                 @Override public void onAnimationCancel(Animator animation) { Log.d(TAG, "Blink animation cancelled."); }
//                 @Override public void onAnimationRepeat(Animator animation) {} // Called every repeat
//             });
//             */
//
//        } else {
//            Log.e(TAG, "ImageView with ID 'splash_image' not found in layout!");
//        }
//        // *** END NEW ***
//
//
//        // --- Your existing navigation logic (keep this) ---
//        // This Handler ensures the splash screen is displayed for at least SPLASH_DURATION
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            Log.d(TAG, "Splash_Screen: postDelayed finished. Checking navigation.");
//            FirebaseUser currentUser = auth.getCurrentUser();
//            boolean isRemembered = sharedPreferences.getBoolean(PREF_REMEMBER_ME, false); // Read "Remember Me" state
//
//            Log.d(TAG, "Splash_Screen: currentUser: " + (currentUser != null ? currentUser.getUid() : "null"));
//            Log.d(TAG, "Splash_Screen: isRemembered: " + isRemembered);
//
//
//            if (currentUser != null) {
//                // Firebase user is authenticated.
//                if (isRemembered) {
//                    // User is authenticated AND RememberMe was checked last time.
//                    // Go to MainActivity. MainActivity will attempt local key load.
//                    Log.d(TAG, "Splash_Screen: User authenticated and remembered. Navigating to MainActivity.");
//                    Intent mainIntent = new Intent(Splash_Screen.this, MainActivity.class);
//                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Add flags for clean stack
//                    startActivity(mainIntent);
//                } else {
//                    // User is authenticated but RememberMe was NOT checked last time.
//                    // Firebase session remains active, but local RememberMe state is off.
//                    // Navigate to Login. Login activity will handle checking Firebase auth state.
//                    Log.d(TAG, "Splash_Screen: User authenticated but NOT remembered. Navigating to Login.");
//                    // No need to manually signOut here based on RememberMe state. Login activity
//                    // checks Firebase Auth state first. If user is already logged in but RememberMe
//                    // is false, Login will proceed to handle keys/navigation based on that state.
//                    startActivity(new Intent(Splash_Screen.this, Login.class)); // Go to Login
//                }
//            } else {
//                // No Firebase user is authenticated. Go to Welcome Screen (or Login).
//                // Based on your previous code, navigating to Welcome_Screen seems appropriate if it exists.
//                // If Welcome_Screen just has Login/Signup buttons, you could directly go to Login.
//                Log.d(TAG, "Splash_Screen: No user authenticated. Navigating to Welcome Screen.");
//                startActivity(new Intent(Splash_Screen.this, Welcome_Screen.class)); // Ensure Welcome_Screen exists
//            }
//
//            // Finish splash screen so user cannot go back
//            finish();
//            Log.d(TAG, "Splash_Screen Activity finished.");
//
//        }, SPLASH_DURATION); // Delay based on SPLASH_DURATION
//        // --- End existing navigation logic ---
//
//        Log.d(TAG, "✅ onCreate finished in Splash_Screen.");
//    }
//
//    // *** NEW: Override onDestroy to cancel the animator ***
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "🔴 Splash_Screen onDestroy called.");
//        // Cancel the animator to stop it and prevent memory leaks
//        if (dotBlinkAnimator != null) {
//            dotBlinkAnimator.cancel(); // Cancel the animation
//            dotBlinkAnimator = null; // Release the reference
//            Log.d(TAG, "Blink animator cancelled and released in onDestroy.");
//        }
//    }
//    // *** END NEW ***
//}





public class Splash_Screen extends AppCompatActivity { // Keep this name as it's likely the LAUNCHER

    private static final String TAG = "Splash_Screen";
    // Duration for *this* initial splash screen
    private static final int INITIAL_SPLASH_DURATION = 3000; // Adjust time as needed

    // Remove Shared Preferences fields if not used here anymore
    // private static final String PREFS_NAME = "CircleUpPrefs";
    // private static final String PREF_REMEMBER_ME = "RememberMe";

    // Remove Firebase auth field if not used here anymore
    // private FirebaseAuth auth;
    // private SharedPreferences sharedPreferences;


    // Member variable for the animator (remains here for the first splash)
    private ObjectAnimator dotBlinkAnimator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen); // Use your first splash screen layout
        Log.d(TAG, "🟢 Splash_Screen onCreate.");

        // Remove Firebase and SharedPreferences initialization if not used here
        // auth = FirebaseAuth.getInstance();
        // sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);


        // --- Find the ImageView and start animation (Remains the same) ---
        ImageView splashImageView = findViewById(R.id.splash_image); // Use the correct ID for the purple logo

        if (splashImageView != null) {
            Log.d(TAG, "Starting dot blinking animation on splash_image.");
            dotBlinkAnimator = ObjectAnimator.ofFloat(splashImageView, "alpha", 1.0f, 0.2f);
            dotBlinkAnimator.setDuration(800); // Match duration from previous step if desired
            dotBlinkAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            dotBlinkAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            dotBlinkAnimator.start();
            // Optional listener remains commented or used as needed
        } else {
            Log.e(TAG, "ImageView with ID 'splash_image' not found in layout!");
        }
        // --- End Animation Setup ---


        // --- Modified Navigation Logic ---
        // After this splash screen's duration, navigate to the *second* splash screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Splash_Screen: postDelayed finished. Navigating to Splash_Screen_Transition.");
            // Start the second splash screen activity
            Intent transitionIntent = new Intent(Splash_Screen.this, Splash_Screen_Transition.class); // Start the NEW Activity
            // Flags are usually not needed when going from one splash to another,
            // as the first will be finished below.
            startActivity(transitionIntent);

            // Finish *this* first splash screen activity
            finish();
            Log.d(TAG, "Splash_Screen Activity finished.");

        }, INITIAL_SPLASH_DURATION); // Use the duration for THIS initial splash screen
        // --- End Modified Navigation Logic ---


        Log.d(TAG, "✅ onCreate finished in Splash_Screen.");
    }

    // --- Override onDestroy to cancel the animator (Remains the same) ---
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔴 Splash_Screen onDestroy called.");
        if (dotBlinkAnimator != null) {
            dotBlinkAnimator.cancel();
            dotBlinkAnimator = null;
            Log.d(TAG, "Blink animator cancelled and released in onDestroy.");
        }
    }
    // --- End onDestroy ---
}