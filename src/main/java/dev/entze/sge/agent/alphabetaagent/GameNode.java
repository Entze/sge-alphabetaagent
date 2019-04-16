package dev.entze.sge.agent.alphabetaagent;

import dev.entze.sge.game.Game;

public class GameNode<G extends Game<A, ?>, A> implements Comparable<GameNode> {

  private Game<A, ?> game;
  private double utility;
  private double heuristic;

  public GameNode(Game<A, ?> game) {
    this(game, game.getUtilityValue(), game.getHeuristicValue());
  }

  public GameNode(Game<A, ?> game, double utility, double heuristic) {
    this.game = game;
    this.utility = utility;
    this.heuristic = heuristic;
  }

  public Game<A, ?> getGame() {
    return game;
  }

  public GameNode<G, A> setGame(Game<A, ?> game) {
    this.game = game;
    return this;
  }

  public double getUtility() {
    return utility;
  }

  public GameNode<G, A> setUtility(double utility) {
    this.utility = utility;
    return this;
  }

  public double getHeuristic() {
    return heuristic;
  }

  public GameNode<G, A> setHeuristic(double heuristic) {
    this.heuristic = heuristic;
    return this;
  }

  @Override
  public int compareTo(GameNode o) {
    return Double.compare(this.utility, o.utility) << 1 + Double
        .compare(this.heuristic, o.heuristic);
  }
}
