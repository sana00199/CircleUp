

package com.sana.circleup;

//public class ProfileUserInfoActivity extends AppCompatActivity {
//
//    private String receiverUserId , sendUserID, Current_State;
//    private CircleImageView userProfileImage;
//    private TextView userProfileName, userProfileStatus;
//    private Button SendMessageRequestButton, DeclineRequestButton;
//
//    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef;
//    private FirebaseAuth mAuth;
//    private String userNameGlobal = "", userImageBase64Global = "";
//
//    // *** NEW MEMBER VARIABLES ***
//    private String senderUserName; // <<< Store the current user's name (the one sending the request)
//    private String receiverUserName; // <<< Store the user's name we are viewing (the recipient)
//    //private String userNameGlobal = "", userImageBase64Global = ""; // <<< These seem redundant now, consider removing if not used
//    // *** END NEW MEMBER VARIABLES ***
//    // *** NEW MEMBER: Retrofit Service for OneSignal API ***
//    private OneSignalApiService oneSignalApiService;
//    // Get this from your OneSignal Dashboard -> Settings -> Keys & IDs
//    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR APP ID
//    // *** END NEW MEMBER ***
//
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile_user_info);
//
//
//        mAuth = FirebaseAuth.getInstance();
//        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
//        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
//        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
//        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
//
//        receiverUserId = getIntent().getStringExtra("visit_user_id").toString();
//        sendUserID = mAuth.getCurrentUser().getUid();
//
//
//
//        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_img);
//        userProfileName = (TextView) findViewById(R.id.visit_user_name);
//        userProfileStatus = (TextView) findViewById(R.id.visit_user_status);
//        SendMessageRequestButton = (Button) findViewById(R.id.sendmsg_request_btn);
//        DeclineRequestButton = (Button) findViewById(R.id.declinemsg_request_btn);
//        Current_State = "new";
//
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
//        RetrieveUserInfo();
//
//    }
//
//    private void RetrieveUserInfo() {
//        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    String userName = snapshot.child("username").getValue(String.class);
//                    String userStatus = snapshot.child("status").getValue(String.class);
//                    String userImageBase64 = snapshot.child("profileImage").getValue(String.class);
//
//
//
//                    // *** NEW: Store Receiver's Name ***
//                    receiverUserName = userName; // Store the receiver's name
//                    // *** END NEW ***
//
//
//                    userNameGlobal = userName;
//                    userImageBase64Global = userImageBase64;
//
//                    userProfileName.setText(userName);
//                    // --- NEW: Check if userStatus is empty or null before setting ---
//                    if (TextUtils.isEmpty(userStatus)) {
//                        userProfileStatus.setText("No Status Uploaded");
//                    } else {
//                        userProfileStatus.setText(userStatus); // Set the actual status if not empty
//                    }
//
//                    if (userImageBase64 != null && !userImageBase64.isEmpty()) {
//                        try {
//                            byte[] decodedString = Base64.decode(userImageBase64, Base64.DEFAULT);
//                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                            userProfileImage.setImageBitmap(decodedBitmap);
//                        } catch (Exception e) {
//                            System.out.println("Error decoding image: " + e.getMessage());
//                        }
//                    } else {
//                        userProfileImage.setImageResource(R.drawable.default_profile_img);
//                    }
//
//
//
//
//                    // --- NEW: Fetch Current User's Name AFTER receiver info is loaded ---
//                    // We need the sender's name for the notification title.
//                    fetchCurrentUserName(); // Fetch the sender's name asynchronously
//                    // --- END NEW ---
//
//
//
//                    //  Now add this block inside after data is ready
//                    if (sendUserID.equals(receiverUserId)) {
//                        SendMessageRequestButton.setText("Send Message");
//                        SendMessageRequestButton.setEnabled(true);
//                        DeclineRequestButton.setVisibility(View.INVISIBLE);
//                        DeclineRequestButton.setEnabled(false);
//
//                        SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Intent chatIntent = new Intent(ProfileUserInfoActivity.this, ChatPageActivity.class);
//                                chatIntent.putExtra("visit_user_id", receiverUserId);
//                                chatIntent.putExtra("userName", userNameGlobal);
//                                chatIntent.putExtra("userImage", userImageBase64Global);
//                                startActivity(chatIntent);
//                            }
//                        });
//                    }
//
//                } else {
//                    Toast.makeText(ProfileUserInfoActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
//                }
//
//                ManageChatRequest();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(ProfileUserInfoActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//
//
//
//
//
//
//    // *** NEW HELPER METHOD TO FETCH CURRENT USER'S NAME (SENDER) ***
//    private void fetchCurrentUserName() {
//        if (TextUtils.isEmpty(sendUserID)) {
//            Log.w(TAG, "fetchCurrentUserName: sendUserID is empty, cannot fetch name.");
//            senderUserName = "A User"; // Default sender name
//            return;
//        }
//        Log.d(TAG, "Fetching current user's (sender) name for UID: " + sendUserID);
//        userRef.child(sendUserID).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists() && snapshot.hasChild("username")) {
//                    String name = snapshot.child("username").getValue(String.class);
//                    if (!TextUtils.isEmpty(name)) {
//                        senderUserName = name; // Store the sender's name
//                        Log.d(TAG, "Fetched sender name: " + senderUserName);
//                    } else {
//                        senderUserName = "A User"; // Default if username field is empty
//                        Log.w(TAG, "Current user's username field is empty. Using default sender name.");
//                    }
//                } else {
//                    senderUserName = "A User"; // Default if user data or username field is missing
//                    Log.w(TAG, "Current user data or username field not found for sender UID. Using default sender name.");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to fetch current user (sender) name from DB", error.toException());
//                senderUserName = "A User"; // Default on error
//            }
//        });
//    }
//    // *** END NEW HELPER METHOD ***
//
//
//
//    private void ManageChatRequest() {
//
//        // If the current user is viewing their own profile, show the "Send Message" button
//        if (sendUserID.equals(receiverUserId)) {
//            // Show "Send Message" button for the user's own profile
//            SendMessageRequestButton.setText("   Me   ");
//            SendMessageRequestButton.setEnabled(true);  // Enable the button
//            DeclineRequestButton.setVisibility(View.INVISIBLE); // Hide the Decline button
//            DeclineRequestButton.setEnabled(false);  // Disable the Decline button
//        } else {
//            // If it's not the user's own profile, show the request-related buttons as before
//            chatRequestRef.child(sendUserID)
//                    .addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if(snapshot.hasChild(receiverUserId)) {
//                                String request_type = snapshot.child(receiverUserId).child("request_type").getValue().toString();
//
//                                if(request_type.equals("sent")) {
//                                    Current_State = "request_sent";
//                                    SendMessageRequestButton.setText("Cancel Chat Request");
//                                } else if(request_type.equals("received")) {
//                                    Current_State = "request_received";
//                                    SendMessageRequestButton.setText("Accept Chat Request");
//
//                                    DeclineRequestButton.setVisibility(View.VISIBLE);
//                                    DeclineRequestButton.setEnabled(true);
//
//                                    DeclineRequestButton.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View view) {
//                                            CancelChatRequest();
//                                        }
//                                    });
//                                }
//                            } else {
//                                contactsRef.child(sendUserID)
//                                        .addListenerForSingleValueEvent(new ValueEventListener() {
//                                            @Override
//                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                                if(snapshot.hasChild(receiverUserId)) {
//                                                    Current_State = "friends";
//                                                    SendMessageRequestButton.setText("Remove this contact");
//                                                }
//                                            }
//
//                                            @Override
//                                            public void onCancelled(@NonNull DatabaseError error) {}
//                                        });
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {}
//                    });
//        }
//
//        // Handle click for "Send Message" button based on the state
//        if (!sendUserID.equals(receiverUserId)) {
//            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    SendMessageRequestButton.setEnabled(false);
//
//                    if (Current_State.equals("new")) {
//                        SendChatRequest();
//                    }
//                    if (Current_State.equals("request_sent")) {
//                        CancelChatRequest();
//                    }
//                    if (Current_State.equals("request_received")) {
//                        AcceptChatRequest();
//                    }
//                    if (Current_State.equals("friends")) {
//                        RemoveSpecificContact();
//                    }
//                }
//            });
//        } else {
//            // If it's the user's own profile, handle the "Send Message" click
//            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(ProfileUserInfoActivity.this, "Your Own Profile", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        }
//    }
//
//
//    private void RemoveSpecificContact() {
//        contactsRef.child(sendUserID).child(receiverUserId)
//                .removeValue()
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful())
//                        {
//                            contactsRef.child(receiverUserId).child(sendUserID)
//                                    .removeValue()
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful())
//                                            {
//                                                SendMessageRequestButton.setEnabled(true);
//                                                Current_State = "new";
//                                                SendMessageRequestButton.setText("Send Message");
//
//                                                DeclineRequestButton.setVisibility(View.INVISIBLE);
//                                                DeclineRequestButton.setEnabled(false);
//                                            }
//
//                                        }
//                                    });
//                        }
//
//                    }
//                });
//
//    }
//
////    private void AcceptChatRequest() {
////        contactsRef.child(sendUserID).child(receiverUserId)
////                .child("Contacts").setValue("Saved")
////                .addOnCompleteListener(new OnCompleteListener<Void>() {
////                    @Override
////                    public void onComplete(@NonNull Task<Void> task) {
////                        if(task.isSuccessful())
////                        {
////                            contactsRef.child(receiverUserId).child(sendUserID)
////                                    .child("Contacts").setValue("Saved")
////                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
////                                        @Override
////                                        public void onComplete(@NonNull Task<Void> task) {
////                                            if(task.isSuccessful())
////                                            {
////                                                chatRequestRef.child(sendUserID).child(receiverUserId)
////                                                        .removeValue()
////                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
////                                                            @Override
////                                                            public void onComplete(@NonNull Task<Void> task) {
////                                                                if(task.isSuccessful())
////                                                                {
////                                                                    chatRequestRef.child(receiverUserId).child(sendUserID)
////                                                                            .removeValue()
////                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
////                                                                                @Override
////                                                                                public void onComplete(@NonNull Task<Void> task) {
////                                                                                    SendMessageRequestButton.setEnabled(true);
////                                                                                    Current_State = "friends";
////                                                                                    SendMessageRequestButton.setText("Remove this Contact");
////
////                                                                                    DeclineRequestButton.setVisibility(View.INVISIBLE);
////                                                                                    DeclineRequestButton.setEnabled(false);
////
////
////
////                                                                                }
////                                                                            });
////                                                                }
////                                                            }
////                                                        });
////
////                                            }
////                                        }
////                                    });
////                        }
////                    }
////                });
////
////    }
//
//
//
//
//    private void AcceptChatRequest() {
//        // Ensure IDs are valid before attempting Firebase ops
//        if (TextUtils.isEmpty(sendUserID) || TextUtils.isEmpty(receiverUserId)) {
//            Log.e(TAG, "AcceptChatRequest: sendUserID or receiverUserId is null/empty.");
//            Toast.makeText(this, "Error accepting request.", Toast.LENGTH_SHORT).show();
//            SendMessageRequestButton.setEnabled(true); // Re-enable button
//            return;
//        }
//
//
//        // Use updateChildren for atomic write for both sides of contact save
//        Map<String, Object> contactUpdates = new HashMap<>();
//        contactUpdates.put("/Contacts/" + sendUserID + "/" + receiverUserId + "/request_type", "accepted"); // Mark as accepted for sender
//        contactUpdates.put("/Contacts/" + receiverUserId + "/" + sendUserID + "/request_type", "accepted"); // Mark as accepted for receiver
//
//        // Add other necessary fields to Contact node if your Contacts model requires them, e.g.:
//        // contactUpdates.put("/Contacts/" + sendUserID + "/" + receiverUserId + "/uid", receiverUserId);
//        // contactUpdates.put("/Contacts/" + receiverUserId + "/" + sendUserID + "/uid", sendUserID);
//        // You might need to fetch user details here if not already stored in the Contacts node when the request was sent.
//        // For simplicity here, assuming "request_type": "accepted" is sufficient for Contact node.
//        // Check your Firebase "Contacts" structure to confirm.
//
//        FirebaseDatabase.getInstance().getReference().updateChildren(contactUpdates)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful())
//                        {
//                            Log.d(TAG, "Contacts updated successfully for both sides.");
//                            // Now remove the chat request nodes for both sides
//                            chatRequestRef.child(sendUserID).child(receiverUserId)
//                                    .removeValue()
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful())
//                                            {
//                                                chatRequestRef.child(receiverUserId).child(sendUserID)
//                                                        .removeValue()
//                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                // This runs on the main thread AFTER the final Firebase write completes for REMOVING THE REQUEST
//                                                                if(task.isSuccessful())
//                                                                {
//                                                                    Log.d(TAG, "Chat request removed from both sides.");
//                                                                    SendMessageRequestButton.setEnabled(true);
//                                                                    Current_State = "friends"; // Update state to friends
//                                                                    SendMessageRequestButton.setText("Remove this Contact"); // Update button text
//
//                                                                    DeclineRequestButton.setVisibility(View.INVISIBLE);
//                                                                    DeclineRequestButton.setEnabled(false);
//
//                                                                    Toast.makeText(ProfileUserInfoActivity.this, "Request Accepted", Toast.LENGTH_SHORT).show();
//
//
//                                                                    // *** NEW: Send OneSignal Push Notification to the ORIGINAL SENDER (sendUserID) ***
//                                                                    // Notify the user who INITIALLY SENT the request that it has been ACCEPTED.
//                                                                    // The current user (receiverUserId in this Activity) is the one ACCEPTING.
//                                                                    // We need the accepting user's name (receiverUserName) for the notification content.
//                                                                    if (oneSignalApiService != null && !TextUtils.isEmpty(receiverUserName)) {
//                                                                        Log.d(TAG, "Initiating OneSignal Push Notification for accepted friend request.");
//                                                                        String title = "Friend Request Accepted";
//                                                                        // Use the name of the person who ACCEPTED the request (which is receiverUserName in this context)
//                                                                        String content = receiverUserName + " accepted your friend request.";
//                                                                        // Call helper: recipient is sendUserID, acting user is receiverUserId, type is accepted
//                                                                        sendFriendRequestPushNotification(
//                                                                                oneSignalApiService,
//                                                                                sendUserID, // Recipient (original sender)
//                                                                                title,
//                                                                                content,
//                                                                                receiverUserId, // Acting user (the accepter)
//                                                                                "friend_request_accepted" // Notification type identifier
//                                                                        );
//                                                                    } else {
//                                                                        Log.e(TAG, "OneSignalApiService or receiverUserName is null/empty. Cannot send friend request accepted notification.");
//                                                                    }
//                                                                    // *** END NEW ***
//
//
//                                                                } else {
//                                                                    Log.e(TAG, "Failed to remove chat request from receiver side.", task.getException());
//                                                                    Toast.makeText(ProfileUserInfoActivity.this, "Failed to remove request fully.", Toast.LENGTH_SHORT).show();
//                                                                    SendMessageRequestButton.setEnabled(true); // Re-enable
//                                                                    // State might be inconsistent, ManageChatRequest listener might fix
//                                                                }
//                                                            }
//                                                        });
//                                            } else {
//                                                Log.e(TAG, "Failed to remove chat request from sender side.", task.getException());
//                                                Toast.makeText(ProfileUserInfoActivity.this, "Failed to remove request.", Toast.LENGTH_SHORT).show();
//                                                SendMessageRequestButton.setEnabled(true); // Re-enable
//                                                // State might be inconsistent
//                                            }
//                                        }
//                                    });
//                        } else {
//                            Log.e(TAG, "Failed to update contacts node for accept.", task.getException());
//                            Toast.makeText(ProfileUserInfoActivity.this, "Failed to accept request.", Toast.LENGTH_SHORT).show();
//                            SendMessageRequestButton.setEnabled(true); // Re-enable
//                            // State remains "request_received" or might be inconsistent
//                        }
//                    }
//                });
//    }
//
//
//    // *** MODIFIED HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION ***
//    private void sendFriendRequestPushNotification(OneSignalApiService apiService,
//                                                   String recipientFirebaseUID, String title, String messageContent,
//                                                   String actingUserFirebaseUID, // The UID of the user who performed the action (sent or accepted)
//                                                   String notificationType) { // Identifier for the type of notification (e.g., "friend_request", "friend_request_accepted")
//
//        if (apiService == null || TextUtils.isEmpty(recipientFirebaseUID) || TextUtils.isEmpty(notificationType)) {
//            Log.e(TAG, "sendFriendRequestPushNotification: API service null, recipient UID empty, or notificationType empty.");
//            return;
//        }
//        // actingUserFirebaseUID can be null/empty if you don't need it in the data payload for some types
//
//        Log.d(TAG, "Preparing OneSignal push notification type '" + notificationType + "' to recipient UID (External ID): " + recipientFirebaseUID);
//
//        JsonObject notificationBody = new JsonObject();
//        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID); // Use your OneSignal App ID (class member)
//
//        // Specify recipients using External User IDs (Firebase UIDs)
//        JsonArray externalUserIds = new JsonArray();
//        externalUserIds.add(recipientFirebaseUID); // Add the recipient's Firebase UID
//        notificationBody.add("include_external_user_ids", externalUserIds); // Use include_external_user_ids
//
//        // Add Notification Title and Content
//        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", title))); // Title passed
//        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", messageContent))); // Content passed
//
//        // Add custom data (important for handling notification click in the app)
//        JsonObject data = new JsonObject();
//        // Add notification type identifier
//        data.addProperty("notificationType", notificationType);
//        // Add relevant IDs based on the type
//        if (!TextUtils.isEmpty(actingUserFirebaseUID)) {
//            data.addProperty("actingUserId", actingUserFirebaseUID); // Who performed the action
//            // Add more specific IDs based on type if needed by your handler
//            if ("friend_request".equals(notificationType)) {
//                data.addProperty("senderId", actingUserFirebaseUID); // Redundant but clear
//                // data.addProperty("recipientId", recipientFirebaseUID); // The handler knows this already
//            } else if ("friend_request_accepted".equals(notificationType)) {
//                data.addProperty("accepterId", actingUserFirebaseUID); // The user who accepted
//                data.addProperty("originalSenderId", recipientFirebaseUID); // The user whose request was accepted
//            }
//        } else {
//            Log.w(TAG, "sendFriendRequestPushNotification: actingUserFirebaseUID is empty for type " + notificationType);
//            // Still include notificationType
//        }
//
//        // Example: To open a specific fragment or activity on click, add identifiers here.
//        // For a friend request/acceptance, maybe you want to open the chat fragment,
//        // or the profile page of the acting user, or the friend list.
//        // data.addProperty("targetChatPartnerId", actingUserFirebaseUID); // Maybe open chat with them?
//        // data.addProperty("targetProfileId", actingUserFirebaseUID); // Maybe open their profile?
//
//
//        notificationBody.add("data", data);
//
//        // Optional: Set small icon (recommended)
//        notificationBody.addProperty("small_icon", "app_icon_circleup"); // Use your icon's resource name (string)
//
//        // Optional: Customize notification appearance (sound, vibration, etc.)
//        // notificationBody.addProperty("sound", "default");
//
//
//        // Make the API call asynchronously
//        Log.d(TAG, "Making OneSignal API call for '" + notificationType + "' to recipient: " + recipientFirebaseUID);
//        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "OneSignal API call successful for '" + notificationType + "' to " + recipientFirebaseUID + ". Response Code: " + response.code());
//                    // Log response body for debugging success/failure
//                    try (ResponseBody responseBody = response.body()) {
//                        String resBody = responseBody != null ? responseBody.string() : "N/A";
//                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read success response body ('" + notificationType + "')", e);
//                    }
//                } else {
//                    Log.e(TAG, "OneSignal API call failed for '" + notificationType + "' to " + recipientFirebaseUID + ". Response Code: " + response.code());
//                    // Log error body for debugging failure reason
//                    try (ResponseBody errorBody = response.errorBody()) {
//                        String errBody = errorBody != null ? errorBody.string() : "N/A";
//                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read error response body ('" + notificationType + "')", e);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.e(TAG, "OneSignal API call failed (network error) for '" + notificationType + "' to " + recipientFirebaseUID, t);
//            }
//        });
//        Log.d(TAG, "OneSignal API call enqueued for '" + notificationType + "' to recipient: " + recipientFirebaseUID);
//    }
//    // *** END MODIFIED HELPER METHOD ***
//
//
//
//
//
//
////    private void SendChatRequest() {
////        chatRequestRef.child(sendUserID).child(receiverUserId)
////                .child("request_type").setValue("sent")
////                .addOnCompleteListener(new OnCompleteListener<Void>() {
////                    @Override
////                    public void onComplete(@NonNull Task<Void> task) {
////
////                        if(task.isSuccessful())
////                        {
////                            chatRequestRef.child(receiverUserId).child(sendUserID)
////                                    .child("request_type").setValue("received")
////                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
////                                        @Override
////                                        public void onComplete(@NonNull Task<Void> task) {
////                                            if(task.isSuccessful())
////                                            {
////
////                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
////                                                chatNotificationMap.put("from", sendUserID);
////                                                chatNotificationMap.put("type", "request");
////
////                                                notificationRef.child(receiverUserId).push()
////                                                        .setValue(chatNotificationMap)
////                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
////                                                            @Override
////                                                            public void onComplete(@NonNull Task<Void> task) {
////                                                                if(task.isSuccessful())
////                                                                {
////                                                                    SendMessageRequestButton.setEnabled(true);
////                                                                    Current_State = "request_sent";
////                                                                    SendMessageRequestButton.setText("Cancel Chat Request");
////                                                                }
////
////                                                            }
////                                                        });
////
////
////                                            }
////
////                                        }
////                                    });
////                        }
////
////                    }
////                });
////    }
//
//
//
//
//    private void SendChatRequest() {
//        Log.d(TAG, "Sending chat request to: " + receiverUserId);
//        // Use updateChildren for atomic write of both sides of the request
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("/Chat Requests/" + sendUserID + "/" + receiverUserId + "/request_type", "sent"); // Request from current user to receiver
//        updates.put("/Chat Requests/" + receiverUserId + "/" + sendUserID + "/request_type", "received"); // Request from receiver to current user
//
//        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
//                .addOnCompleteListener(task -> {
//                    SendMessageRequestButton.setEnabled(true); // Re-enable button regardless of success/failure
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "Chat request sent to Firebase.");
//                        Current_State = "request_sent"; // Update local state
//                        SendMessageRequestButton.setText("Cancel Chat Request"); // Update button text
//                        DeclineRequestButton.setVisibility(View.INVISIBLE);
//                        DeclineRequestButton.setEnabled(false);
//                        Toast.makeText(ProfileUserInfoActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
//
//                        // *** NEW: Send OneSignal Push Notification to the RECIPIENT (receiverUserId) ***
//                        // Notify the user who RECEIVED the request.
//                        if (oneSignalApiService != null) {
//                            Log.d(TAG, "Initiating OneSignal Push Notification for friend request.");
//                            String title = "New Friend Request";
//                            String content = (senderUserName != null && !senderUserName.isEmpty()) ? senderUserName + " sent you a friend request." : "You received a new friend request."; // Use fetched sender name
//                            sendFriendRequestPushNotification(oneSignalApiService, receiverUserId, title, content, sendUserID); // Call helper method
//                        } else {
//                            Log.e(TAG, "OneSignalApiService is null. Cannot send friend request push notification.");
//                        }
//                        // *** END NEW ***
//
//                        // Removed the old Firebase notificationRef logic here.
//
//                    } else {
//                        Log.e(TAG, "Failed to send chat request to Firebase", task.getException());
//                        Toast.makeText(ProfileUserInfoActivity.this, "Failed to send request.", Toast.LENGTH_SHORT).show();
//                        // State might need to be manually reset or re-fetched if updateChildren fails
//                        // ManageChatRequest(); // Re-fetch state
//                    }
//                    // The ValueEventListener on chatRequestRef in ManageChatRequest will likely update the UI state after changes sync anyway.
//                });
//    }
//
//
//
//
//
//
//
//     *** NEW HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION FOR FRIEND REQUEST ***
//    private void sendFriendRequestPushNotification(OneSignalApiService apiService,
//                                                   String recipientFirebaseUID, String title, String messageContent,
//                                                   String senderFirebaseUID) { // No need for messageId/conversationId here unless deep linking to requests is different
//
//        if (apiService == null || TextUtils.isEmpty(recipientFirebaseUID)) {
//            Log.e(TAG, "sendFriendRequestPushNotification: API service is null or recipient UID is empty.");
//            return;
//        }
//
//        Log.d(TAG, "Preparing OneSignal push notification for friend request to recipient UID (External ID): " + recipientFirebaseUID);
//
//        JsonObject notificationBody = new JsonObject();
//        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID); // Use your OneSignal App ID (class member)
//
//        // Specify recipients using External User IDs (Firebase UIDs)
//        JsonArray externalUserIds = new JsonArray();
//        externalUserIds.add(recipientFirebaseUID); // Add the recipient's Firebase UID
//        notificationBody.add("include_external_user_ids", externalUserIds); // Use include_external_user_ids
//
//        // Add Notification Title and Content
//        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", title))); // Title passed
//        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", messageContent))); // Content passed
//
//        // Add custom data (important for handling notification click in the app)
//        JsonObject data = new JsonObject();
//        data.addProperty("senderId", senderFirebaseUID); // Add sender's UID so recipient knows who sent it
//        // Add a specific key to identify this as a friend request notification
//        data.addProperty("notificationType", "friend_request");
//        // You might want to pass receiverUserId too, although the recipient knows they are the receiver
//        // data.addProperty("recipientId", recipientFirebaseUID);
//        notificationBody.add("data", data);
//
//        // Optional: Set small icon (recommended)
//        notificationBody.addProperty("small_icon", "app_icon_circleup"); // Use your icon's resource name (string)
//
//        // Optional: Customize notification appearance (sound, vibration, etc.)
//
//
//        // Make the API call asynchronously
//        Log.d(TAG, "Making OneSignal API call for friend request to recipient: " + recipientFirebaseUID);
//        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "OneSignal API call successful for friend request to " + recipientFirebaseUID + ". Response Code: " + response.code());
//                    // Log response body for debugging success/failure
//                    try (ResponseBody responseBody = response.body()) {
//                        String resBody = responseBody != null ? responseBody.string() : "N/A";
//                        Log.d(TAG, "OneSignal API Response Body: " + resBody);
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read success response body (friend request)", e);
//                    }
//                } else {
//                    Log.e(TAG, "OneSignal API call failed for friend request to " + recipientFirebaseUID + ". Response Code: " + response.code());
//                    // Log error body for debugging failure reason
//                    try (ResponseBody errorBody = response.errorBody()) {
//                        String errBody = errorBody != null ? errorBody.string() : "N/A";
//                        Log.e(TAG, "OneSignal API Error Body: " + errBody);
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to read error response body (friend request)", e);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.e(TAG, "OneSignal API call failed (network error) for friend request to " + recipientFirebaseUID, t);
//            }
//        });
//        Log.d(TAG, "OneSignal API call enqueued for friend request to recipient: " + recipientFirebaseUID);
//    }
//    // *** END NEW HELPER METHOD ***
//
//
//    private void CancelChatRequest() {
//        chatRequestRef.child(sendUserID).child(receiverUserId)
//                .removeValue()
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful())
//                        {
//                            chatRequestRef.child(receiverUserId).child(sendUserID)
//                                    .removeValue()
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful())
//                                            {
//                                                SendMessageRequestButton.setEnabled(true);
//                                                Current_State = "new";
//                                                SendMessageRequestButton.setText("Send Message");
//
//                                                DeclineRequestButton.setVisibility(View.INVISIBLE);
//                                                DeclineRequestButton.setEnabled(false);
//                                            }
//
//                                        }
//                                    });
//                        }
//
//                    }
//                });
//    }
//}


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


