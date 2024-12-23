package navigator;

import config.ServerConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FileHandler {

    // Relatif par rapport @ htdocs daholo ny zavatra lalaovina ato
    private String htdocs;

    public FileHandler(String htdocs) {
        this.htdocs = htdocs;
    }

    // Ilay eo @ url an'ny navigateur misy an'itony % zavatra tony (ohatra hoe @ href, rehefa fichier/dossier misy espace) dia mila averina @ laoniny aloha
    public String decodeUrl(String url) {
        String decodedUrl = url;

        decodedUrl = decodedUrl.replaceAll("%20", " ");
        decodedUrl = decodedUrl.replaceAll("%2F", "/");
        decodedUrl = decodedUrl.replaceAll("%3A", ":");
        decodedUrl = decodedUrl.replaceAll("%3F", "?");
        decodedUrl = decodedUrl.replaceAll("%3D", "=");
        decodedUrl = decodedUrl.replaceAll("%26", "&");

        return decodedUrl;
    }

    public String normalizePath(String requestUrl) {
        // Raha depart root dia fafana ny /
        if (requestUrl.startsWith("/")) {
            requestUrl = requestUrl.substring(1); 
        }

        String decodedUrl = this.decodeUrl(requestUrl);  // %20 -> " "

        // Atao absolu par rappoort @ htdocs no sady asorina ny chemin miverimberina
        // Eg: fako/../fako/fichier = > fako/fichier 
        Path resolvedPath = Paths.get(ServerConfig.getHtdocs(), decodedUrl).normalize(); 
        return resolvedPath.toString();
    }

    // Mitady Fichier index.extension
    public File findIndexFile(File directory) {
        // Raha read_php = yes dia index.php tadiavina voalohany
        if (ServerConfig.canReadPhp()) {
            File phpFile = new File(directory, "index.php");
            if (phpFile.exists()) 
                return phpFile;
        }

        File htmlFile = new File(directory, "index.html");
        if (htmlFile.exists()) {
            return htmlFile;
        }

        return null;
    }

    // Miliste ny fichiers anatin'io 
    public String listDirectoryContent(File directory) {
         // raha misy espace dia tsy %20 no mipoitra fa " "
        String url = getUrlFromFile(directory);
        url = decodeUrl(url);

        StringBuilder directoryListing = new StringBuilder();
        directoryListing.append("<html><body>");
        directoryListing.append("<h1>Index of ").append(url).append("</h1>");
        directoryListing.append("<table border=\"1\" style=\"border-collapse: collapse; width: 100%;\">");
        directoryListing.append("<thead><tr>");
        directoryListing.append("<th>Name</th>");
        directoryListing.append("<th>Size</th>");
        directoryListing.append("<th>Last Modified</th>");
        directoryListing.append("<th>Type</th>");
        directoryListing.append("</tr></thead>");
        directoryListing.append("<tbody>");

        // Mamerina any @ dossier parent
        if (directory.getParentFile() != null) {
            String parentUrl = url.endsWith("/") ? "../" : url + "/../";
            directoryListing.append("<tr>");
            directoryListing.append("<td><a href=\"").append(parentUrl).append("\">")
                                    .append("Back to Parent Directory</a></td>");
            directoryListing.append("<td>-</td>");
            directoryListing.append("<td>-</td>");
            directoryListing.append("<td>Directory</td>");
            directoryListing.append("</tr>");
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                String fileName = f.getName();
                String filePath = url.endsWith("/") ? url + fileName : url + "/" + fileName;

                // Details an'ny fichier
                long fileSize = f.length();
                String formattedSize = f.isDirectory() ? "-" : formatFileSize(fileSize);
                String lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(f.lastModified()));
                String fileType = f.isDirectory() ? "Directory" : "File";

                // Manampy ligne
                directoryListing.append("<tr>");
                directoryListing.append("<td><a href=\"").append(filePath).append("\">").append(fileName).append("</a></td>");
                directoryListing.append("<td>").append(formattedSize).append("</td>");
                directoryListing.append("<td>").append(lastModified).append("</td>");
                directoryListing.append("<td>").append(fileType).append("</td>");
                directoryListing.append("</tr>");
            }
        }

        directoryListing.append("</tbody></table>");
        directoryListing.append("</body></html>");
        return directoryListing.toString();
    }

    private String getUrlFromFile(File file) {
        String htdocsPath = ServerConfig.getHtdocs(); 


        String absolutePath = file.getAbsolutePath(); // Chemin absolu an le fichier
        String relativePath = absolutePath.replace(htdocsPath, ""); // / Soloina banga na "/" ilay soratra eo @ chemin absolu 

        // Raha tsisy nininona ilay chemin relatif dia ao @ htdocs izany ilay fichier
        if (relativePath.isEmpty()) {
            relativePath = "/";
        }

        // Make sure the relative path starts with a "/"
        return relativePath.startsWith("/") ? relativePath : "/" + relativePath;
    }




    /**
     * Byte io size io de base, dia aseho sous forme: Bytes, KB, MB, GB ...
     */
    private String formatFileSize(long size) {  
        if (size < 1024) {
            return size + " B";
        }
        double sizeInKB = size / 1024.0;
        if (sizeInKB < 1024) {
            return String.format("%.2f KB", sizeInKB);
        }
        double sizeInMB = sizeInKB / 1024.0;
        if (sizeInMB < 1024) {
            return String.format("%.2f MB", sizeInMB);
        }
        double sizeInGB = sizeInMB / 1024.0;
        if (sizeInGB < 1024) {
            return String.format("%.2f GB", sizeInGB);
        }
        double sizeInTB = sizeInGB / 1024.0;
        return String.format("%.2f TB", sizeInTB);
    }

    /* 
        Fichiers afaka asehon'ny navigateur, sinon asain'ny navigateur telechargena @ alalan'ny 
        Content-Disposition: attachment; filename="example.pdf"
     */
    public boolean canDisplayFile(File file) {
        // Ireo ihany no afaka apoitra, ankotran zay telechargena
        String fileName = file.getName().toLowerCase();
        String[] displayableExtensions = {"html", "txt", "jpg", "png", "gif", "css", "js", "mp3", "mp4", "mkv"};

        for (String extension : displayableExtensions) {
            if (fileName.endsWith("." + extension)) {
                return true;
            }
        }

        return false;
    }

    public byte[] readFileContent(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public String getContentType(File file) {
        // Raha dossier
        if (file.isDirectory()) {
            return "text/html";
        }

        // Raha fichier
        for (String extension : FileHandler.getContentTypes().keySet()) {
            if (file.getName().endsWith("." + extension)) {
                return getContentTypes().get(extension);
            }
        }

        // Raha tsy fantatra
        return "application/octet-stream";
    }

    /* 
     * =====================
     * Section: utilities
     * =====================
     */
    static Map<String, String> getContentTypes() {
        Map<String, String> extension_contentType = new HashMap<>();
        extension_contentType.put("txt", "text/plain");
        extension_contentType.put("html", "text/html");
        extension_contentType.put("php", "text/html");
        extension_contentType.put("htm", "text/html");
        extension_contentType.put("css", "text/css");
        extension_contentType.put("csv", "text/csv");
        extension_contentType.put("xml", "application/xml");
        extension_contentType.put("json", "application/json");
        extension_contentType.put("jpg", "image/jpeg");
        extension_contentType.put("jpeg", "image/jpeg");
        extension_contentType.put("png", "image/png");
        extension_contentType.put("gif", "image/gif");
        extension_contentType.put("bmp", "image/bmp");
        extension_contentType.put("svg", "image/svg+xml");
        extension_contentType.put("webp", "image/webp");
        extension_contentType.put("mp3", "audio/mpeg");
        extension_contentType.put("wav", "audio/wav");
        extension_contentType.put("ogg", "audio/ogg");
        extension_contentType.put("mp4", "video/mp4");
        extension_contentType.put("webm", "video/webm");
        extension_contentType.put("pdf", "application/pdf");
        extension_contentType.put("zip", "application/zip");
        extension_contentType.put("tar", "application/x-tar");
        extension_contentType.put("gzip", "application/gzip");
        extension_contentType.put("exe", "application/octet-stream");
        extension_contentType.put("woff", "font/woff");
        extension_contentType.put("woff2", "font/woff2");
        extension_contentType.put("ttf", "font/ttf");
        extension_contentType.put("otf", "font/otf");
        extension_contentType.put("xls", "application/vnd.ms-excel");
        extension_contentType.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        extension_contentType.put("ppt", "application/vnd.ms-powerpoint");
        extension_contentType.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        extension_contentType.put("doc", "application/msword");
        extension_contentType.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        extension_contentType.put("rar", "application/vnd.rar");
        extension_contentType.put("class", "application/java-class");
        extension_contentType.put("js", "application/javascript");
        extension_contentType.put("mjs", "application/javascript");
        extension_contentType.put("md", "text/markdown");
        extension_contentType.put("yml", "application/x-yaml");
        extension_contentType.put("yaml", "application/x-yaml");
        return extension_contentType;
    }

}
