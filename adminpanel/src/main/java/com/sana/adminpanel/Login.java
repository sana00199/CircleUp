package com.sana.adminpanel; // Ensure this is the correct package name for your admin module

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Import necessary classes from your main app if they are used
// Example: com.sana.circleup.MainActivity; // Although admin should not go here

import java.util.regex.Pattern;

public class Login extends AppCompatActivity { // Renamed from Login to AdminLogin if you want distinct names, but keeping Login as per your code

    private static final String TAG = "AdminLogin"; // Changed TAG for clarity

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signupTV;
    private CheckBox rememberMeCheckbox;
    // No Google Sign-In ImageView needed for Admin login

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef; // Reference to the main app's "Users" node
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;

    private static final String PREFS_NAME = "AdminPrefs";
    private static final String PREF_EMAIL = "AdminEmail"; // Key to save/load admin email

    // Basic email and password validation patterns (can be stricter if needed)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PASSWORD_MIN_LENGTH_PATTERN = Pattern.compile(".{6,}"); // Firebase Auth minimum is 6


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started."); // Log start of onCreate

        setContentView(R.layout.activity_login); // Ensure this points to your admin_login.xml layout

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users"); // Reference to the main app's Users node

        // Initialize UI elements
        initializeViews();

        FirebaseApp.initializeApp(this);


        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved email if "Remember Me" was checked last time
        loadSavedEmail();

