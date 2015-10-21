package com.example.rxandroid;

import android.os.Bundle;

import com.example.rxandroid.util.EditTextValidationAction;
import com.example.rxandroid.util.TextChangeToDouble;
import com.example.rxandroid.util.TextChangeToInteger;
import com.example.rxandroid.util.ValidRangeDouble;
import com.example.rxandroid.util.ValidRangeInteger;
import com.jakewharton.rxbinding.widget.RxTextView;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;

/**
 * Attempts to highlight filtering, mapping, and combining. When coming to an understanding of what
 * Rx is all about, it's important not to underestimate the value of clearly defined functions.
 * For example, there is a clearly defined function that determines the validity of an input field.
 * That piece of truth can be fed into other functions and manipulated in useful ways and not lost
 * in random variables in the stack, or confused in stateful variables on the heap. We get small,
 * dedicated, streams of truth.
 *
 * Created by Dustin on 10/20/15.
 */
public class ReactiveWageCalculatorActivity extends WageCalculatorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    Observable<Double> hourlyWageObservable = RxTextView.textChanges(_hourlyWage)
        .map(new Func1<CharSequence, Double>() {
            @Override
            public Double call(CharSequence charSequence) {
                try {
                    return Double.parseDouble(charSequence.toString());
                } catch (NumberFormatException e) {
                    return 0D;
                }
            }
        });

        Observable<Boolean> hourlyWageValid = hourlyWageObservable.map(new ValidRangeDouble(0, Double.MAX_VALUE));
        Observable<Double> validWageValues = hourlyWageObservable.filter(new ValidRangeDouble(0, Double.MAX_VALUE));


        Observable<Integer> hoursPerWeekObservable = RxTextView.textChanges(_hoursPerWeek)
                .map(new TextChangeToInteger());

        Observable<Boolean> hoursPerWeekValid = hoursPerWeekObservable.map(new ValidRangeInteger(0, 168));
        Observable<Integer> validHoursValues = hoursPerWeekObservable.filter(new ValidRangeInteger(0, 168));

        Observable<Double> savingsPercentObservable = RxTextView.textChanges(_savingsPercent)
                .map(new TextChangeToDouble());

        Observable<Boolean> savingsValid = savingsPercentObservable.map(new ValidRangeDouble(0, 1.0));
        Observable<Double> validSavingsValues = savingsPercentObservable.filter(new ValidRangeDouble(0, 1.0));


        // subscribe to clean updates
        Observable.combineLatest(
                validWageValues, validHoursValues, validSavingsValues,
                new Func3<Double, Integer, Double, WageInfo>() {
                    @Override
                    public WageInfo call(Double hourlyWage, Integer hoursPerWeek, Double savingsPercent) {
                        return new WageInfo(hourlyWage, hoursPerWeek, savingsPercent);
                    }
                })
                .subscribe(new Action1<WageInfo>() {
                    @Override
                    public void call(WageInfo wageInfo) {
                        updateResult(wageInfo);

                    }
                });


        //subscribe to validation errors
        Func1<Boolean, Boolean> invalidPredicate = new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean aBoolean) {
                return !aBoolean;
            }
        };
        hourlyWageValid.filter(invalidPredicate).subscribe(new EditTextValidationAction<Boolean>(_hourlyWage));
        hoursPerWeekValid.filter(invalidPredicate).subscribe(new EditTextValidationAction<Boolean>(_hoursPerWeek));
        savingsValid.filter(invalidPredicate).subscribe(new EditTextValidationAction<Boolean>(_savingsPercent));

    }

    private void updateResult(WageInfo wageInfo) {
        _weeklyPay.setText(String.format("Weekly Pay: %.2f", wageInfo.getWeeklyPay()));
        _monthlyPay.setText(String.format("Monthly Pay: %.2f", wageInfo.getMonthlyPay()));
        _yearlyPay.setText(String.format("Yearly Pay: %.2f", wageInfo.getYearlyPay()));
        _yearlySavings.setText(String.format("Annual Savings: %.2f", wageInfo.getYearlySavings()));
    }

    private class WageInfo {
        public final double hourlyRate;
        public final int hoursPerWeek;
        public final double savingsPercent;

        private WageInfo(double hourlyRate, int hoursPerWeek, double savingsPercent) {
            this.hourlyRate = hourlyRate;
            this.hoursPerWeek = hoursPerWeek;
            this.savingsPercent = savingsPercent;
        }

        private double getWeeklySavings() {
            return hourlyRate * hoursPerWeek * savingsPercent;
        }

        public double getWeeklyPay() {
            double gross = hourlyRate * hoursPerWeek;
            return gross - getWeeklySavings();
        }

        public double getMonthlyPay() {
            return getWeeklyPay() * 4;
        }

        public double getYearlyPay() {
            return getMonthlyPay() * 12;
        }

        public double getYearlySavings() {
            return getWeeklySavings() * 4 * 12;
        }
    }

}
