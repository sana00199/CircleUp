package com.sana.circleup; // Replace with your actual package name

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn; // Import GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // Import GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks; // Import Tasks for await
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential; // Import AuthCredential
import com.google.firebase.auth.EmailAuthProvider; // Import EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot; // Import DataSnapshot
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue; // Import ServerValue for timestamp
import com.sana.circleup.encryptionfiles.CryptoUtils;
import com.sana.circleup.encryptionfiles.SecureKeyStorageUtil;
import com.sana.circleup.encryptionfiles.YourKeyManager;
import com.sana.circleup.room_db_implement.ChatDao;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.ChatEntity;
import com.sana.circleup.room_db_implement.ConversationKeyDao;
import com.sana.circleup.room_db_implement.MessageDao;

import java.security.KeyPair; // Import KeyPair
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.List; // Import List
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException; // Import ExecutionException
import java.lang.InterruptedException; // Import InterruptedException
import java.util.ArrayList; // Import ArrayList for list manipulation

import javax.crypto.SecretKey;

// Needed for Base64 encoding/decoding cryptographic keys for Firebase storage
import java.util.Base64;
import java.util.regex.Pattern;


public class ResetPassphraseActivity extends AppCompatActivity {

    private static final String TAG = "ResetPassphraseAct";

    // UI Elements
    private TextView resetWarningTitle, resetWarningMessage, resetWarningConfirm;
    private TextView identityVerificationTitle, identityVerificationMessage;
    private TextInputEditText editTextVerifyEmail, editTextVerifyPassword;
    private TextView newPassphraseTitle;
    private TextInputEditText editTextNewPassphrase, editTextConfirmNewPassphrase;
    private Button buttonResetPassphrase;
    private ProgressBar progressBar;
    private TextView statusTextView; // Added a TextView to show processing status

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private DatabaseReference rootRef; // Added RootRef for ConversationKeys/ChatSummaries
    private FirebaseUser currentUser;

    // Async Task Execution
    private ExecutorService executorService;
    private Handler mainHandler;

    // Room DB & DAOs (Need these to get list of conversation IDs and delete local data)
    private ChatDatabase chatDatabase; // Room DB
    private ChatDao chatDao; // Room DAO for Chat list items
    private MessageDao messageDao; // Room DAO for individual messages
    private ConversationKeyDao conversationKeyDao; // Room DAO for conversation keys
    private ExecutorService databaseExecutor; // Shared DB executor


    // Password strength pattern (Same as Signup/ChangeAuthPassword)
    private static final Pattern PASSPHRASE_STRENGTH_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_passphrase);
        Log.d(TAG, "🟢 ResetPassphraseActivity onCreate.");

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_reset_passphrase); // Match your layout ID
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Reset Passphrase"); // Set title
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
            }
        } else {
            Log.w(TAG, "Toolbar not found in layout with ID toolbar_reset_passphrase");
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference(); // Initialize RootRef
        userRef = rootRef.child("Users"); // userRef is child of RootRef
        currentUser = auth.getCurrentUser();

        // Check authentication state
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated in ResetPassphraseActivity! Redirecting to Login.");
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity();
            return;
        }

        // Initialize Room DB and DAOs
        chatDatabase = ChatDatabase.getInstance(getApplicationContext()); // Use application context
        chatDao = chatDatabase.chatDao();
        messageDao = chatDatabase.messageDao();
        conversationKeyDao = chatDatabase.conversationKeyDao();
        databaseExecutor = ChatDatabase.databaseWriteExecutor; // Use shared DB executor


        // Check if user is Email/Password user for re-authentication
        boolean isEmailPasswordUser = false;
        if (currentUser.getProviderData() != null) {
            for (com.google.firebase.auth.UserInfo profile : currentUser.getProviderData()) {
                if (profile != null && profile.getProviderId() != null && profile.getProviderId().equals(EmailAuthProvider.PROVIDER_ID)) {
                    isEmailPasswordUser = true;
                    break;
                }
            }
        }

        initializeViews(); // Initialize UI

        // Setup Executor and Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Find statusTextView in layout if it exists, otherwise logging will be used
