package com.sana.circleup.encryptionfiles;



import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log; // Added for logging

import androidx.annotation.Nullable;

import javax.crypto.SecretKey;

//public class YourKeyManager {
//
//    private static YourKeyManager instance; // The single instance
//
//    private String currentUserId; // The ID of the currently logged-in user
//    private PublicKey userPublicKey; // The user's own public key
//    private PrivateKey userPrivateKey; // The user's decrypted private key
//    // private String passphraseUsedForDecryption; // Optional: You could store this if needed, but handle with extreme care
//
//    // Private constructor to prevent instantiation from outside
//
//    // Map to store decrypted conversation keys
//    // Key: Conversation ID (String), Value: Decrypted AES SecretKey
//    private Map<String, SecretKey> conversationKeys = new ConcurrentHashMap<>();
//
//
//    // Private constructor to prevent instantiation from outside
//    private YourKeyManager() {
//        // Initialize the map
//        conversationKeys = new ConcurrentHashMap<>();
//        Log.d("YourKeyManager", "KeyManager instance created.");
//    }
//
//
//    // Get the singleton instance
//    public static synchronized YourKeyManager getInstance() {
//        if (instance == null) {
//            instance = new YourKeyManager();
//        }
//        return instance;
//    }
//
//    /**
//     * Sets the keys for the current user session after successful decryption.
//     * Call this ONLY after decryption succeeds during login.
//     */
//    public void setKeys(String userId, PublicKey publicKey, PrivateKey privateKey) {
//        this.currentUserId = userId;
//        this.userPublicKey = publicKey;
//        this.userPrivateKey = privateKey; // Decrypted private key
//        // Log.d("YourKeyManager", "Keys set for user: " + userId + ", Private Key available: " + (privateKey != null));
//    }
//
//    /**
//     * Sets keys when private key decryption is NOT possible (e.g., user skipped setup, or cancelled).
//     * The private key will be null.
//     */
//    public void setPublicOnly(String userId, PublicKey publicKey) {
//        this.currentUserId = userId;
//        this.userPublicKey = publicKey;
//        this.userPrivateKey = null; // Private key is NOT available
//        // Log.d("YourKeyManager", "Public key set for user: " + userId + ", Private Key NOT available.");
//    }
//
//
//    /**
//     * Clears the keys, typically when the user logs out.
//     */
//    public void clearKeys() {
//        this.currentUserId = null;
//        this.userPublicKey = null;
//        this.userPrivateKey = null;
//        this.conversationKeys.clear(); // Clear all cached conversation keys
//        Log.d("YourKeyManager", "All keys cleared on logout.");
//    }
//
//    // --- Getters ---
//
//    public String getCurrentUserId() {
//        return currentUserId;
//    }
//
//    /**
//     * Gets the user's own public key. May be null if not loaded or available.
//     */
//    public PublicKey getUserPublicKey() {
//        return userPublicKey;
//    }
//
//    /**
//     * Gets the user's decrypted private key.
//     * This will be null if decryption failed, user skipped the setup, or they cancelled passphrase input.
//     */
//    public PrivateKey getUserPrivateKey() {
//        return userPrivateKey;
//    }
//
//    /**
//     * Checks if the user's private key is available (i.e., successfully decrypted and loaded).
//     */
//    public boolean isPrivateKeyAvailable() {
//        return userPrivateKey != null;
//    }
//
//    // You will add methods later to manage CONVERSATION keys here or in a related class.
//    // This class is mainly for the USER's persistent Public/Private key pair.
//    public void setConversationKey(String conversationId, SecretKey aesKey) {
//        if (conversationId != null && aesKey != null) {
//            conversationKeys.put(conversationId, aesKey);
//            Log.d("YourKeyManager", "Conversation key set for ID: " + conversationId);
//        }
//    }
//
//    /**
//     * Retrieves a decrypted conversation AES key from memory.
//     *
//     * @param conversationId The ID of the conversation.
//     * @return The SecretKey if available, null otherwise.
//     */
//    public SecretKey getConversationKey(String conversationId) {
//        if (conversationId != null) {
//            SecretKey key = conversationKeys.get(conversationId);
//            if (key == null) {
//                Log.w("YourKeyManager", "Attempted to get non-existent conversation key for ID: " + conversationId);
//            }
//            return key;
//        }
//        Log.w("YourKeyManager", "Attempted to get conversation key with null ID.");
//        return null;
//    }
//
//    /**
//     * Checks if a decrypted conversation key is available in memory for a given conversation.
//     */
//    public boolean hasConversationKey(String conversationId) {
//        return conversationId != null && conversationKeys.containsKey(conversationId) && conversationKeys.get(conversationId) != null;
//    }
//
//    /**
//     * Removes a conversation key from memory (e.g., when chat activity is finished, or conversation is deleted).
//     * You might manage this based on active conversations or a cache limit.
//     */
//    public void removeConversationKey(String conversationId) {
//        if (conversationId != null) {
//            conversationKeys.remove(conversationId);
//            Log.d("YourKeyManager", "Conversation key removed from cache for ID: " + conversationId);
//        }
//    }
//
//    // --- End Methods for Conversation Keys ---
//
//    // You might add methods later for group chat keys if applicable.
//}


import java.util.HashMap; // Added for getAllConversationKeys

