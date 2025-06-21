package com.sana.circleup;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.sana.circleup.encryptionfiles.CryptoUtils;
import com.sana.circleup.encryptionfiles.SecureKeyStorageUtil;
import com.sana.circleup.encryptionfiles.YourKeyManager;
import com.sana.circleup.navigation_fragments.ChatFragment;
import com.sana.circleup.navigation_fragments.FindFriendsFragment;
import com.sana.circleup.navigation_fragments.GroupFragment;
import com.sana.circleup.navigation_fragments.MyContactsFragment;
import com.sana.circleup.navigation_fragments.RequestFragment;
import com.sana.circleup.room_db_implement.ChatDao;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.ConversationKeyDao;
import com.sana.circleup.room_db_implement.ConversationKeyEntity;
import com.sana.circleup.room_db_implement.GroupListDao;
import com.sana.circleup.temporary_chat_room.TemporaryChatRoomMain;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private DatabaseReference usersRef, RootRef;
    private TextView userName, userEmail;
    private ImageView userProfilePhoto;

    private ExecutorService keyLoadExecutor;
    private Handler mainHandler;
    private ProgressDialog keyLoadProgressDialog;
    private ChatDao chatDao; // <-- NEW: Declare ChatDao member

    private FragmentManager fragmentManager;
    private Fragment chatFragment, groupFragment, findFriendsFragment, contactsFragment, requestFragment;
    private Fragment activeFragment; // Keep track of the currently active fragment
    String currentUserID;

    private ConversationKeyDao conversationKeyDao;
    private ChildEventListener keyChangeNotificationListener; // NEW member

    private DatabaseReference currentUserBlockedStatusRef; // Reference to /Users/{uid}/isBlocked
    private ValueEventListener blockedStatusListener;



    // Firebase Listeners for Badge Counts
    private ValueEventListener chatGroupBadgeListener; // Listener for total unread from /Chat Summaries
    private ValueEventListener requestBadgeListener; // Listener for "received" requests from /Chat Requests

    // Variables to hold current unread counts
    private int unreadChatGroupCountForBadge = 0; // Total unread count from /Chat Summaries
    private int pendingRequestCountForBadge = 0;

    private ValueEventListener groupsBadgeListener;
    private ValueEventListener tempRoomsBadgeListener;


    // Variables to hold current unread counts for each type
    private int unreadPrivateChatCountForBadge = 0; // From /Chat Summaries (already exists, holds total messages)
    private int unreadGroupCountForBadge = 0; // New: Count of groups with unread messages for this user
    private int unreadTemporaryRoomCountForBadge = 0; // New: Count of temporary rooms with unread messages for this user

    // For the Requests tab (already exists)
// Variables to hold current unread counts for each type

    // In MainActivity


    private GroupListDao groupListDao; // <-- NEW: Declare GroupListDao member
