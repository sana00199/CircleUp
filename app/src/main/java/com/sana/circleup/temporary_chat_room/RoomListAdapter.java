package com.sana.circleup.temporary_chat_room;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sana.circleup.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.RoomViewHolder> {
    private Context context;
    private List<Room> roomList;
    private DatabaseReference databaseReference;

    public RoomListAdapter(Context context, List<Room> roomList) {
        this.context = context;
        this.roomList = roomList;
        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");  // Reference to Firebase "rooms"
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.room_list_item, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);

        // Check if room ID is not null
        if (room.getId() != null) {
            // Fetch the room name from Firebase using room ID
            databaseReference.child(room.getId()).child("name").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String roomName = task.getResult().getValue(String.class);
                    if (roomName != null) {
                        holder.roomName.setText(roomName);  // Set the room name from Firebase
                    } else {
                        Log.e("RoomListAdapter", "Room name is null for roomId: " + room.getId());
                    }
                } else {
                    Log.e("RoomListAdapter", "Failed to fetch room name for roomId: " + room.getId(), task.getException());
                }
            });
        } else {
            Log.e("RoomListAdapter", "Room ID is null at position: " + position);
        }

        // Set the room status
        holder.roomStatus.setText("Temporary Room");
        holder.roomStatus.setTextColor(ContextCompat.getColor(context, R.color.teal_700));

        // Set default profile image
        holder.roomImage.setImageResource(R.drawable.temporary_chatroom); // Always set default image

//        // Optional: Display expiry time if available
//        if (room.getExpiryTime() != null && !room.getExpiryTime().isEmpty()) {
//            holder.roomExpiryTime.setVisibility(View.VISIBLE);
//            holder.roomExpiryTime.setText("Expires at: " + room.getExpiryTime());
//        } else {
//            holder.roomExpiryTime.setVisibility(View.GONE); // Hide expiry time if not available
//        }

        // Open room on item click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TemporaryRoomChatActivity.class);
            intent.putExtra("roomId", room.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, roomStatus, roomExpiryTime;
        CircleImageView roomImage;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.room_name);
            roomStatus = itemView.findViewById(R.id.room_status);
            roomExpiryTime = itemView.findViewById(R.id.room_expiry_time); // Ensure this is added to the view
            roomImage = itemView.findViewById(R.id.room_profile_image);
        }
    }
}
