package com.sana.circleup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.GroupListDao;
import com.sana.circleup.temporary_chat_room.TemporaryRoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

// Activity to select recipients (Contacts, Groups, Temporary Rooms) for scheduled messages.
public class ScheduledRecipientSelectionActivity extends AppCompatActivity {

    private static final String TAG = "RecipientSelection";

    private Toolbar toolbar;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private Button btnConfirmSelection;

    private SelectableRecipientAdapter adapter;
    private List<SelectableRecipient> allRecipientsList = new ArrayList<>(); // Holds the full combined list

    // Firebase & Auth
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private String currentUserId;

    // Room DB & DAOs (for fetching existing groups/rooms)
    private ChatDatabase chatDatabase;
    private GroupListDao groupListDao; // Assuming GroupListDao also handles TemporaryRoomEntity queries
    private ExecutorService databaseExecutor;

    // Firebase Listeners (for syncing initial lists if needed, though Room LiveData sync is often handled elsewhere)
    // We'll fetch lists from Firebase once here, not live listen in this specific selection screen.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_recipient_selection);

        Log.d(TAG, "🟢 ScheduledRecipientSelectionActivity launched");

        // --- Initialize UI ---
        toolbar = findViewById(R.id.toolbar_recipient_selection);
        searchView = findViewById(R.id.search_recipients);
        recyclerView = findViewById(R.id.recycler_recipients);
        tvEmpty = findViewById(R.id.tv_empty_recipients);
        btnConfirmSelection = findViewById(R.id.btn_confirm_selection);

