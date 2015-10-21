package com.example.rxandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Dustin on 10/20/15.
 */
public abstract class WageCalculatorActivity extends AppCompatActivity {

    @Bind(R.id.hourly_wage)
    EditText _hourlyWage;

    @Bind(R.id.hours_per_week)
    EditText _hoursPerWeek;

    @Bind(R.id.savings_percent)
    EditText _savingsPercent;

    @Bind(R.id.weekly_pay)
    TextView _weeklyPay;

    @Bind(R.id.monthly_pay)
    TextView _monthlyPay;

    @Bind(R.id.yearly_pay)
    TextView _yearlyPay;

    @Bind(R.id.yearly_savings)
    TextView _yearlySavings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wage_calculator);
        ButterKnife.bind(this);
    }
}
