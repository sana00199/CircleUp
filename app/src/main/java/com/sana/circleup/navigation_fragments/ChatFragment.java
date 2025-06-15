

package com.sana.circleup.navigation_fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.sana.circleup.ChatAdapter;
import com.sana.circleup.ChatPageActivity;
import com.sana.circleup.Login;
import com.sana.circleup.ProfileUserInfoActivity;
import com.sana.circleup.R;
import com.sana.circleup.encryptionfiles.CryptoUtils;
import com.sana.circleup.encryptionfiles.YourKeyManager;
import com.sana.circleup.one_signal_notification.OneSignalApiService;
import com.sana.circleup.room_db_implement.ChatDao;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.ChatEntity;
import com.sana.circleup.room_db_implement.ConversationKeyDao;
import com.sana.circleup.room_db_implement.ConversationKeyEntity;
import com.sana.circleup.room_db_implement.MessageDao;
import com.sana.circleup.room_db_implement.MessageEntity;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;






import java.util.Set; // Import Set
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import javax.crypto.SecretKey;
import android.util.Base64; // Use Android's Base64

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
//
//
//public class ChatFragment extends Fragment implements ChatAdapter.OnChatInteractionListener {
//
//    private static final String TAG = "ChatFragment";
//
//    private View privateChatsView;
//    private RecyclerView privateChatsList;
//    private TextView noChatsText;
//    private SearchView searchView; // Use SearchView from androidx.appcompat.widget
//
//    private DatabaseReference rootRef;
//    private DatabaseReference usersRef;
//    private DatabaseReference chatSummariesRef; // Reference to the current user's chat summaries
//
//    private FirebaseAuth mAuth;
//    private String currentUserID; // Current logged-in user's UID
//
//    // --- Room DB and DAO members ---
//    private ChatDatabase chatDatabase; // Room DB
//    private ChatDao chatDao; // Room DAO for Chat list items
//    private MessageDao messageDao; // Room DAO for individual messages (for deleting history)
//    private ExecutorService databaseExecutor; // Executor for Room DB operations
//
//
//
//    // ... (Existing members) ...
//
//    // --- Members for Secure Chat Initiation from list click ---
//    private ExecutorService chatOpenExecutor; // For async key loading specific to click
//    private Handler chatOpenHandler; // For posting UI updates back to Main Thread for click
//    private ProgressDialog chatOpenProgressDialog; // Progress dialog for chat setup process
//// --- End Members ---
//
//    private ConversationKeyDao conversationKeyDao; // <<< Make sure this is also added and initialized in onCreate
//    private ChatAdapter chatAdapter; // Adapter for the RecyclerView
//
//    // LiveData from Room
//    private LiveData<List<ChatEntity>> chatListLiveData;
//
//    // Firebase listener
//    private ValueEventListener chatSummaryListener; // Firebase listener for ChatSummaries
//
//    // Keep track of Firebase listener attachment state
//    private boolean isChatSummaryListenerAttached = false; // <<< ADD THIS FLAG
//
//    @SuppressLint("RestrictedApi") // Needed for setHasOptionsMenu if used with older support library, remove if targeting newer AndroidX
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Log.d(TAG, "onCreate started.");
//        setHasOptionsMenu(true); // Optional: If you want to handle options menu for search etc.
//
//
//        // --- Initialize Firebase Auth ---
//        mAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            currentUserID = currentUser.getUid();
//            Log.d(TAG, "Current User ID: " + currentUserID);
//        } else {
//            Log.e(TAG, "User not authenticated in ChatFragment onCreate! This fragment should not be visible in this state.");
//            currentUserID = null; // Ensure currentUserID is null if user is not authenticated
//            // Handle authentication error - e.g., redirect to login or show an error screen
//            if (getContext() != null) {
//                Toast.makeText(getContext(), "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
//                // Consider navigating to login activity if this fragment is reached without auth
//                // startActivity(new Intent(getContext(), Login.class)); // Ensure Login activity exists
//                // if (getActivity() != null) getActivity().finish();
//            }
//        }
//        // --- End Firebase Auth ---
//
//
//        // --- Initialize Firebase Refs ---
//        // Only initialize if currentUserID is available
//        if (currentUserID != null) {
//            rootRef = FirebaseDatabase.getInstance().getReference();
//            usersRef = rootRef.child("Users");
//            // Reference to the node containing chat summaries for THIS user
//            // Structure: ChatSummaries/{currentUserID}/{chatPartnerId}/...
//            chatSummariesRef = rootRef.child("ChatSummaries").child(currentUserID); // Corrected path from previous code
//            Log.d(TAG, "ChatSummaries Ref: " + chatSummariesRef.getPath()); // Log path
//        } else {
//            Log.w(TAG, "currentUserID is null, Firebase Refs will not be initialized in onCreate.");
//            rootRef = null;
//            usersRef = null;
//            chatSummariesRef = null;
//        }
//        // --- End Initialize Firebase Refs ---
//
//
//        // --- Initialize Room DB and Executors ---
//        // Always initialize Room DB as it provides offline data even if not authenticated,
//        // although you won't see user-specific data without a valid owner ID.
//        chatDatabase = ChatDatabase.getInstance(requireContext()); // Use requireContext() in Fragment
//        chatDao = chatDatabase.chatDao();
//        messageDao = chatDatabase.messageDao(); // Initialize MessageDao
//        conversationKeyDao = chatDatabase.conversationKeyDao(); // <<< INITIALIZE THE NEW DAO HERE
//        databaseExecutor = ChatDatabase.databaseWriteExecutor; // Use the shared executor
//        // --- End Initialize Room DB ---
//
//
//
//
//        // --- Initialize Executor and Handler for chat opening async tasks ---
//        chatOpenExecutor = Executors.newSingleThreadExecutor(); // Dedicated executor for click tasks
//        chatOpenHandler = new Handler(Looper.getMainLooper()); // Handler for UI updates after click tasks
//        // Initialize dialog (use requireContext() as it's available in Fragment onCreate)
//        chatOpenProgressDialog = new ProgressDialog(requireContext());
//        chatOpenProgressDialog.setCancelable(false);
//        // Make sure it's owned by the parent activity to prevent window leaks
//        if (getActivity() != null) {
//            chatOpenProgressDialog.setOwnerActivity(getActivity());
//        }
//        // --- End Initialization ---
//
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.d(TAG, "onCreateView started.");
//        // Assuming R.layout.fragment_chat contains R.id.chat_list, R.id.noChatsText, R.id.search_view
//        privateChatsView = inflater.inflate(R.layout.fragment_chat, container, false); // Ensure fragment_chat.xml exists
//
//        // Initialize UI views
//        privateChatsList = privateChatsView.findViewById(R.id.chat_list); // Match your layout ID
//        noChatsText = privateChatsView.findViewById(R.id.noChatsText); // Match your layout ID
//        searchView = privateChatsView.findViewById(R.id.search_view); // Make sure ID matches layout
//
//        setupRecyclerView(); // Setup RecyclerView and Adapter
//        setupSearchView(); // Setup SearchView
//
//        return privateChatsView;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        Log.d(TAG, "onViewCreated started.");
//
//        // Check authentication again here, as the view might be created after onCreate.
//        // If currentUserID is null, display an error state.
//        if (currentUserID == null) {
//            Log.e(TAG, "Cannot proceed in ChatFragment onViewCreated without currentUserID. Showing error UI.");
//            noChatsText.setVisibility(View.VISIBLE);
//            noChatsText.setText("Authentication Error");
//            privateChatsList.setVisibility(View.GONE);
//            if (searchView != null) searchView.setVisibility(View.GONE); // Hide search if no user
//            // Also hide bottom navigation if applicable in the parent Activity or FragmentManager
//            return; // Stop execution in this method
//        }
//
//
//        // --- Observe Room LiveData for changes ---
//        // This observer runs on the main thread when data changes in Room.
//        // Decryption of the last message preview happens *inside* this observer.
//        observeChatList(); // Attach Room LiveData observer
//
//        // --- Attach Firebase Listener for ChatSummaries ---
//        // This listener syncs data from Firebase ChatSummaries to Room.
//        // It should be attached whenever the fragment's view is active (onViewCreated or onStart).
//        // It should be detached in onDestroyView or onStop.
//        // We attach the listener regardless of network status. Firebase handles offline caching/sync.
//        Log.d(TAG, "Attaching Firebase listener for ChatSummaries in onViewCreated.");
//        attachChatSummaryListener(); // Attach the listener
//
//        // Show initial loading text until Room data loads or sync happens
//        noChatsText.setVisibility(View.VISIBLE);
//        noChatsText.setText("Loading chats...");
//        privateChatsList.setVisibility(View.GONE);
//        if (searchView != null) searchView.setVisibility(View.GONE);
//
//        // Check network status for informational toast only (listener works offline)
//        if (!isNetworkAvailable()) {
//            Log.d(TAG, "Network unavailable in onViewCreated. Relying on cached data from Room.");
//            if (getContext() != null) {
//                // Show toast only once when network is unavailable (maybe use a flag or ViewModel to show only once)
//                Toast.makeText(getContext(), "Offline mode: Showing cached chats", Toast.LENGTH_SHORT).show();
//            }
//            // LiveData observer is already attached and will show cached data.
//        }
//    }
//
//
//    // --- Setup RecyclerView and Adapter ---
//    private void setupRecyclerView() {
//        Log.d(TAG, "Setting up RecyclerView.");
//        privateChatsList.setLayoutManager(new LinearLayoutManager(getContext()));
//        // Initialize ChatAdapter with context, empty list, listener, AND currentUserID
//        // Pass currentUserID here. This ID is used by the adapter to determine message alignment/sender.
//        // Use requireContext() as onCreateView guarantees context is available.
//        chatAdapter = new ChatAdapter(requireContext(), new ArrayList<>(), this, currentUserID); // Pass currentUserID here
//        privateChatsList.setAdapter(chatAdapter);
//    }
//
//    // --- Setup SearchView ---
//    private void setupSearchView() {
//        Log.d(TAG, "Setting up SearchView.");
//        // Use androidx.appcompat.widget.SearchView
//        if (searchView != null) { // Safety check
//            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                @Override
//                public boolean onQueryTextSubmit(String query) {
//                    return false; // Don't handle submission by default
//                }
//
//                @Override
//                public boolean onQueryTextChange(String newText) {
//                    Log.d(TAG, "Search query changed: " + newText);
//                    // Check if chatAdapter is not null before filtering
//                    if (chatAdapter != null) {
//                        // The adapter's filter method should work on the list containing *displayed* previews
//                        // Assuming ChatAdapter's filter method filters based on username or the displayed last message preview.
//                        chatAdapter.filter(newText); // Assuming your ChatAdapter has a filter method
//                    } else {
//                        Log.w(TAG, "ChatAdapter is null, cannot apply filter.");
//                    }
//                    return true; // Query handled
//                }
//            });
//            // Add a listener for when the search view is closed
//            searchView.setOnCloseListener(() -> {
//                Log.d(TAG, "SearchView closed.");
//                // Optionally reset the filter or load full data here if your filter implementation needs it
//                // chatAdapter.filter(""); // Example: reset filter to empty string
//                return false; // Allow the system to handle closing the search view
//            });
//        } else {
//            Log.w(TAG, "SearchView is null, skipping setup.");
//        }
//    }
//
//
//    // --- Observe LiveData from Room DAO ---
//    private void observeChatList() {
//        // Ensure currentUserID and chatDao are initialized before observing
//        if (currentUserID == null || chatDao == null) {
//            Log.e(TAG, "currentUserID or chatDao is null, cannot observe chat list.");
//            // Optionally show an error message in UI
//            if (noChatsText != null) {
//                noChatsText.setVisibility(View.VISIBLE);
//                noChatsText.setText("Error loading chats: User not logged in.");
//                privateChatsList.setVisibility(View.GONE);
//                if (searchView != null) searchView.setVisibility(View.GONE);
//            }
//            return; // Exit method if prerequisites are missing
//        }
//        Log.d(TAG, "Attaching Room LiveData observer for user: " + currentUserID);
//        // Get the LiveData from the DAO for chats owned by the current user, ordered by latest message timestamp
//        // Make sure your ChatDao.getAllChats method queries WHERE ownerUserId = :ownerUserId ORDER BY timestamp DESC
//        chatListLiveData = chatDao.getAllChats(currentUserID); // Assuming this method exists and takes ownerId
//
//        // Use getViewLifecycleOwner() for fragment-specific lifecycle.
//        // The observer will be automatically removed when the fragment's view is destroyed.
//        chatListLiveData.observe(getViewLifecycleOwner(), new Observer<List<ChatEntity>>() { // Use Observer interface
//            @Override
//            public void onChanged(List<ChatEntity> chatEntities) {
//                // This observer runs on the main thread whenever the data in Room changes
//                Log.d(TAG, "LiveData onChanged triggered. Received " + (chatEntities != null ? chatEntities.size() : 0) + " chats from Room for owner: " + currentUserID);
//
//                // Create a new list to hold ChatEntity objects with processed content for the adapter
//                // It's safer to create a new list than modify the one provided by LiveData
//                List<ChatEntity> processedChatList = new ArrayList<>();
//
//                if (chatEntities != null) {
//                    for (ChatEntity chat : chatEntities) {
//                        // Ensure chat entity is not null
//                        if (chat == null) {
//                            Log.w(TAG, "Skipping null ChatEntity from Room list.");
//                            continue;
//                        }
//                        // --- Get all necessary fields from the original Room ChatEntity ---
//                        String encryptedLastMessage = chat.getLastMessage(); // This is the encrypted Base64 string or placeholder from Room
//                        String chatPartnerId = chat.getUserId();
//                        String conversationId = chat.getConversationId(); // Get the conversation ID
//                        String lastMessageType = chat.getLastMessageType(); // Get lastMessageType from ChatEntity
//                        String username = chat.getUsername();
//                        String profileImage = chat.getProfileImage();
//                        long timestamp = chat.getTimestamp();
//                        int unreadCount = chat.getUnreadCount();
//                        String ownerUserId = chat.getOwnerUserId(); // Should be currentUserID
//
//
//                        String displayedLastMessagePreview = ""; // This will be the string shown in the list item
//
//                        // --- Logic to determine what to display for the last message preview ---
//                        // Default placeholder based on type if content is empty or decryption fails
//                        String placeholder = "";
//                        if ("image".equals(lastMessageType)) placeholder = "[Image]";
//                        else if ("file".equals(lastMessageType)) placeholder = "[File]";
//                        else placeholder = ""; // Default for text or unknown
//
//
//                        // Check if the stored content is empty. If so, just show the placeholder.
//                        if (TextUtils.isEmpty(encryptedLastMessage)) {
//                            displayedLastMessagePreview = placeholder; // Show placeholder if no content
//                        } else if ("text".equals(lastMessageType) || "image".equals(lastMessageType)) { // Only attempt decryption for message types that are encrypted
//                            // Attempt decryption if user's private key is available AND the conversation key is available in KeyManager's cache
//                            // The conversation key should have been loaded into KeyManager after account unlock (in Login/MainActivity)
//                            if (YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId)) {
//                                Log.d(TAG, "Attempting decryption for last message preview in chat " + conversationId + " with partner " + chatPartnerId);
//                                SecretKey conversationAESKey = YourKeyManager.getInstance().getLatestConversationKey(conversationId); // Get the key from KeyManager's memory cache
//
//                                // Double check key is not null after retrieving (should be handled by hasConversationKey, but defensive)
//                                if (conversationAESKey != null) {
//                                    try {
//                                        // encryptedLastMessage is the Base64 encoded encrypted data from Room
//                                        byte[] encryptedBytes = Base64.decode(encryptedLastMessage, Base64.DEFAULT); // Use android.util.Base64 for decoding
//                                        String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytes, conversationAESKey); // Use your CryptoUtils method
//
//                                        if ("text".equals(lastMessageType)) {
//                                            // Show only a snippet for long text messages
//                                            int maxLength = 50; // Display max 50 characters in preview
//                                            displayedLastMessagePreview = decryptedContent.length() > maxLength ?
//                                                    decryptedContent.substring(0, maxLength) + "..." :
//                                                    decryptedContent; // Use decrypted text or snippet
//                                            // Log.d(TAG, "Last message preview decrypted successfully: " + displayedLastMessagePreview);
//
//                                        } else if ("image".equals(lastMessageType)) {
//                                            displayedLastMessagePreview = placeholder; // For image, show placeholder like "[Image]" in the list preview
//                                            // The actual image data is not stored in the lastMessage preview field of ChatEntity
//                                            // Log.d(TAG, "Last message preview decrypted (image), showing placeholder.");
//
//                                        } else { // Should not happen based on outer if condition, but fallback
//                                            displayedLastMessagePreview = "[Unknown Type - Decrypted]";
//                                            Log.w(TAG, "Unexpected message type '" + lastMessageType + "' processed for decryption in chat " + conversationId);
//                                        }
//
//                                    } catch (IllegalArgumentException e) { // Base64 decoding error
//                                        Log.e(TAG, "Base64 decoding error decrypting last message preview for chat " + conversationId, e);
//                                        displayedLastMessagePreview = "[Invalid Data]"; // Placeholder on Base64 error
//                                    } catch (Exception e) { // Catch decryption errors (wrong key, corrupt data, padding issues)
//                                        Log.e(TAG, "Decryption failed for last message preview in chat " + conversationId, e);
//                                        displayedLastMessagePreview = "[Encrypted Message - Failed]"; // Placeholder on crypto error
//                                    }
//                                } else {
//                                    // Conversation key missing from KeyManager (shouldn't happen if hasConversationKey check passed, but defensive)
//                                    Log.w(TAG, "Conversation key returned null from KeyManager for chat " + conversationId + " during decryption attempt.");
//                                    displayedLastMessagePreview = "[Encrypted Message]"; // Placeholder
//                                }
//                            } else {
//                                // User's private key is not available OR conversation key not loaded in KeyManager's cache
//                                // This means we cannot decrypt the message for display in the list preview.
//                                // It should be loaded from Room after unlock, so if it's missing here, it indicates a potential issue
//                                // or the user skipped the security setup/unlock.
//                                Log.d(TAG, "User's private key unavailable (" + !YourKeyManager.getInstance().isPrivateKeyAvailable() + ") OR Conversation key not loaded (" + !YourKeyManager.getInstance().hasConversationKey(conversationId) + "). Cannot decrypt text preview for chat " + conversationId);
//                                displayedLastMessagePreview = "[Locked]"; // Placeholder indicating account needs unlocking / keys not loaded
//                            }
//
//                        } else {
//                            // Message type is not text or image (e.g., file, or an unencrypted system message)
//                            // Or it's a type we don't decrypt for preview (like a placeholder).
//                            // Display content as is from Room (which could be a placeholder like "[File]")
//                            displayedLastMessagePreview = encryptedLastMessage; // Use the content stored in Room
//                            // If the content from Room is empty, use the type placeholder as a fallback
//                            if (TextUtils.isEmpty(displayedLastMessagePreview)) {
//                                displayedLastMessagePreview = placeholder;
//                            }
//                        }
//
//
//                        // --- Create a NEW ChatEntity object for the adapter ---
//                        // Create a new entity and copy all necessary fields, setting the PROCESSED last message.
//                        // This ensures LiveData doesn't get modified entities if it's an unmodifiable list.
//                        ChatEntity processedChat = new ChatEntity(); // Use empty constructor
//                        processedChat.setId(chat.getId()); // Make sure to copy Room auto-generated ID
//                        processedChat.setOwnerUserId(ownerUserId); // Keep the owner ID
//                        processedChat.setUserId(chatPartnerId); // Keep the chat partner ID
//                        processedChat.setConversationId(conversationId); // Keep the conversation ID
//                        processedChat.setUsername(username); // Keep the username
//                        processedChat.setProfileImage(profileImage); // Keep the profile image
//                        processedChat.setLastMessage(displayedLastMessagePreview); // *** Set the PROCESSED/DECRYPTED preview content ***
//                        processedChat.setTimestamp(timestamp); // Keep the timestamp
//                        processedChat.setUnreadCount(unreadCount); // Keep the unread count
//                        processedChat.setLastMessageType(lastMessageType); // Keep the message type
//
//
//                        processedChatList.add(processedChat); // Add the processed entity to the list for the adapter
//                    }
//                }
//                // --- END Processing Messages ---
//
//                // Sort the list by timestamp DESC (latest message first) if not already sorted by DAO query
//                // Make sure your DAO query already sorts by timestamp DESC, then this is redundant but safe.
//                Collections.sort(processedChatList, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
//
//
//                // Submit the list of ChatEntity with processed last messages to the adapter
//                if (chatAdapter != null) {
//                    // Use submitList (more efficient with DiffUtil if implemented in adapter). Pass a *copy* if needed.
//                    // Using ArrayList<>(processedChatList) ensures a new list is passed, preventing issues with adapter's internal list reference.
//                    chatAdapter.submitList(new ArrayList<>(processedChatList));
//                    Log.d(TAG, "Submitted " + processedChatList.size() + " processed chats to adapter.");
//                } else {
//                    Log.e(TAG, "ChatAdapter is null, cannot submit list.");
//                }
//
//
//                // Update UI visibility based on the final list size
//                if (processedChatList.size() > 0) { // Check size > 0 for non-empty list
//                    privateChatsList.setVisibility(View.VISIBLE);
//                    noChatsText.setVisibility(View.GONE);
//                    // Show search view only if there are chats to search
//                    if (searchView != null) searchView.setVisibility(View.VISIBLE);
//                    Log.d(TAG, "Chat list visible.");
//                } else {
//                    privateChatsList.setVisibility(View.GONE);
//                    noChatsText.setVisibility(View.VISIBLE);
//                    noChatsText.setText("No chats yet"); // Message when list is empty
//                    // Hide search view if there are no chats
//                    if (searchView != null) searchView.setVisibility(View.GONE);
//                    Log.d(TAG, "No chats found after LiveData update, showing No chats text.");
//                }
//
//                // Re-apply search filter if there's an active query
//                // The adapter's filter method should work on the list containing *displayed* previews
//                if (searchView != null && chatAdapter != null) {
//                    String currentQuery = searchView.getQuery().toString();
//                    if (!currentQuery.isEmpty()) {
//                        Log.d(TAG, "Applying search filter after LiveData update: '" + currentQuery + "'");
//                        // Ensure adapter's filter method works with the list containing processed previews
//                        // The adapter's filter logic should compare the *displayed* last message preview text
//                        chatAdapter.filter(currentQuery);
//                    }
//                } else {
//                    Log.w(TAG, "SearchView or ChatAdapter is null, cannot re-apply filter.");
//                }
//            }
//        });
//    }
//    // --- End Observe LiveData ---
//
//
//    // --- Attach Real-time Listener for Firebase Chat Summaries (Keep This) ---
//    // This listener listens to the current user's chat summaries node in Firebase
//    // and syncs changes to the local Room database.
//    // @SuppressLint("RestrictedApi") // Keep if needed for any hidden APIs used internally by Firebase library
//    @SuppressLint("RestrictedApi")
//    private void attachChatSummaryListener() {
//        // Ensure Firebase refs and currentUserID are initialized before attaching
//        if (chatSummariesRef == null || currentUserID == null) {
//            Log.e(TAG, "chatSummariesRef or currentUserID is null, cannot attach listener.");
//            return;
//        }
//
//        // Only attach if not already attached
//        if (!isChatSummaryListenerAttached) { // Use the flag
//            Log.d(TAG, "Attaching Firebase ChatSummaries listener to: " + chatSummariesRef.getPath());
//            chatSummaryListener = new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    Log.d(TAG, "Firebase ChatSummaries data received for user: " + currentUserID + ". Processing " + dataSnapshot.getChildrenCount() + " summaries.");
//
//                    Set<String> firebasePartnerIds = new HashSet<>(); // To track partners found in Firebase
//
//                    // Check if the snapshot itself exists and has children BEFORE processing
//                    if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
//                        Log.d(TAG, "No chat summaries found in Firebase for user: " + currentUserID + ". Clearing local chats for this owner.");
//                        // Clear chats from Room if no summaries in Firebase
//                        databaseExecutor.execute(() -> { // Run on DB thread
//                            try {
//                                // Make sure this method exists in ChatDao and deletes WHERE ownerUserId = :ownerId
//                                int deletedCount = chatDao.deleteAllChatsForOwner(currentUserID); // Assuming this method exists
//                                Log.d(TAG, "Successfully cleared " + deletedCount + " chats from Room as Firebase has no summaries for owner " + currentUserID + ".");
//                            } catch (Exception e) {
//                                Log.e(TAG, "Error clearing chats from Room for owner " + currentUserID, e);
//                            }
//                        });
//                        // LiveData observer will update UI automatically after Room is cleared
//                        return; // Exit onDataChange
//                    }
//
//                    // Process each chat partner's summary under the current user's node
//                    for (DataSnapshot chatPartnerSummarySnap : dataSnapshot.getChildren()) {
//                        // The key of the child node is the chat partner's UID
//                        String chatPartnerId = chatPartnerSummarySnap.getKey();
//
//                        // *** ADD THIS CHECK: Skip processing if it's a null, empty or self entry ***
//                        if (TextUtils.isEmpty(chatPartnerId) || chatPartnerId.equals(currentUserID)) {
//                            Log.d(TAG, "Skipping null, empty, or self-chat partner ID in summary: " + chatPartnerId);
//                            continue;
//                        }
//                        // ****************************************************************
//
//                        // Check if the snapshot for this partner has the expected data structure before proceeding
//                        if (!chatPartnerSummarySnap.hasChild("conversationId") || !chatPartnerSummarySnap.hasChild("lastMessageTimestamp") || !chatPartnerSummarySnap.hasChild("unreadCounts") || !chatPartnerSummarySnap.child("unreadCounts").hasChild(currentUserID)) {
//                            Log.w(TAG, "Skipping summary for " + chatPartnerId + ": Missing expected child nodes (convId, timestamp, unreadCounts/" + currentUserID + ").");
//                            // Optional: Delete this invalid entry from Firebase ChatSummaries?
//                            // chatPartnerSummarySnap.getRef().removeValue(); // Use with caution
//                            continue;
//                        }
//
//
//                        Log.d(TAG, "Processing summary for chat partner: " + chatPartnerId);
//                        firebasePartnerIds.add(chatPartnerId); // Add to the set of found partners
//
//                        // Get summary data directly from the snapshot
//                        String conversationId = chatPartnerSummarySnap.child("conversationId").getValue(String.class);
//                        // This is the ENCRYPTED or placeholder preview from the SENDER's Firebase summary
//                        String encryptedLastMessageContentPreview = chatPartnerSummarySnap.child("lastMessageContentPreview").getValue(String.class); // Can be null
//                        Long timestampLong = chatPartnerSummarySnap.child("lastMessageTimestamp").getValue(Long.class); // Can be null initially
//                        // Get unread count specifically for the current user from the sub-node
//                        Integer unreadCount = chatPartnerSummarySnap.child("unreadCounts").child(currentUserID).getValue(Integer.class); // Can be null
//                        String lastMessageSenderId = chatPartnerSummarySnap.child("lastMessageSenderId").getValue(String.class); // Can be null
//                        String lastMessageType = chatPartnerSummarySnap.child("lastMessageType").getValue(String.class); // Can be null
//
//
//                        // Basic validation for essential fields needed to create/update a chat entry in Room
//                        if (TextUtils.isEmpty(chatPartnerId) || TextUtils.isEmpty(conversationId) || timestampLong == null || unreadCount == null) {
//                            Log.w(TAG, "Skipping summary for " + chatPartnerId + ": Missing essential fields for Room insertion/update (partnerId, convId, timestamp, unreadCount).");
//                            // Optional: Delete this invalid entry from Firebase ChatSummaries?
//                            // chatPartnerSummarySnap.getRef().removeValue(); // Use with caution
//                            continue;
//                        }
//
//
//                        // --- Fetch User Data (Username and Image) Asynchronously ---
//                        // Fetch the chat partner's user data asynchronously
//                        // Use SingleValueEvent as we only need the current state once per sync cycle for this user's profile.
//                        // This runs on the main thread. The Room operation will be on a background thread.
//                        usersRef.child(chatPartnerId).addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot userSnap) {
//                                String username = "Unknown User"; // Default username
//                                String base64Image = ""; // Default image (empty Base64)
//
//                                if (userSnap.exists()) {
//                                    // Get username and profile image from the other user's profile data
//                                    username = userSnap.child("username").getValue(String.class);
//                                    base64Image = userSnap.child("profileImage").getValue(String.class);
//                                    // Handle potential nulls/empty strings for display
//                                    if (TextUtils.isEmpty(username)) username = "Unknown User";
//                                    if (base64Image == null) base64Image = ""; // Ensure Base64 string is not null
//
//                                    // Optional: Log fetched user data details
//                                    // Log.d(TAG, "Fetched user data for chat partner: " + chatPartnerId + ", Username: " + username + ", Image exists: " + (!TextUtils.isEmpty(base64Image)));
//
//                                } else {
//                                    Log.w(TAG, "User data not found in /Users/ for chat partner userId: " + chatPartnerId + " during sync.");
//                                    // Keep default placeholders if user data is missing
//                                }
//
//                                // Create ChatEntity for Room using the fetched user data and summary data
//                                ChatEntity chatEntity = new ChatEntity(); // Use empty constructor
//                                // Set the primary key components and other fields
//                                chatEntity.setOwnerUserId(currentUserID); // Set owner to the current logged-in user
//                                chatEntity.setUserId(chatPartnerId); // Set chat partner ID (the other user in this chat)
//                                chatEntity.setConversationId(conversationId); // Store conversation ID
//
//                                // Set display information from the fetched user data
//                                chatEntity.setUsername(username);
//                                chatEntity.setProfileImage(base64Image);
//
//                                // Store the *ENCRYPTED* preview content (or placeholder) from the Firebase summary in Room
//                                chatEntity.setLastMessage(encryptedLastMessageContentPreview != null ? encryptedLastMessageContentPreview : ""); // Can be null from Firebase
//
//                                // Set timestamp, unread count, type, and sender ID
//                                chatEntity.setTimestamp(timestampLong != null ? timestampLong : 0L); // Store Firebase timestamp, default to 0 if null
//                                chatEntity.setUnreadCount(unreadCount != null ? unreadCount : 0); // Store the unread count FOR THIS USER, default to 0
//
//                                // Store lastMessageType (default to text if null or empty)
//                                chatEntity.setLastMessageType(TextUtils.isEmpty(lastMessageType) ? "text" : lastMessageType);
//
//                                // Optional: If you added fields for last message sender ID in ChatEntity
//                                // chatEntity.setLastMessageSenderId(lastMessageSenderId != null ? lastMessageSenderId : "");
//
//
//                                // Save/update the ChatEntity in Room DB on a background thread
//                                databaseExecutor.execute(() -> { // Run on Room DB thread
//                                    try {
//                                        // Insert or replace the chat list entry for the current user and this chat partner
//                                        chatDao.insertOrUpdateChat(chatEntity); // Use insertOrUpdate (REPLACE strategy)
//                                        // Log success (can be verbose)
//                                        // Log.d(TAG, "Successfully saved/updated chat list entry to Room for partner: " + chatPartnerId + " (Owner: " + currentUserID + ")");
//                                    } catch (Exception e) {
//                                        Log.e(TAG, "Error saving/updating chat list entry to Room for partner: " + chatPartnerId + " (Owner: " + currentUserID + ")", e);
//                                        // Handle Room DB errors if necessary (e.g., log, show internal error message)
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                                Log.e(TAG, "Failed to fetch user data for chat partner " + chatPartnerId + " during sync: " + databaseError.getMessage());
//                                // Decide how to handle - if user data fetch fails, the chat entry might not be updated correctly in Room.
//                                // The last known state in Room will persist until next successful sync.
//                                // You might want to create/update the ChatEntity with default/error info here if user data is critical.
//                            }
//                        }); // End of usersRef.child(chatPartnerId).addListenerForSingleValueEvent (asynchronous fetch)
//
//                    } // End of processing each chat partner summary
//
//
//                    // --- Clean up Room DB: Delete chats that are no longer in Firebase Summaries ---
//                    // This ensures that if a chat summary is deleted from Firebase (e.g., by deleting the last message),
//                    // it's also removed from the user's chat list in Room.
//                    databaseExecutor.execute(() -> { // Run on Room DB thread
//                        try {
//                            // Need an immediate query method in your DAO to get current local chats owned by this user
//                            // Make sure getAllChatsImmediate(ownerId) exists and is synchronous and retrieves chats by ownerId
//                            List<ChatEntity> localChats = chatDao.getAllChatsImmediate(currentUserID);
//
//                            if (localChats != null) {
//                                for (ChatEntity localChat : localChats) {
//                                    // Check if this local chat entry (by partner ID) exists in the set of partners found in the Firebase snapshot
//                                    // Ensure localChat.getUserId() is not null/empty before checking firebasePartnerIds
//                                    if (!TextUtils.isEmpty(localChat.getUserId()) && !firebasePartnerIds.contains(localChat.getUserId())) {
//                                        Log.d(TAG, "Deleting local chat entry for partner " + localChat.getUserId() + " as it's no longer in Firebase summaries for owner " + currentUserID + ".");
//                                        // Delete the chat entry owned by currentUserID
//                                        // Make sure deleteChatByUserId(partnerId, ownerId) exists and uses both IDs
//                                        int deletedSummaryRows = chatDao.deleteChatByUserId(localChat.getUserId(), currentUserID); // Ensure this DAO method exists and uses ownerId
//
//                                        if (deletedSummaryRows > 0) {
//                                            Log.d(TAG, "Successfully deleted chat list entry from Room for partner: " + localChat.getUserId() + " owned by: " + currentUserID);
//                                        } else {
//                                            Log.w(TAG, "Attempted to delete chat list entry from Room for partner " + localChat.getUserId() + ", but it wasn't found for owner " + currentUserID);
//                                        }
//
//
//                                        // Optional: Also delete the related messages from Room for this conversation
//                                        // This depends on your deletion policy. Clearing the list item doesn't always mean deleting messages.
//                                        // If you want to delete messages too, you need the conversation ID.
//                                        // The conversation ID is stored in the localChat entity.
//                                        String conversationIdForMessageDelete = localChat.getConversationId();
//                                        if (!TextUtils.isEmpty(conversationIdForMessageDelete)) {
//                                            try {
//                                                // Assuming MessageDao has deleteAllMessagesForChat(ownerId, conversationId)
//                                                // Or deleteMessagesForChat(ownerId, user1Id, user2Id)
//                                                // Using ConversationId is simpler if it's consistently stored
//                                                int deletedMessageRows = messageDao.deleteAllMessagesForChat(currentUserID, conversationIdForMessageDelete); // Assuming a DAO method that takes owner and convId
//                                                Log.d(TAG, "Successfully deleted " + deletedMessageRows + " messages from Room for conversation " + conversationIdForMessageDelete + " for owner " + currentUserID + " during sync cleanup.");
//                                            } catch (Exception msgDeleteEx) {
//                                                Log.e(TAG, "Error deleting messages from Room during sync cleanup for conv " + conversationIdForMessageDelete + " for owner " + currentUserID + ".", msgDeleteEx);
//                                            }
//                                        } else {
//                                            Log.w(TAG, "Cannot delete messages for local chat entry " + localChat.getUserId() + ": Conversation ID is empty.");
//                                        }
//
//                                        // Optional: Also delete the conversation key from Room if the chat is removed
//                                        String conversationIdForKeyDelete = localChat.getConversationId();
//                                        if (!TextUtils.isEmpty(conversationIdForKeyDelete)) {
//                                            try {
//                                                // Assuming ConversationKeyDao has deleteKeyById(ownerId, conversationId)
//                                                int deletedKeyRows = chatDatabase.conversationKeyDao().deleteKeyById(currentUserID, conversationIdForKeyDelete); // Access the new DAO
//                                                if (deletedKeyRows > 0) {
//                                                    Log.d(TAG, "Deleted conversation key from Room during sync cleanup for conv " + conversationIdForKeyDelete + " for owner " + currentUserID + ".");
//                                                    // Optional: Remove from in-memory KeyManager if it was loaded
//                                                    YourKeyManager.getInstance().removeConversationKey(conversationIdForKeyDelete);
//                                                } else {
//                                                    Log.w(TAG, "Attempted to delete conversation key but it was not found in Room during sync cleanup for conv " + conversationIdForKeyDelete);
//                                                }
//                                            } catch (Exception keyDeleteEx) {
//                                                Log.e(TAG, "Error deleting conversation key from Room during sync cleanup for conv " + conversationIdForKeyDelete + " for owner " + currentUserID + ".", keyDeleteEx);
//                                            }
//                                        } else {
//                                            Log.w(TAG, "Cannot delete conversation key for local chat entry " + localChat.getUserId() + ": Conversation ID is empty.");
//                                        }
//
//                                    }
//                                }
//                            }
//                            Log.d(TAG, "Finished Room cleanup based on Firebase summaries for owner " + currentUserID + ".");
//                        } catch (Exception e) {
//                            Log.e(TAG, "Error during Room cleanup after Firebase sync for owner " + currentUserID + ".", e);
//                        }
//                    }); // End of Room cleanup executor
//
//
//                } // End of onDataChange for chatSummariesRef
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//                    Log.e(TAG, "Firebase Chat Summary Listener Cancelled: " + databaseError.getMessage(), databaseError.toException());
//                    if (getContext() != null) {
//                        Toast.makeText(getContext(), "Failed to load chats: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                    // Decide if you need to disable UI or show permanent error here
//                }
//            };
//
//            // Attach the listener
//            chatSummariesRef.addValueEventListener(chatSummaryListener);
//            isChatSummaryListenerAttached = true; // Set the flag
//            Log.d(TAG, "Firebase ChatSummaries listener attached.");
//        } else {
//            // Listener is already attached or cannot be attached (user not logged in, handled earlier)
//            Log.d(TAG, "ChatSummaries listener already attached or cannot be attached.");
//        }
//    }
//    // --- End Attach Real-time Listener ---
//
//
//    // --- Remove Firebase Listener and Observers in onDestroyView ---
//    @Override
//    public void onDestroyView() { // Use onDestroyView for Fragment lifecycle
//        super.onDestroyView();
//        Log.d(TAG, "ChatFragment onDestroyView");
//
//        // Remove Firebase listener only if it was attached
//        if (chatSummariesRef != null && chatSummaryListener != null && isChatSummaryListenerAttached) { // Check the flag
//            chatSummariesRef.removeEventListener(chatSummaryListener);
//            chatSummaryListener = null; // Nullify the listener reference
//            isChatSummaryListenerAttached = false; // Reset the flag
//            Log.d(TAG, "Firebase ChatSummaries listener removed.");
//        } else {
//            Log.d(TAG, "Firebase ChatSummaries listener was not attached or already removed.");
//        }
//
//        // Remove LiveData observer
//        if (chatListLiveData != null) {
//            // Use getViewLifecycleOwner() to ensure the observer is removed when the view is destroyed
//            chatListLiveData.removeObservers(getViewLifecycleOwner());
//            Log.d(TAG, "Room LiveData observer removed.");
//        }
//
//        // ExecutorService for Room DB write is managed by ChatDatabase class, no need to shut down here.
//
//        // Clear view references to prevent memory leaks
//        privateChatsView = null;
//        privateChatsList = null;
//        noChatsText = null;
//        searchView = null;
//        chatAdapter = null; // Clear adapter reference
//
//        Log.d(TAG, "ChatFragment onDestroyView finished.");
//    }
//
//    // Optional: Add onDestroy method for final Fragment cleanup
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "ChatFragment onDestroy");
//        // Clean up any other non-view related resources specific to this fragment if needed.
//        // If you had other executors or resources tied to the fragment's entire lifecycle, clean them here.
//        // The Room DB executor is managed by ChatDatabase singleton, so no need to shut it down here.
//    }
//
//
//    // --- ChatAdapter Interaction Listener Methods (Keep These) ---
//
//    @Override // From ChatAdapter.OnChatInteractionListener
//    public void onChatClick(ChatEntity chat) {
//        // This is called when a chat item is clicked in the RecyclerView list.
//        Log.d(TAG, "Chat clicked: " + chat.getUsername() + " (Other User ID: " + chat.getUserId() + ") Conversation ID: " + chat.getConversationId());
//
//        // Ensure currentUserID is available before proceeding
//        if (currentUserID == null) {
//            Log.e(TAG, "currentUserID is null in onChatClick. Cannot proceed.");
//            if (getContext() != null) {
//                Toast.makeText(getContext(), "Error: User not logged in.", Toast.LENGTH_SHORT).show();
//            }
//            return; // Exit if user ID is not available
//        }
//
//        // Get necessary info from the clicked ChatEntity
//        String conversationId = chat.getConversationId();
//        String chatPartnerId = chat.getUserId();
//        String chatPartnerName = chat.getUsername();
//        String chatPartnerImage = chat.getProfileImage();
//
//
//        // Validate conversationId
//        if (TextUtils.isEmpty(conversationId)) {
//            Log.e(TAG, "Conversation ID is null or empty for clicked chat with partner: " + chat.getUserId());
//            Toast.makeText(getContext(), "Error opening chat: Invalid conversation.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // *** NEW: Check if user's RSA Private Key is available and if conversation key is in KeyManager ***
//        boolean isPrivateKeyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable();
//        boolean hasConversationKeyInMemory = YourKeyManager.getInstance().hasConversationKey(conversationId);
//
//        if (!isPrivateKeyAvailable) {
//            // User's main account is not unlocked or setup. Cannot decrypt messages.
//            Log.w(TAG, "User's Private key is NOT available. Cannot open secure chat.");
//            Toast.makeText(getContext(), "Your account is not unlocked for secure chat. Please set up or unlock your Security Passphrase in Settings.", Toast.LENGTH_LONG).show();
//            // Do NOT proceed to open the chat activity in a disabled state from here.
//            // User needs to fix their security setup first.
//            return;
//        }
//
//        if (hasConversationKeyInMemory) {
//            // Conversation key is already in the in-memory cache. We are ready to go!
//            Log.d(TAG, "Conversation key found in KeyManager cache for ID: " + conversationId + ". Navigating directly to chat.");
//            // Dismiss progress dialog if somehow showing (unlikely here, but safe)
//            if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) {
//                chatOpenProgressDialog.dismiss();
//            }
//            navigateToChatActivity(conversationId, chatPartnerId, chatPartnerName, chatPartnerImage);
//
//        } else {
//            // Private key is available, but the specific conversation key is NOT in KeyManager's cache.
//            // This means it wasn't loaded during the initial load from Room, or the initial load failed for this key.
//            // We need to attempt to load *this specific key* from Room (and potentially Firebase as a fallback).
//            Log.d(TAG, "Conversation key NOT found in KeyManager cache for ID: " + conversationId + ". Attempting to load from Room/DB.");
//
//            // Show progress dialog while we load the key
//            if (getContext() == null) {
//                Log.w(TAG, "Context is null, cannot show progress dialog.");
//                Toast.makeText(getContext(), "Error starting chat.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            // Ensure dialog instance is valid and owned by the activity
//            if (chatOpenProgressDialog == null) { // Re-initialize if null (shouldn't be if initialized in onCreate)
//                chatOpenProgressDialog = new ProgressDialog(requireContext());
//                chatOpenProgressDialog.setCancelable(false);
//                if (getActivity() != null) chatOpenProgressDialog.setOwnerActivity(getActivity());
//            }
//            // Ensure dialog is associated with the correct activity if the fragment detached/re-attached
//            if (getActivity() != null && chatOpenProgressDialog.getOwnerActivity() != getActivity()) {
//                chatOpenProgressDialog.setOwnerActivity(getActivity());
//            }
//            chatOpenProgressDialog.setMessage("Loading secure chat key...");
//            if (!chatOpenProgressDialog.isShowing()) { // Only show if not already showing
//                chatOpenProgressDialog.show();
//            }
//
//
//            // Initiate the asynchronous key loading process for THIS conversation
//            // Use the dedicated executor for chat opening tasks
//            loadConversationKeyForChatAsync(conversationId, chatPartnerId, chatPartnerName, chatPartnerImage);
//        }
//        // --- END NEW CHECK ---
//
//
//        // Original code to navigate to ChatPageActivity is now moved inside the success paths above.
//        // navigateToChatActivity(conversationId, chatPartnerId, chatPartnerName, chatPartnerImage);
//        // The unread count update logic is also moved inside the navigation helper method.
//    }
//
//
//
//
//    // *** NEW Method to Load Conversation Key for a Specific Chat Asynchronously ***
//    // Requires import: com.google.android.gms.tasks.Tasks;
////    private void loadConversationKeyForChatAsync(String conversationId, String chatPartnerId, String chatPartnerName, String chatPartnerImage) {
////
////        if (TextUtils.isEmpty(currentUserID) || TextUtils.isEmpty(conversationId) || conversationKeyDao == null || rootRef == null) {
////            Log.e(TAG, "loadConversationKeyForChatAsync: Missing currentUserID, conversationId, DAO, or rootRef.");
////            chatOpenHandler.post(() -> { // Post error to main thread
////                if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
////                Toast.makeText(getContext(), "Error loading chat key.", Toast.LENGTH_SHORT).show();
////            });
////            return;
////        }
////        Log.d(TAG, "loadConversationKeyForChatAsync: Starting key load for conversation: " + conversationId);
////
////
////        // Use the dedicated chat opening executor
////        chatOpenExecutor.execute(() -> {
////            SecretKey conversationAESKey = null;
////            String errorMessage = null;
////
////            // --- Step 1: Attempt to load the specific key from Room DB ---
////            // Room should be the primary source after the initial bulk load in MainActivity
////            Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Attempting to load key from Room DB for conv ID: " + conversationId + " for owner " + currentUserID);
////            ConversationKeyEntity keyEntity = null;
////            try {
////                // Use the DAO method to get a specific key for this user and conversation
////                // This query runs on the background thread provided by the executor
////                keyEntity = conversationKeyDao.getKeyById(currentUserID, conversationId);
////                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Room query finished. Key found: " + (keyEntity != null));
////
////            } catch (Exception e) { // Catch any exception during DAO query
////                Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Error fetching key from Room DB for conv ID: " + conversationId, e);
////                // Do NOT set error message yet, we will fall back to Firebase if Room fetch fails unexpectedly
////            }
////
////
////            if (keyEntity != null && !TextUtils.isEmpty(keyEntity.getDecryptedKeyBase64())) {
////                // Key found in Room. Decode and convert to SecretKey.
////                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key found in Room. Decoding and converting.");
////                try {
////                    // Use android.util.Base64 for decoding from the Room String
////                    byte[] decryptedKeyBytes = Base64.decode(keyEntity.getDecryptedKeyBase64(), Base64.DEFAULT); // Use android.util.Base64
////                    conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedKeyBytes); // Convert bytes to SecretKey
////
////                    // Store the SecretKey in the in-memory KeyManager cache
////                    YourKeyManager.getInstance().setConversationKey(conversationId, conversationAESKey);
////                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key loaded from Room DB into KeyManager for conv ID: " + conversationId);
////
////                } catch (IllegalArgumentException e) { // Base64 decoding error from Room data
////                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Error decoding Base64 key from Room for conv ID: " + conversationId + ". Marking as corrupt.", e);
////                    errorMessage = "Corrupt key data found locally. Please try re-logging in.";
////                    // Optional: delete the corrupt entry from Room
////                    try {
////                        if (conversationKeyDao != null) { conversationKeyDao.deleteKeyById(currentUserID, conversationId); }
////                    } catch (Exception deleteEx) { Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Error deleting corrupt key from Room", deleteEx); }
////
////                } catch (Exception e) { // Any other unexpected error converting bytes to key
////                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Unexpected error converting key bytes from Room for conv ID: " + conversationId, e);
////                    errorMessage = "Error processing local chat key.";
////                }
////
////            } else {
////                // Key NOT found in Room OR Room entry was empty/corrupt.
////                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key NOT found in Room for conv ID: " + conversationId + " or data empty. Falling back to Firebase.");
////                // --- Step 2: Fallback to Fetching from Firebase if Room failed ---
////                // This happens if the initial load from Room failed or was incomplete.
////                // We need the user's Private RSA key to decrypt the key from Firebase.
////                PrivateKey currentUserPrivateKey = YourKeyManager.getInstance().getUserPrivateKey();
////
////                if (currentUserPrivateKey == null) {
////                    // This state should have been caught earlier in onChatClick, but defensive check.
////                    errorMessage = "Your private key is not available to decrypt conversation keys.";
////                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Private key is unexpectedly null when trying to fetch from Firebase.");
////
////                } else {
////                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Fetching key from Firebase for conv ID: " + conversationId + " for user " + currentUserID);
////                    // Use Tasks.await() to make the Firebase fetch synchronous on this background thread.
////                    try {
////                        DataSnapshot firebaseSnapshot = Tasks.await(rootRef.child("ConversationKeys").child(conversationId).child(currentUserID).get()); // Blocking call
////                        Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Firebase get() completed. Snapshot exists: " + firebaseSnapshot.exists());
////
////                        if (firebaseSnapshot.exists()) {
////                            // Found the encrypted key for this user in Firebase
////                            String encryptedAesKeyForCurrentUserBase64 = firebaseSnapshot.getValue(String.class);
////
////                            if (!TextUtils.isEmpty(encryptedAesKeyForCurrentUserBase64)) {
////                                // Decode Base64 and decrypt using Current User's RSA Private Key
////                                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Encrypted key found in Firebase. Decoding and decrypting.");
////                                byte[] encryptedAesKeyBytes = Base64.decode(encryptedAesKeyForCurrentUserBase64, Base64.DEFAULT); // Use android.util.Base64
////                                byte[] decryptedAesKeyBytes = CryptoUtils.decryptWithRSA(encryptedAesKeyBytes, currentUserPrivateKey); // Decrypt with RSA Private Key!
////                                conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedAesKeyBytes); // Convert bytes to AES SecretKey
////
////                                // Store the decrypted SecretKey in the in-memory KeyManager cache
////                                YourKeyManager.getInstance().setConversationKey(conversationId, conversationAESKey);
////                                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key decrypted from Firebase and loaded into KeyManager for conv ID: " + conversationId);
////
////                                // *** Save the successfully fetched/decrypted key to Room DB for persistence ***
////                                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Saving fetched/decrypted key to Room DB.");
////                                // Use android.util.Base64 for converting the decrypted bytes to a Base64 String for Room storage
////                                String decryptedKeyBase64ForRoom = Base64.encodeToString(decryptedAesKeyBytes, Base64.DEFAULT); // Use android.util.Base64
////                                ConversationKeyEntity keyEntityToSave = new ConversationKeyEntity(currentUserID, conversationId, decryptedKeyBase64ForRoom);
////                                // Use the databaseWriteExecutor for Room save (can't save directly on chatOpenExecutor if DB executor is single thread)
////                                ChatDatabase.databaseWriteExecutor.execute(() -> {
////                                    try {
////                                        conversationKeyDao.insertOrUpdateKey(keyEntityToSave);
////                                        Log.d(TAG, "loadConversationKeyForChatAsync (Executor - DB): Key saved to Room after fetching/decrypting from Firebase for conv ID: " + conversationId);
////                                    } catch (Exception saveEx) {
////                                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor - DB): Error saving fetched key to Room DB", saveEx);
////                                        // Log error, don't show toast, chat will still work for this session from KeyManager
////                                    }
////                                });
////
////
////                            } else {
////                                // Key data in Firebase is empty (unexpected for existing chat)
////                                errorMessage = "Conversation key data missing in server for your account.";
////                                Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Encrypted key data empty in Firebase for user " + currentUserID + " in conv " + conversationId);
////                            }
////
////                        } else {
////                            // Key not found at all in Firebase for this user/conversation
////                            errorMessage = "Conversation key not found in server. Cannot start secure chat.";
////                            Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Key not found in Firebase for user " + currentUserID + " in conv " + conversationId);
////                        }
////
////                    } catch (java.util.concurrent.ExecutionException | InterruptedException e) { // Handle Tasks.await() errors
////                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Firebase fetch failed", e);
////                        errorMessage = "Failed to fetch key from server. Network error?";
////                    } catch (IllegalArgumentException e) { // Base64 or key format error
////                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Key data format error after fetching from Firebase", e);
////                        errorMessage = "Corrupt key data received from server.";
////                    } catch (Exception e) { // Catch other crypto errors during decryption
////                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Decryption error after fetching from Firebase", e);
////                        errorMessage = "Failed to decrypt chat key.";
////                    }
////                } // End if (currentUserPrivateKey != null)
////            } // End else (Key not found in Room)
////
////
////            // --- Post result back to Main Thread ---
////            final String finalErrorMessage = errorMessage;
////            final String finalConversationId = conversationId;
////            final String finalChatPartnerId = chatPartnerId;
////            final String finalChatPartnerName = chatPartnerName;
////            final String finalChatPartnerImage = chatPartnerImage;
////
////
////            chatOpenHandler.post(() -> { // Post to the main thread
////                if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss(); // Dismiss progress
////
////                // After the async task finishes, check if the key is now in KeyManager's cache.
////                // This is the ultimate check for success in loading the key for this chat.
////                if (finalErrorMessage == null && YourKeyManager.getInstance().hasConversationKey(finalConversationId)) {
////                    // Key is now successfully loaded into KeyManager (either from Room or Firebase)
////                    Log.d(TAG, "loadConversationKeyForChatAsync (Main Thread): Key loaded successfully into KeyManager. Navigating to chat.");
////                    // Navigate to the chat activity, passing the conversation details
////                    navigateToChatActivity(finalConversationId, finalChatPartnerId, finalChatPartnerName, finalChatPartnerImage); // Call the navigation method
////
////                } else {
////                    // Key loading failed (either from Room or Firebase fallback)
////                    Log.e(TAG, "loadConversationKeyForChatAsync (Main Thread): Failed to load conversation key for conv ID: " + finalConversationId + ". Error: " + finalErrorMessage);
////                    Toast.makeText(getContext(), finalErrorMessage != null ? finalErrorMessage : "Failed to open secure chat.", Toast.LENGTH_LONG).show();
////                    // Do NOT navigate to chat activity
////                }
////            });
////        }); // End of chatOpenExecutor.execute()
////    }
//
//
//// In ChatFragment.java, locate this method and replace it with the following:
//
//    /**
//     * Attempts to load the conversation key for a specific chat.
//     * Tries Room DB first, then falls back to Firebase.
//     * If Firebase key decryption fails (implying passphrase reset for this user),
//     * triggers generation of a NEW key pair for this conversation.
//     * Runs on a background executor.
//     *
//     * @param conversationId The ID of the conversation.
//     * @param chatPartnerId The Firebase UID of the chat partner.
//     * @param chatPartnerName The name of the chat partner (for navigation).
//     * @param chatPartnerImage The profile image of the chat partner (for navigation).
//     */
//    private void loadConversationKeyForChatAsync(String conversationId, String chatPartnerId, String chatPartnerName, String chatPartnerImage) {
//
//        // Initial validation: Ensure context, user ID, conv ID, DAOs, and Firebase ref are available
//        if (getContext() == null || TextUtils.isEmpty(currentUserID) || TextUtils.isEmpty(conversationId) ||
//                conversationKeyDao == null || rootRef == null || chatOpenExecutor == null || chatOpenHandler == null) {
//            Log.e(TAG, "loadConversationKeyForChatAsync: Prerequisites missing. Cannot load key.");
//            // Post error to main thread if context is available, otherwise just log
//            if (getContext() != null && chatOpenHandler != null) {
//                chatOpenHandler.post(() -> { // Post error to main thread
//                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
//                    Toast.makeText(getContext(), "Error loading chat key.", Toast.LENGTH_SHORT).show();
//                });
//            }
//            return;
//        }
//        Log.d(TAG, "loadConversationKeyForChatAsync: Starting key load for conversation: " + conversationId + " for user: " + currentUserID);
//
//
//        // Use the dedicated chat opening executor for the background task
//        chatOpenExecutor.execute(() -> {
//            SecretKey conversationAESKey = null; // The decrypted AES key (will be null on failure or if generation is triggered)
//            String errorMessage = null; // To hold an error message if loading/decryption fails permanently
//            boolean triggerNewKeyGeneration = false; // Flag to indicate if decryption failed, requiring new key
//
//
//            // --- Step 1: Attempt to load the specific key from Room DB ---
//            // Room is the primary source after the initial bulk load in MainActivity
//            Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Attempting to load key from Room DB for conv ID: " + conversationId);
//            ConversationKeyEntity keyEntity = null;
//            try {
//                // Use the DAO method to get a specific key for this user and conversation
//                // This query runs on the background thread provided by the executor
//                keyEntity = conversationKeyDao.getKeyById(currentUserID, conversationId);
//                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Room query finished. Key found in Room: " + (keyEntity != null));
//
//            } catch (Exception e) { // Catch any exception during DAO query
//                Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Error fetching key from Room DB for conv ID: " + conversationId, e);
//                // Don't set error message yet, we will fall back to Firebase if Room fetch fails unexpectedly
//            }
//
//
//            if (keyEntity != null && !TextUtils.isEmpty(keyEntity.getDecryptedKeyBase64())) {
//                // Key found in Room. Decode and convert to SecretKey.
//                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key found in Room. Decoding and converting.");
//                try {
//                    // Use android.util.Base64 for decoding from the Room String
//                    byte[] decryptedKeyBytes = android.util.Base64.decode(keyEntity.getDecryptedKeyBase64(), android.util.Base64.DEFAULT);
//                    conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedKeyBytes); // Convert bytes to SecretKey
//                    YourKeyManager.getInstance().setConversationKey(finalConversationId, finalResolvedTimestamp, finalConversationAESKey); // *** CORRECTED KEYMANAGER CALL - ADDED TIMESTAMP ***
//                    // Key successfully loaded from Room! No need to proceed to Firebase.
//
//                } catch (IllegalArgumentException e) { // Base64 decoding error from Room data
//                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Error decoding Base64 key from Room for conv ID: " + conversationId + ". Marking as corrupt.", e);
//                    errorMessage = "Corrupt local key data found. Please try re-logging in.";
//                    // Delete the corrupt entry from Room on the database executor
//                    if (conversationKeyDao != null && databaseExecutor != null) {
//                        databaseExecutor.execute(() -> {
//                            try { conversationKeyDao.deleteKeyById(currentUserID, conversationId); Log.d(TAG, "Executor - DB: Deleted corrupt key from Room: " + conversationId); }
//                            catch (Exception deleteEx) { Log.e(TAG, "Executor - DB: Error deleting corrupt key from Room", deleteEx); }
//                        });
//                    }
//
//                } catch (Exception e) { // Any other unexpected error converting bytes to key
//                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Unexpected error converting key bytes from Room for conv ID: " + conversationId, e);
//                    errorMessage = "Error processing local chat key.";
//                }
//
//            } else {
//                // Key NOT found in Room OR Room entry was empty/corrupt.
//                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key NOT found in Room for conv ID: " + conversationId + " or data empty/corrupt. Falling back to Firebase.");
//                // --- Step 2: Fallback to Fetching from Firebase if Room failed ---
//                // This happens if the initial load from Room failed or was incomplete, OR the Room data was corrupt.
//                // We need the user's Private RSA key to decrypt the key from Firebase.
//                PrivateKey currentUserPrivateKey = YourKeyManager.getInstance().getUserPrivateKey();
//
//                if (currentUserPrivateKey == null) {
//                    // This state should have been caught earlier in onChatClick (preventing the call),
//                    // but check defensively here. Private key MUST be available to attempt Firebase decryption.
//                    errorMessage = "Your private key is not available to decrypt conversation keys.";
//                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Private key is unexpectedly null when trying to fetch from Firebase.");
//
//                } else {
//                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Fetching key from Firebase for conv ID: " + conversationId + " for user " + currentUserID);
//                    // Use Tasks.await() to make the Firebase fetch synchronous on this background thread.
//                    try {
//                        // Fetch the *entire* conversation key node for this conversation ID first.
//                        // We need the entire node to get the other user's public key if we need to generate a new key pair.
//                        DataSnapshot convNodeSnapshot = Tasks.await(rootRef.child("ConversationKeys").child(conversationId).get()); // Blocking call to get the *entire* node for this conversation
//                        Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Firebase get() for conv node completed. Snapshot exists: " + convNodeSnapshot.exists());
//
//                        // Check if the node for THIS conversation exists AT ALL
//                        if (convNodeSnapshot.exists()) {
//                            // Check if the key entry for the *current user* exists within the conversation node
//                            if (convNodeSnapshot.hasChild(currentUserID)) {
//                                // Found the encrypted key for this user in Firebase within the existing conversation node.
//                                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Encrypted key found in Firebase for user " + currentUserID + " in conv " + conversationId + ". Attempting decryption.");
//                                String encryptedAesKeyForCurrentUserBase64 = convNodeSnapshot.child(currentUserID).getValue(String.class);
//
//                                if (!TextUtils.isEmpty(encryptedAesKeyForCurrentUserBase64)) {
//                                    // Decode Base64 string to byte array
//                                    byte[] encryptedAesKeyBytes = android.util.Base64.decode(encryptedAesKeyForCurrentUserBase64, android.util.Base64.DEFAULT); // Use android.util.Base64
//
//                                    try {
//                                        // Attempt decryption using the *current* private key
//                                        byte[] decryptedAesKeyBytes = CryptoUtils.decryptWithRSA(encryptedAesKeyBytes, currentUserPrivateKey); // *** THIS IS WHERE IT WILL FAIL WITH OLD KEY ***
//                                        conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedAesKeyBytes); // Convert bytes to AES SecretKey
//
//                                        // If decryption succeeded:
//                                        YourKeyManager.getInstance().setConversationKey(conversationId, conversationAESKey);
//                                        Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key decrypted successfully from Firebase and loaded into KeyManager for conv ID: " + conversationId);
//
//                                        // *** Save the successfully fetched/decrypted key to Room DB for persistence ***
//                                        Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Saving fetched/decrypted key to Room DB.");
//                                        // Use android.util.Base64 for converting the decrypted bytes to a Base64 String for Room storage
//                                        String decryptedKeyBase64ForRoom = android.util.Base64.encodeToString(decryptedAesKeyBytes, android.util.Base64.DEFAULT); // Use android.util.Base64
//                                        ConversationKeyEntity keyEntityToSave = new ConversationKeyEntity(currentUserID, conversationId, decryptedKeyBase64ForRoom);
//                                        // Use the databaseWriteExecutor for Room save (can't save directly on chatOpenExecutor if DB executor is single thread)
//                                        if (databaseExecutor != null && conversationKeyDao != null) {
//                                            databaseExecutor.execute(() -> {
//                                                try {
//                                                    conversationKeyDao.insertOrUpdateKey(keyEntityToSave);
//                                                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor - DB): Key saved to Room after fetching/decrypting from Firebase for conv ID: " + conversationId);
//                                                } catch (Exception saveEx) {
//                                                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor - DB): Error saving fetched key to Room DB", saveEx);
//                                                    // Log error, don't show toast, chat will still work for this session from KeyManager
//                                                }
//                                            });
//                                        } else {
//                                            Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Cannot save fetched key to Room, databaseExecutor or DAO is null.");
//                                        }
//
//                                    } catch (NoSuchAlgorithmException | NoSuchPaddingException |
//                                             InvalidKeyException |
//                                             IllegalBlockSizeException | BadPaddingException e) {
//                                        // *** Crypto decryption failed! (Likely due to old key) ***
//                                        Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Crypto error during decryption after fetching from Firebase for user " + currentUserID + " conv " + conversationId + ".", e);
//
//                                        // Check if it's a likely decryption failure with the wrong key
//                                        if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof InvalidKeyException) {
//                                            // *** This is the trigger for the reset flow! ***
//                                            Log.d(TAG, "Decryption failure detected (likely old key). Setting flag to trigger NEW key generation for conversation: " + conversationId);
//                                            triggerNewKeyGeneration = true; // Set the flag
//                                            // DO NOT SET errorMessage here, DO NOT SET conversationAESKey.
//                                            // The mainHandler.post block will check triggerNewKeyGeneration flag.
//                                        } else {
//                                            // Other unexpected crypto errors
//                                            errorMessage = "Failed to process chat key."; // Generic error for other crypto issues
//                                        }
//
//                                    } catch (IllegalArgumentException e) { // Base64 decoding error from Firebase data
//                                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Base64 decoding error after fetching from Firebase for conv ID: " + conversationId, e);
//                                        errorMessage = "Corrupt key data received from server.";
//                                    }
//
//
//                                } else {
//                                    // Key data in Firebase is empty for this user (unexpected for existing chat, but possible data issue)
//                                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Encrypted key data empty in Firebase for user " + currentUserID + " in conv " + conversationId);
//                                    // If the entry exists but is empty, treat as missing key data.
//                                    errorMessage = "Conversation key data in server is empty for your account.";
//                                    // Decide if you want to trigger new key generation here too, or force user to contact support.
//                                    // For robustness, let's trigger new generation here as well.
//                                    Log.w(TAG, "Firebase key entry empty for user. Triggering new key generation for this conversation.");
//                                    triggerNewKeyGeneration = true; // Set the flag
//                                }
//
//                            } else {
//                                // Key entry for the current user is NOT found in Firebase for this conversation.
//                                // This happens if the user previously deleted the chat (which deleted the summary and local key)
//                                // or if their key entry was manually deleted from Firebase.
//                                // Treat as a brand new secure chat SETUP from this user's perspective FOR THIS CONV.
//                                Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Key entry for current user (" + currentUserID + ") is MISSING in Firebase for conv " + conversationId + ". Triggering NEW key generation.");
//                                triggerNewKeyGeneration = true; // Set the flag
//                                // DO NOT SET errorMessage here.
//                            }
//                        } else {
//                            // Conversation node not found at all in Firebase for this conversation ID.
//                            // This could happen if the chat was deleted by both users, or data inconsistency.
//                            errorMessage = "Conversation data not found in server."; // More accurate message
//                            Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Conversation node not found in Firebase for conv ID: " + conversationId);
//                            // Decide if you need to cleanup local Room entries if Firebase node is completely gone.
//                        }
//
//
//                    } catch (java.util.concurrent.ExecutionException | InterruptedException e) { // Handle Tasks.await() errors
//                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Firebase fetch failed", e);
//                        errorMessage = "Failed to fetch key data from server. Network error?";
//                    } catch (Exception e) { // Catch any other unexpected exceptions from Firebase fetch/processing
//                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Unexpected error during Firebase fetch/processing", e);
//                        errorMessage = "An error occurred fetching key data.";
//                    }
//
//                } // End else (currentUserPrivateKey != null)
//            } // End else (Key not found in Room)
//
//
//            // --- Post result back to Main Thread ---
//            final String finalErrorMessage = errorMessage;
//            final SecretKey finalConversationAESKey = conversationAESKey; // This will be null unless successful load from Room/Firebase
//            final boolean finalTriggerNewKeyGeneration = triggerNewKeyGeneration; // Pass the flag to Main Thread
//            final String finalConversationId = conversationId;
//            final String finalChatPartnerId = chatPartnerId;
//            final String finalChatPartnerName = chatPartnerName;
//            final String finalChatPartnerImage = chatPartnerImage;
//            final String finalCurrentUserId = currentUserID;
//
//
//            chatOpenHandler.post(() -> { // Post to the main thread
//                // Dismiss the progress dialog here, BEFORE potentially starting key generation or navigating
//                if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
//
//                // Check if decryption failure required new key generation
//                if (finalTriggerNewKeyGeneration) {
//                    Log.d(TAG, "loadConversationKeyForChatAsync (Main Thread): Decryption failed or key missing in Firebase. Triggering NEW key generation for conv ID: " + finalConversationId);
//                    // Call generateAndSaveConversationKeysAsync from the main thread, passing necessary info
//                    // This method will show its own progress dialog and handle navigation upon completion.
//                    generateAndSaveConversationKeysAsync(finalConversationId, finalCurrentUserId, finalChatPartnerId, finalChatPartnerName, finalChatPartnerImage);
//                    // DO NOT NAVIGATE OR SHOW ERROR TOAST HERE. generateAndSaveConversationKeysAsync handles the UI flow.
//
//                } else if (finalErrorMessage == null && finalConversationAESKey != null && YourKeyManager.getInstance().hasConversationKey(finalConversationId)) {
//                    // Key is now successfully loaded into KeyManager (either from Room or Firebase)
//                    Log.d(TAG, "loadConversationKeyForChatAsync (Main Thread): Key loaded successfully into KeyManager. Navigating to chat.");
//                    // Navigate to the chat activity, passing the conversation details
//                    navigateToChatActivity(finalConversationId, finalChatPartnerId, finalChatPartnerName, finalChatPartnerImage); // Call the navigation method
//
//                } else {
//                    // Key loading failed (either from Room or Firebase fallback), AND new key generation was NOT triggered
//                    Log.e(TAG, "loadConversationKeyForChatAsync (Main Thread): Failed to load conversation key for conv ID: " + finalConversationId + ". Error: " + finalErrorMessage);
//                    Toast.makeText(getContext(), finalErrorMessage != null ? finalErrorMessage : "Failed to open secure chat.", Toast.LENGTH_LONG).show();
//                    // Do NOT navigate to chat activity
//                }
//            });
//        }); // End of chatOpenExecutor.execute()
//    }
//
//
//
//
//
//
//// In ChatFragment.java, locate this method and replace it with the following:
//
//    /**
//     * Generates a new AES conversation key for the chat with the recipient,
//     * encrypts it using the RSA public keys of both the current user and the recipient,
//     * saves the wrapped keys to Firebase, stores the decrypted AES key in KeyManager (in-memory),
//     * and saves the decrypted AES key to Room DB for persistence.
//     * This is called for brand new secure chats OR when detecting an existing conversation
//     * needs a new key pair established for the current user (e.g., after passphrase reset).
//     * Runs on a background executor.
//     *
//     * @param conversationId The unique ID for this chat conversation.
//     * @param currentUserId The Firebase UID of the current logged-in user (initiator).
//     * @param recipientUserId The Firebase UID of the chat partner (recipient).
//     * @param recipientName The name of the chat partner (for navigation/UI).
//     * @param recipientImageBase64 The profile image of the chat partner (for navigation/UI).
//     */
//    private void generateAndSaveConversationKeysAsync(String conversationId, String currentUserId, String recipientUserId, String recipientName, String recipientImageBase64) {
//        // Ensure context and essential components are available before starting
//        if (getContext() == null || TextUtils.isEmpty(currentUserID) || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(recipientUserId) ||
//                chatOpenExecutor == null || chatOpenHandler == null || rootRef == null || usersRef == null || conversationKeyDao == null || databaseExecutor == null) {
//            Log.e(TAG, "generateAndSaveConversationKeysAsync: Prerequisites missing. Cannot generate keys.");
//            // Post error to main thread if context is available
//            if (getContext() != null && chatOpenHandler != null) {
//                chatOpenHandler.post(() -> {
//                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
//                    Toast.makeText(getContext(), "Error setting up secure chat.", Toast.LENGTH_SHORT).show();
//                });
//            }
//            return;
//        }
//
//        // Ensure progress dialog is showing (redundant check, but safe)
//        if (chatOpenProgressDialog == null || (getActivity() != null && chatOpenProgressDialog.getOwnerActivity() != getActivity())) {
//            chatOpenProgressDialog = new ProgressDialog(requireContext());
//            chatOpenProgressDialog.setCancelable(false);
//            if (getActivity() != null) chatOpenProgressDialog.setOwnerActivity(getActivity());
//        }
//        if (!chatOpenProgressDialog.isShowing()) {
//            chatOpenProgressDialog.setMessage("Generating and saving keys...");
//            chatOpenProgressDialog.show();
//        }
//        Log.d(TAG, "Generating and saving new conversation keys for " + conversationId + " (Users: " + currentUserId + ", " + recipientUserId + ")");
//
//
//        // Fetch recipient's public key first from Firebase (needed for RSA encryption)
//        // This fetch runs on the main thread callback, then crypto goes to chatExecutor.
//        DatabaseReference recipientUserRef = usersRef.child(recipientUserId); // Use UsersRef member variable
//
//        recipientUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // This callback runs on the main thread due to addListenerForSingleValueEvent
//                // Perform heavy crypto and Firebase save on background thread (chatOpenExecutor)
//                chatOpenExecutor.execute(() -> {
//                    SecretKey conversationAESKey = null; // The decrypted AES key
//                    byte[] conversationAESKeyBytes = null; // The raw bytes of the AES key
//
//                    PublicKey recipientPublicKey = null; // Recipient's RSA Public Key
//                    PublicKey currentUserPublicKey = null; // Current user's RSA Public Key
//                    byte[] encryptedAesKeyForRecipient = null; // AES key encrypted with recipient's public key
//                    byte[] encryptedAesKeyForCurrentUser = null; // AES key encrypted with current user's public key
//
//                    String errorMessage = null; // To hold error message if key generation/encryption fails
//
//                    try {
//                        // 1. Generate a new Conversation AES Key (Symmetric Key)
//                        conversationAESKey = CryptoUtils.generateAESKey();
//                        conversationAESKeyBytes = CryptoUtils.secretKeyToBytes(conversationAESKey); // Get the raw bytes
//                        Log.d(TAG, "Generated new AES conversation key.");
//
//                        // 2. Get Recipient's Public Key from Firebase snapshot
//                        if (snapshot.exists() && snapshot.hasChild("publicKey")) {
//                            String recipientPublicKeyBase64 = snapshot.child("publicKey").getValue(String.class);
//                            if (TextUtils.isEmpty(recipientPublicKeyBase64)) {
//                                Log.e(TAG, "Recipient public key Base64 is empty for " + recipientUserId + " in Firebase.");
//                                throw new IllegalArgumentException("Empty recipient public key Base64 from Firebase");
//                            }
//                            // Use android.util.Base64 for decoding the Base64 string from Firebase
//                            byte[] recipientPublicKeyBytes = android.util.Base64.decode(recipientPublicKeyBase64, android.util.Base64.DEFAULT);
//                            recipientPublicKey = CryptoUtils.bytesToPublicKey(recipientPublicKeyBytes);
//                            Log.d(TAG, "Recipient public key obtained for " + recipientUserId);
//
//                            // 3. Get Current User's Public Key from KeyManager
//                            // It *must* be available because onChatClick checked isPrivateKeyAvailable before calling this.
//                            currentUserPublicKey = YourKeyManager.getInstance().getUserPublicKey();
//                            if (currentUserPublicKey == null) {
//                                // This indicates a severe internal state error if isPrivateKeyAvailable was true but PublicKey is null
//                                Log.e(TAG, "Current user's public key is null during key generation! KeyManager state error. Cannot encrypt key for self.");
//                                throw new IllegalStateException("Current user public key unavailable from KeyManager");
//                            }
//                            Log.d(TAG, "Current user public key obtained from KeyManager.");
//
//
//                            // 4. Encrypt the Conversation AES Key for the RECIPIENT using THEIR RSA Public Key
//                            encryptedAesKeyForRecipient = CryptoUtils.encryptWithRSA(conversationAESKeyBytes, recipientPublicKey);
//                            Log.d(TAG, "AES key encrypted for recipient.");
//
//                            // 5. Encrypt the Conversation AES Key for the CURRENT USER using THEIR RSA Public Key
//                            // This allows the user to re-decrypt the AES key later IF they need to load it from Firebase
//                            // (e.g., on a new device after setting up passphrase).
//                            encryptedAesKeyForCurrentUser = CryptoUtils.encryptWithRSA(conversationAESKeyBytes, currentUserPublicKey);
//                            Log.d(TAG, "AES key encrypted for current user.");
//
//
//                        } else {
//                            // Recipient does NOT have crypto keys or user data missing in Firebase.
//                            // Cannot establish secure chat.
//                            Log.w(TAG, "Recipient (" + recipientUserId + ") does not have public key in Firebase or user data missing. Cannot start secure chat.");
//                            errorMessage = "Recipient has not completed security setup. Secure chat unavailable.";
//                            // No need to post dismissal/toast here, handled below in the post block.
//                            return; // Stop this chatOpenExecutor task early
//                        }
//                    } catch (IllegalArgumentException e) { // Base64 decoding error or invalid key format error
//                        Log.e(TAG, "Key data processing error during key generation: " + e.getMessage(), e);
//                        errorMessage = "Failed to start secure chat: Invalid key data encountered.";
//                        return; // Stop task
//                    } catch (NoSuchAlgorithmException e) { // AES key generation failed
//                        Log.e(TAG, "AES key generation failed: " + e.getMessage(), e);
//                        errorMessage = "Failed to start secure chat: Key generation error.";
//                        return; // Stop task
//                    }
//                    catch (NoSuchPaddingException | InvalidKeyException |
//                           IllegalBlockSizeException | BadPaddingException | IllegalStateException e) { // RSA crypto/state errors
//                        Log.e(TAG, "Cryptographic or state error during key encryption: " + e.getMessage(), e); // Log specific error
//                        errorMessage = "Failed to start secure chat: Encryption error during key exchange.";
//                        return; // Stop task
//                    } catch (Exception e) { // Catch any other unexpected errors
//                        Log.e(TAG, "Unexpected error during key generation/encryption: " + e.getMessage(), e);
//                        errorMessage = "Failed to start secure chat: An unexpected error occurred.";
//                        return; // Stop task
//                    }
//
//                    // If crypto operations succeeded, proceed to save wrapped keys to Firebase
//                    Log.d(TAG, "Encryption successful. Preparing to save keys to Firebase.");
//                    // *** FIX: Use android.util.Base64 with DEFAULT or CryptoUtils.bytesToBase64 (which should use NO_WRAP) ***
//                    // Using CryptoUtils helper is preferred for consistency for keys.
//                    String encryptedAesKeyForRecipientBase64 = CryptoUtils.bytesToBase64(encryptedAesKeyForRecipient); // *** Use CryptoUtils Helper ***
//                    String encryptedAesKeyForCurrentUserBase64 = CryptoUtils.bytesToBase64(encryptedAesKeyForCurrentUser); // *** Use CryptoUtils Helper ***
//
//
//                    // 6. Prepare data to save to Realtime Database under ConversationKeys node
//                    Map<String, Object> keyData = new HashMap<>();
//                    keyData.put(currentUserId, encryptedAesKeyForCurrentUserBase64); // Key encrypted for the initiator
//                    keyData.put(recipientUserId, encryptedAesKeyForRecipientBase64); // Key encrypted for the recipient
//                    keyData.put("generatedBy", currentUserId); // Optional metadata
//                    keyData.put("timestamp", ServerValue.TIMESTAMP); // Add timestamp for ordering/metadata
//
//
//                    // 7. Save the encrypted keys to Realtime Database (Async operation, chain next step in its listener)
//                    DatabaseReference conversationKeysRef = rootRef.child("ConversationKeys").child(conversationId); // Use rootRef member variable
//
//                    // Make final copies of variables needed in the listener lambda
//                    final String finalCurrentUserId = currentUserId;
//                    final String finalConversationId = conversationId;
//                    final byte[] finalConversationAESKeyBytes = conversationAESKeyBytes; // Keep bytes to save locally in Room
//                    final SecretKey finalConversationAESKey = conversationAESKey; // Keep SecretKey for KeyManager cache
//                    final String finalRecipientUserId = recipientUserId;
//                    final String finalRecipientName = recipientName;
//                    final String finalRecipientImageBase64 = recipientImageBase64;
//                    final String finalErrorMessageAfterCrypto = errorMessage; // Capture error message from crypto steps
//
//
//                    conversationKeysRef.setValue(keyData)
//                            .addOnCompleteListener(task -> {
//                                // This callback usually runs on the main thread after Firebase finishes the async operation
//                                chatOpenHandler.post(() -> { // Ensure all UI updates and navigation are explicitly on main thread
//                                    // Dismiss progress dialog is handled at the end of the post block
//
//                                    if (task.isSuccessful()) {
//                                        Log.d(TAG, "Conversation keys saved successfully to Firebase for " + finalConversationId);
//                                        // --- Success: Keys Saved to Firebase ---
//                                        // Store the decrypted conversationAESKey in memory for this chat session
//                                        // The key (finalConversationAESKey) is available from the crypto ops above.
//                                        // This key will be used immediately by ChatPageActivity.
//                                        YourKeyManager.getInstance().setConversationKey(finalConversationId, finalResolvedTimestamp, finalConversationAESKey); // *** CORRECTED KEYMANAGER CALL - ADDED TIMESTAMP ***
//                                        Log.d(TAG, "Decrypted AES key loaded into KeyManager for conversation " + finalConversationId + " after generation.");
//
//
//                                        // *** Save the decrypted conversation key locally to Room DB for persistence ***
//                                        // Run this Room operation on the databaseExecutor (background thread).
//                                        if (databaseExecutor != null && conversationKeyDao != null) {
//                                            databaseExecutor.execute(() -> { // Use the shared database executor
//                                                try {
//                                                    // Convert decrypted key bytes (finalConversationAESKeyBytes) to Base64 string for Room storage.
//                                                    // Use android.util.Base64 with DEFAULT for consistency with how you seem to store in Room.
//                                                    String decryptedKeyBase64 = android.util.Base64.encodeToString(finalConversationAESKeyBytes, android.util.Base64.DEFAULT); // *** Use android.util.Base64.DEFAULT ***
//                                                    // Create ConversationKeyEntity. Owner is the current user.
//                                                    ConversationKeyEntity keyEntity = new ConversationKeyEntity(finalCurrentUserId, finalConversationId, decryptedKeyBase64);
//                                                    // Insert or replace the key entry in Room DB.
//                                                    conversationKeyDao.insertOrUpdateKey(keyEntity); // Use insertOrUpdate (REPLACE strategy)
//                                                    Log.d(TAG, "Decrypted conversation key saved to Room DB for owner " + finalCurrentUserId + " and conversation " + finalConversationId + " after Firebase save.");
//                                                } catch (Exception e) {
//                                                    Log.e(TAG, "Error saving decrypted conversation key to Room DB after Firebase save for user " + finalCurrentUserId + " conv " + finalConversationId, e);
//                                                    // Handle Room DB saving errors (e.g., log it, don't stop chat unless critical).
//                                                }
//                                            });
//                                        } else {
//                                            Log.w(TAG, "generateAndSaveConversationKeysAsync (Main Thread): Cannot save generated key to Room, databaseExecutor or DAO is null.");
//                                        }
//
//
//                                        // NOW, navigate to the actual Chat Activity, passing the conversation details
//                                        navigateToChatActivity(finalConversationId, finalRecipientUserId, finalRecipientName, finalRecipientImageBase64); // Call the navigation method
//
//                                    } else {
//                                        // Firebase save failed
//                                        Log.e(TAG, "Failed to save conversation keys to Firebase for " + finalConversationId, task.getException());
//                                        String displayMessage = "Failed to start secure chat: Could not save keys.";
//                                        if (task.getException() != null) {
//                                            displayMessage += " " + task.getException().getMessage(); // Add exception message if available
//                                        }
//                                        Toast.makeText(getContext(), displayMessage, Toast.LENGTH_LONG).show();
//                                        // Handle error: Don't start chat, show error to user
//                                    }
//
//                                    // Dismiss the progress dialog at the end of the main thread post block
//                                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
//                                });
//                            }); // End of addOnCompleteListener for Firebase save
//
//                    // If an error occurred during crypto steps (before Firebase save was attempted)
//                    // Check the error message captured *before* the Firebase save block
//                    if (finalErrorMessageAfterCrypto != null) {
//                        chatOpenHandler.post(() -> { // Ensure UI update is on main thread
//                            Log.e(TAG, "generateAndSaveConversationKeysAsync (Executor): Crypto error occurred before Firebase save. Posting error toast.");
//                            // Dismiss progress dialog is handled below
//                            Toast.makeText(getContext(), finalErrorMessageAfterCrypto, Toast.LENGTH_LONG).show();
//
//                            // Dismiss the progress dialog even on error
//                            if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
//                        });
//                    }
//
//
//                }); // End of chatOpenExecutor.execute() for crypto/save Firebase
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // This callback runs on the main thread
//                Log.e(TAG, "Failed to fetch recipient public key for key generation (Firebase error)", error.toException());
//                chatOpenHandler.post(() -> { // Ensure dismissal and UI updates are on main thread
//                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
//                    Toast.makeText(getContext(), "Failed to start secure chat: Could not fetch recipient data.", Toast.LENGTH_SHORT).show();
//                });
//            }
//        }); // End of Recipient User Data Listener (addListenerForSingleValueEvent)
//    }
//
//    // *** NEW Helper method to Navigate to Chat Activity ***
//    // This method is called upon successful key retrieval/generation for a specific chat.
//    // It also handles the unread count update.
//    private void navigateToChatActivity(String conversationId, String recipientUserId, String recipientName, String recipientImageBase64) {
//        // Ensure context is still valid before starting activity
//        if (getContext() == null) {
//            Log.e(TAG, "Cannot navigate to chat, context is null.");
//            return;
//        }
//        Log.d(TAG, "Navigating to ChatPageActivity for conversationId: " + conversationId + ", Recipient ID: " + recipientUserId);
//        Intent intent = new Intent(getContext(), ChatPageActivity.class);
//        intent.putExtra("conversationId", conversationId); // Pass the consistent conversation ID
//        // Keeping old keys for compatibility with your ChatPageActivity if it still uses them
//        intent.putExtra("visit_users_ids", recipientUserId);
//        intent.putExtra("visit_users_name", recipientName);
//        intent.putExtra("visit_users_image", recipientImageBase64);
//        // Add flags to ensure clean task stack if needed (e.g., if you don't want to go back to contacts easily)
//        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Example
//
//        startActivity(intent); // Launch the ChatPageActivity
//
//
//        // --- Update unread count to 0 in Room for this chat ---
//        // This should be done immediately when the user clicks to open the chat for immediate UI feedback.
//        // Marking individual messages as seen in Firebase will happen in ChatPageActivity.
//        // Updating the unread count in the Firebase ChatSummary will also happen in ChatPageActivity
//        // when the user views the chat.
//        if (!TextUtils.isEmpty(currentUserID) && !TextUtils.isEmpty(recipientUserId) && chatDao != null && databaseExecutor != null) { // Basic validation
//            // We don't need to check if count > 0 here, the DAO update method will handle it.
//            // Just queue the update on the database executor.
//            Log.d(TAG, "Marking unread count to 0 in Room for chat with " + recipientUserId + " for owner " + currentUserID);
//            databaseExecutor.execute(() -> { // Run on DB thread
//                try {
//                    // Call the DAO method with the otherUserId, the new count (0), and the currentUserID (owner)
//                    // Make sure updateUnreadCount takes partnerId, newCount, and ownerId
//                    int updatedRows = chatDao.updateUnreadCount(recipientUserId, 0, currentUserID); // Ensure this DAO method exists
//                    if (updatedRows > 0) {
//                        Log.d(TAG, "Successfully updated unread count to 0 in Room for chat with " + recipientUserId);
//                    } else {
//                        // This might happen if the chat entry was somehow deleted from Room just before click (unlikely)
//                        Log.w(TAG, "Attempted to update unread count to 0, but no chat entity found for partner " + recipientUserId + " owned by " + currentUserID);
//                    }
//                } catch (Exception e) {
//                    Log.e(TAG, "Error updating unread count in Room for user " + recipientUserId, e);
//                }
//            });
//        } else {
//            Log.w(TAG, "Skipping unread count update in Room during navigation: Missing IDs, DAO, or Executor.");
//        }
//    }
//
//
//
//    @Override // From ChatAdapter.OnChatI
//    // nteractionListener
//    public void onChatLongClick(ChatEntity chat) {
//        // Handles long click on a chat item (e.g., to delete)
//        Log.d(TAG, "Chat long clicked: " + chat.getUsername() + " (Other User ID: " + chat.getUserId() + ")");
//        // Ensure chat entity is not null and has a valid partner ID
//        if (chat != null && !TextUtils.isEmpty(chat.getUserId())) {
//            showDeleteChatDialog(chat); // Call method to show the confirmation dialog
//        } else {
//            Log.w(TAG, "Cannot show delete dialog, ChatEntity is null or has empty userId.");
//            Toast.makeText(getContext(), "Error deleting chat.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // --- Helper Methods ---
//
//    // Method to check network availability (Kept from your code)
//    private boolean isNetworkAvailable() {
//        if (getContext() == null) return false; // Return false if context is null
//        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivityManager != null) {
//            try { // Add try-catch for security exceptions on some Android versions
//                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//            } catch (SecurityException e) {
//                Log.e(TAG, "SecurityException checking network state, ensure ACCESS_NETWORK_STATE permission is granted.", e);
//                // In production, you might inform the user or assume offline
//                return false; // Cannot confirm network state
//            } catch (Exception e) {
//                Log.e(TAG, "Error checking network state", e);
//                return false;
//            }
//        }
//        return false;
//    }
//
//    // Method to show delete chat confirmation dialog (Keep this)
//    private void showDeleteChatDialog(ChatEntity chat) {
//        if (getContext() == null) return; // Ensure context is available
//        // Ensure chat entity is not null and has partner ID for the message
//        if (chat == null || TextUtils.isEmpty(chat.getUserId())) {
//            Log.w(TAG, "Cannot show delete chat dialog, ChatEntity is null or has empty userId.");
//            Toast.makeText(getContext(), "Error deleting chat.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        new AlertDialog.Builder(getContext())
//                .setTitle("Delete Chat")
//                // Clarify that this only deletes from *this* user's list in Room and Firebase Summary
//                .setMessage("Are you sure you want to delete this chat with " + chat.getUsername() + "? This will remove it from your chat list and clear your local message history for this conversation.")
//                // Note: This does NOT delete messages for the other user unless you implement "Delete for Everyone" logic here too.
//                .setPositiveButton("Delete", (dialog, which) -> deleteChat(chat.getUserId(), chat.getConversationId())) // <<< MODIFIED: Pass conversationId too
//                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                .show();
//    }
//
//    // Method to delete chat entry from Room DB for the current user's list
//    // and also delete the corresponding summary entry from Firebase.
//    // Also delete messages and conversation key from Room.
//    // Pass conversationId to this method.
//    // In ChatFragment.java, locate the deleteChat(String userIdToDelete, String conversationIdToDelete) method.
//// Modify the databaseExecutor.execute block inside it:
//
//    // Method to delete chat entry from Room DB for the current user's list
//    // and also delete the corresponding summary entry from Firebase.
//    // Also delete messages and conversation key from Room.
//    // Pass conversationId to this method.
//    private void deleteChat(String userIdToDelete, String conversationIdToDelete) { // userIdToDelete is the chat partner's ID
//
//        if (currentUserID == null) {
//            Log.e(TAG, "Cannot delete chat, currentUserID is null.");
//            if (getContext() != null) {
//                Toast.makeText(getContext(), "Error: Could not delete chat. User not logged in?", Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }
//        // Ensure the partner ID and conversation ID are valid, and Room DB is initialized
//        if (TextUtils.isEmpty(userIdToDelete) || TextUtils.isEmpty(conversationIdToDelete) || rootRef == null || chatDatabase == null) { // Check Room DB too
//            Log.e(TAG, "Cannot delete chat, chatPartnerId, conversationId, rootRef, or chatDatabase is null or empty.");
//            Toast.makeText(getContext(), "Error deleting chat.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Log.d(TAG, "Attempting to delete chat entry from Room with partner: " + userIdToDelete + " for owner: " + currentUserID + ", Conversation ID: " + conversationIdToDelete + ", and delete Firebase summary.");
//
//        // --- First, attempt to delete the summary entry from Firebase for the current user ---
//        // The path is ChatSummaries/{currentUserID}/{chatPartnerId}
//        DatabaseReference firebaseSummaryRef = rootRef.child("ChatSummaries").child(currentUserID).child(userIdToDelete);
//        firebaseSummaryRef.removeValue().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Log.d(TAG, "Successfully deleted chat summary from Firebase for partner: " + userIdToDelete + " owner: " + currentUserID);
//
//                // --- Now, delete the corresponding entries from Room DB on a background thread ---
//                // This includes the ChatEntity, all messages for this chat pair, and the conversation key.
//                databaseExecutor.execute(() -> { // Run on Room DB thread
//                    try {
//                        // 1. Delete the ChatEntity for this partner, owned by the current user
//                        // Make sure this DAO method exists and deletes WHERE ownerUserId = :ownerId AND userId = :userIdToDelete
//                        int deletedSummaryRows = chatDao.deleteChatByUserId(userIdToDelete, currentUserID); // Assuming this method exists
//                        if (deletedSummaryRows > 0) {
//                            Log.d(TAG, "Successfully deleted chat list entry from Room for partner: " + userIdToDelete + " owned by: " + currentUserID);
//                        } else {
//                            Log.w(TAG, "Attempted to delete chat list entry from Room for partner " + userIdToDelete + ", but it wasn't found for owner " + currentUserID);
//                        }
//
//                        // 2. Delete the related messages from Room for this conversation
//                        try {
//                            // Assuming MessageDao has deleteAllMessagesForChat(ownerId, conversationId)
//                            // Use the conversationIdToDelete received by the method.
//                            int deletedMessageRows = messageDao.deleteAllMessagesForChat(currentUserID, conversationIdToDelete); // Assuming a DAO method that takes owner and convId
//                            Log.d(TAG, "Successfully deleted " + deletedMessageRows + " messages from Room for conversation " + conversationIdToDelete + " for owner " + currentUserID + " during chat deletion.");
//                        } catch (Exception msgDeleteEx) {
//                            Log.e(TAG, "Error deleting messages from Room during chat deletion for conv " + conversationIdToDelete + " for owner " + currentUserID + ".", msgDeleteEx);
//                        }
//
//
//                        // *** NEW ADDITION START ***
//                        // 3. Delete the conversation key from Room for this chat for the current user
//                        try {
//                            // Assuming ConversationKeyDao has deleteKeyById(ownerId, conversationId)
//                            // Use the conversationIdToDelete received by the method. Access the new DAO via chatDatabase instance.
//                            int deletedKeyRows = chatDatabase.conversationKeyDao().deleteKeyById(currentUserID, conversationIdToDelete); // <<< ADD THIS LINE
//                            if (deletedKeyRows > 0) {
//                                Log.d(TAG, "Deleted conversation key from Room during chat deletion for conv " + conversationIdToDelete + " for owner " + currentUserID + ".");
//                                // Optional: Remove from in-memory KeyManager if it was loaded and is still there
//                                YourKeyManager.getInstance().removeConversationKey(conversationIdToDelete); // <<< ADD THIS LINE
//                            } else {
//                                Log.w(TAG, "Attempted to delete conversation key but it was not found in Room during chat deletion for conv " + conversationIdToDelete);
//                            }
//                        } catch (Exception keyDeleteEx) {
//                            Log.e(TAG, "Error deleting conversation key from Room during chat deletion for conv " + conversationIdToDelete + " for owner " + currentUserID + ".", keyDeleteEx);
//                        }
//                        // *** NEW ADDITION END ***
//
//
//                        // Show success toast on the UI thread after local deletion is attempted
//                        // Use getActivity() != null and !getActivity().isFinishing() for safety in fragments
//                        if (getActivity() != null && !getActivity().isFinishing()) {
//                            getActivity().runOnUiThread(() ->
//                                    Toast.makeText(getContext(), "Chat cleared.", Toast.LENGTH_SHORT).show() // Changed toast text
//                            );
//                        }
//
//                    } catch (Exception e) {
//                        // This catch block primarily handles errors in deleting ChatEntity itself
//                        Log.e(TAG, "Error deleting chat list entry or messages from Room for partner: " + userIdToDelete + " owned by: " + currentUserID, e);
//                        // Show local deletion error toast on the UI thread
//                        if (getActivity() != null && !getActivity().isFinishing()) {
//                            getActivity().runOnUiThread(() ->
//                                    Toast.makeText(getContext(), "Failed to clear chat locally", Toast.LENGTH_SHORT).show()
//                            );
//                        }
//                    }
//                }); // End Room deletion executor
//
//            } else {
//                // Firebase deletion failed. Log the error and inform the user.
//                Log.e(TAG, "Failed to delete chat summary from Firebase for partner: " + userIdToDelete + " owner: " + currentUserID, task.getException());
//                // Show error toast on the UI thread
//                if (getActivity() != null && !getActivity().isFinishing()) {
//                    getActivity().runOnUiThread(() ->
//                            Toast.makeText(getContext(), "Failed to delete chat from server.", Toast.LENGTH_SHORT).show()
//                    );
//                }
//                // Decide if you still want to delete from Room even if Firebase fails? Probably not, for consistency.
//            }
//        }); // End Firebase deletion listener
//    }
//
//
//    // Method to manually refresh data (if needed, currently called by refresh action)
//    public void refreshData() {
//        Log.d(TAG,"Manual refresh triggered.");
//        // The existing Firebase listener should automatically update when data changes.
//        // If you want to force a re-sync on refresh, you could remove and re-add the listener.
//        // However, addValueEventListener generally stays active and updates automatically.
//        // A simple Toast indicating sync attempt is sufficient if the listener is always active.
//        if (isNetworkAvailable() && chatSummariesRef != null && currentUserID != null) { // Added currentUserID check
//            if (getContext() != null) {
//                Toast.makeText(getContext(), "Refreshing chats...", Toast.LENGTH_SHORT).show();
//            }
//            // The attachChatSummaryListener checks if a listener exists. If not, it attaches.
//            // If already attached, this call does nothing, but the listener *should* trigger
//            // on any data change in Firebase anyway.
//            // If you want a *full* re-fetch including users data, you might need a more specific sync method.
//            // For now, relying on the listener should be okay.
//            // attachChatSummaryListener(); // No need to re-attach if already attached unless listener was null
//        } else if (!isNetworkAvailable()) {
//            if (getContext() != null) {
//                Toast.makeText(getContext(), "Network unavailable. Cannot refresh.", Toast.LENGTH_SHORT).show();
//            }
//            // If offline, LiveData will still show cached data.
//        } else { // chatSummariesRef or currentUserID is null
//            Log.w(TAG, "Cannot refresh data: Firebase ref or user ID is null.");
//            if (getContext() != null) {
//                Toast.makeText(getContext(), "Error refreshing data.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//
//
//
//
//    // In ChatFragment.java, add this new public method:
//
//    /**
//     * Forces the chat list to re-render its current data from the LiveData's last value.
//     * Useful when decryption keys become available after the data was initially loaded.
//     * Assumes the LiveData's last value is still in the `chatListLiveData` object.
//     */
//
//    public void forceRefreshDisplay() {
//        Log.d(TAG, "forceRefreshDisplay() called in ChatFragment.");
//        // Ensure LiveData has a value and adapter exists
//        if (chatListLiveData != null && chatListLiveData.getValue() != null && chatAdapter != null) {
//            Log.d(TAG, "Re-processing and submitting current LiveData value (" + chatListLiveData.getValue().size() + " items) to ChatAdapter.");
//
//            // Get the current list of ChatEntity items from the LiveData's last value
//            List<ChatEntity> currentItems = chatListLiveData.getValue();
//
//            // Manually process and submit the list again.
//            // This logic is the same as the end of the onChanged method.
//            // It will trigger the decryption check for each visible item in onBindViewHolder.
//
//            List<ChatEntity> processedChatList = new ArrayList<>();
//            if (currentItems != null) {
//                for (ChatEntity chat : currentItems) {
//                    if (chat == null) continue;
//
//                    String encryptedLastMessage = chat.getLastMessage();
//                    String chatPartnerId = chat.getUserId();
//                    String conversationId = chat.getConversationId();
//                    String lastMessageType = chat.getLastMessageType();
//                    String username = chat.getUsername();
//                    String profileImage = chat.getProfileImage();
//                    long timestamp = chat.getTimestamp();
//                    int unreadCount = chat.getUnreadCount();
//                    String ownerUserId = chat.getOwnerUserId();
//
//                    String displayedLastMessagePreview = "";
//                    String placeholder = "";
//                    // Use consistent placeholders for different message types
//                    if ("image".equals(lastMessageType)) placeholder = "[Image]";
//                    else if ("file".equals(lastMessageType)) placeholder = "[File]";
//                    else placeholder = ""; // Default for text or unknown
//
//                    // Check if the stored content is empty first
//                    if (TextUtils.isEmpty(encryptedLastMessage)) {
//                        displayedLastMessagePreview = placeholder; // Show placeholder if no content
//                    } else if ("text".equals(lastMessageType) || "image".equals(lastMessageType)) { // Only attempt decryption for message types that are encrypted
//                        // Attempt decryption if user's private key is available AND the conversation key is available in KeyManager's cache
//                        // The conversation key should have been loaded into KeyManager after account unlock (in Login/MainActivity)
//                        if (YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId)) {
//                            SecretKey conversationAESKey = YourKeyManager.getInstance().getLatestConversationKey(conversationId); // Get the key from KeyManager's memory cache
//
//                            // Double check key is not null after retrieving (should be handled by hasConversationKey, but defensive)
//                            if (conversationAESKey != null) {
//                                try {
//                                    // encryptedLastMessage is the Base64 encoded encrypted data from Room
//                                    byte[] encryptedBytes = Base64.decode(encryptedLastMessage, Base64.DEFAULT); // Use android.util.Base64 for decoding
//                                    String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytes, conversationAESKey); // Use your CryptoUtils method
//
//                                    if ("text".equals(lastMessageType)) {
//                                        // Show only a snippet for long text messages
//                                        int maxLength = 50; // Display max 50 characters in preview
//                                        displayedLastMessagePreview = decryptedContent.length() > maxLength ?
//                                                decryptedContent.substring(0, maxLength) + "..." :
//                                                decryptedContent; // Use decrypted text or snippet
//                                        // Log.d(TAG, "Last message preview decrypted successfully: " + displayedLastMessagePreview);
//
//                                    } else if ("image".equals(lastMessageType)) {
//                                        // For image previews in the list, we typically just show "[Image]".
//                                        // The actual decrypted image Base64 is in the message.getMessage() of the *MessageEntity*, not ChatEntity.
//                                        // The ChatEntity's lastMessage is just a preview snippet/placeholder for the list.
//                                        displayedLastMessagePreview = placeholder; // For image, show placeholder like "[Image]" in the list preview
//
//                                        // Alternative (less common for list preview): If you stored the decrypted image Base64 preview
//                                        // in the ChatEntity.lastMessage field, you might use it here. But the video shows "[Locked]" or text previews.
//                                        // Example: if ChatEntity.lastMessage stores Base64 image after decryption,
//                                        // you might try to load a tiny thumbnail here if needed, but simpler to use placeholder.
//
//
//                                    } else { // Should not happen based on outer if condition, but fallback
//                                        displayedLastMessagePreview = "[Unknown Type - Decrypted]";
//                                        Log.w(TAG, "Unexpected message type '" + lastMessageType + "' processed for decryption in chat " + conversationId);
//                                    }
//
//                                } catch (IllegalArgumentException e) { // Base64 decoding error
//                                    Log.e(TAG, "Base64 decoding error decrypting last message preview for chat " + conversationId, e);
//                                    displayedLastMessagePreview = "[Invalid Data]"; // Placeholder on Base64 error
//                                } catch (Exception e) { // Catch decryption errors (wrong key, corrupt data, padding issues)
//                                    Log.e(TAG, "Decryption failed for last message preview in chat " + conversationId, e);
//                                    displayedLastMessagePreview = "[Encrypted Message - Failed]"; // Placeholder on crypto error
//                                }
//                            } else {
//                                // Conversation key missing from KeyManager (shouldn't happen if hasConversationKey check passed, but defensive)
//                                Log.w(TAG, "Conversation key returned null from KeyManager for chat " + conversationId + " during decryption attempt.");
//                                displayedLastMessagePreview = "[Encrypted Message]"; // Placeholder
//                            }
//                        } else {
//                            // Secure chat is enabled and key is available, but stored content is empty.
//                            Log.w(TAG, "Secure chat enabled, but stored content is empty for encrypted last message preview.");
//                            displayedLastMessagePreview = placeholder; // Use type placeholder
//                        }
//                    } else {
//                        // Secure chat is NOT enabled, conversation key is missing, User's private key is missing,
//                        // OR the message type is NOT one we encrypt (e.g., file, or an unencrypted system message)
//                        // In this case, display the content as it is stored in Room DB.
//                        // If the content from Room is empty, use the type placeholder as a fallback.
//                        displayedLastMessagePreview = encryptedLastMessage; // Use the content stored in Room
//                        if (TextUtils.isEmpty(displayedLastMessagePreview)) {
//                            displayedLastMessagePreview = placeholder;
//                        }
//                        // Log if displaying potentially encrypted data when keys aren't available
//                        if (!YourKeyManager.getInstance().isPrivateKeyAvailable() && ("text".equals(lastMessageType) || "image".equals(lastMessageType))) {
//                            Log.w(TAG, "Displaying potential encrypted data for last message preview in chat " + conversationId + " because secure chat is disabled.");
//                        }
//                    }
//
//
//                    // --- Create a NEW ChatEntity object for the adapter ---
//                    // Create a new entity and copy all necessary fields, setting the PROCESSED last message.
//                    // This ensures LiveData doesn't get modified entities if it's an unmodifiable list.
//                    ChatEntity processedChat = new ChatEntity(); // Use empty constructor
//                    processedChat.setId(chat.getId()); // Make sure to copy Room auto-generated ID
//                    processedChat.setOwnerUserId(ownerUserId); // Keep the owner ID
//                    processedChat.setUserId(chatPartnerId); // Keep the chat partner ID
//                    processedChat.setConversationId(conversationId); // Keep the conversation ID
//                    processedChat.setUsername(username); // Keep the username
//                    processedChat.setProfileImage(profileImage); // Keep the profile image
//                    processedChat.setLastMessage(displayedLastMessagePreview); // *** Set the PROCESSED/DECRYPTED preview content ***
//                    processedChat.setTimestamp(timestamp); // Keep the timestamp
//                    processedChat.setUnreadCount(unreadCount); // Keep the unread count
//                    processedChat.setLastMessageType(lastMessageType); // Keep the message type
//
//                    processedChatList.add(processedChat); // Add the processed entity to the list for the adapter
//                }
//            }
//            // --- END Processing Messages ---
//
//
//            // Sort the list by timestamp DESC (latest message first) if not already sorted by DAO query
//            // Make sure your DAO query already sorts by timestamp DESC, then this is redundant but safe.
//            Collections.sort(processedChatList, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
//
//
//            // Submit the list of ChatEntity with processed last messages to the adapter
//            if (chatAdapter != null) {
//                // Use submitList (more efficient with DiffUtil if implemented in adapter). Pass a *copy* if needed.
//                // Using ArrayList<>(processedChatList) ensures a new list is passed, preventing issues with adapter's internal list reference.
//                chatAdapter.submitList(new ArrayList<>(processedChatList));
//                Log.d(TAG, "Submitted " + processedChatList.size() + " processed chats to adapter in forceRefreshDisplay.");
//            } else {
//                Log.e(TAG, "ChatAdapter is null in forceRefreshDisplay, cannot submit list.");
//            }
//
//
//            // Update UI visibility based on the final list size
//            if (processedChatList.size() > 0) { // Check size > 0 for non-empty list
//                privateChatsList.setVisibility(View.VISIBLE);
//                noChatsText.setVisibility(View.GONE);
//                // Show search view only if there are chats to search
//                if (searchView != null) searchView.setVisibility(View.VISIBLE);
//                Log.d(TAG, "Chat list visible after forceRefreshDisplay.");
//            } else {
//                privateChatsList.setVisibility(View.GONE);
//                noChatsText.setVisibility(View.VISIBLE);
//                noChatsText.setText("No chats yet"); // Message when list is empty
//                // Hide search view if there are no chats
//                if (searchView != null) searchView.setVisibility(View.GONE);
//                Log.d(TAG, "No chats found after forceRefreshDisplay, showing No chats text.");
//            }
//
//            // Re-apply search filter if there's an active query
//            // The adapter's filter method should work on the list containing *displayed* previews
//            if (searchView != null && chatAdapter != null) {
//                String currentQuery = searchView.getQuery().toString();
//                if (!currentQuery.isEmpty()) {
//                    Log.d(TAG, "Applying search filter after force refresh: '" + currentQuery + "'");
//                    // Ensure adapter's filter method works with the list containing processed previews
//                    // The adapter's filter logic should compare the *displayed* last message preview text
//                    chatAdapter.filter(currentQuery);
//                }
//            } else {
//                Log.w(TAG, "SearchView or ChatAdapter is null in forceRefreshDisplay, cannot re-apply filter.");
//            }
//
//        } else {
//            Log.w(TAG, "forceRefreshDisplay() skipped: LiveData, value, or adapter is null.");
//            // If LiveData has no value, the onChanged would likely have run already and updated the UI.
//        }
//    }
//
//
//
//
//
//
//
//}
//
//
//


























//
//public class ChatFragment extends Fragment implements ChatAdapter.OnChatInteractionListener {
//
//            private static final String TAG = "ChatFragment";
//
//            private View privateChatsView;
//            private RecyclerView privateChatsList;
//            private TextView noChatsText;
//            private SearchView searchView; // Use SearchView from androidx.appcompat.widget
//
//            private DatabaseReference rootRef;
//            private DatabaseReference usersRef;
//            private DatabaseReference chatSummariesRef; // Reference to the current user's chat summaries
//
//            private FirebaseAuth mAuth;
//            private String currentUserID; // Current logged-in user's UID
//
//            // --- Room DB and DAO members ---
//            private ChatDatabase chatDatabase; // Room DB
//            private ChatDao chatDao; // Room DAO for Chat list items
//            private MessageDao messageDao; // Room DAO for individual messages (for deleting history)
//            private ExecutorService databaseExecutor; // Executor for Room DB operations
//
//            private ConversationKeyDao conversationKeyDao; // <<< Make sure this is also added and initialized in onCreate
//
//
//            // --- Members for Secure Chat Initiation from list click ---
//            private ExecutorService chatOpenExecutor; // For async key loading specific to click
//            private Handler chatOpenHandler; // For posting UI updates back to Main Thread for click
//            private ProgressDialog chatOpenProgressDialog; // Progress dialog for chat setup process
//// --- End Members ---
//
//
//            private ChatAdapter chatAdapter; // Adapter for the RecyclerView
//
//            // LiveData from Room
//            private LiveData<List<ChatEntity>> chatListLiveData;
//
//    private Handler mainHandler; // Add this member variable
//    // --- End NEW ---
//
//            // Firebase listener
//            private ValueEventListener chatSummaryListener; // Firebase listener for ChatSummaries
//
//            // Keep track of Firebase listener attachment state
//            private boolean isChatSummaryListenerAttached = false; // <<< ADD THIS FLAG
//
//            @SuppressLint("RestrictedApi") // Needed for setHasOptionsMenu if used with older support library, remove if targeting newer AndroidX
//            @Override
//            public void onCreate(@Nullable Bundle savedInstanceState) {
//                super.onCreate(savedInstanceState);
//                Log.d(TAG, "🟢 ChatFragment onCreate.");
//                setHasOptionsMenu(true); // Optional: If you want to handle options menu for search etc.
//
//
//                // --- Initialize Firebase Auth ---
//                mAuth = FirebaseAuth.getInstance();
//                FirebaseUser currentUser = mAuth.getCurrentUser();
//                if (currentUser != null) {
//                    currentUserID = currentUser.getUid();
//                    Log.d(TAG, "Current User ID: " + currentUserID);
//                } else {
//                    Log.e(TAG, "User not authenticated in ChatFragment onCreate! This fragment should not be visible in this state.");
//                    currentUserID = null; // Ensure currentUserID is null if user is not authenticated
//                    // Handle authentication error - e.g., redirect to login or show an error screen
//                    if (getContext() != null) {
//                        Toast.makeText(getContext(), "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
//                        // Consider navigating to login activity if this fragment is reached without auth
//                        if (getActivity() != null) { // Check if fragment is attached to an activity
//                            Intent loginIntent = new Intent(getActivity(), Login.class); // Assuming Login exists
//                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(loginIntent);
//                            getActivity().finish(); // Finish hosting activity
//                        }
//                    }
//                }
//                // --- End Firebase Auth ---
//
//
//                // --- Initialize Firebase Refs ---
//                // Only initialize if currentUserID is available
//                if (currentUserID != null) {
//                    rootRef = FirebaseDatabase.getInstance().getReference();
//                    usersRef = rootRef.child("Users");
//                    // Reference to the node containing chat summaries for THIS user
//                    // Structure: ChatSummaries/{currentUserID}/{chatPartnerId}/...
//                    chatSummariesRef = rootRef.child("ChatSummaries").child(currentUserID); // Corrected path from previous code
//                    Log.d(TAG, "ChatSummaries Ref: " + chatSummariesRef.getPath()); // Log path
//                } else {
//                    Log.w(TAG, "currentUserID is null, Firebase Refs will not be initialized in onCreate.");
//                    rootRef = null;
//                    usersRef = null;
//                    chatSummariesRef = null;
//                }
//                // --- End Initialize Firebase Refs ---
//
//
//                // --- Initialize Room DB and Executors ---
//                // Always initialize Room DB as it provides offline data even if not authenticated,
//                // although you won't see user-specific data without a valid owner ID.
//                // Use requireContext() which is safe in Fragment's onCreate()
//                chatDatabase = ChatDatabase.getInstance(requireContext()); // Use requireContext() in Fragment
//                chatDao = chatDatabase.chatDao();
//                messageDao = chatDatabase.messageDao(); // Initialize MessageDao
//                conversationKeyDao = chatDatabase.conversationKeyDao(); // <<< INITIALIZE THE NEW DAO HERE
//                databaseExecutor = ChatDatabase.databaseWriteExecutor; // Use the shared executor
//                // --- End Initialize Room DB ---
//
//
//                // --- NEW: Initialize Main Handler ---
//                mainHandler = new Handler(Looper.getMainLooper()); // Initialize the Main Handler here
//                Log.d(TAG, "Main Handler initialized in ChatFragment onCreate.");
//                // --- End NEW ---
//
//                // NEW: After listeners and observers are set, check if keys are already available
//                // and trigger a UI refresh to ensure correct decryption display.
//                // This is important if keys were loaded in MainActivity's onStart before this fragment's view was created.
//                boolean isPrivateKeyAvailableNow = YourKeyManager.getInstance().isPrivateKeyAvailable();
//                boolean areAnyConversationKeysCachedNow = YourKeyManager.getInstance().getTotalCachedConversationKeyVersions() > 0; // Check total count, not just for this conv initially
//
//                Log.d(TAG, "onViewCreated: Checking KeyManager state after setup. Private Available: " + isPrivateKeyAvailableNow + ", Total Conv Key Versions Cached: " + YourKeyManager.getInstance().getTotalCachedConversationKeyVersions());
//
//                if (isPrivateKeyAvailableNow && areAnyConversationKeysCachedNow) {
//                    Log.d(TAG, "onViewCreated: Keys are available in KeyManager. Triggering forceRefreshDisplay for initial rendering.");
//                    // Post to main thread to avoid potential issues with initial layout pass
//                    mainHandler.post(() -> forceRefreshDisplay()); // Assuming you have a mainHandler in ChatFragment, or use getActivity().runOnUiThread
//                } else {
//                    Log.d(TAG, "onViewCreated: Keys not available in KeyManager yet. Initial display will show placeholders.");
//                }
//
//
//                // Show initial loading text until Room data loads or sync happens
//                // Keep this, LiveData observer will hide/show based on data
//                if (noChatsText != null) {
//                    noChatsText.setVisibility(View.VISIBLE);
//                    // More specific message if keys are not available initially
//                    if (!isPrivateKeyAvailableNow) {
//                        noChatsText.setText("Loading chats...\nAccount locked.");
//                    } else if (!areAnyConversationKeysCachedNow) {
//                        noChatsText.setText("Loading chats...\nSecure keys not loaded.");
//                    } else {
//                        noChatsText.setText("Loading chats...");
//                    }
//                }
//                if (privateChatsList != null) privateChatsList.setVisibility(View.GONE);
//                if (searchView != null) searchView.setVisibility(View.GONE);
//
//    // --- Initialize Executor and Handler for chat opening async tasks ---
//                chatOpenExecutor = Executors.newSingleThreadExecutor(); // Dedicated executor for click tasks
//                chatOpenHandler = new Handler(Looper.getMainLooper()); // Handler for UI updates after click tasks
//                // Initialize dialog (use requireContext() as it's available in Fragment onCreate)
//                chatOpenProgressDialog = new ProgressDialog(requireContext());
//                chatOpenProgressDialog.setCancelable(false);
//                // Make sure it's owned by the parent activity to prevent window leaks
//                if (getActivity() != null) {
//                    chatOpenProgressDialog.setOwnerActivity(getActivity());
//                }
//                // --- End Initialization ---
//
//                Log.d(TAG, "✅ onCreate finished in ChatFragment.");
//            }
//
//            @Override
//            public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//                Log.d(TAG, "🟢 onCreateView started.");
//                // Assuming R.layout.fragment_chat contains R.id.chat_list, R.id.noChatsText, R.id.search_view
//                privateChatsView = inflater.inflate(R.layout.fragment_chat, container, false); // Ensure fragment_chat.xml exists
//
//                // Initialize UI views
//                privateChatsList = privateChatsView.findViewById(R.id.chat_list); // Match your layout ID
//                noChatsText = privateChatsView.findViewById(R.id.noChatsText); // Match your layout ID
//                searchView = privateChatsView.findViewById(R.id.search_view); // Make sure ID matches layout
//
//                setupRecyclerView(); // Setup RecyclerView and Adapter
//                setupSearchView(); // Setup SearchView
//
//                Log.d(TAG, "✅ onCreateView finished.");
//                return privateChatsView;
//            }
//
//            @Override
//            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//                super.onViewCreated(view, savedInstanceState);
//                Log.d(TAG, "🟢 onViewCreated started.");
//
//                // Check authentication again here, as the view might be created after onCreate.
//                // If currentUserID is null, display an error state.
//                if (currentUserID == null) {
//                    Log.e(TAG, "Cannot proceed in ChatFragment onViewCreated without currentUserID. Showing error UI.");
//                    if (noChatsText != null) { // Safety check
//                        noChatsText.setVisibility(View.VISIBLE);
//                        noChatsText.setText("Authentication Error");
//                    }
//                    if (privateChatsList != null) privateChatsList.setVisibility(View.GONE); // Safety check
//                    if (searchView != null) searchView.setVisibility(View.GONE); // Hide search if no user
//                    // Also hide bottom navigation if applicable in the parent Activity or FragmentManager
//                    Log.d(TAG, "✅ onViewCreated finished with auth error.");
//                    return; // Stop execution in this method
//                }
//
//
//                // --- Observe Room LiveData for changes ---
//                // This observer runs on the main thread when data changes in Room.
//                // Decryption of the last message preview happens *inside* this observer.
//                observeChatList(); // Attach Room LiveData observer
//
//
//                // --- Attach Firebase Listener for ChatSummaries ---
//                // This listener syncs data from Firebase ChatSummaries to Room.
//                // It should be attached whenever the fragment's view is active (onViewCreated or onStart).
//                // It should be detached in onDestroyView or onStop.
//                // We attach the listener regardless of network status. Firebase handles offline caching/sync.
//                Log.d(TAG, "Attaching Firebase listener for ChatSummaries in onViewCreated.");
//                attachChatSummaryListener(); // Attach the listener
//
//                // Show initial loading text until Room data loads or sync happens
//                if (noChatsText != null) { // Safety check
//                    noChatsText.setVisibility(View.VISIBLE);
//                    noChatsText.setText("Loading chats...");
//                }
//                if (privateChatsList != null) privateChatsList.setVisibility(View.GONE); // Safety check
//                if (searchView != null) searchView.setVisibility(View.GONE); // Safety check
//
//                // Check network status for informational toast only (listener works offline)
//                if (!isNetworkAvailable()) {
//                    Log.d(TAG, "Network unavailable in onViewCreated. Relying on cached data from Room.");
//                    if (getContext() != null) {
//                        // Show toast only once when network is unavailable (maybe use a flag or ViewModel to show only once)
//                        Toast.makeText(getContext(), "Offline mode: Showing cached chats", Toast.LENGTH_SHORT).show();
//                    }
//                    // LiveData observer is already attached and will show cached data.
//                }
//                Log.d(TAG, "✅ onViewCreated finished.");
//            }
//
//
//            // --- Setup RecyclerView and Adapter ---
//            private void setupRecyclerView() {
//                Log.d(TAG, "Setting up RecyclerView.");
//                if (privateChatsList == null) {
//                    Log.w(TAG, "RecyclerView is null, cannot setup.");
//                    return;
//                }
//                privateChatsList.setLayoutManager(new LinearLayoutManager(getContext()));
//                // Initialize ChatAdapter with context, empty list, listener, AND currentUserID
//                // Pass currentUserID here. This ID is used by the adapter to determine message alignment/sender.
//                // Use requireContext() as onCreateView guarantees context is available.
//                chatAdapter = new ChatAdapter(requireContext(), new ArrayList<>(), this, currentUserID); // Pass currentUserID here
//                privateChatsList.setAdapter(chatAdapter);
//            }
//
//            // --- Setup SearchView ---
//            private void setupSearchView() {
//                Log.d(TAG, "Setting up SearchView.");
//                // Use androidx.appcompat.widget.SearchView
//                if (searchView != null) { // Safety check
//                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                        @Override
//                        public boolean onQueryTextSubmit(String query) {
//                            return false; // Don't handle submission by default
//                        }
//
//                        @Override
//                        public boolean onQueryTextChange(String newText) {
//                            Log.d(TAG, "Search query changed: " + newText);
//                            // Check if chatAdapter is not null before filtering
//                            if (chatAdapter != null) {
//                                // The adapter's filter method should work on the list containing *displayed* previews
//                                // Assuming ChatAdapter's filter method filters based on username or the displayed last message preview.
//                                chatAdapter.filter(newText); // Assuming your ChatAdapter has a filter method
//                            } else {
//                                Log.w(TAG, "ChatAdapter is null, cannot apply filter.");
//                            }
//                            return true; // Query handled
//                        }
//                    });
//                    // Add a listener for when the search view is closed
//                    searchView.setOnCloseListener(() -> {
//                        Log.d(TAG, "SearchView closed.");
//                        // Optionally reset the filter or load full data here if your filter implementation needs it
//                        // chatAdapter.filter(""); // Example: reset filter to empty string
//                        return false; // Allow the system to handle closing the search view
//                    });
//                } else {
//                    Log.w(TAG, "SearchView is null, skipping setup.");
//                }
//            }
//
//
//            // --- Observe LiveData from Room DAO ---
//            private void observeChatList() {
//                // Ensure currentUserID and chatDao are initialized before observing
//                if (currentUserID == null || chatDao == null) {
//                    Log.e(TAG, "currentUserID or chatDao is null, cannot observe chat list.");
//                    // Optionally show an error message in UI
//                    if (noChatsText != null) {
//                        noChatsText.setVisibility(View.VISIBLE);
//                        noChatsText.setText("Error loading chats: User not logged in.");
//                    }
//                    if (privateChatsList != null) privateChatsList.setVisibility(View.GONE);
//                    if (searchView != null) searchView.setVisibility(View.GONE);
//                    return; // Exit method if prerequisites are missing
//                }
//                Log.d(TAG, "Attaching Room LiveData observer for user: " + currentUserID);
//                // Get the LiveData from the DAO for chats owned by the current user, ordered by latest message timestamp
//                // Make sure your ChatDao.getAllChats method queries WHERE ownerUserId = :ownerUserId ORDER BY timestamp DESC
//                chatListLiveData = chatDao.getAllChats(currentUserID); // Assuming this method exists and takes ownerId
//
//                // Use getViewLifecycleOwner() for fragment-specific lifecycle.
//                // The observer will be automatically removed when the fragment's view is destroyed.
//                // Inside ChatFragment.java -> observeChatList method
//                // Find this block and replace it entirely with the code below:
//
//                chatListLiveData.observe(getViewLifecycleOwner(), new Observer<List<ChatEntity>>() { // Use Observer interface
//                    @Override
//                    public void onChanged(List<ChatEntity> chatEntities) {
//                        // This observer runs on the main thread whenever the data in Room changes
//                        Log.d(TAG, "LiveData onChanged triggered. Received " + (chatEntities != null ? chatEntities.size() : 0) + " chats from Room for owner: " + currentUserID);
//
//                        // Create a new list to hold ChatEntity objects with processed content for the adapter
//                        // It's safer to create a new list than modify the one provided by LiveData
//                        List<ChatEntity> processedChatList = new ArrayList<>();
//
//                        if (chatEntities != null) {
//                            for (ChatEntity chat : chatEntities) {
//                                // Ensure chat entity is not null
//                                if (chat == null) {
//                                    Log.w(TAG, "Skipping null ChatEntity from Room list.");
//                                    continue;
//                                }
//                                // --- Get all necessary fields from the original Room ChatEntity ---
//                                String encryptedLastMessage = chat.getLastMessage(); // This is the encrypted Base64 string or placeholder from Room
//                                String chatPartnerId = chat.getUserId();
//                                String conversationId = chat.getConversationId(); // Get the conversation ID
//                                String lastMessageType = chat.getLastMessageType(); // Get lastMessageType from ChatEntity
//                                String username = chat.getUsername();
//                                String profileImage = chat.getProfileImage();
//                                long timestamp = chat.getTimestamp();
//                                int unreadCount = chat.getUnreadCount();
//                                String ownerUserId = chat.getOwnerUserId(); // Should be currentUserID
//                                long lastMessageTimestamp = chat.getTimestamp(); // Get the message timestamp
//
//
//                                Log.d(TAG, "Preview Debug: Processing ChatEntity for Conv ID: " + conversationId +
//                                        ", Last Msg Timestamp: " + lastMessageTimestamp +
//                                        ", Type: " + lastMessageType +
//                                        ", Content Empty: " + TextUtils.isEmpty(encryptedLastMessage));
//
//
//                                String displayedLastMessagePreview = ""; // This will be the string shown in the UI bubble, initialize as empty or placeholder
//
//                                // --- Logic to determine what to display for the last message preview ---
//
//                                // Default placeholder based on type if content is empty or decryption fails
//                                String placeholder = "";
//                                if ("image".equals(lastMessageType)) placeholder = "[Image]";
//                                else if ("file".equals(lastMessageType)) placeholder = "[File]";
//                                else placeholder = ""; // Default for text or unknown
//
//
//
//
//                                boolean isPrivateKeyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable();
//                                // Get ALL conversation keys for this chat from KeyManager
//                                SortedMap<Long, SecretKey> convKeysForThisChat = YourKeyManager.getInstance().getAllConversationKeysForConversation(conversationId);
//                                boolean areAnyConversationKeysAvailable = !convKeysForThisChat.isEmpty();
//
//                                Log.d(TAG, "Preview Debug: Key State for Conv " + conversationId +
//                                        ": Private Available=" + isPrivateKeyAvailable +
//                                        ", Any Conv Keys Cached=" + areAnyConversationKeysAvailable +
//                                        ", Cached Key Timestamps=" + convKeysForThisChat.keySet()); // <-- Check ALL key timestamps here
//
//
//                                // Check if the stored content is empty first. If so, just show the placeholder.
//                                if (TextUtils.isEmpty(encryptedLastMessage)) {
//                                    displayedLastMessagePreview = placeholder; // Show placeholder if no content
//                                } else if ("text".equals(lastMessageType) || "image".equals(lastMessageType)) { // Only attempt decryption for message types that are encrypted
//
//                                    // <<< --- CHANGE START ---
//                                    // Ab yahan hum check kar rahe hain agar Private Key available hai aur KeyManager mein conversation keys hain.
//                                    // Agar hain, to humari strategy hai: jitne bhi keys KeyManager mein is conversation ke liye hain,
//                                    // un sabse is last message preview ko decrypt karne ki koshish karo.
//
//                                    if (isPrivateKeyAvailable && areAnyConversationKeysAvailable) {
//                                        Log.d(TAG, "Preview Debug: Attempting decryption for last message preview in chat " + conversationId + ". Trying " + convKeysForThisChat.size() + " cached key version(s).");
//
//                                        boolean decryptedSuccessfullyInPreview = false;
//                                        String tempDisplayedPreview = "[Encrypted Message - Failed]"; // Default if all fail
//
//                                        // Get timestamps in descending order (newest key first is a common strategy)
//                                        List<Long> sortedTimestampsDesc = new ArrayList<>(convKeysForThisChat.keySet());
//                                        Collections.sort(sortedTimestampsDesc, Collections.reverseOrder()); // Sort timestamps descending
//
//                                        // Loop through each available key version
//                                        for(Long keyTs : sortedTimestampsDesc) {
//                                            SecretKey potentialKey = convKeysForThisChat.get(keyTs);
//
//                                            if (potentialKey != null) {
//                                                try {
//                                                    byte[] encryptedBytes = Base64.decode(encryptedLastMessage, Base64.DEFAULT); // Decode Base64
//                                                    String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytes, potentialKey); // Attempt decryption
//
//                                                    // Agar decryption successful ho jaye bina exception ke:
//                                                    if ("text".equals(lastMessageType)) {
//                                                        int maxLength = 50; // Preview ki max length
//                                                        tempDisplayedPreview = decryptedContent.length() > maxLength ?
//                                                                decryptedContent.substring(0, maxLength) + "..." :
//                                                                decryptedContent; // Use decrypted text or snippet
//                                                    } else if ("image".equals(lastMessageType)) {
//                                                        tempDisplayedPreview = placeholder; // Image preview ke liye placeholder
//                                                    } else {
//                                                        tempDisplayedPreview = "[Decrypted Content]"; // Fallback
//                                                    }
//
//                                                    decryptedSuccessfullyInPreview = true; // Decryption successful
//                                                    Log.d(TAG, "Preview Debug: Decrypted last message preview for " + conversationId + " successfully with key version T:" + keyTs);
//                                                    break; // Jab successful ho jaye, to baaki keys try karne ki zaroorat nahi
//
//                                                } catch (javax.crypto.AEADBadTagException e) {
//                                                    // Decryption failed with THIS key version (common error for wrong key).
//                                                    // Continue loop to try next key.
//                                                    Log.d(TAG, "Preview Debug: Decryption FAILED with key T:" + keyTs + " for message preview " + conversationId + ". Trying next key.");
//                                                } catch (IllegalArgumentException e) { // Base64 decoding error
//                                                    Log.e(TAG, "Preview Debug: Base64 decoding error decrypting preview for " + conversationId + " with key T:" + keyTs, e);
//                                                    // Yeh corrupt data ho sakta hai. Try next key.
//                                                } catch (Exception e) {
//                                                    // Catch any other unexpected crypto errors for this key version
//                                                    Log.e(TAG, "Preview Debug: Unexpected error decrypting preview with key T:" + keyTs + " for " + conversationId, e);
//                                                    // Try next key
//                                                }
//                                            } // End if (potentialKey != null) check
//                                        } // End loop through sortedTimestampsDesc
//
//                                        // Loop khatam hone ke baad, dekho agar koi bhi key successful nahi hui
//                                        if (!decryptedSuccessfullyInPreview) {
//                                            Log.e(TAG, "Preview Debug: Decryption FAILED with ALL available keys for last message preview in " + conversationId + " (Type: " + lastMessageType + "). Showing placeholder.");
//                                            displayedLastMessagePreview = "[Encrypted Message - Failed]"; // Show failed placeholder
//                                        } else {
//                                            // Agar successful hua hai, to tempDisplayedPreview mein correct value hogi.
//                                            displayedLastMessagePreview = tempDisplayedPreview;
//                                        }
//
//                                    } else {
//                                        // User's private key is not available OR conversation key not loaded in KeyManager's cache.
//                                        // Decryption possible nahi hai.
//                                        Log.d(TAG, "Preview Debug: User's private key unavailable (" + !isPrivateKeyAvailable + ") OR No Conversation keys loaded (" + !areAnyConversationKeysAvailable + "). Cannot decrypt text preview for chat " + conversationId);
//                                        displayedLastMessagePreview = "[Locked]"; // Placeholder indicating account needs unlocking / keys not loaded
//                                    }
//
//                                    // <<< --- CHANGE END ---
//
//                                } else {
//                                    // Message type is not text or image (e.g., file, or an unencrypted system message)
//                                    // Ya content empty hai. Ya secure chat disabled hai.
//                                    // Yahan woh content dikhao jo Room mein saved hai (placeholder like "[File]" ya plaintext).
//                                    // Agar saved content empty hai, to type placeholder use karo.
//                                    displayedLastMessagePreview = encryptedLastMessage; // Use the content stored in Room
//                                    if (TextUtils.isEmpty(displayedLastMessagePreview)) {
//                                        displayedLastMessagePreview = placeholder;
//                                    }
//                                    // Optional: Log karo agar keys available nahi hain par potentially encrypted data display ho raha hai
//                                    // if (!isPrivateKeyAvailable && ("text".equals(lastMessageType) || "image".equals(lastMessageType))) {
//                                    //     Log.w(TAG, "Preview Debug: Displaying potential encrypted data for last message preview in chat " + conversationId + " because secure chat is disabled.");
//                                    // }
//                                }
//
//
//                                // --- Create a NEW ChatEntity object for the adapter ---
//                                // Create a new entity and copy all necessary fields, setting the PROCESSED last message.
//                                // Yeh LiveData entities modify hone se bachata hai agar woh unmodifiable list ho.
//                                ChatEntity processedChat = new ChatEntity(); // Use empty constructor
//                                processedChat.setId(chat.getId()); // Make sure to copy Room auto-generated ID
//                                processedChat.setOwnerUserId(ownerUserId); // Keep the owner ID
//                                processedChat.setUserId(chatPartnerId); // Keep the chat partner ID
//                                processedChat.setConversationId(conversationId); // Keep the conversation ID
//                                processedChat.setUsername(username); // Keep the username
//                                processedChat.setProfileImage(profileImage); // Keep the profile image
//                                processedChat.setLastMessage(displayedLastMessagePreview); // *** Set the PROCESSED/DECRYPTED preview content ***
//                                processedChat.setTimestamp(timestamp); // Keep the timestamp
//                                processedChat.setUnreadCount(unreadCount); // Keep the unread count
//                                processedChat.setLastMessageType(lastMessageType); // Keep the message type
//                                // Agar ChatEntity mein partnerKeysChanged flag hai to usko bhi copy karo
//                                // processedChat.setPartnerKeysChanged(chat.isPartnerKeysChanged()); // Assuming this getter exists
//
//
//                                processedChatList.add(processedChat); // Add the processed entity to the list for the adapter
//                            }
//                        }
//                        // --- END Processing Messages ---
//
//                        // List ko timestamp DESC (latest message first) order karo agar DAO query ne nahi kiya hai
//                        // Agar aapki DAO query already sort karti hai, to yeh step redundant hai par safe hai.
//                        if (processedChatList.size() > 1) { // Only sort if more than one item
//                            Collections.sort(processedChatList, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
//                        }
//
//
//                        // Adapter ko processed chats ki list submit karo
//                        if (chatAdapter != null) {
//                            // submitList use karo (DiffUtil ke saath zyada efficient). Ek copy pass karo agar zaroori ho.
//                            // ArrayList<>(processedChatList) ek naya list pass karta hai, adapter ke internal list reference ke issues se bachata hai.
//                            chatAdapter.submitList(new ArrayList<>(processedChatList));
//                            Log.d(TAG, "Submitted " + processedChatList.size() + " processed chats to adapter.");
//                        } else {
//                            Log.e(TAG, "ChatAdapter is null, cannot submit list.");
//                        }
//
//
//                        // UI visibility ko update karo based on final list size
//                        if (processedChatList.size() > 0) { // Check size > 0 for non-empty list
//                            if (privateChatsList != null) privateChatsList.setVisibility(View.VISIBLE); // Safety check
//                            if (noChatsText != null) noChatsText.setVisibility(View.GONE); // Safety check
//                            // Search view ko tabhi dikhao jab search karne ke liye chats hon
//                            if (searchView != null) searchView.setVisibility(View.VISIBLE); // Safety check
//                            Log.d(TAG, "Chat list visible.");
//                        } else {
//                            if (privateChatsList != null) privateChatsList.setVisibility(View.GONE); // Safety check
//                            if (noChatsText != null) { // Safety check
//                                noChatsText.setVisibility(View.VISIBLE);
//                                noChatsText.setText("No chats yet"); // Message jab list empty ho
//                            }
//                            // Search view ko hide karo agar koi chats nahi hain
//                            if (searchView != null) searchView.setVisibility(View.GONE); // Safety check
//                            Log.d(TAG, "No chats found after LiveData update, showing No chats text.");
//                        }
//
//                        // Re-apply search filter agar koi active query hai
//                        // Adapter ka filter method displayed previews par kaam karna chahiye
//                        if (searchView != null && chatAdapter != null) {
//                            String currentQuery = searchView.getQuery().toString();
//                            if (!TextUtils.isEmpty(currentQuery)) { // Check if query is not empty
//                                Log.d(TAG, "Applying search filter after LiveData update: '" + currentQuery + "'");
//                                // Ensure adapter's filter method works with the list containing processed previews
//                                // Adapter ka filter logic displayed last message preview text ko compare karega
//                                chatAdapter.filter(currentQuery);
//                            }
//                        } else {
//                            Log.w(TAG, "SearchView or ChatAdapter is null, cannot re-apply filter.");
//                        }
//                    }
//                });
//            }
//            // --- End Observe LiveData ---
//
//
//            // --- Attach Real-time Listener for Firebase Chat Summaries (Keep This) ---
//            // This listener listens to the current user's chat summaries node in Firebase
//            // and syncs changes to the local Room database.
//            // @SuppressLint("RestrictedApi") // Keep if needed for any hidden APIs used internally by Firebase library
//            @SuppressLint("RestrictedApi")
//            private void attachChatSummaryListener() {
//                // Ensure Firebase refs and currentUserID are initialized before attaching
//                if (chatSummariesRef == null || currentUserID == null || databaseExecutor == null || chatDao == null) { // Added DB checks
//                    Log.e(TAG, "chatSummariesRef, currentUserID, databaseExecutor, or chatDao is null, cannot attach listener.");
//                    // Update UI to indicate error if user is expected to be logged in
//                    if (noChatsText != null && currentUserID != null) {
//                        noChatsText.setVisibility(View.VISIBLE);
//                        noChatsText.setText("Error: Could not load chat updates.");
//                        privateChatsList.setVisibility(View.GONE);
//                        if (searchView != null) searchView.setVisibility(View.GONE);
//                    }
//                    return;
//                }
//
//                // Only attach if not already attached
//                if (!isChatSummaryListenerAttached) { // Use the flag
//                    Log.d(TAG, "Attaching Firebase ChatSummaries listener to: " + chatSummariesRef.getPath());
//                    chatSummaryListener = new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            Log.d(TAG, "Firebase ChatSummaries data received for user: " + currentUserID + ". Processing " + dataSnapshot.getChildrenCount() + " summaries.");
//
//                            Set<String> firebasePartnerIds = new HashSet<>(); // To track partners found in Firebase
//
//                            // Check if the snapshot itself exists and has children BEFORE processing
//                            if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
//                                Log.d(TAG, "No chat summaries found in Firebase for user: " + currentUserID + ". Clearing local chats for this owner.");
//                                // Clear chats from Room if no summaries in Firebase
//                                databaseExecutor.execute(() -> { // Run on DB thread
//                                    try {
//                                        // Make sure this method exists in ChatDao and deletes WHERE ownerUserId = :ownerId
//                                        int deletedCount = chatDao.deleteAllChatsForOwner(currentUserID); // Assuming this method exists
//                                        Log.d(TAG, "Successfully cleared " + deletedCount + " chats from Room as Firebase has no summaries for owner " + currentUserID + ".");
//                                    } catch (Exception e) {
//                                        Log.e(TAG, "Error clearing chats from Room for owner " + currentUserID, e);
//                                    }
//                                });
//                                // LiveData observer will update UI automatically after Room is cleared
//                                return; // Exit onDataChange
//                            }
//
//                            // Process each chat partner's summary under the current user's node
//                            for (DataSnapshot chatPartnerSummarySnap : dataSnapshot.getChildren()) {
//                                // The key of the child node is the chat partner's UID
//                                String chatPartnerId = chatPartnerSummarySnap.getKey();
//
//                                // *** ADD THIS CHECK: Skip processing if it's a null, empty or self entry ***
//                                if (TextUtils.isEmpty(chatPartnerId) || chatPartnerId.equals(currentUserID)) {
//                                    Log.d(TAG, "Skipping null, empty, or self-chat partner ID in summary: " + chatPartnerId);
//                                    continue;
//                                }
//                                // ****************************************************************
//
//                                // Check if the snapshot for this partner has the expected data structure before proceeding
//                                // IMPORTANT: Check for presence of required fields based on your ChatSummary structure.
//                                // Ensure conversationId, lastMessageTimestamp, unreadCounts/{currentUserID} exist.
//                                if (!chatPartnerSummarySnap.hasChild("conversationId") ||
//                                        !chatPartnerSummarySnap.hasChild("lastMessageTimestamp") ||
//                                        !chatPartnerSummarySnap.hasChild("unreadCounts") ||
//                                        !chatPartnerSummarySnap.child("unreadCounts").hasChild(currentUserID)) // Check for unread count node for THIS user
//                                {
//                                    Log.w(TAG, "Skipping summary for " + chatPartnerId + ": Missing expected child nodes (convId, timestamp, unreadCounts/" + currentUserID + ").");
//                                    // Optional: Delete this invalid entry from Firebase ChatSummaries?
//                                    // This might indicate an issue with summary creation.
//                                    // chatPartnerSummarySnap.getRef().removeValue(); // Use with caution, could cause sync issues
//                                    continue; // Skip this entry
//                                }
//
//
//                                Log.d(TAG, "Processing summary for chat partner: " + chatPartnerId);
//                                firebasePartnerIds.add(chatPartnerId); // Add to the set of found partners
//
//                                // Get summary data directly from the snapshot
//                                String conversationId = chatPartnerSummarySnap.child("conversationId").getValue(String.class);
//                                // This is the ENCRYPTED or placeholder preview from the SENDER's Firebase summary
//                                String encryptedLastMessageContentPreview = chatPartnerSummarySnap.child("lastMessageContentPreview").getValue(String.class); // Can be null
//                                Long timestampLong = chatPartnerSummarySnap.child("lastMessageTimestamp").getValue(Long.class); // Can be null initially
//                                // Get unread count specifically for the current user from the sub-node
//                                Integer unreadCount = chatPartnerSummarySnap.child("unreadCounts").child(currentUserID).getValue(Integer.class); // Can be null
//                                String lastMessageSenderId = chatPartnerSummarySnap.child("lastMessageSenderId").getValue(String.class); // Can be null
//                                String lastMessageType = chatPartnerSummarySnap.child("lastMessageType").getValue(String.class); // Can be null
//
//
//                                // Basic validation for essential fields needed to create/update a chat entry in Room
//                                if (TextUtils.isEmpty(chatPartnerId) || TextUtils.isEmpty(conversationId) || timestampLong == null || unreadCount == null) {
//                                    // This check is partially redundant with the hasChild checks above, but adds null/empty text check
//                                    Log.w(TAG, "Skipping summary for " + chatPartnerId + ": Essential fields are null/empty/invalid (partnerId, convId, timestamp, unreadCount).");
//                                    continue; // Skip this entry
//                                }
//
//
//                                // --- Fetch User Data (Username and Image) Asynchronously ---
//                                // Fetch the chat partner's user data asynchronously
//                                // Use SingleValueEvent as we only need the current state once per sync cycle for this user's profile.
//                                // This runs on the main thread. The Room operation will be on a background thread.
//                                if (usersRef != null) { // Safety check for usersRef
//                                    usersRef.child(chatPartnerId).addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot userSnap) {
//                                            String username = "Unknown User"; // Default username
//                                            String base64Image = ""; // Default image (empty Base64)
//
//                                            if (userSnap.exists()) {
//                                                // Get username and profile image from the other user's profile data
//                                                username = userSnap.child("username").getValue(String.class);
//                                                base64Image = userSnap.child("profileImage").getValue(String.class);
//                                                // Handle potential nulls/empty strings for display
//                                                if (TextUtils.isEmpty(username)) username = "Unknown User";
//                                                if (base64Image == null) base64Image = ""; // Ensure Base64 string is not null
//
//                                                // Optional: Log fetched user data details
//                                                // Log.d(TAG, "Fetched user data for chat partner: " + chatPartnerId + ", Username: " + username + ", Image exists: " + (!TextUtils.isEmpty(base64Image)));
//
//                                            } else {
//                                                Log.w(TAG, "User data not found in /Users/ for chat partner userId: " + chatPartnerId + " during sync. Keeping defaults.");
//                                                // Keep default placeholders if user data is missing
//                                            }
//
//                                            // Create ChatEntity for Room using the fetched user data and summary data
//                                            ChatEntity chatEntity = new ChatEntity(); // Use empty constructor
//                                            // Set the primary key components and other fields
//                                            chatEntity.setOwnerUserId(currentUserID); // Set owner to the current logged-in user
//                                            chatEntity.setUserId(chatPartnerId); // Set chat partner ID (the other user in this chat)
//                                            chatEntity.setConversationId(conversationId); // Store conversation ID
//
//                                            // Set display information from the fetched user data
//                                            chatEntity.setUsername(username);
//                                            chatEntity.setProfileImage(base64Image);
//
//                                            // Store the *ENCRYPTED* preview content (or placeholder) from the Firebase summary in Room
//                                            chatEntity.setLastMessage(encryptedLastMessageContentPreview != null ? encryptedLastMessageContentPreview : ""); // Can be null from Firebase
//
//                                            // Set timestamp, unread count, type, and sender ID
//                                            chatEntity.setTimestamp(timestampLong != null ? timestampLong : 0L); // Store Firebase timestamp, default to 0 if null
//                                            chatEntity.setUnreadCount(unreadCount != null ? unreadCount : 0); // Store the unread count FOR THIS USER, default to 0
//
//                                            // Store lastMessageType (default to text if null or empty)
//                                            chatEntity.setLastMessageType(TextUtils.isEmpty(lastMessageType) ? "text" : lastMessageType);
//
//                                            // Optional: If you added fields for last message sender ID in ChatEntity
//                                            // chatEntity.setLastMessageSenderId(lastMessageSenderId != null ? lastMessageSenderId : "");
//
//
//                                            // Save/update the ChatEntity in Room DB on a background thread
//                                            if (databaseExecutor != null && chatDao != null) { // Safety check for DB components
//                                                databaseExecutor.execute(() -> { // Run on Room DB thread
//                                                    try {
//                                                        // Insert or replace the chat list entry for the current user and this chat partner
//                                                        chatDao.insertOrUpdateChat(chatEntity); // Use insertOrUpdate (REPLACE strategy)
//                                                        // Log success (can be verbose)
//                                                        // Log.d(TAG, "Successfully saved/updated chat list entry to Room for partner: " + chatPartnerId + " (Owner: " + currentUserID + ")");
//                                                    } catch (Exception e) {
//                                                        Log.e(TAG, "Error saving/updating chat list entry to Room for partner: " + chatPartnerId + " (Owner: " + currentUserID + ")", e);
//                                                        // Handle Room DB errors if necessary (e.g., log, show internal error message)
//                                                    }
//                                                });
//                                            } else {
//                                                Log.w(TAG, "Cannot save chat summary to Room, databaseExecutor or chatDao is null.");
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                                            Log.e(TAG, "Failed to fetch user data for chat partner " + chatPartnerId + " during sync: " + databaseError.getMessage());
//                                            // Decide how to handle - if user data fetch fails, the chat entry might not be updated correctly in Room.
//                                            // The last known state in Room will persist until next successful sync.
//                                            // You might want to create/update the ChatEntity with default/error info here if user data is critical.
//                                        }
//                                    }); // End of usersRef.child(chatPartnerId).addListenerForSingleValueEvent (asynchronous fetch)
//                                } else {
//                                    Log.w(TAG, "usersRef is null, cannot fetch user data for chat partner " + chatPartnerId);
//                                }
//
//                            } // End of processing each chat partner summary
//
//
//                            // --- Clean up Room DB: Delete chats that are no longer in Firebase Summaries ---
//                            // This ensures that if a chat summary is deleted from Firebase (e.g., by deleting the last message),
//                            // it's also removed from the user's chat list in Room.
//                            if (databaseExecutor != null && chatDao != null) { // Safety check for DB components
//                                databaseExecutor.execute(() -> { // Run on Room DB thread
//                                    try {
//                                        // Need an immediate query method in your DAO to get current local chats owned by this user
//                                        // Make sure getAllChatsImmediate(ownerId) exists and is synchronous and retrieves chats by ownerId
//                                        List<ChatEntity> localChats = chatDao.getAllChatsImmediate(currentUserID); // Ensure this exists and is synchronous
//
//                                        if (localChats != null) {
//                                            for (ChatEntity localChat : localChats) {
//                                                // Check if this local chat entry (by partner ID) exists in the set of partners found in the Firebase snapshot
//                                                // Ensure localChat.getUserId() is not null/empty before checking firebasePartnerIds
//                                                if (!TextUtils.isEmpty(localChat.getUserId()) && !firebasePartnerIds.contains(localChat.getUserId())) {
//                                                    Log.d(TAG, "Deleting local chat entry for partner " + localChat.getUserId() + " as it's no longer in Firebase summaries for owner " + currentUserID + ".");
//                                                    // Delete the chat entry owned by currentUserID
//                                                    // Make sure deleteChatByUserId(partnerId, ownerId) exists and uses both IDs
//                                                    int deletedSummaryRows = chatDao.deleteChatByUserId(localChat.getUserId(), currentUserID); // Ensure this DAO method exists and uses ownerId
//
//                                                    if (deletedSummaryRows > 0) {
//                                                        Log.d(TAG, "Successfully deleted chat list entry from Room for partner: " + localChat.getUserId() + " owned by: " + currentUserID);
//                                                    } else {
//                                                        Log.w(TAG, "Attempted to delete chat list entry from Room for partner " + localChat.getUserId() + ", but it wasn't found for owner " + currentUserID);
//                                                    }
//
//
//                                                    // Optional: Also delete the related messages from Room for this conversation
//                                                    String conversationIdForMessageDelete = localChat.getConversationId();
//                                                    if (!TextUtils.isEmpty(conversationIdForMessageDelete)) {
//                                                        try {
//                                                            // Assuming MessageDao has deleteAllMessagesForChat(ownerId, conversationId)
//                                                            // Or deleteMessagesForChat(ownerId, user1Id, user2Id)
//                                                            // Using ConversationId is simpler if it's consistently stored
//                                                            int deletedMessageRows = messageDao.deleteAllMessagesForChat(currentUserID, conversationIdForMessageDelete); // Assuming a DAO method that takes owner and convId
//                                                            Log.d(TAG, "Successfully deleted " + deletedMessageRows + " messages from Room for conversation " + conversationIdForMessageDelete + " for owner " + currentUserID + " during sync cleanup.");
//                                                        } catch (Exception msgDeleteEx) {
//                                                            Log.e(TAG, "Error deleting messages from Room during sync cleanup for conv " + conversationIdForMessageDelete + " for owner " + currentUserID + ".", msgDeleteEx);
//                                                        }
//                                                    } else {
//                                                        Log.w(TAG, "Cannot delete messages for local chat entry " + localChat.getUserId() + ": Conversation ID is empty.");
//                                                    }
//
//                                                    // Optional: Also delete the conversation key from Room if the chat is removed
//                                                    String conversationIdForKeyDelete = localChat.getConversationId();
//                                                    if (!TextUtils.isEmpty(conversationIdForKeyDelete)) {
//                                                        try {
//                                                            // Assuming ConversationKeyDao has deleteKeyById(ownerId, conversationId)
//                                                            int deletedKeyRows = chatDatabase.conversationKeyDao().deleteKeyById(currentUserID, conversationIdForKeyDelete); // Access the new DAO
//                                                            if (deletedKeyRows > 0) {
//                                                                Log.d(TAG, "Deleted conversation key from Room during sync cleanup for conv " + conversationIdForKeyDelete + " for owner " + currentUserID + ".");
//                                                                // Optional: Remove from in-memory KeyManager if it was loaded
//                                                                YourKeyManager.getInstance().removeConversationKey(conversationIdForKeyDelete);
//                                                            } else {
//                                                                Log.w(TAG, "Attempted to delete conversation key but it was not found in Room during sync cleanup for conv " + conversationIdForKeyDelete);
//                                                            }
//                                                        } catch (Exception keyDeleteEx) {
//                                                            Log.e(TAG, "Error deleting conversation key from Room during sync cleanup for conv " + conversationIdForKeyDelete + " for owner " + currentUserID + ".", keyDeleteEx);
//                                                        }
//                                                    } else {
//                                                        Log.w(TAG, "Cannot delete conversation key for local chat entry " + localChat.getUserId() + ": Conversation ID is empty.");
//                                                    }
//
//                                                } // End if (!firebasePartnerIds.contains(localChat.getUserId()))
//                                            } // End for loop through local chats
//                                        } // End if (localChats != null)
//                                        Log.d(TAG, "Finished Room cleanup based on Firebase summaries for owner " + currentUserID + ".");
//                                    } catch (Exception e) {
//                                        Log.e(TAG, "Error during Room cleanup after Firebase sync for owner " + currentUserID + ".", e);
//                                    }
//                                }); // End of Room cleanup executor
//                            } else {
//                                Log.w(TAG, "Cannot perform Room cleanup, databaseExecutor or chatDao is null.");
//                            }
//
//
//                        } // End of onDataChange for chatSummariesRef
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                            Log.e(TAG, "Firebase Chat Summary Listener Cancelled: " + databaseError.getMessage(), databaseError.toException());
//                            if (getContext() != null) {
//                                Toast.makeText(getContext(), "Failed to load chats: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                            // Decide if you need to disable UI or show permanent error here
//                        }
//                    };
//
//                    // Attach the listener
//                    chatSummariesRef.addValueEventListener(chatSummaryListener);
//                    isChatSummaryListenerAttached = true; // Set the flag
//                    Log.d(TAG, "Firebase ChatSummaries listener attached.");
//                } else {
//                    // Listener is already attached or cannot be attached (user not logged in, handled earlier)
//                    Log.d(TAG, "ChatSummaries listener already attached or cannot be attached.");
//                }
//            }
//            // --- End Attach Real-time Listener ---
//
//
//            // --- Remove Firebase Listener and Observers in onDestroyView ---
//            @Override
//            public void onDestroyView() { // Use onDestroyView for Fragment lifecycle
//                super.onDestroyView();
//                Log.d(TAG, "🔴 ChatFragment onDestroyView");
//
//                // Remove Firebase listener only if it was attached
//                if (chatSummariesRef != null && chatSummaryListener != null && isChatSummaryListenerAttached) { // Check the flag
//                    chatSummariesRef.removeEventListener(chatSummaryListener);
//                    chatSummaryListener = null; // Nullify the listener reference
//                    isChatSummaryListenerAttached = false; // Reset the flag
//                    Log.d(TAG, "Firebase ChatSummaries listener removed.");
//                } else {
//                    Log.d(TAG, "Firebase ChatSummaries listener was not attached or already removed.");
//                }
//
//                // Remove LiveData observer
//                if (chatListLiveData != null) {
//                    // Use getViewLifecycleOwner() to ensure the observer is removed when the view is destroyed
//                    chatListLiveData.removeObservers(getViewLifecycleOwner());
//                    Log.d(TAG, "Room LiveData observer removed.");
//                }
//
//                // ExecutorService for Room DB write is managed by ChatDatabase class, no need to shut down here.
//
//                // Clear view references to prevent memory leaks
//                privateChatsView = null;
//                privateChatsList = null;
//                noChatsText = null;
//                searchView = null;
//                chatAdapter = null; // Clear adapter reference
//
//                Log.d(TAG, "✅ ChatFragment onDestroyView finished.");
//            }
//
//            // Optional: Add onDestroy method for final Fragment cleanup
//            @Override
//            public void onDestroy() {
//                super.onDestroy();
//                Log.d(TAG, "🔴 ChatFragment onDestroy");
//                // Clean up any other non-view related resources specific to this fragment if needed.
//                // If you had other executors or resources tied to the fragment's entire lifecycle, clean them here.
//                // The Room DB executor is managed by ChatDatabase singleton, so no need to shut it down here.
//
//                // Shutdown chatOpenExecutor gracefully
//                if (chatOpenExecutor != null && !chatOpenExecutor.isShutdown()) {
//                    Log.d(TAG, "chatOpenExecutor shutting down.");
//                    chatOpenExecutor.shutdownNow(); // Or shutdown()
//                }
//                // Dismiss dialog if it's showing to prevent window leaks
//                if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) {
//                    chatOpenProgressDialog.dismiss();
//                }
//                chatOpenProgressDialog = null; // Nullify dialog reference
//            }
//
//
//            // --- ChatAdapter Interaction Listener Methods (Keep These) ---
//
//            @Override // From ChatAdapter.OnChatInteractionListener
//            public void onChatClick(ChatEntity chat) {
//                // This is called when a chat item is clicked in the RecyclerView list.
//                Log.d(TAG, "Chat clicked: " + chat.getUsername() + " (Other User ID: " + chat.getUserId() + ") Conversation ID: " + chat.getConversationId());
//
//                // Ensure currentUserID is available before proceeding
//                if (currentUserID == null) {
//                    Log.e(TAG, "currentUserID is null in onChatClick. Cannot proceed.");
//                    if (getContext() != null) {
//                        Toast.makeText(getContext(), "Error: User not logged in.", Toast.LENGTH_SHORT).show();
//                    }
//                    return; // Exit if user ID is not available
//                }
//
//                // Get necessary info from the clicked ChatEntity
//                String conversationId = chat.getConversationId();
//                String chatPartnerId = chat.getUserId();
//                String chatPartnerName = chat.getUsername();
//                String chatPartnerImage = chat.getProfileImage();
//
//
//                // Validate conversationId
//                if (TextUtils.isEmpty(conversationId)) {
//                    Log.e(TAG, "Conversation ID is null or empty for clicked chat with partner: " + chat.getUserId());
//                    Toast.makeText(getContext(), "Error opening chat: Invalid conversation.", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                // *** NEW: Check if user's RSA Private Key is available and if conversation key is in KeyManager ***
//                boolean isPrivateKeyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable();
//                boolean hasConversationKeyInMemory = YourKeyManager.getInstance().hasConversationKey(conversationId);
//
//                if (!isPrivateKeyAvailable) {
//                    // User's main account is not unlocked or setup. Cannot decrypt messages.
//                    Log.w(TAG, "User's Private key is NOT available. Cannot open secure chat.");
//                    Toast.makeText(getContext(), "Your account is not unlocked for secure chat. Please set up or unlock your Security Passphrase in Settings.", Toast.LENGTH_LONG).show();
//                    // Do NOT proceed to open the chat activity in a disabled state from here.
//                    // User needs to fix their security setup first.
//                    return;
//                }
//
//                if (hasConversationKeyInMemory) {
//                    // Conversation key is already in the in-memory cache. We are ready to go!
//                    Log.d(TAG, "Conversation key found in KeyManager cache for ID: " + conversationId + ". Navigating directly to chat.");
//                    // Dismiss progress dialog if somehow showing (unlikely here, but safe)
//                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) {
//                        chatOpenProgressDialog.dismiss();
//                    }
//                    navigateToChatActivity(conversationId, chatPartnerId, chatPartnerName, chatPartnerImage);
//
//                } else {
//                    // Private key is available, but the specific conversation key is NOT in KeyManager's cache.
//                    // This means it wasn't loaded during the initial load from Room, or the initial load failed for this key.
//                    // We need to attempt to load *this specific key* from Room (and potentially Firebase as a fallback).
//                    Log.d(TAG, "Conversation key NOT found in KeyManager cache for ID: " + conversationId + ". Attempting to load from Room/DB.");
//
//                    // Show progress dialog while we load the key
//                    if (getContext() == null) {
//                        Log.w(TAG, "Context is null, cannot show progress dialog.");
//                        Toast.makeText(getContext(), "Error starting chat.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    // Ensure dialog instance is valid and owned by the activity
//                    if (chatOpenProgressDialog == null || (getActivity() != null && chatOpenProgressDialog.getOwnerActivity() != getActivity())) {
//                        chatOpenProgressDialog = new ProgressDialog(requireContext());
//                        chatOpenProgressDialog.setCancelable(false);
//                        if (getActivity() != null) chatOpenProgressDialog.setOwnerActivity(getActivity());
//                    }
//                    chatOpenProgressDialog.setMessage("Loading secure chat key...");
//                    if (!chatOpenProgressDialog.isShowing()) { // Only show if not already showing
//                        chatOpenProgressDialog.show();
//                    }
//
//                    // Initiate the asynchronous key loading process for THIS conversation
//                    // Use the dedicated executor for chat opening tasks
//                    loadConversationKeyForChatAsync(conversationId, chatPartnerId, chatPartnerName, chatPartnerImage);
//                }
//                // --- END NEW CHECK ---
//            }
//
//
//
//            private void loadConversationKeyForChatAsync(String conversationId, String chatPartnerId, String chatPartnerName, String chatPartnerImage) {
//
//                // Initial validation: Ensure context, user ID, conv ID, DAOs, and Firebase ref are available
//                if (getContext() == null || TextUtils.isEmpty(currentUserID) || TextUtils.isEmpty(conversationId) ||
//                        conversationKeyDao == null || rootRef == null || chatOpenExecutor == null || chatOpenHandler == null) {
//                    Log.e(TAG, "loadConversationKeyForChatAsync: Prerequisites missing. Cannot load key.");
//                    // Post error to main thread if context is available, otherwise just log
//                    if (getContext() != null && chatOpenHandler != null) {
//                        chatOpenHandler.post(() -> { // Post error to main thread
//                            if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
//                            Toast.makeText(getContext(), "Error loading chat key.", Toast.LENGTH_SHORT).show();
//                        });
//                    }
//                    return;
//                }
//                Log.d(TAG, "loadConversationKeyForChatAsync: Starting key load for conversation: " + conversationId + " for user: " + currentUserID);
//
//
//                // Use the dedicated chat opening executor for the background task
//                chatOpenExecutor.execute(() -> {
//
//                    // Store all successfully loaded/decrypted keys for this conversation across all versions found
//                    // We will check KeyManager state at the end to see if ANY key was successfully loaded
//                    String errorMessage = null; // To hold an error message if loading/decryption fails permanently
//                    boolean triggerNewKeyGeneration = false; // Flag to indicate if decryption failed with *all* versions, requiring new key
//
//
//                    // --- Step 1: Attempt to load ALL key versions for this conversation from Room DB ---
//                    // Room is the primary source after the initial bulk load in MainActivity
//                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Attempting to load ALL keys from Room DB for conv ID: " + conversationId + " for owner " + currentUserID);
//                    List<ConversationKeyEntity> keyEntities = null;
//                    try {
//                        // Use the DAO method to get ALL key versions for this user and conversation
//                        // Make sure this DAO query orders by timestamp ASC
//                        keyEntities = conversationKeyDao.getAllKeysForConversation(currentUserID, conversationId); // Assuming getAllKeysForConversation(ownerId, convId) exists
//                        Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Room query finished. Found " + (keyEntities != null ? keyEntities.size() : 0) + " key versions in Room.");
//
//                        // If keys found in Room, attempt to load them into KeyManager (decrypted keys are already there)
//                        if (keyEntities != null && !keyEntities.isEmpty()) {
//                            Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Keys found in Room. Loading into KeyManager.");
//                            YourKeyManager keyManager = YourKeyManager.getInstance();
//                            for (ConversationKeyEntity keyEntity : keyEntities) {
//                                if (keyEntity == null || TextUtils.isEmpty(keyEntity.getDecryptedKeyBase64()) || keyEntity.getKeyTimestamp() <= 0) {
//                                    Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Skipping corrupt/invalid key entity from Room for conv " + conversationId + ", timestamp " + keyEntity.getKeyTimestamp());
//                                    // Optional: Delete corrupt entry from Room
//                                    if (conversationKeyDao != null && databaseExecutor != null && keyEntity != null) { // Added null checks
//                                        databaseExecutor.execute(() -> {
//                                            try { conversationKeyDao.deleteSpecificKeyVersion(currentUserID, conversationId, keyEntity.getKeyTimestamp()); Log.d(TAG, "Executor - DB: Deleted corrupt key from Room: " + conversationId + " T:" + keyEntity.getKeyTimestamp()); }
//                                            catch (Exception deleteEx) { Log.e(TAG, "Executor - DB: Error deleting corrupt key from Room", deleteEx); }
//                                        });
//                                    }
//                                    continue;
//                                }
//                                // Ensure the key belongs to the current user (should be enforced by DAO query)
//                                if (!keyEntity.getOwnerUserId().equals(currentUserID)) {
//                                    Log.e(TAG, "BUG: Loaded key from Room for owner " + keyEntity.getOwnerUserId() + " but expecting owner " + currentUserID + ". Skipping.");
//                                    continue;
//                                }
//
//                                try {
//                                    byte[] decryptedKeyBytes = android.util.Base64.decode(keyEntity.getDecryptedKeyBase64(), android.util.Base64.DEFAULT);
//                                    SecretKey conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedKeyBytes);
//                                    // *** MODIFIED KEYMANAGER CALL - ADDED TIMESTAMP ***
//                                    keyManager.setConversationKey(conversationId, keyEntity.getKeyTimestamp(), conversationAESKey); // Add timestamp
//                                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key version " + keyEntity.getKeyTimestamp() + " loaded from Room DB into KeyManager for conv ID: " + conversationId);
//                                } catch (IllegalArgumentException e) {
//                                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Error decoding/converting key from Room for conv ID: " + conversationId + ", timestamp " + keyEntity.getKeyTimestamp() + ". Deleting corrupt entry.", e);
//                                    // Delete corrupt entry from Room on the database executor
//                                    if (conversationKeyDao != null && databaseExecutor != null) { // Added null checks
//                                        databaseExecutor.execute(() -> {
//                                            try { conversationKeyDao.deleteSpecificKeyVersion(currentUserID, conversationId, keyEntity.getKeyTimestamp()); Log.d(TAG, "Executor - DB: Deleted corrupt key from Room: " + conversationId + " T:" + keyEntity.getKeyTimestamp()); }
//                                            catch (Exception deleteEx) { Log.e(TAG, "Executor - DB: Error deleting corrupt key from Room", deleteEx); }
//                                        });
//                                    }
//                                } catch (Exception e) {
//                                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Unexpected error processing key from Room for conv ID: " + conversationId + ", timestamp " + keyEntity.getKeyTimestamp(), e);
//                                    // Decide if you delete on other errors.
//                                    if (conversationKeyDao != null && databaseExecutor != null) { // Added null checks
//                                        databaseExecutor.execute(() -> {
//                                            try { conversationKeyDao.deleteSpecificKeyVersion(currentUserID, conversationId, keyEntity.getKeyTimestamp()); Log.d(TAG, "Executor - DB: Deleted key from Room after processing error: " + conversationId + " T:" + keyEntity.getKeyTimestamp()); }
//                                            catch (Exception deleteEx) { Log.e(TAG, "Executor - DB: Error deleting key from Room", deleteEx); }
//                                        });
//                                    }
//                                }
//                            } // End of loop through Room keys
//                            Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Finished loading keys from Room into KeyManager. Total versions cached for conv " + conversationId + ": " + keyManager.getAllConversationKeysForConversation(conversationId).size());
//                        }
//
//
//                    } catch (Exception e) { // Catch any exception during Room DAO query
//                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Overall error fetching keys from Room DB for conv ID: " + conversationId, e);
//                        // Don't set error message yet, we will fall back to Firebase
//                    }
//
//
//                    // --- Step 2: Fallback to Fetching from Firebase if NO keys were successfully loaded into KeyManager from Room ---
//                    // Check if KeyManager still doesn't have any key versions for this conversation *after* trying Room.
//                    if (!YourKeyManager.getInstance().hasConversationKey(conversationId)) {
//                        Log.d(TAG, "loadConversationKeyForChatAsync (Executor): No conversation keys loaded into KeyManager from Room. Falling back to Firebase.");
//                        // This happens if the initial load from Room failed, was incomplete, the Room data was corrupt,
//                        // OR the user cleared local data, OR the keys were never saved to Room.
//                        // We need the user's Private RSA key to decrypt the keys from Firebase.
//                        PrivateKey currentUserPrivateKey = YourKeyManager.getInstance().getUserPrivateKey();
//
//                        if (currentUserPrivateKey == null) {
//                            // This state should have been caught earlier in onChatClick (preventing the call),
//                            // but check defensively here. Private key MUST be available to attempt Firebase decryption.
//                            errorMessage = "Your private key is not available to decrypt chat keys.";
//                            Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Private key is unexpectedly null when trying to fetch from Firebase.");
//
//                        } else {
//                            Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Fetching key VERSIONS from Firebase for conv ID: " + conversationId + " for user " + currentUserID);
//                            // Use Tasks.await() to make the Firebase fetch synchronous on this background thread.
//                            try {
//                                // Fetch the 'key_versions' node for this conversation
//                                DataSnapshot keyVersionsSnapshot = Tasks.await(rootRef.child("ConversationKeys").child(conversationId).child("key_versions").get()); // Fetch the 'key_versions' node
//                                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Firebase get() for key_versions completed. Snapshot exists: " + keyVersionsSnapshot.exists() + ", has children: " + keyVersionsSnapshot.hasChildren());
//
//                                if (keyVersionsSnapshot.exists() && keyVersionsSnapshot.hasChildren()) {
//                                    // Found key versions in Firebase for this conversation. Iterate through ALL versions.
//                                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key versions found in Firebase. Iterating and attempting decryption for user " + currentUserID);
//                                    YourKeyManager keyManager = YourKeyManager.getInstance(); // Get KeyManager instance
//
//                                    for (DataSnapshot keyVersionSnap : keyVersionsSnapshot.getChildren()) {
//                                        String timestampString = keyVersionSnap.getKey(); // The key is the timestamp string
//                                        long keyTimestamp;
//                                        try {
//                                            // Parse the timestamp string to a long
//                                            keyTimestamp = Long.parseLong(timestampString);
//                                            if (keyTimestamp <= 0) throw new NumberFormatException("Invalid timestamp value");
//                                        } catch (NumberFormatException e) {
//                                            Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Skipping Firebase key version with invalid timestamp key: " + timestampString + " for conv " + conversationId, e);
//                                            continue; // Skip this key version
//                                        }
//
//                                        // Check if this key version snapshot contains the encrypted key data for the *current user*
//                                        if (keyVersionSnap.hasChild(currentUserID)) {
//                                            String encryptedAesKeyForCurrentUserBase64 = keyVersionSnap.child(currentUserID).getValue(String.class);
//
//                                            if (!TextUtils.isEmpty(encryptedAesKeyForCurrentUserBase64)) {
//                                                // Decode Base64 string to byte array
//                                                byte[] encryptedAesKeyBytes = android.util.Base64.decode(encryptedAesKeyForCurrentUserBase64, android.util.Base64.DEFAULT); // Use android.util.Base64
//
//                                                try {
//                                                    // Attempt decryption using the *current* private key
//                                                    byte[] decryptedAesKeyBytes = CryptoUtils.decryptWithRSA(encryptedAesKeyBytes, currentUserPrivateKey); // Decrypt with RSA Private Key!
//                                                    SecretKey conversationAESKey = CryptoUtils.bytesToSecretKey(decryptedAesKeyBytes); // Convert bytes to AES SecretKey
//
//                                                    // If decryption succeeded:
//                                                    // *** MODIFIED KEYMANAGER CALL - ADDED TIMESTAMP ***
//                                                    keyManager.setConversationKey(conversationId, keyTimestamp, conversationAESKey); // Add timestamp
//                                                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key version " + keyTimestamp + " decrypted from Firebase and loaded into KeyManager for conv ID: " + conversationId);
//
//
//
//                                                    try {
//                                                        keyTimestamp = Long.parseLong(timestampString); // timestampString is keyVersionSnap.getKey()
//                                                        if (keyTimestamp <= 0) throw new NumberFormatException("Invalid timestamp value");
//                                                    } catch (NumberFormatException e) {
//                                                        Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Skipping Firebase key version with invalid timestamp key: " + timestampString + "...", e);
//                                                        continue; // Skip this key version
//                                                    }
//
//
//                                                    // *** Save the successfully fetched/decrypted key to Room DB for persistence ***
//                                                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Saving fetched/decrypted key version " + keyTimestamp + " to Room DB.");
//                                                    // Use android.util.Base64 for converting the decrypted bytes to a Base64 String for Room storage
//                                                    String decryptedKeyBase64ForRoom = android.util.Base64.encodeToString(decryptedAesKeyBytes, android.util.Base64.DEFAULT); // *** Use android.util.Base64.DEFAULT ***
//                                                    // *** MODIFIED CONSTRUCTOR CALL - ADDED OWNER AND TIMESTAMP ***
//                                                    ConversationKeyEntity keyEntityToSave = new ConversationKeyEntity(currentUserID, conversationId, keyTimestamp, decryptedKeyBase64ForRoom); // Add owner and timestamp
//                                                    // Use the databaseWriteExecutor for Room save (can't save directly on chatOpenExecutor if DB executor is single thread)
//                                                    if (databaseExecutor != null && conversationKeyDao != null) { // Added null checks
//                                                        long finalKeyTimestamp = keyTimestamp;
//                                                        databaseExecutor.execute(() -> {
//                                                            try {
//                                                                conversationKeyDao.insertOrUpdateKey(keyEntityToSave); // Insert or update based on primary key (owner, conv, timestamp)
//                                                                Log.d(TAG, "Executor - DB: Key version " + finalKeyTimestamp + " saved to Room after fetching/decrypting from Firebase for conv ID: " + conversationId);
//                                                            } catch (Exception saveEx) {
//                                                                Log.e(TAG, "Executor - DB: Error saving fetched key version " + finalKeyTimestamp + " to Room DB", saveEx);
//                                                                // Log error, don't show toast, chat will still work for this session from KeyManager
//                                                            }
//                                                        });
//                                                    } else {
//                                                        Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Cannot save fetched key to Room, databaseExecutor or DAO is null.");
//                                                    }
//
//
//                                                } catch (NoSuchAlgorithmException | NoSuchPaddingException |
//                                                         InvalidKeyException |
//                                                         IllegalBlockSizeException | BadPaddingException e) {
//                                                    // *** Crypto decryption failed for THIS key version! (Likely due to old key) ***
//                                                    // This is expected if the key version was encrypted with a private key version
//                                                    // that the current user no longer possesses (e.g., after passphrase reset).
//                                                    Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Crypto error during decryption of version " + keyTimestamp + " after fetching from Firebase for user " + currentUserID + " conv " + conversationId + ". This key version might be unreadable with current key.", e);
//                                                    // DO NOT SET errorMessage here. Continue to the next key version.
//
//                                                } catch (IllegalArgumentException e) { // Base64 decoding error from Firebase data
//                                                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Base64 decoding error for version " + keyTimestamp + " after fetching from Firebase for conv ID: " + conversationId, e);
//                                                    // Still a data format error for this specific version.
//                                                    // DO NOT SET errorMessage here. Continue to the next key version.
//                                                } catch (Exception e) { // Catch any other unexpected exceptions from crypto ops
//                                                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Unexpected error during decryption of version " + keyTimestamp + " after fetching from Firebase for user " + currentUserID + " conv " + conversationId + ".", e);
//                                                    // Still an error for this specific version.
//                                                    // DO NOT SET errorMessage here. Continue to the next key version.
//                                                }
//
//
//                                            } else {
//                                                // Encrypted key data is empty or null for this user in this key version (data inconsistency)
//                                                Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Encrypted key data is empty/null for user " + currentUserID + " in Firebase key version " + keyTimestamp + " for conv " + conversationId);
//                                                // This particular key version cannot be used by this user. Continue to next version.
//                                            }
//
//                                        } else {
//                                            // Key entry for the current user is NOT found in this key version snapshot (data inconsistency)
//                                            Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Key entry for user " + currentUserID + " is MISSING in Firebase key version " + keyTimestamp + " for conv " + conversationId + ". Skipping this version.");
//                                            // Continue to the next key version.
//                                        }
//                                    } // End loop through key versions in Firebase
//
//                                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Finished processing all Firebase key versions found. Total versions cached for conv " + conversationId + ": " + keyManager.getAllConversationKeysForConversation(conversationId).size());
//
//                                    // After trying all key versions from Firebase, check if ANY key was successfully loaded into KeyManager for this conversation.
//                                    if (!keyManager.hasConversationKey(conversationId)) {
//                                        // Decryption failed for ALL available key versions found in Firebase for this conversation.
//                                        // This is the scenario where the conversation key was generated with an old key pair that the user reset.
//                                        // Or it could be genuinely corrupt/missing data for all versions.
//                                        // Trigger NEW key generation for this conversation.
//                                        Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Decryption failed for ALL available key versions from Firebase for conv " + conversationId + ". Triggering NEW key generation.");
//                                        triggerNewKeyGeneration = true; // Set the flag
//                                        // DO NOT SET errorMessage here.
//                                    }
//
//
//                                } else {
//                                    // 'key_versions' node not found or has no children in Firebase for this conversation ID.
//                                    // This means no keys were ever generated for this conversation, OR they were all deleted.
//                                    // Treat as a brand new secure chat SETUP for this user's perspective FOR THIS CONV.
//                                    Log.w(TAG, "loadConversationKeyForChatAsync (Executor): 'key_versions' node not found or empty in Firebase for conv " + conversationId + ". Triggering NEW key generation.");
//                                    triggerNewKeyGeneration = true; // Set the flag
//                                    // DO NOT SET errorMessage here.
//                                }
//
//
//                            } catch (java.util.concurrent.ExecutionException | InterruptedException e) { // Handle Tasks.await() errors
//                                Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Firebase fetch failed for key_versions", e);
//                                errorMessage = "Failed to fetch key data from server. Network error?";
//                                // Do NOT trigger new key generation on network error; it might be temporary.
//                            } catch (Exception e) { // Catch any other unexpected exceptions from Firebase fetch/processing
//                                Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Unexpected error during Firebase key_versions fetch/processing", e);
//                                errorMessage = "An error occurred fetching key data.";
//                                // Do NOT trigger new key generation on unexpected fetch error.
//                            }
//
//                        } // End else (currentUserPrivateKey != null)
//                    } // End else (!YourKeyManager.getInstance().hasConversationKey(conversationId))
//
//
//                    // --- Post result back to Main Thread ---
//                    final String finalErrorMessage = errorMessage; // Error message from non-key-gen paths
//                    final boolean finalTriggerNewKeyGeneration = triggerNewKeyGeneration; // Pass the flag to Main Thread
//                    final String finalConversationId = conversationId;
//                    final String finalChatPartnerId = chatPartnerId;
//                    final String finalChatPartnerName = chatPartnerName;
//                    final String finalRecipientImageBase64 = chatPartnerImage; // <-- Correct variable name // Use recipientImageBase64 variable name for clarity
//                    final String finalCurrentUserId = currentUserID;
//
//
//                    chatOpenHandler.post(() -> { // Post to the main thread
//                        // Dismiss the progress dialog here, BEFORE potentially starting key generation or navigating
//                        if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
//
//                        // Check if decryption failure required new key generation
//                        if (finalTriggerNewKeyGeneration) {
//                            Log.d(TAG, "loadConversationKeyForChatAsync (Main Thread): Decryption failed for all versions OR key node missing. Triggering NEW key generation for conv ID: " + finalConversationId);
//                            // Call generateAndSaveConversationKeysAsync from the main thread, passing necessary info
//                            // This method will show its own progress dialog and handle navigation upon completion.
//                            generateAndSaveConversationKeysAsync(finalConversationId, finalCurrentUserId, finalChatPartnerId, finalChatPartnerName, finalRecipientImageBase64); // <-- यह सही है
//                            // DO NOT NAVIGATE OR SHOW ERROR TOAST HERE. generateAndSaveConversationKeysAsync handles the UI flow.
//
//                        } else if (YourKeyManager.getInstance().hasConversationKey(finalConversationId)) {
//                            // Key is now successfully loaded into KeyManager (at least one version loaded from Room or Firebase)
//                            // And new key generation was NOT triggered.
//                            Log.d(TAG, "loadConversationKeyForChatAsync (Main Thread): Key(s) loaded successfully into KeyManager. Navigating to chat.");
//                            generateAndSaveConversationKeysAsync(finalConversationId, finalCurrentUserId, finalChatPartnerId, finalChatPartnerName, finalRecipientImageBase64); // <-- Correct variable name
//                        } else {
//                            // Key loading failed (either from Room or Firebase fallback), AND new key generation was NOT triggered
//                            // This means a non-recoverable error occurred during fetch or processing, or private key was null.
//                            Log.e(TAG, "loadConversationKeyForChatAsync (Main Thread): Failed to load any conversation key versions for conv ID: " + finalConversationId + ". Error: " + finalErrorMessage);
//                            Toast.makeText(getContext(), finalErrorMessage != null ? finalErrorMessage : "Failed to open secure chat.", Toast.LENGTH_LONG).show();
//                            // Do NOT navigate to chat activity
//                        }
//                    });
//                }); // End of chatOpenExecutor.execute()
//            }
//
//
//
//
//
//
//
//    private void generateAndSaveConversationKeysAsync(String conversationId, String currentUserId, String recipientUserId, String recipientName, String recipientImageBase64) {
//        // Ensure context and essential components are available before starting
//        if (getContext() == null || TextUtils.isEmpty(currentUserID) || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(recipientUserId) ||
//                chatOpenExecutor == null || chatOpenHandler == null || rootRef == null || usersRef == null || conversationKeyDao == null || databaseExecutor == null) {
//            Log.e(TAG, "generateAndSaveConversationKeysAsync: Prerequisites missing. Cannot generate keys.");
//            // Post error to main thread if context is available
//            if (getContext() != null && chatOpenHandler != null) {
//                chatOpenHandler.post(() -> {
//                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
//                    Toast.makeText(getContext(), "Error setting up secure chat.", Toast.LENGTH_SHORT).show();
//                });
//            }
//            return;
//        }
//
//        // Ensure progress dialog is showing (redundant check, but safe)
//        if (chatOpenProgressDialog == null || (getActivity() != null && chatOpenProgressDialog.getOwnerActivity() != getActivity())) {
//            chatOpenProgressDialog = new ProgressDialog(requireContext());
//            chatOpenProgressDialog.setCancelable(false);
//            if (getActivity() != null) chatOpenProgressDialog.setOwnerActivity(getActivity());
//        }
//        if (!chatOpenProgressDialog.isShowing()) {
//            chatOpenProgressDialog.setMessage("Generating and saving keys...");
//            chatOpenProgressDialog.show();
//        }
//        Log.d(TAG, "Generating and saving NEW conversation key version for " + conversationId + " (Users: " + currentUserId + ", " + recipientUserId + ")");
//
//
//        // Fetch recipient's public key first from Firebase (needed for RSA encryption)
//        DatabaseReference recipientUserRef = usersRef.child(recipientUserId); // Use UsersRef member variable
//
//        recipientUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @SuppressLint("RestrictedApi") // Keep if needed by Firebase library internals
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // This callback runs on the main thread due to addListenerForSingleValueEvent
//                // Perform heavy crypto and Firebase save on background thread (chatOpenExecutor)
//                chatOpenExecutor.execute(() -> {
//                    SecretKey conversationAESKey = null; // The decrypted AES key
//                    byte[] conversationAESKeyBytes = null; // The raw bytes of the AES key
//
//                    PublicKey recipientPublicKey = null; // Recipient's RSA Public Key
//                    PublicKey currentUserPublicKey = null; // Current user's RSA Public Key
//                    byte[] encryptedAesKeyForRecipient = null; // AES key encrypted with recipient's public key
//                    byte[] encryptedAesKeyForCurrentUser = null; // AES key encrypted with current user's public key
//
//                    String errorMessage = null; // To hold error message if key generation/encryption fails
//
//                    try {
//                        // 1. Generate a new Conversation AES Key (Symmetric Key)
//                        conversationAESKey = CryptoUtils.generateAESKey();
//                        conversationAESKeyBytes = CryptoUtils.secretKeyToBytes(conversationAESKey); // Get the raw bytes
//                        Log.d(TAG, "Generated new AES conversation key.");
//
//                        // 2. Get Recipient's Public Key from Firebase snapshot
//                        if (snapshot.exists() && snapshot.hasChild("publicKey")) {
//                            String recipientPublicKeyBase64 = snapshot.child("publicKey").getValue(String.class);
//                            if (TextUtils.isEmpty(recipientPublicKeyBase64)) {
//                                Log.e(TAG, "Recipient public key Base64 is empty for " + recipientUserId + " in Firebase.");
//                                throw new IllegalArgumentException("Empty recipient public key Base64 from Firebase");
//                            }
//                            // Use android.util.Base64 for decoding the Base64 string from Firebase
//                            byte[] recipientPublicKeyBytes = android.util.Base64.decode(recipientPublicKeyBase64, android.util.Base64.DEFAULT);
//                            recipientPublicKey = CryptoUtils.bytesToPublicKey(recipientPublicKeyBytes);
//                            Log.d(TAG, "Recipient public key obtained for " + recipientUserId);
//
//                            // 3. Get Current User's Public Key from KeyManager
//                            // It *must* be available because onChatClick checked isPrivateKeyAvailable before calling this.
//                            currentUserPublicKey = YourKeyManager.getInstance().getUserPublicKey();
//                            if (currentUserPublicKey == null) {
//                                // This indicates a severe internal state error if isPrivateKeyAvailable was true but PublicKey is null
//                                Log.e(TAG, "Current user's public key is null during key generation! KeyManager state error. Cannot encrypt key for self.");
//                                throw new IllegalStateException("Current user public key unavailable from KeyManager");
//                            }
//                            Log.d(TAG, "Current user public key obtained from KeyManager.");
//
//
//                            // 4. Encrypt the Conversation AES Key for the RECIPIENT using THEIR RSA Public Key
//                            encryptedAesKeyForRecipient = CryptoUtils.encryptWithRSA(conversationAESKeyBytes, recipientPublicKey);
//                            Log.d(TAG, "AES key encrypted for recipient.");
//
//                            // 5. Encrypt the Conversation AES Key for the CURRENT USER using THEIR RSA Public Key
//                            encryptedAesKeyForCurrentUser = CryptoUtils.encryptWithRSA(conversationAESKeyBytes, currentUserPublicKey);
//                            Log.d(TAG, "AES key encrypted for current user.");
//
//
//                        } else {
//                            // Recipient does NOT have crypto keys or user data missing in Firebase.
//                            Log.w(TAG, "Recipient (" + recipientUserId + ") does not have public key in Firebase or user data missing. Cannot start secure chat.");
//                            errorMessage = "Recipient has not completed security setup. Secure chat unavailable.";
//                            return; // Stop this chatOpenExecutor task early
//                        }
//                    } catch (
//                            IllegalArgumentException e) { // Base64 decoding error or invalid key format error
//                        Log.e(TAG, "Key data processing error during key generation: " + e.getMessage(), e);
//                        errorMessage = "Failed to start secure chat: Invalid key data encountered.";
//                        return; // Stop task
//                    } catch (NoSuchAlgorithmException e) { // AES key generation failed
//                        Log.e(TAG, "AES key generation failed: " + e.getMessage(), e);
//                        errorMessage = "Failed to start secure chat: Key generation error.";
//                        return; // Stop task
//                    } catch (NoSuchPaddingException | InvalidKeyException |
//                             IllegalBlockSizeException | BadPaddingException |
//                             IllegalStateException e) { // RSA crypto/state errors
//                        Log.e(TAG, "Cryptographic or state error during key encryption: " + e.getMessage(), e); // Log specific error
//                        errorMessage = "Failed to start secure chat: Encryption error during key exchange.";
//                        return; // Stop task
//                    } catch (Exception e) { // Catch any other unexpected errors
//                        Log.e(TAG, "Unexpected error during key generation/encryption: " + e.getMessage(), e);
//                        errorMessage = "Failed to start secure chat: An unexpected error occurred.";
//                        return; // Stop task
//                    }
//
//                    // If crypto operations succeeded, proceed to save wrapped keys to Firebase
//                    Log.d(TAG, "Encryption successful. Preparing to save NEW key version to Firebase.");
//
//                    String encryptedAesKeyForRecipientBase64 = CryptoUtils.bytesToBase64(encryptedAesKeyForRecipient);
//                    String encryptedAesKeyForCurrentUserBase64 = CryptoUtils.bytesToBase64(encryptedAesKeyForCurrentUser);
//
//                    // 6. Capture the timestamp for this key version
//                    // Use a local timestamp. Firebase ServerValue.TIMESTAMP is for the *value*, not the key name.
//                    final long newKeyTimestamp = System.currentTimeMillis(); // Capture local time for the key name
//
//                    // 7. Prepare data to save to Realtime Database under ConversationKeys/{convId}/key_versions/{newKeyTimestamp}
//                    Map<String, Object> keyVersionData = new HashMap<>();
//                    keyVersionData.put(currentUserId, encryptedAesKeyForCurrentUserBase64); // Key encrypted for the initiator
//                    keyVersionData.put(recipientUserId, encryptedAesKeyForRecipientBase64); // Key encrypted for the recipient
//                    keyVersionData.put("generatedBy", currentUserId); // Optional metadata
//                    keyVersionData.put("timestamp", ServerValue.TIMESTAMP); // Keep ServerValue.TIMESTAMP as a value for ordering/metadata
//
//
//                    // Create the Firebase reference using the conversationId and the captured timestamp as the key name
//                    DatabaseReference newKeyVersionRef = rootRef.child("ConversationKeys").child(conversationId).child("key_versions").child(String.valueOf(newKeyTimestamp)); // <<< Set timestamp as key name
//
//
//                    // Make final copies of variables needed in the listener lambda
//                    final String finalCurrentUserId = currentUserId;
//                    final String finalConversationId = conversationId; // Use the method parameter directly
//                    final byte[] finalConversationAESKeyBytes = conversationAESKeyBytes;
//                    final SecretKey finalConversationAESKey = conversationAESKey;
//                    final String finalRecipientUserId = recipientUserId;
//                    final String finalRecipientName = recipientName;
//                    final String finalRecipientImageBase64 = recipientImageBase64;
//                    final String finalErrorMessageAfterCrypto = errorMessage; // Capture error message from crypto steps
//
//
//                    // 8. Save the encrypted key version to Realtime Database (Async operation)
//                    newKeyVersionRef.setValue(keyVersionData)
//                            .addOnCompleteListener(task -> {
//                                // This callback usually runs on the main thread after Firebase finishes the async operation
//                                chatOpenHandler.post(() -> { // Ensure all UI updates and navigation are explicitly on main thread
//                                    // Dismiss progress dialog is handled at the end of the post block
//
//                                    if (task.isSuccessful()) {
//                                        Log.d(TAG, "NEW Conversation key version saved successfully to Firebase for " + finalConversationId + " at path: " + newKeyVersionRef.getPath());
//
//                                        // The timestamp used as the Firebase key name is newKeyTimestamp (captured above).
//                                        // Use that captured timestamp.
//
//                                        // --- Success: Keys Saved to Firebase ---
//                                        // Store the decrypted conversationAESKey version in memory for this chat session
//                                        // The key (finalConversationAESKey) is available from the crypto ops above.
//                                        // This key version will be used immediately by ChatPageActivity.
//                                        YourKeyManager.getInstance().setConversationKey(finalConversationId, newKeyTimestamp, finalConversationAESKey); // *** Use the captured newKeyTimestamp ***
//                                        Log.d(TAG, "Decrypted AES key version loaded into KeyManager for conversation " + finalConversationId + " timestamp " + newKeyTimestamp + " after generation. Total versions: " + YourKeyManager.getInstance().getAllConversationKeysForConversation(finalConversationId).size());
//
//
//                                        // *** Save the decrypted conversation key locally to Room DB for persistence ***
//                                        // Run this Room operation on the databaseExecutor (background thread).
//                                        if (databaseExecutor != null && conversationKeyDao != null) { // Use the shared database executor
//                                            databaseExecutor.execute(() -> {
//                                                try {
//                                                    // Convert decrypted key bytes (finalConversationAESKeyBytes) to Base64 string for Room storage.
//                                                    String decryptedKeyBase64 = android.util.Base64.encodeToString(finalConversationAESKeyBytes, android.util.Base64.DEFAULT);
//                                                    // Create ConversationKeyEntity. Owner is the current user. Include the resolved timestamp.
//                                                    // *** MODIFIED CONSTRUCTOR CALL - ADDED OWNER AND TIMESTAMP ***
//                                                    ConversationKeyEntity keyEntity = new ConversationKeyEntity(finalCurrentUserId, finalConversationId, newKeyTimestamp, decryptedKeyBase64); // Add owner and captured timestamp
//                                                    conversationKeyDao.insertOrUpdateKey(keyEntity); // Insert or update based on primary key (owner, conv, timestamp)
//                                                    Log.d(TAG, "NEW Decrypted conversation key version saved to Room DB for owner " + finalCurrentUserId + ", conversation " + finalConversationId + ", timestamp " + newKeyTimestamp + " after Firebase save.");
//                                                } catch (Exception e) {
//                                                    Log.e(TAG, "Error saving NEW decrypted conversation key version to Room DB after Firebase save for user " + finalCurrentUserId + " conv " + finalConversationId + " timestamp " + newKeyTimestamp, e);
//                                                }
//                                            });
//                                        } else {
//                                            Log.w(TAG, "generateAndSaveConversationKeysAsync (Main Thread): Cannot save generated key to Room, databaseExecutor or DAO is null.");
//                                        }
//
//
//                                        // NOW, navigate to the actual Chat Activity
//                                        navigateToChatActivity(finalConversationId, finalRecipientUserId, finalRecipientName, finalRecipientImageBase64); // <-- Correct variable name
//
//                                    } else {
//                                        // Firebase save failed
//                                        Log.e(TAG, "Failed to save NEW conversation key version to Firebase for " + finalConversationId, task.getException());
//                                        String displayMessage = "Failed to start secure chat: Could not save keys.";
//                                        if (task.getException() != null) {
//                                            displayMessage += " " + task.getException().getMessage();
//                                        }
//                                        Toast.makeText(getContext(), displayMessage, Toast.LENGTH_LONG).show();
//                                    }
//
//                                    // Dismiss the progress dialog at the end of the main thread post block
//                                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing())
//                                        chatOpenProgressDialog.dismiss();
//                                });
//                            }); // End of addOnCompleteListener for Firebase save
//
//                    // If an error occurred during crypto steps (before Firebase save was attempted)
//                    if (finalErrorMessageAfterCrypto != null) {
//                        chatOpenHandler.post(() -> { // Ensure UI update is on main thread
//                            Log.e(TAG, "generateAndSaveConversationKeysAsync (Executor): Crypto error occurred before Firebase save. Posting error toast.");
//                            // Dismiss progress dialog is handled below
//                            Toast.makeText(getContext(), finalErrorMessageAfterCrypto, Toast.LENGTH_LONG).show();
//
//                            // Dismiss the progress dialog even on error
//                            if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing())
//                                chatOpenProgressDialog.dismiss();
//                        });
//                    }
//
//
//                }); // End of chatOpenExecutor.execute() for crypto/save Firebase
//            }
//
//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // This callback runs on the main thread
//                Log.e(TAG, "Failed to fetch recipient public key for NEW key generation (Firebase error)", error.toException());
//                chatOpenHandler.post(() -> { // Ensure dismissal and UI updates are on main thread
//                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
//                    Toast.makeText(getContext(), "Failed to start secure chat: Could not fetch recipient data.", Toast.LENGTH_SHORT).show();
//                });
//            }
//        }); // End of Recipient User Data Listener (addListenerForSingleValueEvent)
//    }
//
//            // *** NEW Helper method to Navigate to Chat Activity ***
//            // This method is called upon successful key retrieval/generation for a specific chat.
//            // It also handles the unread count update.
//            private void navigateToChatActivity(String conversationId, String recipientUserId, String recipientName, String recipientImageBase64) {
//                // Ensure context is still valid before starting activity
//                if (getContext() == null) {
//                    Log.e(TAG, "Cannot navigate to chat, context is null.");
//                    return;
//                }
//                Log.d(TAG, "Navigating to ChatPageActivity for conversationId: " + conversationId + ", Recipient ID: " + recipientUserId);
//                Intent intent = new Intent(getContext(), ChatPageActivity.class);
//                intent.putExtra("conversationId", conversationId); // Pass the consistent conversation ID
//                // Keeping old keys for compatibility with your ChatPageActivity if it still uses them
//                intent.putExtra("visit_users_ids", recipientUserId);
//                intent.putExtra("visit_users_name", recipientName);
//                intent.putExtra("visit_users_image", recipientImageBase64);
//                // Add flags to ensure clean task stack if needed (e.g., if you don't want to go back to contacts easily)
//                // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Example
//
//                startActivity(intent); // Launch the ChatPageActivity
//
//
//                // --- Update unread count to 0 in Room for this chat ---
//                // This should be done immediately when the user clicks to open the chat for immediate UI feedback.
//                // Marking individual messages as seen in Firebase will happen in ChatPageActivity.
//                // Updating the unread count in the Firebase ChatSummary will also happen in ChatPageActivity
//                // when the user views the chat.
//                if (!TextUtils.isEmpty(currentUserID) && !TextUtils.isEmpty(recipientUserId) && chatDao != null && databaseExecutor != null) { // Basic validation
//                    // We don't need to check if count > 0 here, the DAO update method will handle it.
//                    // Just queue the update on the database executor.
//                    Log.d(TAG, "Marking unread count to 0 in Room for chat with " + recipientUserId + " for owner " + currentUserID);
//                    databaseExecutor.execute(() -> { // Run on DB thread
//                        try {
//                            // Call the DAO method with the otherUserId, the new count (0), and the currentUserID (owner)
//                            // Make sure updateUnreadCount takes partnerId, newCount, and ownerId
//                            int updatedRows = chatDao.updateUnreadCount(recipientUserId, 0, currentUserID); // Ensure this DAO method exists
//                            if (updatedRows > 0) {
//                                Log.d(TAG, "Successfully updated unread count to 0 in Room for chat with " + recipientUserId);
//                            } else {
//                                // This might happen if the chat entry was somehow deleted from Room just before click (unlikely)
//                                Log.w(TAG, "Attempted to update unread count to 0, but no chat entity found for partner " + recipientUserId + " owned by " + currentUserID);
//                            }
//                        } catch (Exception e) {
//                            Log.e(TAG, "Error updating unread count in Room for user " + recipientUserId, e);
//                        }
//                    });
//                } else {
//                    Log.w(TAG, "Skipping unread count update in Room during navigation: Missing IDs, DAO, or Executor.");
//                }
//            }
//
//
//
//            @Override // From ChatAdapter.OnChatI
//            // nteractionListener
//            public void onChatLongClick(ChatEntity chat) {
//                // Handles long click on a chat item (e.g., to delete)
//                Log.d(TAG, "Chat long clicked: " + chat.getUsername() + " (Other User ID: " + chat.getUserId() + ")");
//                // Ensure chat entity is not null and has a valid partner ID and conversation ID
//                if (chat != null && !TextUtils.isEmpty(chat.getUserId()) && !TextUtils.isEmpty(chat.getConversationId())) { // Added conv ID check
//                    showDeleteChatDialog(chat); // Call method to show the confirmation dialog
//                } else {
//                    Log.w(TAG, "Cannot show delete dialog, ChatEntity is null or has empty userId/conversationId.");
//                    Toast.makeText(getContext(), "Error deleting chat.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            // --- Helper Methods ---
//
//            // Method to check network availability (Kept from your code)
//            private boolean isNetworkAvailable() {
//                if (getContext() == null) return false; // Return false if context is null
//                ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//                if (connectivityManager != null) {
//                    try { // Add try-catch for security exceptions on some Android versions
//                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//                        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//                    } catch (SecurityException e) {
//                        Log.e(TAG, "SecurityException checking network state, ensure ACCESS_NETWORK_STATE permission is granted.", e);
//                        // In production, you might inform the user or assume offline
//                        return false; // Cannot confirm network state
//                    } catch (Exception e) {
//                        Log.e(TAG, "Error checking network state", e);
//                        return false;
//                    }
//                }
//                return false;
//            }
//
//            // Method to show delete chat confirmation dialog (Keep this)
//            private void showDeleteChatDialog(ChatEntity chat) {
//                if (getContext() == null) return; // Ensure context is available
//                // Ensure chat entity is not null and has partner ID for the message
//                if (chat == null || TextUtils.isEmpty(chat.getUserId()) || TextUtils.isEmpty(chat.getConversationId())) { // Added conv ID check
//                    Log.w(TAG, "Cannot show delete chat dialog, ChatEntity is null or has empty userId/conversationId.");
//                    Toast.makeText(getContext(), "Error deleting chat.", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                new AlertDialog.Builder(getContext())
//                        .setTitle("Delete Chat")
//                        // Clarify that this only deletes from *this* user's list in Room and Firebase Summary
//                        .setMessage("Are you sure you want to delete this chat with " + chat.getUsername() + "? This will remove it from your chat list and clear your local message history for this conversation.")
//                        // Note: This does NOT delete messages for the other user unless you implement "Delete for Everyone" logic here too.
//                        .setPositiveButton("Delete", (dialog, which) -> deleteChat(chat.getUserId(), chat.getConversationId())) // <<< MODIFIED: Pass conversationId too
//                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                        .show();
//            }
//
//            // Method to delete chat entry from Room DB for the current user's list
//            // and also delete the corresponding summary entry from Firebase.
//            // Also delete messages and conversation key from Room.
//            // Pass conversationId to this method.
//            private void deleteChat(String userIdToDelete, String conversationIdToDelete) { // userIdToDelete is the chat partner's ID
//
//                if (currentUserID == null) {
//                    Log.e(TAG, "Cannot delete chat, currentUserID is null.");
//                    if (getContext() != null) {
//                        Toast.makeText(getContext(), "Error: Could not delete chat. User not logged in?", Toast.LENGTH_SHORT).show();
//                    }
//                    return;
//                }
//                // Ensure the partner ID and conversation ID are valid, and Room DB is initialized
//                if (TextUtils.isEmpty(userIdToDelete) || TextUtils.isEmpty(conversationIdToDelete) || rootRef == null || chatDatabase == null || chatDao == null || messageDao == null || conversationKeyDao == null || databaseExecutor == null) { // Check all required components
//                    Log.e(TAG, "Cannot delete chat, essential components are null or empty.");
//                    Toast.makeText(getContext(), "Error deleting chat.", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                Log.d(TAG, "Attempting to delete chat entry from Room with partner: " + userIdToDelete + " for owner: " + currentUserID + ", Conversation ID: " + conversationIdToDelete + ", and delete Firebase summary.");
//
//                // --- First, attempt to delete the summary entry from Firebase for the current user ---
//                // The path is ChatSummaries/{currentUserID}/{chatPartnerId}
//                DatabaseReference firebaseSummaryRef = rootRef.child("ChatSummaries").child(currentUserID).child(userIdToDelete);
//                firebaseSummaryRef.removeValue().addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "Successfully deleted chat summary from Firebase for partner: " + userIdToDelete + " owner: " + currentUserID);
//
//                        // --- Now, delete the corresponding entries from Room DB on a background thread ---
//                        // This includes the ChatEntity, all messages for this chat pair, and the conversation key.
//                        databaseExecutor.execute(() -> { // Run on Room DB thread
//                            try {
//                                // 1. Delete the ChatEntity for this partner, owned by the current user
//                                // Make sure this DAO method exists and deletes WHERE ownerUserId = :ownerId AND userId = :userIdToDelete
//                                int deletedSummaryRows = chatDao.deleteChatByUserId(userIdToDelete, currentUserID); // Assuming this method exists
//                                if (deletedSummaryRows > 0) {
//                                    Log.d(TAG, "Successfully deleted chat list entry from Room for partner: " + userIdToDelete + " owned by: " + currentUserID);
//                                } else {
//                                    Log.w(TAG, "Attempted to delete chat list entry from Room for partner " + userIdToDelete + ", but it wasn't found for owner " + currentUserID);
//                                }
//
//                                // 2. Delete the related messages from Room for this conversation for THIS owner
//                                try {
//                                    // Assuming MessageDao has deleteAllMessagesForChat(ownerId, conversationId)
//                                    // Use the conversationIdToDelete received by the method.
//                                    int deletedMessageRows = messageDao.deleteAllMessagesForChat(currentUserID, conversationIdToDelete); // Assuming a DAO method that takes owner and convId
//                                    Log.d(TAG, "Successfully deleted " + deletedMessageRows + " messages from Room for conversation " + conversationIdToDelete + " for owner " + currentUserID + " during chat deletion.");
//                                } catch (Exception msgDeleteEx) {
//                                    Log.e(TAG, "Error deleting messages from Room during chat deletion for conv " + conversationIdToDelete + " for owner " + currentUserID + ".", msgDeleteEx);
//                                }
//
//
//                                // *** NEW ADDITION START ***
//                                // 3. Delete ALL conversation key VERSIONS from Room for this chat for the current user
//                                try {
//                                    // Assuming ConversationKeyDao has deleteAllKeysForConversation(ownerId, conversationId)
//                                    // Use the conversationIdToDelete received by the method. Access the new DAO via chatDatabase instance.
//                                    int deletedKeyRows = chatDatabase.conversationKeyDao().deleteAllKeysForConversation(currentUserID, conversationIdToDelete); // <<< ADD THIS LINE
//                                    if (deletedKeyRows > 0) {
//                                        Log.d(TAG, "Deleted " + deletedKeyRows + " conversation key versions from Room during chat deletion for conv " + conversationIdToDelete + " for owner " + currentUserID + ".");
//                                        // Optional: Remove from in-memory KeyManager if it was loaded and is still there
//                                        YourKeyManager.getInstance().removeConversationKey(conversationIdToDelete); // <<< ADD THIS LINE (removes all versions for this conv from cache)
//                                    } else {
//                                        Log.w(TAG, "Attempted to delete conversation key versions but no entries found in Room during chat deletion for conv " + conversationIdToDelete);
//                                    }
//                                } catch (Exception keyDeleteEx) {
//                                    Log.e(TAG, "Error deleting conversation key versions from Room during chat deletion for conv " + conversationIdToDelete + " for owner " + currentUserID + ".", keyDeleteEx);
//                                }
//                                // *** NEW ADDITION END ***
//
//
//                                // Show success toast on the UI thread after local deletion is attempted
//                                // Use getActivity() != null and !getActivity().isFinishing() for safety in fragments
//                                if (getActivity() != null && !getActivity().isFinishing()) {
//                                    getActivity().runOnUiThread(() ->
//                                            Toast.makeText(getContext(), "Chat cleared.", Toast.LENGTH_SHORT).show() // Changed toast text
//                                    );
//                                }
//
//                            } catch (Exception e) {
//                                // This catch block primarily handles errors in deleting ChatEntity itself
//                                Log.e(TAG, "Error deleting chat list entry or messages from Room for partner: " + userIdToDelete + " owned by: " + currentUserID, e);
//                                // Show local deletion error toast on the UI thread
//                                if (getActivity() != null && !getActivity().isFinishing()) {
//                                    getActivity().runOnUiThread(() ->
//                                            Toast.makeText(getContext(), "Failed to clear chat locally", Toast.LENGTH_SHORT).show()
//                                    );
//                                }
//                            }
//                        }); // End Room deletion executor
//
//                    } else {
//                        // Firebase deletion failed. Log the error and inform the user.
//                        Log.e(TAG, "Failed to delete chat summary from Firebase for partner: " + userIdToDelete + " owner: " + currentUserID, task.getException());
//                        // Show error toast on the UI thread
//                        if (getActivity() != null && !getActivity().isFinishing()) {
//                            getActivity().runOnUiThread(() ->
//                                    Toast.makeText(getContext(), "Failed to delete chat from server.", Toast.LENGTH_SHORT).show()
//                            );
//                        }
//                        // Decide if you still want to delete from Room even if Firebase fails? Probably not, for consistency.
//                    }
//                }); // End Firebase deletion listener
//            }
//
//
//            // Method to manually refresh data (if needed, currently called by refresh action)
//            public void refreshData() {
//                Log.d(TAG,"Manual refresh triggered.");
//                // The existing Firebase listener should automatically update when data changes.
//                // If you want to force a re-sync on refresh, you could remove and re-add the listener.
//                // However, addValueEventListener generally stays active and updates automatically.
//                // A simple Toast indicating sync attempt is sufficient if the listener is always active.
//                if (isNetworkAvailable() && chatSummariesRef != null && currentUserID != null) { // Added currentUserID check
//                    if (getContext() != null) {
//                        Toast.makeText(getContext(), "Refreshing chats...", Toast.LENGTH_SHORT).show();
//                    }
//                    // The attachChatSummaryListener checks if a listener exists. If not, it attaches.
//                    // If already attached, this call does nothing, but the listener *should* trigger
//                    // on any data change in Firebase anyway.
//                    // If you want a *full* re-fetch including users data, you might need a more specific sync method.
//                    // For now, relying on the listener should be okay.
//                    // attachChatSummaryListener(); // No need to re-attach if already attached unless listener was null
//                } else if (!isNetworkAvailable()) {
//                    if (getContext() != null) {
//                        Toast.makeText(getContext(), "Network unavailable. Cannot refresh.", Toast.LENGTH_SHORT).show();
//                    }
//                    // If offline, LiveData will still show cached data.
//                } else { // chatSummariesRef or currentUserID is null
//                    Log.w(TAG, "Cannot refresh data: Firebase ref or user ID is null.");
//                    if (getContext() != null) {
//                        Toast.makeText(getContext(), "Error refreshing data.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//
//            // In ChatFragment.java, add this new public method:
//
//            /**
//             * Forces the chat list to re-render its current data from the LiveData's last value.
//             * Useful when decryption keys become available after the data was initially loaded.
//             * Assumes the LiveData's last value is still in the `chatListLiveData` object.
//             */
//            public void forceRefreshDisplay() {
//                Log.d(TAG, "forceRefreshDisplay() called in ChatFragment.");
//                // Ensure LiveData has a value and adapter exists
//                if (chatListLiveData != null && chatListLiveData.getValue() != null && chatAdapter != null) {
//                    Log.d(TAG, "Re-processing and submitting current LiveData value (" + chatListLiveData.getValue().size() + " items) to ChatAdapter.");
//
//                    // Get the current list of ChatEntity items from the LiveData's last value
//                    List<ChatEntity> currentItems = chatListLiveData.getValue();
//
//                    // Manually process and submit the list again.
//                    // This logic is the same as the end of the onChanged method.
//                    // It will trigger the decryption check for each visible item in onBindViewHolder.
//
//                    List<ChatEntity> processedChatList = new ArrayList<>();
//                    if (currentItems != null) {
//                        for (ChatEntity chat : currentItems) {
//                            if (chat == null) continue;
//
//                            String encryptedLastMessage = chat.getLastMessage();
//                            String chatPartnerId = chat.getUserId();
//                            String conversationId = chat.getConversationId();
//                            String lastMessageType = chat.getLastMessageType();
//                            String username = chat.getUsername();
//                            String profileImage = chat.getProfileImage();
//                            long timestamp = chat.getTimestamp();
//                            int unreadCount = chat.getUnreadCount();
//                            String ownerUserId = chat.getOwnerUserId();
//
//
//                            String displayedLastMessagePreview = ""; // This will be the string shown in the UI bubble, initialize as empty or placeholder
//
//                            // --- Logic to determine what to display for the last message preview ---
//
//                            // Default placeholder based on type if content is empty or decryption fails
//                            String placeholder = "";
//                            if ("image".equals(lastMessageType)) placeholder = "[Image]";
//                            else if ("file".equals(lastMessageType)) placeholder = "[File]";
//                            else placeholder = ""; // Default for text or unknown
//
//                            // Check if the stored content is empty first
//                            if (TextUtils.isEmpty(encryptedLastMessage)) {
//                                displayedLastMessagePreview = placeholder; // Show placeholder if no content
//                            } else if ("text".equals(lastMessageType) || "image".equals(lastMessageType)) { // Only attempt decryption for message types that are encrypted
//                                // Attempt decryption if user's private key is available AND the conversation key is available in KeyManager's cache
//                                // The conversation key should have been loaded into KeyManager after account unlock (in Login/MainActivity)
//                                // We need ANY key version to decrypt the latest message preview.
//                                if (YourKeyManager.getInstance().isPrivateKeyAvailable() && YourKeyManager.getInstance().hasConversationKey(conversationId)) {
//                                    Log.d(TAG, "Attempting decryption for last message preview in chat " + conversationId + " with partner " + chatPartnerId);
//                                    // Get the *latest* key from KeyManager's memory cache for the preview
//                                    SecretKey conversationAESKey = YourKeyManager.getInstance().getLatestConversationKey(conversationId); // Use getLatestConversationKey
//
//                                    // Double check key is not null after retrieving (should be handled by hasConversationKey, but defensive)
//                                    if (conversationAESKey != null) {
//                                        try {
//                                            // encryptedLastMessage is the Base64 encoded encrypted data from Room
//                                            byte[] encryptedBytes = Base64.decode(encryptedLastMessage, Base64.DEFAULT); // Use android.util.Base64 for decoding
//                                            String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytes, conversationAESKey); // Use your CryptoUtils method
//
//                                            if ("text".equals(lastMessageType)) {
//                                                // Show only a snippet for long text messages
//                                                int maxLength = 50; // Display max 50 characters in preview
//                                                displayedLastMessagePreview = decryptedContent.length() > maxLength ?
//                                                        decryptedContent.substring(0, maxLength) + "..." :
//                                                        decryptedContent; // Use decrypted text or snippet
//                                                // Log.d(TAG, "Last message preview decrypted successfully: " + displayedLastMessagePreview);
//
//                                            } else if ("image".equals(lastMessageType)) {
//                                                // For image previews in the list, we typically just show "[Image]".
//                                                // The actual decrypted image Base64 is in the message.getMessage() of the *MessageEntity*, not ChatEntity.
//                                                // The ChatEntity's lastMessage is just a preview snippet/placeholder for the list.
//                                                displayedLastMessagePreview = placeholder; // For image, show placeholder like "[Image]" in the list preview
//
//                                                // Alternative (less common for list preview): If you stored the decrypted image Base64 preview
//                                                // in the ChatEntity.lastMessage field, you might use it here. But the video shows "[Locked]" or text previews.
//                                                // Example: if ChatEntity.lastMessage stores Base64 image after decryption,
//                                                // you might try to load a tiny thumbnail here if needed, but simpler to use placeholder.
//
//
//                                            } else { // Should not happen based on outer if condition, but fallback
//                                                displayedLastMessagePreview = "[Unknown Type - Decrypted]";
//                                                Log.w(TAG, "Unexpected message type '" + lastMessageType + "' processed for decryption in chat " + conversationId);
//                                            }
//
//                                        } catch (IllegalArgumentException e) { // Base64 decoding error
//                                            Log.e(TAG, "Base64 decoding error decrypting last message preview for chat " + conversationId, e);
//                                            displayedLastMessagePreview = "[Invalid Data]"; // Placeholder on Base64 error
//                                        } catch (Exception e) { // Catch decryption errors (wrong key, corrupt data, padding issues)
//                                            Log.e(TAG, "Decryption failed for last message preview in chat " + conversationId, e);
//                                            displayedLastMessagePreview = "[Encrypted Message - Failed]"; // Placeholder on crypto error
//                                        }
//                                    } else {
//                                        // Conversation key missing from KeyManager (shouldn't happen if hasConversationKey check passed, but defensive)
//                                        Log.w(TAG, "Conversation key returned null from KeyManager for chat " + conversationId + " during decryption attempt.");
//                                        displayedLastMessagePreview = "[Encrypted Message]"; // Placeholder
//                                    }
//                                } else {
//                                    // Secure chat is enabled and key is available, but stored content is empty.
//                                    Log.w(TAG, "Secure chat enabled, but stored content is empty for encrypted last message preview.");
//                                    displayedLastMessagePreview = placeholder; // Use type placeholder
//                                }
//                            } else {
//                                // Secure chat is NOT enabled, conversation key is missing, User's private key is missing,
//                                // OR the message type is NOT one we encrypt (e.g., file, or an unencrypted system message)
//                                // In this case, display the content as it is stored in Room DB.
//                                // If the content from Room is empty, use the type placeholder as a fallback.
//                                displayedLastMessagePreview = encryptedLastMessage; // Use the content stored in Room
//                                if (TextUtils.isEmpty(displayedLastMessagePreview)) {
//                                    displayedLastMessagePreview = placeholder;
//                                }
//                                // Log if displaying potentially encrypted data when keys aren't available
//                                if (!YourKeyManager.getInstance().isPrivateKeyAvailable() && ("text".equals(lastMessageType) || "image".equals(lastMessageType))) {
//                                    Log.w(TAG, "Displaying potential encrypted data for last message preview in chat " + conversationId + " because secure chat is disabled.");
//                                }
//                            }
//
//
//                            // --- Create a NEW ChatEntity object for the adapter ---
//                            // Create a new entity and copy all necessary fields, setting the PROCESSED last message.
//                            // This ensures LiveData doesn't get modified entities if it's an unmodifiable list.
//                            ChatEntity processedChat = new ChatEntity(); // Use empty constructor
//                            processedChat.setId(chat.getId()); // Make sure to copy Room auto-generated ID
//                            processedChat.setOwnerUserId(ownerUserId); // Keep the owner ID
//                            processedChat.setUserId(chatPartnerId); // Keep the chat partner ID
//                            processedChat.setConversationId(conversationId); // Keep the conversation ID
//                            processedChat.setUsername(username); // Keep the username
//                            processedChat.setProfileImage(profileImage); // Keep the profile image
//                            processedChat.setLastMessage(displayedLastMessagePreview); // *** Set the PROCESSED/DECRYPTED preview content ***
//                            processedChat.setTimestamp(timestamp); // Keep the timestamp
//                            processedChat.setUnreadCount(unreadCount); // Keep the unread count
//                            processedChat.setLastMessageType(lastMessageType); // Keep the message type
//
//                            processedChatList.add(processedChat); // Add the processed entity to the list for the adapter
//                        }
//                    }
//                    // --- END Processing Messages ---
//
//
//                    // Sort the list by timestamp DESC (latest message first) if not already sorted by DAO query
//                    // Make sure your DAO query already sorts by timestamp DESC, then this is redundant but safe.
//                    if (processedChatList.size() > 1) { // Only sort if more than one item
//                        Collections.sort(processedChatList, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
//                    }
//
//
//                    // Submit the list of ChatEntity with processed last messages to the adapter
//                    if (chatAdapter != null) {
//                        // Use submitList (more efficient with DiffUtil if implemented in adapter). Pass a *copy* if needed.
//                        // Using ArrayList<>(processedChatList) ensures a new list is passed, preventing issues with adapter's internal list reference.
//                        chatAdapter.submitList(new ArrayList<>(processedChatList));
//                        Log.d(TAG, "Submitted " + processedChatList.size() + " processed chats to adapter in forceRefreshDisplay.");
//                    } else {
//                        Log.e(TAG, "ChatAdapter is null in forceRefreshDisplay, cannot submit list.");
//                    }
//
//
//                    // Update UI visibility based on the final list size
//                    if (processedChatList.size() > 0) { // Check size > 0 for non-empty list
//                        if (privateChatsList != null) privateChatsList.setVisibility(View.VISIBLE); // Safety check
//                        if (noChatsText != null) noChatsText.setVisibility(View.GONE); // Safety check
//                        // Show search view only if there are chats to search
//                        if (searchView != null) searchView.setVisibility(View.VISIBLE); // Safety check
//                        Log.d(TAG, "Chat list visible after forceRefreshDisplay.");
//                    } else {
//                        if (privateChatsList != null) privateChatsList.setVisibility(View.GONE); // Safety check
//                        if (noChatsText != null) { // Safety check
//                            noChatsText.setVisibility(View.VISIBLE);
//                            noChatsText.setText("No chats yet"); // Message when list is empty
//                        }
//                        // Hide search view if there are no chats
//                        if (searchView != null) searchView.setVisibility(View.GONE); // Safety check
//                        Log.d(TAG, "No chats found after forceRefreshDisplay, showing No chats text.");
//                    }
//
//                    // Re-apply search filter if there's an active query
//                    // The adapter's filter method should work on the list containing *displayed* previews
//                    if (searchView != null && chatAdapter != null) {
//                        String currentQuery = searchView.getQuery().toString();
//                        if (!TextUtils.isEmpty(currentQuery)) { // Check if query is not empty
//                            Log.d(TAG, "Applying search filter after force refresh: '" + currentQuery + "'");
//                            // Ensure adapter's filter method works with the list containing processed previews
//                            // The adapter's filter logic should compare the *displayed* last message preview text
//                            chatAdapter.filter(currentQuery);
//                        }
//                    } else {
//                        Log.w(TAG, "SearchView or ChatAdapter is null in forceRefreshDisplay, cannot re-apply filter.");
//                    }
//
//                } else {
//                    Log.w(TAG, "forceRefreshDisplay() skipped: LiveData, value, or adapter is null.");
//                    // If LiveData has no value, the onChanged would likely have run already and updated the UI.
//                }
//            }
//
//
//        }












public class ChatFragment extends Fragment implements ChatAdapter.OnChatInteractionListener {

    private static final String TAG = "ChatFragment";

    private View privateChatsView;
    private RecyclerView privateChatsList;
    private TextView noChatsText;
    private SearchView searchView; // Use androidx.appcompat.widget.SearchView

    private DatabaseReference rootRef;
    private DatabaseReference usersRef;
    private DatabaseReference chatSummariesRef;

    private FirebaseAuth mAuth;
    private String currentUserID;

    private ChatDatabase chatDatabase;
    private ChatDao chatDao;
    private MessageDao messageDao;
    private ExecutorService databaseExecutor;

    private ConversationKeyDao conversationKeyDao;


    private ExecutorService chatOpenExecutor;
    private Handler chatOpenHandler;
    private ProgressDialog chatOpenProgressDialog;

    private ChatAdapter chatAdapter;
    private LiveData<List<ChatEntity>> chatListLiveData;

    private Handler mainHandler; // Handler for Main Thread UI updates

    private ValueEventListener chatSummaryListener;
    private boolean isChatSummaryListenerAttached = false;

    // --- NEW: OneSignal API Service ---
    private OneSignalApiService oneSignalApiService;
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR APP ID
    // Remember to add your REST API Key in the OneSignalApiService interface above.
    // --- END NEW ---


    @SuppressLint("RestrictedApi") // Needed for setHasOptionsMenu if used with older support library, remove if targeting newer AndroidX
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🟢 ChatFragment onCreate.");
        setHasOptionsMenu(true); // Optional: If you want to handle options menu for search etc.


        // --- Initialize Firebase Auth ---
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserID = currentUser.getUid();
            Log.d(TAG, "Current User ID: " + currentUserID);
        } else {
            Log.e(TAG, "User not authenticated in ChatFragment onCreate! Redirecting to login.");
            currentUserID = null;
            // Handle authentication error - e.g., redirect to login or show an error screen
            if (getContext() != null) {
                Toast.makeText(getContext(), "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) { // Check if fragment is attached to an activity
                    Intent loginIntent = new Intent(getActivity(), Login.class); // Assuming Login exists
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                    getActivity().finish(); // Finish hosting activity
                }
            }
            return; // Stop onCreate execution if user is not authenticated
        }
        // --- End Firebase Auth ---


        // --- Initialize Firebase Refs ---
        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("Users");
        // Reference to the node containing chat summaries for THIS user
        // Structure: ChatSummaries/{currentUserID}/{chatPartnerId}/...
        chatSummariesRef = rootRef.child("ChatSummaries").child(currentUserID); // Corrected path from previous code
        Log.d(TAG, "ChatSummaries Ref: " + chatSummariesRef.getPath());
        // --- End Initialize Firebase Refs ---


        // --- Initialize Room DB and Executors ---
        chatDatabase = ChatDatabase.getInstance(requireContext()); // Use requireContext() in Fragment
        chatDao = chatDatabase.chatDao();
        messageDao = chatDatabase.messageDao();
        conversationKeyDao = chatDatabase.conversationKeyDao(); // Initialize ConversationKeyDao
        databaseExecutor = ChatDatabase.databaseWriteExecutor; // Use the shared executor
        // --- End Initialize Room DB ---


        // --- Initialize Main Handler ---
        mainHandler = new Handler(Looper.getMainLooper()); // Initialize the Main Handler here
        Log.d(TAG, "Main Handler initialized in ChatFragment onCreate.");
        // --- End NEW ---


        // --- Initialize Executor and Handler for chat opening async tasks ---
        chatOpenExecutor = Executors.newSingleThreadExecutor(); // Dedicated executor for click tasks
        chatOpenHandler = new Handler(Looper.getMainLooper()); // Handler for UI updates after click tasks
        // Initialize dialog (use requireContext() as it's available in Fragment onCreate)
        chatOpenProgressDialog = new ProgressDialog(requireContext());
        chatOpenProgressDialog.setCancelable(false);
        // Make sure it's owned by the parent activity to prevent window leaks
        if (getActivity() != null) {
            chatOpenProgressDialog.setOwnerActivity(getActivity());
        }
        // --- End Initialization ---


        // --- NEW: Initialize Retrofit Service for OneSignal API ---
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    // Base URL for OneSignal API
                    .baseUrl("https://onesignal.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            oneSignalApiService = retrofit.create(OneSignalApiService.class);
            Log.d(TAG, "OneSignalApiService initialized.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignalApiService", e);
            oneSignalApiService = null; // Ensure it's null if initialization fails
        }
        // --- END NEW ---


        // Initial UI state - show loading text
        // We don't need to check KeyManager here, LiveData observer will handle the UI state
        // based on actual data loading and key availability during rendering.
        // Keep the initial text setup for visual feedback.
        Log.d(TAG, "Initial UI state: Showing loading text.");
        // UI elements are findViewById in onCreateView, handle their visibility there or later.

        Log.d(TAG, "✅ onCreate finished in ChatFragment.");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🟢 onCreateView started.");
        // Assuming R.layout.fragment_chat contains R.id.chat_list, R.id.noChatsText, R.id.search_view
        privateChatsView = inflater.inflate(R.layout.fragment_chat, container, false); // Ensure fragment_chat.xml exists

        // Initialize UI views
        privateChatsList = privateChatsView.findViewById(R.id.chat_list); // Match your layout ID
        noChatsText = privateChatsView.findViewById(R.id.noChatsText); // Match your layout ID
        searchView = privateChatsView.findViewById(R.id.search_view); // Make sure ID matches layout

        setupRecyclerView(); // Setup RecyclerView and Adapter
        setupSearchView(); // Setup SearchView

        // Set initial UI state - show loading
        if (noChatsText != null) { // Safety check
            noChatsText.setVisibility(View.VISIBLE);
            noChatsText.setText("Loading chats...");
        }
        if (privateChatsList != null) privateChatsList.setVisibility(View.GONE); // Safety check
        if (searchView != null) searchView.setVisibility(View.GONE); // Hide search initially


        Log.d(TAG, "✅ onCreateView finished.");
        return privateChatsView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "🟢 onViewCreated started.");

        // Check authentication again here, as the view might be created after onCreate.
        // If currentUserID is null, display an error state.
        if (currentUserID == null) {
            Log.e(TAG, "Cannot proceed in ChatFragment onViewCreated without currentUserID. Showing error UI.");
            showAuthenticationErrorUI(); // Helper to set error state UI
            Log.d(TAG, "✅ onViewCreated finished with auth error.");
            return; // Stop execution in this method
        }


        // --- Observe Room LiveData for changes ---
        // This observer runs on the main thread when data changes in Room.
        // Decryption of the last message preview happens *inside* this observer.
        observeChatList(); // Attach Room LiveData observer


        // --- Attach Firebase Listener for ChatSummaries ---
        // This listener syncs data from Firebase ChatSummaries to Room.
        // It should be attached whenever the fragment's view is active (onViewCreated or onStart).
        // It should be detached in onDestroyView or onStop.
        // We attach the listener regardless of network status. Firebase handles offline caching/sync.
        Log.d(TAG, "Attaching Firebase listener for ChatSummaries in onViewCreated.");
        attachChatSummaryListener(); // Attach the listener

        // No need to force refresh here explicitly.
        // The LiveData observer will trigger `onChanged` when the initial data is loaded from Room.
        // The decryption and UI update logic is within `onChanged`.

        // Check network status for informational toast only (listener works offline)
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network unavailable in onViewCreated. Relying on cached data from Room.");
            if (getContext() != null) {
                // Show toast only once when network is unavailable (maybe use a flag or ViewModel to show only once)
                Toast.makeText(getContext(), "Offline mode: Showing cached chats", Toast.LENGTH_SHORT).show();
            }
            // LiveData observer is already attached and will show cached data.
        }
        Log.d(TAG, "✅ onViewCreated finished.");
    }


    private void showAuthenticationErrorUI() {
        if (noChatsText != null) { // Safety check
            noChatsText.setVisibility(View.VISIBLE);
            noChatsText.setText("Authentication Error. Please restart the app or log in again.");
        }
        if (privateChatsList != null) privateChatsList.setVisibility(View.GONE); // Safety check
        if (searchView != null) searchView.setVisibility(View.GONE); // Hide search if no user
        // Also hide bottom navigation if applicable in the parent Activity or FragmentManager
        // You might need to access the parent activity's BottomNavigationView and set its visibility

    }


    // --- Setup RecyclerView and Adapter ---
    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView.");
        if (privateChatsList == null) {
            Log.w(TAG, "RecyclerView is null, cannot setup.");
            return;
        }
        privateChatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize ChatAdapter with context, empty list, listener, AND currentUserID
        // Pass currentUserID here. This ID is used by the adapter to determine message alignment/sender.
        // Use requireContext() which is safe in onCreateView().
        chatAdapter = new ChatAdapter(requireContext(), new ArrayList<>(), this, currentUserID); // Pass currentUserID here
        privateChatsList.setAdapter(chatAdapter);
    }

    // --- Setup SearchView ---
    private void setupSearchView() {
        Log.d(TAG, "Setting up SearchView.");
        // Use androidx.appcompat.widget.SearchView
        if (searchView != null) { // Safety check
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false; // Don't handle submission by default
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.d(TAG, "Search query changed: " + newText);
                    // Check if chatAdapter is not null before filtering
                    if (chatAdapter != null) {
                        // The adapter's filter method should work on the list containing *displayed* previews
                        // Assuming ChatAdapter's filter method filters based on username or the displayed last message preview.
                        chatAdapter.filter(newText); // Assuming your ChatAdapter has a filter method
                    } else {
                        Log.w(TAG, "ChatAdapter is null, cannot apply filter.");
                    }
                    return true; // Query handled
                }
            });
            // Add a listener for when the search view is closed
            searchView.setOnCloseListener(() -> {
                Log.d(TAG, "SearchView closed.");
                // Optionally reset the filter or load full data here if your filter implementation needs it
                // chatAdapter.filter(""); // Example: reset filter to empty string
                return false; // Allow the system to handle closing the search view
            });
        } else {
            Log.w(TAG, "SearchView is null, skipping setup.");
        }
    }


    // --- Observe LiveData from Room DAO (Single Key Decryption Logic) ---
    // --- Observe LiveData from Room DAO (Processing on Background Thread) ---
    private void observeChatList() {
        // Ensure currentUserID and chatDao are initialized before observing
        if (currentUserID == null || chatDao == null || databaseExecutor == null || messageDao == null) { // Add executor and messageDao checks
            Log.e(TAG, "currentUserID, chatDao, databaseExecutor, or messageDao is null, cannot observe chat list.");
            showAuthenticationErrorUI(); // Show error UI
            return; // Exit method if prerequisites are missing
        }
        Log.d(TAG, "Attaching Room LiveData observer for user: " + currentUserID);
        // Get the LiveData from the DAO for chats owned by the current user, ordered by latest message timestamp from Summary
        // Make sure your ChatDao.getAllChats method queries WHERE ownerUserId = :ownerUserId ORDER BY timestamp DESC
        chatListLiveData = chatDao.getAllChats(currentUserID); // Assuming this method exists and takes ownerId

        // Use getViewLifecycleOwner() for fragment-specific lifecycle.
        // The observer will be automatically removed when the fragment's view is destroyed.
        chatListLiveData.observe(getViewLifecycleOwner(), new Observer<List<ChatEntity>>() { // Use Observer interface
            @Override
            public void onChanged(List<ChatEntity> chatEntities) {
                // This observer callback runs on the main thread when data changes in Room.
                // We will now offload the heavy processing (DB query, decryption) to a background thread.
                Log.d(TAG, "LiveData onChanged triggered. Received " + (chatEntities != null ? chatEntities.size() : 0) + " chats from Room (Initial List) for owner: " + currentUserID);

                // Submit the processing of the list to the databaseExecutor (background thread)
                databaseExecutor.execute(() -> {
                    // This code runs on a background thread (databaseExecutor)

                    List<ChatEntity> processedChatList = new ArrayList<>();

                    // Get the current state of the user's private key and conversation keys
                    // These calls should be thread-safe if YourKeyManager is implemented correctly.
                    boolean isPrivateKeyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable(); // Check user's main private key state
                    // We will get the specific conversation key inside the loop if needed

                    if (chatEntities != null) {
                        for (ChatEntity chat : chatEntities) {
                            // Ensure chat entity is not null
                            if (chat == null) {
                                Log.w(TAG, "Skipping null ChatEntity from Room list during background processing.");
                                continue;
                            }
                            // --- Get all necessary fields from the original Room ChatEntity ---
                            // These fields are from the ChatEntity synced from Firebase Summary.
                            // We keep them to populate the processedChat entity and for sorting.
                            String chatPartnerId = chat.getUserId(); // Get chat partner ID
                            String conversationId = chat.getConversationId(); // Get the conversation ID
                            String lastMessageType = chat.getLastMessageType(); // Get lastMessageType from ChatEntity (from Summary)
                            long originalSummaryTimestamp = chat.getTimestamp(); // Keep original timestamp for sorting


                            // This will be the final string shown in the UI bubble preview
                            String displayedContent = "";
                            String processingOutcome = "Default"; // For logging outcome


                            // --- NEW: Query for the latest local UNDELETED message for this specific chat ---
                            // Use the new synchronous method in MessageDao
                            // This gives us the most recent message content actually available to the user locally.
                            MessageEntity latestLocalUndeletedMessage = messageDao.getLastLocalUndeletedMessage(currentUserID, chatPartnerId);

                            if (latestLocalUndeletedMessage == null) {
                                // No local messages found for this chat that are NOT marked as deleted for me.
                                // This chat is effectively empty for this user.
                                displayedContent = "[No messages]"; // Or an empty string "" depending on desired UI
                                processingOutcome = "No Local Undeleted Messages";
                                // Log.d(TAG, "Preview Processing: No local undeleted messages found for chat with " + chatPartnerId + " (" + conversationId + "). Showing placeholder.");

                            } else {
                                // Found a local message. Use its content and type for the preview logic.
                                String storedMessageContentFromLocal = latestLocalUndeletedMessage.getMessage(); // Content from the local message (Base64, plaintext, or placeholder)
                                String localMessageType = latestLocalUndeletedMessage.getType(); // Type from the local message
                                String localScheduledTime = latestLocalUndeletedMessage.getScheduledTime(); // *** Get scheduled time from local message ***
                                boolean isLocalMessageScheduled = !TextUtils.isEmpty(localScheduledTime); // *** Check if scheduled ***


                                // Determine if the local message type is one we expect to be encrypted
                                boolean isExpectedEncryptedType = ("text".equals(localMessageType) || "image".equals(localMessageType));

                                // Define known specific placeholder strings that the Worker or ChatPage might save directly as content
                                // These are strings like "[Image]", "[File]", "[Locked]", etc.
                                boolean isKnownSpecificPlaceholder = !TextUtils.isEmpty(storedMessageContentFromLocal) && (
                                        // Include placeholders from ChatFragment and ChatPageActivity
                                        "[No messages]".equals(storedMessageContentFromLocal) || // Should not happen in local *message* content
                                                "[Image]".equals(storedMessageContentFromLocal) ||
                                                "[File]".equals(storedMessageContentFromLocal) ||
                                                "[Locked]".equals(storedMessageContentFromLocal) ||
                                                "[Tap to Load Chat]".equals(storedMessageContentFromLocal) || // Should not happen in local *message* content
                                                "[Encrypted Message - Failed]".equals(storedMessageContentFromLocal) ||
                                                "[Invalid Encrypted Data]".equals(storedMessageContentFromLocal) ||
                                                "[System Message]".equals(storedMessageContentFromLocal) || // System message placeholder
                                                "[System Message Data Missing]".equals(storedMessageContentFromLocal) // System message placeholder
                                );


                                // --- Logic to determine what to display for the last message preview from the LOCAL message ---

                                // 1. Handle System messages - their content is already plaintext
                                if ("system_key_change".equals(localMessageType)) {
                                    displayedContent = storedMessageContentFromLocal; // Use the content directly from Room
                                    processingOutcome = "System Message (Local)";
                                    if (TextUtils.isEmpty(displayedContent)) displayedContent = "[System Message]"; // Fallback

                                }
                                // --- NEW: Handle Scheduled Messages (treat as plaintext/raw) ---
                                // This check comes BEFORE the decryption attempt for non-scheduled messages.
                                else if (isLocalMessageScheduled) {
                                    displayedContent = storedMessageContentFromLocal; // Display the content directly (it's plaintext/raw)
                                    processingOutcome = "Scheduled Message (Local, Plaintext)";
                                    if (TextUtils.isEmpty(displayedContent)) {
                                        // Fallback placeholder for empty scheduled content
                                        if ("image".equals(localMessageType)) displayedContent = "[Scheduled Image]";
                                        else if ("file".equals(localMessageType)) displayedContent = "[Scheduled File]"; // Assuming scheduled files are a thing
                                        else displayedContent = "[Scheduled Text]";
                                        processingOutcome += " (Content Empty Fallback)";
                                    }

                                }
                                // 2. Handle known Specific Placeholders - display them directly without any decryption attempt
                                // These are placeholders stored *as the content* in Room for specific states.
                                else if (isKnownSpecificPlaceholder) {
                                    displayedContent = storedMessageContentFromLocal; // Display the placeholder string as is
                                    processingOutcome = "Known Specific Placeholder (Local)";

                                }
                                // 3. Attempt Decryption IF it's an expected encrypted type AND content exists AND keys are available
                                // AND it's NOT a scheduled message (handled above) AND it's NOT a known placeholder (handled above).
                                // We check both isPrivateKeyAvailable AND hasConversationKey for decryption capability.
                                else if (isExpectedEncryptedType && !TextUtils.isEmpty(storedMessageContentFromLocal) && isPrivateKeyAvailable && YourKeyManager.getInstance().hasConversationKey(conversationId)) {

                                    processingOutcome = "Attempting Decryption (Local, Keys Available)";
                                    // Log.d(TAG, "Preview Processing: Attempting decryption for local message in conv ID: " + conversationId); // Debug log


                                    // Get the *single* conversation key from KeyManager cache
                                    SecretKey conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId); // Get key from cache


                                    // Double-check key is not null (should be true if hasConversationKey was true)
                                    if (conversationAESKey == null) {
                                        Log.e(TAG, "Preview Processing: Conversation key became null unexpectedly during decryption attempt for conv " + conversationId + ". Should not happen if hasConversationKey was true.");
                                        displayedContent = "[Decryption Error - Key Lost]";
                                        processingOutcome = "Decryption Error (Key Lost, Local)";
                                    } else {
                                        try {
                                            // Decode Base64 string from Room to bytes using CryptoUtils
                                            byte[] encryptedBytesWithIV = CryptoUtils.base64ToBytes(storedMessageContentFromLocal);

                                            if (encryptedBytesWithIV == null || encryptedBytesWithIV.length == 0) {
                                                Log.w(TAG, "Preview Processing: Decoded encrypted bytes null or empty for local preview in conv " + conversationId);
                                                displayedContent = (localMessageType.equals("image") ? "[Invalid Encrypted Image Data]" : "[Invalid Encrypted Data]");
                                                processingOutcome = "Decoded Empty (Local)";

                                            } else {
                                                // Decrypt bytes using the conversation key
                                                String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytesWithIV, conversationAESKey);

                                                // If decryption succeeds:
                                                if ("text".equals(localMessageType)) {
                                                    int maxLength = 50; // Preview length limit
                                                    displayedContent = decryptedContent.length() > maxLength ?
                                                            decryptedContent.substring(0, maxLength) + "..." :
                                                            decryptedContent; // Use decrypted text or snippet
                                                    // Log.d(TAG, "Preview Processing: Decrypted text preview: " + displayedContent); // Avoid logging content
                                                    processingOutcome = "Decrypted Success (Text, Local)";

                                                } else if ("image".equals(localMessageType)) {
                                                    // For image previews, show a placeholder even after successful decryption in the list
                                                    displayedContent = "[Image]"; // Placeholder for images in list
                                                    processingOutcome = "Decrypted Success (Image, Local)";
                                                } else { // Should not happen based on outer if condition, but fallback
                                                    displayedContent = "[Unknown Type - Decrypted]";
                                                    processingOutcome = "Decrypted Success (Unknown Type, Local)";
                                                }
                                            } // End else (decoded bytes not null/empty)

                                        } catch (IllegalArgumentException e) { // Base64 decoding error or invalid bytes format
                                            Log.e(TAG, "Preview Processing: Base64 decoding error decrypting local preview for conv " + conversationId, e);
                                            displayedContent = "[Invalid Encrypted Data]"; // Placeholder on Base64 error
                                            processingOutcome = "Base64 Decoding Error (Local)";
                                        } catch (BadPaddingException |
                                                 IllegalBlockSizeException |
                                                 InvalidKeyException | // Catch InvalidKeyException if the key is wrong
                                                 InvalidAlgorithmParameterException e) {
                                            // Crypto decryption failed for THIS key!
                                            Log.w(TAG, "Preview Processing: Decryption FAILED with conversation key for local msg in conv " + conversationId + ". Exception: " + e.getClass().getSimpleName());
                                            displayedContent = "[Encrypted Message - Failed]"; // Placeholder on crypto error with available key
                                            processingOutcome = "Decryption Failed (Crypto Error, Local)";
                                        } catch (Exception e) { // Catch any other unexpected errors
                                            Log.w(TAG, "Preview Processing: Unexpected error during local decryption for conv " + conversationId, e);
                                            displayedContent = "[Encrypted Message - Failed]"; // Fallback placeholder
                                            processingOutcome = "Decryption Failed (Unexpected Error, Local)";
                                        }
                                    } // End else (conversationAESKey != null)
                                }
                                // 4. If decryption was NOT attempted because keys were unavailable
                                else if (isExpectedEncryptedType && !TextUtils.isEmpty(storedMessageContentFromLocal) && (!isPrivateKeyAvailable || !YourKeyManager.getInstance().hasConversationKey(conversationId))) {
                                    // Content exists and should be encrypted, but keys are missing/unavailable
                                    // This is the case where PrivateKey is null OR ConversationKey is null in KeyManager
                                    displayedContent = "[Locked]"; // Placeholder indicating account needs unlocking or keys need loading
                                    processingOutcome = "Keys Unavailable (Local)";

                                }
                                // 5. Fallback for any other case:
                                //    - Not an expected encrypted type (e.g., "file")
                                //    - Message content from Room is empty after checking Scheduled/System
                                //    - Any other scenario not covered above, including unexpected data.
                                else {
                                    // Display the content as it was stored in Room.
                                    displayedContent = storedMessageContentFromLocal;
                                    processingOutcome = "Fallback Display (Local)";
                                    if (TextUtils.isEmpty(displayedContent)) {
                                        // Fallback to a type-based placeholder if stored content is empty
                                        if ("image".equals(localMessageType)) displayedContent = "[Image]";
                                        else if ("file".equals(localMessageType)) displayedContent = "[File]";
                                        else if ("system_key_change".equals(localMessageType)) displayedContent = "[System Message]";
                                        else displayedContent = "[Message]"; // Generic placeholder
                                        processingOutcome += " (Content Empty, Used Type Placeholder)";
                                    } else {
                                        // If content is not empty but didn't match encrypted types or placeholders, it's displayed as is.
                                        processingOutcome += " (Displayed as is, Local)";
                                    }
                                }
                                // --- End Logic (using Local message) ---
                            } // End else (latestLocalUndeletedMessage != null)


                            // Create a NEW ChatEntity object for the adapter with processed content
                            ChatEntity processedChat = new ChatEntity();
                            // Copy all fields from the original chat entity
                            processedChat.setId(chat.getId()); // Make sure to copy Room auto-generated ID (if used)
                            processedChat.setOwnerUserId(chat.getOwnerUserId()); // Keep the owner ID
                            processedChat.setUserId(chat.getUserId()); // Keep the chat partner ID
                            processedChat.setConversationId(chat.getConversationId()); // Keep the conversation ID
                            processedChat.setUsername(chat.getUsername()); // Keep the username
                            processedChat.setProfileImage(chat.getProfileImage()); // Keep the profile image

                            // *** Set the PROCESSED/DECRYPTED/PLACEHOLDER content determined based on LOCAL messages ***
                            processedChat.setLastMessage(displayedContent);

                            // Keep the original timestamp from the ChatEntity (Summary) for sorting,
                            // as this timestamp represents the last activity time of the chat thread overall in Firebase.
                            processedChat.setTimestamp(originalSummaryTimestamp);

                            // Keep other fields from the original ChatEntity
                            processedChat.setUnreadCount(chat.getUnreadCount()); // Keep the unread count
                            // Use the message type from the *latest local message* if one was found, otherwise default or use original summary type
                            processedChat.setLastMessageType(latestLocalUndeletedMessage != null ? latestLocalUndeletedMessage.getType() : lastMessageType);
                            // Optional: If ChatEntity has partnerKeysChanged, copy it:
                            // processedChat.setPartnerKeysChanged(chat.isPartnerKeysChanged());


                            processedChatList.add(processedChat); // Add the processed entity to the list for the adapter

                            // Log the final outcome for this chat preview processing
                            Log.d(TAG, "Preview Processing: Chat with " + chatPartnerId + " (" + conversationId + "). Outcome: " + processingOutcome + ". Final Displayed: '" + (displayedContent != null && displayedContent.length() > 5 ? displayedContent.substring(0, Math.min(displayedContent.length(), 50)) + (displayedContent.length() > 50 ? "..." : "") : displayedContent) + "'"); // Add null check for displayedContent and limit log length


                        } // End of for loop through chatEntities
                    } // End of if (chatEntities != null) check


                    // Sort the list by the original ChatEntity timestamp DESC (latest chat activity first).
                    // This ensures the list is ordered by when the last message was sent globally, not locally.
                    if (processedChatList.size() > 1) { // Only sort if more than one item
                        Collections.sort(processedChatList, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
                    }


                    // --- Post the processed list back to the Main Thread to update the UI ---
                    final List<ChatEntity> finalProcessedChatList = processedChatList; // Make final for handler
                    mainHandler.post(() -> { // Post to the main thread
                        // This code runs on the Main Thread

                        // Check if the fragment's view is still valid before updating UI components
                        if (getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) { // Check if view is at least CREATED

                            // Update the adapter with the new list of processed chats
                            if (chatAdapter != null) {
                                // Use submitList (more efficient with DiffUtil if implemented in adapter). Pass a *copy*.
                                // Using ArrayList<>(finalProcessedChatList) ensures a new list is passed.
                                chatAdapter.submitList(new ArrayList<>(finalProcessedChatList));
                                Log.d(TAG, "Submitted " + finalProcessedChatList.size() + " processed chats to adapter.");
                            } else {
                                Log.e(TAG, "ChatAdapter is null, cannot submit list on Main Thread.");
                            }


                            // Update UI visibility based on the final list size
                            if (finalProcessedChatList.size() > 0) {
                                if (privateChatsList != null) privateChatsList.setVisibility(View.VISIBLE); // Corrected RecyclerView variable name
                                if (noChatsText != null) noChatsText.setVisibility(View.GONE); // Safety check
                                // Show search view only if there are chats to search
                                if (searchView != null) searchView.setVisibility(View.VISIBLE); // Safety check
                                Log.d(TAG, "Chat list visible after processing.");
                            } else {
                                if (privateChatsList != null) privateChatsList.setVisibility(View.GONE); // Corrected RecyclerView variable name
                                if (noChatsText != null) {
                                    noChatsText.setVisibility(View.VISIBLE);
                                    noChatsText.setText("No chats yet"); // Message when list is empty
                                }
                                // Hide search view if there are no chats
                                if (searchView != null) searchView.setVisibility(View.GONE); // Safety check
                                Log.d(TAG, "No chats found after processing, showing No chats text.");
                            }

                            // Re-apply search filter if there's an active query
                            // The adapter's filter method should work on the list containing *displayed* previews
                            if (searchView != null && chatAdapter != null) {
                                String currentQuery = searchView.getQuery().toString();
                                if (!TextUtils.isEmpty(currentQuery)) {
                                    Log.d(TAG, "Applying search filter after processing: '" + currentQuery + "'");
                                    // Ensure adapter's filter method works with the list containing processed previews
                                    // The adapter's filter logic should compare the *displayed* last message preview text
                                    chatAdapter.filter(currentQuery);
                                }
                            } else {
                                Log.w(TAG, "SearchView or ChatAdapter is null, cannot re-apply filter after processing.");
                            }
                        } else {
                            Log.w(TAG, "Fragment view lifecycle state is less than CREATED. Skipping UI update after background processing.");
                        }
                    }); // End mainHandler.post
                }); // End databaseExecutor.execute()
            }
        });
    }
    // --- End Observe LiveData (Processing on Background Thread) ---

// ... (Keep the rest of your methods, including the new ones for delete for me) ...


    // --- Attach Real-time Listener for Firebase Chat Summaries ---
    // This listener listens to the current user's chat summaries node in Firebase
    // and syncs changes to the local Room database.
    @SuppressLint("RestrictedApi")
    private void attachChatSummaryListener() {
        // Ensure Firebase refs and currentUserID are initialized before attaching
        if (chatSummariesRef == null || currentUserID == null || databaseExecutor == null || chatDao == null || usersRef == null || messageDao == null || conversationKeyDao == null) { // Added all required DB/Executor checks
            Log.e(TAG, "chatSummariesRef, currentUserID, databaseExecutor, chatDao, usersRef, messageDao, or conversationKeyDao is null, cannot attach listener.");
            showAuthenticationErrorUI(); // Update UI to indicate error
            return;
        }

        // Only attach if not already attached
        if (!isChatSummaryListenerAttached) { // Use the flag
            Log.d(TAG, "Attaching Firebase ChatSummaries listener to: " + chatSummariesRef.getPath());
            chatSummaryListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Firebase ChatSummaries data received for user: " + currentUserID + ". Processing " + dataSnapshot.getChildrenCount() + " summaries.");

                    Set<String> firebasePartnerIds = new HashSet<>(); // To track partners found in Firebase

                    // Check if the snapshot itself exists and has children BEFORE processing
                    if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
                        Log.d(TAG, "No chat summaries found in Firebase for user: " + currentUserID + ". Clearing local chats for this owner.");
                        // Clear chats from Room if no summaries in Firebase
                        databaseExecutor.execute(() -> { // Run on DB thread
                            try {
                                // Make sure this method exists in ChatDao and deletes WHERE ownerUserId = :ownerId
                                int deletedCount = chatDao.deleteAllChatsForOwner(currentUserID); // Assuming this method exists
                                Log.d(TAG, "Successfully cleared " + deletedCount + " chats from Room as Firebase has no summaries for owner " + currentUserID + ".");
                            } catch (Exception e) {
                                Log.e(TAG, "Error clearing chats from Room for owner " + currentUserID, e);
                            }
                        });
                        // LiveData observer will update UI automatically after Room is cleared
                        return; // Exit onDataChange
                    }

                    // Process each chat partner's summary under the current user's node
                    for (DataSnapshot chatPartnerSummarySnap : dataSnapshot.getChildren()) {
                        // The key of the child node is the chat partner's UID
                        String chatPartnerId = chatPartnerSummarySnap.getKey();

                        // *** ADD THIS CHECK: Skip processing if it's a null, empty or self entry ***
                        if (TextUtils.isEmpty(chatPartnerId) || chatPartnerId.equals(currentUserID)) {
                            Log.d(TAG, "Skipping null, empty, or self-chat partner ID in summary: " + chatPartnerId);
                            continue;
                        }
                        // ****************************************************************

                        // Check if the snapshot for this partner has the expected data structure before proceeding
                        // IMPORTANT: Check for presence of required fields based on your ChatSummary structure.
                        // Ensure conversationId, lastMessageTimestamp, unreadCounts/{currentUserID} exist.
                        if (!chatPartnerSummarySnap.hasChild("conversationId") ||
                                !chatPartnerSummarySnap.hasChild("lastMessageTimestamp") ||
                                !chatPartnerSummarySnap.hasChild("unreadCounts") ||
                                !chatPartnerSummarySnap.child("unreadCounts").hasChild(currentUserID)) // Check for unread count node for THIS user
                        {
                            Log.w(TAG, "Skipping summary for " + chatPartnerId + ": Missing expected child nodes (convId, timestamp, unreadCounts/" + currentUserID + ").");
                            // Optional: Delete this invalid entry from Firebase ChatSummaries?
                            // This might indicate an issue with summary creation.
                            // chatPartnerSummarySnap.getRef().removeValue(); // Use with caution, could cause sync issues
                            continue; // Skip this entry
                        }


                        Log.d(TAG, "Processing summary for chat partner: " + chatPartnerId);
                        firebasePartnerIds.add(chatPartnerId); // Add to the set of found partners

                        // Get summary data directly from the snapshot
                        String conversationId = chatPartnerSummarySnap.child("conversationId").getValue(String.class);
                        // This is the ENCRYPTED or placeholder preview from the SENDER's Firebase summary
                        String encryptedLastMessageContentPreview = chatPartnerSummarySnap.child("lastMessageContentPreview").getValue(String.class); // Can be null
                        Long timestampLong = chatPartnerSummarySnap.child("lastMessageTimestamp").getValue(Long.class); // Can be null initially
                        // Get unread count specifically for the current user from the sub-node
                        Integer unreadCount = chatPartnerSummarySnap.child("unreadCounts").child(currentUserID).getValue(Integer.class); // Can be null
                        // String lastMessageSenderId = chatPartnerSummarySnap.child("lastMessageSenderId").getValue(String.class); // Not used in ChatEntity for now
                        String lastMessageType = chatPartnerSummarySnap.child("lastMessageType").getValue(String.class); // Can be null


                        // Basic validation for essential fields needed to create/update a chat entry in Room
                        if (TextUtils.isEmpty(chatPartnerId) || TextUtils.isEmpty(conversationId) || timestampLong == null || unreadCount == null) {
                            // This check is partially redundant with the hasChild checks above, but adds null/empty text check
                            Log.w(TAG, "Skipping summary for " + chatPartnerId + ": Essential fields are null/empty/invalid (partnerId, convId, timestamp, unreadCount).");
                            continue; // Skip this entry
                        }


                        // --- Fetch User Data (Username and Image) Asynchronously ---
                        // Fetch the chat partner's user data asynchronously
                        // Use SingleValueEvent as we only need the current state once per sync cycle for this user's profile.
                        // This runs on the main thread. The Room operation will be on a background thread.
                        if (usersRef != null) { // Safety check for usersRef
                            usersRef.child(chatPartnerId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                    String username = "Unknown User"; // Default username
                                    String base64Image = ""; // Default image (empty Base64)

                                    if (userSnap.exists()) {
                                        // Get username and profile image from the other user's profile data
                                        username = userSnap.child("username").getValue(String.class);
                                        base64Image = userSnap.child("profileImage").getValue(String.class);
                                        // Handle potential nulls/empty strings for display
                                        if (TextUtils.isEmpty(username)) username = "Unknown User";
                                        if (base64Image == null) base64Image = ""; // Ensure Base64 string is not null

                                        // Optional: Log fetched user data details
                                        // Log.d(TAG, "Fetched user data for chat partner: " + chatPartnerId + ", Username: " + username + ", Image exists: " + (!TextUtils.isEmpty(base64Image)));

                                    } else {
                                        Log.w(TAG, "User data not found in /Users/ for chat partner userId: " + chatPartnerId + " during sync. Keeping defaults.");
                                        // Keep default placeholders if user data is missing
                                    }

                                    // Create ChatEntity for Room using the fetched user data and summary data
                                    ChatEntity chatEntity = new ChatEntity(); // Use empty constructor
                                    // Set the primary key components and other fields
                                    chatEntity.setOwnerUserId(currentUserID); // Set owner to the current logged-in user
                                    chatEntity.setUserId(chatPartnerId); // Set chat partner ID (the other user in this chat)
                                    chatEntity.setConversationId(conversationId); // Store conversation ID

                                    // Set display information from the fetched user data
                                    chatEntity.setUsername(username);
                                    chatEntity.setProfileImage(base64Image);

                                    // Store the *ENCRYPTED* preview content (or placeholder) from the Firebase summary in Room
                                    chatEntity.setLastMessage(encryptedLastMessageContentPreview != null ? encryptedLastMessageContentPreview : ""); // Can be null from Firebase

                                    // Set timestamp, unread count, type, and sender ID
                                    chatEntity.setTimestamp(timestampLong != null ? timestampLong : 0L); // Store Firebase timestamp, default to 0 if null
                                    chatEntity.setUnreadCount(unreadCount != null ? unreadCount : 0); // Store the unread count FOR THIS USER, default to 0

                                    // Store lastMessageType (default to text if null or empty)
                                    chatEntity.setLastMessageType(TextUtils.isEmpty(lastMessageType) ? "text" : lastMessageType);

                                    // Optional: If you added fields for last message sender ID in ChatEntity
                                    // chatEntity.setLastMessageSenderId(lastMessageSenderId != null ? lastMessageSenderId : "");


                                    // Save/update the ChatEntity in Room DB on a background thread
                                    if (databaseExecutor != null && chatDao != null) { // Safety check for DB components
                                        databaseExecutor.execute(() -> { // Run on Room DB thread
                                            try {
                                                // Insert or replace the chat list entry for the current user and this chat partner
                                                chatDao.insertOrUpdateChat(chatEntity); // Use insertOrUpdate (REPLACE strategy)
                                                // Log success (can be verbose)
                                                // Log.d(TAG, "Successfully saved/updated chat list entry to Room for partner: " + chatPartnerId + " (Owner: " + currentUserID + ")");
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error saving/updating chat list entry to Room for partner: " + chatPartnerId + " (Owner: " + currentUserID + ")", e);
                                                // Handle Room DB errors if necessary (e.g., log, show internal error message)
                                            }
                                        });
                                    } else {
                                        Log.w(TAG, "Cannot save chat summary to Room, databaseExecutor or chatDao is null.");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, "Failed to fetch user data for chat partner " + chatPartnerId + " during sync: " + databaseError.getMessage());
                                    // Decide how to handle - if user data fetch fails, the chat entry might not be updated correctly in Room.
                                    // The last known state in Room will persist until next successful sync.
                                    // You might want to create/update the ChatEntity with default/error info here if user data is critical.
                                }
                            }); // End of usersRef.child(chatPartnerId).addListenerForSingleValueEvent (asynchronous fetch)
                        } else {
                            Log.w(TAG, "usersRef is null, cannot fetch user data for chat partner " + chatPartnerId);
                        }

                    } // End of processing each chat partner summary


                    // --- Clean up Room DB: Delete chats that are no longer in Firebase Summaries ---
                    // This ensures that if a chat summary is deleted from Firebase (e.g., by deleting the last message),
                    // it's also removed from the user's chat list in Room.
                    if (databaseExecutor != null && chatDao != null && messageDao != null && conversationKeyDao != null) { // Safety check for all required DB/Executor components
                        databaseExecutor.execute(() -> { // Run on Room DB thread
                            try {
                                // Need an immediate query method in your DAO to get current local chats owned by this user
                                // Make sure getAllChatsImmediate(ownerId) exists and is synchronous and retrieves chats by ownerId
                                List<ChatEntity> localChats = chatDao.getAllChatsImmediate(currentUserID); // Ensure this exists and is synchronous

                                if (localChats != null) {
                                    for (ChatEntity localChat : localChats) {
                                        // Check if this local chat entry (by partner ID) exists in the set of partners found in the Firebase snapshot
                                        // Ensure localChat.getUserId() is not null/empty before checking firebasePartnerIds
                                        if (!TextUtils.isEmpty(localChat.getUserId()) && !firebasePartnerIds.contains(localChat.getUserId())) {
                                            Log.d(TAG, "Deleting local chat entry for partner " + localChat.getUserId() + " as it's no longer in Firebase summaries for owner " + currentUserID + ".");

                                            // 1. Delete the ChatEntity for this partner, owned by the current user
                                            // Make sure deleteChatByUserId(partnerId, ownerId) exists and uses both IDs
                                            int deletedSummaryRows = chatDao.deleteChatByUserId(localChat.getUserId(), currentUserID); // Ensure this DAO method exists and uses ownerId

                                            if (deletedSummaryRows > 0) {
                                                Log.d(TAG, "Successfully deleted chat list entry from Room for partner: " + localChat.getUserId() + " owned by: " + currentUserID);
                                            } else {
                                                Log.w(TAG, "Attempted to delete chat list entry from Room for partner " + localChat.getUserId() + ", but it wasn't found for owner " + currentUserID);
                                            }


                                            // Optional: Also delete the related messages from Room for this conversation
                                            String conversationIdForMessageDelete = localChat.getConversationId();
                                            if (!TextUtils.isEmpty(conversationIdForMessageDelete)) {
                                                try {
                                                    // Assuming MessageDao has deleteAllMessagesForChat(ownerId, conversationId)
                                                    int deletedMessageRows = messageDao.deleteAllMessagesForChat(currentUserID, conversationIdForMessageDelete); // Assuming a DAO method that takes owner and convId
                                                    Log.d(TAG, "Successfully deleted " + deletedMessageRows + " messages from Room for conversation " + conversationIdForMessageDelete + " for owner " + currentUserID + " during sync cleanup.");
                                                } catch (Exception msgDeleteEx) {
                                                    Log.e(TAG, "Error deleting messages from Room during sync cleanup for conv " + conversationIdForMessageDelete + " for owner " + currentUserID + ".", msgDeleteEx);
                                                }
                                            } else {
                                                Log.w(TAG, "Cannot delete messages for local chat entry " + localChat.getUserId() + ": Conversation ID is empty.");
                                            }

                                            // Optional: Also delete the conversation key from Room if the chat is removed
                                            String conversationIdForKeyDelete = localChat.getConversationId();
                                            if (!TextUtils.isEmpty(conversationIdForKeyDelete)) {
                                                try {
                                                    // <<< Use the single-key DAO method >>>
                                                    int deletedKeyRows = conversationKeyDao.deleteKeyForConversation(currentUserID, conversationIdForKeyDelete); // Access the new DAO
                                                    if (deletedKeyRows > 0) {
                                                        Log.d(TAG, "Deleted conversation key from Room during sync cleanup for conv " + conversationIdForKeyDelete + " for owner " + currentUserID + ".");
                                                        // Optional: Remove from in-memory KeyManager if it was loaded and is still there
                                                        YourKeyManager.getInstance().removeConversationKey(conversationIdForKeyDelete);
                                                    } else {
                                                        Log.w(TAG, "Attempted to delete conversation key but it was not found in Room during sync cleanup for conv " + conversationIdForKeyDelete);
                                                    }
                                                } catch (Exception keyDeleteEx) {
                                                    Log.e(TAG, "Error deleting conversation key from Room during sync cleanup for conv " + conversationIdForKeyDelete + " for owner " + currentUserID + ".", keyDeleteEx);
                                                }
                                            } else {
                                                Log.w(TAG, "Cannot delete conversation key for local chat entry " + localChat.getUserId() + ": Conversation ID is empty.");
                                            }

                                        } // End if (!firebasePartnerIds.contains(localChat.getUserId()))
                                    } // End for loop through local chats
                                } // End if (localChats != null)
                                Log.d(TAG, "Finished Room cleanup based on Firebase summaries for owner " + currentUserID + ".");
                            } catch (Exception e) {
                                Log.e(TAG, "Error during Room cleanup after Firebase sync for owner " + currentUserID + ".", e);
                            }
                        }); // End Room cleanup executor
                    } else {
                        Log.w(TAG, "Cannot perform Room cleanup, databaseExecutor, chatDao, messageDao, or conversationKeyDao is null.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Firebase Chat Summary Listener Cancelled: " + databaseError.getMessage(), databaseError.toException());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load chats: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    // Decide if you need to disable UI or show permanent error here
                }
            };

            // Attach the listener
            chatSummariesRef.addValueEventListener(chatSummaryListener);
            isChatSummaryListenerAttached = true; // Set the flag
            Log.d(TAG, "Firebase ChatSummaries listener attached.");
        } else {
            // Listener is already attached or cannot be attached (user not logged in, handled earlier)
            Log.d(TAG, "ChatSummaries listener already attached or cannot be attached.");
        }
    }
    // --- End Attach Real-time Listener ---


    // --- Remove Firebase Listener and Observers in onDestroyView ---
    @Override
    public void onDestroyView() { // Use onDestroyView for Fragment lifecycle
        super.onDestroyView();
        Log.d(TAG, "🔴 ChatFragment onDestroyView");

        // Remove Firebase listener only if it was attached
        if (chatSummariesRef != null && chatSummaryListener != null && isChatSummaryListenerAttached) { // Check the flag
            chatSummariesRef.removeEventListener(chatSummaryListener);
            chatSummaryListener = null; // Nullify the listener reference
            isChatSummaryListenerAttached = false; // Reset the flag
            Log.d(TAG, "Firebase ChatSummaries listener removed.");
        } else {
            Log.d(TAG, "Firebase ChatSummaries listener was not attached or already removed.");
        }

        // Remove LiveData observer
        if (chatListLiveData != null) {
            // Use getViewLifecycleOwner() to ensure the observer is removed when the view is destroyed
            chatListLiveData.removeObservers(getViewLifecycleOwner());
            Log.d(TAG, "Room LiveData observer removed.");
        }

        // ExecutorService for Room DB write is managed by ChatDatabase class, no need to shut down here.

        // Clear view references to prevent memory leaks
        privateChatsView = null;
        privateChatsList = null;
        noChatsText = null;
        searchView = null;
        chatAdapter = null; // Clear adapter reference

        // DAOs and databaseExecutor are managed by ChatDatabase singleton, no need to nullify here.

        Log.d(TAG, "✅ ChatFragment onDestroyView finished.");
    }

    // Optional: Add onDestroy method for final Fragment cleanup
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔴 ChatFragment onDestroy");
        // Clean up any other non-view related resources specific to this fragment if needed.
        // If you had other executors or resources tied to the fragment's entire lifecycle, clean them here.
        // The Room DB executor is managed by ChatDatabase singleton, so no need to shut it down here.

        // Shutdown chatOpenExecutor gracefully
        if (chatOpenExecutor != null && !chatOpenExecutor.isShutdown()) {
            Log.d(TAG, "chatOpenExecutor shutting down.");
            chatOpenExecutor.shutdownNow(); // Or shutdown()
        }
        // Dismiss dialog if it's showing to prevent window leaks
        if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) {
            chatOpenProgressDialog.dismiss();
        }
        chatOpenProgressDialog = null; // Nullify dialog reference
    }


    // --- ChatAdapter Interaction Listener Methods (Keep These) ---

    @Override // From ChatAdapter.OnChatInteractionListener
    public void onChatClick(ChatEntity chat) {
        // This is called when a chat item is clicked in the RecyclerView list.
        Log.d(TAG, "Chat clicked: " + chat.getUsername() + " (Other User ID: " + chat.getUserId() + ") Conversation ID: " + chat.getConversationId());

        // Ensure currentUserID is available before proceeding
        if (currentUserID == null) {
            Log.e(TAG, "currentUserID is null in onChatClick. Cannot proceed.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: User not logged in.", Toast.LENGTH_SHORT).show();
            }
            return; // Exit if user ID is not available
        }

        // Get necessary info from the clicked ChatEntity
        String conversationId = chat.getConversationId();
        String chatPartnerId = chat.getUserId();
        String chatPartnerName = chat.getUsername();
        String chatPartnerImage = chat.getProfileImage();


        // Validate conversationId, partnerId, and partnerName
        if (TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(chatPartnerId) || TextUtils.isEmpty(chatPartnerName)) {
            Log.e(TAG, "Error: Missing essential chat details for clicked chat (ConvId: " + conversationId + ", PartnerId: " + chatPartnerId + ", PartnerName: " + chatPartnerName + ").");
            Toast.makeText(getContext(), "Error opening chat: Invalid chat details.", Toast.LENGTH_SHORT).show();
            return;
        }

        // *** NEW: Check if user's RSA Private Key is available and if conversation key is in KeyManager ***
        // The user's private key MUST be available to either decrypt a key from Firebase OR generate a new one.
        boolean isPrivateKeyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable();
        boolean hasConversationKeyInMemory = YourKeyManager.getInstance().hasConversationKey(conversationId); // Check for the single key in cache

        if (!isPrivateKeyAvailable) {
            // User's main account is not unlocked or setup. Cannot decrypt messages or generate keys.
            Log.w(TAG, "User's Private key is NOT available. Cannot open secure chat.");
            Toast.makeText(getContext(), "Your account is not unlocked for secure chat. Please set up or unlock your Security Passphrase in Settings.", Toast.LENGTH_LONG).show();
            // Do NOT proceed to open the chat activity in a disabled state from here.
            // User needs to fix their security setup first.
            return;
        }

        if (hasConversationKeyInMemory) {
            // Conversation key is already in the in-memory cache. We are ready to go!
            Log.d(TAG, "Conversation key found in KeyManager cache for ID: " + conversationId + ". Navigating directly to chat.");
            // Dismiss progress dialog if somehow showing (unlikely here, but safe)
            if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) {
                chatOpenProgressDialog.dismiss();
            }
            // Call navigateToChatActivity on the main thread as it performs UI actions
            mainHandler.post(() ->
                    navigateToChatActivity(conversationId, chatPartnerId, chatPartnerName, chatPartnerImage)
            );


        } else {
            // Private key is available, but the specific conversation key is NOT in KeyManager's cache.
            // This means it wasn't loaded during the initial load from Room, or the initial load failed for this key.
            // We need to attempt to load *this specific key* from Room (and potentially Firebase as a fallback).
            Log.d(TAG, "Conversation key NOT found in KeyManager cache for ID: " + conversationId + ". Attempting to load from Room/DB.");

            if (getContext() == null) {
                Log.w(TAG, "Context is null, cannot show progress dialog.");
                Toast.makeText(getContext(), "Error starting chat.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Ensure dialog instance is valid and owned by the activity
            if (chatOpenProgressDialog == null || (getActivity() != null && chatOpenProgressDialog.getOwnerActivity() != getActivity())) {
                chatOpenProgressDialog = new ProgressDialog(requireContext());
                chatOpenProgressDialog.setCancelable(false);
                if (getActivity() != null) chatOpenProgressDialog.setOwnerActivity(getActivity());
            }
            chatOpenProgressDialog.setMessage("Loading secure chat key...");
            if (!chatOpenProgressDialog.isShowing()) { // Only show if not already showing
                chatOpenProgressDialog.show();
            }

            // Initiate the asynchronous key loading process for THIS conversation
            // Use the dedicated executor for chat opening tasks
            loadConversationKeyForChatAsync(conversationId, chatPartnerId, chatPartnerName, chatPartnerImage);
        }
        // --- END NEW CHECK ---
    }


    // --- Async Key Loading Logic (Single Key Model) ---
    // This method attempts to load the conversation key from Room first, then Firebase.
    // It should be called on the Main Thread and execute heavy tasks on chatOpenExecutor.
    private void loadConversationKeyForChatAsync(String conversationId, String chatPartnerId, String chatPartnerName, String chatPartnerImage) {

        // Initial validation: Ensure context, user ID, conv ID, DAOs, and Firebase ref are available
        if (getContext() == null || TextUtils.isEmpty(currentUserID) || TextUtils.isEmpty(conversationId) ||
                conversationKeyDao == null || rootRef == null || chatOpenExecutor == null || chatOpenHandler == null || usersRef == null) {
            Log.e(TAG, "loadConversationKeyForChatAsync: Prerequisites missing. Cannot load key.");
            // Post error to main thread if context is available, otherwise just log
            if (getContext() != null && chatOpenHandler != null) {
                chatOpenHandler.post(() -> { // Post error to main thread
                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
                    Toast.makeText(getContext(), "Error loading chat key.", Toast.LENGTH_SHORT).show();
                });
            }
            return;
        }
        Log.d(TAG, "loadConversationKeyForChatAsync: Starting key load for conversation: " + conversationId + " for user: " + currentUserID);


        // Use the dedicated chat opening executor for the background task
        chatOpenExecutor.execute(() -> {

            String errorMessage = null; // To hold an error message if loading/decryption fails permanently
            boolean triggerNewKeyGeneration = false; // Flag to indicate if decryption failed with *all* versions, requiring new key


            // Check if key is somehow already loaded into KeyManager during executor task (unlikely, but safe)
            if (YourKeyManager.getInstance().hasConversationKey(conversationId)) {
                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key already in KeyManager during async task. Navigating.");
                // This scenario shouldn't happen often if onChatClick check is strict, but handle for robustness.
                final String finalConversationId = conversationId;
                final String finalChatPartnerId = chatPartnerId;
                final String finalChatPartnerName = chatPartnerName;
                final String finalRecipientImageBase64 = chatPartnerImage;
                chatOpenHandler.post(() -> { // Post navigation to main thread
                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
                    navigateToChatActivity(finalConversationId, finalChatPartnerId, finalChatPartnerName, finalRecipientImageBase64);
                });
                return; // Exit the executor task early
            }


            // --- Step 1: Attempt to load the SINGLE key for this conversation from Room DB ---
            // Room is the primary source after the initial bulk load in MainActivity/Login
            Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Attempting to load SINGLE key from Room DB for conv ID: " + conversationId + " for owner " + currentUserID);
            SecretKey loadedKeyFromRoom = null;
            try {
                // Use the single-key DAO method to get the ONE key for this user and conversation
                ConversationKeyEntity keyEntity = conversationKeyDao.getKeyForConversation(currentUserID, conversationId); // <<< Use single-key DAO method
                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Room query finished. Key found: " + (keyEntity != null));

                // If key found in Room, attempt to decode it and load it into KeyManager
                if (keyEntity != null && !TextUtils.isEmpty(keyEntity.getDecryptedKeyBase64())) {
                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key found in Room. Decoding...");
                    try {
                        // Use android.util.Base64 for decoding from Room storage format
                        byte[] decryptedKeyBytes = Base64.decode(keyEntity.getDecryptedKeyBase64(), Base64.DEFAULT);
                        loadedKeyFromRoom = CryptoUtils.bytesToSecretKey(decryptedKeyBytes);
                        // Put the key into KeyManager's cache immediately if decoding was successful
                        YourKeyManager.getInstance().setConversationKey(conversationId, loadedKeyFromRoom); // <<< Use single-key KeyManager method
                        Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key loaded from Room DB into KeyManager for conv ID: " + conversationId);
                    } catch (IllegalArgumentException e) { // Error decoding Base64 or invalid key format
                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Error decoding/converting key from Room for conv ID: " + conversationId + ". Deleting corrupt entry.", e);
                        errorMessage = "Failed to decode key from local storage."; // Set error message
                        // Delete corrupt entry from Room on the database executor
                        if (databaseExecutor != null && conversationKeyDao != null) {
                            databaseExecutor.execute(() -> {
                                try { conversationKeyDao.deleteKeyForConversation(currentUserID, conversationId); Log.d(TAG, "Executor - DB: Deleted corrupt key from Room: " + conversationId); }
                                catch (Exception deleteEx) { Log.e(TAG, "Executor - DB: Error deleting empty key from Room", deleteEx); }
                            });
                        }
                    } catch (Exception e) { // Catch any other unexpected exceptions during processing
                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Unexpected error processing key from Room for conv ID: " + conversationId, e);
                        errorMessage = "Error loading key from local storage."; // Set error message
                        if (databaseExecutor != null && conversationKeyDao != null) {
                            databaseExecutor.execute(() -> {
                                try { conversationKeyDao.deleteKeyForConversation(currentUserID, conversationId); Log.d(TAG, "Executor - DB: Deleted key from Room after processing error: " + conversationId); }
                                catch (Exception deleteEx) { Log.e(TAG, "Executor - DB: Error deleting key from Room", deleteEx); }
                            });
                        }
                    }
                } else {
                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): No key found in Room for conv ID: " + conversationId);
                }
            } catch (Exception e) { // Catch any exception during Room DAO query itself
                Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Overall error fetching key from Room DB for conv ID: " + conversationId, e);
                errorMessage = "Error accessing local key storage."; // Set error message
            }


            // --- Step 2: Fallback to Fetching from Firebase if key NOT loaded into KeyManager from Room ---
            // Check if KeyManager still doesn't have the single key *after* trying Room.
            if (!YourKeyManager.getInstance().hasConversationKey(conversationId)) {
                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Single conversation key NOT loaded into KeyManager from Room. Falling back to Firebase.");

                // We need the user's Private RSA key to decrypt the keys from Firebase.
                PrivateKey currentUserPrivateKey = YourKeyManager.getInstance().getUserPrivateKey();

                if (currentUserPrivateKey == null) {
                    // This state should have been caught earlier in onChatClick, but check defensively.
                    Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Private key is unexpectedly null when trying to fetch from Firebase fallback.");
                    errorMessage = "Your private key is not available to decrypt chat keys from server.";
                    // Do NOT trigger new key generation.

                } else {
                    Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Fetching key VERSIONS from Firebase for conv ID: " + conversationId + " for user " + currentUserID);
                    try {
                        // Fetch the 'key_versions' node for this conversation
                        DataSnapshot keyVersionsSnapshot = Tasks.await(rootRef.child("ConversationKeys").child(conversationId).child("key_versions").get());
                        Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Firebase get() for key_versions completed. Snapshot exists: " + keyVersionsSnapshot.exists() + ", has children: " + keyVersionsSnapshot.hasChildren());

                        SecretKey successfullyDecryptedKeyFromFirebase = null; // Hold the *one* key we successfully decrypt from Firebase
                        long successfullyDecryptedKeyTimestamp = 0L; // Hold its timestamp (optional for single-key model, but useful for logging)

                        if (keyVersionsSnapshot.exists() && keyVersionsSnapshot.hasChildren()) {
                            // Found key versions in Firebase. Iterate and attempt decryption.
                            // We want to find *any* key version that the CURRENT user can decrypt with their CURRENT private key.
                            // Sorting by timestamp descending and trying the latest first is a reasonable heuristic.
                            Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Key versions found in Firebase. Attempting decryption for user " + currentUserID);

                            // Copy snapshots to a list to sort by timestamp descending (latest first)
                            List<DataSnapshot> keyVersionSnapsList = new ArrayList<>();
                            for (DataSnapshot snap : keyVersionsSnapshot.getChildren()) { keyVersionSnapsList.add(snap); }
                            // Sort by timestamp (the key name is the timestamp string) descending
                            Collections.sort(keyVersionSnapsList, (s1, s2) -> {
                                try {
                                    Long ts1 = Long.parseLong(s1.getKey());
                                    Long ts2 = Long.parseLong(s2.getKey());
                                    return Long.compare(ts2, ts1); // Descending order
                                } catch (NumberFormatException e) {
                                    Log.w(TAG, "Error parsing timestamp key during sort: " + s1.getKey() + " or " + s2.getKey());
                                    return 0; // Treat as equal if parsing fails
                                }
                            });

                            // Loop through sorted versions, stop after the first successful decryption
                            for (DataSnapshot keyVersionSnap : keyVersionSnapsList) {
                                String timestampString = keyVersionSnap.getKey();
                                long keyTimestamp;
                                try {
                                    keyTimestamp = Long.parseLong(timestampString);
                                    if (keyTimestamp <= 0) throw new NumberFormatException("Invalid timestamp value");
                                } catch (NumberFormatException e) {
                                    Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Skipping Firebase key version with invalid timestamp key: " + timestampString + " for conv " + conversationId);
                                    continue; // Skip this key version and move to the next
                                }

                                // Check if this key version snapshot contains the encrypted key data for the *current user*
                                if (keyVersionSnap.hasChild(currentUserID)) {
                                    String encryptedAesKeyForCurrentUserBase64 = keyVersionSnap.child(currentUserID).getValue(String.class);

                                    if (!TextUtils.isEmpty(encryptedAesKeyForCurrentUserBase64)) {
                                        try {
                                            // Decode Base64 string using android.util.Base64 (Firebase storage format)
                                            byte[] encryptedAesKeyBytes = Base64.decode(encryptedAesKeyForCurrentUserBase64, Base64.DEFAULT);
                                            // Attempt decryption using the *current* private key
                                            byte[] decryptedAesKeyBytes = CryptoUtils.decryptWithRSA(encryptedAesKeyBytes, currentUserPrivateKey);
                                            successfullyDecryptedKeyFromFirebase = CryptoUtils.bytesToSecretKey(decryptedAesKeyBytes);

                                            // If decryption succeeded: We found a working key!
                                            successfullyDecryptedKeyTimestamp = keyTimestamp;
                                            Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Successfully decrypted key version " + keyTimestamp + " from Firebase for user " + currentUserID + " conv " + conversationId);
                                            // *** IMPORTANT: Break the loop once ONE key version is successfully decrypted! ***
                                            break; // Found a working key, stop searching further versions
                                        } catch (BadPaddingException |
                                                 IllegalBlockSizeException |
                                                 InvalidKeyException e) {
                                            // Crypto decryption failed for THIS key version (likely due to old key from before a reset)
                                            // This is expected behavior if the user reset their keys and this is an old key version.
                                            Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Crypto error during decryption of version " + keyTimestamp + " from Firebase for user " + currentUserID + " conv " + conversationId + ". This key version might be unreadable with current key.", e);
                                            // Continue loop to try next key version.
                                        } catch (IllegalArgumentException e) { // Base64 decoding error from Firebase data
                                            Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Base64 decoding error for version " + keyTimestamp + " from Firebase for conv ID: " + conversationId, e);
                                            // Still a data format error for this specific version. Continue to next version.
                                        } catch (Exception e) { // Catch any other unexpected exceptions from crypto ops
                                            Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Unexpected error during decryption of version " + keyTimestamp + " from Firebase for user " + currentUserID + " conv " + conversationId + ".", e);
                                            // Still an error for this specific version. Continue to next version.
                                        }
                                    } else {
                                        // Encrypted key data is empty or null for this user in this key version (data inconsistency)
                                        Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Encrypted key data is empty/null for user " + currentUserID + " in Firebase key version " + keyTimestamp + " for conv " + conversationId + ". Skipping this version.");
                                        // This particular key version cannot be used by this user. Continue to next version.
                                    }

                                } else {
                                    // Key entry for the current user is NOT found in this key version snapshot (data inconsistency)
                                    Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Key entry for user " + currentUserID + " is MISSING in Firebase key version " + keyTimestamp + " for conv " + conversationId + ". Skipping this version.");
                                    // Continue to the next key version.
                                }
                            } // End loop through key versions in Firebase


                            // After trying all versions, check if we successfully decrypted ANY key
                            if (successfullyDecryptedKeyFromFirebase != null) {
                                // We found a working key! Load it into KeyManager and save it to Room (replacing any old one).
                                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Successfully decrypted A key version (" + successfullyDecryptedKeyTimestamp + ") from Firebase. Loading into KeyManager and saving to Room.");

                                YourKeyManager.getInstance().setConversationKey(conversationId, successfullyDecryptedKeyFromFirebase); // <<< Use the SINGLE-KEY KeyManager method

                                // *** Save the successfully fetched/decrypted key to Room DB for persistence ***
                                Log.d(TAG, "loadConversationKeyForChatAsync (Executor): Saving fetched/decrypted key version " + successfullyDecryptedKeyTimestamp + " to Room DB.");
                                // Convert decrypted key bytes (successfullyDecryptedKeyFromFirebase.getEncoded()) to Base64 string for Room storage.
                                String decryptedKeyBase64ForRoom = Base64.encodeToString(successfullyDecryptedKeyFromFirebase.getEncoded(), Base64.DEFAULT); // <<< Use android.util.Base64.DEFAULT
                                // *** Use the SINGLE-KEY ConversationKeyEntity constructor and DAO method ***
                                ConversationKeyEntity keyEntityToSave = new ConversationKeyEntity(currentUserID, conversationId, decryptedKeyBase64ForRoom); // <<< Use SINGLE-KEY constructor
                                if (databaseExecutor != null && conversationKeyDao != null) {
                                    long finalSuccessfullyDecryptedKeyTimestamp = successfullyDecryptedKeyTimestamp; // Capture for logging in inner executor
                                    databaseExecutor.execute(() -> {
                                        try {
                                            conversationKeyDao.insertOrUpdateKey(keyEntityToSave); // Insert or update based on primary key (owner, conv)
                                            Log.d(TAG, "Executor - DB: Key version (Firebase T:" + finalSuccessfullyDecryptedKeyTimestamp + ") saved to Room after fetching/decrypting from Firebase for conv ID: " + conversationId + " (Single Key).");
                                        } catch (Exception saveEx) {
                                            Log.e(TAG, "Executor - DB: Error saving fetched key version (Firebase T:" + finalSuccessfullyDecryptedKeyTimestamp + ") to Room DB (Single Key)", saveEx);
                                            // Log error, don't show toast, chat will still work for this session from KeyManager
                                        }
                                    });
                                } else {
                                    Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Cannot save fetched key to Room, databaseExecutor or DAO is null.");
                                }
                                // Key successfully loaded from Firebase and saved to Room/KeyManager.

                            } else {
                                // Decryption failed for ALL available key versions found in Firebase for this conversation.
                                // This implies the key used to encrypt all versions is unreadable with the current private key (e.g., sender reset keys).
                                // Or it could be genuinely corrupt/missing data for all versions.
                                // Trigger NEW key generation for this conversation.
                                Log.w(TAG, "loadConversationKeyForChatAsync (Executor): Decryption failed for ALL available key versions from Firebase for conv " + conversationId + ". Triggering NEW key generation.");
                                triggerNewKeyGeneration = true; // Set the flag
                                // DO NOT SET errorMessage here.
                            }

                        } else {
                            // 'key_versions' node not found or has no children in Firebase for this conversation ID.
                            // This means no keys were ever generated for this conversation, OR they were all deleted from Firebase.
                            // Treat as a brand new secure chat SETUP for this user's perspective FOR THIS CONV.
                            Log.w(TAG, "loadConversationKeyForChatAsync (Executor): 'key_versions' node not found or empty in Firebase for conv " + conversationId + ". Triggering NEW key generation.");
                            triggerNewKeyGeneration = true;
                        }

                    } catch (ExecutionException | InterruptedException e) { // Handle Tasks.await() errors
                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Firebase fetch failed for key_versions", e);
                        errorMessage = "Failed to fetch key data from server. Network error?";
                        // Do NOT trigger new key generation on network error; it might be temporary.
                    } catch (Exception e) { // Catch any other unexpected exceptions from Firebase fetch/processing
                        Log.e(TAG, "loadConversationKeyForChatAsync (Executor): Unexpected error during Firebase key_versions fetch/processing", e);
                        errorMessage = "An error occurred fetching key data.";
                        // Do NOT trigger new key generation on unexpected fetch error.
                    }
                } // End else (currentUserPrivateKey != null)
            } // End if (!KeyManager...after Room)


            // --- Post result back to Main Thread ---
            final String finalErrorMessage = errorMessage;
            final boolean finalTriggerNewKeyGeneration = triggerNewKeyGeneration;
            final String finalConversationId = conversationId;
            final String finalChatPartnerId = chatPartnerId;
            final String finalChatPartnerName = chatPartnerName;
            final String finalRecipientImageBase64 = chatPartnerImage;
            final String finalCurrentUserId = currentUserID;


            chatOpenHandler.post(() -> { // Post to the main thread
                // Dismiss the progress dialog here, BEFORE potentially starting key generation or navigating
                if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();

                // Check if a key is now available in KeyManager (either loaded from Room or Firebase)
                boolean isKeyNowAvailableInManager = YourKeyManager.getInstance().hasConversationKey(finalConversationId);

                if (finalTriggerNewKeyGeneration) {
                    Log.d(TAG, "loadConversationKeyForChatAsync (Main Thread): Decryption failed for all versions OR key node missing. Triggering NEW key generation for conv ID: " + finalConversationId);
                    // Call generateAndSaveConversationKeysAsync from the main thread
                    generateAndSaveConversationKeysAsync(finalConversationId, finalCurrentUserId, finalChatPartnerId, finalChatPartnerName, finalRecipientImageBase64);
                    // generateAndSaveConversationKeysAsync handles navigation/error messages

                } else if (isKeyNowAvailableInManager) {
                    // Key is now successfully loaded into KeyManager
                    Log.d(TAG, "loadConversationKeyForChatAsync (Main Thread): Key loaded successfully into KeyManager. Navigating to chat.");
                    navigateToChatActivity(finalConversationId, finalChatPartnerId, finalChatPartnerName, finalRecipientImageBase64);

                } else {
                    // Key loading failed (neither Room nor Firebase fallback worked), AND new key generation was NOT triggered
                    Log.e(TAG, "loadConversationKeyForChatAsync (Main Thread): Failed to load ANY conversation key for conv ID: " + finalConversationId + ". Error: " + finalErrorMessage);
                    Toast.makeText(getContext(), finalErrorMessage != null ? finalErrorMessage : "Failed to open secure chat.", Toast.LENGTH_LONG).show();
                    // Do NOT navigate to chat activity
                }
            });
        });
    }


    // --- Async Key Generation and Saving Logic (Single Key Model) ---
    // This method generates a new key, encrypts it for both users, saves to Firebase (as a new version),
    // saves the decrypted key to Room (replacing the old single key), puts it in KeyManager, and navigates.
    // It should be called on the Main Thread and execute heavy tasks on chatOpenExecutor.
    private void generateAndSaveConversationKeysAsync(String conversationId, String currentUserId, String recipientUserId, String recipientName, String recipientImageBase64) {
        // Ensure context and essential components are available before starting
        if (getContext() == null || TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(recipientUserId) ||
                chatOpenExecutor == null || chatOpenHandler == null || rootRef == null || usersRef == null || conversationKeyDao == null || databaseExecutor == null) {
            Log.e(TAG, "generateAndSaveConversationKeysAsync: Prerequisites missing. Cannot generate keys.");
            // Post error to main thread if context is available
            if (getContext() != null && chatOpenHandler != null) {
                chatOpenHandler.post(() -> {
                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
                    Toast.makeText(getContext(), "Error setting up secure chat.", Toast.LENGTH_SHORT).show();
                });
            }
            return;
        }

        // Ensure progress dialog is showing
        if (chatOpenProgressDialog == null || (getActivity() != null && chatOpenProgressDialog.getOwnerActivity() != getActivity())) {
            chatOpenProgressDialog = new ProgressDialog(requireContext());
            chatOpenProgressDialog.setCancelable(false);
            if (getActivity() != null) chatOpenProgressDialog.setOwnerActivity(getActivity());
        }
        if (!chatOpenProgressDialog.isShowing()) {
            chatOpenProgressDialog.setMessage("Generating and saving keys...");
            chatOpenProgressDialog.show();
        }
        Log.d(TAG, "Generating and saving NEW conversation key for " + conversationId + " (Users: " + currentUserId + ", " + recipientUserId + ")");

        // Fetch recipient's public key from Firebase (needed for RSA encryption)
        DatabaseReference recipientUserRef = usersRef.child(recipientUserId); // Use UsersRef member variable

        recipientUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("RestrictedApi") // Keep if needed by Firebase library internals
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This callback runs on the main thread due to addListenerForSingleValueEvent
                // Perform heavy crypto and Firebase save on background thread (chatOpenExecutor)
                chatOpenExecutor.execute(() -> {
                    SecretKey conversationAESKey = null; // The decrypted AES key
                    byte[] conversationAESKeyBytes = null; // The raw bytes of the AES key

                    PublicKey recipientPublicKey = null; // Recipient's RSA Public Key
                    PublicKey currentUserPublicKey = null; // Current user's RSA Public Key
                    byte[] encryptedAesKeyForRecipient = null; // AES key encrypted with recipient's public key
                    byte[] encryptedAesKeyForCurrentUser = null; // AES key encrypted with current user's public key

                    String errorMessage = null; // To hold error message if key generation/encryption fails

                    try {
                        // User's Private key MUST be available because onChatClick checked isPrivateKeyAvailable.
                        PrivateKey currentUserPrivateKey = YourKeyManager.getInstance().getUserPrivateKey();
                        currentUserPublicKey = YourKeyManager.getInstance().getUserPublicKey();

                        if (currentUserPrivateKey == null || currentUserPublicKey == null) {
                            // This indicates a severe internal state error if isPrivateKeyAvailable was true but PublicKey is null
                            Log.e(TAG, "Current user's RSA keys are null during key generation! KeyManager state error. Cannot proceed.");
                            throw new IllegalStateException("Current user RSA keys unavailable from KeyManager");
                        }
                        Log.d(TAG, "Current user public key obtained from KeyManager.");


                        // 1. Generate a new Conversation AES Key (Symmetric Key)
                        conversationAESKey = CryptoUtils.generateAESKey();
                        conversationAESKeyBytes = CryptoUtils.secretKeyToBytes(conversationAESKey); // Get the raw bytes
                        Log.d(TAG, "Generated new AES conversation key.");

                        // 2. Get Recipient's Public Key from Firebase snapshot
                        if (snapshot.exists() && snapshot.hasChild("publicKey")) {
                            String recipientPublicKeyBase64 = snapshot.child("publicKey").getValue(String.class);
                            if (TextUtils.isEmpty(recipientPublicKeyBase64)) {
                                Log.w(TAG, "Recipient public key Base64 is empty for " + recipientUserId + " in Firebase.");
                                // This recipient cannot receive secure messages. Show error.
                                errorMessage = "Recipient has not completed security setup. Cannot start secure chat.";
                                return; // Stop this chatOpenExecutor task early
                            }
                            // Use android.util.Base64 for decoding the Base64 string from Firebase (assuming this format)
                            byte[] recipientPublicKeyBytes = Base64.decode(recipientPublicKeyBase64, Base64.DEFAULT);
                            recipientPublicKey = CryptoUtils.bytesToPublicKey(recipientPublicKeyBytes);
                            Log.d(TAG, "Recipient public key obtained for " + recipientUserId);

                            // 3. Encrypt the Conversation AES Key for the RECIPIENT using THEIR RSA Public Key
                            encryptedAesKeyForRecipient = CryptoUtils.encryptWithRSA(conversationAESKeyBytes, recipientPublicKey);
                            Log.d(TAG, "AES key encrypted for recipient.");

                            // 4. Encrypt the Conversation AES Key for the CURRENT USER using THEIR RSA Public Key
                            // This is encrypted using the user's *own* public key, mainly for storing in Firebase for consistency.
                            encryptedAesKeyForCurrentUser = CryptoUtils.encryptWithRSA(conversationAESKeyBytes, currentUserPublicKey);
                            Log.d(TAG, "AES key encrypted for current user.");


                        } else {
                            // Recipient does NOT have crypto keys or user data missing in Firebase.
                            Log.w(TAG, "Recipient (" + recipientUserId + ") does not have public key in Firebase or user data missing. Cannot start secure chat.");
                            errorMessage = "Recipient has not completed security setup. Secure chat unavailable.";
                            return;
                        }
                    } catch (IllegalArgumentException | IllegalStateException e) { // Base64 decoding error, invalid key format, or KeyManager state error
                        Log.e(TAG, "Key data processing error or state error during key generation: " + e.getMessage(), e);
                        errorMessage = "Failed to start secure chat: Invalid key data encountered.";
                        return; // Stop task
                    } catch (Exception e) { // Catch any other unexpected errors from crypto ops
                        Log.e(TAG, "Cryptographic or state error during key generation/encryption: " + e.getMessage(), e); // Log specific error
                        errorMessage = "Failed to start secure chat: Encryption error during key exchange.";
                        return; // Stop task
                    }

                    // If crypto operations succeeded, proceed to save wrapped keys to Firebase and local decrypted key
                    Log.d(TAG, "Encryption successful. Preparing to save NEW key to Firebase and local Room/KeyManager.");

                    String encryptedAesKeyForRecipientBase64 = CryptoUtils.bytesToBase64(encryptedAesKeyForRecipient); // Use CryptoUtils for encoding to Base64 for Firebase
                    String encryptedAesKeyForCurrentUserBase64 = CryptoUtils.bytesToBase64(encryptedAesKeyForCurrentUser); // Use CryptoUtils for encoding to Base64 for Firebase

                    // 5. Capture the timestamp for this new key version (for Firebase storage)
                    // Using a local timestamp as the Firebase key name. Firebase ServerValue.TIMESTAMP is for the *value*.
                    final long newKeyTimestamp = System.currentTimeMillis(); // Capture local time for the key name


                    // 6. Prepare data to save to Realtime Database under ConversationKeys/{convId}/key_versions/{newKeyTimestamp}
                    Map<String, Object> keyVersionData = new HashMap<>();
                    keyVersionData.put(currentUserID, encryptedAesKeyForCurrentUserBase64); // Key encrypted for the initiator
                    keyVersionData.put(recipientUserId, encryptedAesKeyForRecipientBase64); // Key encrypted for the recipient
                    keyVersionData.put("generatedBy", currentUserID); // Optional metadata
                    keyVersionData.put("timestamp", ServerValue.TIMESTAMP); // Keep ServerValue.TIMESTAMP as a value for ordering/metadata


                    // Create the Firebase reference using the conversationId and the captured timestamp as the key name
                    DatabaseReference newKeyVersionRef = rootRef.child("ConversationKeys").child(conversationId).child("key_versions").child(String.valueOf(newKeyTimestamp)); // <<< Set timestamp as key name


                    // Make final copies of variables needed in the listener lambda
                    final String finalCurrentUserId = currentUserID;
                    final String finalConversationId = conversationId; // Use the method parameter directly
                    final SecretKey finalConversationAESKey = conversationAESKey; // The actual decrypted key
                    final byte[] finalConversationAESKeyBytes = conversationAESKeyBytes; // The raw bytes of the actual key
                    final String finalRecipientUserId = recipientUserId;
                    final String finalRecipientName = recipientName;
                    final String finalRecipientImageBase64 = recipientImageBase64;
                    final String finalErrorMessageAfterCrypto = errorMessage; // Capture error message from crypto steps


                    // 7. Save the encrypted key version to Realtime Database (Async operation)
                    newKeyVersionRef.setValue(keyVersionData)
                            .addOnCompleteListener(task -> {
                                // This callback usually runs on the main thread after Firebase finishes the async operation
                                chatOpenHandler.post(() -> { // Ensure all UI updates and navigation are explicitly on main thread
                                    // Dismiss progress dialog is handled at the end of the post block

                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "NEW Conversation key version saved successfully to Firebase for " + finalConversationId + " at path: " + newKeyVersionRef.getPath());

                                        // --- Success: Keys Saved to Firebase ---

                                        // Store the decrypted conversationAESKey in memory for this chat session
                                        // The key (finalConversationAESKey) is available from the crypto ops above.
                                        // This key will be used immediately by ChatPageActivity.
                                        YourKeyManager.getInstance().setConversationKey(finalConversationId, finalConversationAESKey); // <<< Use the SINGLE-KEY KeyManager method
                                        Log.d(TAG, "Decrypted AES key loaded into KeyManager for conversation " + finalConversationId + " after generation. Total cached conv keys: " + YourKeyManager.getInstance().getConversationKeys().size());


                                        // *** Save the decrypted conversation key locally to Room DB for persistence ***
                                        // This will REPLACE any previous key for this conversation due to the single-key primary key.
                                        // Run this Room operation on the databaseExecutor (background thread).
                                        Log.d(TAG, "Saving NEW decrypted key to Room DB for conv ID: " + finalConversationId);
                                        // Convert decrypted key bytes (finalConversationAESKeyBytes) to Base64 string for Room storage.
                                        String decryptedKeyBase64 = CryptoUtils.bytesToBase64(finalConversationAESKeyBytes); // Use CryptoUtils.bytesToBase64 for Room storage format

                                        // *** Use the SINGLE-KEY ConversationKeyEntity constructor and DAO method ***
                                        ConversationKeyEntity keyEntity = new ConversationKeyEntity(finalCurrentUserId, finalConversationId, decryptedKeyBase64); // <<< Use SINGLE-KEY constructor (no timestamp needed for PK)
                                        if (databaseExecutor != null && conversationKeyDao != null) { // Use the shared database executor
                                            databaseExecutor.execute(() -> {
                                                try {
                                                    conversationKeyDao.insertOrUpdateKey(keyEntity); // Insert or update based on primary key (owner, conv)
                                                    Log.d(TAG, "Executor - DB: NEW Decrypted conversation key saved to Room DB for owner " + finalCurrentUserId + ", conversation " + finalConversationId + " (Single Key).");
                                                } catch (Exception saveEx) {
                                                    Log.e(TAG, "Executor - DB: Error saving NEW decrypted conversation key to Room DB", saveEx);
                                                    // Log error, don't show toast, chat will still work for this session from KeyManager
                                                }
                                            });
                                        } else {
                                            Log.w(TAG, "generateAndSaveConversationKeysAsync (Main Thread): Cannot save generated key to Room, databaseExecutor or DAO is null.");
                                        }


                                        // 8. Send a "Security Code Changed" system message to the recipient
                                        // This informs the recipient that a key change has occurred.
                                        // The recipient's ChatPageActivity or message handling logic needs to display this message type.
                                        sendSystemMessageKeyChanged(finalConversationId, finalCurrentUserId, finalRecipientUserId); // <<< ADD THIS CALL

                                        // NOW, navigate to the actual Chat Activity
                                        navigateToChatActivity(finalConversationId, finalRecipientUserId, finalRecipientName, finalRecipientImageBase64); // <-- Correct variable name

                                    } else {
                                        // Firebase save failed
                                        Log.e(TAG, "Failed to save NEW conversation key version to Firebase for " + finalConversationId, task.getException());
                                        String displayMessage = "Failed to start secure chat: Could not save keys.";
                                        if (task.getException() != null) {
                                            displayMessage += " " + task.getException().getMessage();
                                        }
                                        Toast.makeText(getContext(), displayMessage, Toast.LENGTH_LONG).show();
                                    }

                                    // Dismiss the progress dialog at the end of the main thread post block
                                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing())
                                        chatOpenProgressDialog.dismiss();
                                });
                            }); // End of addOnCompleteListener for Firebase save

                    // If an error occurred during crypto steps (before Firebase save was attempted)
                    if (finalErrorMessageAfterCrypto != null) {
                        chatOpenHandler.post(() -> { // Ensure UI update is on main thread
                            Log.e(TAG, "generateAndSaveConversationKeysAsync (Executor): Crypto error occurred before Firebase save. Posting error toast.");
                            // Dismiss progress dialog handled below
                            Toast.makeText(getContext(), finalErrorMessageAfterCrypto, Toast.LENGTH_LONG).show();

                            // Dismiss the progress dialog even on error
                            if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing())
                                chatOpenProgressDialog.dismiss();
                        });
                    }
                }); // End of chatOpenExecutor.execute() for crypto/save Firebase
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // This callback runs on the main thread
                Log.e(TAG, "Failed to fetch recipient public key for NEW key generation (Firebase error)", error.toException());
                chatOpenHandler.post(() -> { // Ensure dismissal and UI updates are on main thread
                    if (chatOpenProgressDialog != null && chatOpenProgressDialog.isShowing()) chatOpenProgressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to start secure chat: Could not fetch recipient data.", Toast.LENGTH_SHORT).show();
                });
            }
        }); // End of Recipient User Data Listener (addListenerForSingleValueEvent)
    }


    // --- NEW Helper Method to Send System Message (Key Changed) ---
    // This method sends a special message to the recipient indicating that the sender's security code has changed.
    // This is crucial for the recipient's UI (e.g., showing a warning) and for their key management logic
    // if they needed to re-fetch a key after the sender's reset.
    // This method assumes it's called *after* a NEW key is successfully generated and saved by the SENDER.
    private void sendSystemMessageKeyChanged(String conversationId, String senderId, String recipientId) {
        if (TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(senderId) || TextUtils.isEmpty(recipientId) || rootRef == null || databaseExecutor == null || messageDao == null) {
            Log.e(TAG, "Cannot send system message key changed: Missing IDs or DB components.");
            return;
        }
        Log.d(TAG, "Sending system message 'Security code changed' for conv " + conversationId + " from " + senderId + " to " + recipientId);

        String messageContent = "Security code changed."; // The content of the system message
        String messageType = "system_key_change"; // Special type for system messages


        String sendTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()); // Formatted local time
        Object firebaseTimestamp = ServerValue.TIMESTAMP; // Firebase ServerValue
        long localTimestamp = System.currentTimeMillis(); // Local timestamp for initial Room order

        DatabaseReference messagesRef = rootRef.child("Messages").child(conversationId).push();
        String messagePushId = messagesRef.getKey();

        if (messagePushId == null) {
            Log.e(TAG, "Firebase push key generation failed for system message. Cannot send.");
            return;
        }

        // Create MessageEntity for Room (store the plain text content for system messages)
        MessageEntity messageToSaveLocally = new MessageEntity();
        messageToSaveLocally.setFirebaseMessageId(messagePushId);
        messageToSaveLocally.setOwnerUserId(senderId); // Owner is the sender (current user)
        messageToSaveLocally.setMessage(messageContent); // Store plain text content for system message
        messageToSaveLocally.setType(messageType);
        messageToSaveLocally.setFrom(senderId); // Sender is the current user
        messageToSaveLocally.setTo(recipientId); // Recipient
        messageToSaveLocally.setSendTime(sendTime);
        messageToSaveLocally.setSeen(false);
        messageToSaveLocally.setSeenTime("");
        messageToSaveLocally.setStatus("sent"); // System messages are typically considered sent immediately by the sender
        messageToSaveLocally.setTimestamp(localTimestamp);


        // Insert into Room DB first (runs on background thread)
        databaseExecutor.execute(() -> {
            try {
                // System message inserted for the sender's view
                messageDao.insertMessage(messageToSaveLocally);
                Log.d(TAG, "Inserted system message into Room (owner " + senderId + "): " + messagePushId);
            } catch (Exception e) {
                Log.e(TAG, "Error inserting system message into Room: " + messagePushId, e);
                // Log error, but proceed to send to Firebase
            }

            // Send message to Firebase (runs on background thread)
            DatabaseReference messagePathRef = rootRef.child("Messages").child(conversationId).child(messagePushId);

            Map<String, Object> messageFirebaseBody = new HashMap<>();
            messageFirebaseBody.put("message", messageContent); // Send the plain text content
            messageFirebaseBody.put("type", messageType);
            messageFirebaseBody.put("from", senderId);
            messageFirebaseBody.put("to", recipientId);
            messageFirebaseBody.put("seen", false); // System messages might be considered "seen" upon reception/display? Depends on logic.
            messageFirebaseBody.put("seenTime", ""); // Or current time if considered seen on receipt
            messageFirebaseBody.put("sendTime", sendTime);
            messageFirebaseBody.put("timestamp", firebaseTimestamp);


            messagePathRef.setValue(messageFirebaseBody).addOnCompleteListener(task -> {
                Log.d(TAG, "Firebase setValue for system message " + messagePushId + " completed. Success: " + task.isSuccessful());

                if (task.isSuccessful()) {
                    // --- Update Chat Summaries for BOTH users ---
                    // Use the plain text content for the summary preview
                    updateChatSummaryForUser(
                            senderId, // Owner 1 (Sender)
                            recipientId, // Partner 1 (Recipient)
                            conversationId,
                            messagePushId,
                            messageContent, // Use plain text preview for system message
                            messageType,
                            firebaseTimestamp,
                            senderId // Sender of THIS message
                    );

                    updateChatSummaryForUser(
                            recipientId, // Owner 2 (Recipient)
                            senderId, // Partner 2 (Sender)
                            conversationId,
                            messagePushId,
                            messageContent, // Use plain text preview for system message
                            messageType,
                            firebaseTimestamp,
                            senderId // Sender of THIS message
                    );
                    // --- END Update Chat Summaries ---

                    // Send push notification for system message (optional, might annoy users)
                    // This notification text should NOT be encrypted.
                    // You might want to send a specific notification type for system messages.
                    // sendPushNotification(recipientId, "Security update", messageContent);


                } else {
                    Log.e(TAG, "Firebase setValue failed for system message " + messagePushId, task.getException());
                    // Update status in Room to "failed" for the sender's copy if Firebase fails
                    databaseExecutor.execute(() -> {
                        // Check if the message status is still 'sent' (default for system message on success) before marking failed
                        MessageEntity localCopy = messageDao.getMessageByFirebaseId(messagePushId, senderId);
                        if (localCopy != null && !"failed".equals(localCopy.getStatus())) { // Prevent double-marking failed if listener also triggers
                            messageDao.updateMessageStatus(messagePushId, "failed", senderId);
                            Log.d(TAG, "Updated status to 'failed' in Room for owner " + senderId + " for system message: " + messagePushId + " after Firebase write failure.");
                        } else {
                            Log.d(TAG, "System message status already updated or local copy not found for " + messagePushId + ", skipping Room update on Firebase failure.");
                        }
                    });
                    // No toast needed for failed system message
                }
            });
        });
    }


    // --- Helper method to Navigate to Chat Activity ---
    // This method is called upon successful key retrieval/generation for a specific chat.
    // It also handles the unread count update.
    private void navigateToChatActivity(String conversationId, String recipientUserId, String recipientName, String recipientImageBase64) {
        // Ensure context is still valid before starting activity
        if (getContext() == null) {
            Log.e(TAG, "Cannot navigate to chat, context is null.");
            return;
        }
        Log.d(TAG, "Navigating to ChatPageActivity for conversationId: " + conversationId + ", Recipient ID: " + recipientUserId);
        Intent intent = new Intent(getContext(), ChatPageActivity.class);
        intent.putExtra("conversationId", conversationId); // Pass the consistent conversation ID
        // Keeping old keys for compatibility with your ChatPageActivity if it still uses them for recipient details
        intent.putExtra("visit_users_ids", recipientUserId);
        intent.putExtra("visit_users_name", recipientName);
        intent.putExtra("visit_users_image", recipientImageBase64);
        // Add flags to ensure clean task stack if needed (e.g., if you don't want to go back to contacts easily)
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Example

        startActivity(intent); // Launch the ChatPageActivity


        // --- Update unread count to 0 in Room for this chat ---
        // This should be done immediately when the user clicks to open the chat for immediate UI feedback.
        // Marking individual messages as seen in Firebase will happen in ChatPageActivity.
        // Updating the unread count in the Firebase ChatSummary will also happen in ChatPageActivity
        // when the user views the chat.
        if (!TextUtils.isEmpty(currentUserID) && !TextUtils.isEmpty(recipientUserId) && chatDao != null && databaseExecutor != null) { // Basic validation
            // We don't need to check if count > 0 here, the DAO update method will handle it.
            // Just queue the update on the database executor.
            Log.d(TAG, "Marking unread count to 0 in Room for chat with " + recipientUserId + " for owner " + currentUserID);
            databaseExecutor.execute(() -> { // Run on DB thread
                try {
                    // Call the DAO method with the otherUserId, the new count (0), and the currentUserID (owner)
                    // Make sure updateUnreadCount takes partnerId, newCount, and ownerId
                    int updatedRows = chatDao.updateUnreadCount(recipientUserId, 0, currentUserID); // Ensure this DAO method exists
                    if (updatedRows > 0) {
                        Log.d(TAG, "Successfully updated unread count to 0 in Room for chat with " + recipientUserId);
                    } else {
                        // This might happen if the chat entry was somehow deleted from Room just before click (unlikely)
                        Log.w(TAG, "Attempted to update unread count to 0, but no chat entity found for partner " + recipientUserId + " owned by " + currentUserID);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating unread count in Room for user " + recipientUserId, e);
                }
            });
        } else {
            Log.w(TAG, "Skipping unread count update in Room during navigation: Missing IDs, DAO, or Executor.");
        }
    }


    @Override // From ChatAdapter.OnChatI
    // nteractionListener
    public void onChatLongClick(ChatEntity chat) {
        // Handles long click on a chat item (e.g., to delete)
        Log.d(TAG, "Chat long clicked: " + chat.getUsername() + " (Other User ID: " + chat.getUserId() + ")");
        // Ensure chat entity is not null and has a valid partner ID and conversation ID, and is owned by the current user
        if (chat != null && !TextUtils.isEmpty(chat.getUserId()) && !TextUtils.isEmpty(chat.getConversationId()) && !TextUtils.isEmpty(chat.getOwnerUserId()) && chat.getOwnerUserId().equals(currentUserID)) { // Added conv ID check and owner check
            // *** MODIFIED: Call the new options dialog method ***
            showChatAndReportOptionsDialog(chat); // Call method to show multiple options // Call method to show the confirmation dialog
        } else {
            Log.w(TAG, "Cannot show delete dialog, ChatEntity is null or has empty userId/conversationId/ownerId, or not owned by current user.");
            Toast.makeText(getContext(), "Error deleting chat.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Helper Methods ---

    // Method to check network availability (Kept from your code)
    private boolean isNetworkAvailable() {
        if (getContext() == null) return false; // Return false if context is null
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            try { // Add try-catch for security exceptions on some Android versions
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException checking network state, ensure ACCESS_NETWORK_STATE permission is granted.", e);
                // In production, you might inform the user or assume offline
                return false; // Cannot confirm network state
            } catch (Exception e) {
                Log.e(TAG, "Error checking network state", e);
                return false;
            }
        }
        return false;
    }

    // Method to show delete chat confirmation dialog (Keep this)
    private void showDeleteChatDialog(ChatEntity chat) {
        // Ensure context is available and chat object is valid and owned by current user before showing dialog
        if (getContext() == null || chat == null || TextUtils.isEmpty(chat.getUserId()) || TextUtils.isEmpty(chat.getConversationId()) || TextUtils.isEmpty(chat.getOwnerUserId()) || !chat.getOwnerUserId().equals(currentUserID)) {
            Log.w(TAG, "Cannot show delete chat dialog, ChatEntity invalid or not owned by current user.");
            Toast.makeText(getContext(), "Error deleting chat.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Chat")
                // Clarify that this only deletes from *this* user's list in Room and Firebase Summary
                .setMessage("Are you sure you want to delete this chat with " + chat.getUsername() + "? This will remove it from your chat list and clear your local message history and keys for this conversation.")
                // Note: This does NOT delete messages for the other user unless you implement "Delete for Everyone" logic here too.
                .setPositiveButton("Delete", (dialog, which) -> deleteChat(chat.getUserId(), chat.getConversationId())) // <<< MODIFIED: Pass conversationId too
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Method to delete chat entry from Room DB for the current user's list
    // and also delete the corresponding summary entry from Firebase.
    // Also delete messages and conversation key from Room.
    // Pass conversationId to this method.
    private void deleteChat(String userIdToDelete, String conversationIdToDelete) { // userIdToDelete is the chat partner's ID

        if (currentUserID == null) {
            Log.e(TAG, "Cannot delete chat, currentUserID is null.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Could not delete chat. User not logged in?", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        // Ensure the partner ID and conversation ID are valid, and Room DB and Firebase are initialized
        if (TextUtils.isEmpty(userIdToDelete) || TextUtils.isEmpty(conversationIdToDelete) || rootRef == null || chatDatabase == null || chatDao == null || messageDao == null || conversationKeyDao == null || databaseExecutor == null) { // Check all required components
            Log.e(TAG, "Cannot delete chat, essential components are null or empty.");
            Toast.makeText(getContext(), "Error deleting chat.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting to delete chat entry, messages, and key from Room and Firebase summary for partner: " + userIdToDelete + ", conv: " + conversationIdToDelete + " for owner: " + currentUserID);

        // --- First, attempt to delete the summary entry from Firebase for the current user ---
        // The path is ChatSummaries/{currentUserID}/{chatPartnerId}
        DatabaseReference firebaseSummaryRef = rootRef.child("ChatSummaries").child(currentUserID).child(userIdToDelete);
        firebaseSummaryRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Successfully deleted chat summary from Firebase for partner: " + userIdToDelete + " owner: " + currentUserID);

                // --- Now, delete the corresponding entries from Room DB on a background thread ---
                // This includes the ChatEntity, all messages for this chat pair, and the conversation key.
                databaseExecutor.execute(() -> { // Run on Room DB thread
                    try {
                        // 1. Delete the ChatEntity for this partner, owned by the current user
                        // Make sure this DAO method exists and deletes WHERE ownerUserId = :ownerId AND userId = :userIdToDelete
                        int deletedSummaryRows = chatDao.deleteChatByUserId(userIdToDelete, currentUserID); // Assuming this method exists
                        if (deletedSummaryRows > 0) {
                            Log.d(TAG, "Successfully deleted chat list entry from Room for partner: " + userIdToDelete + " owned by: " + currentUserID);
                        } else {
                            Log.w(TAG, "Attempted to delete chat list entry from Room for partner " + userIdToDelete + ", but it wasn't found for owner " + currentUserID);
                        }

                        // 2. Delete the related messages from Room for this conversation for THIS owner
                        try {
                            // Assuming MessageDao has deleteAllMessagesForChat(ownerId, conversationId)
                            // Use the conversationIdToDelete received by the method.
                            int deletedMessageRows = messageDao.deleteAllMessagesForChat(currentUserID, conversationIdToDelete); // Assuming a DAO method that takes owner and convId
                            Log.d(TAG, "Successfully deleted " + deletedMessageRows + " messages from Room for conversation " + conversationIdToDelete + " for owner " + currentUserID + " during chat deletion.");
                        } catch (Exception msgDeleteEx) {
                            Log.e(TAG, "Error deleting messages from Room during chat deletion for conv " + conversationIdToDelete + " for owner " + currentUserID + ".", msgDeleteEx);
                        }

                        // *** NEW ADDITION START ***
                        // 3. Delete the conversation key from Room for this chat for the current user
                        try {
                            // <<< Use the single-key DAO method >>>
                            // Assuming ConversationKeyDao has deleteKeyForConversation(ownerId, conversationId)
                            // Use the conversationIdToDelete received by the method. Access the new DAO via chatDatabase instance.
                            int deletedKeyRows = conversationKeyDao.deleteKeyForConversation(currentUserID, conversationIdToDelete); // Use the single-key DAO method name
                            if (deletedKeyRows > 0) {
                                Log.d(TAG, "Deleted " + deletedKeyRows + " conversation key from Room during chat deletion for conv " + conversationIdToDelete + " for owner " + currentUserID + ".");
                                // Optional: Remove from in-memory KeyManager if it was loaded and is still there
                                YourKeyManager.getInstance().removeConversationKey(conversationIdToDelete); // Removes the single key from cache
                            } else {
                                Log.w(TAG, "Attempted to delete conversation key but no entries found in Room during chat deletion for conv " + conversationIdToDelete);
                            }
                        } catch (Exception keyDeleteEx) {
                            Log.e(TAG, "Error deleting conversation key from Room during chat deletion for conv " + conversationIdToDelete + " for owner " + currentUserID + ".", keyDeleteEx);
                        }
                        // *** NEW ADDITION END ***


                        // Show success toast on the UI thread after local deletion is attempted
                        // Use getActivity() != null and !getActivity().isFinishing() for safety in fragments
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Chat cleared.", Toast.LENGTH_SHORT).show() // Changed toast text
                            );
                        }

                    } catch (Exception e) {
                        // This catch block primarily handles errors in deleting ChatEntity itself
                        Log.e(TAG, "Error deleting chat list entry from Room for partner: " + userIdToDelete + " owned by: " + currentUserID, e);
                        // Show local deletion error toast on the UI thread
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Failed to clear chat locally", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                }); // End Room deletion executor

            } else {
                // Firebase deletion failed. Log the error and inform the user.
                Log.e(TAG, "Failed to delete chat summary from Firebase for partner: " + userIdToDelete + " owner: " + currentUserID, task.getException());
                // Show error toast on the UI thread
                if (getActivity() != null && !getActivity().isFinishing()) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to delete chat from server.", Toast.LENGTH_SHORT).show()
                    );
                }
                // Decide if you still want to delete from Room even if Firebase fails? Probably not, for consistency.
            }
        }); // End Firebase deletion listene
    }
    public void forceRefreshDisplay() {
        Log.d(TAG, "🟢 forceRefreshDisplay() called in ChatFragment.");
        // Ensure LiveData has a value and adapter exists
        if (chatListLiveData == null || chatListLiveData.getValue() == null || chatAdapter == null || currentUserID == null) {
            Log.w(TAG, "forceRefreshDisplay skipped: LiveData, value, adapter, or currentUserID is null.");
            // Ensure UI reflects locked state if user ID is null
            if (currentUserID == null) {
                showAuthenticationErrorUI();
            }
            Log.d(TAG, "✅ forceRefreshDisplay finished (skipped due to nulls).");
            return;
        }

        List<ChatEntity> currentItems = chatListLiveData.getValue();
        Log.d(TAG, "forceRefreshDisplay triggered. Re-processing " + (currentItems != null ? currentItems.size() : 0) + " ChatEntity objects from Room for owner " + currentUserID);

        List<ChatEntity> processedChatList = new ArrayList<>();
        boolean isPrivateKeyAvailable = YourKeyManager.getInstance().isPrivateKeyAvailable(); // Check user's main private key state


        if (currentItems != null) {
            for (ChatEntity chat : currentItems) {
                if (chat == null) continue;

                // Extract necessary fields from the original ChatEntity
                String storedMessageContentFromRoom = chat.getLastMessage(); // This is the content from Room (could be plaintext, encrypted Base64, or placeholder)
                String conversationId = chat.getConversationId();
                String chatPartnerId = chat.getUserId(); // Get chat partner ID for logging
                String lastMessageType = chat.getLastMessageType();
                // REMOVED: String firebaseMessageId = chat.getLastMessageId(); // This method does not exist in ChatEntity

                String displayedContent = ""; // This will be the string shown in the UI bubble preview
                String processingOutcome = "Default/Placeholder"; // For logging outcome


                // Determine if the message type is one we expect to be encrypted (text or image)
                boolean isExpectedEncryptedType = ("text".equals(lastMessageType) || "image".equals(lastMessageType));

                // Define known specific placeholder strings that the Worker or ChatPage might save
                boolean isKnownSpecificPlaceholder = !TextUtils.isEmpty(storedMessageContentFromRoom) && (
                        "[New Message]".equals(storedMessageContentFromRoom) ||
                                "[Image]".equals(storedMessageContentFromRoom) ||
                                "[File]".equals(storedMessageContentFromRoom) ||
                                "[Locked]".equals(storedMessageContentFromRoom) ||
                                "[Tap to Load Chat]".equals(storedMessageContentFromRoom) ||
                                "[Encrypted Message - Failed]".equals(storedMessageContentFromRoom) ||
                                "[Invalid Encrypted Data]".equals(storedMessageContentFromRoom)
                );


                // --- Logic to determine what to display for the last message preview ---

                // 1. Handle System messages - their content is already plaintext (from Worker/ChatPage logic)
                if ("system_key_change".equals(lastMessageType)) {
                    displayedContent = storedMessageContentFromRoom; // Use the content directly from Room
                    processingOutcome = "System Message";
                    if (TextUtils.isEmpty(displayedContent)) displayedContent = "[System Message]"; // Fallback if content is empty

                }
                // 2. Handle known Specific Placeholders - display them directly without any decryption attempt
                else if (isKnownSpecificPlaceholder) {
                    displayedContent = storedMessageContentFromRoom; // Display the placeholder string as is
                    processingOutcome = "Known Specific Placeholder";

                }
                // 3. Attempt Decryption IF it's an expected encrypted type AND content exists AND keys are available
                // AND it's NOT a known placeholder (handled above).
                else if (isExpectedEncryptedType && !TextUtils.isEmpty(storedMessageContentFromRoom) && isPrivateKeyAvailable && YourKeyManager.getInstance().hasConversationKey(conversationId) /* Check conversation key available explicitly */) { // *** Check Conversation Key Here ***

                    processingOutcome = "Attempting Decryption (Keys Available)";
                    // Log.d(TAG, "Preview Debug: Attempting decryption for conv ID: " + conversationId + ", Partner: " + chatPartnerId); // Debug log


                    // Get the *single* conversation key from KeyManager cache
                    SecretKey conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationId); // Get key from cache


                    // Double-check key is not null (should be true if hasConversationKey was true)
                    if (conversationAESKey == null) {
                        Log.e(TAG, "Preview Debug: Conversation key became null unexpectedly during decryption attempt for conv " + conversationId + ". Should not happen if hasConversationKey was true.");
                        displayedContent = "[Decryption Error - Key Lost]";
                        processingOutcome = "Decryption Error (Key Lost)";
                    } else {
                        try {
                            // Decode Base64 string from Room to bytes using CryptoUtils
                            byte[] encryptedBytesWithIV = CryptoUtils.base64ToBytes(storedMessageContentFromRoom);

                            if (encryptedBytesWithIV == null || encryptedBytesWithIV.length == 0) {
                                Log.w(TAG, "Preview Debug: Decoded encrypted bytes null or empty for preview in conv " + conversationId);
                                displayedContent = (lastMessageType.equals("image") ? "[Invalid Encrypted Image Data]" : "[Invalid Encrypted Data]");
                                processingOutcome = "Decoded Empty";

                            } else {
                                // Decrypt bytes using the conversation key
                                String decryptedContent = CryptoUtils.decryptMessageWithAES(encryptedBytesWithIV, conversationAESKey);

                                // If decryption succeeds:
                                if ("text".equals(lastMessageType)) {
                                    int maxLength = 50; // Preview length limit
                                    displayedContent = decryptedContent.length() > maxLength ?
                                            decryptedContent.substring(0, maxLength) + "..." :
                                            decryptedContent; // Use decrypted text or snippet
                                    // Log.d(TAG, "Preview Debug: Decrypted text preview: " + displayedContent); // Avoid logging content
                                    processingOutcome = "Decrypted Success (Text)";

                                } else if ("image".equals(lastMessageType)) {
                                    // For image previews, show a placeholder even after successful decryption in the list
                                    displayedContent = "[Image]"; // Placeholder for images in list
                                    processingOutcome = "Decrypted Success (Image)";
                                } else { // Should not happen based on outer if condition, but fallback
                                    displayedContent = "[Unknown Type - Decrypted]";
                                    processingOutcome = "Decrypted Success (Unknown Type)";
                                }
                                // Note: You could potentially store the decrypted image Base64 here if your adapter
                                // supports displaying images directly from the chat list item preview string,
                                // but placeholders are more common for performance/UI consistency.
                            }

                        } catch (IllegalArgumentException e) { // Base64 decoding error or invalid bytes format
                            Log.e(TAG, "Preview Debug: Base64 decoding error decrypting preview for conv " + conversationId, e);
                            displayedContent = "[Invalid Encrypted Data]"; // Placeholder on Base64 error
                            processingOutcome = "Base64 Decoding Error";
                        } catch (BadPaddingException |
                                 IllegalBlockSizeException |
                                 InvalidKeyException |// Catch InvalidKeyException if the key is wrong
                                 InvalidAlgorithmParameterException e) {
                            // Crypto decryption failed for THIS key! (e.g., wrong key version if multiple existed, or data corruption)
                            // In the single-key model, this means the only key we have is wrong or data is corrupt.
                            Log.w(TAG, "Preview Debug: Decryption FAILED with conversation key for conv " + conversationId + ". Exception: " + e.getClass().getSimpleName());
                            displayedContent = "[Encrypted Message - Failed]"; // Placeholder on crypto error with available key
                            processingOutcome = "Decryption Failed (Crypto Error)";
                        } catch (Exception e) { // Catch any other unexpected errors
                            Log.e(TAG, "Preview Debug: Unexpected error during decryption for conv " + conversationId, e);
                            displayedContent = "[Encrypted Message - Failed]"; // Fallback placeholder
                            processingOutcome = "Decryption Failed (Unexpected Error)";
                        }
                    } // End else (conversationAESKey != null)
                }
                // 4. If decryption was NOT attempted because keys were unavailable
                else if (isExpectedEncryptedType && !TextUtils.isEmpty(storedMessageContentFromRoom) && (!isPrivateKeyAvailable || !YourKeyManager.getInstance().hasConversationKey(conversationId))) { // *** Check Conversation Key Here ***
                    // Content exists and should be encrypted, but keys are missing/unavailable
                    // This is the case where PrivateKey is null OR ConversationKey is null in KeyManager
                    displayedContent = "[Locked]"; // Placeholder indicating account needs unlocking or keys need loading
                    processingOutcome = "Keys Unavailable";

                }
                // 5. Fallback for any other case:
                //    - Not an expected encrypted type (e.g., "file")
                //    - Message content from Room is empty
                //    - Any other scenario not covered above, including unexpected data.
                else {
                    // Display the content as it was stored in Room.
                    displayedContent = storedMessageContentFromRoom;
                    processingOutcome = "Fallback Display";
                    if (TextUtils.isEmpty(displayedContent)) {
                        // Fallback to a type-based placeholder if stored content is empty
                        if ("image".equals(lastMessageType)) displayedContent = "[Image]";
                        else if ("file".equals(lastMessageType)) displayedContent = "[File]";
                        else if ("system_key_change".equals(lastMessageType)) displayedContent = "[System Message]";
                        else displayedContent = "[Message]"; // Generic placeholder
                        processingOutcome += " (Content Empty, Used Type Placeholder)";
                    } else {
                        // If content is not empty but didn't match encrypted types or placeholders, it's displayed as is.
                        // This covers unexpected plain text or other data types.
                        processingOutcome += " (Displayed as is)";
                    }
                }
                // --- End Logic ---


                // Create a NEW ChatEntity object for the adapter with processed content
                ChatEntity processedChat = new ChatEntity();
                // Copy all fields from the original chat entity, but set the processed message
                processedChat.setId(chat.getId()); // Make sure to copy Room auto-generated ID (if used)
                processedChat.setOwnerUserId(chat.getOwnerUserId()); // Keep the owner ID
                processedChat.setUserId(chat.getUserId()); // Keep the chat partner ID
                processedChat.setConversationId(chat.getConversationId()); // Keep the conversation ID
                processedChat.setUsername(chat.getUsername()); // Keep the username
                processedChat.setProfileImage(chat.getProfileImage()); // Keep the profile image
                processedChat.setLastMessage(displayedContent); // *** Set the PROCESSED/DECRYPTED/PLACEHOLDER content ***
                processedChat.setTimestamp(chat.getTimestamp()); // Keep the timestamp
                processedChat.setUnreadCount(chat.getUnreadCount()); // Keep the unread count
                processedChat.setLastMessageType(chat.getLastMessageType()); // Keep the original type
                // If ChatEntity has partnerKeysChanged, copy it:
                // processedChat.setPartnerKeysChanged(chat.isPartnerKeysChanged());


                processedChatList.add(processedChat); // Add the processed entity to the list for the adapter

                // Log the final outcome for this message after processing
                Log.d(TAG, "Processed chat preview for conv " + conversationId + ", Partner: " + chatPartnerId + ". Outcome: " + processingOutcome + ", Displayed: '" + (displayedContent != null && displayedContent.length() > 5 ? displayedContent.substring(0, Math.min(displayedContent.length(), 50)) + (displayedContent.length() > 50 ? "..." : "") : displayedContent) + "'"); // Add null check for displayedContent and limit log length


            } // End of for loop
        } // End of if (currentItems != null && !currentItems.isEmpty()) check


        // Sort the list by timestamp DESC (latest message first). Ensure this matches your DAO query ORDER BY.
        // If your DAO query already sorts correctly, this step is redundant but safe.
        if (processedChatList.size() > 1) { // Only sort if more than one item
            Collections.sort(processedChatList, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
        }


        // Submit the list of ChatEntity with processed last messages to the adapter
        if (chatAdapter != null) {
            // Use submitList (more efficient with DiffUtil if implemented in adapter). Pass a *copy*.
            // Using ArrayList<>(processedChatList) ensures a new list is passed, preventing issues with adapter's internal list reference.
            chatAdapter.submitList(new ArrayList<>(processedChatList));
            Log.d(TAG, "Submitted " + processedChatList.size() + " processed chats to adapter in forceRefreshDisplay.");
        } else {
            Log.e(TAG, "ChatAdapter is null in forceRefreshDisplay, cannot submit list.");
        }


        // Update UI visibility based on the final list size
        if (processedChatList.size() > 0) { // Check size > 0 for non-empty list
            if (privateChatsList != null) privateChatsList.setVisibility(View.VISIBLE); // Corrected RecyclerView variable name
            if (noChatsText != null) noChatsText.setVisibility(View.GONE); // Safety check
            // Show search view only if there are chats to search
            if (searchView != null) searchView.setVisibility(View.VISIBLE); // Safety check
            // Log.d(TAG, "Chat list visible after forceRefreshDisplay.");
        } else {
            if (privateChatsList != null) privateChatsList.setVisibility(View.GONE); // Corrected RecyclerView variable name
            if (noChatsText != null) { // Safety check
                noChatsText.setVisibility(View.VISIBLE);
                noChatsText.setText("No chats yet"); // Message when list is empty
            }
            // Hide search view if there are no chats
            if (searchView != null) searchView.setVisibility(View.GONE); // Safety check
            // Log.d(TAG, "No chats found after forceRefreshDisplay, showing No chats text.");
        }

        // Re-apply search filter if there's an active query
        // The adapter's filter method should work on the list containing *displayed* previews
        if (searchView != null && chatAdapter != null) {
            String currentQuery = searchView.getQuery().toString();
            if (!TextUtils.isEmpty(currentQuery)) { // Check if query is not empty
                // Log.d(TAG, "Applying search filter after force refresh: '" + currentQuery + "'");
                // Ensure adapter's filter method works with the list containing processed previews
                // The adapter's filter logic should compare the *displayed* last message preview text
                chatAdapter.filter(currentQuery);
            }
        } else {
            Log.w(TAG, "SearchView or ChatAdapter is null in forceRefreshDisplay, cannot re-apply filter.");
        }
        Log.d(TAG, "✅ forceRefreshDisplay finished.");
    }

    // Helper method to update chat summary for a specific user (sender or receiver)
    // This method is used when sending a message (including the system key change message).
    @SuppressLint("RestrictedApi") // Keep if needed
    private void updateChatSummaryForUser(String summaryOwnerId, String chatPartnerId,
                                          String conversationId, String messagePushId,
                                          String lastMessageContentPreview, String messageType, // Use lastMessageContentPreview (Base64 or placeholder or plaintext)
                                          Object firebaseTimestamp, String lastMessageSenderId) {
        if (TextUtils.isEmpty(summaryOwnerId) || TextUtils.isEmpty(chatPartnerId) || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messagePushId) || TextUtils.isEmpty(messageType) || firebaseTimestamp == null || TextUtils.isEmpty(lastMessageSenderId)) {
            Log.e(TAG, "Cannot update chat summary: Missing essential input data.");
            return;
        }

        DatabaseReference summaryRef = rootRef.child("ChatSummaries").child(summaryOwnerId).child(chatPartnerId);
        Log.d(TAG, "Updating summary for owner " + summaryOwnerId + " with partner " + chatPartnerId + " at path: " + summaryRef.getPath());


        Map<String, Object> summaryUpdates = new HashMap<>();
        summaryUpdates.put("conversationId", conversationId);
        summaryUpdates.put("lastMessageId", messagePushId);
        summaryUpdates.put("lastMessageContentPreview", lastMessageContentPreview != null ? lastMessageContentPreview : ""); // Store the preview content (Base64 or placeholder/plaintext)
        summaryUpdates.put("lastMessageTimestamp", firebaseTimestamp);
        summaryUpdates.put("lastMessageSenderId", lastMessageSenderId);
        summaryUpdates.put("lastMessageType", messageType);

        // Ensure participants are recorded if they weren't already
        summaryUpdates.put("participants/" + summaryOwnerId, true);
        summaryUpdates.put("participants/" + chatPartnerId, true);


        if (summaryOwnerId.equals(lastMessageSenderId)) {
            // For the sender, set unread count to 0
            Log.d(TAG, "Setting unread count to 0 for sender (" + summaryOwnerId + ") in their summary.");
            summaryUpdates.put("unreadCounts/" + summaryOwnerId, 0);

            summaryRef.updateChildren(summaryUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) { Log.d(TAG, "Sender's chat summary updated successfully for " + conversationId + " (Owner: " + summaryOwnerId + ")"); }
                else { Log.e(TAG, "Failed to update sender's chat summary for " + conversationId + " (Owner: " + summaryOwnerId + ")", task.getException()); }
            });

        } else {
            // For the receiver, increment unread count
            Log.d(TAG, "Attempting to increment unread count for receiver (" + summaryOwnerId + ") in summary " + conversationId);
            summaryRef.child("unreadCounts").child(summaryOwnerId).runTransaction(new Transaction.Handler() {
                @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer currentCount = currentData.getValue(Integer.class);
                    if (currentCount == null) { currentData.setValue(1); } else { currentData.setValue(currentCount + 1); }
                    return Transaction.success(currentData);
                }
                @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (error != null) { Log.e(TAG, "Firebase transaction for unread count failed for receiver " + summaryOwnerId + ": " + error.getMessage()); }
                    else if (committed) { Log.d(TAG, "Unread count incremented for receiver " + summaryOwnerId + " in summary " + conversationId); }
                    else { Log.d(TAG, "Firebase transaction for unread count not committed for receiver " + summaryOwnerId + "."); }
                    // Update other summary fields after the transaction completes
                    summaryRef.updateChildren(summaryUpdates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) { Log.d(TAG, "Receiver's chat summary updated successfully for " + conversationId + " (Owner: " + summaryOwnerId + ")"); }
                        else { Log.e(TAG, "Failed to update receiver's chat summary for " + conversationId + " (Owner: " + summaryOwnerId + ")", task.getException()); }
                    });
                }
            });
        }
    }





    // --- Method to Show Chat and Report Options Dialog on Long Press ---
    private void showChatAndReportOptionsDialog(ChatEntity chat) {
        // Ensure context is available and chat object is valid and owned by current user before showing dialog
        if (getContext() == null || chat == null || TextUtils.isEmpty(chat.getUserId()) || TextUtils.isEmpty(chat.getConversationId()) || TextUtils.isEmpty(chat.getOwnerUserId()) || !chat.getOwnerUserId().equals(currentUserID)) {
            Log.w(TAG, "Cannot show chat options dialog, ChatEntity invalid or not owned by current user.");
            Toast.makeText(getContext(), "Error performing action.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Showing chat options dialog for chat with " + chat.getUsername() + " (Partner ID: " + chat.getUserId() + ")");

        final CharSequence[] options = {"Report " + chat.getUsername(), "Delete Chat", "View Profile"}; // Options array

                final ChatEntity chatEntityForDialog = chat;

        new AlertDialog.Builder(getContext()) // Use getContext()
                .setTitle("Chat Options") // Dialog title
                // Define the list of options in the dialog
                .setItems(options, (dialog, which) -> {
                    // Handle option clicks based on the selected index (which)
                    switch (which) {
                        case 0: // Index 0: "Report User" option clicked
                            Log.d(TAG, "Report User option clicked for user: " + chat.getUserId());
                            // *** Call the confirmation dialog for reporting ***
                            showReportConfirmationDialog(chat);
                            break;
                        case 1: // Index 1: "Delete Chat" option clicked
                            Log.d(TAG, "Delete Chat option clicked for chat with: " + chat.getUserId());
                            // *** Call the existing delete logic ***
//                            deleteChat(chat.getUserId(), chat.getConversationId()); // Pass partner ID and conversation ID
                            showDeleteChatDialog(chatEntityForDialog);
                            break;
                        case 2:
//                            Intent profileIntent = new Intent(ChatFragment.this, ProfileUserInfoActivity.class);
//                            // Pass the receiver's user ID using the key expected by ProfileUserInfoActivity
//                            profileIntent.putExtra("visit_user_id", messageReceiverID);
//                            startActivity(profileIntent);
//                            break;
                        // Add more cases for other options if needed
                        default:
                            throw new IllegalStateException("Unexpected value: " + which);
                    }
                })
                // No NegativeButton needed if using setItems, clicking outside dismisses it.
                .show(); // Display the dialog
    }


    // --- Method to Show Report Confirmation Dialog ---
    private void showReportConfirmationDialog(ChatEntity chatToReport) {
        // Ensure context is available and chat object is valid and owned by current user
        if (getContext() == null || chatToReport == null || TextUtils.isEmpty(chatToReport.getUserId()) || !chatToReport.getOwnerUserId().equals(currentUserID)) {
            Log.w(TAG, "Cannot show report confirmation dialog, ChatEntity invalid or not owned by current user.");
            Toast.makeText(getContext(), "Error reporting user.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String reportedUserId = chatToReport.getUserId(); // The ID of the user being reported
        final String reportedUsername = chatToReport.getUsername(); // The name of the user being reported
        final String reportingUserId = currentUserID; // The ID of the user doing the reporting (current user)

        // Add a safety check: A user cannot report themselves.
        if (reportedUserId.equals(reportingUserId)) {
            Log.w(TAG, "User attempted to report themselves: " + reportingUserId);
            Toast.makeText(getContext(), "You cannot report yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Showing report confirmation dialog for user: " + reportedUsername + " (ID: " + reportedUserId + ")");

        new AlertDialog.Builder(getContext()) // Use getContext()
                .setTitle("Confirm Report") // Dialog title
                .setMessage("Are you sure you want to report " + reportedUsername + "? Repeated or malicious reports may result in action against your account.") // Confirmation message
                .setPositiveButton("Report", (dialog, which) -> {
                    Log.d(TAG, "Report confirmed for user: " + reportedUserId + " by user: " + reportingUserId);
                    // *** Call the method to actually report the user to Firebase ***
                    reportUser(reportedUserId, reportingUserId);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss(); // Dismiss the dialog if cancelled
                    Log.d(TAG, "Report confirmation cancelled for user: " + reportedUserId);
                })
                .show(); // Display the dialog
    }


    // --- Method to Report a User to Firebase ---
// This method writes the report to the /userReports node.
// Automatic blocking logic will be in Firebase Cloud Functions.
    private void reportUser(String reportedUserId, String reportingUserId) {
        // Ensure IDs and rootRef are valid before attempting Firebase ops
        if (TextUtils.isEmpty(reportedUserId) || TextUtils.isEmpty(reportingUserId) || rootRef == null) {
            Log.e(TAG, "reportUser: reportedUserId, reportingUserId, or rootRef is null/empty. Cannot report.");
            Toast.makeText(getContext(), "Error reporting user.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Submitting report in Firebase: Reported User ID=" + reportedUserId + ", Reporting User ID=" + reportingUserId);

        // Path for the report: /userReports/{reportedUserId}/{reportingUserId}
        DatabaseReference reportRef = rootRef.child("userReports") // Use lowercase 'u' as seen in your Firebase screenshot
                .child(reportedUserId)
                .child(reportingUserId);

        // Data to save for the report. Using a timestamp is useful for tracking *when* a user was reported.
        // Using ServerValue.TIMESTAMP ensures accuracy regardless of client device time.
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("timestamp", ServerValue.TIMESTAMP);
        // Optional: Add reporting user's name if you fetched it (senderUserName), but UID is sufficient for logic
        // reportData.put("reportingUsername", senderUserName != null ? senderUserName : "Unknown");

        // Write the report data to Firebase
        reportRef.setValue(reportData) // Use setValue to ensure unique key based on reportingUserId
                .addOnSuccessListener(aVoid -> {
                    // Firebase write successful
                    Log.d(TAG, "Report successfully submitted for user: " + reportedUserId + " by: " + reportingUserId);
                    Toast.makeText(getContext(), "User reported. Thank you for your feedback.", Toast.LENGTH_SHORT).show();

                    // Note: The automatic blocking logic is handled by Firebase Cloud Functions
                    // listening to writes under /userReports/{reportedUserId}.

                })
                .addOnFailureListener(e -> {
                    // Firebase write failed
                    Log.e(TAG, "Failed to submit report for user: " + reportedUserId + " by: " + reportingUserId, e);
                    Toast.makeText(getContext(), "Failed to submit report. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }


}
