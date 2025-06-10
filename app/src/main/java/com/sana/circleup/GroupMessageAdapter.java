package com.sana.circleup;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot; // Import Firebase classes
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView; // Assuming you use this
 // <<< Make sure this package is correct for GroupMessageAdapter

// --- Android Imports ---
import android.content.Context; // Import Context

// --- AndroidX Imports ---

// --- Third-party Library Imports (Glide) ---

// --- Firebase Imports (Still needed for fetching profile images in onBindViewHolder - noted as potentially inefficient) ---


// --- Other Project Class Imports (Adjust if your packages are different) ---
import com.sana.circleup.drawingboard_chatgroup.YOUR_DRAWING_ACTIVITY_CLASS;
import com.sana.circleup.room_db_implement.GroupMessageEntity;
// import com.sana.circleup.room_db_implement.GroupMessageEntity; // <<< Make sure this points to your GroupMessageEntity file


// --- Standard Java Imports ---
import java.util.Map; // Import Map interface




import android.text.TextUtils; // Import TextUtils

import java.util.ArrayList; // Import ArrayList

// Keep CircleImageView import if used in layout

// Assuming your layout group_custom_msg_layout.xml contains the root LinearLayouts with IDs receiver_layout and sender_layout
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.Toast;


// Standard RecyclerView Adapter for displaying GroupMessageEntity objects from Room
public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder> { // Class definition

    private static final String TAG = "GroupMsgAdapter"; // Define TAG

    // The list now holds GroupMessageEntity objects from Room LiveData
    private List<GroupMessageEntity> groupMessagesList; // Made non-final to be replaceable by setMessages
    private final Context context;
    private final String currentUserId; // <<< Member variable
    // Make currentUserName mutable as it's fetched async (Keep this)
    private String currentUserName; // No longer final

    // Reference to the UsersRef for fetching profile images in onBindViewHolder (Keep this inefficient approach for "asis")
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


    // Constructor - Updated to accept List<GroupMessageEntity>
    public GroupMessageAdapter(List<GroupMessage> groupMessagesList, Context context, String currentUserId, String initialUserName) {
        // Initialize the internal list. It will be updated by the setMessages method later.
        this.groupMessagesList = new ArrayList<>(); // Initialize with an empty list
        // Use the setMessages method to populate the list initially (though LiveData onChanged will likely be the first call with data)
//        if (groupMessagesList != null) {
//            this.groupMessagesList.addAll(groupMessagesList);
//        }

        this.context = context; // Store context
        this.currentUserId = currentUserId; // <<< Store current user ID passed from Activity
        this.currentUserName = initialUserName; // Store initial/placeholder username (will be updated)
    }

    // --- NEW: Method to update username after it's fetched --- (Keep this)
    public void updateCurrentUserName(String newUserName) {
        this.currentUserName = newUserName;
        // Optional: Notify adapter if username display logic depends on it in ViewHolder (e.g., showing "You")
        // For example, if you hid sender name for the current user.
        // If you update the adapter's list via setMessages, this update might be implicitly handled.
        // notifyDataSetChanged(); // Inefficient, consider specific updates if needed
    }

    // --- NEW: Method to update the messages list from LiveData --- (Keep this logic)
    public void setMessages(List<GroupMessageEntity> newMessages) {
        // Using DiffUtil here would be much more efficient for animations and performance
        // on large lists, but for simplicity, we replace the list and notify.
        groupMessagesList.clear(); // Clear the current list
        if (newMessages != null) {
            groupMessagesList.addAll(newMessages); // Add new data from Room LiveData
        }
        notifyDataSetChanged(); // Notify adapter to refresh UI
        // Log.d(TAG, "Adapter data set. New size: " + groupMessagesList.size()); // Too verbose maybe
    }




    // ViewHolder class (should match the IDs in your single message item layout - group_custom_msg_layout.xml)
    // This ViewHolder finds views for both sender and receiver message parts within the single layout.
    public static class GroupMessageViewHolder extends RecyclerView.ViewHolder {
        // These variable names should match the view IDs in your group_custom_msg_layout.xml
        LinearLayout senderLayout, receiverLayout; // Root layouts for sent and received messages
        TextView senderMessageText, senderTimeText; // Sender views
        TextView receiverNameText, receiverMessageText, receiverTimeText; // Receiver views
        CircleImageView receiverProfileImage; // Receiver profile image
        ImageView senderImageView, receiverImageView; // Image message views

        // Add TextViews for read status if you implement it and your layout has these IDs
        TextView senderReadStatus, receiverReadStatus;
        // --- NEW Drawing Link TextViews ---
        TextView senderDrawingLinkText; // <<< NEW
        TextView receiverDrawingLinkText; // <<< NEW
        // --- END NEW ---


        public GroupMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the root layouts for sender and receiver messages
            senderLayout = itemView.findViewById(R.id.sender_layout); // Match your layout IDs
            receiverLayout = itemView.findViewById(R.id.receiver_layout); // Match your layout IDs

            // Find the views within the layouts (assuming all IDs are unique within the single layout)
            senderMessageText = itemView.findViewById(R.id.sender_message_text); // Match your layout IDs
            senderTimeText = itemView.findViewById(R.id.sender_time); // Match your layout IDs
            receiverNameText = itemView.findViewById(R.id.receiver_name); // Match your layout IDs
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text); // Match your layout IDs
            senderImageView = itemView.findViewById(R.id.sender_image_view); // Match your layout IDs (for sent image)
            receiverImageView = itemView.findViewById(R.id.receiver_image_view); // Match your layout IDs (for received image)

            receiverTimeText = itemView.findViewById(R.id.receiver_time); // Match your layout IDs
            receiverProfileImage = itemView.findViewById(R.id.receiver_profile_image); // Match your layout IDs


            senderDrawingLinkText = itemView.findViewById(R.id.sender_drawing_link_text); // <<< NEW
            receiverDrawingLinkText = itemView.findViewById(R.id.receiver_drawing_link_text); // <<< NEW
            // --- END NEW ---


            // Link read status views if added to your layouts (group_custom_msg_layout)
//            senderReadStatus = itemView.findViewById(R.id.sender_read_status); // Example ID
//            receiverReadStatus = itemView.findViewById(R.id.receiver_read_status); // Example ID
        }
    }

    // Since you are using a single layout (group_custom_msg_layout) and controlling visibility,
    // you do NOT need to override getItemViewType or inflate different layouts in onCreateViewHolder.
    // Remove the getItemViewType method and keep the single layout inflation in onCreateViewHolder.

    // REMOVE THIS METHOD:
    /*
     private static final int MESSAGE_TYPE_SENT = 1;
     private static final int MESSAGE_TYPE_RECEIVED = 0;

     @Override
     public int getItemViewType(int position) {
         GroupMessageEntity message = groupMessagesList.get(position);
         if (message.getSenderId().equals(currentUserId)) {
             return MESSAGE_TYPE_SENT;
         } else {
             return MESSAGE_TYPE_RECEIVED;
         }
     }
    */

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the single layout file for all message items
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_custom_msg_layout, parent, false); // <<< Inflate the single layout
        return new GroupMessageViewHolder(view); // Return a new ViewHolder instance with the inflated view
    }


