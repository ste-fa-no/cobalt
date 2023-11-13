package me.stefano.cobalt;

import me.stefano.cobalt.adapter.impl.*;
import me.stefano.cobalt.command.Command;
import me.stefano.cobalt.command.CommandExecutor;
import me.stefano.cobalt.adapter.ParameterAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
     * @param type    The class type for which the parameter adapter is being registered.
     * @param adapter The ParameterAdapter implementation for the specified class type.
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
     * Executes the specified command using the registered command executors.
     *
     * @param command The command to be executed, including the command name and any parameters.
     *                Must not be null or empty.
     */
    public void dispatch(String command) {
        if (command == null || command.isEmpty()) {
            System.err.println("An syntax error has occurred trying to execute command.");
            System.err.println("You must specify a command to be executed!");
            return;
        }

        var splitCommand = new ArrayList<>(List.of(command.split(" ")));
        var commandName = splitCommand.get(0);
        splitCommand.remove(0);

        if (!this.commandMap.containsKey(commandName)) {
            System.err.println("An syntax error has occurred trying to execute command " + commandName + ".");
            System.err.println("No command with name " + commandName + " has ever been registered.");
            return;
        }
        var commandParameterCount = splitCommand.size();

        var commandInstance = this.commandMap.get(commandName);
        var instanceMethods = Arrays.stream(commandInstance.getClass().getDeclaredMethods());
        var executorMethods = instanceMethods.filter(method -> method.isAnnotationPresent(CommandExecutor.class));

        var availableExecutors = executorMethods.filter(method -> method.getParameterCount() == commandParameterCount);
        var executorList = availableExecutors.toList();
        if (executorList.isEmpty()) {
            System.err.println("An syntax error has occurred trying to execute command " + commandName + ".");
            System.err.println("No executor method with " + commandParameterCount + " parameters found for command " + commandName + ".");
            return;
        }

        for (var executor : executorList) {
            var executorParameters = executor.getParameters();
            var parameterList = new ArrayList<>(List.of(executorParameters));
            var invocationParameters = new ArrayList<>();

            for (var parameter : parameterList) {
                var parameterType = parameter.getType();
                var parameterAdapter = this.adapterMap.get(parameterType);

                if (parameterAdapter == null) {
                    System.err.println("An internal error has occurred trying to execute command " + commandName + ".");
                    System.err.println("No parameter adapter found for type " + parameterType.getName() + ".");
                    System.err.println("See the documentation for further information on how to create parameter type adapters.");
                    return;
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
                    System.err.println("An internal error has occurred trying to execute command " + commandName + ".");
                    System.err.println("Unable to invoke method " + executor.getName() + ".");
                    System.err.println("Please open an issue on GitHub if you believe this is a problem.");
                }
            };

            if (!executorAnnotation.async()) {
                execute.run();
                return;
            }

            CompletableFuture<Void> future = CompletableFuture.runAsync(execute);
        }
    }

}
