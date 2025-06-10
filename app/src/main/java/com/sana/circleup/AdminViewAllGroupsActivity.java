package com.sana.circleup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AdminViewAllGroupsActivity extends AppCompatActivity {

    private RecyclerView groupsRecyclerView;
    private DatabaseReference groupsRef;
    private ArrayList<String> groupList;
    private GroupAdapterAdmin groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_all_groups);

        groupsRecyclerView = findViewById(R.id.groups_recycler_view);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");
        groupList = new ArrayList<>();
        groupAdapter = new GroupAdapterAdmin(groupList, this::showGroupOptions);
        groupsRecyclerView.setAdapter(groupAdapter);

        loadGroups();
    }

    private void loadGroups() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();  // Clear the list before adding new data
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    // Get the group details under the UID (groupId)
                    String groupName = groupSnapshot.child("groupName").getValue(String.class);
                    String groupId = groupSnapshot.getKey();  // Fetch the groupId (unique identifier of the group)

                    if (groupName != null && groupId != null) {
                        // Create a custom object or pass the data as needed
                        groupList.add(groupName + ":" + groupId);  // Add both groupName and groupId to the list
                    }
                }
                groupAdapter.notifyDataSetChanged();  // Notify the adapter to refresh the view
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminViewAllGroupsActivity.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGroupOptions(String groupNameAndId) {
        // Split the groupName and groupId
        String[] parts = groupNameAndId.split(":");
        String groupName = parts[0];
        String groupId = parts[1];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(groupName)
                .setItems(new String[]{"View Members", "Delete Group"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, AdminViewGroupMembersActivity.class);
                        intent.putExtra("groupId", groupId);  // ✅ Pass group ID
                        intent.putExtra("groupName", groupName);  // ✅ Pass group name
                        startActivity(intent);


                    } else if (which == 1) {
                        deleteGroup(groupId);  // Pass groupId to delete
                    }
                })
                .show();
    }

    private void deleteGroup(String groupId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete this group?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the group using the groupId
                    groupsRef.child(groupId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete group", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }






}
