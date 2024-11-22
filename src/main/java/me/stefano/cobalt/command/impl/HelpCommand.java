package me.stefano.cobalt.command.impl;

import me.stefano.cobalt.Cobalt;
import me.stefano.cobalt.command.Command;
import me.stefano.cobalt.command.CommandExecutor;

import java.util.Map;


@Command(value = "help", aliases = {"?"}, description = "Prints a list of all registered commands.")
public class HelpCommand {

    @CommandExecutor
    public void execute() {
        for (Map.Entry<String, Object> commandEntry : Cobalt.INSTANCE.commandMap().entrySet()) {
            Command annotation = commandEntry.getValue().getClass().getAnnotation(Command.class);
            System.out.println("Command: " + commandEntry.getKey() + "\nDescription: " + annotation.description() + "\n");
        }
    }

}
