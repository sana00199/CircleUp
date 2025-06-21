package com.sana.circleup;



import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
//
//public class ScheduledMSG extends AppCompatActivity {
//
//    EditText etMessage;
//    Button btnPickTime, btnSchedule, btnSelectContacts;
//    TextView tvSelectedTime;
//    long selectedTimeMillis = -1;
//
//    String senderId; // Sender is the logged-in user
//    ArrayList<String> selectedUserIds = new ArrayList<>(); // List of selected receivers
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_scheduled_msg);
//
//        etMessage = findViewById(R.id.etMessage);
//        btnPickTime = findViewById(R.id.btnPickTime);
//        btnSchedule = findViewById(R.id.btnSchedule);
//        tvSelectedTime = findViewById(R.id.tvSelectedTime);
//        btnSelectContacts = findViewById(R.id.btn_select_contacts);
//
//        etMessage.setMovementMethod(new ScrollingMovementMethod());
//
//        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current logged-in user's ID
//
//        // Contact selection button logic
//        btnSelectContacts.setOnClickListener(v -> {
//            showContactSelectionDialog();
//        });
//
//        // Time selection logic
//        btnPickTime.setOnClickListener(v -> openDateTimePicker());
//
//        // Schedule message logic
//        btnSchedule.setOnClickListener(v -> {
//            String message = etMessage.getText().toString().trim();
//
//            // Validation for message, time, and contacts
//            if (message.isEmpty()) {
//                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (selectedTimeMillis == -1) {
//                Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (selectedUserIds.isEmpty()) {
//                Toast.makeText(this, "Please select at least one contact", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Call method to schedule the message
//            scheduleMessage(senderId, message, selectedTimeMillis);
//        });
//
//    }
//
//    // Method to open date and time picker
//    private void openDateTimePicker() {
//        Calendar calendar = Calendar.getInstance();
//        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
//            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
//                calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
//                selectedTimeMillis = calendar.getTimeInMillis();
//                tvSelectedTime.setText("Scheduled for: " + calendar.getTime().toString());
//            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
//        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
//    }
//
//    // Method to schedule the message
////    private void scheduleMessage(String senderId, String message, long timeMillis) {
////        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ScheduledMessages");
////
////        // Loop through all selected users and save a message for each user
////        for (String receiverId : selectedUserIds) {
////            String msgId = ref.push().getKey();
//////            ScheMsg scheduledMessage = new ScheMsg(senderId, receiverId, message, timeMillis);
////
////
////            // Format the timeMillis to a readable time format as String
////            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
////            String formattedTime = sdf.format(new Date(timeMillis)); // Convert to String
////
////// Now pass the formatted time to the ScheMsg constructor as a String
////            ScheMsg scheduledMessage = new ScheMsg(senderId, receiverId, message, formattedTime);
////
////
////            ref.child(msgId).setValue(scheduledMessage);
////
////            // Schedule the worker to send the message at the selected time
////            Data data = new Data.Builder().putString("msgId", msgId).build();
////            long delay = timeMillis - System.currentTimeMillis();
////
////            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ScheduledMessageWorker.class)
////                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
////                    .setInputData(data)
////                    .build();
////
////            WorkManager.getInstance(this).enqueue(workRequest);
////        }
////
////        Toast.makeText(this, "Message scheduled!", Toast.LENGTH_SHORT).show();
////        finish();
////    }
//
//
//private void scheduleMessage(String senderId, String message, long timeMillis) {
//    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ScheduledMessages");
//
//    // Save receiver names and IDs as lists
//    List<String> receiverIds = selectedUserIds; // Assuming you have the selected user IDs in this list
//    List<String> receiverNames = new ArrayList<>();
//
//    for (String receiverId : receiverIds) {
//        // Get the receiver's name from Firebase or wherever it's stored
//        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(receiverId);
//        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String receiverName = snapshot.child("username").getValue(String.class);
//                receiverNames.add(receiverName);
//
//                // Once all names are fetched, save the scheduled message
//                if (receiverNames.size() == receiverIds.size()) {
//                    String msgId = ref.push().getKey();
//                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//                    String formattedTime = sdf.format(new Date(timeMillis));
//
//                    // Now save the message with receiver names
//                    ScheMsg scheduledMessage = new ScheMsg(senderId, receiverIds, receiverNames, message, formattedTime);
//
//                    // Set the receiver names string if necessary
//                    scheduledMessage.setReceiverNamesStr(String.join(", ", receiverNames));
//
//                    ref.child(msgId).setValue(scheduledMessage);
//
//                    // Schedule the worker to send the message at the selected time
//                    Data data = new Data.Builder().putString("msgId", msgId).build();
//                    long delay = timeMillis - System.currentTimeMillis();
//
//                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ScheduledMessageWorker.class)
//                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
//                            .setInputData(data)
//                            .build();
//
//                    WorkManager.getInstance(ScheduledMSG.this).enqueue(workRequest);
//
//                    Toast.makeText(ScheduledMSG.this, "Message scheduled!", Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(ScheduledMSG.this, "Error fetching user name", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}
//
//
//
//
//
//    // Method to show the contact selection dialog
//    private void showContactSelectionDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Select Contacts");
//
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_contacts, null);
//        builder.setView(dialogView);
//
//        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_contacts);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        List<UserModel> contactsList = new ArrayList<>();
//        ContactScheduleSelectAdapter adapter = new ContactScheduleSelectAdapter(contactsList, selectedUserIds);
//        recyclerView.setAdapter(adapter);
//
//        // Load contacts from Firebase
//        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference("Contacts").child(currentUid);
//
//        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                contactsList.clear();
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    String uid = ds.getKey();
//                    FirebaseDatabase.getInstance().getReference("Users").child(uid)
//                            .addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    UserModel user = snapshot.getValue(UserModel.class);
//                                    if (user != null) {
//                                        user.setUserId(uid);
//                                        contactsList.add(user);
//                                        adapter.notifyDataSetChanged();
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {}
//                            });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        });
//
//        builder.setPositiveButton("Done", (dialog, which) -> {
//            // After selecting contacts, update the selected users list
//            Toast.makeText(this, selectedUserIds.size() + " contacts selected", Toast.LENGTH_SHORT).show();
//        });
//
//        builder.setNegativeButton("Cancel", null);
//        builder.show();
//    }
//}




import android.graphics.Bitmap; // Import Bitmap
import android.graphics.BitmapFactory; // Import BitmapFactory
import android.text.TextUtils;
import android.util.Base64; // Import android.util.Base64
import android.util.Log;
import android.widget.ImageView; // Import ImageView

import androidx.appcompat.widget.SearchView; // Import AndroidX SearchView

import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream; // Import ByteArrayOutputStream

// *** Imports for Worker Key Loading ***
// *** End Worker Key Loading Imports ***

// *** Imports for OneSignal ***

