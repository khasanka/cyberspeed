import com.cyberspeed.PuzzleGame;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;

public class PuzzleGameTest {

    private static String CONFIG_3x3_FILE_PATH = "src/test/resources/config_3x3.json";

    // Helper method to inject a predefined matrix into the com.cyberspeed.PuzzleGame instance
    private void setMatrix(PuzzleGame game, String[][] matrix) throws Exception {
        Field matrixField = PuzzleGame.class.getDeclaredField("matrix");
        matrixField.setAccessible(true);
        matrixField.set(game, matrix);

        Field rowsField = PuzzleGame.class.getDeclaredField("rows");
        rowsField.setAccessible(true);
        rowsField.set(game, matrix.length);

        Field columnsField = PuzzleGame.class.getDeclaredField("columns");
        columnsField.setAccessible(true);
        columnsField.set(game, matrix[0].length);
    }

    // Helper method to call calculateReward via reflection
    private double calculateReward(PuzzleGame game, int betAmount) throws Exception {
        Method calculateReward = PuzzleGame.class.getDeclaredMethod("calculateReward", int.class);
        calculateReward.setAccessible(true);
        return (double) calculateReward.invoke(game, betAmount);
    }

    // Test 1: Lost Game (no winning combinations)
    @Test
    public void testLostGame() throws Exception {
        PuzzleGame game = new PuzzleGame(CONFIG_3x3_FILE_PATH);
        String[][] matrix = {
                {"A", "B", "C"},
                {"E", "B", "5x"},
                {"F", "D", "C"}
        };
        setMatrix(game, matrix);

        double reward = calculateReward(game, 100);
        assertEquals(0, reward, "Reward should be 0 for losing game");

        // Verify no winning combinations applied
        Field appliedCombinationsField = PuzzleGame.class.getDeclaredField("appliedWinningCombinations");
        appliedCombinationsField.setAccessible(true);
        Map<String, List<String>> appliedCombinations = (Map<String, List<String>>) appliedCombinationsField.get(game);
        assertTrue(appliedCombinations.isEmpty(), "No winning combinations should be applied");

        // Verify no bonus applied
        Field bonusSymbolField = PuzzleGame.class.getDeclaredField("appliedBonusSymbol");
        bonusSymbolField.setAccessible(true);
        assertNull(bonusSymbolField.get(game), "Bonus symbol should be null");
    }

    // Test 2: Won Game with 3 Bs and 10x Bonus
    @Test
    public void testWonGameWith10xBonus() throws Exception {
        PuzzleGame game = new PuzzleGame(CONFIG_3x3_FILE_PATH);
        String[][] matrix = {
                {"A", "B", "C"},
                {"E", "B", "10x"},
                {"F", "D", "B"}
        };
        setMatrix(game, matrix);

        double reward = calculateReward(game, 100);
        // Expected: 100 (bet) * 3 (B's multiplier) * 1 (same_symbol_3_times) * 10 (10x bonus) = 3000
        assertEquals(3000, reward, "Reward calculation mismatch");

        // Verify applied combinations
        Field appliedCombinationsField = PuzzleGame.class.getDeclaredField("appliedWinningCombinations");
        appliedCombinationsField.setAccessible(true);
        Map<String, List<String>> appliedCombinations = (Map<String, List<String>>) appliedCombinationsField.get(game);
        assertTrue(appliedCombinations.containsKey("B"), "B should have winning combinations");
        assertEquals(1, appliedCombinations.get("B").size(), "B should have one winning combination");
        assertTrue(appliedCombinations.get("B").contains("same_symbol_3_times"), "Missing combination");

        // Verify bonus symbol
        Field bonusSymbolField = PuzzleGame.class.getDeclaredField("appliedBonusSymbol");
        bonusSymbolField.setAccessible(true);
        assertEquals("10x", bonusSymbolField.get(game), "Incorrect bonus symbol");
    }

    // Test 3: Extra Bonus (+1000) Applied
    @Test
    public void testExtraBonus() throws Exception {
        PuzzleGame game = new PuzzleGame(CONFIG_3x3_FILE_PATH);
        String[][] matrix = {
                {"A", "A", "A"},
                {"A", "+1000", "A"},
                {"A", "A", "A"} // 8 A's (config's same_symbol_8_times has multiplier 10)
        };
        setMatrix(game, matrix);

        double reward = calculateReward(game, 100);
        // Expected:
        // all 8 A's                    => (100 * 5 * 10)   = 5000
        //same_symbols_horizontally x2  => 5000 * 2 * 2     = 20000
        //same_symbols_vertically x2    => 20000 * 2 * 2    = 80000
        //extra_bonus                   => 80000 + 1000     = 81000

        assertEquals(81000, reward, "Extra bonus not applied correctly");

        // Verify applied bonus
        Field bonusSymbolField = PuzzleGame.class.getDeclaredField("appliedBonusSymbol");
        bonusSymbolField.setAccessible(true);
        assertEquals("+1000", bonusSymbolField.get(game), "Incorrect extra bonus");
    }

    // Test 4: Linear Combination (Horizontal)
    @Test
    public void testHorizontalWin() throws Exception {
        PuzzleGame game = new PuzzleGame(CONFIG_3x3_FILE_PATH);
        String[][] matrix = {
                {"A", "A", "A"},
                {"B", "B", "B"},
                {"C", "C", "C"}
        };
        setMatrix(game, matrix);

        double reward = calculateReward(game, 100);
        //
        // Also Each row triggers same_symbol_3_times
        //
        //  A: => 100 * 5 * 1 => 500
        //  B: => 100 * 3 * 1 => 300
        //  C: => 100 * 2.5 * 1 => 250
        //
        // Each row triggers same_symbols_horizontally
        //  A: => 500 * 2 => 1000
        //  B: => 300 * 2 => 600 + 1000 = 1600
        //  C: => 250 * 2 => 500 + 1600 = 2100


        assertEquals(2100, reward, "Horizontal combinations not calculated correctly");
    }

    // Test 5: MISS Bonus (no impact)
    @Test
    public void testMissBonus() throws Exception {
        PuzzleGame game = new PuzzleGame(CONFIG_3x3_FILE_PATH);
        String[][] matrix = {
                {"A", "A", "A"},
                {"A", "MISS", "A"},
                {"A", "A", "A"}
        };
        setMatrix(game, matrix);

        double reward = calculateReward(game, 100);
        // Expected:
        // all 8 A's                    => (100 * 5 * 10)   = 5000
        //same_symbols_horizontally x2  => 5000 * 2 * 2     = 20000
        //same_symbols_vertically x2    => 20000 * 2 * 2    = 80000
        assertEquals(80000, reward, "MISS bonus should not affect reward");

        Field bonusSymbolField = PuzzleGame.class.getDeclaredField("appliedBonusSymbol");
        bonusSymbolField.setAccessible(true);
        assertEquals(null, bonusSymbolField.get(game), "MISS bonus not recorded");
    }
}