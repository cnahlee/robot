package robot;

import java.util.Map;
import java.util.function.Function;

public class CommandFactory {

    private static final Map<String, Function<String[], Command>> FACTORIES = Map.of(
        "base", parts -> {
            requireLength(parts, 3);
            return new BaseCommand(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        },
        "dir", parts -> {
            requireLength(parts, 2);
            return new DirectionCommand(Direction.valueOf(parts[1]));
        },
        "mov", parts -> {
            requireLength(parts, 2);
            return new MoveCommand(Integer.parseInt(parts[1]));
        },
        "drop", parts -> {
            requireLength(parts, 1);
            return new DropCommand();
        }
    );

    public static Command create(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Command must not be blank.");
        }
        String[] parts = line.trim().split("\\s+");
        Function<String[], Command> factory = FACTORIES.get(parts[0]);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown command: " + parts[0]);
        }
        return factory.apply(parts);
    }

    private static void requireLength(String[] parts, int expected) {
        if (parts.length != expected) {
            throw new IllegalArgumentException("Invalid arguments for: " + parts[0]);
        }
    }
}
