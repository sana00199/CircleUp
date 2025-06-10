package com.sana.circleup.one_signal_notification;

// File: OneSignalApiService.java
// Apne package ke mutabiq theek kar lena. Agar naya folder banaya hai api/, toh usay use karna.

import com.google.gson.JsonObject; // Import JsonObject from Gson
import okhttp3.ResponseBody; // Import ResponseBody from OkHttp
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OneSignalApiService {

    // !!! SECURITY WARNING: Hardcoding your REST API key here is NOT SAFE for production apps !!!
    // !!! This is for demonstration/testing purposes given your constraints. !!!
    // !!! In production, always send from a secure backend server. !!!

    @Headers({
            // Replace "YOUR_ONESIGNAL_REST_API_KEY" with your actual key from OneSignal Dashboard -> Settings -> Keys & IDs -> REST API Key
            // Format is "Authorization: Basic YourActualKey"
            // Yeh tumhari REST API Key hai. Isay double quotes ("...") ke andar rakhna hai.
            "Authorization: Basic os_v2_app_bf7opcmeqrahrpzyt2e4gio4a5cp7wob54rup2467cnfftkpme3wxwuxxfso2o6udxew34owb4all7owub5mwkkwpq7tstdvx6myjii", // <-- YAHAN APNI ASLI KEY DAALEIN
            "Content-Type: application/json"
    })
    @POST("api/v1/notifications") // OneSignal API endpoint for sending notifications
    Call<ResponseBody> sendNotification(@Body JsonObject body);
}