package dev.entze.sge.agent.alphabetaagent;

import dev.entze.sge.agent.AbstractGameAgent;
import dev.entze.sge.agent.GameAgent;
import dev.entze.sge.game.Game;
import dev.entze.sge.util.Util;
import dev.entze.sge.util.tree.DoubleLinkedTree;
import dev.entze.sge.util.tree.Tree;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AlphaBetaAgent<G extends Game<A, ?>, A> extends AbstractGameAgent<G, A> implements
    GameAgent<G, A> {

  private final int maxDepth;
  private int depth;

  private Comparator<AbGameNode<A>> gameAbNodeUtilityComparator;
  private Comparator<AbGameNode<A>> gameAbNodeHeuristicComparator;
  private Comparator<AbGameNode<A>> gameAbNodeComparator;
  private Comparator<AbGameNode<A>> gameAbNodeMoveComparator;

  private Comparator<Tree<AbGameNode<A>>> gameAbTreeComparator;

  public AlphaBetaAgent() {
    this(64, 10);
  }

  public AlphaBetaAgent(int maxDepth, int depth) {
    super();
    this.maxDepth = maxDepth;

    this.depth = depth;
    abTree = new DoubleLinkedTree<>();

    gameAbNodeUtilityComparator = Comparator.comparingDouble(AbGameNode::getUtility);
    gameAbNodeHeuristicComparator = Comparator.comparingDouble(AbGameNode::getHeuristic);
    gameAbNodeComparator = gameAbNodeUtilityComparator.thenComparing(gameAbNodeHeuristicComparator);
    gameAbNodeMoveComparator = gameAbNodeComparator
        .thenComparing((o1, o2) -> gameComparator.compare(o1.getGame(), o2.getGame()));

    gameAbTreeComparator = (o1, o2) -> gameAbNodeMoveComparator.compare(o1.getNode(), o2.getNode());

  }

  private Tree<AbGameNode<A>> abTree;

  @Override
  public void setUp(int numberOfPlayers, int playerNumber) {
    super.setUp(numberOfPlayers, playerNumber);

    abTree.clear();
    abTree.setNode(new AbGameNode<>());

  }

  @Override
  public A computeNextAction(G game, long computationTime, TimeUnit timeUnit) {

    super.computeNextAction(game, computationTime, timeUnit);

    Util.findRoot(abTree, game);

    if (sortPromisingCandidates(abTree, gameAbNodeComparator.reversed())) {
      return abTree.getChild(0).getNode().getGame().getPreviousAction();
    }

    //while (!shouldStopComputation()) {
    labelAlphaBetaTree(abTree, determineDepth());
    //}

    if (abTree.isLeaf()) {
      return Collections.max(game.getPossibleActions(),
          (o1, o2) -> gameComparator.compare(game.doAction(o1), game.doAction(o2)));
    }

    return Collections.max(abTree.getChildren(), gameAbTreeComparator).getNode().getGame()
        .getPreviousAction();
  }

  private boolean expandNode(Tree<AbGameNode<A>> tree) {
    if (tree.isLeaf()) {
      AbGameNode<A> abGameNode = tree.getNode();
      Game<A, ?> game = abGameNode.getGame();
      Set<A> possibleActions = game.getPossibleActions();
      for (A possibleAction : possibleActions) {
        tree.add(new AbGameNode<>(game, possibleAction, minMaxWeights, abGameNode.getUtilityAlpha(),
            abGameNode.getUtilityBeta(), abGameNode.getHeuristicAlpha(),
            abGameNode.getHeuristicBeta(), abGameNode.getAbsoluteDepth() + 1));
      }
    }
    return !tree.isLeaf();
  }

  private void evaluateNode(Tree<AbGameNode<A>> tree) {
    AbGameNode<A> node = tree.getNode();
    if (tree.isLeaf()) {
      Game<A, ?> game = node.getGame();
      node.setUtility(game.getUtilityValue(minMaxWeights));
      node.setHeuristic(game.getHeuristicValue(minMaxWeights));
    }
    if (!tree.isRoot()) {
      AbGameNode<A> parent = tree.getParent().getNode();
      Game<A, ?> parentGame = parent.getGame();
      if (parentGame.getCurrentPlayer() == playerNumber) {
        parent.setUtility(Math.max(parent.getUtility(), node.getUtility()));
        parent.setHeuristic(Math.max(parent.getHeuristic(), node.getHeuristic()));
        parent.setUtilityAlpha(Math.max(parent.getUtility(), parent.getUtilityAlpha()));
        parent.setHeuristicAlpha(Math.max(parent.getHeuristic(), parent.getHeuristicAlpha()));
      } else {
        parent.setUtility(Math.min(parent.getUtility(), node.getUtility()));
        parent.setHeuristic(Math.min(parent.getHeuristic(), node.getHeuristic()));
        parent.setUtilityBeta(Math.min(parent.getUtility(), parent.getUtilityBeta()));
        parent.setHeuristicBeta(Math.min(parent.getHeuristic(), parent.getHeuristicBeta()));
      }
    }
  }

  private void labelAlphaBetaTree(Tree<AbGameNode<A>> tree, int depth) {

    Deque<Tree<AbGameNode<A>>> stack = new ArrayDeque<>();
    stack.push(tree);

    depth = Math.max(tree.getNode().getAbsoluteDepth() + depth, depth);

    Tree<AbGameNode<A>> lastParent = null;

    int checkDepth = 0;
    while (!stack.isEmpty() && (checkDepth++ % 31 != 0 || !shouldStopComputation())) {

      tree = stack.peek();

      if (!cutOff(tree)) {
        if (tree.getNode().getAbsoluteDepth() >= depth || !expandNode(tree) || lastParent == tree) {
          evaluateNode(tree);
          stack.pop();
          lastParent = tree.getParent();
        } else {
          tree.sort(gameAbNodeMoveComparator);
          for (Tree<AbGameNode<A>> child : tree.getChildren()) {
            stack.push(child);
          }
        }

      }

    }

  }

  private boolean cutOff(Tree<AbGameNode<A>> tree) {
    if (tree == null) {
      return true;
    }

    if (!tree.isRoot()) {
      AbGameNode<A> parentNode = tree.getParent().getNode();
      return parentNode.getUtilityAlpha() >= parentNode.getUtilityBeta()
          || parentNode.getHeuristicAlpha() >= parentNode.getHeuristicBeta();
    }
    return false;
  }

  private int determineDepth() {
    return Math.min(depth, maxDepth);
  }

  private boolean sortPromisingCandidates(Tree<AbGameNode<A>> tree,
      Comparator<AbGameNode<A>> comparator) {

    while (!tree.isLeaf()) {
      tree.sort(comparator);
      tree = tree.getChild(0);
    }

    return tree.getNode().getGame().isGameOver();

  }

}
