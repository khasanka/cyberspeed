package com.cyberspeed;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static com.cyberspeed.ConfigConst.*;

public class SymbolGenerator {
    private JSONObject config;
    private Random rand;

    public SymbolGenerator(JSONObject config, Random rand) {
        this.config = config;
        this.rand = rand;
    }

    public String[][] generateMatrix(int rows, int columns) {
        String[][] matrix = new String[rows][columns];

        JSONObject probabilities = config.getJSONObject(PROBABILITIES);
        JSONArray standardSymbols = probabilities.getJSONArray(STANDARD_SYMBOLS);
        JSONObject bonusSymbols = probabilities.getJSONObject(BONUS_SYMBOLS).getJSONObject(SYMBOLS);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = getRandomSymbol(standardSymbols, i, j, bonusSymbols);
            }
        }
        return matrix;
    }

    /**
     * This will derive Probabilities and symbols from both 'symbols' & 'bonusSymbols'.
     * It will forward those 'symbolWeights' hash map into 'weightedRandomChoice()'
     * method to get random symbol according to the weights.
     */
    private String getRandomSymbol(JSONArray standardSymbols, int row, int col, JSONObject bonusSymbols) {


        Map<String, Integer> symbolWeights = new HashMap<>();
        JSONObject cellProbabilities = standardSymbols.optJSONObject(row * col);

        if (cellProbabilities == null) {
            cellProbabilities = standardSymbols.getJSONObject(0); // Default to first entry if not found
        }

        JSONObject symbols = cellProbabilities.getJSONObject(SYMBOLS);
        for (String key : symbols.keySet()) {
            symbolWeights.put(key, symbols.getInt(key));
        }

        for (String key : bonusSymbols.keySet()) {
            symbolWeights.put(key, bonusSymbols.getInt(key));
        }

        return weightedRandomChoice(symbolWeights);
    }

    /**
     * This will create weighted pool like bellow
     * ["A", "A", "A", "A", "A", "B", "B", "B", "C", "C"]
     * and pick and return one of them randomly
     */
    private String weightedRandomChoice(Map<String, Integer> symbolWeights) {
        List<String> pool = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : symbolWeights.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.add(entry.getKey());
            }
        }
        return pool.get(rand.nextInt(pool.size()));
    }
}
