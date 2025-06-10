//package com.sana.circleup;
//
//
//
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.util.Base64;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.sana.circleup.ChatPageActivity;
//import com.sana.circleup.R;
//import com.sana.circleup.room_db_implement.ChatEntity;
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
//    private Context context;
//    private List<ChatEntity> chatList;
//    private List<ChatEntity> selectedChats = new ArrayList<>(); // Declare and initialize selectedChats list
//
//
//    public ChatAdapter(Context context, List<ChatEntity> chatList) {
//        this.context = context;
//        this.chatList = chatList;
//    }
//
//    // Method to update chat list and refresh the RecyclerView
//    public void setChats(List<ChatEntity> chats) {
//        this.chatList = chats;
//        notifyDataSetChanged();
//        Log.d("ChatAdapter", "Updated chat list with " + chats.size() + " items");
//    }
//
//    @NonNull
//    @Override
//    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.users_friends_display_layout, parent, false);
//        return new ChatViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
//        ChatEntity chat = chatList.get(position);
//
//        holder.userName.setText(chat.getUsername());
//        holder.lastMessage.setText(chat.getLastMessage());
//
//        // Decode Base64 and Set Profile Image
//        if (chat.getProfileImage() != null && !chat.getProfileImage().isEmpty()) {
//            holder.profileImage.setImageBitmap(decodeBase64Image(chat.getProfileImage()));
//        } else {
//            holder.profileImage.setImageResource(R.drawable.default_profile_img); // Use a default image if there's no profile image
//        }
//
//
//        // Check if the current chat is selected, and change the background color
//        if (selectedChats.contains(chat)) {
//            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.purple)); // Set purple background for selected items
//        } else {
//            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent)); // Default transparent background
//        }
//
//        // Set a click listener to toggle selection on item click
//        holder.itemView.setOnClickListener(v -> {
//            if (selectedChats.contains(chat)) {
//                selectedChats.remove(chat); // Deselect if already selected
//            } else {
//                selectedChats.add(chat); // Select if not already selected
//            }
//            notifyItemChanged(position); // Notify adapter to refresh this item
//        });
//
//
//        // Set a click listener to open ChatPageActivity
//        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, ChatPageActivity.class);
//            intent.putExtra("visit_users_ids", chat.getUserId()); // Pass the user ID
//            intent.putExtra("visit_users_name", chat.getUsername()); // Pass the username
//            intent.putExtra("visit_users_image", chat.getProfileImage()); // Pass the profile image in Base64 format
//            context.startActivity(intent); // Start ChatPageActivity
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return chatList.size(); // Return the size of the chat list
//    }
//
//    // ViewHolder class to bind each item of the RecyclerView
//    public static class ChatViewHolder extends RecyclerView.ViewHolder {
//        CircleImageView profileImage;
//        TextView userName, lastMessage;
//
//        public ChatViewHolder(@NonNull View itemView) {
//            super(itemView);
//            userName = itemView.findViewById(R.id.users_profile_name); // Name of the user
//            lastMessage = itemView.findViewById(R.id.users_profile_status); // Last message sent by the user
//            profileImage = itemView.findViewById(R.id.users_profile_image); // Profile image of the user
//        }
//    }
//
//    // Method to decode the Base64 string to a Bitmap image
//    private Bitmap decodeBase64Image(String base64Image) {
//        try {
//            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
//            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); // Decode Base64 to Bitmap
//        } catch (Exception e) {
//            Log.e("ChatAdapter", "Failed to decode image", e); // Log any errors during decoding
//            return null; // If decoding fails, return null
//        }
//    }
//}





package com.sana.circleup; // Aapka package

import android.content.Context;
// import android.content.Intent; // Unused
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Room/Data related imports
import com.sana.circleup.room_db_implement.ChatEntity; // Import ChatEntity

// Utility imports
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
// import java.util.Date; // Unused
import java.util.List;
import java.util.Locale;
// import java.util.concurrent.TimeUnit; // Unused

