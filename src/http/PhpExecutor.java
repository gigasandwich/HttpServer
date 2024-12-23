package http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class PhpExecutor {

    public byte[] executePhpFile(File phpFile) throws IOException {
        // ProccessBuilder (commande, fanampiny)
        ProcessBuilder processBuilder = new ProcessBuilder("php", phpFile.getAbsolutePath());
        processBuilder.redirectErrorStream(true); // Standard error + standard output miaraka affiche

        // Ohatran'ny Manindry "enter button" ao @ terminal
        Process process = processBuilder.start();   

        // Alaina ny output
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

}
