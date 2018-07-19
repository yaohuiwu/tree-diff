package com.pekall.util.tree.diff;

public class NodeEvent<T> {

    private NodeEventType eventType;
    private Node<T> node;

    public NodeEvent(NodeEventType eventType, Node<T> node) {
        this.eventType = eventType;
        this.node = node;
    }

    public NodeEventType getEventType() {
        return eventType;
    }

    public Node<T> getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "NodeEvent{" +
                "eventType=" + eventType +
                ", node=" + node +
                '}';
    }

    public void setNode(Node<T> node) {
        this.node = node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeEvent<?> nodeEvent = (NodeEvent<?>) o;

        if (eventType != nodeEvent.eventType) return false;
        return node.equals(nodeEvent.node);
    }

    @Override
    public int hashCode() {
        int result = eventType.hashCode();
        result = 31 * result + node.hashCode();
        return result;
    }
}
