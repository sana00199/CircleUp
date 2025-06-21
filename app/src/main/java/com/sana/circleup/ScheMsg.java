package com.sana.circleup; // Replace with your actual package name

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//
//public class ScheMsg {
//    private String msgFirebaseId;
//    private String senderId;
//    private List<String> receiverIds;
//    private List<String> receiverNames;
//    private Map<String, String> receiverIdToNameMap;
//    private String content; // Stores TEXT or IMAGE BASE64
//    private String messageType; // "text" or "image"
//    private long scheduledTimeMillis;
//    private String scheduledTimeFormatted;
//    private String status;
//    private String receiverNamesStr;
//
//    // Removed: private String message; // This field is redundant with 'content' for this feature
//
//
//    // --- Constructors ---
//    public ScheMsg() {} // Required for Firebase
//
//    public ScheMsg(String senderId, List<String> receiverIds, List<String> receiverNames,
//                   String content, String messageType,
//                   long scheduledTimeMillis, String scheduledTimeFormatted) {
//        this.senderId = senderId;
//        this.receiverIds = (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>();
//        this.receiverNames = (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>();
//        this.content = content;
//        this.messageType = (messageType != null && !messageType.isEmpty()) ? messageType : "text";
//        this.scheduledTimeMillis = scheduledTimeMillis;
//        this.scheduledTimeFormatted = scheduledTimeFormatted;
//        this.status = "pending";
//
//        this.receiverIdToNameMap = new HashMap<>();
//        if(receiverIds != null && receiverNames != null && receiverIds.size() == receiverNames.size()){
//            for(int i=0; i<receiverIds.size(); i++){
//                if (receiverIds.get(i) != null && receiverNames.get(i) != null) { // Defensive check
//                    this.receiverIdToNameMap.put(receiverIds.get(i), receiverNames.get(i));
//                }
//            }
//        } else {
//            Log.w("ScheMsg", "Receiver IDs (" + (receiverIds != null ? receiverIds.size() : "null") + ") and Names (" + (receiverNames != null ? receiverNames.size() : "null") + ") lists size mismatch or null during constructor. Map may be incomplete or empty.");
//        }
//
//        this.receiverNamesStr = TextUtils.join(", ", this.receiverNames);
//    }
//
//    // --- Getters and Setters (Ensure they match the fields above) ---
//    public String getMsgFirebaseId() { return msgFirebaseId; }
//    public void setMsgFirebaseId(String msgFirebaseId) { this.msgFirebaseId = msgFirebaseId; }
//
//    public String getSenderId() { return senderId; }
//    public void setSenderId(String senderId) { this.senderId = senderId; }
//
//    public List<String> getReceiverIds() { return (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>(); }
//    public void setReceiverIds(List<String> receiverIds) { this.receiverIds = (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>(); }
//
//    public List<String> getReceiverNames() { return (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>(); }
//    public void setReceiverNames(List<String> receiverNames) {
//        this.receiverNames = (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>();
//        this.receiverNamesStr = TextUtils.join(", ", this.receiverNames); // Update names string
//    }
//
//    public Map<String, String> getReceiverIdToNameMap() { return (receiverIdToNameMap != null) ? new HashMap<>(receiverIdToNameMap) : new HashMap<>(); }
//    public void setReceiverIdToNameMap(Map<String, String> receiverIdToNameMap) { this.receiverIdToNameMap = (receiverIdToNameMap != null) ? new HashMap<>(receiverIdToNameMap) : new HashMap<>(); }
//
//    public String getContent() { return content; }
//    public void setContent(String content) { this.content = content; }
//
//    public String getMessageType() { return messageType; }
//    public void setMessageType(String messageType) { this.messageType = (messageType != null && !messageType.isEmpty()) ? messageType : "text"; }
//
//    public long getScheduledTimeMillis() { return scheduledTimeMillis; }
//    public void setScheduledTimeMillis(long scheduledTimeMillis) { this.scheduledTimeMillis = scheduledTimeMillis; }
//
//    public String getScheduledTimeFormatted() { return scheduledTimeFormatted; }
//    public void setScheduledTimeFormatted(String scheduledTimeFormatted) { this.scheduledTimeFormatted = scheduledTimeFormatted; }
//
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//
//    public String getReceiverNamesStr() { return receiverNamesStr; }
//    public void setReceiverNamesStr(String receiverNamesStr) { this.receiverNamesStr = receiverNamesStr; }
//
//
//    // --- Helper Getters for Adapter Display ---
//    public String getReceiverInfoForDisplay() {
//        return "To: " + getReceiverNamesStr();
//    }
//
//    // Use this to determine what to show in the list item
//    public String getContentPreviewForDisplay() {
//        if ("text".equals(messageType)) {
//            // For text, show the actual content (or a truncated version)
//            return !TextUtils.isEmpty(content) ? content : "[Empty Text]";
//        } else if ("image".equals(messageType)) {
//            // For image, return the Base64 string so the adapter can load it
//            return content; // Adapter will handle loading Base64
//        } else {
//            return "[" + messageType + " Message]"; // Fallback for other types
//        }
//    }
//}





public class ScheMsg {
    private String msgFirebaseId;
    private String senderId;
    // receiverIds and receiverNames will now hold info for *one* recipient per ScheMsg entry
    private List<String> receiverIds;
    private List<String> receiverNames;
    private Map<String, String> receiverIdToNameMap; // Map of the *single* recipient ID to name

