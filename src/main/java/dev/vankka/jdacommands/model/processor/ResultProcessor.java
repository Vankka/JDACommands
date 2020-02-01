package dev.vankka.jdacommands.model.processor;

import dev.vankka.jdacommands.model.command.Command;
import dev.vankka.jdacommands.model.command.CommandResult;
import dev.vankka.jdacommands.model.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.List;

/**
 * The interface for processing results & missing permissions.
 */
public interface ResultProcessor {
    /**
     * Processes the result of a {@link Command}
     *
     * @param commandResult The result of the Command
     * @param event The event the command originated from
     */
    void processResult(CommandResult commandResult, CommandEvent event);

    /**
     * Processes missing permissions. Should send messages, or even
     * direct messages to inform users that the bot is missing permissions.
     *
     * @param missingPermissions The list of missing permissions, should never be empty.
     * @param event The context for sending messages & where the permissions are missing.
     */
    void processMissingPermission(List<Permission> missingPermissions, CommandEvent event);
}
