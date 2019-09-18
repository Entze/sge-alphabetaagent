package at.ac.tuwien.ifs.sge.agent.alphabetaagent;

import at.ac.tuwien.ifs.sge.agent.AbstractGameAgent;
import at.ac.tuwien.ifs.sge.agent.GameAgent;
import at.ac.tuwien.ifs.sge.engine.Logger;
import at.ac.tuwien.ifs.sge.game.Game;
import at.ac.tuwien.ifs.sge.util.Util;
import at.ac.tuwien.ifs.sge.util.tree.DoubleLinkedTree;
import at.ac.tuwien.ifs.sge.util.tree.Tree;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AlphaBetaAgent<G extends Game<A, ?>, A> extends AbstractGameAgent<G, A> implements
    GameAgent<G, A> {

  private static int INSTANCE_NR_COUNTER = 1;

  private final int instanceNr;

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

    abTree = new DoubleLinkedTree<>();

    this.instanceNr = INSTANCE_NR_COUNTER++;
  }

  @Override
  public void setUp(int numberOfPlayers, int playerId) {
    super.setUp(numberOfPlayers, playerId);

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

    log.tra_("Searching for root of tree");
    boolean foundRoot = Util.findRoot(abTree, game);
    if (foundRoot) {
      log._trace(", done.");
    } else {
      log._trace(", failed.");
    }

    log.tra_("Check if best move will eventually end game: ");
    if (sortPromisingCandidates(abTree, gameAbNodeComparator.reversed())) {
      log._trace("Yes");
      return Collections.max(abTree.getChildren(), gameAbTreeComparator).getNode().getGame()
          .getPreviousAction();
    }
    log._trace("No");

    lastDepth = 1;
    excessTime = 2;

    int labeled = 1;
    log.deb_("Labeling tree 1 time");
    while (!shouldStopComputation() && (excessTime > 1) && labeled <= lastDepth) {
      depth = determineDepth();
      if (labeled > 1) {
        log._deb_("\r");
        log.deb_("Labeling tree " + labeled + " times");
      }
      log._deb_(" at depth " + depth);
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
    log._debugf(", done with %d alpha cut-off%s, %d beta cut-off%s and %s left.",
        alphaCutOffs, alphaCutOffs != 1 ? "s" : "",
        betaCutOffs, betaCutOffs != 1 ? "s" : "",
        Util.convertUnitToReadableString(ACTUAL_TIMEOUT - (System.nanoTime() - START_TIME),
            TimeUnit.NANOSECONDS, timeUnit));

    log.tracef("Tree has %d nodes, maximum depth %d, and an average branching factor of %s",
        abTree.size(), depth, Util.convertDoubleToMinimalString(averageBranching, 2));

    if (abTree.isLeaf()) {
      log.debug("Could not find a move, choosing the next best greedy option.");
      return Collections.max(game.getPossibleActions(),
          (o1, o2) -> gameComparator.compare(game.doAction(o1), game.doAction(o2)));
    }

    if (!abTree.getNode().isEvaluated()) {
      labelMinMaxTree(abTree, 1);
    }

    log.debugf("Utility: %.1f, Heuristic: %.1f",
        abTree.getNode().getUtility(), abTree.getNode().getHeuristic());

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

  private boolean appearsQuiet(Tree<AbGameNode<A>> tree) {
    if (tree.isRoot()) {
      return true;
    }

    List<Tree<AbGameNode<A>>> siblings = tree.getParent().getChildren();

    double min = Collections.min(siblings, gameAbTreeComparator).getNode().getUtility();
    double max = Collections.max(siblings, gameAbTreeComparator).getNode().getUtility();

    return siblings.size() <= 2 || (min < tree.getNode().getGame().getUtilityValue()
        && tree.getNode().getGame().getUtilityValue() < max);
  }

  private void quiescence(Tree<AbGameNode<A>> tree) {

    Tree<AbGameNode<A>> originalTree = tree;

    boolean isQuiet = false;
    AbGameNode<A> node = tree.getNode();
    while (!node.isEvaluated()) {
      Game<A, ?> game = node.getGame();
      if (game.isGameOver() || (game.getCurrentPlayer() >= 0 && (isQuiet || appearsQuiet(tree)))) {
        node.setUtility(game.getUtilityValue(minMaxWeights));
        node.setHeuristic(game.getHeuristicValue(minMaxWeights));
        node.setEvaluated(true);
      } else {
        expandNode(tree);
        tree.sort(gameAbNodeComparator);
        tree = tree.getChild(tree.getChildren().size() / 2);
        isQuiet = true;
      }
      node = tree.getNode();
    }

    AbGameNode<A> originalNode = originalTree.getNode();
    if (!originalNode.isEvaluated()) {
      originalNode.setUtility(node.getUtility());
      originalNode.setHeuristic(node.getHeuristic());
      originalNode.setEvaluated(true);
    }

  }

  private void evaluateNode(Tree<AbGameNode<A>> tree) {
    AbGameNode<A> node = tree.getNode();
    if (tree.isLeaf()) {
      quiescence(tree);
    }

    if (!tree.isRoot()) {
      AbGameNode<A> parent = tree.getParent().getNode();
      int parentCurrentPlayer = parent.getGame().getCurrentPlayer();
      double utility = node.getUtility();
      double heuristic = node.getHeuristic();
      double parentUtility;
      double parentHeuristic;

      if (!parent.isEvaluated()) {
        parent.setUtility(utility);
        parent.setHeuristic(heuristic);
      } else if (parentCurrentPlayer < 0) {
        int nrOfSiblings = tree.getParent().getChildren().size();
        if (!parent.areSimulationDone()) {
          parent.simulateDetermineAction(
              Math.max((int) Math.round(nrOfSiblings * simulationTimeFactor()), nrOfSiblings));
        }
        parent.simulateDetermineAction(nrOfSiblings);
        if (parent.isMostFrequentAction(node.getGame().getPreviousAction())) {
          parent.setUtility(utility);
          parent.setHeuristic(heuristic);
        }
      } else {
        parentUtility = parent.getUtility();
        parentHeuristic = parent.getHeuristic();
        if (parentCurrentPlayer == playerId) {
          parent.setUtility(Math.max(parentUtility, utility));
          parent.setHeuristic(Math.max(parentHeuristic, heuristic));
        } else {
          parent.setUtility(Math.min(parentUtility, utility));
          parent.setHeuristic(Math.min(parentHeuristic, heuristic));
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
    if (tree.getNode().getGame().getCurrentPlayer() == playerId) {
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
            || tree.getParent().getNode().getGame().getCurrentPlayer() == playerId) {
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
      } else if ((utilityAlpha < utilityBeta && heuristicAlpha < heuristicBeta) || (
          tree.getParent() != null
              && tree.getParent().getNode().getGame().getCurrentPlayer() < 0)) {
        pushChildrenOntoStack(tree, stack);
        utilityAlphas.push(utilityAlpha);
        heuristicAlphas.push(heuristicAlpha);
        utilityBetas.push(utilityBeta);
        heuristicBetas.push(heuristicBeta);
      } else if (tree.getParent() != null
          && tree.getParent().getNode().getGame().getCurrentPlayer() >= 0) {
        if (tree.isRoot()
            || tree.getParent().getNode().getGame().getCurrentPlayer() == playerId) {
          betaCutOffs++;
        } else {
          alphaCutOffs++;
        }
        tree.getNode().setEvaluated(false);
        tree.dropChildren();
        stack.pop();
      }
    }

  }

  /**
   * Logarithmic fit {{1,10},{60, 100}}.
   *
   * @return a factor determining how many simulations can be done.
   */
  private double simulationTimeFactor() {
    return 21.9815D * Math.log(1.57606D * TimeUnit.NANOSECONDS.toSeconds(nanosLeft()));
  }

  /**
   * Exponential fit {{4,10},{40,2}}.
   *
   * @return a factor depending on the average branching factor determining how deep the minmax
   * search can go.
   */
  private double branchingFactor() {
    return 11.9581D * Math.exp(-0.0447066D * averageBranching);
  }

  /**
   * Logarithmic fit {{10, 0.5}, {40,1}}
   *
   * @return a factor depending on the nanos left determining how deep the minmax search can go.
   */
  private double timeFactor() {
    return 0.360674D * Math.log(0.4D * TimeUnit.NANOSECONDS.toSeconds(nanosLeft()));
  }

  /**
   * Logarithmic fit {{2, 1}, {12, 3}}
   *
   * @return a factor depending on how fast the previous calculation went.
   */
  private double excessTimeBonus() {
    return 1.11622D * Math.log(1.22474D * excessTime);
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

    boolean isDetermined = true;
    while (!tree.isLeaf() && tree.getNode().isEvaluated() && isDetermined) {
      isDetermined = isDetermined && tree.getChildren().stream()
          .allMatch(c -> c.getNode().getGame().getCurrentPlayer() >= 0);
      if (tree.getNode().getGame().getCurrentPlayer() == playerId) {
        tree.sort(gameAbNodeEvaluatedComparator.reversed().thenComparing(comparator));
      } else {
        tree.sort(gameAbNodeEvaluatedComparator.reversed().thenComparing(comparator.reversed()));
      }
      tree = tree.getChild(0);
    }

    return tree.getNode().isEvaluated() && tree.getNode().getGame().isGameOver();

  }

  @Override
  public String toString() {
    if (instanceNr > 1 || AlphaBetaAgent.INSTANCE_NR_COUNTER > 2) {
      return String.format("%s%d", "AlphaBetaAgent#", instanceNr);
    }
    return "AlphaBetaAgent";
  }
}