    // *** NEW FIELD: Type of the recipient (User, Group, or Temporary Room) ***
    private String recipientType; // e.g., "user", "group", "temporary_room"
    // *** END NEW FIELD ***


    private String content; // Stores TEXT or IMAGE BASE64
    private String messageType; // "text" or "image"
    private long scheduledTimeMillis;
    private String scheduledTimeFormatted;
    private String status;
    private String receiverNamesStr; // String Join of receiverNames (will be one name)


    // --- Constructors ---
    // Required no-argument constructor for Firebase DataSnapshot.getValue()
    public ScheMsg() {
        // Default constructor needed for Firebase
    }

    // REMOVE the invalid empty constructor:
    // public ScheMsg(String senderId, List<String> singleRecipientIdList, List<String> singleRecipientNameList, String contentToSend, String messageType, long selectedTimeMillis, String formattedTime) {}


    // Modified constructor to accept the recipientType
    public ScheMsg(String senderId, List<String> receiverIds, List<String> receiverNames,
                   String content, String messageType,
                   long scheduledTimeMillis, String scheduledTimeFormatted,
                   String recipientType) {
        this.senderId = senderId;
        // Store copies of the lists (will have size 1 in new flow)
        this.receiverIds = (receiverIds != null) ? new ArrayList<>(receiverIds) : new ArrayList<>();
        this.receiverNames = (receiverNames != null) ? new ArrayList<>(receiverNames) : new ArrayList<>();
        this.content = content;
        this.messageType = (messageType != null && !messageType.isEmpty()) ? messageType : "text";
        this.scheduledTimeMillis = scheduledTimeMillis;
        this.scheduledTimeFormatted = scheduledTimeFormatted;
        this.status = "pending"; // Default status

        // *** Set the new recipientType field ***
        this.recipientType = recipientType;


        // Populate receiverIdToNameMap for the *single* recipient
        this.receiverIdToNameMap = new HashMap<>();
        if (this.receiverIds != null && this.receiverIds.size() == 1 && this.receiverNames != null && this.receiverNames.size() == 1) {
            String rId = this.receiverIds.get(0);
            String rName = this.receiverNames.get(0);
            if (!TextUtils.isEmpty(rId) && !TextUtils.isEmpty(rName)) {
                this.receiverIdToNameMap.put(rId, rName);
            } else {
                Log.w("ScheMsg", "Single recipient ID or Name is empty during map creation for msgId: " + msgFirebaseId); // Added msgId for context
            }
        } else {
            Log.w("ScheMsg", "Receiver IDs/Names list size not 1 during map creation for msgId: " + msgFirebaseId + ". Size IDs: " + (this.receiverIds != null ? this.receiverIds.size() : "null") + ", Size Names: " + (this.receiverNames != null ? this.receiverNames.size() : "null")); // Added msgId for context
        }


        // Generate the display string for receiver names (will be just one name)
        this.receiverNamesStr = TextUtils.join(", ", this.receiverNames);
    }

    // --- Getters and Setters (Added bodies) ---
    public String getMsgFirebaseId() { return msgFirebaseId; }
    public void setMsgFirebaseId(String msgFirebaseId) { this.msgFirebaseId = msgFirebaseId; }

    public String getSenderId() { return senderId; } // Added body
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public List<String> getReceiverIds() { return receiverIds; } // Added body
    public void setReceiverIds(List<String> receiverIds) { this.receiverIds = receiverIds; }

    public List<String> getReceiverNames() { return receiverNames; } // Added body
    public void setReceiverNames(List<String> receiverNames) { this.receiverNames = receiverNames; }

    public Map<String, String> getReceiverIdToNameMap() { return receiverIdToNameMap; }
    public void setReceiverIdToNameMap(Map<String, String> receiverIdToNameMap) { this.receiverIdToNameMap = receiverIdToNameMap; }

    public String getContent() { return content; } // Added body
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; } // Added body
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public long getScheduledTimeMillis() { return scheduledTimeMillis; } // Added body
    public void setScheduledTimeMillis(long scheduledTimeMillis) { this.scheduledTimeMillis = scheduledTimeMillis; }

    public String getScheduledTimeFormatted() { return scheduledTimeFormatted; } // Added body
    public void setScheduledTimeFormatted(String scheduledTimeFormatted) { this.scheduledTimeFormatted = scheduledTimeFormatted; }

    public String getStatus() { return status; } // Added body
    public void setStatus(String status) { this.status = status; }

    public String getReceiverNamesStr() { return receiverNamesStr; } // Added body
    public void setReceiverNamesStr(String receiverNamesStr) { this.receiverNamesStr = receiverNamesStr; }


    // *** NEW Getter and Setter for recipientType ***
    public String getRecipientType() { return recipientType; } // Added body
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    // *** END NEW Getter and Setter ***


    // --- Helper Getters for Adapter Display ---
    public String getReceiverInfoForDisplay() {
        return "To: " + getReceiverNamesStr() + (!TextUtils.isEmpty(recipientType) ? " (" + recipientType.substring(0, 1).toUpperCase() + recipientType.substring(1) + ")" : "");
    }

    public String getContentPreviewForDisplay() {
        if ("text".equals(messageType)) {
            return !TextUtils.isEmpty(content) ? content : "[Empty Text]";
        } else if ("image".equals(messageType)) {
            return content; // Return Base64 for adapter to load
        } else {
            return "[" + messageType + "]"; // Fallback for other types
        }
    }
}