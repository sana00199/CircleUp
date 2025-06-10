package com.sana.circleup;

// ### Android System/Framework Imports ###

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;


// Inside Login.java class


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// ... (other imports) ...
// *** NEW IMPORTS FOR ONESIGNAL API CALL ***
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
// *** END NEW IMPORTS ***

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.sana.circleup.encryptionfiles.CryptoUtils;
import com.sana.circleup.encryptionfiles.SecureKeyStorageUtil;
import com.sana.circleup.encryptionfiles.YourKeyManager;
import com.sana.circleup.one_signal_notification.OneSignalApiService;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.ConversationKeyDao;
import com.sana.circleup.room_db_implement.ConversationKeyEntity;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class Login extends AppCompatActivity implements GoogleSignInHelper.GoogleSignInListener {

    private static final String TAG = "LoginActivity";

    // --- UI Elements ---
    private EditText emailEditText; // Renamed from email to emailEditText
    private EditText passwordEditText; // Renamed from password to passwordEditText
    private CheckBox rememberMeCheckbox; // Renamed from checkBox to rememberMeCheckbox
    private Button loginButton; // Renamed from welcome_login_btn to loginButton
    private ImageView googleImg; // Kept google_img
    ImageView eyePassword; // Keep eye_password_login (ImageView)
    private TextView signupTextView; // Renamed from donthveaccount_signup to signupTextView
    private TextView forgetPasswordTextView; // Renamed from tvForgetPassword to forgetPasswordTextView

    // --- Firebase Instance
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    // --- Google Sign-In Helper ---
    private GoogleSignInHelper googleSignInHelper;

    // --- Preferences ---
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CircleUpPrefs";
    private static final String PREF_REMEMBER_ME = "RememberMe";
    private static final String PREF_LOGIN_METHOD = "LoginMethod"; // Store login method (email/google)

    // --- Background Task Execution ---
    private ExecutorService executorService; // For background tasks
    private Handler mainHandler; // For posting results back to the main thread

    // --- State Tracking ---
    private ProgressDialog progressDialog;
    private boolean isRememberMeCheckedOnLoginAttempt = true; // Default to false, set right before login attempt starts


    // --- Room DB and DAO members ---
    private ChatDatabase db;
    private ConversationKeyDao conversationKeyDao; // <<< ADD THIS MEMBER VARIABLE HERE



    // *** NEW MEMBER: Retrofit Service for OneSignal API ***
    private OneSignalApiService oneSignalApiService;
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR APP ID
    // *** END NEW MEMBER ***


    // Password strength pattern (Moved here from Signup)
    // Password must be 6+ characters, include uppercase, lowercase, digit, and special character
    private static final Pattern PASSWORD_STRENGTH_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Login onCreate: Started."); // Log start of onCreate

        // --- Initialize auth and preferences early ---
        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);





        // *** NEW: Initialize OneSignal API service ***
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://onesignal.com/") // OneSignal API Base URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            oneSignalApiService = retrofit.create(OneSignalApiService.class);
            Log.d(TAG, "OneSignalApiService initialized in Login Activity.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignalApiService in Login Activity", e);
            // Handle this error - maybe disable login button if notifications are critical?
            // For now, just log and allow login without notification.
        }
        // *** END NEW ***



        // --- Check Firebase auth state AND RememberMe preference immediately ---
        // If the user is already authenticated AND was remembered, redirect to MainActivity.
        FirebaseUser currentUser = auth.getCurrentUser();
        // Use false as the default value for PREF_REMEMBER_ME if it's never set.
        boolean isRemembered = sharedPreferences.getBoolean(PREF_REMEMBER_ME, true);

        Log.d(TAG, "Login onCreate: Checking Firebase user and RememberMe state...");
        Log.d(TAG, "Login onCreate: currentUser UID: " + (currentUser != null ? currentUser.getUid() : "null"));
        Log.d(TAG, "Login onCreate: isRemembered preference: " + isRemembered);

        // We only redirect if Firebase user exists AND RememberMe preference is TRUE.
        // The actual check if keys are *unlocked* (which is needed for full chat) happens in MainActivity's onStart.
        if (currentUser != null && isRemembered) {
            Log.d(TAG, "Login onCreate: User authenticated AND 'Remember Me' is TRUE. Redirecting to MainActivity for potential local key load.");
            // Navigate to MainActivity. MainActivity.onStart will handle checking for keys.
            Intent mainIntent = new Intent(Login.this, MainActivity.class); // Ensure MainActivity exists
            // Clear the Login activity and make MainActivity the new root of the task
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish(); // Finish Login activity
            return; // Stop onCreate execution
        }

        // If not remembered OR not authenticated, proceed with Login UI setup
        Log.d(TAG, "Login onCreate: User not remembered or not authenticated. Setting up Login UI.");
        setContentView(R.layout.activity_login); // Ensure activity_login.xml exists

        // Initialize remaining fields
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeViews(); // Initializes UI elements, incl. RememberMe checkbox
        setupListeners();

        // Setup background executor and main handler for async tasks
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Setup Google Sign-In helper
        googleSignInHelper = new GoogleSignInHelper(this); // Ensure GoogleSignInHelper exists
        googleSignInHelper.setGoogleSignInListener(this);

        // Password visibility toggle listener
        eyePassword.setOnClickListener(v -> togglePasswordVisibility(passwordEditText, eyePassword));

        // Restore the visual state of the checkbox from preferences
        rememberMeCheckbox.setChecked(isRemembered);


        // --- Initialize Room DB and DAO ---
        db = ChatDatabase.getInstance(this); // Initialize DB instance
        conversationKeyDao = db.conversationKeyDao(); // <<< ADD THIS LINE HERE
        Log.d(TAG, "Login onCreate: Room DB and DAOs initialized.");

        // No automatic login attempt here. User must click a button.
    }

    // --- UI Initialization ---
    private void initializeViews() {
        emailEditText = findViewById(R.id.email_edittext); // Match your layout ID
        passwordEditText = findViewById(R.id.password_edittext); // Match your layout ID
        loginButton = findViewById(R.id.welcome_login_btn); // Match your layout ID
        rememberMeCheckbox = findViewById(R.id.checkBoxrememberme); // Match your layout ID
        googleImg = findViewById(R.id.google_img); // Match your layout ID
        signupTextView = findViewById(R.id.donthveaccount_signup); // Match your layout ID
        forgetPasswordTextView = findViewById(R.id.tvForgetPassword); // Match your layout ID
        eyePassword = findViewById(R.id.eye_password_login); // Assuming correct ID from layout

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false); // Prevent dismissing by tapping outside
    }

    // --- Password Visibility Toggle ---
    private void togglePasswordVisibility(EditText editText, ImageView eyeIcon) {
        if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeIcon.setImageResource(R.drawable.eye); // Ensure you have eye.png
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeIcon.setImageResource(R.drawable.eye_closed); // Ensure you have eye_closed.png
        }
        editText.setSelection(editText.getText().length()); // Keep cursor at the end
    }

    // --- Listener Setup ---
    private void setupListeners() {
        // Save remember me preference when checkbox state changes
        rememberMeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // This updates the preference for future app starts.
            // The state for the *current* login attempt is captured just before auth starts.
            sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, isChecked).apply();
            Log.d(TAG, "RememberMe preference set to: " + isChecked + " via checkbox click.");
        });

        // Login button click listener (Email/Password)
        loginButton.setOnClickListener(v -> loginUser());

        // Google Sign-In button click listener
        googleImg.setOnClickListener(v -> {
            Log.d(TAG, "Google Sign-In button clicked.");
            // startGoogleSignIn(false) indicates this is a login attempt (not new user signup)
            googleSignInHelper.startGoogleSignIn(false);
        });

        // Signup text click listener
        signupTextView.setOnClickListener(v -> {
            Log.d(TAG, "Signup text clicked. Navigating to Signup.");
            startActivity(new Intent(Login.this, Signup.class)); // Ensure Signup exists
            finish(); // Finish Login activity after navigating to Signup
        });

        // Forget Password text click listener
        forgetPasswordTextView.setOnClickListener(v -> {
            Log.d(TAG, "Forget Password text clicked. Navigating to PasswordRecoveryActivity.");
            startActivity(new Intent(Login.this, PasswordRecoveryActivity.class)); // Ensure PasswordRecoveryActivity exists
        });
    }

    // --- Handle Email/Password Login ---
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input fields
        if (!validateInputs(email, password)) return;

        loginButton.setEnabled(false); // Prevent multiple clicks
        progressDialog.setMessage("Signing in with Email...");
        progressDialog.show();

        // --- Capture RememberMe state *before* the async login task starts ---
        isRememberMeCheckedOnLoginAttempt = rememberMeCheckbox.isChecked();
        Log.d(TAG, "Email/Password login attempt initiated. RememberMe checked for this attempt: " + isRememberMeCheckedOnLoginAttempt);
        // --- END Capture ---

        // Authenticate with Firebase Email/Password
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true); // Re-enable button

                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser(); // Get the authenticated user
                        if (user != null) {
                            Log.d(TAG, "Email/Password Auth Success. User UID: " + user.getUid());





                            // --- NEW: Check if email is verified ---
                            if (user.isEmailVerified()) {
                                Log.d(TAG, "Email is verified for user: " + user.getUid() + ".");

                                // --- NEW: Check if user profile is complete (basic data like username, and keys) ---
                                checkProfileCompletionAndProceed(user); // Call the new helper method
                                // --- END NEW ---


                            } else {
                                Log.w(TAG, "Email is NOT verified for user: " + user.getUid() + ". Logging out.");
                                progressDialog.dismiss();
                                Toast.makeText(Login.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                auth.signOut();
                                // Stay on login screen.
                            }
                            // --- END NEW ---



//                            // Save login method preference
//                            saveLoginMethodPreference(user); // Save 'email' method
//
//                            // ### YAHAN CALL KAREIN FOR EMAIL/PASSWORD LOGIN SUCCESS (EXISTING USER) ###
//                            // User successfully authenticated via email/password. Set OneSignal External User ID.
//                            Log.d(TAG, "Email/Password login successful for user: " + user.getUid() + ". Setting OneSignal External User ID.");
//                            // setOneSignalExternalUserId(user.getUid()); // Call your OneSignal method here
//
//
//                            // --- Proceed to fetch user data and handle encryption keys ---
//                            // Pass the FirebaseUser object here
//                            fetchUserDataAndHandleKeys(user);
//                            // --- END ---

                        } else {
                            // Authentication successful but Firebase user object is null (should not happen often, but handle defensively)
                            progressDialog.dismiss();
                            Log.e(TAG, "Authentication successful but Firebase user is null. Logging out.");
                            Toast.makeText(Login.this, "Authentication successful but user data is null. Please log in again.", Toast.LENGTH_SHORT).show();
                            auth.signOut(); // Log out from Firebase Auth
                            YourKeyManager.getInstance().clearKeys(); // Clear any potential keys from memory
                            SecureKeyStorageUtil.clearAllSecureKeys(Login.this, null); // Clear local keys (userId might be null here on error)
                            sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply(); // Ensure RememberMe preference is false
                            Log.d(TAG, "RememberMe preference set to FALSE as Firebase user is null after auth success.");
                            // Stay on login screen
                        }
                    } else {
                        // Email/Password Authentication failed
                        progressDialog.dismiss();
                        Log.e(TAG, "Email/Password Login Failed: " + task.getException().getMessage(), task.getException());
                        Toast.makeText(Login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply(); // Clear RememberMe preference on auth failure
                        Log.d(TAG, "RememberMe preference set to FALSE on auth failure.");
                        // Stay on login screen
                    }
                });
    }
    // --- End Email/Password Login ---








    // --- NEW Helper Method: Check Profile Completion and Proceed ---
    private void checkProfileCompletionAndProceed(FirebaseUser authenticatedUser) {
        String userId = authenticatedUser.getUid();
        Log.d(TAG, "checkProfileCompletionAndProceed: Checking profile completion for user: " + userId);
        progressDialog.setMessage("Checking profile status..."); // Update dialog message

        // Fetch user data from DB to check for completion status (e.g., presence of username and publicKey)
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // progressDialog dismissed in fetchUserDataAndHandleKeys or later

                boolean profileIsComplete = false;
                if (snapshot.exists()) {
                    // Check for key fields that indicate profile setup is done
                    boolean hasUsername = snapshot.hasChild("username") && !TextUtils.isEmpty(snapshot.child("username").getValue(String.class));
                    boolean hasCryptoKeys = snapshot.hasChild("publicKey"); // Check for presence of public key

                    if (hasUsername && hasCryptoKeys) {
                        profileIsComplete = true;
                        Log.d(TAG, "Profile is complete for user: " + userId + " (Username and Keys found).");
                    } else {
                        Log.d(TAG, "Profile is INCOMPLETE for user: " + userId + " (Username or Keys missing). HasUsername=" + hasUsername + ", HasCryptoKeys=" + hasCryptoKeys);
                    }
                } else {
                    // User data node itself does not exist (should be rare after signup but defensive)
                    Log.e(TAG, "User data node does NOT exist in DB for user: " + userId + ". Profile is incomplete.");
                    profileIsComplete = false;
                }

                if (profileIsComplete) {
                    // Profile is complete, proceed with the normal login flow (key handling, navigate to MainActivity)
                    Log.d(TAG, "Profile complete. Proceeding with normal login flow.");
                    saveLoginMethodPreference(authenticatedUser); // Save login method
                    fetchUserDataAndHandleKeys(authenticatedUser); // This handles key loading and navigation
                } else {
                    // Profile is incomplete, redirect to Setting_profile for setup
                    Log.d(TAG, "Profile incomplete. Redirecting to Setting_profile for user: " + userId);
                    progressDialog.dismiss(); // Dismiss current dialog before redirecting
                    Toast.makeText(Login.this, "Please complete your profile setup.", Toast.LENGTH_LONG).show();
                    Intent setupIntent = new Intent(Login.this, Setting_profile.class);
                    setupIntent.putExtra("isNewUser", true); // Indicate it's a new user setup flow
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear back stack
                    startActivity(setupIntent);
                    finish(); // Finish Login activity
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check profile completion for user: " + userId, error.toException());
                progressDialog.dismiss(); // Dismiss dialog on error
                Toast.makeText(Login.this, "Error checking profile status.", Toast.LENGTH_SHORT).show();
                // Decide how to handle error: stay on login, retry, force logout?
                // For now, stay on login and user can try again.
            }
        });
    }
    // --- END NEW Helper Method ---



    // --- Method to fetch user data (including keys) after ANY successful Firebase Auth login ---
    // Now accepts FirebaseUser object
    private void fetchUserDataAndHandleKeys(FirebaseUser authenticatedUser) {
        String userId = authenticatedUser.getUid(); // Get UID from the passed object
        progressDialog.setMessage("Fetching user data...");
        if (!progressDialog.isShowing()) { // Show only if not already showing
            progressDialog.show();
        }
        Log.d(TAG, "fetchUserDataAndHandleKeys: Fetching user data from DB for UID: " + userId + ". RememberMe checked on attempt: " + isRememberMeCheckedOnLoginAttempt);

        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "fetchUserDataAndHandleKeys: onDataChange triggered. Snapshot exists: " + snapshot.exists());
                if (snapshot.exists()) {
                    Log.d(TAG, "fetchUserDataAndHandleKeys: User data found in DB for " + userId);

                    // Check if crypto keys exist for this user (RSA keys) in Firebase
                    boolean hasCryptoKeysInFirebase = snapshot.hasChild("encryptedPrivateKey")
                            && snapshot.hasChild("encryptionSalt")
                            && snapshot.hasChild("publicKey");

                    Log.d(TAG, "fetchUserDataAndHandleKeys: hasCryptoKeysInFirebase: " + hasCryptoKeysInFirebase);

                    if (hasCryptoKeysInFirebase) {
                        String encryptedPrivateKeyBase64 = snapshot.child("encryptedPrivateKey").getValue(String.class);
                        String saltBase64 = snapshot.child("encryptionSalt").getValue(String.class);
                        String publicKeyBase64 = snapshot.child("publicKey").getValue(String.class);

                        // Validate fetched key data
                        if (TextUtils.isEmpty(encryptedPrivateKeyBase64) || TextUtils.isEmpty(saltBase64) || TextUtils.isEmpty(publicKeyBase64)) {
                            Log.e(TAG, "fetchUserDataAndHandleKeys: Fetched key data is incomplete or empty for user " + userId + ". Treating as missing.");
                            // Pass the authenticatedUser object and snapshot to handleMissingKeys
                            handleMissingKeys(authenticatedUser, String.valueOf(snapshot)); // Call helper for missing keys scenario
                        } else {
                            // --- Keys complete: Prompt for passphrase and start decryption ---
                            Log.d(TAG, "fetchUserDataAndHandleKeys: Crypto keys complete. Prompting for passphrase.");
                            // Pass the authenticatedUser object and fetched Base64 strings to the decryption prompt
                            promptForPassphraseAndDecrypt(authenticatedUser, publicKeyBase64, encryptedPrivateKeyBase64, saltBase64);
                        }

                    } else {
                        // User exists in Firebase DB but does NOT have crypto keys (security setup incomplete?)
                        Log.w(TAG, "fetchUserDataAndHandleKeys: User data found, but no crypto keys in Firebase. Handling missing keys.");
                        // Pass the authenticatedUser object and snapshot to handleMissingKeys
                        handleMissingKeys(authenticatedUser, String.valueOf(snapshot)); // Call helper for missing keys scenario
                    }
                } else {
                    // User data not found in DB for the authenticated user (Critical error!)
                    Log.e(TAG, "fetchUserDataAndHandleKeys: User data not found in DB for authenticated user: " + userId + ". Logging out.");
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, "User profile data missing. Please contact support or try re-registering.", Toast.LENGTH_LONG).show();
                    auth.signOut(); // Log out from Auth
                    YourKeyManager.getInstance().clearKeys(); // Clear any potentially set keys from memory
                    SecureKeyStorageUtil.clearAllSecureKeys(Login.this, userId); // Clear any potentially set local keys
                    sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply(); // Ensure RememberMe preference is false
                    Log.d(TAG, "RememberMe preference set to FALSE as user data missing in DB.");
                    // Stay on login screen
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "fetchUserDataAndHandleKeys: Failed to fetch user data from DB", error.toException());
                progressDialog.dismiss();
                Toast.makeText(Login.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                auth.signOut(); // Assume a critical failure, log out from Auth
                YourKeyManager.getInstance().clearKeys(); // Clear keys from memory
                SecureKeyStorageUtil.clearAllSecureKeys(Login.this, userId); // Clear local keys
                sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply(); // Ensure RememberMe preference is false
                Log.d(TAG, "RememberMe preference set to FALSE on DB fetch error.");
                // Stay on login screen
            }
        });
    }
    // --- End Fetch User Data and Handle Keys ---

    // --- Helper method for handling missing keys scenario (no Private Key available) ---
    // Now accepts FirebaseUser and DataSnapshot
    // In Login.java, replace your existing handleMissingKeys method with this one.

    /**
     * Helper method for handling missing or inaccessible keys scenario (Private Key unavailable).
     * This is called when:
     * 1. User logs in and crypto keys are found in Firebase, but they cancel the passphrase prompt.
     * 2. User logs in and crypto keys are found in Firebase, but decryption fails.
     * 3. User logs in and user data is found, but NO crypto keys exist in Firebase (incomplete setup).
     *
     * This method attempts to load only the Public Key into KeyManager (if its data is available),
     * clears local secure storage (as private key is not unlocked/available),
     * sets "Remember Me" preference to false, shows an informative Toast,
     * and proceeds to navigate to MainActivity. Secure features will be disabled in MainActivity.
     *
     * @param authenticatedUser The successfully authenticated FirebaseUser object.
     * @param publicKeyBase64String The Base64 string of the user's Public Key fetched from Firebase, or null/empty.
     */
    private void handleMissingKeys(FirebaseUser authenticatedUser, @Nullable String publicKeyBase64String) { // MODIFIED: Accept publicKeyBase64String
        String userId = authenticatedUser.getUid(); // Get UID from the passed object
        Log.d(TAG, "handleMissingKeys: Handling missing/inaccessible crypto keys for user " + userId + ". RememberMe checked on attempt: " + isRememberMeCheckedOnLoginAttempt);

        PublicKey publicKey = null;
        // Default message if no public key data is available at all
        String messageToShow = "Secure setup incomplete. Secure messaging unavailable. Set up in Settings.";

        // --- Attempt to load Public Key if its Base64 data is available ---
        if (!TextUtils.isEmpty(publicKeyBase64String)) { // Check if publicKeyBase64String is not null or empty
            try {
                // Use android.util.Base64 with DEFAULT flag for decoding (consistent with Firebase storage)
                byte[] publicKeyBytes = Base64.decode(publicKeyBase64String, Base64.DEFAULT); // Use android.util.Base64
                publicKey = CryptoUtils.bytesToPublicKey(publicKeyBytes); // Use your CryptoUtils method to convert bytes to PublicKey

                // Store *only* the public key in KeyManager. The private key remains null.
                YourKeyManager.getInstance().setPublicOnly(userId, publicKey); // Sets public, clears private and conv keys
                Log.d(TAG, "handleMissingKeys: Public key successfully loaded into KeyManager from Base64 string.");

                // Update the message to show that at least the public key is loaded
                messageToShow = "Secure messaging unavailable. Account unlocked partially (no private key). Set up/unlock in Settings.";

                // --- Handle Local Storage Save ONLY IF RememberMe was checked for this attempt ---
                // If RememberMe was checked, we attempt to save the public key bytes
                // and an empty placeholder for the private key, along with the symmetric key (which might not be available).
                // However, since decryption failed/was cancelled, we clear the symmetric key and save *only* the public key file.
                // This allows MainActivity's local load attempt to find the public key next time, even without the symmetric key.
                if (isRememberMeCheckedOnLoginAttempt) {
                    Log.d(TAG, "handleMissingKeys: RememberMe checked. Attempting to save Public Key to local secure storage for user: " + userId);
                    // Save an empty byte array for the private key part to signify it's missing/not unlocked locally
                    byte[] dummyEncryptedPrivateKey = new byte[0];
                    // Ensure publicKeyBytes is valid before saving
                    if (publicKeyBytes != null && publicKeyBytes.length > 0) {
                        // Use application context for SecureKeyStorageUtil to avoid leaks
                        boolean saveKeyPairSuccess = SecureKeyStorageUtil.saveEncryptedKeyPair(getApplicationContext(), userId, dummyEncryptedPrivateKey, publicKeyBytes); // Save public key bytes
                        if (saveKeyPairSuccess) {
                            Log.d(TAG, "handleMissingKeys: Public key saved securely to device storage.");
                        } else {
                            Log.w(TAG, "handleMissingKeys: Failed to save public key to secure storage.");
                        }
                    } else {
                        Log.e(TAG, "handleMissingKeys: Failed to get public key bytes (or bytes are empty) for local save. Skipping key pair save.");
                        // If public key bytes were invalid, clear keys completely from KeyManager as well
                        YourKeyManager.getInstance().clearKeys();
                        Log.e(TAG, "handleMissingKeys: Cleared KeyManager because public key bytes were invalid for local save.");
                        messageToShow = "Error loading key data. Secure messaging unavailable."; // Update message
                        SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear local storage again
                    }
                    // Always clear the symmetric key from local storage if we don't have the private key (which is needed to decrypt it)
                    SecureKeyStorageUtil.clearSymmetricKey(getApplicationContext()); // Use your SecureKeyStorageUtil

                } else {
                    // If RememberMe is off, ensure no keys are saved locally for this user from this session.
                    Log.d(TAG, "handleMissingKeys: RememberMe NOT checked. Skipping local save for incomplete setup. Clearing any potentially stale local keys.");
                    // Use application context for SecureKeyStorageUtil
                    SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Use your SecureKeyStorageUtil
                }
                // --- END Handle Local Storage Save ---

            } catch (IllegalArgumentException e) { // Base64 decoding or invalid key format error for public key
                Log.e(TAG, "handleMissingKeys: Failed to load/decode public key Base64 string: " + e.getMessage(), e); // Specific log
                // If public key data is invalid, clear everything from KeyManager and local storage
                YourKeyManager.getInstance().clearKeys();
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
                messageToShow = "Error loading key data. Secure messaging unavailable.";
            } catch (Exception e) { // Catch any other unexpected errors
                Log.e(TAG, "handleMissingKeys: An unexpected error occurred while handling public key loading for user " + userId, e); // Specific log
                YourKeyManager.getInstance().clearKeys(); // Clear everything
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear local data
                messageToShow = "An unexpected error occurred. Secure messaging unavailable.";
            }
        } else {
            // No public key Base64 string was provided (e.g., Firebase node missing entirely)
            YourKeyManager.getInstance().clearKeys(); // Sets both null and clears conv keys
            SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear any partial/corrupt data
            Log.w(TAG, "handleMissingKeys: No public key Base64 string available to load.");
            messageToShow = "Secure setup incomplete. Secure messaging unavailable. Set up in Settings."; // More specific default message
        }

        // --- Final Actions regardless of Public Key loading success ---
        // Dismiss dialog (it should already be dismissed by promptForPassphraseAndDecrypt's post block, but safety)
        // if (progressDialog != null && progressDialog.isShowing()) { progressDialog.dismiss(); } // Dialog dismissal is handled in the calling post block

        // Show a Toast based on the outcome message
        Toast.makeText(Login.this, messageToShow, Toast.LENGTH_LONG).show();

        // Ensure the RememberMe preference reflects that keys were NOT unlocked
        // If decryption failed or was cancelled, do not save "Remember Me" state as true.
        // This prevents an endless loop of attempting silent unlock when the private key isn't available.
        // We set RememberMe preference to false here, regardless of its state *before* this attempt.
        SharedPreferences prefs = getSharedPreferences("CircleUpPrefs", MODE_PRIVATE);
//        prefs.edit().putBoolean(PREF_REMEMBER_ME, true).apply(); // Even if RememberMe was checked, set false because unlock failed/skipped
        Log.d(TAG, "handleMissingKeys: RememberMe preference set to FALSE after key handling (failed/cancelled).");

        // Navigate to MainActivity after handling keys and setting preference
        // This is the intended redirection point when keys are unavailable but auth is valid.
        Log.d(TAG, "handleMissingKeys: Navigating to MainActivity for user: " + userId + " with private keys unavailable.");
        navigateToMainActivity(authenticatedUser); // This will call checkUserRoleAndNavigate

        // The Login activity will be finished by navigateToMainActivity's subsequent call to checkUserRoleAndNavigate.
    }


    // --- Method to Prompt for Passphrase using AlertDialog ---
    // Now accepts FirebaseUser object
    // --- Method to Prompt for Passphrase using AlertDialog ---
    // Now accepts FirebaseUser object
    private void promptForPassphraseAndDecrypt(FirebaseUser authenticatedUser, String publicKeyBase64, String encryptedPrivateKeyBase64, String saltBase64) {
        String userId = authenticatedUser.getUid(); // Get UID from the passed object
        // Dismiss any existing dialogs first if necessary
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Log.d(TAG, "promptForPassphraseAndDecrypt: Showing passphrase dialog for user: " + userId + ". RememberMe checked on attempt: " + isRememberMeCheckedOnLoginAttempt);

        // Ensure this runs on the main thread
        mainHandler.post(() -> { // Changed from new Handler to mainHandler
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            LayoutInflater inflater = getLayoutInflater();
            View customLayout = inflater.inflate(R.layout.dialog_passphrase_input, null); // Ensure this layout exists

            // *** CHNAGES START HERE ***
            // Get reference to the TextInputEditText component
            // Changed from EditText to TextInputEditText
            final TextInputEditText passphraseInput = customLayout.findViewById(R.id.passphrase_edittext_in_layout); // <-- Change type to TextInputEditText

            // If you also need the TextInputLayout reference:
            // TextInputLayout passphraseInputLayout = customLayout.findViewById(R.id.passphrase_input_layout);
            // *** CHNAGES END HERE ***


            builder.setView(customLayout);
            builder.setTitle("Enter Security Passphrase");

            builder.setPositiveButton("Unlock", (dialog, which) -> {
                String passphrase = passphraseInput.getText().toString();
                if (TextUtils.isEmpty(passphrase)) {
                    // Set error on the TextInputEditText
                    if (passphraseInput != null) {
                        passphraseInput.setError("Passphrase cannot be empty.");
                        passphraseInput.requestFocus();
                    } else {
                        // Fallback toast if TextInputEditText is null (shouldn't happen)
                        Toast.makeText(Login.this, "Passphrase cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                    // Re-prompt after a short delay, passing the authenticatedUser object
                    mainHandler.postDelayed(() ->
                            promptForPassphraseAndDecrypt(authenticatedUser, publicKeyBase64, encryptedPrivateKeyBase64, saltBase64), 300);
                } else {
                    Log.d(TAG, "promptForPassphraseAndDecrypt: Passphrase entered. Attempting decryption for user: " + userId);
                    // Start decryption on background thread, passing the authenticatedUser object
                    decryptPrivateKeyAsync(authenticatedUser, publicKeyBase64, encryptedPrivateKeyBase64, saltBase64, passphrase);
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss(); // Dismiss the dialog (changed from cancel())
                Log.w(TAG, "promptForPassphraseAndDecrypt: User cancelled passphrase prompt for user: " + userId + ".");
                // --- Handle keys and navigate without private key ---
                handleMissingKeys(authenticatedUser, publicKeyBase64); // Pass authenticatedUser and public key Base64
            });

            builder.setCancelable(false); // Dialog cannot be dismissed by tapping outside or back button
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            Log.d(TAG, "promptForPassphraseAndDecrypt: Passphrase dialog shown for user: " + userId + ".");
        });
    }
    // --- Helper method to decrypt private key on a background thread ---
    // Now accepts FirebaseUser object
    private void decryptPrivateKeyAsync(FirebaseUser authenticatedUser, String publicKeyBase64, String encryptedPrivateKeyBase64, String saltBase64, String passphrase) {
        String userId = authenticatedUser.getUid(); // Get UID from the passed object
        progressDialog.setMessage("Decrypting keys...");
        // Ensure dialog is showing if it wasn't already from Google Auth
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        Log.d(TAG, "decryptPrivateKeyAsync: Attempting private key decryption on background thread for user: " + userId + ". RememberMe checked on attempt: " + isRememberMeCheckedOnLoginAttempt);

        // Use the dedicated executor service
        executorService.execute(() -> {
            PublicKey publicKey = null;
            PrivateKey privateKey = null;
            SecretKey encryptionKey = null;

            byte[] encryptedPrivateKeyWithIV = null; // Keep this to save to file
            byte[] publicKeyBytes = null; // Keep this to save to file
            byte[] salt = null; // Keep this

            String errorMessage = null; // Holds the error message on failure
            boolean isWrongPassphraseError = false; // Flag for specific error type
            boolean localSaveAttempted = isRememberMeCheckedOnLoginAttempt; // Capture the state of the checkbox for this attempt
            boolean localSaveSuccess = false; // Track success of local save IF attempted

            try {
                // 1. Convert Base64 strings back to byte arrays
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Decoding Base64 data.");
                // Use android.util.Base64 with DEFAULT flag for decoding (assuming Firebase stored it that way)
                salt = Base64.decode(saltBase64, Base64.DEFAULT); // Use android.util.Base64
                encryptedPrivateKeyWithIV = Base64.decode(encryptedPrivateKeyBase64, Base64.DEFAULT); // Use android.util.Base64
                publicKeyBytes = Base64.decode(publicKeyBase64, Base64.DEFAULT); // Use android.util.Base64
                // Check if decoded bytes are valid
                if (salt == null || encryptedPrivateKeyWithIV == null || publicKeyBytes == null || salt.length == 0 || encryptedPrivateKeyWithIV.length == 0 || publicKeyBytes.length == 0) {
                    Log.e(TAG, "decryptPrivateKeyAsync (Executor): Decoded byte arrays are null or empty!");
                    throw new IllegalArgumentException("Decoded key data is invalid.");
                }
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Base64 decoding complete.");


                // 2. Derive Secret Key again from Passphrase and Salt using PBKDF2
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Deriving symmetric key from passphrase.");
                encryptionKey = CryptoUtils.deriveKeyFromPassphrase(
                        passphrase, salt, CryptoUtils.PBKDF2_ITERATIONS); // Use your CryptoUtils method
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Symmetric key derived.");

                // 3. Decrypt Private Key using the derived symmetric key
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Attempting to decrypt private key.");
                privateKey = CryptoUtils.decryptPrivateKey(encryptedPrivateKeyWithIV, encryptionKey); // Use your CryptoUtils method
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Private key decrypted successfully.");

                // 4. Convert Public Key bytes back to PublicKey object
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Converting public key bytes to object.");
                publicKey = CryptoUtils.bytesToPublicKey(publicKeyBytes); // Use your CryptoUtils method
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Public key object created.");


                // --- Success: Keys Decrypted ---
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Private key decrypted successfully for user " + userId);

                // Store BOTH keys securely in memory for the app session using the KeyManager
                // This always happens on decryption success, regardless of local save outcome
                YourKeyManager.getInstance().setKeys(userId, publicKey, privateKey); // Use your KeyManager
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Decrypted RSA keys loaded into KeyManager for current session. Private Key Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable());


                // After successfully loading the user's RSA Private Key into KeyManager,
                // load all conversation keys from Room DB into KeyManager's cache.
                // This happens asynchronously on the databaseWriteExecutor, initiated from here.
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Private key available. Starting conversation key load from Room.");
                if (!TextUtils.isEmpty(userId)) { // Safety check for userId
                    loadAllConversationKeysFromRoom(userId); // <<< CALL THIS HELPER METHOD HERE
                } else {
                    Log.e(TAG, "decryptPrivateKeyAsync (Executor): Cannot load conversation keys from Room, userId is unexpectedly null.");
                }


                // --- Handle Local Storage Save ONLY IF RememberMe was checked for this attempt ---
                if (localSaveAttempted) {
                    Log.d(TAG, "decryptPrivateKeyAsync (Executor): RememberMe checked. Attempting local secure save for user: " + userId);
                    // Save the symmetric key derived from the passphrase
                    // Use application context for SecureKeyStorageUtil
                    boolean saveSymmetricSuccess = SecureKeyStorageUtil.saveSymmetricKey(getApplicationContext(), encryptionKey); // Use your SecureKeyStorageUtil
                    // Save the encrypted private key bytes and the public key bytes (these came from Firebase)
                    // Pass the original encrypted private key bytes (encryptedPrivateKeyWithIV) and public key bytes (publicKeyBytes)
                    // Use application context for SecureKeyStorageUtil
                    boolean saveKeyPairSuccess = SecureKeyStorageUtil.saveEncryptedKeyPair(getApplicationContext(), userId, encryptedPrivateKeyWithIV, publicKeyBytes); // Use your SecureKeyStorageUtil

                    localSaveSuccess = saveSymmetricSuccess && saveKeyPairSuccess; // True only if BOTH succeeded

                    if (localSaveSuccess) {
                        Log.d(TAG, "decryptPrivateKeyAsync (Executor): All keys saved securely to device storage successfully for user: " + userId);
                    } else {
                        // If local save failed while RememberMe was checked, log error and clear potentially partial local data
                        Log.e(TAG, "decryptPrivateKeyAsync (Executor): FAILED to save one or more keys locally for user: " + userId + " even though RememberMe was checked. Clearing potentially partial local data.", new Exception("Local save failed"));
                        // Use application context for SecureKeyStorageUtil
                        SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear any partial save
                        // Note: We don't set an errorMessage here for decryption success, the failure will be signalled to the UI via localSaveSuccess=false.
                    }
                } else {
                    // If RememberMe is off, ensure no keys are saved locally for this user from this session. Clear any old keys just to be safe.
                    Log.d(TAG, "decryptPrivateKeyAsync (Executor): RememberMe NOT checked. Skipping local save for user: " + userId + ". Clearing any potentially stale local keys.");
                    // Use application context for SecureKeyStorageUtil
                    SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Use your SecureKeyStorageUtil
                }
                // --- END Handle Local Storage Save ---

                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Private key decryption complete for user: " + userId);

            } catch (IllegalArgumentException e) {
                // Handle Base64 decoding errors (corrupt data from Firebase?)
                Log.e(TAG, "decryptPrivateKeyAsync (Executor): Base64 decoding failed (corrupt data from Firebase?). Clearing local storage.", e);
                errorMessage = "Invalid key data received from server. Secure messaging unavailable. Please log out and log back in.";
                // Use application context for SecureKeyStorageUtil
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear local storage
                YourKeyManager.getInstance().clearKeys(); // Keys are not available in KeyManager
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException |
                     InvalidKeyException | IllegalBlockSizeException | BadPaddingException |
                     InvalidAlgorithmParameterException e) {
                // Catch specific crypto errors (wrong passphrase or data corruption)
                Log.e(TAG, "decryptPrivateKeyAsync (Executor): Key decryption failed for user: " + userId, e);

                // Check if it's likely a wrong passphrase
                if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof InvalidKeyException) {
                    errorMessage = "Incorrect Security Passphrase. Please try again.";
                    isWrongPassphraseError = true; // Set flag for specific error
                } else {
                    errorMessage = "Failed to decrypt keys. Secure messaging unavailable. Error: " + e.getMessage();
                    isWrongPassphraseError = false;
                }
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Decryption failed with crypto error. Clearing local keys for user: " + userId + ".");
                // Use application context for SecureKeyStorageUtil
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear local keys on decryption failure
                YourKeyManager.getInstance().clearKeys(); // Keys are not available in KeyManager
            } catch (Exception e) {
                // Catch any other unexpected errors during crypto ops or related setup
                Log.e(TAG, "decryptPrivateKeyAsync (Executor): An unexpected error occurred during key decryption for user: " + userId, e);
                errorMessage = "An unexpected error occurred. Secure messaging unavailable. Error: " + e.getMessage();
                isWrongPassphraseError = false;
                Log.d(TAG, "decryptPrivateKeyAsync (Executor): Unexpected error during decryption. Clearing local keys for user: " + userId + ".");
                // Use application context for SecureKeyStorageUtil
                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId); // Clear local keys
                YourKeyManager.getInstance().clearKeys(); // Keys are not available in KeyManager
            }

            // --- Post result back to Main Thread ---
            final String finalErrorMessage = errorMessage;
            final boolean finalIsWrongPassphraseError = isWrongPassphraseError;
            final FirebaseUser finalAuthenticatedUser = authenticatedUser; // Pass the authenticatedUser object
            final String finalPublicKeyBase64 = publicKeyBase64; // Pass original Base64 strings back
            final String finalEncryptedPrivateKeyBase64 = encryptedPrivateKeyBase64;
            final String finalSaltBase64 = saltBase64;
            final boolean finalLocalSaveAttempted = localSaveAttempted; // Pass state to Main Thread
            final boolean finalLocalSaveSuccess = localSaveSuccess;   // Pass state to Main Thread


            mainHandler.post(() -> { // This runs on the Main Thread
                progressDialog.dismiss(); // Dismiss dialog
                Log.d(TAG, "decryptPrivateKeyAsync (Main Thread): Post-decryption callback started for user: " + authenticatedUser.getUid());
                // Log KeyManager state at post start (should have RSA keys if successful)
                Log.d(TAG, "decryptPrivateKeyAsync (Main Thread): KeyManager state at post start: Private Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable() + ", Public Key Available=" + (YourKeyManager.getInstance().getUserPublicKey() != null) + ", Conv Keys loaded=" + YourKeyManager.getInstance().getAllConversationKeys().size());


                if (finalErrorMessage == null) {
                    // Decryption was successful
                    Toast.makeText(Login.this, "Account unlocked. Messages decrypted.", Toast.LENGTH_SHORT).show();

                    // Load all Conversation Keys from Room DB AFTER Private Key is AVAILABLE
                    // This call is already triggered async in the executor block above,
                    // no need to call it again here on the main thread.
                    Log.d(TAG, "decryptPrivateKeyAsync (Main Thread): Private key available. Conversation key load from Room already triggered async.");


                    // Save the final RememberMe state based on local save success/attempt
                    boolean shouldRemember = finalLocalSaveAttempted && finalLocalSaveSuccess; // Only remember if attempted AND succeeded local save
                    sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, shouldRemember).apply();
                    Log.d(TAG, "decryptPrivateKeyAsync (Main Thread): RememberMe preference saved as: " + shouldRemember + " after successful decryption (local save attempted: " + finalLocalSaveAttempted + ", local save success: " + finalLocalSaveSuccess + ").");


                    // If local save failed but decryption succeeded, add an extra warning
                    if (finalLocalSaveAttempted && !finalLocalSaveSuccess) {
                        Log.w(TAG, "decryptPrivateKeyAsync (Main Thread): Local key save failed despite successful decryption. RememberMe preference set FALSE.");
                        // Consider showing a non-critical warning toast here if not already done
                        Toast.makeText(Login.this, "Warning: Could not remember login details securely.", Toast.LENGTH_SHORT).show(); // Example warning
                    }


                    Log.d(TAG, "decryptPrivateKeyAsync (Main Thread): Navigating to MainActivity for user: " + authenticatedUser.getUid());
                    navigateToMainActivity(finalAuthenticatedUser); // Navigate to Main Activity on success, PASS USER OBJECT
                } else {
                    // Decryption failed - show error and handle re-prompt/cancel/navigate
                    Log.e(TAG, "decryptPrivateKeyAsync (Main Thread): Decryption failed for user: " + authenticatedUser.getUid() + ". Error: " + finalErrorMessage + ". Handling next step.");
                    // Pass the original data (publicKeyBase64 etc.) and error type to showDecryptionError
                    showDecryptionError(finalErrorMessage, finalIsWrongPassphraseError, finalAuthenticatedUser, finalPublicKeyBase64, finalEncryptedPrivateKeyBase64, finalSaltBase64); // Pass USER OBJECT and original Base64 strings
                }
                Log.d(TAG, "decryptPrivateKeyAsync (Main Thread): Post-decryption callback finished for user: " + authenticatedUser.getUid());
            });
            // --- End Post result ---
        });
    }


    // --- NEW Helper method to load Conversation Keys from Room ---
    // This method loads all conversation keys from Room DB into YourKeyManager.
    // It runs on the databaseWriteExecutor.
    // In Login.java
    // ... (other methods) ...

    // --- NEW Helper Method: Load Conversation Keys from Room (UPDATED) ---
    // This method loads all conversation key versions from Room DB into YourKeyManager.
    // It runs on the databaseWriteExecutor.
    private void loadAllConversationKeysFromRoom(String ownerUserId) {
        // Ensure ConversationKeyDao and userId are available
        // Use the member variable 'conversationKeyDao' which is initialized in onCreate
        if (conversationKeyDao == null || TextUtils.isEmpty(ownerUserId)) {
            Log.e(TAG, "loadAllConversationKeysFromRoom (Login): DAO or userId is null. Cannot load conversation keys.");
            return;
        }

        Log.d(TAG, "loadAllConversationKeysFromRoom (Login): Task SUBMITTED to executor for owner: " + ownerUserId);
        // Log KeyManager state BEFORE starting the DB task (for debugging flow)
        // Using getTotalCachedConversationKeyVersions which you added in Step 1


        // Use the shared DB executor from ChatDatabase
        // It's crucial that this runs on a background thread
        ChatDatabase.databaseWriteExecutor.execute(() -> { // Ensure ChatDatabase.databaseWriteExecutor is static public final
            Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Task STARTED on background thread for owner: " + ownerUserId);
            // Log KeyManager state AFTER task starts (confirm private key is still there)

            List<ConversationKeyEntity> keyEntities = null;
            try {
                // *** MODIFIED: Use getAllKeys() which now includes timestamp in the Entity ***
                // This query should fetch ALL key versions for this owner from Room
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Attempting get ALL keys from DAO for owner: " + ownerUserId);
                keyEntities = conversationKeyDao.getAllKeys(ownerUserId); // Call the DAO query
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): DAO query finished. Result size: " + (keyEntities != null ? keyEntities.size() : "null"));
                // *** END MODIFIED ***

            } catch (Exception e) { // Catch any exception during DAO query
                Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Error fetching keys from DAO for owner: " + ownerUserId, e);
                // Don't return, proceed with null list and handle errors during processing if list is not null
            }


            // --- Process keys if found ---
            if (keyEntities != null && !keyEntities.isEmpty()) {
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Processing " + keyEntities.size() + " conversation key versions found.");
                YourKeyManager keyManager = YourKeyManager.getInstance(); // Get KeyManager instance once (it's a singleton)

                // Ensure private key is STILL available before loading conversation keys into memory
                // Loading conversation keys requires the user's account to be unlocked (private key available).
                // If the private key disappeared from KeyManager mid-process (shouldn't happen in correct flow), stop loading conversation keys.
                if (!keyManager.isPrivateKeyAvailable()) {
                    Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Private key became unavailable during conversation key load process! Aborting load.");
                    // Optionally clear the conversation keys from KeyManager and Room if state is inconsistent
                    // keyManager.clearKeys(); // Clear everything from KeyManager (will also clear conv keys)
                    // try { conversationKeyDao.deleteAllKeysForOwner(ownerUserId); } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor): Error deleting keys after private key disappeared", deleteEx); }
                    return; // STOP processing if private key is missing
                }


                for (ConversationKeyEntity keyEntity : keyEntities) {
                    if (keyEntity == null) {
                        Log.w(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Skipping null keyEntity from Room list.");
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
                        Log.w(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Skipping key with empty/null essential fields or invalid timestamp from Room. Conv ID: " + convId + ", Owner (in Entity): " + ownerIdFromEntity + ", Timestamp: " + keyTimestamp);
                        // Optional: you might want to delete corrupt entries from Room here
                        try {
                            // Ensure DAO is available
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity) && keyTimestamp > 0) {
                                // Use the delete method that includes timestamp now!
                                conversationKeyDao.deleteKeyForConversation(ownerIdFromEntity, convId); // <<< CORRECTED: Use deleteKeyForConversation and remove keyTimestamp

                                // Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Deleted corrupt key from Room DB. Conv ID: " + convId + ", Timestamp: " + keyTimestamp);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Error deleting corrupt key from Room", deleteEx); }
                        continue;
                    }

                    // Important safety check: Ensure the key belongs to the current user
                    if (!ownerIdFromEntity.equals(ownerUserId)) {
                        // This indicates a potential data issue where a key for a different user is in this user's DB
                        Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Login): BUG? Loaded key from Room for owner " + ownerIdFromEntity + " but expecting owner " + ownerUserId + ". Skipping and deleting unexpected entry.");
                        // Delete this unexpected entry from Room DB
                        try {
                            // Ensure DAO is available
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity) && keyTimestamp > 0) {
                                // Use the delete method that includes timestamp now!
                                conversationKeyDao.deleteKeyForConversation(ownerIdFromEntity, convId); // <<< CORRECTED: Use deleteKeyForConversation and remove keyTimestamp // *** MODIFIED DELETE CALL ***
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Deleted unexpected key from Room DB. Conv ID: " + convId + ", Timestamp: " + keyTimestamp);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Error deleting unexpected key from Room", deleteEx); }

                        continue;
                    }


                    // Convert Base64 back to byte array and then to SecretKey
                    try {
                        Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Processing key version for conv ID: " + convId + ", Timestamp: " + keyTimestamp + ", Owner: " + ownerIdFromEntity);
                        // Use android.util.Base64 with DEFAULT flag for decoding (assuming Room stored it this way)
                        byte[] decryptedKeyBytes = android.util.Base64.decode(decryptedKeyBase64, android.util.Base64.DEFAULT); // Use android.util.Base64
                        // Check if decoded bytes are valid before converting to SecretKey
                        if (decryptedKeyBytes == null || decryptedKeyBytes.length == 0) {
                            Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Decoded key bytes are null or empty for conv ID: " + convId + ", Timestamp: " + keyTimestamp);
                            throw new IllegalArgumentException("Invalid decrypted key data bytes."); // Throw to catch below
                        }
                        SecretKey conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedKeyBytes); // Use your CryptoUtils method to convert bytes to SecretKey

                        // --- Store the decrypted SecretKey in the in-memory KeyManager cache (Use the new method) ---
                        keyManager.setConversationKey(convId, conversationAESKey); // *** MODIFIED KEYMANAGER CALL ***
                        // Log that a key was loaded (can be very verbose)


                    } catch (IllegalArgumentException e) { // Base64 decoding error or invalid bytes format for SecretKey
                        Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Error decoding Base64 or converting to SecretKey from Room for conv ID: " + convId + ", Timestamp: " + keyTimestamp + ". Deleting corrupt entry.", e);
                        // Delete corrupt entry from Room
                        try {
                            // Ensure DAO is available
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity) && keyTimestamp > 0) {
                                // Use the delete method that includes timestamp now!
                                conversationKeyDao.deleteKeyForConversation(ownerIdFromEntity, convId); // <<< CORRECTED: Use deleteKeyForConversation and remove keyTimestamp // *** MODIFIED DELETE CALL ***
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Deleted corrupt key from Room DB. Conv ID: " + convId + ", Timestamp: " + keyTimestamp);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Error deleting corrupt key from Room", deleteEx); }
                    } catch (Exception e) { // Catch any other unexpected errors during conversion
                        Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Unexpected error processing conversation key from Room for conv ID: " + convId + ", Timestamp: " + keyTimestamp, e);
                        // Decide if you delete on other errors - generally safer to delete corrupt data
                        try {
                            if (conversationKeyDao != null && !TextUtils.isEmpty(convId) && !TextUtils.isEmpty(ownerIdFromEntity) && keyTimestamp > 0) {
                                // Use the delete method that includes timestamp now!
                                conversationKeyDao.deleteKeyForConversation(ownerIdFromEntity, convId); // <<< CORRECTED: Use deleteKeyForConversation and remove keyTimestamp // *** MODIFIED DELETE CALL ***
                                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Deleted key from Room DB after processing error. Conv ID: " + convId + ", Timestamp: " + keyTimestamp);
                            }
                        } catch (Exception deleteEx) { Log.e(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Error deleting key from Room after processing error", deleteEx); }
                    }
                }

            } else {
                // No keys found in Room for this user, which is fine (maybe first time using encrypted chat or Room was cleared)
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): No conversation key versions found in Room DB for owner: " + ownerUserId);
            }
            Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Task FINISHED on background thread.");

            // Post a runnable back to the main thread to signal that keys are loaded.
            // In Login, we just log the completion, the navigation happens later
            // in decryptPrivateKeyAsync's mainHandler.post block.
            mainHandler.post(() -> {
                Log.d(TAG, "loadAllConversationKeysFromRoom (Executor - Login): Posted completion signal to Main Thread.");
                // No UI refresh needed in Login, as it navigates after this.
            });
        });
    }

    // ... (rest of Login.java) ...

    // --- Helper method to show decryption error and handle re-prompt/cancel ---
    // Now accepts FirebaseUser object
    private void showDecryptionError(String message, boolean isWrongPassphrase, FirebaseUser authenticatedUser, String publicKeyBase64, String encryptedPrivateKeyBase64, String saltBase64) {
        String userId = authenticatedUser.getUid(); // Get UID from the passed object
        Log.d(TAG, "showDecryptionError: Showing error toast and handling re-prompt/navigation for user: " + userId + ".");
        Log.d(TAG, "showDecryptionError: KeyManager state: Private Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable() + ", Public Key Available=" + (YourKeyManager.getInstance().getUserPublicKey() != null) + ", Conv Keys loaded=" + YourKeyManager.getInstance().getAllConversationKeys().size());

        // Show the user the specific error message
        Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();

        // Re-prompt ONLY if the error is specifically an "Incorrect Passphrase"
        // AND the user had "Remember Me" checked during this login attempt.
        if (isWrongPassphrase && isRememberMeCheckedOnLoginAttempt) {
            Log.d(TAG, "showDecryptionError: Incorrect passphrase entered, RememberMe was checked on attempt. Re-prompting dialog for user: " + userId);
            // Re-prompt after a short delay, passing the authenticatedUser object
            mainHandler.postDelayed(() ->
                    promptForPassphraseAndDecrypt(authenticatedUser, publicKeyBase64, encryptedPrivateKeyBase64, saltBase64), 300);

        } else {
            // For other errors or if RememberMe was NOT checked, do NOT re-prompt. Private key is unavailable.
            Log.e(TAG, "showDecryptionError: Decryption failed permanently for this attempt or RememberMe NOT checked for user: " + userId + ". Proceeding without private keys.");

            // *** Ensure RememberMe State Saving after failure ***
            // If decryption failed or was cancelled, do not save "Remember Me" state as true.
            // This prevents endless loop of attempting silent unlock when the private key isn't available.
            // We set RememberMe preference to false here, regardless of its state *before* this attempt.
            sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply(); // Even if RememberMe was checked, set false because unlock failed/skipped
            Log.d(TAG, "showDecryptionError: RememberMe preference set to FALSE after decryption failure (isWrongPassphrase: " + isWrongPassphrase + ", wasRememberMeChecked: " + isRememberMeCheckedOnLoginAttempt + ").");
            // *** END Ensure ***

            // KeyManager has already been set with null private key in decryptPrivateKeyAsync on failure.
            Log.d(TAG, "showDecryptionError: Navigating to MainActivity for user: " + userId + " with private keys unavailable.");
            // Pass the authenticatedUser object to the next navigation step
            navigateToMainActivity(authenticatedUser); // Navigate to Main Activity
            // User stays logged in via Firebase Auth.
        }
    }


    // --- Helper Navigation Method to go back to Login ---
    private void navigateToLoginScreen() {
        Log.d(TAG, "navigateToLoginScreen: Navigating back to Login screen.");
        Intent loginIntent = new Intent(Login.this, Login.class); // Go back to Login Activity
        // Clear the activity stack to prevent returning to the failed state
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish(); // Finish the current Login activity instance
        Log.d(TAG, "navigateToLoginScreen: Current Login Activity finished.");
    }
    // --- END NEW Helper Navigation Method ---


    // --- Helper Navigation Method to go to MainActivity ---
    // Now accepts FirebaseUser object
    // Inside Login.java

// ... (other methods) ...

    // --- Helper Navigation Method to go to MainActivity ---
    private void navigateToMainActivity(FirebaseUser authenticatedUser) {
        // This method is called *after* login authentication and initial key handling.
        // It checks if Firebase Auth user is present.
        Log.d(TAG, "navigateToMainActivity: Called for user: " + (authenticatedUser != null ? authenticatedUser.getUid() : "null") + ". Checking Firebase User state.");
        // Log KeyManager state (for info) - it's already set by handleMissingKeys or decryptPrivateKeyAsync
        Log.d(TAG, "navigateToMainActivity: KeyManager state upon navigation call: Private Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable() + ", Public Key Available=" + (YourKeyManager.getInstance().getUserPublicKey() != null) + ", Conv Keys loaded=" + YourKeyManager.getInstance().getAllConversationKeys().size());


        // Check if Firebase Auth user is present.
        // If the user is null at this stage, it's a critical error after a successful login.
        if (authenticatedUser == null) { // Check the passed object
            // Critical error: Auth user missing unexpectedly.
            Log.e(TAG, "navigateToMainActivity: CRITICAL ERROR! Firebase Auth user missing unexpectedly! Forcing re-login.");
            Toast.makeText(this, "Authentication state missing. Please log in again.", Toast.LENGTH_LONG).show();
            // Logging out and clearing keys is important before redirecting
            auth.signOut(); // Sign out from Firebase Auth using the global instance
            YourKeyManager.getInstance().clearKeys(); // Clear keys from memory
            // Clear local keys (pass current UID before signOut if possible, or null if already signed out)
            // Since authenticatedUser is null here, pass null for userId to clear any global/non-user-specific keys
            SecureKeyStorageUtil.clearAllSecureKeys(this, null);
            sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply(); // Ensure RememberMe preference is false
            Log.d(TAG, "navigateToMainActivity: RememberMe preference set to FALSE on critical navigation error.");
            // Redirect back to login
            navigateToLoginScreen();
            return; // Stop processing
        } else {
            // Firebase User is present. Proceed with role check.
            Log.d(TAG, "navigateToMainActivity: Firebase User is present. Proceeding with role check.");
        }


        // Set OneSignal External User ID (Firebase UID) here for existing users after successful login and key handling.
        // This links the OneSignal Player ID with your Firebase User ID in OneSignal's system.
        // Use the user's UID to link.
        Log.d(TAG, "navigateToMainActivity: Setting OneSignal External User ID: " + authenticatedUser.getUid());
        // *** UN-COMMENT THIS LINE AND ENSURE IT'S CORRECT ***
//         // <-- CORRECTED LINE // Call OneSignal method with authenticated user's UID
// *** UN-COMMENT THIS LINE AND ENSURE IT'S CORRECT ***
        // *** OneSignal External ID set for push notifications ***

        String currentUserId = authenticatedUser.getUid();
        OneSignal.login(authenticatedUser.getUid()); // <-- CORRECTED LINE




        // *** NEW: Send Account Creation/Login Success Notification to the creator (current user) ***
        Log.d(TAG, "navigateToMainActivity: Sending account success notification to creator.");
        // Fetch username first for a better notification title
        fetchUsernameAndSendLoginNotification(currentUserId);
        // This method will handle sending the notification after fetching the name asynchronously.
        // The rest of the navigation logic (checkUserRoleAndNavigate) runs in parallel.
        // *** END NEW ***




        // Fetch user role and navigate, PASS THE AUTHENTICATED USER OBJECT
        checkUserRoleAndNavigate(authenticatedUser); // This will handle navigation to MainActivity or AdminDashboard
        // The finish() call happens inside checkUserRoleAndNavigate upon successful navigation
    }





    // --- NEW Helper Method: Fetch Username and Send Login Notification ---
    private void fetchUsernameAndSendLoginNotification(String userId) {
        if (TextUtils.isEmpty(userId)) {
            Log.w(TAG, "fetchUsernameAndSendLoginNotification: userId is empty, cannot fetch name or send notification.");
            // Optionally still send a generic notification without username
            // sendPushNotification(userId, "Login Successful!", "Welcome back!");
            return;
        }

        // Fetch username from Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("username");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.getValue(String.class);
                String title = "Login Successful!";
                String message;
                if (!TextUtils.isEmpty(username)) {
                    message = "Welcome back, " + username + "!";
                    Log.d(TAG, "Fetched username '" + username + "'. Sending login notification.");
                } else {
                    message = "Welcome back!";
                    Log.w(TAG, "Username not found for UID " + userId + ". Sending generic login notification.");
                }

                // *** Call the helper method to send the notification ***
                sendPushNotification(
                        userId, // Recipient is the user themselves
                        title,
                        message
                );
                // *** END CALL ***
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch username for login notification: " + error.getMessage(), error.toException());
                // Send a generic notification on error fetching name
                sendPushNotification(userId, "Login Successful!", "Welcome back!");
            }
        });
    }
    // --- END NEW Helper Method ---



