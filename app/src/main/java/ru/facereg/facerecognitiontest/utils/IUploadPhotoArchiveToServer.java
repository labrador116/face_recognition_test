package ru.facereg.facerecognitiontest.utils;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * @author Markin Andrey on 21.08.2018.
 */
public interface IUploadPhotoArchiveToServer {
    @Multipart
    @POST("api/frames/sync-v2")
    Call<ResponseBody> upload(@Part("setId")RequestBody id, @Header("authorization") String jwt, @Part("file") RequestBody file);
}