        // Set up login button click listener
        setupListeners(); // Combined click listeners

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false); // Prevent dismissing by tapping outside


        signupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignupAdmin.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Checking authentication state.");
        // Check if a user is already logged in from a previous session (e.g., app restart with "Remember Me")
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "onStart: User is already authenticated. Checking role for UID: " + currentUser.getUid());
            // If authenticated, immediately check their role without showing the login form initially
            checkUserRoleAndNavigate(currentUser); // Proceed to check role and navigate
        } else {
            Log.d(TAG, "onStart: No user is authenticated. Showing login form.");
            // No authenticated user, the login form is already visible
        }
    }


    private void initializeViews() {
        emailEditText = findViewById(R.id.email_edittext); // Match your layout ID
        passwordEditText = findViewById(R.id.password_edittext); // Match your layout ID
        loginButton = findViewById(R.id.welcome_login_btn); // Match your layout ID
        rememberMeCheckbox = findViewById(R.id.checkBoxrememberme); // Match your layout ID
        signupTV = findViewById(R.id.signup_tv);
        // Add other UI elements if you have them in your admin login layout (e.g., signup text, forget password)
        // For admin panel, maybe you don't have signup/forget password links on the main login page?
        // If you added an AdminSignupActivity, you might need a link here.
        // Example: TextView adminSignupLink = findViewById(R.id.admin_signup_link);
    }

    private void setupListeners() {
        // Set up login button click listener
        loginButton.setOnClickListener(v -> loginAdmin());

        // Handle "Remember Me" checkbox state change
        rememberMeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // The actual saving happens in saveEmailIfRemembered during successful login
            // This listener could be used for other UI effects if needed
            Log.d(TAG, "RememberMe checkbox state changed to: " + isChecked);
        });

        // If you have a link to AdminSignupActivity:
        // Example:
        // TextView adminSignupLink = findViewById(R.id.admin_signup_link);
        // if (adminSignupLink != null) {
        //     adminSignupLink.setOnClickListener(v -> {
        //         startActivity(new Intent(AdminLogin.this, AdminSignupActivity.class));
        //     });
        // }
    }

    private void loadSavedEmail() {
        String savedEmail = sharedPreferences.getString(PREF_EMAIL, "");
        boolean isRemembered = sharedPreferences.getBoolean("RememberMeChecked", false); // Optional: save checkbox state
        if (!TextUtils.isEmpty(savedEmail) && isRemembered) {
            emailEditText.setText(savedEmail);
            rememberMeCheckbox.setChecked(true);
            Log.d(TAG, "Loaded saved email.");
        } else {
            rememberMeCheckbox.setChecked(false); // Ensure unchecked if no saved email or not remembered
            Log.d(TAG, "No saved email found or RememberMe not checked.");
        }
    }

    private void saveEmailIfRemembered(String email, boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isChecked) {
            editor.putString(PREF_EMAIL, email);
            editor.putBoolean("RememberMeChecked", true); // Optional: save checkbox state
            Log.d(TAG, "Saved email to preferences because RememberMe is checked.");
        } else {
            editor.remove(PREF_EMAIL);
            editor.putBoolean("RememberMeChecked", false); // Optional: save checkbox state
            Log.d(TAG, "Removed email from preferences because RememberMe is unchecked.");
        }
        editor.apply();
    }


    // Handles Email/Password Login attempt
    private void loginAdmin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        boolean isRememberMeChecked = rememberMeCheckbox.isChecked(); // Capture checkbox state

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            emailEditText.requestFocus();
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        // Basic length check, Firebase auth does the rest
        if (!PASSWORD_MIN_LENGTH_PATTERN.matcher(password).matches()) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inputs are valid, attempt sign-in
        loginButton.setEnabled(false); // Disable button during process
        progressDialog.setMessage("Signing in...");
        progressDialog.show();
        Log.d(TAG, "Attempting Firebase Auth sign-in for: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true); // Re-enable button

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Firebase Auth Sign-in successful. UID: " + user.getUid());
                            // Authentication succeeded, now check their role in the database
                            // Pass the captured checkbox state here
                            checkUserRoleAndNavigate(user); // Proceed to check role and navigate
                            saveEmailIfRemembered(email, isRememberMeChecked); // Save email *only* if auth was successful and checkbox is checked
                        } else {
                            // This case is rare but handle defensively
                            progressDialog.dismiss(); // Dismiss dialog
                            Log.e(TAG, "Firebase Auth Sign-in successful, but user object is null.");
                            Toast.makeText(this, "Login successful, but user data is null. Please try again.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut(); // Log out the potentially incomplete session
                            // Do not save email on this type of success
                            saveEmailIfRemembered(email, false); // Ensure email is NOT saved if login results in null user
                        }
                    } else {
                        // Firebase Auth Sign-in failed
                        progressDialog.dismiss(); // Dismiss dialog
                        Log.e(TAG, "Firebase Auth Sign-in failed: " + task.getException().getMessage(), task.getException());
                        Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        // Do not save email on auth failure
                        saveEmailIfRemembered(email, false); // Ensure email is NOT saved on login failure
                    }
                });
    }

    /**
     * Checks the authenticated Firebase User's role in the database and navigates accordingly.
     * This method is called after a successful Firebase Auth login (either from login form or onStart).
     *
     * @param authenticatedUser The FirebaseUser object returned by FirebaseAuth.getCurrentUser().
     */
    private void checkUserRoleAndNavigate(FirebaseUser authenticatedUser) {
        // Ensure dialog is showing while checking role from DB, if it wasn't already
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage("Checking role...");
            progressDialog.show();
        }
        Log.d(TAG, "checkUserRoleAndNavigate called for user: " + authenticatedUser.getUid());

        String userId = authenticatedUser.getUid();

        // Fetch the user's data from the "Users" node in the main app's database
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss(); // Dismiss dialog once DB read is complete

                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class); // Get the role value

                    if (role != null && role.equals("admin")) { // Check if role exists and is exactly "admin"
                        Log.d(TAG, "User is Admin. Navigating to MainAdminActivity.");
                        Toast.makeText(Login.this, "Admin login successful!", Toast.LENGTH_SHORT).show();
                        sendUserToAdminMain(); // Navigate to Admin Dashboard
                    } else {
                        // User exists in DB, but role is not "admin" or is missing/empty string
                        Log.w(TAG, "User authenticated but role is NOT 'admin' or missing. Role found: " + role + ". Denying access.");
                        Toast.makeText(Login.this, "Access denied: Not an admin account.", Toast.LENGTH_LONG).show();
                        mAuth.signOut(); // Log out non-admin user from admin panel auth state
                        // Do not save email on non-admin access
                        saveEmailIfRemembered(authenticatedUser.getEmail(), false); // Ensure email is NOT saved for non-admin
                        // Stay on login screen
                    }
                } else {
                    // User authenticated with Firebase Auth but does not have data in the /Users node (Critical error for app structure)
                    progressDialog.dismiss(); // Dismiss dialog
                    Log.e(TAG, "Authenticated user data not found in DB for UID: " + userId + ". Logging out.");
                    Toast.makeText(Login.this, "User data missing. Access denied. Please contact support.", Toast.LENGTH_LONG).show();
                    mAuth.signOut(); // Log out the user
                    // Do not save email on missing data
                    saveEmailIfRemembered(authenticatedUser.getEmail(), false); // Ensure email is NOT saved
                    // Stay on login screen
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss(); // Dismiss dialog on error
                Log.e(TAG, "Database query failed during role check for UID: " + userId, error.toException());
                Toast.makeText(Login.this, "Failed to check user role: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                // On DB error during role check, assume temporary issue but deny access
                mAuth.signOut(); // Log out the user to be safe
                saveEmailIfRemembered(authenticatedUser.getEmail(), false); // Ensure email is NOT saved
                // Stay on login screen
            }
        });
    }

    // Helper method to navigate to the main Admin Activity
    private void sendUserToAdminMain() {
        Log.d(TAG, "Navigating to MainAdminActivity.");
        Intent mainAdminIntent = new Intent(Login.this, MainAdminActivity.class); // Ensure MainAdminActivity exists
        mainAdminIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(mainAdminIntent);
        finish(); // Finish Login activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: AdminLogin activity finished.");
        // Dismiss progress dialog to prevent leaks
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}