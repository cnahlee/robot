package gui;

import javax.swing.SwingUtilities;

/** Application entry point: run this class in IntelliJ. */
public class RobotApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RobotWindow().setVisible(true));
    }
}
