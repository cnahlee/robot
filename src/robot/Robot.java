package robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Robot {
    private final Board board;
    private Position position;
    private Direction direction;
    private DepositedObject carriedObject;

    private final List<String> history = new ArrayList<>();
    private boolean recordingActive = true;
    private static final Set<String> CONTROL_COMMANDS = Set.of("startM", "stopM", "resM", "replay");

    public Robot(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board must not be null.");
        }
        this.board = board;
        this.position = new Position(0, 0);
        this.direction = Direction.EAST;
        board.placeElement(position, this);
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position newPosition) {
        if (newPosition == null) {
            throw new IllegalArgumentException("Position must not be null.");
        }
        if (position.equals(newPosition)) {
            return;
        }
        if (board.getAt(newPosition) instanceof Robot) {
            throw new IllegalStateException("Another robot occupies the destination.");
        }
        board.clearElement(position);
        position = newPosition;
        board.placeElement(position, this);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction must not be null.");
        }
        this.direction = direction;
    }

    public Board getBoard() {
        return board;
    }

    public DepositedObject getCarriedObject() {
        return carriedObject;
    }

    public void pickUp(DepositedObject object) {
        if (object == null) {
            throw new IllegalArgumentException("Object must not be null.");
        }
        if (carriedObject != null) {
            throw new IllegalStateException("The robot is already carrying an object.");
        }
        if (board.getDepositedObjectAt(position) != object) {
            throw new IllegalStateException("The object is not at the robot's position.");
        }
        board.removeDepositedObject(position);
        carriedObject = object;
    }

    public void drop() {
        if (carriedObject == null) {
            return;
        }
        if (board.getDepositedObjectAt(position) != null) {
            throw new IllegalStateException("There is already an object at this position.");
        }
        board.placeElement(position, carriedObject);
        carriedObject = null;
    }

    public void move(int distance) {
        Position destination;
        switch (direction) {
            case NORTH:
                destination = new Position(position.getX(), position.getY() + distance);
                break;
            case SOUTH:
                destination = new Position(position.getX(), position.getY() - distance);
                break;
            case EAST:
                destination = new Position(position.getX() + distance, position.getY());
                break;
            case WEST:
                destination = new Position(position.getX() - distance, position.getY());
                break;
            default:
                throw new IllegalStateException("Unknown direction: " + direction);
        }
        setPosition(destination);
    }

    public void execute(String script) {
        if (script == null) {
            throw new IllegalArgumentException("Script must not be null.");
        }
        for (String line : script.split("\\R")) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            String commandName = parts[0];
            Command command = CommandFactory.create(line);
            command.execute(this);
            if (recordingActive && !CONTROL_COMMANDS.contains(commandName)) {
                history.add(line);
            }
        }
    }

    public void startM() {
        recordingActive = true;
    }

    public void stopM() {
        recordingActive = false;
    }

    public void resM() {
        history.clear();
    }

    public void replay() {
        boolean previousState = recordingActive;
        try {
            recordingActive = false;
            List<String> snapshot = new ArrayList<>(history);
            for (String commandLine : snapshot) {
                Command command = CommandFactory.create(commandLine);
                command.execute(this);
            }
        } finally {
            recordingActive = previousState;
        }
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public boolean isRecordingActive() {
        return recordingActive;
    }
}
