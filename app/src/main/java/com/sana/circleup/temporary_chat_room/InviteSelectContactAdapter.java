package com.sana.circleup.temporary_chat_room;

import android.annotation.SuppressLint; // Keep if needed for position
import android.content.Context; // Keep Context import
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.text.TextUtils; // Keep TextUtils import
import android.util.Log; // Keep Log import
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox; // Keep CheckBox import
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Keep Glide
import com.google.firebase.database.DataSnapshot; // Keep Firebase imports
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

// Removed unused imports
// import com.sana.circleup.Contacts; // Removed
// import java.text.BreakIterator; // Removed
import com.sana.circleup.R;
import com.sana.circleup.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


// Corrected Adapter definition
public class InviteSelectContactAdapter extends RecyclerView.Adapter<InviteSelectContactAdapter.ContactViewHolder> {

    private static final String TAG = "InviteSelectAdapter"; // Added TAG

    // Removed: private List<Contacts> contactList; // <<< REMOVE THIS LINE

    private List<String> contactUids; // List of friend UIDs to display
    private List<String> selectedUids; // List of UIDs currently selected
    private DatabaseReference usersRef; // Reference to /Users node to fetch user details
    private Context context; // Context (Activity)

    // Map to cache fetched UserModel data by UID
    private Map<String, UserModel> userModelCache = new HashMap<>();


    /**
     * Constructor for the InviteSelectContactAdapter.
     *
     * @param contactUids        List of UIDs of friends to display for selection.
     * @param initialSelectedUids List of UIDs that should be initially selected.
     * @param usersRef           DatabaseReference to the Firebase "/Users" node.
     */
    // Corrected Constructor signature (removed the List<Contacts> one)
    public InviteSelectContactAdapter(List<String> contactUids, List<String> initialSelectedUids, DatabaseReference usersRef) {
        this.contactUids = contactUids;
        // Initialize selectedUids with initial selections
        this.selectedUids = new ArrayList<>(initialSelectedUids); // Copy the list

        this.usersRef = usersRef;
        // Context will be set in onCreateViewHolder
    }

