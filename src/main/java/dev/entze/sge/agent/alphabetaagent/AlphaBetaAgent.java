package dev.entze.sge.agent.alphabetaagent;

import dev.entze.sge.agent.GameAgent;
import dev.entze.sge.game.Game;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

public class AlphaBetaAgent<G extends Game<A, ?>, A> implements GameAgent<G, A> {

  private long stopTimeInNano;

  private int lastPly;
  private int ply;


  private Map<Integer, Double> alphaBetaUtilityMap;
  private Map<Integer, Double> alphaBetaHeuristicMap;
  private Map<Integer, Long> previousComputationTime;

  private Comparator<Game<A, ?>> gameUtilityValueComparator = Comparator
      .comparingDouble(o -> o.getUtilityValue());

  private Comparator<Game<A, ?>> gameHeuristicValueComparator = Comparator
      .comparingDouble(o -> o.getHeuristicValue());

  private Comparator<Game<A, ?>> gameComparator = gameUtilityValueComparator.reversed()
      .thenComparing(gameHeuristicValueComparator.reversed());

  public AlphaBetaAgent() {
    alphaBetaUtilityMap = new HashMap<>();
    alphaBetaHeuristicMap = new HashMap<>();
    previousComputationTime = new HashMap<>();
  }

  @Override
  public void setUp(int numberOfPlayers) {
    lastPly = 0;
    ply = 0;
    for (int i = 0; i < numberOfPlayers; i++) {
      alphaBetaUtilityMap.put(0, Double.NEGATIVE_INFINITY);
    }
  }

  @Override
  public void tearDown() {
  }

  @Override
  public A computeNextAction(G game, long computationTime, TimeUnit timeUnit) {
    long startingTimeInNano = System.nanoTime();
    stopTimeInNano = startingTimeInNano + Math
        .max(timeUnit.toNanos((computationTime * 50L) / 100L) - 1000L, 0L);

    for (Integer player : alphaBetaUtilityMap.keySet()) {
      alphaBetaUtilityMap.put(player, Double.NEGATIVE_INFINITY);
      alphaBetaHeuristicMap.put(player, Double.NEGATIVE_INFINITY);
    }



    return null;
    /*
    List<ImmutablePair<Integer, A>> previousActions = game.getPreviousActions();
    ply = previousActions.size();

    boolean foundNextPly = true;
    boolean foundRoot = true;
    System.out.print("[AlphaBeta]: finding new root");
    if (gameTree.isEmpty() || gameTree.getChildren().isEmpty() || lastPly == ply) {
      gameTree.setNode(new GameNode<>(game));
    } else {
      for (int p = lastPly + 1; p < ply && foundRoot; p++) {
        foundRoot = foundRoot && foundNextPly;
        for (Tree<GameNode<G, A>> child : gameTree.getChildren()) {
          if (child.getNode().getGame().getPreviousAction().equals(previousActions.get(p))) {
            gameTree = child;
            System.out.print(".");
            foundNextPly = true;
            break;
          }
          foundNextPly = false;
        }
      }
    }

    if (!foundRoot) {
      System.out.print("\ncould not find root. Building new tree");
      gameTree.clear();
      gameTree.setNode(new GameNode<>(game));
      System.out.print(".");
    }
    System.out.println("done");


    GameNode<G, A> node = gameTree.getNode();



    gameTree.postTreeIterator().forEachRemaining(t -> {
      if (!t.isLeaf()) {
        t.getNode().setHeuristic(t.getChild(0).getNode().getHeuristic());
        t.getNode().setUtility(t.getChild(0).getNode().getUtility());
      }
      t.sort(gameTreeComparator);
    });

    lastPly = ply;
     */

  }

  private double alphaBeta(Game<A, ?> game, int depth,
      Map<Integer, Double> alphaBetaMap, ToDoubleFunction<Game<A, ?>> valueExtractor) {
    if (depth <= 0 || game.isGameOver()) {
      return valueExtractor.applyAsDouble(game);
    }

    double value = Double.NEGATIVE_INFINITY;
    Set<A> possibleActions = game.getPossibleActions();
    List<Game<A, ?>> children = new ArrayList<>(possibleActions.size());

    for (A possibleAction : possibleActions) {
      children.add(game.doAction(possibleAction));
    }
    children.sort(gameComparator);

    for (Game<A, ?> child : children) {
      value = Math.max(value, alphaBeta(child, depth - 1, alphaBetaMap, valueExtractor));

      if (alphaBetaMap.getOrDefault(child.getCurrentPlayer(), Double.NEGATIVE_INFINITY) < value) {
        alphaBetaMap.put(child.getCurrentPlayer(), value);
      }

      boolean cutOff = true;
      for (Integer player : alphaBetaMap.keySet()) {
        cutOff =
            cutOff && (alphaBetaMap.getOrDefault(child.getCurrentPlayer(), Double.NEGATIVE_INFINITY)
                >= (-alphaBetaMap.getOrDefault(player, Double.NEGATIVE_INFINITY)));
      }
      if (cutOff) {
        break;
      }

    }

    return value;
  }

}
