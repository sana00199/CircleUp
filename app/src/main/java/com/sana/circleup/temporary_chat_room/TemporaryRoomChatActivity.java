package com.sana.circleup.temporary_chat_room;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer; // Import CountDownTimer
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// Firebase Imports
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue; // Import ServerValue
import com.google.firebase.database.ValueEventListener;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections; // Import Collections
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit; // Import TimeUnit

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

// Import Android specific features
import android.annotation.SuppressLint;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

// Firebase Imports
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

// Retrofit and Gson Imports for OneSignal (Assume these are available)
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.ResponseBody;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray; // Import JsonArray
import com.sana.circleup.Contacts;
import com.sana.circleup.GroupMessageAdapter;
import com.sana.circleup.Login;

import com.sana.circleup.R;
import com.sana.circleup.one_signal_notification.OneSignalApiService;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.GroupMessageDao;
import com.sana.circleup.room_db_implement.GroupMessageEntity;
import android.Manifest;



public class TemporaryRoomChatActivity extends AppCompatActivity {

    private static final String TAG = "TempRoomChatActivity"; // Updated TAG
    private static final int PICK_IMAGE_REQUEST = 1;

    // UI Elements
    private EditText userMessageInput;
    // --- NEW UI Elements for Image Preview ---
    private FrameLayout imagePreviewContainer; // Container for image preview
    private ImageView imgPreview; // ImageView to show the selected image thumbnail
    private ImageButton btnCancelImage; // Button to cancel image selection
    // --- End NEW UI Elements ---
    private ImageButton sendMessageButton, sendImgMsgButton;
    private RecyclerView messagesRecyclerView;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference tempRoomRef, messagesRef, usersRef; // Firebase DB References
    private String roomId, currentUserID; // Current Room ID and User ID
    private String currentUserName; // Current user's display name (Fetched async)
    // *** NEW MEMBER VARIABLE ***
    private String temporaryRoomName; // Store temporary room name for notifications and UI


    // --- Room DB & LiveData (NEW) ---
    private ChatDatabase db; // Room Database instance
    private GroupMessageDao groupMessageDao; // DAO for GroupMessageEntity (reused)
    // LiveData for temporary room messages from Room DB (using GroupMessageEntity, filtering by roomId)
    private LiveData<List<GroupMessageEntity>> temporaryRoomMessagesLiveData;
    private ExecutorService databaseExecutor; // Use the shared executor from ChatDatabase for Room ops
    // --- End NEW Room DB & LiveData ---

    // RecyclerView and Adapter
    // messagesList is NO LONGER directly manipulated by the Activity.
    // The adapter's internal list is updated by Room LiveData changes via setMessages.
    private GroupMessageAdapter messageAdapter; // Standard RecyclerView Adapter for Room data

    // Toolbar views
    private TextView roomNameToolbar, timerToolbar;
    private ImageView backButtonToolbar, roomIconToolbar;
    private View toolbarArea; // Reference to the clickable toolbar area (LinearLayout)

    // Timer
    private CountDownTimer countDownTimer;
    private long expiryTimeMillis = -1; // Initialize to -1 or some indicator that it hasn't been set

    // Firebase Listener for Sync (NEW)
    private ChildEventListener temporaryRoomMessagesChildEventListener; // Listener to sync messages from Firebase to Room

    // *** NEW MEMBER VARIABLES FOR NOTIFICATIONS AND TEMPORARY ROOM MEMBERS ***
    // Store the list of member UIDs for sending notifications (excluding the sender)
    private List<String> temporaryRoomMemberUids = new ArrayList<>();
    // Listener to keep the members list updated in real-time
    private ValueEventListener temporaryRoomMembersListener; // Firebase listener for temporary room members list



    // --- NEW members for Image Handling and Sending ---
    private Uri imageToSendUri; // URI of the image to be sent (used for camera temp file cleanup)
    private String imageToSendBase64 = ""; // Base64 string of the image data (ready to send)

    // Activity Result Launchers for Image Selection
    private ActivityResultLauncher<Intent> pickImageLauncher; // For Gallery
    private ActivityResultLauncher<Uri> takePictureLauncher; // For Camera

    // Permission Launcher for Camera
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    // Constants for image processing (Reuse from ChatPageActivity/GroupChatActivity)
    private static final int MAX_IMAGE_SEND_SIZE = 1024; // Max dimension (e.g., 1024x1024)
    private static final int IMAGE_SEND_COMPRESSION_QUALITY = 85; // JPEG compression quality (e.g., 85%)
    private static final int REQUEST_CODE_PERMISSION = 101; // Standard permission request code

    // *** Add or confirm you have the FileProvider authority string defined somewhere accessible ***
    // It must match the authority in your AndroidManifest.xml.
    // Assuming it's defined as getApplicationContext().getPackageName() + ".fileprovider".

    // --- End NEW members -

