package com.sana.circleup;//package com.sana.circleup;
//
//
//


import android.annotation.SuppressLint; // Import this for suppressing warnings
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
import android.widget.Toast; // Import Toast

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil; // Import DiffUtil
import androidx.recyclerview.widget.ListAdapter; // Import ListAdapter
import androidx.recyclerview.widget.RecyclerView;

import com.sana.circleup.temporary_chat_room.TemporaryIChatListItemGroup;
import com.sana.circleup.temporary_chat_room.TemporaryRoom;

import java.util.List; // Import List
import java.util.Locale; // Import Locale

import de.hdodenhof.circleimageview.CircleImageView; // Assuming you use this for group images

// Change to extend ListAdapter
//public class GroupListAdapter extends ListAdapter<TemporaryIChatListItemGroup, RecyclerView.ViewHolder> { // Specify the list item type and ViewHolder type
//
//    private Context context;
//    // ListAdapter manages the list internally, so you don't need a separate list field here
//    // private List<TemporaryIChatListItemGroup> chatItemList;
//
//    // Define view types
//    private static final int VIEW_TYPE_GROUP = 1;
//    private static final int VIEW_TYPE_TEMPORARY_ROOM = 2;
//
//    // --- NEW: Click Listener Interface ---
//    private OnItemClickListener listener;
//
//    // Define the interface
//    public interface OnItemClickListener {
//        void onItemClick(TemporaryIChatListItemGroup item);
//    }
//
//    // Method to set the listener from the Fragment
//    public void setOnItemClickListener(OnItemClickListener listener) {
//        this.listener = listener;
//    }
//    // --- End Click Listener Interface ---
//
//
//    // --- NEW: DiffUtil.ItemCallback implementation ---
//    // This is static because it doesn't need access to instance members
//    private static final DiffUtil.ItemCallback<TemporaryIChatListItemGroup> DIFF_CALLBACK = new DiffUtil.ItemCallback<TemporaryIChatListItemGroup>() {
//        @Override
//        public boolean areItemsTheSame(@NonNull TemporaryIChatListItemGroup oldItem, @NonNull TemporaryIChatListItemGroup newItem) {
//            // Items are the same if they represent the same underlying object (same ID)
//            return oldItem.getId().equals(newItem.getId());
//        }
//
//        // Add this annotation to suppress the Lint warning about `equals()`
//        @Override
//        @SuppressLint({"SuspiciousEqualityCheck", "DiffUtilEquals"})
//        public boolean areContentsTheSame(@NonNull TemporaryIChatListItemGroup oldItem, @NonNull TemporaryIChatListItemGroup newItem) {
//            // Contents are the same if all relevant fields are equal
//            // This tells ListAdapter whether the item *changed* and needs to be rebound
//
//            // First, check if they are of the same type
//
//            if(oldItem.getClass() != newItem.getClass()) {
//                return false;
//            }
//
//            // Now compare based on the specific type
//            if (oldItem instanceof Group && newItem instanceof Group) {
//                Group oldGroup = (Group) oldItem;
//                Group newGroup = (Group) newItem;
//                // Compare fields that affect the list item's appearance or sorting
//                // Use .equals() for String and object comparisons, == for primitives (long)
//                return oldGroup.getName().equals(newGroup.getName()) &&
//                        oldGroup.getImageUrl().equals(newGroup.getImageUrl()) &&
//                        oldGroup.getLastMessagePreview().equals(newGroup.getLastMessagePreview()) && // Compare preview text
//                        oldGroup.getSortingTimestamp() == newGroup.getSortingTimestamp() && // Compare timestamp for sorting
//                        oldGroup.hasUnreadMessages() == newGroup.hasUnreadMessages(); // Compare unread status
//                // Add other fields if their changes should trigger a UI update (e.g., groupStatus if displayed)
//
//            } else if (oldItem instanceof TemporaryRoom && newItem instanceof TemporaryRoom) {
//                TemporaryRoom oldRoom = (TemporaryRoom) oldItem;
//                TemporaryRoom newRoom = (TemporaryRoom) newItem;
//                // Compare fields relevant to TemporaryRoom display
//                return oldRoom.getName().equals(newRoom.getName()) &&
//                        oldRoom.getLastMessagePreview().equals(newRoom.getLastMessagePreview()) &&
//                        oldRoom.getSortingTimestamp() == newRoom.getSortingTimestamp() &&
//                        oldRoom.hasUnreadMessages() == newRoom.hasUnreadMessages() &&
//                        // Compare expiry time (handle nulls)
//                        ((oldRoom.getExpiryTime() == null && newRoom.getExpiryTime() == null) ||
//                                (oldRoom.getExpiryTime() != null && oldRoom.getExpiryTime().equals(newRoom.getExpiryTime()))); // Compare expiry time if both are non-null
//
//            }
//            // Should theoretically not reach here if all item types are handled
//            return false;
//        }
//    };
//
//    // --- Constructor (Pass the DiffUtil.ItemCallback to the super ListAdapter constructor) ---
//    public GroupListAdapter(Context context, List<TemporaryIChatListItemGroup> initialList) {
//        super(DIFF_CALLBACK); // Call the super constructor with the DiffUtil callback
//        this.context = context;
//        // Submit the initial list data using ListAdapter's submitList method
//        submitList(initialList);
//    }
//
//
//    // --- Override getItemViewType ---
//    @Override
//    public int getItemViewType(int position) {
//        // Get the item at the specified position using ListAdapter's getItem()
//        TemporaryIChatListItemGroup item = getItem(position);
//        if (item instanceof Group) {
//            return VIEW_TYPE_GROUP;
//        } else if (item instanceof TemporaryRoom) {
//            return VIEW_TYPE_TEMPORARY_ROOM;
//        }
//        // Return a default or throw an exception if an unexpected type is encountered
//        Log.e("GroupListAdapter", "Unknown item type at position: " + position);
//        return -1; // Indicate an error or default type
//    }
//
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        LayoutInflater inflater = LayoutInflater.from(context);
//        if (viewType == VIEW_TYPE_GROUP) {
//            // Inflate the layout for a regular group item
//            View view = inflater.inflate(R.layout.group_list_item, parent, false); // Replace with your actual group list item layout ID
//            return new GroupViewHolder(view);
//        } else if (viewType == VIEW_TYPE_TEMPORARY_ROOM) {
//            // Inflate the layout for a temporary room item
//            View view = inflater.inflate(R.layout.temporary_room_list_item, parent, false); // Replace with your actual temporary room list item layout ID
//            return new TemporaryRoomViewHolder(view);
//        }
//        // Return a dummy ViewHolder or null for the error case, or handle differently
//        View errorView = new View(context); // Simple empty view
//        return new RecyclerView.ViewHolder(errorView) {}; // Return a placeholder ViewHolder
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        // Get the item at the specified position using ListAdapter's getItem()
//        TemporaryIChatListItemGroup item = getItem(position);
//
//        // Bind data based on the view holder type (which corresponds to the item type)
//        if (holder.getItemViewType() == VIEW_TYPE_GROUP) {
//            GroupViewHolder groupHolder = (GroupViewHolder) holder;
//            Group group = (Group) item; // Cast the item to the specific model type
//
//            // Bind Group specific data
//            groupHolder.groupName.setText(group.getName());
//            groupHolder.lastMessagePreview.setText(group.getLastMessagePreview()); // Use the preview text from the interface
//            // groupHolder.groupStatus.setText(group.getGroupStatus()); // Use this if you display status
//
//            // Set profile image (decoding Base64)
//            String base64String = group.getImageUrl(); // Get the image URL/Base64 from the interface
//            if (base64String != null && !base64String.isEmpty()) {
//                try {
//                    byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
//                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                    groupHolder.groupImage.setImageBitmap(decodedBitmap);
//                } catch (IllegalArgumentException e) {
//                    Log.e("GroupListAdapter", "Error decoding Base64 image: " + e.getMessage());
//                    groupHolder.groupImage.setImageResource(R.drawable.default_group_img); // Default image on decoding error
//                }
//            } else {
//                groupHolder.groupImage.setImageResource(R.drawable.default_group_img); // Default image if no image data
//            }
//
//            // Handle Unread Indicator (Assuming you have a TextView with id @+id/text_unread_count)
//            if (group.hasUnreadMessages()) {
//                groupHolder.unreadCountText.setVisibility(View.VISIBLE);
//                // Optionally show the actual unread count if your model/entity includes it
//                // groupHolder.unreadCountText.setText(String.valueOf(group.getUnreadCount())); // If you store count
//                groupHolder.unreadCountText.setText("New"); // Simple indicator
//                // Set background drawable if you have one (like a badge)
//                groupHolder.unreadCountText.setBackgroundResource(R.drawable.badge_background); // Replace with your badge drawable
//            } else {
//                groupHolder.unreadCountText.setVisibility(View.GONE);
//            }
//
//
//            // --- Set Click Listener for the item view ---
//            holder.itemView.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onItemClick(item); // Pass the clicked item to the listener
//                }
//            });
//
//
//        } else if (holder.getItemViewType() == VIEW_TYPE_TEMPORARY_ROOM) {
//            TemporaryRoomViewHolder tempRoomHolder = (TemporaryRoomViewHolder) holder;
//            TemporaryRoom tempRoom = (TemporaryRoom) item; // Cast to TemporaryRoom
//
//            // Bind Temporary Room specific data
//            tempRoomHolder.tempRoomName.setText(tempRoom.getName());
//            tempRoomHolder.lastMessagePreview.setText(tempRoom.getLastMessagePreview()); // Use preview text
//
//            // Display info like expiry time or creation time
//            if (tempRoom.isExpired()) { // Use the helper method in your model
//                tempRoomHolder.tempRoomInfo.setText("Expired");
//                tempRoomHolder.tempRoomInfo.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark)); // Use a red color for expired
//            } else {
//                Long expiryTime = tempRoom.getExpiryTime();
//                if(expiryTime != null) {
//                    long remainingTime = expiryTime - System.currentTimeMillis();
//                    String timeRemainingFormatted = formatRemainingTime(remainingTime); // Use the helper method
//                    tempRoomHolder.tempRoomInfo.setText("Expires in: " + timeRemainingFormatted);
//                    tempRoomHolder.tempRoomInfo.setTextColor(context.getResources().getColor(android.R.color.darker_gray)); // Normal color
//                } else {
//                    // Handle case where expiry time is null (e.g., show creation time or nothing)
//                    tempRoomHolder.tempRoomInfo.setText("No expiry set"); // Or format tempRoom.getCreationTimestamp()
//                    tempRoomHolder.tempRoomInfo.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
//                }
//            }
//
//
//            // Set temporary room icon
//            tempRoomHolder.tempRoomIcon.setImageResource(R.drawable.temporary_chatroom); // Replace with your actual drawable name
//
//            // Handle Unread Indicator (Assuming you have a TextView with id @+id/text_unread_count)
//            if (tempRoom.hasUnreadMessages()) {
//                tempRoomHolder.unreadCountText.setVisibility(View.VISIBLE);
//                // Optionally show the actual unread count if your model/entity includes it
//                // tempRoomHolder.unreadCountText.setText(String.valueOf(tempRoom.getUnreadCount())); // If you store count
//                tempRoomHolder.unreadCountText.setText("New"); // Simple indicator
//                // Set background drawable if you have one (like a badge)
//                tempRoomHolder.unreadCountText.setBackgroundResource(R.drawable.badge_background); // Replace with your badge drawable
//            } else {
//                tempRoomHolder.unreadCountText.setVisibility(View.GONE);
//            }
//
//            // --- Set Click Listener for the item view ---
//            holder.itemView.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onItemClick(item); // Pass the clicked item to the listener
//                }
//            });
//        }
//        // No binding needed for the error view holder if you added one
//    }
//
//    // ListAdapter handles getItemCount() automatically, no need to override
//
//    // Helper method to format remaining time (You need to implement this based on your preference)
//    // It takes milliseconds and returns a formatted string like "5d 3h" or "1h 30m".
//    private String formatRemainingTime(long millis) {
//        if (millis <= 0) return "0s";
//
//        long totalSeconds = millis / 1000;
//        long days = totalSeconds / (60 * 60 * 24);
//        long hours = (totalSeconds % (60 * 60 * 24)) / (60 * 60);
//        long minutes = (totalSeconds % (60 * 60)) / 60;
//        long seconds = totalSeconds % 60;
//
//        StringBuilder sb = new StringBuilder();
//        if (days > 0) {
//            sb.append(days).append("d ");
//        }
//        if (hours > 0) {
//            sb.append(hours).append("h ");
//        }
//        if (minutes > 0 && days == 0) { // Only show minutes if no days are shown or minutes > 0
//            sb.append(minutes).append("m ");
//        } else if (minutes > 0 && days > 0) { // Always show minutes if days exist
//            sb.append(minutes).append("m ");
//        }
//        if (sb.length() == 0 || seconds > 0 && days == 0 && hours == 0 && minutes == 0) {
//            // Show seconds only if the duration is less than a minute, or if it's the only component left
//            sb.append(seconds).append("s");
//        }
//
//
//        return sb.toString().trim(); // Trim any trailing space
//    }
//
//
//    // --- Define your ViewHolders ---
//
//    // ViewHolder for a regular group item
//    public static class GroupViewHolder extends RecyclerView.ViewHolder {
//        TextView groupName;
//        TextView lastMessagePreview; // TextView for the last message preview
//        CircleImageView groupImage; // CircleImageView for the group's profile image
//        TextView unreadCountText; // TextView for unread count or indicator
//
//        public GroupViewHolder(@NonNull View itemView) {
//            super(itemView);
//            // Link variables to the actual views in your layout file (group_list_item.xml)
//            groupName = itemView.findViewById(R.id.group_name); // Match this ID
//            lastMessagePreview = itemView.findViewById(R.id.text_last_message); // Match this ID
//            groupImage = itemView.findViewById(R.id.group_profile_image); // Match this ID
//            unreadCountText = itemView.findViewById(R.id.text_unread_countt); // Match this ID
//        }
//    }
//
//    // ViewHolder for a temporary room item
//    public static class TemporaryRoomViewHolder extends RecyclerView.ViewHolder {
//        TextView tempRoomName; // TextView for the temporary room's name
//        TextView tempRoomInfo; // TextView for info like expiry time or creation time
//        TextView lastMessagePreview; // TextView for the last message preview
//        ImageView tempRoomIcon; // ImageView for the temporary room's icon
//        TextView unreadCountText; // TextView for unread count or indicator
//
//
//        public TemporaryRoomViewHolder(@NonNull View itemView) {
//            super(itemView);
//            // Link variables to the actual views in your layout file (temporary_room_list_item.xml)
//            // Make sure these IDs match your layout file!
//            tempRoomName = itemView.findViewById(R.id.temp_room_name);
//            tempRoomInfo = itemView.findViewById(R.id.temp_room_info);
//            lastMessagePreview = itemView.findViewById(R.id.text_last_message); // Can reuse this ID if consistent
//            tempRoomIcon = itemView.findViewById(R.id.temp_room_icon);
//            unreadCountText = itemView.findViewById(R.id.text_unread_count); // Match this ID
//        }
//    }
//
//    // --- Reminder: Create these layout files if they don't exist ---
//    // res/layout/group_list_item.xml
//    // res/layout/temporary_room_list_item.xml
//    // And the drawable res/drawable/badge_background.xml
//}




