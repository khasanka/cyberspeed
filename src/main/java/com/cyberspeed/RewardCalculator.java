package com.cyberspeed;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static com.cyberspeed.ConfigConst.*;

public class RewardCalculator {
    private JSONObject config;
    private String[][] matrix;
    private int betAmount;
    private Random rand;
    private String selectedBonusSymbol;
    private Map<String, List<String>> appliedWinningCombinations = new HashMap<>();

    public RewardCalculator(JSONObject config, String[][] matrix, int betAmount, Random rand) {
        this.config = config;
        this.matrix = matrix;
        this.betAmount = betAmount;
        this.rand = rand;
    }

    public double calculate() {
        JSONObject winCombinations = config.getJSONObject(WIN_COMBINATIONS);
        double totalReward = 0;
        final Map<String, Double> rewardList = new HashMap<>();

        for (String winningCombination : winCombinations.keySet()) {
            final JSONObject comboDetails = winCombinations.getJSONObject(winningCombination);
            final String when = comboDetails.getString(WHEN);

            if ("linear_symbols".equals(when)) {
                final JSONArray areas = comboDetails.getJSONArray(COVERED_AREAS);
                for (int i = 0; i < areas.length(); i++) {
                    final JSONArray area = areas.getJSONArray(i);
                    if (checkArea(area)) {
                        final String firstSymbol = getSymbolFromPosition(area.getString(0));
                        final int symbolMultiplier = config.getJSONObject(SYMBOLS).getJSONObject(firstSymbol).optInt(REWARD_MULTIPLIER, 1);
                        double reward; // linear_symbols
                        if (rewardList.containsKey(firstSymbol)) {
                            reward = rewardList.get(firstSymbol) * comboDetails.getDouble(REWARD_MULTIPLIER);
                        } else {
                            reward = betAmount * comboDetails.getInt(REWARD_MULTIPLIER) * symbolMultiplier;
                        }
                        rewardList.put(firstSymbol, reward);
                        appliedWinningCombinations.putIfAbsent(firstSymbol, new ArrayList<>());
                        appliedWinningCombinations.get(firstSymbol).add(winningCombination);
                    }
                }
            } else if (comboDetails.getString(WHEN).equals(SAME_SYMBOLS)) {
                int count = comboDetails.getInt(COUNT);
                Map<String, Double> symbolCount = new HashMap<>();

                final int rows = matrix.length;
                final int columns = matrix[0].length;

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
                        final double symbolMultiplier = config.getJSONObject(SYMBOLS).getJSONObject(entry.getKey()).optDouble(REWARD_MULTIPLIER, 1);

                        double reward; // same_symbols
                        if (rewardList.containsKey(entry.getKey())) {
                            reward = rewardList.get(entry.getKey()) * comboDetails.getDouble(REWARD_MULTIPLIER);
                        } else {
                            reward = betAmount * comboDetails.getDouble(REWARD_MULTIPLIER) * symbolMultiplier;
                        }
                        rewardList.put(entry.getKey(), reward);

                        appliedWinningCombinations.putIfAbsent(entry.getKey(), new ArrayList<>());
                        appliedWinningCombinations.get(entry.getKey()).add(winningCombination);
                    }
                }
            }
        }

        totalReward = rewardList.values().stream().mapToDouble(Double::doubleValue).sum();
        return totalReward > 0 ? applyBonusSymbols(totalReward) : 0;
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

    private String getSymbolFromPosition(String position) {
        String[] pos = position.split(":");
        return matrix[Integer.parseInt(pos[0])][Integer.parseInt(pos[1])];
    }

    private double applyBonusSymbols(double reward) {
        List<String> bonusSymbols = new ArrayList<>();
        final int rows = matrix.length;
        final int columns = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (matrix[i][j].length() > 1 && !matrix[i][j].equals(MISS)) {
                    bonusSymbols.add(matrix[i][j]);
                }
            }
        }

        if (bonusSymbols.size() == 0) {
            return reward;
        }

        selectedBonusSymbol = bonusSymbols.get(rand.nextInt(bonusSymbols.size()));
        JSONObject symbolDetails = config.getJSONObject(SYMBOLS).getJSONObject(selectedBonusSymbol);
        if (symbolDetails.getString(TYPE).equals(BONUS)) {
            reward = applyBonusEffect(symbolDetails, reward);
        }

        return reward;
    }

    private double applyBonusEffect(JSONObject bonus, double reward) {
        String impact = bonus.getString(IMPACT);
        if (impact.equals(MULTIPLY_REWARD)) {
            return reward * bonus.getInt(REWARD_MULTIPLIER);
        } else if (impact.equals(EXTRA_BONUS)) {
            return reward + bonus.getInt(EXTRA);
        }
        return reward;
    }

    public String getSelectedBonusSymbol() {
        return selectedBonusSymbol;
    }

    public Map<String, List<String>> getAppliedWinningCombinations() {
        return appliedWinningCombinations;
    }
}