// In MainActivity's onCreate:





    // Key to save/restore the tag of the active fragment
    private static final String ACTIVE_FRAGMENT_TAG = "active_fragment_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "MainActivity onCreate.");

        // Ensure KeyManager is initialized early
        YourKeyManager.getInstance();

        keyLoadExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        keyLoadProgressDialog = new ProgressDialog(this);
        keyLoadProgressDialog.setMessage("Checking account status...");
        keyLoadProgressDialog.setCancelable(false);


        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        RootRef = FirebaseDatabase.getInstance().getReference();

        ChatDatabase db = ChatDatabase.getInstance(this); // Get DB instance
        conversationKeyDao = db.conversationKeyDao(); // Get DAO
        chatDao = db.chatDao(); // <-- NEW: Initialize ChatDao member
        groupListDao = db.groupListDao(); // <-- NEW: Initialize GroupListDao member


        drawerLayout = findViewById(R.id.drawerlayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);



    fragmentManager = getSupportFragmentManager();

        // --- Fragment Initialization Logic ---
        if (savedInstanceState == null) {
            // Only add fragments the first time the Activity is created
            Log.d(TAG, "onCreate: Initializing fragments for the first time.");
            chatFragment = new ChatFragment();
            groupFragment = new GroupFragment();
            findFriendsFragment = new FindFriendsFragment();
            contactsFragment = new MyContactsFragment();
            requestFragment = new RequestFragment();

            fragmentManager.beginTransaction()
                    .add(R.id.container, chatFragment, "chat")
                    .add(R.id.container, groupFragment, "group").hide(groupFragment)
                    .add(R.id.container, findFriendsFragment, "find_friends").hide(findFriendsFragment)
                    .add(R.id.container, contactsFragment, "contacts").hide(contactsFragment)
                    .add(R.id.container, requestFragment, "requests").hide(requestFragment)
                    // Initially show the chat fragment
                    .show(chatFragment)
                    .commitNow();

            activeFragment = chatFragment; // Set initial active fragment

        } else {
            // Activity is being recreated (e.g., after rotation or process death)
            // FragmentManager automatically restores fragments. Get references by tag.
            Log.d(TAG, "onCreate: Restoring fragments from savedInstanceState.");
            chatFragment = fragmentManager.findFragmentByTag("chat");
            groupFragment = fragmentManager.findFragmentByTag("group");
            findFriendsFragment = fragmentManager.findFragmentByTag("find_friends");
            contactsFragment = fragmentManager.findFragmentByTag("contacts");
            requestFragment = fragmentManager.findFragmentByTag("requests");

            // Find the fragment that was visible (or intended to be active) before destruction/recreation
            // You might save the tag in onSaveInstanceState and retrieve it here,
            // or simply iterate and find the currently visible one.
            // Let's find the visible one for simplicity in this hide/show setup.
            String activeFragmentTag = savedInstanceState.getString(ACTIVE_FRAGMENT_TAG);
            if (activeFragmentTag != null) {
                activeFragment = fragmentManager.findFragmentByTag(activeFragmentTag);
                Log.d(TAG, "onCreate: Restored active fragment using tag: " + activeFragmentTag);
            }

            // Fallback if tag wasn't found or fragment couldn't be retrieved by tag (less likely with commitNow)
            if (activeFragment == null) {
                List<Fragment> fragments = fragmentManager.getFragments();
                for (Fragment fragment : fragments) {
                    if (fragment != null && fragment.isVisible()) { // Check fragment != null
                        activeFragment = fragment;
                        Log.d(TAG, "onCreate: Found visible fragment: " + fragment.getClass().getSimpleName());
                        break;
                    }
                }
            }

            // If still null (e.g., very first launch somehow failed state save), default to chat
            if (activeFragment == null) {
                activeFragment = chatFragment;
                Log.d(TAG, "onCreate: No visible or saved active fragment found, defaulting to chat.");
            }
            Log.d(TAG, "onCreate: Final active fragment set to: " + (activeFragment != null ? activeFragment.getClass().getSimpleName() : "null (FATAL)"));

            // Ensure only the active fragment is shown upon restoration
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if (chatFragment != null && activeFragment != chatFragment) ft.hide(chatFragment);
            if (groupFragment != null && activeFragment != groupFragment) ft.hide(groupFragment);
            if (findFriendsFragment != null && activeFragment != findFriendsFragment) ft.hide(findFriendsFragment);
            if (contactsFragment != null && activeFragment != contactsFragment) ft.hide(contactsFragment);
            if (requestFragment != null && activeFragment != requestFragment) ft.hide(requestFragment);
            if (activeFragment != null && activeFragment.isAdded()) { // Ensure activeFragment is not null and is added before showing
                ft.show(activeFragment);
            }
            ft.commitNow(); // Use commitNow to ensure state is updated immediately
        }
        // --- End Fragment Initialization Logic ---
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: Saving state.");
        // Save the tag of the currently active fragment
        if (activeFragment != null) {
            outState.putString(ACTIVE_FRAGMENT_TAG, activeFragment.getTag());
            Log.d(TAG, "onSaveInstanceState: Saved active fragment tag: " + activeFragment.getTag());
        }
    }

    // No need for onRestoreInstanceState, handling is done in onCreate

    @SuppressLint("RestrictedApi")
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity onStart.");
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.d(TAG, "onStart: No current Firebase user. Redirecting to Login.");
            sendUserToLoginActivity();
        } else {
            currentUserID = currentUser.getUid();
            Log.d(TAG, "onStart: Firebase user authenticated. UID: " + currentUserID);


            loadUnreadCountsFromRoom(currentUserID);
            // *** NEW: Attach notification listeners AFTER user is authenticated ***
            if (!TextUtils.isEmpty(currentUserID)) {
                attachNotificationListeners(); // <-- ADD THIS CALL HERE
            } else {
                Log.e(TAG, "onStart: currentUserID is null, skipping notification listener attachment.");
            }


            // *** NEW: Attach Real-time Listener for isBlocked status ***
            if (currentUserBlockedStatusRef == null && blockedStatusListener == null) { // Check if listener is not already set up
                // Get a reference to the specific user's isBlocked field
                currentUserBlockedStatusRef = RootRef.child("Users") // Use your RootRef member variable
                        .child(currentUserID)
                        .child("isBlocked");

                blockedStatusListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // This is triggered whenever the 'isBlocked' value changes for the current user
                        // It also triggers once initially when the listener is attached
                        Log.d(TAG, "Blocked status listener onDataChange triggered in MainActivity.");

                        Boolean isBlockedBoolean = snapshot.getValue(Boolean.class);
                        boolean isBlocked = isBlockedBoolean != null && isBlockedBoolean; // Safely get boolean value

                        if (isBlocked) {
                            // Account has been blocked by admin!
                            Log.d(TAG, "Account isBlocked status changed to TRUE. Initiating logout from MainActivity.");
                            // --- Perform Logout and Redirect ---
                            // Make sure to dismiss any dialogs if showing
                            if (keyLoadProgressDialog != null && keyLoadProgressDialog.isShowing()) {
                                keyLoadProgressDialog.dismiss();
                            }
                            // Show a final toast on the main thread
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Your account has been disabled by an administrator.Contact at circleup0719@gmail.com", Toast.LENGTH_LONG).show();
                            });

                            // Perform the actual logout (signs out from Firebase Auth)
                            LoggingOut(); // Call your existing LoggingOut method which handles Firebase sign out and redirect
                            // Note: LoggingOut method already clears keys, local storage, and redirects.

                            // ------------------------------------
                        } else {
                            // Account is NOT blocked (or has been re-enabled)
                            Log.d(TAG, "Account isBlocked status is FALSE or changed from TRUE to FALSE.");
                            // User remains logged in and can continue using the app.
                            // If a user was blocked and then re-enabled *while* in the app,
                            // they will stay logged in.
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle listener cancellation or error (e.g., network issues, security rules)
                        Log.e(TAG, "Firebase blocked status listener cancelled in MainActivity.", error.toException());
                        // Decide how to handle this - perhaps show a warning, but don't necessarily log out
                        // immediately unless you are sure the status is unrecoverable.
                        runOnUiThread(() -> { // Show toast on main thread
                            Toast.makeText(MainActivity.this, "Warning: Could not monitor account status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                };

                // Attach the listener
                currentUserBlockedStatusRef.addValueEventListener(blockedStatusListener);
                Log.d(TAG, "Attached blocked status listener to: " + currentUserBlockedStatusRef.getPath());
            } else {
                Log.d(TAG, "Blocked status listener already attached or cannot be attached.");
            }
            // *** END NEW LISTENER SETUP ***





            // *** NEW KEY LOADING LOGIC START *** // NEW BLOCK
            boolean isPrivateKeyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable(); // NEW

            if (isPrivateKeyAvailable) {
                Log.d(TAG, "onStart: Private key is already available in KeyManager."); // NEW log
                // Private key is available (from previous activity or earlier load).
                // Proceed with normal UI setup.
                setupMainActivityUIAndProceed(); // (May have been existing, but now called here)
                // Also initiate loading of all conversation keys from Room cache into KeyManager cache // NEW
                loadAllConversationKeysFromRoom(currentUserID); // NEW Call

                // Attach key change listener ONLY if keys are available // NEW
                attachKeyChangeNotificationListener(); // NEW Call

            } else {
                Log.d(TAG, "onStart: User authenticated, but private key not available. Attempting secure load from storage."); // NEW log
                // Private key is NOT available (likely on a cold start).
                // Initiate the background loading process.
                // Show progress dialog // NEW
                if (keyLoadProgressDialog != null && !keyLoadProgressDialog.isShowing()) { // NEW check
                    keyLoadProgressDialog.setMessage("Loading secure keys..."); // NEW message
                    keyLoadProgressDialog.show(); // NEW show
                }
                attemptSecureKeyLoad(currentUserID); // NEW Call to initiate background task
                // Note: keyChangeNotificationListener is attached in setupMainActivityUIAndProceed
                // if the local key load succeeds.
            }



//            if (YourKeyManager.getInstance().isPrivateKeyAvailable()) {
//                Log.d(TAG, "onStart: Private key is available in KeyManager. Proceeding with MainActivity setup.");
//                setupMainActivityUIAndProceed();
//                attachKeyChangeNotificationListener(); // <-- NEW: Attach listener if keys are available
//            } else {
//                Log.d(TAG, "onStart: User authenticated, but private key not available. Attempting secure load.");
//                if (keyLoadProgressDialog != null && keyLoadProgressDialog.isShowing()) {
//                    keyLoadProgressDialog.dismiss();
//                }
//                attemptSecureKeyLoad(currentUserID);
//                // The listener will be attached in setupMainActivityUIAndProceed if local load succeeds.
//            }
        }
    }



    private void attemptSecureKeyLoad(String userId) {
        Log.d(TAG, "attemptSecureKeyLoad: Background task starting for user: " + userId);

        // Validate user ID (This part remains critical and redirects on fatal error)
        if (TextUtils.isEmpty(userId)) {
            Log.e(TAG, "attemptSecureKeyLoad: Called with empty userId! Critical error.");
            // Dismiss dialog and redirect on main thread for critical error
            mainHandler.post(() -> {
                if (keyLoadProgressDialog != null && keyLoadProgressDialog.isShowing()) keyLoadProgressDialog.dismiss();
                Toast.makeText(this, "Critical error loading user data. Please log in again.", Toast.LENGTH_LONG).show();
                // Critical error, clear state before redirect
                // Use application context for SecureKeyStorageUtil to avoid leaks
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clean up any partial data
                YourKeyManager.getInstance().clearKeys(); // Ensure KeyManager is empty
                // On a critical user ID error, we should disable RememberMe to force a proper login.
                SharedPreferences prefs = getSharedPreferences("CircleUpPrefs", MODE_PRIVATE);
                prefs.edit().putBoolean("RememberMe", false).apply();
                Log.w(TAG, "attemptSecureKeyLoad: RememberMe preference set to FALSE due to empty userId critical error.");

                sendUserToLoginActivity(); // Redirect for fatal authentication failure
            });
            return; // Stop execution for critical error
        }

        // If userId is valid, proceed with the background key loading attempt
        keyLoadExecutor.execute(() -> {
            SecretKey symmetricKey = null;
            PrivateKey decryptedPrivateKey = null;
            PublicKey userPublicKey = null; // To store the public key loaded from local file
            byte[] publicKeyBytes_local = null; // Keep bytes for potential public-only save

            // Flag to indicate if the private key was successfully decrypted from local storage
            boolean privateKeySuccessfullyDecrypted = false;

            final String[] errorMessage = {null}; // Use array to modify inside lambda


            try {
                // --- Step 1: Attempt to load Symmetric Key from EncryptedSharedPreferences ---
                Log.d(TAG, "attemptSecureKeyLoad (Executor): Attempting to load symmetric key from EncryptedSharedPreferences.");
                // Use the application context to avoid Activity context leaks
                symmetricKey = SecureKeyStorageUtil.loadSymmetricKey(getApplicationContext()); // Use your SecureKeyStorageUtil

                if (symmetricKey != null) {
                    Log.d(TAG, "attemptSecureKeyLoad (Executor): Symmetric key loaded successfully from EncryptedSharedPreferences.");

                    // --- Step 2: Attempt to load Encrypted Key Pair from EncryptedFile ---
                    Log.d(TAG, "attemptSecureKeyLoad (Executor): Attempting to load Encrypted Key Pair from EncryptedFile for user: " + userId);
                    // Use the application context to avoid Activity context leaks
                    byte[][] keyPairBytes = SecureKeyStorageUtil.loadEncryptedKeyPair(getApplicationContext(), userId); // Use your SecureKeyStorageUtil

                    if (keyPairBytes != null && keyPairBytes.length == 2 && keyPairBytes[0] != null && keyPairBytes[1] != null) {
                        Log.d(TAG, "attemptSecureKeyLoad (Executor): Encrypted key pair loaded successfully from EncryptedFile.");
                        byte[] encryptedPrivateKeyWithIV_local = keyPairBytes[0]; // The encrypted private key + IV
                        publicKeyBytes_local = keyPairBytes[1]; // The user's public key bytes (keep this for potential public-only load)


                        // --- Step 3: Attempt to decrypt Private Key using Loaded Symmetric Key ---
                        Log.d(TAG, "attemptSecureKeyLoad (Executor): Attempting to decrypt private key using loaded symmetric key...");
                        try {
                            // Use the symmetric key loaded from prefs to decrypt the private key from the file
                            decryptedPrivateKey = CryptoUtils.decryptPrivateKey(encryptedPrivateKeyWithIV_local, symmetricKey); // Use your CryptoUtils method
                            Log.d(TAG, "attemptSecureKeyLoad (Executor): Private key decryption successful using local keys.");

                            // Convert the loaded public key bytes to a PublicKey object
                            userPublicKey = CryptoUtils.bytesToPublicKey(publicKeyBytes_local); // Use your CryptoUtils method
                            Log.d(TAG, "attemptSecureKeyLoad (Executor): Public key conversion successful from local bytes.");

                            // Set flag indicating successful decryption from local storage
                            privateKeySuccessfullyDecrypted = true;
                            Log.d(TAG, "attemptSecureKeyLoad (Executor): Private key successfully decrypted from local secure storage.");

                            // --- Success: RSA Keys Loaded and Decrypted using LOCAL storage ---
                            // Load keys into YourKeyManager for the session.
                            YourKeyManager.getInstance().setKeys(userId, userPublicKey, decryptedPrivateKey); // Use your KeyManager
                            Log.d(TAG, "attemptSecureKeyLoad (Executor): Decrypted RSA keys loaded into KeyManager.");


                            // After successfully loading the user's RSA Private Key into KeyManager,
                            // load all conversation keys from Room DB into KeyManager's cache.
                            // This happens asynchronously on the DB executor, initiated from here.
                            Log.d(TAG, "attemptSecureKeyLoad (Executor): Private key available. Starting conversation key load from Room.");
                            if (!TextUtils.isEmpty(userId)) { // Safety check for userId
                                loadAllConversationKeysFromRoom(userId); // <<< CALL THIS HELPER METHOD
                            } else {
                                Log.e(TAG, "attemptSecureKeyLoad (Executor): Cannot load conversation keys from Room, userId is unexpectedly null.");
                            }


                        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException |
                                 NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                                 BadPaddingException | InvalidAlgorithmParameterException e) {
                            // Crypto failure during decryption using locally stored symmetric key
                            Log.e(TAG, "attemptSecureKeyLoad (Executor): Decryption failed using local keys. Clearing local keys.", e);
                            // Determine error message
                            if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof InvalidKeyException) {
                                // These often indicate wrong symmetric key or corrupt encrypted data
                                errorMessage[0] = "Failed to unlock account locally (Incorrect key or corrupt data).";
                            } else {
                                errorMessage[0] = "An error occurred decrypting local keys.";
                            }
                            // Clear potentially corrupt local data (symmetric key and key pair file)
                            SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Use your SecureKeyStorageUtil

                            // Try to load just the public key if its bytes were available and convertible
                            PublicKey loadedPublicKey = null;
                            if (publicKeyBytes_local != null) {
                                try { loadedPublicKey = CryptoUtils.bytesToPublicKey(publicKeyBytes_local); } catch (Exception pubEx) { Log.e(TAG, "Error converting publicKeyBytes_local after decryption failure", pubEx); loadedPublicKey = null; }
                            }
                            if (loadedPublicKey != null) {
                                YourKeyManager.getInstance().setPublicOnly(userId, loadedPublicKey); // Set Public-Only if possible
                                Log.d(TAG, "attemptSecureKeyLoad (Executor): Set public key into KeyManager after private key decryption failed.");
                            } else {
                                YourKeyManager.getInstance().clearKeys(); // Clear everything if public key also failed
                                Log.d(TAG, "attemptSecureKeyLoad (Executor): Cleared all keys from KeyManager as both failed.");
                            }

                            privateKeySuccessfullyDecrypted = false; // Explicitly false

                        } catch (Exception e) {
                            // Any other unexpected error during local decryption/load chain
                            Log.e(TAG, "attemptSecureKeyLoad (Executor): Unexpected error during local key decryption/load.", e);
                            errorMessage[0] = "An unexpected error occurred during local key load.";
                            SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
                            YourKeyManager.getInstance().clearKeys();
                            privateKeySuccessfullyDecrypted = false;
                        }

                    } else {
                        Log.d(TAG, "attemptSecureKeyLoad (Executor): Encrypted key pair file not found or failed to load for user: " + userId + ". Symmetric key was found.");
                        // This implies missing file OR SecureKeyStorageUtil load failed (GeneralSecurityException/IOException on load)
                        errorMessage[0] = "Secure keys file missing or corrupt locally.";



                        Log.d(TAG, "attemptSecureKeyLoad (Executor): Automatic private key decryption skipped (symmetric key not found). Private key will be unavailable in KeyManager until passphrase entered.");




//                        if (!TextUtils.isEmpty(userId)) {
//                            loadAllConversationKeysFromRoom(userId, false); // Pass false because private key is NOT available
//                        }


//                        SecureKeyStorageUtil.clearSymmetricKey(getApplicationContext()); // Clear symmetric key as key pair file is missing
//                        YourKeyManager.getInstance().clearKeys(); // Clear everything (no private key means symmetric key is useless)
//                        privateKeySuccessfullyDecrypted = false;
                    }

                } else {
                    Log.d(TAG, "attemptSecureKeyLoad (Executor): Symmetric key not found in EncryptedSharedPreferences for user: " + userId);
                    // No symmetric key means we cannot decrypt the private key.
                    errorMessage[0] = "Secure symmetric key not found locally.";
                    SecureKeyStorageUtil.clearEncryptedKeyPair(getApplicationContext(), userId); // Clear key pair file as symmetric key is missing
                    YourKeyManager.getInstance().clearKeys(); // Clear everything
                    privateKeySuccessfullyDecrypted = false;
                }

            } catch (Exception e) {
                // Catch any exceptions during the initial symmetric key load attempt itself (e.g., master key issue)
                Log.e(TAG, "attemptSecureKeyLoad (Executor): Exception during initial local symmetric key load attempt", e);
                errorMessage[0] = "Failed to load keys from secure storage.";
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear everything local
                YourKeyManager.getInstance().clearKeys(); // Ensure KeyManager is empty
                privateKeySuccessfullyDecrypted = false;
            }


            // --- Handle result of local loading attempt ---
            // Post result back to Main Thread regardless of success or failure
            final String finalToastMessage;
            if (errorMessage[0] != null) {
                finalToastMessage = errorMessage[0] + " Please log in to re-sync.";
            } else {
                // If no error message, imply success for the local load attempt
                finalToastMessage = "Account unlocked from local storage.";
            }

            mainHandler.post(() -> { // Post to the main thread to update UI and proceed
                if (keyLoadProgressDialog != null && keyLoadProgressDialog.isShowing()) {
                    keyLoadProgressDialog.dismiss();
                }

                // Show toast indicating the result of the local load attempt
                Toast.makeText(this, finalToastMessage, Toast.LENGTH_LONG).show(); // Use LONG to ensure user sees the message

                // --- ALWAYS PROCEED TO SETUP MAIN ACTIVITY UI ---
                // The state of YourKeyManager (set above in the executor) determines which secure features are enabled.
                Log.d(TAG, "attemptSecureKeyLoad (Main Thread): Proceeding to setup MainActivity UI based on KeyManager state. Private Key Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable());
                setupMainActivityUIAndProceed(); // Proceed with normal setup regardless of local key unlock success
                // We do NOT call sendUserToLoginActivity() here anymore based on local load failure.

            });

            Log.d(TAG, "attemptSecureKeyLoad (Executor): Background task FINISHED.");
        });
    }

// ... (Rest of the MainActivity.java class remains the same) ...

    private void setupMainActivityUIAndProceed() {
        Log.d(TAG, "Setting up MainActivity UI and proceeding.");
        if (keyLoadProgressDialog != null && keyLoadProgressDialog.isShowing()) {
            keyLoadProgressDialog.dismiss();
        }



        if (TextUtils.isEmpty(currentUserID)) {
            Log.e(TAG, "setupMainActivityUIAndProceed: currentUserID is null. Cannot set OneSignal External User ID.");
            // Continue with UI setup, but notifications targeting this user ID might not work.
        } else {
            // Set OneSignal External User ID (Firebase UID)
            // This links the OneSignal Player ID with the logged-in user's ID.
            Log.d(TAG, "setupMainActivityUIAndProceed: Setting OneSignal External User ID: " + currentUserID);
            // *** UN-COMMENT THIS LINE ***
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            OneSignal.login(currentUserID);


        }
        // *** END NEW ***

        setupNavigationHeader();
        setupDrawer();
        setupBottomNavigation(); // This will now handle showing the correct fragment


        // Update user state (online status) now that we are ready to use the app
        // Only set online if public key is available (implies user data node exists and loaded)
        if (YourKeyManager.getInstance().getUserPublicKey() != null) {
            Log.d(TAG, "User is fully or partially logged in with keys. Setting state to online.");
            updateUserState("online");
        } else {
            // This case should ideally not be reached if the logic in onStart is correct
            Log.e(TAG, "User state update skipped: Keys are unexpectedly null in setupMainActivityUIAndProceed.");
        }


        // Check admin role after ensuring keys are loaded and user data is accessible
        checkUserRoleAndNavigate(currentUserID);
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity onStop.");

        // *** NEW: Remove the blocked status listener ***
        if (currentUserBlockedStatusRef != null && blockedStatusListener != null) {
            currentUserBlockedStatusRef.removeEventListener(blockedStatusListener);
            Log.d(TAG, "Removed blocked status listener.");
        }
        // *** END REMOVE LISTENER ***

        removeKeyChangeNotificationListener(); // Keep this one

        // *** NEW: Remove notification listeners for badges ***
        removeNotificationListeners(); // <-- ADD THIS CALL HERE
        // *** END NEW CALL ***
        // Only set offline if user was authenticated AND at least public key was available
        // Rely on YourKeyManager state, which is cleared on explicit logout/delete.
        if (isFinishing() && auth.getCurrentUser() != null && YourKeyManager.getInstance().getUserPublicKey() != null) {
            Log.d(TAG, "onStop: Activity is finishing. User is logged in and keys available. Setting state to offline.");
            // Note: update state might be unreliable in onDestroy, especially if app process is killed abruptly.
            // onDisconnect is more reliable for sudden exits. But good practice to set it here too.
            updateUserState("offline");
        } else {
            Log.d(TAG, "onStop: Activity is not finishing OR user not fully logged in/keys not loaded. Skipping offline status update.");
        }
    }







    // --- NEW: Implement Key Change Notification Listener ---
    private void attachKeyChangeNotificationListener() {
        if (TextUtils.isEmpty(currentUserID)) {
            Log.w(TAG, "Cannot attach key change notification listener: currentUserID is null.");
            return;
        }

        // Only attach listener if the user's Private Key is available (implies they are unlocked for secure features)
        if (!YourKeyManager.getInstance().isPrivateKeyAvailable()) {
            Log.d(TAG, "Skipping attachment of key change notification listener: Private key not available.");
            return; // Do not attach listener if secure features are disabled
        }


        if (keyChangeNotificationListener == null) {
            // Listen to the node where notifications for THIS user arrive
            DatabaseReference keyChangeNotificationsRef = RootRef.child("KeyChangeNotifications").child(currentUserID);

            keyChangeNotificationListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // A new key change notification received (e.g., from User A resetting)
                    // The key is the timestamp string
                    String notificationKey = snapshot.getKey(); // This is the timestamp key
                    Log.d(TAG, "KeyChangeNotificationListener: onChildAdded triggered for notification key: " + notificationKey);

                    Map<String, Object> notificationData = (Map<String, Object>) snapshot.getValue();

                    if (notificationData != null) {
                        String senderUserId = (String) notificationData.get("senderUserId"); // The user who reset
                        String conversationId = (String) notificationData.get("conversationId"); // The affected conversation
                        String changeType = (String) notificationData.get("type"); // "passphrase_reset"
                        Long timestampLong = null; // Timestamp of the notification itself
                        Object timestampObj = notificationData.get("timestamp");
                        if (timestampObj instanceof Long) {
                            timestampLong = (Long) timestampObj;
                        } else if (timestampObj instanceof Double) {
                            timestampLong = ((Double) timestampObj).longValue();
                        }

                        Log.d(TAG, "Received key change notification from " + senderUserId + " for conv " + conversationId + ", Type: " + changeType + ", Notification Timestamp: " + notificationKey);


                        // --- Action for User B: Signal UI Update and potentially pre-fetch data ---
                        // User B (current user) received a notification that User A's keys changed.
                        // This means existing messages from User A might become unreadable until User B's app
                        // syncs the new key version (which happens automatically if they open the chat,
                        // or if the ConversationKeyListener in ChatPageActivity is active).

                        // 1. Update the corresponding ChatEntity in Room to visually signal the change in the chat list.
                        // This will trigger ChatFragment's LiveData observer and forceRefreshDisplay.
                        if (!TextUtils.isEmpty(conversationId) && !TextUtils.isEmpty(senderUserId) && chatDao != null && ChatDatabase.databaseWriteExecutor != null) {
                            // Use the shared database executor to update Room
                            ChatDatabase.databaseWriteExecutor.execute(() -> { // Use static executor
                                try {
                                    // Assuming ChatDao has a method to update a flag for a specific chat partner for the owner
                                    // Make sure this method exists and takes ownerId, partnerId, and the flag value
                                    // You need to add a boolean field like 'partnerKeysChanged' to your ChatEntity
                                    chatDao.updatePartnerKeysChangedFlag(currentUserID, senderUserId, true); // Needs implementation in ChatDao
                                    Log.d(TAG, "Marked partner keys changed flag in Room for chat with " + senderUserId + " owned by " + currentUserID);

                                    // After updating Room, ChatFragment's LiveData observer will trigger
                                    // ChatFragment.forceRefreshDisplay will then redraw the list item,
                                    // where you would check this flag to show a UI element.

                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to update partnerKeysChanged flag in Room for " + senderUserId + " owned by " + currentUserID, e);
                                }
                            });
                        } else {
                            Log.w(TAG, "Skipping Room update for key change notification: Missing IDs, DAO, or Executor.");
                        }


                        // 2. Optionally show a non-intrusive UI indication (Toast, Snackbar)
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Security keys updated for a contact.", Toast.LENGTH_SHORT).show();
                            // Or a more specific message like:
                            // Toast.makeText(MainActivity.this, "Keys updated for [Username]. Old messages might be unreadable.", Toast.LENGTH_LONG).show(); // Need a helper to get username or fetch it here
                        });

                        // 3. Clean up the notification node in Firebase after processing it
                        // Using the notificationKey (timestamp string)
                        snapshot.getRef().removeValue().addOnSuccessListener(aVoid -> Log.d(TAG, "Removed processed key change notification: " + notificationKey))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove processed notification: " + notificationKey, e));

                    } else {
                        Log.w(TAG, "KeyChangeNotificationListener: Received null notification data for key: " + notificationKey);
                        // Still attempt to clean up potentially malformed notification node
                        snapshot.getRef().removeValue().addOnSuccessListener(aVoid -> Log.d(TAG, "Removed malformed key change notification: " + notificationKey))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove malformed notification: " + notificationKey, e));
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Log.d(TAG, "KeyChangeNotificationListener: onChildChanged triggered for key: " + snapshot.getKey());
                    // If the notification data itself changes, re-process it
                    onChildAdded(snapshot, previousChildName);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "KeyChangeNotificationListener: onChildRemoved triggered for key: " + snapshot.getKey());
                    // Notification removed - no action needed as we handled cleanup in onChildAdded
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Log.d(TAG, "KeyChangeNotificationListener: onChildMoved triggered for key: " + snapshot.getKey());
                    // Ordering changed - no action needed
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "KeyChangeNotificationListener: Firebase Listener Cancelled for user " + currentUserID + ": " + error.getMessage(), error.toException());
                    // Handle error if needed (e.g., inform user that key change notifications might not be working)
                }
            };

            keyChangeNotificationsRef.addChildEventListener(keyChangeNotificationListener);
            Log.d(TAG, "Key change notification listener attached to: KeyChangeNotifications/" + currentUserID);
        }
    }