import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Consider removing Toast from Adapter, handled better in Fragment
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Assuming you have CircleImageView from a library like 'de.hdodenhof:circleimageview:3.1.0'
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupListAdapter extends ListAdapter<TemporaryIChatListItemGroup, RecyclerView.ViewHolder> {

    private Context context;

    // Define view types
    private static final int VIEW_TYPE_GROUP = 1;
    private static final int VIEW_TYPE_TEMPORARY_ROOM = 2;

    // --- Click Listener Interface ---
    private OnItemClickListener clickListener;
    public interface OnItemClickListener {
        void onItemClick(TemporaryIChatListItemGroup item);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }
    // --- End Click Listener Interface ---

    // --- NEW: Long Click Listener Interface ---
    private OnItemLongClickListener longClickListener;
    public interface OnItemLongClickListener {
        void onItemLongClick(TemporaryIChatListItemGroup item);
    }
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
    // --- End Long Click Listener Interface ---


    // --- DiffUtil.ItemCallback implementation ---
    private static final DiffUtil.ItemCallback<TemporaryIChatListItemGroup> DIFF_CALLBACK = new DiffUtil.ItemCallback<TemporaryIChatListItemGroup>() {
        @Override
        public boolean areItemsTheSame(@NonNull TemporaryIChatListItemGroup oldItem, @NonNull TemporaryIChatListItemGroup newItem) {
            // Items are the same if they represent the same underlying object (same ID)
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        @SuppressLint({"SuspiciousEqualityCheck", "DiffUtilEquals"})
        public boolean areContentsTheSame(@NonNull TemporaryIChatListItemGroup oldItem, @NonNull TemporaryIChatListItemGroup newItem) {
            // Contents are the same if all relevant fields are equal
            if(oldItem.getClass() != newItem.getClass()) {
                return false;
            }

            if (oldItem instanceof Group && newItem instanceof Group) {
                Group oldGroup = (Group) oldItem;
                Group newGroup = (Group) newItem;
                return oldGroup.getName().equals(newGroup.getName()) &&
                        oldGroup.getImageUrl().equals(newGroup.getImageUrl()) &&
                        oldGroup.getLastMessagePreview().equals(newGroup.getLastMessagePreview()) &&
                        oldGroup.getSortingTimestamp() == newGroup.getSortingTimestamp() &&
                        oldGroup.hasUnreadMessages() == newGroup.hasUnreadMessages();
            } else if (oldItem instanceof TemporaryRoom && newItem instanceof TemporaryRoom) {
                TemporaryRoom oldRoom = (TemporaryRoom) oldItem;
                TemporaryRoom newRoom = (TemporaryRoom) newItem;
                return oldRoom.getName().equals(newRoom.getName()) &&
                        oldRoom.getLastMessagePreview().equals(newRoom.getLastMessagePreview()) &&
                        oldRoom.getSortingTimestamp() == newRoom.getSortingTimestamp() &&
                        oldRoom.hasUnreadMessages() == newRoom.hasUnreadMessages() &&
                        ((oldRoom.getExpiryTime() == null && newRoom.getExpiryTime() == null) ||
                                (oldRoom.getExpiryTime() != null && oldRoom.getExpiryTime().equals(newRoom.getExpiryTime())));
            }
            return false;
        }
    };

    // --- Constructor ---
    public GroupListAdapter(Context context, List<TemporaryIChatListItemGroup> initialList) {
        super(DIFF_CALLBACK);
        this.context = context;
        submitList(initialList);
    }


    // --- Override getItemViewType ---
    @Override
    public int getItemViewType(int position) {
        TemporaryIChatListItemGroup item = getItem(position);
        if (item instanceof Group) {
            return VIEW_TYPE_GROUP;
        } else if (item instanceof TemporaryRoom) {
            return VIEW_TYPE_TEMPORARY_ROOM;
        }
        Log.e("GroupListAdapter", "Unknown item type at position: " + position);
        return -1;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_GROUP) {
            View view = inflater.inflate(R.layout.group_list_item, parent, false);
            return new GroupViewHolder(view);
        } else if (viewType == VIEW_TYPE_TEMPORARY_ROOM) {
            View view = inflater.inflate(R.layout.temporary_room_list_item, parent, false);
            return new TemporaryRoomViewHolder(view);
        }
        View errorView = new View(context);
        return new RecyclerView.ViewHolder(errorView) {};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TemporaryIChatListItemGroup item = getItem(position);

        // Apply common click and long-click listeners
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });

        // --- NEW: Set Long Click Listener ---
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(item);
                return true; // Consume the long click event
            }
            return false; // Did not consume the long click
        });


        if (holder.getItemViewType() == VIEW_TYPE_GROUP) {
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            Group group = (Group) item;

            groupHolder.groupName.setText(group.getName());
            groupHolder.lastMessagePreview.setText(group.getLastMessagePreview());

            String base64String = group.getImageUrl();
            if (base64String != null && !base64String.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    groupHolder.groupImage.setImageBitmap(decodedBitmap);
                } catch (IllegalArgumentException e) {
                    Log.e("GroupListAdapter", "Error decoding Base64 image: " + e.getMessage());
                    groupHolder.groupImage.setImageResource(R.drawable.default_group_img);
                }
            } else {
                groupHolder.groupImage.setImageResource(R.drawable.default_group_img);
            }

            if (group.hasUnreadMessages()) {
                groupHolder.unreadCountText.setVisibility(View.VISIBLE);
                groupHolder.unreadCountText.setText("New");
                groupHolder.unreadCountText.setBackgroundResource(R.drawable.badge_background);
            } else {
                groupHolder.unreadCountText.setVisibility(View.GONE);
            }


        } else if (holder.getItemViewType() == VIEW_TYPE_TEMPORARY_ROOM) {
            TemporaryRoomViewHolder tempRoomHolder = (TemporaryRoomViewHolder) holder;
            TemporaryRoom tempRoom = (TemporaryRoom) item;

            tempRoomHolder.tempRoomName.setText(tempRoom.getName());
            tempRoomHolder.lastMessagePreview.setText(tempRoom.getLastMessagePreview());

            if (tempRoom.isExpired()) {
                tempRoomHolder.tempRoomInfo.setText("Expired");
                tempRoomHolder.tempRoomInfo.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                Long expiryTime = tempRoom.getExpiryTime();
                if(expiryTime != null) {
                    long remainingTime = expiryTime - System.currentTimeMillis();
                    String timeRemainingFormatted = formatRemainingTime(remainingTime);
                    tempRoomHolder.tempRoomInfo.setText("Expires in: " + timeRemainingFormatted);
                    tempRoomHolder.tempRoomInfo.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                } else {
                    tempRoomHolder.tempRoomInfo.setText("No expiry set");
                    tempRoomHolder.tempRoomInfo.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                }
            }

            tempRoomHolder.tempRoomIcon.setImageResource(R.drawable.temporary_chatroom);

            if (tempRoom.hasUnreadMessages()) {
                tempRoomHolder.unreadCountText.setVisibility(View.VISIBLE);
                tempRoomHolder.unreadCountText.setText("New");
                tempRoomHolder.unreadCountText.setBackgroundResource(R.drawable.badge_background);
            } else {
                tempRoomHolder.unreadCountText.setVisibility(View.GONE);
            }
        }
    }

    private String formatRemainingTime(long millis) {
        if (millis <= 0) return "0s";

        long totalSeconds = millis / 1000;
        long days = totalSeconds / (60 * 60 * 24);
        long hours = (totalSeconds % (60 * 60 * 24)) / (60 * 60);
        long minutes = (totalSeconds % (60 * 60)) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        // Show minutes if there are no days and hours, or if days/hours exist and minutes > 0
        if (minutes > 0 || (sb.length() > 0 && seconds == 0)) {
            sb.append(minutes).append("m ");
        }
        if (sb.length() == 0 || (seconds > 0 && sb.length() < 3)) { // Show seconds if nothing else shown or very short duration
            sb.append(seconds).append("s");
        }


        return sb.toString().trim();
    }


    // --- Define your ViewHolders ---

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        TextView lastMessagePreview;
        CircleImageView groupImage;
        TextView unreadCountText;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
            lastMessagePreview = itemView.findViewById(R.id.text_last_message);
            groupImage = itemView.findViewById(R.id.group_profile_image);
            // Make sure this ID matches your group_list_item.xml layout
            unreadCountText = itemView.findViewById(R.id.text_unread_countt);
        }
    }

    public static class TemporaryRoomViewHolder extends RecyclerView.ViewHolder {
        TextView tempRoomName;
        TextView tempRoomInfo;
        TextView lastMessagePreview;
        ImageView tempRoomIcon;
        TextView unreadCountText; // Assuming you added this to temporary_room_list_item.xml


        public TemporaryRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tempRoomName = itemView.findViewById(R.id.temp_room_name);
            tempRoomInfo = itemView.findViewById(R.id.temp_room_info);
            lastMessagePreview = itemView.findViewById(R.id.text_last_message);
            tempRoomIcon = itemView.findViewById(R.id.temp_room_icon);
            // Make sure this ID matches your temporary_room_list_item.xml layout
            unreadCountText = itemView.findViewById(R.id.text_unread_count);
        }
    }
}