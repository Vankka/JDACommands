package me.vankka.jdacommands.model;

import java.util.List;

public interface CommandCategory {

    List<Command> getCommands();
    String getDescription();

}