// Add imports for StackTraceElement if you use getStackTraceElement
import java.lang.StackTraceElement;
import java.lang.Thread;
//
//public class YourKeyManager {
//
//    private static final String TAG = "YourKeyManager";
//    // Use volatile for thread-safe singleton, ensures visibility of changes across threads
//    private static volatile YourKeyManager instance;
//
//    private String currentUserId; // The ID of the currently logged-in user
//    private PublicKey userPublicKey; // The user's own public key
//    private PrivateKey userPrivateKey; // The user's decrypted private key
//
//    // Map to store decrypted conversation keys (Thread-safe ConcurrentHashMap)
//    // Key: Conversation ID (String), Value: Decrypted AES SecretKey
//    private final Map<String, SecretKey> conversationKeys;
//
//
//    // Private constructor to prevent instantiation from outside
//    private YourKeyManager() {
//        // Initialize the map
//        conversationKeys = new ConcurrentHashMap<>();
//        Log.d(TAG, "YourKeyManager instance created.");
//    }
//
//
//    // Get the singleton instance (Thread-safe double-checked locking)
//    public static YourKeyManager getInstance() {
//        // Log caller when getInstance is called
//        String caller = getCallerClassName();
//        if (instance == null) {
//            synchronized (YourKeyManager.class) {
//                if (instance == null) {
//                    instance = new YourKeyManager();
//                    Log.d(TAG, "getInstance called by: " + caller + ". New instance CREATED.");
//                } else {
//                    // This branch should ideally not be hit often after initial creation
//                    Log.d(TAG, "getInstance called by: " + caller + ". Existing instance returned (inside sync).");
//                }
//            }
//        } else {
//            Log.d(TAG, "getInstance called by: " + caller + ". Existing instance returned (outside sync).");
//        }
//        return instance;
//    }
//
//    // Helper to get the calling class name for logging
//    private static String getCallerClassName() {
//        try {
//            // StackTraceElement[0] is getStackTrace
//            // StackTraceElement[1] is this getCallerClassName method
//            // StackTraceElement[2] is the method that called getCallerClassName (i.e., getInstance, setKeys, etc.)
//            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//            if (elements.length > 3) { // Need at least 4 elements to get the caller of the caller
//                return elements[3].getClassName() + "." + elements[3].getMethodName(); // Include method name for more context
//            }
//        } catch (Exception e) {
//            // Should not happen, but defensive
//            Log.e(TAG, "Error getting caller class name", e);
//        }
//        return "UnknownCaller"; // Fallback
//    }
//
//
//    /**
//     * Sets the keys for the current user session after successful decryption.
//     * Call this ONLY after decryption succeeds during login or key load.
//     * Clears any previously cached conversation keys.
//     */
//    public synchronized void setKeys(String userId, PublicKey publicKey, PrivateKey privateKey) {
//        String caller = getCallerClassName();
//        Log.d(TAG, "setKeys called by: " + caller + " for user: " + userId + ", Private Key available: " + (privateKey != null) + ". Clearing existing conversation keys (" + conversationKeys.size() + ").");
//
//        this.currentUserId = userId;
//        this.userPublicKey = publicKey;
//        this.userPrivateKey = privateKey; // Decrypted private key
//        this.conversationKeys.clear(); // Clear old keys for a new session/user
//
//        Log.d(TAG, "Keys set for user: " + userId + ". New conversation key count: " + conversationKeys.size());
//    }
//
//    /**
//     * Sets keys when private key decryption is NOT possible (e.g., user skipped setup, or cancelled passphrase input).
//     * Only the public key will be stored. The private key remains null.
//     * Clears any previously cached conversation keys.
//     */
//    public synchronized void setPublicOnly(String userId, PublicKey publicKey) {
//        String caller = getCallerClassName();
//        Log.d(TAG, "setPublicOnly called by: " + caller + " for user: " + userId + ". Private Key will be NOT available. Clearing existing conversation keys (" + conversationKeys.size() + ").");
//
//        this.currentUserId = userId;
//        this.userPublicKey = publicKey;
//        this.userPrivateKey = null; // Private key is NOT available
//        this.conversationKeys.clear(); // Clear old keys for a new session/user
//
//        Log.d(TAG, "Public key set for user: " + userId + ". New conversation key count: " + conversationKeys.size());
//    }
//
//
//    /**
//     * Clears all keys and cached data, typically when the user logs out or their account is deleted.
//     */
//    public synchronized void clearKeys() {
//        // Capture the stack trace at this moment
//        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//        String callerInfo = "Unknown";
//        // Iterate through the stack trace to find the first external method call
//        for (int i = 3; i < elements.length; i++) { // Start from 3 or 4 depending on how getStackTrace works exactly in your environment
//            String className = elements[i].getClassName();
//            // Exclude internal YourKeyManager and java/android framework calls
//            if (!className.startsWith("com.sana.circleup.encryptionfiles.YourKeyManager")
//                    && !className.startsWith("java.")
//                    && !className.startsWith("android.")
//                    && !className.startsWith("dalvik.")) { // Add other package prefixes if needed
//                callerInfo = className + "." + elements[i].getMethodName();
//                break; // Found the first relevant external caller
//            }
//        }
//
//        Log.d(TAG, "clearKeys called by: " + callerInfo + ". Clearing all keys. Current state: Private Available=" + (userPrivateKey != null) + ", Conv Keys loaded=" + conversationKeys.size());
//
//        this.currentUserId = null;
//        this.userPublicKey = null;
//        this.userPrivateKey = null;
//        this.conversationKeys.clear(); // Clear all cached conversation keys
//
//        Log.d(TAG, "All keys cleared from KeyManager.");
//    }
//    // --- Getters ---
//
//    public String getCurrentUserId() {
//        // Log caller when getCurrentUserId is called if needed, can be very verbose
//        // String caller = getCallerClassName();
//        // Log.v(TAG, "getCurrentUserId called by: " + caller);
//        return currentUserId;
//    }
//
//    /**
//     * Gets the user's own public key. May be null if not loaded or available.
//     */
//    public PublicKey getUserPublicKey() {
//        // Log caller if needed
//        // String caller = getCallerClassName();
//        // Log.v(TAG, "getUserPublicKey called by: " + caller + ". Available: " + (userPublicKey != null));
//        return userPublicKey;
//    }
//
//    /**
//     * Gets the user's decrypted private key.
//     * This will be null if decryption failed, user skipped the setup, or they cancelled passphrase input.
//     */
//    public PrivateKey getUserPrivateKey() {
//        // Log caller if needed
//        // String caller = getCallerClassName();
//        // Log.v(TAG, "getUserPrivateKey called by: " + caller + ". Available: " + (userPrivateKey != null));
//        return userPrivateKey;
//    }
//
//    /**
//     * Checks if the user's private key is available (i.e., successfully decrypted and loaded).
//     */
//    public boolean isPrivateKeyAvailable() {
//        String caller = getCallerClassName();
//        // Log this check as it's key to the bug
//        Log.d(TAG, "isPrivateKeyAvailable called by: " + caller + ". Result: " + (userPrivateKey != null));
//        return userPrivateKey != null;
//    }
//
//    // --- Methods for managing CONVERSATION keys ---
//
//    /**
//     * Stores a decrypted conversation AES key in memory.
//     *
//     * @param conversationId The ID of the conversation.
//     * @param aesKey The decrypted SecretKey for the conversation.
//     */
//    public void setConversationKey(String conversationId, SecretKey aesKey) {
//        String caller = getCallerClassName();
//        if (conversationId != null && aesKey != null) {
//            conversationKeys.put(conversationId, aesKey);
//            Log.d(TAG, "setConversationKey called by: " + caller + " for ID: " + conversationId + ". Current count: " + conversationKeys.size());
//        } else {
//            Log.w(TAG, "setConversationKey called by: " + caller + " with null ID or key.");
//        }
//    }
//
//    /**
//     * Retrieves a decrypted conversation AES key from memory.
//     *
//     * @param conversationId The ID of the conversation.
//     * @return The SecretKey if available in the cache, null otherwise.
//     */
//    @Nullable
//    public SecretKey getConversationKey(String conversationId) {
//        String caller = getCallerClassName();
//        if (conversationId != null) {
//            SecretKey key = conversationKeys.get(conversationId);
//            if (key == null) {
//                // This log is already in the method, keep it but maybe adjust verbosity
//                // Log.d(TAG, "getConversationKey called by: " + caller + ". Conversation key not found in cache for ID: " + conversationId);
//            } else {
//                // Log that the key WAS found
//                Log.d(TAG, "getConversationKey called by: " + caller + ". Conversation key FOUND in cache for ID: " + conversationId);
//            }
//            return key;
//        }
//        Log.w(TAG, "getConversationKey called by: " + caller + " with null ID.");
//        return null;
//    }
//
//    /**
//     * Checks if a decrypted conversation key is available in memory for a given conversation.
//     */
//    public boolean hasConversationKey(String conversationId) {
//        String caller = getCallerClassName();
//        boolean hasKey = conversationId != null && conversationKeys.containsKey(conversationId) && conversationKeys.get(conversationId) != null;
//        // This check is called very frequently in ChatFragment's observer.
//        // Make this log verbose (V) or debug (D) based on how much detail you need.
//        // Let's make it D for now.
//        Log.d(TAG, "hasConversationKey called by: " + caller + " for ID: " + conversationId + ". Exists: " + hasKey);
//        return hasKey;
//    }
//
//    /**
//     * Removes a conversation key from memory (e.g., when chat activity is finished, or conversation is deleted).
//     * You might manage this based on active conversations or a cache limit.
//     */
//    public void removeConversationKey(String conversationId) {
//        String caller = getCallerClassName();
//        if (conversationId != null) {
//            if (conversationKeys.containsKey(conversationId)) {
//                conversationKeys.remove(conversationId);
//                Log.d(TAG, "removeConversationKey called by: " + caller + ". Conversation key removed from cache for ID: " + conversationId);
//            } else {
//                Log.d(TAG, "removeConversationKey called by: " + caller + ". Attempted to remove non-existent conversation key for ID: " + conversationId);
//            }
//        } else {
//            Log.w(TAG, "removeConversationKey called by: " + caller + " with null ID.");
//        }
//    }
//
//    /**
//     * Returns a map containing all currently loaded conversation keys.
//     * Added for debugging purposes to check the size of the cache.
//     * Returns a copy to prevent external modification.
//     */
//    public Map<String, SecretKey> getAllConversationKeys() { // Corrected return type
//        String caller = getCallerClassName();
//        // This can also be called frequently, adjust log level if needed.
//        Log.d(TAG, "getAllConversationKeys called by: " + caller + ". Returning map with size: " + conversationKeys.size());
//        return new HashMap<>(conversationKeys); // Return a copy
//    }
//
//    // --- End Methods for Conversation Keys ---
//
//    // You might add methods later for group chat keys if applicable.
//    // Group chat keys would also be SecretKeys stored in the conversationKeys map, keyed by groupId.
//}


