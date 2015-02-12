package com.example.rxandroid.api;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Dustin on 2/10/15.
 */
public interface WhoIsMyRep {

    @GET("/getall_mems.php?output=json")
    Observable<ApiResult> searchAllByZip(@Query("zip")String zip);

}
