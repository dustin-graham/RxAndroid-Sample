package com.example.rxandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Dustin on 10/20/15.
 */
public class SampleDirectoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_directory);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.imperative_wage_calculator)
    void goToImperativeWageCalculator() {
        Intent wageIntent = new Intent(this, ImperativeWageCalculatorActivity.class);
        startActivity(wageIntent);
    }

    @OnClick(R.id.declarative_wage_calculator)
    void goToDeclarativeWageCalculator() {
        Intent wageIntent = new Intent(this, ReactiveWageCalculatorActivity.class);
        startActivity(wageIntent);
    }

    @OnClick(R.id.representatives)
    void goToRepresentatives() {
        Intent representativeIntent = new Intent(this, FunnyRepresentativesActivity.class);
        startActivity(representativeIntent);
    }

}
