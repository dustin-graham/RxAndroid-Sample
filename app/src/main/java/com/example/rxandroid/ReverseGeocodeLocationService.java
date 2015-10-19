package com.example.rxandroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.example.rxandroid.error.MissingLocationPermissionException;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by Dustin on 2/12/15.
 */
public class ReverseGeocodeLocationService {

    public static Observable<String> getCurrentZip(final Context context, final LocationManager locationManager, final Geocoder geocoder) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    subscriber.onError(new Exception("GPS turned off"));
                } else {
                    int permissionCheck = ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                try {
                                    Observable.from(geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1)).take(1).subscribe(new Action1<Address>() {
                                        @Override
                                        public void call(Address address) {
                                            subscriber.onNext(address.getPostalCode());
                                            subscriber.onCompleted();
                                        }
                                    });
                                } catch (IOException e) {
                                    subscriber.onError(e);
                                }
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {

                            }

                            @Override
                            public void onProviderEnabled(String provider) {

                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                                subscriber.onError(new Exception("provider was disabled"));
                            }
                        }, null);
                    } else {
                        // permission denied return error
                        subscriber.onError(new MissingLocationPermissionException());
                    }
                }
            }
        });
    }
}
