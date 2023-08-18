/* Copyright (c) 2023, TicTacToe-JDA. Jericho Crosby <jericho.crosby227@gmail.com> */
package com.chalwk.game;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.*;

import static com.chalwk.Main.getBotAvatar;
import static com.chalwk.Main.getBotName;
import static com.chalwk.game.Globals.hangman_layout;
import static com.chalwk.game.Globals.words;
import static com.chalwk.game.PrivateMessage.privateMessage;

public class Game {

    public String challengerID;
    public String opponentID;
    public String challengerName;
    public String opponentName;
    public String whos_turn;
    public int correct;
    public boolean started = false;
    public int gameID;
    public int state;
    public String word;
    public String[] layout;
    private Guild guild;
    private String stage;
    private String embedID;

    public Game(SlashCommandInteractionEvent event, OptionMapping layout, String challengerID, String opponentID) {
        setGuild(event);
        setCompetitors(challengerID, opponentID);
        setLayout(layout.getAsInt());
    }

    private void setGuild(SlashCommandInteractionEvent event) {
        this.guild = event.getGuild();
    }

    private void setCompetitors(String challengerID, String opponentID) {
        this.challengerID = challengerID;
        this.opponentID = opponentID;
        this.challengerName = guild.getMemberById(challengerID).getEffectiveName();
        this.opponentName = guild.getMemberById(opponentID).getEffectiveName();
    }

    public void showSubmission(SlashCommandInteractionEvent event) {
        this.state = 0;
        setStage(this.state); // dead hangman

        EmbedBuilder embed = getEmbed();
        embed.setDescription("You have been invited to play Hangman.");

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.success("accept", "\uD83D\uDFE2 Accept"));
        buttons.add(Button.danger("decline", "\uD83D\uDD34 Decline"));
        buttons.add(Button.secondary("cancel", "\uD83D\uDEAB Cancel"));
        event.replyEmbeds(embed.build()).addActionRow(buttons).queue();
    }

    public EmbedBuilder getEmbed() {

        String botName = getBotName();
        String botAvatar = getBotAvatar();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("\uD83D\uDD74 \uD80C\uDF6F Hangman \uD80C\uDF6F \uD83D\uDD74");
        embed.addField("Challenger:", "<@" + this.challengerID + ">", true);
        embed.addField("Opponent:", "<@" + this.opponentID + ">", true);
        embed.addField("Hangman:", printHangman(), false);
        embed.setFooter(botName + " - Copyright (c) 2023. Jericho Crosby", botAvatar);
        embed.setColor(0x000000);
        return embed;
    }

    private String whoStarts() {
        Random random = new Random();
        int randomNum = random.nextInt(2);
        return (randomNum == 0) ? this.challengerName : this.opponentName;
    }

    public void setTurn() {
        this.whos_turn = (this.whos_turn.equals(this.challengerName)) ? this.opponentName : this.challengerName;
    }

    public String getTurn() {
        return this.whos_turn;
    }

    private void initializeGame(ButtonInteractionEvent event) {
        newRandomWord();

        this.started = true;
        this.state = layout.length - 1;
        this.whos_turn = whoStarts();

        setStage(this.state);
        EmbedBuilder embed = getEmbed();
        embed.setDescription("The game has started. " + this.whos_turn + " goes first.");
        embed.addField("Guess a letter or the word: " + word.length() + " characters", "", false);
        embed.addField("Characters:", "```" + "〔 〕".repeat(word.length()) + "```", false);
        event.replyEmbeds(embed.build()).queue();
        setMessageID(event);
    }

    private void setMessageID(ButtonInteractionEvent event) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                setEmbedID(event.getChannel().getLatestMessageId());
            }
        }, 500);
    }

    void acceptInvitation(ButtonInteractionEvent event) {
        event.getMessage().delete().queue();
        initializeGame(event);
    }

    void declineInvitation(ButtonInteractionEvent event, Member member) {
        privateMessage(event, member, "Your game invite to " + this.opponentName + " was declined.");
        event.getMessage().delete().queue();
    }

    void cancelInvitation(ButtonInteractionEvent event, Member member) {
        privateMessage(event, member, "Your game invite to " + this.opponentName + " was cancelled.");
        event.getMessage().delete().queue();
    }

    public int getGameID() {
        return this.gameID;
    }

    public String getEmbedID() {
        return this.embedID;
    }

    private void setEmbedID(String embedID) {
        this.embedID = embedID;
    }

    private void newRandomWord() {
        this.word = words[new Random().nextInt(words.length)];
        System.out.println("Word: " + this.word);
    }

    void setStage(int stage) {
        if (stage < 0) stage = 0; // just in case
        System.out.println("Setting stage to " + stage);
        this.stage = this.layout[stage];
    }

    private void setLayout(int layoutIndex) {
        if (layoutIndex < 0) layoutIndex = 0; // just in case
        this.layout = hangman_layout[layoutIndex];
    }

    String printHangman() {
        return "```" + this.stage + "```";
    }
}
