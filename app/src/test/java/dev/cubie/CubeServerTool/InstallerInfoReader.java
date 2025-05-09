package dev.cubie.CubeServerTool;

import dev.cubie.CubeServerTool.Data.Config;
import dev.cubie.CubeServerTool.Utils.FileOperation;
import dev.cubie.CubeServerTool.Utils.LoggerUtility;
import org.json.JSONObject;

import java.util.logging.Logger;

public class InstallerInfoReader {

    private static final Logger logger = LoggerUtility.getLogger(InstallerInfoReader.class);

    public static String getInstallerVersion() {
        // Use FileOperation to read installerInfo.json from the resources
        FileOperation result = FileOperation.getFile("/installerInfo.json")
                .fetch();  // Reads the file from the resources folder

        // Check if the file was successfully read
        if (result.getResponseCode() != 200 || result.getContent() == null) {
            logger.severe("installerInfo.json could not be found or read.");
            return "0.0.0";  // Fallback to a neutral version
        }

        // Content of the JSON file as a string
        String content = result.getContent().toString();
        logger.info("Content of installerInfo.json: " + content);

        // Use org.json to parse the JSON file
        JSONObject jsonObject = new JSONObject(content);

        // Extract the version from the JSON file
        return jsonObject.optString("version", "0.0.0");
    }

    public static String getRemoteInstallerVersion() {

        String cacheFile = Config.tempFolder.resolve("installerInfo.json").toAbsolutePath().toString();
        // Use FileOperation to read installerInfo.json from the resources
        FileOperation result = FileOperation.getFile("https://raw.githubusercontent.com/HellBz/Minecraft-Server-Installer/master/app/src/main/resources/installerInfo.json")
                .cache(60000, cacheFile)  // Checks the cache: If the file exists in the cache and is younger than 60 seconds, it is loaded from the cache.
                .fetch();  // Only downloads the file from the URL if it is not in the cache or if the cache has expired.

        logger.info("RESPONSE: " + result.getResponseCode() );

        // Check if the file was successfully read
        if ( (result.getResponseCode() != 200 && result.getResponseCode() != 304) || result.getContent() == null) {
            logger.severe("installerInfo.json could not be found or read.");
            return "0.0.0";  // Fallback to a neutral version
        }

        // Content of the JSON file as a string
        String content = result.getContent().toString();
        logger.info("Content of installerInfo.json: " + content);

        // Use org.json to parse the JSON file
        JSONObject jsonObject = new JSONObject(content);

        // Extract the version from the JSON file
        return jsonObject.optString("version", "0.0.0");
    }

    public static void main(String[] args) {
        // Version aus installerInfo.json lesen
        String version = getInstallerVersion();

        String remoteVersion = getRemoteInstallerVersion();

        // Ausgabe der Version
        System.out.println("Installer-Version: " + version);

        // Ausgabe der Version
        System.out.println("Remote-Installer-Version: " + remoteVersion);
    }
}