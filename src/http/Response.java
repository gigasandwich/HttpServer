package http;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import navigator.FormatHtml;

public class Response {
    
    int statusCode;
    String message;
    String httpVersion;
    Map<String, String> headers;
    byte[] body;

    String webRoot;

    public Response(Request request) {
        // Initialisations attributs
        body = new byte[0];
        headers = new HashMap<>();
        httpVersion = "HTTP/1.1";

        // Chemin absolu mankeo @ dossier an'ny compilation.sh
        String currentDirectory = java.nio.file.Paths.get("").toAbsolutePath().toString();
        webRoot = currentDirectory + "/www";

        // Mameno ny valeurs ana attributs hafa
        handleRequest(request);
    }

    void handleRequest(Request request) {
        // Etape tsy maintsy atao

        // Raha tsy misy index dia mankany @ URL
        String url = request.getUrl();
        String urlWithoutQuery = request.getUrl();

        if (url.contains("?")) {// Raha misy parametre 
            String[] parts = url.split("\\?", 2);
            urlWithoutQuery = parts[0]; // Ilay tena url alaina fa tsy ilay url misy query
            Map<String, String> queryParams = parseQueryParams(parts[1]); // Tsy rarahiana ito satria tsy miasa pour le moment
        }
        
        // Raha ip fotsiny no nosoratana de direct mankany @ index zay
        String path;
        if (urlWithoutQuery.equals("/")) 
            path = "/index.html";
        else 
            path = urlWithoutQuery;

        // Amoronana objet de type File ilay chemin notapena teo @ url
        File file = new File(webRoot, path);
        if (file.exists()) { // Raha dossier

            if (file.isDirectory()) {
                this.setStatus(200);
                this.addHeader("Content-Type", "text/html");

                // Listena daholo ny elements ao
                String directoryListing = listDirectoryContent(url, file);
                setBody(directoryListing);

            } else if (file.getName().endsWith(".php")) { // Raha fichier php
                try {
                    this.setStatus(200);
                    this.addHeader("Content-Type", "text/html");
                    byte[] output = executePhpFile(file);
                    setBody(output);
                } catch (IOException e) {
                    setErrorResponse(500); // Internal Server Error
                }
            } else { // Ankoatran'izay
                this.setStatus(200);
                this.addHeader("Content-Type", getContentType(file));
                byte[] responseBody = readFileContent(file);
                setBody(responseBody);
            }

        } else {
            setErrorResponse(404); // File not found
        }

        // Miankina amin'ny methode amin'izay ny zavatra atao eto
        String method = request.getMethod();

        switch (method.toLowerCase()) {
            case "get":
                handleGet(request);
                break;
            case "post":
                handlePost(request);
                break;
            default:
                setErrorResponse(405); // Method not allowed
        }
    } 


    private byte[] executePhpFile(File phpFile) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("php", phpFile.getAbsolutePath());
        processBuilder.redirectErrorStream(true); // Merge standard error into standard output

        // Start the process
        Process process = processBuilder.start();

