# CyberSpeed Puzzle Game

## Project Overview
CyberSpeed Puzzle Game is a puzzle game that simulates a simplified slot machine or scratch card game. The player places a bet and receives a reward based on the symbols appearing in a generated matrix. The game's behavior is driven by a configuration file (likely `config.json`), which defines the game's parameters, symbol probabilities, and winning combinations.

## Key Components

### 1. `Main.java`
**Purpose:** Entry point of the application.

**Responsibilities:**
- Parses command-line arguments for the configuration file path and bet amount.
- Provides default values if no arguments are provided.
- Instantiates and starts `PuzzleGame`.
- Handles potential `IOException` and `JSONException` errors.

**Usage:**
```bash
java -jar ScratchGame-jar-with-dependencies.jar --config <path_to_config.json> --betting-amount <bet_amount>
```
If no arguments are provided, the program defaults to `src/main/resources/config.json` and a bet amount of 100.

---
### 2. `PuzzleGame.java`
**Purpose:** Orchestrates the game logic, including matrix generation, reward calculation, and output formatting.

**Responsibilities:**
- Reads and validates the configuration.
- Generates the symbol matrix.
- Calculates the reward.
- Formats and displays the output.

**Key Methods:**
- `PuzzleGame(String configPath)`: Initializes the game with the configuration file.
- `play(int betAmount)`: Executes the game flow.
- `generateMatrix()`: Creates the symbol matrix.
- `calculateReward(int betAmount)`: Determines the reward.
- `displayOutput(double reward)`: Formats and prints results.
- `validateConfig()`: Ensures configuration validity.

---
### 3. `SymbolGenerator.java`
**Purpose:** Generates a matrix of symbols based on defined probabilities.

**Responsibilities:**
- Reads symbol probabilities from the configuration.
- Uses a weighted random choice algorithm.
- Handles standard and bonus symbols.

**Key Methods:**
- `SymbolGenerator(JSONObject config, Random rand)`: Initializes the generator.
- `generateMatrix(int rows, int columns)`: Generates the symbol matrix.
- `getRandomSymbol(JSONArray standardSymbols, int row, int col, JSONObject bonusSymbols)`: Selects a random symbol.
- `weightedRandomChoice(Map symbolWeights)`: Implements the weighted selection algorithm.

---
### 4. `RewardCalculator.java`
**Purpose:** Calculates the reward based on the generated matrix, bet amount, and winning combinations.

**Responsibilities:**
- Identifies winning combinations.
- Applies multipliers and bonus effects.

**Key Methods:**
- `RewardCalculator(JSONObject config, String[][] matrix, int betAmount, Random rand)`: Initializes the calculator.
- `calculate()`: Determines the total reward.
- `checkArea(JSONArray area)`: Checks if an area contains matching symbols.
- `getSymbolFromPosition(String position)`: Extracts symbols from matrix positions.
- `applyBonusSymbols(double reward)`: Applies bonus effects.
- `applyBonusEffect(JSONObject bonus, double reward)`: Implements specific bonus effects.

**Data Structures:**
- `appliedWinningCombinations`: Stores applied winning combinations.
- `selectedBonusSymbol`: Stores the applied bonus symbol.

---
### 5. `OutputFormatter.java`
**Purpose:** Formats the game output into a JSON string.

**Responsibilities:**
- Converts the matrix, applied bonus symbol, winning combinations, and reward into JSON.
- Provides a method to display the matrix in the console.

**Key Methods:**
- `OutputFormatter(String[][] matrix, String appliedBonusSymbol, Map<String, List<String>> appliedWinningCombinations, double reward)`: Initializes the formatter.
- `formatOutput()`: Converts game data into JSON.
- `displayMatrix()`: Prints the symbol matrix to the console.

---
### 6. `ConfigValidator.java`
**Purpose:** Validates the configuration file.

**Responsibilities:**
- Ensures required keys exist.
- Validates data types and value ranges.

**Key Methods:**
- `ConfigValidator(JSONObject config)`: Initializes the validator.
- `validate()`: Performs validation.
- `requireKeys(JSONObject config, String... keys)`: Checks required keys.
- `validateRowsAndColumns()`: Ensures row/column values are positive.
- `validateSymbols()`: Validates symbol configurations.
- `validateProbabilities()`: Checks probability values.
- `validateWinCombinations()`: Ensures win combinations are correctly defined.

---
### 7. `ConfigConst.java`
**Purpose:** Defines constants for configuration keys.

**Responsibilities:**
- Centralized management of configuration keys.
- Improves readability and maintainability by avoiding hardcoded strings.

---
### 8. `PuzzleGameTest.java`

**Purpose:** Contains unit tests for the PuzzleGame class to ensure correctness of matrix generation, reward calculation, and bonus applications.

**Responsibilities:**

- Validates that losing games return a reward of zero and apply no winning combinations or bonuses.
- Tests winning scenarios with different symbol combinations and bonus applications.
- Ensures correct application of multipliers and extra bonuses.
- Uses reflection to inject predefined matrices for testing edge cases.
- Verifies that bonus symbols (e.g., 10x, +1000) impact the reward calculation correctly.
- Ensures non-impactful symbols (e.g., MISS) do not affect reward calculations incorrectly.



---
## Conclusion
CyberSpeed Puzzle Game is structured to allow modular handling of game logic, ensuring clarity, maintainability, and configurability. The architecture separates concerns across multiple classes, making the game adaptable for future enhancements.

