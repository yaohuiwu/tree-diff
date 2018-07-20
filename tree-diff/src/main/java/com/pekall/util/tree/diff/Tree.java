package com.pekall.util.tree.diff;

import java.util.List;
import java.util.Queue;

public interface Tree<T> {

    /**
     * 查询根节点.
     *
     * @return 根节点.
     */
    Node<T> getRoot();

    /**
     * 查询树的深度。
     * @return 树的深度
     */
    int getDeep();

    /**
     * 查询指定层级的节点.
     *
     * @param level 层号，从1开始，最大值为深度.
     * @return 指定层级节点列表
     */
    List<Node<T>> getByLevel(int level);

    /**
     * 添加节点.
     *
     * @param parent 父节点
     * @param key 节点key
     * @return 返回添加的节点
     */
    Node<T> addNode(Node<T> parent, T key);

    /**
     * 先序遍历
     *
     * @param fromNode 初始节点
     * @param visitor 访问器
     */
    void preOrderTraverse(Node<T> fromNode, Visitor<T> visitor);

    /**
     * 深度优先先序遍历
     *
     * @param fromNode 初始节点
     * @param visitor 访问器
     */
    void postOrderTraverse(Node<T> fromNode, Visitor<T> visitor);

    /**
     * 层序遍历.
     *
     * @param fromNode 初始节点
     * @param visitor 访问器
     */
    void levelTraverse(Node<T> fromNode, Visitor<T> visitor);

    /**
     * 层序遍历. 可以通过isContinue控制是否继续遍历.
     *
     * @param fromNode 初始节点
     * @param visitor 访问器
     */
    void levelStopTraverse(Node<T> fromNode, StopVisitor<T> visitor);

    /**
     * 比较与另一棵树的不同.
     * <p>
     *     按照从层级从低到高的顺序比较两棵树:
     *     <ol>
     *         <li>共有层</li>
     *              <ul>
     *                  <li>对于每一层，比较出新增，更新，删除的节点</li>
     *                  <li>对于旧树，已删除的节点不参与比较</li>
     *                  <li>对于删除节点，按后序遍历，对每个节点都要生成删除事件，并标记为已删除节点</li>
     *              </ul>
     *         <li>自有层</li>
     *              <ul>
     *                  <li>旧树较深</li>
     *                      <ol>
     *                          <li>对共有层+1层的所有节点，按后序遍历，对每个节点都要生成删除事件，并标记为已删除节点</li>
     *                      </ol>
     *                  <li>新树较深</li>
     *                      <ol>
     *                          <li>
     *                              对共有层+1层的所有节点，按先序遍历，对每个节点都要生成新增事件，并标记为已增加节点
     *                          </li>
     *                      </ol>
     *              </ul>
     *     </ol>
     * </p>
     *
     * @param other 另一棵树，新树
     * @return  nodeEvents 节点事件，通过增，删，改，移动变成另一棵树的步骤.
     */
    Queue<NodeEvent<T>> diff(Tree<T> other);

}
