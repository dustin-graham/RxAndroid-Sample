package com.example.rxandroid.api;

import retrofit.RestAdapter;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Dustin on 2/11/15.
 */
public class RepresentativeApi {

    private WhoIsMyRep api;

    public RepresentativeApi() {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://whoismyrepresentative.com/")
                .build();

        api = restAdapter.create(WhoIsMyRep.class);
    }

    public Observable<Representative> representativesByZipCode(String zip) {
        return api.searchAllByZip(zip).flatMap(new Func1<ApiResult, Observable<? extends Representative>>() {
            @Override
            public Observable<? extends Representative> call(ApiResult apiResult) {
                return Observable.from(apiResult.results);
            }
        }).flatMap(new Func1<Representative, Observable<? extends  Representative>>() {
            @Override
            public Observable<? extends  Representative> call(Representative representative) {
                return Observable.zip(Observable.just(representative),isRepresentativeFunny(representative),new Func2<Representative, Boolean, Representative>() {
                    @Override
                    public Representative call(Representative representative, Boolean aBoolean) {
                        representative.isFunny = aBoolean;
                        return representative;
                    }
                });
            };
        });
    }

    /**
     * pretend like this is a remote service call
     * @param rep
     * @return
     */
    public Observable<Boolean> isRepresentativeFunny(Representative rep) {
        if (rep.name.trim().length() %2 == 0) {
            return Observable.just(true);
        }
        return Observable.just(false);
    }
}
