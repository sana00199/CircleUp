package com.sana.circleup.temporary_chat_room;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sana.circleup.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShowRoomActivity extends AppCompatActivity {

    private RecyclerView roomRecyclerView;
    private List<Room> roomList = new ArrayList<>();
    private RoomListAdapter roomAdapter;
    private DatabaseReference tempRoomsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_room);

        roomRecyclerView = findViewById(R.id.room_recycler_view);
        roomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomAdapter = new RoomListAdapter(this, roomList);
        roomRecyclerView.setAdapter(roomAdapter);

        fetchRooms();

    }
    private void fetchRooms() {
        tempRoomsRef = FirebaseDatabase.getInstance().getReference().child("temporaryChatRooms");

        tempRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String roomName = dataSnapshot.child("roomName").getValue(String.class);
                    String roomId = dataSnapshot.getKey();
                    String expiryTimeStr = dataSnapshot.child("expiryTime").getValue(String.class);

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        Date expiryDate = sdf.parse(expiryTimeStr);

                        if (expiryDate != null && expiryDate.after(new Date())) {
                            Room room = new Room(roomId, roomName, "Temporary Room");
                            roomList.add(room);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                roomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ShowRoomActivity", "Failed to load rooms: " + error.getMessage());
            }
        });
    }
}
