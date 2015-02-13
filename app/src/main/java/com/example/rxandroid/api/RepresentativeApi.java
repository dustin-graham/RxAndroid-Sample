package com.example.rxandroid.api;

import retrofit.RestAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Dustin on 2/11/15.
 */
public class RepresentativeApi {

    private WhoIsMyRep api;
    private int flakyRepRequestCount = 0;

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
     * This method can be used to practice handling dealing with errors. In this case, every other request errors out.
     * Observers could place a retry on this method for example
     * @param zip
     * @return
     */
    public Observable<Representative> representativesByZipCodeFlaky(final String zip) {

        return Observable.create(new Observable.OnSubscribe<Representative>() {
            @Override
            public void call(final Subscriber<? super Representative> subscriber) {
                flakyRepRequestCount++;
                if (flakyRepRequestCount % 2 == 0) {
                    subscriber.onError(new Exception("service flaked out, sorry"));
                } else {
                    api.searchAllByZip(zip).flatMap(new Func1<ApiResult, Observable<? extends Representative>>() {
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
                    }).subscribe(new Observer<Representative>() {
                        @Override
                        public void onCompleted() {
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onNext(Representative representative) {
                            subscriber.onNext(representative);
                        }
                    });
                }
            }
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
