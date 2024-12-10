package http;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server {
    int port;

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(8080);
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void run() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) { 

            while (true) { 
                // 1 Thread per Client 
                Socket clientSocket = serverSocket.accept();  
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        handleClient(clientSocket);
                    }
                };
                thread.start();
            }

        } catch (Exception e) {
            throw e;
        }
    }

    void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();
        ) {
            
            Request request = new Request(in);
            Response response = new Response(request);
            System.out.println("Request:\n" + request);
            System.out.println("Response:\n" + response);

            // Alefa mitokana ny headers (String)
            out.write(response.getHeadersToString().getBytes("UTF-8"));
            // Alefa mitokana ny body (byte[])
            out.write(response.getBody());
            out.flush();


        } catch (Exception e) {
            e.printStackTrace(); // Raha throwena dia lasa mila catchena na throwena ao @ methode start()
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error by closing clientSocket: " + e.getMessage());
            }
        }
    }
}