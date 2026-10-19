package gui;


import component.*;
import robot.Board;
import robot.Direction;
import robot.DepositedObject;
import robot.Position;
import robot.Robot;


import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import static javax.swing.text.StyleConstants.setBackground;

/** Nicellipse view of the board. It reads the model; it never moves the robot itself. */
public class RobotBoardView extends component.NiSpace {
    private static final int COLUMNS = 12;
    private static final int ROWS = 9;
    private static final int CELL = 46;
    private static final int LEFT = 42;
    private static final int TOP = 18;

    private static final Color LIGHT_TILE = new Color(239, 246, 243);
    private static final Color DARK_TILE = new Color(226, 238, 232);
    private static final Color LINE = new Color(198, 215, 207);
    private static final Color ROBOT_BLUE = new Color(49, 112, 189);
    private static final Color BOX_GOLD = new Color(229, 172, 87);

    private Robot robot;
    private int minX = 0;
    private int minY = 0;

    public RobotBoardView(Robot robot) {
        super("Robot board", new Dimension(LEFT + COLUMNS * CELL + 15,
                TOP + ROWS * CELL + 36));
        setBackground(Color.WHITE);
        this.robot = robot;
        refreshBoard();
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
        minX = 0;
        minY = 0;
        refreshBoard();
    }

    /** Redraws the grid after the model changes. Coordinates increase upward. */
    public void refreshBoard() {
        removeAll();
        Position robotPosition = robot.getPosition();

        // Keep the robot visible without imposing limits on the actual world.
        if (robotPosition.getX() < minX || robotPosition.getX() >= minX + COLUMNS) {
            minX = robotPosition.getX() - COLUMNS / 2;
        }
        if (robotPosition.getY() < minY || robotPosition.getY() >= minY + ROWS) {
            minY = robotPosition.getY() - ROWS / 2;
        }

        Board board = robot.getBoard();
        for (int row = 0; row < ROWS; row++) {
            int y = minY + ROWS - 1 - row;
            addAxisLabel(String.valueOf(y), 2, TOP + row * CELL + 12, LEFT - 8, 20);

            for (int col = 0; col < COLUMNS; col++) {
                int x = minX + col;
                int pixelX = LEFT + col * CELL;
                int pixelY = TOP + row * CELL;

                NiRectangle tile = new NiRectangle();
                tile.setBackground((row + col) % 2 == 0 ? LIGHT_TILE : DARK_TILE);
                tile.setBorder(BorderFactory.createLineBorder(LINE));
                tile.setBounds(pixelX, pixelY, CELL, CELL);
                add(tile);

                DepositedObject object = board.getDepositedObjectAt(new Position(x, y));
                if (object != null) {
                    NiRectangle box = new NiRectangle();
                    box.setBounds(pixelX + 22, pixelY + 5, 18, 18);
                    box.setBackground(BOX_GOLD);
                    box.setBorder(BorderFactory.createLineBorder(new Color(164, 108, 47), 2));
                    box.setToolTipText("Object: " + object.getName());
                    add(box);
                    setComponentZOrder(box, 0);
                }
            }
        }

        for (int col = 0; col < COLUMNS; col++) {
            addAxisLabel(String.valueOf(minX + col), LEFT + col * CELL,
                    TOP + ROWS * CELL + 4, CELL, 20);
        }

        int column = robotPosition.getX() - minX;
        int row = ROWS - 1 - (robotPosition.getY() - minY);
        int x = LEFT + column * CELL;
        int y = TOP + row * CELL;

        NiEllipse robotShape = new NiEllipse();
        robotShape.setBounds(x + 5, y + 7, 32, 32);
        robotShape.setBackground(ROBOT_BLUE);
        robotShape.setBorderColor(new Color(28, 72, 127));
        robotShape.setStrokeWidth(2);

        JLabel facing = new JLabel(directionArrow(robot.getDirection()), SwingConstants.CENTER);
        facing.setForeground(Color.WHITE);
        facing.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
        facing.setBounds(0, 0, 32, 30);
        robotShape.add(facing);
        add(robotShape);
        setComponentZOrder(robotShape, 0);

        revalidate();
        repaint();
    }

    private void addAxisLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        label.setForeground(new Color(84, 102, 93));
        label.setBounds(x, y, width, height);
        add(label);
    }

    private static String directionArrow(Direction direction) {
        switch (direction) {
            case NORTH: return "↑";
            case SOUTH: return "↓";
            case EAST: return "→";
            case WEST: return "←";
            default: throw new IllegalStateException("Unknown direction: " + direction);
        }
    }
}
