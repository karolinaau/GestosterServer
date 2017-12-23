package pl.edu.uj.gestosterserver.controllers;

/**
 * Abstrakcja służąca do kontroli dźwięku w systemie
 */
public interface ISoundController {
    /**
     * Podgłaszanie dźwięku
     */
    public void increaseSound();

    /**
     * Sciszanie dźwięku
     */
    public void decreaseSound();

    /**
     * Wyłączenie/włączenie dźwięku
     */
    public void mute();
}
