package me.stefano.cobalt;

import me.stefano.cobalt.adapter.exception.ParameterAdapterNotFoundException;
import me.stefano.cobalt.adapter.impl.*;
import me.stefano.cobalt.command.Command;
import me.stefano.cobalt.command.CommandExecutor;
import me.stefano.cobalt.adapter.ParameterAdapter;
import me.stefano.cobalt.command.exception.NoMatchingExecutorException;
import me.stefano.cobalt.command.exception.UnknownCommandException;
import me.stefano.cobalt.command.exception.UnspecifiedCommandException;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * The {@code Cobalt} class represents a command handling framework for a command-line application.
 * It provides functionality for registering commands, executing commands, and managing parameter adapters.
 */
public class Cobalt {

    private static Cobalt instance = null;

    private final Map<String, Object> commandMap;
    private final Map<Class<?>, ParameterAdapter<?>> adapterMap;

    /**
     * Constructs a new instance of the {@code Cobalt} class.
     * Initializes the command map and adapter map with default adapters for common types.
     */
    private Cobalt() {
        this.commandMap = new HashMap<>();
        this.adapterMap = new HashMap<>();

        this.registerAdapter(Boolean.class, new BooleanAdapter());
        this.registerAdapter(Character.class, new CharacterAdapter());
        this.registerAdapter(Double.class, new DoubleAdapter());
        this.registerAdapter(Float.class, new FloatAdapter());
        this.registerAdapter(Integer.class, new IntegerAdapter());
        this.registerAdapter(String.class, new StringAdapter());
    }

    /**
     * Gets the singleton instance of the {@code Cobalt} class.
     *
     * @return the singleton instance of the {@code Cobalt} class
     */
    public static Cobalt get() {
        if (instance == null) instance = new Cobalt();
        return instance;
    }

    /**
     * Adds a new object to the command map for handling commands.
     * <p>
     * A new entry is added to the command map only if the specified object's class is annotated with a valid {@link me.stefano.cobalt.command.Command} annotation and contains at least one method annotated with {@link me.stefano.cobalt.command.CommandExecutor}.
     * If the class lacks these annotations, an error message is printed to the console, and the registration process is aborted.
     *
     * @param command instance of a class where executors for a new command are defined
     */
    public void registerCommand(Object command) {
        var commandClass = command.getClass();

        if (!commandClass.isAnnotationPresent(Command.class)) {
            System.err.println("An error has occurred registering command " + commandClass.getName() + ".");
            System.err.println("Please ensure that " + commandClass.getName() + " is annotated with @Command.");
            System.err.println("See the documentation for more information.");
            return;
        }
        var commandAnnotation = commandClass.getAnnotation(Command.class);

        var commandClassMethodsStream = Arrays.stream(commandClass.getDeclaredMethods());
        var commandExecutorMethods = commandClassMethodsStream.filter(method -> method.isAnnotationPresent(CommandExecutor.class));

        if (commandExecutorMethods.findAny().isEmpty()) {
            System.err.println("An error has occurred registering command " + commandClass.getName() + ".");
            System.err.println("Please ensure that " + commandClass.getName() + " has at least one method annotated with @CommandExecutor.");
            System.err.println("See the documentation for more information.");
            return;
        }

        this.commandMap.put(commandAnnotation.value(), command);
        List.of(commandAnnotation.aliases()).forEach(name -> this.commandMap.put(name, command));
    }

    /**
     * Registers a custom parameter adapter for a specific class type.
     * <p>
     * This method allows the registration of a ParameterAdapter for a given class type.
     * If an adapter for the specified type is already registered, an error message will be printed to the standard error stream, and the registration will be skipped.
     *
     * @param type the class type for which the parameter adapter is being registered.
     * @param adapter the ParameterAdapter implementation for the specified class type.
     */
    public void registerAdapter(Class<?> type, ParameterAdapter<?> adapter) {
        if (this.adapterMap.containsKey(type)) {
            System.err.println("An error has occurred registering parameter adapter for class " + type.getName() + ".");
            System.err.println("An adapter for this type is already registered.");
            System.err.println("See the documentation for more information.");
            return;
        }

        this.adapterMap.put(type, adapter);
    }

