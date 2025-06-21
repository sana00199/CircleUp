
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.sana.circleup.drawingboard_chatgroup.YOUR_DRAWING_ACTIVITY_CLASS;
import com.sana.circleup.encryptionfiles.CryptoUtils;
import com.sana.circleup.encryptionfiles.YourKeyManager;
import com.sana.circleup.one_signal_notification.OneSignalApiService;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.ConversationKeyDao;
import com.sana.circleup.room_db_implement.ConversationKeyEntity;
import com.sana.circleup.room_db_implement.DeletedMessageIdDao;
import com.sana.circleup.room_db_implement.DeletedMessageIdEntity;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ChatPageActivity extends AppCompatActivity implements
        MessageAdapter.OnMessageLongClickListener,
        MessageAdapter.OnMessageClickListener {

    private static final String TAG = "ChatPageActivity";


    // Implement the new interface method for single taps

    // Keep your existing onMessageLongClick method implementation





    // --- Existing members ---
    private String messageReceiverID, messageReceiverName, messageReceiverImage;
    private TextView userName, userLastSeen;
    private CircleImageView userProfileImage; // Use full package name here
    private ImageButton sendMessageButton, send_imgmsg_btn;
    // --- Existing members ---
// ... other members ...
// private ImageButton sendMessageButton, send_imgmsg_btn; // Old declaration
    private ImageButton  attachmentButton; // MODIFIED
    // ... rest of members ...
    private EditText messageInputText;
    private Toolbar chatToolbar;
    private FirebaseAuth mAuth;
    private boolean isInvisibleInkSelected = false; // Default is false (normal sending)


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


// ... existing members ...

    // --- New members for Image Preview ---
    private FrameLayout imagePreviewContainer;
    private ImageView imgPreview;
    private ImageButton btnCancelImage;
// --- End New members ---

// ... rest of members ...

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



            if (wasUiInitiallyDisabled) {
                messageInputText.setEnabled(false);
                sendMessageButton.setEnabled(false);
                attachmentButton.setEnabled(false); // ADDED
                messageInputText.setHint("Secure chat unavailable");
            }
            // --- Disable UI for sending messages ---
            messageInputText.setEnabled(false);
            sendMessageButton.setEnabled(false);
//            send_imgmsg_btn.setEnabled(false);
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
        // In onCreate:
        sendMessageButton.setOnClickListener(v -> attemptSendMessage()); // Call the new method
        // In onCreate:
//        send_imgmsg_btn.setOnClickListener(v -> {
//            // Ensure secure chat keys are available before picking image for encryption
//            boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
//            if (!isSecureChatAvailable) {
//                Toast.makeText(this, "Secure chat is not enabled to send images.", Toast.LENGTH_SHORT).show();
//                Log.w(TAG, "Image picker blocked: Secure chat keys unavailable.");
//                return; // Do not proceed if secure chat keys are off
//            }
//
//            // Clear any previously selected image before opening the picker for a new one
//            clearSelectedImage();
//
//            // Show the dialog to choose source (Gallery or Camera)
//            showImageSourceDialog();
//        });



        attachmentButton.setOnClickListener(v -> { // ADDED listener for attachment button
            // Secure Chat check *before* showing picker dialog
            boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
            if (!isSecureChatAvailable) {
                Toast.makeText(this, "Secure chat is not enabled to send images.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Attachment picker blocked: Secure chat keys unavailable.");
                return;
            }

            // Clear any previously selected image before opening the picker for a new one
            clearSelectedImage(); // ADDED: Important to clear previous selection

            // Show the dialog to choose source (Gallery or Camera)
            showAttachmentOptionsBottomSheet();// ADDED: New method for the dialog
        });



        // --- *** End Modify Button Click Listeners *** ---

        initializeImagePickers();

        // In onCreate or InitializeControllers:
        if (btnCancelImage != null) {
            btnCancelImage.setOnClickListener(v -> clearSelectedImage());
        }


        // Initialize the permission launcher
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), // Contract for requesting a single permission
                isGranted -> { // Callback when permission request is finished
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted. Proceeding to open camera.");
                        // Permission was granted, now actually launch the camera intent
                        launchCameraIntentForAttachment(); // Call the helper method to launch camera
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
//        send_imgmsg_btn = findViewById(R.id.send_imgmsg_btn);
        attachmentButton = findViewById(R.id.btn_attachment);
        messageInputText = findViewById(R.id.input_msg);
        messagesList = findViewById(R.id.privatemsges_list_of_users);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(linearLayoutManager);
        messagesList.setHasFixedSize(true);

        // --- Initialize new Image Preview UI elements ---
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        imgPreview = findViewById(R.id.imgPreview);
        btnCancelImage = findViewById(R.id.btnCancelImage);

        // Initially hide the image preview area
        imagePreviewContainer.setVisibility(View.GONE);
        btnCancelImage.setVisibility(View.GONE);


        messageAdapter = new MessageAdapter(messagesArrayList, (Context) this, messageReceiverImage, messageSenderID, (MessageAdapter.OnMessageClickListener) this); // *** MODIFIED: Added 'this' as the 5th argument ***
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
    // Helper method to clear chat messages from Room DB
    // Helper method to clear chat messages from Room DB
    private void clearChatLocally() {
        if (TextUtils.isEmpty(messageSenderID) || TextUtils.isEmpty(messageReceiverID) || messageDao == null || db == null) {
            Log.e(TAG, "Cannot clear chat locally: messageSenderID, messageReceiverID, messageDao, or db is null/empty.");
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error clearing chat.", Toast.LENGTH_SHORT).show());
            return;
        }

        databaseWriteExecutor.execute(() -> {
            try {
                // --- Get messages BEFORE deleting and record their IDs ---
                // Use the NEW synchronous method getMessagesForChatSync
                // Pass ownerUserId (messageSenderID) and partnerId (messageReceiverID)
                List<MessageEntity> messagesToClear = messageDao.getMessagesForChatSync(messageSenderID, messageReceiverID);

                if (messagesToClear != null && !messagesToClear.isEmpty()) {
                    DeletedMessageIdDao deletedMessageIdDao = db.deletedMessageIdDao(); // Get the new DAO
                    List<DeletedMessageIdEntity> deletedEntities = new ArrayList<>();
                    for (MessageEntity msg : messagesToClear) {
                        if (!TextUtils.isEmpty(msg.getFirebaseMessageId())) {
                            deletedEntities.add(new DeletedMessageIdEntity(messageSenderID, msg.getFirebaseMessageId()));
                        }
                    }
                    if (!deletedEntities.isEmpty()) {
                        deletedMessageIdDao.insertAllDeletedMessageIds(deletedEntities); // Insert all IDs in bulk
                        Log.d(TAG, "Recorded " + deletedEntities.size() + " message IDs as deleted for me for owner " + messageSenderID + " during clear chat.");
                    } else {
                        Log.d(TAG, "No valid Firebase IDs found in messages to clear for recording.");
                    }
                } else {
                    Log.d(TAG, "No messages found in Room to clear for chat with " + messageReceiverID + " owned by " + messageSenderID + ".");
                }
                // --- END Recording ---


                // --- Delete all messages from the main messages table ---
                // *** FIX: Change method name to deleteMessagesForChat ***
                // Pass ownerUserId (messageSenderID) and the two participant IDs (messageSenderID, messageReceiverID)
                int deletedRows = messageDao.deleteMessagesForChat(messageSenderID, messageSenderID, messageReceiverID);


                runOnUiThread(() -> {
                    if (deletedRows > 0) {
                        Log.d(TAG, "Cleared " + deletedRows + " messages from main Room table for owner " + messageSenderID + " in chat with " + messageReceiverID);
                        Toast.makeText(ChatPageActivity.this, "Chat cleared.", Toast.LENGTH_SHORT).show();
                        // LiveData observer will automatically update the RecyclerView UI (now empty)
                    } else {
                        Log.w(TAG, "Attempted to clear chat for owner " + messageSenderID + " with " + messageReceiverID + ", but no messages were found in main table to delete.");
                        Toast.makeText(ChatPageActivity.this, "No messages to clear.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error during clearChatLocally operation for chat with " + messageReceiverID, e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error clearing chat.", Toast.LENGTH_SHORT).show());
            }
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
    // Inside ChatPageActivity class

    // --- Modified Method to Send Text Message (Includes Encryption and Invisible Ink Flag) ---
    private void SendTextMessage(String messageText, boolean isInvisibleInk) { // *** MODIFIED SIGNATURE: Added boolean isInvisibleInk ***
        Log.d(TAG, "SendTextMessage called. Message length: " + messageText.length() + ", Invisible Ink: " + isInvisibleInk);

        if (TextUtils.isEmpty(messageText)) {
            Log.w(TAG, "SendTextMessage called with empty text. This should be caught by attemptSendMessage.");
            return; // Should ideally not happen if attemptSendMessage validates correctly
        }

        // --- *** CHECK if Secure Chat is Enabled (Keys are available) *** ---
        // Keep this check here as a final safeguard, even if attemptSendMessage checks.
        boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
        SecretKey conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId);

        if (!isSecureChatAvailable || conversationAESKey == null) {
            Log.w(TAG, "SendTextMessage blocked: Secure chat keys are not available.");
            // Toast or UI disabling is handled by attemptSendMessage/onResume.
            // We might want to update the local message status to failed here if it were already saved as pending.
            // But since we save to Room *after* getting Firebase push ID, we don't have the ID yet here.
            // The 'pending' message saved later will likely stay 'pending' until keys are available or user retries.
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Secure chat not available, cannot send message.", Toast.LENGTH_SHORT).show());
            return;
        }
        // --- *** END Check *** ---

        String sendTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        Object firebaseTimestamp = ServerValue.TIMESTAMP; // Firebase ServerValue for consistent time
        long localTimestamp = System.currentTimeMillis(); // Local timestamp for initial Room order

        // 1. Generate Firebase Push ID FIRST
        DatabaseReference messagesRef = rootRef.child("Messages").child(conversationId).push(); // Use conversationId here!
        String messagePushId = messagesRef.getKey();

        if (messagePushId == null) {
            Log.e(TAG, "Firebase push key generation failed. Cannot send text message.");
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error sending message: Failed to generate ID.", Toast.LENGTH_SHORT).show());
            return; // Exit the method if ID is null
        }
        Log.d(TAG, "Generated Firebase push ID for text message: " + messagePushId + " (Invisible Ink: " + isInvisibleInk + ")");


        // --- *** ENCRYPT the message content *** ---
        // Encrypt the *actual* text regardless of the display effect.
        byte[] encryptedBytes;
        String encryptedMessageContentBase64;
        try {
            encryptedBytes = CryptoUtils.encryptMessageWithAES(messageText, conversationAESKey); // Encrypt raw text
            encryptedMessageContentBase64 = CryptoUtils.bytesToBase64(encryptedBytes); // Convert encrypted bytes to Base64 string

            if (TextUtils.isEmpty(encryptedMessageContentBase64)) {
                Log.e(TAG, "Base64 encoding of encrypted text message failed.");
                throw new Exception("Base64 encoding failed.");
            }
            Log.d(TAG, "Text message encrypted and Base64 encoded. Encrypted Base64 length: " + encryptedMessageContentBase64.length());
        } catch (Exception e) { // Catch any encryption or encoding errors
            Log.e(TAG, "Failed to encrypt/encode text message", e);
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to encrypt message.", Toast.LENGTH_SHORT).show());
            // We don't have a MessageEntity in Room yet to mark as failed,
            // but the user saw a toast. They can retry later if implemented.
            return; // Do not send if encryption/encoding fails
        }
        // --- *** END ENCRYPTION *** ---


        // 2. Create MessageEntity for Room (store the *encrypted Base64 string* and use local timestamp)
        MessageEntity messageToSaveLocally = new MessageEntity();
        messageToSaveLocally.setFirebaseMessageId(messagePushId);
        messageToSaveLocally.setOwnerUserId(messageSenderID); // Owner is the current logged-in user (sender)
        messageToSaveLocally.setMessage(encryptedMessageContentBase64); // Store the encrypted Base64 string
        messageToSaveLocally.setType("text");
        messageToSaveLocally.setFrom(messageSenderID); // Sender is me
        messageToSaveLocally.setTo(messageReceiverID); // Receiver is the chat partner
        messageToSaveLocally.setSendTime(sendTime);
        messageToSaveLocally.setSeen(false); // Not seen yet
        messageToSaveLocally.setSeenTime("");
        messageToSaveLocally.setStatus("pending"); // Initially pending
        messageToSaveLocally.setTimestamp(localTimestamp); // Use local timestamp for initial order
        messageToSaveLocally.setScheduledTime(null); // Not a scheduled message

        // *** SET THE NEW displayEffect FIELD FOR ROOM ***
        // Set based on the flag passed to the method
        messageToSaveLocally.setDisplayEffect(isInvisibleInk ? "invisible_ink" : "none");
        // The 'isRevealed' field defaults to false in the constructor, which is fine for a new message
        // (even for the sender's copy, although it's not really used on the sender's side for display effect).
        // *** END SET FIELD ***


        // 3. Insert into Room DB first with "pending" status (runs on background thread)
        databaseWriteExecutor.execute(() -> {
            try {
                messageDao.insertMessage(messageToSaveLocally);
                Log.d(TAG, "Inserted pending text message into Room (encrypted Base64) for owner " + messageSenderID + ": " + messagePushId + ", Local Timestamp: " + localTimestamp + ", Effect: " + messageToSaveLocally.getDisplayEffect()); // Updated log
            } catch (Exception e) {
                Log.e(TAG, "Error inserting pending text message into Room: " + messagePushId, e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error saving message locally.", Toast.LENGTH_SHORT).show());
                // Continue attempting to send to Firebase even if local save failed
            }


            // 4. Send message to Firebase (runs on background thread after Room insert initiated)
            // Use the push ID generated earlier
            DatabaseReference messagePathRef = rootRef.child("Messages").child(conversationId).child(messagePushId);

            Map<String, Object> messageFirebaseBody = new HashMap<>();
            messageFirebaseBody.put("message", encryptedMessageContentBase64); // Send the encrypted Base64 string
            messageFirebaseBody.put("type", "text");
            messageFirebaseBody.put("from", messageSenderID);
            messageFirebaseBody.put("to", messageReceiverID);
            messageFirebaseBody.put("seen", false); // Always send as not seen by default
            messageFirebaseBody.put("seenTime", "");
            messageFirebaseBody.put("sendTime", sendTime); // Use local formatted time
            messageFirebaseBody.put("timestamp", firebaseTimestamp); // Use Firebase server timestamp


            // *** ADD THE NEW displayEffect FIELD FOR FIREBASE ***
            // Send based on the flag passed to the method
            messageFirebaseBody.put("displayEffect", isInvisibleInk ? "invisible_ink" : "none");
            // Do NOT send 'isRevealed' to Firebase, it's a local state.
            // *** END ADD FIELD ***

            messagePathRef.setValue(messageFirebaseBody).addOnCompleteListener(task -> {
                Log.d(TAG, "Firebase setValue for text message " + messagePushId + " completed. Success: " + task.isSuccessful());

                if (task.isSuccessful()) {
                    // --- Update Chat Summaries for BOTH users ---
                    // For chat summary list, the preview text is usually the actual message content (decrypted).
                    // Using the original *unencrypted* text for the summary preview is easiest and common.
                    // The 'displayEffect' is only for the full message view.
                    String summaryPreview = messageText; // Use original text for summary preview

                    // Optional: If you want the summary list to say "[Invisible Message]" for Invisible Ink
                    if (isInvisibleInk) {
                        summaryPreview = "[Invisible Text Message]";
                    }
                    // Or better: just use the actual text preview even for invisible ink in the list,
                    // as the list itself doesn't apply the effect. Let's revert to using original text.
                    summaryPreview = messageText; // Use original text for summary preview


                    updateChatSummaryForUser(
                            messageSenderID, // Owner 1 (Sender)
                            messageReceiverID, // Partner 1 (Receiver)
                            conversationId,
                            messagePushId,
                            summaryPreview, // Use original text or placeholder for summary preview
                            "text", // Message Type
                            firebaseTimestamp, // Use Firebase timestamp for summary order
                            messageSenderID // Sender of THIS message
                    );

                    // For the receiver's summary, use the same preview
                    updateChatSummaryForUser(
                            messageReceiverID, // Owner 2 (Receiver)
                            messageSenderID, // Partner 2 (Sender)
                            conversationId,
                            messagePushId,
                            summaryPreview, // Use original text or placeholder for summary preview
                            "text", // Message Type
                            firebaseTimestamp, // Use Firebase timestamp for summary order
                            messageSenderID // Sender of THIS message
                    );
                    // --- END Update Chat Summaries ---


                    // *** NEW: Send Push Notification ***
                    Log.d(TAG, "Firebase text message sent. Calling sendPushNotification.");
                    String senderDisplayNameForNotification = (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : "A User";
                    String notificationText = messageText; // Use the ORIGINAL, UNENCRYPTED message text for notification preview

                    // Optional: Modify notification text if you don't want the content in the notif
                    if (isInvisibleInk) {
                        // notificationText = "New Invisible Message from " + senderDisplayNameForNotification;
                        // Or show a generic placeholder in the notif
                        notificationText = "[New Message]"; // Or "[New Invisible Message]"
                    } else {
                        // For normal messages, show the decrypted preview in notif
                        // (Assuming sendPushNotification decrypts or gets decrypted text)
                        // Your current sendPushNotification takes the original text directly, which is fine here.
                        notificationText = messageText;
                    }
                    sendPushNotification(
                            messageReceiverID, // Recipient UID
                            "New Message from " + senderDisplayNameForNotification, // Title
                            notificationText, // Content
                            "text", // <<< Pass message type
                            conversationId, // <<< Pass conversationId
                            null, // <<< Pass null for sessionId (not applicable for text)
                            messagePushId // <<< Pass messageId (optional)
                    );
                    // *** END NEW ***


                    // --- Update status in Room DB for the SENDER's copy ---
                    // This happens on the background executor
                    databaseWriteExecutor.execute(() -> {
                        // Find the message again in Room using its Firebase ID and Owner ID
                        MessageEntity sentMessage = messageDao.getMessageByFirebaseId(messagePushId, messageSenderID);
                        if (sentMessage != null) {
                            // Update its status to 'sent'
                            sentMessage.setStatus("sent");
                            // Re-insert/update it in Room (using REPLACE strategy)
                            messageDao.insertMessage(sentMessage);
                            Log.d(TAG, "Updated status to 'sent' in Room for owner " + messageSenderID + " for message: " + messagePushId);
                        } else {
                            Log.e(TAG, "Sent message " + messagePushId + " not found in Room after Firebase success. Cannot update status.");
                            // This shouldn't happen if the initial insert succeeded, but good to log.
                        }
                    });

                    // Clear input text field on Main Thread after successful Firebase initiation
                    runOnUiThread(() -> messageInputText.setText(""));

                } else {
                    // Firebase write failed. Update status in Room to "failed".
                    Log.e(TAG, "Firebase setValue failed for text message " + messagePushId, task.getException());
                    databaseWriteExecutor.execute(() -> {
                        // Find the message again in Room
                        MessageEntity failedMessage = messageDao.getMessageByFirebaseId(messagePushId, messageSenderID);
                        if (failedMessage != null) {
                            // Update its status to 'failed'
                            failedMessage.setStatus("failed");
                            // Re-insert/update it in Room
                            messageDao.insertMessage(failedMessage);
                            Log.d(TAG, "Updated status to 'failed' in Room for owner " + messageSenderID + " for message: " + messagePushId);
                        } else {
                            Log.e(TAG, "Failed message " + messagePushId + " not found in Room after Firebase failure. Cannot update status.");
                        }
                    });
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show());
                }
            }); // End of Firebase setValue addOnCompleteListener
        }); // End of outer databaseWriteExecutor (Room insert and Firebase send)
    }
// --- End Modified Send Text Message ---

// Keep sendImageMessage() method as is for now. It won't handle 'invisible_ink' effect.
// Keep other methods as they are...
    // --- End Send Text Message ---


    // Inside ChatPageActivity class

    // --- Modified Method to Send Image Message (Includes Encryption and Invisible Ink Flag) ---
    private void sendImageMessage(String base64ImageContent, boolean isInvisibleInk) { // *** MODIFIED SIGNATURE: Added boolean isInvisibleInk ***
        Log.d(TAG, "sendImageMessage called. Base64 length: " + base64ImageContent.length() + ", Invisible Ink: " + isInvisibleInk);

        if (TextUtils.isEmpty(base64ImageContent)) {
            Toast.makeText(this, "Image data is empty!", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "sendImageMessage called with empty data.");
            return; // Should not happen if attemptSendMessage validates
        }

        // --- *** CHECK if Secure Chat is Enabled (Keys are available) *** ---
        // Keep this check here as a final safeguard.
        boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
        SecretKey conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId);

        if (!isSecureChatAvailable || conversationAESKey == null) {
            Log.w(TAG, "sendImageMessage blocked: Secure chat keys are not available.");
            // Toast or UI disabling is handled by attemptSendMessage/onResume.
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Secure chat not available, cannot send image.", Toast.LENGTH_SHORT).show());
            return;
        }
        // --- *** END Check *** ---

        String sendTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        Object firebaseTimestamp = ServerValue.TIMESTAMP;
        long localTimestamp = System.currentTimeMillis();

        // 1. Generate Firebase Push ID FIRST
        DatabaseReference messagesRef = rootRef.child("Messages").child(conversationId).push();
        String messagePushId = messagesRef.getKey();

        if (messagePushId == null) {
            Log.e(TAG, "Failed to generate Firebase push key for image message");
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error sending image message", Toast.LENGTH_SHORT).show());
            return;
        }
        Log.d(TAG, "Generated Firebase push ID for image: " + messagePushId + " (Invisible Ink: " + isInvisibleInk + ")");


        // --- *** ENCRYPT the image content (Base64 string) *** ---
        // Encrypt the *raw* Base64 image string.
        byte[] encryptedBytes;
        String encryptedImageContentBase64;
        try {
            // The raw Base64 image string is the *message content* to be encrypted
            encryptedBytes = CryptoUtils.encryptMessageWithAES(base64ImageContent, conversationAESKey); // Encrypt raw Base64 string

            // Immediately encode this byte[] into a Base64 String using CryptoUtils.bytesToBase64
            encryptedImageContentBase64 = CryptoUtils.bytesToBase64(encryptedBytes);

            if (TextUtils.isEmpty(encryptedImageContentBase64)) { // Defensive check
                Log.e(TAG, "Base64 encoding of encrypted image message failed.");
                throw new Exception("Base64 encoding failed.");
            }
            Log.d(TAG, "Image message encrypted and Base64 encoded. Encrypted Base64 length: " + encryptedImageContentBase64.length());
        } catch (Exception e) { // Catch any encryption or encoding errors
            Log.e(TAG, "Failed to encrypt/encode image message", e);
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to encrypt image message.", Toast.LENGTH_SHORT).show());
            return; // Do not send if encryption/encoding fails
        }
        // --- *** END ENCRYPTION *** ---


        // 2. Create MessageEntity for Room (store the *encrypted Base64 string* and use local timestamp)
        MessageEntity messageToSaveLocally = new MessageEntity();
        messageToSaveLocally.setFirebaseMessageId(messagePushId);
        messageToSaveLocally.setOwnerUserId(messageSenderID); // Owner is the current logged-in user (sender)
        messageToSaveLocally.setMessage(encryptedImageContentBase64); // Store the encrypted Base64 string
        messageToSaveLocally.setType("image"); // Type is image
        messageToSaveLocally.setFrom(messageSenderID); // Sender is me
        messageToSaveLocally.setTo(messageReceiverID); // Receiver is chat partner
        messageToSaveLocally.setSendTime(sendTime);
        messageToSaveLocally.setSeen(false); // Not seen yet
        messageToSaveLocally.setSeenTime("");
        messageToSaveLocally.setStatus("pending"); // Initially pending
        messageToSaveLocally.setTimestamp(localTimestamp);
        messageToSaveLocally.setScheduledTime(null); // Not scheduled

        // *** SET THE NEW displayEffect FIELD FOR ROOM ***
        // Set based on the flag passed to the method
        messageToSaveLocally.setDisplayEffect(isInvisibleInk ? "invisible_ink" : "none");
        // The 'isRevealed' field defaults to false in the constructor, which is fine for a new message
        // *** END SET FIELD ***


        // 3. Insert into Room DB first with "pending" status (runs on background thread)
        databaseWriteExecutor.execute(() -> {
            try {
                messageDao.insertMessage(messageToSaveLocally);
                Log.d(TAG, "Inserted pending image message into Room (encrypted Base64) for owner " + messageSenderID + ": " + messagePushId + ", Local Timestamp: " + localTimestamp + ", Effect: " + messageToSaveLocally.getDisplayEffect()); // Updated log
            } catch (Exception e) {
                Log.e(TAG, "Error inserting pending image message into Room: " + messagePushId, e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error saving image locally.", Toast.LENGTH_SHORT).show());
                // Continue attempting to send to Firebase
            }

            // 4. Send message to Firebase (runs on background thread after Room insert initiated)
            DatabaseReference messagePathRef = rootRef.child("Messages").child(conversationId).child(messagePushId);

            Map<String, Object> messageFirebaseBody = new HashMap<>();
            messageFirebaseBody.put("message", encryptedImageContentBase64); // Send the encrypted Base64 string
            messageFirebaseBody.put("type", "image");
            messageFirebaseBody.put("from", messageSenderID);
            messageFirebaseBody.put("to", messageReceiverID);
            messageFirebaseBody.put("seen", false);
            messageFirebaseBody.put("seenTime", "");
            messageFirebaseBody.put("sendTime", sendTime);
            messageFirebaseBody.put("timestamp", firebaseTimestamp);

            // *** ADD THE NEW displayEffect FIELD FOR FIREBASE ***
            // Send based on the flag passed to the method
            messageFirebaseBody.put("displayEffect", isInvisibleInk ? "invisible_ink" : "none");
            // Do NOT send 'isRevealed' to Firebase, it's a local state.
            // *** END ADD FIELD ***


            messagePathRef.setValue(messageFirebaseBody).addOnCompleteListener(task -> {
                Log.d(TAG, "Firebase setValue for image message " + messagePushId + " completed. Success: " + task.isSuccessful());

                if (task.isSuccessful()) {
                    // --- Update Chat Summaries for BOTH users ---
                    // For image summary, use "[Image]" placeholder. This is not affected by Invisible Ink status in the list.
                    String summaryPreview ;
                    if (isInvisibleInk) {
                        // Use a specific placeholder for Invisible Ink image messages in the summary
                        summaryPreview = "[Invisible Image Message]"; // *** CHANGED ***
                        Log.d(TAG, "Setting summary preview to '[Invisible Image Message]' for Firebase.");
                    } else {
                        // For normal image messages, use the standard placeholder
                        summaryPreview = "[Image]"; // *** CHANGED/CONSISTENT ***
                        Log.d(TAG, "Setting summary preview to '[Image]' for Firebase.");
                    }
                    // *** END MODIFY ***


                    updateChatSummaryForUser(
                            messageSenderID,
                            messageReceiverID,
                            conversationId,
                            messagePushId,
                            summaryPreview, // Placeholder preview for sender's summary list
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
                            summaryPreview, // Placeholder preview for receiver's summary list
                            "image",
                            firebaseTimestamp,
                            messageSenderID
                    );
                    // --- END Update Chat Summaries ---


                    // *** NEW: Send Push Notification ***
                    Log.d(TAG, "Firebase image message sent. Calling sendPushNotification.");
                    String senderDisplayNameForNotification = (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : "A User";
                    String notificationText = "[Image]"; // Placeholder for image content in notification

                    // Optional: Modify notification text if it was Invisible Ink image
                    if (isInvisibleInk) {
                        notificationText = "[New Image]"; // Or "[New Invisible Ink Image]"
                        Log.d(TAG, "Adjusting notification text for Invisible Ink image message notification.");
                    }

                    sendPushNotification(
                            messageReceiverID, // Recipient UID
                            "New Image from " + senderDisplayNameForNotification, // Title
                            notificationText, // Content
                            "image", // <<< Pass message type
                            conversationId, // <<< Pass conversationId
                            null, // <<< Pass null for sessionId (not applicable for image)
                            messagePushId // <<< Pass messageId (optional)
                    );
                    // *** END NEW ***


                    // --- Update status in Room DB for the SENDER's copy ---
                    databaseWriteExecutor.execute(() -> {
                        // Find message in Room and update status
                        MessageEntity sentMessage = messageDao.getMessageByFirebaseId(messagePushId, messageSenderID);
                        if (sentMessage != null) {
                            sentMessage.setStatus("sent");
                            messageDao.insertMessage(sentMessage); // Use REPLACE strategy
                            Log.d(TAG, "Updated status to 'sent' in Room for owner " + messageSenderID + " for image message: " + messagePushId);
                        } else {
                            Log.e(TAG, "Sent image message " + messagePushId + " not found in Room after Firebase success. Cannot update status.");
                        }
                    });
                    // No EditText to clear for images, only image preview cleared by attemptSendMessage
                } else {
                    // Firebase write failed. Update status in Room to "failed".
                    Log.e(TAG, "Firebase setValue failed for image message " + messagePushId, task.getException());
                    databaseWriteExecutor.execute(() -> {
                        // Find message in Room and update status
                        MessageEntity failedMessage = messageDao.getMessageByFirebaseId(messagePushId, messageSenderID);
                        if (failedMessage != null) {
                            failedMessage.setStatus("failed");
                            messageDao.insertMessage(failedMessage); // Use REPLACE strategy
                            Log.d(TAG, "Updated status to 'failed' in Room for owner " + messageSenderID + " for image message: " + messagePushId);
                        } else {
                            Log.e(TAG, "Failed image message " + messagePushId + " not found in Room after Firebase failure. Cannot update status.");
                        }
                    });
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to send image", Toast.LENGTH_SHORT).show());
                }
            }); // End of Firebase setValue addOnCompleteListener
        }); // End of outer databaseWriteExecutor
    }
// --- End Modified Send Image Message ---

// Keep other methods as they are...

    // --- Modify attachMessageListener ---
    // This method syncs messages FROM Firebase TO Room.
    // MessageEntity.message will store the Base64 string received from Firebase.
    // --- Modified attachMessageListener ---
    // This method syncs messages FROM Firebase TO Room.
    // MessageEntity.message will store the Base64 string received from Firebase.
    // Inside ChatPageActivity.java

    // Inside ChatPageActivity.java class body { ... }


    // --- Modified attachMessageListener ---
    // This method syncs messages FROM Firebase TO Room.
    // MessageEntity.message will store the Base64 string received from Firebase or plaintext for system messages.
    // It also handles processing incoming messages for notifications and potentially loading keys.
    private void attachMessageListener() {
        // Ensure conversationId, rootRef, and db are initialized before attaching the listener
        if (TextUtils.isEmpty(conversationId) || rootRef == null || db == null) {
            Log.w(TAG, "Cannot attach message listener: ConversationId, rootRef, or db is null. Conversation ID: " + conversationId);
            return;
        }

        // Check if the listener is already attached to prevent attaching duplicates
        if (messageListener == null) {
            // Get the Firebase Database reference for this specific conversation's messages node
            DatabaseReference conversationRef = rootRef.child("Messages").child(conversationId);
            Log.d(TAG, "Attaching Firebase listener for chat path: Messages/" + conversationId);


            // Create the ChildEventListener implementation
            messageListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Log.d(TAG, "onChildAdded triggered for " + snapshot.getKey() + " in conv " + conversationId);
                    // Get the message data from the snapshot as a HashMap to handle potential casting and missing fields safely
                    Map<String, Object> messageDataMap = (Map<String, Object>) snapshot.getValue(); // Use Map<String, Object> for safety
                    String firebaseMessageId = snapshot.getKey(); // This is the unique Firebase push key

                    if (messageDataMap != null && firebaseMessageId != null) {
                        // Run Room DB operations and potentially blocking tasks like decryption/notification sending on a background thread
                        databaseWriteExecutor.execute(() -> {
                            try { // Keep this try-catch block for the whole executor task running on the background thread

                                // --- NEW: Check if this message is marked as deleted for the current user locally ---
                                // Ensure db and messageSenderID are not null before accessing DAO
                                if (db == null || messageSenderID == null) {
                                    Log.e(TAG, "onChildAdded (Executor): ChatDatabase or messageSenderID instance is null. Cannot check for locally deleted messages.");
                                    return; // Skip processing this message if prerequisites are missing
                                }
                                // Get the DAO for checking locally deleted messages
                                DeletedMessageIdDao deletedMessageIdDao = db.deletedMessageIdDao();

                                // Use the check method from the new DAO
                                boolean isLocallyDeleted = deletedMessageIdDao.isMessageDeletedForUser(messageSenderID, firebaseMessageId);

                                if (isLocallyDeleted) {
                                    Log.d(TAG, "onChildAdded (Executor): Message " + firebaseMessageId + " is marked as locally deleted for owner " + messageSenderID + ". Skipping insertion into main table.");
                                    return; // IMPORTANT: Skip inserting this message into the main messages table in Room
                                }
                                // --- END NEW: Check for locally deleted messages ---


                                // --- Existing code below this point runs ONLY IF the message is NOT locally deleted ---

                                // Create a new MessageEntity object based on the Firebase snapshot data
                                MessageEntity roomMessage = new MessageEntity();
                                // Default values for displayEffect ("none") and isRevealed (false) are set in the MessageEntity constructor.

                                // Populate fields from Firebase snapshot
                                roomMessage.setFirebaseMessageId(firebaseMessageId);
                                roomMessage.setOwnerUserId(messageSenderID); // The owner is the current logged-in user (whose DB this message is for)

                                // Safely get values from the Firebase map, handling potential nulls or incorrect types
                                roomMessage.setMessage((String) messageDataMap.get("message")); // Store the Base64 String, plaintext, or placeholder from Firebase
                                roomMessage.setType((String) messageDataMap.get("type"));
                                roomMessage.setFrom((String) messageDataMap.get("from"));
                                roomMessage.setTo((String) messageDataMap.get("to"));
                                roomMessage.setSendTime((String) messageDataMap.get("sendTime"));

                                // Get seen status (boolean) and handle potential null
                                Object seenObj = messageDataMap.get("seen");
                                boolean seenStatus = (seenObj instanceof Boolean) ? (Boolean) seenObj : false; // Default to false if null or not boolean
                                roomMessage.setSeen(seenStatus);
                                roomMessage.setSeenTime((String) messageDataMap.get("seenTime"));

                                // Get status (string)
                                String statusStr = (String) messageDataMap.get("status");
                                // Determine initial status if not set in Firebase (e.g., for older messages or certain types)
                                roomMessage.setStatus(!TextUtils.isEmpty(statusStr) ? statusStr : (roomMessage.getFrom().equals(messageSenderID) ? "sent" : "received"));

                                // Get scheduledTime (string)
                                roomMessage.setScheduledTime((String) messageDataMap.get("scheduledTime")); // Get scheduledTime from Firebase

                                // Get timestamp (long) and handle potential null or incorrect type (Firebase might send double)
                                Long timestampLong = null; Object timestampObj = messageDataMap.get("timestamp");
                                if (timestampObj instanceof Long) { timestampLong = (Long) timestampObj; }
                                else if (timestampObj instanceof Double) { timestampLong = ((Double) timestampObj).longValue(); }
                                // Use the Firebase timestamp if available and valid, otherwise use current local time
                                roomMessage.setTimestamp(timestampLong != null && timestampLong > 0 ? timestampLong : System.currentTimeMillis());


                                // *** EXISTING: Read displayEffect from Firebase and set in RoomMessage ***
                                String firebaseDisplayEffect = (String) messageDataMap.get("displayEffect");
                                // Default to "none" if the field is missing or empty in Firebase
                                roomMessage.setDisplayEffect(!TextUtils.isEmpty(firebaseDisplayEffect) ? firebaseDisplayEffect : "none");
                                // *** END EXISTING ***


                                // *** NEW: Read conversationId, drawingSessionId, and name from Firebase and set in MessageEntity ***
                                // Do this for ANY message type, as these fields might be present in the Firebase payload.
                                String firebaseConversationId = (String) messageDataMap.get("conversationId");
                                String firebaseDrawingSessionId = (String) messageDataMap.get("drawingSessionId"); // This Firebase field stores the session ID for both group and 1:1 links
                                String senderDisplayNameFromFirebase = (String) messageDataMap.get("name"); // This Firebase field stores the sender's display name

                                roomMessage.setConversationId(firebaseConversationId); // Set the parsed conversationId
                                roomMessage.setDrawingSessionId(firebaseDrawingSessionId); // Set the parsed drawingSessionId (stores session ID)
                                roomMessage.setName(senderDisplayNameFromFirebase); // Set the parsed sender name
                                // --- END NEW ---


                                // *** NEW: Initialize isRevealed for the RECIPIENT's copy of INCOMING INVISIBLE INK messages ***
                                // If this message is incoming (to == messageSenderID) AND has the "invisible_ink" effect,
                                // explicitly set isRevealed to false initially in Room.
                                // For all other cases (normal messages, outgoing messages, system messages, etc.),
                                // the default 'isRevealed = false' from the constructor is acceptable.
                                if (roomMessage.getDisplayEffect().equals("invisible_ink") && roomMessage.getTo().equals(messageSenderID)) {
                                    // This is an incoming Invisible Ink message for the current user (recipient)
                                    roomMessage.setRevealed(false); // Explicitly set to false to ensure it's hidden on arrival
                                    Log.d(TAG, "onChildAdded (Executor): Incoming Invisible Ink message (ID: " + firebaseMessageId + ", Type: " + roomMessage.getType() + ") for recipient " + messageSenderID + ". Setting isRevealed=false.");
                                } else {
                                    // For all other cases, the default isRevealed=false is fine.
                                    // roomMessage.setRevealed(false); // Default is already false in constructor
                                }
                                // *** END NEW ***


                                // Basic validation for essential fields before inserting
                                if (TextUtils.isEmpty(roomMessage.getType()) || TextUtils.isEmpty(roomMessage.getFrom()) || TextUtils.isEmpty(roomMessage.getTo())) {
                                    Log.e(TAG, "Received message from Firebase with missing essential fields for messageId: " + firebaseMessageId + ". Skipping Room insertion.");
                                    return; // Skip insertion if essential fields are missing
                                }


                                // --- Insert the message into main Room DB (INSERT OR REPLACE strategy) for the current user ---
                                // This will insert a new message or update an existing one with the same FirebaseMessageId and OwnerUserId.
                                // Inserting here will trigger the LiveData observer -> forceRefreshDisplay() on Main Thread.
                                messageDao.insertMessage(roomMessage); // Insert the entity with all populated fields
                                Log.d(TAG, "Inserted/Updated message in main Room table: " + firebaseMessageId +
                                        " (onChildAdded), Owner: " + roomMessage.getOwnerUserId() +
                                        ", Type: " + roomMessage.getType() +
                                        ", Name: " + roomMessage.getName() + // Log name
                                        ", Conv ID: " + roomMessage.getConversationId() + // Log Conv ID
                                        ", Session ID: " + roomMessage.getDrawingSessionId() + // Log Session ID
                                        ", Effect: " + roomMessage.getDisplayEffect() +
                                        ", Revealed: " + roomMessage.isRevealed());


                                // --- Handle Notification for Incoming Messages (only if not already notified/seen) ---
                                // This block runs on the background executor.
                                // Only send notification for messages addressed *to* the current user (incoming messages).
                                if (roomMessage.getTo().equals(messageSenderID) && roomMessage.getFrom().equals(messageReceiverID)) { // Incoming message check
                                    // Check if the activity is not currently in a resumed state (user is not viewing this chat)
                                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                                        // Check if the message is not already marked as seen in Firebase (or in Room if your sync is fast)
                                        // Relying on isSeen from Firebase snapshot is safer for preventing duplicate notifications.
                                        Boolean firebaseSeenStatus = (Boolean) messageDataMap.get("seen"); // Get seen status directly from snapshot
                                        boolean isSeenInFirebase = (firebaseSeenStatus != null) ? firebaseSeenStatus : false;

                                        // Only send notification if the message is NOT already seen (by this user/device)
                                        if (!isSeenInFirebase) { // Check against Firebase seen status
                                            Log.d(TAG, "Incoming message " + firebaseMessageId + " received while activity is NOT RESUMED and is NOT seen in Firebase. Preparing notification.");

                                            // --- Decrypt message content or determine placeholder for Notification text ---
                                            String notificationTextToSend = "[Message]"; // Default placeholder

                                            // Get necessary info for decryption/content determination
                                            String storedContentBase64 = roomMessage.getMessage(); // Get the Base64 string from Room (or plaintext for system)
                                            String messageTypeForNotif = roomMessage.getType(); // Get the message type
                                            String displayEffectForNotif = roomMessage.getDisplayEffect(); // Get display effect
                                            // The sender's display name is needed for the notification title, we use messageReceiverName (chat partner's name)
                                            String senderNameForNotification = messageReceiverName; // The sender of this incoming message is the chat partner

                                            // --- Check message type and decrypt/format content ---
                                            // This logic is similar to what forceRefreshDisplay does, but streamlined for notification text.
                                            boolean isExpectedEncryptedType = "text".equals(messageTypeForNotif) || "image".equals(messageTypeForNotif) || "file".equals(messageTypeForNotif);
                                            boolean isSecureChatAvailableForNotification = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
                                            SecretKey conversationAESKey = null;
                                            if (isSecureChatAvailableForNotification) {
                                                conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId);
                                            }

                                            if (isExpectedEncryptedType && !TextUtils.isEmpty(storedContentBase64) && isSecureChatAvailableForNotification && conversationAESKey != null) {
                                                // Attempt decryption for text/image/file if keys are available
                                                try {
                                                    byte[] encryptedBytesWithIV = CryptoUtils.base64ToBytes(storedContentBase64);
                                                    if (encryptedBytesWithIV != null && encryptedBytesWithIV.length > 0) {
                                                        String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytesWithIV, conversationAESKey);
                                                        // Set notification text based on decrypted content and type
                                                        if ("text".equals(messageTypeForNotif)) {
                                                            notificationTextToSend = decryptedContent; // Use decrypted text
                                                        } else if ("image".equals(messageTypeForNotif)) {
                                                            notificationTextToSend = "[Image]"; // Placeholder for images
                                                        } else if ("file".equals(messageTypeForNotif)) {
                                                            notificationTextToSend = "[File]"; // Placeholder for files
                                                        }
                                                    } else {
                                                        Log.w(TAG, "Decoded encrypted bytes null/empty for notification for msg: " + firebaseMessageId);
                                                        notificationTextToSend = "[Invalid Encrypted Data]"; // Fallback
                                                    }
                                                } catch (IllegalArgumentException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidAlgorithmParameterException e) {
                                                    Log.e(TAG, "Decryption failed for notification content for msg: " + firebaseMessageId, e);
                                                    notificationTextToSend = "[Encrypted Message]"; // Fallback
                                                } catch (Exception e) {
                                                    Log.w(TAG, "Unexpected error during notification content decryption for msg: " + firebaseMessageId, e);
                                                    notificationTextToSend = "[Encrypted Message]"; // Fallback
                                                }
                                            } else if ("system_key_change".equals(messageTypeForNotif)) {
                                                // Content for system messages is plaintext in Room
                                                notificationTextToSend = storedContentBase64;
                                                if(TextUtils.isEmpty(notificationTextToSend)) notificationTextToSend = "[System Message]";
                                                Log.d(TAG, "Preparing notification content for system message " + firebaseMessageId + ".");
                                            }
                                            // *** ADD: Handle Drawing Link type for notification content ***
                                            else if ("one_to_one_drawing_session_link".equals(messageTypeForNotif)) {
                                                // The 'message' field for drawing link messages contains the text preview ("User X started...")
                                                notificationTextToSend = storedContentBase64; // Use the stored text preview
                                                if(TextUtils.isEmpty(notificationTextToSend)) notificationTextToSend = "[Drawing Session Link]";
                                                Log.d(TAG, "Preparing notification content for drawing link message " + firebaseMessageId + ".");

                                            }
                                            // *** END ADD ***
                                            else {
                                                // Content is empty OR not an expected encrypted type OR keys unavailable/error
                                                Log.w(TAG, "Notification content determination skipped/failed for msg " + firebaseMessageId + ". Using type-specific placeholder.");
                                                if ("image".equals(messageTypeForNotif)) {
                                                    notificationTextToSend = "[Image]";
                                                } else if ("file".equals(messageTypeForNotif)) {
                                                    notificationTextToSend = "[File]";
                                                } else {
                                                    notificationTextToSend = "[Message]"; // Generic placeholder for text/unknown
                                                }
                                            }
                                            // --- End Decrypt message content ---

                                            // *** Adjust notification text specifically for Invisible Ink if you want to hide the preview ***
                                            // This check happens AFTER determining the base content
                                            String finalNotificationContentToSend = notificationTextToSend; // Start with the determined text
                                            if ("invisible_ink".equals(displayEffectForNotif)) {
                                                // If it's Invisible Ink, override the content in the notification shade
                                                // to prevent revealing it before the user taps to reveal in the app.
                                                finalNotificationContentToSend = "You Received a secret message \uD83D\uDC7B"; // Set the requested text or "[New Invisible Message]"
                                                Log.d(TAG, "Adjusting notification text for incoming Invisible Ink message.");
                                            }
                                            // *** End Adjustment ***


                                            // Send push notification using OneSignal API helper
                                            // This method is assumed to be safe to call from a background thread or handles threading internally.
                                            // *** Call sendPushNotification with all parameters ***
                                            sendPushNotification(
                                                    messageSenderID, // Recipient of the notification is the current user
                                                    "New Message from " + senderNameForNotification, // Notification Title (use chat partner's name)
                                                    finalNotificationContentToSend, // Notification content (the adjusted text)
                                                    messageTypeForNotif, // <<< Pass the message type
                                                    roomMessage.getConversationId(), // <<< Pass conversationId from RoomEntity (should be THIS chat's ID)
                                                    roomMessage.getDrawingSessionId(), // <<< Pass sessionId from RoomEntity (will be null for most types, Session ID for drawing links)
                                                    roomMessage.getFirebaseMessageId() // <<< Pass messageId (optional, but good practice)
                                            );
                                            // *** END Call ***

                                        } else {
                                            // Message is already marked as seen in Firebase, do not send notification.
                                            Log.d(TAG, "Incoming message " + firebaseMessageId + " is already seen in Firebase. Skipping notification.");
                                        }
                                    } else {
                                        // Activity is resumed (user is viewing chat). Mark visible messages as seen after adapter updates.
                                        // No need to send notification if the user is actively in the chat.
                                        Log.d(TAG, "Incoming message " + firebaseMessageId + " received while activity IS RESUMED. Skipping notification.");
                                        // The markVisibleMessagesAsSeen() logic after forceRefreshDisplay will handle marking it seen.
                                    }
                                }
                                // --- End Notification Handling ---


                                // --- Check if the incoming message is a system key change message (Keep existing logic) ---
                                // Only process this if it's an incoming message from the other user AND it's a system_key_change type
                                if ("system_key_change".equals(roomMessage.getType()) && roomMessage.getFrom().equals(messageReceiverID) && roomMessage.getTo().equals(messageSenderID)) {
                                    Log.d(TAG, "onChildAdded (Executor): Received incoming system_key_change message. Triggering conversation key load attempt.");

                                    // Trigger the conversation key load attempt if the user's private key is available.
                                    if (YourKeyManager.getInstance().isPrivateKeyAvailable()) {
                                        // Call the helper method to attempt loading the key for this conversation
                                        // This method runs on the same ExecutorService.
                                        attemptLoadConversationKeyAsync(conversationId, messageSenderID); // Pass THIS chat's conversationId and current user ID
                                    } else {
                                        Log.w(TAG, "onChildAdded (Executor): Received system_key_change, but user's private key is NOT available. Cannot load new key.");
                                        // Messages will continue to show as failed until user unlocks account.
                                    }
                                }
                                // --- END Check ---


                            } catch (Exception e) { // Catch any unexpected errors within this executor task
                                Log.e(TAG, "UNEXPECTED ERROR processing message " + firebaseMessageId + " in onChildAdded executor!", e);
                                // Log the error, but allow other messages to process
                                // Consider adding this message to Room with a specific error status if needed
                            }
                        }); // End databaseWriteExecutor.execute()
                    } else {
                        Log.w(TAG, "Received null message data or messageId from Firebase DataSnapshot in onChildAdded for conv " + conversationId);
                    }
                } // End onChildAdded


                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // *** Your existing onChildChanged implementation with preservation logic goes here ***
                    // Make sure it includes fetching the existing Room message to preserve 'isRevealed' and 'scheduledTime'.
                    // Also ensure it reads the latest 'seen' status and potentially other fields that change.
                    Map<String, Object> messageDataMap = (Map<String, Object>) snapshot.getValue();
                    String firebaseMessageId = snapshot.getKey();

                    if (messageDataMap != null && firebaseMessageId != null) {
                        Log.d(TAG, "onChildChanged triggered for messageId: " + firebaseMessageId + " in conv " + conversationId);

                        databaseWriteExecutor.execute(() -> {
                            try {
                                // --- NEW: Check if this message is marked as deleted for me (Keep existing) ---
                                if (db == null || messageSenderID == null) {
                                    Log.e(TAG, "onChildChanged (Executor): ChatDatabase or messageSenderID instance is null. Cannot check for locally deleted messages.");
                                    return; // Skip if prerequisites missing
                                }
                                DeletedMessageIdDao deletedMessageIdDao = db.deletedMessageIdDao();
                                boolean isLocallyDeleted = deletedMessageIdDao.isMessageDeletedForUser(messageSenderID, firebaseMessageId);

                                if (isLocallyDeleted) {
                                    Log.d(TAG, "onChildChanged (Executor): Message " + firebaseMessageId + " is marked as locally deleted for owner " + messageSenderID + ". Ensuring removal from Room main table.");
                                    // If a message changes but is marked as deleted locally, ensure it's removed from Room main table.
                                    removeMessageFromRoomById(firebaseMessageId, messageSenderID); // Call a helper method to remove from main table
                                    return; // IMPORTANT: Skip updating if locally deleted
                                }
                                // --- END NEW ---


                                // --- Fetch EXISTING MessageEntity from Room to preserve local state ---
                                MessageEntity existingMessage = messageDao.getMessageByFirebaseId(firebaseMessageId, messageSenderID);
                                boolean currentIsRevealedState = false; // Default if not found
                                String currentScheduledTime = null; // Default if not found
                                // Preserve name, convId, sessionId from existing message if they might not be updated in Firebase
                                String preservedName = null;
                                String preservedConvId = null;
                                String preservedSessionId = null;


                                if (existingMessage != null) {
                                    // Preserve the local 'isRevealed' state
                                    currentIsRevealedState = existingMessage.isRevealed();
                                    // Preserve the local 'scheduledTime' state (Firebase might not have this or not update it)
                                    currentScheduledTime = existingMessage.getScheduledTime();
                                    // Preserve name, conversationId, drawingSessionId from the existing message.
                                    // These fields are usually only set on ADD, not necessarily updated on CHANGE.
                                    preservedName = existingMessage.getName();
                                    preservedConvId = existingMessage.getConversationId();
                                    preservedSessionId = existingMessage.getDrawingSessionId();

                                    Log.d(TAG, "onChildChanged (Executor): Found existing message " + firebaseMessageId + " in Room. Preserving isRevealed=" + currentIsRevealedState + ", ScheduledTime=" + currentScheduledTime + ", Name=" + preservedName + ", ConvID=" + preservedConvId + ", SessionID=" + preservedSessionId);
                                } else {
                                    Log.w(TAG, "onChildChanged (Executor): Message " + firebaseMessageId + " NOT found in main Room table for owner " + messageSenderID + ". Cannot preserve local state. It will be inserted/updated with defaults/Firebase data.");
                                    // In this case, the new MessageEntity will get default isRevealed=false.
                                }
                                // --- END Fetch Existing and Preserve ---


                                // --- Create/Update MessageEntity from Firebase data ---
                                MessageEntity updatedMessage = new MessageEntity(); // Create a new entity based on fresh data from Firebase snapshot

                                // Populate fields from Firebase snapshot (Keep existing logic)
                                updatedMessage.setFirebaseMessageId(firebaseMessageId);
                                updatedMessage.setOwnerUserId(messageSenderID); // Owner is the current user
                                updatedMessage.setMessage((String) messageDataMap.get("message")); // Base64 string or plaintext
                                updatedMessage.setType((String) messageDataMap.get("type"));
                                updatedMessage.setFrom((String) messageDataMap.get("from"));
                                updatedMessage.setTo((String) messageDataMap.get("to"));
                                updatedMessage.setSendTime((String) messageDataMap.get("sendTime"));

                                // Get latest seen status from Firebase (This is often what changes)
                                Object seenObj = messageDataMap.get("seen");
                                boolean latestSeenStatus = (seenObj instanceof Boolean) ? (Boolean) seenObj : false;
                                updatedMessage.setSeen(latestSeenStatus); // Update seen status from Firebase

                                updatedMessage.setSeenTime((String) messageDataMap.get("seenTime")); // Get latest seen time from Firebase

                                // Get status (string) - Status might be updated by Firebase (e.g., from "pending" to "sent" if sent from another device?)
                                String statusStr = (String) messageDataMap.get("status");
                                updatedMessage.setStatus(!TextUtils.isEmpty(statusStr) ? statusStr : (updatedMessage.getFrom().equals(messageSenderID) ? "sent" : "received")); // Default if missing

                                updatedMessage.setScheduledTime((String) messageDataMap.get("scheduledTime")); // Get scheduledTime from Firebase (might be null)

                                Long timestampLong = snapshot.hasChild("timestamp") ? snapshot.child("timestamp").getValue(Long.class) : null;
                                updatedMessage.setTimestamp(timestampLong != null && timestampLong > 0 ? timestampLong : System.currentTimeMillis());


                                // *** READ displayEffect from Firebase CHANGE snapshot (Keep existing) ***
                                String firebaseDisplayEffect = (String) messageDataMap.get("displayEffect");
                                updatedMessage.setDisplayEffect(!TextUtils.isEmpty(firebaseDisplayEffect) ? firebaseDisplayEffect : "none");
                                // *** END READ displayEffect ***


                                // --- ADD: Read conversationId, drawingSessionId, and name from CHANGED Firebase snapshot ---
                                // Read these fields. If they are null in Firebase, use the preserved value from the existing message.
                                String firebaseConversationId = (String) messageDataMap.get("conversationId");
                                String firebaseDrawingSessionId = (String) messageDataMap.get("drawingSessionId"); // This Firebase field stores the session ID
                                String senderDisplayNameFromFirebase = (String) messageDataMap.get("name");

                                // Prefer Firebase data if available (non-empty), otherwise use the preserved local data
                                updatedMessage.setConversationId(!TextUtils.isEmpty(firebaseConversationId) ? firebaseConversationId : preservedConvId);
                                updatedMessage.setDrawingSessionId(!TextUtils.isEmpty(firebaseDrawingSessionId) ? firebaseDrawingSessionId : preservedSessionId);
                                updatedMessage.setName(!TextUtils.isEmpty(senderDisplayNameFromFirebase) ? senderDisplayNameFromFirebase : preservedName);
                                // --- END ADD ---


                                // *** RESTORE the LOCAL isRevealed state from the previously fetched entity (Keep existing) ***
                                // Do NOT read isRevealed from Firebase. Use the preserved state from `existingMessage`.
                                updatedMessage.setRevealed(currentIsRevealedState);
                                // *** RESTORE the LOCAL scheduledTime state from the previously fetched entity (Keep existing) ***
                                // We already read scheduledTime from Firebase, but if the Firebase snapshot didn't have it,
                                // and the local one did (e.g., set by scheduler worker), we should preserve it.
                                // This check is slightly redundant now that we read it from Firebase *and* preserve,
                                // but keeping the preservation logic is safe. Use the one from Firebase if available, else preserved.
                                updatedMessage.setScheduledTime(!TextUtils.isEmpty(updatedMessage.getScheduledTime()) ? updatedMessage.getScheduledTime() : currentScheduledTime);

                                // *** END RESTORE ***


                                // Basic validation before inserting (Keep existing)
                                if (TextUtils.isEmpty(updatedMessage.getType()) || TextUtils.isEmpty(updatedMessage.getFrom()) || TextUtils.isEmpty(updatedMessage.getTo())) {
                                    Log.e(TAG, "Received changed message from Firebase with missing essential fields for messageId: " + firebaseMessageId + ". Skipping Room update.");
                                    return;
                                }

                                // Save the updated message entity back to Room (REPLACE strategy)
                                // This will trigger LiveData update -> forceRefreshDisplay() on Main Thread
                                messageDao.insertMessage(updatedMessage); // insert with REPLACE strategy updates if exists
                                Log.d(TAG, "Updated message in main Room table from Firebase change for owner " + messageSenderID + ": " + firebaseMessageId +
                                        ", Type: " + updatedMessage.getType() +
                                        ", Name: " + updatedMessage.getName() + // Log name
                                        ", Conv ID: " + updatedMessage.getConversationId() + // Log Conv ID
                                        ", Session ID: " + updatedMessage.getDrawingSessionId() + // Log Session ID
                                        ", Seen: " + updatedMessage.isSeen() + // Log changed field
                                        ", Effect: " + updatedMessage.getDisplayEffect() +
                                        ", Revealed (Preserved): " + updatedMessage.isRevealed() +
                                        ", ScheduledTime (Preserved): " + updatedMessage.getScheduledTime());


                            } catch (Exception e) {
                                Log.e(TAG, "UNEXPECTED ERROR processing message " + firebaseMessageId + " in onChildChanged executor!", e);
                                // Log the error, but allow other messages to process
                            }
                        }); // End databaseWriteExecutor.execute()
                    } else {
                        Log.w(TAG, "Received null message data or messageId from Firebase DataSnapshot in onChildChanged for conv " + conversationId);
                    }
                } // End onChildChanged


                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    // Keep existing logic for deletion from Room main table and locally deleted list
                    String firebaseMessageId = snapshot.getKey();
                    if (firebaseMessageId != null && !TextUtils.isEmpty(messageSenderID) && db != null) {
                        Log.d(TAG, "onChildRemoved triggered for messageId: " + firebaseMessageId + " in conv " + conversationId + ". Deleting from Room for owner " + messageSenderID + " and removing from locally deleted list.");
                        databaseWriteExecutor.execute(() -> {
                            try {
                                // Delete from main messages table owned by this user
                                removeMessageFromRoomById(firebaseMessageId, messageSenderID); // Use helper method

                                // Remove this ID from the locally deleted list (since it's gone from Firebase for everyone)
                                DeletedMessageIdDao deletedMessageIdDao = db.deletedMessageIdDao();
                                int deletedFromLocalListCount = deletedMessageIdDao.deleteDeletedMessageId(messageSenderID, firebaseMessageId);
                                if (deletedFromLocalListCount > 0) {
                                    Log.d(TAG, "Removed message ID " + firebaseMessageId + " from locally deleted list for owner " + messageSenderID + " via onChildRemoved.");
                                } else {
                                    // This is expected if the message wasn't deleted locally before being deleted for everyone
                                    Log.d(TAG, "Message ID " + firebaseMessageId + " was not found in the locally deleted list for owner " + messageSenderID + " when onChildRemoved triggered.");
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "UNEXPECTED ERROR processing message " + firebaseMessageId + " in onChildRemoved executor!", e);
                                // Log the error, but allow other messages to process
                            }
                        }); // End databaseWriteExecutor.execute()
                    } else {
                        Log.w(TAG, "onChildRemoved received with null messageId, senderID, or db for conv " + conversationId);
                    }
                } // End onChildRemoved


                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // Keep existing logic if any (usually not needed when ordering by timestamp)
                    // Log.d(TAG, "onChildMoved triggered for messageId: " + snapshot.getKey());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Keep existing logic for handling cancellation
                    Log.e(TAG, "Firebase Chat Listener Cancelled for conversation " + conversationId + ": " + error.getMessage(), error.toException());
                    // Show error message to the user on the main thread
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Chat updates failed: " + error.getMessage(), Toast.LENGTH_LONG).show());
                    // Optionally clear local Room data or show an error state for the chat if sync fails critically
                }
            }; // End of messageListener ChildEventListener definition

            // Attach the listener to the conversation-specific path in Firebase
            conversationRef.addChildEventListener(messageListener);
            Log.d(TAG, "Firebase listener attached for chat path: Messages/" + conversationId);

        } else {
            Log.d(TAG, "Firebase listener already attached for conv " + conversationId + ".");
        }
    }
    // --- *** END Modified attachMessageListener *** ---


    // --- ADD NEW Helper Method to remove message from Room by ID ---
    // This method is useful when told by Firebase (onChildRemoved) or when checking locally deleted.
    // It should be called on the databaseWriteExecutor.
    /**
     * Deletes a message from the local Room DB by its Firebase ID for a specific owner.
     * Should be called on a background thread (e.g., databaseWriteExecutor).
     * @param firebaseMessageId The Firebase ID of the message.
     * @param ownerUserId The ID of the user who owns this copy of the message in Room.
     * @return The number of rows deleted.
     */
    private int removeMessageFromRoomById(String firebaseMessageId, String ownerUserId) {
        if (TextUtils.isEmpty(firebaseMessageId) || TextUtils.isEmpty(ownerUserId) || messageDao == null) {
            Log.e(TAG, "Cannot remove message from Room by ID: Missing IDs or DAO.");
            return 0;
        }
        try {
            int deletedRows = messageDao.deleteMessageByFirebaseId(firebaseMessageId, ownerUserId); // Use your DAO method
            if (deletedRows > 0) {
                Log.d(TAG, "Message removed from Room DB for ID: " + firebaseMessageId + " (Owner: " + ownerUserId + "). Rows deleted: " + deletedRows);
                // LiveData observer will pick this Room change up and update UI automatically
            } else {
                // This is expected if the message didn't exist in Room for some reason (e.g., hasn't synced yet or already gone)
                Log.d(TAG, "Attempted to remove message " + firebaseMessageId + " from Room DB for owner " + ownerUserId + ", but it was not found. Rows deleted: " + deletedRows);
            }
            return deletedRows;
        } catch (Exception e) {
            Log.e(TAG, "Error removing message " + firebaseMessageId + " from Room DB for owner " + ownerUserId, e);
            // Handle Room DB errors (log, retry?)
            return 0;
        }
    }
    // --- END NEW Helper Method ---


// ... rest of ChatPageActivity methods ...
    // --- *** END Modify attachMessageListener *** ---




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
    // Delete message only from the current user's Room database (the copy owned by them)
    private void deleteMessageForMe(String firebaseMessageId) {
        if (TextUtils.isEmpty(firebaseMessageId) || TextUtils.isEmpty(messageSenderID) || db == null) {
            Log.e(TAG, "Cannot delete message for me, firebaseMessageId, messageSenderID, or db is null or empty.");
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error deleting message locally.", Toast.LENGTH_SHORT).show());
            return;
        }

        databaseWriteExecutor.execute(() -> {
            try {
                // --- NEW: Record the message ID as deleted for the current user ---
                DeletedMessageIdDao deletedMessageIdDao = db.deletedMessageIdDao(); // Get the new DAO
                DeletedMessageIdEntity deletedEntity = new DeletedMessageIdEntity(messageSenderID, firebaseMessageId); // Create new entity
                deletedMessageIdDao.insertDeletedMessageId(deletedEntity); // Insert into the new table
                Log.d(TAG, "Recorded message ID as deleted for me: " + firebaseMessageId + " for owner " + messageSenderID);
                // --- END NEW ---

                // --- Existing: Delete the message from the main messages table ---
                int deletedRows = messageDao.deleteMessageByFirebaseId(firebaseMessageId, messageSenderID);
                if (deletedRows > 0) {
                    Log.d(TAG, "Message deleted from main Room table (for me) for owner " + messageSenderID + ": " + firebaseMessageId);
                } else {
                    Log.w(TAG, "Attempted to delete message " + firebaseMessageId + " for me from main table, but it wasn't found in Room for owner " + messageSenderID + ". This is expected if the message hasn't synced yet.");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error during deleteMessageForMe operation for msg: " + firebaseMessageId, e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error deleting message locally.", Toast.LENGTH_SHORT).show());
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

        // *** DELETE THIS ENTIRE BLOCK ***
        // This old image picking logic is replaced by ActivityResultLaunchers.
        // if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
        //     imageUri = data.getData();
        //     try {
        //         InputStream inputStream = getContentResolver().openInputStream(imageUri);
        //         Bitmap bitmap = null;
        //         if (inputStream != null) {
        //             bitmap = BitmapFactory.decodeStream(inputStream);
        //             inputStream.close();
        //         }
        //
        //         if (bitmap != null) {
        //             String base64Image = convertBitmapToBase64(bitmap); // Get Base64 string of the image
        //             bitmap.recycle();
        //             // This call caused the error because sendImageMessage now needs 2 arguments:
        //             // sendImageMessage(base64Image); // PROBLEM LINE
        //         } else {
        //             Log.e(TAG, "Failed to decode bitmap from URI");
        //             Toast.makeText(this, "Error loading image!", Toast.LENGTH_SHORT).show();
        //         }
        //     } catch (IOException e) {
        //         Log.e(TAG, "IOException loading image from URI", e);
        //         Toast.makeText(this, "Error loading image!", Toast.LENGTH_SHORT).show();
        //     } catch (Exception e) {
        //         Log.e(TAG, "General error loading image from URI", e);
        //         Toast.makeText(this, "Error loading image!", Toast.LENGTH_SHORT).show();
        //     }
        // }
        // *** END OF BLOCK TO DELETE ***


        // Keep the rest of the onActivityResult method (wallpaper handling)

        // Handle Wallpaper Image Selection/Capture (Keep as is)
          if (requestCode == REQUEST_CODE_PICK_IMAGE_WALLPAPER && resultCode == RESULT_OK && data != null && data.getData() != null) {
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
            // Clean up temporary camera file for wallpaper on cancel
            if (requestCode == REQUEST_CODE_TAKE_PHOTO_WALLPAPER && cameraPhotoUri != null) {
                try {
                    getContentResolver().delete(cameraPhotoUri, null, null);
                    Log.d(TAG, "Cleaned up temp wallpaper camera file on cancel.");
                } catch (Exception e) { Log.w(TAG, "Failed to clean up temp wallpaper camera file on cancel", e); } finally { cameraPhotoUri = null; }
            }
            // Also check and clean up temp message camera URI if cancellation was for message camera
            // This might be handled by the launcher callback's error path, but a fallback here is okay.
            // Check if this was the message camera request (might need a specific request code if launchers aren't enough)
            // For launchers, the cancellation is handled by the launcher's callback receiving 'isSuccess = false'.
            // The cleanup logic for imageToSendUri should be in the takePictureLauncher callback's else block.
            // So, this else-if block for RESULT_CANCELED should primarily focus on wallpaper cleanup.
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
    // Inside ChatPageActivity.java class body { ... }

    /**
     * Helper method to send a push notification via OneSignal API.
     * Runs on the main thread, Retrofit handles background network call.
     */
    // --- MODIFIED SIGNATURE: Add parameters for type, convId, sessionId, messageId ---
    private void sendPushNotification(String recipientFirebaseUID, String title, String messageContent,
                                      @Nullable String messageType, // <<< NEW Parameter
                                      @Nullable String conversationId, // <<< NEW Parameter
                                      @Nullable String sessionId, // <<< NEW Parameter
                                      @Nullable String messageId) { // <<< NEW Parameter (e.g., Firebase Message ID)
        if (oneSignalApiService == null || TextUtils.isEmpty(recipientFirebaseUID)) {
            Log.e(TAG, "sendPushNotification: API service not initialized or recipient UID is empty.");
            return;
        }
        Log.d(TAG, "Preparing to send push notification to recipient UID (External ID): " + recipientFirebaseUID);

        JsonObject notificationBody = new JsonObject();

        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID); // Your OneSignal App ID

        JsonArray externalUserIds = new JsonArray();
        externalUserIds.add(recipientFirebaseUID);
        notificationBody.add("include_external_user_ids", externalUserIds); // Target recipients by Firebase UID

        // Add notification title and content
        notificationBody.add("headings", new Gson().toJsonTree(Collections.singletonMap("en", title)));
        notificationBody.add("contents", new Gson().toJsonTree(Collections.singletonMap("en", messageContent)));


        // --- MODIFIED: Add custom data payload ---
        JsonObject data = new JsonObject();
        // Add sender and receiver IDs (useful for identifying chat)
        data.addProperty("senderId", messageSenderID);
        data.addProperty("recipientId", messageReceiverID); // In 1:1, this is the chat partner ID

        // Add message type, conversation ID, session ID, and message ID if provided
        if (!TextUtils.isEmpty(messageType)) {
            data.addProperty("messageType", messageType); // Include message type (e.g., "text", "image", "one_to_one_drawing_session_link")
            // Set a specific event type based on message type for easier handling in recipient app
            if ("one_to_one_drawing_session_link".equals(messageType)) {
                data.addProperty("eventType", "drawing_link"); // Specific event type for drawing links
            } else if ("system_key_change".equals(messageType)) {
                data.addProperty("eventType", "system_message"); // Specific event type for system messages
            }
            else {
                data.addProperty("eventType", "message"); // Default generic event type for text/image/file etc.
            }
        } else {
            // Default event type if message type is missing (should not happen if parameters are passed correctly)
            data.addProperty("eventType", "message");
        }


        if (!TextUtils.isEmpty(conversationId)) {
            data.addProperty("conversationId", conversationId); // Include conversation ID
        }
        if (!TextUtils.isEmpty(sessionId)) {
            // Use a generic key like "sessionId" in the data payload for drawing links
            data.addProperty("sessionId", sessionId); // Include session ID
        }
        if (!TextUtils.isEmpty(messageId)) {
            data.addProperty("messageId", messageId); // Include Firebase message ID (optional, but useful for deep linking)
        }
        // --- END MODIFIED ADD DATA ---


        notificationBody.add("data", data); // Add the custom data payload

        // Optional: Set small icon (recommended)
        notificationBody.addProperty("small_icon", "app_icon_circleup"); // <<< Replace with your icon's resource name (string)


        // Optional: Customize notification appearance (sound, vibration, etc.)


        // Make the API call asynchronously using Retrofit
        Log.d(TAG, "Making OneSignal API call...");
        oneSignalApiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "OneSignal API call successful. Response Code: " + response.code());
                    try (ResponseBody responseBody = response.body()) {
                        String resBody = responseBody != null ? responseBody.string() : "N/A";
                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
                        // Look for "id" and "recipients" in the response body JSON for confirmation
                    } catch (IOException e) { Log.e(TAG, "Failed to read success response body", e); }
                } else {
                    Log.e(TAG, "OneSignal API call failed. Response Code: " + response.code());
                    try (ResponseBody errorBody = response.errorBody()) {
                        String errBody = errorBody != null ? errorBody.string() : "N/A"; // Corrected to errorBody().string()
                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
                        // Common errors: 400 (Invalid JSON), 403 (Invalid REST API Key), 404 (App ID not found), Invalid External IDs
                    } catch (IOException e) { Log.e(TAG, "Failed to read error response body", e); }
                    Log.w(TAG, "Failed to send push notification via OneSignal API.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "OneSignal API call failed (network error)", t);
                Log.w(TAG, "Failed to send push notification due to network error.");
            }
        });
        Log.d(TAG, "OneSignal API call enqueued.");
    }
    // --- END MODIFIED HELPER METHOD ---

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
// Inside ChatPageActivity.java

// ... (Keep your existing imports at the top) ...

    /**
     * Forces the message list to re-process its current data from the LiveData's last value.
     * Useful when decryption keys become available after the data was initially loaded,
     * or data changes in Room (status, seen, or isRevealed for Invisible Ink).
     * Assumes the LiveData's last value is still available in the `chatMessagesLiveData` object.
     * This method should be called on the Main Thread because it updates UI via the adapter.
     */
    public void forceRefreshDisplay() {
        Log.d(TAG, "🟢 forceRefreshDisplay() called for conversation " + conversationId);

        // Ensure LiveData has a value and adapter exists
        if (chatMessagesLiveData == null || chatMessagesLiveData.getValue() == null || messageAdapter == null || messagesList == null || messagesList.getLayoutManager() == null) {
            Log.w(TAG, "forceRefreshDisplay skipped: LiveData, value, adapter, messagesList, or LayoutManager is null.");

            // Clear adapter list defensively even if components are null/empty
            if (messageAdapter != null) {
                messagesArrayList.clear(); // Clear the adapter's data source
                messageAdapter.notifyDataSetChanged(); // Notify the adapter that the dataset has changed (now empty)
            }

            // Still disable UI if keys are unavailable, even if no messages displayed
            // This check also ensures UI is enabled if keys ARE available but something else was null above
            boolean isSecureChatCurrentlyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
            if (!isSecureChatCurrentlyAvailable) {
                if (messageInputText != null) { messageInputText.setEnabled(false); messageInputText.setHint("Secure chat unavailable"); }
                if (sendMessageButton != null) sendMessageButton.setEnabled(false);
                if (attachmentButton != null) attachmentButton.setEnabled(false); // Include attachment button
                Log.d(TAG, "forceRefreshDisplay disabled UI due to keys unavailable.");
            } else {
                if (messageInputText != null) { messageInputText.setEnabled(true); messageInputText.setHint("Type The Messages..."); }
                if (sendMessageButton != null) sendMessageButton.setEnabled(true);
                if (attachmentButton != null) attachmentButton.setEnabled(true); // Include attachment button
                Log.d(TAG, "forceRefreshDisplay ensured UI is enabled as keys are available.");
            }


            Log.d(TAG, "✅ forceRefreshDisplay finished (skipped due to initial nulls).");
            return;
        }

        // Get the latest list of messages from the LiveData
        List<MessageEntity> currentMessages = chatMessagesLiveData.getValue();

        Log.d(TAG, "forceRefreshDisplay triggered. Re-processing " + (currentMessages != null ? currentMessages.size() : 0) + " MessageEntity objects from Room for owner " + messageSenderID);

        // Get the *single* conversation key from KeyManager *before* processing messages.
        SecretKey conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId); // *** MODIFIED ***
        // Check if the user's main private key is available (required to decrypt conversation keys, indicates account is unlocked)
        boolean isPrivateKeyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable();
        // Check if the single conversation key is available
        boolean isConversationKeyAvailable = (conversationAESKey != null);

        Log.d(TAG, "KeyManager State for Display: PrivateKey Available = " + isPrivateKeyAvailable + ", Conversation Key Available = " + isConversationKeyAvailable);

        // Clear the list that feeds the adapter for a fresh populate based on current state
        messagesArrayList.clear();

        if (currentMessages != null && !currentMessages.isEmpty()) {

            // --- *** PROCESS Messages before adding to the list for the adapter *** ---

            for (MessageEntity storedMessageEntity : currentMessages) {
                if (storedMessageEntity == null) {
                    Log.w(TAG, "Skipping null MessageEntity from Room list.");
                    continue; // Skip this iteration
                }

                String storedMessageContentBase64 = storedMessageEntity.getMessage(); // This is the Base64 string from Room (could be encrypted or plaintext/raw)
                String messageType = storedMessageEntity.getType();
                String fromUserId = storedMessageEntity.getFrom();
                String toUserId = storedMessageEntity.getTo();
                String firebaseMessageId = storedMessageEntity.getFirebaseMessageId();
                String displayEffect = storedMessageEntity.getDisplayEffect(); // *** Get NEW field ***
                boolean isRevealed = storedMessageEntity.isRevealed(); // *** Get NEW field ***
                String scheduledTime = storedMessageEntity.getScheduledTime(); // Get the scheduled time


                String storedConversationId = storedMessageEntity.getConversationId(); // Get conversation ID from Room
                String storedDrawingSessionId = storedMessageEntity.getDrawingSessionId(); // Get session ID from Room
                String storedSenderName = storedMessageEntity.getName(); // Get sender name from Room


                String processedContentForAdapter = ""; // This will hold the final content to display in the adapter
                String processingOutcome = "Default"; // Track how the content was determined



                // --- ADD THIS BLOCK: Handle Drawing Link Type First ---
                if ("one_to_one_drawing_session_link".equals(messageType)) {
                    // For drawing link, the stored content is the preview text. Just use it directly.
                    processedContentForAdapter = storedMessageContentBase64;
                    processingOutcome = "Drawing Link";
                    // No decryption needed for this type
                    Log.d(TAG, "Msg " + storedMessageEntity.getFirebaseMessageId() + ": Handling as Drawing Link message type. Outcome: " + processingOutcome);
                }
                // --- Determine the expected state and type ---
                boolean isIncoming = toUserId != null && toUserId.equals(messageSenderID); // Is message for the current user?
                boolean isOutgoing = fromUserId != null && fromUserId.equals(messageSenderID); // Is message sent by the current user?
                boolean isExpectedEncryptedType = "text".equals(messageType) || "image".equals(messageType) || "file".equals(messageType); // Types that *should* be encrypted
                boolean isPlaintextScheduled = isExpectedEncryptedType && !TextUtils.isEmpty(scheduledTime); // Is it text/image/file sent via scheduler? (assumed plaintext/raw in Room)
                boolean isAlwaysPlaintextType = "system_key_change".equals(messageType); // Types never encrypted by our app


                // --- Step 1: Determine Base Content (Decrypted, Raw, or Placeholder due to Crypto State) ---
                if (TextUtils.isEmpty(storedMessageContentBase64)) {
                    // Content is empty in Room - show a type-specific placeholder
                    if ("image".equals(messageType)) processedContentForAdapter = "[Image Data Missing]";
                    else if ("file".equals(messageType)) processedContentForAdapter = "[File Data Missing]";
                    else if ("system_key_change".equals(messageType)) processedContentForAdapter = "[System Message Data Missing]";
                    else processedContentForAdapter = "[Empty Message]"; // Default for text or unknown type
                    processingOutcome = "Content Empty";
                    Log.w(TAG, "Msg " + firebaseMessageId + " (Type: " + messageType + "): Stored content empty. Outcome: " + processingOutcome);

                }
                // --- Handle Plaintext Scheduled Messages (Text, Image, File) or Always Plaintext Types ---
                else if (isPlaintextScheduled || isAlwaysPlaintextType) {
                    // This message is either a text/image/file from the scheduled worker (has scheduledTime)
                    // OR it is a type that is never encrypted (system_key_change).
                    // In both cases, the stored content is the plaintext/raw data (Base64 for image/file, text for system/scheduled text).
                    processedContentForAdapter = storedMessageContentBase64; // Use the stored content directly
                    if (TextUtils.isEmpty(processedContentForAdapter)) {
                        // Fallback to a placeholder if the *intended* plaintext content is empty
                        if ("image".equals(messageType)) processedContentForAdapter = "[Image Data Missing]";
                        else if ("file".equals(messageType)) processedContentForAdapter = "[File Data Missing]";
                        else if ("system_key_change".equals(messageType)) processedContentForAdapter = "[System Message Data Missing]";
                        else processedContentForAdapter = "[Empty Message]";
                        processingOutcome = "Plaintext/Always Plaintext Type (Content Empty Fallback)";
                    } else {
                        processingOutcome = isPlaintextScheduled ? "Plaintext Scheduled" : "Always Plaintext Type";
                    }
                    Log.d(TAG, "Msg " + firebaseMessageId + " (Type: " + messageType + ", Scheduled: " + scheduledTime + ", Effect: " + displayEffect + "): Displaying as plaintext/raw. Outcome: " + processingOutcome);
                }
                // --- Handle Potentially Encrypted Text/Image/File Messages (Not Scheduled) ---
                else if (isExpectedEncryptedType) { // This condition implies it's text/image/file, has content, and is NOT scheduled

                    // Check if keys are available to decrypt
                    if (isPrivateKeyAvailable && isConversationKeyAvailable) {
                        // Keys available, attempt decryption
                        processingOutcome = "Attempting Decryption";
                        Log.d(TAG, "Msg " + firebaseMessageId + " (Type: " + messageType + ", Effect: " + displayEffect + "): Not scheduled. Keys available. Attempting decryption.");

                        byte[] encryptedBytesWithIV = null;
                        try {
                            // Decode the Base64 string from Room FIRST
                            encryptedBytesWithIV = CryptoUtils.base64ToBytes(storedMessageContentBase64);

                            if (encryptedBytesWithIV == null || encryptedBytesWithIV.length == 0) {
                                Log.w(TAG, "Msg " + firebaseMessageId + ": Decoded encrypted bytes null/empty after Base64 decode.");
                                processingOutcome = "Decoded Empty Bytes";
                                if ("image".equals(messageType)) processedContentForAdapter = "[Invalid Encrypted Image Data]";
                                else if ("file".equals(messageType)) processedContentForAdapter = "[Invalid Encrypted File Data]";
                                else processedContentForAdapter = "[Invalid Encrypted Data]"; // Default for text
                            } else {
                                // Decrypt using the single conversation key
                                try {
                                    String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytesWithIV, conversationAESKey);
                                    processedContentForAdapter = decryptedContent; // Use decrypted content
                                    processingOutcome = "Decryption Success";
                                    Log.d(TAG, "Msg " + firebaseMessageId + ": Decrypted SUCCESSFULLY.");
                                } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidAlgorithmParameterException e) {
                                    // Crypto decryption failed for THIS key!
                                    Log.d(TAG, "Msg " + firebaseMessageId + ": Decryption FAILED. " + e.getClass().getSimpleName());
                                    processingOutcome = "Decryption Failed: " + e.getClass().getSimpleName();
                                    if ("image".equals(messageType)) processedContentForAdapter = "[Encrypted Image - Failed]";
                                    else if ("file".equals(messageType)) processedContentForAdapter = "[Encrypted File - Failed]";
                                    else processedContentForAdapter = "[Encrypted Message - Failed]"; // Default for text
                                } catch (Exception e) {
                                    Log.w(TAG, "Msg " + firebaseMessageId + ": Unexpected error during decryption.", e);
                                    processingOutcome = "Decryption Failed: Unexpected Error";
                                    if ("image".equals(messageType)) processedContentForAdapter = "[Encrypted Image - Failed]";
                                    else if ("file".equals(messageType)) processedContentForAdapter = "[Encrypted File - Failed]";
                                    else processedContentForAdapter = "[Encrypted Message - Failed]"; // Default for text
                                }
                            }

                        } catch (IllegalArgumentException e) { // Base64 decoding error for the *encrypted* data
                            Log.e(TAG, "Msg " + firebaseMessageId + ": Base64 decoding error for encrypted content.", e);
                            processingOutcome = "Base64 Decoding Error";
                            processedContentForAdapter = "[Invalid Encrypted Data]"; // Placeholder on Base64 error
                        } catch (Exception e) { // Any other unexpected decoding error
                            Log.e(TAG, "Msg " + firebaseMessageId + ": Unexpected error decoding Base64 for display.", e);
                            processingOutcome = "Decoding Error";
                            processedContentForAdapter = "[Invalid Encrypted Data]"; // Placeholder
                        }

                    } else {
                        // Keys are missing, cannot decrypt
                        processingOutcome = "Keys Unavailable";
                        Log.d(TAG, "Msg " + firebaseMessageId + " (Type: " + messageType + ", Effect: " + displayEffect + "): Not scheduled. Keys unavailable (PrivKey: " + isPrivateKeyAvailable + ", ConvKey: " + isConversationKeyAvailable + "). Showing [Locked].");
                        if ("image".equals(messageType)) processedContentForAdapter = "[Locked Image]";
                        else if ("file".equals(messageType)) processedContentForAdapter = "[Locked File]";
                        else processedContentForAdapter = "[Locked]"; // Placeholder indicating account needs unlocking or keys need loading
                    }

                }
                // --- Step 2: Fallback for unexpected cases ---
                else {
                    // Should theoretically not be reached if all types and conditions are covered.
                    // This might catch unexpected types or states. Display stored content as is.
                    Log.w(TAG, "Msg " + firebaseMessageId + " (Type: " + messageType + ", Scheduled: " + scheduledTime + ", Content Empty: " + TextUtils.isEmpty(storedMessageContentBase64) + ", Keys Available: " + (isPrivateKeyAvailable && isConversationKeyAvailable) + "): Reached unexpected state. Displaying stored content as is.");
                    processedContentForAdapter = storedMessageContentBase64;
                    processingOutcome = "Fallback State";
                    // Ensure processedContentForAdapter is not null/empty for safety, use generic placeholder if needed
                    if (TextUtils.isEmpty(processedContentForAdapter)) processedContentForAdapter = "[Unknown Message Type/State]";
                }


                // --- *** Step 2: Apply Invisible Ink Logic (ONLY FOR INCOMING TEXT MESSAGES) *** ---
                // After determining the base content (decrypted, raw, or error placeholder),
                // check if it needs to be HIDDEN due to Invisible Ink status.
                if (isIncoming && "text".equals(messageType) && "invisible_ink".equals(displayEffect) && !isRevealed) {
                    // This is an incoming, unrevealed Invisible Ink text message.
                    // We will OVERRIDE the content determined above and use a special marker
                    // to tell the adapter to show the "[Tap to Reveal]" placeholder instead.
                    processedContentForAdapter = "[INVISIBLE_INK_HIDDEN]"; // Use a unique marker string
                    processingOutcome += " -> Hidden (Invisible Ink)";
                    Log.d(TAG, "Msg " + firebaseMessageId + ": Applying Invisible Ink hiding effect.");
                }
                // For all other messages (outgoing, image, file, system, or already revealed Invisible Ink text),
                // we keep the processedContentForAdapter determined in Step 1.
                // --- *** END Invisible Ink Logic *** ---


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
                displayedMessageEntity.setDisplayEffect(storedMessageEntity.getDisplayEffect()); // *** Copy the original display effect! ***
                displayedMessageEntity.setRevealed(storedMessageEntity.isRevealed()); // *** Copy the original revealed state! ***
                displayedMessageEntity.setConversationId(storedConversationId);
                displayedMessageEntity.setDrawingSessionId(storedDrawingSessionId);
                displayedMessageEntity.setName(storedSenderName); // Copy the sender's display name
                // *** Set the processed content to display in the adapter ***
                // This will be the decrypted text, Base64 image, original plaintext,
                // a placeholder for crypto errors/locked state, or the special
                // "[INVISIBLE_INK_HIDDEN]" marker for unrevealed Invisible Ink text.
                displayedMessageEntity.setMessage(processedContentForAdapter);


                // Log the final content being added to the adapter list (maybe truncated)
                String logContent = (processedContentForAdapter != null && processedContentForAdapter.length() > 50) ? processedContentForAdapter.substring(0, 50) + "..." : processedContentForAdapter;
                Log.d(TAG, "Msg " + firebaseMessageId + ": Final processed content for adapter set. Outcome: " + processingOutcome + ". Displayed: '" + logContent + "'");


                messagesArrayList.add(displayedMessageEntity); // Add the processed message to the list for the adapter

            } // End of for loop
        } else {
            Log.d(TAG, "No messages found in LiveData for owner " + messageSenderID + " in conversation " + conversationId);
        }


        // Sort the list if necessary (should already be sorted by DAO query which orders by timestamp ASC)
        // Ensure sorting is consistent with the Room query's order (usually by timestamp)
        if (messagesArrayList.size() > 1) {
            // Using the timestamp from the MessageEntity which comes from Firebase/Local initial save
            Collections.sort(messagesArrayList, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
            // Log.d(TAG, "Messages list sorted by timestamp.");
        }


        // Update the adapter with the new list of processed messages
        messageAdapter.notifyDataSetChanged();
        Log.d(TAG, "Submitted " + messagesArrayList.size() + " processed messages to adapter in forceRefreshDisplay.");

        // Auto-scroll to bottom (Keep existing logic)
        if (!messagesArrayList.isEmpty() && messagesList != null && messagesList.getLayoutManager() != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) messagesList.getLayoutManager();
            if (layoutManager.getItemCount() > 0) {
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                // Auto-scroll if user is near the bottom or if it's a fresh load
                int scrollTolerance = 15; // Scroll if within this many items from the end (increased tolerance)
                boolean isAtBottom = lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition >= totalItemCount - 1;
                // Check if the user is currently within the scroll tolerance distance from the last item
                boolean isNearBottom = lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition >= totalItemCount - 1 - scrollTolerance;
                boolean isInitialLoad = totalItemCount > 0 && messagesArrayList.size() == totalItemCount && totalItemCount < 50; // Heuristic for initial small load

                // Log detailed scroll state
                // Log.d(TAG, "Scroll Check: Total items=" + totalItemCount + ", Last visible=" + lastVisibleItemPosition + ", Is at bottom=" + isAtBottom + ", Is near bottom ("+scrollTolerance+")=" + isNearBottom + ", Is Initial Load=" + isInitialLoad);


                if (isAtBottom || isNearBottom || isInitialLoad) {
                    // Scroll smoothly if possible, otherwise just scroll
                    messagesList.scrollToPosition(totalItemCount - 1);
                    Log.d(TAG, "Auto-scrolling to position " + (totalItemCount - 1));
                } else {
                    Log.d(TAG, "Skipping auto-scroll. User is not near bottom.");
                }
            } else {
                Log.d(TAG, "Message list is empty, no scrolling needed.");
            }
            // Else case (messagesList or LayoutManager null) handled at the beginning of the method
        } else if (messagesList != null && messagesList.getLayoutManager() != null && ((LinearLayoutManager) messagesList.getLayoutManager()).getItemCount() == 0) {
            Log.d(TAG, "Message list is empty after forceRefreshDisplay, no scrolling.");
        }


        // Mark visible messages as seen
        // This should happen AFTER the adapter is updated and potentially scrolled.
        // Adding a slight delay can help ensure layout finishes before checking visible items.
        // Only mark as seen if the activity is resumed (user is seeing the chat).
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            messagesList.postDelayed(this::markVisibleMessagesAsSeen, 100);
            Log.d(TAG, "Posted delayed task to mark visible messages as seen.");
        } else {
            Log.d(TAG, "Activity not resumed, skipping marking visible messages as seen.");
        }


        // --- Check and Update UI enabled state based on the *current* KeyManager state ---
        // UI should be enabled for sending if the user's private key is available AND the conversation key is loaded for this chat.
        // This check is done dynamically here regardless of the initial state.
        boolean isSecureChatCurrentlyAvailable = isPrivateKeyAvailable && isConversationKeyAvailable; // *** MODIFIED CHECK ***

        // Get the current state of the input UI (check one representative element)
        boolean isUiCurrentlyEnabled = messageInputText != null && messageInputText.isEnabled();

        if (isSecureChatCurrentlyAvailable && !isUiCurrentlyEnabled) {
            // Keys are now available, and UI was previously disabled. Enable UI.
            if (messageInputText != null) { messageInputText.setEnabled(true); messageInputText.setHint("Type The Messages..."); }
            if (sendMessageButton != null) sendMessageButton.setEnabled(true);
            if (attachmentButton != null) attachmentButton.setEnabled(true); // Enable attachment button
            Log.d(TAG, "forceRefreshDisplay enabled UI as keys are now available.");
            // No need for toast here, a toast about secure chat becoming available might happen elsewhere (e.g., after key generation/load)

        } else if (!isSecureChatCurrentlyAvailable && isUiCurrentlyEnabled) {
            // Keys are now unavailable, and UI was previously enabled. Disable UI.
            if (messageInputText != null) { messageInputText.setEnabled(false); messageInputText.setHint("Secure chat unavailable"); }
            if (sendMessageButton != null) sendMessageButton.setEnabled(false);
            if (attachmentButton != null) attachmentButton.setEnabled(false); // Disable attachment button
            Log.w(TAG, "forceRefreshDisplay disabled UI as keys are now unavailable.");
            // Show a toast indicating why UI is disabled
            if (!isPrivateKeyAvailable) {
                Toast.makeText(this, "Account not unlocked. Secure chat disabled.", Toast.LENGTH_SHORT).show();
            } else if (!isConversationKeyAvailable) {
                Toast.makeText(this, "Secure chat key missing. Secure chat disabled.", Toast.LENGTH_SHORT).show();
            } else {
                // Generic fallback toast (should be covered by above, but defensive)
                Toast.makeText(this, "Secure chat unavailable.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // UI state is already consistent with key state, no change needed.
            Log.d(TAG, "forceRefreshDisplay: UI state already consistent with key state (Secure Chat Available: " + isSecureChatCurrentlyAvailable + ").");
        }
        Log.d(TAG, "✅ forceRefreshDisplay finished.");
    }

    // --- Helper method to check if a string looks like Base64 data ---
// Useful for differentiating raw text from Base64 image data when checking long press options.
// This is a simple heuristic, not a perfect validator.



// ... (Keep the rest of your ChatPageActivity methods) ...




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




    private void processSelectedImageForSending(@Nullable Uri uri) {
        // --- Step 1: Clear any previously selected image state ---
        clearSelectedImage(); // Clear the previous state FIRST

        if (uri == null) {
            Log.w(TAG, "processSelectedImageForSending called with null URI after clearing previous state.");
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
            return;
        }

        Log.d(TAG, "Processing selected/captured image for sending. URI: " + uri);

        // Run image processing on a background thread
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap originalBitmap = null;
                if (inputStream != null) {
                    originalBitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                }

                if (originalBitmap == null) {
                    Log.e(TAG, "Failed to get bitmap from URI: " + uri);
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state if processing fails
                    return;
                }

                Log.d(TAG, "Original image dimensions: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

                // Resize Bitmap
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SEND_SIZE, MAX_IMAGE_SEND_SIZE);
                originalBitmap.recycle();

                if (resizedBitmap == null) {
                    Log.e(TAG, "Failed to resize bitmap.");
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state if processing fails
                    return;
                }
                Log.d(TAG, "Resized image dimensions for sending: " + resizedBitmap.getWidth() + "x" + resizedBitmap.getHeight());


                // Encode resized Bitmap to Base64 (This is the content to send)
                String base64Image = encodeToBase64(resizedBitmap);
                resizedBitmap.recycle();

                if (TextUtils.isEmpty(base64Image)) {
                    Log.e(TAG, "Encoded Base64 image content is empty!");
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to encode image data.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state if encoding fails
                    return;
                }

                // --- Image Processed Successfully ---
                imageToSendBase64 = base64Image; // Store the Base64 string

                // Store the original URI too if it was a camera file that needs cleanup later
                // Check if this URI is a temporary file URI managed by FileProvider/MediaStore
                boolean isTempFileUri = false;
                if (uri != null && uri.getAuthority() != null && uri.getAuthority().equals(getApplicationContext().getPackageName() + ".fileprovider")) { // Check FileProvider authority
                    isTempFileUri = true;
                } else if (uri != null && uri.getPath() != null && (uri.getPath().contains("/cache/") || uri.getPath().contains("/files/Pictures/"))) { // Heuristic check for common temp locations
                    // This check might be less reliable, consider using FileProvider exclusively for temp files
                }

                if (isTempFileUri) {
                    imageToSendUri = uri; // Keep track of the temp URI for cleanup
                    Log.d(TAG, "Keeping temporary URI for cleanup: " + imageToSendUri);
                } else {
                    // This URI is from gallery or unknown source, no need to track for cleanup
                    imageToSendUri = null;
                }


                Log.d(TAG, "Image processed and Base64 stored. Showing preview.");

                // --- Show the image preview and Cancel button (on Main Thread) ---
                runOnUiThread(() -> {
                    try {
                        // Use Glide to load the selected URI into the preview ImageView
                        if (imgPreview != null) {
                            Glide.with(ChatPageActivity.this)
                                    .load(uri) // Load from the original URI for better quality in preview
                                    .placeholder(R.drawable.image_placeholder_background) // Optional placeholder
                                    .into(imgPreview);
                        }


                        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.VISIBLE);
                        if (btnCancelImage != null) btnCancelImage.setVisibility(View.VISIBLE);

                        // Do NOT hide etMessage here. Keep it visible for text input.

                        Toast.makeText(ChatPageActivity.this, "Image ready to send.", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Log.e(TAG, "Error showing image preview on Main Thread", e);
                        runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error showing image preview.", Toast.LENGTH_SHORT).show());
                        clearSelectedImage(); // Clear state if preview fails
                    }
                });


            } catch (IOException e) {
                Log.e(TAG, "Image processing failed (IOException) for sending.", e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage();
            } catch (SecurityException e) { // Catch potential SecurityException on getContentResolver().openInputStream()
                Log.e(TAG, "Security Exception during image processing (missing permissions?) for sending.", e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Permission error accessing image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage();
            }
            catch (Exception e) { // Catch any other unexpected errors during processing
                Log.e(TAG, "Unexpected error during image processing for sending.", e);
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "An error occurred processing image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage();
            }
        }).start(); // Start the background thread
    }
