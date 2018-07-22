package com.pekall.util.tree.diff;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Queue;

import static com.pekall.util.tree.diff.NodeEventType.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class MyTreeTest {

    private Tree<Integer> oldTree;

    @Before
    public void setUp() throws Exception {
        oldTree = sampleTree();
    }

    /**
     * 示例tree.
     *
     * <pre>
     *     1
                2
                    5
                    6
                3
                    7
                    8
                4
                    9
                    10
     *
     * @return a sample tree.
     */
    private static Tree<Integer> sampleTree(){
        Tree<Integer> myTree = new MyTree<>(1);
        Node<Integer> node2 = myTree.addNode(myTree.getRoot(), 2);
        Node<Integer> node3 = myTree.addNode(myTree.getRoot(), 3);
        Node<Integer> node4 = myTree.addNode(myTree.getRoot(), 4);

        myTree.addNode(node2, 5);
        myTree.addNode(node2, 6);

        myTree.addNode(node3, 7);
        myTree.addNode(node3, 8);

        myTree.addNode(node4, 9);
        myTree.addNode(node4, 10);

        System.out.println(myTree);

        return myTree;
    }

    private static void assertEvent(NodeEvent<Integer> nodeEvent, NodeEventType eventType, Integer key){
        assertThat(nodeEvent.getEventType(), is(eventType));
        assertThat(nodeEvent.getNode().key, is(key));
    }

    @Test
    public void testGetRoot() {
        MyTree<Integer> myTree = new MyTree<>(1);

        assertNotNull(myTree.getRoot());
        assertThat(myTree.getRoot().key, is(1));
        assertThat(myTree.getRoot().parent, is(nullValue()));
        assertThat(myTree.getRoot().children, is(nullValue()));
    }

    @Test
    public void testGetDeep1() {
        MyTree<Integer> myTree = new MyTree<>(1);
        assertThat(myTree.getDeep(), is(1));
    }

    @Test
    public void testGetDeep2() {
        MyTree<Integer> myTree = new MyTree<>(1);
        myTree.addNode(myTree.getRoot(), 2);

        Node<Integer> node3 = myTree.addNode(myTree.getRoot(), 3);
        Node<Integer> node4 = myTree.addNode(node3, 4);
        myTree.addNode(node4, 5);

        assertThat(myTree.getDeep(), is(4));
    }

    @Test
    public void testGetByLevel() {
        Tree<Integer> myTree = sampleTree();

        List<Node<Integer>> level1 = myTree.getByLevel(1);

        assertNotNull(level1);
        assertThat(level1.size(), is(1));
        assertThat(level1.get(0).key, is(1));

        List<Node<Integer>> level2 = myTree.getByLevel(2);

        assertNotNull(level2);
        assertThat(level2.size(), is(3));
        assertThat(level2.get(0).key, is(2));
        assertThat(level2.get(1).key, is(3));
        assertThat(level2.get(2).key, is(4));

        List<Node<Integer>> level3 = myTree.getByLevel(3);

        assertNotNull(level3);
        assertThat(level3.size(), is(6));
        assertThat(level3.get(0).key, is(5));
        assertThat(level3.get(5).key, is(10));
    }

    @Test
    public void testHasChildren() {
        assertTrue(oldTree.getRoot().hasChildren());

        Node<Integer> node11 = oldTree.addNode(oldTree.getRoot(), 11);
        assertFalse(node11.hasChildren());
    }

    @Test
    public void testDiffSelf() {
        Queue<NodeEvent<Integer>> events = oldTree.diff(oldTree);

        assertThat(events.size(), is(10));
        for(int i=0; i<10; i++){
            assertThat(events.poll().getEventType(), is(UPDATE));
        }
    }

    @Test
    public void testDiffOldTreeDeeper() {
        Tree<Integer> newTree = new MyTree<>(1);
        newTree.addNode(newTree.getRoot(), 2);
        newTree.addNode(newTree.getRoot(), 3);

        System.out.println(newTree);

        Queue<NodeEvent<Integer>> events = oldTree.diff(newTree);

        System.out.println(events);

        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertThat(events.size(), is(10));

        assertEvent(events.poll(), UPDATE, 1);
        assertEvent(events.poll(), UPDATE, 2);
        assertEvent(events.poll(), UPDATE, 3);
        assertEvent(events.poll(), DELETE, 9);
        assertEvent(events.poll(), DELETE, 10);
        assertEvent(events.poll(), DELETE, 4);

        assertEvent(events.poll(), DELETE, 5);
        assertEvent(events.poll(), DELETE, 6);
        assertEvent(events.poll(), DELETE, 7);
        assertEvent(events.poll(), DELETE, 8);
    }

    @Test
    public void testDiffOldTreeDeeperMoveUpWithNoChildren() {
        /*
            1
                2
                    5
                    6
                3
                    7
                    8
                4
                    9
                    10

            1
                2
                3
                8
         */

        Tree<Integer> newTree = new MyTree<>(1);
        newTree.addNode(newTree.getRoot(), 2);
        newTree.addNode(newTree.getRoot(), 3);
        newTree.addNode(newTree.getRoot(), 8);

        System.out.println(newTree);

        Queue<NodeEvent<Integer>> events = oldTree.diff(newTree);

        System.out.println(events);

        assertThat(events.size(), is(10));

        assertEvent(events.poll(), UPDATE, 1);
        assertEvent(events.poll(), MOVE, 8);
        assertEvent(events.poll(), UPDATE, 2);
        assertEvent(events.poll(), UPDATE, 3);
        assertEvent(events.poll(), DELETE, 9);
        assertEvent(events.poll(), DELETE, 10);
        assertEvent(events.poll(), DELETE, 4);
        assertEvent(events.poll(), DELETE, 5);
        assertEvent(events.poll(), DELETE, 6);
        assertEvent(events.poll(), DELETE, 7);
    }

    @Test
    public void testDiffOldTreeDeeperMoveUpWithChildren() {
        /*
        old tree:
            1
                2
                    5
                    6
                3
                    7
                    8
                        11
                4
                    9
                    10
        new tree:
            1
                2
                3
                8
                11
         */
        Tree<Integer> myTree = new MyTree<>(1);
        Node<Integer> node2 = myTree.addNode(myTree.getRoot(), 2);
        Node<Integer> node3 = myTree.addNode(myTree.getRoot(), 3);
        Node<Integer> node4 = myTree.addNode(myTree.getRoot(), 4);

        myTree.addNode(node2, 5);
        myTree.addNode(node2, 6);

        myTree.addNode(node3, 7);
        Node<Integer> node8 = myTree.addNode(node3, 8);
        myTree.addNode(node8, 11);

        myTree.addNode(node4, 9);
        myTree.addNode(node4, 10);

        System.out.println(myTree);

        Tree<Integer> newTree = new MyTree<>(1);
        newTree.addNode(newTree.getRoot(), 2);
        newTree.addNode(newTree.getRoot(), 3);
        newTree.addNode(newTree.getRoot(), 8);
        newTree.addNode(newTree.getRoot(), 11);

        System.out.println(newTree);

        Queue<NodeEvent<Integer>> events = myTree.diff(newTree);

        System.out.println(events);

        assertThat(events.size(), is(11));

        assertEvent(events.poll(), UPDATE, 1);
        assertEvent(events.poll(), MOVE, 8);
        assertEvent(events.poll(), MOVE, 11);
        assertEvent(events.poll(), UPDATE, 2);
        assertEvent(events.poll(), UPDATE, 3);
        assertEvent(events.poll(), DELETE, 9);
        assertEvent(events.poll(), DELETE, 10);
        assertEvent(events.poll(), DELETE, 4);
        assertEvent(events.poll(), DELETE, 5);
        assertEvent(events.poll(), DELETE, 6);
        assertEvent(events.poll(), DELETE, 7);
    }

    @Test
    public void testDiffNewTreeDeeperMoveDownWithNoChildren() {
        Tree<Integer> myTree = new MyTree<>(1);
        Node<Integer> node2 = myTree.addNode(myTree.getRoot(), 2);

        myTree.addNode(myTree.getRoot(), 3);
        myTree.addNode(myTree.getRoot(), 4);

        Node<Integer> node5 = myTree.addNode(node2, 5);
        myTree.addNode(node2, 6);

        myTree.addNode(node5, 7);
        Node<Integer> node8 = myTree.addNode(node5, 8);
        myTree.addNode(node5, 11);

        myTree.addNode(node8, 9);
        myTree.addNode(node8, 10);

        System.out.println(myTree);

        Queue<NodeEvent<Integer>> events = oldTree.diff(myTree);

        System.out.println(events);

        assertEvent(events.poll(), UPDATE, 1);
        assertEvent(events.poll(), UPDATE, 2);
        assertEvent(events.poll(), UPDATE, 3);
        assertEvent(events.poll(), UPDATE, 4);
        assertEvent(events.poll(), UPDATE, 5);
        assertEvent(events.poll(), UPDATE, 6);

        assertEvent(events.poll(), MOVE, 7);
        assertEvent(events.poll(), MOVE, 8);
        assertEvent(events.poll(), MOVE, 9);
        assertEvent(events.poll(), MOVE, 10);
        assertEvent(events.poll(), CREATE, 11);
    }

    @Test
    public void testDiffNewTreeDeeperMoveDownWithChildren() {
        Tree<Integer> myTree = new MyTree<>(1);
        Node<Integer> node2 = myTree.addNode(myTree.getRoot(), 2);
        myTree.addNode(myTree.getRoot(), 4);

        Node<Integer> node5 = myTree.addNode(node2, 5);
        myTree.addNode(node2, 6);

        myTree.addNode(node5, 7);
        Node<Integer> node8 = myTree.addNode(node5, 8);
        myTree.addNode(node5, 11);

        myTree.addNode(node8, 3);
        myTree.addNode(node8, 9);
        myTree.addNode(node8, 10);

        System.out.println(myTree);

        Queue<NodeEvent<Integer>> events = oldTree.diff(myTree);

        System.out.println(events);

        assertEvent(events.poll(), UPDATE, 1);
        assertEvent(events.poll(), UPDATE, 2);
        assertEvent(events.poll(), UPDATE, 4);
        assertEvent(events.poll(), MOVE, 7);
        assertEvent(events.poll(), MOVE, 8);
        assertEvent(events.poll(), MOVE, 3);

        assertEvent(events.poll(), UPDATE, 5);
        assertEvent(events.poll(), UPDATE, 6);

        assertEvent(events.poll(), MOVE, 9);
        assertEvent(events.poll(), MOVE, 10);
        assertEvent(events.poll(), CREATE, 11);
    }

    @Test
    public void testDiffEqualDepthWithDifferentRoot() {

        Tree<Integer> newTree = new MyTree<>(21);
        Node<Integer> node2 = newTree.addNode(newTree.getRoot(), 2);
        Node<Integer> node3 = newTree.addNode(newTree.getRoot(), 3);
        Node<Integer> node4 = newTree.addNode(newTree.getRoot(), 4);

        newTree.addNode(node2, 5);
        newTree.addNode(node2, 6);

        newTree.addNode(node3, 7);
        newTree.addNode(node3, 8);

        newTree.addNode(node4, 9);
        newTree.addNode(node4, 10);

        System.out.println(newTree);

        Queue<NodeEvent<Integer>> events = oldTree.diff(newTree);

        System.out.println(events);

        assertEvent(events.poll(), CREATE, 21);

        //the parent of node 5 and 6 is not changed, so the following two actions should be ignored.
        assertEvent(events.poll(), MOVE, 5);
        assertEvent(events.poll(), MOVE, 6);

        assertEvent(events.poll(), MOVE, 2);

        assertEvent(events.poll(), MOVE, 7);
        assertEvent(events.poll(), MOVE, 8);
        assertEvent(events.poll(), MOVE, 3);

        assertEvent(events.poll(), MOVE, 9);
        assertEvent(events.poll(), MOVE, 10);
        assertEvent(events.poll(), MOVE, 4);

        assertEvent(events.poll(), DELETE, 1);

    }
}
