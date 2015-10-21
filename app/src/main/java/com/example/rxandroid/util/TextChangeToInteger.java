package com.example.rxandroid.util;

import rx.functions.Func1;

/**
 * Created by Dustin on 10/21/15.
 */
public class TextChangeToInteger implements Func1<CharSequence, Integer> {

    @Override
    public Integer call(CharSequence charSequence) {
        try {
            return Integer.parseInt(charSequence.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
