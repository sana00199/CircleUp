package com.sana.circleup;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AdminViewGroupMembersActivity extends AppCompatActivity {

    private RecyclerView membersRecyclerView;
    private MemberAdapterAdmin memberAdapter;
    private ArrayList<Member> memberList;  // Change this to store Member objects
    private DatabaseReference groupMembersRef;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_group_members);

        membersRecyclerView = findViewById(R.id.members_recycler_view);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersRecyclerView.setHasFixedSize(true);
        membersRecyclerView.setItemAnimator(null);
        membersRecyclerView.setVisibility(View.VISIBLE);

        memberList = new ArrayList<>();

        // ✅ Get groupId and groupName correctly
        String groupId = getIntent().getStringExtra("groupId");
        String groupName = getIntent().getStringExtra("groupName");



        // Check if the groupName is null
        if (groupName == null || groupName.isEmpty()) {
            Toast.makeText(this, "Group name is missing", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if group name is null or empty
            return;
        }


        groupMembersRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("members");

        memberAdapter = new MemberAdapterAdmin(memberList, new MemberAdapterAdmin.OnMemberClickListener() {
            @Override
            public void onMemberClick(String memberId) {
                removeMember(memberId);
            }
        });

        membersRecyclerView.setAdapter(memberAdapter);

        loadGroupMembers(groupId);  // ✅ Pass the group name as an argument

    }

    private void loadGroupMembers(String groupId) {
        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference()
                .child("Groups").child(groupId).child("members");

        Log.d("FirebasePath", "Fetching from path: " + membersRef.toString());

        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberList.clear();
                Log.d("FirebaseData", "Snapshot exists: " + snapshot.exists());

                if (!snapshot.exists()) {
                    Toast.makeText(AdminViewGroupMembersActivity.this, "No members found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot memberSnap : snapshot.getChildren()) {
                    String userId = memberSnap.getKey();
                    String name = memberSnap.child("name").getValue(String.class);  // ✅ Correct way
                    String role = memberSnap.child("role").getValue(String.class);  // Optional

                    Log.d("FirebaseMembers", "UserID: " + userId + ", Name: " + name + ", Role: " + role);

                    if (userId != null && name != null) {
                        memberList.add(new Member(userId, name));  // ✅ Fix here
                    }
                }

                Log.d("FirebaseCheck", "Total Members Retrieved: " + memberList.size());
                memberAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error loading members: " + error.getMessage());
            }
        });
    }



    private void removeMember(String memberId) {
        groupMembersRef.child(memberId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AdminViewGroupMembersActivity.this, "Member removed", Toast.LENGTH_SHORT).show();
                // Remove the member from the list as well
                for (int i = 0; i < memberList.size(); i++) {
                    if (memberList.get(i).getId().equals(memberId)) {
                        memberList.remove(i);
                        break;
                    }
                }
                memberAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(AdminViewGroupMembersActivity.this, "Failed to remove member", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
