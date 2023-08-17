/* Copyright (c) 2023, TicTacToe-JDA. Jericho Crosby <jericho.crosby227@gmail.com> */
package com.chalwk.game;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

import static com.chalwk.game.Globals.concurrentGames;
import static com.chalwk.game.Globals.guesses;

public class OnMessage {
    public static void onMessage(MessageReceivedEvent event) {

        Member member = event.getMember();
        if (member == null) return;
        String memberID = member.getId();

        for (Game game : concurrentGames) {
            if (game == null) continue;

            String challengerID = game.challengerID;
            String opponentID = game.opponentID;

            if (memberID.equals(challengerID) || memberID.equals(opponentID) && game.started) {

                if (!yourTurn(event, game, member)) return;

                String description;
                String input = event.getMessage().getContentRaw();
                String word = game.word;

                if (input.length() > 1) {
                    if (input.contentEquals(word)) {
                        description = "✅ " + member.getEffectiveName() + " guessed the word!";
                    } else {
                        description = "❌ " + member.getEffectiveName() + " guessed the word incorrectly.";
                        game.state--;
                    }
                } else if (!getGuesses(input, new StringBuilder(word), guesses)) {
                    description = "❌ " + member.getEffectiveName() + ", (" + input + ") is not in the word.";
                    game.state--;
                } else {
                    description = "✅ " + member.getEffectiveName() + ", (" + input + ") is in the word.";
                }

                game.setStage(game.state);
                updateEmbed(new StringBuilder(word), guesses, game, event, description);
            }
        }
    }

    private static boolean yourTurn(MessageReceivedEvent event, Game game, Member member) {
        if (!member.getEffectiveName().equals(game.whos_turn)) {
            event.getMessage().delete().queue();
            return false;
        }
        return true;
    }

    private static void updateEmbed(StringBuilder word, List<Character> guesses, Game game, MessageReceivedEvent event, String description) {

        game.setTurn();
        event.getMessage().delete().queue();

        EmbedBuilder embed = game.getEmbed();
        embed.setDescription("It's now " + game.whos_turn + "'s turn.");
        embed.addField("Characters:", guessBox(word, guesses), false);
        embed.addField(description, " ", false);

        showGuesses(guesses, embed);
        editEmbed(game, event, embed);
    }

    private static void showGuesses(List<Character> guesses, EmbedBuilder embed) {
        StringBuilder sb = new StringBuilder();
        for (Character guess : guesses) {
            sb.append(guess).append(" ");
        }
        embed.addField("Guesses:", sb.toString(), false);
    }

    private static void editEmbed(Game game, MessageReceivedEvent event, EmbedBuilder embed) {
        event.getChannel()
                .retrieveMessageById(game.getEmbedID())
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

    private static boolean getGuesses(String character, StringBuilder word, List<Character> guesses) {
        char guess = character.charAt(0);
        guesses.add(guess);
        return word.toString().contains(character);
    }
}
