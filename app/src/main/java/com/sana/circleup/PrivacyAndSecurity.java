// In PrivacyAndSecurity.java
// In PrivacyAndSecurity.java, replace the existing code with this, adapting imports and package

package com.sana.circleup; // Adjust package name

import androidx.annotation.NonNull; // Import annotation
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.core.content.ContextCompat; // Import ContextCompat

import android.content.Intent; // Import Intent
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.View; // Import View
import android.widget.LinearLayout; // Import LinearLayout (as item is LinearLayout)
import android.widget.TextView; // Import TextView if you want to reference description text
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sana.circleup.encryptionfiles.YourKeyManager;


public class PrivacyAndSecurity extends AppCompatActivity {

    private static final String TAG = "PrivacyAndSecurity";

    // Declare the clickable LinearLayouts
    private LinearLayout itemUnlockAccount;
    private LinearLayout itemChangePassphrase;
    private LinearLayout itemChangeAuthPassword;
    // *** NEW: Add item for Reset Passphrase ***
    private LinearLayout itemResetPassphrase; // Assuming you add this in your layout
    // *** END NEW ***


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Keep EdgeToEdge if you use it
        setContentView(R.layout.activity_privacy_and_security); // Use your redesigned layout
        Log.d(TAG, "🟢 PrivacyAndSecurity onCreate.");


        // Setup Toolbar as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_privacy_security); // Ensure this ID matches your layout
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.privacy_security_title); // Use string resource
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
            }
        } else {
            Log.w(TAG, "Toolbar not found in layout with ID toolbar_privacy_security");
        }


        // Find the clickable LinearLayout items by their IDs
        itemUnlockAccount = findViewById(R.id.item_unlock_account); // Ensure this ID matches your layout
        itemChangePassphrase = findViewById(R.id.item_change_password); // Ensure this ID matches your layout (This is for Change Passphrase)
        itemChangeAuthPassword = findViewById(R.id.item_change_auth_password); // Ensure this ID matches your layout (This is for Change Account Password)
        // *** NEW: Find the Reset Passphrase item ***
//        itemResetPassphrase = findViewById(R.id.item_reset_passphrase); // Assuming you add this in your layout
        // *** END NEW ***


        // --- Set click listener for the "Unlock Secure Account" item ---
        if (itemUnlockAccount != null) {
            itemUnlockAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "'Unlock Secure Account' item clicked.");

                    // *** ADD CHECK HERE ***
                    // Check if the private key is already available in KeyManager
                    if (YourKeyManager.getInstance().isPrivateKeyAvailable()) {
                        Log.d(TAG, "'Unlock Secure Account' clicked, but account is already unlocked.");
                        // Account is already unlocked, show a toast instead of opening the activity
                        Toast.makeText(PrivacyAndSecurity.this, "Account is already unlocked.", Toast.LENGTH_SHORT).show();
                        // No need to start the activity
                    } else {
                        Log.d(TAG, "'Unlock Secure Account' clicked, account is locked. Opening UnlockActivity.");
                        // Account is locked, proceed to open the Unlock activity
                        Intent intent = new Intent(PrivacyAndSecurity.this, UnlockAccountPassphereActivity.class); // Start UnlockAccountActivity
                        startActivity(intent);
                    }
                    // *** END ADD CHECK ***
                }
            });
        } else {
            Log.e(TAG, "Layout item 'item_unlock_account' not found!");
        }
        // --- End Unlock Secure Account listener ---


        // --- Set click listener for the "Change Passphrase" item ---
        // This should ONLY be available if the private key is available (account is unlocked)
        if (itemChangePassphrase != null) {
            itemChangePassphrase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "'Change Passphrase' item clicked.");

                    // *** ADD CHECK HERE: Only allow changing passphrase if account is unlocked ***
                    if (YourKeyManager.getInstance().isPrivateKeyAvailable()) {
                        Log.d(TAG, "'Change Passphrase' clicked, account is unlocked. Opening ChangePassphraseActivity.");
                        // Account is unlocked, allow changing passphrase
                        Intent intent = new Intent(PrivacyAndSecurity.this, ChangePassphraseActivity.class); // Launch the new activity
                        startActivity(intent);
                    } else {
                        Log.w(TAG, "'Change Passphrase' clicked, but account is LOCKED. Cannot change passphrase.");
                        // Account is locked, show error toast
                        Toast.makeText(PrivacyAndSecurity.this, "Account must be unlocked to change passphrase.", Toast.LENGTH_SHORT).show();
                        // Maybe suggest unlocking first
                        // Toast.makeText(PrivacyAndSecurity.this, "Please unlock your account first.", Toast.LENGTH_SHORT).show();
                    }
                    // *** END ADD CHECK ***
                }
            });
        } else {
            Log.e(TAG, "Layout item 'item_change_password' not found!");
        }
        // --- End Change Passphrase listener ---


        // --- Set click listener for the "Change Account Password" item ---
        // This changes the Firebase Auth password, which doesn't require the secure passphrase to be unlocked.
        // It requires Firebase Auth re-authentication with the CURRENT Firebase password.
        if (itemChangeAuthPassword != null) {
            itemChangeAuthPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "'Change Account Password' item clicked.");
                    // Launch the ChangeAuthPasswordActivity
                    Intent intent = new Intent(PrivacyAndSecurity.this, ChangeAuthPasswordActivity.class); // Launch Account Password Change Activity
                    startActivity(intent);
                }
            });
        } else {
            Log.e(TAG, "Layout item 'item_change_auth_password' not found!");
        }
        // --- End Change Account Password listener ---


        // *** NEW: Set click listener for the "Reset Secure Passphrase" item ***
        // This should ALWAYS be available as it's for recovery when passphrase is forgotten.
        if (itemResetPassphrase != null) { // Safety check
            itemResetPassphrase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "'Reset Secure Passphrase' item clicked.");
                    // Launch the ResetPassphraseActivity
                    Intent intent = new Intent(PrivacyAndSecurity.this, ResetPassphraseActivity.class); // Launch the Reset activity
                    startActivity(intent);
                }
            });
        } else {
            Log.e(TAG, "Layout item 'item_reset_passphrase' not found!");
        }
        // *** END NEW LISTENER ***



        // Apply EdgeToEdge insets if used
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> { // Ensure R.id.main matches your root layout ID
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "✅ onCreate finished in PrivacyAndSecurity");
    }

    // Handle back button click on the Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go back to the previous activity (MainActivity)
        return true;
    }

    // You might not need onDestroy if you don't have specific cleanup in this activity
}