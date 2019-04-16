package dev.entze.sge.agent.alphabetaagent;

import dev.entze.sge.agent.GameAgent;
import dev.entze.sge.game.Game;
import dev.entze.sge.util.pair.MutablePair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AlphaBetaAgent<G extends Game<A, ?>, A> implements GameAgent<G, A> {

  public final long STOP_SEARCH_TIME_MULTIPLIER = 50L;
  public final long STOP_SEARCH_TIME_DIVISOR = 100L;

  public final long RESTART_SEARCH_MULTIPLIER = STOP_SEARCH_TIME_MULTIPLIER / 2;
  public final long RESTART_SEARCH_DIVISOR = 100L;
  private final Random random;
  private final Comparator<Game<A, ?>> gameUtilityComparator = Comparator
      .comparingDouble(o -> o.getUtilityValue());
  private final Comparator<Game<A, ?>> gameHeuristicComparator = Comparator
      .comparingDouble(o -> o.getHeuristicValue());
  private final Comparator<Game<A, ?>> gameComparator = gameUtilityComparator.reversed()
      .thenComparing(gameHeuristicComparator.reversed());
  int[] stats;
  private long startingTimeInNano;
  private long stopSearchTimeInNano;
  private double[] evaluationWeights;
  private int playerNumber;
  private int depth;

  public AlphaBetaAgent() {
    stats = new int[64];
    random = new Random();
  }

  @Override
  public void setUp(int numberOfPlayers, int playerNumber) {
    evaluationWeights = new double[numberOfPlayers];
    Arrays.fill(evaluationWeights, (-1D));
    evaluationWeights[playerNumber] = 1;
    this.playerNumber = playerNumber;
    depth = 10;
  }

  @Override
  public void tearDown() {
  }

  @Override
  public A computeNextAction(G game, long computationTime, TimeUnit timeUnit) {
    startingTimeInNano = System.nanoTime();
    long computationTimeInNano = timeUnit.toNanos(computationTime);
    stopSearchTimeInNano = startingTimeInNano + Math
        .max(computationTimeInNano * STOP_SEARCH_TIME_DIVISOR / STOP_SEARCH_TIME_DIVISOR
            - 1000L, 0L);

    Set<A> possibleActions = game.getPossibleActions();

    List<MutablePair<Game<A, ?>, NodeEvaluation>> childMap = new ArrayList<>(
        possibleActions.size());

    NodeEvaluation alpha = NodeEvaluation.NEGATIVE_INFINITY;
    NodeEvaluation beta = NodeEvaluation.POSITIVE_INFINITY;

    for (A possibleAction : possibleActions) {
      Game<A, ?> child = game.doAction(possibleAction);
      childMap.add(new MutablePair<>(child, new NodeEvaluation(child, evaluationWeights)));
    }

    childMap.sort((o1, o2) -> o2.getB().compareTo(o1.getB()));

    int round = 0;
    int lastDepth = 1;
    while ((computationTimeInNano * RESTART_SEARCH_DIVISOR) / RESTART_SEARCH_MULTIPLIER
        > currentComputationTime() && round < lastDepth && !isStopSearchTime()) {
      depth = determineDepth(stats);
      if (depth < 2) {
        depth = (int) timeUnit.toSeconds(computationTime) / childMap.size();
      }
      depth = Math.min(Math.max(depth, lastDepth + 1), stats.length - 1);
      for (MutablePair<Game<A, ?>, NodeEvaluation> child : childMap) {
        NodeEvaluation evaluation = alphaBeta(child.getA(), depth - 1, alpha, beta);
        if (!isStopSearchTime()) {
          child.setB(evaluation);
          if (NodeEvaluation.moreThanOrEqual(evaluation, alpha)) {
            alpha = evaluation;
          }
        }
      }

      if (isStopSearchTime()) {
        stats[depth - 1]--;
      } else {
        stats[depth]++;
      }
      lastDepth = depth;
      round++;
      childMap.sort((o1, o2) -> o2.getB().compareTo(o1.getB()));
      alpha = childMap.get(0).getB();
    }

    return childMap.get(0).getA().getPreviousAction();
  }


  public NodeEvaluation alphaBeta(Game<A, ?> game, int depth, NodeEvaluation alpha,
      NodeEvaluation beta) {
    if (depth <= 0 || game.isGameOver() || isStopSearchTime()) {
      return new NodeEvaluation(game, evaluationWeights);
    }

    int currentPlayer = game.getCurrentPlayer();
    NodeEvaluation value = (currentPlayer == playerNumber ? NodeEvaluation.NEGATIVE_INFINITY
        : NodeEvaluation.POSITIVE_INFINITY);
    List<Game<A, ?>> children = getChildren(game);

    for (Game<A, ?> child : children) {
      if (currentPlayer == playerNumber) { //check if is maximizing
        value = NodeEvaluation.max(value, alphaBeta(child, depth - 1, alpha, beta));
        alpha = NodeEvaluation.max(alpha, value);
      } else {
        value = NodeEvaluation.min(value, alphaBeta(child, depth - 1, alpha, beta));
        beta = NodeEvaluation.min(beta, value);
      }
      if (NodeEvaluation.moreThanOrEqual(alpha, beta)) {
        break;
      }
    }

    return value;
  }

  public List<Game<A, ?>> getChildren(Game<A, ?> parent) {
    Set<A> possibleActions = parent.getPossibleActions();
    List<Game<A, ?>> children = new ArrayList<>(possibleActions.size());

    for (A possibleAction : possibleActions) {
      children.add(parent.doAction(possibleAction));
    }

    children.sort(gameComparator);

    return children;
  }

  private int determineDepth(int[] stats) {
    int max = Integer.MIN_VALUE;
    int maxIndex = (-1);
    for (int i = 0; i < stats.length; i++) {
      if (stats[i] > max) {
        maxIndex = i;
        max = stats[i];
      }
    }
    return maxIndex + random.nextInt(2);
  }

  private boolean isStopSearchTime() {
    return System.nanoTime() >= stopSearchTimeInNano || !Thread.currentThread().isAlive() || Thread
        .currentThread().isInterrupted();
  }

  private long currentComputationTime() {
    return System.nanoTime() - startingTimeInNano;
  }

}
