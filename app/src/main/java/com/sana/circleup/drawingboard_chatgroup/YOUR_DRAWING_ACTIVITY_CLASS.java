//package com.sana.circleup.drawingboard_chatgroup; // <<< USE YOUR ACTUAL PACKAGE NAME
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.content.res.ColorStateList;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ServerValue;
//import com.google.firebase.database.ValueEventListener;
//import com.sana.circleup.Login; // <<< Replace with your actual Login Activity
//import com.sana.circleup.R; // <<< USE YOUR ACTUAL R FILE
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//
//public class YOUR_DRAWING_ACTIVITY_CLASS extends AppCompatActivity implements DrawingView.OnStrokeCompleteListener {
//
//    private static final String TAG = "DrawingActivity";
//
//    // --- UI Elements ---
//    private Toolbar toolbar;
//    private TextView toolbarTitle;
//    private RecyclerView activeUsersRecyclerView;
//    private DrawingView drawingView;
//    private TextView loadingIndicator;
//    private TextView sessionEndedIndicator;
//    private ViewGroup drawingToolsLayout;
//
//    // Drawing Tool Buttons
//    private Button btnColorBlack, btnColorRed, btnColorBlue, btnColorGreen, btnColorYellow, btnColorPurple;
//    private TextView btnStrokeSmall, btnStrokeMedium, btnStrokeLarge; // <<<
//    private ImageButton btnToolEraser;
//
//    // Initiator Action Buttons
//    private Button btnClearDrawing, btnEndSession;
//
//    // --- Firebase ---
//    private FirebaseAuth auth;
//    private DatabaseReference rootRef;
//    private DatabaseReference usersRef;
//    private DatabaseReference groupRef;
//    private DatabaseReference drawingSessionRef;
//    private DatabaseReference strokesRef;
//    private DatabaseReference sessionStateRef;
//    private DatabaseReference activeSessionUsersRef;
//
//
//    // --- Session Info ---
//    private String groupId;
//    private String sessionId;
//    private String currentUserID;
//    private String currentUserName;
//    private String sessionStarterId;
//    private String currentSessionState = "loading";
//
//    // --- Firebase Listeners ---
//    private ChildEventListener strokesChildEventListener;
//    private ValueEventListener sessionStateListener;
//    private ChildEventListener activeUsersListener;
//
//    // --- Adapter for Active Users RecyclerView ---
//    private ActiveUserAdapter activeUserAdapter;
//    private List<ActiveUser> activeUserList = new ArrayList<>();
//
//    // --- Activity's State Variables for Tool Selection Visual Feedback ---
//    // These track the currently selected tool properties in the Activity
//    private int currentActivityColor = Color.BLACK; // Corrected name
//    private float currentActivityStrokeWidth = 12f; // Corrected name
//    private int currentActivityTool = DrawingView.TOOL_PEN; // Corrected name
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_your_drawing_class); // <<< Use your actual layout file name
//
//        Log.d(TAG, "🎨 YOUR_DRAWING_ACTIVITY_CLASS launched");
//
//        auth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = auth.getCurrentUser();
//
//        if (currentUser == null) {
//            Log.w(TAG, "No current user authenticated. Redirecting to Login.");
//            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
//            sendUserToLoginActivity();
//            return;
//        }
//        currentUserID = currentUser.getUid();
//
//        groupId = getIntent().getStringExtra("groupId");
//        sessionId = getIntent().getStringExtra("sessionId");
//
//        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(sessionId)) {
//            Log.e(TAG, "CRITICAL ERROR: Group ID or Session ID missing from Intent!");
//            Toast.makeText(this, "Error: Drawing session information missing!", Toast.LENGTH_LONG).show();
//            finish();
//            return;
//        }
//        Log.d(TAG, "Received Group ID: " + groupId + ", Session ID: " + sessionId);
//
//        rootRef = FirebaseDatabase.getInstance().getReference();
//        usersRef = rootRef.child("Users");
//        groupRef = rootRef.child("Groups").child(groupId);
//        drawingSessionRef = groupRef.child("drawingSessions").child(sessionId);
//        strokesRef = drawingSessionRef.child("strokes");
//        sessionStateRef = drawingSessionRef.child("state");
//        activeSessionUsersRef = drawingSessionRef.child("activeUsers");
//
//        InitializeFields();
//
//        if (drawingView != null) {
//            drawingView.setOnStrokeCompleteListener(this);
//            // Set initial state on DrawingView (should match Activity defaults)
//            drawingView.setCurrentColor(currentActivityColor);
//            drawingView.setCurrentStrokeWidth(currentActivityStrokeWidth);
//            drawingView.setTool(currentActivityTool);
//        } else {
//            Log.e(TAG, "drawingView is null. Cannot set stroke listener or initial state!");
//        }
//
//        GetUserInfo();
//        loadSessionInfoAndListeners();
//        setupActiveUsersRecyclerView();
//        setupPresence();
//        setupDrawingToolListeners();
//        loadExistingStrokesAndListen();
//
//        // --- Set initial visual state of tool buttons ---
//        // Called after InitializeFields and setting initial state on DrawingView
//        updateColorButtonState();
//        updateStrokeSizeButtonState();
//        updateToolButtonState();
//
//
//        Log.d(TAG, "📲 onCreate finished.");
//    }
//
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.d(TAG, "YOUR_DRAWING_ACTIVITY_CLASS onStart");
//        setupPresence();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.d(TAG, "YOUR_DRAWING_ACTIVITY_CLASS onStop");
//        goOffline();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "YOUR_DRAWING_ACTIVITY_CLASS onDestroy called.");
//        removeFirebaseListeners();
//        goOffline();
//    }
//
//
//    private void sendUserToLoginActivity() {
//        Log.d(TAG, "Redirecting to Login Activity.");
//        Intent loginIntent = new Intent(YOUR_DRAWING_ACTIVITY_CLASS.this, Login.class);
//        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(loginIntent);
//        finish();
//    }
//
//
//    private void InitializeFields() {
//        Log.d(TAG, "Initializing UI elements.");
//
//        toolbar = findViewById(R.id.drawing_toolbar);
//        if (toolbar == null) Log.e(TAG, "Toolbar not found!");
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//        toolbarTitle = findViewById(R.id.toolbar_title);
//        if (toolbarTitle == null) Log.e(TAG, "Toolbar title TextView not found!");
//
//        activeUsersRecyclerView = findViewById(R.id.active_users_recycler_view);
//        if (activeUsersRecyclerView == null) Log.e(TAG, "Active Users RecyclerView not found!");
//
//        drawingView = findViewById(R.id.drawing_board);
//        if (drawingView == null) Log.e(TAG, "DrawingView (drawing_board) not found!");
//        loadingIndicator = findViewById(R.id.drawing_loading_indicator);
//        if (loadingIndicator == null) Log.e(TAG, "Loading Indicator TextView not found!");
//        sessionEndedIndicator = findViewById(R.id.session_ended_indicator);
//        if (sessionEndedIndicator == null) Log.e(TAG, "Session Ended Indicator TextView not found!");
//
//        drawingToolsLayout = findViewById(R.id.drawing_tools_layout);
//        if (drawingToolsLayout == null) Log.e(TAG, "Drawing Tools Layout not found!");
//
//        btnColorBlack = findViewById(R.id.btn_color_black); if (btnColorBlack == null) Log.e(TAG, "btnColorBlack not found!");
//        btnColorRed = findViewById(R.id.btn_color_red); if (btnColorRed == null) Log.e(TAG, "btnColorRed not found!");
//        btnColorBlue = findViewById(R.id.btn_color_blue); if (btnColorBlue == null) Log.e(TAG, "btnColorBlue not found!");
//        btnColorGreen = findViewById(R.id.btn_color_green); if (btnColorGreen == null) Log.e(TAG, "btnColorGreen not found!");
//        btnColorYellow = findViewById(R.id.btn_color_yellow); if (btnColorYellow == null) Log.e(TAG, "btnColorYellow not found!");
//        btnColorPurple = findViewById(R.id.btn_color_PURPLE); if (btnColorPurple == null) Log.e(TAG, "btnColorPurple not found!"); // Assuming this ID
//
//        btnStrokeSmall = findViewById(R.id.btn_stroke_small); if (btnStrokeSmall == null) Log.e(TAG, "btnStrokeSmall not found!");
//        btnStrokeMedium = findViewById(R.id.btn_stroke_medium); if (btnStrokeMedium == null) Log.e(TAG, "btnStrokeMedium not found!");
//        btnStrokeLarge = findViewById(R.id.btn_stroke_large); if (btnStrokeLarge == null) Log.e(TAG, "btnStrokeLarge not found!");
//
//        btnToolEraser = findViewById(R.id.btn_tool_eraser); if (btnToolEraser == null) Log.e(TAG, "btnToolEraser not found!");
//
//        btnClearDrawing = findViewById(R.id.btn_clear_drawing); if (btnClearDrawing == null) Log.e(TAG, "btnClearDrawing not found!");
//        btnEndSession = findViewById(R.id.btn_end_session); if (btnEndSession == null) Log.e(TAG, "btnEndSession not found!");
//
//        btnClearDrawing.setVisibility(View.GONE);
//        btnEndSession.setVisibility(View.GONE);
//
//        setDrawingEnabled(false);
//
//        loadingIndicator.setVisibility(View.GONE);
//        sessionEndedIndicator.setVisibility(View.GONE);
//
//        Log.d(TAG, "InitializeFields finished.");
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
//    private void GetUserInfo() {
//        if (usersRef == null || currentUserID == null || currentUserID.isEmpty()) {
//            Log.e(TAG, "GetUserInfo: usersRef or currentUserID is null/empty.");
//            currentUserName = "Unknown";
//            return;
//        }
//        usersRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists() && snapshot.hasChild("username")) {
//                    currentUserName = snapshot.child("username").getValue(String.class);
//                    if (TextUtils.isEmpty(currentUserName)) {
//                        currentUserName = "Unknown";
//                        Log.w(TAG, "Username node found but value is null/empty for user: " + currentUserID);
//                    }
//                    Log.d(TAG, "Fetched current username: " + currentUserName);
//                } else {
//                    currentUserName = "Unknown";
//                    Log.w(TAG, "Username not found in Firebase for current user: " + currentUserID);
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Error retrieving current user data: " + error.getMessage());
//                currentUserName = "Unknown";
//            }
//        });
//    }
//
//
//    @SuppressLint("RestrictedApi")
//    private void loadSessionInfoAndListeners() {
//        if (drawingSessionRef == null || sessionStateRef == null) {
//            Log.e(TAG, "loadSessionInfoAndListeners: drawingSessionRef or sessionStateRef is null. Cannot load session info.");
//            Toast.makeText(this, "Error loading session data.", Toast.LENGTH_SHORT).show();
//            setDrawingEnabled(false);
//            return;
//        }
//        Log.d(TAG, "Loading session info and setting up state listener for session: " + sessionId);
//
//        sessionStateListener = drawingSessionRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    sessionStarterId = snapshot.child("starterId").getValue(String.class);
//                    String sessionState = snapshot.child("state").getValue(String.class);
//                    YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState = (sessionState != null) ? sessionState : "active";
//
//                    Log.d(TAG, "Session info updated. State: " + YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState + ", Starter ID: " + sessionStarterId);
//
//                    updateUIBasedOnSessionState(YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState);
//
//                    boolean isInitiator = currentUserID != null && Objects.equals(currentUserID, sessionStarterId);
//                    if (btnClearDrawing != null) {
//                        btnClearDrawing.setVisibility(isInitiator ? View.VISIBLE : View.GONE);
//                        btnClearDrawing.setEnabled(isInitiator && "active".equals(YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState));
//                    }
//                    if (btnEndSession != null) {
//                        btnEndSession.setVisibility(isInitiator ? View.VISIBLE : View.GONE);
//                        btnEndSession.setEnabled(isInitiator && "active".equals(YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState));
//                    }
//
//                } else {
//                    Log.w(TAG, "Drawing session node does not exist in Firebase: " + sessionId + ". Marking as ended.");
//                    Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Drawing session ended or deleted.", Toast.LENGTH_LONG).show();
//                    YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState = "ended";
//                    updateUIBasedOnSessionState(YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState);
//                }
//            }
//            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Firebase Session Listener cancelled for session " + sessionId + ": " + error.getMessage(), error.toException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to sync session status.", Toast.LENGTH_LONG).show(); YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState = "ended"; updateUIBasedOnSessionState(YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState); }
//        });
//        Log.d(TAG, "Firebase session data listener attached to: " + drawingSessionRef.getPath());
//    }
//
//    private void updateUIBasedOnSessionState(String state) {
//        boolean isInitiator = currentUserID != null && Objects.equals(currentUserID, sessionStarterId);
//        boolean enableDrawing = "active".equals(state);
//
//        setDrawingEnabled(enableDrawing);
//
//        if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
//        if (sessionEndedIndicator != null) {
//            sessionEndedIndicator.setVisibility(enableDrawing ? View.GONE : View.VISIBLE);
//        }
//        if (toolbarTitle != null) {
//            toolbarTitle.setText(enableDrawing ? "Shared Drawing" : "Shared Drawing (Ended)");
//        }
//
//        if (btnClearDrawing != null) {
//            btnClearDrawing.setEnabled(isInitiator && enableDrawing);
//        }
//        if (btnEndSession != null) {
//            btnEndSession.setEnabled(isInitiator && enableDrawing);
//        }
//        // Update visual state of tool buttons based on enabled state
//        updateColorButtonState(); // These methods check enabled state internally or rely on setDrawingEnabled
//        updateStrokeSizeButtonState();
//        updateToolButtonState();
//
//        Log.d(TAG, "UI updated for state: " + state + ", Drawing Enabled: " + enableDrawing);
//    }
//
//    private void setDrawingEnabled(boolean enabled) {
//        if (drawingView != null) {
//            drawingView.setEnabled(enabled);
//        }
//        if (drawingToolsLayout != null) {
//            for (int i = 0; i < drawingToolsLayout.getChildCount(); i++) {
//                View child = drawingToolsLayout.getChildAt(i);
//                child.setEnabled(enabled); // Enable/disable all children
//            }
//            // Initiator buttons enabled state handled in updateUIBasedOnSessionState
//        } else {
//            Log.w(TAG, "drawingToolsLayout is null, cannot enable/disable tools.");
//        }
//        Log.d(TAG, "Drawing input and tools ENABLED: " + enabled);
//    }
//
//
//    @SuppressLint("RestrictedApi")
//    private void setupActiveUsersRecyclerView() {
//        if (activeUsersRecyclerView == null || activeSessionUsersRef == null || usersRef == null) {
//            Log.e(TAG, "Cannot setup active users RecyclerView: Components are null.");
//            return;
//        }
//        Log.d(TAG, "Setting up Active Users RecyclerView and listener for session: " + sessionId);
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        activeUsersRecyclerView.setLayoutManager(layoutManager);
//
//        activeUserAdapter = new ActiveUserAdapter(this, activeUserList);
//        activeUsersRecyclerView.setAdapter(activeUserAdapter);
//
//        activeUsersListener = activeSessionUsersRef.addChildEventListener(new ChildEventListener() {
//            @Override public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { String userId = snapshot.getKey(); if (userId != null && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) { Log.d(TAG, "User " + userId + " joined active session."); fetchAndAddActiveUser(userId); } }
//            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { String userId = snapshot.getKey(); Boolean isActive = snapshot.getValue(Boolean.class); if (userId != null && Boolean.FALSE.equals(isActive)) { Log.d(TAG, "User " + userId + " marked inactive in session."); removeActiveUser(userId); } else if (userId != null && Boolean.TRUE.equals(isActive)) { Log.d(TAG, "User " + userId + " marked active in session (onChildChanged). Re-fetching details."); fetchAndAddActiveUser(userId); } }
//            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { String userId = snapshot.getKey(); if (userId != null) { Log.d(TAG, "User " + userId + " left active session."); removeActiveUser(userId); } }
//            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { /* Not relevant */ }
//            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Firebase Active Users Listener cancelled for session " + sessionId + ": " + error.getMessage(), error.toException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to sync participants list.", Toast.LENGTH_SHORT).show(); }
//        });
//        Log.d(TAG, "Firebase active users listener attached to: " + activeSessionUsersRef.getPath());
//    }
//
//    private void fetchAndAddActiveUser(String userId) {
//        if (usersRef == null || userId == null || userId.isEmpty()) return;
//        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override public void onDataChange(@NonNull DataSnapshot snapshot) { if (snapshot.exists()) { String name = snapshot.child("username").getValue(String.class); String profileImageBase64 = snapshot.child("profileImage").getValue(String.class); ActiveUser user = new ActiveUser(userId, name != null ? name : "Unknown", profileImageBase64); boolean found = false; for (int i = 0; i < activeUserList.size(); i++) { if (Objects.equals(activeUserList.get(i).getUserId(), userId)) { activeUserList.get(i).setName(name != null ? name : "Unknown"); activeUserList.get(i).setProfileImageBase64(profileImageBase64); activeUserAdapter.notifyItemChanged(i); found = true; Log.d(TAG, "Updated user " + userId + " in active list."); break; } } if (!found) { activeUserList.add(user); activeUserAdapter.setActiveUsers(activeUserList); Log.d(TAG, "Added user " + userId + " to active list. Total: " + activeUserList.size()); } } else { Log.w(TAG, "User data not found in /Users for active user ID: " + userId); removeActiveUser(userId); } }
//            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Failed to fetch user details for active user: " + userId + ". Error: " + error.getMessage()); removeActiveUser(userId); }
//        });
//    }
//    private void removeActiveUser(String userId) {
//        if (userId == null) return; boolean removed = activeUserList.removeIf(user -> Objects.equals(user.getUserId(), userId)); if (removed) { activeUserAdapter.setActiveUsers(activeUserList); Log.d(TAG, "Removed user " + userId + " from active list. Total: " + activeUserList.size()); }
//    }
//
//    private void setupPresence() {
//        if (activeSessionUsersRef == null || currentUserID == null || currentUserID.isEmpty()) { Log.e(TAG, "Cannot setup presence: activeSessionUsersRef or currentUserID is null/empty."); return; }
//        Log.d(TAG, "Setting up presence for user " + currentUserID + " in session " + sessionId); DatabaseReference currentUserPresenceRef = activeSessionUsersRef.child(currentUserID);
//        currentUserPresenceRef.setValue(true).addOnSuccessListener(aVoid -> Log.d(TAG, "Presence set to true for user " + currentUserID)).addOnFailureListener(e -> Log.e(TAG, "Failed to set presence to true", e));
//        currentUserPresenceRef.onDisconnect().removeValue().addOnSuccessListener(aVoid -> Log.d(TAG, "onDisconnect removeValue setup successful for user " + currentUserID)).addOnFailureListener(e -> Log.e(TAG, "Failed to set onDisconnect", e));
//    }
//
//    private void goOffline() {
//        if (activeSessionUsersRef == null || currentUserID == null || currentUserID.isEmpty()) { return; }
//        Log.d(TAG, "Setting presence to offline/removing for user " + currentUserID); activeSessionUsersRef.child(currentUserID).onDisconnect().cancel();
//        activeSessionUsersRef.child(currentUserID).removeValue().addOnSuccessListener(aVoid -> Log.d(TAG, "Presence removed successfully for user " + currentUserID)).addOnFailureListener(e -> Log.e(TAG, "Failed to remove presence", e));
//    }
//
//
//    private void setupDrawingToolListeners() {
//        if (drawingView == null) { Log.e(TAG, "drawingView is null. Cannot setup drawing tool listeners."); return; }
//
//        final int BLACK = Color.BLACK; final int RED = Color.RED; final int BLUE = Color.BLUE; final int GREEN = Color.GREEN; final int YELLOW = Color.YELLOW; final int PURPLE = Color.parseColor("#9C27B0");
//        if (btnColorBlack != null) btnColorBlack.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(BLACK); currentActivityColor = BLACK; updateColorButtonState(); updateToolButtonState(); } });
//        if (btnColorRed != null) btnColorRed.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(RED); currentActivityColor = RED; updateColorButtonState(); updateToolButtonState(); } });
//        if (btnColorBlue != null) btnColorBlue.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(BLUE); currentActivityColor = BLUE; updateColorButtonState(); updateToolButtonState(); } });
//        if (btnColorGreen != null) btnColorGreen.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(GREEN); currentActivityColor = GREEN; updateColorButtonState(); updateToolButtonState(); } });
//        if (btnColorYellow != null) btnColorYellow.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(YELLOW); currentActivityColor = YELLOW; updateColorButtonState(); updateToolButtonState(); } });
//        if (btnColorPurple != null) btnColorPurple.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(PURPLE); currentActivityColor = PURPLE; updateColorButtonState(); updateToolButtonState(); } });
//
//        final float SMALL_STROKE = 5f; final float MEDIUM_STROKE = 12f; final float LARGE_STROKE = 25f;
//        if (btnStrokeSmall != null) btnStrokeSmall.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentStrokeWidth(SMALL_STROKE); currentActivityStrokeWidth = SMALL_STROKE; updateStrokeSizeButtonState(); } });
//        if (btnStrokeMedium != null) btnStrokeMedium.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentStrokeWidth(MEDIUM_STROKE); currentActivityStrokeWidth = MEDIUM_STROKE; updateStrokeSizeButtonState(); } });
//        if (btnStrokeLarge != null) btnStrokeLarge.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentStrokeWidth(LARGE_STROKE); currentActivityStrokeWidth = LARGE_STROKE; updateStrokeSizeButtonState(); } });
//
//        if (btnToolEraser != null) btnToolEraser.setOnClickListener(v -> {
//            Log.d(TAG, "Eraser button clicked. Setting tool to ERASER.");
//            if (drawingView != null) {
//                drawingView.setTool(DrawingView.TOOL_ERASER);
//                currentActivityTool = DrawingView.TOOL_ERASER;
//                updateColorButtonState();
//                updateToolButtonState();
//            }
//            Toast.makeText(this, "Eraser tool selected.", Toast.LENGTH_SHORT).show();
//        });
//
//        if (btnClearDrawing != null) { btnClearDrawing.setOnClickListener(v -> confirmClearDrawing()); }
//        if (btnEndSession != null) { btnEndSession.setOnClickListener(v -> confirmEndSession()); }
//    }
//
//
//    @Override
//    public void onStrokeComplete(Stroke completedStroke) {
//        Log.d(TAG, "Stroke completed by user. Sending to Firebase...");
//        if (strokesRef == null || currentUserID == null || currentUserID.isEmpty() || completedStroke == null || completedStroke.getPoints() == null || completedStroke.getPoints().isEmpty()) { Log.e(TAG, "Cannot send stroke: Missing Firebase ref, user ID, or valid stroke data."); Toast.makeText(this, "Error saving drawing stroke.", Toast.LENGTH_SHORT).show(); return; }
//        if (!"active".equals(currentSessionState)) { Log.w(TAG, "Attempted to send stroke but session is not active (" + currentSessionState + ")."); Toast.makeText(this, "Cannot draw: Session is not active.", Toast.LENGTH_SHORT).show(); return; }
//
//        completedStroke.setUserId(currentUserID);
//        String strokeKey = strokesRef.push().getKey();
//        if (strokeKey == null) { Log.e(TAG, "Failed to generate unique stroke key from Firebase."); Toast.makeText(this, "Error saving drawing stroke.", Toast.LENGTH_SHORT).show(); return; }
//        Log.d(TAG, "Generated Firebase push key for new stroke: " + strokeKey);
//
//        Map<String, Object> strokeData = new HashMap<>();
//        strokeData.put("color", completedStroke.getColor());
//        strokeData.put("strokeWidth", completedStroke.getStrokeWidth());
//        strokeData.put("userId", completedStroke.getUserId());
//        strokeData.put("timestamp", ServerValue.TIMESTAMP);
//        strokeData.put("points", completedStroke.getPoints());
//        strokeData.put("toolType", completedStroke.getToolType());
//
//        strokesRef.child(strokeKey).setValue(strokeData)
//                .addOnSuccessListener(aVoid -> Log.d(TAG, "Stroke saved successfully to Firebase with key: " + strokeKey))
//                .addOnFailureListener(e -> { Log.e(TAG, "Failed to save stroke to Firebase.", e); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to save drawing stroke.", Toast.LENGTH_SHORT).show(); });
//    }
//
//    private void loadExistingStrokesAndListen() {
//        if (strokesRef == null) { Log.e(TAG, "loadExistingStrokesAndListen: strokesRef is null. Cannot load/listen."); Toast.makeText(this, "Error loading drawing data.", Toast.LENGTH_SHORT).show(); setDrawingEnabled(false); return; }
//        Log.d(TAG, "Loading existing strokes and setting up listener for session: " + sessionId);
//        if (loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
//        setDrawingEnabled(false);
//
//        strokesRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d(TAG, "Finished loading initial strokes. Count: " + snapshot.getChildrenCount());
//                List<Stroke> initialStrokes = new ArrayList<>();
//                for (DataSnapshot strokeSnapshot : snapshot.getChildren()) { Stroke stroke = parseStrokeFromSnapshot(strokeSnapshot); if (stroke != null) { initialStrokes.add(stroke); } }
//                Collections.sort(initialStrokes, (s1, s2) -> Long.compare(s1.getTimestamp(), s2.getTimestamp()));
//                if (drawingView != null) drawingView.addCompletedStrokes(initialStrokes);
//                if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
//                attachStrokesRealtimeListener();
//            }
//            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Failed to load initial strokes from Firebase: " + error.getMessage(), error.toException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to load drawing history.", Toast.LENGTH_SHORT).show(); if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE); setDrawingEnabled(false); }
//        });
//    }
//
//    private Stroke parseStrokeFromSnapshot(DataSnapshot snapshot) {
//        if (snapshot == null || !snapshot.exists()) { Log.w(TAG, "parseStrokeFromSnapshot received null or non-existent snapshot."); return null; }
//        String strokeKey = snapshot.getKey(); try { Integer colorInt = snapshot.child("color").getValue(Integer.class); int color = (colorInt != null) ? colorInt : Color.BLACK;
//            Float strokeWidthFloat = snapshot.child("strokeWidth").getValue(Float.class); float strokeWidth = (strokeWidthFloat != null) ? strokeWidthFloat : 12f;
//            String userId = snapshot.child("userId").getValue(String.class); if (userId == null || userId.isEmpty()) { Log.w(TAG, "Stroke data missing userId or is empty. Skipping. Stroke key: " + strokeKey); return null; }
//            Long timestampLong = snapshot.child("timestamp").getValue(Long.class); long timestamp = (timestampLong != null) ? timestampLong : 0L;
//            Integer toolTypeInt = snapshot.hasChild("toolType") ? snapshot.child("toolType").getValue(Integer.class) : DrawingView.TOOL_PEN; int toolType = (toolTypeInt != null) ? toolTypeInt : DrawingView.TOOL_PEN;
//            List<Point> points = new ArrayList<>(); DataSnapshot pointsSnapshot = snapshot.child("points");
//            if (pointsSnapshot.exists() && pointsSnapshot.hasChildren()) { for (DataSnapshot pointSnap : pointsSnapshot.getChildren()) { Point point = pointSnap.getValue(Point.class); if (point != null) { points.add(point); } else { Log.w(TAG, "Failed to parse Point for stroke " + strokeKey); } } }
//            if (points.isEmpty()) { Log.w(TAG, "Parsed stroke has no points. Skipping. Stroke key: " + strokeKey); return null; }
//            return new Stroke(points, color, strokeWidth, userId, timestamp, toolType); }
//        catch (Exception e) { Log.e(TAG, "Error parsing stroke data from snapshot: " + strokeKey, e); return null; }
//    }
//
//    @SuppressLint("RestrictedApi")
//    private void attachStrokesRealtimeListener() {
//        if (strokesRef == null) { Log.e(TAG, "Cannot attach strokes listener, strokesRef is null."); return; }
//        if (strokesChildEventListener == null) { Log.d(TAG, "Attaching Firebase ChildEventListener for strokes sync for session: " + sessionId);
//            strokesChildEventListener = new ChildEventListener() {
//                @Override public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { Log.d(TAG, "🎨 onChildAdded triggered for stroke ID: " + snapshot.getKey()); Stroke stroke = parseStrokeFromSnapshot(snapshot); if (stroke != null) { if (drawingView != null) drawingView.addCompletedStroke(stroke); } else { Log.w(TAG, "Failed to parse stroke data on onChildAdded for key: " + snapshot.getKey()); } }
//                @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { Log.d(TAG, "🎨 onChildChanged triggered for stroke ID: " + snapshot.getKey() + ". Handling not implemented."); }
//                @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { Log.d(TAG, "❌ onChildRemoved triggered for stroke ID: " + snapshot.getKey() + "."); if (drawingView != null) drawingView.clearDrawing(); /* Simple clear all workaround */ }
//                @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { Log.d(TAG, "🎨 onChildMoved triggered for stroke ID: " + snapshot.getKey() + ". Handling not relevant."); }
//                @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Firebase Strokes Listener cancelled for session " + sessionId + ": " + error.getMessage(), error.toException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to sync drawing updates: " + error.getMessage(), Toast.LENGTH_SHORT).show(); setDrawingEnabled(false); }
//            };
//            strokesRef.addChildEventListener(strokesChildEventListener); Log.d(TAG, "Firebase strokes sync listener attached to: " + strokesRef.getPath()); }
//    }
//
//    private void removeFirebaseListeners() {
//        if (strokesRef != null && strokesChildEventListener != null) { strokesRef.removeEventListener(strokesChildEventListener); strokesChildEventListener = null; Log.d(TAG, "Removed Firebase strokes listener."); }
//        if (drawingSessionRef != null && sessionStateListener != null) { drawingSessionRef.removeEventListener(sessionStateListener); sessionStateListener = null; Log.d(TAG, "Removed Firebase session state listener."); }
//        if (activeSessionUsersRef != null && activeUsersListener != null) { activeSessionUsersRef.removeEventListener(activeUsersListener); activeUsersListener = null; Log.d(TAG, "Removed Firebase active users listener."); }
//    }
//
//    private void confirmClearDrawing() {
//        if (sessionStarterId == null || currentUserID == null || !Objects.equals(currentUserID, sessionStarterId) || !"active".equals(currentSessionState)) { Toast.makeText(this, "Only the session starter can clear the drawing.", Toast.LENGTH_SHORT).show(); Log.w(TAG, "Non-initiator (" + currentUserID + ") attempted to clear drawing for session " + sessionId); return; }
//        Log.d(TAG, "Showing clear drawing confirmation dialog."); new AlertDialog.Builder(this).setTitle("Clear Drawing").setMessage("Are you sure you want to clear the entire drawing board? This cannot be undone for anyone.").setPositiveButton("Yes, Clear", (dialog, which) -> clearDrawing()).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).setIcon(android.R.drawable.ic_dialog_alert).show();
//    }
//
//    private void clearDrawing() {
//        if (sessionStarterId == null || currentUserID == null || !Objects.equals(currentUserID, sessionStarterId) || !"active".equals(currentSessionState)) { Toast.makeText(this, "Authorization or state error preventing clear.", Toast.LENGTH_SHORT).show(); Log.w(TAG, "Authorization/State check failed during clearDrawing execution."); return; }
//        Log.d(TAG, "Initiator " + currentUserID + " clearing drawing for session " + sessionId + " from Firebase.");
//        if (strokesRef != null) { strokesRef.removeValue().addOnCompleteListener(task -> { if (task.isSuccessful()) { Log.d(TAG, "All strokes removed from Firebase for session " + sessionId); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Drawing cleared for everyone.", Toast.LENGTH_SHORT).show(); if (drawingView != null) drawingView.clearDrawing(); } else { Log.e(TAG, "Failed to clear strokes from Firebase.", task.getException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to clear drawing.", Toast.LENGTH_SHORT).show(); }}); }
//        else { Log.e(TAG, "strokesRef is null, cannot clear drawing from Firebase."); Toast.makeText(this, "Error clearing drawing.", Toast.LENGTH_SHORT).show(); }
//    }
//
//    private void confirmEndSession() {
//        if (sessionStarterId == null || currentUserID == null || !Objects.equals(currentUserID, sessionStarterId) || !"active".equals(currentSessionState)) { Toast.makeText(this, "Only the session starter can end the session.", Toast.LENGTH_SHORT).show(); Log.w(TAG, "Non-initiator (" + currentUserID + ") attempted to end session."); return; }
//        if (!"active".equals(currentSessionState)) { Toast.makeText(this, "Session is already ended or not active.", Toast.LENGTH_SHORT).show(); return; }
//        Log.d(TAG, "Showing end session confirmation dialog."); new AlertDialog.Builder(this).setTitle("End Drawing Session").setMessage("Are you sure you want to end this drawing session? No one will be able to draw after it's ended.").setPositiveButton("Yes, End", (dialog, which) -> endDrawingSession()).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).setIcon(android.R.drawable.ic_dialog_alert).show();
//    }
//
//    private void endDrawingSession() {
//        if (sessionStarterId == null || currentUserID == null || !Objects.equals(currentUserID, sessionStarterId) || !"active".equals(currentSessionState)) { Toast.makeText(this, "Authorization or state error preventing end.", Toast.LENGTH_SHORT).show(); Log.w(TAG, "Authorization/State check failed during endDrawingSession execution."); return; }
//        Log.d(TAG, "Initiator " + currentUserID + " ending session " + sessionId + " by setting state to 'ended'.");
//        if (sessionStateRef != null) { sessionStateRef.setValue("ended").addOnCompleteListener(task -> { if (task.isSuccessful()) { Log.d(TAG, "Drawing session state set to 'ended' in Firebase."); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Drawing session ended for everyone.", Toast.LENGTH_SHORT).show(); } else { Log.e(TAG, "Failed to set session state to 'ended'.", task.getException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to end session.", Toast.LENGTH_SHORT).show(); }}); }
//        else { Log.e(TAG, "sessionStateRef is null, cannot end session."); Toast.makeText(this, "Error ending session.", Toast.LENGTH_SHORT).show(); }
//    }
//
//    private String getCurrentSessionState() { return this.currentSessionState; }
//
//
//    // --- Activity's State Variables for Tool Selection Visual Feedback ---
////    private int currentActivityColor = Color.BLACK;
////    private float currentActivityStrokeWidth = 12f;
////    private int currentActivityTool = DrawingView.TOOL_PEN;
//
//
//    // --- Helper method to visually indicate the selected color button (using backgroundTint) ---
//    // Inside YOUR_DRAWING_ACTIVITY_CLASS.java
//
//    // --- Helper method to visually indicate the selected color button (using Border Drawable) ---
//    // Inside YOUR_DRAWING_ACTIVITY_CLASS.java
//
//    // --- Helper method to visually indicate the selected color button (using backgroundTint for Base Color + Highlight) ---
//    private void updateColorButtonState() {
//        // Get the list of color buttons
//        Button[] colorButtons = {btnColorBlack, btnColorRed, btnColorBlue, btnColorGreen, btnColorYellow, btnColorPurple};
//        // Store the corresponding colors (this array is used to know which color button represents which color)
//        int[] colors = {
//                Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.parseColor("#9C27B0")
//        };
//
//        // Define the highlight color (used as an overlay tint when selected)
//        int selectedHighlightTint = getResources().getColor(R.color.colorAccenttt, getTheme());
//        // Define the default state tint (null means use the color set below)
//        ColorStateList defaultTintList = null;
//
//        for (int i = 0; i < colorButtons.length; i++) {
//            Button button = colorButtons[i];
//            if (button != null) {
//                // Get the color value this button represents from our array
//                int buttonRepresentsColor = colors[i];
//
//                // First, set the button's base color using backgroundTint
//                // This ensures the button always shows its intended color (black, red, etc.)
//                button.setBackgroundTintList(ColorStateList.valueOf(buttonRepresentsColor));
//
//                // Now, apply the selection highlight tint if it's the selected color button
//                if (buttonRepresentsColor == currentActivityColor) {
//                    // This button is selected: Apply the highlight tint ON TOP of the base color tint
//                    // Using BlendMode.SRC_ATOP or similar with setBackgroundTintBlendMode might be needed for complex blending
//                    // For simpler approach, maybe slightly change the base color tint or alpha
//                    // Let's try applying the highlight tint directly, might override base tint slightly
//                    button.setBackgroundTintList(ColorStateList.valueOf(selectedHighlightTint));
//                    Log.d(TAG, "Color button selected: " + String.format("#%06X", (0xFFFFFF & currentActivityColor)) + ". Applied highlight tint.");
//
//                    // Alternative simple highlight: Just add a border drawable on top.
//                    // button.setBackgroundResource(R.drawable.color_button_border_selected); // Requires this drawable and removes base tint temporarily
//                } else {
//                    // This button is not selected: Ensure only the base color tint is applied
//                    // The line `button.setBackgroundTintList(ColorStateList.valueOf(buttonRepresentsColor));` above already sets the base color.
//                    // No additional action needed here unless a previous highlight tint needs removal.
//                    // We can explicitly set back the base tint list for clarity.
//                    button.setBackgroundTintList(ColorStateList.valueOf(buttonRepresentsColor)); // Ensure base color tint
//                    Log.d(TAG, "Color button unselected: " + String.format("#%06X", (0xFFFFFF & buttonRepresentsColor)) + ". Set base tint.");
//                    // If using border drawable for highlight: button.setBackgroundResource(0); // Remove border drawable
//                }
//            }
//        }
//        Log.d(TAG, "Color button state updated. Current color: " + String.format("#%06X", (0xFFFFFF & currentActivityColor)));
//    }
//
//    // --- Helper method to visually indicate the selected stroke size button (using backgroundTint) ---
//    private void updateStrokeSizeButtonState() {
//        TextView[] sizeButtons = {btnStrokeSmall, btnStrokeMedium, btnStrokeLarge};
//        final float SMALL_STROKE = 5f;
//        final float MEDIUM_STROKE = 12f;
//        final float LARGE_STROKE = 25f;
//        float[] widths = {SMALL_STROKE, MEDIUM_STROKE, LARGE_STROKE};
//
//        int selectedHighlightTint = getResources().getColor(R.color.colorAccent, getTheme());
//        ColorStateList defaultTintList = null;
//
//        int[] defaultDrawables = {
//                R.drawable.stroke_width_indicator_small,
//                R.drawable.stroke_width_indicator_medium,
//                R.drawable.stroke_width_indicator_large
//        };
//
//        for (int i = 0; i < sizeButtons.length; i++) {
//            TextView button = sizeButtons[i];
//            if (button != null) {
//                button.setBackgroundResource(defaultDrawables[i]); // Set the base shape drawable
//
//                if (widths[i] == currentActivityStrokeWidth) {
//                    button.setBackgroundTintList(ColorStateList.valueOf(selectedHighlightTint));
//                    Log.d(TAG, "Size button selected: " + currentActivityStrokeWidth + ". Applied highlight tint.");
//                } else {
//                    button.setBackgroundTintList(defaultTintList);
//                    Log.d(TAG, "Size button unselected: " + widths[i] + ". Removed highlight tint.");
//                }
//            }
//        }
//        Log.d(TAG, "Stroke size button state updated. Current width: " + currentActivityStrokeWidth);
//    }
//
//    private int getStrokeWidthDrawableResId(float width) {
//        final float SMALL_STROKE = 5f;
//        final float MEDIUM_STROKE = 12f;
//        final float LARGE_STROKE = 25f;
//        if (width == SMALL_STROKE) return R.drawable.stroke_width_indicator_small;
//        if (width == MEDIUM_STROKE) return R.drawable.stroke_width_indicator_medium;
//        if (width == LARGE_STROKE) return R.drawable.stroke_width_indicator_large;
//        return 0;
//    }
//
//    private void updateToolButtonState() {
//        int selectedHighlightColor = getResources().getColor(R.color.colorAccent, getTheme());
//        if (btnToolEraser != null) {
//            if (currentActivityTool == DrawingView.TOOL_ERASER) {
//                btnToolEraser.setBackgroundTintList(ColorStateList.valueOf(selectedHighlightColor));
//                Log.d(TAG, "Tool button selected: Eraser. Highlighted btnToolEraser.");
//            } else {
//                btnToolEraser.setBackgroundTintList(null);
//                Log.d(TAG, "Tool button selected: Pen (Eraser not highlighted)");
//            }
//        }
//    }
//
//}




