package chatapp;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.github.sarxos.webcam.Webcam;

public class ClientThread implements Runnable {

    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    Client clientFrame;
    StringTokenizer token;
    Login loginform;
    register registerform;
    MessageAlert alert;
    UpdateDetails2 updateDetails2Frame;
    UpdateDetails1 updateDetails1Frame;
    TargetDataLine targetLine;
    DataLine.Info captureInfo, playbackInfo;
    SourceDataLine sourceLine;
    OutputStream outputStream;
    InputStream inputStream;
    boolean isMuted = false;
//    StringTokenizer st;

    private static Webcam webcam;
    private static boolean running;

    public ClientThread(Socket socket, Client ClientFrame, Login loginform) {
        this.clientFrame = ClientFrame;
        this.loginform = loginform;
        this.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientThread(Socket socket, Client ClientFrame, register registerform) {
        this.clientFrame = ClientFrame;
        this.registerform = registerform;
        this.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientThread(Socket socket, Client ClientFrame, UpdateDetails1 updateDetails1Frame) {
        this.clientFrame = ClientFrame;
        this.updateDetails1Frame = updateDetails1Frame;
        this.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientThread(Socket socket, Client ClientFrame, UpdateDetails2 updateDetails2Frame) {
        this.clientFrame = ClientFrame;
        this.updateDetails2Frame = updateDetails2Frame;
        this.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void beginCallStream() {
        try {
            // Create call control frame
            JFrame callControlFrame = new JFrame("Audio Call");
            JButton muteButton = new JButton("Mute");
            JButton endCallButton = new JButton("End Call");
            JLabel status = new JLabel("Status");
            callControlFrame.setLayout(new FlowLayout());
            callControlFrame.add(muteButton);
            callControlFrame.add(endCallButton);
            callControlFrame.add(status);
            callControlFrame.setSize(200, 100);
            callControlFrame.setVisible(true);

            status.setText("Connected to the caller");

            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            targetLine = AudioSystem.getTargetDataLine(format);
            targetLine.open(format);
            targetLine.start();

            sourceLine = AudioSystem.getSourceDataLine(format);
            sourceLine.open(format);
            sourceLine.start();

            socket = new Socket("localhost", 5000);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            // Variables to control mute and running state
            boolean[] isMuted = {false};
            boolean[] running = {true};

            // Capture thread
            Thread captureThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (running[0]) {
                    int bytesRead = targetLine.read(buffer, 0, buffer.length);
                    try {
                        if (!isMuted[0]) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException ex) {
                        if (!running[0]) {
                            break;
                        }
                        ex.printStackTrace();
                    }
                }
            });
            captureThread.start();

            // Playback thread
            Thread playbackThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (running[0]) {
                    try {
                        int bytesRead = inputStream.read(buffer);
                        if (bytesRead == -1) {
                            break; // End of stream
                        }
                        sourceLine.write(buffer, 0, bytesRead);
                    } catch (IOException e) {
                        if (!running[0]) {
                            break;
                        }
                        e.printStackTrace();
                    }
                }
            });
            playbackThread.start();

            // Mute button action listener
            muteButton.addActionListener(e -> {
                isMuted[0] = !isMuted[0];
                muteButton.setText(isMuted[0] ? "Unmute" : "Mute");
            });

            // End call button action listener
            endCallButton.addActionListener(e -> {
                running[0] = false; // Stop the threads
                status.setText("Call ended");
                try {
                    targetLine.stop();
                    targetLine.close();
                    sourceLine.stop();
                    sourceLine.close();
                    socket.close();
                    callControlFrame.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not connect to the server IP address", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void beginVideoCallStream() {
        try {
            JFrame callControlFrame = new JFrame("Call Controls");
            JLabel callerLabel = new JLabel("Caller's Video");
            JLabel calleeLabel = new JLabel("Your Video");
            JButton muteButton = new JButton("Mute");
            JButton endCallButton = new JButton("End Call");
            JLabel status = new JLabel("Status: Connected");

            callControlFrame.setLayout(new FlowLayout());
            callControlFrame.add(callerLabel);
            callControlFrame.add(calleeLabel);
            callControlFrame.add(muteButton);
            callControlFrame.add(endCallButton);
            callControlFrame.add(status);

            callControlFrame.setSize(800, 600);
            callControlFrame.setVisible(true);

            // Initialize webcam
            webcam = Webcam.getDefault();
            webcam.open();

            // Initialize audio
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            targetLine = AudioSystem.getTargetDataLine(format);
            targetLine.open(format);
            targetLine.start();

            sourceLine = AudioSystem.getSourceDataLine(format);
            sourceLine.open(format);
            sourceLine.start();

            OutputStream videoOutputStream = socket.getOutputStream();
            InputStream videoInputStream = socket.getInputStream();
            OutputStream audioOutputStream = socket.getOutputStream();
            InputStream audioInputStream = socket.getInputStream();

            running = true;

            // Video capture and playback threads
            Thread videoCaptureThread = new Thread(() -> {
                try {
                    while (running) {
                        BufferedImage image = webcam.getImage();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "jpg", baos);
                        baos.flush();
                        byte[] imageBytes = baos.toByteArray();
                        baos.close();

                        videoOutputStream.write(imageBytes);
                        videoOutputStream.flush();

                        ImageIcon calleeIcon = new ImageIcon(image);
                        SwingUtilities.invokeLater(() -> calleeLabel.setIcon(calleeIcon));

                        Thread.sleep(100); // Adjust based on frame rate
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

                        ImageIcon callerIcon = new ImageIcon(image);
                        SwingUtilities.invokeLater(() -> callerLabel.setIcon(callerIcon));
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
                    webcam.close();
                    targetLine.stop();
                    targetLine.close();
                    sourceLine.stop();
                    sourceLine.close();
                    socket.close();
                    callControlFrame.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not connect to the server IP address", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String data = dis.readUTF();
                token = new StringTokenizer(data);
                String CMD = token.nextToken();

                switch (CMD) {
                    case "CMD_LOGINCONFIRMED":
                        String username = token.nextToken();
                        String socketInfo = token.nextToken();
                        clientFrame.setVisible(true);
                        clientFrame.username = username;
                        clientFrame.socket = this.socket;
                        clientFrame.setTitle(username);
                        loginform.dispose();
                        JOptionPane.showMessageDialog(clientFrame, "Welcome " + username);
                        break;

                    case "CMD_LOGINNOTCONFIRMED":
                        JOptionPane.showMessageDialog(loginform, "Wrong Login Details");
                        break;

                    case "CMD_UPDATEDETAILSLOGINCONFIRMED":
                        String updateUsername = token.nextToken();
                        String usernameFromUpdateDetails1 = updateDetails1Frame.getUsername();
                        updateDetails2Frame = new UpdateDetails2(usernameFromUpdateDetails1);
                        updateDetails2Frame.setVisible(true);
                        updateDetails2Frame.username = updateUsername;
                        updateDetails2Frame.socket = this.socket;
                        updateDetails2Frame.setTitle(updateUsername);
                        updateDetails1Frame.dispose();
                        JOptionPane.showMessageDialog(updateDetails2Frame, "Welcome " + updateUsername);
                        break;

                    case "CMD_UPDATEDETAILSLOGINNOTCONFIRMED":
                        String failedUsername = token.nextToken();
                        JOptionPane.showMessageDialog(updateDetails1Frame, "Update details login failed for " + failedUsername + ". Please check your username and password.");
                        break;

                    case "CMD_UPDATECONFIRMED":
                        JOptionPane.showMessageDialog(updateDetails2Frame, "Update Successful");
                        break;

                    case "CMD_UPDATENOTCONFIRMED":
                        JOptionPane.showMessageDialog(updateDetails2Frame, "Issue with Update");
                        break;

                    case "CMD_REGISTERCONFIRMED":
                        JOptionPane.showMessageDialog(registerform, "Registration Successful");
                        break;

                    case "CMD_REGISTRATIONNOTCONFIRMED":
                        JOptionPane.showMessageDialog(registerform, "Issue with Registration");
                        break;

                    case "CMD_ONLINE":
                        Vector<String> online = new Vector<>();
                        while (token.hasMoreTokens()) {
                            String list = token.nextToken();
                            if (!list.equalsIgnoreCase(clientFrame.username)) {
                                online.add(list);
                            }
                        }
                        System.out.println(online);
                        clientFrame.appendOnlineList(online);
                        break;

                    //case "CMD_ALLUSERS":
                    case "CMD_CHAT":
                        String msgs = "";
                        String from = token.nextToken();
                        while (token.hasMoreTokens()) {
                            msgs = msgs + " " + token.nextToken();
                        }
                        alert = new MessageAlert();
                        alert.PrivateMsg();
                        String active = clientFrame.jLabel2.getText();
                        if (active.equalsIgnoreCase(from)) {
                            clientFrame.appendMessage("\n" + from + ": " + msgs);
                        }
                        break;

                    case "CMD_FILE_XD":
                        String sender = token.nextToken();
                        String receiver = token.nextToken();
                        String fname = token.nextToken();
                        int confirm = JOptionPane.showConfirmDialog(clientFrame,
                                "From: " + sender + "\nFilename: " + fname + "\nWould you like to Accept?");
                        alert = new MessageAlert();
                        alert.FileMsg();
                        if (confirm == 0) {
                            clientFrame.openFolder();
                            try {
                                dos = new DataOutputStream(socket.getOutputStream());
                                String format = "CMD_SEND_FILE_ACCEPT " + sender + " accepted";
                                dos.writeUTF(format);
                                Socket fSoc = new Socket(clientFrame.getMyHost(), clientFrame.getMyPort());
                                DataOutputStream fdos = new DataOutputStream(fSoc.getOutputStream());
                                fdos.writeUTF("CMD_SHARINGSOCKET " + clientFrame.getMyUsername());
                                new Thread(new ReceivingFileThread(fSoc, clientFrame)).start();
                            } catch (IOException e) {
                                System.out.println("[CMD_FILE_XD]: " + e.getMessage());
                            }
                        } else {
                            try {
                                dos = new DataOutputStream(socket.getOutputStream());
                                String format = "CMD_SEND_FILE_ERROR " + sender + " Client rejected your request or connection was lost.!";
                                dos.writeUTF(format);
                            } catch (IOException e) {
                                System.out.println("[CMD_FILE_XD]: " + e.getMessage());
                            }
                        }
                        break;

                    case "CMD_CALL_XD":
                        sender = token.nextToken();
                        receiver = token.nextToken();

                        confirm = JOptionPane.showConfirmDialog(clientFrame,
                                "From: " + sender + "\nwould you like to Accept.?");
                        alert = new MessageAlert();
                        alert.FileMsg();
                        if (confirm == 0) {
                            beginCallStream();

                        } else { // client rejected the request, then send back result to sender
                            try {
                                dos = new DataOutputStream(socket.getOutputStream());
                                String format = "CALL_NOT_ACCEPTED " + sender;
                                dos.writeUTF(format);
                                dos.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "CMD_VIDEOCALL_XD":  // Format:  CMD_FILE_XD [sender] [receiver] [filename]
                        sender = token.nextToken();
                        receiver = token.nextToken();

                        confirm = JOptionPane.showConfirmDialog(clientFrame,
                                "From: " + sender + "\nwould you like to Accept.?");
                        alert = new MessageAlert();
                        alert.FileMsg();
                        if (confirm == 0) {
                            beginVideoCallStream();

                        } else { // client rejected the request, then send back result to sender
                            try {
                                dos = new DataOutputStream(socket.getOutputStream());
                                String format = "CALL_NOT_ACCEPTED " + sender;
                                dos.writeUTF(format);
                                dos.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "CMD_FETCHMSG":
                        String msg = "";
                        while (token.hasMoreTokens()) {
                            msg = msg + " " + token.nextToken();
                        }
                        clientFrame.appendMessage(msg + "\n");
                        break;

                    case "CMD_NOTIFICATION":
                        msg = "";
                        while (token.hasMoreTokens()) {
                            msg = msg + " " + token.nextToken();
                        }
                        clientFrame.appendNotification(msg + "\n");
                        break;

                    case "CMD_LEAVE":
                        String name = token.nextToken();
                        JOptionPane.showMessageDialog(clientFrame, name + " has logged out.");
                        break;

                    case "CMD_JOIN":
                        String joinname = token.nextToken();
                        JOptionPane.showMessageDialog(clientFrame, joinname + " has logged in.");
                        break;

                    default:
                        clientFrame.appendMessage("[CMDException]: Unknown Command " + CMD);
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
