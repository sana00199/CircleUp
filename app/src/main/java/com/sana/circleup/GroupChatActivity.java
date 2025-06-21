package com.sana.circleup;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sana.circleup.drawingboard_chatgroup.YOUR_DRAWING_ACTIVITY_CLASS;
import com.sana.circleup.one_signal_notification.OneSignalApiService;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.GroupListDao;
import com.sana.circleup.room_db_implement.GroupMessageDao;
import com.sana.circleup.room_db_implement.GroupMessageEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.Manifest;



public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity"; // Define TAG
    private static final int IMAGE_PICK_REQUEST = 1; // Constant for image picker request code

    // UI Elements
    private Toolbar toolbar; // Toolbar
    private ImageButton sendMessageButton, send_imgmsg_btn; // Input buttons
    private EditText userMessageInput; // Message input field
    private RecyclerView messagesRecyclerView; // RecyclerView for messages
    private ImageView groupMenu, groupImageView; // Toolbar views
    private TextView groupNameTextView; // TextView for group name in toolbar

    // Firebase
    private FirebaseAuth auth; // Firebase Auth
    private DatabaseReference rootRef; // Root Firebase DB Reference
    private DatabaseReference usersRef; // Reference to /Users node (Used for fetching sender profiles in adapter)
    private DatabaseReference groupRef; // Reference to the specific group node
    private DatabaseReference messagesRef; // Reference to the messages node within the group

    // User & Group Info
    private String groupId; // ID of the current group (from Intent)
    private String currentUserID; // Current logged-in user's UID
    private String currentUserName; // Current user's display name (Fetched async)

    // --- Room DB and DAO members ---
    private ChatDatabase db; // Room Database instance
    private GroupMessageDao groupMessageDao; // DAO for GroupMessageEntity
    private LiveData<List<GroupMessageEntity>> groupMessagesLiveData; // LiveData for group messages from Room DB
    private ExecutorService databaseExecutor; // Use the shared executor from ChatDatabase for Room ops
    // --- End Room DB and DAO ---

    private GroupListDao groupListDao; // DAO for GroupEntity
    // --- End NEW ---

    // RecyclerView and Adapter
    // messagesList is now the list held by the standard adapter, populated by LiveData from Room
    // This list holds the data currently displayed in the RecyclerView.
    // private final List<GroupMessageEntity> messagesList = new ArrayList<>(); // Removed this - adapter holds its own list
    private GroupMessageAdapter messageAdapter; // Standard RecyclerView Adapter for Room data


    // *** NEW MEMBER VARIABLES FOR NOTIFICATIONS ***
    // Store the list of member UIDs for sending notifications
    private List<String> groupMemberUids = new ArrayList<>();
    // Listener to keep the members list updated
    private ValueEventListener groupMembersListener;
    // Store the group name for notifications (already fetched in loadGroupInfo)
    private String groupName; // Already declared above, just ensuring purpose is clear

    // Retrofit Service for OneSignal API
    private OneSignalApiService oneSignalApiService;
    // Get this from your OneSignal Dashboard -> Settings -> Keys & IDs
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR ACTUAL APP ID
    // *** END NEW MEMBER VARIABLES FOR NOTIFICATIONS ***