//
//public class ScheduledMSG extends AppCompatActivity {
//
//    private static final String TAG = "ScheduledMSG";
//    private static final int REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 123;
//
//    private ImageView backArrowIcon;
//    private EditText etMessage;
//    private Button btnPickTime, btnSchedule, btnSelectContacts;
//    private TextView tvSelectedTime, tvSelectedContacts;
//    private FrameLayout imagePreviewContainer;
//    private ImageView imgPreview;
//    private ImageButton btnCancelImage;
//    private ImageButton btnPickGalleryImage, btnTakePhoto;
//
//    private long selectedTimeMillis = -1;
//    private String senderId;
//    private ArrayList<String> selectedUserIds = new ArrayList<>();
//    private ArrayList<String> selectedUserNames = new ArrayList<>();
//    private String selectedUserNamesString = "No contacts selected";
//
//    private Uri imageToScheduleUri;
//    private String imageToScheduleBase64 = "";
//
//    private DatabaseReference rootRef;
//    private FirebaseAuth mAuth;
//
//    private ActivityResultLauncher<Intent> pickImageLauncher;
//    private ActivityResultLauncher<Uri> takePictureLauncher;
//    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
//
//    private static final int MAX_IMAGE_SCHEDULE_SIZE = 1024;
//    private static final int IMAGE_SCHEDULE_COMPRESSION_QUALITY = 85;
//
//    private static final int REQUEST_CODE_PERMISSION = 101;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_scheduled_msg);
//
//        mAuth = FirebaseAuth.getInstance();
//        rootRef = FirebaseDatabase.getInstance().getReference();
//
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser == null) {
//            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//        senderId = currentUser.getUid();
//
//        backArrowIcon = findViewById(R.id.back_arrow_icon);
//        etMessage = findViewById(R.id.etMessage);
//        btnPickTime = findViewById(R.id.btnPickTime);
//        btnSchedule = findViewById(R.id.btnSchedule);
//        btnSelectContacts = findViewById(R.id.btn_select_contacts);
//        tvSelectedTime = findViewById(R.id.tvSelectedTime);
//        tvSelectedContacts = findViewById(R.id.tvSelectedContacts);
//
//        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
//        imgPreview = findViewById(R.id.imgPreview);
//        btnCancelImage = findViewById(R.id.btnCancelImage);
//        btnPickGalleryImage = findViewById(R.id.btnPickGalleryImage);
//        btnTakePhoto = findViewById(R.id.btnTakePhoto);
//
//        imagePreviewContainer.setVisibility(View.GONE);
//        btnCancelImage.setVisibility(View.GONE);
//
//        etMessage.setMovementMethod(new ScrollingMovementMethod());
//
//        backArrowIcon.setOnClickListener(v -> onBackPressed());
//        btnSelectContacts.setOnClickListener(v -> showContactSelectionDialog());
//        btnPickTime.setOnClickListener(v -> openDateTimePicker());
//        btnPickGalleryImage.setOnClickListener(v -> openGalleryPicker());
//        btnTakePhoto.setOnClickListener(v -> openCameraPicker());
//        btnCancelImage.setOnClickListener(v -> clearSelectedImage());
//
//        btnSchedule.setOnClickListener(v -> scheduleMessages());
//
//        initializeImagePickers();
//
//        requestCameraPermissionLauncher = registerForActivityResult(
//                new ActivityResultContracts.RequestPermission(),
//                isGranted -> {
//                    if (isGranted) {
//                        Log.d(TAG, "Camera permission granted. Launching camera intent.");
//                        launchCameraIntent();
//                    } else {
//                        Log.w(TAG, "Camera permission denied. Cannot take photo for scheduling.");
//                        Toast.makeText(this, "Camera permission denied. Cannot take photo.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }
//
//    private void openDateTimePicker() {
//        Calendar calendar = Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
//
//        new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
//            new TimePickerDialog(this, (view1, selectedHour, selectedMinute) -> {
//                Calendar selectedDateTime = Calendar.getInstance();
//                selectedDateTime.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);
//
//                if (selectedDateTime.getTimeInMillis() < System.currentTimeMillis()) {
//                    Toast.makeText(this, "Cannot schedule in the past.", Toast.LENGTH_SHORT).show();
//                    selectedTimeMillis = -1;
//                    tvSelectedTime.setText("No time selected");
//                } else {
//                    selectedTimeMillis = selectedDateTime.getTimeInMillis();
//                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
//                    tvSelectedTime.setText("Scheduled for: " + dateTimeFormat.format(selectedDateTime.getTime()));
//                    Log.d(TAG, "Selected time: " + selectedTimeMillis);
//                }
//            }, hour, minute, false).show();
//        }, year, month, day).show();
//    }
//
//    private void scheduleSingleMessage(String senderId, String content, long timeMillis, String messageType,
//                                       ArrayList<String> receiverIds, ArrayList<String> receiverNames, String originalScheduledTimeFormatted) {
//
//        if (receiverIds.isEmpty()) {
//            Log.w(TAG, "scheduleSingleMessage called with empty receiver list. Skipping.");
//            return;
//        }
//
//        DatabaseReference scheduledRef = FirebaseDatabase.getInstance().getReference("ScheduledMessages");
//        String msgId = scheduledRef.push().getKey();
//
//        if (msgId == null) {
//            Log.e(TAG, "Failed to generate Firebase push key for a single scheduled message (Type: " + messageType + ").");
//            return;
//        }
//
//        ScheMsg scheduledMessage = new ScheMsg(senderId, receiverIds, receiverNames, content, messageType, timeMillis, originalScheduledTimeFormatted);
//        scheduledMessage.setMsgFirebaseId(msgId);
//        scheduledMessage.setStatus("pending");
//
//        scheduledRef.child(msgId).setValue(scheduledMessage)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "Single scheduled message entry saved to Firebase: " + msgId + " (Type: " + messageType + ")");
//
//                    Data data = new Data.Builder()
//                            .putString("msgId", msgId)
//                            .build();
//
//                    long delay = timeMillis - System.currentTimeMillis();
//                    if (delay < 0) {
//                        Log.w(TAG, "Scheduled time is in the past (" + originalScheduledTimeFormatted + "). Setting delay to 0 for msgId " + msgId);
//                        delay = 0;
//                    }
//
//                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ScheduledMessageWorker.class)
//                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
//                            .setInputData(data)
//                            .addTag(msgId)
//                            .build();
//
//                    WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
//                    Log.d(TAG, "WorkManager request enqueued for msgId " + msgId + " (Type: " + messageType + ") with delay " + delay + "ms.");
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Failed to save single scheduled message entry to Firebase: " + msgId + " (Type: " + messageType + ")", e);
//                });
//    }
//
//    private void scheduleMessages() {
//        String messageText = etMessage.getText().toString().trim();
//        boolean isImageStaged = !TextUtils.isEmpty(imageToScheduleBase64);
//
//        if (TextUtils.isEmpty(messageText) && !isImageStaged) {
//            Toast.makeText(this, "Please enter a message or select an image to schedule.", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Schedule attempt failed: Empty text and no staged image.");
//            return;
//        }
//
//        if (selectedTimeMillis == -1) {
//            Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Schedule attempt failed: No time selected.");
//            return;
//        }
//
//        if (selectedUserIds.isEmpty()) {
//            Toast.makeText(this, "Please select at least one contact.", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Schedule attempt failed: No contacts selected.");
//            return;
//        }
//
//        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
//        String formattedTime = sdf.format(new Date(selectedTimeMillis));
//
//        boolean textScheduled = false;
//        boolean imageScheduled = false;
//
//        if (!TextUtils.isEmpty(messageText)) {
//            Log.d(TAG, "Scheduling text message...");
//            scheduleSingleMessage(senderId, messageText, selectedTimeMillis, "text", selectedUserIds, selectedUserNames, formattedTime);
//            textScheduled = true;
//        }
//
//        if (isImageStaged) {
//            Log.d(TAG, "Scheduling image message...");
//            scheduleSingleMessage(senderId, imageToScheduleBase64, selectedTimeMillis, "image", selectedUserIds, selectedUserNames, formattedTime);
//            imageScheduled = true;
//        }
//
//        if (textScheduled || imageScheduled) {
//            Log.d(TAG, "Messages scheduled. Cleaning up UI.");
//            if (textScheduled && etMessage != null) {
//                etMessage.setText("");
//            }
//            if (imageScheduled) {
//                clearSelectedImage();
//            }
//
//            String scheduledItems = "";
//            if(textScheduled && imageScheduled) scheduledItems = "text and image messages";
//            else if (textScheduled) scheduledItems = "text message";
//            else if (imageScheduled) scheduledItems = "image message";
//
//            Toast.makeText(this, "Scheduled " + scheduledItems + "!", Toast.LENGTH_SHORT).show();
//            finish();
//        } else {
//            Log.w(TAG, "scheduleMessages: No messages were actually scheduled despite passing initial validation.");
//            Toast.makeText(this, "Could not schedule message.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void showContactSelectionDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_contacts, null);
//        builder.setView(dialogView);
//        builder.setTitle("Select Contacts");
//
//        SearchView dialogSearchView = dialogView.findViewById(R.id.dialog_search_view);
//        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_contacts);
//        TextView tvEmpty = dialogView.findViewById(R.id.tv_empty);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        List<UserModel> dialogContactsList = new ArrayList<>();
//        ContactScheduleSelectAdapter adapter = new ContactScheduleSelectAdapter(this, dialogContactsList, selectedUserIds);
//        recyclerView.setAdapter(adapter);
//
//        if (dialogSearchView != null) {
//            dialogSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                @Override public boolean onQueryTextSubmit(String query) { return false; }
//                @Override public boolean onQueryTextChange(String newText) {
//                    adapter.getFilter().filter(newText);
//                    Log.d(TAG, "Dialog Search query changed: " + newText);
//                    return true;
//                }
//            });
//        } else {
//            Log.w(TAG, "Dialog SearchView not found in dialog_select_contacts layout!");
//        }
//
//        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference("Contacts").child(currentUid);
//        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
//
//        Log.d(TAG, "Fetching accepted contacts for dialog...");
//
//        contactsRef.orderByChild("request_type").equalTo("accepted")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        List<String> friendIds = new ArrayList<>();
//                        if (snapshot.exists()) {
//                            for (DataSnapshot ds : snapshot.getChildren()) {
//                                String friendId = ds.getKey();
//                                if (friendId != null && !TextUtils.isEmpty(friendId)) {
//                                    friendIds.add(friendId);
//                                }
//                            }
//                        }
//                        Log.d(TAG, "Found " + friendIds.size() + " accepted contact UIDs for dialog.");
//
//                        if (friendIds.isEmpty()) {
//                            Log.d(TAG, "Friend IDs list is empty. No contacts to show in dialog.");
//                            adapter.setContactList(new ArrayList<>());
//                            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
//                            return;
//                        }
//
//                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
//                                List<UserModel> fetchedUsers = new ArrayList<>();
//                                Log.d(TAG, "Fetching details for " + friendIds.size() + " friends for dialog...");
//
//                                for (String friendId : friendIds) {
//                                    if (userSnapshot.hasChild(friendId)) {
//                                        UserModel user = userSnapshot.child(friendId).getValue(UserModel.class);
//                                        if (user != null) {
//                                            user.setUserId(friendId);
//                                            fetchedUsers.add(user);
//                                        } else {
//                                            Log.w(TAG, "UserModel is null for friendId: " + friendId + " for dialog.");
//                                        }
//                                    } else {
//                                        Log.w(TAG, "User data not found in /Users for friendId: " + friendId + " for dialog.");
//                                    }
//                                }
//                                Log.d(TAG, "Loaded " + fetchedUsers.size() + " user models for dialog.");
//
//                                dialogContactsList.clear();
//                                dialogContactsList.addAll(fetchedUsers);
//
//                                adapter.setContactList(new ArrayList<>(dialogContactsList));
//
//                                if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//                                Log.e(TAG, "Firebase Error fetching users for dialog contacts: " + error.getMessage(), error.toException());
//                                adapter.setContactList(new ArrayList<>());
//                                if (tvEmpty != null) tvEmpty.setText("Error loading contacts.");
//                                if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
//                                Toast.makeText(ScheduledMSG.this, "Error loading contacts.", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Log.e(TAG, "Firebase Database Error loading accepted contacts for dialog: " + error.getMessage(), error.toException());
//                        dialogContactsList.clear();
//                        adapter.setContactList(new ArrayList<>());
//                        if (tvEmpty != null) tvEmpty.setText("Error loading contacts.");
//                        if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
//                        Toast.makeText(ScheduledMSG.this, "Error loading contacts.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//        builder.setPositiveButton("Done", (dialog, which) -> {
//            selectedUserNames.clear();
//            List<UserModel> currentlySelectedUsers = adapter.getSelectedUserModels();
//            if (currentlySelectedUsers != null) {
//                for (UserModel user : currentlySelectedUsers) {
//                    if (!TextUtils.isEmpty(user.getUsername())) {
//                        selectedUserNames.add(user.getUsername());
//                    } else {
//                        selectedUserNames.add("Unknown");
//                    }
//                }
//            }
//
//            Log.d(TAG, "Contact selection dialog 'Done' clicked. Final Selected User IDs: " + selectedUserIds.toString() + ", Names: " + selectedUserNames.toString());
//            updateSelectedContactsDisplay();
//            Toast.makeText(this, selectedUserIds.size() + " contacts selected.", Toast.LENGTH_SHORT).show();
//        });
//
//        builder.setNegativeButton("Cancel", (dialog, which) -> {
//            Log.d(TAG, "Contact selection dialog 'Cancel' clicked.");
//            dialog.dismiss();
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//
//    private void updateSelectedContactsDisplay() {
//        if (selectedUserIds.isEmpty()) {
//            tvSelectedContacts.setText("No contacts selected");
//            tvSelectedContacts.setVisibility(View.GONE);
//            selectedUserNamesString = "No contacts selected";
//        } else {
//            selectedUserNamesString = TextUtils.join(", ", selectedUserNames);
//            tvSelectedContacts.setText("To: " + selectedUserNamesString);
//            tvSelectedContacts.setVisibility(View.VISIBLE);
//            Log.d(TAG, "Updated selected contacts display: " + selectedUserNamesString);
//        }
//    }
//
//    private void initializeImagePickers() {
//        pickImageLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
//                        Uri selectedImageUri = result.getData().getData();
//                        processSelectedImage(selectedImageUri);
//                    } else {
//                        Log.d(TAG, "Gallery image selection cancelled or failed.");
//                        Toast.makeText(this, "Image selection cancelled.", Toast.LENGTH_SHORT).show();
//                        clearSelectedImage();
//                    }
//                });
//
//        takePictureLauncher = registerForActivityResult(
//                new ActivityResultContracts.TakePicture(),
//                isSuccess -> {
//                    if (isSuccess) {
//                        Log.d(TAG, "Picture taken with camera. URI: " + imageToScheduleUri);
//                        processSelectedImage(imageToScheduleUri);
//                    } else {
//                        Log.w(TAG, "Camera picture taking cancelled or failed.");
//                        Toast.makeText(this, "Camera operation cancelled or failed.", Toast.LENGTH_SHORT).show();
//                        if (imageToScheduleUri != null) {
//                            try {
//                                getContentResolver().delete(imageToScheduleUri, null, null);
//                            } catch (Exception e) { Log.e(TAG, "Failed to delete temporary camera file after cancel/failure.", e); }
//                        }
//                        imageToScheduleUri = null;
//                        clearSelectedImage();
//                    }
//                });
//    }
//
//    private void openGalleryPicker() {
//        Log.d(TAG, "Attempting to open gallery for scheduling.");
//        clearSelectedImage();
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
//            return;
//        }
//        try {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            pickImageLauncher.launch(intent);
//        } catch (Exception e) {
//            Log.e(TAG, "Error launching gallery intent for scheduling.", e);
//            Toast.makeText(this, "Error accessing gallery.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void openCameraPicker() {
//        Log.d(TAG, "Attempting to open camera for scheduling.");
//        clearSelectedImage();
//
//        boolean cameraPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
//        boolean storagePermissionGranted = true;
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            storagePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//        }
//
//        if (cameraPermissionGranted && storagePermissionGranted) {
//            Log.d(TAG, "Camera and Storage permissions already granted. Proceeding to launch camera intent.");
//            launchCameraIntent();
//        } else {
//            Log.d(TAG, "Missing Camera or Storage permission(s). Requesting.");
//            List<String> permissionsToRequest = new ArrayList<>();
//            if (!cameraPermissionGranted) permissionsToRequest.add(Manifest.permission.CAMERA);
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !storagePermissionGranted) permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_CODE_PERMISSION);
//        }
//    }
//
//    private void launchCameraIntent() {
//        Log.d(TAG, "Attempting to launch camera intent.");
//        try {
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                Log.e(TAG, "Error creating temp image file for camera scheduling", ex);
//                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error preparing for photo.", Toast.LENGTH_SHORT).show());
//                return;
//            }
//
//            if (photoFile != null) {
//                imageToScheduleUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageToScheduleUri);
//                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                    Log.d(TAG, "Starting camera intent with URI: " + imageToScheduleUri);
//                    takePictureLauncher.launch(imageToScheduleUri);
//                } else {
//                    Log.e(TAG, "No camera app found to handle ACTION_IMAGE_CAPTURE.");
//                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "No camera app found.", Toast.LENGTH_SHORT).show());
//                    try{ if(photoFile.exists()) photoFile.delete(); } catch (Exception e) { Log.w(TAG, "Failed to clean up camera file on no app found", e); }
//                    imageToScheduleUri = null;
//                    clearSelectedImage();
//                }
//            } else {
//                Log.e(TAG, "Photo file was null after creation attempt.");
//                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error preparing for photo.", Toast.LENGTH_SHORT).show());
//                clearSelectedImage();
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Unexpected error setting up camera intent for scheduling.", e);
//            Toast.makeText(this, "Error accessing camera.", Toast.LENGTH_SHORT).show();
//            imageToScheduleUri = null;
//            clearSelectedImage();
//        }
//    }
//
//    private void processSelectedImage(@Nullable Uri uri) {
//        clearSelectedImage();
//
//        if (uri == null) {
//            Log.w(TAG, "processSelectedImage called with null URI after clearing previous state.");
//            runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
//            return;
//        }
//
//        Log.d(TAG, "Processing selected/captured image for scheduling. URI: " + uri);
//
//        new Thread(() -> {
//            try {
//                InputStream inputStream = getContentResolver().openInputStream(uri);
//                Bitmap originalBitmap = null;
//                if (inputStream != null) {
//                    originalBitmap = BitmapFactory.decodeStream(inputStream);
//                    inputStream.close();
//                }
//
//                if (originalBitmap == null) {
//                    Log.e(TAG, "Failed to get bitmap from URI: " + uri);
//                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
//                    clearSelectedImage();
//                    return;
//                }
//
//                Log.d(TAG, "Original image dimensions: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());
//
//                Bitmap resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SCHEDULE_SIZE, MAX_IMAGE_SCHEDULE_SIZE);
//                originalBitmap.recycle();
//
//                if (resizedBitmap == null) {
//                    Log.e(TAG, "Failed to resize bitmap.");
//                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
//                    clearSelectedImage();
//                    return;
//                }
//                Log.d(TAG, "Resized image dimensions: " + resizedBitmap.getWidth() + "x" + resizedBitmap.getHeight());
//
//                String base64Image = encodeToBase64(resizedBitmap);
//                resizedBitmap.recycle();
//
//                if (TextUtils.isEmpty(base64Image)) {
//                    Log.e(TAG, "Encoded Base64 image content is empty!");
//                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to encode image data.", Toast.LENGTH_SHORT).show());
//                    clearSelectedImage();
//                    return;
//                }
//
//                imageToScheduleBase64 = base64Image;
//
//                boolean isTempFileUri = false;
//                if (uri != null && uri.getAuthority() != null && uri.getAuthority().equals(getApplicationContext().getPackageName() + ".fileprovider")) {
//                    isTempFileUri = true;
//                } else if (uri != null && uri.getPath() != null && (uri.getPath().contains("/cache/") || uri.getPath().contains("/files/Pictures/"))) {
//                }
//
//                if (isTempFileUri) {
//                    imageToScheduleUri = uri;
//                    Log.d(TAG, "Keeping temporary URI for cleanup: " + imageToScheduleUri);
//                } else {
//                    imageToScheduleUri = null;
//                }
//
//                Log.d(TAG, "Image processed and Base64 stored. Ready for scheduling. Showing preview.");
//
//                runOnUiThread(() -> {
//                    try {
//                        if (imgPreview != null) {
//                            Glide.with(ScheduledMSG.this)
//                                    .load(uri)
//                                    .placeholder(R.drawable.image_placeholder_background)
//                                    .into(imgPreview);
//                        }
//
//                        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.VISIBLE);
//                        if (btnCancelImage != null) btnCancelImage.setVisibility(View.VISIBLE);
//
//                        Toast.makeText(ScheduledMSG.this, "Image selected for scheduling.", Toast.LENGTH_SHORT).show();
//
//                    } catch (Exception e) {
//                        Log.e(TAG, "Error showing image preview on Main Thread", e);
//                        runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error showing image preview.", Toast.LENGTH_SHORT).show());
//                        clearSelectedImage();
//                    }
//                });
//
//            } catch (IOException e) {
//                Log.e(TAG, "Image processing failed (IOException) for scheduling.", e);
//                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
//                clearSelectedImage();
//            } catch (SecurityException e) {
//                Log.e(TAG, "Security Exception during image processing (missing permissions?) for scheduling.", e);
//                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Permission error accessing image.", Toast.LENGTH_SHORT).show());
//                clearSelectedImage();
//            }
//            catch (Exception e) {
//                Log.e(TAG, "Unexpected error during image processing for scheduling.", e);
//                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "An error occurred processing image.", Toast.LENGTH_SHORT).show());
//                clearSelectedImage();
//            }
//        }).start();
//    }
//
//    private void clearSelectedImage() {
//        Log.d(TAG, "Clearing selected image.");
//        imageToScheduleBase64 = "";
//        if (imageToScheduleUri != null) {
//            try {
//                if (imageToScheduleUri.getAuthority() != null && imageToScheduleUri.getAuthority().equals(getApplicationContext().getPackageName() + ".fileprovider")) {
//                    getContentResolver().delete(imageToScheduleUri, null, null);
//                    Log.d(TAG, "Cleaned up temporary camera file: " + imageToScheduleUri);
//                } else {
//                    Log.d(TAG, "Selected image URI is not a recognized temporary file URI. Skipping cleanup.");
//                }
//            } catch (Exception e) {
//                Log.w(TAG, "Failed to delete temporary camera file.", e);
//            }
//        }
//        imageToScheduleUri = null;
//
//        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.GONE);
//        if (btnCancelImage != null) btnCancelImage.setVisibility(View.GONE);
//        if (imgPreview != null) imgPreview.setImageDrawable(null);
//    }
//
//    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
//        if (bitmap == null) return null;
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//
//        if (width <= maxWidth && height <= maxHeight) {
//            return bitmap;
//        }
//
//        float scale = Math.min(((float) maxWidth / width), ((float) maxHeight / height));
//
//        int newWidth = Math.round(width * scale);
//        int newHeight = Math.round(height * scale);
//
//        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
//    }
//
//    private String encodeToBase64(Bitmap image) {
//        if (image == null) return null;
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        image.compress(Bitmap.CompressFormat.JPEG, IMAGE_SCHEDULE_COMPRESSION_QUALITY, baos);
//        byte[] byteArray = baos.toByteArray();
//        return Base64.encodeToString(byteArray, Base64.DEFAULT);
//    }
//
//    private File createImageFile() throws IOException {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
//        String imageFileName = "SCHEDULED_TEMP_JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        if (storageDir == null) { Log.e(TAG, "getExternalFilesDir returned null"); throw new IOException("External files directory not available."); }
//        if (!storageDir.exists()) { if (!storageDir.mkdirs()) { Log.e(TAG, "Failed to create directory: " + storageDir.getAbsolutePath()); throw new IOException("Failed to create directory for temporary files."); } }
//        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
//        Log.d(TAG, "Created temporary image file for camera: " + image.getAbsolutePath());
//        return image;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CODE_PERMISSION) {
//            boolean allPermissionsGranted = true;
//            for (int result : grantResults) { if (result != PackageManager.PERMISSION_GRANTED) { allPermissionsGranted = false; break; } }
//            if (allPermissionsGranted) {
//                Log.d(TAG, "Permissions granted.");
//            } else {
//                Log.d(TAG, "Permissions denied.");
//                Toast.makeText(this, "Permissions denied. Cannot access storage/camera.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    public void requestBatteryOptimizationExemption() {
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
//                new AlertDialog.Builder(this)
//                        .setTitle("Improve Scheduled Message Delivery")
//                        .setMessage("To ensure scheduled messages send on time even when the app is closed, please allow CircleUp to run without battery restrictions. This will not significantly impact battery life.")
//                        .setPositiveButton("Allow", (dialog, which) -> {
//                            try {
//                                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                                intent.setData(Uri.parse("package:" + getPackageName()));
//                                startActivityForResult(intent, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                            } catch (Exception e) {
//                                Log.e(TAG, "Failed to launch battery optimization settings intent", e);
//                                Toast.makeText(this, "Could not open settings. Please manually find 'Battery Optimization' settings for CircleUp.", Toast.LENGTH_LONG).show();
//                            }
//                        })
//                        .setNegativeButton("Cancel", (dialog, which) -> {
//                            Toast.makeText(this, "Scheduled messages may be delayed if battery optimization is enabled.", Toast.LENGTH_LONG).show();
//                        })
//                        .show();
//            } else {
//                Log.d(TAG, "App is already ignoring battery optimizations.");
//            }
//        } else {
//            Log.d(TAG, "Battery optimization request not applicable below API 23.");
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) {
//            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (pm.isIgnoringBatteryOptimizations(getPackageName())) {
//                    Toast.makeText(this, "Background restrictions lifted for scheduled messages.", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "Background restrictions still active. Scheduled messages may be delayed.", Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (imageToScheduleUri != null) {
//            try {
//                if (imageToScheduleUri.getAuthority() != null && imageToScheduleUri.getAuthority().equals(getApplicationContext().getPackageName() + ".fileprovider")) {
//                    getContentResolver().delete(imageToScheduleUri, null, null);
//                    Log.d(TAG, "Cleaned up temp camera file in onDestroy.");
//                } else {
//                    Log.w(TAG, "imageToScheduleUri did not look like a temp file URI. Skipping cleanup in onDestroy.");
//                }
//            } catch (Exception e) {
//                Log.w(TAG, "Failed to delete temporary camera file in onDestroy", e);
//            } finally {
//                imageToScheduleUri = null;
//            }
//        }
//    }
//}



public class ScheduledMSG extends AppCompatActivity {

    private static final String TAG = "ScheduledMSG";
    private static final int REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 123;

    // --- UI Elements ---
    private ImageView backArrowIcon;
    private EditText etMessage;
    private Button btnPickTime, btnSchedule, btnSelectContacts;
    private TextView tvSelectedTime, tvSelectedContacts;
    // *** NEW Image Preview UI ***
    private FrameLayout imagePreviewContainer;
    private ImageView imgPreview;
    private ImageButton btnCancelImage;
    // *** Image Source Selection Buttons ***
    private ImageButton btnPickGalleryImage, btnTakePhoto;


    // --- Data & State Variables ---
    private long selectedTimeMillis = -1;
    private String senderId;

    // *** NEW Data Structure for Selected Recipients (Users, Groups, Rooms) ***
    // Simple data class to hold ID, Type, and Name of a selected recipient.
    public static class ScheduledRecipientInfo {
        private String id; // User UID, Group ID, Temporary Room ID
        private String type; // "user", "group", "temporary_room"
        private String name; // Name for display

        public ScheduledRecipientInfo(String id, String type, String name) {
            this.id = id;
            this.type = type;
            this.name = name;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getName() { return name; }

        // Optional: Add getters/setters if needed elsewhere
    }

    // List to store the selected recipients (mix of users, groups, rooms) from selection activity
    private List<ScheduledRecipientInfo> selectedRecipients = new ArrayList<>();
    private String selectedRecipientsNamesString = "No recipients selected"; // For displaying selected names in TextView
    // *** END NEW Data Structure ***


    // *** NEW Image Data/Launchers ***
    private Uri imageToScheduleUri; // URI of the selected image (can be temp camera file, for processing/cleanup)
    private String imageToScheduleBase64 = ""; // Base64 string of the image data (after processing, ready to schedule)

    // Activity Result Launchers for Image Selection
    private ActivityResultLauncher<Intent> pickImageLauncher; // For Gallery
    private ActivityResultLauncher<Uri> takePictureLauncher; // For Camera

    // Permission Launcher for Camera
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    // Constants for image processing
    private static final int MAX_IMAGE_SCHEDULE_SIZE = 1024; // Max dimension (e.g., 1024x1024)
    private static final int IMAGE_SCHEDULE_COMPRESSION_QUALITY = 85; // JPEG compression quality (e.g., 85%)
    private static final int REQUEST_CODE_PERMISSION = 101; // Standard permission request code

    // *** Add or confirm you have the FileProvider authority string defined somewhere accessible ***
    // It must match the authority in your AndroidManifest.xml.
    // Assuming it's defined as getApplicationContext().getPackageName() + ".fileprovider".
    // *** End NEW Image Data/Launchers ***


    // --- Activity Result Launcher for Recipient Selection ---
    private ActivityResultLauncher<Intent> selectRecipientsLauncher;
    // --- End Launcher ---


    // --- Firebase ---
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_msg);

        // --- Firebase Initialization ---
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        senderId = currentUser.getUid();


        // --- UI Initialization ---
        backArrowIcon = findViewById(R.id.back_arrow_icon);
        etMessage = findViewById(R.id.etMessage);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSchedule = findViewById(R.id.btnSchedule);
        btnSelectContacts = findViewById(R.id.btn_select_contacts);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        tvSelectedContacts = findViewById(R.id.tvSelectedContacts);

        // *** NEW Image related UI ***
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        imgPreview = findViewById(R.id.imgPreview);
        btnCancelImage = findViewById(R.id.btnCancelImage);
        btnPickGalleryImage = findViewById(R.id.btnPickGalleryImage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);

        // Initially hide the image preview area and cancel button
        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.GONE);
        if (btnCancelImage != null) btnCancelImage.setVisibility(View.GONE);
        // *** End NEW Image related UI ***


        etMessage.setMovementMethod(new ScrollingMovementMethod());


        // --- Initialize Activity Result Launchers ---
        initializeImagePickers(); // Initialize the launchers

        // Initialize Camera Permission Launcher
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted. Launching camera intent.");
                        launchCameraIntent(); // Call helper method to launch camera
                    } else {
                        Log.w(TAG, "Camera permission denied. Cannot take photo for scheduling.");
                        Toast.makeText(ScheduledMSG.this, "Camera permission denied. Cannot take photo.", Toast.LENGTH_SHORT).show();
                    }
                });

        // *** NEW: Register the Recipient Selection Launcher ***
        selectRecipientsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        ArrayList<String> ids = data.getStringArrayListExtra("selectedRecipientIds");
                        ArrayList<String> types = data.getStringArrayListExtra("selectedRecipientTypes");
                        ArrayList<String> names = data.getStringArrayListExtra("selectedRecipientNames");

                        processSelectedRecipientsResult(ids, types, names); // Call helper method

                    } else {
                        Log.d(TAG, "Recipient selection cancelled or failed.");
                        // selectedRecipients list remains unchanged
                    }
                });
        // *** END NEW Launcher Registration ***


        // --- Set Listeners ---
        backArrowIcon.setOnClickListener(v -> onBackPressed());

        // btnSelectContacts listener (MODIFIED)
        btnSelectContacts.setOnClickListener(v -> {
            Log.d(TAG, "Select Contacts button clicked. Launching Recipient Selection Activity.");
            // --- REPLACE the old dialog call ---
            // showContactSelectionDialog(); // REMOVE this old call

            // *** LAUNCH the new ScheduledRecipientSelectionActivity ***
            Intent intent = new Intent(ScheduledMSG.this, ScheduledRecipientSelectionActivity.class); // Use the new activity class
            // Optional: Pass existing selected recipients if you want to pre-select them
            // This requires the selection activity to accept and process this data on launch.
            // For simplicity now, we start fresh selection each time.
            selectRecipientsLauncher.launch(intent); // Launch using the launcher
            // *** END LAUNCH ***
        });


        btnPickTime.setOnClickListener(v -> openDateTimePicker());

        // *** NEW Image Button Listeners ***
        btnPickGalleryImage.setOnClickListener(v -> {
            Log.d(TAG, "Pick Gallery Image button clicked.");
            clearSelectedImage(); // Clear any existing image first
            openGalleryPicker(); // Call method to open gallery picker
        });

        btnTakePhoto.setOnClickListener(v -> {
            Log.d(TAG, "Take Photo button clicked.");
            clearSelectedImage(); // Clear any existing image first
            openCameraPicker(); // Call method to open camera picker
        });

        btnCancelImage.setOnClickListener(v -> clearSelectedImage()); // Listener for the image preview cancel button
        // *** End NEW Image Button Listeners ***


        // btnSchedule listener (MODIFIED)
        btnSchedule.setOnClickListener(v -> scheduleMessages()); // Call the main scheduling method


        // Battery optimization request (Keep this)
        requestBatteryOptimizationExemption();

        Log.d(TAG, "📲 onCreate finished in ScheduledMSG");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    // --- Method to open Date and Time Pickers ---
    private void openDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            new TimePickerDialog(this, (view1, selectedHour, selectedMinute) -> {
                Calendar selectedDateTime = Calendar.getInstance();
                selectedDateTime.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);

                if (selectedDateTime.getTimeInMillis() < System.currentTimeMillis()) {
                    Toast.makeText(ScheduledMSG.this, "Cannot schedule in the past.", Toast.LENGTH_SHORT).show();
                    selectedTimeMillis = -1; // Reset time
                    tvSelectedTime.setText("No time selected");
                } else {
                    selectedTimeMillis = selectedDateTime.getTimeInMillis();
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                    tvSelectedTime.setText("Scheduled for: " + dateTimeFormat.format(selectedDateTime.getTime()));
                    Log.d(TAG, "Selected time: " + selectedTimeMillis);
                }
            }, hour, minute, false).show();
        }, year, month, day).show();
    }


    // --- MODIFIED scheduleMessages method ---
    // This method now iterates through the selectedRecipients list
    // and creates a separate ScheMsg entry and WorkManager task for EACH recipient.
    // Inside ScheduledMSG.java

    // --- MODIFIED scheduleSingleMessage method ---
    // This method schedules a single ScheMsg entry for ONE recipient.
    // Added recipientType parameter.
    private void scheduleSingleMessage(String senderId, String content, long timeMillis, String messageType,
                                       ScheduledRecipientInfo recipientInfo, // *** Accept single recipient info ***
                                       String originalScheduledTimeFormatted) { // Keep original formatted time


        if (recipientInfo == null || TextUtils.isEmpty(recipientInfo.getId()) || TextUtils.isEmpty(recipientInfo.getType())) {
            Log.w(TAG, "scheduleSingleMessage called with null or incomplete recipient info. Skipping.");
            // Optionally show a toast indicating failure for this specific recipient if needed
            // runOnUiThread(() -> Toast.makeText(this, "Failed to schedule for a recipient due to missing info.", Toast.LENGTH_SHORT).show());
            return;
        }

        // Prepare lists containing info for this single recipient
        List<String> singleRecipientIdList = new ArrayList<>();
        singleRecipientIdList.add(recipientInfo.getId());

        List<String> singleRecipientNameList = new ArrayList<>();
        singleRecipientNameList.add(recipientInfo.getName() != null ? recipientInfo.getName() : "Unnamed");

        // Create the ScheMsg object with info for THIS recipient and their type
        ScheMsg scheduledMessage = new ScheMsg(
                senderId,
                singleRecipientIdList,  // List with one ID
                singleRecipientNameList,// List with one Name
                content,
                messageType,
                timeMillis,
                originalScheduledTimeFormatted,
                recipientInfo.getType() // *** PASS THE RECIPIENT TYPE HERE ***
        );
        scheduledMessage.setStatus("pending"); // Set initial status


        // Get a unique key for this scheduled entry in Firebase
        DatabaseReference scheduledRef = FirebaseDatabase.getInstance().getReference("ScheduledMessages");
        String msgId = scheduledRef.push().getKey();

        if (msgId == null) {
            Log.e(TAG, "Failed to generate Firebase push key for scheduled message for recipient: " + recipientInfo.getName());
            runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error scheduling message for " + recipientInfo.getName() + ".", Toast.LENGTH_SHORT).show());
            return; // Exit if ID generation fails
        }
        scheduledMessage.setMsgFirebaseId(msgId); // Set the generated Firebase key


        // Save this single scheduled message entry to Firebase
        scheduledRef.child(msgId).setValue(scheduledMessage)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Scheduled message entry saved to Firebase: " + msgId + " for recipient '" + recipientInfo.getName() + "' (Type: " + recipientInfo.getType() + ", MsgType: " + messageType + ")");

                    // Data for WorkManager (pass the Firebase key of this specific scheduled entry)
                    Data data = new Data.Builder()
                            .putString("msgId", msgId) // Pass the Firebase key of the scheduled entry
                            .build();

                    // Calculate delay based on selected time
                    long delay = timeMillis - System.currentTimeMillis();
                    if (delay < 0) {
                        Log.w(TAG, "Scheduled time is in the past (" + originalScheduledTimeFormatted + "). Setting delay to 0 for msgId " + msgId);
                        delay = 0;
                    }

                    // Schedule the WorkManager task for this specific entry
                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ScheduledMessageWorker.class)
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(data)
                            .addTag(msgId) // Add the msgId as a tag for potential cancellation
                            .build();

                    // Enqueue the WorkManager task
                    WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
                    Log.d(TAG, "WorkManager request enqueued for msgId " + msgId + " (Recipient: " + recipientInfo.getName() + ") with delay " + delay + "ms.");

                    // Success for THIS recipient is handled here.
                    // Overall success toast is shown after the loop in scheduleMessages().

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save scheduled message entry to Firebase for recipient '" + recipientInfo.getName() + "': " + msgId, e);
                    // Handle failure for THIS recipient
                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error scheduling message for " + recipientInfo.getName() + ".", Toast.LENGTH_SHORT).show());
                    // The ScheMsg entry might be left in Firebase with status="pending" or you could update status to "failed" here.
                    // For now, log the error.
                });
    }

    // --- MODIFIED scheduleMessages method ---
    // This method iterates through the selectedRecipients list
    // and calls scheduleSingleMessage for EACH recipient.
    private void scheduleMessages() {
        String messageText = etMessage.getText().toString().trim();
        boolean isImageStaged = !TextUtils.isEmpty(imageToScheduleBase64);

        // --- Validation ---
        // Check if EITHER text message is entered OR an image is selected
        if (TextUtils.isEmpty(messageText) && !isImageStaged) {
            Toast.makeText(this, "Please enter a message or select an image to schedule.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Schedule attempt failed: Empty text and no staged image.");
            return;
        }

        if (selectedTimeMillis == -1) {
            Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Schedule attempt failed: No time selected.");
            return;
        }

        if (selectedRecipients.isEmpty()) { // Use the new list
            Toast.makeText(this, "Please select at least one recipient.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Schedule attempt failed: No recipients selected.");
            return;
        }
        // --- End Validation ---

        // Format the scheduled time for display/storage in ScheMsg entry
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        String formattedTime = sdf.format(new Date(selectedTimeMillis));

        Log.d(TAG, "Initiating scheduling process.");

        boolean anyMessageScheduledAttempted = false;
        boolean textScheduled = false;
        boolean imageScheduled = false;


        // --- Loop through EACH selected recipient ---
        for (ScheduledRecipientInfo recipient : selectedRecipients) {
            if (recipient == null || TextUtils.isEmpty(recipient.getId()) || TextUtils.isEmpty(recipient.getType())) {
                Log.w(TAG, "Skipping scheduling for null or incomplete recipient info for a recipient.");
                // Optionally show a toast for this specific recipient's failure if needed
                continue; // Skip this recipient
            }

            // 1. Schedule Text Message if text is available
            if (!TextUtils.isEmpty(messageText)) {
                Log.d(TAG, "Scheduling text message for recipient: " + recipient.getName());
                // Call scheduleSingleMessage for the text message
                scheduleSingleMessage(senderId, messageText, selectedTimeMillis, "text", recipient, formattedTime);
                textScheduled = true;
                anyMessageScheduledAttempted = true;
            }

            // 2. Schedule Image Message if image is staged
            if (isImageStaged) {
                Log.d(TAG, "Scheduling image message for recipient: " + recipient.getName());
                // Call scheduleSingleMessage for the image message
                scheduleSingleMessage(senderId, imageToScheduleBase64, selectedTimeMillis, "image", recipient, formattedTime);
                imageScheduled = true;
                anyMessageScheduledAttempted = true;
            }
        } // End loop through selected recipients

        // --- After initiating scheduling attempts for all recipients and message types ---

        if (anyMessageScheduledAttempted) {
            Log.d(TAG, "Scheduling process initiated for all selected recipients and message types. Cleaning up UI.");
            // Clear the message input field and staged image after initiating sends
            if (textScheduled && etMessage != null) { // Only clear text if text was scheduled
                etMessage.setText("");
            }
            if (imageScheduled) { // Only clear image state if image was scheduled
                clearSelectedImage();
            }

            // Clear the selected recipients list and update the display TextView
            selectedRecipients.clear();
            updateSelectedContactsDisplay();
            selectedTimeMillis = -1; // Reset selected time
            tvSelectedTime.setText("No time selected"); // Update time display

            // Determine final toast message
            String scheduledItems = "";
            if(textScheduled && imageScheduled) scheduledItems = "text and image messages";
            else if (textScheduled) scheduledItems = "text messages"; // Pluralize
            else if (imageScheduled) scheduledItems = "image messages"; // Pluralize
            else scheduledItems = "message(s)"; // Fallback


            // Show a final toast confirming scheduling initiation
            String finalScheduledItems = scheduledItems;
            runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Scheduled " + finalScheduledItems + "!", Toast.LENGTH_SHORT).show());

            // Finish the activity
            finish();
        } else {
            Log.w(TAG, "No messages were scheduled attempted (either no recipients, no content, or all failed initial checks).");
            // If no messages were scheduled, do not clear inputs or finish the activity.
            runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Could not schedule message(s). Check input and recipients.", Toast.LENGTH_SHORT).show());
        }
    }
