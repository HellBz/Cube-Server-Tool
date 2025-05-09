package dev.cubie.CubeServerTool.Data;

import dev.cubie.CubeServerTool.CubeServerModule;
import dev.cubie.CubeServerTool.Utils.ConfigHandler;
import dev.cubie.CubeServerTool.Utils.LoggerUtility;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class Config {

    // Get Logger instance from LoggerUtility
    private static final Logger logger = LoggerUtility.getLogger(Config.class);

    public static String appVersion = ConfigHandler.getLocalVersion();

    private static boolean isConfigLoaded = false;  // Flag to check if config is loaded


    public static CubeServerModule selectedInstaller;
    public static String selectedType;
    public static String selectedVersion;
    public static String selectedSubVersion;
    public static Boolean selectedAutoUpdate;

    // Define the base root folder
    public static Path rootFolder = Paths.get(".").toAbsolutePath().normalize();

    // Define the base data folder
    public static Path dataFolder = rootFolder.resolve("cst_data");  // This can be adjusted as needed

    // Define subfolders for better structure using Paths.get()
    public static Path configFolder = dataFolder.resolve("config");
    public static Path logFolder = dataFolder.resolve("logs");
    public static Path modulesFolder = dataFolder.resolve("modules");
    public static Path tempFolder = dataFolder.resolve("temp");

    // Define paths based on the subfolders
    public static Path configFilePath = configFolder.resolve("cst.conf");
    public static Path logFilePath = logFolder.resolve("latest-log.csv");

    // Configurable settings loaded from config
    public static boolean logToFile = false;
    public static String logLevelConsole = "DEBUG";
    public static String logLevelCSV = "DEBUG";
    public static boolean detailedLog = false;

    static {
        // Automatically create directories when the class is loaded
        try {
            ConfigHandler.createRequiredDirectories();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to ensure the config is loaded
    private static void ensureConfigLoaded() {
        if (!isConfigLoaded) {
            loadConfig();
        }
    }

    // Static method to manually load the config
    public static void loadConfig() {
        try {
            logger.info("Loading Configuration.");
            ConfigHandler.loadConfig();  // Load the configuration
        } catch (IOException e) {
            logger.warning("Error loading configuration. Using default settings.");
        }
    }
}
