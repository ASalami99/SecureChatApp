package chatapp;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;

public class SocketThread implements Runnable {

    Socket socket;
    Server serverform;
    DataInputStream dis;
    DataOutputStream dos;
    StringTokenizer st;
    dbconnection con;
    PreparedStatement ps;
    ResultSet rs;
    String client, oname, sname, username, email, phone, password, filesharing_username;
    static byte photo[] = null;

    final int BUFFER_SIZE = 100;

    public SocketThread(Socket socket, Server serverform) {
        this.serverform = serverform;
        this.socket = socket;

        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createConnection(String receiver, String sender, String filename) {
        try {

            Socket s = serverform.getClientList(receiver);
            if (s != null) {
                DataOutputStream dosS = new DataOutputStream(s.getOutputStream());
                // Format:  CMD_FILE_XD [sender] [receiver] [filename]
                String format = "CMD_FILE_XD " + sender + " " + receiver + " " + filename;
                dosS.writeUTF(format);

            } else {

                serverform.appendMessage("Client was not found '" + receiver + "'");
                dos.writeUTF("CMD_SENDFILEERROR " + "Client '" + receiver + "' was not found in the list, make sure it is on the online list.!");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
            private void createConnectionCall(String receiver, String sender) {
        try {

            Socket s = serverform.getClientList(receiver);
            if (s != null) {
                DataOutputStream dosS = new DataOutputStream(s.getOutputStream());
                // Format:  CMD_FILE_XD [sender] [receiver] [filename]
                String format = "CMD_CALL_XD " + sender + " " + receiver;
                dosS.writeUTF(format);

            } else {

                serverform.appendMessage("Client was not found '" + receiver + "'");
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF("CMD_SENDFILEERROR " + "Client '" + receiver + "' was not found in the list, make sure it is on the online list.!");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
private void createConnectionVideoCall(String receiver, String sender) {
        try {

            Socket s = serverform.getClientList(receiver);
            if (s != null) {
                DataOutputStream dosS = new DataOutputStream(s.getOutputStream());
                // Format:  CMD_FILE_XD [sender] [receiver] [filename]
                String format = "CMD_VIDEOCALL_XD " + sender + " " + receiver;
                dosS.writeUTF(format);

            } else {

                serverform.appendMessage("Client was not found '" + receiver + "'");
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF("CMD_SENDFILEERROR " + "Client '" + receiver + "' was not found in the list, make sure it is on the online list.!");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
}
    @Override
    public void run() {
        try {
            while (true) {
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                String CMD = st.nextToken();
                //serverform.appendMessage(data + " connected \n");
                /**
                 * Check COMMAND *
                 */
                switch (CMD) {
                    case "CMD_LOGIN":
                        /**
                         * CMD_LOGIN [Username] [PASSWORD]*
                         */
                        username = st.nextToken();
                        password = st.nextToken();
                        client = username;
                        try {
                            con = new dbconnection();
                            ps = con.psStatement("Select * from login where username=? and password=? ");
                            ps.setString(1, username);
                            ps.setString(2, password);
                            rs = ps.executeQuery();

                            if (rs.next()) {

                                serverform.appendMessage(username + " connected \n");

                                dos.writeUTF("CMD_LOGINCONFIRMED " + username + " " + socket);
                                dos.flush();
                                serverform.setClientList(username);
                                serverform.setSocketList(socket);
                                serverform.appendMessage("" + username + " logged in successfully");
                                for (int x = 0; x < serverform.clientList.size(); x++) {
                                    if (!serverform.clientList.elementAt(x).equals(username)) {
                                        try {
                                            Socket tsoc2 = (Socket) serverform.socketList.elementAt(x);
                                            DataOutputStream dos2 = new DataOutputStream(tsoc2.getOutputStream());
                                            dos2.writeUTF("CMD_JOIN " + username);
                                        } catch (IOException e) {

                                        }
                                    }
                                }
                            } else {
                                dos.writeUTF("CMD_LOGINNOTCONFIRMED " + username);
                                dos.flush();
                            }
                        } catch (Exception e) {
                            serverform.appendMessage("Error when trying to log in" + username + e);
                        }
                        break;

                    case "CMD_UPDATEDETAILSLOGIN":
                        /**
                         * CMD_UPDATEDETAILSLOGIN [Username] [PASSWORD]*
                         */
                        username = st.nextToken();
                        password = st.nextToken();
                        client = username;
                        try {
                            con = new dbconnection();
                            ps = con.psStatement("Select * from login where username=? and password=? ");
                            ps.setString(1, username);
                            ps.setString(2, password);
                            rs = ps.executeQuery();

                            if (rs.next()) {
                                serverform.appendMessage(username + " attempting to update details \n");

                                dos.writeUTF("CMD_UPDATEDETAILSLOGINCONFIRMED " + username);
                                dos.flush();
                            } else {
                                dos.writeUTF("CMD_UPDATEDETAILSLOGINNOTCONFIRMED " + username);
                                dos.flush();
                            }
                        } catch (Exception e) {
                            serverform.appendMessage("Error when trying to update details for " + username + e);
                        }

                        break;

                    case "CMD_CALL":                         
                        try {
                        String caller = st.nextToken();
                        String callee = st.nextToken();

                        serverform.appendMessage("CMD_CALL Host: " + caller);
                        this.createConnectionCall(callee, caller);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    break;

                    case "CMD_UPDATE":
                        /**
                        * CMD_UPDATE [SNAME] [ONAME] [EMAIL] [PHONE] [USERNAME] [PASSWORD] [PHOTO]
                        */
                        try {
                        sname = st.nextToken();
                        oname = st.nextToken();
                        email = st.nextToken();
                        phone = st.nextToken();
                        username = st.nextToken();
                        password = st.nextToken();
//                        String photoStr = st.nextToken();
//                        byte[] photo = photoStr.getBytes();  // Convert photo string to bytes if needed
                        int size = dis.readInt();
                        byte[] imageData = new byte[size];
                        dis.readFully(imageData);
                        client = username;

                        con = new dbconnection();
                        PreparedStatement ps = con.psStatement("UPDATE login SET surname=?, firstname=?, email=?, phone=?, password=?, profilephoto=? WHERE username=?");
                        ps.setString(1, sname);
                        ps.setString(2, oname);
                        ps.setString(3, email);
                        ps.setString(4, phone);
                        ps.setString(5, password);
                        ps.setBytes(6, imageData);
                        ps.setString(7, username);

//                        dos.writeUTF("CMD_UPDATECONFIRMED " + username);
//                        dos.flush();
//                        serverform.appendMessage("" + username + " UPDATED successfully");
                        int result = ps.executeUpdate();
                        if (result > 0) {
                            dos.writeUTF("CMD_UPDATECONFIRMED " + username);
                            dos.flush();
                            serverform.appendMessage("" + username + " UPDATED successfully");
                        } else {
                            dos.writeUTF("CMD_UPDATENOTCONFIRMED " + username);
                            serverform.appendMessage("Error updating details for " + username);
                        }

                    } catch (Exception e) {
                        dos.writeUTF("CMD_UPDATENOTCONFIRMED " + username);
                        serverform.appendMessage("Error when trying to update details for " + username + ": " + e);
                    }
                    break;

                    case "CMD_REGISTER":
                        /**
                         * CMD_REGISTER [SNAME] [ONAME] [EMAIL] [PHONE]
                         * [USERNAME] [PASSWORD]*
                         */
                        try {
                        sname = st.nextToken();
                        oname = st.nextToken();
                        email = st.nextToken();
                        phone = st.nextToken();
                        username = st.nextToken();
                        password = st.nextToken();
                        //String photoStr = st.nextToken();
                        int size = dis.readInt();
                        byte[] imageData = new byte[size];
                        dis.readFully(imageData);
                        // byte[] photo = photoStr.getBytes();  // Convert photo string to bytes if needed
                        client = username;

                        con = new dbconnection();
                        PreparedStatement ps = con.psStatement("insert into login values (?,?,?,?,?,?,?) ");
                        ps.setString(1, sname);
                        ps.setString(2, oname);
                        ps.setString(3, email);
                        ps.setString(4, phone);
                        ps.setString(5, username);
                        ps.setString(6, password);
                        ps.setBytes(7, imageData);
                        int rs = ps.executeUpdate();
                        serverform.appendMessage(username + " connected \n");

                        dos.writeUTF("CMD_REGISTERCONFIRMED " + username);
                        dos.flush();
                        serverform.appendMessage("" + username + " REGISTERD successfully");

                    } catch (Exception e) {
                        dos.writeUTF("CMD_REGISTRATIONNOTCONFIRMED " + username);
                        serverform.appendMessage("Error when trying to register" + username + e);
                    }

                    break;

                    case "CMD_CHAT":

                        /**
                         * CMD_CHAT [from] [sendTo] [message] *
                         */
                        String from = st.nextToken();
                        String sendTo = st.nextToken();
                        String msg = "";
                        
                        while (st.hasMoreTokens()) {
                            msg = msg + " " + st.nextToken();
                        }
                        Socket tsoc = serverform.getClientList(sendTo);
                        try {

                            // Now we need to store this message in the database
                            try {
                                Encryption encrypt= new Encryption();
                        byte[] message = encrypt.encryptText(msg);
                                con = new dbconnection();
                                // table format sender, reciever, msg, timestamp, status
                                PreparedStatement ps = con.psStatement("insert into messages values (?,?,?,?,?)");
                                ps.setString(1, from);
                                ps.setString(2, sendTo);
                                ps.setBytes(3, message);
                                // to get timestamp
                                LocalDateTime now = LocalDateTime.now();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                String Timestamp = now.format(formatter);
                                ps.setString(4, Timestamp);
                                ps.setString(5, "Not Read");
                                int rs = ps.executeUpdate();
                                con.close();
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(serverform, e);
                            }

                            if (tsoc != null) {
                                dos = new DataOutputStream(tsoc.getOutputStream());
                                String content = from + " " + msg;
                                dos.writeUTF("CMD_CHAT " + content);
                            }
                            serverform.appendMessage("Message: From " + from + " To " + sendTo + " : " + msg);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, e);

                        }
                        break;
                        case "CMD_VIDEO":
                        try {
                            String caller = st.nextToken();
                            String callee = st.nextToken();

                            serverform.appendMessage("CMD_VIDEO Host: " + caller);
                            this.createConnectionVideoCall(callee, caller);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                        case "CMD_GETMESSAGES":

                        /**
                         * CMD_GETMESSAGES [from] [users]*
                         */
                        String with = st.nextToken();
                        String me = st.nextToken();
                        Socket mysocket = serverform.getClientList(me);

                        try {
                            dos = new DataOutputStream(mysocket.getOutputStream());

                            // Now we need to store this message in the database
                            con = new dbconnection();
                            // table format sender, reciever, msg, timestamp, status
                            ps = con.psStatement("select * from messages  where sender=? and reciever=? union "
                                    + "select * from messages  where sender=? and reciever=? order by timestamp");
                            ps.setString(1, me);
                            ps.setString(2, with);
                            ps.setString(3, with);
                            ps.setString(4, me);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                String sender = rs.getString(1);
                                 byte[] message = rs.getBytes(3);
                                 Encryption decrypt=new Encryption();
                                 String messages = decrypt.decryptText(message);
                                String time = rs.getString(4);
                                if (sender.equalsIgnoreCase(me)) {
                                    dos.writeUTF("CMD_FETCHMSG " + me + ":" + messages + " :" + time);
                                } else {
                                    dos.writeUTF("CMD_FETCHMSG " + with + ":" + messages + " :" + time);
                                }

                            }

                            // Here you can now modify status from Not Read to Read
                            ps = con.psStatement("update messages  set status=? where sender=? and reciever=?");
                            ps.setString(1, "Read");
                            ps.setString(2, with);
                            ps.setString(3, me);
                            int rs = ps.executeUpdate();

                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(serverform, e);
                        }

                        serverform.appendMessage("Retrieve messages between " + with + " and " + me);

                        break;

                    case "CMD_NOTIFICATION":

                        /**
                         * CMD_GETMESSAGES [from] [users]*
                         */
                        String friend = st.nextToken();
                        String myusername = st.nextToken();
                        Socket Mysocket = serverform.getClientList(myusername);
                        int count = 0;

                        try {
                            dos = new DataOutputStream(Mysocket.getOutputStream());
                            con = new dbconnection();

                            ps = con.psStatement("SELECT count(*) FROM messages where sender=? and reciever=? and status=?");
                            ps.setString(1, friend);
                            ps.setString(2, myusername);
                            ps.setString(3, "Not Read");
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                count = rs.getInt(1);
                            }

                            dos.writeUTF("CMD_NOTIFICATION You have " + count + " Unread message(s) from " + friend);

                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(serverform, e);
                        }

                        serverform.appendMessage("Notification for " + friend + " and " + myusername);

                        break;

                    case "CMD_SHARINGSOCKET":
                        serverform.appendMessage("CMD_SHARINGSOCKET : Client stablish a socket connection for file sharing...");
                        String file_sharing_username = st.nextToken();
                        filesharing_username = file_sharing_username;
                        serverform.setClientFileSharingUsername(file_sharing_username);
                        serverform.setClientFileSharingSocket(socket);
                        serverform.appendMessage("CMD_SHARINGSOCKET : Username: " + file_sharing_username);

                        break;

                    case "CMD_SENDFILE":
                        serverform.appendMessage("CMD_SENDFILE : Client sending a file...");
                        /*
                        Format: CMD_SENDFILE [Filename] [Size] [Recipient] [Sender]  from: Sender Format
                        Format: CMD_SENDFILE [Filename] [Size] [Sender] to Receiver Format
                         */
                        String file_name = st.nextToken();
                        String filesize = st.nextToken();
                        String sendto = st.nextToken();
                        String Sender = st.nextToken();
                        serverform.appendMessage("CMD_SENDFILE : From: " + Sender);
                        serverform.appendMessage("CMD_SENDFILE : To: " + sendto);
                        serverform.appendMessage("CMD_SENDFILE : preparing connections..");
                        Socket cSock = serverform.getClientFileSharingSocket(sendto);

                        if (cSock != null) {
                            try {
                                DataOutputStream cDos = new DataOutputStream(cSock.getOutputStream());
                                cDos.writeUTF("CMD_SENDFILE " + file_name + " " + filesize + " " + Sender);
                                InputStream input = socket.getInputStream();
                                OutputStream sendFile = cSock.getOutputStream();
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int cnt;
                                while ((cnt = input.read(buffer)) > 0) {
                                    sendFile.write(buffer, 0, cnt);
                                }
                                sendFile.flush();
                                sendFile.close();
                                serverform.removeClientFileSharing(sendto);
                                serverform.removeClientFileSharing(Sender);

                            } catch (IOException e) {
                                System.err.println(e.getMessage());
                            }
                        } else {
                            /*   FORMAT: CMD_SENDFILEERROR  */
                            serverform.removeClientFileSharing(Sender);
                            serverform.appendMessage("CMD_SENDFILE : Client '" + sendto + "' was not found.!");
                            dos.writeUTF("CMD_SENDFILEERROR " + "Client '" + sendto + "' was not found, File Sharing will exit.");
                        }
                        break;

                    case "CMD_SENDFILERESPONSE":
                        /*
                        Format: CMD_SENDFILERESPONSE [username] [Message]
                         */
                        String receiver = st.nextToken();
                        String rMsg = "";
                        serverform.appendMessage("[CMD_SENDFILERESPONSE]: username: " + receiver);
                        while (st.hasMoreTokens()) {
                            rMsg = rMsg + " " + st.nextToken();
                        }
                        try {
                            Socket rSock = (Socket) serverform.getClientFileSharingSocket(receiver);
                            DataOutputStream rDos = new DataOutputStream(rSock.getOutputStream());
                            rDos.writeUTF("CMD_SENDFILERESPONSE" + " " + receiver + " " + rMsg);
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                        break;

                    case "CMD_SEND_FILE_XD":  // Format: CMD_SEND_FILE_XD [sender] [receiver]                        
                        try {
                        String send_sender = st.nextToken();
                        String send_receiver = st.nextToken();
                        String send_filename = st.nextToken();
                        serverform.appendMessage("CMD_SEND_FILE_XD Host: " + send_sender);
                        this.createConnection(send_receiver, send_sender, send_filename);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    break;

                    case "CMD_SEND_FILE_ERROR":
                        // Format:  CMD_SEND_FILE_ERROR [receiver] [Message]
                        String eReceiver = st.nextToken();
                        String eMsg = "";
                        while (st.hasMoreTokens()) {
                            eMsg = eMsg + " " + st.nextToken();
                        }
                        try {

                            Socket eSock = serverform.getClientFileSharingSocket(eReceiver);
                            DataOutputStream eDos = new DataOutputStream(eSock.getOutputStream());
                            //  Format:  CMD_RECEIVE_FILE_ERROR [Message]
                            eDos.writeUTF("CMD_RECEIVE_FILE_ERROR " + eMsg);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        break;

                    case "CMD_SEND_FILE_ACCEPT": // Format:  CMD_SEND_FILE_ACCEPT [receiver] [Message]
                        String aReceiver = st.nextToken();
                        String aMsg = "";
                        while (st.hasMoreTokens()) {
                            aMsg = aMsg + " " + st.nextToken();
                        }
                        try {
                            /*  Send Error to the File Sharing host  */
                            Socket aSock = serverform.getClientFileSharingSocket(aReceiver); // get the file sharing host socket for connection
                            DataOutputStream aDos = new DataOutputStream(aSock.getOutputStream());
                            //  Format:  CMD_RECEIVE_FILE_ACCEPT [Message]
                            aDos.writeUTF("CMD_RECEIVE_FILE_ACCEPT " + aMsg);
                        } catch (IOException e) {
                            serverform.appendMessage("[CMD_RECEIVE_FILE_ERROR]: " + e.getMessage());
                        }
                        break;

                    case "CMD_LEAVE":

                        /**
                         * CMD_CHATALL [from] [message] *
                         */
                        String leaver = st.nextToken();
                        for (int x = 0; x < serverform.clientList.size(); x++) {
                            if (!serverform.clientList.elementAt(x).equals(leaver)) {
                                try {
                                    Socket tsoc2 = (Socket) serverform.socketList.elementAt(x);
                                    DataOutputStream dos2 = new DataOutputStream(tsoc2.getOutputStream());
                                    dos2.writeUTF("CMD_LEAVE " + leaver);
                                } catch (IOException e) {

                                }
                            }
                        }
                        serverform.appendMessage("CMD_LEAVE " + leaver + " has logged out.");
                        break;

                    default:
                        serverform.appendMessage("Unknown Command " + CMD);
                        break;
                }
            }
        } catch (IOException e) {

            serverform.removeFromTheList(client);
            serverform.appendMessage("Connection closed..!");
        }
    }

}
