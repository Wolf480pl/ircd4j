package com.github.wolf480pl.ircd.impl;

import io.netty.util.concurrent.EventExecutor;

public class NettyExecutor implements com.github.wolf480pl.ircd.util.EventExecutor {
    private final EventExecutor executor;

    public NettyExecutor(EventExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return executor.inEventLoop(thread);
    }

    @Override
    public boolean inEventLoop() {
        return executor.inEventLoop();
    }
}
