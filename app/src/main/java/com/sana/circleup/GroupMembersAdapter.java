package com.sana.circleup;

import android.app.MediaRouteButton;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.ViewHolder> {
    private Context context;
    private List<UserModel> groupMembersList;
    private String adminId;
    private String currentUserId;
    private OnMemberLongClickListener longClickListener;

    public interface OnMemberLongClickListener {
        void onMemberLongClick(UserModel user);
    }

    // Constructor with context
    public GroupMembersAdapter(Context context, List<UserModel> groupMembersList) {
        this.context = context;
        this.groupMembersList = groupMembersList;
    }

    // Constructor with admin and long click listener
    public GroupMembersAdapter(Context context, List<UserModel> groupMembersList, String adminId, String currentUserId, OnMemberLongClickListener longClickListener) {
        this.context = context;
        this.groupMembersList = groupMembersList;
        this.adminId = adminId;
        this.currentUserId = currentUserId;
        this.longClickListener = longClickListener;


        //  Move Admin to Top
        sortAdminToTop();
    }


    //  Function to Move Admin to Top of List
    private void sortAdminToTop() {
        if (adminId == null) return;

        Collections.sort(groupMembersList, new Comparator<UserModel>() {
            @Override
            public int compare(UserModel u1, UserModel u2) {
                if (u1.getUserId().equals(adminId)) return -1; // Admin goes first
                if (u2.getUserId().equals(adminId)) return 1;  // Others go below
                return 0;
            }
        });

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_additem_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = groupMembersList.get(position);
        holder.userName.setText(user.getUsername());


        //  Add "(Admin)" next to admin name
        if (user.getUserId().equals(adminId)) {
            holder.userName.setText(user.getUsername() + " (Admin)");
        } else {
            holder.userName.setText(user.getUsername());
        }

        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            byte[] decodedString = Base64.decode(user.getProfileImage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.userProfileImage.setImageBitmap(decodedByte);
        } else {
            holder.userProfileImage.setImageResource(R.drawable.default_profile_img);
        }

        holder.userCheckbox.setVisibility(View.GONE);

        if (currentUserId.equals(adminId)) {
            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onMemberLongClick(user);
                }
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }

    }

    @Override
    public int getItemCount() {
        return groupMembersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfileImage;
        CheckBox userCheckbox;
        TextView userName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_name);
            userCheckbox = itemView.findViewById(R.id.user_checkbox);
        }
    }
}
