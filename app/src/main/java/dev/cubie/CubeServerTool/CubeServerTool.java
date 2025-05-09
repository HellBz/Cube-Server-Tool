package dev.cubie.CubeServerTool;

import dev.cubie.CubeServerTool.Data.Config;
import dev.cubie.CubeServerTool.Utils.ConfigHandler;
import dev.cubie.CubeServerTool.Utils.LoggerUtility;
import dev.cubie.CubeServerTool.Data.InstallerUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// https://chatgpt.com/c/69609c43-014f-47da-8556-cb95b05d6f38

public class CubeServerTool {

    public static void main(String[] args) {

        // Initialize LoggerUtility after the config is loaded
        Logger logger = LoggerUtility.getLogger(CubeServerTool.class);


        String cVersion = centerText( Config.appVersion, 15);

        System.out.println("\n" +
                "   ________________  \n" +
                "  /               /| Created by HellBz \n" +
                " /_______________/ | GitHub:    https://github.com/HellBz\n" +
                "|     Cube      |  | Twitter:   https://x.com/HellBz\n" +
                "|    Server     |  | Discord:   https://discord.gg/tuzpmeZ\n" +
                "|     Tool      |  | Steam:     https://s.team/u/hellbz\n" +
                "|"+ cVersion + "| /  Facebook:  https://fb.com/hellbz\n" +
                "|_______________|/   Instagram: https://instagram.com/h3llbz\n\n" +
                "------------------------------------------------------------");

        // Load configuration at the start
        try {
            ConfigHandler.loadConfig();
        } catch (IOException e) {
            logger.severe("Error loading configuration: " + e.getMessage());
        }

        logger.info("Starting the CubeServerTool...");

        try {
            // 1. Load internal installers
            logger.info("Loading internal Installer-Modules...");
            List<CubeServerModule> installers = new ArrayList<>(CubeServerModule.loadInternalInstallers());

            // 2. Load external JARs
            logger.info("Loading external JARs from " + Config.modulesFolder + "...");
            CubeServerModule.loadExternalJars( Config.modulesFolder, installers);

            // Check if any installers were loaded
            if (installers.isEmpty()) {
                logger.severe("No installers were loaded. Exiting.");
                System.exit(1);
            }

            // 3. Installer selection
            InstallerUI.selectInstaller(installers);

            // 3.5. Version selection
            InstallerUI.selectType();

            // 4. Version selection
            InstallerUI.selectVersion();

            // 5. Subversion selection (if available)
            InstallerUI.selectSubVersion();

            // 6. Auto-update selection
            InstallerUI.selectAutoUpdate();

            // 7. Start installation
            Config.selectedInstaller.init();

            // Log auto-update option
            if (Config.selectedAutoUpdate) {
                logger.info("Auto-update is enabled. (This is a future feature)");
            } else {
                logger.info("Auto-update is disabled.");
            }

            Config.selectedInstaller.install();
            Config.selectedInstaller.start();


        } catch (Exception e) {
            logger.severe("An error occurred: " + e.getMessage());
            //e.printStackTrace();
        } finally {
            logger.info("Installation process completed. Exiting.");
            System.exit(0);
        }
    }

    public static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width); // Kürzt, wenn der Text zu lang ist
        }
        int paddingTotal = width - text.length();
        int paddingStart = (paddingTotal + 1) / 2; // +1 sorgt für mehr Leerzeichen vorn bei ungeraden Zahlen
        int paddingEnd = paddingTotal / 2;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < paddingStart; i++) {
            builder.append(" ");
        }
        builder.append(text);
        for (int i = 0; i < paddingEnd; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }
}
