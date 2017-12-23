package pl.edu.uj.gestosterserver.interpreters;

import java.awt.*;

import pl.edu.uj.gestosterserver.controllers.IMouseController;
import pl.edu.uj.gestosterserver.controllers.MouseActions;
import pl.edu.uj.gestosterserver.controllers.RobotMouseController;

/**
 * Klasa służąca do przełożenia pozycji telefonu na ruch myszką
 */
public class OrientationInterpreter {
    private int counterAngleY = 0;
    private int counterAngleP = 0;

    private IMouseController mouseController;

    public OrientationInterpreter() throws AWTException {
        mouseController = new RobotMouseController();
    }

    /**
     * Obsługa pozycji telefonu i wykonanie konkretnej akcji myszką na komputerze
     *
     * @param yaw   -
     * @param pitch -
     * @param roll  -
     */
    public void angleChange(double yaw, double pitch, double roll) {
        // obsługa ruchu myszy
        double[] mouseMove = new double[2];

        if (Math.abs(yaw) > 4) {
            mouseMove[0] = yaw * counterAngleY / 200;
            counterAngleY++;
        } else {
            mouseMove[0] = 0;
            counterAngleY = 0;
        }

        if ((Math.abs(pitch) > 4)) {
            mouseMove[1] = pitch * counterAngleP / 200;
            counterAngleP++;
        } else {
            mouseMove[1] = 0;
            counterAngleP = 0;
        }

        mouseController.moveMouse((int) mouseMove[0], (int) mouseMove[1]);

        // obsługa kliknięcia lewym przyciskiem
        if ((!mouseController.getButtonState(MouseActions.LEFT_STATE) && roll < -15)) {
            mouseController.clickMouse(MouseActions.LEFT_CLICK);
        } else if (Math.abs(roll) < 8 && mouseController.getButtonState(MouseActions.LEFT_STATE)) {
            mouseController.clickMouse(MouseActions.LEFT_RELEASE);
        }

        // obsluga kliknięcia prawym przyciskiem
        if (!mouseController.getButtonState(MouseActions.RIGHT_STATE) && (roll > 15)) {
            mouseController.clickMouse(MouseActions.RIGHT_CLICK);
        } else if (Math.abs(roll) < 8 && mouseController.getButtonState(MouseActions.RIGHT_STATE)) {
            mouseController.clickMouse(MouseActions.RIGHT_RELEASE);
        }
    }
}