import androidx.annotation.NonNull;

//
//public class YourKeyManager {
//
//    private static final String TAG = "YourKeyManager";
//    private static volatile YourKeyManager instance; // Use volatile for thread-safe singleton
//
//    private String currentUserId; // The ID of the currently logged-in user
//    private PublicKey userPublicKey; // The user's own public key
//    private PrivateKey userPrivateKey; // The user's decrypted private key
//
//    // *** MODIFIED MAP TYPE: Map Conversation ID to a Map of Timestamp to SecretKey ***
//    private final Map<String, ConcurrentMap<Long, SecretKey>> conversationKeys; // Use ConcurrentMap for inner map as well
//
//    // Private constructor to prevent instantiation from outside
//    private YourKeyManager() {
//        // Initialize the outer map
//        conversationKeys = new ConcurrentHashMap<>(); // Use ConcurrentHashMap for thread-safe outer map
//        Log.d(TAG, "YourKeyManager instance created with multi-key support.");
//    }
//
//    // Get the singleton instance (Thread-safe double-checked locking)
//    public static YourKeyManager getInstance() {
//        String caller = getCallerClassName();
//        if (instance == null) {
//            synchronized (YourKeyManager.class) {
//                if (instance == null) {
//                    instance = new YourKeyManager();
//                    Log.d(TAG, "getInstance called by: " + caller + ". New instance CREATED.");
//                } else {
//                    Log.d(TAG, "getInstance called by: " + caller + ". Existing instance returned (inside sync).");
//                }
//            }
//        } else {
//            Log.d(TAG, "getInstance called by: " + caller + ". Existing instance returned (outside sync).");
//        }
//        return instance;
//    }
//
//    // Helper to get the calling class name for logging
//    private static String getCallerClassName() {
//        try {
//            // StackTraceElement[0] is getStackTrace, [1] is this method, [2] is the caller of this method
//            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//            if (elements.length > 3) { // Need at least 4 elements to get the caller of the caller
//                return elements[3].getClassName() + "." + elements[3].getMethodName();
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error getting caller class name", e);
//        }
//        return "UnknownCaller";
//    }
//
//
//    /**
//     * Sets the RSA keys for the current user session after successful decryption.
//     * Call this ONLY after RSA keys are successfully decrypted during login/unlock.
//     * Clears user's RSA keys and *all* previously cached conversation keys.
//     * Conversation keys should be loaded from Room *after* calling this method.
//     */
//    public synchronized void setKeys(String userId, PublicKey publicKey, PrivateKey privateKey) {
//        String caller = getCallerClassName();
//        Log.d(TAG, "setKeys called by: " + caller + " for user: " + userId + ", Private Key available: " + (privateKey != null) + ". Clearing ALL existing conversation keys (" + getTotalCachedConversationKeyVersions() + ")."); // Use helper for count
//
//        this.currentUserId = userId;
//        this.userPublicKey = publicKey;
//        this.userPrivateKey = privateKey; // Decrypted private key
//        this.conversationKeys.clear(); // *** CLEAR ALL cached conversation keys (clears outer map) ***
//
//        Log.d(TAG, "RSA keys set for user: " + userId + ". All cached conversation keys cleared.");
//    }
//
//    /**
//     * Sets keys when private key decryption is NOT possible (e.g., user skipped setup, cancelled passphrase).
//     * Only the public key will be stored. The private key remains null.
//     * Clears user's RSA keys and *all* previously cached conversation keys.
//     */
//    public synchronized void setPublicOnly(String userId, PublicKey publicKey) {
//        String caller = getCallerClassName();
//        Log.d(TAG, "setPublicOnly called by: " + caller + " for user: " + userId + ". Private Key will be NOT available. Clearing ALL existing conversation keys (" + getTotalCachedConversationKeyVersions() + ")."); // Use helper for count
//
//        this.currentUserId = userId;
//        this.userPublicKey = publicKey;
//        this.userPrivateKey = null; // Private key is NOT available
//        this.conversationKeys.clear(); // *** CLEAR ALL cached conversation keys ***
//
//        Log.d(TAG, "Public key set for user: " + userId + ". All cached conversation keys cleared.");
//    }
//
//    /**
//     * Clears all keys and cached data, typically when the user logs out or their account is deleted.
//     * Clears RSA keys and all cached conversation keys.
//     */
//    public synchronized void clearKeys() {
//        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//        String callerInfo = "Unknown";
//        for (int i = 3; i < elements.length; i++) {
//            String className = elements[i].getClassName();
//            if (!className.startsWith("com.sana.circleup.encryptionfiles.YourKeyManager")
//                    && !className.startsWith("java.")
//                    && !className.startsWith("android.")
//                    && !className.startsWith("dalvik.")) {
//                callerInfo = className + "." + elements[i].getMethodName();
//                break;
//            }
//        }
//
//        Log.d(TAG, "clearKeys called by: " + callerInfo + ". Clearing ALL keys. Current state: Private Available=" + (userPrivateKey != null) + ", Total Conv Key Versions cached=" + getTotalCachedConversationKeyVersions());
//
//        this.currentUserId = null;
//        this.userPublicKey = null;
//        this.userPrivateKey = null;
//        this.conversationKeys.clear(); // *** CLEAR ALL cached conversation keys ***
//
//        Log.d(TAG, "ALL keys cleared from KeyManager.");
//    }
//
//    // --- Getters ---
//
//    public String getCurrentUserId() {
//        return currentUserId;
//    }
//
//    /**
//     * Gets the user's own public key. May be null if not loaded or available.
//     */
//    public PublicKey getUserPublicKey() {
//        return userPublicKey;
//    }
//
//    /**
//     * Gets the user's decrypted private key.
//     * This will be null if decryption failed, user skipped the setup, or they cancelled passphrase input.
//     */
//    public PrivateKey getUserPrivateKey() {
//        return userPrivateKey;
//    }
//
//    /**
//     * Checks if the user's private key is available (i.e., successfully decrypted and loaded).
//     */
//    public boolean isPrivateKeyAvailable() {
//        String caller = getCallerClassName();
//        // Log this check as it's key to the bug
//        Log.d(TAG, "isPrivateKeyAvailable called by: " + caller + ". Result: " + (userPrivateKey != null));
//        return userPrivateKey != null;
//    }
//
//    // --- Methods for managing CONVERSATION keys (UPDATED) ---
//
//    /**
//     * Stores a decrypted conversation AES key version in memory for a specific conversation and timestamp.
//     *
//     * @param conversationId The ID of the conversation.
//     * @param keyTimestamp The timestamp identifying this key version (usually from Firebase).
//     * @param aesKey The decrypted SecretKey for this key version.
//     */
//    public void setConversationKey(@NonNull String conversationId, long keyTimestamp, @NonNull SecretKey aesKey) { // *** ADD keyTimestamp parameter ***
//        String caller = getCallerClassName();
//        if (conversationId != null && keyTimestamp > 0 && aesKey != null) {
//            // Get or create the inner map for this conversation
//            ConcurrentMap<Long, SecretKey> convKeys = conversationKeys.get(conversationId);
//            if (convKeys == null) {
//                convKeys = new ConcurrentHashMap<>();
//                // Use putIfAbsent to avoid race condition if multiple threads add the same conversation concurrently
//                ConcurrentMap<Long, SecretKey> existing = conversationKeys.putIfAbsent(conversationId, convKeys);
//                if (existing != null) { // Another thread added it first
//                    convKeys = existing;
//                }
//            }
//            // Add the key version to the inner map
//            convKeys.put(keyTimestamp, aesKey);
//            Log.d(TAG, "setConversationKey called by: " + caller + ". Key version " + keyTimestamp + " added/updated for conv ID: " + conversationId + ". Total versions for this conv: " + convKeys.size() + ". Total convs with keys: " + conversationKeys.size());
//        } else {
//            Log.w(TAG, "setConversationKey called by: " + caller + " with null/empty ID, invalid timestamp, or null key.");
//        }
//    }
//
//    /**
//     * Retrieves the latest decrypted conversation AES key from memory for a given conversation.
//     * Assumes the latest key has the highest timestamp.
//     *
//     * @param conversationId The ID of the conversation.
//     * @return The latest SecretKey if available in the cache, null otherwise.
//     */
//    @Nullable
//    public SecretKey getLatestConversationKey(@NonNull String conversationId) { // *** NEW METHOD: Get LATEST Key ***
//        String caller = getCallerClassName();
//        if (conversationId != null) {
//            ConcurrentMap<Long, SecretKey> convKeys = conversationKeys.get(conversationId);
//            if (convKeys != null && !convKeys.isEmpty()) {
//                try {
//                    // Find the largest timestamp key in the map
//                    long latestTimestamp = Collections.max(convKeys.keySet()); // Finds the max key (timestamp)
//                    SecretKey latestKey = convKeys.get(latestTimestamp);
//                    if (latestKey != null) {
//                        Log.d(TAG, "getLatestConversationKey called by: " + caller + ". Latest key found for ID: " + conversationId + " (timestamp: " + latestTimestamp + "). Total versions: " + convKeys.size());
//                        return latestKey;
//                    } else {
//                        // Should not happen if maxKey returned a key that exists, but defensive
//                        Log.w(TAG, "getLatestConversationKey called by: " + caller + ". Max timestamp found, but key is null for ID: " + conversationId + " (timestamp: " + latestTimestamp + "). Total versions: " + convKeys.size());
//                        return null; // Key found but was null (unexpected)
//                    }
//                } catch (java.util.NoSuchElementException e) {
//                    // This catch is necessary if convKeys.keySet() is empty for some reason,
//                    // although the check !convKeys.isEmpty() should prevent it.
//                    Log.e(TAG, "getLatestConversationKey called by: " + caller + ". No timestamps found in key set for conv ID: " + conversationId + ". Total versions: " + convKeys.size(), e);
//                    return null;
//                }
//            } else {
//                Log.d(TAG, "getLatestConversationKey called by: " + caller + ". No conversation keys found in cache for ID: " + conversationId);
//                return null; // No keys for this conversation
//            }
//        }
//        Log.w(TAG, "getLatestConversationKey called by: " + caller + " with null ID.");
//        return null;
//    }
//
//
//    /**
//     * Retrieves a specific decrypted conversation AES key by its timestamp.
//     *
//     * @param conversationId The ID of the conversation.
//     * @param keyTimestamp The timestamp identifying this key version.
//     * @return The SecretKey if available in the cache, null otherwise.
//     */
//    @Nullable
//    public SecretKey getConversationKeyByTimestamp(@NonNull String conversationId, long keyTimestamp) { // *** NEW METHOD: Get Specific Key ***
//        String caller = getCallerClassName();
//        if (conversationId != null && keyTimestamp > 0) {
//            ConcurrentMap<Long, SecretKey> convKeys = conversationKeys.get(conversationId);
//            if (convKeys != null) {
//                SecretKey key = convKeys.get(keyTimestamp);
//                if (key != null) {
//                    Log.d(TAG, "getConversationKeyByTimestamp called by: " + caller + ". Key found for ID: " + conversationId + " timestamp: " + keyTimestamp);
//                } else {
//                    Log.d(TAG, "getConversationKeyByTimestamp called by: " + caller + ". Key NOT found in cache for ID: " + conversationId + " timestamp: " + keyTimestamp);
//                }
//                return key;
//            }
//            Log.d(TAG, "getConversationKeyByTimestamp called by: " + caller + ". No keys stored for conv ID: " + conversationId);
//            return null; // No keys for this conversation
//        }
//        Log.w(TAG, "getConversationKeyByTimestamp called by: " + caller + " with null ID or invalid timestamp.");
//        return null;
//    }
//
//    /**
//     * Gets a map of all decrypted conversation AES keys for a specific conversation, ordered by timestamp.
//     *
//     * @param conversationId The ID of the conversation.
//     * @return A map of timestamp -> SecretKey, ordered by timestamp ascending. Returns empty map if none found.
//     */
//    @NonNull // Return empty map instead of null
//    public SortedMap<Long, SecretKey> getAllConversationKeysForConversation(@NonNull String conversationId) { // *** NEW METHOD: Get ALL Keys for a Conv ***
//        String caller = getCallerClassName();
//        SortedMap<Long, SecretKey> sortedKeys = new TreeMap<>(); // TreeMap keeps keys sorted by timestamp
//
//        if (conversationId != null) {
//            ConcurrentMap<Long, SecretKey> convKeys = conversationKeys.get(conversationId);
//            if (convKeys != null) {
//                // Copy to a TreeMap to return a sorted list
//                sortedKeys.putAll(convKeys); // Copy contents
//                Log.d(TAG, "getAllConversationKeysForConversation called by: " + caller + ". Found " + sortedKeys.size() + " versions for conv ID: " + conversationId);
//            } else {
//                Log.d(TAG, "getAllConversationKeysForConversation called by: " + caller + ". No keys stored for conv ID: " + conversationId);
//            }
//        } else {
//            Log.w(TAG, "getAllConversationKeysForConversation called by: " + caller + " with null ID.");
//        }
//        return sortedKeys; // Return the sorted map (copy)
//    }
//
//
//    /**
//     * Checks if *any* decrypted conversation key is available in memory for a given conversation.
//     */
//    public boolean hasConversationKey(@NonNull String conversationId) { // Keep this method, check if inner map exists and is not empty
//        String caller = getCallerClassName();
//        boolean hasAnyKey = conversationId != null && conversationKeys.containsKey(conversationId) && conversationKeys.get(conversationId) != null && !conversationKeys.get(conversationId).isEmpty();
//        // Log this check as it's called frequently
//        Log.d(TAG, "hasConversationKey called by: " + caller + " for ID: " + conversationId + ". Has any version: " + hasAnyKey);
//        return hasAnyKey;
//    }
//
//    /**
//     * Checks if a SPECIFIC decrypted conversation key version is available in memory.
//     * @param conversationId The ID of the conversation.
//     * @param keyTimestamp The timestamp of the key version.
//     * @return true if the specific key version is found and not null, false otherwise.
//     */
//    public boolean hasConversationKeyVersion(@NonNull String conversationId, long keyTimestamp) { // *** NEW METHOD: Check for Specific Version ***
//        String caller = getCallerClassName();
//        boolean hasSpecificKey = conversationId != null && keyTimestamp > 0 && conversationKeys.containsKey(conversationId) && conversationKeys.get(conversationId) != null && conversationKeys.get(conversationId).containsKey(keyTimestamp) && conversationKeys.get(conversationId).get(keyTimestamp) != null;
//        Log.d(TAG, "hasConversationKeyVersion called by: " + caller + " for ID: " + conversationId + " timestamp: " + keyTimestamp + ". Found: " + hasSpecificKey);
//        return hasSpecificKey;
//    }
//
//
//    /**
//     * Removes ALL conversation keys for a specific conversation from memory cache.
//     *
//     * @param conversationId The ID of the conversation.
//     */
//    public void removeConversationKey(@NonNull String conversationId) { // Keep this method, remove the inner map
//        String caller = getCallerClassName();
//        if (conversationId != null) {
//            if (conversationKeys.containsKey(conversationId)) {
//                ConcurrentMap<Long, SecretKey> removed = conversationKeys.remove(conversationId);
//                if (removed != null) {
//                    Log.d(TAG, "removeConversationKey called by: " + caller + ". ALL versions removed from cache for ID: " + conversationId + ". Count removed: " + removed.size());
//                } else {
//                    // Should not happen if containsKey was true, but defensive
//                    Log.w(TAG, "removeConversationKey called by: " + caller + ". Attempted to remove keys for ID: " + conversationId + " but inner map was null.");
//                }
//            } else {
//                Log.d(TAG, "removeConversationKey called by: " + caller + ". Attempted to remove non-existent conversation keys for ID: " + conversationId);
//            }
//        } else {
//            Log.w(TAG, "removeConversationKey called by: " + caller + " with null ID.");
//        }
//    }
//
//    /**
//     * Removes a specific conversation key version from memory cache.
//     * @param conversationId The ID of the conversation.
//     * @param keyTimestamp The timestamp of the key version to remove.
//     */
//    public void removeConversationKeyVersion(@NonNull String conversationId, long keyTimestamp) { // *** NEW METHOD: Remove Specific Version ***
//        String caller = getCallerClassName();
//        if (conversationId != null && keyTimestamp > 0) {
//            ConcurrentMap<Long, SecretKey> convKeys = conversationKeys.get(conversationId);
//            if (convKeys != null) {
//                SecretKey removed = convKeys.remove(keyTimestamp);
//                if (removed != null) {
//                    Log.d(TAG, "removeConversationKeyVersion called by: " + caller + ". Key version " + keyTimestamp + " removed from cache for conv ID: " + conversationId);
//                    // If the inner map becomes empty, remove the conversation ID from the outer map to keep it clean
//                    if (convKeys.isEmpty()) {
//                        conversationKeys.remove(conversationId);
//                        Log.d(TAG, "removeConversationKeyVersion called by: " + caller + ". Inner map for conv ID: " + conversationId + " is now empty, removed from outer map.");
//                    }
//                } else {
//                    Log.d(TAG, "removeConversationKeyVersion called by: " + caller + ". Attempted to remove non-existent key version " + keyTimestamp + " for conv ID: " + conversationId);
//                }
//            } else {
//                Log.d(TAG, "removeConversationKeyVersion called by: " + caller + ". No keys stored for conv ID: " + conversationId);
//            }
//        } else {
//            Log.w(TAG, "removeConversationKeyVersion called by: " + caller + " with null ID or invalid timestamp.");
//        }
//    }
//
//
//    /**
//     * Returns a map containing all conversation IDs and their stored key versions.
//     * Added for debugging purposes.
//     * Returns a copy.
//     */
//    @NonNull // Return empty map instead of null
//    public Map<String, Map<Long, SecretKey>> getAllConversationKeys() { // Keep/Modify this method signature and logic
//        String caller = getCallerClassName();
//        Log.d(TAG, "getAllConversationKeys called by: " + caller + ". Returning map with " + conversationKeys.size() + " conversations. Total versions: " + getTotalCachedConversationKeyVersions()); // Use helper for count
//        // Return a deep copy to prevent external modification
//        Map<String, Map<Long, SecretKey>> copy = new HashMap<>();
//        for (Map.Entry<String, ConcurrentMap<Long, SecretKey>> entry : conversationKeys.entrySet()) {
//            copy.put(entry.getKey(), new TreeMap<>(entry.getValue())); // Copy inner map to TreeMap for sorting by timestamp
//        }
//        return copy;
//    }
//
//    /**
//     * Helper method to get the total count of all cached conversation key versions across all conversations.
//     * @return The total number of individual key versions stored in the cache.
//     */
//    public int getTotalCachedConversationKeyVersions() { // *** NEW HELPER METHOD ***
//        int count = 0;
//        for (ConcurrentMap<Long, SecretKey> convKeys : conversationKeys.values()) {
//            if (convKeys != null) {
//                count += convKeys.size();
//            }
//        }
//        return count;
//    }
//
//    // --- End Methods for Conversation Keys ---
//
//    // You might add methods later for group chat keys if applicable.
//    // Group chat keys would also be SecretKeys stored in the conversationKeys map, keyed by groupId.
//}




