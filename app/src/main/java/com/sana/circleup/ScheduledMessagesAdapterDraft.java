package com.sana.circleup;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
//
//public class ScheduledMessagesAdapterDraft extends RecyclerView.Adapter<ScheduledMessagesAdapterDraft.ScheduledMessageViewHolder> {
//
//    private Context context;
//    private List<ScheMsg> scheduledMessages;
//    private FirebaseUser currentUser;
//
//    // Constructor
//    public ScheduledMessagesAdapterDraft(Context context, List<ScheMsg> scheduledMessages) {
//        this.context = context;
//        this.scheduledMessages = scheduledMessages;
//        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
//    }
//
//    @NonNull
//    @Override
//    public ScheduledMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.itemdraft_scheduled_message, parent, false);
//        return new ScheduledMessageViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ScheduledMessageViewHolder holder, int position) {
//        ScheMsg message = scheduledMessages.get(position);
//
//
//        // Ensure receiverNames is not null
//        if (message.getReceiverNames() != null && !message.getReceiverNames().isEmpty()) {
//            // Use TextUtils.join to display names, ensure receiverNames is not null
//            String receiverNamesStr = TextUtils.join(", ", message.getReceiverNames());
//            holder.tvReceiver.setText(receiverNamesStr);
//        } else {
//            // Handle case where receiverNames is null or empty
//            holder.tvReceiver.setText("No receiver names available");
//        }
//
//        // Only show messages scheduled by the current user
//        if (message.getSenderId().equals(currentUser.getUid())) {
//            holder.tvMessage.setText(message.getMessage());
//            holder.tvTime.setText(message.getScheduledTimeMillis());
//
//            holder.tvReceiver.setText(message.getReceiverNamesStr());
//
//            // Concatenate all receiver names into one string
//            String receivers = "To: " + TextUtils.join(", ", message.getReceiverNames());
//            holder.tvReceiver.setText(receivers);
//
//
//            holder.btnEdit.setOnClickListener(v -> {
//                // Handle edit action
//                // Open a dialog or another activity to edit the message
//            });
//
//            holder.btnDelete.setOnClickListener(v -> {
//                // Handle delete action
//                // Remove the message from the database
//                deleteScheduledMessage(message);
//            });
//        } else {
//            holder.itemView.setVisibility(View.GONE); // Hide messages from other users
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return scheduledMessages.size();
//    }
//
//    // ViewHolder for each message
//    public static class ScheduledMessageViewHolder extends RecyclerView.ViewHolder {
//        TextView tvMessage, tvTime, tvReceiver;
//        Button btnEdit, btnDelete;
//
//        public ScheduledMessageViewHolder(View itemView) {
//            super(itemView);
//            tvMessage = itemView.findViewById(R.id.tvMessage);
//            tvTime = itemView.findViewById(R.id.tvTime);
//            btnEdit = itemView.findViewById(R.id.btnEdit);
//            btnDelete = itemView.findViewById(R.id.btnDelete);
//            tvReceiver = itemView.findViewById(R.id.tvReceiver);
//        }
//    }
//
//    // Delete the scheduled message from Firebase
//    private void deleteScheduledMessage(ScheMsg message) {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ScheduledMessages");
//        ref.orderByChild("message").equalTo(message.getMessage()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    ds.getRef().removeValue(); // Remove the message
//                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(context, "Error deleting message", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}






import android.content.Context;
import android.text.TextUtils; // Import TextUtils
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Import NonNull
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;


import java.util.List;

public class ScheduledMessagesAdapterDraft extends RecyclerView.Adapter<ScheduledMessagesAdapterDraft.MessageViewHolder> {

    private static final String TAG = "ScheduledMsgAdapter";
    private final List<ScheMsg> scheduledMessagesList;
    private final Context context;
    private OnDeleteClickListener deleteClickListener; // Listener for delete button

    // Interface for delete button clicks
    public interface OnDeleteClickListener {
        void onDeleteClick(ScheMsg message);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }


