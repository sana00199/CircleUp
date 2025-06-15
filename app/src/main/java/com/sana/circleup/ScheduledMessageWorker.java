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




public class ScheduledMessageWorker extends Worker {

    private static final String TAG = "ScheduledMsgWorker";

    // Your OneSignal App ID
    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07";
    // Your OneSignal REST API Key (Replace with your actual key)
    private static final String ONESIGNAL_REST_API_KEY = "YOUR_ONESIGNAL_REST_API_KEY";


    // Firebase References (Initialize in doWork)
    private DatabaseReference scheduledRef; // Reference to the specific scheduled message entry
    private DatabaseReference messagesNodeRef; // Reference to the main /Messages node
    private DatabaseReference usersRef; // Reference to the /Users node
    private DatabaseReference chatSummariesNodeRef; // NEW: Reference to the /ChatSummaries node
    // Removed: conversationKeysNodeRef

    // OneSignal API Service (Initialize in doWork)
    private OneSignalApiService oneSignalApiService;

    // Room Database and DAOs (Not needed for message sending in this plaintext version)
    // Removed: private ChatDatabase chatDatabase;
    // Removed: private ConversationKeyDao conversationKeyDao;
    // Removed: private UserDao userDao;

    // Removed: KeyManager keyManager; // Not needed


