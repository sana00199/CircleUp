



package com.sana.circleup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64; // Use android.util.Base64
import android.util.Log;
import android.view.LayoutInflater;
// ... existing imports ...
import android.view.GestureDetector; // Add this import
import android.view.MotionEvent;    // Add this import
// ... rest of your imports ...
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
// Removed AlertDialog import as it wasn't used in the provided onBindViewHolder logic
// Removed Toast import as it wasn't used
// Removed Firebase imports as adapter shouldn't directly interact with them

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Import your MessageEntity class
import com.bumptech.glide.Glide;
import com.sana.circleup.room_db_implement.MessageEntity; // <-- Make sure this import is correct

// Assuming this class exists for image full screen view
import com.sana.circleup.ChatImgFullScreenViewer; // Replace with your actual package name for this activity


import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;



import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


import de.hdodenhof.circleimageview.CircleImageView;

// Make sure your MessageEntity import is correct
// import com.example.yourchatapp.data.db.entity.MessageEntity; // Adjust package


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.sana.circleup.room_db_implement.MessageEntity; // Import your MessageEntity

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapter"; // Use a TAG for logs
    private final List<MessageEntity> userMessagesList;
    private final Context context;
    private final String chatPartnerProfileImage;
    private final String currentUserID;


    private static final int VIEW_TYPE_SENDER_TEXT = 1;
    private static final int VIEW_TYPE_RECEIVER_TEXT = 2;
    private static final int VIEW_TYPE_SENDER_IMAGE = 3;
    private static final int VIEW_TYPE_RECEIVER_IMAGE = 4;
    private static final int VIEW_TYPE_SYSTEM_MESSAGE = 5; // <-- NEW View Type



    // --- Interface for Long Click ---
    public interface OnMessageLongClickListener {
        void onMessageLongClick(MessageEntity message); // Pass the clicked message
    }

    private OnMessageLongClickListener longClickListener; // Listener field

    // Setter for the listener
    public void setOnMessageLongClickListener(OnMessageLongClickListener listener) {
        this.longClickListener = listener;
    }

    public MessageAdapter(List<MessageEntity> userMessagesList, Context context, String chatPartnerProfileImage, String currentUserID) {
        this.userMessagesList = userMessagesList;
        this.context = context;
        this.chatPartnerProfileImage = chatPartnerProfileImage;
        this.currentUserID = currentUserID;
        Log.d(TAG, "Adapter initialized with Current User ID: " + currentUserID);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout systemMessageLayout;
        public TextView systemMessageText;
        TextView senderMessageText, receiverMessageText, msgSendTime, msgSeenTime, msgReceiverTime;
        CircleImageView receiverProfileImage;
        View senderLayout, receiverLayout;
        ImageView senderImageView, receiverImageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView); // Corrected this line

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            systemMessageText = itemView.findViewById(R.id.tv_system_message);
            systemMessageLayout = itemView.findViewById(R.id.system_message_layout);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            msgSendTime = itemView.findViewById(R.id.msg_send_time);
            msgSeenTime = itemView.findViewById(R.id.msg_seen_time);
            msgReceiverTime = itemView.findViewById(R.id.msg_receiver_time);
            receiverProfileImage = itemView.findViewById(R.id.receiver_profile_image);
            senderLayout = itemView.findViewById(R.id.sender_layout);
            receiverLayout = itemView.findViewById(R.id.receiver_layout);
            senderImageView = itemView.findViewById(R.id.sender_image_view);
            receiverImageView = itemView.findViewById(R.id.receiver_image_view);
        }
    }


    @Override
    public int getItemViewType(int position) {
        MessageEntity message = userMessagesList.get(position);
        if (message == null) return -1; // Should not happen if list is managed well

        String fromUserID = message.getFrom();
        String messageType = message.getType();

        // --- Determine View Type based on sender and message type ---
        if ("system_key_change".equals(messageType)) { // <-- NEW: Check for system message type
            return VIEW_TYPE_SYSTEM_MESSAGE;
        } else if (fromUserID != null && fromUserID.equals(currentUserID)) {
            // Message is from the current user (Sender)
            if ("text".equals(messageType)) {
                return VIEW_TYPE_SENDER_TEXT;
            } else if ("image".equals(messageType)) {
                return VIEW_TYPE_SENDER_IMAGE;
            }
            // Add other sender types here if needed
            return VIEW_TYPE_SENDER_TEXT; // Default for unknown sender types
        } else {
            // Message is from the chat partner (Receiver)
            if ("text".equals(messageType)) {
                return VIEW_TYPE_RECEIVER_TEXT;
            } else if ("image".equals(messageType)) {
                return VIEW_TYPE_RECEIVER_IMAGE;
            }
            // Add other receiver types here if needed
            return VIEW_TYPE_RECEIVER_TEXT; // Default for unknown receiver types
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_messages_layout, parent, false);
        return new MessageViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
//        MessageEntity message = userMessagesList.get(position);
//
//        if (message == null || currentUserID == null) {
//            Log.e(TAG, "Null message or currentUserID at position " + position);
//            holder.senderLayout.setVisibility(View.GONE);
//            holder.receiverLayout.setVisibility(View.GONE);
//            return;
//        }
//
//        // Basic check for essential data fields from MessageEntity
//        if(message.getMessage() == null || message.getType() == null || message.getFrom() == null || message.getTimestamp() <= 0) {
//            Log.w(TAG, "Skipping binding due to invalid MessageEntity data at position " + position +
//                    " Msg:" + message.getMessage() + ", Type:" + message.getType() + ", From:" + message.getFrom() + ", Timestamp:" + message.getTimestamp());
//            // Optionally hide this invalid viewholder completely
//            holder.itemView.setVisibility(View.GONE);
//            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0)); // Make it take no space
//            return;
//        }
//
//        // Ensure the item view is visible if it was hidden previously
//        holder.itemView.setVisibility(View.VISIBLE);
//        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//
//        String fromUserID = message.getFrom();
//        String messageType = message.getType();
//        String messageContent = message.getMessage();
//        long timestamp = message.getTimestamp();
//        boolean isSeen = message.isSeen();
//        String seenTime = message.getSeenTime();
//        String status = message.getStatus(); // Use status for Pending/Failed
//
//        String formattedSendTime = formatTimestamp(timestamp);
//
//        // --- Hide all views initially ---
//        holder.senderLayout.setVisibility(View.GONE);
//        holder.receiverLayout.setVisibility(View.GONE);
//        holder.senderMessageText.setVisibility(View.GONE);
//        holder.receiverMessageText.setVisibility(View.GONE);
//        holder.receiverProfileImage.setVisibility(View.GONE);
//        holder.msgSendTime.setVisibility(View.GONE);
//        holder.msgSeenTime.setVisibility(View.GONE); // Hide by default, show for sender messages
//        holder.msgReceiverTime.setVisibility(View.GONE);
//        holder.senderImageView.setVisibility(View.GONE);
//        holder.receiverImageView.setVisibility(View.GONE);
//
//
//        // --- Check if the message is from the current user (Sender - Right side) ---
//        if (fromUserID.equals(currentUserID)) {
//            holder.senderLayout.setVisibility(View.VISIBLE);
//
//            if ("text".equals(messageType)) {
//                holder.senderMessageText.setVisibility(View.VISIBLE);
//                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
//                holder.senderMessageText.setText(messageContent);
//
//                holder.msgSendTime.setVisibility(View.VISIBLE);
//                holder.msgSendTime.setText(formattedSendTime);
//
//                // Status and Seen Time for Sender
//                holder.msgSeenTime.setVisibility(View.VISIBLE);
//                String statusText = "";
//                int statusColor = context.getResources().getColor(android.R.color.darker_gray); // Default color
//
//                // --- Modified Logic ---
//                if ("failed".equals(status)) { // Still show 'Failed'
//                    statusText = "Failed";
//                    statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
//                } else if (isSeen && seenTime != null && !seenTime.isEmpty()) { // If seen, show Seen
//                    statusText = "Seen at " + seenTime;
//                    statusColor = context.getResources().getColor(android.R.color.holo_blue_dark);
//                } else { // Otherwise (not failed, not seen, including 'pending' or 'sent'), show 'Sent'
//                    statusText = "Sent";
//                    statusColor = context.getResources().getColor(android.R.color.darker_gray);
//                }
//// --- End Modified Logic ---
//
//                holder.msgSeenTime.setText(statusText);
//                holder.msgSeenTime.setTextColor(statusColor);
//
//
//                // --- Long Click Listener for Sender Text ---
//                holder.senderMessageText.setOnLongClickListener(v -> {
//                    if (longClickListener != null) {
//                        longClickListener.onMessageLongClick(message);
//                    }
//                    return true; // Consume the long click
//                });
//
//
//            } else if ("image".equals(messageType)) {
//                holder.senderImageView.setVisibility(View.VISIBLE);
//                decodeBase64AndSetImage(messageContent, holder.senderImageView);
//
//                holder.msgSendTime.setVisibility(View.VISIBLE);
//                holder.msgSendTime.setText(formattedSendTime);
//
//                // Status and Seen Time for Sender Image
//                holder.msgSeenTime.setVisibility(View.VISIBLE);
//                String statusText = "";
//                int statusColor = context.getResources().getColor(android.R.color.darker_gray); // Default color
//
//                if ("failed".equals(status)) { // Still show 'Failed'
//                    statusText = "Failed";
//                    statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
//                } else if (isSeen && seenTime != null && !seenTime.isEmpty()) { // If seen, show Seen
//                    statusText = "Seen at " + seenTime;
//                    statusColor = context.getResources().getColor(android.R.color.holo_blue_dark);
//                } else { // Otherwise (not failed, not seen, including 'pending' or 'sent'), show 'Sent'
//                    statusText = "Sent";
//                    statusColor = context.getResources().getColor(android.R.color.darker_gray);
//                }
//
//                holder.msgSeenTime.setText(statusText);
//                holder.msgSeenTime.setTextColor(statusColor);
//
//
//                final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
//                    @Override
//                    public boolean onSingleTapUp(MotionEvent e) {
//                        // This is the click event: Open full screen image viewer
//                        Log.d(TAG, "Sender image clicked: " + message.getFirebaseMessageId());
//                        Intent intent = new Intent(context, ChatImgFullScreenViewer.class);
//                        intent.putExtra("imageUrl", messageContent); // Pass the Base64 string
//                        context.startActivity(intent);
//                        return true; // Consume the event
//                    }
//
//                    @Override
//                    public void onLongPress(MotionEvent e) {
//                        // This is the long click event: Trigger delete dialog
//                        Log.d(TAG, "Sender image long pressed: " + message.getFirebaseMessageId());
//                        if (longClickListener != null) {
//                            longClickListener.onMessageLongClick(message);
//                        }
//                        // Note: onLongPress is void, no return value needed
//                    }
//
//                    // Override onDown to ensure it always returns true so detector starts
//                    @Override
//                    public boolean onDown(MotionEvent e) {
//                        return true;
//                    }
//                });
//
//                holder.senderImageView.setOnTouchListener((v, event) -> {
//                    // Pass the touch event to the gesture detector
//                    boolean handled = gestureDetector.onTouchEvent(event);
//
//                    // Return true if the gesture detector handled the event.
//                    // This prevents default click/long click and also potential RecyclerView scrolling interference.
//                    return handled;
//                });
//
//                // Remove any previous direct listeners just in case (optional but safe)
//                holder.senderImageView.setOnClickListener(null);
//                holder.senderImageView.setOnLongClickListener(null);
//
//
//            // ... rest of sender handling (if any other types) ...
//
//
//
//            if ("failed".equals(status)) {
//                    holder.senderImageView.setOnLongClickListener(v -> {
//                        Log.d(TAG, "Long clicked on failed image message: " + message.getFirebaseMessageId());
//                        Toast.makeText(context, "Retry logic needed for failed image.", Toast.LENGTH_SHORT).show();
//                        // Notify activity/fragment via listener if implemented
//                        // if (onMessageActionListener != null) onMessageActionListener.onFailedMessageLongClick(message.getFirebaseMessageId());
//                        return true;
//                    });
//                } else {
//                    holder.senderImageView.setOnLongClickListener(null);
//                }
//            }
//        }
//        // --- Message is from the chat partner (Receiver - Left side) ---
//        else {
//            holder.receiverLayout.setVisibility(View.VISIBLE);
//            holder.receiverProfileImage.setVisibility(View.VISIBLE);
//            decodeBase64AndSetImage(chatPartnerProfileImage, holder.receiverProfileImage);
//
//            if ("text".equals(messageType)) {
//                holder.receiverMessageText.setVisibility(View.VISIBLE);
//                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
//                holder.receiverMessageText.setText(messageContent);
//
//                holder.msgReceiverTime.setVisibility(View.VISIBLE);
//                holder.msgReceiverTime.setText(formattedSendTime);
//
//                holder.msgSeenTime.setVisibility(View.GONE); // Hide seen status for receiver
//
//
//                // --- Long Click Listener for Receiver Text ---
//                holder.receiverMessageText.setOnLongClickListener(v -> {
//                    if (longClickListener != null) {
//                        longClickListener.onMessageLongClick(message);
//                    }
//                    return true; // Consume the long click
//                });
//
//
//            } else if ("image".equals(messageType)) {
//                holder.receiverImageView.setVisibility(View.VISIBLE);
//                decodeBase64AndSetImage(messageContent, holder.receiverImageView);
//
//                holder.msgReceiverTime.setVisibility(View.VISIBLE);
//                holder.msgReceiverTime.setText(formattedSendTime);
//
//                final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
//                    @Override
//                    public boolean onSingleTapUp(MotionEvent e) {
//                        // This is the click event: Open full screen image viewer
//                        Log.d(TAG, "Receiver image clicked: " + message.getFirebaseMessageId());
//                        Intent intent = new Intent(context, ChatImgFullScreenViewer.class);
//                        intent.putExtra("imageUrl", messageContent); // Pass the Base64 string
//                        context.startActivity(intent);
//                        return true; // Consume the event
//                    }
//
//                    @Override
//                    public void onLongPress(MotionEvent e) {
//                        // This is the long click event: Trigger delete dialog
//                        Log.d(TAG, "Receiver image long pressed: " + message.getFirebaseMessageId());
//                        if (longClickListener != null) {
//                            longClickListener.onMessageLongClick(message);
//                        }
//                        // Note: onLongPress is void, no return value needed
//                    }
//
//                    // Override onDown to ensure it always returns true
//                    @Override
//                    public boolean onDown(MotionEvent e) {
//                        return true;
//                    }
//                });
//
//                holder.receiverImageView.setOnTouchListener((v, event) -> {
//                    // Pass the touch event to the gesture detector
//                    boolean handled = gestureDetector.onTouchEvent(event);
//                    return handled; // Return true if detector handled it
//                });
//
//                // Remove any previous direct listeners just in case (optional but safe)
//                holder.receiverImageView.setOnClickListener(null);
//                holder.receiverImageView.setOnLongClickListener(null);
//
//
//
//                holder.receiverImageView.setOnLongClickListener(null); // Remove listener
//                holder.msgSeenTime.setVisibility(View.GONE); // Hide seen status for receiver
//            }
//        }
//
////        // --- Optional: Long press listener for text messages (copy) ---
////        if (fromUserID.equals(currentUserID)) {
////            holder.senderMessageText.setOnLongClickListener(v -> {
////                // Implement copy logic
////                Log.d(TAG, "Long pressed sender text: " + message.getFirebaseMessageId());
////                Toast.makeText(context, "Copy logic needed.", Toast.LENGTH_SHORT).show();
////                return true;
////            });
////        } else {
////            holder.receiverMessageText.setOnLongClickListener(v -> {
////                // Implement copy logic
////                Log.d(TAG, "Long pressed receiver text: " + message.getFirebaseMessageId());
////                Toast.makeText(context, "Copy logic needed.", Toast.LENGTH_SHORT).show();
////                return true;
////            });
////        }
//    }





@Override
public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
    MessageEntity message = userMessagesList.get(position);

    if (message == null || currentUserID == null) {
        Log.e(TAG, "Null message or currentUserID at position " + position);
        // Hide everything for null message
        holder.senderLayout.setVisibility(View.GONE);
        holder.receiverLayout.setVisibility(View.GONE);
        if(holder.systemMessageLayout != null) holder.systemMessageLayout.setVisibility(View.GONE); // <-- Hide system layout too
        return;
    }

    // Ensure the item view is visible if it was hidden previously
    holder.itemView.setVisibility(View.VISIBLE);
    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


    String fromUserID = message.getFrom();
    String messageType = message.getType();
    String messageContent = message.getMessage();
    long timestamp = message.getTimestamp();
    boolean isSeen = message.isSeen();
    String seenTime = message.getSeenTime();
    String status = message.getStatus();

    String formattedSendTime = formatTimestamp(timestamp);

    // --- Hide ALL main message layouts initially ---
    holder.senderLayout.setVisibility(View.GONE);
    holder.receiverLayout.setVisibility(View.GONE);
    if(holder.systemMessageLayout != null) holder.systemMessageLayout.setVisibility(View.GONE); // <-- Hide system layout


    // --- Bind Data Based on Message Type ---
    int viewType = getItemViewType(position); // Get the view type for this position

    if (viewType == VIEW_TYPE_SYSTEM_MESSAGE) { // --- NEW: Handle System Message ---
        if(holder.systemMessageLayout != null && holder.systemMessageText != null) {
            holder.systemMessageLayout.setVisibility(View.VISIBLE);
            // Assuming the messageContent for system_key_change is the string you want to display
            holder.systemMessageText.setText(messageContent); // Display the stored content

            // System messages typically don't have sender/receiver specific times or seen status
            // These are already hidden by the GONE visibility of sender/receiver layouts
        } else {
            Log.e(TAG, "System message layout or TextView not found for view type " + viewType);
        }

        // System messages should not be long-clickable for copy/delete etc.
        holder.itemView.setOnLongClickListener(null);
        // Ensure specific views within sender/receiver layouts also have null long click listeners if they could overlap
        holder.senderMessageText.setOnLongClickListener(null);
        holder.receiverMessageText.setOnLongClickListener(null);
        holder.senderImageView.setOnLongClickListener(null);
        holder.receiverImageView.setOnLongClickListener(null);


    } else if (viewType == VIEW_TYPE_SENDER_TEXT || viewType == VIEW_TYPE_SENDER_IMAGE) { // --- Handle Sender Messages ---
        holder.senderLayout.setVisibility(View.VISIBLE);

        if (viewType == VIEW_TYPE_SENDER_TEXT) {
            holder.senderMessageText.setVisibility(View.VISIBLE);
            holder.senderImageView.setVisibility(View.GONE); // Hide image view
            holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout); // Set correct bubble drawable
            holder.senderMessageText.setText(messageContent); // Display decrypted/processed text

            // Long Click Listener for Sender Text
            holder.senderMessageText.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onMessageLongClick(message);
                }
                return true; // Consume the long click
            });
            holder.itemView.setOnLongClickListener(null); // Avoid long click on item if text is used

        } else if (viewType == VIEW_TYPE_SENDER_IMAGE) {
            holder.senderImageView.setVisibility(View.VISIBLE);
            holder.senderMessageText.setVisibility(View.GONE); // Hide text view
            decodeBase64AndSetImage(messageContent, holder.senderImageView); // Load image

            // Set OnTouchListener for Click and Long Press for Sender Image
            // (Keep the OnTouchListener logic from your original code for image clicks/long clicks)
            final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onSingleTapUp(MotionEvent e) { /* ... click logic ... */
                    Log.d(TAG, "Sender image clicked: " + message.getFirebaseMessageId());
                    Intent intent = new Intent(context, ChatImgFullScreenViewer.class);
                    intent.putExtra("imageUrl", messageContent); // Pass the Base64 string
                    context.startActivity(intent);
                    return true; }
                @Override public void onLongPress(MotionEvent e) { /* ... long click logic ... */
                    Log.d(TAG, "Sender image long pressed: " + message.getFirebaseMessageId());
                    if (longClickListener != null) { longClickListener.onMessageLongClick(message); }
                }
                @Override public boolean onDown(MotionEvent e) { return true; } // Important
            });
            holder.senderImageView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
            holder.itemView.setOnLongClickListener(null); // Avoid long click on item if image is used


        }

        // Time and Seen Status for Sender Messages
        holder.msgSendTime.setVisibility(View.VISIBLE);
        holder.msgSendTime.setText(formattedSendTime);

        holder.msgSeenTime.setVisibility(View.VISIBLE);
        String statusText = "";
        int statusColor = context.getResources().getColor(android.R.color.darker_gray); // Default color

        if ("failed".equals(status)) {
            statusText = "Failed";
            statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
        } else if (isSeen && seenTime != null && !seenTime.isEmpty()) {
            statusText = "Seen at " + seenTime;
            statusColor = context.getResources().getColor(android.R.color.holo_blue_dark);
        } else {
            statusText = "Sent";
            statusColor = context.getResources().getColor(android.R.color.darker_gray);
        }
        holder.msgSeenTime.setText(statusText);
        holder.msgSeenTime.setTextColor(statusColor);

        // Make Sender Layout long clickable as a fallback or if no specific view handled it
        holder.senderLayout.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onMessageLongClick(message);
            }
            return true;
        });


    } else if (viewType == VIEW_TYPE_RECEIVER_TEXT || viewType == VIEW_TYPE_RECEIVER_IMAGE) { // --- Handle Receiver Messages ---
        holder.receiverLayout.setVisibility(View.VISIBLE);
        holder.receiverProfileImage.setVisibility(View.VISIBLE);
        decodeBase64AndSetImage(chatPartnerProfileImage, holder.receiverProfileImage); // Load partner's profile image

        if (viewType == VIEW_TYPE_RECEIVER_TEXT) {
            holder.receiverMessageText.setVisibility(View.VISIBLE);
            holder.receiverImageView.setVisibility(View.GONE); // Hide image view
            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout); // Set correct bubble drawable
            holder.receiverMessageText.setText(messageContent); // Display decrypted/processed text

            // Long Click Listener for Receiver Text
            holder.receiverMessageText.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onMessageLongClick(message);
                }
                return true; // Consume the long click
            });
            holder.itemView.setOnLongClickListener(null); // Avoid long click on item if text is used

        } else if (viewType == VIEW_TYPE_RECEIVER_IMAGE) {
            holder.receiverImageView.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setVisibility(View.GONE); // Hide text view
            decodeBase64AndSetImage(messageContent, holder.receiverImageView); // Load image

            // Set OnTouchListener for Click and Long Press for Receiver Image
            // (Keep the OnTouchListener logic from your original code for image clicks/long clicks)
            final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onSingleTapUp(MotionEvent e) { /* ... click logic ... */
                    Log.d(TAG, "Receiver image clicked: " + message.getFirebaseMessageId());
                    Intent intent = new Intent(context, ChatImgFullScreenViewer.class);
                    intent.putExtra("imageUrl", messageContent); // Pass the Base64 string
                    context.startActivity(intent);
                    return true; }
                @Override public void onLongPress(MotionEvent e) { /* ... long click logic ... */
                    Log.d(TAG, "Receiver image long pressed: " + message.getFirebaseMessageId());
                    if (longClickListener != null) { longClickListener.onMessageLongClick(message); }
                }
                @Override public boolean onDown(MotionEvent e) { return true; } // Important
            });
            holder.receiverImageView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
            holder.itemView.setOnLongClickListener(null); // Avoid long click on item if image is used

        }

        // Time for Receiver Messages
        holder.msgReceiverTime.setVisibility(View.VISIBLE);
        holder.msgReceiverTime.setText(formattedSendTime);

        holder.msgSendTime.setVisibility(View.GONE); // Hide sender time
        holder.msgSeenTime.setVisibility(View.GONE); // Hide seen status


        // Make Receiver Layout long clickable as a fallback
        holder.receiverLayout.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onMessageLongClick(message);
            }
            return true;
        });

    } else {
        // --- Handle Unknown or Invalid Message Type ---
        Log.w(TAG, "Unknown message type or view type: " + messageType + " at position " + position);
        // You could potentially display a generic error message here using the system message layout
        if(holder.systemMessageLayout != null && holder.systemMessageText != null) {
            holder.systemMessageLayout.setVisibility(View.VISIBLE);
            holder.systemMessageText.setText("[Unsupported Message Type]"); // Or messageContent if you want to show raw
        }
        holder.itemView.setOnLongClickListener(null);
        holder.senderMessageText.setOnLongClickListener(null);
        holder.receiverMessageText.setOnLongClickListener(null);
        holder.senderImageView.setOnLongClickListener(null);
        holder.receiverImageView.setOnLongClickListener(null);
    }
}


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    // --- Helper Methods for Image Loading ---
    private void decodeBase64AndSetImage(String base64String, CircleImageView imageView) {
        if (base64String != null && !base64String.isEmpty()) {
            try {
                Glide.with(context)
                        .load("data:image/jpeg;base64," + base64String)
                        .placeholder(R.drawable.default_profile_img)
                        .error(R.drawable.default_profile_img)
                        .into(imageView);
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile image: " + e.getMessage());
                imageView.setImageResource(R.drawable.default_profile_img);
            }
        } else {
            imageView.setImageResource(R.drawable.default_profile_img);
        }
    }

    private void decodeBase64AndSetImage(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageView.setImageBitmap(decodedBitmap);
                imageView.setVisibility(View.VISIBLE);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid Base64 string for message image: " + e.getMessage());
                imageView.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.e(TAG, "Error decoding message image: " + e.getMessage());
                imageView.setVisibility(View.GONE);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    // --- Date Formatting Helper Method ---
    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) {
            return ""; // Or some default
        }

        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTimeInMillis(timestamp);

        Calendar now = Calendar.getInstance();

        // Check if it's Today
        if (messageCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                messageCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return timeFormat.format(new Date(timestamp));

        } else {
            // Calculate yesterday
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);

            // Check if it's Yesterday
            if (messageCalendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    messageCalendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                return "Yesterday, " + timeFormat.format(new Date(timestamp));

            } else {
                // For older dates
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault());
                return dateTimeFormat.format(new Date(timestamp));
            }
        }
    }
    // ------------------------------------
}


