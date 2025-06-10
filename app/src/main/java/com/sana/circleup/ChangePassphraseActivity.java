package com.sana.circleup;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sana.circleup.encryptionfiles.CryptoUtils; // Ensure correct import
import com.sana.circleup.encryptionfiles.SecureKeyStorageUtil; // Ensure correct import
import com.sana.circleup.encryptionfiles.YourKeyManager; // Ensure correct import

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.GeneralSecurityException; // Import specific security exceptions
import java.io.IOException; // Import IO Exception

import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Import for Base64 (android.util is often used for String conversion)
import android.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey; // Import SecretKey

public class ChangePassphraseActivity extends AppCompatActivity {

    private static final String TAG = "ChangePassphraseAct";

    private TextInputLayout layoutOldPassphrase, layoutNewPassphrase, layoutConfirmNewPassphrase;
    private TextInputEditText editTextOldPassphrase, editTextNewPassphrase, editTextConfirmNewPassphrase;
    private Button buttonChangePassphrase;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private String currentUserId;

    private ExecutorService executorService; // For async tasks
    private Handler mainHandler; // For posting results back to the main thread

    // Variables to hold fetched key data from Firebase
    private String fetchedEncryptedPrivateKeyBase64;
    private String fetchedPublicKeyBase64;
    private String fetchedSaltBase64;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_passphrase);
        Log.d(TAG, "🟢 ChangePassphraseActivity onCreate.");

        // Setup Toolbar as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_change_passphrase); // Match toolbar ID
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Change Passphrase"); // Set title
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
            }
        } else {
            Log.w(TAG, "Toolbar not found in layout with ID toolbar_change_passphrase");
        }

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Get current user and check authentication
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated in ChangePassphraseActivity! Redirecting to Login.");
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity(); // Redirect to Login activity
            return; // Stop onCreate execution
        }
        currentUserId = currentUser.getUid();
        Log.d(TAG, "Current User ID: " + currentUserId);

        // Initialize UI elements
        layoutOldPassphrase = findViewById(R.id.layout_old_passphrase);
        editTextOldPassphrase = findViewById(R.id.editTextOldPassphrase);
        layoutNewPassphrase = findViewById(R.id.layout_new_passphrase);
        editTextNewPassphrase = findViewById(R.id.editTextNewPassphrase);
        layoutConfirmNewPassphrase = findViewById(R.id.layout_confirm_passphrase);
        editTextConfirmNewPassphrase = findViewById(R.id.editTextConfirmNewPassphrase);
        buttonChangePassphrase = findViewById(R.id.buttonChangePassphrase);
        progressBar = findViewById(R.id.progressBarChangePassphrase);

        // Initialize Executors and Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Check if user's Private Key is currently available in memory
        // If not, they cannot decrypt the old key to change it.
        // This is a crucial check before fetching from Firebase.
        if (!YourKeyManager.getInstance().isPrivateKeyAvailable()) {
            Log.e(TAG, "User's Private Key is NOT available in KeyManager. Cannot change passphrase.");
            // Show error and disable UI, suggest unlocking first
            showErrorAndDisableUI("Account is locked. Please unlock your account first via 'Unlock Secure Account'.");
            return; // Stop onCreate execution
        } else {
            Log.d(TAG, "User's Private Key is available in KeyManager. Proceeding to fetch keys for change.");
        }


        // Fetch user's key data (encrypted private key, public key, salt) from Firebase
        // This is needed for decryption using the old passphrase and re-encryption with the new one.
        fetchKeyDataFromFirebase();

        // Set click listener for the Change Passphrase button
        buttonChangePassphrase.setOnClickListener(v -> onChangePassphraseButtonClick());

        Log.d(TAG, "✅ onCreate finished in ChangePassphraseActivity");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Handle back button click on toolbar
        return true;
    }

    // Handle back button press - Add confirmation if inputs are not empty
    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(editTextOldPassphrase.getText()) ||
                !TextUtils.isEmpty(editTextNewPassphrase.getText()) ||
                !TextUtils.isEmpty(editTextConfirmNewPassphrase.getText())) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Are you sure you want to leave?")
                    .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }


    // --- Fetch Key Data from Firebase ---
    private void fetchKeyDataFromFirebase() {
        if (TextUtils.isEmpty(currentUserId) || usersRef == null) {
            Log.e(TAG, "Cannot fetch key data: currentUserId or usersRef is null.");
            showErrorAndDisableUI("Error fetching user data.");
            return;
        }

        // Show progress indication during fetch
        setUIEnabled(false); // Disable inputs and button
        progressBar.setVisibility(View.VISIBLE);
        // Update a status TextView if you had one, or just rely on progress bar

        Log.d(TAG, "Fetching encrypted key data from Firebase for user: " + currentUserId);

        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This runs on the main thread
                progressBar.setVisibility(View.GONE); // Hide progress bar

                if (snapshot.exists()) {
                    // Extract key data Base64 strings
                    fetchedEncryptedPrivateKeyBase64 = snapshot.child("encryptedPrivateKey").getValue(String.class);
                    fetchedPublicKeyBase64 = snapshot.child("publicKey").getValue(String.class);
                    fetchedSaltBase64 = snapshot.child("encryptionSalt").getValue(String.class);

                    // Check if key data is complete
                    if (TextUtils.isEmpty(fetchedEncryptedPrivateKeyBase64) || TextUtils.isEmpty(fetchedPublicKeyBase64) || TextUtils.isEmpty(fetchedSaltBase64)) {
                        Log.e(TAG, "Fetched key data from Firebase is incomplete or empty.");
                        showErrorAndDisableUI("Incomplete key data found. Your account needs security setup. Please log out and log back in.");
                        // Consider clearing local storage if incomplete data indicates corruption
                        SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Use application context
                    } else {
                        Log.d(TAG, "Key data fetched successfully from Firebase. Ready for passphrase change attempt.");
                        setUIEnabled(true); // Re-enable UI
                        // Optional: Clear old passphrase field on successful fetch
                        editTextOldPassphrase.setText("");
                        editTextNewPassphrase.setText("");
                        editTextConfirmNewPassphrase.setText("");
                    }
                } else {
                    // User data node not found in Firebase (shouldn't happen if auth worked, but defensive)
                    Log.e(TAG, "User data node not found in Firebase for key fetch for user: " + currentUserId + ".");
                    showErrorAndDisableUI("User data missing in server. Please log out and log back in.");
                    // Consider clearing local storage if data is missing
                    SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Use application context
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // This runs on the main thread
                progressBar.setVisibility(View.GONE); // Hide progress bar
                Log.e(TAG, "Firebase key fetch failed: " + error.getMessage(), error.toException());
                showErrorAndDisableUI("Failed to fetch key data from server. Check network.");
                // Consider clearing local storage on fetch failure
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Use application context
            }
        });
    }

    // --- Helper to set UI enabled/disabled state ---
    private void setUIEnabled(boolean enabled) {
        editTextOldPassphrase.setEnabled(enabled);
        editTextNewPassphrase.setEnabled(enabled);
        editTextConfirmNewPassphrase.setEnabled(enabled);
        buttonChangePassphrase.setEnabled(enabled);
    }

    // --- Helper to show critical error and disable UI ---
    private void showErrorAndDisableUI(String message) {
        // Assuming you might want to set a TextView for errors
        // If you don't have a dedicated TextView, use a Toast or Dialog
        // For simplicity, let's use a Toast and disable UI
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        setUIEnabled(false); // Disable all inputs and button
        progressBar.setVisibility(View.GONE); // Ensure progress bar is hidden
        // Optionally hide input layouts as well
        layoutOldPassphrase.setVisibility(View.GONE);
        layoutNewPassphrase.setVisibility(View.GONE);
        layoutConfirmNewPassphrase.setVisibility(View.GONE);
        buttonChangePassphrase.setVisibility(View.GONE);

        Log.e(TAG, "Critical error displayed and UI disabled: " + message);
    }


    // --- Handle Change Passphrase Button Click ---
    private void onChangePassphraseButtonClick() {
        String oldPassphrase = editTextOldPassphrase.getText().toString();
        String newPassphrase = editTextNewPassphrase.getText().toString();
        String confirmNewPassphrase = editTextConfirmNewPassphrase.getText().toString();

        // --- Validation ---
        if (TextUtils.isEmpty(oldPassphrase)) {
            editTextOldPassphrase.setError("Current passphrase is required");
            editTextOldPassphrase.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newPassphrase)) {
            editTextNewPassphrase.setError("New passphrase is required");
            editTextNewPassphrase.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmNewPassphrase)) {
            editTextConfirmNewPassphrase.setError("Confirm new passphrase is required");
            editTextConfirmNewPassphrase.requestFocus();
            return;
        }
        if (!newPassphrase.equals(confirmNewPassphrase)) {
            editTextConfirmNewPassphrase.setError("New passphrases do not match");
            editTextConfirmNewPassphrase.requestFocus();
            return;
        }
        if (newPassphrase.length() < 6) { // Use the same minimum length as signup
            editTextNewPassphrase.setError("Passphrase must be at least 6 characters");
            editTextNewPassphrase.requestFocus();
            return;
        }
        // Add complexity check for new passphrase if needed (similar to Signup)
        // private static final Pattern PASSWORD_STRENGTH_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");
        // if (!PASSWORD_STRENGTH_PATTERN.matcher(newPassphrase).matches()) {
        //     editTextNewPassphrase.setError("New passphrase must be 6+ chars, include uppercase, lowercase, digit, special char.");
        //     editTextNewPassphrase.requestFocus();
        //     return;
        // }


        // Check if key data was successfully fetched from Firebase
        if (TextUtils.isEmpty(fetchedEncryptedPrivateKeyBase64) || TextUtils.isEmpty(fetchedPublicKeyBase64) || TextUtils.isEmpty(fetchedSaltBase64)) {
            Log.e(TAG, "Key data not available for change. Cannot attempt change.");
            showErrorAndDisableUI("Key data not loaded. Try again or log out and back in.");
            return; // Stop if data is not ready
        }


        // Disable input and button, show progress
        setUIEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        // Update status text if you have one

        Log.d(TAG, "Change Passphrase button clicked. Attempting verification and change process.");

        // Start the asynchronous change process
        changePassphraseAsync(oldPassphrase, newPassphrase, fetchedPublicKeyBase64, fetchedEncryptedPrivateKeyBase64, fetchedSaltBase64);
    }

    // --- Asynchronous Passphrase Change Process ---
    private void changePassphraseAsync(String oldPassphrase, String newPassphrase, String publicKeyBase64, String encryptedPrivateKeyBase64, String oldSaltBase64) {

        // Run the process on the background executor
        executorService.execute(() -> {
            Exception processException = null;
            String successMessage = null;
            boolean requiresLogoutAndResync = false; // Flag for critical errors

            Log.d(TAG, "Starting passphrase change process on background thread.");

            try {
                // --- Step 1: Verify Old Passphrase by Decrypting Private Key ---
                Log.d(TAG, "Decoding old salt and encrypted private key from Base64.");
                byte[] oldSalt = Base64.decode(oldSaltBase64, Base64.DEFAULT); // Use android.util.Base64
                byte[] encryptedPrivateKeyWithIV = Base64.decode(encryptedPrivateKeyBase64, Base64.DEFAULT); // Use android.util.Base64

                // Basic validity checks for decoded bytes
                if (oldSalt == null || encryptedPrivateKeyWithIV == null || oldSalt.length == 0 || encryptedPrivateKeyWithIV.length == 0) {
                    Log.e(TAG, "Decoded old salt or encrypted private key is null or empty!");
                    throw new IllegalArgumentException("Invalid fetched key data.");
                }


                Log.d(TAG, "Deriving OLD symmetric key from old passphrase and salt.");
                SecretKey oldEncryptionKey = CryptoUtils.deriveKeyFromPassphrase(oldPassphrase, oldSalt, CryptoUtils.PBKDF2_ITERATIONS);

                Log.d(TAG, "Attempting to decrypt private key using OLD symmetric key.");
                // This will throw BadPaddingException or similar if the old passphrase is wrong
                PrivateKey decryptedPrivateKey = CryptoUtils.decryptPrivateKey(encryptedPrivateKeyWithIV, oldEncryptionKey);
                Log.d(TAG, "Private key decrypted successfully with old passphrase. Verification successful.");

                // --- Step 2: Generate New Keys and Encrypt Private Key with New Passphrase ---
                Log.d(TAG, "Generating NEW salt.");
                byte[] newSalt = CryptoUtils.generateSalt();

                Log.d(TAG, "Deriving NEW symmetric key from NEW passphrase and NEW salt.");
                SecretKey newEncryptionKey = CryptoUtils.deriveKeyFromPassphrase(newPassphrase, newSalt, CryptoUtils.PBKDF2_ITERATIONS);

                Log.d(TAG, "Re-encrypting private key with NEW symmetric key.");
                byte[] newlyEncryptedPrivateKeyWithIV = CryptoUtils.encryptPrivateKey(decryptedPrivateKey, newEncryptionKey);


                // --- Step 3: Update Firebase ---
                Log.d(TAG, "Preparing data for Firebase update.");
                // Convert new salt and newly encrypted private key to Base64 for Firebase storage
                // *** Use java.util.Base64 for encoding cryptographic data before storing in Firebase/local secure files ***
                String newSaltBase64 = java.util.Base64.getEncoder().encodeToString(newSalt);
                String newlyEncryptedPrivateKeyBase64 = java.util.Base64.getEncoder().encodeToString(newlyEncryptedPrivateKeyWithIV);


                Map<String, Object> updates = new HashMap<>();
                updates.put("encryptionSalt", newSaltBase64);
                updates.put("encryptedPrivateKey", newlyEncryptedPrivateKeyBase64);

                Log.d(TAG, "Updating Firebase with new salt and encrypted private key.");
                // Use Tasks.await() for synchronous Firebase update on this background thread
                Tasks.await(usersRef.child(currentUserId).updateChildren(updates)); // Requires import com.google.android.gms.tasks.Tasks; java.util.concurrent.ExecutionException; java.lang.InterruptedException;
                Log.d(TAG, "Firebase update successful.");

                // --- Step 4: Update Local Secure Storage ---
                Log.d(TAG, "Updating local secure storage with new symmetric key and encrypted private key.");
                // Clear OLD local keys first to ensure atomicity (as much as possible)
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Use application context

                // Save NEW derived symmetric key
                boolean saveSymmetricSuccess = SecureKeyStorageUtil.saveSymmetricKey(getApplicationContext(), newEncryptionKey); // Use application context

                // Save NEWLY encrypted private key bytes (from step 2) and existing public key bytes
                // You need the public key bytes. Convert from the base64 fetched initially.
                byte[] publicKeyBytes = Base64.decode(publicKeyBase64, Base64.DEFAULT); // Use android.util.Base64
                boolean saveKeyPairSuccess = SecureKeyStorageUtil.saveEncryptedKeyPair(getApplicationContext(), currentUserId, newlyEncryptedPrivateKeyWithIV, publicKeyBytes); // Use application context

                if (saveSymmetricSuccess && saveKeyPairSuccess) {
                    Log.d(TAG, "Local secure storage updated successfully.");
                    // Local save successful -> RememberMe preference can be maintained as is.
                    // If it was true, it remains true and will use the new local keys next time.
                    // If it was false, it remains false.
                } else {
                    Log.e(TAG, "FAILED to update one or more keys in local secure storage!");
                    // If local save fails after Firebase update, it's a potential inconsistency.
                    // Firebase has the new key, but local is old/corrupt.
                    // Forcing logout and re-login is the safest path to re-sync from Firebase.
                    Log.e(TAG, "Local save failed after Firebase update. Forcing logout and re-sync from Firebase.");
                    requiresLogoutAndResync = true; // Set flag for critical failure handling
                    processException = new Exception("Failed to save keys locally. Please log in again.");
                    // Clear local storage again to ensure no partial new keys are left
                    SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Use application context
                }


                // --- Step 5: Update In-Memory KeyManager ---
                // Clear the old keys from memory and load the *newly* decrypted private key and public key.
                // The decrypted Private Key is available from Step 1 ('decryptedPrivateKey').
                Log.d(TAG, "Updating KeyManager with new keys.");
                // The Public Key object is also needed. You can create it from the bytes fetched initially.
                PublicKey updatedPublicKey = CryptoUtils.bytesToPublicKey(publicKeyBytes);
                YourKeyManager.getInstance().setKeys(currentUserId, updatedPublicKey, decryptedPrivateKey); // Load the decrypted private key and updated public key
                Log.d(TAG, "KeyManager updated with new keys.");

                // Conversation keys in KeyManager's cache are still valid IF they were decrypted with the *old* Private Key.
                // They don't need to be re-decrypted with the *new* passphrase/symmetric key.
                // They only needed the *old* RSA Private Key to be fetched/decrypted from Firebase.
                // If KeyManager was cleared above, you might need to reload conversation keys from Room here,
                // but YourKeyManager.setKeys clears it. It's safer to just clear all and rely on MainActivity's
                // onResume/ChatFragment click to re-load conversation keys from Room after the main Private Key is set.

                successMessage = "Security Passphrase changed successfully!";


            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error decoding Base64 or processing key data", e);
                processException = new Exception("Invalid key data format from server.");
                requiresLogoutAndResync = true; // Likely corrupt server data
            } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                     InvalidKeyException | IllegalBlockSizeException |
                     BadPaddingException | InvalidAlgorithmParameterException |
                     InvalidKeySpecException e) {
                // Crypto operation failed
                Log.e(TAG, "Cryptographic operation failed during passphrase change", e);
                if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof InvalidKeyException) {
                    // These often indicate incorrect OLD passphrase
                    processException = new Exception("Incorrect Current Passphrase.");
                    requiresLogoutAndResync = false; // Not a server data issue, user error
                } else {
                    // Other crypto errors (shouldn't happen with valid inputs unless implementation error)
                    processException = new Exception("Encryption/Decryption failed.");
                    requiresLogoutAndResync = true; // Treat as critical if not wrong passphrase
                }
            } catch (java.util.concurrent.ExecutionException | InterruptedException e) { // Firebase Tasks.await() errors
                Log.e(TAG, "Firebase update failed", e);
                processException = new Exception("Failed to update keys on server. Network error?");
                requiresLogoutAndResync = true; // Server update failed, critical
            }
            catch (Exception e) {
                // Catch any other unexpected errors
                Log.e(TAG, "An unexpected error occurred during passphrase change", e);
                processException = new Exception("An unexpected error occurred.");
                requiresLogoutAndResync = true; // Treat as critical
            }

            // Post the result back to the main thread
            final String finalSuccessMessage = successMessage;
            final String finalErrorMessage = processException != null ? processException.getMessage() : null;
            final boolean finalRequiresLogoutAndResync = requiresLogoutAndResync;
            final boolean finalIsWrongPassphrase = (processException instanceof BadPaddingException || processException instanceof IllegalBlockSizeException || processException instanceof InvalidKeyException) && !requiresLogoutAndResync;


            mainHandler.post(() -> { // This runs on the Main Thread
                progressBar.setVisibility(View.GONE); // Hide progress bar
                setUIEnabled(true); // Re-enable UI elements

                if (finalSuccessMessage != null) {
                    // Success
                    Log.d(TAG, "Passphrase change successful. Showing toast and finishing.");
                    Toast.makeText(ChangePassphraseActivity.this, finalSuccessMessage, Toast.LENGTH_LONG).show();
                    finish(); // Finish the activity to go back to Privacy & Security
                    // MainActivity's onResume will handle refreshing fragments based on KeyManager state

                } else {
                    // Failure
                    Log.e(TAG, "Passphrase change failed. Showing error.");
                    String displayMessage = finalErrorMessage != null ? finalErrorMessage : "Unknown error changing passphrase.";
                    Toast.makeText(ChangePassphraseActivity.this, displayMessage, Toast.LENGTH_LONG).show();

                    if (finalIsWrongPassphrase) {
                        // Specific error: Incorrect old passphrase
                        editTextOldPassphrase.setError("Incorrect Passphrase");
                        editTextOldPassphrase.requestFocus();
                        editTextOldPassphrase.setText(""); // Clear the incorrect old passphrase
                        editTextNewPassphrase.setText(""); // Clear new passphrases on verification failure
                        editTextConfirmNewPassphrase.setText("");
                    } else if (finalRequiresLogoutAndResync) {
                        // Critical error: Inconsistent state or server/local save failed
                        // Force logout and redirect to login
                        Log.e(TAG, "Critical failure during passphrase change, forcing logout.");
                        new AlertDialog.Builder(ChangePassphraseActivity.this)
                                .setTitle("Security Error")
                                .setMessage(displayMessage + "\nYour account state is inconsistent. Please log out and log back in to re-sync.")
                                .setCancelable(false) // User must acknowledge
                                .setPositiveButton("OK", (dialog, which) -> forceLogout()) // Call forceLogout helper
                                .show();
                    } else {
                        // Other errors (e.g., invalid format, unexpected crypto error)
                        // Keep the UI enabled, but clear inputs maybe?
                        editTextOldPassphrase.setText(""); // Clear all inputs on non-wrong-passphrase errors
                        editTextNewPassphrase.setText("");
                        editTextConfirmNewPassphrase.setText("");
                        // UI remains enabled for user to try again
                    }
                }
            });
        });
    }

    // --- Helper to force logout and redirect to Login ---
    private void forceLogout() {
        Log.d(TAG, "Force logging out after critical passphrase change failure.");
        // Clear all local keys and KeyManager state
        if (currentUserId != null) {
            SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Use application context
        } else {
            SecureKeyStorageUtil.clearSymmetricKey(getApplicationContext());
        }
        YourKeyManager.getInstance().clearKeys();

        // Sign out from Firebase
        auth.signOut();
        Log.d(TAG, "Firebase Auth signed out.");

        // Sign out from Google if applicable
        GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut() // Use application context
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "Google SignOut completed during force logout.");
                    sendUserToLoginActivity(); // Redirect to Login
                })
                .addOnFailureListener(task -> {
                    Log.e(TAG, "Google SignOut failed during force logout.", task.getCause());
                    sendUserToLoginActivity(); // Redirect even if Google sign out fails
                });
    }


    // --- Helper Navigation Method to go back to Login ---
    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity from ChangePassphraseActivity.");
        Intent loginIntent = new Intent(ChangePassphraseActivity.this, Login.class); // Ensure Login activity exists
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish(); // Finish this activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shut down the executor service
        if (executorService != null && !executorService.isShutdown()) {
            Log.d(TAG, "ExecutorService shutting down in ChangePassphraseActivity.");
            executorService.shutdownNow(); // Attempt to stop executing tasks
        }
        // If using a ProgressDialog field, dismiss it here to prevent window leaks
        // if (progressDialog != null && progressDialog.isShowing()) { progressDialog.dismiss(); }
        Log.d(TAG, "🔴 ChangePassphraseActivity onDestroy called.");
    }
}