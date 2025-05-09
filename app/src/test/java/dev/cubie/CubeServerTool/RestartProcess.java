package dev.cubie.CubeServerTool;

import dev.cubie.CubeServerTool.Utils.LoggerUtility;
import dev.cubie.CubeServerTool.Utils.ProcessHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class RestartProcess {

    // Static reference to the ProcessHandler
    private static ProcessHandler processHandler;

    // Logger for logging process events
    private static final Logger logger = LoggerUtility.getLogger(RestartProcess.class);

    public static void main(String[] args) {

        try {
            // Start the process for the first time
            restartProcess();

            // Monitor console input in a separate thread
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equalsIgnoreCase("/restart")) {
                            logger.info("Sending stop command to process...");
                            sendStopCommand();  // Send the "stop" command to the Minecraft server

                            // Wait for the process to exit
                            int exitCode = processHandler.getProcess().waitFor();
                            if (exitCode == 0) {
                                logger.info("Process exited with code 0, restarting...");
                                restartProcess();  // Restart the process after it exits cleanly
                            } else {
                                logger.severe("Process exited with code: " + exitCode);
                            }
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    logger.severe("Error reading console input: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
        }
    }

    // Method to start or restart the process
    private static void restartProcess() throws IOException, InterruptedException {
        // Stop the previous process if it exists
        if (processHandler != null) {
            logger.info("Stopping the existing process...");
            processHandler.stop();  // Use the stop method to stop the current process
        }

        // Start a new process using ProcessHandler
        processHandler = ProcessHandler.create("D:\\GIT\\Minecraft-Server-Installer\\Minecraft-Server-Installer\\RUN-TEST\\server.jar")
                .addJvmArgument("-Xms512M")  // Add JVM arguments
                .addParameter("nogui")  // Add server-specific argument (e.g., for Minecraft server)
                .useConsole(true);

        processHandler.start();               // Start the process

        logger.info("Process started.");
    }

    // Method to send the "stop" command to the Minecraft server
    private static void sendStopCommand() throws IOException {
        if (processHandler != null && processHandler.getProcess() != null) {
            // Send "stop" command to the process
            processHandler.sendCommand("stop");
        }
    }
}
