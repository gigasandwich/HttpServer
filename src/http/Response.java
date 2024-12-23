    package http;
    import config.ServerConfig;
    import java.io.File;
    import java.io.IOException;
    import java.io.UnsupportedEncodingException;
    import java.net.URLDecoder;
    import java.nio.file.Files;
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
            headers = new HashMap<>();
            httpVersion = "HTTP/1.1";

            htdocs = ServerConfig.getHtdocs(); 
            fileHandler = new FileHandler(htdocs);
            phpExecutor = new PhpExecutor();

            // Mameno ny valeurs ana attributs hafa
            handleRequest(request);
        }

        void handleRequest(Request request) {
            /* 
            * =====================
            * ETAPES TSY MAINTSY ATAO NA GET NA POST
            * =====================
            */

            // Raha tsy misy index dia mankany @ URL
            String url = request.getUrl();
            String urlWithoutQuery = request.getUrl();

            // Raha misy parametre
            if (url.contains("?")) {  
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
            path = fileHandler.decodeUrl(path);
            File file = new File(htdocs, path);
            if (file.exists()) { 
                // Raha dossier
                if (file.isDirectory()) 
                    handleDirectoryRequest(file, url);

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
        
        private void handleDirectoryRequest(File directory, String url) {
            // Amin'izay rehefa mametraka lien @ href dia tsisy olana @ resaka espace 
            url = decodeUrl(url);
    
            File indexFile = fileHandler.findIndexFile(directory);
            if (indexFile != null) {
                // Ilay fichier index no servena fa tsy listena intsony ny fichiers anaty dossier
                if (indexFile.getAbsolutePath().endsWith(".php"))
                    handlePhpFileRequest(indexFile);
                else 
                    handleRegularFileRequest(indexFile);
            }
            else {
                this.setStatus(200);
                this.addHeader("Content-Type", "text/html");    

                // Listena daholo ny elements ao
                String directoryListing = fileHandler.listDirectoryContent(url, directory);
                setBody(directoryListing);
            }
        }


        private String decodeUrl(String url) {
            try {
                return URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return url;
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

            byte[] responseBody = readFileContent(file);
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

            response.append(body);

            return response.toString();
        }
    }