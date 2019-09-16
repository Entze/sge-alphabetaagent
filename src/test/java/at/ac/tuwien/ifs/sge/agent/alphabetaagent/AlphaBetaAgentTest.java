package at.ac.tuwien.ifs.sge.agent.alphabetaagent;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.sge.engine.Logger;
import at.ac.tuwien.ifs.sge.game.Game;
import at.ac.tuwien.ifs.sge.game.Gib;
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


  Gib gib;
  AlphaBetaAgent<Gib, String> gibAgent0 = new AlphaBetaAgent<>(log);
  AlphaBetaAgent<Gib, String> gibAgent1 = new AlphaBetaAgent<>(log);

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
  public void test_agent_indeterminant_1Player() {
    TestDiceGame game = new TestDiceGame();
    AlphaBetaAgent<TestDiceGame, Integer> agent = new AlphaBetaAgent<>(log);
    agent.setUp(1, 0);

    while (!game.isGameOver()) {
      Integer action;
      if (game.getCurrentPlayer() >= 0) {
        action = agent.computeNextAction(game, 1000, TimeUnit.SECONDS);
        assertTrue("action: " + action, 5 <= action && action <= 9);
      } else {
        action = game.determineNextAction();
      }

      game = (TestDiceGame) game.doAction(action);
    }

  }

  @Test
  public void test_agent_2Players_depth1_2() {
    gib = new Gib(Collections.singletonList("L"), Arrays.asList("L", "R"), 2);
    gibAgent0.setUp(2, 0);
    gibAgent1.setUp(2, 1);

    while (!gib.isGameOver()) {
      String action = null;
      if (gib.getCurrentPlayer() == 0) {
        action = gibAgent0.computeNextAction(gib, 60, TimeUnit.MINUTES);
      } else if (gib.getCurrentPlayer() == 1) {
        action = gibAgent1.computeNextAction(gib, 60, TimeUnit.MINUTES);
      }
      gib = (Gib) gib.doAction(action);
    }

    assertArrayEquals(new double[] {1, 0}, gib.getGameUtilityValue(), 0.001D);

  }

  @Test
  public void test_agent_2Players_depth2_2() {
    gib = new Gib(Arrays.asList("LL", "LR"), Arrays.asList("L", "R"), 2);
    gibAgent0.setUp(2, 0);
    gibAgent1.setUp(2, 1);

    while (!gib.isGameOver()) {
      String action = null;
      if (gib.getCurrentPlayer() == 0) {
        action = gibAgent0.computeNextAction(gib, 60, TimeUnit.SECONDS);
      } else if (gib.getCurrentPlayer() == 1) {
        action = gibAgent1.computeNextAction(gib, 60, TimeUnit.SECONDS);
      }
      gib = (Gib) gib.doAction(action);
    }

    assertArrayEquals(new double[] {1D / 2D, 1D / 2D}, gib.getGameUtilityValue(), 0.001D);

  }

  @Test
  public void test_agent_2Players_depth3_1() {
    gib = new Gib(Arrays.asList("LLL", "LLR", "LRL", "RLR"), Arrays.asList("L", "R"), 2);
    gibAgent0.setUp(2, 0);
    gibAgent1.setUp(2, 1);

    while (!gib.isGameOver()) {
      String action = null;
      if (gib.getCurrentPlayer() == 0) {
        action = gibAgent0.computeNextAction(gib, 60, TimeUnit.SECONDS);
      } else if (gib.getCurrentPlayer() == 1) {
        action = gibAgent1.computeNextAction(gib, 60, TimeUnit.SECONDS);
      }
      gib = (Gib) gib.doAction(action);
    }

    assertArrayEquals(new double[] {1, 0}, gib.getGameUtilityValue(), 0.001D);

  }

  @Test
  public void test_agent_2Players_depth4_0() {
    gib = new Gib(Arrays.asList("LLLL", "LLLR", "LLRL", "LRLR", "RLLL", "RLLR", "RLRL", "RRLR"),
        Arrays.asList("L", "R"), 2);
    gibAgent0.setUp(2, 0);
    gibAgent1.setUp(2, 1);

    while (!gib.isGameOver()) {
      String action = null;
      if (gib.getCurrentPlayer() == 0) {
        action = gibAgent0.computeNextAction(gib, 60, TimeUnit.SECONDS);
      } else if (gib.getCurrentPlayer() == 1) {
        action = gibAgent1.computeNextAction(gib, 60, TimeUnit.SECONDS);
      }
      gib = (Gib) gib.doAction(action);
    }

    assertArrayEquals(new double[] {0, 1}, gib.getGameUtilityValue(), 0.001D);

  }

  @Test
  public void test_agent_2Players_depth3_2() {
    gib = new Gib(Arrays
        .asList("LL", "LM", "LR", "MLL", "MLM", "MLR", "MMM", "MRL", "MRM", "MRR", "RL", "RM",
            "RR"),
        Arrays.asList("L", "M", "R"), 2);
    gibAgent0.setUp(2, 0);
    gibAgent1.setUp(2, 1);

    while (!gib.isGameOver()) {
      String action = null;
      if (gib.getCurrentPlayer() == 0) {
        action = gibAgent0.computeNextAction(gib, 60, TimeUnit.SECONDS);
      } else if (gib.getCurrentPlayer() == 1) {
        action = gibAgent1.computeNextAction(gib, 60, TimeUnit.SECONDS);
      }
      gib = (Gib) gib.doAction(action);
    }

    assertArrayEquals(new double[] {1, 0}, gib.getGameUtilityValue(), 0.001D);

  }

  @Test
  public void test_agent_2Players_depth5_0() {
    gib = new Gib(Arrays
        .asList(
            "LLLLL", "LLLML", "LLLRL", "LMLLL", "LMLML", "LMLRL", "LRLLL", "LRLML", "LRLRL"
        ),
        Arrays.asList("L", "M", "R"), 2);
    gibAgent0.setUp(2, 0);
    gibAgent1.setUp(2, 1);

    while (!gib.isGameOver()) {
      String action = null;
      if (gib.getCurrentPlayer() == 0) {
        action = gibAgent0.computeNextAction(gib, 60, TimeUnit.SECONDS);
        assertEquals("L", action);
      } else if (gib.getCurrentPlayer() == 1) {
        action = gibAgent1.computeNextAction(gib, 60, TimeUnit.SECONDS);
      }
      gib = (Gib) gib.doAction(action);
    }

    assertArrayEquals(new double[] {1, 0}, gib.getGameUtilityValue(), 0.001D);
  }

  @Test
  public void test_agent_2Players_depth5_1() {
    gib = new Gib(Arrays
        .asList(
            "RLRLR", "RLRMR", "RLRRR", "RMRLR", "RMRMR", "RMRRR", "RRRLR", "RRRMR", "RRRRR"
        ),
        Arrays.asList("L", "M", "R"), 2);
    gibAgent0.setUp(2, 0);
    gibAgent1.setUp(2, 1);

    while (!gib.isGameOver()) {
      String action = null;
      if (gib.getCurrentPlayer() == 0) {
        action = gibAgent0.computeNextAction(gib, 60, TimeUnit.SECONDS);
        assertEquals("R", action);
      } else if (gib.getCurrentPlayer() == 1) {
        action = "M";
      }
      gib = (Gib) gib.doAction(action);
    }

    assertArrayEquals(new double[] {1, 0}, gib.getGameUtilityValue(), 0.001D);
  }

  @Test
  public void test_agent_2Players_depth5_2() {
    gib = new Gib(Arrays
        .asList(
            "LLLL", "LLLR", "LLRL", "LLRR", "LRLL", "LRLR", "LRRL", "LRRR", "RR", "RLLL", "RLLRLL",
            "RLLRR"
        ),
        Arrays.asList("L", "R"), 2);
    gibAgent1.setUp(2, 1);

    int round = 0;
    while (!gib.isGameOver()) {
      String action = null;
      if (gib.getCurrentPlayer() == 0) {
        action = new String[] {"R", "L", "R"}[round++];
      } else if (gib.getCurrentPlayer() == 1) {
        action = gibAgent1.computeNextAction(gib, 60, TimeUnit.SECONDS);
      }
      gib = (Gib) gib.doAction(action);
    }

    assertArrayEquals(new double[] {0, 1}, gib.getGameUtilityValue(), 0.001D);
  }


}
