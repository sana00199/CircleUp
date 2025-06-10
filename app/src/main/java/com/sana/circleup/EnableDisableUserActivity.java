package com.sana.circleup;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class EnableDisableUserActivity extends AppCompatActivity {

    EditText emailEditText;
    Button submitBtn;
    RecyclerView recyclerView;
    DisableUserAdapter adapter;
    List<EnableDisableUserModelaAdmin> disabledUsersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_disable_user);

        emailEditText = findViewById(R.id.email_edittext);
        submitBtn = findViewById(R.id.submit_btn);
        recyclerView = findViewById(R.id.disabled_users_recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DisableUserAdapter(disabledUsersList, this::toggleUserStatus);
        recyclerView.setAdapter(adapter);

        // Fetch disabled users on activity start
        fetchDisabledUsers();

        // Search and toggle based on email input
        submitBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                searchAndToggleUser(email);
            } else {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAndToggleUser(String email) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userRef.orderByChild("email").equalTo(email);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // User found, toggle status
                DataSnapshot userSnapshot = task.getResult().getChildren().iterator().next();
                String userId = userSnapshot.getKey();
                boolean isBlocked = userSnapshot.child("isBlocked").getValue(Boolean.class);

                toggleUserStatus(userId, !isBlocked); // Toggle status
            } else {
                Toast.makeText(this, "No user found with this email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleUserStatus(String userId, boolean newStatus) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.child("isBlocked").setValue(newStatus).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String message = newStatus ? "User disabled" : "User enabled";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                fetchDisabledUsers(); // Refresh the list
            } else {
                Toast.makeText(this, "Failed to update user status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDisabledUsers() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.orderByChild("isBlocked").equalTo(true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                disabledUsersList.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    EnableDisableUserModelaAdmin user = snapshot.getValue(EnableDisableUserModelaAdmin.class);
                    user.setUserId(snapshot.getKey()); // Ensure userId is set
                    disabledUsersList.add(user);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to fetch disabled users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
