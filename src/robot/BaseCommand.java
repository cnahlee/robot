package robot;

public class BaseCommand implements Command {
    private final int x;
    private final int y;

    public BaseCommand(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void execute(Robot robot) {
        robot.setPosition(new Position(x, y));
    }
}
