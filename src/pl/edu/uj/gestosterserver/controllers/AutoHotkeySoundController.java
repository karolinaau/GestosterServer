package pl.edu.uj.gestosterserver.controllers;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;

/**
 * Klasa implementująca obsługę dźwięku przy wykorzystaniu natywnej biblioteki AutoHotkey.dll
 */
public class AutoHotkeySoundController implements ISoundController {
    public interface IAutoHotKeyDll extends Library {
        void ahkExec(WString s);
        //void ahkdll(WString s, WString o, WString p);
        //void addFile(WString s, int a);
        void ahktextdll(WString s, WString o, WString p);
        //Pointer ahkFunction(WString f);
    }

    // referencja do biblioteki
    private IAutoHotKeyDll lib;

    /**
     * Konstruktor ładujący bibliotekę
     */
    public AutoHotkeySoundController() {
        lib = (IAutoHotKeyDll) Native.loadLibrary("AutoHotkey.dll", AutoHotkeySoundController.IAutoHotKeyDll.class);

        lib.ahktextdll(new WString(""), new WString(""), new WString(""));
    }

    @Override
    public void increaseSound() {
        lib.ahkExec(new WString("SoundSet +5"));
    }

    @Override
    public void decreaseSound() {
        lib.ahkExec(new WString("SoundSet -5"));
    }
    
    @Override
    public void mute() {
        lib.ahkExec(new WString("SoundSet, +1, , mute "));
    }
}
