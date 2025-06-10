package com.sana.circleup.temporary_chat_room;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sana.circleup.Contacts;
import com.sana.circleup.Login;
import com.sana.circleup.R;
import com.sana.circleup.UserModel;
import com.sana.circleup.one_signal_notification.OneSignalApiService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



//
//public class TemporaryChatRoomMain extends AppCompatActivity {
//
//    private static final String TAG = "TempRoomMain"; // Define TAG for logging
//
//    Button createRoomBtn, joinRoomBtn;
//    // Button showRoom; // Button was commented out in XML, commenting out here too for now
//
//    // To store the selected expiry date and time before creating the room
//    private Calendar selectedExpiryCalendar;
//
//    // Reference to the main Users node (needed for fetching invited user details and sender names)
//    private DatabaseReference usersRef;
//
//    // --- List to hold selected contact UIDs for the temporary room ---
//    private List<String> invitedMemberUids = new ArrayList<>(); // Declare this field
//
//    // *** NEW MEMBER VARIABLE TO ADD ***
//    private String currentUserName; // To store the name of the current logged-in user (the creator/joiner)
//    private String currentUserId; // Store current user ID
//    // *** END NEW MEMBER ***
//
//
//    // *** NEW MEMBER: Retrofit Service for OneSignal API ***
//    private OneSignalApiService oneSignalApiService;
//
//    // Get this from your OneSignal Dashboard -> Settings -> Keys & IDs
//    // ENSURE THIS MATCHES THE APP ID USED IN ChatPageActivity and GroupChatActivity
//    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR ACTUAL APP ID if different
//
//    // *** END NEW MEMBER ***
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_temporary_chat_room_main);
//
//        Log.d(TAG, "🟢 TemporaryChatRoomMain launched");
//
//        // Initialize Firebase Auth and get current user
//        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        // Check if user is authenticated. Redirect to login if not.
//        if (currentUser == null) {
//            Log.w(TAG, "No current user authenticated. Redirecting to Login.");
//            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
//            sendUserToLoginActivity(); // Navigate to login and finish
//            return; // Stop further execution
//        }
//        currentUserId = currentUser.getUid(); // Get current user ID
//
//        // Initialize Firebase Users ref (needed for fetching user details for invitation and sender names)
//        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
//
//        // Fetch current user's username from Firebase (Needed for notification sender name)
//        fetchCurrentUserName(); // Call the new helper method to fetch sender's name
//
//
//        // Initialize buttons
//        createRoomBtn = findViewById(R.id.createRoom);
//        joinRoomBtn = findViewById(R.id.joinRoom);
//        // showRoom = findViewById(R.id.showRoom); // Commented out as per XML
//
//
//        // *** NEW: Initialize Retrofit Service for OneSignal API ***
//        try {
//            Retrofit retrofit = new Retrofit.Builder()
//                    // Base URL for OneSignal API (DO NOT CHANGE THIS)
//                    .baseUrl("https://onesignal.com/")
//                    // Add Gson converter factory
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//            // Create an instance of your API service interface
//            oneSignalApiService = retrofit.create(OneSignalApiService.class);
//            Log.d(TAG, "OneSignalApiService initialized.");
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to initialize OneSignalApiService", e);
//            // Handle this error - perhaps disable buttons if notifications are critical?
//            createRoomBtn.setEnabled(false);
//            joinRoomBtn.setEnabled(false);
//            Toast.makeText(this, "Error initializing notification service.", Toast.LENGTH_SHORT).show();
//        }
//        // --- *** END NEW *** ---
//
//
//        // --- Click Listeners ---
//
//        createRoomBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Clear previous invited UIDs when opening dialog
//                invitedMemberUids.clear(); // IMPORTANT: Start fresh for each room creation attempt
//                showCreateRoomDialog(); // Call function to show the creation dialog
//            }
//        });
//
//        // Click listener for Join Chat Rooms button
//        joinRoomBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showJoinRoomDialog(); // Call the new method to show join dialog
//            }
//        });
//
//        Log.d(TAG, "📲 onCreate finished in TemporaryChatRoomMain");
//    }
//
//    // Helper method to navigate to login activity
//    private void sendUserToLoginActivity() {
//        Log.d(TAG, "Redirecting to Login Activity.");
//        Intent loginIntent = new Intent(TemporaryChatRoomMain.this, Login.class); // Replace Login with your actual Login Activity class
//        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
//        startActivity(loginIntent);
//        finish(); // Close this activity after redirecting
//    }
//
//
//    // *** NEW HELPER METHOD TO ADD: to fetch current user's name ***
//    private void fetchCurrentUserName() {
//        if (TextUtils.isEmpty(currentUserId) || usersRef == null) {
//            Log.w(TAG, "fetchCurrentUserName: currentUserId or usersRef is empty/null, cannot fetch name.");
//            currentUserName = "A User"; // Default if ID is missing or ref is null
//            return;
//        }
//
//        Log.d(TAG, "Fetching current user's name for UID: " + currentUserId);
//
//        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists() && snapshot.hasChild("username")) {
//                    String name = snapshot.child("username").getValue(String.class);
//                    if (!TextUtils.isEmpty(name)) {
//                        currentUserName = name; // Store the fetched name
//                        Log.d(TAG, "Fetched current user name: " + currentUserName);
//                    } else {
//                        currentUserName = "A User"; // Default if username field is empty
//                        Log.w(TAG, "Current user's username field is empty. Using default.");
//                    }
//                } else {
//                    currentUserName = "A User"; // Default if user data or username field is missing
//                    Log.w(TAG, "Current user data or username field not found in DB. Using default.");
//                }
//                // The currentUserName is now available for use in notifications
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to fetch current user name from DB", error.toException());
//                currentUserName = "A User"; // Default on error
//            }
//        });
//    }
//    // *** END NEW HELPER METHOD TO ADD ***
//
//
//    // Function to show the dialog for creating a temporary chat room
//    private void showCreateRoomDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        // Inflate the custom layout for the dialog
//        View view = getLayoutInflater().inflate(R.layout.dialog_create_temp_room, null); // Ensure this layout exists
//        builder.setView(view);
//        AlertDialog dialog = builder.create(); // Create the dialog
//
//        // --- Initialize views from the dialog layout ---
//        EditText editRoomName = view.findViewById(R.id.editRoomName); // Ensure this ID exists
//        TextView textTimePicker = view.findViewById(R.id.textTimePicker); // Ensure this ID exists
//        Button generateIdBtn = view.findViewById(R.id.generateid); // Ensure this ID exists
//        Button btnCopyId = view.findViewById(R.id.btnCopyId); // Ensure this ID exists
//        Button btnInvite = view.findViewById(R.id.btnInvite); // Ensure this ID exists
//        Button btnCreate = view.findViewById(R.id.btnCreateRoom); // Ensure this ID exists
//        TextView textRoomId = view.findViewById(R.id.textRoomId); // Ensure this ID exists
//        LinearLayout idLayout = view.findViewById(R.id.generate_room_id_layout); // Ensure this ID exists
//
//        // Variables to hold the generated room ID and selected expiry time (Calendar object)
//        final String[] generatedRoomId = {null};
//        selectedExpiryCalendar = null; // Initialize the Calendar object
//
//
//        // --- Click Listeners for Dialog elements ---
//
//        // Click listener for Generate Room ID button
//        generateIdBtn.setOnClickListener(v -> {
//            generatedRoomId[0] = generateRandomRoomId(8); // Generate an 8-character random ID
//            textRoomId.setText("ID: " + generatedRoomId[0]); // Display the ID
//            idLayout.setVisibility(View.VISIBLE); // Make the layout visible
//        });
//
//        // Click listener for Copy ID button
//        btnCopyId.setOnClickListener(v -> {
//            if (generatedRoomId[0] != null) {
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("Room ID", generatedRoomId[0]);
//                if (clipboard != null) {
//                    clipboard.setPrimaryClip(clip);
//                    Toast.makeText(this, "Room ID copied!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "Clipboard service not available.", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(this, "Generate ID first!", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Click listener for Select Expiry Time (Date and Time Picker)
//        textTimePicker.setOnClickListener(v -> {
//            // Get current date and time to initialize pickers
//            final Calendar c = Calendar.getInstance();
//            int year = c.get(Calendar.YEAR);
//            int month = c.get(Calendar.MONTH);
//            int day = c.get(Calendar.DAY_OF_MONTH);
//
//            // Show Date Picker Dialog first
//            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
//                    (view1, selectedYear, selectedMonth, selectedDay) -> {
//                        // Store the selected date
//                        final Calendar dateSelected = Calendar.getInstance();
//                        dateSelected.set(selectedYear, selectedMonth, selectedDay);
//
//                        // Now show Time Picker Dialog
//                        int hour = c.get(Calendar.HOUR_OF_DAY);
//                        int minute = c.get(Calendar.MINUTE);
//
//                        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
//                                (view2, hourOfDay, minuteOfHour) -> {
//                                    // Store the selected time with the selected date
//                                    dateSelected.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                                    dateSelected.set(Calendar.MINUTE, minuteOfHour);
//                                    dateSelected.set(Calendar.SECOND, 0); // Set seconds to 0 for consistency
//
//                                    selectedExpiryCalendar = dateSelected; // Store the full date and time
//
//                                    // Format and display the selected date and time
//                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//                                    String formattedDateTime = sdf.format(selectedExpiryCalendar.getTime());
//                                    textTimePicker.setText("Expiry: " + formattedDateTime);
//
//                                    // Basic validation: Check if selected time is in the past
//                                    if (selectedExpiryCalendar.before(Calendar.getInstance())) {
//                                        Toast.makeText(this, "Selected expiry time is in the past!", Toast.LENGTH_LONG).show();
//                                        selectedExpiryCalendar = null; // Reset if in past
//                                        textTimePicker.setText("Select Expiry Time"); // Reset text
//                                    }
//
//                                }, hour, minute, false); // 'false' for 12-hour format, 'true' for 24-hour format
//                        timePickerDialog.show();
//                    }, year, month, day);
//
//            // Optional: Restrict past dates in the Date Picker (requires min date)
//            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Prevent selecting dates in the past
//
//            datePickerDialog.show(); // Show the Date Picker
//        });
//
//
//        // Click listener for Invite Someone button (Placeholder)
//        // --- NEW: Click listener for Invite Someone button ---
//        btnInvite.setOnClickListener(v -> {
//            // Fetch Current User's Contacts who are "friends"
//            if (TextUtils.isEmpty(currentUserId)) {
//                Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference()
//                    .child("Contacts").child(currentUserId);
//
//            contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    List<Contacts> userFriends = new ArrayList<>(); // Use UserContact list
//                    for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
//                        // The key is the friend's UID
//                        String friendUid = contactSnapshot.getKey();
//
//                        // Read the details stored under this friend's UID node
//                        Contacts friendContact = contactSnapshot.getValue(Contacts.class);
//
//                        if (friendContact != null && friendUid != null && !friendUid.equals(currentUserId)) {
//                            // Assuming you only want to invite actual friends ("request_type" is "accepted")
//                            // Adjust this condition based on your contact structure and what indicates a "friend"
//                            // Check if the friendContact object is complete enough,
//                            // e.g., if it has a username and isn't blocked by the current user.
//                            // You might need to fetch full user details from /Users node if not all info is under /Contacts
//                            // For now, let's assume the UserContact object from /Contacts is sufficient
//
//                            // ** IMPORTANT: Set the UID in the UserContact object **
//                            // If the UID is not automatically mapped from the key, set it manually
//                            friendContact.setUid(friendUid);
//
//                            // Add to list if they are actual friends (based on your Firebase structure)
//                            if ("accepted".equals(friendContact.getRequest_type())) { // Check the request_type
//                                userFriends.add(friendContact);
//                            }
//                        }
//                    }
//
//                    // --- Show Contact Selection Dialog ---
//                    if (userFriends.isEmpty()) {
//                        Toast.makeText(TemporaryChatRoomMain.this, "You have no friends to invite.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    // Show the dialog using the fetched friends list and pass the list to update
//                    showContactSelectionDialog(userFriends, invitedMemberUids);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.e(TAG, "Failed to load contacts for invitation: " + error.getMessage());
//                    Toast.makeText(TemporaryChatRoomMain.this, "Failed to load contacts for invitation.", Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
//        // --- END NEW: Click listener for Invite Someone button ---
//
//
//        // Click listener for Create Room button
//        btnCreate.setOnClickListener(v -> {
//            String roomName = editRoomName.getText().toString().trim(); // Get room name
//            String roomId = generatedRoomId[0]; // Get generated ID
//            // currentUserId is already a member variable
//            // currentUserName is already a member variable (fetched asynchronously)
//
//            // --- Basic Validation ---
//            if (TextUtils.isEmpty(roomName)) {
//                editRoomName.setError("Room name is required");
//                editRoomName.requestFocus();
//                return;
//            }
//            if (TextUtils.isEmpty(roomId)) {
//                Toast.makeText(this, "Please generate Room ID first!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (selectedExpiryCalendar == null) {
//                Toast.makeText(this, "Please select expiry time!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (selectedExpiryCalendar.before(Calendar.getInstance())) {
//                Toast.makeText(this, "Expiry time cannot be in the past!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (TextUtils.isEmpty(currentUserId)) {
//                Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
//                // Maybe redirect to login here?
//                return;
//            }
//            // --- End Validation ---
//
//
//            // Get Firebase database reference for the temporary room
//            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("temporaryChatRooms").child(roomId);
//
//            // Create a map to store room data
//            Map<String, Object> roomData = new HashMap<>();
//            roomData.put("roomName", roomName);
//            roomData.put("roomId", roomId);
//            // Store expiry time as a long (timestamp in milliseconds)
//            roomData.put("expiryTime", selectedExpiryCalendar.getTimeInMillis());
//            roomData.put("createdBy", currentUserId); // Use member variable
//            roomData.put("timestamp", ServerValue.TIMESTAMP); // Timestamp for creation time
//
//            // Add members node with the creator as the initial member
//            Map<String, Object> members = new HashMap<>();
//            members.put(currentUserId, true); // Store creator's UID with a boolean flag (common practice)
//
//            // Add invited members from the list
//            for (String uid : invitedMemberUids) {
//                if (uid != null && !uid.isEmpty()) { // Basic check
//                    members.put(uid, true);
//                }
//            }
//            roomData.put("members", members); // Add members map to room data
//
//
//            // Write room data to Firebase
//            btnCreate.setEnabled(false); // Disable button to prevent double click
//            roomRef.setValue(roomData).addOnCompleteListener(task -> {
//                btnCreate.setEnabled(true); // Re-enable button
//
//                if (task.isSuccessful()) {
//                    Log.d(TAG, "Temporary Room '" + roomName + "' created successfully with ID: " + roomId);
//                    Toast.makeText(this, "Room '" + roomName + "' created!", Toast.LENGTH_LONG).show();
//                    dialog.dismiss(); // Close the dialog on success
//
//                    // *** NEW: Send Push Notification for Room Creation ***
//                    // Include the creator (currentUserId) and all invited members
//                    // Ensure oneSignalApiService and currentUserName are available
//                    if (oneSignalApiService != null && !TextUtils.isEmpty(currentUserName)) {
//                        // Combine creator and invited members for notification recipients
//                        List<String> notificationRecipients = new ArrayList<>(invitedMemberUids);
//                        if (!TextUtils.isEmpty(currentUserId)) {
//                            notificationRecipients.add(0, currentUserId); // Add creator at the beginning
//                        }
//
//                        Log.d(TAG, "Room created. Calling sendRoomCreationNotification.");
//                        sendRoomCreationNotification(
//                                roomName, // Room name
//                                roomId, // Room ID
//                                currentUserId, // Created by (Sender UID)
//                                currentUserName, // Creator's name (Sender Name)
//                                notificationRecipients // List of all members (creator + invited)
//                        );
//                    } else {
//                        Log.e(TAG, "Cannot send room creation notification: API service or currentUserName is null/empty.");
//                    }
//                    // *** END NEW ***
//
//
//                    // TODO: Maybe navigate to the newly created chat room or show it in a list
//                    // Start the TemporaryRoomChatActivity
//                    Intent chatIntent = new Intent(TemporaryChatRoomMain.this, TemporaryRoomChatActivity.class); // Replace with your actual Chat Activity for Temp Rooms
//                    chatIntent.putExtra("roomId", roomId); // Pass the created room ID
//                    startActivity(chatIntent);
//
//
//                } else {
//                    // Handle errors during creation
//                    Log.e(TAG, "Error creating temporary room: " + task.getException().getMessage(), task.getException());
//                    Toast.makeText(this, "Error creating room: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
//
//        // Show the AlertDialog
//        dialog.show();
//    }
//
//
//    // --- NEW: Function to show contact selection dialog ---
//    private void showContactSelectionDialog(List<Contacts> contacts, List<String> invitedUidsListToUpdate) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Invite Contacts");
//
//        // Use a custom layout for the dialog containing a RecyclerView
//        LayoutInflater inflater = getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.temporaryroom_dialog_select_contacts, null); // Ensure this layout exists and matches
//        RecyclerView contactsRecyclerView = dialogView.findViewById(R.id.recycler_view_contacts); // Match ID from dialog_select_contacts.xml
//
//        // Initialize RecyclerView
//        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Create and set the adapter for selecting contacts
//        // Pass the list of already invited UIDs to the adapter so it can pre-select them
//        InviteSelectContactAdapter adapter = new InviteSelectContactAdapter(contacts, new ArrayList<>(invitedUidsListToUpdate)); // Pass a copy of current invited list
//        contactsRecyclerView.setAdapter(adapter);
//
//        builder.setView(dialogView); // Set the custom layout
//
//        // Add "Done" button
//        builder.setPositiveButton("Done", (dialog, which) -> {
//            // Get selected UIDs from the adapter
//            List<String> selectedUids = adapter.getSelectedUids();
//            // Clear previous selections and add new ones to the list that will be used for room creation
//            invitedUidsListToUpdate.clear(); // Clear the class-level list
//            invitedUidsListToUpdate.addAll(selectedUids); // Add newly selected UIDs
//
//            // Optional: Show a toast with selected names (fetched by adapter)
//            List<String> selectedNames = adapter.getSelectedNames();
//            if (!selectedNames.isEmpty()) {
//                Toast.makeText(this, "Selected: " + TextUtils.join(", ", selectedNames), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "No contacts selected.", Toast.LENGTH_SHORT).show();
//            }
//            dialog.dismiss(); // Close dialog
//        });
//
//        // Add "Cancel" button
//        builder.setNegativeButton("Cancel", (dialog, which) -> {
//            // Just close the dialog. The invitedMemberUids list will retain its state
//            // from before the dialog was opened.
//            dialog.cancel();
//        });
//
//        builder.create().show(); // Create and show the dialog
//    }
//    // --- END NEW: Function to show contact selection dialog ---
//
//
//    // Function to generate a random alphanumeric string for Room ID
//    private String generateRandomRoomId(int length) {
//        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//        StringBuilder id = new StringBuilder();
//        Random random = new Random();
//        for (int i = 0; i < length; i++) {
//            id.append(chars.charAt(random.nextInt(chars.length())));
//        }
//        return id.toString();
//    }
//
//
//    // Function to show the dialog for joining a temporary chat room
//    private void showJoinRoomDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.dialog_join_temp_room, null); // Ensure this layout exists and matches
//        builder.setView(dialogView);
//
//        EditText editRoomIdToJoin = dialogView.findViewById(R.id.editRoomIdToJoin); // Ensure this ID exists
//        Button btnJoin = dialogView.findViewById(R.id.btnJoinRoomInDialog); // Ensure this ID exists
//        // Optional: Get status TextView or ProgressBar
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        // Set Click Listener for the custom Join button AFTER showing the dialog
//        if (btnJoin != null) {
//            btnJoin.setOnClickListener(v -> {
//                String enteredRoomId = editRoomIdToJoin.getText().toString().trim().toUpperCase();
//
//                if (TextUtils.isEmpty(enteredRoomId)) {
//                    Toast.makeText(this, "Please enter a Room ID", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                // currentUserId and currentUserName are already member variables
//                if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(currentUserName) || usersRef == null) {
//                    Toast.makeText(this, "User information missing. Cannot join.", Toast.LENGTH_SHORT).show();
//                    // Consider fetching user info here if it wasn't available earlier
//                    // fetchCurrentUserName(); // Could call again, but it's async. Safer to ensure it's fetched on Create.
//                    return;
//                }
//
//
//                btnJoin.setEnabled(false); // Optional: Disable button during check
//                // statusTextView.setText("Checking room..."); // Optional status update
//
//
//                DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("temporaryChatRooms").child(enteredRoomId);
//                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        // This runs on the main thread
//                        btnJoin.setEnabled(true); // Optional: Re-enable button
//
//                        if (!snapshot.exists()) {
//                            Toast.makeText(TemporaryChatRoomMain.this, "Room not found with this ID.", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // Room exists, check expiry and membership
//                            Long expiryTime = snapshot.child("expiryTime").getValue(Long.class);
//                            DataSnapshot membersSnapshot = snapshot.child("members");
//                            String roomName = snapshot.child("roomName").getValue(String.class); // Get room name for notification
//
//                            // Get existing members list BEFORE adding the new member (for notification recipients)
//                            List<String> existingMemberUidsBeforeJoin = new ArrayList<>();
//                            if (membersSnapshot.exists()) {
//                                for (DataSnapshot memberSnap : membersSnapshot.getChildren()) {
//                                    String memberUid = memberSnap.getKey();
//                                    if (!TextUtils.isEmpty(memberUid)) {
//                                        existingMemberUidsBeforeJoin.add(memberUid);
//                                    }
//                                }
//                            }
//
//
//                            if (expiryTime != null && System.currentTimeMillis() > expiryTime) {
//                                Toast.makeText(TemporaryChatRoomMain.this, "This room has expired.", Toast.LENGTH_SHORT).show();
//                                dialog.dismiss(); // Close expired room dialog
//
//                                // Optional: Delete expired room from Firebase
//                                // roomRef.removeValue(); // Dangerous, only do if sure
//                                // Log.w(TAG, "Attempted to join expired room. Consider cleaning up expired rooms.");
//
//
//                            } else if (membersSnapshot.hasChild(currentUserId)) {
//                                // User is already a member
//                                Toast.makeText(TemporaryChatRoomMain.this, "You are already a member of this room.", Toast.LENGTH_SHORT).show();
//                                dialog.dismiss(); // Close dialog
//                                // Navigate to the chat activity
//                                Intent chatIntent = new Intent(TemporaryChatRoomMain.this, TemporaryRoomChatActivity.class); // Replace with your actual Chat Activity for Temp Rooms
//                                chatIntent.putExtra("roomId", enteredRoomId);
//                                startActivity(chatIntent);
//
//                            } else {
//                                // User is not a member and room is not expired. Add user to members!
//                                Log.d(TAG, "User " + currentUserId + " is joining room: " + enteredRoomId);
//                                Map<String, Object> memberUpdate = new HashMap<>();
//                                memberUpdate.put(currentUserId, true); // Add current user UID
//
//                                roomRef.child("members").updateChildren(memberUpdate)
//                                        .addOnCompleteListener(task -> {
//                                            // This runs on the main thread
//                                            btnJoin.setEnabled(true); // Re-enable button
//
//                                            if (task.isSuccessful()) {
//                                                Log.d(TAG, "User " + currentUserId + " successfully joined room: " + enteredRoomId);
//                                                Toast.makeText(TemporaryChatRoomMain.this, "Joined room successfully!", Toast.LENGTH_SHORT).show();
//                                                dialog.dismiss(); // Close dialog
//
//                                                // *** NEW: Send Push Notification for Room Join ***
//                                                // Notify existing members (the ones in existingMemberUidsBeforeJoin list)
//                                                // Ensure oneSignalApiService and currentUserName are available
//                                                if (oneSignalApiService != null && !TextUtils.isEmpty(currentUserName)) {
//                                                    Log.d(TAG, "User joined. Calling sendRoomJoinNotification.");
//                                                    sendRoomJoinNotification(
//                                                            roomName, // Room name (fetched earlier)
//                                                            enteredRoomId, // Room ID
//                                                            currentUserId, // Joining User UID
//                                                            currentUserName, // Joining User Name
//                                                            existingMemberUidsBeforeJoin // List of members BEFORE this user joined
//                                                    );
//                                                } else {
//                                                    Log.e(TAG, "Cannot send room join notification: API service or currentUserName is null/empty.");
//                                                }
//                                                // *** END NEW ***
//
//
//                                                // Navigate to the chat activity
//                                                Intent chatIntent = new Intent(TemporaryChatRoomMain.this, TemporaryRoomChatActivity.class); // Replace with your actual Chat Activity for Temp Rooms
//                                                chatIntent.putExtra("roomId", enteredRoomId);
//                                                startActivity(chatIntent);
//
//
//                                            } else {
//                                                Log.e(TAG, "Failed to update members list for join: " + task.getException().getMessage(), task.getException());
//                                                Toast.makeText(TemporaryChatRoomMain.this, "Failed to join room: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                                // statusTextView.setText("Join Failed"); // Optional status update
//                                            }
//                                        });
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Log.e(TAG, "Firebase query cancelled during join check: " + error.getMessage(), error.toException());
//                        Toast.makeText(TemporaryChatRoomMain.this, "Error checking room.", Toast.LENGTH_SHORT).show();
//                        btnJoin.setEnabled(true); // Re-enable button
//                        dialog.dismiss();
//                        // statusTextView.setText("Error"); // Optional status update
//                    }
//                });
//            });
//        } else {
//            Log.e(TAG, "Join button (btnJoinRoomInDialog) not found in dialog layout!");
//            Toast.makeText(this, "Error: Join button not found.", Toast.LENGTH_SHORT).show();
//            dialog.dismiss(); // Dismiss if button not found
//        }
//        // Cancel button is added by the builder automatically
//    }
//
//
//    // *** NEW HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION for Room Creation ***
//    private void sendRoomCreationNotification(String roomName, String roomId, String createdByUid, String creatorName, List<String> allMemberUids) {
//
//        // Check if API service is initialized and if there are members to send to
//        if (oneSignalApiService == null || allMemberUids == null || allMemberUids.isEmpty()) {
//            Log.e(TAG, "sendRoomCreationNotification: API service is null or member list is empty.");
//            return; // Cannot send notification
//        }
//
//        Log.d(TAG, "Preparing OneSignal push notification for room creation (ID: " + roomId + ") to " + allMemberUids.size() + " members.");
//
//        // --- Build the JSON payload for OneSignal API ---
//        JsonObject notificationBody = new JsonObject();
//
//        // 1. Add App ID
//        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID); // Use your OneSignal App ID (class member)
//
//        // 2. Specify recipients using External User IDs (Firebase UIDs)
//        // Include ALL member UIDs (creator + invited)
//        JsonArray externalUserIdsArray = new JsonArray();
//        for(String uid : allMemberUids) {
//            if (!TextUtils.isEmpty(uid)) {
//                externalUserIdsArray.add(uid); // Add each member's Firebase UID
//            }
//        }
//        if (externalUserIdsArray.size() == 0) {
//            Log.w(TAG, "No valid recipient UIDs after filtering. Skipping notification.");
//            return; // No valid recipients
//        }
//        notificationBody.add("include_external_user_ids", externalUserIdsArray); // Use include_external_user_ids
//
//
//        // 3. Add Notification Title and Content
//        String notificationTitle = "New Temporary Room: " + (roomName != null ? roomName : "Unnamed Room"); // Title
//        String notificationContent = (creatorName != null ? creatorName : "A User") + " created room '" + (roomName != null ? roomName : "Unnamed Room") + "'. ID: " + roomId; // Content
//
//        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationTitle))); // Title passed
//        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationContent))); // Content passed
//
//        // Add custom data (important for handling notification click in the app)
//        // Include roomId and perhaps the type of event ("room_created")
//        JsonObject data = new JsonObject();
//        data.addProperty("eventType", "room_created"); // Custom key to identify notification type
//        data.addProperty("roomId", roomId); // Pass Room ID
//        data.addProperty("createdBy", createdByUid); // Pass Creator's UID (sender)
//        // You might want to add other relevant data
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
//        Log.d(TAG, "Making OneSignal API call for room creation notification...");
//        oneSignalApiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "OneSignal API call successful for room creation notification. Response Code: " + response.code());
//                    // Log response body for debugging success/failure
//                    try (ResponseBody responseBody = response.body()) {
//                        String resBody = responseBody != null ? responseBody.string() : "N/A";
//                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
//                        // Look for "id" and "recipients" in the response body JSON for confirmation
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read success response body (room create noti)", e);
//                    }
//                } else {
//                    Log.e(TAG, "OneSignal API call failed for room creation notification. Response Code: " + response.code());
//                    // Log error body for debugging failure reason
//                    try (ResponseBody errorBody = response.errorBody()) {
//                        String errBody = errorBody != null ? errorBody.string() : "N/A"; // Corrected
//                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read error response body (room create noti)", e);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.e(TAG, "OneSignal API call failed (network error) for room creation notification", t);
//            }
//        });
//        Log.d(TAG, "OneSignal API call enqueued for room creation notification.");
//    }
//    // *** END NEW HELPER METHOD ***
//
//
//    // *** NEW HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION for Room Join ***
//    private void sendRoomJoinNotification(String roomName, String roomId, String joiningUserId, String joiningUserName, List<String> existingMemberUidsBeforeJoin) {
//
//        // Check if API service is initialized and if there are recipients (existing members)
//        // We are notifying members who were already in the room, excluding the one who just joined.
//        if (oneSignalApiService == null || existingMemberUidsBeforeJoin == null || existingMemberUidsBeforeJoin.isEmpty()) {
//            Log.e(TAG, "sendRoomJoinNotification: API service is null or existing member list is empty.");
//            return; // Cannot send notification (no one was in the room except the joiner?)
//        }
//
//        // Filter out the joining user from the recipient list
//        List<String> recipientUids = new ArrayList<>();
//        if (!TextUtils.isEmpty(joiningUserId)) {
//            for (String memberUid : existingMemberUidsBeforeJoin) {
//                if (!TextUtils.isEmpty(memberUid) && !memberUid.equals(joiningUserId)) {
//                    recipientUids.add(memberUid);
//                }
//            }
//        } else {
//            // If joiningUserId is null, we cannot filter. Maybe send to all existing members?
//            // For safety, let's log a warning and skip if we can't identify the joiner to filter.
//            Log.w(TAG, "sendRoomJoinNotification: Joining User ID is null/empty, cannot filter recipients.");
//            return;
//        }
//
//
//        // If after filtering, there are no recipients (e.g., user joined an empty room they didn't create)
//        if (recipientUids.isEmpty()) {
//            Log.d(TAG, "sendRoomJoinNotification: No recipients after filtering joiner. Skipping notification.");
//            return;
//        }
//
//
//        Log.d(TAG, "Preparing OneSignal push notification for room join (ID: " + roomId + ") to " + recipientUids.size() + " members.");
//
//        // --- Build the JSON payload for OneSignal API ---
//        JsonObject notificationBody = new JsonObject();
//
//        // 1. Add App ID
//        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID); // Use your OneSignal App ID
//
//        // 2. Specify recipients using External User IDs (Firebase UIDs)
//        JsonArray externalUserIdsArray = new JsonArray();
//        for(String uid : recipientUids) {
//            externalUserIdsArray.add(uid); // Add each recipient's Firebase UID
//        }
//        notificationBody.add("include_external_user_ids", externalUserIdsArray); // Use include_external_user_ids
//
//
//        // 3. Add Notification Title and Content
//        String notificationTitle = "Member Joined Room: " + (roomName != null ? roomName : "Unnamed Room"); // Title
//        String notificationContent = (joiningUserName != null ? joiningUserName : "Someone") + " joined room '" + (roomName != null ? roomName : "Unnamed Room") + "'."; // Content
//
//        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationTitle))); // Title passed
//        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", notificationContent))); // Content passed
//
//        // Add custom data (important for handling notification click in the app)
//        // Include roomId and perhaps the type of event ("room_joined")
//        JsonObject data = new JsonObject();
//        data.addProperty("eventType", "room_joined"); // Custom key to identify notification type
//        data.addProperty("roomId", roomId); // Pass Room ID
//        data.addProperty("joiningUserId", joiningUserId); // Pass the UID of the user who joined
//        // You might want to add other relevant data
//        notificationBody.add("data", data);
//
//        // Optional: Set small icon (recommended)
//        notificationBody.addProperty("small_icon", "app_icon_circleup"); // <<< Replace with your icon's resource name (string)
//
//
//        // Optional: Customize notification appearance (sound, vibration, etc.)
//        // Check OneSignal API docs
//        // notificationBody.addProperty("sound", "default"); // Example sound
//
//
//        // --- Make the API call asynchronously ---
//        Log.d(TAG, "Making OneSignal API call for room join notification...");
//        oneSignalApiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "OneSignal API call successful for room join notification. Response Code: " + response.code());
//                    try (ResponseBody responseBody = response.body()) {
//                        String resBody = responseBody != null ? responseBody.string() : "N/A";
//                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read success response body (room join noti)", e);
//                    }
//                } else {
//                    Log.e(TAG, "OneSignal API call failed for room join notification. Response Code: " + response.code());
//                    try (ResponseBody errorBody = response.errorBody()) {
//                        String errBody = errorBody != null ? errorBody.string() : "N/A"; // Corrected
//                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read error response body (room join noti)", e);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.e(TAG, "OneSignal API call failed (network error) for room join notification", t);
//            }
//        });
//        Log.d(TAG, "OneSignal API call enqueued for room join notification.");
//    }
//    // *** END NEW HELPER METHOD ***
//
//    // ... rest of TemporaryChatRoomMain ...
//}





