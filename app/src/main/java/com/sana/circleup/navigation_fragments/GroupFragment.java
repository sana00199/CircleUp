package com.sana.circleup.navigation_fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.sana.circleup.CreateGroupActivity;
import com.sana.circleup.Group; // Your Group model
import com.sana.circleup.GroupListAdapter; // Your adapter
import com.sana.circleup.GroupChatActivity; // *** Replace with your actual Group Chat Activity ***
import com.sana.circleup.temporary_chat_room.TemporaryRoomChatActivity; // *** Replace with your actual Temporary Room Chat Activity ***


import com.sana.circleup.R;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.GroupEntity; // Room Entities
import com.sana.circleup.room_db_implement.TemporaryRoomEntity;
import com.sana.circleup.room_db_implement.GroupListDao; // Room DAO
import com.sana.circleup.temporary_chat_room.TemporaryChatRoomMain;
import com.sana.circleup.temporary_chat_room.TemporaryIChatListItemGroup; // Interface
import com.sana.circleup.temporary_chat_room.TemporaryRoom; // Your TemporaryRoom model


import java.util.ArrayList;
import java.util.Collections; // Import Collections
import java.util.Comparator; // Import Comparator
import java.util.List;
import java.util.concurrent.ExecutorService;








import android.app.ProgressDialog; // For showing progress during deletion

// Make Fragment implement the new OnItemLongClickListener interface
public class GroupFragment extends Fragment implements GroupListAdapter.OnItemClickListener, GroupListAdapter.OnItemLongClickListener {

    private static final String TAG = "GroupFragment";

    private View groupFragmentView;

    private ValueEventListener groupsListener;
    private ValueEventListener tempRoomsListener;

    private RecyclerView groupRecyclerView;
    private GroupListAdapter chatListAdapter;
    private TextView noChatsText;
    private FloatingActionButton fabCreateGroup;

    private DatabaseReference rootRef;
    private FirebaseAuth auth;
    private String currentUserId;

    private ChatDatabase chatDatabase;
    private GroupListDao groupListDao;
    private ExecutorService databaseExecutor;

    private LiveData<List<GroupEntity>> groupsLiveData;
    private LiveData<List<TemporaryRoomEntity>> temporaryRoomsLiveData;
    private final MediatorLiveData<List<TemporaryIChatListItemGroup>> combinedGroupListLiveData = new MediatorLiveData<>();

