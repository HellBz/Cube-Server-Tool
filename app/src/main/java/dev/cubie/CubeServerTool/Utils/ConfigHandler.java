package dev.cubie.CubeServerTool.Utils;

import dev.cubie.CubeServerTool.Data.Config;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigHandler {

    // Get Logger instance from LoggerUtility
    private static final Logger logger = LoggerUtility.getLogger(ConfigHandler.class);

    private static Properties properties = new Properties();  // Store loaded properties

    // Method to load properties from the config file
    public static void loadConfig() throws IOException {

        logger.info("Configuration loaded.");

        // Ensure directories are created before loading the config
        createRequiredDirectories();

        if (Files.exists(Config.configFilePath)) {
            properties.load(Files.newBufferedReader(Config.configFilePath, StandardCharsets.UTF_8));

            // Set static variables in Config from properties
            Config.logToFile = Boolean.parseBoolean(properties.getProperty("logToFile", "true"));
            Config.logLevelConsole = properties.getProperty("logLevelConsole", "INFO");
            Config.logLevelCSV = properties.getProperty("logLevelCSV", "DEBUG");
            Config.detailedLog = Boolean.parseBoolean(properties.getProperty("detailedLog", "false"));


            logger.config("Configuration loaded from: " + Config.configFilePath.toAbsolutePath());
            LoggerUtility.updateLoggerConfig(Config.detailedLog, Config.logLevelConsole, Config.logLevelCSV, Config.logToFile);


        } else {
            logger.warning("Configuration file not found. Creating a default one.");
            createDefaultConfig();
            loadConfig();  // Reload the config after creating default
        }
    }

    // Method to copy the default config from resources to cst_data/config
    public static void createDefaultConfig() throws IOException {

        // Create the directory if it doesn't exist
        if (!Files.exists(Config.configFolder)) {
            Files.createDirectories(Config.configFolder);
        }

        // Check if the config file already exists
        if (!Files.exists(Config.configFilePath)) {
            // Load the default config from resources
            InputStream defaultConfigStream = ConfigHandler.class.getClassLoader().getResourceAsStream("default-cst.conf");

            if (defaultConfigStream == null) {
                throw new FileNotFoundException("Default configuration file not found in resources.");
            }

            // Copy the default config to the destination directory
            Files.copy(defaultConfigStream, Config.configFilePath, StandardCopyOption.REPLACE_EXISTING);
            logger.config("Default configuration file copied to: " + Config.configFilePath.toAbsolutePath());
        } else {
            logger.config("Configuration file already exists at: " + Config.configFilePath.toAbsolutePath());
        }
    }

    // Method to create the directories if they don't exist
    public static void createRequiredDirectories() throws IOException {
            if (!Files.exists(Config.dataFolder)) {
                Files.createDirectories(Config.dataFolder);
                logger.info("Created data folder: " + Config.dataFolder.toAbsolutePath());
            }
            if (!Files.exists(Config.configFolder)) {
                Files.createDirectories(Config.configFolder);
                logger.info("Created config folder: " + Config.configFolder.toAbsolutePath());
            }
            if (!Files.exists(Config.logFolder)) {
                Files.createDirectories(Config.logFolder);
                logger.info("Created log folder: " + Config.logFolder.toAbsolutePath());
            }
            if (!Files.exists(Config.modulesFolder)) {
                Files.createDirectories(Config.modulesFolder);
                logger.info("Created modules folder: " + Config.modulesFolder.toAbsolutePath());
            }
            if (!Files.exists(Config.tempFolder)) {
                Files.createDirectories(Config.tempFolder);
                logger.info("Created temp folder: " + Config.tempFolder.toAbsolutePath());
            }
        }

    // Update or add a property in the config file
    public static void updateProperty(String propertiesFilePath, String key, String newValue) throws IOException {
        Properties properties = new Properties();
        Path filePath = Paths.get(propertiesFilePath);

        if (Files.exists(filePath)) {
            properties.load(Files.newBufferedReader(filePath, StandardCharsets.UTF_8));
        }

        properties.setProperty(key, newValue);

        // Save updated properties back to file
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            properties.store(writer, "Updated " + key);
        }

        logger.config("Property updated: " + key + " = " + newValue);
    }

    // Methode, die die aktuelle Version ausliest
    public static String getLocalVersion() {
        FileOperation result = FileOperation.getFile("/installerInfo.json").noLog().fetch();  // Reads the file from the resources folder

        // Überprüfe den ResponseCode und lese die Datei, falls erfolgreich
        if (result.getResponseCode() == 200) {
            return new JSONObject(result.getContent()).optString("version", "0.0.0");
        }

        return "0.0.0";
    }
}
