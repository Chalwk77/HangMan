/* Copyright (c) 2023, Hangman. Jericho Crosby <jericho.crosby227@gmail.com> */

package com.chalwk.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * Sets up abstract methods for commands that will be used in the CommandManager class.
 */
public interface CommandInterface {

    // Returns the name of the command:
    String getName();

    // Returns the description of the command:
    String getDescription();

    // Returns the options for the command:
    List<OptionData> getOptions();

    // Executes the command:
    void execute(SlashCommandInteractionEvent event);
}
