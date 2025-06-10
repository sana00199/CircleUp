package com.sana.circleup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;


// *** Import CircleImageView ***
import de.hdodenhof.circleimageview.CircleImageView;
// *** End Import ***

//
//public class SelectContactAdapter extends RecyclerView.Adapter<SelectContactAdapter.ViewHolder> {
//    private static final String TAG = "SelectContactAdapter"; // Add TAG
//
//    private final List<UserModel> userList;
//    private final List<String> selectedUsers;
//    private final Context context;
//
//    public SelectContactAdapter(Context context, List<UserModel> userList, List<String> selectedUsers) {
//        this.context = context;
//        this.userList = userList;
//        this.selectedUsers = selectedUsers;
//        Log.d(TAG, "Adapter initialized with " + userList.size() + " users."); // Log adapter init
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_additem_contact, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        UserModel user = userList.get(position);
//        Log.d(TAG, "Binding data for position " + position + ", user: " + user.getUsername() + ", id: " + user.getUserId()); // Log binding
//
//        holder.userName.setText(user.getUsername());
//
//        // Load profile image using the helper function
//        String profileImageBase64 = user.getProfileImage();
//        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
//            try {
//                Bitmap profileBitmap = decodeBase64ToBitmap(profileImageBase64);
//                if (profileBitmap != null) {
//                    holder.profileImage.setImageBitmap(profileBitmap); // Use setImageBitmap
//                    Log.d(TAG, "Profile image decoded and set for user: " + user.getUsername());
//                } else {
//                    Log.w(TAG, "Failed to decode Base64 profile image for user: " + user.getUsername() + ". Using default.");
//                    holder.profileImage.setImageResource(R.drawable.default_profile_img);
//                }
//            } catch (Exception e) { // Catch any exception during decode/set
//                Log.e(TAG, "Error setting profile image for user: " + user.getUsername(), e);
//                holder.profileImage.setImageResource(R.drawable.default_profile_img); // Use default on error
//            }
//        } else {
//            Log.d(TAG, "ProfileImage Base64 is null or empty for user: " + user.getUsername() + ". Using default.");
//            holder.profileImage.setImageResource(R.drawable.default_profile_img);
//        }
//
//
//        // --- Checkbox logic ---
//        holder.selectCheckBox.setOnCheckedChangeListener(null); // Remove listener before setting state
//        boolean isSelected = selectedUsers.contains(user.getUserId());
//        holder.selectCheckBox.setChecked(isSelected);
//        Log.d(TAG, "Checkbox state set to " + isSelected + " for user: " + user.getUsername());
//
//
//        holder.selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            String userId = user.getUserId(); // Get user ID from the UserModel
//            if (TextUtils.isEmpty(userId)) { // Basic validation
//                Log.w(TAG, "User ID is empty for selected user " + user.getUsername() + ", cannot add/remove from selected list.");
//                buttonView.setChecked(!isChecked); // Revert checkbox state if user ID is invalid
//                return; // Stop processing for this item
//            }
//
//            if (isChecked) {
//                if (!selectedUsers.contains(userId)) {
//                    selectedUsers.add(userId);
//                    Log.d(TAG, "Selected: " + user.getUsername() + " (ID: " + userId + ")");
//                }
//            } else {
//                selectedUsers.remove(userId); // Remove by object if using ArrayList of String
//                // If selectedUsers holds UserModel objects, remove by object
//                // boolean removed = selectedUsers.remove(user); // Example if list holds objects
//                // Log.d(TAG, "Removed: " + user.getUsername() + " (ID: " + user.getUserId() + "). Removed status: " + removed);
//                Log.d(TAG, "Deselected: " + user.getUsername() + " (ID: " + userId + ")");
//            }
//            Log.d(TAG, "Current Selected Users (" + selectedUsers.size() + "): " + selectedUsers.toString()); // Log current selected list
//        });
//
//        // --- Item click listener to toggle checkbox ---
//        holder.itemView.setOnClickListener(v -> {
//            Log.d(TAG, "Item view clicked for user: " + user.getUsername());
//            // Toggle the checked state of the checkbox
//            boolean isChecked = holder.selectCheckBox.isChecked();
//            holder.selectCheckBox.setChecked(!isChecked); // This will trigger the OnCheckedChangeListener
//        });
//        // --- End Item click listener ---
//    }
//
//
//    @Override
//    public int getItemCount() {
//        return userList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView userName;
//        // *** CHANGE TYPE TO CircleImageView ***
//        CircleImageView profileImage; // Use CircleImageView type
//        // *** End Change Type ***
//
//        CheckBox selectCheckBox;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            userName = itemView.findViewById(R.id.user_name);
//            // *** CHANGE TYPE TO CircleImageView ***
//            profileImage = itemView.findViewById(R.id.user_profile_image); // Find by the same ID
//            // *** End Change Type ***
//
//            selectCheckBox = itemView.findViewById(R.id.user_checkbox);
//        }
//    }
//
//    //  Helper function to decode Base64 profile images (Remains the same)
//    private Bitmap decodeBase64ToBitmap(String base64String) {
//        try {
//            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
//            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//        } catch (IllegalArgumentException e) {
//            Log.e(TAG, "Error decoding Base64 string to Bitmap", e);
//            return null;
//        } catch (Exception e) { // Catch any other unexpected errors
//            Log.e(TAG, "Unexpected error decoding Base64 string to Bitmap", e);
//            return null;
//        }
//    }
//}


