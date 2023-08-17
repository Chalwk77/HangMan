/* Copyright (c) 2023, TicTacToe-JDA. Jericho Crosby <jericho.crosby227@gmail.com> */
package com.chalwk.game;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

import static com.chalwk.Main.getBotAvatar;
import static com.chalwk.Main.getBotName;
import static com.chalwk.game.Globals.concurrentGames;
import static com.chalwk.game.Globals.guesses;

public class OnMessage {
    public static void onMessage(MessageReceivedEvent event) {

        Member member = event.getMember();
        String memberID = member.getId();
        String input = event.getMessage().getContentRaw();

        for (Game game : concurrentGames) {
            if (game == null) continue;

            String word = game.word;

            String challengerID = game.challengerID;
            String opponentID = game.opponentID;

            if (memberID.equals(challengerID) || memberID.equals(opponentID) && game.started) {

                // Player is attempting to guess the whole word:
                if (input.length() > 1) {
                    if (input.contentEquals(word)) {
                        // you guessed the word / game over
                    } else {
                        game.setStage(0);
                    }
                    // Player is attempting to guess a letter:
                } else if (!getGuesses(input, new StringBuilder(word), guesses)) {
                    game.setStage(0);
                }

                updateEmbed(new StringBuilder(word), guesses, game);
            }
        }
    }

    private static void updateEmbed(StringBuilder word, List<Character> guesses, Game game) {

        String guessBox = guessBox(word, guesses);
        String botName = getBotName();
        String botAvatar = getBotAvatar();
        EmbedBuilder embed = game.getEmbed();

        embed.setTitle("\uD83D\uDD74 \uD80C\uDF6F Hangman \uD80C\uDF6F \uD83D\uDD74");
        embed.addField("Challenger:", "<@" + game.challengerID + ">", true);
        embed.addField("Opponent:", "<@" + game.opponentID + ">", true);
        embed.addField("Hangman:", game.printHangman(), false);
        embed.addField("Characters:", "```" + guessBox + "```", false);
        embed.setFooter(botName + " - Copyright (c) 2023. Jericho Crosby", botAvatar);
    }

    private static String guessBox(StringBuilder word, List<Character> guesses) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char guess = word.charAt(i);
            if (guesses.contains(guess)) {
                sb.append("〔").append(guess).append("〕");
            } else {
                sb.append("〔 〕");
            }
        }
        return sb.toString();
    }

    private static boolean getGuesses(String input, StringBuilder word, List<Character> guesses) {
        char guess = input.charAt(0);
        guesses.add(guess);
        return word.toString().contains(input);
    }
}