// --- NEW members for Image Preview and Sending ---
    private FrameLayout imagePreviewContainer; // Container for image preview
    private ImageView imgPreview; // ImageView to show the selected image thumbnail
    private ImageButton btnCancelImage; // Button to cancel image selection

    private Uri imageToSendUri; // URI of the image to be sent (used for camera temp file cleanup)
    private String imageToSendBase64 = ""; // Base64 string of the image data (ready to send)

    // Activity Result Launchers for Image Selection
    private ActivityResultLauncher<Intent> pickImageLauncher; // For Gallery
    private ActivityResultLauncher<Uri> takePictureLauncher; // For Camera

    // Permission Launcher for Camera
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    // Constants for image processing (Reuse from ChatPageActivity)
    private static final int MAX_IMAGE_SEND_SIZE = 1024; // Max dimension (e.g., 1024x1024)
    private static final int IMAGE_SEND_COMPRESSION_QUALITY = 85; // JPEG compression quality (e.g., 85%)
    private static final int REQUEST_CODE_PERMISSION = 101; // Standard permission request code

    // Firebase Listener for Sync (NEW)
    private ChildEventListener groupMessagesChildEventListener; // Listener to sync messages from Firebase to Room


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assuming your layout is activity_group_chat and includes appbar_groupmain.xml
        setContentView(R.layout.activity_group_chat);

        Log.d(TAG, "🟢 GroupChatActivity launched");

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Check if user is authenticated. Redirect to login if not.
        if (currentUser == null) {
            Log.w(TAG, "No current user authenticated. Redirecting to Login.");
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity(); // Navigate to login and finish
            return; // Stop further execution
        }

        currentUserID = currentUser.getUid(); // Get current user ID
        rootRef = FirebaseDatabase.getInstance().getReference(); // Initialize rootRef

        // Initialize users reference (for fetching current user's name and receiver profile images in adapter)
        usersRef = rootRef.child("Users"); // Reference to the base /Users node


        // Get group ID from Intent. Group ID is essential.
        groupId = getIntent().getStringExtra("groupId");
        if (TextUtils.isEmpty(groupId)) { // Added check for empty groupId
            Log.e(TAG, "Error: Group ID missing or empty from Intent!");
            Toast.makeText(this, "Error: Group ID missing!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return; // Stop further execution
        }

        // Initialize group and messages references using the obtained groupId
        groupRef = rootRef.child("Groups").child(groupId); // Reference to the specific group node in Firebase
        messagesRef = groupRef.child("Messages"); // Reference to the messages node within the group in Firebase


        // Initialize Room DB and DAO (NEW)
        db = ChatDatabase.getInstance(getApplicationContext()); // Use application context for database
        groupMessageDao = db.groupMessageDao(); // Get the DAO for group messages
        databaseExecutor = ChatDatabase.databaseWriteExecutor; // Use the shared DB executor for Room ops
        groupListDao = db.groupListDao();

        // Initialize UI elements (Keep This, Ensure IDs Match Layout)
        InitializeFields();

        initializeImagePickers();

        // Initialize Camera Permission Launcher (NEW)
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted. Proceeding to launch camera intent.");
                        // Permission granted, now proceed to launch the camera
                        launchCameraIntent(); // Call the method to launch the camera intent using the launcher
                    } else {
                        Log.w(TAG, "Camera permission denied.");
                        Toast.makeText(this, "Camera permission denied. Cannot take photo.", Toast.LENGTH_SHORT).show();
                    }
                });
        // *** END NEW Launcher Initialization ***


        // *** NEW: Initialize Retrofit Service for OneSignal API ***
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://onesignal.com/") // OneSignal API Base URL (DO NOT CHANGE THIS)
                    .addConverterFactory(GsonConverterFactory.create()) // For JSON handling
                    .build();
            // Create an instance of your API service interface. API key is set in OneSignalApiService.java.
            oneSignalApiService = retrofit.create(OneSignalApiService.class);
            Log.d(TAG, "OneSignalApiService initialized.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignalApiService", e);
            // Handle this error - notifications for send might not work
            // Disable sending buttons if notifications are considered critical feature for sending?
            // sendMessageButton.setEnabled(false);
            // send_imgmsg_btn.setEnabled(false);
            Toast.makeText(this, "Error initializing notification service. Messages may not be notified.", Toast.LENGTH_SHORT).show();
        }
        // *** END NEW ***


        // Set up UI Listeners (Keep These)
        // Group Menu icon click listener
        if (groupMenu != null) {
            // Use lambda for simplicity if your targetSdkVersion is >= 24
            groupMenu.setOnClickListener(this::showPopupMenu); // Use method reference
        } else {
            Log.w(TAG, "Group menu button is null, cannot set click listener.");
        }

        // Send Text Message button click listener
        if (sendMessageButton != null && userMessageInput != null) { // Check for null

                sendMessageButton.setOnClickListener(v -> attemptSendMessage());

        } else {
            Log.w(TAG, "Send message button or input field is null.");
        }

        // Send Image Message button click listener
        if (send_imgmsg_btn != null) { // Check for null
            send_imgmsg_btn.setOnClickListener(v -> {
                if (userMessageInput != null && !userMessageInput.isEnabled()) {
                    Toast.makeText(this, "Cannot send messages in this group.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Image picker blocked: Message input UI is disabled.");
                    return; // Do not proceed if sending is disabled
                }
                clearSelectedImage();
                Log.d(TAG, "Previous selected image cleared.");

                // Show the dialog to choose source (Gallery or Camera)
                showImageSourceDialog(); // Call the new method

            });
        } else {
            Log.w(TAG, "Send image message button is null.");
        }


        // Check user membership before proceeding with data loading/sending
        // This is important for security to prevent unauthorized access to group messages.
        // The rest of the setup (data loading, listeners) will proceed after this check if successful (by not finishing).
        checkUserMembership();


        // Fetch current user's username from Firebase (Needed for sending messages and adapter display)
        // This is an asynchronous operation.
        GetUserInfo();

        // Load initial group info (name, image) from Firebase
        // This is an asynchronous operation.
        loadGroupInfo();

        // Start loading messages from Room DB and set up Firebase sync listener
        // This is the core Room/Firebase sync setup for message display.
        LoadMessages();


        // *** NEW: Attach Listener for Group Members on Start ***
        // We need an up-to-date list of members for sending notifications.
        // This listener is crucial for the notification logic.
        attachGroupMembersListener();
        // *** END NEW ***


        Log.d(TAG, "📲 onCreate finished in GroupChatActivity");
    }

    // --- Activity Lifecycle Methods ---
    // onStart and onStop can be used to manage listeners or resources needed only when activity is visible/started.
    // onDestroy is for final cleanup.

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "GroupChatActivity onStart");
        // Firebase message sync listener is attached in LoadMessages (which is called in onCreate).
        // If you prefer syncing ONLY when the activity is started, move the call to
        // attachGroupMessagesSyncListener() from LoadMessages to here (onStart).

        // *** NEW: Attach Listener for Group Members on Start ***
        // We need an up-to-date list of members for sending notifications.
        // This listener is crucial for the notification logic.
        attachGroupMembersListener();
        // *** END NEW ***
        // Room LiveData observation is already handled by using `this` lifecycle in LoadMessages.
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "GroupChatActivity onStop");
        // Firebase message sync listener is removed in onDestroy in this version.
        // If you want to stop syncing when activity is stopped (e.g., goes to background),
        // move removeGroupMessagesSyncListener() here and re-attach in onStart.
        // removeGroupMessagesSyncListener(); // Example if moving removal here

        // *** NEW: Remove Listener for Group Members on Stop ***
        // Stop listening for membership changes when the activity is not visible.
        removeGroupMembersListener();
        // *** END NEW ***
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "GroupChatActivity onDestroy called.");

        // --- Remove Firebase Listener ---
        // Remove the messages sync listener to prevent memory leaks and unnecessary Firebase reads
        // when the activity is destroyed.
        removeGroupMessagesSyncListener();

        // LiveData observer is automatically removed because we used `this` (the Activity lifecycle)
        // when calling `observe`. Explicit removal is not strictly necessary but is an option:
        // if (groupMessagesLiveData != null) groupMessagesLiveData.removeObservers(this);

        // Room DB executor is managed by ChatDatabase singleton, no need to shut down here.

        // Dismiss progress dialog if it was used and is showing to prevent window leaks
        // if (progressDialog != null && progressDialog.isShowing()) { progressDialog.dismiss(); }
    }
    // --- End Activity Lifecycle Methods ---


    // Navigate to login activity (Keep This Helper Method)
    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity.");
        Intent loginIntent = new Intent(GroupChatActivity.this, Login.class); // Replace Login with your actual Login Activity class
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(loginIntent);
        finish(); // Close this activity after redirecting
    }


    // Initialize UI elements (Keep This, Ensure IDs Match Layout)
    // Assumes your activity_group_chat.xml includes a layout file like appbar_groupmain.xml
    // where the toolbar and its internal views are defined.
    private void InitializeFields() {
        Log.d(TAG, "Initializing UI elements.");
        // Initialize Toolbar and find it by its ID in the main activity layout
        toolbar = findViewById(R.id.group_chat_bar_layout); // Ensure this ID matches your layout

        // !!! Crucial Null Check for Toolbar !!! It's essential for the UI.
        if (toolbar == null) {
            Log.e(TAG, "CRITICAL ERROR: Toolbar not found in layout (R.id.group_chat_bar_layout)!");
            Toast.makeText(this, "Toolbar setup error.", Toast.LENGTH_SHORT).show();
            finish(); // Toolbar is essential, finish activity
            return; // Stop initialization process
        }

        // Set the found toolbar as the ActionBar for this activity
        setSupportActionBar(toolbar);
        // Hide the default title provided by the ActionBar if you are using custom views inside the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
            // Enable custom view
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            // Get custom view from layout (assuming custom_group_chat_bar.xml exists and is inflated)
            // You might need to inflate this layout manually and set it as custom view:
             /*
             View customActionBarView = LayoutInflater.from(this).inflate(R.layout.custom_group_chat_bar, null); // Replace with your custom layout
             getSupportActionBar().setCustomView(customActionBarView);
             // Find views inside the custom layout:
             groupNameTextView = customActionBarView.findViewById(R.id.group_name); // Ensure this ID matches custom layout
             groupImageView = customActionBarView.findViewById(R.id.group_profile_image); // Ensure this ID matches custom layout
             groupMenu = customActionBarView.findViewById(R.id.group_menu); // Ensure this ID matches custom layout
              */
            // OR if your main layout includes the custom bar directly, the find is sufficient:
        }


        // Find the custom views located *inside* the toolbar layout OR the custom view layout
        groupNameTextView = findViewById(R.id.group_name); // Ensure this ID matches your layout
        groupImageView = findViewById(R.id.group_profile_image); // Ensure this ID matches your layout
        groupMenu = findViewById(R.id.group_menu); // Ensure this ID matches your layout


        // !!! Null checks for Toolbar's internal views (if they are optional in your layout) !!!
        // Log warnings if these expected views are missing, as they might cause NullPointerExceptions later.
        if (groupNameTextView == null)
            Log.w(TAG, "groupNameTextView (R.id.group_name) not found in layout!");
        if (groupImageView == null)
            Log.w(TAG, "groupImageView (R.id.group_profile_image) not found in layout!");
        if (groupMenu == null)
            Log.w(TAG, "groupMenu (R.id.group_menu) not found in layout!");


        // Initialize message input and send buttons by finding them in the main activity layout
        sendMessageButton = findViewById(R.id.send_msg_button); // Ensure your send text button ID exists
        userMessageInput = findViewById(R.id.input_group_msg); // Ensure your input EditText ID exists
        send_imgmsg_btn = findViewById(R.id.send_imgmsg_btn); // Ensure your send image button ID exists


        // --- *** NEW: Initialize Image Preview UI elements *** ---
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        imgPreview = findViewById(R.id.imgPreview);
        btnCancelImage = findViewById(R.id.btnCancelImage);

        // Initially hide the image preview area and cancel button
        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.GONE);
        if (btnCancelImage != null) btnCancelImage.setVisibility(View.GONE);
        // *** END NEW ***

        // Initialize RecyclerView by finding it in the main activity layout
        messagesRecyclerView = findViewById(R.id.group_chat_recycler_view); // Ensure your RecyclerView ID exists


        // Initialize Adapter (NEW)
        // The adapter is initialized with an empty list because the actual message data will be
        // provided asynchronously by the Room LiveData observer.
        // Pass necessary info: empty list, activity context, current user ID, and initially null/empty username.
        // The username will be updated in the adapter once fetched by GetUserInfo.
        messageAdapter = new GroupMessageAdapter(new ArrayList<>(), this, currentUserID, null); // Pass null for initial username

        // Setup RecyclerView LayoutManager and Adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // Show latest messages at the bottom
        messagesRecyclerView.setLayoutManager(linearLayoutManager); // Set the layout manager
        messagesRecyclerView.setAdapter(messageAdapter); // Set the adapter to the RecyclerView


        // *** NEW: Set click listener for the cancel image button ***
        if (btnCancelImage != null) {
            btnCancelImage.setOnClickListener(v -> clearSelectedImage()); // clearSelectedImage method will be created next
        } else {
            Log.w(TAG, "btnCancelImage is null, cannot set click listener.");
        }
        // *** END NEW ***

        Log.d(TAG, "InitializeFields finished.");
    }


    // Load group info (name, image) from Firebase (Keep This Helper Method)
    // Fetches group profile information from Firebase and updates the toolbar UI.
    private void loadGroupInfo() {
        // Check if essential Firebase reference and UI views are initialized and valid
        if (groupRef == null || groupNameTextView == null || groupImageView == null) {
            Log.e(TAG, "loadGroupInfo: groupRef or UI elements are null. Aborting.");
            // Decide how to handle this critical error (e.g., disable chat, show error)
            return;
        }
        Log.d(TAG, "Loading group info from Firebase for ID: " + groupId);
        // Use addListenerForSingleValueEvent as group info doesn't change very often
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This callback runs on the main thread
                if (snapshot.exists()) {
                    // Group data found, extract name and image
                    String name = snapshot.child("groupName").getValue(String.class);
                    String groupPic = snapshot.child("groupImage").getValue(String.class); // Assume image is stored as Base64 string


                    // *** NEW: Populate groupName member variable ***
                    GroupChatActivity.this.groupName = (name != null) ? name : "Unnamed Group"; // Populate member variable with fetched or default name
                    Log.d(TAG, "Fetched group name: " + groupName);
                    // *** END NEW ***

                    // Update Toolbar views on the main thread
                    groupNameTextView.setText(groupName); // Use the populated groupName variable

                    // Update Group Profile Picture using Base64 decoding and Glide (or manual)
                    if (groupImageView != null) { // Ensure ImageView is not null
                        if (groupPic != null && !groupPic.isEmpty()) {
                            // Use Glide to load Base64 image (more robust)
                            // Assuming Base64 is just the string, add data URI prefix
                            String glideLoadableImage = "data:image/jpeg;base64," + groupPic; // Assuming JPEG

                            // Load image using Glide
                            Glide.with(GroupChatActivity.this) // Use Activity context
                                    .load(glideLoadableImage) // Load from Base64 data URI
                                    .placeholder(R.drawable.default_group_img) // Placeholder image
                                    .error(R.drawable.default_group_img) // Error image
                                    .into(groupImageView);
                            Log.d(TAG, "Loaded group image from Base64.");
                        } else {
                            // Default image if no profile pic data or data is empty
                            groupImageView.setImageResource(R.drawable.default_group_img);
                            Log.d(TAG, "No group image found or data is empty. Using default.");
                        }
                    }

                } else {
                    // Group data not found in Firebase (e.g., deleted)
                    Log.w(TAG, "Group data not found in Firebase for ID: " + groupId);
                    // Show error state and disable chat functionality
                    if (groupNameTextView != null)
                        groupNameTextView.setText("Group Not Found"); // Update UI
                    if (groupImageView != null)
                        groupImageView.setImageResource(R.drawable.default_group_img); // Update UI
                    Toast.makeText(GroupChatActivity.this, "Group does not exist or was deleted.", Toast.LENGTH_LONG).show();
                    // Disable sending messages if group is not found
                    disableMessageInputUI(); // Use helper method
                    // Do not finish, allow viewing history if any was in Room.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load group info from Firebase: " + error.getMessage());
                Toast.makeText(GroupChatActivity.this, "Failed to load group info", Toast.LENGTH_SHORT).show();
                // Show error state and disable chat functionality on error
                if (groupNameTextView != null) groupNameTextView.setText("Error Loading Group");
                if (groupImageView != null)
                    groupImageView.setImageResource(R.drawable.default_group_img);
                // Disable sending messages on error
                disableMessageInputUI(); // Use helper method
                // Do not finish, allow viewing history if any was in Room.
            }
        });
    }


    // Fetch the current user's username from Firebase (Keep This Helper Method)
    // Needed for sending messages (displaying sender name) and adapter display logic.
    private void GetUserInfo() {
        // Check if usersRef and currentUserID are initialized
        if (usersRef == null || currentUserID == null || currentUserID.isEmpty()) {
            Log.e(TAG, "GetUserInfo: usersRef or currentUserID is null/empty.");
            // Set default username and inform user if necessary
            currentUserName = "Unknown";
            if (messageAdapter != null) messageAdapter.updateCurrentUserName(currentUserName);
            return;
        }
        Log.d(TAG, "Fetching current user info for UID: " + currentUserID);
        // Fetch current user's details using SingleValueEvent (efficient for one-time fetch)
        usersRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This callback runs on the main thread
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    currentUserName = snapshot.child("username").getValue(String.class);
                    // Handle potential null username
                    if (currentUserName == null || currentUserName.isEmpty()) {
                        currentUserName = "Unknown";
                        Log.w(TAG, "Username node found but value is null/empty for user: " + currentUserID);
                    }
                    Log.d(TAG, "Fetched current username: " + currentUserName);
                    // Update the adapter with the correct username once fetched (e.g., for "You" display logic)
                    if (messageAdapter != null) {
                        messageAdapter.updateCurrentUserName(currentUserName);
                        // If adapter's display logic for "You" vs name changes based on this,
                        // you might need to notify the adapter to rebind visible items.
                        // messageAdapter.notifyDataSetChanged(); // Inefficient full refresh, maybe rebind visible items
                    }
                } else {
                    // Username node is missing or user data snapshot doesn't exist
                    currentUserName = "Unknown"; // Default name
                    Log.w(TAG, "Username not found in Firebase for current user: " + currentUserID);
                    if (messageAdapter != null) {
                        messageAdapter.updateCurrentUserName(currentUserName);
                    }
                    // Optionally update UI or inform user if their username is missing
                    Toast.makeText(GroupChatActivity.this, "Your username not found, sending as Unknown.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error retrieving current user data from Firebase: " + error.getMessage());
                Toast.makeText(GroupChatActivity.this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
                currentUserName = "Unknown"; // Set to Unknown on error
                if (messageAdapter != null) {
                    messageAdapter.updateCurrentUserName(currentUserName);
                }
                // Optionally update UI or inform user on error
            }
        });
    }


    // --- MODIFIED: Send message info to Database ---
    // This method prepares message data (text or image Base64), gets a Firebase key,
    // and writes the message to the group's messages node in Firebase.
    // The Firebase sync listener will pick it up and save it to Room.
    // It also updates the readBy map initially for the sender.
    // Added drawingSessionId parameter, which can be null for text/image messages.
    // --- MODIFIED: Send message info to Database ---
