package test;

import data.User;
import util.CommonUtils;
import util.EncryptionUtils;
import util.KeyGenerator;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * Created by lifengshuang on 13/05/2017.
 */
public class Client {

    private static String username;
    private static KeyPair tempKeyPair;
    private static KeyPair keyPair;
    private static PublicKey serverPublicKey;

    public static void main(String[] args) {
        testRegister();
        testGetAllUsers();
    }

    private static void testRegister() {
        try {
            Socket socket = new Socket("localhost", 2333);
            tempKeyPair = KeyGenerator.generateRSAKey("key/temp.pri", "key/temp.pub");
            serverPublicKey = KeyGenerator.loadPublicKey("key/server.pub");
            MessageHeader header = new MessageHeader();
            username = "lfs" + System.currentTimeMillis();
            header
                    .add("Service", "register")
                    .add("Username", username);
            byte[] body = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(tempKeyPair.getPublic()), serverPublicKey);
            MessageWrapper request = new MessageWrapper(header, body, serverPublicKey, tempKeyPair.getPrivate());
            System.out.println(request);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request.getWrappedData());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            byte[] data = (byte[])inputStream.readObject();
            MessageWrapper response = new MessageWrapper(data, serverPublicKey, tempKeyPair.getPrivate());
            System.out.println(response);
            keyPair = (KeyPair)CommonUtils.stringToObject(EncryptionUtils.decryptWithRSA(response.getBody(), tempKeyPair.getPrivate()));
            System.out.println(keyPair.getPrivate());
            System.out.println(keyPair.getPublic());
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testGetAllUsers() {
        try {
            Socket socket = new Socket("localhost", 2333);
            MessageHeader header = new MessageHeader();
            header
                    .add("Service", "getAllUsers")
                    .add("Username", username);
            MessageWrapper request = new MessageWrapper(header, new byte[0], serverPublicKey, keyPair.getPrivate());
            System.out.println(request);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request.getWrappedData());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            byte[] data = (byte[])inputStream.readObject();
            MessageWrapper response = new MessageWrapper(data, serverPublicKey, keyPair.getPrivate());
            System.out.println(response);
            User[] users = (User[]) CommonUtils.stringToObject(EncryptionUtils.decryptWithRSA(response.getBody(), keyPair.getPrivate()));
            System.out.println(Arrays.toString(users));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
