package com.example.rxandroid.util;

import rx.functions.Func1;

/**
 * Created by Dustin on 10/21/15.
 */
public class TextChangeToDouble implements Func1<CharSequence, Double> {

    @Override
    public Double call(CharSequence charSequence) {
        try {
            return Double.parseDouble(charSequence.toString());
        } catch (NumberFormatException e) {
            return 0D;
        }
    }
}
