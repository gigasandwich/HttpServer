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
    int port;
    static String webRoot;
    boolean isServerRunning = false;
    ServerSocket serverSocket;
    List<Socket> clientSockets; 

    public Server() {
        try {
            ServerConfig config = new ServerConfig("config/httpserver.conf");
            this.port = config.getServerPort();
            Server.webRoot = config.getPath();
            this.clientSockets = new ArrayList<>();

        } catch (Exception e) {
            System.out.println("Configuration error: " + e.getMessage());
        }
    }

    // Alefa via swing
    // public static void main(String[] args) {
    //     Server server = new Server();
    //     server.startServer();
    // }

    public void startServer() {
        try {
            if (!isServerRunning) {
                isServerRunning = true;
                run(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                        System.err.println("Error closing client socket: " + e.getMessage());
                    }
                }

                 try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Server has been stopped.");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error closing sockets: " + e.getMessage());
            }
        }
    }



    public void run() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) { 

            while (isServerRunning) { 
                // 1 Thread per Client 
                Socket clientSocket = serverSocket.accept();  
                clientSockets.add(clientSocket);

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
            System.out.println("Response:\n" + response.stringValue());

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

    public void setPort(int port) {
        this.port = port;
    }

    // Getters
    public static void setWebRoot(String webRoot) {
        Server.webRoot = webRoot;
    }    

    public int getPort() {
        return port;
    }

    // Getters
    public static String getWebRoot() {
        return webRoot;
    }
  
}