    // --- NEW: Progress Dialog for Deletion ---
    private ProgressDialog loadingBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            rootRef = FirebaseDatabase.getInstance().getReference();
            Log.d(TAG, "Current User ID: " + currentUserId);
        } else {
            currentUserId = null;
            Log.e(TAG, "User not authenticated in GroupFragment!");
        }

        chatDatabase = ChatDatabase.getInstance(requireContext());
        groupListDao = chatDatabase.groupListDao();
        databaseExecutor = ChatDatabase.databaseWriteExecutor;

        if (currentUserId != null) {
            groupsLiveData = groupListDao.getAllGroupsForOwner(currentUserId);
            temporaryRoomsLiveData = groupListDao.getAllTemporaryRoomsForOwner(currentUserId);

            combinedGroupListLiveData.addSource(groupsLiveData, groupEntities -> {
                Log.d(TAG, "groupsLiveData changed. Combining...");
                combineAndSortGroupList(groupEntities, temporaryRoomsLiveData.getValue());
            });

            combinedGroupListLiveData.addSource(temporaryRoomsLiveData, roomEntities -> {
                Log.d(TAG, "temporaryRoomsLiveData changed. Combining...");
                combineAndSortGroupList(groupsLiveData.getValue(), roomEntities);
            });
        } else {
            Log.e(TAG, "currentUserID is null, skipping Room LiveData observation setup.");
        }

        // Initialize Progress Dialog
        loadingBar = new ProgressDialog(getContext());
        loadingBar.setMessage("Deleting group...");
        loadingBar.setCanceledOnTouchOutside(false); // Prevent dismissal by tapping outside
        loadingBar.setCancelable(false); // Prevent dismissal by back button
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_group, container, false);

        InitializeFields();

        chatListAdapter.setOnItemClickListener(this);
        // --- NEW: Set the long click listener ---
        chatListAdapter.setOnItemLongClickListener(this);

        fabCreateGroup.setOnClickListener(v -> showCreateChatTypeDialog());

        return groupFragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUserId == null) {
            Log.e(TAG, "Cannot proceed without currentUserID in onViewCreated.");
            noChatsText.setVisibility(View.VISIBLE);
            noChatsText.setText("Authentication Error");
            groupRecyclerView.setVisibility(View.GONE);
            fabCreateGroup.setVisibility(View.GONE);
            return;
        }

        combinedGroupListLiveData.observe(getViewLifecycleOwner(), groupList -> {
            Log.d(TAG, "Combined LiveData onChanged triggered. Received " + (groupList != null ? groupList.size() : 0) + " items from Room.");
            if (groupList != null && !groupList.isEmpty()) {
                chatListAdapter.submitList(groupList);
                groupRecyclerView.setVisibility(View.VISIBLE);
                noChatsText.setVisibility(View.GONE);
                fabCreateGroup.setVisibility(View.VISIBLE);
            } else {
                chatListAdapter.submitList(new ArrayList<>());
                groupRecyclerView.setVisibility(View.GONE);
                noChatsText.setVisibility(View.VISIBLE);
                noChatsText.setText("No groups or temporary rooms yet");
                fabCreateGroup.setVisibility(View.VISIBLE);
                Log.d(TAG, "No groups or temporary rooms found in Room for user: " + currentUserId);
            }
        });

        // Start Firebase Sync
        if (rootRef != null) {
            Log.d(TAG, "Starting Firebase sync for Groups and Temporary Rooms for user: " + currentUserId);
            // Assign listeners
            groupsListener = rootRef.child("Groups").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Firebase Group data received. Processing...");
                    processAndSaveGroupsToRoom(snapshot, currentUserId);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load regular groups from Firebase: " + error.getMessage());
                    if(getContext() != null) {
                        Toast.makeText(getContext(), "Failed to sync groups: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            tempRoomsListener = rootRef.child("temporaryChatRooms").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Firebase Temporary Room data received. Processing...");
                    processAndSaveTemporaryRoomsToRoom(snapshot, currentUserId);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load temporary rooms from Firebase: " + error.getMessage());
                    if(getContext() != null) {
                        Toast.makeText(getContext(), "Failed to sync temporary rooms: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Log.e(TAG, "rootRef is null, cannot start Firebase sync.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Removing Firebase listeners.");
        if (rootRef != null) {
            if (groupsListener != null) {
                rootRef.child("Groups").removeEventListener(groupsListener);
                Log.d(TAG, "Removed Groups ValueEventListener");
            }
            if (tempRoomsListener != null) {
                rootRef.child("temporaryChatRooms").removeEventListener(tempRoomsListener);
                Log.d(TAG, "Removed TemporaryRooms ValueEventListener");
            }
        }
        Log.d(TAG, "onDestroyView finished.");
    }


    private void InitializeFields() {
        groupRecyclerView = groupFragmentView.findViewById(R.id.group_recycler_view);
        noChatsText = groupFragmentView.findViewById(R.id.noChatsText);
        fabCreateGroup = groupFragmentView.findViewById(R.id.fab_create_group);

        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatListAdapter = new GroupListAdapter(getContext(), new ArrayList<>());
        groupRecyclerView.setAdapter(chatListAdapter);
    }

    private void showCreateChatTypeDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create New Chat");
        String[] types = {"New Group", "New Temporary Room"};

        builder.setItems(types, (dialog, which) -> {
            if (which == 0) { // New Group
                Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
                startActivity(intent);
            } else if (which == 1) { // New Temporary Room
                Intent intent = new Intent(getActivity(), TemporaryChatRoomMain.class); // Check if this is correct Activity
                startActivity(intent);
            }
        });
        builder.show();
    }

    // --- Helper method to combine and sort lists ---
    private void combineAndSortGroupList(@Nullable List<GroupEntity> groupEntities, @Nullable List<TemporaryRoomEntity> roomEntities) {
        List<TemporaryIChatListItemGroup> combinedList = new ArrayList<>();

        if (groupEntities != null) {
            for (GroupEntity entity : groupEntities) {
                // Check if the group is still valid (e.g., if it wasn't properly removed from Room)
                // Although Firebase sync should handle this, an extra check doesn't hurt.
                combinedList.add(new Group(entity)); // Assumes Group model has constructor from Entity
            }
        }

        if (roomEntities != null) {
            long currentTime = System.currentTimeMillis();
            for (TemporaryRoomEntity entity : roomEntities) {
                if (entity.getExpiryTime() == null || currentTime <= entity.getExpiryTime()) {
                    combinedList.add(new TemporaryRoom(entity)); // Assumes TemporaryRoom model has constructor from Entity
                } else {
                    // Delete expired rooms from Room
                    String expiredRoomId = entity.getRoomId();
                    String ownerId = entity.getOwnerUserId();
                    Log.d(TAG, "Found expired temporary room in Room, deleting: " + expiredRoomId);
                    databaseExecutor.execute(() -> {
                        try {
                            groupListDao.deleteTemporaryRoomForOwner(expiredRoomId, ownerId);
                            Log.d(TAG, "Deleted expired temporary room from Room: " + expiredRoomId);
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting expired temporary room from Room: " + expiredRoomId, e);
                        }
                    });
                }
            }
        }

        Collections.sort(combinedList, Comparator.comparingLong(TemporaryIChatListItemGroup::getSortingTimestamp).reversed());

        combinedGroupListLiveData.setValue(combinedList);
    }

    // --- Firebase Sync Methods (Now saves to Room) ---
    // (Keep these methods as they are triggered by the Firebase listeners)

    // Make sure you have this line at the top of your class, using the custom tag:


    // Inside GroupFragment.java

    private void processAndSaveGroupsToRoom(DataSnapshot snapshot, String ownerUserId) {
        databaseExecutor.execute(() -> {
            Log.d(TAG, "--- START: processAndSaveGroupsToRoom for owner: " + ownerUserId + " ---");

            if (snapshot == null) {
                Log.e(TAG, "Firebase snapshot is NULL in processAndSaveGroupsToRoom!");
                return; // Exit if snapshot is null
            }
            Log.d(TAG, "Firebase snapshot received with " + snapshot.getChildrenCount() + " top-level children (groups/rooms?).");

            try {
                // --- REMOVE THIS LIST --- We no longer use this for GroupEntity cleanup based on Firebase presence
                // List<String> presentGroupIdsInFirebase = new ArrayList<>();

                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey();
                    if (groupId == null) {
                        Log.w(TAG, "Skipping group with null ID in snapshot.");
                        continue;
                    }
                    // Log.d(TAG, "Processing Group ID: " + groupId); // Too verbose inside loop

                    DataSnapshot membersSnapshot = groupSnapshot.child("members");
                    boolean isMember = membersSnapshot.hasChild(ownerUserId);
                    // Log.d(TAG, "  User " + ownerUserId + " is member: " + isMember + " of Group " + groupId); // Too verbose inside loop

                    // --- KEEP the logic for members, process and save the GroupEntity ---
                    if (isMember) {
                        // --- REMOVE THIS LINE --- It was only used for the cleanup loop we are removing
                        // presentGroupIdsInFirebase.add(groupId);

                        String groupName = groupSnapshot.child("groupName").getValue(String.class);
                        String groupImageBase64 = groupSnapshot.child("groupImage").getValue(String.class);
                        // Log.d(TAG, "  Group Name: " + groupName + ", Image Data Found: " + (groupImageBase64 != null && !groupImageBase64.isEmpty()) + " for Group " + groupId); // Too verbose

                        // --- Process Messages to find latest and check unread status ---
                        // (Keep this logic as it calculates info needed for the GroupEntity)
                        String lastMessageText = "";
                        long lastMessageTimestamp = 0L;
                        boolean hasUnreadMessages = false;
                        DataSnapshot latestMessageSnap = null;
                        long highestMessageTimestamp = 0L;
                        String latestMessageType = null;

                        DataSnapshot messagesSnapshot = groupSnapshot.child("Messages");
                        // Log.d(TAG, "  Group " + groupId + " checking 'Messages' child. Exists: " + messagesSnapshot.exists() + ", Has Children: " + messagesSnapshot.hasChildren() + ", Children Count: " + messagesSnapshot.getChildrenCount()); // Too verbose

                        if (messagesSnapshot.exists() && messagesSnapshot.hasChildren()) {
                            for (DataSnapshot messageSnap : messagesSnapshot.getChildren()) {
                                String messageKey = messageSnap.getKey();
                                Long messageTimestamp = messageSnap.child("timestamp").getValue(Long.class);
                                String messageType = messageSnap.child("type").getValue(String.class);

                                // Log.d(TAG, "    Processing Message: " + messageKey + " in Group " + groupId + ", Firebase Timestamp: " + messageTimestamp + ", Type: " + messageType + ", Message Exists: " + messageSnap.exists()); // Too verbose

                                if (messageTimestamp != null) {
                                    if (messageTimestamp > highestMessageTimestamp) {
                                        highestMessageTimestamp = messageTimestamp;
                                        latestMessageSnap = messageSnap;
                                        latestMessageType = messageType;
                                        // Log.d(TAG, "      Found NEW latest message candidate: " + messageKey + " with timestamp: " + highestMessageTimestamp + ", Type: " + latestMessageType + " in Group " + groupId); // Too verbose
                                    }

                                    DataSnapshot readBySnapshot = messageSnap.child("readBy");
                                    boolean isReadByCurrentUser = readBySnapshot.exists() &&
                                            readBySnapshot.hasChild(ownerUserId) &&
                                            Boolean.TRUE.equals(readBySnapshot.child(ownerUserId).getValue(Boolean.class));
                                    // Log.d(TAG, "      Message " + messageKey + " in Group " + groupId + " read by " + ownerUserId + ": " + isReadByCurrentUser); // Too verbose

                                    if (!isReadByCurrentUser) {
                                        hasUnreadMessages = true;
                                        // Log.d(TAG, "      Message " + messageKey + " is UNREAD for " + ownerUserId + " in Group " + groupId + ". Setting group's hasUnreadMessages = true."); // Too verbose
                                    }
                                } else {
                                    Log.w(TAG, "      Message " + messageKey + " in Group " + groupId + " has NULL timestamp or timestamp field missing. Skipping.");
                                }
                            }

                            // Apply the logic AFTER finding the latest message
                            if (latestMessageSnap != null) {
                                lastMessageTimestamp = highestMessageTimestamp;

                                if ("image".equals(latestMessageType)) {
                                    lastMessageText = "\uD83D\uDCF8 Photo";
                                    // Log.d(TAG, "  Latest message is an image for Group " + groupId + ". Setting text to '" + lastMessageText + "'."); // Too verbose
                                } else {
                                    lastMessageText = latestMessageSnap.child("message").getValue(String.class);
                                    if (lastMessageText == null) lastMessageText = "[Empty Message]";
                                    // String loggableText = (lastMessageText.length() > 50) ? lastMessageText.substring(0, 50) + "..." : lastMessageText;
                                    // Log.d(TAG, "  Latest message FROM MESSAGES in Group " + groupId + " is type '" + latestMessageType + "'. Text='" + loggableText + "', Timestamp=" + lastMessageTimestamp); // Too verbose
                                }
                            } else {
                                // Log.d(TAG, "  Messages node existed and had children for Group " + groupId + ", but NO valid latest message FOUND."); // Too verbose
                                lastMessageText = "";
                                lastMessageTimestamp = 0L;
                            }
                        } else {
                            // Log.d(TAG, "  No messages found in Group " + groupId + " ('Messages' snapshot doesn't exist or is empty)."); // Too verbose
                            lastMessageText = "";
                            lastMessageTimestamp = 0L;
                        }
                        // --- End Message Processing ---

                        // Fallback to creation timestamp if still no message timestamp found
                        if (lastMessageTimestamp == 0L) {
                            Long creationTimestamp = groupSnapshot.child("createdAt").getValue(Long.class);
                            // Log.d(TAG, "  lastMessageTimestamp is 0 for Group " + groupId + ". Checking Group Creation Timestamp ('createdAt'): " + creationTimestamp); // Too verbose
                            if(creationTimestamp != null) {
                                lastMessageTimestamp = creationTimestamp;
                                // Log.d(TAG, "  Using Group Creation Timestamp for lastMessageTimestamp: " + lastMessageTimestamp + " for Group " + groupId); // Too verbose
                            } else {
                                lastMessageTimestamp = 0L; // Still 0 if no creation timestamp either
                                // Log.d(TAG, "  Group Creation Timestamp is NULL for Group " + groupId + ". Using fallback lastMessageTimestamp = 0."); // Too verbose
                            }
                        } else {
                            // Log.d(TAG, "  Last message found with timestamp > 0 for Group " + groupId + ". Using message timestamp for sorting."); // Too verbose
                        }


                        // --- Prepare and Save/Update GroupEntity to Room ---
                        // This part is ONLY reached if user IS a member.
                        // This saves the group info and updates latest message/unread status *while they are a member*.
                        GroupEntity groupEntity = new GroupEntity();
                        groupEntity.setGroupId(groupId);
                        groupEntity.setOwnerUserId(ownerUserId);
                        groupEntity.setGroupName(groupName != null ? groupName : "Unnamed Group");
                        groupEntity.setGroupImage(groupImageBase64 != null ? groupImageBase64 : "");
                        groupEntity.setLastMessageText(lastMessageText); // Set the text here
                        groupEntity.setLastMessageTimestamp(lastMessageTimestamp);
                        groupEntity.setHasUnreadMessages(hasUnreadMessages); // This reflects unread status *while they are a member*

                        groupListDao.insertOrUpdateGroup(groupEntity);
                        Log.d(TAG, "  Group Entity for member " + ownerUserId + " in group " + groupId + " inserted/updated successfully in Room.");

                    } else {
                        // --- REMOVE THIS ENTIRE ELSE BLOCK ---
                        // If user is NOT a member based on this snapshot, we DO NOT delete the GroupEntity from Room.
                        // The GroupEntity will persist in Room as long as the user has messages for this group locally,
                        // or until they manually clear the chat.
                        /*
                        // If user is NOT a member based on this snapshot, ensure it's deleted from Room
                        Log.d(TAG, "User " + ownerUserId + " is NOT member of group " + groupId + " in Firebase snapshot. Checking if exists in Room for deletion.");
                        // We check if the group existed in Room for this user before attempting to delete
                        boolean existsInRoom = groupListDao.getGroupById(groupId, ownerUserId) != null; // Requires getGroupById(id, ownerId) query in DAO
                        if (existsInRoom) {
                            groupListDao.deleteGroupForOwner(groupId, ownerUserId); // Ensure this query exists and works
                            Log.d(TAG, "User is no longer member of Group " + groupId + ". Deleted GroupEntity from Room.");
                        } else {
                            Log.d(TAG, "User is not member of Group " + groupId + " in Firebase snapshot, and it didn't exist in Room for owner " + ownerUserId + ". No deletion needed.");
                        }
                        */
                        // --- END REMOVE BLOCK ---
                    }
                }

                // --- REMOVE THIS ENTIRE CLEANUP BLOCK ---
                // We no longer delete GroupEntity objects from Room based on whether they are present in the Firebase snapshot.
                // The GroupEntity stays if the user has messages, even if not a member.
                /*
                // --- Cleanup: Delete groups from Room that are no longer found in Firebase for this user ---
                Log.d(TAG, "Starting cleanup: Checking for Group Entities in Room not present in current Firebase snapshot for owner: " + ownerUserId);
                List<String> groupIdsInRoom = groupListDao.getGroupIdsForOwner(ownerUserId); // Ensure this query exists and works

                if (groupIdsInRoom != null && !groupIdsInRoom.isEmpty()) {
                    List<String> idsToDelete = new ArrayList<>();
                    for (String groupIdInRoom : groupIdsInRoom) {
                        if (!presentGroupIdsInFirebase.contains(groupIdInRoom)) {
                            idsToDelete.add(groupIdInRoom);
                            Log.d(TAG, "  Found Group ID " + groupIdInRoom + " in Room for owner " + ownerUserId + " but NOT in current Firebase snapshot. Marking for deletion.");
                        }
                    }
                    if (!idsToDelete.isEmpty()) {
                        groupListDao.deleteGroupsByIdsForOwner(ownerUserId, idsToDelete); // Ensure this query exists and works
                        Log.d(TAG, "Cleaned up " + idsToDelete.size() + " group entities from Room that were not found in Firebase for owner " + ownerUserId + ".");
                    } else {
                        Log.d(TAG, "No group entities in Room for owner " + ownerUserId + " needed cleanup (all found in Firebase snapshot).");
                    }
                } else {
                    Log.d(TAG, "No group entities found in Room for owner " + ownerUserId + ". No cleanup needed.");
                }
                */
                // --- END REMOVE BLOCK ---

                Log.d(TAG, "--- END: processAndSaveGroupsToRoom for owner: " + ownerUserId + ". Processing complete. ---");

            } catch (Exception e) {
                Log.e(TAG, "FATAL ERROR in processAndSaveGroupsToRoom for owner: " + ownerUserId, e);
            }
        });
    }


    private void processAndSaveTemporaryRoomsToRoom(DataSnapshot snapshot, String ownerUserId) {
        databaseExecutor.execute(() -> {
            try {
                List<String> presentRoomIdsInFirebase = new ArrayList<>(); // List to track rooms found in Firebase

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) { // Loop through each temporary room node
                    String roomId = dataSnapshot.getKey();
                    if (roomId == null) continue;

                    DataSnapshot membersSnapshot = dataSnapshot.child("members");
                    Long expiryTimeMillis = dataSnapshot.child("expiryTime").getValue(Long.class);

                    // Fetch creation timestamp from Firebase
                    Long roomCreationTimestamp = dataSnapshot.child("timestamp").getValue(Long.class);

                    if (membersSnapshot.hasChild(ownerUserId) &&
                            (expiryTimeMillis == null || System.currentTimeMillis() <= expiryTimeMillis)) {

                        presentRoomIdsInFirebase.add(roomId);

                        String roomName = dataSnapshot.child("roomName").getValue(String.class);
                        String createdBy = dataSnapshot.child("createdBy").getValue(String.class);

                        // --- Logic to find latest message from 'messages' collection ---
                        String lastMessageText = "";
                        long latestMessageTimestamp = roomCreationTimestamp != null ? roomCreationTimestamp : 0; // Default to room creation time or 0
                        boolean hasUnreadMessages = false;

                        // --- NEW VARIABLES TO TRACK LATEST MESSAGE TYPE ---
                        DataSnapshot latestMessageSnap = null;
                        long highestMessageTimestamp = 0; // Find the actual latest message timestamp
                        String latestMessageType = null; // <-- NEW: Store the type of the latest message


                        DataSnapshot messagesSnapshot = dataSnapshot.child("messages");
                        if (messagesSnapshot.exists() && messagesSnapshot.hasChildren()) { // Added hasChildren check
                            for (DataSnapshot messageSnap : messagesSnapshot.getChildren()) { // Loop through each message
                                // Fetch message timestamp and type
                                Long messageTimestamp = messageSnap.child("timestamp").getValue(Long.class);
                                String messageType = messageSnap.child("type").getValue(String.class); // <-- NEW: Get the message type

                                if (messageTimestamp != null) {
                                    if (messageTimestamp > highestMessageTimestamp) {
                                        highestMessageTimestamp = messageTimestamp;
                                        latestMessageSnap = messageSnap;
                                        latestMessageType = messageType; // <-- NEW: Update latest type here
                                        // Log.d(TAG, "Found NEW latest message candidate: " + messageSnap.getKey() + " with timestamp: " + highestMessageTimestamp + ", Type: " + latestMessageType + " in Room " + roomId); // Too verbose
                                    }
                                    // Check unread status for the current user
                                    DataSnapshot readBySnapshot = messageSnap.child("readBy");
                                    // Check if 'readBy' exists, if current user's ID exists under 'readBy', and if its value is Boolean TRUE
                                    boolean isReadByCurrentUser = readBySnapshot.exists() &&
                                            readBySnapshot.hasChild(ownerUserId) &&
                                            Boolean.TRUE.equals(readBySnapshot.child(ownerUserId).getValue(Boolean.class));

                                    if (!isReadByCurrentUser) {
                                        hasUnreadMessages = true; // Set flag if *any* message is unread
                                        // Log.d(TAG, "Message " + messageSnap.getKey() + " is UNREAD for " + ownerUserId + " in Room " + roomId + ". Setting room's hasUnreadMessages = true."); // Too verbose
                                    }
                                }
                            }

                            // --- Apply the logic AFTER finding the latest message ---
                            if (latestMessageSnap != null) {
                                latestMessageTimestamp = highestMessageTimestamp; // Use the found timestamp

                                // <-- NEW: Check the type of the latest message
                                if ("image".equals(latestMessageType)) { // Assuming "image" is the type string for images
                                    lastMessageText = "📸 Photo"; // <-- Set custom text + emoji for images
                                    Log.d(TAG, "  Latest message is an image for Temporary Room " + roomId + ". Setting text to '" + lastMessageText + "'.");
                                } else {
                                    // For text or other types, get the actual message content
                                    lastMessageText = latestMessageSnap.child("message").getValue(String.class);
                                    if (lastMessageText == null) lastMessageText = ""; // Ensure it's not null
                                    // Log.d(TAG, "  Latest message FROM MESSAGES in Temporary Room " + roomId + " is type '" + latestMessageType + "'. Text='" + ((lastMessageText.length() > 50) ? lastMessageText.substring(0, 50) + "..." : lastMessageText) + "', Timestamp=" + latestMessageTimestamp); // Too verbose
                                }
                            } else {
                                // If messages node existed but no valid latest message found (e.g., empty 'messages' node)
                                Log.d(TAG, "  Messages node existed and had children for Temporary Room " + roomId + ", but NO valid latest message FOUND (latestMessageSnap is null after loop).");
                                lastMessageText = ""; // Ensure it's empty
                                // Fallback to room creation timestamp if no messages were found at all
                                latestMessageTimestamp = roomCreationTimestamp != null ? roomCreationTimestamp : 0;
                            }
                        } else {
                            Log.d(TAG, "  No messages found in Temporary Room " + roomId + " ('messages' snapshot doesn't exist or is empty).");
                            // If no messages node or empty, reset text/timestamp
                            lastMessageText = "";
                            // Use room creation timestamp if no messages node exists
                            latestMessageTimestamp = roomCreationTimestamp != null ? roomCreationTimestamp : 0;
                        }
                        // --- End logic to find latest message ---


                        TemporaryRoomEntity roomEntity = new TemporaryRoomEntity();
                        roomEntity.setRoomId(roomId);
                        roomEntity.setOwnerUserId(ownerUserId);
                        roomEntity.setRoomName(roomName != null ? roomName : "Unnamed Room");
                        roomEntity.setCreatedBy(createdBy != null ? createdBy : "Unknown");
                        roomEntity.setExpiryTime(expiryTimeMillis);
                        roomEntity.setLastMessageText(lastMessageText); // <-- This now holds "📸 Photo" or the actual text
                        roomEntity.setLastMessageTimestamp(latestMessageTimestamp); // <-- This is the timestamp from the messages collection (or room creation)
                        roomEntity.setHasUnreadMessages(hasUnreadMessages); // <-- This is calculated from messages collection
                        // Save the room's creation timestamp to the entity
                        roomEntity.setTimestamp(roomCreationTimestamp); // <-- Saving the correct room creation timestamp


                        groupListDao.insertOrUpdateTemporaryRoom(roomEntity);
                        Log.d(TAG, "Temporary Room Entity for " + roomId + " inserted/updated successfully in Room.");

                    } else {
                        // If user is NOT a member or room is expired in Firebase, ensure it's deleted from Room
                        // Add check if it exists in Room first before attempting deletion
                        boolean existsInRoom = groupListDao.getTemporaryRoomById(roomId, ownerUserId) != null; // Requires getTemporaryRoomById(id, ownerId) query in DAO
                        if (existsInRoom) {
                            groupListDao.deleteTemporaryRoomForOwner(roomId, ownerUserId);
                            Log.d(TAG, "User is no longer member or room expired in Firebase, deleted TemporaryRoomEntity from Room: " + roomId);
                        } else {
                            // Log.d(TAG, "Temporary Room " + roomId + " not in Firebase for user " + ownerUserId + " and not found in Room. No deletion needed."); // Too verbose
                        }
                    }
                }

                // --- NEW: Cleanup - Delete temporary rooms in Room that are no longer in Firebase for this user ---
                Log.d(TAG, "Starting cleanup: Checking for Temporary Room Entities in Room not present in current Firebase snapshot for owner: " + ownerUserId);
                List<String> roomIdsInRoom = groupListDao.getTemporaryRoomIdsForOwner(ownerUserId); // You need this query in your DAO
                if (roomIdsInRoom != null && !roomIdsInRoom.isEmpty()) {
                    List<String> idsToDelete = new ArrayList<>();
                    for (String roomIdInRoom : roomIdsInRoom) {
                        if (!presentRoomIdsInFirebase.contains(roomIdInRoom)) {
                            idsToDelete.add(roomIdInRoom);
                            Log.d(TAG, "  Found Temporary Room ID " + roomIdInRoom + " in Room for owner " + ownerUserId + " but NOT in current Firebase snapshot. Marking for deletion.");
                        }
                    }
                    if (!idsToDelete.isEmpty()) {
                        groupListDao.deleteTemporaryRoomsByIdsForOwner(ownerUserId, idsToDelete); // You need this query in your DAO
                        Log.d(TAG, "Cleaned up " + idsToDelete.size() + " temporary room entities from Room not found in Firebase for owner " + ownerUserId + ".");
                    } else {
                        Log.d(TAG, "No temporary room entities in Room for owner " + ownerUserId + " needed cleanup.");
                    }
                } else {
                    Log.d(TAG, "No temporary room entities found in Room for owner " + ownerUserId + ". No cleanup needed.");
                }

                Log.d(TAG, "--- END: processAndSaveTemporaryRoomsToRoom for owner: " + ownerUserId + ". Processing complete. ---");


            } catch (Exception e) {
                Log.e(TAG, "FATAL ERROR in processAndSaveTemporaryRoomsToRoom", e);
            }
        });
    }

    // --- Implement the OnItemClickListener interface method ---
    @Override
    public void onItemClick(TemporaryIChatListItemGroup item) {
        if (currentUserId == null) {
            if(getContext() != null) {
                Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Log.d(TAG, "Item clicked: " + item.getName() + " (ID: " + item.getId() + ")");

        if (item instanceof Group) {
            Group group = (Group) item;
            Intent groupIntent = new Intent(getContext(), GroupChatActivity.class);
            groupIntent.putExtra("groupId", group.getId());
//            groupIntent.putExtra("groupName", group.getName());
//            groupIntent.putExtra("groupImage", group.getImageUrl());
            startActivity(groupIntent);

            markGroupMessagesAsSeenInFirebase(group.getId());

            // Optimistically update unread status in Room
            if (group.hasUnreadMessages()) {
                databaseExecutor.execute(() -> {
                    try {
                        GroupEntity entityToUpdate = groupListDao.getGroupById(group.getId(), currentUserId);
                        if(entityToUpdate != null) {
                            entityToUpdate.setHasUnreadMessages(false);
                            groupListDao.insertOrUpdateGroup(entityToUpdate);
                            Log.d(TAG, "Updated Group unread status to false in Room for " + group.getId());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating Group unread status in Room", e);
                    }
                });
            }

        } else if (item instanceof TemporaryRoom) {
            TemporaryRoom room = (TemporaryRoom) item;
            if (room.isExpired()) {
                if(getContext() != null) {
                    Toast.makeText(getContext(), "This room has expired.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent roomIntent = new Intent(getContext(), TemporaryRoomChatActivity.class);
                roomIntent.putExtra("roomId", room.getId());
                roomIntent.putExtra("roomName", room.getName());
                startActivity(roomIntent);

                markTemporaryRoomMessagesAsSeenInFirebase(room.getId());

                // Optimistically update unread status in Room
                if (room.hasUnreadMessages()) {
                    databaseExecutor.execute(() -> {
                        try {
                            TemporaryRoomEntity entityToUpdate = groupListDao.getTemporaryRoomById(room.getId(), currentUserId);
                            if(entityToUpdate != null) {
                                entityToUpdate.setHasUnreadMessages(false);
                                groupListDao.insertOrUpdateTemporaryRoom(entityToUpdate);
                                Log.d(TAG, "Updated Temporary Room unread status to false in Room for " + room.getId());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating Temporary Room unread status in Room", e);
                        }
                    });
                }
            }
        }
    }

    // --- NEW: Implement the OnItemLongClickListener interface method ---
    @Override
    public void onItemLongClick(TemporaryIChatListItemGroup item) {
        if (currentUserId == null || getContext() == null) {
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Item long-clicked: " + item.getName() + " (ID: " + item.getId() + ")");

        // --- Only handle long press for Groups for deletion ---
        if (item instanceof Group) {
            Group group = (Group) item;
            showDeleteConfirmationDialog(group);
        } else {
            // Optionally handle long press for Temporary Rooms differently (e.g., Leave Room)
            Log.d(TAG, "Long press on Temporary Room. No delete action defined.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Long press action not available for this chat type.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // --- NEW: Method to show the delete confirmation dialog ---
    private void showDeleteConfirmationDialog(Group groupToDelete) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete the group '" + groupToDelete.getName() + "'? This action cannot be undone and will remove the group for everyone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Log.d(TAG, "Delete confirmed for group: " + groupToDelete.getId());
                    deleteGroupFromFirebase(groupToDelete.getId());
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    Log.d(TAG, "Delete cancelled for group: " + groupToDelete.getId());
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Optional: Add an icon
                .show();
    }

    // --- NEW: Method to delete the group from Firebase ---
    private void deleteGroupFromFirebase(String groupId) {
        if (rootRef == null || currentUserId == null || getContext() == null) {
            Log.e(TAG, "Cannot delete group: rootRef or currentUserID or context is null.");
            Toast.makeText(getContext(), "Error deleting group.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting to delete group from Firebase: " + groupId);

        // Show progress dialog
        loadingBar.show();

        // Get the reference to the specific group node
        DatabaseReference groupRef = rootRef.child("Groups").child(groupId);

        // Delete the node
        groupRef.removeValue()
                .addOnCompleteListener(task -> {
                    // Hide progress dialog
                    loadingBar.dismiss();

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Group successfully deleted from Firebase: " + groupId);
                        // Firebase deletion triggers the ValueEventListener,
                        // which will update Room and then the UI automatically.
                        // So, no need to manually remove from the list or Room here.
                        if(getContext() != null) {
                            Toast.makeText(getContext(), "Group deleted.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.e(TAG, "Failed to delete group from Firebase: " + groupId, task.getException());
                        if(getContext() != null) {
                            Toast.makeText(getContext(), "Failed to delete group: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    // --- Implement mark messages as seen methods (as before) ---
    private void markGroupMessagesAsSeenInFirebase(String groupId) {
        if (rootRef == null || currentUserId == null) return;
        rootRef.child("Groups").child(groupId).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    DataSnapshot readBySnapshot = messageSnapshot.child("readBy");
                    if (!readBySnapshot.hasChild(currentUserId) ||
                            !Boolean.TRUE.equals(readBySnapshot.child(currentUserId).getValue(Boolean.class))) {
                        messageSnapshot.getRef().child("readBy").child(currentUserId).setValue(true);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to mark group messages as seen: " + error.getMessage());
            }
        });
    }

    private void markTemporaryRoomMessagesAsSeenInFirebase(String roomId) {
        if (rootRef == null || currentUserId == null) return;
        rootRef.child("temporaryChatRooms").child(roomId).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    DataSnapshot readBySnapshot = messageSnapshot.child("readBy");
                    if (!readBySnapshot.hasChild(currentUserId) ||
                            !Boolean.TRUE.equals(readBySnapshot.child(currentUserId).getValue(Boolean.class))) {
                        messageSnapshot.getRef().child("readBy").child(currentUserId).setValue(true);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to mark temporary room messages as seen: " + error.getMessage());
            }
        });
    }

    // --- Optional: Implement refreshData() if needed externally ---
    public void refreshData() {
        Log.d(TAG,"Manual refresh triggered.");
        if (rootRef != null && currentUserId != null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Refreshing chats...", Toast.LENGTH_SHORT).show();
            }
            // Attaching listeners again acts like a re-sync, but existing listeners might
            // already be active. A better approach for manual refresh might be to
            // force the Firebase sync logic to re-run if needed, or just rely on
            // the LiveData observer. Since ValueEventListeners stay active,
            // any change (including deletions by others) *should* sync automatically.
            // Calling these again is mostly redundant if listeners are properly managed.
            // You could add a flag or detach/reattach if you truly need to force it.
            // For now, rely on the existing listeners.
            Log.d(TAG, "Listeners should already be active, relying on automatic sync.");
        } else {
            if (getContext() != null) {
                Toast.makeText(getContext(), "User not authenticated, cannot refresh.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --- Methods needed in GroupListDao (Add these if you haven't already) ---
    // You already listed these, but you'll need a couple more for cleanup:
    // @Query("SELECT groupId FROM groupss WHERE owner_user_id = :loggedInUserId")
    // List<String> getGroupIdsForOwner(String loggedInUserId);
    //
    // @Query("DELETE FROM groupss WHERE owner_user_id = :ownerUserId AND groupId IN (:groupIds)")
    // void deleteGroupsByIdsForOwner(String ownerUserId, List<String> groupIds);
    //
    // @Query("SELECT roomId FROM temporary_rooms WHERE owner_user_id = :loggedInUserId")
    // List<String> getTemporaryRoomIdsForOwner(String loggedInUserId);
    //
    // @Query("DELETE FROM temporary_rooms WHERE owner_user_id = :ownerUserId AND roomId IN (:roomIds)")
    // void deleteTemporaryRoomsByIdsForOwner(String ownerUserId, List<String> roomIds);


}