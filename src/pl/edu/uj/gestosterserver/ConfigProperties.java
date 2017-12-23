package pl.edu.uj.gestosterserver;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Klasa zawiera konfigurację programu
 */
public class ConfigProperties extends Properties {
    /**
	 * 
	 */
	private static final long serialVersionUID = 85855007667770572L;

	/**
     * Zaczytanie konfiguracji z pliku
     * @param configFile - plik z konfiguracją
     */
    public ConfigProperties(String configFile) {
        super();

        try (
                Reader reader = Files.newBufferedReader(Paths.get(configFile));
        ) {
            load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}