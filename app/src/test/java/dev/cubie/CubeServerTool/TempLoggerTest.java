package dev.cubie.CubeServerTool;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TempLoggerTest {

    public static void main(String[] args) {
        Logger tempLogger = Logger.getLogger("TempLogger");
        tempLogger.setUseParentHandlers(false);  // Verhindert Ausgabe über Parent-Logger

        // ConsoleHandler hinzufügen
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);  // Stelle sicher, dass alle Levels geloggt werden
        tempLogger.addHandler(consoleHandler);

        // Logger-Level setzen
        tempLogger.setLevel(Level.ALL);  // Setze auf das niedrigste Level

        // Testlog-Nachrichten
        tempLogger.info("This is an info message");
        tempLogger.warning("This is a warning message");
        tempLogger.severe("This is a severe error message");
    }
}