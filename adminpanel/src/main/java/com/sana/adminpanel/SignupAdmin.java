package com.sana.adminpanel;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupAdmin extends AppCompatActivity {

    private static final String TAG = "AdminSignupActivity";

    private EditText emailEditText, passwordEditText, usernameEditText;
    private Button signupButton;
    private TextView loginTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private ProgressDialog progressDialog;

    // Basic email and password validation patterns (can be stricter)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    // Basic password length, Firebase Auth enforces 6+ characters
    private static final Pattern PASSWORD_MIN_LENGTH_PATTERN = Pattern.compile(".{6,}");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_admin); // Link to your new layout

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users"); // Reference to main users node

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.admin_email_edittext);
        passwordEditText = findViewById(R.id.admin_password_edittext);
        usernameEditText = findViewById(R.id.admin_username_edittext);
        signupButton = findViewById(R.id.admin_signup_button);
        loginTextView = findViewById(R.id.admin_login_text);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Admin Account...");
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        signupButton.setOnClickListener(view -> registerAdmin());
        loginTextView.setOnClickListener(view -> {
            Log.d(TAG, "Login text clicked. Navigating to Admin Login Activity.");
            startActivity(new Intent(SignupAdmin.this, Login.class)); // Go to your adminpanel.Login
            finish(); // Finish AdminSignup
        });
    }

    private void registerAdmin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim(); // Get username

        if (!validateInputs(email, password, username)) {
            return;
        }

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "Firebase Auth admin user created. UID: " + userId);

                            // *** IMPORTANT: Save user data including role: "admin" ***
                            // This is the part that needs server-side validation if this page is accessible.
                            saveAdminToDatabase(userId, email, username);
                            // *** END IMPORTANT ***

                        } else {
                            progressDialog.dismiss();
                            Log.e(TAG, "Firebase Auth admin creation failed: " + task.getException().getMessage(), task.getException());
                            Toast.makeText(SignupAdmin.this, "Admin signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveAdminToDatabase(String userId, String email, String username) {
        Log.d(TAG, "Saving admin data to database for UID: " + userId);
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("uid", userId);
        adminData.put("email", email);
        adminData.put("username", username); // Save the entered username
        adminData.put("role", "admin"); // *** Crucially set the role here ***

        // Add placeholders for main app's key fields if needed
        adminData.put("publicKey", "");
        adminData.put("encryptedPrivateKey", "");
        adminData.put("encryptionSalt", "");
        // Do NOT add status, isBlocked, userState etc.

        usersRef.child(userId).setValue(adminData).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Log.d(TAG, "Admin data saved to database successfully.");
                Toast.makeText(SignupAdmin.this, "Admin account created successfully! You can now log in.", Toast.LENGTH_LONG).show();

                // Log out the newly created admin user immediately (they need to log in via the Login page)
                mAuth.signOut();
                Log.d(TAG, "Newly created admin user signed out.");

                // Redirect to the Admin Login page
                Intent intent = new Intent(SignupAdmin.this, Login.class); // Go to your adminpanel.Login
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear back stack
                startActivity(intent);
                finish(); // Finish AdminSignup activity

            } else {
                Log.e(TAG, "Failed to save admin data to database: " + task.getException().getMessage(), task.getException());
                Toast.makeText(SignupAdmin.this, "Failed to save admin data.", Toast.LENGTH_SHORT).show();
                // Optional: Consider deleting the Firebase Auth user if DB save fails?
                // mAuth.getCurrentUser().delete(); // Dangerous if user is already signed out
            }
        });
    }


    // Input validation helper for admin signup
    private boolean validateInputs(String email, String password, String username) {
        if (TextUtils.isEmpty(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }
        if (!PASSWORD_MIN_LENGTH_PATTERN.matcher(password).matches()) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }
        // You could add stricter password checks here if desired

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return false;
        }
        // You might add checks for minimum username length etc.

        return true; // All validations passed
    }
}