package com.sana.circleup.drawingboard_chatgroup; // <<< USE YOUR ACTUAL PACKAGE NAME

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sana.circleup.Login; // <<< Replace with your actual Login Activity
import com.sana.circleup.R; // <<< USE YOUR ACTUAL R FILE

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class YOUR_DRAWING_ACTIVITY_CLASS extends AppCompatActivity implements DrawingView.OnStrokeCompleteListener {

    private static final String TAG = "DrawingActivity";

    // --- UI Elements ---
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private RecyclerView activeUsersRecyclerView;
    private DrawingView drawingView;
    private TextView loadingIndicator;
    private TextView sessionEndedIndicator;
    private ViewGroup drawingToolsLayout;

    // Drawing Tool Buttons
    private Button btnColorBlack, btnColorRed, btnColorBlue, btnColorGreen, btnColorYellow, btnColorPurple;
    private TextView btnStrokeSmall, btnStrokeMedium, btnStrokeLarge; // Size are TextViews
    private ImageButton btnToolEraser;

    // --- NEW Scroll and Zoom Controls (TextViews) ---
    private TextView btnScrollUp, btnScrollDown, btnZoomIn, btnZoomOut; // Scroll/Zoom are TextViews


    // Initiator Action Buttons
    private Button btnClearDrawing, btnEndSession;

    // --- Firebase ---
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference usersRef;
    private DatabaseReference groupRef;
    private DatabaseReference drawingSessionRef;
    private DatabaseReference strokesRef;
    private DatabaseReference sessionStateRef;
    private DatabaseReference activeSessionUsersRef;


    // --- Session Info ---
    private String groupId;
    private String sessionId;
    private String currentUserID;
    private String currentUserName;
    private String sessionStarterId;
    private String currentSessionState = "loading";

    // --- Firebase Listeners ---
    private ChildEventListener strokesChildEventListener;
    private ValueEventListener sessionStateListener;
    private ChildEventListener activeUsersListener;

    // --- Adapter for Active Users RecyclerView ---
    private ActiveUserAdapter activeUserAdapter;
    private List<ActiveUser> activeUserList = new ArrayList<>();

    // --- Activity's State Variables for Tool Selection Visual Feedback ---
    private int currentActivityColor = Color.BLACK;
    private float currentActivityStrokeWidth = 12f;
    private int currentActivityTool = DrawingView.TOOL_PEN;

    // --- Activity's State Variables for Pan and Zoom ---
    // Keep track of the current pan and zoom state in the Activity
    // These values will be passed to the DrawingView
    private float currentActivityScaleFactor = 1.0f; // Matches DrawingView default
    private float currentActivityPosX = 0.0f;      // Matches DrawingView default
    private float currentActivityPosY = 0.0f;      // Matches DrawingView default

    // Constants for Pan/Zoom amounts
    private static final float SCROLL_AMOUNT = 50f; // Scroll by 50 pixels per click
    private static final float ZOOM_FACTOR = 1.2f; // Zoom in/out by 20% per click (1.2x or 1/1.2x)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_drawing_class); // <<< Use your actual layout file name

        Log.d(TAG, "🎨 YOUR_DRAWING_ACTIVITY_CLASS launched");

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "No current user authenticated. Redirecting to Login.");
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity();
            return;
        }
        currentUserID = currentUser.getUid();

        groupId = getIntent().getStringExtra("groupId");
        sessionId = getIntent().getStringExtra("sessionId");

        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(sessionId)) {
            Log.e(TAG, "CRITICAL ERROR: Group ID or Session ID missing from Intent!");
            Toast.makeText(this, "Error: Drawing session information missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d(TAG, "Received Group ID: " + groupId + ", Session ID: " + sessionId);

        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("Users");
        groupRef = rootRef.child("Groups").child(groupId);
        drawingSessionRef = groupRef.child("drawingSessions").child(sessionId);
        strokesRef = drawingSessionRef.child("strokes");
        sessionStateRef = drawingSessionRef.child("state");
        activeSessionUsersRef = drawingSessionRef.child("activeUsers");

        InitializeFields();

        if (drawingView != null) {
            drawingView.setOnStrokeCompleteListener(this);
            // Set initial state on DrawingView (should match Activity defaults)
            drawingView.setCurrentColor(currentActivityColor); // setCurrentColor also sets tool to PEN
            drawingView.setCurrentStrokeWidth(currentActivityStrokeWidth);
            drawingView.setTool(currentActivityTool); // Explicitly set tool just in case
            // --- NEW: Set initial pan/zoom state on DrawingView ---
            drawingView.setScaleFactor(currentActivityScaleFactor);
            drawingView.setPan(currentActivityPosX, currentActivityPosY);

        } else {
            Log.e(TAG, "drawingView is null. Cannot set stroke listener or initial state!");
            // Cannot draw without DrawingView, disable tools
            setDrawingEnabled(false); // Ensure tools are disabled if view is null
        }

        GetUserInfo();
        loadSessionInfoAndListeners();
        setupActiveUsersRecyclerView();
        setupPresence();
        setupDrawingToolListeners();
        loadExistingStrokesAndListen(); // This also attaches the realtime listener

        // --- Set initial visual state of tool buttons ---
        updateColorButtonState();
        updateStrokeSizeButtonState();
        updateToolButtonState();


        Log.d(TAG, "📲 onCreate finished.");
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "YOUR_DRAWING_ACTIVITY_CLASS onStart");
        setupPresence(); // Ensure presence is marked onStart too (if somehow lost without onDestroy)
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "YOUR_DRAWING_ACTIVITY_CLASS onStop");
        goOffline(); // Remove presence when activity stops/goes to background
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "YOUR_DRAWING_ACTIVITY_CLASS onDestroy called.");
        removeFirebaseListeners(); // Remove all Firebase listeners
        goOffline(); // Ensure user is marked offline
    }


    private void sendUserToLoginActivity() {
        Log.d(TAG, "Redirecting to Login Activity.");
        Intent loginIntent = new Intent(YOUR_DRAWING_ACTIVITY_CLASS.this, Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void InitializeFields() {
        Log.d(TAG, "Initializing UI elements.");

        toolbar = findViewById(R.id.drawing_toolbar);
        if (toolbar == null) Log.e(TAG, "Toolbar not found!");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle == null) Log.e(TAG, "Toolbar title TextView not found!");

        activeUsersRecyclerView = findViewById(R.id.active_users_recycler_view);
        if (activeUsersRecyclerView == null) Log.e(TAG, "Active Users RecyclerView not found!");

        drawingView = findViewById(R.id.drawing_board);
        if (drawingView == null) Log.e(TAG, "DrawingView (drawing_board) not found!");
        loadingIndicator = findViewById(R.id.drawing_loading_indicator);
        if (loadingIndicator == null) Log.e(TAG, "Loading Indicator TextView not found!");
        sessionEndedIndicator = findViewById(R.id.session_ended_indicator);
        if (sessionEndedIndicator == null) Log.e(TAG, "Session Ended Indicator TextView not found!");

        drawingToolsLayout = findViewById(R.id.drawing_tools_layout);
        if (drawingToolsLayout == null) Log.e(TAG, "Drawing Tools Layout not found!");

        // Color Buttons
        btnColorBlack = findViewById(R.id.btn_color_black); if (btnColorBlack == null) Log.e(TAG, "btnColorBlack not found!");
        btnColorRed = findViewById(R.id.btn_color_red); if (btnColorRed == null) Log.e(TAG, "btnColorRed not found!");
        btnColorBlue = findViewById(R.id.btn_color_blue); if (btnColorBlue == null) Log.e(TAG, "btnColorBlue not found!");
        btnColorGreen = findViewById(R.id.btn_color_green); if (btnColorGreen == null) Log.e(TAG, "btnColorGreen not found!");
        btnColorYellow = findViewById(R.id.btn_color_yellow); if (btnColorYellow == null) Log.e(TAG, "btnColorYellow not found!");
        btnColorPurple = findViewById(R.id.btn_color_PURPLE); if (btnColorPurple == null) Log.e(TAG, "btnColorPurple not found!");

        // Size TextViews - <<< Find as TextView >>>
        btnStrokeSmall = findViewById(R.id.btn_stroke_small); if (btnStrokeSmall == null) Log.e(TAG, "btnStrokeSmall (TextView) not found!");
        btnStrokeMedium = findViewById(R.id.btn_stroke_medium); if (btnStrokeMedium == null) Log.e(TAG, "btnStrokeMedium (TextView) not found!");
        btnStrokeLarge = findViewById(R.id.btn_stroke_large); if (btnStrokeLarge == null) Log.e(TAG, "btnStrokeLarge (TextView) not found!");

        // --- NEW Scroll and Zoom Controls (TextViews) ---
        btnScrollUp = findViewById(R.id.btn_scroll_up); if (btnScrollUp == null) Log.e(TAG, "btnScrollUp (TextView) not found!");
        btnScrollDown = findViewById(R.id.btn_scroll_down); if (btnScrollDown == null) Log.e(TAG, "btnScrollDown (TextView) not found!");
        btnZoomIn = findViewById(R.id.btn_zoom_in); if (btnZoomIn == null) Log.e(TAG, "btnZoomIn (TextView) not found!");
        btnZoomOut = findViewById(R.id.btn_zoom_out); if (btnZoomOut == null) Log.e(TAG, "btnZoomOut (TextView) not found!");


        btnToolEraser = findViewById(R.id.btn_tool_eraser); if (btnToolEraser == null) Log.e(TAG, "btnToolEraser not found!");

        btnClearDrawing = findViewById(R.id.btn_clear_drawing); if (btnClearDrawing == null) Log.e(TAG, "btnClearDrawing not found!");
        btnEndSession = findViewById(R.id.btn_end_session); if (btnEndSession == null) Log.e(TAG, "btnEndSession not found!");

        btnClearDrawing.setVisibility(View.GONE);
        btnEndSession.setVisibility(View.GONE);

        setDrawingEnabled(false);

        loadingIndicator.setVisibility(View.GONE);
        sessionEndedIndicator.setVisibility(View.GONE);

        Log.d(TAG, "InitializeFields finished.");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void GetUserInfo() {
        if (usersRef == null || currentUserID == null || currentUserID.isEmpty()) {
            Log.e(TAG, "GetUserInfo: usersRef or currentUserID is null/empty.");
            currentUserName = "Unknown";
            return;
        }
        usersRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    currentUserName = snapshot.child("username").getValue(String.class);
                    if (TextUtils.isEmpty(currentUserName)) {
                        currentUserName = "Unknown";
                        Log.w(TAG, "Username node found but value is null/empty for user: " + currentUserID);
                    }
                    Log.d(TAG, "Fetched current username: " + currentUserName);
                } else {
                    currentUserName = "Unknown";
                    Log.w(TAG, "Username not found in Firebase for current user: " + currentUserID);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error retrieving current user data: " + error.getMessage());
                currentUserName = "Unknown";
            }
        });
    }


    @SuppressLint("RestrictedApi")
    private void loadSessionInfoAndListeners() {
        if (drawingSessionRef == null || sessionStateRef == null) {
            Log.e(TAG, "loadSessionInfoAndListeners: drawingSessionRef or sessionStateRef is null. Cannot load session info.");
            Toast.makeText(this, "Error loading session data.", Toast.LENGTH_SHORT).show();
            setDrawingEnabled(false);
            return;
        }
        Log.d(TAG, "Loading session info and setting up state listener for session: " + sessionId);

        sessionStateListener = drawingSessionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    sessionStarterId = snapshot.child("starterId").getValue(String.class);
                    String sessionState = snapshot.child("state").getValue(String.class);
                    YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState = (sessionState != null) ? sessionState : "active";

                    Log.d(TAG, "Session info updated. State: " + YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState + ", Starter ID: " + sessionStarterId);

                    updateUIBasedOnSessionState(YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState);

                    boolean isInitiator = currentUserID != null && Objects.equals(currentUserID, sessionStarterId);
                    if (btnClearDrawing != null) {
                        btnClearDrawing.setVisibility(isInitiator ? View.VISIBLE : View.GONE);
                        btnClearDrawing.setEnabled(isInitiator && "active".equals(YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState));
                    }
                    if (btnEndSession != null) {
                        btnEndSession.setVisibility(isInitiator ? View.VISIBLE : View.GONE);
                        btnEndSession.setEnabled(isInitiator && "active".equals(YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState));
                    }

                } else {
                    Log.w(TAG, "Drawing session node does not exist in Firebase: " + sessionId + ". Marking as ended.");
                    Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Drawing session ended or deleted.", Toast.LENGTH_LONG).show();
                    YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState = "ended";
                    updateUIBasedOnSessionState(YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Firebase Session Listener cancelled for session " + sessionId + ": " + error.getMessage(), error.toException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to sync session status.", Toast.LENGTH_LONG).show(); YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState = "ended"; updateUIBasedOnSessionState(YOUR_DRAWING_ACTIVITY_CLASS.this.currentSessionState); }
        });
        Log.d(TAG, "Firebase session data listener attached to: " + drawingSessionRef.getPath());
    }

    private void updateUIBasedOnSessionState(String state) {
        boolean isInitiator = currentUserID != null && Objects.equals(currentUserID, sessionStarterId);
        boolean enableDrawing = "active".equals(state);

        setDrawingEnabled(enableDrawing);

        if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
        if (sessionEndedIndicator != null) {
            sessionEndedIndicator.setVisibility(enableDrawing ? View.GONE : View.VISIBLE);
        }
        if (toolbarTitle != null) {
            toolbarTitle.setText(enableDrawing ? "Shared Drawing" : "Shared Drawing (Ended)");
        }

        if (btnClearDrawing != null) {
            btnClearDrawing.setEnabled(isInitiator && enableDrawing);
        }
        if (btnEndSession != null) {
            btnEndSession.setEnabled(isInitiator && enableDrawing);
        }
        // Update visual state of tool buttons based on enabled state
        updateColorButtonState();
        updateStrokeSizeButtonState();
        updateToolButtonState();
        // Update visual state of pan/zoom buttons based on enabled state
        updatePanZoomButtonState(); // <<< NEW Call

        Log.d(TAG, "UI updated for state: " + state + ", Drawing Enabled: " + enableDrawing);
    }

    private void setDrawingEnabled(boolean enabled) {
        if (drawingView != null) {
            drawingView.setEnabled(enabled);
        }
        if (drawingToolsLayout != null) {
            for (int i = 0; i < drawingToolsLayout.getChildCount(); i++) {
                View child = drawingToolsLayout.getChildAt(i);
                child.setEnabled(enabled);
            }
        } else {
            Log.w(TAG, "drawingToolsLayout is null, cannot enable/disable tools.");
        }
        Log.d(TAG, "Drawing input and tools ENABLED: " + enabled);
    }


    @SuppressLint("RestrictedApi")
    private void setupActiveUsersRecyclerView() {
        if (activeUsersRecyclerView == null || activeSessionUsersRef == null || usersRef == null) {
            Log.e(TAG, "Cannot setup active users RecyclerView: Components are null.");
            return;
        }
        Log.d(TAG, "Setting up Active Users RecyclerView and listener for session: " + sessionId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        activeUsersRecyclerView.setLayoutManager(layoutManager);

        activeUserAdapter = new ActiveUserAdapter(this, activeUserList);
        activeUsersRecyclerView.setAdapter(activeUserAdapter);

        activeUsersListener = activeSessionUsersRef.addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { String userId = snapshot.getKey(); if (userId != null && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) { Log.d(TAG, "User " + userId + " joined active session."); fetchAndAddActiveUser(userId); } }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { String userId = snapshot.getKey(); Boolean isActive = snapshot.getValue(Boolean.class); if (userId != null && Boolean.FALSE.equals(isActive)) { Log.d(TAG, "User " + userId + " marked inactive in session."); removeActiveUser(userId); } else if (userId != null && Boolean.TRUE.equals(isActive)) { Log.d(TAG, "User " + userId + " marked active in session (onChildChanged). Re-fetching details."); fetchAndAddActiveUser(userId); } }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { String userId = snapshot.getKey(); if (userId != null) { Log.d(TAG, "User " + userId + " left active session."); removeActiveUser(userId); } }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { Log.d(TAG, "🎨 onChildMoved triggered for stroke ID: " + snapshot.getKey() + ". Handling not relevant."); }
            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Firebase Active Users Listener cancelled for session " + sessionId + ": " + error.getMessage(), error.toException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to sync participants list.", Toast.LENGTH_SHORT).show(); }
        });
        Log.d(TAG, "Firebase active users listener attached to: " + activeSessionUsersRef.getPath());
    }

    private void fetchAndAddActiveUser(String userId) {
        if (usersRef == null || userId == null || userId.isEmpty()) return;
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) { if (snapshot.exists()) { String name = snapshot.child("username").getValue(String.class); String profileImageBase64 = snapshot.child("profileImage").getValue(String.class); ActiveUser user = new ActiveUser(userId, name != null ? name : "Unknown", profileImageBase64); boolean found = false; for (int i = 0; i < activeUserList.size(); i++) { if (Objects.equals(activeUserList.get(i).getUserId(), userId)) { activeUserList.get(i).setName(name != null ? name : "Unknown"); activeUserList.get(i).setProfileImageBase64(profileImageBase64); activeUserAdapter.notifyItemChanged(i); found = true; Log.d(TAG, "Updated user " + userId + " in active list."); break; } } if (!found) { activeUserList.add(user); activeUserAdapter.setActiveUsers(activeUserList); Log.d(TAG, "Added user " + userId + " to active list. Total: " + activeUserList.size()); } } else { Log.w(TAG, "User data not found in /Users for active user ID: " + userId); removeActiveUser(userId); } }
            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Failed to fetch user details for active user: " + userId + ". Error: " + error.getMessage()); removeActiveUser(userId); }
        });
    }
    private void removeActiveUser(String userId) {
        if (userId == null) return; boolean removed = activeUserList.removeIf(user -> Objects.equals(user.getUserId(), userId)); if (removed) { activeUserAdapter.setActiveUsers(activeUserList); Log.d(TAG, "Removed user " + userId + " from active list. Total: " + activeUserList.size()); }
    }

    private void setupPresence() {
        if (activeSessionUsersRef == null || currentUserID == null || currentUserID.isEmpty()) { Log.e(TAG, "Cannot setup presence: activeSessionUsersRef or currentUserID is null/empty."); return; }
        Log.d(TAG, "Setting up presence for user " + currentUserID + " in session " + sessionId); DatabaseReference currentUserPresenceRef = activeSessionUsersRef.child(currentUserID);
        currentUserPresenceRef.setValue(true).addOnSuccessListener(aVoid -> Log.d(TAG, "Presence set to true for user " + currentUserID)).addOnFailureListener(e -> Log.e(TAG, "Failed to set presence to true", e));
        currentUserPresenceRef.onDisconnect().removeValue().addOnSuccessListener(aVoid -> Log.d(TAG, "onDisconnect removeValue setup successful for user " + currentUserID)).addOnFailureListener(e -> Log.e(TAG, "Failed to set onDisconnect", e));
    }

    private void goOffline() {
        if (activeSessionUsersRef == null || currentUserID == null || currentUserID.isEmpty()) { return; }
        Log.d(TAG, "Setting presence to offline/removing for user " + currentUserID); activeSessionUsersRef.child(currentUserID).onDisconnect().cancel();
        activeSessionUsersRef.child(currentUserID).removeValue().addOnSuccessListener(aVoid -> Log.d(TAG, "Presence removed successfully for user " + currentUserID)).addOnFailureListener(e -> Log.e(TAG, "Failed to remove presence", e));
    }


    private void setupDrawingToolListeners() {
        if (drawingView == null) { Log.e(TAG, "drawingView is null. Cannot setup drawing tool listeners."); return; }

        final int BLACK = Color.BLACK; final int RED = Color.RED; final int BLUE = Color.BLUE; final int GREEN = Color.GREEN; final int YELLOW = Color.YELLOW; final int PURPLE = Color.parseColor("#9C27B0");
        if (btnColorBlack != null) btnColorBlack.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(BLACK); currentActivityColor = BLACK; updateColorButtonState(); updateToolButtonState(); } });
        if (btnColorRed != null) btnColorRed.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(RED); currentActivityColor = RED; updateColorButtonState(); updateToolButtonState(); } });
        if (btnColorBlue != null) btnColorBlue.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(BLUE); currentActivityColor = BLUE; updateColorButtonState(); updateToolButtonState(); } });
        if (btnColorGreen != null) btnColorGreen.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(GREEN); currentActivityColor = GREEN; updateColorButtonState(); updateToolButtonState(); } });
        if (btnColorYellow != null) btnColorYellow.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(YELLOW); currentActivityColor = YELLOW; updateColorButtonState(); updateToolButtonState(); } });
        if (btnColorPurple != null) btnColorPurple.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentColor(PURPLE); currentActivityColor = PURPLE; updateColorButtonState(); updateToolButtonState(); } });

        final float SMALL_STROKE = 5f; final float MEDIUM_STROKE = 12f; final float LARGE_STROKE = 25f;
        if (btnStrokeSmall != null) btnStrokeSmall.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentStrokeWidth(SMALL_STROKE); currentActivityStrokeWidth = SMALL_STROKE; updateStrokeSizeButtonState(); } });
        if (btnStrokeMedium != null) btnStrokeMedium.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentStrokeWidth(MEDIUM_STROKE); currentActivityStrokeWidth = MEDIUM_STROKE; updateStrokeSizeButtonState(); } });
        if (btnStrokeLarge != null) btnStrokeLarge.setOnClickListener(v -> { if (drawingView != null) { drawingView.setCurrentStrokeWidth(LARGE_STROKE); currentActivityStrokeWidth = LARGE_STROKE; updateStrokeSizeButtonState(); } });

        if (btnToolEraser != null) btnToolEraser.setOnClickListener(v -> {
            Log.d(TAG, "Eraser button clicked. Setting tool to ERASER.");
            if (drawingView != null) {
                drawingView.setTool(DrawingView.TOOL_ERASER);
                currentActivityTool = DrawingView.TOOL_ERASER;
                updateColorButtonState();
                updateToolButtonState();
            }
            Toast.makeText(this, "Eraser tool selected.", Toast.LENGTH_SHORT).show();
        });

        if (btnClearDrawing != null) { btnClearDrawing.setOnClickListener(v -> confirmClearDrawing()); }
        if (btnEndSession != null) { btnEndSession.setOnClickListener(v -> confirmEndSession()); }

        // --- NEW Scroll and Zoom Controls Listeners (TextViews) ---
        final float SCROLL_AMOUNT = 50f; // Pixels to scroll per click
        final float ZOOM_FACTOR = 1.2f; // Factor to zoom in/out per click

        if (btnScrollUp != null) btnScrollUp.setOnClickListener(v -> {
            if (drawingView != null) {
                currentActivityPosY -= SCROLL_AMOUNT; // Update Activity's Y pan state (move content up)
                drawingView.setPan(currentActivityPosX, currentActivityPosY); // Tell DrawingView to pan
                updatePanZoomButtonState(); // Update visual state of pan/zoom buttons (optional)
                Log.d(TAG, "Scrolled Up. New Pan Y: " + currentActivityPosY);
            }
        });
        if (btnScrollDown != null) btnScrollDown.setOnClickListener(v -> {
            if (drawingView != null) {
                currentActivityPosY += SCROLL_AMOUNT; // Update Activity's Y pan state (move content down)
                drawingView.setPan(currentActivityPosX, currentActivityPosY); // Tell DrawingView to pan
                updatePanZoomButtonState(); // Update visual state
                Log.d(TAG, "Scrolled Down. New Pan Y: " + currentActivityPosY);
            }
        });
        if (btnZoomIn != null) btnZoomIn.setOnClickListener(v -> {
            if (drawingView != null) {
                float newScale = currentActivityScaleFactor * ZOOM_FACTOR; // Calculate new scale (zoom in)
                // Apply constraint
                newScale = Math.max(DrawingView.MIN_SCALE_FACTOR, Math.min(newScale, DrawingView.MAX_SCALE_FACTOR)); // Assuming DrawingView has MIN/MAX constants

                currentActivityScaleFactor = newScale; // Update Activity's scale state
                drawingView.setScaleFactor(currentActivityScaleFactor); // Tell DrawingView to scale
                updatePanZoomButtonState(); // Update visual state
                Log.d(TAG, "Zoomed In. New Scale: " + currentActivityScaleFactor);
            }
        });
        if (btnZoomOut != null) btnZoomOut.setOnClickListener(v -> {
            if (drawingView != null) {
                float newScale = currentActivityScaleFactor / ZOOM_FACTOR; // Calculate new scale (zoom out)
                // Apply constraint
                newScale = Math.max(DrawingView.MIN_SCALE_FACTOR, Math.min(newScale, DrawingView.MAX_SCALE_FACTOR)); // Assuming DrawingView has MIN/MAX constants

                currentActivityScaleFactor = newScale; // Update Activity's scale state
                drawingView.setScaleFactor(currentActivityScaleFactor); // Tell DrawingView to scale
                updatePanZoomButtonState(); // Update visual state
                Log.d(TAG, "Zoomed Out. New Scale: " + currentActivityScaleFactor);
            }
        });
    }


    @Override
    public void onStrokeComplete(Stroke completedStroke) {
        Log.d(TAG, "Stroke completed by user. Sending to Firebase...");
        if (strokesRef == null || currentUserID == null || currentUserID.isEmpty() || completedStroke == null || completedStroke.getPoints() == null || completedStroke.getPoints().isEmpty()) { Log.e(TAG, "Cannot send stroke: Missing Firebase ref, user ID, or valid stroke data."); Toast.makeText(this, "Error saving drawing stroke.", Toast.LENGTH_SHORT).show(); return; }
        if (!"active".equals(currentSessionState)) { Log.w(TAG, "Attempted to send stroke but session is not active (" + currentSessionState + ")."); Toast.makeText(this, "Cannot draw: Session is not active.", Toast.LENGTH_SHORT).show(); return; }

        completedStroke.setUserId(currentUserID);
        String strokeKey = strokesRef.push().getKey();
        if (strokeKey == null) { Log.e(TAG, "Failed to generate unique stroke key from Firebase."); Toast.makeText(this, "Error saving drawing stroke.", Toast.LENGTH_SHORT).show(); return; }
        Log.d(TAG, "Generated Firebase push key for new stroke: " + strokeKey);

        Map<String, Object> strokeData = new HashMap<>();
        strokeData.put("color", completedStroke.getColor());
        strokeData.put("strokeWidth", completedStroke.getStrokeWidth());
        strokeData.put("userId", completedStroke.getUserId());
        strokeData.put("timestamp", ServerValue.TIMESTAMP);
        strokeData.put("points", completedStroke.getPoints());
        strokeData.put("toolType", completedStroke.getToolType());

        strokesRef.child(strokeKey).setValue(strokeData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Stroke saved successfully to Firebase with key: " + strokeKey))
                .addOnFailureListener(e -> { Log.e(TAG, "Failed to save stroke to Firebase.", e); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to save drawing stroke.", Toast.LENGTH_SHORT).show(); });
    }

    private void loadExistingStrokesAndListen() {
        if (strokesRef == null) { Log.e(TAG, "loadExistingStrokesAndListen: strokesRef is null. Cannot load/listen."); Toast.makeText(this, "Error loading drawing data.", Toast.LENGTH_SHORT).show(); setDrawingEnabled(false); return; }
        Log.d(TAG, "Loading existing strokes and setting up listener for session: " + sessionId);
        if (loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
        setDrawingEnabled(false);

        strokesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Finished loading initial strokes. Count: " + snapshot.getChildrenCount());
                List<Stroke> initialStrokes = new ArrayList<>();
                for (DataSnapshot strokeSnapshot : snapshot.getChildren()) { Stroke stroke = parseStrokeFromSnapshot(strokeSnapshot); if (stroke != null) { initialStrokes.add(stroke); } } // <<< CORRECTED: Use strokeSnapshot here
                Collections.sort(initialStrokes, (s1, s2) -> Long.compare(s1.getTimestamp(), s2.getTimestamp()));
                if (drawingView != null) drawingView.addCompletedStrokes(initialStrokes);
                if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
                attachStrokesRealtimeListener();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Failed to load initial strokes from Firebase: " + error.getMessage(), error.toException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to load drawing history.", Toast.LENGTH_SHORT).show(); if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE); setDrawingEnabled(false); }
        });
    }

    private Stroke parseStrokeFromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) { Log.w(TAG, "parseStrokeFromSnapshot received null or non-existent snapshot."); return null; }
        String strokeKey = snapshot.getKey(); try { Integer colorInt = snapshot.child("color").getValue(Integer.class); int color = (colorInt != null) ? colorInt : Color.BLACK;
            Float strokeWidthFloat = snapshot.child("strokeWidth").getValue(Float.class); float strokeWidth = (strokeWidthFloat != null) ? strokeWidthFloat : 12f;
            String userId = snapshot.child("userId").getValue(String.class); if (userId == null || userId.isEmpty()) { Log.w(TAG, "Stroke data missing userId or is empty. Skipping. Stroke key: " + strokeKey); return null; }
            Long timestampLong = snapshot.child("timestamp").getValue(Long.class); long timestamp = (timestampLong != null) ? timestampLong : 0L;
            Integer toolTypeInt = snapshot.hasChild("toolType") ? snapshot.child("toolType").getValue(Integer.class) : DrawingView.TOOL_PEN; int toolType = (toolTypeInt != null) ? toolTypeInt : DrawingView.TOOL_PEN;
            List<Point> points = new ArrayList<>(); DataSnapshot pointsSnapshot = snapshot.child("points");
            if (pointsSnapshot.exists() && pointsSnapshot.hasChildren()) { for (DataSnapshot pointSnap : pointsSnapshot.getChildren()) { Point point = pointSnap.getValue(Point.class); if (point != null) { points.add(point); } else { Log.w(TAG, "Failed to parse Point for stroke " + strokeKey); } } }
            if (points.isEmpty()) { Log.w(TAG, "Parsed stroke has no points. Skipping. Stroke key: " + strokeKey); return null; }
            return new Stroke(points, color, strokeWidth, userId, timestamp, toolType); }
        catch (Exception e) { Log.e(TAG, "Error parsing stroke data from snapshot: " + strokeKey, e); return null; }
    }

    @SuppressLint("RestrictedApi")
    private void attachStrokesRealtimeListener() {
        if (strokesRef == null) { Log.e(TAG, "Cannot attach strokes listener, strokesRef is null."); return; }
        if (strokesChildEventListener == null) { Log.d(TAG, "Attaching Firebase ChildEventListener for strokes sync for session: " + sessionId);
            strokesChildEventListener = new ChildEventListener() {
                @Override public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { Log.d(TAG, "🎨 onChildAdded triggered for stroke ID: " + snapshot.getKey()); Stroke stroke = parseStrokeFromSnapshot(snapshot); if (stroke != null) { if (drawingView != null) drawingView.addCompletedStroke(stroke); } else { Log.w(TAG, "Failed to parse stroke data on onChildAdded for key: " + snapshot.getKey()); } }
                @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { Log.d(TAG, "🎨 onChildChanged triggered for stroke ID: " + snapshot.getKey() + ". Handling not implemented."); }
                @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { Log.d(TAG, "❌ onChildRemoved triggered for stroke ID: " + snapshot.getKey() + "."); if (drawingView != null) drawingView.clearDrawing(); }
                @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { Log.d(TAG, "🎨 onChildMoved triggered for stroke ID: " + snapshot.getKey() + ". Handling not relevant."); }
                @Override public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Firebase Strokes Listener cancelled for session " + sessionId + ": " + error.getMessage(), error.toException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to sync drawing updates: " + error.getMessage(), Toast.LENGTH_SHORT).show(); setDrawingEnabled(false); }
            };
            strokesRef.addChildEventListener(strokesChildEventListener); Log.d(TAG, "Firebase strokes sync listener attached to: " + strokesRef.getPath()); }
    }

    private void removeFirebaseListeners() {
        if (strokesRef != null && strokesChildEventListener != null) { strokesRef.removeEventListener(strokesChildEventListener); strokesChildEventListener = null; Log.d(TAG, "Removed Firebase strokes listener."); }
        if (drawingSessionRef != null && sessionStateListener != null) { drawingSessionRef.removeEventListener(sessionStateListener); sessionStateListener = null; Log.d(TAG, "Removed Firebase session state listener."); }
        if (activeSessionUsersRef != null && activeUsersListener != null) { activeSessionUsersRef.removeEventListener(activeUsersListener); activeUsersListener = null; Log.d(TAG, "Removed Firebase active users listener."); }
    }

    private void confirmClearDrawing() {
        if (sessionStarterId == null || currentUserID == null || !Objects.equals(currentUserID, sessionStarterId) || !"active".equals(currentSessionState)) { Toast.makeText(this, "Only the session starter can clear the drawing.", Toast.LENGTH_SHORT).show(); Log.w(TAG, "Non-initiator (" + currentUserID + ") attempted to clear drawing for session " + sessionId); return; }
        Log.d(TAG, "Showing clear drawing confirmation dialog."); new AlertDialog.Builder(this).setTitle("Clear Drawing").setMessage("Are you sure you want to clear the entire drawing board? This cannot be undone for anyone.").setPositiveButton("Yes, Clear", (dialog, which) -> clearDrawing()).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    private void clearDrawing() {
        if (sessionStarterId == null || currentUserID == null || !Objects.equals(currentUserID, sessionStarterId) || !"active".equals(currentSessionState)) { Toast.makeText(this, "Authorization or state error preventing clear.", Toast.LENGTH_SHORT).show(); Log.w(TAG, "Authorization/State check failed during clearDrawing execution."); return; }
        Log.d(TAG, "Initiator " + currentUserID + " clearing drawing for session " + sessionId + " from Firebase.");
        if (strokesRef != null) { strokesRef.removeValue().addOnCompleteListener(task -> { if (task.isSuccessful()) { Log.d(TAG, "All strokes removed from Firebase for session " + sessionId); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Drawing cleared for everyone.", Toast.LENGTH_SHORT).show(); if (drawingView != null) drawingView.clearDrawing(); } else { Log.e(TAG, "Failed to clear strokes from Firebase.", task.getException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to clear drawing.", Toast.LENGTH_SHORT).show(); }}); }
        else { Log.e(TAG, "strokesRef is null, cannot clear drawing from Firebase."); Toast.makeText(this, "Error clearing drawing.", Toast.LENGTH_SHORT).show(); }
    }

    private void confirmEndSession() {
        if (sessionStarterId == null || currentUserID == null || !Objects.equals(currentUserID, sessionStarterId) || !"active".equals(currentSessionState)) { Toast.makeText(this, "Only the session starter can end the session.", Toast.LENGTH_SHORT).show(); Log.w(TAG, "Non-initiator (" + currentUserID + ") attempted to end session."); return; }
        if (!"active".equals(currentSessionState)) { Toast.makeText(this, "Session is already ended or not active.", Toast.LENGTH_SHORT).show(); return; }
        Log.d(TAG, "Showing end session confirmation dialog."); new AlertDialog.Builder(this).setTitle("End Drawing Session").setMessage("Are you sure you want to end this drawing session? No one will be able to draw after it's ended.").setPositiveButton("Yes, End", (dialog, which) -> endDrawingSession()).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    private void endDrawingSession() {
        if (sessionStarterId == null || currentUserID == null || !Objects.equals(currentUserID, sessionStarterId) || !"active".equals(currentSessionState)) { Toast.makeText(this, "Authorization or state error preventing end.", Toast.LENGTH_SHORT).show(); Log.w(TAG, "Authorization/State check failed during endDrawingSession execution."); return; }
        Log.d(TAG, "Initiator " + currentUserID + " ending session " + sessionId + " by setting state to 'ended'.");
        if (sessionStateRef != null) { sessionStateRef.setValue("ended").addOnCompleteListener(task -> { if (task.isSuccessful()) { Log.d(TAG, "Drawing session state set to 'ended' in Firebase."); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Drawing session ended for everyone.", Toast.LENGTH_SHORT).show(); } else { Log.e(TAG, "Failed to set session state to 'ended'.", task.getException()); Toast.makeText(YOUR_DRAWING_ACTIVITY_CLASS.this, "Failed to end session.", Toast.LENGTH_SHORT).show(); }}); }
        else { Log.e(TAG, "sessionStateRef is null, cannot end session."); Toast.makeText(this, "Error ending session.", Toast.LENGTH_SHORT).show(); }
    }

    private String getCurrentSessionState() { return this.currentSessionState; }


    // --- Activity's State Variables for Tool Selection Visual Feedback ---
//    private int currentActivityColor = Color.BLACK;
//    private float currentActivityStrokeWidth = 12f;
//    private int currentActivityTool = DrawingView.TOOL_PEN;
//
//    // --- Activity's State Variables for Pan and Zoom ---
//    private float currentActivityScaleFactor = 1.0f;
//    private float currentActivityPosX = 0.0f;
//    private float currentActivityPosY = 0.0f;
//
//    // Constants for Pan/Zoom amounts
//    private static final float SCROLL_AMOUNT = 50f; // Pixels to scroll per click
//    private static final float ZOOM_FACTOR = 1.2f; // Factor to zoom in/out per click


    // --- Helper method to visually indicate the selected color button (using backgroundTint for Base Color + Highlight) ---
    private void updateColorButtonState() {
        Button[] colorButtons = {btnColorBlack, btnColorRed, btnColorBlue, btnColorGreen, btnColorYellow, btnColorPurple};
        int[] colors = {Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.parseColor("#9C27B0")};
        int selectedHighlightTint = getResources().getColor(R.color.colorAccent, getTheme());
        ColorStateList defaultTintList = null;

        for (int i = 0; i < colorButtons.length; i++) {
            Button button = colorButtons[i];
            if (button != null) {
                int buttonRepresentsColor = colors[i];
                button.setBackgroundTintList(ColorStateList.valueOf(buttonRepresentsColor)); // Set base color tint

                if (buttonRepresentsColor == currentActivityColor) {
                    button.setBackgroundTintList(ColorStateList.valueOf(selectedHighlightTint)); // Apply highlight tint
                    Log.d(TAG, "Color button selected: " + String.format("#%06X", (0xFFFFFF & currentActivityColor)) + ". Applied highlight tint.");
                } else {
                    button.setBackgroundTintList(ColorStateList.valueOf(buttonRepresentsColor)); // Ensure base color tint
                    Log.d(TAG, "Color button unselected: " + String.format("#%06X", (0xFFFFFF & buttonRepresentsColor)) + ". Set base tint.");
                }
            }
        }
        Log.d(TAG, "Color button state updated. Current color: " + String.format("#%06X", (0xFFFFFF & currentActivityColor)));
    }

    // --- Helper method to visually indicate the selected stroke size button (using backgroundTint) ---
    private void updateStrokeSizeButtonState() {
        TextView[] sizeButtons = {btnStrokeSmall, btnStrokeMedium, btnStrokeLarge};
        final float SMALL_STROKE = 5f;
        final float MEDIUM_STROKE = 12f;
        final float LARGE_STROKE = 25f;
        float[] widths = {SMALL_STROKE, MEDIUM_STROKE, LARGE_STROKE};

        int selectedHighlightTint = getResources().getColor(R.color.colorAccent, getTheme());
        ColorStateList defaultTintList = null;

        int[] baseShapeDrawables = {
                R.drawable.stroke_width_indicator_small,
                R.drawable.stroke_width_indicator_medium,
                R.drawable.stroke_width_indicator_large
        };

        for (int i = 0; i < sizeButtons.length; i++) {
            TextView textView = sizeButtons[i];
            if (textView != null) {
                textView.setBackgroundResource(baseShapeDrawables[i]); // Set the base shape drawable

                if (widths[i] == currentActivityStrokeWidth) {
                    textView.setBackgroundTintList(ColorStateList.valueOf(selectedHighlightTint));
                    Log.d(TAG, "Size button selected: " + currentActivityStrokeWidth + ". Applied highlight tint.");
                } else {
                    textView.setBackgroundTintList(defaultTintList);
                    Log.d(TAG, "Size button unselected: " + widths[i] + ". Removed highlight tint.");
                }
            }
        }
        Log.d(TAG, "Stroke size button state updated. Current width: " + currentActivityStrokeWidth);
    }


    private int getStrokeWidthDrawableResId(float width) {
        final float SMALL_STROKE = 5f;
        final float MEDIUM_STROKE = 12f;
        final float LARGE_STROKE = 25f;
        if (width == SMALL_STROKE) return R.drawable.stroke_width_indicator_small;
        if (width == MEDIUM_STROKE) return R.drawable.stroke_width_indicator_medium;
        if (width == LARGE_STROKE) return R.drawable.stroke_width_indicator_large;
        return 0;
    }

    private void updateToolButtonState() {
        int selectedHighlightColor = getResources().getColor(R.color.colorAccent, getTheme());
        if (btnToolEraser != null) {
            if (currentActivityTool == DrawingView.TOOL_ERASER) {
                btnToolEraser.setBackgroundTintList(ColorStateList.valueOf(selectedHighlightColor));
                Log.d(TAG, "Tool button selected: Eraser. Highlighted btnToolEraser.");
            } else {
                btnToolEraser.setBackgroundTintList(null);
                Log.d(TAG, "Tool button selected: Pen (Eraser not highlighted)");
            }
        }
    }
    // --- NEW Helper method to update the visual state of Pan/Zoom buttons ---
    private void updatePanZoomButtonState() {
        // You can add visual feedback for these buttons too if needed
        // E.g., change tint or icon when clicked.
        // For now, just a placeholder.
        Log.d(TAG, "Pan/Zoom button visual state update called.");
    }

}