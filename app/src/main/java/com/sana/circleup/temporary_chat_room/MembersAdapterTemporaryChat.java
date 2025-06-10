package com.sana.circleup.temporary_chat_room;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sana.circleup.Contacts;
import com.sana.circleup.R;

import java.util.List;

public class MembersAdapterTemporaryChat extends RecyclerView.Adapter<MembersAdapterTemporaryChat.MemberViewHolder> {

    private List<Contacts> memberList;
    private Context context; // Keep context if needed elsewhere, but not directly used in bind for name only

    public MembersAdapterTemporaryChat(Context context, List<Contacts> memberList) {
        this.context = context; // Keep if needed for other things later
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_member_temporaryappbarmemberchat, parent, false); // Use the simplified layout
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Contacts member = memberList.get(position);

        holder.memberName.setText(member.getUsername()); // Display username

        // --- REMOVE Image Setting Logic ---
        // String base64Image = member.getProfileImage();
        // if (base64Image != null && !base64Image.isEmpty()) {
        //    try { ... decode and set image ... } catch (IllegalArgumentException e) { ... }
        // } else { ... set default image ... }
        // --- END REMOVE ---

        // You could still add a click listener here if needed
        // holder.itemView.setOnClickListener(...)
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    // ViewHolder class
    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView memberName;
        // --- REMOVE ImageView reference ---
        // CircleImageView memberImage;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.member_name); // Match ID from XML
            // --- REMOVE ImageView finding ---
            // memberImage = itemView.findViewById(R.id.member_profile_image);
        }
    }
}