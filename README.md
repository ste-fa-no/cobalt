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
@Command(value = "sum", aliases = {"add"})
public class SumCommand {

    @CommandExecutor(async = false)
    public void execute(Integer num1, Integer num2) {
        System.out.println(num1 + num2);
    }

}
```

In order to be used, commands need to be registered calling the ```registerCommand()``` method.

```java
public class Main {
    
    public static void main(String[] args) {
        Cobalt.get().registerCommand(new SumCommand());
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
        Cobalt.get().registerCommand(new SumCommand());

        Cobalt.get().dispatch("sum 15 18");
    }

}
```
The output should be something like this:

```
33


Process finished with exit code 0
```
