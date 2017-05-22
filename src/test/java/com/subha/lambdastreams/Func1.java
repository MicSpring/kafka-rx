package com.subha.lambdastreams;

/**
 * Created by user on 5/14/2017.
 */

@FunctionalInterface
public interface Func1<T,R> {
    R transform(T t);
}
