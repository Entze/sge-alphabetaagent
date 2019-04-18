package dev.entze.sge.agent.alphabetaagent;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import dev.entze.sge.engine.Logger;
import dev.entze.sge.game.Game;
import dev.entze.sge.game.Nim;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class AlphaBetaAgentTest {

  Logger log = new Logger(-2, "",
      "",
      "",
      System.out,
      "",
      "",
      System.out,
      "",
      "",
      System.out,
      "",
      "",
      System.err,
      "",
      "",
      System.err,
      ""
  );

  AlphaBetaAgent<Game<Integer, Integer[]>, Integer> agent = new AlphaBetaAgent<>(log);

  Game<Integer, Integer[]> testGame;


  Nim nim;
  AlphaBetaAgent<Nim, String> nimAgent0 = new AlphaBetaAgent<>(log);
  AlphaBetaAgent<Nim, String> nimAgent1 = new AlphaBetaAgent<>(log);

  @Before
  public void setUp() {
  }

  @Test
  public void test_agent_2Players_depth1_0() {
    agent.setUp(2, 0);
    assertEquals(1, (int) agent.computeNextAction(new TestCountGame(), 10, TimeUnit.SECONDS));
  }

  @Test
  public void test_agent_2Players_depth1_1() {
    agent.setUp(2, 1);
    assertEquals(-1, (int) agent
        .computeNextAction(new TestCountGame(1, true, Collections.emptyList(), 0, -1, 1), 10,
            TimeUnit.SECONDS));
  }

  @Test
  public void test_agent_2Players_depth2_0() {
    int player = 0;
    testGame = new TestCountGame(player, true, Collections.emptyList(), 0, -2, 2);
    agent.setUp(2, player);
    while (!testGame.isGameOver()) {
      assertEquals(1 - 2 * player, (int) agent.computeNextAction(testGame, 10, TimeUnit.SECONDS));
      testGame = testGame.doAction(1 - 2 * player);
      if (!testGame.isGameOver()) {
        testGame = testGame.doAction(0);
      }
    }
  }

  @Test
  public void test_agent_2Players_depth2_1() {
    int player = 1;
    testGame = new TestCountGame(player, true, Collections.emptyList(), 0, -2, 2);
    agent.setUp(2, player);
    while (!testGame.isGameOver()) {
      assertEquals(1 - 2 * player, (int) agent.computeNextAction(testGame, 10, TimeUnit.SECONDS));
      testGame = testGame.doAction(1 - 2 * player);
      if (!testGame.isGameOver()) {
        testGame = testGame.doAction(0);
      }
    }
  }

  @Test
  public void test_agent_2Players_depth3_0() {
    int player = 0;
    testGame = new TestCountGame(player, true, Collections.emptyList(), 0, -2, 2);
    agent.setUp(2, player);
    while (!testGame.isGameOver()) {
      assertEquals(1 - 2 * player, (int) agent.computeNextAction(testGame, 10, TimeUnit.SECONDS));
      testGame = testGame.doAction(1 - 2 * player);
      if (!testGame.isGameOver()) {
        testGame = testGame.doAction(0);
      }
    }
  }

  @Test
  public void test_agent_2Players_depth99_0() {
    int player = 0;
    testGame = new TestCountGame(player, true, Collections.emptyList(), 0, -99, 99);
    agent.setUp(2, player);
    while (!testGame.isGameOver()) {
      assertEquals(1 - 2 * player, (int) agent.computeNextAction(testGame, 10, TimeUnit.SECONDS));
      testGame = testGame.doAction(1 - 2 * player);
      if (!testGame.isGameOver()) {
        testGame = testGame.doAction(0);
      }
    }
  }


  @Test
  public void test_agent_2Players_depth1_2() {
    nim = new Nim(Collections.singletonList("L"), Arrays.asList("L", "R"), 2);
    nimAgent0.setUp(2, 0);
    nimAgent1.setUp(2, 1);

    while (!nim.isGameOver()) {
      String action = null;
      if (nim.getCurrentPlayer() == 0) {
        action = nimAgent0.computeNextAction(nim, 60, TimeUnit.MINUTES);
      } else if (nim.getCurrentPlayer() == 1) {
        action = nimAgent1.computeNextAction(nim, 60, TimeUnit.MINUTES);
      }
      nim = (Nim) nim.doAction(action);
    }

    assertArrayEquals(new double[] {1, 0}, nim.getGameUtilityValue(), 0.001D);

  }

  @Test
  public void test_agent_2Players_depth2_2() {
    nim = new Nim(Arrays.asList("LL", "LR"), Arrays.asList("L", "R"), 2);
    nimAgent0.setUp(2, 0);
    nimAgent1.setUp(2, 1);

    while (!nim.isGameOver()) {
      String action = null;
      if (nim.getCurrentPlayer() == 0) {
        action = nimAgent0.computeNextAction(nim, 60, TimeUnit.SECONDS);
      } else if (nim.getCurrentPlayer() == 1) {
        action = nimAgent1.computeNextAction(nim, 60, TimeUnit.SECONDS);
      }
      nim = (Nim) nim.doAction(action);
    }

    assertArrayEquals(new double[] {1D / 2D, 1D / 2D}, nim.getGameUtilityValue(), 0.001D);

  }

  @Test
  public void test_agent_2Players_depth3_1() {
    nim = new Nim(Arrays.asList("LLL", "LLR", "LRL", "RLR"), Arrays.asList("L", "R"), 2);
    nimAgent0.setUp(2, 0);
    nimAgent1.setUp(2, 1);

    while (!nim.isGameOver()) {
      String action = null;
      if (nim.getCurrentPlayer() == 0) {
        action = nimAgent0.computeNextAction(nim, 60, TimeUnit.SECONDS);
      } else if (nim.getCurrentPlayer() == 1) {
        action = nimAgent1.computeNextAction(nim, 60, TimeUnit.SECONDS);
      }
      nim = (Nim) nim.doAction(action);
    }

    assertArrayEquals(new double[] {1, 0}, nim.getGameUtilityValue(), 0.001D);

  }

  @Test
  public void test_agent_2Players_depth4_0() {
    nim = new Nim(Arrays.asList("LLLL", "LLLR", "LLRL", "LRLR", "RLLL", "RLLR", "RLRL", "RRLR"),
        Arrays.asList("L", "R"), 2);
    nimAgent0.setUp(2, 0);
    nimAgent1.setUp(2, 1);

    while (!nim.isGameOver()) {
      String action = null;
      if (nim.getCurrentPlayer() == 0) {
        action = nimAgent0.computeNextAction(nim, 60, TimeUnit.SECONDS);
      } else if (nim.getCurrentPlayer() == 1) {
        action = nimAgent1.computeNextAction(nim, 60, TimeUnit.SECONDS);
      }
      nim = (Nim) nim.doAction(action);
    }

    assertArrayEquals(new double[] {0, 1}, nim.getGameUtilityValue(), 0.001D);

  }

  @Test
  public void test_agent_2Players_depth3_2() {
    nim = new Nim(Arrays
        .asList("LL", "LM", "LR", "MLL", "MLM", "MLR", "MMM", "MRL", "MRM", "MRR", "RL", "RM",
            "RR"),
        Arrays.asList("L", "M", "R"), 2);
    nimAgent0.setUp(2, 0);
    nimAgent1.setUp(2, 1);

    while (!nim.isGameOver()) {
      String action = null;
      if (nim.getCurrentPlayer() == 0) {
        action = nimAgent0.computeNextAction(nim, 60, TimeUnit.SECONDS);
      } else if (nim.getCurrentPlayer() == 1) {
        action = nimAgent1.computeNextAction(nim, 60, TimeUnit.SECONDS);
      }
      nim = (Nim) nim.doAction(action);
    }

    assertArrayEquals(new double[] {1, 0}, nim.getGameUtilityValue(), 0.001D);

  }

  @Test
  public void test_agent_2Players_depth5_0() {
    nim = new Nim(Arrays
        .asList(
            "LLLLL", "LLLML", "LLLRL", "LMLLL", "LMLML", "LMLRL", "LRLLL", "LRLML", "LRLRL"
        ),
        Arrays.asList("L", "M", "R"), 2);
    nimAgent0.setUp(2, 0);
    nimAgent1.setUp(2, 1);

    while (!nim.isGameOver()) {
      String action = null;
      if (nim.getCurrentPlayer() == 0) {
        action = nimAgent0.computeNextAction(nim, 60, TimeUnit.SECONDS);
        assertEquals("L", action);
      } else if (nim.getCurrentPlayer() == 1) {
        action = nimAgent1.computeNextAction(nim, 60, TimeUnit.SECONDS);
      }
      nim = (Nim) nim.doAction(action);
    }

    assertArrayEquals(new double[] {1, 0}, nim.getGameUtilityValue(), 0.001D);
  }

  @Test
  public void test_agent_2Players_depth5_1() {
    nim = new Nim(Arrays
        .asList(
            "RLRLR", "RLRMR", "RLRRR", "RMRLR", "RMRMR", "RMRRR", "RRRLR", "RRRMR", "RRRRR"
        ),
        Arrays.asList("L", "M", "R"), 2);
    nimAgent0.setUp(2, 0);
    nimAgent1.setUp(2, 1);

    while (!nim.isGameOver()) {
      String action = null;
      if (nim.getCurrentPlayer() == 0) {
        action = nimAgent0.computeNextAction(nim, 60, TimeUnit.SECONDS);
        assertEquals("R", action);
      } else if (nim.getCurrentPlayer() == 1) {
        action = "M";
      }
      nim = (Nim) nim.doAction(action);
    }

    assertArrayEquals(new double[] {1, 0}, nim.getGameUtilityValue(), 0.001D);
  }

  @Test
  public void test_agent_2Players_depth5_2() {
    nim = new Nim(Arrays
        .asList(
            "LLLL", "LLLR", "LLRL", "LLRR", "LRLL", "LRLR", "LRRL", "LRRR", "RR", "RLLL", "RLLRLL",
            "RLLRR"
        ),
        Arrays.asList("L", "R"), 2);
    nimAgent1.setUp(2, 1);

    int round = 0;
    while (!nim.isGameOver()) {
      String action = null;
      if (nim.getCurrentPlayer() == 0) {
        action = new String[] {"R", "L", "R"}[round++];
      } else if (nim.getCurrentPlayer() == 1) {
        action = nimAgent1.computeNextAction(nim, 60, TimeUnit.SECONDS);
      }
      nim = (Nim) nim.doAction(action);
    }

    assertArrayEquals(new double[] {0, 1}, nim.getGameUtilityValue(), 0.001D);
  }


}