// Added sessionIdForNotification parameter
    private void SendMsgInfoToDatabase(String messageType, String messageContent, @Nullable String drawingSessionId, @Nullable String sessionIdForNotification) {
        // messageContent should be already validated and processed (text string, Base64 image string, or drawing session text)
        // messageType is "text", "image", or "drawing_session".
        // drawingSessionId is the ID of the drawing session, relevant only if messageType is "drawing_session".
        // sessionIdForNotification is the drawing session ID to include in the notification data (only if messageType is "drawing_session")


        if (TextUtils.isEmpty(messageContent) && !"drawing_session".equals(messageType)) {
            Log.w(TAG, "Attempted to send empty message content for type: " + messageType + ". Skipping.");
            return;
        }

        if (messagesRef == null || currentUserID == null || currentUserID.isEmpty() || currentUserName == null) {
            Log.e(TAG, "messagesRef, currentUserID, or currentUserName is null/empty, cannot send message.");
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
        messageInfoMap.put("message", messageContent);
        messageInfoMap.put("type", messageType);
        messageInfoMap.put("name", currentUserName);
        messageInfoMap.put("senderId", currentUserID);
        messageInfoMap.put("timestamp", ServerValue.TIMESTAMP);

        HashMap<String, Object> readByMap = new HashMap<>();
        readByMap.put(currentUserID, true);
        messageInfoMap.put("readBy", readByMap);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String currentTime = timeFormat.format(calendar.getTime());

        messageInfoMap.put("date", currentDate);
        messageInfoMap.put("time", currentTime);

        if ("drawing_session".equals(messageType) && !TextUtils.isEmpty(drawingSessionId)) {
            messageInfoMap.put("drawingSessionId", drawingSessionId);
            Log.d(TAG, "Including drawingSessionId " + drawingSessionId + " in message payload.");
        }


        messagesRef.child(messageKey).setValue(messageInfoMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, messageType + " message sent successfully to Firebase with key: " + messageKey);

                    if (oneSignalApiService != null && currentUserID != null && !currentUserID.isEmpty()
                            && currentUserName != null && !currentUserName.isEmpty()
                            && groupName != null && !groupName.isEmpty()
                            && groupMemberUids != null && !groupMemberUids.isEmpty())
                    {
                        Log.d(TAG, "Firebase message sent. Calling sendGroupPushNotification.");

                        // Determine the preview content for the notification
                        String notificationContentPreview;
                        if ("text".equals(messageType)) {
                            notificationContentPreview = messageContent;
                        } else if ("image".equals(messageType)) {
                            notificationContentPreview = "[Image]";
                        } else if ("drawing_session".equals(messageType)) {
                            // --- MODIFIED: Use the actual message content for the drawing session notification preview ---
                            notificationContentPreview = messageContent; // Use the actual message content like "User X started..."
                        } else {
                            notificationContentPreview = "[Message]";
                        }


                        sendGroupPushNotification(
                                oneSignalApiService,
                                groupMemberUids,
                                currentUserID,
                                currentUserName,
                                groupName,
                                notificationContentPreview,
                                groupId,
                                messageKey,
                                messageType,
                                sessionIdForNotification // <<< Pass the sessionId here
                        );
                    } else {
                        Log.e(TAG, "OneSignalApiService, sender info, groupName, or groupMemberUids is null/empty. Cannot send group push notification.");
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, messageType + " message failed to send to Firebase.", e);
                    Toast.makeText(GroupChatActivity.this, "Message failed to send.", Toast.LENGTH_SHORT).show();
                });
    }
    // Helper method for sending image messages (calls the main SendMsgInfoToDatabase)
    // Handles the image picking result and triggers the actual sending process.
    // Helper method for sending image messages
    private void sendImageMessageToDatabase(String encodedImage) {
        if (TextUtils.isEmpty(encodedImage)) {
            Toast.makeText(this, "Could not process image for sending", Toast.LENGTH_SHORT).show();
            return;
        }
        // Pass the encoded image (Base64 string) as message content and type "image"
        SendMsgInfoToDatabase("image", encodedImage, null, null); // <<< Added null for sessionIdForNotification
    }


    // --- MODIFIED: Load messages from Room and set up Firebase sync listener ---
    // This method sets up the data flow: Room LiveData -> Adapter + Firebase Sync Listener -> Room DB.
    private void LoadMessages() {
        // Check if essential references are initialized
        if (groupId == null || messagesRef == null || groupMessageDao == null || messagesRecyclerView == null || messageAdapter == null) {
            Log.e(TAG, "LoadMessages: Essential components are null. Cannot load messages.");
            // Optionally finish or show error state
            // finish();
            return;
        }
        Log.d(TAG, "Loading messages for group: " + groupId);

        // --- Step 1: Load messages from Room DB using LiveData ---
        // Get LiveData for messages for the specific group, ordered by timestamp (ascending).
        // This query is defined in GroupMessageDao. It will immediately load any existing offline data
        // and the observer will keep the UI updated whenever Room data changes.
        groupMessagesLiveData = groupMessageDao.getMessagesForGroup(groupId);

        // Observe the LiveData. 'this' refers to the Activity, using its lifecycle.
        // The observer will be automatically removed when the activity is destroyed.
        groupMessagesLiveData.observe(this, messagesFromRoom -> { // Use lambda for simplicity
            // This callback runs on the main thread whenever the data in the Room DB changes for this query.
            Log.d(TAG, "Group Messages LiveData updated with " + (messagesFromRoom != null ? messagesFromRoom.size() : 0) + " messages from Room for group: " + groupId);

            // Update the adapter's list with the new data from Room.
            // The list from Room is already sorted by timestamp due to the DAO query.
            messageAdapter.setMessages(messagesFromRoom); // Use the setMessages method in the adapter

            // Scroll to the bottom when messages are loaded/updated (only if list is not empty)
            // This ensures the latest messages are visible upon initial load or when new messages arrive.
            if (messagesFromRoom != null && !messagesFromRoom.isEmpty()) {
                // Use post to ensure layout calculation is done after data updates but before scrolling
                messagesRecyclerView.post(() -> {
                    // Check if the user is already near the bottom before auto-scrolling
                    LinearLayoutManager layoutManager = (LinearLayoutManager) messagesRecyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                        int totalItemCount = layoutManager.getItemCount();
                        // Auto-scroll if the list grew substantially, or the last item was already visible,
                        // or if the user is near the bottom (e.g., last 5 items are visible).
                        // Adjust the threshold (e.g., -5) as needed based on your item height.
                        if (totalItemCount > 0 && (lastVisibleItemPosition >= totalItemCount - 5 || lastVisibleItemPosition == -1)) {
                            messagesRecyclerView.smoothScrollToPosition(totalItemCount - 1);
                        }
                    } else {
                        // Fallback: always scroll if layout manager is null (shouldn't happen)
                        messagesRecyclerView.smoothScrollToPosition(messagesFromRoom.size() - 1);
                    }
                });
            }
        });
        // --- End Step 1 (LiveData Observation Setup) ---


        // --- Step 2: Attach Firebase ChildEventListener for Syncing ---
        // This listener watches the group's messages node in Firebase for adds, changes, and removals.
        // It syncs these changes *into* the Room DB on a background thread.
        // Room LiveData then automatically updates the UI via the observer above.
        attachGroupMessagesSyncListener();
        // --- End Step 2 (Firebase Sync Listener Setup) ---

        // Removed the direct list population logic from the old LoadMessages method.
    }


    // --- NEW Method to Attach Firebase ChildEventListener for Group Messages Sync ---
    // This listener syncs changes from Firebase messages node to Room DB.
    // It is attached in LoadMessages.
    @SuppressLint("RestrictedApi")
    private void attachGroupMessagesSyncListener() {
        // Check if messagesRef and groupId are initialized
        if (messagesRef == null || groupId == null || groupId.isEmpty()) { // Added check for empty groupId
            Log.e(TAG, "Cannot attach messages sync listener, messagesRef or groupId is null/empty.");
            return;
        }
        // Check if the listener is already attached to prevent duplicate listeners
        if (groupMessagesChildEventListener == null) {
            Log.d(TAG, "Attaching Firebase ChildEventListener for messages sync for group: " + messagesRef.getPath());

            // Create the ChildEventListener implementation
            groupMessagesChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // This is called for each existing message when the listener attaches, and for each new message added to Firebase.
                    String messageId = snapshot.getKey(); // This is the unique Firebase push key for the message

                    if (TextUtils.isEmpty(messageId)) {
                        Log.w(TAG, "onChildAdded received null or empty message ID from Firebase.");
                        return;
                    }

                    // Log.d(TAG, "💬 onChildAdded triggered for message ID: " + messageId); // Too verbose

                    // Get the message data from the snapshot as a HashMap to handle potential casting and missing fields
                    Map<String, Object> messageDataMap = (Map<String, Object>) snapshot.getValue();

                    if (messageDataMap == null) {
                        Log.w(TAG, "Message data is null for message ID: " + messageId);
                        // Decide how to handle this (e.g., maybe delete the message from Firebase if data is corrupt?)
                        return;
                    }


                    // --- NEW CHECK: Is this message marked as deleted for the current user? ---
                    // Fetch synchronously in the background thread's context where this listener callback runs.
                    DatabaseReference userDeletedCheckRef = rootRef.child("UserDeletedMessages").child(currentUserID).child("groups").child(groupId).child(messageId);
                    try {
                        DataSnapshot deletedCheckSnapshot = Tasks.await(userDeletedCheckRef.get(), 5, TimeUnit.SECONDS); // Add timeout

                        if (deletedCheckSnapshot.exists() && Boolean.TRUE.equals(deletedCheckSnapshot.getValue(Boolean.class))) {
                            // This message is marked as deleted for the current user. Skip saving it to Room.
                            Log.d(TAG, "onChildAdded: Message " + messageId + " is marked as deleted for user " + currentUserID + ". Skipping Room sync.");
                            // Optional: Ensure it's removed from Room if it somehow got added earlier
                            // removeMessageFromRoom(messageId); // Call if you need aggressive cleanup
                            return; // *** IMPORTANT: Exit onChildAdded, do not process further ***
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error checking if message " + messageId + " is deleted for user " + currentUserID + ". Proceeding with sync cautiously.", e);
                        // On error checking deletion status, it's safer to assume it's NOT deleted and sync it.
                    }
                    // --- END NEW CHECK ---



                    // Save/Update the message in Room DB on a background thread.
                    // This method converts the Firebase snapshot data into a Room Entity and saves it.
                    saveMessageToRoomFromSnapshot(snapshot, groupId); // Pass snapshot and groupId


                    // --- Mark the received message as read in Firebase (if it's not from the current user) ---
                    // Mark messages as read when they are added/synced to Room.
                    // This indicates the message has been "delivered" to this device and processed.
                    // This Firebase write should happen on a background thread.
                    String senderId = (String) messageDataMap.get("senderId"); // Get sender ID from map
                    if (senderId != null && !senderId.equals(currentUserID) && currentUserID != null && !currentUserID.isEmpty()) // If message is from another user AND current user ID is valid
                    {
                        // Check if current user is already in the readBy map before writing to Firebase (optimization)
                        Map<String, Boolean> readByMap = null;
                        if (messageDataMap.containsKey("readBy") && messageDataMap.get("readBy") instanceof Map) {
                            try {
                                readByMap = (Map<String, Boolean>) messageDataMap.get("readBy");
                            } catch (ClassCastException e) {
                                Log.e(TAG, "Casting readBy map failed.", e);
                            }
                        }

                        // If readBy is null, or does not contain current user as true, mark as read
                        if (readByMap == null || !Boolean.TRUE.equals(readByMap.get(currentUserID))) {
                            // We are currently in the chat activity for this group, so the user is viewing the message.
                            // Mark it as read in Firebase.
                            // The snapshot.getRef() method gives the DatabaseReference to the message node.
                            Log.d(TAG, "Attempting to mark message " + messageId + " as read for user " + currentUserID + " in Firebase (onChildAdded sync).");

                            snapshot.getRef().child("readBy").child(currentUserID).setValue(true)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Marked message " + messageId + " as read for user " + currentUserID + " in Firebase."))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to mark message " + messageId + " as read in Firebase.", e));
                            // The onChildChanged event will eventually trigger for this message when Firebase updates,
                            // syncing the updated readBy map back to Room.
                        }
                        // If message is already marked as read by this user, no action needed.
                    }
                    // --- Trigger Notification Logic (should be in background service, not here) ---
                    // Notification for incoming messages should be handled by your Firebase Messaging Service
                    // which receives messages even when the app is backgrounded or closed.
                    // Decrypting message content for the notification should happen there if needed.
                    // Removing previous local notification logic.
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // This is called when a message's data changes in Firebase (e.g., seen status, timestamp resolves, readBy map is updated)
                    String firebaseMessageId = snapshot.getKey();
                    if (TextUtils.isEmpty(firebaseMessageId)) {
                        Log.w(TAG, "onChildChanged received null or empty message ID from Firebase.");
                        return;
                    }
                    // Log.d(TAG, "🔄 onChildChanged triggered for message ID: " + firebaseMessageId); // Too verbose


                    // --- NEW CHECK: Is this message marked as deleted for the current user? ---
                    // Perform the check again for changes. If a message changes but was deleted locally,
                    // we should ignore the change and ensure it's NOT in Room.
                    DatabaseReference userDeletedCheckRef = rootRef.child("UserDeletedMessages").child(currentUserID).child("groups").child(groupId).child(firebaseMessageId);
                    try {
                        DataSnapshot deletedCheckSnapshot = Tasks.await(userDeletedCheckRef.get(), 5, TimeUnit.SECONDS); // Add timeout

                        if (deletedCheckSnapshot.exists() && Boolean.TRUE.equals(deletedCheckSnapshot.getValue(Boolean.class))) {
                            // Message is marked as deleted. Ensure it's not in Room by removing it.
                            Log.d(TAG, "🔄 onChildChanged: Message " + firebaseMessageId + " is marked as deleted for user " + currentUserID + ". Ensuring removal from Room.");
                            removeMessageFromRoom(firebaseMessageId); // Ensure it's removed from Room
                            return; // *** IMPORTANT: Exit onChildChanged, do not process further ***
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error checking if message " + firebaseMessageId + " is deleted for user " + currentUserID + " during change. Proceeding with sync cautiously.", e);
                        // On error checking deletion status, sync the change.
                    }
                    // --- END NEW CHECK ---


                    // Save/Update the message in Room DB on a background thread.
                    // This method converts the Firebase snapshot data into a Room Entity and saves it.
                    // Room's INSERT or REPLACE handles the update based on primary key.
                    saveMessageToRoomFromSnapshot(snapshot, groupId); // Pass snapshot and groupId

                    // If the change was us marking it as read, the Room update syncs the new readBy map.
                    // If the change was someone else reading our message (and we want to show read receipts),
                    // the LiveData observer will trigger UI update via Room.
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    // A message is removed from the conversation's messages node in Firebase (e.g., "Clear Chat for Everyone")
                    String firebaseMessageId = snapshot.getKey();
                    if (TextUtils.isEmpty(firebaseMessageId)) {
                        Log.w(TAG, "❌ onChildRemoved received null or empty message ID from Firebase.");
                        return;
                    }
                    Log.d(TAG, "❌ onChildRemoved triggered for message ID: " + firebaseMessageId + ". Removing from Room DB.");

                    // Remove the message from Room DB on a background thread using the DAO
                    removeMessageFromRoom(firebaseMessageId); // Pass messageId

                    // --- NEW: Also remove this ID from the locally deleted list in Firebase ---
                    // If a message is deleted for everyone, it should also be removed from the user's
                    // "deleted for me" list, otherwise, the check above would permanently block it.
                    DatabaseReference userDeletedRef = rootRef.child("UserDeletedMessages").child(currentUserID).child("groups").child(groupId).child(firebaseMessageId);
                    userDeletedRef.removeValue()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Removed message ID " + firebaseMessageId + " from user's deleted list after onChildRemoved."))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to remove message ID " + firebaseMessageId + " from user's deleted list after onChildRemoved.", e));
                    // --- END NEW ---
                }



                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // This is called if a message's position changes in Firebase (e.g., due to priority changes).
                    // Not strictly relevant if sorting by timestamp in Room query.
                    // Log.d(TAG, "onChildMoved triggered for message ID: " + snapshot.getKey());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase Group Messages Listener cancelled for group " + groupId + ": " + error.getMessage(), error.toException());
                    // Handle error - show a message to the user on the main thread
                    Toast.makeText(GroupChatActivity.this, "Failed to sync messages: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    // Optionally clear local Room data or show an error state for the chat
                }
            };

            // Attach the listener to the messages node for this specific group in Firebase
            messagesRef.addChildEventListener(groupMessagesChildEventListener);
            Log.d(TAG, "Firebase messages sync listener attached to: " + messagesRef.getPath());
        }
    }
    // --- END NEW Method to Attach Firebase Listener ---

    // --- NEW Method to Remove Firebase ChildEventListener ---
    // Remove the listener when the activity is destroyed to prevent memory leaks and unnecessary Firebase background activity.
    private void removeGroupMessagesSyncListener() {
        // Check if Firebase reference and listener are initialized
        if (messagesRef != null && groupMessagesChildEventListener != null) {
            Log.d(TAG, "Removing Firebase ChildEventListener for messages sync for group: " + groupId);
            // Remove the listener from the Firebase reference
            messagesRef.removeEventListener(groupMessagesChildEventListener);
            groupMessagesChildEventListener = null; // Nullify the reference
        }
    }
    // --- END NEW Method to Remove Firebase Listener ---


    // Inside GroupChatActivity.java class body { ... }


    private void saveMessageToRoomFromSnapshot(@NonNull DataSnapshot snapshot, String groupId) {
        // Ensure Room DAO and Executor are available
        if (groupMessageDao == null || databaseExecutor == null || groupId == null || groupId.isEmpty()) { // Added empty check for groupId
            Log.e(TAG, "Cannot save message to Room DB from snapshot: DAO, Executor, or groupId is null/empty.");
            return;
        }
        String firebaseMessageId = snapshot.getKey(); // Get the message ID (Firebase push key)
        if (TextUtils.isEmpty(firebaseMessageId)) {
            Log.w(TAG, "saveMessageToRoomFromSnapshot received snapshot with empty key.");
            return;
        }

        // Get the message data as a HashMap to handle potential casting and missing fields safely
        Map<String, Object> messageDataMap = (Map<String, Object>) snapshot.getValue();
        if (messageDataMap == null) {
            Log.w(TAG, "Message data map is null for message ID: " + firebaseMessageId + " in saveMessageToRoomFromSnapshot.");
            // Optionally remove this entry from Firebase if it's corrupt? For FYP, logging warning is likely enough.
            return;
        }

        // --- Extract data from the map. Use default values or handle nulls appropriately. ---
        String messageContent = (String) messageDataMap.get("message"); // Message content (text, Base64 image, or Drawing Session text)
        String messageType = (String) messageDataMap.get("type"); // Message type ("text", "image", "drawing_session")
        String senderName = (String) messageDataMap.get("name"); // Sender's display name
        String senderId = (String) messageDataMap.get("senderId"); // Sender's UID
        // Timestamp from Firebase resolves to a Long. Use default 0L if missing.
        // Use the actual long value from snapshot directly if available, otherwise fallback.
        Long timestampLong = snapshot.hasChild("timestamp") ? snapshot.child("timestamp").getValue(Long.class) : 0L;
        // Handle case where timestamp might be null from Firebase for some reason, or if using local time fallback
        if (timestampLong == null) timestampLong = 0L; // Ensure it's not null for the long type

        String date = (String) messageDataMap.get("date"); // Client date (Optional)
        String time = (String) messageDataMap.get("time"); // Client time (Optional)

        // Safely get and cast the readBy map.
        Map<String, Boolean> readByMap = null;
        if (messageDataMap.containsKey("readBy") && messageDataMap.get("readBy") instanceof Map) {
            try {
                // Use a type-safe way to cast the map values to Boolean, defaulting to false if not Boolean
                Map<String, Object> rawMap = (Map<String, Object>) messageDataMap.get("readBy");
                readByMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                    if (entry.getValue() instanceof Boolean) {
                        readByMap.put(entry.getKey(), (Boolean) entry.getValue());
                    } else if (entry.getValue() != null) {
                        // Log warning if value is not Boolean but not null
                        Log.w(TAG, "Expected Boolean value for readBy map entry " + entry.getKey() + ", but found " + entry.getValue().getClass().getName());
                    }
                    // If value is null, it's treated as not read (default false)
                }

            } catch (ClassCastException e) {
                Log.e(TAG, "Failed to cast 'readBy' to Map<String, Object> for message " + firebaseMessageId + " in saveMessageToRoom.", e);
            }
        }
        if (readByMap == null) {
            readByMap = new HashMap<>(); // Default to empty map if missing or casting failed
        }


        // --- NEW: Extract drawingSessionId from the snapshot ---
        // This field will only exist for messages of type "drawing_session".
        // Use snapshot.hasChild() and getValue() to get the string value safely.
        String drawingSessionId = snapshot.hasChild("drawingSessionId") ?
                snapshot.child("drawingSessionId").getValue(String.class) :
                null; // Set to null if the child node does not exist


        // --- Create the GroupMessageEntity object using the constructor ---
        // Ensure all fields from the entity are included and mapped correctly from Firebase data.
        // Pass the extracted drawingSessionId as the last argument.
        GroupMessageEntity messageEntity = new GroupMessageEntity(
                firebaseMessageId, // Primary Key (Firebase push key)
                groupId, // Group ID (from Activity member variable)
                messageContent, // Message content
                messageType, // Message type
                senderName, // Sender's display name
                senderId, // Sender's UID
                timestampLong, // Resolved timestamp
                date, // Client date
                time, // Client time
                readByMap, // ReadBy map
                drawingSessionId // <<< NEW: Pass the extracted drawingSessionId (will be null for text/image)
        );

        // Save/Update the message in Room DB on a background thread using the shared executor
        databaseExecutor.execute(() -> { // Execute on the shared DB executor
            try {
                // Insert or replace the message in Room. This handles both new messages (insert)
                // and updates to existing ones (replace based on primary key messageId).
                groupMessageDao.insertOrUpdateMessage(messageEntity);
                // Log.d(TAG, "Group message saved/updated in Room DB for message ID: " + firebaseMessageId + " (Group: " + groupId + ")."); // Too verbose maybe
                // LiveData observer attached in LoadMessages will automatically pick this Room change up and update the UI
            } catch (Exception e) {
                Log.e(TAG, "Error saving/updating message " + firebaseMessageId + " in Room DB from snapshot", e);
                // Handle Room DB errors (log, retry?)
            }
        });
    }
