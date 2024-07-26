package chatapp;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.sound.sampled.*;
import javax.swing.*;
import com.github.sarxos.webcam.Webcam;

public class videoClass implements Runnable {

    private SSLServerSocket serverSocket;
    private Server serverform;
    private Webcam camera;
    private TargetDataLine targetLine;
    private SourceDataLine sourceLine;
    private volatile boolean isMuted; // Use volatile to ensure visibility across threads
    private volatile boolean running; // To control the running state of threads
    private SSLSocket clientSocket;
        static SSLServerSocketFactory sslServerSocketFactory;

    public videoClass(Server serverform) throws IOException {
        this.serverform = serverform;
        this.serverform = serverform;
                    sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(1235);
        serverform.appendMessage("Video Server is running on port 1235. !\n");
    }

    @Override
    public void run() {
        while (true) {
            try {
                serverform.appendMessage("Waiting for a client to connect...\n");
                clientSocket = (SSLSocket) serverSocket.accept();

                serverform.appendMessage("Client has connected to the video server!\n");

                // Create call control frame when a client has connected
                JFrame callControlFrame = new JFrame("Call Controls for Caller");
                callControlFrame.setLayout(new FlowLayout());
                JLabel callerLabel = new JLabel("Caller's Video");
                JLabel calleeLabel = new JLabel("Server's Video");
                JButton muteButton = new JButton("Mute");
                JButton endCallButton = new JButton("End Call");
                JLabel status = new JLabel("Status: Connected to the client");

                callControlFrame.add(callerLabel);
                callControlFrame.add(calleeLabel);
                callControlFrame.add(muteButton);
                callControlFrame.add(endCallButton);
                callControlFrame.add(status);

                callControlFrame.setSize(800, 600);
                callControlFrame.setVisible(true);

                // Initialize webcam for server's video
                camera = Webcam.getDefault();
                camera.open();

                AudioFormat audioFormat = new AudioFormat(8000.0f, 16, 1, true, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
                targetLine = (TargetDataLine) AudioSystem.getLine(info);
                targetLine.open(audioFormat);
                targetLine.start();

                info = new DataLine.Info(SourceDataLine.class, audioFormat);
                sourceLine = (SourceDataLine) AudioSystem.getLine(info);
                sourceLine.open(audioFormat);
                sourceLine.start();

                OutputStream videoOutputStream = clientSocket.getOutputStream();
                InputStream videoInputStream = clientSocket.getInputStream();
                OutputStream audioOutputStream = clientSocket.getOutputStream();
                InputStream audioInputStream = clientSocket.getInputStream();

                running = true; // Set running to true when the call starts

                // Video capture and playback threads
                Thread videoCaptureThread = new Thread(() -> {
                    try {
                        while (running) {
                            BufferedImage image = camera.getImage();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(image, "jpg", baos);
                            baos.flush();
                            byte[] imageBytes = baos.toByteArray();
                            baos.close();

                            videoOutputStream.write(imageBytes);
                            videoOutputStream.flush();

                            ImageIcon callerIcon = new ImageIcon(image);
                            SwingUtilities.invokeLater(() -> callerLabel.setIcon(callerIcon));

                            Thread.sleep(100); // Adjust this delay based on frame rate
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                videoCaptureThread.start();

                Thread videoPlaybackThread = new Thread(() -> {
                    try {
                        while (running) {
                            byte[] buffer = new byte[1024];
                            int bytesRead = videoInputStream.read(buffer);
                            if (bytesRead == -1) {
                                break; // End of stream
                            }
                            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                            BufferedImage image = ImageIO.read(bais);

                            ImageIcon serverIcon = new ImageIcon(image);
                            SwingUtilities.invokeLater(() -> calleeLabel.setIcon(serverIcon));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                videoPlaybackThread.start();

                // Audio capture and playback threads
                Thread audioCaptureThread = new Thread(() -> {
                    byte[] buffer = new byte[1024];
                    while (running) {
                        int bytesRead = targetLine.read(buffer, 0, buffer.length);
                        try {
                            if (!isMuted) {
                                audioOutputStream.write(buffer, 0, bytesRead);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                audioCaptureThread.start();

                Thread audioPlaybackThread = new Thread(() -> {
                    byte[] buffer = new byte[1024];
                    while (running) {
                        try {
                            int bytesRead = audioInputStream.read(buffer);
                            if (bytesRead == -1) {
                                break; // End of stream
                            }
                            sourceLine.write(buffer, 0, bytesRead);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                audioPlaybackThread.start();

                // Mute button action listener
                muteButton.addActionListener(e -> {
                    isMuted = !isMuted;
                    muteButton.setText(isMuted ? "Unmute" : "Mute");
                });

                // End call button action listener
                endCallButton.addActionListener(e -> {
                    running = false; // Stop the threads
                    status.setText("Status: Call ended");
                    try {
                        camera.close();
                        targetLine.stop();
                        targetLine.close();
                        sourceLine.stop();
                        sourceLine.close();
                        clientSocket.close();
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