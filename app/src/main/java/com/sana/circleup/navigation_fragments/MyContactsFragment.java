package com.sana.circleup.navigation_fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sana.circleup.ChatPageActivity;
import com.sana.circleup.R;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import de.hdodenhof.circleimageview.CircleImageView;




// --- Keep all existing and new imports ---
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

// Assuming 'Contacts' model and 'users_friends_display_layout' exist
// import com.sana.circleup.model.Contacts;
// import com.sana.circleup.R;

// Keep other imports you might have (e.g., CircleImageView)

// --- Keep New Imports for Crypto and Key Management ---
import com.sana.circleup.encryptionfiles.CryptoUtils; // !! ADJUST THIS IMPORT !!
import com.sana.circleup.encryptionfiles.YourKeyManager; // !! ADJUST THIS IMPORT !!
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.ContactDao;
import com.sana.circleup.room_db_implement.ContactEntity;
import com.sana.circleup.room_db_implement.ConversationKeyDao;
import com.sana.circleup.room_db_implement.ConversationKeyEntity;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
// --- End New Imports ---


import androidx.lifecycle.LiveData; // Import LiveData
import androidx.lifecycle.Observer; // Import Observer

import com.google.firebase.database.ChildEventListener; // Import ChildEventListener

import com.sana.circleup.encryptionfiles.ChatIdUtil; // Import your ChatIdUtil

import java.util.ArrayList; // Import ArrayList