//        statusTextView = findViewById(R.id.unlock_status_text); // Assuming you might reuse or add a TextView for status

        // --- Handle Identity Verification UI ---
        if (isEmailPasswordUser) {
            identityVerificationTitle.setVisibility(View.VISIBLE);
            identityVerificationMessage.setVisibility(View.VISIBLE);
            findViewById(R.id.layout_verify_email).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_verify_password).setVisibility(View.VISIBLE);

            if (currentUser.getEmail() != null) {
                editTextVerifyEmail.setText(currentUser.getEmail());
            }
            editTextVerifyEmail.setEnabled(false);
            Log.d(TAG, "User is Email/Password user. Identity verification required.");

        } else {
            Log.w(TAG, "User is NOT Email/Password user. Skipping identity verification with password.");
            identityVerificationTitle.setVisibility(View.GONE);
            identityVerificationMessage.setVisibility(View.GONE);
            findViewById(R.id.layout_verify_email).setVisibility(View.GONE);
            findViewById(R.id.layout_verify_password).setVisibility(View.GONE);
            editTextVerifyEmail.setEnabled(false);
            editTextVerifyPassword.setEnabled(false);
        }
        // --- End Identity Verification UI ---


        // Set click listener for the Reset button
        boolean finalIsEmailPasswordUser = isEmailPasswordUser;
        buttonResetPassphrase.setOnClickListener(v -> {
            // First, perform validation
            if (finalIsEmailPasswordUser) {
                if (!validateIdentityVerificationInputs()) {
                    return;
                }
            }
            if (!validateNewPassphraseInputs()) {
                return;
            }

            // If validations pass, show final confirmation dialog
            showConfirmationDialog(finalIsEmailPasswordUser);
        });


        Log.d(TAG, "✅ onCreate finished in ResetPassphraseActivity.");
    }

    // --- Toolbar Back Button ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Handle Back Press (Optional confirmation if fields are filled) ---
    @Override
    public void onBackPressed() {
        boolean inputsFilled = !TextUtils.isEmpty(editTextVerifyPassword.getText()) ||
                !TextUtils.isEmpty(editTextNewPassphrase.getText()) ||
                !TextUtils.isEmpty(editTextConfirmNewPassphrase.getText());

        if (inputsFilled) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have entered data. Are you sure you want to leave and lose these changes?")
                    .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    // Initialize UI elements
    private void initializeViews() {
        resetWarningTitle = findViewById(R.id.reset_warning_title);
        resetWarningMessage = findViewById(R.id.reset_warning_message);

        identityVerificationTitle = findViewById(R.id.identity_verification_title);
        identityVerificationMessage = findViewById(R.id.identity_verification_message);
        editTextVerifyEmail = findViewById(R.id.editTextVerifyEmail);
        editTextVerifyPassword = findViewById(R.id.editTextVerifyPassword);
        newPassphraseTitle = findViewById(R.id.new_passphrase_title);
        editTextNewPassphrase = findViewById(R.id.editTextNewPassphrase);
        editTextConfirmNewPassphrase = findViewById(R.id.editTextConfirmNewPassphrase);
        buttonResetPassphrase = findViewById(R.id.buttonResetPassphrase);
        progressBar = findViewById(R.id.progressBarResetPassphrase);
        // statusTextView reference is attempted in onCreate
    }

    // Helper to set UI enabled/disabled state
    private void setUIEnabled(boolean enabled) {
        if (identityVerificationTitle.getVisibility() == View.VISIBLE) {
            editTextVerifyPassword.setEnabled(enabled);
        }
        editTextNewPassphrase.setEnabled(enabled);
        editTextConfirmNewPassphrase.setEnabled(enabled);
        buttonResetPassphrase.setEnabled(enabled);
        // email field remains disabled
    }

    // Helper to update status text if the TextView exists
    private void updateStatusText(String message) {
        if (statusTextView != null) {
            mainHandler.post(() -> statusTextView.setText(message));
        }
        Log.d(TAG, "Status: " + message); // Always log status
    }


    // Validate Identity Verification Inputs (for Email/Password users)
    private boolean validateIdentityVerificationInputs() {
        String password = editTextVerifyPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            editTextVerifyPassword.setError("Account password is required for verification.");
            editTextVerifyPassword.requestFocus();
            return false;
        }
        return true;
    }

    // Validate New Passphrase Inputs
    private boolean validateNewPassphraseInputs() {
        String newPassphrase = editTextNewPassphrase.getText().toString().trim();
        String confirmNewPassphrase = editTextConfirmNewPassphrase.getText().toString().trim();

        if (TextUtils.isEmpty(newPassphrase)) {
            editTextNewPassphrase.setError("New passphrase is required.");
            editTextNewPassphrase.requestFocus();
            return false;
        }
        if (!PASSPHRASE_STRENGTH_PATTERN.matcher(newPassphrase).matches()) {
            editTextNewPassphrase.setError("Passphrase must be 6+ characters, include uppercase, lowercase, digit, and special character (@#$%^&+=!)");
            editTextNewPassphrase.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(confirmNewPassphrase)) {
            editTextConfirmNewPassphrase.setError("Confirm new passphrase is required.");
            editTextConfirmNewPassphrase.requestFocus();
            return false;
        }
        if (!newPassphrase.equals(confirmNewPassphrase)) {
            editTextConfirmNewPassphrase.setError("Passphrases do not match.");
            editTextConfirmNewPassphrase.requestFocus();
            return false;
        }
        return true;
    }


    // Show final confirmation dialog before performing the reset
    private void showConfirmationDialog(boolean isEmailPasswordUser) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Passphrase Reset")
                .setMessage("Are you absolutely sure you want to reset your security passphrase? You will permanently lose access to ALL your previous encrypted messages. This action cannot be undone.") // More explicit warning
                .setPositiveButton("Yes, Reset", (dialog, which) -> {
                    startResetProcess(isEmailPasswordUser);
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert) // Warning icon
                .show();
    }


    // Start the reset process (handle identity verification if needed, then trigger async key reset)
    private void startResetProcess(boolean isEmailPasswordUser) {
        Log.d(TAG, "Starting reset process...");

        setUIEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        updateStatusText("Resetting secure account..."); // Update status text


        final String newPassphrase = editTextNewPassphrase.getText().toString().trim();

        if (isEmailPasswordUser) {
            String email = editTextVerifyEmail.getText().toString().trim();
            String password = editTextVerifyPassword.getText().toString().trim();
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

            updateStatusText("Verifying identity...");
            Log.d(TAG, "Re-authenticating user for identity verification...");
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(this, reauthTask -> {
                        if (reauthTask.isSuccessful()) {
                            Log.d(TAG, "Re-authentication successful. Proceeding with key reset.");
                            updateStatusText("Identity verified. Proceeding with reset...");
                            performKeyResetAsync(newPassphrase);
                        } else {
                            Log.e(TAG, "Re-authentication failed.", reauthTask.getException());
                            String errorMessage = "Identity verification failed.";
                            if (reauthTask.getException() != null) {
                                errorMessage = "Identity verification failed: " + reauthTask.getException().getMessage();
                                if (reauthTask.getException() instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                                    errorMessage = "Incorrect account password.";
                                }
                            }
                            final String finalErrorMessage = errorMessage;
                            mainHandler.post(() -> {
                                progressBar.setVisibility(View.GONE);
                                setUIEnabled(true);
                                updateStatusText("Verification failed.");
                                Toast.makeText(ResetPassphraseActivity.this, finalErrorMessage, Toast.LENGTH_LONG).show();
                                editTextVerifyPassword.setError("Incorrect password");
                                editTextVerifyPassword.requestFocus();
                                editTextVerifyPassword.setText("");
                            });
                        }
                    });

        } else {
            Log.d(TAG, "User is not Email/Password. Skipping re-authentication.");
            updateStatusText("Skipping identity verification...");
            performKeyResetAsync(newPassphrase);
        }
    }


    // Perform the core asynchronous key reset logic
