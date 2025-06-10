package com.sana.circleup;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Make sure to import Toolbar
import android.content.Intent; // Make sure to import Intent
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils; // Make sure to import TextUtils
import android.util.Log; // Make sure to import Log
import android.view.LayoutInflater;
import android.view.MenuItem; // Make sure to import MenuItem
import android.view.View; // Make sure to import View
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // Make sure to import Toast

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputEditText; // Import TextInputEditText
import com.google.android.material.textfield.TextInputLayout; // Import TextInputLayout
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sana.circleup.encryptionfiles.CryptoUtils;
import com.sana.circleup.encryptionfiles.SecureKeyStorageUtil;
import com.sana.circleup.encryptionfiles.YourKeyManager;


import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Import for PublicKey/PrivateKey conversion (used later in code logic)
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;


public class RecoverAccountWithCodeActivity extends AppCompatActivity {

    private static final String TAG = "RecoverAccountAct";

    // --- UI Elements ---
    private TextInputEditText editTextRecoveryCode;
    private Button buttonSubmitRecoveryCode;
    private TextView recoveryWarningTextView;
    private TextView recoveryStatusTextView;
    private ProgressBar recoveryProgressBar;
    private TextView textViewFallbackToReset; // Link to Reset Passphrase

    // --- Firebase ---
    private FirebaseAuth auth;
    // No need for DatabaseReference to Users here, we already have keys in Base64 from Intent or fetch them.
    // We WILL need it later if we implement setting a NEW passphrase after recovery.

    // --- Data from Intent ---
    private String currentUserId;
    // Optionally pass public key if already fetched in UnlockActivity
    // private String publicKeyBase64; // Declare if you pass it via Intent

    // --- Background Task Execution ---
    private ExecutorService executorService;
    private Handler mainHandler;
    private DatabaseReference usersRef;

