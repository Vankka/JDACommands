package dev.vankka.jdacommands.model.processor;

import dev.vankka.jdacommands.JDACommands;
import dev.vankka.jdacommands.model.command.Command;
import dev.vankka.jdacommands.model.command.CommandEvent;

/**
 * Interface for processing commands, checking if properties are met, the bot has the right permissions, etc.
 * A default processor is provided by {@link JDACommands}
 */
public interface CommandPreprocessor {
    /**
     * Process the CommandEvent to run the Command.
     * Should call the {@link ResultProcessor} after the command has been executed.
     *
     * @param event The event this execution originated from
     * @param command The command to be executed by this preprocessor
     */
    void preprocessCommand(CommandEvent event, Command command);
}
