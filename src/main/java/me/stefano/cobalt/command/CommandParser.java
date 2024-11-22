package me.stefano.cobalt.command;

import me.stefano.cobalt.Cobalt;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Utility class responsible for parsing command classes and extracting metadata.
 * <p>
 * The {@code CommandParser} provides methods to analyze command classes,
 * retrieve annotations, identify executors, and determine the executed command name
 * from a command string.
 */
public class CommandParser {


    /**
     * Checks if the given class is annotated with the {@link Command} annotation.
     *
     * @param commandClass the class to check.
     * @return {@code true} if the class has the {@code Command} annotation, {@code false} otherwise.
     */
    public boolean hasCommandAnnotation(Class<?> commandClass) {
        return commandClass.isAnnotationPresent(Command.class);
    }

    /**
     * Retrieves the {@link Command} annotation from the given class.
     *
     * @param commandClass the class from which to retrieve the annotation.
     * @return the {@code Command} annotation if present, or {@code null} if the class is not annotated.
     */
    public Command getCommandAnnotation(Class<?> commandClass) {
        return commandClass.getAnnotation(Command.class);
    }

    /**
     * Retrieves all methods annotated with {@link CommandExecutor} from the given class.
     *
     * @param commandClass the class to analyze.
     * @return a list of methods annotated with {@code CommandExecutor}.
     */
    public List<Method> getExecutors(Class<?> commandClass) {
        return Arrays.stream(commandClass.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(CommandExecutor.class)).toList();
    }

    /**
     * Checks if the given class contains any methods annotated with {@link CommandExecutor}.
     *
     * @param commandClass the class to check.
     * @return {@code true} if the class has at least one {@code CommandExecutor} method, {@code false} otherwise.
     */
    public boolean hasExecutors(Class<?> commandClass) {
        return !getExecutors(commandClass).isEmpty();
    }

    /**
     * Extracts the name of the command being executed from a command string.
     * <p>
     * The method identifies the command name based on the registered commands in {@link Cobalt#commandMap()}.
     *
     * @param command the input command string.
     * @return the name of the command being executed, trimmed of extra whitespace.
     * @throws NoSuchElementException if no matching command name is found.
     */
    public String getExecutedCommandName(String command) {
        return Cobalt.INSTANCE.commandMap().keySet().stream().filter((command + " ")::startsWith).sorted(Comparator.comparingInt((o1) -> o1.length() * -1)).toList().getFirst().trim();
    }

}