// UI related imports
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static final String TAG = "ChatAdapter"; // Logging ke liye TAG

    private Context context;
    private List<ChatEntity> chatList; // Original full list (source of truth)
    private List<ChatEntity> chatListFiltered; // List displayed after filtering
    private OnChatInteractionListener listener; // Interface for clicks/long clicks
    private String currentFilterQuery = ""; // Track current filter

    // Date formatting constants (static finals improve efficiency slightly)
    // Example: 10:30 PM
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    // Example: 23/04/25 (Shorter year for space)
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

    // Interface for callbacks to Fragment/Activity
    public interface OnChatInteractionListener {
        void onChatClick(ChatEntity chat);
        void onChatLongClick(ChatEntity chat);
    }

    public ChatAdapter(Context context, List<ChatEntity> initialChatList, OnChatInteractionListener listener, String currentUserID) {
        this.context = context;
        this.listener = listener;
        // Initialize lists safely, creating copies
        this.chatList = initialChatList != null ? new ArrayList<>(initialChatList) : new ArrayList<>();
        this.chatListFiltered = new ArrayList<>(this.chatList); // Start with the full list displayed
    }

    // Method to update the list data (Called from Fragment/Activity's observer)
    public void submitList(List<ChatEntity> newChatList) {
        // Update the source of truth
        this.chatList = newChatList != null ? new ArrayList<>(newChatList) : new ArrayList<>();
        // Re-apply the current filter to the new list
        filter(this.currentFilterQuery);
        Log.d(TAG, "Submitted new list. Original size: " + this.chatList.size());
        // Note: Consider using DiffUtil for better performance on list updates
        // notifyDataSetChanged(); // filter() already calls this
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ** MAKE SURE R.layout.users_friends_display_layout IS THE CORRECT FILE **
        View view = LayoutInflater.from(context).inflate(R.layout.users_friends_display_layout, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        // Always get item from the filtered list
        if (chatListFiltered == null || position < 0 || position >= chatListFiltered.size()) {
            Log.e(TAG, "Invalid position requested: " + position + ", Filtered list size: " + (chatListFiltered != null ? chatListFiltered.size() : "null"));
            // Clear views or set defaults to avoid showing stale data
            holder.userName.setText("Error");
            holder.lastMessageText.setText("");
            holder.timestampText.setVisibility(View.GONE);
            holder.unreadCountText.setVisibility(View.GONE);
            holder.profileImage.setImageResource(R.drawable.default_profile_img); // Use your default placeholder
            return; // Avoid further processing and potential crash
        }
        final ChatEntity chat = chatListFiltered.get(position); // Use final for use in listeners

        // 1. Set Username (with null check)
        holder.userName.setText(chat.getUsername() != null ? chat.getUsername() : "Unknown User");

//        // 2. Set Last Message (Using the 'userStatus' TextView as requested)
//        String lastMsg = chat.getLastMessage();
//        holder.lastMessageText.setText(lastMsg != null && !lastMsg.isEmpty() ? lastMsg : "No messages yet");



        String lastMsg = chat.getLastMessage();
        String messageType = chat.getMessageType(); // Make sure this exists!

        if (lastMsg == null || lastMsg.isEmpty()) {
            holder.lastMessageText.setText("No messages yet");
        } else {
            if ("image".equals(messageType)) {
                holder.lastMessageText.setText("📷 sent an image");
            } else {
                holder.lastMessageText.setText(lastMsg);
            }
        }



        // 3. Load Profile Image
        loadProfileImage(chat.getProfileImage(), holder.profileImage);

        // 4. Timestamp Logic
        long timestamp = chat.getTimestamp();
        if (timestamp > 0) {
            String formattedTime = formatTimestamp(timestamp); // Format the timestamp
            holder.timestampText.setText(formattedTime);       // Set the text
            holder.timestampText.setVisibility(View.VISIBLE);  // Make it visible
        } else {
            // If timestamp is invalid (0 or less), hide the TextView
            holder.timestampText.setVisibility(View.GONE);
            Log.w(TAG, "Invalid timestamp (<=0) for user: " + chat.getUsername());
        }

        // 5. Unread Count Logic
        int unreadCount = chat.getUnreadCount();
        if (unreadCount > 0) {
            holder.unreadCountText.setVisibility(View.VISIBLE);
            // Show actual count, limit to 99+ if desired for cleaner UI
            holder.unreadCountText.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
        } else {
            holder.unreadCountText.setVisibility(View.GONE); // Hide if count is 0
        }

        // 6. Click Listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Get the most up-to-date position in case of changes
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < chatListFiltered.size()) {
                    listener.onChatClick(chatListFiltered.get(currentPosition));
                } else {
                    Log.w(TAG, "onClick - Invalid adapter position: " + currentPosition);
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                // Get the most up-to-date position
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < chatListFiltered.size()) {
                    listener.onChatLongClick(chatListFiltered.get(currentPosition));
                    return true; // Indicate event is consumed
                } else {
                    Log.w(TAG, "onLongClick - Invalid adapter position: " + currentPosition);
                }
            }
            return false; // Indicate event is not consumed
        });




        // *** START NEW: Set OnClickListener for the Profile Image (CircleImageView) ***
        if (holder.profileImage != null) { // Ensure the view was found in the ViewHolder
            holder.profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Profile image clicked for user: " + chat.getUsername() + " (ID: " + chat.getUserId() + ")");

                    // Get the profile image Base64 string from the ChatEntity
                    String profileImageBase64 = chat.getProfileImage();

                    // Check if the image data is available
                    if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                        // Create an Intent to launch FullscreenImageActivity
                        Intent fullScreenIntent = new Intent(context, FullscreenImageActivity.class); // Use the context passed to the adapter
                        // Put the Base64 image string as an extra
                        fullScreenIntent.putExtra("profileImage", profileImageBase64);

                        // *** IMPORTANT: Tell FullscreenImageActivity to hide the Edit button ***
                        // Because the user is viewing someone else's profile picture, not their own.
                        fullScreenIntent.putExtra("hideEditButton", true);

                        // Start the activity
                        context.startActivity(fullScreenIntent); // Use the adapter's context

                    } else {
                        // If the image data is missing, show a toast message
                        Log.w(TAG, "Profile image Base64 string is null or empty for user: " + chat.getUsername() + " (ID: " + chat.getUserId() + "). Cannot open full screen viewer.");
                        Toast.makeText(context, "Profile picture not available.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Log.d(TAG, "Profile image click listener set for item at position: " + position); // Log confirmation

        } else {
            Log.w(TAG, "Profile image view is null for item at position: " + position + ". Cannot set click listener."); // Log warning if view is null
        }
        // *** END NEW: Set OnClickListener for the Profile Image ***


    }

    /**
     * Formats the timestamp according to the logic:
     * - Today: Shows time (e.g., "10:30 PM")
     * - Yesterday: Shows "Yesterday"
     * - Older: Shows date (e.g., "23/04/25")
     *
     * @param timestamp The timestamp in milliseconds since epoch.
     * @return Formatted string representation of the timestamp, or empty string if invalid.
     */
    private static String formatTimestamp(long timestamp) {
        if (timestamp <= 0) {
            return ""; // Return empty for invalid timestamp
        }

        Calendar messageTime = Calendar.getInstance();
        messageTime.setTimeInMillis(timestamp);

        Calendar now = Calendar.getInstance();

        // Check if it's Today
        if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
            return timeFormat.format(messageTime.getTime()); // Format as time "h:mm a"
        }
        // Check if it's Yesterday
        else {
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1); // Set calendar to yesterday

            if (yesterday.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                    yesterday.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
                return "Yesterday"; // Return literal string "Yesterday"
            }
            // Older than yesterday
            else {
                return dateFormat.format(messageTime.getTime()); // Format as date "dd/MM/yy"
            }
        }
    }

    @Override
    public int getItemCount() {
        // Return size of the *filtered* list
        return chatListFiltered == null ? 0 : chatListFiltered.size();
    }

    /**
     * Loads profile image from Base64 string into the CircleImageView.
     * Sets a default image on failure or if the string is empty/null.
     * Consider using Glide/Picasso library for better performance, caching, and memory management.
     */
    private void loadProfileImage(String base64Image, CircleImageView profileImageView) {
        // Set default first in case decoding fails or is slow
        profileImageView.setImageResource(R.drawable.default_profile_img); // Use your default image resource

        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                // It's slightly better to decode on a background thread if images are large
                // or if you notice UI stutters. For now, keeping it simple.
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (decodedBitmap != null) {
                    profileImageView.setImageBitmap(decodedBitmap);
                } else {
                    Log.w(TAG, "Decoded bitmap is null for non-empty Base64 string.");
                    // Already set default above
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Base64 decoding failed: " + e.getMessage());
                // Already set default above
            } catch (OutOfMemoryError oom) {
                Log.e(TAG, "OutOfMemoryError decoding Base64 image.", oom);
                // Already set default above
            }
        } else {
            // String is null or empty, default is already set
            // Log.d(TAG, "Profile image Base64 string is null or empty.");
        }
    }

    /**
     * Filters the chat list based on the query (searches username).
     * Call this from your Fragment/Activity's SearchView listener.
     */
    public void filter(String query) {
        // Normalize the query
        final String processedQuery = query != null ? query.toLowerCase().trim() : "";
        this.currentFilterQuery = processedQuery; // Store the current filter query

        chatListFiltered.clear(); // Start with an empty filtered list

        // If query is empty, show the full original list
        if (processedQuery.isEmpty()) {
            if (chatList != null) { // Ensure original list is not null
                chatListFiltered.addAll(chatList);
            }
        }
        // Otherwise, filter the original list
        else {
            if (chatList != null) { // Check if original list exists
                for (ChatEntity item : chatList) {
                    // Check if username contains the filter pattern (case-insensitive)
                    if (item.getUsername() != null && item.getUsername().toLowerCase().contains(processedQuery)) {
                        chatListFiltered.add(item);
                    }
                    // You could add more filtering criteria here (e.g., search last message)
                    // else if (item.getLastMessage() != null && item.getLastMessage().toLowerCase().contains(processedQuery)) {
                    //    chatListFiltered.add(item);
                    // }
                }
            }
        }
        Log.d(TAG, "Filtering with query '" + query + "'. Filtered list size: " + chatListFiltered.size());
        notifyDataSetChanged(); // Update the RecyclerView display
    }

    /**
     * ViewHolder Class
     * Holds references to the views within each item layout.
     */
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        // ** Ensure these IDs match EXACTLY your R.layout.users_friends_display_layout **
        CircleImageView profileImage;
        TextView userName;
        TextView lastMessageText; // Renamed for clarity (was userStatus)
        TextView unreadCountText; // Renamed for clarity
        TextView timestampText;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            // ** Verify these IDs carefully! If any are wrong, you'll get NullPointerException **
            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.users_profile_name);
            lastMessageText = itemView.findViewById(R.id.users_profile_status); // Using this for last message
            timestampText = itemView.findViewById(R.id.textViewChatTimestamp); // TextView for the timestamp
            unreadCountText = itemView.findViewById(R.id.unread_text);       // TextView for the unread count

            // --- Immediate Check for Null Views (Helps debug layout issues FAST) ---
            if (profileImage == null) Log.e(TAG, "ViewHolder Error: profileImage (R.id.users_profile_image) not found in layout!");
            if (userName == null) Log.e(TAG, "ViewHolder Error: userName (R.id.users_profile_name) not found in layout!");
            if (lastMessageText == null) Log.e(TAG, "ViewHolder Error: lastMessageText (R.id.users_profile_status) not found in layout!");
            if (timestampText == null) Log.e(TAG, "ViewHolder Error: timestampText (R.id.textViewChatTimestamp) not found in layout!");
            if (unreadCountText == null) Log.e(TAG, "ViewHolder Error: unreadCountText (R.id.unread_text) not found in layout!");
            // --- End Null Check ---
        }
    }
}



