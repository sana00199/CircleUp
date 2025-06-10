package com.sana.circleup;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.DraftViewHolder> {
    private Context context;
    private List<DraftMessage> draftsList;

    public DraftsAdapter(Context context, List<DraftMessage> draftsList) {
        this.context = context;
        this.draftsList = draftsList;
    }

    @NonNull
    @Override
    public DraftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.draft_item, parent, false);
        return new DraftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DraftViewHolder holder, int position) {
        DraftMessage draft = draftsList.get(position);
        holder.tvMessage.setText(draft.getMessage());
        holder.tvScheduledTime.setText(draft.getScheduledDateTime());

        holder.itemView.setOnClickListener(v -> {
            // On click, move draft message to scheduled messages and delete it from drafts
            sendDraftMessage(draft);
        });
    }

    @Override
    public int getItemCount() {
        return draftsList.size();
    }

    private void sendDraftMessage(DraftMessage draft) {
        // Send the draft message to the selected users
        for (String contactId : draft.getContactIds()) {
            // Implement the logic to send the message to the selected users.
            // After sending, remove it from drafts
            FirebaseDatabase.getInstance().getReference("Drafts")
                    .orderByChild("message")
                    .equalTo(draft.getMessage())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().removeValue();  // Delete draft after sending
                            }

                            // Remove the draft from the local list and notify the adapter
                            draftsList.remove(draft);
                            notifyDataSetChanged();  // Correctly calling notifyDataSetChanged() on the current adapter
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("DraftsAdapter", "Failed to delete draft", databaseError.toException());
                        }
                    });
        }
    }

    public class DraftViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvScheduledTime;

        public DraftViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_draft_message);
            tvScheduledTime = itemView.findViewById(R.id.tv_draft_scheduled_time);
        }
    }
}
