package com.example.vimeo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiInterface {

    @GET("/otp/installation/phoneNumberDetail/{accessToken}")
    Call<String> verifyUser(@Path("accessToken") String accessToken);

}