    // --- State ---
    private boolean isProcessing = false; // To prevent multiple clicks


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_account_with_code); // Set your layout
        Log.d(TAG, "🟢 RecoverAccountWithCodeActivity onCreate.");

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_recover_code); // Match your layout ID
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Recover Account"); // Set title
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
            }
        } else {
            Log.w(TAG, "Toolbar not found in layout with ID toolbar_recover_code");
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // --- Get User ID from Intent ---
        // The userId should have been passed from UnlockAccountPassphereActivity
        currentUserId = getIntent().getStringExtra("userId");
        // Optionally get public key Base64 if passed
        // publicKeyBase64 = getIntent().getStringExtra("publicKeyBase64"); // If you passed it

        // --- Critical Check: Is User Authenticated and did we get a User ID? ---
        if (currentUser == null || TextUtils.isEmpty(currentUserId) || !currentUser.getUid().equals(currentUserId)) {
            Log.e(TAG, "Authentication error or missing/mismatched userId in RecoverAccountWithCodeActivity!");
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show();
            // Redirect to Login Activity on critical auth failure
            sendUserToLoginActivity();
            return; // Stop onCreate execution
        }
        Log.d(TAG, "Current User ID: " + currentUserId);

        initializeViews(); // Initialize UI elements

        // Setup Executor and Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());


        // --- Set Click Listeners ---
        buttonSubmitRecoveryCode.setOnClickListener(v -> {
            if (!isProcessing) { // Prevent double-clicking
                String recoveryCode = editTextRecoveryCode.getText().toString().trim();
                onSubmitRecoveryCode(recoveryCode); // Call the method to handle submission
            } else {
                Log.d(TAG, "Submit button clicked while processing.");
            }
        });

        // Set click listener for fallback to Reset Passphrase
        if (textViewFallbackToReset != null) {
            textViewFallbackToReset.setOnClickListener(v -> {
                Log.d(TAG, "Fallback to Reset Passphrase clicked. Navigating.");
                Intent intent = new Intent(RecoverAccountWithCodeActivity.this, ResetPassphraseActivity.class); // Ensure ResetPassphraseActivity exists
                startActivity(intent);
                finish(); // Finish this activity
            });
        }


        Log.d(TAG, "✅ onCreate finished in RecoverAccountWithCodeActivity");
    }


    // --- Initialize UI views ---
    private void initializeViews() {
        editTextRecoveryCode = findViewById(R.id.editTextRecoveryCode); // Match your layout ID
        buttonSubmitRecoveryCode = findViewById(R.id.buttonSubmitRecoveryCode); // Match your layout ID
        recoveryWarningTextView = findViewById(R.id.recoveryWarningTextView); // Match your layout ID
        recoveryStatusTextView = findViewById(R.id.recoveryStatusTextView); // Match your layout ID
        recoveryProgressBar = findViewById(R.id.recoveryProgressBar); // Match your layout ID
        textViewFallbackToReset = findViewById(R.id.textViewFallbackToReset); // Match your layout ID
    }

    // --- Helper to Update Status Text and Visibility ---
    private void updateStatusUI(String message, boolean showProgress, boolean enableInput) {
        mainHandler.post(() -> { // Ensure this runs on the main thread
            if (recoveryStatusTextView != null) {
                recoveryStatusTextView.setText(message);
                recoveryStatusTextView.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
            }
            if (recoveryProgressBar != null) {
                recoveryProgressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
            }
            if (editTextRecoveryCode != null) {
                editTextRecoveryCode.setEnabled(enableInput);
            }
            if (buttonSubmitRecoveryCode != null) {
                buttonSubmitRecoveryCode.setEnabled(enableInput);
            }
            // Manage isProcessing state
            isProcessing = showProgress;

            Log.d(TAG, "Status UI Update: Message='" + message + "', Progress=" + showProgress + ", Input Enabled=" + enableInput);
        });
    }


    // --- Method to handle Recovery Code Submission (Logic will be added here) ---
    private void onSubmitRecoveryCode(String recoveryCode) {
        if (TextUtils.isEmpty(recoveryCode)) {
            editTextRecoveryCode.setError("Recovery code is required.");
            editTextRecoveryCode.requestFocus();
            return;
        }

        // Start the recovery process on a background thread
        attemptRecoveryAsync(recoveryCode);
    }


    // --- Background Task for Recovery Logic (Will be implemented next) ---

    // --- Background Task for Recovery Logic (Step 8 Implementation) ---
    private void attemptRecoveryAsync(String recoveryCode) {
        // Update UI to show processing
        updateStatusUI("Attempting recovery...", true, false);

        if (TextUtils.isEmpty(currentUserId)) {
            Log.e(TAG, "attemptRecoveryAsync: currentUserId is null or empty! Aborting.");
            updateStatusUI("Error: User ID missing.", false, true);
            Toast.makeText(this, "Error: User ID missing. Please log in again.", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity(); // Redirect on critical error
            return;
        }

        executorService.execute(() -> {
            PrivateKey reconstructedPrivateKey = null;
            PublicKey fetchedPublicKey = null;
            PublicKey derivedPublicKeyFromReconstructed = null;
            String errorMessage = null;

            try {
                // 1. Use CryptoUtils.recoveryCodeToPrivateKey to reconstruct the Private Key from the code
                Log.d(TAG, "Attempting to reconstruct Private Key from recovery code.");
                reconstructedPrivateKey = CryptoUtils.recoveryCodeToPrivateKey(recoveryCode);
                Log.d(TAG, "Private Key reconstructed successfully from code.");

                // 2. Fetch the user's Public Key from Firebase
                Log.d(TAG, "Fetching user's Public Key from Firebase for user: " + currentUserId);
                // Using Tasks.await to make the Firebase fetch synchronous on this background thread
                DataSnapshot userSnapshot = Tasks.await(usersRef.child(currentUserId).child("publicKey").get()); // Fetch only the publicKey node

                if (userSnapshot.exists()) {
                    String publicKeyBase64FromFirebase = userSnapshot.getValue(String.class);
                    if (!TextUtils.isEmpty(publicKeyBase64FromFirebase)) {
                        Log.d(TAG, "Public Key fetched from Firebase.");
                        // Decode Base64 and convert to PublicKey object
                        // Use CryptoUtils method for decoding crypto data
                        byte[] publicKeyBytes = CryptoUtils.base64ToBytes(publicKeyBase64FromFirebase);
                        if (publicKeyBytes != null && publicKeyBytes.length > 0) {
                            fetchedPublicKey = CryptoUtils.bytesToPublicKey(publicKeyBytes);
                            Log.d(TAG, "Public Key object created from Firebase data.");
                        } else {
                            Log.e(TAG, "Decoded Public Key bytes from Firebase are null or empty.");
                            errorMessage = "Invalid Public Key data from server.";
                        }
                    } else {
                        Log.e(TAG, "Public Key node in Firebase is empty for user: " + currentUserId);
                        errorMessage = "Public Key missing from server data.";
                    }
                } else {
                    Log.e(TAG, "Public Key node not found in Firebase for user: " + currentUserId);
                    errorMessage = "Public Key data missing from server.";
                }


                // --- CRUCIAL SECURITY STEP 3: Verify that the reconstructed Private Key matches the fetched Public Key ---
                if (reconstructedPrivateKey != null && fetchedPublicKey != null) {
                    Log.d(TAG, "Performing verification: Check if reconstructed Private Key matches fetched Public Key.");
                    try {
                        // Derive the Public Key from the reconstructed Private Key
                        // This requires getting the corresponding Public Key from the Private Key object.
                        // Note: Standard PrivateKey object does NOT directly give you its Public Key.
                        // You need to check if the PrivateKey can decrypt data encrypted by the PublicKey.
                        // A simpler verification for RSA is to check if the modulus and exponent match,
                        // but getting those values depends on the PrivateKey implementation format.
                        // A practical check: try encrypting some random data with the fetchedPublicKey
                        // and decrypting it with the reconstructedPrivateKey.

                        // Alternative (and simpler if CryptoUtils provides): Check if the modulus/exponent match
                        // Let's assume for simplicity that the PrivateKey object allows deriving/comparing the Public Key properties,
                        // OR, a common approach is to assume the exported/imported key file *also* includes the public key
                        // and verify that. But our current recovery code concept is *only* from the Private Key bytes.

                        // Let's use a practical check: encrypt small random data with fetchedPublicKey
                        // and decrypt with reconstructedPrivateKey.
                        byte[] testData = new byte[16]; // Small data
                        new SecureRandom().nextBytes(testData);

                        byte[] encryptedTestData = CryptoUtils.encryptWithRSA(testData, fetchedPublicKey); // Encrypt with Public Key from Firebase
                        byte[] decryptedTestData = CryptoUtils.decryptWithRSA(encryptedTestData, reconstructedPrivateKey); // Decrypt with reconstructed Private Key

                        // Check if decrypted data matches original test data
                        if (Arrays.equals(testData, decryptedTestData)) {
                            Log.d(TAG, "Key verification successful! Reconstructed Private Key matches Public Key from Firebase.");
                            // --- Verification SUCCESS ---
                            // The user has successfully recovered their original Private Key!
                            // Now proceed to the next step: setting a NEW passphrase for this recovered key.
                            // This process requires user interaction on the main thread.
                            Log.d(TAG, "Recovery successful. Proceeding to set new passphrase.");
                            // Signal main thread to show the "Set New Passphrase" UI
                            PrivateKey finalReconstructedPrivateKey = reconstructedPrivateKey;
                            PublicKey finalFetchedPublicKey = fetchedPublicKey;
                            mainHandler.post(() -> showSetNewPassphraseUI(finalReconstructedPrivateKey, finalFetchedPublicKey)); // Pass recovered keys
                        } else {
                            Log.e(TAG, "Key verification FAILED! Reconstructed Private Key does NOT match Public Key.");
                            errorMessage = "Invalid recovery code. Please check the code and try again.";
                        }

                    } catch (Exception e) { // Catch exceptions during verification encryption/decryption
                        Log.e(TAG, "Exception during key verification process.", e);
                        errorMessage = "Error during key verification. Invalid code or corrupt data.";
                    }

                } else {
                    // Either reconstructedPrivateKey or fetchedPublicKey is null
                    if (reconstructedPrivateKey == null) {
                        Log.e(TAG, "Reconstructed Private Key is null after decoding code.");
                        // Error message should already be set by recoveryCodeToPrivateKey if code was invalid format.
                        // If it decoded but was null, it's a CryptoUtils bug.
                        if (errorMessage == null) errorMessage = "Failed to decode recovery code.";
                    }
                    if (fetchedPublicKey == null) {
                        // Error message should already be set during Firebase fetch/decoding.
                        if (errorMessage == null) errorMessage = "Failed to fetch valid Public Key from server.";
                    }
                    // If both were null, a more general error might be appropriate
                    if (reconstructedPrivateKey == null && fetchedPublicKey == null) {
                        errorMessage = "Error fetching or decoding key data.";
                    }
                    Log.e(TAG, "Key verification skipped due to null keys.");
                }


            } catch (IllegalArgumentException e) {
                // This exception is specifically for invalid recovery code format (e.g., odd length, non-hex chars)
                Log.e(TAG, "Invalid recovery code format.", e);
                errorMessage = "Invalid recovery code format. Please check the code.";
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                // Error during key reconstruction from bytes (e.g., not PKCS8 format) or Public Key conversion
                Log.e(TAG, "Crypto error during key reconstruction/conversion.", e);
                errorMessage = "Crypto error during recovery. Invalid key format?";
            } catch (Exception e) { // Catch any other unexpected errors during the background task
                Log.e(TAG, "An unexpected error occurred during recovery attempt.", e);
                errorMessage = "An unexpected error occurred: " + e.getMessage();
            }


            // --- Post result back to Main Thread if verification failed or other error occurred ---
            if (errorMessage != null) {
                final String finalErrorMessage = errorMessage;
                mainHandler.post(() -> {
                    // Update UI to show the error and re-enable input
                    updateStatusUI(finalErrorMessage, false, true);
                    Toast.makeText(RecoverAccountWithCodeActivity.this, finalErrorMessage, Toast.LENGTH_LONG).show();
                    // Optionally clear the input field or highlight error depending on the error type
                    if (finalErrorMessage.contains("code") || finalErrorMessage.contains("format")) { // Heuristic check for code-related errors
                        editTextRecoveryCode.setError("Incorrect Code?");
                        editTextRecoveryCode.requestFocus();
                        editTextRecoveryCode.setText(""); // Clear the code on likely incorrect input
                    }
                });
            }
            // Note: If verification succeeds, the main thread post is handled by showSetNewPassphraseUI call.
        });
    }





    private void showSetNewPassphraseUI(PrivateKey recoveredPrivateKey, PublicKey originalPublicKey) {
        // This method runs on the Main Thread
        // It will hide the current recovery code UI and show a new UI section or dialog
        // to prompt the user for a new passphrase.
        // Once the user enters and confirms the new passphrase, another async task will
        // save the original recoveredPrivateKey encrypted with the NEW passphrase and NEW salt.

        Log.d(TAG, "Showing UI to set a new passphrase.");
        updateStatusUI("Code verified. Set a new security passphrase.", false, true); // Update status

        // --- HIDE current UI elements ---
        if (editTextRecoveryCode != null) editTextRecoveryCode.setVisibility(View.GONE);
        if (buttonSubmitRecoveryCode != null) buttonSubmitRecoveryCode.setVisibility(View.GONE);
        if (recoveryWarningTextView != null) recoveryWarningTextView.setVisibility(View.GONE);
        if (textViewFallbackToReset != null) textViewFallbackToReset.setVisibility(View.GONE);
        // recoveryStatusTextView and recoveryProgressBar are managed by updateStatusUI


        // --- SHOW NEW UI elements for setting passphrase ---
        // You will need to add these views to your activity_recover_account_with_code.xml layout
        // and initialize them in initializeViews().
        // Example:
        // LinearLayout setNewPassphraseLayout = findViewById(R.id.setNewPassphraseLayout); // Needs to be in layout and initialized
        // if (setNewPassphraseLayout != null) setNewPassphraseLayout.setVisibility(View.VISIBLE);

        // Let's plan to implement this using a **Dialog** for simplicity in this activity.
        // If you prefer a new screen or layout section, you can adapt this.
        showSetNewPassphraseDialog(recoveredPrivateKey, originalPublicKey);

    }


    // --- Show Dialog for Setting New Passphrase (Step 9 & 10 Continued) ---
    private void showSetNewPassphraseDialog(PrivateKey recoveredPrivateKey, PublicKey originalPublicKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        // You need to create a new layout file for this dialog, e.g., dialog_set_new_passphrase.xml
        View dialogLayout = inflater.inflate(R.layout.dialog_set_new_passphrase, null); // Needs to be created

        // Find input fields in the dialog layout
        final TextInputEditText newPassphraseInput = dialogLayout.findViewById(R.id.editTextNewPassphraseDialog); // Needs to be in dialog_set_new_passphrase.xml
        final TextInputEditText confirmPassphraseInput = dialogLayout.findViewById(R.id.editTextConfirmNewPassphraseDialog); // Needs to be in dialog_set_new_passphrase.xml

        builder.setView(dialogLayout);
        builder.setTitle("Set New Security Passphrase");
        builder.setCancelable(false); // User must set a new passphrase or cancel the whole process

        builder.setPositiveButton("Set Passphrase", (dialog, which) -> {
            // This button click listener is initially set up here,
            // but we need to validate inputs before dismissing the dialog.
            // We will override the button behavior later to handle validation.
        });

        builder.setNegativeButton("Cancel Recovery", (dialog, which) -> {
            Log.d(TAG, "User cancelled setting new passphrase. Aborting recovery.");
            dialog.dismiss();
            Toast.makeText(this, "Passphrase recovery cancelled.", Toast.LENGTH_SHORT).show();
            // Clear any partially recovered state (KeyManager is still fine with original public key if set)
            // No need to clear local keys yet unless they were already overwritten which they shouldn't be.
            // Simply finish the activity to go back to where they were (likely Login).
            finish();
        });

        AlertDialog dialog = builder.create();

        // *** Override Positive Button to perform validation ***
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String newPassphrase = newPassphraseInput.getText().toString().trim();
                String confirmPassphrase = confirmPassphraseInput.getText().toString().trim();

                if (TextUtils.isEmpty(newPassphrase) || TextUtils.isEmpty(confirmPassphrase)) {
                    Toast.makeText(this, "Please enter and confirm new passphrase.", Toast.LENGTH_SHORT).show();
                    if (TextUtils.isEmpty(newPassphrase)) newPassphraseInput.setError("Required");
                    if (TextUtils.isEmpty(confirmPassphrase)) confirmPassphraseInput.setError("Required");
                    return; // Don't dismiss dialog
                }
                if (!newPassphrase.equals(confirmPassphrase)) {
                    Toast.makeText(this, "Passphrases do not match.", Toast.LENGTH_SHORT).show();
                    newPassphraseInput.setError("Mismatch");
                    confirmPassphraseInput.setError("Mismatch");
                    return; // Don't dismiss dialog
                }
                // Add strength check similar to Setting_profile
                // private static final Pattern PASSPHRASE_STRENGTH_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");
                // (You need to copy this pattern or make it static final in CryptoUtils or a Constants class)
                // Let's copy it here for now:
                Pattern PASSPHRASE_STRENGTH_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");
                if (!PASSPHRASE_STRENGTH_PATTERN.matcher(newPassphrase).matches()) {
                    Toast.makeText(this, "Passphrase must be 6+ chars with uppercase, lowercase, digit, special.", Toast.LENGTH_LONG).show();
                    newPassphraseInput.setError("Weak Passphrase");
                    return; // Don't dismiss dialog
                }


                // Validation successful! Dismiss dialog and start the final save process.
                dialog.dismiss();
                // Start the final save process on a background thread
                saveNewEncryptedKeysAsync(recoveredPrivateKey, originalPublicKey, newPassphrase);
            });
        });

        dialog.show();
    }


    // --- Background Task to Save NEW Encrypted Keys (Step 10 Implementation) ---
    private void saveNewEncryptedKeysAsync(PrivateKey originalDecryptedPrivateKey, PublicKey originalPublicKey, String newPassphrase) {
        updateStatusUI("Saving new security setup...", true, false); // Update UI

        if (TextUtils.isEmpty(currentUserId) || originalDecryptedPrivateKey == null || originalPublicKey == null || TextUtils.isEmpty(newPassphrase)) {
            Log.e(TAG, "saveNewEncryptedKeysAsync: Missing data (userId, keys, passphrase)! Aborting.");
            updateStatusUI("Error: Missing setup data.", false, true);
            Toast.makeText(this, "Error: Missing setup data. Please log in again.", Toast.LENGTH_LONG).show();
            sendUserToLoginActivity(); // Redirect on critical error
            return;
        }

        // Need DatabaseReference usersRef here if not a member variable
        // If it's not a member, initialize it here:
        // DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users"); // Or ensure it's a member initialized in onCreate


        executorService.execute(() -> {
            Exception saveException = null;
            boolean localSaveSuccess = false;

            try {
                // 1. Generate NEW Salt
                Log.d(TAG, "Generating NEW salt for new passphrase.");
                byte[] newSalt = CryptoUtils.generateSalt();

                // 2. Derive NEW Symmetric Key from NEW Passphrase and NEW Salt
                Log.d(TAG, "Deriving NEW symmetric key from new passphrase and new salt.");
                SecretKey newEncryptionKey = CryptoUtils.deriveKeyFromPassphrase(
                        newPassphrase, newSalt, CryptoUtils.PBKDF2_ITERATIONS);

                // 3. Encrypt the ORIGINAL Decrypted Private Key with the NEW Symmetric Key and NEW Salt
                Log.d(TAG, "Encrypting the ORIGINAL Private Key with the NEW symmetric key.");
                byte[] newlyEncryptedPrivateKeyWithIV = CryptoUtils.encryptPrivateKey(originalDecryptedPrivateKey, newEncryptionKey);

                // 4. Encode data for Firebase
                Log.d(TAG, "Encoding new key data for Firebase.");
                // Original Public Key Bytes (get from the Public Key object)
                byte[] originalPublicKeyBytes = CryptoUtils.publicKeyToBytes(originalPublicKey);
                String originalPublicKeyBase64 = CryptoUtils.bytesToBase64(originalPublicKeyBytes);
                String newlyEncryptedPrivateKeyBase64 = CryptoUtils.bytesToBase64(newlyEncryptedPrivateKeyWithIV);
                String newSaltBase64 = CryptoUtils.bytesToBase64(newSalt);


                // 5. Update Firebase (Users/{userId}) with ORIGINAL Public Key, NEW Encrypted Private Key, NEW Salt
                Log.d(TAG, "Updating Firebase Users node with NEW encrypted key data for user: " + currentUserId);
                Map<String, Object> newKeyUpdates = new HashMap<>();
                // Use originalPublicKeyBase64 to ensure the Public Key node is updated/confirmed
                newKeyUpdates.put("publicKey", originalPublicKeyBase64); // Original Public Key (should already be there, but confirm)
                newKeyUpdates.put("encryptedPrivateKey", newlyEncryptedPrivateKeyBase64); // NEW Encrypted Private Key
                newKeyUpdates.put("encryptionSalt", newSaltBase64); // NEW Salt

                // Use Tasks.await for synchronous Firebase update on background thread
                Tasks.await(usersRef.child(currentUserId).updateChildren(newKeyUpdates));
                Log.d(TAG, "Firebase Users node updated successfully with NEW key data.");


                // 6. Save NEW Keys in Local Secure Storage
                Log.d(TAG, "Saving NEW symmetric key and encrypted key pair to local secure storage.");
                // Use application context for SecureKeyStorageUtil
                boolean saveSymmetricSuccess = SecureKeyStorageUtil.saveSymmetricKey(getApplicationContext(), newEncryptionKey);
                // Save the newly generated Encrypted Private Key bytes and the Original Public Key bytes
                boolean saveKeyPairSuccess = SecureKeyStorageUtil.saveEncryptedKeyPair(getApplicationContext(), currentUserId, newlyEncryptedPrivateKeyWithIV, originalPublicKeyBytes);

                localSaveSuccess = saveSymmetricSuccess && saveKeyPairSuccess;

                if (localSaveSuccess) {
                    Log.d(TAG, "NEW keys saved securely to local device storage successfully.");
                } else {
                    Log.e(TAG, "FAILED to save one or more NEW keys locally for user: " + currentUserId + ". Clearing potentially partial new local data.");
                    // Clear any partially saved new local keys on failure
                    SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId);
                    // This is an error, but the main thread will proceed, just without RememberMe.
                }

                // 7. Update KeyManager with the ORIGINAL Decrypted Private Key
                // This is done on the Main Thread AFTER the save process.
                Log.d(TAG, "Original Decrypted Private Key will be loaded into KeyManager on Main Thread.");


            } catch (Exception e) { // Catch any exceptions during the save process
                Log.e(TAG, "An error occurred during saving new encrypted keys.", e);
                saveException = e;
                // Attempt to clean up local state on failure if it wasn't already done
                Log.d(TAG, "Attempting local key cleanup on save failure...");
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), currentUserId);
                // KeyManager will be updated on Main Thread (likely cleared if save failed)
            }


            // --- Post final result back to Main Thread ---
            final String finalSaveErrorMessage = saveException != null ? saveException.getMessage() : null;
            final boolean finalLocalSaveSuccess = localSaveSuccess;
            // Pass the original decrypted private key and public key to Main Thread for KeyManager update on success
            final PrivateKey finalRecoveredPrivateKey = (saveException == null) ? originalDecryptedPrivateKey : null;
            final PublicKey finalOriginalPublicKey = (saveException == null) ? originalPublicKey : null;

            final String finalUserIdForMain = currentUserId;

            mainHandler.post(() -> { // This runs on the Main Thread
                updateStatusUI("Save process finished.", false, true); // Final status update

                if (finalSaveErrorMessage == null) {
                    // Save process successful
                    Log.d(TAG, "Saving new encrypted keys finished successfully.");
                    Toast.makeText(RecoverAccountWithCodeActivity.this, "New passphrase set successfully!", Toast.LENGTH_LONG).show();

                    // --- Update KeyManager with the ORIGINAL Decrypted Private Key ---
                    // Now that the new keys are saved to Firebase/Local and are ready for future logins,
                    // load the recovered Decrypted Private Key into KeyManager for the current session.
                    if (finalRecoveredPrivateKey != null && finalOriginalPublicKey != null) {
                        Log.d(TAG, "Updating KeyManager with the recovered Private Key.");
                        YourKeyManager.getInstance().setKeys(finalUserIdForMain, finalOriginalPublicKey, finalRecoveredPrivateKey);
                        Log.d(TAG, "KeyManager updated with recovered keys.");
                    } else {
                        Log.e(TAG, "Final save successful, but recovered keys are null! Cannot update KeyManager.");
                        // This is a critical inconsistent state. Keys were saved to server, but not loaded into memory.
                        // Force logout to make user re-login and fetch from Firebase.
                        Toast.makeText(RecoverAccountWithCodeActivity.this, "Recovery complete, but failed to load keys. Please log in again.", Toast.LENGTH_LONG).show();
                        // Ensure KeyManager is cleared if keys couldn't be loaded
                        YourKeyManager.getInstance().clearKeys();
                        // Ensure RememberMe is false if keys aren't loaded into memory
                        SharedPreferences prefs = getSharedPreferences("CircleUpPrefs", MODE_PRIVATE);
                        prefs.edit().putBoolean("RememberMe", false).apply();
                        Log.d(TAG, "RememberMe preference set FALSE due to failure loading recovered keys.");
                        sendUserToLoginActivity(); // Redirect to Login
                        return; // Stop here
                    }


                    // *** Handle RememberMe State based on local save success ***
                    // Check finalLocalSaveSuccess from the background task
                    SharedPreferences prefs = getSharedPreferences("CircleUpPrefs", MODE_PRIVATE);
                    if (finalLocalSaveSuccess) {
                        prefs.edit().putBoolean("RememberMe", true).apply();
                        Log.d(TAG, "Recovery (Main Thread): RememberMe preference set TRUE after successful save.");
                    } else {
                        prefs.edit().putBoolean("RememberMe", false).apply();
                        Log.w(TAG, "Recovery (Main Thread): NEW local key save FAILED. RememberMe preference set FALSE.");
                        Toast.makeText(RecoverAccountWithCodeActivity.this, "Warning: Could not save new keys locally for 'Remember Me'.", Toast.LENGTH_LONG).show();
                    }
                    // *** END RememberMe ***


                    // --- Final Step: Navigate to MainActivity ---
                    Log.d(TAG, "Recovery process successful. Navigating to MainActivity.");
                    Intent mainIntent = new Intent(RecoverAccountWithCodeActivity.this, MainActivity.class);
                    // Clear the back stack and start fresh with MainActivity
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                    finish(); // Finish RecoverAccountWithCodeActivity

                } else {
                    // Save process failed
                    Log.e(TAG, "Saving new encrypted keys failed. Error: " + finalSaveErrorMessage);
                    String displayMessage = finalSaveErrorMessage != null ? finalSaveErrorMessage : "Unknown error saving new security setup.";
                    Toast.makeText(RecoverAccountWithCodeActivity.this, displayMessage, Toast.LENGTH_LONG).show();
                    // Enable inputs to allow user to potentially try again (though unlikely to fix server error)
                    // Maybe show a specific message and keep inputs disabled for critical errors?
                    updateStatusUI("Save failed: " + displayMessage, false, false); // Disable inputs on critical save failure
                    // KeyManager and local storage should be cleaned up by the background task.
                }
            }); // End mainHandler.post
        }); // End executorService.execute
    }


    // --- Toolbar Back Button ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Handle Back Press (Optional confirmation) ---
    @Override
    public void onBackPressed() {
        // Option 1: Just go back
        super.onBackPressed();

        // Option 2: Show a confirmation dialog if user entered text
        // String codeInput = editTextRecoveryCode.getText().toString().trim();
        // if (!TextUtils.isEmpty(codeInput)) {
        //     new AlertDialog.Builder(this)
        //             .setTitle("Discard Code?")
        //             .setMessage("You have entered a code. Are you sure you want to leave?")
        //             .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
        //             .setNegativeButton("Cancel", null)
        //             .show();
        // } else {
        //     super.onBackPressed();
        // }
    }


    // --- Helper Navigation Method to go back to Login ---
    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity from RecoverAccountWithCodeActivity.");
        Intent loginIntent = new Intent(RecoverAccountWithCodeActivity.this, Login.class); // Ensure Login activity exists
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish(); // Finish this activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shut down the executor service
        if (executorService != null && !executorService.isShutdown()) {
            Log.d(TAG, "ExecutorService shutting down in RecoverAccountWithCodeActivity.");
            executorService.shutdownNow(); // Attempt to stop executing tasks
        }
        // No explicit progress dialog member in this activity, just a ProgressBar view
        Log.d(TAG, "🔴 RecoverAccountWithCodeActivity onDestroy called.");
    }
}