//    private void performKeyResetAsync(String newPassphrase) {
//        if (currentUser == null) {
//            Log.e(TAG, "performKeyResetAsync called with null currentUser!");
//            mainHandler.post(() -> {
//                progressBar.setVisibility(View.GONE);
//                setUIEnabled(true);
//                updateStatusText("Error: User not logged in.");
//                Toast.makeText(ResetPassphraseActivity.this, "User not logged in. Cannot reset.", Toast.LENGTH_SHORT).show();
//            });
//            return;
//        }
//        final String userId = currentUser.getUid();
//
//        executorService.execute(() -> {
//            Exception resetException = null;
//            String successMessage = null;
//            boolean localSaveSuccess = false;
//
//            Log.d(TAG, "Starting asynchronous key reset process for user: " + userId);
//            updateStatusText("Starting key reset...");
//
//            PublicKey newPublicKey = null;
//            PrivateKey newPrivateKey = null;
//            try {
//                // --- Step 1: Get list of Conversation IDs from Room *before* deleting ---
//                // We need this list to clean up Firebase ConversationKeys later.
//                Log.d(TAG, "Fetching conversation IDs from local Room DB for cleanup.");
//                List<ChatEntity> localChats = chatDao.getAllChatsImmediate(userId); // Assuming getAllChatsImmediate exists and is synchronous
//                List<String> conversationIdsToDelete = new ArrayList<>();
//                if (localChats != null) {
//                    for (ChatEntity chat : localChats) {
//                        if (!TextUtils.isEmpty(chat.getConversationId())) {
//                            conversationIdsToDelete.add(chat.getConversationId());
//                        }
//                    }
//                }
//                Log.d(TAG, "Found " + conversationIdsToDelete.size() + " conversation IDs in Room for deletion cleanup.");
//                updateStatusText("Cleaning up old data...");
//
//
////                // --- Step 2: Delete User's Chat Summaries from Firebase ---
////                Log.d(TAG, "Deleting user's chat summaries from Firebase: ChatSummaries/" + userId);
////                Tasks.await(rootRef.child("ChatSummaries").child(userId).removeValue());
////                Log.d(TAG, "User's chat summaries deleted from Firebase.");
//
//
////                // --- Step 3: Delete User's Specific Conversation Keys from Firebase ---
////                Log.d(TAG, "Deleting user's conversation keys from Firebase...");
////                // Perform deletions asynchronously without waiting for each one if order doesn't matter much for cleanup
////                // Or await if strict sequence needed (less likely for cleanup).
////                // Using await for simplicity in example, but batch writes are better for many items.
////                for (String convId : conversationIdsToDelete) {
////                    try {
////                        Tasks.await(rootRef.child("ConversationKeys").child(convId).child(userId).removeValue());
////                        Log.d(TAG, "Deleted Firebase conversation key for conv: " + convId);
////                    } catch (Exception e) {
////                        Log.w(TAG, "Failed to delete Firebase conversation key for conv: " + convId, e);
////                        // Continue deletion for other keys even if one fails
////                    }
////                }
////                Log.d(TAG, "Attempted deletion of " + conversationIdsToDelete.size() + " Firebase conversation key entries.");
//
//
//                // --- Step 4: Clear ALL OLD Local Keys from Secure Storage and KeyManager ---
//                // This removes the symmetric key and the old encrypted private key file.
//                Log.d(TAG, "Clearing all old secure local storage keys.");
//                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Use application context
//                // KeyManager will be cleared on the main thread post later by setKeys.
//                Log.d(TAG, "Old local storage keys cleared.");
//
//                // --- Step 5: Delete User's Chat Data from Room DB ---
//                // Delete chat list entries, messages, and conversation keys owned by this user from Room.
//                Log.d(TAG, "Deleting user's chat data from local Room DB...");
//                databaseExecutor.execute(() -> { // Submit to DB executor
//                    try {
//                        int deletedChats = chatDao.deleteAllChatsForOwner(userId);
//                        Log.d(TAG, "Room DB: Deleted " + deletedChats + " ChatEntity entries.");
//                        int deletedMessages = messageDao.deleteAllMessagesForChatOwner(userId); // Assuming this method exists
//                        Log.d(TAG, "Room DB: Deleted " + deletedMessages + " MessageEntity entries for owner.");
//                        int deletedKeys = conversationKeyDao.deleteAllKeysForOwner(userId);
//                        Log.d(TAG, "Room DB: Deleted " + deletedKeys + " ConversationKeyEntity entries.");
//                        Log.d(TAG, "Room DB: Local chat data cleanup complete.");
//                    } catch (Exception e) {
//                        Log.e(TAG, "Room DB: Error performing local chat data cleanup during reset", e);
//                        // Log error, but main reset process can continue.
//                    }
//                    // --- Optional: Signal UI refresh for ChatFragment ---
//                    // Even though we navigate to MainActivity which will restart ChatFragment,
//                    // if ChatFragment somehow persists or is in backstack, a refresh might be needed.
//                    // This is complex across activities/fragments. Relying on MainActivity.onStart
//                    // re-initializing fragments and their observers (which react to the now-empty Room DB) is standard.
//                    // If a manual refresh signal is needed, it would involve LocalBroadcastManager or similar.
//                    // For now, rely on standard lifecycle + LiveData observers.
//                });
//                Log.d(TAG, "Room DB cleanup tasks submitted to executor.");
//
//
//                // --- Step 6: Generate NEW Keys and Encrypt Private Key with NEW Passphrase ---
//                updateStatusText("Generating new keys...");
//                Log.d(TAG, "Generating NEW salt.");
//                byte[] newSalt = CryptoUtils.generateSalt();
//
//                Log.d(TAG, "Deriving NEW symmetric key from NEW passphrase and NEW salt.");
//                SecretKey newEncryptionKey = CryptoUtils.deriveKeyFromPassphrase(
//                        newPassphrase, newSalt, CryptoUtils.PBKDF2_ITERATIONS);
//
//                Log.d(TAG, "Generating NEW RSA key pair.");
//                KeyPair newKeyPair = CryptoUtils.generateRSAKeyPair();
//                newPublicKey = newKeyPair.getPublic();
//                newPrivateKey = newKeyPair.getPrivate();
//
//                Log.d(TAG, "Encrypting NEW private key with NEW symmetric key.");
//                byte[] newlyEncryptedPrivateKeyWithIV = CryptoUtils.encryptPrivateKey(newPrivateKey, newEncryptionKey);
//
//
//                // --- Step 7: Save NEW Key Data to Firebase ---
//                updateStatusText("Saving new keys to server...");
//                Log.d(TAG, "Preparing NEW key data for Firebase update.");
//                String newPublicKeyBase64 = Base64.getEncoder().encodeToString(CryptoUtils.publicKeyToBytes(newPublicKey));
//                String newlyEncryptedPrivateKeyBase64 = Base64.getEncoder().encodeToString(newlyEncryptedPrivateKeyWithIV);
//                String newSaltBase64 = Base64.getEncoder().encodeToString(newSalt);
//
//
//                Map<String, Object> newKeyUpdates = new HashMap<>();
//                newKeyUpdates.put("publicKey", newPublicKeyBase64);
//                newKeyUpdates.put("encryptedPrivateKey", newlyEncryptedPrivateKeyBase64);
//                newKeyUpdates.put("encryptionSalt", newSaltBase64);
//
//                Log.d(TAG, "Updating Firebase Users node with NEW key data.");
//                Tasks.await(userRef.child(userId).updateChildren(newKeyUpdates));
//                Log.d(TAG, "Firebase Users node updated successfully with NEW key data.");
//
//
//                // --- Step 8: Save NEW Keys in Local Secure Storage ---
//                updateStatusText("Saving new keys locally...");
//                Log.d(TAG, "Saving NEW symmetric key and encrypted key pair to local secure storage.");
//                boolean saveSymmetricSuccess = SecureKeyStorageUtil.saveSymmetricKey(getApplicationContext(), newEncryptionKey);
//                boolean saveKeyPairSuccess = SecureKeyStorageUtil.saveEncryptedKeyPair(getApplicationContext(), userId, newlyEncryptedPrivateKeyWithIV, CryptoUtils.publicKeyToBytes(newPublicKey));
//
//                localSaveSuccess = saveSymmetricSuccess && saveKeyPairSuccess;
//
//                if (localSaveSuccess) {
//                    Log.d(TAG, "NEW keys saved securely to local device storage successfully.");
//                } else {
//                    Log.e(TAG, "FAILED to save one or more NEW keys locally for user: " + userId + ". Clearing potentially partial new local data.");
//                    SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
//                }
//
//
//                // --- Step 9: Update KeyManager with NEW Decrypted Keys (on Main Thread) ---
//                // This will be posted to the main thread.
//                Log.d(TAG, "NEW KeyManager update will be posted to Main Thread.");
//
//
//                successMessage = "Security Passphrase reset successfully!";
//
//
//            } catch (ExecutionException | InterruptedException e) {
//                Log.e(TAG, "Firebase operation failed during key reset", e);
//                resetException = new Exception("Failed to update keys on server. Network error?");
//            } catch (IllegalArgumentException e) {
//                Log.e(TAG, "Error decoding Base64 or processing key data during reset", e);
//                resetException = new Exception("Invalid key data format.");
//                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
//                YourKeyManager.getInstance().clearKeys(); // Ensure in-memory is clean
//            } catch (Exception e) {
//                Log.e(TAG, "An unexpected error occurred during key reset", e);
//                resetException = new Exception("An unexpected error occurred during reset.");
//                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
//                YourKeyManager.getInstance().clearKeys(); // Ensure in-memory is clean
//            }
//
//
//            // --- Post result back to Main Thread ---
//            final String finalSuccessMessage = successMessage;
//            final String finalErrorMessage = resetException != null ? resetException.getMessage() : null;
//            final boolean finalLocalSaveSuccess = localSaveSuccess;
//
//            final PublicKey finalNewPublicKey = (resetException == null) ? newPublicKey : null;
//            final PrivateKey finalNewPrivateKey = (resetException == null) ? newPrivateKey : null;
//
//            final String finalUserId = userId;
//
//
//            mainHandler.post(() -> {
//                progressBar.setVisibility(View.GONE);
//                setUIEnabled(true); // Re-enable UI
//                updateStatusText("Reset process finished."); // Final status update
//
//                if (finalSuccessMessage != null) {
//                    Log.d(TAG, "Reset process successful. Showing toast and navigating.");
//                    Toast.makeText(ResetPassphraseActivity.this, finalSuccessMessage, Toast.LENGTH_LONG).show();
//
//                    // --- Update KeyManager with NEW Keys ---
//                    if (finalNewPublicKey != null && finalNewPrivateKey != null) { // Use final userId
//                        Log.d(TAG, "Updating KeyManager with newly generated keys.");
//                        YourKeyManager.getInstance().setKeys(finalUserId, finalNewPublicKey, finalNewPrivateKey);
//                        Log.d(TAG, "KeyManager updated with new keys after reset.");
//
//                        // *** Handle RememberMe State Saving After Successful Reset ***
//                        SharedPreferences prefs = getSharedPreferences("CircleUpPrefs", MODE_PRIVATE);
//                        if (finalLocalSaveSuccess) {
//                            prefs.edit().putBoolean("RememberMe", true).apply();
//                            Log.d(TAG, "Reset (Main Thread): RememberMe preference set to TRUE after successful reset and NEW local save.");
//                        } else {
//                            prefs.edit().putBoolean("RememberMe", false).apply();
//                            Log.w(TAG, "Reset (Main Thread): NEW local key save failed despite successful reset. RememberMe preference set FALSE.");
//                            Toast.makeText(ResetPassphraseActivity.this, "Warning: Could not save new keys locally.", Toast.LENGTH_SHORT).show();
//                        }
//                        // *** END Handle RememberMe ***
//
//                        // After successful reset and KeyManager update, navigate to MainActivity
//                        Log.d(TAG, "Reset (Main Thread): Reset successful. Navigating to MainActivity.");
//                        Intent mainIntent = new Intent(ResetPassphraseActivity.this, MainActivity.class);
//                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(mainIntent);
//                        finish(); // Finish this activity
//
//                    } else {
//                        Log.e(TAG, "Reset (Main Thread): Success message received, but new keys are null. Inconsistent state. Force logout.");
//                        Toast.makeText(ResetPassphraseActivity.this, "Reset completed, but failed to load new keys. Log in again.", Toast.LENGTH_LONG).show();
//                        forceLogout(); // Critical error
//                    }
//
//                } else {
//                    // Reset process failed
//                    Log.e(TAG, "Reset process failed. Showing error: " + finalErrorMessage);
//                    String displayMessage = finalErrorMessage != null ? finalErrorMessage : "Unknown error resetting passphrase.";
//                    Toast.makeText(ResetPassphraseActivity.this, displayMessage, Toast.LENGTH_LONG).show();
//
//                    // Keep UI enabled for user to potentially try again.
//                    if (identityVerificationTitle.getVisibility() == View.VISIBLE) {
//                        editTextVerifyPassword.setText("");
//                    }
//                }
//            }); // End mainHandler.post
//        }); // End executorService.execute
//    }