// --- END NEW ---

        private void removeKeyChangeNotificationListener () {
            if (RootRef != null && keyChangeNotificationListener != null && !TextUtils.isEmpty(currentUserID)) {
                DatabaseReference keyChangeNotificationsRef = RootRef.child("KeyChangeNotifications").child(currentUserID);
                keyChangeNotificationsRef.removeEventListener(keyChangeNotificationListener);
                keyChangeNotificationListener = null;
                Log.d(TAG, "Key change notification listener removed.");
            } else {
                Log.d(TAG, "Key change notification listener was null or RootRef/currentUserID empty, nothing to remove.");
            }
        }



private void checkUserRoleAndNavigate(String userId) {
    Log.d(TAG, "checkUserRoleAndNavigate called in MainActivity for user: " + userId);
    // In MainActivity, the user has already passed login checks in Login.java.
    // This check is less for security blocking and more for verifying data/role.
    // The critical ADMIN role check that prevents login to *this* app is done ONLY in Login.java.

    // Check if Firebase Auth user is present (should be, but defensive).
    FirebaseUser currentUser = auth.getCurrentUser();
    if (currentUser == null || TextUtils.isEmpty(userId) || !currentUser.getUid().equals(userId)) {
        Log.e(TAG, "checkUserRoleAndNavigate (MainActivity): Firebase Auth user missing or ID mismatch! Forcing re-login.");
        Toast.makeText(this, "Authentication state mismatch. Please log in again.", Toast.LENGTH_LONG).show();
        // Critical error: User is not authenticated properly. Clear state before redirect.
        SecureKeyStorageUtil.clearAllSecureKeys(MainActivity.this, userId); // Use potentially valid userId
        YourKeyManager.getInstance().clearKeys();
        SharedPreferences prefs = getSharedPreferences("CircleUpPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("RememberMe", false).apply();
        auth.signOut();
        sendUserToLoginActivity(); // Redirect back to Login
        return;
    }

    // Fetch user data to ensure the user node exists and get the role for potential logging.
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

    userRef.get().addOnCompleteListener(task -> {
        // Progress dialog is dismissed by setupMainActivityUIAndProceed before this.
        // No need to dismiss here.

        if (task.isSuccessful() && task.getResult().exists()) {
            DataSnapshot snapshot = task.getResult();
            String role = snapshot.child("role").getValue(String.class);

            // Log the outcome for informational purposes
            if (role != null && role.equals("admin")) {
                // theoretically, an admin user should NEVER reach here after the fix in Login.java.
                // If they do, it indicates a serious flow/logic error.
                Log.e(TAG, "checkUserRoleAndNavigate (MainActivity): Admin user detected in MainActivity! This should not happen. User UID: " + userId);
                Toast.makeText(this, "Security Error: Detected Admin account in user app. Logging out.", Toast.LENGTH_LONG).show();
                // Force logout as a failsafe
                LoggingOut(); // Use your existing logout method
            } else if (role != null && role.equals("user")) {
                // Standard user - continue with normal app flow.
                Log.d(TAG, "checkUserRoleAndNavigate (MainActivity): User is standard user. Continuing with Main Activity UI.");
                // No explicit navigation or finish() needed. The UI is already set up by setupMainActivityUIAndProceed.
            } else {
                // Role is missing, empty, or something unexpected (not "admin" or "user")
                Log.w(TAG, "checkUserRoleAndNavigate (MainActivity): User role is missing, empty, or invalid ('" + role + "') for UID: " + userId + ". User data valid, but role is ambiguous.");
                // Decide how strict to be. If a valid user node exists but role is weird, maybe let them in but log.
                // For now, just log and let them continue, assuming the Login checks were more strict.
            }

            // KeyManager state is already set by attemptSecureKeyLoad before this method runs.
            // UI based on key state is handled by fragments' LiveData observers.

        } else {
            // User data not found in DB for an authenticated user - Critical error in MainActivity.
            Log.e(TAG, "checkUserRoleAndNavigate (MainActivity): User data not found in DB for UID: " + userId + ". This indicates a critical data inconsistency after login. Logging out.");
            Toast.makeText(MainActivity.this, "User data missing post-login! Please contact support. Logging out.", Toast.LENGTH_LONG).show();
            // Force logout
            LoggingOut(); // Use your existing logout method
        }
    });
}


    private void setupNavigationHeader() {
        if (drawerLayout == null || navigationView == null || toolbar == null) {
            Log.e(TAG, "setupNavigationHeader: UI components not initialized.");
            return;
        }
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) {
            Log.e(TAG, "setupNavigationHeader: Navigation header view is null.");
            return;
        }
        userName = headerView.findViewById(R.id.userName);
        userEmail = headerView.findViewById(R.id.userEmail);
        userProfilePhoto = headerView.findViewById(R.id.userprofile_photo);


        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "setupNavigationHeader: Firebase user authenticated. Attempting to load profile data for user: " + user.getUid());
            // Call the method to load profile data (username, email, image) from Firebase DB
            loadUserProfile(user.getUid()); // <-- Always call this if user is logged in
        } else {
            // If user is NOT authenticated (this block should ideally not be reached if
            // MainActivity.onStart correctly redirects unauthenticated users),
            // show default or error state in the header.
            Log.w(TAG, "setupNavigationHeader: Firebase user is null. Showing default header state.");
            userName.setText("Guest"); // Or "Not Logged In"
            userEmail.setText(""); // Clear email
            userProfilePhoto.setImageResource(R.drawable.default_profile_img); // Set default image
            // You might also want to hide profile image view or disable clicks
            userProfilePhoto.setOnClickListener(null); // Remove click listener for default image
        }
        // --- END MODIFIED CONDITION ---
    }

    private void loadUserProfile(String userId) {
        if (userName == null || userEmail == null || userProfilePhoto == null) {
            Log.e(TAG, "loadUserProfile: Header UI components not initialized.");
            // Attempt to re-setup headers if they are null
            setupNavigationHeader();
            if (userName == null) return; // Exit if re-setup failed
        }
        Log.d(TAG, "Fetching user profile data for UID: " + userId);


        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "User profile data found in DB.");
                    // Use User model for type safety and cleaner access
                    Users userProfile = snapshot.getValue(Users.class);

                    if (userProfile != null) {
                        userName.setText(userProfile.getUsername() != null ? userProfile.getUsername() : "Unknown");
                        userEmail.setText(userProfile.getEmail() != null ? userProfile.getEmail() : "No Email");

                        String profileImageBase64 = userProfile.getProfileImage();
                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            Bitmap profileBitmap = decodeBase64ToBitmap(profileImageBase64);
                            if (profileBitmap != null) {
                                userProfilePhoto.setImageBitmap(profileBitmap);
                            } else {
                                Log.w(TAG, "Failed to decode profile image Base64. Using default.");
                                userProfilePhoto.setImageResource(R.drawable.default_profile_img);
                            }
                        } else {
                            Log.d(TAG, "ProfileImage Base64 string is empty or null. Using default.");
                            userProfilePhoto.setImageResource(R.drawable.default_profile_img);
                        }

                        // Set click listener for full screen image
                        userProfilePhoto.setOnClickListener(v -> {
                            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                                Intent intent = new Intent(MainActivity.this, FullscreenImageActivity.class); // Ensure FullscreenImageActivity exists
                                intent.putExtra("profileImage", profileImageBase64);
                                startActivity(intent);
                            } else {
                                Toast.makeText(MainActivity.this, "Profile image not available", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Log.e(TAG, "Failed to parse User object from snapshot for UID: " + userId);
                        userName.setText("Error");
                        userEmail.setText("Error");
                        userProfilePhoto.setImageResource(R.drawable.default_profile_img);
                    }

                } else {
                    Log.e(TAG, "User data not found in DB for UID: " + userId + " during profile load.");
                    userName.setText("Error");
                    userEmail.setText("Error");
                    userProfilePhoto.setImageResource(R.drawable.default_profile_img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile data", error.toException());
                Toast.makeText(MainActivity.this, "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                userName.setText("Error Loading");
                userEmail.setText("Error Loading");
                userProfilePhoto.setImageResource(R.drawable.default_profile_img);
            }
        });
    }


    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error decoding Base64 to Bitmap", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error decoding Base64 to Bitmap", e);
            return null;
        }
    }


    private void setupDrawer() {
        if (drawerLayout == null || navigationView == null || toolbar == null) {
            Log.e(TAG, "setupDrawer: UI components not initialized.");
            return;
        }
        Log.d(TAG, "Setting up Navigation Drawer.");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.OpenDrawer, R.string.CloseDrawer
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            boolean isSecureMessagingAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable();

            boolean requiresPrivateKey = (id == R.id.create_new_group || id == R.id.scheduled_messages ||
                    id == R.id.temporary_chat_room || id == R.id.drafts); // Ensure these IDs match your menu


            if (requiresPrivateKey && !isSecureMessagingAvailable) {
                Log.w(TAG, "Attempted secure action (" + item.getTitle() + ") with private key unavailable.");
                Toast.makeText(this, "Secure keys are not loaded. Please unlock your account to use this feature.", Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }


            if (id == R.id.settings) {
                Log.d(TAG, "Navigating to Settings.");
                Intent intent = new Intent(MainActivity.this, Setting_profile.class);
                intent.putExtra("isNewUser", false);
                startActivity(intent);
            } else if (id == R.id.create_new_group) {
                Log.d(TAG, "Navigating to Create Group.");
                startActivity(new Intent(MainActivity.this, CreateGroupActivity.class)); // Ensure CreateGroupActivity exists
            } else if (id == R.id.scheduled_messages) {
                Log.d(TAG, "Navigating to Scheduled Messages.");
                Intent intent = new Intent(this, ScheduledMSG.class); // Ensure ScheduledMSG exists
                startActivity(intent);
            }
            else if ( id == R.id.nav_about) {
                Log.d(TAG, "Navigating to Privacy Policy.");
                Intent intent = new Intent(this, About.class); // Ensure PrivacyPolicy exists
                startActivity(intent);
            }
            else if ( id == R.id.nav_feedback)
            {
                Log.d(TAG, "Navigating to Feedback.");
                Intent intent = new Intent(this, Feedback.class); // Ensure Feedback exists
                startActivity(intent);
            }
            else if ( id == R.id.privacy_and_security)
            {
                Log.d(TAG, "Navigating to Privacy and Security.");
                Intent intent = new Intent(this, PrivacyAndSecurity.class); // Ensure PrivacyAndSecurity exists
                startActivity(intent);
            }
            else if (id == R.id.temporary_chat_room) {
                Log.d(TAG, "Navigating to Temporary Chat.");
                Intent intent = new Intent(this, TemporaryChatRoomMain.class); // Ensure TemporaryChatRoomMain exists
                startActivity(intent);
            }
            else if (id == R.id.drafts) {
                Log.d(TAG, "Navigating to Drafts.");
                Intent intent = new Intent(this, DraftScheduled.class); // Ensure DraftScheduled exists
                startActivity(intent);
            }else if (id == R.id.logout) {
                Log.d(TAG, "Initiating Logout.");
                LoggingOut();
                return true; // Handled the click
            } else if (id == R.id.nav_delete_account) {
                Log.d(TAG, "Initiating Delete Account.");
                confirmDeleteAccount();
                return true; // Handled the click
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true; // Item click handled
        });
    }


    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Check if BottomNavigationView and necessary fragments are initialized
        if (bottomNavigation == null || fragmentManager == null || chatFragment == null || groupFragment == null || findFriendsFragment == null || contactsFragment == null || requestFragment == null) {
            Log.e(TAG, "setupBottomNavigation: UI or fragments not initialized properly. Hiding Bottom Navigation.");
            // Hide the bottom nav or show an error state if needed
            if (bottomNavigation != null) bottomNavigation.setVisibility(View.GONE);
            return;
        }
        Log.d(TAG, "Setting up Bottom Navigation.");

        // activeFragment is already determined in onCreate (either initial or restored)

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String tag = null;

            int itemId = item.getItemId();
            // Determine which fragment corresponds to the selected item ID
            if (itemId == R.id.bottom_chat) {
                selectedFragment = chatFragment;
                tag = "chat";
            } else if (itemId == R.id.bottom_groups) { // Assuming this is your Groups tab ID
                selectedFragment = groupFragment;
                tag = "group";
            } else if (itemId == R.id.bottom_find_friends) {
                selectedFragment = findFriendsFragment;
                tag = "find_friends";
            } else if (itemId == R.id.bottom_contacts) {
                selectedFragment = contactsFragment;
                tag = "contacts";
            } else if (itemId == R.id.bottom_requests) { // Assuming this is your Requests tab ID
                selectedFragment = requestFragment;
                tag = "requests";
            } else {
                Log.w(TAG, "Unknown bottom navigation item selected: " + item.getTitle() + " (ID: " + itemId + ")");
                return false; // Indicates the event was not handled
            }

            // Only perform fragment transaction if a different fragment is selected
            if (selectedFragment != null && selectedFragment != activeFragment) {
                // Perform safety checks before transaction
                if (activeFragment == null || selectedFragment == null) {
                    Log.e(TAG, "Fragment switch failed: active or selected fragment is null during transaction setup.");
                    Toast.makeText(this, "Error switching screen.", Toast.LENGTH_SHORT).show();
                    return false; // Event not handled due to error
                }

                // Check if the selected fragment has already been added to the FragmentManager
                if (selectedFragment.isAdded()) {
                    Log.d(TAG, "Switching fragment from " + (activeFragment != null ? activeFragment.getClass().getSimpleName() : "null") + " to: " + selectedFragment.getClass().getSimpleName());

                    // Begin transaction: hide the current fragment and show the selected one
                    fragmentManager.beginTransaction()
                            .hide(activeFragment) // Hide the currently active fragment
                            .show(selectedFragment) // Show the newly selected fragment
                            .commitNow(); // Use commitNow() for synchronous fragment state update

                    activeFragment = selectedFragment; // Update the tracked active fragment

                    // *** REMOVE THE MANUAL BADGE REMOVAL CALLS HERE ***
                    // The Firebase listeners are responsible for updating/removing the badge
                    // based on the actual count in Firebase.
                    // For example, the logic to mark messages as seen/requests as processed
                    // should be in ChatPageActivity or RequestFragment, which updates Firebase.
                    // The MainActivity listeners will then react and update the badge.

                    // --- REMOVED ---
                    // if (itemId == R.id.bottom_chat) {
                    //     bottomNavigation.removeBadge(R.id.bottom_chat);
                    //     Log.d(TAG, "Removed badge locally for Chat tab on selection.");
                    // } else if (itemId == R.id.bottom_groups) { // Groups tab selected (ASSUMPTION)
                    //     bottomNavigation.removeBadge(R.id.bottom_groups); // Remove badge from Groups tab
                    //     Log.d(TAG, "Removed badge locally for Groups tab on selection.");
                    // } else if (itemId == R.id.bottom_requests) { // Requests tab selected (ASSUMPTION)
                    //      bottomNavigation.removeBadge(R.id.bottom_requests);
                    //      Log.d(TAG, "Removed badge locally for Requests tab on selection.");
                    // }
                    // --- END REMOVED ---


                } else {
                    // This case should ideally not happen if fragments are added in onCreate using commitNow
                    Log.e(TAG, "Attempted to show a fragment that is not added to FragmentManager: " + selectedFragment.getClass().getSimpleName());
                    Toast.makeText(this, "Error loading screen: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    return false; // Event not handled due to error
                }
            } else if (selectedFragment == activeFragment) {
                Log.d(TAG, "Clicked on the currently active fragment: " + selectedFragment.getClass().getSimpleName());
                // Optional: If the active fragment is clicked again, you might trigger
                // scrolling to the top of a list or refreshing content within the fragment.
                // Do NOT remove the badge here either. The badge should only clear if
                // the underlying count in Firebase becomes 0.

                // *** REMOVE THE MANUAL BADGE REMOVAL CALLS HERE AS WELL ***
                // --- REMOVED ---
                // if (itemId == R.id.bottom_chat) {
                //    bottomNavigation.removeBadge(R.id.bottom_chat);
                //    Log.d(TAG, "Removed badge locally for Chat tab on re-click.");
                // } else if (itemId == R.id.bottom_groups) { // Groups tab re-clicked (ASSUMPTION)
                //    bottomNavigation.removeBadge(R.id.bottom_groups);
                //    Log.d(TAG, "Removed badge locally for Groups tab on re-click.");
                // } else if (itemId == R.id.bottom_requests) { // Requests tab re-clicked (ASSUMPTION)
                //    bottomNavigation.removeBadge(R.id.bottom_requests);
                //    Log.d(TAG, "Removed badge locally for Requests tab on re-click.");
                // }
                // --- END REMOVED ---

            }

            return true; // Indicates the item selection event was handled
        });

        // --- Set the initial selected item based on the active fragment ---
        // This ensures the correct tab is highlighted when the Activity starts or is restored.
        // Calling setSelectedItemId will also trigger the OnItemSelectedListener above,
        // which will now correctly switch fragments *without* removing the badge.
        if (activeFragment != null) {
            int initialItemId = R.id.bottom_chat; // Default assumption

            // Determine the correct menu item ID based on the currently active fragment instance
            if (activeFragment instanceof GroupFragment) {
                initialItemId = R.id.bottom_groups; // Assuming bottom_groups maps to GroupFragment
            } else if (activeFragment instanceof FindFriendsFragment) {
                initialItemId = R.id.bottom_find_friends;
            } else if (activeFragment instanceof MyContactsFragment) {
                initialItemId = R.id.bottom_contacts;
            } else if (activeFragment instanceof RequestFragment) {
                initialItemId = R.id.bottom_requests;
            } else if (activeFragment instanceof ChatFragment){
                initialItemId = R.id.bottom_chat;
            }
            // Add checks for other fragment types if necessary to determine the initial tab

            Log.d(TAG, "Setting initial selected Bottom Nav item based on active fragment (" + activeFragment.getClass().getSimpleName() + "): " + initialItemId);

            // Find the menu item by ID in the BottomNavigationView's menu
            MenuItem item = bottomNavigation.getMenu().findItem(initialItemId);
            if (item != null) {
                // Set the selected item visually and trigger the listener.
                // The listener will switch fragments but NO LONGER remove the badge.
                bottomNavigation.setSelectedItemId(initialItemId);
            } else {
                // Fallback to selecting a default item if the determined initial item ID is not found in the menu
                Log.e(TAG, "setupBottomNavigation: Initial item ID " + initialItemId + " not found in BottomNavigationView menu! Defaulting to Chat.");
                bottomNavigation.setSelectedItemId(R.id.bottom_chat); // Fallback to the Chat tab
            }
        } else {
            // Default case if activeFragment is somehow null after onCreate Fragment setup
            Log.e(TAG, "setupBottomNavigation: activeFragment is null after onCreate fragment setup. Defaulting to Chat tab.");
            bottomNavigation.setSelectedItemId(R.id.bottom_chat); // Default to the Chat tab
        }
    }


