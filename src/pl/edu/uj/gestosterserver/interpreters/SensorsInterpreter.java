package pl.edu.uj.gestosterserver.interpreters;

import pl.edu.uj.gestosterserver.controllers.AutoHotkeySoundController;
import pl.edu.uj.gestosterserver.controllers.IKeyboardController;
import pl.edu.uj.gestosterserver.controllers.ISoundController;
import pl.edu.uj.gestosterserver.controllers.RobotKeyboardController;
import pl.edu.uj.gestosterserver.gestures.GestureDetector;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Klasa służąca do przełożenia odczytów sensorów na gesty
 */
public class SensorsInterpreter {
    private IKeyboardController keyboard;
    private ISoundController sound;
    private List<double[]> list = new LinkedList<>();
    private int licznik = 0;
    private GestureDetector gestureDetector;

    /**
     * Konstruktor domyślny, tworzący obiekty klas do obsługi dźwięku i klawiatury
     *
     * @throws AWTException - wyjątek rzucany przez bibliotekę Robot
     */
    public SensorsInterpreter(Properties config) throws AWTException {
        keyboard = new RobotKeyboardController();
        sound = new AutoHotkeySoundController();
        gestureDetector = new GestureDetector(config);
    }

    /**
     * 
     * @param data
     */
    public void interpreteSensorsData(double[] data) {
        list.add(data);

        if (list.size() > 95) {
            list.remove(0);
        }

        licznik++;

        if (licznik % 2 == 0 && list.size() == 95) {

            String gestureName = gestureDetector.getGestureName(list);

            if (gestureName == null) {
                return;
            }

            list.clear();

            switch (gestureName) {
            	case "zero":
            		list.clear();
            		break;
                case "kolo":
                    sound.mute();
                    break;
                case "gora":
                    sound.increaseSound();
                    break;
                case "dol":
                    sound.decreaseSound();
                    break;
                case "prawo":
                    keyboard.keyPressAndRelease(KeyEvent.VK_RIGHT);
                    break;
                case "lewo":
                    keyboard.keyPressAndRelease(KeyEvent.VK_LEFT);
                    break;
                case "tak":
                    keyboard.keyPressAndRelease(KeyEvent.VK_ENTER);
                    break;
                case "nie":
                    keyboard.keyPressAndRelease(KeyEvent.VK_ESCAPE);
                    break;
                default:
                    break;
            }
        }
    }
}