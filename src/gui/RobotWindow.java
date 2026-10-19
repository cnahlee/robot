package gui;

import robot.Board;
import robot.DepositedObject;
import robot.Robot;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** Swing controls around the Nicellipse board. Calls the existing Robot.execute(). */
public class RobotWindow extends JFrame {
    private static final String EXAMPLE = String.join("\n",
            "base 4 1", "dir EAST", "mov 2", "mov 3", "dir NORTH", "mov 3");

    private Robot robot;
    private final RobotBoardView boardView;
    private final JTextArea scriptEditor = new JTextArea(EXAMPLE, 11, 25);
    private final JTextArea logArea = new JTextArea(5, 25);
    private final JLabel positionLabel = new JLabel();
    private final JLabel directionLabel = new JLabel();
    private final JLabel carryingLabel = new JLabel();
    private final JLabel stepLabel = new JLabel("Ready");
    private final JButton runButton = new JButton("Run script");
//this is the list of commands that is going to be extracted and used
    private final List<String> commands = new ArrayList<>();
    private String preparedScript;
    private int nextCommand = 0;
    private final Timer timer;

    public RobotWindow() {
        super("Robot Explorer - Nicellipse");
        robot = new Robot(new Board());
        boardView = new RobotBoardView(robot);
        timer = new Timer(550, event -> executeNext());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(new Color(247, 249, 248));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBoardArea(), BorderLayout.CENTER);
        add(buildControls(), BorderLayout.EAST);

        updateDisplay();
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(getSize());
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(15, 18, 0, 18));

        JLabel title = new JLabel("ROBOT EXPLORER");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        title.setForeground(new Color(33, 54, 72));
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Etape 3  |  Script commands + graphical world");
        subtitle.setForeground(new Color(92, 111, 117));
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBoardArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 9));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 18, 18, 0),
                BorderFactory.createLineBorder(new Color(214, 224, 220))));
        panel.add(boardView, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 7));
        legend.setBackground(Color.WHITE);
        legend.add(new JLabel("●  Blue: robot"));
        legend.add(new JLabel("■  Gold: deposited object"));
        legend.add(new JLabel("x →   y ↑"));
        panel.add(legend, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(340, 610));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 18, 18),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        panel.add(sectionTitle("Robot state"));
        panel.add(positionLabel);
        panel.add(directionLabel);
        panel.add(carryingLabel);
        panel.add(new JLabel(" "));

        panel.add(sectionTitle("Script (one command per line)"));
        scriptEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        scriptEditor.setTabSize(4);
        scriptEditor.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        JScrollPane scriptScroll = new JScrollPane(scriptEditor);
        scriptScroll.setPreferredSize(new Dimension(315, 190));
        scriptScroll.setMaximumSize(new Dimension(500, 190));
        panel.add(scriptScroll);

        JPanel execution = new JPanel(new GridLayout(2, 2, 7, 7));
        execution.setOpaque(false);
        execution.setMaximumSize(new Dimension(500, 82));
        JButton stepButton = new JButton("Step");
        JButton resetButton = new JButton("Reset robot");
        JButton exampleButton = new JButton("Load example");
        runButton.addActionListener(e -> toggleRun());
        stepButton.addActionListener(e -> { stopTimer(); executeNext(); });
        resetButton.addActionListener(e -> resetRobot());
        exampleButton.addActionListener(e -> {
            stopTimer();
            scriptEditor.setText(EXAMPLE);
            invalidatePreparedScript();
            log("Loaded professor's example script");
        });
        execution.add(runButton);
        execution.add(stepButton);
        execution.add(resetButton);
        execution.add(exampleButton);
        panel.add(execution);
        panel.add(stepLabel);

        panel.add(new JLabel(" "));
        panel.add(sectionTitle("Objects (demo controls)"));
        JPanel objects = new JPanel(new GridLayout(1, 3, 5, 5));
        objects.setOpaque(false);
        objects.setMaximumSize(new Dimension(500, 38));
        JButton place = new JButton("Place box");
        JButton pickup = new JButton("Pick up");
        JButton drop = new JButton("Drop");
        place.addActionListener(e -> performAction("Place box", () -> {
            robot.getBoard().placeElement(robot.getPosition(), new DepositedObject("Sample"));
        }));
        pickup.addActionListener(e -> performAction("Pick up", () -> {
            DepositedObject object = robot.getBoard().getDepositedObjectAt(robot.getPosition());
            if (object == null) {
                throw new IllegalStateException("No deposited object at the robot's position.");
            }
            robot.pickUp(object);
        }));
        drop.addActionListener(e -> performAction("Drop", robot::drop));
        objects.add(place);
        objects.add(pickup);
        objects.add(drop);
        panel.add(objects);

        panel.add(new JLabel(" "));
        panel.add(sectionTitle("Execution log"));
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(315, 100));
        logScroll.setMaximumSize(new Dimension(500, 120));
        panel.add(logScroll);

        return panel;
    }

    private static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        label.setForeground(new Color(38, 67, 80));
        label.setBorder(BorderFactory.createEmptyBorder(3, 0, 8, 0));
        return label;
    }

    private void prepareScript() {
        String text = scriptEditor.getText();
        if (text.equals(preparedScript)) {
            return;
        }
        commands.clear();
        for (String line : text.split("\\R")) {
            line = line.trim();
            if (!line.isEmpty()) {
                commands.add(line);
            }
        }
        preparedScript = text;
        nextCommand = 0;
    }

    private void toggleRun() {
        if (timer.isRunning()) {
            stopTimer();
            return;
        }
        prepareScript();
        if (nextCommand >= commands.size()) {
            log("No remaining instructions. Press Reset to start over.");
            return;
        }
        scriptEditor.setEditable(false);
        runButton.setText("Pause");
        timer.start();
    }

    private void executeNext() {
        prepareScript();
        if (nextCommand >= commands.size()) {
            stopTimer();
            log("Script completed");
            return;
        }

        String line = commands.get(nextCommand);
        try {
            robot.execute(line); // Reuses Etape 3: CommandFactory + Command classes
            nextCommand++;
            stepLabel.setText("Instruction " + nextCommand + " / " + commands.size());
            log("OK  " + line);
            updateDisplay();
            if (nextCommand == commands.size()) {
                stopTimer();
                log("Script completed");
            }
        } catch (RuntimeException ex) {
            stopTimer();
            log("ERROR  " + line + "  ->  " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Command error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetRobot() {
        stopTimer();
        robot = new Robot(new Board());
        boardView.setRobot(robot);
        invalidatePreparedScript();
        stepLabel.setText("Ready");
        logArea.setText("");
        log("Robot reset at (0, 0), facing EAST");
        updateDisplay();
    }

    private void invalidatePreparedScript() {
        preparedScript = null;
        commands.clear();
        nextCommand = 0;
    }

    private void stopTimer() {
        timer.stop();
        runButton.setText("Run script");
        scriptEditor.setEditable(true);
    }

    private void performAction(String name, Runnable action) {
        stopTimer();
        try {
            action.run();
            log("OK  " + name);
            updateDisplay();
        } catch (RuntimeException ex) {
            log("ERROR  " + name + "  ->  " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Action error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDisplay() {
        positionLabel.setText("Position: (" + robot.getPosition().getX()
                + ", " + robot.getPosition().getY() + ")");
        directionLabel.setText("Direction: " + robot.getDirection());
        carryingLabel.setText("Carrying: " + (robot.getCarriedObject() == null
                ? "nothing" : robot.getCarriedObject().getName()));
        boardView.refreshBoard();
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
