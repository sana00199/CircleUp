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
import com.sana.circleup.one_signal_notification.OneSignalApiService;

import retrofit2.Retrofit; // Import Retrofit
import retrofit2.converter.gson.GsonConverterFactory; // Import GsonConverterFactory
// *** End Imports for OneSignal ***

//
//public class ScheduledMSG extends AppCompatActivity {
//
//    private EditText etMessage;
//    private Button btnPickTime, btnSchedule, btnSelectContacts;
//    private TextView tvSelectedTime;
//    private Toolbar toolbar;
//
//    private long selectedTimeMillis = -1;
//
//    private String senderId;
//    private ArrayList<String> selectedUserIds = new ArrayList<>();
//
//    private OneSignalApiService oneSignalApiService;
//    private static final String ONESIGNAL_APP_ID = "097ee789-8484-4078-bf38-9e89c321dc07";
//
//    private static final String TAG = "ScheduledMSG";
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_scheduled_msg);
//
//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("Schedule Message"); // Optional: Set title programmatically if not set in XML
//        }
//
//        etMessage = findViewById(R.id.etMessage);
//        btnPickTime = findViewById(R.id.btnPickTime);
//        btnSchedule = findViewById(R.id.btnSchedule);
//        tvSelectedTime = findViewById(R.id.tvSelectedTime);
//        btnSelectContacts = findViewById(R.id.btn_select_contacts);
//
//        etMessage.setMovementMethod(new ScrollingMovementMethod());
//
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//        senderId = currentUser.getUid();
//
//        btnSelectContacts.setOnClickListener(v -> {
//            showContactSelectionDialog();
//        });
//
//        btnPickTime.setOnClickListener(v -> openDateTimePicker());
//
//        btnSchedule.setOnClickListener(v -> {
//            String message = etMessage.getText().toString().trim();
//
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
//            scheduleMessage(senderId, message, selectedTimeMillis);
//        });
//
//        try {
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl("https://onesignal.com/")
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//            oneSignalApiService = retrofit.create(OneSignalApiService.class);
//            Log.d(TAG, "OneSignalApiService initialized in ScheduledMSG Activity.");
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to initialize OneSignalApiService in ScheduledMSG Activity", e);
//            Toast.makeText(this, "Error initializing notification service.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//
//    private void openDateTimePicker() {
//        Calendar calendar = Calendar.getInstance();
//        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
//            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
//                calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
//                selectedTimeMillis = calendar.getTimeInMillis();
//                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
//                tvSelectedTime.setText("Scheduled for: " + dateTimeFormat.format(calendar.getTime()));
//            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
//        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
//    }
//
//    private void scheduleMessage(String senderId, String message, long timeMillis) {
//        if (selectedUserIds.isEmpty()) {
//            Toast.makeText(this, "No contacts selected.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        DatabaseReference scheduledRef = FirebaseDatabase.getInstance().getReference("ScheduledMessages");
//        String msgId = scheduledRef.push().getKey();
//
//        if (msgId == null) {
//            Log.e(TAG, "Failed to generate Firebase push key for scheduled message.");
//            Toast.makeText(this, "Error scheduling message. Failed to generate ID.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        fetchReceiverNamesAndSchedule(senderId, msgId, message, timeMillis, new ArrayList<>(selectedUserIds));
//    }
//
//    private void fetchReceiverNamesAndSchedule(String senderId, String msgId, String message, long timeMillis, List<String> receiverIds) {
//        if (receiverIds.isEmpty()) {
//            Log.w(TAG, "Receiver IDs list is empty when fetching names. Cannot schedule.");
//            Toast.makeText(this, "No valid contacts selected.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
//        List<String> receiverNames = new ArrayList<>();
//        Map<String, String> receiverIdToNameMap = new HashMap<>();
//
//        final int[] namesFetchedCount = {0};
//        final int totalReceivers = receiverIds.size();
//
//        for (String receiverId : receiverIds) {
//            if (TextUtils.isEmpty(receiverId)) {
//                Log.w(TAG, "Skipping name fetch for empty receiverId.");
//                namesFetchedCount[0]++;
//                if (namesFetchedCount[0] == totalReceivers) {
//                    saveScheduledMessageAndScheduleWorker(senderId, msgId, message, timeMillis, receiverIds, new ArrayList<>(receiverNames), new HashMap<>(receiverIdToNameMap));
//                }
//                continue;
//            }
//
//            usersRef.child(receiverId).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    namesFetchedCount[0]++;
//
//                    String receiverName = snapshot.getValue(String.class);
//                    if (!TextUtils.isEmpty(receiverName)) {
//                        receiverNames.add(receiverName);
//                        receiverIdToNameMap.put(receiverId, receiverName);
//                    } else {
//                        receiverNames.add("Unknown User");
//                        receiverIdToNameMap.put(receiverId, "Unknown User");
//                        Log.w(TAG, "Receiver name not found for UID: " + receiverId + ". Using default.");
//                    }
//
//                    if (namesFetchedCount[0] == totalReceivers) {
//                        Log.d(TAG, "All receiver names fetched. Proceeding to save scheduled message and schedule worker.");
//                        saveScheduledMessageAndScheduleWorker(senderId, msgId, message, timeMillis, receiverIds, new ArrayList<>(receiverNames), new HashMap<>(receiverIdToNameMap));
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.e(TAG, "Failed to fetch receiver name for UID: " + receiverId + ".", error.toException());
//                    namesFetchedCount[0]++;
//
//                    receiverNames.add("Unknown User");
//                    receiverIdToNameMap.put(receiverId, "Unknown User");
//
//                    if (namesFetchedCount[0] == totalReceivers) {
//                        Log.d(TAG, "Finished fetching receiver names (with errors). Proceeding to save scheduled message and schedule worker.");
//                        saveScheduledMessageAndScheduleWorker(senderId, msgId, message, timeMillis, receiverIds, new ArrayList<>(receiverNames), new HashMap<>(receiverIdToNameMap));
//                    }
//                }
//            });
//        }
//    }
//
//    private void saveScheduledMessageAndScheduleWorker(String senderId, String msgId, String message, long timeMillis, List<String> receiverIds, List<String> receiverNames, Map<String, String> receiverIdToNameMap) {
//        DatabaseReference scheduledRef = FirebaseDatabase.getInstance().getReference("ScheduledMessages");
//
//        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
//        String formattedTime = sdf.format(new Date(timeMillis));
//
//        ScheMsg scheduledMessage = new ScheMsg(senderId, receiverIds, receiverNames, message, formattedTime);
//        scheduledMessage.setStatus("pending");
//        scheduledMessage.setMessageType("text");
//
//        scheduledRef.child(msgId).setValue(scheduledMessage)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "Scheduled message entry saved to Firebase: " + msgId);
//
//                    Data data = new Data.Builder().putString("msgId", msgId).build();
//                    long delay = timeMillis - System.currentTimeMillis();
//
//                    if (delay < 0) {
//                        Log.w(TAG, "Scheduled time is in the past. Setting delay to 0 for immediate execution.");
//                        delay = 0;
//                        runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Scheduled time is in the past, sending now.", Toast.LENGTH_SHORT).show());
//                    }
//
//                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ScheduledMessageWorker.class) // Replace with your Worker class name
//                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
//                            .setInputData(data)
//                            .build();
//
//                    WorkManager.getInstance(ScheduledMSG.this).enqueue(workRequest);
//                    Log.d(TAG, "WorkManager request enqueued for msgId " + msgId + " with delay " + delay + "ms.");
//
//                    runOnUiThread(() -> {
//                        Toast.makeText(ScheduledMSG.this, "Message scheduled!", Toast.LENGTH_SHORT).show();
//                        finish();
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Failed to save scheduled message entry to Firebase: " + msgId, e);
//                    runOnUiThread(() -> {
//                        Toast.makeText(ScheduledMSG.this, "Error saving scheduled message.", Toast.LENGTH_SHORT).show();
//                    });
//                });
//    }
//
//    private void showContactSelectionDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_contacts, null); // Ensure this layout exists and has RecyclerView with ID recycler_contacts and SearchView with ID dialog_search_view
//        builder.setView(dialogView);
//
//        SearchView dialogSearchView = dialogView.findViewById(R.id.dialog_search_view);
//        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_contacts);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        List<UserModel> dialogContactsList = new ArrayList<>();
//        ContactScheduleSelectAdapter adapter = new ContactScheduleSelectAdapter(this, dialogContactsList, selectedUserIds);
//        recyclerView.setAdapter(adapter);
//
//        if (dialogSearchView != null) {
//            dialogSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                @Override
//                public boolean onQueryTextSubmit(String query) {
//                    return false;
//                }
//
//                @Override
//                public boolean onQueryTextChange(String newText) {
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
//                        dialogContactsList.clear();
//
//                        if (!snapshot.exists()) {
//                            Log.d(TAG, "No accepted contacts found for dialog.");
//                            adapter.setContactList(new ArrayList<>());
//                            return;
//                        }
//
//                        List<String> friendIds = new ArrayList<>();
//                        for (DataSnapshot ds : snapshot.getChildren()) {
//                            String friendId = ds.getKey();
//                            if (friendId != null && !TextUtils.isEmpty(friendId)) {
//                                friendIds.add(friendId);
//                            }
//                        }
//                        Log.d(TAG, "Found " + friendIds.size() + " accepted contact UIDs for dialog.");
//
//                        if (friendIds.isEmpty()) {
//                            Log.d(TAG, "Friend IDs list is empty for dialog.");
//                            adapter.setContactList(new ArrayList<>());
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
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//                                Log.e(TAG, "Firebase Error fetching users for dialog contacts: " + error.getMessage(), error.toException());
//                                adapter.setContactList(new ArrayList<>());
//                                Toast.makeText(ScheduledMSG.this, "Error loading contacts.", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Log.e(TAG, "Firebase Database Error loading accepted contacts for dialog: " + error.getMessage(), error.toException());
//                        adapter.setContactList(new ArrayList<>());
//                        Toast.makeText(ScheduledMSG.this, "Error loading contacts.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//        builder.setPositiveButton("Done", (dialog, which) -> {
//            Log.d(TAG, "Contact selection dialog 'Done' clicked. Selected User IDs: " + selectedUserIds.toString());
//            Toast.makeText(this, selectedUserIds.size() + " contacts selected.", Toast.LENGTH_SHORT).show();
//        });
//
//        builder.setNegativeButton("Cancel", (dialog, which) -> {
//            Log.d(TAG, "Contact selection dialog 'Cancel' clicked.");
//            dialog.dismiss();
//        });
//
//        builder.show();
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
    private FrameLayout imagePreviewContainer;
    private ImageView imgPreview;
    private ImageButton btnCancelImage;
    private ImageButton btnPickGalleryImage, btnTakePhoto;

    // --- Data & State Variables ---
    private long selectedTimeMillis = -1;
    private String senderId;
    private ArrayList<String> selectedUserIds = new ArrayList<>();
    private ArrayList<String> selectedUserNames = new ArrayList<>(); // Store selected user names
    private String selectedUserNamesString = "No contacts selected"; // For displaying selected names

    private Uri imageToScheduleUri; // URI of the selected image (can be temp camera file)
    private String imageToScheduleBase64 = ""; // Base64 string of the image data (after processing, BEFORE encryption)


    // --- Firebase ---
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    // --- Activity Result Launchers for Image Picking/Taking ---
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    // --- Permission Launcher for Camera ---
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    // --- Constants for image processing ---
    private static final int MAX_IMAGE_SCHEDULE_SIZE = 1024; // Max dimension (e.g., 1024x1024)
    private static final int IMAGE_SCHEDULE_COMPRESSION_QUALITY = 85; // JPEG compression quality (e.g., 85%)

    // --- Permission Request Code (if still using ActivityCompat.requestPermissions elsewhere) ---
    private static final int REQUEST_CODE_PERMISSION = 101; // Example code


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_msg); // Ensure this is your new layout XML

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

        // Image related UI
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        imgPreview = findViewById(R.id.imgPreview);
        btnCancelImage = findViewById(R.id.btnCancelImage);
        btnPickGalleryImage = findViewById(R.id.btnPickGalleryImage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);


        etMessage.setMovementMethod(new ScrollingMovementMethod()); // Enable scrolling for text input

        // --- Set Listeners ---
        backArrowIcon.setOnClickListener(v -> onBackPressed());

        btnSelectContacts.setOnClickListener(v -> showContactSelectionDialog());

        btnPickTime.setOnClickListener(v -> openDateTimePicker());

        btnPickGalleryImage.setOnClickListener(v -> openGalleryPicker());
        btnTakePhoto.setOnClickListener(v -> openCameraPicker());

        btnCancelImage.setOnClickListener(v -> clearSelectedImage());


        btnSchedule.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();

            // --- Validation ---
            // Check if EITHER text message is entered OR an image is selected (Base64 has content)
            if (TextUtils.isEmpty(messageText) && TextUtils.isEmpty(imageToScheduleBase64)) {
                Toast.makeText(this, "Please enter a message or select an image to schedule.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTimeMillis == -1) {
                Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedUserIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one contact.", Toast.LENGTH_SHORT).show();
                return;
            }
            // --- End Validation ---

            // Determine content and type based on whether an image is selected
            // Content is NOT encrypted here for this non-E2EE version
            String contentToSend = TextUtils.isEmpty(imageToScheduleBase64) ? messageText : imageToScheduleBase64;
            String messageType = TextUtils.isEmpty(imageToScheduleBase64) ? "text" : "image";

            // Call the scheduling method
            scheduleMessage(senderId, contentToSend, selectedTimeMillis, messageType, selectedUserIds, selectedUserNames);
        });

        // --- Initialize Activity Result Launchers ---
        initializeImagePickers(); // Initialize the launchers

        // Initialize Permission Launcher (for Camera)
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted. Launching camera intent.");
                        launchCameraIntent(); // Call helper method to launch camera
                    } else {
                        Log.w(TAG, "Camera permission denied. Cannot take photo for scheduling.");
                        Toast.makeText(this, "Camera permission denied. Cannot take photo.", Toast.LENGTH_SHORT).show();
                    }
                });

        // OneSignal API Service initialization is NOT needed here (worker handles notifications)
    }

    // --- Method to handle the back arrow icon click ---
    public void onBackPressed() {
        super.onBackPressed(); // Go back to the previous screen
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
                    Toast.makeText(this, "Cannot schedule in the past.", Toast.LENGTH_SHORT).show();
                    selectedTimeMillis = -1; // Reset time
                    tvSelectedTime.setText("No time selected");
                } else {
                    selectedTimeMillis = selectedDateTime.getTimeInMillis();
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                    tvSelectedTime.setText("Scheduled for: " + dateTimeFormat.format(selectedDateTime.getTime()));
                    Log.d(TAG, "Selected time: " + selectedTimeMillis);
                }
            }, hour, minute, false).show(); // 'false' for 12-hour format, 'true' for 24-hour format
        }, year, month, day).show();
    }


    // --- Modified scheduleMessage method ---
    // This method saves ONE scheduled message entry with a LIST of recipients.
    private void scheduleMessage(String senderId, String content, long timeMillis, String messageType,
                                 ArrayList<String> receiverIds, ArrayList<String> receiverNames) { // Added receiverIds & receiverNames lists

        if (receiverIds.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "No contacts selected.", Toast.LENGTH_SHORT).show());
            return;
        }

        DatabaseReference scheduledRef = FirebaseDatabase.getInstance().getReference("ScheduledMessages");
        String msgId = scheduledRef.push().getKey(); // Get unique key immediately for this scheduled entry

        if (msgId == null) {
            Log.e(TAG, "Failed to generate Firebase push key for scheduled message.");
            runOnUiThread(() -> Toast.makeText(this, "Error scheduling message. Failed to generate ID.", Toast.LENGTH_SHORT).show());
            return;
        }

        // Format the scheduled time for display/storage
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        String formattedTime = sdf.format(new Date(timeMillis));

        // Create the ScheMsg object with ALL recipients and PLAIN content
        // This single ScheMsg object represents the task to send to all specified recipients.
        ScheMsg scheduledMessage = new ScheMsg(senderId, receiverIds, receiverNames, content, messageType, timeMillis, formattedTime);
        scheduledMessage.setMsgFirebaseId(msgId); // Set the generated Firebase key
        scheduledMessage.setStatus("pending"); // Ensure status is set

        // Save this single scheduled message entry to Firebase
        scheduledRef.child(msgId).setValue(scheduledMessage)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Scheduled message entry saved to Firebase: " + msgId + " for " + receiverIds.size() + " recipients.");

                    // Data for WorkManager
                    Data data = new Data.Builder()
                            .putString("msgId", msgId) // Pass the Firebase key of the scheduled entry
                            .build();

                    // Calculate delay
                    long delay = timeMillis - System.currentTimeMillis();
                    if (delay < 0) {
                        Log.w(TAG, "Scheduled time is in the past (" + formattedTime + "). Setting delay to 0.");
                        delay = 0;
                    }

                    // Schedule the WorkManager task
                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ScheduledMessageWorker.class)
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(data)
                            .addTag(msgId) // Add the msgId as a tag for potential cancellation
                            .build();

                    // Use application context to enqueue WorkManager
                    WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
                    Log.d(TAG, "WorkManager request enqueued for msgId " + msgId + " with delay " + delay + "ms.");


                    // Show Toast and finish on the main thread
                    runOnUiThread(() -> {
                        Toast.makeText(ScheduledMSG.this, "Message scheduled!", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after scheduling
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save scheduled message entry to Firebase: " + msgId, e);
                    // Show Toast on main thread
                    runOnUiThread(() -> {
                        Toast.makeText(ScheduledMSG.this, "Error saving scheduled message.", Toast.LENGTH_SHORT).show();
                    });
                });
        // fetchReceiverNamesAndSchedule is no longer needed here as names are fetched in the dialog
    }


    // --- Contact Selection Dialog ---
    // This method needs to fetch contacts, display them with checkboxes,
    // and update the 'selectedUserIds' and 'selectedUserNames' lists
    // when the user clicks "Done".
    private void showContactSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_contacts, null); // Ensure this layout exists
        builder.setView(dialogView);
        builder.setTitle("Select Contacts"); // Add a title

        SearchView dialogSearchView = dialogView.findViewById(R.id.dialog_search_view);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_contacts);
        TextView tvEmpty = dialogView.findViewById(R.id.tv_empty); // Assuming this ID exists in your dialog layout

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<UserModel> dialogContactsList = new ArrayList<>();
        // IMPORTANT: Pass the Activity's selectedUserIds list directly.
        // The adapter's internal logic MUST add/remove user IDs from this list
        // when checkboxes are clicked.
        ContactScheduleSelectAdapter adapter = new ContactScheduleSelectAdapter(this, dialogContactsList, selectedUserIds);
        recyclerView.setAdapter(adapter);

        if (dialogSearchView != null) {
            dialogSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { return false; }
                @Override public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
                    Log.d(TAG, "Dialog Search query changed: " + newText);
                    return true;
                }
            });
        } else {
            Log.w(TAG, "Dialog SearchView not found in dialog_select_contacts layout!");
        }

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference("Contacts").child(currentUid);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        Log.d(TAG, "Fetching accepted contacts for dialog...");

        contactsRef.orderByChild("request_type").equalTo("accepted")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> friendIds = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String friendId = ds.getKey(); // Contact ID is the friend's UID
                                if (friendId != null && !TextUtils.isEmpty(friendId)) {
                                    friendIds.add(friendId);
                                }
                            }
                        }
                        Log.d(TAG, "Found " + friendIds.size() + " accepted contact UIDs for dialog.");

                        if (friendIds.isEmpty()) {
                            Log.d(TAG, "Friend IDs list is empty. No contacts to show in dialog.");
                            adapter.setContactList(new ArrayList<>()); // Clear adapter list
                            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                            return;
                        }

                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                List<UserModel> fetchedUsers = new ArrayList<>();
                                Log.d(TAG, "Fetching details for " + friendIds.size() + " friends for dialog...");

                                for (String friendId : friendIds) {
                                    if (userSnapshot.hasChild(friendId)) {
                                        UserModel user = userSnapshot.child(friendId).getValue(UserModel.class);
                                        if (user != null) {
                                            user.setUserId(friendId); // Make sure UserModel has this setter
                                            fetchedUsers.add(user);
                                        } else {
                                            Log.w(TAG, "UserModel is null for friendId: " + friendId + " for dialog.");
                                        }
                                    } else {
                                        Log.w(TAG, "User data not found in /Users for friendId: " + friendId + " for dialog.");
                                    }
                                }
                                Log.d(TAG, "Loaded " + fetchedUsers.size() + " user models for dialog.");

                                dialogContactsList.clear();
                                dialogContactsList.addAll(fetchedUsers);

                                adapter.setContactList(new ArrayList<>(dialogContactsList)); // Pass the fetched list to the adapter

                                // Hide empty view if it was shown
                                if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Firebase Error fetching users for dialog contacts: " + error.getMessage(), error.toException());
                                adapter.setContactList(new ArrayList<>()); // Clear adapter list on error
                                if (tvEmpty != null) tvEmpty.setText("Error loading contacts.");
                                if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                                Toast.makeText(ScheduledMSG.this, "Error loading contacts.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase Database Error loading accepted contacts for dialog: " + error.getMessage(), error.toException());
                        dialogContactsList.clear(); // Clear original list
                        adapter.setContactList(new ArrayList<>());
                        if (tvEmpty != null) tvEmpty.setText("Error loading contacts.");
                        if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(ScheduledMSG.this, "Error loading contacts.", Toast.LENGTH_SHORT).show();
                    }
                });


        builder.setPositiveButton("Done", (dialog, which) -> {
            // The selectedUserIds list is now directly updated by the adapter's checkbox listeners.
            // We need to populate selectedUserNames based on the final selectedUserIds list.
            selectedUserNames.clear(); // Clear old names
            // You NEED a way to get the *list of UserModel objects that are currently selected* from the adapter.
            // The method `adapter.getCurrentList()` might give you the filtered list, but not necessarily which ones are checked.
            // The adapter needs to store the list of *selected* UserModel objects or at least their names.
            // Assuming your adapter has a method like `getSelectedUserModels()` or `getSelectedUserNames()`:
            List<UserModel> currentlySelectedUsers = adapter.getSelectedUserModels(); // <<< Assuming this method exists in your adapter
            if (currentlySelectedUsers != null) {
                for (UserModel user : currentlySelectedUsers) {
                    if (!TextUtils.isEmpty(user.getUsername())) {
                        selectedUserNames.add(user.getUsername());
                    } else {
                        selectedUserNames.add("Unknown"); // Fallback
                    }
                }
            }


            Log.d(TAG, "Contact selection dialog 'Done' clicked. Final Selected User IDs: " + selectedUserIds.toString() + ", Names: " + selectedUserNames.toString());
            updateSelectedContactsDisplay(); // Update the TextView display based on the final lists
            Toast.makeText(this, selectedUserIds.size() + " contacts selected.", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Log.d(TAG, "Contact selection dialog 'Cancel' clicked.");
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Helper to update the text view showing selected contacts
    private void updateSelectedContactsDisplay() {
        if (selectedUserIds.isEmpty()) {
            tvSelectedContacts.setText("No contacts selected");
            tvSelectedContacts.setVisibility(View.GONE);
            selectedUserNamesString = "No contacts selected"; // Reset the string
        } else {
            // We already have the selectedUserNames list populated from the dialog's "Done" button
            selectedUserNamesString = TextUtils.join(", ", selectedUserNames);
            tvSelectedContacts.setText("To: " + selectedUserNamesString);
            tvSelectedContacts.setVisibility(View.VISIBLE);
            Log.d(TAG, "Updated selected contacts display: " + selectedUserNamesString);
        }
    }


    // --- Initialize Activity Result Launchers ---
    private void initializeImagePickers() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        processSelectedImage(selectedImageUri);
                    } else {
                        Log.d(TAG, "Gallery image selection cancelled or failed.");
                        Toast.makeText(this, "Image selection cancelled.", Toast.LENGTH_SHORT).show();
                    }
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess) {
                        Log.d(TAG, "Picture taken with camera. URI: " + imageToScheduleUri);
                        processSelectedImage(imageToScheduleUri);
                    } else {
                        Log.w(TAG, "Camera picture taking cancelled or failed.");
                        Toast.makeText(this, "Camera operation cancelled or failed.", Toast.LENGTH_SHORT).show();
                        // Clean up the temporary URI if it wasn't used
                        if (imageToScheduleUri != null) {
                            try {
                                getContentResolver().delete(imageToScheduleUri, null, null);
                            } catch (Exception e) { Log.e(TAG, "Failed to delete temporary camera file after cancel/failure.", e); }
                        }
                        imageToScheduleUri = null; // Reset URI
                    }
                });
    }

    // --- Helper Method: Contains the logic to open the Gallery intent ---
    private void openGalleryPicker() {
        Log.d(TAG, "Attempting to open gallery for scheduling.");
        clearSelectedImage(); // Clear any existing selected image first
        // Permission check for READ_EXTERNAL_STORAGE is generally good here for older APIs
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            return; // Request permission, then user can try again after grant
        }
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent); // Launch gallery picker
        } catch (Exception e) {
            Log.e(TAG, "Error launching gallery intent for scheduling.", e);
            Toast.makeText(this, "Error accessing gallery.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Helper Method: Contains the logic to open the Camera intent (with permission check) ---
    private void openCameraPicker() {
        Log.d(TAG, "Attempting to open camera for scheduling.");
        clearSelectedImage(); // Clear any existing selected image first

        // Check CAMERA permission (and WRITE_EXTERNAL_STORAGE for older APIs)
        boolean cameraPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storagePermissionGranted = true; // Assume granted for Q+ for simplicity here, check explicitly below if needed
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storagePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }


        if (cameraPermissionGranted && storagePermissionGranted) {
            Log.d(TAG, "Camera and Storage permissions already granted. Proceeding to launch camera intent.");
            launchCameraIntent(); // Proceed
        } else {
            Log.d(TAG, "Missing Camera or Storage permission(s). Requesting.");
            List<String> permissionsToRequest = new ArrayList<>();
            if (!cameraPermissionGranted) permissionsToRequest.add(Manifest.permission.CAMERA);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !storagePermissionGranted) permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            // Request permissions using ActivityCompat
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_CODE_PERMISSION);
            // The camera intent will be launched in onRequestPermissionsResult if permission is granted
        }
    }

    // --- NEW Helper Method: Launch Camera Intent (Called AFTER permission check) ---
    private void launchCameraIntent() {
        Log.d(TAG, "Attempting to launch camera intent.");
        try {
            File photoFile = null;
            try {
                photoFile = createImageFile(); // Use helper to create temp file
            } catch (IOException ex) {
                Log.e(TAG, "Error creating temp image file for camera scheduling", ex);
                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error preparing for photo.", Toast.LENGTH_SHORT).show());
                return;
            }

            if (photoFile != null) {
                imageToScheduleUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", photoFile); // *** Use your FileProvider authority ***
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageToScheduleUri); // Output to the created URI
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Verify that the camera app exists and can handle this intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    Log.d(TAG, "Starting camera intent with URI: " + imageToScheduleUri);
                    // Use the launcher with the URI
                    takePictureLauncher.launch(imageToScheduleUri); // Corrected to use imageToScheduleUri
                } else {
                    Log.e(TAG, "No camera app found to handle ACTION_IMAGE_CAPTURE.");
                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "No camera app found.", Toast.LENGTH_SHORT).show());
                    // Clean up the created file if intent cannot be resolved
                    try{ if(photoFile.exists()) photoFile.delete(); } catch (Exception e) { Log.w(TAG, "Failed to clean up camera file on no app found", e); }
                    imageToScheduleUri = null; // Reset URI on failure
                }
            } else {
                Log.e(TAG, "Photo file was null after creation attempt.");
                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error preparing for photo.", Toast.LENGTH_SHORT).show());
            }

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error setting up camera intent for scheduling.", e);
            Toast.makeText(this, "Error accessing camera.", Toast.LENGTH_SHORT).show();
            imageToScheduleUri = null;
        }
    }


    // --- NEW Helper Method: Process Selected/Captured Image ---
    // Processes the image (resize, encode to Base64) and stores it in imageToScheduleBase64
    private void processSelectedImage(@Nullable Uri uri) {
        if (uri == null) {
            Log.w(TAG, "processSelectedImage called with null URI.");
            clearSelectedImage();
            runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
            return;
        }

        Log.d(TAG, "Processing selected/captured image for scheduling. URI: " + uri);

        // Run image processing on a background thread
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap originalBitmap = null;
                if (inputStream != null) {
                    originalBitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                }

                if (originalBitmap == null) {
                    Log.e(TAG, "Failed to get bitmap from URI: " + uri);
                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to load image.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage();
                    return;
                }

                Log.d(TAG, "Original image dimensions: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

                // Resize Bitmap
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SCHEDULE_SIZE, MAX_IMAGE_SCHEDULE_SIZE);
                originalBitmap.recycle();

                if (resizedBitmap == null) {
                    Log.e(TAG, "Failed to resize bitmap.");
                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage();
                    return;
                }
                Log.d(TAG, "Resized image dimensions: " + resizedBitmap.getWidth() + "x" + resizedBitmap.getHeight());


                // Encode resized Bitmap to Base64 (This is the content to save/send)
                String base64Image = encodeToBase64(resizedBitmap);
                resizedBitmap.recycle();

                if (TextUtils.isEmpty(base64Image)) {
                    Log.e(TAG, "Encoded Base64 image content is empty!");
                    runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to encode image data.", Toast.LENGTH_SHORT).show());
                    clearSelectedImage();
                    return;
                }

                // --- Image Processed Successfully ---
                imageToScheduleBase64 = base64Image; // Store the Base64 string as content

                // Store the original URI too if it was a camera file that needs cleanup later
                // Check if this URI is a temporary file URI managed by FileProvider/MediaStore
                // A simple check could be if the URI authority matches your FileProvider authority
                // Or if it's in the app's external files/cache directory
                boolean isTempFileUri = false;
                if (uri != null && uri.getAuthority() != null && uri.getAuthority().equals(getApplicationContext().getPackageName() + ".fileprovider")) { // Check FileProvider authority
                    isTempFileUri = true;
                } else if (uri != null && uri.getPath() != null && (uri.getPath().contains("/cache/") || uri.getPath().contains("/files/Pictures/"))) { // Heuristic check for common temp locations
                    // This check might be less reliable
                }

                if (isTempFileUri) {
                    imageToScheduleUri = uri; // Keep track of the temp URI for cleanup
                    Log.d(TAG, "Keeping temporary URI for cleanup: " + imageToScheduleUri);
                } else {
                    // This URI is from gallery or unknown source, no need to track for cleanup
                    imageToScheduleUri = null;
                }


                Log.d(TAG, "Image processed and Base64 stored. Ready for scheduling. Showing preview.");

                // --- Show the image preview and hide text input (on Main Thread) ---
                runOnUiThread(() -> {
                    try {
                        // Use Glide to load the selected URI into the preview ImageView
                        Glide.with(ScheduledMSG.this)
                                .load(uri) // Load from the original URI for better quality in preview
                                .placeholder(R.drawable.image_placeholder_background) // Optional placeholder
                                .into(imgPreview);

                        imagePreviewContainer.setVisibility(View.VISIBLE); // Show the container
                        btnCancelImage.setVisibility(View.VISIBLE); // Show the cancel button
                        etMessage.setVisibility(View.GONE); // Hide the text input field
                        etMessage.setText(""); // Clear text input just in case
                        etMessage.setHint(""); // Clear hint

                        Toast.makeText(ScheduledMSG.this, "Image selected for scheduling.", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Log.e(TAG, "Error showing image preview on Main Thread", e);
                        runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Error showing image preview.", Toast.LENGTH_SHORT).show());
                        clearSelectedImage(); // Clear state if preview fails
                    }
                });


            } catch (IOException e) {
                Log.e(TAG, "Image processing failed (IOException) for scheduling.", e);
                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Failed to process image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage();
            } catch (SecurityException e) { // Catch potential SecurityException on getContentResolver().openInputStream()
                Log.e(TAG, "Security Exception during image processing (missing permissions?).", e);
                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "Permission error accessing image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage();
            }
            catch (Exception e) { // Catch any other unexpected errors during processing
                Log.e(TAG, "Unexpected error during image processing for scheduling.", e);
                runOnUiThread(() -> Toast.makeText(ScheduledMSG.this, "An error occurred processing image.", Toast.LENGTH_SHORT).show());
                clearSelectedImage();
            }
        }).start(); // Start the background thread
    }

    // --- NEW Helper Method: Clear Selected Image ---
    private void clearSelectedImage() {
        Log.d(TAG, "Clearing selected image.");
        imageToScheduleBase64 = ""; // Clear the Base64 data
        // Clean up temporary camera file if it exists
        if (imageToScheduleUri != null) {
            try {
                // Ensure the URI is one we actually created and need to delete
                if (imageToScheduleUri.getAuthority() != null && imageToScheduleUri.getAuthority().equals(getApplicationContext().getPackageName() + ".fileprovider")) {
                    getContentResolver().delete(imageToScheduleUri, null, null);
                    Log.d(TAG, "Cleaned up temporary camera file: " + imageToScheduleUri);
                } else {
                    Log.w(TAG, "imageToScheduleUri did not look like a temp file URI. Skipping cleanup.");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to delete temporary camera file.", e);
            }
        }
        imageToScheduleUri = null; // Clear the URI reference

        // Hide the image preview and show text input
        imagePreviewContainer.setVisibility(View.GONE);
        btnCancelImage.setVisibility(View.GONE);
        imgPreview.setImageDrawable(null); // Clear the ImageView content
        etMessage.setVisibility(View.VISIBLE); // Show text input
        etMessage.setText(""); // Clear text input too, just in case
        etMessage.setHint("Enter your message"); // Restore hint
    }


    // --- Add these helper methods from ChatPageActivity for image processing and camera file creation ---
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

    private String encodeToBase64(Bitmap image) {
        if (image == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, IMAGE_SCHEDULE_COMPRESSION_QUALITY, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // You NEED FileProvider configured for this method to work correctly with camera
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "SCHEDULED_TEMP_JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // Or getCacheDir() etc.
        if (storageDir == null) { Log.e(TAG, "getExternalFilesDir returned null"); throw new IOException("External files directory not available."); }
        if (!storageDir.exists()) { if (!storageDir.mkdirs()) { Log.e(TAG, "Failed to create directory: " + storageDir.getAbsolutePath()); throw new IOException("Failed to create directory for temporary files."); } }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "Created temporary image file for camera: " + image.getAbsolutePath());
        return image;
    }
    // --- End Image Processing Helpers ---


    // --- Add onRequestPermissionsResult for image permissions ---
    // This is needed if you use ActivityCompat.requestPermissions directly.
    // If you only use ActivityResultLaunchers, the result is handled in their callbacks.
    // Since openCameraPicker might use ActivityCompat, let's keep this.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) { if (result != PackageManager.PERMISSION_GRANTED) { allPermissionsGranted = false; break; } }
            if (allPermissionsGranted) {
                Log.d(TAG, "Permissions granted.");
                // If the user was trying to open the camera and permission was just granted,
                // you might want to re-attempt launching the camera here.
                // However, often users click the button again.
                // If the launcher was used, its callback already handles this.
            } else {
                Log.d(TAG, "Permissions denied.");
                Toast.makeText(this, "Permissions denied. Cannot access storage/camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void requestBatteryOptimizationExemption() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if the app is NOT already ignoring optimizations
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                new AlertDialog.Builder(this)
                        .setTitle("Improve Scheduled Message Delivery")
                        .setMessage("To ensure scheduled messages send on time even when the app is closed, please allow CircleUp to run without battery restrictions. This will not significantly impact battery life.")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            try {
                                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS); // Use startActivityForResult
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
                // App is already optimized, maybe show a confirmation or nothing
            }
        } else {
            // For versions below M, battery optimization is less aggressive or not an API concern
            Log.d(TAG, "Battery optimization request not applicable below API 23.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        // ... handle other activity results (like image picker)


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the temporary camera file if it still exists
        if (imageToScheduleUri != null) {
            try {
                // Ensure the URI is one we actually created and need to delete
                if (imageToScheduleUri.getAuthority() != null && imageToScheduleUri.getAuthority().equals(getApplicationContext().getPackageName() + ".fileprovider")) {
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