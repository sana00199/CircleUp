package com.sana.circleup;
// In your package (e.g., com.sana.circleup), create UnlockAccountActivity.java

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.core.content.ContextCompat; // Import ContextCompat for color

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64; // Use android.util.Base64 for encoding/decoding strings
import android.util.Log;
import android.view.MenuItem;
import android.view.View; // Import View
import android.widget.Button; // Import Button
import android.widget.ProgressBar; // Use ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText; // Import TextInputEditText
import com.google.android.material.textfield.TextInputLayout; // Import TextInputLayout

import com.google.android.gms.tasks.Tasks; // Import Tasks for synchronous Firebase get
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener; // Import ValueEventListener

// >>>>>>>>>>>>>>>> IMPORT YOUR SPECIFIC ENCRYPTION AND ROOM CLASSES HERE <<<<<<<<<<<<<<<<<<
// Replace these with the actual package and class names in your project
import com.sana.circleup.encryptionfiles.CryptoUtils;
import com.sana.circleup.encryptionfiles.YourKeyManager;
import com.sana.circleup.encryptionfiles.SecureKeyStorageUtil; // Import SecureKeyStorageUtil
import com.sana.circleup.room_db_implement.ChatDatabase; // Import ChatDatabase
import com.sana.circleup.room_db_implement.ConversationKeyDao; // Import ConversationKeyDao
import com.sana.circleup.room_db_implement.ConversationKeyEntity; // Import ConversationKeyEntity
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

import java.security.PrivateKey; // Import PrivateKey
import java.security.PublicKey; // Import PublicKey
import java.util.List; // Import List
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; // Use java.util.concurrent.Executors
import java.security.InvalidKeyException; // Import crypto exceptions
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey; // Import SecretKey
import java.security.spec.InvalidKeySpecException; // Import InvalidKeySpecException
import java.security.InvalidAlgorithmParameterException; // Import InvalidAlgorithmParameterException







public class UnlockAccountPassphereActivity extends AppCompatActivity {

    private static final String TAG = "UnlockPassphraseAct";
    private TextView textViewUseRecoveryCode;

    private TextInputEditText editTextUnlockPassphrase; // Changed to TextInputEditText
    private Button buttonConfirmUnlock;
    private ProgressBar progressBar;
    private TextView unlockStatusText; // Added TextView for the initial status message

    // *** NEW MEMBERS START HERE ***
    private TextView resetPassphraseExplanation;
    private TextView textViewResetPassphrase; // The clickable "Reset Secure Passphrase" text
    // *** NEW MEMBERS END HERE ***


    private ExecutorService executorService;
    private Handler mainHandler;

    private String currentUserId; // Current logged-in user's UID
    private String publicKeyBase64; // To store fetched public key Base64
    private String encryptedPrivateKeyBase64; // To store fetched encrypted private key Base64
    private String saltBase64; // To store fetched salt Base64

    private DatabaseReference usersRef; // Firebase reference to fetch user data


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_account_passphere); // Ensure this matches your layout file name
        Log.d(TAG, "🟢 UnlockAccountPassphereActivity onCreate.");


        // Setup Toolbar as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_unlock_account); // Match your layout ID
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Security Passphrase"); // Set title
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
            }
        } else {
            Log.w(TAG, "Toolbar not found in layout with ID toolbar_unlock_account");
        }


        // Initialize Firebase Auth and Database
        FirebaseAuth auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "No current user in UnlockAccountPassphereActivity! Redirecting to Login.");
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity(); // Redirect to Login activity
            return; // Stop onCreate execution
        }
        currentUserId = currentUser.getUid();
        Log.d(TAG, "Current User ID: " + currentUserId);


        initializeViews(); // Initialize UI elements


        // Setup background executor and main handler for async tasks
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());


        // --- Check if keys are *already* unlocked in KeyManager ---
        // If user comes here but keys are already loaded (e.g., came from login where keys loaded successfully),
        // no need to prompt for passphrase again.
        if (YourKeyManager.getInstance().isPrivateKeyAvailable()) {
            Log.d(TAG, "Private key is already available in KeyManager. Account is unlocked.");
            showUnlockStatus("Account is already unlocked.");
            // Optionally disable input and button, or just show message and maybe auto-finish after delay
            editTextUnlockPassphrase.setVisibility(View.GONE);
            buttonConfirmUnlock.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            // *** Optionally, hide the reset options too if account is fully unlocked ***
            resetPassphraseExplanation.setVisibility(View.GONE);
            textViewResetPassphrase.setVisibility(View.GONE);

            // Maybe auto-finish this activity after a short delay?
            mainHandler.postDelayed(this::finish, 2000); // Finish after 2 seconds

            // No need to fetch keys or set listeners below if already unlocked
            Log.d(TAG, "Skipping key fetch and listener setup as account is already unlocked.");
            return; // Stop onCreate execution here
        } else {
            Log.d(TAG, "Private key is NOT available. Need passphrase to unlock.");
            showUnlockStatus("Enter your security passphrase to unlock encrypted messages."); // Default message
            editTextUnlockPassphrase.setVisibility(View.VISIBLE); // Ensure input is visible
            buttonConfirmUnlock.setVisibility(View.VISIBLE); // Ensure button is visible
            // Ensure reset options are visible if keys are not unlocked
            resetPassphraseExplanation.setVisibility(View.VISIBLE);
            textViewResetPassphrase.setVisibility(View.VISIBLE);
        }
        // --- END Check unlocked state ---


        // Fetch user's key data (encrypted private key, salt, public key) from Firebase
        // This data is needed to attempt decryption with the user's entered passphrase.
        // Only fetch if keys are NOT already available in KeyManager.
        fetchKeyDataFromFirebase(currentUserId);




// *** NEW: Set click listener for the "Use Recovery Code" TextView ***
        if (textViewUseRecoveryCode != null) { // Safety check
            textViewUseRecoveryCode.setOnClickListener(v -> {
                Log.d(TAG, "'Use Recovery Code' clicked. Navigating to Recovery Activity.");
                // Navigate to the new activity that handles the recovery process
                // You will need to create RecoverAccountWithCodeActivity
                Intent intent = new Intent(UnlockAccountPassphereActivity.this, RecoverAccountWithCodeActivity.class); // Ensure RecoverAccountWithCodeActivity exists
                // Pass the current user ID, as it's needed in the recovery activity
                if (currentUserId != null) {
                    intent.putExtra("userId", currentUserId);
                    // Pass Base64 encoded public key if already fetched (optional, can fetch again)
                    if (publicKeyBase64 != null) intent.putExtra("publicKeyBase64", publicKeyBase64);
                    startActivity(intent);
                    // Decide if you finish THIS activity or leave it open.
                    // Finishing might be better to prevent returning here with old inputs.
                    finish(); // Finish UnlockAccountPassphereActivity
                } else {
                    Log.e(TAG, "Cannot navigate to Recovery Activity, currentUserId is null.");
                    Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
                    // Redirect to Login if user ID is unexpectedly null
                    sendUserToLoginActivity();
                }
            });
        } else {
            Log.e(TAG, "textViewUseRecoveryCode not found in layout!");
            // This option will not be available if the view wasn't found
        }
