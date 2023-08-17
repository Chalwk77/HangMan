/* Copyright (c) 2023, TicTacToe-JDA. Jericho Crosby <jericho.crosby227@gmail.com> */
package com.chalwk.game;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

import static com.chalwk.game.GameOver.gameOver;
import static com.chalwk.game.Globals.concurrentGames;
import static com.chalwk.game.Globals.guesses;

public class OnMessage {
    public static void onMessage(MessageReceivedEvent event) {

        Member member = event.getMember();
        String memberID = member.getId();

        for (Game game : concurrentGames) {
            if (game == null) continue;

            String challengerID = game.challengerID;
            String opponentID = game.opponentID;

            if (memberID.equals(challengerID) || memberID.equals(opponentID) && game.started) {

                String description = null;
                String input = event.getMessage().getContentRaw();
                String word = game.word;
                //
                // Player is attempting to guess the whole word:
                //
                if (input.length() > 1) {
                    if (input.contentEquals(word)) {
                        description = member.getEffectiveName() + " guessed the word!";
                    } else {
                        description = member.getEffectiveName() + ", that is not the word.";
                    }
                } else if (!getGuesses(input, new StringBuilder(word), guesses)) {
                    description = member.getEffectiveName() + ", (" + input + ") is not in the word.";
                }

                game.state--;
                if (guesses.size() == word.length()) {
                    // you win
                } else if (game.state < 0) {
                    // you lose
                }

                game.setStage(game.state--);
                updateEmbed(new StringBuilder(word), guesses, game, event, description);
            }
        }
    }

    private static void updateEmbed(StringBuilder word, List<Character> guesses, Game game, MessageReceivedEvent event, String description) {
        event.getMessage().delete().queue(); // delete the player's message input
        String messageID = game.getEmbedID();

        String whos_turn = (game.whos_turn.equals(game.opponentName)) ? game.challengerName : game.opponentName;

        EmbedBuilder embed = game.getEmbed();
        embed.addField("Guess a letter or the word:", word.length() + " characters", false);
        embed.addField("Characters:", guessBox(word, guesses), false);

        description = (description == null) ? "It is " + whos_turn + "'s turn." : description;
        embed.setDescription(description);

        StringBuilder sb = new StringBuilder();
        for (Character guess : guesses) {
            sb.append(guess).append(" ");
        }
        embed.addField("Guesses:", sb.toString(), false);
        event.getChannel()
                .retrieveMessageById(messageID)
                .queue(message -> message.editMessageEmbeds(embed.build()).queue());
    }

    private static String guessBox(StringBuilder word, List<Character> guesses) {
        StringBuilder sb = new StringBuilder();
        sb.append("```");
        for (int i = 0; i < word.length(); i++) {
            char guess = word.charAt(i);
            if (guesses.contains(guess)) {
                sb.append("〔").append(guess).append("〕");
            } else {
                sb.append("〔 〕");
            }
        }
        sb.append("```");
        return sb.toString();
    }

    private static boolean getGuesses(String input, StringBuilder word, List<Character> guesses) {
        char guess = input.charAt(0);
        guesses.add(guess);
        return word.toString().contains(input);
    }
}
