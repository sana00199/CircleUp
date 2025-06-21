



package com.sana.circleup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64; // Use android.util.Base64
import android.util.Log;
import android.view.LayoutInflater;
// ... existing imports ...
import android.view.GestureDetector; // Add this import
import android.view.MotionEvent;    // Add this import
// ... rest of your imports ...
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.sana.circleup.drawingboard_chatgroup.YOUR_DRAWING_ACTIVITY_CLASS;
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




public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {


    private static final String TAG = "MessageAdapter";
    private final List<MessageEntity> userMessagesList;
    private final Context context;
    private final String chatPartnerProfileImage;
    private final String currentUserID;


    private static final int VIEW_TYPE_SYSTEM_MESSAGE = 5;
    // --- ADD NEW CONSTANT HERE ---
    private static final int VIEW_TYPE_DRAWING_LINK = 6;
    private static final int VIEW_TYPE_SENDER_TEXT = 1;
    private static final int VIEW_TYPE_RECEIVER_TEXT = 2;
    private static final int VIEW_TYPE_SENDER_IMAGE = 3;
    private static final int VIEW_TYPE_RECEIVER_IMAGE = 4;
   // <-- NEW View Type



    // --- Interface for Long Click ---
    public interface OnMessageLongClickListener {
        void onMessageLongClick(MessageEntity message); // Pass the clicked message
    }

    private OnMessageLongClickListener longClickListener; // Listener field

    // Setter for the listener (You can still use this if you prefer setting after constructor)
    public void setOnMessageLongClickListener(OnMessageLongClickListener listener) {
        this.longClickListener = listener;
    }


    public interface OnMessageClickListener {
        void onMessageClicked(MessageEntity message); // Pass the clicked message entity
    }
// *** END NEW Interface ***


    // *** NEW LISTENER MEMBER FOR TAPS ***
    private OnMessageClickListener messageClickListener; // Listener field for single taps
    // *** END NEW MEMBER ***


    // Adapter Constructor - Add NEW tap listener parameter
    public MessageAdapter(List<MessageEntity> userMessagesList, Context context, String chatPartnerProfileImage, String currentUserID, OnMessageClickListener clickListener) { // *** MODIFIED: Added OnMessageClickListener parameter ***
        this.userMessagesList = userMessagesList;
        this.context = context;
        this.chatPartnerProfileImage = chatPartnerProfileImage;
        this.currentUserID = currentUserID;
        this.messageClickListener = clickListener; // *** Store the NEW tap listener ***
        Log.d(TAG, "Adapter initialized with Current User ID: " + currentUserID);
    }

    // You can keep or remove the setOnMessageLongClickListener method based on your preference.
    // Setting listeners via the constructor is generally cleaner.


    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout systemMessageLayout;
        public TextView systemMessageText;
        TextView senderMessageText, receiverMessageText, msgSendTime, msgSeenTime, msgReceiverTime;
        CircleImageView receiverProfileImage;
        View senderLayout, receiverLayout;
        ImageView senderImageView, receiverImageView;
        public TextView drawingLinkTextViewReceiver; // Find the receiver's drawing link TextView
        public TextView drawingLinkTextViewSender;

        TextView hiddenImagePlaceholderTextView; // Renamed for clarity

        // *** NEW VIEWS FOR INVISIBLE INK DISPLAY and CONTAINER ***
        public LinearLayout receiverMessageContentContainer; // The container holding receiver text/image/placeholder
        public TextView hiddenMessagePlaceholder;          // The TextView for "[Tap to Reveal]" text (for text messages)
        public ImageView hiddenImagePlaceholder;          // *** NEW: The ImageView for hidden image placeholder (for image messages) ***
        // *** END NEW VIEWS ***

        // Assuming reactionTextView is also inside receiverMessageContentContainer
        // public TextView reactionTextView; // Keep this if you use it


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

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

            drawingLinkTextViewReceiver = itemView.findViewById(R.id.receiver_drawing_link_text_one_to_one);
            drawingLinkTextViewSender = itemView.findViewById(R.id.sender_drawing_link_text_one_to_one);

            // *** FIND NEW VIEWS ***
            // Find the container that has the receiver bubble background (the clickable area for reveal)
            receiverMessageContentContainer = itemView.findViewById(R.id.receiver_message_content_container);
            // Find the new placeholder TextView (for hidden text messages)
            hiddenMessagePlaceholder = itemView.findViewById(R.id.hidden_message_placeholder);
            // Find the new placeholder ImageView (for hidden image messages)
            hiddenImagePlaceholder = itemView.findViewById(R.id.hidden_image_placeholder); // *** FIND THE NEW IMAGEVIEW ***
            // *** END FIND NEW VIEWS ***
            hiddenImagePlaceholderTextView = itemView.findViewById(R.id.hidden_image_placeholder_text); // Find the view
        }
    }


    @Override
    public int getItemViewType(int position) {
        MessageEntity message = userMessagesList.get(position);
        if (message == null) return -1; // Should not happen if list is managed well

        String fromUserID = message.getFrom();
        String messageType = message.getType();


        if ("one_to_one_drawing_session_link".equals(message.getType())) {
            return VIEW_TYPE_DRAWING_LINK; // Return the new constant for drawing links
            }
        // --- Determine View Type based on sender and message type ---
        else if ("system_key_change".equals(messageType)) { // <-- Check for system message type
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
        // Use the single layout file you have for all types
        View view = LayoutInflater.from(context).inflate(R.layout.custom_messages_layout, parent, false);
        return new MessageViewHolder(view);
    }


    // *** MODIFIED onBindViewHolder (Full Implementation) ***
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageEntity message = userMessagesList.get(position);

        // --- Basic Null Checks ---
        if (message == null || currentUserID == null) {
            Log.e(TAG, "Null message or currentUserID at position " + position);
            // Hide the item view entirely if data is invalid
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0)); // Collapse the item height/width
            return;
        }

        // Ensure the item view is visible and has layout params if it was hidden previously
        holder.itemView.setVisibility(View.VISIBLE);
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        // --- Get Message Data ---
        String fromUserID = message.getFrom();
        String messageType = message.getType();
        // messageContent is the processed content from forceRefreshDisplay (decrypted text, Base64 image, or placeholder)
        String messageContent = message.getMessage();
        long timestamp = message.getTimestamp();
        boolean isSeen = message.isSeen();
        String seenTime = message.getSeenTime();
        String status = message.getStatus();
        String senderDisplayName = message.getName();
        String conversationIdFromMessage = message.getConversationId();
        String drawingSessionIdFromMessage = message.getDrawingSessionId();
        // *** GET NEW FIELDS ***
        String displayEffect = message.getDisplayEffect();
        boolean isRevealed = message.isRevealed();
        // *** END GET NEW FIELDS ***

        // Helper marker string used in forceRefreshDisplay for hidden Invisible Ink text
        String INVISIBLE_INK_HIDDEN_MARKER = "[INVISIBLE_INK_HIDDEN]";


        String formattedSendTime = formatTimestamp(timestamp);

        // --- Hide ALL main message layouts initially ---
        // This is important for recycling view holders
        holder.senderLayout.setVisibility(View.GONE);
        holder.receiverLayout.setVisibility(View.GONE);
        if(holder.drawingLinkTextViewReceiver != null) holder.drawingLinkTextViewReceiver.setVisibility(View.GONE);
        if(holder.drawingLinkTextViewSender != null) holder.drawingLinkTextViewSender.setVisibility(View.GONE);
        if(holder.systemMessageLayout != null) holder.systemMessageLayout.setVisibility(View.GONE);


        // --- Bind Data Based on Message Type ---
        int viewType = getItemViewType(position); // Get the view type for this position






        // --- ADD NEW: Handle the Drawing Link View Type ---
        if (viewType == VIEW_TYPE_DRAWING_LINK) {

            Log.d(TAG, "Binding Drawing Link message for ID: " + message.getFirebaseMessageId());

            // Determine if message is from sender or receiver
            boolean isSenderMessage = fromUserID != null && fromUserID.equals(currentUserID);

            // Select the correct TextView and make the parent layout visible
            TextView drawingLinkTextViewToUse = null;
            if (isSenderMessage) {
                if(holder.senderLayout != null) holder.senderLayout.setVisibility(View.VISIBLE);
                if(holder.receiverLayout != null) holder.receiverLayout.setVisibility(View.GONE);

                // Ensure receiver layout is hidden
                drawingLinkTextViewToUse = holder.drawingLinkTextViewSender;

                if(holder.senderMessageText != null) holder.senderMessageText.setVisibility(View.GONE);
                if(holder.senderImageView != null) holder.senderImageView.setVisibility(View.GONE);
                // --- END ADDITION ---
                // Show sender time
                if(holder.msgSendTime != null) holder.msgSendTime.setVisibility(View.VISIBLE);
                if(holder.msgSendTime != null) holder.msgSendTime.setText(formattedSendTime);
                // Show sender seen status if needed for this type (usually yes)
                if(holder.msgSeenTime != null) holder.msgSeenTime.setVisibility(View.VISIBLE);
                // Bind seen status for sender (logic similar to sender text/image below)
                if(holder.msgSeenTime != null) {
                    String statusText = "";
                    int statusColor = context.getResources().getColor(android.R.color.darker_gray); // Default color

                    if ("failed".equals(status)) {
                        statusText = "Failed";
                        statusColor = context.getResources().getColor(R.color.reed); // Use your 'reed' color
                    } else if (isSeen && seenTime != null && !seenTime.isEmpty()) {
                        statusText = "Seen at " + seenTime;
                        statusColor = context.getResources().getColor(android.R.color.holo_blue_dark); // Use blue for seen
                    } else { // Includes 'sent' and 'received' (though 'received' shouldn't show on sender side)
                        statusText = "Sent";
                        statusColor = context.getResources().getColor(android.R.color.darker_gray); // Default color for sent
                    }
                    holder.msgSeenTime.setText(statusText);
                    holder.msgSeenTime.setTextColor(statusColor);
                }


            } else { // It's a receiver message
                if(holder.receiverLayout != null) holder.receiverLayout.setVisibility(View.VISIBLE);
                if(holder.senderLayout != null) holder.senderLayout.setVisibility(View.GONE);
                if(holder.receiverProfileImage!=null) holder.receiverProfileImage.setVisibility(View.VISIBLE);

                if(holder.senderLayout != null) holder.senderLayout.setVisibility(View.GONE); // Ensure sender layout is hidden
                drawingLinkTextViewToUse = holder.drawingLinkTextViewReceiver;

                if(holder.receiverMessageContentContainer != null) holder.receiverMessageContentContainer.setVisibility(View.VISIBLE); // Hide the bubble container
                if(holder.receiverMessageText != null) holder.receiverMessageText.setVisibility(View.GONE);
                if(holder.receiverImageView != null) holder.receiverImageView.setVisibility(View.GONE);
                if(holder.receiverProfileImage != null) holder.receiverProfileImage.setVisibility(View.VISIBLE);

                if(holder.hiddenMessagePlaceholder != null) holder.hiddenMessagePlaceholder.setVisibility(View.GONE);
                if(holder.hiddenImagePlaceholder != null) holder.hiddenImagePlaceholder.setVisibility(View.GONE);
                if(holder.hiddenImagePlaceholderTextView != null) holder.hiddenImagePlaceholderTextView.setVisibility(View.GONE);
                // Show receiver time
                if(holder.msgReceiverTime != null) holder.msgReceiverTime.setVisibility(View.VISIBLE);
                if(holder.msgReceiverTime != null) holder.msgReceiverTime.setText(formattedSendTime);
                // Hide receiver profile image for system/link messages (Optional, based on your design)
                // if(holder.receiverProfileImage != null) holder.receiverProfileImage.setVisibility(View.GONE); // You might want to keep it
            }

            // Ensure the selected drawing link TextView is not null and make it visible
            if (drawingLinkTextViewToUse != null) {
                drawingLinkTextViewToUse.setVisibility(View.VISIBLE);

                // Set the text for the link message
                // Determine the display name based on sender vs receiver
                String linkText;

                 isSenderMessage = fromUserID != null && fromUserID.equals(currentUserID); // Re-calculate or use the one from the outer if/else if you have it

                if (isSenderMessage) {
                    // *** For the SENDER's view of their own message, show "Your" ***
                    linkText = "Your started a shared drawing session.\nTap to join.";
                    Log.d(TAG, "onBindViewHolder: Binding Sender Drawing Link with 'Your'.");

                } else {
                    // *** For the RECEIVER's view, use the sender's actual name ***
                    String senderNameToDisplay = (senderDisplayName != null && !senderDisplayName.isEmpty()) ? senderDisplayName : "Someone";
                    // Use messageContent if forceRefreshDisplay provided it, otherwise use the default format with the name
                    linkText = TextUtils.isEmpty(messageContent) ?
                            senderNameToDisplay + " started a shared drawing session.\nTap to join." : // Default/fallback
                            messageContent; // Use processed content from forceRefreshDisplay (which should contain the sender's name)

                    Log.d(TAG, "onBindViewHolder: Binding Receiver Drawing Link with name: " + senderNameToDisplay);
                }

                // Now set the determined text
                if (drawingLinkTextViewToUse != null) {
                    drawingLinkTextViewToUse.setText(linkText);
                    drawingLinkTextViewToUse.setVisibility(View.VISIBLE); // Make it visible regardless of sender/receiver status
                    // ... rest of the drawing link click listener and long click listener logic ...
                } else {
                    // Handle null drawingLinkTextViewToUse (shouldn't happen if layout is correct)
                    Log.e(TAG, "Drawing link TextView is null for VIEW_TYPE_DRAWING_LINK at position " + position);
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    return; // Stop binding this item
                }


                // --- Set Click Listener for the Drawing Link ---
                // This listener will launch the Drawing Activity when the link is tapped.
                // We need conversationId and sessionId from the MessageEntity.
                final String convId = conversationIdFromMessage;       // Get conversation ID from MessageEntity
                final String sessId = drawingSessionIdFromMessage;     // Get session ID from MessageEntity

                if (!TextUtils.isEmpty(convId) && !TextUtils.isEmpty(sessId)) {
                    drawingLinkTextViewToUse.setOnClickListener(v -> {
                        Log.d(TAG, "Drawing link message clicked. Launching drawing activity for Conversation ID: " + convId + ", Session ID: " + sessId);

                        if (context instanceof Activity) { // Ensure the adapter's context is an Activity
                            Intent intent = new Intent(context, YOUR_DRAWING_ACTIVITY_CLASS.class); // <<< Replace with your actual Drawing Activity Class Name
                            intent.putExtra("conversationId", convId); // Pass Conversation ID
                            intent.putExtra("sessionId", sessId);     // Pass Session ID
                            // Do NOT pass groupId for 1:1 sessions. The DrawingActivity handles this distinction.
                            context.startActivity(intent);

                        } else {
                            // Log an error if context is not an Activity (shouldn't happen if using Activity context)
                            Log.e(TAG, "Adapter context is not an Activity. Cannot launch drawing activity from link click.");
                            // Inform the user on the UI
                            Toast.makeText(context, "Error launching drawing.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Make the TextView clickable and focusable
                    drawingLinkTextViewToUse.setClickable(true);
                    drawingLinkTextViewToUse.setFocusable(true);
                    // Remove long click listener
                    drawingLinkTextViewToUse.setOnLongClickListener(null);
                    drawingLinkTextViewToUse.setLongClickable(false);


                } else {
                    // If conversationId or sessionId is missing in the MessageEntity (data inconsistency),
                    // make the link non-clickable and show an error state.
                    Log.e(TAG, "Drawing link message (" + message.getFirebaseMessageId() + ") is missing conversationId (" + convId + ") or sessionId (" + sessId + "). Cannot launch activity.");
                    drawingLinkTextViewToUse.setOnClickListener(null); // Remove click listener
                    drawingLinkTextViewToUse.setClickable(false); // Make it not clickable
                    drawingLinkTextViewToUse.setText("Error: Drawing link data missing."); // Show error text
                    // Use context.getColor() or ContextCompat.getColor() for color
                    drawingLinkTextViewToUse.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark)); // Set error color
                    // Remove long click listener
                    drawingLinkTextViewToUse.setOnLongClickListener(null);
                    drawingLinkTextViewToUse.setLongClickable(false);
                    // Hide the drawing icon if it represents an error state visually
                    drawingLinkTextViewToUse.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null); // Remove icon
                }

                // --- IMPORTANT: Ensure item view and parent layouts don't intercept clicks ---
                // Since the TextView itself is clickable, ensure the containing layouts or the RecyclerView item view don't handle clicks.
                holder.itemView.setOnClickListener(null);
                holder.itemView.setOnLongClickListener(null);
                if(holder.senderLayout != null) {
                    holder.senderLayout.setOnClickListener(null);
                    holder.senderLayout.setOnLongClickListener(null); // No long click on sender layout
                }
                if(holder.receiverLayout != null) {
                    holder.receiverLayout.setOnClickListener(null);
                    holder.receiverLayout.setOnLongClickListener(null); // No long click on receiver layout
                }


            } else {
                // This case should ideally not happen if the view was found in the ViewHolder constructor
                Log.e(TAG, "Drawing link TextView (sender or receiver) not found in ViewHolder for VIEW_TYPE_DRAWING_LINK at position " + position + ". Layout might be incorrect.");
                // Hide the item view entirely as it cannot be displayed correctly
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0)); // Collapse item
                return; // Stop binding this item
            }

            // Return after handling this specific view type
            return;
        }
        // --- END Handle Drawing Link View Type ---



        else  if (viewType == VIEW_TYPE_SYSTEM_MESSAGE) { // --- Handle System Message ---
            if(holder.systemMessageLayout != null && holder.systemMessageText != null) {
                holder.systemMessageLayout.setVisibility(View.VISIBLE);
                // Assuming the messageContent for system_key_change is the string you want to display (plaintext)
                holder.systemMessageText.setText(messageContent);

                // System messages should not be clickable or long-clickable
                holder.itemView.setOnClickListener(null);
                holder.itemView.setOnLongClickListener(null);
                // Also remove listeners from potential inner views if necessary
                if(holder.senderMessageText != null) { holder.senderMessageText.setOnClickListener(null); holder.senderMessageText.setOnLongClickListener(null); }
                if(holder.receiverMessageText != null) { holder.receiverMessageText.setOnClickListener(null); holder.receiverMessageText.setOnLongClickListener(null); }
                if(holder.senderImageView != null) { holder.senderImageView.setOnClickListener(null); holder.senderImageView.setOnLongClickListener(null); }
                if(holder.receiverImageView != null) { holder.receiverImageView.setOnClickListener(null); holder.receiverImageView.setOnLongClickListener(null); }
                if(holder.receiverMessageContentContainer != null) {
                    holder.receiverMessageContentContainer.setOnClickListener(null);
                    holder.receiverMessageContentContainer.setOnLongClickListener(null);
                }
                if(holder.hiddenMessagePlaceholder != null) { holder.hiddenMessagePlaceholder.setOnClickListener(null); holder.hiddenMessagePlaceholder.setOnLongClickListener(null); }
                if(holder.hiddenImagePlaceholder != null) { holder.hiddenImagePlaceholder.setOnClickListener(null); holder.hiddenImagePlaceholder.setOnLongClickListener(null); }

            } else {
                Log.e(TAG, "System message layout or TextView not found for view type " + viewType + " at position " + position);
            }


        }
        else if (fromUserID != null && fromUserID.equals(currentUserID)) { // --- Handle Sender Messages (from current user) ---
            holder.senderLayout.setVisibility(View.VISIBLE);
            // Sender messages are always displayed normally on the sender's side, regardless of displayEffect
            // The effect is only applied to the recipient's view.

            // Ensure receiver-specific views are hidden and not clickable/long-clickable
            if(holder.receiverLayout != null) holder.receiverLayout.setVisibility(View.GONE);
            if(holder.receiverProfileImage != null) holder.receiverProfileImage.setVisibility(View.GONE);
            if(holder.msgReceiverTime != null) holder.msgReceiverTime.setVisibility(View.GONE);
            if(holder.drawingLinkTextViewReceiver != null) holder.drawingLinkTextViewReceiver.setVisibility(View.GONE);
            if(holder.drawingLinkTextViewSender != null) holder.drawingLinkTextViewSender.setVisibility(View.GONE);
            if(holder.receiverMessageContentContainer != null) {
                holder.receiverMessageContentContainer.setVisibility(View.GONE); // Hide receiver bubble container
                holder.receiverMessageContentContainer.setOnClickListener(null);
                holder.receiverMessageContentContainer.setOnLongClickListener(null);
            }
            if(holder.hiddenMessagePlaceholder != null) {
                holder.hiddenMessagePlaceholder.setVisibility(View.GONE); // Hide text placeholder
                holder.hiddenMessagePlaceholder.setOnClickListener(null);
                holder.hiddenMessagePlaceholder.setOnLongClickListener(null);
            }
            if(holder.hiddenImagePlaceholder != null) {
                holder.hiddenImagePlaceholder.setVisibility(View.GONE); // Hide image placeholder
                holder.hiddenImagePlaceholder.setOnClickListener(null);
                holder.hiddenImagePlaceholder.setOnLongClickListener(null);
            }
            // Hide reactionTextView for sender messages (if used)
            // if (holder.reactionTextView != null) { holder.reactionTextView.setVisibility(View.GONE); holder.reactionTextView.setOnClickListener(null); holder.reactionTextView.setOnLongClickListener(null); }


            if (viewType == VIEW_TYPE_SENDER_TEXT) {
                if(holder.senderMessageText != null) holder.senderMessageText.setVisibility(View.VISIBLE);
                if(holder.senderImageView != null) holder.senderImageView.setVisibility(View.GONE); // Hide image view

                // messageContent for sender text is the decrypted/plaintext from forceRefreshDisplay
                if(holder.senderMessageText != null) {
                    holder.senderMessageText.setText(messageContent);
                    // Long Click Listener for Sender Text (attach to TextView)
                    holder.senderMessageText.setOnLongClickListener(v -> {
                        if (longClickListener != null) {
                            longClickListener.onMessageLongClick(message);
                        }
                        return true; // Consume the long click
                    });
                    holder.senderMessageText.setLongClickable(true);
                    // No tap listener for sender text messages
                    holder.senderMessageText.setOnClickListener(null);
                    holder.senderMessageText.setClickable(false);
                }


                // Ensure item view or senderLayout don't have click listeners that interfere
                holder.itemView.setOnClickListener(null); holder.itemView.setOnLongClickListener(null);
                holder.senderLayout.setOnClickListener(null);
                // Keep long click on senderLayout as fallback if desired:
                // holder.senderLayout.setOnLongClickListener(v -> {
                //    if (longClickListener != null) {
                //        longClickListener.onMessageLongClick(message);
                //    } return true; });
                // if(holder.senderLayout != null) holder.senderLayout.setLongClickable(true);


            }
            else if (viewType == VIEW_TYPE_SENDER_IMAGE) {
                if(holder.senderImageView != null) holder.senderImageView.setVisibility(View.VISIBLE);
                if(holder.senderMessageText != null) holder.senderMessageText.setVisibility(View.GONE); // Hide text view

                // messageContent for sender image is the decrypted/Base64 image from forceRefreshDisplay
                if(holder.senderImageView != null) {
                    decodeBase64AndSetImage(messageContent, holder.senderImageView); // Load image

                    // Set OnTouchListener for Click and Long Press for Sender Image
                    final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                        @Override public boolean onSingleTapUp(MotionEvent e) {
                            Log.d(TAG, "Sender image clicked: " + message.getFirebaseMessageId());
                            // Assuming messageContent for image type is the Base64 string of the image
                            Intent intent = new Intent(context, ChatImgFullScreenViewer.class);
                            intent.putExtra("imageUrl", messageContent); // Pass the Base64 string
                            context.startActivity(intent);
                            return true;
                        }
                        @Override public void onLongPress(MotionEvent e) {
                            Log.d(TAG, "Sender image long pressed: " + message.getFirebaseMessageId());
                            if (longClickListener != null) {
                                longClickListener.onMessageLongClick(message);
                            }
                        }
                        @Override public boolean onDown(MotionEvent e) { return true; } // Important for gesture detection
                    });
                    holder.senderImageView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
                    holder.senderImageView.setClickable(true);
                    holder.senderImageView.setLongClickable(true); // Make image long-clickable
                }

                // Ensure item view or senderLayout don't have click listeners that interfere
                holder.itemView.setOnClickListener(null); holder.itemView.setOnLongClickListener(null);
                holder.senderLayout.setOnClickListener(null);
                // Keep long click on senderLayout as fallback if desired:
                // holder.senderLayout.setOnLongClickListener(v -> {
                //    if (longClickListener != null) {
                //        longClickListener.onMessageLongClick(message);
                //    } return true; });
                // if(holder.senderLayout != null) holder.senderLayout.setLongClickable(true);


            } else {
                // Default/Unknown sender type
                Log.w(TAG, "Unknown sender message type: " + messageType + " at position " + position + ". Message: " + message);
                if(holder.senderMessageText != null) {
                    holder.senderMessageText.setVisibility(View.VISIBLE);
                    if(holder.senderImageView != null) holder.senderImageView.setVisibility(View.GONE);
                    holder.senderMessageText.setText("[Unsupported Sender Message]");
                    // Remove listeners
                    holder.senderMessageText.setOnClickListener(null);
                    holder.senderMessageText.setOnLongClickListener(null);
                }
                holder.itemView.setOnClickListener(null); holder.itemView.setOnLongClickListener(null);
                holder.senderLayout.setOnClickListener(null); holder.senderLayout.setOnLongClickListener(null);
            }

            // Time and Seen Status for Sender Messages
            if(holder.msgSendTime != null) {
                holder.msgSendTime.setVisibility(View.VISIBLE);
                holder.msgSendTime.setText(formattedSendTime);
            }

            if(holder.msgSeenTime != null) {
                holder.msgSeenTime.setVisibility(View.VISIBLE);
                String statusText = "";
                int statusColor = context.getResources().getColor(android.R.color.darker_gray); // Default color

                if ("failed".equals(status)) {
                    statusText = "Failed";
                    // Use context.getColor() if targeting API 23+ or ContextCompat.getColor()
                    statusColor = context.getResources().getColor(R.color.reed); // Use your 'reed' color for failed
                } else if (isSeen && seenTime != null && !seenTime.isEmpty()) {
                    statusText = "Seen at " + seenTime;
                    // Use context.getColor() or ContextCompat.getColor()
                    statusColor = context.getResources().getColor(android.R.color.holo_blue_dark); // Use blue for seen
                } else { // Includes 'sent' and 'received' (though 'received' shouldn't show on sender side)
                    statusText = "Sent";
                    // Use context.getColor() or ContextCompat.getColor()
                    statusColor = context.getResources().getColor(android.R.color.darker_gray); // Default color for sent
                }
                holder.msgSeenTime.setText(statusText);
                // Use context.getColor() if targeting API 23+ or ContextCompat.getColor()
                holder.msgSeenTime.setTextColor(statusColor);
            }

        } else { // --- Handle Receiver Messages (from chat partner) ---
            holder.receiverLayout.setVisibility(View.VISIBLE);

            // Ensure sender-specific views are hidden and not clickable/long-clickable
            if(holder.senderLayout != null) holder.senderLayout.setVisibility(View.GONE);
            if(holder.senderMessageText != null) holder.senderMessageText.setVisibility(View.GONE);
            if(holder.senderImageView != null) holder.senderImageView.setVisibility(View.GONE);
            if(holder.msgSendTime != null) holder.msgSendTime.setVisibility(View.GONE);
            if(holder.msgSeenTime != null) holder.msgSeenTime.setVisibility(View.GONE);


            // Load receiver profile image
            if(holder.receiverProfileImage != null) {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                // Use chatPartnerProfileImage passed to adapter constructor
                decodeBase64AndSetImage(chatPartnerProfileImage, holder.receiverProfileImage);
                // No click/long-click on profile image here usually
                holder.receiverProfileImage.setOnClickListener(null);
                holder.receiverProfileImage.setOnLongClickListener(null);
            }

            // Time for Receiver Messages
            if(holder.msgReceiverTime != null) {
                holder.msgReceiverTime.setVisibility(View.VISIBLE);
                holder.msgReceiverTime.setText(formattedSendTime);
                // No click/long-click on time here usually
                holder.msgReceiverTime.setOnClickListener(null);
                holder.msgReceiverTime.setOnLongClickListener(null);
            }

            // Hide reactionTextView for receiver messages for now (unless you use it)
            // if (holder.reactionTextView != null) { holder.reactionTextView.setVisibility(View.GONE); holder.reactionTextView.setOnClickListener(null); holder.reactionTextView.setOnLongClickListener(null); }


            // Get the clickable area for the message bubble
            View clickableArea = (holder.receiverMessageContentContainer != null) ? holder.receiverMessageContentContainer : holder.itemView;
            if (clickableArea == null) {
                Log.e(TAG, "Clickable area for receiver message is null at position " + position + "!");
                // Cannot bind correctly if clickable area is null
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                return;
            }


            if (viewType == VIEW_TYPE_RECEIVER_TEXT) {
                // Ensure receiver image view is hidden for text messages
                if(holder.receiverImageView != null) holder.receiverImageView.setVisibility(View.GONE);
                if(holder.hiddenImagePlaceholder != null) holder.hiddenImagePlaceholder.setVisibility(View.GONE); // Hide image placeholder

                // *** Handle Invisible Ink Display Logic for Receiver TEXT ***
                // Check if the message is Invisible Ink AND if it's NOT yet revealed locally
                // Also check if the messageContent is the special hidden marker
                if ("invisible_ink".equals(displayEffect) && !isRevealed && INVISIBLE_INK_HIDDEN_MARKER.equals(messageContent)) {
                    // Message is Invisible Ink TEXT and NOT yet revealed, and forceRefreshDisplay marked it hidden
                    Log.d(TAG, "Binding hidden Invisible Ink text message for receiver: " + message.getFirebaseMessageId());

                    // Show the text placeholder and hide the actual text TextView
                    if(holder.receiverMessageText != null) holder.receiverMessageText.setVisibility(View.GONE);
                    if(holder.hiddenMessagePlaceholder != null) {
                        holder.hiddenMessagePlaceholder.setVisibility(View.VISIBLE);
                        // Set a placeholder text to indicate it's hidden and how to reveal
                        holder.hiddenMessagePlaceholder.setText("Tap to Reveal \uD83D\uDC7B");
                        holder.hiddenMessagePlaceholder.setTextColor(context.getResources().getColor(android.R.color.white));
                        // Use context.getColor() or ContextCompat.getColor()
                        holder.hiddenMessagePlaceholder.setTextColor(context.getResources().getColor(R.color.dark_gray)); // Use your 'dark_gray' color
                        holder.hiddenMessagePlaceholder.setTypeface(null, Typeface.ITALIC);
                    } else {
                        // Fallback if placeholder view is missing (shouldn't happen if layout is correct)
                        Log.e(TAG, "hiddenMessagePlaceholder is null for Invisible Ink text message!");
                        if(holder.receiverMessageText != null) {
                            // Show the actual text (if available in the original MessageEntity) but maybe with some error indicator?
                            // Note: messageContent is the MARKER here, not the actual text.
                            // We would need to access the original decrypted text here, which is complicated.
                            // Best to just show an error placeholder if the layout is broken.
                            holder.receiverMessageText.setVisibility(View.VISIBLE);
                            holder.receiverMessageText.setText("[Error: Placeholder missing]");
                            holder.receiverMessageText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                            holder.receiverMessageText.setTypeface(null, Typeface.NORMAL);
                        }
                    }

                    // *** Attach a tap click listener to the message bubble container to trigger revelation ***
                    clickableArea.setOnClickListener(v -> {
                        Log.d(TAG, "Hidden Invisible Ink text message clicked: " + message.getFirebaseMessageId());
                        // Call the listener provided by the Activity
                        if (messageClickListener != null) {
                            messageClickListener.onMessageClicked(message); // Pass the message entity
                        }
                    });
                    clickableArea.setClickable(true); // Make sure this view is clickable

                    // Remove long click listener from the clickable area to avoid conflicts with tap
                    clickableArea.setOnLongClickListener(null);
                    clickableArea.setLongClickable(false);


                    // Also remove listeners from individual views inside if they exist and could interfere
                    if(holder.receiverMessageText != null) { holder.receiverMessageText.setOnClickListener(null); holder.receiverMessageText.setOnLongClickListener(null); holder.receiverMessageText.setClickable(false); holder.receiverMessageText.setLongClickable(false); }
                    if(holder.hiddenMessagePlaceholder != null) { holder.hiddenMessagePlaceholder.setOnClickListener(null); holder.hiddenMessagePlaceholder.setOnLongClickListener(null); holder.hiddenMessagePlaceholder.setClickable(false); holder.hiddenMessagePlaceholder.setLongClickable(false); } // Placeholder itself is not clickable for reveal
                    if(holder.receiverImageView != null) { holder.receiverImageView.setOnClickListener(null); holder.receiverImageView.setOnLongClickListener(null); holder.receiverImageView.setClickable(false); holder.receiverImageView.setLongClickable(false); } // Should be GONE
                    if(holder.hiddenImagePlaceholder != null) { holder.hiddenImagePlaceholder.setOnClickListener(null); holder.hiddenImagePlaceholder.setOnLongClickListener(null); holder.hiddenImagePlaceholder.setClickable(false); holder.hiddenImagePlaceholder.setLongClickable(false); } // Should be GONE

                } else {
                    // Message is normal text OR Invisible Ink text and ALREADY revealed OR the marker is missing
                    // Display normally using the actual decrypted text
                    Log.d(TAG, "Binding normal or revealed receiver text message: " + message.getFirebaseMessageId() + ", Revealed: " + isRevealed + ", Effect: " + displayEffect);

                    // Show the actual text TextView and hide the placeholder
                    if(holder.receiverMessageText != null) {
                        holder.receiverMessageText.setVisibility(View.VISIBLE);
                        // messageContent here is the decrypted/plaintext from forceRefreshDisplay (because it wasn't overridden with the marker)
                        holder.receiverMessageText.setText(messageContent);
                        // Reset any placeholder specific styling
                        // Use context.getColor() or ContextCompat.getColor()
                        holder.receiverMessageText.setTextColor(context.getResources().getColor(android.R.color.black)); // Use black or your default text color
                        holder.receiverMessageText.setTypeface(null, Typeface.NORMAL); // Reset typeface if italic was applied
                    }
                    if(holder.hiddenMessagePlaceholder != null) holder.hiddenMessagePlaceholder.setVisibility(View.GONE);


                    // Remove the tap click listener as the message is revealed/normal
                    clickableArea.setOnClickListener(null); // Remove listener
                    clickableArea.setClickable(false); // Make sure it's not clickable by tap

                    // Re-attach or ensure the long click listener is active for copying/deleting
                    if(holder.receiverMessageText != null) { // Attach long click to the TextView
                        holder.receiverMessageText.setOnLongClickListener(v -> {
                            if (longClickListener != null) {
                                longClickListener.onMessageLongClick(message);
                            }
                            return true; // Consume the long click
                        });
                        holder.receiverMessageText.setLongClickable(true); // Make text long-clickable
                    } else {
                        // Fallback: make the container long clickable if text view is null (shouldn't happen for text messages)
                        clickableArea.setOnLongClickListener(v -> {
                            if (longClickListener != null) {
                                longClickListener.onMessageLongClick(message);
                            }
                            return true;
                        });
                        clickableArea.setLongClickable(true);
                    }

                    // Ensure other views inside the container don't have listeners that interfere
                    if(holder.hiddenMessagePlaceholder != null) { holder.hiddenMessagePlaceholder.setOnClickListener(null); holder.hiddenMessagePlaceholder.setOnLongClickListener(null); holder.hiddenMessagePlaceholder.setClickable(false); holder.hiddenMessagePlaceholder.setLongClickable(false); }
                    if(holder.receiverImageView != null) { holder.receiverImageView.setOnClickListener(null); holder.receiverImageView.setOnLongClickListener(null); holder.receiverImageView.setClickable(false); holder.receiverImageView.setLongClickable(false); } // Should be GONE
                    if(holder.hiddenImagePlaceholder != null) { holder.hiddenImagePlaceholder.setOnClickListener(null); holder.hiddenImagePlaceholder.setOnLongClickListener(null); holder.hiddenImagePlaceholder.setClickable(false); holder.hiddenImagePlaceholder.setLongClickable(false); } // Should be GONE
                }
                // *** End Handle Invisible Ink Display Logic for TEXT ***


            } else if (viewType == VIEW_TYPE_RECEIVER_IMAGE) {
                // Ensure receiver text and text placeholder are hidden for image messages
                if(holder.receiverMessageText != null) holder.receiverMessageText.setVisibility(View.GONE);
                if(holder.hiddenMessagePlaceholder != null) holder.hiddenMessagePlaceholder.setVisibility(View.GONE); // Hide text placeholder


                // *** Handle Invisible Ink Display Logic for Receiver IMAGE ***
                // Check if the message is Invisible Ink AND if it's NOT yet revealed locally
                // Note: messageContent for images is the decrypted Base64 string or error placeholder, NOT the text marker.
                // forceRefreshDisplay does *not* override image content with the text marker.
                if ("invisible_ink".equals(displayEffect) && !isRevealed) {
                    // Message is Invisible Ink IMAGE and NOT yet revealed
                    Log.d(TAG, "Binding hidden Invisible Ink IMAGE for receiver: " + message.getFirebaseMessageId());

                    // Hide the actual image ImageView and show the image placeholder ImageView
                    if(holder.receiverImageView != null) holder.receiverImageView.setVisibility(View.GONE);
                    holder.receiverImageView.setOnTouchListener(null);


                    if(holder.hiddenImagePlaceholder != null) {
                        holder.hiddenImagePlaceholder.setVisibility(View.VISIBLE);
                        // We are showing the entire included layout.
                        // The children (icon and text) within this layout will respect
                        // their own XML visibility or any visibility set on them directly.
                        // Ensure you are NOT hiding the text view here by mistake!
                        // if(holder.hiddenImagePlaceholderText != null) holder.hiddenImagePlaceholderText.setVisibility(View.GONE); // <-- Remove this line if it exists here
                    } else {
                        // Fallback if includedHiddenImagePlaceholderView is null
                        Log.e(TAG, "includedHiddenImagePlaceholderView is null for Invisible Ink Image! Cannot show placeholder.");
                        if (holder.receiverImageView != null) {
                            holder.receiverImageView.setVisibility(View.VISIBLE);
                        }
                    }


                    if(holder.hiddenImagePlaceholderTextView != null) {
                        holder.hiddenImagePlaceholderTextView.setVisibility(View.VISIBLE);
                        // Aap chahein toh yahan text ya style override kar sakte hain, jese ki XML me set kiya hua hai
                    }


                    if(holder.hiddenImagePlaceholder != null) { // *** Use the NEW image placeholder ImageView ***
                        holder.hiddenImagePlaceholder.setVisibility(View.VISIBLE);
                        // The source drawable and tint are set in XML.
                        // You could set text over the image placeholder if needed, but a dedicated TextView might be better.
                    } else {
                        // Fallback if hiddenImagePlaceholder is null (shouldn't happen)
                        Log.e(TAG, "hiddenImagePlaceholder is null for Invisible Ink Image!");
                        if(holder.receiverImageView != null) {
                            // For simplicity, just show the actual image but log the error.
                            holder.receiverImageView.setVisibility(View.VISIBLE);
                        }
                    }


                    // *** Attach a tap click listener to the message bubble container to trigger revelation ***
                    // Use the receiverMessageContentContainer if found, otherwise item view as fallback
                    // clickableArea is already defined and null-checked before the inner blocks.
                    clickableArea.setOnClickListener(v -> {
                        Log.d(TAG, "Hidden Invisible Ink IMAGE clicked: " + message.getFirebaseMessageId());
                        // Call the listener provided by the Activity
                        if (messageClickListener != null) {
                            messageClickListener.onMessageClicked(message); // Pass the message entity
                        }
                    });
                    clickableArea.setClickable(true); // Make sure this view is clickable

                    // Remove long click listener from the clickable area to avoid conflicts with tap
                    clickableArea.setOnLongClickListener(null);
                    clickableArea.setLongClickable(false);

                    // Also remove listeners from individual views inside if they exist and could interfere
                    if(holder.receiverImageView != null) {
                        holder.receiverImageView.setOnClickListener(null); holder.receiverImageView.setOnLongClickListener(null); holder.receiverImageView.setClickable(false); holder.receiverImageView.setLongClickable(false); }
                    if(holder.hiddenImagePlaceholder != null) { holder.hiddenImagePlaceholder.setOnClickListener(null); holder.hiddenImagePlaceholder.setOnLongClickListener(null); holder.hiddenImagePlaceholder.setClickable(false); holder.hiddenImagePlaceholder.setLongClickable(false); } // Placeholder itself is not clickable for reveal
                    if(holder.receiverMessageText != null) { holder.receiverMessageText.setOnClickListener(null); holder.receiverMessageText.setOnLongClickListener(null); holder.receiverMessageText.setClickable(false); holder.receiverMessageText.setLongClickable(false); } // Should be GONE
                    if(holder.hiddenMessagePlaceholder != null) { holder.hiddenMessagePlaceholder.setOnClickListener(null); holder.hiddenMessagePlaceholder.setOnLongClickListener(null); holder.hiddenMessagePlaceholder.setClickable(false); holder.hiddenMessagePlaceholder.setLongClickable(false); } // Should be GONE

                } else {
                    // Message is normal Image OR Invisible Ink Image and ALREADY revealed
                    Log.d(TAG, "Binding normal or revealed receiver image message: " + message.getFirebaseMessageId() + ", Revealed: " + isRevealed + ", Effect: " + displayEffect);

                    // Show the actual image ImageView and hide the image placeholder ImageView
                    if(holder.receiverImageView != null) {
                        holder.receiverImageView.setVisibility(View.VISIBLE);
                        // messageContent here is the decrypted/Base64 image from forceRefreshDisplay
                        decodeBase64AndSetImage(messageContent, holder.receiverImageView); // Load image
                        // Ensure image is displayed normally (remove any temporary styling)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            // If you were using blur on the image view (unlikely with placeholder approach), remove it here.
                            // holder.receiverImageView.setRenderEffect(null);
                        }
                    }
                    if(holder.hiddenImagePlaceholder != null) holder.hiddenImagePlaceholder.setVisibility(View.GONE);
                    // *** YEH LINE YAHAN ADD KAREN: Hide the placeholder text when revealed/normal ***
                    if(holder.hiddenImagePlaceholderTextView != null) {
                        holder.hiddenImagePlaceholderTextView.setVisibility(View.GONE);
                    }
                    // *** END ADDITION ***


                    // Remove the tap click listener as the message is revealed/normal
                    // clickableArea is already defined and null-checked.
                    clickableArea.setOnClickListener(null); // Remove listener
                    clickableArea.setClickable(false); // Make sure it's not clickable by tap

                    // Re-attach the standard OnTouchListener for standard image click (fullscreen) and long press
                    if(holder.receiverImageView != null) { // Attach OnTouchListener to the ImageView itself
                        final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                            @Override public boolean onSingleTapUp(MotionEvent e) {
                                Log.d(TAG, "Revealed Receiver image clicked: " + message.getFirebaseMessageId());
                                // messageContent for image type is the Base64 string of the image
                                Intent intent = new Intent(context, ChatImgFullScreenViewer.class);
                                intent.putExtra("imageUrl", messageContent); // Pass the Base64 string
                                context.startActivity(intent);
                                return true;
                            }
                            @Override public void onLongPress(MotionEvent e) {
                                Log.d(TAG, "Revealed Receiver image long pressed: " + message.getFirebaseMessageId());
                                if (longClickListener != null) {
                                    longClickListener.onMessageLongClick(message);
                                }
                            }
                            @Override public boolean onDown(MotionEvent e) { return true; } // Important
                        });
                        holder.receiverImageView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
                        holder.receiverImageView.setClickable(true);
                        holder.receiverImageView.setLongClickable(true); // Make image long-clickable

                        // Ensure the container/item doesn't interfere with image listeners
                        clickableArea.setOnLongClickListener(null); // Remove long click from container
                    } else {
                        // Fallback: if image view is null, remove all listeners from container/item
                        clickableArea.setOnLongClickListener(null);
                        Log.e(TAG, "receiverImageView is null for receiver image type!");
                    }

                    // Ensure other views inside the container don't have listeners that interfere
                    if(holder.hiddenMessagePlaceholder != null) { holder.hiddenMessagePlaceholder.setOnClickListener(null); holder.hiddenImagePlaceholder.setOnLongClickListener(null); holder.hiddenImagePlaceholder.setClickable(false); holder.hiddenImagePlaceholder.setLongClickable(false); } // Should be GONE
                    if(holder.receiverMessageText != null) { holder.receiverMessageText.setOnClickListener(null); holder.receiverMessageText.setOnLongClickListener(null); holder.receiverMessageText.setClickable(false); holder.receiverMessageText.setLongClickable(false); } // Should be GONE
                    if(holder.hiddenImagePlaceholder != null) { holder.hiddenImagePlaceholder.setOnClickListener(null); holder.hiddenImagePlaceholder.setOnLongClickListener(null); holder.hiddenImagePlaceholder.setClickable(false); holder.hiddenImagePlaceholder.setLongClickable(false); } // Should be GONE
                }
                // *** End Handle Invisible Ink Display Logic for IMAGE ***


            } else {
                // Default/Unknown receiver type
                Log.w(TAG, "Unknown receiver message type: " + messageType + " at position " + position + ". Message: " + message);
                // Try to show the raw message content or an error
                if(holder.receiverMessageText != null) {
                    holder.receiverMessageText.setVisibility(View.VISIBLE);
                    if(holder.receiverImageView != null) holder.receiverImageView.setVisibility(View.GONE);
                    if(holder.hiddenMessagePlaceholder != null) holder.hiddenMessagePlaceholder.setVisibility(View.GONE); // Hide text placeholder
                    if(holder.hiddenImagePlaceholder != null) holder.hiddenImagePlaceholder.setVisibility(View.GONE); // Hide image placeholder
                    // Display content if available, otherwise a generic message
                    holder.receiverMessageText.setText(TextUtils.isEmpty(messageContent) ? "[Unsupported Receiver Message]" : messageContent);
                    // Remove listeners from the text view itself
                    holder.receiverMessageText.setOnClickListener(null);
                    holder.receiverMessageText.setOnLongClickListener(null);
                }
                // Remove listeners from the container/item as it's an unknown type
                // clickableArea is already defined and null-checked.
                clickableArea.setOnClickListener(null);
                clickableArea.setClickable(false);
                clickableArea.setOnLongClickListener(null);
                clickableArea.setLongClickable(false);
            }

            // Hide sender time and seen status for receiver messages
            if(holder.msgSendTime != null) holder.msgSendTime.setVisibility(View.GONE);
            if(holder.msgSeenTime != null) holder.msgSeenTime.setVisibility(View.GONE);


        }
    } // --- End of onBindViewHolder ---


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    // --- Helper Methods for Image Loading ---
    // Keep your existing decodeBase64AndSetImage methods
    private void decodeBase64AndSetImage(String base64String, CircleImageView imageView) {
        if (base64String != null && !base64String.isEmpty()) {
            try {
                // If it's a data URL (e.g., "data:image/jpeg;base64,..."), Glide can load it directly
                if (base64String.startsWith("data:image/")) {
                    Glide.with(context)
                            .load(base64String)
                            .placeholder(R.drawable.default_profile_img)
                            .error(R.drawable.default_profile_img)
                            .into(imageView);
                } else {
                    // Otherwise, assume it's raw Base64 and prepend the data URL prefix for Glide
                    Glide.with(context)
                            .load("data:image/jpeg;base64," + base64String)
                            .placeholder(R.drawable.default_profile_img)
                            .error(R.drawable.default_profile_img)
                            .into(imageView);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile image with Glide: " + e.getMessage());
                imageView.setImageResource(R.drawable.default_profile_img);
            }
        } else {
            imageView.setImageResource(R.drawable.default_profile_img);
        }
    }

    private void decodeBase64AndSetImage(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                // If it's a data URL, remove the prefix for manual decoding
                String cleanBase64 = base64Image.startsWith("data:image/") ? base64Image.substring(base64Image.indexOf(',') + 1) : base64Image;

                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (decodedBitmap != null) {
                    imageView.setImageBitmap(decodedBitmap);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "Failed to decode Base64 to bitmap for message image.");
                    imageView.setVisibility(View.GONE);
                }
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
    // Keep your existing formatTimestamp method
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

} // End of MessageAdapter class