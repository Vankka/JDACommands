package me.vankka.jdacommands.model;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;

public class CommandEvent {

    private final GenericMessageEvent event;
    private final Message message;
    private final User author;
    private final Member member;
    private final boolean edited;

    public CommandEvent(MessageReceivedEvent event) {
        this(event, event.getMessage(), event.getAuthor(), event.getMember(), false);
    }

    public CommandEvent(MessageUpdateEvent event) {
        this(event, event.getMessage(), event.getAuthor(), event.getMember(), true);
    }

    public CommandEvent(GenericMessageEvent event, Message message, User author, Member member, boolean edited) {
        this.event = event;
        this.message = message;
        this.author = author;
        this.member = member;
        this.edited = edited;
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

    public Group getGroup() {
        return event.getGroup();
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
}