//    private void setupBottomNavigation() {
//        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
//
//        if (bottomNavigation == null || fragmentManager == null || chatFragment == null || groupFragment == null || findFriendsFragment == null || contactsFragment == null || requestFragment == null) {
//            Log.e(TAG, "setupBottomNavigation: UI or fragments not initialized properly.");
//            // Maybe hide the bottom nav or show an error state
//            if (bottomNavigation != null) bottomNavigation.setVisibility(View.GONE);
//            return;
//        }
//        Log.d(TAG, "Setting up Bottom Navigation.");
//
//        // activeFragment is already determined in onCreate (either initial or restored)
//
//        bottomNavigation.setOnItemSelectedListener(item -> {
//            Fragment selectedFragment = null;
//            String tag = null;
//
//            int itemId = item.getItemId();
//            if (itemId == R.id.bottom_chat) {
//                selectedFragment = chatFragment;
//                tag = "chat";
//            } else if (itemId == R.id.bottom_groups) {
//                selectedFragment = groupFragment;
//                tag = "group";
//            } else if (itemId == R.id.bottom_find_friends) {
//                selectedFragment = findFriendsFragment;
//                tag = "find_friends";
//            } else if (itemId == R.id.bottom_contacts) {
//                selectedFragment = contactsFragment;
//                tag = "contacts";
//            } else if (itemId == R.id.bottom_requests) {
//                selectedFragment = requestFragment;
//                tag = "requests";
//            } else {
//                Log.w(TAG, "Unknown bottom navigation item selected: " + item.getTitle());
//                return false; // Did not handle the click
//            }
//
//            if (selectedFragment != null && selectedFragment != activeFragment) {
//                // Check if fragments are null before attempting transaction (shouldn't be if onCreate works)
//                if (activeFragment == null || selectedFragment == null) {
//                    Log.e(TAG, "Fragment switch failed: active or selected fragment is null.");
//                    Toast.makeText(this, "Error switching screen.", Toast.LENGTH_SHORT).show();
//                    return false; // Did not handle due to error
//                }
//
//                if (selectedFragment.isAdded()) {
//                    Log.d(TAG, "Switching fragment from " + activeFragment.getClass().getSimpleName() + " to: " + selectedFragment.getClass().getSimpleName());
//                    fragmentManager.beginTransaction()
//                            .hide(activeFragment)
//                            .show(selectedFragment)
//                            .commitNow(); // Use commitNow() for synchronous switch
//                    activeFragment = selectedFragment; // Update active fragment
//
//                } else {
//                    // This case should ideally not happen if fragments are added in onCreate
//                    Log.e(TAG, "Attempted to show a fragment that is not added: " + selectedFragment.getClass().getSimpleName());
//                    Toast.makeText(this, "Error loading screen: " + item.getTitle(), Toast.LENGTH_SHORT).show();
//                    return false; // Did not handle due to error
//                }
//            } else if (selectedFragment == activeFragment) {
//                Log.d(TAG, "Clicked on the currently active fragment: " + selectedFragment.getClass().getSimpleName());
//                // Optional: If the active fragment is clicked again, you might scroll to the top
//                // of a list or refresh content.
//            }
//
//            // Return true to indicate the item selection was handled
//            return true;
//        });
//
//        // Set the selected item on the bottom navigation to match the *initially* or *restored* active fragment
//        if (activeFragment != null) {
//            int initialItemId = R.id.bottom_chat; // Default
//
//            if (activeFragment == groupFragment) {
//                initialItemId = R.id.bottom_groups;
//            } else if (activeFragment == findFriendsFragment) {
//                initialItemId = R.id.bottom_find_friends;
//            } else if (activeFragment == contactsFragment) {
//                initialItemId = R.id.bottom_contacts;
//            } else if (activeFragment == requestFragment) {
//                initialItemId = R.id.bottom_requests;
//            } else if (activeFragment == chatFragment){
//                initialItemId = R.id.bottom_chat;
//            }
//
//            Log.d(TAG, "Setting initial selected Bottom Nav item based on active fragment: " + initialItemId);
//            // Setting the selected item *after* the listener is set will trigger the listener
//            // and handle the initial display correctly, ensuring only the active fragment is shown.
//            // Check if the item exists in the menu before selecting it.
//            MenuItem item = bottomNavigation.getMenu().findItem(initialItemId);
//            if (item != null) {
//                item.setChecked(true); // This selects the item visually
//                // Note: setSelectedItemId(id) also triggers the listener.
//                // Using setChecked(true) might be sufficient if the fragments were correctly
//                // shown/hidden in onCreate's restoration logic.
//                // Let's stick to setSelectedItemId as it's more explicit about state sync.
//                bottomNavigation.setSelectedItemId(initialItemId); // This triggers the listener again
//            } else {
//                Log.e(TAG, "setupBottomNavigation: Initial item ID " + initialItemId + " not found in menu!");
//                // Fallback to selecting chat if the determined active item is missing
//                bottomNavigation.setSelectedItemId(R.id.bottom_chat);
//            }
//        } else {
//            Log.e(TAG, "setupBottomNavigation: activeFragment is null after onCreate. Defaulting to Chat.");
//            bottomNavigation.setSelectedItemId(R.id.bottom_chat);
//        }
//    }





    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity.");
        Intent loginIntent = new Intent(MainActivity.this, Login.class); // Ensure Login activity exists
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void removeUserData(String userId, ProgressDialog progressDialog, FirebaseUser user) {
        Log.d(TAG, "Removing Firebase user data for: " + userId);

        // Use a Task chain or nested listeners for sequential deletion if dependencies exist,
        // or use batched writes if possible and order doesn't strictly matter for cleanup.
        // The current nested approach is okay but can become complex.

        // Example: Delete user state first
        DatabaseReference userStateRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("userState");
        userStateRef.removeValue().addOnCompleteListener(stateTask -> {
            if (stateTask.isSuccessful()) {
                Log.d(TAG, "Firebase: User state removed.");
            } else {
                Log.w(TAG, "Firebase: Failed to remove user state.", stateTask.getException());
            }

            // Then remove user from others' contacts
            DatabaseReference contactsRootRef = FirebaseDatabase.getInstance().getReference("Contacts");
            contactsRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Deleting from others' contact lists - iterate carefully
                    Map<String, Object> updates = new HashMap<>();
                    int removedCount = 0;
                    for (DataSnapshot userContactListSnapshot : snapshot.getChildren()) {
                        if (userContactListSnapshot.hasChild(userId)) {
                            updates.put(userContactListSnapshot.getKey() + "/" + userId, null); // Set to null to remove
                            removedCount++;
                        }
                    }
                    Log.d(TAG, "Firebase: Preparing to remove user from " + removedCount + " others' contact lists.");

                    if (!updates.isEmpty()) {
                        contactsRootRef.updateChildren(updates)
                                .addOnCompleteListener(contactRemovalTask -> {
                                    if (contactRemovalTask.isSuccessful()) {
                                        Log.d(TAG, "Firebase: Successfully removed user from others' contact lists.");
                                    } else {
                                        Log.w(TAG, "Firebase: Failed to remove user from all contact lists.", contactRemovalTask.getException());
                                        Toast.makeText(MainActivity.this, "Warning: Failed to clean up contacts fully.", Toast.LENGTH_SHORT).show();
                                    }
                                    // Continue to next step regardless of success
                                    removeChatRequests(userId, progressDialog, user);
                                });
                    } else {
                        Log.d(TAG, "Firebase: User not found in any contact lists to remove.");
                        removeChatRequests(userId, progressDialog, user); // Continue even if no contacts found
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase: Failed to fetch Contacts for removal", error.toException());
                    Toast.makeText(MainActivity.this, "Warning: Failed to fetch contacts for cleanup.", Toast.LENGTH_SHORT).show();
                    removeChatRequests(userId, progressDialog, user); // Continue on error
                }
            });
        });
    }





    private void removeChatRequests(String userId, ProgressDialog progressDialog, FirebaseUser user) {
        Log.d(TAG, "Removing Firebase chat requests for: " + userId);
        DatabaseReference chatRequestsRootRef = FirebaseDatabase.getInstance().getReference("Chat Requests");

        Map<String, Object> updates = new HashMap<>();
        // Remove requests sent *by* this user
        updates.put(userId, null); // Remove the node where this user is the sender

        // Find and remove requests sent *to* this user
        chatRequestsRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int receivedRemovedCount = 0;
                for (DataSnapshot senderRequestsSnapshot : snapshot.getChildren()) { // Iterate through senders
                    // Check if this user (the one being deleted) is a receiver under this sender
                    if (senderRequestsSnapshot.hasChild(userId)) {
                        updates.put(senderRequestsSnapshot.getKey() + "/" + userId, null); // Remove the node where this user is the receiver
                        receivedRemovedCount++;
                    }
                }
                Log.d(TAG, "Firebase: Preparing to remove sent/received chat requests (" + (updates.containsKey(userId) ? 1 : 0) + " sent node, " + receivedRemovedCount + " received requests).");

                chatRequestsRootRef.updateChildren(updates)
                        .addOnCompleteListener(requestRemovalTask -> {
                            if (requestRemovalTask.isSuccessful()) {
                                Log.d(TAG, "Firebase: Successfully removed chat requests.");
                            } else {
                                Log.w(TAG, "Firebase: Failed to remove all chat requests.", requestRemovalTask.getException());
                                // Avoid toast here
                            }
                            // Continue to next step regardless of success
                            // Add cleanup for Chat Summaries related to this user
                            removeChatSummariesData(userId, progressDialog, user); // *** CHANGE THIS CALL ***
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase: Failed to fetch Chat Requests for removal", error.toException());
                // Avoid toast here
                // Add cleanup for Chat Summaries even on error
                removeChatSummariesData(userId, progressDialog, user); // *** CHANGE THIS CALL ***
            }
        });
    }

