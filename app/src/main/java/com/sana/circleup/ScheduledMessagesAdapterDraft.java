package com.sana.circleup;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
//
//public class ScheduledMessagesAdapterDraft extends RecyclerView.Adapter<ScheduledMessagesAdapterDraft.ScheduledMessageViewHolder> {
//
//    private Context context;
//    private List<ScheMsg> scheduledMessages;
//    private FirebaseUser currentUser;
//
//    // Constructor
//    public ScheduledMessagesAdapterDraft(Context context, List<ScheMsg> scheduledMessages) {
//        this.context = context;
//        this.scheduledMessages = scheduledMessages;
//        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
//    }
//
//    @NonNull
//    @Override
//    public ScheduledMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.itemdraft_scheduled_message, parent, false);
//        return new ScheduledMessageViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ScheduledMessageViewHolder holder, int position) {
//        ScheMsg message = scheduledMessages.get(position);
//
//
//        // Ensure receiverNames is not null
//        if (message.getReceiverNames() != null && !message.getReceiverNames().isEmpty()) {
//            // Use TextUtils.join to display names, ensure receiverNames is not null
//            String receiverNamesStr = TextUtils.join(", ", message.getReceiverNames());
//            holder.tvReceiver.setText(receiverNamesStr);
//        } else {
//            // Handle case where receiverNames is null or empty
//            holder.tvReceiver.setText("No receiver names available");
//        }
//
//        // Only show messages scheduled by the current user
//        if (message.getSenderId().equals(currentUser.getUid())) {
//            holder.tvMessage.setText(message.getMessage());
//            holder.tvTime.setText(message.getScheduledTimeMillis());
//
//            holder.tvReceiver.setText(message.getReceiverNamesStr());
//
//            // Concatenate all receiver names into one string
//            String receivers = "To: " + TextUtils.join(", ", message.getReceiverNames());
//            holder.tvReceiver.setText(receivers);
//
//
//            holder.btnEdit.setOnClickListener(v -> {
//                // Handle edit action
//                // Open a dialog or another activity to edit the message
//            });
//
//            holder.btnDelete.setOnClickListener(v -> {
//                // Handle delete action
//                // Remove the message from the database
//                deleteScheduledMessage(message);
//            });
//        } else {
//            holder.itemView.setVisibility(View.GONE); // Hide messages from other users
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return scheduledMessages.size();
//    }
//
//    // ViewHolder for each message
//    public static class ScheduledMessageViewHolder extends RecyclerView.ViewHolder {
//        TextView tvMessage, tvTime, tvReceiver;
//        Button btnEdit, btnDelete;
//
//        public ScheduledMessageViewHolder(View itemView) {
//            super(itemView);
//            tvMessage = itemView.findViewById(R.id.tvMessage);
//            tvTime = itemView.findViewById(R.id.tvTime);
//            btnEdit = itemView.findViewById(R.id.btnEdit);
//            btnDelete = itemView.findViewById(R.id.btnDelete);
//            tvReceiver = itemView.findViewById(R.id.tvReceiver);
//        }
//    }
//
//    // Delete the scheduled message from Firebase
//    private void deleteScheduledMessage(ScheMsg message) {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ScheduledMessages");
//        ref.orderByChild("message").equalTo(message.getMessage()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    ds.getRef().removeValue(); // Remove the message
//                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(context, "Error deleting message", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}






import android.content.Context;
import android.text.TextUtils; // Import TextUtils
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Import NonNull
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat; // Import SimpleDateFormat
import java.util.Date; // Import Date
import java.util.List;
import java.util.Locale; // Import Locale

public class ScheduledMessagesAdapterDraft extends RecyclerView.Adapter<ScheduledMessagesAdapterDraft.ScheduledMessageViewHolder> {

    private static final String TAG = "ScheMsgAdapterDraft"; // Add TAG

    private Context context;
    private List<ScheMsg> scheduledMessages; // List of ScheMsg objects
    private FirebaseUser currentUser;


