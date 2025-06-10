package com.sana.circleup.room_db_implement; // Adjust package name if needed

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull; // Import NonNull
import androidx.annotation.Nullable; // Import Nullable if needed
//
//@Entity(tableName = "messages")
//public class MessageEntity {
//
//    // Primary Key is firebaseMessageId
//    @NonNull
//    @PrimaryKey
//    private String firebaseMessageId;
//
//    // Assuming ownerUserId is @NonNull in your schema
//    @NonNull
//    private String ownerUserId;
//
//    // Check nullability for these fields in your DB schema:
//    @Nullable private String message;
//    @Nullable private String type;
//
//    @NonNull private String from; // Assuming always non-null
//    @NonNull private String to;   // Assuming always non-null
//
//    @Nullable private String sendTime;
//    private boolean seen; // primitive boolean is non-null
//    @Nullable private String seenTime;
//    @Nullable private String status;
//    private long timestamp; // primitive long is non-null
//
//
//    // --- Constructor used by Room for database READS ---
//    // This constructor must take all fields that Room will read from the DB.
//    // It must be public and NOT @Ignore'd if it's for reads.
//    // Let's make this the 10-parameter one you were trying to use for display logic,
//    // assuming it matches the fields Room reads (excluding ownerUserId if Room somehow doesn't populate it via this constructor).
//    // But if ownerUserId is @NonNull and in schema, Room NEEDS a constructor that includes it.
//
//
//
//    // --- Constructor for creating NEW messages (used in ChatPageActivity) ---
//    // This constructor has the same signature as the one above.
//    // Room will be confused UNLESS one is annotated with @Ignore.
//    // We want ChatPageActivity's `new MessageEntity(...)` calls to use THIS one.
//    @Ignore // <<< Add @Ignore here to tell Room to ignore this for DB reads.
//    public MessageEntity(@NonNull String firebaseMessageId, @NonNull String ownerUserId,
//                         @Nullable String message, @Nullable String type,
//                         @NonNull String from, @NonNull String to,
//                         boolean seen, @Nullable String seenTime,
//                         @Nullable String status, long timestamp) {
//        // This body must assign the values.
//        this.firebaseMessageId = firebaseMessageId;
//        this.ownerUserId = ownerUserId;
//        this.message = message;
//        this.type = type;
//        this.from = from;
//        this.to = to;
//        this.sendTime = sendTime;
//        this.seen = seen;
//        this.seenTime = seenTime;
//        this.status = status;
//        this.timestamp = timestamp;
//    }
//
//
//    // Room might also need a public no-argument constructor
//    public MessageEntity() {
//        // Empty constructor for Room
//    }
//
//
//    // --- Getters and Setters (Required by Room and used in ChatPageActivity) ---
//    // Ensure getters match field nullability (@NonNull or @Nullable)
//
//    @NonNull public String getFirebaseMessageId() { return firebaseMessageId; }
//    // public void setFirebaseMessageId(@NonNull String firebaseMessageId) { this.firebaseMessageId = firebaseMessageId; } // Setter optional if constructor covers it
//
//    @NonNull public String getOwnerUserId() { return ownerUserId; }
//    // public void setOwnerUserId(@NonNull String ownerUserId) { this.ownerUserId = ownerUserId; } // Setter optional
//
//    @Nullable public String getMessage() { return message; }
//    public void setMessage(@Nullable String message) { this.message = message; }
//
//    @Nullable public String getType() { return type; }
//    // public void setType(@Nullable String type) { this.type = type; }
//
//    @NonNull public String getFrom() { return from; }
//    // public void setFrom(@NonNull String from) { this.from = from; }
//
//    @NonNull public String getTo() { return to; }
//    // public void setTo(@NonNull String to) { this.to = to; }
//
//    @Nullable public String getSendTime() { return sendTime; }
//    // public void setSendTime(@Nullable String sendTime) { this.sendTime = sendTime; }
//
//    public boolean isSeen() { return seen; }
//    public void setSeen(boolean seen) { this.seen = seen; }
//
//    @Nullable public String getSeenTime() { return seenTime; }
//    // public void setSeenTime(@Nullable String seenTime) { this.seenTime = seenTime; }
//
//    @Nullable public String getStatus() { return status; }
//    public void setStatus(@Nullable String status) { this.status = status; }
//
//    public long getTimestamp() { return timestamp; }
//    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
//
//    public void setSeenTime(String seenTimeStr) {
//
//        this.seenTime = seenTimeStr;
//    }
//
//    public void setFirebaseMessageId(@NonNull String firebaseMessageId) {
//        this.firebaseMessageId = firebaseMessageId;
//    }
//
//    public void setFrom(@NonNull String from) {
//        this.from = from;
//    }
//
//    public void setTo(@NonNull String to) {
//        this.to = to;
//    }
//
//    public void setOwnerUserId(@NonNull String ownerUserId) {
//        this.ownerUserId = ownerUserId;
//    }
//
//    public void setType(@Nullable String type) {
//        this.type = type;
//    }
//
//    public void setSendTime(@Nullable String sendTime) {
//        this.sendTime = sendTime;
//    }
//
//    // Add setters for fields you need to modify after object creation (like message field in observer)
//    // Example: public void setMessage(@Nullable String message) { this.message = message; } // Already added
//}
// Inside MessageEntity.java
@Entity(tableName = "messages_table")
public class MessageEntity {

