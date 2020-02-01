package dev.vankka.jdacommands.model.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.List;

public interface Command {

    /**
     * Description to be displayed on the help command.
     * @return the description of the command.
     */
    String getDescription();

    /**
     * Gets the aliases for this command, the first alias is shown on the help command.
     * The first alias should have all the arguments of the command posted
     *
     * @return returns all the aliases
     */
    List<String> getAliases();
    List<CommandProperty> getProperties();

    /**
     * The permissions the bot requires to execute the command.
     *
     * @return the list of required permissions
     */
    List<Permission> getBotRequiredPermissions();

    CommandResult execute(CommandEvent event) throws PermissionException;

}