        // Capture the output
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        ) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString().getBytes("UTF-8");
        } finally {
            process.destroy();
        }
    }
    
    // Get method
    void handleGet(Request request) {

        // index.html?name=Mitia&age=12
        String url = request.getUrl();

        // Alaina ny query parameters
        Map<String, String> queryParams = new HashMap<>();

        if (url.contains("?")) {
            String[] parts = url.split("\\?", 2);
            // parts[0]: index.html
            // parts[1]: name=Fako&age=17&Country=Madagascar
            url = parts[0]; 
            queryParams = parseQueryParams(parts[1]); // Eto indray ny url no tsy rarahiana fa ny query ihany satria ireo sisa no affichena
        }

        // Raha misy parametres dia afficheo aloha ao @le page ihany 
        if (!queryParams.isEmpty()) {
            StringBuilder sbQueryParam = new StringBuilder();
            sbQueryParam.append("\n");
            sbQueryParam.append("<h2>Query Parameters:</h2>");
            sbQueryParam.append(FormatHtml.MapToUl(queryParams));
            sbQueryParam.append("\n");
            appendBody(sbQueryParam.toString());
        }
    }

    // Post method  
    void handlePost(Request request) {
        try {
            // Verification hoe misy body ve sa tsia
            String requestBody = request.getBody();
            if (requestBody == null || requestBody.isEmpty()) {
                setErrorResponse(400); // Bad Request
                return;
            }

            // Alaina ilay map avy @ body: meme structure amin'ny get ihany (age=17&nom=Fako)
            Map<String, String> bodyMap = request.parseFormBody();

            this.setStatus(200);
            this.addHeader("Content-Type", "text/html"); // na text/plain ????

            // Affichena sous format html ny mao
            StringBuilder sbPost = new StringBuilder();
            sbPost.append("\n");
            sbPost.append("<h2>Post datas:</h2>");
            sbPost.append(FormatHtml.MapToUl(bodyMap));
            sbPost.append("\n");
            appendBody(sbPost.toString());
        } catch (Exception e) {
            setErrorResponse(500); // Internal Server Error
        }
    }

    private Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        // Sarahana ny &
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] key_value = pair.split("=", 2);
            if (key_value.length == 2) {
                // age, 17
                queryParams.put(key_value[0], key_value[1]);
            }
        }
        return queryParams;
    }

    private byte[] readFileContent(File file) {
        try {
            return Files.readAllBytes(file.toPath()); 
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    // Miliste ny fichiers anatin'io 
    private String listDirectoryContent(String url, File file) {
        StringBuilder directoryListing = new StringBuilder();
        directoryListing.append("<html><body>");
        directoryListing.append("<h1>Index of ").append(url).append("</h1>");
        directoryListing.append("<ul>");

        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                String fileName = f.getName();
                String filePath = ""; // Apetraka amle href an'ilay balise a ny filePath
                if( url.endsWith("/") ) 
                    filePath = url + fileName;
                else 
                    filePath = url + "/" + fileName;
                
                directoryListing.append("<li><a href=\"").append(filePath).append("\">")
                        .append(fileName)
                        .append("</a></li>");
            }
        }

        directoryListing.append("</ul>");
        directoryListing.append("</body></html>");
        return directoryListing.toString();
    }

    /* -------------------------- SETTERS -------------------------- */
    public void setBody(byte[] content) {
        addHeader("Content-Length", String.valueOf(content.length));
        this.body = content;
    }

    public void setBody(String content) {
        setBody(FormatHtml.toBytes(content));
    }

    public void appendBody(byte[] content) {
        byte[] combined = new byte[body.length + content.length];
        System.arraycopy(body, 0, combined, 0, body.length);
        System.arraycopy(content, 0, combined, body.length, content.length);
        setBody(combined);
    }

    public void appendBody(String content) {
        appendBody(FormatHtml.toBytes(content));
    }

    public void setStatus(int statusCode) {
        this.statusCode = statusCode;
        // Alaina ny message mifanaraka amle map
        this.message = getStatusMessages().get(statusCode);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setErrorResponse(int statusCode) {
        setStatus(statusCode);
        addHeader("Content-Type", "text/html");
        setBody("<html><body><h1>Error " + statusCode + ": " + getStatusMessages().get(statusCode) + "</h1></body></html>");
    }
 
    /* -------------------------- GETTERS -------------------------- */
    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
    
    private String getContentType(File file) {
        // Raha dossier
        if (file.isDirectory()) {
            return "text/html"; 
        }

        // Raha fichier
        for (String extension : getContentTypes().keySet()) {
            if (file.getName().endsWith("." + extension)) {
                return getContentTypes().get(extension);
            }
        }

        // Raha tsy fantatra
        return "application/octet-stream"; 
    }


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

    static Map<Integer, String> getStatusMessages() {
        Map<Integer, String> status_message = new HashMap<>();
        status_message.put(200, "OK");
        status_message.put(201, "Created");
        status_message.put(202, "Accepted");
        status_message.put(204, "No Content");
        status_message.put(301, "Moved Permanently");
        status_message.put(302, "Found");
        status_message.put(304, "Not Modified");
        status_message.put(400, "Bad Request");
        status_message.put(401, "Unauthorized");
        status_message.put(403, "Forbidden");
        status_message.put(404, "Not Found");
        status_message.put(405, "Method Not Allowed");
        status_message.put(406, "Not Acceptable");
        status_message.put(407, "Proxy Authentication Required");
        status_message.put(408, "Request Timeout");
        status_message.put(409, "Conflict");
        status_message.put(410, "Gone");
        status_message.put(415, "Unsupported Media Type");
        status_message.put(429, "Too Many Requests");
        status_message.put(500, "Internal Server Error");
        status_message.put(501, "Not Implemented");
        status_message.put(502, "Bad Gateway");
        status_message.put(503, "Service Unavailable");
        status_message.put(504, "Gateway Timeout");
        return status_message;
    }

    // Ilay map an'ny headers avadika String version hitan'ny client HTTP
    // eg: Content-Length: 31043
    public String getHeadersToString() {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(httpVersion).append(" ").append(statusCode).append(" ").append(message).append("\r\n");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            headerBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        headerBuilder.append("\r\n"); // End of headers
        return headerBuilder.toString();
    }

    /* -------------------------- DISPLAY -------------------------- */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("statusCode: ").append(statusCode).append("\n");
        sb.append("message: ").append(message).append("\n");
        sb.append("headers: ").append(headers).append("\n");
        sb.append("body: ").append(body).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    // Ilay affichena ao @ navigateur 
    public String stringValue() {
        StringBuilder response = new StringBuilder();

        // Response line
        response.append(httpVersion).append(" ").append(statusCode).append(" ").append(message).append("\r\n");

        // Headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey()).append(": ").append(header.getValue());
            response.append("\r\n");
        }

        // Asina espace entre header sy body
        response.append("\r\n");

        // if (!body == null)
        response.append(body);

        return response.toString();
    }
}