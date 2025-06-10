package com.sana.circleup.encryptionfiles;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
// import java.util.Base64; // Remove this import
import android.util.Base64; // Ensure this import is present



import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class to securely store and retrieve the symmetric key derived
 * from the user's passphrase using AndroidX EncryptedSharedPreferences.
 * This key is used to decrypt the user's encrypted private key stored in Firebase.
 */
/*
public class SecureKeyStorageUtil {

    private static final String TAG = "SecureKeyStorageUtil";
    private static final String PREF_FILE_NAME = "secure_keys_prefs";
    private static final String SYMMETRIC_KEY_ALIAS = "passphrase_derived_symmetric_key"; // Key for the derived symmetric key



    // For EncryptedFile
    // File name will include the user ID to make it user-specific
    private static final String ENCRYPTED_KEYS_FILE_BASE_NAME = "user_rsa_keys";
    private static final String FILE_SEPARATOR = "_"; // Separator for file name parts

    // Structure of the file: [length of encryptedPrivateKeyWithIV bytes] + [encryptedPrivateKeyWithIV bytes] + [publicKeyBytes]
    private static final int LENGTH_BYTES = 4; // To store the length of the first byte array


    private static SharedPreferences secureSharedPreferences;
    // Master key is managed internally by EncryptedSharedPreferences via MasterKeys.

    private static String masterKeyAlias; // Master key alias for both EncryptedSharedPreferences and EncryptedFile

    // Synchronized method to initialize EncryptedSharedPreferences
    private static synchronized void initialize(Context context) {
        if (secureSharedPreferences == null) {
            Log.d(TAG, "Initializing EncryptedSharedPreferences...");
            try {
                // 1. Create or retrieve the Master Key
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                Log.d(TAG, "Master key alias obtained: " + masterKeyAlias);

                // 2. Initialize EncryptedSharedPreferences
                secureSharedPreferences = EncryptedSharedPreferences.create(
                        PREF_FILE_NAME,
                        masterKeyAlias,
                        context.getApplicationContext(), // Use application context to avoid leaks
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
                Log.d(TAG, "EncryptedSharedPreferences initialized successfully.");

            } catch (GeneralSecurityException e) {
                Log.e(TAG, "GeneralSecurityException during EncryptedSharedPreferences initialization", e);
                secureSharedPreferences = null; // Ensure it's null on failure
            } catch (IOException e) {
                Log.e(TAG, "IOException during EncryptedSharedPreferences initialization", e);
                secureSharedPreferences = null; // Ensure it's null on failure
            } catch (Exception e) { // Catch any other unexpected exceptions
                Log.e(TAG, "Unexpected exception during EncryptedSharedPreferences initialization", e);
                secureSharedPreferences = null; // Ensure it's null on failure
            }
            // Note: If initialization fails, secureSharedPreferences remains null, and save/load methods will handle it.
        } else {
            Log.d(TAG, "EncryptedSharedPreferences already initialized.");
        }
    }

    */
/**
     * Saves the derived symmetric key securely.
     * Call this after successfully decrypting the private key using the passphrase.
     *
     * @param context The application context.
     * @param symmetricKey The SecretKey derived from the user's passphrase.
     * @return true if saved successfully, false otherwise.
     *//*

    public static boolean saveSymmetricKey(Context context, SecretKey symmetricKey) {
        initialize(context); // Ensure initialization
        if (secureSharedPreferences == null) {
            Log.e(TAG, "SecureSharedPreferences is not initialized. Cannot save key.");
            return false; // Initialization failed
        }
        if (symmetricKey == null) {
            Log.e(TAG, "Attempted to save a null symmetric key.");
            return false;
        }


        try {
            Log.d(TAG, "Preparing to save symmetric key.");
            // Convert SecretKey to byte array and encode it as Base64 for storage
            byte[] keyBytes = symmetricKey.getEncoded();
            String keyBase64 = CryptoUtils.bytesToBase64(keyBytes); // Use your existing Base64 utility

            SharedPreferences.Editor editor = secureSharedPreferences.edit();
            editor.putString(SYMMETRIC_KEY_ALIAS, keyBase64);
            Log.d(TAG, "Putting key string into editor. Committing...");
            boolean success = editor.commit(); // Use commit() for synchronous save, apply() for async

            if (success) {
                Log.d(TAG, "Symmetric key saved successfully.");
            } else {
                Log.e(TAG, "Failed to commit symmetric key save.");
            }
            return success;

        } catch (Exception e) { // Catch any exceptions during encoding or putString/commit
            Log.e(TAG, "Error saving symmetric key", e);
            return false;
        }
    }

    */
/**
     * Loads the securely stored symmetric key.
     * Call this on app start if the user is authenticated but keys are not in memory.
     *
     * @param context The application context.
     * @return The loaded SecretKey, or null if not found or loading fails.
     *//*

    public static SecretKey loadSymmetricKey(Context context) {
        initialize(context); // Ensure initialization
        if (secureSharedPreferences == null) {
            Log.e(TAG, "SecureSharedPreferences is not initialized. Cannot load key.");
            return null; // Initialization failed
        }

        try {
            Log.d(TAG, "Attempting to load symmetric key from secure storage.");
            String keyBase64 = secureSharedPreferences.getString(SYMMETRIC_KEY_ALIAS, null);

            if (keyBase64 != null) {
                Log.d(TAG, "Symmetric key Base64 string found.");
                // Decode Base64 string back to byte array
                byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64); // Use your existing Base64 utility

                // Convert byte array back to SecretKey object
                SecretKey symmetricKey = new SecretKeySpec(keyBytes, CryptoUtils.AES_ALGORITHM); // Assuming AES_ALGORITHM is public in CryptoUtils

                Log.d(TAG, "Symmetric key loaded and converted to SecretKey successfully.");
                return symmetricKey;

            } else {
                Log.d(TAG, "No symmetric key Base64 string found in secure storage.");
                return null; // Key not found
            }

        } catch (IllegalArgumentException e) {
            // This likely means the Base64 string in storage is corrupt
            Log.e(TAG, "Error decoding Base64 key from secure storage (corrupt data?). Clearing stored key.", e);
            // Clear potentially corrupt data so it doesn't cause issues on next load attempt
            clearStoredKey(context); // <<< KEEP clearing here IF decoding fails
            return null;
        } catch (Exception e) { // Catch any exceptions during getString or key spec creation
            Log.e(TAG, "Error loading symmetric key from secure storage", e);
            // DO NOT clear the stored key for generic load errors.
            // It might be a transient issue or a problem with the secure storage system itself.
            // clearStoredKey(context); // <<< REMOVE clearing here >>>
            return null;
        }
    }

    */
