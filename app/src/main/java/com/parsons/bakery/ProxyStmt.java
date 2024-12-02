package com.parsons.bakery;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProxyStmt {
    @GET("KidProxy/")
    Call<List<Post>> getPosts(@Query("token") String token, @Query("sqlStmt") String stmt);
}