    public ScheduledMessagesAdapterDraft(Context context, List<ScheMsg> scheduledMessagesList) {
        this.context = context;
        this.scheduledMessagesList = scheduledMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemdraft_scheduled_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ScheMsg message = scheduledMessagesList.get(position);

        // Bind Receiver Names
        holder.tvReceiver.setText(message.getReceiverInfoForDisplay());

        // Bind Scheduled Time
        holder.tvTime.setText("Scheduled for: " + message.getScheduledTimeFormatted());

        // Handle Message Content (Text or Image)
        String messageType = message.getMessageType();
        String contentForDisplay = message.getContentPreviewForDisplay(); // This gives content for text, Base64 for image

        if ("text".equals(messageType)) {
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.imgPreview.setVisibility(View.GONE);
            holder.tvMessage.setText(contentForDisplay); // Display the text content
            Log.d(TAG, "Binding text message draft: " + message.getMsgFirebaseId() + " Content: " + contentForDisplay);

        } else if ("image".equals(messageType)) {
            holder.tvMessage.setVisibility(View.GONE);
            holder.imgPreview.setVisibility(View.VISIBLE);

            // Load Base64 image using Glide
            if (!TextUtils.isEmpty(contentForDisplay)) {
                try {
                    // Load Base64 directly into ImageView
                    // Use android.util.Base64 for image data
                    Glide.with(context)
                            .asBitmap()
                            .load(Base64.decode(contentForDisplay, Base64.DEFAULT))
                            .placeholder(R.drawable.image_placeholder_background) // Optional placeholder while loading
                            .error(R.drawable.ic_broken_image) // Optional error drawable
                            .into(holder.imgPreview);
                    Log.d(TAG, "Binding image message draft: " + message.getMsgFirebaseId() + " Base64 length: " + contentForDisplay.length());

                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid Base64 string for image draft " + message.getMsgFirebaseId(), e);
                    holder.imgPreview.setImageResource(R.drawable.ic_error); // Show error icon
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image Base64 for draft " + message.getMsgFirebaseId(), e);
                    holder.imgPreview.setImageResource(R.drawable.ic_error); // Show error icon
                }
            } else {
                // Content is empty, show a placeholder or error
                holder.imgPreview.setImageResource(R.drawable.image_placeholder_background); // Show default placeholder
                Log.w(TAG, "Image content is empty for draft: " + message.getMsgFirebaseId());
            }

        } else {
            // Handle unexpected types - show text placeholder
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.imgPreview.setVisibility(View.GONE);
            holder.tvMessage.setText("[Unsupported Message Type: " + messageType + "]");
            Log.w(TAG, "Unsupported message type in draft: " + message.getMsgFirebaseId() + " Type: " + messageType);
        }

        // Bind Delete Button Click Listener
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(message);
            }
        });

        // Edit Button Listener (Placeholder - not implemented yet)
        holder.btnEdit.setOnClickListener(v -> {
            Toast.makeText(context, "Edit not implemented yet", Toast.LENGTH_SHORT).show();
            // TODO: Implement Edit functionality
        });
    }

    @Override
    public int getItemCount() {
        return scheduledMessagesList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvReceiver;
        TextView tvMessage; // For text content
        ImageView imgPreview; // For image content
        TextView tvTime;
        Button btnEdit;
        Button btnDelete;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReceiver = itemView.findViewById(R.id.tvReceiver);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            imgPreview = itemView.findViewById(R.id.imgPreview);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Method to update the adapter's list and refresh the UI
    public void setScheduledMessages(List<ScheMsg> messages) {
        this.scheduledMessagesList.clear();
        if (messages != null) {
            this.scheduledMessagesList.addAll(messages);
        }
        notifyDataSetChanged(); // Notify RecyclerView that data has changed
        Log.d(TAG, "Adapter data set updated with " + this.scheduledMessagesList.size() + " messages.");
    }
}