// --- END MODIFIED Method saveMessageToRoomFromSnapshot ---


    // --- NEW Method to Remove Message from Room DB ---
    // This method is called by the onChildRemoved event of the Firebase listener, or potentially manually (e.g., Clear Chat).
    private void removeMessageFromRoom(String messageId) {
        // Ensure Room DAO and Executor are available
        if (groupMessageDao == null || databaseExecutor == null) {
            Log.e(TAG, "Cannot remove message from Room DB: DAO or Executor is null.");
            return;
        }
        if (TextUtils.isEmpty(messageId)) {
            Log.w(TAG, "removeMessageFromRoom received empty messageId.");
            return;
        }

        // Execute the delete operation on the shared database executor (background thread)
        databaseExecutor.execute(() -> { // Use the shared database executor for Room ops
            try {
                // Delete the message from Room DB by its messageId (primary key)
                int deletedRows = groupMessageDao.deleteMessageById(messageId); // Use delete by messageId
                if (deletedRows > 0) {
                    Log.d(TAG, "Message removed from Room DB for message ID: " + messageId + ". Rows deleted: " + deletedRows);
                    // LiveData observer will pick this Room change up and update UI automatically
                } else {
                    // This is expected if the message didn't exist in Room for some reason (e.g., wasn't synced)
                    Log.w(TAG, "Attempted to remove message " + messageId + " from Room DB, but it was not found.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error removing message " + messageId + " from Room DB", e);
                // Handle Room DB errors
            }
        });
    }
    // --- END NEW Method to Remove Message from Room DB ---



    // --- Keep checkUserMembership method ---
    // This method checks if the current user is a member or admin of the group in Firebase.
    // It enables/disables the message input UI based on membership status,
    // allowing viewing historical messages even if not a member.
    // Inside GroupChatActivity.java

    // --- REWRITTEN checkUserMembership method ---
    // This method checks if the current user is a member or admin.
    // It enables/disables the message input UI based on membership status,
    // allowing viewing historical messages even if not a member.
    private void checkUserMembership() {
        // Check if essential references and user ID are initialized
        if (groupRef == null || currentUserID == null || currentUserID.isEmpty()) { // Added empty check for userId
            Log.e(TAG, "checkUserMembership: groupRef or currentUserID is null/empty. Cannot check membership.");
            Toast.makeText(this, "Critical error checking membership.", Toast.LENGTH_SHORT).show();
            finish(); // Critical error, cannot safely proceed
            return;
        }
        Log.d(TAG, "Checking membership status for user " + currentUserID + " in group " + groupId);

        // Use addListenerForSingleValueEvent to check membership status once when activity starts
        // We only need to listen to the group node to get both 'members' and 'admin'
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This callback runs on the main thread

                if (!snapshot.exists()) {
                    // Group node does not exist in Firebase. User cannot view it at all.
                    Log.w(TAG, "Group data not found in Firebase for ID: " + groupId + ". Finishing activity.");
                    Toast.makeText(GroupChatActivity.this, "Group does not exist or was deleted.", Toast.LENGTH_LONG).show();
                    finish(); // Group doesn't exist, cannot view history associated with it
                    return; // Exit the onDataChange method
                }

                // Group node exists, now check membership details
                DataSnapshot membersSnapshot = snapshot.child("members"); // Get the members node snapshot
                String adminId = snapshot.child("admin").getValue(String.class); // Get the admin UID

                // Check if the current user ID exists within the members node OR is the admin
                boolean isMember = membersSnapshot.hasChild(currentUserID);
                boolean isAdmin = adminId != null && adminId.equals(currentUserID);


                // Logic based on membership status:
                if (isMember || isAdmin) {
                    // User is either explicitly listed as a member OR is the admin
                    Log.d(TAG, "User " + currentUserID + " is a member or admin of group " + groupId + ". Enabling message input UI.");
                    // User is authorized, ensure message input UI is enabled
                    enableMessageInputUI();

                } else {
                    // User is NOT a member and NOT the admin. They cannot send messages.
                    Log.w(TAG, "User " + currentUserID + " is NOT a member or admin of group " + groupId + ". Disabling message input UI.");
                    Toast.makeText(GroupChatActivity.this, "You are no longer a member of this group.", Toast.LENGTH_LONG).show();
                    // User is not authorized to send, disable message input UI
                    disableMessageInputUI();
                    // *** IMPORTANT: DO NOT call finish() here ***
                    // The activity remains open, allowing the user to view the historical messages
                    // loaded from the Room database.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check membership status in Firebase: " + error.getMessage(), error.toException());
                Toast.makeText(GroupChatActivity.this, "Failed to verify group access. Cannot send messages.", Toast.LENGTH_SHORT).show();
                // On error fetching membership status, defensively disable UI as we cannot confirm authorization.
                disableMessageInputUI();
                // *** IMPORTANT: DO NOT call finish() here on cancellation either ***
                // Similar to the non-member case, allow viewing history if the group node itself exists.
            }
        });
    }
    // --- END REWRITTEN checkUserMembership ---

    // --- Keep showPopupMenu method ---
    // This method handles displaying the menu when the groupMenu icon is clicked.
    // It inflates the menu resource and sets the click listener for menu items.
    @SuppressLint("NonConstantResourceId")
    // Keep this if using R.id in switch statement or if-else chain
    private void showPopupMenu(View view) {
        // Ensure context is not null before showing menu
        if (this == null) {
            Log.w(TAG, "Context is null, cannot show popup menu.");
            return;
        }
        // Create a new PopupMenu attached to the specified view
        PopupMenu popupMenu = new PopupMenu(this, view);
        // Get the menu inflater and inflate the menu resource
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.popup_group_menu, popupMenu.getMenu()); // Replace with your actual menu resource ID


        // Handle Menu Item Clicks using setOnMenuItemClickListener
        popupMenu.setOnMenuItemClickListener(item -> {
            // Get the ID of the clicked menu item
            int id = item.getItemId();

            // Use if-else or switch to handle menu item clicks by ID
            // Ensure these R.id values match the item IDs in your popup_group_menu.xml
            // This requires your menu resource to be defined with these IDs.
            if (id == R.id.group_settings) { // Assuming R.id.group_settings exists in your menu
                Log.d(TAG, "Popup menu item clicked: Group Settings");
                openGroupSettings(); // Call method to open settings activity
                return true; // Event handled
            } else if (id == R.id.clear_chat) { // Assuming R.id.clear_chat exists in your menu
                Log.d(TAG, "Popup menu item clicked: Clear Chat");
                confirmClearChat(); // Call method to confirm and clear chat
                return true; // Event handled
            } else if (id == R.id.exit_group) { // Assuming R.id.exit_group exists in your menu
                Log.d(TAG, "Popup menu item clicked: Exit Group");
                confirmExitGroup(); // Call method to confirm and exit group
                return true; // Event handled

            } else if (id == R.id.start_drawing_chat) { // <<< NEW Case
                Log.d(TAG, "Popup menu item clicked: Start Shared Drawing");
                confirmStartDrawingSession(); // <<< Call the new method
                return true; // Event handled

            } else {
                // Handle other menu items if any
                // Use getResources().getResourceEntryName(id) to get the XML ID name for logging
                String itemName = "Unknown";
                try {
                    itemName = getResources().getResourceEntryName(id);
                } catch (Exception e) {
                    // Ignore exception if resource name cannot be found
                }
                Log.d(TAG, "Popup menu item clicked with unhandled ID: " + id + " (" + itemName + ")");
                return false; // Event not handled by this listener
            }
        });

        // Show the popup menu
        popupMenu.show();
    }

    // --- Keep openGroupSettings method ---
    // Opens the Group Settings activity, passing the group ID.
    private void openGroupSettings() {
        // Ensure context is not null before starting activity
        if (this == null) {
            Log.w(TAG, "Context is null, cannot open group settings.");
            return;
        }
        // Create intent for GroupSettingsActivity
        Intent intent = new Intent(this, GroupSettingsActivity.class); // Replace GroupSettingsActivity with your actual Activity class
        intent.putExtra("groupId", groupId); // Pass the current group ID to the settings activity

        Log.d(TAG, "Starting GroupSettingsActivity with groupId: " + groupId);

        // Start the activity
        startActivity(intent);
    }

    // --- Keep confirmClearChat method ---
    // Shows a confirmation dialog before clearing the chat messages.
    private void confirmClearChat() {
        // Ensure context is not null
        if (this == null) {
            Log.w(TAG, "Context is null, cannot show clear chat confirmation dialog.");
            return;
        }
        Log.d(TAG, "Showing clear chat confirmation dialog.");
        // Create and show an AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Are you sure you want to clear all messages from this group locally? This will only remove messages from your device.")
                .setPositiveButton("Yes, Clear", (dialog, which) -> clearChat()) // If "Yes, Clear" is clicked, call clearChat()
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // If "Cancel" is clicked, just dismiss the dialog
                .setIcon(android.R.drawable.ic_dialog_alert) // Optional: add an icon
                .show(); // Show the dialog
    }

    // --- MODIFIED clearChat method ---
    // Deletes messages ONLY from Room DB for the current user.
    private void clearChat() {
        Log.d(TAG, "Initiating local clear chat process for group: " + groupId + " for user: " + currentUserID);

        if (groupMessageDao == null || databaseExecutor == null || groupId == null || groupId.isEmpty() || currentUserID == null || currentUserID.isEmpty() || rootRef == null || messageAdapter == null) { // Added messageAdapter check
            Log.e(TAG, "Cannot clear chat locally: Missing dependencies.");
            runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Error clearing chat.", Toast.LENGTH_SHORT).show());
            return;
        }

        databaseExecutor.execute(() -> {
            try {
                List<String> messageIdsToDelete = groupMessageDao.getMessageIdsForGroup(groupId);

                if (messageIdsToDelete == null || messageIdsToDelete.isEmpty()) {
                    Log.d(TAG, "No messages found in Room DB to clear for group: " + groupId + ". Nothing to delete or record.");
                    runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "No messages to clear locally.", Toast.LENGTH_SHORT).show());
                    // Also clear adapter if the list was already empty in Room but still showing
                    runOnUiThread(() -> {
                        messageAdapter.setMessages(new ArrayList<>()); // Ensure adapter is empty
                    });
                    return;
                }
                Log.d(TAG, "Found " + messageIdsToDelete.size() + " message IDs in Room to clear. Proceeding to record in Firebase and delete from Room.");

                DatabaseReference userDeletedMessagesRef = rootRef.child("UserDeletedMessages").child(currentUserID).child("groups").child(groupId);
                Map<String, Object> updatesMap = new HashMap<>();
                for (String msgId : messageIdsToDelete) { /* ... populate map ... */ }

                if (!updatesMap.isEmpty()) { /* ... record in Firebase ... */ }


                // --- Step 3: Delete the messages from the local Room DB ---
                Log.d(TAG, "Deleting messages from Room DB for group: " + groupId);
                int deletedRows = groupMessageDao.deleteAllMessagesForGroup(groupId); // Delete all for this group

                // --- Step 4: Manually Update Adapter After Room Deletion ---
                // Even though LiveData *should* trigger, manually clearing and notifying
                // the adapter immediately after the Room deletion ensures the UI updates.
                Log.d(TAG, "Room deletion complete. Manually updating adapter on Main Thread.");
                runOnUiThread(() -> {
                    if (deletedRows > 0) {
                        Log.d(TAG, "Successfully deleted " + deletedRows + " messages from Room DB locally. Clearing adapter.");
                        // Clear the adapter's list and notify
                        messageAdapter.setMessages(new ArrayList<>()); // *** Clear the adapter's list ***
                        Toast.makeText(GroupChatActivity.this, "Chat cleared locally.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Attempted to delete from Room DB locally, but DAO reported 0 rows deleted.");
                        // If 0 rows were deleted but the initial list was not empty, clear the adapter forcefully.
                        if (!messageIdsToDelete.isEmpty()) {
                            Log.w(TAG, "Initial messageIdsToDelete was not empty, but 0 rows deleted from Room. Forcing adapter clear.");
                            messageAdapter.setMessages(new ArrayList<>()); // *** Force adapter clear ***
                            Toast.makeText(GroupChatActivity.this, "Error deleting messages from Room.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GroupChatActivity.this, "No messages to clear locally.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // --- End Step 4 ---

            } catch (Exception e) {
                Log.e(TAG, "Error during local clear chat process for group " + groupId, e);
                runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Error clearing chat locally.", Toast.LENGTH_SHORT).show());
                // On error, maybe attempt to clear adapter forcefully if the list was showing items?
                runOnUiThread(() -> {
                    if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
                        Log.w(TAG, "Error during clear chat, but adapter has items. Forcing adapter clear.");
                        messageAdapter.setMessages(new ArrayList<>());
                    }
                });
            }
        });
    }

    // --- Keep confirmExitGroup method ---
    // Shows a confirmation dialog before exiting the group.
    private void confirmExitGroup() {
        // Ensure context is not null
        if (this == null) {
            Log.w(TAG, "Context is null, cannot show exit group confirmation dialog.");
            return;
        }
        Log.d(TAG, "Showing exit group confirmation dialog.");
        // Create and show an AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Exit Group")
                .setMessage("Are you sure you want to exit this group? You will not be able to send or receive messages unless re-added. Local chat history will remain.") // Updated message
                .setPositiveButton("Exit", (dialog, which) -> exitGroup()) // If "Exit" is clicked, call exitGroup()
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // If "Cancel" is clicked, just dismiss the dialog
                .setIcon(android.R.drawable.ic_dialog_alert) // Optional: add an icon
                .show(); // Show the dialog
    }

    // --- REWRITTEN exitGroup method ---
    // Removes the current user from the group's members list in Firebase,
    // keeps local data in Room, disables the message input UI, and keeps the activity open.
    private void exitGroup() {
        Log.d(TAG, "User " + currentUserID + " attempting to exit group " + groupId);

        // Check if essential references and user ID are initialized
        if (groupRef == null || currentUserID == null || currentUserID.isEmpty() || groupListDao == null || databaseExecutor == null) { // Added DAO/Executor check
            Log.e(TAG, "groupRef, currentUserID, groupListDao, or databaseExecutor is null/empty, cannot initiate exit group.");
            Toast.makeText(this, "Error initiating exit.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Step 1: Remove user from the group's members list in Firebase ---
        Log.d(TAG, "Removing user " + currentUserID + " from members list in Firebase for group " + groupId);
        groupRef.child("members").child(currentUserID).removeValue().addOnCompleteListener(task -> {
            // This callback runs on the main thread after the Firebase remove operation completes

            if (task.isSuccessful()) {
                Log.d(TAG, "User " + currentUserID + " successfully removed from group " + groupId + " members list in Firebase.");
                // Do NOT show "You exited the group." Toast here yet, show after Room deletion for better timing perception.

                // --- Step 2: Delete the GroupEntity from Room DB (for this user) ---
                // This is the change that makes the group disappear from the GroupFragment list.
                // Run this on the background executor.
                Log.d(TAG, "Deleting GroupEntity from Room DB for user " + currentUserID + " group " + groupId);
                databaseExecutor.execute(() -> { // Use the shared DB executor
                    try {
                        groupListDao.deleteGroupForOwner(groupId, currentUserID); // <<< NEW CALL to delete from Room

                        // Switch back to the main thread to show the success toast after Room deletion
                        runOnUiThread(() -> {
                            Log.d(TAG, "GroupEntity deleted from Room. Showing success toast.");
                            Toast.makeText(GroupChatActivity.this, "You exited the group.", Toast.LENGTH_SHORT).show();
                            // The GroupFragment's LiveData will update automatically.
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Error deleting GroupEntity from Room DB for group " + groupId, e);
                        // Show failure toast on main thread
                        runOnUiThread(() -> {
                            Toast.makeText(GroupChatActivity.this, "Error cleaning up local data.", Toast.LENGTH_SHORT).show();
                        });
                        // Even if Room deletion fails, the Firebase removal succeeded.
                        // The group might still show in the list until the next sync or app restart if Room deletion failed.
                    }
                });
                // --- End Step 2 ---


                // --- Step 3: Disable message input UI immediately ---
                // This prevents the user from sending new messages after successfully exiting,
                // even if the Room deletion takes a moment or fails.
                disableMessageInputUI();

                // --- Step 4: KEEP the local Room DB chat history (GroupMessageEntity) ---
                // The messages themselves are NOT deleted from the group_messages table by this process.
                // They will remain visible in this activity until the user manually clears the chat.

                // --- Step 5: Keep the activity open ---
                // The activity remains open, allowing the user to view the historical messages.

            } else {
                Log.e(TAG, "Failed to remove user " + currentUserID + " from group " + groupId + " members list in Firebase.", task.getException());
                Toast.makeText(this, "Failed to exit group: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                // On failure, the user is still technically a member in Firebase.
                // The UI remains enabled (as per checkUserMembership if it runs again).
                // They would need to retry exiting.
            }
        });
        // --- End Step 1 ---
    }
// --- END REWRITTEN exitGroup (MODIFIED) ---


    private void enableMessageInputUI() {
        if (userMessageInput != null) {
            userMessageInput.setEnabled(true);
            userMessageInput.setHint("Enter Message..."); // Set back to default hint
        }
        if (sendMessageButton != null) {
            sendMessageButton.setEnabled(true);
        }
        if (send_imgmsg_btn != null) {
            send_imgmsg_btn.setEnabled(true);
        }
        Log.d(TAG, "Message input UI ENABLED.");
    }

    private void disableMessageInputUI() {
        if (userMessageInput != null) {
            userMessageInput.setEnabled(false);
            userMessageInput.setHint("You have exited this group."); // Inform user why it's disabled
        }
        if (sendMessageButton != null) {
            sendMessageButton.setEnabled(false);
        }
        if (send_imgmsg_btn != null) {
            send_imgmsg_btn.setEnabled(false);
        }
        Log.d(TAG, "Message input UI DISABLED.");
    }
    // --- END NEW Helper methods ---


    // Keep onActivityResult for image picking (Handles the result from the image picker Intent)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if the result is from the image picker request and was successful
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData(); // Get the Uri of the selected image
            Log.d(TAG, "Image selected from picker. URI: " + imageUri);

            // Process and encode image to Base64.
            // This can be a heavy task for large images. Consider using an ExecutorService
            // or a library that handles background processing for image loading/encoding.
            // For simplicity here, it runs on the main thread.
            try {
                // Get Bitmap from Uri using MediaStore
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Log.d(TAG, "Bitmap obtained from URI. Dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                // Encode the Bitmap to a Base64 string
                String encodedImage = encodeImageToBase64(bitmap); // Use your helper method (runs on calling thread)
                Log.d(TAG, "Image encoded to Base64. Sending...");

                // Send the Base64 encoded image as a message
                sendImageMessageToDatabase(encodedImage); // Call the image sending method

            } catch (IOException e) {
                Log.e(TAG, "Image processing failed during selection/encoding (IOException)", e);
                Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show();
            } catch (Exception e) { // Catch any other unexpected exceptions during bitmap/encoding
                Log.e(TAG, "Unexpected error during image processing/encoding", e);
                Toast.makeText(this, "An error occurred processing image", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Log if the activity result was not for the image picker or was not successful
            Log.d(TAG, "ActivityResult for image picker cancelled or failed. RequestCode: " + requestCode + ", ResultCode: " + resultCode);
        }
    }

    // Helper method to encode Bitmap to Base64 string (Keep This Helper Method)


    // --- NEW Method: Start a new drawing session in Firebase and send chat message ---
    private void startDrawingSession() {
        Log.d(TAG, "Attempting to start a new drawing session for group: " + groupId);

        // Check for essential data (Firebase refs, user info, group ID, session ID)
        // currentUserID, currentUserName are fetched in GetUserInfo().
        // groupId is from intent.
        // messagesRef, rootRef are initialized in onCreate.
        if (rootRef == null || groupId == null || groupId.isEmpty() || currentUserID == null || currentUserID.isEmpty() || currentUserName == null) { // Added empty checks
            Log.e(TAG, "Cannot start drawing session: Required dependencies are null/empty.");
            Toast.makeText(this, "Error starting drawing session. Missing setup info.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Get reference to the drawingSessions node within this group
        DatabaseReference drawingSessionsRef = rootRef.child("Groups").child(groupId).child("drawingSessions");

        // 2. Create a new entry under 'drawingSessions' in Firebase
        // This generates a unique ID for the new session using push().getKey()
        String newSessionId = drawingSessionsRef.push().getKey();

        if (newSessionId == null) {
            Log.e(TAG, "Failed to generate unique session ID from Firebase.");
            Toast.makeText(this, "Error starting drawing: Could not generate session ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Generated new drawing session ID: " + newSessionId);

        // 3. Prepare initial data for the new session node
        HashMap<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("starterId", currentUserID); // Store who started it
        sessionInfo.put("createdAt", ServerValue.TIMESTAMP); // Store server timestamp
        sessionInfo.put("state", "active"); // Set initial state to active
        sessionInfo.put("groupId", groupId); // Store the groupId in the session node itself


        // 4. Write the initial session info to Firebase
        drawingSessionsRef.child(newSessionId).setValue(sessionInfo)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New drawing session node created in Firebase: " + newSessionId);

                    // 5. After successfully creating the session node, send the special chat message
                    // This message links to the new session and appears in the main chat history.
                    // This will trigger the notification for other members.
                    sendDrawingSessionStartedMessage(newSessionId, currentUserName, currentUserID);

                    // 6. Navigate to the new Drawing Activity
                    // Pass the group ID and the new session ID to the activity.
                    // Ensure YOUR_DRAWING_ACTIVITY_CLASS exists and accepts these extras.
                    Intent drawingIntent = new Intent(GroupChatActivity.this, YOUR_DRAWING_ACTIVITY_CLASS.class); // <<< Replace with your actual Drawing Activity Class Name
                    drawingIntent.putExtra("groupId", groupId); // Pass group ID
                    drawingIntent.putExtra("sessionId", newSessionId); // Pass the NEWLY generated session ID
                    startActivity(drawingIntent);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create new drawing session node in Firebase.", e);
                    Toast.makeText(GroupChatActivity.this, "Failed to start drawing session.", Toast.LENGTH_SHORT).show();
                    // Optional: Clean up the partially created session node if the write failed
                    // drawingSessionsRef.child(newSessionId).removeValue();
                });
    }

    // --- NEW Method to Send the Special "Drawing Session Started" Message ---
// This is similar to SendMsgInfoToDatabase but includes the drawingSessionId.
    // Inside GroupChatActivity.java

    // --- NEW Method: Send the Special "Drawing Session Started" Message to Chat History ---
    // --- NEW Method: Send the Special "Drawing Session Started" Message to Chat History ---
// Modified call to SendMsgInfoToDatabase
    private void sendDrawingSessionStartedMessage(String sessionId, String senderName, String senderId) {
        Log.d(TAG, "Sending 'drawing_session' message for session: " + sessionId + " in group: " + groupId);

        if (messagesRef == null || senderId == null || senderId.isEmpty() || senderName == null || sessionId == null || sessionId.isEmpty() || groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Cannot send drawing session message: Required dependencies are null/empty.");
            Toast.makeText(this, "Error sending drawing link message.", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageKey = messagesRef.push().getKey();

        if (messageKey == null) {
            Log.e(TAG, "Failed to generate unique message key for drawing session message.");
            Toast.makeText(this, "Error sending drawing link: Failed to generate message ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Generated Firebase push ID for drawing session message: " + messageKey);

        String startMessageText = senderName + " started a shared drawing session."; // Customize this text

        // --- MODIFIED: Pass the sessionId twice: once for the message data itself, and once for the notification data ---
        SendMsgInfoToDatabase("drawing_session", startMessageText, sessionId, sessionId); // <<< Pass sessionId here for the notification
        // The rest of the method below preparing messageInfoMap is actually redundant *after* the call above.
        // You can remove the rest of this method's content below the SendMsgInfoToDatabase call if you like,
        // as SendMsgInfoToDatabase now handles creating the map and sending to Firebase.

        // --- Remove this redundant map creation and Firebase call ---
    /*
    HashMap<String, Object> messageInfoMap = new HashMap<>();
    messageInfoMap.put("message", startMessageText);
    messageInfoMap.put("type", "drawing_session");
    messageInfoMap.put("name", senderName);
    messageInfoMap.put("senderId", senderId);
    messageInfoMap.put("timestamp", ServerValue.TIMESTAMP);
    messageInfoMap.put("drawingSessionId", sessionId);
    HashMap<String, Object> readByMap = new HashMap<>();
    readByMap.put(senderId, true);
    messageInfoMap.put("readBy", readByMap);
     // ... rest of map creation ...
    // messagesRef.child(messageKey).setValue(messageInfoMap) // <-- REMOVE THIS FIREBASE CALL as SendMsgInfoToDatabase does it
    */
        // --- End Remove ---
    }

    // Inside GroupChatActivity.java

    // --- NEW Method: Show confirmation dialog before starting drawing ---
    private void confirmStartDrawingSession() {
        if (this == null) {
            Log.w(TAG, "Context is null, cannot show start drawing confirmation dialog.");
            return;
        }
        Log.d(TAG, "Showing start drawing session confirmation dialog for group: " + groupId);

        // Optional: Check if the user is still a member/admin here if needed.
        // The checkUserMembership method already runs on start/resume and might disable UI.
        // For simplicity, we assume the menu item isn't shown if they aren't members
        // or we allow viewing but not starting if not members.
        // A stricter check could be added here, but it would require fetching membership status again or relying on a state flag.
        // Let's assume checkUserMembership's UI disablement is sufficient to prevent button clicks if not authorized.

        new AlertDialog.Builder(this)
                .setTitle("Start Shared Drawing")
                .setMessage("Are you sure you want to start a new shared drawing session in this group? All members will be able to draw.")
                .setPositiveButton("Yes, Start", (dialog, which) -> startDrawingSession()) // If "Yes, Start" clicked, call startDrawingSession()
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // If "Cancel" clicked, just dismiss
                .setIcon(android.R.drawable.ic_dialog_info) // Optional: add an icon
                .show();
    }


    // *** NEW Method to Attach ValueEventListener for Group Members Sync ***
    // This listener watches the group's members list in Firebase.
    // It populates the groupMemberUids list needed for sending notifications.
    @SuppressLint("RestrictedApi")
    private void attachGroupMembersListener() {
        // Check if groupRef and groupId are initialized
        if (groupRef == null || groupId == null || groupId.isEmpty()) { // Added empty check for groupId
            Log.e(TAG, "Cannot attach group members listener, groupRef or groupId is null/empty.");
            return;
        }
        // Check if the listener is already attached to prevent duplicate listeners
        if (groupMembersListener == null) {
            Log.d(TAG, "Attaching Firebase ValueEventListener for group members for group: " + groupRef.child("members").getPath());

            groupMembersListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // This callback runs on the main thread when members data changes
                    groupMemberUids.clear(); // Clear the old list

                    if (snapshot.exists()) {
                        // Loop through the UIDs under the 'members' node
                        for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                            String memberId = memberSnapshot.getKey(); // Get the member's UID (the key)
                            if (!TextUtils.isEmpty(memberId)) {
                                groupMemberUids.add(memberId); // Add UID to the list
                            }
                        }
                        Log.d(TAG, "Group members list updated. Total members: " + groupMemberUids.size());
                        // Log the members list (optional)
                        // Log.d(TAG, "Group members UIDs: " + groupMemberUids.toString());

                    } else {
                        // Members node does not exist or is empty (shouldn't happen for a valid group with at least the creator)
                        Log.w(TAG, "Group members node not found or is empty for group: " + groupId);
                        // groupMemberUids list is already cleared.
                        // If members node is unexpectedly empty, consider disabling sending messages?
                        // This might indicate a data inconsistency or group deletion.
                        // disableMessageInputUI();
                    }
                    // The updated groupMemberUids list is now ready for use in SendMsgInfoToDatabase.
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase Group Members Listener cancelled for group " + groupId + ": " + error.getMessage(), error.toException());
                    // Handle error - maybe show a message to the user on the main thread
                    // Or disable sending messages if membership list is crucial
                    Toast.makeText(GroupChatActivity.this, "Failed to get group members list.", Toast.LENGTH_SHORT).show();
                    // Clear the list on error, as we don't have accurate member info
                    groupMemberUids.clear();
                    // If members list fetch fails critically, perhaps disable message sending?
                    // disableMessageInputUI();
                }
            };

            // Attach the listener to the 'members' node within the group reference
            groupRef.child("members").addValueEventListener(groupMembersListener);
            Log.d(TAG, "Firebase group members listener attached to: " + groupRef.child("members").getPath());
        }
    }
    // *** END NEW Method to Attach ValueEventListener for Group Members Sync ***

    // *** NEW Method to Remove ValueEventListener for Group Members ***
    // Remove the listener when the activity is stopped to prevent memory leaks and unnecessary Firebase background activity.
    private void removeGroupMembersListener() {
        // Check if Firebase reference and listener are initialized
        if (groupRef != null && groupMembersListener != null) {
            Log.d(TAG, "Removing Firebase ValueEventListener for group members for group: " + groupId);
            // Remove the listener from the Firebase reference
            groupRef.child("members").removeEventListener(groupMembersListener);
            groupMembersListener = null; // Nullify the reference
        }
    }
    // *** END NEW Method to Remove ValueEventListener ***


    // Inside GroupChatActivity.java class body { ... }

    // --- MODIFIED HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION TO GROUP MEMBERS ---
// Added sessionId parameter
    private void sendGroupPushNotification(OneSignalApiService apiService,
                                           List<String> allMemberUids,
                                           String senderUid,
                                           String senderName,
                                           String groupName,
                                           String messageContentPreview,
                                           String groupId,
                                           String messageId,
                                           String messageType,
                                           @Nullable String sessionId) { // <<< Added sessionId parameter


        if (apiService == null || allMemberUids == null || allMemberUids.isEmpty() || senderUid == null || senderUid.isEmpty() || groupId == null || groupId.isEmpty() || messageId == null || messageId.isEmpty()) {
            Log.e(TAG, "sendGroupPushNotification: Essential parameters are null/empty. Cannot send notification.");
            return;
        }

        List<String> recipientUids = new ArrayList<>();
        for (String memberUid : allMemberUids) {
            if (!TextUtils.isEmpty(memberUid) && !memberUid.equals(senderUid)) {
                recipientUids.add(memberUid);
            }
        }

        if (recipientUids.isEmpty()) {
            Log.d(TAG, "sendGroupPushNotification: No recipients after filtering sender (" + senderUid + "). Skipping notification for group " + groupId + " message " + messageId + ".");
            return;
        }

        Log.d(TAG, "Preparing OneSignal push notification for group " + groupId + " to " + recipientUids.size() + " recipients.");

        JsonObject notificationBody = new JsonObject();

        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID);

        JsonArray externalUserIdsArray = new JsonArray();
        for (String uid : recipientUids) {
            externalUserIdsArray.add(uid);
        }
        notificationBody.add("include_external_user_ids", externalUserIdsArray);

        String finalGroupName = (groupName != null && !groupName.isEmpty()) ? groupName : "Your Group";
        String finalSenderName = (senderName != null && !senderName.isEmpty()) ? senderName : "A Member";
        String finalContentPreview = (messageContentPreview != null && !messageContentPreview.isEmpty()) ? messageContentPreview : "[Message]";

        String notificationTitle = "New Message in " + finalGroupName;
        // --- MODIFIED: Use the messageContentPreview provided for the body ---
        // The messageContentPreview is already set to the correct text in SendMsgInfoToDatabase
        String notificationContent = finalSenderName + ": " + finalContentPreview; // Content: "[Sender Name]: [Message Preview]"


        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationTitle)));
        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationContent)));


        // 4. Add custom data (important for handling notification click in the app)
        JsonObject data = new JsonObject();
//        data.addProperty("eventType", "group_message");

        String eventType;
        if ("drawing_session".equals(messageType)) {
            eventType = "group_drawing_link"; // Use a specific event type for group drawing links
            if (!TextUtils.isEmpty(sessionId)) {
                data.addProperty("sessionId", sessionId); // Include the session ID in data for drawing links
                Log.d(TAG, "Including sessionId " + sessionId + " in notification data.");
            } else {
                Log.w(TAG, "Message type is drawing_session but sessionId is null/empty. Cannot include in data payload.");
            }
        } else {
            eventType = "group_message"; // Generic type for other group messages (text, image, etc.)
        }
        data.addProperty("eventType", eventType); // Set the determined event type
        // --- END MODIFIED ---
        //
        // Generic type for group messages
        data.addProperty("groupId", groupId);
        data.addProperty("messageId", messageId);
        data.addProperty("senderId", senderUid);
        data.addProperty("messageType", messageType);

        // --- NEW: Include drawingSessionId if messageType is "drawing_session" and sessionId is provided ---
        if ("drawing_session".equals(messageType) && !TextUtils.isEmpty(sessionId)) {
            data.addProperty("sessionId", sessionId); // Include the session ID in data
            Log.d(TAG, "Including sessionId " + sessionId + " in notification data.");
        } else if ("drawing_session".equals(messageType) && TextUtils.isEmpty(sessionId)) {
            Log.w(TAG, "Message type is drawing_session but sessionId is null/empty. Cannot include in data payload.");
        }
        // --- END NEW ---


        notificationBody.add("data", data);

        notificationBody.addProperty("small_icon", "app_icon_circleup");


        Log.d(TAG, "Making OneSignal API call for group message notification...");
        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "OneSignal API call successful for group message notification. Response Code: " + response.code());
                    try (ResponseBody responseBody = response.body()) {
                        String resBody = responseBody != null ? responseBody.string() : "N/A";
                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read success response body (group noti)", e);
                    }
                } else {
                    Log.e(TAG, "OneSignal API call failed for group message notification. Response Code: " + response.code());
                    try (ResponseBody errorBody = response.errorBody()) {
                        String errBody = errorBody != null ? errorBody.string() : "N/A";
                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read error response body (group noti)", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "OneSignal API call failed (network error) for group message notification", t);
            }
        });
        Log.d(TAG, "OneSignal API call enqueued for group message notification.");
    }
