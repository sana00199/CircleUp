//package com.sana.circleup; // Replace with your package name if different
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.text.TextUtils;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.app.ActivityCompat;
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.MutableData;
//import com.google.firebase.database.ServerValue;
//import com.google.firebase.database.Transaction;
//import com.google.firebase.database.ValueEventListener;
//import com.google.android.gms.tasks.Tasks; // Import Tasks for waiting
//
//// >>>>>>>>>>>>>>>> IMPORT YOUR SPECIFIC CLASSES HERE <<<<<<<<<<<<<<<<<<
//// Replace these with the actual package and class names in your project
//import com.sana.circleup.encryptionfiles.ChatIdUtil; // Your ChatIdUtil class
//import com.sana.circleup.encryptionfiles.CryptoUtils; // Your CryptoUtils class
//import com.sana.circleup.encryptionfiles.YourKeyManager; // Your YourKeyManager class
//// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit; // Import TimeUnit
//
//import javax.crypto.SecretKey; // Import SecretKey
//
//
//public class ScheduledMessageWorker extends Worker {
//
//    private static final String TAG = "ScheduledMsgWorker";
//
//    public ScheduledMessageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
//        super(context, workerParams);
//    }
//
//    @NonNull
//    @Override
//    public Result doWork() {
//        String msgId = getInputData().getString("msgId");
//
//        if (TextUtils.isEmpty(msgId)) {
//            Log.e(TAG, "Worker input data missing msgId.");
//            return Result.failure(); // Cannot proceed without message ID
//        }
//
//        DatabaseReference scheduledRef = FirebaseDatabase.getInstance()
//                .getReference("ScheduledMessages")
//                .child(msgId);
//
//        try {
//            // Synchronously fetch the scheduled message data from Firebase
//            // This blocks the worker thread until the data is fetched or timeout occurs.
//            // Timeout added for safety. Adjust timeout as needed.
//            Log.d(TAG, "Fetching scheduled message with ID: " + msgId);
//            DataSnapshot snapshot = Tasks.await(scheduledRef.get(), 30, TimeUnit.SECONDS);
//
//            ScheMsg msg = snapshot.getValue(ScheMsg.class);
//
//            if (msg == null) {
//                Log.e(TAG, "Scheduled message with ID " + msgId + " not found or data is null after fetch. Cleaning up.");
//                // Use a separate async call to remove, so doWork can return immediately after cleaning up
//                scheduledRef.removeValue()
//                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Cleaned up missing scheduled message " + msgId))
//                        .addOnFailureListener(e -> Log.e(TAG, "Failed to clean up missing scheduled message " + msgId, e));
//                return Result.failure(); // Indicate that the task failed
//            }
//
//            String senderId = msg.getSenderId();
//            List<String> receiverIds = msg.getReceiverIds(); // List of receiver IDs
//
//            if (TextUtils.isEmpty(senderId) || receiverIds == null || receiverIds.isEmpty()) {
//                Log.e(TAG, "Scheduled message " + msgId + " has invalid sender or receiver(s). Cleaning up.");
//                scheduledRef.removeValue()
//                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Cleaned up invalid scheduled message " + msgId))
//                        .addOnFailureListener(e -> Log.e(TAG, "Failed to clean up invalid scheduled message " + msgId, e));
//                return Result.failure(); // Indicate that the task failed
//            }
//
//            Log.d(TAG, "Processing scheduled message " + msgId + " from " + senderId + " to " + receiverIds.size() + " receivers.");
//
//            // Optional: Mark as being processed in ScheduledMessages node (useful for monitoring)
//            // scheduledRef.child("status").setValue("processing"); // You could add this if needed
//
//
//            // --- Loop through each receiver and send the message ---
//            // We will initiate async Firebase writes for each receiver.
//            // The outcome of individual writes doesn't block the loop or this doWork method directly.
//            // Tracking success/failure of each write to report in the final notification requires
//            // more complex async handling (e.g., using ListenableWorker and managing futures).
//            // For this implementation, we initiate writes and report overall processing status via notification.
//
//            String plainText = msg.getMessage(); // Get the plain text message once
//
//            for (String recId : receiverIds) {
//                Log.d(TAG, "Attempting to send scheduled message " + msgId + " to receiver " + recId);
//
//                // --- Step 1: Calculate the correct conversationId ---
//                // Use your specific ChatIdUtil method here.
//                String conversationIdForThisPair = ChatIdUtil.generateConversationId(senderId, recId);
//
//                if (TextUtils.isEmpty(conversationIdForThisPair)) {
//                    Log.e(TAG, "Failed to calculate conversationId for pair: " + senderId + " and " + recId + ". Skipping sending to this receiver.");
//                    continue; // Skip this receiver
//                }
//
//                // --- Step 2: Get the AES key for this conversation ---
//                // Your KeyManager must be accessible and initialized globally or via application context
//                SecretKey conversationAESKey = YourKeyManager.getInstance().getConversationKey(conversationIdForThisPair);
//
//                if (conversationAESKey == null) {
//                    // This means the key is NOT in the KeyManager's in-memory cache.
//                    // The message cannot be sent securely at this time for this receiver.
//                    Log.e(TAG, "AES key NOT found in KeyManager for conversation: " + conversationIdForThisPair + ". Cannot send scheduled secure message to " + recId + ". Skipping this receiver.");
//                    // Log this failure. If retries are needed, they'd be managed by WorkManager based on Result.retry() or a separate mechanism.
//                    continue; // Skip sending to this receiver if key is missing
//                }
//
//                // --- Step 3: Encrypt the message ---
//                String encryptedMessageContent;
//                try {
//                    // *** CORRECTED LINE: Pass the plainText STRING directly to encryptMessageWithAES ***
//                    // Assuming CryptoUtils.encryptMessageWithAES(String data, SecretKey key) exists and works.
//                    // It should internally convert the String to bytes (e.g., using UTF-8) before encrypting.
//                    byte[] encryptedBytes = CryptoUtils.encryptMessageWithAES(plainText, conversationAESKey);
//                    // The return type of encryptMessageWithAES must be byte[] if you are Base64 encoding it next.
//                    // If encryptMessageWithAES already returns Base64 String, then the next line is wrong.
//                    // Assuming it returns byte[], then Base64 encode the result:
//                    encryptedMessageContent = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT); // Use android.util.Base64
//
//                    Log.d(TAG, "Message encrypted successfully for conv: " + conversationIdForThisPair);
//                } catch (Exception e) {
//                    Log.e(TAG, "Failed to encrypt message for conversation: " + conversationIdForThisPair + ". Skipping sending to this receiver.", e);
//                    continue; // Skip sending to this receiver if encryption fails
//                }
//
//                // --- Step 4: Write the ENCRYPTED message to the correct Firebase path ---
//                // Generate a new unique key under the conversation node
//                DatabaseReference messagesRef = FirebaseDatabase.getInstance()
//                        .getReference("Messages")
//                        .child(conversationIdForThisPair) // Use the CORRECT conversationId
//                        .push(); // Generate a new unique key under the conversation node
//                String messageKey = messagesRef.getKey(); // Get the generated key
//
//                if (TextUtils.isEmpty(messageKey)) {
//                    Log.e(TAG, "Failed to generate message key for " + conversationIdForThisPair + ". Skipping sending to this receiver.");
//                    continue; // Skip this receiver if key generation fails
//                }
//
//                Map<String, Object> messageData = new HashMap<>();
//                messageData.put("message", encryptedMessageContent); // Put the ENCRYPTED content (Base64 String) here
//                messageData.put("from", senderId);
//                messageData.put("to", recId); // Store specific receiver ID in the message payload
//                messageData.put("type", "text"); // Assuming text message type for scheduled
//                // REMOVE status field like "sent". ChatPageActivity listener will handle status updates in Room.
//                // messageData.put("status", "sent");
//
//                messageData.put("timestamp", ServerValue.TIMESTAMP); // Use Firebase ServerValue for accurate ordering and sync
//                messageData.put("sendTime", msg.getScheduledTimeMillis()); // Use the formatted time string from ScheMsg
//                messageData.put("seen", false); // Initially not seen
//
//                // Write the message data to Firebase (Asynchronous call)
//                // We attach listeners to log the result, but doWork() does not wait for these to finish.
//                messagesRef.setValue(messageData)
//                        .addOnSuccessListener(aVoid -> {
//                            Log.d(TAG, "Scheduled message sent successfully to Firebase for conv: " + conversationIdForThisPair + ", msgId: " + messageKey);
//                            // Message written successfully. Now update chat summaries.
//
//                            // --- Step 5: Update Chat Summaries for both sender and receiver ---
//                            // Call the helper method within the Worker
//                            Object firebaseTimestamp = ServerValue.TIMESTAMP; // Use ServerValue for summary timestamp consistency
//
//                            // Update sender's summary (Owner: senderId, Partner: recId)
//                            // Sender sees encrypted preview (Base64 String) in their chat list. ChatFragment decrypts this.
//                            updateChatSummaryForPair(senderId, recId, conversationIdForThisPair, messageKey, encryptedMessageContent, "text", firebaseTimestamp, senderId);
//
//                            // Update receiver's summary (Owner: recId, Partner: senderId)
//                            // Receiver sees a placeholder like "[Message]" in their chat list.
//                            // ChatFragment needs to recognize "[Message]" or other placeholders and not try to decrypt them.
//                            updateChatSummaryForPair(recId, senderId, conversationIdForThisPair, messageKey, "[Message]", "text", firebaseTimestamp, senderId);
//                        })
//                        .addOnFailureListener(e -> {
//                            Log.e(TAG, "Failed to send scheduled message to Firebase for conv: " + conversationIdForThisPair + ". Message " + messageKey, e);
//                            // Log this specific failure. No further action on this message for this receiver in this Worker run.
//                        });
//            } // End of for loop through receiverIds
//
//            // --- Clean up the ScheduledMessage node ---
//            // Remove the original scheduled message from the ScheduledMessages node
//            // This happens after initiating send attempts for all receivers.
//            scheduledRef.removeValue()
//                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Scheduled message " + msgId + " removed from ScheduledMessages node after processing attempts."))
//                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove scheduled message " + msgId + " from ScheduledMessages node.", e));
//
//
//            // --- Show notification after initiating all sends ---
//            // This notification confirms the Worker processed the scheduled item and attempted sending.
//            List<String> receiverNames = msg.getReceiverNames(); // Assuming ScheMsg has receiverNames list
//            String title = "Scheduled Message Processed"; // Title reflecting processing initiated
//            String content;
//
//            if (receiverNames != null && !receiverNames.isEmpty()) {
//                content = "Attempted to send scheduled message to " + (receiverNames.size() == 1 ? receiverNames.get(0) : TextUtils.join(", ", receiverNames));
//            } else {
//                // Fallback if receiver names are not available
//                content = "Attempted to send scheduled message to selected contacts.";
//            }
//            // Add a note about secure sending potentially failing if key wasn't available
//            content += " (Secure send attempted)";
//
//            showNotification(getApplicationContext(), title, content);
//
//            // doWork() returns Result.success() if the *processing* of the scheduled message completed
//            // (i.e., fetch, loop through receivers, and initiating the async Firebase writes succeeded).
//            // It does NOT guarantee that all async writes completed successfully.
//            return Result.success();
//
//        } catch (Exception e) {
//            // Catch any exceptions during the synchronous fetch (Tasks.await) or initial processing setup
//            Log.e(TAG, "Exception occurred during ScheduledMessageWorker execution for msgId " + msgId + ": " + e.getMessage(), e);
//            // If a critical error happens before initiating writes, return failure
//            // Optional: show a different notification for complete failure
//            // showNotification(getApplicationContext(), "Scheduled Message Failed", "An error occurred processing scheduled message " + msgId + ".");
//
//            // Attempt cleanup of the scheduled node if it still exists after an exception
//            scheduledRef.removeValue()
//                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Cleaned up scheduled message " + msgId + " after worker exception."))
//                    .addOnFailureListener(e2 -> Log.e(TAG, "Failed to clean up scheduled message " + msgId + " after worker exception.", e2));
//
//            return Result.failure();
//        }
//    }
//
//
//    // --- Helper method to Update Chat Summary for a specific owner/partner pair ---
//    // This logic is copied and adapted from ChatPageActivity
//    private void updateChatSummaryForPair(String summaryOwnerId, String chatPartnerId,
//                                          String conversationId, String messagePushId,
//                                          String lastMessageContentPreview, String messageType,
//                                          Object firebaseTimestamp, String lastMessageSenderId) {
//
//        // Validate inputs
//        if (TextUtils.isEmpty(summaryOwnerId) || TextUtils.isEmpty(chatPartnerId) || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messagePushId) || TextUtils.isEmpty(messageType) || firebaseTimestamp == null || TextUtils.isEmpty(lastMessageSenderId)) {
//            Log.e(TAG, "Cannot update chat summary from Worker: Missing essential input data for " + summaryOwnerId + " with " + chatPartnerId);
//            return;
//        }
//
//        DatabaseReference summaryRef = FirebaseDatabase.getInstance().getReference("ChatSummaries").child(summaryOwnerId).child(chatPartnerId);
//        Log.d(TAG, "Updating summary from Worker for owner " + summaryOwnerId + " with partner " + chatPartnerId + " (Conv: " + conversationId + ")");
//
//        Map<String, Object> summaryUpdates = new HashMap<>();
//        summaryUpdates.put("conversationId", conversationId);
//        summaryUpdates.put("lastMessageId", messagePushId);
//        summaryUpdates.put("lastMessageContentPreview", lastMessageContentPreview != null ? lastMessageContentPreview : "");
//        summaryUpdates.put("lastMessageTimestamp", firebaseTimestamp);
//        summaryUpdates.put("lastMessageSenderId", lastMessageSenderId);
//        summaryUpdates.put("lastMessageType", messageType);
//        // Ensure both participants are in the summary node (important for displaying chat list)
//        summaryUpdates.put("participants/" + summaryOwnerId, true);
//        summaryUpdates.put("participants/" + chatPartnerId, true);
//
//
//        // --- Handle Unread Count Transaction ---
//        if (summaryOwnerId.equals(lastMessageSenderId)) {
//            // Sender's summary: set unread count to 0 for themselves when they send
//            Log.d(TAG, "Setting unread count to 0 for sender (" + summaryOwnerId + ") in their summary from Worker.");
//            summaryUpdates.put("unreadCounts/" + summaryOwnerId, 0);
//            // Apply updates directly for sender (no transaction needed for setting 0)
//            summaryRef.updateChildren(summaryUpdates)
//                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Sender's chat summary updated successfully from Worker."))
//                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update sender's chat summary from Worker.", e));
//
//        } else {
//            // Receiver's summary: increment unread count for themselves using a transaction
//            Log.d(TAG, "Attempting to increment unread count for receiver (" + summaryOwnerId + ") in summary from Worker.");
//
//            summaryRef.child("unreadCounts").child(summaryOwnerId).runTransaction(new Transaction.Handler() {
//                @NonNull
//                @Override
//                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
//                    Integer currentCount = currentData.getValue(Integer.class);
//                    if (currentCount == null) {
//                        currentData.setValue(1); // Start count at 1
//                    } else {
//                        // Always increment in background/Worker. ChatPageActivity will reset to 0 when opened.
//                        currentData.setValue(currentCount + 1); // Increment by 1
//                    }
//                    return Transaction.success(currentData);
//                }
//
//                @Override
//                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
//                    if (error != null) {
//                        Log.e(TAG, "Firebase transaction for unread count failed for receiver " + summaryOwnerId + " from Worker: " + error.getMessage());
//                    } else if (committed) {
//                        Log.d(TAG, "Unread count incremented for receiver " + summaryOwnerId + " from Worker. New count: " + (currentData != null ? currentData.getValue() : "N/A"));
//                    } else {
//                        Log.d(TAG, "Firebase transaction for unread count not committed for receiver " + summaryOwnerId + " from Worker (concurrent update?).");
//                    }
//                    // Update other summary fields AFTER the transaction completes, regardless of transaction outcome
//                    summaryRef.updateChildren(summaryUpdates)
//                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Receiver's chat summary (other fields) updated successfully from Worker."))
//                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update receiver's chat summary (other fields) from Worker.", e));
//                }
//            });
//        }
//    }
//
//
//    // --- Helper method to show notification ---
//    // Existing method, slightly adapted for Worker context
//    private void showNotification(Context context, String title, String message) {
//        String channelId = "scheduled_msg_channel";
//        String channelName = "Scheduled Messages"; // User-visible channel name
//
//        // Step 1: Create notification channel (Android 8+)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    channelId,
//                    channelName,
//                    NotificationManager.IMPORTANCE_HIGH // Use HIGH importance for heads-up notification
//            );
//            channel.setDescription("Notifications for scheduled message sending status.");
//            channel.enableLights(true);
//            channel.enableVibration(true);
//
//
//            NotificationManager manager = context.getSystemService(NotificationManager.class);
//            if (manager != null) {
//                manager.createNotificationChannel(channel);
//            }
//        }
//
//        // Step 2: Build the notification
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
//                .setSmallIcon(R.drawable.logo_front_one) // Replace with your app's small icon
//                .setContentTitle(title)
//                .setContentText(message)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // Use BigTextStyle for longer messages
//                .setPriority(NotificationCompat.PRIORITY_HIGH) // Match channel importance
//                .setAutoCancel(true); // Dismiss notification when tapped
//
//        // Optional: Add an Intent to open the app or chat screen when notification is tapped
//        // This is more complex as you need to decide which chat to open.
//        // For a simple status notification, no action intent might be fine.
//        /*
//        Intent intent = new Intent(context, MainActivity.class); // Or your main activity
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(pendingIntent);
//        */
//
//
//        // Step 3: Show the notification
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//
//        // Check for POST_NOTIFICATIONS permission (Android 13+)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                // Permission is required for Android 13+.
//                // In a Worker, you cannot request permissions directly.
//                // You might need to inform the user that notifications are disabled or rely on them granting it via app settings.
//                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification on Android 13+.");
//                return; // Cannot show notification
//            }
//        }
//        // For older Android versions, assuming permission is granted in manifest
//
//        // Use a unique ID for each notification (e.g., based on current time)
//        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
//        Log.d(TAG, "Status notification shown.");
//    }
//
//}


