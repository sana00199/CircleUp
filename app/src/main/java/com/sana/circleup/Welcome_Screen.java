package com.sana.circleup;

import android.content.Intent;
import android.content.SharedPreferences; // Zaroori import
import android.content.pm.PackageManager; // Zaroori import
import android.os.Build; // Zaroori import
import android.os.Bundle;
import android.util.Log; // Zaroori import
import android.view.View;
import android.widget.Button;
import android.widget.Toast; // Optional: For user feedback

import androidx.annotation.NonNull; // Zaroori import
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; // Zaroori import
import androidx.core.content.ContextCompat; // Zaroori import

import java.util.ArrayList; // Zaroori import
import java.util.List; // Zaroori import


public class Welcome_Screen extends AppCompatActivity {

    private static final String TAG = "WelcomeScreen"; // Logging tag

    Button btn_login, btn_signup;

    // Permission Request Code (Koi bhi unique integer value)
    private static final int ALL_NECESSARY_PERMISSIONS_REQUEST_CODE = 100;

    // SharedPreferences Key (Pehli baar permission manga hai ya nahi, isko track karne ke liye)
    private static final String PREFS_NAME = "AppPermissionsPrefs";
    private static final String PREF_FIRST_PERMISSION_ASKED_WELCOME = "firstPermissionAskedWelcome";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        btn_login = findViewById(R.id.welcome_login_btn);
        btn_signup = findViewById(R.id.welcome_signup_btn);

        // --- START: ADD THIS PERMISSION CHECK LOGIC ---
        // Check if permissions have been asked for the first time before
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean firstPermissionAsked = prefs.getBoolean(PREF_FIRST_PERMISSION_ASKED_WELCOME, false);

        if (!firstPermissionAsked) {
            // This is likely the first launch after installation (or after data clear)
            Log.d(TAG, "First time launch detected on Welcome Screen. Checking and requesting permissions.");
            checkAndRequestPermissions();
            // Set the flag so we don't ask again on subsequent starts of WelcomeScreen
            prefs.edit().putBoolean(PREF_FIRST_PERMISSION_ASKED_WELCOME, true).apply();
        } else {
            Log.d(TAG, "Permissions already asked before (or this is not the first time on Welcome). Proceeding normally.");
            // Permissions were already asked or granted in a previous session, OR
            // this is a return visit to the welcome screen after the initial launch.
            // Proceed with normal Welcome Screen setup (button listeners are set below).
            // No need to call checkAndRequestPermissions() again unless permissions are missing later in flow.
        }
        // --- END: ADD THIS PERMISSION CHECK LOGIC ---


        // Set click listeners for buttons - these should always be active once the layout is loaded
        // The permission dialog will appear *on top* if needed, and doesn't stop listeners from being set.
        btn_login.setOnClickListener(view -> {
            Log.d(TAG, "Login button clicked. Starting Login Activity.");
            startActivity(new Intent(Welcome_Screen.this, Login.class));
        });

        btn_signup.setOnClickListener(view -> {
            Log.d(TAG, "Signup button clicked. Starting Signup Activity.");
            startActivity(new Intent(Welcome_Screen.this, Signup.class));
        });

        Log.d(TAG, "Welcome Screen onCreate finished.");
    }


    // --- NEW METHOD TO CHECK AND REQUEST NECESSARY PERMISSIONS ---
    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        List<String> permissionsToRequest = new ArrayList<>();

        // 1. Add POST_NOTIFICATIONS for Android 13+ (Crucial for OneSignal)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API level 33
            permissionsNeeded.add(android.Manifest.permission.POST_NOTIFICATIONS);
        }

        // 2. Add Storage/Media permissions (if needed early in the flow)
        // Example: WRITE_EXTERNAL_STORAGE for saving on older Android (<10) - Though less common to need on Welcome
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // API level 28 or below
            // Check WRITE_EXTERNAL_STORAGE only if needed on Welcome/early flow
            // permissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Log.d(TAG, "WRITE_EXTERNAL_STORAGE check skipped for Welcome Screen (API < 29).");
        }
        // Example: READ_MEDIA_IMAGES for selecting photos from gallery on Android 13+ (e.g., for Profile Pic later)
        // Asking here can be good for first-time setup readiness.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API level 33
            permissionsNeeded.add(android.Manifest.permission.READ_MEDIA_IMAGES);
        }
        // Note: Decide based on your app's logic if you need media permissions *this early*.
        // If media permission is only needed for setting profile pic, you might ask for READ_MEDIA_IMAGES
        // specifically on the Setting_profile screen instead of here.
        // However, asking here covers readiness for profile pic and potentially displaying shared media.

        // Check which permissions are NOT granted
        for (String permission : permissionsNeeded) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
                Log.d(TAG, "Permission needs requesting: " + permission);
            } else {
                Log.d(TAG, "Permission already granted: " + permission);
            }
        }

        // If there are permissions to request, ask the user
        if (!permissionsToRequest.isEmpty()) {
            Log.d(TAG, "Initiating permission request dialog for: " + permissionsToRequest.toString());
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    ALL_NECESSARY_PERMISSIONS_REQUEST_CODE
            );
        } else {
            // All necessary permissions (as defined in 'permissionsNeeded') are already granted
            Log.d(TAG, "All necessary permissions already granted. No dialog shown.");
            // Since this is the welcome screen, having permissions granted means we can
            // proceed to show the login/signup options. The button listeners are already set below.
        }
    }

    // --- HANDLE THE PERMISSION REQUEST RESULT ---
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ALL_NECESSARY_PERMISSIONS_REQUEST_CODE) {
            Log.d(TAG, "Received permission request result.");
            boolean allGranted = true; // Assume all granted initially

            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, permissions[i] + " permission GRANTED.");
                        // Permission granted
                    } else {
                        Log.d(TAG, permissions[i] + " permission DENIED.");
                        // Permission denied
                        allGranted = false;
                        // Inform user about denial if needed
                        Toast.makeText(this, permissions[i] + " was denied. Some features may not work.", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // This case is rare but handles if request somehow yielded empty results
                Log.w(TAG, "Permission request result array is empty.");
                // Decide how to handle: maybe assume denied, or just proceed
                // For Welcome, perhaps just proceed but know not all permissions were handled.
            }

            // After processing results, the Welcome screen remains visible.
            // The buttons are already set up in onCreate.
            // You can add logic here if you need to visually change the UI based on denied permissions
            // (e.g., disable features, show warnings).
            Log.d(TAG, "Permission result processing complete. All granted: " + allGranted);
        }
    }
    // --- END OF PERMISSION LOGIC ---

    // ... Rest of your Welcome_Screen code (currently empty below this point) ...
}