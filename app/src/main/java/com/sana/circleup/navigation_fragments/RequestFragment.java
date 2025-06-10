package com.sana.circleup.navigation_fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sana.circleup.Contacts;
import com.sana.circleup.R;

import de.hdodenhof.circleimageview.CircleImageView;
//
//public class RequestFragment extends Fragment {
//
//    private View RequestView;
//    private RecyclerView myRequestList;
//    private DatabaseReference chatRequestsRef, userRef, contactsRef, notificationsRef;
//    private FirebaseAuth mAuth;
//    private String currentUserId;
//    private TextView noChatsText;
//    private static final String CHANNEL_ID = "friend_request_channel";
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        RequestView = inflater.inflate(R.layout.fragment_request, container, false);
//
//        mAuth = FirebaseAuth.getInstance();
//        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
//
//        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
//        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
//        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
//        notificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
//
//        noChatsText = RequestView.findViewById(R.id.no_requests_text);
//
//
//        myRequestList = RequestView.findViewById(R.id.chat_requests_list);
//        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        return RequestView;
//
//
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        if (currentUserId == null) {
//            Log.e("RequestFragment", "User not logged in.");
//            return;
//        }
//
//        createNotificationChannel();
//
//        Query query = chatRequestsRef.child(currentUserId).orderByChild("request_type");
//
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!snapshot.exists()) {
//                    noChatsText.setVisibility(View.VISIBLE);
//                } else {
//                    noChatsText.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("RequestFragment", "Database error: " + error.getMessage());
//            }
//        });
//
//
//        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
//                .setQuery(query, Contacts.class)
//                .build();
//
//        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
//
//
//
//
//
//            @Override
//            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Contacts model) {
//                String senderUserId = getRef(position).getKey();
//                if (senderUserId == null) {
//                    Log.e("RequestFragment", "senderUserId is null");
//                    return;
//                }
//
//                userRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (!snapshot.exists()) return;
//
//                        String name = snapshot.child("username").getValue(String.class);
//                        String status = snapshot.child("status").getValue(String.class);
//                        String base64Image = snapshot.child("profileImage").getValue(String.class);
//                        String role = snapshot.child("role").getValue(String.class);
//                        Boolean isBlocked = snapshot.child("isBlocked").getValue(Boolean.class);
//                        String requestType = model.getRequest_type();  // Get request type
//
//                        name = (name != null) ? name : "Unknown";
//                        status = (status != null) ? status : "";
//                        base64Image = (base64Image != null) ? base64Image : "";
//                        role = (role != null) ? role : "user";
//                        isBlocked = (isBlocked != null) ? isBlocked : false;
//
//                        holder.userName.setText(name);
//                        holder.userStatus.setText(status);
//
//                        if (!base64Image.isEmpty()) {
//                            try {
//                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
//                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                                holder.profileImage.setImageBitmap(decodedBitmap);
//                            } catch (Exception e) {
//                                holder.profileImage.setImageResource(R.drawable.default_profile_img);
//                                Log.e("RequestFragment", "Error decoding image: " + e.getMessage());
//                            }
//                        } else {
//                            holder.profileImage.setImageResource(R.drawable.default_profile_img);
//                        }
//
//                        // **Logic to show buttons/text based on request type**
//                        if ("received".equals(requestType)) {
//                            showFriendRequestNotification(name);
//                            holder.AcceptButton.setVisibility(View.VISIBLE);
//                            holder.CancelButton.setVisibility(View.VISIBLE);
//                            holder.requestSentText.setVisibility(View.GONE);
//
//                            String finalName = name;
//                            String finalStatus = status;
//                            String finalBase64Image = base64Image;
//                            String finalRole = role;
//                            Boolean finalIsBlocked = isBlocked;
//                            holder.AcceptButton.setOnClickListener(v -> acceptRequest(senderUserId, finalName, finalStatus, finalBase64Image, finalRole, finalIsBlocked));
//                            holder.CancelButton.setOnClickListener(v -> rejectRequest(senderUserId));
//                        }
//                        else if ("sent".equals(requestType)) {
//                            holder.AcceptButton.setVisibility(View.GONE);
//                            holder.CancelButton.setVisibility(View.GONE);
//                            holder.requestSentText.setVisibility(View.VISIBLE);
//                            holder.requestSentText.setText("Request Sent");
//
//                            // Set the text to be bigger, bold, and italic
//                            holder.CancelButton.setTextSize(22); // Increase font size
//                            holder.CancelButton.setTypeface(null, Typeface.BOLD_ITALIC); // Make text bold & italic
//
//                            holder.requestSentText.setOnClickListener(v -> cancelSentRequest(senderUserId));
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        Log.e("RequestFragment", "Database error: " + databaseError.getMessage());
//                    }
//                });
//            }
//
//
//
//
//            @NonNull
//            @Override
//            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.users_friends_display_layout, parent, false);
//                return new RequestViewHolder(view);
//            }
//        };
//
//        myRequestList.setAdapter(adapter);
//        adapter.startListening();
//    }
//
//    private void acceptRequest(String senderUserId, String name, String status, String base64Image, String role, boolean isBlocked) {
//        Contacts contact = new Contacts(senderUserId, name, base64Image, status, role, isBlocked, "accepted");
//
//        contactsRef.child(currentUserId).child(senderUserId).setValue(contact)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        contactsRef.child(senderUserId).child(currentUserId).setValue(contact)
//                                .addOnCompleteListener(task1 -> {
//                                    if (task1.isSuccessful()) {
//                                        removeChatRequest(senderUserId);
////                                        sendFriendAcceptanceNotification(senderUserId);
//                                        Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    }
//                });
//    }
//
//    private void rejectRequest(String senderUserId) {
//        removeChatRequest(senderUserId);
//        Toast.makeText(getContext(), "Contact Request Deleted", Toast.LENGTH_SHORT).show();
//    }
//
//
//
//    private void cancelSentRequest(String receiverUserId) {
//        new AlertDialog.Builder(getContext())
//                .setTitle("Cancel Friend Request")
//                .setMessage("Are you sure you want to cancel this friend request?")
//                .setPositiveButton("Yes", (dialog, which) -> {
//                    chatRequestsRef.child(currentUserId).child(receiverUserId).removeValue()
//                            .addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    chatRequestsRef.child(receiverUserId).child(currentUserId).removeValue()
//                                            .addOnCompleteListener(task1 -> {
//                                                if (task1.isSuccessful()) {
//                                                    Toast.makeText(getContext(), "Friend Request Cancelled", Toast.LENGTH_SHORT).show();
//                                                }
//                                            });
//                                }
//                            });
//                })
//                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
//                .show();
//    }
//
//
//
//
//
//    private void removeChatRequest(String senderUserId) {
//        chatRequestsRef.child(currentUserId).child(senderUserId).removeValue()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        chatRequestsRef.child(senderUserId).child(currentUserId).removeValue()
//                                .addOnCompleteListener(task1 -> {
//                                    if (task1.isSuccessful()) {
//                                        Log.d("RequestFragment", "Chat request removed successfully.");
//                                    }
//                                });
//                    }
//                });
//    }
//
//    private void sendFriendAcceptanceNotification(String senderUserId) {
//        String notificationKey = notificationsRef.push().getKey();
//        if (notificationKey != null) {
//            notificationsRef.child(senderUserId).child(notificationKey)
//                    .setValue("Your friend request was accepted by " + currentUserId)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            Log.d("RequestFragment", "Notification sent successfully.");
//                        }
//                    });
//        }
//    }
//
//
//    private void showFriendRequestNotification(String senderName) {
//        // Enqueue the worker to send the notification in the background
//        FriendRequestNotificationWorker.enqueueWork(getContext(), senderName);
//    }
//
//
//
//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Friend Request Notifications";
//            String description = "Notifications for received friend requests";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//
//            // Register the channel with the system
//            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
//            if (notificationManager != null) {
//                notificationManager.createNotificationChannel(channel);
//            }
//        }
//    }
//
//
//    public static class RequestViewHolder extends RecyclerView.ViewHolder {
//        TextView userName, userStatus, requestSentText;
//        CircleImageView profileImage;
//        Button AcceptButton, CancelButton;
//
//        public RequestViewHolder(@NonNull View itemView) {
//            super(itemView);
//            userName = itemView.findViewById(R.id.users_profile_name);
//            userStatus = itemView.findViewById(R.id.users_profile_status);
//            profileImage = itemView.findViewById(R.id.users_profile_image);
//            AcceptButton = itemView.findViewById(R.id.request_accept_button);
//            CancelButton = itemView.findViewById(R.id.request_cancel_button);
//            requestSentText = itemView.findViewById(R.id.request_sent_text);  // Add this in XML
//        }
//    }
//}
//



// Removed: import android.app.NotificationChannel;
// Removed: import android.app.NotificationManager;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonArray; // Added for OneSignal API
import com.google.gson.JsonObject; // Added for OneSignal API
import com.sana.circleup.one_signal_notification.OneSignalApiService; // Added OneSignal API Service

import java.io.IOException; // Added for response body reading
import java.util.Collections; // Added for singletonMap
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody; // Added for Retrofit ResponseBody
import retrofit2.Call; // Added for Retrofit Call
import retrofit2.Callback; // Added for Retrofit Callback
import retrofit2.Response; // Added for Retrofit Response
import retrofit2.Retrofit; // Added for Retrofit
import retrofit2.converter.gson.GsonConverterFactory; // Added for Gson Converter

public class RequestFragment extends Fragment {

    private static final String TAG = "RequestFragment"; // Added TAG

    private View RequestView;
    private RecyclerView myRequestList;
    // Removed notificationsRef
    private DatabaseReference chatRequestsRef, userRef, contactsRef; // Removed notificationsRef
    private FirebaseAuth mAuth;
    private String currentUserId;
    private TextView noChatsText; // Kept, assuming it's for showing "No Requests"

    // *** NEW MEMBER: Retrofit Service for OneSignal API ***
    private OneSignalApiService oneSignalApiService;
    // Get this from your OneSignal Dashboard -> Settings -> Keys & IDs
    // Use the same App ID used in CircleUpApp and ProfileUserInfoActivity
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR ACTUAL APP ID

    // *** NEW MEMBER: To store the current user's name for notifications ***
    private String currentUserName;
    // *** END NEW MEMBER ***


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🟢 onCreateView started.");
        RequestView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getUid() : null;

        // Check if user is authenticated. If not, display a message and return early.
        if (currentUserId == null) {
            Log.e(TAG, "User not logged in. Cannot initialize fragment.");
            // You might want to show a message or redirect user here
            TextView errorText = RequestView.findViewById(R.id.no_requests_text); // Reuse no_requests_text
            if (errorText != null) {
                errorText.setText("Please log in to view requests.");
                errorText.setVisibility(View.VISIBLE);
            }
            // Disable RecyclerView or hide it if needed
            if (myRequestList != null) myRequestList.setVisibility(View.GONE);
            // Returning null or a limited view might be appropriate
            return RequestView; // Return the view with error message
        }


        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        // Removed: notificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");


        noChatsText = RequestView.findViewById(R.id.no_requests_text);
        myRequestList = RequestView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));


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
            // Handle this error - notifications for accept/reject/cancel might not work
            // Consider showing a warning to the user
            Toast.makeText(getContext(), "Error initializing notification service. Some features may be limited.", Toast.LENGTH_LONG).show();
        }
        // *** END NEW ***

        // *** NEW: Fetch current user's name ***
        fetchCurrentUserName();
        // *** END NEW ***


        Log.d(TAG, "✅ onCreateView finished.");
        return RequestView;
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "➡️ onStart started.");

        if (currentUserId == null) {
            Log.w(TAG, "User ID is null in onStart. Skipping Firebase listener setup.");
            // onCreateView handles showing a message if user is not logged in
            return;
        }

        // Removed: createNotificationChannel(); // This is for local notifications

        // Query for chat requests related to the current user
        // The query should be on the current user's node in Chat Requests
        // Ordering by "request_type" might be okay depending on your desired sorting,
        // but listening directly to the current user's request node is key.
        Query query = chatRequestsRef.child(currentUserId); // Listen to all requests involving current user

        // Add a ValueEventListener to show/hide the "No Requests" text
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This listener will run whenever there's any change under /Chat Requests/currentUserId
                // It checks if there are any children under this node
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    Log.d(TAG, "No chat requests found for user " + currentUserId + ". Showing no_requests_text.");
                    if (noChatsText != null) {
                        noChatsText.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(TAG, "Requests found for user " + currentUserId + ". Hiding no_requests_text.");
                    if (noChatsText != null) {
                        noChatsText.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error on chat request listener: " + error.getMessage());
                // Consider showing an error message to the user
                if (noChatsText != null) {
                    noChatsText.setText("Error loading requests.");
                    noChatsText.setVisibility(View.VISIBLE);
                }
            }
        });


        // FirebaseRecyclerAdapter setup
        // The query remains the same, targeting chat requests involving the current user.
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(query, Contacts.class) // Use the query defined above
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_friends_display_layout, parent, false); // Assuming this layout is correct
                return new RequestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Contacts model) {
                // Get the UID of the OTHER user involved in this request node.
                // If the node is Chat Requests/currentUser/otherUser, then otherUser's UID is the key.
                final String otherUserId = getRef(position).getKey();
                if (TextUtils.isEmpty(otherUserId)) {
                    Log.e(TAG, "otherUserId is null or empty at position " + position + ". Skipping bind.");
                    // Hide or clear the view holder if data is invalid
                    holder.clear(); // Assuming you add a clear method to your ViewHolder
                    return;
                }

                // Fetch details of the OTHER user involved in the request
                userRef.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.w(TAG, "User data not found for ID: " + otherUserId + ". Skipping bind for this request.");
                            // Hide or clear the view holder if user data is missing
                            holder.clear();
                            return; // Exit this onDataChange
                        }

                        String name = snapshot.child("username").getValue(String.class);
                        String status = snapshot.child("status").getValue(String.class);
                        String base64Image = snapshot.child("profileImage").getValue(String.class);
                        // role and isBlocked might not be directly relevant for displaying requests, but keep if needed
                        // String role = snapshot.child("role").getValue(String.class);
                        // Boolean isBlocked = snapshot.child("isBlocked").getValue(Boolean.class);

                        // Use default values if data is null
                        name = (name != null) ? name : "Unknown User";
                        status = (status != null) ? status : "No Status Available";
                        base64Image = (base64Image != null) ? base64Image : "";
                        // role = (role != null) ? role : "user";
                        // isBlocked = (isBlocked != null) ? isBlocked : false;

                        holder.userName.setText(name);
                        holder.userStatus.setText(status);

                        // Load profile image from Base64 string or URL
                        if (!TextUtils.isEmpty(base64Image)) {
                            try {
                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                holder.profileImage.setImageBitmap(decodedBitmap);
                            } catch (IllegalArgumentException e) { // Specific catch for Base64 errors
                                Log.e(TAG, "Error decoding Base64 image for user " + otherUserId + ": Invalid Base64 string", e);
                                holder.profileImage.setImageResource(R.drawable.default_profile_img); // Default on error
                            } catch (Exception e) {
                                Log.e(TAG, "Error decoding image for user " + otherUserId, e);
                                holder.profileImage.setImageResource(R.drawable.default_profile_img); // Default on error
                            }
                        } else {
                            holder.profileImage.setImageResource(R.drawable.default_profile_img); // Default if data is null/empty
                        }


                        // **Logic to show buttons/text based on request type**
                        // model.getRequest_type() gets the type from the Chat Requests/currentUserId/otherUserId node
                        String requestType = model.getRequest_type(); // This is the type from *current user's* perspective

                        if ("received".equals(requestType)) {
                            // This is an incoming request TO the current user FROM otherUserId
                            // Removed: showFriendRequestNotification(name); // Notification for receiving is sent by the *sender*

                            holder.AcceptButton.setVisibility(View.VISIBLE);
                            holder.CancelButton.setVisibility(View.VISIBLE); // Cancel button here means Decline
                            holder.CancelButton.setText("Decline"); // Change text for clarity
                            holder.requestSentText.setVisibility(View.GONE);

                            // Set click listeners for Accept and Decline
                            // Pass otherUserId (the sender of the request) to the handler methods
                            holder.AcceptButton.setOnClickListener(v -> acceptRequest(otherUserId)); // Pass the sender's ID
                            holder.CancelButton.setOnClickListener(v -> rejectRequest(otherUserId)); // Pass the sender's ID

                            // Optional: Add text style changes for "Decline" if needed
                            holder.CancelButton.setTypeface(null, Typeface.NORMAL); // Reset typeface
                            holder.CancelButton.setTextSize(14); // Reset text size to normal button size

                        } else if ("sent".equals(requestType)) {
                            // This is an outgoing request FROM the current user TO otherUserId
                            holder.AcceptButton.setVisibility(View.GONE);
                            holder.CancelButton.setVisibility(View.GONE); // No standard Cancel button for sent requests (using text view click)
                            holder.requestSentText.setVisibility(View.VISIBLE); // Show the text view
                            holder.requestSentText.setText("Request Sent (Tap to Cancel)"); // Inform user they can tap to cancel

                            // Set the text style
                            holder.requestSentText.setTextSize(14); // Standard text size, adjust as needed
                            holder.requestSentText.setTypeface(null, Typeface.NORMAL); // Normal typeface
                            // Optional: Maybe make it italic or a specific color to indicate clickability

                            // Set the click listener on the TextView for canceling the SENT request
                            // Pass otherUserId (the receiver of the request) to the cancelSentRequest method
                            holder.requestSentText.setOnClickListener(v -> cancelSentRequest(otherUserId)); // Pass the receiver's ID
                        } else {
                            // Handle any unexpected request_type values
                            Log.w(TAG, "Unexpected request_type '" + requestType + "' for user " + otherUserId);
                            // Clear the view holder or hide it
                            holder.clear();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Database error fetching user data for request: " + databaseError.getMessage());
                        // Handle error, maybe show a placeholder or clear the item
                        holder.clear();
                    }
                });
            }

            // Helper method to clear view holder content
            private void clearViewHolder(RequestViewHolder holder) {
                if (holder != null) {
                    holder.userName.setText("");
                    holder.userStatus.setText("");
                    holder.profileImage.setImageResource(R.drawable.default_profile_img);
                    holder.AcceptButton.setVisibility(View.GONE);
                    holder.CancelButton.setVisibility(View.GONE);
                    holder.requestSentText.setVisibility(View.GONE);
                    // Clear click listeners to prevent stale references (important!)
                    holder.AcceptButton.setOnClickListener(null);
                    holder.CancelButton.setOnClickListener(null);
                    holder.requestSentText.setOnClickListener(null);
                }
            }


        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
        Log.d(TAG, "✅ FirebaseRecyclerAdapter started listening.");
    }

    // *** NEW HELPER METHOD TO FETCH CURRENT USER'S NAME ***
    private void fetchCurrentUserName() {
        if (TextUtils.isEmpty(currentUserId) || userRef == null) {
            Log.w(TAG, "fetchCurrentUserName: currentUserId or userRef is empty/null, cannot fetch name.");
            currentUserName = "A User"; // Default name
            return;
        }
        Log.d(TAG, "Fetching current user's name for UID: " + currentUserId);
        userRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    String name = snapshot.child("username").getValue(String.class);
                    if (!TextUtils.isEmpty(name)) {
                        currentUserName = name; // Store the fetched name
                        Log.d(TAG, "Fetched current user name: " + currentUserName);
                    } else {
                        currentUserName = "A User"; // Default if username field is empty
                        Log.w(TAG, "Current user's username field is empty. Using default name.");
                    }
                } else {
                    currentUserName = "A User"; // Default if user data or username field is missing
                    Log.w(TAG, "Current user data or username field not found for UID: " + currentUserId + ". Using default name.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch current user name from DB for UID: " + currentUserId, error.toException());
                currentUserName = "A User"; // Default on error
            }
        });
    }
    // *** END NEW HELPER METHOD ***


    // --- Modified acceptRequest method ---
    // We only need the sender's ID to perform Firebase ops and send notification
    private void acceptRequest(String senderUserId) {
        Log.d(TAG, "Accepting request from: " + senderUserId);
        // Ensure IDs and references are valid before attempting Firebase ops
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(senderUserId) || contactsRef == null || chatRequestsRef == null || getContext() == null) {
            Log.e(TAG, "acceptRequest: currentUserId, senderUserId, refs, or context is null/empty.");
            Toast.makeText(getContext(), "Error accepting request.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use updateChildren for atomic write for both sides of contact save
        Map<String, Object> contactUpdates = new HashMap<>();
        // Add contact node for current user (currentUserId -> senderUserId)
        contactUpdates.put("/Contacts/" + currentUserId + "/" + senderUserId + "/request_type", "accepted"); // Mark as accepted for current user
        // Add contact node for sender (senderUserId -> currentUserId)
        contactUpdates.put("/Contacts/" + senderUserId + "/" + currentUserId + "/request_type", "accepted"); // Mark as accepted for original sender

        FirebaseDatabase.getInstance().getReference().updateChildren(contactUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // This runs on the main thread AFTER the contact updates complete
                        if(task.isSuccessful())
                        {
                            Log.d(TAG, "Contacts updated successfully for both sides to 'accepted'. Now removing chat request nodes.");
                            // Now remove the chat request nodes for both sides using the helper
                            removeChatRequest(senderUserId, taskForRemoval -> { // Pass a callback to run code after removal
                                // This callback runs on the main thread after removeChatRequest's final task completes
                                if (taskForRemoval.isSuccessful()) {
                                    Log.d(TAG, "Chat request removed after acceptance.");
                                    Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();

                                    // *** NEW: Send OneSignal Push Notification to the ORIGINAL SENDER (senderUserId) ***
                                    // Notify the user who INITIALLY SENT the request that it has been ACCEPTED.
                                    // The current user (currentUserId) is the one ACCEPTING.
                                    // We need the ACCEPTING user's name (currentUserName) for the notification content.
                                    if (oneSignalApiService != null && !TextUtils.isEmpty(currentUserName)) {
                                        Log.d(TAG, "Initiating OneSignal Push Notification for accepted friend request.");
                                        String title = "Friend Request Accepted";
                                        // Use the name of the person who ACCEPTED the request (current user's name)
                                        String content = currentUserName + " accepted your friend request.";
                                        // Call helper:
                                        // recipient is senderUserId (the original sender of the request)
                                        // acting user is currentUserId (the accepter)
                                        sendFriendRequestPushNotification(
                                                oneSignalApiService,
                                                senderUserId, // Recipient (original sender of the request)
                                                title,
                                                content,
                                                currentUserId, // Acting user (the accepter, who is the current user)
                                                "friend_request_accepted" // Notification type identifier
                                        );
                                    } else {
                                        Log.e(TAG, "OneSignalApiService or currentUserName is null/empty. Cannot send friend request accepted notification.");
                                        // Optionally show a warning to the user
                                        // Toast.makeText(getContext(), "Notification failed.", Toast.LENGTH_SHORT).show();
                                    }
                                    // *** END NEW ***

                                } else {
                                    Log.e(TAG, "Failed to remove chat request nodes after acceptance.", taskForRemoval.getException());
                                    Toast.makeText(getContext(), "Request accepted, but cleanup failed.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e(TAG, "Failed to update contacts node for accept.", task.getException());
                            Toast.makeText(getContext(), "Failed to accept request.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    // --- End Modified acceptRequest method ---


    // --- Modified rejectRequest method ---
    private void rejectRequest(String senderUserId) {
        Log.d(TAG, "Rejecting request from: " + senderUserId);
        // Ensure IDs and references are valid before attempting Firebase ops
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(senderUserId) || chatRequestsRef == null || getContext() == null) {
            Log.e(TAG, "rejectRequest: currentUserId, senderUserId, ref, or context is null/empty.");
            Toast.makeText(getContext(), "Error rejecting request.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove the chat request nodes for both sides using the helper
        removeChatRequest(senderUserId, task -> { // Pass a callback to run code after removal
            // This callback runs on the main thread after removeChatRequest's final task completes
            if (task.isSuccessful()) {
                Log.d(TAG, "Chat request removed after rejection.");
                Toast.makeText(getContext(), "Contact Request Deleted", Toast.LENGTH_SHORT).show();

                // *** NEW: Send OneSignal Push Notification to the ORIGINAL SENDER (senderUserId) ***
                // Notify the user who INITIALLY SENT the request that it has been REJECTED.
                // The current user (currentUserId) is the one REJECTING.
                // We need the REJECTING user's name (currentUserName) for the notification content.
                if (oneSignalApiService != null && !TextUtils.isEmpty(currentUserName)) {
                    Log.d(TAG, "Initiating OneSignal Push Notification for rejected friend request.");
                    String title = "Friend Request Declined";
                    // Use the name of the person who REJECTED the request (current user's name)
                    String content = currentUserName + " declined your friend request.";
                    // Call helper:
                    // recipient is senderUserId (the original sender of the request)
                    // acting user is currentUserId (the decliner)
                    sendFriendRequestPushNotification(
                            oneSignalApiService,
                            senderUserId, // Recipient (original sender of the request)
                            title,
                            content,
                            currentUserId, // Acting user (the decliner, who is the current user)
                            "friend_request_declined" // Notification type identifier
                    );
                } else {
                    Log.e(TAG, "OneSignalApiService or currentUserName is null/empty. Cannot send friend request rejected notification.");
                    // Optionally show a warning to the user
                    // Toast.makeText(getContext(), "Notification failed.", Toast.LENGTH_SHORT).show();
                }
                // *** END NEW ***

            } else {
                Log.e(TAG, "Failed to remove chat request nodes after rejection.", task.getException());
                Toast.makeText(getContext(), "Request rejected, but cleanup failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // --- End Modified rejectRequest method ---


    // --- Modified cancelSentRequest method ---
    private void cancelSentRequest(String receiverUserId) {
        Log.d(TAG, "Attempting to cancel sent request to: " + receiverUserId);
        // Ensure IDs and references are valid before attempting Firebase ops
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(receiverUserId) || chatRequestsRef == null || getContext() == null) {
            Log.e(TAG, "cancelSentRequest: currentUserId, receiverUserId, ref, or context is null/empty.");
            Toast.makeText(getContext(), "Error cancelling request.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Cancel Friend Request")
                .setMessage("Are you sure you want to cancel the friend request sent to " + receiverUserId + "?") // You could use receiverUserName if fetched
                .setPositiveButton("Yes", (dialog, which) -> {
                    Log.d(TAG, "User confirmed cancellation of request to " + receiverUserId);
                    // Remove the chat request nodes for both sides using the helper
                    removeChatRequest(receiverUserId, task -> { // Pass a callback to run code after removal
                        // This callback runs on the main thread after removeChatRequest's final task completes
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Chat request removed after cancellation.");
                            Toast.makeText(getContext(), "Friend Request Cancelled", Toast.LENGTH_SHORT).show();

                            // *** NEW: Send OneSignal Push Notification to the RECIPIENT (receiverUserId) ***
                            // Notify the user who RECEIVED the request that it has been CANCELLED.
                            // The current user (currentUserId) is the one CANCELLING.
                            // We need the CANCELLING user's name (currentUserName) for the notification content.
                            if (oneSignalApiService != null && !TextUtils.isEmpty(currentUserName)) {
                                Log.d(TAG, "Initiating OneSignal Push Notification for cancelled friend request.");
                                String title = "Friend Request Cancelled";
                                // Use the name of the person who CANCELLED the request (current user's name)
                                String content = currentUserName + " cancelled the friend request.";
                                // Call helper:
                                // recipient is receiverUserId (the user who received the request)
                                // acting user is currentUserId (the canceller)
                                sendFriendRequestPushNotification(
                                        oneSignalApiService,
                                        receiverUserId, // Recipient (the user who received the request)
                                        title,
                                        content,
                                        currentUserId, // Acting user (the canceller, who is the current user)
                                        "friend_request_cancelled" // Notification type identifier
                                );
                            } else {
                                Log.e(TAG, "OneSignalApiService or currentUserName is null/empty. Cannot send friend request cancelled notification.");
                                // Optionally show a warning to the user
                                // Toast.makeText(getContext(), "Notification failed.", Toast.LENGTH_SHORT).show();
                            }
                            // *** END NEW ***

                        } else {
                            Log.e(TAG, "Failed to remove chat request nodes after cancellation.", task.getException());
                            Toast.makeText(getContext(), "Request cancellation cleanup failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Log.d(TAG, "User cancelled the cancellation dialog.");
                    dialog.dismiss();
                })
                .show();
    }
    // --- End Modified cancelSentRequest method ---


    // --- Modified removeChatRequest method to include a completion callback ---
    // This method now removes the request node from both sides and runs a provided callback
    private void removeChatRequest(String otherUserId, OnCompleteListener<Void> completionCallback) {
        // Ensure IDs and references are valid
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(otherUserId) || chatRequestsRef == null) {
            Log.e(TAG, "removeChatRequest: currentUserId, otherUserId, or chatRequestsRef is null/empty. Aborting removal.");
            // Since we can't run the actual removal, we need to signal failure to the callback
            // Creating a synthetic failed Task might be complex. Simpler to log and return,
            // assuming callers handle the potential for no callback execution on immediate failure.
            // Or, you could call the callback immediately with a failed Task if context allows.
            // Let's assume basic validation passes based on callers.
            // For robustness, consider a better way to handle immediate validation failures.
        }

        Log.d(TAG, "Attempting to remove chat request between " + currentUserId + " and " + otherUserId);

        // Remove chat request node from current user's perspective
        chatRequestsRef.child(currentUserId).child(otherUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Log.d(TAG, "Current user's chat request node removed (" + currentUserId + " -> " + otherUserId + ").");
                            // Remove chat request node from the other user's perspective
                            chatRequestsRef.child(otherUserId).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // This runs on the main thread AFTER the second remove operation completes
                                            if(task.isSuccessful())
                                            {
                                                Log.d(TAG, "Other user's chat request node removed (" + otherUserId + " -> " + currentUserId + "). Request nodes fully removed.");
                                                // Call the provided callback with the final task result
                                                if (completionCallback != null) {
                                                    completionCallback.onComplete(task); // Task should be successful here
                                                }

                                            } else {
                                                Log.e(TAG, "Failed to remove other user's chat request node (" + otherUserId + ").", task.getException());
                                                // Call the provided callback with the failed task result
                                                if (completionCallback != null) {
                                                    completionCallback.onComplete(task); // Task has failed
                                                }
                                            }
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Failed to remove current user's chat request node (" + currentUserId + ").", task.getException());
                            // Call the provided callback with the failed task result
                            if (completionCallback != null) {
                                completionCallback.onComplete(task); // Task has failed
                            }
                        }
                    }
                });
    }
    // --- End Modified removeChatRequest method ---


    // Removed sendFriendAcceptanceNotification

    // Removed showFriendRequestNotification

    // Removed createNotificationChannel


    // *** NEW HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION FOR FRIEND REQUEST/ACCEPTANCE/DECLINE/CANCEL ***
    // This is a generic helper that can be used by acceptRequest, rejectRequest, cancelSentRequest
    /**
     * Sends a OneSignal push notification for friend request related events (sent, accepted, declined, cancelled).
     *
     * @param apiService          The Retrofit OneSignalApiService instance.
     * @param recipientFirebaseUID The Firebase UID of the user who should RECEIVE the notification.
     * @param title               The title of the notification.
     * @param messageContent      The main content/body of the notification.
     * @param actingUserFirebaseUID The Firebase UID of the user who PERFORMED the action (e.g., sender of request, accepter of request).
     * @param notificationType    A string identifier for the type of notification (e.g., "friend_request", "friend_request_accepted", "friend_request_declined", "friend_request_cancelled").
     */
    private void sendFriendRequestPushNotification(OneSignalApiService apiService,
                                                   String recipientFirebaseUID,
                                                   String title,
                                                   String messageContent,
                                                   String actingUserFirebaseUID, // The UID of the user who performed the action
                                                   String notificationType) { // Identifier for the type of notification


        // --- Input Validation ---
        if (apiService == null) {
            Log.e(TAG, "sendFriendRequestPushNotification: API service is null. Cannot send notification.");
            return;
        }
        if (TextUtils.isEmpty(recipientFirebaseUID)) {
            Log.e(TAG, "sendFriendRequestPushNotification: Recipient UID is null or empty. Cannot send notification.");
            return;
        }
        if (TextUtils.isEmpty(notificationType)) {
            Log.e(TAG, "sendFriendRequestPushNotification: Notification type is null or empty. Cannot send notification.");
            return;
        }
        // actingUserFirebaseUID can be null/empty if your specific notification type doesn't involve an acting user,
        // but for friend requests/acceptances/declines/cancels, it's crucial.
        if (TextUtils.isEmpty(actingUserFirebaseUID)) {
            Log.w(TAG, "sendFriendRequestPushNotification: 'actingUserFirebaseUID' is empty for type " + notificationType + ". Data may be incomplete for handling.");
            // Continue anyway, but data payload might be missing info.
        }


        Log.d(TAG, "Preparing OneSignal push notification type '" + notificationType + "' to recipient UID (External ID): " + recipientFirebaseUID);

        // --- Build the JSON payload for OneSignal API ---
        JsonObject notificationBody = new JsonObject();

        // 1. Add App ID
        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID); // Use your OneSignal App ID (class member)

        // 2. Specify recipients using External User IDs (Firebase UIDs)
        JsonArray externalUserIds = new JsonArray();
        externalUserIds.add(recipientFirebaseUID); // Add the recipient's Firebase UID
        notificationBody.add("include_external_user_ids", externalUserIds); // Use include_external_user_ids

        // 3. Add Notification Title and Content
        // Use different keys for different languages (e.g., "en" for English)
        // For simplicity, using singletonMap
        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", title))); // Title passed
        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", messageContent))); // Content passed

        // 4. Add custom data (important for handling notification click in the app)
        // This data can be retrieved when the user clicks the notification
        JsonObject data = new JsonObject();
        // Add notification type identifier
        data.addProperty("notificationType", notificationType);

        // Add relevant IDs based on the type
        if (!TextUtils.isEmpty(actingUserFirebaseUID)) {
            // The user who performed the action (sent, accepted, declined, cancelled)
            data.addProperty("actingUserId", actingUserFirebaseUID);
        }

        // For friend request related notifications, the other user involved is the one who is *not* the recipient and *not* the acting user.
        // For example:
        // - accept: Recipient=originalSender, ActingUser=accepter. The 'other' user is the originalSender/recipient.
        // - decline: Recipient=originalSender, ActingUser=decliner. The 'other' user is the originalSender/recipient.
        // - cancel: Recipient=originalReceiver, ActingUser=canceller. The 'other' user is the originalReceiver/recipient.
        // The 'targetUser' if they click the notification is likely the user who performed the action (e.g., see profile of the accepter/decliner).
        if (!TextUtils.isEmpty(actingUserFirebaseUID)) {
            // Target the user who performed the action (e.g., tapping shows accepter's profile)
            data.addProperty("targetUserId", actingUserFirebaseUID);
        } else {
            // Fallback: target the recipient if acting user ID is somehow missing
            data.addProperty("targetUserId", recipientFirebaseUID);
        }


        notificationBody.add("data", data);

        // 5. Optional: Set small icon (recommended)
        // Use the resource name of your app's small notification icon (e.g., "app_icon_circleup")
        // Ensure this drawable exists and is correctly configured in your app.
        notificationBody.addProperty("small_icon", "app_icon_circleup"); // <<< REPLACE with your actual small icon resource name (string)


        // 6. Optional: Customize notification appearance (sound, vibration, etc.)
        // Check OneSignal API documentation for available options:
        // https://documentation.onesignal.com/reference/create-notification
        // notificationBody.addProperty("sound", "default"); // Example sound


        // --- Make the API call asynchronously using Retrofit ---
        Log.d(TAG, "Making OneSignal API call for '" + notificationType + "' to recipient: " + recipientFirebaseUID);
        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                // This callback runs on the main thread
                if (response.isSuccessful()) {
                    // Notification request was successfully sent to OneSignal (doesn't mean it was delivered or seen)
                    Log.d(TAG, "OneSignal API call successful for '" + notificationType + "' to " + recipientFirebaseUID + ". Response Code: " + response.code());
                    // Log response body for debugging success/failure details from OneSignal
                    try (ResponseBody responseBody = response.body()) {
                        String resBody = responseBody != null ? responseBody.string() : "N/A";
                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
                        // Example success body: {"id": "a62fddc6-5c02-4020-a7aa-2d022951bcf1", "recipients": 1}
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read success response body ('" + notificationType + "')", e);
                    }
                } else {
                    // API call failed (e.g., invalid key, invalid payload, invalid external user ID)
                    Log.e(TAG, "OneSignal API call failed for '" + notificationType + "' to " + recipientFirebaseUID + ". Response Code: " + response.code());
                    // Log error body for debugging failure reason from OneSignal
                    try (ResponseBody errorBody = response.errorBody()) {
                        String errBody = errorBody != null ? errorBody.string() : "N/A";
                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
                        // Common errors: 400 (Invalid JSON), 403 (Invalid REST API Key), 404 (App ID not found), Invalid External IDs
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read error response body ('" + notificationType + "')", e);
                    }
                    Log.w(TAG, "Push notification failed via OneSignal API for type: " + notificationType);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                // Network error or request couldn't be sent at all
                Log.e(TAG, "OneSignal API call failed (network error) for '" + notificationType + "' to " + recipientFirebaseUID, t);
                Log.w(TAG, "Push notification failed due to network error for type: " + notificationType);
            }
        });
        Log.d(TAG, "OneSignal API call enqueued for '" + notificationType + "' to recipient: " + recipientFirebaseUID);
    }
    // *** END NEW HELPER METHOD ***



    // You need to add a method to clear the ViewHolder in case of errors or invalid data
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus, requestSentText;
        CircleImageView profileImage;
        Button AcceptButton, CancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.users_profile_name);
            userStatus = itemView.findViewById(R.id.users_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_button); // Ensure correct ID
            CancelButton = itemView.findViewById(R.id.request_cancel_button); // Ensure correct ID
            requestSentText = itemView.findViewById(R.id.request_sent_text);  // Ensure correct ID
        }

        // Helper method to clear the view holder
        public void clear() {
            userName.setText("");
            userStatus.setText("");
            profileImage.setImageResource(R.drawable.default_profile_img);
            AcceptButton.setVisibility(View.GONE);
            CancelButton.setVisibility(View.GONE);
            requestSentText.setVisibility(View.GONE);
            // Crucially, clear click listeners to prevent memory leaks and stale references
            AcceptButton.setOnClickListener(null);
            CancelButton.setOnClickListener(null);
            requestSentText.setOnClickListener(null);
        }
    }
}