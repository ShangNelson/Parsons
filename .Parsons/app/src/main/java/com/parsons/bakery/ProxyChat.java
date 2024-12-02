package com.parsons.bakery;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProxyChat {
    @GET("KidProxy/")
    Call<List<Post>> getPosts(@Query("token") String token, @Query("message") String message, @Query("topic") String topic, @Query("isChat") String isChat, @Query("sender") String sender);
}
