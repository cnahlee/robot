package unitTestsObservers;
import org.junit.jupiter.api.Test;
import robot.Board;
import robot.Position;
import robot.Robot;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.*;
import static org.testng.Assert.assertEquals;

public class RobotObserverTest {
    @Test
    public void setPositionShouldNotifyObservers() {
        Board board = new Board();
        Robot r = new Robot(board);
        TestObserver obs = new TestObserver();
        r.addObserver(obs);

        // change position
        r.setPosition(new Position(3, 2));

        assertEquals(1, obs.calls, "observer should have been notified once");
        assertSame(r, obs.last);
    }

    @Test
    public void startStopRecordNotify() {
        Board board = new Board();
        Robot r = new Robot(board);
        TestObserver obs = new TestObserver();
        r.addObserver(obs);

        r.stopM();
        r.startM();

        assertTrue(obs.calls >= 2);
    }
}