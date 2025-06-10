package com.sana.circleup.drawingboard_chatgroup;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64; // Use android.util.Base64
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sana.circleup.R; // <<< USE YOUR ACTUAL R FILE
import de.hdodenhof.circleimageview.CircleImageView; // If you use CircleImageView library

import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // For comparing user IDs safely

// Adapter to display a horizontal list of active users in the DrawingActivity
public class ActiveUserAdapter extends RecyclerView.Adapter<ActiveUserAdapter.ActiveUserViewHolder> {

    private static final String TAG = "ActiveUserAdapter";
    private List<ActiveUser> activeUserList; // List of users to display
    private Context context; // Activity context (for loading images, toasts etc.)
    // We might not need usersRef here if the Activity fetches user details and provides them in the ActiveUser object.
    // Let's assume the Activity provides the full ActiveUser object with Base64 image.

    // Constructor
    public ActiveUserAdapter(Context context, List<ActiveUser> activeUserList) {
        this.context = context;
        // Initialize with the list provided (which will be managed by the Activity)
        this.activeUserList = activeUserList != null ? activeUserList : new ArrayList<>();
    }

    // --- Method to update the list of active users ---
    // Called by the DrawingActivity whenever the list changes (user joins/leaves)
    public void setActiveUsers(List<ActiveUser> newActiveUsers) {
        // Using DiffUtil is more efficient for large lists, but for simplicity, replace and notify
        this.activeUserList = newActiveUsers != null ? newActiveUsers : new ArrayList<>();
        notifyDataSetChanged(); // Notify the adapter that the entire dataset has changed
        Log.d(TAG, "Active user list updated. New size: " + this.activeUserList.size());
    }


    @NonNull
    @Override
    public ActiveUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single active user item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_user_drawingclass, parent, false); // <<< Use your layout file name
        return new ActiveUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveUserViewHolder holder, int position) {
        // Get the ActiveUser object for the current position
        ActiveUser user = activeUserList.get(position);

        // Set the user's name
        if (holder.userName != null) {
            holder.userName.setText(user.getName() != null ? user.getName() : "Unknown");
        }

        // Load and set the user's profile image from Base64 string
        if (holder.profileImage != null) {
            String base64Image = user.getProfileImageBase64(); // Get Base64 from the ActiveUser object

            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    // Decode the Base64 string to a Bitmap
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    // Set the bitmap to the CircleImageView
                    if (decodedBitmap != null) { holder.profileImage.setImageBitmap(decodedBitmap); }
                    else {
                        Log.w(TAG, "Decoded bitmap is null for user " + user.getUserId() + ". Using default.");
                        holder.profileImage.setImageResource(R.drawable.default_profile_img); // Default on decoding issue
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid Base64 string for user profile " + user.getUserId(), e);
                    holder.profileImage.setImageResource(R.drawable.default_profile_img); // Default on Base64 error
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error decoding user profile image " + user.getUserId(), e);
                    holder.profileImage.setImageResource(R.drawable.default_profile_img); // Default on other errors
                }
            } else {
                // If Base64 string is null or empty, set the default profile image
                holder.profileImage.setImageResource(R.drawable.default_profile_img);
            }

            // Optional: Add a click listener to the profile image if you want to show user profile
            // holder.profileImage.setOnClickListener(v -> {
            //     // Handle click, e.g., open user profile activity
            //     Toast.makeText(context, "Clicked on user: " + user.getName(), Toast.LENGTH_SHORT).show();
            // });
        }
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return activeUserList.size();
    }

    // --- ViewHolder Class ---
    // Holds the views for a single list item
    public static class ActiveUserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage; // The profile picture view
        TextView userName; // The username TextView

        public ActiveUserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views using their IDs from item_active_user_drawingclass.xml
            profileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_name);
        }
    }
}