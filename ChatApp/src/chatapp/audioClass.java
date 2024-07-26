package chatapp;
import java.awt.FlowLayout;
import java.io.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.*;
import javax.sound.sampled.*;
import javax.swing.*;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author abdul
 */
public class audioClass implements Runnable {

    ServerSocket server;
    Socket client;
    
    TargetDataLine targetLine;
    DataLine.Info captureInfo, playbackInfo;
    SourceDataLine sourceLine;
    OutputStream outputStream;
    InputStream inputStream;
    boolean isMuted = false;
//    StringTokenizer st;
    //private static Webcam webcam;
    private static boolean running;
    Server serverform;

    public audioClass(Server serverform) {
        this.serverform = serverform;
        try {
            this.serverform = serverform;
            server = new ServerSocket(5000);
            serverform.appendMessage("Audio Server is running on port 5000. !\n");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                serverform.appendMessage("Waiting for a client to connect...\n");
                client = server.accept();
                serverform.appendMessage("Client has connected to the audio server!\n");

                // Create call control frame, frame is created when a client has connected
                JFrame callControlFrame = new JFrame("Call Controls for server");
                JButton muteButton = new JButton("Mute");
                JButton endCallButton = new JButton("End Call");
                JLabel status = new JLabel("Status");
                callControlFrame.setLayout(new FlowLayout());
                callControlFrame.add(muteButton);
                callControlFrame.add(endCallButton);
                callControlFrame.add(status);
                callControlFrame.setSize(200, 100);
                callControlFrame.setVisible(true);

                status.setText("Connected to the client");

                AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
                targetLine = AudioSystem.getTargetDataLine(format);
                targetLine.open(format);
                targetLine.start();

                sourceLine = AudioSystem.getSourceDataLine(format);
                sourceLine.open(format);
                sourceLine.start();

                OutputStream outputStream = client.getOutputStream();
                InputStream inputStream = client.getInputStream();

                running = true; // Set running to true when the call starts

                Thread captureThread = new Thread(() -> {
                    byte[] buffer = new byte[1024];
                    while (running) {
                        int bytesRead = targetLine.read(buffer, 0, buffer.length);
                        try {
                            if (!isMuted) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        } catch (IOException e) {
                            if (!running) {
                                break;
                            }
                            e.printStackTrace();
                        }
                    }
                });
                captureThread.start();

                Thread playbackThread = new Thread(() -> {
                    byte[] buffer = new byte[1024];
                    while (running) {
                        try {
                            int bytesRead = inputStream.read(buffer);
                            if (bytesRead == -1) {
                                break; // End of stream
                            }
                            sourceLine.write(buffer, 0, bytesRead);
                        } catch (IOException e) {
                            if (!running) {
                                break;
                            }
                            e.printStackTrace();
                        }
                    }
                });
                playbackThread.start();

                // Mute button action listener
                muteButton.addActionListener(e -> {
                    isMuted = !isMuted;
                    muteButton.setText(isMuted ? "Unmute" : "Mute");
                });

                // End call button action listener
                endCallButton.addActionListener(e -> {
                    running = false; // Stop the threads
                    status.setText("Call ended");
                    try {
                        targetLine.stop();
                        targetLine.close();
                        sourceLine.stop();
                        sourceLine.close();
                        client.close();
                        callControlFrame.dispose();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

            } catch (LineUnavailableException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
