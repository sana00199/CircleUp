package com.sana.circleup;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import static org.bouncycastle.asn1.x509.ObjectDigestInfo.publicKey;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;



// *** NEW IMPORTS FOR ONESIGNAL API CALL ***
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray; // Import JsonArray from Gson
import com.google.gson.JsonObject; // Import JsonObject from Gson
import okhttp3.ResponseBody; // Import ResponseBody from OkHttp
import retrofit2.Call; // Import Retrofit Call
import retrofit2.Callback; // Import Retrofit Callback
import retrofit2.Response; // Import Retrofit Response
import retrofit2.Retrofit; // Import Retrofit
import retrofit2.converter.gson.GsonConverterFactory; // Import GsonConverterFactory
// *** END NEW IMPORTS ***

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.room.Room;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.sana.circleup.room_db_implement.AppDatabase;
import com.sana.circleup.room_db_implement.UserProfileDao;
import com.sana.circleup.room_db_implement.UserProfileEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;





// --- Existing Imports ---
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout; // Already exists, but need to use it

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room; // Keep if using ROOM DB

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sana.circleup.R; // Ensure R is imported
// --- End Existing Imports ---


// --- New Imports for Step 3 ---
import android.util.Log; // For logging crypto errors
import android.view.View; // For View.GONE/VISIBLE
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText; // For the new Passphrase EditTexts

import javax.crypto.SecretKey;
// SETINGPROFILE (Cleaned up - removed empty setupClickListeners method)

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log; // Use android.util.Log
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Import CryptoUtils class
import com.sana.circleup.encryptionfiles.CryptoUtils; // !! ADJUST THIS IMPORT if CryptoUtils is in a different package !!
// Import SecureKeyStorageUtil
import com.sana.circleup.encryptionfiles.SecureKeyStorageUtil; // !! ADJUST THIS IMPORT !!

// Import necessary crypto exceptions
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import java.security.InvalidAlgorithmParameterException;
import javax.crypto.SecretKey;


public class Setting_profile extends AppCompatActivity {

    private static final String TAG = "SettingProfile";

    private ImageView profileImageView, deleteImageView;
    // In Setting_profile.java

    // ... existing UI Elements ...
    private TextView setupStatusTextView; // New: TextView for status updates
    // ... existing UI Elements ...
    private EditText usernameEditText, statusEditText;
    private EditText emailEditText;
    private TextInputEditText editTextEncryptionPassphrase, editTextConfirmPassphrase;
    private LinearLayout passphraseInputLayout;
    private CardView signup_card;
    private Button saveButton;
    private Uri imageUri;

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    private ProgressDialog progressDialog;

    private static final int PICK_IMAGE_REQUEST = 10;
    private static final int MAX_IMAGE_SIZE = 800;
    private static final int IMAGE_COMPRESSION_QUALITY = 80;


    private String base64Image = "";

    private boolean isNewUser = false;
    private ExecutorService executorService;
    private Handler mainHandler;


    // In Setting_profile.java

    // ... existing UI Elements ...
    private LinearLayout recoveryCodeLayout; // New: Layout for recovery code section
    private TextView textViewRecoveryCode; // New: TextView to display the recovery code
    private Button buttonCopyRecoveryCode; // New: Button to copy the code
    private Button buttonProceedToApp; // New: Button to proceed after saving code
    private Space spacerAfterRecoveryCode; // New: Spacer view
// ... existing UI Elements ...

    private String generatedRecoveryCode = ""; // New: To temporarily hold the generated code

// ... existing Firebase, ExecutorService, Handler, etc. members ...

    // *** NEW MEMBER: Retrofit Service for OneSignal API ***
    private OneSignalApiService oneSignalApiService;
    // Get this from your OneSignal Dashboard -> Settings -> Keys & IDs -> OneSignal App ID
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR APP ID
// In Setting_profile.java

    // ... existing member variables ...
    private TextInputLayout emailInputLayout; // New member variable for the email TextInputLayout
// ...
    // *** END NEW MEMBER ***


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);
        Log.d(TAG, "Setting_profile onCreate.");

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeViews();
        // Removed call to setupClickListeners() as listeners are set inline below




        // *** NEW: Initialize OneSignal API service ***
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    // Base URL for OneSignal API (DO NOT CHANGE THIS)
                    .baseUrl("https://onesignal.com/")
                    // Add Gson converter factory
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            // Create an instance of your API service interface
            oneSignalApiService = retrofit.create(OneSignalApiService.class);
            Log.d(TAG, "OneSignalApiService initialized in Setting_profile.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignalApiService in Setting_profile", e);
            // Handle this error - maybe disable save button if notifications are critical?
            // For now, just log and allow profile setup without notification.
        }
        // *** END NEW ***




        // --- START: ADD THIS CODE AFTER initializeViews() ---
        // Setup Toolbar as ActionBar
        // Setup Toolbar as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_setting_profile); // Ensure this ID matches your layout
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.settings_profile_title); // Use string resource
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
            }
        } else {
            Log.w(TAG, "Toolbar not found in layout with ID toolbar_privacy_security");
        }




        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        isNewUser = getIntent().getBooleanExtra("isNewUser", false);
        Log.d(TAG, "isNewUser flag: " + isNewUser);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No current user in Setting_profile! Redirecting to Login.");
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity();
            return;
        }



