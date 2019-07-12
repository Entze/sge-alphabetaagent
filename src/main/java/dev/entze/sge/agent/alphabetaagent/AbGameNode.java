package dev.entze.sge.agent.alphabetaagent;

import dev.entze.sge.game.Game;
import dev.entze.sge.util.node.GameNode;
import java.util.HashMap;
import java.util.Map;

public class AbGameNode<A> implements GameNode<A> {

  private Game<A, ?> game;
  private double utility;
  private double heuristic;
  private int absoluteDepth;
  private boolean evaluated;

  private final Map<A, Integer> actionFrequency;

  public AbGameNode() {
    this(null,
        Double.NEGATIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        0);
  }


  public AbGameNode(Game<A, ?> game, A action, double[] weights, int absoluteDepth) {
    this(game.doAction(action), weights, absoluteDepth);
  }

  public AbGameNode(Game<A, ?> game, double[] weights, int absoluteDepth) {
    this(game,
        Double.NEGATIVE_INFINITY * (
            0 <= game.getCurrentPlayer() && game.getCurrentPlayer() < game.getNumberOfPlayers()
                ? weights[game.getCurrentPlayer()]
                : (-1)),
        Double.NEGATIVE_INFINITY * (
            0 <= game.getCurrentPlayer() && game.getCurrentPlayer() < game.getNumberOfPlayers()
                ? weights[game.getCurrentPlayer()]
                : (-1))
        , absoluteDepth);
  }

  public AbGameNode(Game<A, ?> game, double utility, double heuristic,
      int absoluteDepth) {
    this.game = game;
    this.utility = utility;
    this.heuristic = heuristic;
    this.absoluteDepth = absoluteDepth;
    this.evaluated = false;
    if (game != null && game.getCurrentPlayer() < 0) {
      actionFrequency = new HashMap<>();
    } else {
      actionFrequency = null;
    }
  }

  @Override
  public Game<A, ?> getGame() {
    return game;
  }

  @Override
  public void setGame(Game<A, ?> game) {
    this.game = game;
  }

  public double getUtility() {
    return utility;
  }

  public void setUtility(double utility) {
    this.utility = utility;
  }

  public double getHeuristic() {
    return heuristic;
  }

  public void setHeuristic(double heuristic) {
    this.heuristic = heuristic;
  }

  public int getAbsoluteDepth() {
    return absoluteDepth;
  }

  public void setAbsoluteDepth(int absoluteDepth) {
    this.absoluteDepth = absoluteDepth;
  }

  public boolean isEvaluated() {
    return evaluated;
  }

  public void setEvaluated(boolean evaluated) {
    this.evaluated = evaluated;
  }

  public void simulateDetermineAction() {
    A action = game.determineNextAction();
    if (action != null && actionFrequency != null) {
      actionFrequency.compute(action, (k, v) -> v == null ? 1 : v + 1);
    }
  }

  public void simulateDetermineAction(int times) {
    for (int i = 0; i < times; i++) {
      simulateDetermineAction();
    }
  }

  public boolean areSimulationDone() {
    return actionFrequency != null && !actionFrequency.isEmpty();
  }

  public int simulationsDone() {
    if (actionFrequency == null) {
      return 0;
    }
    return actionFrequency.values().stream().reduce(0, Integer::sum);
  }

  public double frequencyOf(A action) {
    if (actionFrequency == null) {
      return 0D;
    }
    double all = simulationsDone();
    double n = actionFrequency.getOrDefault(action, 0);

    return n / all;
  }

}
