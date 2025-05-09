package dev.cubie.CubeServerTool.Data;

import dev.cubie.CubeServerTool.Utils.LoggerUtility;

import java.util.List;
import java.util.logging.Logger;

public class DataTablePrinter {

    // Initialize LoggerUtility after the config is loaded
    static Logger logger = LoggerUtility.getLogger(DataTablePrinter.class);

    public static void printTable(List<String> items, int numColumns, String prefix, String suffix) {
        int maxLength = 0;

        // Finde die maximale Länge eines Eintrags (inklusive Nummerierung mit Präfix und Suffix)
        for (int j = 0; j < items.size(); j++) {
            String numberedItem = prefix + (j + 1) + suffix + " " + items.get(j);
            if (numberedItem.length() > maxLength) {
                maxLength = numberedItem.length();
            }
        }

        StringBuilder formattedOutput = new StringBuilder();

        // Spaltenbreite definieren
        int columnWidth = maxLength + 2; // Plus 2 für Trennzeichen

        // Einträge in tabellarischer Form ausgeben
        for (int i = 0; i < items.size(); i++) {
            String numberedItem = prefix + (i + 1) + suffix + " " + items.get(i);
            formattedOutput.append(String.format("%-" + columnWidth + "s", numberedItem));
            if ((i + 1) % numColumns == 0 || i == items.size() - 1) {
                // Ausgabe der formatierten Zeilen über den Logger
                logger.info(formattedOutput.toString());
                formattedOutput = new StringBuilder();
                // Neue Zeile nach jeder vollen Reihe oder am Ende der Liste
            }
        }
    }

    // Überladene Methode für den Standardfall (3 Spalten, ohne Präfix und Suffix)
    public static void printTable(List<String> items) {
        printTable(items, 3, "", ".");  // Standardmäßig keine Präfix, Suffix ist "."
    }

    // Überladene Methode für den Standardfall mit festgelegter Spaltenanzahl (ohne Präfix und Suffix)
    public static void printTable(List<String> items, int numColumns) {
        printTable(items, numColumns, "", ".");
    }
}