package com.sana.circleup;

// --- Existing Imports ---
// Ensure you have all necessary Android, Firebase, Crypto, etc. imports

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sana.circleup.encryptionfiles.ChatIdUtil;
import com.sana.circleup.encryptionfiles.CryptoUtils;
import com.sana.circleup.encryptionfiles.YourKeyManager;
import com.sana.circleup.one_signal_notification.OneSignalApiService;
import com.sana.circleup.room_db_implement.ChatDatabase;
import com.sana.circleup.room_db_implement.ConversationKeyDao;
import com.sana.circleup.room_db_implement.ConversationKeyEntity;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



//
//public class ScheduledMessageWorker extends Worker {
//
//    private static final String TAG = "ScheduledMsgWorker";
//
//    // Your OneSignal App ID
//    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07";
//    // Your OneSignal REST API Key (Replace with your actual key)
//    private static final String ONESIGNAL_REST_API_KEY = "YOUR_ONESIGNAL_REST_API_KEY";
//
//
//    // Firebase References (Initialize in doWork)
//    private DatabaseReference scheduledRef; // Reference to the specific scheduled message entry
//    private DatabaseReference messagesNodeRef; // Reference to the main /Messages node
//    private DatabaseReference usersRef; // Reference to the /Users node
//    private DatabaseReference chatSummariesNodeRef; // NEW: Reference to the /ChatSummaries node
//    // Removed: conversationKeysNodeRef
//
//    // OneSignal API Service (Initialize in doWork)
//    private OneSignalApiService oneSignalApiService;
//
//    // Room Database and DAOs (Not needed for message sending in this plaintext version)
//    // Removed: private ChatDatabase chatDatabase;
//    // Removed: private ConversationKeyDao conversationKeyDao;
//    // Removed: private UserDao userDao;
//
//    // Removed: KeyManager keyManager; // Not needed
//
//
//    public ScheduledMessageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
//        super(context, workerParams);
//        Log.d(TAG, "ScheduledMessageWorker instance created.");
//    }
//
//    @NonNull
//    @Override
//    @SuppressLint({"RestrictedApi", "SimpleDateFormat"}) // For ServerValue.TIMESTAMP, SimpleDateFormat
//    public Result doWork() {
//        Log.d(TAG, "ScheduledMessageWorker doWork: Started.");
//
//        final String msgId = getInputData().getString("msgId");
//
//        if (TextUtils.isEmpty(msgId)) {
//            Log.e(TAG, "Worker input data missing msgId. Cannot process.");
//            showNotification(getApplicationContext(), "Scheduled Message Failed", "Internal error: Message ID missing.", null);
//            return Result.failure();
//        }
//
//        // --- Initialize Firebase References inside doWork() ---
//        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        scheduledRef = firebaseDatabase.getReference("ScheduledMessages").child(msgId);
//        messagesNodeRef = firebaseDatabase.getReference("Messages");
//        usersRef = firebaseDatabase.getReference("Users");
//        chatSummariesNodeRef = firebaseDatabase.getReference("ChatSummaries"); // NEW: ChatSummaries node
//        // Removed conversationKeysNodeRef initialization
//
//
//        // --- Initialize OneSignalApiService inside doWork() ---
//        try {
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .addInterceptor(chain -> {
//                        Request originalRequest = chain.request();
//                        Request newRequest = originalRequest.newBuilder()
//                                .header("Authorization", "Basic " + Base64.encodeToString((ONESIGNAL_REST_API_KEY + ":").getBytes(), Base64.NO_WRAP))
//                                .header("Content-Type", "application/json")
//                                .build();
//                        return chain.proceed(newRequest);
//                    })
//                    .connectTimeout(30, TimeUnit.SECONDS)
//                    .readTimeout(30, TimeUnit.SECONDS)
//                    .writeTimeout(30, TimeUnit.SECONDS)
//                    .build();
//
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl("https://onesignal.com/api/v1/") // Correct Base URL
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(client)
//                    .build();
//            oneSignalApiService = retrofit.create(OneSignalApiService.class);
//            Log.d(TAG, "OneSignalApiService initialized in Worker.");
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to initialize OneSignalApiService in Worker.", e);
//            oneSignalApiService = null; // Ensure null if init fails
//        }
//
//        // --- Room DB Initialization removed ---
//        // try { ... } catch (Exception e) { ... }
//
//
//        // --- Main Worker Logic ---
//        try {
//            // Synchronously fetch the scheduled message data
//            Log.d(TAG, "Fetching scheduled message with ID: " + msgId);
//            DataSnapshot snapshot = Tasks.await(scheduledRef.get(), 30, TimeUnit.SECONDS);
//
//            ScheMsg msg = snapshot.getValue(ScheMsg.class);
//
//            // Basic validation after fetching data
//            if (msg == null || TextUtils.isEmpty(msg.getSenderId()) || msg.getReceiverIds() == null || msg.getReceiverIds().isEmpty() || TextUtils.isEmpty(msg.getContent()) || TextUtils.isEmpty(msg.getMessageType())) {
//                Log.e(TAG, "Scheduled message with ID " + msgId + " not found or data is incomplete after fetch. Cleaning up.");
//                showNotification(getApplicationContext(), "Scheduled Message Failed", "Message data is missing or corrupt.", msgId);
//                scheduledRef.removeValue();
//                return Result.failure();
//            }
//
//            final String senderId = msg.getSenderId();
//            List<String> receiverIds = msg.getReceiverIds(); // Get a copy of the list
//            final String contentToSend = msg.getContent(); // Plaintext or Image Base64 (NOT encrypted)
//            final String messageType = msg.getMessageType();
//            List<String> receiverNames = msg.getReceiverNames();
//            final String scheduledTimeFormatted = msg.getScheduledTimeFormatted(); // Keep for historical logging if needed
//
//
//            Log.d(TAG, "Processing scheduled message " + msgId + " from " + senderId + " to " + receiverIds.size() + " receivers. Type: " + messageType);
//
//
//            // Fetch the sender's name ONCE for the push notification title
//            String senderName = "A User";
//            try {
//                if (!TextUtils.isEmpty(senderId)) {
//                    // Use synchronous get() for usersRef in Worker
//                    DataSnapshot senderUserSnapshot = Tasks.await(usersRef.child(senderId).child("username").get(), 15, TimeUnit.SECONDS);
//                    if (senderUserSnapshot.exists()) {
//                        String name = senderUserSnapshot.getValue(String.class);
//                        if (!TextUtils.isEmpty(name)) {
//                            senderName = name;
//                        }
//                    }
//                    Log.d(TAG, "Fetched sender name '" + senderName + "' for scheduled message notifications.");
//                } else {
//                    Log.w(TAG, "Sender ID is empty for msgId " + msgId + ". Cannot fetch sender name.");
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Failed to fetch sender name for scheduled message " + msgId, e);
//            }
//            final String finalSenderName = senderName;
//
//
//            // --- Loop through each receiver and send the message ---
//            boolean allSucceeded = true; // Flag to track if *all* individual sends succeed
//
//            for (String recId : receiverIds) {
//                // Skip invalid receiver IDs
//                if (TextUtils.isEmpty(recId) || recId.equals(senderId)) {
//                    Log.w(TAG, "Scheduled message " + msgId + ": Skipping invalid receiver ID: " + recId + " in sequential send.");
//                    allSucceeded = false; // Consider invalid recipient as a failure for overall status
//                    continue;
//                }
//                Log.d(TAG, "Scheduled message " + msgId + ": Attempting to send to receiver " + recId);
//
//                // --- Step 1: Calculate the correct conversationId ---
//                String conversationIdForThisPair = ChatIdUtil.generateConversationId(senderId, recId);
//
//                if (TextUtils.isEmpty(conversationIdForThisPair)) {
//                    Log.e(TAG, "Scheduled message " + msgId + ": Failed to calculate conversationId for pair: " + senderId + " and " + recId + ". Skipping sending to this receiver.");
//                    allSucceeded = false;
//                    continue;
//                }
//
//                // *** E2EE key loading/encryption logic is REMOVED from here ***
//                // The 'contentToSend' is already the plaintext/Base64 content.
//
//
//                // --- Step 2: Write the message to Firebase for THIS pair (Synchronously using Tasks.await) ---
//                DatabaseReference conversationMessagesRef = messagesNodeRef.child(conversationIdForThisPair).push();
//                final String messageKey = conversationMessagesRef.getKey(); // Get the unique key
//
//                if (TextUtils.isEmpty(messageKey)) {
//                    Log.e(TAG, "Worker: Failed to generate message key for " + conversationIdForThisPair + ". Skipping sending to receiver " + recId + ".");
//                    allSucceeded = false;
//                    continue;
//                }
//
//                Map<String, Object> messageData = new HashMap<>();
//                messageData.put("message", contentToSend); // Send the PLAIN Base64 content (image) or plaintext (text)
//                messageData.put("type", messageType); // Set the message type
//                messageData.put("from", senderId);
//                messageData.put("to", recId);
//                messageData.put("timestamp", ServerValue.TIMESTAMP); // Use ServerValue.TIMESTAMP for Firebase time
//                messageData.put("scheduledTime", scheduledTimeFormatted); // Store the original scheduled time string (Optional)
//                messageData.put("sendTime", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date())); // Add actual send time formatted
//                messageData.put("seen", false);
//                messageData.put("seenTime", "");
//
//
//                try {
//                    Tasks.await(conversationMessagesRef.setValue(messageData), 30, TimeUnit.SECONDS); // Wait for Firebase write
//                    Log.d(TAG, "Worker: Firebase message write SUCCESS for msgKey: " + messageKey + " in conv: " + conversationIdForThisPair + " to receiver: " + recId);
//
//                    // --- Update Chat Summaries ---
//                    // Fetch actual server timestamp AFTER write is confirmed for summary timestamp consistency
//                    // Use get() + Tasks.await
//                    DataSnapshot timestampSnapshot = Tasks.await(conversationMessagesRef.child("timestamp").get(), 10, TimeUnit.SECONDS);
//                    Object actualFirebaseTimestamp = timestampSnapshot.getValue(); // Should be a Long
//
//                    // Use contentToSend (plaintext/Base64) for the preview
//                    String previewContent = (messageType.equals("text") ? contentToSend : "[" + messageType.substring(0, 1).toUpperCase() + messageType.substring(1) + "]");
//
//                    updateChatSummaryForPair(senderId, recId, conversationIdForThisPair, messageKey, previewContent, messageType, actualFirebaseTimestamp, senderId);
//                    Log.d(TAG, "Worker: Initiated Chat Summary update for sender " + senderId + " with partner " + recId);
//
//                    updateChatSummaryForPair(recId, senderId, conversationIdForThisPair, messageKey, previewContent, messageType, actualFirebaseTimestamp, senderId); // Use same preview content
//                    Log.d(TAG, "Worker: Initiated Chat Summary update for receiver " + recId + " with partner " + senderId);
//
//
//                    // *** Send OneSignal Push Notification to this specific Recipient (recId) ***
//                    if (oneSignalApiService != null) {
//                        Log.d(TAG, "Worker: Initiating OneSignal Push Notification for receiver: " + recId);
//
//                        String notificationTitle = (messageType.equals("text") ? "New Message from " : "New " + messageType + " from ") + finalSenderName;
//                        String notificationContentForNotification = (messageType.equals("text") ? contentToSend : "[" + messageType.substring(0, 1).toUpperCase() + messageType.substring(1) + "]"); // Use actual text or placeholder for notification
//
//                        // Pass necessary data payload for the recipient app to open the correct chat
//                        sendPushNotificationToRecipient(oneSignalApiService, recId, notificationTitle, notificationContentForNotification, senderId, conversationIdForThisPair, messageKey, msgId);
//
//                    } else {
//                        Log.e(TAG, "Worker: OneSignalApiService is null. Cannot send push notification to receiver: " + recId);
//                    }
//
//                } catch (Exception e) { // Catch Firebase write or summary/notification error for THIS recipient
//                    Log.e(TAG, "Worker: *** SENDING TO RECIPIENT FAILED *** for msgKey: " + messageKey + " in conv: " + conversationIdForThisPair + " to receiver " + recId + ". Error: " + e.getMessage(), e);
//                    allSucceeded = false; // Mark overall as failed if any recipient fails
//                    // Decide if you need to notify sender about this specific failure.
//                }
//            } // End sequential for loop
//
//            // --- After attempting to send to all recipients ---
//
//            // Clean up the ScheduledMessage node (do this after the loop finishes)
//            scheduledRef.removeValue()
//                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Worker: Scheduled message " + msgId + " removed from ScheduledMessages node after processing attempts."))
//                    .addOnFailureListener(e -> Log.e(TAG, "Worker: Failed to remove scheduled message " + msgId + " from ScheduledMessages node.", e));
//
//            // Show local status notification to the SENDER
//            String receiverNamesString;
//            if (receiverNames != null && !receiverNames.isEmpty()) {
//                receiverNamesString = TextUtils.join(", ", receiverNames);
//            } else {
//                receiverNamesString = "recipients";
//            }
//            String title = allSucceeded ? "Scheduled Message Sent" : "Scheduled Message Send Failed";
//            String content = allSucceeded ? "Your scheduled message to " + receiverNamesString + " was sent successfully." : "Failed to send your scheduled message to some recipients (" + receiverNamesString + ").";
//
//            showNotification(getApplicationContext(), title, content, msgId);
//
//            Log.d(TAG, "ScheduledMessageWorker doWork: Finished. Overall success: " + allSucceeded);
//            return allSucceeded ? Result.success() : Result.failure();
//
//
//        } catch (Exception e) { // Catch any synchronous exceptions during initial fetch of the scheduled message
//            Log.e(TAG, "Worker: *** SYNCHRONOUS EXCEPTION *** occurred during initial ScheduledMessageWorker execution for msgId " + msgId + ": " + e.getMessage(), e);
//
//            // Attempt to clean up the scheduled message node on synchronous failure
//            if (scheduledRef != null) { // Check if ref was initialized before cleanup attempt
//                scheduledRef.removeValue().addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "Worker: Cleaned up scheduled message " + msgId + " from ScheduledMessages node after synchronous exception.");
//                    } else {
//                        Log.e(TAG, "Worker: Failed to clean up scheduled message " + msgId + " from ScheduledMessages node after synchronous exception.", task.getException());
//                    }
//                });
//            }
//
//            // Show a failure notification to the SENDER
//            showNotification(getApplicationContext(), "Scheduled Message Failed", "An error occurred processing scheduled message " + msgId + ". Check app logs.", msgId);
//
//            return Result.failure();
//        }
//    }
//
//
//    // --- Helper method to Update Chat Summary for a specific owner/partner pair ---
//    // (Keep as is, uses FirebaseDatabase.getInstance() with application context)
//    private void updateChatSummaryForPair(String summaryOwnerId, String chatPartnerId,
//                                          String conversationId, String messagePushId,
//                                          String lastMessageContentPreview, String messageType,
//                                          Object firebaseTimestamp, String lastMessageSenderId) {
//
//        if (TextUtils.isEmpty(summaryOwnerId) || TextUtils.isEmpty(chatPartnerId) || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messagePushId) || TextUtils.isEmpty(messageType) || firebaseTimestamp == null || TextUtils.isEmpty(lastMessageSenderId)) {
//            Log.e(TAG, "Worker: Cannot update chat summary: Missing essential input data for " + summaryOwnerId + " with " + chatPartnerId);
//            return;
//        }
//
//        DatabaseReference summaryRef = FirebaseDatabase.getInstance().getReference("ChatSummaries").child(summaryOwnerId).child(chatPartnerId);
//
//        Map<String, Object> summaryUpdates = new HashMap<>();
//        summaryUpdates.put("conversationId", conversationId);
//        summaryUpdates.put("lastMessageId", messagePushId);
////        summaryUpdates.put("lastMessageContentPreview", lastMessageContentPreview != null ? lastMessageContentPreview : "");
//        summaryUpdates.put("lastMessageTimestamp", firebaseTimestamp);
//        summaryUpdates.put("lastMessageSenderId", lastMessageSenderId);
//        summaryUpdates.put("lastMessageType", messageType);
//        summaryUpdates.put("participants/" + summaryOwnerId, true);
//        summaryUpdates.put("participants/" + chatPartnerId, true);
//
//        String scheduledMessagePreviewPlaceholder = "[Scheduled Message]"; // Or "[Scheduled Text]", "[Scheduled Image]" based on type if you prefer specificity
//
//        // You could make it type-specific if you want:
//        if (messageType.equals("text")) {
//            scheduledMessagePreviewPlaceholder = "[Scheduled Text]";
//        } else if (messageType.equals("image")) {
//            scheduledMessagePreviewPlaceholder = "[Scheduled Image]";
//        } else {
//            scheduledMessagePreviewPlaceholder = "[Scheduled Message]"; // Generic fallback
//        }
//
//
//        summaryUpdates.put("lastMessageContentPreview", scheduledMessagePreviewPlaceholder);
//        // The actual contentToSend is NOT used for the preview here. It was only used when sending the message to /Messages.
//
//        // Log the preview content being set in the summary
//        Log.d(TAG, "Worker: Setting summary preview for conv " + conversationId + " with " + chatPartnerId + " to: '" + scheduledMessagePreviewPlaceholder + "' (Scheduled message)");
//
//
//
//
//
//        if (summaryOwnerId.equals(lastMessageSenderId)) {
//            // Sender's summary: Mark as read by sender (unread count 0)
//            summaryUpdates.put("unreadCounts/" + summaryOwnerId, 0);
//            try {
//                Tasks.await(summaryRef.updateChildren(summaryUpdates), 10, TimeUnit.SECONDS);
//                Log.d(TAG, "Worker: Sender summary updated successfully for " + summaryOwnerId + " with partner " + chatPartnerId);
//            } catch (Exception e) {
//                Log.e(TAG, "Worker: Failed to update sender summary for " + summaryOwnerId + " with partner " + chatPartnerId + ": " + e.getMessage(), e);
//            }
//
//        } else {
//            // Receiver's summary: Increment unread count for the receiver (summaryOwnerId)
//            try {
//                // Update the main summary fields
//                Tasks.await(summaryRef.updateChildren(summaryUpdates), 10, TimeUnit.SECONDS);
//                Log.d(TAG, "Worker: Receiver summary updated successfully for " + summaryOwnerId + " with partner " + chatPartnerId + ". Initiating unread count transaction.");
//
//                // Run transaction specifically for the unread count (synchronously using Tasks.await)
//                DataSnapshot currentCountSnapshot = Tasks.await(summaryRef.child("unreadCounts").child(summaryOwnerId).get(), 10, TimeUnit.SECONDS);
//                Integer currentCount = currentCountSnapshot.getValue(Integer.class);
//                int newCount = (currentCount == null) ? 1 : currentCount + 1;
//                Tasks.await(summaryRef.child("unreadCounts").child(summaryOwnerId).setValue(newCount), 10, TimeUnit.SECONDS);
//                Log.d(TAG, "Worker: Unread count incremented successfully for receiver " + summaryOwnerId + ". New count: " + newCount);
//
//            } catch (Exception e) {
//                Log.e(TAG, "Worker: Failed to update receiver summary/unread count for " + summaryOwnerId + ": " + e.getMessage(), e);
//            }
//        }
//    }
//
//
//    // --- Helper method to show local status notification to the SENDER ---
//    // (Keep as is, uses getApplicationContext() for SystemService)
//    private void showNotification(Context context, String title, String message, @Nullable String scheduledMessageId) {
//        String channelId = "scheduled_msg_channel";
//        String channelName = "Scheduled Messages";
//
//        android.app.NotificationManager manager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    channelId,
//                    channelName,
//                    android.app.NotificationManager.IMPORTANCE_HIGH
//            );
//            channel.setDescription("Notifications for scheduled message sending status.");
//            channel.enableLights(true);
//            channel.enableVibration(true);
//
//            if (manager != null) {
//                manager.createNotificationChannel(channel);
//            }
//        }
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
//                .setSmallIcon(R.drawable.app_icon_circleup)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true);
//
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification on Android 13+.");
//                return;
//            }
//        }
//
//        int notificationId = scheduledMessageId != null ? scheduledMessageId.hashCode() : (int) System.currentTimeMillis();
//
//        manager.notify(notificationId, builder.build());
//        Log.d(TAG, "Status notification shown for scheduled message " + scheduledMessageId);
//    }
//
//
//    // --- Helper method to SEND ONESIGNAL PUSH NOTIFICATION TO RECIPIENT ---
//    // (Keep as is, uses OneSignalApiService)
//    private void sendPushNotificationToRecipient(OneSignalApiService apiService,
//                                                 String recipientFirebaseUID, String title, String messageContentForNotification,
//                                                 String senderFirebaseUID, String conversationId, String messageFirebaseId,
//                                                 String scheduledMessageId) {
//        if (apiService == null || TextUtils.isEmpty(recipientFirebaseUID) || TextUtils.isEmpty(title) || TextUtils.isEmpty(messageContentForNotification)) {
//            Log.e(TAG, "sendPushNotificationToRecipient: Missing required parameters or API service is null. Cannot send notification.");
//            return;
//        }
//
//        Log.d(TAG, "Worker: Preparing OneSignal push notification for recipient UID (External ID): " + recipientFirebaseUID);
//
//        JsonObject notificationBody = new JsonObject();
//        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID);
//
//        JsonArray externalUserIds = new JsonArray();
//        externalUserIds.add(recipientFirebaseUID);
//        notificationBody.add("include_external_user_ids", externalUserIds);
//
//        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", title)));
//        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", messageContentForNotification)));
//
//        JsonObject data = new JsonObject();
//        data.addProperty("eventType", "new_scheduled_message");
//        data.addProperty("senderId", senderFirebaseUID);
//        data.addProperty("recipientId", recipientFirebaseUID);
//        if (!TextUtils.isEmpty(conversationId)) data.addProperty("conversationId", conversationId);
//        if (!TextUtils.isEmpty(messageFirebaseId)) data.addProperty("messageId", messageFirebaseId);
//        if (!TextUtils.isEmpty(scheduledMessageId)) data.addProperty("scheduledMessageId", scheduledMessageId); // Include scheduled ID for tracking
//
//        notificationBody.add("data", data);
//
//        notificationBody.addProperty("small_icon", "app_icon_circleup");
//        notificationBody.addProperty("large_icon", "app_icon_circleup");
//
//        Log.d(TAG, "Worker: Making OneSignal API call for recipient: " + recipientFirebaseUID + "...");
//        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "Worker: OneSignal API call SUCCESS for " + recipientFirebaseUID + ". Code: " + response.code());
//                    // Log response body if needed
//                } else {
//                    Log.e(TAG, "Worker: ONESIGNAL API CALL FAILED for " + recipientFirebaseUID + ". Code: " + response.code());
//                    try (ResponseBody errorBody = response.errorBody()) { String errBody = errorBody != null ? errorBody.string() : "N/A"; Log.e(TAG, "Worker: OneSignal Error Body: " + errBody); } catch (IOException e) { Log.e(TAG, "Worker: Failed to read OneSignal error body", e); }
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.e(TAG, "Worker: ONESIGNAL API CALL FAILED (NETWORK) for " + recipientFirebaseUID, t);
//            }
//        });
//        Log.d(TAG, "Worker: OneSignal API call enqueued for recipient: " + recipientFirebaseUID);
//    }
//
//
//    // --- Add this helper method from ChatPageActivity for creating temp camera file ---
//    // Used by launchCameraIntent in the Activity
//
//
//}


import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class ScheduledMessageWorker extends Worker {

    private static final String TAG = "ScheduledMsgWorker";

    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07";
    private static final String ONESIGNAL_REST_API_KEY = "YOUR_ONESIGNAL_REST_API_KEY"; // *** REPLACE ***


    private DatabaseReference scheduledRef;
    private DatabaseReference rootRef;
    private DatabaseReference usersRef;


    private OneSignalApiService oneSignalApiService;


    public ScheduledMessageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.d(TAG, "ScheduledMessageWorker instance created.");
    }

    @NonNull
    @Override
    @SuppressLint({"RestrictedApi", "SimpleDateFormat"})
    public Result doWork() {
        Log.d(TAG, "ScheduledMessageWorker doWork: Started.");

        final String msgId = getInputData().getString("msgId");

        if (TextUtils.isEmpty(msgId)) {
            Log.e(TAG, "Worker input data missing msgId. Cannot process.");
            showNotification(getApplicationContext(), "Scheduled Message Failed", "Internal error: Message ID missing.", null);
            return Result.failure();
        }

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();
        scheduledRef = rootRef.child("ScheduledMessages").child(msgId);
        usersRef = rootRef.child("Users");

        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Basic " + Base64.encodeToString((ONESIGNAL_REST_API_KEY + ":").getBytes(), Base64.NO_WRAP))
                                .header("Content-Type", "application/json")
                                .build();
                        return chain.proceed(newRequest);
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://onesignal.com/api/v1/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            oneSignalApiService = retrofit.create(OneSignalApiService.class);
            Log.d(TAG, "OneSignalApiService initialized in Worker.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignalApiService in Worker.", e);
            oneSignalApiService = null;
        }

        ScheMsg msg = null;
        try {
            // ... (Existing code to fetch ScheMsg from Firebase and perform initial validation) ...
            Log.d(TAG, "Fetching scheduled message with ID: " + msgId);
            DataSnapshot snapshot = Tasks.await(scheduledRef.get(), 30, TimeUnit.SECONDS);
            msg = snapshot.getValue(ScheMsg.class);

            // ... (Existing validation of msg and its fields) ...
            if (msg == null || TextUtils.isEmpty(msg.getSenderId()) || msg.getReceiverIds() == null || msg.getReceiverIds().isEmpty() || TextUtils.isEmpty(msg.getContent()) || TextUtils.isEmpty(msg.getMessageType()) || TextUtils.isEmpty(msg.getRecipientType())) {
                // ... (error logging, notification, cleanup, return Failure) ...
                String missingInfo = ""; // ... (populate missingInfo) ...
                Log.e(TAG, "Scheduled message with ID " + msgId + " not found or data is incomplete after fetch. Missing: " + missingInfo + " Cleaning up.");
                showNotification(getApplicationContext(), "Scheduled Message Failed", "Message data is missing or corrupt.", msgId);
                scheduledRef.removeValue();
                return Result.failure();
            }
            // ... (End validation) ...

            final String senderId = msg.getSenderId();
            List<String> targetRecipientIds = msg.getReceiverIds();
            List<String> targetRecipientNames = msg.getReceiverNames();
            final String contentToSend = msg.getContent();
            final String messageType = msg.getMessageType();
            final String recipientType = msg.getRecipientType();
            final String scheduledTimeFormatted = msg.getScheduledTimeFormatted();

            // Ensure we have exactly one recipient ID and name for this entry
            if (targetRecipientIds.size() != 1 || targetRecipientNames.size() != 1) {
                Log.e(TAG, "Scheduled message with ID " + msgId + " has invalid recipient list size (" + targetRecipientIds.size() + "). Expected 1. Cleaning up.");
                showNotification(getApplicationContext(), "Scheduled Message Failed", "Recipient data corrupt.", msgId);
                scheduledRef.removeValue();
                return Result.failure();
            }
            final String targetRecipientId = targetRecipientIds.get(0);
            final String targetRecipientName = targetRecipientNames.get(0);

            Log.d(TAG, "Processing scheduled message " + msgId + " from " + senderId + " to recipient '" + targetRecipientName + "' (ID: " + targetRecipientId + "). RecipientType: " + recipientType + ", MessageType: " + messageType);

            // Fetch the sender's name ONCE for the push notification title (Keep this)
            String senderName = "A User"; // ... (Existing code to fetch senderName) ...
            try {
                if (!TextUtils.isEmpty(senderId)) { /* ... fetch username ... */
                    DataSnapshot senderUserSnapshot = Tasks.await(usersRef.child(senderId).child("username").get(), 15, TimeUnit.SECONDS);
                    if (senderUserSnapshot.exists()) {
                        String name = senderUserSnapshot.getValue(String.class);
                        if (!TextUtils.isEmpty(name)) senderName = name;
                    }
                    Log.d(TAG, "Fetched sender name '" + senderName + "' for scheduled message notifications.");
                } else Log.w(TAG, "Sender ID is empty for msgId " + msgId + ". Cannot fetch sender name.");
            } catch (Exception e) { Log.e(TAG, "Failed to fetch sender name for scheduled message " + msgId, e); }
            final String finalSenderName = senderName;


            // --- Determine Firebase path and Prepare Message Data based on Recipient Type ---
            DatabaseReference messageTargetRef = null;
            String conversationId = null;
            List<String> notificationRecipientUidsList = new ArrayList<>();
            Map<String, Object> messageData = new HashMap<>(); // *** Prepare messageData HERE ***

            switch (recipientType) {
                case "user":
                    conversationId = ChatIdUtil.generateConversationId(senderId, targetRecipientId);
                    if (TextUtils.isEmpty(conversationId)) {
                        Log.e(TAG, "Worker: Failed to generate conversationId for user pair: " + senderId + " and " + targetRecipientId + " for msgId " + msgId + ". Skipping send.");
                        showNotification(getApplicationContext(), "Scheduled Message Failed", "Could not generate chat ID for user " + targetRecipientName, msgId);
                        scheduledRef.removeValue();
                        return Result.failure();
                    }
                    // The message path is /Messages/{conversationId}/...
                    messageTargetRef = rootRef.child("Messages").child(conversationId).push();
                    Log.d(TAG, "Worker: RecipientType is 'user'. Target path: Messages/" + conversationId);

                    // --- Prepare Message Data for USER (Match ChatPageActivity expectations) ---
                    messageData.put("message", contentToSend);
                    messageData.put("type", messageType);
                    messageData.put("from", senderId); // *** Use "from" ***
                    messageData.put("to", targetRecipientId); // *** Use "to" ***

                    messageData.put("timestamp", ServerValue.TIMESTAMP);
                    messageData.put("sendTime", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()));
                    messageData.put("seen", false); // Initial state for recipient
                    messageData.put("seenTime", "");
                    messageData.put("scheduledTime", scheduledTimeFormatted); // Optional: Keep for debugging

                    // name, recipientType, recipientId, drawingSessionId are NOT standard for 1:1 payload
                    // Do not put them here to match standard structure.
                    // *** REMOVE messageData.put("name", finalSenderName); ***
                    // *** REMOVE messageData.put("recipientType", recipientType); ***
                    // *** REMOVE messageData.put("recipientId", targetRecipientId); ***
                    // *** REMOVE messageData.put("readBy", readByMap); (1:1 uses seen/seenTime) ***
                    // *** REMOVE messageData.put("drawingSessionId", null); (Not for 1:1) ***


                    // For user, notify only the targetRecipientId
                    notificationRecipientUidsList.add(targetRecipientId);
                    // Note: The notification logic later will need senderName for the title.
                    break;

                case "group":
                    conversationId = targetRecipientId; // Group ID
                    messageTargetRef = rootRef.child("Groups").child(conversationId).child("Messages").push();
                    Log.d(TAG, "Worker: RecipientType is 'group'. Target path: Groups/" + conversationId + "/Messages");

                    // --- Prepare Message Data for GROUP (Match GroupChatActivity expectations) ---
                    messageData.put("message", contentToSend);
                    messageData.put("type", messageType);
                    messageData.put("senderId", senderId); // Group uses senderId
                    messageData.put("name", finalSenderName); // Group uses sender's name in payload

                    messageData.put("timestamp", ServerValue.TIMESTAMP);
                    messageData.put("sendTime", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()));
                    // ReadBy map for groups/temp rooms
                    Map<String, Object> readByMap = new HashMap<>();
                    readByMap.put(senderId, true); // Sender has read it
                    messageData.put("readBy", readByMap);

                    messageData.put("scheduledTime", scheduledTimeFormatted);
                    messageData.put("drawingSessionId", null); // Not for scheduled text/image


                    // For group, fetch all members to notify (excluding sender)
                    notificationRecipientUidsList = fetchGroupMembers(targetRecipientId);
                    Log.d(TAG, "Worker: Fetched " + notificationRecipientUidsList.size() + " members for group " + targetRecipientId + " for notification.");
                    break;

                case "temporary_room":
                    conversationId = targetRecipientId; // Room ID
                    messageTargetRef = rootRef.child("temporaryChatRooms").child(conversationId).child("messages").push();
                    Log.d(TAG, "Worker: RecipientType is 'temporary_room'. Target path: temporaryChatRooms/" + conversationId + "/messages");

                    // --- Prepare Message Data for TEMPORARY ROOM (Match TemporaryRoomChatActivity expectations) ---
                    messageData.put("message", contentToSend);
                    messageData.put("type", messageType);
                    messageData.put("senderId", senderId); // Temp Room uses senderId
                    messageData.put("name", finalSenderName); // Temp Room uses sender's name in payload

                    messageData.put("timestamp", ServerValue.TIMESTAMP);
                    messageData.put("sendTime", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()));
                    // ReadBy map for groups/temp rooms
                    Map<String, Object> readByMapTemp = new HashMap<>(); // Use a different map variable name
                    readByMapTemp.put(senderId, true);
                    messageData.put("readBy", readByMapTemp);

                    messageData.put("scheduledTime", scheduledTimeFormatted);
                    messageData.put("drawingSessionId", null); // Not for scheduled text/image


                    // For temporary room, fetch all members to notify (excluding sender)
                    notificationRecipientUidsList = fetchTemporaryRoomMembers(targetRecipientId);
                    Log.d(TAG, "Worker: Fetched " + notificationRecipientUidsList.size() + " members for temporary room " + targetRecipientId + " for notification.");
                    break;

                default:
                    Log.e(TAG, "Worker: Unknown recipientType '" + recipientType + "' for msgId " + msgId + ". Cannot send message. Cleaning up.");
                    showNotification(getApplicationContext(), "Scheduled Message Failed", "Unknown recipient type for " + targetRecipientName, msgId);
                    scheduledRef.removeValue();
                    return Result.failure();
            }


            if (messageTargetRef == null) {
                Log.e(TAG, "Worker: messageTargetRef is null after determining recipient type for msgId " + msgId + ". Skipping send.");
                showNotification(getApplicationContext(), "Scheduled Message Failed", "Internal error determining chat.", msgId);
                scheduledRef.removeValue();
                return Result.failure();
            }

            final String messageKey = messageTargetRef.getKey();

            if (TextUtils.isEmpty(messageKey)) {
                Log.e(TAG, "Worker: Failed to generate message key for conv/group/room " + conversationId + " for msgId " + msgId + ". Skipping send.");
                showNotification(getApplicationContext(), "Scheduled Message Failed", "Failed to generate message ID.", msgId);
                scheduledRef.removeValue();
                return Result.failure();
            }

            // --- Send the message to Firebase ---
            Log.d(TAG, "Worker: Attempting Firebase message write for msgId " + msgId + " (MessageKey: " + messageKey + "). Recipient Type: " + recipientType);
            // Using messageData prepared in the switch block
            Tasks.await(messageTargetRef.setValue(messageData), 30, TimeUnit.SECONDS);
            Log.d(TAG, "Worker: Firebase message write SUCCESS for msgKey: " + messageKey + " in conv/group/room " + conversationId + " for msgId " + msgId);


            // --- Update Chat Summaries / List Info (Handle based on Recipient Type) ---
            if ("user".equals(recipientType)) {
                // For 1:1 chat, update chat summaries for both users.
                String previewContent = (messageType.equals("text") ? contentToSend : "[" + messageType.substring(0, 1).toUpperCase() + messageType.substring(1) + "]");

                DataSnapshot timestampSnapshot = Tasks.await(messageTargetRef.child("timestamp").get(), 10, TimeUnit.SECONDS);
                Object actualFirebaseTimestamp = timestampSnapshot.getValue();

                Log.d(TAG, "Worker: Initiating Chat Summary update for user recipient.");
                updateChatSummaryForUser(
                        senderId, targetRecipientId, // OwnerId, PartnerId
                        conversationId, messageKey, previewContent, messageType, // ConvId, MsgKey, Preview, MsgType
                        actualFirebaseTimestamp, senderId // Timestamp, Sender of THIS message
                );
                updateChatSummaryForUser(
                        targetRecipientId, senderId, // OwnerId, PartnerId
                        conversationId, messageKey, previewContent, messageType, // ConvId, MsgKey, Preview, MsgType
                        actualFirebaseTimestamp, senderId // Timestamp, Sender of THIS message
                );
            }
            // Group/Temporary Room list updates are handled by listeners in GroupFragment/TemporaryRoomChatActivity
            // based on changes in /Groups/{groupId}/Messages or /temporaryChatRooms/{roomId}/messages nodes.


            // *** Send OneSignal Push Notification ***
            if (oneSignalApiService != null && notificationRecipientUidsList != null && !notificationRecipientUidsList.isEmpty()) { // Added check for empty notification list
                String notificationTitle;
                String notificationContentForNotification;

                if (messageType.equals("text")) {
                    notificationContentForNotification = contentToSend;
                } else if (messageType.equals("image")) {
                    notificationContentForNotification = "[Image]";
                } else {
                    notificationContentForNotification = "[" + messageType.substring(0, 1).toUpperCase() + messageType.substring(1) + "]";
                }

                if ("user".equals(recipientType)) {
                    notificationTitle = "New Message from " + finalSenderName;
                } else { // group or temporary_room
                    notificationTitle = "New Message in " + targetRecipientName; // Use group/room name from ScheMsg
                }

                sendPushNotificationToRecipients(
                        oneSignalApiService, notificationRecipientUidsList,
                        senderId, targetRecipientId,
                        conversationId, messageKey,
                        "scheduled_" + recipientType + "_message", // Use a specific event type
                        notificationTitle, notificationContentForNotification
                );

            } else {
                Log.w(TAG, "Worker: OneSignalApiService is null or notification recipient list is null/empty. Cannot send push notification for msgId: " + msgId); // Changed to W
            }
            // *** END NEW ***


            // --- Clean up the ScheduledMessage node ---
            scheduledRef.removeValue()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Worker: Scheduled message " + msgId + " removed from ScheduledMessages node after successful processing."))
                    .addOnFailureListener(e -> Log.e(TAG, "Worker: Failed to remove scheduled message " + msgId + " from ScheduledMessages node.", e));

            // Show local status notification to the SENDER
            String title = "Scheduled Message Sent";
            String content = "Your scheduled message to " + targetRecipientName + " was sent successfully.";
            showNotification(getApplicationContext(), title, content, msgId);

            Log.d(TAG, "ScheduledMessageWorker doWork: Finished SUCCESS for msgId: " + msgId);
            return Result.success();


        } catch (InterruptedException e) {
            Log.e(TAG, "Worker: InterruptedException during ScheduledMessageWorker execution for msgId " + msgId, e);
            showNotification(getApplicationContext(), "Scheduled Message Failed", "Task interrupted.", msgId);
            if (scheduledRef != null) scheduledRef.removeValue();
            return Result.retry();

        } catch (ExecutionException e) {
            Log.e(TAG, "Worker: ExecutionException during ScheduledMessageWorker execution for msgId " + msgId + ". Firebase task failed.", e);
            showNotification(getApplicationContext(), "Scheduled Message Failed", "Error sending message.", msgId);
            if (scheduledRef != null) scheduledRef.removeValue();
            return Result.failure();

        } catch (TimeoutException e) {
            Log.e(TAG, "Worker: TimeoutException during ScheduledMessageWorker execution for msgId " + msgId + ". Firebase task timed out.", e);
            showNotification(getApplicationContext(), "Scheduled Message Failed", "Sending timed out.", msgId);
            if (scheduledRef != null) scheduledRef.removeValue();
            return Result.retry();

        } catch (Exception e) { // Catch any other unexpected exceptions
            Log.e(TAG, "Worker: *** UNEXPECTED EXCEPTION *** occurred during ScheduledMessageWorker execution for msgId " + msgId + ": " + e.getMessage(), e);

            if (scheduledRef != null) {
                scheduledRef.removeValue();
            }

            String recipientNameForNotification = "recipient";
            if (msg != null && msg.getReceiverNames() != null && !msg.getReceiverNames().isEmpty()) {
                recipientNameForNotification = msg.getReceiverNames().get(0);
            }
            showNotification(getApplicationContext(), "Scheduled Message Failed", "Failed to send your message to " + recipientNameForNotification + ". An error occurred.", msgId);

            Log.d(TAG, "ScheduledMessageWorker doWork: Finished with UNEXPECTED FAILURE for msgId: " + msgId);
            return Result.failure();
        }
    }


    // --- NEW Helper method to fetch group members synchronously ---
    private List<String> fetchGroupMembers(String groupId) {
        List<String> memberUids = new ArrayList<>();
        if (rootRef == null || TextUtils.isEmpty(groupId)) {
            Log.e(TAG, "fetchGroupMembers: rootRef or groupId is null/empty.");
            return memberUids;
        }
        try {
            DataSnapshot membersSnapshot = Tasks.await(rootRef.child("Groups").child(groupId).child("members").get(), 10, TimeUnit.SECONDS);
            if (membersSnapshot.exists()) {
                for (DataSnapshot memberSnap : membersSnapshot.getChildren()) {
                    String uid = memberSnap.getKey();
                    if (!TextUtils.isEmpty(uid)) {
                        memberUids.add(uid);
                    }
                }
            } else {
                Log.w(TAG, "Group members node not found for group: " + groupId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch group members for group " + groupId, e);
        }
        return memberUids;
    }

    // --- NEW Helper method to fetch temporary room members synchronously ---
    private List<String> fetchTemporaryRoomMembers(String roomId) {
        List<String> memberUids = new ArrayList<>();
        if (rootRef == null || TextUtils.isEmpty(roomId)) {
            Log.e(TAG, "fetchTemporaryRoomMembers: rootRef or roomId is null/empty.");
            return memberUids;
        }
        try {
            DataSnapshot membersSnapshot = Tasks.await(rootRef.child("temporaryChatRooms").child(roomId).child("members").get(), 10, TimeUnit.SECONDS);
            if (membersSnapshot.exists()) {
                for (DataSnapshot memberSnap : membersSnapshot.getChildren()) {
                    String uid = memberSnap.getKey();
                    if (!TextUtils.isEmpty(uid)) {
                        memberUids.add(uid);
                    }
                }
            } else {
                Log.w(TAG, "Temporary room members node not found for room: " + roomId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch temporary room members for room " + roomId, e);
        }
        return memberUids;
    }


    // --- Keep show local status notification to the SENDER ---
    private void showNotification(Context context, String title, String message, @Nullable String scheduledMessageId) {
        String channelId = "scheduled_msg_channel";
        String channelName = "Scheduled Messages";

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for scheduled message sending status.");
            channel.enableLights(true);
            channel.enableVibration(true);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_icon_circleup)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification on Android 13+.");
                return;
            }
        }

        int notificationId = scheduledMessageId != null ? scheduledMessageId.hashCode() : (int) System.currentTimeMillis();

        if (manager != null) {
            manager.notify(notificationId, builder.build());
            Log.d(TAG, "Status notification shown for scheduled message " + scheduledMessageId);
        } else {
            Log.e(TAG, "NotificationManager is null. Cannot show status notification.");
        }
    }

    // --- Helper method to SEND ONESIGNAL PUSH NOTIFICATION TO A LIST OF RECIPIENTS ---
    // ... (existing sendPushNotificationToRecipients method) ...
    private void sendPushNotificationToRecipients(OneSignalApiService apiService,
                                                  List<String> allRecipientUids,
                                                  String senderUid,
                                                  String targetRecipientId,
                                                  String conversationOrChatId,
                                                  String messageFirebaseId,
                                                  String eventType,
                                                  String notificationTitle,
                                                  String notificationContent) {

        if (apiService == null) {
            Log.e(TAG, "sendPushNotificationToRecipients: API service is null. Cannot send notification.");
            return;
        }
        if (allRecipientUids == null || allRecipientUids.isEmpty()) {
            Log.w(TAG, "sendPushNotificationToRecipients: All recipient UIDs list is null or empty. Cannot send notification.");
            return;
        }
        if (TextUtils.isEmpty(senderUid) || TextUtils.isEmpty(targetRecipientId) || TextUtils.isEmpty(conversationOrChatId) || TextUtils.isEmpty(messageFirebaseId) || TextUtils.isEmpty(eventType)) {
            Log.e(TAG, "sendPushNotificationToRecipients: Essential data payload parameters are null/empty. Cannot send notification.");
            return;
        }
        if (TextUtils.isEmpty(notificationTitle) || TextUtils.isEmpty(notificationContent)) {
            Log.w(TAG, "sendPushNotificationToRecipients: Notification title or content is empty. Cannot send notification.");
            return;
        }

        List<String> recipientUidsFiltered = new ArrayList<>();
        for (String uid : allRecipientUids) {
            if (!TextUtils.isEmpty(uid) && !uid.equals(senderUid)) {
                recipientUidsFiltered.add(uid);
            }
        }

        if (recipientUidsFiltered.isEmpty()) {
            Log.d(TAG, "sendPushNotificationToRecipients: No recipients after filtering sender (" + senderUid + "). Skipping notification.");
            return;
        }

        Log.d(TAG, "Preparing OneSignal push notification event '" + eventType + "' to " + recipientUidsFiltered.size() + " recipients.");

        JsonObject notificationBody = new JsonObject();
        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID);

        JsonArray externalUserIdsArray = new JsonArray();
        for(String uid : recipientUidsFiltered) {
            externalUserIdsArray.add(uid);
        }
        notificationBody.add("include_external_user_ids", externalUserIdsArray);


        notificationBody.add("headings", new Gson().toJsonTree(Collections.singletonMap("en", notificationTitle)));
        notificationBody.add("contents", new Gson().toJsonTree(Collections.singletonMap("en", notificationContent)));

        JsonObject data = new JsonObject();
        data.addProperty("eventType", eventType);
        data.addProperty("senderId", senderUid);
        data.addProperty("messageId", messageFirebaseId);

        data.addProperty("targetRecipientId", targetRecipientId);
        data.addProperty("chatId", conversationOrChatId);
        data.addProperty("recipientType", getRecipientTypeFromEventType(eventType));


        notificationBody.add("data", data);
        notificationBody.addProperty("small_icon", "app_icon_circleup");


        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "OneSignal API call successful for event '" + eventType + "'. Code: " + response.code());
                    try (ResponseBody responseBody = response.body()) { String resBody = responseBody != null ? responseBody.string() : "N/A"; Log.d(TAG, "OneSignal API Response Body: " + resBody); }
                    catch (IOException e) { Log.e(TAG, "Failed to read success response body ('" + eventType + "')", e); }
                } else {
                    Log.e(TAG, "OneSignal API call failed for event '" + eventType + "'. Code: " + response.code());
                    try (ResponseBody errorBody = response.errorBody()) { String errBody = errorBody != null ? errorBody.string() : "N/A"; Log.e(TAG, "OneSignal API Error Body: " + errBody); }
                    catch (IOException e) { Log.e(TAG, "Failed to read error response body ('" + eventType + "')", e); }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "OneSignal API call failed (network error) for event '" + eventType + "'", t);
            }
        });
        Log.d(TAG, "OneSignal API call enqueued for event '" + eventType + "'.");
    }

    // Helper to derive recipient type string from event type string
    private String getRecipientTypeFromEventType(String eventType) {
        if ("scheduled_user_message".equals(eventType)) return "user";
        if ("scheduled_group_message".equals(eventType)) return "group";
        if ("scheduled_temp_room_message".equals(eventType)) return "temporary_room";
        return null; // Or "unknown"
    }

    // --- Keep updateChatSummaryForUser (only used for user-to-user scheduled messages) ---
    @SuppressLint("RestrictedApi")
    private void updateChatSummaryForUser(String summaryOwnerId, String chatPartnerId,
                                          String conversationId, String messagePushId,
                                          String lastMessageContentPreview, String messageType,
                                          Object firebaseTimestamp, String lastMessageSenderId) {
        if (TextUtils.isEmpty(summaryOwnerId) || TextUtils.isEmpty(chatPartnerId) || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messagePushId) || TextUtils.isEmpty(messageType) || firebaseTimestamp == null || TextUtils.isEmpty(lastMessageSenderId)) {
            Log.e(TAG, "Worker: Cannot update chat summary: Missing essential input data for " + summaryOwnerId + " with " + chatPartnerId);
            return;
        }

        DatabaseReference summaryRef = FirebaseDatabase.getInstance().getReference("ChatSummaries").child(summaryOwnerId).child(chatPartnerId);
        Log.d(TAG, "Worker: Updating summary for owner " + summaryOwnerId + " with partner " + chatPartnerId + " at path: " + summaryRef.getPath());


        Map<String, Object> summaryUpdates = new HashMap<>();
        summaryUpdates.put("conversationId", conversationId);
        summaryUpdates.put("lastMessageId", messagePushId);

        String previewContent;
        if ("text".equals(messageType)) {
            previewContent = lastMessageContentPreview;
        } else if ("image".equals(messageType)) {
            previewContent = "[Image]";
        } else {
            previewContent = "[" + messageType.substring(0, 1).toUpperCase() + messageType.substring(1) + "]";
        }
        summaryUpdates.put("lastMessageContentPreview", previewContent);


        summaryUpdates.put("lastMessageTimestamp", firebaseTimestamp);
        summaryUpdates.put("lastMessageSenderId", lastMessageSenderId);
        summaryUpdates.put("lastMessageType", messageType);

        summaryUpdates.put("participants/" + summaryOwnerId, true);
        summaryUpdates.put("participants/" + chatPartnerId, true);


        if (summaryOwnerId.equals(lastMessageSenderId)) {
            Log.d(TAG, "Worker: Setting unread count to 0 for sender (" + summaryOwnerId + ") in their summary.");
            summaryUpdates.put("unreadCounts/" + summaryOwnerId, 0);

            try {
                Tasks.await(summaryRef.updateChildren(summaryUpdates), 10, TimeUnit.SECONDS);
                Log.d(TAG, "Worker: Sender summary updated successfully for " + summaryOwnerId + " with partner " + chatPartnerId);
            } catch (Exception e) {
                Log.e(TAG, "Worker: Failed to update sender summary for " + summaryOwnerId + " with partner " + chatPartnerId + ": " + e.getMessage(), e);
            }


        } else {
            try {
                Tasks.await(summaryRef.updateChildren(summaryUpdates), 10, TimeUnit.SECONDS);
                Log.d(TAG, "Worker: Receiver summary updated successfully for " + summaryOwnerId + " with partner " + chatPartnerId + ". Initiating unread count transaction.");

                DataSnapshot currentCountSnapshot = Tasks.await(summaryRef.child("unreadCounts").child(summaryOwnerId).get(), 10, TimeUnit.SECONDS);
                Integer currentCount = currentCountSnapshot.getValue(Integer.class);
                int newCount = (currentCount == null) ? 1 : currentCount + 1;
                Tasks.await(summaryRef.child("unreadCounts").child(summaryOwnerId).setValue(newCount), 10, TimeUnit.SECONDS);
                Log.d(TAG, "Worker: Unread count incremented successfully for receiver " + summaryOwnerId + ". New count: " + newCount);

            } catch (Exception e) {
                Log.e(TAG, "Worker: Failed to update receiver summary/unread count for " + summaryOwnerId + ": " + e.getMessage(), e);
            }
        }
    }
}