// --- END Modified Helper Method ---
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



    // --- NEW Helper Method: Clear Selected Image ---
    private void clearSelectedImage() {
        Log.d(TAG, "Clearing selected image.");
        imageToSendBase64 = ""; // Clear the Base64 data
        // Clean up temporary camera file if it exists (logic already exists in your code)
        if (imageToSendUri != null) {
            try {
                // Ensure the URI is one we actually created and need to delete
                // Check against your FileProvider authority or other temp file indicators
                if (imageToSendUri.getAuthority() != null && imageToSendUri.getAuthority().equals(getApplicationContext().getPackageName() + ".fileprovider")) { // Replace "your_file_provider_authority"
                    getContentResolver().delete(imageToSendUri, null, null);
                    Log.d(TAG, "Cleaned up temporary camera file: " + imageToSendUri);
                } else {
                    // If it's not a temp camera URI we created, maybe it's a gallery URI, no deletion needed
                    Log.d(TAG, "Selected image URI is not a recognized temporary file URI. Skipping cleanup.");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to delete temporary camera file.", e);
            }
        }
        imageToSendUri = null; // Clear the URI reference

        // Hide the image preview and show text input
        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.GONE);
        if (btnCancelImage != null) btnCancelImage.setVisibility(View.GONE);
        if (imgPreview != null) imgPreview.setImageDrawable(null); // Clear the ImageView content

        // Make sure text input is visible again
        if (messageInputText != null) {
            messageInputText.setVisibility(View.VISIBLE); // Ensure text input is visible
            // Optionally clear text input here too if you want (e.g., if user changes mind after typing)
            // etMessage.setText("");
            // etMessage.setHint("Enter Message..."); // Restore hint
        }
    }