    @NonNull
    @PrimaryKey
    private String firebaseMessageId;

    // This should be the ID of the user whose local DB this entry is in
    @NonNull
    private String ownerUserId;

    @Nullable private String message; // Encrypted content or plaintext will be stored here
    @Nullable private String type;
    @NonNull private String from;
    @NonNull private String to;

    @Nullable private String sendTime;
    private boolean seen;
    @Nullable private String seenTime;
    @Nullable private String status; // e.g., "pending", "sent", "failed", "received"
    private long timestamp;

    // --- NEW FIELD FOR SCHEDULED MESSAGES ---
    // This field will store the original scheduled time string (if applicable)
    // Its presence indicates this message was sent via the scheduler.
    @Nullable private String scheduledTime;
    // --- END NEW FIELD ---


    // --- Public no-argument constructor for Room ---
    public MessageEntity() {
        // Empty constructor required by Room
    }

    // --- DO NOT ADD OTHER PUBLIC CONSTRUCTORS WITH ARGUMENTS unless they are @Ignore'd ---
    // Example (if you *must* keep them for other reasons, although it's confusing):
    // @Ignore // Room will ignore this constructor for reading from the DB
    // public MessageEntity(@NonNull String firebaseMessageId, @NonNull String ownerUserId, ...) { ... }


    // --- Getters and Setters ---

    @NonNull public String getFirebaseMessageId() { return firebaseMessageId; }
    public void setFirebaseMessageId(@NonNull String firebaseMessageId) { this.firebaseMessageId = firebaseMessageId; }

    @NonNull public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(@NonNull String ownerUserId) { this.ownerUserId = ownerUserId; }

    @Nullable public String getMessage() { return message; }
    public void setMessage(@Nullable String message) { this.message = message; }

    @Nullable public String getType() { return type; }
    public void setType(@Nullable String type) { this.type = type; }

    @NonNull public String getFrom() { return from; }
    public void setFrom(@NonNull String from) { this.from = from; }

    @NonNull public String getTo() { return to; }
    public void setTo(@NonNull String to) { this.to = to; }

    @Nullable public String getSendTime() { return sendTime; }
    public void setSendTime(@Nullable String sendTime) { this.sendTime = sendTime; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    @Nullable public String getSeenTime() { return seenTime; }
    public void setSeenTime(@Nullable String seenTime) { this.seenTime = seenTime; }

    @Nullable public String getStatus() { return status; }
    public void setStatus(@Nullable String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }


    // --- Getter and Setter for the NEW scheduledTime field ---
    @Nullable
    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(@Nullable String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    // --- END Getter and Setter for scheduledTime ---
}