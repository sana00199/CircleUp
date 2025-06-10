package com.sana.circleup.room_db_implement;



import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;



import java.util.List;

@Dao
public interface ContactDao {

    // Insert or replace a contact. If a contact with the same contactId exists, it will be replaced.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateContact(ContactEntity contact);

    // Get all contacts for a specific user (ownerUserId), ordered by name.
    // Returns LiveData, so observers are notified when data changes.
    @Query("SELECT * FROM contacts WHERE ownerUserId = :ownerUserId ORDER BY name ASC")
    LiveData<List<ContactEntity>> getAllContactsForUser(String ownerUserId);




    // Delete a specific contact entry by its unique contactId.
    @Query("DELETE FROM contacts WHERE contactId = :contactId")
    int deleteContactById(String contactId); // Return int for rows affected (optional)

    // Delete a specific contact entry using owner and contact UIDs
    @Query("DELETE FROM contacts WHERE ownerUserId = :ownerUserId AND contactUserId = :contactUserId")
    int deleteContactByUids(String ownerUserId, String contactUserId);


    // Delete all contacts for a specific user (e.g., on logout).
    @Query("DELETE FROM contacts WHERE ownerUserId = :ownerUserId")
    void deleteAllContactsForUser(String ownerUserId);

    // You might add other query methods here if needed
    // Example: Get a single contact by contactUserId for a given owner
    @Query("SELECT * FROM contacts WHERE ownerUserId = :ownerUserId AND contactUserId = :contactUserId LIMIT 1")
    ContactEntity getContactByUids(String ownerUserId, String contactUserId); // Not LiveData, for specific checks
}