//    private void removeChatRequests(String userId, ProgressDialog progressDialog, FirebaseUser user) {
//        Log.d(TAG, "Removing Firebase chat requests for: " + userId);
//        DatabaseReference chatRequestsRootRef = FirebaseDatabase.getInstance().getReference("Chat Requests");
//
//        // Use a transaction or batched write for requests as well
//        Map<String, Object> updates = new HashMap<>();
//        // Remove requests sent *by* this user
//        updates.put(userId, null); // Remove the node where this user is the sender
//
//        // Find and remove requests sent *to* this user
//        chatRequestsRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int receivedRemovedCount = 0;
//                for (DataSnapshot userRequestsSnapshot : snapshot.getChildren()) {
//                    // Check if this user is the receiver
//                    if (userRequestsSnapshot.hasChild(userId)) {
//                        updates.put(userRequestsSnapshot.getKey() + "/" + userId, null); // Remove the node where this user is the receiver
//                        receivedRemovedCount++;
//                    }
//                }
//                Log.d(TAG, "Firebase: Preparing to remove sent/received chat requests (" + (updates.size() -1) + " sent, " + receivedRemovedCount + " received)."); // -1 for the sent root node
//
//                chatRequestsRootRef.updateChildren(updates)
//                        .addOnCompleteListener(requestRemovalTask -> {
//                            if (requestRemovalTask.isSuccessful()) {
//                                Log.d(TAG, "Firebase: Successfully removed chat requests.");
//                            } else {
//                                Log.w(TAG, "Firebase: Failed to remove all chat requests.", requestRemovalTask.getException());
//                                Toast.makeText(MainActivity.this, "Warning: Failed to clean up chat requests fully.", Toast.LENGTH_SHORT).show();
//                            }
//                            // Continue to next step regardless of success
//                            removeUserMainData(userId, progressDialog, user);
//                        });
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Firebase: Failed to fetch Chat Requests for removal", error.toException());
//                Toast.makeText(MainActivity.this, "Warning: Failed to fetch chat requests for cleanup.", Toast.LENGTH_SHORT).show();
//                removeUserMainData(userId, progressDialog, user); // Continue on error
//            }
//        });
//    }

    private void removeUserMainData(String userId, ProgressDialog progressDialog, FirebaseUser user) {
        Log.d(TAG, "Removing main Firebase user data for: " + userId);
        DatabaseReference userMainDataRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userMainDataRef.removeValue().addOnCompleteListener(userDataTask -> {
            if (userDataTask.isSuccessful()) {
                Log.d(TAG, "Firebase: User main data removed from /Users/" + userId);

                Log.d(TAG, "Clearing all secure local keys for user: " + userId + " during deletion.");
                SecureKeyStorageUtil.clearAllSecureKeys(MainActivity.this, userId);

                YourKeyManager.getInstance().clearKeys();
                Log.d(TAG, "Keys cleared from KeyManager during account deletion.");

                // Final step: Delete Firebase Auth user
                deleteFirebaseUser(user, progressDialog);

            } else {
                progressDialog.dismiss();
                Log.e(TAG, "Firebase: Failed to remove user main data", userDataTask.getException());
                Toast.makeText(MainActivity.this, "Failed to remove user data from server.", Toast.LENGTH_LONG).show();
                sendUserToLoginActivity(); // Redirect even on failure
            }
        });
    }


    private void LoggingOut() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseUser currentUser = auth.getCurrentUser();
                    String currentUserId = (currentUser != null) ? currentUser.getUid() : null;
                    Log.d(TAG, "Logout confirmed. User ID for cleanup: " + (currentUserId != null ? currentUserId : "null"));

                    // Unlink OneSignal External User ID from this device. Use OneSignal.logout().
                    Log.d(TAG, "Logout confirmed. Unlinking OneSignal External User ID (" + currentUserId + ").");
                    if (!TextUtils.isEmpty(currentUserId)) { // Only logout OneSignal if we have a user ID
                        OneSignal.logout(); // Use logout() in v5.x (Ensure OneSignal is initialized)
                        Log.d(TAG, "OneSignal.logout() called.");
                    } else {
                        Log.w(TAG, "Skipping OneSignal.logout(), currentUserId is null.");
                    }


                    // Set user state to offline BEFORE signing out from Firebase Auth (async)
                    // Only attempt if user was authenticated and public key was available when onStop was called.
                    // Or simply check if auth.getCurrentUser() is not null now.
                    if (auth.getCurrentUser() != null && currentUserId != null) { // Check auth user and ID
                        Log.d(TAG, "Setting user state to offline before signing out.");
                        updateUserState("offline"); // Fire and forget (Ensure this method exists and works)
                    } else {
                        Log.w(TAG, "Skipping offline state update on logout, user not authenticated or ID is null.");
                    }

                    // Initiate local Room DB cleanup (runs on background thread)
                    if (currentUserId != null) {
                        Log.d(TAG, "Initiating local Room DB cleanup for user: " + currentUserId + " during logout.");
                        ChatDatabase chatDatabase = ChatDatabase.getInstance(this); // Get DB instance
                        ExecutorService dbExecutor = ChatDatabase.databaseWriteExecutor; // Get shared executor

                        dbExecutor.execute(() -> {
                            try {
                                Log.d(TAG, "Logout DB Cleanup (Executor): Starting local Room DB cleanup for user: " + currentUserId);
                                // Delete user-specific data from Room DB
                                int deletedChats = chatDatabase.chatDao().deleteAllChatsForOwner(currentUserId); // Assuming this method exists
                                Log.d(TAG, "Logout DB Cleanup (Executor): Deleted " + deletedChats + " chat entries from Room.");
                                // Ensure messageDao().deleteAllMessagesForOwner(userId) exists and is called if needed for orphaned messages
                                int deletedMessages = chatDatabase.messageDao().deleteAllMessagesForOwner(currentUserId); // Add if you have this method in MessageDao
                                Log.d(TAG, "Logout DB Cleanup (Executor): Deleted " + deletedMessages + " messages from Room.");

                                int deletedGroups = chatDatabase.groupListDao().deleteAllGroupsForOwner(currentUserId); // Assuming this method exists for groups owned by the user
                                Log.d(TAG, "Logout DB Cleanup (Executor): Deleted " + deletedGroups + " group entries from Room.");

                                // If TemporaryRooms are per-user, delete them too
                                int deletedTempRooms = chatDatabase.groupListDao().deleteAllTemporaryRoomsForOwner(currentUserId); // Assuming you have this DAO method
                                Log.d(TAG, "Logout DB Cleanup (Executor): Deleted " + deletedTempRooms + " temporary room entries from Room.");

//                                // --- *** NEW ADDITION START (MODIFIED to delete ALL key versions) *** ---
//                                // Delete ALL conversation key versions for this user from Room DB
//                                Log.d(TAG, "Logout DB Cleanup (Executor): Deleting ALL conversation key versions for owner: " + currentUserId);
//                                int deletedKeyRows = chatDatabase.conversationKeyDao().deleteAllKeysForOwner(currentUserId); // <<< USE THE CORRECT DAO METHOD
//                                Log.d(TAG, "Logout DB Cleanup (Executor): Deleted " + deletedKeyRows + " conversation key versions from Room.");
//                                // --- *** NEW ADDITION END ***

                                Log.d(TAG, "Logout DB Cleanup (Executor): Completed local Room DB cleanup for user: " + currentUserId);
                            } catch (Exception e) {
                                Log.e(TAG, "Logout DB Cleanup (Executor): Error performing local Room DB cleanup during logout", e);
                                mainHandler.post(() -> Toast.makeText(MainActivity.this, "Warning: Local data cleanup failed.", Toast.LENGTH_SHORT).show()); // Post toast to main thread
                            }
                            Log.d(TAG, "Logout DB Cleanup (Executor): Task FINISHED.");

                            // --- Proceed with Firebase sign-out and subsequent cleanup on main thread after Room is done ---
                            mainHandler.post(() -> {
                                Log.d(TAG, "Firebase Auth sign out initiated after Room cleanup.");
                                auth.signOut(); // Sign out from Firebase Auth

                                // Sign out from Google if applicable and chain cleanup
                                GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                                        .addOnCompleteListener(googleTask -> {
                                            Log.d(TAG, "Google SignOut task completed.");
                                            // --- *** CLEAR KEYS AND REDIRECT HERE *** ---
                                            // This is where the final local key cleanup and redirect happens.
                                            performFinalLogoutCleanupAndRedirect(currentUserId); // Call helper method
                                        }).addOnFailureListener(googleTask -> {
                                            Log.e(TAG, "Google SignOut task failed.", googleTask.getCause());
                                            // --- *** CLEAR KEYS AND REDIRECT HERE (in failure listener) *** ---
                                            // Perform final cleanup and redirect even if Google sign out fails.
                                            performFinalLogoutCleanupAndRedirect(currentUserId); // Call helper method
                                        });
                            });
                        });
                        Log.d(TAG, "Logout DB Cleanup: Task submitted to executor.");
                    } else {
                        // If user ID is null (shouldn't happen if LoggingOut is called correctly), proceed to clear global state and redirect immediately
                        Log.w(TAG, "No current user found during logout, skipping user-specific local data cleanup. Proceeding with global state clear and redirection.");
                        performFinalLogoutCleanupAndRedirect(null); // Pass null userId for general cleanup
                        // Exit this lambda as we've initiated the next step
                        return;
                    }

                    // *** REMOVE ANY redundant clearKeys() or SecureKeyStorageUtil.clearAllSecureKeys() CALLS FROM HERE ***
                    // The final cleanup is now handled by performFinalLogoutCleanupAndRedirect, which is called AFTER Room cleanup OR immediately if user ID is null.

                })
                .setNegativeButton("No", null)
                .show();
    }

    // --- NEW Helper method for final cleanup after sign outs (Remains the same) ---
    private void performFinalLogoutCleanupAndRedirect(String userId) {
        Log.d(TAG, "performFinalLogoutCleanupAndRedirect: Starting final cleanup for user: " + userId);

        // Clear all secure local keys (symmetric key and RSA pair file)
        if (userId != null) {
            Log.d(TAG, "performFinalLogoutCleanupAndRedirect: Clearing all secure local keys for user: " + userId);
            SecureKeyStorageUtil.clearAllSecureKeys(this, userId); // Use application context or activity context here
        } else {
            Log.w(TAG, "performFinalLogoutCleanupAndRedirect: User ID was null during local key clear. Attempting symmetric key clear only.");
            SecureKeyStorageUtil.clearSymmetricKey(this); // Use application context or activity context here
        }

        // Clear keys from the in-memory KeyManager
        YourKeyManager.getInstance().clearKeys(); // This method clears ALL cached conversation keys
        Log.d(TAG, "performFinalLogoutCleanupAndRedirect: ALL keys cleared from KeyManager.");

        // Redirect to Login activity and clear the back stack
        Log.d(TAG, "performFinalLogoutCleanupAndRedirect: Redirecting to Login activity.");
        sendUserToLoginActivity(); // Use the helper method
    }


    // --- Modified deleteAccount method (MODIFIED) ---
    // In MainActivity.java, locate the deleteAccount() method.
    // Modify the databaseWriteExecutor.execute block inside it:

    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Attempted to delete account but no user logged in.");
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting Account...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String userId = user.getUid();
        Log.d(TAG, "Initiating account deletion for user: " + userId);

        // Unlink OneSignal External User ID when deleting the account. Use OneSignal.logout().
        Log.d(TAG, "Deleting account. Unlinking OneSignal External User ID (" + userId + ").");
        OneSignal.logout(); // Use logout() in v5.x (Ensure OneSignal is initialized)
        Log.d(TAG, "OneSignal.logout() called during account deletion.");


        ChatDatabase chatDatabase = ChatDatabase.getInstance(this); // Get DB instance
        ExecutorService dbExecutor = ChatDatabase.databaseWriteExecutor; // Get shared executor

        dbExecutor.execute(() -> { // Run local DB cleanup on background thread
            try {
                Log.d(TAG, "Starting local Room DB cleanup for user: " + userId + " during deletion.");
                // Delete user-specific data from Room DB
                int deletedChats = chatDatabase.chatDao().deleteAllChatsForOwner(userId); // Assuming this method exists
                Log.d(TAG, "Deleted " + deletedChats + " chat entries from Room.");
                // Ensure messageDao().deleteAllMessagesForOwner(userId) exists and is called if needed for orphaned messages
                int deletedMessages = chatDatabase.messageDao().deleteAllMessagesForOwner(userId); // Add if you have this
                Log.d(TAG, "Deleted " + deletedMessages + " messages from Room.");

                int deletedGroups = chatDatabase.groupListDao().deleteAllGroupsForOwner(userId); // Assuming this method exists for groups owned by the user
                Log.d(TAG, "Deleted " + deletedGroups + " group entries from Room.");

                int deletedTempRooms = chatDatabase.groupListDao().deleteAllTemporaryRoomsForOwner(userId); // Assuming you have this DAO method
                Log.d(TAG, "Deleted " + deletedTempRooms + " temporary room entries from Room.");

                // --- *** NEW ADDITION START (MODIFIED to delete ALL key versions) *** ---
                // Delete ALL conversation key versions for this user from Room
                Log.d(TAG, "Deleting ALL conversation key versions for owner: " + userId + " during deletion cleanup.");
                int deletedKeyRows = chatDatabase.conversationKeyDao().deleteAllKeysForOwner(userId); // <<< USE THE CORRECT DAO METHOD
                Log.d(TAG, "Deleted " + deletedKeyRows + " conversation key versions from Room.");
                // --- *** NEW ADDITION END ***

                Log.d(TAG, "Completed local Room DB cleanup for user: " + userId + " during deletion.");

                // --- Proceed to remove data from Firebase on the main thread after local cleanup ---
                mainHandler.post(() -> { // Post to main thread
                    // Pass the user ID and progress dialog, and the FirebaseUser
                    removeUserData(userId, progressDialog, user); // Start Firebase data removal chain
                });

            } catch (Exception e) {
                Log.e(TAG, "Error performing local Room DB cleanup for user: " + userId + " during deletion.", e);
                mainHandler.post(() -> { // Post toast to main thread
                    Toast.makeText(MainActivity.this, "Warning: Local data cleanup failed.", Toast.LENGTH_SHORT).show();
                    // Proceed to remove data from Firebase even if local cleanup failed
                    removeUserData(userId, progressDialog, user); // Start Firebase data removal chain
                });
            }
        });
    }

    // ... (rest of MainActivity.java) ...


