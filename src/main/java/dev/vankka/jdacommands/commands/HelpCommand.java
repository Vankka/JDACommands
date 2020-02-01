package dev.vankka.jdacommands.commands;

import dev.vankka.jdacommands.model.command.*;
import dev.vankka.jdacommands.object.Emoji;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand implements CommandCategory, Command {

    private final List<Command> commands = Collections.singletonList(this);

    @Override
    public String getDescription() {
        return "Gets the list of commands";
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("help [category]");
    }

    @Override
    public List<CommandProperty> getProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<Permission> getBotRequiredPermissions() {
        return Collections.emptyList();
    }

    @Override
    public CommandResult execute(CommandEvent event) throws PermissionException {
        List<CommandCategory> commandCategories = event.getJdaCommands().getCommandCategories();

        if (!event.getArguments().isEmpty()) {
            CommandCategory commandCategory = commandCategories.stream()
                    .filter(category -> category.getName().equalsIgnoreCase(event.getArguments().get(0)))
                    .findFirst().orElse(null);
            if (commandCategory == null)
                return new CommandResult.Message(Emoji.X + " Category not found");

            StringBuilder stringBuilder = new StringBuilder("__**" + commandCategory.getName() + " help**__\n\n");
            for (Command command : commandCategory.getCommands()) {
                if (command.getProperties().contains(CommandProperty.GUILD_ONLY) && event.getGuild() == null)
                    continue;
                if (command.getProperties().contains(CommandProperty.BOT_OWNER_ONLY)
                        && !event.getJdaCommands().getBotOwnerId().equals(event.getAuthor().getId()))
                    continue;

                appendStringBuilder(stringBuilder, command, event.getPrefix());
            }

            stringBuilder.append("\n**[] = optional, <> = required**");
            return new CommandResult.Message(stringBuilder.toString());
        } else {
            StringBuilder stringBuilder = new StringBuilder("__**Help**__");

            List<CommandCategory> categories = commandCategories.stream().sorted()
                    .sorted(Comparator.comparingLong(commandCategory -> getCategorySize(commandCategory, event)))
                    .collect(Collectors.toList());

            for (CommandCategory category : categories) {
                if (getCategorySize(category, event) < 1L)
                    continue;

                stringBuilder.append("**").append(category.getName()).append("** ");

                if (category.getCommands().size() > 1)
                    stringBuilder.append(category.getDescription()).append("\n");
                else
                    appendStringBuilder(stringBuilder, category.getCommands().get(0), event.getPrefix());
            }

            stringBuilder.append("\n**[] = optional, <> = required**");
            return new CommandResult.Message(stringBuilder.toString());
        }
    }

    private void appendStringBuilder(StringBuilder stringBuilder, Command command, String prefix) {
        stringBuilder
                .append(command.getProperties().contains(CommandProperty.BOT_OWNER_ONLY) ? Emoji.CROWN : Emoji.GEAR)
                .append(" `")
                .append(prefix)
                .append("` ")
                .append(command.getDescription())
                .append("\n");
    }

    private long getCategorySize(CommandCategory commandCategory, CommandEvent event) {
        return commandCategory.getCommands().stream().map(Command::getProperties)
                .filter(commandProperties -> !commandProperties.contains(CommandProperty.GUILD_ONLY)
                        && event.getGuild() != null)
                .filter(commandProperties -> !commandProperties.contains(CommandProperty.BOT_OWNER_ONLY)
                        && !event.getAuthor().getId().equals(event.getJdaCommands().getBotOwnerId()))
                .count();
    }
}