    // Retrofit Service for OneSignal API
    private OneSignalApiService oneSignalApiService;
    // Get this from your OneSignal Dashboard -> Settings -> Keys & IDs
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR ACTUAL APP ID
    private int requestCode;
    private int resultCode;
    @Nullable
    private Intent data;
    // *** END NEW MEMBER VARIABLES ***


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temporary_room_chat); // Ensure this layout exists

        Log.d(TAG, "🟢 TemporaryRoomChatActivity launched");

        // --- Get Room ID ---
        if (getIntent().hasExtra("roomId")) {
            roomId = getIntent().getStringExtra("roomId");
            if (TextUtils.isEmpty(roomId)) { // Added empty check
                Log.e(TAG, "Error: Room ID is empty from Intent!");
                Toast.makeText(this, "Error: Room ID missing!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Log.d(TAG, "Room ID received: " + roomId);
        } else {
            Log.e(TAG, "Error: Room ID missing from Intent!");
            Toast.makeText(this, "Error: Room ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Get Auth and User ID ---
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "No current user authenticated. Redirecting to Login.");
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity(); // Navigate to login and finish
            return;
        }
        currentUserID = currentUser.getUid();
        Log.d(TAG, "Current User ID: " + currentUserID);

        // --- Initialize Firebase References ---
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        tempRoomRef = FirebaseDatabase.getInstance().getReference().child("temporaryChatRooms").child(roomId);
        messagesRef = tempRoomRef.child("messages"); // Messages are under 'messages' child

        // --- Initialize Room DB and DAO (NEW) ---
        db = ChatDatabase.getInstance(getApplicationContext()); // Use application context
        groupMessageDao = db.groupMessageDao(); // Get the DAO (reused for temporary room messages)
        databaseExecutor = ChatDatabase.databaseWriteExecutor; // Use the shared DB executor
        Log.d(TAG, "Room DB and DAO initialized.");

        // *** NEW: Initialize Retrofit Service for OneSignal API ***
        // NOTE: Your OneSignalApiService MUST be defined and correctly configured
        // with the Base URL and the Authorization Header (containing your REST API Key).
        try {
            // Assuming OneSignalApiService base URL is correct and it handles the API Key header.
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://onesignal.com/") // OneSignal API Base URL (DO NOT CHANGE THIS)
                    .addConverterFactory(GsonConverterFactory.create()) // For JSON handling
                    // .addConverterFactory(ScalarsConverterFactory.create()) // If needed for plain text responses
                    .build();
            // Create an instance of your API service interface.
            // The API key should be set via an @Header or Interceptor in OneSignalApiService.java
            oneSignalApiService = retrofit.create(OneSignalApiService.class); // Assuming OneSignalApiService is accessible
            Log.d(TAG, "OneSignalApiService initialized.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignalApiService", e);
            // Handle this error - notifications for send might not work
            // Optionally disable sending buttons later in InitializeFields or show a persistent warning
            // Toast.makeText(this, "Error initializing notification service. Messages may not be notified.", Toast.LENGTH_SHORT).show(); // Avoid toast in onCreate if UI isn't ready
        }
        // *** END NEW ***

        // Initialize UI elements
        InitializeFields(); // <-- Call this after Firebase/Room init

        // *** NEW: Initialize Activity Result Launchers and Camera Permission Launcher ***
        initializeImagePickers(); // Call the new method to register launchers
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted. Proceeding to launch camera intent.");
                        launchCameraIntent(); // Call the method to launch camera intent
                    } else {
                        Log.w(TAG, "Camera permission denied. Cannot take photo.");
                        Toast.makeText(this, "Camera permission denied. Cannot take photo.", Toast.LENGTH_SHORT).show();
                    }
                });
        // *** END NEW Launcher Initialization ***



        // --- Set up UI Listeners ---
        if (toolbarArea != null) {
            toolbarArea.setOnClickListener(v -> {
                Log.d(TAG, "Toolbar Area Clicked!");
                showMembersDialog(); // Call method to show the members dialog
            });
        } else {
            Log.e(TAG, "toolbarArea view is NULL in onCreate! Check appbar_temproom_chat.xml include and ID.");
        }

        if (backButtonToolbar != null) backButtonToolbar.setOnClickListener(v -> { onBackPressed(); });
        if (sendMessageButton != null && userMessageInput != null) {
            sendMessageButton.setOnClickListener(v -> attemptSendMessage());
        } else {
            Log.w(TAG, "Send message button or input field is null.");
        }
        if (sendImgMsgButton != null) {
            sendImgMsgButton.setOnClickListener(v -> {
                Log.d(TAG, "Image pick button clicked.");
                // Check if message input is enabled (room is not expired/usable)
                if (userMessageInput != null && !userMessageInput.isEnabled()) {
                    Toast.makeText(this, "Cannot send messages in this room.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Image picker blocked: Message input UI is disabled.");
                    return; // Do not proceed if sending is disabled
                }
                clearSelectedImage(); // Clear any previously selected image first
                showImageSourceDialog(); // Show the dialog to choose source
            });
        } else {
            Log.w(TAG, "sendImgMsgButton is null, cannot set click listener.");
        }
        // --- End UI Listeners ---


        // --- Load Data ---
        // Fetch current user's info (needed for currentUserName for messages)
        GetUserInfo(); // Async

        // Load Room Info, Start Timer, and Check Membership
        loadRoomInfoAndCheckMembership(); // Async

        // Load messages from Room DB and set up Firebase sync listener (NEW Flow)
        LoadMessages(); // Setup LiveData and Firebase Listener


        // *** NEW: Attach Listener for Temporary Room Members on Start ***
        // We need an up-to-date list of members for sending notifications.
        // This listener is crucial for the notification logic.
        attachTemporaryRoomMembersListener(); // Called in onCreate and onStart
        // *** END NEW ***


        Log.d(TAG, "📲 onCreate finished in TemporaryRoomChatActivity");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "TemporaryRoomChatActivity onStart");
        // *** NEW: Attach Listener for Temporary Room Members on Start ***
        // Ensure the listener is attached if it was removed in onStop
        attachTemporaryRoomMembersListener();
        // *** END NEW ***
        // Messages sync listener attached in LoadMessages (in onCreate).
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "TemporaryRoomChatActivity onStop");
        // *** NEW: Remove Listener for Temporary Room Members on Stop ***
        // Stop listening for membership changes when the activity is not visible.
        removeTemporaryRoomMembersListener();
        // *** END NEW ***
        // Messages sync listener removed in onDestroy.
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TemporaryRoomChatActivity onDestroy called.");
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Cancel the timer
            Log.d(TAG, "Countdown timer cancelled.");
        }
        // --- Remove Firebase Listener ---
        removeTemporaryRoomMessagesSyncListener(); // Call the removal method

        // *** NEW: Remove Temporary Room Members Listener ***
        removeTemporaryRoomMembersListener();
        // *** END NEW ***

        // LiveData observer is automatically removed because we used 'this' lifecycle.

        // Room DB executor is managed by ChatDatabase singleton.
    }


    // Navigate to login activity
    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity.");
        Intent loginIntent = new Intent(TemporaryRoomChatActivity.this, Login.class); // Use your Login Activity class
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    // Initialize UI Elements
    private void InitializeFields() {
        Log.d(TAG, "Initializing UI elements.");
        // --- Input area ---
        sendMessageButton = findViewById(R.id.send_msg_button_temp);
        sendImgMsgButton = findViewById(R.id.send_imgmsg_btn_temp);
        userMessageInput = findViewById(R.id.input_temp_room_msg);

        // --- RecyclerView ---
        messagesRecyclerView = findViewById(R.id.temp_room_chat_recycler_view);
        // messagesList = new ArrayList<>(); // Remove direct list management

        // Initialize adapter with an empty list. Data will come from LiveData.
        // Use null for initialUserName, it will be updated by GetUserInfo.
        // Make sure your GroupMessageAdapter is configured to handle message types and sender IDs correctly.
        messageAdapter = new GroupMessageAdapter(new ArrayList<>(), this, currentUserID, null); // Pass empty list initially
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // Show latest messages at the bottom
        messagesRecyclerView.setLayoutManager(linearLayoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        // --- Toolbar views ---
        Toolbar toolbar = findViewById(R.id.temp_room_chat_bar_layout); // Match include ID
        if (toolbar == null) {
            Log.e(TAG, "CRITICAL ERROR: Toolbar not found (R.id.temp_room_chat_bar_layout)!");
            Toast.makeText(this, "Toolbar setup error.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        imgPreview = findViewById(R.id.imgPreview);
        btnCancelImage = findViewById(R.id.btnCancelImage);

        // Initially hide the image preview area and cancel button
        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.GONE);
        if (btnCancelImage != null) btnCancelImage.setVisibility(View.GONE);
        // *** END NEW ***

        // Get the clickable LinearLayout *INSIDE* the toolbar
        toolbarArea = toolbar.findViewById(R.id.temp_room_info_clickable_area);

        // Get views inside the clickable area
        roomNameToolbar = toolbar.findViewById(R.id.temp_room_name_toolbar);
        timerToolbar = toolbar.findViewById(R.id.temp_room_timer);
        roomIconToolbar = toolbar.findViewById(R.id.temp_room_icon_toolbar);

        // *** NEW: Set click listener for the cancel image button ***
        if (btnCancelImage != null) {
            btnCancelImage.setOnClickListener(v -> clearSelectedImage());
        } else {
            Log.w(TAG, "btnCancelImage is null, cannot set click listener.");
        }
        // *** END NEW ***

        Log.d(TAG, "UI elements initialized.");

        // Back button
        backButtonToolbar = toolbar.findViewById(R.id.back_button);
        Log.d(TAG, "UI elements initialized.");
    }


    // --- Fetch Current User's Info ---
    private void GetUserInfo() {
        if (usersRef == null || currentUserID == null || currentUserID.isEmpty()) { // Added empty check
            Log.e(TAG, "GetUserInfo: usersRef or currentUserID is null/empty.");
            currentUserName = "Unknown";
            if (messageAdapter != null) messageAdapter.updateCurrentUserName(currentUserName);
            return;
        }
        Log.d(TAG, "Fetching current user info for UID: " + currentUserID);
        usersRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    currentUserName = snapshot.child("username").getValue(String.class);
                    if (currentUserName == null || currentUserName.isEmpty()) {
                        currentUserName = "Unknown";
                        Log.w(TAG, "Username node found but value is null/empty for user: " + currentUserID);
                    }
                    Log.d(TAG, "Fetched current username: " + currentUserName);
                    // Only update the adapter's username, do NOT re-create the adapter
                    if (messageAdapter != null) {
                        messageAdapter.updateCurrentUserName(currentUserName);
                        // Re-bind visible items if username display logic matters for already displayed items
                        // messageAdapter.notifyDataSetChanged(); // Consider if needed, often not necessary
                    }

                } else {
                    currentUserName = "Unknown";
                    Log.w(TAG, "Username not found in Firebase for current user: " + currentUserID);
                    if (messageAdapter != null) messageAdapter.updateCurrentUserName(currentUserName);
                    Toast.makeText(TemporaryRoomChatActivity.this, "Your username not found, sending as Unknown.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error retrieving current user data from Firebase: " + error.getMessage());
                Toast.makeText(TemporaryRoomChatActivity.this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
                currentUserName = "Unknown";
                if (messageAdapter != null) messageAdapter.updateCurrentUserName(currentUserName);
            }
        });
    }


    // --- Load Room Info, Start Timer, and Check Membership ---
    private void loadRoomInfoAndCheckMembership() {
        if (tempRoomRef == null || currentUserID == null || currentUserID.isEmpty()) { // Added empty check
            Log.e(TAG, "tempRoomRef or UserId is NULL/empty during load/check.");
            Toast.makeText(this, "Internal error loading room.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Loading room info and checking membership for Room ID: " + roomId);

        tempRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.w(TAG, "Room data not found in Firebase for ID: " + roomId);
                    Toast.makeText(TemporaryRoomChatActivity.this, "Room does not exist", Toast.LENGTH_SHORT).show();
                    finish(); // Room doesn't exist, close activity
                    return;
                }

                // Check Membership FIRST
                boolean isMember = snapshot.child("members").hasChild(currentUserID);
                if (!isMember) {
                    Log.w(TAG, "User " + currentUserID + " is NOT a member of temporary room " + roomId + ". Finishing activity.");
                    Toast.makeText(TemporaryRoomChatActivity.this, "You are not a member of this temporary room", Toast.LENGTH_SHORT).show();
                    finish(); // Not a member, close activity
                    return;
                }
                Log.d(TAG, "User is a member. Proceeding with room info load.");

                // If member, then load room info and start timer
                String roomName = snapshot.child("roomName").getValue(String.class);
                Long expiryTime = snapshot.child("expiryTime").getValue(Long.class);

                // *** MODIFIED: Store roomName in member variable ***
                temporaryRoomName = (roomName != null) ? roomName : "Temporary Room"; // Store fetched or default name
                if (roomNameToolbar != null) {
                    roomNameToolbar.setText(temporaryRoomName); // Use the stored name
                } else {
                    Log.w(TAG, "roomNameToolbar is null, cannot set room name.");
                }
                // *** END MODIFIED ***


                if (expiryTime != null) {
                    expiryTimeMillis = expiryTime;
                    // Check client-side expiry immediately
                    if (System.currentTimeMillis() > expiryTimeMillis) {
                        Log.d(TAG, "Room expired client-side. Handling expiry.");
                        Toast.makeText(TemporaryRoomChatActivity.this, "This room has already expired.", Toast.LENGTH_LONG).show();
                        if (timerToolbar != null) timerToolbar.setText("Expired");
                        handleRoomExpiry(); // Handle expiry on UI
                        // Don't start timer, disable input
                        return; // Stop here if expired
                    }
                    Log.d(TAG, "Room expires at: " + expiryTimeMillis + ". Starting timer.");
                    startExpiryTimer(); // Start timer only if not expired
                } else {
                    Log.w(TAG, "Expiry time is null for room: " + roomId + ". Not starting timer.");
                    if (timerToolbar != null) timerToolbar.setText("Expiry time not set.");
                    // Do not disable input if expiry is not set, but log warning
                }

                // User is confirmed member and room is not client-side expired, proceeds to chat.
                // LoadMessages is already called in onCreate, LiveData observation and Firebase listener
                // should now start fetching messages for this room.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load room info or check membership: " + error.getMessage());
                Toast.makeText(TemporaryRoomChatActivity.this, "Failed to load room.", Toast.LENGTH_SHORT).show();
                finish(); // Close on error
            }
        });
    }


    // --- Start the countdown timer ---
    private void startExpiryTimer() {
        long timeRemaining = expiryTimeMillis - System.currentTimeMillis();

        if (timeRemaining <= 0) {
            if (timerToolbar != null) timerToolbar.setText("Expired");
            handleRoomExpiry();
            return;
        }

        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timerToolbar != null) {
                    String timeFormatted = formatMillisToTime(millisUntilFinished);
                    timerToolbar.setText("Expires in: " + timeFormatted);
                    // Update color based on remaining time
                    if (millisUntilFinished < TimeUnit.MINUTES.toMillis(5)) {
                        timerToolbar.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    } else if (millisUntilFinished < TimeUnit.HOURS.toMillis(1)) {
                        timerToolbar.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    } else {
                        // Reset to default color (e.g., darker_gray from Android resources)
                        // Make sure your color resource ID exists or use direct color.
                        // For simplicity, using a standard Android color resource.
                        timerToolbar.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }
                }
            }

            @Override
            public void onFinish() {
                if (timerToolbar != null) timerToolbar.setText("Expired");
                handleRoomExpiry();
            }
        }.start();
        Log.d(TAG, "Expiry timer started.");
    }

    // Helper method to format milliseconds into time string
    private String formatMillisToTime(long millis) {
        long totalSeconds = millis / 1000;
        long days = totalSeconds / (60 * 60 * 24);
        long hours = (totalSeconds % (60 * 60 * 24)) / (60 * 60);
        long minutes = (totalSeconds % (60 * 60)) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) {
            return String.format(Locale.getDefault(), "%d d %02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    // --- Handle what happens when the room expires on the client side ---
    private void handleRoomExpiry() {
        Log.d(TAG, "Handling room expiry.");
        Toast.makeText(this, "This room has expired.", Toast.LENGTH_LONG).show();
        // Disable input fields
        if (userMessageInput != null) {
            userMessageInput.setEnabled(false);
            userMessageInput.setHint("Room Expired");
            userMessageInput.setTextColor(getResources().getColor(android.R.color.darker_gray)); // Indicate disabled
        }
        if (sendMessageButton != null) sendMessageButton.setEnabled(false);
        if (sendImgMsgButton != null) sendImgMsgButton.setEnabled(false);

        // Optional: Remove Firebase listeners to stop processing new messages if desired
        removeTemporaryRoomMessagesSyncListener(); // Ensure this is called
        // *** NEW: Remove members listener as membership might change on expiry (though less likely for temp rooms) ***
        removeTemporaryRoomMembersListener();
        // *** END NEW ***
    }


    // --- MODIFIED: Send message info to Database (Text or Image) ---
    // Use this single method for both text and image messages.
    // Added nullable drawingSessionId parameter for consistency, though it will be null for temp rooms



    // --- MODIFIED: Load messages from Room and set up Firebase sync listener ---
    // This method sets up the data flow: Room LiveData -> Adapter + Firebase Sync Listener -> Room DB.
    // No change needed here regarding notifications; this is for UI display via Room.
    private void LoadMessages() {
        // Check if essential references and Room DB components are initialized
        if (roomId == null || roomId.isEmpty() || messagesRef == null || groupMessageDao == null || databaseExecutor == null || messagesRecyclerView == null || messageAdapter == null) {
            Log.e(TAG, "LoadMessages: Essential components are null/empty. Cannot load messages.");
            // Optionally show error state or finish
            Toast.makeText(this, "Error loading messages.", Toast.LENGTH_SHORT).show();
            // finish();
            return;
        }
        Log.d(TAG, "Setting up message loading and sync for temporary room: " + roomId);

        // --- Step 1: Load messages from Room DB using LiveData ---
        // Get LiveData for messages for this specific room (using the roomId as the groupId), ordered by timestamp.
        // The DAO query getMessagesForGroup(groupId) is reused.
        temporaryRoomMessagesLiveData = groupMessageDao.getMessagesForGroup(roomId); // Pass roomId as groupId

        // Observe the LiveData using the Activity's lifecycle.
        temporaryRoomMessagesLiveData.observe(this, new Observer<List<GroupMessageEntity>>() {
            @Override
            public void onChanged(List<GroupMessageEntity> messagesFromRoom) {
                // This callback runs on the main thread whenever Room data for this room changes.
                Log.d(TAG, "Temporary Room Messages LiveData updated with " + (messagesFromRoom != null ? messagesFromRoom.size() : 0) + " messages from Room for room: " + roomId);

                // Update the adapter's list with the new data from Room.
                // The list from Room is already sorted.
                messageAdapter.setMessages(messagesFromRoom); // Use the setMessages method

                // Scroll to the bottom when messages are loaded/updated, but only if the list is not empty.
                if (messagesFromRoom != null && !messagesFromRoom.isEmpty()) {
                    // Use post to ensure UI layout is updated before scrolling
                    messagesRecyclerView.post(() -> {
                        // Check if the user is near the bottom before auto-scrolling
                        LinearLayoutManager layoutManager = (LinearLayoutManager) messagesRecyclerView.getLayoutManager();
                        if (layoutManager != null) {
                            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                            int totalItemCount = layoutManager.getItemCount();

                            // Auto-scroll if it's the initial load or if the user is already near the bottom (e.g., last few items visible)
                            // Or if a new message was just added (totalItemCount changed)
                            // To make this check work, you need to store the previous item count in the adapter
                            // and compare it with the new totalItemCount here.
                            // For now, keeping a simpler scroll logic: scroll if list grows or if already near bottom.
                            if (totalItemCount > 0 && (lastVisibleItemPosition >= totalItemCount - 5 || lastVisibleItemPosition == -1)) { // Scroll if near end or first load
                                messagesRecyclerView.smoothScrollToPosition(totalItemCount - 1);
                            }
                            // If you want to only scroll on *new* messages AND user is near the bottom,
                            // you need a more sophisticated approach, perhaps tracking the count in the adapter
                            // or checking if the last item's ID is new.

                        } else {
                            // Fallback if layout manager is null
                            messagesRecyclerView.smoothScrollToPosition(messagesFromRoom.size() - 1);
                        }
                    });
                }
            }
        });
        // --- End Step 1 (LiveData Observation Setup) ---


        // --- Step 2: Attach Firebase ChildEventListener for Syncing ---
        // This listener watches the temporary room's messages node in Firebase for adds, changes, and removals.
        // It syncs these changes *into* the Room DB on a background thread.
        // Room LiveData then automatically updates the UI via the observer above.
        attachTemporaryRoomMessagesSyncListener(); // Call the method to attach the listener
        // --- End Step 2 (Firebase Sync Listener Setup) ---
    }


    // --- NEW Method to Attach Firebase ChildEventListener for Temporary Room Messages Sync ---
    // This listener syncs changes from Firebase messages node to Room DB for this specific temporary room.
    @SuppressLint("RestrictedApi") // Ok to use internal Log.d here
    private void attachTemporaryRoomMessagesSyncListener() {
        // Check if messagesRef and roomId are initialized
        if (messagesRef == null || roomId == null || roomId.isEmpty() || groupMessageDao == null || databaseExecutor == null || currentUserID == null || currentUserID.isEmpty()) { // Added checks
            Log.e(TAG, "Cannot attach temporary room messages sync listener, essential components are null/empty.");
            return;
        }
        // Check if the listener is already attached to prevent duplicate listeners
        if (temporaryRoomMessagesChildEventListener == null) {
            Log.d(TAG, "Attaching Firebase ChildEventListener for messages sync for temporary room: " + roomId);

            // Create the ChildEventListener implementation
            temporaryRoomMessagesChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // Called for each existing message when listener attaches, and for each new message added.
                    String messageId = snapshot.getKey(); // Firebase push key
                    if (TextUtils.isEmpty(messageId)) {
                        Log.w(TAG, "onChildAdded received null or empty message ID from Firebase for temporary room.");
                        return;
                    }
                    // Log.d(TAG, "💬 onChildAdded triggered for message ID: " + messageId + " in room: " + roomId); // Too verbose

                    // Save/Update the message in Room DB on a background thread.
                    // The LiveData observer will update the UI.
                    saveTemporaryRoomMessageToRoomFromSnapshot(snapshot); // Pass snapshot

                    // --- Mark the received message as read in Firebase (if it's not from the current user) ---
                    // Mark messages as read when they are added/synced to Room, *IF* the user is currently viewing the chat.
                    // This indicates the message has been "delivered" to this device and processed.
                    // This Firebase write should happen on a background thread.
                    String senderId = (String) snapshot.child("senderId").getValue(String.class);
                    if (senderId != null && !senderId.equals(currentUserID)) {
                        // Check if current user is already in the readBy map before writing (optimization)
                        DataSnapshot readBySnapshot = snapshot.child("readBy");
                        boolean isReadByCurrentUser = readBySnapshot.exists() &&
                                readBySnapshot.hasChild(currentUserID) &&
                                Boolean.TRUE.equals(readBySnapshot.child(currentUserID).getValue(Boolean.class));

                        if (!isReadByCurrentUser) { // If not already marked as read by current user
                            Log.d(TAG, "Attempting to mark temporary room message " + messageId + " as read for user " + currentUserID + " in Firebase (onChildAdded sync).");
                            // Use the snapshot.getRef() to get the message's reference and update
                            databaseExecutor.execute(() -> { // Run the Firebase write on a background thread
                                snapshot.getRef().child("readBy").child(currentUserID).setValue(true)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Marked message " + messageId + " as read for user " + currentUserID + " in Firebase successfully."))
                                        .addOnFailureListener(e -> Log.e(TAG, "Failed to mark temporary room message " + messageId + " as read in Firebase.", e));
                            });
                            // The onChildChanged event will eventually trigger for this message when Firebase updates.
                        }
                    }
                    // Notifications for incoming messages should be handled by the Firebase Messaging Service.
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // Called when a message's data changes (e.g., read status, timestamp resolves)
                    String messageId = snapshot.getKey();
                    if (TextUtils.isEmpty(messageId)) {
                        Log.w(TAG, "onChildChanged received null or empty message ID from Firebase for temporary room.");
                        return;
                    }
                    // Log.d(TAG, "🔄 onChildChanged triggered for message ID: " + messageId + " in room: " + roomId); // Too verbose

                    // Save/Update the message in Room DB on a background thread.
                    // insertOrUpdateMessage handles updates based on primary key.
                    saveTemporaryRoomMessageToRoomFromSnapshot(snapshot);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    // Called when a message is removed from Firebase
                    String messageId = snapshot.getKey();
                    if (TextUtils.isEmpty(messageId)) {
                        Log.w(TAG, "❌ onChildRemoved received null or empty message ID from Firebase for temporary room.");
                        return;
                    }
                    Log.d(TAG, "❌ onChildRemoved triggered for message ID: " + messageId + " in room: " + roomId + ". Removing from Room DB.");

                    // Remove the message from Room DB on a background thread
                    removeTemporaryRoomMessageFromRoom(messageId);
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // Not usually needed if sorting by timestamp in Room
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase Temporary Room Messages Listener cancelled for room " + roomId + ": " + error.getMessage(), error.toException());
                    Toast.makeText(TemporaryRoomChatActivity.this, "Failed to sync messages: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            };

            // Attach the listener
            messagesRef.addChildEventListener(temporaryRoomMessagesChildEventListener);
            Log.d(TAG, "Firebase messages sync listener attached to: " + messagesRef.getPath());
        }
    }

    // --- NEW Method to Remove Firebase ChildEventListener ---
    private void removeTemporaryRoomMessagesSyncListener() {
        if (messagesRef != null && temporaryRoomMessagesChildEventListener != null) {
            Log.d(TAG, "Removing Firebase ChildEventListener for temporary room messages sync for room: " + roomId);
            messagesRef.removeEventListener(temporaryRoomMessagesChildEventListener);
            temporaryRoomMessagesChildEventListener = null;
        } // else Log.d(TAG, "Firebase listener is null or messagesRef is null, nothing to remove."); // Too verbose
    }


    // --- NEW Method to Save/Update Temporary Room Message from Firebase Snapshot to Room DB ---
    // This method converts Firebase snapshot data into a Room Entity and saves/updates it.
    private void saveTemporaryRoomMessageToRoomFromSnapshot(@NonNull DataSnapshot snapshot) {
        if (groupMessageDao == null || databaseExecutor == null || roomId == null || roomId.isEmpty()) { // Added empty check for roomId
            Log.e(TAG, "Cannot save temporary room message to Room DB from snapshot: DAO, Executor, or roomId is null/empty.");
            return;
        }
        String firebaseMessageId = snapshot.getKey(); // Firebase push key
        if (TextUtils.isEmpty(firebaseMessageId)) {
            Log.w(TAG, "saveTemporaryRoomMessageToRoomFromSnapshot received snapshot with empty key.");
            return;
        }

        // Extract data safely from the snapshot
        String messageContent = (String) snapshot.child("message").getValue(String.class);
        String messageType = (String) snapshot.child("type").getValue(String.class);
        String senderName = (String) snapshot.child("name").getValue(String.class);
        String senderId = (String) snapshot.child("senderId").getValue(String.class);
        Long timestampLong = snapshot.hasChild("timestamp") ? snapshot.child("timestamp").getValue(Long.class) : 0L;
        if (timestampLong == null) timestampLong = 0L; // Ensure it's not null for the long type


        String date = (String) snapshot.child("date").getValue(String.class); // Client date (Optional)
        String time = (String) snapshot.child("time").getValue(String.class); // Client time (Optional)

        // Safely get and cast the readBy map
        Map<String, Boolean> readByMap = null;
        DataSnapshot readBySnapshot = snapshot.child("readBy");
        if (readBySnapshot.exists() && readBySnapshot.getValue() instanceof Map) {
            try {
                // This cast is safe if Firebase stores the map as Map<String, Boolean> or Map<String, Object>
                // and the values are actually Booleans. Iterate for safety as shown in GroupChatActivity.
                Map<String, Object> rawMap = (Map<String, Object>) readBySnapshot.getValue();
                readByMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                    if (entry.getValue() instanceof Boolean) {
                        readByMap.put(entry.getKey(), (Boolean) entry.getValue());
                    } else if (entry.getValue() != null) {
                        Log.w(TAG, "Expected Boolean value for readBy map entry " + entry.getKey() + ", but found " + entry.getValue().getClass().getName() + " for message " + firebaseMessageId);
                    }
                }

            } catch (ClassCastException e) {
                Log.e(TAG, "Failed to process 'readBy' map for message " + firebaseMessageId + " in saveTemporaryRoomMessageToRoomFromSnapshot.", e);
                readByMap = new HashMap<>(); // Default to empty on error
            }
        }
        if (readByMap == null) {
            readByMap = new HashMap<>();
        }

        // --- NEW: Define the drawingSessionId for this context ---
        // Since this is a temporary room message, it does NOT have a drawing session link.
        // So, we set it to null. This needs to match the GroupMessageEntity constructor.
        String drawingSessionId = null;


        // --- Create the GroupMessageEntity object using the constructor ---
        // Pass the extracted data AND the null drawingSessionId as the last argument.
        // Ensure your GroupMessageEntity constructor matches this order and types.
        GroupMessageEntity messageEntity = new GroupMessageEntity(
                firebaseMessageId, // Primary Key
                roomId, // *** Store roomId in the groupId field (Reusing field name for temporary rooms) ***
                messageContent,
                messageType,
                senderName,
                senderId,
                timestampLong, // Use resolved timestamp
                date,
                time,
                readByMap, // Pass the readBy map
                drawingSessionId // <<< NEW: Pass null for drawingSessionId for temporary messages
        );

        // Save/Update the message in Room DB on a background thread
        databaseExecutor.execute(() -> { // Use the shared database executor for Room ops
            try {
                // Insert or replace the message. This handles both new messages (insert)
                // and updates to existing ones (replace based on primary key messageId).
                groupMessageDao.insertOrUpdateMessage(messageEntity);
                // Log.d(TAG, "Temporary room message saved/updated in Room DB for message ID: " + firebaseMessageId + " (Room: " + roomId + ")."); // Too verbose
                // LiveData observer attached in LoadMessages will automatically pick this Room change up and update the UI
            } catch (Exception e) {
                Log.e(TAG, "Error saving/updating temporary room message " + firebaseMessageId + " in Room DB from snapshot", e);
            }
        });
    }
    // --- END NEW Method saveTemporaryRoomMessageToRoomFromSnapshot ---


    // --- NEW Method to Remove Temporary Room Message from Room DB ---
    // This method is called by the onChildRemoved event of the Firebase listener.
    private void removeTemporaryRoomMessageFromRoom(String messageId) {
        if (groupMessageDao == null || databaseExecutor == null) {
            Log.e(TAG, "Cannot remove temporary room message from Room DB: DAO or Executor is null.");
            return;
        }
        if (TextUtils.isEmpty(messageId)) {
            Log.w(TAG, "removeTemporaryRoomMessageFromRoom received empty messageId.");
            return;
        }

        // Execute the delete operation on the shared database executor (background thread)
        databaseExecutor.execute(() -> { // Use the shared database executor for Room ops
            try {
                // Delete the message from Room DB by its messageId (primary key)
                int deletedRows = groupMessageDao.deleteMessageById(messageId); // Use delete by messageId
                if (deletedRows > 0) {
                    Log.d(TAG, "Temporary room message removed from Room DB for message ID: " + messageId + ". Rows deleted: " + deletedRows);
                    // LiveData observer will pick this Room change up and update UI automatically
                } else {
                    // This is expected if the message didn't exist in Room for some reason (e.g., wasn't synced)
                    Log.w(TAG, "Attempted to remove temporary room message " + messageId + " from Room DB, but it was not found.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error removing temporary room message " + messageId + " from Room DB", e);
            }
        });
    }
    // --- END NEW Method to Remove Temporary Room Message from Room DB ---


    // --- Method to show the members dialog (Paste the code here) ---
    // Ensure this method is complete and uses the correct imports and view IDs.
    // This method is kept as provided, assuming it works.
    private void showMembersDialog() {
        // Ensure context is not null
        if (this == null) {
            Log.w(TAG, "Context is null, cannot show members dialog.");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_view_members, null); // Ensure this layout exists
        builder.setView(dialogView);

        TextView textRoomIdDisplay = dialogView.findViewById(R.id.textRoomIdDisplay); // Ensure this ID exists
        Button btnCopyRoomId = dialogView.findViewById(R.id.btnCopyRoomId); // Ensure this ID exists
        RecyclerView membersRecyclerView = dialogView.findViewById(R.id.recycler_view_room_members); // Ensure this ID exists
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_bar_members); // Ensure this ID exists

        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Contacts> memberList = new ArrayList<>();
        // Ensure MembersAdapterTemporaryChat exists and is imported
        MembersAdapterTemporaryChat adapter = new MembersAdapterTemporaryChat(this, memberList);
        membersRecyclerView.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        if (roomId != null) {
            textRoomIdDisplay.setText("Room ID: " + roomId);
            btnCopyRoomId.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Temporary Room ID", roomId);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Room ID copied!", Toast.LENGTH_SHORT).show();
            });
        } else {
            textRoomIdDisplay.setText("Room ID: N/A");
            btnCopyRoomId.setEnabled(false);
        }

        // Fetch members from Firebase
        if (tempRoomRef != null && usersRef != null) {
            tempRoomRef.child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> memberUids = new ArrayList<>();
                    for (DataSnapshot memberUidSnapshot : snapshot.getChildren()) {
                        String memberUid = memberUidSnapshot.getKey();
                        if (memberUid != null) {
                            memberUids.add(memberUid);
                        }
                    }

                    if (memberUids.isEmpty()) {
                        Log.d(TAG, "No members found in Firebase for room: " + roomId);
                        Toast.makeText(TemporaryRoomChatActivity.this, "No members found in this room.", Toast.LENGTH_SHORT).show();
                        // Show an empty list if no members
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        membersRecyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged(); // Update adapter to show empty list
                        return;
                    }

                    CountDownLatch latch = new CountDownLatch(memberUids.size());
                    if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                    membersRecyclerView.setVisibility(View.GONE);
                    memberList.clear(); // Clear list before adding

                    for (String uid : memberUids) {
                        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                Contacts memberContact = userSnapshot.getValue(Contacts.class);
                                if (memberContact != null) {
                                    memberContact.setUid(userSnapshot.getKey());
                                    memberList.add(memberContact);
                                } else {
                                    Log.w(TAG, "User data missing for UID: " + uid + " during member fetch.");
                                    // Add a placeholder for users whose data is missing
                                    Contacts unknownUser = new Contacts();
                                    unknownUser.setUid(uid);
                                    unknownUser.setUsername("Unknown User (" + uid.substring(0, Math.min(uid.length(), 4)) + "...)"); // Use Math.min for safety
                                    memberList.add(unknownUser);
                                }
                                latch.countDown();
                                if (latch.getCount() == 0) {
                                    // All user details fetched, sort and update adapter
                                    Collections.sort(memberList, (u1, u2) -> {
                                        String name1 = u1.getUsername() != null ? u1.getUsername() : "";
                                        String name2 = u2.getUsername() != null ? u2.getUsername() : "";
                                        return name1.compareToIgnoreCase(name2);
                                    });
                                    adapter.notifyDataSetChanged();
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    membersRecyclerView.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "All member details fetched. Showing dialog.");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Failed to fetch user details for UID " + uid + ": " + error.getMessage());
                                latch.countDown();
                                if (latch.getCount() == 0) {
                                    // Even on error, sort what we have and update
                                    Collections.sort(memberList, (u1, u2) -> {
                                        String name1 = u1.getUsername() != null ? u1.getUsername() : "";
                                        String name2 = u2.getUsername() != null ? u2.getUsername() : "";
                                        return name1.compareToIgnoreCase(name2);
                                    });
                                    adapter.notifyDataSetChanged();
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    membersRecyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to fetch member UIDs for room " + roomId + ": " + error.getMessage());
                    Toast.makeText(TemporaryRoomChatActivity.this, "Failed to load members.", Toast.LENGTH_SHORT).show();
                    // Show an error state in the dialog if needed, but don't dismiss
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    // membersRecyclerView remains hidden or show error view
                }
            });
        } else {
            Log.e(TAG, "Firebase references are null, cannot fetch members.");
            Toast.makeText(this, "Error loading members.", Toast.LENGTH_SHORT).show();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }
    }


    // --- Handle Image Pick Result ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Log.d(TAG, "Image selected from picker. URI: " + imageUri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                // You might want to resize/compress large images before encoding
                String encodedImage = encodeImageToBase64(bitmap); // Use helper method
                Log.d(TAG, "Image encoded to Base64. Sending...");
                SendMsgInfoToDatabase("image", encodedImage); // Use the unified send method
            } catch (IOException e) {
                Log.e(TAG, "Image processing failed during selection/encoding", e);
                Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during image processing/encoding", e);
                Toast.makeText(this, "An error occurred processing image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "ActivityResult for image picker cancelled or failed. RequestCode: " + requestCode + ", ResultCode: " + resultCode);
        }
    }

    // Helper method to encode Bitmap to Base64 string (Keep This Helper Method)
    // Ensure this matches the one in GroupChatActivity if you modified quality etc.




    // *** NEW Method to Attach ValueEventListener for Temporary Room Members Sync ***
    // This listener watches the temporary room's members list in Firebase.
    // It populates the temporaryRoomMemberUids list needed for sending notifications.
    @SuppressLint("RestrictedApi") // Ok to use internal Log.d here
    private void attachTemporaryRoomMembersListener() {
        // Check if tempRoomRef and roomId are initialized
        if (tempRoomRef == null || roomId == null || roomId.isEmpty()) { // Added empty check for roomId
            Log.e(TAG, "Cannot attach temporary room members listener, tempRoomRef or roomId is null/empty.");
            return;
        }
        // Check if the listener is already attached to prevent duplicate listeners
        if (temporaryRoomMembersListener == null) {
            Log.d(TAG, "Attaching Firebase ValueEventListener for temporary room members for room: " + tempRoomRef.child("members").getPath());

            temporaryRoomMembersListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // This callback runs on the main thread when members data changes
                    temporaryRoomMemberUids.clear(); // Clear the old list

                    if (snapshot.exists()) {
                        // Loop through the UIDs under the 'members' node
                        for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                            String memberId = memberSnapshot.getKey(); // Get the member's UID (the key)
                            if (!TextUtils.isEmpty(memberId)) {
                                temporaryRoomMemberUids.add(memberId); // Add UID to the list
                            }
                        }
                        Log.d(TAG, "Temporary room members list updated. Total members: " + temporaryRoomMemberUids.size());
                        // Log the members list (optional)
                        // Log.d(TAG, "Temporary room members UIDs: " + temporaryRoomMemberUids.toString());

                    } else {
                        // Members node does not exist or is empty (shouldn't happen for a valid room with at least the creator)
                        Log.w(TAG, "Temporary room members node not found or is empty for room: " + roomId);
                        // temporaryRoomMemberUids list is already cleared.
                        // If members node is unexpectedly empty, consider disabling sending messages?
                        // This might indicate a data inconsistency or room deletion.
                        handleRoomExpiry(); // Treat as if the room is unusable
                    }
                    // The updated temporaryRoomMemberUids list is now ready for use in SendMsgInfoToDatabase.
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase Temporary Room Members Listener cancelled for room " + roomId + ": " + error.getMessage(), error.toException());
                    // Handle error - maybe show a message to the user on the main thread
                    // Or disable sending messages if membership list is crucial
                    Toast.makeText(TemporaryRoomChatActivity.this, "Failed to get temporary room members list.", Toast.LENGTH_SHORT).show();
                    // Clear the list on error, as we don't have accurate member info
                    temporaryRoomMemberUids.clear();
                    // If members list fetch fails critically, perhaps disable message sending?
                    handleRoomExpiry(); // Treat as if the room is unusable
                }
            };

            // Attach the listener to the 'members' node within the temporary room reference
            tempRoomRef.child("members").addValueEventListener(temporaryRoomMembersListener);
            Log.d(TAG, "Firebase temporary room members listener attached to: " + tempRoomRef.child("members").getPath());
        }
    }
    // *** END NEW Method to Attach ValueEventListener for Temporary Room Members Sync ***

    // *** NEW Method to Remove ValueEventListener for Temporary Room Members ***
    // Remove the listener when the activity is stopped to prevent memory leaks and unnecessary Firebase background activity.
    private void removeTemporaryRoomMembersListener() {
        // Check if Firebase reference and listener are initialized
        if (tempRoomRef != null && temporaryRoomMembersListener != null) {
            Log.d(TAG, "Removing Firebase ValueEventListener for temporary room members for room: " + roomId);
            // Remove the listener from the Firebase reference
            tempRoomRef.child("members").removeEventListener(temporaryRoomMembersListener);
            temporaryRoomMembersListener = null; // Nullify the reference
        }
    }
    // *** END NEW Method to Remove ValueEventListener ***


    // *** NEW HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION TO TEMPORARY ROOM MEMBERS ***
    // This method is called by SendMsgInfoToDatabase
    // It filters out the sender and sends the notification to the remaining members.
    private void sendTemporaryRoomPushNotification(OneSignalApiService apiService,
                                                   List<String> allMemberUids, // List of all members in the temporary room (fetched by listener)
                                                   String senderUid, // UID of the user sending the message (current user)
                                                   String senderName, // Display name of the user sending the message
                                                   String roomName, // Name of the temporary room (fetched by loadRoomInfoAndCheckMembership)
                                                   String messageContentPreview, // Preview of the message content
                                                   String roomId, // Temporary Room ID (from Intent)
                                                   String messageId, // Firebase message ID (generated during send)
                                                   String messageType) { // Type of the message (text, image)

        // Check if API service is initialized and if there are members to send to
        if (apiService == null || allMemberUids == null || allMemberUids.isEmpty() || senderUid == null || senderUid.isEmpty() || roomId == null || roomId.isEmpty() || messageId == null || messageId.isEmpty()) {
            Log.e(TAG, "sendTemporaryRoomPushNotification: Essential parameters are null/empty. Cannot send notification.");
            return; // Cannot send notification
        }

        // Filter out the sender's UID from the list of recipients
        List<String> recipientUids = new ArrayList<>();
        for (String memberUid : allMemberUids) {
            // Add member UID to the recipient list if it's not the sender's UID and not empty
            if (!TextUtils.isEmpty(memberUid) && !memberUid.equals(senderUid)) {
                recipientUids.add(memberUid);
            }
        }

        // If after filtering, there are no recipients (e.g., room with only one member - the sender)
        if (recipientUids.isEmpty()) {
            Log.d(TAG, "sendTemporaryRoomPushNotification: No recipients after filtering sender (" + senderUid + "). Skipping notification for room " + roomId + " message " + messageId + ".");
            return;
        }

        Log.d(TAG, "Preparing OneSignal push notification for temporary room " + roomId + " to " + recipientUids.size() + " recipients.");

        // --- Build the JSON payload for OneSignal API ---
        JsonObject notificationBody = new JsonObject();

        // 1. Add App ID
        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID); // Use your OneSignal App ID (class member)

        // 2. Specify recipients using External User IDs (Firebase UIDs)
        // Pass the list of recipient UIDs filtered above
        JsonArray externalUserIdsArray = new JsonArray();
        for(String uid : recipientUids) {
            externalUserIdsArray.add(uid); // Add each recipient's Firebase UID
        }
        // OneSignal requires at least one ID in include_external_user_ids, which is ensured by recipientUids.isEmpty() check above.
        notificationBody.add("include_external_user_ids", externalUserIdsArray); // Use include_external_user_ids


        // 3. Add Notification Title and Content
        String finalRoomName = (roomName != null && !roomName.isEmpty()) ? roomName : "Temporary Room";
        String finalSenderName = (senderName != null && !senderName.isEmpty()) ? senderName : "A Member";
        String finalContentPreview = (messageContentPreview != null && !messageContentPreview.isEmpty()) ? messageContentPreview : "[Message]";

        String notificationTitle = "New Message in " + finalRoomName; // Title: "New Message in [Room Name]"
        String notificationContent = finalSenderName + ": " + finalContentPreview; // Content: "[Sender Name]: [Message Preview]"

        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationTitle))); // Title passed
        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationContent))); // Content passed

        // 4. Add custom data (important for handling notification click in the app)
        JsonObject data = new JsonObject();
        // Set a distinct event type for temporary room messages
        data.addProperty("eventType", "temporary_room_message"); // Distinct type
        data.addProperty("roomId", roomId); // Pass Room ID so recipient can open the chat directly
        data.addProperty("messageId", messageId); // Pass Message ID for deep linking (optional)
        data.addProperty("senderId", senderUid); // Pass Sender ID
        data.addProperty("messageType", messageType); // Pass message type (text, image)

        // No drawingSessionId data for temporary rooms in this implementation.

        notificationBody.add("data", data);

        // Optional: Set small icon (recommended)
        // Use the resource name of your app's small notification icon
        notificationBody.addProperty("small_icon", "app_icon_circleup"); // <<< Replace with your icon's resource name (string)


        // Optional: Customize notification appearance (sound, vibration, etc.)
        // Check OneSignal API docs for more options: https://documentation.onesignal.com/reference/create-notification


        // --- Make the API call asynchronously using Retrofit ---
        Log.d(TAG, "Making OneSignal API call for temporary room message notification...");
        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "OneSignal API call successful for temporary room message notification. Response Code: " + response.code());
                    // Log response body for debugging success/failure
                    try (ResponseBody responseBody = response.body()) {
                        String resBody = responseBody != null ? responseBody.string() : "N/A";
                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
                        // Look for "id" and "recipients" in the response body JSON for confirmation
                        // Example: {"id": "a62fddc6-5c02-4020-a7aa-2d022951bcf1", "recipients": 1} - The 'recipients' count confirms how many matched UIDs were found
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read success response body (temp room noti)", e);
                    }
                } else {
                    Log.e(TAG, "OneSignal API call failed for temporary room message notification. Response Code: " + response.code());
                    // Log error body for debugging failure reason
                    try (ResponseBody errorBody = response.errorBody()) {
                        String errBody = errorBody != null ? errorBody.string() : "N/A";
                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
                        // Common errors: 400 (Invalid JSON), 403 (Invalid REST API Key), 404 (App ID not found), Invalid External IDs
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read error response body (temp room noti)", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "OneSignal API call failed (network error) for temporary room message notification", t);
            }
        });
        Log.d(TAG, "OneSignal API call enqueued for temporary room message notification.");
    }
    // *** END NEW HELPER METHOD ***

    private void clearSelectedImage() {
        Log.d(TAG, "Clearing selected image.");

        // 1. Clear the Base64 data
        imageToSendBase64 = "";

        // 2. Clean up temporary camera file if it exists
        if (imageToSendUri != null) {
            try {
                // Ensure the URI is one we actually created and need to delete
                // Check against your FileProvider authority string.
                String fileProviderAuthority = getApplicationContext().getPackageName() + ".fileprovider"; // *** Use your actual authority ***
                if (imageToSendUri.getAuthority() != null && imageToSendUri.getAuthority().equals(fileProviderAuthority)) {
                    // It's a FileProvider URI from our app, safe to delete
                    getContentResolver().delete(imageToSendUri, null, null);
                    Log.d(TAG, "Cleaned up temporary camera file: " + imageToSendUri);
                } else {
                    // It's not a temp camera URI we created (e.g., it's from the gallery), no deletion needed
                    Log.d(TAG, "Selected image URI is not a recognized temporary file URI. Skipping cleanup.");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to delete temporary camera file.", e);
            }
        }

        // 3. Clear the URI reference
        imageToSendUri = null;

        // 4. Hide the image preview UI elements
        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.GONE);
        if (btnCancelImage != null) btnCancelImage.setVisibility(View.GONE);
        if (imgPreview != null) imgPreview.setImageDrawable(null); // Clear the ImageView content

        // The text input (userMessageInput) remains visible.
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null) return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // No resizing needed if already within limits
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        // Calculate the scale factor
        float scale = Math.min(((float) maxWidth / width), ((float) maxHeight / height));

        // Calculate new dimensions
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        // Create and return the scaled bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true); // true for filtering
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Compress the bitmap to JPEG format with the specified quality for sending
        bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_SEND_COMPRESSION_QUALITY, baos); // Use the constant

        byte[] imageBytes = baos.toByteArray(); // Get the byte array
        // Encode the byte array into a Base64 string using Base64.DEFAULT flags
        return Base64.encodeToString(imageBytes, Base64.DEFAULT); // Use android.util.Base64
    }

    private File createImageFile() throws IOException {
        // Create a unique image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "GROUP_JPEG_" + timeStamp + "_"; // Prefix with GROUP for clarity

        // Get the directory where you want to save the temporary file.
        // getExternalFilesDir(Environment.DIRECTORY_PICTURES) is a good choice for temporary images
        // related to pictures that are specific to your app. This is within your app's private storage
        // on external storage, which is typically accessible without WRITE_EXTERNAL_STORAGE permission on Q+.
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Check if the directory is available
        if (storageDir == null) {
            Log.e(TAG, "getExternalFilesDir returned null");
            throw new IOException("External files directory not available.");
        }

        // Ensure the directory exists, create it if it doesn't
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + storageDir.getAbsolutePath());
                throw new IOException("Failed to create directory for temporary files.");
            }
        }

        // Create the temporary file in the specified directory
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        Log.d(TAG, "Created temporary image file for camera: " + image.getAbsolutePath());
        return image; // Return the created File object
    }

    private void initializeImagePickers() {
        Log.d(TAG, "Initializing Activity Result Launchers.");

        // Launcher for picking an image from the gallery
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), // Contract for a generic activity result
                result -> { // Callback when gallery selection activity returns
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // User selected an image successfully
                        Uri selectedImageUri = result.getData().getData(); // Get the URI of the selected image
                        Log.d(TAG, "Image selected from gallery. URI: " + selectedImageUri);
                        processSelectedImageForSending(selectedImageUri); // Call method to process the selected image
                    } else {
                        // User cancelled or selection failed
                        Log.d(TAG, "Gallery image selection cancelled or failed. Result Code: " + result.getResultCode());
                        Toast.makeText(this, "Image selection cancelled.", Toast.LENGTH_SHORT).show();
                        clearSelectedImage(); // Clear state on cancel/failure
                    }
                });
        Log.d(TAG, "Gallery image picker launcher registered.");


        // Launcher for taking a picture with the camera
        // This contract requires a URI where the camera should save the picture.
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), // Contract specifically for taking a picture
                isSuccess -> { // Callback when camera activity returns (boolean indicates success)
                    if (isSuccess) {
                        // Picture was taken successfully and saved to the URI we provided (imageToSendUri)
                        Log.d(TAG, "Picture taken with camera. URI: " + imageToSendUri); // imageToSendUri was set before launching
                        processSelectedImageForSending(imageToSendUri); // Call method to process the captured image from the URI
                    } else {
                        // User cancelled or picture taking failed
                        Log.w(TAG, "Camera picture taking cancelled or failed.");
                        Toast.makeText(this, "Camera operation cancelled or failed.", Toast.LENGTH_SHORT).show();
                        // Clean up the temporary URI we created if it wasn't used
                        clearSelectedImage(); // This method handles cleaning up the temporary URI
                    }
                });
        Log.d(TAG, "Camera take picture launcher registered.");

        // Camera Permission Launcher initialized in onCreate above
    }

    private void openGallery() {
        Log.d(TAG, "Attempting to open gallery for sending.");

        // Check for READ_EXTERNAL_STORAGE permission if targeting older APIs (pre-Q)
        // On Android 10 (API 29) and above, scoped storage makes READ_EXTERNAL_STORAGE generally not needed
        // for accessing images via MediaStore.ACTION_PICK or ACTION_GET_CONTENT.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it.
            Log.d(TAG, "Storage permission not granted. Requesting for gallery.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            Toast.makeText(this, "Storage permission required to open gallery. Please try again after granting.", Toast.LENGTH_LONG).show();
            return; // Exit if permission is needed
        }

        // Permission is granted or not needed for this API level, proceed to launch gallery
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            // Launch the gallery picker using the registered launcher
            pickImageLauncher.launch(intent);
            Log.d(TAG, "Gallery intent launched via launcher.");

        } catch (Exception e) {
            // Log any error that occurs when trying to create or launch the intent
            Log.e(TAG, "Error launching gallery intent.", e);
            Toast.makeText(this, "Error accessing gallery.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {
        Log.d(TAG, "Attempting to open camera for sending.");

        // Check if the CAMERA permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted. Proceeding to launch camera intent.");
            // Permission is already granted, proceed with launching the camera intent
            launchCameraIntent(); // Call the helper method that uses takePictureLauncher
        } else {
            Log.d(TAG, "Camera permission not granted. Requesting permission.");
            // Permission is not granted, request it using the launcher
            // The result is handled in the requestCameraPermissionLauncher callback (which will call launchCameraIntent if granted)
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCameraIntent() {
        Log.d(TAG, "Preparing and launching camera intent.");
        try {
            // 1. Create a temporary File object where the camera should save the picture.
            File photoFile = null;
            try {
                photoFile = createImageFile(); // Use your helper to create temp file (ensure it exists in this class)
            } catch (IOException ex) {
                Log.e(TAG, "Error creating temp image file for camera", ex);
                runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Error preparing for photo.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Ensure state is clean
                return; // Exit if file creation fails
            }

            // 2. If the temporary file was created successfully, get its secure content:// URI using FileProvider.
            if (photoFile != null) {
                try {
                    // Get the secure content:// URI using FileProvider.
                    // Pass Context, your FileProvider authority string, and the File object.
                    String fileProviderAuthority = getApplicationContext().getPackageName() + ".fileprovider"; // *** Use your actual authority ***

                    Uri photoURI = FileProvider.getUriForFile(
                            this, // Context
                            fileProviderAuthority, // *** YOUR FILEPROVIDER AUTHORITY STRING HERE ***
                            photoFile // The java.io.File object
                    );

                    // 3. Store this URI in the member variable imageToSendUri.
                    imageToSendUri = photoURI;
                    Log.d(TAG, "Prepared temporary URI for camera using FileProvider: " + imageToSendUri);

                    // 4. Create the camera intent (ACTION_IMAGE_CAPTURE).
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // 5. Specify where the camera app should save the picture.
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageToSendUri);

                    // 6. Grant necessary permissions to the camera app.
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // 7. Verify that there is a camera app available.
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        Log.d(TAG, "Starting camera intent with URI: " + imageToSendUri);
                        // 8. Launch the camera activity using the registered launcher.
                        takePictureLauncher.launch(imageToSendUri);

                    } else {
                        Log.e(TAG, "No camera app found to handle ACTION_IMAGE_CAPTURE.");
                        runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "No camera app found.", Toast.LENGTH_SHORT).show());
                        try { if (photoFile.exists()) photoFile.delete(); } catch (Exception e) { Log.w(TAG, "Failed to clean up camera file on no app found", e); }
                        clearSelectedImage(); // Clear state on failure
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "FileProvider setup error or file issue: " + e.getMessage(), e);
                    runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Error accessing file storage. Check configuration.", Toast.LENGTH_LONG).show());
                    clearSelectedImage(); // Clear state on failure
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error setting up camera intent.", e);
                    runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Error accessing camera.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state on failure
                }
            } else {
                Log.e(TAG, "Photo file was null after creation attempt. Cannot launch camera.");
                runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Error preparing for photo.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on failure
            }
        } catch (Exception e) {
            // This catch block should theoretically not be reached if inner catches handle specific errors,
            // but can serve as a final safety net.
            Log.e(TAG, "Outer catch: Unexpected error in launchCameraIntent.", e);
            runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show());
            clearSelectedImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) { if (result != PackageManager.PERMISSION_GRANTED) { allPermissionsGranted = false; break; } }
            if (allPermissionsGranted) {
                Log.d(TAG, "Permissions granted.");
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Permissions denied.");
                Toast.makeText(this, "Permissions denied. Cannot access storage or camera.", Toast.LENGTH_SHORT).show();
            }
        }
        // Handle results for other permission request codes if you have them
    }

    private void attemptSendMessage() {
        // Get the current text input and check if an image is staged
        String messageText = userMessageInput.getText().toString().trim();
        boolean isImageStaged = !TextUtils.isEmpty(imageToSendBase64);

        // --- Basic Validation: Ensure *something* is available to send ---
        if (TextUtils.isEmpty(messageText) && !isImageStaged) {
            Toast.makeText(this, "Please enter a message or select an image!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Attempted send with empty text and no staged image. Skipping send.");
            return; // Nothing to send, exit method
        }

        // --- Check if message input is ENABLED (implies room is usable/not expired) ---
        // This check is done here defensively, as the button should ideally be disabled by handleRoomExpiry.
        if (userMessageInput != null && !userMessageInput.isEnabled()) {
            Toast.makeText(this, "Cannot send messages in this room.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Attempted send blocked: Message input UI is disabled.");
            return; // Do not proceed if sending is disabled
        }
        // --- End Check ---


        Log.d(TAG, "Attempting to send message(s). Text available: " + (!TextUtils.isEmpty(messageText)) + ", Image staged: " + isImageStaged);

        // --- Initiate Sending Process(es) ---

        // 1. Send Text Message if the input field has text
        if (!TextUtils.isEmpty(messageText)) {
            Log.d(TAG, "Initiating text message send.");
            // Call the existing method to send the text message.
            // Pass "text" as type, the trimmed text content. DrawingSessionId is always null for temp room messages.
            SendMsgInfoToDatabase("text", messageText); // Call with 2 arguments

            // Clear the text input field immediately after initiating the text send.
            // SendMsgInfoToDatabase will handle Firebase success/failure and Room sync.
            userMessageInput.setText("");
            // Keep the text input visible.
        }

        // 2. Send Image Message if an image is staged (Base64 content is available)
        if (isImageStaged) {
            Log.d(TAG, "Initiating image message send.");
            // Call the existing method to send the image message.
            // Pass "image" as type, the staged Base64 string as content. DrawingSessionId is null.
            SendMsgInfoToDatabase("image", imageToSendBase64); // Call with 2 arguments

            // Clear the staged image state and hide the preview immediately after initiating the image send.
            // We call clearSelectedImage() outside the if blocks below, as it applies whether text or image (or both) were sent.
        }

        // --- Clean up UI state after initiating send(s) ---
        // The text input is cleared above if text was sent.
        // Clear the staged image and hide the preview regardless of whether text was also sent.
        if (isImageStaged) { // Only clear image if an image was actually staged for sending
            clearSelectedImage();
            Log.d(TAG, "Staged image cleared after initiating send.");
        }

        // Toasts and status updates for individual messages (sent/failed) will come
        // from the completion listeners within SendMsgInfoToDatabase.
    }
    // --- END NEW Method attemptSendMessage ---


    // --- MODIFIED SendMsgInfoToDatabase method ---
    // This method should now accept ONLY 2 arguments: String messageType, String messageContent.
    // It will internally set drawingSessionId to null as temporary rooms don't have shared drawing.
    // It is called by attemptSendMessage().
    // Find your existing SendMsgInfoToDatabase and ensure it matches this signature:
    private void SendMsgInfoToDatabase(String messageType, String messageContent) {
        // ... (Existing validation for messageContent, expiry check) ...

        // Check if essential references and sender info are available
        if (messagesRef == null || tempRoomRef == null || currentUserID == null || currentUserID.isEmpty() || currentUserName == null || temporaryRoomName == null) { // Added checks for empty currentUserID and null temporaryRoomName
            Log.e(TAG, "SendMsgInfoToDatabase: messagesRef, tempRoomRef, currentUserID, currentUserName, or temporaryRoomName is null/empty. Cannot send message.");
            Toast.makeText(this, "Error sending message: Missing sender info or reference.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Preparing to send " + messageType + " message.");

        String messageKey = messagesRef.push().getKey();

        if (messageKey == null) {
            Log.e(TAG, "Failed to generate unique message key from Firebase.");
            Toast.makeText(this, "Error sending message: Failed to generate ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Generated Firebase push ID: " + messageKey);


        HashMap<String, Object> messageInfoMap = new HashMap<>();
        messageInfoMap.put("message", messageContent); // text or base64 string
        messageInfoMap.put("type", messageType);
        messageInfoMap.put("name", currentUserName); // Use fetched username
        messageInfoMap.put("senderId", currentUserID);
        messageInfoMap.put("timestamp", ServerValue.TIMESTAMP); // Use ServerValue.TIMESTAMP for server time

        // Add readBy node (sender has read it)
        Map<String, Object> readByMap = new HashMap<>();
        readByMap.put(currentUserID, true);
        messageInfoMap.put("readBy", readByMap);

        // Add client-side date and time (Optional, primarily for display history or if timestamp fails)
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()); // Use default locale
        String currentDate = dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Use default locale
        String currentTime = timeFormat.format(calendar.getTime());

        messageInfoMap.put("date", currentDate); // Client-side date string
        messageInfoMap.put("time", currentTime); // Client-side time string

        // *** Set drawingSessionId to null for temporary rooms ***
        // Temporary rooms do not support shared drawing sessions in this implementation.
        messageInfoMap.put("drawingSessionId", null); // <<< Set to null
        // *** END Set ***


        // Define the content to update the parent node's last message summary
        String lastMessageSummary;
        if ("image".equals(messageType)) {
            lastMessageSummary = "📸 Photo";
        } else if ("text".equals(messageType)) {
            lastMessageSummary = (messageContent.length() > 50) ? messageContent.substring(0, 50) + "..." : messageContent;
        } else if ("drawing_session".equals(messageType)) { // Although drawing_session won't be initiated by attemptSendMessage here, good to handle preview
            lastMessageSummary = "[Drawing Session Started]";
        }
        else {
            lastMessageSummary = "[Message]";
        }


        // Multi-path update to update message AND last message/timestamp on parent node
        Map<String, Object> updateParentMap = new HashMap<>();
        updateParentMap.put("/messages/" + messageKey, messageInfoMap);
        updateParentMap.put("lastMessageText", lastMessageSummary);
        updateParentMap.put("lastMessageTimestamp", ServerValue.TIMESTAMP);


        // Execute the multi-path update
        tempRoomRef.updateChildren(updateParentMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, messageType + " message sent successfully to Firebase and parent node updated with key: " + messageKey);
                // Clear input only if text was sent
                if ("text".equals(messageType) && userMessageInput != null) {
                    userMessageInput.setText("");
                }
                // Clear staged image state in attemptSendMessage after this method is called.

                // *** NEW: Send Push Notification to Recipients ***
                if (oneSignalApiService != null && currentUserID != null && !currentUserID.isEmpty()
                        && currentUserName != null && !currentUserName.isEmpty()
                        && temporaryRoomName != null && !temporaryRoomName.isEmpty()
                        && temporaryRoomMemberUids != null && !temporaryRoomMemberUids.isEmpty())
                {
                    Log.d(TAG, "Firebase message sent. Calling sendTemporaryRoomPushNotification.");
                    // The notification preview is the same as the summary preview
                    String notificationContentPreview = lastMessageSummary;

                    sendTemporaryRoomPushNotification(
                            oneSignalApiService,
                            temporaryRoomMemberUids, // List of all member UIDs
                            currentUserID, // Sender's UID
                            currentUserName, // Sender's display name
                            temporaryRoomName, // Temporary room name
                            notificationContentPreview, // Message preview for notification
                            roomId, // Room ID for custom data
                            messageKey, // Message ID for custom data
                            messageType // Message type for custom data
                            // No drawingSessionId for temp rooms
                    );
                } else {
                    Log.e(TAG, "OneSignalApiService, sender info, temporaryRoomName, or temporaryRoomMemberUids is null/empty. Cannot send temporary room push notification.");
                }
                // *** END NEW ***


            } else {
                Log.e(TAG, messageType + " message failed to send to Firebase.", task.getException());
                Toast.makeText(TemporaryRoomChatActivity.this, "Message failed to send: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                // Handle failure (e.g., mark as failed in Room if you add status field)
            }
        });
    }
    // --- END MODIFIED SendMsgInfoToDatabase ---

    private void showImageSourceDialog() {
        Log.d(TAG, "Showing image source dialog.");
        // Ensure context is not null before showing dialog
        if (this == null) {
            Log.w(TAG, "Context is null, cannot show image source dialog.");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Choose Image Source")
                .setItems(new CharSequence[]{"Gallery", "Camera"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Gallery option clicked
                            Log.d(TAG, "Gallery option selected.");
                            openGallery(); // Call method to open gallery (will use launcher)
                            break;
                        case 1: // Camera option clicked
                            Log.d(TAG, "Camera option selected.");
                            openCamera(); // Call method to open camera (will use launcher/permission check)
                            break;
                        default:
                            Log.w(TAG, "Unexpected image source selection: " + which);
                            break;
                    }
                })
                .setOnCancelListener(dialog -> Log.d(TAG, "Image source dialog cancelled.")) // Optional: Log dialog dismissal
                .show(); // Show the dialog
    }
