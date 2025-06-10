package com.sana.circleup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLogin extends AppCompatActivity {

    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputUserPhoneNumber, InputUserVerificationCode;
    private LinearLayout phoneCodeEditTextLayout, sendAgainLayout;
    private TextView textViewSendAgain;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private static final String TAG = "PhoneLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        initializeFields();
        setupClickListeners();
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();

        SendVerificationCodeButton = findViewById(R.id.send_code_btn);
        VerifyButton = findViewById(R.id.verify_phoneno_btn);
        InputUserPhoneNumber = findViewById(R.id.phoneveri_edittext);
        InputUserVerificationCode = findViewById(R.id.code_phoneveri_edittext);
        phoneCodeEditTextLayout = findViewById(R.id.phonee_code_editext_layout);
        sendAgainLayout = findViewById(R.id.send_again_layout);
        textViewSendAgain = findViewById(R.id.textview_send_again);

        loadingBar = new ProgressDialog(this);
        loadingBar.setCancelable(false);
    }

    private void setupClickListeners() {
        SendVerificationCodeButton.setOnClickListener(v -> initiatePhoneVerification());
        VerifyButton.setOnClickListener(v -> verifyCode());
        textViewSendAgain.setOnClickListener(v -> resetToPhoneNumberInput());
    }

    private void initiatePhoneVerification() {
        String phoneNumber = InputUserPhoneNumber.getText().toString().trim();

        if (validatePhoneNumber(phoneNumber)) {
            // Ensure phone number has country code
            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+" + phoneNumber;
            }
            startPhoneVerification(phoneNumber);
        }
    }

    private void startPhoneVerification(String phoneNumber) {
        showLoadingBar("Phone Verification", "Please wait while we verify your phone...");

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Log.d(TAG, "onVerificationCompleted: " + credential);
                        signInWithPhoneAuthCredential(credential);
                    }

                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e(TAG, "onVerificationFailed", e);
                        hideLoadingBar();
                        showToast("Verification failed: " + e.getMessage());
                        resetToPhoneNumberInput();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(TAG, "onCodeSent: " + verificationId);
                        mVerificationId = verificationId;
                        mResendToken = token;
                        hideLoadingBar();
                        showToast("Verification code sent successfully!");
                        switchToCodeInput();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode() {
        String code = InputUserVerificationCode.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            showToast("Please enter verification code");
            return;
        }

        if (mVerificationId != null) {
            showLoadingBar("Code Verification", "Please wait while we verify the code...");
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        } else {
            showToast("Error: Verification ID is null. Please try again.");
            resetToPhoneNumberInput();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    hideLoadingBar();
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        SendUserToMainActivity();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        showToast("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void resetToPhoneNumberInput() {
        VerifyButton.setVisibility(View.GONE);
        phoneCodeEditTextLayout.setVisibility(View.GONE);
        sendAgainLayout.setVisibility(View.GONE);

        SendVerificationCodeButton.setVisibility(View.VISIBLE);
        InputUserPhoneNumber.setVisibility(View.VISIBLE);
        InputUserPhoneNumber.setText("");
    }

    private void switchToCodeInput() {
        SendVerificationCodeButton.setVisibility(View.GONE);
        InputUserPhoneNumber.setVisibility(View.GONE);

        VerifyButton.setVisibility(View.VISIBLE);
        phoneCodeEditTextLayout.setVisibility(View.VISIBLE);
        sendAgainLayout.setVisibility(View.VISIBLE);
        InputUserVerificationCode.setText("");
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLogin.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            showToast("Please enter a phone number");
            return false;
        }

        // Remove any spaces or special characters
        phoneNumber = phoneNumber.replaceAll("[\\s-()]", "");

        if (phoneNumber.length() < 10) {
            showToast("Please enter a valid phone number with country code");
            return false;
        }
        return true;
    }

    private void showLoadingBar(String title, String message) {
        loadingBar.setTitle(title);
        loadingBar.setMessage(message);
        loadingBar.show();
    }

    private void hideLoadingBar() {
        if (loadingBar != null && loadingBar.isShowing()) {
            loadingBar.dismiss();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingBar != null && loadingBar.isShowing()) {
            loadingBar.dismiss();
        }
    }
}