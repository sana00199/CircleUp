package com.sana.circleup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DisableUserAdapter extends RecyclerView.Adapter<DisableUserAdapter.ViewHolder> {

    private List<EnableDisableUserModelaAdmin> disabledUsers;
    private OnToggleUserStatusListener toggleUserStatusListener;

    // Updated constructor to accept both disabledUsers and toggleUserStatusListener
    public DisableUserAdapter(List<EnableDisableUserModelaAdmin> disabledUsers, OnToggleUserStatusListener listener) {
        this.disabledUsers = disabledUsers;
        this.toggleUserStatusListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_disabled_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnableDisableUserModelaAdmin user = disabledUsers.get(position);

        holder.usernameTextView.setText(user.getUsername());
        holder.emailTextView.setText(user.getEmail());

        holder.enableButton.setOnClickListener(v -> {
            if (toggleUserStatusListener != null) {
                toggleUserStatusListener.onToggle(user.getUserId(), false); // Set newStatus to false to enable the user
            }
        });
    }

    @Override
    public int getItemCount() {
        return disabledUsers.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, emailTextView;
        Button enableButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username_textview);
            emailTextView = itemView.findViewById(R.id.email_textview);
            enableButton = itemView.findViewById(R.id.enable_button);
        }
    }

    // Interface for toggle callback
    public interface OnToggleUserStatusListener {
        void onToggle(String userId, boolean newStatus);
    }
}