// *** END MODIFIED HELPER METHOD ***


    private void clearSelectedImage() {
        Log.d(TAG, "Clearing selected image.");

        // 1. Clear the Base64 data
        imageToSendBase64 = "";

        // 2. Clean up temporary camera file if it exists
        if (imageToSendUri != null) {
            try {
                // Ensure the URI is one we actually created and need to delete
                // Check against your FileProvider authority string.
                // This check is crucial to avoid deleting random files on the user's device.
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

        // Note: The text input (userMessageInput) is now always visible in the layout,
        // so we don't need to show/hide it here based on image preview visibility.
        // You might want to clear the text input field here *if* you want selecting an
        // image to clear any previously typed text. This is a design choice.
        // For this implementation, let's assume text input remains as is when image preview is shown/hidden.
    }

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
            // After permission is granted, the user needs to click the button again or you can re-attempt here.
            // For simplicity, we rely on the user clicking again.
            Toast.makeText(this, "Storage permission required to open gallery. Please try again after granting.", Toast.LENGTH_LONG).show();
            return; // Exit if permission is needed
        }

        // Permission is granted or not needed for this API level, proceed to launch gallery
        try {
            // Use ACTION_PICK with MediaStore.Images.Media.EXTERNAL_CONTENT_URI for selecting from gallery
            // Or use ACTION_GET_CONTENT to let user choose from various sources (gallery, cloud storage etc.)
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Optional: Specify image types if needed
            // intent.setType("image/*");

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

    // Inside GroupChatActivity.java

// --- NEW Helper Method: Launch Camera Intent (Called AFTER permission check) ---
    /**
     * Prepares a temporary URI using FileProvider and launches the camera intent
     * using the registered takePictureLauncher.
     */
    private void launchCameraIntent() {
        Log.d(TAG, "Preparing and launching camera intent.");
        try {
            // 1. Create a temporary File object where the camera should save the picture.
            // This uses your existing createImageFile() method.
            File photoFile = null;
            try {
                photoFile = createImageFile(); // Use your helper to create temp file
            } catch (IOException ex) {
                Log.e(TAG, "Error creating temp image file for camera", ex);
                // Ensure we're on the main thread for Toast and UI updates
                runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Error preparing for photo.", Toast.LENGTH_SHORT).show());
                // Clean up any state if file creation failed
                clearSelectedImage(); // Ensure state is clean by calling the helper method
                return; // Exit the method if file creation fails
            }

            // 2. If the temporary file was created successfully, get its secure content:// URI using FileProvider.
            if (photoFile != null) {
                try {
                    // Get the secure content:// URI using FileProvider.
                    // Pass Context, your FileProvider authority string, and the File object.
                    // The authority string MUST match the one defined in your AndroidManifest.xml.
                    // Assuming your authority is getApplicationContext().getPackageName() + ".fileprovider"
                    String fileProviderAuthority = getApplicationContext().getPackageName() + ".fileprovider"; // *** Use your actual authority ***

                    Uri photoURI = FileProvider.getUriForFile(
                            this, // Context
                            fileProviderAuthority, // *** YOUR FILEPROVIDER AUTHORITY STRING HERE (e.g., "com.sana.circleup.fileprovider") ***
                            photoFile // The java.io.File object
                    );

                    // 3. Store this URI in the member variable imageToSendUri.
                    // This is important so we can access it later in the launcher callback
                    // to process the image or clean it up if cancelled/failed.
                    imageToSendUri = photoURI;
                    Log.d(TAG, "Prepared temporary URI for camera using FileProvider: " + imageToSendUri);

                    // 4. Create the camera intent (ACTION_IMAGE_CAPTURE).
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // 5. Specify where the camera app should save the picture by passing the URI.
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageToSendUri);

                    // 6. Grant necessary permissions to the camera app to write to this URI.
                    // These flags are essential for the camera app to access the URI.
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Also good practice to grant read permission


                    // 7. Verify that there is a camera app available to handle this intent.
                    // resolveActivity returns null if no app can handle the intent.
                    // Check against the package manager.
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        Log.d(TAG, "Starting camera intent with URI: " + imageToSendUri);
                        // 8. Launch the camera activity using the registered launcher with the URI.
                        takePictureLauncher.launch(imageToSendUri); // Launch the activity using the URI

                    } else {
                        // No camera app found to handle the intent
                        Log.e(TAG, "No camera app found to handle ACTION_IMAGE_CAPTURE.");
                        // Ensure we're on the main thread for Toast
                        runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "No camera app found.", Toast.LENGTH_SHORT).show());
                        // Clean up the created file if intent cannot be resolved
                        try {
                            if (photoFile.exists()) {
                                // Try deleting the file if it was created but couldn't be used
                                photoFile.delete();
                                Log.d(TAG, "Cleaned up temp camera file as camera app not found.");
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Failed to clean up camera file on no app found", e);
                        }
                        clearSelectedImage(); // Clear state on failure
                    }
                } catch (IllegalArgumentException e) {
                    // This exception can occur if the FileProvider authority or paths are misconfigured
                    Log.e(TAG, "FileProvider setup error or file issue: " + e.getMessage(), e);
                    // Ensure we're on the main thread for Toast
                    runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Error accessing file storage. Check configuration.", Toast.LENGTH_LONG).show());
                    clearSelectedImage(); // Clear state on failure
                } catch (Exception e) {
                    // Catch any other unexpected error during URI creation or intent setup
                    Log.e(TAG, "Unexpected error setting up camera intent.", e);
                    // Ensure we're on the main thread for Toast
                    runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Error accessing camera.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state on failure
                }
            } else {
                // This case should ideally be caught by the inner try-catch around createImageFile(),
                // but included here for robustness.
                Log.e(TAG, "Photo file was null after creation attempt. Cannot launch camera.");
                runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Error preparing for photo.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on failure
            }
        }catch (Exception e) {
            Log.e(TAG, "Error creating URI for camera picture or launching intent.", e);
            // This catch block handles potential exceptions during URI creation or intent launch
            Toast.makeText(this, "Error accessing camera.", Toast.LENGTH_SHORT).show();
            imageToSendUri = null; // Ensure URI is null on failure
        } // No extra catch block needed here. The method definition ends here.
