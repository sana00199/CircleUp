package com.sana.circleup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

//public class GoogleSignInHelper {
//
//    public static final int RC_SIGN_IN = 100;
//    private final Activity activity;
//    private final GoogleSignInClient mGoogleSignInClient;
//    private final FirebaseAuth mAuth;
//    private final DatabaseReference usersRef;
//    private boolean fromSignup;
//
//    public GoogleSignInHelper(Activity activity) {
//        this.activity = activity;
//        mAuth = FirebaseAuth.getInstance();
//        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
//
//        // Configure Google Sign-In
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(activity.getString(R.string.default_web_client_id)) // Ensure this matches Firebase Web Client ID
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
//    }
//
//
//    public void startGoogleSignIn(boolean fromSignup) {
//        this.fromSignup = fromSignup;
//        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
//            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
//        });
//    }
//
//    public void handleSignInResult(Intent data) {
//        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//        try {
//            GoogleSignInAccount account = task.getResult(ApiException.class);
//            if (account != null) {
//                firebaseAuthWithGoogle(account.getIdToken(), account);
//            }
//        } catch (ApiException e) {
//            Log.w("Google Sign-In", "Google sign-in failed", e);
//            Toast.makeText(activity, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }
//
//
//
//
//    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount account) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(activity, task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        if (user != null) {
//
//                            // Save authentication state in SharedPreferences
//                            SharedPreferences sharedPreferences = activity.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
//                            sharedPreferences.edit().putBoolean("isLoggedIn", true).apply();
////                            saveUserToDatabase(user, account);
//                            checkIfUserExists(activity, user.getUid(), account, fromSignup); //  Now 4 arguments
//
//                        }
//                    } else {
//                        Exception exception = task.getException();
//                        Log.e("GoogleAuth", "Authentication Failed", exception);
//                        Toast.makeText(activity, "Authentication Failed: " + (exception != null ? exception.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
//
//
//    private void checkIfUserExists(Context context, String userId, GoogleSignInAccount account, boolean fromSignup) {
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
//        userRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful() && task.getResult().exists()) {
//                // Account already exists
//                if (fromSignup) {
//                    // Already signed up, so go to MainActivity
//                    context.startActivity(new Intent(context, MainActivity.class));
//                    ((Activity) context).finish();
//                } else {
//                    // Coming from Login but account exists -> go to MainActivity
//                    context.startActivity(new Intent(context, MainActivity.class));
//                    ((Activity) context).finish();
//                }
//            } else {
//                // Account doesn't exist yet
//                if (fromSignup) {
//
////                    // Save new user to database
////                    saveUserToDatabase(FirebaseAuth.getInstance().getCurrentUser(), account);
////                    // First-time signup: go to SetProfileActivity
////                    context.startActivity(new Intent(context, Setting_profile.class));
////                    ((Activity) context).finish();
//
//                    // Don’t save anything here!
//                    Intent intent = new Intent(context, Setting_profile.class);
//                    intent.putExtra("isNewUser", true); //  Important
//                    context.startActivity(intent);
//                    ((Activity) context).finish();
//
//
//                } else {
//                    // Coming from login but no account exists → show toast
//                    Toast.makeText(context, "Please sign up first using Google before trying to login.", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//    }
//
//
//
//    private void saveUserToDatabase(FirebaseUser user, GoogleSignInAccount account) {
//        String userID = user.getUid();
//        HashMap<String, Object> userData = new HashMap<>();
//        userData.put("username", "");
//        userData.put("email", account.getEmail());
//        userData.put("uid", userID);
//        userData.put("status", "Hey there! I’m using CircleUp");
//        userData.put("role", "user");
//        userData.put("isBlocked", false);
//
//        usersRef.child(userID).updateChildren(userData).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Toast.makeText(activity, "Signed in as: " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
//                updateUI(user);
//            } else {
//                Toast.makeText(activity, "Failed to save user info to database.", Toast.LENGTH_LONG).show();
//            }
//        });
//    }
//
//
//
//    private void updateUI(FirebaseUser user) {
//        if (user != null) {
//            Intent intent = new Intent(activity, MainActivity.class);
//            activity.startActivity(intent);
//            activity.finish();
//        }
//    }
//}











