package com.example.rxandroid.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class ProgressObservable {
    /**
     * @param source        the source observable you are going to observe
     * @param context       used for creating the progress dialog
     * @param title         for the dialog
     * @param message       for the dialog
     * @param indeterminate for the dialog
     * @param cancelable    for the dialog
     * @return an Observable<T> that will present a progress UI while it is being subscribed to.
     * Dismissing the dialog will also unsubscribe the subscriber. When the observable finishes the
     * dialog will be dismissed automatically.
     */
    public static <T> Observable<T> fromObservable(final Observable<T> source,
                                                   final Context context, final String title,
                                                   final String message, final boolean indeterminate,
                                                   final boolean cancelable) {
        Observable.OnSubscribe<T> wrappedSubscription = new Observable.OnSubscribe<T>() {

            @Override
            public void call(final Subscriber<? super T> subscriber) {
                final ProgressDialog progressDialog = ProgressDialog.show(context, title,
                        message, indeterminate, cancelable, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                subscriber.unsubscribe();
                            }
                        });
                Subscription progressSubscription = Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.dismiss();
                    }
                });
                subscriber.add(progressSubscription);
                source.subscribe(subscriber);

            }
        };
        return Observable.create(wrappedSubscription);
    }
}