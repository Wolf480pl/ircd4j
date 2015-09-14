package com.github.wolf480pl.ircd.util;

import java.util.concurrent.CompletionException;
import java.util.function.Function;

@FunctionalInterface
public interface ExHandler<T> extends Function<Throwable, T> {

    T handle(Throwable t) throws Throwable;

    @Override
    default T apply(Throwable t) {
        if (t instanceof CompletionException) {
            t = t.getCause();
        }
        try {
            return handle(t);
        } catch (Throwable e) {
            throw new CompletionException(e);
        }
    }
}
