//package com.sana.circleup;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class AdminViewAllUsersActivity extends AppCompatActivity {
//
//    private RecyclerView usersRecyclerView;
//    private UserAdapter usersAdapter;
//    private List<Users> usersList;
//    private DatabaseReference usersRef;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admin_view_all_users_db);
//
//        usersRecyclerView = findViewById(R.id.usersRecyclerView);
//        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        usersList = new ArrayList<>();
//        usersAdapter = new UserAdapter(usersList, this);
//        usersRecyclerView.setAdapter(usersAdapter);
//
//        usersRef = FirebaseDatabase.getInstance().getReference("Users");
//
//        loadUsers();
//    }
//
//    private void loadUsers() {
//        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
//
//        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                List<Users> usersList = new ArrayList<>();
//
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Users user = snapshot.getValue(Users.class);
//                    if (user != null) {
//                        user.setUserId(snapshot.getKey());  // ✅ Assign userId from Firebase key
//                        usersList.add(user);
//                    }
//                }
//
//                UserAdapter userAdapter = new UserAdapter(usersList, AdminViewAllUsersActivity.this);
//                usersRecyclerView.setAdapter(userAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e("Firebase", "Error fetching users", databaseError.toException());
//            }
//        });
//
//    }
//}
//


package com.sana.circleup;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;

public class AdminViewAllUsersActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private UserAdapter usersAdapter;
    private List<Users> usersList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_all_users_db);

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        usersList = new ArrayList<>();
        usersAdapter = new UserAdapter(usersList, this);
        usersRecyclerView.setAdapter(usersAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        loadUsers();
    }

    private void loadUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear(); // Clear previous data

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    Boolean isBlocked = snapshot.child("isBlocked").getValue(Boolean.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);
                    String profileImage = snapshot.child("profileImage").getValue(String.class);

                    Users user = new Users(userId, username, email, status, role, profileImage, isBlocked != null && isBlocked);
                    usersList.add(user);


                }

                usersAdapter.notifyDataSetChanged(); // Refresh RecyclerView properly
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching users", databaseError.toException());
            }
        });
    }
}