    public ScheduledMessageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.d(TAG, "ScheduledMessageWorker instance created.");
    }

    @NonNull
    @Override
    @SuppressLint({"RestrictedApi", "SimpleDateFormat"}) // For ServerValue.TIMESTAMP, SimpleDateFormat
    public Result doWork() {
        Log.d(TAG, "ScheduledMessageWorker doWork: Started.");

        final String msgId = getInputData().getString("msgId");

        if (TextUtils.isEmpty(msgId)) {
            Log.e(TAG, "Worker input data missing msgId. Cannot process.");
            showNotification(getApplicationContext(), "Scheduled Message Failed", "Internal error: Message ID missing.", null);
            return Result.failure();
        }

        // --- Initialize Firebase References inside doWork() ---
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        scheduledRef = firebaseDatabase.getReference("ScheduledMessages").child(msgId);
        messagesNodeRef = firebaseDatabase.getReference("Messages");
        usersRef = firebaseDatabase.getReference("Users");
        chatSummariesNodeRef = firebaseDatabase.getReference("ChatSummaries"); // NEW: ChatSummaries node
        // Removed conversationKeysNodeRef initialization


        // --- Initialize OneSignalApiService inside doWork() ---
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
                    .baseUrl("https://onesignal.com/api/v1/") // Correct Base URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            oneSignalApiService = retrofit.create(OneSignalApiService.class);
            Log.d(TAG, "OneSignalApiService initialized in Worker.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OneSignalApiService in Worker.", e);
            oneSignalApiService = null; // Ensure null if init fails
        }

        // --- Room DB Initialization removed ---
        // try { ... } catch (Exception e) { ... }


        // --- Main Worker Logic ---
        try {
            // Synchronously fetch the scheduled message data
            Log.d(TAG, "Fetching scheduled message with ID: " + msgId);
            DataSnapshot snapshot = Tasks.await(scheduledRef.get(), 30, TimeUnit.SECONDS);

            ScheMsg msg = snapshot.getValue(ScheMsg.class);

            // Basic validation after fetching data
            if (msg == null || TextUtils.isEmpty(msg.getSenderId()) || msg.getReceiverIds() == null || msg.getReceiverIds().isEmpty() || TextUtils.isEmpty(msg.getContent()) || TextUtils.isEmpty(msg.getMessageType())) {
                Log.e(TAG, "Scheduled message with ID " + msgId + " not found or data is incomplete after fetch. Cleaning up.");
                showNotification(getApplicationContext(), "Scheduled Message Failed", "Message data is missing or corrupt.", msgId);
                scheduledRef.removeValue();
                return Result.failure();
            }

            final String senderId = msg.getSenderId();
            List<String> receiverIds = msg.getReceiverIds(); // Get a copy of the list
            final String contentToSend = msg.getContent(); // Plaintext or Image Base64 (NOT encrypted)
            final String messageType = msg.getMessageType();
            List<String> receiverNames = msg.getReceiverNames();
            final String scheduledTimeFormatted = msg.getScheduledTimeFormatted(); // Keep for historical logging if needed


            Log.d(TAG, "Processing scheduled message " + msgId + " from " + senderId + " to " + receiverIds.size() + " receivers. Type: " + messageType);


            // Fetch the sender's name ONCE for the push notification title
            String senderName = "A User";
            try {
                if (!TextUtils.isEmpty(senderId)) {
                    // Use synchronous get() for usersRef in Worker
                    DataSnapshot senderUserSnapshot = Tasks.await(usersRef.child(senderId).child("username").get(), 15, TimeUnit.SECONDS);
                    if (senderUserSnapshot.exists()) {
                        String name = senderUserSnapshot.getValue(String.class);
                        if (!TextUtils.isEmpty(name)) {
                            senderName = name;
                        }
                    }
                    Log.d(TAG, "Fetched sender name '" + senderName + "' for scheduled message notifications.");
                } else {
                    Log.w(TAG, "Sender ID is empty for msgId " + msgId + ". Cannot fetch sender name.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch sender name for scheduled message " + msgId, e);
            }
            final String finalSenderName = senderName;


            // --- Loop through each receiver and send the message ---
            boolean allSucceeded = true; // Flag to track if *all* individual sends succeed

            for (String recId : receiverIds) {
                // Skip invalid receiver IDs
                if (TextUtils.isEmpty(recId) || recId.equals(senderId)) {
                    Log.w(TAG, "Scheduled message " + msgId + ": Skipping invalid receiver ID: " + recId + " in sequential send.");
                    allSucceeded = false; // Consider invalid recipient as a failure for overall status
                    continue;
                }
                Log.d(TAG, "Scheduled message " + msgId + ": Attempting to send to receiver " + recId);

                // --- Step 1: Calculate the correct conversationId ---
                String conversationIdForThisPair = ChatIdUtil.generateConversationId(senderId, recId);

                if (TextUtils.isEmpty(conversationIdForThisPair)) {
                    Log.e(TAG, "Scheduled message " + msgId + ": Failed to calculate conversationId for pair: " + senderId + " and " + recId + ". Skipping sending to this receiver.");
                    allSucceeded = false;
                    continue;
                }

                // *** E2EE key loading/encryption logic is REMOVED from here ***
                // The 'contentToSend' is already the plaintext/Base64 content.


                // --- Step 2: Write the message to Firebase for THIS pair (Synchronously using Tasks.await) ---
                DatabaseReference conversationMessagesRef = messagesNodeRef.child(conversationIdForThisPair).push();
                final String messageKey = conversationMessagesRef.getKey(); // Get the unique key

                if (TextUtils.isEmpty(messageKey)) {
                    Log.e(TAG, "Worker: Failed to generate message key for " + conversationIdForThisPair + ". Skipping sending to receiver " + recId + ".");
                    allSucceeded = false;
                    continue;
                }

                Map<String, Object> messageData = new HashMap<>();
                messageData.put("message", contentToSend); // Send the PLAIN Base64 content (image) or plaintext (text)
                messageData.put("type", messageType); // Set the message type
                messageData.put("from", senderId);
                messageData.put("to", recId);
                messageData.put("timestamp", ServerValue.TIMESTAMP); // Use ServerValue.TIMESTAMP for Firebase time
                messageData.put("scheduledTime", scheduledTimeFormatted); // Store the original scheduled time string (Optional)
                messageData.put("sendTime", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date())); // Add actual send time formatted
                messageData.put("seen", false);
                messageData.put("seenTime", "");


                try {
                    Tasks.await(conversationMessagesRef.setValue(messageData), 30, TimeUnit.SECONDS); // Wait for Firebase write
                    Log.d(TAG, "Worker: Firebase message write SUCCESS for msgKey: " + messageKey + " in conv: " + conversationIdForThisPair + " to receiver: " + recId);

                    // --- Update Chat Summaries ---
                    // Fetch actual server timestamp AFTER write is confirmed for summary timestamp consistency
                    // Use get() + Tasks.await
                    DataSnapshot timestampSnapshot = Tasks.await(conversationMessagesRef.child("timestamp").get(), 10, TimeUnit.SECONDS);
                    Object actualFirebaseTimestamp = timestampSnapshot.getValue(); // Should be a Long

                    // Use contentToSend (plaintext/Base64) for the preview
                    String previewContent = (messageType.equals("text") ? contentToSend : "[" + messageType.substring(0, 1).toUpperCase() + messageType.substring(1) + "]");

                    updateChatSummaryForPair(senderId, recId, conversationIdForThisPair, messageKey, previewContent, messageType, actualFirebaseTimestamp, senderId);
                    Log.d(TAG, "Worker: Initiated Chat Summary update for sender " + senderId + " with partner " + recId);

                    updateChatSummaryForPair(recId, senderId, conversationIdForThisPair, messageKey, previewContent, messageType, actualFirebaseTimestamp, senderId); // Use same preview content
                    Log.d(TAG, "Worker: Initiated Chat Summary update for receiver " + recId + " with partner " + senderId);


                    // *** Send OneSignal Push Notification to this specific Recipient (recId) ***
                    if (oneSignalApiService != null) {
                        Log.d(TAG, "Worker: Initiating OneSignal Push Notification for receiver: " + recId);

                        String notificationTitle = (messageType.equals("text") ? "New Message from " : "New " + messageType + " from ") + finalSenderName;
                        String notificationContentForNotification = (messageType.equals("text") ? contentToSend : "[" + messageType.substring(0, 1).toUpperCase() + messageType.substring(1) + "]"); // Use actual text or placeholder for notification

                        // Pass necessary data payload for the recipient app to open the correct chat
                        sendPushNotificationToRecipient(oneSignalApiService, recId, notificationTitle, notificationContentForNotification, senderId, conversationIdForThisPair, messageKey, msgId);

                    } else {
                        Log.e(TAG, "Worker: OneSignalApiService is null. Cannot send push notification to receiver: " + recId);
                    }

                } catch (Exception e) { // Catch Firebase write or summary/notification error for THIS recipient
                    Log.e(TAG, "Worker: *** SENDING TO RECIPIENT FAILED *** for msgKey: " + messageKey + " in conv: " + conversationIdForThisPair + " to receiver " + recId + ". Error: " + e.getMessage(), e);
                    allSucceeded = false; // Mark overall as failed if any recipient fails
                    // Decide if you need to notify sender about this specific failure.
                }
            } // End sequential for loop

            // --- After attempting to send to all recipients ---

            // Clean up the ScheduledMessage node (do this after the loop finishes)
            scheduledRef.removeValue()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Worker: Scheduled message " + msgId + " removed from ScheduledMessages node after processing attempts."))
                    .addOnFailureListener(e -> Log.e(TAG, "Worker: Failed to remove scheduled message " + msgId + " from ScheduledMessages node.", e));

            // Show local status notification to the SENDER
            String receiverNamesString;
            if (receiverNames != null && !receiverNames.isEmpty()) {
                receiverNamesString = TextUtils.join(", ", receiverNames);
            } else {
                receiverNamesString = "recipients";
            }
            String title = allSucceeded ? "Scheduled Message Sent" : "Scheduled Message Send Failed";
            String content = allSucceeded ? "Your scheduled message to " + receiverNamesString + " was sent successfully." : "Failed to send your scheduled message to some recipients (" + receiverNamesString + ").";

            showNotification(getApplicationContext(), title, content, msgId);

            Log.d(TAG, "ScheduledMessageWorker doWork: Finished. Overall success: " + allSucceeded);
            return allSucceeded ? Result.success() : Result.failure();


        } catch (Exception e) { // Catch any synchronous exceptions during initial fetch of the scheduled message
            Log.e(TAG, "Worker: *** SYNCHRONOUS EXCEPTION *** occurred during initial ScheduledMessageWorker execution for msgId " + msgId + ": " + e.getMessage(), e);

            // Attempt to clean up the scheduled message node on synchronous failure
            if (scheduledRef != null) { // Check if ref was initialized before cleanup attempt
                scheduledRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Worker: Cleaned up scheduled message " + msgId + " from ScheduledMessages node after synchronous exception.");
                    } else {
                        Log.e(TAG, "Worker: Failed to clean up scheduled message " + msgId + " from ScheduledMessages node after synchronous exception.", task.getException());
                    }
                });
            }

            // Show a failure notification to the SENDER
            showNotification(getApplicationContext(), "Scheduled Message Failed", "An error occurred processing scheduled message " + msgId + ". Check app logs.", msgId);

            return Result.failure();
        }
    }


    // --- Helper method to Update Chat Summary for a specific owner/partner pair ---
    // (Keep as is, uses FirebaseDatabase.getInstance() with application context)
    private void updateChatSummaryForPair(String summaryOwnerId, String chatPartnerId,
                                          String conversationId, String messagePushId,
                                          String lastMessageContentPreview, String messageType,
                                          Object firebaseTimestamp, String lastMessageSenderId) {

        if (TextUtils.isEmpty(summaryOwnerId) || TextUtils.isEmpty(chatPartnerId) || TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(messagePushId) || TextUtils.isEmpty(messageType) || firebaseTimestamp == null || TextUtils.isEmpty(lastMessageSenderId)) {
            Log.e(TAG, "Worker: Cannot update chat summary: Missing essential input data for " + summaryOwnerId + " with " + chatPartnerId);
            return;
        }

        DatabaseReference summaryRef = FirebaseDatabase.getInstance().getReference("ChatSummaries").child(summaryOwnerId).child(chatPartnerId);

        Map<String, Object> summaryUpdates = new HashMap<>();
        summaryUpdates.put("conversationId", conversationId);
        summaryUpdates.put("lastMessageId", messagePushId);
        summaryUpdates.put("lastMessageContentPreview", lastMessageContentPreview != null ? lastMessageContentPreview : "");
        summaryUpdates.put("lastMessageTimestamp", firebaseTimestamp);
        summaryUpdates.put("lastMessageSenderId", lastMessageSenderId);
        summaryUpdates.put("lastMessageType", messageType);
        summaryUpdates.put("participants/" + summaryOwnerId, true);
        summaryUpdates.put("participants/" + chatPartnerId, true);


        if (summaryOwnerId.equals(lastMessageSenderId)) {
            // Sender's summary: Mark as read by sender (unread count 0)
            summaryUpdates.put("unreadCounts/" + summaryOwnerId, 0);
            try {
                Tasks.await(summaryRef.updateChildren(summaryUpdates), 10, TimeUnit.SECONDS);
                Log.d(TAG, "Worker: Sender summary updated successfully for " + summaryOwnerId + " with partner " + chatPartnerId);
            } catch (Exception e) {
                Log.e(TAG, "Worker: Failed to update sender summary for " + summaryOwnerId + " with partner " + chatPartnerId + ": " + e.getMessage(), e);
            }

        } else {
            // Receiver's summary: Increment unread count for the receiver (summaryOwnerId)
            try {
                // Update the main summary fields
                Tasks.await(summaryRef.updateChildren(summaryUpdates), 10, TimeUnit.SECONDS);
                Log.d(TAG, "Worker: Receiver summary updated successfully for " + summaryOwnerId + " with partner " + chatPartnerId + ". Initiating unread count transaction.");

                // Run transaction specifically for the unread count (synchronously using Tasks.await)
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


    // --- Helper method to show local status notification to the SENDER ---
    // (Keep as is, uses getApplicationContext() for SystemService)
    private void showNotification(Context context, String title, String message, @Nullable String scheduledMessageId) {
        String channelId = "scheduled_msg_channel";
        String channelName = "Scheduled Messages";

        android.app.NotificationManager manager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    android.app.NotificationManager.IMPORTANCE_HIGH
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

        manager.notify(notificationId, builder.build());
        Log.d(TAG, "Status notification shown for scheduled message " + scheduledMessageId);
    }


    // --- Helper method to SEND ONESIGNAL PUSH NOTIFICATION TO RECIPIENT ---
    // (Keep as is, uses OneSignalApiService)
    private void sendPushNotificationToRecipient(OneSignalApiService apiService,
                                                 String recipientFirebaseUID, String title, String messageContentForNotification,
                                                 String senderFirebaseUID, String conversationId, String messageFirebaseId,
                                                 String scheduledMessageId) {
        if (apiService == null || TextUtils.isEmpty(recipientFirebaseUID) || TextUtils.isEmpty(title) || TextUtils.isEmpty(messageContentForNotification)) {
            Log.e(TAG, "sendPushNotificationToRecipient: Missing required parameters or API service is null. Cannot send notification.");
            return;
        }

        Log.d(TAG, "Worker: Preparing OneSignal push notification for recipient UID (External ID): " + recipientFirebaseUID);

        JsonObject notificationBody = new JsonObject();
        notificationBody.addProperty("app_id", ONESIGNAL_APP_ID);

        JsonArray externalUserIds = new JsonArray();
        externalUserIds.add(recipientFirebaseUID);
        notificationBody.add("include_external_user_ids", externalUserIds);

        notificationBody.add("headings", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", title)));
        notificationBody.add("contents", new com.google.gson.Gson().toJsonTree(Collections.singletonMap("en", messageContentForNotification)));

        JsonObject data = new JsonObject();
        data.addProperty("eventType", "new_scheduled_message");
        data.addProperty("senderId", senderFirebaseUID);
        data.addProperty("recipientId", recipientFirebaseUID);
        if (!TextUtils.isEmpty(conversationId)) data.addProperty("conversationId", conversationId);
        if (!TextUtils.isEmpty(messageFirebaseId)) data.addProperty("messageId", messageFirebaseId);
        if (!TextUtils.isEmpty(scheduledMessageId)) data.addProperty("scheduledMessageId", scheduledMessageId); // Include scheduled ID for tracking

        notificationBody.add("data", data);

        notificationBody.addProperty("small_icon", "app_icon_circleup");
        notificationBody.addProperty("large_icon", "app_icon_circleup");

        Log.d(TAG, "Worker: Making OneSignal API call for recipient: " + recipientFirebaseUID + "...");
        apiService.sendNotification(notificationBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Worker: OneSignal API call SUCCESS for " + recipientFirebaseUID + ". Code: " + response.code());
                    // Log response body if needed
                } else {
                    Log.e(TAG, "Worker: ONESIGNAL API CALL FAILED for " + recipientFirebaseUID + ". Code: " + response.code());
                    try (ResponseBody errorBody = response.errorBody()) { String errBody = errorBody != null ? errorBody.string() : "N/A"; Log.e(TAG, "Worker: OneSignal Error Body: " + errBody); } catch (IOException e) { Log.e(TAG, "Worker: Failed to read OneSignal error body", e); }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "Worker: ONESIGNAL API CALL FAILED (NETWORK) for " + recipientFirebaseUID, t);
            }
        });
        Log.d(TAG, "Worker: OneSignal API call enqueued for recipient: " + recipientFirebaseUID);
    }


    // --- Add this helper method from ChatPageActivity for creating temp camera file ---
    // Used by launchCameraIntent in the Activity


}