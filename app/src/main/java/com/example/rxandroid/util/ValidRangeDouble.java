package com.example.rxandroid.util;

import rx.functions.Func1;

/**
 * Created by Dustin on 10/21/15.
 */
public class ValidRangeDouble implements Func1<Double, Boolean> {

    private final double _min;
    private final double _max;

    public ValidRangeDouble(double min, double max) {
        _min = min;
        _max = max;
    }

    @Override
    public Boolean call(Double value) {
        return value >= _min && value <= _max;
    }
}
