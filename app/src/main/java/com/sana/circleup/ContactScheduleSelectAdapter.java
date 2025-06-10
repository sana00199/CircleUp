package com.sana.circleup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
//
//public class ContactScheduleSelectAdapter extends RecyclerView.Adapter<ContactScheduleSelectAdapter.ViewHolder> {
//    private final List<UserModel> userList;
//    private final List<String> selectedIds;
//
//    public ContactScheduleSelectAdapter(List<UserModel> userList, List<String> selectedIds) {
//        this.userList = userList;
//        this.selectedIds = selectedIds;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_checkbox, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @NonNull
//
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        UserModel user = userList.get(position);
//
//        holder.userName.setText(user.getUsername());
//
//
//        // Manage the checkbox state
//        holder.checkBox.setOnCheckedChangeListener(null);
//        holder.checkBox.setChecked(selectedIds.contains(user.getUserId()));
//        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                selectedIds.add(user.getUserId());
//            } else {
//                selectedIds.remove(user.getUserId());
//            }
//        });
//
//        // Optionally, add an onClickListener to toggle the checkbox state
//        holder.itemView.setOnClickListener(v -> {
//            boolean isChecked = !holder.checkBox.isChecked();
//            holder.checkBox.setChecked(isChecked);
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return userList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView userName;
//
//        CheckBox checkBox;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            userName = itemView.findViewById(R.id.user_name);
//
//            checkBox = itemView.findViewById(R.id.checkbox_user);
//        }
//    }
//
//    // Helper method to decode Base64 profile images
//    private Bitmap decodeBase64ToBitmap(String base64String) {
//        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//    }
//}






import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Filter; // Import Filter
import android.widget.Filterable; // Import Filterable

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView; // Import CircleImageView


// Implement Filterable interface
public class ContactScheduleSelectAdapter extends RecyclerView.Adapter<ContactScheduleSelectAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "ContactSchedAdapter";

    private List<UserModel> userList; // List currently displayed (filtered)
    private List<UserModel> userListFull; // Full list of contacts (for filtering)
    private final List<String> selectedUserIds; // List of selected user UIDs - Pass by reference
    private final Context context;



    // Listener interface and member variable
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount); // Method to notify Activity
    }
    private OnSelectionChangedListener selectionChangedListener;


    // *** CORRECTED Constructor: Fix the assignment error and list initialization ***
    public ContactScheduleSelectAdapter(Context context, List<UserModel> userList, List<String> selectedUsers) {
        this.context = context;
        // Initialize the displayed list with the initial data (which will be filtered)
        this.userList = userList; // Keep reference to the list passed from Activity (which should be Mutable)

        // *** Create a COPY of the initial list for filtering ***
        // userListFull should always hold the complete, original data from the load.
        this.userListFull = new ArrayList<>(userList);

        // *** Assign the parameter 'selectedUsers' to the member variable 'selectedUserIds' ***
        // This list should be the one managed by the Activity (passed by reference)
        this.selectedUserIds = selectedUsers;

        // *** REMOVE the incorrect duplicate assignment line here ***
        // The line 'this.selectedUsers = selectedUsers;' caused a compilation error
        // because 'selectedUsers' is not a member variable name in this class.
        // (The member variable is 'selectedUserIds').




        Log.d(TAG, "Adapter initialized with " + userList.size() + " users.");
        // Initially, the userList and userListFull are the same.
    }
    // *** END CORRECTED Constructor ***


    // *** CORRECTED Method to update the list from Activity (Called by loadContacts) ***
    // This method is called by ScheduledMSG.showContactSelectionDialog when the full list is fetched.
    // It updates the adapter's internal list (userList and userListFull) and triggers filtering.
    // REMOVE the previous incorrect setContactList(ArrayList<Object> objects) method entirely.
    public void setContactList(List<UserModel> newUserList) {
        Log.d(TAG, "setContactList called with " + (newUserList != null ? newUserList.size() : 0) + " users.");

        // Update both the displayed list (userList) and the full list for filtering (userListFull)
        this.userListFull = (newUserList != null) ? new ArrayList<>(newUserList) : new ArrayList<>();

        // Also update the displayed list. Clear existing data.
        this.userList.clear();
        if (newUserList != null) {
            this.userList.addAll(new ArrayList<>(newUserList)); // Add all fetched users to the displayed list
        }


        // Re-apply filter to update the displayed list based on current search query (if any)
        // This ensures the UI is updated correctly after the data is loaded.
        // Pass null to show the full list initially if no search query is active.
        getFilter().filter(null); // This will trigger publishResults -> notifyDataSetChanged()
    }




    // NEW: Method to get the list of ALL original users the adapter was given
    public List<UserModel> getUserListFull() {
        return userListFull != null ? new ArrayList<>(userListFull) : new ArrayList<>();
    }

    // NEW: Method to get the list of users currently displayed (after filtering)
    public List<UserModel> getUserListFiltered() {
        return userList != null ? new ArrayList<>(userList) : new ArrayList<>();
    }

    // NEW: Method to get the list of *selected* UserModel objects
