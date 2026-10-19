package robot;

public class DirectionCommand implements Command {
    private final Direction direction;

    public DirectionCommand(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void execute(Robot robot) {
        robot.setDirection(direction);
    }
}