// --- YourKeyManager.java (Modified) ---


public class YourKeyManager {

    private static final String TAG = "YourKeyManager";
    private static volatile YourKeyManager instance;

    private String currentUserId;
    private PublicKey userPublicKey;
    private PrivateKey userPrivateKey;

    // *** MODIFIED MAP TYPE: Map Conversation ID directly to SecretKey ***
    private final Map<String, SecretKey> conversationKeys; // Use ConcurrentHashMap for thread safety

    // Private constructor
    private YourKeyManager() {
        // Initialize the single-level map
        conversationKeys = new ConcurrentHashMap<>(); // Use ConcurrentHashMap
        Log.d(TAG, "YourKeyManager instance created with single-key support per conv."); // Updated log
    }

    // Get the singleton instance (Thread-safe)
    public static YourKeyManager getInstance() {
        // ... (Keep thread-safe singleton logic) ...
        String caller = getCallerClassName();
        if (instance == null) {
            synchronized (YourKeyManager.class) {
                if (instance == null) {
                    instance = new YourKeyManager();
                    Log.d(TAG, "getInstance called by: " + caller + ". New instance CREATED.");
                } else {
                    Log.d(TAG, "getInstance called by: " + caller + ". Existing instance returned (inside sync).");
                }
            }
        } else {
            Log.d(TAG, "getInstance called by: " + caller + ". Existing instance returned (outside sync).");
        }
        return instance;
    }

