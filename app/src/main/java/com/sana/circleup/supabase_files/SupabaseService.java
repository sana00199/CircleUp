package com.sana.circleup.supabase_files;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface SupabaseService {
    @Multipart
    @POST("storage/v1/object/{bucket}")
    Call<ResponseBody> uploadDocument(
            @Header("Authorization") String bearerToken,
            @Path("bucket") String bucket,
            @Part MultipartBody.Part file,
            @Query("file") String fileName
    );
}