// Inside ResetPassphraseActivity.java

// ... (other methods) ...

// Perform the core asynchronous key reset logic
// Inside ResetPassphraseActivity.java

// ... (existing imports and members) ...

// Perform the core asynchronous key reset logic
private void performKeyResetAsync(String newPassphrase) {
    if (currentUser == null) {
        Log.e(TAG, "performKeyResetAsync called with null currentUser!");
        mainHandler.post(() -> {
            progressBar.setVisibility(View.GONE);
            setUIEnabled(true);
            updateStatusText("Error: User not logged in.");
            Toast.makeText(ResetPassphraseActivity.this, "User not logged in. Cannot reset.", Toast.LENGTH_SHORT).show();
        });
        return;
    }
    final String userId = currentUser.getUid();

    executorService.execute(() -> {
        Exception resetException = null;
        String successMessage = null;
        boolean localSaveSuccess = false;

        Log.d(TAG, "Starting asynchronous key reset process for user: " + userId);
        updateStatusText("Starting key reset...");

        PublicKey newPublicKey = null;
        PrivateKey newPrivateKey = null;

        // List to hold conversation IDs and chat partners for Firebase cleanup and notification
        Map<String, String> conversationPartnersForCleanupAndNotification = new HashMap<>();

        try {
            // --- Step 1: Get list of Conversation IDs and partners from Room *before* deleting local data ---
            Log.d(TAG, "Fetching conversation IDs and partners from local Room DB for cleanup and notification.");
            // Ensure getAllChatsImmediate returns a list of ChatEntity
            List<ChatEntity> localChats = chatDao.getAllChatsImmediate(userId); // Assuming getAllChatsImmediate exists and is synchronous
            if (localChats != null) {
                for (ChatEntity chat : localChats) {
                    if (!TextUtils.isEmpty(chat.getConversationId()) && !TextUtils.isEmpty(chat.getUserId())) {
                        // Store Conversation ID and the other user's ID (the partner)
                        conversationPartnersForCleanupAndNotification.put(chat.getConversationId(), chat.getUserId());
                    }
                }
            }
            Log.d(TAG, "Found " + conversationPartnersForCleanupAndNotification.size() + " conversations in Room for cleanup and notification.");
            updateStatusText("Collecting chat info...");


            // --- Step 2: Clear ALL OLD Local Keys from Secure Storage and KeyManager ---
            Log.d(TAG, "Clearing all old secure local storage keys.");
            SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Use application context
            // KeyManager will be cleared on the main thread post later by setKeys.
            Log.d(TAG, "Old local storage keys cleared.");
            updateStatusText("Local keys removed...");


            // --- Step 3: Delete User A's Specific Wrapped Key Entries from Firebase ConversationKeys ---
            // This removes User A's ability to decrypt keys previously shared with others if needed from Firebase.
            Log.d(TAG, "Deleting user's specific wrapped key entries from Firebase ConversationKeys...");
            // Iterate through each conversation and delete the child node named after the user's UID under each timestamp in 'key_versions'
            for (Map.Entry<String, String> entry : conversationPartnersForCleanupAndNotification.entrySet()) {
                String convId = entry.getKey();
                try {
                    // Path to key versions for this conversation: ConversationKeys/{convId}/key_versions
                    DatabaseReference convKeyVersionsRef = rootRef.child("ConversationKeys").child(convId).child("key_versions");

                    // Fetch all key versions for this conversation to find the entries for userId
                    // Using Task.await to keep it synchronous on this background thread.
                    DataSnapshot keyVersionsSnapshot = Tasks.await(convKeyVersionsRef.get());

                    if(keyVersionsSnapshot.exists() && keyVersionsSnapshot.hasChildren()){
                        Map<String, Object> updatesToDelete = new HashMap<>();
                        int entriesToDeleteCount = 0;
                        for(DataSnapshot keyVersionSnap : keyVersionsSnapshot.getChildren()){
                            // The child key is the timestamp (string)
                            String timestampKey = keyVersionSnap.getKey();
                            // Check if this key version has an entry for the current user's UID
                            if(keyVersionSnap.hasChild(userId)){
                                // Prepare update to set the user's entry to null (delete it)
                                updatesToDelete.put(timestampKey + "/" + userId, null);
                                entriesToDeleteCount++;
                            }
                        }
                        if(entriesToDeleteCount > 0){
                            Log.d(TAG, "Preparing to delete " + entriesToDeleteCount + " entries for user " + userId + " in Firebase ConversationKeys for conv " + convId);
                            // Apply the deletion updates to the key_versions node
                            Tasks.await(convKeyVersionsRef.updateChildren(updatesToDelete));
                            Log.d(TAG, "Successfully deleted entries for user " + userId + " in Firebase ConversationKeys for conv " + convId);
                        } else {
                            Log.d(TAG, "No entries found for user " + userId + " in Firebase ConversationKeys for conv " + convId + " to delete.");
                        }
                    } else {
                        Log.d(TAG, "No key versions found in Firebase for conv " + convId + " during deletion cleanup.");
                    }

                } catch (Exception e) {
                    Log.w(TAG, "Failed to delete Firebase conversation key entries for user " + userId + " in conv " + convId, e);
                    // Continue deletion for other conversations even if one fails
                }
            }
            Log.d(TAG, "Attempted deletion of Firebase conversation key entries for user " + userId + " across " + conversationPartnersForCleanupAndNotification.size() + " conversations.");
            updateStatusText("Firebase keys cleaned...");


            // --- Step 4: Delete User's Chat Data from Room DB (Messages, Keys, Chat Summaries) ---
            // User A loses ALL old messages from their view permanently.
            Log.d(TAG, "Deleting user's chat data from local Room DB...");
            // This runs on the databaseExecutor submitted *from* here, so it won't block the current executor thread.
            // This is fire-and-forget relative to the reset process, which is fine as long as it's queued.
            databaseExecutor.execute(() -> { // Submit to DB executor
                try {
                    Log.d(TAG, "Reset DB Cleanup (Executor): Starting local Room DB cleanup for user: " + userId);
                    int deletedChats = chatDao.deleteAllChatsForOwner(userId); // Delete all chat list entries owned by this user
                    Log.d(TAG, "Reset DB Cleanup (Executor): Deleted " + deletedChats + " ChatEntity entries.");
                    int deletedMessages = messageDao.deleteAllMessagesForOwner(userId); // Delete all messages owned by this user
                    Log.d(TAG, "Reset DB Cleanup (Executor): Deleted " + deletedMessages + " MessageEntity entries for owner.");
                    int deletedKeys = conversationKeyDao.deleteAllKeysForOwner(userId); // Delete ALL conversation key versions for this owner
                    Log.d(TAG, "Reset DB Cleanup (Executor): Deleted " + deletedKeys + " ConversationKeyEntity entries.");

                    // If you have other user-specific data in Room, delete it here too (e.g., contacts if not synced by listener)
                    // int deletedContacts = contactDao.deleteAllContactsForOwner(userId); // Assuming this method exists
                    // Log.d(TAG, "Reset DB Cleanup (Executor): Deleted " + deletedContacts + " Contact entries for owner.");

                    Log.d(TAG, "Reset DB Cleanup (Executor): Local chat data cleanup complete.");
                } catch (Exception e) {
                    Log.e(TAG, "Reset DB Cleanup (Executor): Error performing local Room DB cleanup during reset", e);
                    // Log error, but main reset process can continue.
                }
                Log.d(TAG, "Reset DB Cleanup (Executor): Task FINISHED.");
            });
            Log.d(TAG, "Room DB cleanup tasks submitted to executor.");
            updateStatusText("Local data cleared...");


            // --- Step 5: Generate NEW Keys and Encrypt Private Key with NEW Passphrase ---
            updateStatusText("Generating new keys...");
            Log.d(TAG, "Generating NEW salt.");
            byte[] newSalt = CryptoUtils.generateSalt();

            Log.d(TAG, "Deriving NEW symmetric key from NEW passphrase and NEW salt.");
            SecretKey newEncryptionKey = CryptoUtils.deriveKeyFromPassphrase(
                    newPassphrase, newSalt, CryptoUtils.PBKDF2_ITERATIONS);

            Log.d(TAG, "Generating NEW RSA key pair.");
            KeyPair newKeyPair = CryptoUtils.generateRSAKeyPair();
            newPublicKey = newKeyPair.getPublic();
            newPrivateKey = newKeyPair.getPrivate();

            Log.d(TAG, "Encrypting NEW private key with NEW symmetric key.");
            byte[] newlyEncryptedPrivateKeyWithIV = CryptoUtils.encryptPrivateKey(newPrivateKey, newEncryptionKey);


            // --- Step 6: Save NEW RSA Key Pair Data to Firebase User Profile ---
            updateStatusText("Saving new keys to server...");
            Log.d(TAG, "Preparing NEW key data for Firebase Users update.");
            String newPublicKeyBase64 = Base64.getEncoder().encodeToString(CryptoUtils.publicKeyToBytes(newPublicKey));
            String newlyEncryptedPrivateKeyBase64 = Base64.getEncoder().encodeToString(newlyEncryptedPrivateKeyWithIV);
            String newSaltBase64 = Base64.getEncoder().encodeToString(newSalt);


            Map<String, Object> newKeyUpdates = new HashMap<>();
            newKeyUpdates.put("publicKey", newPublicKeyBase64); // Update the public key
            newKeyUpdates.put("encryptedPrivateKey", newlyEncryptedPrivateKeyBase64); // Update the encrypted private key
            newKeyUpdates.put("encryptionSalt", newSaltBase64); // Update the salt

            Log.d(TAG, "Updating Firebase Users node with NEW key data.");
            Tasks.await(userRef.child(userId).updateChildren(newKeyUpdates)); // Update User's profile node
            Log.d(TAG, "Firebase Users node updated successfully with NEW key data.");
            updateStatusText("New keys saved...");


            // --- Step 7: Save NEW Keys in Local Secure Storage ---
            updateStatusText("Saving new keys locally...");
            Log.d(TAG, "Saving NEW symmetric key and encrypted key pair to local secure storage.");
            boolean saveSymmetricSuccess = SecureKeyStorageUtil.saveSymmetricKey(getApplicationContext(), newEncryptionKey);
            boolean saveKeyPairSuccess = SecureKeyStorageUtil.saveEncryptedKeyPair(getApplicationContext(), userId, newlyEncryptedPrivateKeyWithIV, CryptoUtils.publicKeyToBytes(newPublicKey));

            localSaveSuccess = saveSymmetricSuccess && saveKeyPairSuccess;

            if (localSaveSuccess) {
                Log.d(TAG, "NEW keys saved securely to local device storage successfully.");
            } else {
                Log.e(TAG, "FAILED to save one or more NEW keys locally for user: " + userId + ". Clearing potentially partial new local data.");
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear potentially partial new keys if save failed
            }
            updateStatusText("New keys saved locally...");


            // --- Step 8: Insert System Message in Firebase for Each Conversation ---
            // This message indicates to all participants in the conversation that the keys have changed.
            // This will be synced to User B's Room DB via their message listener.
            Log.d(TAG, "Inserting system message in Firebase for each conversation affected by reset.");
            for (Map.Entry<String, String> entry : conversationPartnersForCleanupAndNotification.entrySet()) {
                String convId = entry.getKey();
                String partnerUserId = entry.getValue(); // The other user in this chat

                // Generate a unique push ID for the system message
                DatabaseReference systemMessageRef = rootRef.child("Messages").child(convId).push();
                String systemMessageId = systemMessageRef.getKey();

                if(systemMessageId != null) {
                    Map<String, Object> systemMessageData = new HashMap<>();
                    systemMessageData.put("message", "Security keys updated by " + userId + "."); // Message content (can be localized)
                    systemMessageData.put("type", "system_key_change"); // Special type to identify system messages
                    systemMessageData.put("from", userId); // Indicates who initiated the change
                    systemMessageData.put("to", partnerUserId); // Indicate the other user
                    systemMessageData.put("timestamp", ServerValue.TIMESTAMP); // Use server timestamp

                    systemMessageRef.setValue(systemMessageData)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Inserted system message for conv " + convId))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to insert system message for conv " + convId, e));
                } else {
                    Log.w(TAG, "Failed to generate Firebase ID for system message for conv " + convId);
                }
            }
            Log.d(TAG, "Finished inserting system messages in Firebase.");
            updateStatusText("System messages sent...");


            // --- Step 9: Update KeyManager with NEW Decrypted Keys (on Main Thread) ---
            // This happens after the reset process is complete.
            Log.d(TAG, "NEW KeyManager update will be posted to Main Thread.");


            successMessage = "Security Passphrase reset successfully!";


        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Firebase operation failed during key reset", e);
            resetException = new Exception("Failed to update keys on server. Network error?");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error decoding Base64 or processing key data during reset", e);
            resetException = new Exception("Invalid key data format.");
            SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear local keys
            YourKeyManager.getInstance().clearKeys(); // Ensure in-memory is clean
        } catch (Exception e) {
            Log.e(TAG, "An unexpected error occurred during key reset", e);
            resetException = new Exception("An unexpected error occurred during reset.");
            SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear local keys
            YourKeyManager.getInstance().clearKeys(); // Ensure in-memory is clean
        }


        // --- Post result back to Main Thread ---
        final String finalSuccessMessage = successMessage;
        final String finalErrorMessage = resetException != null ? resetException.getMessage() : null;
        final boolean finalLocalSaveSuccess = localSaveSuccess;

        final PublicKey finalNewPublicKey = (resetException == null) ? newPublicKey : null;
        final PrivateKey finalNewPrivateKey = (resetException == null) ? newPrivateKey : null;

        final String finalUserId = userId;


        mainHandler.post(() -> {
            progressBar.setVisibility(View.GONE);
            setUIEnabled(true); // Re-enable UI
            updateStatusText("Reset process finished."); // Final status update

            if (finalSuccessMessage != null) {
                Log.d(TAG, "Reset process successful. Showing toast and navigating.");
                Toast.makeText(ResetPassphraseActivity.this, finalSuccessMessage, Toast.LENGTH_LONG).show();

                // --- Update KeyManager with NEW Keys ---
                // If the reset was successful, load the newly generated keys into KeyManager.
                // This replaces the old (now unusable) keys in KeyManager.
                if (finalNewPublicKey != null && finalNewPrivateKey != null) { // Use final userId
                    Log.d(TAG, "Updating KeyManager with newly generated keys.");
                    YourKeyManager.getInstance().setKeys(finalUserId, finalNewPublicKey, finalNewPrivateKey); // Clears old keys and loads new RSA pair
                    Log.d(TAG, "KeyManager updated with new keys after reset.");

                    // After updating KeyManager with the new RSA pair, you *could* (optionally)
                    // trigger loading conversation keys *from Room* for chats that User A is still in.
                    // However, since User A's local Room DB was just cleared in Step 4, this load
                    // would find no conversation keys initially. The new conversation key version
                    // will be generated automatically when User A first opens a chat after reset.
                    // loadAllConversationKeysFromRoom(finalUserId); // This would load zero keys initially

                    // *** Handle RememberMe State Saving After Successful Reset ***
                    SharedPreferences prefs = getSharedPreferences("CircleUpPrefs", MODE_PRIVATE);
                    if (finalLocalSaveSuccess) {
                        prefs.edit().putBoolean("RememberMe", true).apply();
                        Log.d(TAG, "Reset (Main Thread): RememberMe preference set to TRUE after successful reset and NEW local save.");
                    } else {
                        prefs.edit().putBoolean("RememberMe", false).apply();
                        Log.w(TAG, "Reset (Main Thread): NEW local key save failed despite successful reset. RememberMe preference set FALSE.");
                        Toast.makeText(ResetPassphraseActivity.this, "Warning: Could not save new keys locally.", Toast.LENGTH_SHORT).show();
                    }
                    // *** END Handle RememberMe ***

                    // After successful reset and KeyManager update, navigate to MainActivity
                    Log.d(TAG, "Reset (Main Thread): Reset successful. Navigating to MainActivity.");
                    Intent mainIntent = new Intent(ResetPassphraseActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                    finish(); // Finish this activity

                } else {
                    // This indicates a serious issue - reset succeeded on server but generated keys were null.
                    Log.e(TAG, "Reset (Main Thread): Success message received, but new keys are null. Inconsistent state. Force logout.");
                    Toast.makeText(ResetPassphraseActivity.this, "Reset completed, but failed to load new keys. Log in again.", Toast.LENGTH_LONG).show();
                    forceLogout(); // Critical error, ensure state is clean and redirect to login
                }

            } else {
                // Reset process failed (e.g., re-authentication failed, Firebase update failed)
                Log.e(TAG, "Reset process failed. Showing error: " + finalErrorMessage);
                String displayMessage = finalErrorMessage != null ? finalErrorMessage : "Unknown error resetting passphrase.";
                Toast.makeText(ResetPassphraseActivity.this, displayMessage, Toast.LENGTH_LONG).show();

                // Keep UI enabled for user to potentially try again.
                if (identityVerificationTitle.getVisibility() == View.VISIBLE) {
                    editTextVerifyPassword.setText(""); // Clear password field
                }
                // The KeyManager and local storage state should already be cleaned by the executor task on failure.
            }
        }); // End mainHandler.post
    }); // End executorService.execute
}

