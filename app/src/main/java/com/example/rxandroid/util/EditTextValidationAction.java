package com.example.rxandroid.util;

import android.widget.EditText;

import rx.functions.Action1;

/**
 * Created by Dustin on 10/21/15.
 */
public class EditTextValidationAction<T> implements Action1<T> {

    private final EditText _editText;

    public EditTextValidationAction(EditText editText) {
        _editText = editText;
    }

    @Override
    public void call(Object o) {
        _editText.setError("invalid value");
    }
}
