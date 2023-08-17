/* Copyright (c) 2023, Hangman. Jericho Crosby <jericho.crosby227@gmail.com> */
package com.chalwk.game;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import static com.chalwk.game.Globals.concurrentGames;

public class GameOver {

    public static boolean gameOver(Game game, MessageReceivedEvent event) {

        // Check for:

        // 1. Correct letter in word (input.length() == 1).
        // 2. Guessed whole word (input.length() > 1).
        // 3. Guessed all letters in word (guesses == word.length()).

        concurrentGames[game.getGameID()] = null;
        return false;
    }
}
