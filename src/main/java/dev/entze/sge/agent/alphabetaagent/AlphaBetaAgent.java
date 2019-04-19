package dev.entze.sge.agent.alphabetaagent;

import dev.entze.sge.agent.AbstractGameAgent;
import dev.entze.sge.agent.GameAgent;
import dev.entze.sge.engine.Logger;
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
  private int lastDepth;
  private int depth;

  private Comparator<AbGameNode<A>> gameAbNodeUtilityComparator;
  private Comparator<AbGameNode<A>> gameAbNodeHeuristicComparator;
  private Comparator<AbGameNode<A>> gameAbNodeEvaluatedComparator;
  private Comparator<AbGameNode<A>> gameAbNodeComparator;
  private Comparator<AbGameNode<A>> gameAbNodeMoveComparator;

  private Comparator<Tree<AbGameNode<A>>> gameAbTreeComparator;
  private Tree<AbGameNode<A>> abTree;
  private int alphaCutOffs;
  private int betaCutOffs;

  private int excessTime;
  private int averageBranchingCount;
  private double averageBranching;

  public AlphaBetaAgent() {
    this(64, null);
  }

  public AlphaBetaAgent(Logger log) {
    this(64, log);
  }

  public AlphaBetaAgent(int maxDepth, Logger log) {
    super(log);
    this.maxDepth = maxDepth;

    this.depth = depth;
    abTree = new DoubleLinkedTree<>();

  }

  @Override
  public void setUp(int numberOfPlayers, int playerNumber) {
    super.setUp(numberOfPlayers, playerNumber);

    abTree.clear();
    abTree.setNode(new AbGameNode<>());

    averageBranchingCount = 0;
    averageBranching = 10;

    gameAbNodeUtilityComparator = Comparator.comparingDouble(AbGameNode::getUtility);
    gameAbNodeHeuristicComparator = Comparator.comparingDouble(AbGameNode::getHeuristic);
    gameAbNodeEvaluatedComparator = (o1, o2) -> Boolean.compare(o1.isEvaluated(), o2.isEvaluated());
    gameAbNodeComparator = gameAbNodeUtilityComparator.thenComparing(gameAbNodeHeuristicComparator);
    gameAbNodeMoveComparator = gameAbNodeComparator
        .thenComparing((o1, o2) -> gameComparator.compare(o1.getGame(), o2.getGame()));

    gameAbTreeComparator = ((Comparator<Tree<AbGameNode<A>>>) (o1, o2) -> gameAbNodeEvaluatedComparator
        .compare(o1.getNode(), o2.getNode()))
        .thenComparing(((o1, o2) -> gameAbNodeMoveComparator.compare(o1.getNode(), o2.getNode())));
  }

  @Override
  public A computeNextAction(G game, long computationTime, TimeUnit timeUnit) {

    super.setTimers(computationTime, timeUnit);

    log.tra("Searching for root of tree");
    Util.findRoot(abTree, game);
    log.trace_(", done.");

    log.tra("Check if best move will eventually end game: ");
    if (sortPromisingCandidates(abTree, gameAbNodeComparator.reversed())) {
      log.trace_("Yes");
      return Collections.max(abTree.getChildren(), gameAbTreeComparator).getNode().getGame()
          .getPreviousAction();
    }
    log.trace_("No");

    lastDepth = 1;
    excessTime = 2;

    int labeled = 1;
    log.deb("Labeling tree 1 time");
    while (!shouldStopComputation() && (excessTime > 1) && labeled <= lastDepth) {
      depth = determineDepth();
      if (labeled > 1) {
        log.deb_("\r");
        log.deb("Labeling tree " + labeled + " times");
      }
      log.deb_(" at depth " + depth);
      alphaCutOffs = 0;
      betaCutOffs = 0;
      labelAlphaBetaTree(abTree, depth,
          Double.NEGATIVE_INFINITY,
          Double.POSITIVE_INFINITY,
          Double.NEGATIVE_INFINITY,
          Double.POSITIVE_INFINITY);
      excessTime = (int) (TIMEOUT / Math.min(Math.max(System.nanoTime() - START_TIME, 1), TIMEOUT));
      labeled++;
    }
    log.debug_(
        String
            .format(", done with %d alpha cut-off%s, %d beta cut-off%s and %s left.",
                alphaCutOffs, alphaCutOffs != 1 ? "s" : "",
                betaCutOffs, betaCutOffs != 1 ? "s" : "",
                Util.convertUnitToReadableString(ACTUAL_TIMEOUT - (System.nanoTime() - START_TIME),
                    TimeUnit.NANOSECONDS, timeUnit)));

    if (abTree.isLeaf()) {
      log.debug("Could not find a move, choosing the next best greedy option.");
      return Collections.max(game.getPossibleActions(),
          (o1, o2) -> gameComparator.compare(game.doAction(o1), game.doAction(o2)));
    }

    if (!abTree.getNode().isEvaluated()) {
      labelMinMaxTree(abTree, 1);
    }

    log.debug(String.format("Utility: %.1f, Heuristic: %.1f with a tree size of %d.",
        abTree.getNode().getUtility(), abTree.getNode().getHeuristic(), abTree.size()));

    return Collections.max(abTree.getChildren(), gameAbTreeComparator).getNode().getGame()
        .getPreviousAction();
  }

  private boolean expandNode(Tree<AbGameNode<A>> tree) {
    if (tree.isLeaf()) {
      AbGameNode<A> abGameNode = tree.getNode();
      Game<A, ?> game = abGameNode.getGame();
      if (!game.isGameOver()) {
        Set<A> possibleActions = game.getPossibleActions();
        averageBranching = (averageBranching * averageBranchingCount++ + possibleActions.size())
            / averageBranchingCount;
        for (A possibleAction : possibleActions) {
          tree.add(new AbGameNode<>(game, possibleAction, minMaxWeights,
              abGameNode.getAbsoluteDepth() + 1));
        }
      }
    }
    return !tree.isLeaf();
  }

  private void evaluateNode(Tree<AbGameNode<A>> tree) {
    AbGameNode<A> node = tree.getNode();
    Game<A, ?> game = node.getGame();
    if (tree.isLeaf() || game.isGameOver()) {
      node.setUtility(game.getUtilityValue(minMaxWeights));
      node.setHeuristic(game.getHeuristicValue(minMaxWeights));
      node.setEvaluated(true);
    }

    if (!tree.isRoot()) {
      AbGameNode<A> parent = tree.getParent().getNode();
      if (parent.getGame().getCurrentPlayer() == playerNumber) {
        if (parent.isEvaluated()) {
          parent.setUtility(Math.max(parent.getUtility(), node.getUtility()));
          parent.setHeuristic(Math.max(parent.getHeuristic(), node.getHeuristic()));
        } else {
          parent.setUtility(node.getUtility());
          parent.setHeuristic(node.getHeuristic());
        }
      } else {
        if (parent.isEvaluated()) {
          parent.setUtility(Math.min(parent.getUtility(), node.getUtility()));
          parent.setHeuristic(Math.min(parent.getHeuristic(), node.getHeuristic()));
        } else {
          parent.setUtility(node.getUtility());
          parent.setHeuristic(node.getHeuristic());
        }
      }
      parent.setEvaluated(true);
    }
  }

  private void labelMinMaxTree(Tree<AbGameNode<A>> tree, int depth) {

    Deque<Tree<AbGameNode<A>>> stack = new ArrayDeque<>();
    stack.push(tree);

    Tree<AbGameNode<A>> lastParent = null;

    depth = Math.max(tree.getNode().getAbsoluteDepth() + depth, depth);

    int checkDepth = 0;

    while (!stack.isEmpty() && (checkDepth++ % 31 != 0 || !shouldStopComputation())) {

      tree = stack.peek();

      if (lastParent == tree || tree.getNode().getAbsoluteDepth() >= depth || !expandNode(tree)) {
        evaluateNode(tree);

        stack.pop();
        lastParent = tree.getParent();
      } else {

        pushChildrenOntoStack(tree, stack);

      }

    }

  }

  private void pushChildrenOntoStack
      (Tree<AbGameNode<A>> tree, Deque<Tree<AbGameNode<A>>> stack) {
    if (tree.getNode().getGame().getCurrentPlayer() == playerNumber) {
      tree.sort(gameAbNodeMoveComparator);
    } else {
      tree.sort(gameAbNodeMoveComparator.reversed());
    }

    for (Tree<AbGameNode<A>> child : tree.getChildren()) {
      stack.push(child);
    }
  }

  private void labelAlphaBetaTree(Tree<AbGameNode<A>> tree, int depth,
      double utilityAlpha, double utilityBeta,
      double heuristicAlpha, double heuristicBeta
  ) {

    Deque<Tree<AbGameNode<A>>> stack = new ArrayDeque<>();
    Deque<Double> utilityAlphas = new ArrayDeque<>();
    Deque<Double> heuristicAlphas = new ArrayDeque<>();
    Deque<Double> utilityBetas = new ArrayDeque<>();
    Deque<Double> heuristicBetas = new ArrayDeque<>();

    stack.push(tree);
    utilityAlphas.push(utilityAlpha);
    utilityBetas.push(utilityBeta);
    heuristicAlphas.push(heuristicAlpha);
    heuristicBetas.push(heuristicBeta);

    depth = Math.max(tree.getNode().getAbsoluteDepth() + depth, depth);

    Tree<AbGameNode<A>> lastParent = null;

    int checkDepth = 0;
    while (!stack.isEmpty() && (checkDepth++ % 31 != 0 || !shouldStopComputation())) {

      tree = stack.peek();

      if (lastParent == tree || tree.getNode().getAbsoluteDepth() >= depth || !expandNode(tree)) {
        evaluateNode(tree);

        if (tree.isRoot()
            || tree.getParent().getNode().getGame().getCurrentPlayer() == playerNumber) {
          utilityAlpha = Math.max(utilityAlphas.peek(), tree.getNode().getUtility());
          heuristicAlpha = Math.max(heuristicAlphas.peek(), tree.getNode().getHeuristic());
          utilityBeta = utilityBetas.peek();
          heuristicBeta = heuristicBetas.peek();
        } else {
          utilityAlpha = utilityAlphas.peek();
          heuristicAlpha = heuristicAlphas.peek();
          utilityBeta = Math.min(utilityBetas.peek(), tree.getNode().getUtility());
          heuristicBeta = Math.min(heuristicBetas.peek(), tree.getNode().getUtility());
        }

        stack.pop();
        if (lastParent == tree) {
          utilityAlphas.pop();
          utilityBetas.pop();
          heuristicAlphas.pop();
          heuristicBetas.pop();
        }

        lastParent = tree.getParent();
      } else if (utilityAlpha < utilityBeta && heuristicAlpha < heuristicBeta) {
        pushChildrenOntoStack(tree, stack);
        utilityAlphas.push(utilityAlpha);
        heuristicAlphas.push(heuristicAlpha);
        utilityBetas.push(utilityBeta);
        heuristicBetas.push(heuristicBeta);
      } else {
        if (tree.isRoot()
            || tree.getParent().getNode().getGame().getCurrentPlayer() == playerNumber) {
          betaCutOffs++;
        } else {
          alphaCutOffs++;
        }
        tree.getNode().setEvaluated(false); //??
        tree.dropChildren();
        stack.pop();
      }
    }

  }


  private double branchingFactor() {
    return 11.5022D * Math.exp(-0.0349878D * averageBranching);
  }

  private double timeFactor() {
    return 0.45512D * Math
        .log(0.3D * TimeUnit.NANOSECONDS.toSeconds(TIMEOUT - (System.nanoTime() - START_TIME)));
  }

  private double excessTimeBonus() {
    return 1.67433D * Math.log(0.90856D * excessTime);
  }

  private int determineDepth() {

    depth = (int) Math.max(Math.round(branchingFactor() * timeFactor()), 2);
    depth = Math.max(lastDepth + (int) Math.round(excessTimeBonus()), depth);
    depth = Math.min(depth, maxDepth);

    lastDepth = depth;

    return Math.min(depth, maxDepth);
  }

  private boolean sortPromisingCandidates(Tree<AbGameNode<A>> tree,
      Comparator<AbGameNode<A>> comparator) {

    while (!tree.isLeaf() && tree.getNode().isEvaluated()) {
      if (tree.getNode().getGame().getCurrentPlayer() == playerNumber) {
        tree.sort(gameAbNodeEvaluatedComparator.reversed().thenComparing(comparator));
      } else {
        tree.sort(gameAbNodeEvaluatedComparator.reversed().thenComparing(comparator.reversed()));
      }
      tree = tree.getChild(0);
    }

    return tree.getNode().isEvaluated() && tree.getNode().getGame().isGameOver();

  }

}
