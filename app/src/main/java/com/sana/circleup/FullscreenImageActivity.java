package com.sana.circleup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager; // Still needed for package manager constants if used elsewhere
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
// import android.provider.Settings; // REMOVE THIS IMPORT
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
// import androidx.appcompat.app.AlertDialog; // REMOVE THIS IMPORT
import androidx.appcompat.app.AppCompatActivity;
// import androidx.core.app.ActivityCompat; // REMOVE THIS IMPORT
// import androidx.core.content.ContextCompat; // REMOVE THIS IMPORT

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects; // Still needed

// Import the correct Image View class
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.ImageSource;

// Or if using PhotoView:
// import com.github.chrisbanes.photoview.PhotoView;


public class FullscreenImageActivity extends AppCompatActivity {

    private static final String TAG = "FullscreenImageAct";

    // Change this based on which library you are using:
    private SubsamplingScaleImageView fullImageView;
    // OR private PhotoView fullImageView;


    private ImageButton closeButton;
    ImageButton btnEdit;
    private ImageButton btnSave;

    // Store the decoded Bitmap
    private Bitmap displayedBitmap;

    // *** REMOVE THIS CONSTANT ***
    // private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    // *** END REMOVE ***


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        // Change this findViewById based on which library you are using:
        fullImageView = findViewById(R.id.fullscreen_image);

        closeButton = findViewById(R.id.closeButton);
        btnEdit = findViewById(R.id.btn_edit);
        btnSave = findViewById(R.id.btn_save_image);


        // *** Add null checks for essential views early ***
        if (fullImageView == null || closeButton == null || btnEdit == null || btnSave == null) {
            Log.e(TAG, "CRITICAL ERROR: One or more essential views not found (Image, Close, Edit, Save)!");
            Toast.makeText(this, "Display error.", Toast.LENGTH_SHORT).show();
            finish();
            return; // Stop execution
        }


        String base64Image = getIntent().getStringExtra("profileImage");
        boolean hideEditButton = getIntent().getBooleanExtra("hideEditButton", false);


        // *** MODIFIED: Logic to handle image loading and store Bitmap ***
        // Decode the bitmap first and store it
        if (base64Image != null && !base64Image.isEmpty()) {
            Log.d(TAG, "Attempting to decode Base64 image string.");
            try {
                displayedBitmap = decodeBase64ToBitmap(base64Image); // Store in member variable
                if (displayedBitmap == null) {
                    Log.w(TAG, "decodeBase64ToBitmap returned null for a non-empty string.");
                } else {
                    Log.d(TAG, "Base64 string successfully decoded to Bitmap. Dimensions: " + displayedBitmap.getWidth() + "x" + displayedBitmap.getHeight());
                }
            } catch (Exception e) { // Catch decoding errors
                Log.e(TAG, "Error during Base64 decoding.", e);
                displayedBitmap = null; // Ensure bitmap is null on error
            }
        } else {
            Log.w(TAG, "Base64 image string is null or empty.");
            displayedBitmap = null; // Ensure bitmap is null
        }