/**
     * Clears the securely stored symmetric key.
     * Call this when the user logs out or their account is deleted.
     *
     * @param context The application context.
     *//*

    public static void clearStoredKey(Context context) {
        initialize(context); // Ensure initialization
        if (secureSharedPreferences == null) {
            Log.e(TAG, "SecureSharedPreferences is not initialized. Cannot clear key.");
            return; // Initialization failed
        }
        try {
            Log.d(TAG, "Clearing symmetric key from secure storage.");
            SharedPreferences.Editor editor = secureSharedPreferences.edit();
            editor.remove(SYMMETRIC_KEY_ALIAS);
            editor.apply(); // Use apply() for async clear
            Log.d(TAG, "Symmetric key cleared from secure storage.");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing symmetric key", e);
        }
    }

    // Ensure CryptoUtils.AES_ALGORITHM is public static final
}*/




 // Use your app's package name

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;

import androidx.security.crypto.EncryptedFile; // Import for EncryptedFile
import androidx.security.crypto.MasterKeys;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets; // Use StandardCharsets
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;



/**
 * Utility class to securely store and retrieve keys using AndroidX Security.
 * Stores the derived symmetric key in EncryptedSharedPreferences.
 * Stores the encrypted private key and public key bytes in EncryptedFile.
 */













