package com.daniyal.appspro.networklibrary;

import com.daniyal.appspro.models.AddPersonResponse;
import com.daniyal.appspro.models.RecoginizeApiResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST("add")
    @Multipart
    Call<AddPersonResponse> addPerson(@PartMap Map<String, RequestBody> map, @Part("id") RequestBody id, @Part("name") RequestBody name);

    @POST("recognize")
    @Multipart
    Call<RecoginizeApiResponse> recoginizePerson(@PartMap Map<String, RequestBody> map);

}
