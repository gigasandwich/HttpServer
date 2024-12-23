package http;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class Request {
    String method; 
    String url;
    String httpVersion;
    Map<String, String> headers;
    String body; 

    public Request(BufferedReader in) throws IOException {
        // Initialisations attributs
        method = "";
        url = "";
        headers = new HashMap<>();
        body = "";

        // Ligne voalohany GET /index.html HTTP/1.1      
        String line = in.readLine();

        String[] requested = line.split(" ");
        this.method = requested[0];
        this.url = requested[1];
        this.httpVersion = requested[2];

        // Headers (nb: misy saut a la ligne alohan'ny body)
        String headerLine;
        while ((headerLine = in.readLine()) !=null && !headerLine.isEmpty()) { // Tsy any amin'ny farany sy tsy banga ilay line
            String[] headerParts = headerLine.split(": ", 2); // Partie avant et apres deux points
            if (headerParts.length == 2) {
                this.headers.put(headerParts[0], headerParts[1]);
            }
        }

        // Raha misy body (post, put, patch) dia parsena char[] ilay izy amzay voaray tsara ny contenu any
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            char[] bodyChars = new char[contentLength]; // Izay tokony vakiana ihany no vakiana (selon ny Content-Length)
            in.read(bodyChars); 
            this.body = new String(bodyChars);
        }
    }

    // Raha misy body dia antsoina ao @ class Response:
    public Map<String, String> parseFormBody() {
        Map<String, String> formData = new HashMap<>();
        String[] pairs = this.body.split("&");          
        for (String pair : pairs) {
            String[] key_value = pair.split("=", 2);
            if (key_value.length == 2) {
                formData.put(key_value[0], key_value[1]);
            }
        }
        return formData;
    }

    /* 
     * =====================
     * Section: Display
     * =====================
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("method: ").append(method).append("\n\n");
        sb.append("url: ").append(url).append("\n\n");
        sb.append("headers: ").append(headers).append("\n\n");
        sb.append("body: ").append(body).append("\n\n");
        return sb.toString();
    }

    /* 
     * =====================
     * Section: Getters
     * =====================
     */
    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getHttpVersion() {
        return httpVersion;
    }
}