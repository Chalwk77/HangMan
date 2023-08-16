/* Copyright (c) 2023, TicTacToe-JDA. Jericho Crosby <jericho.crosby227@gmail.com> */
package com.chalwk.game;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class PrivateMessage {

    public static <T> void privateMessage(T event, Member member, String Message) {

        Guild guild = (event instanceof ButtonInteractionEvent)
                ? ((ButtonInteractionEvent) event).getGuild()
                : ((SlashCommandInteraction) event).getGuild();

        assert guild != null;
        guild.retrieveMemberById(member.getId()).queue(user -> user.getUser()
                .openPrivateChannel()
                .queue(privateChannel -> privateChannel.sendMessage(Message).queue()));
    }
}