// This method iterates through the *full* list and checks against selectedUserIds
    public List<UserModel> getSelectedUserModels() {
        List<UserModel> selectedModels = new ArrayList<>();
        if (userListFull != null && selectedUserIds != null) {
            for (UserModel user : userListFull) {
                if (user != null && user.getUserId() != null && selectedUserIds.contains(user.getUserId())) {
                    selectedModels.add(user);
                }
            }
        }
        return selectedModels;
    }


    // *** END CORRECTED Method ***


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_additem_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position); // Use the potentially filtered userList

        if (user == null) {
            Log.w(TAG, "Binding skipped for null user at position " + position);
            holder.userName.setText("");
            holder.profileImage.setImageResource(R.drawable.default_profile_img);
            holder.selectCheckBox.setOnCheckedChangeListener(null);
            holder.selectCheckBox.setChecked(false);
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.userName.setText(user.getUsername());

        String profileImageBase64 = user.getProfileImage();
        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            try {
                Bitmap profileBitmap = decodeBase64ToBitmap(profileImageBase64);
                if (profileBitmap != null) {
                    holder.profileImage.setImageBitmap(profileBitmap);
                } else {
                    Log.w(TAG, "Failed to decode Base64 profile image for user: " + user.getUsername() + ". Using default.");
                    holder.profileImage.setImageResource(R.drawable.default_profile_img);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting profile image for user: " + user.getUsername(), e);
                holder.profileImage.setImageResource(R.drawable.default_profile_img);
            }
        } else {
            holder.profileImage.setImageResource(R.drawable.default_profile_img);
        }

        // --- Checkbox logic ---
        holder.selectCheckBox.setOnCheckedChangeListener(null);
        boolean isSelected = selectedUserIds.contains(user.getUserId());
        holder.selectCheckBox.setChecked(isSelected);


        holder.selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String userId = user.getUserId();
            if (TextUtils.isEmpty(userId)) {
                Log.w(TAG, "User ID is empty for user " + user.getUsername() + ", cannot select/deselect.");
                return;
            }

            if (isChecked) {
                if (!selectedUserIds.contains(userId)) {
                    selectedUserIds.add(userId);
                }
            } else {
                selectedUserIds.remove(userId);
            }
            Log.d(TAG, "Selected Users (" + selectedUserIds.size() + "): " + selectedUserIds.toString());

            // *** Notify the Activity when selection changes (if a listener is set) ***
            // Although we removed live count update in the dialog title,
            // keeping this listener allows the Activity to know when the selection count changes (e.g., for enabling a button outside the dialog)
            // In ScheduledMSG dialog, this listener is not strictly necessary as the count is read when "Done" is clicked.
            // But it's good practice to have it for reusability.
            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged(selectedUserIds.size());
            }
            // *** END NEW ***
        });

        // --- Item click listener to toggle checkbox ---
        holder.itemView.setOnClickListener(v -> {
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
        CircleImageView profileImage;
        CheckBox selectCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            selectCheckBox = itemView.findViewById(R.id.user_checkbox);
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error decoding Base64 string to Bitmap", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error decoding Base64 string to Bitmap", e);
            return null;
        }
    }


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
                filteredList.addAll(userListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (UserModel user : userListFull) {
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
            userList.clear();
            if (results.values != null) {
                // Make sure to cast to the correct type: List<UserModel>
                userList.addAll((List<UserModel>) results.values);
            }

            notifyDataSetChanged();

            Log.d(TAG, "Filter results published. Showing " + userList.size() + " users.");
        }
    };
}