// *** Assuming InviteSelectContactAdapter is in the same package or correctly imported ***
// import com.sana.circleup.InviteSelectContactAdapter; // Example import


public class TemporaryChatRoomMain extends AppCompatActivity {

    private static final String TAG = "TempRoomMain"; // Define TAG for logging

    Button createRoomBtn, joinRoomBtn;
    // Button showRoom; // Button was commented out in XML, commenting out here too for now

    // To store the selected expiry date and time before creating the room
    private Calendar selectedExpiryCalendar;

    // Reference to the main Users node (needed for fetching invited user details and sender names)
    private DatabaseReference usersRef;
    private Toolbar toolbar;


    // --- List to hold selected contact UIDs for the temporary room ---
    private List<String> invitedMemberUids = new ArrayList<>(); // Declare this field

    // *** NEW MEMBER VARIABLE TO ADD ***
    private String currentUserName; // To store the name of the current logged-in user (the creator/joiner)
    private String currentUserId; // Store current user ID
    // *** END NEW MEMBER ***

    // *** ProgressDialog member variable ***
    private ProgressDialog progressDialog; // Declare ProgressDialog here
    // *** End ProgressDialog ***


    // *** NEW MEMBER: Retrofit Service for OneSignal API ***
    private OneSignalApiService oneSignalApiService;

