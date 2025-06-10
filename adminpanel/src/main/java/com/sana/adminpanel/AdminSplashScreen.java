package com.sana.adminpanel; // <-- Ensure this is the correct package name for your admin module

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper; // <-- Import Looper

import androidx.appcompat.app.AppCompatActivity;

// Assuming your layout R file is accessible from the main app's R or defined in admin module
// Or import com.sana.adminpanel.R; if layout is in admin module res

// Import the next activity in the flow
// Ensure this points to the Login activity within your adminpanel module
// import com.sana.adminpanel.Login; // <-- Make sure this class exists

public class AdminSplashScreen extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the splash screen layout
        setContentView(R.layout.activity_admin_splash_screen); // <-- Ensure this points to your XML

        /* New Handler to start the Admin-Login-Activity
         * and close this splash activity after some seconds.*/
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // <-- Use Looper.getMainLooper()
            @Override
            public void run() {
                /* Create an Intent that will start the Admin-Login-Activity. */
                // Ensure this targets the Login activity *within your adminpanel module*
                Intent mainIntent = new Intent(AdminSplashScreen.this, Login.class); // <-- Navigate to adminpanel.Login
                startActivity(mainIntent);
                finish(); // Close this activity
            }
        }, SPLASH_DISPLAY_LENGTH); // Pass the delay length
    }
}