// --- END NEW Helper Method ---


    // --- NEW Method to Attempt Sending Message(s) ---
    // Inside ChatPageActivity class

    // --- NEW Method to Attempt Sending Message(s) ---
// Inside ChatPageActivity class

    // --- Modified Method to Attempt Sending Message(s) ---
    private void attemptSendMessage() {
        String messageText = messageInputText.getText().toString().trim();
        boolean isImageStaged = !TextUtils.isEmpty(imageToSendBase64);

        // Capture the current state of the flag before potentially resetting it
        boolean isInvisibleInkModeActive = isInvisibleInkSelected; // Renamed for clarity within this method

        // --- Basic Validation: Ensure *something* is available to send ---
        // If no text AND no image, don't send anything. Also, if Invisible Ink mode was on,
        // reset it because the user didn't send anything.
        if (TextUtils.isEmpty(messageText) && !isImageStaged) {
            Toast.makeText(this, "Please enter a message or select an image!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "attemptSendMessage: Attempted send with empty text and no staged image.");

            // Reset the flag if nothing was sent, even if the mode was active
            if (isInvisibleInkModeActive) {
                isInvisibleInkSelected = false; // Reset flag
                // Optional: Reset UI indicator if any (like send button icon or hint)
                // if (sendMessageButton != null) sendMessageButton.setImageResource(R.drawable.send_msg);
                // if (messageInputText != null) messageInputText.setHint("Type The Messages...");
                Log.d(TAG, "attemptSendMessage: Resetting Invisible Ink flag as nothing was sent.");
            }
            return; // Nothing to send
        }

        // --- Secure Chat Availability Check (Keep existing) ---
        // Secure chat is required for ANY message type we send because they are encrypted.
        boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
        if (!isSecureChatAvailable) {
            Log.w(TAG, "attemptSendMessage: Secure chat keys are not available. Blocking send.");
            // ... (log and show toast about secure chat unavailability) ...
            // Since keys are unavailable, reset Invisible Ink state too
            if (isInvisibleInkModeActive) {
                isInvisibleInkSelected = false; // Reset flag
                // Optional: Reset UI indicator if any
                // if (sendMessageButton != null) sendMessageButton.setImageResource(R.drawable.send_msg);
                // if (messageInputText != null) messageInputText.setHint("Type The Messages...");
            }
            // UI elements are already disabled by onResume/onCreate logic based on key availability
            return; // Do not proceed if keys are not available
        }
        // --- END Secure Chat Check ---


        // --- Send Text Message if available ---
        // Send text message with the Invisible Ink flag
        if (!TextUtils.isEmpty(messageText)) {
            Log.d(TAG, "attemptSendMessage: Attempting to send text message. Invisible Ink mode active: " + isInvisibleInkModeActive);
            SendTextMessage(messageText, isInvisibleInkModeActive); // Pass the flag to text sender
            // SendTextMessage will clear etMessage upon successful initiation.
        }


        // --- Send Image Message if available ---
        // Send image message with the Invisible Ink flag
        if (isImageStaged) {
            Log.d(TAG, "attemptSendMessage: Attempting to send staged image message. Invisible Ink mode active: " + isInvisibleInkModeActive);
            sendImageMessage(imageToSendBase64, isInvisibleInkModeActive); // *** MODIFIED: Pass the flag to image sender ***
            // sendImageMessage does NOT clear etMessage. It also doesn't clear the image preview; we do that below.
        }


        // --- Clean up UI and State after initiating send(s) ---
        // Clear the text input field only if text was sent (SendTextMessage handles this)
        // Clear the staged image regardless of whether text was also sent
        clearSelectedImage(); // This clears imageToSendBase64/Uri and hides the preview UI.

        // *** IMPORTANT: Reset the Invisible Ink state after attempting to send ***
        // This ensures the *next* message defaults back to normal unless Invisible Ink is selected again.
        isInvisibleInkSelected = false;
        // Optional: Reset UI indicator if any
        // if (sendMessageButton != null) sendMessageButton.setImageResource(R.drawable.send_msg); // Restore original icon
        // if (messageInputText != null) messageInputText.setHint("Type The Messages..."); // Restore original hint
        Log.d(TAG, "attemptSendMessage: Resetting Invisible Ink flag after send attempt.");
        // --- END Clean up ---

        Log.d(TAG, "attemptSendMessage: Send initiation complete.");
    }
// --- END Modified Method ---

    // --- NEW Method: Show Image Source Dialog for Attachments ---
    // --- NEW Method: Show Image Source Bottom Sheet Dialog for Attachments ---
// Inside your ChatPageActivity class

    private void showAttachmentOptionsBottomSheet() {
        Log.d(TAG, "Showing attachment options bottom sheet dialog.");

        // Secure Chat check *before* showing picker dialog (Keep this check)
        boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
        if (!isSecureChatAvailable) {
            Toast.makeText(this, "Secure chat is not enabled to send attachments.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Attachment picker blocked: Secure chat keys unavailable.");
            return;
        }

        // Clear any previously selected image before showing options
        clearSelectedImage(); // ADDED: Important to clear previous selection


        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        // Inflate the custom layout for the bottom sheet
        View bottomSheetView = LayoutInflater.from(this).inflate(
                R.layout.dialog_attachment_options, // Use your custom layout
                null
        );
        bottomSheetDialog.setContentView(bottomSheetView);

        // Find the option layouts in the custom view
        LinearLayout layoutGallery = bottomSheetView.findViewById(R.id.layout_gallery_option);
        LinearLayout layoutCamera = bottomSheetView.findViewById(R.id.layout_camera_option);
        LinearLayout layoutInvisibleInk = bottomSheetView.findViewById(R.id.layout_invisible_ink_option);
        // Find the NEW Shared Drawing option layout
        LinearLayout layoutSharedDrawing = bottomSheetView.findViewById(R.id.layout_shared_drawing_option); // <<< FIND THE NEW ID


        // Set Click Listeners for options
        layoutGallery.setOnClickListener(v -> {
            Log.d(TAG, "Gallery option clicked in bottom sheet.");
            bottomSheetDialog.dismiss(); // Dismiss the dialog
            openGalleryForAttachment(); // Call your existing method to open gallery
        });

        layoutCamera.setOnClickListener(v -> {
            Log.d(TAG, "Camera option clicked in bottom sheet.");
            bottomSheetDialog.dismiss(); // Dismiss the dialog
            openCameraForAttachment(); // Call your existing method to open camera (which includes permission check)
        });

        layoutInvisibleInk.setOnClickListener(v -> {
            Log.d(TAG, "Invisible Ink option clicked.");
            bottomSheetDialog.dismiss(); // Dismiss the dialog

            // *** Set the flag for the next message ***
            isInvisibleInkSelected = true;
            Log.d(TAG, "Invisible Ink mode activated for the next message.");

            // *** Optional: Give the user visual feedback ***
            runOnUiThread(() -> {
                Toast.makeText(ChatPageActivity.this, "Type your message and press Send. It will be sent as Invisible Ink.", Toast.LENGTH_SHORT).show();
                // You can also change the Send button's icon or change the hint text in the input field
            });

        });

        // *** ADD Click Listener for the NEW Shared Drawing option ***
        if (layoutSharedDrawing != null) { // Add null check for safety
            layoutSharedDrawing.setOnClickListener(v -> {
                Log.d(TAG, "Shared Drawing option clicked.");
                bottomSheetDialog.dismiss(); // Dismiss the dialog

                // *** STEP 1 COMPLETE: Placeholder call to the next step's method ***
                // This method will handle checking secure chat status again and initiating the drawing session flow
                confirmStartOneToOneDrawingSession(); // <<< CALL THE NEW METHOD HERE
                // *** END STEP 1 ***
            });
        } else {
            Log.w(TAG, "layout_shared_drawing_option not found in bottom sheet layout!");
            // Optionally hide the option in the layout if it's missing, or show a toast.
        }


        // Add listeners for other attachment types here later

        // Show the bottom sheet dialog
        bottomSheetDialog.show();
    }
    // --- END NEW Method ---
// Inside your ChatPageActivity class

    // --- NEW Helper Method: Contains the logic to open the Gallery intent for message attachment ---
// This method is called from the BottomSheetDialog when "Gallery" is chosen.
    private void openGalleryForAttachment() {
        Log.d(TAG, "Attempting to open gallery for attachment.");
        try {
            // No explicit storage permission needed for ACTION_PICK on modern APIs (>= API 29)
            // as long as you use MediaStore.Images.Media.EXTERNAL_CONTENT_URI.
            // For older APIs or specific storage needs, you might still need READ_EXTERNAL_STORAGE.
            // Your checkPermissionsAndOpenGallery (for wallpaper) handles the older API case.
            // For ACTION_PICK with MediaStore URI, Android handles permissions automatically for you
            // when providing the URI result.
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            // Optional but good practice: add flags for permission grant just in case
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Use the pre-registered launcher to start the activity
            pickImageLauncher.launch(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error launching gallery intent for attachment.", e);
            // Inform the user on the main thread
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error accessing gallery.", Toast.LENGTH_SHORT).show());
        }
    }
// --- END NEW Helper Method ---


    // --- NEW Helper Method: Contains the logic to open the Camera intent for message attachment ---
// This method is called from the BottomSheetDialog when "Camera" is chosen.
// It first performs a permission check using the launcher.
    private void openCameraForAttachment() {
        Log.d(TAG, "Attempting to open camera for attachment.");

        // Check if the CAMERA permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted. Proceeding to launch camera intent for attachment.");
            // Permission is already granted, proceed with launching the camera intent
            launchCameraIntentForAttachment(); // Call the method to launch the camera activity
        } else {
            Log.d(TAG, "Camera permission not granted. Requesting permission using launcher.");
            // Permission is not granted, request it using the launcher
            // The launcher's callback (in initializeImagePickers) will call launchCameraIntentForAttachment() if granted.
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
// --- END NEW Helper Method ---


    // --- NEW Helper Method: Contains the logic to prepare the URI and launch the camera app FOR ATTACHMENT ---
// This method should be called ONLY AFTER camera permission is confirmed
// (either instantly if already granted by openCameraForAttachment,
// or by the requestCameraPermissionLauncher callback).
    private void launchCameraIntentForAttachment() {
        Log.d(TAG, "Attempting to launch camera intent for attachment.");

        // Clear any leftover temporary URI from a previous failed attempt or cancellation
        if (imageToSendUri != null) {
            Log.w(TAG, "launchCameraIntentForAttachment found existing imageToSendUri. Cleaning up: " + imageToSendUri);
            try {
                // Use the same cleanup logic as in clearSelectedImage
                if (ContentResolver.SCHEME_CONTENT.equals(imageToSendUri.getScheme()) && imageToSendUri.getAuthority() != null && imageToSendUri.getAuthority().contains(getApplicationContext().getPackageName())) {
                    getContentResolver().delete(imageToSendUri, null, null);
                    Log.d(TAG, "Cleaned up previous temporary camera file.");
                } else {
                    Log.d(TAG, "Previous imageToSendUri is not a recognized temporary file URI. Skipping cleanup.");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to clean up previous temporary camera file.", e);
            } finally {
                imageToSendUri = null; // Always clear the reference
            }
        }


        try {
            // Create a new content:// URI in MediaStore for the picture output.
            // This is the modern way and generally preferred over FileProvider for photos
            // that you intend to save to the gallery area.
            // For APIs < 29, this might still require WRITE_EXTERNAL_STORAGE, but MediaStore
            // insert handles this complexity better than managing raw files with FileProvider.
            // Let's use a simple insert that works well on modern Android.
            ContentValues values = new ContentValues();
            // Optional: Add display name or other details
            // values.put(MediaStore.Images.Media.DISPLAY_NAME, "Attachment_" + System.currentTimeMillis() + ".jpg");
            // values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

            imageToSendUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);


            if (imageToSendUri != null) {
                Log.d(TAG, "Prepared temporary URI for camera attachment: " + imageToSendUri);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageToSendUri);

                // Grant write permission to the camera app for THIS specific URI
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Also grant read permission, though less critical for output URI
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


                // Verify that there's an app that can handle this intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    Log.d(TAG, "Launching camera intent with URI: " + imageToSendUri);
                    // Use the pre-registered launcher to start the activity.
                    // The TakePicture contract takes the output URI directly.
                    takePictureLauncher.launch(imageToSendUri);
                } else {
                    Log.w(TAG, "No camera app found to handle ACTION_IMAGE_CAPTURE intent.");
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "No camera app found.", Toast.LENGTH_SHORT).show());
                    // Clean up the URI if we couldn't launch
                    try {
                        if (imageToSendUri != null && ContentResolver.SCHEME_CONTENT.equals(imageToSendUri.getScheme())) {
                            getContentResolver().delete(imageToSendUri, null, null);
                            Log.d(TAG, "Cleaned up temporary URI after failed camera launch.");
                        }
                    } catch (Exception cleanupEx) { Log.w(TAG, "Failed to cleanup URI after camera launch failure", cleanupEx); }
                    imageToSendUri = null; // Clear the reference
                }

            } else {
                Log.e(TAG, "Failed to create temporary URI for camera attachment picture using MediaStore.");
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error preparing camera.", Toast.LENGTH_SHORT).show());
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception creating URI for camera picture (MediaStore?)", e);
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Permission error accessing storage for camera.", Toast.LENGTH_SHORT).show());
            imageToSendUri = null; // Ensure URI is null on failure
        }
        catch (Exception e) {
            Log.e(TAG, "Error creating URI for camera attachment picture or launching intent.", e);
            // This catch block handles potential exceptions during URI creation or intent launch
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error accessing camera.", Toast.LENGTH_SHORT).show());
            imageToSendUri = null; // Ensure URI is null on failure
        }
    }
