package com.sana.circleup.encryptionfiles;

 // <<< Make sure this package name matches where you create the file

import android.text.TextUtils;

import java.util.Arrays;

public class ChatIdUtil {

    /**
     * Generates a consistent conversation ID for a pair of users by sorting their IDs.
     *
     * @param userId1 The ID of the first user.
     * @param userId2 The ID of the second user.
     * @return A unique and consistent conversation ID string (e.g., "id1_id2" where id1 < id2 alphabetically), or null if inputs are invalid.
     */
    public static String generateConversationId(String userId1, String userId2) {
        // Basic validation
        if (TextUtils.isEmpty(userId1) || TextUtils.isEmpty(userId2)) {
            // Handle this error case - cannot create an ID without valid user IDs
            return null;
        }

        // Sort the IDs alphabetically
        String[] ids = {userId1, userId2};
        Arrays.sort(ids); // Sorts the array in place

        // Combine the sorted IDs with a separator
        return ids[0] + "_" + ids[1];
    }



    // You can add other static chat-related utility methods here if needed
}