package com.sana.circleup;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sana.circleup.room_db_implement.ChatDatabase;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GroupSettingsActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView groupProfileImage, deleteGroupProfileImage;
    private EditText groupNameEditText;
    private Button saveGroupNameButton;
    private RecyclerView groupMembersRecycler;
    private DatabaseReference groupsRef;
    private FirebaseAuth auth;
    private String groupId, currentUserId, adminId;
    private List<UserModel> groupMembersList = new ArrayList<>();
    private String base64GroupImage;

    private GroupMembersAdapter adapter;
    private ImageView addMembersButton;
    // *** NEW: Activity Result Launcher for Add Members Selection ***
    private ActivityResultLauncher<Intent> addMembersLauncher;
    private ExecutorService databaseExecutor; // Use the shared executor from ChatDatabase for Room ops
// *** End NEW ***

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);

        groupProfileImage = findViewById(R.id.group_profile_image);
        deleteGroupProfileImage = findViewById(R.id.delete_group_profile_image);
        groupNameEditText = findViewById(R.id.groupname_edittext);
        saveGroupNameButton = findViewById(R.id.save_group_name_button);
        groupMembersRecycler = findViewById(R.id.group_members_recycler);
        addMembersButton = findViewById(R.id.add_members);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        groupId = getIntent().getStringExtra("groupId");

        if (groupId == null || groupId.isEmpty()) {
            Toast.makeText(this, "Error: Group ID is missing", Toast.LENGTH_SHORT).show();
            Log.e("GroupSettingsActivity", "groupId is NULL or EMPTY");
            finish(); // Close the activity
            return;
        } else {
            Log.d("GroupSettingsActivity", "Received groupId: " + groupId);
        }

        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);

        groupMembersRecycler.setLayoutManager(new LinearLayoutManager(this));
// Initialize Room DB and DAO (NEW)
         // Get the DAO for group messages
        databaseExecutor = ChatDatabase.databaseWriteExecutor; // Use the shared DB executor for Room ops



        // --- NEW: Register Activity Result Launcher for adding members ---
        addMembersLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Data received from the Add Members selection activity
                        Intent data = result.getData();
                        ArrayList<String> selectedUserIdsToAdd = data.getStringArrayListExtra("selectedUserIds"); // Assuming key matches
                        // Optional: Get names if needed for logs/UI feedback
                        // ArrayList<String> selectedUserNamesToAdd = data.getStringArrayListExtra("selectedUserNames");

                        if (selectedUserIdsToAdd != null && !selectedUserIdsToAdd.isEmpty()) {
                            Log.d(TAG, "Received " + selectedUserIdsToAdd.size() + " selected user IDs to add to group.");
                            addSelectedMembersToGroup(selectedUserIdsToAdd); // Call the new method to add members
                        } else {
                            Log.d(TAG, "Add members selection activity returned OK but no user IDs were selected.");
                            Toast.makeText(GroupSettingsActivity.this, "No new members selected.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // User cancelled or selection failed
                        Log.d(TAG, "Add members selection activity cancelled or failed.");
                        // Optional: Show a toast indicating cancellation
                        Toast.makeText(GroupSettingsActivity.this, "Adding members cancelled.", Toast.LENGTH_SHORT).show();
                    }
                });
        // --- End NEW Launcher Registration ---




        loadGroupDetails();

