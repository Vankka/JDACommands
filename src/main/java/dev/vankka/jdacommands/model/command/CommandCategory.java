package dev.vankka.jdacommands.model.command;

import java.util.List;

public interface CommandCategory {

    /**
     * Description to be displayed on the help command.
     *
     * @return the description of the category.
     */
    String getDescription();

    List<Command> getCommands();

    String getName();

}