    private static String getCallerClassName() { /* ... (Keep helper method) ... */
        try {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            if (elements.length > 3) {
                return elements[3].getClassName() + "." + elements[3].getMethodName();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting caller class name", e);
        }
        return "UnknownCaller";
    }


    /**
     * Sets the RSA keys for the current user session after successful decryption.
     * Call this ONLY after RSA keys are successfully decrypted during login/unlock.
     * Clears user's RSA keys and *all* previously cached conversation keys.
     * Conversation keys should be loaded from Room *after* calling this method.
     */
    public synchronized void setKeys(String userId, PublicKey publicKey, PrivateKey privateKey) {
        String caller = getCallerClassName();
        Log.d(TAG, "setKeys called by: " + caller + " for user: " + userId + ", Private Key available: " + (privateKey != null) + ". Clearing ALL existing cached conversation keys (" + conversationKeys.size() + ")."); // Updated log for single key count

        this.currentUserId = userId;
        this.userPublicKey = publicKey;
        this.userPrivateKey = privateKey; // Decrypted private key
        this.conversationKeys.clear(); // *** CLEAR ALL cached conversation keys (clears the map) ***

        Log.d(TAG, "RSA keys set for user: " + userId + ". All cached conversation keys cleared.");
    }

    /**
     * Sets keys when private key decryption is NOT possible (e.g., user skipped setup, cancelled passphrase).
     * Only the public key will be stored. The private key remains null.
     * Clears user's RSA keys and *all* previously cached conversation keys.
     */
    public synchronized void setPublicOnly(String userId, PublicKey publicKey) {
        String caller = getCallerClassName();
        Log.d(TAG, "setPublicOnly called by: " + caller + " for user: " + userId + ". Private Key will be NOT available. Clearing ALL existing cached conversation keys (" + conversationKeys.size() + ")."); // Updated log

        this.currentUserId = userId;
        this.userPublicKey = publicKey;
        this.userPrivateKey = null; // Private key is NOT available
        this.conversationKeys.clear(); // *** CLEAR ALL cached conversation keys ***

        Log.d(TAG, "Public key set for user: " + userId + ". All cached conversation keys cleared.");
    }

    /**
     * Clears all keys and cached data, typically when the user logs out or their account is deleted.
     * Clears RSA keys and all cached conversation keys.
     */
    public synchronized void clearKeys() {
        String callerInfo = getCallerClassName(); // Use the helper method

        Log.d(TAG, "clearKeys called by: " + callerInfo + ". Clearing ALL keys. Current state: Private Available=" + (userPrivateKey != null) + ", Total Cached Conv Keys=" + conversationKeys.size()); // Updated log

        this.currentUserId = null;
        this.userPublicKey = null;
        this.userPrivateKey = null;
        this.conversationKeys.clear(); // *** CLEAR ALL cached conversation keys ***

        Log.d(TAG, "ALL keys cleared from KeyManager.");
    }

    // --- Getters ---
    public String getCurrentUserId() { return currentUserId; }
    public PublicKey getUserPublicKey() { return userPublicKey; }
    public PrivateKey getUserPrivateKey() { return userPrivateKey; }

    /**
     * Checks if the user's decrypted private key is available.
     */
    public boolean isPrivateKeyAvailable() {
        String caller = getCallerClassName();
        Log.d(TAG, "isPrivateKeyAvailable called by: " + caller + ". Result: " + (userPrivateKey != null));
        return userPrivateKey != null;
    }

    // --- Methods for managing CONVERSATION keys (UPDATED) ---

    /**
     * Stores the decrypted conversation AES key in memory for a specific conversation.
     * Overwrites any existing key for this conversation ID.
     *
     * @param conversationId The ID of the conversation.
     * @param aesKey The decrypted SecretKey for this conversation.
     */
    public void setConversationKey(@NonNull String conversationId, @NonNull SecretKey aesKey) { // *** MODIFIED: Removed keyTimestamp ***
        String caller = getCallerClassName();
        if (conversationId != null && aesKey != null) {
            conversationKeys.put(conversationId, aesKey); // Store key directly
            Log.d(TAG, "setConversationKey called by: " + caller + ". Key stored for conv ID: " + conversationId + ". Total cached conv keys: " + conversationKeys.size()); // Updated log
        } else {
            Log.w(TAG, "setConversationKey called by: " + caller + " with null/empty ID or null key.");
        }
    }

    /**
     * Retrieves the decrypted conversation AES key from memory for a given conversation.
     *
     * @param conversationId The ID of the conversation.
     * @return The SecretKey if available in the cache, null otherwise.
     */
    @Nullable // Keep annotation
    public SecretKey getConversationKey(@NonNull String conversationId) { // *** MODIFIED/RENAMED: getConversationKey ***
        String caller = getCallerClassName();
        if (conversationId != null) {
            SecretKey key = conversationKeys.get(conversationId);
            if (key != null) {
                Log.d(TAG, "getConversationKey called by: " + caller + ". Key found for conv ID: " + conversationId);
            } else {
                Log.d(TAG, "getConversationKey called by: " + caller + ". Key NOT found in cache for conv ID: " + conversationId);
            }
            return key;
        }
        Log.w(TAG, "getConversationKey called by: " + caller + " with null ID.");
        return null;
    }

    // REMOVED: getLatestConversationKey (replaced by getConversationKey)
    // REMOVED: getConversationKeyByTimestamp
    // REMOVED: getAllConversationKeysForConversation

    /**
     * Checks if a decrypted conversation key is available in memory for a given conversation.
     *
     * @param conversationId The ID of the conversation.
     * @return true if the key is found and not null, false otherwise.
     */
    public boolean hasConversationKey(@NonNull String conversationId) { // Keep this method
        String caller = getCallerClassName();
        boolean hasKey = conversationId != null && conversationKeys.containsKey(conversationId) && conversationKeys.get(conversationId) != null;
        // Log this check as it's called frequently
        Log.d(TAG, "hasConversationKey called by: " + caller + " for ID: " + conversationId + ". Found: " + hasKey);
        return hasKey;
    }

    // REMOVED: hasConversationKeyVersion

    /**
     * Removes the conversation key for a specific conversation from memory cache.
     *
     * @param conversationId The ID of the conversation.
     */
    public void removeConversationKey(@NonNull String conversationId) { // Keep this method
        String caller = getCallerClassName();
        if (conversationId != null) {
            SecretKey removed = conversationKeys.remove(conversationId); // Remove key directly
            if (removed != null) {
                Log.d(TAG, "removeConversationKey called by: " + caller + ". Key removed from cache for ID: " + conversationId);
            } else {
                Log.d(TAG, "removeConversationKey called by: " + caller + ". Attempted to remove non-existent key for ID: " + conversationId);
            }
        } else {
            Log.w(TAG, "removeConversationKey called by: " + caller + " with null ID.");
        }
    }

    // REMOVED: removeConversationKeyVersion

    /**
     * Returns a map containing all conversation IDs and their stored single key.
     * Added for debugging purposes. Returns a copy.
     */
    @NonNull // Return empty map instead of null
    public Map<String, SecretKey> getAllConversationKeys() { // *** MODIFIED RETURN TYPE ***
        String caller = getCallerClassName();
        Log.d(TAG, "getAllConversationKeys called by: " + caller + ". Returning map with " + conversationKeys.size() + " conversations."); // Updated log
        // Return a copy to prevent external modification
        return new HashMap<>(conversationKeys); // Return a copy of the map
    }

    public Map<String, SecretKey> getConversationKeys() {
        return conversationKeys;
    }

    // REMOVED: getTotalCachedConversationKeyVersions

    // --- End Methods for Conversation Keys ---

}