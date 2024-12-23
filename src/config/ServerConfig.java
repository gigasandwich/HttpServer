package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig {
    // static no lalaovina amin'izay tsy mila mamorona objet de type ServerConfig isaky ny classe mampiasa an'azy
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE_PATH = "config/httpserver.conf";
    private static final String PORT_KEY = "port";
    private static final String HTDOCS_KEY = "htdocs";
    private static final String READ_PHP_KEY = "read_php";

    // Atao anaty bloc static amin'izay afaka initialisena sy loadena ilay properties vao manomboka (anaty RAM)
    static {
        try(FileInputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(input);
        } catch (Exception e) {
            System.err.println("Failed to load configuration file: " + CONFIG_FILE_PATH);
            e.printStackTrace();
        }
    }
 
    /* 
     * =====================
     * Section: Setters
     * =====================
     */

    // Ilay zavatra tokony atao ao 
    /**
        * Ny zavatra soratana ao:
        * port = 1234
        * path = /home/yvan/Documents/HttpServer/htdocs
        * read_php = yes
    */

    public static void setProperty(String key, String value) throws IOException {
        properties.setProperty(key, value);
        saveProperties();
    }

    public static void saveProperties() throws IOException {
        /**
            * Manoratra an'ito ao @ fichier de configuration
            * #Updated Server Configuration
            * #Sun Dec 22 13:58:09 EAT 2024
         */
        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE_PATH)) {
            properties.store(output, "Updated Server Configuration");
        } catch (IOException e) {
            throw e; // alefa any @ Swing
        }
    }

    public static void setPort(int port) throws IOException {
        setProperty(PORT_KEY, String.valueOf(port));
    }

    public static void setHtdocs(String path) throws IOException {
        setProperty(HTDOCS_KEY, path);
    }

    public static void setCanReadPhp(boolean value) throws IOException {
        setProperty(READ_PHP_KEY, String.valueOf(value));
    }

    /* 
     * =====================
     * Section: Getters
     * =====================
     */

    // Manao ohatran'ny map 
    // eg: property.get("path") = "/etc/.../config.conf" 
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    // De base String no averin'ny getProperty dia mila parsena izay tokony ho int
    public static int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public static int getPort() {
        return getIntProperty("port");
    }

    public static String getHtdocs() {
        return getProperty("htdocs");
    }

    public static boolean canReadPhp() {
        // return getProperty("read_php").equals("yes"); // .equals fa tsy == satria pointeur ny String
        String value = getProperty(READ_PHP_KEY);
        return value != null && (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"));
    }

}