// --- END NEW Helper Method launchCameraIntent ---
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

    private void processSelectedImageForSending(@Nullable Uri uri) {
        // --- Step 1: Clear any previously selected image state ---
        // This also handles cleanup of temporary camera files
        clearSelectedImage(); // Clear the previous state FIRST

        // Check if the URI is null after clearing
        if (uri == null) {
            Log.w(TAG, "processSelectedImageForSending called with null URI after clearing previous state.");
            // Inform the user on the UI thread
            runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
            return; // Exit if the URI is null
        }

        Log.d(TAG, "Processing selected/captured image for sending. URI: " + uri);

        // --- Step 2: Perform image processing on a background thread ---
        // Image decoding, resizing, and encoding can be time-consuming.
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
                    runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
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
                    runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
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
                    runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Failed to encode image data.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state if encoding fails
                    return; // Exit if encoding fails
                }

                // --- Step 3: Image Processed Successfully ---
                Log.d(TAG, "Image processed and Base64 stored. Length: " + base64Image.length() + ". Ready for sending. Showing preview.");

                // Store the resulting Base64 string in the member variable.
                GroupChatActivity.this.imageToSendBase64 = base64Image; // Use the stored Base64

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
                    GroupChatActivity.this.imageToSendUri = uri; // Keep track of the temp URI for cleanup
                    Log.d(TAG, "Keeping temporary URI for cleanup: " + imageToSendUri);
                } else {
                    // This URI is from gallery or another non-temporary source, no need to track for cleanup
                    GroupChatActivity.this.imageToSendUri = null;
                }


                // --- Step 4: Update the UI to show the image preview (on Main Thread) ---
                runOnUiThread(() -> {
                    try {
                        // Use Glide to load the selected URI into the preview ImageView
                        // Load the original URI (not Base64) for better quality in the preview if possible
                        if (imgPreview != null) {
                            Glide.with(GroupChatActivity.this) // Use Activity context
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
                        Toast.makeText(GroupChatActivity.this, "Image ready to send.", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Log.e(TAG, "Error showing image preview on Main Thread", e);
                        // If showing preview fails, treat as processing failure and clear state
                        runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Error showing image preview.", Toast.LENGTH_SHORT).show());
                        clearSelectedImage(); // Clear state if preview fails
                    }
                });


            } catch (IOException e) {
                // Handle file reading errors
                Log.e(TAG, "Image processing failed (IOException) for sending.", e);
                runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on processing failure
            } catch (SecurityException e) {
                // Handle permission errors when accessing the URI
                Log.e(TAG, "Security Exception during image processing (missing permissions?).", e);
                runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "Permission error accessing image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on processing failure
            }
            catch (Exception e) {
                // Catch any other unexpected errors during processing (decoding, etc.)
                Log.e(TAG, "Unexpected error during image processing for sending.", e);
                runOnUiThread(() -> Toast.makeText(GroupChatActivity.this, "An error occurred processing image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on processing failure
            }
        }).start(); // Start the background thread
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

    private void attemptSendMessage() {
        String messageText = userMessageInput.getText().toString().trim();
        boolean isImageStaged = !TextUtils.isEmpty(imageToSendBase64);

        if (TextUtils.isEmpty(messageText) && !isImageStaged) {
            Toast.makeText(this, "Please enter a message or select an image!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Attempted send with empty text and no staged image. Skipping send.");
            return;
        }

        if (userMessageInput != null && !userMessageInput.isEnabled()) {
            Toast.makeText(this, "Cannot send messages in this group.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Attempted send blocked: Message input UI is disabled.");
            return;
        }

        Log.d(TAG, "Attempting to send message(s). Text available: " + (!TextUtils.isEmpty(messageText)) + ", Image staged: " + isImageStaged);

        if (!TextUtils.isEmpty(messageText)) {
            Log.d(TAG, "Initiating text message send.");
            // Pass "text" as type, the trimmed text content, and null for both session IDs.
            SendMsgInfoToDatabase("text", messageText, null, null); // <<< Added null for sessionIdForNotification
            userMessageInput.setText("");
        }

        if (isImageStaged) {
            Log.d(TAG, "Initiating image message send.");
            // Pass "image" as type, the staged Base64 string as content, and null for both session IDs.
            SendMsgInfoToDatabase("image", imageToSendBase64, null, null); // <<< Added null for sessionIdForNotification
        }

        if (isImageStaged) {
            clearSelectedImage();
            Log.d(TAG, "Staged image cleared after initiating send.");
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handle results for permission requests initiated by ActivityCompat.requestPermissions
        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean allPermissionsGranted = true;
            // Check if all requested permissions in this batch were granted
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break; // If any permission is denied, the whole batch is considered not granted
                }
            }

            if (allPermissionsGranted) {
                Log.d(TAG, "Permissions granted.");
                // Inform the user they can now proceed, or potentially re-trigger the action
                // Example: Toast.makeText(this, "Permissions granted. You can now pick/take photos.", Toast.LENGTH_SHORT).show();
                // Note: For camera permission requested via launcher, the launcher's callback handles re-triggering launchCameraIntent.
                // For gallery permission via ActivityCompat, the user usually needs to click the button again.
            } else {
                Log.d(TAG, "Permissions denied.");
                // Inform the user that functionality requiring these permissions might not work
                Toast.makeText(this, "Permissions denied. Cannot access storage or camera for photos.", Toast.LENGTH_SHORT).show();
            }
        }
        // Handle results for other permission request codes if you have them
    }
}