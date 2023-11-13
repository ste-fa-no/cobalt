package me.stefano.cobalt.adapter;

/**
 * The {@code ParameterAdapter} interface defines methods for adapting string parameters to a specified type {@code T}.
 *
 * @param <T> the type to which the string parameters will be adapted
 */
public interface ParameterAdapter<T> {

    /**
     * Converts the specified string parameter to the target type {@code T}.
     *
     * @param parameter the string parameter to be converted
     * @return an instance of type {@code T} representing the converted parameter
     */
    T from(String parameter);

    /**
     * Provides a fallback mechanism for handling exceptions during parameter conversion.
     * This method is called when an exception occurs while converting the parameter in the {@link #from(String)} method.
     *
     * @param parameter the string parameter that failed to be converted
     * @param e the exception that occurred during conversion
     * @return an alternative value of type {@code T} to be used as a fallback
     */
    T fallback(String parameter, Exception e);

}
