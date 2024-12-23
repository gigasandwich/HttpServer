package http;
import config.ServerConfig;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import navigator.FileHandler;
import navigator.FormatHtml;

public class Response {
    
    int statusCode;
    String message;
    String httpVersion;
    Map<String, String> headers;
    byte[] body;

    String htdocs;

    FileHandler fileHandler;
    PhpExecutor phpExecutor;

    public Response(Request request) {
        // Initialisations attributs
        body = new byte[0];
        // LinkedHashMap manaraka ordre fa tsy otran'ny HashMap tsotra, sady na manova value ana header ary dia tsy miova fona ny ordrem de depart
        headers = new LinkedHashMap<>(); 

        httpVersion = "HTTP/1.1";

        htdocs = ServerConfig.getHtdocs(); 
        fileHandler = new FileHandler(htdocs);
        phpExecutor = new PhpExecutor();

        // initialiseHeaders();
        // Mameno ny valeurs ana attributs hafa
        handleRequest(request);
    }

    private void initialiseHeaders() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        /* 
        * =====================
        * Headers oblige apetaka
        * =====================
        */
        addHeader("Date", sdf.format(new Date())); // Date nandefasana an'ilay reponse
        addHeader("Connection", "keep-alive");  // @ izay ilay serveur afaka mi-handle requete maromaro amina connexion tokana (manena latence satria tsy mila mamorona connexion isaky ny mihetsika)
        addHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // Tsy mampisy cache
        addHeader("Content-Type", "text/html; charset=UTF-8"); // Content-Type par defaut, miova io any aoriana any 
        addHeader("Content-Language", "en-US"); // Language of the content
        addHeader("Strict-Transport-Security", "max-age=31536000"); // Asaina mampiasa HTTPS ilay navigateur durant 31536000 secondes 
        // addHeader("Content-Security-Policy", "default-src 'self'"); // Ressource (scripts, images, styles) ao @ domaine de base ihany no afaka miasa fa tsy afaka mi-load avy any ivelany

        /**
         * Ilay MIME type de base ihany hi-interprettena an le fichier. 
         * Eg: fichier html lazain le serveur fa "application/javascript" ny MIME type any nefa normalement io "text/html".
         * Dia tsy raha misy an'io header "X-Content-Type-Options: nosniff" io dia tazominy amle application/javascript ilay izy 
        */
        addHeader("X-Content-Type-Options", "nosniff"); 

        addHeader("X-Frame-Options", "SAMEORIGIN"); // Site hafa tsy afaka manao balise iframe an'ito site ito
        addHeader("Server", "gasy-ttp server"); // Identification an'ilay serveur fotsiny


        /* 
        * =====================
        * Headers optionels
        * =====================
        */
        // addHeader("Content-Encoding", "gzip"); // Vao decommentena dia tsy mipoitra ilay page
        addHeader("Content-Length", String.valueOf(body.length)); // Hamantaran'ny client (navigateur) mialoha ny taille an'ny zavatra afficheny
        addHeader("Expires", "Wen, 15 Nov 2025 12:00:00 GMT"); // Miteny hoe ovina no tokony tsy cachena intsony ilay reponse
        addHeader("Last-Modified", sdf.format(new Date())); // Genere isaky ny misy reponse lasa 
        addHeader("X-Powered-By", "Java HTTP Server"); // Technology na framework mampandefa an'ilay web server.
    }
    

    void handleRequest(Request request) {
        /* 
        * =====================
        * ETAPES TSY MAINTSY ATAO NA GET NA POST
        * =====================
        */

        String url = request.getUrl();

        // Raha misy parametre
        if (url.contains("?")) {  
            String[] parts = url.split("\\?", 2);
            url = parts[0]; // Ilay tena url alaina fa tsy ilay url misy query
            Map<String, String> queryParams = parseQueryParams(parts[1]); // Tsy rarahiana ito satria tsy miasa pour le moment
        }
        
        
        // Amoronana objet de type File ilay chemin notapena teo @ url
        String normalizedPath = fileHandler.normalizePath(url); // Atao par rapport @ htdocs fona (avadika chemin absolu)
        File file = new File(normalizedPath);

        if (file.exists()) { 
            // Raha dossier
            if (file.isDirectory()) 
                handleDirectoryRequest(file);

            // Raha fichier php, no sady eken'ny serveur hoe afaka mamaky php
            else if (file.getName().endsWith(".php") && ServerConfig.canReadPhp()) 
                handlePhpFileRequest(file);

            // Ankoatran'izay
            else  
                handleRegularFileRequest(file);

        } else {
            setErrorResponse(404); // File not found
        }

        /* 
        * =====================
        * ETO IZAY VAO JERENA PAR RAPPORT @ METHODE ILAY REQUETE
        * =====================
        */
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
    
    
    private void handleDirectoryRequest(File directory) {
        File indexFile = fileHandler.findIndexFile(directory);
        if (indexFile != null) {
            // Ilay fichier index no servena fa tsy listena intsony ny fichiers anaty dossier
            if (indexFile.getAbsolutePath().endsWith(".php"))
                handlePhpFileRequest(indexFile); // Interpreteur php no miasa
            else 
                handleRegularFileRequest(indexFile);
        }
        else {
            this.setStatus(200);
            this.addHeader("Content-Type", "text/html");    

            // Listena daholo ny elements ao
            String directoryListing = fileHandler.listDirectoryContent(directory);
            setBody(directoryListing);
        }
    }
    
    private void handlePhpFileRequest(File phpFile) {
        try {
            this.setStatus(200);
            this.addHeader("Content-Type", "text/html");
            byte[] output = phpExecutor.executePhpFile(phpFile);
            setBody(output);
        } catch (IOException e) {
            setErrorResponse(500);  // Internal Server Error
        }
    }

    private void handleRegularFileRequest(File file) {
        this.setStatus(200);
        this.addHeader("Content-Type", fileHandler.getContentType(file));

        // Raha fichier php na tsy anaty liste no anaovana GET dia asaina telechargena
        if (file.getName().endsWith(".php") || ! fileHandler.canDisplayFile(file)) 
            this.addHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

        byte[] responseBody = fileHandler.readFileContent(file);
        setBody(responseBody);
    }

    // Get method
    void handleGet(Request request) {

        // index.html?name=Mitia&age=12
        String url = request.getUrl();

        // Alaina ny query parameters
        Map<String, String> queryParams = new HashMap<>();

        if (url.contains("?")) {
            String[] parts = url.split("\\?", 2);
            /*
            * parts[0]: index.html
            * parts[1]: name=Fako&age=17&Country=Madagascar
            */
            url = parts[0]; 

            // Eto indray ny url no tsy rarahiana fa ny query ihany satria ireo sisa no affichena
            queryParams = parseQueryParams(parts[1]); 
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


    /* 
    * =====================
    * Section: Setters
    * =====================
    */
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


    
    /* 
    * =====================
    * Section: Getters
    * =====================
    */
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
    public String getHeadersToString() {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(httpVersion).append(" ").append(statusCode).append(" ").append(message).append("\r\n");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            // eg: Content-Length: 31043
            headerBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        headerBuilder.append("\r\n"); // 2 sauts a la ligne
        return headerBuilder.toString();
    }

     
    /* 
    * =====================
    * Section: Display
    * =====================
    */
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

        response.append(new String(body));

        return response.toString();
    }
}