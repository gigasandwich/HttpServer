package aff;

import config.ServerConfig;
import http.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;

public class ServerSwing extends JFrame {
    private Server server;
    private JButton startButton;
    private JButton stopButton;
    private JButton saveButton;
    private JLabel statusLabel;
    private JTextField portField;
    private JTextField pathField;
    private JRadioButton readPhpField;



    public static void main(String[] args) {
        ServerSwing serverSwing = new ServerSwing();
        serverSwing.setVisible(true);
    }

    public ServerSwing() {
     
        server = new Server();

        setTitle("HTTP Server");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        statusLabel = new JLabel("Server Status: Stopped", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Port and Path
        JPanel configPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        configPanel.setBorder(BorderFactory.createTitledBorder("Server Configuration"));

        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField(String.valueOf(ServerConfig.getPort()));

        JLabel pathLabel = new JLabel("HTML Folder Path:");
        pathField = new JTextField(ServerConfig.getHtdocs());

        JLabel readPhpLabel = new JLabel("Read php:");
        readPhpField = new JRadioButton();

        configPanel.add(portLabel); configPanel.add(portField);
        configPanel.add(pathLabel); configPanel.add(pathField);
        configPanel.add(readPhpLabel); configPanel.add(readPhpField);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        saveButton = new JButton("Save Config");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        stopButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(saveButton);

        stopButton.setEnabled(false); // Atao maty par defaut

        // Button Actions
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveConfig();
            }
        });

        // Add components to the frame
        add(statusLabel, BorderLayout.NORTH);
        add(configPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            String path = pathField.getText().trim();

            // Update 
            ServerConfig.setPort(port);
            ServerConfig.setHtdocs(path);

            new Thread(){ // Anaovana thread amin'izay mandeha foana ny fonctionalite hafa
                @Override
                public void run(){
                    try {
                        server.startServer();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.start();

            updateStatus("Server is running...");
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid port number. Please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to start the server: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopServer() {
        server.stopServer();
        updateStatus("Server has been stopped.");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void saveConfig() {
        try {
            String port = portField.getText().trim();
            String path = pathField.getText().trim();
            boolean readPhp = readPhpField.isSelected();
            
            // "yes" na "no" no soratana @ fichier de config
            String readPhpProperty = "";
            if (readPhp)
                readPhpProperty = "yes";
            else 
                readPhpProperty = "no";


            ServerConfig.setProperty("port", port);
            ServerConfig.setProperty("htdocs", path);
            ServerConfig.setProperty("read_php", readPhpProperty);
            ServerConfig.saveProperties();

            JOptionPane.showMessageDialog(this, "Configuration saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save configuration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatus(String status) {
        statusLabel.setText("Server Status: " + status);
    }

}
