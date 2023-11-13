package me.stefano.cobalt.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code Command} annotation is used to mark a class as a command for a command-line application.
 * Command classes annotated with this annotation can be discovered and processed by the application.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    /**
     * The primary name of the command.
     *
     * @return the primary name of the command
     */
    String value();

    /**
     * An array of alternative names (aliases) for the command.
     *
     * @return an array of alternative names for the command
     */
    String[] aliases() default {};

    /**
     * A brief description of the command, which can be displayed in help messages.
     *
     * @return the description of the command
     */
    String description() default "Description unavailable.";

}