//        // --- NEW: Check if email is verified (Defensive Check) ---
//        if (!currentUser.isEmailVerified()) {
//            Log.e(TAG, "User email is NOT verified in Setting_profile! Redirecting to Login.");
//            Toast.makeText(this, "Please verify your email to complete setup.", Toast.LENGTH_LONG).show();
//            // Log out and redirect to ensure they must verify via Login flow
//            auth.signOut(); // Log out
//            sendUserToLoginActivity(); // Redirect
//            return; // Stop onCreate execution
//        }
//        // --- END NEW ---




        if (isNewUser) {
            passphraseInputLayout.setVisibility(View.VISIBLE);
            signup_card.setVisibility(View.VISIBLE);
            saveButton.setText("Set Profile & Secure Account");
            Log.d(TAG, "New user setup path.");

            if (currentUser.getEmail() != null) {
                emailEditText.setText(currentUser.getEmail());
            } else {
                emailEditText.setText("No Email Available");
                Log.w(TAG, "Firebase User email is null for new user.");
            }
            emailEditText.setEnabled(false);
            Log.d(TAG, "Skipping loadUserData for new user.");

        } else {
            passphraseInputLayout.setVisibility(View.GONE);
            signup_card.setVisibility(View.GONE);
            saveButton.setText("Save Profile");
            emailEditText.setEnabled(false);
//             <<< YAHAN EXISTING USER KE LIYE FALSE HAI. Yeh sahi hai agar non-editable chahte hain.

            Log.d(TAG, "Existing user profile update path.");
            loadUserData(currentUser.getUid());
        }

        // --- Setup Click Listeners (Inline in onCreate) ---
        profileImageView.setOnClickListener(v -> openImagePicker());

        deleteImageView.setOnClickListener(v -> {
            Log.d(TAG, "Delete profile image clicked.");
            base64Image = "";
            profileImageView.setImageResource(R.drawable.photocameradefault);
            Toast.makeText(this, "Profile image removed. Save profile to confirm.", Toast.LENGTH_SHORT).show();
        });


        if (emailEditText != null && emailInputLayout != null) {
            emailEditText.setOnClickListener(v -> {
                Log.d(TAG, "User email field clicked (non-editable). Showing error.");
                // Set the error message on the TextInputLayout
                emailInputLayout.setError("Email cannot be changed.");
                // Enable the error state visual indicator
                emailInputLayout.setErrorEnabled(true);

                // Use a Handler to clear the error after a few seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Clear the error message
                    emailInputLayout.setError(null);
                    // Explicitly disable the error state visual (sometimes needed)
                    emailInputLayout.setErrorEnabled(false);
                    Log.d(TAG, "Email field error message cleared.");
                }, 3000); // Clear after 3 seconds (adjust delay as needed)
            });

            // Optional: Also handle focus change (when user tabs into the field)
            emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    Log.d(TAG, "User email field focused (non-editable). Showing error.");
                    // Set the error message on the TextInputLayout
                    emailInputLayout.setError("Email cannot be changed.");
                    emailInputLayout.setErrorEnabled(true);

                    // Use a Handler to clear the error after a few seconds
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        emailInputLayout.setError(null);
                        emailInputLayout.setErrorEnabled(false);
                        Log.d(TAG, "Email field error message cleared.");
                    }, 3000); // Clear after 3 seconds
                }
                // You could also clear the error when focus is lost if desired,
                // but the timed clear handles it even if focus remains.
            });

        } else {
            Log.e(TAG, "email_edittext or email_input_layout not found for setting non-editable listener!");
        }
        // --- END NEW ---



        saveButton.setOnClickListener(v -> saveUserProfile());
        // --- End Setup Click Listeners ---
    }


    private void initializeViews() {
        profileImageView = findViewById(R.id.profile_image);
        deleteImageView = findViewById(R.id.delete_profile_image);
        usernameEditText = findViewById(R.id.username_edittext);
        statusEditText = findViewById(R.id.status_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        emailInputLayout = findViewById(R.id.email_input_layout);
        saveButton = findViewById(R.id.welcome_login_btn);
        passphraseInputLayout = findViewById(R.id.passphraseInputLayout);
        signup_card = findViewById(R.id.signup_card);
        editTextEncryptionPassphrase = findViewById(R.id.editTextEncryptionPassphrase);
        editTextConfirmPassphrase = findViewById(R.id.editTextConfirmPassphrase);

        setupStatusTextView = findViewById(R.id.setupStatusTextView); // Make sure ID matches layout

        recoveryCodeLayout = findViewById(R.id.recoveryCodeLayout);
        textViewRecoveryCode = findViewById(R.id.textViewRecoveryCode);
        buttonCopyRecoveryCode = findViewById(R.id.buttonCopyRecoveryCode);
        buttonProceedToApp = findViewById(R.id.button_proceed_to_app); // Make sure ID matches layout
        spacerAfterRecoveryCode = findViewById(R.id.spacerAfterRecoveryCode); // Make sure ID matches layout
        // --- End New ---.

    }




    // --- START: ADD THIS NEW METHOD OUTSIDE onCreate() ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check if the Up button (back button) was clicked
        if (item.getItemId() == android.R.id.home) {
            // Simulate a back press, which will finish the current activity
            onBackPressed(); // Ya finish(); bhi use kar sakte hain
            return true; // Indicate that we've handled this click event
        }
        // Let the system handle any other menu items
        return super.onOptionsItemSelected(item);
    }
