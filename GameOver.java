public class GameOver {
    public static void drawGameOver(Game game) {
        game.changeBackgroundColor(game.black);
        game.clearBackground(game.width(), game.height());

        int centerX = game.width() / 2;

        // Determine if the game is won or lost
        boolean isGameWon = game.targetWord.equals(new String(game.letters[game.currentLine - 1]));

        // Draw game result message
        if (isGameWon) {
            game.changeColor(game.green);
            game.drawCenteredBoldText(centerX, 200, "Congratulations!", "Arial", 40);
            game.drawCenteredBoldText(centerX, 240, "You guessed the word!", "Arial", 20);
        } else {
            game.changeColor(game.red);
            game.drawCenteredBoldText(centerX, 200, "Game Over", "Arial", 40);
            game.drawCenteredBoldText(centerX, 240, "The word was: " + game.targetWord, "Arial", 20);
        }

        // Draw remaining options after each guess
        game.changeColor(game.white);
        int yOffsetStart = 300;
        int yOffsetIncrement = 25;
        int remainingGuesses = game.remainingOptionsAfterEachGuess.size() - 1;
        int yOffset = yOffsetStart;

        for (int i = 0; i < remainingGuesses; i++) {
            int remainingOptions = game.remainingOptionsAfterEachGuess.get(i);
            String optionText = remainingOptions == 1 ? " option remained" : " options remained";
            game.drawCenteredBoldText(centerX, yOffset, "After guess " + (i + 1) + ": " + remainingOptions + optionText, "Arial", 20);
            yOffset += yOffsetIncrement;
        }

        // Draw restart and menu instructions
        yOffset += 20; // Add some extra space before the instructions
        game.drawCenteredBoldText(centerX, yOffset, "Press R to Restart", "Arial", 20);
        game.drawCenteredBoldText(centerX, yOffset + 50, "Press M for Menu", "Arial", 20);
    }
}
