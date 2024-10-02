package de.hellbz.MinecraftServerInstaller.Modules.MinecraftForge;

import de.hellbz.MinecraftServerInstaller.Data.Config;
import de.hellbz.MinecraftServerInstaller.MinecraftServerInstaller;

import java.util.regex.Pattern;

// Implementierung des Forge-Installers
public class MinecraftForge implements MinecraftServerInstaller {

    @Override
    public void init() {
        System.out.println("Forge Minecraft Installer initialized.");
    }

    @Override
    public void install() {
        System.out.println("Installing Forge Minecraft version: " + Config.selectedVersion + ", Forge version: " + Config.selectedSubVersion);
    }

    @Override
    public void start() {

    }

    @Override
    public String getInstallerName() {
        return "Minecraft-FORGE";
    }

    @Override
    public String[] getAvailableTypes() {
        return new String[] {"Release", "Snapshot", "All"};
    }

    @Override
    public String[] getAvailableVersions() {
        return new String[] {"1.20.2", "1.19.4"};  // Beispielhafte Forge-Versionen
    }

    @Override
    public String[] getAvailableSubVersions() {
        if ("1.20.2".equals( Config.selectedVersion)) {
            return new String[] {"36.1.0", "36.1.1"};  // Beispielhafte Sub-Versionen
        }
        return new String[0];
    }

    @Override
    public Pattern getStartFile() {
        return null;
    }
}
