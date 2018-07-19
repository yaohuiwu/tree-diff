package com.pekall.util.tree.diff;

public interface StopVisitor<T> extends Visitor<T>{

    boolean stopNow();
}
