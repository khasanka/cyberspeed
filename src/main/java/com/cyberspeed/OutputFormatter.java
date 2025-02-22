package com.cyberspeed;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class OutputFormatter {
    static final String MATRIX = "matrix";
    static final String APPLIED_BONUS_SYMBOL = "applied_bonus_symbol";
    static final String APPLIED_WINNING_COMBINATIONS = "applied_winning_combinations";
    static final String REWARD = "reward";

    private String[][] matrix;
    private String appliedBonusSymbol;
    private Map<String, List<String>> appliedWinningCombinations;
    private double reward;

    public OutputFormatter(String[][] matrix, String appliedBonusSymbol, Map<String, List<String>> appliedWinningCombinations, double reward) {
        this.matrix = matrix;
        this.appliedBonusSymbol = appliedBonusSymbol;
        this.appliedWinningCombinations = appliedWinningCombinations;
        this.reward = reward;
    }

    public String formatOutput() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MATRIX, new JSONArray(matrix));
        jsonObject.put(APPLIED_BONUS_SYMBOL, appliedBonusSymbol);
        jsonObject.put(APPLIED_WINNING_COMBINATIONS, new JSONObject(appliedWinningCombinations));
        jsonObject.put(REWARD, reward);
        return jsonObject.toString();
    }

    public void displayMatrix() {
        final int rows = matrix.length;
        final int columns = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.print(String.format("%-" + 6 + "s", matrix[i][j]) + "\t");
            }
            System.out.println();
        }
    }
}