// *** END NEW LISTENER ***



        // Set click listener for the Confirm Unlock button
        buttonConfirmUnlock.setOnClickListener(v -> {
            String passphrase = editTextUnlockPassphrase.getText().toString();
            if (TextUtils.isEmpty(passphrase)) {
                editTextUnlockPassphrase.setError("Passphrase is required");
                editTextUnlockPassphrase.requestFocus();
                return;
            }
            // Attempt decryption with the entered passphrase
            attemptUnlockAsync(passphrase);
        });


        // *** NEW: Set click listener for the "Reset Secure Passphrase" TextView ***
        if (textViewResetPassphrase != null) { // Safety check
            textViewResetPassphrase.setOnClickListener(v -> {
                Log.d(TAG, "Reset Secure Passphrase clicked. Navigating to ResetPassphraseActivity.");
                // Navigate to the new activity that handles the reset process
                // You will need to create ResetPassphraseActivity
                Intent intent = new Intent(UnlockAccountPassphereActivity.this, ResetPassphraseActivity.class); // Ensure ResetPassphraseActivity exists
                startActivity(intent);
                // Decide if you finish this activity or leave it open.
                // Finishing might be better to prevent returning to this state with old inputs.
                finish();
            });
        } else {
            Log.e(TAG, "textViewResetPassphrase not found in layout!");
        }
        // *** END NEW LISTENER ***


        Log.d(TAG, "✅ onCreate finished in UnlockAccountPassphereActivity");
    }


    // --- START: ADD THIS NEW METHOD TO HANDLE TOOLBAR BACK BUTTON ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check if the Up button (back button) was clicked
        if (item.getItemId() == android.R.id.home) {
            // Simulate a back press, which will finish the current activity
            onBackPressed();
            return true;
        }
        // Let the system handle any other menu items
        return super.onOptionsItemSelected(item);
    }
    // --- END: ADD THIS NEW METHOD ---

    // Initialize UI views (Kept from your code, adjusted for TextInputEditText)
    private void initializeViews() {
        // Make sure your layout uses TextInputLayout and TextInputEditText
        TextInputLayout passphraseInputLayout = findViewById(R.id.unlock_passphrase_input_layout);
        editTextUnlockPassphrase = findViewById(R.id.editTextUnlockPassphrase); // Should be TextInputEditText
        buttonConfirmUnlock = findViewById(R.id.buttonConfirmUnlock);
        progressBar = findViewById(R.id.unlock_progressBar);
//        unlockStatusText = findViewById(R.id.unlock_explanation_text); // Reference the explanation text

        // *** NEW: Initialize the new TextViews ***
        resetPassphraseExplanation = findViewById(R.id.reset_passphrase_explanation);
        textViewResetPassphrase = findViewById(R.id.text_view_reset_passphrase);
        // *** END NEW INITIALIZATION ***
        textViewUseRecoveryCode = findViewById(R.id.textViewUseRecoveryCode);

        // Set password toggle for the passphrase field
        if (passphraseInputLayout != null && editTextUnlockPassphrase != null) {
            // TextInputLayout style already defines the password toggle appearance
            // No need to manually manage ImageView and input type here if using TextInputLayout's feature
            // If you prefer the manual eye icon, you would need to add that logic.
            // For simplicity with TextInputLayout, rely on its built-in toggle.
        } else {
            Log.w(TAG, "TextInputLayout or TextInputEditText for passphrase not found.");
        }
    }

    // Helper to update the status TextView
    private void showUnlockStatus(String message) {
        if (unlockStatusText != null) {
            unlockStatusText.setText(message);
        }
    }


    // Fetch key data from Firebase (Similar to ChangePassphraseActivity)
    private void fetchKeyDataFromFirebase(String userId) {
        if (TextUtils.isEmpty(userId) || usersRef == null) {
            Log.e(TAG, "Cannot fetch key data: userId or usersRef is null.");
            showUnlockStatus("Error fetching user data.");
            // Disable unlock attempt if data cannot be fetched
            buttonConfirmUnlock.setEnabled(false);
            editTextUnlockPassphrase.setEnabled(false);
            return;
        }

        // Show progress indication during fetch
        showUnlockStatus("Fetching secure key data...");
        progressBar.setVisibility(View.VISIBLE);
        buttonConfirmUnlock.setEnabled(false);
        editTextUnlockPassphrase.setEnabled(false);


        Log.d(TAG, "Fetching encrypted key data from Firebase for user: " + userId);

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This runs on the main thread
                progressBar.setVisibility(View.GONE); // Hide progress bar
                buttonConfirmUnlock.setEnabled(true); // Re-enable inputs/button
                editTextUnlockPassphrase.setEnabled(true);

                if (snapshot.exists()) {
                    // Extract key data Base64 strings
                    encryptedPrivateKeyBase64 = snapshot.child("encryptedPrivateKey").getValue(String.class);
                    saltBase64 = snapshot.child("encryptionSalt").getValue(String.class);
                    publicKeyBase64 = snapshot.child("publicKey").getValue(String.class);


                    // Check if crypto keys exist and are complete for this user
                    boolean hasCompleteKeysInFirebase = !TextUtils.isEmpty(encryptedPrivateKeyBase64)
                            && !TextUtils.isEmpty(saltBase64)
                            && !TextUtils.isEmpty(publicKeyBase64);

                    if (hasCompleteKeysInFirebase) {
                        Log.d(TAG, "Key data fetched successfully from Firebase. Ready to attempt unlock.");
                        showUnlockStatus("Enter your security passphrase to unlock encrypted messages.");
                        // Keep unlock button/input enabled
                    } else {
                        // User data found, but crypto keys are missing or incomplete in Firebase
                        Log.e(TAG, "Fetched key data from Firebase is incomplete or missing for user: " + userId + ".");
                        showUnlockStatus("Secure setup incomplete. Cannot unlock encrypted messages.");
                        // Disable unlock functionality if keys are missing on the server side
                        buttonConfirmUnlock.setEnabled(false);
                        editTextUnlockPassphrase.setEnabled(false);
                        // *** Consider showing the Reset option more prominently here ***
                        // Maybe change its text to suggest setting up the passphrase?
                        textViewResetPassphrase.setText("Set Up Security Passphrase");
                        resetPassphraseExplanation.setText("Your account is not secured.");
                        Log.d(TAG, "Showing 'Set Up' option instead of 'Reset' as keys are missing in Firebase.");
                    }
                } else {
                    // User data node not found in Firebase (shouldn't happen if auth worked, but defensive)
                    Log.e(TAG, "User data node not found in Firebase for key fetch for user: " + userId + ".");
                    showUnlockStatus("User data missing in server. Cannot unlock messages.");
                    buttonConfirmUnlock.setEnabled(false);
                    editTextUnlockPassphrase.setEnabled(false);
                    // *** Consider redirecting to login or showing critical error UI here ***
                    // If user data is missing, they likely need to re-login or re-setup.
                    // Forcing a re-login to Login activity might be safer, where it handles user data fetch.
                    // For now, just disable unlock.
                    Log.e(TAG, "User data missing in Firebase. Clearing KeyManager and showing error.");
                    YourKeyManager.getInstance().clearKeys(); // Ensure in-memory keys are cleared
                    // Optionally clear local storage too if firebase data is gone
                    SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
                    // No need to explicitly redirect yet, user can see the error.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // This runs on the main thread
                progressBar.setVisibility(View.GONE); // Hide progress bar
                buttonConfirmUnlock.setEnabled(false); // Disable button on error
                editTextUnlockPassphrase.setEnabled(false);
                Log.e(TAG, "Firebase key fetch failed: " + error.getMessage(), error.toException());
                showUnlockStatus("Failed to fetch key data. Check network or log in again.");
                // Clear keys as data fetch failed
                YourKeyManager.getInstance().clearKeys();
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
            }
        });
    }


    // Attempt to decrypt the private key asynchronously (Similar to Login.java)
    private void attemptUnlockAsync(String passphrase) {
        // Check if fetched key data is available before attempting decryption
        if (TextUtils.isEmpty(publicKeyBase64) || TextUtils.isEmpty(encryptedPrivateKeyBase64) || TextUtils.isEmpty(saltBase64)) {
            Log.e(TAG, "Cannot attempt unlock: Key data not fetched successfully from Firebase.");
            showUnlockStatus("Secure keys not loaded. Please try again.");
            // Disable input/button if data is missing
            buttonConfirmUnlock.setEnabled(false);
            editTextUnlockPassphrase.setEnabled(false);
            return;
        }
        if (TextUtils.isEmpty(currentUserId)) {
            Log.e(TAG, "Cannot attempt unlock: currentUserId is null or empty.");
            showUnlockStatus("User ID missing. Log in again.");
            buttonConfirmUnlock.setEnabled(false);
            editTextUnlockPassphrase.setEnabled(false);
            return;
        }


        showUnlockStatus("Attempting to unlock...");
        progressBar.setVisibility(View.VISIBLE);
        buttonConfirmUnlock.setEnabled(false); // Disable button during process
        editTextUnlockPassphrase.setEnabled(false); // Disable input


        Log.d(TAG, "Attempting private key decryption with provided passphrase on background thread for user: " + currentUserId);

        // Use the dedicated executor service
        executorService.execute(() -> {
            PublicKey publicKey = null;
            PrivateKey decryptedPrivateKey = null;
            SecretKey encryptionKey = null; // Symmetric key derived from passphrase

            byte[] salt = null; // Keep salt bytes
            byte[] encryptedPrivateKeyWithIV = null; // Keep these for potential local save
            byte[] publicKeyBytes = null; // Keep these for potential local save

            String errorMessage = null; // Holds the error message on failure
            boolean isWrongPassphraseError = false; // Flag for specific error type
            boolean localSaveSuccess = false; // Track success of local save IF attempted


            try {
                // 1. Decode Base64 strings back to byte arrays
                Log.d(TAG, "attemptUnlockAsync (Executor): Decoding Base64 data.");
                // Use android.util.Base64 with DEFAULT flag for decoding (assuming Firebase stored it that way)
                salt = Base64.decode(saltBase64, Base64.DEFAULT); // Use android.util.Base64
                encryptedPrivateKeyWithIV = Base64.decode(encryptedPrivateKeyBase64, Base64.DEFAULT); // Use android.util.Base64
                publicKeyBytes = Base64.decode(publicKeyBase64, Base64.DEFAULT); // Use android.util.Base64

                // Check if decoded bytes are valid
                if (salt == null || encryptedPrivateKeyWithIV == null || publicKeyBytes == null || salt.length == 0 || encryptedPrivateKeyWithIV.length == 0 || publicKeyBytes.length == 0) {
                    Log.e(TAG, "attemptUnlockAsync (Executor): Decoded byte arrays are null or empty!");
                    throw new IllegalArgumentException("Decoded key data from Firebase is invalid.");
                }
                Log.d(TAG, "attemptUnlockAsync (Executor): Base64 decoding complete. Salt length: " + salt.length + ", Encrypted Private Key length: " + encryptedPrivateKeyWithIV.length + ", Public Key length: " + publicKeyBytes.length);


                // 2. Derive Secret Key from Passphrase and Salt using PBKDF2
                Log.d(TAG, "attemptUnlockAsync (Executor): Deriving symmetric key from passphrase.");
                encryptionKey = CryptoUtils.deriveKeyFromPassphrase(
                        passphrase, salt, CryptoUtils.PBKDF2_ITERATIONS); // Use your CryptoUtils method
                Log.d(TAG, "attemptUnlockAsync (Executor): Symmetric key derived.");

                // 3. Decrypt Private Key using the derived symmetric key
                Log.d(TAG, "attemptUnlockAsync (Executor): Attempting to decrypt private key.");
                decryptedPrivateKey = CryptoUtils.decryptPrivateKey(encryptedPrivateKeyWithIV, encryptionKey); // Use your CryptoUtils method
                Log.d(TAG, "attemptUnlockAsync (Executor): Private key decryption successful.");

                // 4. Convert Public Key bytes back to PublicKey object
                Log.d(TAG, "attemptUnlockAsync (Executor): Converting public key bytes to object.");
                publicKey = CryptoUtils.bytesToPublicKey(publicKeyBytes); // Use your CryptoUtils method
                Log.d(TAG, "attemptUnlockAsync (Executor): Public key object created.");


                // --- Success: Keys Decrypted ---
                Log.d(TAG, "attemptUnlockAsync (Executor): Private key decrypted successfully for user " + currentUserId);

                // Store BOTH keys securely in memory for the app session using the KeyManager
                YourKeyManager.getInstance().setKeys(currentUserId, publicKey, decryptedPrivateKey); // Use your KeyManager
                Log.d(TAG, "attemptUnlockAsync (Executor): Decrypted RSA keys loaded into KeyManager. Private Key Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable());


                // After successfully loading the user's RSA Private Key into KeyManager,
                // load all conversation keys from Room DB into KeyManager's cache.
                // This happens asynchronously on the databaseWriteExecutor, initiated from here.
                Log.d(TAG, "attemptUnlockAsync (Executor): Private key available. Starting conversation key load from Room.");
                if (!TextUtils.isEmpty(currentUserId)) { // Safety check for userId
                    // Assume ChatDatabase.databaseWriteExecutor exists and loadAllConversationKeysFromRoom is available
                    // (This method should ideally be in ChatDatabase or similar utility, or accessible static)
                    // Let's assume a static helper or accessible instance:
                    // ChatDatabase.loadAllConversationKeysFromRoom(currentUserId); // If static utility exists
                    // OR if you have a DB instance member accessible in your class:
                    // db.loadAllConversationKeysFromRoom(currentUserId); // If instance method exists
                    // OR if you passed DAO here: conversationKeyDao.loadAllIntoKeyManager(currentUserId); // If DAO method exists

                    // For now, let's call the method assumed to be in MainActivity's context,
                    // but this call will be complex across activities.
                    // A cleaner way is to make this a static method or utility class method.

                    // *** REUSING loadAllConversationKeysFromRoom (Assuming it's static/utility or can be called) ***
                    // Let's assume you make this method callable statically or via a shared instance
                    // For now, we'll log the intention. The actual call would need a design fix.
                    // loadAllConversationKeysIntoKeyManager(getApplicationContext(), currentUserId); // Placeholder call structure
                    // Let's use the executor directly to load them via DAO here:
                    loadAllConversationKeysFromRoom(currentUserId); // Calling local helper method

                    // *** END NEW ADDITION ***
                } else {
                    Log.e(TAG, "attemptUnlockAsync (Executor): Cannot load conversation keys from Room, userId is unexpectedly null after decryption success.");
                }


                // --- Handle Local Storage Save ---
                // If decryption was successful, we should save the keys locally
                // using SecureKeyStorageUtil, regardless of the "Remember Me" checkbox state
                // during the *Login* attempt (which brought them here).
                // Saving locally allows for "Remember Me" functionality *next time*.
                Log.d(TAG, "attemptUnlockAsync (Executor): Decryption successful. Attempting local secure save for user: " + currentUserId);
                // Use application context for SecureKeyStorageUtil to avoid leaks
                // Save the symmetric key derived from the passphrase
                boolean saveSymmetricSuccess = SecureKeyStorageUtil.saveSymmetricKey(getApplicationContext(), encryptionKey); // Use your SecureKeyStorageUtil
                // Save the encrypted private key bytes and the public key bytes (these came from Firebase)
                // Pass the original encrypted private key bytes (encryptedPrivateKeyWithIV) and public key bytes (publicKeyBytes)
                boolean saveKeyPairSuccess = SecureKeyStorageUtil.saveEncryptedKeyPair(getApplicationContext(), currentUserId, encryptedPrivateKeyWithIV, publicKeyBytes); // Use your SecureKeyStorageUtil

                localSaveSuccess = saveSymmetricSuccess && saveKeyPairSuccess; // True only if BOTH succeeded

                if (localSaveSuccess) {
                    Log.d(TAG, "attemptUnlockAsync (Executor): All keys saved securely to device storage successfully for user: " + currentUserId);
                    // If local save succeeds here, the "RememberMe" preference should be set to true in SharedPreferences.
                    // This happens on the main thread after this async task.
                } else {
                    // If local save failed despite successful decryption, log error and clear potentially partial local data
                    Log.e(TAG, "attemptUnlockAsync (Executor): FAILED to save one or more keys locally for user: " + currentUserId + " even though decryption succeeded. Clearing potentially partial local data.", new Exception("Local save failed"));
                    // Use application context for SecureKeyStorageUtil
                    SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Clear any partial save
                    // Note: We don't set an errorMessage here for decryption success, the failure will be signalled to the UI via localSaveSuccess=false.
                }
                // --- END Handle Local Storage Save ---


                Log.d(TAG, "attemptUnlockAsync (Executor): Decryption and key loading process finished for user: " + currentUserId);


            } catch (IllegalArgumentException e) {
                // Handle Base64 decoding errors (corrupt data from Firebase?)
                Log.e(TAG, "attemptUnlockAsync (Executor): Base64 decoding failed (corrupt data from Firebase?). Clearing local storage.", e);
                errorMessage = "Invalid key data received from server. Secure messaging unavailable.";
                // Use application context for SecureKeyStorageUtil
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Clear local storage
                YourKeyManager.getInstance().clearKeys(); // Ensure KeyManager state is reset if public key was set
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException |
                     InvalidKeyException | IllegalBlockSizeException | BadPaddingException |
                     InvalidAlgorithmParameterException e) {
                // Catch specific crypto errors (wrong passphrase or data corruption)
                Log.e(TAG, "attemptUnlockAsync (Executor): Key decryption failed for user: " + currentUserId, e);

                // Check if it's likely a wrong passphrase
                if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof InvalidKeyException) {
                    errorMessage = "Incorrect Security Passphrase. Please try again.";
                    isWrongPassphraseError = true; // Set flag for specific error
                } else {
                    errorMessage = "Failed to decrypt keys. Secure messaging unavailable. Error: " + e.getMessage();
                    isWrongPassphraseError = false;
                }
                Log.d(TAG, "attemptUnlockAsync (Executor): Decryption failed with crypto error. Clearing local keys for user: " + currentUserId + ".");
                // Use application context for SecureKeyStorageUtil
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Clear local keys on decryption failure
                YourKeyManager.getInstance().clearKeys(); // Ensure KeyManager state is reset if public key was set

            } catch (Exception e) {
                // Catch any other unexpected errors during crypto ops or related setup
                Log.e(TAG, "attemptUnlockAsync (Executor): An unexpected error occurred during key decryption for user: " + currentUserId, e);
                errorMessage = "An unexpected error occurred. Secure messaging unavailable. Error: " + e.getMessage();
                isWrongPassphraseError = false;
                Log.d(TAG, "attemptUnlockAsync (Executor): Unexpected error during decryption. Clearing local keys for user: " + currentUserId + ".");
                // Use application context for SecureKeyStorageUtil
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Clear local keys
                YourKeyManager.getInstance().clearKeys(); // Ensure KeyManager state is reset if public key was set
            }


            // --- Post result back to Main Thread ---
            final String finalErrorMessage = errorMessage;
            final boolean finalIsWrongPassphraseError = isWrongPassphraseError;
            final boolean finalLocalSaveSuccess = localSaveSuccess; // Pass the success status to Main Thread

            mainHandler.post(() -> { // This runs on the Main Thread
                progressBar.setVisibility(View.GONE); // Hide progress bar
                buttonConfirmUnlock.setEnabled(true); // Re-enable button
                editTextUnlockPassphrase.setEnabled(true); // Re-enable input

                Log.d(TAG, "attemptUnlockAsync (Main Thread): Post-decryption callback started for user: " + currentUserId);
                // Log KeyManager state at post start (should have RSA keys if successful)
                Log.d(TAG, "attemptUnlockAsync (Main Thread): KeyManager state at post start: Private Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable() + ", Public Key Available=" + (YourKeyManager.getInstance().getUserPublicKey() != null) + ", Conv Keys loaded=" + YourKeyManager.getInstance().getAllConversationKeys().size());


                if (finalErrorMessage == null) {
                    // Decryption was successful
                    showUnlockStatus("Account unlocked. Messages decrypted.");
                    Toast.makeText(UnlockAccountPassphereActivity.this, "Account unlocked. Messages decrypted.", Toast.LENGTH_SHORT).show();

                    // *** Handle RememberMe State Saving After Successful Decryption ***
                    // If decryption was successful AND local save succeeded, set "Remember Me" to true.
                    // Otherwise, keep it false. The state is managed in Login/MainActivity.
                    // For this activity, if unlock is successful, we assume the user *intended* to unlock
                    // and might want to be remembered if local save worked.
                    // The actual "Remember Me" preference needs to be updated here if local save succeeded.
                    // Access SharedPreferences directly here.
                    SharedPreferences prefs = getSharedPreferences("CircleUpPrefs", MODE_PRIVATE);
                    if (finalLocalSaveSuccess) {
                        prefs.edit().putBoolean("RememberMe", true).apply(); // Ensure the key name matches Login/MainActivity
                        Log.d(TAG, "attemptUnlockAsync (Main Thread): RememberMe preference set to TRUE after successful unlock and local save.");
                    } else {
                        // If decryption succeeded but local save failed, RememberMe cannot be guaranteed.
                        prefs.edit().putBoolean("RememberMe", false).apply(); // Ensure false if local save failed
                        Log.w(TAG, "attemptUnlockAsync (Main Thread): Local key save failed despite successful decryption. RememberMe preference set FALSE.");
                        // Optionally inform user about local save failure
                        Toast.makeText(UnlockAccountPassphereActivity.this, "Warning: Could not save keys locally. You may need to enter passphrase again.", Toast.LENGTH_LONG).show();
                    }
                    // *** END Handle RememberMe State Saving ***


                    // Now that keys are loaded, finish this activity to go back to MainActivity
                    Log.d(TAG, "attemptUnlockAsync (Main Thread): Unlocking successful. Finishing activity.");
                    finish(); // Finish this activity

                } else {
                    // Decryption failed - show error and handle re-prompt/cancel/navigate
                    Log.e(TAG, "attemptUnlockAsync (Main Thread): Decryption failed for user: " + currentUserId + ". Error: " + finalErrorMessage);
                    showUnlockStatus(finalErrorMessage); // Show the specific error message
                    Toast.makeText(UnlockAccountPassphereActivity.this, finalErrorMessage, Toast.LENGTH_LONG).show();

                    if (finalIsWrongPassphraseError) {
                        // Specific error: Incorrect passphrase
                        editTextUnlockPassphrase.setError("Incorrect Passphrase");
                        editTextUnlockPassphrase.requestFocus();
                        editTextUnlockPassphrase.setText(""); // Clear the incorrect passphrase
                        // Keep button and input enabled for retry
                    } else {
                        // Other decryption errors (e.g., invalid key data, crypto library issue)
                        // This usually indicates a more critical problem, don't allow retry on this screen.
                        buttonConfirmUnlock.setEnabled(false); // Keep button disabled
                        editTextUnlockPassphrase.setEnabled(false); // Keep input disabled
                        // The error message is already shown in showUnlockStatus and Toast.
                        // The "Reset Passphrase" option remains visible.
                        Log.e(TAG, "attemptUnlockAsync (Main Thread): Non-passphrase related decryption failure. Disable unlock inputs.");
                    }

                }
                Log.d(TAG, "attemptUnlockAsync (Main Thread): Post-decryption callback finished for user: " + currentUserId);
            });
            // --- End Post result ---
        });
    }

    // --- NEW Helper method to load Conversation Keys from Room (Needed here too) ---
    // This method loads all conversation keys for the given user from Room DB into YourKeyManager.
    // It is intended to be called on a background thread (like databaseWriteExecutor).
    private void loadAllConversationKeysFromRoom(String ownerUserId) {
        // Ensure ChatDatabase is initialized and ConversationKeyDao is accessible
        // Assuming ChatDatabase.getInstance(context) works and it has a public static databaseWriteExecutor
        // and you can get the dao like ChatDatabase.getInstance(context).conversationKeyDao()

        // Access DAO via ChatDatabase instance (assuming getInstance provides singleton)
        ChatDatabase chatDatabase = ChatDatabase.getInstance(getApplicationContext()); // Use application context for DB
        ConversationKeyDao conversationKeyDao = chatDatabase.conversationKeyDao(); // Get the DAO

        if (conversationKeyDao == null || TextUtils.isEmpty(ownerUserId)) {
            Log.e(TAG, "loadAllConversationKeysFromRoom (Unlock): DAO or userId is null. Cannot load conversation keys.");
            return;
        }

        Log.d(TAG, "loadAllConversationKeysFromRoom (Unlock): Task SUBMITTED to executor for owner: " + ownerUserId);
        // Log KeyManager state BEFORE starting the DB task (for debugging flow)
        Log.d(TAG, "loadAllConversationKeysFromRoom (Unlock): KeyManager state at submission: Private Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable() + ", Conv Keys loaded=" + YourKeyManager.getInstance().getAllConversationKeys().size());


        // Use the shared DB executor from ChatDatabase
        ChatDatabase.databaseWriteExecutor.execute(() -> { // Ensure ChatDatabase.databaseWriteExecutor is static public final
            Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Task STARTED on background thread for owner: " + ownerUserId);
            // Log KeyManager state AFTER task starts (confirm private key is still there)
            Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): KeyManager state at task start: Private Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable() + ", Conv Keys loaded=" + YourKeyManager.getInstance().getAllConversationKeys().size());


            List<ConversationKeyEntity> keyEntities = null;
            try {
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Attempting get keys from DAO for owner: " + ownerUserId);
                keyEntities = conversationKeyDao.getAllKeys(ownerUserId); // Call the DAO query
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): DAO query finished. Result size: " + (keyEntities != null ? keyEntities.size() : "null"));

            } catch (Exception e) { // Catch any exception during DAO query
                Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Error fetching keys from DAO for owner: " + ownerUserId, e);
                // Don't return, proceed with null list and handle errors during processing if list is not null
            }


            // --- Process keys if found ---
            if (keyEntities != null && !keyEntities.isEmpty()) {
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Processing " + keyEntities.size() + " conversation keys found.");
                YourKeyManager keyManager = YourKeyManager.getInstance(); // Get KeyManager instance once (it's a singleton)

                // Ensure private key is STILL available before loading conversation keys into memory
                if (!keyManager.isPrivateKeyAvailable()) {
                    Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Private key became unavailable during conversation key load process! Aborting load.");
                    // Optional: clear the conversation keys from KeyManager if state is inconsistent
                    // keyManager.clearKeys(); // Clear everything from KeyManager (will also clear conv keys)
                    // try { conversationKeyDao.deleteAllKeysForOwner(ownerUserId); } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Error deleting keys after private key disappeared", deleteEx); }
                    return; // STOP processing if private key is missing
                }


                for (ConversationKeyEntity keyEntity : keyEntities) {
                    if (keyEntity == null) {
                        Log.w(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Skipping null keyEntity from Room list.");
                        continue; // Skip null entries
                    }
                    String convId = keyEntity.getConversationId();
                    String decryptedKeyBase64 = keyEntity.getDecryptedKeyBase64();
                    String ownerIdFromEntity = keyEntity.getOwnerUserId();

                    // Basic validation
                    if (TextUtils.isEmpty(convId) || TextUtils.isEmpty(decryptedKeyBase64) || TextUtils.isEmpty(ownerIdFromEntity)) {
                        Log.w(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Skipping key with empty/null essential fields from Room. Conv ID: " + convId + ", Owner (in Entity): " + ownerIdFromEntity);
                        // Optional: you might want to delete corrupt entries from Room here
                        try {
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity)) {
                                conversationKeyDao.deleteKeyForConversation(convId, ownerIdFromEntity);
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Deleted corrupt key from Room DB: " + convId);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Error deleting corrupt key from Room", deleteEx); }
                        continue;
                    }

                    // Important safety check: Ensure the key belongs to the current user
                    if (!ownerIdFromEntity.equals(ownerUserId)) {
                        Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): BUG? Loaded key from Room for owner " + ownerIdFromEntity + " but expecting owner " + ownerUserId + ". Skipping and deleting unexpected entry.");
                        // Delete this unexpected entry from Room DB
                        try {
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity)) {
                                conversationKeyDao.deleteKeyForConversation(convId, ownerIdFromEntity);
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Deleted unexpected key from Room DB: " + convId);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Error deleting unexpected key from Room", deleteEx); }

                        continue;
                    }


                    // Convert Base64 back to byte array and then to SecretKey
                    try {
                        Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Processing key for conv ID: " + convId + ", Owner: " + ownerIdFromEntity);
                        // Use android.util.Base64 with DEFAULT flag for decoding (assuming Room stored it this way)
                        byte[] decryptedKeyBytes = Base64.decode(decryptedKeyBase64, Base64.DEFAULT); // Use android.util.Base64
                        SecretKey conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedKeyBytes); // Use your CryptoUtils method to convert bytes to SecretKey

                        // Store the decrypted SecretKey version in the in-memory KeyManager cache (Use the new method)
                        keyManager.setConversationKey(convId, conversationAESKey); // *** MODIFIED KEYMANAGER CALL - ADDED TIMESTAMP ***
                        Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Key for conv ID " + convId + " LOADED into KeyManager. Current KeyManager conv key count: " + keyManager.getAllConversationKeys().size()); // Add log


                    } catch (IllegalArgumentException e) { // Base64 decoding error
                        Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Error decoding Base64 conversation key from Room for conv ID: " + convId + ". Deleting corrupt entry.", e);
                        // Delete corrupt entry from Room
                        try {
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity)) {
                                conversationKeyDao.deleteKeyForConversation(convId, ownerIdFromEntity);
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Deleted corrupt key from Room DB: " + convId);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Error deleting corrupt key from Room", deleteEx); }
                    } catch (Exception e) { // Catch any other unexpected errors during conversion
                        Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Unexpected error processing conversation key from Room for conv ID: " + convId, e);
                        // Decide if you delete on other errors - generally safer to delete corrupt data
                        try {
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity)) {
                                conversationKeyDao.deleteKeyForConversation(convId, ownerIdFromEntity); // Optional delete
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Deleted key from Room DB after processing error: " + convId);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Error deleting key from Room after processing error", deleteEx); }
                    }
                }
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): FINISHED processing and loading all found keys into KeyManager. Final count: " + YourKeyManager.getInstance().getAllConversationKeys().size());

            } else {
                // No keys found in Room for this user, which is fine (maybe first time using encrypted chat or Room was cleared)
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): No conversation keys found in Room DB for owner: " + ownerUserId);
            }
            Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Task FINISHED on background thread.");


            // *** Trigger UI Update on Main Thread After Conversation Keys are Loaded ***
            // This is important so fragments like ChatFragment know keys are available and can decrypt previews.
            mainHandler.post(() -> { // Post to the main thread using the mainHandler
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Unlock): Posted completion signal to Main Thread.");
                // Signal MainActivity to potentially refresh its fragments
                // This requires a way for UnlockAccountPassphereActivity to communicate back to MainActivity.
                // A simple way is to send a Broadcast or rely on MainActivity's onResume/onStart checking KeyManager state.
                // Since we finish this activity, MainActivity's onStart will run next, which checks KeyManager.
                // Also, ChatFragment's LiveData observer on the chat list should re-run when the activity resumes
                // and should trigger re-processing/decryption in the adapter.
                Log.d(TAG, "loadAllConversationKeysFromRoom (Main Thread): Conversation keys loaded into KeyManager. MainActivity's onStart/onResume will pick this up.");
            });
            // *** END Trigger UI Update ***

        });
    }
    // --- END NEW Helper method ---



    // --- Helper Navigation Method to go back to Login ---
    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity from UnlockAccountPassphereActivity.");
        Intent loginIntent = new Intent(UnlockAccountPassphereActivity.this, Login.class); // Ensure Login activity exists
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish(); // Finish this activity
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shut down the executor service
        if (executorService != null && !executorService.isShutdown()) {
            Log.d(TAG, "ExecutorService shutting down in UnlockAccountPassphereActivity.");
            executorService.shutdownNow(); // Attempt to stop executing tasks
        }
        // Dismiss progress dialog if it's showing to prevent window leaks
        if (progressBar.getVisibility() == View.VISIBLE) {
            // ProgressBar doesn't have dismiss, just ensure visibility is GONE if needed,
            // but it's handled in post callback.
        }
        Log.d(TAG, "🔴 UnlockAccountPassphereActivity onDestroy called.");
    }
}




