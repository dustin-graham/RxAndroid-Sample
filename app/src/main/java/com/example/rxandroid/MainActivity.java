package com.example.rxandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.rxandroid.api.Representative;
import com.example.rxandroid.api.RepresentativeAdapter;
import com.example.rxandroid.api.RepresentativeApi;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static rx.android.app.AppObservable.bindActivity;
import static rx.android.content.ContentObservable.fromBroadcast;

public class MainActivity extends ActionBarActivity {

    private CompositeSubscription _subscriptions;
    @InjectView(R.id.searchField) EditText searchField;
    @InjectView(R.id.resultList) RecyclerView resultList;
    @InjectView(R.id.loadingProgress)
    SmoothProgressBar progressBar;
    private RepresentativeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RepresentativeApi representativeApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        resultList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        resultList.setLayoutManager(layoutManager);
        adapter = new RepresentativeAdapter();
        resultList.setAdapter(adapter);

        representativeApi = new RepresentativeApi();

        _subscriptions = new CompositeSubscription();

        _subscriptions.add(bindActivity(this, createBufferedSearchObservable(searchField)).subscribe(onQueryEntered()));

        _subscriptions.add(bindActivity(this,fromBroadcast(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)))
                .subscribe(onConnectivityChanged()));

        LocationManager locationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Geocoder geocoder = new Geocoder(this);
        _subscriptions.add(bindActivity(this, ReverseGeocodeLocationService.getCurrentZip(locationManager, geocoder)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()))
                .subscribe(onZipCodeReceived()));

    }

    private Observer<String> onZipCodeReceived() {
        return new Observer<String>() {
            @Override
            public void onCompleted() {
                Timber.d("geocoder completed");
            }

            @Override
            public void onError(Throwable e) {
                Timber.d("geocoder error: " + e.getMessage());
                new AlertDialog.Builder(MainActivity.this).setTitle("Location Error").setMessage("Failed to find your current zip code").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }

            @Override
            public void onNext(String s) {
                searchField.setText(s);
            }
        };
    }

    private Action1<String> onQueryEntered() {
        return new Action1<String>() {
            @Override
            public void call(String s) {
                progressBar.setVisibility(View.VISIBLE);
//                representativeApi.representativesByZipCode(s)
                representativeApi.representativesByZipCodeFlaky(s)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .toList()
                    .retry(2)
                    .subscribe(onRepresentativesReceived());
            }
        };
    }

    private Observer<List<Representative>> onRepresentativesReceived() {
        return new Observer<List<Representative>>() {
            @Override
            public void onCompleted() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable e) {
                progressBar.setVisibility(View.GONE);
                adapter.swap(null);
            }

            @Override
            public void onNext(List<Representative> representatives) {
                adapter.swap(representatives);
            }
        };
    }

    private Action1<Intent> onConnectivityChanged() {
        return new Action1<Intent>() {
            @Override
            public void call(Intent intent) {
                boolean noConnectivity = intent.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (noConnectivity) {
                    new AlertDialog.Builder(MainActivity.this).setTitle("Connectivity Error").setMessage("Network access is currently unavailable. Please reconnect.").setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
            }
        };
    }

    private Observable<String> createBufferedSearchObservable(final EditText inputField) {
        Observable<String> searchFieldObservable = Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(final Subscriber<? super String> subscriber) {
                inputField.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        subscriber.onNext(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }

        }).debounce(400, TimeUnit.MILLISECONDS, Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        return searchFieldObservable;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _subscriptions.unsubscribe();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
