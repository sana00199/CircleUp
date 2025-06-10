


package com.sana.circleup;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sana.circleup.one_signal_notification.OneSignalApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;




//
//public class CreateGroupActivity extends AppCompatActivity {
//
//    private static final String TAG = "CreateGroupActivity"; // Define TAG for logging
//
//    private EditText groupNameInput;
//    private Button createGroupButton;
//    private RecyclerView contactsRecyclerView;
//    private SelectContactAdapter contactAdapter; // Assuming this is your adapter class
//    private List<UserModel> contactList = new ArrayList<>(); // List to hold UserModel objects
//    private List<String> selectedUserIds = new ArrayList<>(); // List to hold UIDs of selected members (excluding creator)
//    private DatabaseReference usersRef, groupsRef, contactsRef;
//    private FirebaseAuth auth;
//    private String currentUserId;
//    private ProgressDialog progressDialog;
//
//    // *** MEMBER VARIABLES ***
//    private String currentUserName; // To store the name of the current logged-in user (the creator)
//    // *** END MEMBER VARIABLES ***
//
//    // *** Retrofit Service for OneSignal API ***
//    private OneSignalApiService oneSignalApiService;
//    // Get this from your OneSignal Dashboard -> Settings -> Keys & IDs
//    // ENSURE THIS MATCHES THE APP ID USED IN OTHER ACTIVITIES
//    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR ACTUAL APP ID
//    // *** END NEW MEMBER ***
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_create_group); // Ensure this layout exists
//
//        Log.d(TAG, "🟢 CreateGroupActivity launched");
//
//        auth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = auth.getCurrentUser(); // Get current user
//
//        if (currentUser != null) {
//            currentUserId = currentUser.getUid();
//            // currentUserName is fetched asynchronously later.
//        } else {
//            Log.e(TAG, "Error: currentUserId is NULL. User not authenticated.");
//            Toast.makeText(this, "Authentication error! Please log in again.", Toast.LENGTH_SHORT).show();
//            // Assuming you have a Login activity
//            Intent loginIntent = new Intent(CreateGroupActivity.this, Login.class);
//            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
//            startActivity(loginIntent);
//            finish(); // Exit activity
//            return;
//        }
//
//        // Initialize Firebase References
//        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
//        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
//
//        if (currentUserId != null) {
//            // Reference to the current user's contacts
//            contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
//        } else {
//            // This else block should ideally not be reached if the auth check above passes.
//            // If somehow reached, log and finish as contactsRef is critical.
//            Log.e(TAG, "contactsRef not initialized because currentUserId is null after auth check.");
//            Toast.makeText(this, "Initialization error. Cannot load contacts.", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//
//        // Initialize UI Elements
//        groupNameInput = findViewById(R.id.group_name_input); // Ensure this ID exists
//        createGroupButton = findViewById(R.id.create_group_button); // Ensure this ID exists
//        contactsRecyclerView = findViewById(R.id.contacts_recycler_view); // Ensure this ID exists
//
//        // Ensure UI elements are not null before proceeding
//        if (groupNameInput == null || createGroupButton == null || contactsRecyclerView == null) {
//            Log.e(TAG, "CRITICAL ERROR: One or more UI elements not found in layout!");
//            Toast.makeText(this, "Layout error.", Toast.LENGTH_SHORT).show();
//            finish();
//            return; // Stop if UI is not complete
//        }
//
//        // *** NEW: Initially disable the Create Group button ***
//        createGroupButton.setEnabled(false);
//        Log.d(TAG, "Create Group button initially disabled.");
//        // *** END NEW ***
//
//
//        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        // Initialize the adapter. It needs the context, the list of contacts, the list of selected user IDs, and a listener for item clicks (this activity implements the listener).
//        // Assuming SelectContactAdapter expects Context, List<UserModel>, List<String> selectedIds, SelectContactAdapter.OnItemClickListener
//        // You need to ensure your SelectContactAdapter has an interface or callback for item clicks.
//        // For this example, let's assume it implements View.OnClickListener on the ViewHolder items,
//        // and you handle the selection logic inside the adapter's onBindViewHolder, updating the selectedUserIds list passed here by reference.
//        // If your adapter needs a listener callback in the Activity, you'll need to implement that interface and pass 'this'.
//        contactAdapter = new SelectContactAdapter(this, contactList, selectedUserIds); // Assuming adapter constructor only needs list and selectedIds for simplicity. You might need to pass 'this' if it has an interface.
//        contactsRecyclerView.setAdapter(contactAdapter);
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("Loading contacts...");
//
//
//        // *** Initialize Retrofit Service for OneSignal API ***
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
//            // Handle this error - notifications for group creation might not work
//            // createGroupButton.setEnabled(false); // Button is already disabled, keep it disabled if API fails
//            Toast.makeText(this, "Error initializing notification service. Group notifications may not work.", Toast.LENGTH_LONG).show();
//        }
//        // *** END NEW ***
//
//        // *** NEW: Fetch current user's name AFTER UI is ready and before contacts load ***
//        // This fetch will enable the createGroupButton once it completes.
//        fetchCurrentUserName(); // <-- This is async, will enable button later
//        // *** END NEW ***
//
//
//        // Load contacts after UI and auth are initialized
//        loadContacts(); // This runs independently but populates the contact list for selection.
//
//        // Set click listener for create group button
//        // This listener will only be active *after* the button is enabled in fetchCurrentUserName callback.
//        createGroupButton.setOnClickListener(v -> {
//            Log.d(TAG, "Create Group button clicked.");
//            Log.d(TAG, "Selected User IDs: " + selectedUserIds); // Log selected IDs
//            createGroup(); // Call the create group process
//        });
//
//        Log.d(TAG, "📲 onCreate finished in CreateGroupActivity");
//    }
//
//    // *** MODIFIED HELPER METHOD TO FETCH CURRENT USER'S NAME (CREATOR) ***
//    // This method now enables the createGroupButton after fetching the name.
//    private void fetchCurrentUserName() {
//        if (TextUtils.isEmpty(currentUserId) || usersRef == null) {
//            Log.w(TAG, "fetchCurrentUserName: currentUserId or usersRef is empty/null, cannot fetch name.");
//            currentUserName = "A User"; // Default creator name
//            // *** NEW: Enable button even if name fetch critical error happens ***
//            if (createGroupButton != null) {
//                createGroupButton.setEnabled(true);
//                Log.d(TAG, "Create Group button enabled after critical name fetch error.");
//            }
//            // *** End NEW ***
//            return;
//        }
//        Log.d(TAG, "Fetching current user's (creator) name for UID: " + currentUserId);
//        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists() && snapshot.hasChild("username")) {
//                    String name = snapshot.child("username").getValue(String.class);
//                    if (!TextUtils.isEmpty(name)) {
//                        currentUserName = name; // Store the creator's name
//                    } else {
//                        currentUserName = "A User"; // Default if username field is empty in DB
//                        Log.w(TAG, "Current user's username field is empty. Using default creator name.");
//                    }
//                } else {
//                    currentUserName = "A User"; // Default if user data or username field is missing in DB
//                    Log.w(TAG, "Current user data or username field not found for creator UID. Using default creator name.");
//                }
//                // currentUserName is now populated.
//                // *** NEW: Enable the Create Group button after name is fetched or defaulted ***
//                if (createGroupButton != null) {
//                    createGroupButton.setEnabled(true);
//                    Log.d(TAG, "Create Group button enabled after name fetch complete. currentUserName is: '" + currentUserName + "'");
//                }
//                // *** End NEW ***
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to fetch current user (creator) name from DB", error.toException());
//                currentUserName = "A User"; // Default on error fetching
//                // *** NEW: Enable the Create Group button even if name fetch fails ***
//                if (createGroupButton != null) {
//                    createGroupButton.setEnabled(true);
//                    Log.d(TAG, "Create Group button enabled after name fetch cancelled. Defaulting to: '" + currentUserName + "'");
//                }
//                // *** End NEW ***
//            }
//        });
//    }
//    // *** END MODIFIED HELPER METHOD ***
//
//
//    private void loadContacts() {
//        Log.d(TAG, "Loading contacts...");
//        if (progressDialog != null) progressDialog.show(); // Show dialog
//
//        // Ensure contactsRef is initialized
//        if (contactsRef == null) {
//            Log.e(TAG, "loadContacts: contactsRef is null. Cannot load contacts.");
//            if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//            Toast.makeText(this, "Error loading contacts.", Toast.LENGTH_SHORT).show();
//            // Disable button if contacts cannot be loaded? Handled by fetchCurrentUserName for now.
//            return;
//        }
//
//        // Listen for accepted contacts only
//        // Filter by request_type = "accepted"
//        contactsRef.orderByChild("request_type").equalTo("accepted")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        // This runs on the main thread
//                        contactList.clear(); // Clear previous list
//
//                        if (!snapshot.exists()) {
//                            Log.d(TAG, "No accepted contacts found for current user.");
//                            if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//                            Toast.makeText(CreateGroupActivity.this, "No friends found to add to a group.", Toast.LENGTH_SHORT).show();
//                            if (contactAdapter != null) contactAdapter.notifyDataSetChanged(); // Update adapter to show empty list
//                            return;
//                        }
//
//                        List<String> friendIds = new ArrayList<>();
//                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                            String friendId = dataSnapshot.getKey(); // The key is the friend's UID
//                            if (friendId != null && !TextUtils.isEmpty(friendId)) {
//                                friendIds.add(friendId);
//                            }
//                        }
//                        Log.d(TAG, "Found " + friendIds.size() + " accepted contact friend UIDs.");
//
//
//                        if (friendIds.isEmpty()) {
//                            Log.d(TAG, "Friend IDs list is empty after filtering. No contacts to display.");
//                            if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//                            Toast.makeText(CreateGroupActivity.this, "No friends found to add to a group.", Toast.LENGTH_SHORT).show();
//                            if (contactAdapter != null) contactAdapter.notifyDataSetChanged(); // Update adapter
//                            return;
//                        }
//
//
//                        // Fetch details for all friends in one query from Users node
//                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @SuppressLint("NotifyDataSetChanged")
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
//                                // This runs on the main thread after fetching user details
//                                Log.d(TAG, "Fetching details for " + friendIds.size() + " friends from /Users.");
//                                for (String friendId : friendIds) {
//                                    if (userSnapshot.hasChild(friendId)) {
//                                        // Get UserModel from the userSnapshot
//                                        UserModel user = userSnapshot.child(friendId).getValue(UserModel.class);
//                                        if (user != null) {
//                                            // Set the userId from the snapshot key (which is the UID)
//                                            user.setUserId(friendId);
//                                            contactList.add(user); // Add the UserModel to the contact list
//                                        } else {
//                                            Log.w(TAG, "UserModel is null for friendId: " + friendId);
//                                        }
//                                    } else {
//                                        Log.w(TAG, "User data not found in /Users for friendId: " + friendId);
//                                    }
//                                }
//                                Log.d(TAG, "Loaded " + contactList.size() + " user models for contacts.");
//                                if (contactAdapter != null) contactAdapter.notifyDataSetChanged(); // Notify adapter the list has changed
//                                if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//                                // This runs on the main thread
//                                Log.e(TAG, "Firebase Error fetching users for contacts: " + error.getMessage(), error.toException());
//                                if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//                                Toast.makeText(CreateGroupActivity.this, "Error fetching contacts.", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        // This runs on the main thread
//                        Log.e(TAG, "Firebase Database Error loading accepted contacts: " + error.getMessage(), error.toException());
//                        if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//                        Toast.makeText(CreateGroupActivity.this, "Error loading contacts.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//
//    private void createGroup() {
//        String groupName = groupNameInput.getText().toString().trim();
//
//        // --- Basic Validation ---
//        if (TextUtils.isEmpty(groupName)) {
//            Toast.makeText(this, "Enter group name", Toast.LENGTH_SHORT).show();
//            // Keep button disabled as process hasn't finished
//            return;
//        }
//        // selectedUserIds list contains the UIDs of the users the creator selected.
//        // The creator (currentUserId) is automatically part of the group.
//        // A group needs at least 2 people total (creator + 1 selected).
//        if (selectedUserIds.size() < 1) {
//            Toast.makeText(this, "Select at least 1 member", Toast.LENGTH_SHORT).show();
//            // Keep button disabled as process hasn't finished
//            return;
//        }
//        // Ensure currentUserId is not null before proceeding (checked in onCreate, but double check)
//        if (TextUtils.isEmpty(currentUserId)) {
//            Log.e(TAG, "currentUserId is empty during group creation. Cannot create group.");
//            Toast.makeText(this, "User not authenticated. Cannot create group.", Toast.LENGTH_SHORT).show();
//            // Keep button disabled or redirect
//            // startActivity(new Intent(CreateGroupActivity.this, Login.class)); finish();
//            return;
//        }
//        // Ensure groupsRef is initialized
//        if (groupsRef == null) {
//            Log.e(TAG, "groupsRef is null during group creation. Cannot create group.");
//            Toast.makeText(this, "Database error. Cannot create group.", Toast.LENGTH_SHORT).show();
//            // Keep button disabled
//            return;
//        }
//        // OneSignal API service check is done before sending notifications in onGroupCreationComplete.
//
//        // --- End Validation ---
//
//        // Disable the create button to prevent double submission - it's already disabled by default,
//        // but ensure it stays disabled here if validation fails.
//        // createGroupButton.setEnabled(false); // Already handled by logic
//
//        if (progressDialog != null) {
//            progressDialog.setMessage("Creating group...");
//            progressDialog.show();
//        }
//
//
//        // Step 1: Check if group name already exists
//        Log.d(TAG, "Checking if group name '" + groupName + "' exists.");
//        groupsRef.orderByChild("groupName").equalTo(groupName)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        // This runs on the main thread
//                        if (snapshot.exists()) {
//                            // If the group name already exists, prevent duplicate creation
//                            Log.w(TAG, "Group name '" + groupName + "' already exists.");
//                            Toast.makeText(CreateGroupActivity.this, "Group name already exists! Choose a different name.", Toast.LENGTH_SHORT).show();
//                            createGroupButton.setEnabled(true); // Re-enable button if validation failed earlier or state was inconsistent
//                            if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//                        } else {
//                            // Group name is unique, proceed with creation
//                            Log.d(TAG, "Group name '" + groupName + "' is unique. Proceeding to create group.");
//                            createNewGroup(groupName); // Call method to create the group
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        // This runs on the main thread
//                        Log.e(TAG, "Error checking group name in Firebase: " + error.getMessage(), error.toException());
//                        Toast.makeText(CreateGroupActivity.this, "Error checking group name.", Toast.LENGTH_SHORT).show();
//                        createGroupButton.setEnabled(true); // Re-enable button on error
//                        if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//                    }
//                });
//    }
//
//    private void createNewGroup(String groupName) {
//        // Generate a unique push ID for the new group node
//        String groupId = groupsRef.push().getKey();
//
//        if (groupId == null) {
//            Log.e(TAG, "Failed to generate group ID using push().getKey().");
//            Toast.makeText(this, "Error generating group ID.", Toast.LENGTH_SHORT).show();
//            createGroupButton.setEnabled(true); // Re-enable button
//            if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//            return;
//        }
//
//        Log.d(TAG, "Generated groupId: " + groupId);
//
//        // Prepare initial group data
//        HashMap<String, Object> groupData = new HashMap<>();
//        groupData.put("groupId", groupId); // Store the generated ID within the node
//        groupData.put("groupName", groupName);
//        groupData.put("admin", currentUserId); // Creator is the admin
//        groupData.put("createdAt", System.currentTimeMillis()); // Store creation time
//        // Add any other default group properties here (e.g., groupImage, description)
//
//
//        // Set the initial group data in Firebase
//        groupsRef.child(groupId).setValue(groupData).addOnCompleteListener(task -> {
//            // This runs on the main thread
//            if (task.isSuccessful()) {
//                Log.d(TAG, "Group data written to Firebase successfully for group ID: " + groupId);
//                // Group document is created, now add members
//                addMembersToGroup(groupId, groupName); // Pass group name to the next step
//            } else {
//                Log.e(TAG, "Failed to create group data in Firebase: " + task.getException(), task.getException());
//                Toast.makeText(CreateGroupActivity.this, "Failed to create group.", Toast.LENGTH_SHORT).show();
//                createGroupButton.setEnabled(true); // Re-enable button
//                if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//            }
//        });
//    }
//
//
//    private void addMembersToGroup(String groupId, String groupName) {
//        // Ensure groupId is valid and groupsRef is initialized
//        if (TextUtils.isEmpty(groupId) || groupsRef == null) {
//            Log.e(TAG, "addMembersToGroup: groupId is null or groupsRef is null. Aborting member addition.");
//            Toast.makeText(this, "Error adding members to group.", Toast.LENGTH_SHORT).show();
//            createGroupButton.setEnabled(true);
//            if (progressDialog != null) progressDialog.dismiss();
//            // Maybe attempt to clean up the partially created group node?
//            return;
//        }
//
//        DatabaseReference membersRef = groupsRef.child(groupId).child("members");
//        Log.d(TAG, "Adding members to group ID: " + groupId);
//
//        // We need to fetch names for selected users before adding them to the map and updating Firebase.
//        // A more structured approach: first, get all required user data, then perform the single Firebase update.
//
//        final Map<String, Object> finalMemberUpdates = new HashMap<>(); // Collect all member data here
//
//        // 1. Add the creator (admin) first to the map
//        Log.d(TAG, "Adding creator (" + currentUserId + ") as admin. Name used: '" + currentUserName + "'");
//        HashMap<String, Object> creatorMemberData = new HashMap<>();
//        creatorMemberData.put("role", "admin"); // Creator is the admin
//        creatorMemberData.put("joinedAt", System.currentTimeMillis());
//        creatorMemberData.put("name", currentUserName != null ? currentUserName : "Unknown"); // Use fetched name
//        finalMemberUpdates.put(currentUserId, creatorMemberData); // Add creator to the map
//
//
//        // 2. Fetch names for all selected users and add them to the map
//        // Need to wait for all selected users' names to be fetched.
//        // Use a counter for the asynchronous fetches.
//        final int totalSelectedMembers = selectedUserIds.size();
//        final int[] membersFetchedCount = {0}; // Counter for selected members whose data is fetched
//
//        if (totalSelectedMembers == 0) {
//            // Case: Group with only the creator (admin)
//            Log.d(TAG, "No additional members selected. Group has only the creator.");
//            // Perform the single Firebase update with just the creator's member data
//            membersRef.updateChildren(finalMemberUpdates).addOnCompleteListener(memberTask -> {
//                // This runs on the main thread
//                if (memberTask.isSuccessful()) {
//                    Log.d(TAG, "Creator member data added successfully.");
//                    onGroupCreationComplete(groupId, groupName); // Call completion handler
//                } else {
//                    Log.e(TAG, "Failed to add creator member data.", memberTask.getException());
//                    onGroupCreationFailed(groupId, "Failed to add creator as member."); // Call failure handler
//                }
//            });
//
//        } else {
//            // Case: Group with creator + selected members
//            Log.d(TAG, "Fetching names for " + totalSelectedMembers + " selected members.");
//            for (String userId : selectedUserIds) {
//                if (TextUtils.isEmpty(userId)) {
//                    Log.w(TAG, "Skipping fetching data for empty userId from selected list.");
//                    membersFetchedCount[0]++; // Count this as processed even if invalid
//                    // Check if all selected members' data fetching is complete
//                    if (membersFetchedCount[0] == totalSelectedMembers) {
//                        // If the list had invalid IDs, this might trigger early.
//                        // Proceed with the update using the data gathered so far.
//                        performFinalMemberUpdate(groupId, groupName, finalMemberUpdates);
//                    }
//                    continue; // Skip this iteration
//                }
//
//                // Fetch user name for the selected member
//                usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        // This runs on the main thread
//                        membersFetchedCount[0]++; // Increment counter for this member
//
//                        if (snapshot.exists()) {
//                            String userName = snapshot.child("username").getValue(String.class);
//
//                            // Create HashMap for this member's data
//                            HashMap<String, Object> memberData = new HashMap<>();
//                            memberData.put("role", "member"); // Selected users are members
//                            memberData.put("joinedAt", System.currentTimeMillis());
//                            memberData.put("name", userName != null ? userName : "Unknown"); // Use fetched name
//
//                            // Add this member's data to the map
//                            finalMemberUpdates.put(userId, memberData);
//                            Log.d(TAG, "Fetched data and added member " + userId + " to updates map. Processed: " + membersFetchedCount[0] + "/" + totalSelectedMembers);
//
//                        } else {
//                            Log.w(TAG, "User data not found in /Users for selected member: " + userId + ". Member will not be added to group members list.");
//                            // Don't add to finalMemberUpdates map if data not found
//                            Log.d(TAG, "Processed member " + userId + ". Processed: " + membersFetchedCount[0] + "/" + totalSelectedMembers);
//                        }
//
//                        // Check if all selected members' data fetching is complete
//                        if (membersFetchedCount[0] == totalSelectedMembers) {
//                            Log.d(TAG, "Finished processing all selected members (" + membersFetchedCount[0] + "/" + totalSelectedMembers + "). Performing final member update in Firebase.");
//                            // All member data (creator + successfully fetched selected) should now be in finalMemberUpdates map.
//                            performFinalMemberUpdate(groupId, groupName, finalMemberUpdates);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        // This runs on the main thread
//                        Log.e(TAG, "Firebase Error fetching user name for selected member " + userId + ": " + error.getMessage(), error.toException());
//                        membersFetchedCount[0]++; // Increment counter even on error
//
//                        // Check if all selected members' data fetching is complete (including errors)
//                        if (membersFetchedCount[0] == totalSelectedMembers) {
//                            Log.d(TAG, "Finished processing all selected members (with some errors). Performing final member update in Firebase.");
//                            // Proceed with the update using the data gathered so far.
//                            performFinalMemberUpdate(groupId, groupName, finalMemberUpdates);
//                        }
//                        // Don't show individual member errors as toasts, log instead.
//                    }
//                });
//            } // End of for loop iterating through selectedUserIds
//        }
//    }
//
//
//    // --- NEW: Helper method to perform the final Firebase update for members ---
//    private void performFinalMemberUpdate(String groupId, String groupName, Map<String, Object> memberUpdates) {
//        DatabaseReference membersRef = groupsRef.child(groupId).child("members"); // Get reference again
//
//        membersRef.updateChildren(memberUpdates).addOnCompleteListener(memberTask -> {
//            // This runs on the main thread
//            if (memberTask.isSuccessful()) {
//                Log.d(TAG, "All accessible group members added successfully in Firebase.");
//                onGroupCreationComplete(groupId, groupName); // Call completion handler
//            } else {
//                Log.e(TAG, "Failed to add all group members to Firebase.", memberTask.getException());
//                onGroupCreationFailed(groupId, "Failed to add members to group."); // Call failure handler
//            }
//        });
//    }
//    // --- END NEW Helper method ---
//
//
//    // --- Helper method to handle actions AFTER group creation and member addition are complete ---
//    private void onGroupCreationComplete(String groupId, String groupName) {
//        Log.d(TAG, "Group creation process completed successfully for group ID: " + groupId);
//
//        // *** NEW: Send Separate Push Notifications for Group Creation ***
//
//        // 1. Send notification to the creator (currentUserId)
//        if (oneSignalApiService != null && !TextUtils.isEmpty(currentUserId)) {
//            Log.d(TAG, "Sending group creation notification to creator (" + currentUserId + ").");
//            String creatorTitle = "Group Created!";
//            String creatorContent = "You created the group '" + (groupName != null ? groupName : "Unnamed Group") + "'.";
//            List<String> creatorRecipientList = Collections.singletonList(currentUserId); // List containing only creator's UID
//
//            // Use a generic helper method to send the notification
//            sendOneSignalNotificationToUsers(
//                    oneSignalApiService,
//                    creatorRecipientList,
//                    creatorTitle,
//                    creatorContent,
//                    groupId, // Pass groupId in data
//                    currentUserId, // createdBy in data
//                    currentUserName, // Pass creator's name even to creator's own notification data (consistent)
//                    "group_created_creator" // Event type in data
//            );
//
//        } else {
//            Log.w(TAG, "Cannot send group creation notification to creator: API service is null or currentUserId is empty.");
//        }
//
//
//        // 2. Send notification to the other members (selectedUserIds)
//        // Only send if there are other members selected
//        if (oneSignalApiService != null && selectedUserIds != null && !selectedUserIds.isEmpty()) {
//            Log.d(TAG, "Sending group creation notification to " + selectedUserIds.size() + " other members.");
//            String memberTitle = "New Group: " + (groupName != null ? groupName : "Unnamed Group");
//
//            // --- Construct content using the currentUserName variable (which should now be populated) ---
//            String creatorDisplayName = (currentUserName != null && !TextUtils.isEmpty(currentUserName)) ? currentUserName : "Someone";
//            String memberContent = creatorDisplayName + " added you to '" + (groupName != null ? groupName : "Unnamed Group") + "'.";
//            Log.d(TAG, "onGroupCreationComplete: Member notification content constructed as: '" + memberContent + "' using creatorDisplayName: '" + creatorDisplayName + "'");
//            // --- End Check ---
//
//
//            // Use a generic helper method to send the notification
//            sendOneSignalNotificationToUsers(
//                    oneSignalApiService,
//                    selectedUserIds, // List of UIDs for the other members
//                    memberTitle,
//                    memberContent, // <-- Pass the correctly constructed content string
//                    groupId, // Pass groupId in data
//                    currentUserId, // createdBy in data
//                    currentUserName, // <<< NEW: Pass the fetched creator's name here
//                    "group_created_member" // Event type in data
//            );
//        } else {
//            Log.w(TAG, "Cannot send group creation notification to other members: API service is null or selectedUserIds is empty.");
//        }
//
//        // *** END NEW Separate Notifications ***
//
//        // Dismiss progress dialog
//        if (progressDialog != null) progressDialog.dismiss();
//
//        // Show success toast
//        Toast.makeText(this, "Group '" + groupName + "' created successfully!", Toast.LENGTH_LONG).show();
//
//        // Navigate to Main Activity (or the Group Chat Activity)
//        // Pass groupId to the Group Chat Activity directly?
//        // Intent chatIntent = new Intent(CreateGroupActivity.this, GroupChatActivity.class);
//        // chatIntent.putExtra("groupId", groupId);
//        // startActivity(chatIntent);
//
//        startActivity(new Intent(CreateGroupActivity.this, MainActivity.class)); // Navigate to Main Activity
//        finish(); // Finish this activity
//    }
//
//
//    // --- Helper method to handle failure during member addition ---
//    private void onGroupCreationFailed(String groupId, String reason) {
//        Log.e(TAG, "Group creation failed during member addition for group ID: " + groupId + ". Reason: " + reason);
//        // Handle the failure scenario. The group node might exist without all members.
//        // Decide if you want to clean up the partial group node in Firebase.
//        // For FYP, maybe just log and show an error message.
//        // groupsRef.child(groupId).removeValue(); // Optional: Clean up partial group
//
//        createGroupButton.setEnabled(true); // Re-enable button
//        if (progressDialog != null) progressDialog.dismiss(); // Dismiss dialog
//        Toast.makeText(this, "Failed to create group: " + reason, Toast.LENGTH_SHORT).show();
//    }
//    // --- END NEW Helper methods ---
//
//
//    // *** MODIFIED GENERIC HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION TO A LIST OF USERS ***
//    // Added creatorName parameter.
//    /**
//     * Sends a OneSignal push notification to a specified list of users.
//     *
//     * @param apiService       The Retrofit OneSignalApiService instance.
//     * @param recipientFirebaseUIDs A List of Firebase UIDs of the users who should RECEIVE the notification.
//     * @param title            The title of the notification.
//     * @param messageContent   The main content/body of the notification.
//     * @param groupId          (Optional) The Firebase ID of the group, if applicable.
//     * @param createdByUid     (Optional) The Firebase UID of the user who initiated the action (e.g., creator, sender).
//     * @param creatorName      (Optional) The display name of the creator/sender. Pass this for specific notification types.
//     * @param eventType        A string identifier for the type of notification (e.g., "group_created_creator", "group_created_member", "new_message").
//     */
//    private void sendOneSignalNotificationToUsers(OneSignalApiService apiService,
//                                                  List<String> recipientFirebaseUIDs,
//                                                  String title,
//                                                  String messageContent, // <-- The content string constructed in the caller
//                                                  String groupId, // Added for custom data
//                                                  String createdByUid, // Added for custom data
//                                                  String creatorName, // <<< NEW: Added creatorName parameter
//                                                  String eventType) { // Added for custom data
//
//        // --- Input Validation ---
//        if (apiService == null) {
//            Log.e(TAG, "sendOneSignalNotificationToUsers: API service is null. Cannot send notification.");
//            return;
//        }
//        if (recipientFirebaseUIDs == null || recipientFirebaseUIDs.isEmpty()) {
//            Log.w(TAG, "sendOneSignalNotificationToUsers: Recipient list is null or empty. Cannot send notification.");
//            return;
//        }
//        if (TextUtils.isEmpty(eventType)) {
//            Log.w(TAG, "sendOneSignalNotificationToUsers: Event type is null or empty. Cannot send notification.");
//            return; // eventType is crucial for handling on the recipient side
//        }
//
//
//        Log.d(TAG, "Preparing OneSignal push notification type '" + eventType + "' to " + recipientFirebaseUIDs.size() + " recipients.");
//
//        // --- Build the JSON payload for OneSignal API ---
//        JsonObject notificationBody = new JsonObject();
//
//        // 1. Add App ID
//        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID); // Use your OneSignal App ID (class member)
//
//        // 2. Specify recipients using External User IDs (Firebase UIDs)
//        JsonArray externalUserIdsArray = new JsonArray();
//        for(String uid : recipientFirebaseUIDs) {
//            if (!TextUtils.isEmpty(uid)) {
//                externalUserIdsArray.add(uid); // Add each recipient's Firebase UID
//            }
//        }
//        // If no valid UIDs remain after the loop (shouldn't happen if recipientFirebaseUIDs wasn't empty initially)
//        if (externalUserIdsArray.size() == 0) {
//            Log.w(TAG, "No valid recipient UIDs after filtering list. Skipping notification API call.");
//            return;
//        }
//        notificationBody.add("include_external_user_ids", externalUserIdsArray); // Use include_external_user_ids
//
//
//        // 3. Add Notification Title and Content
//        // Use the *messageContent* string constructed in the caller.
//        // This string *should* contain the creator's name if the caller provided it correctly.
//        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", title))); // Title passed
//        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", messageContent))); // <--- Use the messageContent string
//
//
//        // 4. Add custom data (important for handling notification click in the app)
//        JsonObject data = new JsonObject();
//        data.addProperty("eventType", eventType); // Add the event type
//
//        // Add optional data if available
//        if (!TextUtils.isEmpty(groupId)) {
//            data.addProperty("groupId", groupId);
//        }
//        if (!TextUtils.isEmpty(createdByUid)) {
//            data.addProperty("createdBy", createdByUid); // Creator's UID
//        }
//        // *** NEW: Add creatorName to custom data if provided ***
//        // This gives the receiving app the creator's name directly.
//        if (creatorName != null && !creatorName.isEmpty()) {
//            data.addProperty("creatorName", creatorName);
//            Log.d(TAG, "sendOneSignalNotificationToUsers: Added creatorName '" + creatorName + "' to custom data for event '" + eventType + "'.");
//        }
//        // Add other relevant data depending on eventType (e.g., messageId, conversationId)
//        // For group creation, perhaps the target screen is the group chat or group details?
//        // data.addProperty("targetScreen", "GroupChatActivity"); // Example target screen for click handler
//        // data.addProperty("targetGroupId", groupId); // Pass group ID for navigation
//
//
//        notificationBody.add("data", data);
//
//        // 5. Optional: Set small icon (recommended)
//        notificationBody.addProperty("small_icon", "app_icon_circleup"); // <<< REPLACE with your actual small icon resource name (string)
//
//
//        // 6. Optional: Customize notification appearance (sound, vibration, etc.)
//        // Check OneSignal API documentation for available options:
//        // https://documentation.onesignal.com/reference/create-notification
//        // notificationBody.addProperty("sound", "default"); // Example sound
//
//
//        // --- Make the API call asynchronously using Retrofit ---
//        Log.d(TAG, "Making OneSignal API call for type '" + eventType + "' to " + recipientFirebaseUIDs.size() + " users...");
//        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                // This callback runs on the main thread
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "OneSignal API call successful for type '" + eventType + "'. Response Code: " + response.code());
//                    try (ResponseBody responseBody = response.body()) {
//                        String resBody = responseBody != null ? responseBody.string() : "N/A";
//                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
//                        // Example success body: {"id": "a62fddc6-5c02-4020-a7aa-2d022951bcf1", "recipients": 3}
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read success response body ('" + eventType + "')", e);
//                    }
//                } else {
//                    Log.e(TAG, "OneSignal API call failed for type '" + eventType + "'. Response Code: " + response.code());
//                    try (ResponseBody errorBody = response.errorBody()) {
//                        String errBody = errorBody != null ? errorBody.string() : "N/A";
//                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read error response body ('" + eventType + "')", e);
//                    }
//                    Log.w(TAG, "Push notification failed via OneSignal API for type: " + eventType);
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                // Network error or request couldn't be sent at all
//                Log.e(TAG, "OneSignal API call failed (network error) for type '" + eventType + "'", t);
//                Log.w(TAG, "Push notification failed due to network error for type: " + eventType);
//            }
//        });
//        Log.d(TAG, "OneSignal API call enqueued for type '" + eventType + "'.");
//    }
//    // *** END MODIFIED GENERIC HELPER METHOD ***
//
//
//    // --- Assuming you have a SelectContactAdapter class and a UserModel class ---
//    // SelectContactAdapter will need to manage the 'selectedUserIds' list.
//    // It could have a method like 'toggleSelection(UserModel user)' or update the list directly
//    // when a checkbox/item is clicked in its ViewHolder.
//
//    // Example Adapter Sketch (you already have this, but just showing the idea of updating selectedUserIds)
//    /*
//    public class SelectContactAdapter extends RecyclerView.Adapter<SelectContactAdapter.ContactViewHolder> {
//        private List<UserModel> contactList;
//        private List<String> selectedUserIds; // This list is passed by reference from the Activity
//        private Context context;
//        // Assuming you have an OnItemClickListener interface if needed
//
//        public SelectContactAdapter(Context context, List<UserModel> contactList, List<String> selectedUserIds) {
//            this.context = context;
//            this.contactList = contactList;
//            this.selectedUserIds = selectedUserIds; // Keep reference
//        }
//
//        // ... onCreateViewHolder and onBindViewHolder implementations ...
//
//        @Override
//        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
//            UserModel user = contactList.get(position);
//            holder.userName.setText(user.getUsername());
//            // Load user image
//            // Set checkbox state based on selectedUserIds.contains(user.getUserId())
//            holder.checkbox.setOnCheckedChangeListener(null); // Remove listener before setting state
//            holder.checkbox.setChecked(selectedUserIds.contains(user.getUserId()));
//            holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                 String userId = user.getUserId();
//                 if (userId != null) {
//                     if (isChecked) {
//                         if (!selectedUserIds.contains(userId)) {
//                             selectedUserIds.add(userId);
//                             Log.d("Adapter", "Selected: " + userId);
//                         }
//                     } else {
//                         selectedUserIds.remove(userId);
//                         Log.d("Adapter", "Deselected: " + userId);
//                     }
//                 }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return contactList.size();
//        }
//
//        public static class ContactViewHolder extends RecyclerView.ViewHolder {
//            // Define your ViewHolder elements (TextView, ImageView, CheckBox)
//            TextView userName;
//            CircleImageView profileImage; // If using CircleImageView
//            android.widget.CheckBox checkbox; // Added CheckBox
//            public ContactViewHolder(@NonNull View itemView) {
//                super(itemView);
//                userName = itemView.findViewById(R.id.users_profile_name); // Example ID
//                profileImage = itemView.findViewById(R.id.users_profile_image); // Example ID
//                checkbox = itemView.findViewById(R.id.contact_checkbox); // Example Checkbox ID
//            }
//        }
//    }
//    */
//
//    // --- Assuming you have a UserModel class ---
//    /*
//    public class UserModel {
//        private String userId; // Make sure this is populated when fetching from Firebase
//        private String username;
//        private String status;
//        private String profileImage; // Assuming this is the Base64 string or URL
//
//        // Getters and setters
//        public String getUserId() { return userId; }
//        public void setUserId(String userId) { this.userId = userId; }
//        public String getUsername() { return username; }
//        public void setUsername(String username) { this.username = username; }
//        public String getStatus() { return status; }
//        public void setStatus(String status) { this.status = status; }
//        public String getProfileImage() { return profileImage; }
//        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
//
//        // Add no-argument constructor for Firebase
//        public UserModel() {}
//    }
//    */
//
//}


