package dev.entze.sge.agent.alphabetaagent;

import dev.entze.sge.game.Game;
import dev.entze.sge.util.node.GameNode;

public class AbGameNode<A> implements GameNode<A> {

  private Game<A, ?> game;
  private double utility;
  private double heuristic;
  private int absoluteDepth;
  private boolean evaluated;

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
}
