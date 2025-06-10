package com.sana.circleup;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<Users> usersList;
    private Context context;

    public UserAdapter(List<Users> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.showing_user_items_dbadmin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Users user = usersList.get(position);
        holder.usernameTextView.setText(user.getUsername());
        holder.emailTextView.setText(user.getEmail());
        holder.statusTextView.setText(user.getStatus());
        holder.roleTextView.setText(user.getRole());

        // Decode profile image if available
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Bitmap decodedImage = decodeBase64(user.getProfileImage());
            if (decodedImage != null) {
                holder.profileImage.setImageBitmap(decodedImage);
            }
        }

        // Update button text and color based on blocked status
        if (user.isBlocked()) {
            holder.disableButton.setText("Enable Account");
            holder.disableButton.setBackgroundColor(0xFF1D491E);
        } else {
            holder.disableButton.setText("Disable Account");
            holder.disableButton.setBackgroundColor(0xFFFF0000);
        }

        // Set button listeners
        holder.disableButton.setOnClickListener(v -> toggleUserStatus(user, holder));
        holder.deleteButton.setOnClickListener(v -> deleteUser(user, holder));

        holder.itemView.setOnClickListener(v -> {
            showMakeAdminDialog(user);
        });


    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, emailTextView, statusTextView, roleTextView;
        CircleImageView profileImage;
        Button disableButton, deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            emailTextView = itemView.findViewById(R.id.email_text_view);
            statusTextView = itemView.findViewById(R.id.status_text_view);
            roleTextView = itemView.findViewById(R.id.role_text_view);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            disableButton = itemView.findViewById(R.id.disable_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

//    private void toggleUserStatus(Users user) {
//        if (user.getUserId() == null) {
//            Toast.makeText(context, "Error: User ID is null", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        boolean newStatus = !user.isBlocked();
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUserId());
//
//        userRef.child("isBlocked").setValue(newStatus)
//                .addOnSuccessListener(aVoid -> {
//                    user.setBlocked(newStatus);
//                    Toast.makeText(context, "User " + (newStatus ? "disabled" : "enabled"), Toast.LENGTH_SHORT).show();
//                    notifyItemChanged(usersList.indexOf(user));
//                })
//                .addOnFailureListener(e -> Toast.makeText(context, "Error updating user", Toast.LENGTH_SHORT).show());
//    }


//    private void showMakeAdminDialog(Users user) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Make Admin")
//                .setMessage("Make \"" + user.getUsername() + "\" an admin of CircleUp?")
//                .setPositiveButton("Yes", (dialog, which) -> makeUserAdmin(user))
//                .setNegativeButton("No", null)
//                .show();
//    }

    private void showMakeAdminDialog(Users user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Check if the user is already an admin
        boolean isCurrentlyAdmin = "admin".equals(user.getRole());

        String message = isCurrentlyAdmin
                ? "Remove \"" + user.getUsername() + "\" as an admin?"
                : "Make \"" + user.getUsername() + "\" an admin of CircleUp?";

        String positiveButtonText = isCurrentlyAdmin ? "Remove Admin" : "Make Admin";

        builder.setTitle("Admin Role")
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialog, which) -> toggleAdminRole(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleAdminRole(Users user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUserId());

        // Toggle role between "admin" and "user"
        String newRole = "admin".equals(user.getRole()) ? "user" : "admin";

        userRef.child("role").setValue(newRole).addOnSuccessListener(aVoid -> {
            user.setRole(newRole);  // Update local object
            Toast.makeText(context, user.getUsername() + " is now a " + newRole, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to update role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void makeUserAdmin(Users user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUserId());
        userRef.child("role").setValue("admin").addOnSuccessListener(aVoid -> {
            Toast.makeText(context, user.getUsername() + " is now an admin!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to update role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void toggleUserStatus(Users user, UserViewHolder holder) {
        if (user.getUserId() == null) {
            Toast.makeText(context, "User ID is null!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean newStatus = !user.isBlocked();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUserId());

        userRef.child("isBlocked").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    user.setBlocked(newStatus);
                    notifyItemChanged(usersList.indexOf(user));
                    holder.disableButton.setText(newStatus ? "Enable Account" : "Disable Account");
                    holder.disableButton.setBackgroundColor(newStatus ? 0xFF1D491E : 0xFFFF0000);
                    Toast.makeText(context, "User " + (newStatus ? "disabled" : "enabled"), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error updating user", Toast.LENGTH_SHORT).show());
    }

//    private void deleteUser(Users user) {
//        if (user.getUserId() == null) {
//            Toast.makeText(context, "User ID is null!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        new AlertDialog.Builder(context)
//                .setTitle("Delete Account")
//                .setMessage("Are you sure you want to permanently delete this account?")
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUserId());
//                    userRef.removeValue()
//                            .addOnSuccessListener(aVoid -> deleteUserFromAuth(user.getUserId()))
//                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show());
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }


    private void deleteUser(Users user, UserViewHolder holder) {
        if (user.getUserId() == null) {
            Toast.makeText(context, "Error: User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(user.getUserId())) {
            Toast.makeText(context, "You cannot delete your own account!", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete this account?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUserId());

                    userRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                deleteUserFromAuth(user.getUserId());
                                int position = usersList.indexOf(user);
                                usersList.remove(user);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUserFromAuth(String userId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String currentAdminId = firebaseAuth.getUid();

        if (currentAdminId != null && currentAdminId.equals(userId)) {
            Toast.makeText(context, "You cannot delete your own account!", Toast.LENGTH_LONG).show();
            return;
        }

        firebaseAuth.getCurrentUser().delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserAdapter", "User authentication deleted: " + userId);
                    Toast.makeText(context, "User authentication deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthException) {
                        Toast.makeText(context, "Reauthentication required before deleting user", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Failed to delete user authentication", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap decodeBase64(String encodedImage) {
        try {
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("UserAdapter", "Error decoding profile image", e);
            return null;
        }
    }
}


