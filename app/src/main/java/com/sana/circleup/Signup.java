package com.sana.circleup;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;





import android.app.AlertDialog; // Import AlertDialog
import android.app.ProgressDialog;
import android.content.DialogInterface; // Import DialogInterface
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Keep import for Handler
import android.os.Looper; // Keep import for Looper
import android.text.Html; // Import Html (older versions) - Better to use HtmlCompat
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater; // Import LayoutInflater
import android.view.View; // Import View
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ScrollView; // Import ScrollView (if using custom layout)

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat; // *** Import HtmlCompat ***

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

// Ensure these imports are correct
import com.sana.circleup.Login;
import com.sana.circleup.Setting_profile;
import com.sana.circleup.GoogleSignInHelper;




public class Signup extends AppCompatActivity implements GoogleSignInHelper.GoogleSignInListener {

    private static final String TAG = "SignupActivity";

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;
    private TextView loginTextView;
    private ImageView googleImg;
    private CheckBox agreeCheckBox;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private ProgressDialog progressDialog;
    private GoogleSignInHelper googleSignInHelper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PASSWORD_STRENGTH_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeViews();
        setupClickListeners();

        googleSignInHelper = new GoogleSignInHelper(this);
        googleSignInHelper.setGoogleSignInListener(this);

        googleImg.setOnClickListener(v -> {
            Log.d(TAG, "Google image clicked on Signup screen. Starting Google Sign-In for potential new user.");
            googleSignInHelper.startGoogleSignIn(true);
        });

        ImageView eyePassword = findViewById(R.id.eye_password);
        ImageView eyeConfirmPassword = findViewById(R.id.eye_confirm_password);

        eyePassword.setOnClickListener(v -> togglePasswordVisibility(passwordEditText, eyePassword));
        eyeConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(confirmPasswordEditText, eyeConfirmPassword));
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        confirmPasswordEditText = findViewById(R.id.confirmpassword_edittext);
        signupButton = findViewById(R.id.signup_btn);
        loginTextView = findViewById(R.id.login);
        agreeCheckBox = findViewById(R.id.checkBox); // Match your Checkbox ID
        googleImg = findViewById(R.id.google_img);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);
    }

    private void togglePasswordVisibility(EditText editText, ImageView eyeIcon) {
        if (editText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeIcon.setImageResource(R.drawable.eye);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeIcon.setImageResource(R.drawable.eye_closed);
        }
        editText.setSelection(editText.getText().length());
    }


    private void setupClickListeners() {
        signupButton.setOnClickListener(view -> registerUser());
        loginTextView.setOnClickListener(view -> {
            Log.d(TAG, "Login text clicked. Navigating to Login Activity.");
            startActivity(new Intent(Signup.this, Login.class));
            finish();
        });

        agreeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showPrivacyPolicyDialog();
            } else {
                Log.d(TAG, "Privacy policy checkbox unchecked manually.");
            }
        });
    }

    // --- Modified Method: showPrivacyPolicyDialog ---
    private void showPrivacyPolicyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Privacy Policy");

        // Inflate custom layout for dialog content (Scrollable TextView)
        LayoutInflater inflater = getLayoutInflater();
        View policyView = inflater.inflate(R.layout.dialog_privacy_policy, null);
        TextView policyTextView = policyView.findViewById(R.id.policy_text_view);

        // *** FIX: Load HTML text using HtmlCompat.fromHtml ***
        String privacyPolicyHtmlText = getString(R.string.privacy_policy_text); // Get the HTML string from resources
        // Use HtmlCompat for modern Android versions
        // FROM_HTML_MODE_LEGACY handles basic HTML tags like h1, p, ul, li, strong
        CharSequence formattedText = HtmlCompat.fromHtml(privacyPolicyHtmlText, HtmlCompat.FROM_HTML_MODE_LEGACY);

        policyTextView.setText(formattedText); // Set the parsed HTML text to the TextView
        // *** END FIX ***


        builder.setView(policyView);

        builder.setPositiveButton("I Accept", (dialog, which) -> {
            dialog.dismiss();
            Log.d(TAG, "User accepted privacy policy via dialog.");
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            agreeCheckBox.setChecked(false); // Uncheck the box if user cancels
            dialog.dismiss();
            Log.d(TAG, "User cancelled privacy policy dialog. Checkbox unchecked.");
        });

        builder.setCancelable(false); // Dialog cannot be dismissed by tapping outside or back button

        AlertDialog dialog = builder.create();
        dialog.show();
        Log.d(TAG, "Privacy policy dialog shown.");
    }
    // --- End Modified Method ---


    // Handles Email/Password Registration
    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!validateInputs(email, password, confirmPassword)) {
            Log.d(TAG, "Input validation failed for signup.");
            return;
        }
        Log.d(TAG, "Input validation passed. Proceeding with Firebase signup.");

        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            Log.d(TAG, "Firebase Auth createUserWithEmailAndPassword success. UID: " + userId);
                            // --- NEW: Save initial user data, Send verification email, Log out, and Redirect ---
