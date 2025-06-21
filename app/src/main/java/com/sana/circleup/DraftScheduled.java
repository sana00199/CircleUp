package com.sana.circleup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query; // Import Query
import com.google.android.gms.tasks.Tasks; // Import Tasks for synchronous operations if needed
import com.google.firebase.database.ChildEventListener; // Consider using ChildEventListener for more granular updates

import java.util.ArrayList;
import java.util.Collections; // Import Collections for sorting
import java.util.Comparator; // Import Comparator
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit; // Import TimeUnit

public class DraftScheduled extends AppCompatActivity {

    private static final String TAG = "DraftScheduled"; // Add TAG
    private RecyclerView recyclerScheduled;
    private List<ScheMsg> scheduledMessages; // Use ScheMsg model
    private ScheduledMessagesAdapterDraft adapter; // Use the updated adapter
    private TextView tvNoDrafts;
    private Toolbar toolbar;

    private DatabaseReference scheduledMessagesRef; // Firebase reference to the ScheduledMessages node
    private ValueEventListener scheduledMessagesListener; // Use a ValueEventListener for real-time updates


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_scheduled);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Drafts");
        }

        recyclerScheduled = findViewById(R.id.recyclerScheduled);
        recyclerScheduled.setLayoutManager(new LinearLayoutManager(this));

        tvNoDrafts = findViewById(R.id.tvNoDrafts);

        scheduledMessages = new ArrayList<>();
        adapter = new ScheduledMessagesAdapterDraft(this, scheduledMessages);
        recyclerScheduled.setAdapter(adapter);

        // Set the delete click listener on the adapter
        adapter.setOnDeleteClickListener(this::deleteScheduledMessage);

        // Initialize Firebase reference
        scheduledMessagesRef = FirebaseDatabase.getInstance().getReference("ScheduledMessages");

        // Load and listen for scheduled messages
        loadAndListenForScheduledMessages();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadAndListenForScheduledMessages() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (TextUtils.isEmpty(currentUid)) {
            Log.e(TAG, "Current user ID is null or empty. Cannot load scheduled messages.");
            tvNoDrafts.setText("Error: User not authenticated.");
            tvNoDrafts.setVisibility(View.VISIBLE);
            return;
        }

        // --- ADD THIS LOG LINE ---
        Log.d(TAG, "Current user UID for Drafts query: " + currentUid);
        // --- END ADD ---

        Query scheduledMessagesQuery = scheduledMessagesRef.orderByChild("senderId").equalTo(currentUid);
        Log.d(TAG, "Attaching real-time listener for scheduled messages for senderId: " + currentUid);

        scheduledMessagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange triggered for scheduled messages. Count: " + snapshot.getChildrenCount());
                List<ScheMsg> tempList = new ArrayList<>();
                List<String> receiverIdsToFetchNames = new ArrayList<>(); // List to collect all unique receiver IDs across all messages in this snapshot

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    Log.d(TAG, "No scheduled messages found for senderId: " + currentUid);
                    scheduledMessages.clear();
                    adapter.setScheduledMessages(scheduledMessages); // Update adapter with empty list
                    tvNoDrafts.setVisibility(View.VISIBLE);
                    return;
                }

                // First pass: Collect all messages and all receiver IDs
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ScheMsg message = ds.getValue(ScheMsg.class);
                    if (message != null) {
                        message.setMsgFirebaseId(ds.getKey()); // Set the Firebase key
                        tempList.add(message);

                        // Collect receiver IDs from this message
                        List<String> msgReceiverIds = message.getReceiverIds();
                        if (msgReceiverIds != null) {
                            for (String receiverId : msgReceiverIds) {
                                if (!TextUtils.isEmpty(receiverId) && !receiverIdsToFetchNames.contains(receiverId)) {
                                    receiverIdsToFetchNames.add(receiverId);
                                }
                            }
                        }
                    } else {
                        Log.w(TAG, "Received null ScheMsg object from snapshot: " + ds.getKey());
                    }
                }

                // If no receiver IDs to fetch, update the adapter immediately
                if (receiverIdsToFetchNames.isEmpty()) {
                    Log.d(TAG, "No receiver IDs found in scheduled messages. Updating adapter without fetching names.");
                    // Sort messages by scheduled time before updating adapter
                    Collections.sort(tempList, Comparator.comparingLong(ScheMsg::getScheduledTimeMillis));
                    scheduledMessages.clear();
                    scheduledMessages.addAll(tempList); // Add messages without names fetched yet
                    adapter.setScheduledMessages(scheduledMessages);
                    tvNoDrafts.setVisibility(scheduledMessages.isEmpty() ? View.VISIBLE : View.GONE);
                    return;
                }

                Log.d(TAG, "Fetching names for " + receiverIdsToFetchNames.size() + " unique receiver IDs.");
                // Fetch all required user models ONCE
                fetchUserModels(receiverIdsToFetchNames, new UserModelsFetchCallback() {
                    @Override
                    public void onUserModelsFetched(List<UserModel> userModels) {
                        Log.d(TAG, "User models fetched for " + userModels.size() + " receivers.");
                        // Create a map from user ID to username for quick lookup
                        Map<String, String> userIdToNameMap = new HashMap<>();
                        if (userModels != null) {
                            for (UserModel user : userModels) {
                                if (!TextUtils.isEmpty(user.getUserId()) && !TextUtils.isEmpty(user.getUsername())) {
                                    userIdToNameMap.put(user.getUserId(), user.getUsername());
                                }
                            }
                        }
                        Log.d(TAG, "Created User ID to Name map with " + userIdToNameMap.size() + " entries.");

                        // Second pass: Populate receiver names in the ScheMsg objects using the map
                        for (ScheMsg message : tempList) {
                            List<String> currentReceiverNames = new ArrayList<>();
                            List<String> msgReceiverIds = message.getReceiverIds();
                            if (msgReceiverIds != null) {
                                for (String receiverId : msgReceiverIds) {
                                    String name = userIdToNameMap.get(receiverId);
                                    currentReceiverNames.add(name != null ? name : "Unknown"); // Use "Unknown" if name not found
                                }
                            }
                            message.setReceiverNames(currentReceiverNames); // This also updates receiverNamesStr
                        }

                        // Sort messages by scheduled time before updating adapter
                        Collections.sort(tempList, Comparator.comparingLong(ScheMsg::getScheduledTimeMillis));

                        // Update the main list and notify the adapter
//                        scheduledMessages.clear();
//                        scheduledMessages.addAll(tempList);
//                        adapter.setScheduledMessages(scheduledMessages);


                        adapter.setScheduledMessages(tempList);
                        
                        tvNoDrafts.setVisibility(scheduledMessages.isEmpty() ? View.VISIBLE : View.GONE);

                        Log.d(TAG, "Adapter updated with " + scheduledMessages.size() + " scheduled messages with names.");
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        Log.e(TAG, "Error fetching user models for scheduled messages: " + error.getMessage(), error.toException());
                        // Update adapter with messages without names, or clear list depending on desired error handling
                        // For now, update with potentially missing names
                        // Sort messages by scheduled time before updating adapter
                        Collections.sort(tempList, Comparator.comparingLong(ScheMsg::getScheduledTimeMillis));
                        scheduledMessages.clear();
                        scheduledMessages.addAll(tempList);
                        adapter.setScheduledMessages(scheduledMessages);
                        tvNoDrafts.setVisibility(scheduledMessages.isEmpty() ? View.VISIBLE : View.GONE);
                        Toast.makeText(DraftScheduled.this, "Error loading receiver names.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase listener cancelled for scheduled messages: " + error.getMessage(), error.toException());
                Toast.makeText(DraftScheduled.this, "Failed to load scheduled messages.", Toast.LENGTH_SHORT).show();
                scheduledMessages.clear();
                adapter.setScheduledMessages(scheduledMessages); // Clear list on cancellation
                tvNoDrafts.setText("Error loading scheduled messages.");
                tvNoDrafts.setVisibility(View.VISIBLE);
            }
        };

        scheduledMessagesQuery.addValueEventListener(scheduledMessagesListener); // Attach the listener
    }

    // Helper method to fetch multiple UserModels given a list of UIDs
    private void fetchUserModels(List<String> userIds, UserModelsFetchCallback callback) {
        if (userIds == null || userIds.isEmpty() || callback == null) {
            if (callback != null) callback.onUserModelsFetched(new ArrayList<>());
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        List<UserModel> userModels = new ArrayList<>();
        int totalUsers = userIds.size();
        final int[] processedCount = {0};
        final boolean[] hasError = {false};

        for (String userId : userIds) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            user.setUserId(snapshot.getKey()); // Ensure userId is set
                            userModels.add(user);
                        } else {
                            Log.w(TAG, "User model null for ID: " + userId + " during batch fetch.");
                        }
                    } else {
                        Log.w(TAG, "User data not found for ID: " + userId + " during batch fetch.");
                    }
                    processedCount[0]++;
                    if (processedCount[0] == totalUsers && !hasError[0]) {
                        callback.onUserModelsFetched(userModels);
                    } else if (processedCount[0] == totalUsers && hasError[0]) {
                        // If there was an error, the error callback was already called,
                        // but we still need to potentially call the success callback with partial data
                        // depending on desired error handling behavior.
                        // For now, we let the error callback handle the failure scenario.
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to fetch user model for ID: " + userId + " during batch fetch: " + error.getMessage(), error.toException());
                    if (!hasError[0]) { // Only call error callback once
                        hasError[0] = true;
                        callback.onError(error);
                    }
                    processedCount[0]++;
                    // Even on error, we continue processing other users.
                }
            });
        }
    }

    // Callback interface for fetching multiple UserModels
    private interface UserModelsFetchCallback {
        void onUserModelsFetched(List<UserModel> userModels);
        void onError(DatabaseError error);
    }


    // Method to delete a scheduled message from Firebase
    private void deleteScheduledMessage(ScheMsg message) {
        if (message == null || TextUtils.isEmpty(message.getMsgFirebaseId())) {
            Log.e(TAG, "Cannot delete scheduled message: message or message ID is null/empty.");
            Toast.makeText(this, "Error deleting scheduled message.", Toast.LENGTH_SHORT).show();
            return;
        }

        String msgId = message.getMsgFirebaseId();
        Log.d(TAG, "Attempting to delete scheduled message from Firebase: " + msgId);

        scheduledMessagesRef.child(msgId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Scheduled message deleted successfully from Firebase: " + msgId);
                    Toast.makeText(this, "Scheduled message deleted.", Toast.LENGTH_SHORT).show();
                    // The ValueEventListener will automatically update the list
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete scheduled message from Firebase: " + msgId, e);
                    Toast.makeText(this, "Failed to delete scheduled message.", Toast.LENGTH_SHORT).show();
                });

        // Optional: Also cancel the corresponding WorkManager task
        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(msgId);
        Log.d(TAG, "Attempted to cancel WorkManager task with tag: " + msgId);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Removing Firebase listener.");
        if (scheduledMessagesRef != null && scheduledMessagesListener != null) {
            scheduledMessagesRef.removeEventListener(scheduledMessagesListener);
        }
    }
}