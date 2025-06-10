package com.sana.circleup;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MemberAdapterAdmin extends RecyclerView.Adapter<MemberAdapterAdmin.MemberViewHolder> {

    private ArrayList<Member> memberList;  // Change this to store Member objects
    private OnMemberClickListener onMemberClickListener;

    // Update constructor to accept List<Member>
    public MemberAdapterAdmin(ArrayList<Member> memberList, OnMemberClickListener onMemberClickListener) {
        this.memberList = memberList;
        this.onMemberClickListener = onMemberClickListener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_admin, parent, false);
        return new MemberViewHolder(view);
    }

    @Override

    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = memberList.get(position);

        // Log data to check if the adapter is getting the right information
        Log.d("MemberAdapterAdmin", "Binding member: " + member.getId() + " - " + member.getName());

        holder.memberTextView.setText(member.getName() != null ? member.getName() : "Unknown Member");

        holder.removeButton.setOnClickListener(v -> {
            Log.d("MemberAdapterAdmin", "Remove clicked for: " + member.getId());
            onMemberClickListener.onMemberClick(member.getId());
        });
    }


    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {

        TextView memberTextView;
        Button removeButton;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberTextView = itemView.findViewById(R.id.member_text_view);
            removeButton = itemView.findViewById(R.id.remove_member_button);
        }
    }

    public interface OnMemberClickListener {
        void onMemberClick(String memberId);
    }
}