import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class CreateGroupActivity extends AppCompatActivity implements SelectContactAdapter.OnSelectionChangedListener {

    private static final String TAG = "CreateGroupActivity";

    private Toolbar toolbar;
    private SearchView searchView;
    private EditText groupNameInput;
    private RecyclerView contactsRecyclerView;
    private FloatingActionButton fabNext;
    private TextView textViewSelectedCount;

    private SelectContactAdapter contactAdapter;
    private List<UserModel> contactList = new ArrayList<>();
    private List<String> selectedUserIds = new ArrayList<>();

    private FirebaseAuth auth;
    private DatabaseReference usersRef, groupsRef, contactsRef;
    private String currentUserId;

    private ProgressDialog progressDialog;

    private String currentUserName;

    private OneSignalApiService oneSignalApiService;
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        Log.d(TAG, "🟢 CreateGroupActivity launched");

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Log.e(TAG, "Error: currentUserId is NULL. User not authenticated.");
            Toast.makeText(this, "Authentication error! Please log in again.", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(CreateGroupActivity.this, Login.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        if (currentUserId != null) {
            contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        } else {
            Log.e(TAG, "contactsRef not initialized because currentUserId is null after auth check.");
            Toast.makeText(this, "Initialization error. Cannot load contacts.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Initialize UI Elements ---
        toolbar = findViewById(R.id.toolbar_create_group);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("New group");
            getSupportActionBar().setSubtitle("Add members");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // searchView will be initialized in onCreateOptionsMenu
        groupNameInput = findViewById(R.id.group_name_input);
        contactsRecyclerView = findViewById(R.id.contacts_recycler_view);
        fabNext = findViewById(R.id.fab_next);
        textViewSelectedCount = findViewById(R.id.text_view_selected_count);
        // --- End Initialize UI Elements ---

        if (toolbar == null || groupNameInput == null || contactsRecyclerView == null || fabNext == null || textViewSelectedCount == null) {
            Log.e(TAG, "CRITICAL ERROR: One or more UI elements not found in layout!");
            Toast.makeText(this, "Layout error.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new SelectContactAdapter(this, contactList, selectedUserIds, this);
        contactsRecyclerView.setAdapter(contactAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading contacts...");

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://onesignal.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            oneSignalApiService = retrofit.create(OneSignalApiService.class);
            Log.d(TAG, "OneSignalApiService initialized.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignalApiService", e);
            Toast.makeText(this, "Error initializing notification service. Group notifications may not work.", Toast.LENGTH_LONG).show();
        }

        fabNext.setOnClickListener(v -> {
            Log.d(TAG, "FAB Next clicked.");
            createGroup();
        });
        fabNext.setVisibility(View.GONE);
        textViewSelectedCount.setVisibility(View.GONE);

        fetchCurrentUserName();
        loadContacts();

        Log.d(TAG, "📲 onCreate finished in CreateGroupActivity");
    }

    // --- Override onCreateOptionsMenu to add the search icon and setup SearchView ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_group, menu);

        // *** Initialize SearchView from the menu item ***
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                // --- Set up Search View Listener ---
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        contactAdapter.getFilter().filter(newText);
                        Log.d(TAG, "Search query changed: " + newText);
                        return true;
                    }
                });
                // Make search view expanded by default
                searchView.setIconifiedByDefault(false); // Already set in XML, but double check here
                // Add listener for when the search view is closed
                searchView.setOnCloseListener(() -> {
                    Log.d(TAG, "Search view closed.");
                    // Optionally clear filter or reset list here if needed
                    contactAdapter.getFilter().filter(null); // Show full list again
                    return false; // Allow the system to handle closing
                });

            } else {
                Log.e(TAG, "SearchView is null after getting action view!");
            }
        } else {
            Log.e(TAG, "Search MenuItem not found in menu!");
        }
        // *** End Initialize SearchView ***

        return true;
    }
    // --- End onCreateOptionsMenu ---

    // --- Handle Toolbar Back Button ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // --- End onOptionsItemSelected ---


    @Override
    public void onSelectionChanged(int selectedCount) {
        Log.d(TAG, "Selection changed. Total selected: " + selectedCount);
        textViewSelectedCount.setText(String.valueOf(selectedCount));

        if (selectedCount > 0) {
            fabNext.setVisibility(View.VISIBLE);
            textViewSelectedCount.setVisibility(View.VISIBLE);
        } else {
            fabNext.setVisibility(View.GONE);
            textViewSelectedCount.setVisibility(View.GONE);
        }
    }


    private void fetchCurrentUserName() {
        if (TextUtils.isEmpty(currentUserId) || usersRef == null) {
            Log.w(TAG, "fetchCurrentUserName: currentUserId or usersRef is empty/null, cannot fetch name.");
            currentUserName = "A User";
            return;
        }
        Log.d(TAG, "Fetching current user's (creator) name for UID: " + currentUserId);
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    String name = snapshot.child("username").getValue(String.class);
                    if (!TextUtils.isEmpty(name)) {
                        currentUserName = name;
                    } else {
                        currentUserName = "A User";
                        Log.w(TAG, "Current user's username field is empty. Using default creator name.");
                    }
                } else {
                    currentUserName = "A User";
                    Log.w(TAG, "Current user data or username field not found for creator UID. Using default creator name.");
                }
                Log.d(TAG, "Name fetch complete. currentUserName is: '" + currentUserName + "'");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch current user (creator) name from DB", error.toException());
                currentUserName = "A User";
                Log.d(TAG, "Name fetch cancelled. Defaulting to: '" + currentUserName + "'");
            }
        });
    }


    private void loadContacts() {
        Log.d(TAG, "Loading contacts...");
        if (progressDialog != null) progressDialog.show();

        if (contactsRef == null) {
            Log.e(TAG, "loadContacts: contactsRef is null. Cannot load contacts.");
            if (progressDialog != null) progressDialog.dismiss();
            Toast.makeText(this, "Error loading contacts.", Toast.LENGTH_SHORT).show();
            return;
        }

        contactsRef.orderByChild("request_type").equalTo("accepted")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        contactList.clear();
                        selectedUserIds.clear();

                        if (!snapshot.exists()) {
                            Log.d(TAG, "No accepted contacts found for current user.");
                            contactAdapter.setContactList(new ArrayList<>());
                            onSelectionChanged(0);

                            if (progressDialog != null) progressDialog.dismiss();
                            Toast.makeText(CreateGroupActivity.this, "No friends found to add to a group.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<String> friendIds = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String friendId = dataSnapshot.getKey();
                            if (friendId != null && !TextUtils.isEmpty(friendId)) {
                                friendIds.add(friendId);
                            }
                        }
                        Log.d(TAG, "Found " + friendIds.size() + " accepted contact friend UIDs.");


                        if (friendIds.isEmpty()) {
                            Log.d(TAG, "Friend IDs list is empty after filtering. No contacts to display.");
                            contactAdapter.setContactList(new ArrayList<>());
                            onSelectionChanged(0);

                            if (progressDialog != null) progressDialog.dismiss();
                            Toast.makeText(CreateGroupActivity.this, "No friends found to add to a group.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                List<UserModel> fetchedUsers = new ArrayList<>();

                                Log.d(TAG, "Fetching details for " + friendIds.size() + " friends from /Users.");
                                for (String friendId : friendIds) {
                                    if (userSnapshot.hasChild(friendId)) {
                                        UserModel user = userSnapshot.child(friendId).getValue(UserModel.class);
                                        if (user != null) {
                                            user.setUserId(friendId);
                                            fetchedUsers.add(user);
                                        } else {
                                            Log.w(TAG, "UserModel is null for friendId: " + friendId);
                                        }
                                    } else {
                                        Log.w(TAG, "User data not found in /Users for friendId: " + friendId);
                                    }
                                }
                                Log.d(TAG, "Loaded " + fetchedUsers.size() + " user models for contacts.");

                                contactList.clear();
                                contactList.addAll(fetchedUsers);

                                contactAdapter.setContactList(new ArrayList<>(contactList));

                                onSelectionChanged(0);


                                if (progressDialog != null) progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Firebase Error fetching users for contacts: " + error.getMessage(), error.toException());
                                if (progressDialog != null) progressDialog.dismiss();
                                Toast.makeText(CreateGroupActivity.this, "Error fetching contacts.", Toast.LENGTH_SHORT).show();
                                contactAdapter.setContactList(new ArrayList<>());
                                onSelectionChanged(0);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase Database Error loading accepted contacts: " + error.getMessage(), error.toException());
                        if (progressDialog != null) progressDialog.dismiss();
                        Toast.makeText(CreateGroupActivity.this, "Error loading contacts.", Toast.LENGTH_SHORT).show();
                        contactAdapter.setContactList(new ArrayList<>());
                        onSelectionChanged(0);
                    }
                });
    }

    private void createGroup() {
        String groupName = groupNameInput.getText().toString().trim();

        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(this, "Enter group name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedUserIds.isEmpty()) {
            Toast.makeText(this, "Select at least 1 member", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(currentUserId)) {
            Log.e(TAG, "currentUserId is empty during group creation. Cannot create group.");
            Toast.makeText(this, "User not authenticated. Cannot create group.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (groupsRef == null) {
            Log.e(TAG, "groupsRef is null during group creation. Cannot create group.");
            Toast.makeText(this, "Database error. Cannot create group.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressDialog != null) {
            progressDialog.setMessage("Creating group...");
            progressDialog.show();
        }

        fabNext.setEnabled(false);

        Log.d(TAG, "Checking if group name '" + groupName + "' exists.");
        groupsRef.orderByChild("groupName").equalTo(groupName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.w(TAG, "Group name '" + groupName + "' already exists.");
                            Toast.makeText(CreateGroupActivity.this, "Group name already exists! Choose a different name.", Toast.LENGTH_SHORT).show();
                            fabNext.setEnabled(true);
                            if (progressDialog != null) progressDialog.dismiss();
                        } else {
                            Log.d(TAG, "Group name '" + groupName + "' is unique. Proceeding to create group.");
                            createNewGroup(groupName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking group name in Firebase: " + error.getMessage(), error.toException());
                        Toast.makeText(CreateGroupActivity.this, "Error checking group name.", Toast.LENGTH_SHORT).show();
                        fabNext.setEnabled(true);
                        if (progressDialog != null) progressDialog.dismiss();
                    }
                });
    }

    private void createNewGroup(String groupName) {
        String groupId = groupsRef.push().getKey();

        if (groupId == null) {
            Log.e(TAG, "Failed to generate group ID using push().getKey().");
            Toast.makeText(this, "Error generating group ID.", Toast.LENGTH_SHORT).show();
            fabNext.setEnabled(true);
            if (progressDialog != null) progressDialog.dismiss();
            return;
        }

        Log.d(TAG, "Generated groupId: " + groupId);

        HashMap<String, Object> groupData = new HashMap<>();
        groupData.put("groupId", groupId);
        groupData.put("groupName", groupName);
        groupData.put("admin", currentUserId);
        groupData.put("createdAt", System.currentTimeMillis());


        groupsRef.child(groupId).setValue(groupData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Group data written to Firebase successfully for group ID: " + groupId);
                addMembersToGroup(groupId, groupName);
            } else {
                Log.e(TAG, "Failed to create group data in Firebase: " + task.getException(), task.getException());
                Toast.makeText(CreateGroupActivity.this, "Failed to create group.", Toast.LENGTH_SHORT).show();
                fabNext.setEnabled(true);
                if (progressDialog != null) progressDialog.dismiss();
            }
        });
    }


    private void addMembersToGroup(String groupId, String groupName) {
        if (TextUtils.isEmpty(groupId) || groupsRef == null) {
            Log.e(TAG, "addMembersToGroup: groupId is null or groupsRef is null. Aborting member addition.");
            Toast.makeText(this, "Error adding members to group.", Toast.LENGTH_SHORT).show();
            fabNext.setEnabled(true);
            if (progressDialog != null) progressDialog.dismiss();
            return;
        }

        DatabaseReference membersRef = groupsRef.child(groupId).child("members");
        Log.d(TAG, "Adding members to group ID: " + groupId);

        final Map<String, Object> finalMemberUpdates = new HashMap<>();

        Log.d(TAG, "Adding creator (" + currentUserId + ") as admin. Name used: '" + currentUserName + "'");
        HashMap<String, Object> creatorMemberData = new HashMap<>();
        creatorMemberData.put("role", "admin");
        creatorMemberData.put("joinedAt", System.currentTimeMillis());
        creatorMemberData.put("name", currentUserName != null ? currentUserName : "Unknown");
        finalMemberUpdates.put(currentUserId, creatorMemberData);

        Log.d(TAG, "Adding " + selectedUserIds.size() + " selected members.");

        for (String selectedUserId : selectedUserIds) {
            UserModel selectedUser = null;
            for (UserModel user : contactList) {
                if (user.getUserId() != null && user.getUserId().equals(selectedUserId)) {
                    selectedUser = user;
                    break;
                }
            }

            if (selectedUser != null) {
                HashMap<String, Object> memberData = new HashMap<>();
                memberData.put("role", "member");
                memberData.put("joinedAt", System.currentTimeMillis());
                memberData.put("name", selectedUser.getUsername() != null ? selectedUser.getUsername() : "Unknown");

                finalMemberUpdates.put(selectedUserId, memberData);
                Log.d(TAG, "Added selected member " + selectedUserId + " to updates map.");
            } else {
                Log.w(TAG, "UserModel not found in contactList for selected userId: " + selectedUserId + ". Skipping adding this member.");
            }
        }
        Log.d(TAG, "Finished preparing member updates map. Total members to add: " + finalMemberUpdates.size());


        membersRef.updateChildren(finalMemberUpdates).addOnCompleteListener(memberTask -> {
            if (memberTask.isSuccessful()) {
                Log.d(TAG, "All accessible group members added successfully in Firebase.");
                onGroupCreationComplete(groupId, groupName);
            } else {
                Log.e(TAG, "Failed to add all group members to Firebase.", memberTask.getException());
                onGroupCreationFailed(groupId, "Failed to add members to group.");
            }
        });
    }


    private void onGroupCreationComplete(String groupId, String groupName) {
        Log.d(TAG, "Group creation process completed successfully for group ID: " + groupId);

        if (oneSignalApiService != null && !TextUtils.isEmpty(currentUserId)) {
            Log.d(TAG, "Sending group creation notification to creator (" + currentUserId + ").");
            String creatorTitle = "Group Created!";
            String creatorContent = "You created the group '" + (groupName != null ? groupName : "Unnamed Group") + "'.";
            List<String> creatorRecipientList = Collections.singletonList(currentUserId);

            sendOneSignalNotificationToUsers(
                    oneSignalApiService,
                    creatorRecipientList,
                    creatorTitle,
                    creatorContent,
                    groupId,
                    currentUserId,
                    currentUserName,
                    "group_created_creator"
            );
        } else {
            Log.w(TAG, "Cannot send group creation notification to creator: API service is null or currentUserId is empty.");
        }


        if (oneSignalApiService != null && selectedUserIds != null && !selectedUserIds.isEmpty()) {
            Log.d(TAG, "Sending group creation notification to " + selectedUserIds.size() + " other members.");
            String memberTitle = "New Group: " + (groupName != null ? groupName : "Unnamed Group");
            String creatorDisplayName = (currentUserName != null && !TextUtils.isEmpty(currentUserName)) ? currentUserName : "Someone";
            String memberContent = creatorDisplayName + " added you to '" + (groupName != null ? groupName : "Unnamed Group") + "'.";

            sendOneSignalNotificationToUsers(
                    oneSignalApiService,
                    selectedUserIds,
                    memberTitle,
                    memberContent,
                    groupId,
                    currentUserId,
                    currentUserName,
                    "group_created_member"
            );
        } else {
            Log.w(TAG, "Cannot send group creation notification to other members: API service is null or selectedUserIds is empty.");
        }

        if (progressDialog != null) progressDialog.dismiss();

        Toast.makeText(this, "Group '" + groupName + "' created successfully!", Toast.LENGTH_LONG).show();

        startActivity(new Intent(CreateGroupActivity.this, MainActivity.class));
        finish();
    }


    private void onGroupCreationFailed(String groupId, String reason) {
        Log.e(TAG, "Group creation failed during member addition for group ID: " + groupId + ". Reason: " + reason);
        fabNext.setEnabled(true);
        if (progressDialog != null) progressDialog.dismiss();
        Toast.makeText(this, "Failed to create group: " + reason, Toast.LENGTH_SHORT).show();
    }


    private void sendOneSignalNotificationToUsers(OneSignalApiService apiService,
                                                  List<String> recipientFirebaseUIDs,
                                                  String title,
                                                  String messageContent,
                                                  String groupId,
                                                  String createdByUid,
                                                  String creatorName,
                                                  String eventType) {

        if (apiService == null || recipientFirebaseUIDs == null || recipientFirebaseUIDs.isEmpty() || TextUtils.isEmpty(eventType)) {
            Log.e(TAG, "sendOneSignalNotificationToUsers: Missing required parameters. Cannot send notification.");
            return;
        }

        Log.d(TAG, "Preparing OneSignal push notification type '" + eventType + "' to " + recipientFirebaseUIDs.size() + " recipients.");

        JsonObject notificationBody = new JsonObject();
        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID);

        JsonArray externalUserIdsArray = new JsonArray();
        for(String uid : recipientFirebaseUIDs) {
            if (!TextUtils.isEmpty(uid)) {
                externalUserIdsArray.add(uid);
            }
        }
        if (externalUserIdsArray.size() == 0) {
            Log.w(TAG, "No valid recipient UIDs after filtering list. Skipping notification API call.");
            return;
        }
        notificationBody.add("include_external_user_ids", externalUserIdsArray);

        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", title)));
        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", messageContent)));


        JsonObject data = new JsonObject();
        data.addProperty("eventType", eventType);

        if (!TextUtils.isEmpty(groupId)) {
            data.addProperty("groupId", groupId);
        }
        if (!TextUtils.isEmpty(createdByUid)) {
            data.addProperty("createdBy", createdByUid);
        }
        if (creatorName != null && !creatorName.isEmpty()) {
            data.addProperty("creatorName", creatorName);
            Log.d(TAG, "sendOneSignalNotificationToUsers: Added creatorName '" + creatorName + "' to custom data for event '" + eventType + "'.");
        }

        notificationBody.add("data", data);

        notificationBody.addProperty("small_icon", "app_icon_circleup");

        Log.d(TAG, "Making OneSignal API call for type '" + eventType + "' to " + recipientFirebaseUIDs.size() + " users...");
        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "OneSignal API call successful for type '" + eventType + "'. Response Code: " + response.code());
                    try (ResponseBody responseBody = response.body()) {
                        String resBody = responseBody != null ? responseBody.string() : "N/A";
                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
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
                Log.e(TAG, "OneSignal API call failed (network error) for type '" + eventType + "'", t);
                Log.w(TAG, "Push notification failed due to network error for type: " + eventType);
            }
        });
        Log.d(TAG, "OneSignal API call enqueued for type '" + eventType + "'.");
    }
}