// ... (Keep your existing imports) ...
import com.google.android.gms.auth.api.signin.GoogleSignInAccount; // Ensure this is imported
import com.google.android.gms.common.api.ApiException; // Ensure this is imported
import com.google.firebase.auth.AuthCredential; // Ensure this is imported
import com.google.firebase.auth.FirebaseUser; // Ensure this is imported
import com.google.firebase.auth.GoogleAuthProvider; // Ensure this is imported
import com.google.firebase.database.DataSnapshot; // Ensure this is imported
import com.google.firebase.database.DatabaseReference; // Ensure this is imported
import com.google.firebase.database.FirebaseDatabase; // Ensure this is imported
import com.google.firebase.database.ValueEventListener; // Ensure this is imported







public class GoogleSignInHelper {

    private static final String TAG = "GoogleSignInHelper"; // Added TAG
    public static final int RC_SIGN_IN = 100;
    private final Activity activity;
    private final GoogleSignInClient mGoogleSignInClient;
    private final FirebaseAuth mAuth;
    // usersRef is not needed here anymore if we notify the listener
    // private final DatabaseReference usersRef;
    private boolean fromSignup; // Still needed to track if signup flow initiated Google Login

    // --- New: Listener Interface and Member ---
    // Define this interface in the helper class so activities can implement it
    public interface GoogleSignInListener {
        void onGoogleAuthComplete(FirebaseUser user, boolean isNewUser); // Notify activity with the authenticated user and if they are new based on *database* check
        void onGoogleAuthFailed(Exception e); // Notify activity on failure
        void onGoogleAuthCancelled(); // Handle user cancelling the sign-in flow
    }

    private GoogleSignInListener listener;

    public void setGoogleSignInListener(GoogleSignInListener listener) {
        this.listener = listener;
    }
    // --- End New Listener ---


    public GoogleSignInHelper(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        // usersRef = FirebaseDatabase.getInstance().getReference().child("Users"); // No longer needed here

        // Configure Google Sign-In
        // Ensure R.string.default_web_client_id is correct and matches Firebase Web Client ID
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        Log.d(TAG, "GoogleSignInHelper initialized.");
    }


