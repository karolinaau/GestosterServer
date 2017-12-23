package pl.edu.uj.gestosterserver.controllers;

import java.awt.*;
import java.awt.event.InputEvent;

public class RobotMouseController implements IMouseController {
    // referencja do klasy Robot
    private Robot robot;
    // informacja o przyciśniętym prawym przycisku
    private boolean rightClick = false;
    // informacja o przyciśniętym lewym przycisku
    private boolean leftClick = false;

    /**
     * Konstruktor domyślny - inicjuje obiekt klasy Robot
     */
    public RobotMouseController() throws AWTException {
        robot = new Robot();
    }

    @Override
    public void moveMouse(int x, int y) {
        Point point = MouseInfo.getPointerInfo().getLocation();

        robot.mouseMove((int) (point.getX() + x), (int) (point.getY() + y));
    }

    @Override
    public void clickMouse(MouseActions mouseActions) {
        if (mouseActions == MouseActions.LEFT_CLICK) {
            System.out.println("left_click");
            robot.mousePress(InputEvent.BUTTON1_MASK);
            leftClick = true;
        } else if (mouseActions == MouseActions.LEFT_RELEASE) {
            System.out.println("left_release");
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            leftClick = false;
        } else if (mouseActions == MouseActions.RIGHT_CLICK) {
            System.out.println("right_click");
            robot.mousePress(InputEvent.BUTTON3_MASK);
            rightClick = true;
        } else if (mouseActions == MouseActions.RIGHT_RELEASE) {
            System.out.println("right_release");
            robot.mouseRelease(InputEvent.BUTTON3_MASK);
            rightClick = false;
        }
    }

    @Override
    public boolean getButtonState(MouseActions mouseActions) {
        if (mouseActions == MouseActions.LEFT_STATE) {
            return leftClick;
        }

        return mouseActions == MouseActions.RIGHT_STATE && rightClick;
    }
}
