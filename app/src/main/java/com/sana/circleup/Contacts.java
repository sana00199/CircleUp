//package com.sana.circleup;
//
//public class Contacts {
//    private String uid;
//    private String username;
//    private String profileImage;
//    private String fcmToken; // Add this field
//    private String status;
//    private String role;
//    private boolean isBlocked;
//    private String request_type; // 🔹 Add this field
//
//    // Default constructor (required for Firebase)
//    public Contacts() {
//    }
//
//    // Constructor with parameters
//    public Contacts(String uid, String username, String profileImage, String status, String role, boolean isBlocked, String request_type) {
//        this.uid = uid;
//        this.username = username;
//        this.profileImage = profileImage;
//        this.status = status;
//        this.role = role;
//        this.isBlocked = isBlocked;
//        this.request_type = request_type; // 🔹 Initialize request_type
//    }
//
//    // Getters and Setters
//    public String getUid() {
//        return uid;
//    }
//
//    public void setUid(String uid) {
//        this.uid = uid;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getProfileImage() {
//        return profileImage;
//    }
//
//    public void setProfileImage(String profileImage) {
//        this.profileImage = profileImage;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getRole() {
//        return role;
//    }
//
//    public String getFcmToken() {
//        return fcmToken;
//    }
//
//    public void setFcmToken(String fcmToken) {
//        this.fcmToken = fcmToken;
//    }
//
//
//
//    public void setRole(String role) {
//        this.role = role;
//    }
//
//    public boolean isBlocked() {
//        return isBlocked;
//    }
//
//    public void setBlocked(boolean blocked) {
//        isBlocked = blocked;
//    }
//
//    public String getRequest_type() { // 🔹 Getter for request_type
//        return request_type;
//    }
//
//    public void setRequest_type(String request_type) { // 🔹 Setter for request_type
//        this.request_type = request_type;
//    }
//
//
//
////    ✅ Debugging: Useful for logging Contact objects
//    @Override
//    public String toString() {
//        return "Contacts{" +
//                "uid='" + uid + '\'' +
//                ", username='" + username + '\'' +
//                ", profileImage='" + profileImage + '\'' +
//                ", status='" + status + '\'' +
//                ", role='" + role + '\'' +
//                ", blocked=" + isBlocked +
//                ", request_type='" + request_type + '\'' +
//                '}';
//    }
//}


package com.sana.circleup;

import android.os.Parcel;
import android.os.Parcelable;

public class Contacts implements Parcelable {
    private String uid;
    private String username;
    private String profileImage;
    private String fcmToken;
    private String status;
    private String role;
    private boolean isBlocked;
    private String request_type;


    // Added for contact selection dialog
    private boolean isSelected = false;

    // Default constructor (required for Firebase)
    public Contacts() {
    }

    // Constructor with parameters
    public Contacts(String uid, String username, String profileImage, String status, String role, boolean isBlocked, String request_type) {
        this.uid = uid;
        this.username = username;
        this.profileImage = profileImage;
        this.status = status;
        this.role = role;
        this.isBlocked = isBlocked;
        this.request_type = request_type;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    // Parcelable implementation
    protected Contacts(Parcel in) {
        uid = in.readString();
        username = in.readString();
        profileImage = in.readString();
        fcmToken = in.readString();
        status = in.readString();
        role = in.readString();
        isBlocked = in.readByte() != 0;
        request_type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(username);
        dest.writeString(profileImage);
        dest.writeString(fcmToken);
        dest.writeString(status);
        dest.writeString(role);
        dest.writeByte((byte) (isBlocked ? 1 : 0));
        dest.writeString(request_type);
    }



    // Getter and Setter for selection state
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Contacts> CREATOR = new Creator<Contacts>() {
        @Override
        public Contacts createFromParcel(Parcel in) {
            return new Contacts(in);
        }

        @Override
        public Contacts[] newArray(int size) {
            return new Contacts[size];
        }
    };
}
