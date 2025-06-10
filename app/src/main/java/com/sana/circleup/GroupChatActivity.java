package com.sana.circleup;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import com.sana.circleup.room_db_implement.GroupMessageDao;
import com.sana.circleup.room_db_implement.GroupMessageEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//
//public class GroupChatActivity extends AppCompatActivity {
//
//    private static final String TAG = "GroupChatActivity"; // Define TAG
//    private static final int IMAGE_PICK_REQUEST = 1; // Constant for image picker request code
//
//    // UI Elements
//    private Toolbar toolbar; // Toolbar
//    private ImageButton sendMessageButton, send_imgmsg_btn; // Input buttons
//    private EditText userMessageInput; // Message input field
//    private RecyclerView messagesRecyclerView; // RecyclerView for messages
//    private ImageView groupMenu, groupImageView; // Toolbar views
//    private TextView groupNameTextView; // TextView for group name in toolbar
//
//    // Firebase
//    private FirebaseAuth auth; // Firebase Auth
//    private DatabaseReference rootRef; // Root Firebase DB Reference
//    private DatabaseReference usersRef; // Reference to /Users node (Used for fetching sender profiles in adapter)
//    private DatabaseReference groupRef; // Reference to the specific group node
//    private DatabaseReference messagesRef; // Reference to the messages node within the group
//
//    // User & Group Info
//    private String groupId; // ID of the current group (from Intent)
//    private String currentUserID; // Current logged-in user's UID
//    private String currentUserName; // Current user's display name (Fetched async)
//
//    // --- NEW Room DB and DAO members ---
//    private ChatDatabase db; // Room Database instance
//    private GroupMessageDao groupMessageDao; // DAO for GroupMessageEntity
//    private LiveData<List<GroupMessageEntity>> groupMessagesLiveData; // LiveData for group messages from Room DB
//    private ExecutorService databaseExecutor; // Use the shared executor from ChatDatabase for Room ops
//    // --- End NEW Room DB and DAO ---
//
//
//    // RecyclerView and Adapter
//    // messagesList is now the list held by the standard adapter, populated by LiveData from Room
//    // This list holds the data currently displayed in the RecyclerView.
//    private final List<GroupMessageEntity> messagesList = new ArrayList<>();
//    private GroupMessageAdapter messageAdapter; // Standard RecyclerView Adapter for Room data
//
//
//    // *** NEW MEMBER VARIABLES ***
//    // Store the list of member UIDs for sending notifications
//    private List<String> groupMemberUids = new ArrayList<>();
//    // Listener to keep the members list updated
//    private ValueEventListener groupMembersListener;
//    // Store the group name for notifications (already fetched in loadGroupInfo)
//    private String groupName;
//
//    // Retrofit Service for OneSignal API
//    private OneSignalApiService oneSignalApiService;
//    // Get this from your OneSignal Dashboard -> Settings -> Keys & IDs
//    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR ACTUAL APP ID
//    // *** END NEW MEMBER VARIABLES ***
//
//    // Deprecated UI elements (Remove these from layout and code if they are not used)
//    // private ScrollView scrollView;
//    // private TextView displayMessageText;
//
//    // Firebase Listener for Sync (NEW)
//    private ChildEventListener groupMessagesChildEventListener; // Listener to sync messages from Firebase to Room
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        // Assuming your layout is activity_group_chat and includes appbar_groupmain.xml
//        setContentView(R.layout.activity_group_chat);
//
//        Log.d(TAG, "🟢 GroupChatActivity launched");
//
//        // Initialize Firebase Auth
//        auth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = auth.getCurrentUser();
//
//        // Check if user is authenticated. Redirect to login if not.
//        if (currentUser == null) {
//            Log.w(TAG, "No current user authenticated. Redirecting to Login.");
//            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
//            sendUserToLoginActivity(); // Navigate to login and finish
//            return; // Stop further execution
//        }
//
//        currentUserID = currentUser.getUid(); // Get current user ID
//        rootRef = FirebaseDatabase.getInstance().getReference(); // Initialize rootRef
//
//        // Initialize users reference (for fetching current user's name and receiver profile images in adapter)
//        usersRef = rootRef.child("Users"); // Reference to the base /Users node
//
//
//        // Get group ID from Intent. Group ID is essential.
//        groupId = getIntent().getStringExtra("groupId");
//        if (groupId == null || groupId.isEmpty()) { // Added check for empty groupId
//            Log.e(TAG, "Error: Group ID missing or empty from Intent!");
//            Toast.makeText(this, "Error: Group ID missing!", Toast.LENGTH_SHORT).show();
//            finish(); // Close the activity
//            return; // Stop further execution
//        }
//
//        // Initialize group and messages references using the obtained groupId
//        groupRef = rootRef.child("Groups").child(groupId); // Reference to the specific group node in Firebase
//        messagesRef = groupRef.child("Messages"); // Reference to the messages node within the group in Firebase
//
//
//        // Initialize Room DB and DAO (NEW)
//        db = ChatDatabase.getInstance(getApplicationContext()); // Use application context for database
//        groupMessageDao = db.groupMessageDao(); // Get the DAO for group messages
//        databaseExecutor = ChatDatabase.databaseWriteExecutor; // Use the shared DB executor for Room ops
//
//
//        // Initialize UI elements (Keep This, Ensure IDs Match Layout)
//        InitializeFields();
//
//
//        // *** NEW: Initialize Retrofit Service for OneSignal API ***
//        try {
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl("https://onesignal.com/") // OneSignal API Base URL (DO NOT CHANGE THIS)
//                    .addConverterFactory(GsonConverterFactory.create()) // For JSON handling
//                    .build();
//            // Create an instance of your API service interface. API key is set in OneSignalApiService.java.
//            oneSignalApiService = retrofit.create(OneSignalApiService.class);
//            Log.d(TAG, "OneSignalApiService initialized.");
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to initialize OneSignalApiService", e);
//            // Handle this error - perhaps disable send request button if notifications are critical
//            // SendMessageRequestButton.setEnabled(false);
//            Toast.makeText(this, "Error initializing notification service.", Toast.LENGTH_SHORT).show();
//        }
//        // *** END NEW ***
//
//
//
//        // Set up UI Listeners (Keep These)
//        // Group Menu icon click listener
//        if (groupMenu != null) {
//            groupMenu.setOnClickListener(this::showPopupMenu); // Use method reference
//        } else {
//            Log.w(TAG, "Group menu button is null, cannot set click listener.");
//        }
//
//        // Send Text Message button click listener
//        sendMessageButton.setOnClickListener(view -> {
//            String messageContent = userMessageInput.getText().toString();
//            if (TextUtils.isEmpty(messageContent)) {
//                Toast.makeText(this, "Please write a message first", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            // Send text message using the dedicated method
//            SendMsgInfoToDatabase("text", messageContent); // Specify type and content
//            userMessageInput.setText(""); // Clear input field after sending
//        });
//
//        // Send Image Message button click listener
//        send_imgmsg_btn.setOnClickListener(v -> {
//            // Open image picker intent using ACTION_GET_CONTENT
//            Intent intent = new Intent();
//            intent.setType("image/*"); // Specify image type
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_PICK_REQUEST); // Use constant request code
//        });
//
//
//        // Check user membership before proceeding with data loading/sending
//        // This is important for security to prevent unauthorized access to group messages.
//        // The rest of the setup (data loading, listeners) will proceed after this check if successful (by not finishing).
//        checkUserMembership();
//
//
//        // Fetch current user's username from Firebase (Needed for sending messages and adapter display)
//        // This is an asynchronous operation.
//        GetUserInfo();
//
//        // Load initial group info (name, image) from Firebase
//        // This is an asynchronous operation.
//        loadGroupInfo();
//
//        // Start loading messages from Room DB and set up Firebase sync listener
//        // This is the core Room/Firebase sync setup for message display.
//        LoadMessages();
//
//
//        // *** NEW: Attach Listener for Group Members on Start ***
//        // We need an up-to-date list of members for sending notifications.
//        attachGroupMembersListener();
//        // *** END NEW ***
//
//
//        Log.d(TAG, "📲 onCreate finished in GroupChatActivity");
//    }
//
//    // --- Activity Lifecycle Methods ---
//    // onStart and onStop can be used to manage listeners or resources needed only when activity is visible/started.
//    // onDestroy is for final cleanup.
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.d(TAG, "GroupChatActivity onStart");
//        // Firebase message sync listener is attached in LoadMessages (which is called in onCreate).
//        // If you prefer syncing ONLY when the activity is started, move the call to
//        // attachGroupMessagesSyncListener() from LoadMessages to here (onStart).
//
//        // *** NEW: Remove Listener for Group Members on Stop ***
//        // Stop listening for membership changes when the activity is not visible.
//        removeGroupMembersListener();
//        // *** END NEW ***
//        // Room LiveData observation is already handled by using `this` lifecycle in LoadMessages.
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.d(TAG, "GroupChatActivity onStop");
//        // Firebase message sync listener is removed in onDestroy in this version.
//        // If you want to stop syncing when activity is stopped (e.g., goes to background),
//        // move removeGroupMessagesSyncListener() here and re-attach in onStart.
//        // removeGroupMessagesSyncListener(); // Example if moving removal here
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "GroupChatActivity onDestroy called.");
//
//        // --- Remove Firebase Listener ---
//        // Remove the messages sync listener to prevent memory leaks and unnecessary Firebase reads
//        // when the activity is destroyed.
//        removeGroupMessagesSyncListener();
//
//        // LiveData observer is automatically removed because we used `this` (the Activity lifecycle)
//        // when calling `observe`. Explicit removal is not strictly necessary but is an option:
//        // if (groupMessagesLiveData != null) groupMessagesLiveData.removeObservers(this);
//
//        // Room DB executor is managed by ChatDatabase singleton, no need to shut down here.
//
//        // Dismiss progress dialog if it was used and is showing to prevent window leaks
//        // if (progressDialog != null && progressDialog.isShowing()) { progressDialog.dismiss(); }
//    }
//    // --- End Activity Lifecycle Methods ---
//
//
//    // Navigate to login activity (Keep This Helper Method)
//    private void sendUserToLoginActivity() {
//        Log.d(TAG, "Redirecting to Login Activity.");
//        Intent loginIntent = new Intent(GroupChatActivity.this, Login.class); // Replace Login with your actual Login Activity class
//        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
//        startActivity(loginIntent);
//        finish(); // Close this activity after redirecting
//    }
//
//
//    // Initialize UI elements (Keep This, Ensure IDs Match Layout)
//    // Assumes your activity_group_chat.xml includes a layout file like appbar_groupmain.xml
//    // where the toolbar and its internal views are defined.
//    private void InitializeFields() {
//        Log.d(TAG, "Initializing UI elements.");
//        // Initialize Toolbar and find it by its ID in the main activity layout
//        toolbar = findViewById(R.id.group_chat_bar_layout); // Ensure this ID matches your layout
//
//        // !!! Crucial Null Check for Toolbar !!! It's essential for the UI.
//        if (toolbar == null) {
//            Log.e(TAG, "CRITICAL ERROR: Toolbar not found in layout (R.id.group_chat_bar_layout)!");
//            Toast.makeText(this, "Toolbar setup error.", Toast.LENGTH_SHORT).show();
//            finish(); // Toolbar is essential, finish activity
//            return; // Stop initialization process
//        }
//
//        // Set the found toolbar as the ActionBar for this activity
//        setSupportActionBar(toolbar);
//        // Hide the default title provided by the ActionBar if you are using custom views inside the toolbar
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
//        }
//
//        // Find the custom views located *inside* the toolbar layout
//        groupNameTextView = toolbar.findViewById(R.id.group_name); // Ensure this ID matches your toolbar layout
//        groupImageView = toolbar.findViewById(R.id.group_profile_image); // Ensure this ID matches your toolbar layout
//        groupMenu = toolbar.findViewById(R.id.group_menu); // Ensure this ID matches your toolbar layout
//
//
//        // !!! Null checks for Toolbar's internal views (if they are optional in your layout) !!!
//        // Log warnings if these expected views are missing, as they might cause NullPointerExceptions later.
//        if (groupNameTextView == null)
//            Log.w(TAG, "groupNameTextView (R.id.group_name) not found in toolbar layout!");
//        if (groupImageView == null)
//            Log.w(TAG, "groupImageView (R.id.group_profile_image) not found in toolbar layout!");
//        if (groupMenu == null)
//            Log.w(TAG, "groupMenu (R.id.group_menu) not found in toolbar layout!");
//
//
//        // Initialize message input and send buttons by finding them in the main activity layout
//        sendMessageButton = findViewById(R.id.send_msg_button); // Ensure your send text button ID exists
//        userMessageInput = findViewById(R.id.input_group_msg); // Ensure your input EditText ID exists
//        send_imgmsg_btn = findViewById(R.id.send_imgmsg_btn); // Ensure your send image button ID exists
//
//        // Initialize RecyclerView by finding it in the main activity layout
//        messagesRecyclerView = findViewById(R.id.group_chat_recycler_view); // Ensure your RecyclerView ID exists
//
//        // Initialize Adapter (NEW)
//        // The adapter is initialized with an empty list because the actual message data will be
//        // provided asynchronously by the Room LiveData observer.
//        // Pass necessary info: empty list, activity context, current user ID, and initially null/empty username.
//        // The username will be updated in the adapter once fetched by GetUserInfo.
//        messageAdapter = new GroupMessageAdapter(new ArrayList<>(), this, currentUserID, null);
//
//        // Setup RecyclerView LayoutManager and Adapter
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setStackFromEnd(true); // Show latest messages at the bottom
//        messagesRecyclerView.setLayoutManager(linearLayoutManager); // Set the layout manager
//        messagesRecyclerView.setAdapter(messageAdapter); // Set the adapter to the RecyclerView
//
//
//        // Removed references to deprecated ScrollView and displayMessageText - Ensure these are removed from your layout if not used.
//        // if (scrollView != null) scrollView.setVisibility(View.GONE);
//        // if (displayMessageText != null) displayMessageText.setVisibility(View.GONE);
//
//        Log.d(TAG, "InitializeFields finished.");
//    }
//
//
//    // Load group info (name, image) from Firebase (Keep This Helper Method)
//    // Fetches group profile information from Firebase and updates the toolbar UI.
//    private void loadGroupInfo() {
//        // Check if essential Firebase reference and UI views are initialized and valid
//        if (groupRef == null || groupNameTextView == null || groupImageView == null) {
//            Log.e(TAG, "loadGroupInfo: groupRef or UI elements are null.");
//            // Decide how to handle this critical error (e.g., disable chat, show error)
//            return;
//        }
//        Log.d(TAG, "Loading group info from Firebase for ID: " + groupId);
//        // Use addListenerForSingleValueEvent as group info doesn't change very often
//        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // This callback runs on the main thread
//                if (snapshot.exists()) {
//                    // Group data found, extract name and image
//                    String groupName = snapshot.child("groupName").getValue(String.class);
//                    String groupPic = snapshot.child("groupImage").getValue(String.class); // Assume image is stored as Base64 string
//
//
//                    // *** NEW: Populate groupName member variable ***
//                    GroupChatActivity.this.groupName = groupName; // Populate member variable
//                    // *** END NEW ***
//
//                    // Update Toolbar views on the main thread
//                    if (groupName != null && !groupName.isEmpty()) { // Check for empty name too
//                        groupNameTextView.setText(groupName);
//                    } else {
//                        groupNameTextView.setText("Unnamed Group"); // Default name if missing/empty
//                    }
//
//                    // Update Group Profile Picture using Base64 decoding and Glide (or manual)
//                    if (groupImageView != null) { // Ensure ImageView is not null
//                        if (groupPic != null && !groupPic.isEmpty()) {
//                            // Use Glide to load Base64 image (more robust)
//                            // Assuming Base64 is just the string, add data URI prefix
//                            String glideLoadableImage = "data:image/jpeg;base64," + groupPic; // Assuming JPEG
//
//                            // Load image using Glide
//                            Glide.with(GroupChatActivity.this) // Use Activity context
//                                    .load(glideLoadableImage) // Load from Base64 data URI
//                                    .placeholder(R.drawable.default_group_img) // Placeholder image
//                                    .error(R.drawable.default_group_img) // Error image
//                                    .into(groupImageView);
//                        } else {
//                            // Default image if no profile pic data or data is empty
//                            groupImageView.setImageResource(R.drawable.default_group_img);
//                        }
//                    }
//                    Log.d(TAG, "Group info loaded: Name=" + groupName + ", Image=" + (groupPic != null ? groupPic.length() + " bytes" : "null"));
//
//                } else {
//                    // Group data not found in Firebase (e.g., deleted)
//                    Log.w(TAG, "Group data not found in Firebase for ID: " + groupId);
//                    // Show error state and disable chat functionality
//                    groupNameTextView.setText("Group Not Found"); // Update UI
//                    groupImageView.setImageResource(R.drawable.default_group_img); // Update UI
//                    Toast.makeText(GroupChatActivity.this, "Group does not exist or was deleted.", Toast.LENGTH_LONG).show();
//                    // Disable sending messages if group is not found
//                    userMessageInput.setEnabled(false);
//                    sendMessageButton.setEnabled(false);
//                    send_imgmsg_btn.setEnabled(false);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to load group info from Firebase: " + error.getMessage());
//                Toast.makeText(GroupChatActivity.this, "Failed to load group info", Toast.LENGTH_SHORT).show();
//                // Show error state and disable chat functionality
//                if (groupNameTextView != null) groupNameTextView.setText("Error Loading Group");
//                if (groupImageView != null)
//                    groupImageView.setImageResource(R.drawable.default_group_img);
//                // Disable sending messages on error
//                userMessageInput.setEnabled(false);
//                sendMessageButton.setEnabled(false);
//                send_imgmsg_btn.setEnabled(false);
//            }
//        });
//    }
//
//
//    // Fetch the current user's username from Firebase (Keep This Helper Method)
//    // Needed for sending messages (displaying sender name) and adapter display logic.
//    private void GetUserInfo() {
//        // Check if usersRef and currentUserID are initialized
//        if (usersRef == null || currentUserID == null) {
//            Log.e(TAG, "GetUserInfo: usersRef or currentUserID is null.");
//            // Set default username and inform user if necessary
//            currentUserName = "Unknown";
//            if (messageAdapter != null) messageAdapter.updateCurrentUserName(currentUserName);
//            return;
//        }
//        Log.d(TAG, "Fetching current user info for UID: " + currentUserID);
//        // Fetch current user's details using SingleValueEvent (efficient for one-time fetch)
//        usersRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // This callback runs on the main thread
//                if (snapshot.exists() && snapshot.hasChild("username")) {
//                    currentUserName = snapshot.child("username").getValue(String.class);
//                    // Handle potential null username
//                    if (currentUserName == null || currentUserName.isEmpty()) {
//                        currentUserName = "Unknown";
//                        Log.w(TAG, "Username node found but value is null/empty for user: " + currentUserID);
//                    }
//                    Log.d(TAG, "Fetched current username: " + currentUserName);
//                    // Update the adapter with the correct username once fetched (e.g., for "You" display logic)
//                    if (messageAdapter != null) {
//                        messageAdapter.updateCurrentUserName(currentUserName);
//                        // If adapter's display logic for "You" vs name changes based on this,
//                        // you might need to notify the adapter to rebind visible items.
//                        // messageAdapter.notifyDataSetChanged(); // Inefficient full refresh
//                    }
//                } else {
//                    // Username node is missing or user data snapshot doesn't exist
//                    currentUserName = "Unknown"; // Default name
//                    Log.w(TAG, "Username not found in Firebase for current user: " + currentUserID);
//                    if (messageAdapter != null) {
//                        messageAdapter.updateCurrentUserName(currentUserName);
//                    }
//                    // Optionally update UI or inform user if their username is missing
//                    Toast.makeText(GroupChatActivity.this, "Your username not found, sending as Unknown.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Error retrieving current user data from Firebase: " + error.getMessage());
//                Toast.makeText(GroupChatActivity.this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
//                currentUserName = "Unknown"; // Set to Unknown on error
//                if (messageAdapter != null) {
//                    messageAdapter.updateCurrentUserName(currentUserName);
//                }
//                // Optionally update UI or inform user on error
//            }
//        });
//    }
//
//
//    // --- MODIFIED: Send message info to Database ---
//    // This method prepares message data (text or image Base64), gets a Firebase key,
//    // and writes the message to the group's messages node in Firebase.
//    // The Firebase sync listener will pick it up and save it to Room.
//    // It also updates the readBy map initially for the sender.
//    private void SendMsgInfoToDatabase(String messageType, String messageContent) {
//        // messageContent should be already validated and processed (text string or Base64 image string)
//        if (TextUtils.isEmpty(messageContent)) {
//            Log.w(TAG, "Attempted to send empty message content. Skipping.");
//            return; // Do not send empty messages
//        }
//
//        // Check if essential references and sender info are available
//        if (messagesRef == null || currentUserID == null || currentUserName == null) {
//            Log.e(TAG, "messagesRef, currentUserID, or currentUserName is null, cannot send message.");
//            Toast.makeText(this, "Error sending message: Missing sender info or reference.", Toast.LENGTH_SHORT).show();
//            return; // Stop if essential data is missing
//        }
//        Log.d(TAG, "Preparing to send " + messageType + " message.");
//
//        // Generate a unique push ID for the new message in Firebase
//        // This ID will serve as the primary key in the Room DB entity.
//        String messageKey = messagesRef.push().getKey();
//
//        if (messageKey == null) {
//            Log.e(TAG, "Failed to generate unique message key from Firebase.");
//            Toast.makeText(this, "Error sending message: Failed to generate ID.", Toast.LENGTH_SHORT).show();
//            return; // Stop if key generation fails
//        }
//        Log.d(TAG, "Generated Firebase push ID: " + messageKey);
//
//        // Prepare the message data as a HashMap to send to Firebase
//        HashMap<String, Object> messageInfoMap = new HashMap<>();
//        messageInfoMap.put("message", messageContent); // Message content (text string or Base64 image string)
//        messageInfoMap.put("type", messageType); // Message type ("text" or "image")
//        messageInfoMap.put("name", currentUserName); // Sender's display name (fetched)
//        messageInfoMap.put("senderId", currentUserID); // Sender's UID
//
//        // Add server timestamp - CRUCIAL for sorting messages correctly across all users and devices
//        messageInfoMap.put("timestamp", ServerValue.TIMESTAMP); // Use Firebase ServerValue
//
//        // Add initial readBy map - The sender has read their own message upon sending
//        HashMap<String, Object> readByMap = new HashMap<>();
//        readByMap.put(currentUserID, true); // Mark sender as read with boolean true
//        messageInfoMap.put("readBy", readByMap); // This will be stored as a nested map in Firebase
//
//        // Add client-side date and time (Optional, primarily for display history or if timestamp fails)
//        // Use current local time/date - Note: timestamp is more reliable for sorting
//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()); // Use default locale
//        String currentDate = dateFormat.format(calendar.getTime());
//        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Use default locale
//        String currentTime = timeFormat.format(calendar.getTime());
//
//        messageInfoMap.put("date", currentDate); // Client-side date string
//        messageInfoMap.put("time", currentTime); // Client-side time string
//
//
//        // Write the message data to Firebase using the generated key (Asynchronous operation)
//        messagesRef.child(messageKey).setValue(messageInfoMap)
//                .addOnSuccessListener(aVoid -> {
//                    // This callback runs on the main thread after the write is successful on the server
//                    Log.d(TAG, messageType + " message sent successfully to Firebase with key: " + messageKey);
//                    // Message sent toast is often annoying in chat, maybe remove in production
//                    // Toast.makeText(GroupChatActivity.this, "Sent!", Toast.LENGTH_SHORT).show();
//
//                    // The Firebase ChildEventListener attached in LoadMessages will automatically pick up this message
//                    // (even the one you sent yourself) via onChildAdded and sync it into Room DB.
//                    // The Room LiveData observer will then update the UI.
//
//
//
//
//
//                    // *** NEW: Send Push Notification to the RECIPIENTS (other group members) ***
//                    // Call the helper method here AFTER Firebase confirms write
//                    if (oneSignalApiService != null && currentUserName != null && !currentUserName.isEmpty() && groupName != null && !groupName.isEmpty()) {
//                        Log.d(TAG, "Firebase message sent. Calling sendGroupPushNotification.");
//                        // Pass the list of member UIDs (excluding sender), sender name, group name, message content/preview, etc.
//                        // Use a preview string for the notification content
//                        String notificationContentPreview = (messageType.equals("text")) ? messageContent : "[" + messageType + "]"; // Use actual text or type placeholder
//                        sendGroupPushNotification(
//                                oneSignalApiService,
//                                groupMemberUids, // List of all member UIDs
//                                currentUserID, // Sender's UID (to exclude from recipients)
//                                currentUserName, // Sender's display name
//                                groupName, // Group name
//                                notificationContentPreview, // Message preview for notification
//                                groupId, // Group ID for custom data
//                                messageKey, // Message ID for custom data
//                                messageType // Message type for custom data
//                        );
//                    } else {
//                        Log.e(TAG, "OneSignalApiService, currentUserName, or groupName is null/empty. Cannot send group push notification.");
//                    }
//                    // *** END NEW ***
//
//
//
//                    // Optionally update Chat Summaries here if you have them for groups (less common than 1:1)
//                    // updateGroupChatSummary(...); // If you have a summary node for groups
//
//
//
//
//                })
//                .addOnFailureListener(e -> {
//                    // This callback runs on the main thread if the Firebase write fails
//                    Log.e(TAG, messageType + " message failed to send to Firebase.", e);
//                    Toast.makeText(GroupChatActivity.this, "Message failed to send.", Toast.LENGTH_SHORT).show();
//                    // In a real app, you might save failed message locally in Room with a "failed" status
//                    // and show a "retry" indicator in the UI.
//                    // This would require adding a 'status' field to GroupMessageEntity and updating Room here.
//                    // saveMessageToRoomLocallyWithStatus(messageKey, groupId, messageContent, messageType, currentUserName, currentUserID, ServerValue.TIMESTAMP, currentDate, currentTime, readByMap, "failed");
//                });
//    }
//
//    // Helper method for sending image messages (calls the main SendMsgInfoToDatabase)
//    // Handles the image picking result and triggers the actual sending process.
//    private void sendImageMessageToDatabase(String encodedImage) {
//        if (TextUtils.isEmpty(encodedImage)) {
//            Toast.makeText(this, "Could not process image for sending", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        // Pass the encoded image (Base64 string) as message content and type "image"
//        SendMsgInfoToDatabase("image", encodedImage); // Use the main SendMsgInfoToDatabase with two arguments
//    }
//
//
//    // --- MODIFIED: Load messages from Room and set up Firebase sync listener ---
//    // This method sets up the data flow: Room LiveData -> Adapter + Firebase Sync Listener -> Room DB.
//    private void LoadMessages() {
//        // Check if essential references are initialized
//        if (groupId == null || messagesRef == null || groupMessageDao == null || messagesRecyclerView == null || messageAdapter == null) {
//            Log.e(TAG, "LoadMessages: Essential components are null. Cannot load messages.");
//            // Optionally finish or show error state
//            // finish();
//            return;
//        }
//        Log.d(TAG, "Loading messages for group: " + groupId);
//
//        // --- Step 1: Load messages from Room DB using LiveData ---
//        // Get LiveData for messages for the specific group, ordered by timestamp (ascending).
//        // This query is defined in GroupMessageDao. It will immediately load any existing offline data
//        // and the observer will keep the UI updated whenever Room data changes.
//        groupMessagesLiveData = groupMessageDao.getMessagesForGroup(groupId);
//
//        // Observe the LiveData. 'this' refers to the Activity, using its lifecycle.
//        // The observer will be automatically removed when the activity is destroyed.
//        groupMessagesLiveData.observe(this, new Observer<List<GroupMessageEntity>>() { // Observe using 'this' in Activity
//            @Override
//            public void onChanged(List<GroupMessageEntity> messagesFromRoom) {
//                // This callback runs on the main thread whenever the data in the Room DB changes for this query.
//                Log.d(TAG, "Group Messages LiveData updated with " + (messagesFromRoom != null ? messagesFromRoom.size() : 0) + " messages from Room for group: " + groupId);
//
//                // Update the adapter's list with the new data from Room.
//                // The list from Room is already sorted by timestamp due to the DAO query.
//                // messagesList.clear(); // The adapter's setMessages method should handle clearing its internal list
//                // if (messagesFromRoom != null) messagesList.addAll(messagesFromRoom);
//                messageAdapter.setMessages(messagesFromRoom); // Use the setMessages method in the adapter
//
//                // Scroll to the bottom when messages are loaded/updated (only if list is not empty)
//                // This ensures the latest messages are visible upon initial load or when new messages arrive.
//                if (!messagesFromRoom.isEmpty()) {
//                    // Use post to ensure layout calculation is done after data updates but before scrolling
//                    messagesRecyclerView.post(() -> messagesRecyclerView.smoothScrollToPosition(messagesFromRoom.size() - 1));
//                }
//                // Note: More complex scrolling logic might be needed to avoid auto-scrolling when user is reading old messages
//            }
//        });
//        // --- End Step 1 (LiveData Observation Setup) ---
//
//
//        // --- Step 2: Attach Firebase ChildEventListener for Syncing ---
//        // This listener watches the group's messages node in Firebase for adds, changes, and removals.
//        // It syncs these changes *into* the Room DB on a background thread.
//        // Room LiveData then automatically updates the UI via the observer above.
//        attachGroupMessagesSyncListener();
//        // --- End Step 2 (Firebase Sync Listener Setup) ---
//
//        // Removed the direct list population logic from the old LoadMessages method.
//    }
//
//
//    // --- NEW Method to Attach Firebase ChildEventListener for Group Messages Sync ---
//    // This listener syncs changes from Firebase messages node to Room DB.
//    // It is attached in LoadMessages.
//    @SuppressLint("RestrictedApi")
//    private void attachGroupMessagesSyncListener() {
//        // Check if messagesRef and groupId are initialized
//        if (messagesRef == null || groupId == null) {
//            Log.e(TAG, "Cannot attach messages sync listener, messagesRef or groupId is null.");
//            return;
//        }
//        // Check if the listener is already attached to prevent duplicate listeners
//        if (groupMessagesChildEventListener == null) {
//            Log.d(TAG, "Attaching Firebase ChildEventListener for messages sync for group: " + groupId);
//
//            // Create the ChildEventListener implementation
//            groupMessagesChildEventListener = new ChildEventListener() {
//                @Override
//                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                    // This is called for each existing message when the listener attaches, and for each new message added to Firebase.
//                    String messageId = snapshot.getKey(); // This is the unique Firebase push key for the message
//
//                    if (TextUtils.isEmpty(messageId)) {
//                        Log.w(TAG, "onChildAdded received null or empty message ID from Firebase.");
//                        return;
//                    }
//
//                    Log.d(TAG, "💬 onChildAdded triggered for message ID: " + messageId);
//
//                    // Get the message data from the snapshot as a HashMap to handle potential casting and missing fields
//                    Map<String, Object> messageDataMap = (Map<String, Object>) snapshot.getValue();
//
//                    if (messageDataMap == null) {
//                        Log.w(TAG, "Message data is null for message ID: " + messageId);
//                        // Decide how to handle this (e.g., maybe delete the message from Firebase if data is corrupt?)
//                        return;
//                    }
//
//                    // Save/Update the message in Room DB on a background thread.
//                    // This method converts the Firebase snapshot data into a Room Entity and saves it.
//                    saveMessageToRoomFromSnapshot(snapshot, groupId); // Pass snapshot and groupId
//
//                    // --- Mark the received message as read in Firebase (if it's not from the current user) ---
//                    // Mark messages as read when they are added/synced to Room.
//                    // This indicates the message has been "delivered" to this device and processed.
//                    // This Firebase write should happen on a background thread.
//                    String senderId = (String) messageDataMap.get("senderId"); // Get sender ID from map
//                    if (senderId != null && !senderId.equals(currentUserID)) { // If message is from another user
//                        // Check if current user is already in the readBy map before writing to Firebase (optimization)
//                        Map<String, Boolean> readByMap = null;
//                        if (messageDataMap.containsKey("readBy") && messageDataMap.get("readBy") instanceof Map) {
//                            try {
//                                readByMap = (Map<String, Boolean>) messageDataMap.get("readBy");
//                            } catch (ClassCastException e) {
//                                Log.e(TAG, "Casting readBy map failed.", e);
//                            }
//                        }
//
//                        if (readByMap == null || !Boolean.TRUE.equals(readByMap.get(currentUserID))) { // If readBy is null or does not contain current user as true
//                            Log.d(TAG, "Attempting to mark message " + messageId + " as read for user " + currentUserID + " in Firebase (onChildAdded sync).");
//                            // Use the snapshot.getRef() to get the message's reference and update
//                            snapshot.getRef().child("readBy").child(currentUserID).setValue(true)
//                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Marked message " + messageId + " as read for user " + currentUserID + " in Firebase."))
//                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to mark message " + messageId + " as read in Firebase.", e));
//                            // The onChildChanged event will eventually trigger for this message when Firebase updates,
//                            // syncing the updated readBy map back to Room.
//                        } else {
//                            // Message is already marked as read by this user in Firebase
//                            // Log.d(TAG, "Message " + messageId + " already marked as read by " + currentUserID + " in Firebase."); // Too verbose
//                        }
//
//                        // --- Trigger Notification for incoming messages (if activity is not in foreground) ---
//                        // This logic should ideally be handled by your background message processing (e.g., Firebase Messaging Service or a Worker)
//                        // that receives messages even when the app is closed or in background.
//                        // If you want to trigger notification *from here* and decrypt here, ensure conversationAESKey is accessible and decrypt before passing to NotificationHelper.
//                        // Placeholder logic:
//                        // if (!isActivityResumed()) { // Check if activity is in background/stopped (requires tracking activity state)
//                        //   NotificationHelper.createNotification(GroupChatActivityActivity.this, "New Message in " + (groupNameTextView != null ? groupNameTextView.getText().toString() : "Group"), messageContent); // messageContent is encrypted! Needs decryption!
//                        // }
//                    }
//                }
//
//                @Override
//                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                    // This is called when a message's data changes in Firebase (e.g., seen status, timestamp resolves, readBy map is updated)
//                    String firebaseMessageId = snapshot.getKey();
//                    if (TextUtils.isEmpty(firebaseMessageId)) {
//                        Log.w(TAG, "onChildChanged received null or empty message ID from Firebase.");
//                        return;
//                    }
//                    Log.d(TAG, "🔄 onChildChanged triggered for message ID: " + firebaseMessageId);
//
//                    // Save/Update the message in Room DB on a background thread.
//                    // This method converts the Firebase snapshot data into a Room Entity and saves it.
//                    saveMessageToRoomFromSnapshot(snapshot, groupId); // Pass snapshot and groupId
//
//                    // If the change was us marking it as read, no need for notification or further action here.
//                    // If the change was someone else reading our message (and we want to show read receipts),
//                    // the LiveData observer will trigger UI update via Room.
//                }
//
//                @Override
//                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                    // A message is removed from the conversation's messages node in Firebase (e.g., "Clear Chat")
//                    String firebaseMessageId = snapshot.getKey();
//                    if (TextUtils.isEmpty(firebaseMessageId)) {
//                        Log.w(TAG, "❌ onChildRemoved received null or empty message ID from Firebase.");
//                        return;
//                    }
//                    Log.d(TAG, "❌ onChildRemoved triggered for message ID: " + firebaseMessageId + ". Removing from Room DB.");
//
//                    // Remove the message from Room DB on a background thread using the DAO
//                    removeMessageFromRoom(firebaseMessageId); // Pass messageId
//                }
//
//                @Override
//                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                    // This is called if a message's position changes in Firebase (e.g., due to priority changes).
//                    // Not strictly relevant if sorting by timestamp in Room query.
//                    // Log.d(TAG, "onChildMoved triggered for message ID: " + snapshot.getKey());
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.e(TAG, "Firebase Group Messages Listener cancelled for group " + groupId + ": " + error.getMessage(), error.toException());
//                    // Handle error - show a message to the user on the main thread
//                    Toast.makeText(GroupChatActivity.this, "Failed to sync messages: " + error.getMessage(), Toast.LENGTH_LONG).show();
//                    // Optionally clear local Room data or show an error state for the chat
//                }
//            };
//
//            // Attach the listener to the messages node for this specific group in Firebase
//            messagesRef.addChildEventListener(groupMessagesChildEventListener);
//            Log.d(TAG, "Firebase messages sync listener attached to: " + messagesRef.getPath());
//        }
//    }
//    // --- END NEW Method to Attach Firebase Listener ---
//
//    // --- NEW Method to Remove Firebase ChildEventListener ---
//    // Remove the listener when the activity is destroyed to prevent memory leaks and unnecessary Firebase background activity.
//    private void removeGroupMessagesSyncListener() {
//        // Check if Firebase reference and listener are initialized
//        if (messagesRef != null && groupMessagesChildEventListener != null) {
//            Log.d(TAG, "Removing Firebase ChildEventListener for messages sync for group: " + groupId);
//            // Remove the listener from the Firebase reference
//            messagesRef.removeEventListener(groupMessagesChildEventListener);
//            groupMessagesChildEventListener = null; // Nullify the reference
//        } else {
//            // Log.d(TAG, "Firebase listener is null or messagesRef is null, nothing to remove."); // Too verbose maybe
//        }
//    }
//    // --- END NEW Method to Remove Firebase Listener ---
//
//
//
//
//
//    // Inside GroupChatActivity.java class body { ... }
//
//    // --- MODIFIED: Save/Update Message from Firebase Snapshot to Room DB ---
//// This method is called by the onChildAdded and onChildChanged events of the Firebase listener.
//// It extracts data from the Firebase DataSnapshot and saves/updates the MessageEntity in Room DB.
//    private void saveMessageToRoomFromSnapshot(@NonNull DataSnapshot snapshot, String groupId) {
//        // Ensure Room DAO and Executor are available
//        if (groupMessageDao == null || databaseExecutor == null || groupId == null) {
//            Log.e(TAG, "Cannot save message to Room DB from snapshot: DAO, Executor, or groupId is null.");
//            return;
//        }
//        String firebaseMessageId = snapshot.getKey(); // Get the message ID (Firebase push key)
//        if (TextUtils.isEmpty(firebaseMessageId)) {
//            Log.w(TAG, "saveMessageToRoomFromSnapshot received snapshot with empty key.");
//            return;
//        }
//
//        // Get the message data as a HashMap to handle potential casting and missing fields safely
//        Map<String, Object> messageDataMap = (Map<String, Object>) snapshot.getValue();
//        if (messageDataMap == null) {
//            Log.w(TAG, "Message data map is null for message ID: " + firebaseMessageId + " in saveMessageToRoomFromSnapshot.");
//            // Optionally remove this entry from Firebase if it's corrupt? For FYP, logging warning is likely enough.
//            return;
//        }
//
//        // --- Extract data from the map. Use default values or handle nulls appropriately. ---
//        String messageContent = (String) messageDataMap.get("message"); // Message content (text, Base64 image, or Drawing Session text)
//        String messageType = (String) messageDataMap.get("type"); // Message type ("text", "image", "drawing_session")
//        String senderName = (String) messageDataMap.get("name"); // Sender's display name
//        String senderId = (String) messageDataMap.get("senderId"); // Sender's UID
//        // Timestamp from Firebase resolves to a Long. Use default 0L if missing.
//        // Use the actual long value from snapshot directly if available, otherwise fallback.
//        Long timestampLong = snapshot.hasChild("timestamp") ? snapshot.child("timestamp").getValue(Long.class) : 0L;
//        // Handle case where timestamp might be null from Firebase for some reason, or if using local time fallback
//        if (timestampLong == null) timestampLong = 0L; // Ensure it's not null for the long type
//
//        String date = (String) messageDataMap.get("date"); // Client date (Optional)
//        String time = (String) messageDataMap.get("time"); // Client time (Optional)
//
//        // Safely get and cast the readBy map.
//        Map<String, Boolean> readByMap = null;
//        if (messageDataMap.containsKey("readBy") && messageDataMap.get("readBy") instanceof Map) {
//            try {
//                readByMap = (Map<String, Boolean>) messageDataMap.get("readBy");
//            } catch (ClassCastException e) {
//                Log.e(TAG, "Failed to cast 'readBy' to Map<String, Boolean> for message " + firebaseMessageId + " in saveMessageToRoom.", e);
//            }
//        }
//        if (readByMap == null) {
//            readByMap = new HashMap<>(); // Default to empty map if missing or casting failed
//        }
//
//        // --- NEW: Extract drawingSessionId from the snapshot ---
//        // This field will only exist for messages of type "drawing_session".
//        // Use snapshot.hasChild() and getValue() to get the string value safely.
//        String drawingSessionId = snapshot.hasChild("drawingSessionId") ?
//                snapshot.child("drawingSessionId").getValue(String.class) :
//                null; // Set to null if the child node does not exist
//
//
//        // --- Create the GroupMessageEntity object using the UPDATED constructor ---
//        // Ensure all fields from the entity are included and mapped correctly from Firebase data.
//        // Pass the extracted drawingSessionId as the last argument.
//        GroupMessageEntity messageEntity = new GroupMessageEntity(
//                firebaseMessageId, // Primary Key (Firebase push key)
//                groupId, // Group ID (from Activity member variable)
//                messageContent, // Message content
//                messageType, // Message type
//                senderName, // Sender's name
//                senderId, // Sender's UID
//                timestampLong, // Resolved timestamp
//                date, // Client date
//                time, // Client time
//                readByMap, // ReadBy map
//                drawingSessionId // <<< NEW: Pass the extracted drawingSessionId (will be null for text/image)
//        );
//
//        // Save/Update the message in Room DB on a background thread using the shared executor
//        databaseExecutor.execute(() -> { // Execute on the shared DB executor
//            try {
//                // Insert or replace the message in Room. This handles both new messages (insert)
//                // and updates to existing ones (replace based on primary key messageId).
//                groupMessageDao.insertOrUpdateMessage(messageEntity);
//                // Log.d(TAG, "Group message saved/updated in Room DB for message ID: " + firebaseMessageId + " (Group: " + groupId + ")."); // Too verbose maybe
//                // LiveData observer attached in LoadMessages will automatically pick this Room change up and update the UI
//            } catch (Exception e) {
//                Log.e(TAG, "Error saving/updating message " + firebaseMessageId + " in Room DB from snapshot", e);
//                // Handle Room DB errors (log, retry?)
//            }
//        });
//    }
//// --- END MODIFIED Method saveMessageToRoomFromSnapshot ---
//
//
//    // --- NEW Method to Remove Message from Room DB ---
//    // This method is called by the onChildRemoved event of the Firebase listener, or potentially manually (e.g., Clear Chat).
//    private void removeMessageFromRoom(String messageId) {
//        // Ensure Room DAO and Executor are available
//        if (groupMessageDao == null || databaseExecutor == null) {
//            Log.e(TAG, "Cannot remove message from Room DB: DAO or Executor is null.");
//            return;
//        }
//        if (TextUtils.isEmpty(messageId)) {
//            Log.w(TAG, "removeMessageFromRoom received empty messageId.");
//            return;
//        }
//
//        // Execute the delete operation on the shared database executor (background thread)
//        databaseExecutor.execute(() -> { // Use the shared database executor for Room ops
//            try {
//                // Delete the message from Room DB by its messageId (primary key)
//                int deletedRows = groupMessageDao.deleteMessageById(messageId); // Use delete by messageId
//                if (deletedRows > 0) {
//                    Log.d(TAG, "Message removed from Room DB for message ID: " + messageId + ". Rows deleted: " + deletedRows);
//                    // LiveData observer will pick this Room change up and update UI automatically
//                } else {
//                    // This is expected if the message didn't exist in Room for some reason (e.g., wasn't synced)
//                    Log.w(TAG, "Attempted to remove message " + messageId + " from Room DB, but it was not found.");
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error removing message " + messageId + " from Room DB", e);
//                // Handle Room DB errors
//            }
//        });
//    }
//    // --- END NEW Method to Remove Message from Room DB ---
//
//
//    // --- Method to mark a specific message as read in Firebase (Keep This Helper Method) ---
//    // Called from onChildAdded for incoming messages that are not already read by this user.
//    // This updates the 'readBy' map for this message in Firebase for the current user.
//    private void markMessageAsReadInFirebase(DatabaseReference messageRef, String userId) {
//        // Ensure messageRef and userId are valid
//        if (messageRef == null || userId == null || userId.isEmpty()) { // Added check for empty userId
//            Log.e(TAG, "markMessageAsReadInFirebase: messageRef or userId is null/empty.");
//            return;
//        }
//        // Log.d(TAG, "Attempting to mark message as read in Firebase for user: " + userId); // Too verbose
//
//        // The path to the specific user's entry in the readBy map for this message
//        // messages/{groupId}/{messageId}/readBy/{userId}
//        // We are given the messageRef (messages/{groupId}/{messageId}), so we just need to add /readBy/{userId}
//        messageRef.child("readBy").child(userId).setValue(true) // Set value to boolean true to mark as read
//                .addOnSuccessListener(aVoid -> {
//                    // Log.d(TAG, "Marked message as read in Firebase successfully for user: " + userId); // Too verbose
//                    // The Firebase onChildChanged listener will pick this up and sync to Room.
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Failed to mark message as read in Firebase.", e);
//                    // Handle failure (e.g., retry logic, log error)
//                });
//    }
//
//
//    // --- Keep checkUserMembership method ---
//    // This method checks if the current user is a member or admin of the group in Firebase.
//    // It's a security check. If not a member, the activity is finished.
//    // It runs asynchronously in onCreate.
//    // Inside GroupChatActivity.java
//
//    // --- REWRITTEN checkUserMembership method ---
//    // This method checks if the current user is a member or admin.
//    // It enables/disables the message input UI based on membership status,
//    // allowing viewing historical messages even if not a member.
//    private void checkUserMembership() {
//        // Check if essential references and user ID are initialized
//        if (groupRef == null || currentUserID == null || currentUserID.isEmpty()) { // Added empty check for userId
//            Log.e(TAG, "checkUserMembership: groupRef or currentUserID is null/empty. Cannot check membership.");
//            Toast.makeText(this, "Critical error checking membership.", Toast.LENGTH_SHORT).show();
//            finish(); // Critical error, cannot safely proceed
//            return;
//        }
//        Log.d(TAG, "Checking membership status for user " + currentUserID + " in group " + groupId);
//
//        // Use addListenerForSingleValueEvent to check membership status once when activity starts
//        // We only need to listen to the group node to get both 'members' and 'admin'
//        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // This callback runs on the main thread
//
//                if (!snapshot.exists()) {
//                    // Group node does not exist in Firebase. User cannot view it at all.
//                    Log.w(TAG, "Group data not found in Firebase for ID: " + groupId + ". Finishing activity.");
//                    Toast.makeText(GroupChatActivity.this, "Group does not exist or was deleted.", Toast.LENGTH_LONG).show();
//                    finish(); // Group doesn't exist, cannot view history associated with it
//                    return; // Exit the onDataChange method
//                }
//
//                // Group node exists, now check membership details
//                DataSnapshot membersSnapshot = snapshot.child("members"); // Get the members node snapshot
//                String adminId = snapshot.child("admin").getValue(String.class); // Get the admin UID
//
//                // Check if the current user ID exists within the members node OR is the admin
//                boolean isMember = membersSnapshot.hasChild(currentUserID);
//                boolean isAdmin = adminId != null && adminId.equals(currentUserID);
//
//
//                // Logic based on membership status:
//                if (isMember || isAdmin) {
//                    // User is either explicitly listed as a member OR is the admin
//                    Log.d(TAG, "User " + currentUserID + " is a member or admin of group " + groupId + ". Enabling message input UI.");
//                    // User is authorized, ensure message input UI is enabled
//                    enableMessageInputUI();
//
//                } else {
//                    // User is NOT a member and NOT the admin. They cannot send messages.
//                    Log.w(TAG, "User " + currentUserID + " is NOT a member or admin of group " + groupId + ". Disabling message input UI.");
//                    Toast.makeText(GroupChatActivity.this, "You are no longer a member of this group.", Toast.LENGTH_LONG).show();
//                    // User is not authorized to send, disable message input UI
//                    disableMessageInputUI();
//                    // *** IMPORTANT: DO NOT call finish() here ***
//                    // The activity remains open, allowing the user to view the historical messages
//                    // loaded from the Room database.
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to check membership status in Firebase: " + error.getMessage(), error.toException());
//                Toast.makeText(GroupChatActivity.this, "Failed to verify group access. Cannot send messages.", Toast.LENGTH_SHORT).show();
//                // On error fetching membership status, defensively disable UI as we cannot confirm authorization.
//                disableMessageInputUI();
//                // *** IMPORTANT: DO NOT call finish() here on cancellation either ***
//                // Similar to the non-member case, allow viewing history if the group node itself exists.
//            }
//        });
//    }
//    // --- END REWRITTEN checkUserMembership ---
//
//    // --- Keep showPopupMenu method ---
//    // This method handles displaying the menu when the groupMenu icon is clicked.
//    // It inflates the menu resource and sets the click listener for menu items.
//    @SuppressLint("NonConstantResourceId")
//    // Keep this if using R.id in switch statement or if-else chain
//    private void showPopupMenu(View view) {
//        // Ensure context is not null before showing menu
//        if (this == null) {
//            Log.w(TAG, "Context is null, cannot show popup menu.");
//            return;
//        }
//        // Create a new PopupMenu attached to the specified view
//        PopupMenu popupMenu = new PopupMenu(this, view);
//        // Get the menu inflater and inflate the menu resource
//        MenuInflater inflater = popupMenu.getMenuInflater();
//        inflater.inflate(R.menu.popup_group_menu, popupMenu.getMenu()); // Replace with your actual menu resource ID
//
//
//        // Handle Menu Item Clicks using setOnMenuItemClickListener
//        popupMenu.setOnMenuItemClickListener(item -> {
//            // Get the ID of the clicked menu item
//            int id = item.getItemId();
//
//            // Use if-else or switch to handle menu item clicks by ID
//            // Ensure these R.id values match the item IDs in your popup_group_menu.xml
//            // This requires your menu resource to be defined with these IDs.
//            if (id == R.id.group_settings) { // Assuming R.id.group_settings exists in your menu
//                Log.d(TAG, "Popup menu item clicked: Group Settings");
//                openGroupSettings(); // Call method to open settings activity
//                return true; // Event handled
//            } else if (id == R.id.clear_chat) { // Assuming R.id.clear_chat exists in your menu
//                Log.d(TAG, "Popup menu item clicked: Clear Chat");
//                confirmClearChat(); // Call method to confirm and clear chat
//                return true; // Event handled
//            } else if (id == R.id.exit_group) { // Assuming R.id.exit_group exists in your menu
//                Log.d(TAG, "Popup menu item clicked: Exit Group");
//                confirmExitGroup(); // Call method to confirm and exit group
//                return true; // Event handled
//
//            } else if (id == R.id.start_drawing_chat) { // <<< NEW Case
//                Log.d(TAG, "Popup menu item clicked: Start Shared Drawing");
//                confirmStartDrawingSession(); // <<< Call the new method
//                return true; // Event handled
//
//            } else {
//                // Handle other menu items if any
//                Log.d(TAG, "Popup menu item clicked with unhandled ID: " + getResources().getResourceEntryName(id));
//                return false; // Event not handled by this listener
//            }
//        });
//
//        // Show the popup menu
//        popupMenu.show();
//    }
//
//    // --- Keep openGroupSettings method ---
//    // Opens the Group Settings activity, passing the group ID.
//    private void openGroupSettings() {
//        // Ensure context is not null before starting activity
//        if (this == null) {
//            Log.w(TAG, "Context is null, cannot open group settings.");
//            return;
//        }
//        // Create intent for GroupSettingsActivity
//        Intent intent = new Intent(this, GroupSettingsActivity.class); // Replace GroupSettingsActivity with your actual Activity class
//        intent.putExtra("groupId", groupId); // Pass the current group ID to the settings activity
//
//        Log.d(TAG, "Starting GroupSettingsActivity with groupId: " + groupId);
//
//        // Start the activity
//        startActivity(intent);
//    }
//
//    // --- Keep confirmClearChat method ---
//    // Shows a confirmation dialog before clearing the chat messages.
//    private void confirmClearChat() {
//        // Ensure context is not null
//        if (this == null) {
//            Log.w(TAG, "Context is null, cannot show clear chat confirmation dialog.");
//            return;
//        }
//        Log.d(TAG, "Showing clear chat confirmation dialog.");
//        // Create and show an AlertDialog
//        new AlertDialog.Builder(this)
//                .setTitle("Clear Chat")
//                .setMessage("Are you sure you want to clear all messages from this group locally? This will only remove messages from your device.")
//                .setPositiveButton("Yes, Clear", (dialog, which) -> clearChat()) // If "Yes, Clear" is clicked, call clearChat()
//                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // If "Cancel" is clicked, just dismiss the dialog
//                .setIcon(android.R.drawable.ic_dialog_alert) // Optional: add an icon
//                .show(); // Show the dialog
//    }
//
//    // --- MODIFIED clearChat method ---
//    // Deletes messages from Firebase and then from Room DB.
//    // Inside GroupChatActivity.java
//
//    // --- MODIFIED clearChat method ---
//// Deletes messages ONLY from Room DB for the current user.
//    private void clearChat() {
//        Log.d(TAG, "Initiating local clear chat process for group: " + groupId);
//
//        // --- REMOVE THE FOLLOWING BLOCK THAT DELETES FROM FIREBASE ---
//    /*
//    // Delete messages from Firebase first. This is the source of truth.
//    if (messagesRef != null) {
//        messagesRef.removeValue().addOnCompleteListener(task -> {
//            // This callback runs on the main thread after the Firebase remove operation completes
//            if (task.isSuccessful()) {
//                Log.d(TAG, "Chat messages cleared from Firebase successfully for group: " + groupId);
//                // Toast.makeText(this, "Chat Cleared", Toast.LENGTH_SHORT).show(); // This toast will now be misleading
//
//                // The Firebase ChildEventListener attached in LoadMessages will automatically
//                // trigger onChildRemoved for each deleted message from Firebase, which then calls
//                // removeMessageFromRoom() to delete them from Room DB one by one.
//                // However, for a bulk delete like this (clearing the whole chat), it is much faster and more efficient
//                // to explicitly tell Room to delete *all* messages for this group after Firebase confirms deletion.
//                if (groupMessageDao != null && databaseExecutor != null) {
//                    // Execute the bulk delete in Room DB on the background database executor
//                    databaseExecutor.execute(() -> {
//                        groupMessageDao.deleteAllMessagesForGroup(groupId); // Use the DAO method to delete all messages for this groupId
//                        Log.d(TAG, "Explicitly cleared Room DB messages for group: " + groupId + " after Firebase clear.");
//                        // LiveData observer will see the list is empty and update UI automatically
//                    });
//                } else {
//                    Log.w(TAG, "groupMessageDao or databaseExecutor is null, cannot explicitly clear Room DB after Firebase clear.");
//                }
//
//            } else {
//                Log.e(TAG, "Failed to clear chat messages from Firebase.", task.getException());
//                Toast.makeText(this, "Failed to clear chat.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    } else {
//        Log.e(TAG, "messagesRef is null, cannot clear chat from Firebase.");
//        Toast.makeText(this, "Error clearing chat.", Toast.LENGTH_SHORT).show();
//    }
//    */
//        // --- END BLOCK TO REMOVE ---
//
//
//        // --- KEEP AND ENSURE THIS BLOCK IS CORRECT ---
//        // Clear local Room DB data for this group for the current user.
//        // This prevents seeing old messages after clearing the chat. Run on background thread.
//        if (groupMessageDao != null && databaseExecutor != null) {
//            databaseExecutor.execute(() -> { // Use the shared DB executor for Room ops
//                try {
//                    // Delete all messages for this group from Room DB using the DAO method
//                    int deletedRows = groupMessageDao.deleteAllMessagesForGroup(groupId); // Get the number of rows deleted
//
//                    // Switch back to the main thread to show a Toast message
//                    runOnUiThread(() -> {
//                        if (deletedRows > 0) {
//                            Log.d(TAG, "Cleared " + deletedRows + " messages from Room DB locally for group: " + groupId);
//                            Toast.makeText(GroupChatActivity.this, "Chat cleared locally.", Toast.LENGTH_SHORT).show();
//                            // LiveData observer will automatically update the RecyclerView UI to show an empty list
//                        } else {
//                            Log.w(TAG, "Attempted to clear local chat for group " + groupId + ", but no messages were found to delete.");
//                            Toast.makeText(GroupChatActivity.this, "No messages to clear locally.", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } catch (Exception e) {
//                    Log.e(TAG, "Error clearing messages from Room DB locally for group " + groupId, e);
//                    // Switch back to the main thread to show an error Toast
//                    runOnUiThread(() -> {
//                        Toast.makeText(GroupChatActivity.this, "Error clearing chat locally.", Toast.LENGTH_SHORT).show();
//                    });
//                }
//            });
//        } else {
//            Log.e(TAG, "groupMessageDao or databaseExecutor is null, cannot clear Room DB locally.");
//            Toast.makeText(this, "Error clearing chat locally.", Toast.LENGTH_SHORT).show();
//        }
//        // --- END BLOCK TO KEEP ---
//    }
//
//    // --- Keep confirmExitGroup method ---
//    // Shows a confirmation dialog before exiting the group.
//    private void confirmExitGroup() {
//        // Ensure context is not null
//        if (this == null) {
//            Log.w(TAG, "Context is null, cannot show exit group confirmation dialog.");
//            return;
//        }
//        Log.d(TAG, "Showing exit group confirmation dialog.");
//        // Create and show an AlertDialog
//        new AlertDialog.Builder(this)
//                .setTitle("Exit Group")
//                .setMessage("Are you sure you want to exit this group? You will not be able to send or receive messages unless re-added. This will also clear the chat history locally.")
//                .setPositiveButton("Exit", (dialog, which) -> exitGroup()) // If "Exit" is clicked, call exitGroup()
//                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // If "Cancel" is clicked, just dismiss the dialog
//                .setIcon(android.R.drawable.ic_dialog_alert) // Optional: add an icon
//                .show(); // Show the dialog
//    }
//
//    // --- MODIFIED exitGroup method ---
//    // Removes the current user from the group's members list in Firebase and clears local data from Room DB.
//    // Inside GroupChatActivity.java
//
//    // --- REWRITTEN exitGroup method ---
//    // Removes the current user from the group's members list in Firebase,
//    // keeps local data in Room, disables the message input UI, and keeps the activity open.
//    private void exitGroup() {
//        Log.d(TAG, "User " + currentUserID + " attempting to exit group " + groupId);
//
//        // Check if essential references and user ID are initialized
//        if (groupRef == null || currentUserID == null || currentUserID.isEmpty()) {
//            Log.e(TAG, "groupRef or currentUserID is null/empty, cannot initiate exit group.");
//            Toast.makeText(this, "Error initiating exit.", Toast.LENGTH_SHORT).show();
//            // Do not finish here, just report the error initiating the process
//            return;
//        }
//
//        // --- Step 1: Remove user from the group's members list in Firebase ---
//        Log.d(TAG, "Removing user " + currentUserID + " from members list in Firebase for group " + groupId);
//        groupRef.child("members").child(currentUserID).removeValue().addOnCompleteListener(task -> {
//            // This callback runs on the main thread after the Firebase remove operation completes
//
//            if (task.isSuccessful()) {
//                Log.d(TAG, "User " + currentUserID + " successfully removed from group " + groupId + " members list in Firebase.");
//                Toast.makeText(this, "You exited the group.", Toast.LENGTH_SHORT).show();
//
//                // --- Step 2: Disable message input UI immediately ---
//                // This prevents the user from sending new messages after successfully exiting.
//                disableMessageInputUI();
//
//                // --- Step 3: KEEP the local Room DB data ---
//                // REMOVED: The previous block that called groupMessageDao.deleteAllMessagesForGroup is removed.
//                // The user's local chat history in Room is preserved.
//
//                // --- Step 4: Keep the activity open ---
//                // REMOVED: The previous call to finish() is removed.
//                // The user stays in the chat activity view to see the history.
//
//                // The checkUserMembership logic (which might run on Activity start or resume)
//                // will now find that the user's ID doesn't exist in the members list and will
//                // call disableMessageInputUI(), ensuring the UI remains disabled on subsequent views.
//
//
//            } else {
//                Log.e(TAG, "Failed to remove user " + currentUserID + " from group " + groupId + " members list in Firebase.", task.getException());
//                Toast.makeText(this, "Failed to exit group.", Toast.LENGTH_SHORT).show();
//                // On failure, the user is still technically a member in Firebase.
//                // The UI remains enabled (as per checkUserMembership if it runs again).
//                // They would need to retry exiting.
//            }
//        });
//        // --- End Step 1 ---
//    }
//    // --- END REWRITTEN exitGroup ---
//
//
//
//    // Inside GroupChatActivity.java, within the class body { ... }
//
//    // --- NEW Helper methods to manage message input UI state (Add these if they are missing or misplaced) ---
//    private void enableMessageInputUI() {
//        if (userMessageInput != null) {
//            userMessageInput.setEnabled(true);
//            userMessageInput.setHint("Enter Message..."); // Set back to default hint
//        }
//        if (sendMessageButton != null) {
//            sendMessageButton.setEnabled(true);
//        }
//        if (send_imgmsg_btn != null) {
//            send_imgmsg_btn.setEnabled(true);
//        }
//        Log.d(TAG, "Message input UI ENABLED.");
//    }
//
//    private void disableMessageInputUI() {
//        if (userMessageInput != null) {
//            userMessageInput.setEnabled(false);
//            userMessageInput.setHint("You have exited this group."); // Inform user why it's disabled
//        }
//        if (sendMessageButton != null) {
//            sendMessageButton.setEnabled(false);
//        }
//        if (send_imgmsg_btn != null) {
//            send_imgmsg_btn.setEnabled(false);
//        }
//        Log.d(TAG, "Message input UI DISABLED.");
//    }
//    // --- END NEW Helper methods ---
//
//// ... Rest of your GroupChatActivity code ...
//
//    // Keep onActivityResult for image picking (Handles the result from the image picker Intent)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        // Check if the result is from the image picker request and was successful
//        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            Uri imageUri = data.getData(); // Get the Uri of the selected image
//            Log.d(TAG, "Image selected from picker. URI: " + imageUri);
//
//            // Process and encode image to Base64.
//            // This can be a heavy task for large images. Consider using an ExecutorService
//            // or a library that handles background processing for image loading/encoding.
//            // For simplicity here, it runs on the main thread.
//            try {
//                // Get Bitmap from Uri using MediaStore
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//                Log.d(TAG, "Bitmap obtained from URI. Dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight());
//
//                // Encode the Bitmap to a Base64 string
//                String encodedImage = encodeImageToBase64(bitmap); // Use your helper method (runs on calling thread)
//                Log.d(TAG, "Image encoded to Base64. Sending...");
//
//                // Send the Base64 encoded image as a message
//                sendImageMessageToDatabase(encodedImage); // Call the image sending method
//
//            } catch (IOException e) {
//                Log.e(TAG, "Image processing failed during selection/encoding (IOException)", e);
//                Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show();
//            } catch (Exception e) { // Catch any other unexpected exceptions during bitmap/encoding
//                Log.e(TAG, "Unexpected error during image processing/encoding", e);
//                Toast.makeText(this, "An error occurred processing image", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            // Log if the activity result was not for the image picker or was not successful
//            Log.d(TAG, "ActivityResult for image picker cancelled or failed. RequestCode: " + requestCode + ", ResultCode: " + resultCode);
//        }
//    }
//
//    // Helper method to encode Bitmap to Base64 string (Keep This Helper Method)
//    private String encodeImageToBase64(Bitmap bitmap) {
//        // Use android.util.Base64 consistently for Base64 encoding/decoding in Android
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        // Compress the bitmap to JPEG format with a specified quality (e.g., 70%)
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Using 70% quality
//
//        byte[] imageBytes = baos.toByteArray(); // Get the byte array from the stream
//
//        // Encode the byte array into a Base64 string using Base64.DEFAULT flags
//        return Base64.encodeToString(imageBytes, Base64.DEFAULT); // Use android.util.Base64.DEFAULT flags
//    }
//
//
//    // --- NEW Standard RecyclerView Adapter for GroupMessageEntity ---
//    // This static inner class defines the adapter responsible for binding GroupMessageEntity data
//    // from Room DB to the message list item layouts (e.g., message_item_sent.xml, message_item_received.xml).
//    // This replaces the FirebaseRecyclerAdapter approach.
//
//
//
//
//    // Inside GroupChatActivity class body { ... }
//
//    // Inside GroupChatActivity.java
//
//    // --- NEW Method: Start a new drawing session in Firebase and send chat message ---
//    private void startDrawingSession() {
//        Log.d(TAG, "Attempting to start a new drawing session for group: " + groupId);
//
//        // Check for essential data (Firebase refs, user info, group ID, session ID)
//        // currentUserID, currentUserName are fetched in GetUserInfo().
//        // groupId is from intent.
//        // messagesRef, rootRef are initialized in onCreate.
//        if (rootRef == null || groupId == null || currentUserID == null || currentUserName == null) {
//            Log.e(TAG, "Cannot start drawing session: Required dependencies are null.");
//            Toast.makeText(this, "Error starting drawing session. Missing setup info.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 1. Get reference to the drawingSessions node within this group
//        DatabaseReference drawingSessionsRef = rootRef.child("Groups").child(groupId).child("drawingSessions");
//
//        // 2. Create a new entry under 'drawingSessions' in Firebase
//        // This generates a unique ID for the new session using push().getKey()
//        String newSessionId = drawingSessionsRef.push().getKey();
//
//        if (newSessionId == null) {
//            Log.e(TAG, "Failed to generate unique session ID from Firebase.");
//            Toast.makeText(this, "Error starting drawing: Could not generate session ID.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Log.d(TAG, "Generated new drawing session ID: " + newSessionId);
//
//        // 3. Prepare initial data for the new session node
//        HashMap<String, Object> sessionInfo = new HashMap<>();
//        sessionInfo.put("starterId", currentUserID); // Store who started it
//        sessionInfo.put("createdAt", ServerValue.TIMESTAMP); // Store server timestamp
//        sessionInfo.put("state", "active"); // Set initial state to active
//
//        // 4. Write the initial session info to Firebase
//        drawingSessionsRef.child(newSessionId).setValue(sessionInfo)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "New drawing session node created in Firebase: " + newSessionId);
//
//                    // 5. After successfully creating the session node, send the special chat message
//                    // This message links to the new session and appears in the main chat history.
//                    sendDrawingSessionStartedMessage(newSessionId, currentUserName, currentUserID);
//
//                    // 6. Navigate to the new Drawing Activity
//                    // Pass the group ID and the new session ID to the activity.
//                    Intent drawingIntent = new Intent(GroupChatActivity.this, YOUR_DRAWING_ACTIVITY_CLASS.class); // <<< Replace with your actual Drawing Activity Class Name
//                    drawingIntent.putExtra("groupId", groupId); // Pass group ID
//                    drawingIntent.putExtra("sessionId", newSessionId); // Pass the NEWLY generated session ID
//                    startActivity(drawingIntent);
//
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Failed to create new drawing session node in Firebase.", e);
//                    Toast.makeText(GroupChatActivity.this, "Failed to start drawing session.", Toast.LENGTH_SHORT).show();
//                    // Optional: Clean up the partially created session node if the write failed
//                    // drawingSessionsRef.child(newSessionId).removeValue();
//                });
//    }
//
//    // --- NEW Method to Send the Special "Drawing Session Started" Message ---
//// This is similar to SendMsgInfoToDatabase but includes the drawingSessionId.
//    // Inside GroupChatActivity.java
//
//    // --- NEW Method: Send the Special "Drawing Session Started" Message to Chat History ---
//    private void sendDrawingSessionStartedMessage(String sessionId, String senderName, String senderId) {
//        Log.d(TAG, "Sending 'drawing_session' message for session: " + sessionId + " in group: " + groupId);
//
//        // Check for essential data
//        if (messagesRef == null || senderId == null || senderName == null || sessionId == null || groupId == null) {
//            Log.e(TAG, "Cannot send drawing session message: Required dependencies are null.");
//            Toast.makeText(this, "Error sending drawing link message.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 1. Generate a unique push ID for the new message in Firebase (under the group's Messages node)
//        String messageKey = messagesRef.push().getKey();
//
//        if (messageKey == null) {
//            Log.e(TAG, "Failed to generate unique message key for drawing session message.");
//            Toast.makeText(this, "Error sending drawing link: Failed to generate message ID.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Log.d(TAG, "Generated Firebase push ID for drawing session message: " + messageKey);
//
//        // 2. Prepare the message data as a HashMap
//        HashMap<String, Object> messageInfoMap = new HashMap<>();
//        // Use a standard indicator message content
//        // This text will be displayed in the chat list.
//        String startMessageText = senderName + " started a shared drawing session."; // Customize this text
//        messageInfoMap.put("message", startMessageText);
//        messageInfoMap.put("type", "drawing_session"); // <<< CRUCIAL: The new message type
//        messageInfoMap.put("name", senderName); // Sender's display name
//        messageInfoMap.put("senderId", senderId); // Sender's UID
//        messageInfoMap.put("timestamp", ServerValue.TIMESTAMP); // Server Timestamp
//
//        // Add the link to the drawing session ID
//        messageInfoMap.put("drawingSessionId", sessionId); // <<< NEW FIELD added here
//
//        // Add initial readBy map - The sender has read their own message
//        HashMap<String, Object> readByMap = new HashMap<>();
//        readByMap.put(senderId, true); // Mark sender as read
//        messageInfoMap.put("readBy", readByMap);
//
//        // Add client-side date and time (Optional, Room Entity already has fields for this)
//        // Calendar calendar = Calendar.getInstance();
//        // SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
//        // String currentDate = dateFormat.format(calendar.getTime());
//        // SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//        // String currentTime = timeFormat.format(calendar.getTime());
//        // messageInfoMap.put("date", currentDate);
//        // messageInfoMap.put("time", currentTime);
//
//
//        // 3. Write the message data to Firebase
//        // Use messageKey generated by push() for consistency
//        messagesRef.child(messageKey).setValue(messageInfoMap)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "'drawing_session' message sent successfully to Firebase with key: " + messageKey + " for session: " + sessionId);
//                    // The Firebase sync listener (ChildEventListener) in GroupChatActivity
//                    // will pick this up, save it to Room (which now supports drawingSessionId),
//                    // and Room LiveData will update the UI.
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "'drawing_session' message failed to send to Firebase.", e);
//                    Toast.makeText(GroupChatActivity.this, "Failed to send drawing link message.", Toast.LENGTH_SHORT).show();
//                    // Error handling: Consider if the drawing session created earlier should be cleaned up
//                    // if the link message fails to send? For FYP, logging is likely enough.
//                });
//    }
//
//
//    // Inside GroupChatActivity.java
//
//    // --- NEW Method: Show confirmation dialog before starting drawing ---
//    private void confirmStartDrawingSession() {
//        if (this == null) {
//            Log.w(TAG, "Context is null, cannot show start drawing confirmation dialog.");
//            return;
//        }
//        Log.d(TAG, "Showing start drawing session confirmation dialog for group: " + groupId);
//
//        // Optional: Check if the user is still a member/admin here if needed.
//        // The checkUserMembership method already runs on start/resume and might disable UI.
//        // For simplicity, we assume the menu item isn't shown if they aren't members
//        // or we allow viewing but not starting if not members.
//        // A stricter check could be added here:
//        // if (!isUserMemberOrAdmin()) { // You would need a method to get this status
//        //     Toast.makeText(this, "You must be a group member to start a drawing.", Toast.LENGTH_SHORT).show();
//        //     return;
//        // }
//
//
//        new AlertDialog.Builder(this)
//                .setTitle("Start Shared Drawing")
//                .setMessage("Are you sure you want to start a new shared drawing session in this group? All members will be able to draw.")
//                .setPositiveButton("Yes, Start", (dialog, which) -> startDrawingSession()) // If "Yes, Start" clicked, call startDrawingSession()
//                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // If "Cancel" clicked, just dismiss
//                .setIcon(android.R.drawable.ic_dialog_info) // Optional: add an icon
//                .show();
//    }
//
//
//
//    // *** NEW Method to Attach ValueEventListener for Group Members Sync ***
//    // This listener watches the group's members list in Firebase.
//    // It populates the groupMemberUids list needed for sending notifications.
//    @SuppressLint("RestrictedApi")
//    private void attachGroupMembersListener() {
//        // Check if groupRef and groupId are initialized
//        if (groupRef == null || groupId == null) {
//            Log.e(TAG, "Cannot attach group members listener, groupRef or groupId is null.");
//            return;
//        }
//        // Check if the listener is already attached
//        if (groupMembersListener == null) {
//            Log.d(TAG, "Attaching Firebase ValueEventListener for group members for group: " + groupId);
//
//            groupMembersListener = new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    // This callback runs on the main thread when members data changes
//                    groupMemberUids.clear(); // Clear the old list
//
//                    if (snapshot.exists()) {
//                        // Loop through the UIDs under the 'members' node
//                        for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
//                            String memberId = memberSnapshot.getKey(); // Get the member's UID
//                            if (!TextUtils.isEmpty(memberId)) {
//                                groupMemberUids.add(memberId); // Add UID to the list
//                            }
//                        }
//                        Log.d(TAG, "Group members list updated. Total members: " + groupMemberUids.size());
//                        // Log the members list (optional)
//                        // Log.d(TAG, "Group members UIDs: " + groupMemberUids.toString());
//
//                    } else {
//                        // Members node does not exist or is empty (shouldn't happen for a valid group)
//                        Log.w(TAG, "Group members node not found or is empty for group: " + groupId);
//                        // groupMemberUids list is already cleared.
//                    }
//                    // The updated groupMemberUids list is now ready for use in SendMsgInfoToDatabase.
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.e(TAG, "Firebase Group Members Listener cancelled for group " + groupId + ": " + error.getMessage(), error.toException());
//                    // Handle error - maybe show a message to the user on the main thread
//                    // Or disable sending messages if membership list is crucial
//                    Toast.makeText(GroupChatActivity.this, "Failed to get group members list.", Toast.LENGTH_SHORT).show();
//                    // Clear the list on error, as we don't have accurate member info
//                    groupMemberUids.clear();
//                }
//            };
//
//            // Attach the listener to the 'members' node within the group reference
//            groupRef.child("members").addValueEventListener(groupMembersListener);
//            Log.d(TAG, "Firebase group members listener attached to: " + groupRef.child("members").getPath());
//        }
//    }
//    // *** END NEW Method to Attach ValueEventListener for Group Members Sync ***
//
//    // *** NEW Method to Remove ValueEventListener for Group Members ***
//    // Remove the listener when the activity is stopped to prevent memory leaks and unnecessary Firebase background activity.
//    private void removeGroupMembersListener() {
//        // Check if Firebase reference and listener are initialized
//        if (groupRef != null && groupMembersListener != null) {
//            Log.d(TAG, "Removing Firebase ValueEventListener for group members for group: " + groupId);
//            // Remove the listener from the Firebase reference
//            groupRef.child("members").removeEventListener(groupMembersListener);
//            groupMembersListener = null; // Nullify the reference
//        }
//    }
//    // *** END NEW Method to Remove ValueEventListener ***
//
//
//
//    // Inside GroupChatActivity.java class body { ... }
//
//    // *** NEW HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION TO GROUP MEMBERS ***
//    private void sendGroupPushNotification(OneSignalApiService apiService,
//                                           List<String> allMemberUids, // List of all members in the group
//                                           String senderUid, // UID of the user sending the message
//                                           String senderName, // Display name of the user sending the message
//                                           String groupName, // Name of the group
//                                           String messageContentPreview, // Preview of the message content
//                                           String groupId, // Group ID
//                                           String messageId, // Firebase message ID
//                                           String messageType) { // Type of the message (text, image, etc.)
//
//        // Check if API service is initialized and if there are members to send to
//        if (apiService == null || allMemberUids == null || allMemberUids.isEmpty()) {
//            Log.e(TAG, "sendGroupPushNotification: API service is null or member list is empty.");
//            return; // Cannot send notification
//        }
//
//        // Filter out the sender's UID from the list of recipients
//        // We need a list of UIDs who should *receive* the notification
//        List<String> recipientUids = new ArrayList<>();
//        if (senderUid != null) { // Ensure senderUid is not null before comparing
//            for (String memberUid : allMemberUids) {
//                // Add member UID to the recipient list if it's not the sender's UID
//                if (!memberUid.equals(senderUid)) {
//                    recipientUids.add(memberUid);
//                }
//            }
//        } else {
//            // If senderUid is null, we can't filter, maybe send to all members?
//            // Or log a warning and skip? Skipping is safer for security.
//            Log.w(TAG, "sendGroupPushNotification: Sender UID is null, cannot filter recipient list.");
//            // For safety, let's stop if senderUid is null
//            return;
//        }
//
//
//        // If after filtering, there are no recipients (e.g., group with only one member - the sender)
//        if (recipientUids.isEmpty()) {
//            Log.d(TAG, "sendGroupPushNotification: No recipients after filtering sender. Skipping notification.");
//            return;
//        }
//
//        Log.d(TAG, "Preparing OneSignal push notification for group " + groupId + " to " + recipientUids.size() + " members.");
//
//        // --- Build the JSON payload for OneSignal API ---
//        JsonObject notificationBody = new JsonObject();
//
//        // 1. Add App ID
//        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID); // Use your OneSignal App ID (class member)
//
//        // 2. Specify recipients using External User IDs (Firebase UIDs)
//        // Pass the list of recipient UIDs filtered above
//        JsonArray externalUserIdsArray = new JsonArray();
//        for(String uid : recipientUids) {
//            externalUserIdsArray.add(uid); // Add each recipient's Firebase UID
//        }
//        notificationBody.add("include_external_user_ids", externalUserIdsArray); // Use include_external_user_ids
//
//
//        // 3. Add Notification Title and Content
//        String notificationTitle = "New Message in " + (groupName != null ? groupName : "Your Group"); // Title: "New Message in [Group Name]"
//        // Content: Show sender name and message preview
//        String notificationContent = (senderName != null ? senderName : "A Member") + ": " + (messageContentPreview != null ? messageContentPreview : "[Message]");
//
//        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationTitle))); // Title passed
//        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationContent))); // Content passed
//
//        // Add custom data (important for handling notification click in the app)
//        JsonObject data = new JsonObject();
//        data.addProperty("groupId", groupId); // Pass Group ID so recipient can open the chat directly
//        data.addProperty("messageId", messageId); // Pass Message ID for deep linking (optional but good)
//        data.addProperty("senderId", senderUid); // Pass Sender ID
//        data.addProperty("messageType", messageType); // Pass message type (optional)
//        // You might want to add other data like recipientId if needed, though it's the current user
//        // data.addProperty("recipientId", recipientFirebaseUID); // Not needed when sending to a list of external IDs
//
//        notificationBody.add("data", data);
//
//        // Optional: Set small icon (recommended)
//        // Use the resource name of your app's small notification icon (e.g., "app_icon_circleup" or "ic_stat_onesignal_default")
//        notificationBody.addProperty("small_icon", "app_icon_circleup"); // <<< Replace with your icon's resource name (string)
//
//
//        // Optional: Customize notification appearance (sound, vibration, etc.)
//        // Check OneSignal API docs for more options: https://documentation.onesignal.com/reference/create-notification
//
//
//        // --- Make the API call asynchronously ---
//        Log.d(TAG, "Making OneSignal API call for group notification to " + recipientUids.size() + " recipients...");
//        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "OneSignal API call successful for group notification. Response Code: " + response.code());
//                    // Log response body for debugging success/failure
//                    try (ResponseBody responseBody = response.body()) {
//                        String resBody = responseBody != null ? responseBody.string() : "N/A";
//                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
//                        // Look for "id" and "recipients" in the response body JSON for confirmation
//                        // Example: {"id": "a62fddc6-5c02-4020-a7aa-2d022951bcf1", "recipients": 1} - The 'recipients' count confirms how many matched UIDs were found
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read success response body (group noti)", e);
//                    }
//                } else {
//                    Log.e(TAG, "OneSignal API call failed for group notification. Response Code: " + response.code());
//                    // Log error body for debugging failure reason
//                    try (ResponseBody errorBody = response.errorBody()) {
//                        String errBody = response.errorBody() != null ? response.errorBody().string() : "N/A"; // Corrected to errorBody().string()
//                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
//                        // Common errors: 400 (Invalid JSON), 403 (Invalid REST API Key), 404 (App ID not found), Invalid External IDs
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read error response body (group noti)", e);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.e(TAG, "OneSignal API call failed (network error) for group notification", t);
//            }
//        });
//        Log.d(TAG, "OneSignal API call enqueued for group notification.");
//    }
//    // *** END NEW HELPER METHOD ***
//
//// ... Rest of your GroupChatActivity class ...
//
//// ... rest of GroupChatActivity ...
//}
//
//
//




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


        // Initialize UI elements (Keep This, Ensure IDs Match Layout)
        InitializeFields();


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
            sendMessageButton.setOnClickListener(view -> {
                String messageContent = userMessageInput.getText().toString();
                if (TextUtils.isEmpty(messageContent)) {
                    Toast.makeText(this, "Please write a message first", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Send text message using the dedicated method
                SendMsgInfoToDatabase("text", messageContent, null); // Specify type, content, no session ID
                userMessageInput.setText(""); // Clear input field after sending
            });
        } else {
            Log.w(TAG, "Send message button or input field is null.");
        }

        // Send Image Message button click listener
        if (send_imgmsg_btn != null) { // Check for null
            send_imgmsg_btn.setOnClickListener(v -> {
                // Open image picker intent using ACTION_GET_CONTENT
                Intent intent = new Intent();
                intent.setType("image/*"); // Specify image type
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_PICK_REQUEST); // Use constant request code
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
                    if (groupNameTextView != null) groupNameTextView.setText("Group Not Found"); // Update UI
                    if (groupImageView != null) groupImageView.setImageResource(R.drawable.default_group_img); // Update UI
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
                if (groupImageView != null) groupImageView.setImageResource(R.drawable.default_group_img);
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
    private void SendMsgInfoToDatabase(String messageType, String messageContent, @Nullable String drawingSessionId) {
        // messageContent should be already validated and processed (text string, Base64 image string, or drawing session text)
        // messageType is "text", "image", or "drawing_session".
        // drawingSessionId is the ID of the drawing session, relevant only if messageType is "drawing_session".

        if (TextUtils.isEmpty(messageContent) && !"drawing_session".equals(messageType)) { // Allow empty content for drawing_session type message text
            Log.w(TAG, "Attempted to send empty message content for type: " + messageType + ". Skipping.");
            return; // Do not send empty messages (unless it's the drawing session placeholder message)
        }

        // Check if essential references and sender info are available
        if (messagesRef == null || currentUserID == null || currentUserID.isEmpty() || currentUserName == null) { // Added check for empty currentUserID
            Log.e(TAG, "messagesRef, currentUserID, or currentUserName is null/empty, cannot send message.");
            Toast.makeText(this, "Error sending message: Missing sender info or reference.", Toast.LENGTH_SHORT).show();
            return; // Stop if essential data is missing
        }
        Log.d(TAG, "Preparing to send " + messageType + " message.");

        // Generate a unique push ID for the new message in Firebase
        // This ID will serve as the primary key in the Room DB entity.
        String messageKey = messagesRef.push().getKey();

        if (messageKey == null) {
            Log.e(TAG, "Failed to generate unique message key from Firebase.");
            Toast.makeText(this, "Error sending message: Failed to generate ID.", Toast.LENGTH_SHORT).show();
            return; // Stop if key generation fails
        }
        Log.d(TAG, "Generated Firebase push ID: " + messageKey);

        // Prepare the message data as a HashMap to send to Firebase
        HashMap<String, Object> messageInfoMap = new HashMap<>();
        messageInfoMap.put("message", messageContent); // Message content
        messageInfoMap.put("type", messageType); // Message type ("text", "image", "drawing_session")
        messageInfoMap.put("name", currentUserName); // Sender's display name (fetched)
        messageInfoMap.put("senderId", currentUserID); // Sender's UID

        // Add server timestamp - CRUCIAL for sorting messages correctly across all users and devices
        messageInfoMap.put("timestamp", ServerValue.TIMESTAMP); // Use Firebase ServerValue

        // Add initial readBy map - The sender has read their own message upon sending
        HashMap<String, Object> readByMap = new HashMap<>();
        readByMap.put(currentUserID, true); // Mark sender as read with boolean true
        messageInfoMap.put("readBy", readByMap); // This will be stored as a nested map in Firebase

        // Add client-side date and time (Optional, primarily for display history or if timestamp fails)
        // Use current local time/date - Note: timestamp is more reliable for sorting
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()); // Use default locale
        String currentDate = dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Use default locale
        String currentTime = timeFormat.format(calendar.getTime());

        messageInfoMap.put("date", currentDate); // Client-side date string
        messageInfoMap.put("time", currentTime); // Client-side time string

        // *** NEW: Include drawingSessionId if messageType is "drawing_session" ***
        if ("drawing_session".equals(messageType) && !TextUtils.isEmpty(drawingSessionId)) {
            messageInfoMap.put("drawingSessionId", drawingSessionId); // Include the session ID
            Log.d(TAG, "Including drawingSessionId " + drawingSessionId + " in message payload.");
        }
        // *** END NEW ***


        // Write the message data to Firebase using the generated key (Asynchronous operation)
        messagesRef.child(messageKey).setValue(messageInfoMap)
                .addOnSuccessListener(aVoid -> {
                    // This callback runs on the main thread after the write is successful on the server
                    Log.d(TAG, messageType + " message sent successfully to Firebase with key: " + messageKey);
                    // Message sent toast is often annoying in chat, maybe remove in production
                    // Toast.makeText(GroupChatActivity.this, "Sent!", Toast.LENGTH_SHORT).show();

                    // The Firebase ChildEventListener attached in LoadMessages will automatically pick up this message
                    // (even the one you sent yourself) via onChildAdded and sync it into Room DB.
                    // The Room LiveData observer will then update the UI.


                    // *** NEW: Send Push Notification to the RECIPIENTS (other group members) ***
                    // Call the helper method here AFTER Firebase confirms write
                    // Ensure OneSignal API service, sender info, group name, and member list are available
                    if (oneSignalApiService != null && currentUserID != null && !currentUserID.isEmpty()
                            && currentUserName != null && !currentUserName.isEmpty()
                            && groupName != null && !groupName.isEmpty()
                            && groupMemberUids != null && !groupMemberUids.isEmpty()) // Ensure member list is loaded
                    {
                        Log.d(TAG, "Firebase message sent. Calling sendGroupPushNotification.");

                        // Determine the preview content for the notification
                        String notificationContentPreview;
                        if ("text".equals(messageType)) {
                            notificationContentPreview = messageContent; // Use actual text for preview
                        } else if ("image".equals(messageType)) {
                            notificationContentPreview = "[Image]"; // Placeholder for image
                        } else if ("drawing_session".equals(messageType)) { // Handle drawing session type notification content
                            notificationContentPreview = "[Drawing Session Started]"; // Placeholder for drawing session start
                        } else {
                            notificationContentPreview = "[Message]"; // Default placeholder
                        }


                        sendGroupPushNotification(
                                oneSignalApiService,
                                groupMemberUids, // List of all member UIDs (fetched by listener)
                                currentUserID, // Sender's UID
                                currentUserName, // Sender's display name
                                groupName, // Group name
                                notificationContentPreview, // Message preview for notification
                                groupId, // Group ID for custom data
                                messageKey, // Message ID for custom data
                                messageType // Message type for custom data
                        );
                    } else {
                        Log.e(TAG, "OneSignalApiService, sender info, groupName, or groupMemberUids is null/empty. Cannot send group push notification.");
                        // Optionally show a warning to the user that notification failed
                        // Toast.makeText(GroupChatActivity.this, "Notification failed.", Toast.LENGTH_SHORT).show();
                    }
                    // *** END NEW ***


                    // Optionally update Chat Summaries here if you have them for groups (less common than 1:1)
                    // updateGroupChatSummary(...); // If you have a summary node for groups


                })
                .addOnFailureListener(e -> {
                    // This callback runs on the main thread if the Firebase write fails
                    Log.e(TAG, messageType + " message failed to send to Firebase.", e);
                    Toast.makeText(GroupChatActivity.this, "Message failed to send.", Toast.LENGTH_SHORT).show();
                    // In a real app, you might save failed message locally in Room with a "failed" status
                    // and show a "retry" indicator in the UI.
                    // This would require adding a 'status' field to GroupMessageEntity and updating Room here.
                    // saveMessageToRoomLocallyWithStatus(messageKey, groupId, messageContent, messageType, currentUserName, currentUserID, ServerValue.TIMESTAMP, currentDate, currentTime, readByMap, "failed", drawingSessionId); // Need to add status field to Entity and DAO
                });
    }

    // Helper method for sending image messages (calls the main SendMsgInfoToDatabase)
    // Handles the image picking result and triggers the actual sending process.
    private void sendImageMessageToDatabase(String encodedImage) {
        if (TextUtils.isEmpty(encodedImage)) {
            Toast.makeText(this, "Could not process image for sending", Toast.LENGTH_SHORT).show();
            return;
        }
        // Pass the encoded image (Base64 string) as message content and type "image"
        SendMsgInfoToDatabase("image", encodedImage, null); // Use the main SendMsgInfoToDatabase with three arguments (drawingSessionId is null)
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

    // --- MODIFIED: Save/Update Message from Firebase Snapshot to Room DB ---
// This method is called by the onChildAdded and onChildChanged events of the Firebase listener.
// It extracts data from the Firebase DataSnapshot and saves/updates the MessageEntity in Room DB.
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


    // --- Method to mark a specific message as read in Firebase (Keep This Helper Method) ---
    // Called from onChildAdded for incoming messages that are not already read by this user.
    // This updates the 'readBy' map for this message in Firebase for the current user.
    private void markMessageAsReadInFirebase(DatabaseReference messageRef, String userId) {
        // Ensure messageRef and userId are valid
        if (messageRef == null || userId == null || userId.isEmpty()) { // Added check for empty userId
            Log.e(TAG, "markMessageAsReadInFirebase: messageRef or userId is null/empty.");
            return;
        }
        // Log.d(TAG, "Attempting to mark message as read in Firebase for user: " + userId); // Too verbose

        // The path to the specific user's entry in the readBy map for this message
        // messages/{groupId}/{messageId}/readBy/{userId}
        // We are given the messageRef (messages/{groupId}/{messageId}), so we just need to add /readBy/{userId}
        messageRef.child("readBy").child(userId).setValue(true) // Set value to boolean true to mark as read
                .addOnSuccessListener(aVoid -> {
                    // Log.d(TAG, "Marked message as read in Firebase successfully for user: " + userId); // Too verbose
                    // The Firebase onChildChanged listener will pick this up and sync to Room.
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark message as read in Firebase.", e);
                    // Handle failure (e.g., retry logic, log error)
                });
    }


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
        Log.d(TAG, "Initiating local clear chat process for group: " + groupId);

        // Clear local Room DB data for this group for the current user.
        // Run on background thread.
        if (groupMessageDao != null && databaseExecutor != null && groupId != null && !groupId.isEmpty()) { // Added check for groupId
            databaseExecutor.execute(() -> { // Use the shared DB executor for Room ops
                try {
                    // Delete all messages for this group from Room DB using the DAO method
                    int deletedRows = groupMessageDao.deleteAllMessagesForGroup(groupId); // Get the number of rows deleted

                    // Switch back to the main thread to show a Toast message
                    runOnUiThread(() -> {
                        if (deletedRows > 0) {
                            Log.d(TAG, "Cleared " + deletedRows + " messages from Room DB locally for group: " + groupId);
                            Toast.makeText(GroupChatActivity.this, "Chat cleared locally.", Toast.LENGTH_SHORT).show();
                            // LiveData observer will automatically update the RecyclerView UI to show an empty list
                        } else {
                            Log.w(TAG, "Attempted to clear local chat for group " + groupId + ", but no messages were found to delete.");
                            Toast.makeText(GroupChatActivity.this, "No messages to clear locally.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error clearing messages from Room DB locally for group " + groupId, e);
                    // Switch back to the main thread to show an error Toast
                    runOnUiThread(() -> {
                        Toast.makeText(GroupChatActivity.this, "Error clearing chat locally.", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Log.e(TAG, "groupMessageDao, databaseExecutor, or groupId is null/empty, cannot clear Room DB locally.");
            Toast.makeText(this, "Error clearing chat locally.", Toast.LENGTH_SHORT).show();
        }
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
        if (groupRef == null || currentUserID == null || currentUserID.isEmpty()) {
            Log.e(TAG, "groupRef or currentUserID is null/empty, cannot initiate exit group.");
            Toast.makeText(this, "Error initiating exit.", Toast.LENGTH_SHORT).show();
            // Do not finish here, just report the error initiating the process
            return;
        }

        // --- Step 1: Remove user from the group's members list in Firebase ---
        Log.d(TAG, "Removing user " + currentUserID + " from members list in Firebase for group " + groupId);
        groupRef.child("members").child(currentUserID).removeValue().addOnCompleteListener(task -> {
            // This callback runs on the main thread after the Firebase remove operation completes

            if (task.isSuccessful()) {
                Log.d(TAG, "User " + currentUserID + " successfully removed from group " + groupId + " members list in Firebase.");
                Toast.makeText(this, "You exited the group.", Toast.LENGTH_SHORT).show();

                // --- Step 2: Disable message input UI immediately ---
                // This prevents the user from sending new messages after successfully exiting.
                disableMessageInputUI();

                // --- Step 3: KEEP the local Room DB data ---
                // REMOVED: The previous block that called groupMessageDao.deleteAllMessagesForGroup is removed.
                // The user's local chat history in Room is preserved.

                // --- Step 4: Keep the activity open ---
                // REMOVED: The previous call to finish() is removed.
                // The user stays in the chat activity view to see the history.

                // The checkUserMembership logic (which might run on Activity start or resume)
                // will now find that the user's ID doesn't exist in the members list and will
                // call disableMessageInputUI(), ensuring the UI remains disabled on subsequent views.


            } else {
                Log.e(TAG, "Failed to remove user " + currentUserID + " from group " + groupId + " members list in Firebase.", task.getException());
                Toast.makeText(this, "Failed to exit group.", Toast.LENGTH_SHORT).show();
                // On failure, the user is still technically a member in Firebase.
                // The UI remains enabled (as per checkUserMembership if it runs again).
                // They would need to retry exiting.
            }
        });
        // --- End Step 1 ---
    }
    // --- END REWRITTEN exitGroup ---



    // Inside GroupChatActivity.java class body { ... }

    // --- NEW Helper methods to manage message input UI state ---
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
    private String encodeImageToBase64(Bitmap bitmap) {
        // Use android.util.Base64 consistently for Base64 encoding/decoding in Android
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Compress the bitmap to JPEG format with a specified quality (e.g., 70%)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Using 70% quality

        byte[] imageBytes = baos.toByteArray(); // Get the byte array from the stream

        // Encode the byte array into a Base64 string using Base64.DEFAULT flags
        return Base64.encodeToString(imageBytes, Base64.DEFAULT); // Use android.util.Base64.DEFAULT flags
    }


    // --- NEW Standard RecyclerView Adapter for GroupMessageEntity ---
    // This static inner class defines the adapter responsible for binding GroupMessageEntity data
    // from Room DB to the message list item layouts (e.g., message_item_sent.xml, message_item_received.xml).
    // This replaces the FirebaseRecyclerAdapter approach.
    // ... Assuming GroupMessageAdapter.java exists and works with GroupMessageEntity ...




    // Inside GroupChatActivity.java

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
    private void sendDrawingSessionStartedMessage(String sessionId, String senderName, String senderId) {
        Log.d(TAG, "Sending 'drawing_session' message for session: " + sessionId + " in group: " + groupId);

        // Check for essential data
        if (messagesRef == null || senderId == null || senderId.isEmpty() || senderName == null || sessionId == null || sessionId.isEmpty() || groupId == null || groupId.isEmpty()) { // Added empty checks
            Log.e(TAG, "Cannot send drawing session message: Required dependencies are null/empty.");
            Toast.makeText(this, "Error sending drawing link message.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Generate a unique push ID for the new message in Firebase (under the group's Messages node)
        String messageKey = messagesRef.push().getKey();

        if (messageKey == null) {
            Log.e(TAG, "Failed to generate unique message key for drawing session message.");
            Toast.makeText(this, "Error sending drawing link: Failed to generate message ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Generated Firebase push ID for drawing session message: " + messageKey);

        // 2. Prepare the message data as a HashMap
        HashMap<String, Object> messageInfoMap = new HashMap<>();
        // Use a standard indicator message content
        // This text will be displayed in the chat list.
        // NOTE: The actual UI rendering in the adapter will likely use the 'type' and 'drawingSessionId'
        // to show a special clickable view, not just this text.
        String startMessageText = senderName + " started a shared drawing session."; // Customize this text
        messageInfoMap.put("message", startMessageText); // Content of the message in chat history
        messageInfoMap.put("type", "drawing_session"); // <<< CRUCIAL: The new message type identifier
        messageInfoMap.put("name", senderName); // Sender's display name
        messageInfoMap.put("senderId", senderId); // Sender's UID
        messageInfoMap.put("timestamp", ServerValue.TIMESTAMP); // Server Timestamp

        // Add the link to the drawing session ID
        messageInfoMap.put("drawingSessionId", sessionId); // <<< NEW FIELD added here

        // Add initial readBy map - The sender has read their own message
        HashMap<String, Object> readByMap = new HashMap<>();
        readByMap.put(senderId, true); // Mark sender as read
        messageInfoMap.put("readBy", readByMap);

        // Add client-side date and time (Optional, Room Entity already has fields for this)
        // Calendar calendar = Calendar.getInstance();
        // SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        // String currentDate = dateFormat.format(calendar.getTime());
        // SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        // String currentTime = timeFormat.format(calendar.getTime());
        // messageInfoMap.put("date", currentDate);
        // messageInfoMap.put("time", currentTime);


        // 3. Write the message data to Firebase
        // Use messageKey generated by push() for consistency
        messagesRef.child(messageKey).setValue(messageInfoMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "'drawing_session' message sent successfully to Firebase with key: " + messageKey + " for session: " + sessionId);
                    // The Firebase sync listener (ChildEventListener) in GroupChatActivity
                    // will pick this up, save it to Room (which now supports drawingSessionId),
                    // and Room LiveData will update the UI.

                    // *** NEW: Send Push Notification for Drawing Session Started ***
                    // Call the helper method here AFTER Firebase confirms write
                    // Ensure OneSignal API service, sender info, group name, and member list are available
                    if (oneSignalApiService != null && currentUserID != null && !currentUserID.isEmpty()
                            && currentUserName != null && !currentUserName.isEmpty()
                            && groupName != null && !groupName.isEmpty()
                            && groupMemberUids != null && !groupMemberUids.isEmpty()) // Ensure member list is loaded
                    {
                        Log.d(TAG, "Drawing session message sent. Calling sendGroupPushNotification.");

                        String notificationContentPreview = "[Drawing Session Started]"; // Specific preview for drawing

                        sendGroupPushNotification(
                                oneSignalApiService,
                                groupMemberUids, // List of all member UIDs
                                currentUserID, // Sender's UID
                                currentUserName, // Sender's display name
                                groupName, // Group name
                                notificationContentPreview, // Message preview for notification
                                groupId, // Group ID for custom data
                                messageKey, // Message ID for custom data
                                "drawing_session" // Message type for custom data
                                // Optional: Add sessionId to custom data if needed for notification handling
                                // data.addProperty("sessionId", sessionId);
                        );
                    } else {
                        Log.e(TAG, "OneSignalApiService, sender info, groupName, or groupMemberUids is null/empty. Cannot send drawing session push notification.");
                        // Optionally show a warning to the user
                        // Toast.makeText(GroupChatActivity.this, "Notification failed.", Toast.LENGTH_SHORT).show();
                    }
                    // *** END NEW ***

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "'drawing_session' message failed to send to Firebase.", e);
                    Toast.makeText(GroupChatActivity.this, "Failed to send drawing link message.", Toast.LENGTH_SHORT).show();
                    // Error handling: Consider if the drawing session created earlier should be cleaned up
                    // if the link message fails to send? For FYP, logging is likely enough.
                    // You might need to delete the session node created in startDrawingSession if the message fails.
                    // This requires storing the sessionRef or sessionId and handling cleanup here.
                });
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

    // *** MODIFIED HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION TO GROUP MEMBERS ***
    // This method is called by SendMsgInfoToDatabase and sendDrawingSessionStartedMessage
    // It filters out the sender and sends the notification to the remaining members.
    private void sendGroupPushNotification(OneSignalApiService apiService,
                                           List<String> allMemberUids, // List of all members in the group (fetched by listener)
                                           String senderUid, // UID of the user sending the message (current user)
                                           String senderName, // Display name of the user sending the message
                                           String groupName, // Name of the group (fetched by loadGroupInfo)
                                           String messageContentPreview, // Preview of the message content (determined by message type)
                                           String groupId, // Group ID (from Intent)
                                           String messageId, // Firebase message ID (generated during send)
                                           String messageType) { // Type of the message (text, image, drawing_session)

        // Check if API service is initialized and if there are members to send to
        if (apiService == null || allMemberUids == null || allMemberUids.isEmpty() || senderUid == null || senderUid.isEmpty() || groupId == null || groupId.isEmpty() || messageId == null || messageId.isEmpty()) {
            Log.e(TAG, "sendGroupPushNotification: Essential parameters are null/empty. Cannot send notification.");
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

        // If after filtering, there are no recipients (e.g., group with only one member - the sender)
        if (recipientUids.isEmpty()) {
            Log.d(TAG, "sendGroupPushNotification: No recipients after filtering sender (" + senderUid + "). Skipping notification for group " + groupId + " message " + messageId + ".");
            return;
        }

        Log.d(TAG, "Preparing OneSignal push notification for group " + groupId + " to " + recipientUids.size() + " recipients.");

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
        String finalGroupName = (groupName != null && !groupName.isEmpty()) ? groupName : "Your Group";
        String finalSenderName = (senderName != null && !senderName.isEmpty()) ? senderName : "A Member";
        String finalContentPreview = (messageContentPreview != null && !messageContentPreview.isEmpty()) ? messageContentPreview : "[Message]";

        String notificationTitle = "New Message in " + finalGroupName; // Title: "New Message in [Group Name]"
        String notificationContent = finalSenderName + ": " + finalContentPreview; // Content: "[Sender Name]: [Message Preview]"

        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationTitle))); // Title passed
        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationContent))); // Content passed

        // 4. Add custom data (important for handling notification click in the app)
        JsonObject data = new JsonObject();
        // Set a distinct event type for group messages
        data.addProperty("eventType", "group_message"); // Generic type for group messages
        data.addProperty("groupId", groupId); // Pass Group ID so recipient can open the chat directly
        data.addProperty("messageId", messageId); // Pass Message ID for deep linking (optional but good)
        data.addProperty("senderId", senderUid); // Pass Sender ID
        data.addProperty("messageType", messageType); // Pass message type (text, image, drawing_session)

        // Add specific data based on message type
        if ("drawing_session".equals(messageType)) {
            // You'll need the sessionId here. It was passed to SendMsgInfoToDatabase.
            // Need to get it back somehow, or refactor to pass it through.
            // For now, assume you can get it if needed, or fetch it later.
            // If you want to navigate to the drawing session on click, you need the sessionId.
            // Let's try to pass it from sendDrawingSessionStartedMessage.
            // (Need to refactor SendMsgInfoToDatabase to pass sessionId to this method if type is drawing_session)
            // For now, I'll pass null below and add a note.

            // NOTE: To pass sessionId here, modify SendMsgInfoToDatabase to accept @Nullable String drawingSessionId
            // and pass it down to this method.
            // Example if sessionId was passed:
            // data.addProperty("sessionId", drawingSessionId); // If available
            Log.w(TAG, "sessionId is not passed to sendGroupPushNotification. Cannot include in data.");
        }


        // Optional: Target the user who sent the message or the group chat screen on tap
        // data.addProperty("targetScreen", "GroupChatActivity");
        // data.addProperty("targetGroupId", groupId);


        notificationBody.add("data", data);

        // Optional: Set small icon (recommended)
        // Use the resource name of your app's small notification icon (e.g., "app_icon_circleup" or "ic_stat_onesignal_default")
        notificationBody.addProperty("small_icon", "app_icon_circleup"); // <<< Replace with your icon's resource name (string)


        // Optional: Customize notification appearance (sound, vibration, etc.)
        // Check OneSignal API docs for more options: https://documentation.onesignal.com/reference/create-notification


        // --- Make the API call asynchronously using Retrofit ---
        Log.d(TAG, "Making OneSignal API call for group message notification...");
        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "OneSignal API call successful for group message notification. Response Code: " + response.code());
                    // Log response body for debugging success/failure
                    try (ResponseBody responseBody = response.body()) {
                        String resBody = responseBody != null ? responseBody.string() : "N/A";
                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
                        // Look for "id" and "recipients" in the response body JSON for confirmation
                        // Example: {"id": "a62fddc6-5c02-4020-a7aa-2d022951bcf1", "recipients": 1} - The 'recipients' count confirms how many matched UIDs were found
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read success response body (group noti)", e);
                    }
                } else {
                    Log.e(TAG, "OneSignal API call failed for group message notification. Response Code: " + response.code());
                    // Log error body for debugging failure reason
                    try (ResponseBody errorBody = response.errorBody()) {
                        String errBody = errorBody != null ? errorBody.string() : "N/A"; // Corrected to errorBody().string()
                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
                        // Common errors: 400 (Invalid JSON), 403 (Invalid REST API Key), 404 (App ID not found), Invalid External IDs
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


}