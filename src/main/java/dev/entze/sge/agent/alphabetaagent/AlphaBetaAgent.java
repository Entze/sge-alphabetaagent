package dev.entze.sge.agent.alphabetaagent;

import dev.entze.sge.agent.GameAgent;
import dev.entze.sge.game.Game;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class AlphaBetaAgent<G extends Game<? extends A, ?>, A> implements GameAgent<G, A> {

  private long stopSearchTimeInNano;

  private final Comparator<Game<A, ?>> gameUtilityComparator = Comparator
      .comparingDouble(o -> o.getUtilityValue());

  private final Comparator<Game<A, ?>> gameHeuristicComparator = Comparator
      .comparingDouble(o -> o.getHeuristicValue());

  private final Comparator<Game<A, ?>> gameComparator = gameUtilityComparator.reversed()
      .thenComparing(gameHeuristicComparator.reversed());

  public AlphaBetaAgent() {

  }

  @Override
  public void setUp(int numberOfPlayers) {
  }

  @Override
  public void tearDown() {
  }

  @Override
  public A computeNextAction(G game, long computationTime, TimeUnit timeUnit) {
    long startingTimeInNano = System.nanoTime();
    stopSearchTimeInNano = startingTimeInNano + Math
        .max(timeUnit.toNanos((computationTime * 50L) / 100L) - 1000L, 0L);


    return null;
  }


  //TODO: Recursive implementation -> don't care how many enemies
  public NodeEvaluation alphaBeta(Game<A, ?> game, int depth,
      Map<Integer, NodeEvaluation> alphaBetaMap) {
    if (depth <= 0 || game.isGameOver() || isStopSearchTime()) {
      return new NodeEvaluation(game);
    }

    int currentPlayer = game.getCurrentPlayer();
    NodeEvaluation value = new NodeEvaluation();
    List<Game<A, ?>> children = getChildren(game);
    Set<Integer> nextPlayerCandidates = new TreeSet<>();

    for (Game<A, ?> child : children) {
      nextPlayerCandidates.add(child.getCurrentPlayer());
    }

    nextPlayerCandidates.remove(currentPlayer);

    for (Game<A, ?> child : children) {
      value = NodeEvaluation.max(value, alphaBeta(child, depth - 1, alphaBetaMap));

      //alphaBetaMap.put(currentPlayer, )

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


  private boolean isStopSearchTime() {
    return System.nanoTime() >= stopSearchTimeInNano;
  }

}
