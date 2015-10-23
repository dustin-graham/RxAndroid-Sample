package com.example.rxandroid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Dustin on 10/22/15.
 */
public class BasicExamplesTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testTestMergeCompletedBehavior() throws Exception {
        Observable<Integer> one = Observable.range(0, 10);
        Observable<Integer> two = one.take(5);

        Observable<Integer> merged = Observable.merge(two, one);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();

        merged.subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        int size = testSubscriber.getOnNextEvents().size();
        System.out.println("onNext count: " + size);
        assertThat(size, is(equalTo(15)));
    }
}