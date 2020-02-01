package dev.vankka.jdacommands;

import dev.vankka.jdacommands.model.PrefixProvider;
import dev.vankka.jdacommands.model.command.*;
import dev.vankka.jdacommands.model.processor.CommandPreprocessor;
import dev.vankka.jdacommands.model.processor.EventPreprocessor;
import dev.vankka.jdacommands.model.processor.ResultProcessor;
import dev.vankka.jdacommands.object.Emoji;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "WeakerAccess"})
public class JDACommands implements EventPreprocessor, CommandPreprocessor, ResultProcessor, PrefixProvider {

    private final List<CommandCategory> commandCategories = new ArrayList<>();
    private final CommandListener commandListener = new CommandListener();
    private final ShardManager shardManager;
    private final JDA jda;

    private String defaultPrefix = "!";
    private boolean allowMentionAsPrefix = true;

    private EventPreprocessor eventPreprocessor = this;
    private CommandPreprocessor commandPreprocessor = this;
    private ResultProcessor resultProcessor = this;
    private PrefixProvider prefixProvider = this;

    private String botOwnerId = "";

    public JDACommands(ShardManager shardManager) {
        this.shardManager = shardManager;
        this.jda = null;

        shardManager.addEventListener(commandListener);
        reloadBotOwner();
    }

    public JDACommands(JDA jda) {
        this.shardManager = null;
        this.jda = jda;

        jda.addEventListener(commandListener);
        reloadBotOwner();
    }

    public void reloadBotOwner() {
        JDA jda = this.shardManager != null ? this.shardManager.getShardById(0) : this.jda;

        ApplicationInfo applicationInfo = Objects.requireNonNull(jda).retrieveApplicationInfo().complete();
        botOwnerId = applicationInfo.getOwner().getId();
    }

    /**
     * Gets the Bot's owner's id.
     *
     * @return The bot's owner's id
     */
    public String getBotOwnerId() {
        return botOwnerId;
    }

    /**
     * Gets the list of command categories in this JDACommands instance
     *
     * @return the list of command categories in this JDACommands instance
     */
    public List<CommandCategory> getCommandCategories() {
        return commandCategories;
    }

    /**
     * Add command categories to this JDACommands instance
     *
     * @param commandCategories command categories to be added
     */
    public void addCommandCategories(CommandCategory... commandCategories) {
        this.commandCategories.addAll(Arrays.asList(commandCategories));
    }

    /**
     * Remove command categories from this JDACommands instance
     *
     * @param commandCategories command categories to be removed
     */
    public void removeCommandCategories(CommandCategory... commandCategories) {
        this.commandCategories.removeAll(Arrays.asList(commandCategories));
    }

