package com.owncloud.android.api;

import com.google.gson.JsonObject;
import com.owncloud.android.datamodel.Plan;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SpaciumCloudApiInterface {

    @GET("plans")
    Call<List<Plan>> getPlans();

    @FormUrlEncoded
    @POST("users/register")
    Call<JsonObject> register(@Field("name") String name,@Field("email") String email,@Field("password") String password);
    @FormUrlEncoded
    @POST("subscriptions/process")
    Call<JsonObject> processSubscription(@Field("purchaseJson") String purchaseJson,@Field("account") String account);

}
