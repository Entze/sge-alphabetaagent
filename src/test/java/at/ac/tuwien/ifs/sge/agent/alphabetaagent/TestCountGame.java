package at.ac.tuwien.ifs.sge.agent.alphabetaagent;

import at.ac.tuwien.ifs.sge.game.ActionRecord;
import at.ac.tuwien.ifs.sge.game.Game;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TestCountGame implements Game<Integer, Integer[]> {

  private static final Set<Integer> possibleActions = Collections
      .unmodifiableSet(new TreeSet<>(Arrays.asList(-1, 0, 1)));
  private final boolean canonical;
  private final List<ActionRecord<Integer>> actionRecords;
  private final int max;
  private final int min;
  private int currentPlayer = 0;
  private int score;

  public TestCountGame() {
    this(-1, 1);
  }

  public TestCountGame(int min, int max) {
    this(0, true, Collections.emptyList(), 0, min, max);
  }

  public TestCountGame(int currentPlayer, boolean canonical,
      List<ActionRecord<Integer>> actionRecords,
      int... board) {
    this.currentPlayer = currentPlayer;
    this.canonical = canonical;
    this.actionRecords = new ArrayList<>(actionRecords);
    this.score = board[0];
    this.min = board[1];
    this.max = board[2];
  }


  @Override
  public boolean isGameOver() {
    return !(min <= score && score <= max);
  }

  @Override
  public int getMinimumNumberOfPlayers() {
    return 2;
  }

  @Override
  public int getMaximumNumberOfPlayers() {
    return 2;
  }

  @Override
  public int getNumberOfPlayers() {
    return 2;
  }

  @Override
  public int getCurrentPlayer() {
    return currentPlayer;
  }

  @Override
  public double getUtilityValue(int player) {
    return score * (1 - 2 * player);
  }

  @Override
  public Set<Integer> getPossibleActions() {
    if (isGameOver()) {
      return Collections.emptySet();
    }
    return possibleActions;
  }

  @Override
  public Integer[] getBoard() {
    return new Integer[] {score, min, max};
  }

  @Override
  public boolean isValidAction(Integer integer) {
    return min <= integer && integer <= max;
  }

  @Override
  public Game<Integer, Integer[]> doAction(Integer integer) {
    if (!isValidAction(integer) || isGameOver()) {
      throw new IllegalArgumentException("" + integer);
    }
    TestCountGame next = new TestCountGame(1 - currentPlayer, this.canonical, actionRecords,
        score, min, max);
    next.actionRecords.add(new ActionRecord<>(currentPlayer, integer));
    next.score += integer;
    return next;
  }

  @Override
  public Integer determineNextAction() {
    return null;
  }

  @Override
  public List<ActionRecord<Integer>> getActionRecords() {
    return Collections.unmodifiableList(actionRecords);
  }

  @Override
  public boolean isCanonical() {
    return canonical;
  }

  @Override
  public Game<Integer, Integer[]> getGame(int i) {
    return new TestCountGame(currentPlayer, false, actionRecords, score, min, max);
  }

}
