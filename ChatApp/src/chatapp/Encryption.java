package chatapp;
import java.security.SecureRandom;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

public class Encryption {

    String msg = "";
    String message = "";
    String key = "";
    byte[] encryptedText;
    byte[] getEncryptedText;
    String decryptedText;

    Server serverform;

    public Encryption() {

    };
    
    
    public static byte[] encrypt(String plaintext, String secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        
        return cipher.doFinal(plaintext.getBytes());
    }

    public static String decrypt(byte[] ciphertext, String secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decryptedBytes = cipher.doFinal(ciphertext);
        return new String(decryptedBytes);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public String decryptText(byte[] message) {

        key = "a4B9xP3r1Z6nH8tY";
        System.out.println(key);
        int len = key.length();
        if (len != 16) {
            JOptionPane.showMessageDialog(serverform, "Key must be length 16");
        } else {
            if (message.equals("")) {
                JOptionPane.showMessageDialog(serverform, "Please enter text");
            } else {
                try {
                    //getEncryptedText = hexToBytes(message);
                    decryptedText = decrypt(message, key);

                } catch (Exception e) {

                }
            }
        }
        System.out.println(decryptedText);
        return decryptedText;
    }

    public byte[] encryptText(String message) {
//       msg = message;
        key = "a4B9xP3r1Z6nH8tY";
        System.out.println(key);
        int len = key.length();
        if (len != 16) {
            JOptionPane.showMessageDialog(serverform, "Key must be 16 characters");
        } else {
            if (message.equals("")) {
                JOptionPane.showMessageDialog(serverform, "Enter Message");
            } else {
                try {
                    encryptedText = encrypt(message, key); //the messaged pass is converted to an encypted byte
                    //jTextArea2.setText(bytesToHex(encryptedText));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(serverform, "Error encountered" + e);
                }
            }
        }
        System.out.println(encryptedText);
        
        return encryptedText;
    }


}
