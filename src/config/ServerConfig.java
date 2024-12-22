package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig {
    // static no lalaovina amin'izay tsy mila mamorona objet de type ServerConfig isaky ny classe mampiasa an'azy
    static Properties properties;
    static String configFilePath;

    // Atao anaty bloc static amin'izay afaka initialisena sy loadena ilay properties 
    static {
        properties = new Properties();
        configFilePath = "config/httpserver.conf";

        try(FileInputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /* -------------------------- SETTERS -------------------------- */

    // Ilay zavatra tokony atao ao 
    /**
        * Ny zavatra soratana ao:
        * port = 1234
        * path = /home/yvan/Documents/HttpServer/www
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
        try (FileOutputStream output = new FileOutputStream(configFilePath)) {
            properties.store(output, "Updated Server Configuration");
        } catch (IOException e) {
            throw e; // alefa any @ Swing
        }
    }

    public static void setPort(int port) throws IOException {
        setProperty("port", String.valueOf(port));
    }

    public static void setHtdocs(String path) throws IOException {
        setProperty("htdocs", path);
    }

    public static void setCanReadPhp(boolean value) throws IOException {
        setProperty("read_php", String.valueOf(value));
    }

    /* -------------------------- GETTERS -------------------------- */

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
        return getProperty("read_php").equals("yes"); // .equals fa tsy == satria pointeur ny String
    }

}