// --- Modified deleteAccount method ---
    // In MainActivity.java, locate the deleteAccount() method.
// Modify the databaseWriteExecutor.execute block inside it:





    private void deleteFirebaseUser(FirebaseUser user, ProgressDialog progressDialog) {
        Log.d(TAG, "Deleting Firebase Auth user: " + user.getUid());
        user.delete().addOnCompleteListener(deleteTask -> {
            progressDialog.dismiss();
            if (deleteTask.isSuccessful()) {
                Log.d(TAG, "Firebase Auth user deleted successfully.");
                Toast.makeText(MainActivity.this, "Account deleted successfully", Toast.LENGTH_LONG).show();
                // Also sign out from Google if applicable
                GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                        .addOnCompleteListener(googleTask -> {
                            Log.d(TAG, "Google SignOut completed after account deletion.");
                            sendUserToLoginActivity(); // Redirect after successful deletion/sign-out
                        })
                        .addOnFailureListener(googleTask -> {
                            Log.e(TAG, "Google SignOut failed after account deletion.", googleTask.getCause());
                            sendUserToLoginActivity(); // Redirect even if Google sign out fails
                        });
            } else {
                Log.e(TAG, "Failed to delete Firebase Auth user. Requires re-authentication?", deleteTask.getException());
                Toast.makeText(MainActivity.this, "Failed to delete account from Firebase Auth. Please try logging in again and delete.", Toast.LENGTH_LONG).show();
                sendUserToLoginActivity(); // Redirect on Firebase Auth deletion failure
            }
        });

        // FirebaseAuth.getInstance().getCurrentUser().delete(); // <<< REMOVE THIS DUPLICATE LINE
    }


//    private void deleteFirebaseUser(FirebaseUser user, ProgressDialog progressDialog) {
//        Log.d(TAG, "Deleting Firebase Auth user: " + user.getUid());
//        user.delete().addOnCompleteListener(deleteTask -> {
//            progressDialog.dismiss();
//            if (deleteTask.isSuccessful()) {
//                Log.d(TAG, "Firebase Auth user deleted successfully.");
//                Toast.makeText(MainActivity.this, "Account deleted successfully", Toast.LENGTH_LONG).show();
//                // Also sign out from Google if applicable
//                GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
//                        .addOnCompleteListener(googleTask -> {
//                            Log.d(TAG, "Google SignOut completed after account deletion.");
//                            sendUserToLoginActivity(); // Redirect after successful deletion/sign-out
//                        })
//                        .addOnFailureListener(googleTask -> {
//                            Log.e(TAG, "Google SignOut failed after account deletion.", googleTask.getCause());
//                            sendUserToLoginActivity(); // Redirect even if Google sign out fails
//                        });
//            } else {
//                Log.e(TAG, "Failed to delete Firebase Auth user. Requires re-authentication?", deleteTask.getException());
//                Toast.makeText(MainActivity.this, "Failed to delete account from Firebase Auth. Please try logging in again and delete.", Toast.LENGTH_LONG).show();
//                sendUserToLoginActivity(); // Redirect on Firebase Auth deletion failure
//            }
//        });
//
//        FirebaseAuth.getInstance().getCurrentUser().delete();
//    }


    private void updateUserState(String state) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || currentUserID == null) {
            Log.w(TAG, "updateUserState called with no authenticated user or user ID.");
            return;
        }

        DatabaseReference userStateRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(currentUserID).child("userState");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        Date now = Calendar.getInstance().getTime();
        String currentDate = dateFormat.format(now);
        String currentTime = timeFormat.format(now);

        Map<String, Object> stateMap = new HashMap<>();
        stateMap.put("state", state);
        stateMap.put("date", currentDate);
        stateMap.put("time", currentTime);

        Log.d(TAG, "Attempting to set user state to: " + state + " for UID: " + currentUserID);

        userStateRef.setValue(stateMap)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Failed to set user state to: " + state, task.getException());
                    } else {
                        Log.d(TAG, "User state set to: " + state);
                    }
                });

        // Set onDisconnect for online state only
        if ("online".equals(state)) {
            // This listener will be triggered when the user disconnects.
            // Use ServerValue.TIMESTAMP for accurate disconnect time if needed,
            // but your current date/time format is also okay if consistency is needed.
            Map<String, Object> offlineStateMap = new HashMap<>();
            offlineStateMap.put("state", "offline");
            // Capture time at the moment onDisconnect is SET, or use ServerValue.TIMESTAMP
            offlineStateMap.put("date", dateFormat.format(Calendar.getInstance().getTime()));
            offlineStateMap.put("time", timeFormat.format(Calendar.getInstance().getTime()));


            userStateRef.onDisconnect().setValue(offlineStateMap).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Failed to set onDisconnect listener for user state.", task.getException());
                } else {
                    Log.d(TAG, "onDisconnect listener set for user state.");
                }
            });
        } else {
            // If setting state to offline manually, cancel any previous onDisconnect
            userStateRef.onDisconnect().cancel();
            Log.d(TAG, "onDisconnect listener cancelled for user state.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity onDestroy called.");

        // Shutdown executor gracefully
        if (keyLoadExecutor != null) {
            keyLoadExecutor.shutdown();
            Log.d(TAG, "keyLoadExecutor shutdown called.");
            // Consider awaitTermination if you need to ensure tasks complete
        }

        // Dismiss dialog if it's showing to prevent window leaks
        if (keyLoadProgressDialog != null && keyLoadProgressDialog.isShowing()) {
            keyLoadProgressDialog.dismiss();
        }

        // Set offline state if user was logged in and keys were available when activity is finishing
        // isFinishing() check ensures this isn't called on simple configuration changes
        if (isFinishing() && auth.getCurrentUser() != null && YourKeyManager.getInstance().getUserPublicKey() != null) {
            Log.d(TAG, "onDestroy: Activity is finishing. User is logged in and keys available. Setting state to offline.");
            // Note: update state might be unreliable in onDestroy, especially if app process is killed abruptly.
            // onDisconnect is more reliable for sudden exits. But good practice to set it here too.
            updateUserState("offline");
        } else {
            Log.d(TAG, "onDestroy: Activity is not finishing or user not fully logged in/keys not loaded. Skipping offline status update.");
        }
    }




    // In MainActivity.java
    // ... (other methods) ...

    private void loadAllConversationKeysFromRoom(String ownerUserId) {
        // Ensure ConversationKeyDao and userId are available
        // Use the member variable 'conversationKeyDao' which is initialized in onCreate
        if (conversationKeyDao == null || TextUtils.isEmpty(ownerUserId)) {
            Log.e(TAG, "loadAllConversationKeysFromRoom (Main): DAO or userId is null. Cannot load conversation keys.");
            return;
        }

        Log.d(TAG, "loadAllConversationKeysFromRoom (Main): Task SUBMITTED to executor for owner: " + ownerUserId);
        // Log KeyManager state BEFORE starting the DB task (for debugging flow)
        // Using getTotalCachedConversationKeyVersions which you added in Step 1

        // Use the shared DB executor from ChatDatabase
        // It's crucial that this runs on a background thread
        ChatDatabase.databaseWriteExecutor.execute(() -> { // Ensure ChatDatabase.databaseWriteExecutor is static public final
            Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Task STARTED on background thread for owner: " + ownerUserId);
            // Log KeyManager state AFTER task starts (confirm private key is still there)

            List<ConversationKeyEntity> keyEntities = null;
            try {
                // *** MODIFIED: Use getAllKeys() which now includes timestamp in the Entity ***
                // This query should fetch ALL key versions for this owner from Room
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Attempting get ALL keys from DAO for owner: " + ownerUserId);
                keyEntities = conversationKeyDao.getAllKeys(ownerUserId); // Call the DAO query
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): DAO query finished. Result size: " + (keyEntities != null ? keyEntities.size() : "null"));
                // *** END MODIFIED ***

            } catch (Exception e) { // Catch any exception during DAO query
                Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Error fetching keys from DAO for owner: " + ownerUserId, e);
                // Don't return, proceed with null list and handle errors during processing if list is not null
            }


            // --- Process keys if found ---
            if (keyEntities != null && !keyEntities.isEmpty()) {
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Processing " + keyEntities.size() + " conversation key versions found.");
                YourKeyManager keyManager = YourKeyManager.getInstance(); // Get KeyManager instance once (it's a singleton)

                // Ensure private key is STILL available before loading conversation keys into memory
                // Loading conversation keys requires the user's account to be unlocked (private key available).
                // If the private key disappeared from KeyManager mid-process (shouldn't happen in correct flow), stop loading conversation keys.
                if (!keyManager.isPrivateKeyAvailable()) {
                    Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Private key became unavailable during conversation key load process! Aborting load.");
                    // Optionally clear the conversation keys from KeyManager and Room if state is inconsistent
                    // keyManager.clearKeys(); // Clear everything from KeyManager (will also clear conv keys)
                    // try { conversationKeyDao.deleteAllKeysForOwner(ownerUserId); } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Error deleting keys after private key disappeared", deleteEx); }
                    return; // STOP processing if private key is missing
                }


                for (ConversationKeyEntity keyEntity : keyEntities) {
                    if (keyEntity == null) {
                        Log.w(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Skipping null keyEntity from Room list.");
                        continue; // Skip null entries
                    }
                    String convId = keyEntity.getConversationId();
                    String decryptedKeyBase64 = keyEntity.getDecryptedKeyBase64();
                    String ownerIdFromEntity = keyEntity.getOwnerUserId();
                    long keyTimestamp = keyEntity.getKeyTimestamp(); // *** GET THE TIMESTAMP ***


                    // Basic validation
                    // Ensure convId, decryptedKeyBase64, ownerIdFromEntity are not empty/null
                    // Ensure keyTimestamp is valid (e.g., > 0 if using Firebase timestamps)
                    if (TextUtils.isEmpty(convId) || TextUtils.isEmpty(decryptedKeyBase64) || TextUtils.isEmpty(ownerIdFromEntity) || keyTimestamp <= 0) { // *** ADD timestamp check ***
                        Log.w(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Skipping key with empty/null essential fields or invalid timestamp from Room. Conv ID: " + convId + ", Owner (in Entity): " + ownerIdFromEntity + ", Timestamp: " + keyTimestamp);
                        // Optional: you might want to delete corrupt entries from Room here
                        try {
                            // Ensure DAO is available
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity) && keyTimestamp > 0) {
                                // Use the delete method that includes timestamp now!
                                conversationKeyDao.deleteKeyForConversation(ownerIdFromEntity, convId); // <<< CORRECTED: Use deleteKeyForConversation and remove keyTimestamp// *** MODIFIED DELETE CALL ***
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Deleted corrupt key from Room DB. Conv ID: " + convId + ", Timestamp: " + keyTimestamp);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Error deleting corrupt key from Room", deleteEx); }
                        continue;
                    }

                    // Important safety check: Ensure the key belongs to the current user
                    if (!ownerIdFromEntity.equals(ownerUserId)) {
                        // This indicates a potential data issue where a key for a different user is in this user's DB
                        Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): BUG? Loaded key from Room for owner " + ownerIdFromEntity + " but expecting owner " + ownerUserId + ". Skipping and deleting unexpected entry.");
                        // Delete this unexpected entry from Room DB
                        try {
                            // Ensure DAO is available
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity) && keyTimestamp > 0) {
                                // Use the delete method that includes timestamp now!
                                conversationKeyDao.deleteKeyForConversation(ownerIdFromEntity, convId); // <<< CORRECTED: Use deleteKeyForConversation and remove keyTimestamp // *** MODIFIED DELETE CALL ***
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Deleted unexpected key from Room DB. Conv ID: " + convId + ", Timestamp: " + keyTimestamp);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Error deleting unexpected key from Room", deleteEx); }
                        continue;
                    }


                    // Convert Base64 back to byte array and then to SecretKey
                    try {
                        Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Processing key version for conv ID: " + convId + ", Timestamp: " + keyTimestamp + ", Owner: " + ownerIdFromEntity);
                        // Use android.util.Base64 with DEFAULT flag for decoding (assuming Room stored it this way)
                        byte[] decryptedKeyBytes = android.util.Base64.decode(decryptedKeyBase64, android.util.Base64.DEFAULT); // Use android.util.Base64
                        // Check if decoded bytes are valid before converting to SecretKey
                        if (decryptedKeyBytes == null || decryptedKeyBytes.length == 0) {
                            Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Decoded key bytes are null or empty for conv ID: " + convId + ", Timestamp: " + keyTimestamp);
                            throw new IllegalArgumentException("Invalid decrypted key data bytes."); // Throw to catch below
                        }
                        SecretKey conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedKeyBytes); // Use your CryptoUtils method to convert bytes to SecretKey

                        // --- Store the decrypted SecretKey in the in-memory KeyManager cache (Use the new method) ---
                        keyManager.setConversationKey(convId, conversationAESKey); // *** MODIFIED KEYMANAGER CALL ***
                        // Log that a key was loaded (can be very verbose)


                    } catch (IllegalArgumentException e) { // Base64 decoding error or invalid bytes format for SecretKey
                        Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Error decoding Base64 or converting to SecretKey from Room for conv ID: " + convId + ", Timestamp: " + keyTimestamp + ". Deleting corrupt entry.", e);
                        // Delete corrupt entry from Room
                        try {
                            // Ensure DAO is available
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity) && keyTimestamp > 0) {
                                // Use the delete method that includes timestamp now!
                                conversationKeyDao.deleteKeyForConversation(ownerIdFromEntity, convId); // <<< CORRECTED: Use deleteKeyForConversation and remove keyTimestamp // *** MODIFIED DELETE CALL ***
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Deleted corrupt key from Room DB. Conv ID: " + convId + ", Timestamp: " + keyTimestamp);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Error deleting corrupt key from Room", deleteEx); }
                    } catch (Exception e) { // Catch any other unexpected errors during conversion
                        Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Unexpected error processing conversation key from Room for conv ID: " + convId + ", Timestamp: " + keyTimestamp, e);
                        // Decide if you delete on other errors - generally safer to delete corrupt data
                        try {
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity) && keyTimestamp > 0) {
                                // Use the delete method that includes timestamp now!
                                conversationKeyDao.deleteKeyForConversation(ownerIdFromEntity, convId); // <<< CORRECTED: Use deleteKeyForConversation and remove keyTimestamp // *** MODIFIED DELETE CALL ***
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Deleted key from Room DB after processing error. Conv ID: " + convId + ", Timestamp: " + keyTimestamp);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Error deleting key from Room after processing error", deleteEx); }
                    }
                }

            } else {
                // No keys found in Room for this user, which is fine (maybe first time using encrypted chat or Room was cleared)
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): No conversation key versions found in Room DB for owner: " + ownerUserId);
            }
            Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Task FINISHED on background thread.");


            // --- Post a runnable back to the main thread to signal that keys are loaded. ---
            // This runnable will explicitly trigger a UI refresh if the relevant fragments/activities are visible.
            mainHandler.post(() -> { // Post to the main thread using the mainHandler
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Main): Posted completion signal to Main Thread.");

                // Trigger refresh in the currently active Fragment if it's ChatFragment or ContactsFragment
                // These fragments display lists that need decryption previews or key availability checks.
                // Get the FragmentManager from the Activity
                FragmentManager fragmentManager = getSupportFragmentManager(); // Assuming this method is available or use getFragmentManager() / requireActivity().getSupportFragmentManager()
                if (fragmentManager != null) {
                    // Find the currently active fragment by tag or ID. You need a way to track this in MainActivity.
                    // Assuming 'activeFragment' member variable exists and is kept up-to-date.
                    Fragment currentActiveFragment = fragmentManager.findFragmentById(R.id.container); // Assuming R.id.container is where fragments are added

                    if (currentActiveFragment != null) {
                        Log.d(TAG, "loadAllConversationKeysFromRoom (Main Thread): Signalling active fragment (" + currentActiveFragment.getClass().getSimpleName() + ") to refresh.");
                        // You need a way for the fragment to refresh its display.
                        // Call forceRefreshDisplay() on the active fragment if it's one of the relevant types.
                        // Make sure ChatFragment and MyContactsFragment have a public forceRefreshDisplay() method.
                        if (currentActiveFragment instanceof ChatFragment) {
                            ((ChatFragment) currentActiveFragment).forceRefreshDisplay(); // Assuming this method exists
                            Log.d(TAG, "Called forceRefreshDisplay on ChatFragment.");
                        } else if (currentActiveFragment instanceof MyContactsFragment) {
                            ((MyContactsFragment) currentActiveFragment).forceRefreshDisplay(); // Assuming this method exists
                            Log.d(TAG, "Called forceRefreshDisplay on MyContactsFragment.");
                        }
                        // Add similar checks for other fragments that display message previews (like GroupFragment)
                        // if (currentActiveFragment instanceof GroupFragment) { ((GroupFragment) currentActiveFragment).forceRefreshDisplay(); } // Example
                    } else {
                        Log.w(TAG, "loadAllConversationKeysFromRoom (Main Thread): Active fragment in R.id.container is null.");
                    }
                } else {
                    Log.w(TAG, "loadAllConversationKeysFromRoom (Main Thread): FragmentManager is null. Cannot signal fragment refresh.");
                }


                // --- Handle ChatPageActivity Refresh (More complex) ---
                // ChatPageActivity is a separate Activity, not managed by MainActivity's FragmentManager directly.
                // If ChatPageActivity is the currently *foreground* activity, it also needs to be told to refresh.
                // The simplest way to handle this is to rely on ChatPageActivity's LiveData observer
                // triggering when its data is updated, OR by it re-checking key availability
                // whenever it comes to the foreground (e.g., in onResume).
                // The LiveData observer approach is usually sufficient if messages are flowing.
                // If the user unlocks keys *while* in a ChatPageActivity without new messages arriving,
                // the LiveData might not trigger.
                // One way to signal the foreground activity is using a LocalBroadcastManager or EventBus.
                // For now, let's add a log indicating that ChatPageActivity might need to refresh.
                // Its own LiveData observer *should* handle it when data changes, but it might need resume.
                Log.d(TAG, "loadAllConversationKeysFromRoom (Main Thread): Conversation keys loaded. ChatPageActivity might need to re-check keys on resume or data update.");

            });
            // --- End Post result ---
        });
    }
    // ... (Rest of the MainActivity.java class remains the same) ...



