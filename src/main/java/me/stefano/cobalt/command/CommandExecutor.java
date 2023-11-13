package me.stefano.cobalt.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code CommandExecutor} annotation is used to mark a method as a command executor.
 * Annotated methods will be invoked to handle the execution of specific commands.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandExecutor {

    /**
     * Specifies whether the annotated method should be executed asynchronously.
     *
     * @return {@code true} if the method should be executed asynchronously, {@code false} otherwise.
     */
    boolean async() default false;

}
