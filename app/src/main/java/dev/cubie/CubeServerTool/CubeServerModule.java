package dev.cubie.CubeServerTool;

import dev.cubie.CubeServerTool.Utils.LoggerUtility;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import java.util.regex.Pattern;

// Schnittstelle für CubeServerTool
public interface CubeServerModule {

    void init();
    String getInstallerName();
    String[] getAvailableTypes();
    String[] getAvailableVersions();
    String[] getAvailableSubVersions();

    Pattern getStartFile();

    void install();
    void start();

    // Get Logger instance from LoggerUtility
    static final Logger logger = LoggerUtility.getLogger(CubeServerModule.class);

    static List<CubeServerModule> loadInternalInstallers() {
        List<CubeServerModule> installers = new ArrayList<>();

        try {
            String packageName = "dev.cubie.CubeServerTool.Modules";
            String path = packageName.replace('.', '/');
            URL resource = CubeServerTool.class.getClassLoader().getResource(path);

            if (resource != null) {
                if (resource.getProtocol().equals("file")) {
                    // Läuft in einer Entwicklungsumgebung
                    File directory = new File(resource.toURI());
                    if (directory.exists()) {
                        for (File subdir : directory.listFiles(File::isDirectory)) {
                            String subdirName = subdir.getName();
                            File classFile = new File(subdir, subdirName + ".class");
                            if (classFile.exists()) {
                                String className = packageName + '.' + subdirName + '.' + subdirName;
                                Class<?> clazz = Class.forName(className);
                                if (CubeServerModule.class.isAssignableFrom(clazz)) {
                                    CubeServerModule installer = (CubeServerModule) clazz.getDeclaredConstructor().newInstance();
                                    installers.add(installer);
                                    logger.info("Loaded internal installer: " + subdirName);
                                }
                            }
                        }
                    }
                } else if (resource.getProtocol().equals("jar")) {
                    // Läuft innerhalb einer JAR-Datei
                    String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
                    try (JarFile jarFile = new JarFile(jarPath)) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String entryName = entry.getName();
                            if (entryName.startsWith(path) && entryName.endsWith(".class")) {
                                String relativePath = entryName.substring(path.length() + 1);
                                int slashIndex = relativePath.indexOf('/');
                                if (slashIndex != -1) {
                                    String subdirName = relativePath.substring(0, slashIndex);
                                    String className = packageName + '.' + subdirName + '.' + subdirName;
                                    if (relativePath.equals(subdirName + "/" + subdirName + ".class")) {
                                        Class<?> clazz = Class.forName(className);

                                        // Prüfen, ob die Klasse ein CubeServerModule ist
                                        if (CubeServerModule.class.isAssignableFrom(clazz)) {
                                            // Prüfen, ob die Variable 'isPublic' existiert
                                            boolean loadClass = false;
                                            try {
                                                // Reflektiere die Variable 'isPublic'
                                                java.lang.reflect.Field isPublicField = clazz.getField("isPublic");

                                                // Überprüfe den Wert der statischen Variable 'isPublic'
                                                boolean isPublic = isPublicField.getBoolean(null); // Null, da es eine statische Variable ist

                                                // Lade nur, wenn 'isPublic' true ist
                                                if (isPublic) {
                                                    loadClass = true;
                                                }
                                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                                // Falls die Variable nicht existiert oder nicht zugänglich ist, Klasse nicht laden
                                                logger.warning("Field 'isPublic' not found or not accessible in class: " + className);
                                            }

                                            // Wenn die Klasse geladen werden soll
                                            if (loadClass) {
                                                CubeServerModule installer = (CubeServerModule) clazz.getDeclaredConstructor().newInstance();
                                                installers.add(installer);
                                                logger.info("Loaded internal installer: " + className);
                                            } else {
                                                logger.info("Skipped internal installer: " + className + " (isPublic is false or missing)");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return installers;
    }

    static void loadExternalJars(Path directoryPath, List<CubeServerModule> installers) {
        logger.info("Scanning directory: \"" + directoryPath.toAbsolutePath() + "\" ...");

        if (Files.exists(directoryPath) && Files.isDirectory(directoryPath)) {
            try {
                // Stream all the JAR files in the directory
                Files.list(directoryPath)
                        .filter(path -> path.toString().endsWith(".jar"))  // Filter for JAR files
                        .forEach(jar -> loadSingleJar(jar.toFile(), installers));  // Convert to File and load

            } catch (IOException e) {
                logger.severe("Error reading directory: \"" + directoryPath + "\". " + e.getMessage());
            }
        } else {
            logger.warning("Directory does not exist or is not a directory: \"" + directoryPath.toAbsolutePath() + "\".");
        }
    }

    static void loadSingleJar(File jar, List<CubeServerModule> installers) {
        String status = "loaded"; // Default status
        CubeServerModule externalInstaller = null;

        try {
            externalInstaller = loadInstallerFromJar(jar);
            if (externalInstaller != null) {
                String externalClassName = externalInstaller.getClass().getName();

                if (externalClassName.startsWith("dev.cubie.CubeServerTool.Modules")) {
                    // Prüfen, ob die Variable 'isPublic' existiert und true ist
                    boolean loadClass = false;
                    try {
                        Class<?> clazz = externalInstaller.getClass();

                        // Reflektiere die Variable 'isPublic'
                        java.lang.reflect.Field isPublicField = clazz.getField("isPublic");

                        // Überprüfe den Wert der statischen Variable 'isPublic'
                        boolean isPublic = isPublicField.getBoolean(null); // Null, da es eine statische Variable ist

                        // Lade nur, wenn 'isPublic' true ist
                        if (isPublic) {
                            loadClass = true;
                        } else {
                            status = "skipped (isPublic is false)";
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        // Falls die Variable nicht existiert oder nicht zugänglich ist, Klasse nicht laden
                        status = "skipped (isPublic not found or not accessible)";
                        logger.warning("Field 'isPublic' not found or not accessible in class: " + externalClassName);
                    }

                    // Wenn die Klasse geladen werden soll
                    if (loadClass) {
                        status = handleJarReplacement(externalInstaller, externalClassName, installers);
                    }
                } else {
                    status = "skipped (not in the allowed package)";
                }
            } else {
                status = "skipped (unable to load Main-Class correctly)";
            }
        } catch (Exception e) {
            status = "skipped (error loading class)";
            logger.severe("Error loading installer from JAR: " + jar.getName());
            e.printStackTrace();
        }

        // Log final status
        logger.info("Found external JAR: " + jar.getName() + " - " + status);
    }


    static String handleJarReplacement(CubeServerModule externalInstaller, String externalClassName, List<CubeServerModule> installers) {
        boolean replaced = false;
        for (int i = 0; i < installers.size(); i++) {
            CubeServerModule internalInstaller = installers.get(i);
            String internalClassName = internalInstaller.getClass().getName();

            if (internalClassName.equals(externalClassName)) {
                installers.set(i, externalInstaller);
                replaced = true;
                return "overwrite (" + internalInstaller.getClass().getSimpleName() + ")";
            }
        }

        if (!replaced) {
            installers.add(externalInstaller);
        }
        return "loaded";
    }


    static CubeServerModule loadInstallerFromJar(File jarFile) throws Exception {
        URL jarUrl = jarFile.toURI().toURL();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, CubeServerTool.class.getClassLoader())) {

            // Öffne das JAR und lese die Manifest-Datei
            try (JarFile jar = new JarFile(jarFile)) {
                Manifest manifest = jar.getManifest();
                Attributes attrs = manifest.getMainAttributes();
                String className = attrs.getValue("Main-Class");

                if (className != null) {
                    //System.out.println("Loading class: " + className + " from " + jarFile.getName());
                    //System.out.print("loading, ");
                    Class<?> clazz = Class.forName(className, true, classLoader);
                    return (CubeServerModule) clazz.getDeclaredConstructor().newInstance();
                } else {
                    //System.out.println("No Main-Class attribute found in " + jarFile.getName());
                    return null; // Keine Main-Class angegeben
                }
            }
        } catch (ClassNotFoundException | ClassCastException e) {
            // Ungültige JAR-Datei oder Klasse kann nicht geladen werden
            return null;
        } catch (Exception e) {
            // Allgemeine Fehlerbehandlung für ungültige JAR-Dateien
            e.printStackTrace();
            return null;
        }
    }


}