//                            saveUserToDatabase(userId, email); // Save initial data to DB first
//

                            // --- NEW: Save initial user data, Send verification email, Log out, and STAY on Signup ---
                            saveUserToDatabase(userId, user.getEmail()); // Save initial data to DB first

                            sendVerificationEmail(user); // Call the helper method

                            auth.signOut(); // Log out the newly created user immediately
                            Log.d(TAG, "User signed out immediately after signup for email verification.");

                            progressDialog.dismiss(); // Dismiss dialog

                            // *** MODIFIED: Inform user and STAY on Signup screen ***
                            Toast.makeText(Signup.this, "Account created. Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Staying on Signup screen after signup and verification email sent.");
                            // Do NOT start Login Activity automatically here.
                            // The user must manually go to Login (e.g., via loginTextView click).
                            // *** END MODIFIED ***



                        } else {
                            progressDialog.dismiss();
                            Log.e(TAG, "Firebase Auth createUserWithEmailAndPassword success, but user is null.");
                            Toast.makeText(Signup.this, "Registration successful but user data is null.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Log.e(TAG, "Firebase Auth createUserWithEmailAndPassword failed: " + task.getException().getMessage(), task.getException());
                        Toast.makeText(Signup.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    // --- NEW Helper Method: Send Verification Email ---
    private void sendVerificationEmail(FirebaseUser user) {
        if (user == null) {
            Log.w(TAG, "sendVerificationEmail called with null user.");
            return;
        }
        Log.d(TAG, "Attempting to send verification email to: " + user.getEmail());
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Verification email sent successfully to " + user.getEmail());
                    } else {
                        Log.e(TAG, "Failed to send verification email to " + user.getEmail(), task.getException());
                        // Inform user about the failure to send email? It's an error after signup.
                        // Maybe show a warning toast.
                        Toast.makeText(Signup.this, "Failed to send verification email. Please try logging in later.", Toast.LENGTH_LONG).show();
                    }
                });
    }
    // --- END NEW Helper Method ---




    // Input validation helper (Remains the same)
    private boolean validateInputs(String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            emailEditText.setError("Enter a valid Gmail address (e.g. example@example.com)");
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }
        if (!PASSWORD_STRENGTH_PATTERN.matcher(password).matches()) {
            passwordEditText.setError("Password must be 6+ characters, include uppercase, lowercase, digit, and special character (@#$%^&+=!)");
            passwordEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirm Password is required");
            confirmPasswordEditText.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        if (!agreeCheckBox.isChecked()) {
            Toast.makeText(Signup.this, "You must agree to the terms & conditions to sign up.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true; // All validations passed
    }

//     Saves initial user data to Firebase Database after successful Auth registration (Remains the same)
    private void saveUserToDatabase(String userId, String email) {
        Log.d(TAG, "Saving initial user data to database for UID: " + userId);
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", userId);
        userData.put("email", email);
        userData.put("username", "");
        userData.put("status", "Hey, I'm using CircleUp!");
        userData.put("role", "user");
        userData.put("isBlocked", false);

        Map<String, Object> userState = new HashMap<>();
        userState.put("date", "");
        userState.put("time", "");
        userState.put("state", "offline");
        userData.put("userState", userState);

        usersRef.child(userId).setValue(userData).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Log.d(TAG, "User data saved to database successfully.");
                Toast.makeText(Signup.this, "Account created successfully! Now, set up your profile and security passphrase.", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Signup.this, Setting_profile.class);
                intent.putExtra("isNewUser", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            } else {
                Log.e(TAG, "Failed to save user data to database: " + task.getException().getMessage(), task.getException());
                Toast.makeText(Signup.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }





    // --- Implement GoogleSignInListener Callbacks (Remain the same, with note about policy) ---
    @Override
    public void onGoogleAuthComplete(FirebaseUser user, boolean isNewUser) {
        Log.d(TAG, "Google Auth Complete callback received in Signup. User: " + user.getUid() + ", isNewUser: " + isNewUser);
        progressDialog.dismiss();

        if (user != null) {
            if (isNewUser) {
                // *** Reminder: Implement policy acceptance for Google Sign-up here if mandatory ***
                // showPrivacyPolicyDialog(); // Call dialog here if policy is mandatory for Google signups
                // ... Then, inside the dialog's positive button, proceed to Setting_profile ...

                Log.d(TAG, "Google Auth Complete (Signup): New user confirmed. Navigating to Setting_profile.");
                Intent intent = new Intent(Signup.this, Setting_profile.class);
                intent.putExtra("isNewUser", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.w(TAG, "Google Auth Complete (Signup): Existing user detected. Should navigate to Login.");
                Toast.makeText(Signup.this, "You already have an account. Please log in instead.", Toast.LENGTH_LONG).show();
                Intent loginIntent = new Intent(Signup.this, Login.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
            }
        } else {
            Log.e(TAG, "Google Auth Complete callback received null user.");
            Toast.makeText(Signup.this, "Google Sign-in failed: User data is null.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGoogleAuthFailed(Exception e) {
        Log.e(TAG, "Google Auth Failed callback received in Signup.", e);
        progressDialog.dismiss();
        Toast.makeText(Signup.this, "Google sign-up failed: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGoogleAuthCancelled() {
        Log.d(TAG, "Google Auth Cancelled callback received in Signup.");
        progressDialog.dismiss();
        Toast.makeText(Signup.this, "Google sign-up cancelled.", Toast.LENGTH_SHORT).show();
    }
    // --- End GoogleSignInListener Callbacks ---


    // Handles the result from startActivityForResult (Remains the same)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleSignInHelper.RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult: Received Google Sign-In result. Passing to helper.");
            progressDialog.setMessage("Authenticating with Google...");
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
            googleSignInHelper.handleSignInResult(data);
        }
    }
}






