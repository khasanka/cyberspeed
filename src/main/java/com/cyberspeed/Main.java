package com.cyberspeed;

import com.cyberspeed.PuzzleGame;
import org.json.JSONException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, JSONException {

        // default values
        String configPath = "src/main/resources/config.json";
        int betAmount = 100;

        if (args.length < 2) {
            System.out.println(" >>>> Usage: java -jar ScratchGame-jar-with-dependencies.jar  --config <configPath> --betting-amount <amount> <<<<");
            System.out.println(
                    " >>>> Running program using default config.\n" +
                    " >>>> betAmount : " + betAmount + "\n" +
                    " >>>> configPath : " + configPath
            );
        } else {
            configPath = args[1];
            betAmount = Integer.parseInt(args[3]);
        }

        PuzzleGame game = new PuzzleGame(configPath);
        game.play(betAmount);

    }
}

