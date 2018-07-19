package com.pekall.util.tree.diff;

public interface Visitor<T> {

    void visit(Node<T> node);
}
