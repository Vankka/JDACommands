package dev.vankka.jdacommands.model.processor;

import dev.vankka.jdacommands.JDACommands;
import dev.vankka.jdacommands.model.command.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

/**
 * The event preprocessor interface for checking if the prefix matches, parsing the arguments etc.
 * A default processor is provided by {@link JDACommands}
 */
public interface EventPreprocessor {
    /**
     * Processes the incoming event, checks if the prefix matches & looks for a
     * {@link Command} to run, if a match is
     * found run it with the provided details & parsed arguments.
     * Should call the {@link CommandPreprocessor} if the command should be processed.
     *
     * @param event   GenericMessageEvent from JDA
     * @param message The message
     * @param author  The message author
     * @param member  The member, may be null
     * @param edited  True if it was a {@link MessageReceivedEvent}, false if it was a {@link MessageUpdateEvent}
     */
    void preprocessEvent(GenericMessageEvent event, Message message, User author, Member member, boolean edited);
}
