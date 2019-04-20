package me.vankka.jdacommands;

import me.vankka.jdacommands.model.*;
import me.vankka.jdacommands.object.Emoji;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "WeakerAccess"})
public class JDACommands {

    private final List<CommandCategory> commandCategories = new ArrayList<>();
    private final CommandListener commandListener = new CommandListener();
    private final ShardManager shardManager;
    private final JDA jda;

    private String defaultPrefix = "!";
    private boolean allowMentionAsPrefix = true;
    private Consumer<CommandEvent> commandEventProcessor = this::defaultEventProcessor;
    private Consumer<Exception> errorHandler = this::defaultErrorHandler;
    private Function<Guild, String> prefixProvider = guild -> defaultPrefix;

    private String ownerId = "";

    public JDACommands(ShardManager shardManager) {
        this.shardManager = shardManager;
        this.jda = null;

        shardManager.addEventListener(commandListener);
        reloadBotOwners();
    }

    public JDACommands(JDA jda) {
        this.shardManager = null;
        this.jda = jda;

        jda.addEventListener(commandListener);
        reloadBotOwners();
    }

    public void reloadBotOwners() {
        JDA jda = this.shardManager != null ? this.shardManager.getShardById(0) : this.jda;

        ApplicationInfo applicationInfo = jda.asBot().getApplicationInfo().complete();
        ownerId = applicationInfo.getOwner().getId();
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
     * Puts the command through the commandEventProcessor in this JDACommands instance
     *
     * @param commandEvent CommandEvent instance
     */
    public void executeCommand(CommandEvent commandEvent) {
        commandEventProcessor.accept(commandEvent);
    }

    /**
     * Sets the commandEventProcessor for this JDACommands instance
     *
     * @param commandEventProcessor the new commandEventProcessor for this JDACommands instance
     */
    public void setCommandEventConsumer(Consumer<CommandEvent> commandEventProcessor) {
        if (commandEventProcessor == null)
            throw new NullPointerException("commandEventProcessor cannot be null");
        this.commandEventProcessor = commandEventProcessor;
    }

    /**
     * Handles a error with the errorHandler in this JDACommands instance
     *
     * @param exception the error to be handled
     */
    public void handleError(Exception exception) {
        errorHandler.accept(exception);
    }

    /**
     * Sets the errorHandler for this JDACommands instance
     *
     * @param errorHandler the new errorHandler for this JDACommands instance
     */
    public void setErrorHandler(Consumer<Exception> errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Gets the prefix for a guild from the prefixProvider in this JDACommands instance
     *
     * @param guild the possibly-null Guild
     * @return the non-null prefix for the provided guild
     */
    public String getPrefix(@Nullable Guild guild) {
        return prefixProvider.apply(guild);
    }

    /**
     * Sets the prefixProvider for this JDACommands instance
     *
     * @param prefixProvider the new prefixProvider for this JDACommands instance
     */
    public void setPrefixProvider(Function<@Nullable Guild, @NotNull String> prefixProvider) {
        this.prefixProvider = prefixProvider;
    }

    /**
     * Removes the command listener from the ShardManager or JDA instance
     */
    @SuppressWarnings("WeakerAccess")
    public void shutdown() {
        if (shardManager != null)
            shardManager.removeEventListener(commandListener);

        if (jda != null)
            jda.removeEventListener(commandListener);
    }

    public void defaultEventProcessor(CommandEvent event) {
        String content = event.getMessage().getContentRaw();

        String prefix = prefixProvider.apply(event.getGuild());
        String mention = event.getGuild() != null ? event.getGuild().getSelfMember()
                .getAsMention() : event.getJDA().getSelfUser().getAsMention();

        boolean mentionPrefix = content.contains(" ") && content.startsWith(mention) && allowMentionAsPrefix;
        if (!content.startsWith(prefix) && !mentionPrefix)
            return;

        List<String> arguments = new ArrayList<>(content.contains(" ")
                ? Arrays.asList(content.split(" ")) : Collections.singletonList(content));
        if (mentionPrefix)
            arguments.remove(0); // remove mention

        String cmd = mentionPrefix ? arguments.get(0) : arguments.get(0).replaceFirst(prefix, "");
        arguments.remove(0); // remove command

        List<Command> commands = new ArrayList<>();
        for (CommandCategory commandCategory : commandCategories)
            commands.addAll(commandCategory.getCommands());

        Command command = commands.stream()
                .filter(c -> c.getAliases().stream().anyMatch(format -> format.equalsIgnoreCase(cmd)))
                .findAny().orElse(null);
        if (command == null)
            return;

        List<CommandProperty> properties = command.getProperties();
        if (properties.contains(CommandProperty.GUILD_ONLY) && event.getGuild() == null)
            return;
        if (properties.contains(CommandProperty.BOT_OWNER) && !event.getAuthor().getId().equals(ownerId))
            return;

        try {
            defaultResultHandler(event, command.execute(event, arguments));
        } catch (PermissionException exception) {
            sendMessageSafely(event, Emoji.X + " Missing permission, " +
                    "`" + exception.getPermission().getName() + "`");
        }
    }

    public void defaultErrorHandler(Exception exception) {
        exception.printStackTrace();
    }

    public void defaultResultHandler(CommandEvent event, CommandResult commandResult) {
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

    @Override
    protected void finalize() {
        shutdown();
    }

    private class CommandListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            commandEventProcessor.accept(new CommandEvent(event));
        }

        @Override
        public void onMessageUpdate(MessageUpdateEvent event) {
            commandEventProcessor.accept(new CommandEvent(event));
        }
    }
}
