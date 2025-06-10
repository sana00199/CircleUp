package com.sana.circleup.temporary_chat_room;

 // <<< Make sure this package is correct

// --- Android Imports ---
import android.content.Context; // Import Context
import android.graphics.Bitmap; // Import Bitmap
import android.graphics.BitmapFactory; // Import BitmapFactory
import android.text.TextUtils; // Import TextUtils
import android.util.Base64; // Import Base64
import android.util.Log; // Import Log for logging
import android.view.LayoutInflater; // Import LayoutInflater
import android.view.View; // Import View
import android.view.ViewGroup; // Import ViewGroup
import android.widget.ImageView; // Import ImageView
import android.widget.TextView; // Import TextView

// --- AndroidX Imports ---
import androidx.annotation.NonNull; // Import NonNull annotation
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

// --- Third-party Library Imports (Glide) ---
import com.bumptech.glide.Glide; // Import Glide for image loading


// --- Firebase Imports (Still needed for fetching profile images in onBindViewHolder) ---
import com.google.firebase.database.DataSnapshot; // Still needed for fetching user image
import com.google.firebase.database.DatabaseError; // Still needed for Firebase errors
import com.google.firebase.database.FirebaseDatabase; // Still needed for Firebase database instance
import com.google.firebase.database.ValueEventListener; // Still needed for fetching user image


// --- Other Project Class Imports (Adjust if your packages are different) ---
import com.sana.circleup.GroupMessage;
import com.sana.circleup.R; // <<< Make sure your R file is correctly imported for resource IDs
import com.sana.circleup.GroupMessage; // <<< Use the Firebase Model GroupMessage


// --- Standard Java Imports ---
import java.util.List; // Import List interface
import java.util.Map; // Import Map interface (if readBy is in GroupMessage Firebase model)
import java.util.Locale; // Import Locale for date/time formatting
import java.text.SimpleDateFormat; // Import SimpleDateFormat for date/time formatting
import java.util.Calendar; // Import Calendar for date calculations
import java.util.Date; // Import Date for current time

import de.hdodenhof.circleimageview.CircleImageView;


// This adapter is for Temporary Room Chats and uses the Firebase Model GroupMessage directly
public class TemporaryRoomMessageAdapter {

}

