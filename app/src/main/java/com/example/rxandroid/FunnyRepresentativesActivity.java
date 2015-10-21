package com.example.rxandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.rxandroid.api.Representative;
import com.example.rxandroid.api.RepresentativeAdapter;
import com.example.rxandroid.api.RepresentativeApi;
import com.example.rxandroid.util.ProgressObservable;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.example.rxandroid.util.ContentObservable.fromBroadcast;

public class FunnyRepresentativesActivity extends RxAppCompatActivity {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private CompositeSubscription _subscriptions;
    @Bind(R.id.searchField) EditText searchField;
    @Bind(R.id.resultList) RecyclerView resultList;
    @Bind(R.id.loadingProgress)
    SmoothProgressBar progressBar;
    private RepresentativeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RepresentativeApi representativeApi;
    private LocationManager locationManager;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        resultList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        resultList.setLayoutManager(layoutManager);
        adapter = new RepresentativeAdapter();
        resultList.setAdapter(adapter);

        representativeApi = new RepresentativeApi();

        _subscriptions = new CompositeSubscription();

        createBufferedSearchObservable(searchField)
                .flatMap(new Func1<String, Observable<List<Representative>>>() {
                    @Override
                    public Observable<List<Representative>> call(String s) {
                        return representativeApi.representativesByZipCode(s).toList();
                    }
                })
                .compose(this.<List<Representative>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onRepresentativesReceived());

        fromBroadcast(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
                .compose(this.<Intent>bindToLifecycle())
                .subscribe(onConnectivityChanged());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(R.id.look_up_zip_button)
    void onAutoFindButtonClicked() {

        Observable<String> zipObservable = ReverseGeocodeLocationService
                .getCurrentZip(FunnyRepresentativesActivity.this, locationManager, geocoder);

        ProgressObservable
            .fromObservable(zipObservable, this, "Finding Zip Code", "Please Wait…", true, true)
            .compose(this.<String>bindUntilEvent(ActivityEvent.PAUSE))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<String>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    new AlertDialog.Builder(FunnyRepresentativesActivity.this)
                            .setTitle("Error!")
                            .setMessage(e.getMessage()).create().show();
                }

                @Override
                public void onNext(String s) {
                    onZipCodeReceived(s);
                }
            });

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void showLocationPermissionExplanation() {
        new AlertDialog.Builder(FunnyRepresentativesActivity.this)
                .setTitle("Location Permission")
                .setMessage("Grant this application location access to automatically find your representatives")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            getZipCode();
        }
    }

    private void onZipCodeReceived(String zipcode) {
        searchField.setText(zipcode);
    }

    private Action1<String> onQueryEntered() {
        return new Action1<String>() {
            @Override
            public void call(String s) {
                progressBar.setVisibility(View.VISIBLE);
//                representativeApi.representativesByZipCode(s)
                representativeApi.representativesByZipCodeFlaky(s)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
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
                    new AlertDialog.Builder(FunnyRepresentativesActivity.this).setTitle("Connectivity Error").setMessage("Network access is currently unavailable. Please reconnect.").setPositiveButton("OK",new DialogInterface.OnClickListener() {
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

        }).debounce(1, TimeUnit.SECONDS, Schedulers.io());
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