// ... (rest of ResetPassphraseActivity.java) ...



    // --- Helper to force logout and redirect to Login ---
    private void forceLogout() {
        Log.d(TAG, "Force logging out after critical reset failure.");
        String userId = (currentUser != null) ? currentUser.getUid() : null;

        // Clear all local keys and KeyManager state
        if (userId != null) {
            SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
        } else {
            SecureKeyStorageUtil.clearSymmetricKey(getApplicationContext());
        }
        YourKeyManager.getInstance().clearKeys();

        // Sign out from Firebase
        auth.signOut();
        Log.d(TAG, "Firebase Auth signed out.");

        // Sign out from Google if applicable
        // Use application context for GoogleSignInClient.signOut to avoid leaks
        GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "Google SignOut completed during force logout.");
                    sendUserToLoginActivity(); // Redirect
                })
                .addOnFailureListener(task -> {
                    Log.e(TAG, "Google SignOut failed during force logout.", task.getCause());
                    sendUserToLoginActivity(); // Redirect even if Google sign out fails
                });
    }


    // --- Helper Navigation Method to go back to Login ---
    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity from ResetPassphraseActivity.");
        Intent loginIntent = new Intent(ResetPassphraseActivity.this, Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            Log.d(TAG, "ExecutorService shutting down in ResetPassphraseActivity.");
            executorService.shutdownNow();
        }
        // Dismiss dialog if using a ProgressDialog field
        // if (progressDialog != null && progressDialog.isShowing()) { progressDialog.dismiss(); }
        Log.d(TAG, "🔴 ResetPassphraseActivity onDestroy called.");
    }
}