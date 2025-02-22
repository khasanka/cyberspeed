package com.cyberspeed;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.cyberspeed.ConfigConst.COLUMNS;
import static com.cyberspeed.ConfigConst.ROWS;

public class PuzzleGame {

    private int rows;
    private int columns;
    private String[][] matrix;
    private JSONObject config;
    private Random rand = new Random();
    private Map<String, List<String>> appliedWinningCombinations = new HashMap<>();
    private String appliedBonusSymbol = null;

    public PuzzleGame(String configPath) throws IOException, JSONException {
        String content = new String(Files.readAllBytes(Paths.get(configPath)));
        this.config = new JSONObject(content);
        this.rows = config.getInt(ROWS);
        this.columns = config.getInt(COLUMNS);
        this.matrix = new String[rows][columns];

        validateConfig();
    }

    public void play(int betAmount) {
        generateMatrix();
        double reward = calculateReward(betAmount);
        displayOutput(reward);
    }

    private void validateConfig() {
        ConfigValidator configValidator = new ConfigValidator(config);
        configValidator.validate();
    }

    private void generateMatrix() {

        SymbolGenerator generator = new SymbolGenerator(config, rand);
        matrix = generator.generateMatrix(rows, columns);
    }

    private double calculateReward(int betAmount) {

        RewardCalculator rewardCalculator = new RewardCalculator(config, matrix, betAmount, rand);
        double reward = rewardCalculator.calculate();
        appliedBonusSymbol = rewardCalculator.getSelectedBonusSymbol();
        appliedWinningCombinations = rewardCalculator.getAppliedWinningCombinations();

        return reward;
    }

    private void displayOutput(double reward) {

        OutputFormatter formatter = new OutputFormatter(matrix, appliedBonusSymbol, appliedWinningCombinations, reward);
        System.out.println(formatter.formatOutput());
    }

}
