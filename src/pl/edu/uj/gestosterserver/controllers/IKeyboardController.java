package pl.edu.uj.gestosterserver.controllers;

/**
 * Abstrakcja s�u��ca do kontroli klawiatury w systemie
 */
public interface IKeyboardController {
    /**
     * Metoda, kt�rej zadaniem jest wirtualne naci�ni�cie na klawiaturze wskazanego klawisza
     * @param keyId - identyfikator klawisza do wci�ni�cia
     */
    public void keyPressAndRelease(int keyId);
}
