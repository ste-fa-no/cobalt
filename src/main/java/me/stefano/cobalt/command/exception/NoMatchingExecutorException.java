package me.stefano.cobalt.command.exception;

public class NoMatchingExecutorException extends Exception {

    public NoMatchingExecutorException() {}

    public NoMatchingExecutorException(String message) {
        super(message);
    }

}
