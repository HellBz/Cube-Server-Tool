package dev.cubie.CubeServerTool.Modules.MinecraftVanilla;

import dev.cubie.CubeServerTool.CubeServerModule;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import dev.cubie.CubeServerTool.Data.Config;
import dev.cubie.CubeServerTool.Utils.FileOperation;
import dev.cubie.CubeServerTool.Utils.LoggerUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import static dev.cubie.CubeServerTool.Utils.FileOperation.resolveBaseToFolder;

// Implementierung des Vanilla-Installers
public class MinecraftVanilla implements CubeServerModule {

    String className = this.getClass().getSimpleName();

    // Statische Variable für die Sichtbarkeit
    public static boolean isPublic = true;

    // Definiere den Pfad zur Cache-Datei
    String cacheFile = Config.tempFolder.resolve( className + "_versions.json").toAbsolutePath().toString();

    String versionFileURL = "https://piston-meta.mojang.com/mc/game/version_manifest.json";

    // Initialize LoggerUtility after the config is loaded
    Logger logger = LoggerUtility.getLogger(MinecraftVanilla.class);

    @Override
    public void init() {
        System.out.println("Vanilla Minecraft Installer initialized.");
    }

    @Override
    public String getInstallerName() {
        return "Minecraft-Vanilla";
    }

    @Override
    public String[] getAvailableTypes() {
        return new String[] {"Release", "Snapshot", "All"};
    }

    @Override
    public String[] getAvailableVersions() {

        // Verwende FileOperation, um die Datei herunterzuladen und zu cachen
        FileOperation downloadResult = FileOperation.getFile(versionFileURL)
                .fetch()
                .cache(60000, cacheFile);  // Prüft den Cache und lädt die Datei nur neu, wenn der Cache abgelaufen ist

        // Überprüfe den ResponseCode, um zu sehen, ob die Datei aus dem Cache oder von der URL geladen wurde
        if (downloadResult.getResponseCode() == 200) {
            logger.info("File successfully fetched from URL and saved.");
        } else if (downloadResult.getResponseCode() == 304) {
            logger.info("File loaded from cache.");
        } else {
            logger.severe("Failed to fetch the file. Response code: " + downloadResult.getResponseCode());
        }

        // JSON parsen und "versions" Array extrahieren
        JSONObject jsonObj = new JSONObject(downloadResult.getContent());
        JSONArray versionsArray = jsonObj.getJSONArray("versions");

        // Verwende eine dynamische Liste anstelle eines Arrays
        List<String> versionIds = new ArrayList<>();

        // Durch das "versions" Array iterieren und die "id" Werte extrahieren
        for (int i = 0; i < versionsArray.length(); i++) {
            JSONObject versionObj = versionsArray.getJSONObject(i);
            String type = versionObj.getString("type");

            // Überprüfe den globalen Filter Config.selectedType
            if ("all".equalsIgnoreCase(Config.selectedType) || type.equalsIgnoreCase(Config.selectedType)) {
                versionIds.add(versionObj.getString("id"));  // Dynamisch in die Liste einfügen
            }

        }
        // Konvertiere die dynamische Liste in ein Array und gib es zurück
        return versionIds.toArray(new String[0]);
    }

    @Override
    public String[] getAvailableSubVersions() {
        return new String[0];  // No sub-versions for Vanilla.
    }

    @Override
    public Pattern getStartFile() {
        String patternString = ".*(server|minecraft_server)\\.jar";
        return Pattern.compile(patternString);
    }

    @Override
    public void install() {
         logger.info("Installing Vanilla Minecraft version: " + Config.selectedVersion);

        // Verwende FileOperation, um die Datei herunterzuladen und zu cachen
        FileOperation downloadResult = FileOperation.getFile("https://piston-meta.mojang.com/mc/game/version_manifest.json")
                .fetch()
                .cache(60000, cacheFile );  // Prüft den Cache und lädt die Datei nur neu, wenn der Cache abgelaufen ist

        // Überprüfe den ResponseCode
        if (downloadResult.getResponseCode() != 200 && downloadResult.getResponseCode() != 304) {
            logger.severe ("Failed to fetch the version manifest file. Response code: " + downloadResult.getResponseCode());
            return;
        }

        // JSON parsen und das "versions" Array extrahieren
        JSONObject jsonObj = new JSONObject(downloadResult.getContent());
        JSONArray versionsArray = jsonObj.getJSONArray("versions");

        // Durch das "versions" Array iterieren, um die passende Version zu finden
        for (int i = 0; i < versionsArray.length(); i++) {
            JSONObject versionObj = versionsArray.getJSONObject(i);

            // Wenn die "id" der gesuchten Version entspricht
            if (versionObj.getString("id").equals(Config.selectedVersion)) {
                String type = versionObj.getString("type");
                String versionUrl = versionObj.getString("url");
                String time = versionObj.getString("time");
                String releaseTime = versionObj.getString("releaseTime");

                System.out.println("Version ID: " + Config.selectedVersion);
                System.out.println("Type: " + type);
                System.out.println("URL: " + versionUrl);
                System.out.println("Time: " + time);
                System.out.println("Release Time: " + releaseTime);

                // Jetzt die URL aufrufen, um weitere Daten zu holen
                FileOperation versionDetailsDownload = FileOperation.getFile(versionUrl).fetch();

                if (versionDetailsDownload.getResponseCode() == 200) {
                    JSONObject versionDetails = new JSONObject(versionDetailsDownload.getContent());

                    // Beispiel: Die "downloads -> server -> url" Information extrahieren
                    if (versionDetails.has("downloads")) {
                        JSONObject downloads = versionDetails.getJSONObject("downloads");
                        if (downloads.has("server")) {
                            String serverDownloadUrl = downloads.getJSONObject("server").getString("url");
                            logger.info("Server download URL: " + serverDownloadUrl);

                            String targetFile = resolveBaseToFolder(Config.rootFolder, serverDownloadUrl);
                            // Lade die Datei herunter und speichere sie im richtigen Verzeichnis
                            FileOperation.getFile(serverDownloadUrl).fetchBinaryWithProgressBar().saveTo( targetFile );
                        } else {
                            logger.warning("No server download available for this version.");
                        }
                    } else {
                        logger.warning("No downloads section available in the version details.");
                    }
                } else {
                    logger.severe("Failed to fetch version details. Response code: " + versionDetailsDownload.getResponseCode());
                }
                break;  // Version gefunden, Schleife abbrechen
            }
        }
    }

    @Override
    public void start() {

    }

}