// --- END: ADD THIS NEW METHOD ---


    // Load data for existing users (Kept from your code)
    private void loadUserData(String userId) {
        progressDialog.setMessage("Loading profile data...");
        progressDialog.show();
        Log.d(TAG, "Loading existing user data for UID: " + userId);

        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();

                if (snapshot.exists()) {
                    Log.d(TAG, "Existing user data found in DB.");
                    String name = snapshot.hasChild("username") ? snapshot.child("username").getValue(String.class) : null;
                    String email = snapshot.hasChild("email") ? snapshot.child("email").getValue(String.class) : null;
                    String status = snapshot.hasChild("status") ? snapshot.child("status").getValue(String.class) : null;
                    String profileImage = snapshot.hasChild("profileImage") ? snapshot.child("profileImage").getValue(String.class) : null;

                    if (name != null) usernameEditText.setText(name); else Log.w(TAG, "Username node missing.");
                    if (status != null) statusEditText.setText(status); else Log.w(TAG, "Status node missing.");


                    // >>>>>>>>>>>>>>>> ADD THIS BLOCK TO SET EMAIL >>>>>>>>>>>>>>>>>>
                    if (email != null) {
                        emailEditText.setText(email); // <<< Fetched email ko EditText mein set kiya
                        Log.d(TAG, "Email loaded: " + email);
                    } else {
                        emailEditText.setText("No Email Available"); // Default text agar email node missing ho
                        Log.w(TAG, "Email node missing for user: " + userId);
                    }
                    //

                    base64Image = profileImage;
                    Log.d(TAG, "Loaded profileImage Base64 string: " + (profileImage != null ? profileImage.length() + " bytes" : "null"));

                    if (!TextUtils.isEmpty(profileImage)) {
                        try {
                            Bitmap bitmap = decodeBase64(profileImage); // Uses android.util.Base64
                            if (bitmap != null) {
                                profileImageView.setImageBitmap(bitmap);
                            } else {
                                Log.w(TAG, "Failed to decode profile image Base64. Using default.");
                                profileImageView.setImageResource(R.drawable.photocameradefault);
                            }
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Error decoding profile image Base64 string from DB", e);
                            profileImageView.setImageResource(R.drawable.photocameradefault);
                        }
                    } else {
                        Log.d(TAG, "ProfileImage Base64 string is empty or null. Using default.");
                        profileImageView.setImageResource(R.drawable.photocameradefault);
                    }
                } else {
                    Log.e(TAG, "Existing user data not found in database for UID: " + userId + "!");
                    Toast.makeText(Setting_profile.this, "User data not found in database!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Log.e(TAG, "Failed to load profile data from DB", error.toException());
                Toast.makeText(Setting_profile.this, "Failed to load profile data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openImagePicker() {
        Log.d(TAG, "Opening image picker.");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Log.d(TAG, "Image selected from picker. URI: " + imageUri);
            processSelectedImage(imageUri);
        } else {
            Log.d(TAG, "Image selection cancelled or failed.");
        }
    }

    private void processSelectedImage(Uri uri) {
        try {
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            Log.d(TAG, "Original image dimensions: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

            Bitmap resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
            Log.d(TAG, "Resized image dimensions: " + resizedBitmap.getWidth() + "x" + resizedBitmap.getHeight());

            profileImageView.setImageBitmap(resizedBitmap);

            base64Image = encodeToBase64(resizedBitmap); // Uses android.util.Base64
            Log.d(TAG, "Encoded image to Base64. Length: " + (base64Image != null ? base64Image.length() : 0) + " bytes.");

        } catch (IOException e) {
            Log.e(TAG, "Image processing failed during selection", e);
            Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show();
            base64Image = "";
            profileImageView.setImageResource(R.drawable.photocameradefault);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during image processing", e);
            Toast.makeText(this, "An error occurred processing image", Toast.LENGTH_SHORT).show();
            base64Image = "";
            profileImageView.setImageResource(R.drawable.photocameradefault);
        }
    }


    private void saveUserProfile() {
        String username = usernameEditText.getText().toString().trim();
        String status = statusEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return;
        }

        if (isNewUser) {
            String passphrase = editTextEncryptionPassphrase.getText().toString();
            String confirmPassphrase = editTextConfirmPassphrase.getText().toString();
            Log.d(TAG, "Attempting new user setup with passphrase.");

            if (TextUtils.isEmpty(passphrase) || TextUtils.isEmpty(confirmPassphrase)) {
                Toast.makeText(this, "Please set a Security Passphrase", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passphrase.equals(confirmPassphrase)) {
                Toast.makeText(this, "Passphrases do not match", Toast.LENGTH_SHORT).show();
                editTextEncryptionPassphrase.requestFocus();
                return;
            }
            if (passphrase.length() < 6) {
                Toast.makeText(this, "Passphrase must be at least 6 characters", Toast.LENGTH_SHORT).show();
                editTextEncryptionPassphrase.requestFocus();
                return;
            }

            performInitialKeySetupAndSaveProfile(username, status, base64Image, passphrase);

        } else {
            Log.d(TAG, "Attempting existing user profile update.");
            saveExistingProfile(username, status, base64Image);
        }
    }


    private void saveExistingProfile(String username, String status, String profileImageBase64) {
        progressDialog.setMessage("Saving profile updates...");
        progressDialog.show();
        Log.d(TAG, "Saving existing profile updates for user: " + auth.getCurrentUser().getUid());

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("username", username);
            profileMap.put("status", status);
            profileMap.put("isBlocked", false);
            profileMap.put("role", "user");
            profileMap.put("email", currentUser.getEmail());

            profileMap.put("profileImage", profileImageBase64);

            userRef.child(userId).updateChildren(profileMap).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Log.d(TAG, "Profile updated successfully for UID: " + userId);
                    Toast.makeText(Setting_profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Setting_profile.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();

                } else {
                    Log.e(TAG, "Failed to update profile: " + task.getException().getMessage(), task.getException());
                    Toast.makeText(Setting_profile.this, "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressDialog.dismiss();
            Log.e(TAG, "User not logged in during profile update!");
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity();
        }
    }


    // --- Modified performInitialKeySetupAndSaveProfile Method ---
    // This method runs on a background thread to generate keys and save to Firebase.
//    private void performInitialKeySetupAndSaveProfile(String username, String status, String profileImageBase64, String passphrase) {
//        progressDialog.setMessage("Securing account and saving profile...");
//        progressDialog.show();
//        Log.d(TAG, "Starting initial key setup and profile save for new user.");
//
//        FirebaseUser currentUser = auth.getCurrentUser();
//        if (currentUser == null) {
//            Log.e(TAG, "User not logged in during new user setup!");
//            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
//            progressDialog.dismiss();
//            sendUserToLoginActivity();
//            return;
//        }
//        final String userId = currentUser.getUid(); // Use final for background thread access
//        final String userEmail = currentUser.getEmail(); // Use final for background thread access
//        final String finalUsername = username; // Use final for background thread access
//
//
//        executorService.execute(() -> {
//            Exception cryptoException = null;
//            boolean firebaseSaveSuccess = false;
//            boolean localSaveSuccess = false;
//
//            // *** Declare keys outside try block so they are accessible later ***
//            PublicKey publicKey = null;
//            PrivateKey privateKey = null; // Decrypted Private Key
//            byte[] newlyEncryptedPrivateKeyWithIV = null; // Encrypted Private Key with IV
//            // *** End Declaration ***
//
//
//            try {
//                Log.d(TAG, "Generating salt."); // Step 1
//                byte[] salt = CryptoUtils.generateSalt();
//
//                Log.d(TAG, "Deriving encryption key from passphrase."); // Step 2
//                SecretKey encryptionKey = CryptoUtils.deriveKeyFromPassphrase(
//                        passphrase, salt, CryptoUtils.PBKDF2_ITERATIONS);
//
//                Log.d(TAG, "Generating RSA key pair."); // Step 3
//                KeyPair keyPair = CryptoUtils.generateRSAKeyPair();
//                publicKey = keyPair.getPublic(); // Assign to member variable
//                privateKey = keyPair.getPrivate(); // Assign to member variable
//
//
//
//
//
//                Log.d(TAG, "Encrypting private key."); // Step 4
//                // *** Ensure this line exists and is uncommented ***
//                newlyEncryptedPrivateKeyWithIV = CryptoUtils.encryptPrivateKey(privateKey, encryptionKey); // Assign to member variable
//
//
//                Log.d(TAG, "Encoding keys and salt to Base64."); // Step 5
//                String publicKeyBase64 = java.util.Base64.getEncoder().encodeToString(CryptoUtils.publicKeyToBytes(publicKey));
//                // *** Ensure this line exists and uses the correct variable name ***
//                String encryptedPrivateKeyBase64 = java.util.Base64.getEncoder().encodeToString(newlyEncryptedPrivateKeyWithIV); // Use the variable
//                String saltBase64 = java.util.Base64.getEncoder().encodeToString(salt);
//
//                // --- Data for Firebase ---
//                Map<String, Object> userData = new HashMap<>(); // Step 6
//                userData.put("uid", userId);
//                userData.put("email", userEmail);
//                userData.put("username", finalUsername);
//                userData.put("status", status);
//                userData.put("profileImage", profileImageBase64);
//                userData.put("role", "user");
//                userData.put("isBlocked", false);
//
//                userData.put("publicKey", publicKeyBase64);
//                userData.put("encryptedPrivateKey", encryptedPrivateKeyBase64);
//                userData.put("encryptionSalt", saltBase64);
//
//                Map<String, Object> userState = new HashMap<>();
//                userState.put("date", "");
//                userState.put("time", "");
//                userState.put("state", "offline");
//                userData.put("userState", userState);
//
//                // --- Save to Local Secure Storage --- // Step 7
//                Log.d(TAG, "Saving symmetric key and encrypted key pair to local secure storage.");
//                boolean saveSymmetricSuccess = SecureKeyStorageUtil.saveSymmetricKey(getApplicationContext(), encryptionKey);
//                // *** Ensure this line exists and uses the correct variable name ***
//                boolean saveKeyPairSuccess = SecureKeyStorageUtil.saveEncryptedKeyPair(getApplicationContext(), userId, newlyEncryptedPrivateKeyWithIV, CryptoUtils.publicKeyToBytes(publicKey)); // Use the variable
//
//
//                localSaveSuccess = saveSymmetricSuccess && saveKeyPairSuccess;
//
//                if (localSaveSuccess) {
//                    Log.d(TAG, "All keys saved securely to local device storage.");
//                } else {
//                    Log.e(TAG, "FAILED to save one or more keys to local secure storage. Offline access may require re-entering passphrase.");
//                }
//
//                // --- Post Firebase write to main thread --- // Step 8
//                Log.d(TAG, "Saving user data to Firebase...");
//                Tasks.await(userRef.child(userId).setValue(userData)); // Using Tasks.await for synchronous save
//                firebaseSaveSuccess = true;
//                Log.d(TAG, "User data (profile & keys) saved to Firebase successfully for new user.");
//
//
//            } catch (Exception e) {
//                cryptoException = e;
//                Log.e(TAG, "Error during initial key setup and save process", e);
//                // Clean up any potentially partially saved local keys on error
//                SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
//                YourKeyManager.getInstance().clearKeys();
//            }
//
//
//            // --- Post result back to Main Thread ---
//            final Exception finalCryptoException = cryptoException;
//            final boolean finalFirebaseSaveSuccess = firebaseSaveSuccess;
//            final boolean finalLocalSaveSuccess = localSaveSuccess;
//
//            // *** Ensure these are declared as PublicKey and PrivateKey types ***
//            // Pass the successfully generated keys to update KeyManager on the main thread if successful
//            final PublicKey finalNewPublicKey = publicKey; // Use the variable assigned in try block
//            final PrivateKey finalNewPrivateKey = privateKey; // Use the variable assigned in try block
//            // The ternary operator was correct, but simpler to assign directly here if success is checked below.
//            // If you prefer the ternary, ensure the variables are PublicKey and PrivateKey type.
//
//
//            mainHandler.post(() -> { // This runs on the Main Thread
//                progressDialog.dismiss();
//
//                if (finalCryptoException == null && finalFirebaseSaveSuccess) {
//                    // Success: Keys generated and saved to Firebase
//                    Log.d(TAG, "Profile setup & Account secured successfully for user: " + userId);
//                    Toast.makeText(Setting_profile.this, "Profile setup & Account secured!", Toast.LENGTH_SHORT).show();
//
//                    // --- Update KeyManager with NEW Keys ---
//                    // *** Ensure finalNewPublicKey and finalNewPrivateKey are NOT null before calling setKeys ***
//                    if (finalNewPublicKey != null && finalNewPrivateKey != null) {
//                        Log.d(TAG, "Updating KeyManager with newly generated keys.");
//                        // *** This call needs String, PublicKey, PrivateKey types ***
//                        YourKeyManager.getInstance().setKeys(userId, finalNewPublicKey, finalNewPrivateKey); // Ensure types match
//                        Log.d(TAG, "Keys loaded into KeyManager after new user setup.");
//
//                        // *** Handle RememberMe State Saving After Successful Setup ***
//                        SharedPreferences prefs = getSharedPreferences("CircleUpPrefs", MODE_PRIVATE);
//                        if (finalLocalSaveSuccess) {
//                            prefs.edit().putBoolean("RememberMe", true).apply();
//                            Log.d(TAG, "Setup (Main Thread): RememberMe preference set to TRUE after successful setup and local save.");
//                        } else {
//                            prefs.edit().putBoolean("RememberMe", false).apply();
//                            Log.w(TAG, "Setup (Main Thread): NEW local key save failed despite successful setup. RememberMe preference set FALSE.");
//                            Toast.makeText(Setting_profile.this, "Warning: Could not save keys locally for 'Remember Me'.", Toast.LENGTH_SHORT).show();
//                        }
//                        // *** END Handle RememberMe ***
//
//
//                        // *** Send Account Setup Success Notification to the creator (user) ***
//                        // Ensure sendPushNotification method is accessible here (copied into this class)
//                        // Ensure username and email are available (passed as final variables or fetched again)
//                        Log.d(TAG, "Setup (Main Thread): Sending account setup success notification to creator.");
//                        // Using finalUsername and userEmail passed into the outer method
//                        String title = "Account Setup Complete!";
//                        String message = "Your CircleUp account is now secured.\nUsername: " + finalUsername + "\nEmail: " + userEmail;
//
//                        if (oneSignalApiService != null && userId != null && userEmail != null && finalUsername != null) { // Basic safety checks
//                            sendPushNotification(
//                                    userId, // Recipient is the user themselves
//                                    title,
//                                    message
//                            );
//                        } else {
//                            Log.w(TAG, "Cannot send setup complete notification: API service or user details are null.");
//                        }
//                        // *** END NEW ***
//
//
//                        // Navigate to Main Activity
//                        Log.d(TAG, "Setup (Main Thread): Navigating to MainActivity.");
//                        Intent intent = new Intent(Setting_profile.this, MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                        finish(); // Finish Setting_profile activity
//
//                    } else {
//                        // This should not happen if Firebase save succeeded, but defensive
//                        Log.e(TAG, "Setup (Main Thread): Firebase save succeeded, but new keys are null. Inconsistent state.");
//                        Toast.makeText(Setting_profile.this, "Setup completed, but failed to load new keys. Log in again.", Toast.LENGTH_LONG).show();
//                        sendUserToLoginActivity(); // Redirect to login to re-sync from Firebase
//                    }
//
//                } else {
//                    // Failure occurred during key generation or Firebase save
//                    Log.e(TAG, "Profile setup failed.");
//                    String displayMessage = "Failed to secure account.";
//                    if (finalCryptoException != null) {
//                        displayMessage = "Failed to generate/save keys: " + finalCryptoException.getMessage();
//                    } else if (!finalFirebaseSaveSuccess) {
//                        displayMessage = "Failed to save profile data to server.";
//                    }
//                    Toast.makeText(Setting_profile.this, displayMessage, Toast.LENGTH_LONG).show();
//                    // Stay on Setting_profile to allow user to try again
//                }
//            }); // End mainHandler.post
//        }); // End executorService.execute
//    }






    // In Setting_profile.java

    private void performInitialKeySetupAndSaveProfile(String username, String status, String profileImageBase64, String passphrase) {
        progressDialog.setMessage("Securing account and saving profile...");
        progressDialog.show();
        Log.d(TAG, "Starting initial key setup and profile save for new user.");

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not logged in during new user setup!");
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            sendUserToLoginActivity(); // Redirect to login
            return;
        }
        final String userId = currentUser.getUid(); // Use final for background thread access
        final String userEmail = currentUser.getEmail(); // Use final
        final String finalUsername = username; // Use final
        final String finalStatus = status; // Use final if needed in background (optional)
        final String finalProfileImageBase64 = profileImageBase64; // Use final

        // Capture the isNewUser flag for the background task context
        final boolean isNewUserFlow = this.isNewUser; // Capture the member variable as final

        executorService.execute(() -> {
                    Exception setupException = null; // Changed variable name for clarity
                    String successMessage = null;
                    boolean localSaveSuccess = false;

                    // *** Declare keys and recovery code here to be accessible later ***
                    PublicKey newPublicKey = null;
                    PrivateKey newPrivateKey = null; // Decrypted Private Key
                    byte[] newlyEncryptedPrivateKeyWithIV = null; // Encrypted Private Key with IV
                    String generatedRecoveryCode = null; // New: To hold the generated code
                    // *** End Declaration ***


                    try {
                        Log.d(TAG, "Generating salt."); // Step 1
                        byte[] salt = CryptoUtils.generateSalt();

                        Log.d(TAG, "Deriving encryption key from passphrase."); // Step 2
                        SecretKey encryptionKey = CryptoUtils.deriveKeyFromPassphrase(
                                passphrase, salt, CryptoUtils.PBKDF2_ITERATIONS);

                        Log.d(TAG, "Generating NEW RSA key pair."); // Step 3
                        KeyPair newKeyPair = CryptoUtils.generateRSAKeyPair();
                        newPublicKey = newKeyPair.getPublic(); // Assign
                        newPrivateKey = newKeyPair.getPrivate(); // Assign (Decrypted)

                        // --- NEW STEP: Generate Recovery Code from the Decrypted Private Key ---
                        if (isNewUserFlow) { // Only generate code during initial new user setup
                            updateStatusText("Generating recovery code..."); // Update status (via MainHandler post later)
                            Log.d(TAG, "Attempting to generate recovery code from decrypted private key.");
                            try {
                                // Use the new CryptoUtils method to generate the code
                                generatedRecoveryCode = CryptoUtils.privateKeyToRecoveryCode(newPrivateKey); // Use the DECRYPTED private key
                                Log.d(TAG, "Generated recovery code successfully. Length: " + (generatedRecoveryCode != null ? generatedRecoveryCode.length() : 0));

                                if (TextUtils.isEmpty(generatedRecoveryCode)) {
                                    Log.e(TAG, "Generated recovery code is null or empty!");
                                    // Decide how to handle this failure. For a real app, this is critical.
                                    // We will proceed but the Main Thread logic will show an error.
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Failed to generate recovery code.", e);
                                // This exception will be caught by the outer catch block below.
                                throw e; // Re-throw the exception
                            }
                        } else {
                            Log.d(TAG, "Skipping recovery code generation for existing user update.");
                        }
                        // --- END NEW STEP ---


                        Log.d(TAG, "Encrypting NEW private key with NEW symmetric key."); // Step 4 (was Step 3)
                        newlyEncryptedPrivateKeyWithIV = CryptoUtils.encryptPrivateKey(newPrivateKey, encryptionKey); // Assign


                        Log.d(TAG, "Encoding keys and salt to Base64."); // Step 5 (was Step 4)
                        // Use CryptoUtils methods for encoding crypto data (ensures consistency)
                        String publicKeyBase64 = CryptoUtils.bytesToBase64(CryptoUtils.publicKeyToBytes(newPublicKey));
                        String newlyEncryptedPrivateKeyBase64 = CryptoUtils.bytesToBase64(newlyEncryptedPrivateKeyWithIV);
                        String newSaltBase64 = CryptoUtils.bytesToBase64(salt);

                        // --- Data for Firebase --- // Step 6 (was Step 5)
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", userId);
                        userData.put("email", userEmail);
                        userData.put("username", finalUsername);
                        userData.put("status", finalStatus);
                        userData.put("profileImage", finalProfileImageBase64);
                        userData.put("role", "user");
                        userData.put("isBlocked", false);
                        // Add keys and salt to Firebase data map
                        userData.put("publicKey", publicKeyBase64);
                        userData.put("encryptedPrivateKey", newlyEncryptedPrivateKeyBase64);
                        userData.put("encryptionSalt", newSaltBase64);

                        Map<String, Object> userState = new HashMap<>();
                        userState.put("date", "");
                        userState.put("time", "");
                        userState.put("state", "offline");
                        userData.put("userState", userState);


                        // --- Save to Local Secure Storage --- // Step 7 (was Step 6)
                        updateStatusText("Saving keys locally..."); // Update status
                        Log.d(TAG, "Saving symmetric key and encrypted key pair to local secure storage.");
                        // Use application context for SecureKeyStorageUtil
                        boolean saveSymmetricSuccess = SecureKeyStorageUtil.saveSymmetricKey(getApplicationContext(), encryptionKey);
                        // Use the original encrypted private key bytes (newlyEncryptedPrivateKeyWithIV) and public key bytes
                        boolean saveKeyPairSuccess = SecureKeyStorageUtil.saveEncryptedKeyPair(getApplicationContext(), userId, newlyEncryptedPrivateKeyWithIV, CryptoUtils.publicKeyToBytes(newPublicKey));

                        localSaveSuccess = saveSymmetricSuccess && saveKeyPairSuccess;

                        if (localSaveSuccess) {
                            Log.d(TAG, "NEW keys saved securely to local device storage successfully.");
                        } else {
                            Log.e(TAG, "FAILED to save one or more NEW keys locally for user: " + userId + ". Clearing potentially partial new local data.");
                            // Clear any partially saved new local keys on failure
                            SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
                            // Note: KeyManager state (in-memory) is handled on Main Thread.
                        }


                        // --- Save User Data to Firebase --- // Step 8 (was Step 7)
                        updateStatusText("Saving profile to server..."); // Update status
                        Log.d(TAG, "Saving user data (profile & keys) to Firebase...");
                        // Using Tasks.await to make Firebase save synchronous relative to this background task.
                        Tasks.await(userRef.child(userId).setValue(userData)); // Save the whole map
                        Log.d(TAG, "Firebase User node updated successfully.");

                        // If we reached here without exception, Firebase save was successful
                        successMessage = "Profile setup & Account secured!";


                    } catch (ExecutionException | InterruptedException e) {
                        Log.e(TAG, "Firebase operation failed during initial key setup and save", e);
                        setupException = new Exception("Failed to save profile or key data to server. Network error?", e);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Error processing key data (Base64/format) during initial setup", e);
                        setupException = new Exception("Invalid key data format.", e);
                        SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
                        YourKeyManager.getInstance().clearKeys(); // Ensure in-memory is clean
                    } catch (Exception e) { // Catch any other unexpected errors during crypto ops or saving
                        Log.e(TAG, "An unexpected error occurred during initial key setup and save", e);
                        setupException = new Exception("An unexpected error occurred during setup.", e);
                        SecureKeyStorageUtil.clearAllSecureKeys(getApplicationContext(), userId);
                        YourKeyManager.getInstance().clearKeys(); // Ensure in-memory is clean
                    }


                    // --- Post result back to Main Thread ---
                    final String finalSuccessMessage = successMessage;
                    final String finalErrorMessage = setupException != null ? setupException.getMessage() : null;
                    final boolean finalLocalSaveSuccess = localSaveSuccess;
                    final String finalGeneratedRecoveryCode = generatedRecoveryCode; // Pass the generated code to Main Thread

                    // Pass the successfully generated/decrypted keys to update KeyManager on the main thread IF successful
                    // ONLY pass keys if the entire process (no setupException) was successful.
                    final PublicKey finalNewPublicKey = (setupException == null) ? newPublicKey : null;
                    final PrivateKey finalNewPrivateKey = (setupException == null) ? newPrivateKey : null;

                    // Pass the userId to the main thread
                    final String finalUserId = userId;
                    final String finalUserEmail = userEmail; // Also pass email/username for notification
                    final String finalFinalUsername = finalUsername; // Pass final username

                    mainHandler.post(() -> { // This runs on the Main Thread
                        progressDialog.dismiss(); // Dismiss dialog
                        updateStatusText("Setup process finished."); // Final status update on UI

                        if (finalErrorMessage == null) {
                            // Background task completed successfully
                            Log.d(TAG, "Initial setup background task finished successfully.");
                            Toast.makeText(Setting_profile.this, finalSuccessMessage, Toast.LENGTH_LONG).show();

                            // --- Update KeyManager with NEW Keys ---
                            // Load the newly generated keys into KeyManager for the current session.
                            if (finalNewPublicKey != null && finalNewPrivateKey != null) {
                                Log.d(TAG, "Updating KeyManager with newly generated keys after successful setup.");
                                YourKeyManager.getInstance().setKeys(finalUserId, finalNewPublicKey, finalNewPrivateKey);
                                Log.d(TAG, "Keys loaded into KeyManager.");

                                // *** Handle RememberMe State based on local save success ***
                                SharedPreferences prefs = getSharedPreferences("CircleUpPrefs", MODE_PRIVATE);
                                if (finalLocalSaveSuccess) {
                                    prefs.edit().putBoolean("RememberMe", true).apply();
                                    Log.d(TAG, "Setup (Main Thread): RememberMe preference set to TRUE after successful setup and local save.");
                                } else {
                                    // Local save failed despite successful setup -> Cannot Remember Me
                                    prefs.edit().putBoolean("RememberMe", false).apply();
                                    Log.w(TAG, "Setup (Main Thread): NEW local key save failed despite successful setup. RememberMe preference set FALSE.");
                                    Toast.makeText(Setting_profile.this, "Warning: Could not save keys locally for 'Remember Me'.", Toast.LENGTH_LONG).show();
                                }
                                // *** END Handle RememberMe ***


                                // *** Send Account Setup Success Notification to the creator (user) ***
                                Log.d(TAG, "Setup (Main Thread): Sending account setup success notification to creator.");
                                // Use the final username and email passed from the background thread
                                String title = "Account Setup Complete!";
                                String message = "Your CircleUp account is now secured.\nUsername: " + finalFinalUsername + "\nEmail: " + finalUserEmail;

                                if (oneSignalApiService != null && finalUserId != null && finalUserEmail != null && finalFinalUsername != null) { // Basic safety checks
                                    sendPushNotification(
                                            finalUserId, // Recipient is the user themselves
                                            title,
                                            message
                                    );
                                } else {
                                    Log.w(TAG, "Cannot send setup complete notification: API service or user details are null.");
                                }
                                // *** END Notification ***


                                // --- NEW LOGIC: SHOW RECOVERY CODE UI OR NAVIGATE ---
                                // This logic determines the next screen based on whether it's a new user
                                // and if the recovery code was generated successfully.

                                // Check if it was the new user setup flow AND if a recovery code was generated.
                                if (isNewUserFlow && !TextUtils.isEmpty(finalGeneratedRecoveryCode)) {
                                    // It's the initial setup for a new user AND recovery code is available.
                                    Log.d(TAG, "Initial setup successful, recovery code generated. Showing recovery code UI.");

                                    // Show the recovery code section and hide elements related to saving/passphrase input
                                    passphraseInputLayout.setVisibility(View.GONE); // Hide passphrase input
                                    signup_card.setVisibility(View.GONE); // Hide the card if it wraps passphrase
                                    saveButton.setVisibility(View.GONE); // Hide the original "Save" button

                                    recoveryCodeLayout.setVisibility(View.VISIBLE); // Show the recovery code layout
                                    textViewRecoveryCode.setText(finalGeneratedRecoveryCode); // Display the code
                                    spacerAfterRecoveryCode.setVisibility(View.VISIBLE); // Show spacer

                                    Button buttonProceedToApp = findViewById(R.id.button_proceed_to_app); // Find the button (already done in initViews)
                                    if(buttonProceedToApp != null){
                                        buttonProceedToApp.setVisibility(View.VISIBLE); // Show the proceed button
                                        // Click listener for "Copy Code" button (already in initViews)
                                        buttonCopyRecoveryCode.setOnClickListener(v -> {
                                            copyRecoveryCodeToClipboard(finalGeneratedRecoveryCode); // Use the final code
                                        });
                                        // Click listener for "Proceed" button (navigates to MainActivity)
                                        buttonProceedToApp.setOnClickListener(v -> {
                                            Log.d(TAG, "Proceed to App button clicked. Navigating to MainActivity.");
                                            Intent intent = new Intent(Setting_profile.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            startActivity(intent);
                                            finish(); // Finish Setting_profile after navigating away
                                        });
                                    } else {
                                        Log.e(TAG, "Proceed to App button not found in layout!");
                                        // Fallback: maybe show a toast or auto-navigate after a delay (less safe)
                                        Toast.makeText(Setting_profile.this, "Please save your code and restart the app to proceed.", Toast.LENGTH_LONG).show();
                                        // Don't finish the activity automatically, let user handle.
                                    }


                                    updateStatusText("Account secured. Save your recovery code and proceed."); // Update status text

                                } else {
                                    // This case is either:
                                    // 1. An existing user updating their profile (isNewUserFlow is false) - Standard save path
                                    // 2. A new user setup attempt, but recovery code generation failed (finalGeneratedRecoveryCode is null/empty)
                                    Log.d(TAG, "Navigating to MainActivity. isNewUserFlow: " + isNewUserFlow + ", Recovery Code Generated: " + (!TextUtils.isEmpty(finalGeneratedRecoveryCode)));

                                    // If recovery code generation failed for a new user, log an error and show a warning toast.
                                    if (isNewUserFlow && TextUtils.isEmpty(finalGeneratedRecoveryCode)) {
                                        Log.e(TAG, "Recovery code was NOT generated successfully during setup. Proceeding to app, but recovery via code is unavailable.");
                                        Toast.makeText(Setting_profile.this, "Warning: Failed to generate recovery code. Account secured, but recovery via code is unavailable. Consider changing passphrase later.", Toast.LENGTH_LONG).show();
                                    }

                                    // Navigate directly to MainActivity (standard flow for existing users or failed code gen)
                                    Intent intent = new Intent(Setting_profile.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear previous activities
                                    startActivity(intent);
                                    finish(); // Finish Setting_profile

                                }
                                // --- END NEW LOGIC ---


                            } else {
                                // Background task reported an error during setup (Firebase save failed, crypto error, etc.)
                                Log.e(TAG, "Initial setup failed. Showing error: " + finalErrorMessage);
                                String displayMessage = finalErrorMessage != null ? finalErrorMessage : "Unknown error setting up profile.";
                                Toast.makeText(Setting_profile.this, displayMessage, Toast.LENGTH_LONG).show();

                                // Ensure UI is re-enabled (already done at the start of mainHandler.post)
                                // Ensure passphrase fields are cleared if it was a validation/crypto error related to passphrase?
                                // For now, keep inputs as they are so user can correct them if needed.
                                // The KeyManager and local storage state should already be cleaned by the executor task on failure.
                            }
                        }; // End mainHandler.post
                    }); // End executorService.execute
                });
    }

// --- Add Helper Method to Copy Code to Clipboard ---
        private void copyRecoveryCodeToClipboard(String code) {
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, "No recovery code to copy.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Attempted to copy empty recovery code.");
                return;
            }

            // Use getSystemService with Context.CLIPBOARD_SERVICE
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                // Create a ClipData object
                ClipData clip = ClipData.newPlainText("CircleUp Recovery Code", code); // Label the copied data
                // Set the ClipData as the primary clip on the clipboard
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Recovery code copied to clipboard.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Recovery code copied to clipboard.");
            } else {
                Toast.makeText(this, "Failed to copy code.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to get ClipboardManager service.");
            }
        }


    private void updateStatusText(String message) {
        mainHandler.post(() -> { // Ensure this runs on the main thread
            if (setupStatusTextView != null) {
                setupStatusTextView.setText(message);
                // Show the TextView only if there's text to display
                setupStatusTextView.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
            }
            // Always log for debugging, regardless of TextView presence
            Log.d(TAG, "Status Update: " + message);
        });
    }
// --- End Helper Method ---


// ... rest of Setting_profile.java


    // --- Copy the sendPushNotification helper method from Login.java or ChatPageActivity.java ---
    // This method sends the actual push notification via OneSignal API.
    // Ensure ONESIGNAL_APP_ID member variable is set correctly at the top of this class.
    private void sendPushNotification(String recipientFirebaseUID, String title, String messageContent) {
        // Check if API service is initialized and recipient UID is valid
        if (oneSignalApiService == null || TextUtils.isEmpty(recipientFirebaseUID)) {
            Log.e(TAG, "sendPushNotification (Setup): API service not initialized or recipient UID is empty. Cannot send notification.");
            return; // Cannot send notification
        }

        Log.d(TAG, "Preparing to send push notification (Setup) to recipient UID (External ID): " + recipientFirebaseUID);

        JsonObject notificationBody = new JsonObject();

        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID);

        JsonArray externalUserIds = new JsonArray();
        externalUserIds.add(recipientFirebaseUID); // Target the user themselves
        notificationBody.add("include_external_user_ids", externalUserIds);

        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", title)));
        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", messageContent)));

        // Optional: Add custom data - for setup success
        JsonObject data = new JsonObject();
        data.addProperty("eventType", "account_setup_complete"); // Custom event type
        data.addProperty("userId", recipientFirebaseUID); // User ID
        // You could add username/email to data too if you want to handle it in NotificationOpenedHandler

        notificationBody.add("data", data);
        notificationBody.addProperty("small_icon", "app_icon_circleup"); // Use your small icon resource name

        // --- Make the API call ---
        Log.d(TAG, "Making API call to OneSignal (Setup)...");
        oneSignalApiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "OneSignal API call successful (Setup). Response Code: " + response.code());
                    try (ResponseBody responseBody = response.body()) {
                        String resBody = responseBody != null ? responseBody.string() : "N/A";
                        Log.d(TAG, "OneSignal API Response Body (Setup): " + resBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read success response body (Setup)", e);
                    }
                } else {
                    Log.e(TAG, "OneSignal API call failed (Setup). Response Code: " + response.code());
                    try (ResponseBody errorBody = response.errorBody()) {
                        String errBody = errorBody != null ? errorBody.string() : "N/A";
                        Log.e(TAG, "OneSignal API Error Body (Setup): " + errBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read error response body (Setup)", e);
                    }
                    Log.w(TAG, "Push notification failed via OneSignal API (Setup).");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "OneSignal API call failed (network error) (Setup)", t);
                Log.w(TAG, "Failed to send push notification due to network error (Setup).");
            }
        });
        Log.d(TAG, "OneSignal API call enqueued (Setup).");
    }
    // --- END Copy ---

    // ... (other methods like onDestroy) ...


    private String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, IMAGE_COMPRESSION_QUALITY, baos);
        byte[] byteArray = baos.toByteArray();
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }

    private Bitmap decodeBase64(String input) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(input, android.util.Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid Base64 string for decoding profile image", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error decoding profile image Base64", e);
            return null;
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.d(TAG, "Original bitmap dimensions: " + width + "x" + height);

        if (width <= maxWidth && height <= maxHeight) {
            Log.d(TAG, "No resizing needed.");
            return bitmap;
        }

        float scale = Math.min(((float) maxWidth / width), ((float) maxHeight / height));

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        Log.d(TAG, "Resizing to dimensions: " + newWidth + "x" + newHeight + " with scale: " + scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }


    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity.");
        Intent loginIntent = new Intent(Setting_profile.this, Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            Log.d(TAG, "ExecutorService shutting down.");
            executorService.shutdownNow();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Log.d(TAG, "Setting_profile onDestroy called.");
    }
}




