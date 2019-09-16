package at.ac.tuwien.ifs.sge.agent.alphabetaagent;

import at.ac.tuwien.ifs.sge.game.ActionRecord;
import at.ac.tuwien.ifs.sge.game.Dice;
import at.ac.tuwien.ifs.sge.game.Game;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TestDiceGame implements Game<Integer, Integer> {

  private final static Dice dice = new Dice(1);
  private final static int PREDICTION_OFFSET = 0;
  private final static int PREDICTION_BITS = 0b1111;
  private final static int PREDICTION_MASK = PREDICTION_BITS << PREDICTION_OFFSET;
  private final static int DIE1_OFFSET = Integer.bitCount(PREDICTION_BITS) + 1;
  private final static int DIE1_BITS = 0b111;
  private final static int DIE1_MASK = DIE1_BITS << DIE1_OFFSET;
  private final static int DIE2_OFFSET = DIE1_OFFSET + Integer.bitCount(DIE1_BITS) + 1;
  private final static int DIE2_BITS = DIE1_BITS;
  private final static int DIE2_MASK = DIE2_BITS << DIE2_OFFSET;
  private final boolean canonical;
  private int currentPlayer;
  private List<ActionRecord<Integer>> actionRecords;
  private int board;

  public TestDiceGame() {
    currentPlayer = 0;
    canonical = true;
    board = 0;
    actionRecords = Collections.emptyList();
  }

  public TestDiceGame(int currentPlayer, boolean canonical,
      List<ActionRecord<Integer>> actionRecords, int board) {
    this.currentPlayer = currentPlayer;
    this.canonical = canonical;
    this.actionRecords = new ArrayList<>(actionRecords);
    this.board = board;
  }

  public TestDiceGame(TestDiceGame testDiceGame) {
    this(testDiceGame.currentPlayer, testDiceGame.canonical, testDiceGame.actionRecords,
        testDiceGame.board);
  }

  private static int getFromBoard(int board, int mask, int offset) {
    return (board & mask) >>> offset;
  }

  private static int getPrediction(int board) {
    return getFromBoard(board, PREDICTION_MASK, PREDICTION_OFFSET);
  }

  private static int getDie1(int board) {
    return getFromBoard(board, DIE1_MASK, DIE1_OFFSET);
  }

  private static int getDie2(int board) {
    return getFromBoard(board, DIE2_MASK, DIE2_OFFSET);
  }

  @Override
  public boolean isGameOver() {
    return 2 <= getPrediction() && getPrediction() <= 12
        && 1 <= getDie1() && getDie1() <= 6
        && 1 <= getDie2() && getDie2() <= 6;
  }

  @Override
  public int getMinimumNumberOfPlayers() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfPlayers() {
    return 1;
  }

  @Override
  public int getNumberOfPlayers() {
    return 1;
  }

  @Override
  public int getCurrentPlayer() {
    return this.currentPlayer;
  }

  @Override
  public double getUtilityValue(int i) {
    int prediction = getPrediction();
    if (!(2 <= prediction && prediction <= 12)) {
      return 0;
    }
    int die1 = getDie1();
    if (!(1 <= die1 && die1 <= 6)) {
      return 0;
    }
    int die2 = getDie2();
    if (!(1 <= die2 && die2 <= 6)) {
      return 0;
    }

    return prediction == die1 + die2 ? 1 : (-1);
  }

  @Override
  public Set<Integer> getPossibleActions() {
    if (!(1 <= getPrediction() && getPrediction() <= 12)) {
      return Set.of(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    } else if (!(1 <= getDie1() && getDie1() <= 6) || !(1 <= getDie2() && getDie2() <= 6)) {
      return Set.of(1, 2, 3, 4, 5, 6);
    }
    return Collections.emptySet();
  }

  @Override
  public Integer getBoard() {
    return board;
  }

  @Override
  public Game<Integer, Integer> doAction(Integer integer) {
    if (integer == null || isGameOver() || !(1 <= integer && integer <= 12)) {
      throw new IllegalArgumentException();
    }

    TestDiceGame next = new TestDiceGame(this);
    if (!(2 <= getPrediction() && getPrediction() <= 12)) {
      next.setPrediction(integer);
      next.currentPlayer = (-1);
    } else if (!(1 <= getDie1() && getDie1() <= 6) || !(1 <= getDie2() && getDie2() <= 6)) {
      if (integer > 6) {
        throw new IllegalArgumentException();
      }
      if (!(1 <= getDie1() && getDie1() <= 6)) {
        next.setDie1(integer);
      } else {
        next.setDie2(integer);
        next.currentPlayer = 0;
      }
    }

    next.actionRecords.add(new ActionRecord<>(currentPlayer, integer));

    return next;
  }

  @Override
  public Integer determineNextAction() {
    if (currentPlayer < 0) {
      dice.roll();
      return dice.getFaceOf(0);
    }
    return null;
  }

  @Override
  public List<ActionRecord<Integer>> getActionRecords() {
    return actionRecords;
  }

  @Override
  public boolean isCanonical() {
    return this.canonical;
  }

  @Override
  public Game<Integer, Integer> getGame(int i) {
    return new TestDiceGame(currentPlayer, false, actionRecords, board);
  }

  private int getPrediction() {
    return getPrediction(board);
  }

  private void setPrediction(int prediction) {
    board = setBoard(board, prediction, PREDICTION_MASK, PREDICTION_BITS, PREDICTION_OFFSET);
  }

  private int setBoard(int board, int value, int mask, int bits, int offset) {
    return (board & ~mask) | ((value & bits) << offset);
  }

  private int getDie1() {
    return getDie1(board);
  }

  private void setDie1(int die1) {
    board = setBoard(board, die1, DIE1_MASK, DIE1_BITS, DIE1_OFFSET);
  }

  private int getDie2() {
    return getDie2(board);
  }

  private void setDie2(int die2) {
    board = setBoard(board, die2, DIE2_MASK, DIE2_BITS, DIE2_OFFSET);
  }

}
