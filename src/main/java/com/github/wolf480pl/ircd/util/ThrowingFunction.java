package com.github.wolf480pl.ircd.util;

import java.util.concurrent.CompletionException;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R> extends Function<T, R> {

    R applyThrowing(T arg) throws Throwable;

    @Override
    default R apply(T arg) {
        try {
            return applyThrowing(arg);
        } catch (Throwable e) {
            throw new CompletionException(e);
        }
    }
}
