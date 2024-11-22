package me.stefano.cobalt.command;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Abstract base class for defining commands in the Cobalt framework.
 * <p>
 * The {@code AbstractCommand} class serves as the foundation for all user-defined commands
 * in the framework. It ensures that a command class is annotated with {@link Command} and provides
 * methods to retrieve command executors annotated with {@link CommandExecutor}.
 * <p>
 * Concrete command classes should extend this class and provide specific implementations for
 * the command execution logic using the {@link CommandExecutor} annotation on methods.
 */
public abstract class AbstractCommand {

    /**
     * Constructor for the {@code AbstractCommand} class.
     * <p>
     * This constructor ensures that the command class is annotated with {@link Command}.
     * If the class is not annotated with {@link Command}, an {@link IllegalStateException}
     * will be thrown.
     */
    protected AbstractCommand() {
        if (!hasCommandAnnotation()) throw new IllegalStateException();
    }

    /**
     * Checks if the command class is annotated with the {@link Command} annotation.
     * <p>
     * This method verifies if the current command class has been properly annotated
     * with the {@link Command} annotation, which is required to register and identify
     * the command within the framework.
     *
     * @return {@code true} if the class has the {@link Command} annotation, {@code false} otherwise.
     */
    public boolean hasCommandAnnotation() {
        return getClass().isAnnotationPresent(Command.class);
    }

    /**
     * Retrieves the {@link Command} annotation present on the command class.
     * <p>
     * This method returns the {@link Command} annotation that contains metadata
     * about the command, such as its name and aliases. If the class is not annotated
     * with {@link Command}, this method will return {@code null}.
     *
     * @return the {@link Command} annotation of the class, or {@code null} if not present.
     */
    public Command getCommandAnnotation() {
        return getClass().getAnnotation(Command.class);
    }

    /**
     * Retrieves a list of methods annotated with {@link CommandExecutor} in the command class.
     * <p>
     * This method returns a list of methods within the command class that are annotated
     * with {@link CommandExecutor}, which are the methods responsible for executing
     * the command logic. Each of these methods will handle specific parts of the command
     * execution based on their arguments and annotations.
     *
     * @return a list of {@link Method} objects representing command executor methods.
     */
    public List<Method> getExecutors() {
        return Arrays.stream(getClass().getDeclaredMethods()).filter(method -> method.isAnnotationPresent(CommandExecutor.class)).toList();
    }

}
