package dev.cubie.CubeServerTool.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

// CSVFileHandler for CSV formatting
public class CSVFileHandler extends Handler {
    private final FileWriter writer;

    public CSVFileHandler(Path path) throws IOException {
        this.writer = new FileWriter(path.toFile(), true);  // Append to the file

        // Write CSV header if the file is new
        if (path.toFile().length() == 0) {
            writer.append("Timestamp,Log Level,Message,Class,Method\n");
        }
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }

        try {
            String logEntry = String.format(
                    "%1$tF %1$tT,%2$s,%3$s,%4$s,%5$s%n",
                    record.getMillis(),
                    record.getLevel(),
                    record.getMessage().replaceAll("\\x1b\\[[\\d;]*m", "").replaceAll(",", ""),  // Remove colors and commas
                    record.getSourceClassName(),
                    record.getSourceMethodName()
            );
            writer.append(logEntry);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws SecurityException {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