        // Set the image on the view or set a default if bitmap is null
        if (displayedBitmap != null) {
            // *** Valid image bitmap is available ***
            if (fullImageView != null) { // Safety check
                // Set the image using the appropriate method for PhotoView or SubsamplingScaleImageView
                // If using SubsamplingScaleImageView:
                fullImageView.setImage(ImageSource.bitmap(displayedBitmap));
                // If using PhotoView:
                // fullImageView.setImageBitmap(displayedBitmap);

                fullImageView.setVisibility(View.VISIBLE); // Ensure image view is visible
                Log.d(TAG, "Valid image set on view.");
            }

        } else {
            // *** Image data missing, empty, or failed to decode to a valid bitmap ***
            Log.d(TAG, "No valid image bitmap available. Setting default/error image.");
            if (fullImageView != null) { // Safety check
                fullImageView.setVisibility(View.VISIBLE); // Ensure image view is visible even for default
                try {
                    // *** Set a default image resource using the appropriate method ***
                    // If using SubsamplingScaleImageView:
                    fullImageView.setImage(ImageSource.resource(R.drawable.default_profile_img)); // <-- Use your default profile image resource
                    // If using PhotoView:
                    // fullImageView.setImageResource(R.drawable.default_profile_img);

                    Log.d(TAG, "Set default image resource on view.");
                } catch (Exception resourceError) {
                    // This catch handles potential errors in setting the resource itself (e.g., resource ID invalid)
                    Log.e(TAG, "Failed to set default image resource.", resourceError);
                    // If setting default fails, hide the view completely as a last resort
                    fullImageView.setVisibility(View.GONE);
                    Toast.makeText(this, "Image not available.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // fullImageView was null from the start, already handled by the initial check
            }
        }
        // *** END MODIFIED: Image loading logic and storing Bitmap ***


        // Close button functionality remains the same
        closeButton.setOnClickListener(v -> finish());

        // *** MODIFIED: Control visibility of Edit and Save buttons ***
        if (hideEditButton) {
            // If edit button should be hidden (viewing someone else's profile from chat/profile list)
            Log.d(TAG, "Hide edit button, show save button.");
            btnEdit.setVisibility(View.GONE); // Hide Edit
            btnEdit.setEnabled(false);

            // *** Show the Save button ***
            btnSave.setVisibility(View.VISIBLE); // Show Save
            // Save button is only enabled if there's a bitmap to save
            btnSave.setEnabled(displayedBitmap != null); // Enable Save ONLY if bitmap exists

            // Set the click listener for the Save button
            btnSave.setOnClickListener(v -> {
                Log.d(TAG, "Save button clicked.");
                // Check if the button is enabled (means bitmap exists)
                if (btnSave.isEnabled()) {
                    // *** MODIFIED: Call performSaveBitmapToGallery directly ***
                    performSaveBitmapToGallery(displayedBitmap); // Call the save method directly
                    // *** END MODIFIED ***
                } else {
                    // Should ideally not happen if button is disabled correctly
                    Log.w(TAG, "Save button clicked but it was disabled. displayedBitmap is null.");
                    Toast.makeText(this, "No image available to save.", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // If edit button should be shown (viewing own profile potentially from settings)
            Log.d(TAG, "Show edit button, hide save button.");
            btnEdit.setVisibility(View.VISIBLE); // Show Edit
            btnEdit.setEnabled(true); // Enable Edit

            // Set the click listener for the Edit button
            btnEdit.setOnClickListener(v -> {
                Log.d(TAG, "Edit button clicked.");
                Intent intent = new Intent(FullscreenImageActivity.this, Setting_profile.class);
                startActivity(intent);
                // Optional: finish this activity after navigating to settings
                // finish();
            });

            // *** Hide the Save button ***
            btnSave.setVisibility(View.GONE); // Hide Save
            btnSave.setEnabled(false);
        }
        // *** END MODIFIED: Button visibility logic ***

    } // End of onCreate


    // *** REMOVE THIS METHOD ENTIRELY ***
    /*
    private void saveImageToGallery() {
        // ... (Removed permission check/request logic) ...
    }
    */
    // *** END REMOVE ***


    // *** REMOVE THIS METHOD ENTIRELY ***
    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // ... (Removed permission result handling) ...
    }
    */
    // *** END REMOVE ***


    // *** Perform the Actual Saving of the Bitmap to Gallery (Remains the same, includes good logging) ***
    private void performSaveBitmapToGallery(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "performSaveBitmapToGallery called with null bitmap!");
            Toast.makeText(this, "Error saving: Image data missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Performing actual save of bitmap to gallery.");

        // Generate a file name using the current timestamp
        String filename = "ProfileImage_" + System.currentTimeMillis() + ".jpg";
        Log.d(TAG, "Generated filename: " + filename);

        // Use MediaStore API for saving (recommended for Android 10+)
        ContentResolver resolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg"); // Set MIME type

        // For Android 10 (API 29) and above, use RELATIVE_PATH to save to the Pictures directory
        // For older APIs, saving to Environment.DIRECTORY_PICTURES via OutputStream is handled below
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Log.d(TAG, "Using MediaStore with RELATIVE_PATH for API " + Build.VERSION.SDK_INT);
            // Set IS_PENDING true while writing, then set to false after writing is complete
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
        } else {
            Log.d(TAG, "Using MediaStore with EXTERNAL_CONTENT_URI for API " + Build.VERSION.SDK_INT);
        }

        Uri uri = null; // URI where the image will be saved

        try {
            // Insert a new record into MediaStore
            // This gets the URI where we can write the file
            final Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) : // Use external primary volume for Android 10+
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI; // Use external content URI for older APIs

            Log.d(TAG, "MediaStore collection URI: " + collection);
            uri = resolver.insert(collection, contentValues);
            Log.d(TAG, "MediaStore insert returned URI: " + uri);

            if (uri != null) {
                // Open an OutputStream to write the bitmap data to the URI
                try (OutputStream stream = resolver.openOutputStream(uri)) {
                    if (stream != null) {
                        Log.d(TAG, "OutputStream obtained. Compressing and writing bitmap.");
                        // Compress the bitmap and write it to the output stream
                        // Use JPEG format and 90% quality (adjust quality as needed)
                        boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        if(compressed) {
                            Log.d(TAG, "Bitmap compressed and written to stream successfully.");
                        } else {
                            Log.e(TAG, "Bitmap compression failed!");
                            // Even if compression fails, the stream might be closed, proceed to update.
                            // But likely indicates an issue.
                        }
                    } else {
                        Log.e(TAG, "OutputStream is null for URI: " + uri + ". Cannot write data.");
                        Toast.makeText(this, "Error saving image.", Toast.LENGTH_SHORT).show();
                        // Clean up the pending entry if possible
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            try {
                                resolver.delete(uri, null, null); // Delete the pending entry
                                Log.d(TAG, "Cleaned up pending entry due to null OutputStream.");
                            } catch (SecurityException deleteEx) {
                                Log.e(TAG, "SecurityException during pending entry cleanup after null OutputStream.", deleteEx);
                            } catch (Exception deleteOtherEx) {
                                Log.e(TAG, "Other exception during pending entry cleanup after null OutputStream.", deleteOtherEx);
                            }
                        }
                        return; // Exit on stream error
                    }
                } // stream is closed automatically by try-with-resources

                // If successful, update the MediaStore entry (especially for Android 10+)
                // This marks the file as ready and viewable in the gallery
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear();
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0); // Mark as not pending (writing complete)
                    int updatedRows = resolver.update(uri, contentValues, null, null);
                    if (updatedRows > 0) {
                        Log.d(TAG, "MediaStore entry updated (IS_PENDING=0) successfully for URI: " + uri);
                    } else {
                        Log.e(TAG, "MediaStore update (IS_PENDING=0) returned 0 rows updated for URI: " + uri);
                        // This might still work but indicates an issue with the update call
                    }
                } else {
                    // For older APIs, the file is usually immediately available once stream is closed.
                    // MediaStore.scanFile is sometimes used for immediate indexing, but often not strictly needed.
                    Log.d(TAG, "Image saved. No IS_PENDING update needed for API " + Build.VERSION.SDK_INT);
                }


                // Show success message
                Log.d(TAG, "Image saved to gallery successfully at URI: " + uri);
                Toast.makeText(this, "Image saved to Gallery!", Toast.LENGTH_SHORT).show();

            } else {
                Log.e(TAG, "MediaStore insert returned null URI.");
                Toast.makeText(this, "Error creating file for image.", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Log.e(TAG, "IOException during image save to gallery: " + e.getMessage(), e);
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Clean up the pending entry on error
            if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    int deletedRows = resolver.delete(uri, null, null);
                    Log.d(TAG, "Cleaned up pending entry ("+deletedRows+" rows) on save error for URI: " + uri);
                } catch (SecurityException deleteEx) {
                    Log.e(TAG, "SecurityException during pending entry cleanup after IOException.", deleteEx);
                } catch (Exception deleteOtherEx) {
                    Log.e(TAG, "Other exception during pending entry cleanup after IOException.", deleteOtherEx);
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException during image save to gallery: " + e.getMessage(), e);
            Toast.makeText(this, "Permission issue during save. Check app settings.", Toast.LENGTH_LONG).show();
            // If this happens after permission granted, it's a deeper system/manifest issue.
        }
        catch (Exception e) { // Catch any other unexpected errors during the save process
            Log.e(TAG, "Unexpected error during image save to gallery: " + e.getMessage(), e);
            Toast.makeText(this, "An unexpected error occurred during save.", Toast.LENGTH_SHORT).show();
            // Attempt cleanup for unexpected errors too
            if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    int deletedRows = resolver.delete(uri, null, null);
                    Log.d(TAG, "Cleaned up pending entry ("+deletedRows+" rows) on unexpected save error for URI: " + uri);
                } catch (SecurityException deleteEx) {
                    Log.e(TAG, "SecurityException during pending entry cleanup after unexpected Exception.", deleteEx);
                } catch (Exception deleteOtherEx) {
                    Log.e(TAG, "Other exception during pending entry cleanup after unexpected Exception.", deleteOtherEx);
                }
            }
        }
    }


    // decodeBase64ToBitmap helper method remains the same
    private Bitmap decodeBase64ToBitmap(String base64) {
        // ... (Keep your existing decodeBase64ToBitmap method code here) ...
        if (base64 == null || base64.isEmpty()) {
            Log.w(TAG, "decodeBase64ToBitmap received null or empty string.");
            return null;
        }
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap == null) Log.w(TAG, "BitmapFactory failed to decode bytes.");
            return bitmap;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid Base64 string for decoding.", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error decoding Base64 string", e);
            return null;
        }
    }
}