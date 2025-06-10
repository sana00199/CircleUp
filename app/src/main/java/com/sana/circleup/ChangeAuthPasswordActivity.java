package com.sana.circleup; // Adjust package name

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog; // Use this AlertDialog
import android.app.ProgressDialog; // Use ProgressDialog for simple loading
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException; // Import specific exception

import java.util.regex.Pattern; // For password pattern


public class ChangeAuthPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangeAuthPasswordAct";

    private TextInputLayout layoutCurrentPassword, layoutNewPassword, layoutConfirmNewPassword;
    private TextInputEditText editTextCurrentPassword, editTextNewPassword, editTextConfirmNewPassword;
    private Button buttonChangeAuthPassword;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog; // For blocking progress

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Password strength pattern (Same as Signup)
    private static final Pattern PASSWORD_STRENGTH_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_auth_password); // Match layout file
        Log.d(TAG, "🟢 ChangeAuthPasswordActivity onCreate.");

        // Setup Toolbar as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_change_auth_password); // Match toolbar ID
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.change_auth_password_title); // Use string resource
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
            }
        } else {
            Log.w(TAG, "Toolbar not found in layout with ID toolbar_change_auth_password");
        }

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Check authentication
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated in ChangeAuthPasswordActivity! Redirecting to Login.");
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity(); // Redirect to Login activity
            return; // Stop onCreate execution
        }

        // Check if the user signed in with Email/Password provider
        boolean isEmailPasswordUser = false;
        if (currentUser.getProviderData() != null) {
            for (com.google.firebase.auth.UserInfo profile : currentUser.getProviderData()) {
                if (profile != null && profile.getProviderId() != null && profile.getProviderId().equals(EmailAuthProvider.PROVIDER_ID)) {
                    isEmailPasswordUser = true;
                    break;
                }
            }
        }

        if (!isEmailPasswordUser) {
            Log.w(TAG, "User is not signed in with Email/Password provider. Cannot change password.");
            showErrorAndDisableUI("You can only change the password for accounts signed in with Email/Password.");
            return; // Stop onCreate execution
        }


        // Initialize UI elements
        layoutCurrentPassword = findViewById(R.id.layout_current_password);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        layoutNewPassword = findViewById(R.id.layout_new_password);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        layoutConfirmNewPassword = findViewById(R.id.layout_confirm_new_password);
        editTextConfirmNewPassword = findViewById(R.id.editTextConfirmNewPassword);
        buttonChangeAuthPassword = findViewById(R.id.buttonChangeAuthPassword);
        progressBar = findViewById(R.id.progressBarChangeAuthPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Changing Password...");
        progressDialog.setCancelable(false);

        // Set click listener
        buttonChangeAuthPassword.setOnClickListener(v -> onChangePasswordButtonClick());

        Log.d(TAG, "✅ onCreate finished in ChangeAuthPasswordActivity");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Handle back button click on toolbar
        return true;
    }

    // Handle back button press - Add confirmation if inputs are not empty
    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(editTextCurrentPassword.getText()) ||
                !TextUtils.isEmpty(editTextNewPassword.getText()) ||
                !TextUtils.isEmpty(editTextConfirmNewPassword.getText())) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Are you sure you want to leave?")
                    .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }


    // --- Helper to set UI enabled/disabled state ---
    private void setUIEnabled(boolean enabled) {
        editTextCurrentPassword.setEnabled(enabled);
        editTextNewPassword.setEnabled(enabled);
        editTextConfirmNewPassword.setEnabled(enabled);
        buttonChangeAuthPassword.setEnabled(enabled);
        // Optional: Enable/disable password toggles if needed, though TextInputLayout usually handles this
        // layoutCurrentPassword.setPasswordVisibilityToggleEnabled(enabled);
        // layoutNewPassword.setPasswordVisibilityToggleEnabled(enabled);
        // layoutConfirmNewPassword.setPasswordVisibilityToggleEnabled(enabled);
    }

    // --- Helper to show critical error and disable UI ---
    private void showErrorAndDisableUI(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        setUIEnabled(false); // Disable all inputs and button
        progressBar.setVisibility(View.GONE); // Ensure progress bar is hidden
        // Optionally hide input layouts as well
        layoutCurrentPassword.setVisibility(View.GONE);
        layoutNewPassword.setVisibility(View.GONE);
        layoutConfirmNewPassword.setVisibility(View.GONE);
        buttonChangeAuthPassword.setVisibility(View.GONE);

        Log.e(TAG, "Critical error displayed and UI disabled: " + message);
    }


    // --- Handle Change Password Button Click ---
    private void onChangePasswordButtonClick() {
        String currentPassword = editTextCurrentPassword.getText().toString();
        String newPassword = editTextNewPassword.getText().toString();
        String confirmNewPassword = editTextConfirmNewPassword.getText().toString();

        // --- Validation ---
        if (TextUtils.isEmpty(currentPassword)) {
            editTextCurrentPassword.setError("Current password is required");
            editTextCurrentPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            editTextNewPassword.setError("New password is required");
            editTextNewPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmNewPassword)) {
            editTextConfirmNewPassword.setError("Confirm new password is required");
            editTextConfirmNewPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            editTextConfirmNewPassword.setError("New passwords do not match");
            editTextConfirmNewPassword.requestFocus();
            return;
        }
        // Add password strength check for new password
        if (!PASSWORD_STRENGTH_PATTERN.matcher(newPassword).matches()) {
            editTextNewPassword.setError("Password must be 6+ characters, include uppercase, lowercase, digit, and special character (@#$%^&+=!)");
            editTextNewPassword.requestFocus();
            return;
        }
        // Ensure new password is different from current password (optional but good UX)
        if (currentPassword.equals(newPassword)) {
            editTextNewPassword.setError("New password must be different from current password");
            editTextNewPassword.requestFocus();
            return;
        }


        // Disable input and button, show progress dialog
        setUIEnabled(false);
        progressDialog.show();

        Log.d(TAG, "Change Account Password button clicked. Attempting re-authentication.");

        // Re-authenticate the user with their current password
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated successfully.");

                            // Now update the password
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> updateTask) {
                                            progressDialog.dismiss(); // Dismiss main dialog
                                            setUIEnabled(true); // Re-enable UI

                                            if (updateTask.isSuccessful()) {
                                                Log.d(TAG, "Account password updated successfully.");
                                                Toast.makeText(ChangeAuthPasswordActivity.this, "Account password updated successfully!", Toast.LENGTH_LONG).show();
                                                // Clear fields on success
                                                editTextCurrentPassword.setText("");
                                                editTextNewPassword.setText("");
                                                editTextConfirmNewPassword.setText("");
                                                // Optionally finish the activity
                                                finish();
                                            } else {
                                                Log.e(TAG, "Failed to update account password.", updateTask.getException());
                                                Toast.makeText(ChangeAuthPasswordActivity.this, "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                // Clear new password fields on failure
                                                editTextNewPassword.setText("");
                                                editTextConfirmNewPassword.setText("");
                                            }
                                        }
                                    });
                        } else {
                            progressDialog.dismiss(); // Dismiss main dialog
                            setUIEnabled(true); // Re-enable UI
                            Log.e(TAG, "Re-authentication failed.", task.getException());

                            String errorMessage = "Authentication failed.";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Incorrect current password.";
                                editTextCurrentPassword.setError(errorMessage); // Set error on the current password field
                                editTextCurrentPassword.requestFocus();
                                editTextCurrentPassword.setText(""); // Clear the incorrect password
                                editTextNewPassword.setText(""); // Clear new passwords too
                                editTextConfirmNewPassword.setText("");
                            } else {
                                errorMessage = "Failed to re-authenticate. Please try again.";
                                // Clear all fields for general re-auth failure
                                editTextCurrentPassword.setText("");
                                editTextNewPassword.setText("");
                                editTextConfirmNewPassword.setText("");
                            }
                            Toast.makeText(ChangeAuthPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    // --- Helper Navigation Method to go back to Login ---
    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity from ChangeAuthPasswordActivity.");
        Intent loginIntent = new Intent(ChangeAuthPasswordActivity.this, Login.class); // Ensure Login activity exists
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish(); // Finish this activity
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss progress dialog to prevent window leaks
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Log.d(TAG, "🔴 ChangeAuthPasswordActivity onDestroy called.");
    }
}