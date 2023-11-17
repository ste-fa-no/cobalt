package me.stefano.cobalt.command.exception;

public class UnspecifiedCommandException extends Exception {

    // Parameterless Constructor
    public UnspecifiedCommandException() {}

    // Constructor that accepts a message
    public UnspecifiedCommandException(String message)
    {
        super(message);
    }

}