    // Get this from your OneSignal Dashboard -> Settings -> Keys & IDs
    // ENSURE THIS MATCHES THE APP ID USED IN ChatPageActivity and GroupChatActivity
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR ACTUAL APP ID if different

    // *** END NEW MEMBER ***


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temporary_chat_room_main);

        Log.d(TAG, "🟢 TemporaryChatRoomMain launched");


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Temporary Room Chat"); // Optional: Set title programmatically if not set in XML
        }

        // Initialize Firebase Auth and get current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check if user is authenticated. Redirect to login if not.
        if (currentUser == null) {
            Log.w(TAG, "No current user authenticated. Redirecting to Login.");
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity(); // Navigate to login and finish
            return; // Stop further execution
        }
        currentUserId = currentUser.getUid(); // Get current user ID

        // Initialize Firebase Users ref (needed for fetching user details for invitation and sender names)
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Initialize ProgressDialog here in onCreate
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false); // User cannot cancel by tapping outside
        progressDialog.setMessage("Loading..."); // Default message

        // Fetch current user's username from Firebase (Needed for notification sender name)
        fetchCurrentUserName(); // Call the new helper method to fetch sender's name


        // Initialize buttons
        createRoomBtn = findViewById(R.id.createRoom);
        joinRoomBtn = findViewById(R.id.joinRoom);
        // showRoom = findViewById(R.id.showRoom); // Commented out as per XML


        // *** NEW: Initialize Retrofit Service for OneSignal API ***
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    // Base URL for OneSignal API (DO NOT CHANGE THIS)
                    .baseUrl("https://onesignal.com/")
                    // Add Gson converter factory
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            // Create an instance of your API service interface
            oneSignalApiService = retrofit.create(OneSignalApiService.class);
            Log.d(TAG, "OneSignalApiService initialized.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignalApiService", e);
            // Handle this error - perhaps disable buttons if notifications are critical?
            // Keep buttons enabled but log error, as notification sending is a secondary function.
            Log.e(TAG, "OneSignalApiService initialization failed. Notifications will not be sent.", e);
            // createRoomBtn.setEnabled(false); // Decide if crucial
            // joinRoomBtn.setEnabled(false); // Decide if crucial
            Toast.makeText(this, "Error initializing notification service. Some notification features may not work.", Toast.LENGTH_LONG).show();
        }
        // --- *** END NEW *** ---


        // --- Click Listeners ---

        createRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear previous invited UIDs when opening dialog for a new room creation attempt
                invitedMemberUids.clear(); // IMPORTANT: Start fresh for each room creation attempt
                showCreateRoomDialog(); // Call function to show the creation dialog
            }
        });

        // Click listener for Join Chat Rooms button
        joinRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJoinRoomDialog(); // Call the new method to show join dialog
            }
        });

        Log.d(TAG, "📲 onCreate finished in TemporaryChatRoomMain");
    }

    // Helper method to navigate to login activity
    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity.");
        Intent loginIntent = new Intent(TemporaryChatRoomMain.this, Login.class); // Replace Login with your actual Login Activity class
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(loginIntent);
        finish(); // Close this activity after redirecting
    }


    // Handle toolbar item clicks (specifically the back button)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle back button press
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    // *** HELPER METHOD TO ADD: to fetch current user's name ***
    private void fetchCurrentUserName() {
        if (TextUtils.isEmpty(currentUserId) || usersRef == null) {
            Log.w(TAG, "fetchCurrentUserName: currentUserId or usersRef is empty/null, cannot fetch name.");
            currentUserName = "A User"; // Default if ID is missing or ref is null
            return;
        }

        Log.d(TAG, "Fetching current user's name for UID: " + currentUserId);

        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    String name = snapshot.child("username").getValue(String.class);
                    if (!TextUtils.isEmpty(name)) {
                        currentUserName = name; // Store the fetched name
                        Log.d(TAG, "Fetched current user name: " + currentUserName);
                    } else {
                        currentUserName = "A User"; // Default if username field is empty
                        Log.w(TAG, "Current user's username field is empty. Using default.");
                    }
                } else {
                    currentUserName = "A User"; // Default if user data or username field is missing
                    Log.w(TAG, "Current user data or username field not found in DB. Using default.");
                }
                // The currentUserName is now available for use in notifications
                Log.d(TAG, "fetchCurrentUserName: Final currentUserName is: '" + currentUserName + "'");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch current user name from DB", error.toException());
                currentUserName = "A User"; // Default on error
                Log.d(TAG, "fetchCurrentUserName: Error fetching name. Defaulting to: '" + currentUserName + "'");
            }
        });
    }
    // *** END NEW HELPER METHOD TO ADD ***


    // Function to show the dialog for creating a temporary chat room
    private void showCreateRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate the custom layout for the dialog
        View view = getLayoutInflater().inflate(R.layout.dialog_create_temp_room, null); // Ensure this layout exists
        builder.setView(view);
        AlertDialog dialog = builder.create(); // Create the dialog

        // --- Initialize views from the dialog layout ---
        EditText editRoomName = view.findViewById(R.id.editRoomName); // Ensure this ID exists
        TextView textTimePicker = view.findViewById(R.id.textTimePicker); // Ensure this ID exists
        Button generateIdBtn = view.findViewById(R.id.generateid); // Ensure this ID exists
        Button btnCopyId = view.findViewById(R.id.btnCopyId); // Ensure this ID exists
        Button btnInvite = view.findViewById(R.id.btnInvite); // Ensure this ID exists
        Button btnCreate = view.findViewById(R.id.btnCreateRoom); // Ensure this ID exists
        TextView textRoomId = view.findViewById(R.id.textRoomId); // Ensure this ID exists
        LinearLayout idLayout = view.findViewById(R.id.generate_room_id_layout); // Ensure this ID exists

        // Variables to hold the generated room ID and selected expiry time (Calendar object)
        final String[] generatedRoomId = {null};
        selectedExpiryCalendar = null; // Initialize the Calendar object


        // --- Click Listeners for Dialog elements ---

        // Click listener for Generate Room ID button
        generateIdBtn.setOnClickListener(v -> {
            generatedRoomId[0] = generateRandomRoomId(8); // Generate an 8-character random ID
            textRoomId.setText("ID: " + generatedRoomId[0]); // Display the ID
            idLayout.setVisibility(View.VISIBLE); // Make the layout visible
            btnCopyId.setEnabled(true); // Enable copy button
        });

        // Initially disable copy button until ID is generated
        btnCopyId.setEnabled(false);

        // Click listener for Copy ID button
        btnCopyId.setOnClickListener(v -> {
            if (generatedRoomId[0] != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Room ID", generatedRoomId[0]);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Room ID copied!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Clipboard service not available.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Generate ID first!", Toast.LENGTH_SHORT).show();
            }
        });

        // Click listener for Select Expiry Time (Date and Time Picker)
        textTimePicker.setOnClickListener(v -> {
            // Get current date and time to initialize pickers
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Show Date Picker Dialog first
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        // Store the selected date
                        final Calendar dateSelected = Calendar.getInstance();
                        dateSelected.set(selectedYear, selectedMonth, selectedDay);

                        // Now show Time Picker Dialog
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                                (view2, hourOfDay, minuteOfHour) -> {
                                    // Store the selected time with the selected date
                                    dateSelected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    dateSelected.set(Calendar.MINUTE, minuteOfHour);
                                    dateSelected.set(Calendar.SECOND, 0); // Set seconds to 0 for consistency
                                    dateSelected.set(Calendar.MILLISECOND, 0); // Set milliseconds to 0

                                    selectedExpiryCalendar = dateSelected; // Store the full date and time

                                    // Format and display the selected date and time
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    String formattedDateTime = sdf.format(selectedExpiryCalendar.getTime());
                                    textTimePicker.setText("Expiry: " + formattedDateTime);

                                    // Basic validation: Check if selected time is in the past
                                    if (selectedExpiryCalendar.before(Calendar.getInstance())) {
                                        Toast.makeText(this, "Selected expiry time is in the past!", Toast.LENGTH_LONG).show();
                                        selectedExpiryCalendar = null; // Reset if in past
                                        textTimePicker.setText("Select Expiry Time"); // Reset text
                                    }

                                }, hour, minute, false); // 'false' for 12-hour format, 'true' for 24-hour format
                        timePickerDialog.show();
                    }, year, month, day);

            // Optional: Restrict past dates in the Date Picker (requires min date)
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Prevent selecting dates slightly in the past due to time drift

            datePickerDialog.show(); // Show the Date Picker
        });


        // Click listener for Invite Someone button
        btnInvite.setOnClickListener(v -> {
            // Fetch Current User's Contacts who are "friends"
            if (TextUtils.isEmpty(currentUserId)) {
                Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure contactsRef is initialized
            if (FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId) == null) {
                Toast.makeText(this, "Contacts reference not available.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Contacts reference is null for user " + currentUserId);
                return;
            }

            DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference()
                    .child("Contacts").child(currentUserId);

            // *** Removed progressDialog.show() here ***
            // Loading contacts is handled in loadContacts() on activity start.
            // We need to fetch the *list* of friends for the dialog here,
            // but the fetching of names/images for the adapter can happen inside the dialog's adapter.

            contactsRef.orderByChild("request_type").equalTo("accepted")
                    .addListenerForSingleValueEvent(new ValueEventListener() { // Use SingleValueEvent as contacts list isn't expected to change during dialog
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<UserModel> userFriends = new ArrayList<>(); // Use UserModel list to match adapter
                            List<String> friendUids = new ArrayList<>(); // List to hold just UIDs

                            for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                                String friendUid = contactSnapshot.getKey(); // The key is the friend's UID

                                if (friendUid != null && !friendUid.isEmpty() && !friendUid.equals(currentUserId)) {
                                    friendUids.add(friendUid); // Add UID to the list
                                }
                            }

                            // --- Show Contact Selection Dialog ---
                            if (friendUids.isEmpty()) {
                                Toast.makeText(TemporaryChatRoomMain.this, "You have no friends to invite.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // showContactSelectionDialog will now take the list of friend UIDs
                            showContactSelectionDialog(friendUids, invitedMemberUids); // Pass list of friend UIDs
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Failed to load contacts for invitation: " + error.getMessage());
                            Toast.makeText(TemporaryChatRoomMain.this, "Failed to load contacts for invitation.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        // Click listener for Create Room button
        btnCreate.setOnClickListener(v -> {
            String roomName = editRoomName.getText().toString().trim(); // Get room name
            String roomId = generatedRoomId[0]; // Get generated ID
            // currentUserId is already a member variable
            // currentUserName is already a member variable (fetched asynchronously)

            // --- Basic Validation ---
            if (TextUtils.isEmpty(roomName)) {
                editRoomName.setError("Room name is required");
                editRoomName.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(roomId)) {
                Toast.makeText(this, "Please generate Room ID first!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedExpiryCalendar == null) {
                Toast.makeText(this, "Please select expiry time!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedExpiryCalendar.before(Calendar.getInstance())) {
                Toast.makeText(this, "Expiry time cannot be in the past!", Toast.LENGTH_SHORT).show();
                selectedExpiryCalendar = null; // Reset if in past
                textTimePicker.setText("Select Expiry Time"); // Reset text
                return; // Exit if expiry is in past
            }
            if (TextUtils.isEmpty(currentUserId)) {
                Toast.makeText(this, "User not authenticated. Cannot create room.", Toast.LENGTH_SHORT).show();
                // Maybe redirect to login here?
                // sendUserToLoginActivity();
                return;
            }
            // Ensure currentUserName is fetched (or defaulted) for notification
            if (TextUtils.isEmpty(currentUserName)) {
                Log.w(TAG, "currentUserName is empty during room creation. Using default for notification.");
                currentUserName = "A User"; // Ensure it has a value for notification payload
            }
            // Ensure OneSignal API service is available if notifications are critical.
            if (oneSignalApiService == null) {
                Log.e(TAG, "OneSignalApiService is null. Room creation notifications will fail.");
                // Decide if you want to stop creation or proceed without notifications.
                // Proceeding is usually okay.
            }


            // Get Firebase database reference for the temporary room
            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("temporaryChatRooms").child(roomId);

            // Create a map to store room data
            Map<String, Object> roomData = new HashMap<>();
            roomData.put("roomName", roomName);
            roomData.put("roomId", roomId);
            // Store expiry time as a long (timestamp in milliseconds)
            roomData.put("expiryTime", selectedExpiryCalendar.getTimeInMillis());
            roomData.put("createdBy", currentUserId); // Use member variable
            roomData.put("timestamp", ServerValue.TIMESTAMP); // Timestamp for creation time

            // Add members node with the creator as the initial member
            Map<String, Object> members = new HashMap<>();
            members.put(currentUserId, true); // Store creator's UID with a boolean flag (common practice)

            // Add invited members from the list
            for (String uid : invitedMemberUids) {
                if (uid != null && !uid.isEmpty()) { // Basic check
                    members.put(uid, true);
                }
            }
            roomData.put("members", members); // Add members map to room data


            // Write room data to Firebase
            btnCreate.setEnabled(false); // Disable button to prevent double click
            dialog.setCancelable(false); // Make dialog not cancelable while creating
            if (progressDialog != null) {
                progressDialog.setMessage("Creating room...");
                progressDialog.show();
            }

            roomRef.setValue(roomData).addOnCompleteListener(task -> {
                btnCreate.setEnabled(true); // Re-enable button
                dialog.setCancelable(true); // Make dialog cancelable again
                if (progressDialog != null) progressDialog.dismiss();


                if (task.isSuccessful()) {
                    Log.d(TAG, "Temporary Room '" + roomName + "' created successfully with ID: " + roomId);
                    Toast.makeText(this, "Room '" + roomName + "' created!", Toast.LENGTH_LONG).show();
                    dialog.dismiss(); // Close the dialog on success


                    // *** NEW: Send Push Notification for Room Creation ***
                    // Ensure oneSignalApiService and currentUserName are available
                    if (oneSignalApiService != null && !TextUtils.isEmpty(currentUserName) && !TextUtils.isEmpty(currentUserId)) {
                        // Combine creator and invited members for notification recipients list used by helper
                        List<String> allMemberUids = new ArrayList<>(invitedMemberUids);
                        allMemberUids.add(0, currentUserId); // Add creator at the beginning

                        Log.d(TAG, "Room created. Calling sendOneSignalNotificationToUsers for creation.");

                        // 1. Send notification to the creator
                        List<String> creatorRecipientList = Collections.singletonList(currentUserId);
                        String creatorTitle = "Temporary Room Created!";
                        // Format expiry time for notification content
                        String formattedExpiry = formatExpiryTime(selectedExpiryCalendar.getTimeInMillis());
                        String creatorContent = "You created room '" + roomName + "'. Expires " + formattedExpiry + ".";

                        sendOneSignalNotificationToUsers(
                                oneSignalApiService,
                                creatorRecipientList,
                                creatorTitle,
                                creatorContent,
                                roomId, // Pass roomId in data
                                currentUserId, // createdBy in data
                                currentUserName, // Creator's name in data
                                "room_created_creator" // Event type in data
                        );

                        // 2. Send notification to invited members
                        // Ensure there are actual invited members before sending
                        if (!invitedMemberUids.isEmpty()) {
                            List<String> invitedRecipientList = new ArrayList<>(invitedMemberUids); // Use the invited members list
                            String invitedTitle = "New Temporary Room: " + roomName;
                            String invitedContent = (currentUserName != null ? currentUserName : "Someone") + " added you to room '" + roomName + "'. Expires " + formattedExpiry + ".";

                            sendOneSignalNotificationToUsers(
                                    oneSignalApiService,
                                    invitedRecipientList,
                                    invitedTitle,
                                    invitedContent,
                                    roomId, // Pass roomId in data
                                    currentUserId, // createdBy in data
                                    currentUserName, // Creator's name in data
                                    "room_created_invited" // Event type in data
                            );
                        } else {
                            Log.d(TAG, "No members invited to the temporary room. Skipping notification to invited members.");
                        }

                    } else {
                        Log.e(TAG, "Cannot send room creation notifications: API service, currentUserName, or currentUserId is null/empty.");
                    }
                    // *** END NEW ***


                    // TODO: Maybe navigate to the newly created chat room or show it in a list
                    // Start the TemporaryRoomChatActivity
                    Intent chatIntent = new Intent(TemporaryChatRoomMain.this, TemporaryRoomChatActivity.class); // Replace with your actual Chat Activity for Temp Rooms
                    chatIntent.putExtra("roomId", roomId); // Pass the created room ID
                    startActivity(chatIntent);


                } else {
                    // Handle errors during creation
                    Log.e(TAG, "Error creating temporary room: " + task.getException().getMessage(), task.getException());
                    Toast.makeText(this, "Error creating room: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Show the AlertDialog
        dialog.show();
    }


    // --- NEW: Function to show contact selection dialog ---
    // This method now accepts List<String> of friend UIDs and fetches UserModel details in the adapter.


    // Function to generate a random alphanumeric string for Room ID
    private String generateRandomRoomId(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder id = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            id.append(chars.charAt(random.nextInt(chars.length())));
        }
        return id.toString();
    }


    // Function to show the dialog for joining a temporary chat room
    private void showJoinRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_join_temp_room, null); // Ensure this layout exists and matches
        builder.setView(dialogView);

        EditText editRoomIdToJoin = dialogView.findViewById(R.id.editRoomIdToJoin); // Ensure this ID exists
        Button btnJoin = dialogView.findViewById(R.id.btnJoinRoomInDialog); // Ensure this ID exists
        // Optional: Get status TextView or ProgressBar

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set Click Listener for the custom Join button AFTER showing the dialog
        if (btnJoin != null) {
            btnJoin.setOnClickListener(v -> {
                String enteredRoomId = editRoomIdToJoin.getText().toString().trim().toUpperCase();

                if (TextUtils.isEmpty(enteredRoomId)) {
                    Toast.makeText(this, "Please enter a Room ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                // currentUserId and currentUserName are already member variables
                if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(currentUserName) || usersRef == null) {
                    Toast.makeText(this, "User information missing. Cannot join.", Toast.LENGTH_SHORT).show();
                    // Consider fetching user info here if it wasn't available earlier
                    // fetchCurrentUserName(); // Could call again, but it's async. Safer to ensure it's fetched on Create.
                    return;
                }

                // Ensure OneSignal API service is available for notification
                if (oneSignalApiService == null) {
                    Log.e(TAG, "OneSignalApiService is null. Room join notifications will fail.");
                    // Decide if you want to stop joining or proceed without notifications.
                    // Proceeding is usually okay.
                }


                btnJoin.setEnabled(false); // Optional: Disable button during check
                // statusTextView.setText("Checking room..."); // Optional status update


                DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("temporaryChatRooms").child(enteredRoomId);
                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // This runs on the main thread
                        btnJoin.setEnabled(true); // Optional: Re-enable button

                        if (!snapshot.exists()) {
                            Toast.makeText(TemporaryChatRoomMain.this, "Room not found with this ID.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Room exists, check expiry and membership
                            Long expiryTime = snapshot.child("expiryTime").getValue(Long.class);
                            DataSnapshot membersSnapshot = snapshot.child("members");
                            String roomName = snapshot.child("roomName").getValue(String.class); // Get room name for notification

                            // Get existing members list BEFORE adding the new member (for notification recipients)
                            List<String> existingMemberUidsBeforeJoin = new ArrayList<>();
                            if (membersSnapshot.exists()) {
                                for (DataSnapshot memberSnap : membersSnapshot.getChildren()) {
                                    String memberUid = memberSnap.getKey();
                                    if (!TextUtils.isEmpty(memberUid)) {
                                        existingMemberUidsBeforeJoin.add(memberUid);
                                    }
                                }
                            }


                            if (expiryTime != null && System.currentTimeMillis() > expiryTime) {
                                Toast.makeText(TemporaryChatRoomMain.this, "This room has expired.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss(); // Close expired room dialog

                                // Optional: Delete expired room from Firebase (implement this carefully)
                                // This would require a separate process or user action to trigger cleanup.
                                // Deleting immediately here is generally not recommended unless you have a very specific reason.
                                // roomRef.removeValue(); // Dangerous, only do if sure
                                // Log.w(TAG, "User attempted to join expired room. Consider implementing background cleanup.");


                            } else if (membersSnapshot.hasChild(currentUserId)) {
                                // User is already a member
                                Toast.makeText(TemporaryChatRoomMain.this, "You are already a member of this room.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss(); // Close dialog
                                // Navigate to the chat activity
                                Intent chatIntent = new Intent(TemporaryChatRoomMain.this, TemporaryRoomChatActivity.class); // Replace with your actual Chat Activity for Temp Rooms
                                chatIntent.putExtra("roomId", enteredRoomId);
                                startActivity(chatIntent);

                            } else {
                                // User is not a member and room is not expired. Add user to members!
                                Log.d(TAG, "User " + currentUserId + " is joining room: " + enteredRoomId);
                                Map<String, Object> memberUpdate = new HashMap<>();
                                memberUpdate.put(currentUserId, true); // Add current user UID

                                roomRef.child("members").updateChildren(memberUpdate)
                                        .addOnCompleteListener(task -> {
                                            // This runs on the main thread
                                            btnJoin.setEnabled(true); // Re-enable button

                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User " + currentUserId + " successfully joined room: " + enteredRoomId);
                                                Toast.makeText(TemporaryChatRoomMain.this, "Joined room successfully!", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss(); // Close dialog

                                                // *** NEW: Send Push Notification for Room Join ***
                                                // Notify existing members (the ones in existingMemberUidsBeforeJoin list)
                                                // Ensure oneSignalApiService and currentUserName are available
                                                if (oneSignalApiService != null && !TextUtils.isEmpty(currentUserName) && !TextUtils.isEmpty(currentUserId)) {
                                                    Log.d(TAG, "User joined. Calling sendOneSignalNotificationToUsers for room join.");
                                                    sendOneSignalNotificationToUsers(
                                                            oneSignalApiService,
                                                            existingMemberUidsBeforeJoin, // List of members BEFORE this user joined (recipients)
                                                            "Member Joined Room: " + (roomName != null ? roomName : "Unnamed Room"), // Title
                                                            (currentUserName != null ? currentUserName : "Someone") + " joined room '" + (roomName != null ? roomName : "Unnamed Room") + "'.", // Content
                                                            enteredRoomId, // Pass roomId in data
                                                            currentUserId, // Pass joiningUserId in data
                                                            currentUserName, // Pass joiningUserName in data
                                                            "room_joined" // Event type in data
                                                    );
                                                } else {
                                                    Log.e(TAG, "Cannot send room join notification: API service, currentUserName, or currentUserId is null/empty.");
                                                }
                                                // *** END NEW ***


                                                // Navigate to the chat activity
                                                Intent chatIntent = new Intent(TemporaryChatRoomMain.this, TemporaryRoomChatActivity.class); // Replace with your actual Chat Activity for Temp Rooms
                                                chatIntent.putExtra("roomId", enteredRoomId);
                                                startActivity(chatIntent);


                                            } else {
                                                Log.e(TAG, "Failed to update members list for join: " + task.getException().getMessage(), task.getException());
                                                Toast.makeText(TemporaryChatRoomMain.this, "Failed to join room: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                // statusTextView.setText("Join Failed"); // Optional status update
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase query cancelled during join check: " + error.getMessage(), error.toException());
                        Toast.makeText(TemporaryChatRoomMain.this, "Error checking room.", Toast.LENGTH_SHORT).show();
                        btnJoin.setEnabled(true); // Re-enable button
                        dialog.dismiss();
                        // statusTextView.setText("Error"); // Optional status update
                    }
                });
            });
        } else {
            Log.e(TAG, "Join button (btnJoinRoomInDialog) not found in dialog layout!");
            Toast.makeText(this, "Error: Join button not found.", Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // Dismiss if button not found
        }
        // Cancel button is added by the builder automatically
    }

    // *** NEW HELPER METHOD: Format expiry time for display ***
    private String formatExpiryTime(long expiryTimestampMillis) {
        // Use SimpleDateFormat to format the timestamp into a readable string
        // You can adjust the format string ("yyyy-MM-dd HH:mm" etc.) as needed
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        return sdf.format(expiryTimestampMillis);
    }
    // *** END NEW HELPER METHOD ***


    // *** NEW GENERIC HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION TO A LIST OF USERS ***
    // This method is flexible enough to send different notifications (room create, room join, etc.)
    /**
     * Sends a OneSignal push notification to a specified list of users.
     *
     * @param apiService       The Retrofit OneSignalApiService instance.
     * @param recipientFirebaseUIDs A List of Firebase UIDs of the users who should RECEIVE the notification.
     * @param title            The title of the notification.
     * @param messageContent   The main content/body of the notification.
     * @param roomId           (Optional) The Firebase ID of the temporary room, if applicable.
     * @param actionUserId     (Optional) The Firebase UID of the user who initiated the action (e.g., creator, joiner).
     * @param actionUserName   (Optional) The display name of the user who initiated the action.
     * @param eventType        A string identifier for the type of notification (e.g., "room_created_creator", "room_created_invited", "room_joined").
     */
    private void sendOneSignalNotificationToUsers(OneSignalApiService apiService,
                                                  List<String> recipientFirebaseUIDs,
                                                  String title,
                                                  String messageContent, // <-- The content string constructed in the caller
                                                  String roomId, // Added for custom data
                                                  String actionUserId, // Added for custom data
                                                  String actionUserName, // Added for custom data
                                                  String eventType) { // Added for custom data

        // --- Input Validation ---
        if (apiService == null) {
            Log.e(TAG, "sendOneSignalNotificationToUsers: API service is null. Cannot send notification.");
            return;
        }
        if (recipientFirebaseUIDs == null || recipientFirebaseUIDs.isEmpty()) {
            Log.w(TAG, "sendOneSignalNotificationToUsers: Recipient list is null or empty. Cannot send notification.");
            return;
        }
        if (TextUtils.isEmpty(eventType)) {
            Log.w(TAG, "sendOneSignalNotificationToUsers: Event type is null or empty. Cannot send notification.");
            return; // eventType is crucial for handling on the recipient side
        }


        Log.d(TAG, "Preparing OneSignal push notification type '" + eventType + "' to " + recipientFirebaseUIDs.size() + " recipients.");

        // --- Build the JSON payload for OneSignal API ---
        JsonObject notificationBody = new JsonObject();

        // 1. Add App ID
        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID); // Use your OneSignal App ID (class member)

        // 2. Specify recipients using External User IDs (Firebase UIDs)
        JsonArray externalUserIdsArray = new JsonArray();
        for(String uid : recipientFirebaseUIDs) {
            if (!TextUtils.isEmpty(uid)) {
                externalUserIdsArray.add(uid); // Add each recipient's Firebase UID
            }
        }
        // If no valid UIDs remain after the loop (shouldn't happen if recipientFirebaseUIDs wasn't empty initially)
        if (externalUserIdsArray.size() == 0) {
            Log.w(TAG, "No valid recipient UIDs after filtering list. Skipping notification API call.");
            return;
        }
        notificationBody.add("include_external_user_ids", externalUserIdsArray); // Use include_external_user_ids


        // 3. Add Notification Title and Content
        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", title))); // Title passed
        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", messageContent))); // <--- The content string is put here


        // 4. Add custom data (important for handling notification click in the app)
        JsonObject data = new JsonObject();
        data.addProperty("eventType", eventType); // Add the event type

        // Add optional data if available
        if (!TextUtils.isEmpty(roomId)) {
            data.addProperty("roomId", roomId);
        }
        if (!TextUtils.isEmpty(actionUserId)) {
            data.addProperty("actionUserId", actionUserId); // User who performed the action (creator or joiner)
        }
        if (!TextUtils.isEmpty(actionUserName)) {
            data.addProperty("actionUserName", actionUserName); // Name of user who performed the action
        }

        // Optional: Add other relevant data needed for click handling/display
        // For room creation/join, perhaps the target screen is the temporary room chat?
        // data.addProperty("targetScreen", "TemporaryRoomChatActivity"); // Example target screen for click handler
        // data.addProperty("targetRoomId", roomId); // Pass room ID for navigation


        notificationBody.add("data", data);

        // 5. Optional: Set small icon (recommended)
        notificationBody.addProperty("small_icon", "app_icon_circleup"); // <<< REPLACE with your actual small icon resource name (string)


        // 6. Optional: Customize notification appearance (sound, vibration, etc.)
        // Check OneSignal API documentation for available options:
        // https://documentation.onesignal.com/reference/create-notification
        // notificationBody.addProperty("sound", "default"); // Example sound


        // --- Make the API call asynchronously using Retrofit ---
        Log.d(TAG, "Making OneSignal API call for type '" + eventType + "' to " + recipientFirebaseUIDs.size() + " users...");
        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                // This callback runs on the main thread
                if (response.isSuccessful()) {
                    Log.d(TAG, "OneSignal API call successful for type '" + eventType + "'. Response Code: " + response.code());
                    try (ResponseBody responseBody = response.body()) {
                        String resBody = responseBody != null ? responseBody.string() : "N/A";
                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
                        // Example success body: {"id": "a62fddc6-5c02-4020-a7aa-2d022951bcf1", "recipients": 3}
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read success response body ('" + eventType + "')", e);
                    }
                } else {
                    Log.e(TAG, "OneSignal API call failed for type '" + eventType + "'. Response Code: " + response.code());
                    try (ResponseBody errorBody = response.errorBody()) {
                        String errBody = errorBody != null ? errorBody.string() : "N/A";
                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read error response body ('" + eventType + "')", e);
                    }
                    Log.w(TAG, "Push notification failed via OneSignal API for type: " + eventType);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                // Network error or request couldn't be sent at all
                Log.e(TAG, "OneSignal API call failed (network error) for type '" + eventType + "'", t);
                Log.w(TAG, "Push notification failed due to network error for type: " + eventType);
            }
        });
        Log.d(TAG, "OneSignal API call enqueued for type '" + eventType + "'.");
    }
    // *** END NEW GENERIC HELPER METHOD ***

    // --- MODIFIED InviteSelectContactAdapter class ---
    // This adapter now works with List<String> of UIDs and fetches UserModel details for display.
    // It manages a List<String> of selected UIDs.
    // Inside TemporaryChatRoomMain.java

    // --- MODIFIED InviteSelectContactAdapter class ---
// This adapter now takes List<String> of contact UIDs. It fetches UserModel data
// for display and manages a list of selected UIDs.

    // --- Assuming your Contacts model class exists and has getRequest_type() ---
    // Used for filtering friends from the /Contacts node.
    // It might *not* need getUsername/getStatus/getProfileImage if loadContacts fetches UserModel instead.
    /*
    public static class Contacts {
        private String request_type; // e.g., "accepted"
        // ... maybe other fields like timestamp of becoming friends ...

        public String getRequest_type() { return request_type; }
        public void setRequest_type(String request_type) { this.request_type = request_type; }

        // Add no-argument constructor for Firebase
        public Contacts() {}
    }
    */

    // --- Assuming your UserModel class exists ---
    // This model should map to the data under /Users/{uid}
    public static class UserModel {
        private String userId; // Should be populated manually from snapshot key
        private String username;
        private String status;
        private String profileImage; // Assuming this is the Base64 string or URL
        // *** Added a transient field for UI selection state ***
        // Marked transient so Firebase doesn't try to save it
        private transient boolean isSelected = false; // Default to not selected


        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

        // *** Getters and setters for the transient selection state ***
        public boolean isSelected() { return isSelected; }
        public void setSelected(boolean selected) { isSelected = selected; }


        // Add no-argument constructor for Firebase
        public UserModel() {}
        // Constructor with UID for minimal models (used in loadContacts)
        public UserModel(String userId) {
            this.userId = userId;
        }
        // Note: If your /Users data includes more fields directly mapped, you'll need a constructor
        // that matches those fields if you want Firebase to map them directly.
        // The empty constructor is sufficient if using snapshot.getValue(UserModel.class).
    }

    // --- Function to show contact selection dialog ---
    // This method now accepts List<String> of friend UIDs and fetches UserModel details in the adapter.
    private void showContactSelectionDialog(List<String> friendUids, List<String> invitedUidsListToUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invite Contacts");

        // Use a custom layout for the dialog containing a RecyclerView
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.temporaryroom_dialog_select_contacts, null); // Ensure this layout exists and matches
        RecyclerView contactsRecyclerView = dialogView.findViewById(R.id.recycler_view_contacts); // Match ID from dialog_select_contacts.xml

        // Initialize RecyclerView
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create and set the adapter for selecting contacts
        // Pass the list of friend UIDs, the list of already invited UIDs (for pre-selection), and usersRef
        // Assuming InviteSelectContactAdapter constructor takes List<String> friendUids, List<String> selectedUids, DatabaseReference usersRef
        InviteSelectContactAdapter adapter = new InviteSelectContactAdapter(friendUids, new ArrayList<>(invitedUidsListToUpdate), usersRef); // Pass list of friend UIDs and usersRef
        contactsRecyclerView.setAdapter(adapter);

        builder.setView(dialogView); // Set the custom layout

        // Add "Done" button
        builder.setPositiveButton("Done", (dialog, which) -> {
            // Get selected UIDs from the adapter
            List<String> selectedUids = adapter.getSelectedUids();
            // Clear previous selections and add new ones to the list that will be used for room creation
            invitedUidsListToUpdate.clear(); // Clear the class-level list
            invitedUidsListToUpdate.addAll(selectedUids); // Add newly selected UIDs

            // Optional: Show a toast with selected names (fetched by adapter)
            List<String> selectedNames = adapter.getSelectedNames(); // Adapter should provide selected names
            if (!selectedNames.isEmpty()) {
                Toast.makeText(this, "Selected: " + TextUtils.join(", ", selectedNames), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No contacts selected.", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss(); // Close dialog
        });

        // Add "Cancel" button
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Just close the dialog. The invitedMemberUids list will retain its state
            // from before the dialog was opened (unless changes were already added to it).
            // Since we clear invitedMemberUids at the start of showCreateRoomDialog,
            // the state is correctly managed for each creation attempt.
            dialog.cancel();
        });

        builder.create().show(); // Create and show the dialog
    }

}