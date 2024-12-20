package me.stefano.cobalt;

import me.stefano.cobalt.adapter.ParameterAdapter;
import me.stefano.cobalt.adapter.exception.ParameterAdapterNotFoundException;
import me.stefano.cobalt.adapter.impl.*;
import me.stefano.cobalt.command.Command;
import me.stefano.cobalt.command.CommandDispatcher;
import me.stefano.cobalt.command.AbstractCommand;
import me.stefano.cobalt.command.exception.NoMatchingExecutorException;
import me.stefano.cobalt.command.exception.UnknownCommandException;
import me.stefano.cobalt.command.impl.HelpCommand;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton enum that serves as the core manager for the command framework.
 * <p>
 * The {@code Cobalt} class provides methods to register commands, manage parameter adapters,
 * and dispatch commands dynamically. It uses a parser and dispatcher internally to interpret
 * and execute commands based on user input.
 */
public enum Cobalt {

    /**
     * Singleton instance of the Cobalt class.
     */
    INSTANCE;

    private final Map<String, AbstractCommand> commandMap;
    private final Map<Class<?>, ParameterAdapter<?>> adapterMap;

    private final CommandDispatcher dispatcher;

    /**
     * Private constructor for the singleton instance. Initializes the command map,
     * adapter map, parser, and dispatcher. Additionally, registers default parameter
     * adapters for common data types.
     */
    Cobalt() {
        this.commandMap = new HashMap<>();
        this.adapterMap = new HashMap<>();
        this.dispatcher = new CommandDispatcher();

        this.registerAdapter(Boolean.class, new BooleanAdapter());
        this.registerAdapter(Character.class, new CharacterAdapter());
        this.registerAdapter(Double.class, new DoubleAdapter());
        this.registerAdapter(Float.class, new FloatAdapter());
        this.registerAdapter(Integer.class, new IntegerAdapter());
        this.registerAdapter(String.class, new StringAdapter());

        this.registerCommand(new HelpCommand());
    }

    /**
     * Retrieves an immutable view of the current command map.
     *
     * @return an unmodifiable map of registered commands, where keys are command names
     *         or aliases and values are the corresponding command objects.
     */
    public Map<String, AbstractCommand> commandMap() {
        return Map.copyOf(this.commandMap);
    }

    /**
     * Retrieves an immutable view of the current parameter adapter map.
     *
     * @return an unmodifiable map of registered parameter adapters, where keys are
     *         parameter types and values are the corresponding adapters.
     */
    public Map<Class<?>, ParameterAdapter<?>> adapterMap() {
        return Map.copyOf(this.adapterMap);
    }

    /**
     * Registers a command in the framework.
     *
     * The command is associated with its primary name and any aliases defined
     * in its {@link Command} annotation.
     *
     * @param command the {@link AbstractCommand} to register.
     */
    public void registerCommand(AbstractCommand command) {
        Command annotation = command.getCommandAnnotation();
        this.commandMap.put(annotation.value(), command);
        List.of(annotation.aliases()).forEach(name -> this.commandMap.putIfAbsent(name, command));
    }

    /**
     * Registers a parameter adapter for a specific type.
     *
     * @param type    the parameter type for which the adapter will be registered.
     * @param adapter the adapter implementation.
     */
    public void registerAdapter(Class<?> type, ParameterAdapter<?> adapter) {
        this.adapterMap.putIfAbsent(type, adapter);
    }

    /**
     * Dispatches a command string to be executed.
     *
     * @param command the input string representing the command and its arguments.
     * @throws UnknownCommandException          if the command is not recognized.
     * @throws NoMatchingExecutorException      if no suitable executor is found for the command.
     * @throws ParameterAdapterNotFoundException if required parameter adapters are not available.
     */
    public void dispatch(String command) throws UnknownCommandException, NoMatchingExecutorException, ParameterAdapterNotFoundException {
        String cmd = command.trim();
        if (!dispatcher.isValid(cmd)) throw new UnknownCommandException("Unknown command.");

        String commandName = dispatcher.getExecutedCommandName(command);
        AbstractCommand commandObject = commandMap.get(commandName);
        if (commandObject == null) throw new UnknownCommandException("Unknown command.");

        String arguments = cmd.replace(commandName, "");
        List<String> args = dispatcher.getSplitArguments(arguments);

        List<Method> executors = dispatcher.getAvailableExecutors(commandObject, args);
        if (executors.isEmpty()) throw new NoMatchingExecutorException();

        for (Method executor : executors) {
            List<Object> parameterList = dispatcher.getInvocationParameters(executor, args);
            if (parameterList.size() != executor.getParameterCount()) continue;

            dispatcher.execute(commandObject, executor, parameterList);
        }

    }

}
