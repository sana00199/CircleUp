package com.sana.circleup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class GroupAdapterAdmin extends RecyclerView.Adapter<GroupAdapterAdmin.GroupViewHolder> {
    private ArrayList<String> groupList;
    private OnGroupClickListener onGroupClickListener;

    public interface OnGroupClickListener {
        void onGroupClick(String groupName);
    }

    public GroupAdapterAdmin(ArrayList<String> groupList, OnGroupClickListener onGroupClickListener) {
        this.groupList = groupList;
        this.onGroupClickListener = onGroupClickListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_groupmembers, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        String groupName = groupList.get(position);
        holder.groupNameTextView.setText(groupName);
        holder.itemView.setOnClickListener(v -> onGroupClickListener.onGroupClick(groupName));
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.group_name_text_view);
        }
    }
}
