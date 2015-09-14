package com.github.wolf480pl.ircd.util;

import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;

@FunctionalInterface
public interface BiHandler<T, R> extends BiFunction<T, Throwable, R> {

    R handle(T res, Throwable ex) throws Throwable;

    @Override
    default R apply(T res, Throwable t) {
        if (t instanceof CompletionException) {
            t = t.getCause();
        }
        try {
            return handle(res, t);
        } catch (Throwable e) {
            throw new CompletionException(e);
        }
    }
}