import android.widget.Filter; // Import Filter
import android.widget.Filterable; // Import Filterable

import java.util.ArrayList; // Import ArrayList

// Inside SelectContactAdapter.java

// Implement the Filterable interface
public class SelectContactAdapter extends RecyclerView.Adapter<SelectContactAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "SelectContactAdapter";

    private List<UserModel> userList; // List currently displayed (filtered)
    private List<UserModel> userListFull; // Full list of contacts (for filtering)
    // *** FIX: Initialize selectedUserIds here, don't use Collections.emptyList() directly as it's immutable ***
    // It should be initialized with the list passed from the Activity, which is mutable.
    private List<String> selectedUserIds; // List of selected user UIDs
    // *** End FIX ***

    private final Context context;

    // Listener interface and member variable
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount); // Method to notify Activity
    }
    private OnSelectionChangedListener selectionChangedListener;


    // *** MODIFIED Constructor: Fix the assignment error ***
    public SelectContactAdapter(Context context, List<UserModel> userList, List<String> selectedUsers, OnSelectionChangedListener listener) {
        this.context = context;
        this.userList = userList; // Initially, this will be the list displayed (will be updated by filter)
        // *** FIX: Create a COPY of the initial list for filtering ***
        // Do NOT assign the same list reference to userListFull if userList is expected to be filtered.
        // userListFull should always hold the complete, original data.
        this.userListFull = new ArrayList<>(userList); // Create a copy of the full list
        // *** End FIX ***


        // *** FIX: Assign the parameter 'selectedUsers' to the member variable 'selectedUserIds' ***
        this.selectedUserIds = selectedUsers; // Keep reference to the Activity's list passed here
        // *** REMOVE the incorrect line: this.selectedUsers = selectedUsers; ***
        // That was the source of the "Cannot resolve symbol 'selectedUsers'" error.
        // this.selectedUsers = selectedUsers; // <-- REMOVE THIS LINE

        this.selectionChangedListener = listener; // Assign the listener

        Log.d(TAG, "Adapter initialized with " + userList.size() + " users.");
    }
    // *** END MODIFIED Constructor ***

    // *** NEW Method to update the list from Activity (Called by loadContacts) ***
    // This method is called by CreateGroupActivity.loadContacts() when the full list is fetched.
    public void setContactList(List<UserModel> newUserList) {
        Log.d(TAG, "setContactList called with " + newUserList.size() + " users.");
        this.userListFull = new ArrayList<>(newUserList); // Update the full list with the new data
        // Clearing and adding to userList ensures the displayed list is reset
        this.userList.clear();
        this.userList.addAll(new ArrayList<>(newUserList)); // Initially display the full list

        getFilter().filter(null); // Re-apply filter (null or empty query shows full list)
        // The publishResults method of the filter will call notifyDataSetChanged()
    }
    // *** END NEW Method ***


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_additem_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position); // Use the potentially filtered userList
        // Log.d(TAG, "Binding data for position " + position + ", user: " + (user != null ? user.getUsername() : "null") + ", id: " + (user != null ? user.getUserId() : "null")); // Defensive logging


        if (user == null) { // Defensive check for null user in list
            Log.w(TAG, "Binding skipped for null user at position " + position);
            // Clear holder views or set to default if user is null
            holder.userName.setText("");
            holder.profileImage.setImageResource(R.drawable.default_profile_img);
            holder.selectCheckBox.setOnCheckedChangeListener(null);
            holder.selectCheckBox.setChecked(false);
            holder.itemView.setOnClickListener(null);
            return; // Skip binding the rest if user is null
        }


        holder.userName.setText(user.getUsername());

        String profileImageBase64 = user.getProfileImage();
        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            try {
                // Use context from adapter member variable
                Bitmap profileBitmap = decodeBase64ToBitmap(profileImageBase64);
                if (profileBitmap != null) {
                    holder.profileImage.setImageBitmap(profileBitmap);
                    // Log.d(TAG, "Profile image decoded and set for user: " + user.getUsername()); // Can be verbose
                } else {
                    Log.w(TAG, "Failed to decode Base64 profile image for user: " + user.getUsername() + ". Using default.");
                    holder.profileImage.setImageResource(R.drawable.default_profile_img);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting profile image for user: " + user.getUsername(), e);
                holder.profileImage.setImageResource(R.drawable.default_profile_img);
            }
        } else {
            // Log.d(TAG, "ProfileImage Base64 is null or empty for user: " + user.getUsername() + ". Using default."); // Can be verbose
            holder.profileImage.setImageResource(R.drawable.default_profile_img);
        }

        // --- Checkbox logic ---
        holder.selectCheckBox.setOnCheckedChangeListener(null);
        boolean isSelected = selectedUserIds.contains(user.getUserId()); // Use the member variable selectedUserIds
        holder.selectCheckBox.setChecked(isSelected);
        // Log.d(TAG, "Checkbox state set to " + isSelected + " for user: " + user.getUsername());


        holder.selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String userId = user.getUserId();
            if (TextUtils.isEmpty(userId)) {
                Log.w(TAG, "User ID is empty for selected user " + user.getUsername() + ", cannot add/remove from selected list.");
                // Optional: Revert checkbox state if user ID is invalid
                // buttonView.setChecked(!isChecked);
                return;
            }

            if (isChecked) {
                if (!selectedUserIds.contains(userId)) {
                    selectedUserIds.add(userId); // Use the member variable selectedUserIds
                    Log.d(TAG, "Selected: " + user.getUsername() + " (ID: " + userId + ")");
                }
            } else {
                selectedUserIds.remove(userId); // Use the member variable selectedUserIds
                Log.d(TAG, "Deselected: " + user.getUsername() + " (ID: " + userId + ")");
            }
            Log.d(TAG, "Current Selected Users (" + selectedUserIds.size() + "): " + selectedUserIds.toString());

            // *** NEW: Notify the Activity when selection changes ***
            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged(selectedUserIds.size()); // Pass the current count
            }
            // *** END NEW ***
        });

        // --- Item click listener to toggle checkbox ---
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Item view clicked for user: " + user.getUsername());
            boolean isChecked = holder.selectCheckBox.isChecked();
            holder.selectCheckBox.setChecked(!isChecked); // This will trigger the OnCheckedChangeListener
        });
        // --- End Item click listener ---
    }


    @Override
    public int getItemCount() {
        return userList.size(); // Use the size of the potentially filtered list
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        CircleImageView profileImage; // Use CircleImageView type
        CheckBox selectCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            selectCheckBox = itemView.findViewById(R.id.user_checkbox);
        }
    }

    //  Helper function to decode Base64 profile images
    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            // Use android.util.Base64 with DEFAULT flag
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error decoding Base64 string to Bitmap", e);
            return null;
        } catch (Exception e) { // Catch any other unexpected errors
            Log.e(TAG, "Unexpected error decoding Base64 string to Bitmap", e);
            return null;
        }
    }


    // Implement getFilter() method for Filterable interface
    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<UserModel> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                // If the search query is null or empty, show the full list
                filteredList.addAll(userListFull); // Use the full list
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (UserModel user : userListFull) { // Iterate through the full list
                    if (user != null && user.getUsername() != null && user.getUsername().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Update the adapter's list with the filtered results on the UI thread
            userList.clear(); // Clear the current displayed list
            if (results.values != null) {
                userList.addAll((List<UserModel>) results.values);
            }

            // Notify the adapter that the data set has changed
            notifyDataSetChanged();

            Log.d(TAG, "Filter results published. Showing " + userList.size() + " users.");
            // Note: Selection state remains in 'selectedUserIds' list.
        }
    };
}