// ... (rest of Login.java) ...

    // --- Implement GoogleSignInListener Callbacks ---
//    @Override
//    public void onGoogleAuthComplete(FirebaseUser user, boolean isNewUser) {
//        // This callback is triggered by GoogleSignInHelper after successfully authenticating with Firebase via Google,
//        // and after checking if the user exists in the /Users database node.
//        // isNewUser is true IF the Google flow started from Signup AND the user didn't exist in DB.
//
//        Log.d(TAG, "onGoogleAuthComplete callback received in Login. User: " + (user != null ? user.getUid() : "null") + ", isNewUser: " + isNewUser);
//        progressDialog.dismiss(); // Dismiss any Google sign-in progress dialog
//
//        if (user != null) {
//            if (isNewUser) {
//                // It's a genuinely new user coming from Google Sign-Up.
//                // Navigate to the profile setup screen to collect username/status and MOST IMPORTANTLY, set up the passkey/crypto keys.
//                Log.d(TAG, "Google Auth Complete (Login): New user confirmed. Navigating to Setting_profile."); // Corrected log context
//                Intent intent = new Intent(Login.this, Setting_profile.class); // Ensure Setting_profile exists
//                intent.putExtra("isNewUser", true); // Flag for Setting_profile
//                // Clear the task stack so user cannot go back to Signup/Login using the back button
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                finish(); // Finish Login activity
//            } else {
//                // This Google account already exists in our Firebase DB.
//                // This is the standard flow for an *existing* Google user logging in via the Login screen.
//                Log.d(TAG, "Google Auth Complete (Login): Existing user login for UID: " + user.getUid() + ". Fetching user data from DB."); // Corrected log context
//
//                // ### YAHAN CALL KAREIN FOR EXISTING GOOGLE LOGIN SUCCESS ###
//                // Existing Google user authenticated. Set OneSignal External User ID.
//                Log.d(TAG, "Existing Google login successful for user: " + user.getUid() + ". Setting OneSignal External User ID.");
//                // setOneSignalExternalUserId(user.getUid()); // Call your OneSignal method here
//
//                // --- Capture RememberMe state *before* fetching user data ---
//                // We need this state in the fetchUserDataAndHandleKeys method.
//                isRememberMeCheckedOnLoginAttempt = rememberMeCheckbox.isChecked(); // Capture state from UI
//                Log.d(TAG, "Google login complete. RememberMe checked on attempt: " + isRememberMeCheckedOnLoginAttempt);
//                // --- END Capture ---
//
//                // Save login method preference
//                saveLoginMethodPreference(user); // Save 'google' method
//
//                // Proceed to fetch user data and handle encryption keys for existing user, PASS USER OBJECT
//                fetchUserDataAndHandleKeys(user); // Fetch user data and handle keys
//            }
//        } else {
//            // Should not happen if task is successful, but defensive check
//            Log.e(TAG, "onGoogleAuthComplete callback received null user unexpectedly. Logging out.");
//            progressDialog.dismiss(); // Ensure dismiss
//            Toast.makeText(Login.this, "Google Sign-in failed: User data is null. Please log in again.", Toast.LENGTH_SHORT).show();
//            auth.signOut(); // Log out from Firebase Auth
//            YourKeyManager.getInstance().clearKeys(); // Clear any potential keys from memory
//            SecureKeyStorageUtil.clearAllSecureKeys(Login.this, null); // Clear local keys
//            sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply(); // Ensure RememberMe preference is false
//            Log.d(TAG, "RememberMe preference set to FALSE on Google auth null user.");
//            // Stay on login screen
//        }
//    }





    // --- Modified GoogleSignInListener Callbacks ---
    @Override
    public void onGoogleAuthComplete(FirebaseUser user, boolean isNewUserFromHelper) { // Renamed isNewUserFromHelper for clarity
        Log.d(TAG, "onGoogleAuthComplete callback received in Login. User: " + (user != null ? user.getUid() : "null") + ", isNewUserFromHelper: " + isNewUserFromHelper);
        progressDialog.dismiss(); // Dismiss any Google sign-in progress dialog

        if (user != null) {
            // --- NEW: Check if email is verified (usually true for Google) ---
            if (user.isEmailVerified()) {
                Log.d(TAG, "Google Auth Complete: Email is verified for user: " + user.getUid() + ".");

                // --- NEW: Check if this is a *genuinely* new user needing profile setup ---
                // Use the isNewUserFromHelper flag passed by GoogleSignInHelper which checks DB existence
                if (isNewUserFromHelper) {
                    // This Google account is NEW in our Firebase DB. Redirect to Setting_profile.
                    Log.d(TAG, "Google Auth Complete (Login): Genuinely new Google user confirmed. Navigating to Setting_profile.");
                    Intent intent = new Intent(Login.this, Setting_profile.class);
                    intent.putExtra("isNewUser", true); // Flag for Setting_profile
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Finish Login activity
                } else {
                    // This Google account already exists in our Firebase DB.
                    // Proceed with the normal login flow (check profile completion, fetch data, handle keys).
                    Log.d(TAG, "Google Auth Complete (Login): Existing Google user login for UID: " + user.getUid() + ". Checking profile completion.");
                    saveLoginMethodPreference(user); // Save 'google' method
                    isRememberMeCheckedOnLoginAttempt = rememberMeCheckbox.isChecked(); // Capture state
                    // Check profile completion before fetching keys/navigating
                    checkProfileCompletionAndProceed(user); // Call the helper method
                }
            } else {
                // This case is rare for Google Auth but handle defensively
                Log.w(TAG, "Google Auth Complete: Email is NOT verified for user: " + user.getUid() + ". Logging out.");
                progressDialog.dismiss();
                Toast.makeText(Login.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                auth.signOut(); // Log out the user
                // Stay on login screen.
            }
            // --- END NEW ---
        } else {
            Log.e(TAG, "onGoogleAuthComplete callback received null user unexpectedly.");
            progressDialog.dismiss();
            Toast.makeText(Login.this, "Google Sign-in failed: User data is null. Please log in again.", Toast.LENGTH_SHORT).show();
            auth.signOut();
            YourKeyManager.getInstance().clearKeys();
            SecureKeyStorageUtil.clearAllSecureKeys(Login.this, null);
            sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply();
        }
    }



    @Override
    public void onGoogleAuthFailed(Exception e) {
        // Google Sign-In or Firebase Auth with Google failed when initiated from Login.
        Log.e(TAG, "Google Auth Failed callback received in Login.", e);
        progressDialog.dismiss(); // Dismiss dialog
        Toast.makeText(Login.this, "Google sign-in failed: " + (e != null ? e.getMessage() : "Unknown Error"), Toast.LENGTH_LONG).show();
        sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply(); // Clear RememberMe preference on auth failure
        Log.d(TAG, "RememberMe preference set to FALSE on Google auth failure.");
        // Stay on login screen to allow user to try again or use email login.
    }

    @Override
    public void onGoogleAuthCancelled() {
        // Google Sign-In flow was cancelled by the user when initiated from Login.
        Log.d(TAG, "Google Auth Cancelled callback received in Login.");
        progressDialog.dismiss(); // Dismiss dialog if it was showing
        Toast.makeText(Login.this, "Google sign-in cancelled.", Toast.LENGTH_SHORT).show();
        // User cancelled Google sign-in flow. Do not change RememberMe preference based on this.
        // Stay on login screen.
    }
    // --- End GoogleSignInListener Callbacks ---


    // Handles the result from startActivityForResult, specifically for Google Sign-In.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the result to GoogleSignInHelper if it's the Google Sign-In request code
        if (requestCode == GoogleSignInHelper.RC_SIGN_IN) {
            // GoogleSignInHelper will handle the result and notify us via the listener
            Log.d(TAG, "onActivityResult: Received Google Sign-In result. Passing to helper.");
            // Show progress while helper processes
            progressDialog.setMessage("Authenticating with Google...");
            if (!progressDialog.isShowing()) { // Defensive check
                progressDialog.show(); // Show progress while GoogleSignInHelper processes result
            }
            googleSignInHelper.handleSignInResult(data);
        }
    }
    // --- End Handle Google Sign-In Activity Result ---


    // --- Existing Helper Methods (Keep these) ---

    // Helper method to save login method preference
    private void saveLoginMethodPreference(FirebaseUser user) {
        if (user == null) {
            Log.w(TAG, "saveLoginMethodPreference called with null FirebaseUser.");
            return; // Defensive check
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String loginMethod = "email"; // Default if no specific provider found

        // Check provider data. The first element is usually the primary provider.
        boolean isGoogleUser = false;
        for (UserInfo profile : user.getProviderData()) {
            if (profile != null && profile.getProviderId() != null && profile.getProviderId().equals("google.com")) {
                isGoogleUser = true;
                break;
            }
            // Add checks for other providers like phone, facebook etc. if you add them
        }
        if (isGoogleUser) {
            loginMethod = "google";
        }

        editor.putString(PREF_LOGIN_METHOD, loginMethod);
        editor.apply();
        Log.d(TAG, "Login method preference saved as: " + loginMethod);
    }

    // Helper method to validate email and password inputs (for Email/Password Login)
    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }
        // Using a basic check for email format (can be improved)
        // This regex allows a broader range of email formats than the one in Signup.
        Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }
        // You might want to add password length check here for consistency with signup,
        // but Firebase Auth handles basic password length requirements.
        // if (password.length() < 6) { ... }
        return true; // All basic validations passed
    }

    // This method is called after login/key handling to check user role and navigate
    // Now accepts FirebaseUser object
    private void checkUserRoleAndNavigate(FirebaseUser authenticatedUser) {
        String userId = authenticatedUser.getUid(); // Get UID from the passed object
        Log.d(TAG, "checkUserRoleAndNavigate called for user: " + userId);

        // Check if Firebase Auth user is present.
        // Use the passed authenticatedUser object for the check.
        if (authenticatedUser == null) { // Check the passed object
            // This state is handled by navigateToMainActivity before calling this.
            // If we reach here with null user, something went very wrong.
            Log.e(TAG, "checkUserRoleAndNavigate: CRITICAL ERROR! Firebase Auth user missing unexpectedly during role check for UID: " + userId + ". Forcing re-login.");
            Toast.makeText(this, "Authentication state missing. Please log in again.", Toast.LENGTH_LONG).show();
            // Logging out and clearing keys is important before redirecting
            auth.signOut(); // Use global instance
            YourKeyManager.getInstance().clearKeys();
            SecureKeyStorageUtil.clearAllSecureKeys(this, userId);
            sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply();
            Log.d(TAG, "RememberMe preference set to FALSE on critical navigation error.");
            navigateToLoginScreen();
            return; // Stop processing
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.get().addOnCompleteListener(task -> {
            progressDialog.dismiss(); // Dismiss dialog before potentially finishing activity

            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String role = snapshot.child("role").getValue(String.class); // Assuming 'role' field exists

                if (role != null && !role.isEmpty()) {
                    Log.d(TAG, "checkUserRoleAndNavigate: User role found: " + role + " for UID: " + userId);
                    if (role.equals("admin")) {
                        Log.d(TAG, "checkUserRoleAndNavigate: User is Admin. Navigating to Admin Dashboard.");
//                        Intent intent = new Intent(Login.this, AdminDashboard.class); // Or AdminSplashScreen
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);

                        Toast.makeText(this, "this is not admin apk", Toast.LENGTH_SHORT).show();
                    } else { // Role is user or something else
                        Log.d(TAG, "checkUserRoleAndNavigate: User is not Admin. Navigating to Main Activity.");
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    finish(); // Finish Login activity after navigation
                    Log.d(TAG, "checkUserRoleAndNavigate: Login Activity finished after successful navigation.");
                } else {
                    // Role is missing - Critical error for a successfully logged-in user
                    Log.e(TAG, "checkUserRoleAndNavigate: User role not found for UID: " + userId + ". Logging out.");
                    Toast.makeText(Login.this, "User role data missing. Please contact support. Logging out.", Toast.LENGTH_LONG).show();
                    auth.signOut(); // Use global instance
                    YourKeyManager.getInstance().clearKeys();
                    SecureKeyStorageUtil.clearAllSecureKeys(this, userId);
                    sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply();
                    Log.d(TAG, "RememberMe preference set to FALSE on missing role.");
                    navigateToLoginScreen(); // Redirect to Login
                }
            } else {
                // User data not found in DB - Critical error
                Log.e(TAG, "checkUserRoleAndNavigate: User data not found in DB for UID: " + userId + ". Logging out.");
                Toast.makeText(Login.this, "User data not found! Please contact support. Logging out.", Toast.LENGTH_LONG).show();
                auth.signOut(); // Use global instance
                YourKeyManager.getInstance().clearKeys();
                SecureKeyStorageUtil.clearAllSecureKeys(this, userId);
                sharedPreferences.edit().putBoolean(PREF_REMEMBER_ME, false).apply();
                Log.d(TAG, "RememberMe preference set to FALSE on user data not found.");
                navigateToLoginScreen(); // Redirect to Login
            }
        });
    }

    // Assuming you have a method like this somewhere in your OneSignal integration



    // --- Add onDestroy to shut down the ExecutorService ---
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shut down the executor service when the activity is destroyed
        if (executorService != null && !executorService.isShutdown()) {
            Log.d(TAG, "ExecutorService shutting down.");
            executorService.shutdownNow();
        }
        // Dismiss progress dialog to prevent leaks
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Log.d(TAG, "LoginActivity onDestroy called.");
        // Optional: Log KeyManager state on destroy
        // Log.d(TAG, "LoginActivity onDestroy: KeyManager state: Private Available=" + YourKeyManager.getInstance().isPrivateKeyAvailable() + ", Conv Keys loaded=" + YourKeyManager.getInstance().getAllConversationKeys().size());
    }
    // --- End onDestroy ---




    // --- Copy the sendPushNotification helper method from ChatPageActivity.java ---
    // This method sends the actual push notification via OneSignal API.
    // Ensure you adjust the parameters if your original method was slightly different.
    // This version uses recipientFirebaseUID, title, messageContent.
    private void sendPushNotification(String recipientFirebaseUID, String title, String messageContent) {
        // Check if API service is initialized and recipient UID is valid
        if (oneSignalApiService == null || TextUtils.isEmpty(recipientFirebaseUID)) {
            Log.e(TAG, "sendPushNotification (Login): API service not initialized or recipient UID is empty. Cannot send notification.");
            // No toast needed here, this is a background action
            return;
        }

        Log.d(TAG, "Preparing to send push notification (Login) to recipient UID (External ID): " + recipientFirebaseUID);

        // --- Build the JSON payload for OneSignal API ---
        JsonObject notificationBody = new JsonObject();
        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID);

        JsonArray externalUserIds = new JsonArray();
        externalUserIds.add(recipientFirebaseUID); // Target the user themselves
        notificationBody.add("include_external_user_ids", externalUserIds);

        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", title)));
        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", messageContent)));

        // Optional: Add custom data - for login success, maybe just indicate type?
        JsonObject data = new JsonObject();
        data.addProperty("eventType", "login_success"); // Custom event type
        // Add sender/recipient IDs (they are the same user in this case)
        data.addProperty("senderId", recipientFirebaseUID);
        data.addProperty("recipientId", recipientFirebaseUID);
        // Add other relevant data if needed for handling notification click (e.g., opening main screen)
        // data.addProperty("targetScreen", "MainActivity"); // Example

        notificationBody.add("data", data);
        notificationBody.addProperty("small_icon", "app_icon_circleup"); // Use your small icon resource name

        // --- Make the API call ---
        Log.d(TAG, "Making API call to OneSignal (Login)...");
        oneSignalApiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) { // Added @NonNull
                if (response.isSuccessful()) {
                    Log.d(TAG, "OneSignal API call successful (Login). Response Code: " + response.code());
                    try (ResponseBody responseBody = response.body()) { // Use try-with-resources
                        String resBody = responseBody != null ? responseBody.string() : "N/A";
                        Log.d(TAG, "OneSignal API Response Body (Login): " + resBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read success response body (Login)", e);
                    }
                } else {
                    Log.e(TAG, "OneSignal API call failed (Login). Response Code: " + response.code());
                    try (ResponseBody errorBody = response.errorBody()) { // Use try-with-resources
                        String errBody = errorBody != null ? errorBody.string() : "N/A";
                        Log.e(TAG, "OneSignal API Error Body (Login): " + errBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read error response body (Login)", e);
                    }
                    Log.w(TAG, "Push notification failed via OneSignal API (Login).");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) { // Added @NonNull
                Log.e(TAG, "OneSignal API call failed (network error) (Login)", t);
                Log.w(TAG, "Failed to send push notification due to network error (Login).");
            }
        });
        Log.d(TAG, "OneSignal API call enqueued (Login).");
    }
    // --- END Copy ---


    // This method should now call the OneSignal SDK method.


}