    /**
     * The `dispatch` method is responsible for executing a specified command by identifying the appropriate command executor and invoking the corresponding method.
     * This method handles command parsing, executor selection, and parameter adaptation.
     *
     * @param command The command to be executed.
     *
     * @throws UnspecifiedCommandException if the provided command is null or empty.
     * @throws UnknownCommandException if the specified command is not registered.
     * @throws NoMatchingExecutorException if no executor method with the correct parameter count is found.
     * @throws ParameterAdapterNotFoundException if no parameter adapter is found for a specific parameter type.
     * @throws ExecutionException if an exception occurs during the execution of a command.
     * @throws InterruptedException if the execution of a command is interrupted.
     */
    public void dispatch(String command) throws UnspecifiedCommandException, UnknownCommandException, NoMatchingExecutorException, ParameterAdapterNotFoundException, ExecutionException, InterruptedException {

        if (command == null || command.isEmpty()) {
            throw new UnspecifiedCommandException("""
                    A syntax error has occurred trying to execute command.
                    Please specify a command to execute!""");
        }

        var splitCommand = new ArrayList<>(List.of(command.split(" ")));
        var commandName = splitCommand.get(0);
        splitCommand.remove(0);

        if (!this.commandMap.containsKey(commandName)) {
            throw new UnknownCommandException(String.format("""
                    A syntax error has occurred trying to execute command %s.
                    No command with name %s has ever been registered.""", commandName, commandName));
        }
        var commandParameterCount = splitCommand.size();

        var commandInstance = this.commandMap.get(commandName);
        var instanceMethods = Arrays.stream(commandInstance.getClass().getDeclaredMethods());
        var executorMethods = instanceMethods.filter(method -> method.isAnnotationPresent(CommandExecutor.class));

        var availableExecutors = executorMethods.filter(method -> method.getParameterCount() == commandParameterCount);
        var executorList = availableExecutors.toList();

        if (executorList.isEmpty()) {
            throw new NoMatchingExecutorException(String.format("""
                    A syntax error has occurred trying to execute command %s.
                    No executor method with %d parameters found for command %s.""", commandName, commandParameterCount, commandName));
        }

        for (var executor : executorList) {
            var executorParameters = executor.getParameters();
            var parameterList = new ArrayList<>(List.of(executorParameters));
            var invocationParameters = new ArrayList<>();

            for (var parameter : parameterList) {
                var parameterType = parameter.getType();
                var typeName = parameterType.getName();
                var parameterAdapter = this.adapterMap.get(parameterType);

                if (parameterAdapter == null) {
                    throw new ParameterAdapterNotFoundException(String.format("""
                            An internal error has occurred trying to execute command %s.
                            No parameter adapter found for type %s.
                            See the documentation for further information on how to create parameter type adapters.""", commandName, typeName));
                }

                var parameterIndex = parameterList.indexOf(parameter);
                var currentParameter = splitCommand.get(parameterIndex);
                Object parameterValue = null;

                try {
                    parameterValue = parameterAdapter.from(currentParameter);
                } catch (Exception e) {
                    parameterValue = parameterAdapter.fallback(currentParameter, e);
                } finally {
                    invocationParameters.add(parameterValue);
                }

            }

            var executorAnnotation = executor.getAnnotation(CommandExecutor.class);

            Runnable execute = () -> {
                try {
                    executor.invoke(commandInstance, invocationParameters.toArray());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(String.format("""
                            An internal error has occurred trying to execute command %s.
                            Unable to invoke method %s.
                            Please open an issue on GitHub if you believe this is a problem.""", commandName, executor.getName()));
                }
            };

            if (!executorAnnotation.async()) {
                execute.run();
                return;
            }

            CompletableFuture.runAsync(execute);
        }
    }

}
