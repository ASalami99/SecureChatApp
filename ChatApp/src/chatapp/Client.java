package chatapp;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JOptionPane;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static java.util.Collections.list;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author New
 */
public class Client extends javax.swing.JFrame {

    String username, command, password, sname, oname, phone, email;
    String host;
    register registerform;
    Login loginform;
    int port;
    Socket socket;
    DataOutputStream dos;
    String SendTo = "";
    Thread t;
    byte photo[] = null;
    BufferedImage img;
    boolean attachmentOpen = false;
    boolean isConnected = false;
    String mydownloadfolder = "C:\\";
    UpdateDetails1 updateDetails1Frame;
    UpdateDetails2 updateDetails2Frame;

    /**
     * Creates new form Client
     */
    public Client() {
        initComponents();
        Notification r = new Notification();
        t = new Thread(r);
        t.start();
        // AllContacts();
        populateUserList();

    }

    public void verifyLoginDetails(String username, String host, int port, String command, String password, Login loginform) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.command = command;
        this.password = password;
        this.loginform = loginform;
        connect();
    }

    public void connect() {
        System.out.println("Login");
        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            /**
             * Send username and password *
             */
            dos.writeUTF("CMD_LOGIN " + username + " " + password);
            // dos.writeUTF(command + username+" "+password);
            System.out.println("Login details sent to sever");

            /**
             * Start Client Thread *
             */
            ClientThread clientThread = new ClientThread(socket, this, loginform);
            Thread thread = new Thread(clientThread);
            thread.start();

        } catch (IOException e) {

            JOptionPane.showMessageDialog(this,
                    "Unable to Connect to Server, please try again later.!",
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);

        }
    }

    public void RegistrationDetails(String sname, String oname, String email, String phone, String username, String host, int port, String command, String password, byte[] photo, register registerform) {
        this.username = username;
        this.sname = sname;
        this.oname = oname;
        this.email = email;
        this.phone = phone;
        this.host = host;
        this.port = port;
        this.command = command;
        this.password = password;
        this.photo = photo;
        this.registerform = registerform;
        register();
    }

    public void UpdateDetails(String sname, String oname, String email, String phone, String username, String host, int port, String command, String password, byte[] photo, UpdateDetails2 updateDetails2Frame) {
        this.username = username;
        this.sname = sname;
        this.oname = oname;
        this.email = email;
        this.phone = phone;
        this.host = host;
        this.port = port;
        this.command = command;
        this.password = password;
        this.photo = photo;
        this.updateDetails2Frame = updateDetails2Frame;
        update();
    }

    public void update() {
        System.out.println("Update");
        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            /**
             * Send username and password *
             */
            dos.writeUTF("CMD_UPDATE " + sname + " " + oname + " " + email + " " + phone + " " + username + " " + password);
            dos.writeInt(photo.length);
            dos.write(photo);
            System.out.println("Update details sent to sever");

            /**
             * Start Client Thread *
             */
            ClientThread clientThread = new ClientThread(socket, this, updateDetails2Frame);
            Thread thread = new Thread(clientThread);
            thread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to Connect to Server, please try again later.!",
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);

        }
    }

    public void register() {
        System.out.println("Registration");
        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            /**
             * Send username and password *
             */
            dos.writeUTF("CMD_REGISTER " + sname + " " + oname + " " + email + " " + phone + " " + username + " " + password);
            dos.writeInt(photo.length);
            dos.write(photo);

            System.out.println("Registration  details sent to sever");

            /**
             * Start Client Thread *
             */
            ClientThread clientThread = new ClientThread(socket, this, registerform);
            Thread thread = new Thread(clientThread);
            thread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to Connect to Server, please try again later.!",
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);

        }
    }

    public void verifyUpdateLoginDetails(String username, String host, int port, String command, String password, UpdateDetails1 updateDetails1Frame) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.command = command;
        this.password = password;
        this.updateDetails1Frame = updateDetails1Frame;
        correct(updateDetails1Frame);
    }

    public void correct(UpdateDetails1 updateDetails1Frame) {
        System.out.println("Login");
        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            /**
             * Send username and password *
             */
            dos.writeUTF("CMD_UPDATEDETAILSLOGIN " + username + " " + password);
            // dos.writeUTF(command + username+" "+password);
            System.out.println("Login details sent to sever");

            /**
             * Start Client Thread *
             */
            ClientThread clientThread = new ClientThread(socket, this, updateDetails1Frame);
            Thread thread = new Thread(clientThread);
            thread.start();

        } catch (IOException e) {

            JOptionPane.showMessageDialog(this,
                    "Unable to Connect to Server, please try again later.!",
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);

        }
    }

    public void appendMessage(String msg) {
        jTextArea1.append(msg);
    }

    public void appendNotification(String msg) {
        jLabel4.setText("");
        jLabel4.setText(msg);
    }

    public void appendOnlineList(Vector list) {

        OnlineList(list);
    }

    private void OnlineList(Vector list) {
        list1.removeAll();
        Iterator i = list.iterator();
        while (i.hasNext()) {
            try {
                Object e = i.next();
                String users = String.valueOf(e);
                list1.add(users);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }

        }
    }

