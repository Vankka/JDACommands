package dev.vankka.jdacommands.model.command;

import dev.vankka.jdacommands.JDACommands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

import java.util.List;

@SuppressWarnings("unused")
public class CommandEvent {

    private final GenericMessageEvent event;
    private final Message message;
    private final User author;
    private final Member member;
    private final boolean edited;
    private final JDACommands jdaCommands;
    private final List<String> arguments;
    private final String prefix;

    public CommandEvent(MessageReceivedEvent event, JDACommands jdaCommands, List<String> arguments, String prefix) {
        this(event, event.getMessage(), event.getAuthor(), event.getMember(), false,
                jdaCommands, arguments, prefix);
    }

    public CommandEvent(MessageUpdateEvent event, JDACommands jdaCommands, List<String> arguments, String prefix) {
        this(event, event.getMessage(), event.getAuthor(), event.getMember(), true,
                jdaCommands, arguments, prefix);
    }

    public CommandEvent(GenericMessageEvent event, Message message, User author, Member member,
                        boolean edited, JDACommands jdaCommands, List<String> arguments, String prefix) {
        this.event = event;
        this.message = message;
        this.author = author;
        this.member = member;
        this.edited = edited;
        this.jdaCommands = jdaCommands;
        this.arguments = arguments;
        this.prefix = prefix;
    }

    public JDA getJDA() {
        return event.getJDA();
    }

    public MessageChannel getChannel() {
        return event.getChannel();
    }

    public String getMessageId() {
        return event.getMessageId();
    }

    public long getMessageIdLong() {
        return event.getMessageIdLong();
    }

    public boolean isFromType(ChannelType channelType) {
        return event.isFromType(channelType);
    }

    public ChannelType getChannelType() {
        return event.getChannelType();
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public TextChannel getTextChannel() {
        return event.getTextChannel();
    }

    public PrivateChannel getPrivateChannel() {
        return event.getPrivateChannel();
    }

    public boolean isWebhookMessage() {
        return message.isWebhookMessage();
    }

    public GenericMessageEvent getEvent() {
        return this.event;
    }

    public Message getMessage() {
        return this.message;
    }

    public User getAuthor() {
        return this.author;
    }

    public Member getMember() {
        return this.member;
    }

    public boolean isEdited() {
        return this.edited;
    }

    /**
     * Provided by JDACommands
     *
     * @return the instance of JDACommands this even came from
     */
    public JDACommands getJdaCommands() {
        return jdaCommands;
    }

    /**
     * Provided by JDACommands
     *
     * @return the list of arguments given when running this command
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Provided by JDACommands
     *
     * @return the prefix for the current guild, the command maybe have been executed with a mention prefix
     */
    public String getPrefix() {
        return prefix;
    }
}