//        saveGroupNameButton.setOnClickListener(v -> saveGroupName());




        // --- NEW: Set listener for Add Members Button ---
        if (addMembersButton != null) { // Check if the button was found
            addMembersButton.setOnClickListener(v -> {
                Log.d(TAG, "Add Members button clicked.");
                // Check if the current user is the admin before allowing to add members
                if (currentUserId != null && adminId != null && currentUserId.equals(adminId)) {
                    Log.d(TAG, "Current user is admin. Launching contact selection for adding members.");
                    // Launch the contact selection activity (we'll create/modify one next)
                    launchAddMembersSelection(); // Call the new method to launch selection
                } else {
                    Log.w(TAG, "Current user is not admin. Cannot add members.");
                    Toast.makeText(GroupSettingsActivity.this, "Only admins can add members.", Toast.LENGTH_SHORT).show();
                    // Optional: Hide the add members button if not admin (handled visually)
                }
            });
        } else {
            Log.w(TAG, "Add Members button not found in layout.");
        }
        // --- End NEW Listener



        saveGroupNameButton.setOnClickListener(v -> {
            String newName = groupNameEditText.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save group name
            Task<Void> nameTask = groupsRef.child("groupName").setValue(newName);

            // Save group image only if updated
            Task<Void> imageTask;
            if (base64GroupImage != null) {
                imageTask = groupsRef.child("groupImage").setValue(base64GroupImage);
            } else {
                imageTask = Tasks.forResult(null); // No update needed
            }

            // Wait for both tasks to complete
            Tasks.whenAllComplete(nameTask, imageTask).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to chat screen
                } else {
                    Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show();
                }
            });
        });

        groupProfileImage.setOnClickListener(v -> openImagePicker());
        deleteGroupProfileImage.setOnClickListener(v -> deleteGroupImage());




        adapter = new GroupMembersAdapter(this, groupMembersList, adminId, currentUserId, user -> showMemberOptions(user));
        groupMembersRecycler.setAdapter(adapter);


    }

    private void loadGroupDetails() {
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                adminId = snapshot.child("admin").getValue(String.class);

                // Load group name
                String name = snapshot.child("groupName").getValue(String.class);
                groupNameEditText.setText(name);

                // Load group image
                String imageBase64 = snapshot.child("groupImage").getValue(String.class);
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    groupProfileImage.setImageBitmap(decodedBitmap);
                } else {
                    groupProfileImage.setImageResource(R.drawable.photocameradefault);
                }

                // Load group members
                groupMembersList.clear();
                for (DataSnapshot ds : snapshot.child("members").getChildren()) {
                    String userId = ds.getKey(); // Member userId

                    fetchUserDetails(userId); // Fetch from "Users" node
                }

                // Set up adapter AFTER fetching admin ID
                adapter = new GroupMembersAdapter(GroupSettingsActivity.this, groupMembersList, adminId, currentUserId, user -> showMemberOptions(user));
                groupMembersRecycler.setAdapter(adapter);



                // *** NEW: Set visibility of Add Members button ***
                Log.d(TAG, "Admin ID fetched: " + adminId + ". Current User ID: " + currentUserId + ". Setting Add Members button visibility.");
                if (addMembersButton != null) { // Ensure button is initialized
                    if (currentUserId != null && adminId != null && currentUserId.equals(adminId)) {
                        // Current user is the admin, show the add members button
                        addMembersButton.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Current user is admin. Showing Add Members button.");
                    } else {
                        // Current user is NOT the admin, hide the add members button
                        addMembersButton.setVisibility(View.GONE);
                        Log.d(TAG, "Current user is not admin. Hiding Add Members button.");
                    }
                } else {
                    Log.w(TAG, "addMembersButton is null in loadGroupDetails.");
                }
                // *** End NEW ***

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GroupSettings", "Error fetching group details: " + error.getMessage());
            }
        });
    }



    private void fetchUserDetails(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        Log.d("GroupSettings", "Fetching user details for ID: " + userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    String name = userSnapshot.child("username").getValue(String.class);
                    String profileImage = userSnapshot.child("profileImage").getValue(String.class);

                    Log.d("GroupSettings", "User found: " + name + ", Image: " + profileImage);

                    UserModel user = new UserModel(userId, name, profileImage);
                    groupMembersList.add(user);


                    // Ensure adapter updates UI
                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } else {
                    Log.e("GroupSettings", "User not found in Users node for ID: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GroupSettings", "Error fetching user details: " + error.getMessage());
            }
        });
    }





    private void saveGroupName() {
        String newName = groupNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        groupsRef.child("groupName").setValue(newName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Group name updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                groupProfileImage.setImageBitmap(bitmap);
                base64GroupImage = encodeToBase64(bitmap);
                saveGroupImageToFirebase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveGroupImageToFirebase() {
        groupsRef.child("groupImage").setValue(base64GroupImage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Group photo updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update group photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroupImage() {
        groupsRef.child("groupImage").removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                groupProfileImage.setImageResource(R.drawable.photocameradefault);
                Toast.makeText(this, "Group photo removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to remove group photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupMembers() {
        groupsRef.child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupMembersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String userId = ds.getKey();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String name = userSnapshot.child("username").getValue(String.class);
                                String profileImage = userSnapshot.child("profileImage").getValue(String.class);

                                UserModel user = new UserModel(userId, name, profileImage);
                                if (userId.equals(adminId)) {
                                    groupMembersList.add(0, user); // Admin at top
                                } else {
                                    groupMembersList.add(user);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void showMemberOptions(UserModel user) {
        // If the current user is NOT the admin, do nothing
        if (!currentUserId.equals(adminId)) return;

        // If the selected user is already the admin, do nothing
        if (user.getUserId().equals(adminId)) {
            Toast.makeText(this, "You cannot modify the admin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show options dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage Member")
                .setItems(new String[]{"Remove Member", "Make Admin"}, (dialog, which) -> {
                    if (which == 0) {
                        removeMember(user);
                    } else if (which == 1) {
                        makeAdmin(user);
                    }
                })
                .show();
    }

    private void removeMember(UserModel user) {
        groupsRef.child("members").child(user.getUserId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Member removed", Toast.LENGTH_SHORT).show();
                groupMembersList.remove(user);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to remove member", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void makeAdmin(UserModel user) {
        groupsRef.child("admin").setValue(user.getUserId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "New admin assigned", Toast.LENGTH_SHORT).show();
                adminId = user.getUserId();  // Update locally
                loadGroupMembers();  // Refresh members list
            } else {
                Toast.makeText(this, "Failed to assign admin", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void launchAddMembersSelection() {
        if (this == null || groupId == null || groupId.isEmpty() || currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "Cannot launch add members selection: Missing essential info.");
            Toast.makeText(this, "Error launching selection.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the list of *current* group member UIDs to exclude from the selection list.
        // The GroupMembersAdapter list (groupMembersList) holds UserModels of current members.
        List<String> currentMemberUids = new ArrayList<>();
        for (UserModel member : groupMembersList) { // Use the list populated by loadGroupDetails
            if (member != null && !TextUtils.isEmpty(member.getUserId())) {
                currentMemberUids.add(member.getUserId());
            }
        }
        Log.d(TAG, "Preparing add members selection. Excluding " + currentMemberUids.size() + " current members.");

        // --- Launch the activity to select contacts ---
        // You need an activity that can list the current user's contacts
        // and allows multi-selection, while EXCLUDING the UIDs in currentMemberUids.
        // This could be a modified version of CreateGroupActivity or ScheduledRecipientSelectionActivity,
        // or a completely new activity.
        // Let's assume you create a new activity specifically for this, e.g., AddGroupMembersActivity.
        // This new activity needs to:
        // 1. Receive the list of currentMemberUids to exclude.
        // 2. Fetch the current user's *accepted* contacts.
        // 3. Display the contacts, EXCLUDING those in the exclusion list.
        // 4. Allow multi-selection using checkboxes.
        // 5. Return the list of *newly selected* user IDs (the ones not previously in the group) via Activity Result.

        Intent intent = new Intent(GroupSettingsActivity.this, AddGroupMembersActivity.class); // <<< Replace with your actual Activity class for adding members
        intent.putStringArrayListExtra("existingMemberUids", new ArrayList<>(currentMemberUids)); // Pass UIDs to exclude
        // Optional: Pass group ID or name if needed in the selection activity
        // intent.putExtra("groupId", groupId);
        // intent.putExtra("groupName", groupName); // Assuming groupName member is populated

        Log.d(TAG, "Launching AddGroupMembersActivity with exclusion list.");
        // Launch the activity using the registered launcher
        addMembersLauncher.launch(intent);
        // --- End Launch ---
    }


    private void addSelectedMembersToGroup(List<String> selectedUserIdsToAdd) {
        if (groupsRef == null || groupId == null || groupId.isEmpty() || selectedUserIdsToAdd == null || selectedUserIdsToAdd.isEmpty() || currentUserId == null || currentUserId.isEmpty() || databaseExecutor == null || adapter == null) { // Added checks
            Log.e(TAG, "Cannot add selected members: Missing essential info.");
            Toast.makeText(this, "Error adding members.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Attempting to add " + selectedUserIdsToAdd.size() + " new members to group: " + groupId);

        DatabaseReference membersRef = groupsRef.child("members");

        Map<String, Object> memberUpdates = new HashMap<>();
        // We need to fetch the usernames for the newly selected members to store in Firebase
        // This requires fetching from /Users for each selected UID.

        // Use a CountDownLatch to wait for user details fetches for the new members
        CountDownLatch latch = new CountDownLatch(selectedUserIdsToAdd.size());
        List<UserModel> newMemberUserModels = new ArrayList<>(); // To store the fetched UserModel data

        Log.d(TAG, "Fetching details for " + selectedUserIdsToAdd.size() + " new members.");

        // Fetch user details for each selected user ID on a background thread
        databaseExecutor.execute(() -> { // Use the shared executor
            for (String userIdToAdd : selectedUserIdsToAdd) {
                if (TextUtils.isEmpty(userIdToAdd)) {
                    Log.w(TAG, "Skipping adding empty user ID to group.");
                    latch.countDown(); // Countdown even for empty ID
                    continue;
                }
                // Fetch user details from /Users
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userIdToAdd);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        if (userSnapshot.exists()) {
                            UserModel user = userSnapshot.getValue(UserModel.class);
                            if (user != null) {
                                user.setUserId(userSnapshot.getKey()); // Ensure UID is set
                                newMemberUserModels.add(user); // Add to our list of new member models
                                Log.d(TAG, "Fetched details for potential new member: " + user.getUsername());
                            } else {
                                Log.w(TAG, "UserModel is null for potential new member ID: " + userIdToAdd);
                            }
                        } else {
                            Log.w(TAG, "User data not found for potential new member ID: " + userIdToAdd);
                        }
                        latch.countDown(); // Signal fetch complete for this user
                        Log.d(TAG, "fetch user details latch countdown. Count: " + latch.getCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch user details for potential new member ID " + userIdToAdd + ": " + error.getMessage());
                        latch.countDown(); // Signal fetch complete (with error)
                        Log.d(TAG, "fetch user details latch countdown (cancelled). Count: " + latch.getCount());
                    }
                });
            }

            // Wait for all user details fetches to complete
            try {
                latch.await(15, TimeUnit.SECONDS); // Wait with a timeout
                Log.d(TAG, "Finished fetching details for all potential new members.");

                // Prepare the Firebase update map with the fetched details
                for (UserModel user : newMemberUserModels) {
                    if (user != null && !TextUtils.isEmpty(user.getUserId())) {
                        HashMap<String, Object> memberData = new HashMap<>();
                        memberData.put("role", "member"); // New members are typically "member" role
                        memberData.put("joinedAt", System.currentTimeMillis());
                        memberData.put("name", user.getUsername() != null ? user.getUsername() : "Unknown"); // Use fetched name
                        memberUpdates.put(user.getUserId(), memberData);
                        Log.d(TAG, "Prepared Firebase update for new member: " + user.getUserId());
                    } else {
                        Log.w(TAG, "Skipping preparing update for invalid UserModel in newMemberUserModels list.");
                    }
                }
                Log.d(TAG, "Finished preparing Firebase member updates map. Updates count: " + memberUpdates.size());


                // --- Write the member updates to Firebase ---
                if (!memberUpdates.isEmpty()) {
                    // Run Firebase write on the main thread (or handle success/failure on main thread)
                    // For simplicity in handling UI updates, let's run the updateChildren on the main thread.
                    // Or use Tasks.await for sync write in worker (but this is Activity, not worker).
                    // Let's use addOnCompleteListener and handle success/failure on main thread.
                    runOnUiThread(() -> { // Switch back to main thread for Firebase write initiation and UI updates
                        Log.d(TAG, "Executing Firebase updateChildren for adding members on main thread.");
                        membersRef.updateChildren(memberUpdates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully added new members to group in Firebase.");
                                    Toast.makeText(GroupSettingsActivity.this, "Members added successfully!", Toast.LENGTH_SHORT).show();
                                    // Refresh the members list displayed in the RecyclerView
                                    loadGroupMembers(); // Reload data and update adapter
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to add new members to group in Firebase.", e);
                                    Toast.makeText(GroupSettingsActivity.this, "Failed to add members.", Toast.LENGTH_SHORT).show();
                                });
                    });

                } else {
                    Log.d(TAG, "No valid users to add. Skipping Firebase update.");
                    runOnUiThread(() -> Toast.makeText(GroupSettingsActivity.this, "No new members to add.", Toast.LENGTH_SHORT).show());
                }


            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted or timed out while fetching user details for new members.", e);
                runOnUiThread(() -> Toast.makeText(GroupSettingsActivity.this, "Error fetching member details.", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e(TAG, "An unexpected error occurred while adding members.", e);
                runOnUiThread(() -> Toast.makeText(GroupSettingsActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show());
            }
        });
    }
// --- End NEW Method addSelectedMembersToGroup ---


}
