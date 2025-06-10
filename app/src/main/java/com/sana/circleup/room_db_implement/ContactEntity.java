package com.sana.circleup.room_db_implement;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull; // Add this import

// Define the table name. Index ownerUserId for efficient querying.
@Entity(tableName = "contacts", indices = @Index("ownerUserId"))
public class ContactEntity {

    // Primary key is the contact's UID + the owner's UID for uniqueness per user
    // Example: "contactUid_ownerUid"
    @PrimaryKey
    @NonNull // Primary key should be non-null
    private String contactId; // A unique ID for this contact entry (e.g., contactUid_ownerUid)

    @NonNull // The UID of the user whose contacts list this entry belongs to
    private String ownerUserId;

    @NonNull // The actual UID of the contact user
    private String contactUserId;

    // Store contact details fetched from /Users
    private String name;
    private String status;
    private String profileImageBase64;
    private boolean isOnline; // Store online status


    // Constructor (Room requires an empty constructor OR a constructor that takes all fields)
    // Using a constructor with fields for easier object creation
    public ContactEntity(@NonNull String contactId, @NonNull String ownerUserId, @NonNull String contactUserId, String name, String status, String profileImageBase64, boolean isOnline) {
        this.contactId = contactId;
        this.ownerUserId = ownerUserId;
        this.contactUserId = contactUserId;
        this.name = name;
        this.status = status;
        this.profileImageBase64 = profileImageBase64;
        this.isOnline = isOnline;
    }

    // --- Getters ---
    @NonNull
    public String getContactId() { return contactId; }

    @NonNull
    public String getOwnerUserId() { return ownerUserId; }

    @NonNull
    public String getContactUserId() { return contactUserId; }

    public String getName() { return name; }

    public String getStatus() { return status; }

    public String getProfileImageBase64() { return profileImageBase64; }

    public boolean isOnline() { return isOnline; }

    // --- Setters (Needed if using empty constructor or for updates) ---
    // Room can use setters for updates if you don't modify final fields
    public void setContactId(@NonNull String contactId) { this.contactId = contactId; }
    public void setOwnerUserId(@NonNull String ownerUserId) { this.ownerUserId = ownerUserId; }
    public void setContactUserId(@NonNull String contactUserId) { this.contactUserId = contactUserId; }
    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
    public void setOnline(boolean online) { isOnline = online; }


    // Helper method to generate the contactId from owner and contact UIDs
    // Ensure this logic is consistent throughout your app wherever you need this ID
    public static String generateContactId(String ownerUid, String contactUid) {
        // Use your ChatIdUtil or similar logic if you sort IDs
        // A simple concatenation is fine IF the ownerUid is always first here.
        // If you sort IDs for chat conversation ID, maybe use the same logic for consistency.
        // Example using simple concatenation assuming ownerUid is always the list owner:
        if (TextUtils.isEmpty(ownerUid) || TextUtils.isEmpty(contactUid)) return null;
        return ownerUid + "_" + contactUid; // Example: "yourUid_friendUid"
        // If you use ChatIdUtil: return ChatIdUtil.generateConversationId(ownerUid, contactUid) + "_contact"; // Add suffix if needed to avoid collision with conversation IDs
        // Using ownerUid_contactUid seems safer and clearer for a contact list entry ID.
    }
}

