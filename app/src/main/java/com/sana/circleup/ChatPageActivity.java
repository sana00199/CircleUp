
package com.sana.circleup;

// --- Existing Imports ---



// Add these imports at the top of ChatPageActivity.java


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sana.circleup.encryptionfiles.CryptoUtils;
import com.sana.circleup.encryptionfiles.YourKeyManager;
import com.sana.circleup.one_signal_notification.OneSignalApiService;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.ConversationKeyDao;
import com.sana.circleup.room_db_implement.ConversationKeyEntity;
import com.sana.circleup.room_db_implement.MessageDao;
import com.sana.circleup.room_db_implement.MessageEntity;
import com.sana.circleup.room_db_implement.WallpaperDao;
import com.sana.circleup.room_db_implement.WallpaperEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ChatPageActivity extends AppCompatActivity implements
        MessageAdapter.OnMessageLongClickListener {

    private static final String TAG = "ChatPageActivity";

    // --- Existing members ---
    private String messageReceiverID, messageReceiverName, messageReceiverImage;
    private TextView userName, userLastSeen;
    private CircleImageView userProfileImage; // Use full package name here
    private ImageButton sendMessageButton, send_imgmsg_btn;
    private EditText messageInputText;
    private Toolbar chatToolbar;
    private FirebaseAuth mAuth;


    // ... (existing members) ...
    // --- New members for Crypto ---
    private String conversationId; // Unique ID for THIS chat
    // Removed: private SecretKey conversationAESKey; // The decrypted symmetric key for this chat - Now fetched dynamically
    // Removed: private boolean isSecureChatEnabled = false; // Flag to indicate if keys are available

    // Removed: private boolean isSecureChatEnabled = false; // Flag to indicate if keys are available
    private boolean wasUiInitiallyDisabled = false; // *** Make sure THIS line is here ***
    // --- End New members ---
    // ... (rest of members) ...

    private DatabaseReference rootRef;
    private RecyclerView messagesList;

    private MessageAdapter messageAdapter;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    // messagesArrayList stores MessageEntity objects with *decrypted* message content or placeholders for the adapter
    private final List<MessageEntity> messagesArrayList = new ArrayList<>();
    private ChildEventListener messageListener; // Firebase listener for messages

    private ChatDatabase db;
    private MessageDao messageDao;
    private ExecutorService databaseWriteExecutor;
    private LiveData<List<MessageEntity>> chatMessagesLiveData;

    private RelativeLayout mainChatLayout;
    private static final int REQUEST_CODE_PERMISSION = 101;
    private static final int REQUEST_CODE_PICK_IMAGE_WALLPAPER = 102;
    private static final int REQUEST_CODE_TAKE_PHOTO_WALLPAPER = 103;
    private Uri cameraPhotoUri;

    private WallpaperDao wallpaperDao;

    private String currentUserName; // Sender's name for notifications

    private String messageSenderID;
    private String messageSenderImage; // Sender's image (optional)
    // In ChatPageActivity.java

// ... existing member variables ...

    // Activity Result Launchers for Image Selection
    private ActivityResultLauncher<Intent> pickImageLauncher; // For Gallery (ActivityResultContracts.StartActivityForResult)
    private ActivityResultLauncher<Uri> takePictureLauncher; // For Camera (ActivityResultContracts.TakePicture)
    private ActivityResultLauncher<String> requestCameraPermissionLauncher; // For requesting CAMERA permission


    // Variables to hold the URI and Base64 string of the image to be sent
    private Uri imageToSendUri; // URI of the image to be sent
    private String imageToSendBase64 = ""; // Base64 string of the image to be sent (for message payload)

    // Optional: Maximum size for image processing before sending
    private static final int MAX_IMAGE_SEND_SIZE = 1024; // Max dimension (e.g., 1024x1024)
    private static final int IMAGE_SEND_COMPRESSION_QUALITY = 85; // JPEG compression quality (e.g., 85%)


// ... existing member variables ...


    private RecyclerView.OnScrollListener recyclerViewScrollListener;

    // --- New members for Crypto (Simplified) ---

    // Removed: private SecretKey conversationAESKey; // No longer storing key here, fetch from KeyManager
    // Removed: private boolean isSecureChatEnabled = false; // KeyManager state check replaces this flag

    // *** NEW MEMBER: Retrofit Service for OneSignal API ***
    private OneSignalApiService oneSignalApiService;
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR APP ID
    // *** END NEW MEMBER ***

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showToastAndExit("Error: User not authenticated!");
            return;
        }
        messageSenderID = currentUser.getUid();


        // --- Get data from Intent ---
        if (getIntent() != null && getIntent().getExtras() != null) {
            // Get conversationId
            conversationId = getIntent().getStringExtra("conversationId");
            // Get recipient details
            messageReceiverID = getIntent().getStringExtra("visit_users_ids");
            messageReceiverName = getIntent().getStringExtra("visit_users_name");
            messageReceiverImage = getIntent().getStringExtra("visit_users_image");


            Log.d(TAG, "ChatPageActivity onCreate: Received Intent with Recipient ID: " + messageReceiverID + ", Conversation ID: " + conversationId);


            if (TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messageReceiverID) || TextUtils.isEmpty(messageReceiverName)) {
                showToastAndExit("Error: Missing chat details!");
                return;
            }
            Log.d(TAG, "Opened chat for conversationId: " + conversationId + ", Recipient ID: " + messageReceiverID);

        } else {
            showToastAndExit("Error: Intent data is null");
            return;
        }
        // --- End Get data from Intent ---

        // --- Initialize Firebase DB Ref, Room DB, and Executors ---
        rootRef = FirebaseDatabase.getInstance().getReference();
        db = ChatDatabase.getInstance(this);
        messageDao = db.messageDao();
        databaseWriteExecutor = ChatDatabase.databaseWriteExecutor;
        wallpaperDao = db.wallpaperDao(); // Initialize WallpaperDao here
        // --- End Initialization ---


        InitializeControllers(); // Initialize UI elements
        loadSavedWallpaper();


        userName.setText(messageReceiverName); // Set recipient's name in toolbar
        loadProfileImage(messageReceiverImage, userProfileImage); // Load recipient's image


        DisplayLastSeen(messageReceiverID); // Display recipient's last seen status

        fetchCurrentUserName(); // Call the helper method to fetch sender's name


        // --- *** NEW: Initialize Retrofit Service for OneSignal API *** ---
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    // Base URL for OneSignal API
                    .baseUrl("https://onesignal.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            oneSignalApiService = retrofit.create(OneSignalApiService.class);
            Log.d(TAG, "OneSignalApiService initialized.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignalApiService", e);
        }
        // --- *** END NEW *** ---


        // --- *** Check Initial Security Status & Set UI *** ---
        Log.d(TAG, "ChatPageActivity onCreate: Checking initial key status from YourKeyManager.");
        // Check if the user's own private key is available AND the single conversation key is available in KeyManager
        boolean isInitialSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId); // *** MODIFIED CHECK ***


        if (isInitialSecureChatAvailable) {
            Log.d(TAG, "Initial check: Keys available. Secure chat enabled.");
            Toast.makeText(this, "Secure chat enabled.", Toast.LENGTH_SHORT).show();
            // UI is enabled by default, nothing to do here
            wasUiInitiallyDisabled = false; // UI was NOT disabled initially
        } else {
            Log.w(TAG, "Initial check: Keys NOT available. Secure chat DISABLED.");

            if (!YourKeyManager.getInstance().isPrivateKeyAvailable()) {
                Toast.makeText(this, "Your account is not unlocked for secure chat. Messages cannot be encrypted/decrypted.", Toast.LENGTH_LONG).show();
            } else if (!YourKeyManager.getInstance().hasConversationKey(conversationId)) { // Check if conversation key is missing specifically
                Toast.makeText(this, "Secure chat key missing for this conversation. Messages will not be encrypted/decrypted.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Secure chat unavailable.", Toast.LENGTH_LONG).show();
            }


            // --- Disable UI for sending messages ---
            messageInputText.setEnabled(false);
            sendMessageButton.setEnabled(false);
            send_imgmsg_btn.setEnabled(false);
            messageInputText.setHint("Secure chat unavailable");
            // --- End Disable UI ---

            wasUiInitiallyDisabled = true; // UI *was* disabled initially
        }
        // --- *** End Fetch Key and Check Initial Security Status & Set UI *** ---


        // --- *** Load Messages from Room DB using LiveData *** ---
        // We will load MessageEntity objects (containing encrypted data if sent/received encrypted)
        // Decryption will happen in the LiveData observer by calling forceRefreshDisplay().
        chatMessagesLiveData = messageDao.getMessagesForChat(messageSenderID, messageReceiverID); // Assuming this query filters by owner and participants

        // Observe LiveData. When data changes in Room, trigger a display refresh.
        chatMessagesLiveData.observe(this, messages -> {
            Log.d(TAG, "LiveData onChanged triggered for owner " + messageSenderID + ". Calling forceRefreshDisplay.");
            forceRefreshDisplay(); // This handles decryption and UI update
        });
        // --- *** End Load Messages from Room DB *** ---


        // --- *** Modify Button Click Listeners to call new SendMessage variants *** ---
        sendMessageButton.setOnClickListener(v -> SendTextMessage());
        send_imgmsg_btn.setOnClickListener(v -> {
            // Ensure secure chat keys are available before picking image for encryption
            // Check dynamically here, don't rely solely on initial flag
            boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
            if (!isSecureChatAvailable) {
                Toast.makeText(this, "Secure chat is not enabled to send images.", Toast.LENGTH_SHORT).show();
                return; // Do not proceed if secure chat keys are off
            }
//            Intent intent = new Intent();
//            intent.setType("image/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(intent, PICK_IMAGE_REQUEST);


            showImageSourceDialog();


        });
        // --- *** End Modify Button Click Listeners *** ---

        initializeImagePickers();

        // Initialize the permission launcher
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), // Contract for requesting a single permission
                isGranted -> { // Callback when permission request is finished
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted. Proceeding to open camera.");
                        // Permission was granted, now actually launch the camera intent
                        launchCameraIntent(); // Call the helper method to launch camera
                    } else {
                        Log.w(TAG, "Camera permission denied.");
                        // Permission denied, inform the user
                        Toast.makeText(this, "Camera permission denied. Cannot take photo.", Toast.LENGTH_SHORT).show();
                    }
                });
        // --- END NEW ---




        attachScrollListener();
        messageAdapter.setOnMessageLongClickListener(this); // Set the long click listener on the adapter
    }


    // Method to initialize UI elements (Keep as is)
    private void InitializeControllers() {
        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        View actionBarView = null;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            actionBarView = LayoutInflater.from(this).inflate(R.layout.custom_chat_bar, null);
            getSupportActionBar().setCustomView(actionBarView);

            userName = actionBarView.findViewById(R.id.custom_profile_name);
            userLastSeen = actionBarView.findViewById(R.id.custom_user_last_seen);
            userProfileImage = actionBarView.findViewById(R.id.custom_profile_img);

            ImageView backButton = actionBarView.findViewById(R.id.back_button);
            if (backButton != null) {
                backButton.setOnClickListener(v -> onBackPressed());
            } else {
                Log.w(TAG, "Back button not found in custom_chat_bar layout.");
            }

        } else {
            Log.w(TAG, "SupportActionBar is null. Cannot set custom view.");
        }


        // profileClickArea.setOnClickListener(...)
        if (actionBarView != null) { // Check if actionBarView was inflated
            actionBarView.setOnClickListener(v -> showToolbarOptionsDialog());
        } else {
            Log.w(TAG, "actionBarView is null, cannot set click listener for toolbar options.");
        }


        mainChatLayout = findViewById(R.id.main_chat_layout);
        db = ChatDatabase.getInstance(this);
        wallpaperDao = db.wallpaperDao();

        sendMessageButton = findViewById(R.id.send_msg_btn);
        send_imgmsg_btn = findViewById(R.id.send_imgmsg_btn);
        messageInputText = findViewById(R.id.input_msg);
        messagesList = findViewById(R.id.privatemsges_list_of_users);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(linearLayoutManager);
        messagesList.setHasFixedSize(true);


        // Initialize the MessageAdapter. It uses the messagesArrayList which will be populated by LiveData.
        messageAdapter = new MessageAdapter(messagesArrayList, this, messageReceiverImage, messageSenderID);
        messagesList.setAdapter(messageAdapter);
    }


    // Helper method to fetch current user's name (Keep as is)
    private void fetchCurrentUserName() { /* ... (existing method) ... */
        if (TextUtils.isEmpty(messageSenderID)) {
            Log.w(TAG, "fetchCurrentUserName: messageSenderID is empty, cannot fetch name.");
            currentUserName = "You"; // Default if ID is missing
            return;
        }

        Log.d(TAG, "Fetching current user's name for UID: " + messageSenderID);
        DatabaseReference currentUserRef = rootRef.child("Users").child(messageSenderID);

        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    String name = snapshot.child("username").getValue(String.class);
                    if (!TextUtils.isEmpty(name)) {
                        currentUserName = name; // Store the fetched name
                        Log.d(TAG, "Fetched current user name: " + currentUserName);
                    } else {
                        currentUserName = "You"; // Default if username field is empty
                        Log.w(TAG, "Current user's username field is empty. Using default.");
                    }
                } else {
                    currentUserName = "You"; // Default if user data or username field is missing
                    Log.w(TAG, "Current user data or username field not found in DB. Using default.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch current user name from DB", error.toException());
                currentUserName = "You"; // Default on error
            }
        });
    }


    // Helper method to show toolbar options dialog (Keep as is)
    private void showToolbarOptionsDialog() { /* ... (existing method) ... */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chat Options");

        final CharSequence[] options = {"Clear Chat", "See Profile", "Change Wallpaper"}; // Define your options

        builder.setItems(options, (dialog, which) -> {
            String selectedOption = options[which].toString();

            if ("Clear Chat".equals(selectedOption)) {
                Log.d(TAG, "Selected: Clear Chat");
                showClearChatConfirmationDialog(); // Show a confirmation dialog before clearing
            } else if ("See Profile".equals(selectedOption)) {
                Log.d(TAG, "Selected: See Profile");
                // Start the ProfileUserInfoActivity for the message receiver
                Intent profileIntent = new Intent(ChatPageActivity.this, ProfileUserInfoActivity.class);
                // Pass the receiver's user ID using the key expected by ProfileUserInfoActivity
                profileIntent.putExtra("visit_user_id", messageReceiverID);
                startActivity(profileIntent);
            }

            // Handle "Change Wallpaper" option
            else if ("Change Wallpaper".equals(selectedOption)) {
                Log.d(TAG, "Selected: Change Wallpaper");
                showChangeWallpaperDialog(); // Call the new method
            }
            // Handle other options if you add them
        });

        builder.show(); // Show the dialog
    }


    // Helper method to show clear chat confirmation dialog (Keep as is)
    private void showClearChatConfirmationDialog() { /* ... (existing method) ... */
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Are you sure you want to clear all messages in this chat for you?")
                .setPositiveButton("Yes, Clear", (dialog, which) -> {
                    Log.d(TAG, "Confirmation received: Clearing chat.");
                    clearChatLocally(); // Call method to delete messages from Room DB
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d(TAG, "Clear chat cancelled.");
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Optional: Add an alert icon
                .show();
    }

    // Helper method to clear chat messages from Room DB (Keep as is)
    private void clearChatLocally() { /* ... (existing method) ... */
        if (TextUtils.isEmpty(messageSenderID) || TextUtils.isEmpty(messageReceiverID) || messageDao == null) {
            Log.e(TAG, "Cannot clear chat locally: messageSenderID, messageReceiverID, or messageDao is null/empty.");
            Toast.makeText(this, "Error clearing chat.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseWriteExecutor.execute(() -> {
            int deletedRows = messageDao.deleteAllMessagesForChat(messageSenderID, messageReceiverID);

            runOnUiThread(() -> {
                if (deletedRows > 0) {
                    Log.d(TAG, "Cleared " + deletedRows + " messages from Room DB for owner " + messageSenderID + " in chat with " + messageReceiverID);
                    Toast.makeText(ChatPageActivity.this, "Chat cleared.", Toast.LENGTH_SHORT).show();
                    // LiveData observer will automatically update the RecyclerView UI
                } else {
                    Log.w(TAG, "Attempted to clear chat for owner " + messageSenderID + " with " + messageReceiverID + ", but no messages were found to delete.");
                    Toast.makeText(ChatPageActivity.this, "No messages to clear.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Helper method to load profile image (Keep as is, uses android.util.Base64 which is fine for images)
    private void loadProfileImage(String imageString, CircleImageView imageView) { /* ... (existing method) ... */
        if (imageView == null) {
            Log.e(TAG, "Cannot load profile image, target ImageView is null.");
            return;
        }
        if (!TextUtils.isEmpty(imageString)) {
            if (imageString.startsWith("http")) {
                Glide.with(this).load(imageString).placeholder(R.drawable.default_profile_img).into(imageView);
            } else {
                try {
                    // Use android.util.Base64 for image data (not crypto data)
                    Glide.with(this).load("data:image/jpeg;base64," + imageString).placeholder(R.drawable.default_profile_img).into(imageView);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading Base64 image with Glide", e);
                    try {
                        byte[] decodedBytes = Base64.decode(imageString, Base64.DEFAULT); // Keep using android.util.Base64 for image data
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(R.drawable.default_profile_img);
                        }
                    } catch (Exception decodeEx) {
                        Log.e(TAG, "Error decoding Base64 image manually", decodeEx);
                        imageView.setImageResource(R.drawable.default_profile_img);
                    }
                }
            }
        } else {
            imageView.setImageResource(R.drawable.default_profile_img);
        }
    }


    // Method to mark messages currently visible on screen as "seen" in Firebase (Keep as is)
    private void markVisibleMessagesAsSeen() { /* ... (existing method) ... */
        if (TextUtils.isEmpty(conversationId) || !getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            Log.d(TAG, "Skipping markVisibleMessagesAsSeen: ConvId missing or activity not resumed.");
            return;
        }

        if (messagesList == null || messagesList.getLayoutManager() == null || messageAdapter == null || messagesArrayList.isEmpty()) {
            Log.d(TAG, "Skipping markVisibleMessagesAsSeen: LayoutManager, adapter, or list is null/empty.");
            return;
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) messagesList.getLayoutManager();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        if (firstVisibleItemPosition == RecyclerView.NO_POSITION || lastVisibleItemPosition == RecyclerView.NO_POSITION) {
            Log.d(TAG, "Skipping markVisibleMessagesAsSeen: No visible items.");
            return;
        }

        Log.d(TAG, "Checking visible items from " + firstVisibleItemPosition + " to " + lastVisibleItemPosition + " for unseen incoming messages.");
        for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
            if (i < 0 || i >= messagesArrayList.size()) {
                Log.w(TAG, "Index out of bounds during visible item check: " + i);
                continue;
            }

            MessageEntity message = messagesArrayList.get(i);

            if (message != null
                    && !message.isSeen()
                    && message.getFrom().equals(messageReceiverID)
                    && message.getTo().equals(messageSenderID)
                    && !TextUtils.isEmpty(message.getFirebaseMessageId()))
            {
                Log.d(TAG, "Visible incoming message found to mark seen: " + message.getFirebaseMessageId() + " (from: " + message.getFrom() + ", seen: " + message.isSeen() + ", status: " + message.getStatus() + ")");
                markSingleMessageAsSeen(message.getFirebaseMessageId(), messageSenderID, messageReceiverID);
            }
        }
        Log.d(TAG, "Finished checking visible items for unseen incoming messages.");
    }


    // Method to remove the Firebase message listener (Keep as is)
    private void removeMessageListener() { /* ... (existing method) ... */
        if (messageListener != null && !TextUtils.isEmpty(conversationId)) {
            rootRef.child("Messages").child(conversationId)
                    .removeEventListener(messageListener);
            messageListener = null;
            Log.d(TAG, "Firebase listener removed for chat path: Messages/" + conversationId);
        } else {
            Log.d(TAG, "Firebase listener is null or conversationId is empty, nothing to remove.");
        }
    }

    // Method to display the recipient's last seen status (Keep as is)
    private void DisplayLastSeen(String chatPartnerId) { /* ... (existing method) ... */
        if (!TextUtils.isEmpty(chatPartnerId)) {
            rootRef.child("Users").child(chatPartnerId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.child("userState").exists()) {
                        String state = dataSnapshot.child("userState").child("state").getValue(String.class);
                        String date = dataSnapshot.child("userState").child("date").getValue(String.class);
                        String time = dataSnapshot.child("userState").child("time").getValue(String.class);

                        if ("online".equals(state)) {
                            userLastSeen.setText("Online");
                        } else if (!TextUtils.isEmpty(date) && !TextUtils.isEmpty(time)) {
                            userLastSeen.setText("Last Seen: " + date + " at " + time);
                        } else {
                            userLastSeen.setText("Offline");
                        }
                    } else {
                        userLastSeen.setText("Offline");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database Error (DisplayLastSeen): " + databaseError.getMessage());
                    userLastSeen.setText("Status Unavailable");
                }
            });
        } else {
            Log.w(TAG, "Cannot display last seen, chatPartnerId is empty.");
            userLastSeen.setText("Status Unavailable");
        }
    }

    // Helper method to show a toast message and finish the activity (Keep as is)
    private void showToastAndExit(String message) { /* ... (existing method) ... */
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }


    // --- Modified Method to Send Text Message (Includes Encryption) ---
    private void SendTextMessage() {
        String messageText = messageInputText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please enter a message!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- *** CHECK if Secure Chat is Enabled (Keys are available) *** ---
        boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId); // *** MODIFIED CHECK ***

        // Get the *single* conversation key from KeyManager
        SecretKey conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId); // *** MODIFIED ***

        if (!isSecureChatAvailable || conversationAESKey == null) { // Check if key is null explicitly
            Log.w(TAG, "Cannot send message: Secure chat keys are not available.");
            if (!YourKeyManager.getInstance().isPrivateKeyAvailable()) {
                Toast.makeText(this, "Your account is not unlocked for secure chat.", Toast.LENGTH_SHORT).show();
            } else {
                // This case should imply YourKeyManager.hasConversationKey was false, but good to be specific
                Toast.makeText(this, "Secure chat key missing for this conversation.", Toast.LENGTH_SHORT).show();
            }
            return; // Do not proceed if keys are not available
        }
        // --- *** END Check *** ---

        String sendTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()); // Formatted local time
        Object firebaseTimestamp = ServerValue.TIMESTAMP; // Firebase ServerValue for consistent time
        long localTimestamp = System.currentTimeMillis(); // Local timestamp for initial Room order

        DatabaseReference messagesRef = rootRef.child("Messages").child(conversationId).push(); // Use conversationId here!
        String messagePushId = messagesRef.getKey();

        if (messagePushId == null) {
            Log.e(TAG, "Firebase push key generation failed. Cannot send message.");
            Toast.makeText(this, "Error sending message: Failed to generate ID.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if ID is null
        }
        Log.d(TAG, "Generated Firebase push ID: " + messagePushId);

        // --- *** ENCRYPT the message content and get the Base64 STRING *** ---
        byte[] encryptedBytes; // Will get raw bytes from encryption
        String encryptedMessageContentBase64; // Will be the Base64 String for storage/sending
        try {
            // Use the NEW encryptMessageWithAES which returns byte[]
            encryptedBytes = CryptoUtils.encryptMessageWithAES(messageText, conversationAESKey); // <-- MODIFIED

            // Immediately encode this byte[] into a Base64 String using CryptoUtils.bytesToBase64
            encryptedMessageContentBase64 = CryptoUtils.bytesToBase64(encryptedBytes); // *** NEW STEP ***

            if (TextUtils.isEmpty(encryptedMessageContentBase64)) { // Defensive check
                Log.e(TAG, "Base64 encoding of encrypted text message failed.");
                throw new Exception("Base64 encoding failed."); // Throw to catch below
            }

            Log.d(TAG, "Text message encrypted and Base64 encoded. Base64 length: " + encryptedMessageContentBase64.length());
        } catch (Exception e) { // Catch any encryption or encoding errors
            Log.e(TAG, "Failed to encrypt/encode text message", e);
            Toast.makeText(this, "Failed to encrypt message.", Toast.LENGTH_SHORT).show();
            return; // Do not send if encryption/encoding fails
        }
        // --- *** END ENCRYPTION *** ---


        // Create MessageEntity for Room (store the *encrypted Base64 string* and use local timestamp)
        MessageEntity messageToSaveLocally = new MessageEntity();
        messageToSaveLocally.setFirebaseMessageId(messagePushId);
        messageToSaveLocally.setOwnerUserId(messageSenderID);
        messageToSaveLocally.setMessage(encryptedMessageContentBase64); // Store the encrypted Base64 string <-- MODIFIED
        messageToSaveLocally.setType("text");
        messageToSaveLocally.setFrom(messageSenderID);
        messageToSaveLocally.setTo(messageReceiverID);
        messageToSaveLocally.setSendTime(sendTime);
        messageToSaveLocally.setSeen(false);
        messageToSaveLocally.setSeenTime("");
        messageToSaveLocally.setStatus("pending");
        messageToSaveLocally.setTimestamp(localTimestamp);


        // --- Insert into Room DB first with "pending" status (runs on background thread) ---
        databaseWriteExecutor.execute(() -> {
            try {
                messageDao.insertMessage(messageToSaveLocally);
                Log.d(TAG, "Inserted pending text message into Room (encrypted Base64) for owner " + messageSenderID + ": " + messagePushId + ", Local Timestamp: " + localTimestamp); // Updated log
            } catch (Exception e) {
                Log.e(TAG, "Error inserting pending message into Room: " + messagePushId, e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error saving message locally.", Toast.LENGTH_SHORT).show());
                // Proceeding to Firebase might be better if Room save is non-critical.
            }


            // --- Send message to Firebase (runs on background thread after Room insert) ---
            DatabaseReference messagePathRef = rootRef.child("Messages").child(conversationId).child(messagePushId);

            Map<String, Object> messageFirebaseBody = new HashMap<>();
            messageFirebaseBody.put("message", encryptedMessageContentBase64); // Send the encrypted Base64 string <-- MODIFIED
            messageFirebaseBody.put("type", "text");
            messageFirebaseBody.put("from", messageSenderID);
            messageFirebaseBody.put("to", messageReceiverID);
            messageFirebaseBody.put("seen", false);
            messageFirebaseBody.put("seenTime", "");
            messageFirebaseBody.put("sendTime", sendTime);
            messageFirebaseBody.put("timestamp", firebaseTimestamp);


            messagePathRef.setValue(messageFirebaseBody).addOnCompleteListener(task -> {
                Log.d(TAG, "Firebase setValue for text message " + messagePushId + " completed. Success: " + task.isSuccessful());

                if (task.isSuccessful()) {
                    // --- Update Chat Summaries for BOTH users ---
                    // Pass the encrypted Base64 string for the preview
                    updateChatSummaryForUser(
                            messageSenderID, // Owner 1 (Sender)
                            messageReceiverID, // Partner 1 (Receiver)
                            conversationId,
                            messagePushId,
                            encryptedMessageContentBase64, // Encrypted preview for sender's summary <-- MODIFIED
                            "text", // Message Type
                            firebaseTimestamp,
                            messageSenderID // Sender of THIS message
                    );

                    // For the receiver's summary, use the same encrypted preview
                    updateChatSummaryForUser(
                            messageReceiverID, // Owner 2 (Receiver)
                            messageSenderID, // Partner 2 (Sender)
                            conversationId,
                            messagePushId,
                            encryptedMessageContentBase64, // Encrypted preview for receiver's summary <-- MODIFIED
                            "text", // Message Type
                            firebaseTimestamp,
                            messageSenderID // Sender of THIS message
                    );
                    // --- END Update Chat Summaries ---


                    // *** NEW: Send Push Notification with Correct Sender Name ***
                    Log.d(TAG, "Firebase text message sent. Calling sendPushNotification.");
                    String senderDisplayNameForNotification = (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : "A User";
                    sendPushNotification(
                            messageReceiverID,
                            "New Message from " + senderDisplayNameForNotification,
                            messageText // Use the ORIGINAL, UNENCRYPTED message text for notification preview
                    );
                    // *** END NEW ***


                    // --- Update status in Room DB for the SENDER's copy ---
                    databaseWriteExecutor.execute(() -> {
                        messageDao.updateMessageStatus(messagePushId, "sent", messageSenderID);
                        Log.d(TAG, "Updated status to 'sent' in Room for owner " + messageSenderID + " for message: " + messagePushId);
                    });

                    runOnUiThread(() -> messageInputText.setText("")); // Clear input only on main thread after successful async start

                } else {
                    // Firebase write failed. Update status in Room to "failed".
                    Log.e(TAG, "Firebase setValue failed for text message " + messagePushId, task.getException());
                    databaseWriteExecutor.execute(() -> {
                        messageDao.updateMessageStatus(messagePushId, "failed", messageSenderID);
                        Log.d(TAG, "Updated status to 'failed' in Room for owner " + messageSenderID + " for message: " + messagePushId);
                    });
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show());
                }
            }); // End of addOnCompleteListener
        }); // End of outer databaseWriteExecutor
    }
    // --- End Send Text Message ---


    // --- Modified Method to Send Image Message (Includes Encryption) ---
    private void sendImageMessage(String base64ImageContent) { // Input is raw Base64 image string
        if (TextUtils.isEmpty(base64ImageContent)) {
            Toast.makeText(this, "Image data is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- *** CHECK if Secure Chat is Enabled (Keys are available) *** ---
        // Check dynamically here
        boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId); // *** MODIFIED CHECK ***

        // Get the *single* conversation key from KeyManager
        SecretKey conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId); // *** MODIFIED ***

        if (!isSecureChatAvailable || conversationAESKey == null) { // Check if key is null explicitly
            Log.w(TAG, "Cannot send image message: Secure chat keys are not available.");
            if (!YourKeyManager.getInstance().isPrivateKeyAvailable()) {
                Toast.makeText(this, "Your account is not unlocked for secure chat.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Secure chat key missing for this conversation.", Toast.LENGTH_SHORT).show();
            }
            return; // Do not proceed if keys are not available
        }
        // --- *** END Check *** ---

        String sendTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        Object firebaseTimestamp = ServerValue.TIMESTAMP;
        long localTimestamp = System.currentTimeMillis();

        DatabaseReference messagesRef = rootRef.child("Messages").child(conversationId).push(); // Use conversationId here!
        String messagePushId = messagesRef.getKey();

        if (messagePushId == null) {
            Log.e(TAG, "Failed to generate Firebase push key for image message");
            Toast.makeText(this, "Error sending image message", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Generated Firebase push ID for image: " + messagePushId);

        // --- *** ENCRYPT the image content (Base64 string) and get the Base64 STRING *** ---
        byte[] encryptedBytes; // Will get raw bytes from encryption
        String encryptedImageContentBase64; // Will be the Base64 String for storage/sending
        try {
            // The raw Base64 image string is the *message content* to be encrypted
            encryptedBytes = CryptoUtils.encryptMessageWithAES(base64ImageContent, conversationAESKey); // <-- MODIFIED (Input is String, Output is byte[])

            // Immediately encode this byte[] into a Base64 String using CryptoUtils.bytesToBase64
            encryptedImageContentBase64 = CryptoUtils.bytesToBase64(encryptedBytes); // *** NEW STEP ***

            if (TextUtils.isEmpty(encryptedImageContentBase64)) { // Defensive check
                Log.e(TAG, "Base64 encoding of encrypted image message failed.");
                throw new Exception("Base64 encoding failed."); // Throw to catch below
            }

            Log.d(TAG, "Image message encrypted and Base64 encoded. Base64 length: " + encryptedImageContentBase64.length());
        } catch (Exception e) { // Catch any encryption or encoding errors
            Log.e(TAG, "Failed to encrypt/encode image message", e);
            Toast.makeText(this, "Failed to encrypt image message.", Toast.LENGTH_SHORT).show();
            return; // Do not send if encryption/encoding fails
        }
        // --- *** END ENCRYPTION *** ---


        // Create MessageEntity for Room (store the *encrypted Base64 string* and use local timestamp)
        MessageEntity messageToSaveLocally = new MessageEntity();
        messageToSaveLocally.setFirebaseMessageId(messagePushId);
        messageToSaveLocally.setOwnerUserId(messageSenderID);
        messageToSaveLocally.setMessage(encryptedImageContentBase64); // Store the encrypted Base64 string <-- MODIFIED
        messageToSaveLocally.setType("image"); // Type is image
        messageToSaveLocally.setFrom(messageSenderID);
        messageToSaveLocally.setTo(messageReceiverID);
        messageToSaveLocally.setSendTime(sendTime);
        messageToSaveLocally.setSeen(false);
        messageToSaveLocally.setSeenTime("");
        messageToSaveLocally.setStatus("pending");
        messageToSaveLocally.setTimestamp(localTimestamp);


        // --- Insert into Room DB first with "pending" status (runs on background thread) ---
        databaseWriteExecutor.execute(() -> {
            try {
                messageDao.insertMessage(messageToSaveLocally);
                Log.d(TAG, "Inserted pending image message into Room (encrypted Base64) for owner " + messageSenderID + ": " + messagePushId + ", Local Timestamp: " + localTimestamp); // Updated log
            } catch (Exception e) {
                Log.e(TAG, "Error inserting pending image message into Room: " + messagePushId, e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error saving image locally.", Toast.LENGTH_SHORT).show());
            }

            // --- Send message to Firebase (runs on background thread after Room insert) ---
            DatabaseReference messagePathRef = rootRef.child("Messages").child(conversationId).child(messagePushId);

            Map<String, Object> messageFirebaseBody = new HashMap<>();
            messageFirebaseBody.put("message", encryptedImageContentBase64); // Send the encrypted Base64 string <-- MODIFIED
            messageFirebaseBody.put("type", "image");
            messageFirebaseBody.put("from", messageSenderID);
            messageFirebaseBody.put("to", messageReceiverID);
            messageFirebaseBody.put("seen", false);
            messageFirebaseBody.put("seenTime", "");
            messageFirebaseBody.put("sendTime", sendTime);
            messageFirebaseBody.put("timestamp", firebaseTimestamp);


            messagePathRef.setValue(messageFirebaseBody).addOnCompleteListener(task -> {
                Log.d(TAG, "Firebase setValue for image message " + messagePushId + " completed. Success: " + task.isSuccessful());

                if (task.isSuccessful()) {
                    // --- Update Chat Summaries for BOTH users ---
                    // Use "[Image]" or a similar placeholder for the preview content in the summary for IMAGE type
                    updateChatSummaryForUser(
                            messageSenderID,
                            messageReceiverID,
                            conversationId,
                            messagePushId,
                            "[Image]", // Placeholder preview for sender's summary list (images typically don't show decrypted preview in list)
                            "image",
                            firebaseTimestamp,
                            messageSenderID
                    );

                    // For the receiver's summary, also use the placeholder
                    updateChatSummaryForUser(
                            messageReceiverID,
                            messageSenderID,
                            conversationId,
                            messagePushId,
                            "[Image]", // Placeholder preview for receiver's summary list
                            "image",
                            firebaseTimestamp,
                            messageSenderID
                    );
                    // --- END Update Chat Summaries ---


                    // *** NEW: Send Push Notification with Correct Sender Name ***
                    Log.d(TAG, "Firebase image message sent. Calling sendPushNotification.");
                    String senderDisplayNameForNotification = (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : "A User";
                    sendPushNotification(
                            messageReceiverID,
                            "New Image from " + senderDisplayNameForNotification,
                            "[Image]" // Placeholder for image content in notification
                    );
                    // *** END NEW ***

                    // --- Update status in Room DB for the SENDER's copy ---
                    databaseWriteExecutor.execute(() -> {
                        messageDao.updateMessageStatus(messagePushId, "sent", messageSenderID);
                        Log.d(TAG, "Updated status to 'sent' in Room for owner " + messageSenderID + " for message: " + messagePushId);
                    });

                } else {
                    // Firebase write failed. Update status in Room to "failed".
                    Log.e(TAG, "Firebase setValue failed for image message " + messagePushId, task.getException());
                    databaseWriteExecutor.execute(() -> {
                        messageDao.updateMessageStatus(messagePushId, "failed", messageSenderID);
                        Log.d(TAG, "Updated status to 'failed' in Room for owner " + messageSenderID + " for message: " + messagePushId);
                    });
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to send image", Toast.LENGTH_SHORT).show());
                }
            }); // End of addOnCompleteListener
        }); // End of outer databaseWriteExecutor
    }
    // --- End Send Image Message ---


    // --- Modify attachMessageListener ---
    // This method syncs messages FROM Firebase TO Room.
    // MessageEntity.message will store the Base64 string received from Firebase.
    private void attachMessageListener() {
        if (TextUtils.isEmpty(conversationId)) {
            Log.w(TAG, "Cannot attach message listener: ConversationId is null.");
            return;
        }

        if (messageListener == null) {
            DatabaseReference conversationRef = rootRef.child("Messages").child(conversationId);

            messageListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Log.d(TAG, "onChildAdded triggered for " + snapshot.getKey() + " in conv " + conversationId);
                    Map<String, Object> messageData = (Map<String, Object>) snapshot.getValue();
                    String firebaseMessageId = snapshot.getKey();

                    if (messageData != null && firebaseMessageId != null) {
                        MessageEntity roomMessage = new MessageEntity();
                        roomMessage.setFirebaseMessageId(firebaseMessageId);
                        roomMessage.setOwnerUserId(messageSenderID); // Owner is the current logged-in user

                        // Get fields from Firebase snapshot
                        // The 'message' field from Firebase IS the Base64 String (encrypted content or placeholder)
                        roomMessage.setMessage((String) messageData.get("message")); // Store the Base64 String from Firebase <-- MODIFIED
                        roomMessage.setType((String) messageData.get("type"));
                        roomMessage.setFrom((String) messageData.get("from"));
                        roomMessage.setTo((String) messageData.get("to"));
                        roomMessage.setSendTime((String) messageData.get("sendTime"));
                        Boolean seenStatus = (Boolean) messageData.get("seen");
                        roomMessage.setSeen(seenStatus != null ? seenStatus : false);
                        roomMessage.setSeenTime((String) messageData.get("seenTime"));
                        String statusStr = (String) messageData.get("status");
                        roomMessage.setStatus(!TextUtils.isEmpty(statusStr) ? statusStr : (roomMessage.getFrom().equals(messageSenderID) ? "sent" : "received"));

                        roomMessage.setScheduledTime((String) messageData.get("scheduledTime"));

                        Log.d(TAG, "onChildAdded: Populated MessageEntity for " + firebaseMessageId +
                                ". Type: " + roomMessage.getType() +
                                ", From: " + roomMessage.getFrom() +
                                ", To: " + roomMessage.getTo() +
                                ", ScheduledTime: " + roomMessage.getScheduledTime()); // <-- Add this log


                        // --- END ADD THIS LINE ---

                        Long timestampLong = null;
                        Object timestampObj = messageData.get("timestamp");
                        if (timestampObj instanceof Long) {
                            timestampLong = (Long) timestampObj;
                        } else if (timestampObj instanceof Double) {
                            timestampLong = ((Double) timestampObj).longValue();
                        }
                        roomMessage.setTimestamp(timestampLong != null && timestampLong > 0 ? timestampLong : System.currentTimeMillis());


                        // Basic validation for essential fields
                        // message content (Base64) CAN be empty for some types or errors, don't block insert on empty content.
                        if (TextUtils.isEmpty(roomMessage.getType()) || TextUtils.isEmpty(roomMessage.getFrom()) || TextUtils.isEmpty(roomMessage.getTo())) {
                            Log.e(TAG, "Received message from Firebase with missing essential fields for messageId: " + firebaseMessageId + ". Skipping Room insertion.");
                            return;
                        }

                        databaseWriteExecutor.execute(() -> {
                            // Insert the message into Room DB (INSERT OR REPLACE strategy) for the current user
                            // Inserting here will trigger the LiveData observer -> forceRefreshDisplay()
                            messageDao.insertMessage(roomMessage);
                            Log.d(TAG, "Inserted/Updated message in Room: " + firebaseMessageId + " (onChildAdded), Owner: " + roomMessage.getOwnerUserId() + ", Timestamp: " + roomMessage.getTimestamp());

                            Log.d(TAG, "Inserted/Updated message in Room: " + firebaseMessageId +
                                    " (onChildAdded), Owner: " + roomMessage.getOwnerUserId() +
                                    ", Timestamp: " + roomMessage.getTimestamp() +
                                    ", ScheduledTime (in Room): " + roomMessage.getScheduledTime()); // <-- Update this log






                            // --- NEW: Check if the incoming message is a system key change message ---
                            // Only process this if it's an incoming message from the other user
                            if ("system_key_change".equals(roomMessage.getType()) && roomMessage.getFrom().equals(messageReceiverID) && roomMessage.getTo().equals(messageSenderID)) {
                                Log.d(TAG, "onChildAdded (Executor): Received incoming system_key_change message. Triggering conversation key load attempt.");

                                // Trigger the conversation key load attempt if the user's private key is available.
                                // This method (attemptLoadConversationKeyAsync) needs to be implemented separately
                                // in ChatPageActivity to perform the Room/Firebase key loading/decryption.
                                // It needs access to Room DB (ConversationKeyDao) and Firebase (RootRef) and YourKeyManager.
                                // Since this code block is already on databaseWriteExecutor, calling
                                // attemptLoadConversationKeyAsync directly will keep it on the background thread.
                                if (YourKeyManager.getInstance().isPrivateKeyAvailable()) {
                                    // Call the new helper method to attempt loading the key for this conversation
                                    attemptLoadConversationKeyAsync(conversationId, messageSenderID); // Pass conversationId and current user ID (senderId)
                                } else {
                                    Log.w(TAG, "onChildAdded (Executor): Received system_key_change, but user's private key is NOT available. Cannot load new key.");
                                    // Messages will continue to show as failed until user unlocks account.
                                }
                            }
                            // --- END NEW: Check if the incoming message is a system key change message ---




                            // --- Handle Notification for Incoming Messages (only if not already notified/seen) ---
                            // This logic needs the decrypted content for the notification text.
                            // This is handled in the executor, so it can block.
                            if (roomMessage.getTo().equals(messageSenderID) && roomMessage.getFrom().equals(messageReceiverID)) { // Incoming message
                                // Check if the activity is not currently resumed (i.e., user is not viewing this chat)
                                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                                    // Check if the message is not already seen (in Room) and hasn't been notified already
                                    // The 'seen' status in Room comes from Firebase sync
                                    // NotifiedMessageStore.isAlreadyNotified is a placeholder, implement proper tracking
                                    if (!roomMessage.isSeen() /*&& !NotifiedMessageStore.isAlreadyNotified(ChatPageActivity.this, firebaseMessageId)*/) { // Commented out placeholder
                                        Log.d(TAG, "Incoming message " + firebaseMessageId + " received while activity is NOT RESUMED and not seen/notified. Preparing notification.");

                                        // --- Decrypt message content for Notification text ---
                                        String notificationText = "[Message]"; // Default placeholder
                                        String storedContentBase64 = roomMessage.getMessage(); // Get the Base64 from Room

                                        // Attempt decryption ONLY IF secure chat keys are available AND it's text/image AND content exists
                                        boolean isSecureChatAvailableForNotification = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId); // *** MODIFIED CHECK ***
                                        SecretKey conversationAESKey = null;
                                        if (isSecureChatAvailableForNotification) {
                                            conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId); // *** MODIFIED ***
                                        }

                                        // Attempt decryption if keys available and content exists
                                        if (isSecureChatAvailableForNotification && conversationAESKey != null && ("text".equals(roomMessage.getType()) || "image".equals(roomMessage.getType())) && !TextUtils.isEmpty(storedContentBase64)) {
                                            try {
                                                // Decode Base64 string to bytes using CryptoUtils.base64ToBytes
                                                byte[] encryptedBytesWithIV = CryptoUtils.base64ToBytes(storedContentBase64); // *** MODIFIED ***

                                                if (encryptedBytesWithIV != null && encryptedBytesWithIV.length > 0) { // Check decoded bytes

                                                    // Decrypt bytes using CryptoUtils.decryptMessageWithAES
                                                    String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytesWithIV, conversationAESKey); // *** MODIFIED ***

                                                    if ("text".equals(roomMessage.getType())) {
                                                        notificationText = decryptedContent; // Use decrypted text
                                                    } else if ("image".equals(roomMessage.getType())) {
                                                        notificationText = "[Image]"; // Placeholder for images in notification
                                                    } else {
                                                        notificationText = "[Unknown Type]"; // Fallback
                                                    }
                                                    // Log.d(TAG, "Notification text decrypted successfully: " + notificationText); // Avoid logging content

                                                } else {
                                                    Log.w(TAG, "Decoded encrypted bytes null/empty for notification for msg " + firebaseMessageId);
                                                    notificationText = "[Invalid Encrypted Data]"; // Fallback
                                                }

                                            } catch (IllegalArgumentException e) { // Base64 decoding error
                                                Log.e(TAG, "Base64 decoding error decrypting message for notification: " + firebaseMessageId, e);
                                                notificationText = "[Invalid Encrypted Data]"; // Placeholder
                                            } catch (Exception e) { // Catch other decryption errors
                                                Log.e(TAG, "Decryption failed for message for notification: " + firebaseMessageId, e);
                                                notificationText = "[Encrypted Message]"; // Fallback
                                            }
                                        } else {
                                            // Secure chat not enabled or key missing or content empty, use placeholder
                                            Log.w(TAG, "Secure chat NOT available or key missing or content empty for notification decryption. Using placeholder.");
                                            if ("image".equals(roomMessage.getType())) {
                                                notificationText = "[Image]";
                                            } else if ("file".equals(roomMessage.getType())) {
                                                notificationText = "[File]";
                                            } else if ("system_key_change".equals(roomMessage.getType())) { // Handle system message specifically
                                                notificationText = storedContentBase64; // System message content is plain text
                                                if(TextUtils.isEmpty(notificationText)) notificationText = "[System Message]";
                                            } else {
                                                notificationText = "[Message]"; // Generic placeholder
                                            }
                                        }
                                        // --- End Decrypt message content ---

                                        // Send push notification using OneSignal API helper
                                        // This runs on the ExecutorService, post to Main Thread if needed
                                        // or use a background notification service (recommended).
                                        // Assuming sendPushNotification is thread-safe or uses a Handler internally.
                                        String senderNameForNotification = messageReceiverName; // The sender of the incoming message is the chat partner
                                        sendPushNotification(
                                                messageSenderID, // Recipient is the current user
                                                "New Message from " + senderNameForNotification,
                                                notificationText // Decrypted or placeholder text
                                        );
                                        // Mark as notified locally to prevent duplicate notifications (implement NotifiedMessageStore)
                                        // NotifiedMessageStore.markAsNotified(ChatPageActivity.this, firebaseMessageId);


                                    }
                                }
                            }
                        });
                    } else {
                        Log.w(TAG, "Received null message data or messageId from Firebase DataSnapshot in onChildAdded.");
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Map<String, Object> messageData = (Map<String, Object>) snapshot.getValue();
                    String firebaseMessageId = snapshot.getKey();

                    if (messageData != null && firebaseMessageId != null) {
                        Log.d(TAG, "onChildChanged triggered for messageId: " + firebaseMessageId + " in conv " + conversationId);

                        databaseWriteExecutor.execute(() -> {
                            MessageEntity existingMessage = messageDao.getMessageByFirebaseId(firebaseMessageId, messageSenderID);

                            if (existingMessage != null) {
                                if (!existingMessage.getOwnerUserId().equals(messageSenderID)) { /* ... bug check ... */ return; }

                                // Update fields that can change in Firebase (status, seen, seenTime, timestamp)
                                String updatedContentBase64 = (String) messageData.get("message"); // Get the Base64 string from Firebase
                                Boolean seenStatus = (Boolean) messageData.get("seen");
                                String seenTimeStr = (String) messageData.get("seenTime");
                                String statusStr = (String) messageData.get("status");
                                Long timestampLong = null;
                                Object timestampObj = messageData.get("timestamp");
                                if (timestampObj instanceof Long) { timestampLong = (Long) timestampObj; }
                                else if (timestampObj instanceof Double) { timestampLong = ((Double) timestampObj).longValue(); }


                                // Update message content ONLY if it's somehow different (less likely for secure content unless edited)
                                // Always update the Base64 string from Firebase in Room to ensure it's the latest version
                                if (!TextUtils.equals(updatedContentBase64, existingMessage.getMessage())) { // Use TextUtils.equals for safety
                                    existingMessage.setMessage(updatedContentBase64); // Update encrypted content Base64 string
                                    Log.d(TAG, "Updating encrypted message content Base64 string in Room for " + firebaseMessageId + " (onChildChanged)");
                                }


                                // Update seen status and time
                                if (seenStatus != null) {
                                    existingMessage.setSeen(seenStatus);
                                    if (seenStatus && !TextUtils.isEmpty(seenTimeStr)) {
                                        existingMessage.setSeenTime(seenTimeStr);
                                    } else if (!seenStatus) {
                                        existingMessage.setSeenTime("");
                                    }
                                }

                                // Update status
                                if (!TextUtils.isEmpty(statusStr) && !statusStr.equals(existingMessage.getStatus())) {
                                    existingMessage.setStatus(statusStr);
                                }

                                // Update timestamp
                                if (timestampLong != null && timestampLong > 0 && existingMessage.getTimestamp() != timestampLong) {
                                    existingMessage.setTimestamp(timestampLong);
                                }

                                // Save the updated message entity back to Room (REPLACE strategy)
                                // This will trigger LiveData update -> forceRefreshDisplay()
                                messageDao.insertMessage(existingMessage);
                                // Log.d(TAG, "Updated message in Room from Firebase change for owner " + messageSenderID + ": " + firebaseMessageId);

                            } else {
                                // Message changed but wasn't in Room for this user. Re-insert it.
                                Log.e(TAG, "Received onChildChanged for message " + firebaseMessageId + " NOT found in Room for owner " + messageSenderID + ". Re-inserting.");

                                // Re-fetch all necessary data from the snapshot
                                String messageContentBase64 = (String) messageData.get("message"); // This is the Base64 string
                                String type = (String) messageData.get("type");
                                String fromUserID = (String) messageData.get("from");
                                String toUserID = (String) messageData.get("to");
                                String sendTimeStrRe = (String) messageData.get("sendTime");
                                Boolean seenStatusRe = (Boolean) messageData.get("seen");
                                String seenTimeStrRe = (String) messageData.get("seenTime");
                                String statusStrRe = (String) messageData.get("status");
                                Long timestampLongRe = null;
                                Object timestampObjRe = messageData.get("timestamp");
                                if (timestampObjRe instanceof Long) {
                                    timestampLongRe = (Long) timestampObjRe;
                                } else if (timestampObjRe instanceof Double) {
                                    timestampLongRe = ((Double) timestampObjRe).longValue();
                                }


                                // Create and insert a new MessageEntity for this user
                                MessageEntity newMessage = new MessageEntity();
                                newMessage.setFirebaseMessageId(firebaseMessageId);
                                newMessage.setOwnerUserId(messageSenderID); // Owner is the current user
                                newMessage.setMessage(messageContentBase64); // Encrypted content Base64 string
                                newMessage.setType(type);
                                newMessage.setFrom(fromUserID);
                                newMessage.setTo(toUserID);
                                newMessage.setSendTime(sendTimeStrRe);
                                newMessage.setSeen(seenStatusRe != null ? seenStatusRe : false);
                                newMessage.setSeenTime(seenTimeStrRe);
                                newMessage.setStatus(!TextUtils.isEmpty(statusStrRe) ? statusStrRe : (fromUserID.equals(messageSenderID) ? "sent" : "received"));
                                newMessage.setTimestamp(timestampLongRe != null && timestampLongRe > 0 ? timestampLongRe : System.currentTimeMillis()); // Use resolved timestamp or local

                                messageDao.insertMessage(newMessage); // Insert the missing message. This triggers LiveData.
                                Log.d(TAG, "Re-inserted message " + firebaseMessageId + " into Room for owner " + messageSenderID + " after onChildChanged.");
                            }
                        });
                    } else {
                        Log.w(TAG, "Received null message data or messageId from Firebase DataSnapshot in onChildChanged.");
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    // When a message is deleted from Firebase (e.g., "Delete for Everyone")
                    String firebaseMessageId = snapshot.getKey();
                    if (firebaseMessageId != null && !TextUtils.isEmpty(messageSenderID)) {
                        Log.d(TAG, "onChildRemoved triggered for messageId: " + firebaseMessageId + ". Deleting from Room for owner " + messageSenderID + ".");
                        databaseWriteExecutor.execute(() -> {
                            // *** MODIFIED: Delete the message from Room for the current user ***
                            // Deleting from Room will trigger LiveData observer -> forceRefreshDisplay()
                            int deletedCount = messageDao.deleteMessageByFirebaseId(firebaseMessageId, messageSenderID);
                            if (deletedCount > 0) {
                                Log.d(TAG, "Message removed from Room (onChildRemoved) for owner " + messageSenderID + ": " + firebaseMessageId);
                            } else {
                                Log.w(TAG, "Attempted to remove message " + firebaseMessageId + " from Room via onChildRemoved for owner " + messageSenderID + ", but it wasn't found.");
                            }
                        });
                    } else {
                        Log.w(TAG, "onChildRemoved received with null messageId or senderID.");
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // Messages moving within the list - typically not relevant for standard chat UIs
                    Log.d(TAG, "onChildMoved triggered for messageId: " + snapshot.getKey());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase Chat Listener Cancelled for conversation " + conversationId + ": " + error.getMessage(), error.toException());
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Chat updates failed: " + error.getMessage(), Toast.LENGTH_LONG).show());
                }
            };

            // Attach listener to the conversation-specific path in Firebase
            conversationRef.addChildEventListener(messageListener);
            Log.d(TAG, "Firebase listener attached for chat path: Messages/" + conversationId);

        } else {
            Log.d(TAG, "Firebase listener already attached.");
        }
    }
    // --- *** END Modify attachMessageListener *** ---




    // Add this NEW method inside ChatPageActivity.java, outside of other methods

    /**
     * Attempts to load the conversation AES key for a specific conversation from Room DB,
     * falling back to Firebase if not found or invalid locally. If a key is found/decrypted,
     * it's loaded into YourKeyManager and saved to Room.
     * Designed to be called on a background thread (e.g., databaseWriteExecutor).
     *
     * @param conversationId The ID of the conversation.
     * @param userId The ID of the current user (owner of the keys).
     */
    private void attemptLoadConversationKeyAsync(String conversationId, String userId) {
        Log.d(TAG, "attemptLoadConversationKeyAsync (Executor): Attempting to load key for conv ID: " + conversationId + " for user: " + userId);

        // Check necessary members (db, RootRef, YourKeyManager should be initialized in onCreate)
        // This method runs on a background thread, so accessing initialized members is okay.
        // 'rootRef' is the member variable for FirebaseDatabase.getInstance().getReference()
        if (TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(userId) || db == null || rootRef == null) {
            Log.e(TAG, "attemptLoadConversationKeyAsync (Executor): Prerequisites (DB, Firebase, IDs) missing. Cannot load key.");
            return;
        }

        ConversationKeyDao keyDao = db.conversationKeyDao(); // Get DAO instance
        YourKeyManager keyManager = YourKeyManager.getInstance(); // Get KeyManager instance

        SecretKey loadedKey = null; // Will hold the successfully loaded/decrypted key
        String failureReason = "Unknown Error"; // Track why loading failed

        // --- Step 1: Attempt to load the SINGLE key from Room DB ---
        Log.d(TAG, "attemptLoadConversationKeyAsync (Executor): Checking Room DB for key.");
        try {
            // Use the single-key DAO method to get the key for this user and conversation
            ConversationKeyEntity keyEntity = keyDao.getKeyForConversation(userId, conversationId);

            if (keyEntity != null && !TextUtils.isEmpty(keyEntity.getDecryptedKeyBase64())) {
                try {
                    // Use CryptoUtils for Base64 decode from Room storage
                    byte[] decryptedKeyBytes = CryptoUtils.base64ToBytes(keyEntity.getDecryptedKeyBase64()); // *** Use CryptoUtils ***
                    if (decryptedKeyBytes != null && decryptedKeyBytes.length > 0) {
                        loadedKey = CryptoUtils.bytesToSecretKey(decryptedKeyBytes);
                        Log.d(TAG, "attemptLoadConversationKeyAsync (Executor): Key successfully loaded from Room.");
                        // Found in Room, no need to check Firebase unless Room data is suspect.
                    } else {
                        Log.w(TAG, "attemptLoadConversationKeyAsync (Executor): Decoded key bytes from Room are empty/null for conv " + conversationId + ". Deleting corrupt entry.");
                        keyDao.deleteKeyForConversation(userId, conversationId); // Delete corrupt entry
                        failureReason = "Corrupt data in Room.";
                    }
                } catch (IllegalArgumentException e) { // Base64 decode error
                    Log.e(TAG, "attemptLoadConversationKeyAsync (Executor): Base64 decoding error from Room for conv " + conversationId + ". Deleting corrupt entry.", e);
                    keyDao.deleteKeyForConversation(userId, conversationId); // Delete corrupt entry
                    failureReason = "Base64 error in Room.";
                } catch (Exception e) { // Error converting to SecretKey etc.
                    Log.e(TAG, "attemptLoadConversationKeyAsync (Executor): Error processing key from Room for conv " + conversationId + ". Deleting corrupt entry.", e);
                    keyDao.deleteKeyForConversation(userId, conversationId); // Delete corrupt entry
                    failureReason = "Processing error from Room.";
                }
            } else {
                Log.d(TAG, "attemptLoadConversationKeyAsync (Executor): Key not found in Room DB for conv " + conversationId + " for user " + userId);
                failureReason = "Not found in Room.";
            }
        } catch (Exception e) {
            Log.e(TAG, "attemptLoadConversationKeyAsync (Executor): Error querying Room DB for conv " + conversationId + " for user " + userId, e);
            failureReason = "Room query error.";
        }


        // --- Step 2: If key not loaded from Room, attempt to load from Firebase ---
        // Only attempt Firebase if Room failed AND we have the user's private key (needed for decryption)
        if (loadedKey == null && keyManager.isPrivateKeyAvailable()) {
            Log.d(TAG, "attemptLoadConversationKeyAsync (Executor): Key not loaded from Room (" + failureReason + "). Checking Firebase for conv " + conversationId + ".");
            PrivateKey currentUserPrivateKey = keyManager.getUserPrivateKey(); // Should not be null here

            if (currentUserPrivateKey == null) { // Defensive check, should be true by outer check
                Log.e(TAG, "attemptLoadConversationKeyAsync (Executor): Private key null unexpectedly during Firebase fallback check.");
                failureReason = "Private key unavailable.";
            } else {
                try {
                    // Fetch the 'key_versions' node from Firebase
                    // This requires the RootRef object initialized in onCreate
                    DataSnapshot keyVersionsSnapshot = Tasks.await(rootRef.child("ConversationKeys").child(conversationId).child("key_versions").get(), 15, TimeUnit.SECONDS); // Add timeout

                    if (keyVersionsSnapshot.exists() && keyVersionsSnapshot.hasChildren()) {
                        Log.d(TAG, "attemptLoadConversationKeyAsync (Executor): Key versions found in Firebase. Attempting decryption.");

                        // Iterate through each key version found in Firebase
                        // Sorting by timestamp descending and trying the latest first is a reasonable heuristic.
                        List<DataSnapshot> keyVersionSnapsList = new ArrayList<>();
                        for (DataSnapshot snap : keyVersionsSnapshot.getChildren()) { keyVersionSnapsList.add(snap); }
                        Collections.sort(keyVersionSnapsList, (s1, s2) -> {
                            try {
                                Long ts1 = Long.parseLong(s1.getKey());
                                Long ts2 = Long.parseLong(s2.getKey());
                                return Long.compare(ts2, ts1); // Descending order (latest first)
                            } catch (NumberFormatException e) {
                                Log.w(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Error parsing timestamp key during sort: " + s1.getKey() + " or " + s2.getKey());
                                return 0; // Treat as equal if parsing fails
                            }
                        });


                        for (DataSnapshot keyVersionSnap : keyVersionSnapsList) {
                            String timestampKey = keyVersionSnap.getKey();
                            long keyTimestamp; // Optional: capture timestamp for Room entity
                            try {
                                keyTimestamp = Long.parseLong(timestampKey);
                                if (keyTimestamp <= 0) throw new NumberFormatException();
                            } catch (NumberFormatException e) {
                                Log.w(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Skipping Firebase key version with invalid timestamp key: " + timestampKey + " for conv " + conversationId);
                                continue; // Skip this key version and move to the next
                            }

                            // Check if this key version snapshot contains the encrypted key data for the *current user*
                            if (keyVersionSnap.hasChild(userId)) {
                                String encryptedAesKeyForCurrentUserBase64 = keyVersionSnap.child(userId).getValue(String.class);
                                if (TextUtils.isEmpty(encryptedAesKeyForCurrentUserBase64)) {
                                    Log.w(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Empty encrypted key data for user " + userId + " in Firebase key version " + keyTimestamp + " for conv " + conversationId);
                                    failureReason = "Firebase key data empty."; // Update failure reason for this version
                                    continue; // Try next version
                                }

                                try {
                                    // Decode Base64 using CryptoUtils
                                    byte[] encryptedAesKeyBytes = CryptoUtils.base64ToBytes(encryptedAesKeyForCurrentUserBase64);

                                    // Attempt decryption using the *current user's* private key
                                    byte[] decryptedAesKeyBytes = CryptoUtils.decryptWithRSA(encryptedAesKeyBytes, currentUserPrivateKey);

                                    // Convert decrypted bytes back to SecretKey
                                    SecretKey conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedAesKeyBytes);

                                    // --- Success: Key Decrypted from this Firebase version! ---
                                    loadedKey = conversationAESKey; // Set the loadedKey
                                    Log.d(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Successfully decrypted key version " + keyTimestamp + " from Firebase for user " + userId + " conv " + conversationId);

                                    // Save this successfully decrypted key to Room (replace any old one)
                                    // Use the shared DB executor if this method is not already on it, or just call directly if it is.
                                    // Since this method is called from databaseWriteExecutor, calling DAO directly is fine.
                                    if (decryptedAesKeyBytes != null) {
                                        try {
                                            String decryptedKeyBase64ForRoom = CryptoUtils.bytesToBase64(decryptedAesKeyBytes); // *** Use CryptoUtils for consistency ***
                                            ConversationKeyEntity keyEntityToSave = new ConversationKeyEntity(userId, conversationId, decryptedKeyBase64ForRoom);
                                            keyEntityToSave.setKeyTimestamp(keyTimestamp); // Set the timestamp from Firebase
                                            keyDao.insertOrUpdateKey(keyEntityToSave); // Call DAO
                                            Log.d(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Key version (Firebase T:" + keyTimestamp + ") saved to Room after Firebase decrypt for conv ID: " + conversationId);
                                        } catch (Exception saveEx) {
                                            Log.e(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Error saving fetched key to Room after Firebase decrypt", saveEx);
                                        }
                                    } else {
                                        Log.e(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Skipped saving to Room: Decrypted AES key bytes were null.");
                                    }

                                    // *** We found and processed a working key, exit the loop! ***
                                    break; // Found a working key, stop searching further versions

                                } catch (IllegalArgumentException e) { // Base64/Key conversion error
                                    Log.w(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Base64 decoding error or invalid key format during Firebase decrypt for version " + keyTimestamp, e);
                                    failureReason = "Firebase key data invalid."; // Update failure reason
                                    continue; // Try next version
                                } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                                         InvalidKeyException |
                                         IllegalBlockSizeException | BadPaddingException e) {
                                    Log.w(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Crypto error during Firebase decrypt for version " + keyTimestamp + " with current key.", e);
                                    failureReason = "Decryption failed with current key."; // Update failure reason
                                    continue; // Try next version (might work with an older private key version if the user reset their own keys multiple times)
                                } catch (Exception e) {
                                    Log.e(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Unexpected error during Firebase fetch/decrypt for version " + keyTimestamp, e);
                                    failureReason = "Unexpected Firebase processing error."; // Update failure reason
                                    continue; // Try next version
                                }
                            } else {
                                Log.w(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Encrypted key for user " + userId + " NOT found in Firebase key version snapshot: " + timestampKey + " for conv " + conversationId);
                                failureReason = "Key entry missing in Firebase version."; // Update failure reason
                                // Continue to next version
                            }
                        } // End loop through Firebase versions

                        if (loadedKey == null) {
                            Log.e(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Decryption failed for ALL available key versions from Firebase for conv " + conversationId + " for user " + userId + ".");
                            failureReason = "All Firebase keys failed decryption.";
                            // If no key was found/decrypted from Firebase after checking all versions,
                            // we don't trigger generation here. Generation happens in ChatFragment
                            // if the user clicks the chat and no key can be loaded.
                        }


                    } else {
                        Log.d(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): No key_versions node or no children in Firebase for conv ID: " + conversationId + ". Key cannot be loaded from Firebase.");
                        failureReason = "No key versions in Firebase.";
                        // No key found in Firebase.
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Firebase fetch failed for key_versions for conv " + conversationId + ".", e);
                    failureReason = "Firebase fetch error.";
                } catch (TimeoutException e) {
                    Log.e(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Firebase fetch timed out for key_versions for conv " + conversationId + ".");
                    failureReason = "Firebase fetch timeout.";
                } catch (Exception e) {
                    Log.e(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Unexpected error during Firebase fetch/processing for conv " + conversationId + ".", e);
                    failureReason = "Unexpected Firebase error.";
                }
            }
        }

        // --- Final Step: Load the key into YourKeyManager if successfully loaded ---
        if (loadedKey != null) {
            // Use the single-key KeyManager method to set the key for this conversation ID
            keyManager.setConversationKey(conversationId, loadedKey); // *** Use single-key method ***
            Log.d(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Final Key loaded into KeyManager for conv ID: " + conversationId);
            // The LiveData observer will be triggered by the message insertion,
            // and forceRefreshDisplay will use the updated KeyManager state.
        } else {
            Log.w(TAG, "attemptLoadConversationKeyAsync (Worker/Executor): Failed to load key for conv ID: " + conversationId + ". Final reason: " + failureReason);
            // KeyManager remains without this conversation's key. Messages will continue to show as failed.
            // The user might need to manually trigger key regeneration by clicking the chat in ChatFragment.
        }

        Log.d(TAG, "attemptLoadConversationKeyAsync (Executor): Task finished for conv ID: " + conversationId);

        // No post to Main Thread needed from here for UI refresh.
        // The initial message insertion in onChildAdded triggers the LiveData update,
        // which calls forceRefreshDisplay() on the Main Thread.
        // forceRefreshDisplay() will then use the state of KeyManager *at that moment*
        // to decrypt the messages. If this async key loading task finishes *before*
        // forceRefreshDisplay runs, forceRefreshDisplay will see the new key.
        // If it finishes *after* forceRefreshDisplay runs, the next message received
        // or a manual refresh will trigger forceRefreshDisplay again, and it will use the new key.
    }


    // --- Modify markSingleMessageAsSeen --- (Keep as is, updates Firebase 'seen' status and unread count)
    private void markSingleMessageAsSeen(String firebaseMessageId, String currentUserId, String chatPartnerId) { /* ... (existing method) ... */
        if (TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(firebaseMessageId) || TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(chatPartnerId) || !getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            Log.w(TAG, "Cannot mark message as seen: Missing essential IDs or activity not resumed.");
            return;
        }

        String messagePathInFirebase = "Messages/" + conversationId + "/" + firebaseMessageId;
        String seenTimeValue = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        Map<String, Object> updates = new HashMap<>();
        updates.put("seen", true);
        updates.put("seenTime", seenTimeValue);

        rootRef.child(messagePathInFirebase).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Marked message " + firebaseMessageId + " as seen in Firebase (path: " + messagePathInFirebase + ") with seenTime: " + seenTimeValue);

                        DatabaseReference receiverSummaryUnreadCountRef = rootRef
                                .child("ChatSummaries")
                                .child(currentUserId)
                                .child(chatPartnerId)
                                .child("unreadCounts")
                                .child(currentUserId);

                        Log.d(TAG, "Attempting to set unread count to 0 for receiver " + currentUserId + " in their summary with partner " + chatPartnerId);
                        receiverSummaryUnreadCountRef.setValue(0)
                                .addOnCompleteListener(unreadTask -> {
                                    if (unreadTask.isSuccessful()) {
                                        Log.d(TAG, "Successfully set unread count to 0 in receiver's summary.");
                                    } else {
                                        Log.e(TAG, "Failed to set unread count to 0 in receiver's summary.", unreadTask.getException());
                                    }
                                });
                    } else {
                        Log.e(TAG, "Failed to mark message " + firebaseMessageId + " as seen in Firebase", task.getException());
                    }
                });
    }


    // --- Modify retryPendingMessages --- (Keep as is, check KeyManager state, calls sendPendingMessageToFirebase)
    private void retryPendingMessages() { /* ... (existing method) ... */
        if (TextUtils.isEmpty(messageSenderID) || TextUtils.isEmpty(messageReceiverID) || TextUtils.isEmpty(conversationId) ||
                !YourKeyManager.getInstance().isPrivateKeyAvailable() || !YourKeyManager.getInstance().hasConversationKey(conversationId)) { // *** MODIFIED CHECK ***
            Log.w(TAG, "Skipping retryPendingMessages: Secure chat keys not available or missing IDs.");
            return;
        }

        databaseWriteExecutor.execute(() -> {
            List<MessageEntity> pendingMessages = messageDao.getPendingMessagesForChat(messageSenderID, messageSenderID);

            if (pendingMessages != null && !pendingMessages.isEmpty()) {
                Log.d(TAG, "Found " + pendingMessages.size() + " pending messages to retry for this chat.");
                for (MessageEntity pendingMessage : pendingMessages) {
                    MessageEntity latestMessageState = messageDao.getMessageByFirebaseId(pendingMessage.getFirebaseMessageId(), messageSenderID);

                    if (latestMessageState != null && "pending".equals(latestMessageState.getStatus())) {
                        Log.d(TAG, "Retrying message: " + pendingMessage.getFirebaseMessageId());
                        sendPendingMessageToFirebase(latestMessageState);
                    }
                }
            } else {
                Log.d(TAG, "No pending messages found for chat with: " + messageReceiverID + " owned by " + messageSenderID);
            }
        });
    }


    // --- Modify sendPendingMessageToFirebase --- (Keep as is, uses content already in Room)
    private void sendPendingMessageToFirebase(MessageEntity pendingMessage) { /* ... (existing method) ... */
        if (TextUtils.isEmpty(pendingMessage.getMessage()) || TextUtils.isEmpty(pendingMessage.getType()) ||
                TextUtils.isEmpty(pendingMessage.getFrom()) || TextUtils.isEmpty(pendingMessage.getTo()) ||
                TextUtils.isEmpty(pendingMessage.getFirebaseMessageId()) || TextUtils.isEmpty(pendingMessage.getOwnerUserId()) ||
                TextUtils.isEmpty(conversationId) ||
                !YourKeyManager.getInstance().isPrivateKeyAvailable() ||
                !YourKeyManager.getInstance().hasConversationKey(conversationId)) // *** MODIFIED CHECK ***
        {
            Log.w(TAG, "Cannot retry sending pending message: Secure chat disabled, missing IDs, or corrupt message data.");
            if (!TextUtils.isEmpty(pendingMessage.getFirebaseMessageId()) && !TextUtils.isEmpty(pendingMessage.getOwnerUserId())) {
                databaseWriteExecutor.execute(() -> messageDao.updateMessageStatus(pendingMessage.getFirebaseMessageId(), "failed", pendingMessage.getOwnerUserId()));
            } else {
                Log.e(TAG, "Cannot update failed status for pending message, missing ID/Owner ID.");
            }
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Retry failed: Secure chat unavailable or data corrupt.", Toast.LENGTH_SHORT).show());
            return;
        }

        DatabaseReference messagePathRef = rootRef.child("Messages").child(conversationId).child(pendingMessage.getFirebaseMessageId());

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", pendingMessage.getMessage()); // This is already the ENCRYPTED Base64 content from Room
        messageData.put("type", pendingMessage.getType());
        messageData.put("from", pendingMessage.getFrom());
        messageData.put("to", pendingMessage.getTo());

        messageData.put("seen", pendingMessage.isSeen());
        messageData.put("seenTime", pendingMessage.getSeenTime());
        messageData.put("sendTime", pendingMessage.getSendTime());
        messageData.put("timestamp", pendingMessage.getTimestamp());


        messagePathRef.setValue(messageData).addOnCompleteListener(task -> {
            Log.d(TAG, "Firebase retry setValue for message " + pendingMessage.getFirebaseMessageId() + " completed. Success: " + task.isSuccessful());
            if (!task.isSuccessful()) {
                Log.e(TAG, "Firebase retry setValue failed for message " + pendingMessage.getFirebaseMessageId(), task.getException());
            }
            databaseWriteExecutor.execute(() -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Firebase retry write success for " + pendingMessage.getFirebaseMessageId() + ". Status update via listener.");
                } else {
                    Log.e(TAG, "Firebase retry write failed for " + pendingMessage.getFirebaseMessageId() + ". Updating status to failed in Room.");
                    messageDao.updateMessageStatus(pendingMessage.getFirebaseMessageId(), "failed", pendingMessage.getOwnerUserId());
                    Log.d(TAG, "Updated status to 'failed' in Room for owner " + pendingMessage.getOwnerUserId() + " for message: " + pendingMessage.getFirebaseMessageId());
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Retry failed for message: " + pendingMessage.getFirebaseMessageId(), Toast.LENGTH_SHORT).show());
                }
            });
        });
    }


    // --- Modify Delete Message Logic --- (Keep as is, checks KeyManager state)
    @Override // This method is already implemented as part of OnMessageLongClickListener
    public void onMessageLongClick(MessageEntity message) { /* ... (existing method) ... */
        Log.d(TAG, "Message Long Clicked: " + message.getFirebaseMessageId() + ", Type: " + message.getType() + ", From: " + message.getFrom() + ", Owner: " + message.getOwnerUserId());

        if (!message.getOwnerUserId().equals(messageSenderID)) {
            Log.w(TAG, "Ignoring long click on message not owned by current user: " + message.getFirebaseMessageId());
            return;
        }

        List<String> optionsList = new ArrayList<>();

        // Only allow Copy Text if secure chat keys are available AND it's a text message AND content is decrypted/available
        boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId); // *** MODIFIED CHECK ***

        if (isSecureChatAvailable && "text".equals(message.getType()) && !TextUtils.isEmpty(message.getMessage()) && !message.getMessage().startsWith("[")) { // Check if decrypted content is not empty and not a placeholder
            optionsList.add("Copy Text");
        }

        // Add Save Image option ONLY if the message is an image AND secure chat keys are available AND decrypted content is available (not a placeholder)
        if (isSecureChatAvailable && "image".equals(message.getType()) && !TextUtils.isEmpty(message.getMessage()) && !message.getMessage().startsWith("[")) { // Check if decrypted content is not empty and not a placeholder
            optionsList.add("Save Image to Gallery");
        }

        optionsList.add("Delete for Me");

        // Add "Delete for Everyone" ONLY if the message was sent by the current user AND Secure Chat keys are available
        if (isSecureChatAvailable && message.getFrom().equals(messageSenderID)) { // *** MODIFIED CHECK ***
            optionsList.add("Delete for Everyone");
        }

        if (optionsList.isEmpty()) {
            Toast.makeText(this, "No options available for this message.", Toast.LENGTH_SHORT).show();
            return;
        }


        final CharSequence[] options = optionsList.toArray(new CharSequence[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message Options");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which].toString();

                if ("Copy Text".equals(selectedOption)) {
                    Log.d(TAG, "Selected: Copy Text for message " + message.getFirebaseMessageId());
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("message_text", message.getMessage()); // Use the decrypted text
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(ChatPageActivity.this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Copied text: " + message.getMessage());
                    } else {
                        Toast.makeText(ChatPageActivity.this, "Failed to copy text.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Clipboard service not available.");
                    }

                } else if ("Save Image to Gallery".equals(selectedOption)) {
                    Log.d(TAG, "Selected: Save Image to Gallery for message " + message.getFirebaseMessageId());
                    if ("image".equals(message.getType())) {
                        // message.getMessage() should contain the decrypted Base64 image string here
                        saveImageToGallery(message.getMessage(), message.getFirebaseMessageId()); // Pass Decrypted Base64 and ID
                    } else {
                        Log.e(TAG, "Attempted to save non-image message as image (dialog logic error?): " + message.getFirebaseMessageId());
                        Toast.makeText(ChatPageActivity.this, "This is not an image message.", Toast.LENGTH_SHORT).show();
                    }

                } else if ("Delete for Me".equals(selectedOption)) {
                    Log.d(TAG, "Selected: Delete for Me for message " + message.getFirebaseMessageId());
                    deleteMessageForMe(message.getFirebaseMessageId());

                } else if ("Delete for Everyone".equals(selectedOption)) {
                    Log.d(TAG, "Selected: Delete for Everyone for message " + message.getFirebaseMessageId());
                    // Double-check conditions again before executing delete
                    boolean areKeysAvailableForDeletion = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId); // *** MODIFIED CHECK ***
                    if (areKeysAvailableForDeletion && message.getFrom().equals(messageSenderID)) {
                        deleteMessageForEveryone(message.getFirebaseMessageId());
                    } else {
                        Log.e(TAG, "Attempted Delete for Everyone on a receiver message or without keys! Message ID: " + message.getFirebaseMessageId());
                        Toast.makeText(ChatPageActivity.this, "You can only delete your own messages for everyone in secure chats.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.show();
    }

    // Delete message only from the current user's Room database (the copy owned by them) (Keep as is)
    private void deleteMessageForMe(String firebaseMessageId) { /* ... (existing method) ... */
        if (TextUtils.isEmpty(firebaseMessageId) || TextUtils.isEmpty(messageSenderID)) {
            Log.e(TAG, "Cannot delete message for me, firebaseMessageId or messageSenderID is null or empty.");
            Toast.makeText(this, "Error deleting message locally.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseWriteExecutor.execute(() -> {
            int deletedRows = messageDao.deleteMessageByFirebaseId(firebaseMessageId, messageSenderID);
            if (deletedRows > 0) {
                Log.d(TAG, "Message deleted from Room (for me) for owner " + messageSenderID + ": " + firebaseMessageId);
            } else {
                Log.w(TAG, "Attempted to delete message " + firebaseMessageId + " for me, but it wasn't found in Room for owner " + messageSenderID);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to delete message locally.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Delete message from Firebase for both users (only for sender's own messages) (Keep as is, checks KeyManager state)
    @SuppressLint("RestrictedApi") // Keep if needed
    private void deleteMessageForEveryone(String firebaseMessageId) { /* ... (existing method) ... */
        // This should only be called for messages sent by the current user and if keys are available (checked in caller).
        if (TextUtils.isEmpty(firebaseMessageId) || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messageSenderID)) {
            Log.e(TAG, "Cannot delete message for everyone, missing essential IDs.");
            Toast.makeText(this, "Error deleting message for everyone.", Toast.LENGTH_SHORT).show();
            return;
        }
        // The path to the message node in Firebase is Messages/{conversationId}/{messageId}
        DatabaseReference messagePathInFirebase = rootRef.child("Messages").child(conversationId).child(firebaseMessageId);

        Log.d(TAG, "Attempting to delete message for everyone from Firebase: " + firebaseMessageId + " at path: " + messagePathInFirebase.getPath());

        messagePathInFirebase.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Message deleted from Firebase for everyone: " + firebaseMessageId);
                        runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Message deleted for everyone.", Toast.LENGTH_SHORT).show());
                    } else {
                        Log.e(TAG, "Failed to delete message from Firebase for everyone: " + firebaseMessageId, task.getException());
                        runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to delete message for everyone.", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    // --- Helper method to Save Decrypted Base64 image to Gallery --- (Keep as is, uses android.util.Base64 which is fine for images)
    private void saveImageToGallery(String decryptedBase64Image, String firebaseMessageId) { /* ... (existing method) ... */
        if (TextUtils.isEmpty(decryptedBase64Image)) {
            Log.e(TAG, "Cannot save image, decrypted Base64 data is null or empty.");
            Toast.makeText(this, "Image data is unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Use android.util.Base64 for image data (not crypto data)
            byte[] decodedBytes = Base64.decode(decryptedBase64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode Base64 string to bitmap.");
                Toast.makeText(this, "Failed to decode image data.", Toast.LENGTH_SHORT).show();
                return;
            }

            String filename = "IMG_" + (firebaseMessageId != null ? firebaseMessageId : System.currentTimeMillis()) + ".jpg";
            String mimeType = "image/jpeg";

            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "CircleUpImages");
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "CircleUpImages";
                File file = new File(imagesDir);
                if (!file.exists()) { file.mkdirs(); }
                File imageFile = new File(imagesDir, filename);
                values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
            }

            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri imageUri = resolver.insert(collection, values);

            if (imageUri == null) {
                Log.e(TAG, "Failed to create new MediaStore record.");
                Toast.makeText(this, "Failed to create image file.", Toast.LENGTH_SHORT).show();
                return;
            }

            try (OutputStream fos = resolver.openOutputStream(imageUri)) {
                if (fos == null) {
                    Log.e(TAG, "Failed to open output stream for imageUri: " + imageUri);
                    Toast.makeText(this, "Failed to save image data.", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0); resolver.update(imageUri, values, null, null);
                    }
                    return;
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                Log.d(TAG, "Image saved to gallery: " + filename);
                Toast.makeText(this, "Image saved to Gallery!", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0); resolver.update(imageUri, values, null, null);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error writing image to MediaStore: " + e.getMessage(), e);
                Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && imageUri != null) {
                    try { values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0); resolver.update(imageUri, values, null, null); }
                    catch (Exception ex) { Log.e(TAG, "Error updating MediaStore IS_PENDING on save failure", ex); }
                }
            }

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid Base64 string for image saving: " + e.getMessage());
            Toast.makeText(this, "Invalid image data.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "General error saving image: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred while saving image.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = null;
                if (inputStream != null) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                }

                if (bitmap != null) {
                    String base64Image = convertBitmapToBase64(bitmap); // Get Base64 string of the image
                    bitmap.recycle();
                    sendImageMessage(base64Image); // Pass the Base64 string to sendImageMessage
                } else {
                    Log.e(TAG, "Failed to decode bitmap from URI");
                    Toast.makeText(this, "Error loading image!", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException loading image from URI", e);
                Toast.makeText(this, "Error loading image!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "General error loading image from URI", e);
                Toast.makeText(this, "Error loading image!", Toast.LENGTH_SHORT).show();
            }
        }

        // Handle Wallpaper Image Selection/Capture (Keep as is)
        else if (requestCode == REQUEST_CODE_PICK_IMAGE_WALLPAPER && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            Log.d(TAG, "Gallery image selected for wallpaper: " + selectedImageUri);
            processSelectedWallpaper(selectedImageUri);
        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO_WALLPAPER && resultCode == RESULT_OK) {
            Log.d(TAG, "Photo taken for wallpaper: " + cameraPhotoUri);
            if (cameraPhotoUri != null) {
                processSelectedWallpaper(cameraPhotoUri);
            } else {
                Log.e(TAG, "Camera photo URI is null after taking photo.");
                Toast.makeText(this, "Error capturing photo.", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "Image picker or camera activity cancelled.");
            if (requestCode == REQUEST_CODE_TAKE_PHOTO_WALLPAPER && cameraPhotoUri != null) {
                try {
                    getContentResolver().delete(cameraPhotoUri, null, null);
                    Log.d(TAG, "Cleaned up temp camera file on cancel.");
                } catch (Exception e) {
                    Log.w(TAG, "Failed to clean up temp camera file on cancel", e);
                } finally {
                    cameraPhotoUri = null;
                }
            }
        }
    }


    // Method to Process and Save Wallpaper (Keep as is)
    private void processSelectedWallpaper(Uri imageUri) { /* ... (existing method) ... */
        if (imageUri == null) {
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "processSelectedWallpaper called with null URI.");
            return;
        }

        Log.d(TAG, "Processing selected wallpaper URI: " + imageUri);

        databaseWriteExecutor.execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = null;
                if (inputStream != null) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                }


                if (bitmap == null) {
                    Log.e(TAG, "Failed to get bitmap from URI: " + imageUri);
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
                    return;
                }

                // --- Resize/Compress Bitmap (Optional but Recommended) ---
                int maxWidth = 1080;
                int maxHeight = 1080;
                float ratioBitmap = (float) bitmap.getWidth() / (float) bitmap.getHeight();
                int finalWidth = maxWidth;
                int finalHeight = (int) (finalWidth / ratioBitmap);
                if (finalHeight > maxHeight) {
                    finalHeight = maxHeight;
                    finalWidth = (int) (maxHeight * ratioBitmap);
                }
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
                bitmap.recycle();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageData = baos.toByteArray();

                resizedBitmap.recycle();


                Log.d(TAG, "Image processed, size: " + imageData.length + " bytes.");

                // --- Save to Room DB ---
                if (TextUtils.isEmpty(messageSenderID) || TextUtils.isEmpty(conversationId) || wallpaperDao == null) {
                    Log.e(TAG, "Cannot save wallpaper: Missing sender ID, conversation ID, or wallpaper DAO.");
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error saving wallpaper settings.", Toast.LENGTH_SHORT).show());
                    return;
                }

                WallpaperEntity wallpaperEntity = new WallpaperEntity();
                wallpaperEntity.setOwnerUserId(messageSenderID);
                wallpaperEntity.setConversationId(conversationId);
                wallpaperEntity.setImageData(imageData);

                wallpaperDao.insertOrReplaceWallpaper(wallpaperEntity);
                Log.d(TAG, "Wallpaper saved/updated in Room for conversation: " + conversationId);

                // --- Apply Wallpaper to UI (on Main Thread) ---
                Bitmap finalDisplayBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                if (finalDisplayBitmap != null) {
                    runOnUiThread(() -> {
                        if (mainChatLayout != null) {
                            mainChatLayout.setBackground(new BitmapDrawable(getResources(), finalDisplayBitmap));
                            Log.d(TAG, "Wallpaper applied to chat background.");
                            Toast.makeText(ChatPageActivity.this, "Wallpaper set successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "mainChatLayout is null, cannot apply wallpaper.");
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to decode image data back to bitmap for display.");
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error displaying wallpaper.", Toast.LENGTH_SHORT).show());
                }

            } catch (IOException e) {
                Log.e(TAG, "IOException processing wallpaper URI: " + imageUri, e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error reading image file.", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e(TAG, "General error processing wallpaper: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show());
            } finally {
                if (cameraPhotoUri != null) {
                    try { getContentResolver().delete(cameraPhotoUri, null, null); Log.d(TAG, "Cleaned up temp camera file after processing."); }
                    catch (Exception e) { Log.w(TAG, "Failed to clean up temp camera file after processing", e); } finally { cameraPhotoUri = null; }
                }
            }
        });
    }


    // Method to Load Saved Wallpaper (Keep as is)
    private void loadSavedWallpaper() { /* ... (existing method) ... */
        if (TextUtils.isEmpty(messageSenderID) || TextUtils.isEmpty(conversationId) || wallpaperDao == null || mainChatLayout == null) {
            Log.w(TAG, "Cannot load saved wallpaper: Missing sender ID, conversation ID, wallpaper DAO, or main layout.");
            return;
        }

        databaseWriteExecutor.execute(() -> {
            try {
                WallpaperEntity wallpaper = wallpaperDao.getWallpaper(messageSenderID, conversationId);

                if (wallpaper != null && wallpaper.getImageData() != null && wallpaper.getImageData().length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(wallpaper.getImageData(), 0, wallpaper.getImageData().length);

                    if (bitmap != null) {
                        Log.d(TAG, "Saved wallpaper loaded from Room for conversation: " + conversationId);
                        runOnUiThread(() -> {
                            if (mainChatLayout != null) {
                                mainChatLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                                Log.d(TAG, "Saved wallpaper applied to chat background.");
                            } else {
                                Log.w(TAG, "mainChatLayout became null before wallpaper application.");
                            }
                        });
                    } else {
                        Log.e(TAG, "Failed to decode saved wallpaper image data from Room.");
                        databaseWriteExecutor.execute(() -> { try { wallpaperDao.deleteWallpaper(messageSenderID, conversationId); Log.d(TAG, "Deleted corrupt wallpaper entry from Room."); } catch (Exception e) { Log.e(TAG, "Error deleting corrupt wallpaper", e); } });
                    }
                } else {
                    Log.d(TAG, "No saved wallpaper found in Room for conversation: " + conversationId);
                    runOnUiThread(() -> { if (mainChatLayout != null) { mainChatLayout.setBackground(null); Log.d(TAG, "Set default/no background as no wallpaper was saved."); } });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading saved wallpaper from Room: " + e.getMessage(), e);
                runOnUiThread(() -> { if (mainChatLayout != null) { mainChatLayout.setBackground(null); Log.e(TAG, "Error loading wallpaper, set default background."); } });
            }
        });
    }

    // Method to Remove Wallpaper (Keep as is)
    private void removeWallpaper() { /* ... (existing method) ... */
        if (TextUtils.isEmpty(messageSenderID) || TextUtils.isEmpty(conversationId) || wallpaperDao == null) {
            Log.e(TAG, "Cannot remove wallpaper: Missing sender ID, conversation ID, or wallpaper DAO.");
            Toast.makeText(this, "Error removing wallpaper.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseWriteExecutor.execute(() -> {
            try {
                int deletedRows = wallpaperDao.deleteWallpaper(messageSenderID, conversationId);

                if (deletedRows > 0) {
                    Log.d(TAG, "Wallpaper removed from Room for conversation: " + conversationId);
                    runOnUiThread(() -> {
                        if (mainChatLayout != null) {
                            mainChatLayout.setBackground(null);
                            Log.d(TAG, "Wallpaper removed from chat background.");
                            Toast.makeText(ChatPageActivity.this, "Wallpaper removed.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "mainChatLayout is null, cannot remove wallpaper from UI.");
                        }
                    });
                } else {
                    Log.w(TAG, "Attempted to remove wallpaper but no entry found in Room for conversation: " + conversationId);
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "No wallpaper to remove.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error removing wallpaper from Room: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error removing wallpaper.", Toast.LENGTH_SHORT).show());
            }
        });
    }


    @Override
    protected void onDestroy() { /* ... (existing method) ... */
        super.onDestroy();
        Log.d(TAG, "ChatPageActivity onDestroy");
        removeMessageListener();
        removeScrollListener();
        if (chatMessagesLiveData != null) {
            chatMessagesLiveData.removeObservers(this);
            Log.d(TAG, "chatMessagesLiveData observers removed.");
        }
        if (cameraPhotoUri != null) {
            try { getContentResolver().delete(cameraPhotoUri, null, null); Log.d(TAG, "Cleaned up temp camera file in onDestroy."); }
            catch (Exception e) { Log.w(TAG, "Failed to clean up temp camera file in onDestroy", e); } finally { cameraPhotoUri = null; }
        }
        Log.d(TAG, "ChatPageActivity onDestroy finished.");
    }

    // This method converts Bitmap to Base64 *image data*, not crypto bytes. Keep as is.
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT); // Keep using android.util.Base64 for image data
    }


    private void attachScrollListener() { /* ... (existing method) ... */
        if (messagesList == null) { Log.w(TAG, "Cannot attach scroll listener, messagesList is null."); return; }
        if (recyclerViewScrollListener == null) {
            recyclerViewScrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        Log.d(TAG, "Scroll state idle, marking visible messages as seen.");
                        markVisibleMessagesAsSeen();
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            };
            messagesList.addOnScrollListener(recyclerViewScrollListener);
            Log.d(TAG, "Scroll listener attached.");
        } else { Log.d(TAG, "Scroll listener already attached."); }
    }

    private void removeScrollListener() { /* ... (existing method) ... */
        if (messagesList == null) { Log.w(TAG, "Cannot remove scroll listener, messagesList is null."); return; }
        if (recyclerViewScrollListener != null) {
            messagesList.removeOnScrollListener(recyclerViewScrollListener);
            recyclerViewScrollListener = null;
            Log.d(TAG, "Scroll listener removed.");
        } else { Log.d(TAG, "Scroll listener was not attached."); }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "ChatPageActivity onStart");
        attachMessageListener();
        retryPendingMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ChatPageActivity onResume. Checking KeyManager state and potentially refreshing display.");

        // Re-check if private key and conversation key are now available
        boolean isSecureChatCurrentlyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId); // *** MODIFIED CHECK ***

        // Get the current state of the input UI
        boolean isUiCurrentlyEnabled = messageInputText != null && messageInputText.isEnabled();


        if (isSecureChatCurrentlyAvailable && !isUiCurrentlyEnabled) {
            Log.d(TAG, "onResume: Keys became available and UI was disabled. Enabling UI and refreshing display.");
            if (messageInputText != null) { messageInputText.setEnabled(true); messageInputText.setHint("Enter Message..."); }
            if (sendMessageButton != null) sendMessageButton.setEnabled(true);
            if (send_imgmsg_btn != null) send_imgmsg_btn.setEnabled(true);
            forceRefreshDisplay(); // Force refresh to decrypt messages
            Toast.makeText(this, "Secure chat enabled.", Toast.LENGTH_SHORT).show();

        } else if (!isSecureChatCurrentlyAvailable && isUiCurrentlyEnabled) {
            Log.w(TAG, "onResume: Keys became unavailable and UI was enabled. Disabling UI.");
            if (messageInputText != null) { messageInputText.setEnabled(false); messageInputText.setHint("Secure chat unavailable"); }
            if (sendMessageButton != null) sendMessageButton.setEnabled(false);
            if (send_imgmsg_btn != null) send_imgmsg_btn.setEnabled(false);
            Toast.makeText(this, "Secure chat disabled.", Toast.LENGTH_LONG).show();
            forceRefreshDisplay(); // Force refresh to show messages as locked
        } else {
            if (isSecureChatCurrentlyAvailable) {
                Log.d(TAG, "onResume: Keys are available and UI is enabled. Forcing display refresh defensively.");
                forceRefreshDisplay(); // Re-process messages with available keys
            } else {
                Log.d(TAG, "onResume: Keys are not available and UI is disabled. No action needed, UI state already reflects this.");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "ChatPageActivity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "ChatPageActivity onStop");
    }


    // --- Add a helper method to send the actual push notification --- (Keep as is)
    private void sendPushNotification(String recipientFirebaseUID, String title, String messageContent) { /* ... (existing method) ... */
        if (oneSignalApiService == null || TextUtils.isEmpty(recipientFirebaseUID)) { Log.e(TAG, "sendPushNotification: API service not initialized or recipient UID is empty."); return; }
        Log.d(TAG, "Preparing to send push notification to recipient UID (External ID): " + recipientFirebaseUID);
        JsonObject notificationBody = new JsonObject();
        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID);
        JsonArray externalUserIds = new JsonArray();
        externalUserIds.add(recipientFirebaseUID);
        notificationBody.add("include_external_user_ids", externalUserIds);
        notificationBody.add("headings", new Gson().toJsonTree(Collections.singletonMap("en", title)));
        notificationBody.add("contents", new Gson().toJsonTree(Collections.singletonMap("en", messageContent)));
        JsonObject data = new JsonObject();
        data.addProperty("senderId", messageSenderID);
        data.addProperty("recipientId", messageReceiverID);
        data.addProperty("conversationId", conversationId);
        notificationBody.add("data", data);
        notificationBody.addProperty("small_icon", "app_icon_circleup");
        Log.d(TAG, "Making API call to OneSignal...");
        oneSignalApiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) { /* ... log success/error ... */
                if (response.isSuccessful()) {
                    Log.d(TAG, "OneSignal API call successful. Response Code: " + response.code());
                    try (ResponseBody responseBody = response.body()) { String resBody = responseBody != null ? responseBody.string() : "N/A"; Log.d(TAG, "OneSignal API Response Body: " + resBody); }
                    catch (IOException e) { Log.e(TAG, "Failed to read OneSignal response body", e); }
                } else {
                    Log.e(TAG, "OneSignal API call failed. Response Code: " + response.code());
                    try (ResponseBody errorBody = response.errorBody()) { String errBody = errorBody != null ? errorBody.string() : "N/A"; Log.e(TAG, "OneSignal API Error Body: " + errBody); }
                    catch (IOException e) { Log.e(TAG, "Failed to read OneSignal error body", e); }
                    Log.w(TAG, "Failed to send push notification via OneSignal API.");
                }
            }
            @Override public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) { Log.e(TAG, "OneSignal API call failed (network error)", t); Log.w(TAG, "Failed to send push notification due to network error."); }
        });
        Log.d(TAG, "OneSignal API call enqueued.");
    }


    // --- NEW Method to show wallpaper options dialog --- (Keep as is)
    private void showChangeWallpaperDialog() { /* ... (existing method) ... */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Chat Wallpaper");

        final CharSequence[] options = {"Choose from Gallery", "Take Photo", "Remove Wallpaper", "Cancel"};

        builder.setItems(options, (dialog, which) -> {
            String selectedOption = options[which].toString();

            if ("Choose from Gallery".equals(selectedOption)) {
                Log.d(TAG, "Selected: Choose from Gallery");
                checkPermissionsAndOpenGallery();
            } else if ("Take Photo".equals(selectedOption)) {
                Log.d(TAG, "Selected: Take Photo");
                checkPermissionsAndOpenCamera();
            } else if ("Remove Wallpaper".equals(selectedOption)) {
                Log.d(TAG, "Selected: Remove Wallpaper");
                removeWallpaper();
            } else if ("Cancel".equals(selectedOption)) {
                dialog.dismiss();
            }
        });

        builder.show();
    }


    // --- NEW Permission Methods --- (Keep as is)
    private void checkPermissionsAndOpenGallery() { /* ... (existing method) ... */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        } else { openGalleryForWallpaper(); }
    }

    private void checkPermissionsAndOpenCamera() { /* ... (existing method) ... */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            String[] permissions;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { permissions = new String[]{Manifest.permission.CAMERA}; }
            else { permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}; }
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION);
        } else { openCameraForWallpaper(); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { /* ... (existing method) ... */
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) { if (result != PackageManager.PERMISSION_GRANTED) { allPermissionsGranted = false; break; } }
            if (allPermissionsGranted) { Toast.makeText(this, "Permissions granted. Try again.", Toast.LENGTH_SHORT).show(); }
            else { Toast.makeText(this, "Permissions denied. Cannot set wallpaper.", Toast.LENGTH_SHORT).show(); }
        }
    }

    // --- NEW Intent Methods --- (Keep as is)
    private void openGalleryForWallpaper() { /* ... (existing method) ... */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { Toast.makeText(this, "Storage permission required to open gallery.", Toast.LENGTH_SHORT).show(); return; }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE_WALLPAPER);
    }

    private void openCameraForWallpaper() { /* ... (existing method) ... */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { Toast.makeText(this, "Camera permission required to take photo.", Toast.LENGTH_SHORT).show(); return; }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { Toast.makeText(this, "Storage permission required to save photo.", Toast.LENGTH_SHORT).show(); return; }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try { photoFile = createImageFile(); }
            catch (IOException ex) { Log.e(TAG, "Error creating temp image file for camera", ex); Toast.makeText(this, "Error creating file for photo.", Toast.LENGTH_SHORT).show(); return; }
            if (photoFile != null) {
                try {
                    cameraPhotoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    Log.d(TAG, "Starting camera intent with URI: " + cameraPhotoUri);
                    startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PHOTO_WALLPAPER);
                } catch (IllegalArgumentException e) { Log.e(TAG, "FileProvider setup error or file issue", e); Toast.makeText(this, "Error accessing file storage. Check configuration.", Toast.LENGTH_LONG).show(); }
            }
        } else { Toast.makeText(this, "No camera app found.", Toast.LENGTH_SHORT).show(); }
    }

    // Helper method to create a temporary file for the camera photo (Keep as is)
    private File createImageFile() throws IOException { /* ... (existing method) ... */
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) { Log.e(TAG, "getExternalFilesDir returned null"); throw new IOException("External files directory not available."); }
        if (!storageDir.exists()) { if (!storageDir.mkdirs()) { Log.e(TAG, "Failed to create directory: " + storageDir.getAbsolutePath()); throw new IOException("Failed to create directory for temporary files."); } }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "Created temporary image file: " + image.getAbsolutePath());
        return image;
    }


    // Helper method to update chat summary for a specific user (sender or receiver) (Keep as is, uses Base64 string as preview)
    @SuppressLint("RestrictedApi") // Keep if needed
    private void updateChatSummaryForUser(String summaryOwnerId, String chatPartnerId,
                                          String conversationId, String messagePushId,
                                          String lastMessageContentPreview, String messageType, // Use lastMessageContentPreview (Base64 or placeholder)
                                          Object firebaseTimestamp, String lastMessageSenderId) { /* ... (existing method) ... */
        if (TextUtils.isEmpty(summaryOwnerId) || TextUtils.isEmpty(chatPartnerId) || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messagePushId) || TextUtils.isEmpty(messageType) || firebaseTimestamp == null || TextUtils.isEmpty(lastMessageSenderId)) {
            Log.e(TAG, "Cannot update chat summary: Missing essential input data.");
            return;
        }

        DatabaseReference summaryRef = rootRef.child("ChatSummaries").child(summaryOwnerId).child(chatPartnerId);
        Log.d(TAG, "Updating summary for owner " + summaryOwnerId + " with partner " + chatPartnerId + " at path: " + summaryRef.getPath());


        Map<String, Object> summaryUpdates = new HashMap<>();
        summaryUpdates.put("conversationId", conversationId);
        summaryUpdates.put("lastMessageId", messagePushId);
        summaryUpdates.put("lastMessageContentPreview", lastMessageContentPreview != null ? lastMessageContentPreview : ""); // Store the preview content (Base64 or placeholder)
        summaryUpdates.put("lastMessageTimestamp", firebaseTimestamp);
        summaryUpdates.put("lastMessageSenderId", lastMessageSenderId);
        summaryUpdates.put("lastMessageType", messageType);

        summaryUpdates.put("participants/" + summaryOwnerId, true);
        summaryUpdates.put("participants/" + chatPartnerId, true);


        if (summaryOwnerId.equals(lastMessageSenderId)) {
            Log.d(TAG, "Setting unread count to 0 for sender (" + summaryOwnerId + ") in their summary.");
            summaryUpdates.put("unreadCounts/" + summaryOwnerId, 0);

            summaryRef.updateChildren(summaryUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) { Log.d(TAG, "Sender's chat summary updated successfully for " + conversationId + " (Owner: " + summaryOwnerId + ")"); }
                else { Log.e(TAG, "Failed to update sender's chat summary for " + conversationId + " (Owner: " + summaryOwnerId + ")", task.getException()); }
            });

        } else {
            Log.d(TAG, "Attempting to increment unread count for receiver (" + summaryOwnerId + ") in summary " + conversationId);
            summaryRef.child("unreadCounts").child(summaryOwnerId).runTransaction(new Transaction.Handler() {
                @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer currentCount = currentData.getValue(Integer.class);
                    if (currentCount == null) { currentData.setValue(1); } else { currentData.setValue(currentCount + 1); }
                    return Transaction.success(currentData);
                }
                @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (error != null) { Log.e(TAG, "Firebase transaction for unread count failed for receiver " + summaryOwnerId + ": " + error.getMessage()); }
                    else if (committed) { Log.d(TAG, "Unread count incremented for receiver " + summaryOwnerId + " in summary " + conversationId); }
                    else { Log.d(TAG, "Firebase transaction for unread count not committed for receiver " + summaryOwnerId + "."); }
                    summaryRef.updateChildren(summaryUpdates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) { Log.d(TAG, "Receiver's chat summary updated successfully for " + conversationId + " (Owner: " + summaryOwnerId + ")"); }
                        else { Log.e(TAG, "Failed to update receiver's chat summary for " + conversationId + " (Owner: " + summaryOwnerId + ")", task.getException()); }
                    });
                }
            });
        }
    }


    // --- Modified Public Method to Force Refresh Display ---
    /**
     * Forces the message list to re-process its current data from the LiveData's last value.
     * Useful when decryption keys become available after the data was initially loaded, or data changes in Room.
     * Assumes the LiveData's last value is still available in the `chatMessagesLiveData` object.
     * This method should be called on the Main Thread.
     */
