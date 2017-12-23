package pl.edu.uj.gestosterserver.controllers;

import java.awt.*;

/**
 * @author Karolinka
 */
public class RobotKeyboardController implements IKeyboardController {
    // referencja do klasy Robot
    private Robot robot;

    public RobotKeyboardController() throws AWTException {
        robot = new Robot();
    }

    @Override
    public void keyPressAndRelease(int keyId) {
        robot.keyPress(keyId);
        robot.keyRelease(keyId);
    }

}
