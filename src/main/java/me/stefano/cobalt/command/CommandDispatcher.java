package me.stefano.cobalt.command;

import me.stefano.cobalt.Cobalt;
import me.stefano.cobalt.adapter.ParameterAdapter;
import me.stefano.cobalt.adapter.exception.ParameterAdapterNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the execution of commands and their associated methods.
 * <p>
 * The {@code CommandDispatcher} class is responsible for validating commands, splitting
 * arguments, determining executors, converting arguments to parameters, and executing
 * command methods synchronously or asynchronously.
 */
public class CommandDispatcher {

    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    /**
     * Validates whether the given command string is non-null and non-blank.
     *
     * @param command the command string to validate.
     * @return {@code true} if the command is valid, {@code false} otherwise.
     */
    public boolean isValid(String command) {
        return command != null && !command.isBlank();
    }

    /**
     * Splits the arguments string into a list of individual arguments.
     * <p>
     * Arguments enclosed in double quotes are treated as a single argument.
     *
     * @param arguments the raw arguments string.
     * @return a list of parsed arguments.
     */
    public List<String> getSplitArguments(String arguments) {
        List<String> result = new ArrayList<>();

        String regex = "\"([^\"]*)\"|\\S+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(arguments);

        while (matcher.find())
            if (matcher.group(1) != null) result.add(matcher.group(1));
            else result.add(matcher.group());

        return result;
    }

    /**
     * Retrieves a list of methods from the command object that match the given
     * number of arguments.
     *
     * @param command the command object containing the methods.
     * @param args    the list of arguments provided to the command.
     * @return a list of matching methods.
     */
    public List<Method> getAvailableExecutors(Object command, List<String> args) {
        return Arrays.stream(command.getClass().getDeclaredMethods()).filter(method -> method.getParameterCount() == args.size()).toList();
    }

    /**
     * Converts a list of string arguments to a list of invocation parameters based on the
     * method's parameter types.
     *
     * @param executor the method to be invoked.
     * @param args     the list of arguments provided to the command.
     * @return a list of converted parameters for method invocation.
     * @throws ParameterAdapterNotFoundException if no adapter is found for a parameter type.
     * @throws IllegalStateException             if a parameter conversion fails.
     */
    public List<Object> getInvocationParameters(Method executor, List<String> args) throws ParameterAdapterNotFoundException {
        List<Parameter> parameterList = Arrays.asList(executor.getParameters());
        var invocationParameters = new ArrayList<>();

        for (Parameter param : parameterList) {
            Class<?> parameterType = param.getType();
            ParameterAdapter<?> parameterAdapter = Cobalt.INSTANCE.adapterMap().get(parameterType);

            if (parameterAdapter == null)
                throw new ParameterAdapterNotFoundException("No adapter found for parameter type: " + parameterType.getName());

            int parameterIndex = parameterList.indexOf(param);
            String currentParameter = args.get(parameterIndex);
            Object parameterValue;

            try {
                parameterValue = parameterAdapter.from(currentParameter);
            } catch (Exception e) {
                parameterValue = parameterAdapter.fallback(currentParameter, e);
            }

            if (parameterValue != null) invocationParameters.add(parameterValue);
            else throw new IllegalStateException("Failed to convert parameter " + parameterType.getName() + ".");
        }
        return invocationParameters;
    }

    /**
     * Checks whether the given method should be executed asynchronously.
     *
     * @param executor the method to check.
     * @return {@code true} if the method is annotated as asynchronous, {@code false} otherwise.
     */
    public boolean isAsync(Method executor) {
        return executor.getAnnotation(CommandExecutor.class) != null && executor.getAnnotation(CommandExecutor.class).async();
    }

    /**
     * Executes a command method with the specified parameters.
     * <p>
     * If the method is marked as asynchronous, it is submitted to an asynchronous executor.
     * Otherwise, it is executed immediately on the current thread.
     *
     * @param commandObject the object containing the method.
     * @param executor      the method to execute.
     * @param parameterList the list of parameters to pass to the method.
     */
    public void execute(Object commandObject, Method executor, List<Object> parameterList) {
        Runnable execute = () -> {
            try {
                executor.invoke(commandObject, parameterList.toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("An internal error has occurred.");
            }
        };

        if (!isAsync(executor)) {
            execute.run();
            return;
        }

        asyncExecutor.submit(execute);
    }

}
