package dev.cubie.CubeServerTool.Data;

import dev.cubie.CubeServerTool.CubeServerModule;
import dev.cubie.CubeServerTool.Utils.LoggerUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Logger;

import static dev.cubie.CubeServerTool.Data.DataTablePrinter.printTable;

public class InstallerUI {

    private static final Logger logger = LoggerUtility.getLogger(InstallerUI.class);

    // Method to select an installer from the list
    public static void selectInstaller(List<CubeServerModule> installers) {
        logger.info("Available installers:");
        List<String> installerNames = new ArrayList<>();
        for (CubeServerModule installer : installers) {
            installerNames.add(installer.getInstallerName());
        }
        printTable(installerNames);

        logger.info("Select an installer (1-" + installers.size() + "): ");
        Scanner scanner = new Scanner(System.in);
        int installerChoice;
        try {
            installerChoice = scanner.nextInt();
        } catch (NoSuchElementException e) {
            installerChoice = 1; // Default choice
            logger.warning("No input provided, using default installer: " + installerChoice);
        }
        Config.selectedInstaller = installers.get(installerChoice - 1);
    }
    // Method to select the Version-Type from the installer
    public static void selectType() {
        List<String> availableTypes = Arrays.asList(Config.selectedInstaller.getAvailableTypes());
        if (!availableTypes.isEmpty()) {
            logger.info("Available Types:");
            printTable(availableTypes, 4, "[", "]");

            logger.info("Select a Type (1-" + availableTypes.size() + "): ");
            Scanner scanner = new Scanner(System.in);
            int subVersionChoice;
            try {
                subVersionChoice = scanner.nextInt();
            } catch (NoSuchElementException e) {
                subVersionChoice = 1; // Default choice
                logger.warning("No input provided, using default subVersion: " + subVersionChoice);
            }
            Config.selectedType = availableTypes.get(subVersionChoice - 1);
        }
    }

    // Method to select a version from the installer
    public static void selectVersion() {
        List<String> availableVersions = Arrays.asList(Config.selectedInstaller.getAvailableVersions());
        logger.info("Available versions:");
        printTable(availableVersions, 4, "[", "]");

        logger.info("Select a version (1-" + availableVersions.size() + "): ");
        Scanner scanner = new Scanner(System.in);
        int versionChoice;
        try {
            versionChoice = scanner.nextInt();
        } catch (NoSuchElementException e) {
            versionChoice = 1; // Default choice
            logger.warning("No input provided, using default version: " + versionChoice);
        }
        Config.selectedVersion =  availableVersions.get(versionChoice - 1);
    }

    // Method to select a subversion if available
    public static void selectSubVersion() {
        List<String> availableSubVersions = Arrays.asList(Config.selectedInstaller.getAvailableSubVersions());
        if (!availableSubVersions.isEmpty()) {
            logger.info("Available subversions:");
            printTable(availableSubVersions, 4, "[", "]");

            logger.info("Select a subversion (1-" + availableSubVersions.size() + "): ");
            Scanner scanner = new Scanner(System.in);
            int subVersionChoice;
            try {
                subVersionChoice = scanner.nextInt();
            } catch (NoSuchElementException e) {
                subVersionChoice = 1; // Default choice
                logger.warning("No input provided, using default subVersion: " + subVersionChoice);
            }
            Config.selectedSubVersion =  availableSubVersions.get(subVersionChoice - 1);
        }
    }

    // Method to select auto-update option
    public static void selectAutoUpdate() {
        logger.info("Enable auto-update? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String autoUpdateChoice;
        try {
            autoUpdateChoice = scanner.next();
        } catch (NoSuchElementException e) {
            autoUpdateChoice = "no"; // Default choice
            logger.warning("No input provided, using default choice: " + autoUpdateChoice);
        }
        Config.selectedAutoUpdate = autoUpdateChoice.equalsIgnoreCase("yes");
    }
}