// --- END NEW Helper Method ---



    // Inside ChatPageActivity.java

    // --- Implement the new OnMessageClickListener method ---
    @Override // This method is implemented from the MessageAdapter.OnMessageClickListener interface
    public void onMessageClicked(MessageEntity message) {
        Log.d(TAG, "Message clicked in Activity: " + message.getFirebaseMessageId() +
                ", Type: " + message.getType() +
                ", Effect: " + message.getDisplayEffect() +
                ", Revealed: " + message.isRevealed() +
                ", From: " + message.getFrom() +
                ", To: " + message.getTo());

        // Check if it's an INCOMING (To is me) message...
        // ... that is either a TEXT or IMAGE message... // *** MODIFIED CHECK ***
        // ... that has the "invisible_ink" display effect...
        // ... AND is currently NOT revealed.
        if (message != null &&                 // Check if message is not null
                message.getTo() != null && message.getTo().equals(messageSenderID) && // It's an incoming message for THIS user
                ("text".equals(message.getType()) || "image".equals(message.getType())) && // *** MODIFIED: Include image type ***
                "invisible_ink".equals(message.getDisplayEffect()) && // It's an Invisible Ink message
                !message.isRevealed())              // It's not yet revealed locally
        {
            // This is an incoming, unrevealed Invisible Ink message (either text or image)
            Log.d(TAG, "Incoming, unrevealed Invisible Ink message (Type: " + message.getType() + ") clicked. Attempting to reveal: " + message.getFirebaseMessageId());

            // --- Update the isRevealed status in Room DB on a background thread ---
            // The DAO method updateMessageRevealedStatus works for any message type,
            // as long as you provide the correct firebaseMessageId and ownerUserId.
            databaseWriteExecutor.execute(() -> {
                try {
                    // Call the DAO method to update the status
                    int updatedRows = messageDao.updateMessageRevealedStatus(
                            message.getFirebaseMessageId(),
                            messageSenderID, // The message copy in Room is owned by the current user
                            true // Set isRevealed to true
                    );

                    if (updatedRows > 0) {
                        Log.d(TAG, "Successfully updated message " + message.getFirebaseMessageId() + " to revealed=true in Room. Rows updated: " + updatedRows);
                        // The LiveData observer will detect this change and trigger forceRefreshDisplay,
                        // which will cause the adapter to re-bind the message and show the content (decrypted text or image).
                        Log.d(TAG, "Room update will trigger LiveData -> forceRefreshDisplay.");
                    } else {
                        Log.w(TAG, "Failed to update message " + message.getFirebaseMessageId() + " revealed status in Room. Message not found for owner " + messageSenderID + " or already revealed?");
                        // Optionally inform the user on the main thread
                        // runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to reveal message.", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating message revealed status in Room for message: " + message.getFirebaseMessageId(), e);
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error revealing message.", Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            Log.d(TAG, "Message clicked is not an unrevealed incoming Invisible Ink message for me. No action needed by reveal logic.");
            // --- Handle Clicks on Other Message Types ---
            // This block runs if the message is NOT an unrevealed incoming Invisible Ink message.
            // You can add logic here for other message types' tap actions if needed.
            // For instance, if you want clicking a normal text message to do something, add it here.
            // Clicks on image messages might already be handled by the OnTouchListener in the adapter,
            // but if not, you could handle opening fullscreen image here for revealed/normal images.

            // Example (Handle clicking a normal/revealed image):
            // if (message != null && "image".equals(message.getType()) && message.getMessage() != null && !message.getMessage().startsWith("[")) {
            //     Log.d(TAG, "Revealed image message clicked: " + message.getFirebaseMessageId());
            //     // Assuming message.getMessage() contains the Base64 for images after decryption
            //     Intent intent = new Intent(this, ChatImgFullScreenViewer.class);
            //     intent.putExtra("imageUrl", message.getMessage());
            //     startActivity(intent);
            // }
            // For now, we just log and do nothing for other clicks based on your previous implementation.
        }
    }

    // Inside ChatPageActivity.java class

    /**
     * Shows a confirmation dialog before starting a shared drawing session
     * in this one-to-one chat. Checks if Secure Chat is enabled.
     */
    private void confirmStartOneToOneDrawingSession() {
        Log.d(TAG, "Showing confirm start one-to-one drawing session dialog for conv: " + conversationId);

        // --- Critical Check: Ensure Secure Chat is Available for THIS conversation ---
        // Starting a drawing session in a secure chat requires the conversation key
        // to potentially encrypt drawing data or for presence/sync authentication.
        boolean isSecureChatAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId);
        if (!isSecureChatAvailable) {
            Log.w(TAG, "Cannot start shared drawing session: Secure chat is not fully available for conversation " + conversationId + " (Keys missing).");
            runOnUiThread(() -> {
                // Inform the user why drawing cannot be started
                if (!YourKeyManager.getInstance().isPrivateKeyAvailable()) {
                    Toast.makeText(ChatPageActivity.this, "Cannot start drawing: Your account is not unlocked.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ChatPageActivity.this, "Cannot start drawing: Secure chat key missing for this conversation. Try reopening chat or initiating key exchange.", Toast.LENGTH_LONG).show();
                }
            });
            // Exit the method as we cannot proceed without keys
            return;
        }
        // --- End Secure Chat Check ---


        // Ensure context is not null before showing dialog
        if (this == null) {
            Log.w(TAG, "Context is null, cannot show start drawing confirmation dialog.");
            return;
        }

        // Create and show the AlertDialog for confirmation
        new AlertDialog.Builder(this)
                .setTitle("Start Shared Drawing")
                .setMessage("Are you sure you want to start a new shared drawing session with " + messageReceiverName + "?") // Include partner's name
                .setPositiveButton("Yes, Start", (dialog, which) -> {
                    Log.d(TAG, "Confirmation received: Starting one-to-one drawing session.");
                    // Call the method that will handle creating the session and launching the activity (Implement this in Step 3)
                    startOneToOneDrawingSession(); // <<< Call the next step's method
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d(TAG, "Start drawing session cancelled by user.");
                    dialog.dismiss(); // Just dismiss the dialog
                })
                .setIcon(android.R.drawable.ic_dialog_info) // Optional: add an icon
                .show(); // Show the dialog
    }


    /**
     * Initiates the process of starting a new shared drawing session for this one-to-one chat.
     * This involves creating a Firebase node for the session, sending a chat message link,
     * and launching the drawing activity.
     * This method should be called ONLY AFTER confirmation and Secure Chat check.
     */
    private void startOneToOneDrawingSession() {
        Log.d(TAG, "Initiating startOneToOneDrawingSession for conv: " + conversationId);

        // Check for essential data (Firebase refs, user info, conversation ID)
        // rootRef, messageSenderID, messageReceiverID, conversationId, currentUserName
        // should be initialized in onCreate or GetUserInfo.
        if (rootRef == null || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messageSenderID) || TextUtils.isEmpty(messageReceiverID) || TextUtils.isEmpty(currentUserName)) {
            Log.e(TAG, "Cannot start one-to-one drawing session: Required dependencies are null/empty.");
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error starting drawing session. Missing setup info.", Toast.LENGTH_SHORT).show());
            // Consider disabling the menu option or button if this happens unexpectedly after initial checks.
            return;
        }

        // --- Step 1: Generate a unique ID for the new drawing session ---
        // We need a node to store strokes and session state for THIS specific session.
        // A good place might be under the conversation ID node itself.
        DatabaseReference drawingSessionsRef = rootRef.child("Conversations").child(conversationId).child("drawingSessions");

        // Generate a unique ID for the new session using push().getKey()
        String newSessionId = drawingSessionsRef.push().getKey();

        if (newSessionId == null) {
            Log.e(TAG, "Failed to generate unique session ID from Firebase for one-to-one session.");
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error starting drawing: Could not generate session ID.", Toast.LENGTH_SHORT).show());
            return; // Stop if ID generation fails
        }
        Log.d(TAG, "Generated new one-to-one drawing session ID: " + newSessionId + " for conv: " + conversationId);


        // --- Step 2: Prepare initial data for the new session node in Firebase ---
        // This node will store metadata about the session (who started, state)
        // and will be the parent node for strokes and active users.
        HashMap<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("starterId", messageSenderID); // Store who started it
        sessionInfo.put("createdAt", ServerValue.TIMESTAMP); // Store server timestamp
        sessionInfo.put("state", "active"); // Set initial state to active
        // For 1:1 chat, the participants are fixed and known.
        // You might store them explicitly, or implicitly rely on the conversationId parent node.
        // Let's store them for clarity, similar to group sessions:
        HashMap<String, Boolean> participantsMap = new HashMap<>();
        participantsMap.put(messageSenderID, true);
        participantsMap.put(messageReceiverID, true);
        sessionInfo.put("participants", participantsMap); // Store participants
        // Also store the conversation ID itself for easier lookup from the session node if needed
        sessionInfo.put("conversationId", conversationId); // Store the parent conversation ID


        // --- Step 3: Write the initial session info to Firebase ---
        drawingSessionsRef.child(newSessionId).setValue(sessionInfo)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New one-to-one drawing session node created in Firebase: " + newSessionId + " under conv: " + conversationId);

                    // --- MODIFIED: Call the new method to send the chat link message and launch the activity ---
                    sendOneToOneDrawingSessionLinkAndLaunchActivity(newSessionId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create new one-to-one drawing session node in Firebase for conv " + conversationId, e);
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to start drawing session.", Toast.LENGTH_SHORT).show());
                    // Optional: Clean up the partially created session node if the write failed
                    // drawingSessionsRef.child(newSessionId).removeValue(); // This might require another task/thread
                });
    }


// Inside ChatPageActivity.java class body { ... }

    /**
     * Sends the special chat message indicating a drawing session started
     * and launches the drawing activity.
     * Called after the Firebase session node (under Conversations/{convId}/drawingSessions/{sessionId})
     * is successfully created.
     */
    // Inside ChatPageActivity.java class body { ... }

    /**
     * Sends the special chat message indicating a drawing session started
     * and launches the drawing activity.
     * Called after the Firebase session node (under Conversations/{convId}/drawingSessions/{sessionId})
     * is successfully created by startOneToOneDrawingSession().
     *
     * This method handles:
     * 1. Generating a Firebase push ID for the chat message.
     * 2. Preparing the Firebase payload with message content, type, sender/receiver IDs, times, and session IDs/name.
     * 3. Creating a local MessageEntity for Room with all relevant data.
     * 4. Inserting the MessageEntity into Room DB (background thread).
     * 5. Sending the message payload to Firebase (background thread).
     * 6. On Firebase success:
     *    a. Updating chat summaries for both sender and receiver.
     *    b. Sending a push notification to the recipient with message/session data.
     *    c. Updating the message status in Room to 'sent'.
     *    d. Launching the drawing activity (main thread).
     * 7. On Firebase failure:
     *    a. Updating the message status in Room to 'failed'.
     *    b. Showing a failure toast (main thread).
     *
     * @param sessionId The unique ID generated for the drawing session in Firebase.
     *                  This ID is stored in the 'drawingSessionId' field of the chat message.
     */
    private void sendOneToOneDrawingSessionLinkAndLaunchActivity(String sessionId) {
        Log.d(TAG, "sendOneToOneDrawingSessionLinkAndLaunchActivity called for session: " + sessionId + " in conv: " + conversationId);

        // Check for essential data before proceeding
        // rootRef, messageSenderID, messageReceiverID, conversationId, currentUserName should be initialized in onCreate/GetUserInfo
        if (rootRef == null || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messageSenderID) || TextUtils.isEmpty(messageReceiverID) || TextUtils.isEmpty(currentUserName) || TextUtils.isEmpty(sessionId)) {
            Log.e(TAG, "Cannot send drawing session link: Required dependencies are null/empty. rootRef=" + (rootRef == null) + ", convId=" + TextUtils.isEmpty(conversationId) + ", senderId=" + TextUtils.isEmpty(messageSenderID) + ", receiverId=" + TextUtils.isEmpty(messageReceiverID) + ", userName=" + TextUtils.isEmpty(currentUserName) + ", sessionId=" + TextUtils.isEmpty(sessionId));
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error sending drawing link message. Missing info.", Toast.LENGTH_SHORT).show());
            return; // Stop execution if essential data is missing
        }

        // 1. Generate a unique push ID for the new chat message in Firebase
        // This message goes into the main chat message list for this conversation.
        // Use the conversationId to get the correct messages node.
        DatabaseReference messagesRef = rootRef.child("Messages").child(conversationId).push();
        String messagePushId = messagesRef.getKey(); // The unique ID for THIS message

        if (messagePushId == null) {
            Log.e(TAG, "Failed to generate unique message key from Firebase for drawing session link message.");
            runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error sending drawing link: Failed to generate message ID.", Toast.LENGTH_SHORT).show());
            return; // Stop if Firebase push ID generation fails
        }
        Log.d(TAG, "Generated Firebase push ID for drawing session link message: " + messagePushId);


        // Prepare the text preview for this message type (used in chat list and notification)
        String linkMessageText = currentUserName + " started a shared drawing session."; // Use current user's display name
        Log.d(TAG, "Drawing link message preview text: '" + linkMessageText + "'");

        // Get current local time for the sendTime string and immediate Room timestamp
        String sendTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        // Use Firebase ServerValue.TIMESTAMP for the actual timestamp stored in Firebase (for consistent sorting)
        Object firebaseTimestamp = ServerValue.TIMESTAMP;
        // Use local timestamp for immediate Room save and initial UI sorting until Firebase syncs the server timestamp
        long localTimestamp = System.currentTimeMillis();


        // 2. Create the Firebase payload map for the message
        Map<String, Object> messageFirebaseBody = new HashMap<>();
        messageFirebaseBody.put("message", linkMessageText); // Store the text preview in the 'message' field
        messageFirebaseBody.put("type", "one_to_one_drawing_session_link"); // <<< CRUCIAL: The NEW message type identifier
        messageFirebaseBody.put("from", messageSenderID); // Sender's UID
        messageFirebaseBody.put("to", messageReceiverID); // Receiver's UID
        messageFirebaseBody.put("sendTime", sendTime); // Use local formatted time string
        messageFirebaseBody.put("timestamp", firebaseTimestamp); // Use ServerValue.TIMESTAMP for Firebase
        messageFirebaseBody.put("seen", false); // Always send as not seen initially by recipient
        messageFirebaseBody.put("seenTime", ""); // Seen time is empty initially

        // --- Include conversationId, drawingSessionId, and name in the Firebase payload ---
        // These fields are essential for the recipient's MessageAdapter to display the link correctly and launch the activity.
        // They are also saved to Room when messages are received/synced.
        messageFirebaseBody.put("conversationId", conversationId); // Store the conversation ID
        messageFirebaseBody.put("drawingSessionId", sessionId); // Store the session ID (using the existing drawingSessionId field)
        messageFirebaseBody.put("name", currentUserName); // Store sender's display name (needed by adapter)
        // --- END Include IDs and name ---

        // Optional: Add initial readBy map if your message structure includes it
        // HashMap<String, Object> readByMap = new HashMap<>();
        // readByMap.put(messageSenderID, true); // Sender has read their own message immediately
        // messageFirebaseBody.put("readBy", readByMap);


        // 3. Create MessageEntity for Room (store necessary data locally, status pending initially)
        MessageEntity messageToSaveLocally = new MessageEntity();
        messageToSaveLocally.setFirebaseMessageId(messagePushId); // Use the generated Firebase ID as the primary key
        messageToSaveLocally.setOwnerUserId(messageSenderID); // The current user is the owner of this copy in Room
        messageToSaveLocally.setMessage(linkMessageText); // Store the text preview locally (not encrypted for this type)
        messageToSaveLocally.setType("one_to_one_drawing_session_link"); // Store the message type
        messageToSaveLocally.setFrom(messageSenderID); // Sender is the current user
        messageToSaveLocally.setTo(messageReceiverID); // Receiver is the chat partner
        messageToSaveLocally.setSendTime(sendTime); // Use local formatted time string
        messageToSaveLocally.setSeen(false); // Local seen status (will be updated by Firebase sync)
        messageToSaveLocally.setSeenTime("");
        messageToSaveLocally.setStatus("pending"); // Set status to pending initially (will be updated to 'sent' on Firebase success)
        messageToSaveLocally.setTimestamp(localTimestamp); // *** Use local timestamp for initial Room order ***
        messageToSaveLocally.setScheduledTime(null); // Not a scheduled message
        messageToSaveLocally.setDisplayEffect("none"); // No special display effect for this type
        messageToSaveLocally.setRevealed(true); // This message type is always visually "revealed"

        // --- Store conversationId, sessionId (drawingSessionId), and name in the MessageEntity for Room ---
        // These are needed by the Adapter when it displays this message type.
        messageToSaveLocally.setConversationId(conversationId); // Store the Conversation ID
        messageToSaveLocally.setDrawingSessionId(sessionId); // Store the Session ID in the drawingSessionId field
        messageToSaveLocally.setName(currentUserName); // Store the sender's display name
        // --- END Store IDs and name ---


        // 4. Insert the message into Room DB first with "pending" status (on background thread)
        // Using INSERT OR REPLACE strategy in the DAO handles both initial insert and potential updates.
        databaseWriteExecutor.execute(() -> { // Execute on the shared database writer executor
            try {
                messageDao.insertMessage(messageToSaveLocally);
                Log.d(TAG, "Inserted pending drawing session link into Room for owner " + messageSenderID + ": " + messagePushId + ", Local Timestamp: " + messageToSaveLocally.getTimestamp() + ", Session ID: " + sessionId + ", Conv ID: " + conversationId);
            } catch (Exception e) {
                Log.e(TAG, "Error inserting pending drawing session link into Room: " + messagePushId, e);
                // Show error toast on the main thread if local save fails
                runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Error saving drawing link locally.", Toast.LENGTH_SHORT).show());
                // Continue attempting to send to Firebase even if local save failed (design choice)
            }


            // 5. Send message data to Firebase (on background thread after Room insert initiated)
            // Use the messagePushId generated earlier to write to the correct location.
            messagesRef.setValue(messageFirebaseBody).addOnCompleteListener(task -> {
                Log.d(TAG, "Firebase setValue for drawing session link message " + messagePushId + " completed. Success: " + task.isSuccessful());

                if (task.isSuccessful()) {
                    // Firebase write was successful

                    // --- Update Chat Summaries for BOTH users ---
                    // Update the chat summary entries for both the sender and the receiver.
                    // The preview text is the linkMessageText.
                    // The timestamp used for sorting in the summary list should be a Long.
                    // Pass the localTimestamp here to avoid casting ServerValue.TIMESTAMP placeholder error.
                    String summaryPreview = linkMessageText; // Use the text preview created earlier

                    // Update sender's summary (last message is seen by sender immediately)
                    updateChatSummaryForUser(
                            messageSenderID, // Summary Owner 1 (Sender)
                            messageReceiverID, // Partner 1 (Receiver)
                            conversationId,
                            messagePushId,
                            summaryPreview, // Text preview for summary
                            "one_to_one_drawing_session_link", // Message Type in summary
                            localTimestamp, // <<< Pass localTimestamp here for summary sort
                            messageSenderID // isGroupMessage (false for 1:1 chat summaries)
                    );

                    // Update receiver's summary (last message is unseen by receiver initially, unread count increments)
                    updateChatSummaryForUser(
                            messageReceiverID, // Summary Owner 2 (Receiver)
                            messageSenderID, // Partner 2 (Sender)
                            conversationId,
                            messagePushId,
                            summaryPreview, // Text preview for summary
                            "one_to_one_drawing_session_link", // Message Type in summary
                            localTimestamp, // <<< Pass localTimestamp here for summary sort
                            messageSenderID // isGroupMessage (false for 1:1 chat summaries)
                    );
                    // --- END Update Chat Summaries ---


                    // *** Send Push Notification ***
                    // Send a push notification to the recipient (the other person in the 1:1 chat).
                    Log.d(TAG, "Firebase drawing session link message sent successfully. Calling sendPushNotification.");
                    String senderDisplayNameForNotification = (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : "Someone";
                    // The content of the notification in the shade
                    String notificationContent = senderDisplayNameForNotification + " started a shared drawing session."; // Use the preview text

                    // Call the sendPushNotification helper method with all parameters
                    // This method handles building the OneSignal payload and making the API call.
                    sendPushNotification(
                            messageReceiverID, // Recipient UID (the other person in the 1:1 chat)
                            "New Drawing Session", // Notification Title (Can be customized)
                            notificationContent, // Notification content (the preview text)
                            "one_to_one_drawing_session_link", // <<< Pass the message type to the notification handler
                            conversationId, // <<< Pass conversationId for notification data
                            sessionId, // <<< Pass sessionId for notification data
                            messagePushId // <<< Pass messageId (optional, but good practice for deep linking from notification)
                    );
                    // *** END Push Notification ***


                    // --- Update status in Room DB for the SENDER's copy ---
                    // Update the local message status in Room from 'pending' to 'sent'.
                    // This happens on the same background executor.
                    databaseWriteExecutor.execute(() -> {
                        // Find the message again in Room using its Firebase ID and Owner ID
                        MessageEntity sentMessage = messageDao.getMessageByFirebaseId(messagePushId, messageSenderID);
                        if (sentMessage != null) {
                            sentMessage.setStatus("sent"); // Update status to 'sent'
                            messageDao.insertMessage(sentMessage); // Use REPLACE strategy to update the existing entry
                            Log.d(TAG, "Updated status to 'sent' in Room for owner " + messageSenderID + " for drawing link message: " + messagePushId);
                        } else {
                            // This shouldn't happen if the initial insert succeeded, but log it if it does.
                            Log.e(TAG, "Sent drawing link message " + messagePushId + " not found in Room after Firebase success. Cannot update status.");
                        }
                    });


                    // --- 6. Launch the Drawing Activity (ON MAIN THREAD) ---
                    // Launch the drawing activity after the message has been successfully sent to Firebase.
                    Log.d(TAG, "Firebase write successful. Launching drawing activity on Main Thread.");
                    runOnUiThread(() -> { // Ensure UI operations run on the main thread
                        try {
                            // Create an Intent for your drawing activity class
                            Intent drawingIntent = new Intent(ChatPageActivity.this, YOUR_DRAWING_ACTIVITY_CLASS.class); // <<< Replace with your actual drawing activity class name
                            // Pass the necessary IDs as extras
                            drawingIntent.putExtra("conversationId", conversationId); // Pass the conversation ID
                            drawingIntent.putExtra("sessionId", sessionId);     // Pass the session ID
                            // Do NOT pass groupId for 1:1 sessions. The drawing activity will check for conversationId.

                            // Start the activity
                            startActivity(drawingIntent);

                            // Optional: Add an activity transition animation if you have one defined
                            // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                        } catch (Exception e) {
                            Log.e(TAG, "Error launching drawing activity after successful Firebase write.", e);
                            // Show an error toast if launching the activity fails
                            Toast.makeText(ChatPageActivity.this, "Error launching drawing activity.", Toast.LENGTH_SHORT).show();
                        }
                    });


                } else {
                    // Firebase write failed. Update status in Room to "failed".
                    Log.e(TAG, "Firebase setValue failed for drawing session link message " + messagePushId, task.getException());
                    // Update the local message status in Room to 'failed'.
                    databaseWriteExecutor.execute(() -> { // Execute on the background thread
                        // Find the message again in Room
                        MessageEntity failedMessage = messageDao.getMessageByFirebaseId(messagePushId, messageSenderID);
                        if (failedMessage != null) {
                            failedMessage.setStatus("failed"); // Update status to 'failed'
                            messageDao.insertMessage(failedMessage); // Use REPLACE strategy to update the existing entry
                            Log.d(TAG, "Updated status to 'failed' in Room for owner " + messageSenderID + " for drawing link message: " + messagePushId);
                        } else {
                            Log.e(TAG, "Failed drawing link message " + messagePushId + " not found in Room after Firebase failure. Cannot update status.");
                        }
                    });
                    // Show a failure toast on the main thread
                    runOnUiThread(() -> Toast.makeText(ChatPageActivity.this, "Failed to send drawing link message.", Toast.LENGTH_SHORT).show());
                }
            }); // End of Firebase setValue addOnCompleteListener
        }); // End of outer databaseWriteExecutor (Room insert and Firebase send)
    }
// --- END Full Modified Method sendOneToOneDrawingSessionLinkAndLaunchActivity ---

// ... rest of ChatPageActivity methods ...
    // ... (Keep the rest of your ChatPageActivity methods) ...

} // End of ChatPageActivity class
