package com.cyberspeed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PuzzleGame {

    private static final String CONFIG_ROWS = "rows";
    private static final String CONFIG_COLUMNS = "columns";
    private static final String CONFIG_PROBABILITIES = "probabilities";
    private static final String CONFIG_WIN_COMBINATIONS = "win_combinations";
    private static final String CONFIG_REWARD_MULTIPLIER = "reward_multiplier";
    private static final String CONFIG_COVERED_AREAS = "covered_areas";
    private static final String CONFIG_WHEN = "when";
    private static final String CONFIG_COUNT = "count";
    private static final String CONFIG_TYPE = "type";
    private static final String CONFIG_IMPACT = "impact";
    private static final String CONFIG_EXTRA = "extra";
    private static final String CONFIG_STANDARD_SYMBOLS = "standard_symbols";
    private static final String CONFIG_BONUS_SYMBOLS = "bonus_symbols";
    private static final String CONFIG_SYMBOLS = "symbols";

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
        this.rows = config.getInt(CONFIG_ROWS);
        this.columns = config.getInt(CONFIG_COLUMNS);
        this.matrix = new String[rows][columns];
    }

    private void generateMatrix() {

        JSONObject probabilities = config.getJSONObject(CONFIG_PROBABILITIES);
        JSONArray standardSymbols = probabilities.getJSONArray(CONFIG_STANDARD_SYMBOLS);
        JSONObject bonusSymbols = probabilities.getJSONObject(CONFIG_BONUS_SYMBOLS).getJSONObject(CONFIG_SYMBOLS);

        // add symbol to each cell
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = getRandomSymbol(standardSymbols, i, j, bonusSymbols);
            }
        }
    }

    private String getRandomSymbol(JSONArray standardSymbols, int row, int col, JSONObject bonusSymbols) {
        /** This will derive Probabilities and symbols from both 'symbols' & 'bonusSymbols'.
         * It will forward those 'symbolWeights' hash map into 'weightedRandomChoice()'
         * method to get random symbol according to the weights.
         */

        JSONObject cellProbabilities = standardSymbols.optJSONObject(row * columns + col);
        Map<String, Integer> symbolWeights = new HashMap<>();
        JSONObject symbols = cellProbabilities.getJSONObject(CONFIG_SYMBOLS);

        if (cellProbabilities == null) {
            cellProbabilities = standardSymbols.getJSONObject(0); // Default to first entry if not found
        }

        for (String key : symbols.keySet()) {
            symbolWeights.put(key, symbols.getInt(key));
        }

        for (String key : bonusSymbols.keySet()) {
            symbolWeights.put(key, bonusSymbols.getInt(key));
        }

        return weightedRandomChoice(symbolWeights);
    }

    private String weightedRandomChoice(Map<String, Integer> symbolWeights) {
        /**
         * This will create weighted pool like bellow
         * ["A", "A", "A", "A", "A", "B", "B", "B", "C", "C"]
         * and pick and return one of them randomly
         */

        List<String> pool = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : symbolWeights.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.add(entry.getKey());
            }
        }
        return pool.get(rand.nextInt(pool.size()));
    }

    private double calculateReward(int betAmount) {
        JSONObject winCombinations = config.getJSONObject(CONFIG_WIN_COMBINATIONS);
        double totalReward = 0;
        boolean hasWinningCombination = false;
        Map<String, Double> rewardList = new HashMap<>();

        for (String winningCombination : winCombinations.keySet()) {
            JSONObject comboDetails = winCombinations.getJSONObject(winningCombination);

            if (comboDetails.getString(CONFIG_WHEN).equals("linear_symbols")) {
                JSONArray areas = comboDetails.getJSONArray(CONFIG_COVERED_AREAS);

                for (int i = 0; i < areas.length(); i++) {
                    JSONArray area = areas.getJSONArray(i);

                    if (checkArea(area)) {
                        hasWinningCombination = true;
                        String firstSymbol = getSymbolFromPosition(area.getString(0));
                        int symbolMultiplier = config.getJSONObject(CONFIG_SYMBOLS).getJSONObject(firstSymbol).optInt(CONFIG_REWARD_MULTIPLIER, 1);

                        double reward; // linear_symbols
                        if (rewardList.containsKey(firstSymbol)) {
                            reward = rewardList.get(firstSymbol) * comboDetails.getDouble(CONFIG_REWARD_MULTIPLIER);
                        } else {
                            reward = betAmount * comboDetails.getInt(CONFIG_REWARD_MULTIPLIER) * symbolMultiplier;
                        }
                        rewardList.put(firstSymbol, reward);
                        appliedWinningCombinations.putIfAbsent(firstSymbol, new ArrayList<>());
                        appliedWinningCombinations.get(firstSymbol).add(winningCombination);
                    }
                }

            } else if (comboDetails.getString(CONFIG_WHEN).equals("same_symbols")) {
                int count = comboDetails.getInt(CONFIG_COUNT);
                Map<String, Double> symbolCount = new HashMap<>();

                // -- count same_symbols each
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        symbolCount.put(matrix[i][j], symbolCount.getOrDefault(matrix[i][j], 0.0) + 1);
                    }
                }
                int symbolReward = 0;
                for (Map.Entry<String, Double> entry : symbolCount.entrySet()) {
                    // -- selected symbol
                    if (entry.getValue() == count && entry.getKey().length() == 1) {
                        hasWinningCombination = true;
                        double symbolMultiplier = config.getJSONObject(CONFIG_SYMBOLS).getJSONObject(entry.getKey()).optDouble(CONFIG_REWARD_MULTIPLIER, 1);

                        double reward; // same_symbols
                        if (rewardList.containsKey(entry.getKey())) {
                            reward = rewardList.get(entry.getKey()) * comboDetails.getDouble(CONFIG_REWARD_MULTIPLIER);
                        } else {
                            reward = betAmount * comboDetails.getDouble(CONFIG_REWARD_MULTIPLIER) * symbolMultiplier;
                        }
                        rewardList.put(entry.getKey(), reward);

                        appliedWinningCombinations.putIfAbsent(entry.getKey(), new ArrayList<>());
                        appliedWinningCombinations.get(entry.getKey()).add(winningCombination);
                    }
                }
            }
        }

        totalReward = rewardList.values().stream().mapToDouble(Double::intValue).sum();

        // Apply bonuses only if at least one winning combination was found
        if (hasWinningCombination) {
            totalReward = applyBonusSymbols(totalReward);
        }
        return totalReward;
    }

    private double applyBonusSymbols(double reward) {
        List<String> bonusSymbols = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (matrix[i][j].length() > 1 && !matrix[i][j].equals("MISS")) {
                    bonusSymbols.add(matrix[i][j]);
                }
            }
        }

        if (bonusSymbols.size() == 0) {
            return reward;
        }

        appliedBonusSymbol = bonusSymbols.get(rand.nextInt(bonusSymbols.size()));
        JSONObject symbolDetails = config.getJSONObject(CONFIG_SYMBOLS).getJSONObject(appliedBonusSymbol);
        if (symbolDetails.getString(CONFIG_TYPE).equals("bonus")) {
            reward = applyBonusEffect(symbolDetails, reward);
        }

        return reward;
    }

    private double applyBonusEffect(JSONObject bonus, double reward) {
        String impact = bonus.getString(CONFIG_IMPACT);
        if (impact.equals("multiply_reward")) {
            return reward * bonus.getInt(CONFIG_REWARD_MULTIPLIER);
        } else if (impact.equals("extra_bonus")) {
            return reward + bonus.getInt(CONFIG_EXTRA);
        }
        return reward;
    }


    private String getSymbolFromPosition(String position) {
        String[] pos = position.split(":");
        return matrix[Integer.parseInt(pos[0])][Integer.parseInt(pos[1])];
    }

    private boolean checkArea(JSONArray area) {
        String firstSymbol = getSymbolFromPosition(area.getString(0));
        for (int i = 1; i < area.length(); i++) {
            if (!getSymbolFromPosition(area.getString(i)).equals(firstSymbol)) {
                return false;
            }
        }
        return true;
    }

    public void play(int betAmount) {
        generateMatrix();
//        displayMatrix();
        double reward = calculateReward(betAmount);
        displayOutput(reward);
//        System.out.println("{\n" +
//                "\"matrix\": \n" +
//                new JSONArray(matrix).toString() +
//                ", \n\"reward\": " + reward +
//                ", \n\"applied_winning_combinations\": \n" +
//                new JSONObject(appliedWinningCombinations).toString() +
//                ", \n\"applied_bonus_symbol\": " + appliedBonusSymbol
//                + "\n}");

    }

    public void displayOutput(double reward) {

        JSONObject jsonObject = new JSONObject();

        // Create the main JSON object
        jsonObject.put("matrix", new JSONArray(matrix));
        jsonObject.put("applied_bonus_symbol", appliedBonusSymbol);
        jsonObject.put("applied_winning_combinations", new JSONObject(appliedWinningCombinations));
        jsonObject.put("reward", reward);

        // Print the JSON object
        System.out.println(jsonObject.toString());
    }

    private void displayMatrix() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.print(String.format("%-" + 6 + "s", matrix[i][j]) + "\t");
            }
            System.out.println();
        }
    }


}
