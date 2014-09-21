package com.github.wolf480pl.ircd;


public interface CommandRegistry {

    public abstract void putCommand(String name, Command cmd);

}