// --- END NEW Method showImageSourceDialog ---

    private void processSelectedImageForSending(@Nullable Uri uri) {
        // --- Step 1: Clear any previously selected image state ---
        // This also handles cleanup of temporary camera files
        clearSelectedImage(); // Clear the previous state FIRST

        // Check if the URI is null after clearing
        if (uri == null) {
            Log.w(TAG, "processSelectedImageForSending called with null URI after clearing previous state.");
            // Inform the user on the UI thread
            runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
            return; // Exit if the URI is null
        }

        Log.d(TAG, "Processing selected/captured image for sending. URI: " + uri);

        // --- Step 2: Perform image processing on a background thread ---
        // Use a new Thread or an Executor (like the databaseExecutor if suitable, or create a dedicated one)
        // to avoid blocking the main UI thread. Using a simple new Thread here.
        new Thread(() -> {
            try {
                // a. Get Bitmap from URI: Use ContentResolver to open an InputStream and decode.
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap originalBitmap = null;
                if (inputStream != null) {
                    originalBitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close(); // Close the input stream
                }

                // Check if decoding was successful
                if (originalBitmap == null) {
                    Log.e(TAG, "Failed to get bitmap from URI: " + uri);
                    // Inform the user on the main thread
                    runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state if processing fails
                    return; // Exit if bitmap is null
                }

                Log.d(TAG, "Original image dimensions: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

                // b. Resize Bitmap: Resize the image to reduce size for sending.
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SEND_SIZE, MAX_IMAGE_SEND_SIZE); // Use MAX_IMAGE_SEND_SIZE constants
                originalBitmap.recycle(); // Recycle the original bitmap to free memory

                // Check if resizing was successful (shouldn't fail unless input was invalid)
                if (resizedBitmap == null) {
                    Log.e(TAG, "Failed to resize bitmap.");
                    runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state if processing fails
                    return; // Exit if resizing fails
                }
                Log.d(TAG, "Resized image dimensions for sending: " + resizedBitmap.getWidth() + "x" + resizedBitmap.getHeight());


                // c. Encode resized Bitmap to Base64 string.
                String base64Image = encodeImageToBase64(resizedBitmap); // Use your helper method with compression quality
                resizedBitmap.recycle(); // Recycle the resized bitmap

                // Check if encoding was successful
                if (TextUtils.isEmpty(base64Image)) {
                    Log.e(TAG, "Encoded Base64 image content is empty!");
                    runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Failed to encode image data.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state if encoding fails
                    return; // Exit if encoding fails
                }

                // --- Step 3: Image Processed Successfully ---
                Log.d(TAG, "Image processed and Base64 stored. Length: " + base64Image.length() + ". Ready for sending. Showing preview.");

                // Store the resulting Base64 string in the member variable.
                TemporaryRoomChatActivity.this.imageToSendBase64 = base64Image; // Use the stored Base64

                // Store the original URI too if it was a camera file that needs cleanup later
                // This check needs to match how you create temporary files for the camera.
                // Assuming you use FileProvider with your package name authority.
                boolean isTempFileUri = false;
                // Check if the URI authority matches your FileProvider authority
                String fileProviderAuthority = getApplicationContext().getPackageName() + ".fileprovider"; // *** Use your actual authority ***
                if (uri != null && uri.getAuthority() != null && uri.getAuthority().equals(fileProviderAuthority)) {
                    isTempFileUri = true;
                } else if (uri != null && uri.getPath() != null && (uri.getPath().contains("/cache/") || uri.getPath().contains("/files/Pictures/"))) {
                    // Heuristic check for common temporary file locations within app storage
                    // This might be less reliable than the FileProvider authority check
                }

                if (isTempFileUri) {
                    TemporaryRoomChatActivity.this.imageToSendUri = uri; // Keep track of the temp URI for cleanup
                    Log.d(TAG, "Keeping temporary URI for cleanup: " + imageToSendUri);
                } else {
                    // This URI is from gallery or another non-temporary source, no need to track for cleanup
                    TemporaryRoomChatActivity.this.imageToSendUri = null;
                }


                // --- Step 4: Update the UI to show the image preview (on Main Thread) ---
                runOnUiThread(() -> {
                    try {
                        // Use Glide to load the selected URI into the preview ImageView
                        // Load the original URI (not Base64) for better quality in the preview if possible
                        if (imgPreview != null) {
                            Glide.with(TemporaryRoomChatActivity.this) // Use Activity context
                                    .load(uri) // Load from the original URI
                                    .placeholder(R.drawable.image_placeholder_background) // Optional placeholder
                                    .error(R.drawable.ic_broken_image) // Optional error drawable
                                    .into(imgPreview);
                        }


                        // Make the image preview container and cancel button visible
                        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.VISIBLE);
                        if (btnCancelImage != null) btnCancelImage.setVisibility(View.VISIBLE);

                        // Do NOT hide the text input EditText. It remains visible.

                        // Inform the user that the image is ready to be sent
                        Toast.makeText(TemporaryRoomChatActivity.this, "Image ready to send.", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Log.e(TAG, "Error showing image preview on Main Thread", e);
                        // If showing preview fails, treat as processing failure and clear state
                        runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Error showing image preview.", Toast.LENGTH_SHORT).show());
                        clearSelectedImage(); // Clear state if preview fails
                    }
                });


            } catch (IOException e) {
                // Handle file reading errors
                Log.e(TAG, "Image processing failed (IOException) for sending.", e);
                runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on processing failure
            } catch (SecurityException e) {
                // Handle permission errors when accessing the URI
                Log.e(TAG, "Security Exception during image processing (missing permissions?).", e);
                runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "Permission error accessing image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on processing failure
            }
            catch (Exception e) {
                // Catch any other unexpected errors during processing (decoding, etc.)
                Log.e(TAG, "Unexpected error during image processing for sending.", e);
                runOnUiThread(() -> Toast.makeText(TemporaryRoomChatActivity.this, "An error occurred processing image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on processing failure
            }
        }).start(); // Start the background thread
    }






}