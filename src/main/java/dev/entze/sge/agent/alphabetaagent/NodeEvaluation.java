package dev.entze.sge.agent.alphabetaagent;

import dev.entze.sge.game.Game;
import java.util.Objects;

public class NodeEvaluation implements Comparable<NodeEvaluation> {

  private final double utility;
  private final double heuristic;

  public NodeEvaluation(){
    this(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
  }

  public NodeEvaluation(Game<?, ?> game) {
    this(game.getUtilityValue(), game.getHeuristicValue());
  }

  public NodeEvaluation(double utility, double heuristic) {
    this.utility = utility;
    this.heuristic = heuristic;
  }

  public double getUtility() {
    return utility;
  }

  public double getHeuristic() {
    return heuristic;
  }

  @Override
  public int compareTo(NodeEvaluation o) {
    int compare = Double.compare(this.utility, o.utility);
    if (compare == 0) {
      return Double.compare(this.heuristic, o.heuristic);
    }
    return compare;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeEvaluation that = (NodeEvaluation) o;
    return Double.compare(that.utility, utility) == 0 &&
        Double.compare(that.heuristic, heuristic) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(utility, heuristic);
  }

  public static NodeEvaluation min(NodeEvaluation a, NodeEvaluation b) {
    int compare = a.compareTo(b);
    if (compare == 0) {
      if (a.heuristic > b.heuristic) {
        return b;
      }
      return a;
    } else if (compare > 0) {
      return b;
    }
    return a;
  }

  public static NodeEvaluation max(NodeEvaluation a, NodeEvaluation b) {
    int compare = a.compareTo(b);
    if (compare == 0) {
      if (a.heuristic < b.heuristic) {
        return b;
      }
      return a;
    } else if (compare < 0) {
      return b;
    }
    return a;
  }

  public static boolean lessThan(NodeEvaluation a, NodeEvaluation b) {
    return a.compareTo(b) < 0;
  }

  public static boolean lessThanOrEqual(NodeEvaluation a, NodeEvaluation b) {
    return a.compareTo(b) <= 0;
  }

  public static boolean moreThanOrEqual(NodeEvaluation a, NodeEvaluation b) {
    return a.compareTo(b) >= 0;
  }

  public static boolean moreThan(NodeEvaluation a, NodeEvaluation b) {
    return a.compareTo(b) > 0;
  }

}