@Override
    public void onBindViewHolder(@NonNull GroupMessageViewHolder holder, int position) {
        // Get the message data for the current position from the list (GroupMessageEntity)
        GroupMessageEntity message = groupMessagesList.get(position); // <<< Use GroupMessageEntity

        String senderId = message.getSenderId(); // Sender's UID from Entity
        String senderName = message.getSenderName(); // Sender's display name from Entity
        String messageContent = message.getMessageContent(); // Message content (text string or Base64 image) from Entity
        String messageTime = message.getTime(); // Client-side time string from Entity (or format timestamp)

        String messageType = message.getMessageType(); // Message type ("text", "image", etc.) from Entity
        String messageId = message.getMessageId(); // Message ID (Firebase push key) from Entity
        String drawingSessionId = message.getDrawingSessionId(); // <<< NEW: Get the session ID


        // --- Control visibility based on sender (Sent vs Received Layouts) ---
        // Show the appropriate layout (sender_layout or receiver_layout) based on the sender ID.
        if (currentUserId != null && currentUserId.equals(senderId)) { // Use adapter's currentUserId
            // Message sent by the current user (Display using sender_layout)
            holder.senderLayout.setVisibility(View.VISIBLE); // Show sender layout
            holder.receiverLayout.setVisibility(View.GONE); // Hide receiver layout

            // Bind views within the sender layout
            if (holder.senderTimeText != null) {
                holder.senderTimeText.setText(messageTime); // Use time string from Entity
            }

            // Handle Content (Text or Image) within sender layout
            if ("text".equals(messageType)) {
                if (holder.senderMessageText != null) {
                    holder.senderMessageText.setVisibility(View.VISIBLE); // Show sender text view
                    holder.senderMessageText.setText(messageContent); // Use content from Entity
                }
                if (holder.senderImageView != null) holder.senderImageView.setVisibility(View.GONE); // Hide sender image view

                // Hide drawing link
                if (holder.senderDrawingLinkText != null) holder.senderDrawingLinkText.setVisibility(View.GONE); // <<< Hide drawing link

            } else if ("image".equals(messageType)) {
                if (holder.senderImageView != null) {
                    holder.senderImageView.setVisibility(View.VISIBLE); // Show sender image view
                    // Decode and set image
                    if (!TextUtils.isEmpty(messageContent)) { // messageContent is Base64 from Entity
                        try {
                            // Use android.util.Base64 for decoding
                            byte[] decodedString = Base64.decode(messageContent, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            if (decodedBitmap != null) {
                                holder.senderImageView.setImageBitmap(decodedBitmap);
                            } else {
                                holder.senderImageView.setImageResource(R.drawable.error_image);
                            } // Error placeholder
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Invalid Base64 for sender img msg " + messageId, e);
                            holder.senderImageView.setImageResource(R.drawable.error_image);
                        } catch (Exception e) {
                            Log.e(TAG, "Unexpected error decoding sender img msg " + messageId, e);
                            holder.senderImageView.setImageResource(R.drawable.error_image);
                        }
                    } else {
                        holder.senderImageView.setImageResource(R.drawable.default_group_img);
                    } // Default placeholder for empty data
                    // Add click listener to sender's image view if needed (e.g., view full image)
                    // if (holder.senderImageView != null) holder.senderImageView.setOnClickListener(...)


                    holder.senderImageView.setOnClickListener(v -> {
                        Log.d(TAG, "Sender image clicked for message ID: " + messageId);
                        String imageUrl = message.getMessageContent(); // Get the Base64 string from the message entity
                        if (!TextUtils.isEmpty(imageUrl)) {
                            Intent viewerIntent = new Intent(context, ChatImgFullScreenViewer.class);
                            viewerIntent.putExtra("imageUrl", imageUrl); // Pass the Base64 string
                            context.startActivity(viewerIntent);
                        } else {
                            Toast.makeText(context, "Image data unavailable.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Sender image clicked but content is empty for message ID: " + messageId);
                        }
                    });
                    // --- END NEW ---


                }
                if (holder.senderMessageText != null)
                    holder.senderMessageText.setVisibility(View.GONE); // Hide sender text view

                // Hide drawing link
                if (holder.senderDrawingLinkText != null)
                    holder.senderDrawingLinkText.setVisibility(View.GONE); // <<< Hide drawing link

            }else if ("drawing_session".equals(messageType)) { // <<< NEW: Handle Drawing Session message
                // Hide text and image views in sender layout
                if (holder.senderMessageText != null) holder.senderMessageText.setVisibility(View.GONE);
                if (holder.senderImageView != null) holder.senderImageView.setVisibility(View.GONE);

                // Show the sender's drawing link TextView
                if (holder.senderDrawingLinkText != null) {
                    holder.senderDrawingLinkText.setVisibility(View.VISIBLE);
                    // Set the text for the link (messageContent has the "User started..." string)
                    holder.senderDrawingLinkText.setText(messageContent);
                    // Add a click listener to open the Drawing Activity
                    holder.senderDrawingLinkText.setOnClickListener(v -> {
                        Log.d(TAG, "Sender Drawing link clicked for session ID: " + drawingSessionId);
                        // Check if the session ID is valid before launching the activity
                        if (!TextUtils.isEmpty(drawingSessionId)) {
                            Intent drawingIntent = new Intent(context, YOUR_DRAWING_ACTIVITY_CLASS.class); // <<< Use your actual Drawing Activity Class Name
                            drawingIntent.putExtra("groupId", message.getGroupId()); // Pass group ID from Entity
                            drawingIntent.putExtra("sessionId", drawingSessionId); // Pass the session ID from Entity
                            context.startActivity(drawingIntent);
                        } else {
                            // Handle case where session ID is missing (shouldn't happen if message was created correctly)
                            Toast.makeText(context, "Drawing session link broken.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Drawing session link clicked but session ID is empty for message ID: " + message.getMessageId());
                        }
                    });
                }


            } else {
                // Handle other message types or unknown type - display content as text placeholder in sender layout
                if (holder.senderMessageText != null) {
                    holder.senderMessageText.setVisibility(View.VISIBLE);
                    holder.senderMessageText.setText("[Unsupported Type: " + messageType + "]");
                }
                if (holder.senderImageView != null) holder.senderImageView.setVisibility(View.GONE);

                if (holder.senderDrawingLinkText != null) holder.senderDrawingLinkText.setVisibility(View.GONE); // <<< Hide drawing link


            }

            // Binding read status for sender (using message.getReadBy() from Entity)
            if (holder.senderReadStatus != null && message.getReadBy() != null) { // Check if sender status view exists and readBy map is not null
                Map<String, Boolean> readBy = message.getReadBy();
                int membersReadCount = 0;
                // Count how many *other* members have read it
                for (Map.Entry<String, Boolean> entry : readBy.entrySet()) {
                    if (Boolean.TRUE.equals(entry.getValue()) && currentUserId != null && !entry.getKey().equals(currentUserId)) { // Use adapter's currentUserId
                        membersReadCount++;
                    }
                }

                if (membersReadCount > 0) {
                    // Example: show count, or a single checkmark if only one other person read
                    holder.senderReadStatus.setText("Read by " + membersReadCount); // Show count of others
                    holder.senderReadStatus.setVisibility(View.VISIBLE); // Make status view visible
                } else {
                    holder.senderReadStatus.setVisibility(View.GONE); // Hide status view if not read by others
                }
            } else if (holder.senderReadStatus != null) {
                holder.senderReadStatus.setVisibility(View.GONE); // Hide sender status view if nulls
            }


        } else {
            // Message sent by another user (Display using receiver_layout)
            holder.senderLayout.setVisibility(View.GONE); // Hide sender layout
            holder.receiverLayout.setVisibility(View.VISIBLE); // Show receiver layout

            // Bind views within the receiver layout
            if (holder.receiverNameText != null) {
                holder.receiverNameText.setText(senderName != null ? senderName : "Unknown User"); // Use name from Entity
            }
            if (holder.receiverTimeText != null) {
                holder.receiverTimeText.setText(messageTime); // Use time string from Entity
            }

            // Binding Receiver Profile Image (INEFFICIENT but Keeping for "asis")
            // This fetches image data for every received message from Firebase.
            // Ideally, fetch user profile images once per unique user and cache them (e.g., in Room or memory).
            // If using separate layouts, this is likely only done in the received message layout's ViewHolder.
            if (holder.receiverProfileImage != null && senderId != null && !senderId.equals(currentUserId) && usersRef != null) { // Check if view exists, senderId is valid and not current user, and usersRef is valid
                holder.receiverProfileImage.setVisibility(View.VISIBLE); // Make visible before trying to load
                // Use Glide with Firebase integration or manual Base64 decoding + caching
                // Using the static usersRef member to fetch the profile image:
                usersRef.child(senderId).child("profileImage").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // This callback runs on the main thread
                        if (holder.receiverProfileImage == null) return; // Ensure view still exists (recycling)

                        if (snapshot.exists()) {
                            String base64Image = snapshot.getValue(String.class);
                            if (base64Image != null && !base64Image.isEmpty()) {
                                try {
                                    // Use android.util.Base64 for decoding
                                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    if (bitmap != null) {
                                        holder.receiverProfileImage.setImageBitmap(bitmap);
                                    } else {
                                        holder.receiverProfileImage.setImageResource(R.drawable.default_profile_img); // Default on decoding error
                                    }
                                } catch (IllegalArgumentException e) {
                                    Log.e(TAG, "Invalid Base64 string for receiver profile in message " + messageId, e);
                                    holder.receiverProfileImage.setImageResource(R.drawable.default_profile_img); // Default on error
                                } catch (Exception e) {
                                    Log.e(TAG, "Unexpected error decoding receiver profile image in message " + messageId, e);
                                    holder.receiverProfileImage.setImageResource(R.drawable.default_profile_img); // Default on error
                                }
                            } else {
                                holder.receiverProfileImage.setImageResource(R.drawable.default_profile_img); // Default if data is empty
                            }
                        } else {
                            holder.receiverProfileImage.setImageResource(R.drawable.default_profile_img); // Default if data not found
                        }
                        // Ensure view stays visible after load attempt
                        // holder.receiverProfileImage.setVisibility(View.VISIBLE); // Already set above
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch receiver profile image for message " + messageId + ": " + error.getMessage());
                        if (holder.receiverProfileImage != null) {
                            holder.receiverProfileImage.setImageResource(R.drawable.default_profile_img); // Default on error
                            holder.receiverProfileImage.setVisibility(View.VISIBLE); // Make visible
                        }
                    }
                });
            } else if (holder.receiverProfileImage != null) {
                // If senderId is null or message is from current user (shouldn't be in receiver layout), set default image and hide if needed
                holder.receiverProfileImage.setImageResource(R.drawable.default_profile_img);
                // In a correctly designed UI with visibility control, this case might not need explicit handling here.
                // If this is for a received message (senderId != currentUserID), the view should be visible.
                // If senderId was null or empty, log a warning earlier.
                holder.receiverProfileImage.setVisibility(View.VISIBLE); // Default to visible for receiver layout if check passed
            }


            // Handle Content (Text vs Image) within receiver layout
            if ("text".equals(messageType)) {
                if (holder.receiverMessageText != null) {
                    holder.receiverMessageText.setVisibility(View.VISIBLE); // Show receiver text view
                    holder.receiverMessageText.setText(messageContent); // Use content from Entity
                }
                if (holder.receiverImageView != null) holder.receiverImageView.setVisibility(View.GONE); // Hide receiver image view

                // Hide drawing link
                if (holder.receiverDrawingLinkText != null) holder.receiverDrawingLinkText.setVisibility(View.GONE); // <<< Hide drawing link


            } else if ("image".equals(messageType)) {
                if (holder.receiverImageView != null) {
                    holder.receiverImageView.setVisibility(View.VISIBLE); // Show receiver image view
                    // Decode and set image
                    if (!TextUtils.isEmpty(messageContent)) { // messageContent is Base64 from Entity
                        try {
                            Bitmap decodedBitmap = decodeBase64ToBitmap(messageContent); // Use helper method to decode Base64
                            if (decodedBitmap != null) { holder.receiverImageView.setImageBitmap(decodedBitmap); }
                            else { holder.receiverImageView.setImageResource(R.drawable.error_image); } // Error placeholder
                        } catch (IllegalArgumentException e) { Log.e(TAG, "Invalid Base64 for receiver img msg " + messageId, e); holder.receiverImageView.setImageResource(R.drawable.error_image); }
                        catch (Exception e) { Log.e(TAG, "Unexpected error decoding receiver img msg " + messageId, e); holder.receiverImageView.setImageResource(R.drawable.error_image); }
                    } else {
                        // Placeholder for empty image data
                        holder.receiverImageView.setImageResource(R.drawable.default_group_img);
                    }




                    // --- NEW: Add click listener for receiver's image view ---
                    holder.receiverImageView.setOnClickListener(v -> {
                        Log.d(TAG, "Receiver image clicked for message ID: " + messageId);
                        String imageUrl = message.getMessageContent(); // Get the Base64 string from the message entity
                        if (!TextUtils.isEmpty(imageUrl)) {
                            Intent viewerIntent = new Intent(context, ChatImgFullScreenViewer.class);
                            viewerIntent.putExtra("imageUrl", imageUrl); // Pass the Base64 string
                            context.startActivity(viewerIntent);
                        } else {
                            Toast.makeText(context, "Image data unavailable.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Receiver image clicked but content is empty for message ID: " + messageId);
                        }
                    });
                    // --- END NEW ---

                    // Add click listener to receiver's image view if needed (e.g., view full image)
                    // if (holder.receiverImageView != null) holder.receiverImageView.setOnClickListener(...)
                }
                if (holder.receiverMessageText != null) holder.receiverMessageText.setVisibility(View.GONE); // Hide receiver text view
                // Hide drawing link
                if (holder.receiverDrawingLinkText != null) holder.receiverDrawingLinkText.setVisibility(View.GONE); // <<< Hide drawing link
            } else if ("drawing_session".equals(messageType)) { // <<< NEW: Handle Drawing Session message
                // Hide text and image views in receiver layout
                if (holder.receiverMessageText != null) holder.receiverMessageText.setVisibility(View.GONE);
                if (holder.receiverImageView != null) holder.receiverImageView.setVisibility(View.GONE);
                // Hide receiver name if the link bubble already includes name? Or keep name?
                // Based on your layout, name TextView is separate, keep it visible.

                // Show the receiver's drawing link TextView
                if (holder.receiverDrawingLinkText != null) {
                    holder.receiverDrawingLinkText.setVisibility(View.VISIBLE);
                    // Set the text for the link (messageContent has the "User started..." string)
                    holder.receiverDrawingLinkText.setText(messageContent); // Use content from Entity
                    // Add a click listener to open the Drawing Activity
                    holder.receiverDrawingLinkText.setOnClickListener(v -> {
                        Log.d(TAG, "Receiver Drawing link clicked for session ID: " + drawingSessionId);
                        // Check if the session ID is valid
                        if (!TextUtils.isEmpty(drawingSessionId)) {
                            Intent drawingIntent = new Intent(context, YOUR_DRAWING_ACTIVITY_CLASS.class); // <<< Use your actual Drawing Activity Class Name
                            drawingIntent.putExtra("groupId", message.getGroupId()); // Pass group ID from Entity
                            drawingIntent.putExtra("sessionId", drawingSessionId); // Pass the session ID from Entity
                            context.startActivity(drawingIntent);
                        } else {
                            Toast.makeText(context, "Drawing session link broken.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Drawing session link clicked but session ID is empty for message ID: " + message.getMessageId());
                        }
                    });
                }




            } else {
                // Handle other message types or unknown type - display content as text placeholder in receiver layout
                if (holder.receiverMessageText != null) {
                    holder.receiverMessageText.setVisibility(View.VISIBLE);
                    holder.receiverMessageText.setText("[Unsupported Type: " + messageType + "]");






                }
                if (holder.receiverImageView != null) holder.receiverImageView.setVisibility(View.GONE);
                if (holder.receiverDrawingLinkText != null) holder.receiverDrawingLinkText.setVisibility(View.GONE); // <<< Hide drawing link
            }

            // Binding read status for receiver (your read status on messages sent by others)
            // Usually, you don't show *your* read status on incoming messages in the bubble UI itself.
            // If you *do* want to show something like a checkmark *after* this user has read it,
            // you would check message.isReadBy(currentUserID) here and show receiverReadStatus.
