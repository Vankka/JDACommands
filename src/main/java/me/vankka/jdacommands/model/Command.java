package me.vankka.jdacommands.model;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.List;

public interface Command {

    List<String> getAliases();
    List<CommandProperty> getProperties();
    List<Permission> getBotRequiredPermissions();

    CommandResult execute(CommandEvent commandEvent) throws PermissionException;

}
