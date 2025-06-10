package com.sana.circleup;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsViewHolder extends RecyclerView.ViewHolder {

    public TextView username, status;
    public CircleImageView profileImage;
    public ImageView onlineStatus;

    public FindFriendsViewHolder(@NonNull View itemView) {
        super(itemView);

        username = itemView.findViewById(R.id.users_profile_name);
        status = itemView.findViewById(R.id.users_profile_status);
        profileImage = itemView.findViewById(R.id.users_profile_image);
    }
}

