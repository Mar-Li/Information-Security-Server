package test;

import util.CommonUtils;
import util.EncryptionUtils;
import util.KeyGenerator;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import java.io.IOException;
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
    public static void main(String[] args) {
        testRegister();
    }

    public static void testRegister() {
        try {
            Socket socket = new Socket("localhost", 2333);
            KeyPair keyPair = KeyGenerator.generateRSAKey("key/temp.pri", "key/temp.pub");
            PublicKey serverPublicKey = KeyGenerator.loadPublicKey("key/server.pub");
            MessageHeader header = new MessageHeader();
            header
                    .add("Service", "register")
                    .add("Username", "lfs" + System.currentTimeMillis());
            byte[] body = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(keyPair.getPublic()), serverPublicKey);
            MessageWrapper request = new MessageWrapper(header, body, serverPublicKey, keyPair.getPrivate());
            System.out.println(request);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request.getWrappedData());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            byte[] data = (byte[])inputStream.readObject();
            MessageWrapper response = new MessageWrapper(data, serverPublicKey, keyPair.getPrivate());
            System.out.println(response);
            KeyPair myKeyPair = (KeyPair)CommonUtils.stringToObject(EncryptionUtils.decryptWithRSA(response.getBody(), keyPair.getPrivate()));
            System.out.println(myKeyPair);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