// ... (Keep your existing methods) ...

    // Helper method to update a specific badge on the BottomNavigationView
    private void updateBadge(int itemId, int count) {
        // Ensure BottomNavigationView is initialized and has items
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation == null || bottomNavigation.getMenu().findItem(itemId) == null) {
            // Log.w(TAG, "updateBadge: BottomNavigationView or menu item " + itemId + " not found."); // Use w for warning
            return; // Silently return if view/item is not ready
        }

        // Run on the UI thread
        runOnUiThread(() -> {
            if (count > 0) {
                // Show badge with count
                BadgeDrawable badgeDrawable = bottomNavigation.getOrCreateBadge(itemId);
                badgeDrawable.setNumber(count);
                badgeDrawable.setVisible(true);

                // Optional: Customize badge appearance if needed (color, size, etc.)
                // badgeDrawable.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
                Log.d(TAG, "Badge updated for item " + getResources().getResourceEntryName(itemId) + " with count: " + count);
            } else {
                // Hide/Remove badge if count is 0 or less
                bottomNavigation.removeBadge(itemId);
                Log.d(TAG, "Badge removed for item " + getResources().getResourceEntryName(itemId) + " (count is 0).");
            }
        });
    }



    // --- NEW: Implement notification badge listeners ---

    // Method to attach all Firebase listeners for notification badges
    private void attachNotificationListeners() {
        // Ensure RootRef and currentUserID are available
        if (RootRef == null || auth.getCurrentUser() == null) {
            Log.w(TAG, "attachNotificationListeners: RootRef or Firebase User is null. Cannot attach listeners.");
            return;
        }
        String currentUserID = auth.getCurrentUser().getUid();
        if (TextUtils.isEmpty(currentUserID)) {
            Log.w(TAG, "attachNotificationListeners: currentUserID is empty after getting from Auth. Cannot attach listeners.");
            return;
        }
        Log.d(TAG, "Attaching notification listeners for user: " + currentUserID);

        // --- Listener for Total Unread Private Chats (from Chat Summaries/{currentUserID}) ---
        // Keep this block, but modify to call updatePrivateChatBadge()
        if (chatGroupBadgeListener == null) {
            DatabaseReference currentUserSummariesRef = RootRef.child("ChatSummaries").child(currentUserID); // Correct path

            chatGroupBadgeListener = currentUserSummariesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Chat/Group (Private) Badge Listener: onDataChange triggered for user summaries node. Snapshot has " + snapshot.getChildrenCount() + " children (partner summaries).");
                    int currentPrivateUnreadCount = 0; // Calculate count for this listener's type
                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

                    if (!TextUtils.isEmpty(userId) && snapshot.exists()) {
                        for (DataSnapshot partnerSummarySnapshot : snapshot.getChildren()) {
                            Integer unreadCount = null;
                            if (partnerSummarySnapshot.hasChild("unreadCounts") && partnerSummarySnapshot.child("unreadCounts").hasChild(userId)) {
                                unreadCount = partnerSummarySnapshot.child("unreadCounts").child(userId).getValue(Integer.class);
                            }

                            if (unreadCount != null && unreadCount > 0) {
                                currentPrivateUnreadCount ++; // Summing messages for private chats
                            }
                        }
                    } else if (TextUtils.isEmpty(userId)) {
                        Log.e(TAG, "Chat/Group (Private) Badge Listener: User ID is null. Cannot calculate unread.");
                    } else {
                        Log.d(TAG, "Chat/Group (Private) Badge Listener: No chat summaries found.");
                    }

                    // Update the private chat count state variable
                    unreadPrivateChatCountForBadge = currentPrivateUnreadCount;
                    Log.d(TAG, "Chat/Group (Private) Badge Listener: Calculated total unread messages: " + unreadPrivateChatCountForBadge);

                    // *** Call the method to update the Private Chat badge ***
                    updatePrivateChatBadge();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Chat/Group (Private) Badge Listener: Cancelled.", error.toException());
                    // On error, reset this specific count to 0 and update the badge
                    unreadPrivateChatCountForBadge = 0;
                    updatePrivateChatBadge(); // Update private chat badge
                }
            });
            Log.d(TAG, "Chat/Group (Private) Badge Listener attached to /Chat Summaries/" + currentUserID);
        }

        // --- Listener for Unread Groups (from /Groups) ---
        // NEW BLOCK
        if (groupsBadgeListener == null) {
            DatabaseReference groupsRootRef = RootRef.child("Groups");

            groupsBadgeListener = groupsRootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Groups Badge Listener: onDataChange triggered. Snapshot has " + snapshot.getChildrenCount() + " children (groups).");
                    int unreadGroupCount = 0; // Calculate count for this listener's type
                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

                    if (!TextUtils.isEmpty(userId) && snapshot.exists()) {
                        for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                            boolean isMember = groupSnapshot.child("members").hasChild(userId);

                            if (isMember) {
                                boolean groupHasUnread = false;
                                DataSnapshot messagesSnapshot = groupSnapshot.child("Messages"); // Assuming Group messages are under /Groups/{groupId}/Messages

                                if (messagesSnapshot.exists() && messagesSnapshot.hasChildren()) {
                                    for (DataSnapshot messageSnap : messagesSnapshot.getChildren()) {
                                        DataSnapshot readBySnapshot = messageSnap.child("readBy");
                                        // Message is unread for this user if 'readBy/userId' is missing or false
                                        // Note: Checking for boolean FALSE is important if you explicitly set false initially
                                        Object readStatus = readBySnapshot.child(userId).getValue();
                                        if (readStatus == null || (readStatus instanceof Boolean && !(Boolean)readStatus)) {
                                            groupHasUnread = true;
                                            break; // Found at least one unread message, the group is unread
                                        }
                                    }
                                }
                                // Check if group has any unread messages for this user
                                if (groupHasUnread) {
                                    unreadGroupCount++; // Count this group as unread
                                }
                            }
                        }
                    } else if (TextUtils.isEmpty(userId)) {
                        Log.e(TAG, "Groups Badge Listener: User ID is null. Cannot calculate unread.");
                    } else {
                        Log.d(TAG, "Groups Badge Listener: No groups found.");
                    }

                    // Update the group count state variable
                    unreadGroupCountForBadge = unreadGroupCount;
                    Log.d(TAG, "Groups Badge Listener: Calculated unread groups count: " + unreadGroupCountForBadge);

                    // *** Call the method to update the Groups Tab badge ***
                    updateGroupsTabBadge();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Groups Badge Listener: Cancelled.", error.toException());
                    // On error, reset this specific count to 0 and update the total badge
                    unreadGroupCountForBadge = 0;
                    updateGroupsTabBadge(); // Update Groups tab badge
                }
            });
            Log.d(TAG, "Groups Badge Listener attached to /Groups.");
        }


        // --- Listener for Unread Temporary Rooms (from /temporaryChatRooms) ---
        // NEW BLOCK
        if (tempRoomsBadgeListener == null) {
            DatabaseReference tempRoomsRootRef = RootRef.child("temporaryChatRooms"); // Assuming this is the path

            tempRoomsBadgeListener = tempRoomsRootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Temporary Rooms Badge Listener: onDataChange triggered. Snapshot has " + snapshot.getChildrenCount() + " children (rooms).");
                    int unreadTemporaryRoomCount = 0; // Calculate count for this listener's type
                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                    long currentTime = System.currentTimeMillis(); // Get current time once

                    if (!TextUtils.isEmpty(userId) && snapshot.exists()) {
                        for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                            boolean isMember = roomSnapshot.child("members").hasChild(userId);
                            Long expiryTimeMillis = roomSnapshot.child("expiryTime").getValue(Long.class);
                            // Check if room is NOT expired
                            boolean isNotExpired = (expiryTimeMillis == null || currentTime <= expiryTimeMillis);


                            if (isMember && isNotExpired) { // Only count if member and not expired
                                boolean roomHasUnread = false;
                                DataSnapshot messagesSnapshot = roomSnapshot.child("messages"); // Assuming Room messages are under /temporaryChatRooms/{roomId}/messages

                                if (messagesSnapshot.exists() && messagesSnapshot.hasChildren()) {
                                    for (DataSnapshot messageSnap : messagesSnapshot.getChildren()) {
                                        DataSnapshot readBySnapshot = messageSnap.child("readBy");
                                        // Message is unread for this user if 'readBy/userId' is missing or false
                                        Object readStatus = readBySnapshot.child(userId).getValue();
                                        if (readStatus == null || (readStatus instanceof Boolean && !(Boolean)readStatus)) {
                                            roomHasUnread = true;
                                            break; // Found at least one unread message, the room is unread
                                        }
                                    }
                                }
                                // Check if room has any unread messages for this user
                                if (roomHasUnread) {
                                    unreadTemporaryRoomCount++; // Count this room as unread
                                }
                            }
                        }
                    } else if (TextUtils.isEmpty(userId)) {
                        Log.e(TAG, "Temporary Rooms Badge Listener: User ID is null. Cannot calculate unread.");
                    } else {
                        Log.d(TAG, "Temporary Rooms Badge Listener: No temporary rooms found.");
                    }

                    // Update the temporary room count state variable
                    unreadTemporaryRoomCountForBadge = unreadTemporaryRoomCount;
                    Log.d(TAG, "Temporary Rooms Badge Listener: Calculated unread rooms count: " + unreadTemporaryRoomCountForBadge);


                    // *** Call the method to update the Groups Tab badge ***
                    updateGroupsTabBadge();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Temporary Rooms Badge Listener: Cancelled.", error.toException());
                    // On error, reset this specific count to 0 and update the total badge
                    unreadTemporaryRoomCountForBadge = 0;
                    updateGroupsTabBadge(); // Update Groups tab badge
                }
            });
            Log.d(TAG, "Temporary Rooms Badge Listener attached to /temporaryChatRooms.");
        }


        // --- Listener for Pending Requests (from Chat Requests) ---
        // Keep this block as is, it updates the separate Requests tab badge.
        if (requestBadgeListener == null) {
            DatabaseReference chatRequestsRootRef = RootRef.child("Chat Requests");

            requestBadgeListener = chatRequestsRootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Request Badge Listener: onDataChange triggered. Snapshot has " + snapshot.getChildrenCount() + " children (potential senders).");
                    int receivedRequestCount = 0;
                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

                    if (!TextUtils.isEmpty(userId) && snapshot.exists()) {
                        for (DataSnapshot senderSnapshot : snapshot.getChildren()) {
                            String senderId = senderSnapshot.getKey();
                            if (senderSnapshot.hasChild(userId)) {
                                DataSnapshot receiverSnapshot = senderSnapshot.child(userId);
                                String requestType = receiverSnapshot.child("request_type").getValue(String.class);
                                if ("sent".equals(requestType)) { // Correctly counting requests sent TO this user (as receiver)
                                    receivedRequestCount++;
                                }
                            }
                        }
                    } else if (TextUtils.isEmpty(userId)) {
                        Log.e(TAG, "Request Badge Listener: User ID is null. Cannot calculate requests.");
                    } else {
                        Log.d(TAG, "Request Badge Listener: No top-level request nodes found.");
                    }
                    pendingRequestCountForBadge = receivedRequestCount;
                    Log.d(TAG, "Request Badge Listener: Total received requests count: " + pendingRequestCountForBadge);
                    // Update the badge for the REQUESTS tab (assuming this is R.id.bottom_requests)
                    updateBadge(R.id.bottom_requests, pendingRequestCountForBadge);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Request Badge Listener: Cancelled.", error.toException());
                    // On error, clear the badge for Requests tab
                    updateBadge(R.id.bottom_requests, 0);
                }
            });
            Log.d(TAG, "Request Badge Listener attached to /Chat Requests.");
        }
    }

    // ... (Keep the rest of MainActivity.java)


    // ... (Keep your existing methods, including attachNotificationListeners) ...

    // Method to remove all Firebase listeners for notification badges
    private void removeNotificationListeners() {
        Log.d(TAG, "Removing notification listeners.");
        // No need to get currentUserID here as listeners are removed from root paths

        if (RootRef != null) {
            // Remove Chat/Group listener from /Chat Summaries
            if (chatGroupBadgeListener != null) {
                // The listener is on the root "Chat Summaries" node
                RootRef.child("Chat Summaries").removeEventListener(chatGroupBadgeListener);
                chatGroupBadgeListener = null;
                Log.d(TAG, "Chat/Group Badge Listener removed from /Chat Summaries.");
            }

            // Remove Request listener from /Chat Requests
            // The listener is on the root "Chat Requests" node
            if (requestBadgeListener != null) {
                RootRef.child("Chat Requests").removeEventListener(requestBadgeListener); // Removed from the root
                requestBadgeListener = null;
                Log.d(TAG, "Request Badge Listener removed from /Chat Requests.");
            }

        } else {
            Log.d(TAG, "RootRef is null, nothing to remove for notification listeners.");
        }

        // Reset counts and clear badges visually when listeners are removed (e.g., onStop, logout)
        // Ensure this runs on the UI thread
        runOnUiThread(() -> {
            unreadChatGroupCountForBadge = 0;
            pendingRequestCountForBadge = 0;
            // unreadChatCountState = 0; // These state variables were removed
            // unreadGroupCountState = 0; // These state variables were removed

            // Explicitly remove badges from the Bottom Navigation View
            BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
            if (bottomNavigation != null) {
                bottomNavigation.removeBadge(R.id.bottom_chat);
                bottomNavigation.removeBadge(R.id.bottom_requests);
                // Remove badges for other tabs if they can have them
            }
        });
        Log.d(TAG, "Badge counts reset and badges removed.");
    }

