package com.sana.circleup.encryptionfiles; // MAKE SURE THIS PACKAGE NAME IS CORRECT

import android.text.TextUtils;

import java.security.InvalidAlgorithmParameterException;
// ... other imports ...

// ... other imports ...
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets; // Needed for String to byte[] conversion




import android.util.Log; // Use Android Log

public class CryptoUtils {

    private static final String TAG = "CryptoUtils";
    private static final String RSA_ALGORITHM = "RSA";
    private static final int RSA_KEY_SIZE = 2048;
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final int PBKDF2_ITERATIONS = 10000;
    private static final int AES_KEY_LENGTH_BITS = 256;
    public static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // Standard GCM tag length (bits)
    private static final int GCM_IV_LENGTH = 12; // Standard GCM IV length (bytes)

    // --- Key Pair Generation --- (Keep as is)
    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        Log.d(TAG, "Generating RSA Key Pair...");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(RSA_KEY_SIZE, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        Log.d(TAG, "RSA Key Pair generated.");
        return keyPair;
    }

    // --- Key Derivation from Passphrase --- (Keep as is)
    public static byte[] generateSalt() {
        Log.d(TAG, "Generating salt...");
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        Log.d(TAG, "Salt generated.");
        return salt;
    }

    public static SecretKey deriveKeyFromPassphrase(String passphrase, byte[] salt, int iterations)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        Log.d(TAG, "Deriving key from passphrase...");
        PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, iterations, AES_KEY_LENGTH_BITS);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        SecretKey key = skf.generateSecret(spec);
        SecretKeySpec derivedKey = new SecretKeySpec(key.getEncoded(), AES_ALGORITHM);
        Log.d(TAG, "Key derived from passphrase.");
        return derivedKey;
    }

    // --- Private Key Encryption/Decryption --- (Keep as is, works with byte[])
    public static byte[] encryptPrivateKey(PrivateKey privateKey, SecretKey encryptionKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Log.d(TAG, "Encrypting private key...");
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, ivSpec);

        byte[] privateKeyEncoded = privateKey.getEncoded();
        byte[] ciphertext = cipher.doFinal(privateKeyEncoded);

        byte[] encryptedDataWithIV = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encryptedDataWithIV, 0, iv.length);
        System.arraycopy(ciphertext, 0, encryptedDataWithIV, iv.length, ciphertext.length);
        Log.d(TAG, "Private key encrypted.");
        return encryptedDataWithIV;
    }

    public static PrivateKey decryptPrivateKey(byte[] encryptedPrivateKeyWithIV, SecretKey encryptionKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
            InvalidKeySpecException {
        Log.d(TAG, "Decrypting private key...");
        if (encryptedPrivateKeyWithIV == null || encryptedPrivateKeyWithIV.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Encrypted data is too short for decryption.");
        }

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedPrivateKeyWithIV, 0, iv, 0, GCM_IV_LENGTH);

        byte[] ciphertext = new byte[encryptedPrivateKeyWithIV.length - GCM_IV_LENGTH];
        if (ciphertext.length < GCM_TAG_LENGTH / 8) { // Ensure enough space for the GCM tag
            throw new IllegalArgumentException("Encrypted data is too short to contain ciphertext and tag.");
        }
        System.arraycopy(encryptedPrivateKeyWithIV, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);


        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, ivSpec);

        byte[] privateKeyEncoded = cipher.doFinal(ciphertext);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyEncoded);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        Log.d(TAG, "Private key decrypted.");
        return privateKey;
    }

    // --- Utility methods to convert Keys to/from byte arrays --- (Keep as is, returns raw encoded bytes)
    public static byte[] publicKeyToBytes(PublicKey publicKey) {
        if (publicKey == null) return null;
        return publicKey.getEncoded(); // X.509 format
    }

    public static PublicKey bytesToPublicKey(byte[] publicKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (publicKeyBytes == null) throw new IllegalArgumentException("Public key bytes are null");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    public static byte[] privateKeyToBytes(PrivateKey privateKey) {
        if (privateKey == null) return null;
        return privateKey.getEncoded(); // PKCS#8 format
    }

    public static PrivateKey bytesToPrivateKey(byte[] privateKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (privateKeyBytes == null) throw new IllegalArgumentException("Private key bytes are null");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }


    // --- Standardized Base64 Encoding/Decoding for ALL Crypto Bytes ---

    /**
     * Encodes a byte array (representing any crypto data: keys, encrypted messages, salt)
     * into a Base64 String. Uses java.util.Base64 with no line breaks.
     *
     * @param data The byte array to encode.
     * @return The Base64 encoded String, or null if input is null.
     */
    public static String bytesToBase64(byte[] data) {
        if (data == null) return null;
        // Log.d(TAG, "Encoding bytes to Base64...");
        return java.util.Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decodes a Base64 String (representing any crypto data) back into a byte array.
     * Uses java.util.Base64.
     *
     * @param base64String The Base64 encoded String.
     * @return The decoded byte array, or null if input is null.
     * @throws IllegalArgumentException If the string is not valid Base64.
     */
    public static byte[] base64ToBytes(String base64String) throws IllegalArgumentException {
        if (base64String == null) return null;
        // Log.d(TAG, "Decoding Base64 to bytes...");
        return java.util.Base64.getDecoder().decode(base64String);
    }

    // REMOVE THESE METHODS: bytesToMessageBase64, messageBase64ToBytes

    // --- RSA Encryption/Decryption (for exchanging AES keys) --- (Keep as is, works with byte[])
    public static byte[] encryptWithRSA(byte[] dataToEncrypt, PublicKey recipientPublicKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        Log.d(TAG, "Encrypting with RSA...");
        if (dataToEncrypt == null || recipientPublicKey == null) {
            throw new IllegalArgumentException("Data to encrypt or public key is null.");
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey);
        byte[] encryptedData = cipher.doFinal(dataToEncrypt);
        Log.d(TAG, "RSA encryption complete.");
        return encryptedData;
    }

    public static byte[] decryptWithRSA(byte[] encryptedData, PrivateKey userPrivateKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        Log.d(TAG, "Decrypting with RSA...");
        if (encryptedData == null || userPrivateKey == null) {
            throw new IllegalArgumentException("Data to decrypt or private key is null.");
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, userPrivateKey);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        Log.d(TAG, "RSA decryption complete.");
        return decryptedData;
    }

    // --- AES Encryption/Decryption (for messages) ---

    // (Keep as is, generates SecretKey)
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        Log.d(TAG, "Generating AES Key...");
        javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_LENGTH_BITS);
        SecretKey aesKey = keyGen.generateKey();
        Log.d(TAG, "AES Key generated.");
        return aesKey;
    }


    /**
     * Encrypts a message (String) using a conversation's AES key and AES/GCM.
     * Converts the String message to bytes using UTF-8.
     * Returns the raw byte array containing the IV followed by the ciphertext.
     *
     * @param message The message content as a String.
     * @param conversationAESKey The symmetric SecretKey for the conversation.
     * @return A byte array containing the IV followed by the ciphertext.
     * @throws NoSuchAlgorithmException ...
     * @throws NoSuchPaddingException ...
     * @throws InvalidKeyException ...
     * @throws IllegalBlockSizeException ...
     * @throws BadPaddingException ...
     * @throws InvalidAlgorithmParameterException ...
     */
    public static byte[] encryptMessageWithAES(String message, SecretKey conversationAESKey) // <-- Returns byte[]
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Log.d(TAG, "Encrypting message with AES...");
        if (message == null) message = ""; // Handle null message input defensively
        if (conversationAESKey == null) throw new InvalidKeyException("Conversation AES key is null for encryption.");

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv); // Generate a random IV for EACH message

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, conversationAESKey, ivSpec);

        byte[] ciphertext = cipher.doFinal(messageBytes);

        byte[] encryptedDataWithIV = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encryptedDataWithIV, 0, iv.length);
        System.arraycopy(ciphertext, 0, encryptedDataWithIV, iv.length, ciphertext.length);
        Log.d(TAG, "Message encrypted with AES. Result byte length: " + encryptedDataWithIV.length);
        return encryptedDataWithIV; // <-- Return the raw bytes
    }

    /**
     * Decrypts an encrypted message byte array (IV followed by ciphertext) using a conversation's AES key and AES/GCM.
     * Converts the resulting bytes back to a String using UTF-8.
     *
     * @param encryptedMessageWithIV A byte array containing the message's IV followed by ciphertext.
     * @param conversationAESKey     The symmetric SecretKey for the conversation.
     * @return The decrypted message as a String.
     * @throws NoSuchAlgorithmException ...
     * @throws NoSuchPaddingException ...
     * @throws InvalidKeyException ...
     * @throws IllegalBlockSizeException ...
     * @throws BadPaddingException ...
     * @throws InvalidAlgorithmParameterException ...
     * @throws IllegalArgumentException If the input byte array is null, too short, or invalid.
     */
    public static String decryptMessageWithAES(byte[] encryptedMessageWithIV, SecretKey conversationAESKey) // <-- Takes byte[]
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
            IllegalArgumentException { // Added IllegalArgumentException for short input check
        Log.d(TAG, "Decrypting message with AES...");
        if (encryptedMessageWithIV == null || encryptedMessageWithIV.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Encrypted data is null or too short to contain IV.");
        }
        if (conversationAESKey == null) throw new InvalidKeyException("Conversation AES key is null for decryption.");

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedMessageWithIV, 0, iv, 0, GCM_IV_LENGTH);

        byte[] ciphertext = new byte[encryptedMessageWithIV.length - GCM_IV_LENGTH];
        if (ciphertext.length < GCM_TAG_LENGTH / 8) { // Ensure enough space for the GCM tag
            throw new IllegalArgumentException("Encrypted data is too short to contain ciphertext and tag.");
        }
        System.arraycopy(encryptedMessageWithIV, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);


        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, conversationAESKey, ivSpec);

        byte[] decryptedMessageBytes = cipher.doFinal(ciphertext);

        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        // Log.d(TAG, "Message decrypted with AES. Result string length: " + decryptedMessage.length()); // Avoid logging decrypted content
        return decryptedMessage;
    }


    // --- Utility method to convert SecretKey to/from byte arrays --- (Keep as is)
    public static byte[] secretKeyToBytes(SecretKey key) {
        if (key == null) return null;
        return key.getEncoded();
    }

    public static SecretKey bytesToSecretKey(byte[] keyBytes) {
        if (keyBytes == null) return null;
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }

    // --- Utility method to convert String messages to/from byte arrays --- (Keep as is)
    public static byte[] stringToBytes(String message) {
        if (message == null) return null;
        return message.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToString(byte[] bytes) {
        if (bytes == null) return null;
        return new String(bytes, StandardCharsets.UTF_8);
    }




// --- Utility methods for Recovery Code (Simple Hex String example for demo) ---
// NOTE: For a real-world app, highly recommend using a secure mnemonic phrase library (BIP39).
// This Hex conversion is a simplified example to illustrate the concept of key-to-string conversion.

    /**
     * Converts PrivateKey bytes (PKCS#8 encoded) to a Hex String recovery code.
     * This Hex string represents the raw bytes of the decrypted private key.
     * SECURITY: The security of the recovery process relies entirely on the user
     * securely storing this Hex String.
     *
     * @param privateKey The Decrypted PrivateKey object.
     * @return A Hex String representation of the private key bytes, or null if input is null.
     */
    public static String privateKeyToRecoveryCode(PrivateKey privateKey) {
        if (privateKey == null) return null;
        Log.d(TAG, "Converting PrivateKey to Hex Recovery Code...");
        // Get the raw encoded bytes of the private key (PKCS#8 format is common for PrivateKey.getEncoded())
        byte[] privateKeyBytes = privateKey.getEncoded();
        if (privateKeyBytes == null) {
            Log.e(TAG, "PrivateKey.getEncoded() returned null.");
            return null;
        }

        // Convert bytes to Hex String
        StringBuilder hexString = new StringBuilder(2 * privateKeyBytes.length);
        for (byte b : privateKeyBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        Log.d(TAG, "Conversion to Hex Recovery Code complete. Length: " + hexString.length());
        return hexString.toString();
    }













    /**
     * Converts a Hex String recovery code back to a PrivateKey object.
     * This assumes the Hex string represents PKCS#8 encoded private key bytes
     * and the RSA algorithm is available.
     *
     * @param recoveryCode The Hex String recovery code obtained from privateKeyToRecoveryCode.
     * @return The reconstructed PrivateKey object.
     * @throws IllegalArgumentException If the recovery code is null, empty, or invalid hex format.
     * @throws NoSuchAlgorithmException If the RSA algorithm is not available.
     * @throws InvalidKeySpecException If the private key bytes are not in a valid PKCS#8 format.
     */
    public static PrivateKey recoveryCodeToPrivateKey(String recoveryCode)
            throws IllegalArgumentException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (TextUtils.isEmpty(recoveryCode)) {
            throw new IllegalArgumentException("Recovery code is null or empty");
        }
        if (recoveryCode.length() % 2 != 0) {
            throw new IllegalArgumentException("Recovery code hex string has odd length");
        }
        Log.d(TAG, "Converting Hex Recovery Code to PrivateKey...");

        // Convert Hex String back to bytes
        int len = recoveryCode.length();
        byte[] privateKeyBytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(recoveryCode.charAt(i), 16);
            int low = Character.digit(recoveryCode.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Recovery code contains non-hex characters");
            }
            privateKeyBytes[i / 2] = (byte) ((high << 4) + low);
        }
        Log.d(TAG, "Hex Recovery Code converted to bytes. Length: " + privateKeyBytes.length);


        // Convert bytes back to PrivateKey (Assuming PKCS#8 format)
        // Use the same RSA_ALGORITHM constant you already have defined
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        Log.d(TAG, "Conversion to PrivateKey from Recovery Code complete.");
        return privateKey;
    }




}