public class MyContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment"; // Added TAG
    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference ContactsRef, RootRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId; // Current logged-in user's UID
    private TextView noChatsText; // Use this for "No contacts" or "Loading" status


    // --- NEW Room DB and DAO members ---
    private ChatDatabase db; // Room Database instance
    private ContactDao contactDao; // DAO for contact Entity
    private ConversationKeyDao conversationKeyDao; // DAO for conversation key Entity (Needed for key management logic)
    private LiveData<List<ContactEntity>> contactListLiveData; // LiveData from Room for contact list display
    private ContactAdapter contactAdapter; // Standard RecyclerView Adapter for Room data
    private final List<ContactEntity> contactsArrayList = new ArrayList<>(); // List that holds data for the adapter

    private ExecutorService databaseExecutor; // Use the shared executor from ChatDatabase for Room ops
    // --- End NEW Room DB and DAO ---


    // --- Members for Secure Chat Initiation (Keep These) ---
    private ExecutorService chatExecutor; // For async crypto/Firebase ops initiated from UI click
    private Handler chatHandler; // For posting UI updates back to Main Thread
    private ProgressDialog chatSetupProgressDialog; // Progress dialog for chat setup process
    // --- End Members for Secure Chat Initiation ---

    // --- NEW Firebase Listener for Syncing Contacts from Firebase to Room ---
    private ChildEventListener contactsSyncChildEventListener; // Listener to sync changes from /Contacts
    // --- End Firebase Listener for Sync ---


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { // Added @NonNull
        Log.d(TAG, "onCreateView called.");

        ContactsView = inflater.inflate(R.layout.fragment_my_contacts, container, false);
        myContactsList = ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        noChatsText = ContactsView.findViewById(R.id.noChatsText);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            Log.d(TAG, "Current User ID: " + currentUserId);
        } else {
            Log.e(TAG, "User not authenticated in ContactsFragment onCreateView!");
            currentUserId = null;
            // Handle case where user is not authenticated (e.g., redirect to login)
            if (getContext() != null) {
                Toast.makeText(getContext(), "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
                // Consider navigating to login activity
            }
            // Set UI state for unauthenticated user
            noChatsText.setVisibility(View.VISIBLE);
            noChatsText.setText("User not logged in.");
            myContactsList.setVisibility(View.GONE);
            return ContactsView; // Return view in an error state
        }


        // --- Initialize Firebase DB References (Keep These) ---
        RootRef = FirebaseDatabase.getInstance().getReference();
        ContactsRef = RootRef.child("Contacts").child(currentUserId); // Path to the current user's contacts list
        UsersRef = RootRef.child("Users"); // Path to all user profiles
        // --- End Initialization ---


        // --- Initialize Executors and Handler (Keep These) ---
        chatExecutor = Executors.newSingleThreadExecutor(); // For async crypto/Firebase ops initiated from UI click
        chatHandler = new Handler(Looper.getMainLooper()); // For posting UI updates back to Main Thread
        // Initialize dialog here if context is available, or lazy init before showing
        // Use getContext() which is available in onCreateView
        chatSetupProgressDialog = new ProgressDialog(getContext());
        chatSetupProgressDialog.setCancelable(false); // Prevent dismissing by tapping outside
        // --- End Initialization ---


        // --- Initialize Room DB and DAOs (NEW/Updated) ---
        // Use requireContext() in Fragment, safe because onCreateView guarantees context
        db = ChatDatabase.getInstance(requireContext()); // Get Room DB instance
        contactDao = db.contactDao(); // Get the DAO for contacts
        conversationKeyDao = db.conversationKeyDao(); // Get the DAO for conversation keys (Needed for key management)
        databaseExecutor = ChatDatabase.databaseWriteExecutor; // Use the shared DB executor for Room ops
        // --- End Initialize Room DB and DAOs ---


        // --- Setup RecyclerView Adapter (NEW/Updated) ---
        // Initialize the standard adapter. It will be empty initially, then populated by LiveData.
        // Pass the click listener implementation here.
        contactAdapter = new ContactAdapter(contactsArrayList, requireContext(), new ContactAdapter.OnContactClickListener() {
            @Override
            public void onContactClick(ContactEntity contact) {
                Log.d(TAG, "Contact item or button clicked (via adapter listener): " + contact.getName() + ", UID: " + contact.getContactUserId());
                // When the item or button is clicked, initiate the secure chat process
                startSecureChatWithUser(contact.getContactUserId(), contact.getName(), contact.getProfileImageBase64()); // Use data from Room entity
            }
        });
        myContactsList.setAdapter(contactAdapter); // Set the new standard adapter
        // --- End Setup RecyclerView Adapter ---


        // Show loading text initially while Room loads or syncs
        noChatsText.setVisibility(View.VISIBLE);
        noChatsText.setText("Loading contacts...");
        myContactsList.setVisibility(View.GONE);


        // Removed the FirebaseRecyclerAdapter initialization and setup from onCreateView
        // It will now be handled by Room LiveData and Firebase sync listener


        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called.");

        if (currentUserId == null) {
            Log.e(TAG, "Cannot start ContactsFragment listeners, currentUserId is null.");
            // UI state already set in onCreateView if needed
            return; // Exit if user not authenticated
        }

        // --- Step 1: Load contacts from Room DB using LiveData ---
        // This will immediately load any existing offline data and keep UI updated with Room changes
        contactListLiveData = contactDao.getAllContactsForUser(currentUserId); // Use the correct DAO method to get LiveData

        // Observe the LiveData. getViewLifecycleOwner() is used to bind the observer to the fragment's view lifecycle.
        // The observer will be automatically removed when the fragment's view is destroyed.
        contactListLiveData.observe(getViewLifecycleOwner(), new Observer<List<ContactEntity>>() {
            @Override
            public void onChanged(List<ContactEntity> contactEntities) {
                Log.d(TAG, "Contacts LiveData updated with " + (contactEntities != null ? contactEntities.size() : 0) + " contacts for owner " + currentUserId);

                // Update the adapter's list with the new data from Room
                contactsArrayList.clear(); // Clear the list feeding the adapter
                if (contactEntities != null) {
                    contactsArrayList.addAll(contactEntities); // Add contacts from Room
                }
                contactAdapter.notifyDataSetChanged(); // Notify adapter to refresh UI

                // --- Step 2: Update UI visibility based on Room data ---
                // Show "No contacts" message if the list is empty from Room
                if (contactsArrayList.isEmpty()) {
                    noChatsText.setVisibility(View.VISIBLE);
                    // Set text indicating no contacts found. Initial "Loading..." will be overwritten if no data found.
                    noChatsText.setText("No contacts found.\nAdd friends or sync contacts."); // Custom message
                    myContactsList.setVisibility(View.GONE);
                } else {
                    noChatsText.setVisibility(View.GONE);
                    myContactsList.setVisibility(View.VISIBLE);
                }
            }
        });
        // --- End Step 1 & 2 ---


        // --- Step 3: Attach Firebase ChildEventListener for Syncing ---
        // This listener syncs changes from Firebase *into* the Room DB.
        // Room LiveData then automatically updates the UI via the observer above.
        attachContactsSyncListener(); // Call the method to attach the sync listener
        // --- End Step 3 ---

        // Removed FirebaseRecyclerAdapter initialization and startListening() from onStart
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called.");
        // --- Step 4: Remove Firebase Listener ---
        removeContactsSyncListener(); // Remove the ChildEventListener when the fragment is stopped
        // --- End Step 4 ---

        // Removed FirebaseRecyclerAdapter stopListening() from onStop

        // Dismiss dialog if visible to prevent window leaks when fragment stops
        if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) {
            chatSetupProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() { // Use onDestroyView for fragment view cleanup
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called.");

        // Remove LiveData observer when the view is destroyed to prevent memory leaks
        if (contactListLiveData != null) {
            contactListLiveData.removeObservers(getViewLifecycleOwner()); // Use getViewLifecycleOwner()
            Log.d(TAG, "Contacts LiveData observer removed.");
        }

        // Shutdown chatExecutor service gracefully to free up resources
        // Note: databaseExecutor is managed by ChatDatabase itself (singleton).
        if (chatExecutor != null && !chatExecutor.isShutdown()) {
            chatExecutor.shutdownNow(); // Or shutdown() if you want to finish queued tasks before shutting down
            Log.d(TAG, "chatExecutor shutdown called.");
        }

        // Dismiss progress dialog if it's showing to prevent window leaks
        if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) {
            chatSetupProgressDialog.dismiss();
        }
        // Clean up view references to help garbage collection and prevent memory leaks
        myContactsList = null;
        ContactsView = null;
        noChatsText = null;
        // contactAdapter and contactsArrayList references are fine as they are members

        Log.d(TAG, "onDestroyView finished.");
    }

    // Keep onDestroy for any cleanup beyond view destruction if necessary (e.g., non-view related resources)
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called.");
        // Room DB executor is managed by ChatDatabase singleton, no need to shut down here.
        // Any other long-lived resources not tied to the view should be cleaned up here.
    }


    // --- NEW Method to Attach Firebase ChildEventListener for Contacts Sync (Keep This) ---
    // This listener watches the current user's /Contacts node for adds, changes, and removals.
    // It then triggers fetching user details from /Users and updating the Room DB.
    // @SuppressLint("RestrictedApi") // Keep if needed for any hidden APIs used internally by Firebase library
    @SuppressLint("RestrictedApi")
    private void attachContactsSyncListener() {
        if (currentUserId == null) {
            Log.e(TAG, "Cannot attach contacts sync listener, currentUserId is null.");
            return;
        }
        // Check if the listener is already attached
        if (contactsSyncChildEventListener == null) {
            Log.d(TAG, "Attaching Firebase ChildEventListener for Contacts sync for user: " + currentUserId);

            // Create the ChildEventListener implementation
            contactsSyncChildEventListener = new ChildEventListener() { // Corrected member variable name
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // A new contact UID is added to /Contacts/{currentUserId} list in Firebase
                    String contactUserId = snapshot.getKey(); // This is the UID of the contact

                    if (TextUtils.isEmpty(contactUserId)) {
                        Log.w(TAG, "onChildAdded received null or empty contact UID.");
                        return;
                    }
                    // Crucial: Ensure we don't process self UID mistakenly added to contacts list
                    if (contactUserId.equals(currentUserId)) {
                        Log.d(TAG, "onChildAdded received self UID (" + contactUserId + ") in contacts. Ignoring.");
                        return;
                    }

                    Log.d(TAG, "onChildAdded triggered for new contact UID: " + contactUserId);

                    // Fetch the details for this contact UID from the /Users node and save/update in Room DB
                    // This is necessary because the /Contacts node usually only stores the UID.
                    fetchAndSaveContactDetailsToRoom(contactUserId, currentUserId); // Pass contact's UID and current user's UID (as owner)
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // An existing contact UID's entry changed in /Contacts/{currentUserId}
                    // This might happen if you store extra data (like request status) directly under /Contacts,
                    // or if Firebase re-orders the list. We re-fetch user details to be safe.
                    String contactUserId = snapshot.getKey();

                    if (TextUtils.isEmpty(contactUserId)) {
                        Log.w(TAG, "onChildChanged received null or empty contact UID.");
                        return;
                    }
                    if (contactUserId.equals(currentUserId)) {
                        Log.d(TAG, "onChildChanged received self UID (" + contactUserId + ") in contacts. Ignoring.");
                        return;
                    }

                    Log.d(TAG, "onChildChanged triggered for contact UID: " + contactUserId);
                    // Re-fetch user details from /Users to update the Room entry
                    // This will ensure profile picture, status, etc. are updated if they change in /Users
                    // and the contact entry in /Contacts triggers a change.
                    fetchAndSaveContactDetailsToRoom(contactUserId, currentUserId); // Pass contact's UID and current user's UID (as owner)
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    // A contact UID is removed from /Contacts/{currentUserId} list in Firebase
                    String contactUserId = snapshot.getKey();

                    if (TextUtils.isEmpty(contactUserId)) {
                        Log.w(TAG, "onChildRemoved received null or empty contact UID.");
                        return;
                    }
                    if (contactUserId.equals(currentUserId)) {
                        Log.d(TAG, "onChildRemoved received self UID (" + contactUserId + ") in contacts. Ignoring.");
                        return;
                    }
                    Log.d(TAG, "onChildRemoved triggered for contact UID: " + contactUserId + ". Removing from Room DB.");

                    // Remove the corresponding contact entry from Room DB on a background thread
                    removeContactFromRoom(contactUserId, currentUserId); // Pass contact's UID and current user's UID (as owner)
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    // A child's position changed in the list. Usually only matters if you display in Firebase's native order.
                    // Room LiveData handles ordering internally, so this event is often less critical for Room sync.
                    // Log.d(TAG, "onChildMoved triggered for contact UID: " + snapshot.getKey());
                    // If you need to handle order changes in Room, implement logic here.
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase Contacts Listener cancelled for user " + currentUserId + ": " + error.getMessage(), error.toException());
                    // Handle error - show a message to the user on the main thread
                    if (getContext() != null) {
                        chatHandler.post(() -> Toast.makeText(getContext(), "Failed to sync contacts: " + error.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }
            };

            // Attach the listener to the current user's contacts node in Firebase
            ContactsRef.addChildEventListener(contactsSyncChildEventListener); // Corrected member variable name
            Log.d(TAG, "Contacts sync listener attached to: " + ContactsRef.getPath());

            // Note: This sync listener primarily reacts to changes in the *list of contacts* maintained
            // under /Contacts/{currentUserId}. To keep profile *details* (username, status, image)
            // updated in Room *in real-time* when they change in /Users/{contactUserId}, you would
            // ideally attach ValueEventListeners to /Users/{contactUserId} for each contact in your list.
            // However, doing this for every contact is resource-intensive and not scalable for large lists.
            // A more practical approach is to rely on the fetch triggered by the ContactsRef listener,
            // or implement a separate mechanism for profile details sync (e.g., periodically, or when user is online).
            // For now, profile details sync happens when the contact entry in the /Contacts list is added or changed.
        }
    }
    // --- END NEW Method to Attach Firebase Listener ---

    // --- NEW Method to Remove Firebase ChildEventListener (Keep This) ---
    // Remove the listener when the fragment is stopped to prevent memory leaks and unnecessary background activity.
    private void removeContactsSyncListener() {
        if (ContactsRef != null && contactsSyncChildEventListener != null) { // Corrected member variable name
            Log.d(TAG, "Removing Firebase ChildEventListener for Contacts sync.");
            // Remove the listener from the Firebase reference
            ContactsRef.removeEventListener(contactsSyncChildEventListener); // Corrected member variable name
            contactsSyncChildEventListener = null; // Nullify the reference
        }
        // If you implemented individual user listeners for online status, remove them here too
        // if (userDetailsSyncListener != null && UsersRef != null) { UsersRef.removeEventListener(userDetailsSyncListener); ... }
    }
    // --- END NEW Method to Remove Firebase Listener ---


    // --- NEW Method to Fetch User Details from /Users and Save/Update to Room (Keep This) ---
    // This method is called by the contactsSyncChildEventListener when a contact is added or changed in Firebase.
    // It fetches the contact's profile data from the /Users node and saves it as a ContactEntity in Room.
    private void fetchAndSaveContactDetailsToRoom(String contactUserId, String ownerUserId) {
        if (TextUtils.isEmpty(contactUserId) || TextUtils.isEmpty(ownerUserId)) {
            Log.e(TAG, "Cannot fetch and save contact details: Missing user IDs.");
            return;
        }
        Log.d(TAG, "Fetching details for contact " + contactUserId + " to save/update for owner " + ownerUserId);

        // Fetch contact's details from the /Users node using SingleValueEvent (efficient for one-time fetch)
        // This fetch runs on the main thread, the Room operation will be moved to background.
        UsersRef.child(contactUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Extract user details from /Users/{contactUserId} snapshot
                    String name = snapshot.child("username").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    // Fetch profileImage as Base64 string (check if it exists)
                    String profileImageBase64 = snapshot.hasChild("profileImage") ? snapshot.child("profileImage").getValue(String.class) : "";
                    // Check online status from userState (check if userState and state exist)
                    boolean isOnline = snapshot.child("userState").hasChild("state") && "online".equals(snapshot.child("userState").child("state").getValue(String.class));


                    // Handle potential nulls or empty strings for display purposes
                    if (TextUtils.isEmpty(name)) name = "Unknown User";
                    if (TextUtils.isEmpty(status)) status = "No Status";

                    // Create a ContactEntity object from the fetched data
                    // The primary key (contactId) must be unique for this owner's list.
                    String contactId = ContactEntity.generateContactId(ownerUserId, contactUserId); // Use your helper method to generate ID
                    if (TextUtils.isEmpty(contactId)) {
                        Log.e(TAG, "Failed to generate unique contactId for saving contact " + contactUserId + " for owner " + ownerUserId + ". Skipping save to Room.");
                        // This is a critical ID generation failure. Decide how to handle.
                        return; // Cannot save without a valid primary key
                    }

                    // Create the entity instance
                    ContactEntity contact = new ContactEntity(contactId, ownerUserId, contactUserId, name, status, profileImageBase64, isOnline);

                    // Save/Update the contact in Room DB on a background thread using the shared executor
                    databaseExecutor.execute(() -> {
                        try {
                            contactDao.insertOrUpdateContact(contact); // Insert or replace the contact entry for this owner
                            Log.d(TAG, "Contact details saved/updated in Room DB for contact " + contactUserId + " (owner: " + ownerUserId + ")");
                            // LiveData observer attached in onStart will automatically pick this Room change up and update the UI
                        } catch (Exception e) {
                            Log.e(TAG, "Error saving/updating contact " + contactUserId + " in Room DB for owner " + ownerUserId, e);
                            // Handle Room DB errors if necessary
                        }
                    });

                } else {
                    // If user data does not exist in /Users for this contact UID, the contact entry is invalid.
                    Log.w(TAG, "User data not found in /Users for contact UID: " + contactUserId + ". Removing this contact from Room for owner " + ownerUserId + " and from Firebase contacts list.");
                    // Remove the invalid contact entry from Room DB on a background thread
                    removeContactFromRoom(contactUserId, ownerUserId); // Remove from Room first

                    // Also remove this invalid contact entry from the current user's Firebase contacts list (/Contacts/{currentUserId})
                    // This cleans up the Firebase list if a user account gets deleted or becomes invalid.
                    ContactsRef.child(contactUserId).removeValue()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Removed invalid contact " + contactUserId + " from Firebase Contacts for owner " + ownerUserId + " (User data not found)."))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to remove invalid contact " + contactUserId + " from Firebase Contacts for owner " + ownerUserId, e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase Error fetching user data for contact " + contactUserId + " for sync: " + error.getMessage(), error.toException());
                // Handle Firebase fetch errors for user details. Log it, maybe retry logic needed for production.
                // No need to remove from Room/Firebase just because fetch failed; it might be a temporary network issue.
            }
        });
    }
    // --- END NEW Method to Fetch User Details and Save to Room ---

    // --- NEW Method to Remove Contact from Room (Keep This) ---
    // This method is called by the contactsSyncChildEventListener when a contact is removed from the Firebase /Contacts list.
    private void removeContactFromRoom(String contactUserId, String ownerUserId) {
        if (TextUtils.isEmpty(contactUserId) || TextUtils.isEmpty(ownerUserId)) {
            Log.e(TAG, "Cannot remove contact from Room DB: Missing user IDs.");
            return;
        }
        // Use the same ID generation logic to find the contact entry in Room DB
        String contactId = ContactEntity.generateContactId(ownerUserId, contactUserId);
        if (TextUtils.isEmpty(contactId)) {
            Log.e(TAG, "Failed to generate contactId for removing contact " + contactUserId + " for owner " + ownerUserId + ". Skipping removal from Room.");
            return; // Cannot remove without a valid primary key
        }

        databaseExecutor.execute(() -> { // Use the shared database executor for Room ops
            try {
                int deletedRows = contactDao.deleteContactById(contactId); // Use delete by the generated contactId (primary key)
                if (deletedRows > 0) {
                    Log.d(TAG, "Contact removed from Room DB for contact " + contactUserId + " (owner: " + ownerUserId + "). Rows deleted: " + deletedRows);
                    // LiveData observer will pick this Room change up and update UI automatically
                } else {
                    Log.w(TAG, "Attempted to remove contact " + contactUserId + " from Room DB for owner " + ownerUserId + ", but it was not found in Room.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error removing contact " + contactUserId + " from Room DB for owner " + ownerUserId, e);
                // Handle Room DB errors if necessary
            }
        });
    }
    // --- END NEW Method to Remove Contact from Room ---


    // --- Helper method to load Conversation Keys from Room (Keep This) ---
    // This method is kept for reference but is NOT called from this fragment's core logic.
    // Loading conversation keys into KeyManager should ideally happen once during user login/app startup
    // to populate the in-memory KeyManager cache. Keeping it here as it interacts with conversationKeyDao.
    // This method does NOT affect the contacts list display itself.





    // Inside ContactsFragment.java

    private void startSecureChatWithUser(String recipientUserId, String recipientName, String recipientImageBase64) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to chat.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Attempted to start chat, but user is not logged in.");
            return;
        }
        String currentUserId = currentUser.getUid(); // Current user's UID

        // --- IMPORTANT: Check if Current User's Private Key is Available in KeyManager ---
        // This is necessary because we need it to decrypt wrapped conversation keys or to encrypt new keys.
        if (!YourKeyManager.getInstance().isPrivateKeyAvailable()) {
            Log.w(TAG, "User's Private key is NOT available. Cannot start secure chat.");
            Toast.makeText(getContext(), "Your account security setup is incomplete or locked. Cannot start secure chat. Please go to settings to set up or unlock your Security Passphrase.", Toast.LENGTH_LONG).show();
            // Option: Navigate user to your Security Settings Activity here.
            // startActivity(new Intent(getContext(), PrivacyAndSecurity.class)); // Example navigation
            return; // Stop the chat initiation process
        }

        // --- Ensure recipient is not the current user ---
        if (currentUserId.equals(recipientUserId)) {
            Toast.makeText(getContext(), "Cannot chat with yourself.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Attempted to start chat with self: " + currentUserId);
            return; // Stop the chat initiation process
        }

        // 1. Generate a unique, consistent Conversation ID for this pair of users
        String conversationId = ChatIdUtil.generateConversationId(currentUserId, recipientUserId); // Use your helper method
        if (TextUtils.isEmpty(conversationId)) {
            Log.e(TAG, "Failed to generate conversationId for chat with " + recipientUserId);
            Toast.makeText(getContext(), "Error starting chat: Invalid conversation ID.", Toast.LENGTH_SHORT).show();
            return; // Stop if conversationId is invalid
        }
        Log.d(TAG, "Generated Conversation ID: " + conversationId + " for users " + currentUserId + " and " + recipientUserId);


        // --- Check if ANY conversation key version is already in YourKeyManager's in-memory cache (MODIFIED Check) ---
        // Keys should be loaded into KeyManager from Room/Secure Storage when the app starts or user logs in.
        // Checking the cache is the fastest way. We now check if *any* version exists.
        boolean hasAnyCachedConversationKey = YourKeyManager.getInstance().hasConversationKey(conversationId); // <-- Use the new hasConversationKey method

        if (hasAnyCachedConversationKey) {
            Log.d(TAG, "At least one conversation key version found in KeyManager cache for ID: " + conversationId + ". Navigating directly to chat.");
            // At least one key version is available in memory. The decryption logic in ChatPageActivity
            // will handle trying all available versions. No need to check Firebase or generate/decrypt here.
            // Just navigate to the chat activity immediately.
            // Dismiss progress dialog if showing (unlikely at this point unless race condition)
            if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) {
                chatSetupProgressDialog.dismiss();
            }
            navigateToChatActivity(conversationId, recipientUserId, recipientName, recipientImageBase64);
            return; // Exit the method, chat initiated successfully from cache
        }
        // --- END Check KeyManager Cache ---

        // If no key versions are in KeyManager cache, proceed with showing progress and checking Firebase as a fallback.
        // This path handles cases where keys were NOT loaded into memory on app start but might exist in Firebase.

        // Show progress indicator while we check Firebase and potentially generate/decrypt keys
        // Check if context and dialog instance is valid before showing
        if (getContext() == null) {
            Log.w(TAG, "Context is null, cannot show progress dialog.");
            Toast.makeText(getContext(), "Error starting chat.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (chatSetupProgressDialog == null || chatSetupProgressDialog.getOwnerActivity() != getActivity()) {
            chatSetupProgressDialog = new ProgressDialog(getContext()); // Re-create if needed
            chatSetupProgressDialog.setCancelable(false);
            if (getActivity() != null) chatSetupProgressDialog.setOwnerActivity(getActivity()); // Set owner activity to prevent leaks
            else Log.w(TAG, "getActivity() is null, cannot set dialog owner activity.");
        }
        chatSetupProgressDialog.setMessage("Setting up secure chat...");
        if (!chatSetupProgressDialog.isShowing()) { // Only show if not already showing
            chatSetupProgressDialog.show();
        }


        // --- Check for key versions in Firebase (Fallback if not in cache) (MODIFIED) ---
        // This is the fallback if no key versions were in the in-memory KeyManager cache.
        // Check if the node containing key versions exists for this conversation.
        DatabaseReference conversationKeyVersionsRef = RootRef.child("ConversationKeys").child(conversationId).child("key_versions"); // <-- MODIFIED PATH

        conversationKeyVersionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This callback runs on the main thread

                // Check if the 'key_versions' node exists and has any children (i.e., any key versions stored)
                if (snapshot.exists() && snapshot.hasChildren()) {
                    // Key versions found in Firebase for this conversation.
                    Log.d(TAG, "Conversation key versions found in Firebase for ID: " + conversationId + ". Fetching existing versions.");
                    // Proceed to fetch and decrypt ALL existing key versions for the current user on a background thread
                    fetchAndDecryptConversationKeyAsync(conversationId, currentUserId, snapshot, recipientUserId, recipientName, recipientImageBase64);

                } else {
                    // No key versions found under 'key_versions' for this conversation in Firebase.
                    // This is either a brand new secure chat, OR an existing chat where key versions were deleted.
                    Log.d(TAG, "No conversation key versions found in Firebase for ID: " + conversationId + ". Generating NEW keys.");
                    // Proceed with generating and saving a new initial key version on a background thread
                    generateAndSaveConversationKeysAsync(conversationId, currentUserId, recipientUserId, recipientName, recipientImageBase64);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // This callback runs on the main thread due to addListenerForSingleValueEvent
                if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) chatSetupProgressDialog.dismiss();
                Log.e(TAG, "Failed to check for existing conversation key versions in Firebase", error.toException());
                Toast.makeText(getContext(), "Error starting chat: Could not check for keys.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Method to Generate and Save Keys (Called if keys don't exist in Firebase) (Keep This) ---
    // This method generates a new AES key, encrypts it with the public keys of both users,
    // saves the wrapped keys to Firebase, stores the decrypted AES key in KeyManager (in-memory),
    // AND saves the decrypted AES key to Room DB for persistence (offline loading).

    // --- Method to Fetch and Decrypt Existing Conversation Key (Called if keys exist in Firebase but not in KeyManager/Room) (Keep This) ---
    // This method fetches the wrapped keys from Firebase, decrypts the key for the current user
    // using their RSA Private Key, stores the decrypted AES key in KeyManager (in-memory),
    // AND saves the decrypted AES key to Room DB for persistence (offline loading).

    private void generateAndSaveConversationKeysAsync(String conversationId, String currentUserId, String recipientUserId, String recipientName, String recipientImageBase64) {
        // Ensure progress dialog is showing (redundant check, but safe)
        if (getContext() == null) { Log.w(TAG, "Context null in generateAndSaveConversationKeysAsync."); return; }
        if (chatSetupProgressDialog == null || chatSetupProgressDialog.getOwnerActivity() != getActivity()) {
            chatSetupProgressDialog = new ProgressDialog(getContext()); chatSetupProgressDialog.setCancelable(false); chatSetupProgressDialog.setOwnerActivity(getActivity());
        }
        if (!chatSetupProgressDialog.isShowing()) {
            chatSetupProgressDialog.setMessage("Generating and saving keys...");
            chatSetupProgressDialog.show();
        }
        Log.d(TAG, "Generating and saving new conversation key version for " + conversationId);

        // Fetch recipient's public key first from Firebase (needed for RSA encryption)
        DatabaseReference recipientUserRef = UsersRef.child(recipientUserId);

        recipientUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatExecutor.execute(() -> {
                    SecretKey conversationAESKey = null;
                    byte[] conversationAESKeyBytes = null;

                    PublicKey recipientPublicKey = null;
                    PublicKey currentUserPublicKey = null;
                    byte[] encryptedAesKeyForRecipient = null;
                    byte[] encryptedAesKeyForCurrentUser = null;

                    try {
                        conversationAESKey = CryptoUtils.generateAESKey();
                        conversationAESKeyBytes = CryptoUtils.secretKeyToBytes(conversationAESKey);

                        if (snapshot.exists() && snapshot.hasChild("publicKey")) {
                            String recipientPublicKeyBase64 = snapshot.child("publicKey").getValue(String.class);
                            if (TextUtils.isEmpty(recipientPublicKeyBase64)) {
                                Log.e(TAG, "Recipient public key Base64 is empty for " + recipientUserId + " in Firebase.");
                                throw new IllegalArgumentException("Empty recipient public key Base64 from Firebase");
                            }
                            byte[] recipientPublicKeyBytes = CryptoUtils.base64ToBytes(recipientPublicKeyBase64);
                            recipientPublicKey = CryptoUtils.bytesToPublicKey(recipientPublicKeyBytes);
                            Log.d(TAG, "Recipient public key obtained for " + recipientUserId);

                            currentUserPublicKey = YourKeyManager.getInstance().getUserPublicKey();
                            if (currentUserPublicKey == null) {
                                Log.e(TAG, "Current user's public key is null during key generation! KeyManager state error. Cannot encrypt key for self.");
                                throw new IllegalStateException("Current user public key unavailable from KeyManager");
                            }
                            Log.d(TAG, "Current user public key obtained from KeyManager.");

                            encryptedAesKeyForRecipient = CryptoUtils.encryptWithRSA(conversationAESKeyBytes, recipientPublicKey);
                            Log.d(TAG, "AES key encrypted for recipient.");

                            encryptedAesKeyForCurrentUser = CryptoUtils.encryptWithRSA(conversationAESKeyBytes, currentUserPublicKey);
                            Log.d(TAG, "AES key encrypted for current user.");

                        } else {
                            Log.w(TAG, "Recipient (" + recipientUserId + ") does not have public key in Firebase or user data missing. Cannot start secure chat.");
                            chatHandler.post(() -> {
                                if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) chatSetupProgressDialog.dismiss();
                                Toast.makeText(getContext(), "Recipient has not completed security setup or user data missing. Secure chat unavailable.", Toast.LENGTH_LONG).show();
                            });
                            return;
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Key data processing error during key generation: " + e.getMessage(), e);
                        chatHandler.post(() -> { if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) chatSetupProgressDialog.dismiss(); Toast.makeText(getContext(), "Failed to start secure chat: Invalid key data encountered.", Toast.LENGTH_SHORT).show(); });
                        return;
                    } catch (NoSuchAlgorithmException e) {
                        Log.e(TAG, "AES key generation failed: " + e.getMessage(), e);
                        chatHandler.post(() -> { if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) chatSetupProgressDialog.dismiss(); Toast.makeText(getContext(), "Failed to start secure chat: Key generation error.", Toast.LENGTH_SHORT).show(); });
                        return;
                    }
                    catch (NoSuchPaddingException | InvalidKeyException |
                           IllegalBlockSizeException | BadPaddingException | IllegalStateException e) {
                        Log.e(TAG, "Cryptographic or state error during key encryption: " + e.getMessage(), e);
                        chatHandler.post(() -> {
                            if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) chatSetupProgressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed to start secure chat: Encryption error during key exchange.", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    } catch (Exception e) {
                        Log.e(TAG, "Unexpected error during key generation/encryption: " + e.getMessage(), e);
                        chatHandler.post(() -> {
                            if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) chatSetupProgressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed to start secure chat: An unexpected error occurred.", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    // --- Prepare data to save to Realtime Database under ConversationKeys/{convId}/key_versions/{pushId} ---
                    Log.d(TAG, "Encryption successful. Preparing to save NEW key version to Firebase.");

                    String encryptedAesKeyForRecipientBase64 = CryptoUtils.bytesToBase64(encryptedAesKeyForRecipient);
                    String encryptedAesKeyForCurrentUserBase64 = CryptoUtils.bytesToBase64(encryptedAesKeyForCurrentUser);

                    // Use .push() to generate a unique key *before* setting the value
                    // Inside MyContactsFragment.java -> generateAndSaveConversationKeysAsync
// Use a local timestamp for the key name, which is consistent with your loading logic
                    final long newKeyTimestamp = System.currentTimeMillis(); // Get a timestamp here before saving
                    DatabaseReference newKeyVersionRef = RootRef.child("ConversationKeys").child(conversationId).child("key_versions").child(String.valueOf(newKeyTimestamp)); // <-- Use the timestamp as the child key name
                    // Use ServerValue.TIMESTAMP. Firebase will set the actual server time.
                    Map<String, Object> keyVersionData = new HashMap<>();
                    keyVersionData.put(currentUserId, encryptedAesKeyForCurrentUserBase64);
                    keyVersionData.put(recipientUserId, encryptedAesKeyForRecipientBase64);
                    keyVersionData.put("generatedBy", currentUserId);
                    keyVersionData.put("timestamp", ServerValue.TIMESTAMP); // Use ServerValue.TIMESTAMP

                    final String finalCurrentUserId = currentUserId;
                    final String finalConversationId = conversationId;
                    final byte[] finalConversationAESKeyBytes = conversationAESKeyBytes;
                    final SecretKey finalConversationAESKey = conversationAESKey;
                    final String finalRecipientUserId = recipientUserId;
                    final String finalRecipientName = recipientName;
                    final String finalRecipientImageBase64 = recipientImageBase64;
                    // No need for finalResolvedTimestamp here yet

                    // 7. Save the NEW key version to Realtime Database (Async operation)
                    newKeyVersionRef.setValue(keyVersionData) // <-- Set value on the PUSHED ref
                            .addOnCompleteListener(task -> {
                                chatHandler.post(() -> {
                                    if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) chatSetupProgressDialog.dismiss();

                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "NEW Conversation key version saved successfully to Firebase for " + finalConversationId + " at path: " + newKeyVersionRef.getPath());

                                        // --- Capture the resolved server timestamp by reading it back (or use a reliable fallback) ---
                                        // Reading back is more complex but accurate. Using System.currentTimeMillis() as a fallback is simpler for now.
                                        // A better approach might be to listen for onChildAdded on key_versions or use a cloud function to timestamp.
                                        // For immediate loading after a successful write, using a local timestamp is often acceptable
                                        // *if* you understand its limitations (clock skew). Let's use local timestamp here for simplicity
                                        // after the Firebase write is confirmed successful.

                                        final long finalResolvedTimestamp = System.currentTimeMillis(); // <-- Use local time AFTER Firebase success


                                        // --- Store the decrypted conversationAESKey + timestamp in memory (YourKeyManager) ---
//                                        YourKeyManager.getInstance().setConversationKey(finalConversationId, finalResolvedTimestamp, finalConversationAESKey); // *** CORRECTED KEYMANAGER CALL ***
//                                        Log.d(TAG, "NEW Key version added to KeyManager cache. Conv ID: " + finalConversationId + ", Timestamp: " + finalResolvedTimestamp + ". Total versions: " + YourKeyManager.getInstance().getAllConversationKeysForConversation(finalConversationId).size());



                                        YourKeyManager.getInstance().setConversationKey(finalConversationId, finalConversationAESKey); // <<< CORRECTED: Removed finalResolvedTimestamp


                                        // --- Save the decrypted conversation key + resolved timestamp locally to Room DB for persistence ---
                                        databaseExecutor.execute(() -> {
                                            try {
                                                String decryptedKeyBase64 = CryptoUtils.bytesToBase64(finalConversationAESKeyBytes);
                                                ConversationKeyEntity keyEntity = new ConversationKeyEntity(finalCurrentUserId, finalConversationId, decryptedKeyBase64); // *** USE RESOLVED TIMESTAMP ***
                                                conversationKeyDao.insertOrUpdateKey(keyEntity);
                                                Log.d(TAG, "NEW Decrypted conversation key version saved to Room DB for owner " + finalCurrentUserId + ", conversation " + finalConversationId + ", timestamp " + finalResolvedTimestamp + " after Firebase save.");
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error saving NEW decrypted conversation key version to Room DB after Firebase save for user " + finalCurrentUserId + " conv " + finalConversationId + " timestamp " + finalResolvedTimestamp, e);
                                            }
                                        });

                                        // NOW, navigate to the actual Chat Activity
                                        navigateToChatActivity(finalConversationId, finalRecipientUserId, finalRecipientName, finalRecipientImageBase64);

                                    } else {
                                        Log.e(TAG, "Failed to save NEW conversation key version to Firebase for " + finalConversationId, task.getException());
                                        Toast.makeText(getContext(), "Failed to start secure chat: Could not save keys.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch recipient public key for key generation (Firebase error)", error.toException());
                chatHandler.post(() -> {
                    if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) chatSetupProgressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to start secure chat: Could not fetch recipient data.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    // --- Method to Fetch and Decrypt Existing Conversation Keys (Remains the same from previous step) ---
    private void fetchAndDecryptConversationKeyAsync(String conversationId, String currentUserId, DataSnapshot keyVersionsSnapshot, String recipientUserId, String recipientName, String recipientImageBase64) {
        // Ensure progress dialog is showing
        if (getContext() == null) { Log.w(TAG, "Context null in fetchAndDecryptConversationKeyAsync."); return; }
        if (chatSetupProgressDialog == null || chatSetupProgressDialog.getOwnerActivity() != getActivity()) {
            chatSetupProgressDialog = new ProgressDialog(getContext()); chatSetupProgressDialog.setCancelable(false); chatSetupProgressDialog.setOwnerActivity(getActivity());
        }
        if (!chatSetupProgressDialog.isShowing()) {
            chatSetupProgressDialog.setMessage("Loading secure chat keys...");
            chatSetupProgressDialog.show();
        }
        Log.d(TAG, "Fetching and decrypting existing conversation key VERSIONS for " + conversationId);

        chatExecutor.execute(() -> {
            int successfulDecryptions = 0;
            String lastErrorMessage = null;

            // --- Iterate through ALL key version snapshots under the "key_versions" node ---
            for (DataSnapshot keyVersionSnap : keyVersionsSnapshot.getChildren()) {
                String timestampKey = keyVersionSnap.getKey();
                long keyTimestamp;
                try {
                    keyTimestamp = Long.parseLong(timestampKey);
                    if (keyTimestamp <= 0) throw new NumberFormatException("Invalid timestamp value: " + timestampKey);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Skipping key version with invalid timestamp key in Firebase: " + timestampKey + " for conv ID: " + conversationId, e);
                    continue;
                }

                Log.d(TAG, "Processing Firebase key version snapshot with timestamp: " + keyTimestamp + " for conv ID: " + conversationId);

                SecretKey conversationAESKey = null;
                byte[] decryptedAesKeyBytes = null;

                try {
                    if (keyVersionSnap.hasChild(currentUserId)) {
                        String encryptedAesKeyForCurrentUserBase64 = keyVersionSnap.child(currentUserId).getValue(String.class);

                        if (TextUtils.isEmpty(encryptedAesKeyForCurrentUserBase64)) {
                            Log.w(TAG, "Encrypted key data is empty for user " + currentUserId + " in key version " + keyTimestamp + " in Firebase for conv " + conversationId);
                            lastErrorMessage = "Key data empty for version " + keyTimestamp;
                            continue;
                        }

                        byte[] encryptedAesKeyForCurrentUserBytes = android.util.Base64.decode(encryptedAesKeyForCurrentUserBase64, android.util.Base64.DEFAULT);

                        PrivateKey currentUserPrivateKey = YourKeyManager.getInstance().getUserPrivateKey();

                        if (currentUserPrivateKey != null) {

                            decryptedAesKeyBytes = CryptoUtils.decryptWithRSA(encryptedAesKeyForCurrentUserBytes, currentUserPrivateKey);
                            conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedAesKeyBytes);

                            YourKeyManager.getInstance().setConversationKey(conversationId, conversationAESKey); // *** CORRECTED KEYMANAGER CALL ***
                            Log.d(TAG, "Key version " + keyTimestamp + " decrypted from Firebase and loaded into KeyManager for conv ID: " + conversationId);
                            successfulDecryptions++;


                            if (decryptedAesKeyBytes != null) {
                                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Saving fetched/decrypted key version to Room DB.");
                                String decryptedKeyBase64ForRoom = android.util.Base64.encodeToString(decryptedAesKeyBytes, android.util.Base64.DEFAULT);
                                ConversationKeyEntity keyEntityToSave = new ConversationKeyEntity(currentUserId, conversationId, decryptedKeyBase64ForRoom);
                                ChatDatabase.databaseWriteExecutor.execute(() -> {
                                    try {
                                        conversationKeyDao.insertOrUpdateKey(keyEntityToSave);
                                        Log.d(TAG, "Key version " + keyTimestamp + " saved to Room after fetching/decrypting from Firebase for conv ID: " + conversationId);
                                    } catch (Exception saveEx) {
                                        Log.e(TAG, "Error saving fetched key version " + keyTimestamp + " to Room DB", saveEx);
                                    }
                                });
                            } else {
                                Log.e(TAG, "Skipping save to Room DB: Decrypted AES key bytes were null after successful decryption (unexpected state). Key version: " + keyTimestamp);
                            }

                        } else {
                            lastErrorMessage = "Your private key is not available to decrypt conversation keys.";
                            Log.e(TAG, "Current user's private key is null during conversation key decryption for version " + keyTimestamp + ".");
                        }

                    } else {
                        Log.w(TAG, "Encrypted key for user " + currentUserId + " NOT found in Firebase key version snapshot: " + keyVersionSnap.getKey() + " for conv ID: " + conversationId + ".");
                        lastErrorMessage = "Key entry missing for your account in version " + keyVersionSnap.getKey();
                    }

                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Base64 decoding error during conversation key decryption for version " + keyVersionSnap.getKey() + ": " + e.getMessage(), e);
                    lastErrorMessage = "Error decoding key data for version " + keyVersionSnap.getKey();
                } catch (NoSuchAlgorithmException e) {
                    Log.w(TAG, "Error converting decrypted bytes to SecretKey for version " + keyVersionSnap.getKey() + ": " + e.getMessage(), e);
                    lastErrorMessage = "Error processing key for version " + keyVersionSnap.getKey();
                }
                catch (NoSuchPaddingException | InvalidKeyException |
                       IllegalBlockSizeException | BadPaddingException e) {
                    Log.w(TAG, "Crypto error during conversation key decryption for version " + keyVersionSnap.getKey() + ": " + e.getMessage(), e);
                    lastErrorMessage = "Failed to decrypt key for version " + keyVersionSnap.getKey();
                } catch (Exception e) {
                    Log.w(TAG, "Unexpected error during conversation key fetch/decrypt for version " + keyVersionSnap.getKey() + ": " + e.getMessage(), e);
                    lastErrorMessage = "An error occurred loading key for version " + keyVersionSnap.getKey();
                }
            }

            final String finalErrorMessage = lastErrorMessage;
            final int finalSuccessfulDecryptions = successfulDecryptions;
            final String finalConversationId = conversationId;
            final String finalRecipientUserId = recipientUserId;
            final String finalRecipientName = recipientName;
            final String finalRecipientImageBase64 = recipientImageBase64;

            chatHandler.post(() -> {
                if (chatSetupProgressDialog != null && chatSetupProgressDialog.isShowing()) chatSetupProgressDialog.dismiss();

                if (finalSuccessfulDecryptions > 0) {
                    Log.d(TAG, "fetchAndDecryptConversationKeyAsync (Main Thread): Successfully loaded " + finalSuccessfulDecryptions + " key version(s) into KeyManager. Navigating to chat.");
                    Toast.makeText(getContext(), "Secure chat loaded.", Toast.LENGTH_SHORT).show();

                    navigateToChatActivity(finalConversationId, finalRecipientUserId, finalRecipientName, finalRecipientImageBase64);

                } else {
                    Log.e(TAG, "fetchAndDecryptConversationKeyAsync (Main Thread): Failed to load ANY conversation key versions for conv ID: " + finalConversationId + ". Last Error: " + finalErrorMessage);
                    String displayMessage = finalErrorMessage != null ? finalErrorMessage : "Failed to load chat keys.";
                    Toast.makeText(getContext(), displayMessage + " Secure chat unavailable.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // ... (rest of MyContactsFragment.java) ...



    // --- Helper method to Navigate to Chat Activity (Keep This) ---
    // This method is called upon successful key setup/retrieval, regardless of whether
    // the key was generated, fetched/decrypted from Firebase, or found in KeyManager cache.
    private void navigateToChatActivity(String conversationId, String recipientUserId, String recipientName, String recipientImageBase64) {
        // Ensure context is still valid before starting activity
        if (getContext() == null) {
            Log.e(TAG, "Cannot navigate to chat, context is null.");
            return;
        }
        Log.d(TAG, "Navigating to ChatPageActivity for conversationId: " + conversationId);
        Intent intent = new Intent(getContext(), ChatPageActivity.class);
        intent.putExtra("conversationId", conversationId); // Pass the consistent conversation ID
        // Keeping old keys for compatibility with your ChatPageActivity if it still uses them
        intent.putExtra("visit_users_ids", recipientUserId);
        intent.putExtra("visit_users_name", recipientName);
        intent.putExtra("visit_users_image", recipientImageBase64);
        // Add flags to ensure clean task stack if needed (e.g., if you don't want to go back to contacts easily)
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Example
        startActivity(intent);
    }
    // --- Keep Helper Navigation ---


    // --- NEW Standard RecyclerView Adapter (Keep This) ---
    // This static inner class defines the adapter for displaying ContactEntity objects in the RecyclerView.
    // This replaces the FirebaseRecyclerAdapter.
    public static class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

        private List<ContactEntity> contacts; // Data list for the adapter (populated by LiveData)
        private Context context; // Context needed for Glide etc.
        private OnContactClickListener listener; // Listener for item clicks (implemented by the fragment)


        // Interface for click listener (defined within the adapter)
        public interface OnContactClickListener {
            void onContactClick(ContactEntity contact); // Method called when an item/button is clicked
        }

        // Constructor for the adapter
        public ContactAdapter(List<ContactEntity> contacts, Context context, OnContactClickListener listener) {
            this.contacts = contacts; // Initial empty list
            this.context = context;
            this.listener = listener; // The fragment's implementation
        }

        // Method to update the data list in the adapter and notify the adapter
        // Called from the LiveData observer in the fragment whenever Room data changes.
        public void setContacts(List<ContactEntity> newContacts) {
            // Using DiffUtil here would be more efficient for large lists and animations,
            // but for simplicity, we just replace the list and notify.
            this.contacts = newContacts; // Replace the internal list with new data from Room
            notifyDataSetChanged(); // Notify adapter that data has changed, causing UI refresh
        }


        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout for a single list item view
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_friends_display_layout, parent, false);
            return new ContactViewHolder(view); // Return a new ViewHolder instance
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            // Get the ContactEntity data for the current position
            ContactEntity contact = contacts.get(position);

            // Bind data from the ContactEntity to the views in the ViewHolder
            holder.userName.setText(contact.getName());
            holder.userStatus.setText(contact.getStatus());

            // Load profile image using Glide. Supports Base64 string from Room.
            if (!TextUtils.isEmpty(contact.getProfileImageBase64())) {
                // Use context passed in adapter constructor (Application Context is ideal)
                Glide.with(context)
                        .load("data:image/jpeg;base64," + contact.getProfileImageBase64()) // Load from Base64 data URI string
                        .placeholder(R.drawable.default_profile_img) // Placeholder image while loading
                        .error(R.drawable.default_profile_img) // Image to show if loading fails (e.g., invalid Base64)
                        .into(holder.profileImage);
            } else {
                // Set default image if Base64 string is null or empty
                holder.profileImage.setImageResource(R.drawable.default_profile_img);
            }

            // Set online status indicator visibility based on ContactEntity data
            holder.onlineIcon.setVisibility(contact.isOnline() ? View.VISIBLE : View.INVISIBLE);


            // >>>>>>>>>>>>>>>> SET CLICK LISTENERS <<<<<<<<<<<<<<<<<<

            // Option 1: Make the WHOLE ITEM clickable (like the old Firebase adapter likely did)
            // This is the common behavior: clicking anywhere on the contact row opens the chat.
            // If you want this, ensure the listener is set here AND remove the button's listener below.
            holder.itemView.setOnClickListener(v -> {
                Log.d(TAG, "Contact item view clicked for: " + contact.getName());
                if (listener != null) {
                    listener.onContactClick(contact); // Call the fragment's listener method
                }
            });

            // Option 2: Make ONLY THE BUTTON clickable to open chat.
            // If you want only the button to work, COMMENT OUT Option 1 above and UNCOMMENT the code below.
            // Also, set the button to be visible if your layout has it.
            if(holder.sendMessageBtn != null) {
                holder.sendMessageBtn.setVisibility(View.VISIBLE); // Make button visible

                // Attach click listener to the BUTTON INSTEAD OF THE WHOLE ITEM
                 /*
                 holder.sendMessageBtn.setOnClickListener(v -> {
                     Log.d(TAG, "Send message button clicked for contact: " + contact.getName());
                     if (listener != null) {
                         // Call the same listener method implemented in the fragment
                         listener.onContactClick(contact);
                     }
                 });
                 */



                holder.sendMessageBtn.setOnClickListener(v -> {
                    Log.d(TAG, "Send message button clicked for contact: " + contact.getName());
                    // Check if the listener from the fragment is set
                    if (listener != null) {
                        // Call the listener method implemented in the fragment.
                        // The fragment's implementation of onContactClick will then call startSecureChatWithUser.
                        listener.onContactClick(contact);

                    } else {
                        Log.w(TAG, "OnContactClickListener is null. Cannot open chat from button click.");
                        // Optionally show a Toast here if the listener is null unexpectedly
                        // Toast.makeText(context, "Chat functionality not available.", Toast.LENGTH_SHORT).show();
                    }
                });
                // <<< END


            } else {
                // If the button is not found in the layout, log a warning
                Log.w(TAG, "Send message button not found in layout for contact item.");
                // If the button is the *only* intended way to start chat, and it's missing,
                // you might want to disable the item click fallback by doing:
                // holder.itemView.setOnClickListener(null);
            }
            // Decide between Option 1 (Item click) and Option 2 (Button click only) above.
            // As requested to keep previous functionality, Option 1 (Item click) is likely closer,
            // assuming your old Firebase adapter made the whole item clickable.
            // I will leave Option 1 active and Option 2 commented out, but the button is visible.
            // If you want only the button to work, comment Option 1 and uncomment Option 2 + setVisible(true).

            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        }

        @Override
        public int getItemCount() {
            // Return the total number of items in the data list
            return contacts.size();
        }

        // ViewHolder class (should match the IDs in users_friends_display_layout)
        public static class ContactViewHolder extends RecyclerView.ViewHolder {
            TextView userName, userStatus;
            CircleImageView profileImage;
            ImageView onlineIcon;
            Button sendMessageBtn; // Assuming this button exists in the layout users_friends_display_layout

            public ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                // Find the views in the item layout by their IDs
                userName = itemView.findViewById(R.id.users_profile_name);
                userStatus = itemView.findViewById(R.id.users_profile_status);
                profileImage = itemView.findViewById(R.id.users_profile_image);
                onlineIcon = itemView.findViewById(R.id.users_online_status);
                // Find the button if it exists in the layout
                sendMessageBtn = itemView.findViewById(R.id.send_message_btn);
            }
        }
    }
    // --- END NEW Standard RecyclerView Adapter ---






    // In MyContactsFragment.java, add this new public method:

    /**
     * Forces the contacts list to re-render its current data from the LiveData's last value.
     * Useful when decryption keys become available after the data was initially loaded,
     * or when the contacts list state needs to be updated visually.
     * Assumes the LiveData's last value is still in the `contactListLiveData` object.
     */

    public void forceRefreshDisplay() {
        Log.d(TAG, "forceRefreshDisplay() called in MyContactsFragment.");
        // Ensure LiveData has a value and adapter exists
        if (contactListLiveData != null && contactListLiveData.getValue() != null && contactAdapter != null) {
            Log.d(TAG, "Re-submitting current LiveData value (" + contactListLiveData.getValue().size() + " items) to ContactAdapter.");

            // Get the current list of ContactEntity items from the LiveData's last value
            List<ContactEntity> currentItems = contactListLiveData.getValue();

            // Clear the adapter's internal list
            contactsArrayList.clear();
            // Add the current items from the LiveData's value
            if (currentItems != null) { // Safety check
                contactsArrayList.addAll(currentItems);
            }


            // Notify the adapter that the data set has changed
            contactAdapter.notifyDataSetChanged(); // This will cause onBindViewHolder to be called again
            Log.d(TAG, "Submitted " + contactsArrayList.size() + " items to ContactAdapter in forceRefreshDisplay.");


            // Re-apply search filter if active (if applicable in this fragment)
            // If your ContactAdapter has a filter method and search is active in this fragment, re-apply it.
             /*
             if (searchView != null && contactAdapter != null) { // Assuming searchView is a member
                 String currentQuery = searchView.getQuery().toString();
                 if (!currentQuery.isEmpty()) {
                      Log.d(TAG, "Applying search filter after force refresh: '" + currentQuery + "'");
                      contactAdapter.filter(currentQuery); // Assuming filter method exists
                 }
             }
             */

            // Update UI visibility based on the list size (same logic as in onChanged)
            if (contactsArrayList.size() > 0) {
                myContactsList.setVisibility(View.VISIBLE); // Corrected RecyclerView variable name if needed
                noChatsText.setVisibility(View.GONE);
                // If search is present in this fragment layout, show it
                // if (searchView != null) searchView.setVisibility(View.VISIBLE); // Assuming search is in this fragment's layout
            } else {
                myContactsList.setVisibility(View.GONE); // Corrected RecyclerView variable name if needed
                noChatsText.setVisibility(View.VISIBLE);
                noChatsText.setText("No contacts found."); // Set appropriate text for empty list
                // If search is present in this fragment layout, hide it
                // if (searchView != null) searchView.setVisibility(View.GONE); // Assuming search is in this fragment's layout
            }


        } else {
            Log.w(TAG, "forceRefreshDisplay() skipped in MyContactsFragment: LiveData, value, or adapter is null.");
            // If LiveData has no value, the onChanged would likely have run already and updated the UI.
        }
    }



    // The original ContactsViewHolder class that was used with FirebaseRecyclerAdapter
    // is no longer needed and should be removed or commented out if it was defined outside the new ContactAdapter class.
    // public static class ContactsViewHolder extends RecyclerView.ViewHolder {...} // REMOVE OR COMMENT OUT THIS CLASS if it's a duplicate

}



