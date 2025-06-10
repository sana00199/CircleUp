package com.sana.circleup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupSettingsActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView groupProfileImage, deleteGroupProfileImage;
    private EditText groupNameEditText;
    private Button saveGroupNameButton;
    private RecyclerView groupMembersRecycler;
    private DatabaseReference groupsRef;
    private FirebaseAuth auth;
    private String groupId, currentUserId, adminId;
    private List<UserModel> groupMembersList = new ArrayList<>();
    private String base64GroupImage;

    private GroupMembersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);

        groupProfileImage = findViewById(R.id.group_profile_image);
        deleteGroupProfileImage = findViewById(R.id.delete_group_profile_image);
        groupNameEditText = findViewById(R.id.groupname_edittext);
        saveGroupNameButton = findViewById(R.id.save_group_name_button);
        groupMembersRecycler = findViewById(R.id.group_members_recycler);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        groupId = getIntent().getStringExtra("groupId");

        if (groupId == null || groupId.isEmpty()) {
            Toast.makeText(this, "Error: Group ID is missing", Toast.LENGTH_SHORT).show();
            Log.e("GroupSettingsActivity", "groupId is NULL or EMPTY");
            finish(); // Close the activity
            return;
        } else {
            Log.d("GroupSettingsActivity", "Received groupId: " + groupId);
        }

        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);

        groupMembersRecycler.setLayoutManager(new LinearLayoutManager(this));

        loadGroupDetails();

//        saveGroupNameButton.setOnClickListener(v -> saveGroupName());


        saveGroupNameButton.setOnClickListener(v -> {
            String newName = groupNameEditText.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save group name
            Task<Void> nameTask = groupsRef.child("groupName").setValue(newName);

            // Save group image only if updated
            Task<Void> imageTask;
            if (base64GroupImage != null) {
                imageTask = groupsRef.child("groupImage").setValue(base64GroupImage);
            } else {
                imageTask = Tasks.forResult(null); // No update needed
            }

            // Wait for both tasks to complete
            Tasks.whenAllComplete(nameTask, imageTask).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to chat screen
                } else {
                    Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show();
                }
            });
        });

        groupProfileImage.setOnClickListener(v -> openImagePicker());
        deleteGroupProfileImage.setOnClickListener(v -> deleteGroupImage());




        adapter = new GroupMembersAdapter(this, groupMembersList, adminId, currentUserId, user -> showMemberOptions(user));
        groupMembersRecycler.setAdapter(adapter);


    }

    private void loadGroupDetails() {
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                adminId = snapshot.child("admin").getValue(String.class);

                // Load group name
                String name = snapshot.child("groupName").getValue(String.class);
                groupNameEditText.setText(name);

                // Load group image
                String imageBase64 = snapshot.child("groupImage").getValue(String.class);
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    groupProfileImage.setImageBitmap(decodedBitmap);
                } else {
                    groupProfileImage.setImageResource(R.drawable.photocameradefault);
                }

                // Load group members
                groupMembersList.clear();
                for (DataSnapshot ds : snapshot.child("members").getChildren()) {
                    String userId = ds.getKey(); // Member userId

                    fetchUserDetails(userId); // Fetch from "Users" node
                }

                // Set up adapter AFTER fetching admin ID
                adapter = new GroupMembersAdapter(GroupSettingsActivity.this, groupMembersList, adminId, currentUserId, user -> showMemberOptions(user));
                groupMembersRecycler.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GroupSettings", "Error fetching group details: " + error.getMessage());
            }
        });
    }



    private void fetchUserDetails(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        Log.d("GroupSettings", "Fetching user details for ID: " + userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    String name = userSnapshot.child("username").getValue(String.class);
                    String profileImage = userSnapshot.child("profileImage").getValue(String.class);

                    Log.d("GroupSettings", "User found: " + name + ", Image: " + profileImage);

                    UserModel user = new UserModel(userId, name, profileImage);
                    groupMembersList.add(user);


                    // Ensure adapter updates UI
                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } else {
                    Log.e("GroupSettings", "User not found in Users node for ID: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GroupSettings", "Error fetching user details: " + error.getMessage());
            }
        });
    }





    private void saveGroupName() {
        String newName = groupNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        groupsRef.child("groupName").setValue(newName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Group name updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                groupProfileImage.setImageBitmap(bitmap);
                base64GroupImage = encodeToBase64(bitmap);
                saveGroupImageToFirebase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveGroupImageToFirebase() {
        groupsRef.child("groupImage").setValue(base64GroupImage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Group photo updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update group photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroupImage() {
        groupsRef.child("groupImage").removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                groupProfileImage.setImageResource(R.drawable.photocameradefault);
                Toast.makeText(this, "Group photo removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to remove group photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupMembers() {
        groupsRef.child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupMembersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String userId = ds.getKey();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String name = userSnapshot.child("username").getValue(String.class);
                                String profileImage = userSnapshot.child("profileImage").getValue(String.class);

                                UserModel user = new UserModel(userId, name, profileImage);
                                if (userId.equals(adminId)) {
                                    groupMembersList.add(0, user); // Admin at top
                                } else {
                                    groupMembersList.add(user);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void showMemberOptions(UserModel user) {
        // If the current user is NOT the admin, do nothing
        if (!currentUserId.equals(adminId)) return;

        // If the selected user is already the admin, do nothing
        if (user.getUserId().equals(adminId)) {
            Toast.makeText(this, "You cannot modify the admin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show options dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage Member")
                .setItems(new String[]{"Remove Member", "Make Admin"}, (dialog, which) -> {
                    if (which == 0) {
                        removeMember(user);
                    } else if (which == 1) {
                        makeAdmin(user);
                    }
                })
                .show();
    }

    private void removeMember(UserModel user) {
        groupsRef.child("members").child(user.getUserId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Member removed", Toast.LENGTH_SHORT).show();
                groupMembersList.remove(user);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to remove member", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void makeAdmin(UserModel user) {
        groupsRef.child("admin").setValue(user.getUserId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "New admin assigned", Toast.LENGTH_SHORT).show();
                adminId = user.getUserId();  // Update locally
                loadGroupMembers();  // Refresh members list
            } else {
                Toast.makeText(this, "Failed to assign admin", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