//    public void AllContacts() {
//        try {
//            list2.removeAll();
//            Class.forName("com.mysql.cj.jdbc.Driver");   // loading the driver
//            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "password999...");
//
//            PreparedStatement ps = con.prepareStatement("SELECT username FROM login");
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//                String firstname = rs.getString("username");
//                list2.add(firstname);
//            }
//
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(this, "Failed to retrieve contacts from database: " + e.getMessage());
//        }
//    }
    public Vector<String> fetchAllUsers() {
        Vector<String> users = new Vector<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load the driver
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "password999...");
            PreparedStatement ps = con.prepareStatement("SELECT username FROM login");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                users.add(username);
            }

            rs.close();
            // stmt.close();
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + e.getMessage());
        }
        return users;
    }

    public void populateUserList() {
        Vector<String> users = fetchAllUsers();
        list2.removeAll();
        for (String user : users) {
            list2.add(user);
        }
    }

    public void setMyTitle(String s) {
        setTitle(s);
    }

    public String getMyHost() {
        return this.host;
    }

    public int getMyPort() {
        return this.port;
    }

    public void updateAttachment(boolean b) {
        this.attachmentOpen = b;
    }

    public void openFolder() {
        jFileChooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int open = jFileChooser1.showDialog(this, "Select Folder");
        if (open == jFileChooser1.APPROVE_OPTION) {
            mydownloadfolder = jFileChooser1.getSelectedFile().toString() + "\\";
        } else {
            mydownloadfolder = "C:\\";
        }
    }

    public String getMyDownloadFolder() {
        return this.mydownloadfolder;
    }

    public String getMyUsername() {
        return this.username;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        list1 = new java.awt.List();
        jPanel4 = new javax.swing.JPanel();
        list2 = new java.awt.List();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 255, 204));

        jPanel2.setBackground(new java.awt.Color(0, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Contacts"));

        jTabbedPane1.setBackground(new java.awt.Color(204, 255, 204));
        jTabbedPane1.setOpaque(true);

        jPanel5.setBackground(new java.awt.Color(255, 204, 204));

        list1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                list1MouseClicked(evt);
            }
        });
        list1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                list1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(list1, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(list1, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Online", jPanel5);

        list2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                list2MouseClicked(evt);
            }
        });
        list2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                list2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(list2, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(list2, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("All Contacts", jPanel4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 204));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Message"));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jButton1.setText("Send Message");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Rockwell", 0, 14)); // NOI18N

        jButton2.setText("Video Call");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Voice Call");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Send Voicenote");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 438, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 141, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18))
        );

        jLabel1.setFont(new java.awt.Font("Rockwell", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Our Chat Application");

        jLabel3.setFont(new java.awt.Font("Rockwell", 0, 14)); // NOI18N
        jLabel3.setText("Notification");

        jLabel4.setFont(new java.awt.Font("Bodoni MT", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 0, 51));
        jLabel4.setText("jLabel4");

        jButton5.setText("Log Out");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 677, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(106, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(153, 153, 153)
                .addComponent(jButton5)
                .addGap(24, 24, 24))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jButton5))
                .addGap(1, 1, 1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jMenu1.setText(" Attachment");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        jMenuItem2.setText("Download Attachment");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        jMenuItem1.setText("Send Attachment");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Settings");

        jMenuItem3.setText("Modify Your Details");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void list1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_list1MouseClicked
//        jTextArea1.setText("");
//        SendTo = list1.getSelectedItem();
//        jLabel2.setText(SendTo);
////Get All Messages from Database BETWEEN  you and the friend
//        try {
//            dos = new DataOutputStream(socket.getOutputStream());
//            dos.writeUTF("CMD_GETMESSAGES " + SendTo + " " + username);
//        } catch (Exception e) {
//        }

        jTextArea1.setText("");
        SendTo = list1.getSelectedItem();
        jLabel2.setText(SendTo);

        // Get the profile photo from the database and set it to jLabel2
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load the driver
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "password999...");
            PreparedStatement ps = con.prepareStatement("SELECT profilephoto FROM login WHERE username = ?");
            ps.setString(1, SendTo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                byte[] imgBytes = rs.getBytes("profilephoto");
                if (imgBytes != null) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes);
                    BufferedImage img = ImageIO.read(bais);
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(jLabel5.getWidth(), jLabel5.getHeight(), Image.SCALE_SMOOTH));
                    jLabel5.setIcon(icon);
                } else {
                    jLabel5.setIcon(null);
                    jLabel5.setText("No Image");
                }
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load profile photo: " + e.getMessage());
            jLabel5.setIcon(null);
            jLabel5.setText("Error");
        }

        // Get all messages from the database between you and the friend
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("CMD_GETMESSAGES " + SendTo + " " + username);
        } catch (Exception e) {
            // Handle exception
        }

