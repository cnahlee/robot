package unitTestsObservers;

import robot.Robot;
import robot.RobotObserver;

   class TestObserver implements RobotObserver {
        int calls = 0;
        Robot last;

        @Override
        public void robotChanged(Robot robot) {
            calls++;
            last = robot;
        }
    }