//            if (holder.receiverReadStatus != null) { // Check if receiver status view exists
////                // Check if the message entity indicates the current user has read this message
//////                if (currentUserID != null && message.isReadBy(currentUserID)) { // Use the helper method in GroupMessageEntity and adapter's currentUserID
//////                    // holder.receiverReadStatus.setVisibility(View.VISIBLE); // Make visible
////                    // holder.receiverReadStatus.setText("Read"); // Or set a checkmark icon
////                } else {
////                    // holder.receiverReadStatus.setVisibility(View.GONE); // Hide if not read
////                }
////                holder.receiverReadStatus.setVisibility(View.GONE); // Hiding by default as it's less common display
////            }


        } // End of if/else for sender vs receiver


        // --- Handle Item Click (Optional in Group Chat) ---
        // If you want the whole message bubble clickable (e.g., for copying text, viewing image)
        // holder.itemView.setOnClickListener(v -> {
        //     // Access message data: message.getMessageContent(), message.getMessageId(), etc.
        //     // You might need a listener interface to communicate clicks back to the activity.
        // });

        // --- Handle Long Click (Optional) ---
        // For deleting messages etc. (Requires implementing OnMessageLongClickListener in Activity and passing to adapter)
        // holder.itemView.setOnLongClickListener(v -> {
        //    // Handle long click, e.g., show context menu for delete/copy
        //    // You'll need a listener interface to communicate long clicks back to the activity.
        //    // You will need the messageId (message.getMessageId()) for deletion or other actions.
        //    // Access message data: message.getMessageId(), message.getSenderId(), etc.
        //    // Example: if (currentUserID != null && message.getSenderId().equals(currentUserID)) showDeleteOption(message.getMessageId());
        //    return false; // Don't consume by default if not handled
        // });
    }








            // Helper method to decode Base64 string to Bitmap (Keep This Helper Method)
    private Bitmap decodeBase64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        // Use android.util.Base64 consistently
        try {
            byte[] decodedBytes = Base64.decode(base64String, android.util.Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid Base64 string for decoding.", e);
            return null; // Return null on error
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error decoding Base64 string", e);
            return null; // Catch any other exceptions
        }
    }


    @Override
    public int getItemCount() {
        // Return the total number of items in the data list held by the adapter
        return groupMessagesList.size();
    }

    // --- Optional: Add methods for specific updates using DiffUtil if needed later ---
    // If you decide to use ListAdapter for better performance and animations:
    // You would extend ListAdapter<GroupMessageEntity, GroupMessageViewHolder> instead of RecyclerView.Adapter
    // and implement DiffUtil.ItemCallback<GroupMessageEntity>.
    // Then you would call submitList(newList) from the LiveData observer in the Activity.

}


