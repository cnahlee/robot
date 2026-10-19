package robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class Robot {
    private final Board board;
    private Position position;
    private Direction direction;
    private DepositedObject carriedObject;

    private final List<String> history = new ArrayList<>();
    private boolean recordingActive = true;
    private static final Set<String> CONTROL_COMMANDS = Set.of("startM", "stopM", "resM", "replay");


    //1-create a list of robot Observer
    private final List<RobotObserver> observers = new CopyOnWriteArrayList<>();

    public void addObserver(RobotObserver o)
    {
        if(o!=null)
            observers.add(o);
    }

    public void removeObserver(RobotObserver o)
    {
        observers.remove(o);
    }

    private void notifyObserver()
    {
        for(RobotObserver o:observers) {
            try {
                o.robotChanged(this);
            } catch (RuntimeException ex) {
                // protect notifications from observer exceptions
                ex.printStackTrace();
            }
        }
    }

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
        notifyObserver();
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction must not be null.");
        }
        this.direction = direction;
        notifyObserver();
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
        notifyObserver();
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
        notifyObserver();
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
        // setPosition will already call notifyObserver(), so no additional notify here
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
            // notifyObserver() is not strictly necessary here because the command
            // implementations call model mutators (setPosition/setDirection/drop/pickUp)
            // which already call notifyObserver(). If you prefer guaranteed updates
            // after every command, you could uncomment the next line:
            // notifyObserver();
        }
    }

    public void startM() {
        recordingActive = true;
        notifyObserver();
    }

    public void stopM() {
        recordingActive = false;
        notifyObserver();
    }

    public void resM() {
        history.clear();
        notifyObserver();
    }

    public void replay() {
        boolean previousState = recordingActive;
        try {
            recordingActive = false;
            // notify that recording was disabled for clarity (optional)
            notifyObserver();
            List<String> snapshot = new ArrayList<>(history);
            for (String commandLine : snapshot) {
                Command command = CommandFactory.create(commandLine);
                command.execute(this);
            }
        } finally {
            recordingActive = previousState;
            notifyObserver();
        }
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public boolean isRecordingActive() {
        return recordingActive;
    }
}