//
//public class SecureKeyStorageUtil {
//
//    private static final String TAG = "SecureKeyStorageUtil";
//
//    // For EncryptedSharedPreferences
//    private static final String PREF_FILE_NAME = "circleup_secure_keys_prefs"; // More specific name
//    private static final String SYMMETRIC_KEY_PREF_KEY = "passphrase_derived_symmetric_key"; // Key for the derived symmetric key in prefs
//
//    // For EncryptedFile
//    // File name will include the user ID to make it user-specific
//    private static final String ENCRYPTED_KEYS_FILE_BASE_NAME = "user_rsa_keys";
//    private static final String FILE_EXTENSION = ".enc";
//    private static final String FILE_SEPARATOR = "_"; // Separator for file name parts
//
//    // Structure of the file: [length of encryptedPrivateKeyWithIV bytes] + [encryptedPrivateKeyWithIV bytes] + [publicKeyBytes]
//    private static final int LENGTH_BYTES = 4; // To store the length of the first byte array
//
//
//    private static SharedPreferences secureSharedPreferences;
//    private static String masterKeyAlias; // Master key alias for both EncryptedSharedPreferences and EncryptedFile
//
//    // Flag to indicate if initialization was successful
//    private static volatile boolean isInitialized = false;
//    private static final Object lock = new Object(); // Lock for synchronization
//
//
//    // Synchronized method to initialize AndroidX Security components
//    private static boolean initialize(Context context) {
//        // Double-check inside synchronized block
//        synchronized (lock) {
//            if (!isInitialized) {
//                Log.d(TAG, "Initializing AndroidX Security components...");
//                try {
//                    // 1. Create or retrieve the Master Key (used by both SharedPreferences and File)
//                    // This key is stored securely in the Android KeyStore.
//                    // Use context.getApplicationContext() to avoid Activity context leaks.
//                    masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
//                    Log.d(TAG, "Master key alias obtained: " + masterKeyAlias);
//
//                    // 2. Initialize EncryptedSharedPreferences
//                    secureSharedPreferences = EncryptedSharedPreferences.create(
//                            PREF_FILE_NAME,
//                            masterKeyAlias,
//                            context.getApplicationContext(), // Use application context
//                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//                    );
//                    Log.d(TAG, "EncryptedSharedPreferences initialized successfully.");
//
//                    isInitialized = true; // Mark as initialized
//                    return true; // Initialization successful
//
//                } catch (GeneralSecurityException e) {
//                    Log.e(TAG, "GeneralSecurityException during AndroidX Security initialization", e);
//                    masterKeyAlias = null;
//                    secureSharedPreferences = null;
//                    isInitialized = false;
//                    return false; // Initialization failed
//                } catch (IOException e) {
//                    Log.e(TAG, "IOException during AndroidX Security initialization", e);
//                    masterKeyAlias = null;
//                    secureSharedPreferences = null;
//                    isInitialized = false;
//                    return false; // Initialization failed
//                } catch (Exception e) { // Catch any other unexpected exceptions
//                    Log.e(TAG, "Unexpected exception during AndroidX Security initialization", e);
//                    masterKeyAlias = null;
//                    secureSharedPreferences = null;
//                    isInitialized = false;
//                    return false; // Initialization failed
//                }
//            } else {
//                Log.d(TAG, "AndroidX Security components already initialized.");
//                return true; // Already initialized
//            }
//        }
//    }
//
//
//    // --- Symmetric Key Storage (in EncryptedSharedPreferences) ---
//
//    /**
//     * Saves the derived symmetric key securely to EncryptedSharedPreferences.
//     * Call this after successfully decrypting the private key using the passphrase.
//     * The symmetric key is stored as a Base64 string.
//     *
//     * @param context The application context.
//     * @param symmetricKey The SecretKey derived from the user's passphrase.
//     * @return true if saved successfully, false otherwise.
//     */
//    public static boolean saveSymmetricKey(Context context, SecretKey symmetricKey) {
//        if (!initialize(context) || secureSharedPreferences == null) {
//            Log.e(TAG, "Secure storage initialization failed. Cannot save symmetric key.");
//            return false;
//        }
//        if (symmetricKey == null) {
//            Log.w(TAG, "Attempted to save a null symmetric key.");
//            return false;
//        }
//
//        try {
//            Log.d(TAG, "Preparing to save symmetric key.");
//            byte[] keyBytes = symmetricKey.getEncoded(); // Get raw key bytes
//            // Use java.util.Base64 for encoding/decoding cryptographic keys
//            String keyBase64 = Base64.encodeToString(keyBytes, Base64.DEFAULT);
//
//            SharedPreferences.Editor editor = secureSharedPreferences.edit();
//            editor.putString(SYMMETRIC_KEY_PREF_KEY, keyBase64);
//            // Use commit() for synchronous saving in critical flows like login completion
//            boolean success = editor.commit();
//
//            if (success) {
//                Log.d(TAG, "Symmetric key saved successfully to EncryptedSharedPreferences.");
//            } else {
//                // This can happen if commit fails, e.g., storage full
//                Log.e(TAG, "Failed to commit symmetric key save.");
//            }
//            return success;
//
//        } catch (Exception e) { // Catch any exceptions during encoding or saving
//            Log.e(TAG, "Error saving symmetric key to EncryptedSharedPreferences", e);
//            return false;
//        }
//    }
//
//    /**
//     * Loads the securely stored symmetric key from EncryptedSharedPreferences.
//     *
//     * @param context The application context.
//     * @return The loaded SecretKey, or null if not found or loading fails.
//     */
//    @Nullable
//    public static SecretKey loadSymmetricKey(Context context) {
//        if (!initialize(context) || secureSharedPreferences == null) {
//            Log.e(TAG, "Secure storage initialization failed. Cannot load symmetric key.");
//            return null;
//        }
//
//        try {
//            Log.d(TAG, "Attempting to load symmetric key from EncryptedSharedPreferences.");
//            // Use getString with a default value of null
//            String keyBase64 = secureSharedPreferences.getString(SYMMETRIC_KEY_PREF_KEY, null);
//
//            if (keyBase64 != null) {
//                Log.d(TAG, "Symmetric key Base64 string found.");
//                // Use java.util.Base64 for decoding
//                byte[] keyBytes = Base64.decode(keyBase64, Base64.DEFAULT);
//
//
//                // Assuming the key is AES and its length implies the correct algorithm name
//                // CryptoUtils.AES_ALGORITHM should be "AES"
//                SecretKey symmetricKey = new SecretKeySpec(keyBytes, CryptoUtils.AES_ALGORITHM);
//
//                Log.d(TAG, "Symmetric key loaded and converted to SecretKey successfully from EncryptedSharedPreferences.");
//                return symmetricKey;
//
//            } else {
//                Log.d(TAG, "No symmetric key Base64 string found in EncryptedSharedPreferences.");
//                return null; // Key not found
//            }
//
//        } catch (IllegalArgumentException e) {
//            // This happens if the Base64 string is invalid
//            Log.e(TAG, "Error decoding Base64 key from EncryptedSharedPreferences (corrupt data?). Clearing stored key.", e);
//            // Clear potentially corrupt data so it doesn't cause issues on next load attempt
//            clearSymmetricKey(context); // Clear *only* the symmetric key from prefs
//            return null;
//        } catch (Exception e) { // Catch any exceptions during getString or key spec creation
//            Log.e(TAG, "Error loading symmetric key from EncryptedSharedPreferences", e);
//            return null;
//        }
//    }
//
//    /**
//     * Clears the securely stored symmetric key from EncryptedSharedPreferences.
//     * Call this when the user logs out or their account is deleted.
//     *
//     * @param context The application context.
//     */
//    public static void clearSymmetricKey(Context context) {
//        if (!initialize(context) || secureSharedPreferences == null) {
//            Log.e(TAG, "Secure storage initialization failed. Cannot clear symmetric key.");
//            return;
//        }
//        try {
//            Log.d(TAG, "Clearing symmetric key from EncryptedSharedPreferences.");
//            SharedPreferences.Editor editor = secureSharedPreferences.edit();
//            editor.remove(SYMMETRIC_KEY_PREF_KEY);
//            editor.apply(); // Use apply() for async clear
//            Log.d(TAG, "Symmetric key cleared from EncryptedSharedPreferences.");
//        } catch (Exception e) {
//            Log.e(TAG, "Error clearing symmetric key from EncryptedSharedPreferences", e);
//        }
//    }
//
//
//    // --- RSA Key Pair Storage (in EncryptedFile) ---
//
//    // Helper to get the user-specific file name for EncryptedFile
//    private static String getEncryptedKeysFileName(String userId) {
//        if (TextUtils.isEmpty(userId)) {
//            Log.e(TAG, "Cannot get file name, userId is empty.");
//            return null;
//        }
//        // Example: user_rsa_keys_USERID.enc
//        // Sanitize userId to be safe for filenames if necessary (though UIDs are usually safe)
//        return ENCRYPTED_KEYS_FILE_BASE_NAME + FILE_SEPARATOR + userId + FILE_EXTENSION;
//    }
//
//    /**
//     * Saves the encrypted private key bytes and public key bytes to a secure file.
//     * Call this after successfully decrypting the private key using the passphrase.
//     * Data is saved in the format: [length of encryptedPrivateKeyWithIV] + [encryptedPrivateKeyWithIV] + [publicKeyBytes].
//     *
//     * @param context The application context.
//     * @param userId The ID of the current user.
//     * @param encryptedPrivateKeyWithIV The encrypted private key bytes (including IV).
//     * @param publicKeyBytes The public key bytes.
//     * @return true if saved successfully, false otherwise.
//     */
//    public static boolean saveEncryptedKeyPair(Context context, String userId, byte[] encryptedPrivateKeyWithIV, byte[] publicKeyBytes) {
//        if (!initialize(context) || masterKeyAlias == null) {
//            Log.e(TAG, "Secure storage initialization failed. Cannot save encrypted key pair.");
//            return false;
//        }
//        if (TextUtils.isEmpty(userId) || encryptedPrivateKeyWithIV == null || publicKeyBytes == null) {
//            Log.w(TAG, "Attempted to save null or empty encrypted key pair data.");
//            return false;
//        }
//
//        String fileName = getEncryptedKeysFileName(userId);
//        if (fileName == null) return false;
//
//        // Use context.getFilesDir() for internal storage private to the app
//        File file = new File(context.getFilesDir(), fileName);
//        Log.d(TAG, "Attempting to save encrypted key pair to file: " + file.getAbsolutePath());
//
//        try {
//            // Combine data into a single byte array: [length of encryptedPrivateKeyWithIV] + [encryptedPrivateKeyWithIV] + [publicKeyBytes]
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//            // Write the length of the private key part first (as 4 bytes, big-endian)
//            byte[] privateKeyLengthBytes = new byte[] {
//                    (byte) (encryptedPrivateKeyWithIV.length >> 24),
//                    (byte) (encryptedPrivateKeyWithIV.length >> 16),
//                    (byte) (encryptedPrivateKeyWithIV.length >> 8),
//                    (byte) encryptedPrivateKeyWithIV.length
//            };
//            outputStream.write(privateKeyLengthBytes);
//
//            // Write the encrypted private key bytes
//            outputStream.write(encryptedPrivateKeyWithIV);
//
//            // Write the public key bytes
//            outputStream.write(publicKeyBytes);
//
//            byte[] dataToWrite = outputStream.toByteArray();
//            Log.d(TAG, "Combined key data bytes length: " + dataToWrite.length);
//
//
//            // Create an EncryptedFile instance
//            EncryptedFile encryptedFile = new EncryptedFile.Builder(
//                    file,
//                    context.getApplicationContext(), // Use application context
//                    masterKeyAlias,
//                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB // Use GCM for authenticated encryption
//            ).build();
//
//            // Write data to the encrypted file
//            try (OutputStream fos = encryptedFile.openFileOutput()) {
//                fos.write(dataToWrite);
//                fos.flush(); // Ensure data is written
//            } // try-with-resources closes the stream automatically
//
//            Log.d(TAG, "Encrypted key pair saved successfully to EncryptedFile.");
//            return true;
//
//        } catch (IOException e) {
//            Log.e(TAG, "IOException saving encrypted key pair file", e);
//            return false;
//        } catch (GeneralSecurityException e) {
//            Log.e(TAG, "GeneralSecurityException saving encrypted key pair file", e);
//            return false;
//        } catch (Exception e) {
//            Log.e(TAG, "Unexpected exception saving encrypted key pair file", e);
//            return false;
//        }
//    }
//
//    /**
//     * Loads the encrypted private key bytes and public key bytes from a secure file.
//     * Reads data from the file format: [length of encryptedPrivateKeyWithIV] + [encryptedPrivateKeyWithIV] + [publicKeyBytes].
//     *
//     * @param context The application context.
//     * @param userId The ID of the current user.
//     * @return A byte array containing [encryptedPrivateKeyWithIV, publicKeyBytes], or null if loading fails.
//     */
//    @Nullable
//    public static byte[][] loadEncryptedKeyPair(Context context, String userId) {
//        if (!initialize(context) || masterKeyAlias == null) {
//            Log.e(TAG, "Secure storage initialization failed. Cannot load encrypted key pair.");
//            return null;
//        }
//        if (TextUtils.isEmpty(userId)) {
//            Log.w(TAG, "Cannot load key pair, userId is empty.");
//            return null;
//        }
//
//        String fileName = getEncryptedKeysFileName(userId);
//        if (fileName == null) return null;
//
//        // Use context.getFilesDir() for internal storage private to the app
//        File file = new File(context.getFilesDir(), fileName);
//        if (!file.exists()) {
//            Log.d(TAG, "Encrypted key pair file not found for user: " + userId + " at " + file.getAbsolutePath());
//            return null; // File doesn't exist, which is expected on first run or after clearing
//        }
//        Log.d(TAG, "Attempting to load encrypted key pair from file: " + file.getAbsolutePath());
//
//
//        try {
//            // Create an EncryptedFile instance
//            EncryptedFile encryptedFile = new EncryptedFile.Builder(
//                    file,
//                    context.getApplicationContext(), // Use application context
//                    masterKeyAlias,
//                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
//            ).build();
//
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            try (InputStream fis = encryptedFile.openFileInput()) {
//                byte[] buffer = new byte[1024];
//                int read;
//                // Read the entire file content into a byte array
//                while ((read = fis.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, read);
//                }
//            } // try-with-resources closes the stream automatically
//
//            byte[] fileData = outputStream.toByteArray();
//            Log.d(TAG, "Read " + fileData.length + " bytes from encrypted file.");
//
//            if (fileData.length < LENGTH_BYTES) {
//                Log.e(TAG, "File data is too short to contain key lengths.");
//                clearEncryptedKeyPair(context, userId); // Clear corrupt file
//                return null;
//            }
//
//            // Read the length of the encryptedPrivateKeyWithIV bytes (4 bytes, big-endian)
//            int privateKeyLength = (fileData[0] & 0xFF) << 24 |
//                    (fileData[1] & 0xFF) << 16 |
//                    (fileData[2] & 0xFF) << 8 |
//                    (fileData[3] & 0xFF);
//
//            if (privateKeyLength <= 0 || fileData.length < LENGTH_BYTES + privateKeyLength) {
//                Log.e(TAG, "Invalid private key length (" + privateKeyLength + ") or file data too short (" + fileData.length + " bytes).");
//                clearEncryptedKeyPair(context, userId); // Clear corrupt file
//                return null;
//            }
//
//            // Extract encryptedPrivateKeyWithIV bytes
//            byte[] encryptedPrivateKeyWithIV = Arrays.copyOfRange(fileData, LENGTH_BYTES, LENGTH_BYTES + privateKeyLength);
//
//            // Extract publicKeyBytes (the rest of the data)
//            byte[] publicKeyBytes = Arrays.copyOfRange(fileData, LENGTH_BYTES + privateKeyLength, fileData.length);
//
//            // Basic sanity check on public key length (RSA public keys have a predictable structure/min length)
//            // This is not a guarantee, but helps catch obvious corruption
//            if (publicKeyBytes.length < 100) { // Typical min length for RSA public key bytes
//                Log.e(TAG, "Public key data seems too short (" + publicKeyBytes.length + " bytes). Likely corrupt.");
//                clearEncryptedKeyPair(context, userId); // Clear corrupt file
//                return null;
//            }
//
//
//            Log.d(TAG, "Encrypted key pair loaded successfully from EncryptedFile. Private key bytes: " + encryptedPrivateKeyWithIV.length + ", Public key bytes: " + publicKeyBytes.length);
//
//            // Return as a 2D array: [encryptedPrivateKeyWithIV, publicKeyBytes]
//            return new byte[][] {encryptedPrivateKeyWithIV, publicKeyBytes};
//
//        } catch (IOException e) {
//            // This can happen if the file exists but there's an issue reading it (permissions, disk error)
//            Log.e(TAG, "IOException loading encrypted key pair file", e);
//            // Decide if you want to clear the file on IOException. Often indicates a temporary problem.
//            // clearEncryptedKeyPair(context, userId); // Optional clear
//            return null;
//        } catch (GeneralSecurityException e) {
//            // This likely means the master key is gone/corrupt, or the file data is corrupt/tampered.
//            // The EncryptedFile integrity check (GCM) failed.
//            Log.e(TAG, "GeneralSecurityException loading encrypted key pair file (Master Key issue or corrupt data?)", e);
//            // Clear the file if decryption/integrity check failed
//            clearEncryptedKeyPair(context, userId);
//            return null;
//        } catch (Exception e) {
//            Log.e(TAG, "Unexpected exception loading encrypted key pair file", e);
//            // Consider clearing the file on unexpected errors too
//            clearEncryptedKeyPair(context, userId);
//            return null;
//        }
//    }
//
//    /**
//     * Clears the securely stored encrypted key pair file for a specific user.
//     * Call this when the user logs out or their account is deleted.
//     *
//     * @param context The application context.
//     * @param userId The ID of the current user.
//     */
//    public static void clearEncryptedKeyPair(Context context, String userId) {
//        if (TextUtils.isEmpty(userId)) {
//            Log.w(TAG, "Cannot clear key pair file, userId is empty.");
//            return;
//        }
//        String fileName = getEncryptedKeysFileName(userId);
//        if (fileName == null) {
//            Log.w(TAG, "Cannot get file name for clearing key pair, userId might be invalid.");
//            return;
//        }
//
//        File file = new File(context.getFilesDir(), fileName);
//        if (file.exists()) {
//            Log.d(TAG, "Attempting to clear encrypted key pair file: " + file.getAbsolutePath());
//            boolean deleted = file.delete();
//            if (deleted) {
//                Log.d(TAG, "Encrypted key pair file cleared successfully.");
//            } else {
//                Log.e(TAG, "Failed to clear encrypted key pair file: " + file.getAbsolutePath());
//            }
//        } else {
//            Log.d(TAG, "Encrypted key pair file not found for clearing: " + file.getAbsolutePath());
//        }
//    }
//
//    /**
//     * Clears ALL securely stored keys (symmetric key from prefs and RSA key pair file) for a specific user.
//     * Call this when the user logs out or their account is deleted.
//     *
//     * @param context The application context.
//     * @param userId The ID of the current user.
//     */
//    public static void clearAllSecureKeys(Context context, String userId) {
//        Log.d(TAG, "Clearing all secure keys for user: " + userId);
//        clearSymmetricKey(context); // Clear symmetric key from prefs
//        clearEncryptedKeyPair(context, userId); // Clear RSA key pair file
//        Log.d(TAG, "All secure keys cleared.");
//    }
//
//    // Ensure CryptoUtils.AES_ALGORITHM is public static final
//    // Ensure CryptoUtils has bytesToPublicKey and decryptPrivateKey methods
//}
//





public class SecureKeyStorageUtil {

    private static final String TAG = "SecureKeyStorageUtil";

    // For EncryptedSharedPreferences
    private static final String PREF_FILE_NAME = "circleup_secure_keys_prefs";
    private static final String SYMMETRIC_KEY_PREF_KEY = "passphrase_derived_symmetric_key";

    // For EncryptedFile
    private static final String ENCRYPTED_KEYS_FILE_BASE_NAME = "user_rsa_keys";
    private static final String FILE_EXTENSION = ".enc";
    private static final String FILE_SEPARATOR = "_";

    // Structure of the file: [length of encryptedPrivateKeyWithIV bytes] + [encryptedPrivateKeyWithIV bytes] + [publicKeyBytes]
    private static final int LENGTH_BYTES = 4;


    private static SharedPreferences secureSharedPreferences;
    private static String masterKeyAlias;

    private static volatile boolean isInitialized = false;
    private static final Object lock = new Object();


    // Synchronized method to initialize AndroidX Security components
    private static boolean initialize(Context context) {
        synchronized (lock) {
            if (!isInitialized) {
                Log.d(TAG, "Initializing AndroidX Security components...");
                try {
                    masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                    Log.d(TAG, "Master key alias obtained: " + masterKeyAlias);

                    secureSharedPreferences = EncryptedSharedPreferences.create(
                            PREF_FILE_NAME,
                            masterKeyAlias,
                            context.getApplicationContext(),
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                    Log.d(TAG, "EncryptedSharedPreferences initialized successfully.");

                    isInitialized = true;
                    return true;

                } catch (GeneralSecurityException e) {
                    Log.e(TAG, "GeneralSecurityException during AndroidX Security initialization", e);
                    masterKeyAlias = null;
                    secureSharedPreferences = null;
                    isInitialized = false;
                    return false;
                } catch (IOException e) {
                    Log.e(TAG, "IOException during AndroidX Security initialization", e);
                    masterKeyAlias = null;
                    secureSharedPreferences = null;
                    isInitialized = false;
                    return false;
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected exception during AndroidX Security initialization", e);
                    masterKeyAlias = null;
                    secureSharedPreferences = null;
                    isInitialized = false;
                    return false;
                }
            } else {
                Log.d(TAG, "AndroidX Security components already initialized.");
                return true;
            }
        }
    }


    // --- Symmetric Key Storage (in EncryptedSharedPreferences) ---

    /**
     * Saves the derived symmetric key securely to EncryptedSharedPreferences.
     * Call this after successfully decrypting the private key using the passphrase.
     * The symmetric key is stored as a Base64 string using CryptoUtils' method.
     *
     * @param context The application context.
     * @param symmetricKey The SecretKey derived from the user's passphrase.
     * @return true if saved successfully, false otherwise.
     */
    public static boolean saveSymmetricKey(Context context, SecretKey symmetricKey) {
        if (!initialize(context) || secureSharedPreferences == null) {
            Log.e(TAG, "Secure storage initialization failed. Cannot save symmetric key.");
            return false;
        }
        if (symmetricKey == null) {
            Log.w(TAG, "Attempted to save a null symmetric key.");
            return false;
        }

        try {
            Log.d(TAG, "Preparing to save symmetric key.");
            byte[] keyBytes = symmetricKey.getEncoded(); // Get raw key bytes
            // Use CryptoUtils method for KEY encoding (now standardized to java.util.Base64)
            // MODIFIED: Use CryptoUtils.bytesToBase64
            String keyBase64 = CryptoUtils.bytesToBase64(keyBytes); // <-- Change here

            if (TextUtils.isEmpty(keyBase64)) { // Basic validation
                Log.e(TAG, "Failed to Base64 encode symmetric key bytes.");
                return false;
            }


            SharedPreferences.Editor editor = secureSharedPreferences.edit();
            editor.putString(SYMMETRIC_KEY_PREF_KEY, keyBase64);
            // Use commit() for synchronous saving in critical flows like login completion
            boolean success = editor.commit();

            if (success) {
                Log.d(TAG, "Symmetric key saved successfully to EncryptedSharedPreferences.");
            } else {
                // This can happen if commit fails, e.g., storage full
                Log.e(TAG, "Failed to commit symmetric key save.");
            }
            return success;

        } catch (Exception e) { // Catch any exceptions during encoding or saving
            Log.e(TAG, "Error saving symmetric key to EncryptedSharedPreferences", e);
            return false;
        }
    }

    /**
     * Loads the securely stored symmetric key from EncryptedSharedPreferences.
     *
     * @param context The application context.
     * @return The loaded SecretKey, or null if not found or loading fails.
     */
    @Nullable
    public static SecretKey loadSymmetricKey(Context context) {
        if (!initialize(context) || secureSharedPreferences == null) {
            Log.e(TAG, "Secure storage initialization failed. Cannot load symmetric key.");
            return null;
        }

        try {
            Log.d(TAG, "Attempting to load symmetric key from EncryptedSharedPreferences.");
            // Use getString with a default value of null
            String keyBase64 = secureSharedPreferences.getString(SYMMETRIC_KEY_PREF_KEY, null);

            if (keyBase64 != null) {
                Log.d(TAG, "Symmetric key Base64 string found.");
                // Use CryptoUtils method for KEY decoding (now standardized to java.util.Base64)
                // MODIFIED: Use CryptoUtils.base64ToBytes
                byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64); // <-- Change here

                if (keyBytes == null || keyBytes.length == 0) { // Defensive check
                    Log.e(TAG, "Decoded symmetric key bytes are null or empty!");
                    // Treat as corrupt data, clear it
                    clearSymmetricKey(context);
                    return null;
                }


                // Assuming the key is AES and its length implies the correct algorithm name
                // CryptoUtils.AES_ALGORITHM should be "AES"
                SecretKey symmetricKey = new SecretKeySpec(keyBytes, CryptoUtils.AES_ALGORITHM);

                Log.d(TAG, "Symmetric key loaded and converted to SecretKey successfully from EncryptedSharedPreferences.");
                return symmetricKey;

            } else {
                Log.d(TAG, "No symmetric key Base64 string found in EncryptedSharedPreferences.");
                return null; // Key not found
            }

        } catch (IllegalArgumentException e) {
            // This happens if the Base64 string is invalid
            Log.e(TAG, "Error decoding Base64 key from EncryptedSharedPreferences (corrupt data?). Clearing stored key.", e);
            // Clear potentially corrupt data so it doesn't cause issues on next load attempt
            clearSymmetricKey(context); // Clear *only* the symmetric key from prefs
            return null;
        } catch (Exception e) { // Catch any exceptions during getString or key spec creation
            Log.e(TAG, "Error loading symmetric key from EncryptedSharedPreferences", e);
            return null;
        }
    }

    /**
     * Clears the securely stored symmetric key from EncryptedSharedPreferences.
     * Call this when the user logs out or their account is deleted.
     *
     * @param context The application context.
     */
    public static void clearSymmetricKey(Context context) {
        if (!initialize(context) || secureSharedPreferences == null) {
            Log.e(TAG, "Secure storage initialization failed. Cannot clear symmetric key.");
            return;
        }
        try {
            Log.d(TAG, "Clearing symmetric key from EncryptedSharedPreferences.");
            SharedPreferences.Editor editor = secureSharedPreferences.edit();
            editor.remove(SYMMETRIC_KEY_PREF_KEY);
            editor.apply(); // Use apply() for async clear
            Log.d(TAG, "Symmetric key cleared from EncryptedSharedPreferences.");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing symmetric key from EncryptedSharedPreferences", e);
        }
    }


    // --- RSA Key Pair Storage (in EncryptedFile) ---
    // These methods save/load raw byte arrays of the encrypted private key and public key.
    // The conversion to/from PublicKey/PrivateKey objects is done *after* loading/before saving
    // in methods like Login.decryptPrivateKeyAsync or Login.fetchUserDataAndHandleKeys.
    // Those methods should use CryptoUtils.bytesToPublicKey/privateKey/SecretKey for object conversion
    // and CryptoUtils.bytesToBase64/base64ToBytes for Firebase Base64 encoding/decoding.
    // No changes needed in saveEncryptedKeyPair or loadEncryptedKeyPair themselves, as they handle the file format (length + bytes + bytes).

    // Helper to get the user-specific file name for EncryptedFile
    private static String getEncryptedKeysFileName(String userId) {
        if (TextUtils.isEmpty(userId)) {
            Log.e(TAG, "Cannot get file name, userId is empty.");
            return null;
        }
        // Example: user_rsa_keys_USERID.enc
        // Sanitize userId to be safe for filenames if necessary (though UIDs are usually safe)
        return ENCRYPTED_KEYS_FILE_BASE_NAME + FILE_SEPARATOR + userId + FILE_EXTENSION;
    }

    /**
     * Saves the encrypted private key bytes and public key bytes to a secure file.
     * Call this after successfully decrypting the private key using the passphrase.
     * Data is saved in the format: [length of encryptedPrivateKeyWithIV] + [encryptedPrivateKeyWithIV] + [publicKeyBytes].
     *
     * @param context The application context.
     * @param userId The ID of the current user.
     * @param encryptedPrivateKeyWithIV The encrypted private key bytes (including IV).
     * @param publicKeyBytes The public key bytes.
     * @return true if saved successfully, false otherwise.
     */
    public static boolean saveEncryptedKeyPair(Context context, String userId, byte[] encryptedPrivateKeyWithIV, byte[] publicKeyBytes) {
        if (!initialize(context) || masterKeyAlias == null) {
            Log.e(TAG, "Secure storage initialization failed. Cannot save encrypted key pair.");
            return false;
        }
        if (TextUtils.isEmpty(userId) || encryptedPrivateKeyWithIV == null || publicKeyBytes == null) {
            Log.w(TAG, "Attempted to save null or empty encrypted key pair data.");
            return false;
        }

        String fileName = getEncryptedKeysFileName(userId);
        if (fileName == null) return false;

        // Use context.getFilesDir() for internal storage private to the app
        File file = new File(context.getFilesDir(), fileName);
        Log.d(TAG, "Attempting to save encrypted key pair to file: " + file.getAbsolutePath());

        try {
            // Combine data into a single byte array: [length of encryptedPrivateKeyWithIV] + [encryptedPrivateKeyWithIV] + [publicKeyBytes]
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Write the length of the private key part first (as 4 bytes, big-endian)
            byte[] privateKeyLengthBytes = new byte[] {
                    (byte) (encryptedPrivateKeyWithIV.length >> 24),
                    (byte) (encryptedPrivateKeyWithIV.length >> 16),
                    (byte) (encryptedPrivateKeyWithIV.length >> 8),
                    (byte) encryptedPrivateKeyWithIV.length
            };
            outputStream.write(privateKeyLengthBytes);

            // Write the encrypted private key bytes
            outputStream.write(encryptedPrivateKeyWithIV);

            // Write the public key bytes
            outputStream.write(publicKeyBytes);

            byte[] dataToWrite = outputStream.toByteArray();
            Log.d(TAG, "Combined key data bytes length: " + dataToWrite.length);


            // Create an EncryptedFile instance
            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    file,
                    context.getApplicationContext(), // Use application context
                    masterKeyAlias,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB // Use GCM for authenticated encryption
            ).build();

            // Write data to the encrypted file
            try (OutputStream fos = encryptedFile.openFileOutput()) {
                fos.write(dataToWrite);
                fos.flush(); // Ensure data is written
            } // try-with-resources closes the stream automatically

            Log.d(TAG, "Encrypted key pair saved successfully to EncryptedFile.");
            return true;

        } catch (IOException e) {
            Log.e(TAG, "IOException saving encrypted key pair file", e);
            return false;
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "GeneralSecurityException saving encrypted key pair file", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected exception saving encrypted key pair file", e);
            return false;
        }
    }

    /**
     * Loads the encrypted private key bytes and public key bytes from a secure file.
     * Reads data from the file format: [length of encryptedPrivateKeyWithIV] + [encryptedPrivateKeyWithIV] + [publicKeyBytes].
     *
     * @param context The application context.
     * @param userId The ID of the current user.
     * @return A byte array containing [encryptedPrivateKeyWithIV, publicKeyBytes], or null if loading fails.
     */
    @Nullable
    public static byte[][] loadEncryptedKeyPair(Context context, String userId) {
        if (!initialize(context) || masterKeyAlias == null) {
            Log.e(TAG, "Secure storage initialization failed. Cannot load encrypted key pair.");
            return null;
        }
        if (TextUtils.isEmpty(userId)) {
            Log.w(TAG, "Cannot load key pair, userId is empty.");
            return null;
        }

        String fileName = getEncryptedKeysFileName(userId);
        if (fileName == null) return null;

        // Use context.getFilesDir() for internal storage private to the app
        File file = new File(context.getFilesDir(), fileName);
        if (!file.exists()) {
            Log.d(TAG, "Encrypted key pair file not found for user: " + userId + " at " + file.getAbsolutePath());
            return null; // File doesn't exist, which is expected on first run or after clearing
        }
        Log.d(TAG, "Attempting to load encrypted key pair from file: " + file.getAbsolutePath());


        try {
            // Create an EncryptedFile instance
            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    file,
                    context.getApplicationContext(), // Use application context
                    masterKeyAlias,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (InputStream fis = encryptedFile.openFileInput()) {
                byte[] buffer = new byte[1024];
                int read;
                // Read the entire file content into a byte array
                while ((read = fis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            } // try-with-resources closes the stream automatically

            byte[] fileData = outputStream.toByteArray();
            Log.d(TAG, "Read " + fileData.length + " bytes from encrypted file.");

            if (fileData.length < LENGTH_BYTES) {
                Log.e(TAG, "File data is too short to contain key lengths.");
                clearEncryptedKeyPair(context, userId); // Clear corrupt file
                return null;
            }

            // Read the length of the encryptedPrivateKeyWithIV bytes (4 bytes, big-endian)
            int privateKeyLength = (fileData[0] & 0xFF) << 24 |
                    (fileData[1] & 0xFF) << 16 |
                    (fileData[2] & 0xFF) << 8 |
                    (fileData[3] & 0xFF);

            if (privateKeyLength < 0 || fileData.length < LENGTH_BYTES + privateKeyLength) { // Added < 0 check for safety
                Log.e(TAG, "Invalid private key length (" + privateKeyLength + ") or file data too short (" + fileData.length + " bytes).");
                clearEncryptedKeyPair(context, userId); // Clear corrupt file
                return null;
            }

            // Extract encryptedPrivateKeyWithIV bytes
            byte[] encryptedPrivateKeyWithIV = Arrays.copyOfRange(fileData, LENGTH_BYTES, LENGTH_BYTES + privateKeyLength);

            // Extract publicKeyBytes (the rest of the data)
            byte[] publicKeyBytes = Arrays.copyOfRange(fileData, LENGTH_BYTES + privateKeyLength, fileData.length);

            // Basic sanity check on public key length
            if (publicKeyBytes.length <= 0) { // Changed check to <= 0 as 100 might be too arbitrary
                Log.e(TAG, "Public key data seems null or empty (" + publicKeyBytes.length + " bytes). Likely corrupt.");
                clearEncryptedKeyPair(context, userId); // Clear corrupt file
                return null;
            }


            Log.d(TAG, "Encrypted key pair loaded successfully from EncryptedFile. Private key bytes: " + encryptedPrivateKeyWithIV.length + ", Public key bytes: " + publicKeyBytes.length);

            // Return as a 2D array: [encryptedPrivateKeyWithIV, publicKeyBytes]
            return new byte[][] {encryptedPrivateKeyWithIV, publicKeyBytes};

        } catch (IOException e) {
            Log.e(TAG, "IOException loading encrypted key pair file", e);
            return null;
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "GeneralSecurityException loading encrypted key pair file (Master Key issue or corrupt data?)", e);
            clearEncryptedKeyPair(context, userId);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected exception loading encrypted key pair file", e);
            clearEncryptedKeyPair(context, userId);
            return null;
        }
    }

    /**
     * Clears the securely stored encrypted key pair file for a specific user.
     * Call this when the user logs out or their account is deleted.
     *
     * @param context The application context.
     * @param userId The ID of the current user.
     */
    public static void clearEncryptedKeyPair(Context context, String userId) {
        if (TextUtils.isEmpty(userId)) {
            Log.w(TAG, "Cannot clear key pair file, userId is empty.");
            return;
        }
        String fileName = getEncryptedKeysFileName(userId);
        if (fileName == null) {
            Log.w(TAG, "Cannot get file name for clearing key pair, userId might be invalid.");
            return;
        }

        File file = new File(context.getFilesDir(), fileName);
        if (file.exists()) {
            Log.d(TAG, "Attempting to clear encrypted key pair file: " + file.getAbsolutePath());
            boolean deleted = file.delete();
            if (deleted) {
                Log.d(TAG, "Encrypted key pair file cleared successfully.");
            } else {
                Log.e(TAG, "Failed to clear encrypted key pair file: " + file.getAbsolutePath());
            }
        } else {
            Log.d(TAG, "Encrypted key pair file not found for clearing: " + file.getAbsolutePath());
        }
    }

    /**
     * Clears ALL securely stored keys (symmetric key from prefs and RSA key pair file) for a specific user.
     * Call this when the user logs out or their account is deleted.
     *
     * @param context The application context.
     * @param userId The ID of the current user.
     */
    public static void clearAllSecureKeys(Context context, String userId) {
        Log.d(TAG, "Clearing all secure keys for user: " + userId);
        clearSymmetricKey(context); // Clear symmetric key from prefs
        clearEncryptedKeyPair(context, userId); // Clear RSA key pair file
        Log.d(TAG, "All secure keys cleared.");
    }

    // Ensure CryptoUtils.AES_ALGORITHM is public static final (It is)
    // Ensure CryptoUtils has bytesToPublicKey and decryptPrivateKey methods (They do)
}