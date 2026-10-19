package robot;

public class MoveCommand implements Command {
    private final int distance;

    public MoveCommand(int distance) {
        this.distance = distance;
    }

    @Override
    public void execute(Robot robot) {
        robot.move(distance);
    }
}