// --- Existing Imports ---

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.io.IOException; // Added for errorBody().string()
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileUserInfoActivity extends AppCompatActivity {

    private static final String TAG = "ProfileUserInfoAct"; // Define TAG for logging

    private String receiverUserId, sendUserID; // sendUserID is the current user viewing the profile
    private String Current_State; // Represents the chat/contact state between sender and receiver

    // UI Elements
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageRequestButton, DeclineRequestButton;

    // Firebase References
    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef; // notificationRef might be old Firebase DB notification logic
    private FirebaseAuth mAuth;

    // User Info
    private String senderUserName; // <<< Stores the current user's name (the one sending/accepting)
    private String receiverUserName; // <<< Stores the user's name we are viewing (the recipient/original sender)
    // Removed userNameGlobal and userImageBase64Global as they seem unused now.


    // *** NEW MEMBER: Retrofit Service for OneSignal API ***
    private OneSignalApiService oneSignalApiService;
    // Get this from your OneSignal Dashboard -> Settings -> Keys & IDs
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07"; // <<< REPLACE WITH YOUR ACTUAL APP ID
    // *** END NEW MEMBER ***


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user_info); // Ensure this layout exists

        Log.d(TAG, "🟢 ProfileUserInfoActivity launched");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get current user

        // Check if user is authenticated. Redirect to login if not.
        if (currentUser == null) {
            Log.w(TAG, "No current user authenticated. Redirecting to Login.");
            // Assuming you have a Login activity class named 'Login'
            Intent loginIntent = new Intent(ProfileUserInfoActivity.this, Login.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(loginIntent);
            finish();
            return; // Stop further execution
        }

        sendUserID = currentUser.getUid(); // Get current user ID (this user is the sender of the accept/decline action)

        // Initialize Firebase References
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications"); // Keep if still used for old Firebase DB notifications


        // Safely get receiverUserId from Intent, check for null/empty
        // receiverUserId is the ID of the user whose profile we are viewing (the user who sent the request initially)
        receiverUserId = getIntent().getStringExtra("visit_user_id");
        if (TextUtils.isEmpty(receiverUserId)) {
            Log.e(TAG, "Error: Receiver User ID missing from Intent!");
            Toast.makeText(this, "Error loading profile: Missing user ID.", Toast.LENGTH_SHORT).show();
            finish(); // Cannot proceed without receiver ID
            return; // Stop further execution
        }
        Log.d(TAG, "Viewing profile for UID: " + receiverUserId);


        // Initialize UI Elements
        userProfileImage = findViewById(R.id.visit_profile_img); // Ensure this ID exists
        userProfileName = findViewById(R.id.visit_user_name); // Ensure this ID exists
        userProfileStatus = findViewById(R.id.visit_user_status); // Ensure this ID exists
        SendMessageRequestButton = findViewById(R.id.sendmsg_request_btn); // Ensure this ID exists
        DeclineRequestButton = findViewById(R.id.declinemsg_request_btn); // Ensure this ID exists

        // Ensure buttons are not null before proceeding
        if (userProfileImage == null || userProfileName == null || userProfileStatus == null || SendMessageRequestButton == null || DeclineRequestButton == null) {
            Log.e(TAG, "CRITICAL ERROR: One or more UI elements not found in layout!");
            Toast.makeText(this, "Layout error.", Toast.LENGTH_SHORT).show();
            finish();
            return; // Stop if UI is not complete
        }


        Current_State = "new"; // Initial state


        // *** Initialize Retrofit Service for OneSignal API ***
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
            // Handle this error - perhaps disable send request button if notifications are critical
            // We'll check `oneSignalApiService != null` before attempting to send notifications.
            // Buttons are enabled/disabled later in ManageChatRequest.
        }
        // *** END NEW ***


        // Retrieve user info - this fetches both receiver and sender names now and sets up the UI state
        RetrieveUserInfo();
        Log.d(TAG, "📲 onCreate finished in ProfileUserInfoActivity");
    }


    private void RetrieveUserInfo() {
        // Ensure receiverUserId is not null/empty before querying
        if (TextUtils.isEmpty(receiverUserId) || userRef == null || TextUtils.isEmpty(sendUserID)) {
            Log.e(TAG, "RetrieveUserInfo: receiverUserId, sendUserID, or userRef is null/empty.");
            // Error toast already shown in onCreate if receiverUserId was missing
            // Disable buttons if we can't load user info or identify current user
            if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(false);
            if (DeclineRequestButton != null) {
                DeclineRequestButton.setVisibility(View.INVISIBLE);
                DeclineRequestButton.setEnabled(false);
            }
            return;
        }

        Log.d(TAG, "Retrieving user info for UID: " + receiverUserId);
        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This runs on the main thread
                if (snapshot.exists()) {
                    String userName = snapshot.child("username").getValue(String.class);
                    String userStatus = snapshot.child("status").getValue(String.class);
                    String userImageBase64 = snapshot.child("profileImage").getValue(String.class);

                    // *** Store Receiver's Name (the user whose profile we are viewing) ***
                    receiverUserName = !TextUtils.isEmpty(userName) ? userName : "Unknown User";
                    Log.d(TAG, "Fetched receiver name: " + receiverUserName);
                    // *** END NEW ***

                    userProfileName.setText(receiverUserName); // Set the name of the profile being viewed

                    // --- Check if userStatus is empty or null before setting ---
                    if (TextUtils.isEmpty(userStatus)) {
                        userProfileStatus.setText("No Status Uploaded");
                    } else {
                        userProfileStatus.setText(userStatus); // Set the actual status if not empty
                    }

                    // Load profile image from Base64
                    if (userImageBase64 != null && !userImageBase64.isEmpty()) {
                        try {
                            // Use android.util.Base64 consistently
                            byte[] decodedString = android.util.Base64.decode(userImageBase64, android.util.Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            userProfileImage.setImageBitmap(decodedBitmap);
                            Log.d(TAG, "Loaded receiver profile image from Base64.");
                        } catch (IllegalArgumentException e) { // Specific catch for Base64 errors
                            Log.e(TAG, "Error decoding Base64 image: Invalid Base64 string", e);
                            userProfileImage.setImageResource(R.drawable.default_profile_img); // Default on error
                        } catch (Exception e) {
                            Log.e(TAG, "Error decoding image", e);
                            userProfileImage.setImageResource(R.drawable.default_profile_img); // Default on error
                        }
                    } else {
                        userProfileImage.setImageResource(R.drawable.default_profile_img); // Default if data is null/empty
                        Log.d(TAG, "No receiver profile image found or data is empty. Using default.");
                    }




                    // *** START NEW: Set OnClickListener for the Profile Image ***
                    if (userProfileImage != null) { // Ensure the view was found
                        // Only make it clickable if an image *might* be displayable (either loaded from DB or default)
                        // If snapshot exists, the user exists, so the image view area is relevant.
                        userProfileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "Profile image clicked for UID: " + receiverUserId);
                                // Create the Intent to launch FullscreenImageActivity
                                Intent fullScreenIntent = new Intent(ProfileUserInfoActivity.this, FullscreenImageActivity.class);
                                // Pass the Base64 string of the profile image
                                // Use the 'userImageBase64' variable retrieved from the snapshot
                                fullScreenIntent.putExtra("profileImage", userImageBase64); // Pass the Base64 string (can be null/empty)

                                // *** NEW: Indicate to FullscreenImageActivity to hide the Edit button ***
                                // When viewing another user's profile, the edit button should be hidden.
                                fullScreenIntent.putExtra("hideEditButton", true);
                                // *** END NEW ***

                                // Start the activity
                                startActivity(fullScreenIntent);
                            }
                        });
                        Log.d(TAG, "Profile image click listener set.");
                    } else {
                        Log.w(TAG, "userProfileImage is null, cannot set click listener.");
                    }
                    // *** END NEW: Set OnClickListener ***




                    // --- Fetch Current User's Name (Sender) AFTER receiver info is loaded ---
                    // We need the current user's name for notifications (as the sender of the request or the accepter).
                    // This happens asynchronously. The button click handlers will use this variable.
                    fetchCurrentUserName();
                    // --- END Fetch Current User's Name ---


                    // *** IMPORTANT: Set up UI based on whether it's the user's own profile or someone else's ***
                    if (sendUserID.equals(receiverUserId)) {
                        Log.d(TAG, "Viewing own profile.");
                        SendMessageRequestButton.setText("Your Profile"); // Clearer text
                        SendMessageRequestButton.setEnabled(false); // Cannot send message request to self
                        DeclineRequestButton.setVisibility(View.INVISIBLE); // Hide the Decline button
                        DeclineRequestButton.setEnabled(false); // Disable the Decline button

                        // Optional: Add a different click listener for the "Your Profile" button if needed
                         /*
                        SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View view) {
                                 Toast.makeText(ProfileUserInfoActivity.this, "This is your profile.", Toast.LENGTH_SHORT).show();
                             }
                        });
                         */
                    }
                    // Call ManageChatRequest to set up other buttons based on chat state IF it's not the user's own profile
                    else {
                        Log.d(TAG, "Viewing another user's profile. Setting up chat request management.");
                        ManageChatRequest();
                    }


                } else {
                    Log.w(TAG, "User data not found in Firebase for UID: " + receiverUserId);
                    Toast.makeText(ProfileUserInfoActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    // Disable buttons and hide decline if user data is missing
                    if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(false);
                    if (DeclineRequestButton != null) {
                        DeclineRequestButton.setVisibility(View.INVISIBLE);
                        DeclineRequestButton.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load user data from Firebase for UID: " + receiverUserId, error.toException());
                Toast.makeText(ProfileUserInfoActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                // Disable buttons on error loading profile
                if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(false);
                if (DeclineRequestButton != null) {
                    DeclineRequestButton.setVisibility(View.INVISIBLE);
                    DeclineRequestButton.setEnabled(false);
                }
            }
        });
    }


    // *** NEW HELPER METHOD TO FETCH CURRENT USER'S NAME (SENDER/ACCEPTER) ***
    private void fetchCurrentUserName() {
        // Ensure sendUserID and userRef are initialized before querying
        if (TextUtils.isEmpty(sendUserID) || userRef == null) {
            Log.w(TAG, "fetchCurrentUserName: sendUserID or userRef is empty/null, cannot fetch name.");
            senderUserName = "A User"; // Default sender name if UID or ref is missing
            return;
        }
        Log.d(TAG, "Fetching current user's (sender/accepter) name for UID: " + sendUserID);
        userRef.child(sendUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This runs on the main thread
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    String name = snapshot.child("username").getValue(String.class);
                    if (!TextUtils.isEmpty(name)) {
                        senderUserName = name; // Store the current user's name
                        Log.d(TAG, "Fetched current user name: " + senderUserName);
                    } else {
                        senderUserName = "A User"; // Default if username field is empty in DB
                        Log.w(TAG, "Current user's username field is empty. Using default name.");
                    }
                } else {
                    senderUserName = "A User"; // Default if user data or username field is missing in DB
                    Log.w(TAG, "Current user data or username field not found for UID: " + sendUserID + ". Using default name.");
                }
                // senderUserName is now populated for potential use in notifications triggered later by button clicks.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch current user name from DB for UID: " + sendUserID, error.toException());
                senderUserName = "A User"; // Default on error fetching
            }
        });
    }
    // *** END NEW HELPER METHOD ***


    private void ManageChatRequest() {
        // This method should only run if viewing another user's profile,
        // which is handled by the check in RetrieveUserInfo before calling this.
        // Add a safety check anyway.
        if (sendUserID.equals(receiverUserId) || TextUtils.isEmpty(sendUserID) || TextUtils.isEmpty(receiverUserId) || chatRequestRef == null || contactsRef == null) {
            Log.w(TAG, "ManageChatRequest called for self-profile or with missing IDs/refs. Aborting.");
            return;
        }

        Log.d(TAG, "Managing chat request state for sender " + sendUserID + " to receiver " + receiverUserId);

        // Listen to the chat request node from the sender's perspective (this user's perspective)
        chatRequestRef.child(sendUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // This runs on the main thread
                        // Ensure receiverUserId is valid before checking snapshot children
                        if (TextUtils.isEmpty(receiverUserId)) {
                            Log.e(TAG, "ManageChatRequest listener onDataChange: receiverUserId is null/empty. Cannot proceed.");
                            // Potentially disable buttons if state cannot be determined
                            if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(false);
                            if (DeclineRequestButton != null) {
                                DeclineRequestButton.setVisibility(View.INVISIBLE);
                                DeclineRequestButton.setEnabled(false);
                            }
                            return;
                        }


                        if(snapshot.hasChild(receiverUserId)) {
                            // A request node exists involving sendUserID and receiverUserId
                            // Check the type of request from THIS user's perspective (sendUserID) to the OTHER user (receiverUserId)
                            String request_type = snapshot.child(receiverUserId).child("request_type").getValue(String.class); // Safely get String value

                            if("sent".equals(request_type)) { // If the type is "sent", this user (sendUserID) sent the request to receiverUserId
                                Log.d(TAG, "State is 'request_sent'.");
                                Current_State = "request_sent";
                                SendMessageRequestButton.setText("Cancel Chat Request");
                                SendMessageRequestButton.setVisibility(View.VISIBLE); // Ensure it's visible
                                DeclineRequestButton.setVisibility(View.INVISIBLE); // Hide Decline button
                                DeclineRequestButton.setEnabled(false); // Disable Decline button

                            } else if("received".equals(request_type)) { // If the type is "received", this user (sendUserID) received the request from receiverUserId
                                Log.d(TAG, "State is 'request_received'.");
                                Current_State = "request_received";
                                SendMessageRequestButton.setText("Accept Chat Request");
                                SendMessageRequestButton.setVisibility(View.VISIBLE); // Ensure it's visible

                                DeclineRequestButton.setVisibility(View.VISIBLE); // Show Decline button
                                DeclineRequestButton.setEnabled(true); // Enable Decline button

                                // Set the click listener for the Decline button here or once outside, ensuring it's active in this state.
                                // Setting inside ensures it's only active in this state from this listener update.
                                if (DeclineRequestButton != null) {
                                    DeclineRequestButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d(TAG, "Decline button clicked. Current_State: " + Current_State);
                                            // Ensure the button is enabled and the state is correct before acting
                                            if (DeclineRequestButton.isEnabled() && "request_received".equals(Current_State)) {
                                                DeclineRequestButton.setEnabled(false); // Disable while action is processed
                                                CancelChatRequest(); // Decline action is essentially cancelling the received request
                                            } else {
                                                // Should not happen if listener logic is correct or if button disabled quickly
                                                Log.w(TAG, "Decline button clicked while disabled or in unexpected state: " + Current_State);
                                            }
                                        }
                                    });
                                }


                            }
                        } else {
                            // No request node from sendUserID to receiverUserId exists.
                            // Check if they are contacts (friends) from the sender's perspective.
                            Log.d(TAG, "No request node found from sender to receiver. Checking Contacts...");
                            contactsRef.child(sendUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            // This runs on the main thread AFTER the single event listener finishes
                                            if (TextUtils.isEmpty(receiverUserId)) {
                                                Log.e(TAG, "ManageChatRequest contactsRef onDataChange: receiverUserId is null/empty. Cannot proceed.");
                                                // Potentially disable buttons on error
                                                if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(false);
                                                if (DeclineRequestButton != null) {
                                                    DeclineRequestButton.setVisibility(View.INVISIBLE);
                                                    DeclineRequestButton.setEnabled(false);
                                                }
                                                return;
                                            }
                                            if(snapshot.hasChild(receiverUserId)) {
                                                // A contact node exists from sendUserID to receiverUserId
                                                Log.d(TAG, "State is 'friends'.");
                                                Current_State = "friends";
                                                SendMessageRequestButton.setText("Remove this contact");
                                                SendMessageRequestButton.setVisibility(View.VISIBLE); // Ensure visible
                                                DeclineRequestButton.setVisibility(View.INVISIBLE); // Hide Decline
                                                DeclineRequestButton.setEnabled(false); // Disable Decline
                                            } else {
                                                // Default state: no request, not friends. User can send a new request.
                                                Log.d(TAG, "State is 'new'.");
                                                Current_State = "new";
                                                SendMessageRequestButton.setText("Send Message"); // Button text for sending request
                                                SendMessageRequestButton.setVisibility(View.VISIBLE); // Ensure visible
                                                DeclineRequestButton.setVisibility(View.INVISIBLE); // Hide Decline
                                                DeclineRequestButton.setEnabled(false); // Disable Decline
                                            }
                                            // Ensure SendMessageRequestButton is enabled after state check allows it, unless API service failed
                                            // Check oneSignalApiService != null is needed if disabling button on API error
                                            // Also, don't enable if viewing own profile (already handled, but safety)
                                            if (!sendUserID.equals(receiverUserId) && oneSignalApiService != null) {
                                                if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true);
                                            } else {
                                                // Disable if API service is null or viewing own profile
                                                if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, "Firebase contactsRef listener cancelled: " + error.getMessage(), error.toException());
                                            Toast.makeText(ProfileUserInfoActivity.this, "Error managing contacts state.", Toast.LENGTH_SHORT).show();
                                            // Disable buttons on error if state cannot be determined
                                            if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                        // The button enablement logic is now primarily handled inside the contactsRef listener's onDataChange
                        // to ensure it's correctly set AFTER the state ('new' or 'friends') is determined.
                        // A final check here might be redundant but harmless:
                         /*
                        if (!sendUserID.equals(receiverUserId) && oneSignalApiService != null) {
                             if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true);
                         } else {
                             if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(false);
                         }
                         */
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase chatRequestRef listener cancelled: " + error.getMessage(), error.toException());
                        Toast.makeText(ProfileUserInfoActivity.this, "Error managing chat request state.", Toast.LENGTH_SHORT).show();
                        // Disable buttons on error if state cannot be determined
                        if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(false);
                    }
                });


        // Set the main click listener for SendMessageRequestButton ONCE outside the listener
        // This listener will call the appropriate method based on the 'Current_State' variable.
        // This listener is only for interactions with *other* users.
        if (!sendUserID.equals(receiverUserId) && SendMessageRequestButton != null) {
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Disable button immediately to prevent multiple clicks while processing
                    SendMessageRequestButton.setEnabled(false);
                    // Disable Decline button if it's visible/enabled as well
                    if (DeclineRequestButton != null && DeclineRequestButton.getVisibility() == View.VISIBLE && DeclineRequestButton.isEnabled()) {
                        DeclineRequestButton.setEnabled(false);
                    }
                    Log.d(TAG, "SendMessageRequestButton clicked. Current_State: " + Current_State);

                    // Act based on the current state
                    if ("new".equals(Current_State)) {
                        SendChatRequest(); // Method to send request
                    } else if ("request_sent".equals(Current_State)) {
                        CancelChatRequest(); // Method to cancel request
                    } else if ("request_received".equals(Current_State)) {
                        AcceptChatRequest(); // Method to accept request - THIS IS WHERE WE ADD NOTIFICATION
                    } else if ("friends".equals(Current_State)) {
                        // In "friends" state, the button likely says "Remove this contact"
                        RemoveSpecificContact(); // Method to remove contact
                    } else {
                        // Log if button clicked in an unexpected state
                        Log.w(TAG, "SendMessageRequestButton clicked in unhandled state: " + Current_State);
                        // Re-enable button(s) if state is unhandled
                        SendMessageRequestButton.setEnabled(true);
                        if (DeclineRequestButton != null && "request_received".equals(Current_State)) { // Only re-enable decline if it was supposed to be enabled
                            DeclineRequestButton.setEnabled(true);
                        }
                    }
                }
            });
        }
        // The click listener for the Decline button is set inside the ValueEventListener when Current_State becomes "request_received"
    }


    private void RemoveSpecificContact() {
        // Ensure IDs and references are valid before attempting Firebase ops
        if (TextUtils.isEmpty(sendUserID) || TextUtils.isEmpty(receiverUserId) || contactsRef == null) {
            Log.e(TAG, "RemoveSpecificContact: sendUserID, receiverUserId, or contactsRef is null/empty.");
            Toast.makeText(this, "Error removing contact.", Toast.LENGTH_SHORT).show();
            if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable button
            return;
        }

        Log.d(TAG, "Attempting to remove contact: Sender=" + sendUserID + ", Receiver=" + receiverUserId);

        // Remove contact node from current user's perspective (sendUserID -> receiverUserId)
        contactsRef.child(sendUserID).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Log.d(TAG, "Contact removed from sender's node (" + sendUserID + " -> " + receiverUserId + ").");
                            // Remove contact node from the other user's perspective (receiverUserId -> sendUserID)
                            contactsRef.child(receiverUserId).child(sendUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // This runs on the main thread AFTER the second remove operation completes
                                            if(task.isSuccessful())
                                            {
                                                Log.d(TAG, "Contact removed from receiver's node (" + receiverUserId + " -> " + sendUserID + ").");

                                                // *** Update UI State AFTER all Firebase ops succeed ***
                                                if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable button
                                                Current_State = "new"; // Go back to initial state
                                                if (SendMessageRequestButton != null) SendMessageRequestButton.setText("Send Message"); // Reset button text

                                                if (DeclineRequestButton != null) {
                                                    DeclineRequestButton.setVisibility(View.INVISIBLE);
                                                    DeclineRequestButton.setEnabled(false);
                                                }

                                                Toast.makeText(ProfileUserInfoActivity.this, "Contact Removed", Toast.LENGTH_SHORT).show();

                                            } else {
                                                Log.e(TAG, "Failed to remove contact from receiver side (" + receiverUserId + ").", task.getException());
                                                Toast.makeText(ProfileUserInfoActivity.this, "Failed to remove contact fully.", Toast.LENGTH_SHORT).show();
                                                if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable
                                                // State might be inconsistent, ManageChatRequest listener might fix
                                            }

                                        }
                                    });
                        } else {
                            Log.e(TAG, "Failed to remove contact from sender side (" + sendUserID + ").", task.getException());
                            Toast.makeText(ProfileUserInfoActivity.this, "Failed to remove contact.", Toast.LENGTH_SHORT).show();
                            if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable
                            // State remains "friends" or might be inconsistent
                        }
                    }
                });

    }


    private void AcceptChatRequest() {
        // Ensure IDs and references are valid before attempting Firebase ops
        if (TextUtils.isEmpty(sendUserID) || TextUtils.isEmpty(receiverUserId) || contactsRef == null || chatRequestRef == null) {
            Log.e(TAG, "AcceptChatRequest: sendUserID, receiverUserId, contactsRef, or chatRequestRef is null/empty.");
            Toast.makeText(this, "Error accepting request.", Toast.LENGTH_SHORT).show();
            if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable button
            if (DeclineRequestButton != null) DeclineRequestButton.setEnabled(true); // Re-enable decline button
            return;
        }

        Log.d(TAG, "Attempting to accept chat request: Accepter=" + sendUserID + ", Original Sender=" + receiverUserId);

        // Use updateChildren for atomic write for both sides of contact save
        // Assuming your Contacts node just needs the UID and request_type="accepted"
        // If it needs other fields like username or profileImage, you'd need to fetch them here
        // or rely on them being present from the initial user data load (receiver side)
        // and potentially fetch sender details for the receiver's Contacts entry.
        // For simplicity here, we'll just set the request_type.
        Map<String, Object> contactUpdates = new HashMap<>();
        // Add contact node for the accepter (sendUserID -> receiverUserId)
        // The accepter (current user) adds the original sender (receiverUserId) to their contacts
        contactUpdates.put("/Contacts/" + sendUserID + "/" + receiverUserId + "/request_type", "accepted"); // Mark as accepted for accepter
        // Add contact node for the original sender (receiverUserId -> sendUserID)
        // The original sender (receiverUserId) adds the accepter (sendUserID) to their contacts
        contactUpdates.put("/Contacts/" + receiverUserId + "/" + sendUserID + "/request_type", "accepted"); // Mark as accepted for original sender

        // Optional: Add display info to the contacts node if your Contacts model uses them.
        // This might require fetching sender's name and image for the receiver's contacts list
        // if those aren't stored elsewhere or consistently updated.
        // For the accepter's contacts list entry about the original sender (receiverUserId):
        // contactUpdates.put("/Contacts/" + sendUserID + "/" + receiverUserId + "/username", receiverUserName); // Use the fetched receiver name
        // contactUpdates.put("/Contacts/" + sendUserID + "/" + receiverUserId + "/profileImage", receiverUserImageBase64); // If you loaded this
        // For the original sender's contacts list entry about the accepter (sendUserID):
        // contactUpdates.put("/Contacts/" + receiverUserId + "/" + sendUserID + "/username", senderUserName); // Use the fetched sender name
        // contactUpdates.put("/Contacts/" + receiverUserId + "/" + sendUserID + "/profileImage", senderUserImageBase64); // If you loaded this


        FirebaseDatabase.getInstance().getReference().updateChildren(contactUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // This runs on the main thread AFTER the contact updates complete
                        if(task.isSuccessful())
                        {
                            Log.d(TAG, "Contacts updated successfully for both sides to 'accepted'. Now removing chat request nodes.");
                            // Now remove the chat request nodes for both sides
                            chatRequestRef.child(sendUserID).child(receiverUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // This runs on the main thread AFTER the first remove operation completes
                                            if(task.isSuccessful())
                                            {
                                                Log.d(TAG, "Accepter's chat request node removed (" + sendUserID + " -> " + receiverUserId + ").");
                                                chatRequestRef.child(receiverUserId).child(sendUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                // This runs on the main thread AFTER the final Firebase write completes for REMOVING THE ORIGINAL SENDER'S REQUEST NODE
                                                                if(task.isSuccessful())
                                                                {
                                                                    Log.d(TAG, "Original sender's chat request node removed (" + receiverUserId + " -> " + sendUserID + "). Request fully cleared.");

                                                                    // *** Update UI State AFTER all Firebase ops succeed ***
                                                                    if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable button
                                                                    if (DeclineRequestButton != null) DeclineRequestButton.setEnabled(true); // Re-enable decline button (before hiding)

                                                                    Current_State = "friends"; // Update state to friends
                                                                    if (SendMessageRequestButton != null) SendMessageRequestButton.setText("Remove this Contact"); // Update button text

                                                                    if (DeclineRequestButton != null) {
                                                                        DeclineRequestButton.setVisibility(View.INVISIBLE);
                                                                        DeclineRequestButton.setEnabled(false); // Ensure disabled when hidden
                                                                    }

                                                                    Toast.makeText(ProfileUserInfoActivity.this, "Request Accepted", Toast.LENGTH_SHORT).show();


                                                                    // *** NEW: Send OneSignal Push Notification to the ORIGINAL SENDER (receiverUserId) ***
                                                                    // Notify the user who INITIALLY SENT the request that it has been ACCEPTED.
                                                                    // The current user (sendUserID) is the one ACCEPTING.
                                                                    // We need the ACCEPTING user's name (senderUserName) for the notification content.
                                                                    if (oneSignalApiService != null && !TextUtils.isEmpty(senderUserName)) {
                                                                        Log.d(TAG, "Initiating OneSignal Push Notification for accepted friend request.");
                                                                        String title = "Friend Request Accepted";
                                                                        // Use the name of the person who ACCEPTED the request (which is senderUserName in this context)
                                                                        String content = senderUserName + " accepted your friend request.";
                                                                        // Call helper:
                                                                        // recipient is receiverUserId (the original sender of the request)
                                                                        // acting user is sendUserID (the accepter)
                                                                        sendFriendRequestPushNotification(
                                                                                oneSignalApiService,
                                                                                receiverUserId, // Recipient (original sender of the request)
                                                                                title,
                                                                                content,
                                                                                sendUserID, // Acting user (the accepter, who is the current user)
                                                                                "friend_request_accepted" // Notification type identifier
                                                                        );
                                                                    } else {
                                                                        Log.e(TAG, "OneSignalApiService or senderUserName is null/empty. Cannot send friend request accepted notification.");
                                                                        // Optionally show a warning to the user
                                                                        // Toast.makeText(ProfileUserInfoActivity.this, "Notification failed for accepter.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    // *** END NEW ***


                                                                } else {
                                                                    Log.e(TAG, "Failed to remove original sender's chat request node for accept (" + receiverUserId + ").", task.getException());
                                                                    Toast.makeText(ProfileUserInfoActivity.this, "Failed to remove request fully.", Toast.LENGTH_SHORT).show();
                                                                    if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable
                                                                    if (DeclineRequestButton != null) DeclineRequestButton.setEnabled(true); // Re-enable
                                                                    // State might be inconsistent, ManageChatRequest listener might fix
                                                                }
                                                            }
                                                        });
                                            } else {
                                                Log.e(TAG, "Failed to remove accepter's chat request node for accept (" + sendUserID + ").", task.getException());
                                                Toast.makeText(ProfileUserInfoActivity.this, "Failed to remove request.", Toast.LENGTH_SHORT).show();
                                                if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable
                                                if (DeclineRequestButton != null) DeclineRequestButton.setEnabled(true); // Re-enable
                                                // State might be inconsistent
                                            }
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Failed to update contacts node for accept.", task.getException());
                            Toast.makeText(ProfileUserInfoActivity.this, "Failed to accept request.", Toast.LENGTH_SHORT).show();
                            if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable
                            if (DeclineRequestButton != null) DeclineRequestButton.setEnabled(true); // Re-enable
                            // State remains "request_received" or might be inconsistent
                        }
                    }
                });
    }


    // *** MODIFIED HELPER METHOD TO SEND ONESIGNAL PUSH NOTIFICATION FOR FRIEND REQUEST/ACCEPTANCE ***
    // This method signature is the same as the one you already had in your code.
    /**
     * Sends a OneSignal push notification for friend request related events (sent or accepted).
     *
     * @param apiService          The Retrofit OneSignalApiService instance.
     * @param recipientFirebaseUID The Firebase UID of the user who should RECEIVE the notification.
     * @param title               The title of the notification.
     * @param messageContent      The main content/body of the notification.
     * @param actingUserFirebaseUID The Firebase UID of the user who PERFORMED the action (e.g., sender of request, accepter of request).
     * @param notificationType    A string identifier for the type of notification (e.g., "friend_request", "friend_request_accepted").
     */
    private void sendFriendRequestPushNotification(OneSignalApiService apiService,
                                                   String recipientFirebaseUID,
                                                   String title,
                                                   String messageContent,
                                                   String actingUserFirebaseUID, // The UID of the user who performed the action (sent or accepted)
                                                   String notificationType) { // Identifier for the type of notification (e.g., "friend_request", "friend_request_accepted")

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
        // but for friend requests/acceptances, it's crucial. We'll check it when building data payload.
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
            // The user who performed the action (e.g., sent the request, accepted the request)
            data.addProperty("actingUserId", actingUserFirebaseUID);

            // Add specific IDs based on type for clarity in the handler
            if ("friend_request".equals(notificationType)) {
                // When sending a request, the acting user is the sender.
                // Recipient is the receiver (the user whose profile is being viewed)
                data.addProperty("senderId", actingUserFirebaseUID); // The sender of the request
                data.addProperty("receiverId", recipientFirebaseUID); // The receiver of the request
            } else if ("friend_request_accepted".equals(notificationType)) {
                // When accepting a request, the acting user is the accepter (the current user).
                // The recipient is the *original sender* of the request (the user whose profile was viewed).
                data.addProperty("accepterId", actingUserFirebaseUID); // The user who accepted
                data.addProperty("originalSenderId", recipientFirebaseUID); // The user whose request was accepted
            }
            // Add more cases here if you introduce other friend-request related notifications (e.g., declined, removed)
        }

        // Example: If you want tapping the notification to take them to a specific fragment or activity on click, add identifiers here.
        // For a friend request/acceptance, maybe you want to open the chat fragment,
        // or the profile page of the acting user, or the friend list.
        // data.addProperty("targetScreen", "FriendListFragment"); // Or similar identifier your handler understands
        // data.addProperty("targetUserId", actingUserFirebaseUID); // Maybe open chat/profile with the acting user?
        // For an accepted request, perhaps opening the profile of the accepter (`actingUserFirebaseUID`)
        // or opening the chat screen with the accepter (which requires retrieving the conversationId)?
        // Let's add the acting user ID as target for now, your handler can decide what to do.
        if (!TextUtils.isEmpty(actingUserFirebaseUID)) {
            data.addProperty("targetUserId", actingUserFirebaseUID);
        } else if (!TextUtils.isEmpty(recipientFirebaseUID)) {
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
    // *** END MODIFIED HELPER METHOD ***


    // --- Modified SendChatRequest to use the updated helper ---
    private void SendChatRequest() {
        Log.d(TAG, "Sending chat request to: " + receiverUserId);
        // Ensure IDs and references are valid before attempting Firebase ops
        if (TextUtils.isEmpty(sendUserID) || TextUtils.isEmpty(receiverUserId) || chatRequestRef == null) {
            Log.e(TAG, "SendChatRequest: sendUserID, receiverUserId, or chatRequestRef is null/empty.");
            Toast.makeText(this, "Error sending request.", Toast.LENGTH_SHORT).show();
            if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable button
            return;
        }


        // Use updateChildren for atomic write of both sides of the request
        Map<String, Object> updates = new HashMap<>();
        // Set request type for sender (sendUserID -> receiverUserId) as "sent"
        updates.put("/Chat Requests/" + sendUserID + "/" + receiverUserId + "/request_type", "sent");
        // Set request type for receiver (receiverUserId -> sendUserID) as "received"
        updates.put("/Chat Requests/" + receiverUserId + "/" + sendUserID + "/request_type", "received");

        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                .addOnCompleteListener(task -> {
                    // This runs on the main thread
                    if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable button regardless of success/failure

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Chat request nodes written to Firebase successfully.");
                        Current_State = "request_sent"; // Update local state
                        if (SendMessageRequestButton != null) SendMessageRequestButton.setText("Cancel Chat Request"); // Update button text

                        if (DeclineRequestButton != null) {
                            DeclineRequestButton.setVisibility(View.INVISIBLE);
                            DeclineRequestButton.setEnabled(false);
                        }
                        Toast.makeText(ProfileUserInfoActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();

                        // *** NEW: Send OneSignal Push Notification to the RECIPIENT (receiverUserId) ***
                        // Notify the user who RECEIVED the request.
                        // Use the sender's name (senderUserName) for the notification content.
                        // Ensure API service is initialized and sender name is available
                        if (oneSignalApiService != null && !TextUtils.isEmpty(senderUserName)) {
                            Log.d(TAG, "Initiating OneSignal Push Notification for friend request.");
                            String title = "New Friend Request";
                            // Use the name of the person who SENT the request (which is senderUserName in this context)
                            String content = senderUserName + " sent you a friend request.";
                            // Call helper:
                            // recipient is receiverUserId (the user who receives the notification)
                            // acting user is sendUserID (the sender of the request)
                            sendFriendRequestPushNotification(
                                    oneSignalApiService,
                                    receiverUserId, // Recipient of the notification (the user whose profile is being viewed)
                                    title,
                                    content,
                                    sendUserID, // Acting user (the sender of the request, who is the current user)
                                    "friend_request" // Notification type identifier
                            );
                        } else {
                            Log.e(TAG, "OneSignalApiService or senderUserName is null/empty. Cannot send friend request push notification.");
                            // Optionally show a warning to the user
                            // Toast.makeText(ProfileUserInfoActivity.this, "Notification failed for receiver.", Toast.LENGTH_SHORT).show();
                        }
                        // *** END NEW ***

                        // Removed the old Firebase notificationRef logic here as OneSignal replaces it.

                    } else {
                        Log.e(TAG, "Failed to send chat request to Firebase", task.getException());
                        Toast.makeText(ProfileUserInfoActivity.this, "Failed to send request.", Toast.LENGTH_SHORT).show();
                        // State might need to be manually reset or re-fetched if updateChildren fails
                        // ManageChatRequest(); // Could call this to reset state after failure, but listener might handle it
                    }
                    // The ValueEventListener on chatRequestRef in ManageChatRequest will likely update the UI state after changes sync anyway.
                });
    }
    // --- End Modified SendChatRequest ---


    private void CancelChatRequest() {
        // Ensure IDs and references are valid before attempting Firebase ops
        if (TextUtils.isEmpty(sendUserID) || TextUtils.isEmpty(receiverUserId) || chatRequestRef == null) {
            Log.e(TAG, "CancelChatRequest: sendUserID, receiverUserId, or chatRequestRef is null/empty.");
            Toast.makeText(this, "Error cancelling request.", Toast.LENGTH_SHORT).show();
            if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable button
            if (DeclineRequestButton != null && "request_received".equals(Current_State)) DeclineRequestButton.setEnabled(true); // Re-enable decline button IF it was enabled
            return;
        }

        Log.d(TAG, "Attempting to cancel chat request: User=" + sendUserID + ", Target=" + receiverUserId);

        // Capture the state *before* initiating Firebase ops to determine if this was a Cancel (state was 'sent') or Decline (state was 'received') action for notification purposes.
        final String stateBeforeCancel = Current_State; // Store the state

        // Remove chat request node from sender's perspective (sendUserID -> receiverUserId)
        chatRequestRef.child(sendUserID).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // This runs on the main thread AFTER the first remove operation completes
                        if(task.isSuccessful())
                        {
                            Log.d(TAG, "User's chat request node removed (" + sendUserID + " -> " + receiverUserId + ").");
                            // Remove chat request node from the other user's perspective (receiverUserId -> sendUserID)
                            chatRequestRef.child(receiverUserId).child(sendUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // This runs on the main thread AFTER the final Firebase write completes
                                            if(task.isSuccessful())
                                            {
                                                Log.d(TAG, "Other user's chat request node removed (" + receiverUserId + " -> " + sendUserID + "). Request fully cancelled.");

                                                // *** Update UI State AFTER all Firebase ops succeed ***
                                                if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable button
                                                if (DeclineRequestButton != null) DeclineRequestButton.setEnabled(true); // Re-enable decline button (before hiding)

                                                Current_State = "new"; // Go back to initial state
                                                if (SendMessageRequestButton != null) SendMessageRequestButton.setText("Send Message"); // Reset button text

                                                if (DeclineRequestButton != null) {
                                                    DeclineRequestButton.setVisibility(View.INVISIBLE);
                                                    DeclineRequestButton.setEnabled(false); // Ensure disabled when hidden
                                                }

                                                Toast.makeText(ProfileUserInfoActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();

                                                // *** Optional: Send notification to the other user that the request was cancelled/declined ***
                                                // Only send notification if API service is available and names are fetched.
                                                if (oneSignalApiService != null && !TextUtils.isEmpty(senderUserName) && !TextUtils.isEmpty(receiverUserName)) {
                                                    String notiRecipient; // Who receives the notification?
                                                    String notiTitle;
                                                    String notiContent;
                                                    String notiActingUser; // Who performed the action?
                                                    String notiType;

                                                    if ("request_received".equals(stateBeforeCancel)) {
                                                        // This was a Decline action (current user received the request and declined it)
                                                        // Notify the original sender (receiverUserId) that the request was declined by the current user (sendUserID)
                                                        notiRecipient = receiverUserId; // The user who sent the request
                                                        notiTitle = "Friend Request Declined";
                                                        // Use the name of the user who declined (current user's name)
                                                        notiContent = senderUserName + " declined your friend request.";
                                                        notiActingUser = sendUserID; // The user who declined
                                                        notiType = "friend_request_declined";
                                                        Log.d(TAG, "Initiating OneSignal Push Notification for declined friend request.");

                                                    } else if ("request_sent".equals(stateBeforeCancel)) {
                                                        // This was a Cancel action (current user sent the request and cancelled it)
                                                        // Notify the receiver (receiverUserId) that the request was cancelled by the sender (sendUserID)
                                                        notiRecipient = receiverUserId; // The user who received the request
                                                        notiTitle = "Friend Request Cancelled";
                                                        // Use the name of the user who cancelled (current user's name)
                                                        notiContent = senderUserName + " cancelled the friend request.";
                                                        notiActingUser = sendUserID; // The user who cancelled
                                                        notiType = "friend_request_cancelled";
                                                        Log.d(TAG, "Initiating OneSignal Push Notification for cancelled friend request.");

                                                    } else {
                                                        Log.w(TAG, "CancelChatRequest completed from unexpected state: " + stateBeforeCancel + ". Skipping decline/cancel notification.");
                                                        return; // Don't send notification if state was unexpected
                                                    }

                                                    // Call the helper method
                                                    sendFriendRequestPushNotification(oneSignalApiService, notiRecipient, notiTitle, notiContent, notiActingUser, notiType);

                                                } else {
                                                    Log.w(TAG, "OneSignalApiService, senderUserName, or receiverUserName is null/empty. Cannot send decline/cancel notification.");
                                                }
                                                // *** End Optional Notification ***


                                            } else {
                                                Log.e(TAG, "Failed to remove other user's chat request node for cancel (" + receiverUserId + ").", task.getException());
                                                Toast.makeText(ProfileUserInfoActivity.this, "Failed to cancel request fully.", Toast.LENGTH_SHORT).show();
                                                if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable
                                                if (DeclineRequestButton != null) DeclineRequestButton.setEnabled(true); // Re-enable
                                                // State might be inconsistent
                                            }

                                        }
                                    });
                        } else {
                            Log.e(TAG, "Failed to remove user's chat request node for cancel (" + sendUserID + ").", task.getException());
                            Toast.makeText(ProfileUserInfoActivity.this, "Failed to cancel request.", Toast.LENGTH_SHORT).show();
                            if (SendMessageRequestButton != null) SendMessageRequestButton.setEnabled(true); // Re-enable
                            if (DeclineRequestButton != null) DeclineRequestButton.setEnabled(true); // Re-enable
                            // State might be inconsistent
                        }

                    }
                });
    }
}