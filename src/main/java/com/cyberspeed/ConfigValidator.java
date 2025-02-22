package com.cyberspeed;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.cyberspeed.ConfigConst.*;

public class ConfigValidator {

    static final String INTEGER = "Integer";
    static final String MUST_BE_AN_INTEGER = " must be an integer";
    static final String MUST_BE_POSITIVE = " must be positive";
    static final String CANNOT_BE_EMPTY = " cannot be empty";

    private JSONObject config;
    private List<String> errors;

    public ConfigValidator(JSONObject config) {
        this.config = config;
        this.errors = new ArrayList<>();
    }

    public void validate() {

        requireKeys(config, ROWS, COLUMNS, SYMBOLS, PROBABILITIES, WIN_COMBINATIONS);

        validateRowsAndColumns();

        validateSymbols();

        validateProbabilities();

        validateWinCombinations();

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.toString());
        }

    }

    private void validateRowsAndColumns() {
        if (!config.get(ROWS).getClass().getSimpleName().equals(INTEGER)) {
            errors.add(ROWS + MUST_BE_AN_INTEGER);
        }
        if (!config.get(COLUMNS).getClass().getSimpleName().equals(INTEGER)) {
            errors.add(COLUMNS + MUST_BE_AN_INTEGER);
        }

        try {
            int rows = config.getInt(ROWS);
            int columns = config.getInt(COLUMNS);
            if (rows <= 0) errors.add(ROWS + MUST_BE_POSITIVE);
            if (columns <= 0) errors.add(COLUMNS + MUST_BE_POSITIVE);
        } catch (Exception e) {
            errors.add("Invalid rows or columns value.");
        }
    }

    private void validateSymbols() {
        JSONObject symbols = config.getJSONObject(SYMBOLS);
        if (symbols.isEmpty()) {
            errors.add(SYMBOLS + CANNOT_BE_EMPTY);
            return;
        }

        for (String symbolKey : symbols.keySet()) {
            JSONObject symbol = symbols.getJSONObject(symbolKey);
            if (!symbol.has(TYPE)) {
                errors.add("Symbol " + symbolKey + " is missing the 'type' field.");
                continue;
            }

            String type = symbol.getString(TYPE);
            if ("standard".equals(type)) {
                if (!symbol.has(REWARD_MULTIPLIER)) {
                    errors.add("Symbol " + symbolKey + " is missing the 'reward_multiplier' field.");
                } else if (!(symbol.get(REWARD_MULTIPLIER) instanceof Number)) {
                    errors.add("Symbol " + symbolKey + " reward_multiplier must be a number.");
                } else if (symbol.getDouble(REWARD_MULTIPLIER) <= 0) {
                    errors.add("Symbol " + symbolKey + " reward_multiplier must be positive.");
                }
            } else if (BONUS.equals(type)) {
                if (!symbol.has(IMPACT)) {
                    errors.add("Symbol " + symbolKey + " is missing the 'impact' field.");
                } else {
                    String impact = symbol.getString(IMPACT);
                    if (impact.equals(MULTIPLY_REWARD)) {
                        if (!symbol.has(REWARD_MULTIPLIER)) {
                            errors.add("Symbol " + symbolKey + " is missing the 'reward_multiplier' field.");
                        } else if (!(symbol.get(REWARD_MULTIPLIER) instanceof Number)) {
                            errors.add("Symbol " + symbolKey + " reward_multiplier must be a number.");
                        }
                    } else if (impact.equals(EXTRA_BONUS)) {
                        if (!symbol.has(EXTRA)) {
                            errors.add("Symbol " + symbolKey + " is missing the 'extra' field.");
                        } else if (!symbol.get(EXTRA).getClass().getSimpleName().equals(INTEGER)) {
                            errors.add("Symbol " + symbolKey + " extra must be an integer.");
                        }
                    } else if (!impact.equals("miss")) {
                        errors.add("Invalid bonus impact: " + impact + " for symbol " + symbolKey);
                    }
                }
            } else {
                errors.add("Invalid symbol type: " + type + " for symbol " + symbolKey);
            }
        }
    }

    private void validateProbabilities() {
        JSONArray standardSymbolsProbabilities = config.getJSONObject(PROBABILITIES).getJSONArray(STANDARD_SYMBOLS);
        if (standardSymbolsProbabilities.isEmpty()) {
            errors.add(PROBABILITIES + CANNOT_BE_EMPTY);
            return;
        }

        for (int i = 0; i < standardSymbolsProbabilities.length(); i++) {
            JSONObject probability = standardSymbolsProbabilities.getJSONObject(i);
            if (!probability.has(COLUMN)) {
                errors.add("Probability at index " + i + " is missing the 'column' field.");
            } else if (!probability.get(COLUMN).getClass().getSimpleName().equals(INTEGER)) {
                errors.add("Probability at index " + i + " column must be an integer.");
            }

            if (!probability.has(ROW)) {
                errors.add("Probability at index " + i + " is missing the 'row' field.");
            } else if (!probability.get(ROW).getClass().getSimpleName().equals(INTEGER)) {
                errors.add("Probability at index " + i + " row must be an integer.");
            }

            if (!probability.has(SYMBOLS)) {
                errors.add("Probability at index " + i + " is missing the 'symbols' field.");
            } else {
                JSONObject symbolProbabilities = probability.getJSONObject(SYMBOLS);
                if (symbolProbabilities.isEmpty()) {
                    errors.add("Probability at index " + i + " symbols cannot be empty.");
                } else {
                    for (String symbolKey : symbolProbabilities.keySet()) {
                        if (!config.getJSONObject(SYMBOLS).has(symbolKey)) {
                            errors.add("Symbol " + symbolKey + " in probability at index " + i + " not found in symbols list.");
                        } else if (!symbolProbabilities.get(symbolKey).getClass().getSimpleName().equals(INTEGER)) {
                            errors.add("Symbol " + symbolKey + " probability at index " + i + " must be an integer.");
                        }
                    }
                }
            }
        }
    }

    private void validateWinCombinations() {
        JSONObject winCombinations = config.getJSONObject(WIN_COMBINATIONS);
        if (winCombinations.isEmpty()) {
            errors.add(WIN_COMBINATIONS + CANNOT_BE_EMPTY);
            return;
        }

        for (String combinationKey : winCombinations.keySet()) {
            JSONObject combination = winCombinations.getJSONObject(combinationKey);
            if (!combination.has(REWARD_MULTIPLIER)) {
                errors.add("Combination " + combinationKey + " is missing the 'reward_multiplier' field.");
            }
            if (!combination.has(WHEN)) {
                errors.add("Combination " + combinationKey + " is missing the 'when' field.");
            } else {
                String when = combination.getString(WHEN);
                if (when.equals(SAME_SYMBOLS)) {
                    if (!combination.has(COUNT)) {
                        errors.add("Combination " + combinationKey + " is missing the 'count' field.");
                    } else if (!combination.get(COUNT).getClass().getSimpleName().equals(INTEGER)) {
                        errors.add("Combination " + combinationKey + " count must be an integer.");
                    }
                } else if (when.equals(LINEAR_SYMBOLS)) {
                    if (!combination.has(COVERED_AREAS)) {
                        errors.add("Combination " + combinationKey + " is missing the 'covered_areas' field.");
                    } else {
                        JSONArray coveredAreas = combination.getJSONArray(COVERED_AREAS);
                        if (coveredAreas.isEmpty()) {
                            errors.add("Combination " + combinationKey + " covered_areas cannot be empty.");
                        } else {
                            for (int i = 0; i < coveredAreas.length(); i++) {
                                Object area = coveredAreas.get(i);
                                if (!(area instanceof JSONArray)) {
                                    errors.add("Combination " + combinationKey + " covered_areas must be an array of arrays.");
                                } else {
                                    JSONArray areaArray = (JSONArray) area;
                                    for (int j = 0; j < areaArray.length(); j++) {
                                        if (!(areaArray.get(j) instanceof String)) {
                                            errors.add("Combination " + combinationKey + " covered_areas must contain strings in the format 'column:row'.");
                                        } else {
                                            String[] pos = areaArray.getString(j).split(":");
                                            if (pos.length != 2) {
                                                errors.add("Combination " + combinationKey + " covered_areas position must be in the format 'column:row'.");
                                            } else {
                                                try {
                                                    int row = Integer.parseInt(pos[0]);
                                                    int col = Integer.parseInt(pos[1]);
                                                    if (row < 0 || row >= config.getInt(ROWS) || col < 0 || col >= config.getInt(COLUMNS)) {
                                                        errors.add("Combination " + combinationKey + " covered_areas position " + areaArray.getString(j) + " is out of bounds.");
                                                    }
                                                } catch (NumberFormatException e) {
                                                    errors.add("Combination " + combinationKey + " covered_areas position must contain integers.");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    errors.add("Invalid winning combination 'when' value: " + when + " for combination " + combinationKey);
                }
            }
        }
    }

    private void requireKeys(JSONObject json, String... keys) {
        for (String key : keys) {
            if (!json.has(key)) {
                errors.add("Missing key: " + key);
            }
        }
    }
}