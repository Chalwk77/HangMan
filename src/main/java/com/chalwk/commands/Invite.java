/* Copyright (c) 2023, Hangman. Jericho Crosby <jericho.crosby227@gmail.com> */

package com.chalwk.commands;

import com.chalwk.game.Game;
import com.chalwk.listeners.CommandInterface;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

import static com.chalwk.game.Globals.concurrentGames;
import static com.chalwk.game.Globals.hangman_layout;
import static com.chalwk.game.PrivateMessage.privateMessage;

public class Invite implements CommandInterface {

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Invite someone to play Hangman with you.";
    }

    @Override
    public List<OptionData> getOptions() {
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "opponent", "The user you want to invite.").setRequired(true));
        OptionData option = new OptionData(OptionType.INTEGER, "layout", "The hangman layout you want to use.");
        for (int i = 0; i < hangman_layout.length; i++) {
            option.addChoice("Layout: " + (i + 1), i);
        }
        option.setRequired(true);
        options.add(option);
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        Member member = event.getMember();
        OptionMapping option = event.getOption("opponent");
        OptionMapping layout = event.getOption("layout");

        assert member != null;
        assert option != null;

        if (option.getAsUser().isBot()) {
            privateMessage(event, member, "You cannot invite a bot to play Hangman.");
        } else if (event.getUser().getId().equals(option.getAsUser().getId())) {
            privateMessage(event, member, "You cannot invite yourself to play Hangman.");
        } else {
            invitePlayer(event, option, layout);
        }
    }

    private void invitePlayer(SlashCommandInteractionEvent event, OptionMapping option, OptionMapping layout) {

        String challengerID = event.getUser().getId();
        String opponentID = option.getAsUser().getId();

        int length = concurrentGames.length;
        Game[] temp = new Game[length + 1];
        System.arraycopy(concurrentGames, 0, temp, 0, length);

        concurrentGames = temp;
        concurrentGames[length] = new Game(event, layout, challengerID, opponentID);
        concurrentGames[length].gameID = length;
        concurrentGames[length].showSubmission(event);
    }
}
