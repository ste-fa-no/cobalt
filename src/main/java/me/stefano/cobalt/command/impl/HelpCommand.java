package me.stefano.cobalt.command.impl;

import me.stefano.cobalt.Cobalt;
import me.stefano.cobalt.command.AbstractCommand;
import me.stefano.cobalt.command.Command;
import me.stefano.cobalt.command.CommandExecutor;

@Command(value = "help", aliases = {"?"}, description = "Prints a list of all registered commands.")
public class HelpCommand extends AbstractCommand {

    @CommandExecutor
    public void execute() {
        for (var commandEntry : Cobalt.INSTANCE.commandMap().entrySet()) {
            Command annotation = commandEntry.getValue().getCommandAnnotation();
            System.out.println("Command: " + commandEntry.getKey() + "\nDescription: " + annotation.description() + "\n");
        }
    }

}