//
//public class UnlockAccountPassphereActivity extends AppCompatActivity {
//
//    private static final String TAG = "UnlockAccountAct";
//
//    private TextView statusText;
//    private TextInputLayout passphraseInputLayout; // Added TextInputLayout
//    private TextInputEditText editTextPassphrase;
//    private Button buttonConfirmUnlock;
//    private ProgressBar progressBar;
//
//    private FirebaseAuth auth;
//    private DatabaseReference usersRef;
//    private String currentUserId;
//
//    private ExecutorService executorService; // For async tasks (Firebase fetch, Crypto, Room save)
//    private Handler mainHandler; // For posting results back to the main thread
//
//    private ChatDatabase db; // Room DB
//    private ConversationKeyDao conversationKeyDao; // Room DAO for conversation keys
//
//    // Variables to hold fetched key data from Firebase (to avoid fetching multiple times)
//    private String fetchedEncryptedPrivateKeyBase64;
//    private String fetchedPublicKeyBase64;
//    private String fetchedSaltBase64;
//
//    // --- Preferences for RememberMe ---
//    private SharedPreferences sharedPreferences;
//    private static final String PREFS_NAME = "CircleUpPrefs";
//    private static final String PREF_REMEMBER_ME = "RememberMe";
//    // --- End Preferences ---
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_unlock_account_passphere); // Use your new layout XML
//        Log.d(TAG, "🟢 UnlockAccountActivity onCreate.");
//
//        // Setup Toolbar as ActionBar
//        Toolbar toolbar = findViewById(R.id.toolbar_unlock_account); // Ensure toolbar ID matches layout
//        if (toolbar != null) {
//            setSupportActionBar(toolbar);
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setTitle(R.string.unlock_account_title); // Use string resource
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
//            }
//        } else {
//            Log.w(TAG, "Toolbar not found in layout with ID toolbar_unlock_account");
//        }
//
//
//        auth = FirebaseAuth.getInstance();
//        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
//
//        // Get current user and check authentication
//        FirebaseUser currentUser = auth.getCurrentUser();
//        if (currentUser == null) {
//            Log.e(TAG, "User not authenticated in UnlockAccountActivity! Redirecting to Login.");
//            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
//            sendUserToLoginActivity(); // Redirect to Login activity
//            return; // Stop onCreate execution
//        }
//        currentUserId = currentUser.getUid();
//        Log.d(TAG, "Current User ID: " + currentUserId);
//
//        // Initialize UI elements
//        statusText = findViewById(R.id.unlock_status_text); // Ensure ID matches layout
//        passphraseInputLayout = findViewById(R.id.unlock_passphrase_input_layout); // Ensure ID matches layout
//        editTextPassphrase = findViewById(R.id.editTextUnlockPassphrase); // Ensure ID matches layout
//        buttonConfirmUnlock = findViewById(R.id.buttonConfirmUnlock); // Ensure ID matches layout
//        progressBar = findViewById(R.id.unlock_progressBar); // Ensure ID matches layout
//
//        // Initialize Executors and Handler
//        executorService = Executors.newSingleThreadExecutor();
//        mainHandler = new Handler(Looper.getMainLooper());
//
//        // Initialize Room DB and DAO
//        db = ChatDatabase.getInstance(getApplicationContext()); // Use application context
//        conversationKeyDao = db.conversationKeyDao();
//
//        // Initialize SharedPreferences
//        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE); // Use same prefs file as Login
//
//        // Check initial key status and update UI
//        // If keys are already unlocked, no need to show this screen
//        if (YourKeyManager.getInstance().isPrivateKeyAvailable()) {
//            Log.d(TAG, "Private key is already available. Account is unlocked. Finishing activity.");
//            Toast.makeText(this, "Account is already unlocked.", Toast.LENGTH_SHORT).show();
//            finish(); // Finish immediately if already unlocked
//            return; // Stop onCreate execution
//        }
//
//        // If not already unlocked, show the prompt and fetch key data from Firebase
//        statusText.setText("Enter your security passphrase to unlock encrypted messages.");
//        passphraseInputLayout.setVisibility(View.VISIBLE);
//        editTextPassphrase.setText(""); // Clear any previous input
//        buttonConfirmUnlock.setVisibility(View.VISIBLE);
//        buttonConfirmUnlock.setEnabled(true); // Enable button
//        progressBar.setVisibility(View.GONE); // Ensure progress bar is hidden
//
//
//        // Fetch user's key data (encrypted private key, public key, salt) from Firebase
//        // This is needed for decryption. Only fetch once when the activity starts.
//        fetchKeyDataFromFirebase();
//
//        // Set click listener for the Unlock button
//        buttonConfirmUnlock.setOnClickListener(v -> onConfirmUnlockButtonClick());
//
//        Log.d(TAG, "✅ onCreate finished in UnlockAccountActivity");
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed(); // Handle back button click on toolbar
//        return true;
//    }
//
//
//    // --- Fetch Key Data from Firebase ---
//    // This method fetches the encrypted key components needed for decryption.
//    private void fetchKeyDataFromFirebase() {
//        if (TextUtils.isEmpty(currentUserId) || usersRef == null) {
//            Log.e(TAG, "Cannot fetch key data: currentUserId or usersRef is null.");
//            showFetchError("Error fetching user data.");
//            return;
//        }
//
//        // Show a temporary status or progress indication if needed during fetch
//        statusText.setText("Fetching key data...");
//        buttonConfirmUnlock.setEnabled(false); // Disable button while fetching
//
//        Log.d(TAG, "Fetching encrypted key data from Firebase for user: " + currentUserId);
//
//        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // This runs on the main thread
//                if (snapshot.exists()) {
//                    // Extract key data Base64 strings
//                    fetchedEncryptedPrivateKeyBase64 = snapshot.child("encryptedPrivateKey").getValue(String.class);
//                    fetchedPublicKeyBase64 = snapshot.child("publicKey").getValue(String.class);
//                    fetchedSaltBase64 = snapshot.child("encryptionSalt").getValue(String.class);
//
//                    // Check if key data is complete
//                    if (TextUtils.isEmpty(fetchedEncryptedPrivateKeyBase64) || TextUtils.isEmpty(fetchedPublicKeyBase64) || TextUtils.isEmpty(fetchedSaltBase64)) {
//                        Log.e(TAG, "Fetched key data from Firebase is incomplete or empty.");
//                        showFetchError("Incomplete key data found. Please set up your profile again.");
//                        // Consider clearing local storage if incomplete data indicates corruption
//                        SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Use application context
//                    } else {
//                        Log.d(TAG, "Key data fetched successfully from Firebase. Ready for unlock attempt.");
//                        statusText.setText("Enter your security passphrase to unlock encrypted messages."); // Reset status text
//                        buttonConfirmUnlock.setEnabled(true); // Re-enable unlock button
//                    }
//                } else {
//                    // User data node not found in Firebase for authenticated user (shouldn't happen if login worked, but defensive)
//                    Log.e(TAG, "User data node not found in Firebase for key fetch for user: " + currentUserId + ".");
//                    showFetchError("User data missing in server. Please log in again.");
//                    // Consider clearing local storage if data is missing
//                    SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Use application context
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // This runs on the main thread
//                Log.e(TAG, "Firebase key fetch failed: " + error.getMessage(), error.toException());
//                showFetchError("Failed to fetch key data from server. Check network.");
//                // Consider clearing local storage on fetch failure
//                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId); // Use application context
//            }
//        });
//    }
//
//
//    // --- Helper to show error if key data fetch failed ---
//    private void showFetchError(String message) {
//        statusText.setText("Error: " + message);
//        statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark)); // Red color for error
//        buttonConfirmUnlock.setEnabled(false); // Keep button disabled
//        passphraseInputLayout.setVisibility(View.GONE); // Hide input field
//    }
//
//
//    // --- Handle Confirm Unlock Button Click ---
//    private void onConfirmUnlockButtonClick() {
//        String passphrase = editTextPassphrase.getText().toString();
//
//        if (TextUtils.isEmpty(passphrase)) {
//            editTextPassphrase.setError("Passphrase is required");
//            editTextPassphrase.requestFocus();
//            return;
//        }
//        if (passphrase.length() < 6) { // Use the same minimum length as signup
//            editTextPassphrase.setError("Passphrase must be at least 6 characters");
//            editTextPassphrase.requestFocus();
//            return;
//        }
//
//
//        // Check if key data was successfully fetched from Firebase first
//        if (TextUtils.isEmpty(fetchedEncryptedPrivateKeyBase64) || TextUtils.isEmpty(fetchedPublicKeyBase64) || TextUtils.isEmpty(fetchedSaltBase64)) {
//            Log.e(TAG, "Key data not available for decryption. Cannot attempt unlock.");
//            showFetchError("Key data not loaded. Try again or log out and back in.");
//            return; // Stop if data is not ready
//        }
//
//
//        // Disable input and button, show progress
//        editTextPassphrase.setEnabled(false);
//        buttonConfirmUnlock.setEnabled(false);
//        progressBar.setVisibility(View.VISIBLE);
//        statusText.setText("Unlocking..."); // Update status text
//        statusText.setTextColor(ContextCompat.getColor(this, android.R.color.black)); // Reset color
//
//        Log.d(TAG, "Confirm Unlock button clicked. Attempting decryption.");
//
//        // Start the asynchronous decryption process
//        decryptPrivateKeyAsync(passphrase, fetchedPublicKeyBase64, fetchedEncryptedPrivateKeyBase64, fetchedSaltBase64);
//    }
//
//
//    // --- Decrypt Private Key (Async) ---
//    // This logic is adapted from Login.decryptPrivateKeyAsync
//    private void decryptPrivateKeyAsync(String passphrase, String publicKeyBase64, String encryptedPrivateKeyBase64, String saltBase64) {
//
//        // Run decryption and KeyManager/Room/Local storage save on the background executor
//        executorService.execute(() -> {
//            Exception processException = null;
//            SecretKey derivedEncryptionKey = null;
//            PrivateKey decryptedPrivateKey = null;
//            PublicKey userPublicKey = null;
//            boolean decryptionSuccessful = false;
//            byte[] decryptedAesKeyBytes = null; // To hold raw bytes for Room save
//
//            Log.d(TAG, "Decrypting private key on background thread.");
//
//            try {
//                // Decode Base64 key components fetched from Firebase
//                byte[] salt = Base64.decode(saltBase64, Base64.DEFAULT); // Use android.util.Base64
//                byte[] encryptedPrivateKeyWithIV = Base64.decode(encryptedPrivateKeyBase64, Base64.DEFAULT); // Use android.util.Base64
//                byte[] publicKeyBytes = Base64.decode(publicKeyBase64, Base64.DEFAULT); // Use android.util.Base64
//
//                // Basic validity checks for decoded bytes
//                if (salt == null || encryptedPrivateKeyWithIV == null || publicKeyBytes == null || salt.length == 0 || encryptedPrivateKeyWithIV.length == 0 || publicKeyBytes.length == 0) {
//                    Log.e(TAG, "Decoded key data from Base64 is null or empty!");
//                    throw new IllegalArgumentException("Invalid key data format.");
//                }
//
//
//                // Derive Symmetric Key from passphrase and salt
//                derivedEncryptionKey = CryptoUtils.deriveKeyFromPassphrase(passphrase, salt, CryptoUtils.PBKDF2_ITERATIONS);
//
//                // Decrypt Private Key using the derived key
//                decryptedPrivateKey = CryptoUtils.decryptPrivateKey(encryptedPrivateKeyWithIV, derivedEncryptionKey); // Throws on wrong passphrase or corrupt data
//                decryptedAesKeyBytes = CryptoUtils.privateKeyToBytes(decryptedPrivateKey); // Get bytes of the decrypted private key (Needed for Room save)
//
//
//                // Convert Public Key bytes to PublicKey object
//                userPublicKey = CryptoUtils.bytesToPublicKey(publicKeyBytes);
//
//                // If we reached here without throwing, decryption was successful
//                decryptionSuccessful = true;
//                Log.d(TAG, "Private key decryption successful!");
//
//                // --- Step 1: Load Keys into KeyManager ---
//                YourKeyManager.getInstance().setKeys(currentUserId, userPublicKey, decryptedPrivateKey);
//                Log.d(TAG, "Decrypted keys loaded into YourKeyManager.");
//
//                // --- Step 2: Save Keys to Secure Local Storage for "Remember Me" ---
//                // Save the derived symmetric key and the encrypted key pair (which came from Firebase)
//                // This ensures the local load on next app start (if RememberMe is true) works.
//                // We assume here that the user *intends* to be remembered if they manually unlock.
//                // If you need a checkbox for RememberMe on this screen, add it and check its state.
//                boolean saveSymmetricSuccess = SecureKeyStorageUtil.saveSymmetricKey(getApplicationContext(), derivedEncryptionKey);
//                boolean saveKeyPairSuccess = SecureKeyStorageUtil.saveEncryptedKeyPair(getApplicationContext(), currentUserId, encryptedPrivateKeyWithIV, publicKeyBytes);
//
//                if (saveSymmetricSuccess && saveKeyPairSuccess) {
//                    Log.d(TAG, "Keys successfully saved to local secure storage.");
//                    // If local save is successful, ensure RememberMe is true.
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putBoolean(PREF_REMEMBER_ME, true); // Set RememberMe to TRUE here
//                    editor.apply();
//                    Log.d(TAG, "RememberMe preference set to TRUE after successful local save.");
//                } else {
//                    Log.e(TAG, "Failed to save keys to local secure storage after unlock!");
//                    // If local save fails, ensure RememberMe is FALSE.
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putBoolean(PREF_REMEMBER_ME, false); // Set RememberMe to FALSE
//                    editor.apply();
//                    Log.d(TAG, "RememberMe preference set to FALSE due to local save failure.");
//                    // Clear any potentially partial local data
//                    SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId);
//                }
//
//
//            } catch (IllegalArgumentException e) { // Base64 decode error, invalid key spec etc.
//                Log.e(TAG, "Error processing key data during decryption", e);
//                processException = new Exception("Corrupt key data.");
//            } catch (NoSuchAlgorithmException | NoSuchPaddingException |
//                     InvalidKeyException | IllegalBlockSizeException |
//                     BadPaddingException | InvalidAlgorithmParameterException |
//                     InvalidKeySpecException e) {
//                // Crypto decryption failed (e.g., wrong passphrase, corrupt data)
//                Log.e(TAG, "Private key decryption failed", e);
//                if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof InvalidKeyException) {
//                    // These often indicate incorrect passphrase
//                    processException = new Exception("Incorrect Security Passphrase.");
//                } else {
//                    // Other crypto errors
//                    processException = new Exception("Decryption failed.");
//                }
//            }
//            // --- End Decryption Attempt ---
//            catch (Exception e) {
//                // Catch any other unexpected errors during the async process
//                Log.e(TAG, "An unexpected error occurred during decryption", e);
//                processException = new Exception("An unexpected error occurred.");
//            }
//
//
//            // --- Step 3: Load Conversation Keys from Room into KeyManager Cache ---
//            // This needs to happen AFTER the user's Private Key is successfully loaded into KeyManager.
//            // Run this on the shared database executor (or can queue directly on executorService if needed, but DB executor is better).
//            if (decryptionSuccessful && conversationKeyDao != null && !TextUtils.isEmpty(currentUserId)) { // Only load conv keys if decryption was successful
//                Log.d(TAG, "Private key unlocked. Triggering conversation key load from Room.");
//                loadAllConversationKeysFromRoom(currentUserId); // Call the helper method (runs on DB executor)
//            } else if (decryptionSuccessful && conversationKeyDao == null) {
//                Log.e(TAG, "ConversationKeyDao is null, cannot load conversation keys from Room.");
//                // This is a non-critical failure for unlock, but user might need to restart app for keys to load.
//            } else {
//                Log.d(TAG, "Conversation key load from Room skipped: Decryption failed.");
//            }
//
//
//            // Post the result back to the main thread
//            postUnlockResult(decryptionSuccessful, processException != null ? processException.getMessage() : null, processException instanceof BadPaddingException || processException instanceof IllegalBlockSizeException || processException instanceof InvalidKeyException);
//        });
//    }
//
//
//    // --- Helper method to load Conversation Keys from Room ---
//    // Copy this method from Login.java or MainActivity.java
//    // This method loads all conversation keys from Room DB into YourKeyManager.
//    // It is intended to be called on a background thread (like executorService or databaseWriteExecutor).
//    private void loadAllConversationKeysFromRoom(String ownerUserId) {
//        // Ensure ConversationKeyDao and userId are available
//        // Use the member variable 'conversationKeyDao'
//        if (conversationKeyDao == null || TextUtils.isEmpty(ownerUserId)) {
//            Log.e(TAG, "loadAllConversationKeysFromRoom (UnlockAccount): DAO or userId is null. Cannot load conversation keys.");
//            return;
//        }
//
//        Log.d(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - Executor): Task STARTED on background thread for owner: " + ownerUserId);
//        Log.d(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - Executor): KeyManager state at task start: Private Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable() + ", Conv Keys loaded=" + YourKeyManager.getInstance().getAllConversationKeys().size());
//
//
//        // Use the shared DB executor from ChatDatabase
//        // It's crucial that this runs on a background thread
//        ChatDatabase.databaseWriteExecutor.execute(() -> { // Ensure ChatDatabase.databaseWriteExecutor is static public final
//            Log.d(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): Task STARTED on background thread for owner: " + ownerUserId);
//
//            List<ConversationKeyEntity> keyEntities = null;
//            try {
//                Log.d(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): Attempting get keys from DAO for owner: " + ownerUserId);
//                keyEntities = conversationKeyDao.getAllKeys(ownerUserId); // Call the DAO query
//                Log.d(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): DAO query finished. Result size: " + (keyEntities != null ? keyEntities.size() : "null"));
//
//            } catch (Exception e) { // Catch any exception during DAO query
//                Log.e(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): Error fetching keys from DAO for owner: " + ownerUserId, e);
//                // Don't return, proceed with null list
//            }
//
//
//            // --- Process keys if found ---
//            if (keyEntities != null && !keyEntities.isEmpty()) {
//                Log.d(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): Processing " + keyEntities.size() + " conversation keys found.");
//                YourKeyManager keyManager = YourKeyManager.getInstance(); // Get KeyManager instance once
//
//                // Ensure private key is STILL available before loading conversation keys into memory
//                if (!keyManager.isPrivateKeyAvailable()) {
//                    Log.e(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): Private key became unavailable during conversation key load process! Aborting load.");
//                    // Optionally clear the conversation keys from KeyManager and Room if state is inconsistent
//                    // keyManager.clearKeys(); // Clear everything from KeyManager (will also clear conv keys)
//                    // try { conversationKeyDao.deleteAllKeysForOwner(ownerUserId); } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor): Error deleting keys after private key disappeared", deleteEx); }
//                    return; // STOP processing if private key is missing
//                }
//
//
//                for (ConversationKeyEntity keyEntity : keyEntities) {
//                    if (keyEntity == null) { Log.w(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): Skipping null keyEntity."); continue; }
//                    String convId = keyEntity.getConversationId();
//                    String decryptedKeyBase64 = keyEntity.getDecryptedKeyBase64();
//                    String ownerIdFromEntity = keyEntity.getOwnerUserId();
//
//                    // Basic validation
//                    if (TextUtils.isEmpty(convId) || TextUtils.isEmpty(decryptedKeyBase64) || TextUtils.isEmpty(ownerIdFromEntity)) {
//                        Log.w(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): Skipping key with empty/null fields. Conv ID: " + convId);
//                        try { if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity)) { conversationKeyDao.deleteKeyById(convId, ownerIdFromEntity); Log.d(TAG, "Deleted corrupt key from Room DB: " + convId); } } catch (Exception deleteEx) { Log.e(TAG, "Error deleting corrupt key from Room", deleteEx); }
//                        continue;
//                    }
//
//                    // Important safety check: Ensure the key belongs to the current user
//                    if (!ownerIdFromEntity.equals(ownerUserId)) {
//                        Log.e(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): BUG? Loaded key from Room for owner " + ownerIdFromEntity + " but expecting " + ownerUserId + ". Deleting.");
//                        try { if (conversationKeyDao != null) { conversationKeyDao.deleteKeyById(convId, ownerIdFromEntity); } } catch (Exception deleteEx) { Log.e(TAG, "Error deleting unexpected key from Room", deleteEx); }
//                        continue;
//                    }
//
//
//                    // Convert Base64 back to byte array and then to SecretKey
//                    try {
//                        byte[] decryptedKeyBytes = Base64.decode(decryptedKeyBase64, Base64.DEFAULT); // Use android.util.Base64
//                        if (decryptedKeyBytes == null || decryptedKeyBytes.length == 0) {
//                            Log.e(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): Decoded key bytes are null or empty for conv ID: " + convId);
//                            throw new IllegalArgumentException("Invalid decrypted key data bytes."); // Throw to catch below
//                        }
//                        SecretKey conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedKeyBytes); // Use your CryptoUtils method
//
//                        // Store the decrypted SecretKey in the in-memory KeyManager
//                        keyManager.setConversationKey(convId, conversationAESKey);
//                        // Log.d(TAG, "Key for conv ID " + convId + " LOADED into KeyManager."); // Verbose log
//
//                    } catch (IllegalArgumentException e) { // Base64 decoding or invalid bytes format for SecretKey
//                        Log.e(TAG, "Error decoding Base64 or converting to SecretKey for conv ID: " + convId + ". Deleting corrupt entry.", e);
//                        try { if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity)) { conversationKeyDao.deleteKeyById(convId, ownerIdFromEntity); Log.d(TAG, "Deleted corrupt key from Room DB: " + convId); } } catch (Exception deleteEx) { Log.e(TAG, "Error deleting corrupt key from Room", deleteEx); }
//                    } catch (Exception e) { // Catch any other unexpected errors
//                        Log.e(TAG, "Unexpected error processing conversation key from Room for conv ID: " + convId, e);
//                        try { if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity)) { conversationKeyDao.deleteKeyById(convId, ownerIdFromEntity); } } catch (Exception deleteEx) { Log.e(TAG, "Error deleting key from Room after processing error", deleteEx); }
//                    }
//                }
//                Log.d(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): FINISHED processing and loading keys into KeyManager. Final count: " + YourKeyManager.getInstance().getAllConversationKeys().size());
//
//            } else {
//                Log.d(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): No conversation keys found in Room DB for owner: " + ownerUserId);
//            }
//            Log.d(TAG, "loadAllConversationKeysFromRoom (UnlockAccount - DB Executor): Task FINISHED.");
//            // No need to post back to main thread from here, the main unlock result post handles UI updates.
//        });
//    }
//
//
//    // --- Post Unlock Result back to Main Thread and Update UI ---
//    private void postUnlockResult(boolean success, String errorMessage, boolean isWrongPassphrase) {
//        mainHandler.post(() -> { // This runs on the Main Thread
//            progressBar.setVisibility(View.GONE); // Hide progress bar
//            editTextPassphrase.setEnabled(true); // Re-enable input
//            buttonConfirmUnlock.setEnabled(true); // Re-enable button
//
//            if (success) {
//                Log.d(TAG, "Account unlock successful in UnlockAccountActivity.");
//                statusText.setText("Account unlocked successfully!");
//                statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark)); // Green color for success
//                editTextPassphrase.setText(""); // Clear passphrase input on success
//
//
//                // Keys are now loaded into KeyManager.
//                // Signal MainActivity to refresh its UI components (like ChatFragment)
//                // The simplest way is to finish this activity. When MainActivity resumes,
//                // its onResume will re-check KeyManager state and signal fragments.
//                Toast.makeText(this, "Secure account unlocked.", Toast.LENGTH_SHORT).show();
//                finish(); // Finish this activity to go back to MainActivity
//
//            } else {
//                // Unlock failed - show error message
//                Log.e(TAG, "Account unlock failed: " + errorMessage);
//                statusText.setText("Unlock Failed: " + (errorMessage != null ? errorMessage : "Unknown error"));
//                statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark)); // Red color for failure
//
//                if (isWrongPassphrase) {
//                    editTextPassphrase.setError("Incorrect Passphrase");
//                    editTextPassphrase.requestFocus();
//                    Toast.makeText(this, "Incorrect Passphrase. Please try again.", Toast.LENGTH_SHORT).show();
//                } else {
//                    // For other errors, clear passphrase maybe?
//                    editTextPassphrase.setText(""); // Clear passphrase on other errors
//                    Toast.makeText(this, "Failed to unlock account.", Toast.LENGTH_SHORT).show();
//                }
//
//                // Keys remain NOT loaded in KeyManager. Local storage cleared by decrypt process on failure. RememberMe remains false.
//                // User stays on this screen to try again.
//            }
//        });
//    }
//
//
//    // --- Helper Navigation Method to go back to Login ---
//    // Used if user is unexpectedly not authenticated in this activity
//    private void sendUserToLoginActivity() {
//        Log.d(TAG, "Redirecting to Login Activity from UnlockAccount.");
//        Intent loginIntent = new Intent(UnlockAccountPassphereActivity.this, Login.class); // Ensure Login activity exists
//        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(loginIntent);
//        finish(); // Finish this activity
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        // Shut down the executor service
//        if (executorService != null && !executorService.isShutdown()) {
//            Log.d(TAG, "ExecutorService shutting down in UnlockAccountActivity.");
//            executorService.shutdownNow(); // Attempt to stop executing tasks
//        }
//        Log.d(TAG, "🔴 UnlockAccountActivity onDestroy called.");
//    }
//}