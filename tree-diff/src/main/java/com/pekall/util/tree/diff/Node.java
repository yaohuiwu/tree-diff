package com.pekall.util.tree.diff;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node<T> {

    public T key;

    /**
     * 父节点
     */
    public Node<T> parent;

    /**
     * 子节点
     */
    public List<Node<T>> children;

    Node() {
    }

    Node(T key) {
        this.key = key;
    }

    public boolean isValid(){
        return key != null;
    }

    public List<T> getPath(){
        List<T> path = new ArrayList<>();
        path.add(key);
        Node<T> nodeParent = parent;
        while (nodeParent != null){
            path.add(nodeParent.key);
            nodeParent = nodeParent.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public int getLevel(){
        int level = 1;
        Node<T> nodeParent = parent;
        while (nodeParent != null){
            level++;
            nodeParent = nodeParent.parent;
        }
        return level;
    }

    public boolean hasChildren(){
        return children != null && !children.isEmpty();
    }

    @Override
    public String toString() {

        List<T> path = new ArrayList<>();
        path.add(key);
        Node<T> nodeParent = parent;
        while (nodeParent != null){
            path.add(nodeParent.key);
            nodeParent = nodeParent.parent;
        }
        Collections.reverse(path);
        return StringUtils.join(path, "/");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node<?> node = (Node<?>) o;

        return key.equals(node.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
