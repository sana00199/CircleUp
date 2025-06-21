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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

// Adapter for the RecyclerView in ScheduledRecipientSelectionActivity
// Displays selectable users, groups, and temporary rooms.
public class SelectableRecipientAdapter extends RecyclerView.Adapter<SelectableRecipientAdapter.RecipientViewHolder> implements Filterable {

    private static final String TAG = "RecipientAdapter";

    // The list of all selectable items (users, groups, rooms)
    private List<SelectableRecipient> fullRecipientList; // The original unfiltered list
    private List<SelectableRecipient> filteredRecipientList; // The list currently shown in RecyclerView

    private final Context context;

    // Constructor
    public SelectableRecipientAdapter(Context context, List<SelectableRecipient> initialList) {
        this.context = context;
        // Store both the full list and the filtered list initially
        this.fullRecipientList = new ArrayList<>(initialList);
        this.filteredRecipientList = new ArrayList<>(initialList);
        Log.d(TAG, "Adapter initialized with " + initialList.size() + " items.");
    }

    // --- ViewHolder Class ---
    // This ViewHolder will be used for all item types (User, Group, Room)
    // using a single layout that can adapt (or you can create multiple ViewHolders if needed)
    public static class RecipientViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName;
        TextView itemType; // Optional: To show "Contact", "Group", "Room"
        CheckBox itemCheckbox;

