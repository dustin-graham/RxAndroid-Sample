package com.example.rxandroid;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * A version of the wage calculator that adheres more closely to a traditiona, imperative
 * programming pattern where we dictate all the transformational steps that have to be taken to
 * arrive at the final result.
 *
 * Created by Dustin on 10/20/15.
 */
public class ImperativeWageCalculatorActivity extends WageCalculatorActivity {

    private double _latestHourlyWageValue = 0;
    private int _latestWeeklyHoursValue = 0;
    private double _latestSavingsPercentValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        subscribeToTextChangedEvents();
    }

    private void subscribeToTextChangedEvents() {
        _hourlyWage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    _latestHourlyWageValue = Double.parseDouble(s.toString());
                    calculateResultsFromLatest();
                } catch (NumberFormatException ignored){
                    showError(_hourlyWage, "invalid input");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        _hoursPerWeek.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    _latestWeeklyHoursValue = Integer.parseInt(s.toString());
                    calculateResultsFromLatest();
                } catch (NumberFormatException ignored){
                    showError(_hoursPerWeek, "invalid input");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        _savingsPercent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    _latestSavingsPercentValue = Double.parseDouble(s.toString());
                    calculateResultsFromLatest();
                } catch (NumberFormatException ignored){
                    showError(_savingsPercent, "invalid input");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void calculateResultsFromLatest() {
        calculateResults(_latestHourlyWageValue, _latestWeeklyHoursValue, _latestSavingsPercentValue);
    }

    private void calculateResults(double hourlyWage, int weeklyHours, double savingsPercent) {
        //validate input
        if (inputsValidated(hourlyWage, weeklyHours, savingsPercent)) {
            //calculate weekly pay
            double weeklyPay = hourlyWage * weeklyHours;
            setWeeklyPay(weeklyPay, savingsPercent);
        }
    }

    private boolean inputsValidated(double hourlyWage, int weeklyHours, double savingsPercent) {
        if (hourlyWage < 0) {
            showError(_hourlyWage,"hourly wage must be >= 0");
            return false;
        }
        if (weeklyHours < 0) {
            showError(_hoursPerWeek, "weekly hours must be >= 0");
            return false;
        }
        if (weeklyHours > 168) {
            showError(_hoursPerWeek, "weekly hours must be <= 168");
            return false;
        }
        if (savingsPercent < 0) {
            showError(_savingsPercent, "savings percent must be >= 0");
            return false;
        }
        if (savingsPercent > 1.0) {
            showError(_savingsPercent, "savings percent must be <= 1.0");
            return false;
        }
        return true;
    }

    private void showError(EditText errorField, String message) {
        errorField.setError(message);
        setWeeklyPay(0, 0);
    }

    private void setWeeklyPay(double weeklyPay, double savingsPercent) {
        double weeklySavings = weeklyPay * savingsPercent;
        weeklyPay = weeklyPay - weeklySavings;
        _weeklyPay.setText(String.format("Weekly Pay: %.2f", weeklyPay));
        double monthlyPay = weeklyPay * 4;
        _monthlyPay.setText(String.format("Monthly Pay: %.2f", monthlyPay));
        double yearlyPay = monthlyPay * 12;
        _yearlyPay.setText(String.format("Yearly Pay: %.2f", yearlyPay));

        double yearlySavings = weeklySavings * 4 * 12;
        _yearlySavings.setText(String.format("Annual Savings: %.2f", yearlySavings));
    }
}