// --- END MODIFIED scheduleMessages ---



    // --- NEW: Process the Result from the Selection Activity ---
    /**
     * Processes the result data received from the ScheduledRecipientSelectionActivity,
     * updates the internal selectedRecipients list, and refreshes the UI display.
     *
     * @param ids   ArrayList of selected recipient IDs.
     * @param types ArrayList of selected recipient Types ("user", "group", "temporary_room").
     * @param names ArrayList of selected recipient Names.
     */
    private void processSelectedRecipientsResult(ArrayList<String> ids, ArrayList<String> types, ArrayList<String> names) {
        // Basic validation for the received data
        if (ids == null || types == null || names == null || ids.size() != types.size() || ids.size() != names.size()) {
            Log.e(TAG, "Received invalid data from Recipient Selection Activity.");
            runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error processing selection result.", Toast.LENGTH_SHORT).show());
            selectedRecipients.clear(); // Clear the list on invalid data
            updateSelectedContactsDisplay(); // Refresh UI display
            return;
        }

        // Clear the old list of selected recipients
        selectedRecipients.clear();

        // Populate the new list with the received data
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            String type = types.get(i);
            String name = names.get(i); // Use the name from the selection activity

            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(type)) {
                selectedRecipients.add(new ScheduledRecipientInfo(id, type, name != null ? name : (TextUtils.isEmpty(type) ? "Unnamed" : type + " - " + id.substring(0, Math.min(id.length(), 4)) + "..."))); // Add to the main list
            } else {
                Log.w(TAG, "Skipping recipient with empty ID or Type in selection result.");
            }
        }

        Log.d(TAG, "Selected recipients list updated. Total selected: " + selectedRecipients.size());

        // Update the UI display showing selected recipients
        updateSelectedContactsDisplay(); // Call method to refresh the TextView
    }
    // --- END NEW Process Result Method ---


    // --- MODIFIED Helper to update the text view showing selected recipients ---
    private void updateSelectedContactsDisplay() {
        if (selectedRecipients.isEmpty()) {
            tvSelectedContacts.setText("No recipients selected");
            tvSelectedContacts.setVisibility(View.GONE); // Hide the TextView if empty
            selectedRecipientsNamesString = "No recipients selected"; // Update the string member variable
        } else {
            // Build a string of names from the selectedRecipients list
            List<String> recipientNames = new ArrayList<>();
            for (ScheduledRecipientInfo recipient : selectedRecipients) {
                recipientNames.add(recipient.getName()); // Get the name from the info object
            }
            selectedRecipientsNamesString = TextUtils.join(", ", recipientNames); // Join the names

            tvSelectedContacts.setText("To: " + selectedRecipientsNamesString);
            tvSelectedContacts.setVisibility(View.VISIBLE); // Show the TextView if not empty
            Log.d(TAG, "Updated selected recipients display: " + selectedRecipientsNamesString);
        }
    }
    // --- END MODIFIED Helper ---


    // --- REMOVE OLD showContactSelectionDialog method ---
    /*
    private void showContactSelectionDialog() {
        // ... (Old dialog code) ...
        // This method is replaced by launching ScheduledRecipientSelectionActivity.
    }
     */


    // --- NEW Helper Method: Initialize Image Pickers (Launchers) ---
    private void initializeImagePickers() {
        Log.d(TAG, "Initializing Activity Result Launchers for image picking.");

        // Launcher for picking an image from the gallery
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        Log.d(TAG, "Image selected from gallery. URI: " + selectedImageUri);
                        processSelectedImage(selectedImageUri); // Call the method to process the image
                    } else {
                        Log.d(TAG, "Gallery image selection cancelled or failed. Result Code: " + result.getResultCode());
                        Toast.makeText(ScheduledMSG.this, "Image selection cancelled.", Toast.LENGTH_SHORT).show();
                        clearSelectedImage(); // Clear state on cancel/failure
                    }
                });
        Log.d(TAG, "Gallery image picker launcher registered.");


        // Launcher for taking a picture with the camera
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), // Contract specifically for taking a picture
                isSuccess -> { // Callback when camera activity returns (boolean indicates success)
                    if (isSuccess) {
                        // Picture was taken successfully and saved to the URI we provided (imageToScheduleUri)
                        Log.d(TAG, "Picture taken with camera. URI: " + imageToScheduleUri); // imageToScheduleUri was set before launching
                        processSelectedImage(imageToScheduleUri); // Call the method to process the captured image
                    } else {
                        // User cancelled or picture taking failed
                        Log.w(TAG, "Camera picture taking cancelled or failed.");
                        Toast.makeText(ScheduledMSG.this, "Camera operation cancelled or failed.", Toast.LENGTH_SHORT).show();
                        // Clean up the temporary URI we created if it wasn't used
                        clearSelectedImage(); // This method handles cleaning up the temporary URI and state
                    }
                });
        Log.d(TAG, "Camera take picture launcher registered.");

        // Camera Permission Launcher initialized in onCreate
    }
    // --- END NEW Helper Method initializeImagePickers ---


    // --- MODIFIED Helper Method: Open Gallery Picker ---
    private void openGalleryPicker() {
        Log.d(TAG, "Attempting to open gallery for scheduling.");

        // Check for READ_EXTERNAL_STORAGE permission if targeting older APIs (pre-Q)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Storage permission not granted. Requesting for gallery.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            Toast.makeText(this, "Storage permission required to open gallery. Please try again after granting.", Toast.LENGTH_LONG).show();
            return;
        }

        // Permission is granted or not needed, proceed to launch gallery
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent); // Launch using the launcher
            Log.d(TAG, "Gallery intent launched via launcher.");
        } catch (Exception e) {
            Log.e(TAG, "Error launching gallery intent.", e);
            Toast.makeText(this, "Error accessing gallery.", Toast.LENGTH_SHORT).show();
        }
    }
    // --- END MODIFIED Helper Method openGalleryPicker ---


    // --- MODIFIED Helper Method: Open Camera Picker ---
    private void openCameraPicker() {
        Log.d(TAG, "Attempting to open camera for scheduling.");

        // Check if the CAMERA permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted. Proceeding to launch camera intent.");
            launchCameraIntent(); // Call the helper method that uses takePictureLauncher
        } else {
            Log.d(TAG, "Camera permission not granted. Requesting permission.");
            // Permission is not granted, request it using the launcher
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    // --- END MODIFIED Helper Method openCameraPicker ---


    // --- NEW Helper Method: Launch Camera Intent (Called AFTER permission check) ---
    /**
     * Prepares a temporary URI using FileProvider and launches the camera intent
     * using the registered takePictureLauncher.
     * Requires createImageFile() helper method.
     */
    private void launchCameraIntent() {
        Log.d(TAG, "Preparing and launching camera intent.");
        try {
            // 1. Create a temporary File object where the camera should save the picture.
            File photoFile = null;
            try {
                photoFile = createImageFile(); // Use your helper to create temp file (ensure it exists in this class)
            } catch (IOException ex) {
                Log.e(TAG, "Error creating temp image file for camera", ex);
                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error preparing for photo.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Ensure state is clean
                return; // Exit if file creation fails
            }

            // 2. If the temporary file was created successfully, get its secure content:// URI using FileProvider.
            if (photoFile != null) {
                try {
                    // Get the secure content:// URI using FileProvider.
                    String fileProviderAuthority = getApplicationContext().getPackageName() + ".fileprovider"; // *** Use your actual authority ***

                    Uri photoURI = FileProvider.getUriForFile(
                            this, // Context
                            fileProviderAuthority, // *** YOUR FILEPROVIDER AUTHORITY STRING HERE ***
                            photoFile // The java.io.File object
                    );

                    // 3. Store this URI in the member variable imageToScheduleUri.
                    imageToScheduleUri = photoURI;
                    Log.d(TAG, "Prepared temporary URI for camera using FileProvider: " + imageToScheduleUri);

                    // 4. Create the camera intent (ACTION_IMAGE_CAPTURE).
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // 5. Specify where the camera app should save the picture.
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageToScheduleUri);

                    // 6. Grant necessary permissions to the camera app.
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // 7. Verify that there is a camera app available.
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        Log.d(TAG, "Starting camera intent with URI: " + imageToScheduleUri);
                        // 8. Launch the camera activity using the registered launcher.
                        takePictureLauncher.launch(imageToScheduleUri);

                    } else {
                        Log.e(TAG, "No camera app found to handle ACTION_IMAGE_CAPTURE.");
                        runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "No camera app found.", Toast.LENGTH_SHORT).show());
                        try { if (photoFile.exists()) photoFile.delete(); } catch (Exception e) { Log.w(TAG, "Failed to clean up camera file on no app found", e); }
                        clearSelectedImage(); // Clear state on failure
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "FileProvider setup error or file issue: " + e.getMessage(), e);
                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error accessing file storage. Check configuration.", Toast.LENGTH_LONG).show());
                    clearSelectedImage(); // Clear state on failure
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error setting up camera intent.", e);
                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error accessing camera.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state on failure
                }
            } else {
                Log.e(TAG, "Photo file was null after creation attempt. Cannot launch camera.");
                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error preparing for photo.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on failure
            }
        } catch (Exception e) {
            // This catch block should theoretically not be reached if inner catches handle specific errors,
            // but can serve as a final safety net.
            Log.e(TAG, "Outer catch: Unexpected error in launchCameraIntent.", e);
            runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show());
            clearSelectedImage();
        }
    }
    // --- END NEW Helper Method launchCameraIntent ---


    // --- NEW Helper Method: Process Selected/Captured Image ---
    /**
     * Processes the selected/captured image from the given URI (decode, resize, encode to Base64),
     * stores the resulting Base64 string, and updates the UI to show the image preview.
     * This method runs on a background thread for performance.
     * Requires resizeBitmap and encodeToBase64 helper methods.
     *
     * @param uri The content URI of the selected or captured image.
     */
    private void processSelectedImage(@Nullable Uri uri) {
        // --- Step 1: Clear any previously selected image state ---
        // This also handles cleanup of temporary camera files
        clearSelectedImage(); // Clear the previous state FIRST

        // Check if the URI is null after clearing
        if (uri == null) {
            Log.w(TAG, "processSelectedImage called with null URI after clearing previous state.");
            // Inform the user on the UI thread
            runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
            return; // Exit if the URI is null
        }

        Log.d(TAG, "Processing selected/captured image for scheduling. URI: " + uri);

        // --- Step 2: Perform image processing on a background thread ---
        new Thread(() -> {
            try {
                // a. Get Bitmap from URI: Use ContentResolver to open an InputStream and decode.
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap originalBitmap = null;
                if (inputStream != null) {
                    originalBitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close(); // Close the input stream
                }

                // Check if decoding was successful
                if (originalBitmap == null) {
                    Log.e(TAG, "Failed to get bitmap from URI: " + uri);
                    // Inform the user on the main thread
                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state if processing fails
                    return; // Exit if bitmap is null
                }

                Log.d(TAG, "Original image dimensions: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

                // b. Resize Bitmap: Resize the image to reduce size for scheduling/sending.
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SCHEDULE_SIZE, MAX_IMAGE_SCHEDULE_SIZE); // Use SCHEDULE size constants
                originalBitmap.recycle(); // Recycle the original bitmap

                // Check if resizing was successful
                if (resizedBitmap == null) {
                    Log.e(TAG, "Failed to resize bitmap.");
                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state if processing fails
                    return; // Exit if resizing fails
                }
                Log.d(TAG, "Resized image dimensions for scheduling: " + resizedBitmap.getWidth() + "x" + resizedBitmap.getHeight());


                // c. Encode resized Bitmap to Base64 string.
                String base64Image = encodeToBase64(resizedBitmap); // Use your helper method with compression quality
                resizedBitmap.recycle(); // Recycle the resized bitmap

                // Check if encoding was successful
                if (TextUtils.isEmpty(base64Image)) {
                    Log.e(TAG, "Encoded Base64 image content is empty!");
                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to encode image data.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage(); // Clear state if encoding fails
                    return; // Exit if encoding fails
                }

                // --- Step 3: Image Processed Successfully ---
                Log.d(TAG, "Image processed and Base64 stored. Length: " + base64Image.length() + ". Ready for scheduling. Showing preview.");

                // Store the resulting Base64 string in the member variable.
                ScheduledMSG.this.imageToScheduleBase64 = base64Image; // Use the stored Base64

                // Store the original URI too if it was a camera file that needs cleanup later
                // This check needs to match how you create temporary files for the camera.
                boolean isTempFileUri = false;
                // Check if the URI authority matches your FileProvider authority
                String fileProviderAuthority = getApplicationContext().getPackageName() + ".fileprovider"; // *** Use your actual authority ***
                if (uri != null && uri.getAuthority() != null && uri.getAuthority().equals(fileProviderAuthority)) {
                    isTempFileUri = true;
                } else if (uri != null && uri.getPath() != null && (uri.getPath().contains("/cache/") || uri.getPath().contains("/files/Pictures/"))) {
                    // Heuristic check for common temporary file locations within app storage
                }

                if (isTempFileUri) {
                    ScheduledMSG.this.imageToScheduleUri = uri; // Keep track of the temp URI for cleanup
                    Log.d(TAG, "Keeping temporary URI for cleanup: " + imageToScheduleUri);
                } else {
                    // This URI is from gallery or another non-temporary source, no need to track for cleanup
                    ScheduledMSG.this.imageToScheduleUri = null;
                }


                // --- Step 4: Update the UI to show the image preview (on Main Thread) ---
                runOnUiThread(() -> {
                    try {
                        // Use Glide to load the selected URI into the preview ImageView
                        if (imgPreview != null) {
                            Glide.with(ScheduledMSG.this) // Use Activity context
                                    .load(uri) // Load from the original URI
                                    .placeholder(R.drawable.image_placeholder_background) // Optional placeholder
                                    .error(R.drawable.ic_broken_image) // Optional error drawable
                                    .into(imgPreview);
                        }

                        // Make the image preview container and cancel button visible
                        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.VISIBLE);
                        if (btnCancelImage != null) btnCancelImage.setVisibility(View.VISIBLE);

                        // The text input EditText remains visible.

                        // Inform the user that the image is ready to be scheduled
                        Toast.makeText(ScheduledMSG.this, "Image ready to schedule.", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Log.e(TAG, "Error showing image preview on Main Thread", e);
                        // If showing preview fails, treat as processing failure and clear state
                        runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error showing image preview.", Toast.LENGTH_SHORT).show());
                        clearSelectedImage(); // Clear state if preview fails
                    }
                });


            } catch (IOException e) {
                // Handle file reading errors
                Log.e(TAG, "Image processing failed (IOException) for scheduling.", e);
                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on processing failure
            } catch (SecurityException e) {
                // Handle permission errors when accessing the URI
                Log.e(TAG, "Security Exception during image processing (missing permissions?).", e);
                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Permission error accessing image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on processing failure
            }
            catch (Exception e) {
                // Catch any other unexpected errors during processing (decoding, etc.)
                Log.e(TAG, "Unexpected error during image processing for scheduling.", e);
                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "An error occurred processing image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage(); // Clear state on processing failure
            }
        }).start(); // Start the background thread
    }
    // --- END NEW Helper Method processSelectedImage ---


    // --- NEW Helper Method: Clear Selected Image ---
    /**
     * Clears the selected image state, hides the image preview UI,
     * and cleans up any temporary camera file.
     */
    private void clearSelectedImage() {
        Log.d(TAG, "Clearing selected image.");

        // 1. Clear the Base64 data
        imageToScheduleBase64 = "";

        // 2. Clean up temporary camera file if it exists
        if (imageToScheduleUri != null) {
            try {
                // Ensure the URI is one we actually created and need to delete
                // Check against your FileProvider authority string.
                String fileProviderAuthority = getApplicationContext().getPackageName() + ".fileprovider"; // *** Use your actual authority ***
                if (imageToScheduleUri.getAuthority() != null && imageToScheduleUri.getAuthority().equals(fileProviderAuthority)) {
                    // It's a FileProvider URI from our app, safe to delete
                    getContentResolver().delete(imageToScheduleUri, null, null);
                    Log.d(TAG, "Cleaned up temporary camera file: " + imageToScheduleUri);
                } else {
                    // It's not a temp camera URI we created (e.g., it's from the gallery), no deletion needed
                    Log.d(TAG, "Selected image URI is not a recognized temporary file URI. Skipping cleanup.");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to delete temporary camera file.", e);
            }
        }

        // 3. Clear the URI reference
        imageToScheduleUri = null;

        // 4. Hide the image preview UI elements
        if (imagePreviewContainer != null) imagePreviewContainer.setVisibility(View.GONE);
        if (btnCancelImage != null) btnCancelImage.setVisibility(View.GONE);
        if (imgPreview != null) imgPreview.setImageDrawable(null); // Clear the ImageView content

        // The text input (etMessage) remains visible.
    }
    // --- END NEW Helper Method ---


    // --- Helper method to resize Bitmap (Keep This Helper Method) ---
    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null) return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float scale = Math.min(((float) maxWidth / width), ((float) maxHeight / height));

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    // --- End Helper Method resizeBitmap ---


    // --- Helper method to encode Bitmap to Base64 string (Keep This Helper Method) ---
    private String encodeToBase64(Bitmap image) {
        if (image == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, IMAGE_SCHEDULE_COMPRESSION_QUALITY, baos); // Use the constant
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT); // Use android.util.Base64
    }
    // --- End Helper Method encodeToBase64 ---


    // --- Helper method to create a temporary file for the camera photo (Keep This Helper Method) ---
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "SCHEDULED_TEMP_JPEG_" + timeStamp + "_"; // Prefix for scheduled temp images
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // Or getCacheDir() etc.
        if (storageDir == null) { Log.e(TAG, "getExternalFilesDir returned null"); throw new IOException("External files directory not available."); }
        if (!storageDir.exists()) { if (!storageDir.mkdirs()) { Log.e(TAG, "Failed to create directory: " + storageDir.getAbsolutePath()); throw new IOException("Failed to create directory for temporary files."); } }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "Created temporary image file for camera: " + image.getAbsolutePath());
        return image;
    }
    // --- End Helper Method createImageFile ---


    // --- Add onRequestPermissionsResult ---
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) { if (result != PackageManager.PERMISSION_GRANTED) { allPermissionsGranted = false; break; } }
            if (allPermissionsGranted) {
                Log.d(TAG, "Permissions granted.");
                Toast.makeText(this, "Permissions granted. You can now pick/take photos.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Permissions denied.");
                Toast.makeText(this, "Permissions denied. Cannot access storage/camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // --- Keep Battery Optimization Exemption Request ---
    public void requestBatteryOptimizationExemption() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                new AlertDialog.Builder(this)
                        .setTitle("Improve Scheduled Message Delivery")
                        .setMessage("To ensure scheduled messages send on time even when the app is closed, please allow CircleUp to run without battery restrictions. This will not significantly impact battery life.")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            try {
                                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to launch battery optimization settings intent", e);
                                Toast.makeText(this, "Could not open settings. Please manually find 'Battery Optimization' settings for CircleUp.", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(this, "Scheduled messages may be delayed if battery optimization is enabled.", Toast.LENGTH_LONG).show();
                        })
                        .show();
            } else {
                Log.d(TAG, "App is already ignoring battery optimizations.");
            }
        } else {
            Log.d(TAG, "Battery optimization request not applicable below API 23.");
        }
    }

    // --- MODIFIED onActivityResult ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Keep handling for Battery Optimization request
        if (requestCode == REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (pm.isIgnoringBatteryOptimizations(getPackageName())) {
                    Toast.makeText(this, "Background restrictions lifted for scheduled messages.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Background restrictions still active. Scheduled messages may be delayed.", Toast.LENGTH_LONG).show();
                }
            }
        }
        // --- REMOVE old image picker handling ---
        // Old logic for PICK_IMAGE_REQUEST is removed here.
        // The new ActivityResultLaunchers handle results for gallery and camera.
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the temporary camera file if it still exists
        if (imageToScheduleUri != null) {
            try {
                String fileProviderAuthority = getApplicationContext().getPackageName() + ".fileprovider"; // *** Use your actual authority ***
                if (imageToScheduleUri.getAuthority() != null && imageToScheduleUri.getAuthority().equals(fileProviderAuthority)) {
                    getContentResolver().delete(imageToScheduleUri, null, null);
                    Log.d(TAG, "Cleaned up temp camera file in onDestroy.");
                } else {
                    Log.w(TAG, "imageToScheduleUri did not look like a temp file URI. Skipping cleanup in onDestroy.");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to delete temporary camera file in onDestroy", e);
            } finally {
                imageToScheduleUri = null;
            }
        }
    }
}