        public RecipientViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views assuming a single list item layout (e.g., list_item_selectable_recipient.xml)
            itemImage = itemView.findViewById(R.id.item_image); // ImageView for profile/group/room image
            itemName = itemView.findViewById(R.id.item_name); // TextView for name
            itemType = itemView.findViewById(R.id.item_type); // Optional TextView for type
            itemCheckbox = itemView.findViewById(R.id.item_checkbox); // CheckBox for selection
        }
    }

    // --- Override onCreateViewHolder ---
    @NonNull
    @Override
    public RecipientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the single layout for a selectable recipient item
        // You need to create this layout file: res/layout/list_item_selectable_recipient.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_selectable_recipient, parent, false);
        return new RecipientViewHolder(view);
    }

    // --- Override onBindViewHolder ---
    @Override
    public void onBindViewHolder(@NonNull RecipientViewHolder holder, int position) {
        // Get the current item from the FILTERED list
        SelectableRecipient currentItem = filteredRecipientList.get(position);

        // Bind data to the views
        holder.itemName.setText(currentItem.getName());
        holder.itemType.setText(currentItem.getType()); // Display the item type (e.g., "user", "group")

        // Load image using Glide (handling Base64 or URL)
        String imageUrl = currentItem.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.startsWith("http")) {
                // Load from URL
                Glide.with(context).load(imageUrl).placeholder(R.drawable.default_profile_img).error(R.drawable.ic_error).into(holder.itemImage); // Use default user image as fallback
            } else {
                // Assume it's Base64
                try {
                    // Load Base64 using Glide
                    Glide.with(context).asBitmap().load(Base64.decode(imageUrl, Base64.DEFAULT)).placeholder(R.drawable.default_profile_img).error(R.drawable.ic_error).into(holder.itemImage); // Use default user image as fallback
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid Base64 for recipient image: " + currentItem.getId(), e);
                    holder.itemImage.setImageResource(R.drawable.default_profile_img); // Default on error
                } catch (Exception e) {
                    Log.e(TAG, "Error loading Base64 recipient image: " + currentItem.getId(), e);
                    holder.itemImage.setImageResource(R.drawable.default_profile_img); // Default on error
                }
            }
        } else {
            // Set a default image based on type if no image URL/Base64 is available
            if ("user".equals(currentItem.getType())) {
                holder.itemImage.setImageResource(R.drawable.default_profile_img); // Default for users
            } else if ("group".equals(currentItem.getType())) {
                holder.itemImage.setImageResource(R.drawable.default_group_img); // Default for groups
            } else if ("temporary_room".equals(currentItem.getType())) {
                holder.itemImage.setImageResource(R.drawable.temporary_chatroom);

            }
                // Default for rooms
//            } else {
//                holder.itemImage.setImageResource(R.drawable.ic_info); // Generic default
//            }
        }

        // Bind the selection state to the checkbox
        holder.itemCheckbox.setChecked(currentItem.isSelected());

        // Set click listener for the CheckBox itself
        holder.itemCheckbox.setOnClickListener(v -> {
            // Toggle the selection state of the current item when the checkbox is clicked
            boolean isChecked = holder.itemCheckbox.isChecked();
            currentItem.setSelected(isChecked);
            Log.d(TAG, "Recipient selected state changed: " + currentItem.getName() + " isSelected: " + isChecked);
            // Note: This directly updates the isSelected state in the original SelectableRecipient object
            // within both the full and filtered lists (since they hold references).
        });

        // Optional: Set click listener for the entire item view (not just checkbox)
        holder.itemView.setOnClickListener(v -> {
            // You can either just toggle the checkbox state and trigger its listener
            holder.itemCheckbox.performClick(); // Simulate checkbox click
            // Or implement separate logic for item click vs checkbox click if needed
        });

        // Optional: Handle long clicks on the item view if needed
        // holder.itemView.setOnLongClickListener(...)
    }

    // --- Override getItemCount ---
    @Override
    public int getItemCount() {
        // Return the size of the FILTERED list
        return filteredRecipientList.size();
    }

    // --- Implement Filterable interface methods (for SearchView) ---
    @Override
    public Filter getFilter() {
        return new RecipientFilter();
    }

    private class RecipientFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<SelectableRecipient> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                // No constraint, show the full original list
                suggestions.addAll(fullRecipientList);
            } else {
                // Convert constraint to lowercase for case-insensitive filtering
                String filterPattern = constraint.toString().toLowerCase().trim();

                // Filter the full original list
                for (SelectableRecipient item : fullRecipientList) {
                    // Check if the item's name or type contains the filter pattern
                    // You might want to add other fields to search against (e.g., phone number for users)
                    if (item.getName().toLowerCase().contains(filterPattern) ||
                            item.getType().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions; // The filtered list
            results.count = suggestions.size(); // The size of the filtered list
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Update the filtered list with the results
            filteredRecipientList.clear();
            if (results.values != null) {
                filteredRecipientList.addAll((List<SelectableRecipient>) results.values);
            }
            // Notify the adapter that the data has changed
            notifyDataSetChanged();
            Log.d(TAG, "Filter results published. New filtered list size: " + filteredRecipientList.size());
            // TODO: Update the empty state TextView in the Activity here if possible,
            // as the adapter doesn't have direct access to it. You'd need a callback or
            // handle this logic in the Activity's search listener.
        }
    }

    // --- Helper method to get the currently selected UIDs ---
    // This method iterates through the *full* list to find selected items
    public List<String> getSelectedRecipientIds() {
        List<String> selectedIds = new ArrayList<>();
        for (SelectableRecipient item : fullRecipientList) {
            if (item.isSelected()) {
                selectedIds.add(item.getId());
            }
        }
        Log.d(TAG, "Found " + selectedIds.size() + " selected recipient IDs.");
        return selectedIds;
    }

    // --- Helper method to get the type of selected recipients ---
    // Returns a map of selected ID to its type (e.g., "user", "group", "temporary_room")
    public List<SelectedRecipientInfo> getSelectedRecipientsInfo() {
        List<SelectedRecipientInfo> selectedInfoList = new ArrayList<>();
        for (SelectableRecipient item : fullRecipientList) {
            if (item.isSelected()) {
                // Use a simple data class to hold ID and Type
                selectedInfoList.add(new SelectedRecipientInfo(item.getId(), item.getType(), item.getName())); // Include name for convenience
            }
        }
        Log.d(TAG, "Found " + selectedInfoList.size() + " selected recipient Info objects.");
        return selectedInfoList;
    }

    // --- Helper data class for selected recipient info ---
    public static class SelectedRecipientInfo {
        private String id;
        private String type;
        private String name; // Include name

        public SelectedRecipientInfo(String id, String type, String name) {
            this.id = id;
            this.type = type;
            this.name = name;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getName() { return name; } // Getter for name
    }

    // --- Method to update the list data (e.g., after fetching from Firebase/Room) ---
    public void updateList(List<SelectableRecipient> newList) {
        this.fullRecipientList.clear();
        this.fullRecipientList.addAll(newList);
        // Re-filter the list after updating the full list
        getFilter().filter(null); // Passing null or empty string effectively shows the full list
        Log.d(TAG, "Adapter data list updated with " + newList.size() + " new items.");
    }
}