    /**
     * Gets the default prefix for this JDACommands instance
     *
     * @return the default prefix in this JDACommands instance
     */
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    /**
     * Sets the default prefix for this JDACommands instance
     *
     * @param defaultPrefix the new default prefix for this JDACommands instance
     */
    public void setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }

    /**
     * Whether or not @mentioning the bot/client is allowed as a prefix for commands
     *
     * @param allowMentionAsPrefix true to allow @mentioning to prefix commands
     */
    public void setAllowMentionAsPrefix(boolean allowMentionAsPrefix) {
        this.allowMentionAsPrefix = allowMentionAsPrefix;
    }

    /**
     * Get the {@link EventPreprocessor} for this JDACommands instance.
     *
     * @return the EventProcessor for this JDACommands instance
     */
    public EventPreprocessor getEventPreprocessor() {
        return eventPreprocessor;
    }

    /**
     * Set the {@link EventPreprocessor} for this JDACommands instance.
     *
     * @param eventPreprocessor the new EventProcessor for this JDACommands instance
     */
    public void setEventPreprocessor(EventPreprocessor eventPreprocessor) {
        this.eventPreprocessor = eventPreprocessor;
    }

    /**
     * Get the {@link CommandPreprocessor} for this JDACommands instance.
     *
     * @return the CommandPreprocessor for this JDACommands instance.
     */
    public CommandPreprocessor getCommandPreprocessor() {
        return commandPreprocessor;
    }

    /**
     * Set the {@link CommandPreprocessor} for this JDACommands instance.
     *
     * @param commandPreprocessor the new CommandPreprocessor for this JDACommands instance
     */
    public void setCommandPreprocessor(CommandPreprocessor commandPreprocessor) {
        this.commandPreprocessor = commandPreprocessor;
    }

    /**
     * Gets the {@link ResultProcessor} for this JDACommands instance.
     *
     * @return the ResultProcessor for this JDACommands instance.
     */
    public ResultProcessor getResultProcessor() {
        return resultProcessor;
    }

    /**
     * Sets the {@link ResultProcessor} for this JDACommands instance.
     *
     * @param resultProcessor the new ResultProcessor for this JDACommands instance.
     */
    public void setResultProcessor(ResultProcessor resultProcessor) {
        this.resultProcessor = resultProcessor;
    }

    /**
     * Gets the {@link PrefixProvider} for this JDACommands instance.
     *
     * @return the PrefixProvider for this JDACommands instance.
     */
    public PrefixProvider getPrefixProvider() {
        return prefixProvider;
    }

    /**
     * Sets the {@link PrefixProvider} for this JDACommands instance.
     *
     * @param prefixProvider the new PrefixProvider for this JDACommands instance.
     */
    public void setPrefixProvider(PrefixProvider prefixProvider) {
        this.prefixProvider = prefixProvider;
    }

    /**
     * Removes the command listener from the ShardManager or JDA instance.
     */
    @SuppressWarnings("WeakerAccess")
    public void shutdown() {
        if (shardManager != null)
            shardManager.removeEventListener(commandListener);

        if (jda != null)
            jda.removeEventListener(commandListener);
    }

    /**
     * Default {@link EventPreprocessor}
     *
     * @param event   GenericMessageEvent from JDA
     * @param message The message
     * @param author  The message author
     * @param member  The member, may be null
     * @param edited  True if it was a {@link MessageReceivedEvent}, false if it was a {@link MessageUpdateEvent}
     */
    @Override
    public void preprocessEvent(GenericMessageEvent event, Message message, User author, Member member, boolean edited) {
        String content = message.getContentRaw();

        Guild guild;
        try {
            guild = event.getGuild();
        } catch (IllegalStateException ignored) {
            guild = null;
        }

        String prefix = prefixProvider.providePrefix(guild, defaultPrefix);
        String mention = guild != null ? event.getGuild().getSelfMember()
                .getAsMention() : event.getJDA().getSelfUser().getAsMention();

        boolean mentionPrefix = content.contains(" ") && content.startsWith(mention) && allowMentionAsPrefix;
        if (!content.startsWith(prefix) && !mentionPrefix)
            return;

        List<String> arguments = new ArrayList<>(content.contains(" ")
                ? Arrays.asList(content.split(" ")) : Collections.singletonList(content));
        if (mentionPrefix)
            arguments.remove(0); // remove mention

        String cmd = mentionPrefix ? arguments.get(0) : Pattern.compile(prefix, Pattern.LITERAL)
                .matcher(arguments.get(0)).replaceFirst("");
        arguments.remove(0); // remove command

        List<Command> commands = new ArrayList<>();
        for (CommandCategory commandCategory : commandCategories)
            commands.addAll(commandCategory.getCommands());

        Command command = commands.stream()
                .filter(c -> c.getAliases().stream().anyMatch(format -> format.contains(" ")
                        ? format.split(" ")[0].equalsIgnoreCase(cmd)
                        : format.equalsIgnoreCase(cmd)))
                .findAny().orElse(null);
        if (command == null)
            return;

        CommandEvent commandEvent = new CommandEvent(event, message, author,
                member, edited, this, arguments, prefix);

        commandPreprocessor.preprocessCommand(commandEvent, command);
    }

    /**
     * Default {@link CommandPreprocessor}
     *
     * @param event   CommandEvent
     * @param command The command to execute
     */
    @Override
    public void preprocessCommand(CommandEvent event, Command command) {
        List<CommandProperty> properties = command.getProperties();
        if (properties.contains(CommandProperty.GUILD_ONLY) && event.getGuild() == null)
            return;
        if (properties.contains(CommandProperty.BOT_OWNER_ONLY) && !event.getAuthor().getId().equals(botOwnerId))
            return;

        List<Permission> missingPermissions = new ArrayList<>();
        for (Permission permission : command.getBotRequiredPermissions()) {
            if (!event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), permission))
                missingPermissions.add(permission);
        }

        if (!missingPermissions.isEmpty()) {
            resultProcessor.processMissingPermission(missingPermissions, event);
            return;
        }

        try {
            resultProcessor.processResult(command.execute(event), event);
        } catch (PermissionException exception) {
            resultProcessor.processMissingPermission(Collections.singletonList(exception.getPermission()), event);
        }
    }

    // ResultProcessor
    @Override
    public void processResult(CommandResult commandResult, CommandEvent event) {
        if (commandResult instanceof CommandResult.Error) {
            CommandResult.Error error = (CommandResult.Error) commandResult;

            error.getException().printStackTrace();
            sendMessageSafely(event, Emoji.X + " An error occurred while processing the command.");
        } else if (commandResult instanceof CommandResult.Message) {
            CommandResult.Message message = (CommandResult.Message) commandResult;

            sendMessageSafely(event, message.getMessage());
        } else if (commandResult instanceof CommandResult.Generic) {
            //noinspection SwitchStatementWithTooFewBranches
            switch ((CommandResult.Generic) commandResult) {
                case SUCCESS_CHECK_MARK:
                    handleSuccessCheckMark(event);
                    break;
            }
        }
    }

    @Override
    public void processMissingPermission(List<Permission> missingPermissions, CommandEvent event) {
        sendMessageSafely(event, Emoji.X + " Missing permission" + (missingPermissions.size() == 1 ? "" : "s")
                + ", " + "`" + missingPermissions.stream()
                .map(Permission::getName).collect(Collectors.joining(", ")) + "`");
    }

    public void handleSuccessCheckMark(CommandEvent event) {
        if (event.getChannel() instanceof TextChannel) {
            Member selfMember = event.getGuild().getSelfMember();

            if (hasPermission(event.getTextChannel(), selfMember, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION))
                event.getMessage().addReaction(Emoji.WHITE_CHECK_MARK).queue();
            else if (hasPermission(event, selfMember, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE))
                event.getChannel().sendMessage(Emoji.WHITE_CHECK_MARK).queue();

            return;
        }

        event.getMessage().addReaction(Emoji.WHITE_CHECK_MARK).queue();
    }

    public void sendMessageSafely(CommandEvent event, String message) {
        MessageChannel messageChannel = event.getChannel();
        if (!(messageChannel instanceof TextChannel)) {
            messageChannel.sendMessage(message).queue();
            return;
        }

        TextChannel textChannel = (TextChannel) messageChannel;
        Member selfMember = textChannel.getGuild().getSelfMember();

        if (hasPermission(event, selfMember, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE))
            textChannel.sendMessage(message).queue();
    }

    public boolean hasPermission(CommandEvent event, Member member, Permission... permissions) {
        TextChannel textChannel = event.getTextChannel();

        List<Permission> missingPermissions = new ArrayList<>();
        for (Permission permission : permissions) {
            if (!member.hasPermission(textChannel, permission))
                missingPermissions.add(permission);
        }

        if (missingPermissions.isEmpty())
            return true;

        String missingPermissionsMessage = Emoji.WARNING + " Missing permission" + (missingPermissions.size() == 1
                ? "" : "s") + ", `" + missingPermissions.stream().map(Permission::getName)
                .collect(Collectors.joining(", ")) + "`";

        Member selfMember = textChannel.getGuild().getSelfMember();
        if (selfMember.hasPermission(textChannel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)) {
            textChannel.sendMessage(missingPermissionsMessage).queue();
            return false;
        }

        if (selfMember.hasPermission(textChannel, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ADD_REACTION)
                && !event.getMessage().getContentRaw().contains("--error")) {
            event.getMessage().addReaction(Emoji.WARNING).queue();
            return false;
        }

        event.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(
                missingPermissionsMessage + " in " + textChannel.getAsMention()).queue());
        return false;
    }

    public boolean hasPermission(TextChannel textChannel, Member member, Permission... permissions) {
        return member.hasPermission(textChannel, permissions);
    }

    /**
     * Default {@link PrefixProvider},
     *
     * @param guild Guild context
     * @return The prefix for the guild, otherwise the defaultPrefix
     */
    @Override
    public String providePrefix(Guild guild, String defaultPrefix) {
        return defaultPrefix;
    }

    // shutdown on finalize
    @SuppressWarnings("deprecation") // Newer Java versions
    @Override
    protected void finalize() {
        shutdown();
    }

    // CommandListener, hidden to keep people from registering it twice
    private class CommandListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
            eventPreprocessor.preprocessEvent(event, event.getMessage(),
                    event.getAuthor(), event.getMember(), false);
        }

        @Override
        public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
            eventPreprocessor.preprocessEvent(event, event.getMessage(),
                    event.getAuthor(), event.getMember(), true);
        }
    }
}
