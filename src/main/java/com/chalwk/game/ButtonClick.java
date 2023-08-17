/* Copyright (c) 2023, TicTacToe-JDA. Jericho Crosby <jericho.crosby227@gmail.com> */
package com.chalwk.game;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import static com.chalwk.game.Globals.concurrentGames;
import static com.chalwk.game.PrivateMessage.privateMessage;

public class ButtonClick {
    public static void buttonClick(ButtonInteractionEvent event) {

        buttonData data = getButtonData(event);

        for (Game game : concurrentGames) {
            if (game == null) continue;

            String challengerID = game.challengerID;
            String opponentID = game.opponentID;

            if (data.memberID().equals(challengerID) || data.memberID().equals(opponentID)) {
                if (!game.started) {
                    if (data.buttonID.equalsIgnoreCase("accept")) {
                        game.acceptInvitation(event);
                    } else if (data.buttonID.equalsIgnoreCase("decline")) {
                        if (canClick(data, opponentID, event, "You are not the opponent. Unable to decline."))
                            continue;
                        game.declineInvitation(event, data.member);
                    } else if (data.buttonID.equalsIgnoreCase("cancel")) {
                        if (canClick(data, challengerID, event, "You are not the challenger. Unable to cancel."))
                            continue;
                        game.cancelInvitation(event, data.member);
                    }
                }
            } else {
                privateMessage(event, data.member, "You are not part of this game.");
            }
        }
    }

    private static boolean canClick(buttonData button, String playerID, ButtonInteractionEvent event, String message) {
        if (!button.memberID.equals(playerID)) {
            privateMessage(event, button.member, message);
            return true;
        }
        return false;
    }

    @NotNull
    private static buttonData getButtonData(ButtonInteractionEvent event) {
        Member member = event.getMember();
        String memberID = member.getId();
        Button button = event.getComponent();
        String buttonID = button.getId();
        return new buttonData(member, memberID, button, buttonID);
    }

    private record buttonData(Member member, String memberID, Button button, String buttonID) {

    }
}
