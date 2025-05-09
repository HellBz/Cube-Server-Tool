package dev.cubie.CubeServerTool;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstallerTable {

    public static void main(String[] args) {
        // Beispiel für Installer, Versionen oder Subversionen
        List<String> installers = new ArrayList<>();
        installers.add("Installer1");
        installers.add("Installer2");
        installers.add("Installer3");
        installers.add("VeryLongInstallerName");
        installers.add("Installer5");
        installers.add("Installer6");
        installers.add("Installer7");
        installers.add("Installer1");
        installers.add("Installer2");
        installers.add("Installer3");
        installers.add("VeryLongInstallerName");
        installers.add("Installer5");
        installers.add("Installer6");
        installers.add("Installer1");
        installers.add("Installer2");
        installers.add("Installer3");
        installers.add("VeryLongInstallerName");
        installers.add("Installer5");
        installers.add("Installer6");
        installers.add("Installer7");
        installers.add("Installer1");
        installers.add("Installer2");
        installers.add("Installer3");
        installers.add("VeryLongInstallerName");
        installers.add("Installer5");
        installers.add("Installer6");
        installers.add("Installer7");
        installers.add("Installer1");
        installers.add("Installer2");
        installers.add("Installer3");
        installers.add("VeryLongInstallerName");
        installers.add("Installer5");
        installers.add("Installer6");
        installers.add("Installer1");
        installers.add("Installer2");
        installers.add("Installer3");
        installers.add("VeryLongInstallerName");
        installers.add("Installer5");
        installers.add("Installer6");
        installers.add("Installer7");
        installers.add("Installer1");
        installers.add("Installer2");
        installers.add("Installer3");
        installers.add("VeryLongInstallerName");
        installers.add("Installer5");
        installers.add("Installer6");
        installers.add("Installer7");
        installers.add("Installer1");
        installers.add("Installer2");
        installers.add("Installer3");
        installers.add("VeryLongInstallerName");
        installers.add("Installer5");
        installers.add("Installer6");
        installers.add("Installer1");
        installers.add("Installer2");
        installers.add("Installer3");
        installers.add("VeryLongInstallerName");
        installers.add("Installer5");
        installers.add("Installer6");
        installers.add("Installer7");

        // Einträge alphabetisch sortieren
        Collections.sort(installers);

        // Längsten Eintrag finden
        int maxLength = findMaxLength(installers);

        // Breite der Konsole ermitteln
        int consoleWidth = getConsoleWidth();

        // Tabelle drucken
        printTable(installers, consoleWidth, maxLength);
    }

    private static int findMaxLength(List<String> items) {
        int maxLength = 0;
        for (int i = 0; i < items.size(); i++) {
            String numberedItem = (i + 1) + ". " + items.get(i);
            if (numberedItem.length() > maxLength) {
                maxLength = numberedItem.length();
            }
        }
        return maxLength;
    }

    private static int getConsoleWidth() {
        // Standardbreite, falls die Konsole nicht ermittelt werden kann
        int defaultWidth = 80;

        // Versuche die Breite der Konsole zu ermitteln
        try {
            Console console = System.console();
            if (console != null) {
                String columns = System.getenv("COLUMNS");
                if (columns != null) {
                    return Integer.parseInt(columns);
                }
            }
        } catch (Exception e) {
            // Ignoriere Fehler und verwende die Standardbreite
        }

        return defaultWidth;
    }

    private static void printTable(List<String> items, int consoleWidth, int maxLength) {
        // Die Spaltenbreite entspricht der Länge des längsten Eintrags + 2 Leerzeichen zur Trennung
        int columnWidth = maxLength + 2;
        int numColumns = Math.max(1, consoleWidth / columnWidth) + 3 ;

        // Drucke die Elemente in einer tabellarischen Struktur
        for (int i = 0; i < items.size(); i++) {
            String numberedItem = (i + 1) + ". " + items.get(i);
            System.out.printf("%-" + columnWidth + "s", numberedItem);
            if ((i + 1) % numColumns == 0 || i == items.size() - 1) {
                System.out.println(); // Neue Zeile nach jeder vollen Reihe oder am Ende der Liste
            }
        }
    }
}
