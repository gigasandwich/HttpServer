package aff;

import config.ServerConfig;
import http.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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

    private JButton browseButton;

    public static void main(String[] args) {
        ServerSwing serverSwing = new ServerSwing();
        serverSwing.setVisible(true);
    }

    public ServerSwing() {
        /**
         * Mizara partie 4 ilay JFRAME: statusLabel: label mampiseho ny status
         * an'ny server configPanel: misy ny port, htdocs sy read_php: Jlabel 3
         * manana JTextfield na JRadio browsePanel: Cote droite misy an le
         * bouton ahafahana mametraka an le option manova chemin an'ny htdocs
         * buttonPanel: misy ny boutons 3: start, stop, save
         */

        server = new Server();

        setTitle("HTTP Server");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        statusLabel = new JLabel("Server Status: Stopped", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Port, Htdocs sy read_php
        JPanel configPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        configPanel.setBorder(BorderFactory.createTitledBorder("Server Configuration"));

        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField(String.valueOf(ServerConfig.getPort()));

        JLabel pathLabel = new JLabel("HTDOCS path:");
        pathField = new JTextField(ServerConfig.getHtdocs());

        JLabel readPhpLabel = new JLabel("Read php:");
        readPhpField = new JRadioButton();

        configPanel.add(portLabel);
        configPanel.add(portField);
        configPanel.add(pathLabel);
        configPanel.add(pathField);
        configPanel.add(readPhpLabel);
        configPanel.add(readPhpField);

        // Cote droite, ivelan le panel misy grid 
        JPanel browsePanel = new JPanel();
        browseButton = new JButton("Change HTDOCS path");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileExplorer();
            }
        });
        browsePanel.add(browseButton);

        // ButtonPanel
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

        stopButton.setEnabled(false); // Stop button disabled par defaut

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

        add(statusLabel, BorderLayout.NORTH);
        add(configPanel, BorderLayout.CENTER);
        add(browsePanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void startServer() {
        try {
            // Asina thread iray amzay afaka mandeha en parallele ny operations hafa

            new Thread() {
                @Override
                public void run() {
                    try {
                        server.startServer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            updateStatus("Server is running...");
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to start the server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

            // Port valide
            int portNumber = Integer.parseInt(port);
            if (portNumber <= 0 || portNumber > 65535)
                throw new NumberFormatException("Port must be between 1 and 65535.");
 
            // Chemin valide
            File htdocsPath = new File(path);
            if (!htdocsPath.exists() || !htdocsPath.isDirectory()) 
                 throw new IllegalArgumentException("The path does not exist or is not a directory.");
        

            // Sady miset no misave
            ServerConfig.setPort(portNumber);
            ServerConfig.setHtdocs(path);
            ServerConfig.setCanReadPhp(readPhp);
            ServerConfig.saveProperties();

            JOptionPane.showMessageDialog(this, "Configuration saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number. Please enter a valid integer between 1 and 65535.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid file path: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save configuration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatus(String status) {
        statusLabel.setText("Server Status: " + status);
    }

    // Manokatra explorateur de fichier
    private void openFileExplorer() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select HTML Folder");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Dossier ihany no afaka sokafana

        // Mampiseo file chooser and sy ny selection natao
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // Ovaina ilay chemin eo @ JTextField
            String selectedPath = fileChooser.getSelectedFile().getAbsolutePath();
            pathField.setText(selectedPath);
        }
    }
}