    /**
     * Starts the Google Sign-In Intent.
     * @param fromSignup True if the sign-in flow was initiated from the Signup activity.
     */
    public void startGoogleSignIn(boolean fromSignup) {
        Log.d(TAG, "Starting Google Sign-In Intent (fromSignup: " + fromSignup + ")");
        this.fromSignup = fromSignup;
        // Sign out Google account first before attempting new login (good practice, handles switching Google accounts)
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Log.d(TAG, "Previous Google account signed out.");
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    /**
     * Handles the result from the Google Sign-In Intent.
     * Should be called from the calling Activity's onActivityResult.
     * @param data The Intent data received in onActivityResult.
     */
    public void handleSignInResult(Intent data) {
        Log.d(TAG, "Handling Google Sign-In Result.");
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                Log.d(TAG, "GoogleSignInAccount obtained. ID Token available.");
                firebaseAuthWithGoogle(account.getIdToken()); // Pass only the ID token
            } else {
                // This case happens if the user cancelled or sign-in failed without throwing an ApiException
                Log.w(TAG, "Google sign-in account is null or ID Token is null.");
                if (listener != null) {
                    listener.onGoogleAuthCancelled(); // Notify listener if cancellation is likely (account null)
                    // Alternatively, you could treat this as a failure depending on desired behavior
                    // listener.onGoogleAuthFailed(new Exception("Google sign-in account or ID Token is null."));
                } else {
                    Log.w(TAG, "No listener set to handle Google sign-in result.");
                    // Optionally show a default Toast if no listener is set
                    // Toast.makeText(activity, "Google Sign-In Cancelled or failed.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ApiException e) {
            // ApiException indicates a failure during the Google Sign-In process
            Log.w(TAG, "Google sign-in failed with ApiException", e);
            if (listener != null) {
                listener.onGoogleAuthFailed(e); // Notify listener on failure
            } else {
                Log.w(TAG, "No listener set to handle Google sign-in ApiException.");
                // Optionally show a default Toast if no listener is set
                // Toast.makeText(activity, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) { // Catch any other unexpected errors
            Log.e(TAG, "Unexpected error handling Google sign-in result", e);
            if (listener != null) {
                listener.onGoogleAuthFailed(e);
            } else {
                Log.w(TAG, "No listener set to handle unexpected error in Google sign-in result.");
            }
        }
    }


    /**
     * Authenticates with Firebase using the Google ID Token.
     * @param idToken The Google ID Token obtained from GoogleSignInAccount.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "Authenticating with Firebase using Google ID Token.");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        // Use mAuth member variable to call signInWithCredential
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        // Firebase Auth success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Firebase Auth with Google successful. UID: " + user.getUid());
                            // Now check if this user exists in our Firebase Database /Users node
                            checkIfUserExistsInDatabase(user);
                        } else {
                            // Should not happen if task is successful, but handle defensively
                            Log.e(TAG, "Firebase Auth with Google successful, but user is null.");
                            if (listener != null) listener.onGoogleAuthFailed(new Exception("Firebase user is null after Google auth success"));
                            else Log.w(TAG, "No listener set for Firebase Auth success with null user.");
                        }
                    } else {
                        // Firebase Auth failed (e.g., invalid credential, network issues)
                        Exception exception = task.getException();
                        Log.e(TAG, "Firebase Auth with Google Failed", exception);
                        if (listener != null) {
                            listener.onGoogleAuthFailed(exception); // Notify listener on failure
                        } else {
                            Log.w(TAG, "No listener set for Firebase Auth failure.");
                            // Optionally show a default Toast if no listener is set
                            // Toast.makeText(activity, "Authentication Failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Checks if the authenticated Firebase user exists in the /Users database node.
     * Notifies the listener with the result.
     * @param user The successfully authenticated FirebaseUser.
     */
    private void checkIfUserExistsInDatabase(FirebaseUser user) {
        String userId = user.getUid();
        Log.d(TAG, "Checking if user exists in database: " + userId);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean existsInDb = snapshot.exists();
                Log.d(TAG, "User " + userId + " exists in database: " + existsInDb);

                // Determine if this user should go through the NEW user setup flow (Setting_profile).
                // This happens if the Google flow was initiated from Signup AND they DON'T exist in the DB.
                // If initiated from Login AND they DON'T exist, it's an error (they should signup first).
                // If initiated from either AND they DO exist, it's an existing user LOGIN.
                // The 'isNewUser' flag passed to the listener indicates if they should navigate to Setting_profile.
                boolean shouldGoToSetup = fromSignup && !existsInDb;

                if (listener != null) {
                    Log.d(TAG, "Notifying listener: onGoogleAuthComplete (user: " + userId + ", isNewUser: " + shouldGoToSetup + ")");
                    listener.onGoogleAuthComplete(user, shouldGoToSetup);
                } else {
                    Log.w(TAG, "No listener set to receive onGoogleAuthComplete callback.");
                    // If no listener, default behavior? (e.g., show success message, but where to go?)
                    // This helper MUST be used with a listener activity.
                }
                // All navigation logic is now removed from here and handled by the listener activity!
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check user existence in DB", error.toException());
                if (listener != null) {
                    listener.onGoogleAuthFailed(error.toException()); // Notify listener on failure
                } else {
                    Log.w(TAG, "No listener set for database check failure.");
                    // Optionally show a default Toast
                    // Toast.makeText(activity, "Database check failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}


