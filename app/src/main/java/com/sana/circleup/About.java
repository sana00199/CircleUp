package com.sana.circleup; // Replace with your actual package name

import android.os.Bundle;
import android.text.Html; // Import Html
import android.util.Log;
import android.view.MenuItem; // Import MenuItem
import android.widget.TextView; // Import TextView

import androidx.annotation.NonNull; // Import NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.core.text.HtmlCompat; // Import HtmlCompat (recommended)

public class About extends AppCompatActivity {

    private static final String TAG = "AboutActivity"; // Add TAG

    private TextView aboutContentTextView; // TextView to display the about text
    private TextView versionTextView; // TextView for the version (optional)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about); // Use the layout created in Step 2
        Log.d(TAG, "🟢 AboutActivity onCreate.");


        // Setup Toolbar as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_about); // Match your layout ID
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("About CircleUp"); // Set title
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
            }
        } else {
            Log.w(TAG, "Toolbar not found in layout with ID toolbar_about");
        }

        // Initialize TextViews
        aboutContentTextView = findViewById(R.id.text_view_about_content);

        // Load and display the about content from resources
        loadAboutContent();


        Log.d(TAG, "✅ onCreate finished in AboutActivity.");
    }

    // Load and display the about content
    private void loadAboutContent() {
        // Get the HTML string from the string resource file (Step 3)
        String aboutHtmlText = getString(R.string.about_circleup_content);

        // Parse the HTML string using HtmlCompat (preferred for modern Android)
        // FROM_HTML_MODE_LEGACY handles basic HTML tags like h1, p, ul, li, strong
        CharSequence formattedText = HtmlCompat.fromHtml(aboutHtmlText, HtmlCompat.FROM_HTML_MODE_LEGACY);

        // Set the formatted text to the TextView
        if (aboutContentTextView != null) {
            aboutContentTextView.setText(formattedText);
            Log.d(TAG, "About content loaded and set to TextView.");
        } else {
            Log.e(TAG, "TextView for about content not found in layout!");
        }

        // Optional: Set app version dynamically if you have it
        // try {
        //     String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        //     if (versionTextView != null) {
        //         versionTextView.setText("Version " + versionName);
        //     }
        // } catch (Exception e) {
        //     Log.e(TAG, "Error getting app version", e);
        //     if (versionTextView != null) { versionTextView.setText("Version Unknown"); }
        // }
    }


    // Handle back button click on the Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back to the previous activity (MainActivity)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // No need for onDestroy if no special cleanup is needed
}