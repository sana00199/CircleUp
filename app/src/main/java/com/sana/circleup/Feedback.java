package com.sana.circleup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

// --- Import Firebase Firestore ---
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;
// ----------------------------------

public class Feedback extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";

    private EditText editTextFeedback;
    private RatingBar ratingBar; // Optional
    private Button buttonSendFeedback;

    private FirebaseAuth mAuth; // To get current user
    private FirebaseFirestore db; // --- Add Firestore instance ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Log.d(TAG, "FeedbackActivity onCreate.");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // --- Initialize Firebase Firestore ---
        db = FirebaseFirestore.getInstance();
        // -------------------------------------

        // Setup Toolbar (assuming you added it to the XML as in the previous suggestion)
        Toolbar toolbar = findViewById(R.id.toolbar_feedback);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Find UI elements
        editTextFeedback = findViewById(R.id.editTextFeedback);
        ratingBar = findViewById(R.id.ratingBar); // Optional - Comment out if not used
        buttonSendFeedback = findViewById(R.id.buttonSendFeedback);

        // Set click listener for the Send button
        buttonSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedbackToFirestore(); // --- Call new Firestore method ---
            }
        });
    }

    // --- Handle Toolbar Back Button ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // --- End Optional Toolbar Back Button ---


    // --- New method to save feedback to Firestore ---
    private void sendFeedbackToFirestore() {
        String feedbackText = editTextFeedback.getText().toString().trim();
        float rating = ratingBar != null ? ratingBar.getRating() : -1.0f; // -1.0f if no rating bar

        // Check if feedback text is empty
        if (TextUtils.isEmpty(feedbackText)) {
            editTextFeedback.setError("Please enter your feedback");
            editTextFeedback.requestFocus();
            return;
        }

        // Get current user information
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = "N/A";
        String userEmail = "N/A";
        // Note: Getting username requires an extra read operation,
        // which adds complexity. For simplicity, we'll use UID and Email
        // as identifiers. You can fetch the username in the admin panel
        // based on the userId if needed.

        if (currentUser != null) {
            userId = currentUser.getUid();
            if (currentUser.getEmail() != null) {
                userEmail = currentUser.getEmail();
            }
        } else {
            // This case might happen if feedback is allowed without login,
            // or if the auth state is somehow not loaded correctly.
            Log.w(TAG, "No user is currently logged in for feedback.");
            Toast.makeText(this, "Cannot identify user. Sending anonymous feedback.", Toast.LENGTH_SHORT).show();
            // You might want to disable the feedback feature if login is required
        }


        // Create a Map to store feedback data
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("userId", userId);
        feedbackData.put("userEmail", userEmail); // Store email for easier lookup
        feedbackData.put("feedbackText", feedbackText);
        if (rating != -1.0f) {
            feedbackData.put("rating", (double) rating); // Firestore recommends Double for numbers
        }
        feedbackData.put("timestamp", FieldValue.serverTimestamp()); // Use server timestamp
        feedbackData.put("appVersion", BuildConfig.VERSION_NAME); // Add app version

        // Add the feedback data to a Firestore collection named "feedback"
        db.collection("feedback")
                .add(feedbackData) // add() generates a unique document ID
                .addOnSuccessListener(new OnSuccessListener<com.google.firebase.firestore.DocumentReference>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.DocumentReference documentReference) {
                        Log.d(TAG, "Feedback successfully written with ID: " + documentReference.getId());
                        Toast.makeText(Feedback.this, "Feedback sent!", Toast.LENGTH_SHORT).show();
                        // Clear the input fields after successful submission
                        editTextFeedback.setText("");
                        if (ratingBar != null) ratingBar.setRating(0);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding feedback document", e);
                        Toast.makeText(Feedback.this, "Failed to send feedback. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }
    // --- End Firestore method ---

    // Remove the old sendFeedback() method as it's replaced by sendFeedbackToFirestore()
    /*
    private void sendFeedback() {
        // ... (old email sending code) ...
    }
    */

    // Add this activity to AndroidManifest.xml if not already there
    // <activity android:name=".Feedback" android:screenOrientation="portrait"/> // Optional: set portrait
}