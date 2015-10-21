package com.example.rxandroid.util;

import rx.functions.Func1;

/**
 * Created by Dustin on 10/21/15.
 */
public class ValidRangeInteger implements Func1<Integer, Boolean> {

    private final int _min;
    private final int _max;

    public ValidRangeInteger(int min, int max) {
        _min = min;
        _max = max;
    }

    @Override
    public Boolean call(Integer value) {
        return value >= _min && value <= _max;
    }
}
