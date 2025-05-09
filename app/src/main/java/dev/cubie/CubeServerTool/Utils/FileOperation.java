package dev.cubie.CubeServerTool.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FileOperation {

    private String source;
    private Map<String, String> headers = new HashMap<>();
    private String content;
    private byte[] binaryContent;  // For binary files
    private int responseCode;
    private static final Logger logger = LoggerUtility.getLogger(FileOperation.class);

    // Static variable to control logging
    public static boolean noLog = false;  // If true, logs will be suppressed

    // Constructor
    public FileOperation(String source) {
        this.source = source;
    }

    // Method to add headers
    public FileOperation header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    // Methode, um noLog auf true zu setzen und Logging zu deaktivieren
    public FileOperation noLog() {
        noLog = true;
        return this;  // Ermöglicht method chaining
    }

    // Methode, um das Logging zu steuern
    public FileOperation doLog(boolean enableLogging) {
        noLog = !enableLogging;  // Wenn enableLogging false ist, setze noLog auf true und umgekehrt
        return this;  // Ermöglicht method chaining
    }

    // Method to fetch the file from a remote URL, local file, or resource folder
    public FileOperation fetch() {
        try {
            if (source.toLowerCase().startsWith("http://") || source.toLowerCase().startsWith("https://")) {
                fetchFromUrl();
            } else if (Files.exists(Paths.get(source))) {
                fetchFromLocalFile();
            } else {
                fetchFromResource();
            }
        } catch (IOException e) {
            logger.severe("Fetching file failed: " + e.getMessage());
            responseCode = 500;
        }
        return this;
    }

    // Method to fetch the binary file from a remote URL
    public FileOperation fetchBinary() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(source);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Set headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.connect();

            responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                try (InputStream in = connection.getInputStream();
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                    byte[] tempBuffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(tempBuffer)) != -1) {
                        buffer.write(tempBuffer, 0, bytesRead);
                    }
                    binaryContent = buffer.toByteArray();  // Store binary data
                }
                if (!noLog) logger.info("Successfully fetched URL: " + source);
            } else {
                if (!noLog) logger.severe("Failed to fetch URL: " + source + " - Server returned an error.");
            }
        } catch (IOException e) {
            if (!noLog) logger.severe("Fetching file failed: " + e.getMessage());
            responseCode = 500;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return this;
    }

    private String initialTimestamp;

    // Method to fetch the binary file from a remote URL with a visual progress bar (▓ and ░)
    public FileOperation fetchBinaryWithProgressBar() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(source);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Set headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.connect();

            responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                long contentLength = connection.getContentLengthLong(); // Get the content length
                if (contentLength == -1) {
                    if (!noLog) logger.severe("Could not determine file size.");
                    return this;
                } else {
                    if (!noLog) logger.info("File size: " + formatSize(contentLength));
                }

                // Generate the timestamp once at the start
                initialTimestamp = getFormattedTimestamp();

                // Get the file name from the URL
                String fileName = getFileNameFromUrl(url);

                if (!noLog) logger.info("Downloading file: " + fileName );

                try (InputStream in = connection.getInputStream();
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                    byte[] tempBuffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;
                    int lastProgress = 0;

                    // Initial progress bar setup
                    printProgressBar(0, contentLength, 0, fileName);

                    while ((bytesRead = in.read(tempBuffer)) != -1) {
                        buffer.write(tempBuffer, 0, bytesRead);
                        totalBytesRead += bytesRead;

                        // Calculate progress in percentage
                        int progress = (int) ((totalBytesRead * 100) / contentLength);
                        if (progress / 10 > lastProgress / 10) {
                            lastProgress = progress;
                            // Update progress bar
                            printProgressBar(totalBytesRead, contentLength, progress, fileName);
                        }
                    }

                    binaryContent = buffer.toByteArray();  // Store binary data

                    // Print final progress bar at 100%
                    printProgressBar(contentLength, contentLength, 100, fileName);
                    System.out.println();  // Move to a new line after progress bar completion

                }
                if (!noLog) logger.info("Successfully fetched URL: " + source);
            } else {
                if (!noLog) logger.severe("Failed to fetch URL: " + source + " - Server returned an error.");
            }
        } catch (IOException e) {
            if (!noLog) logger.severe("Fetching file failed: " + e.getMessage());
            responseCode = 500;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return this;
    }

    // Helper method to print the progress bar with ▓ and ░ characters and the prefixed timestamp
    private void printProgressBar(long bytesRead, long totalBytes, int progress, String fileName) {
        int barLength = 30;  // Length of the progress bar
        int filledLength = (int) (barLength * progress / 100);

        StringBuilder bar = new StringBuilder();
        bar.append("[");

        // Create the filled portion of the bar using ▓
        for (int i = 0; i < filledLength; i++) {
            bar.append("#");
        }

        // Create the unfilled portion of the bar using ░
        for (int i = filledLength; i < barLength; i++) {
            bar.append("-");
        }

        bar.append("] ");

        // Print progress percentage and size information with the prefixed timestamp
        System.out.printf("\r%s %sDOWNLOAD" +
                        ":%s %s %d%% (%s / %s) File: %s",
                initialTimestamp,
                ConsoleColors.PURPLE_BRIGHT, ConsoleColors.RESET,
                bar.toString(), progress, formatSize(bytesRead), formatSize(totalBytes), fileName);

        System.out.flush();  // Ensure the progress bar is displayed in real-time
    }

    // Helper method to format file sizes in a human-readable format (MB, GB, etc.)
    private String formatSize(long sizeInBytes) {
        double size = sizeInBytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    // Method to get the formatted timestamp
    private String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "[" + LocalDateTime.now().format(formatter) + "]";
    }

    // Helper method to extract the file name from the URL object
    private String getFileNameFromUrl(URL url) {
        String filePath = url.getPath();  // Get the path of the URL
        return filePath.substring(filePath.lastIndexOf('/') + 1);  // Extract the file name
    }

    // Method to fetch from a URL
    private void fetchFromUrl() throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(source);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Set headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.connect();

            responseCode = connection.getResponseCode();
            InputStream in = connection.getInputStream();

            content = readContentFromStream(in);

            if (responseCode >= 200 && responseCode < 300) {
                if (!noLog) logger.info("Successfully fetched URL: " + source);
            } else {
                if (!noLog) logger.severe("Failed to fetch URL: " + source + " - Server returned an error.");
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Method to fetch from a local file
    private void fetchFromLocalFile() throws IOException {
        content = new String(Files.readAllBytes(Paths.get(source)), StandardCharsets.UTF_8);
        responseCode = 200;
        if (!noLog) logger.info("Successfully read local file: " + source);
    }

    // Method to fetch from resources
    private void fetchFromResource() throws IOException {
        try (InputStream in = getClass().getResourceAsStream(source)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + source);
            }
            content = readContentFromStream(in);
            responseCode = 200;
            if (!noLog) logger.info("Successfully read resource: " + source);
        }
    }

    // Method to replace content within the file
    public FileOperation replace(String target, String replacement) {
        if (content == null) {
            if (!noLog) logger.severe("No content loaded to perform replacement.");
            return this;
        }

        content = content.replace(target, replacement);
        if (!noLog) logger.info("Replaced \"" + target + "\" with \"" + replacement + "\" in the content.");
        return this;
    }

    // Method to save the content (either binary or text) to a file
    public FileOperation saveTo(String destinationPath) {
        if (binaryContent != null) {
            // Save binary content
            try (OutputStream out = Files.newOutputStream(Paths.get(destinationPath), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                out.write(binaryContent);
                if (!noLog) logger.info("Binary file saved to: " + destinationPath);
            } catch (IOException e) {
                if (!noLog) logger.severe("Failed to save binary file: " + e.getMessage());
            }
        } else if (content != null) {
            // Save text content
            try {
                Files.write(Paths.get(destinationPath), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                if (!noLog) logger.info("Text file saved to: " + destinationPath);
            } catch (IOException e) {
                if (!noLog) logger.severe("Failed to save text file: " + e.getMessage());
            }
        } else {
            if (!noLog) logger.severe("No content available to save.");
        }
        return this;
    }

    // Method to cache the content
    public FileOperation cache(long maxAgeMillis, String cachePath) {
        File cacheFile = new File(cachePath);
        if (cacheFile.exists() && (System.currentTimeMillis() - cacheFile.lastModified() < maxAgeMillis)) {
            if (!noLog) logger.info("Using cached file: " + cachePath);
            try {
                content = new String(Files.readAllBytes(Paths.get(cachePath)), StandardCharsets.UTF_8);
                responseCode = 304;  // Set response code to indicate the file was loaded from cache
            } catch (IOException e) {
                if (!noLog) logger.severe("Failed to read cached file: " + e.getMessage());
                responseCode = 500;
            }
        } else {
            if (content != null && responseCode == 200) {
                saveTo(cachePath);
            }
        }
        return this;
    }

    // Getter for the content
    public String getContent() {
        return content;
    }

    // Getter for the response code
    public int getResponseCode() {
        return responseCode;
    }

    // Static method to create a FileOperation instance
    public static FileOperation getFile(String source) {
        return new FileOperation(source);
    }

    // Helper method to read content from an InputStream
    private String readContentFromStream(InputStream in) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        return contentBuilder.toString();
    }

    // Method to extract the basename (filename) from a URL or a local file path
    public static String getBaseName(String urlOrPath) {
        // Try to parse the string as a URL and extract the path part if it's a valid URL
        try {
            URL url = new URL(urlOrPath);
            urlOrPath = url.getPath();  // Extract the path part from the URL
        } catch (MalformedURLException e) {
            // If it's not a valid URL, treat it as a local file path
        }

        // Extract the filename from the path
        return Paths.get(urlOrPath).getFileName().toString();
    }

    // Method to resolve the full path by combining the root folder and the basename
    public static String resolveBaseToFolder(Path getFolder, String urlOrPath) {
        String fileName = getBaseName(urlOrPath);  // Get the basename from the URL or path
        return getFolder.resolve(fileName).toAbsolutePath().toString();  // Combine root folder and basename
    }

    // Main method for testing
    public static void main(String[] args) {
        if (!noLog) logger.info("Starting FileOperation tests...");

        // Test replacing content in a file from the resource folder and saving it locally
        FileOperation resourceFile = FileOperation.getFile("/config/resource_file.txt")
                .fetch()
                .replace("oldText", "newText")
                .replace("anotherOldText", "anotherNewText")
                .saveTo("resource_copy.txt");

        if (resourceFile.getResponseCode() == 200) {
            System.out.println("Resource file content after replacements: " + resourceFile.getContent());
        } else {
            if (!noLog) logger.severe("Failed to fetch or process the resource file.");
        }

        // Test downloading a remote file and saving it locally
        FileOperation downloadResult = FileOperation.getFile("https://example.com/remote_file.txt")
                .header("Authorization", "Bearer your_token_here")
                .fetch()
                .saveTo("remote_file.txt");

        if (downloadResult.getResponseCode() == 200) {
            if (!noLog) logger.info("Downloaded file content: " + downloadResult.getContent());
        } else {
            if (!noLog) logger.severe("Failed to download file. Response code: " + downloadResult.getResponseCode());
        }

        // Test reading a remote file with cache support
        FileOperation cacheResult = FileOperation.getFile("https://example.com/remote_file.txt")
                .fetch()
                .cache(60000, "cached_file.txt");

        if (cacheResult.getResponseCode() == 200) {
            if (!noLog) logger.info("Using cached file content: " + cacheResult.getContent());
        } else {
            if (!noLog) logger.severe("Failed to fetch or cache the file. Response code: " + cacheResult.getResponseCode());
        }

        if (!noLog) logger.info("FileOperation tests completed.");

        // Test fetching, caching, and saving a remote file
        FileOperation remoteSaveReadResultCache = FileOperation.getFile("https://api.curseforge.com/v1/minecraft/modloader/?version=1.20.4")
                .fetch()
                .cache(60000, "ModLoaderCache.json");

        if (remoteSaveReadResultCache.getResponseCode() == 200) {
            if (!noLog) logger.info("File successfully fetched from URL and saved.");
        } else if (remoteSaveReadResultCache.getResponseCode() == 304) {
            if (!noLog) logger.info("File loaded from cache.");
        } else {
            if (!noLog) logger.severe("Failed to fetch the file. Response code: " + remoteSaveReadResultCache.getResponseCode());
        }

    }

}