//    public void forceRefreshDisplay() {
//        Log.d(TAG, "🟢 forceRefreshDisplay() called for conversation " + conversationId);
//
//        // Ensure LiveData has a value and adapter exists
//        if (chatMessagesLiveData == null || chatMessagesLiveData.getValue() == null || messageAdapter == null || messagesList == null || messagesList.getLayoutManager() == null) {
//            Log.w(TAG, "forceRefreshDisplay skipped: LiveData, value, adapter, messagesList, or LayoutManager is null.");
//            if (messageAdapter != null) { messagesArrayList.clear(); messageAdapter.notifyDataSetChanged(); }
//            boolean isSecureChatCurrentlyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
//            if (!isSecureChatCurrentlyAvailable) {
//                if (messageInputText != null) { messageInputText.setEnabled(false); messageInputText.setHint("Secure chat unavailable"); }
//                if (sendMessageButton != null) sendMessageButton.setEnabled(false);
//                if (send_imgmsg_btn != null) send_imgmsg_btn.setEnabled(false);
//            }
//            Log.d(TAG, "✅ forceRefreshDisplay finished (skipped due to nulls).");
//            return;
//        }
//
//        List<MessageEntity> currentMessages = chatMessagesLiveData.getValue();
//
//        Log.d(TAG, "forceRefreshDisplay triggered. Re-processing " + (currentMessages != null ? currentMessages.size() : 0) + " MessageEntity objects from Room for owner " + messageSenderID);
//
//        // Get the *single* conversation key from KeyManager *before* processing messages.
//        SecretKey conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId); // *** MODIFIED ***
//        // Check if the user's main private key is available (required to decrypt conversation keys, indicates account is unlocked)
//        boolean isPrivateKeyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable();
//        // Check if the single conversation key is available
//        boolean isConversationKeyAvailable = (conversationAESKey != null); // *** MODIFIED CHECK ***
//
//        Log.d(TAG, "KeyManager State: PrivateKey Available = " + isPrivateKeyAvailable + ", Conversation Key Available = " + isConversationKeyAvailable); // *** MODIFIED LOG ***
//
//        // Clear the list that feeds the adapter for a fresh populate
//        messagesArrayList.clear();
//
//        if (currentMessages != null && !currentMessages.isEmpty()) {
//
//
//            // *** ADD THESE VARIABLE DECLARATIONS HERE ***
//            boolean decryptionAttempted; // Declare here
//            boolean decryptionSuccessful; // Declare here
//            String decryptionAttemptOutcome = ""; // Declare here
//            // *** END ADDITIONS ***
//            // --- *** PROCESS & DECRYPT Messages before adding to the list for the adapter *** ---
//
//            for (MessageEntity storedMessageEntity : currentMessages) {
//                if (storedMessageEntity == null) { Log.w(TAG, "Skipping null MessageEntity from Room list."); continue; }
//
//                String storedMessageContentBase64 = storedMessageEntity.getMessage(); // This is the Base64 string from Room
//                String messageType = storedMessageEntity.getType();
//                String firebaseMessageId = storedMessageEntity.getFirebaseMessageId();
//                long messageTimestamp = storedMessageEntity.getTimestamp();
//
//                String displayedContent = "";
//
//
//                // Determine the expected state and type placeholder
//                boolean messageShouldBeEncrypted = ("text".equals(messageType) || "image".equals(messageType));
//                String typePlaceholder = "";
//                if ("image".equals(messageType)) typePlaceholder = "Sent an Image";
//                else if ("file".equals(messageType)) typePlaceholder = "[File]";
//                else if ("system_key_change".equals(messageType)) typePlaceholder = "[System Message]"; // Handle system message
//                else typePlaceholder = "";
//
//
//                // --- Logic to determine what to display ---
//
//                // Handle system message type first (it's not encrypted text/image)
//                if ("system_key_change".equals(messageType)) {
//                    decryptionAttemptOutcome = "System Message";
//                    displayedContent = storedMessageContentBase64; // Stored content is the actual message text for system type
//                    if (TextUtils.isEmpty(displayedContent)) {
//                        displayedContent = typePlaceholder; // Use placeholder if stored content is empty
//                    }
//                    Log.d(TAG, "Processing system_key_change message " + firebaseMessageId + ". Displaying stored content.");
//
//                }
//
//                // 2. Handle Text or Image messages (these *could* be encrypted)
//                else if ("text".equals(messageType) || "image".equals(messageType)) {
//
//                }
//                // Attempt decryption only IF it's a text/image type AND content exists AND keys are available
//                else if (messageShouldBeEncrypted && !TextUtils.isEmpty(storedMessageContentBase64) && isPrivateKeyAvailable && isConversationKeyAvailable) { // *** MODIFIED CHECK ***
//
//                    decryptionAttempted = true;
//                    decryptionAttemptOutcome = "Attempted";
//                    Log.d(TAG, "Attempting decryption for message " + firebaseMessageId + " (Type: " + messageType + ", Msg Timestamp: " + messageTimestamp + ").");
//
//                    // --- Decode the Base64 string from Room BEFORE attempting decryption ---
//                    byte[] encryptedBytesWithIV;
//                    try {
//                        // Use CryptoUtils.base64ToBytes to decode the Base64 string to bytes
//                        encryptedBytesWithIV = CryptoUtils.base64ToBytes(storedMessageContentBase64); // *** MODIFIED ***
//                        if (encryptedBytesWithIV == null || encryptedBytesWithIV.length == 0) { // Defensive check
//                            Log.w(TAG, "Decoded encrypted bytes null or empty for msg: " + firebaseMessageId);
//                            decryptionAttemptOutcome = "Decoded Empty";
//                            displayedContent = (messageType.equals("image") ? "[Invalid Encrypted Image Data]" : "[Invalid Encrypted Data]");
//                            // No need to try decrypting null/empty bytes, move to next message
//                            // Continue loop implicit here if not explicitly returned/broken
//                        } else {
//                            // --- Decrypt using the single conversation key ---
//                            try {
//                                // Use CryptoUtils.decryptMessageWithAES which takes byte[] and returns String
//                                String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytesWithIV, conversationAESKey); // *** MODIFIED ***
//
//                                // If decryption succeeds:
//                                if ("text".equals(messageType)) {
////                                    int maxLength = 50;
////                                    displayedContent = decryptedContent.length() > maxLength ?
////                                            decryptedContent.substring(0, maxLength) + "..." :
////                                            decryptedContent;
//                                    displayedContent = decryptedContent; // Use decrypted text for text type
//                                    Log.d(TAG, "Message " + firebaseMessageId + " decrypted SUCCESSFULLY.");
//
//                                } else if ("image".equals(messageType)) {
//                                    // For images, the decrypted content is the Base64 image data string.
//                                    // The adapter will handle displaying the image from this Base64.
//                                    displayedContent = decryptedContent; // Use decrypted Base64 string for image type
//                                } else {
//                                    displayedContent = "[Unknown Type - Decrypted]";
//                                    Log.w(TAG, "Unexpected encrypted message type '" + messageType + "' successfully processed for decryption for msg: " + firebaseMessageId);
//                                }
//
//                                decryptionSuccessful = true;
//                                decryptionAttemptOutcome = "Success";
//                                Log.d(TAG, "Message " + firebaseMessageId + " decrypted SUCCESSFULLY.");
//                                // Decryption succeeded, exit this decryption attempt branch
//
//                            } catch (IllegalArgumentException e) {
//                                // Should ideally not happen if byte[] wasn't null/empty, but safety
//                                Log.w(TAG, "Error processing decrypted bytes for message " + firebaseMessageId + ": " + e.getMessage());
//                                decryptionAttemptOutcome = "Processing Decrypted Bytes Error"; // Specific outcome
//                                displayedContent = "[Decryption Error]"; // Fallback placeholder
//                            } catch (BadPaddingException |
//                                     IllegalBlockSizeException |
//                                     InvalidKeyException |// Catch InvalidKeyException here if the key is wrong
//                                     InvalidAlgorithmParameterException e) {
//                                // Crypto decryption failed for THIS key!
//                                Log.d(TAG, "Decryption FAILED for message " + firebaseMessageId + ". Exception: " + e.getClass().getSimpleName() + " (" + e.getMessage() + ")");
//                                decryptionAttemptOutcome = "Crypto Error (" + e.getClass().getSimpleName() + ")";
//                                displayedContent = "[Encrypted Message - Failed]"; // Placeholder
//                            } catch (Exception e) {
//                                Log.w(TAG, "Unexpected error during decryption of message " + firebaseMessageId + ".", e);
//                                decryptionAttemptOutcome = "Unexpected Crypto Error";
//                                displayedContent = "[Encrypted Message - Failed]"; // Placeholder
//                            }
//                        } // End else (decoded bytes not null/empty)
//
//                    } catch (IllegalArgumentException e) { // Base64 decoding error
//                        Log.e(TAG, "Base64 decoding error decrypting message " + firebaseMessageId + " for display.", e);
//                        decryptionAttemptOutcome = "Base64 Error";
//                        displayedContent = "[Invalid Encrypted Data]"; // Placeholder on Base64 error
//                    } catch (Exception e) { // Any other unexpected decoding error
//                        Log.e(TAG, "Unexpected error decoding Base64 for message " + firebaseMessageId + " for display.", e);
//                        decryptionAttemptOutcome = "Decoding Error";
//                        displayedContent = "[Invalid Encrypted Data]"; // Placeholder
//                    }
//
//                } else if (messageShouldBeEncrypted && !TextUtils.isEmpty(storedMessageContentBase64) && (!isPrivateKeyAvailable || !isConversationKeyAvailable)) { // *** MODIFIED CHECK ***
//                    // Content exists and should be encrypted, but keys are missing/unavailable
//                    decryptionAttemptOutcome = "Keys Unavailable";
//                    Log.d(TAG, "Message " + firebaseMessageId + " is encrypted but keys are unavailable (PrivKey: " + isPrivateKeyAvailable + ", ConvKey: " + isConversationKeyAvailable + "). Showing [Locked] placeholder."); // *** MODIFIED LOG ***
//                    displayedContent = "[Locked]"; // Placeholder indicating account needs unlocking or keys need loading
//
//                }
//                else if (TextUtils.isEmpty(storedMessageContentBase64)) {
//                    // Stored content is explicitly empty in Room
//                    decryptionAttemptOutcome = "Content Empty";
//                    Log.w(TAG, "Stored content empty for message " + firebaseMessageId + " (Type: " + messageType + "). Displaying type placeholder.");
//                    displayedContent = typePlaceholder; // Fallback to a type-based placeholder
//
//                } else {
//                    // Default case: Not expected to be encrypted (file), OR keys are available BUT decryption wasn't attempted
//                    // Display the content Base64 string as it was stored in Room.
//                    decryptionAttemptOutcome = "Not Encrypted or Skipped"; // *** MODIFIED LOG ***
//                    Log.d(TAG, "Displaying stored content as is for message " + firebaseMessageId + " (Type: " + messageType + ").");
//                    displayedContent = storedMessageContentBase64; // Use the content stored in Room
//
//                    // Safety check: if stored content is empty for unexpected reason, use placeholder
//                    if (TextUtils.isEmpty(displayedContent)) {
//                        displayedContent = typePlaceholder;
//                        Log.w(TAG, "Stored content empty even in default case for message " + firebaseMessageId + ". Using type placeholder.");
//                    }
//                }
//
//
//                // Create a *new* MessageEntity object with the processed (decrypted or placeholder) content for the adapter
//                MessageEntity displayedMessageEntity = new MessageEntity();
//                // Copy all fields, setting the PROCESSED last message.
//                displayedMessageEntity.setFirebaseMessageId(storedMessageEntity.getFirebaseMessageId());
//                displayedMessageEntity.setOwnerUserId(storedMessageEntity.getOwnerUserId());
//                displayedMessageEntity.setMessage(displayedContent); // *** Set the PROCESSED content *** <-- MODIFIED
//                displayedMessageEntity.setType(storedMessageEntity.getType());
//                displayedMessageEntity.setFrom(storedMessageEntity.getFrom());
//                displayedMessageEntity.setTo(storedMessageEntity.getTo());
//                displayedMessageEntity.setSendTime(storedMessageEntity.getSendTime());
//                displayedMessageEntity.setSeen(storedMessageEntity.isSeen());
//                displayedMessageEntity.setSeenTime(storedMessageEntity.getSeenTime());
//                displayedMessageEntity.setStatus(storedMessageEntity.getStatus());
//                displayedMessageEntity.setTimestamp(storedMessageEntity.getTimestamp());
//
//                messagesArrayList.add(displayedMessageEntity); // Add the processed message to the list for the adapter
//
//                // Log the final outcome for this message after processing
//                Log.d(TAG, "Processed message " + firebaseMessageId + " for display. Outcome: " + decryptionAttemptOutcome + ", Displayed: '" + (displayedContent != null && displayedContent.length() > 50 ? displayedContent.substring(0, 50) + "..." : displayedContent) + "'"); // Add null check for displayedContent
//
//            } // End of for loop
//        } // End of if (currentMessages != null && !currentMessages.isEmpty()) check
//
//
//        // Sort the list (if necessary, should already be sorted by DAO query which orders by timestamp ASC)
//        if (messagesArrayList.size() > 1) {
//            Collections.sort(messagesArrayList, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
//        }
//
//        messageAdapter.notifyDataSetChanged();
//        Log.d(TAG, "Submitted " + messagesArrayList.size() + " processed messages to adapter in forceRefreshDisplay.");
//
//        // Auto-scroll to bottom
//        if (!messagesArrayList.isEmpty() && messagesList != null && messagesList.getLayoutManager() != null) {
//            LinearLayoutManager layoutManager = (LinearLayoutManager) messagesList.getLayoutManager();
//            if (layoutManager.getItemCount() > 0) {
//                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
//                int totalItemCount = layoutManager.getItemCount();
//                boolean isAtBottom = lastVisiblePosition != RecyclerView.NO_POSITION && lastVisiblePosition == totalItemCount - 1;
//                int tolerance = 5;
//                boolean isNearBottom = lastVisiblePosition != RecyclerView.NO_POSITION && lastVisiblePosition >= totalItemCount - 1 - tolerance;
//                // Heuristic guess for initial load
//                boolean isInitialLoad = messagesArrayList.size() > 0 && totalItemCount == messagesArrayList.size() && messagesArrayList.size() < 50; // Consider a threshold like 50 messages for initial load
//                // Scroll to the bottom if the user was already near the bottom, or if it's likely an initial load of a small list
//                if (isAtBottom || isNearBottom || isInitialLoad) { // *** MODIFIED CONDITION ***
//                    messagesList.scrollToPosition(totalItemCount - 1);
//                    Log.d(TAG, "Auto-scrolling to bottom. Total items: " + totalItemCount + ", Last visible: " + lastVisiblePosition + ", Is near bottom: " + isNearBottom + ", Is at bottom: " + isAtBottom + ", Is Initial Load (heuristic): " + isInitialLoad);
//                } else {
//                    Log.d(TAG, "Skipping auto-scroll. User is not near bottom. Total items: " + totalItemCount + ", Last visible: " + lastVisiblePosition);
//                }
//            } else if (messagesList != null && messagesList.getLayoutManager() != null && ((LinearLayoutManager) messagesList.getLayoutManager()).getItemCount() == 0) {
//                Log.d(TAG, "Message list is empty after forceRefreshDisplay, no scrolling.");
//            } else {
//                // Redundant check, already handled
//            }
//
//            // Mark visible messages as seen
//            markVisibleMessagesAsSeen();
//        }
//
//        // --- Check and Update UI enabled state based on the *current* KeyManager state ---
//        // UI should be enabled if the user's private key is available AND the conversation key is loaded for this chat.
//        boolean isSecureChatCurrentlyAvailable = isPrivateKeyAvailable && isConversationKeyAvailable; // *** MODIFIED CHECK ***
//
//        if (isSecureChatCurrentlyAvailable && (messageInputText != null && !messageInputText.isEnabled())) {
//            if (messageInputText != null) { messageInputText.setEnabled(true); messageInputText.setHint("Enter Message..."); }
//            if (sendMessageButton != null) sendMessageButton.setEnabled(true);
//            if (send_imgmsg_btn != null) send_imgmsg_btn.setEnabled(true);
//            Log.d(TAG, "forceRefreshDisplay enabled UI as keys are now available.");
//        } else if (!isSecureChatCurrentlyAvailable && (messageInputText != null && messageInputText.isEnabled())) {
//            if (messageInputText != null) { messageInputText.setEnabled(false); messageInputText.setHint("Secure chat unavailable"); }
//            if (sendMessageButton != null) sendMessageButton.setEnabled(false);
//            if (send_imgmsg_btn != null) send_imgmsg_btn.setEnabled(false);
//            Log.d(TAG, "forceRefreshDisplay disabled UI as keys are now unavailable.");
//        } else {
//            Log.d(TAG, "forceRefreshDisplay: UI state already consistent with key state (Secure Chat Available: " + isSecureChatCurrentlyAvailable + ").");
//        }
//        Log.d(TAG, "✅ forceRefreshDisplay finished.");
//    }


    // --- Modified Public Method to Force Refresh Display ---
    /**
     * Forces the message list to re-process its current data from the LiveData's last value.
     * Useful when decryption keys become available after the data was initially loaded, or data changes in Room.
     * Assumes the LiveData's last value is still available in the `chatMessagesLiveData` object.
     * This method should be called on the Main Thread.
     */
    public void forceRefreshDisplay() {
        Log.d(TAG, "🟢 forceRefreshDisplay() called for conversation " + conversationId);

        // Ensure LiveData has a value and adapter exists
        if (chatMessagesLiveData == null || chatMessagesLiveData.getValue() == null || messageAdapter == null || messagesList == null || messagesList.getLayoutManager() == null) {
            Log.w(TAG, "forceRefreshDisplay skipped: LiveData, value, adapter, messagesList, or LayoutManager is null.");
            // Clear adapter list defensively even if components are null/empty
            if (messageAdapter != null) {
                // Clear the messagesArrayList (the adapter's data source)
                messagesArrayList.clear();
                // Notify the adapter that the dataset has changed (now empty)
                messageAdapter.notifyDataSetChanged();
            }

            // Still disable UI if keys are unavailable, even if no messages displayed
            boolean isSecureChatCurrentlyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
            if (!isSecureChatCurrentlyAvailable) {
                if (messageInputText != null) { messageInputText.setEnabled(false); messageInputText.setHint("Secure chat unavailable"); }
                if (sendMessageButton != null) sendMessageButton.setEnabled(false);
                if (send_imgmsg_btn != null) send_imgmsg_btn.setEnabled(false);
            } else {
                // If keys ARE available but we skipped due to other nulls, ensure UI is enabled
                if (messageInputText != null) { messageInputText.setEnabled(true); messageInputText.setHint("Enter Message..."); }
                if (sendMessageButton != null) sendMessageButton.setEnabled(true);
                if (send_imgmsg_btn != null) send_imgmsg_btn.setEnabled(true);
            }

            Log.d(TAG, "✅ forceRefreshDisplay finished (skipped due to nulls).");
            return;
        }

        List<MessageEntity> currentMessages = chatMessagesLiveData.getValue();

        Log.d(TAG, "forceRefreshDisplay triggered. Re-processing " + (currentMessages != null ? currentMessages.size() : 0) + " MessageEntity objects from Room for owner " + messageSenderID);

        // Get the *single* conversation key from KeyManager *before* processing messages.
        SecretKey conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId); // *** MODIFIED ***
        // Check if the user's main private key is available (required to decrypt conversation keys, indicates account is unlocked)
        boolean isPrivateKeyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable();
        // Check if the single conversation key is available
        boolean isConversationKeyAvailable = (conversationAESKey != null); // *** MODIFIED CHECK ***

        Log.d(TAG, "KeyManager State: PrivateKey Available = " + isPrivateKeyAvailable + ", Conversation Key Available = " + isConversationKeyAvailable); // *** MODIFIED LOG ***

        // Clear the list that feeds the adapter for a fresh populate
        messagesArrayList.clear();

        if (currentMessages != null && !currentMessages.isEmpty()) {

            // --- *** PROCESS Messages before adding to the list for the adapter *** ---

            for (MessageEntity storedMessageEntity : currentMessages) {
                if (storedMessageEntity == null) {
                    Log.w(TAG, "Skipping null MessageEntity from Room list.");
                    continue; // Skip this iteration
                }

                String storedMessageContentBase64 = storedMessageEntity.getMessage(); // This is the Base64 string from Room (could be encrypted or plaintext)
                String messageType = storedMessageEntity.getType();
                String firebaseMessageId = storedMessageEntity.getFirebaseMessageId();
                long messageTimestamp = storedMessageEntity.getTimestamp();
                String scheduledTime = storedMessageEntity.getScheduledTime(); // Get the scheduled time

                String displayedContent = ""; // This will hold the final content to display
                String processingOutcome = "Default"; // Track how the content was determined


                // --- Determine the expected state and type placeholder ---
                boolean isExpectedEncryptedType = "text".equals(messageType) || "image".equals(messageType);
                boolean isPlaintextScheduled = isExpectedEncryptedType && !TextUtils.isEmpty(scheduledTime); // It's text/image AND has scheduledTime
                boolean isAlwaysPlaintextType = "file".equals(messageType) || "system_key_change".equals(messageType); // These types are never encrypted by our app

                // --- Handle Empty Content ---
                if (TextUtils.isEmpty(storedMessageContentBase64)) {
                    // Content is empty in Room - show a type-specific placeholder
                    if ("image".equals(messageType)) displayedContent = "[Image Data Missing]";
                    else if ("file".equals(messageType)) displayedContent = "[File Data Missing]";
                    else if ("system_key_change".equals(messageType)) displayedContent = "[System Message Data Missing]";
                    else displayedContent = "[Empty Message]"; // Default for text or unknown type
                    processingOutcome = "Content Empty";
                    Log.w(TAG, "Msg " + firebaseMessageId + " (Type: " + messageType + "): Stored content empty. Outcome: " + processingOutcome);

                }
                // --- Handle Plaintext Scheduled Messages (Text or Image) ---
                else if (isPlaintextScheduled || isAlwaysPlaintextType) {
                    // This message is either a text/image from the scheduled worker (has scheduledTime)
                    // OR it is a type that is never encrypted (file, system_key_change).
                    // In both cases, the stored content is the plaintext/raw data.
                    displayedContent = storedMessageContentBase64; // Display the stored content directly
                    if (TextUtils.isEmpty(displayedContent)) {
                        // Fallback to placeholder if the *intended* plaintext content is empty
                        if ("image".equals(messageType)) displayedContent = "[Image Data Missing]";
                        else if ("file".equals(messageType)) displayedContent = "[File Data Missing]";
                        else if ("system_key_change".equals(messageType)) displayedContent = "[System Message Data Missing]";
                        else displayedContent = "[Empty Message]";
                        processingOutcome = "Plaintext/Always Plaintext Type (Content Empty Fallback)";
                    } else {
                        processingOutcome = isPlaintextScheduled ? "Plaintext Scheduled" : "Always Plaintext Type";
                    }
                    Log.d(TAG, "Msg " + firebaseMessageId + " (Type: " + messageType + ", Scheduled: " + scheduledTime + "): Displaying as plaintext/raw. Outcome: " + processingOutcome);
                }
                // --- Handle Potentially Encrypted Text/Image Messages (Not Scheduled) ---
                else if (isExpectedEncryptedType) { // This condition implies it's text/image, has content, and is NOT scheduled

                    // Check if keys are available to decrypt
                    if (isPrivateKeyAvailable && isConversationKeyAvailable) {
                        // Keys available, attempt decryption
                        processingOutcome = "Attempting Decryption";
                        Log.d(TAG, "Msg " + firebaseMessageId + " (Type: " + messageType + "): Not scheduled. Keys available. Attempting decryption.");

                        byte[] encryptedBytesWithIV = null;
                        try {
                            // Decode the Base64 string from Room FIRST
                            encryptedBytesWithIV = CryptoUtils.base64ToBytes(storedMessageContentBase64);

                            if (encryptedBytesWithIV == null || encryptedBytesWithIV.length == 0) {
                                Log.w(TAG, "Msg " + firebaseMessageId + ": Decoded encrypted bytes null/empty after Base64 decode.");
                                processingOutcome = "Decoded Empty Bytes";
                                displayedContent = "text".equals(messageType) ? "[Invalid Encrypted Data]" : "[Invalid Encrypted Image Data]";
                            } else {
                                // Decrypt using the single conversation key
                                try {
                                    String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytesWithIV, conversationAESKey);
                                    displayedContent = decryptedContent; // Use decrypted content
                                    processingOutcome = "Decryption Success";
                                    Log.d(TAG, "Msg " + firebaseMessageId + ": Decrypted SUCCESSFULLY.");
                                } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidAlgorithmParameterException e) {
                                    // Crypto decryption failed for THIS key!
                                    Log.d(TAG, "Msg " + firebaseMessageId + ": Decryption FAILED. " + e.getClass().getSimpleName());
                                    processingOutcome = "Decryption Failed: " + e.getClass().getSimpleName();
                                    displayedContent = "text".equals(messageType) ? "[Encrypted Message - Failed]" : "[Encrypted Image - Failed]"; // Type-specific failure placeholder
                                } catch (Exception e) {
                                    Log.w(TAG, "Msg " + firebaseMessageId + ": Unexpected error during decryption.", e);
                                    processingOutcome = "Decryption Failed: Unexpected Error";
                                    displayedContent = "text".equals(messageType) ? "[Encrypted Message - Failed]" : "[Encrypted Image - Failed]"; // Type-specific failure placeholder
                                }
                            }

                        } catch (IllegalArgumentException e) { // Base64 decoding error
                            Log.e(TAG, "Msg " + firebaseMessageId + ": Base64 decoding error for encrypted content.", e);
                            processingOutcome = "Base64 Decoding Error";
                            displayedContent = "[Invalid Encrypted Data]"; // Placeholder on Base64 error
                        } catch (Exception e) { // Any other unexpected decoding error
                            Log.e(TAG, "Msg " + firebaseMessageId + ": Unexpected error decoding Base64 for display.", e);
                            processingOutcome = "Decoding Error";
                            displayedContent = "[Invalid Encrypted Data]"; // Placeholder
                        }

                    } else {
                        // Keys are missing, cannot decrypt
                        processingOutcome = "Keys Unavailable";
                        Log.d(TAG, "Msg " + firebaseMessageId + " (Type: " + messageType + "): Not scheduled, but keys are unavailable (PrivKey: " + isPrivateKeyAvailable + ", ConvKey: " + isConversationKeyAvailable + "). Showing [Locked].");
                        displayedContent = "[Locked]"; // Placeholder indicating account needs unlocking or keys need loading
                    }

                }
                // --- Step 6: Fallback for unexpected cases ---
                else {
                    // Should theoretically not be reached if all types and conditions are covered.
                    // This might catch unexpected types or states. Display stored content as is.
                    Log.w(TAG, "Msg " + firebaseMessageId + " (Type: " + messageType + ", Scheduled: " + scheduledTime + ", Content Empty: " + TextUtils.isEmpty(storedMessageContentBase64) + ", Keys Available: " + (isPrivateKeyAvailable && isConversationKeyAvailable) + "): Reached unexpected state. Displaying stored content as is.");
                    displayedContent = storedMessageContentBase64;
                    processingOutcome = "Fallback State";
                    // Ensure displayedContent is not null/empty for safety, use generic placeholder if needed
                    if (TextUtils.isEmpty(displayedContent)) displayedContent = "[Unknown Message Type/State]";
                }


                // Create a *new* MessageEntity object with the processed content for the adapter
                MessageEntity displayedMessageEntity = new MessageEntity();
                // Copy all original fields from the stored entity
                displayedMessageEntity.setFirebaseMessageId(storedMessageEntity.getFirebaseMessageId());
                displayedMessageEntity.setOwnerUserId(storedMessageEntity.getOwnerUserId());
                displayedMessageEntity.setType(storedMessageEntity.getType());
                displayedMessageEntity.setFrom(storedMessageEntity.getFrom());
                displayedMessageEntity.setTo(storedMessageEntity.getTo());
                displayedMessageEntity.setSendTime(storedMessageEntity.getSendTime());
                displayedMessageEntity.setSeen(storedMessageEntity.isSeen());
                displayedMessageEntity.setSeenTime(storedMessageEntity.getSeenTime());
                displayedMessageEntity.setStatus(storedMessageEntity.getStatus());
                displayedMessageEntity.setTimestamp(storedMessageEntity.getTimestamp());
                displayedMessageEntity.setScheduledTime(storedMessageEntity.getScheduledTime()); // *** Copy the scheduled time field! ***
                // *** Set the processed content to display ***
                displayedMessageEntity.setMessage(displayedContent);

                // Log the final content being added to the adapter list (maybe truncated)
                Log.d(TAG, "Msg " + firebaseMessageId + ": Final displayed content determined. Adding to list. Outcome: " + processingOutcome + ". Displayed: '" + (displayedContent != null && displayedContent.length() > 50 ? displayedContent.substring(0, 50) + "..." : displayedContent) + "'");


                messagesArrayList.add(displayedMessageEntity); // Add the processed message to the list for the adapter

            } // End of for loop
        } // End of if (currentMessages != null && !currentMessages.isEmpty()) check


        // Sort the list (if necessary, should already be sorted by DAO query which orders by timestamp ASC)
        // Ensure sorting is consistent with the Room query's order (usually by timestamp)
        if (messagesArrayList.size() > 1) {
            Collections.sort(messagesArrayList, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
        }


        // Update the adapter with the new list of processed messages
        messageAdapter.notifyDataSetChanged();
        Log.d(TAG, "Submitted " + messagesArrayList.size() + " processed messages to adapter in forceRefreshDisplay.");

        // Auto-scroll to bottom (Keep existing logic)
        if (!messagesArrayList.isEmpty() && messagesList != null && messagesList.getLayoutManager() != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) messagesList.getLayoutManager();
            if (layoutManager.getItemCount() > 0) {
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                // Auto-scroll if near the bottom OR if it's likely the initial small load
                // Increased tolerance for "near bottom" check
                int scrollTolerance = 10; // Scroll if within this many items from the end
                boolean isAtBottom = lastVisiblePosition != RecyclerView.NO_POSITION && lastVisiblePosition == totalItemCount - 1;
                boolean isNearBottom = lastVisiblePosition != RecyclerView.NO_POSITION && lastVisiblePosition >= totalItemCount - 1 - scrollTolerance;
                boolean isInitialLoad = messagesArrayList.size() > 0 && totalItemCount == messagesArrayList.size() && messagesArrayList.size() < 50; // Heuristic for initial load size

                if (isAtBottom || isNearBottom || isInitialLoad) { // Keep modified condition
                    messagesList.scrollToPosition(totalItemCount - 1);
                    Log.d(TAG, "Auto-scrolling to bottom. Total items: " + totalItemCount + ", Last visible: " + lastVisiblePosition + ", Is near bottom: " + isNearBottom + ", Is at bottom: " + isAtBottom + ", Is Initial Load (heuristic): " + isInitialLoad);
                } else {
                    Log.d(TAG, "Skipping auto-scroll. User is not near bottom. Total items: " + totalItemCount + ", Last visible: " + lastVisiblePosition);
                }
            } else if (messagesList != null && messagesList.getLayoutManager() != null && ((LinearLayoutManager) messagesList.getLayoutManager()).getItemCount() == 0) {
                Log.d(TAG, "Message list is empty after forceRefreshDisplay, no scrolling.");
            }
            // Else case (messagesList or LayoutManager null) handled at the beginning of the method
        }


        // Mark visible messages as seen (Keep existing logic)
        // This should happen AFTER the adapter is updated and potentially scrolled
        // Adding a slight delay can sometimes help ensure layout finishes before checking visible items
        messagesList.postDelayed(this::markVisibleMessagesAsSeen, 100);


        // --- Check and Update UI enabled state based on the *current* KeyManager state ---
        // UI should be enabled for sending if the user's private key is available AND the conversation key is loaded for this chat.
        // This check is done dynamically here regardless of the initial state.
        boolean isSecureChatCurrentlyAvailable = isPrivateKeyAvailable && isConversationKeyAvailable; // *** MODIFIED CHECK ***

        if (isSecureChatCurrentlyAvailable && (messageInputText != null && !messageInputText.isEnabled())) {
            // Keys are now available, and UI was previously disabled. Enable UI.
            if (messageInputText != null) { messageInputText.setEnabled(true); messageInputText.setHint("Enter Message..."); }
            if (sendMessageButton != null) sendMessageButton.setEnabled(true);
            if (send_imgmsg_btn != null) send_imgmsg_btn.setEnabled(true);
            Log.d(TAG, "forceRefreshDisplay enabled UI as keys are now available.");
        } else if (!isSecureChatCurrentlyAvailable && (messageInputText != null && messageInputText.isEnabled())) {
            // Keys are now unavailable, and UI was previously enabled. Disable UI.
            if (messageInputText != null) { messageInputText.setEnabled(false); messageInputText.setHint("Secure chat unavailable"); }
            if (sendMessageButton != null) sendMessageButton.setEnabled(false);
            if (send_imgmsg_btn != null) send_imgmsg_btn.setEnabled(false);
            Log.d(TAG, "forceRefreshDisplay disabled UI as keys are now unavailable.");
            // Show a toast indicating why UI is disabled
            if (!isPrivateKeyAvailable) {
                Toast.makeText(this, "Account not unlocked. Secure chat disabled.", Toast.LENGTH_SHORT).show();
            } else if (!isConversationKeyAvailable) {
                Toast.makeText(this, "Secure chat key missing. Secure chat disabled.", Toast.LENGTH_SHORT).show();
            } else {
                // Generic fallback toast
                Toast.makeText(this, "Secure chat unavailable.", Toast.LENGTH_SHORT).show();
            }

        } else {
            // UI state is already consistent with key state, no change needed.
            Log.d(TAG, "forceRefreshDisplay: UI state already consistent with key state (Secure Chat Available: " + isSecureChatCurrentlyAvailable + ").");
        }
        Log.d(TAG, "✅ forceRefreshDisplay finished.");
    }


    private void showImageSourceDialog() {
        Log.d(TAG, "Showing image source dialog.");
        new AlertDialog.Builder(this)
                .setTitle("Choose Image Source")
                .setItems(new CharSequence[]{"Gallery", "Camera"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Gallery option clicked
                            openGallery(); // We'll create this method
                            break;
                        case 1: // Camera option clicked
                            openCamera(); // We'll create this method
                            break;
                    }
                })
                .show();
    }



    // --- NEW Helper Method: Initialize Image Pickers (Launchers) ---
    private void initializeImagePickers() {
        // Launcher for picking an image from the gallery
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), // Contract for a generic activity result
                result -> { // Callback when gallery selection activity returns
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // User selected an image successfully
                        Uri selectedImageUri = result.getData().getData(); // Get the URI of the selected image
                        Log.d(TAG, "Image selected from gallery. URI: " + selectedImageUri);
                        processSelectedImageForSending(selectedImageUri); // Process the selected image
                    } else {
                        // User cancelled or selection failed
                        Log.d(TAG, "Gallery image selection cancelled or failed.");
                        Toast.makeText(this, "Image selection cancelled.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Launcher for taking a picture with the camera
        // This contract requires a URI where the camera should save the picture.
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), // Contract specifically for taking a picture
                isSuccess -> { // Callback when camera activity returns (boolean indicates success)
                    if (isSuccess) {
                        // Picture was taken successfully and saved to the URI we provided
                        Log.d(TAG, "Picture taken with camera. URI: " + imageToSendUri); // imageToSendUri was set before launching
                        processSelectedImageForSending(imageToSendUri); // Process the captured image from the URI
                    } else {
                        // User cancelled or picture taking failed
                        Log.w(TAG, "Camera picture taking cancelled or failed.");
                        Toast.makeText(this, "Camera operation cancelled or failed.", Toast.LENGTH_SHORT).show();
                        // Clean up the temporary URI we created if it wasn't used
                        if (imageToSendUri != null) {
                            try {
                                getContentResolver().delete(imageToSendUri, null, null); // Delete temporary file
                            } catch (Exception e) { Log.e(TAG, "Failed to delete temporary camera file after cancel/failure.", e); }
                        }
                        imageToSendUri = null; // Reset URI
                    }
                });
    }



    // This method takes the image URI, processes it (resize, encode), and prepares it for sending.
    private void processSelectedImageForSending(@Nullable Uri uri) {
        if (uri == null) {
            Log.w(TAG, "processSelectedImageForSending called with null URI.");
            imageToSendUri = null; // Ensure temporary URI is cleared if it was from camera
            imageToSendBase64 = "";
            // TODO: Update UI if any preview was shown before processing failed
            return;
        }

        Log.d(TAG, "Processing selected/captured image for sending. URI: " + uri);

        try {
            // Get Bitmap from URI
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            Log.d(TAG, "Original image dimensions: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

            // Resize Bitmap
            Bitmap resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SEND_SIZE, MAX_IMAGE_SEND_SIZE); // Use MAX_IMAGE_SEND_SIZE
            Log.d(TAG, "Resized image dimensions for sending: " + resizedBitmap.getWidth() + "x" + resizedBitmap.getHeight());

            // Encode to Base64
            String tempBase64 = encodeToBase64(resizedBitmap); // Encode the resized image to Base64
            Log.d(TAG, "Encoded image to Base64 for sending. Length: " + (tempBase64 != null ? tempBase64.length() : 0) + " bytes.");

            if (TextUtils.isEmpty(tempBase64)) {
                Log.e(TAG, "Encoded Base64 image content is empty!");
                Toast.makeText(this, "Failed to encode image data.", Toast.LENGTH_SHORT).show();
                // Clean up temp URI if needed
                if (uri != null && uri.toString().contains("content://media")) { // Check if it's likely a MediaStore URI from camera
                    try { getContentResolver().delete(uri, null, null); } catch (Exception e) { Log.e(TAG, "Failed to delete temporary camera file after encode failure.", e); }
                }
                imageToSendUri = null; // Clear temp URI variable
                imageToSendBase64 = ""; // Ensure our member variable is empty
                return; // Stop processing here
            }

            // *** NEW STEP: IMMEDIATELY SEND THE IMAGE AFTER SUCCESSFUL PROCESSING ***
            Log.d(TAG, "Image processed successfully. Initiating send.");
            sendImageMessage(tempBase64); // Call your method to send the image message
            // *** END NEW STEP ***

            // After sending is initiated (asynchronous), clear the temporary image data
            // Note: The UI might update later based on the message status (pending -> sent/failed)
            imageToSendUri = null; // Clear the URI
            imageToSendBase64 = ""; // Clear the Base64 string

            // TODO: Update UI if any preview was shown before sending
            // If sending immediately, you might not show a preview first.
            // The UI update should happen when the message status changes (handled by your message adapter).

            // Optional toast confirming processing is done and sending is initiated
            Toast.makeText(this, "Image processed, sending...", Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            Log.e(TAG, "Image processing failed for sending (IOException).", e);
            Toast.makeText(this, "Failed to process image.", Toast.LENGTH_SHORT).show();
            imageToSendUri = null;
            imageToSendBase64 = "";
            // TODO: Update UI
        } catch (Exception e) { // Catch any other unexpected errors during processing
            Log.e(TAG, "Unexpected error during image processing for sending.", e);
            Toast.makeText(this, "An error occurred processing image.", Toast.LENGTH_SHORT).show();
            imageToSendUri = null;
            imageToSendBase64 = "";
            // TODO: Update UI
        }
    }
// --- END NEW Helper Method ---

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null) return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // Log.d(TAG, "Original bitmap dimensions for sending: " + width + "x" + height); // Optional log

        if (width <= maxWidth && height <= maxHeight) {
            // Log.d(TAG, "No resizing needed for sending."); // Optional log
            return bitmap;
        }

        float scale = Math.min(((float) maxWidth / width), ((float) maxHeight / height));

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        // Log.d(TAG, "Resizing for sending to dimensions: " + newWidth + "x" + newHeight + " with scale: " + scale); // Optional log

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private String encodeToBase64(Bitmap image) {
        if (image == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, IMAGE_SEND_COMPRESSION_QUALITY, baos); // Use IMAGE_SEND_COMPRESSION_QUALITY
        byte[] byteArray = baos.toByteArray();
        // Use android.util.Base64 for image data encoding/decoding
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
// --- End Image Processing Helpers ---


    // --- Helper Method to Launch Camera Intent (Called AFTER permission check) ---
// This method contains the actual logic to prepare the URI and launch the camera app.
    private void launchCameraIntent() {
        Log.d(TAG, "Attempting to launch camera intent.");
        try {
            // Create a new content:// URI for the picture in MediaStore.
            // This is the preferred modern way (API 29+).
            // On older APIs or for specific storage needs, you might need WRITE_EXTERNAL_STORAGE
            // permission and a FileProvider setup in Manifest and res/xml/file_paths.xml.
            // Using getContentResolver().insert() here relies on the system's MediaStore handling.
            imageToSendUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());

            if (imageToSendUri != null) {
                Log.d(TAG, "Prepared temporary URI for camera: " + imageToSendUri);
                // Launch camera, output will be saved to this URI
                takePictureLauncher.launch(imageToSendUri);
            } else {
                Log.e(TAG, "Failed to create temporary URI for camera picture.");
                Toast.makeText(this, "Error preparing camera.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating URI for camera picture or launching intent.", e);
            // This catch block handles potential exceptions during URI creation or intent launch
            Toast.makeText(this, "Error accessing camera.", Toast.LENGTH_SHORT).show();
            imageToSendUri = null; // Ensure URI is null on failure
        }
    }
// --- END NEW Helper Method ---


    // --- NEW Helper Method: Contains the logic to open the Gallery intent ---
    private void openGallery() {
        Log.d(TAG, "Attempting to open gallery.");
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent); // Launch gallery picker
        } catch (Exception e) {
            Log.e(TAG, "Error launching gallery intent.", e);
            Toast.makeText(this, "Error accessing gallery.", Toast.LENGTH_SHORT).show();
        }
    }
// --- END NEW Helper Method ---


    // --- NEW Helper Method: Contains the logic to open the Camera intent ---
// This method performs the permission check and then calls launchCameraIntent if granted.
    private void openCamera() {
        Log.d(TAG, "Attempting to open camera.");
        // Check if the CAMERA permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted. Proceeding to launch camera intent.");
            // Permission is already granted, proceed with launching the camera intent
            launchCameraIntent();
        } else {
            Log.d(TAG, "Camera permission not granted. Requesting permission.");
            // Permission is not granted, request it using the launcher
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
// --- END NEW Helper Method ---






} // End of ChatPageActivity class
