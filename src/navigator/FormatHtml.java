package navigator;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class FormatHtml{

    public static  byte[] toBytes(String content) {
        try {
            return content.getBytes("UTF-8"); 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0]; 
        }
    }

    public static String MapToUl(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
            
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("<li>")
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue());
            sb.append("</li>");
        }
        sb.append("</ul>");   
        return sb.toString();
    }

}