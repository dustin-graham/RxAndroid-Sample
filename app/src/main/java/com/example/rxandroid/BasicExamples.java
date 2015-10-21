package com.example.rxandroid;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Dustin on 10/21/15.
 */
public class BasicExamples {

    public void basicContractExample() {
        Observable.just(1,2,3,4,5)
            .subscribe(new Subscriber<Integer>() {
                @Override
                public void onCompleted() {
                    Log.d("Test", "sequence completed");
                }

                @Override
                public void onError(Throwable e) {
                    Log.e("Test", "got error: " + e.getMessage());
                }

                @Override
                public void onNext(Integer integer) {
                    Log.d("Test", "got int: " + integer);
                }
            });
    }

    public void basicSchedulerUsage() {
        Observable
            .just(1)
            .delay(10, TimeUnit.SECONDS, Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<Integer>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Integer integer) {
                    Log.d("Test", "this is on the main thread!");
                }
            });
    }
}
