package dev.cubie.CubeServerTool.Utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DirectoryLister {

    private final Path directory;
    private final List<String> excludedDirs = new ArrayList<>();
    private final List<String> excludedFiles = new ArrayList<>();
    private final List<String> includedDirs = new ArrayList<>();
    private final List<String> includedFiles = new ArrayList<>();
    private final List<Predicate<Path>> customFilters = new ArrayList<>();
    private static final Logger logger = LoggerUtility.getLogger(DirectoryLister.class);

    private boolean dryRun = false;
    private int maxRetries = 3;  // Default to 3 retries
    private long retryDelay = 1000;  // Default to 1-second delay between retries
    private final List<Path> filteredFiles = new ArrayList<>();  // List to collect filtered files
    private boolean filterDirectoriesOnly = false;
    private boolean filterFilesOnly = false;
    private boolean showFullPath = false;  // New variable to control full path display

    public DirectoryLister(Path directory) {
        this.directory = directory;

        // Hard-coded exclusion of "cst_data"
        this.excludedDirs.add("cst_data");

        // Determine the name of the current JAR file and exclude it if it exists
        String jarName = getJarName();
        if (jarName != null) {
            this.excludedFiles.add(jarName);
        }
    }

    // Method to enable the display of the full path
    public DirectoryLister setShowFullPath(boolean showFullPath) {
        this.showFullPath = showFullPath;
        return this;
    }

    public DirectoryLister excludeDirectory(String dirName) {
        excludedDirs.add(dirName);
        return this;
    }

    public DirectoryLister excludeFile(String regexPattern) {
        excludedFiles.add(regexPattern);
        return this;
    }

    public DirectoryLister includeDirectory(String dirName) {
        includedDirs.add(dirName);
        return this;
    }

    public DirectoryLister includeFile(String regexPattern) {
        includedFiles.add(regexPattern);
        return this;
    }

    public DirectoryLister addCustomFilter(Predicate<Path> customFilter) {
        customFilters.add(customFilter);
        return this;
    }

    public DirectoryLister enableDryRun() {
        this.dryRun = true;
        return this;
    }

    public DirectoryLister setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public DirectoryLister setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }

    public DirectoryLister filterDirectoriesOnly() {
        this.filterDirectoriesOnly = true;
        this.filterFilesOnly = false;  // If both are set, prioritize directories
        return this;
    }

    public DirectoryLister filterFilesOnly() {
        this.filterFilesOnly = true;
        this.filterDirectoriesOnly = false;  // If both are set, prioritize files
        return this;
    }

    public List<Path> getFilteredFiles() {
        return new ArrayList<>(filteredFiles);  // Return a copy of the filtered files
    }

    public void list() {
        filteredFiles.clear();  // Clear the list before processing
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, this::filter)) {
            for (Path entry : stream) {
                filteredFiles.add(entry);  // Add file to the list
                logger.info("Listed: " + entry.getFileName());
            }
        } catch (IOException e) {
            logger.severe("Failed to list directory: " + directory + " due to " + e.getMessage());
        }
    }

    public void rekursiv() {
        filteredFiles.clear();  // Clear the list before processing
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    // Log the directory being visited regardless of filters
                    if (showFullPath) {
                        logger.info("Visiting directory: " + dir.toAbsolutePath());
                    } else {
                        logger.info("Visiting directory: " + dir.getFileName());
                    }

                    // Apply filters
                    if (filter(dir)) {
                        return FileVisitResult.CONTINUE;
                    } else {
                        logger.info("Skipping directory: " + dir.getFileName());
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (filter(file)) {
                        filteredFiles.add(file);  // Add file to the list
                        logger.info("Visiting file: " + file.getFileName());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    logger.warning("Failed to visit file: " + file.getFileName() + " due to " + exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.severe("Failed to walk directory tree: " + directory + " due to " + e.getMessage());
        }
    }

    public void delete() {
        if (dryRun) {
            // Simulate deletion
            for (Path file : filteredFiles) {
                logger.info("Dry Run: Simulating deletion of: " + file.getFileName());
            }
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, this::filter)) {
                for (Path entry : stream) {
                    if (Files.isRegularFile(entry)) {
                        deleteFile(entry);
                    }
                }
            } catch (IOException e) {
                logger.severe("Failed to delete files in directory: " + directory + " due to " + e.getMessage());
            }
        }
    }

    public void deleteFile(Path file) {
        retryOnFailure(() -> {
            try {
                Files.delete(file);
                logger.info("Deleted: " + file.getFileName());
            } catch (IOException e) {
                logger.severe("Failed to delete file: " + file.getFileName() + " due to " + e.getMessage());
                throw new RuntimeException(e);  // Re-throw the exception to trigger retry logic
            }
            return null;
        });
    }

    public void backup(String zipFileName, Path destination) {
        if (dryRun) {
            // Simulate backup
            for (Path file : filteredFiles) {
                logger.info("Dry Run: Simulating adding to zip: " + file.getFileName());
            }
        } else {
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(destination.resolve(zipFileName)))) {
                Files.walkFileTree(directory, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        if (filter(dir)) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (filter(file)) {
                            try {
                                ZipEntry zipEntry = new ZipEntry(directory.relativize(file).toString());
                                zos.putNextEntry(zipEntry);
                                Files.copy(file, zos);
                                zos.closeEntry();
                                logger.info("Added to zip: " + file.getFileName());
                            } catch (IOException e) {
                                logger.severe("Failed to add file to zip: " + file.getFileName() + " due to " + e.getMessage());
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        logger.warning("Failed to visit file: " + file.getFileName() + " due to " + exc.getMessage());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                logger.severe("Failed to create zip file: " + zipFileName + " due to " + e.getMessage());
            }
        }
    }

    private void retryOnFailure(Supplier<Void> action) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                action.get();
                return;  // Success, break the loop
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    logger.severe("Action failed after " + maxRetries + " attempts: " + e.getMessage());
                    break;
                }
                try {
                    Thread.sleep(retryDelay);  // Delay before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();  // Reset the interrupt status
                }
            }
        }
    }

    private String getJarName() {
        try {
            // Get the path of the running JAR file or directory
            Path path = Paths.get(DirectoryLister.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            // Check if it is a JAR file
            if (path.toString().endsWith(".jar")) {
                return path.getFileName().toString();
            } else {
                return null; // No JAR file name if running in development environment
            }
        } catch (URISyntaxException e) {
            logger.severe("Failed to determine the JAR file name: " + e.getMessage());
            return null;
        }
    }

    private boolean filter(Path entry) {
        // Hard-coded exclusion of "cst_data" and the JAR file
        if (entry.getFileName().toString().equals("cst_data") || excludedFiles.contains(entry.getFileName().toString())) {
            return false;
        }

        if (Files.isDirectory(entry)) {
            if (filterFilesOnly) {
                return false;  // Only filtering files, so skip directories
            }
            if (!includedDirs.isEmpty() && !includedDirs.contains(entry.getFileName().toString())) {
                return false;
            }
            return !excludedDirs.contains(directory.relativize(entry).toString());
        } else if (Files.isRegularFile(entry)) {
            if (filterDirectoriesOnly) {
                return false;  // Only filtering directories, so skip files
            }
            if (!includedFiles.isEmpty()) {
                boolean matchesInclude = includedFiles.stream()
                        .anyMatch(pattern -> Pattern.matches(pattern, entry.getFileName().toString()));
                if (!matchesInclude) {
                    return false;
                }
            }
            if (excludedFiles.stream()
                    .anyMatch(pattern -> Pattern.matches(pattern, entry.getFileName().toString()))) {
                return false;
            }
        }

        // Apply custom filters
        for (Predicate<Path> customFilter : customFilters) {
            if (!customFilter.test(entry)) {
                return false;
            }
        }

        return true;  // Return true if all filters pass
    }

    public static void main(String[] args) {
        Path dir = Paths.get("../");
        Path backupDir = Paths.get("../");

        // Example: Recursive listing with backup and deletion in dry run mode
        DirectoryLister lister = new DirectoryLister(dir)
                //.includeDirectory("importantDir")
                //.includeFile(".*\\.txt$")
                //.excludeDirectory("excludedDir")
                //.excludeFile(".*\\.log$")
                .setShowFullPath(true)  // Set to true to display full paths
                .enableDryRun();  // Enable dry run mode

        // Recursive listing
         lister.rekursiv();

        // Simulate backup of filtered files to a ZIP archive
        //lister.backup("backup.zip", backupDir);

        // Simulate deletion of filtered files
        //lister.delete();

        // Access the list of filtered files
        List<Path> filteredFiles = lister.getFilteredFiles();
        System.out.println("Filtered files:");
        for (Path file : filteredFiles) {
            System.out.println(file);
        }
    }
}
