package com.sana.circleup.navigation_fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;
import com.sana.circleup.Contacts;
import com.sana.circleup.ProfileUserInfoActivity;
import com.sana.circleup.R;
import com.sana.circleup.Users;

import java.util.ArrayList;
import java.util.List;


public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.ViewHolder> {

    private List<Contacts> contactsList;
    private Context context;

    public FindFriendsAdapter(Context context, List<Contacts> contactsList) {
        this.context = context;
        this.contactsList = contactsList;
    }

    public FindFriendsAdapter(ArrayList<Users> allUsersList) {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_friends_display_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contacts model = contactsList.get(position);

        //  Retrieve UID correctly
        String userId = model.getUid();
        if (userId == null || userId.isEmpty()) {
            Log.e("FindFriendsAdapter", "Error: UID is null at position " + position);
            return; // Skip this user
        }

        // set username
        if (position == 0) {
            holder.username.setText(Html.fromHtml("<b>" + model.getUsername() + " (Me)</b>"));
        } else {
            holder.username.setText(model.getUsername());
        }

        //  Set status
        holder.status.setText(model.getStatus() != null ? model.getStatus() : "No status");

        //  Decode and Set Profile Image
        if (model.getProfileImage() != null && !model.getProfileImage().isEmpty()) {
            Bitmap bitmap = decodeBase64(model.getProfileImage());
            if (bitmap != null) {
                holder.profileImage.setImageBitmap(bitmap);
            } else {
                holder.profileImage.setImageResource(R.drawable.default_profile_img);
            }
        } else {
            holder.profileImage.setImageResource(R.drawable.default_profile_img);
        }

        //  Handle Click to Open Profile
        holder.itemView.setOnClickListener(view -> {
            Log.d("FindFriendsAdapter", "Passing UID: " + userId);

            Intent profileIntent = new Intent(context, ProfileUserInfoActivity.class);
            profileIntent.putExtra("uid", userId);
            context.startActivity(profileIntent);
        });

        //  Hide friend request buttons
        holder.acceptButton.setVisibility(View.INVISIBLE);
        holder.cancelButton.setVisibility(View.INVISIBLE);

        //  Hide checkbox
        holder.checkBox.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, status;
        ImageView profileImage;
        CheckBox checkBox;
        Button acceptButton, cancelButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.users_profile_name);
            status = itemView.findViewById(R.id.users_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_button);
            cancelButton = itemView.findViewById(R.id.request_cancel_button);
        }
    }

    // ✅ Improved Base64 decoding
    private Bitmap decodeBase64(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("FindFriendsAdapter", "Image decoding failed: " + e.getMessage());
            return null;
        }
    }




}