    // Removed the unused constructor:
    // public InviteSelectContactAdapter(List<Contacts> contactList) { this.contactList = contactList; }


    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext(); // Get context here
        View view = LayoutInflater.from(context) // Use the context
                .inflate(R.layout.temporaryroom_list_item_contact_selectable, parent, false); // Use the selectable item layout
        return new ContactViewHolder(view);
    }

    // Use @SuppressLint("RecyclerView") if needed, but often avoidable with getAdapterPosition() check
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String uid = contactUids.get(position); // Get the UID for this position

        if (TextUtils.isEmpty(uid)) {
            Log.w(TAG, "onBindViewHolder: Empty UID at position " + position);
            holder.clear(); // Clear the holder if UID is invalid
            return;
        }

        // Try to get UserModel from cache first
        UserModel user = userModelCache.get(uid);

        if (user != null) {
            // If user data is in cache, bind it immediately
            bindViewHolder(holder, user, uid);
            holder.checkBox.setEnabled(true); // Ensure checkbox is enabled if loaded from cache
        } else {
            // If user data is not in cache, fetch it from Firebase
            // Show placeholders while loading
            holder.clear(); // Clear old data/show placeholders
            holder.contactName.setText("Loading..."); // Use contactName here
            holder.checkBox.setEnabled(false); // Disable checkbox while loading

            // Fetch user data asynchronously
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserModel fetchedUser = snapshot.getValue(UserModel.class);
                        if (fetchedUser != null) {
                            fetchedUser.setUserId(uid); // Set the UID from the key
                            userModelCache.put(uid, fetchedUser); // Add to cache
                            // Check if the ViewHolder is still bound to the same item before binding data
                            // This prevents displaying data for the wrong user if the RecyclerView item was recycled
                            if (holder.getAdapterPosition() == position) {
                                bindViewHolder(holder, fetchedUser, uid); // Bind the fetched data
                                holder.checkBox.setEnabled(true); // Enable checkbox
                            } else {
                                Log.d(TAG, "ViewHolder recycled, skipping bind for UID: " + uid + " at old position " + position);
                            }
                        } else {
                            Log.w(TAG, "InviteSelectContactAdapter: UserModel null for UID: " + uid);
                            if (holder.getAdapterPosition() == position) holder.clear(); // Clear on null model
                        }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to fetch user data for UID " + uid, error.toException());
                    holder.clear(); // Clear on error


                }
            });
        }
    }

    // Helper method to bind data to the ViewHolder
    private void bindViewHolder(ContactViewHolder holder, UserModel user, String uid) {
        if (user == null || holder == null || uid == null) {
            Log.w(TAG, "bindViewHolder called with null user, holder, or uid.");
            return;
        }
        // Bind name and status
        holder.contactName.setText(user.getUsername() != null ? user.getUsername() : "Unknown"); // Use contactName here
        holder.contactStatus.setText(user.getStatus() != null ? user.getStatus() : "No Status"); // Use contactStatus here

        // Load profile image (decode Base64 or use Glide for URL)
        String imageString = user.getProfileImage(); // Assuming getProfileImage() gives Base64 or URL
        if (context != null && holder.contactImage != null) { // Ensure context and ImageView are available
            if (!TextUtils.isEmpty(imageString)) {
                if (imageString.startsWith("http")) {
                    // Load from URL using Glide
                    Glide.with(context).load(imageString).placeholder(R.drawable.default_profile_img).into(holder.contactImage);
                } else {
                    // Load from Base64 string manually
                    try {
                        byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (decodedBitmap != null) {
                            holder.contactImage.setImageBitmap(decodedBitmap);
                        } else {
                            Log.w(TAG, "Base64 image decoding resulted in null Bitmap for UID: " + uid);
                            holder.contactImage.setImageResource(R.drawable.default_profile_img);
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Error decoding Base64 image for UID " + uid + ": Invalid Base64 string", e);
                        holder.contactImage.setImageResource(R.drawable.default_profile_img); // Default on error
                    } catch (Exception e) { // Catch other bitmap decoding errors
                        Log.e(TAG, "Error decoding Base64 image for UID " + uid, e);
                        holder.contactImage.setImageResource(R.drawable.default_profile_img); // Default on error
                    }
                }
            } else {
                holder.contactImage.setImageResource(R.drawable.default_profile_img); // Default image if no image data
            }
        } else {
            Log.w(TAG, "Context or contactImage is null in bindViewHolder, cannot load image.");
            if (holder.contactImage != null) holder.contactImage.setImageResource(R.drawable.default_profile_img); // Default if context missing
        }


        // --- Handle CheckBox State and Clicks ---
        // Set initial state based on the selectedUids list
        holder.checkBox.setOnCheckedChangeListener(null); // Remove listener before setting state
        holder.checkBox.setChecked(selectedUids.contains(uid));

        // Set click listener on the checkbox
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedUids.contains(uid)) {
                    selectedUids.add(uid);
                    Log.d(TAG, "Selected UID: " + uid);
                }
            } else {
                selectedUids.remove(uid);
                Log.d(TAG, "Deselected UID: " + uid);
            }
        });

        // Optional: Set click listener on the whole item view to toggle checkbox
        holder.itemView.setOnClickListener(v -> {
            if (holder.checkBox.isEnabled()) { // Only toggle if checkbox is enabled (data is loaded)
                holder.checkBox.setChecked(!holder.checkBox.isChecked()); // Toggle checkbox state
                // The OnCheckedChangeListener above will handle adding/removing from selectedUids list
            } else {
                Log.d(TAG, "Item click ignored, checkbox is disabled (data loading).");
            }
        });


        // Checkbox enablement is handled above in the fetch callback/cache hit logic
        // holder.checkBox.setEnabled(true); // Do not force enable here

        // --- End CheckBox Handling ---
    }


    @Override
    public int getItemCount() {
        // Return the count of UIDs provided to the adapter
        return contactUids != null ? contactUids.size() : 0; // <<< Corrected to use contactUids and add null check
    }

    // Method to get the UIDs of selected contacts
    public List<String> getSelectedUids() {
        return selectedUids; // Return the list of selected UIDs
    }

    // Method to get the usernames of selected contacts (for confirmation toast)
    public List<String> getSelectedNames() {
        List<String> names = new ArrayList<>();
        for (String uid : selectedUids) {
            // Get the UserModel from the cache using the selected UID
            UserModel user = userModelCache.get(uid);
            if (user != null) {
                names.add(user.getUsername() != null ? user.getUsername() : "Unknown");
            } else {
                // Fallback if user somehow not in cache (shouldn't happen if bound)
                names.add("Unknown (UID: " + uid + ")"); // Include UID for debugging if name missing
            }
        }
        return names;
    }


    // ViewHolder class
    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        // Removed: public BreakIterator userName; // REMOVE THIS INCORRECT DECLARATION
        TextView contactName, contactStatus; // <<< Use these TextViews
        CircleImageView contactImage;
        CheckBox checkBox;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.text_contact_name); // Match IDs from XML
            contactStatus = itemView.findViewById(R.id.text_contact_status); // Match IDs from XML
            contactImage = itemView.findViewById(R.id.contact_profile_image); // Match IDs from XML
            checkBox = itemView.findViewById(R.id.checkbox_select_contact); // Match IDs from XML

            // *** Optional: Add null checks for debugging findViewById if still getting crashes ***
            if (contactName == null) Log.e(TAG, "ContactViewHolder: text_contact_name TextView not found in layout temporaryroom_list_item_contact_selectable!");
            if (contactStatus == null) Log.e(TAG, "ContactViewHolder: text_contact_status TextView not found in layout temporaryroom_list_item_contact_selectable!");
            if (contactImage == null) Log.e(TAG, "ContactViewHolder: contact_profile_image CircleImageView not found in layout temporaryroom_list_item_contact_selectable!");
            if (checkBox == null) Log.e(TAG, "ContactViewHolder: checkbox_select_contact CheckBox not found in layout temporaryroom_list_item_contact_selectable!");
            // *** End optional null checks ***
        }

        // Helper method to clear holder content (used while loading or if data is invalid)
        public void clear() {
            if (contactName != null) contactName.setText(""); // Use contactName
            if (contactStatus != null) contactStatus.setText(""); // Use contactStatus
            if (contactImage != null) contactImage.setImageResource(R.drawable.default_profile_img);
            if (checkBox != null) {
                checkBox.setChecked(false); // Uncheck by default when clearing
                checkBox.setEnabled(false); // Disable while loading/cleared
            }
        }
    }
}