    public ScheduledMessagesAdapterDraft(Context context, List<ScheMsg> scheduledMessages) {
        this.context = context;
        this.scheduledMessages = scheduledMessages;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "Adapter initialized with " + scheduledMessages.size() + " messages."); // Log init
    }

    @NonNull
    @Override
    public ScheduledMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.itemdraft_scheduled_message, parent, false); // Use correct layout
        return new ScheduledMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduledMessageViewHolder holder, int position) {
        ScheMsg message = scheduledMessages.get(position);

        if (message == null) { // Defensive check
            Log.w(TAG, "Binding skipped for null message at position " + position);
            holder.itemView.setVisibility(View.GONE); // Hide the item
            return;
        }
        // Ensure the item is visible initially if message is not null
        holder.itemView.setVisibility(View.VISIBLE);
        // Adjust layout params if visibility changes, might need to use 0dp height for GONE

        // Only display messages sent by the current user
        if (currentUser == null || !message.getSenderId().equals(currentUser.getUid())) {
            // If currentUser is null or message is not from current user, hide the item
            holder.itemView.setVisibility(View.GONE);
            // Ensure the item takes no space when hidden
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            Log.d(TAG, "Hiding message " + (message.getSenderId() != null ? message.getSenderId() : "null") + " not from current user.");
            return; // Stop processing this item
        } else {
            // Message is from the current user, ensure it's visible and takes space
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            Log.d(TAG, "Displaying message " + message.getSenderId() + " from current user.");
        }


        // --- Set message details ---
        holder.tvMessage.setText(message.getMessage());
        // Use the stored formatted time string
        holder.tvTime.setText(message.getScheduledTimeFormatted()); // Use the correct getter

        // Concatenate receiver names for display
        // Use the receiverNamesStr field from ScheMsg for simplicity
        String receivers = "To: " + message.getReceiverNamesStr(); // Use the getter for the string
        holder.tvReceiver.setText(receivers);

        // Optional: Show message status (pending, sent, failed)
        // You would need a TextView for status in your item layout
        // holder.tvStatus.setText(message.getStatus());


        // --- Button Listeners ---
        holder.btnEdit.setOnClickListener(v -> {
            Log.d(TAG, "Edit button clicked for message: " + message.getMessage());
            // Handle edit action - Pass message ID or object to edit activity/dialog
            // showEditScheduledMessageDialog(message); // Implement this method
        });

        holder.btnDelete.setOnClickListener(v -> {
            Log.d(TAG, "Delete button clicked for message: " + message.getMessage());
            // Handle delete action
            deleteScheduledMessage(message); // Call the method to delete
        });

        Log.d(TAG, "Bound message data: Msg='" + message.getMessage() + "', Time='" + message.getScheduledTimeFormatted() + "', To='" + message.getReceiverNamesStr() + "'");
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the data list
        // Note: This count is for the original list, hidden items still count.
        // Filtering would require a different approach with a filtered list.
        return scheduledMessages.size();
    }

    // ViewHolder for each message (Remains the same)
    public static class ScheduledMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvReceiver;
        Button btnEdit, btnDelete;
        // Optional: TextView tvStatus;

        public ScheduledMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage); // Ensure correct IDs
            tvTime = itemView.findViewById(R.id.tvTime); // Ensure correct IDs
            btnEdit = itemView.findViewById(R.id.btnEdit); // Ensure correct IDs
            btnDelete = itemView.findViewById(R.id.btnDelete); // Ensure correct IDs
            tvReceiver = itemView.findViewById(R.id.tvReceiver); // Ensure correct IDs
            // Optional: tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

    // *** MODIFIED deleteScheduledMessage Method ***
    // Delete the scheduled message from Firebase using its unique msgId
    private void deleteScheduledMessage(ScheMsg message) {
        // We need the msgId (the Firebase key) to delete the scheduled message entry.
        // Your ScheMsg class does not currently store this ID.
        // You need to either:
        // 1. Modify ScheMsg to store its Firebase key (msgId).
        // 2. Pass the msgId to the adapter when creating the list.
        // 3. Re-query Firebase based on message content, sender/receivers, and time (less reliable).

        // Assumption: Let's assume for now that message.getFirebaseKey() exists in ScheMsg
        // OR you need to pass the key from the Activity/Fragment when the list is populated.

        // A more reliable approach: Pass the list of ScheMsg with their Firebase keys
        // from the activity/fragment that loads them.

        // If you are loading ScheMsg directly from Firebase in your DraftScheduled activity/fragment,
        // you get the key from the DataSnapshot. You need to store this key in your ScheMsg object
        // or in a structure alongside it.

        // Assuming the simplest fix: You can query Firebase using a unique field, like the scheduledTimeFormatted.
        // However, time might not be unique if multiple messages are scheduled for the exact same second.
        // Querying by message content + sender + formatted time is more unique but still not foolproof.
        // The MOST reliable way is to store the Firebase key (msgId) in the ScheMsg object itself.

        // Let's use a query that is reasonably unique, assuming message + sender + time is unique enough for FYP.
        // WARNING: This is NOT foolproof if two identical messages are scheduled by the same sender at the same second.
        // The correct fix requires storing the Firebase key in ScheMsg.

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ScheduledMessages");

        // Query by unique combination: senderId, message content, scheduledTimeFormatted
        ref.orderByChild("senderId").equalTo(message.getSenderId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean foundAndDeleted = false;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ScheMsg scheduled = ds.getValue(ScheMsg.class);
                            if (scheduled != null &&
                                    scheduled.getMessage().equals(message.getMessage()) &&
                                    scheduled.getScheduledTimeFormatted().equals(message.getScheduledTimeFormatted())
                                // Add check for receiverIds/names if stored identically in ScheMsg
                                // && scheduled.getReceiverIds().equals(message.getReceiverIds()) // Requires lists to be equal
                            )
                            {
                                // Found the matching scheduled message entry
                                ds.getRef().removeValue(); // Remove the message from Firebase
                                Log.d(TAG, "Scheduled message deleted from Firebase: " + ds.getKey());
                                Toast.makeText(context, "Scheduled message deleted.", Toast.LENGTH_SHORT).show();
                                foundAndDeleted = true;

                                // Optional: Also cancel the WorkManager task associated with this message ID if it hasn't run yet
                                // This requires knowing the WorkManager Tag or ID used when scheduling.
                                // If you used the msgId as the WorkRequest ID or Tag, you can cancel it.
                                // WorkManager.getInstance(context).cancelAllWorkByTag(ds.getKey()); // Example if msgId is tag
                                // WorkManager.getInstance(context).cancelWorkById(UUID.fromString(ds.getKey())); // Example if msgId is UUID and stored as such


                                // Break loop after deleting the first match (assuming intended to delete only one)
                                break;
                            }
                        }

                        if (!foundAndDeleted) {
                            Log.w(TAG, "Scheduled message not found in Firebase to delete.");
                            Toast.makeText(context, "Scheduled message not found.", Toast.LENGTH_SHORT).show();
                        }

                        // Note: Deleting from Firebase will trigger listener in DraftScheduled activity/fragment
                        // which should update the list (remove the deleted item).
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error deleting scheduled message from Firebase.", error.toException());
                        Toast.makeText(context, "Error deleting message.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // --- END MODIFIED deleteScheduledMessage ---
}