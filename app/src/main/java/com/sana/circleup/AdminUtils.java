package com.sana.circleup;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminUtils {

    // Method to enable or disable a user
    public void toggleUserAccountStatus(String email, boolean isBlocked) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        userRef.orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String userId = task.getResult().getChildren().iterator().next().getKey();
                userRef.child(userId).child("isBlocked").setValue(isBlocked);
            }
        });
    }
}

