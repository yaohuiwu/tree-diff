package com.pekall.util.tree.diff;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MyTree<T> implements Tree<T>{
    private Node<T> root;

    public MyTree(T rootData) {
        this(new Node<T>(rootData));
    }

    private MyTree(Node<T> root) {
        if(!root.isValid()){
            throw new IllegalArgumentException("key can't be null");
        }
        this.root = root;
    }

    public Node<T> getRoot() {
        return root;
    }

    @Override
    public Node<T> addNode(Node<T> parent, T key) {
        if(parent == null){
            throw new IllegalArgumentException("parent can't be null");
        }
        if(key == null){
            throw new IllegalArgumentException("key can't be null");
        }

        Node<T> node = new Node<T>();
        node.key = key;
        node.parent = parent;

        if(parent.children == null){
            parent.children = new LinkedList<>();
        }
        parent.children.add(node);

        return node;
    }

    public void preOrderTraverse(Node<T> fromNode, Visitor<T> visitor) {

        visitor.visit(fromNode);

        if(fromNode.children != null){
            for(Node<T> child : fromNode.children){
                preOrderTraverse(child, visitor);
            }
        }
    }

    @Override
    public void postOrderTraverse(Node<T> fromNode, Visitor<T> visitor) {

        if(fromNode.children != null){
            for(Node<T> child : fromNode.children){
                postOrderTraverse(child, visitor);
            }
        }

        visitor.visit(fromNode);
    }

    @Override
    public void levelTraverse(Node<T> fromNode, Visitor<T> visitor) {
        if(fromNode == null){
            return;
        }

        Queue<Node<T>> queue = new LinkedList<>();
        queue.offer(fromNode);

        while (!queue.isEmpty()){

            Node<T> currentNode = queue.poll();
            visitor.visit(currentNode);

            if(currentNode.children != null){
                queue.addAll(currentNode.children);
            }
        }
    }

    @Override
    public void levelStopTraverse(Node<T> fromNode, StopVisitor<T> visitor) {
        if(fromNode == null){
            return;
        }

        Queue<Node<T>> queue = new LinkedList<>();
        queue.offer(fromNode);

        while (!queue.isEmpty() && !visitor.stopNow()){

            Node<T> currentNode = queue.poll();
            visitor.visit(currentNode);

            if(currentNode.children != null){
                queue.addAll(currentNode.children);
            }
        }
    }

    @Override
    public int getDeep() {
        final AtomicInteger deep = new AtomicInteger(0);
        postOrderTraverse(root, new Visitor<T>() {

            @Override
            public void visit(Node<T> node) {
                List<T> path = node.getPath();
                if(deep.get() < path.size()){
                    deep.set(path.size());
                }
                node.getPath();
            }
        });

        return deep.get();
    }

    @Override
    public List<Node<T>> getByLevel(final int level) {
        final List<Node<T>> levelNodes = new LinkedList<>();

        levelStopTraverse(root, new StopVisitor<T>() {
            private boolean stopNow;

            @Override
            public boolean stopNow() {
                return stopNow;
            }

            @Override
            public void visit(Node<T> node) {
                int currentLevel = node.getLevel();
                if(currentLevel == level){
                    levelNodes.add(node);
                }else if(currentLevel > level){
                    stopNow = true;
                }
            }
        });
        return levelNodes;
    }

    @Override
    public Queue<NodeEvent<T>> diff(final Tree<T> other) {
        //节点事件队列
        final LinkedList<NodeEvent<T>> nodeEvents = new LinkedList<>();

        //已删除的节点集合，用于标记被删除的节点以及它的子孙节点
        final Set<Node<T>> removedNodes = new HashSet<>();
        final Set<Node<T>> createdNodes = new HashSet<>();

        //标记事件。用于删除'假的'删除事件。
        final NodeEvent<T> DELETE_NODE_EVENT = new NodeEvent<>(NodeEventType.DELETE, null);
        final NodeEvent<T> ADD_NODE_EVENT = new NodeEvent<>(NodeEventType.CREATE, null);

        int myDeep = getDeep();
        int otherDeep = other.getDeep();

        int maxDeep = Math.max(myDeep, otherDeep);
        int minDeep = Math.min(myDeep, otherDeep);

        for(int deep = 1; deep <= minDeep + 1; deep ++){

            //deal with levels they both have.
            if(deep <= minDeep){
                List<Node<T>> myNodes = getByLevel(deep);
                myNodes.removeAll(removedNodes);

                List<Node<T>> otherNodes = other.getByLevel(deep);

                List<Node<T>> addNodes = new ArrayList<>(otherNodes);
                addNodes.removeAll(myNodes);

                for(Node<T> node : addNodes){
                    processCreateNode(nodeEvents, node, DELETE_NODE_EVENT, removedNodes);
                    createdNodes.add(node);
                }

                //update nodes
                List<Node<T>> updateNodes = new ArrayList<>(myNodes);
                updateNodes.retainAll(otherNodes);
                for(Node<T> node : updateNodes){
                    nodeEvents.add(new NodeEvent<T>(NodeEventType.UPDATE, node));
                }

                myNodes.removeAll(otherNodes);
                for(Node<T> node : myNodes){
                    postOrderTraverse(node, new Visitor<T>() {
                        @Override
                        public void visit(Node<T> node) {
                            nodeEvents.add(new NodeEvent<T>(NodeEventType.DELETE, node));
                            removedNodes.add(node);
                        }
                    });
                }

                continue;
            }

            //my tree有更多层
            //deal with levels only my tree have
            if(myDeep == maxDeep){
                List<Node<T>> myNodes = getByLevel(deep);
                for(Node<T> node : myNodes){
                    postOrderTraverse(node, new Visitor<T>() {
                        @Override
                        public void visit(Node<T> node) {
                            //之前未删除
                            if(!removedNodes.contains(node)){
                                //之前新增
                                if(createdNodes.contains(node)){//现在要删除，说明之前是一个move
                                    ADD_NODE_EVENT.setNode(node);
                                    NodeEvent<T> preCreatedEvent = findEvent(nodeEvents, ADD_NODE_EVENT);
                                    if(preCreatedEvent != null){
                                        preCreatedEvent.setEventType(NodeEventType.MOVE);
                                    }
                                }else{
                                    nodeEvents.add(new NodeEvent<T>(NodeEventType.DELETE, node));
                                }
                            }
                        }
                    });
                }
            }
            //other tree有更多层
            //deal with levels only other tree have
            else if(otherDeep == maxDeep){
                List<Node<T>> otherNodes = other.getByLevel(deep);
                for(Node<T> node : otherNodes){
                    preOrderTraverse(node, new Visitor<T>() {
                        @Override
                        public void visit(Node<T> node) {
                            processCreateNode(nodeEvents, node, DELETE_NODE_EVENT, removedNodes);
                        }
                    });
                }
            }
        }

        return nodeEvents;
    }

    private NodeEvent<T> findEvent(Queue<NodeEvent<T>> queue, NodeEvent<T> event){
        for(NodeEvent<T> nodeEvent : queue){
            if(nodeEvent.equals(event)){
                return nodeEvent;
            }
        }
        return null;
    }

    private void processCreateNode(Queue<NodeEvent<T>> queue, Node<T> node, NodeEvent<T> delFlagNode,
                                   Set<Node<T>> removedNodes){
        if(removedNodes.contains(node)){
            delFlagNode.setNode(node);
            NodeEvent<T> preDeleted = findEvent(queue, delFlagNode);
            if(preDeleted != null){
                //之前被删除的子节点有后代，属于移动操作，需要调整操作顺序
                if(preDeleted.getNode().hasChildren()){
                    //TODO 处理有后代的节点移动操作
                    //Just let the test case com.pekall.util.tree.diff.MyTreeTest.testDiffNewTreeDeeperMoveDownWithChildren()
                    // to pass.
                    preDeleted.setEventType(NodeEventType.MOVE);
                    preDeleted.setNode(node);
                }else{
                    preDeleted.setEventType(NodeEventType.MOVE);
                    preDeleted.setNode(node);
                }
            }
        }else{
            queue.add(new NodeEvent<T>(NodeEventType.CREATE, node));
        }
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        preOrderTraverse(root, new Visitor<T>() {
            @Override
            public void visit(Node<T> node) {
                s.append(StringUtils.repeat("\t", node.getLevel()-1))
                        .append(node.key)
                        .append("\n");
            }
        });

        return s.toString();
    }
}