// ... (Keep your existing methods) ...


    // ... (Keep removeChatRequests method) ...

    // --- NEW Method to clean up Chat Summaries related to the deleted user ---
// ... (Keep removeChatRequests method) ...

    // --- NEW Method to clean up Chat Summaries related to the deleted user ---
    private void removeChatSummariesData(String userId, ProgressDialog progressDialog, FirebaseUser user) {
        Log.d(TAG, "Removing Firebase chat summaries data related to: " + userId);
        DatabaseReference chatSummariesRootRef = FirebaseDatabase.getInstance().getReference("Chat Summaries");

        // Use addListenerForSingleValueEvent because we only need the data once to clean up
        chatSummariesRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> updates = new HashMap<>();
                int modifiedSummaries = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot summarySnapshot : snapshot.getChildren()) {
                        String conversationId = summarySnapshot.getKey();
                        // Check if the deleted user was a participant
                        boolean isParticipant = summarySnapshot.child("participants").hasChild(userId);

                        if (isParticipant) {
                            // If the deleted user was a participant, remove their entry from participants and unreadCounts
                            // Path to remove: /Chat Summaries/{convId}/participants/{userId}
                            updates.put(conversationId + "/participants/" + userId, null);
                            // Path to remove: /Chat Summaries/{convId}/unreadCounts/{userId}
                            updates.put(conversationId + "/unreadCounts/" + userId, null);
                            modifiedSummaries++;

                            // OPTIONAL (More complex): If the conversation becomes empty (e.g., 1:1 chat)
                            // after removing this user, you might want to delete the *entire* summary node.
                            // This requires checking if the 'participants' node becomes empty or only contains the other user.
                            // For now, just removing the user's specific entries is a safer basic cleanup.

                        }
                    }
                }
                Log.d(TAG, "Firebase: Preparing to clean up Chat Summaries for user: " + userId + ". Modified " + modifiedSummaries + " summaries.");

                if (!updates.isEmpty()) {
                    chatSummariesRootRef.updateChildren(updates)
                            .addOnCompleteListener(summariesRemovalTask -> {
                                if (summariesRemovalTask.isSuccessful()) {
                                    Log.d(TAG, "Firebase: Successfully cleaned up Chat Summaries.");
                                } else {
                                    Log.w(TAG, "Firebase: Failed to clean up Chat Summaries.", summariesRemovalTask.getException());
                                    // Avoid toast here as this runs on a background thread initiated from deleteAccount
                                }
                                // Continue to the next step regardless of success/failure
                                removeUserMainData(userId, progressDialog, user); // *** CALL THE NEXT STEP IN THE CHAIN ***
                            });
                } else {
                    Log.d(TAG, "Firebase: User not found in any Chat Summaries participants lists to modify.");
                    removeUserMainData(userId, progressDialog, user); // *** CALL THE NEXT STEP IN THE CHAIN ***
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase: Failed to fetch Chat Summaries for cleanup", error.toException());
                // Avoid toast here
                removeUserMainData(userId, progressDialog, user); // *** CALL THE NEXT STEP EVEN ON ERROR ***
            }
        });
    }
// ... (Keep removeUserMainData and subsequent methods) ...

    // Helper method to calculate total unread conversations and update the badge
    private void updateGroupsTabBadge() {
        // Sum up the unread counts for Groups and Temporary Rooms
        int totalUnreadGroupsAndRooms = unreadGroupCountForBadge + unreadTemporaryRoomCountForBadge;

        // Update the badge on the BottomNavigationView for the Groups tab
        // ASSUMPTION: R.id.bottom_groups is the correct ID for your Groups tab
        updateBadge(R.id.bottom_groups, totalUnreadGroupsAndRooms);

        Log.d(TAG, "Updated Groups Tab Badge (R.id.bottom_groups). Groups: " + unreadGroupCountForBadge +
                ", Temp Rooms: " + unreadTemporaryRoomCountForBadge +
                ", TOTAL Unread Groups/Rooms: " + totalUnreadGroupsAndRooms);
    }


    private void updatePrivateChatBadge() {
        // unreadPrivateChatCountForBadge holds the total unread messages from private chats
        updateBadge(R.id.bottom_chat, unreadPrivateChatCountForBadge);

        Log.d(TAG, "Updated Private Chat Badge (R.id.bottom_chat) with total unread messages: " + unreadPrivateChatCountForBadge);
    }



    // *** NEW Helper method to load unread counts from Room for initial display (especially offline) ***
    // *** NEW Helper method to load unread counts from Room for initial display (especially offline) ***
    private void loadUnreadCountsFromRoom(String userId) {
        // Ensure DAOs and Executor are initialized before trying to use them
        // Use the member variables initialized in MainActivity's onCreate
        // Access the static executor from ChatDatabase class
        if (TextUtils.isEmpty(userId) || chatDao == null || groupListDao == null || ChatDatabase.databaseWriteExecutor == null || mainHandler == null) {
            Log.w(TAG, "Cannot load unread counts from Room: userId, DAOs, Executor, or Handler is null.");
            // Optionally clear counts and badges here if critical failure, but logging is usually sufficient
            return;
        }
        Log.d(TAG, "Loading initial unread counts from Room for user: " + userId);

        // Run Room queries on the background database executor (Access via static member)
        ChatDatabase.databaseWriteExecutor.execute(() -> { // <<< CORRECTED: Access static executor
            int privateChatCount = 0;
            int groupCount = 0;
            int tempRoomCount = 0;

            try {
                // Query ChatDao for total unread messages in private chats (Use member variable)
                privateChatCount = chatDao.getTotalUnreadMessageCount(userId);
                Log.d(TAG, "Room Load (Executor): Loaded " + privateChatCount + " total unread messages from private chats for user " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Room Load (Executor): Error loading private chat unread count.", e);
            }

            try {
                // Query GroupListDao for unread groups (Use member variable)
                groupCount = groupListDao.getUnreadGroupCount(userId);
                Log.d(TAG, "Room Load (Executor): Loaded " + groupCount + " unread groups for user " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Room Load (Executor): Error loading group unread count.", e);
            }

            try {
                // Query GroupListDao for unread temporary rooms (Use member variable)
                long currentTime = System.currentTimeMillis();
                tempRoomCount = groupListDao.getUnreadTemporaryRoomCount(userId, currentTime);
                Log.d(TAG, "Room Load (Executor): Loaded " + tempRoomCount + " unread temporary rooms for user " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Room Load (Executor): Error loading temporary room unread count.", e);
            }

            // Requests count is not cached in Room with the current setup, so we'll assume 0 offline initially.
            int requestCount = 0; // Initialize to 0 as it's not loaded from Room here.


            // Post the loaded counts to the Main Thread to update state variables and badges
            int finalPrivateChatCount = privateChatCount;
            int finalGroupCount = groupCount;
            int finalTempRoomCount = tempRoomCount;
            mainHandler.post(() -> {
                Log.d(TAG, "Room Load (Main Thread): Updating badge counts from Room.");
                // Update the state variables with the loaded counts
                unreadPrivateChatCountForBadge = finalPrivateChatCount;
                unreadGroupCountForBadge = finalGroupCount;
                unreadTemporaryRoomCountForBadge = finalTempRoomCount;
                // Request count remains 0 from Room load
                pendingRequestCountForBadge = requestCount; // Explicitly set to 0 from Room load


                // Update the badges immediately based on the locally loaded data
                updatePrivateChatBadge(); // Update chat tab badge
                updateGroupsTabBadge(); // Update groups tab badge
                updateBadge(R.id.bottom_requests, pendingRequestCountForBadge); // Update requests tab badge (will be 0 here)

                Log.d(TAG, "Room Load (Main Thread): Initial badge counts set from Room. Private: " + unreadPrivateChatCountForBadge + ", Groups: " + unreadGroupCountForBadge + ", Temp Rooms: " + unreadTemporaryRoomCountForBadge + ", Requests (Offline): " + pendingRequestCountForBadge);

                // Now proceed with other setup that might depend on user ID or keys being available
                // The existing onStart logic handles attaching Firebase listeners *after* this Room load starts.
                // The Firebase listeners will override these counts when Firebase data arrives.

            });
        });
    }

}





