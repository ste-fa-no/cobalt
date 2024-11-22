# cobalt

This repository aims to simplify handling string-based commands while being as platform-agnostic as possible.

## Installation

Git clone this repository and build using ``mvn clean install``

Add cobalt as a dependency to your project.

```xml
    <dependencies>
        <dependency>
            <groupId>me.stefano</groupId>
            <artifactId>cobalt</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
```

## Usage

cobalt uses the singleton design pattern, as having multiple istances of the library is useless. 

You can define commands using annotations.

```java
@Command(value = "help", aliases = {"?"}, description = "Prints a list of all registered commands.")
public class HelpCommand extends AbstractCommand {

    @CommandExecutor
    public void execute() {
        for (Map.Entry<String, AbstractCommand> commandEntry : Cobalt.INSTANCE.commandMap().entrySet()) {
            Command annotation = commandEntry.getValue().getClass().getAnnotation(Command.class);
            System.out.println("Command: " + commandEntry.getKey() + "\nDescription: " + annotation.description() + "\n");
        }
    }

}
```

In order to be used, commands need to be registered calling the ```registerCommand()``` method.

```java
public class Main {

    public static void main(String[] args) {
        // NB: you don't actually need to register HelpCommand specifically as Cobalt does it on its own!
        Cobalt.INSTANCE.registerCommand(new HelpCommand());
    }

}
```

To simplify the implementation of new commands, custom parameter type adapters are supported.

You can define new ones by implementing the ```ParameterAdapter<T>``` interface.

```java
public class BooleanAdapter implements ParameterAdapter<Boolean> {

    @Override
    public Boolean from(String parameter) {
        return Boolean.valueOf(parameter);
    }

    @Override
    public Boolean fallback(String parameter, Exception e) {
        return false;
    }

}
```

Commands can be executed using the ```dispatch()``` method.

```java
public class Main {

    public static void main(String[] args) {
        Cobalt.INSTANCE.dispatch("help");
        Cobalt.INSTANCE.dispatch("?");
    }

}
```
The output should be something like this:

```
Command: help
Description: Prints a list of all registered commands.

Command: ?
Description: Prints a list of all registered commands.


Process finished with exit code 0
```
