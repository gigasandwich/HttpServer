package http;
import config.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    
    boolean isServerRunning = false;
    ServerSocket serverSocket;
    List<Socket> clientSockets; 

    public Server() {
        try {
            this.clientSockets = new ArrayList<>();
            AppLogger.logInfo("Server initialized successfully."); 
        } catch (Exception e) {
            AppLogger.logError("Configuration error: " + e.getMessage(), e);
        }
    }

    // Alefa via swing dia tsy mila main intsony

    /*
    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
    */

    public void startServer() {
        try {
            if (!isServerRunning) {
                isServerRunning = true;
                AppLogger.logInfo("Server is starting...");
                run(); 
            }
        } catch (Exception e) {
            AppLogger.logError("Error while starting the server.", e);
        }
    }       

    public void stopServer() {
        if (isServerRunning) {
            try {
                isServerRunning = false;

                // Pihana daholo ny clientSocket rehetra
                for (Socket clientSocket : clientSockets) {
                    try {
                        if (clientSocket != null && !clientSocket.isClosed()) {
                            clientSocket.close(); 
                        }
                    } catch (IOException e) {
                        AppLogger.logWarning("Error closing client socket: " + e.getMessage());
                    }
                }   

                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }   
                } catch (IOException e) {
                    AppLogger.logError("Error closing server socket.", e);
                }

                AppLogger.logInfo("Server has been stopped.");
            } catch (Exception e) {
                AppLogger.logError("Error during server shutdown.", e);
            }
        }
    }


    public void run() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(ServerConfig.getPort())) { 

            while (isServerRunning) { 
                // 1 Thread per Client 
                Socket clientSocket = serverSocket.accept();  
                clientSockets.add(clientSocket);

                AppLogger.logInfo("New client connected: " + clientSocket.getLocalAddress().getHostAddress());

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        handleClient(clientSocket);
                    }
                };
                thread.start();
            }

        } catch (Exception e) {
            throw e; // startServer no aleo micatch an'ilay exception
        }
    }
    

    void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();
        ) {
            
            Request request = new Request(in);
            Response response = new Response(request);

            AppLogger.logInfo("Request received from client: " + clientSocket.getLocalAddress().getHostAddress());
            AppLogger.logInfo("Request Details:\n" + request);
            AppLogger.logInfo("Response Details:\n" + response.stringValue());

            // Alefa mitokana ny headers (String)
            out.write(response.getHeadersToString().getBytes("UTF-8"));

            // Mihidina a la ligne 2 fois vita header izay vao alefa ny body
            out.write(response.getBody());
            out.flush();


        } catch (Exception e) {
            // Raha throwena dia lasa mila catchena na throwena ao @ methode start() fa aleo tonga dia catchena eto ilay Exception
            AppLogger.logError("Error handling client: " + clientSocket.getLocalAddress().getHostAddress(), e);
        } finally {
            try {
                clientSocket.close();
                AppLogger.logInfo("Connection closed with client: " + clientSocket.getLocalAddress().getHostAddress());
            } catch (IOException e) {
                AppLogger.logWarning("Error closing client socket: " + e.getMessage());
            }
        }
    }
  
}