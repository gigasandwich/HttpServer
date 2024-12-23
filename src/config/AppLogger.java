package config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppLogger {
    private static final String LOG_FILE = "/etc/httpserver/server.log";
    private static BufferedWriter writer;

    static {
        try {
            // Soratana en mode append
            writer = new BufferedWriter(new FileWriter(LOG_FILE, true));
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    // Fanoratana log
    private static void writeLog(String level, String message) {
        try {
            if (writer != null) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);
                writer.write(logMessage);
                writer.newLine();
                writer.flush(); 
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    // INFO level no loggena
    public static void logInfo(String message) {
        writeLog("INFO", message);
    }

    // WARNING level
    public static void logWarning(String message) {
        writeLog("WARNING", message);
    }

    // Error level
    public static void logError(String message, Throwable throwable) {
        writeLog("ERROR", message + " - " + throwable.getMessage());
        throwable.printStackTrace();  // Alefa any @ terminal ihany amzay miavaka tsara
    }

    // Hidiana ny writer rehefa vita ny atao
    public static void close() {
        try {
            if (writer != null) 
                writer.close();
        } catch (IOException e) {
            System.err.println("Failed to close log file: " + e.getMessage());
        }
    }
}