// TODO add your handling code here:
    }//GEN-LAST:event_list1MouseClicked


    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            String to = jLabel2.getText();
            /**
             * CMD_CHAT [from] [sendTo] [message] *
             */
            if (!to.equalsIgnoreCase("")) {
                String message = username + " " + to + " " + jTextArea2.getText();
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF("CMD_CHAT " + message);
                jTextArea1.append("\n" + username + " : " + jTextArea2.getText());
                jTextArea2.setText("");
            } else {
                JOptionPane.showMessageDialog(rootPane, "Select who you want to Chat With");

            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void list1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_list1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_list1ActionPerformed

    private void list2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_list2MouseClicked
        // Clear the text area
        jTextArea1.setText("");
        SendTo = list2.getSelectedItem();
        jLabel2.setText(SendTo);

        // Get the profile photo from the database and set it to jLabel5
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load the driver
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "password999...");
            PreparedStatement ps = con.prepareStatement("SELECT profilephoto FROM login WHERE username = ?");
            ps.setString(1, SendTo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                byte[] imgBytes = rs.getBytes("profilephoto");
                if (imgBytes != null) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes);
                    BufferedImage img = ImageIO.read(bais);
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(jLabel5.getWidth(), jLabel5.getHeight(), Image.SCALE_SMOOTH));
                    jLabel5.setIcon(icon);
                } else {
                    jLabel5.setIcon(null);
                    jLabel5.setText("No Image");
                }
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load profile photo: " + e.getMessage());
            jLabel5.setIcon(null);
            jLabel5.setText("Error");
        }

        // Get all messages from the database between you and the selected user
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load the driver
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "password999...");
            PreparedStatement ps = con.prepareStatement("SELECT sender, message FROM messages WHERE (sender = ? AND reciever = ?) OR (sender = ? AND reciever = ?)");
            ps.setString(1, SendTo);
            ps.setString(2, username);
            ps.setString(3, username);
            ps.setString(4, SendTo);
            ResultSet rs = ps.executeQuery();

            StringBuilder chatHistory = new StringBuilder();
            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message");
                chatHistory.append(sender).append(": ").append(message).append("\n");
            }

            jTextArea1.setText(chatHistory.toString());

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load chat history: " + e.getMessage());
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_list2MouseClicked

    private void list2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_list2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_list2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        String to = jLabel2.getText();
        if (to != null) {
            try {
                String format = "CMD_CALL " + username + " " + to;
                dos.writeUTF(format);
                System.out.println(format);

                jButton4.setEnabled(false);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Incomplete Form.!", "Error", JOptionPane.ERROR_MESSAGE);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        if (!attachmentOpen) {
            Attachment sendfile = new Attachment();
            if (sendfile.prepare(username, host, port, this)) {
                sendfile.setLocationRelativeTo(null);
                sendfile.setVisible(true);
                attachmentOpen = true;
            } else {
                JOptionPane.showMessageDialog(this,
                        "Unable to stablish File Sharing at this moment, please try again later.!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        try {
            jFileChooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int browse = jFileChooser1.showOpenDialog(this);
            if (browse == jFileChooser1.APPROVE_OPTION) {
                this.mydownloadfolder = jFileChooser1.getSelectedFile().toString() + "\\";
            }
        } catch (Exception e) {

        }
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        UpdateDetails1 u = new UpdateDetails1();
        u.setVisible(true);
        u.pack();
        u.setLocationRelativeTo(null);
        u.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //dispose();// TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Voicenotes v = new Voicenotes();
        v.setVisible(true);
        v.pack();
        v.setLocationRelativeTo(null);
        v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("CMD_LEAVE " + username);
            // TODO add your handling code here:
            this.dispose();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
String to = jLabel2.getText();
        if (to != null) {
            try {
                // Format: CMD_SEND_FILE_XD [sender] [receiver] [filename]
                
                String format = "CMD_VIDEO " + username + " " + to;
                dos.writeUTF(format);
                System.out.println(format);

                jButton4.setEnabled(false);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Incomplete Form.!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    //Notification Thread
    public class Notification implements Runnable {

        //int count = 0;
        String friend = "";

        @Override
        public void run() {
            while (true) {
                //count = list1.getItemCount();

                for (String i : list1.getItems()) {
                    try {
                        dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF("CMD_NOTIFICATION " + i + " " + username);

                        Thread.sleep(1900);
                    } catch (Exception e) {
                    }
                }

                /*
                
                for (int i = 0; i < list1.getItemCount(); i++) {
                    friend = list1.getItem(i);
                    try {
                        dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF("CMD_NOTIFICATION " + friend + " " + username);

                        Thread.sleep(1900);
                    } catch (Exception e) {
                    }

                }
                 */
            }

        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Client.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Client.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Client.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Client().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    public static javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    public static java.awt.List list1;
    public static java.awt.List list2;
    // End of variables declaration//GEN-END:variables
}
