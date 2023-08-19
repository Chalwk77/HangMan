/* Copyright (c) 2023, Hangman. Jericho Crosby <jericho.crosby227@gmail.com> */

package com.chalwk;

import com.chalwk.game.Game;
import com.chalwk.listeners.CommandInterface;
import com.chalwk.listeners.CommandManager;
import com.chalwk.listeners.EventListeners;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import static com.chalwk.util.Authentication.getToken;
import static org.reflections.Reflections.log;

public class Main {

    public static Game[] games = new Game[0];
    public static String botName;
    public static String botAvatar;
    private ShardManager shardManager;

    public Main() throws LoginException, IOException {
        shardManager = buildBot();
        shardManager.addEventListener(loadCommands());
    }

    public static void main(String[] args) {
        try {
            new Main();
        } catch (LoginException | IOException e) {
            System.out.println("Failed to start bot: " + e.getMessage());
        }
    }

    public static String getBotName() {
        return botName;
    }

    public static String getBotAvatar() {
        return botAvatar;
    }

    public static Game[] addGame(Game[] games, Game game) {
        Game[] newGames = new Game[games.length + 1];
        System.arraycopy(games, 0, newGames, 0, games.length);
        newGames[games.length] = game;
        return newGames;
    }

    public static Game[] removeGame(Game[] games, Game game) {
        Game[] newGames = new Game[games.length - 1];
        int index = 0;
        for (Game g : games) {
            if (g != game) {
                newGames[index++] = g;
            }
        }
        return newGames;
    }

    @NotNull
    private CommandManager loadCommands() {
        CommandManager commands = new CommandManager();
        Reflections reflections = new Reflections("com.chalwk.commands");
        for (Class<?> commandClass : reflections.getSubTypesOf(CommandInterface.class)) {
            try {
                commands.add((CommandInterface) commandClass.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.warn("Failed to load command: " + commandClass.getName(), e, Level.WARNING);
            }
        }
        return commands;
    }

    @NotNull
    private ShardManager buildBot() throws IOException {
        String token = getToken();
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("Hangman"));
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.MESSAGE_CONTENT);

        shardManager = builder.build();
        shardManager.addEventListener(new EventListeners());
        botName = shardManager.getShards().get(0).getSelfUser().getName();
        botAvatar = shardManager.getShards().get(0).getSelfUser().getAvatarUrl();

        return shardManager;
    }
}
