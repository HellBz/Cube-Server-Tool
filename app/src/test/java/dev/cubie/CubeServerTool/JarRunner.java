package dev.cubie.CubeServerTool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

//TODO NOT WORKING !!!!!!!!!! Use ProcessHandler

public class JarRunner {

    public static void main(String[] args) {
        // Fester Pfad zur JAR-Datei
        String jarFilePath = "D:\\GIT\\Minecraft-Server-Installer\\Minecraft-Server-Installer\\RUN-TEST\\server.jar";

        try {
            // Lade die JAR-Datei
            File jarFile = new File(jarFilePath);
            JarFile jar = new JarFile(jarFile);

            // Extrahiere die Main-Class aus dem Manifest der JAR-Datei
            Manifest manifest = jar.getManifest();
            String mainClassName = manifest.getMainAttributes().getValue("Main-Class");

            if (mainClassName == null) {
                System.out.println("No Main-Class found in the JAR manifest.");
                return;
            }

            // Lade die JAR-Datei über einen URLClassLoader
            URL jarUrl = jarFile.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl});

            // Lade die Hauptklasse dynamisch
            Class<?> mainClass = classLoader.loadClass(mainClassName);

            // Finde die "main"-Methode der geladenen Klasse
            Method mainMethod = mainClass.getMethod("main", String[].class);

            // Parameter für die "main"-Methode (falls benötigt)
            String[] jarArgs = new String[args.length];
            System.arraycopy(args, 0, jarArgs, 0, args.length);

            // Rufe die "main"-Methode der Hauptklasse auf
            mainMethod.invoke(null, (Object) jarArgs);

            // ClassLoader schließen, um Ressourcen freizugeben
            classLoader.close();

        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
