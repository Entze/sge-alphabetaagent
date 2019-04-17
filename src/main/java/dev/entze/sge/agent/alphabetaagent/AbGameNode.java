package dev.entze.sge.agent.alphabetaagent;

import dev.entze.sge.game.Game;
import dev.entze.sge.util.node.GameNode;
import java.util.Objects;

public class AbGameNode<A> implements GameNode<A> {

  private Game<A, ?> game;
  private double utility;
  private double heuristic;
  private double utilityAlpha;
  private double utilityBeta;
  private double heuristicAlpha;
  private double heuristicBeta;
  private int absoluteDepth;

  public AbGameNode(double[] weights) {
    this(null, weights, 0);
  }


  public AbGameNode(Game<A, ?> game, A action, double[] weights, int absoluteDepth) {
    this(game.doAction(action), weights, absoluteDepth);
  }

  public AbGameNode(Game<A, ?> game, double[] weights, int absoluteDepth) {
    this(game,
        Double.NEGATIVE_INFINITY * weights[game.getCurrentPlayer()],
        Double.NEGATIVE_INFINITY * weights[game.getCurrentPlayer()],
        Double.NEGATIVE_INFINITY * weights[game.getCurrentPlayer()],
        Double.NEGATIVE_INFINITY * weights[game.getCurrentPlayer()],
        Double.NEGATIVE_INFINITY * weights[game.getCurrentPlayer()],
        Double.NEGATIVE_INFINITY * weights[game.getCurrentPlayer()], absoluteDepth);
  }

  public AbGameNode(Game<A, ?> game, A action, double[] weights, double utilityAlpha,
      double utilityBeta,
      double heuristicAlpha, double heuristicBeta, int absoluteDepth) {
    this(game.doAction(action), weights, utilityAlpha, utilityBeta, heuristicAlpha, heuristicBeta,
        absoluteDepth);
  }

  public AbGameNode(Game<A, ?> game, double[] weights, double utilityAlpha, double utilityBeta,
      double heuristicAlpha, double heuristicBeta, int absoluteDepth) {
    this(game,
        Double.NEGATIVE_INFINITY * weights[game.getCurrentPlayer()],
        Double.NEGATIVE_INFINITY * weights[game.getCurrentPlayer()],
        utilityAlpha,
        utilityBeta,
        heuristicAlpha,
        heuristicBeta,
        absoluteDepth
    );
  }

  public AbGameNode(Game<A, ?> game, double utility, double heuristic, double utilityAlpha,
      double utilityBeta, double heuristicAlpha, double heuristicBeta, int absoluteDepth) {
    this.game = game;
    this.utility = utility;
    this.heuristic = heuristic;
    this.utilityAlpha = utilityAlpha;
    this.utilityBeta = utilityBeta;
    this.heuristicAlpha = heuristicAlpha;
    this.heuristicBeta = heuristicBeta;
    this.absoluteDepth = absoluteDepth;
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

  public double getUtilityAlpha() {
    return utilityAlpha;
  }

  public void setUtilityAlpha(double utilityAlpha) {
    this.utilityAlpha = utilityAlpha;
  }

  public double getUtilityBeta() {
    return utilityBeta;
  }

  public void setUtilityBeta(double utilityBeta) {
    this.utilityBeta = utilityBeta;
  }

  public double getHeuristicAlpha() {
    return heuristicAlpha;
  }

  public void setHeuristicAlpha(double heuristicAlpha) {
    this.heuristicAlpha = heuristicAlpha;
  }

  public double getHeuristicBeta() {
    return heuristicBeta;
  }

  public void setHeuristicBeta(double heuristicBeta) {
    this.heuristicBeta = heuristicBeta;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbGameNode<?> that = (AbGameNode<?>) o;
    return Double.compare(that.utility, utility) == 0 &&
        Double.compare(that.heuristic, heuristic) == 0 &&
        Double.compare(that.utilityAlpha, utilityAlpha) == 0 &&
        Double.compare(that.utilityBeta, utilityBeta) == 0 &&
        Double.compare(that.heuristicAlpha, heuristicAlpha) == 0 &&
        Double.compare(that.heuristicBeta, heuristicBeta) == 0 &&
        Objects.equals(game, that.game);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(game, utility, heuristic, utilityAlpha, utilityBeta, heuristicAlpha, heuristicBeta);
  }

  public int getAbsoluteDepth() {
    return absoluteDepth;
  }

  public void setAbsoluteDepth(int absoluteDepth) {
    this.absoluteDepth = absoluteDepth;
  }
}