        // Set up Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
            // Title is set in XML
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter with an empty list. Data will be loaded and updated later.
        adapter = new SelectableRecipientAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);


        // --- Initialize Firebase & Auth ---
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "User not authenticated. Cannot select recipients.");
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_LONG).show();
            finish(); // Finish activity if user is not logged in
            return;
        }
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "Current User ID: " + currentUserId);

        // --- Initialize Executor for background tasks ---
        // Reusing the database executor from ChatDatabase singleton for background work
        databaseExecutor = ChatDatabase.databaseWriteExecutor;
        if (databaseExecutor == null) {
            Log.e(TAG, "ChatDatabase.databaseWriteExecutor is null! Cannot perform background tasks.");
            Toast.makeText(this, "Internal error. Cannot load data.", Toast.LENGTH_LONG).show();
            tvEmpty.setText("Error initializing background tasks.");
            tvEmpty.setVisibility(View.VISIBLE);
            btnConfirmSelection.setEnabled(false);
            return;
        }


        // --- Load Data (Contacts, Groups, Temporary Rooms) ---
        loadRecipientsData(); // Call the method to fetch data

        // --- Set Listeners ---
        setupListeners(); // Call the method to set up search and button listeners

        // Set the result to CANCELED by default in case the user backs out without confirming
        setResult(Activity.RESULT_CANCELED);
        Log.d(TAG, "Activity result set to CANCELED by default.");
    }

    // Handle back button click on Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Method to Load All Recipient Data (Contacts, Groups, Rooms) ---
    private void loadRecipientsData() {
        Log.d(TAG, "Starting to load recipient data for user: " + currentUserId);
        allRecipientsList.clear(); // Clear the main list before loading

        // Show loading state
        runOnUiThread(() -> { // Ensure UI updates are on the main thread
            tvEmpty.setText("Loading recipients...");
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnConfirmSelection.setEnabled(false); // Disable button while loading
        });


        // Use CountDownLatch to wait for all asynchronous Firebase fetches to complete
        // We need to fetch: 1. User's Friends, 2. User's Groups, 3. User's Temporary Rooms
        int numberOfSourcesToFetch = 3;
        CountDownLatch latch = new CountDownLatch(numberOfSourcesToFetch);

        // Execute fetches on a background thread
        databaseExecutor.execute(() -> {
            Log.d(TAG, "Recipient data fetching started on background thread.");

            // --- Fetch 1: User's Friends ---
            fetchFriends(latch); // fetchFriends will call latch.countDown() when done

            // --- Fetch 2: User's Groups ---
            fetchGroups(latch); // fetchGroups will call latch.countDown() when done

            // --- Fetch 3: User's Temporary Rooms ---
            fetchTemporaryRooms(latch); // fetchTemporaryRooms will call latch.countDown() when done


            // --- Wait for all fetches to complete and then process the combined list ---
            try {
                latch.await(); // Wait until count becomes zero

                Log.d(TAG, "All recipient data sources finished fetching. Total items collected: " + allRecipientsList.size());

                // Sort the combined list (e.g., alphabetically by name)
                Collections.sort(allRecipientsList, Comparator.comparing(SelectableRecipient::getName, Comparator.nullsLast(String::compareToIgnoreCase))); // Handle null names

                // Update the adapter on the main thread
                runOnUiThread(() -> {
                    adapter.updateList(allRecipientsList); // Use adapter's updateList method

                    // Update UI state based on whether the list is empty
                    if (allRecipientsList.isEmpty()) {
                        tvEmpty.setText("No contacts, groups, or rooms found.");
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        btnConfirmSelection.setEnabled(false); // Nothing to select
                        Log.d(TAG, "No recipients found, showing empty state.");
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        btnConfirmSelection.setEnabled(true); // Enable button if there are items
                        Log.d(TAG, "Recipients loaded, showing list.");
                    }
                    Log.d(TAG, "Recipient selection UI updated after loading.");
                });

            } catch (InterruptedException e) {
                Log.e(TAG, "CountDownLatch interrupted while waiting for recipient data.", e);
                runOnUiThread(() -> {
                    tvEmpty.setText("Error loading recipients.");
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    btnConfirmSelection.setEnabled(false);
                    Toast.makeText(ScheduledRecipientSelectionActivity.this, "Error loading recipients.", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "An unexpected error occurred while processing recipient data.", e);
                runOnUiThread(() -> {
                    tvEmpty.setText("Error loading recipients.");
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    btnConfirmSelection.setEnabled(false);
                    Toast.makeText(ScheduledRecipientSelectionActivity.this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // --- Helper method to fetch User's Friends from Firebase ---
    private void fetchFriends(CountDownLatch latch) {
        Log.d(TAG, "Fetching user friends from Firebase...");
        DatabaseReference contactsRef = rootRef.child("Contacts").child(currentUserId);
        DatabaseReference usersRef = rootRef.child("Users");

        contactsRef.orderByChild("request_type").equalTo("accepted")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> friendUids = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String friendId = ds.getKey(); // Friend's UID is the key
                                if (!TextUtils.isEmpty(friendId)) {
                                    friendUids.add(friendId);
                                }
                            }
                        }
                        Log.d(TAG, "Found " + friendUids.size() + " accepted friend UIDs.");

                        if (friendUids.isEmpty()) {
                            Log.d(TAG, "No friends found. Completing fetchFriends.");
                            latch.countDown(); // Finish this source even if no friends
                            return;
                        }

                        // Now fetch UserModel details for these friend UIDs
                        fetchUserModels(friendUids, new UserModelsFetchCallback() {
                            @Override
                            public void onUserModelsFetched(List<UserModel> userModels) {
                                Log.d(TAG, "Fetched " + userModels.size() + " UserModel details for friends. Converting to SelectableUser.");
                                // Convert UserModels to SelectableUser and add to main list
                                for (UserModel user : userModels) {
                                    if (user != null && !TextUtils.isEmpty(user.getUserId())) {
                                        allRecipientsList.add(new SelectableUser(user));
                                    } else {
                                        Log.w(TAG, "Skipping null or empty UserModel after fetch for friends.");
                                    }
                                }
                                Log.d(TAG, "Added " + userModels.size() + " selectable users to the list. Completing fetchFriends.");
                                latch.countDown(); // Signal this source is complete
                            }

                            @Override
                            public void onError(DatabaseError error) {
                                Log.e(TAG, "Error fetching UserModel details for friends: " + error.getMessage());
                                // Continue even on error, just don't add these users
                                latch.countDown(); // Signal this source is complete (with error)
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase Error loading accepted contacts: " + error.getMessage());
                        // Continue even on error
                        latch.countDown(); // Signal this source is complete (with cancellation)
                    }
                });
    }

    // Helper method to fetch multiple UserModels given a list of UIDs (reused from DraftScheduled)
    // This needs to be in ScheduledRecipientSelectionActivity.java
    private void fetchUserModels(List<String> userIds, UserModelsFetchCallback callback) {
        if (userIds == null || userIds.isEmpty() || callback == null) {
            Log.d(TAG, "fetchUserModels called with empty list or null callback.");
            if (callback != null) callback.onUserModelsFetched(new ArrayList<>());
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        List<UserModel> userModels = new ArrayList<>();
        int totalUsers = userIds.size();
        final int[] processedCount = {0};
        final boolean[] hasError = {false};
        final DatabaseError[] lastError = {null};

        // Ensure allUserIds are unique and non-empty before starting fetches
        List<String> uniqueUserIds = new ArrayList<>();
        for(String id : userIds) {
            if (!TextUtils.isEmpty(id) && !uniqueUserIds.contains(id)) {
                uniqueUserIds.add(id);
            }
        }
        if (uniqueUserIds.isEmpty()) {
            Log.d(TAG, "Unique user IDs list is empty after filtering. Calling callback with empty list.");
            if (callback != null) callback.onUserModelsFetched(new ArrayList<>());
            return;
        }
        totalUsers = uniqueUserIds.size(); // Update total count to unique count
        Log.d(TAG, "Fetching user models for " + totalUsers + " unique IDs.");


        for (String userId : uniqueUserIds) { // Loop through unique IDs
            int finalTotalUsers = totalUsers;
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            user.setUserId(snapshot.getKey()); // Ensure userId is set from key
                            userModels.add(user);
                        } else {
                            Log.w(TAG, "User model null for ID: " + userId + " during batch fetch.");
                        }
                    } else {
                        Log.w(TAG, "User data not found for ID: " + userId + " during batch fetch.");
                    }
                    processedCount[0]++;
                    if (processedCount[0] == finalTotalUsers) {
                        if (hasError[0]) {
                            callback.onError(lastError[0]);
                        } else {
                            callback.onUserModelsFetched(userModels);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to fetch user model for ID: " + userId + " during batch fetch: " + error.getMessage());
                    if (!hasError[0]) {
                        hasError[0] = true;
                        lastError[0] = error;
                    }
                    processedCount[0]++;
                    if (processedCount[0] == finalTotalUsers) {
                        callback.onError(lastError[0]);
                    }
                }
            });
        }
    }

    // Callback interface for fetching multiple UserModels (reused from DraftScheduled)
    // This needs to be in ScheduledRecipientSelectionActivity.java
    private interface UserModelsFetchCallback {
        void onUserModelsFetched(List<UserModel> userModels);
        void onError(DatabaseError error);
    }


    // --- Helper method to fetch User's Groups from Firebase ---
    private void fetchGroups(CountDownLatch latch) {
        Log.d(TAG, "Fetching user groups from Firebase...");
        DatabaseReference groupsRef = rootRef.child("Groups");

        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int groupsProcessed = 0;
                long totalGroupsToCheck = snapshot.getChildrenCount();
                if (totalGroupsToCheck == 0) {
                    Log.d(TAG, "No groups found in Firebase.");
                    latch.countDown();
                    return;
                }

                Log.d(TAG, "Checking membership for " + totalGroupsToCheck + " groups.");

                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey();
                    if (groupId == null) {
                        Log.w(TAG, "Skipping group with null ID.");
                        groupsProcessed++;
                        if (groupsProcessed == totalGroupsToCheck) latch.countDown();
                        continue;
                    }

                    DataSnapshot membersSnapshot = groupSnapshot.child("members");
                    boolean isMember = membersSnapshot.hasChild(currentUserId);

                    if (isMember) {
                        // If user is a member, create a SelectableGroup model
                        String groupName = groupSnapshot.child("groupName").getValue(String.class);
                        String groupImageBase64 = groupSnapshot.child("groupImage").getValue(String.class);

                        // Create a new SelectableGroup object and add to the main list
                        // Pass necessary fields directly or create a temporary Group model
                        // Creating a temporary Group model makes it easier to reuse the SelectableGroup constructor
                        Group tempGroupModel = new Group();
                        tempGroupModel.setGroupId(groupId);
                        tempGroupModel.setGroupName(groupName);
                        tempGroupModel.setGroupImage(groupImageBase64);

                        allRecipientsList.add(new SelectableGroup(tempGroupModel));
                        Log.d(TAG, "User is member of group " + groupId + ". Added to selection list.");
                    }
                    groupsProcessed++;
                    if (groupsProcessed == totalGroupsToCheck) {
                        Log.d(TAG, "Finished processing all groups for membership. Completing fetchGroups.");
                        latch.countDown();
                    }
                }
                if (totalGroupsToCheck == 0 && snapshot.exists()) {
                    latch.countDown();
                } else if (!snapshot.exists()) {
                    Log.d(TAG, "Firebase Groups node does not exist.");
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase Error loading groups: " + error.getMessage());
                latch.countDown();
            }
        });
    }

    // --- Helper method to fetch User's Temporary Rooms from Firebase ---
    private void fetchTemporaryRooms(CountDownLatch latch) {
        Log.d(TAG, "Fetching user temporary rooms from Firebase...");
        DatabaseReference tempRoomsRef = rootRef.child("temporaryChatRooms");

        tempRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentTime = System.currentTimeMillis();
                int roomsProcessed = 0;
                long totalRoomsToCheck = snapshot.getChildrenCount();

                if (totalRoomsToCheck == 0) {
                    Log.d(TAG, "No temporary rooms found in Firebase.");
                    latch.countDown();
                    return;
                }

                Log.d(TAG, "Checking membership and expiry for " + totalRoomsToCheck + " temporary rooms.");

                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    String roomId = roomSnapshot.getKey();
                    if (roomId == null) {
                        Log.w(TAG, "Skipping temporary room with null ID.");
                        roomsProcessed++;
                        if (roomsProcessed == totalRoomsToCheck) latch.countDown();
                        continue;
                    }

                    DataSnapshot membersSnapshot = roomSnapshot.child("members");
                    Long expiryTime = roomSnapshot.child("expiryTime").getValue(Long.class);

                    boolean isMember = membersSnapshot.hasChild(currentUserId);
                    boolean isExpired = (expiryTime != null && currentTime > expiryTime);

                    if (isMember && !isExpired) {
                        // If user is a member and room is NOT expired, create a SelectableTemporaryRoom model
                        String roomName = roomSnapshot.child("roomName").getValue(String.class);

                        // Create a new SelectableTemporaryRoom object and add to the main list
                        // Pass necessary fields directly or create a temporary TemporaryRoom model
                        TemporaryRoom tempRoomModel = new TemporaryRoom();
                        tempRoomModel.setRoomId(roomId);
                        tempRoomModel.setRoomName(roomName);
                        tempRoomModel.setExpiryTime(expiryTime);

                        allRecipientsList.add(new SelectableTemporaryRoom(tempRoomModel));
                        Log.d(TAG, "User is member of unexpired temporary room " + roomId + ". Added to selection list.");
                    }
                    roomsProcessed++;
                    if (roomsProcessed == totalRoomsToCheck) {
                        Log.d(TAG, "Finished processing all temporary rooms. Completing fetchTemporaryRooms.");
                        latch.countDown();
                    }
                }
                if (totalRoomsToCheck == 0 && snapshot.exists()) {
                    latch.countDown();
                } else if (!snapshot.exists()) {
                    Log.d(TAG, "Firebase temporaryChatRooms node does not exist.");
                    latch.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase Error loading temporary rooms: " + error.getMessage());
                latch.countDown();
            }
        });
    }


    // --- Method to Set Up UI Listeners (Search and Button) ---
    private void setupListeners() {
        // Set up Search View listener for filtering
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
                    Log.d(TAG, "Search query changed: " + newText);
                    // Adapter's publishResults method will call notifyDataSetChanged.
                    // To update the empty state TextView based on filtered results,
                    // you could check adapter.getItemCount() after a short delay or in the adapter's publishResults via a callback.
                    // For simplicity now, the empty state only fully updates on initial load or manual refresh.
                    return true;
                }
            });
        } else {
            Log.w(TAG, "SearchView not found!");
        }


        // Set up Confirm Selection Button listener
        if (btnConfirmSelection != null) {
            btnConfirmSelection.setOnClickListener(v -> {
                // Get the list of selected recipient info from the adapter
                List<SelectableRecipientAdapter.SelectedRecipientInfo> selectedRecipients = adapter.getSelectedRecipientsInfo();

                if (selectedRecipients.isEmpty()) {
                    Toast.makeText(this, "Please select at least one recipient.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Confirm button clicked with no recipients selected.");
                    return;
                }

                Log.d(TAG, "Confirm button clicked. Selected recipients count: " + selectedRecipients.size());

                // Prepare the result intent to send back to ScheduledMSG activity
                Intent resultIntent = new Intent();
                // Pass the list of selected recipient IDs and types back to the calling activity.
                // Using Bundle and putting ArrayList<String> for IDs and Types is a common way.
                ArrayList<String> selectedIds = new ArrayList<>();
                ArrayList<String> selectedTypes = new ArrayList<>();
                ArrayList<String> selectedNames = new ArrayList<>();

                for (SelectableRecipientAdapter.SelectedRecipientInfo info : selectedRecipients) {
                    selectedIds.add(info.getId());
                    selectedTypes.add(info.getType());
                    selectedNames.add(info.getName());
                }

                resultIntent.putStringArrayListExtra("selectedRecipientIds", selectedIds);
                resultIntent.putStringArrayListExtra("selectedRecipientTypes", selectedTypes);
                resultIntent.putStringArrayListExtra("selectedRecipientNames", selectedNames);

                // Set the result to OK and pass the intent
                setResult(Activity.RESULT_OK, resultIntent);
                Log.d(TAG, "Result set to OK. Finishing activity.");
                finish(); // Close the activity
            });
        } else {
            Log.w(TAG, "Confirm Selection Button not found!");
        }
    }
}