package com.sana.circleup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DraftScheduled extends AppCompatActivity {

    private RecyclerView recyclerScheduled;
    private List<ScheMsg> scheduledMessages;
    private ScheduledMessagesAdapterDraft adapter;
    private TextView tvNoDrafts; // Add a reference to the TextView
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_draft_scheduled);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Drafts"); // Optional: Set title programmatically if not set in XML
        }

        recyclerScheduled = findViewById(R.id.recyclerScheduled);
        recyclerScheduled.setLayoutManager(new LinearLayoutManager(this));

        tvNoDrafts = findViewById(R.id.tvNoDrafts); // Initialize the TextView



        scheduledMessages = new ArrayList<>();
        adapter = new ScheduledMessagesAdapterDraft(this, scheduledMessages);
        recyclerScheduled.setAdapter(adapter);

        // Load scheduled messages from Firebase
        loadScheduledMessages();
    }




    // Handle toolbar item clicks (specifically the back button)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle back button press
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadScheduledMessages() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ScheduledMessages");

        ref.orderByChild("senderId").equalTo(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        scheduledMessages.clear();

                        List<ScheMsg> tempList = new ArrayList<>();
                        int totalMessages = (int) snapshot.getChildrenCount();

                        if (totalMessages == 0) {
                            tvNoDrafts.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        final int[] processedCount = {0};

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ScheMsg message = ds.getValue(ScheMsg.class);
                            if (message != null) {
                                getReceiverNames(message, () -> {
                                    tempList.add(message);
                                    processedCount[0]++;

                                    // Check if all messages have been processed
                                    if (processedCount[0] == totalMessages) {
                                        scheduledMessages.clear();
                                        scheduledMessages.addAll(tempList);
                                        adapter.notifyDataSetChanged();

                                        // Show/hide "No Drafts"
                                        tvNoDrafts.setVisibility(scheduledMessages.isEmpty() ? View.VISIBLE : View.GONE);
                                    }
                                });
                            } else {
                                processedCount[0]++;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DraftScheduled.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Method to retrieve the receiver's name from Firebase
    // Method to retrieve the names of multiple receivers from Firebase
    private void getReceiverNames(ScheMsg message, Runnable onComplete) {
        if (message.getReceiverIds() == null || message.getReceiverIds().isEmpty()) {
            Log.e("DraftScheduled", "Receiver IDs are null or empty");
            onComplete.run();
            return;
        }

        List<String> receiverNames = new ArrayList<>();
        int total = message.getReceiverIds().size();
        final int[] counter = {0};

        for (String receiverId : message.getReceiverIds()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(receiverId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (user != null) {
                        receiverNames.add(user.getUsername());
                    }
                    counter[0]++;
                    if (counter[0] == total) {
                        message.setReceiverNames(receiverNames);
                        String receiverNamesStr = TextUtils.join(", ", receiverNames);
                        message.setReceiverNamesStr(receiverNamesStr);
                        onComplete.run(); // Callback once all names fetched
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    counter[0]++;
                    if (counter[0] == total) {
                        onComplete.run();
                    }
                }
            });
        }
    }

}
