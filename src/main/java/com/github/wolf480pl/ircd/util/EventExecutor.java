package com.github.wolf480pl.ircd.util;

import java.util.concurrent.Executor;

public interface EventExecutor extends Executor {

    boolean inEventLoop(Thread thread);

    default boolean inEventLoop() {
        return inEventLoop(Thread.currentThread());
    }

}
