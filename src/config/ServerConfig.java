package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig {

    private Properties properties;
    private String configFilePath;

    // Ilay chemin an'ilay fichier de configuration atao eo
    /*
        Ny zavatra soratana ao:
        port = 1234
        path = /home/yvan/Documents/HttpServer/www
     */
    public ServerConfig(String configFilePath) throws IOException {
        this.properties = new Properties();
        this.configFilePath = configFilePath;
        
        try(FileInputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
        }
    }

    public void setProperty(String key, String value) throws IOException {
        properties.setProperty(key, value);
        saveProperties();
    }

    public void saveProperties() throws IOException {
        try (FileOutputStream output = new FileOutputStream(configFilePath)) {
            properties.store(output, "Updated Server Configuration");
        }
    }
    // Manao ohatran'ny map 
    // eg: property.get("path") = "/etc/.../config.conf" 
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    // De base String no averin'ny getProperty dia mila parsena izay tokony ho int
    public int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public int getServerPort() {
        return getIntProperty("port");
    }

    public String getPath() {
        return getProperty("path");
    }

}