package pl.edu.uj.gestosterserver.controllers;

/**
 * Abstrakcja służąca do kontroli myszki w systemie
 */
public interface IMouseController {
    /**
     * Przesunięcie myszki o współrzędne x,y
     * @param x
     * @param y
     */
    public void moveMouse(int x, int y);

    /**
     * Kliknięcie lub odkliknięcie prawego/lewego klawisza myszki
     * @param mouseActions
     */
    public void clickMouse(MouseActions mouseActions);

    /**
     * Pobiera stan przycisku myszy
     * @param mouseActions
     * @return - <code>true</code> jeżeli przycisk jest wciśnięty, <code>false</code> w przeciwnym wypadku
     */
    public boolean getButtonState(MouseActions mouseActions);
}
