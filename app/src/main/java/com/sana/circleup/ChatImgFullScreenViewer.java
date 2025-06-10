package com.sana.circleup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log; // Import Log
import android.widget.ImageView; // Keep ImageView import if needed elsewhere
import android.widget.Toast; // Import Toast
import android.view.View; // Import View for findViewById null check

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets; // Keep if EdgeToEdge requires it
import androidx.core.view.ViewCompat; // Keep if EdgeToEdge requires it
import androidx.core.view.WindowInsetsCompat; // Keep if EdgeToEdge requires it

// *** NEW: Import Subsampling Scale ImageView ***
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.ImageSource; // Import ImageSource


// Keep Picasso import if used elsewhere, but not needed for Base64 display
// import com.squareup.picasso.Picasso;

public class ChatImgFullScreenViewer extends AppCompatActivity {

    private static final String TAG = "ChatImgFullScreen"; // Define TAG

    // *** MODIFIED: Change type from ImageView to SubsamplingScaleImageView ***
    SubsamplingScaleImageView fullImageView; // <-- Yahan type change karain
    // *** END MODIFIED ***

    @Override // <-- Yeh woh method hai jismein changes karni hain
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep EdgeToEdge if desired for full screen experience
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_img_full_screen_viewer);

        // Keep EdgeToEdge setup if present and desired
        // Check R.id.main - If your root layout has a different ID, change R.id.main here.
        // If you don't need manual insets handling, this block can be removed.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // *** MODIFIED: Cast findViewById result to SubsamplingScaleImageView ***
        fullImageView = findViewById(R.id.fullImageView); // <-- Yahan find karein
        // If your variable type is SubsamplingScaleImageView, no explicit cast might be needed
        // depending on your Android Studio/Gradle setup, but it's safer to declare the variable
        // with the correct type directly.
        // *** END MODIFIED ***


        // Add a null check for the view immediately
        if (fullImageView == null) {
            Log.e(TAG, "fullImageView (SubsamplingScaleImageView) not found in layout R.id.fullImageView!");
            Toast.makeText(this, "Error loading image view.", Toast.LENGTH_SHORT).show();
            finish(); // Cannot proceed without the image view
            return; // Stop execution
        }


        String imageUrl = getIntent().getStringExtra("imageUrl");

        if (imageUrl != null && !imageUrl.isEmpty()) { // Add empty check for safety
            Log.d(TAG, "Received image URL (Base64). Decoding...");
            // Decode Base64 to Bitmap
            try {
                // Use android.util.Base64 consistently
                byte[] decodedString = android.util.Base64.decode(imageUrl, android.util.Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (decodedByte != null) {
                    // *** MODIFIED: Setting image on SubsamplingScaleImageView ***
                    fullImageView.setImage(ImageSource.bitmap(decodedByte)); // <-- Use this method
                    Log.d(TAG, "Bitmap decoded and set on SubsamplingScaleImageView.");
                    // *** END MODIFIED ***
                } else {
                    Log.w(TAG, "Base64 decode returned null bitmap for chat image.");
                    // When bitmap is null, SubsamplingScaleImageView cannot set it directly.
                    // You can set a default placeholder image resource if you have one.
                    try {
                        fullImageView.setImage(ImageSource.resource(R.drawable.error_image)); // Use an error image resource placeholder
                        Log.d(TAG, "Set error image resource.");
                    } catch (Exception resourceError) {
                        Log.e(TAG, "Failed to set error image resource.", resourceError);
                        fullImageView.setVisibility(View.GONE); // Hide view on failure
                    }
                    Toast.makeText(this, "Failed to decode image data.", Toast.LENGTH_SHORT).show();
                }
            } catch (IllegalArgumentException e) { // Specific catch for invalid Base64
                Log.e(TAG, "Invalid Base64 string received for chat image.", e);
                try {
                    fullImageView.setImage(ImageSource.resource(R.drawable.error_image)); // Use an error image resource placeholder
                    Log.d(TAG, "Set error image resource for invalid Base64.");
                } catch (Exception resourceError) {
                    Log.e(TAG, "Failed to set error image resource.", resourceError);
                    fullImageView.setVisibility(View.GONE); // Hide view on failure
                }
                Toast.makeText(this, "Invalid image data format.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) { // Catch any other unexpected decoding errors
                Log.e(TAG, "Unexpected error during Base64 decoding or setting image.", e);
                try {
                    fullImageView.setImage(ImageSource.resource(R.drawable.error_image)); // Use an error image resource placeholder
                    Log.d(TAG, "Set error image resource for unexpected error.");
                } catch (Exception resourceError) {
                    Log.e(TAG, "Failed to set error image resource.", resourceError);
                    fullImageView.setVisibility(View.GONE); // Hide view on failure
                }
                Toast.makeText(this, "An error occurred loading the image.", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.w(TAG, "Image URL (Base64) is null or empty in intent extras.");
            // If no image URL is provided, set a default placeholder or show a message
            try {
                fullImageView.setImage(ImageSource.resource(R.drawable.default_profile_img)); // Use a default image resource
                Log.d(TAG, "Set default image resource as no image URL received.");
            } catch (Exception resourceError) {
                Log.e(TAG, "Failed to set default image resource.", resourceError);
                fullImageView.setVisibility(View.GONE); // Hide view on failure
            }
            Toast.makeText(this, "No image provided.", Toast.LENGTH_SHORT).show();
        }
    }

    // You might want to keep the decodeBase64ToBitmap helper if you use it elsewhere,
    // but here we are decoding directly in onCreate for simplicity.
    // If you keep it, ensure it's private and within the class.
    /*
    private Bitmap decodeBase64ToBitmap(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid Base64 string for decoding.", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error decoding Base64 string", e);
            return null;
        }
    }
    */
}