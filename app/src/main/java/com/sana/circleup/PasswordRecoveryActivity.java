package com.sana.circleup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class PasswordRecoveryActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        EditText emailEditText = findViewById(R.id.email_recovery_edittext);
        Button recoverButton = findViewById(R.id.recover_password_button);
        TextView returnToLoginText = findViewById(R.id.return_to_login_text);
        // 1. Back button ImageView ko find karein uski ID se
        ImageView backButton = findViewById(R.id.back_arrow_icon);

        // 2. Back button par click listener set karein
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Jab button click ho, current activity ko finish kar dein
                finish(); // Yeh current activity ko close kar dega aur previous screen par wapas le jayega
            }
        });

        recoverButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }
            // Trigger Firebase password reset email
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PasswordRecoveryActivity.this,
                                    "Password reset email sent.",
                                    Toast.LENGTH_SHORT).show();
                            finish(); // Go back to login screen
                        } else {
                            Toast.makeText(PasswordRecoveryActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });



        returnToLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PasswordRecoveryActivity.this, Login.class);
                startActivity(intent);
            }
        });
    }
}